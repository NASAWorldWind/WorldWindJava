/*
 * Copyright (C) 2015 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.dataimport;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shows how to install a collection of DTED data.
 *
 * @author tag
 * @version $Id: InstallDTED.java 2915 2015-03-20 16:48:43Z tgaskins $
 */
public class InstallDTED extends ApplicationTemplate
{
    // Define a subdirectory in the installed-data area to place the installed elevation tiles.
    protected static final String BASE_CACHE_PATH = "DTED0/";

    // Override ApplicationTemplate.AppFrame's constructor to install an elevation dataset.
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ProgressMonitor progressMonitor;
        protected PropertyChangeListener progressListener;
        protected java.util.Timer progressTimer;
        protected TiledElevationProducer producer;

        public AppFrame()
        {
            Timer timer = new Timer(3000, new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Choose a DTED folder");
                    fileChooser.setApproveButtonText("Choose");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setMultiSelectionEnabled(false);
                    int status = fileChooser.showOpenDialog(wwjPanel);
                    if (status == JFileChooser.APPROVE_OPTION)
                    {
                        final File sourceDir = fileChooser.getSelectedFile();

                        // Show the WAIT cursor because the installation may take a while.
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));

                        // Install the elevations on a thread other than the event-dispatch thread to avoid freezing the UI.
                        Thread t = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                installElevations(sourceDir);

                                // Clean up everything on the event dispatch thread.
                                SwingUtilities.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // Restore the cursor.
                                        setCursor(Cursor.getDefaultCursor());

                                        // Clean up the producer.
                                        producer.removePropertyChangeListener(progressListener);
                                        producer.removeAllDataSources();
                                        producer = null;

                                        // Shut down progress monitoring.
                                        progressMonitor.close();
                                        progressMonitor = null;
                                        progressTimer.cancel();
                                        progressTimer = null;
                                    }
                                });
                            }
                        });

                        t.start();
                    }
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        protected void installElevations(File sourceDir)
        {
            // Get a reference to the FileStore into which we'll install the elevations.
            FileStore fileStore = WorldWind.getDataFileStore();

            // Install the elevations and get the resulting elevation model.
            ArrayList<File> sources = new ArrayList<File>();
            this.findDTEDFiles(sourceDir, sources);
            System.out.println("Found " + sources.size() + " DTED files.");
            final ElevationModel em = installElevations("DTED Elevations", sources, fileStore);
            if (em == null)
                return;

            // Add the new elevation model to the current (default) one. Must do it on the event dispatch thread.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    CompoundElevationModel model
                        = (CompoundElevationModel) AppFrame.this.getWwd().getModel().getGlobe().getElevationModel();
                    model.addElevationModel(em);

                    // Set the view to look at the installed elevations. Get the location from the elevation model's
                    // construction parameters.
                    AVList params = (AVList) em.getValue(AVKey.CONSTRUCTION_PARAMETERS);
                    Sector sector = (Sector) params.getValue(AVKey.SECTOR);
                    ExampleUtil.goTo(getWwd(), sector);
                }
            });
        }

        protected void findDTEDFiles(File directory, ArrayList<File> files)
        {
            File[] thisDirectoryFiles = directory.listFiles();
            if (thisDirectoryFiles == null)
                return;

            for (File file : thisDirectoryFiles)
            {
                if (file.isDirectory())
                {
                    this.findDTEDFiles(file, files);
                }
                else if (file.getName().endsWith("dt0")
                    || file.getName().endsWith("dt1")
                    || file.getName().endsWith("dt2"))
                {
                    files.add(file);
                }
            }
        }

        protected ElevationModel installElevations(String displayName, ArrayList<File> sources, FileStore fileStore)
        {
            // Use the FileStore's install location as the destination for the imported elevation tiles. The install
            // location is an area in the data file store for permanent storage.
            File fileStoreLocation = DataInstallUtil.getDefaultInstallLocation(fileStore);

            // Create a unique cache name that specifies the FileStore path to the installed elevations.
            String cacheName = BASE_CACHE_PATH + WWIO.replaceIllegalFileNameCharacters(displayName);

            // Create a parameter list specifying the install location information.
            AVList params = new AVListImpl();
            params.setValue(AVKey.FILE_STORE_LOCATION, fileStoreLocation.getAbsolutePath());
            params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            params.setValue(AVKey.DATASET_NAME, displayName);

            // Instruct the raster producer to produce only three initial levels and to create the remaining
            // tiles at whatever level is needed on the fly when the elevations are subsequently used.
            params.setValue(AVKey.TILED_RASTER_PRODUCER_LIMIT_MAX_LEVEL, 2); // three initial levels
            params.setValue(AVKey.SERVICE_NAME, AVKey.SERVICE_NAME_LOCAL_RASTER_SERVER); // on-the-fly tile creation

            // Create a TiledImageProducer to install the imagery.
            this.producer = new TiledElevationProducer();
            this.producer.setStoreParameters(params);
            this.producer.offerAllDataSources(sources);

            try
            {
                // Install the elevations.
                System.out.println("Starting production");
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setupProgressMonitor();
                    }
                });
                this.producer.startProduction();
            }
            catch (Exception e)
            {
                this.producer.removeProductionState();
                e.printStackTrace();
                return null;
            }
            System.out.println("Ending production");

            // Extract the data configuration document from the production results. If production successfully
            // completed, the TiledElevationProducer should always contain a document in the production results, but
            // test the results anyway.
            Iterable<?> results = this.producer.getProductionResults();
            if (results == null || results.iterator() == null || !results.iterator().hasNext())
                return null;

            Object o = results.iterator().next();
            if (o == null || !(o instanceof Document))
                return null;

            // Construct an ElevationModel by passing the data configuration document to an ElevationModelFactory.
            return (ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
                ((Document) o).getDocumentElement());
        }

        protected void setupProgressMonitor()
        {
            // Create a ProgressMonitor that will provide feedback on the installation.
            this.progressMonitor = new ProgressMonitor(this.wwjPanel, "Installing", null, 0, 100);

            final AtomicInteger progress = new AtomicInteger(0);

            // Configure the ProgressMonitor to receive progress events from the DataStoreProducer. This stops sending
            // progress events when the user clicks the "Cancel" button, ensuring that the ProgressMonitor does not
            this.progressListener = new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    if (progressMonitor.isCanceled())
                        return;

                    if (evt.getPropertyName().equals(AVKey.PROGRESS))
                        progress.set((int) (100 * (Double) evt.getNewValue()));
                }
            };
            this.producer.addPropertyChangeListener(this.progressListener);
            this.progressMonitor.setProgress(0);

            // Configure a timer to check if the user has clicked the ProgressMonitor's "Cancel" button. If so, stop
            // production as soon as possible. This just stops the production from completing; it doesn't clean up any state
            // changes made during production,
            this.progressTimer = new java.util.Timer();
            this.progressTimer.schedule(new TimerTask()
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
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind DTED Installation", InstallDTED.AppFrame.class);
    }
}
