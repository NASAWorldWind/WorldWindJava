/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.dataimport;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.xpath.XPath;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Illustrates a simple application that installs imagery and elevation data for use in WorldWind. The application
 * enables the user to locate and install imagery or elevation data on the local hard drive. Once installed, the data is
 * visualized in WorldWind either as a <code>{@link gov.nasa.worldwind.layers.TiledImageLayer}</code> or an
 * <code>{@link gov.nasa.worldwind.globes.ElevationModel}</code>. The application also illustrates how to visualize data
 * that has been installed during a previous session.
 * <p>
 * For the simplest possible examples of installing imagery and elevation data, see the examples <code>{@link
 * InstallImagery}</code> and <code>{@link InstallElevations}</code>.
 *
 * @author dcollins
 * @version $Id: InstallImageryAndElevationsDemo.java 2915 2015-03-20 16:48:43Z tgaskins $
 */
public class InstallImageryAndElevationsDemo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected InstalledDataFrame installedDataFrame;

        public AppFrame()
        {
            this.installedDataFrame = new InstalledDataFrame(WorldWind.getDataFileStore(), this.getWwd());
            WWUtil.alignComponent(this, this.installedDataFrame, AVKey.RIGHT);
            this.installedDataFrame.setVisible(true);

            this.layoutComponents();
        }

        public InstalledDataFrame getInstalledDataFrame()
        {
            return this.installedDataFrame;
        }

        protected void layoutComponents()
        {
            JButton button = new JButton("Show Installed Data...");
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    getInstalledDataFrame().setVisible(true);
                }
            });

            Box box = Box.createVerticalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right
            box.add(button);
            this.getControlPanel().add(box, BorderLayout.SOUTH);
            this.validate();
            this.pack();
        }
    }

    public static class InstalledDataFrame extends JFrame
    {
        public static final String TOOLTIP_FULL_PYRAMID =
            "Installing a full pyramid takes longer and consumes more space on the user's hard drive, "
                + "but has the best runtime performance, which is important for WorldWind Server";

        public static final String TOOLTIP_PARTIAL_PYRAMID =
            "Installing a partial pyramid takes less time and consumes less space on the user's hard drive"
                + "but requires that the original data not be moved or deleted";

        protected FileStore fileStore;
        protected InstalledDataPanel dataConfigPanel;
        protected JFileChooser fileChooser;
        protected File lastUsedFolder = null;

        public InstalledDataFrame(FileStore fileStore, WorldWindow worldWindow) throws HeadlessException
        {
            if (fileStore == null)
            {
                String msg = Logging.getMessage("nullValue.FileStoreIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.fileStore = fileStore;
            this.dataConfigPanel = new InstalledDataPanel("Installed Surface Data", worldWindow);
            this.fileChooser = new JFileChooser(this.getLastUsedFolder());
            this.fileChooser.setAcceptAllFileFilterUsed(true);
            this.fileChooser.addChoosableFileFilter(new InstallableDataFilter());
            this.fileChooser.setMultiSelectionEnabled(true);
            this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            this.layoutComponents();
            this.loadPreviouslyInstalledData();
        }

        protected File getLastUsedFolder()
        {
            if (WWUtil.isEmpty(this.lastUsedFolder))
                this.setLastUsedFolder(new File(Configuration.getUserHomeDirectory()));

            return this.lastUsedFolder;
        }

        protected void setLastUsedFolder(File folder)
        {
            if (null != folder && folder.isDirectory())
                this.lastUsedFolder = folder;
        }

        protected void loadPreviouslyInstalledData()
        {
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    loadInstalledDataFromFileStore(fileStore, dataConfigPanel);
                }
            });
            t.start();
        }

        protected void installFromFiles()
        {
            int retVal = this.fileChooser.showDialog(this, "Install");
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            this.setLastUsedFolder(this.fileChooser.getCurrentDirectory());

            final File[] files = this.fileChooser.getSelectedFiles();
            if (files == null || files.length == 0)
                return;

            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    Document dataConfig = null;

                    try
                    {
                        // Install the file into a form usable by WorldWind components.
                        dataConfig = installDataFromFiles(InstalledDataFrame.this, files, fileStore);
                    }
                    catch (Exception e)
                    {
                        final String message = e.getMessage();
                        Logging.logger().log(java.util.logging.Level.FINEST, message, e);

                        // Show a message dialog indicating that the installation failed, and why.
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                JOptionPane.showMessageDialog(InstalledDataFrame.this, message, "Installation Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }

                    if (dataConfig != null)
                    {
                        AVList params = new AVListImpl();
                        addInstalledData(dataConfig, params, dataConfigPanel);
                    }
                }
            });
            thread.start();
        }

        protected void layoutComponents()
        {
            this.setTitle("Installed Data");
            this.getContentPane().setLayout(new BorderLayout(0, 0)); // hgap, vgap
            this.getContentPane().add(this.dataConfigPanel, BorderLayout.CENTER);

            JButton installButton = new JButton("Install...");
            installButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    installFromFiles();
                }
            });

            JCheckBox fullPyramidCheckBox = new JCheckBox("Create a full pyramid", true);
            // set default option "Full pyramid"
            Configuration.setValue(AVKey.PRODUCER_ENABLE_FULL_PYRAMID, true);
            Configuration.removeKey(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL);
            fullPyramidCheckBox.setToolTipText(TOOLTIP_FULL_PYRAMID);

            fullPyramidCheckBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    Object source = e.getSource();
                    if (source instanceof JCheckBox)
                    {
                        JCheckBox checkBox = (JCheckBox) source;
                        String tooltipText;

                        if (checkBox.isSelected())
                        {
                            Configuration.setValue(AVKey.PRODUCER_ENABLE_FULL_PYRAMID, true);
                            Configuration.removeKey(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL);
                            tooltipText = TOOLTIP_FULL_PYRAMID;
                        }
                        else
                        {
                            Configuration.removeKey(AVKey.PRODUCER_ENABLE_FULL_PYRAMID);
                            // Set partial pyramid level:
                            // "0" - level zero only; "1" levels 0 and 1; "2" levels 0,1,2; etc
                            // "100%" - full pyramid, "50%" half pyramid, "25%" quarter of pyramid, etc
                            // "Auto" - whatever default is set in a TileProducer (50%)
                            Configuration.setValue(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL, "50%");
                            tooltipText = TOOLTIP_PARTIAL_PYRAMID;
                        }
                        checkBox.setToolTipText(tooltipText);
                    }
                }
            });

            Box box = Box.createHorizontalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right
            box.add(installButton);
            box.add(fullPyramidCheckBox);
            this.getContentPane().add(box, BorderLayout.SOUTH);

            this.setPreferredSize(new Dimension(400, 500));
            this.validate();
            this.pack();
        }
    }

    protected static void addInstalledData(final Document dataConfig, final AVList params,
        final InstalledDataPanel panel)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    addInstalledData(dataConfig, params, panel);
                }
            });
        }
        else
        {
            panel.addInstalledData(dataConfig.getDocumentElement(), params);
        }
    }

    //**************************************************************//
    //********************  Loading Previously Installed Data  *****//
    //**************************************************************//

    protected static void loadInstalledDataFromDirectory(File dir, InstalledDataPanel panel)
    {
        String[] names = WWIO.listDescendantFilenames(dir, new DataConfigurationFilter(), false);
        if (names == null || names.length == 0)
            return;

        for (String filename : names)
        {
            Document doc = null;

            try
            {
                File dataConfigFile = new File(dir, filename);
                doc = WWXML.openDocument(dataConfigFile);
                doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
            }
            catch (WWRuntimeException e)
            {
                e.printStackTrace();
            }

            if (doc == null)
                continue;

            // This data configuration came from an existing file from disk, therefore we cannot guarantee that the
            // current version of WorldWind's data installer produced it. This data configuration file may have been
            // created by a previous version of WorldWind, or by another program. Set fallback values for any missing
            // parameters that WorldWind needs to construct a Layer or ElevationModel from this data configuration.
            AVList params = new AVListImpl();
            setFallbackParams(doc, filename, params);

            // Add the data configuraiton to the InstalledDataPanel.
            addInstalledData(doc, params, panel);
        }
    }

    protected static void loadInstalledDataFromFileStore(FileStore fileStore, InstalledDataPanel panel)
    {
        for (File file : fileStore.getLocations())
        {
            if (!file.exists())
                continue;

            if (!fileStore.isInstallLocation(file.getPath()))
                continue;

            loadInstalledDataFromDirectory(file, panel);
        }
    }

    protected static void setFallbackParams(Document dataConfig, String filename, AVList params)
    {
        XPath xpath = WWXML.makeXPath();
        Element domElement = dataConfig.getDocumentElement();

        // If the data configuration document doesn't define a cache name, then compute one using the file's path
        // relative to its file cache directory.
        String s = WWXML.getText(domElement, "DataCacheName", xpath);
        if (s == null || s.length() == 0)
            DataConfigurationUtils.getDataConfigCacheName(filename, params);

        // If the data configuration document doesn't define the data's extreme elevations, provide default values using
        // the minimum and maximum elevations of Earth.
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type.equalsIgnoreCase("ElevationModel"))
        {
            if (WWXML.getDouble(domElement, "ExtremeElevations/@min", xpath) == null)
                params.setValue(AVKey.ELEVATION_MIN, Earth.ELEVATION_MIN);
            if (WWXML.getDouble(domElement, "ExtremeElevations/@max", xpath) == null)
                params.setValue(AVKey.ELEVATION_MAX, Earth.ELEVATION_MAX);
        }
    }

    //**************************************************************//
    //********************  Installing Data From File  *************//
    //**************************************************************//

    protected static Document installDataFromFiles(Component parentComponent, File[] files, FileStore fileStore)
        throws Exception
    {
        // Create a DataStoreProducer which is capable of processing the file.
        final DataStoreProducer producer = createDataStoreProducerFromFiles(files);

        // Create a ProgressMonitor that will provide feedback on how
        final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent, "Importing ....", null, 0, 100);

        final AtomicInteger progress = new AtomicInteger(0);

        // Configure the ProgressMonitor to receive progress events from the DataStoreProducer. This stops sending
        // progress events when the user clicks the "Cancel" button, ensuring that the ProgressMonitor does not
        PropertyChangeListener progressListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if (progressMonitor.isCanceled())
                    return;

                if (evt.getPropertyName().equals(AVKey.PROGRESS))
                    progress.set((int) (100 * (Double) evt.getNewValue()));
            }
        };
        producer.addPropertyChangeListener(progressListener);
        progressMonitor.setProgress(0);

        // Configure a timer to check if the user has clicked the ProgressMonitor's "Cancel" button. If so, stop
        // production as soon as possible. This just stops the production from completing; it doesn't clean up any state
        // changes made during production,
        java.util.Timer progressTimer = new java.util.Timer();
        progressTimer.schedule(new TimerTask()
        {
            public void run()
            {
                progressMonitor.setProgress(progress.get());

                if (progressMonitor.isCanceled())
                {
                    producer.stopProduction();
                    this.cancel();
                }
            }
        }, progressMonitor.getMillisToDecideToPopup(), 100L);

        Document doc = null;
        try
        {
            // Install the file into the specified FileStore.
            doc = createDataStore(files, fileStore, producer);

            // The user clicked the ProgressMonitor's "Cancel" button. Revert any change made during production, and
            // discard the returned DataConfiguration reference.
            if (progressMonitor.isCanceled())
            {
                doc = null;
                producer.removeProductionState();
            }
        }
        finally
        {
            // Remove the progress event listener from the DataStoreProducer. stop the progress timer, and signify to the
            // ProgressMonitor that we're done.
            producer.removePropertyChangeListener(progressListener);
            producer.removeAllDataSources();
            progressMonitor.close();
            progressTimer.cancel();
        }

        return doc;
    }

    protected static Document createDataStore(File[] files,
        FileStore fileStore, DataStoreProducer producer) throws Exception
    {
        File installLocation = DataInstallUtil.getDefaultInstallLocation(fileStore);
        if (installLocation == null)
        {
            String message = Logging.getMessage("generic.NoDefaultImportLocation");
            Logging.logger().severe(message);
            return null;
        }

        // Create the production parameters. These parameters instruct the DataStoreProducer where to install the cached
        // data, and what name to put in the data configuration document.
        AVList params = new AVListImpl();

        String datasetName = askForDatasetName(suggestDatasetName(files));

        params.setValue(AVKey.DATASET_NAME, datasetName);
        params.setValue(AVKey.DATA_CACHE_NAME, datasetName);
        params.setValue(AVKey.FILE_STORE_LOCATION, installLocation.getAbsolutePath());

        // These parameters define producer's behavior:
        // create a full tile cache OR generate only first two low resolution levels
        boolean enableFullPyramid = Configuration.getBooleanValue(AVKey.PRODUCER_ENABLE_FULL_PYRAMID, false);
        if (!enableFullPyramid)
        {
            params.setValue(AVKey.SERVICE_NAME, AVKey.SERVICE_NAME_LOCAL_RASTER_SERVER);
            // retrieve the value of the AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL, default to "Auto" if missing
            String maxLevel = Configuration.getStringValue(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL, "Auto");
            params.setValue(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL, maxLevel);
        }
        else
        {
            params.setValue(AVKey.PRODUCER_ENABLE_FULL_PYRAMID, true);
        }

        producer.setStoreParameters(params);

        try
        {
            for (File file : files)
            {
                producer.offerDataSource(file, null);
                Thread.yield();
            }

            // Convert the file to a form usable by WorldWind components, according to the specified DataStoreProducer.
            // This throws an exception if production fails for any reason.
            producer.startProduction();
        }
        catch (InterruptedException ie)
        {
            producer.removeProductionState();
            Thread.interrupted();
            throw ie;
        }
        catch (Exception e)
        {
            // Exception attempting to convert the file. Revert any change made during production.
            producer.removeProductionState();
            throw e;
        }

        // Return the DataConfiguration from the production results. Since production successfully completed, the
        // DataStoreProducer should contain a DataConfiguration in the production results. We test the production
        // results anyway.
        Iterable results = producer.getProductionResults();
        if (results != null && results.iterator() != null && results.iterator().hasNext())
        {
            Object o = results.iterator().next();
            if (o != null && o instanceof Document)
            {
                return (Document) o;
            }
        }

        return null;
    }

    protected static String askForDatasetName(String suggestedName)
    {
        String datasetName = suggestedName;

        for (; ; )
        {
            Object o = JOptionPane.showInputDialog(null, "Name:", "Enter dataset name",
                JOptionPane.QUESTION_MESSAGE, null, null, datasetName);

            if (!(o instanceof String)) // user canceled the input
            {
                Thread.interrupted();

                String msg = Logging.getMessage("generic.OperationCancelled", "Import");
                Logging.logger().info(msg);
                throw new WWRuntimeException(msg);
            }

            datasetName = WWIO.replaceIllegalFileNameCharacters((String) o);

            String message = "Import as `" + datasetName + "` ?";

            int userChoice = JOptionPane.showOptionDialog(
                null, // parentComponent
                message,
                null, // title
                JOptionPane.YES_NO_CANCEL_OPTION, // option type
                JOptionPane.QUESTION_MESSAGE, // message type
                null, // icon
                new Object[] {"Yes", "Edit name", "Cancel import"}, // options
                "Yes" // default option
            );

            if (userChoice == JOptionPane.YES_OPTION)
            {
                return datasetName;
            }
            else if (userChoice == JOptionPane.NO_OPTION)
            {
//                continue;
            }
            else if (userChoice == JOptionPane.CANCEL_OPTION)
            {
                Thread.interrupted();

                String msg = Logging.getMessage("generic.OperationCancelled", "Import");
                Logging.logger().info(msg);
                throw new WWRuntimeException(msg);
            }
        }
    }

    /**
     * Suggests a name for a dataset based on pathnames of the passed files.
     * <p>
     * Attempts to extract all common words that files' path can share, removes all non-alpha-numeric chars
     *
     * @param files Array of raster files
     *
     * @return A suggested name
     */
    protected static String suggestDatasetName(File[] files)
    {
        if (null == files || files.length == 0)
            return null;

        // extract file and folder names that all files have in common
        StringBuilder sb = new StringBuilder();
        for (File file : files)
        {
            String name = file.getAbsolutePath();
            if (WWUtil.isEmpty(name))
                continue;

            name = WWIO.replaceIllegalFileNameCharacters(WWIO.replaceSuffix(name, ""));

            if (sb.length() == 0)
            {
                sb.append(name);
                continue;
            }
            else
            {
                int size = Math.min(name.length(), sb.length());
                for (int i = 0; i < size; i++)
                {
                    if (name.charAt(i) != sb.charAt(i))
                    {
                        sb.setLength(i);
                        break;
                    }
                }
            }
        }

        String name = sb.toString();
        sb.setLength(0);

        ArrayList<String> words = new ArrayList<String>();

        StringTokenizer tokens = new StringTokenizer(name, " _:/\\-=!@#$%^&()[]{}|\".,<>;`+");
        String lastWord = null;
        while (tokens.hasMoreTokens())
        {
            String word = tokens.nextToken();
            // discard empty, one-char long, and duplicated keys
            if (WWUtil.isEmpty(word) || word.length() < 2 || word.equalsIgnoreCase(lastWord))
                continue;

            lastWord = word;

            words.add(word);
            if (words.size() > 4)  // let's keep only last four words
                words.remove(0);
        }

        if (words.size() > 0)
        {
            sb.setLength(0);
            for (String word : words)
            {
                sb.append(word).append(' ');
            }
            return sb.toString().trim();
        }
        else
            return (WWUtil.isEmpty(name)) ? "change me" : name;
    }

    //**************************************************************//
    //********************  Utility Methods  ***********************//
    //**************************************************************//

    /**
     * Creates an instance of the DataStoreProducer basing on raster type. Also validates that all rasters are the same
     * types.
     *
     * @param files Array of raster files
     *
     * @return instance of the DataStoreProducer
     *
     * @throws IllegalArgumentException if types of rasters do not match, or array of raster files is null or empty
     */
    protected static DataStoreProducer createDataStoreProducerFromFiles(File[] files) throws IllegalArgumentException
    {
        if (files == null || files.length == 0)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String commonPixelFormat = null;

        for (File file : files)
        {
            AVList params = new AVListImpl();
            if (DataInstallUtil.isDataRaster(file, params))
            {
                String pixelFormat = params.getStringValue(AVKey.PIXEL_FORMAT);
                if (WWUtil.isEmpty(commonPixelFormat))
                {
                    if (WWUtil.isEmpty(pixelFormat))
                    {
                        String message = Logging.getMessage("generic.UnrecognizedSourceType", file.getAbsolutePath());
                        Logging.logger().severe(message);
                        throw new IllegalArgumentException(message);
                    }
                    else
                    {
                        commonPixelFormat = pixelFormat;
                    }
                }
                else if (commonPixelFormat != null && !commonPixelFormat.equals(pixelFormat))
                {
                    if (WWUtil.isEmpty(pixelFormat))
                    {
                        String message = Logging.getMessage("generic.UnrecognizedSourceType", file.getAbsolutePath());
                        Logging.logger().severe(message);
                        throw new IllegalArgumentException(message);
                    }
                    else
                    {
                        String reason = Logging.getMessage("generic.UnexpectedRasterType", pixelFormat);
                        String details = file.getAbsolutePath() + ": " + reason;
                        String message = Logging.getMessage("DataRaster.IncompatibleRaster", details);
                        Logging.logger().severe(message);
                        throw new IllegalArgumentException(message);
                    }
                }
            }
            else if (DataInstallUtil.isWWDotNetLayerSet(file))
            {
                // you cannot select multiple WorldWind .NET Layer Sets
                // bail out on a first raster
                return new WWDotNetLayerSetConverter();
            }
        }

        if (AVKey.IMAGE.equals(commonPixelFormat))
        {
            return new TiledImageProducer();
        }
        else if (AVKey.ELEVATION.equals(commonPixelFormat))
        {
            return new TiledElevationProducer();
        }

        String message = Logging.getMessage("generic.UnexpectedRasterType", commonPixelFormat);
        Logging.logger().severe(message);
        throw new IllegalArgumentException(message);
    }

    protected static class InstallableDataFilter extends javax.swing.filechooser.FileFilter
    {
        public InstallableDataFilter()
        {
        }

        public boolean accept(File file)
        {
            if (file == null || file.isDirectory())
                return true;

            if (DataInstallUtil.isDataRaster(file, null))
                return true;
            else if (DataInstallUtil.isWWDotNetLayerSet(file))
                return true;

            return false;
        }

        public String getDescription()
        {
            return "Supported Images/Elevations";
        }
    }

    //**************************************************************//
    //********************  Main Method  ***************************//
    //**************************************************************//

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Imagery and Elevation Installation", AppFrame.class);
    }
}
