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
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: MipMappedBufferedImageRaster.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MipMappedBufferedImageRaster extends BufferedImageRaster
{
    protected BufferedImageRaster[] levelRasters;

    /**
     * Creates a mipmapped version of a BufferedImageRaster from a single BufferedImage instance.
     *
     * @param sector A sector
     * @param image  BufferedImage
     */
    public MipMappedBufferedImageRaster(Sector sector, java.awt.image.BufferedImage image)
    {
        super(sector, image);

        int maxLevel = ImageUtil.getMaxMipmapLevel(image.getWidth(), image.getHeight());
        java.awt.image.BufferedImage[] levelImages = ImageUtil.buildMipmaps(image,
            java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE, maxLevel);

        this.levelRasters = new BufferedImageRaster[1 + maxLevel];
        for (int i = 0; i <= maxLevel; i++)
        {
            this.levelRasters[i] = new BufferedImageRaster(sector, levelImages[i]);
        }
    }

    /**
     * Creates a mipmapped version of a BufferedImageRaster from multi-resolution array od BufferedImage instances.
     *
     * @param sector A sector
     * @param images An array of BufferedImages
     */
    public MipMappedBufferedImageRaster(Sector sector, java.awt.image.BufferedImage[] images)
    {
        super(sector, (null != images && images.length > 0) ? images[0] : null);

        if (null == sector)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null == images || images.length == 0)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.levelRasters = new BufferedImageRaster[images.length];
        for (int i = 0; i < images.length; i++)
        {
            this.levelRasters[i] = new BufferedImageRaster(sector, images[i]);
        }
    }

    public long getSizeInBytes()
    {
        long sizeInBytes = 0L;
        for (BufferedImageRaster raster : this.levelRasters)
        {
            sizeInBytes += raster.getSizeInBytes();
        }

        return sizeInBytes;
    }

    public void dispose()
    {
        for (BufferedImageRaster raster : this.levelRasters)
        {
            raster.dispose();
        }
    }

    protected void doDrawOnTo(BufferedImageRaster canvas)
    {
        if (!this.getSector().intersects(canvas.getSector()))
        {
            return;
        }

        BufferedImageRaster raster = this.chooseRasterForCanvas(canvas);
        raster.doDrawOnTo(canvas);
    }

    protected BufferedImageRaster chooseRasterForCanvas(BufferedImageRaster canvas)
    {
        int level = this.computeMipmapLevel(
            this.getWidth(), this.getHeight(), this.getSector(),
            canvas.getWidth(), canvas.getHeight(), canvas.getSector());

        int maxLevel = this.levelRasters.length - 1;
        level = (int) WWMath.clamp(level, 0, maxLevel);

        return this.levelRasters[level];
    }

    protected int computeMipmapLevel(int sourceWidth, int sourceHeight, Sector sourceSector,
        int destWidth, int destHeight, Sector destSector)
    {
        double sy = ((double) sourceHeight / (double) destHeight)
            * (destSector.getDeltaLatDegrees() / sourceSector.getDeltaLatDegrees());
        double sx = ((double) sourceWidth / (double) destWidth)
            * (destSector.getDeltaLonDegrees() / sourceSector.getDeltaLonDegrees());
        double scale = Math.max(sx, sy);

        if (scale < 1)
        {
            return 0;
        }

        return (int) WWMath.logBase2(scale);
    }
}
