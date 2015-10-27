/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;

/**
 * @author dcollins
 * @version $Id: OrbitView.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface OrbitView extends View
{
     /**
     * Returns whether the this <code>View</code> will detect collisions with other objects,
     * such as the surface geometry. If true, implementations may also automatically
     * resolve any detected collisions.
     *
     * @return <code>true</code> If this <code>View</code> will detect collisions; <code>false</code> otherwise.
     */
    boolean isDetectCollisions();

    /**
     * Sets whether or not this <code>View</code> will detect collisions with other objects,
     * such as the surface geometry. If <code>detectCollisions</code> is true, implementations may also automatically
     * resolve any detected collisions.
     *
     * @param detectCollisions If <code>true</code>, this <code>View</code> will resolve collisions; otherwise this
     *                          <code>View</code> will ignore collisions.
     */
    void setDetectCollisions(boolean detectCollisions);

    /**
     * Returns whether or not a collision has occurred since the last call to <code>hadCollisions</code>.
     * If {@link #isDetectCollisions} is false, collisions will not be detected and
     * <code>hadCollisions</code> will always return false.
     *
     * @return <code>true</code> if a collision has occurred since the last call; <code>false</code> otherwise.
     */
    boolean hadCollisions();

    /**
     * Get the center position of the OrbitView.  The center position is used as the point about which the
     * heading and pitch rotate.  It is defined by the intersection of a ray from the eye position through the
     * center of the viewport with the surface of the globe.
     * @return the center position.
     */
    Position getCenterPosition();

    /**
     * Sets the center position of the OrbitView. The center position is used as the point about which the
     * heading and pitch rotate.  It is defined by the intersection of a ray from the eye position through the
     * center of the viewport with the surface of the globe.
     * @param center
     */
    void setCenterPosition(Position center);

    /**
     * Get the zoom value for the OrbitView.  The zoom value is the distance between the eye
     * position and the center position.
     * @return the zoom value
     */
    double getZoom();

    /**
     * Set the zoom value for the OrbitVeiw. The zoom value is the distance between the eye
     * position and the center position.
     * @param zoom
     */
    void setZoom(double zoom);

    /**
     * Get the limits for this OrbitView.  OrbitView has state values that augment the state values of a {@link View}.
     * Specifically, zoom and center position.  {@link OrbitViewLimits} enables the limiting of those values in addition
     * the the derived {@link gov.nasa.worldwind.view.BasicViewPropertyLimits} state.
     *
     * @return The active view limits.
     */
    OrbitViewLimits getOrbitViewLimits();

    /**
     * Set the limits for this OrbitView.  OrbitView has state values that augment the state values of a {@link View}.
     * Specifically, zoom and center position.  {@link OrbitViewLimits} enables the limiting of those values in addition
     * the the derived {@link gov.nasa.worldwind.view.BasicViewPropertyLimits} state.
     * @param limits
     */
    void setOrbitViewLimits(OrbitViewLimits limits);

    /**
     * Implementations are expected to determines if the OrbitView can set the center of rotation for heading and pitch
     * changes to the viewport center intersection with the globe surface via a call to {@link #focusOnViewportCenter}.
     * @return true if the OrbitView implementation can focus on the viewport center.
     **/
    boolean canFocusOnViewportCenter();

    /**
     * Implementations are expected to set the center of rotation for heading and pitch at the intersection of a ray
     * originates at the eye, and passes through the center of the viewport with the globe surface.
     */
    void focusOnViewportCenter();

    /**
     * Stop any changes to the center position.
     */
    void stopMovementOnCenter();

    public static final String CENTER_STOPPED = "gov.nasa.worldwind.view.orbit.OrbitView.CenterStopped";
}
