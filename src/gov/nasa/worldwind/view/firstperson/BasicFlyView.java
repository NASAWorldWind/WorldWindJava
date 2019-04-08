/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.firstperson;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.*;

import com.jogamp.opengl.GL;

/**
 * This is a basic view that implements a yaw-pitch-roll model that can be applied to first-person style view
 * applications (such as flight simulation).
 * <p/>
 * Note that the pitch angle is defined as normal to the ground plane, not parallel as in most body axis
 * representations.  This is to be consistent with the definition of pitch within WorldWind. Applications will need to
 * correct for pitch values by adding 90 degrees when commanding pitch (i.e. to get a horizontal view, enter 90 degrees
 * pitch.  To get straight down, enter 0 degrees).
 *
 * @author jym
 * @author M. Duquette
 * @version $Id: BasicFlyView.java 1933 2014-04-14 22:54:19Z dcollins $
 */
public class BasicFlyView extends BasicView
{
    protected final static double DEFAULT_MIN_ELEVATION = 0;
    protected final static double DEFAULT_MAX_ELEVATION = 4000000;
    protected final static Angle DEFAULT_MIN_PITCH = Angle.ZERO;
    protected final static Angle DEFAULT_MAX_PITCH = Angle.fromDegrees(180);

    public BasicFlyView()
    {
        this.viewInputHandler = new FlyViewInputHandler();

        this.viewLimits = new FlyViewLimits();
        this.viewLimits.setPitchLimits(DEFAULT_MIN_PITCH, DEFAULT_MAX_PITCH);
        this.viewLimits.setEyeElevationLimits(DEFAULT_MIN_ELEVATION, DEFAULT_MAX_ELEVATION);

        loadConfigurationValues();
    }

    protected void loadConfigurationValues()
    {
        Double initLat = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
        Double initLon = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
        double initElev = 50000.0;

        // Set center latitude and longitude. Do not change center elevation.
        Double initAltitude = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
        if (initAltitude != null)
            initElev = initAltitude;
        if (initLat != null && initLon != null)
        {
            initElev = ((FlyViewLimits) viewLimits).limitEyeElevation(initElev);

            setEyePosition(Position.fromDegrees(initLat, initLon, initElev));
        }

        // Set only center latitude. Do not change center longitude or center elevation.
        else if (initLat != null)
            setEyePosition(Position.fromDegrees(initLat, this.eyePosition.getLongitude().degrees, initElev));
            // Set only center longitude. Do not center latitude or center elevation.
        else if (initLon != null)
            setEyePosition(Position.fromDegrees(this.eyePosition.getLatitude().degrees, initLon, initElev));

        Double initHeading = Configuration.getDoubleValue(AVKey.INITIAL_HEADING);
        if (initHeading != null)
            setHeading(Angle.fromDegrees(initHeading));

        Double initPitch = Configuration.getDoubleValue(AVKey.INITIAL_PITCH);
        if (initPitch != null)
            setPitch(Angle.fromDegrees(initPitch));

        Double initFov = Configuration.getDoubleValue(AVKey.FOV);
        if (initFov != null)
            setFieldOfView(Angle.fromDegrees(initFov));
    }

    @Override
    public void setEyePosition(Position eyePosition)
    {
        if (eyePosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.getGlobe() != null)
        {
            double elevation = ((FlyViewLimits) this.viewLimits).limitEyeElevation(
                eyePosition, this.getGlobe());
            LatLon location = BasicViewPropertyLimits.limitEyePositionLocation(
                eyePosition.getLatitude(), eyePosition.getLongitude(), this.viewLimits);
            this.eyePosition = new Position(location, elevation);
        }
        else
        {
            LatLon location = BasicViewPropertyLimits.limitEyePositionLocation(
                eyePosition.getLatitude(), eyePosition.getLongitude(), this.viewLimits);
            this.eyePosition = new Position(location, eyePosition.getElevation());
            this.eyePosition = eyePosition;
        }

        this.updateModelViewStateID();
    }

    public Matrix getModelViewMatrix(Position eyePosition, Position centerPosition)
    {
        if (eyePosition == null || centerPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (this.globe == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Vec4 newEyePoint = this.globe.computePointFromPosition(eyePosition);
        Vec4 newCenterPoint = this.globe.computePointFromPosition(centerPosition);
        if (newEyePoint == null || newCenterPoint == null)
        {
            String message = Logging.getMessage("View.ErrorSettingOrientation", eyePosition, centerPosition);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If eye lat/lon != center lat/lon, then the surface normal at the center point will be a good value
        // for the up direction.
        Vec4 up = this.globe.computeSurfaceNormalAtPoint(newCenterPoint);
        // Otherwise, estimate the up direction by using the *current* heading with the new center position.
        Vec4 forward = newCenterPoint.subtract3(newEyePoint).normalize3();
        if (forward.cross3(up).getLength3() < 0.001)
        {
            Matrix modelview = ViewUtil.computeTransformMatrix(this.globe, eyePosition, this.heading, Angle.ZERO,
                Angle.ZERO);
            if (modelview != null)
            {
                Matrix modelviewInv = modelview.getInverse();
                if (modelviewInv != null)
                {
                    up = Vec4.UNIT_Y.transformBy4(modelviewInv);
                }
            }
        }

        if (up == null)
        {
            String message = Logging.getMessage("View.ErrorSettingOrientation", eyePosition, centerPosition);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix modelViewMatrix = ViewUtil.computeModelViewMatrix(this.globe, newEyePoint, newCenterPoint, up);

        return (modelViewMatrix);
    }

    public ViewUtil.ViewState getViewState(Position eyePosition, Position centerPosition)
    {
        if (eyePosition == null || centerPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.globe == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Vec4 newEyePoint = this.globe.computePointFromPosition(eyePosition);
        Vec4 newCenterPoint = this.globe.computePointFromPosition(centerPosition);
        if (newEyePoint == null || newCenterPoint == null)
        {
            String message = Logging.getMessage("View.ErrorSettingOrientation", eyePosition, centerPosition);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If eye lat/lon != center lat/lon, then the surface normal at the center point will be a good value
        // for the up direction.
        Vec4 up = this.globe.computeSurfaceNormalAtPoint(newCenterPoint);

        // Otherwise, estimate the up direction by using the *current* heading with the new center position.
        Vec4 forward = newCenterPoint.subtract3(newEyePoint).normalize3();
        if (forward.cross3(up).getLength3() < 0.001)
        {
            Matrix modelview = ViewUtil.computeTransformMatrix(this.globe, eyePosition, this.heading, Angle.ZERO,
                Angle.ZERO);
            if (modelview != null)
            {
                Matrix modelviewInv = modelview.getInverse();
                if (modelviewInv != null)
                    up = Vec4.UNIT_Y.transformBy4(modelviewInv);
            }
        }

        if (up == null)
        {
            String message = Logging.getMessage("View.ErrorSettingOrientation", eyePosition, centerPosition);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ViewUtil.ViewState viewState = ViewUtil.computeViewState(this.globe, newEyePoint, newCenterPoint, up);

        return (viewState);
    }

    @Override
    protected void doApply(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Update DrawContext and Globe references.
        this.dc = dc;
        this.globe = this.dc.getGlobe();

        //========== modelview matrix state ==========//
        // Compute the current modelview matrix.
        this.modelview = ViewUtil.computeTransformMatrix(this.globe, this.eyePosition, this.heading, this.pitch,
            this.roll);
        if (this.modelview == null)
            this.modelview = Matrix.IDENTITY;

        // Compute the current inverse-modelview matrix.
        this.modelviewInv = this.modelview.getInverse();
        if (this.modelviewInv == null)
            this.modelviewInv = Matrix.IDENTITY;

        //========== projection matrix state ==========//
        // Get the current OpenGL viewport state.
        int[] viewportArray = new int[4];
        this.dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
        this.viewport = new java.awt.Rectangle(viewportArray[0], viewportArray[1], viewportArray[2], viewportArray[3]);

        // Compute the current clip plane distances. The near distance depends on the far distance, so we must compute
        // the far distance first.
        this.farClipDistance = this.computeFarClipDistance();
        this.nearClipDistance = this.computeNearClipDistance();

        // Compute the current viewport dimensions.
        double viewportWidth = this.viewport.getWidth() <= 0.0 ? 1.0 : this.viewport.getWidth();
        double viewportHeight = this.viewport.getHeight() <= 0.0 ? 1.0 : this.viewport.getHeight();

        // Compute the current projection matrix.
        this.projection = Matrix.fromPerspective(this.fieldOfView, viewportWidth, viewportHeight, this.nearClipDistance,
            this.farClipDistance);

        // Compute the current frustum.
        this.frustum = Frustum.fromPerspective(this.fieldOfView, (int) viewportWidth, (int) viewportHeight,
            this.nearClipDistance, this.farClipDistance);

        //========== load GL matrix state ==========//
        loadGLViewState(dc, this.modelview, this.projection);

        //========== after apply (GL matrix state) ==========//
        afterDoApply();
    }

    protected void afterDoApply()
    {
        // Establish frame-specific values.
        this.lastEyePosition = this.computeEyePositionFromModelview();
        this.horizonDistance = this.computeHorizonDistance();

        // Clear cached computations.
        this.lastEyePoint = null;
        this.lastUpVector = null;
        this.lastForwardVector = null;
        this.lastFrustumInModelCoords = null;
    }

    @Override
    protected void setViewState(ViewUtil.ViewState viewState)
    {
        if (viewState == null)
        {
            String message = Logging.getMessage("nullValue.ViewStateIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewState.getPosition() != null)
        {
            Position eyePos = ViewUtil.normalizedEyePosition(viewState.getPosition());
            LatLon limitedLocation = BasicViewPropertyLimits.limitEyePositionLocation(
                this.eyePosition.getLatitude(),
                this.eyePosition.getLongitude(), this.getViewPropertyLimits());
            this.eyePosition = new Position(limitedLocation, eyePos.getElevation());
        }

        this.setHeading(viewState.getHeading());
        this.setPitch(viewState.getPitch());
        this.setRoll(viewState.getRoll());
    }

    @Override
    public void setHeading(Angle heading)
    {
        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = ViewUtil.normalizedHeading(heading);
        this.heading = BasicViewPropertyLimits.limitHeading(this.heading, this.getViewPropertyLimits());

        this.updateModelViewStateID();
    }

    @Override
    public void setPitch(Angle pitch)
    {
        if (pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pitch = ViewUtil.normalizedPitch(pitch);
        this.pitch = BasicViewPropertyLimits.limitPitch(this.pitch, this.getViewPropertyLimits());

        this.updateModelViewStateID();
    }

    public void setViewPropertyLimits(ViewPropertyLimits limits)
    {
        this.viewLimits = limits;
        this.setViewState(new ViewUtil.ViewState(this.getEyePosition(),
            this.getHeading(), this.getPitch(), Angle.ZERO));
    }
}
