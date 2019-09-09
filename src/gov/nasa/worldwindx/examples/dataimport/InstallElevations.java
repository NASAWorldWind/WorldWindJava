/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.dataimport;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.data.TiledElevationProducer;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;
import org.w3c.dom.Document;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Illustrates how to install elevation data into a WorldWind <code>{@link gov.nasa.worldwind.cache.FileStore}</code>.
 * <p>
 * Elevation data is installed into a FileStore by executing the following steps: <ol> <li>Choose the FileStore location
 * to place the installed elevations. This example uses the FileStore's install location. </li> <li>Compute a unique
 * cache name for the elevations. In this example, the cache name is "Examples/ElevationsName", where "ElevationsName"
 * is the elevation data's display name, stripped of any illegal filename characters.</li> <li>Install the elevations by
 * constructing, configuring and running a {@link gov.nasa.worldwind.data.TiledElevationProducer}.</li> <li>The
 * elevation data is subsequently described by a configuration {@link org.w3c.dom.Document}, which we use to construct
 * an ElevationModel via the {@link gov.nasa.worldwind.Factory} method {@link gov.nasa.worldwind.Factory#createFromConfigSource(Object,
 * gov.nasa.worldwind.avlist.AVList)}.</li> </ol>
 *
 * @author tag
 * @version $Id: InstallElevations.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class InstallElevations extends ApplicationTemplate
{
    // Define a subdirectory in the installed-data area to place the installed elevation tiles.
    protected static final String BASE_CACHE_PATH = "Examples/";
    // This example's elevations file is loaded from the following class-path resource.
    protected static final String ELEVATIONS_PATH = "gov/nasa/worldwindx/examples/data/craterlake-elev-16bit-30m.tif";

    // Override ApplicationTemplate.AppFrame's constructor to install an elevation dataset.
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Show the WAIT cursor because the installation may take a while.
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Install the elevations on a thread other than the event-dispatch thread to avoid freezing the UI.
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    installElevations();

                    // Restore the cursor.
                    setCursor(Cursor.getDefaultCursor());
                }
            });

            t.start();
        }

        protected void installElevations()
        {
            // Download the source file.
            File sourceFile = ExampleUtil.saveResourceToTempFile(ELEVATIONS_PATH, ".tif");

            // Get a reference to the FileStore into which we'll install the elevations.
            FileStore fileStore = WorldWind.getDataFileStore();

            // Install the elevations and get the resulting elevation model.
            final ElevationModel em = installElevations("Crater Lake Elevations 16bit 30m", sourceFile, fileStore);
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

        protected ElevationModel installElevations(String displayName, Object elevationSource, FileStore fileStore)
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

            // Create a TiledImageProducer to install the imagery.
            TiledElevationProducer producer = new TiledElevationProducer();
            try
            {
                // Configure the TiledElevationProducer with the parameter list and the elevation data source.
                producer.setStoreParameters(params);
                producer.offerDataSource(elevationSource, null);

                // Install the elevations.
                producer.startProduction();
            }
            catch (Exception e)
            {
                producer.removeProductionState();
                e.printStackTrace();
                return null;
            }

            // Extract the data configuration document from the production results. If production successfully
            // completed, the TiledElevationProducer should always contain a document in the production results, but
            // test the results anyway.
            Iterable<?> results = producer.getProductionResults();
            if (results == null || results.iterator() == null || !results.iterator().hasNext())
                return null;

            Object o = results.iterator().next();
            if (o == null || !(o instanceof Document))
                return null;

            // Construct an ElevationModel by passing the data configuration document to an ElevationModelFactory.
            return (ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
                ((Document) o).getDocumentElement());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Elevation Installation", InstallElevations.AppFrame.class);
    }
}
