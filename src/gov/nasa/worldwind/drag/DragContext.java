/*
 * Copyright (C) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.drag;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Provides information about mouse inputs and {@link WorldWindow} state for use in dragging operations.
 */
public class DragContext
{
    /**
     * In accordance with the AWT screen coordinates the top left point of the window is the origin.
     */
    protected Point point;
    /**
     * In accordance with the AWT screen coordinates the top left point of the window is the origin. This point is the
     * previous screen point.
     */
    protected Point previousPoint;
    /**
     * In accordance with the AWT screen coordinates the top left point of the window is the origin. This point refers
     * to the initial point of the drag event.
     */
    protected Point initialPoint;
    /**
     * The current {@link SceneController} of the {@link WorldWindow}.
     */
    protected SceneController sceneController;
    /**
     * The current {@link Globe} of the {@link WorldWindow}.
     */
    protected Globe globe;
    /**
     * The current{@link View} of the {@link WorldWindow}.
     */
    protected View view;
    /**
     * The current drag state, which can be one of the three following values:
     * {@link gov.nasa.worldwind.avlist.AVKey#DRAG_BEGIN}, {@link gov.nasa.worldwind.avlist.AVKey#DRAG_CHANGE},
     * {@link gov.nasa.worldwind.avlist.AVKey#DRAG_ENDED}.
     */
    protected String dragState;

    /**
     * Creates a new {@link DragContext} instance.
     */
    public DragContext()
    {
    }

    /**
     * Returns the current screen point with the origin at the top left corner of the window.
     *
     * @return the current screen point.
     */
    public Point getPoint()
    {
        return point;
    }

    /**
     * Set the {@link DragContext} current screen point.
     *
     * @param point the point to assign to the current screen point.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public void setPoint(Point point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.point = point;
    }

    /**
     * Returns the previous screen point with the origin at the top left corner of the window.
     *
     * @return the previous point.
     */
    public Point getPreviousPoint()
    {
        return previousPoint;
    }

    /**
     * Set the {@link DragContext} previous screen point.
     *
     * @param previousPoint the screen point to assign to the previous screen point.
     *
     * @throws IllegalArgumentException if the previousPoint is null.
     */
    public void setPreviousPoint(Point previousPoint)
    {
        if (previousPoint == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.previousPoint = previousPoint;
    }

    /**
     * Returns the initial screen point with the origin at the top left corner of the window. The initial point is the
     * screen point at the initiation of the drag event.
     *
     * @return the initial screen point.
     */
    public Point getInitialPoint()
    {
        return initialPoint;
    }

    /**
     * Set the {@link DragContext} initial screen point.
     *
     * @param initialPoint the screen point to assign to the initial screen point.
     *
     * @throws IllegalArgumentException if the initialPoint is null.
     */
    public void setInitialPoint(Point initialPoint)
    {
        if (initialPoint == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.initialPoint = initialPoint;
    }

    /**
     * Returns the current {@link SceneController} for this drag event.
     *
     * @return the current {@link SceneController}.
     */
    public SceneController getSceneController()
    {
        return sceneController;
    }

    /**
     * Set the {@link DragContext} {@link SceneController}.
     *
     * @param sceneController the {@link SceneController} to assign to the {@link DragContext}.
     *
     * @throws IllegalArgumentException if the scene controller is null.
     */
    public void setSceneController(SceneController sceneController)
    {
        if (sceneController == null)
        {
            String msg = Logging.getMessage("nullValue.SceneControllerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sceneController = sceneController;
    }

    /**
     * Returns the current {@link Globe} for this drag event.
     *
     * @return the current {@link Globe}.
     */
    public Globe getGlobe()
    {
        return globe;
    }

    /**
     * Set the {@link DragContext} {@link Globe}.
     *
     * @param globe the {@link Globe} to assign to the {@link DragContext}.
     *
     * @throws IllegalArgumentException if the globe is null.
     */
    public void setGlobe(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.globe = globe;
    }

    /**
     * Returns the current {@link View} for this drag event.
     *
     * @return the current {@link View}.
     */
    public View getView()
    {
        return view;
    }

    /**
     * Set the {@link DragContext} {@link View}.
     *
     * @param view the {@link View} to assign to the {@link DragContext}.
     *
     * @throws IllegalArgumentException if the view is null.
     */
    public void setView(View view)
    {
        if (view == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.view = view;
    }

    /**
     * Returns the current drag state for this drag event.
     *
     * @return the drag state.
     */
    public String getDragState()
    {
        return dragState;
    }

    /**
     * Set the {@link DragContext} drag state, which must be one of the following three states: {@link AVKey#DRAG_BEGIN}
     * , {@link AVKey#DRAG_CHANGE}, or {@link AVKey#DRAG_ENDED}.
     *
     * @param dragState the drag state to assign to the {@link DragContext}.
     *
     * @throws IllegalArgumentException if the drag state is null or not one of the three states defined for dragging.
     */
    public void setDragState(String dragState)
    {
        if (dragState == null)
        {
            String msg = Logging.getMessage("nullValue.DragStateIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!dragState.equals(AVKey.DRAG_BEGIN) && !dragState.equals(AVKey.DRAG_CHANGE)
            && !dragState.equals(AVKey.DRAG_ENDED))
        {
            String msg = Logging.getMessage("generic.UnknownDragState", dragState);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.dragState = dragState;
    }
}