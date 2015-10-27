/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.awt;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.geom.*;

import java.awt.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: ViewInputHandler.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ViewInputHandler
    extends KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener
{
    /**
     * Return the <code>WorldWindow</code> this ViewInputHandler is listening to for input events, and will modify in
     * response to those events
     *
     * @return the <code>WorldWindow</code> this ViewInputHandler is listening to, and will modify in response to
     * events.
     */
    WorldWindow getWorldWindow();

    /**
     * Sets the <code>WorldWindow</code> this ViewInputHandler should listen to for input events, and should modify in
     * response to those events. If the parameter <code>newWorldWindow</code> is null, then this ViewInputHandler
     * will do nothing.
     *
     * @param newWorldWindow the <code>WorldWindow</code> to listen on, and modify in response to events.
     */
    void setWorldWindow(WorldWindow newWorldWindow);

    /**
     * Returns the values that are used to transform raw input events into view movments.
     *
     * @return values that are be used to transform raw input into view movement.
     */
    ViewInputAttributes getAttributes();

    /**
     * Sets the values that will be used to transform raw input events into view movements. ViewInputAttributes
     * define a calibration value for each combination of device and action, and a general sensitivity value
     * for each device.
     *
     * @param attributes values that will be used to transform raw input into view movement.
     *
     * @throws IllegalArgumentException if <code>attributes</code> is null.
     *
     * @see ViewInputAttributes
     */
    void setAttributes(ViewInputAttributes attributes);

    /**
     * Returns whether the ViewInputHandler will smooth view movements in response to input events.
     *
     * @return true if the view will movements are smoothed; false otherwise.
     */
    boolean isEnableSmoothing();

    /**
     * Sets whether the ViewInputHandler should smooth view movements in response to input events. A value of true
     * will cause the ViewInputHandler to delegate decisions about whether to smooth a certain input event to its
     * {@link ViewInputAttributes}. A value of false will disable all smoothing.
     *
     * @param enable true to smooth view movements; false otherwise.
     */
    void setEnableSmoothing(boolean enable);

    /**
     * Returns whether the view's heading should stay the same unless explicitly changed.
     *
     * @return true if the view's heading will stay the same unless explicity changed; false otherwise.
     */
    boolean isLockHeading();

    /**
     * Sets whether the view's heading should stay the same unless explicitly changed. For example, moving forward
     * along a great arc would suggest a change in position and heading. If the heading had been locked, the
     * ViewInputHandler will move forward in a way that doesn't change the heading.
     *
     * @param lock true if the view's heading should stay the same unless explicity changed; false otherwise.
     */
    void setLockHeading(boolean lock);

    /**
     * Returns whether the view will stop when the WorldWindow looses focus.
     *
     * @return true if the view will stop when the WorldWindow looses focus; false otherwise.
     */
    boolean isStopOnFocusLost();

    /**
     * Sets whether the view should stop when the WorldWindow looses focus.
     *
     * @param stop true if the view should stop when the WorldWindow looses focus; false otherwise.
     */
    void setStopOnFocusLost(boolean stop);

    /**
     * Returns the <code>factor</code> that dampens view movement when the user pans drags the cursor in a way that could
     * cause an abrupt transition.
     *
     * @return factor dampening view movement when a mouse drag event would cause an abrupt transition.
     * @see #setDragSlopeFactor
     */
    double getDragSlopeFactor();

    /**
     * Sets the <code>factor</code> that dampens view movement when a mouse drag event would cause an abrupt
     * transition. The drag slope is the ratio of screen pixels to Cartesian distance moved, measured by the previous
     * and current mouse points. As drag slope gets larger, it becomes more difficult to operate the view. This
     * typically happens while dragging over and around the horizon, where movement of a few pixels can cause the view
     * to move many kilometers. This <code>factor</code> is the amount of damping applied to the view movement in such
     * cases. Setting <code>factor</code> to zero will disable this behavior, while setting <code>factor</code> to a
     * positive value may dampen the effects of mouse dragging.
     *
     * @param factor dampening view movement when a mouse drag event would cause an abrupt transition. Must be greater
     * than or equal to zero.
     *
     * @throws IllegalArgumentException if <code>factor</code> is less than zero.
     */
    void setDragSlopeFactor(double factor);

    /**
     * Compute the drag slope the given screen and world coordinates.  The drag slope is the ratio of
     * screen pixels to Cartesian distance moved, measured by the previous and current mouse points.
     *
     * @param point1 The previous mouse coordinate.
     * @param point2 The current mouse coordinate.
     * @param vec1 The first cartesian world space coordinate.
     * @param vec2 The second cartesion world space coordinate.
     * @return the ratio of
     * screen pixels to Cartesian distance moved.
     */
    double computeDragSlope(Point point1, Point point2, Vec4 vec1, Vec4 vec2);

    /**
     * Animate to the specified position.  The implementation is expected to animate the <code>View</code> to look
     * at the given position from the given elevation.
     *
     * @param lookAtPos The position to animate the view to look at.
     * @param elevation The elevation to look at the <code>position</code> from.
     */
    void goTo(Position lookAtPos, double elevation);

    /**
     * Stops any animations that are active in this <code>View</code>
     */
    void stopAnimators();

    /**
     * Determine if there are any animations active in the <code>View</code>.
     * @return true if there are active animations, false otherwise.
     */
    boolean isAnimating();

    /**
     * Add an {@link gov.nasa.worldwind.animation.Animator} to this <code>ViewInputHandler</code>.
     * This method does not start the {@link gov.nasa.worldwind.animation.Animator}.  Starting the
     * {@link gov.nasa.worldwind.animation.Animator} is the responsibility of the application.
     * This method is here primarily for use by the {@link gov.nasa.worldwind.View}.  Applications should call
     * {@link gov.nasa.worldwind.View#addAnimator(gov.nasa.worldwind.animation.Animator)} to add an animtion to the
     * view.
     *
     * @param animator the {@link gov.nasa.worldwind.animation.Animator} to be added
     */
    void addAnimator(Animator animator);

    /**
     * Implementations are expected to apply any changes to the {@link gov.nasa.worldwind.View} state prior to the View
     * setting the modelview matrix for rendering the current frame.
     */
    void apply();

    /**
     * Called just after the view applies its state and computes its internal transforms.
     */
    void viewApplied();
}
