/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: RPFPolarFrameStructure.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class RPFPolarFrameStructure extends RPFFrameStructure
{
    private final int polarPixelConstant;
    private final double polewardExtent;
    private final double equatorwardExtent;
    private final int polarFrames;

    private RPFPolarFrameStructure(
        int polarPixelConstant,
        double polewardExtent, double equatorwardExtent,
        int polarFrames)
    {
        this.polarPixelConstant = polarPixelConstant;
        this.polewardExtent = polewardExtent;
        this.equatorwardExtent = equatorwardExtent;
        this.polarFrames = polarFrames;
    }

    public static RPFPolarFrameStructure computeStructure(char zoneCode, String rpfDataType, double resolution)
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
            String message = Logging.getMessage("generic.ArgumentOutOfRange", rpfDataType);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        int nsPixelSpacingConst = northSouthPixelSpacingConstant();
        
        int polarPixelConst, polarFrames;
        if (RPFDataSeries.isCADRGDataType(rpfDataType))
        {
            polarPixelConst = polarPixelConstant_CADRG(nsPixelSpacingConst, resolution);
            polarFrames = polarFrames_CADRG(polarPixelConst);

        }
        else if (RPFDataSeries.isCIBDataType(rpfDataType))
        {
            int nsPixelConst = northSouthPixelConstant_CIB(nsPixelSpacingConst, resolution);
            polarPixelConst = polarPixelConstant_CIB(nsPixelConst);
            polarFrames = polarFrames_CIB(polarPixelConst);
        }
        else
        {
            polarPixelConst = -1;
            polarFrames = -1;
        }

        double polewardExtent = polewardExtent(polewardNominalBoundary(zoneCode));
        double equatorwardExtent = equatorwardExtent(equatorwardNominalBoundary(zoneCode));


        return new RPFPolarFrameStructure(
            polarPixelConst,
            polewardExtent, equatorwardExtent,
            polarFrames);
    }

    public final int getPolarPixelConstant()
    {
        return this.polarPixelConstant;
    }

    public final double getPolewardExtent()
    {
        return this.polewardExtent;
    }

    public final double getEquatorwardExtent()
    {
        return this.equatorwardExtent;
    }

    public final int getPolarFrames()
    {
        return this.polarFrames;
    }

    // TODO: include supporting sources for CADRG in docs

    /* [Section A.5.1.1, MIL-PRF-89041A] */
    private static int northSouthPixelConstant_CIB(double northSouthPixelSpacingConstant, double groundSampleDistance)
    {
        double S = 100d / groundSampleDistance;
        double tmp = northSouthPixelSpacingConstant * S;
        tmp = 512d * (int) Math.ceil(tmp / 512d);
        tmp /= 4d;
        return 256 * (int) Math.round(tmp / 256d);
    }

    /* [Section 60.2.1, MIL-C-89038] */
    private static int polarPixelConstant_CADRG(double northSouthPixelSpacingConstant, double scale)
    {
        double S = 1000000d / scale;
        double tmp = northSouthPixelSpacingConstant * S;
        tmp = 512d * (int) Math.ceil(tmp / 512d);
        tmp *= (20d / 360d);
        tmp /= (150d / 100d);
        tmp = 512d * (int) Math.round(tmp / 512d);
        return (int) (tmp * 360d / 20d);
    }

    /* [Section A.5.2.1, MIL-PRF-89041A */
    private static int polarPixelConstant_CIB(double northSouthPixelConstant)
    {
        double tmp = northSouthPixelConstant * 20d / 90d;
        tmp = 512d * (int) Math.round(tmp / 512d);
        return (int) (tmp * 90d / 20d);
    }

    /* [Section 60.2.3, MIL-C-89038] */
    private static int polarFrames_CADRG(double polarPixelConstant)
    {
        double tmp = polarPixelConstant * 20d / 360d;
        tmp /= 256d;
        tmp /= 6d;
        tmp = Math.ceil(tmp);
        if (((int) tmp) % 2 == 0)
            tmp = tmp + 1;
        return (int) tmp;
    }

    /* [Section A.5.2.2, MIL-PRF-89041A] */
    private static int polarFrames_CIB(double polarPixelConstant)
    {
        double tmp = polarPixelConstant * 20d / 90d;
        tmp /= 256d;
        tmp += 4d;
        tmp /= 6d;
        tmp = Math.ceil(tmp);
        if (((int) tmp) % 2 == 0)
            tmp = tmp + 1;
        return (int) tmp;
    }

    /* [Section A.5.2.3, MIL-PRF-89041A] */
    private static double polewardExtent(double polewardNominalBoundary)
    {
        return polewardNominalBoundary;
    }

    /* [Section A.5.2.3, MIL-PRF-89041A] */
    private static double equatorwardExtent(double equatorwardNominalBoundary)
    {
        return equatorwardNominalBoundary;
    }
}
