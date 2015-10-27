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
 * @version $Id: RPFNonpolarFrameTransform.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings({"UnusedDeclaration"})
class RPFNonpolarFrameTransform extends RPFFrameTransform
{
    private final char zoneCode;
    private final String rpfDataType;
    private final double resolution;
    private final RPFNonpolarFrameStructure frameStructure;

    private RPFNonpolarFrameTransform(char zoneCode, String rpfDataType, double resolution,
                                      RPFNonpolarFrameStructure frameStructure)
    {
        this.zoneCode = zoneCode;
        this.rpfDataType = rpfDataType;
        this.resolution = resolution;
        this.frameStructure = frameStructure;
    }

    static RPFNonpolarFrameTransform createNonpolarFrameTransform(char zoneCode, String rpfDataType, double resolution)
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


        RPFNonpolarFrameStructure frameStructure = RPFNonpolarFrameStructure.computeStructure(
            zoneCode, rpfDataType, resolution);
        return new RPFNonpolarFrameTransform(zoneCode, rpfDataType, resolution, frameStructure);
    }

    public final char getZoneCode()
    {
        return this.zoneCode;
    }

    public final String getRpfDataType()
    {
        return this.rpfDataType;
    }

    public final double getResolution()
    {
        return this.resolution;
    }

    public final RPFFrameStructure getFrameStructure()
    {
        return this.frameStructure;
    }

    public int getFrameNumber(int row, int column)
    {
        return frameNumber(row, column, this.frameStructure.getLongitudinalFrames());
    }

    public int getMaximumFrameNumber()
    {
        return maxFrameNumber(this.frameStructure.getLatitudinalFrames(), this.frameStructure.getLongitudinalFrames());
    }

    public int getRows()
    {
        return this.frameStructure.getLatitudinalFrames();
    }

    public int getColumns()
    {
        return this.frameStructure.getLongitudinalFrames();
    }

    public LatLon computeFrameOrigin(int frameNumber)
    {
        if (frameNumber < 0 || frameNumber > getMaximumFrameNumber())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", frameNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int row = frameRow(frameNumber, this.frameStructure.getLongitudinalFrames());
        int col = frameColumn(frameNumber, row, this.frameStructure.getLongitudinalFrames());

        double zoneLat = RPFZone.isZoneInUpperHemisphere(this.zoneCode) ?
            this.frameStructure.getEquatorwardExtent() : this.frameStructure.getPolewardExtent();
        double n = frameOriginLatitude(row, this.frameStructure.getNorthSouthPixelConstant(),
            this.frameStructure.getPixelRowsPerFrame(), zoneLat);
        double w = frameOriginLongitude(col, this.frameStructure.getEastWestPixelConstant(),
            this.frameStructure.getPixelRowsPerFrame());

        return LatLon.fromDegrees(n, w);
    }

    public Sector computeFrameCoverage(int frameNumber)
    {
        int maxFrameNumber = maxFrameNumber(this.frameStructure.getLatitudinalFrames(),
            this.frameStructure.getLongitudinalFrames());
        if (frameNumber < 0 || frameNumber > maxFrameNumber)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", frameNumber);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int row = frameRow(frameNumber, this.frameStructure.getLongitudinalFrames());
        int col = frameColumn(frameNumber, row, this.frameStructure.getLongitudinalFrames());

        double zoneLat = RPFZone.isZoneInUpperHemisphere(this.zoneCode) ?
            this.frameStructure.getEquatorwardExtent() : this.frameStructure.getPolewardExtent();
        double n = frameOriginLatitude(row, this.frameStructure.getNorthSouthPixelConstant(),
            this.frameStructure.getPixelRowsPerFrame(), zoneLat);
        double s = n - frameDeltaLatitude(this.frameStructure.getNorthSouthPixelConstant(),
                this.frameStructure.getPixelRowsPerFrame());

        double w = frameOriginLongitude(col, this.frameStructure.getEastWestPixelConstant(),
            this.frameStructure.getPixelRowsPerFrame());
        double e = w + frameDeltaLongitude(this.frameStructure.getEastWestPixelConstant(),
                this.frameStructure.getPixelRowsPerFrame());
        //e = normalizedDegreesLongitude(e);

        return Sector.fromDegrees(s, n, w, e);
    }

    public RPFImage[] deproject(int frameNumber, BufferedImage frame) {
        // Effectively a no-op for non-polar frames.
        RPFImage[] image = new RPFImage[1];
        Sector sector = computeFrameCoverage(frameNumber);
        image[0] = new RPFImage(sector, frame);
        return image;
    }

    //public int[] computeFramesInSector(Sector sector)
    //{
    //    if (sector == null)
    //    {
    //        String message = Logging.getMessage("nullValue.SectorIsNull");
    //        Logging.logger().fine(message);
    //        throw new IllegalArgumentException(message);
    //    }
    //
    //    double minLat, maxLat;<
    //    if (this.frameStructure.getPolewardExtent() < this.frameStructure.getEquatorwardExtent())
    //    {
    //        minLat = this.frameStructure.getPolewardExtent();
    //        maxLat = this.frameStructure.getEquatorwardExtent();
    //    }
    //    else
    //    {
    //        minLat = this.frameStructure.getEquatorwardExtent();
    //        maxLat = this.frameStructure.getPolewardExtent();
    //    }
    //
    //    Sector intersection = Sector.fromDegrees(minLat, maxLat, -180, 180).intersection(sector);
    //    if (intersection == null)
    //        return null;
    //
    //    double zoneLat = RPFZone.isZoneInUpperHemisphere(this.zoneCode) ?
    //        this.frameStructure.getEquatorwardExtent() : this.frameStructure.getPolewardExtent();
    //    int startRow = frameRow(intersection.getMinLatitude().degrees,
    //            this.frameStructure.getNorthSouthPixelConstant(), this.frameStructure.getPixelRowsPerFrame(),
    //            zoneLat);
    //    int startCol = frameColumn(intersection.getMinLongitude().degrees,
    //            this.frameStructure.getEastWestPixelConstant(), this.frameStructure.getPixelRowsPerFrame());
    //    int endRow = frameRow(intersection.getMaxLatitude().degrees,
    //            this.frameStructure.getNorthSouthPixelConstant(), this.frameStructure.getPixelRowsPerFrame(),
    //            zoneLat);
    //    int endCol = frameColumn(intersection.getMaxLongitude().degrees,
    //            this.frameStructure.getEastWestPixelConstant(), this.frameStructure.getPixelRowsPerFrame());
    //
    //    int numFrames = (endRow - startRow + 1) * (endCol - startCol + 1);
    //    int[] frames = new int[numFrames];
    //    int index = 0;
    //    for (int row = startRow; row <= endRow; row++)
    //    {
    //        for (int col = startCol; col <= endCol; col++)
    //        {
    //            frames[index++] = frameNumber(row, col, this.frameStructure.getLongitudinalFrames());
    //        }
    //    }
    //
    //    return frames;
    //}

    /* [Section 30.2.1 MIL-C-89038 ] */
    private static double pixelLatitude_CADRG(double latFrameOrigin, int row, double northSouthPixelConstant)
    {
        return latFrameOrigin - (90d / northSouthPixelConstant) * row;
    }

    /* [Section 30.2.2 MIL-C-89038] */
    private static double pixelLongitude_CADRG(double lonFrameOrigin, int col, double eastWestPixelConstant)
    {
        return lonFrameOrigin + (360d / eastWestPixelConstant) * col;
    }

    /* [Section A.3.2.1 MIL-PRF-89041A] */
    private static double pixelLatitude_CIB(double latFrameOrigin, int row, double northSouthPixelConstant)
    {
        return latFrameOrigin - (90d / northSouthPixelConstant) * (row + 0.5);
    }

    /* [Section A.3.2.2 MIL-PRF-89041A] */
    private static double pixelLongitude_CIB(double lonFrameOrigin, int col, double eastWestPixelConstant)
    {
        return lonFrameOrigin + (360d / eastWestPixelConstant) * (col + 0.5);
    }

    /* [Section 30.3.1, MIL-C-89038] */
    /* [Section A.3.3.1, MIL-PRF-89041A] */
    private static int frameRow(double latitude, double northSouthPixelConstant, double pixelRowsPerFrame,
                                double zoneOriginLatitude)
    {
        return (int) (((latitude - zoneOriginLatitude) / 90d) * (northSouthPixelConstant / pixelRowsPerFrame));
    }

    /* [Section 30.3.1, MIL-C-89038] */
    /* [Section A.3.3.1, MIL-PRF-89041A] */
    private static double frameOriginLatitude(int row, double northSouthPixelConstant, double pixelRowsPerFrame,
                                              double zoneOriginLatitude)
    {
        return (90d / northSouthPixelConstant) * pixelRowsPerFrame * (row + 1) + zoneOriginLatitude;
    }

    /* [Section 30.3.2, MIL-C-89038] */
    /* [Section A.3.3.2, MIL-PRF-89041A] */
    private static int frameColumn(double longitude, double eastWestPixelConstant, double pixelRowsPerFrame)
    {
        return (int) (((longitude + 180d) / 360d) * (eastWestPixelConstant / pixelRowsPerFrame));
    }

    /* [Section 30.3.2, MIL-C-89038] */
    /* [Section A.3.3.2, MIL-PRF-89041A] */
    private static double frameOriginLongitude(int column, double eastWestPixelConstant, double pixelRowsPerFrame)
    {
        return (360d / eastWestPixelConstant) * pixelRowsPerFrame * column - 180d;
    }

    private static double frameDeltaLatitude(double northSouthPixelConstant, double pixelRowsPerFrame)
    {
        return (90d / northSouthPixelConstant) * pixelRowsPerFrame;
    }

    private static double frameDeltaLongitude(double eastWestPixelConstant, double pixelRowsPerFrame)
    {
        return (360d / eastWestPixelConstant) * pixelRowsPerFrame;
    }

    private static double normalizedDegreesLongitude(double degrees)
    {
        double lon = degrees % 360;
        return lon > 180 ? lon - 360 : lon < -180 ? 360 + lon : lon;
    }
}
