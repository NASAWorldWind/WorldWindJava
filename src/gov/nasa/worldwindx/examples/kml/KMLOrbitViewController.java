/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.kml;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.*;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.*;

/**
 * View controller to animate a {@link OrbitView} to look at a KML feature.
 *
 * @author pabercrombie
 * @version $Id: KMLOrbitViewController.java 1838 2014-02-05 20:48:12Z dcollins $
 */
public class KMLOrbitViewController extends KMLViewController
{
    /** Minimum time for animation, in milliseconds. */
    protected final long MIN_LENGTH_MILLIS = 4000;
    /** Maximum time for animation, in milliseconds. */
    protected final long MAX_LENGTH_MILLIS = 16000;

    /** The view to animate. */
    protected OrbitView orbitView;

    /**
     * Create the view controller.
     *
     * @param wwd WorldWindow that holds the view to animate. The WorldWindow's view must be an instance of {@link
     *            OrbitView}.
     */
    protected KMLOrbitViewController(WorldWindow wwd)
    {
        super(wwd);
        this.orbitView = (OrbitView) wwd.getView();
    }

    /** {@inheritDoc} */
    @Override
    protected void goTo(KMLLookAt lookAt)
    {
        double latitude = lookAt.getLatitude() != null ? lookAt.getLatitude() : 0.0;
        double longitude = lookAt.getLongitude() != null ? lookAt.getLongitude() : 0.0;
        double altitude = lookAt.getAltitude() != null ? lookAt.getAltitude() : 0.0;
        double heading = lookAt.getHeading() != null ? lookAt.getHeading() : 0.0;
        double tilt = lookAt.getTilt() != null ? lookAt.getTilt() : 0.0;
        double range = lookAt.getRange() != null ? lookAt.getRange() : 0.0;

        String altitudeMode = lookAt.getAltitudeMode();

        Position lookAtPosition = Position.fromDegrees(latitude, longitude, altitude);

        long timeToMove = AnimationSupport.getScaledTimeMillisecs(
            this.orbitView.getCenterPosition(), lookAtPosition,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

        FlyToOrbitViewAnimator animator = FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(this.orbitView,
            this.orbitView.getCenterPosition(), lookAtPosition, this.orbitView.getHeading(),
            Angle.fromDegrees(heading), this.orbitView.getPitch(), Angle.fromDegrees(tilt),
            this.orbitView.getZoom(), range, timeToMove, KMLUtil.convertAltitudeMode(altitudeMode, WorldWind.CLAMP_TO_GROUND)); // KML default

        OrbitViewInputHandler inputHandler = (OrbitViewInputHandler) this.orbitView.getViewInputHandler();
        inputHandler.stopAnimators();
        inputHandler.addAnimator(animator);
    }

    /** {@inheritDoc} */
    @Override
    protected void goTo(KMLCamera camera)
    {
        double latitude = camera.getLatitude() != null ? camera.getLatitude() : 0.0;
        double longitude = camera.getLongitude() != null ? camera.getLongitude() : 0.0;
        double altitude = camera.getAltitude() != null ? camera.getAltitude() : 0.0;
        double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
        double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0;
        double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;

        // Roll in WWJ is opposite to KML, so change the sign of roll.
        roll = -roll;        

        String altitudeMode = camera.getAltitudeMode();

        Position cameraPosition = Position.fromDegrees(latitude, longitude, altitude);

        long timeToMove = AnimationSupport.getScaledTimeMillisecs(
            this.orbitView.getEyePosition(), cameraPosition,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

        FlyToOrbitViewAnimator panAnimator = createFlyToOrbitViewAnimator(this.orbitView, cameraPosition,
            Angle.fromDegrees(heading), Angle.fromDegrees(tilt), Angle.fromDegrees(roll), timeToMove,
            KMLUtil.convertAltitudeMode(altitudeMode, WorldWind.RELATIVE_TO_GROUND)); // Camera default, differs from KML default

        OrbitViewInputHandler inputHandler = (OrbitViewInputHandler) this.orbitView.getViewInputHandler();
        inputHandler.stopAnimators();
        inputHandler.addAnimator(panAnimator);
    }

    /**
     * Create an animator to animate an orbit view to an eye position and orientation. The animator is aware of altitude
     * mode, and will re-compute the final center position as animation runs to ensure that the calculation uses the
     * most accurate elevation data available.
     *
     * @param orbitView    View to animate.
     * @param eyePosition  Desired eye position.
     * @param heading      Desired heading.
     * @param pitch        Desired pitch.
     * @param roll         Desired roll.
     * @param timeToMove   Animation time.
     * @param altitudeMode Altitude mode of {@code eyePosition}. ({@link WorldWind#CLAMP_TO_GROUND}, {@link
     *                     WorldWind#RELATIVE_TO_GROUND}, or {@link WorldWind#ABSOLUTE}).
     *
     * @return An animator to animate an OrbitView to the desired eye position and orientation.
     */
    protected FlyToOrbitViewAnimator createFlyToOrbitViewAnimator(OrbitView orbitView, Position eyePosition,
        Angle heading, Angle pitch, Angle roll, long timeToMove, int altitudeMode)
    {
        Globe globe = orbitView.getGlobe();

        // Create a FlyView to represent the camera position. We do not actually set this view to be the active view;
        // we just use it to do the math of figuring out the forward vector and eye point and then we throw it away
        // and set the configuration in the active OrbitView.
        BasicFlyView flyView = new BasicFlyView();
        flyView.setGlobe(globe);

        flyView.setEyePosition(eyePosition);
        flyView.setHeading(heading);
        flyView.setPitch(pitch);

        Vec4 eyePoint = globe.computePointFromPosition(eyePosition);
        Vec4 forward = flyView.getCurrentForwardVector();
        Position lookAtPosition = this.computeCenterPosition(eyePosition, forward, pitch, altitudeMode);

        double range = eyePoint.distanceTo3(globe.computePointFromPosition(lookAtPosition));

        EyePositionAnimator centerAnimator = new EyePositionAnimator(new ScheduledInterpolator(timeToMove),
            orbitView.getCenterPosition(), lookAtPosition, eyePosition, forward, pitch,
            OrbitViewPropertyAccessor.createCenterPositionAccessor(orbitView), altitudeMode);

        // Create an elevation animator with ABSOLUTE altitude mode because the OrbitView altitude mode applies to the
        // center position, not the zoom.
        ViewElevationAnimator zoomAnimator = new ViewElevationAnimator(globe,
            orbitView.getZoom(), range, orbitView.getCenterPosition(), eyePosition, WorldWind.ABSOLUTE,
            OrbitViewPropertyAccessor.createZoomAccessor(orbitView));

        centerAnimator.useMidZoom = zoomAnimator.getUseMidZoom();

        AngleAnimator headingAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            orbitView.getHeading(), heading,
            ViewPropertyAccessor.createHeadingAccessor(orbitView));

        AngleAnimator pitchAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            orbitView.getPitch(), pitch,
            ViewPropertyAccessor.createPitchAccessor(orbitView));

        AngleAnimator rollAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            orbitView.getRoll(), roll,
            ViewPropertyAccessor.createRollAccessor(orbitView));

        return new FlyToOrbitViewAnimator(orbitView, new ScheduledInterpolator(timeToMove), altitudeMode,
            centerAnimator, zoomAnimator, headingAnimator, pitchAnimator, rollAnimator);
    }

    /**
     * Compute a center position from an eye position and an orientation. If the view is looking at the earth, the
     * center position is the intersection point of the globe and a ray beginning at the eye point, in the direction of
     * the forward vector. If the view is looking at the horizon, the center position is the eye position. Otherwise,
     * the center position is null.
     *
     * @param eyePosition  The eye position.
     * @param forward      The forward vector.
     * @param pitch        View pitch.
     * @param altitudeMode Altitude mode of {@code eyePosition}.
     *
     * @return The center position of the view.
     */
    protected Position computeCenterPosition(Position eyePosition, Vec4 forward, Angle pitch, int altitudeMode)
    {
        double height;
        Angle latitude = eyePosition.getLatitude();
        Angle longitude = eyePosition.getLongitude();
        Globe globe = this.wwd.getModel().getGlobe();

        if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
            height = globe.getElevation(latitude, longitude);
        else if (altitudeMode == WorldWind.RELATIVE_TO_GROUND)
            height = globe.getElevation(latitude, longitude) + eyePosition.getAltitude();
        else
            height = eyePosition.getAltitude();

        Vec4 eyePoint = globe.computePointFromPosition(new Position(latitude, longitude, height));

        // Find the intersection of the globe and the camera's forward vector. Looking at the horizon (tilt == 90)
        // is a special case because it is a valid view, but the view vector does not intersect the globe.
        Position lookAtPosition;
        final double tolerance = 0.001;
        if (Math.abs(pitch.degrees - 90.0) > tolerance)
            lookAtPosition = globe.getIntersectionPosition(new Line(eyePoint, forward));
        else // Use the camera position as the center position when looking at the horizon.
            lookAtPosition = globe.computePositionFromPoint(eyePoint);

        return lookAtPosition;
    }

    /**
     * A position animator that will compute an {@link OrbitView} center position based on an eye position and
     * orientation. The animator has an altitude mode that applies to the eye position. If the altitude mode is relative
     * to the surface elevation, then the center position will be re-computed on each iteration to ensure that the
     * animation uses the most accurate elevation data available.
     */
    protected class EyePositionAnimator extends PositionAnimator
    {
        protected Position endEyePosition;
        protected int eyeAltitudeMode;
        protected boolean useMidZoom = true;
        protected Vec4 forward;
        protected Angle pitch;

        /**
         * Create a new animator.
         *
         * @param interpolator     Interpolator to control the animation.
         * @param beginCenter      Center position at the start.
         * @param endCenter        Center position at the end. (This position will be re-evaluated based on {@code
         *                         endEyePosition} as the animation runs.
         * @param endEyePosition   Eye position at the end.
         * @param forward          Eye forward vector at the end.
         * @param pitch            View pitch at the end.
         * @param propertyAccessor Accessor to change the center position.
         * @param altitudeMode     Altitude mode of {@code endEyePosition}.
         */
        public EyePositionAnimator(Interpolator interpolator, Position beginCenter, Position endCenter,
            Position endEyePosition, Vec4 forward, Angle pitch, PropertyAccessor.PositionAccessor propertyAccessor,
            int altitudeMode)
        {
            super(interpolator, beginCenter, endCenter, propertyAccessor);

            this.forward = forward;
            this.pitch = pitch;
            this.endEyePosition = endEyePosition;
            this.eyeAltitudeMode = altitudeMode;
        }

        /** {@inheritDoc} */
        @Override
        protected Position nextPosition(double interpolant)
        {
            // Re-compute the center position if the center depends on surface elevation.

            final int MAX_SMOOTHING = 1;

            final double CENTER_START = this.useMidZoom ? 0.2 : 0.0;
            final double CENTER_STOP = this.useMidZoom ? 0.8 : 0.8;
            double latLonInterpolant = AnimationSupport.basicInterpolant(interpolant, CENTER_START, CENTER_STOP,
                MAX_SMOOTHING);

            // Invoke the standard next position functionality.
            Position pos = super.nextPosition(latLonInterpolant);

            // Evaluate altitude mode, if necessary
            if (this.eyeAltitudeMode == WorldWind.CLAMP_TO_GROUND
                || this.eyeAltitudeMode == WorldWind.RELATIVE_TO_GROUND)
            {
                Position endPos = computeCenterPosition(this.endEyePosition, this.forward, this.pitch,
                    this.eyeAltitudeMode);

                LatLon ll = pos; // Use interpolated lat/lon.
                double e1 = getBegin().getElevation();
                pos = new Position(ll, (1 - latLonInterpolant) * e1 + latLonInterpolant * endPos.getElevation());
            }

            return pos;
        }
    }
}
