/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.image.*;
import java.io.File;
import java.util.logging.Level;

/**
 * Generates a preview image for a file set.
 *
 * @author tag
 * @version $Id: FileSetPreviewImageGenerator.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSetPreviewImageGenerator extends AVListImpl implements Runnable
{
    protected FileSet fileSet;
    protected int width;
    protected int height;
    protected BufferedImageRaster thumbnailRaster;

    public FileSetPreviewImageGenerator(final FileSet fileSet, int width, int height)
    {
        this.fileSet = fileSet;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run()
    {
        this.createImageRaster();
        this.composeImage();

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (getPreviewImage() != null)
                    fileSet.setImage(getPreviewImage());
            }
        });

    }

    public BufferedImage getPreviewImage()
    {
        return this.thumbnailRaster != null ? this.thumbnailRaster.getBufferedImage() : null;
    }

    protected void createImageRaster()
    {
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        this.thumbnailRaster = new BufferedImageRaster(this.fileSet.getSector(), image);
    }

    protected void composeImage()
    {
        // Loop through all files in the data set and compose them into a single raster.

        DataRasterReaderFactory readerFactory = DataInstaller.getReaderFactory();

        for (File file : this.fileSet.getFiles())
        {
            AVList params = new AVListImpl();
            DataRasterReader reader = readerFactory.findReaderFor(file, params);
            if (reader == null)
            {
                Logging.logger().fine("No reader for " + file.getPath());
                continue;
            }

            DataRaster raster = null;
            try
            {
                DataRaster[] rasters = reader.read(file, params);
                if (rasters != null && rasters.length > 0)
                    raster = rasters[0];
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileReading", e.getMessage());
                Logging.logger().finest(message);
            }

            if (raster == null)
            {
                Logging.logger().fine("No raster for " + file.getPath());
                continue;
            }

            try
            {
                raster.drawOnTo(this.thumbnailRaster);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "Exception composing preview image", e);
                continue;
            }
            finally
            {
                raster.dispose();
            }
        }
    }
}
