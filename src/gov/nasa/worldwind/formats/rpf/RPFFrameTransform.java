/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
