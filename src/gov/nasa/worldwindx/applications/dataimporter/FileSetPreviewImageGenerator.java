/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
