/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

/**
 * @author dcollins
 * @version $Id: RPFFrameStructure.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class RPFFrameStructure
{
    static final int PIXEL_ROWS_PER_FRAME = 1536;
    static final int SUBFRAME_ROWS_PER_FRAME = 6;

    public final int getPixelRowsPerFrame()
    {
        return PIXEL_ROWS_PER_FRAME;
    }

    public final int getSubframeRowsPerFrame()
    {
        return SUBFRAME_ROWS_PER_FRAME;
    }

    /* [Table III, Section 70, MIL-A-89007] */
    private static final int NORTH_SOUTH_PIXEL_SPACING_CONSTANT = 400384;
    private static final int[] EAST_WEST_PIXEL_SPACING_CONSTANT = {
        369664, 302592, 245760, 199168, 163328, 137216, 110080, 82432};
    private static final int[] EQUATORWARD_NOMINAL_BOUNDARY = {
        0,  32, 48, 56, 64, 68, 72, 76, 80};
    private static final int[] POLEWARD_NOMINAL_BOUNDARY = {
        32, 48, 56, 64, 68, 72, 76, 80, 90};

    static int eastWestPixelSpacingConstant(char zoneCode)
    {
        int index = RPFZone.indexFor(zoneCode) % 9;
        if (index < 0)
            return -1;

        return EAST_WEST_PIXEL_SPACING_CONSTANT[index];
    }

    static int northSouthPixelSpacingConstant()
    {
        return NORTH_SOUTH_PIXEL_SPACING_CONSTANT;
    }

    static int equatorwardNominalBoundary(char zoneCode)
    {
        return nominalBoundary(zoneCode, EQUATORWARD_NOMINAL_BOUNDARY);
    }

    static int polewardNominalBoundary(char zoneCode)
    {
        return nominalBoundary(zoneCode, POLEWARD_NOMINAL_BOUNDARY);
    }

    private static int nominalBoundary(char zoneCode, int[] boundaryArray)
    {
        int index = RPFZone.indexFor(zoneCode) % 9;
        if (index < 0)
            return -1;

        if (!RPFZone.isZoneInUpperHemisphere(zoneCode))
            return 0 - boundaryArray[index];
        return boundaryArray[index];
    }
}
