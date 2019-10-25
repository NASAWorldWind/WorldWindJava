/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.*;

/**
 * BasicViewPropertyLimits provides an implementation of ViewPropertyLimits.
 *
 * @author jym
 * @version $Id: BasicViewPropertyLimits.java 2253 2014-08-22 16:33:46Z dcollins $
 */
public class BasicViewPropertyLimits implements ViewPropertyLimits
{
    protected Sector eyeLocationLimits;
    protected Angle minHeading;
    protected Angle maxHeading;
    protected Angle minPitch;
    protected Angle maxPitch;
    protected Angle minRoll;
    protected Angle maxRoll;
    protected double minEyeElevation;
    protected double maxEyeElevation;

    /** Creates a new BasicViewPropertyLimits with default limits. */
    public BasicViewPropertyLimits()
    {
        this.reset();
    }

    /** {@inheritDoc} */
    @Override
    public Sector getEyeLocationLimits()
    {
        return this.eyeLocationLimits;
    }

    /** {@inheritDoc} */
    @Override
    public void setEyeLocationLimits(Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.eyeLocationLimits = sector;
    }

    /** {@inheritDoc} */
    @Override
    public double[] getEyeElevationLimits()
    {
        return new double[] {this.minEyeElevation, this.maxEyeElevation};
    }

    /** {@inheritDoc} */
    @Override
    public void setEyeElevationLimits(double minValue, double maxValue)
    {
        this.minEyeElevation = minValue;
        this.maxEyeElevation = maxValue;
    }

    /** {@inheritDoc} */
    @Override
    public Angle[] getHeadingLimits()
    {
        return new Angle[] {this.minHeading, this.maxHeading};
    }

    /** {@inheritDoc} */
    @Override
    public void setHeadingLimits(Angle minAngle, Angle maxAngle)
    {
        if (minAngle == null || maxAngle == null)
        {
            String message = Logging.getMessage("nullValue.MinOrMaxAngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minHeading = minAngle;
        this.maxHeading = maxAngle;
    }

    /** {@inheritDoc} */
    @Override
    public Angle[] getPitchLimits()
    {
        return new Angle[] {this.minPitch, this.maxPitch};
    }

    /** {@inheritDoc} */
    @Override
    public void setPitchLimits(Angle minAngle, Angle maxAngle)
    {
        if (minAngle == null || maxAngle == null)
        {
            String message = Logging.getMessage("nullValue.MinOrMaxAngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minPitch = minAngle;
        this.maxPitch = maxAngle;
    }

    /** {@inheritDoc} */
    @Override
    public Angle[] getRollLimits()
    {
        return new Angle[] {this.minRoll, this.maxRoll};
    }

    /** {@inheritDoc} */
    @Override
    public void setRollLimits(Angle minAngle, Angle maxAngle)
    {
        if (minAngle == null || maxAngle == null)
        {
            String message = Logging.getMessage("nullValue.MinOrMaxAngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minRoll = minAngle;
        this.maxRoll = maxAngle;
    }

    /** {@inheritDoc} */
    public void reset()
    {
        this.eyeLocationLimits = Sector.FULL_SPHERE;
        this.minEyeElevation = -Double.MAX_VALUE;
        this.maxEyeElevation = Double.MAX_VALUE;
        this.minHeading = Angle.NEG180;
        this.maxHeading = Angle.POS180;
        this.minPitch = Angle.ZERO;
        this.maxPitch = Angle.POS90;
        this.minRoll = Angle.NEG180;
        this.maxRoll = Angle.POS180;
    }

    /** {@inheritDoc} */
    @Override
    public Position limitEyePosition(View view, Position position)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Sector sector = this.eyeLocationLimits;
        Angle lat = Angle.clamp(position.latitude, sector.getMinLatitude(), sector.getMaxLatitude());
        Angle lon = Angle.clamp(position.longitude, sector.getMinLongitude(), sector.getMaxLongitude());
        double alt = WWMath.clamp(position.elevation, this.minEyeElevation, this.maxEyeElevation);

        return new Position(lat, lon, alt);
    }

    /** {@inheritDoc} */
    @Override
    public Angle limitHeading(View view, Angle angle)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.isNonContinous2DGlobe(view.getGlobe()))
        {
            return angle; // ignore the heading limit on non-continuous 2D globes
        }

        return Angle.clamp(angle, this.minHeading, this.maxHeading);
    }

    /** {@inheritDoc} */
    @Override
    public Angle limitPitch(View view, Angle angle)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.is2DGlobe(view.getGlobe()))
        {
            return Angle.ZERO; // keep the view looking straight down on 2D globes
        }

        return Angle.clamp(angle, this.minPitch, this.maxPitch);
    }

    /** {@inheritDoc} */
    @Override
    public Angle limitRoll(View view, Angle angle)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.clamp(angle, this.minRoll, this.maxRoll);
    }

    protected boolean is2DGlobe(Globe globe)
    {
        return globe instanceof Globe2D;
    }

    protected boolean isNonContinous2DGlobe(Globe globe)
    {
        return globe instanceof Globe2D && !((Globe2D) globe).isContinuous();
    }

    /**
     * Clamp a heading angle to the range specified in a limit object.
     *
     * @param angle      angle to clamp to the allowed range.
     * @param viewLimits defines the heading limits.
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     * @deprecated Use {@link #limitHeading(gov.nasa.worldwind.View, gov.nasa.worldwind.geom.Angle)} instead.
     */
    @Deprecated
    public static Angle limitHeading(Angle angle, ViewPropertyLimits viewLimits)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (viewLimits == null)
        {
            String message = Logging.getMessage("nullValue.ViewLimitsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle[] limits = viewLimits.getHeadingLimits();
        Angle newAngle = angle;

        if (angle.compareTo(limits[0]) < 0)
        {
            newAngle = limits[0];
        }
        else if (angle.compareTo(limits[1]) > 0)
        {
            newAngle = limits[1];
        }

        return newAngle;
    }

    /**
     * Clamp a pitch angle to the range specified in a limit object.
     *
     * @param angle      angle to clamp to the allowed range.
     * @param viewLimits defines the pitch limits.
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     * @deprecated Use {@link #limitPitch(gov.nasa.worldwind.View, gov.nasa.worldwind.geom.Angle)} instead.
     */
    @Deprecated
    public static Angle limitPitch(Angle angle, ViewPropertyLimits viewLimits)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (viewLimits == null)
        {
            String message = Logging.getMessage("nullValue.ViewLimitsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle[] limits = viewLimits.getPitchLimits();
        Angle newAngle = angle;
        if (angle.compareTo(limits[0]) < 0)
        {
            newAngle = limits[0];
        }
        else if (angle.compareTo(limits[1]) > 0)
        {
            newAngle = limits[1];
        }

        return newAngle;
    }

    /**
     * Clamp a roll angle to the range specified in a limit object.
     *
     * @param angle      angle to clamp to the allowed range.
     * @param viewLimits defines the roll limits.
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     * @deprecated Use {@link #limitRoll(gov.nasa.worldwind.View, gov.nasa.worldwind.geom.Angle)} instead.
     */
    @Deprecated
    public static Angle limitRoll(Angle angle, ViewPropertyLimits viewLimits)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (viewLimits == null)
        {
            String message = Logging.getMessage("nullValue.ViewLimitsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle[] limits = viewLimits.getRollLimits();
        Angle newAngle = angle;
        if (angle.compareTo(limits[0]) < 0)
        {
            newAngle = limits[0];
        }
        else if (angle.compareTo(limits[1]) > 0)
        {
            newAngle = limits[1];
        }

        return newAngle;
    }

    /**
     * Clamp an eye elevation to the range specified in a limit object.
     *
     * @param elevation  elevation to clamp to the allowed range.
     * @param viewLimits defines the eye elevation limits.
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     * @deprecated Use {@link #limitEyePosition(gov.nasa.worldwind.View, gov.nasa.worldwind.geom.Position)} instead.
     */
    @Deprecated
    public static double limitEyeElevation(double elevation, ViewPropertyLimits viewLimits)
    {
        if (viewLimits == null)
        {
            String message = Logging.getMessage("nullValue.ViewLimitsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        double newElevation = elevation;
        double[] elevLimits = viewLimits.getEyeElevationLimits();

        if (elevation < elevLimits[0])
        {
            newElevation = elevLimits[0];
        }
        else if (elevation > elevLimits[1])
        {
            newElevation = elevLimits[1];
        }
        return (newElevation);
    }

    /**
     * Clamp eye location angles to the range specified in a limit object.
     *
     * @param latitude   latitude angle to clamp to the allowed range.
     * @param longitude  longitude angle to clamp to the allowed range.
     * @param viewLimits defines the eye location limits.
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     * @deprecated Use {@link #limitEyePosition(gov.nasa.worldwind.View, gov.nasa.worldwind.geom.Position)} instead.
     */
    @Deprecated
    public static LatLon limitEyePositionLocation(Angle latitude, Angle longitude, ViewPropertyLimits viewLimits)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (viewLimits == null)
        {
            String message = Logging.getMessage("nullValue.ViewLimitsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Sector limits = viewLimits.getEyeLocationLimits();
        Angle newLatitude = latitude;
        Angle newLongitude = longitude;

        if (latitude.compareTo(limits.getMinLatitude()) < 0)
        {
            newLatitude = limits.getMinLatitude();
        }
        else if (latitude.compareTo(limits.getMaxLatitude()) > 0)
        {
            newLatitude = limits.getMaxLatitude();
        }

        if (longitude.compareTo(limits.getMinLongitude()) < 0)
        {
            newLongitude = limits.getMinLongitude();
        }
        else if (longitude.compareTo(limits.getMaxLongitude()) > 0)
        {
            newLongitude = limits.getMaxLongitude();
        }

        return new LatLon(newLatitude, newLongitude);
    }

    //**************************************************************//
    //******************** Restorable State  ***********************//
    //**************************************************************//

    @Override
    public void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        rs.addStateValueAsSector(context, "eyeLocationLimits", this.eyeLocationLimits);
        rs.addStateValueAsDouble(context, "minEyeElevation", this.minEyeElevation);
        rs.addStateValueAsDouble(context, "maxEyeElevation", this.maxEyeElevation);
        rs.addStateValueAsDouble(context, "minHeadingDegrees", this.minHeading.degrees);
        rs.addStateValueAsDouble(context, "maxHeadingDegrees", this.maxHeading.degrees);
        rs.addStateValueAsDouble(context, "minPitchDegrees", this.minPitch.degrees);
        rs.addStateValueAsDouble(context, "maxPitchDegrees", this.maxPitch.degrees);
    }

    @Override
    public void restoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        Sector sector = rs.getStateValueAsSector(context, "eyeLocationLimits");
        if (sector != null)
            this.setEyeLocationLimits(sector);

        // Min and max center elevation.
        double[] minAndMaxValue = this.getEyeElevationLimits();
        Double min = rs.getStateValueAsDouble(context, "minEyeElevation");
        if (min != null)
            minAndMaxValue[0] = min;

        Double max = rs.getStateValueAsDouble(context, "maxEyeElevation");
        if (max != null)
            minAndMaxValue[1] = max;

        if (min != null || max != null)
            this.setEyeElevationLimits(minAndMaxValue[0], minAndMaxValue[1]);

        // Min and max heading angle.
        Angle[] minAndMaxAngle = this.getHeadingLimits();
        min = rs.getStateValueAsDouble(context, "minHeadingDegrees");
        if (min != null)
            minAndMaxAngle[0] = Angle.fromDegrees(min);

        max = rs.getStateValueAsDouble(context, "maxHeadingDegrees");
        if (max != null)
            minAndMaxAngle[1] = Angle.fromDegrees(max);

        if (min != null || max != null)
            this.setHeadingLimits(minAndMaxAngle[0], minAndMaxAngle[1]);

        // Min and max pitch angle.
        minAndMaxAngle = this.getPitchLimits();
        min = rs.getStateValueAsDouble(context, "minPitchDegrees");
        if (min != null)
            minAndMaxAngle[0] = Angle.fromDegrees(min);

        max = rs.getStateValueAsDouble(context, "maxPitchDegrees");
        if (max != null)
            minAndMaxAngle[1] = Angle.fromDegrees(max);

        if (min != null || max != null)
            this.setPitchLimits(minAndMaxAngle[0], minAndMaxAngle[1]);
    }
}
