/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.firstperson;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.*;

/**
 * @author jym
 * @version $Id: FlyToFlyViewAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FlyToFlyViewAnimator extends CompoundAnimator
{
    int altitudeMode;

    public FlyToFlyViewAnimator(Interpolator interpolator, int altitudeMode,
        PositionAnimator eyePositionAnimator, DoubleAnimator elevationAnimator,
        AngleAnimator headingAnimator, AngleAnimator pitchAnimator, AngleAnimator rollAnimator)
    {
        super(interpolator, eyePositionAnimator, elevationAnimator, headingAnimator, pitchAnimator, rollAnimator);
        if (interpolator == null)
        {
            this.interpolator = new ScheduledInterpolator(10000);
        }
        this.altitudeMode = altitudeMode;
    }

    public static FlyToFlyViewAnimator createFlyToFlyViewAnimator(
        BasicFlyView view,
        Position beginCenterPos, Position endCenterPos,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginElevation, double endElevation, long timeToMove, int altitudeMode)
    {
        return createFlyToFlyViewAnimator(view, beginCenterPos, endCenterPos, beginHeading, endHeading,
            beginPitch, endPitch, view.getRoll(), view.getRoll(), beginElevation, endElevation, timeToMove,
            altitudeMode);
    }

    public static FlyToFlyViewAnimator createFlyToFlyViewAnimator(
        BasicFlyView view,
        Position beginCenterPos, Position endCenterPos,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        Angle beginRoll, Angle endRoll,
        double beginElevation, double endElevation, long timeToMove, int altitudeMode)
    {
        OnSurfacePositionAnimator centerAnimator = new OnSurfacePositionAnimator(
            view.getGlobe(),
            new ScheduledInterpolator(timeToMove),
            beginCenterPos, endCenterPos,
            ViewPropertyAccessor.createEyePositionAccessor(
                view), altitudeMode);

        FlyToElevationAnimator elevAnimator = new FlyToElevationAnimator(view, view.getGlobe(),
            beginElevation, endElevation, beginCenterPos, endCenterPos, altitudeMode,
            ViewPropertyAccessor.createElevationAccessor(view));

        AngleAnimator headingAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            beginHeading, endHeading,
            ViewPropertyAccessor.createHeadingAccessor(view));

        AngleAnimator pitchAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            beginPitch, endPitch,
            ViewPropertyAccessor.createPitchAccessor(view));

        AngleAnimator rollAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            beginRoll, endRoll,
            ViewPropertyAccessor.createRollAccessor(view));

        FlyToFlyViewAnimator panAnimator = new FlyToFlyViewAnimator(
            new ScheduledInterpolator(timeToMove), altitudeMode, centerAnimator,
            elevAnimator, headingAnimator, pitchAnimator, rollAnimator);

        return (panAnimator);
    }

    public static class FlyToElevationAnimator extends ViewElevationAnimator
    {
        public FlyToElevationAnimator(BasicFlyView flyView, Globe globe,
            double beginZoom, double endZoom, LatLon beginLatLon,
            LatLon endLatLon, int altitudeMode, PropertyAccessor.DoubleAccessor propertyAccessor)
        {
            super(globe, beginZoom, endZoom, beginLatLon, endLatLon, altitudeMode, propertyAccessor);

            if (globe == null)
            {
                useMidZoom = false;
            }
            else
            {
                this.midZoom = computeMidZoom(globe, beginLatLon, endLatLon, beginZoom, endZoom);
                double maxMidZoom = flyView.getViewPropertyLimits().getEyeElevationLimits()[1];
                if (this.midZoom > maxMidZoom)
                {
                    this.midZoom = maxMidZoom;
                }
                useMidZoom = useMidZoom(beginZoom, endZoom, midZoom);
            }
            if (useMidZoom)
            {
                this.trueEndZoom = endZoom;
                this.end = this.midZoom;
            }
        }
    }

    public static class OnSurfacePositionAnimator extends PositionAnimator
    {
        Globe globe;
        int altitudeMode;
        boolean useMidZoom = true;

        public OnSurfacePositionAnimator(Globe globe, Interpolator interpolator,
            Position begin,
            Position end,
            PropertyAccessor.PositionAccessor propertyAccessor, int altitudeMode)
        {
            super(interpolator, begin, end, propertyAccessor);
            this.globe = globe;
            this.altitudeMode = altitudeMode;
        }

        @Override
        protected Position nextPosition(double interpolant)
        {
            final int MAX_SMOOTHING = 1;

            final double CENTER_START = this.useMidZoom ? 0.2 : 0.0;
            final double CENTER_STOP = this.useMidZoom ? 0.8 : 0.8;
            double latLonInterpolant = AnimationSupport.basicInterpolant(interpolant, CENTER_START, CENTER_STOP,
                MAX_SMOOTHING);

            // Invoke the standard next position functionality.
            Position pos = super.nextPosition(latLonInterpolant);

            // Check the altitude mode. If the altitude mode depends on the surface elevation we will reevaluate the
            // end position altitude. When the animation starts we may not have accurate elevation data available for
            // the end position, so recalculating the elevation as we go ensures that the animation will end at the
            // correct altitude.
            double endElevation = 0.0;
            boolean overrideEndElevation = false;

            if (this.altitudeMode == WorldWind.CLAMP_TO_GROUND)
            {
                overrideEndElevation = true;
                endElevation = this.globe.getElevation(getEnd().getLatitude(), getEnd().getLongitude());
            }
            else if (this.altitudeMode == WorldWind.RELATIVE_TO_GROUND)
            {
                overrideEndElevation = true;
                endElevation = this.globe.getElevation(getEnd().getLatitude(), getEnd().getLongitude())
                    + getEnd().getAltitude();
            }

            if (overrideEndElevation)
            {
                LatLon ll = pos; // Use interpolated lat/lon.
                double e1 = getBegin().getElevation();
                pos = new Position(ll, (1 - latLonInterpolant) * e1 + latLonInterpolant * endElevation);
            }

            return pos;
        }
    }
}
