/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Controller for onscreen view controls displayed by {@link ViewControlsLayer}.
 *
 * @author Patrick Murris
 * @version $Id: ViewControlsSelectListener.java 1876 2014-03-19 17:13:30Z tgaskins $
 * @see ViewControlsLayer
 */
public class ViewControlsSelectListener implements SelectListener
{
    protected static final int DEFAULT_TIMER_DELAY = 50;

    protected WorldWindow wwd;
    protected ViewControlsLayer viewControlsLayer;

    protected ScreenAnnotation pressedControl;
    protected String pressedControlType;
    protected Point lastPickPoint = null;

    protected Timer repeatTimer;
    protected double panStep = .6;
    protected double zoomStep = .8;
    protected double headingStep = 1;
    protected double pitchStep = 1;
    protected double fovStep = 1.05;
    protected double veStep = 0.1;

    /**
     * Construct a controller for specified <code>WorldWindow</code> and <code>ViewControlsLayer</code>.
     * <p>
     * <code>ViewControlLayer</code>s are not sharable among <code>WorldWindow</code>s. A separate layer and controller
     * must be established for each window that's to have view controls.
     *
     * @param wwd   the <code>WorldWindow</code> the specified layer is associated with.
     * @param layer the layer to control.
     */
    public ViewControlsSelectListener(WorldWindow wwd, ViewControlsLayer layer)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (layer == null)
        {
            String msg = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.viewControlsLayer = layer;

        // Setup repeat timer
        this.repeatTimer = new Timer(DEFAULT_TIMER_DELAY, new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                if (pressedControl != null)
                    updateView(pressedControl, pressedControlType);
            }
        });
        this.repeatTimer.start();
    }

    /**
     * Set the repeat timer delay in milliseconds.
     *
     * @param delay the repeat timer delay in milliseconds.
     *
     * @throws IllegalArgumentException if delay is less than or equal to zero.
     */
    public void setRepeatTimerDelay(int delay)
    {
        if (delay <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", delay);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.repeatTimer.setDelay(delay);
    }

    /**
     * Get the repeat timer delay in milliseconds.
     *
     * @return the repeat timer delay in milliseconds.
     */
    public int getRepeatTimerDelay()
    {
        return this.repeatTimer.getDelay();
    }

    /**
     * Set the panning distance factor. Doubling this value will double the panning speed. Negating it will reverse the
     * panning direction. Default value is .6.
     *
     * @param value the panning distance factor.
     */
    public void setPanIncrement(double value)
    {
        this.panStep = value;
    }

    /**
     * Get the panning distance factor.
     *
     * @return the panning distance factor.
     */
    public double getPanIncrement()
    {
        return this.panStep;
    }

    /**
     * Set the zoom distance factor. Doubling this value will double the zooming speed. Negating it will reverse the
     * zooming direction. Default value is .8.
     *
     * @param value the zooming distance factor.
     */
    public void setZoomIncrement(double value)
    {
        this.zoomStep = value;
    }

    /**
     * Get the zooming distance factor.
     *
     * @return the zooming distance factor.
     */
    public double getZoomIncrement()
    {
        return this.zoomStep;
    }

    /**
     * Set the heading increment value in decimal degrees. Doubling this value will double the heading change speed.
     * Negating it will reverse the heading change direction. Default value is 1 degree.
     *
     * @param value the heading increment value in decimal degrees.
     */
    public void setHeadingIncrement(double value)
    {
        this.headingStep = value;
    }

    /**
     * Get the heading increment value in decimal degrees.
     *
     * @return the heading increment value in decimal degrees.
     */
    public double getHeadingIncrement()
    {
        return this.headingStep;
    }

    /**
     * Set the pitch increment value in decimal degrees. Doubling this value will double the pitch change speed. Must be
     * positive. Default value is 1 degree.
     *
     * @param value the pitch increment value in decimal degrees.
     *
     * @throws IllegalArgumentException if value is &lt; zero.
     */
    public void setPitchIncrement(double value)
    {
        if (value < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.pitchStep = value;
    }

    /**
     * Get the pitch increment value in decimal degrees.
     *
     * @return the pitch increment value in decimal degrees.
     */
    public double getPitchIncrement()
    {
        return this.pitchStep;
    }

    /**
     * Set the field of view increment factor. At each iteration the current field of view will be multiplied or divided
     * by this value. Must be greater then or equal to one. Default value is 1.05.
     *
     * @param value the field of view increment factor.
     *
     * @throws IllegalArgumentException if value &lt; 1;
     */
    public void setFovIncrement(double value)
    {
        if (value < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.fovStep = value;
    }

    /**
     * Get the field of view increment factor.
     *
     * @return the field of view increment factor.
     */
    public double getFovIncrement()
    {
        return this.fovStep;
    }

    /**
     * Set the vertical exaggeration increment. At each iteration the current vertical exaggeration will be increased or
     * decreased by this amount. Must be greater than or equal to zero. Default value is 0.1.
     *
     * @param value the vertical exaggeration increment.
     *
     * @throws IllegalArgumentException if value &lt; 0.
     */
    public void setVeIncrement(double value)
    {
        if (value < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.veStep = value;
    }

    /**
     * Get the vertical exaggeration increment.
     *
     * @return the vertical exaggeration increment.
     */
    public double getVeIncrement()
    {
        return this.veStep;
    }

    public void selected(SelectEvent event)
    {
        if (this.wwd == null)
            return;

        if (!(this.wwd.getView() instanceof OrbitView))
            return;

        OrbitView view = (OrbitView) this.wwd.getView();

        if (this.viewControlsLayer.getHighlightedObject() != null)
        {
            this.viewControlsLayer.highlight(null);
            this.wwd.redraw(); // must redraw so the de-highlight can take effect
        }

        if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed())
            return;

        if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() != this.getParentLayer()
            || !(event.getTopObject() instanceof AVList))
            return;

        String controlType = ((AVList) event.getTopObject()).getStringValue(AVKey.VIEW_OPERATION);
        if (controlType == null)
            return;

        ScreenAnnotation selectedObject = (ScreenAnnotation) event.getTopObject();

        this.lastPickPoint = event.getPickPoint();
        if (event.getEventAction().equals(SelectEvent.ROLLOVER))
        {
            // Highlight on rollover
            this.viewControlsLayer.highlight(selectedObject);
            this.wwd.redraw();
        }
        if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            // just consume drag events
            event.consume();
        }
        else if (event.getEventAction().equals(SelectEvent.HOVER))
        {
            // Highlight on hover
            this.viewControlsLayer.highlight(selectedObject);
            this.wwd.redraw();
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_PRESS) ||
            (event.getEventAction().equals(SelectEvent.DRAG) && controlType.equals(AVKey.VIEW_PAN)) ||
            (event.getEventAction().equals(SelectEvent.DRAG) && controlType.equals(AVKey.VIEW_LOOK)))
        {
            // Handle left press on controls
            this.pressedControl = selectedObject;
            this.pressedControlType = controlType;

            // Consume drag events, but do not consume left press events. It is not necessary to consume left press
            // events here, and doing so prevents the WorldWindow from gaining focus.
            if (event.getEventAction().equals(SelectEvent.DRAG))
                event.consume();
        }
        else if (event.getEventAction().equals(SelectEvent.LEFT_CLICK)
            || event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK)
            || event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            // Release pressed control

            if (pressedControl != null)
                event.consume();

            this.pressedControl = null;
            resetOrbitView(view);
            view.firePropertyChange(AVKey.VIEW, null, view);
        }

        // Keep pressed control highlighted - overrides rollover non currently pressed controls
        if (this.pressedControl != null)
        {
            this.viewControlsLayer.highlight(this.pressedControl);
            this.wwd.redraw();
        }
    }

    /**
     * Returns this ViewControlsSelectListener's parent layer. The parent layer is associated with picked objects, and
     * is used to determine which SelectEvents thsi ViewControlsSelectListner responds to.
     *
     * @return this ViewControlsSelectListener's parent layer.
     */
    protected Layer getParentLayer()
    {
        return this.viewControlsLayer;
    }

    protected void updateView(ScreenAnnotation control, String controlType)
    {
        if (this.wwd == null)
            return;
        if (!(this.wwd.getView() instanceof OrbitView))
            return;

        OrbitView view = (OrbitView) this.wwd.getView();
        view.stopAnimations();
        view.stopMovement();

        if (controlType.equals(AVKey.VIEW_PAN))
        {
            resetOrbitView(view);
            // Go some distance in the control mouse direction
            Angle heading = computePanHeading(view, control);
            Angle distance = computePanAmount(this.wwd.getModel().getGlobe(), view, control, panStep);
            LatLon newViewCenter = LatLon.greatCircleEndPosition(view.getCenterPosition(),
                heading, distance);
            // Turn around if passing by a pole - TODO: better handling of the pole crossing situation
            if (this.isPathCrossingAPole(newViewCenter, view.getCenterPosition()))
                view.setHeading(Angle.POS180.subtract(view.getHeading()));
            // Set new center pos
            view.setCenterPosition(new Position(newViewCenter, view.getCenterPosition().getElevation()));
        }
        else if (controlType.equals(AVKey.VIEW_LOOK))
        {
            setupFirstPersonView(view);
            Angle heading = computeLookHeading(view, control, headingStep);
            Angle pitch = computeLookPitch(view, control, pitchStep);
            // Check whether the view will still point at terrain
            Vec4 surfacePoint = computeSurfacePoint(view, heading, pitch);
            if (surfacePoint != null)
            {
                // Change view state
                final Position eyePos = view.getEyePosition();// Save current eye position
                view.setHeading(heading);
                view.setPitch(pitch);
                view.setZoom(0);
                view.setCenterPosition(eyePos); // Set center at the eye position
            }
        }
        else if (controlType.equals(AVKey.VIEW_ZOOM_IN))
        {
            resetOrbitView(view);
            view.setZoom(computeNewZoom(view, -zoomStep));
        }
        else if (controlType.equals(AVKey.VIEW_ZOOM_OUT))
        {
            resetOrbitView(view);
            view.setZoom(computeNewZoom(view, zoomStep));
        }
        else if (controlType.equals(AVKey.VIEW_HEADING_LEFT))
        {
            resetOrbitView(view);
            view.setHeading(view.getHeading().addDegrees(headingStep));
        }
        else if (controlType.equals(AVKey.VIEW_HEADING_RIGHT))
        {
            resetOrbitView(view);
            view.setHeading(view.getHeading().addDegrees(-headingStep));
        }
        else if (controlType.equals(AVKey.VIEW_PITCH_UP))
        {
            resetOrbitView(view);
            if (view.getPitch().degrees >= pitchStep)
                view.setPitch(view.getPitch().addDegrees(-pitchStep));
        }
        else if (controlType.equals(AVKey.VIEW_PITCH_DOWN))
        {
            resetOrbitView(view);
            if (view.getPitch().degrees <= 90 - pitchStep)
                view.setPitch(view.getPitch().addDegrees(pitchStep));
        }
        else if (controlType.equals(AVKey.VIEW_FOV_NARROW))
        {
            if (view.getFieldOfView().degrees / fovStep >= 4)
                view.setFieldOfView(view.getFieldOfView().divide(fovStep));
        }
        else if (controlType.equals(AVKey.VIEW_FOV_WIDE))
        {
            if (view.getFieldOfView().degrees * fovStep < 120)
                view.setFieldOfView(view.getFieldOfView().multiply(fovStep));
        }
        else if (controlType.equals(AVKey.VERTICAL_EXAGGERATION_UP))
        {
            SceneController sc = this.wwd.getSceneController();
            sc.setVerticalExaggeration(sc.getVerticalExaggeration() + this.veStep);
        }
        else if (controlType.equals(AVKey.VERTICAL_EXAGGERATION_DOWN))
        {
            SceneController sc = this.wwd.getSceneController();
            sc.setVerticalExaggeration(Math.max(1d, sc.getVerticalExaggeration() - this.veStep));
        }
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    protected boolean isPathCrossingAPole(LatLon p1, LatLon p2)
    {
        return Math.abs(p1.getLongitude().degrees - p2.getLongitude().degrees) > 20
            && Math.abs(p1.getLatitude().degrees - 90 * Math.signum(p1.getLatitude().degrees)) < 10;
    }

    protected double computeNewZoom(OrbitView view, double amount)
    {
        double coeff = 0.05;
        double change = coeff * amount;
        double logZoom = view.getZoom() != 0 ? Math.log(view.getZoom()) : 0;
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
        // 1) Zooming is slow near the globe, and fast at great distances.
        // 2) Zooming in then immediately zooming out returns the viewer to the same zoom value.
        return Math.exp(logZoom + change);
    }

    protected Angle computePanHeading(OrbitView view, ScreenAnnotation control)
    {
        // Compute last pick point 'heading' relative to pan control center
        double size = control.getAttributes().getSize().width * control.getAttributes().getScale();
        Vec4 center = new Vec4(control.getScreenPoint().x, control.getScreenPoint().y + size / 2, 0);
        double px = lastPickPoint.x - center.x;
        double py = view.getViewport().getHeight() - lastPickPoint.y - center.y;
        Angle heading = view.getHeading().add(Angle.fromRadians(Math.atan2(px, py)));
        heading = heading.degrees >= 0 ? heading : heading.addDegrees(360);
        return heading;
    }

    protected Angle computePanAmount(Globe globe, OrbitView view, ScreenAnnotation control, double panStep)
    {
        // Compute last pick point distance relative to pan control center
        double size = control.getAttributes().getSize().width * control.getAttributes().getScale();
        Vec4 center = new Vec4(control.getScreenPoint().x, control.getScreenPoint().y + size / 2, 0);
        double px = lastPickPoint.x - center.x;
        double py = view.getViewport().getHeight() - lastPickPoint.y - center.y;
        double pickDistance = Math.sqrt(px * px + py * py);
        double pickDistanceFactor = Math.min(pickDistance / 10, 5);

        // Compute globe angular distance depending on eye altitude
        Position eyePos = view.getEyePosition();
        double radius = globe.getRadiusAt(eyePos);
        double minValue = 0.5 * (180.0 / (Math.PI * radius)); // Minimum change ~0.5 meters
        double maxValue = 1.0; // Maximum change ~1 degree

        // Compute an interpolated value between minValue and maxValue, using (eye altitude)/(globe radius) as
        // the interpolant. Interpolation is performed on an exponential curve, to keep the value from
        // increasing too quickly as eye altitude increases.
        double a = eyePos.getElevation() / radius;
        a = (a < 0 ? 0 : (a > 1 ? 1 : a));
        double expBase = 2.0; // Exponential curve parameter.
        double value = minValue + (maxValue - minValue) * ((Math.pow(expBase, a) - 1.0) / (expBase - 1.0));

        return Angle.fromDegrees(value * pickDistanceFactor * panStep);
    }

    protected Angle computeLookHeading(OrbitView view, ScreenAnnotation control, double headingStep)
    {
        // Compute last pick point 'heading' relative to look control center on x
        double size = control.getAttributes().getSize().width * control.getAttributes().getScale();
        Vec4 center = new Vec4(control.getScreenPoint().x, control.getScreenPoint().y + size / 2, 0);
        double px = lastPickPoint.x - center.x;
        double pickDistanceFactor = Math.min(Math.abs(px) / 3000, 5) * Math.signum(px);
        // New heading
        Angle heading = view.getHeading().add(Angle.fromRadians(headingStep * pickDistanceFactor));
        heading = heading.degrees >= 0 ? heading : heading.addDegrees(360);
        return heading;
    }

    protected Angle computeLookPitch(OrbitView view, ScreenAnnotation control, double pitchStep)
    {
        // Compute last pick point 'pitch' relative to look control center on y
        double size = control.getAttributes().getSize().width * control.getAttributes().getScale();
        Vec4 center = new Vec4(control.getScreenPoint().x, control.getScreenPoint().y + size / 2, 0);
        double py = view.getViewport().getHeight() - lastPickPoint.y - center.y;
        double pickDistanceFactor = Math.min(Math.abs(py) / 3000, 5) * Math.signum(py);
        // New pitch
        Angle pitch = view.getPitch().add(Angle.fromRadians(pitchStep * pickDistanceFactor));
        pitch = pitch.degrees >= 0 ? (pitch.degrees <= 90 ? pitch : Angle.fromDegrees(90)) : Angle.ZERO;
        return pitch;
    }

    /**
     * Reset the view to an orbit view state if in first person mode (zoom = 0)
     *
     * @param view the orbit view to reset
     */
    protected void resetOrbitView(OrbitView view)
    {
        if (view.getZoom() > 0)   // already in orbit view mode
            return;

        // Find out where on the terrain the eye is looking at in the viewport center
        // TODO: if no terrain is found in the viewport center, iterate toward viewport bottom until it is found
        Vec4 centerPoint = computeSurfacePoint(view, view.getHeading(), view.getPitch());
        // Reset the orbit view center point heading, pitch and zoom
        if (centerPoint != null)
        {
            Vec4 eyePoint = view.getEyePoint();
            // Center pos on terrain surface
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);
            // Compute pitch and heading relative to center position
            Vec4 normal = wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(centerPosition.getLatitude(),
                centerPosition.getLongitude());
            Vec4 north = wwd.getModel().getGlobe().computeNorthPointingTangentAtLocation(centerPosition.getLatitude(),
                centerPosition.getLongitude());
            // Pitch
            view.setPitch(Angle.POS180.subtract(view.getForwardVector().angleBetween3(normal)));
            // Heading
            Vec4 perpendicular = view.getForwardVector().perpendicularTo3(normal);
            Angle heading = perpendicular.angleBetween3(north);
            double direction = Math.signum(-normal.cross3(north).dot3(perpendicular));
            view.setHeading(heading.multiply(direction));
            // Zoom
            view.setZoom(eyePoint.distanceTo3(centerPoint));
            // Center pos
            view.setCenterPosition(centerPosition);
        }
    }

    /**
     * Setup the view to a first person mode (zoom = 0)
     *
     * @param view the orbit view to set into a first person view.
     */
    protected void setupFirstPersonView(OrbitView view)
    {
        if (view.getZoom() == 0)  // already in first person mode
            return;

        Vec4 eyePoint = view.getEyePoint();
        // Center pos at eye pos
        Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(eyePoint);
        // Compute pitch and heading relative to center position
        Vec4 normal = wwd.getModel().getGlobe().computeSurfaceNormalAtLocation(centerPosition.getLatitude(),
            centerPosition.getLongitude());
        Vec4 north = wwd.getModel().getGlobe().computeNorthPointingTangentAtLocation(centerPosition.getLatitude(),
            centerPosition.getLongitude());
        // Pitch
        view.setPitch(Angle.POS180.subtract(view.getForwardVector().angleBetween3(normal)));
        // Heading
        Vec4 perpendicular = view.getForwardVector().perpendicularTo3(normal);
        Angle heading = perpendicular.angleBetween3(north);
        double direction = Math.signum(-normal.cross3(north).dot3(perpendicular));
        view.setHeading(heading.multiply(direction));
        // Zoom
        view.setZoom(0);
        // Center pos
        view.setCenterPosition(centerPosition);
    }

    /**
     * Find out where on the terrain surface the eye would be looking at with the given heading and pitch angles.
     *
     * @param view    the orbit view
     * @param heading heading direction clockwise from north.
     * @param pitch   view pitch angle from the surface normal at the center point.
     *
     * @return the terrain surface point the view would be looking at in the viewport center.
     */
    protected Vec4 computeSurfacePoint(OrbitView view, Angle heading, Angle pitch)
    {
        Globe globe = wwd.getModel().getGlobe();
        // Compute transform to be applied to north pointing Y so that it would point in the view direction
        // Move coordinate system to view center point
        Matrix transform = globe.computeSurfaceOrientationAtPosition(view.getCenterPosition());
        // Rotate so that the north pointing axes Y will point in the look at direction
        transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));
        transform = transform.multiply(Matrix.fromRotationX(Angle.NEG90.add(pitch)));
        // Compute forward vector
        Vec4 forward = Vec4.UNIT_Y.transformBy4(transform);
        // Return intersection with terrain
        Intersection[] intersections = wwd.getSceneController().getTerrain().intersect(
            new Line(view.getEyePoint(), forward));
        return (intersections != null && intersections.length != 0) ? intersections[0].getIntersectionPoint() : null;
    }
}
