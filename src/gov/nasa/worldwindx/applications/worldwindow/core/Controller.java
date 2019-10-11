/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.view.orbit.*;
import gov.nasa.worldwindx.applications.worldwindow.core.layermanager.LayerManager;
import gov.nasa.worldwindx.applications.worldwindow.features.NetworkActivitySignal;
import gov.nasa.worldwindx.applications.worldwindow.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: Controller.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Controller
{
    static
    {
        // The following is required to use Swing menus with the heavyweight canvas used by WorldWind.
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    private String appConfigurationLocation;
    private Registry registry = new Registry();
    private String appTitle;
    private JFileChooser fileChooser;
    private Dimension appSize;
    private WWOUnitsFormat unitsFormat;

    public void start(String appConfigurationLocation, Dimension appSize)
        throws Exception
    {
        this.appTitle = Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME);
        this.appSize = appSize;

        this.unitsFormat = new WWOUnitsFormat();
        this.unitsFormat.setShowUTM(true);
        this.unitsFormat.setShowWGS84(true);

        this.appConfigurationLocation = appConfigurationLocation;
        final AppConfiguration appConfig = new AppConfiguration();
        appConfig.initialize(this);

        appConfig.configure(this.appConfigurationLocation);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                redraw();
            }
        });
    }

    public String getAppTitle()
    {
        return appTitle;
    }

    public Registry getRegistry()
    {
        return registry;
    }

    public String getAppConfigurationLocation()
    {
        return this.appConfigurationLocation;
    }

    public Dimension getAppSize()
    {
        return appSize;
    }

    public String getVersion()
    {
        return Version.getVersion();
    }

    public WorldWindow getWWd()
    {
        return getWWPanel().getWWd();
    }

    public WWPanel getWWPanel()
    {
        return (WWPanel) getRegisteredObject(Constants.WW_PANEL);
    }

    public AppFrame getAppFrame()
    {
        return (AppFrame) getRegisteredObject(Constants.APP_FRAME);
    }

    public Frame getFrame()
    {
        AppFrame appFrame = (AppFrame) getRegisteredObject(Constants.APP_FRAME);
        if (appFrame != null)
            return appFrame.getFrame();

        return Util.findParentFrame((Container) getRegisteredObject(Constants.APP_PANEL));
    }

    public StatusPanel getStatusPanel()
    {
        return (StatusPanel) getRegisteredObject(Constants.STATUS_PANEL);
    }

    public AppPanel getAppPanel()
    {
        return (AppPanel) getRegisteredObject(Constants.APP_PANEL);
    }

    public ToolBar getToolBar()
    {
        return (ToolBar) getRegisteredObject(Constants.TOOL_BAR);
    }

    public MenuBar getMenuBar()
    {
        return (MenuBar) getRegisteredObject(Constants.MENU_BAR);
    }

    public LayerManager getLayerManager()
    {
        return (LayerManager) getRegisteredObject(Constants.FEATURE_LAYER_MANAGER);
    }

    public JFileChooser getFileChooser()
    {
        if (this.fileChooser == null)
            this.fileChooser = new JFileChooser();

        return this.fileChooser;
    }

    public WWOUnitsFormat getUnits()
    {
        return this.unitsFormat;
    }

    public NetworkActivitySignal getNetworkActivitySignal()
    {
        return (NetworkActivitySignal) this.getRegisteredObject(Constants.NETWORK_STATUS_SIGNAL);
    }

    public void redraw()
    {
        if (this.getWWd() != null)
            this.getWWd().redraw();
    }

    public LayerList getActiveLayers()
    {
        return getWWd().getModel().getLayers();
    }

    public Layer addInternalLayer(Layer layer)
    {
        return addLayer(layer, Constants.INTERNAL_LAYER);
    }

    public Layer addInternalActiveLayer(Layer layer)
    {
        // Internal Active layers are not shown in the layer tree but are shown in the active layers list
        layer.setValue(Constants.ACTIVE_LAYER, true);

        return addLayer(layer, Constants.INTERNAL_LAYER);
    }

    private Layer addLayer(Layer layer, String layerType)
    {
        if (layer != null)
        {
            layer.setValue(layerType, true);
            this.getWWPanel().addLayer(layer);
        }

        return layer;
    }

    public void moveToLocation(PointOfInterest location)
    {
        this.moveToLocation(location.getLatlon());
    }

    private static final double GOTO_ALTITUDE = 100000; // 100 km

    public void moveToLocation(LatLon location)
    {
        Double curAlt = this.getCurrentAltitude();
        double newAlt = curAlt != null && curAlt <= GOTO_ALTITUDE ? curAlt : GOTO_ALTITUDE;
        this.moveToLocation(new gov.nasa.worldwind.geom.Position(location, newAlt));
    }

    public Double getCurrentAltitude()
    {
        View view = this.getWWd().getView();
        return view != null ? view.getEyePosition().getElevation() : null;
    }

    public void moveToLocation(Position position)
    {
        OrbitView view = (OrbitView) this.getWWd().getView();
        Globe globe = this.getWWd().getModel().getGlobe();

        if (globe != null && view != null)
        {
            ((OrbitViewInputHandler) view.getViewInputHandler()).addPanToAnimator(position,
                Angle.ZERO, Angle.ZERO, position.elevation, true);
        }
    }

    public void setCursor(java.awt.Cursor cursor)
    {
        if (!((Component) this.getWWd()).getCursor().equals(cursor))
            ((Component) this.getWWd()).setCursor(cursor);
    }

    public String setStatusMessage(String message)
    {
        return this.getStatusPanel() != null ? this.getStatusPanel().setStatusMessage(message) : null;
    }

    public Object getRegisteredObject(String objectID)
    {
        Object o = this.registry.getRegisteredObject(objectID);
        if (o == null)
            return null;

        if (!(o instanceof Class))
            return this.registry.getRegisteredObject(objectID);

        try
        {
            // Create on-demand objects
            Object newObj = this.createAndRegisterObject(objectID, o);
            if (newObj instanceof Initializable)
                ((Initializable) newObj).initialize(this);
            return newObj;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void registerObject(String objectID, Object o)
    {
        this.registry.registerObject(objectID, o);
    }

    public Object createAndRegisterObject(String objectID, Object className)
        throws IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        return this.registry.createAndRegisterObject(objectID, className);
    }

    public Object createRegistryObject(String className)
        throws IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        return this.registry.createRegistryObject(className);
    }

    public void showErrorDialog(Exception e, String title, String message, Object... args)
    {
        this.showMessageDialog(formatMessage(e, message, args), title, JOptionPane.ERROR_MESSAGE);
    }

    public void showErrorDialogLater(final Exception e, final String title, final String message, final Object... args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                showErrorDialog(e, title, message, args);
            }
        });
    }

    public void showCommunicationErrorDialogLater(final Exception e, final String message, final Object... args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                showCommunicationErrorDialog(e, message, args);
            }
        });
    }

    public void showMessageDialog(Object message, String title, int messageType)
    {
        this.showMessageDialog(this.getFrame(), message, title, messageType);
    }

    public void showMessageDialog(Component component, Object message, String title, int messageType)
    {
        JOptionPane.showMessageDialog(component, message, title, messageType);
    }

    public void showMessageDialog(Object message, String title, int messageType, Object... args)
    {
        this.showMessageDialog(this.getFrame(), message, title, messageType, args);
    }

    public void showMessageDialog(Component component, Object message, String title, int messageType, Object... args)
    {
        JOptionPane.showMessageDialog(component, formatMessage(null, message, args), title, messageType);
    }

    public void showCommunicationErrorDialog(Exception e, String message, Object... args)
    {
        this.showMessageDialog(formatMessage(e, message, args), "Communication Error", JOptionPane.ERROR_MESSAGE);
    }

    public int showOptionDialog(Object message, String title, int optionType, int messageType, Icon icon,
        Object[] options, Object initialValue)
    {
        return JOptionPane.showOptionDialog(this.getFrame(), message, title, optionType, messageType,
            icon, options, initialValue);
    }

    @SuppressWarnings( {"StringConcatenationInsideStringBufferAppend"})
    private static String formatMessage(Exception e, Object message, Object[] args)
    {
        StringBuilder sb = new StringBuilder();

        if (message != null)
            sb.append(message.toString());

        if (e != null)
            sb.append((sb.length() > 0 ? "\n" : "") + e.toString());

        for (Object o : args)
        {
            if (o != null)
                sb.append((sb.length() > 0 ? "\n" : "") + o.toString());
        }

        return sb.toString();
    }

    public void openLink(String link)
    {
        if (WWUtil.isEmpty(link))
            return;

        try
        {
            try
            {
                // See if the link is a URL, and invoke the browser if it is
                URL url = new URL(link.replace(" ", "%20"));
                Desktop.getDesktop().browse(url.toURI());
                return;
            }
            catch (MalformedURLException ignored)
            { // just means that the link is not a URL
            }

            // It's not a URL, so see if it's a file and invoke the desktop to open it if it is.
            File file = new File(link);
            if (file.exists())
            {
                Desktop.getDesktop().open(new File(link));
                return;
            }

            String message = "Cannot open resource. It's not a valid file or URL.";
            Util.getLogger().log(Level.SEVERE, message);
            this.showErrorDialog(null, "No Reconocido V\u00ednculo", message);
        }
        catch (UnsupportedOperationException e)
        {
            String message = "Unable to open resource.\n" + link
                + (e.getMessage() != null ? "\n" + e.getMessage() : "");
            Util.getLogger().log(Level.SEVERE, message, e);
            this.showErrorDialog(e, "Error Opening Resource", message);
        }
        catch (IOException e)
        {
            String message = "I/O error while opening resource.\n" + link
                + (e.getMessage() != null ? ".\n" + e.getMessage() : "");
            Util.getLogger().log(Level.SEVERE, message, e);
            this.showErrorDialog(e, "I/O Error", message);
        }
        catch (Exception e)
        {
            String message = "Error attempting to open resource.\n" + link
                + (e.getMessage() != null ? "\n" + e.getMessage() : "");
            Util.getLogger().log(Level.SEVERE, message);
            this.showMessageDialog(message, "Error Opening Resource", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Raises the Save As dialog to have the user identify the location to save groups of things.
    public File determineSaveLocation(String dialogTitle, String defaultFolderName)
    {
        String defaultPath = this.getFileChooser().getCurrentDirectory().getPath();
        if (!WWUtil.isEmpty(defaultPath))
            defaultPath += File.separatorChar + defaultFolderName;

        File outFile;

        while (true)
        {
            this.getFileChooser().setDialogTitle(dialogTitle);
            this.getFileChooser().setSelectedFile(new File(defaultPath));
            this.getFileChooser().setMultiSelectionEnabled(false);
            this.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int status = this.getFileChooser().showSaveDialog(this.getFrame());
            if (status != JFileChooser.APPROVE_OPTION)
                return null;

            outFile = this.getFileChooser().getSelectedFile();
            if (outFile == null)
            {
                this.showMessageDialog("No location selected", "No Selection", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            break;
        }

        if (!outFile.exists())
            //noinspection ResultOfMethodCallIgnored
            outFile.mkdir();

        return outFile;
    }

    public File chooseOutputFile(String defaultName, String suffixWithoutDot, String dialogTitle)
    {
        String defaultPath = this.getFileChooser().getCurrentDirectory().getPath();

        if (defaultName != null && defaultName.length() > 0)
            defaultPath += File.separatorChar + defaultName;

        if (suffixWithoutDot != null && suffixWithoutDot.length() > 0)
            defaultPath += "." + suffixWithoutDot;

        if (dialogTitle == null || dialogTitle.length() == 0)
            dialogTitle = "Choose Save Location";
        this.getFileChooser().setDialogTitle(dialogTitle);

        this.getFileChooser().setSelectedFile(new File(defaultPath));
        this.getFileChooser().setMultiSelectionEnabled(false);

        while (true)
        {
            int status = this.getFileChooser().showSaveDialog(this.getFrame());
            if (status != JFileChooser.APPROVE_OPTION)
                return null;

            File outFile = this.getFileChooser().getSelectedFile();
            if (outFile == null)
            {
                this.showMessageDialog("No location selected", "No Selection", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (suffixWithoutDot != null && suffixWithoutDot.length() > 0)
                outFile = Util.ensureFileSuffix(outFile, suffixWithoutDot);

            if (outFile.exists())
            {
                status = this.showConfirmFileOverwriteDialog(outFile);
                if (status == JOptionPane.NO_OPTION)
                    continue;
                if (status == JOptionPane.CANCEL_OPTION)
                    return null;
            }

            return outFile;
        }
    }

    public int showConfirmFileOverwriteDialog(File outFile)
    {
        return JOptionPane.showConfirmDialog(this.getFrame(),
            "Replace existing file\n" + outFile.getName() + "?",
            "Overwrite Existing File?", JOptionPane.YES_NO_CANCEL_OPTION);
    }
}
