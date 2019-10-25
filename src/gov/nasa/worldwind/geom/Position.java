/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author tag
 * @version $Id: Position.java 2291 2014-08-30 21:38:47Z tgaskins $
 */
public class Position extends LatLon {

    public static final Position ZERO = new Position(Angle.ZERO, Angle.ZERO, 0d);

    public final double elevation;

    public static Position fromRadians(double latitude, double longitude, double elevation) {
        return new Position(Angle.fromRadians(latitude), Angle.fromRadians(longitude), elevation);
    }

    public static Position fromDegrees(double latitude, double longitude, double elevation) {
        return new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation);
    }

    public static Position fromDegrees(double latitude, double longitude) {
        return new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), 0);
    }

    public Position(Angle latitude, Angle longitude, double elevation) {
        super(latitude, longitude);
        this.elevation = elevation;
    }

    public Position(LatLon latLon, double elevation) {
        super(latLon);
        this.elevation = elevation;
    }

    public Position(Position that) {
        this(that.latitude, that.longitude, that.elevation);
    }

    // A class that makes it easier to pass around position lists.
    public static class PositionList {

        public List<? extends Position> list;

        public PositionList(List<? extends Position> list) {
            this.list = list;
        }
    }

    /**
     * Obtains the elevation of this position
     *
     * @return this position's elevation
     */
    public double getElevation() {
        return this.elevation;
    }

    /**
     * Obtains the elevation of this position
     *
     * @return this position's elevation
     */
    public double getAltitude() {
        return this.elevation;
    }

    public Position add(Position that) {
        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.longitude));

        return new Position(lat, lon, this.elevation + that.elevation);
    }

    public Position subtract(Position that) {
        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude));

        return new Position(lat, lon, this.elevation - that.elevation);
    }

    /**
     * Returns the linear interpolation of <code>value1</code> and <code>value2</code>, treating the geographic
     * locations as simple 2D coordinate pairs, and treating the elevation values as 1D scalars.
     *
     * @param amount the interpolation factor
     * @param value1 the first position.
     * @param value2 the second position.
     *
     * @return the linear interpolation of <code>value1</code> and <code>value2</code>.
     *
     * @throws IllegalArgumentException if either position is null.
     */
    public static Position interpolate(double amount, Position value1, Position value2) {
        if (value1 == null || value2 == null) {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (amount < 0) {
            return value1;
        } else if (amount > 1) {
            return value2;
        }

        LatLon latLon = LatLon.interpolate(amount, value1, value2);
        // Elevation is independent of geographic interpolation method (i.e. rhumb, great-circle, linear), so we
        // interpolate elevation linearly.
        double elevation = WWMath.mix(amount, value1.getElevation(), value2.getElevation());

        return new Position(latLon, elevation);
    }

    /**
     * Returns the an interpolated location along the great-arc between <code>value1</code> and <code>value2</code>. The
     * position's elevation components are linearly interpolated as a simple 1D scalar value. The interpolation factor
     * <code>amount</code> defines the weight given to each value, and is clamped to the range [0, 1]. If <code>a</code>
     * is 0 or less, this returns <code>value1</code>. If <code>amount</code> is 1 or more, this returns
     * <code>value2</code>. Otherwise, this returns the position on the great-arc between <code>value1</code> and
     * <code>value2</code> with a linearly interpolated elevation component, and corresponding to the specified
     * interpolation factor.
     *
     * @param amount the interpolation factor
     * @param value1 the first position.
     * @param value2 the second position.
     *
     * @return an interpolated position along the great-arc between <code>value1</code> and <code>value2</code>, with a
     * linearly interpolated elevation component.
     *
     * @throws IllegalArgumentException if either location is null.
     */
    public static Position interpolateGreatCircle(double amount, Position value1, Position value2) {
        if (value1 == null || value2 == null) {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon latLon = LatLon.interpolateGreatCircle(amount, value1, value2);
        // Elevation is independent of geographic interpolation method (i.e. rhumb, great-circle, linear), so we
        // interpolate elevation linearly.
        double elevation = WWMath.mix(amount, value1.getElevation(), value2.getElevation());

        return new Position(latLon, elevation);
    }

    /**
     * Returns the an interpolated location along the rhumb line between <code>value1</code> and <code>value2</code>.
     * The position's elevation components are linearly interpolated as a simple 1D scalar value. The interpolation
     * factor <code>amount</code> defines the weight given to each value, and is clamped to the range [0, 1]. If
     * <code>a</code> is 0 or less, this returns <code>value1</code>. If <code>amount</code> is 1 or more, this returns
     * <code>value2</code>. Otherwise, this returns the position on the rhumb line between <code>value1</code> and
     * <code>value2</code> with a linearly interpolated elevation component, and corresponding to the specified
     * interpolation factor.
     *
     * @param amount the interpolation factor
     * @param value1 the first position.
     * @param value2 the second position.
     *
     * @return an interpolated position along the great-arc between <code>value1</code> and <code>value2</code>, with a
     * linearly interpolated elevation component.
     *
     * @throws IllegalArgumentException if either location is null.
     */
    public static Position interpolateRhumb(double amount, Position value1, Position value2) {
        if (value1 == null || value2 == null) {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon latLon = LatLon.interpolateRhumb(amount, value1, value2);
        // Elevation is independent of geographic interpolation method (i.e. rhumb, great-circle, linear), so we
        // interpolate elevation linearly.
        double elevation = WWMath.mix(amount, value1.getElevation(), value2.getElevation());

        return new Position(latLon, elevation);
    }

    public static boolean positionsCrossDateLine(Iterable<? extends Position> positions) {
        if (positions == null) {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position pos = null;
        for (Position posNext : positions) {
            if (pos != null) {
                // A segment cross the line if end pos have different longitude signs
                // and are more than 180 degress longitude apart
                if (Math.signum(pos.getLongitude().degrees) != Math.signum(posNext.getLongitude().degrees)) {
                    double delta = Math.abs(pos.getLongitude().degrees - posNext.getLongitude().degrees);
                    if (delta > 180 && delta < 360) {
                        return true;
                    }
                }
            }
            pos = posNext;
        }

        return false;
    }

    /**
     * Computes a new set of positions translated from a specified reference position to a new reference position.
     *
     * @param oldPosition the original reference position.
     * @param newPosition the new reference position.
     * @param positions the positions to translate.
     *
     * @return the translated positions, or null if the positions could not be translated.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public static List<Position> computeShiftedPositions(Position oldPosition, Position newPosition,
            Iterable<? extends Position> positions) {
        // TODO: Account for dateline spanning
        if (oldPosition == null || newPosition == null) {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (positions == null) {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        ArrayList<Position> newPositions = new ArrayList<Position>();

        double elevDelta = newPosition.getElevation() - oldPosition.getElevation();

        for (Position pos : positions) {
            Angle distance = LatLon.greatCircleDistance(oldPosition, pos);
            Angle azimuth = LatLon.greatCircleAzimuth(oldPosition, pos);
            LatLon newLocation = LatLon.greatCircleEndPosition(newPosition, azimuth, distance);
            double newElev = pos.getElevation() + elevDelta;

            newPositions.add(new Position(newLocation, newElev));
        }

        return newPositions;
    }

    public static List<Position> computeShiftedPositions(Globe globe, Position oldPosition, Position newPosition,
            Iterable<? extends Position> positions) {
        if (globe == null) {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (oldPosition == null || newPosition == null) {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (positions == null) {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        ArrayList<Position> newPositions = new ArrayList<Position>();

        double elevDelta = newPosition.getElevation() - oldPosition.getElevation();
        Vec4 oldPoint = globe.computePointFromPosition(oldPosition);
        Vec4 newPoint = globe.computePointFromPosition(newPosition);
        Vec4 delta = newPoint.subtract3(oldPoint);

        for (Position pos : positions) {
            Vec4 point = globe.computePointFromPosition(pos);
            point = point.add3(delta);
            Position newPos = globe.computePositionFromPoint(point);
            double newElev = pos.getElevation() + elevDelta;

            newPositions.add(new Position(newPos, newElev));
        }

        return newPositions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Position position = (Position) o;

        //noinspection RedundantIfStatement
        if (Double.compare(position.elevation, elevation) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = elevation != +0.0d ? Double.doubleToLongBits(elevation) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public String toString() {
        return "(" + this.latitude.toString() + ", " + this.longitude.toString() + ", " + this.elevation + ")";
    }
}
