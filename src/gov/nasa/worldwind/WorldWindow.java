/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.PerformanceStatistic;

import javax.media.opengl.GLContext;
import java.util.*;

/**
 * The top-level interface common to all toolkit-specific World Wind windows.
 *
 * @author Tom Gaskins
 * @version $Id: WorldWindow.java 2047 2014-06-06 22:48:33Z tgaskins $
 */
public interface WorldWindow extends AVList
{
    /**
     * Sets the model to display in this window. If <code>null</code> is specified for the model, the current model, if
     * any, is disassociated with the window.
     *
     * @param model the model to display. May be <code>null</code>.
     */
    void setModel(Model model);

    /**
     * Returns the window's current model.
     *
     * @return the window's current model.
     */
    Model getModel();

    /**
     * Sets the view to use when displaying this window's model. If <code>null</code> is specified for the view, the
     * current view, if any, is disassociated with the window.
     *
     * @param view the view to use to display this window's model. May be null.
     */
    void setView(View view);

    /**
     * Returns this window's current view.
     *
     * @return the window's current view.
     */
    View getView();

    /**
     * Sets the model to display in this window and the view used to display it. If <code>null</code> is specified for
     * the model, the current model, if any, is disassociated with the window. If <code>null</code> is specified for the
     * view, the current view, if any, is disassociated with the window.
     *
     * @param model the model to display. May be<code>null</code>.
     * @param view  the view to use to display this window's model. May be<code>null</code>.
     */
    void setModelAndView(Model model, View view);

    /**
     * Returns the scene controller associated with this instance.
     *
     * @return The scene controller associated with the instance, or <code>null</code> if no scene controller is
     *         associated.
     */
    SceneController getSceneController();

    /**
     * Specifies a new scene controller for the window. The caller is responsible for populating the new scene
     * controller with a {@link View}, {@link Model} and any desired per-frame statistics keys.
     *
     * @param sceneController the new scene controller.
     *
     * @see SceneController#setView(View)
     * @see SceneController#setModel(Model)
     * @see SceneController#setPerFrameStatisticsKeys(java.util.Set)
     */
    void setSceneController(SceneController sceneController);

    /**
     * Returns the input handler associated with this instance.
     *
     * @return The input handler associated with this instance, or <code>null</code> if no input handler is associated.
     */
    InputHandler getInputHandler();

    /**
     * Sets the input handler to use for this instance.
     *
     * @param inputHandler The input handler to use for this world window. May by <code>null</code> if <code>null</code>
     *                     is specified, the current input handler, if any, is disassociated with the world window.
     */
    void setInputHandler(InputHandler inputHandler);

    /**
     * Adds a rendering listener to this world window. Rendering listeners are called at key point during World Wind
     * drawing and provide applications the ability to participate or monitor rendering.
     *
     * @param listener The rendering listener to add to those notified of rendering events by this world window.
     */
    void addRenderingListener(RenderingListener listener);

    /**
     * Removes a specified rendering listener associated with this world window.
     *
     * @param listener The rendering listener to remove.
     */
    void removeRenderingListener(RenderingListener listener);

    /**
     * Adds a select listener to this world window. Select listeners are called when a selection is made by the user in
     * the world window. A selection is any operation that identifies a visible item.
     *
     * @param listener The select listener to add.
     */
    void addSelectListener(SelectListener listener);

    /**
     * Removes the specified select listener associated with this world window.
     *
     * @param listener The select listener to remove.
     */
    void removeSelectListener(SelectListener listener);

    /**
     * Adds a position listener to this world window. Position listeners are called when the cursor's position changes.
     * They identify the position of the cursor on the globe, or that the cursor is not on the globe.
     *
     * @param listener The position listener to add.
     */
    void addPositionListener(PositionListener listener);

    /**
     * Removes the specified position listener associated with this world window.
     *
     * @param listener The listener to remove.
     */
    void removePositionListener(PositionListener listener);

    /**
     * Causes a repaint event to be enqueued with the window system for this world window. The repaint will occur at the
     * window system's discretion, within the window system toolkit's event loop, and on the thread of that loop. This
     * is the preferred method for requesting a repaint of the world window.
     */
    void redraw();

    /**
     * Immediately repaints the world window without waiting for a window system repaint event. This is not the
     * preferred way to cause a repaint, but is provided for the rare cases that require it.
     */
    void redrawNow();

    /**
     * Returns the current latitude, longitude and altitude of the current cursor position, or <code>null</code> if the
     * cursor is not on the globe.
     *
     * @return The current position of the cursor, or <code>null</code> if the cursor is not positioned on the globe.
     */
    Position getCurrentPosition();

    /**
     * Returns the World Wind objects at the current cursor position. The list of objects under the cursor is determined
     * each time the world window is repainted. This method returns the list of objects determined when the most recent
     * repaint was performed.
     *
     * @return The list of objects at the cursor position, or <code>null</code> if no objects are under the cursor.
     */
    PickedObjectList getObjectsAtCurrentPosition();

    /**
     * Returns the World Wind objects intersecting the current selection box. The list of objects in the selection box
     * is determined each time the world window  is repainted. This method returns the list of objects determined when
     * the most recent repaint was performed.
     *
     * @return The list of objects intersecting the selection box, or <code>null</code> if no objects are in the box.
     */
    PickedObjectList getObjectsInSelectionBox();

    /**
     * Returns the GPU Resource used by this World Window. This method is for internal use only.
     * <p/>
     * Note: Applications do not need to interact with the GPU resource cache. It is self managed. Modifying it in any
     * way will cause significant problems such as excessive memory usage or application crashes. The only reason to use
     * the GPU resource cache is to request management of GPU resources within implementations of shapes or layers. And
     * then access should be only through the draw context only.
     *
     * @return The GPU Resource cache used by this World Window.
     */
    GpuResourceCache getGpuResourceCache();

    /**
     * Activates the per-frame performance statistic specified. Per-frame statistics measure values within a single
     * frame of rendering, such as number of tiles drawn to produce the frame.
     *
     * @param keys The statistics to activate.
     */
    void setPerFrameStatisticsKeys(Set<String> keys);

    /**
     * Returns the active per-frame performance statistics such as number of tiles drawn in the most recent frame.
     *
     * @return The keys and values of the active per-frame statistics.
     */
    Collection<PerformanceStatistic> getPerFrameStatistics(); // TODO: move the constants from AVKey to this interface.

    /**
     * Causes resources used by the World Window to be freed. The World Window cannot be used once this method is
     * called.
     */
    void shutdown();

    /**
     * Adds an exception listener to this world window. Exception listeners are called when an exception or other
     * critical event occurs during drawable initialization or during rendering.
     *
     * @param listener the The exception listener to add.
     */
    void addRenderingExceptionListener(RenderingExceptionListener listener);

    /**
     * Removes the specified rendering exception listener associated with this world window.
     *
     * @param listener The listener to remove.
     */
    void removeRenderingExceptionListener(RenderingExceptionListener listener);

    /**
     * Returns the {@link GLContext} associated with this <code>WorldWindow</code>.
     *
     * @return the <code>GLContext</code> associated with this window. May be null.
     */
    GLContext getContext();

    /**
     * Indicates whether the GPU resource cache is reinitialized when this window is reinitialized.
     *
     * @return <code>true</code> if reinitialization is enabled, otherwise <code>false</code>.
     */
    boolean isEnableGpuCacheReinitialization();

    /**
     * Specifies whether to reinitialize the GPU resource cache when this window is reinitialized. A value of
     * <code>true</code> indicates that the GPU resource cache this window is using should be cleared when its init()
     * method is called, typically when re-parented. Set this to <code>false</code> when this window is sharing context
     * with other windows and is likely to be re-parented. It prevents the flashing caused by clearing and
     * re-populating the GPU resource cache during re-parenting. The default value is <code>true</code>.
     *
     * @param enableGpuCacheReinitialization <code>true</code> to enable reinitialization, otherwise <code>false</code>.
     */
    void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization);
}
