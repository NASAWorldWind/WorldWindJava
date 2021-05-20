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
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.*;

/**
 * @author dcollins
 * @version $Id: RPFFrameTransform.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class RPFFrameTransform
{
    RPFFrameTransform()
    {
    }

    public static RPFFrameTransform createFrameTransform(char zoneCode, String rpfDataType, double resolution)
    {
        if (!RPFZone.isZoneCode(zoneCode))
        {
            String message = Logging.getMessage("RPFZone.UnknownZoneCode", zoneCode);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (rpfDataType == null || !RPFDataSeries.isRPFDataType(rpfDataType))
        {
            String message = Logging.getMessage("RPFDataSeries.UnkownDataType", rpfDataType);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (resolution < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", resolution);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        return newFrameTransform(zoneCode, rpfDataType, resolution);
    }

    private static RPFFrameTransform newFrameTransform(char zoneCode, String rpfDataType, double resolution)
    {
        boolean isNonpolarZone = !RPFZone.isPolarZone(zoneCode);
        if (isNonpolarZone)
        {
            return RPFNonpolarFrameTransform.createNonpolarFrameTransform(zoneCode, rpfDataType, resolution);
        }
        else
        {
            return RPFPolarFrameTransform.createPolarFrameTransform(zoneCode, rpfDataType, resolution);
        }
    }

    public abstract int getFrameNumber(int row, int column);

    public abstract int getMaximumFrameNumber();

    public abstract int getRows();

    public abstract int getColumns();

    public abstract LatLon computeFrameOrigin(int frameNumber);

    public abstract Sector computeFrameCoverage(int frameNumber);

    public abstract RPFImage[] deproject(int frameNumber, BufferedImage frame);

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    static int frameNumber(int row, int column, int columnFrames)
    {
        return column + row * columnFrames;
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    static int maxFrameNumber(int rowFrames, int columnFrames)
    {
        return (rowFrames * columnFrames) - 1;
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    static int frameRow(int frameNumber, int columnFrames)
    {
        return (int) (frameNumber / (double) columnFrames);
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    static int frameColumn(int frameNumber, int frameRow, int columnFrames)
    {
        return frameNumber - (frameRow * columnFrames);
    }

    //
    // A class to bundle the results of deprojection -- a BufferedImage and its Sector.
    //
    public class RPFImage
    {
        public Sector sector;
        public BufferedImage image;

        RPFImage(Sector sector, BufferedImage image)
        {
            this.sector = sector;
            this.image = image;
        }

        public Sector getSector()
        {
            return this.sector;
        }

        public BufferedImage getImage()
        {
            return this.image;
        }
    }
}
