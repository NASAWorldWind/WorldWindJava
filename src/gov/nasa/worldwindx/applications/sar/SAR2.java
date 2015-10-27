/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.sar.actions.SARScreenShotAction;
import gov.nasa.worldwindx.applications.sar.tracks.*;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author tag
 * @version $Id: SAR2.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SAR2 extends JFrame
{
    // Track and WWJ components.
    private TrackController trackController;
    private SARAnnotationSupport annotationSupport;
    @SuppressWarnings( {"FieldCanBeLocal"})
    private ScalebarHint scalebarHint;
    private WorldWindow wwd;
    // Timer components.
    @SuppressWarnings( {"FieldCanBeLocal"})
    private Timer redrawTimer;
    private static final int REDRAW_TIMER_DELAY = 1000;  // 1 sec
    // UI components.
    private ControlPanel controlPanel;
    private WWPanel wwPanel;
    private LayerMenu layerMenu;
    private ViewMenu viewLayerMenu;
    private JCheckBoxMenuItem feetMenuItem;
    private JCheckBoxMenuItem metersMenuItem;
    private JCheckBoxMenuItem angleDDMenuItem;
    private JCheckBoxMenuItem angleDMSMenuItem;
    private JButton viewExamineButton;
    private JButton viewFollowButton;
    private JButton viewFreeButton;
    private JButton extendTrackPlaneButton;
    private JButton extendTrackCursorAirButton;
    private JButton extendTrackCursorGroundButton;
    private JButton nextPointButton;
    private JButton removeLastPointButton;
    private JButton showTrackInfoButton;

    private JFileChooser openFileChooser;
    private SaveTrackDialog saveTrackDialog;
    private BulkDownloadFrame bulkDownloadFrame;
    private static final int OK = 0;
    private static final int CANCELLED = 2;
    private static final int ERROR = 4;
    // Unit constants.
    public static final String UNIT_IMPERIAL = "Imperial";
    public static final String UNIT_METRIC = "Metric";
    private final static double METER_TO_FEET = 3.280839895;

    // Help
    protected static final String ONLINE_HELP_URL
        = "http://worldwind.arc.nasa.gov/java/apps/SARApp/help/v6/SARHelp.html";

    // Preferences
    protected static AVList userPreferences = new AVListImpl();
    private Timer autoSaveTimer;
    protected static final long MIN_AUTO_SAVE_INTERVAL = 1000L;

    public SAR2()
    {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        initComponents();
        this.setTitle(SARApp.APP_NAME_AND_VERSION);

        this.wwd = this.wwPanel.getWwd();
        for (Layer layer : this.wwd.getModel().getLayers())
        {
            if (layer instanceof USGSDigitalOrtho)
            {
                layer.setOpacity(0.5);
                layer.setEnabled(false);
            }
            else if (layer instanceof USGSUrbanAreaOrtho)
            {
                layer.setEnabled(false);
            }
        }

        this.getAnalysisPanel().setWwd(this.wwd);

        trackController = new TrackController();
        this.trackController.setWwd(this.wwd);
        this.trackController.setTracksPanel(this.getTracksPanel());
        this.trackController.setAnalysisPanel(this.getAnalysisPanel());

        this.layerMenu.setWwd(this.wwd);
        this.viewLayerMenu.setWwd(this.wwd);

        this.annotationSupport = new SARAnnotationSupport();
        this.annotationSupport.setWwd(this.wwd);

        this.scalebarHint = new ScalebarHint();
        this.scalebarHint.setWwd(this.wwd);

        // Setup and start redraw timer - to force downloads to completion without user interaction
        this.redrawTimer = new Timer(REDRAW_TIMER_DELAY, new ActionListener()   // 1 sec
        {
            public void actionPerformed(ActionEvent event)
            {
                wwd.redraw();
            }
        });
        this.redrawTimer.start();

        // Set up property change listener on wwd
        this.wwd.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getPropertyName().equals(TrackViewPanel.VIEW_MODE_CHANGE)
                    || event.getPropertyName().equals(TrackController.TRACK_CURRENT)
                    || event.getPropertyName().equals(TrackController.BEGIN_TRACK_POINT_ENTRY)
                    || event.getPropertyName().equals(TrackController.END_TRACK_POINT_ENTRY))
                {
                    updateToolBar(event);
                }
            }
        });

        // Preferences
        this.autoSaveTimer = new Timer(0, new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                onAutoSave();
            }
        });
        this.initializeUserPreferences();
        this.loadUserPreferences();
        this.onUserPreferencesChanged();
    }

    public static void centerWindowInDesktop(Window window)
    {
        if (window != null)
        {
            int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
            int desktopWidth = screenWidth - screenInsets.left - screenInsets.right;
            int desktopHeight = screenHeight - screenInsets.bottom - screenInsets.top;
            int frameWidth = window.getSize().width;
            int frameHeight = window.getSize().height;

            if (frameWidth > desktopWidth)
                frameWidth = Math.min(frameWidth, desktopWidth);
            if (frameHeight > desktopHeight)
                frameHeight = Math.min(frameHeight, desktopHeight);

            window.setPreferredSize(new Dimension(
                frameWidth,
                frameHeight));
            window.pack();
            window.setLocation(
                (desktopWidth - frameWidth) / 2 + screenInsets.left,
                (desktopHeight - frameHeight) / 2 + screenInsets.top);
        }
    }

    public static double metersToFeet(double meters)
    {
        return meters * METER_TO_FEET;
    }

    public static double feetToMeters(double feet)
    {
        return feet / METER_TO_FEET;
    }

    public String getElevationUnit()
    {
        return getUserPreferences().getStringValue(SARKey.ELEVATION_UNIT);
    }

    public void setElevationUnit(String unit)
    {
        getUserPreferences().setValue(SARKey.ELEVATION_UNIT, unit);
        this.saveUserPreferences();
        this.onUserPreferencesChanged();
    }

    private void elevationUnitChanged(String oldValue, String newValue)
    {
        // Update unit menu selection.
        if (UNIT_IMPERIAL.equals(newValue))
            this.feetMenuItem.setSelected(true);
        else if (UNIT_METRIC.equals(newValue))
            this.metersMenuItem.setSelected(true);

        // The TracksPanel doesn't listen to the WorldWindow. Handle it as a special case.
        getTracksPanel().setElevationUnit(newValue);

        // Use the WorldWindow as a vehicle for communicating the value change.
        // Components that need to know the current unit will listen on this WorldWindow
        // for a change with the name ELEVATION_UNIT.
        this.wwd.setValue(SARKey.ELEVATION_UNIT, newValue);
        this.wwd.firePropertyChange(SARKey.ELEVATION_UNIT, oldValue, newValue);
        this.wwd.redraw();
    }

    public String getAngleFormat()
    {
        return getUserPreferences().getStringValue(SARKey.ANGLE_FORMAT);
    }

    public void setAngleFormat(String format)
    {
        getUserPreferences().setValue(SARKey.ANGLE_FORMAT, format);
        this.saveUserPreferences();
        this.onUserPreferencesChanged();
    }

    private void angleFormatChanged(String oldValue, String newValue)
    {
        // Update angle format menu selection.
        if (Angle.ANGLE_FORMAT_DD.equals(newValue))
            this.angleDDMenuItem.setSelected(true);
        else if (Angle.ANGLE_FORMAT_DMS.equals(newValue))
            this.angleDMSMenuItem.setSelected(true);

        // The TracksPanel doesn't listen to the WorldWindow. Handle it as a special case.
        getTracksPanel().setAngleFormat(newValue);

        // Use the WorldWindow as a vehicle for communicating the value change.
        // Components that need to know the current angle format will listen on this WorldWindow
        // for a change with the name ANGLE_FORMAT.
        this.wwd.setValue(SARKey.ANGLE_FORMAT, newValue);
        this.wwd.firePropertyChange(SARKey.ANGLE_FORMAT, oldValue, newValue);
        this.wwd.redraw();
    }

    public SARTrack getCurrentTrack()
    {
        return getTracksPanel().getCurrentTrack();
    }

    public TracksPanel getTracksPanel()
    {
        return controlPanel.getTracksPanel();
    }

    public AnalysisPanel getAnalysisPanel()
    {
        return controlPanel.getAnalysisPanel();
    }

    private void newTrack(String name)
    {
        Object inputValue = JOptionPane.showInputDialog(this, "Enter a new track name", "Add New Track",
            JOptionPane.QUESTION_MESSAGE, null, null, name);
        if (inputValue == null)
            return;

        name = inputValue.toString();

        SARTrack st = new SARTrack(name);
        trackController.addTrack(st);

        st.markDirty();
    }

    private void newTrackFromFile()
    {
        File[] files = showOpenDialog("Open a track file");
        if (files == null || files.length == 0)
            return;

        for (File file : files)
        {
            this.newTrackFromFile(file.getPath(), null);
        }
    }

    private void newTrackFromFile(String filePath, String name)
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        SARTrack track = null;
        try
        {
            track = SARTrack.fromFile(filePath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (track == null)
            return;

        if (name != null)
            track.setName(name);

        trackController.addTrack(track);

        try
        {
            // Load annotations if any
            File annotationFile = getAnnotationsPath(filePath);
            if (annotationFile != null && annotationFile.exists())
                this.annotationSupport.readAnnotations(annotationFile.getPath(), track);
            // Restore track state
            this.restoreTrackState(new File(filePath));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        track.clearDirtyBit();
    }

    private File[] showOpenDialog(String title)
    {
        if (this.openFileChooser == null)
        {
            TrackReaderFilter[] filterArray = new TrackReaderFilter[]
                {
                    new TrackReaderFilter(new CSVTrackReader()),
                    new TrackReaderFilter(new GPXTrackReader()),
                    new TrackReaderFilter(new NMEATrackReader())
                };

            this.openFileChooser = new JFileChooser();
            this.openFileChooser.setMultiSelectionEnabled(true);
            this.openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            CompoundFilter filterAll = new CompoundFilter(filterArray, "Accepted Files");
            this.openFileChooser.addChoosableFileFilter(filterAll);

            for (TrackReaderFilter filter : filterArray)
            {
                this.openFileChooser.addChoosableFileFilter(filter);
            }

            this.openFileChooser.setFileFilter(filterAll);
        }

        String s = getUserPreferences().getStringValue(SARKey.CURRENT_BROWSE_DIRECTORY);
        if (s != null)
            this.openFileChooser.setCurrentDirectory(new File(s));

        this.openFileChooser.setDialogTitle(title != null ? title : "Open Track");

        int retVal = this.openFileChooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION)
            return null;

        File file = this.openFileChooser.getCurrentDirectory();
        getUserPreferences().setValue(SARKey.CURRENT_BROWSE_DIRECTORY, file.getPath());
        this.onUserPreferencesChanged();
        this.saveUserPreferences();

        return this.openFileChooser.getSelectedFiles();
    }

    private void newTrackFromURL(String urlString, String name)
    {
        if (urlString == null)
        {
            Object input = JOptionPane.showInputDialog(SAR2.this, "Enter a track URL", "Add New Track",
                JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (input != null)
                urlString = input.toString();
        }

        if (urlString == null)
            return;

        URL url = makeURL(urlString);
        if (url == null)
            return;

        SARTrack track = null;
        try
        {
            ByteBuffer bb = WWIO.readURLContentToBuffer(url);
            File file = WWIO.saveBufferToTempFile(bb, ".xml");
            track = SARTrack.fromFile(file.getPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (track == null)
            return;

        if (name == null)
            name = urlString;

        track.setFile(null);
        track.setName(name);
        trackController.addTrack(track);
        track.markDirty();
    }

    protected void newTrackFromPath(String path, String name)
    {
        SARTrack track = null;
        InputStream stream = null;

        try
        {
            stream = WWIO.openFileOrResourceStream(path, this.getClass());
            ByteBuffer buffer = WWIO.readStreamToBuffer(stream);
            File file = WWIO.saveBufferToTempFile(buffer, "." + WWIO.getSuffix(path));
            track = SARTrack.fromFile(file.getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            WWIO.closeStream(stream, path);
        }

        if (track == null)
            return;

        if (name == null)
            name = WWIO.getFilename(path);

        track.setFile(null);
        track.setName(name);
        trackController.addTrack(track);
        track.markDirty();
    }

    private static URL makeURL(String urlString)
    {
        URL url = null;
        try
        {
            if (urlString != null)
                url = new URL(urlString);
        }
        catch (MalformedURLException e)
        {
            url = null;
        }
        return url;
    }

    private int removeTrack(SARTrack track, boolean forceSavePrompt)
    {
        if (track == null)
            return OK;

        int status = OK;
        if (track.isDirty() || forceSavePrompt)
        {

            int option = SaveTrackDialog.showSaveChangesPrompt(this, null, null, track);
            // Show a save track dialog that won't prompt the user to choose a location unless it's necessary.
            if (option == JOptionPane.YES_OPTION)
                status = saveTrack(track, false);
            else if (option == JOptionPane.CANCEL_OPTION)
                status = CANCELLED;
        }

        if (status != OK)
            return status;

        try
        {
            track.firePropertyChange(TrackController.TRACK_REMOVE, null, track);
            this.trackController.refreshCurrentTrack();
            this.annotationSupport.removeAnnotationsForTrack(track);
            this.wwd.redraw();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ERROR;
        }

        return OK;
    }

    private int removeAllTracks(boolean forceSavePrompt)
    {
        int status = OK;
        for (SARTrack track : getTracksPanel().getAllTracks())
        {
            status |= removeTrack(track, forceSavePrompt);
            if ((status & CANCELLED) != 0)
                return status;
        }

        return status;
    }

    private int saveTrack(SARTrack track, boolean forceSavePrompt)
    {
        return saveTrack(
            track,
            null,  // Use track's file, or prompt user.
            0,     // Use track's format.
            true,  // Save annotations
            forceSavePrompt);
    }

    private int saveTrack(SARTrack track, File file, int format, boolean saveAnnotations, boolean forceSavePrompt)
    {
        if (track == null)
        {
            String message = Logging.getMessage("nullValue.TrackIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (file == null)
            file = track.getFile();
        if (format == 0)
            format = track.getFormat();

        // Show the "Save As..." dialog if either:
        // * The current track has no source file.
        // * The caller has specified that the user should prompted to select a file,
        if (file == null || forceSavePrompt)
        {
            int result = this.showSaveDialog(track, file, format);
            if (result == SaveTrackDialog.CANCEL_OPTION)
                return CANCELLED;
            else if (result == SaveTrackDialog.ERROR_OPTION)
                return ERROR;

            file = this.saveTrackDialog.getSelectedFile();
            format = this.saveTrackDialog.getFileFormat();
            saveAnnotations = this.saveTrackDialog.isSaveAnnotations();
        }

        try
        {
            // Get the file's last modified time,
            // or zero if the file does not exist.
            long time = file.exists() ? file.lastModified() : 0;

            SARTrack.toFile(track, file.getPath(), format);
            if (saveAnnotations)
            {
                File annotationFile = getAnnotationsPath(file.getPath());
                this.annotationSupport.writeAnnotations(annotationFile.getPath(), track);
            }
            // Save current track state
            this.saveTrackState(file);

            // If the track was saved sucessfully (it exists and
            // is newer than is was before the save operation),
            // then adopt the properties of the new
            // file and format, and clear the track's dirty bit.
            if (file.exists() && time <= file.lastModified())
            {
                track.setFile(file);
                track.setFormat(format);
                track.setName(file.getName());
                track.clearDirtyBit();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ERROR;
        }

        return OK;
    }

    private void saveTrackState(File trackFile)
    {
        String state = this.getRestorableState();
        File stateFile = getTrackStateFile(trackFile);
        WWIO.writeTextFile(state, stateFile);
    }

    private void restoreTrackState(File trackFile)
    {
        File stateFile = getTrackStateFile(trackFile);
        if (!stateFile.exists())
            return;

        String state = WWIO.readTextFile(stateFile);
        if (state != null)
            this.restoreState(state);
    }

    private File getTrackStateFile(File trackFile)
    {
        String path = trackFile.getAbsolutePath();
        return new File(path + ".sts");  // SAR Track State
    }

    private int showSaveDialog(SARTrack track, File file, int format)
    {
        if (this.saveTrackDialog == null)
            this.saveTrackDialog = new SaveTrackDialog();

        this.saveTrackDialog.setDialogTitle(track);

        if (file != null)
            this.saveTrackDialog.setSelectedFile(file);
        else
            this.saveTrackDialog.setSelectedFile(track);

        if (format != 0)
            this.saveTrackDialog.setFileFormat(format);
        else
            this.saveTrackDialog.setFileFormat(track);

        return this.saveTrackDialog.showSaveDialog(this);
    }

    private void bulkDownload()
    {
        if (this.bulkDownloadFrame == null)
        {
            this.bulkDownloadFrame = new BulkDownloadFrame(this.wwd);
            this.bulkDownloadFrame.setLocation(new Point(this.getLocationOnScreen().x + 100,
                this.getLocationOnScreen().y + 100));
        }
        this.bulkDownloadFrame.setVisible(true);
    }

    private SARAnnotation getCurrentAnnotation()
    {
        return this.annotationSupport.getCurrent();
    }

    private void newAnnotation()
    {
        newAnnotation(null, getCurrentTrack());
    }

    private void newAnnotation(String text, SARTrack track)
    {
        this.annotationSupport.addNew(text, track);
        this.wwd.redraw();
    }

    private void removeAnnotation(SARAnnotation annotation)
    {
        if (annotation != null)
        {
            this.annotationSupport.remove(annotation);
        }
        this.wwd.redraw();
    }

    private void setAnnotationsEnabled(boolean show)
    {
        this.annotationSupport.setEnabled(show);
        this.wwd.redraw();
    }

    private void extendTrack(String extensionMode)
    {
        SARTrack track = this.trackController.getCurrentTrack();
        if (track == null)
            return;

        if (this.trackController.isExtending())
            track.firePropertyChange(TrackController.END_TRACK_POINT_ENTRY, null, extensionMode);
        else
            track.firePropertyChange(TrackController.BEGIN_TRACK_POINT_ENTRY, null, extensionMode);
    }

    public static AVList getUserPreferences()
    {
        return userPreferences;
    }

    public void showHelp()
    {
        try
        {
            BrowserOpener.browse(new URL(ONLINE_HELP_URL));
        }
        catch (Exception e1)
        {
            System.err.println("Unable to open Help window");
            e1.printStackTrace();
        }
    }

    public void showAbout()
    {
        SARAboutDialog dialog = new SARAboutDialog();
        dialog.showDialog(this);
    }

    public boolean exit()
    {
        int status = removeAllTracks(false);
        if ((status & CANCELLED) != 0)
            return false;

        dispose();
        System.exit(0);
        return true;
    }

    private File getAnnotationsPath(String trackFilePath)
    {
        return (trackFilePath != null) ? new File(trackFilePath + ".sta") : null;
    }

    private void initComponents()
    {
        //======== this ========
        setTitle("World Wind Search and Rescue");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent event)
            {
                exit();
            }

            public void windowClosed(WindowEvent event)
            {
                exit();
            }
        });
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        controlPanel = new ControlPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
        contentPane.add(controlPanel, BorderLayout.WEST);

        //---- WWPanel ----
        wwPanel = new WWPanel();
        wwPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        wwPanel.setPreferredSize(new Dimension(1000, 800));
        contentPane.add(wwPanel, BorderLayout.CENTER);

        //======== Actions ========
        Action screenShotAction = new SARScreenShotAction(wwPanel.getWwd(), this.getIcon("24x24-snapshot.gif"));

        //======== MenuBar ========
        JMenuBar menuBar = new JMenuBar();
        {
            JMenu fileMenu = new JMenu();
            //======== "File" ========
            {
                fileMenu.setText("File");
                fileMenu.setMnemonic('F');

                //---- "New Track" ----
                JMenuItem newTrack = new JMenuItem();
                newTrack.setText("New Track...");
                newTrack.setMnemonic('N');
                newTrack.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                newTrack.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newTrack(null);
                    }
                });
                fileMenu.add(newTrack);

                //---- "Open Track File" ----
                JMenuItem openTrackFile = new JMenuItem();
                openTrackFile.setText("Open Track File...");
                openTrackFile.setMnemonic('O');
                openTrackFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                openTrackFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newTrackFromFile();
                    }
                });
                fileMenu.add(openTrackFile);

                //---- "Open Track URL..." ----
                JMenuItem openTrackURL = new JMenuItem();
                openTrackURL.setText("Open Track URL...");
                openTrackURL.setMnemonic('U');
                openTrackURL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                openTrackURL.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newTrackFromURL(null, null);
                    }
                });
                fileMenu.add(openTrackURL);

                //---- "Close Track" ----
                JMenuItem removeTrack = new JMenuItem();
                removeTrack.setText("Close Track");
                removeTrack.setMnemonic('C');
                removeTrack.setAccelerator(KeyStroke.getKeyStroke(
                    Configuration.isMacOS() ? KeyEvent.VK_W : KeyEvent.VK_F4,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                removeTrack.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        removeTrack(getCurrentTrack(), false);
                    }
                });
                fileMenu.add(removeTrack);

                //--------
                fileMenu.addSeparator();

                //---- "Save Track" ----
                JMenuItem saveTrack = new JMenuItem();
                saveTrack.setText("Save Track");
                saveTrack.setMnemonic('S');
                saveTrack.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                saveTrack.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        // Show a save track dialog that won't prompt the user to choose a location unless it's
                        // necessary.
                        saveTrack(getCurrentTrack(), false);
                    }
                });
                fileMenu.add(saveTrack);

                //---- "Save Track As..." ----
                JMenuItem saveTrackAs = new JMenuItem();
                saveTrackAs.setText("Save Track As...");
                saveTrackAs.setMnemonic('A');
                saveTrackAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + java.awt.Event.SHIFT_MASK));
                saveTrackAs.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        // Show a save track dialog that will always prompt the user to choose a location.
                        saveTrack(getCurrentTrack(), true);
                    }
                });
                fileMenu.add(saveTrackAs);

                //---- "Screen Shot" ----
                JMenuItem screenShot = new JMenuItem(screenShotAction);
                screenShot.setIcon(null); // Make sure the menu items displays only text.
                screenShot.setMnemonic('T');
                screenShot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                fileMenu.add(screenShot);

                //--------
                fileMenu.addSeparator();

                JMenuItem bulkDownload = new JMenuItem();
                bulkDownload.setText("Bulk download...");
                bulkDownload.setMnemonic('B');
                bulkDownload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                bulkDownload.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        // Bring the bulk download frame up
                        bulkDownload();
                    }
                });
                fileMenu.add(bulkDownload);

                //--------
                fileMenu.addSeparator();

                JMenuItem openTrackItem = new JMenuItem();
                openTrackItem.setText("PipeTrackTest.gpx");
                openTrackItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newTrackFromPath("gov/nasa/worldwindx/applications/sar/data/PipeTrackTest.gpx", null);
                    }
                });
                fileMenu.add(openTrackItem);

                openTrackItem = new JMenuItem();
                openTrackItem.setText("PipeTracks2.gpx");
                openTrackItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newTrackFromPath("gov/nasa/worldwindx/applications/sar/data/PipeTracks2.gpx", null);
                    }
                });
                fileMenu.add(openTrackItem);

                openTrackItem = new JMenuItem();
                openTrackItem.setText("PipeTracks3.gpx");
                openTrackItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newTrackFromPath("gov/nasa/worldwindx/applications/sar/data/PipeTracks3.gpx", null);
                    }
                });
                fileMenu.add(openTrackItem);

                if (!Configuration.isMacOS())
                {
                    //--------
                    fileMenu.addSeparator();

                    JMenuItem exit = new JMenuItem();
                    exit.setText("Exit");
                    exit.setMnemonic('X');
                    exit.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
                    exit.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent event)
                        {
                            exit();
                        }
                    });
                    fileMenu.add(exit);
                }
                else
                {
                    try
                    {
                        OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("exit", (Class[]) null));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            menuBar.add(fileMenu);

            //======== "View" ========
            JMenu unitsMenu = new JMenu();
            {
                unitsMenu.setText("Units");
                unitsMenu.setMnemonic('U');

                //---- "Meters" ----
                metersMenuItem = new JCheckBoxMenuItem();
                metersMenuItem.setText("Meters");
                metersMenuItem.setMnemonic('M');
                metersMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                metersMenuItem.setActionCommand(UNIT_METRIC);
                metersMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        setElevationUnit(e.getActionCommand());
                    }
                });
                unitsMenu.add(metersMenuItem);

                //---- "Feet" ----
                feetMenuItem = new JCheckBoxMenuItem();
                feetMenuItem.setText("Feet");
                feetMenuItem.setMnemonic('F');
                feetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                    java.awt.event.InputEvent.ALT_MASK));
                feetMenuItem.setActionCommand(UNIT_IMPERIAL);
                feetMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        setElevationUnit(e.getActionCommand());
                    }
                });
                unitsMenu.add(feetMenuItem);

                ButtonGroup unitGroup = new ButtonGroup();
                unitGroup.add(metersMenuItem);
                unitGroup.add(feetMenuItem);

                unitsMenu.addSeparator();

                //---- "Angle DD" ----
                angleDDMenuItem = new JCheckBoxMenuItem();
                angleDDMenuItem.setText("Angles DD");
                angleDDMenuItem.setMnemonic('D');
                angleDDMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                angleDDMenuItem.setActionCommand(Angle.ANGLE_FORMAT_DD);
                angleDDMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        setAngleFormat(e.getActionCommand());
                    }
                });
                unitsMenu.add(angleDDMenuItem);

                //---- "Angle DMS" ----
                angleDMSMenuItem = new JCheckBoxMenuItem();
                angleDMSMenuItem.setText("Angles DMS");
                angleDMSMenuItem.setMnemonic('S');
                angleDMSMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                    java.awt.event.InputEvent.ALT_MASK));
                angleDMSMenuItem.setActionCommand(Angle.ANGLE_FORMAT_DMS);
                angleDMSMenuItem.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        setAngleFormat(e.getActionCommand());
                    }
                });
                unitsMenu.add(angleDMSMenuItem);

                ButtonGroup formatGroup = new ButtonGroup();
                formatGroup.add(angleDDMenuItem);
                formatGroup.add(angleDMSMenuItem);
            }
            menuBar.add(unitsMenu);

            //======== "Annotation" ========
            JMenu annotationMenu = new JMenu();
            {
                annotationMenu.setText("Annotation");
                annotationMenu.setMnemonic('A');

                //---- "New Annotation..." ----
                JMenuItem newAnnotation = new JMenuItem();
                newAnnotation.setText("New Annotation...");
                newAnnotation.setMnemonic('N');
                newAnnotation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                newAnnotation.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        newAnnotation();
                    }
                });
                annotationMenu.add(newAnnotation);

                //---- "Remove Annotation" ----
                JMenuItem removeAnnotation = new JMenuItem();
                removeAnnotation.setText("Remove Annotation");
                removeAnnotation.setMnemonic('R');
                removeAnnotation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + java.awt.Event.SHIFT_MASK));
                removeAnnotation.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        removeAnnotation(getCurrentAnnotation());
                    }
                });
                annotationMenu.add(removeAnnotation);

                //---- "Show Annotations" ----
                JCheckBoxMenuItem showAnnotations = new JCheckBoxMenuItem();
                showAnnotations.setText("Show Annotations");
                showAnnotations.setMnemonic('S');
                showAnnotations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    java.awt.event.InputEvent.ALT_MASK));
                showAnnotations.setSelected(true);
                showAnnotations.addItemListener(new ItemListener()
                {
                    public void itemStateChanged(ItemEvent e)
                    {
                        setAnnotationsEnabled(e.getStateChange() == ItemEvent.SELECTED);
                    }
                });
                annotationMenu.add(showAnnotations);
            }
            menuBar.add(annotationMenu);

            //======== "View Menu" ========
            viewLayerMenu = new ViewMenu();
            {
                viewLayerMenu.setMnemonic('V');
            }
            menuBar.add(viewLayerMenu);

            //======== "Layers" ========
            layerMenu = new LayerMenu();
            {
                layerMenu.setMnemonic('L');
            }
            menuBar.add(layerMenu);

            //======== "Help" ========
            JMenu helpMenu = new JMenu();
            {
                helpMenu.setText("Help");
                helpMenu.setMnemonic('H');

                //---- "Search and Rescue Help" ----
                JMenuItem sarHelp = new JMenuItem();
                sarHelp.setText("Search and Rescue Help");
                sarHelp.setMnemonic('H');
                if (!Configuration.isMacOS())
                    sarHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
                else
                    sarHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HELP,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                sarHelp.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        showHelp();
                    }
                });
                helpMenu.add(sarHelp);

                //---- "About [World Wind Search and Rescue Prototype]" ----
                if (!Configuration.isMacOS())
                {
                    JMenuItem about = new JMenuItem();
                    about.setText("About");
                    about.setMnemonic('A');
                    about.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent event)
                        {
                            showAbout();
                        }
                    });
                    helpMenu.add(about);
                }
                else
                {
                    try
                    {
                        OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAbout", (Class[]) null));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            menuBar.add(helpMenu);
        }
        setJMenuBar(menuBar);

        //======= Tool bar ======
        JToolBar toolBar = new JToolBar();
        {
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right
            JButton button;

            // == Open track from file ==
            button = makeToolBarButton("24x24-open.gif", "Open track from file", "Open");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    newTrackFromFile();
                }
            });
            toolBar.add(button);

            // == New Track ==
            button = makeToolBarButton("24x24-new.gif", "New track", "New");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    newTrack(null);
                }
            });
            toolBar.add(button);

            // == Save Track ==
            button = makeToolBarButton("24x24-save.gif", "Save track", "Save");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Show a save track dialog that won't prompt the user to choose a location unless it's necessary.
                    saveTrack(getCurrentTrack(), false);
                }
            });
            toolBar.add(button);

            // == Screen Shot ==
            button = new JButton(screenShotAction);
            button.setText(null); // Make sure the toolbar button displays only an icon.
            toolBar.add(button);

            toolBar.addSeparator();

            // == View Mode Examine  ==
            this.viewExamineButton = makeToolBarButton("24x24-view-examine.gif", "View examine", "Examine");
            this.viewExamineButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    wwd.firePropertyChange(TrackViewPanel.VIEW_MODE_CHANGE, null, TrackViewPanel.VIEW_MODE_EXAMINE);
                }
            });
            toolBar.add(this.viewExamineButton);

            // == View Mode Fly-it  ==
            this.viewFollowButton = makeToolBarButton("24x24-view-follow.gif", "View fly-it", "Fly-it");
            this.viewFollowButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    wwd.firePropertyChange(TrackViewPanel.VIEW_MODE_CHANGE, null, TrackViewPanel.VIEW_MODE_FOLLOW);
                }
            });

            toolBar.add(this.viewFollowButton);
            // == View Mode Free  ==
            this.viewFreeButton = makeToolBarButton("24x24-view-free.gif", "View free", "Free");
            this.viewFreeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    wwd.firePropertyChange(TrackViewPanel.VIEW_MODE_CHANGE, null, TrackViewPanel.VIEW_MODE_FREE);
                }
            });
            toolBar.add(this.viewFreeButton);

            toolBar.addSeparator();

            // == Visual Attributes ====

            // == Track Information ==
            this.showTrackInfoButton = makeToolBarButton("24x24-segment-info.gif",
                "Display track information in the 3D view", "Display track information");
            this.showTrackInfoButton.setBorderPainted(false);
            this.showTrackInfoButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Toggle between enabling and disabling the SHOW_TRACK_INFORMATION state.
                    showTrackInfoButton.setBorderPainted(!showTrackInfoButton.isBorderPainted());
                    String state = showTrackInfoButton.isBorderPainted() ? TrackViewPanel.CURRENT_SEGMENT : null;
                    wwd.firePropertyChange(TrackViewPanel.SHOW_TRACK_INFORMATION, null, state);
                }
            });
            toolBar.add(this.showTrackInfoButton);

            toolBar.addSeparator();

            // == Track extension tools ====

            // == Extension plane  ==
            this.extendTrackPlaneButton = makeToolBarButton("24x24-extend-plane.gif", "Extend track using the 3D plane",
                "Extension plane");
            this.extendTrackPlaneButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    extendTrack(TrackController.EXTENSION_PLANE);
                }
            });
            toolBar.add(this.extendTrackPlaneButton);

            // == Extension in air with cursor  ==
            this.extendTrackCursorAirButton = makeToolBarButton("24x24-extend-air.gif",
                "Extend track in the air with the mouse cursor and the Alt key", "Extension air");
            this.extendTrackCursorAirButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    extendTrack(TrackController.EXTENSION_CURSOR_AIR);
                }
            });
            toolBar.add(this.extendTrackCursorAirButton);

            // == Extension on ground with cursor ==
            this.extendTrackCursorGroundButton = makeToolBarButton("24x24-extend-ground.gif",
                "Extend track on the ground with the mouse cursor and the Alt key", "Extension ground");
            this.extendTrackCursorGroundButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    extendTrack(TrackController.EXTENSION_CURSOR_GROUND);
                }
            });
            toolBar.add(this.extendTrackCursorGroundButton);

            // == Remove last point  ==
            this.removeLastPointButton = makeToolBarButton("24x24-remove-point.gif", "Remove last track point",
                "Remove last");
            this.removeLastPointButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (getCurrentTrack() != null)
                        getCurrentTrack().firePropertyChange(TrackController.REMOVE_LAST_POINT, null, null);
                }
            });
            toolBar.add(this.removeLastPointButton);

            // == Next point  ==
            this.nextPointButton = makeToolBarButton("24x24-forward.gif", "Move to next point", "Next point");
            this.nextPointButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (getCurrentTrack() != null)
                        getCurrentTrack().firePropertyChange(TrackController.MOVE_TO_NEXT_POINT, null, null);
                }
            });
            toolBar.add(this.nextPointButton);

            toolBar.addSeparator();

            // == Terrain Profile ==
            button = makeToolBarButton("24x24-profile.gif", "Terrain profile", "Profile");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    wwd.firePropertyChange(TerrainProfilePanel.TERRAIN_PROFILE_OPEN, null, null);
                }
            });
            toolBar.add(button);

            // == Cloud ceiling ==
            button = makeToolBarButton("24x24-clouds.gif", "Cloud ceiling", "Clouds");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    wwd.firePropertyChange(CloudCeilingPanel.CLOUD_CEILING_OPEN, null, null);
                }
            });
            toolBar.add(button);

            toolBar.addSeparator();

            // == Help ==
            button = makeToolBarButton("24x24-help.gif", "Help", "Help");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showHelp();
                }
            });
            toolBar.add(button);

            // == About ==
            button = makeToolBarButton("24x24-about.gif", "About the Search And Rescue application", "About");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showAbout();
                }
            });
            toolBar.add(button);
            // Set toolbar state
            this.setToolbarDefaultState();
        }
        JPanel toolBarPanel = new JPanel(new BorderLayout(0, 0)); // hgap, vgap
        toolBarPanel.add(toolBar, BorderLayout.NORTH);
        toolBarPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);
        this.getContentPane().add(toolBarPanel, BorderLayout.NORTH);

        pack();
        centerWindowInDesktop(this);
    }

    private JButton makeToolBarButton(String imageName, String toolTipText, String altText)
    {
        JButton button = new JButton();
        button.setToolTipText(toolTipText);
        ImageIcon icon = getIcon(imageName);
        if (icon != null)
            button.setIcon(icon);
        else
            button.setText(altText);

        return button;
    }

    private SARTrack toolbarTrack;

    private void updateToolBar(PropertyChangeEvent event)
    {
//        if (event != null)
//            System.out.println("SAR2.updateToolBar for event: " + event.getPropertyName() + ", new value = " + (event.getNewValue() == null ? "" : "'" + event.getNewValue() + "'"));

        if (event == null)
            return;

        // Set state according to event if not null
        if (event.getPropertyName().equals(TrackController.TRACK_CURRENT))
        {
            SARTrack newTrack = getCurrentTrack();
            if (this.toolbarTrack == null || newTrack == null)
            {
                // Reset defaul toolbar state 
                setToolbarDefaultState();
            }
            this.toolbarTrack = newTrack;
        }
        else if (event.getPropertyName().equals(TrackViewPanel.VIEW_MODE_CHANGE))
        {
            Object viewMode = event.getNewValue();
            this.viewExamineButton.setBorderPainted(viewMode.equals(TrackViewPanel.VIEW_MODE_EXAMINE));
            this.viewFollowButton.setBorderPainted(viewMode.equals(TrackViewPanel.VIEW_MODE_FOLLOW));
            this.viewFreeButton.setBorderPainted(viewMode.equals(TrackViewPanel.VIEW_MODE_FREE));
        }
        else if (event.getPropertyName().equals(TrackController.BEGIN_TRACK_POINT_ENTRY))
        {
            Object newMode = event.getNewValue();
            this.extendTrackPlaneButton.setEnabled(newMode.equals(TrackController.EXTENSION_PLANE));
            this.extendTrackCursorGroundButton.setEnabled(newMode.equals(TrackController.EXTENSION_CURSOR_GROUND));
            this.extendTrackCursorAirButton.setEnabled(newMode.equals(TrackController.EXTENSION_CURSOR_AIR));

            this.extendTrackPlaneButton.setBorderPainted(this.extendTrackPlaneButton.isEnabled());
            this.extendTrackCursorGroundButton.setBorderPainted(this.extendTrackCursorGroundButton.isEnabled());
            this.extendTrackCursorAirButton.setBorderPainted(this.extendTrackCursorAirButton.isEnabled());

            this.nextPointButton.setEnabled(this.extendTrackPlaneButton.isEnabled());
            this.removeLastPointButton.setEnabled(true);
        }
        else if (event.getPropertyName().equals(TrackController.END_TRACK_POINT_ENTRY))
        {
            this.extendTrackPlaneButton.setEnabled(true);
            this.extendTrackCursorGroundButton.setEnabled(true);
            this.extendTrackCursorAirButton.setEnabled(true);

            this.extendTrackPlaneButton.setBorderPainted(false);
            this.extendTrackCursorGroundButton.setBorderPainted(false);
            this.extendTrackCursorAirButton.setBorderPainted(false);

            this.nextPointButton.setEnabled(false);
            this.removeLastPointButton.setEnabled(false);
        }
    }

    private void setToolbarDefaultState()
    {
        SARTrack currentTrack = getCurrentTrack();
        String viewMode = this.controlPanel.getAnalysisPanel().getViewMode();
        // Default state
        this.viewExamineButton.setEnabled(currentTrack != null);
        this.viewFollowButton.setEnabled(currentTrack != null);
        this.viewFreeButton.setEnabled(currentTrack != null);
        this.extendTrackPlaneButton.setEnabled(currentTrack != null);
        this.extendTrackCursorGroundButton.setEnabled(currentTrack != null);
        this.extendTrackCursorAirButton.setEnabled(currentTrack != null);
        this.nextPointButton.setEnabled(false);
        this.removeLastPointButton.setEnabled(false);
        this.showTrackInfoButton.setEnabled(currentTrack != null);

        this.viewExamineButton.setBorderPainted(viewMode.equals(TrackViewPanel.VIEW_MODE_EXAMINE));
        this.viewFollowButton.setBorderPainted(viewMode.equals(TrackViewPanel.VIEW_MODE_FOLLOW));
        this.viewFreeButton.setBorderPainted(viewMode.equals(TrackViewPanel.VIEW_MODE_FREE));
        this.extendTrackPlaneButton.setBorderPainted(false);
        this.extendTrackCursorGroundButton.setBorderPainted(false);
        this.extendTrackCursorAirButton.setBorderPainted(false);
    }

    private ImageIcon getIcon(String imageName)
    {
        String imagePath = "gov/nasa/worldwindx/applications/sar/images/" + imageName;
        Object o = WWIO.getFileOrResourceAsStream(imagePath, this.getClass());
        if (!(o instanceof InputStream))
            return null;

        try
        {
            BufferedImage icon = ImageIO.read((InputStream) o);
            return new ImageIcon(icon);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static String formatAngle(String format, Angle angle)
    {
        String s;
        if (Angle.ANGLE_FORMAT_DMS.equals(format))
            s = angle.toDMSString();
        else
            s = String.format("%7.4f\u00B0", angle.degrees);
        return s;
    }

    //**************************************************************//
    //********************  User Preferences  **********************//
    //**************************************************************//

    protected void onUserPreferencesChanged()
    {
        this.startOrStopAutoSave(MIN_AUTO_SAVE_INTERVAL);
        this.elevationUnitChanged(null, getUserPreferences().getStringValue(SARKey.ELEVATION_UNIT));
        this.angleFormatChanged(null, getUserPreferences().getStringValue(SARKey.ANGLE_FORMAT));
    }

    protected void initializeUserPreferences()
    {
        UserPreferenceUtils.getDefaultUserPreferences(getUserPreferences());
    }

    protected void loadUserPreferences()
    {
        File file = new File(UserPreferenceUtils.getDefaultUserPreferencesPath());

        // If the preferences file does not exist, then exit and run with the defaults.
        if (!file.exists())
            return;

        Document doc = null;
        try
        {
            doc = WWXML.openDocumentFile(file.getPath(), this.getClass());
        }
        catch (WWRuntimeException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadFrom", file.getPath());
            Logging.logger().severe(message);
        }

        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            return;
        }

        if (doc.getDocumentElement() == null)
        {
            String message = Logging.getMessage("nullValue.DocumentElementIsNull");
            Logging.logger().severe(message);
            return;
        }

        UserPreferenceUtils.getUserPreferences(doc.getDocumentElement(), getUserPreferences());
    }

    protected void saveUserPreferences()
    {
        File file = new File(UserPreferenceUtils.getDefaultUserPreferencesPath());

        // If the parent file does not exist, then attempt to create it. If creating the parent file fails, then abort
        // the save operation.
        if (!file.getParentFile().exists())
        {
            if (!file.getParentFile().mkdirs())
            {
                String message = Logging.getMessage("generic.CannotCreateFile", file.getPath());
                Logging.logger().severe(message);
                return;
            }
        }

        Document doc = UserPreferenceUtils.createUserPreferencesDocument(getUserPreferences());
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            return;
        }

        try
        {
            WWXML.saveDocumentToFile(doc, file.getPath());
        }
        catch (WWRuntimeException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToWriteTo", file.getPath());
            Logging.logger().severe(message);
        }
    }

    //**************************************************************//
    //********************  Track Auto Save  ***********************//
    //**************************************************************//

    protected void onAutoSave()
    {
        this.autoSaveAllTracks();
    }

    protected void startOrStopAutoSave(long minInterval)
    {
        this.autoSaveTimer.stop();

        if (UserPreferenceUtils.getBooleanValue(getUserPreferences(), SARKey.AUTO_SAVE_TRACKS))
        {
            long delayMillis = AVListImpl.getLongValue(getUserPreferences(), SARKey.AUTO_SAVE_TRACKS_INTERVAL,
                minInterval);
            if (delayMillis < minInterval)
                delayMillis = minInterval;

            this.autoSaveTimer.setDelay((int) delayMillis);
            this.autoSaveTimer.setInitialDelay((int) delayMillis);
            this.autoSaveTimer.start();
        }
    }

    protected void autoSaveAllTracks()
    {
        if (!UserPreferenceUtils.getBooleanValue(getUserPreferences(), SARKey.AUTO_SAVE_TRACKS))
            return;

        for (SARTrack track : this.getTracksPanel().getAllTracks())
        {
            this.autoSaveTrack(track);
        }
    }

    protected void autoSaveTrack(SARTrack track)
    {
        if (!UserPreferenceUtils.getBooleanValue(getUserPreferences(), SARKey.AUTO_SAVE_TRACKS))
            return;

        // Do not save the track if it's already clean. Doing so will cause an infinite recursion between saving the
        // track and changing the dirty bit to clean.
        if (!track.isDirty())
            return;

        // Without knowledge of a file and a format for the track, we'd have to prompt the user to get that information.
        // However, we should not be prompting the user for save information whenever a track is marked dirty. Therefore
        // the track auto save functionality is activated only when the track file and format are already known.
        if (track.getFile() == null || track.getFormat() == 0)
            return;

        // Fire a TRACK_SAVE event which will be captured and handled by SAR2.
        this.saveTrack(track, false);
    }

    // *** Restorable interface ***

    public String getRestorableState()
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
    }

    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        this.doRestoreState(rs, null);
    }

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Add state values
        if (this.controlPanel.getTracksPanel().getCurrentTrack() != null)
        {
            SARTrack track = this.controlPanel.getTracksPanel().getCurrentTrack();
            this.controlPanel.getTracksPanel().getTrackPanel(track).doGetRestorableState(rs,
                rs.addStateObject(context, "trackPanel"));
        }

        if (this.controlPanel.getAnalysisPanel() != null)
            this.controlPanel.getAnalysisPanel().doGetRestorableState(rs, rs.addStateObject(context, "analysisPanel"));
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Retrieve state values
        RestorableSupport.StateObject trackPanelState = rs.getStateObject(context, "trackPanel");
        if (trackPanelState != null && this.controlPanel.getTracksPanel().getCurrentTrack() != null)
        {
            SARTrack track = this.controlPanel.getTracksPanel().getCurrentTrack();
            this.controlPanel.getTracksPanel().getTrackPanel(track).doRestoreState(rs, trackPanelState);
        }

        RestorableSupport.StateObject analysisPanelState = rs.getStateObject(context, "analysisPanel");
        if (analysisPanelState != null && this.controlPanel.getAnalysisPanel() != null)
            this.controlPanel.getAnalysisPanel().doRestoreState(rs, analysisPanelState);
    }
}
