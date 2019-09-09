/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * @author Tom Gaskins
 * @version $Id: DrawContext.java 2281 2014-08-29 23:08:04Z dcollins $
 */
public interface DrawContext extends WWObject, Disposable
{
    /**
     * Assigns this <code>DrawContext</code> a new <code>com.jogamp.opengl.GLContext</code>. May throw a
     * <code>NullPointerException</code> if <code>glContext</code> is null.
     *
     * @param glContext the new <code>com.jogamp.opengl.GLContext</code>
     *
     * @throws NullPointerException if glContext is null
     * @since 1.5
     */
    void setGLContext(GLContext glContext);

    /**
     * Retrieves this <code>DrawContext</code>s <code>com.jogamp.opengl.GLContext</code>. If this method returns null,
     * then there are potentially no active <code>GLContext</code>s and rendering should be aborted.
     *
     * @return this <code>DrawContext</code>s <code>com.jogamp.opengl.GLContext</code>.
     *
     * @since 1.5
     */
    GLContext getGLContext();

    /**
     * Retrieves the current <code>com.jogamp.opengl.GL</code>. A <code>GL</code> or <code>GLU</code> is required for
     * all graphical rendering in WorldWind.
     *
     * @return the current <code>GL</code> if available, null otherwise
     *
     * @since 1.5
     */
    GL getGL();

    /**
     * Retrieves the current <code>com.jogamp.opengl.glu.GLU</code>. A <code>GLU</code> or <code>GL</code> is required
     * for all graphical rendering in WorldWind.
     *
     * @return the current <code>GLU</code> if available, null otherwise
     *
     * @since 1.5
     */
    GLU getGLU();

    /**
     * Retrieves the current <code>com.jogamp.opengl.GLDrawable</code>. A <code>GLDrawable</code> can be used to create
     * a <code>GLContext</code>, which can then be used for rendering.
     *
     * @return the current <code>GLDrawable</code>, null if none available
     *
     * @since 1.5
     */
    GLDrawable getGLDrawable();

    /**
     * Indicates the width in pixels of the drawing target associated with this <code>DrawContext</code>. The returned
     * width is potentially different from the GL viewport width. If this DrawContext is associated with a
     * <code>GLJPanel</code>, the returned width is the power-of-two ceiling of the viewport, and is therefore almost
     * always larger than the GL viewport width.
     * <p>
     * The GL viewport dimensions can be accessed by calling <code>getView().getViewport()</code> on this
     * <code>DrawContext</code>.
     *
     * @return the width of the drawing target associated with this <code>DrawContext</code>, in pixels.
     */
    int getDrawableWidth();

    /**
     * Indicates the height in pixels of the drawing target associated with this <code>DrawContext</code>. The returned
     * height is potentially different from the GL viewport height. If this DrawContext is associated with a
     * <code>GLJPanel</code>, the returned height is the power-of-two ceiling of the viewport, and is therefore almost
     * always larger than the GL viewport height.
     * <p>
     * The GL viewport dimensions can be accessed by calling <code>getView().getViewport()</code> on this
     * <code>DrawContext</code>.
     *
     * @return the height of the drawing target associated with this <code>DrawContext</code>, in pixels.
     */
    int getDrawableHeight();

    /**
     * Returns the {@link GLRuntimeCapabilities} associated with this DrawContext.
     *
     * @return this DrawContext's associated GLRuntimeCapabilities.
     */
    GLRuntimeCapabilities getGLRuntimeCapabilities();

    /**
     * Sets the {@link GLRuntimeCapabilities} associated with this DrawContext to the specified parameter.
     *
     * @param capabilities the GLRuntimeCapabilities to be associated with this DrawContext.
     *
     * @throws IllegalArgumentException if the capabilities are null.
     */
    void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities);

    /**
     * Initializes this <code>DrawContext</code>. This method should be called at the beginning of each frame to prepare
     * the <code>DrawContext</code> for the coming render pass.
     *
     * @param glContext the <code>com.jogamp.opengl.GLContext</code> to use for this render pass
     *
     * @since 1.5
     */
    void initialize(GLContext glContext);

    /**
     * Assigns a new <code>View</code>. Some layers cannot function properly with a null <code>View</code>. It is
     * recommended that the <code>View</code> is never set to null during a normal render pass.
     *
     * @param view the enw <code>View</code>
     *
     * @since 1.5
     */
    void setView(View view);

    /**
     * Retrieves the current <code>View</code>, which may be null.
     *
     * @return the current <code>View</code>, which may be null
     *
     * @since 1.5
     */
    View getView();

    /**
     * Assign a new <code>Model</code>. Some layers cannot function properly with a null <code>Model</code>. It is
     * recommended that the <code>Model</code> is never set to null during a normal render pass.
     *
     * @param model the new <code>Model</code>
     *
     * @since 1.5
     */
    void setModel(Model model);

    /**
     * Retrieves the current <code>Model</code>, which may be null.
     *
     * @return the current <code>Model</code>, which may be null
     *
     * @since 1.5
     */
    Model getModel();

    /**
     * Retrieves the current <code>Globe</code>, which may be null.
     *
     * @return the current <code>Globe</code>, which may be null
     *
     * @since 1.5
     */
    Globe getGlobe();

    /**
     * Retrieves a list containing all the current layers. No guarantee is made about the order of the layers.
     *
     * @return a <code>LayerList</code> containing all the current layers
     *
     * @since 1.5
     */
    LayerList getLayers();

    /**
     * Retrieves a <code>Sector</code> which is at least as large as the current visible sector. The value returned is
     * the value passed to <code>SetVisibleSector</code>. This method may return null.
     *
     * @return a <code>Sector</code> at least the size of the current visible sector, null if unavailable
     *
     * @since 1.5
     */
    Sector getVisibleSector();

    /**
     * Sets the visible <code>Sector</code>. The new visible sector must completely encompass the Sector which is
     * visible on the display.
     *
     * @param s the new visible <code>Sector</code>
     *
     * @since 1.5
     */
    void setVisibleSector(Sector s);

    /**
     * Sets the vertical exaggeration. Vertical exaggeration affects the appearance of areas with varied elevation. A
     * vertical exaggeration of zero creates a surface which exactly fits the shape of the underlying
     * <code>Globe</code>. A vertical exaggeration of 3 will create mountains and valleys which are three times as
     * high/deep as they really are.
     *
     * @param verticalExaggeration the new vertical exaggeration.
     *
     * @since 1.5
     */
    void setVerticalExaggeration(double verticalExaggeration);

    /**
     * Retrieves the current vertical exaggeration. Vertical exaggeration affects the appearance of areas with varied
     * elevation. A vertical exaggeration of zero creates a surface which exactly fits the shape of the underlying
     * <code>Globe</code>. A vertical exaggeration of 3 will create mountains and valleys which are three times as
     * high/deep as they really are.
     *
     * @return the current vertical exaggeration
     *
     * @since 1.5
     */
    double getVerticalExaggeration();

    /**
     * Retrieves a list of all the sectors rendered so far this frame.
     *
     * @return a <code>SectorGeometryList</code> containing every <code>SectorGeometry</code> rendered so far this
     *         render pass.
     *
     * @since 1.5
     */
    SectorGeometryList getSurfaceGeometry();

    /**
     * Returns the list of objects at the pick point during the most recent pick traversal. The returned list is empty
     * if nothing is at the pick point, or if the pick point is <code>null</code>.
     *
     * @return the list of picked objects.
     */
    PickedObjectList getPickedObjects();

    /**
     * Adds a list of picked objects to the current list of objects at the pick point. This list can be accessed by
     * calling {@link #getPickedObjects()}.
     *
     * @param pickedObjects the list to add.
     *
     * @throws IllegalArgumentException if pickedObjects is <code>null</code>.
     */
    void addPickedObjects(PickedObjectList pickedObjects);

    /**
     * Adds the specified picked object to the list of objects at the pick point. This list can be accessed by calling
     * {@link #getPickedObjects()}.
     *
     * @param pickedObject the object to add.
     *
     * @throws IllegalArgumentException if pickedObject is <code>null</code>.
     */
    void addPickedObject(PickedObject pickedObject);

    /**
     * Returns the list of objects intersecting the pick rectangle during the most recent pick traversal. The returned
     * list is empty if nothing intersects the pick rectangle, or if the pick rectangle is <code>null</code>.
     *
     * @return the list of picked objects.
     */
    PickedObjectList getObjectsInPickRectangle();

    /**
     * Adds the specified picked object to the current list of objects intersecting the pick rectangle. This list can be
     * accessed by calling {@link #getObjectsInPickRectangle()}.
     *
     * @param pickedObject the object to add.
     *
     * @throws IllegalArgumentException if pickedObject is <code>null</code>.
     */
    void addObjectInPickRectangle(PickedObject pickedObject);

    /**
     * Returns a unique color to serve as a pick identifier during picking. This allocates a single unique color from
     * the pick color address space and returns that color. The color's RGB components represent an address to the pick
     * color code.
     *
     * @return a unique pick color.
     */
    Color getUniquePickColor();

    /**
     * Returns a range of unique colors to serve as pick identifiers during picking. This allocates <code>count</code>
     * unique colors from the pick color address space and returns the first color in the range. The first color's RGB
     * components represent an address to the beginning of a sequential range of pick color codes. This method is
     * similar to calling the no-argument {@link #getUniquePickColor} <code>count</code> times, but guarantees a
     * contiguous range of color codes and is more efficient when <code>count</code> is large.
     * <p>
     * The number of pick colors is limited to a finite address space. This method returns null when there are fewer
     * than <code>count</code> remaining unique colors in the pick color address space, or when <code>count</code> is
     * less than 1.
     *
     * @param count the number of unique colors to allocate.
     *
     * @return the first unique pick color if there are sufficient unique colors remaining and <code>count</code> is
     *         greater than 0, otherwise null.
     */
    Color getUniquePickColorRange(int count);

    /**
     * Returns the WorldWindow's background color.
     *
     * @return the WorldWindow's background color.
     */
    Color getClearColor();

    /**
     * Returns the framebuffer RGB color for a point in AWT screen coordinates, formatted as a pick color code. The red,
     * green, and blue components are each stored as an 8-bit unsigned integer, and packed into bits 0-23 of the
     * returned integer as follows: bits 16-23 are red, bits 8-15 are green, and bits 0-7 are blue. This format is
     * consistent with the RGB integers used to create the pick colors in getUniquePickColor.
     * <p>
     * This returns 0 if the point contains the clear color, or is outside this draw context's drawable area.
     *
     * @param point the point to return a color for, in AWT screen coordinates.
     *
     * @return the RGB color corresponding to the specified point.
     *
     * @throws IllegalArgumentException if the point is <code>null</code>.
     */
    int getPickColorAtPoint(Point point);

    /**
     * Returns an array of the unique framebuffer RGB colors within a rectangle in AWT screen coordinates, formatted as
     * pick color codes. The red, green, and blue components are each stored as an 8-bit unsigned integer, and packed
     * into bits 0-23 of the returned integers as follows: bits 16-23 are red, bits 8-15 are green, and bits 0-7 are
     * blue. This format is consistent with the RGB integers used to create the pick colors in getUniquePickColor.
     * <p>
     * The returned array contains one entry for each unique color. Points in the rectangle that contain the clear color
     * or are outside this draw context's drawable area are ignored. This returns <code>null</code> if the specified
     * rectangle is empty, or contains only the clear color.
     * <p>
     * The minAndMaxColorCodes parameter limits the unique colors that this method returns to the specified range. The
     * minimum color must be stored in array index 0, and the maximum color must be stored in array index 1. These
     * values can be used to specify a small range of colors relative to the framebuffer contents, effectively culling
     * the colors that must be considered by this method and the caller. When specified, these integers must be
     * formatted exactly as the integers this method returns.
     *
     * @param rectangle           the rectangle to return unique colors for, in AWT screen coordinates.
     * @param minAndMaxColorCodes an two element array representing the minimum and maximum RGB colors to return. May be
     *                            <code>null</code> to specify that all color codes must be returned.
     *
     * @return the unique RGB colors corresponding to the specified rectangle, or <code>null</code> if the rectangle is
     *         empty or the rectangle contains only the clear color.
     *
     * @throws IllegalArgumentException if the rectangle is <code>null</code>.
     */
    int[] getPickColorsInRectangle(Rectangle rectangle, int[] minAndMaxColorCodes);

    /** Specifies that the scene controller is beginning its pick traversal. */
    void enablePickingMode();

    /**
     * Indicates whether the scene controller is currently picking.
     *
     * @return true if the scene controller is picking, otherwise false
     */
    boolean isPickingMode();

    /** Specifies that the scene controller has ended its pick traversal. */
    void disablePickingMode();

    /**
     * Specifies whether all items under the cursor are picked.
     *
     * @param tf true to pick all objects under the cursor
     */
    void setDeepPickingEnabled(boolean tf);

    /**
     * Indicates whether all items under cursor are picked.
     *
     * @return true if all items under the cursor are picked, otherwise false
     */
    boolean isDeepPickingEnabled();

    /**
     * Adds an {@link gov.nasa.worldwind.render.OrderedRenderable} to the draw context's ordered renderable list.
     *
     * @param orderedRenderable the ordered renderable to add.
     *
     * @throws IllegalArgumentException if the ordered renderable is null.
     */
    void addOrderedRenderable(OrderedRenderable orderedRenderable);

    /**
     * Adds an {@link gov.nasa.worldwind.render.OrderedRenderable} to the draw context's ordered renderable list,
     * optionally indicating that the draw context should treat the specified ordered renderable as behind other ordered
     * renderables. If <code>isBehind</code> is <code>true</code>, the draw context treats the specified ordered
     * renderable as though it is behind all other ordered renderables and ignores the ordered renderable's eye
     * distance. If multiple ordered renderables are added with <code>isBehind</code> specified as <code>true</code>,
     * those ordered renderables are drawn according to the order in which they are added.
     *
     * @param orderedRenderable the ordered renderable to add.
     * @param isBehind          <code>true</code> to specify that the ordered renderable is behind all other ordered
     *                          renderables, or <code>false</code> to interpret the ordered renderable according to its
     *                          eye distance.
     */
    void addOrderedRenderable(OrderedRenderable orderedRenderable, boolean isBehind);

    /**
     * Adds an {@link gov.nasa.worldwind.render.OrderedRenderable} to the draw context's ordered surface renderable
     * queue. This queue is populated during layer rendering with objects to render on the terrain surface, and is
     * processed immediately after layer rendering.
     *
     * @param orderedRenderable the ordered renderable to add.
     */
    void addOrderedSurfaceRenderable(OrderedRenderable orderedRenderable);

    /**
     * Returns the draw context's ordered surface renderable queue. This queue is populated during layer rendering with
     * objects to render on the terrain surface, and is processed immediately after layer rendering.
     *
     * @return the draw context's ordered surface renderable queue.
     */
    Queue<OrderedRenderable> getOrderedSurfaceRenderables();

    /**
     * Draws a quadrilateral using the current OpenGL state. The quadrilateral points are (0, 0), (1, 0), (1, 1), (0,
     * 1).
     */
    void drawUnitQuad();

    /**
     * Draws a quadrilateral using the current OpenGL state and specified texture coordinates. The quadrilateral points
     * are (0, 0), (1, 0), (1, 1), (0, 1).
     *
     * @param texCoords texture coordinates to assign to each quadrilateral vertex.
     *
     * @throws NullPointerException if the texture coordinate reference is null.
     */
    void drawUnitQuad(TextureCoords texCoords);

    /**
     * Draws a quadrilateral outline using the current OpenGL state. The quadrilateral points are (0, 0), (1, 0), (1,
     * 1), (0, 1).
     */
    void drawUnitQuadOutline();

    /**
     * Specifies the current surface geometry.
     *
     * @param surfaceGeometry the surface geometry to make current. May be null, indicating no surface geometry.
     */
    void setSurfaceGeometry(SectorGeometryList surfaceGeometry);

    /**
     * Computes a location's Cartesian point on the currently visible terrain.
     *
     * @param latitude  the location's latitude.
     * @param longitude the location's longitude.
     *
     * @return the location's corresponding Cartesian point, or null if the location is not currently visible.
     */
    Vec4 getPointOnTerrain(Angle latitude, Angle longitude);

    /**
     * Returns this draw context's surface tile renderer.
     *
     * @return this draw context's surface tile renderer.s
     */
    SurfaceTileRenderer getGeographicSurfaceTileRenderer();

    /**
     * Returns the current pick point in AWT screen coordinates.
     *
     * @return the current pick point, or <code>null</code> if no pick point is available.
     *
     * @see #setPickPoint(java.awt.Point)
     */
    Point getPickPoint();

    /**
     * Specifies the current pick point in AWT screen coordinates, or <code>null</code> to indicate that there is no
     * pick point. During each pick traversal, layers determine if their contents are drawn at the pick point. If so,
     * layers add each unique picked object to a PickedObjectList on this draw context by calling {@link
     * #addPickedObject(gov.nasa.worldwind.pick.PickedObject)}. This list can be accessed by calling {@link
     * #getPickedObjects()}.
     * <p>
     * If the pick point is <code>null</code>, the pick point is ignored during each pick traversal, and the list of
     * objects returned by getPickedObjects is empty.
     *
     * @param pickPoint the current pick point, or <code>null</code> to specify that there is no pick point.
     */
    void setPickPoint(Point pickPoint);

    /**
     * Returns the current pick rectangle in AWT screen coordinates.
     *
     * @return the current pick rectangle, or <code>null</code> if no pick rectangle is current.
     *
     * @see #setPickRectangle(java.awt.Rectangle)
     */
    Rectangle getPickRectangle();

    /**
     * Specifies the current pick rectangle in AWT screen coordinates, or <code>null</code> to indicate that there is no
     * pick rectangle. During each pick traversal, layers determine if their contents intersect the pick rectangle. If
     * so, layers add each unique picked object to a PickedObjectList on this draw context by calling {@link
     * #addObjectInPickRectangle(gov.nasa.worldwind.pick.PickedObject)}. This is list can be accessed by calling {@link
     * #getObjectsInPickRectangle()}.
     * <p>
     * If the pick rectangle is <code>null</code>, the pick rectangle is ignored during each pick traversal, and the
     * list of objects returned by getObjectsInPickRectangle is empty.
     *
     * @param pickRect the current pick rectangle, or <code>null</code> to specify that there is no pick rectangle.
     */
    void setPickRectangle(Rectangle pickRect);

    /**
     * Returns the GPU resource cache for this draw context. This method returns the same value as {@link
     * #getGpuResourceCache()}.
     *
     * @return the GPU resource cache for this draw context.
     *
     * @see #getGpuResourceCache()
     */
    GpuResourceCache getTextureCache();

    /**
     * Returns the GPU resource cache used by this draw context.
     *
     * @return the GPU resource cache used by this draw context.
     */
    GpuResourceCache getGpuResourceCache();

    /**
     * Specifies the GPU resource cache for this draw context.
     *
     * @param gpuResourceCache the GPU resource cache for this draw context.
     */
    void setGpuResourceCache(GpuResourceCache gpuResourceCache);

    /**
     * Returns the performance statistics that are gathered during each frame.
     *
     * @return the performance statistics that are gathered during each frame.
     */
    Collection<PerformanceStatistic> getPerFrameStatistics();

    /**
     * Specifies the performance statistics that are monitored during each frame. Both arguments to this method may be
     * null. If either is null then statistics are not gathered.
     *
     * @param statKeys the keys identifying the statistics to monitor.
     * @param stats    a list in which the statistics are placed as they're monitored.
     */
    void setPerFrameStatisticsKeys(Set<String> statKeys, Collection<PerformanceStatistic> stats);

    /**
     * Specifies a performance statistic that's assigned for each frame. Use this method to update the value of a
     * specific statistic.
     *
     * @param key         the key identifying the statistic to monitor.
     * @param displayName the name to use when displaying this statistic.
     * @param statistic   the statistic's value. May be null.
     *
     * @throws IllegalArgumentException if either the key or display name are null.
     * @see #setPerFrameStatistics(java.util.Collection)
     */
    void setPerFrameStatistic(String key, String displayName, Object statistic);

    /**
     * Specifies a collection of per-frame performance statistics. Use this method to update a group of statistics.
     *
     * @param stats the statistic keys and their associated values.
     *
     * @throws IllegalArgumentException if the list is null.
     * @see #setPerFrameStatistic(String, String, Object)
     */
    void setPerFrameStatistics(Collection<PerformanceStatistic> stats);

    /**
     * Indicates the statistics that are monitored for each frame.
     *
     * @return the keys of the statistics monitored for each frame.
     */
    Set<String> getPerFrameStatisticsKeys();

    /**
     * Indicates the screen-coordinate point of the current viewport's center.
     *
     * @return the screen-coordinate point of the current viewport's center.
     */
    Point getViewportCenterScreenPoint();

    /**
     * Specifies the screen-coordinate point of the current viewport's center.
     *
     * @param viewportCenterPoint the screen-coordinate point of the current viewport's center.
     */
    void setViewportCenterScreenPoint(Point viewportCenterPoint);

    /**
     * Indicates the geographic coordinates of the point on the terrain at the current viewport's center.
     *
     * @return the geographic coordinates of the current viewport's center. Returns null if the globe's surface is not
     *         under the viewport's center point.
     */
    Position getViewportCenterPosition();

    /**
     * Specifies the geographic coordinates of the point on the terrain at the current viewport's center.
     *
     * @param viewportCenterPosition the geographic coordinates of the current viewport's center. May be null.
     */
    void setViewportCenterPosition(Position viewportCenterPosition);

    /**
     * Returns this draw context's text-renderer cache.
     *
     * @return this context's text renderer cache.
     */
    TextRendererCache getTextRendererCache();

    /**
     * Specifies this context's text renderer cache.
     *
     * @param textRendererCache the context's text renderer cache.
     *
     * @throws IllegalArgumentException if the cache is null.
     */
    void setTextRendererCache(TextRendererCache textRendererCache);

    /**
     * Returns the draw context's annotation renderer, typically used by annotations that are not contained in an {@link
     * AnnotationLayer}.
     *
     * @return the annotation renderer.
     */
    AnnotationRenderer getAnnotationRenderer();

    /**
     * Since {@link Annotation}s are {@link Renderable}s, they can be exist outside an {@link AnnotationLayer}, in which
     * case they are responsible for rendering themselves. The draw context's annotation renderer provides an active
     * renderer for that purpose.
     *
     * @param annotationRenderer the new annotation renderer for the draw context.
     *
     * @throws IllegalArgumentException if <code>annotationRenderer</code> is null;
     */
    void setAnnotationRenderer(AnnotationRenderer annotationRenderer);

    /**
     * Returns the time stamp corresponding to the beginning of a pre-render, pick, render sequence. The stamp remains
     * constant across these three operations so that called objects may avoid recomputing the same values during each
     * of the calls in the sequence.
     *
     * @return the frame time stamp. See {@link System#currentTimeMillis()} for its numerical meaning.
     */
    long getFrameTimeStamp();

    /**
     * Specifies the time stamp corresponding to the beginning of a pre-render, pick, render sequence. The stamp must
     * remain constant across these three operations so that called objects may avoid recomputing the same values during
     * each of the calls in the sequence.
     *
     * @param frameTimeStamp the frame time stamp. See {@link System#currentTimeMillis()} for its numerical meaning.
     */
    void setFrameTimeStamp(long frameTimeStamp);

    /**
     * Returns the visible sectors at one of several specified resolutions within a specified search sector. Several
     * sectors resolutions may be specified along with a time limit. The best resolution that can be determined within
     * the time limit is returned.
     * <p>
     * Adherence to the time limit is not precise. The limit is checked only between full searches at each resolution.
     * The search may take more than the specified time, but will terminate if no time is left before starting a
     * higher-resolution search.
     *
     * @param resolutions  the resolutions of the sectors to return, in latitude.
     * @param timeLimit    the amount of time, in milliseconds, to allow for searching.
     * @param searchSector the sector to decompose into visible sectors.
     *
     * @return the visible sectors at the best resolution achievable given the time limit. The actual resolution can be
     *         determined by examining the delta-latitude value of any of the returned sectors.
     *
     * @throws IllegalArgumentException if the resolutions array or the search sector is null.
     */
    List<Sector> getVisibleSectors(double[] resolutions, long timeLimit, Sector searchSector);

    /**
     * Sets the current-layer field to the specified layer or null. The field is informative only and enables layer
     * contents to determine their containing layer.
     *
     * @param layer the current layer or null.
     */
    void setCurrentLayer(Layer layer);

    /**
     * Returns the current-layer. The field is informative only and enables layer contents to determine their containing
     * layer.
     *
     * @return the current layer, or null if no layer is current.
     */
    Layer getCurrentLayer();

    /**
     * Adds a screen-credit icon to display.
     *
     * @param credit the screen credit to display.
     *
     * @throws IllegalArgumentException if the credit is null.
     */
    void addScreenCredit(ScreenCredit credit);

    /**
     * Returns the screen credits currently held and displayed by this draw context.
     *
     * @return the screen credits currently held by this draw context.
     */
    Map<ScreenCredit, Long> getScreenCredits();

    /**
     * Indicates whether a new frame should be generated by the {@link gov.nasa.worldwind.SceneController}.
     *
     * @return the delay in milliseconds if a redraw has been requested, otherwise 0.
     */
    int getRedrawRequested();

    /**
     * Requests that a new frame should be generated by the {@link gov.nasa.worldwind.SceneController}.
     *
     * @param redrawRequested a delay in milliseconds if a redraw is requested, otherwise 0.
     */
    void setRedrawRequested(int redrawRequested);

    /**
     * Gets the FrustumList containing all the current Pick Frustums
     *
     * @return FrustumList of Pick Frustums
     */
    PickPointFrustumList getPickFrustums();

    /**
     * Set the size (in pixels) of the pick point frustum at the near plane.
     *
     * @param dim dimension of pick point frustum
     */
    void setPickPointFrustumDimension(Dimension dim);

    /**
     * Gets the dimension of the current Pick Point Frustum
     *
     * @return the dimension of the current Pick Point Frustum
     */
    Dimension getPickPointFrustumDimension();

    /**
     * Creates a frustum around the current pick point and adds it to the current list of pick frustums. The frustum's
     * dimension is specified by calling {@link #setPickPointFrustumDimension(java.awt.Dimension)}.
     * <p>
     * This does nothing if the current pick point is <code>null</code>.
     */
    void addPickPointFrustum();

    /**
     * Creates a frustum containing the current pick rectangle and adds it to the current list of pick frustums.
     * <p>
     * This does nothing if the current pick rectangle is <code>null</code>.
     */
    void addPickRectangleFrustum();

    /**
     * Gets the rendering exceptions associated with this DrawContext as a {@link java.util.Collection} of {@link
     * Throwable} objects. If non-null, the returned Collection is used as the data structure that accumulates rendering
     * exceptions passed to this DrawContext in {@link #addRenderingException(Throwable)}. A null collection indicates
     * this DrawContext does not accumulate rendering exceptions.
     *
     * @return the Collection used to accumulate rendering exceptions, or null if this DrawContext does not accumulate
     *         rendering exceptions.
     */
    Collection<Throwable> getRenderingExceptions();

    /**
     * Sets the rendering exceptions associated with this DrawContext to the specified {@link java.util.Collection} of
     * {@link Throwable} objects. If non-null, the specified Collection is used as the data structure that accumulates
     * rendering exceptions passed to this DrawContext in {@link #addRenderingException(Throwable)}. A null collection
     * indicates this DrawContext should not accumulate rendering exceptions.
     *
     * @param exceptions the Collection of exceptions to be used to accumulate rendering exceptions, or null to disable
     *                   accumulation of rendering exception.
     */
    void setRenderingExceptions(Collection<Throwable> exceptions);

    /**
     * Adds the specified {@link Throwable} to this DrawContext's collection of rendering exceptions. If this
     * DrawContext's collection is null, the specified Throwable is ignored and calling this method is benign.
     *
     * @param t the rendering exception to add as a {@link Throwable}.
     *
     * @throws IllegalArgumentException if the Throwable is null.
     */
    void addRenderingException(Throwable t);

    /**
     * Modifies the current projection matrix to slightly offset subsequently drawn objects toward or away from the eye
     * point. This gives those objects visual priority over objects at the same or nearly the same position. After the
     * objects are drawn, call {@link #popProjectionOffest()} to cancel the effect for subsequently drawn objects.
     * <p>
     * <em>Note:</em> This capability is meant to be applied only within a single Renderable. It is not intended as a
     * means to offset a whole Renderable or collection of Renderables.
     * <p>
     * See "Mathematics for Game Programming and 3D Computer Graphics, 2 ed." by  Eric Lengyel, Section 9.1, "Depth
     * Value Offset" for a description of this technique.
     *
     * @param offset a reference to an offset value, typically near 1.0, or null to request use of the default value.
     *               Values less than 1.0 pull objects toward the eye point, values greater than 1.0 push objects away
     *               from the eye point. The default value is 0.99.
     *
     * @see #popProjectionOffest()
     */
    void pushProjectionOffest(Double offset);

    /**
     * Removes the current projection matrix offset added by {@link #pushProjectionOffest(Double)}.
     *
     * @see #pushProjectionOffest(Double)
     */
    void popProjectionOffest();

    /**
     * Indicates whether the {@link SceneController} is currently rendering the draw context's {@link
     * OrderedRenderable}s. When the this method returns false during a call to an ordered renderable's {@link
     * OrderedRenderable#render(DrawContext)} method, the ordered renderable should add itself to the draw context via
     * {@link #addOrderedRenderable(OrderedRenderable)} rather than draw it. When this method returns true during a call
     * to its render method, the ordered renderable should draw itself.
     *
     * @return true if the scene controller is currently rendering its ordered renderables, otherwise false.
     */
    boolean isOrderedRenderingMode();

    /**
     * Called by the {@link gov.nasa.worldwind.SceneController} to indicate whether it is currently drawing the draw
     * context's {@link gov.nasa.worldwind.render.OrderedRenderable}s. See {@link #isOrderedRenderingMode()} for more
     * information.
     *
     * @param tf true if ordered renderables are being drawn, false if ordered renderables are only being accumulated.
     */
    void setOrderedRenderingMode(boolean tf);

    /**
     * Performs a multi-pass rendering technique to ensure that outlines around filled shapes are drawn correctly when
     * blending or ant-aliasing is performed, and that filled portions of the shape resolve depth-buffer fighting with
     * shapes previously drawn in favor of the current shape.
     *
     * @param renderer an object implementing the {@link gov.nasa.worldwind.render.OutlinedShape} interface for the
     *                 shape.
     * @param shape    the shape to render.
     *
     * @see gov.nasa.worldwind.render.OutlinedShape
     */
    void drawOutlinedShape(OutlinedShape renderer, Object shape);

    /**
     * Enables the current standard lighting model. {@link #endStandardLighting()} must be called when rendering of the
     * current shape is complete.
     *
     * @see #setStandardLightingModel(LightingModel)
     * @see #endStandardLighting()
     */
    void beginStandardLighting();

    /** Pops the OpenGL state previously established by {@link #beginStandardLighting()}. */
    void endStandardLighting();

    /**
     * Returns the current model used for standard lighting.
     *
     * @return the current standard lighting model, or null if no model is defined.
     */
    LightingModel getStandardLightingModel();

    /**
     * Specifies the lighting model used for standard lighting.
     *
     * @param standardLighting the lighting model to use for standard lighting, or null to disable standard lighting.
     */
    void setStandardLightingModel(LightingModel standardLighting);

    /**
     * Compute a model-coordinate point on the terrain.
     *
     * @param lat    the point's latitude.
     * @param lon    the point's longitude.
     * @param offset an distance in meters to place the point above or below the terrain.
     *
     * @return a model-coordinate point offset the specified amount from the current terrain.
     *
     * @throws IllegalArgumentException if either the latitude or longitude is null.
     */
    Vec4 computeTerrainPoint(Angle lat, Angle lon, double offset);

    /**
     * Indicates whether a specified extent is smaller than a specified number of pixels for the current view.
     *
     * @param extent    the extent to test. May be null, in which case this method returns false.
     * @param numPixels the number of pixels at and below which the extent is considered too small.
     *
     * @return true if the projected extent is smaller than the specified number of pixels, otherwise false.
     */
    boolean isSmall(Extent extent, int numPixels);

    /**
     * This is a diagnostic method to display normal vectors.
     *
     * @param length the length to draw the vectors, in meters.
     * @param vBuf   a vertex buffer. If null, no vectors are drawn.
     * @param nBuf   a buffer of normal vectors corresponding to the vertex buffer. If null, no vectors are drawn.
     */
    void drawNormals(float length, FloatBuffer vBuf, FloatBuffer nBuf);

    /**
     * Returns the next {@link gov.nasa.worldwind.render.OrderedRenderable} on the ordered-renderable priority queue but
     * does not remove it from the queue.
     *
     * @return the next ordered renderable, or null if there are no more ordered renderables on the queue.
     */
    OrderedRenderable peekOrderedRenderables();

    /**
     * Returns the next {@link gov.nasa.worldwind.render.OrderedRenderable} on the ordered-renderable priority queue and
     * removes it from the queue.
     *
     * @return the next ordered renderable, or null if there are no more ordered renderables on the queue.
     */
    OrderedRenderable pollOrderedRenderables();

    /**
     * Returns a {@link Terrain} object that uses the current sector geometry or the current globe to compute surface
     * points.
     *
     * @return a terrain object for computing surface points and intersections.
     */
    Terrain getTerrain();

    /** Restores the current OpenGL context's blending state to its default. */
    void restoreDefaultBlending();

    /** Restores the current OpenGL context's current color to its default. */
    void restoreDefaultCurrentColor();

    /** Restores the current OpenGL context's depth testing state to its default. */
    void restoreDefaultDepthTesting();

    /**
     * Indicates whether the scene controller is currently pre-rendering.
     *
     * @return true if pre-rendering, otherwise false.
     *
     * @see PreRenderable
     */
    boolean isPreRenderMode();

    /**
     * Specifies whether the scene controller is pre-rendering.
     *
     * @param preRenderMode true to indicate pre-rendering, otherwise false.
     *
     * @see PreRenderable
     */
    void setPreRenderMode(boolean preRenderMode);

    /**
     * Computes a Cartesian point from a specified geographic position, applying a specified altitude mode.
     *
     * @param position     the position to convert.
     * @param altitudeMode the altitude mode.
     *
     * @return the Cartesian point corresponding to the specified position and this context's current globe or terrain.
     *
     * @throws IllegalArgumentException if the position is null.
     */
    Vec4 computePointFromPosition(Position position, int altitudeMode);

    /**
     * Returns the draw context's decluttering text renderer.
     *
     * @return the decluttering text renderer.
     */
    DeclutteringTextRenderer getDeclutteringTextRenderer();

    /** Filter overlapping text from the ordered renderable list. */
    void applyClutterFilter();
//
//    void applyGroupingFilters();
//
//    GroupingFilter getGroupingFilter(String filterName);
//
//    void setGroupingFilters(Map<String, GroupingFilter> filters);

    /**
     * Specifies the clutter filter to use. May be null to indicate no decluttering.
     *
     * @param filter the clutter filter.
     */
    void setClutterFilter(ClutterFilter filter);

    /**
     * Returns the current clutter filter.
     *
     * @return the current clutter filter.
     */
    ClutterFilter getClutterFilter();

    boolean is2DGlobe();

    boolean isContinuous2DGlobe();
}
