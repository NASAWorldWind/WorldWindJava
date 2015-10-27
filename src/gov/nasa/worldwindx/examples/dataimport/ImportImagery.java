/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.dataimport;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;

/**
 * Illustrates how to import imagery into World Wind. This imports a GeoTIFF image file and displays it as a
 * <code>{@link SurfaceImage}</code>.
 *
 * @author tag
 * @version $Id: ImportImagery.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ImportImagery extends ApplicationTemplate
{
    // The data to import.
    protected static final String IMAGE_PATH = "gov/nasa/worldwindx/examples/data/craterlake-imagery-30m.tif";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Show the WAIT cursor because the import may take a while.
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Import the imagery on a thread other than the event-dispatch thread to avoid freezing the UI.
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    importImagery();

                    // Restore the cursor.
                    setCursor(Cursor.getDefaultCursor());
                }
            });

            t.start();
        }

        protected void importImagery()
        {
            try
            {
                // Read the data and save it in a temp file.
                File sourceFile = ExampleUtil.saveResourceToTempFile(IMAGE_PATH, ".tif");

                // Create a raster reader to read this type of file. The reader is created from the currently
                // configured factory. The factory class is specified in the Configuration, and a different one can be
                // specified there.
                DataRasterReaderFactory readerFactory
                    = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
                    AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
                DataRasterReader reader = readerFactory.findReaderFor(sourceFile, null);

                // Before reading the raster, verify that the file contains imagery.
                AVList metadata = reader.readMetadata(sourceFile, null);
                if (metadata == null || !AVKey.IMAGE.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
                    throw new Exception("Not an image file.");

                // Read the file into the raster. read() returns potentially several rasters if there are multiple
                // files, but in this case there is only one so just use the first element of the returned array.
                DataRaster[] rasters = reader.read(sourceFile, null);
                if (rasters == null || rasters.length == 0)
                    throw new Exception("Can't read the image file.");

                DataRaster raster = rasters[0];

                // Determine the sector covered by the image. This information is in the GeoTIFF file or auxiliary
                // files associated with the image file.
                final Sector sector = (Sector) raster.getValue(AVKey.SECTOR);
                if (sector == null)
                    throw new Exception("No location specified with image.");

                // Request a sub-raster that contains the whole image. This step is necessary because only sub-rasters
                // are reprojected (if necessary); primary rasters are not.
                int width = raster.getWidth();
                int height = raster.getHeight();

                // getSubRaster() returns a sub-raster of the size specified by width and height for the area indicated
                // by a sector. The width, height and sector need not be the full width, height and sector of the data,
                // but we use the full values of those here because we know the full size isn't huge. If it were huge
                // it would be best to get only sub-regions as needed or install it as a tiled image layer rather than
                // merely import it.
                DataRaster subRaster = raster.getSubRaster(width, height, sector, null);

                // Tne primary raster can be disposed now that we have a sub-raster. Disposal won't affect the
                // sub-raster.
                raster.dispose();

                // Verify that the sub-raster can create a BufferedImage, then create one.
                if (!(subRaster instanceof BufferedImageRaster))
                    throw new Exception("Cannot get BufferedImage.");
                BufferedImage image = ((BufferedImageRaster) subRaster).getBufferedImage();

                // The sub-raster can now be disposed. Disposal won't affect the BufferedImage.
                subRaster.dispose();

                // Create a SurfaceImage to display the image over the specified sector.
                final SurfaceImage si1 = new SurfaceImage(image, sector);

                // On the event-dispatch thread, add the imported data as an SurfaceImageLayer.
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        // Add the SurfaceImage to a layer.
                        SurfaceImageLayer layer = new SurfaceImageLayer();
                        layer.setName("Imported Surface Image");
                        layer.setPickEnabled(false);
                        layer.addRenderable(si1);

                        // Add the layer to the model and update the application's layer panel.
                        insertBeforeCompass(AppFrame.this.getWwd(), layer);

                        // Set the view to look at the imported image.
                        ExampleUtil.goTo(getWwd(), sector);
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Imagery Import", ImportImagery.AppFrame.class);
    }
}
