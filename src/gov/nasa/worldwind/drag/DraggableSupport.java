/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.drag;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Utility functions which support dragging operations on objects implementing the {@link Movable} or {@link Movable2}
 * interface.
 */
public class DraggableSupport
{
    /**
     * The {@link DraggableSupport#computeRelativePoint(Line, Globe, SceneController, double)} method uses a numeric
     * search method to determine the coordinates of the desired position. The numeric solver will stop at a defined
     * threshold or step limit. The default threshold is provided here. This value is utilized when using the static
     * drag functions provided by this class.
     */
    public static final double DEFAULT_CONVERGENCE_THRESHOLD = 0.1;
    /**
     * The {@link DraggableSupport#computeRelativePoint(Line, Globe, SceneController, double)} method uses a numeric
     * search method to determine the coordinates of the desired position. The numeric solver will stop at a defined
     * threshold or step limit. The default step limit is provided here. This value is utilized when using the static
     * drag functions provided by this class.
     */
    public static final int DEFAULT_STEP_LIMIT = 20;
    /**
     * Initial drag operation offset in x and y screen coordinates, between the object reference position and the screen
     * point. Used for screen size constant drag operations.
     */
    protected Vec4 initialScreenPointOffset = null;
    /**
     * Initial drag operation cartesian coordinates of the objects reference position. Used for globe size constant drag
     * operations.
     */
    protected Vec4 initialEllipsoidalReferencePoint = null;
    /**
     * Initial drag operation cartesian coordinates of the initial screen point.
     */
    protected Vec4 initialEllipsoidalScreenPoint = null;
    /**
     * This instances step limit when using the solver to determine the position of objects using the
     * {@link WorldWind#RELATIVE_TO_GROUND} altitude mode. Increasing this value will increase solver runtime and may
     * cause the event loop to hang. If the solver exceeds the number of steps specified here, it will fall back to
     * using a position calculated by intersection with the ellipsoid.
     */
    protected int stepLimit = DEFAULT_STEP_LIMIT;
    /**
     * This instances convergence threshold for the solver used to determine the position of objects using the
     * {@link WorldWind#RELATIVE_TO_GROUND} altitude mode. Decreasing this value will increase solver runtime and may
     * cause the event loop to hang. If the solver does not converge to the threshold specified here, it will fall back
     * to using a position calculated by intersection with the ellipsoid.
     */
    protected double convergenceThreshold = DEFAULT_CONVERGENCE_THRESHOLD;
    /**
     * The object that will be subject to the drag operations. This object should implement {@link Movable} or {@link
     * Movable2}.
     */
    protected final Object dragObject;
    /**
     * The altitude mode of the object to be dragged.
     */
    protected int altitudeMode;

    /**
     * Provides persistence of initial values of a drag operation to increase dragging precision and provide better
     * dragging behavior.
     *
     * @param dragObject   the object to be dragged.
     * @param altitudeMode the altitude mode.
     *
     * @throws IllegalArgumentException if the object is null.
     */
    public DraggableSupport(Object dragObject, int altitudeMode)
    {
        if (dragObject == null)
        {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (altitudeMode != WorldWind.ABSOLUTE && altitudeMode != WorldWind.CLAMP_TO_GROUND &&
            altitudeMode != WorldWind.RELATIVE_TO_GROUND && altitudeMode != WorldWind.CONSTANT)
        {
            String msg = Logging.getMessage("generic.InvalidAltitudeMode", altitudeMode);
            Logging.logger().warning(msg);
        }

        this.dragObject = dragObject;
        this.altitudeMode = altitudeMode;
    }

    /**
     * Converts the screen position inputs to geographic movement information. Uses the information provided by the
     * {@link DragContext} object and attempts to move the object using the {@link Movable2} or {@link Movable}
     * interface. This method maintains a constant screen offset from the cursor to the reference point of the object
     * being dragged. It is suited for objects maintaining a constant screen size presentation like,
     * {@link gov.nasa.worldwindx.examples.symbology.TacticalSymbols} or
     * {@link gov.nasa.worldwind.render.PointPlacemark}.
     *
     * @param dragContext the current {@link DragContext} for this object.
     *
     * @throws IllegalArgumentException if the {@link DragContext} is null.
     */
    public void dragScreenSizeConstant(DragContext dragContext)
    {
        if (dragContext == null)
        {
            String msg = Logging.getMessage("nullValue.DragContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position referencePosition = this.getReferencePosition();
        if (referencePosition == null)
            return;

        if (dragContext.getDragState().equals(AVKey.DRAG_BEGIN))
        {
            this.initialScreenPointOffset = this.computeScreenOffsetFromReferencePosition(
                referencePosition,
                dragContext);
        }

        if (this.initialScreenPointOffset == null)
            return;

        double referenceAltitude = referencePosition.getAltitude();

        Vec4 currentPoint = new Vec4(
            dragContext.getPoint().getX(),
            dragContext.getPoint().getY()
        );

        // Apply the screen coordinate move to the current screen point
        Vec4 moveToScreenCoordinates = currentPoint.subtract3(this.initialScreenPointOffset);

        // Project the new screen point back through the globe to find a new reference position
        Line ray = dragContext.getView().computeRayFromScreenPoint(
            moveToScreenCoordinates.getX(),
            moveToScreenCoordinates.getY()
        );
        if (ray == null)
            return;

        Vec4 moveToGlobeCoordinates = this.computeGlobeIntersection(
            ray,
            referenceAltitude,
            true,
            dragContext.getGlobe(),
            dragContext.getSceneController());
        if (moveToGlobeCoordinates == null)
            return;

        Position moveTo = dragContext.getGlobe().computePositionFromPoint(moveToGlobeCoordinates);

        this.doMove(new Position(moveTo, referenceAltitude), dragContext.getGlobe());
    }

    /**
     * Converts the screen position inputs to geographic movement information. Uses the information provided by the
     * {@link DragContext} object and attempts to move the object using the {@link Movable2} or {@link Movable}
     * interface. This method maintains a constant geographic distance between the cursor and the reference point of the
     * object being dragged. It is suited for objects which maintain a constant model space or geographic size.
     *
     * @param dragContext the current {@link DragContext} for this object.
     *
     * @throws IllegalArgumentException if the {@link DragContext} is null.
     */
    public void dragGlobeSizeConstant(DragContext dragContext)
    {
        if (dragContext == null)
        {
            String msg = Logging.getMessage("nullValue.DragContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position referencePosition = this.getReferencePosition();
        if (referencePosition == null)
            return;

        if (dragContext.getDragState().equals(AVKey.DRAG_BEGIN))
        {
            this.initialEllipsoidalReferencePoint = dragContext.getGlobe()
                .computeEllipsoidalPointFromPosition(referencePosition);
            this.initialEllipsoidalScreenPoint = this.computeEllipsoidalPointFromScreen(
                dragContext,
                dragContext.getInitialPoint(),
                referencePosition.getAltitude(),
                false);
        }

        if (this.initialEllipsoidalReferencePoint == null || this.initialEllipsoidalScreenPoint == null)
            return;

        double referenceAltitude = referencePosition.getAltitude();

        Vec4 currentScreenPoint = new Vec4(
            dragContext.getPoint().getX(),
            dragContext.getPoint().getY()
        );
        Line ray = dragContext.getView()
            .computeRayFromScreenPoint(currentScreenPoint.getX(), currentScreenPoint.getY());
        if (ray == null)
            return;

        Vec4 currentPoint = this.computeGlobeIntersection(
            ray,
            referenceAltitude,
            false,
            dragContext.getGlobe(),
            dragContext.getSceneController());
        if (currentPoint == null)
            return;

        Position currentPosition = dragContext.getGlobe().computePositionFromPoint(currentPoint);
        if (currentPosition == null)
            return;

        Vec4 currentEllipsoidalPoint = dragContext.getGlobe().computeEllipsoidalPointFromPosition(currentPosition);
        if (currentEllipsoidalPoint == null)
            return;

        Vec4 rotationAxis = this.initialEllipsoidalScreenPoint.cross3(currentEllipsoidalPoint).normalize3();
        Angle rotationAngle = this.initialEllipsoidalScreenPoint.angleBetween3(currentEllipsoidalPoint);
        Matrix rotation = Matrix.fromAxisAngle(rotationAngle, rotationAxis);

        Vec4 dragObjectReferenceMoveToEllipsoidalPoint = this.initialEllipsoidalReferencePoint.transformBy3(rotation);
        Position moveToInterim = dragContext.getGlobe()
            .computePositionFromEllipsoidalPoint(dragObjectReferenceMoveToEllipsoidalPoint);
        if (moveToInterim == null)
            return;

        Position moveTo = new Position(moveToInterim, referenceAltitude);

        this.doMove(moveTo, dragContext.getGlobe());
    }

    /**
     * Returns the step limit used by the position solver method for objects using a screen size constant drag approach
     * and a {@link WorldWind#RELATIVE_TO_GROUND} altitude mode. If the solver exceeds this value it will utilize an
     * intersection with the ellipsoid at the specified altitude.
     *
     * @return the step limit.
     */
    public int getStepLimit()
    {
        return this.stepLimit;
    }

    /**
     * Sets the step limit to use for the position solver. The position solver is only used for screen size constant
     * objects with a {@link WorldWind#RELATIVE_TO_GROUND} altitude mode. The step limit is the maximum steps the solver
     * will attempt before using an intersection with the ellipsoid at the specified altitude. Set this value in
     * coordination with the convergence threshold.
     *
     * @param stepLimit the step limit to set for the solver method.
     */
    public void setStepLimit(int stepLimit)
    {
        this.stepLimit = stepLimit;
    }

    /**
     * Returns the convergence threshold used by the solver when an a screen size constant object using a
     * {@link WorldWind#RELATIVE_TO_GROUND} altitude mode needs to determine a position. When the solver finds an
     * altitude within the convergence threshold to the desired altitude, the solver will stop and return the cartesian
     * position.
     *
     * @return the convergence threshold.
     */
    public double getConvergenceThreshold()
    {
        return this.convergenceThreshold;
    }

    /**
     * Sets the convergence threshold for the screen size constant object using a {@link WorldWind#RELATIVE_TO_GROUND}
     * altitude mode. The solver will test each iterations solution altitude with the desired altitude and if it is
     * found to be within the convergence threshold, the cartesian solution will be returned. Set this value in
     * coordination with the step limit.
     *
     * @param convergenceThreshold the convergence threshold to use for the solver.
     */
    public void setConvergenceThreshold(double convergenceThreshold)
    {
        this.convergenceThreshold = convergenceThreshold;
    }

    /**
     * Returns the current altitude mode being used by the dragging calculations.
     *
     * @return the altitude mode.
     */
    public int getAltitudeMode()
    {
        return this.altitudeMode;
    }

    /**
     * Sets the altitude mode to be used during dragging calculations.
     *
     * @param altitudeMode the altitude mode to use for dragging calculations.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        if (altitudeMode != WorldWind.ABSOLUTE && altitudeMode != WorldWind.CLAMP_TO_GROUND &&
            altitudeMode != WorldWind.RELATIVE_TO_GROUND && altitudeMode != WorldWind.CONSTANT)
        {
            String msg = Logging.getMessage("generic.InvalidAltitudeMode", altitudeMode);
            Logging.logger().warning(msg);
        }
        this.altitudeMode = altitudeMode;
    }

    /**
     * Determines the cartesian coordinate of a screen point given the altitude mode.
     *
     * @param dragContext         the current {@link DragContext} of the dragging event.
     * @param screenPoint         the {@link Point} of the screen to determine the position.
     * @param altitude            the altitude in meters.
     * @param utilizeSearchMethod if the altitude mode is {@link WorldWind#RELATIVE_TO_GROUND}, this determines if the
     *                            search method will be used to determine the position, please see the {@link
     *                            DraggableSupport#computeRelativePoint(Line, Globe, SceneController, double)} for more
     *                            information.
     *
     * @return the cartesian coordinates using an ellipsoidal globe, or null if a position could not be determined.
     */
    protected Vec4 computeEllipsoidalPointFromScreen(DragContext dragContext, Point screenPoint, double altitude,
        boolean utilizeSearchMethod)
    {
        Line ray = dragContext.getView().computeRayFromScreenPoint(screenPoint.getX(), screenPoint.getY());
        Vec4 globePoint = this.computeGlobeIntersection(
            ray,
            altitude,
            utilizeSearchMethod,
            dragContext.getGlobe(),
            dragContext.getSceneController());
        if (globePoint == null)
            return null;

        Position screenPosition = dragContext.getGlobe().computePositionFromPoint(globePoint);
        if (screenPosition == null)
            return null;

        return dragContext.getGlobe().computeEllipsoidalPointFromPosition(screenPosition);
    }

    /**
     * Determines the offset in screen coordinates from the previous screen point ({@link DragContext#getInitialPoint()}
     * and the objects {@link Movable#getReferencePosition()} or {@link Movable2#getReferencePosition()} methods. If the
     * object doesn't implement either of the interfaces, or there is an error determining the offset, this function
     * will return null.
     *
     * @param dragObjectReferencePosition the {@link Movable} or {@link Movable2} reference position {@link Position}.
     * @param dragContext                 the current {@link DragContext} of this drag event.
     *
     * @return a {@link Vec4} containing the x and y offsets in screen coordinates from the reference position and the
     * previous screen point.
     */
    protected Vec4 computeScreenOffsetFromReferencePosition(Position dragObjectReferencePosition,
        DragContext dragContext)
    {
        Vec4 dragObjectPoint;

        if (dragContext.getGlobe() instanceof Globe2D)
        {
            dragObjectPoint = dragContext.getGlobe().computePointFromPosition(
                new Position(dragObjectReferencePosition, 0.0));
        }
        else
        {
            // If the altitude mode is ABSOLUTE, or not recognized as a standard WorldWind altitude mode, use the
            // ABSOLUTE method as the default
            if (this.altitudeMode == WorldWind.ABSOLUTE ||
                (this.altitudeMode != WorldWind.RELATIVE_TO_GROUND && this.altitudeMode != WorldWind.CLAMP_TO_GROUND
                    && this.altitudeMode != WorldWind.CONSTANT))
            {
                dragObjectPoint = dragContext.getGlobe().computePointFromPosition(dragObjectReferencePosition);
            }
            else // Should be any one of the remaining WorldWind altitude modes: CLAMP, RELATIVE, CONSTANT
            {
                dragObjectPoint = dragContext.getSceneController().getTerrain()
                    .getSurfacePoint(dragObjectReferencePosition);
            }
        }

        if (dragObjectPoint == null)
            return null;

        Vec4 dragObjectScreenPoint = dragContext.getView().project(dragObjectPoint);
        if (dragObjectScreenPoint == null)
            return null;

        Vec4 screenPointOffset = new Vec4(
            dragContext.getInitialPoint().getX() - dragObjectScreenPoint.getX(),
            dragContext.getInitialPoint().getY() - (
                dragContext.getView().getViewport().getHeight()
                    - dragObjectScreenPoint.getY() - 1.0)
        );

        return screenPointOffset;
    }

    /**
     * Extracts the reference {@link Position} from objects implementing {@link Movable} or {@link Movable2}. For
     * objects implementing both, it utilizes the {@link Movable2} interface method. If the object does not implement
     * the interfaces, null is returned.
     *
     * @return the reference {@link Movable#getReferencePosition()} or {@link Movable2#getReferencePosition()}, or null
     * if the object didn't implement either interface.
     */
    protected Position getReferencePosition()
    {
        if (this.dragObject instanceof Movable2)
            return ((Movable2) this.dragObject).getReferencePosition();
        else if (this.dragObject instanceof Movable)
            return ((Movable) this.dragObject).getReferencePosition();

        return null;
    }

    /**
     * Executes the {@link Movable2#moveTo(Globe, Position)} or {@link Movable#moveTo(Position)} methods with the
     * provided data. If the object implements both interfaces, the {@link Movable2} is used. If the object doesn't
     * implement either, it is ignored.
     *
     * @param movePosition the {@link Position} to provide to the {@link Movable2#moveTo} method.
     * @param globe        the globe reference, may be null if the object only implements the {@link Movable}
     *                     interface.
     */
    protected void doMove(Position movePosition, Globe globe)
    {
        if (this.dragObject instanceof Movable2)
        {
            if (globe == null)
            {
                String msg = Logging.getMessage("nullValue.GlobeIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            ((Movable2) this.dragObject).moveTo(globe, movePosition);
        }
        else if (this.dragObject instanceof Movable)
            ((Movable) this.dragObject).moveTo(movePosition);
    }

    /**
     * Computes the intersection of the provided {@link Line} with the {@link Globe} while accounting for the altitude
     * mode. If a {@link Globe2D} is specified, then the intersection is calculated using the globe objects method.
     *
     * @param ray             the {@link Line} to calculate the intersection of the {@link Globe}.
     * @param altitude        the altitude mode for the intersection calculation.
     * @param useSearchMethod if the altitude mode is {@link WorldWind#RELATIVE_TO_GROUND}, this determines if the
     *                        search method will be used to determine the position, please see the {@link
     *                        DraggableSupport#computeRelativePoint(Line, Globe, SceneController, double)} for more
     *                        information.
     * @param globe           the {@link Globe} to intersect.
     * @param sceneController if an altitude mode other than {@link WorldWind#ABSOLUTE} is specified, the {@link
     *                        SceneController} which will provide terrain information.
     *
     * @return the cartesian coordinates of the intersection based on the {@link Globe}s coordinate system or null if
     * the intersection couldn't be calculated.
     */
    protected Vec4
    computeGlobeIntersection(Line ray, double altitude, boolean useSearchMethod, Globe globe,
        SceneController sceneController)
    {
        Intersection[] intersections;

        if (globe instanceof Globe2D)
        {
            // Utilize the globe intersection method for a Globe2D as it best describes the appearance and the
            // terrain intersection method returns null when crossing the dateline on a Globe2D
            intersections = globe.intersect(ray, 0.0);
        }
        else if (this.altitudeMode == WorldWind.ABSOLUTE)
        {
            // Accounts for the object being visually placed on the surface in a Globe2D Globe
            intersections = globe.intersect(ray, altitude);
        }
        else if (this.altitudeMode == WorldWind.CLAMP_TO_GROUND || this.altitudeMode == WorldWind.CONSTANT)
        {
            intersections = sceneController.getTerrain().intersect(ray);
        }
        else if (this.altitudeMode == WorldWind.RELATIVE_TO_GROUND)
        {
            // If an object is RELATIVE_TO_GROUND but has an altitude close to 0.0, use CLAMP_TO_GROUND method
            if (altitude < 1.0)
            {
                intersections = sceneController.getTerrain().intersect(ray);
            }
            else
            {
                // When an object maintains a constant screen size independent of globe orientation or eye location,
                // the dragger attempts to determine the position by testing different points of the ray for a
                // matching altitude above elevation. The method is only used in objects maintain a constant screen
                // size as the effects are less pronounced in globe constant features.
                if (useSearchMethod)
                {
                    Vec4 intersectionPoint = this.computeRelativePoint(ray, globe, sceneController, altitude);
                    // In the event the computeRelativePoint fails with the numeric approach it falls back to a
                    // ellipsoidal intersection. Need to check if the result of that calculation was also null,
                    // indicating the screen point doesn't intersect with the globe.
                    if (intersectionPoint != null)
                        intersections = new Intersection[] {new Intersection(intersectionPoint, false)};
                    else
                        intersections = null;
                }
                else
                {
                    intersections = globe.intersect(ray, altitude);
                }
            }
        }
        else
        {
            // If the altitude mode isn't recognized, the ABSOLUTE determination method is used as a fallback/default
            intersections = globe.intersect(ray, altitude);
        }

        if ((intersections != null) && (intersections.length > 0))
            return intersections[0].getIntersectionPoint();
        else
            return null;
    }

    /**
     * Attempts to find a position with the altitude specified for objects which are {@link
     * WorldWind#RELATIVE_TO_GROUND}. Using the provided {@link Line}, conducts a bisectional search along the {@link
     * Line} for a {@link Position} which is within the {@code convergenceThreshold} of the requested {@code altitude}
     * provided. The {@code stepLimit} limits the number of bisections the function will attempt. If the search does not
     * find a position within the {@code stepLimit} or within the {@code convergenceThreshold} it will provide an
     * intersection with the ellipsoid at the provided altitude.
     *
     * @param ray             the {@link Line} from the eye point and direction in globe coordinates.
     * @param globe           the current {@link Globe}.
     * @param sceneController the current {@link SceneController}.
     * @param altitude        the target altitude.
     *
     * @return a {@link Vec4} of the point in globe coordinates.
     */
    protected Vec4 computeRelativePoint(Line ray, Globe globe, SceneController sceneController, double altitude)
    {
        // Calculate the intersection of ray with the terrain
        Intersection[] intersections = sceneController.getTerrain().intersect(ray);
        if (intersections != null)
        {
            Vec4 eye = ray.getOrigin();
            Vec4 surface = intersections[0].getIntersectionPoint();
            double maxDifference = eye.getLength3() - surface.getLength3();

            // Account for extremely zoomed out instances
            if (maxDifference > (5 * altitude))
            {
                double mixAmount = (5 * altitude) / maxDifference;
                eye = Vec4.mix3(mixAmount, surface, eye);
                // maxDifference = eye.getLength3() - surface.getLength3();
            }

            // False position approximation range reduction method. In testing, using this method decreased the average
            // convergence steps by 25%-30% but resulted in greater incidents of search failure, especially in
            // dynamic terrain change regions (mountains). Without the initial approximation, an average number of
            // solver steps was approximately 15.
            // double mixPoint = altitude / maxDifference;
            // double mixLow = Math.max(0.0, mixPoint - 0.1);
            // double mixHigh = Math.min(1.0, mixPoint + 0.1);
            double mixPoint = 0.5;
            double mixLow = 0.0;
            double mixHigh = 1.0;

            // now use a bracket method to find the best spot after 20 steps unless the error threshold is reached
            double pointAlt;
            Vec4 intersectionPoint;

            for (int i = 0; i < this.stepLimit; i++)
            {

                intersectionPoint = Vec4.mix3(mixPoint, surface, eye);
                Position pointPos = globe.computePositionFromPoint(intersectionPoint);
                pointAlt = globe.getElevation(pointPos.getLatitude(), pointPos.getLongitude());
                pointAlt = pointPos.getElevation() - pointAlt;

                if (Math.abs(pointAlt - altitude) < this.convergenceThreshold)
                {
                    return intersectionPoint;
                }

                if (altitude < pointAlt)
                    mixHigh = mixPoint;
                else
                    mixLow = mixPoint;

                mixPoint = (mixHigh + mixLow) / 2.0;
            }
        }

        intersections = globe.intersect(ray, altitude);
        if (intersections != null && (intersections.length > 0))
            return intersections[0].getIntersectionPoint();

        return null;
    }
}