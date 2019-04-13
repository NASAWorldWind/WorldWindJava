/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;

/**
 * @author Tom Gaskins
 * @version $Id: SceneController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface SceneController extends WWObject, Disposable
{
    /**
     * Indicates the scene controller's model. This returns <code>null</code> if the scene controller has no model.
     *
     * @return the scene controller's model, or <code>null</code> if the scene controller has no model.
     */
    Model getModel();

    /**
     * Specifies the scene controller's model. This method fires an {@link gov.nasa.worldwind.avlist.AVKey#MODEL}
     * property change event.
     *
     * @param model the scene controller's model.
     */
    void setModel(Model model);

    /**
     * Returns the current view. This method fires an {@link gov.nasa.worldwind.avlist.AVKey#VIEW} property change
     * event.
     *
     * @return the current view.
     */
    View getView();

    /**
     * Sets the current view.
     *
     * @param view the view.
     */
    void setView(View view);

    /**
     * Cause the window to regenerate the frame, including pick resolution.
     *
     * @return if greater than zero, the window should be automatically repainted again at the indicated number of
     *         milliseconds from this method's return.
     */
    int repaint();

    /**
     * Specifies the exaggeration to apply to elevation values of terrain and other displayed items.
     *
     * @param verticalExaggeration the vertical exaggeration to apply.
     */
    void setVerticalExaggeration(double verticalExaggeration);

    /**
     * Indicates the current vertical exaggeration.
     *
     * @return the current vertical exaggeration.
     */
    double getVerticalExaggeration();

    /**
     * Returns the list of picked objects at the current pick point. The returned list is computed during the most
     * recent call to repaint.
     *
     * @return the list of picked objects at the pick point, or null if no objects are currently picked.
     */
    PickedObjectList getPickedObjectList();

    /**
     * Returns the list of picked objects that intersect the current pick rectangle. The returned list is computed
     * during the most recent call to repaint.
     *
     * @return the list of picked objects intersecting the pick rectangle, or null if no objects are currently
     *         intersecting the rectangle.
     */
    PickedObjectList getObjectsInPickRectangle();

    /**
     * Returns the current average frames drawn per second. A frame is one repaint of the window and includes a pick
     * pass and a render pass.
     *
     * @return the current average number of frames drawn per second.
     */
    double getFramesPerSecond();

    /**
     * Returns the per-frame timestamp.
     *
     * @return the per-frame timestamp, in milliseconds.
     */
    double getFrameTime();

    /**
     * Specifies the current pick point in AWT screen coordinates, or <code>null</code> to indicate that there is no
     * pick point. Each frame, this scene controller determines which objects are drawn at the pick point and places
     * them in a PickedObjectList. This list can be accessed by calling {@link #getPickedObjectList()}.
     * <p>
     * If the pick point is <code>null</code>, this scene controller ignores the pick point and the list of objects
     * returned by getPickedObjectList is empty.
     *
     * @param pickPoint the current pick point, or <code>null</code>.
     */
    void setPickPoint(Point pickPoint);

    /**
     * Returns the current pick point in AWT screen coordinates.
     *
     * @return the current pick point, or <code>null</code> if no pick point is current.
     *
     * @see #setPickPoint(java.awt.Point)
     */
    Point getPickPoint();

    /**
     * Specifies the current pick rectangle in AWT screen coordinates, or <code>null</code> to indicate that there is no
     * pick rectangle. Each frame, this scene controller determines which objects intersect the pick rectangle and
     * places them in a PickedObjectList. This list can be accessed by calling {@link #getObjectsInPickRectangle()}.
     * <p>
     * If the pick rectangle is <code>null</code>, this scene controller ignores the pick rectangle and the list of
     * objects returned by getObjectsInPickRectangle is empty.
     *
     * @param pickRect the current pick rectangle, or <code>null</code>.
     */
    void setPickRectangle(Rectangle pickRect);

    /**
     * Returns the current pick rectangle in AWT screen coordinates.
     *
     * @return the current pick rectangle, or <code>null</code> if no pick rectangle is current.
     *
     * @see #setPickRectangle(java.awt.Rectangle)
     */
    Rectangle getPickRectangle();

    /**
     * Specifies whether all items under the cursor are identified during picking and within {@link
     * gov.nasa.worldwind.event.SelectEvent}s.
     *
     * @param tf true to identify all items under the cursor during picking, otherwise false.
     */
    void setDeepPickEnabled(boolean tf);

    /**
     * Indicates whether all items under the cursor are identified during picking and within {@link
     * gov.nasa.worldwind.event.SelectEvent}s.
     *
     * @return true if all items under the cursor are identified during picking, otherwise false.
     */
    boolean isDeepPickEnabled();

    /**
     * Specifies the GPU Resource cache to use.
     *
     * @param gpuResourceCache the texture cache.
     */
    void setGpuResourceCache(GpuResourceCache gpuResourceCache);

    /**
     * Returns this scene controller's GPU Resource cache.
     *
     * @return this scene controller's GPU Resource cache.
     */
    GpuResourceCache getGpuResourceCache();

    /**
     * Returns the current per-frame statistics.
     *
     * @return the current per-frame statistics.
     */
    Collection<PerformanceStatistic> getPerFrameStatistics();

    /**
     * Specifies the performance values to monitor. See {@link gov.nasa.worldwind.util.PerformanceStatistic} for the
     * available keys.
     *
     * @param keys the performance statistic keys to monitor.
     */
    void setPerFrameStatisticsKeys(Set<String> keys);

    /**
     * Returns the rendering exceptions accumulated by this SceneController during the last frame as a {@link
     * java.util.Collection} of {@link Throwable} objects.
     *
     * @return the Collection of accumulated rendering exceptions.
     */
    Collection<Throwable> getRenderingExceptions();

    /**
     * Returns the terrain geometry used to draw the most recent frame. The geometry spans only the area most recently
     * visible.
     *
     * @return the terrain geometry used to draw the most recent frame. May be null.
     */
    SectorGeometryList getTerrain();

    /**
     * Returns the current draw context.
     *
     * @return the current draw context.
     */
    DrawContext getDrawContext();

    /** Reinitializes the scene controller. */
    void reinitialize();

    /**
     * Returns the current screen credit controller.
     *
     * @return the current screen credit controller. May be null.
     *
     * @see #setScreenCreditController(gov.nasa.worldwind.render.ScreenCreditController)
     */
    ScreenCreditController getScreenCreditController();

    /**
     * Specifies the {@link gov.nasa.worldwind.render.ScreenCreditController} to use for displaying screen credits for
     * the model of this screen controller.
     *
     * @param screenCreditRenderer the screen credit controller. May be null, in which case screen credits are not
     *                             displayed.
     */
    void setScreenCreditController(ScreenCreditController screenCreditRenderer);

    /**
     * Returns the {@link GLRuntimeCapabilities} associated with this SceneController.
     *
     * @return this SceneController's associated GLRuntimeCapabilities.
     */
    GLRuntimeCapabilities getGLRuntimeCapabilities();

    /**
     * Sets the {@link GLRuntimeCapabilities} associated with this SceneController to the specified parameter.
     *
     * @param capabilities the GLRuntimeCapabilities to be associated with this SceneController.
     *
     * @throws IllegalArgumentException if the capabilities are null.
     */
    void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities);
//
//    GroupingFilter getGroupingFilter(String filterName);
//
//    void addGroupingFilter(String filterName, GroupingFilter filter);
//
//    void removeGroupingFilter(String filterName);
//
//    void removeAllGroupingFilters();

    /**
     * Returns the current clutter filter.
     *
     * @return the current clutter filter. May be null, in which case decluttering is not performed.
     */
    ClutterFilter getClutterFilter();

    /**
     * Specifies the clutter filter to use.
     *
     * @param clutterFilter the clutter filter to use. May be null to indicate no decluttering.
     */
    void setClutterFilter(ClutterFilter clutterFilter);
}
