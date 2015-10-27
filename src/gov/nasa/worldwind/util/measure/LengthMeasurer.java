/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.render.Polyline;

import java.util.ArrayList;

/**
 * Utility class to measure length along a path on a globe. <p/> <p>The measurer must be provided a list of at least two
 * positions to be able to compute a distance.</p> <p/> <p>Segments which are longer then the current maxSegmentLength
 * will be subdivided along lines following the current pathType - Polyline.LINEAR, Polyline.RHUMB_LINE or
 * Polyline.GREAT_CIRCLE.</p> <p/> <p>If the measurer is set to follow terrain, the computed length will account for
 * terrain deformations as if someone was walking along that path. Otherwise the length is the sum of the cartesian
 * distance between the positions.</p>
 * <p/>
 * <p>When following terrain the measurer will sample terrain elevations at regular intervals along the path. The
 * minimum number of samples used for the whole length can be set with setLengthTerrainSamplingSteps(). However, the
 * minimum sampling interval is 30 meters.
 *
 * @author Patrick Murris
 * @version $Id: LengthMeasurer.java 2261 2014-08-23 00:31:54Z tgaskins $
 * @see MeasureTool
 */
public class LengthMeasurer implements MeasurableLength
{
    private static final double DEFAULT_TERRAIN_SAMPLING_STEPS = 128; // number of samples when following terrain
    private static final double DEFAULT_MAX_SEGMENT_LENGTH = 100e3; // size above which segments are subdivided
    private static final double DEFAULT_MIN_SEGMENT_LENGTH = 30; // minimum length of a terrain following subdivision

    private ArrayList<? extends Position> positions;
    private ArrayList<? extends Position> subdividedPositions;
    private boolean followTerrain = false;
    private int pathType = Polyline.GREAT_CIRCLE;
    private double maxSegmentLength = DEFAULT_MAX_SEGMENT_LENGTH;
    private Sector sector;
    private double lengthTerrainSamplingSteps = DEFAULT_TERRAIN_SAMPLING_STEPS;
    protected double length = -1;

    public LengthMeasurer()
    {
    }

    public LengthMeasurer(ArrayList<? extends Position> positions)
    {
        this.setPositions(positions);
    }

    protected void clearCachedValues()
    {
        this.subdividedPositions = null;
        this.length = -1;
    }

    public ArrayList<? extends Position> getPositions()
    {
        return this.positions;
    }

    public void setPositions(ArrayList<? extends LatLon> positions, double elevation)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<Position> newPositions = new ArrayList<Position>();
        for (LatLon pos : positions)
        {
            newPositions.add(new Position(pos, elevation));
        }

        setPositions(newPositions);
    }

    public void setPositions(ArrayList<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positions = positions;
        if (this.positions.size() > 2)
            this.sector = Sector.boundingSector(this.positions);
        else
            this.sector = null;

        clearCachedValues();
    }

    public boolean isFollowTerrain()
    {
        return this.followTerrain;
    }

    /**
     * Set whether measurements should account for terrain deformations.
     *
     * @param followTerrain set to true if measurements should account for terrain deformations.
     */
    public void setFollowTerrain(boolean followTerrain)
    {
        if (this.followTerrain != followTerrain)
        {
            this.followTerrain = followTerrain;
            clearCachedValues();
        }
    }

    public int getPathType()
    {
        return this.pathType;
    }

    /**
     * Sets the type of path used when subdividing long segments, one of Polyline.GREAT_CIRCLE, which draws segments as
     * a great circle, Polyline.LINEAR, which determines the intermediate positions between segments by interpolating
     * the segment endpoints, or Polyline.RHUMB_LINE, which draws segments as a line of constant heading.
     *
     * @param pathType the type of path to measure.
     */
    public void setPathType(int pathType)
    {
        if (this.pathType != pathType)
        {
            this.pathType = pathType;
            clearCachedValues();
        }
    }

    /**
     * Get the maximum length a segment can have before being subdivided along a line following the current pathType.
     *
     * @return the maximum length a segment can have before being subdivided.
     */
    public double getMaxSegmentLength()
    {
        return this.maxSegmentLength;
    }

    /**
     * Set the maximum length a segment can have before being subdivided along a line following the current pathType.
     *
     * @param length the maximum length a segment can have before being subdivided.
     */
    public void setMaxSegmentLength(double length)
    {
        if (length <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.maxSegmentLength != length)
        {
            this.maxSegmentLength = length;
            clearCachedValues();
        }
    }

    public Sector getBoundingSector()
    {
        if (this.sector == null && this.positions != null && this.positions.size() > 2)
            this.sector = Sector.boundingSector(this.positions);

        return this.sector;
    }

    /**
     * Returns true if the current position list describe a closed path - one which last position is equal to the
     * first.
     *
     * @return true if the current position list describe a closed path.
     */
    public boolean isClosedShape()
    {
        return this.positions != null
            && this.positions.size() > 1
            && this.positions.get(0).equals(this.positions.get(this.positions.size() - 1));
    }

    /**
     * Get the number of terrain elevation samples used along the path to approximate it's terrain following length.
     *
     * @return the number of terrain elevation samples used.
     */
    public double getLengthTerrainSamplingSteps()
    {
        return this.lengthTerrainSamplingSteps;
    }

    /**
     * Set the number of terrain elevation samples to be used along the path to approximate it's terrain following
     * length.
     *
     * @param steps the number of terrain elevation samples to be used.
     */
    public void setLengthTerrainSamplingSteps(double steps)
    {
        if (steps < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", steps);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.lengthTerrainSamplingSteps != steps)
        {
            this.lengthTerrainSamplingSteps = steps;
            this.subdividedPositions = null;
            this.length = -1;
        }
    }

    /**
     * Get the path length in meter. <p/> <p>If the measurer is set to follow terrain, the computed length will account
     * for terrain deformations as if someone was walking along that path. Otherwise the length is the sum of the
     * cartesian distance between each positions.</p>
     *
     * @param globe the globe to draw terrain information from.
     *
     * @return the current path length or -1 if the position list is too short.
     */
    public double getLength(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.length = this.computeLength(globe, this.followTerrain);

        return this.length;
    }

    // *** Computing length *****************************************************************************

    protected double computeLength(Globe globe, boolean followTerrain)
    {
        if (this.positions == null || this.positions.size() < 2)
            return -1;

        if (this.subdividedPositions == null)
        {
            // Subdivide path so as to have at least segments smaller then maxSegmentLength. If follow terrain,
            // subdivide so as to have at least lengthTerrainSamplingSteps segments, but no segments shorter then
            // DEFAULT_MIN_SEGMENT_LENGTH either.
            double maxLength = this.maxSegmentLength;
            if (followTerrain)
            {
                // Recurse to compute overall path length not following terrain
                double pathLength = computeLength(globe, false);
                // Determine segment length to have enough sampling points
                maxLength = pathLength / this.lengthTerrainSamplingSteps;
                maxLength = Math.min(Math.max(maxLength, DEFAULT_MIN_SEGMENT_LENGTH), getMaxSegmentLength());
            }
            this.subdividedPositions = subdividePositions(globe, this.positions, maxLength,
                followTerrain, this.pathType);
        }

        // Sum each segment length
        double length = 0;
        Vec4 p1 = globe.computeEllipsoidalPointFromPosition(this.subdividedPositions.get(0));
        for (int i = 1; i < subdividedPositions.size(); i++)
        {
            Vec4 p2 = globe.computeEllipsoidalPointFromPosition(this.subdividedPositions.get(i));
            length += p1.distanceTo3(p2);
            p1 = p2;
        }

        return length;
    }

//    // This tries not to use the globe.computePointFromPosition so as to report accurate length on flat globes
//    // However, it shows significant deviations for east-west measurements.
//    private double computeSegmentLengthLinearApprox(Globe globe, Position pos1, Position pos2)
//    {
//        // Cartesian distance approximation for short segments - only needs globe radius
//        LatLon midPoint = LatLon.interpolate(.5, pos1, pos2);
//        double radius = globe.getRadiusAt(midPoint);
//        double cosLat = Math.cos(midPoint.getLatitude().radians);
//        return new Vec4(
//                (pos2.getLongitude().radians - pos1.getLongitude().radians) * radius * cosLat,
//                (pos2.getLatitude().radians - pos1.getLatitude().radians) * radius,
//                pos2.getElevation() - pos1.getElevation()
//        ).getLength3(); // Meters
//    }

    /**
     * Subdivide a list of positions so that no segment is longer then the provided maxLength. <p/> <p>If needed, new
     * intermediate positions will be created along lines that follow the given pathType - one of Polyline.LINEAR,
     * Polyline.RHUMB_LINE or Polyline.GREAT_CIRCLE. All position elevations will be either at the terrain surface if
     * followTerrain is true, or interpolated according to the original elevations.</p>
     *
     * @param globe         the globe to draw elevations and points from.
     * @param positions     the original position list
     * @param maxLength     the maximum length for one segment.
     * @param followTerrain true if the positions should be on the terrain surface.
     * @param pathType      the type of path to use in between two positions.
     *
     * @return a list of positions with no segment longer then maxLength and elevations following terrain or not.
     */
    protected static ArrayList<? extends Position> subdividePositions(Globe globe,
        ArrayList<? extends Position> positions,
        double maxLength, boolean followTerrain, int pathType)
    {
        return subdividePositions(globe, positions, maxLength, followTerrain, pathType, 0, positions.size());
    }

    /**
     * Subdivide a list of positions so that no segment is longer then the provided maxLength. Only the positions
     * between start and start + count - 1 will be processed. <p/> <p>If needed, new intermediate positions will be
     * created along lines that follow the given pathType - one of Polyline.LINEAR, Polyline.RHUMB_LINE or
     * Polyline.GREAT_CIRCLE. All position elevations will be either at the terrain surface if followTerrain is true, or
     * interpolated according to the original elevations.</p>
     *
     * @param globe         the globe to draw elevations and points from.
     * @param positions     the original position list
     * @param maxLength     the maximum length for one segment.
     * @param followTerrain true if the positions should be on the terrain surface.
     * @param pathType      the type of path to use in between two positions.
     * @param start         the first position indice in the original list.
     * @param count         how many positions from the original list have to be processed and returned.
     *
     * @return a list of positions with no segment longer then maxLength and elevations following terrain or not.
     */
    protected static ArrayList<? extends Position> subdividePositions(Globe globe,
        ArrayList<? extends Position> positions,
        double maxLength, boolean followTerrain, int pathType,
        int start, int count)
    {
        if (positions == null || positions.size() < start + count)
            return positions;

        ArrayList<Position> newPositions = new ArrayList<Position>();
        // Add first position
        Position pos1 = positions.get(start);
        if (followTerrain)
            newPositions.add(new Position(pos1, globe.getElevation(pos1.getLatitude(), pos1.getLongitude())));
        else
            newPositions.add(pos1);
        for (int i = 1; i < count; i++)
        {
            Position pos2 = positions.get(start + i);
            double arcLengthRadians = LatLon.greatCircleDistance(pos1, pos2).radians;
            double arcLength = arcLengthRadians * globe.getRadiusAt(LatLon.interpolate(.5, pos1, pos2));
            if (arcLength > maxLength)
            {
                // if necessary subdivide segment at regular intervals smaller then maxLength
                Angle segmentAzimuth = null;
                Angle segmentDistance = null;
                int steps = (int) Math.ceil(arcLength / maxLength);  // number of intervals - at least two
                for (int j = 1; j < steps; j++)
                {
                    float s = (float) j / steps;
                    LatLon destLatLon;
                    if (pathType == Polyline.LINEAR)
                    {
                        destLatLon = LatLon.interpolate(s, pos1, pos2);
                    }
                    else if (pathType == Polyline.RHUMB_LINE)
                    {
                        if (segmentAzimuth == null)
                        {
                            segmentAzimuth = LatLon.rhumbAzimuth(pos1, pos2);
                            segmentDistance = LatLon.rhumbDistance(pos1, pos2);
                        }
                        destLatLon = LatLon.rhumbEndPosition(pos1, segmentAzimuth.radians,
                            s * segmentDistance.radians);
                    }
                    else // GREAT_CIRCLE
                    {
                        if (segmentAzimuth == null)
                        {
                            segmentAzimuth = LatLon.greatCircleAzimuth(pos1, pos2);
                            segmentDistance = LatLon.greatCircleDistance(pos1, pos2);
                        }
                        destLatLon = LatLon.greatCircleEndPosition(pos1, segmentAzimuth.radians,
                            s * segmentDistance.radians);
                    }
                    // Set elevation
                    double elevation;
                    if (followTerrain)
                        elevation = globe.getElevation(destLatLon.getLatitude(), destLatLon.getLongitude());
                    else
                        elevation = pos1.getElevation() * (1 - s) + pos2.getElevation() * s;
                    // Add new position
                    newPositions.add(new Position(destLatLon, elevation));
                }
            }
            // Finally add the segment end position
            if (followTerrain)
                newPositions.add(new Position(pos2, globe.getElevation(pos2.getLatitude(), pos2.getLongitude())));
            else
                newPositions.add(pos2);
            // Prepare for next segment
            pos1 = pos2;
        }
        return newPositions;
    }
}
