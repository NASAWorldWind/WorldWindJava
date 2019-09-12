/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.*;

/**
 * @author jym
 * @version $Id: FlyToOrbitViewAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class FlyToOrbitViewAnimator extends CompoundAnimator
{
    int altitudeMode;
    PositionAnimator centerAnimator;
    ViewElevationAnimator zoomAnimator;
    AngleAnimator headingAnimator;
    AngleAnimator pitchAnimator;
    AngleAnimator rollAnimator;
    BasicOrbitView orbitView;

    public FlyToOrbitViewAnimator(OrbitView orbitView, Interpolator interpolator, int altitudeMode,
        PositionAnimator centerAnimator, DoubleAnimator zoomAnimator,
        AngleAnimator headingAnimator, AngleAnimator pitchAnimator, AngleAnimator rollAnimator)
    {
        super(interpolator, centerAnimator, zoomAnimator, headingAnimator, pitchAnimator, rollAnimator);
        this.orbitView = (BasicOrbitView) orbitView;
        this.centerAnimator = centerAnimator;
        this.zoomAnimator = (ViewElevationAnimator) zoomAnimator;
        this.headingAnimator = headingAnimator;
        this.pitchAnimator = pitchAnimator;
        this.rollAnimator = rollAnimator;
        if (interpolator == null)
        {
            this.interpolator = new ScheduledInterpolator(10000);
        }
        this.altitudeMode = altitudeMode;
    }

    public static FlyToOrbitViewAnimator createFlyToOrbitViewAnimator(
        OrbitView orbitView,
        Position beginCenterPos, Position endCenterPos,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom, long timeToMove, int altitudeMode)
    {
        OnSurfacePositionAnimator centerAnimator = new OnSurfacePositionAnimator(orbitView.getGlobe(),
            new ScheduledInterpolator(timeToMove),
            beginCenterPos, endCenterPos,
            OrbitViewPropertyAccessor.createCenterPositionAccessor(
                orbitView), altitudeMode);

        // Create an elevation animator with ABSOLUTE altitude mode because the OrbitView altitude mode applies to the
        // center position, not the zoom.
        ViewElevationAnimator zoomAnimator = new ViewElevationAnimator(orbitView.getGlobe(),
            beginZoom, endZoom, beginCenterPos, endCenterPos, WorldWind.ABSOLUTE,
            OrbitViewPropertyAccessor.createZoomAccessor(orbitView));

        centerAnimator.useMidZoom = zoomAnimator.getUseMidZoom();

        AngleAnimator headingAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            beginHeading, endHeading,
            ViewPropertyAccessor.createHeadingAccessor(orbitView));

        AngleAnimator pitchAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            beginPitch, endPitch,
            ViewPropertyAccessor.createPitchAccessor(orbitView));

        FlyToOrbitViewAnimator panAnimator = new FlyToOrbitViewAnimator(orbitView,
            new ScheduledInterpolator(timeToMove), altitudeMode, centerAnimator,
            zoomAnimator, headingAnimator, pitchAnimator, null);

        return (panAnimator);
    }

    protected static class OnSurfacePositionAnimator extends PositionAnimator
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

    @Override
    public void stop()
    {
        if (this.altitudeMode == WorldWind.CLAMP_TO_GROUND)
        {
            this.orbitView.setViewOutOfFocus(true);
        }
        super.stop();
    }
}
