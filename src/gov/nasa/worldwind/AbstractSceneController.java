/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: AbstractSceneController.java 2442 2014-11-19 22:50:42Z tgaskins $
 */
public abstract class AbstractSceneController extends WWObjectImpl implements SceneController
{
    protected Model model;
    protected View view;
    protected double verticalExaggeration = 1d;
    protected DrawContext dc = new DrawContextImpl();
    /**
     * The list of picked objects at the current pick point. This list is computed during each call to repaint.
     * Initially <code>null</code>.
     */
    protected PickedObjectList lastPickedObjects;
    /**
     * The list of picked objects that intersect the current pick rectangle. This list is computed during each call to
     * repaint. Initially <code>null</code>.
     */
    protected PickedObjectList lastObjectsInPickRect;
    /**
     * Map of integer color codes to picked objects used to quickly resolve the top picked objects in {@link
     * #doResolveTopPick(gov.nasa.worldwind.render.DrawContext, java.awt.Rectangle)}. This map is used only when a pick
     * rectangle is specified. Initialized to a new HashMap.
     */
    protected Map<Integer, PickedObject> pickableObjects = new HashMap<Integer, PickedObject>();
    protected long frame = 0;
    protected long timebase = System.currentTimeMillis();
    protected double framesPerSecond;
    protected double frameTime;
    protected double pickTime;
    /**
     * The pick point in AWT screen coordinates, or <code>null</code> if the pick point is disabled. Initially
     * <code>null</code>.
     */
    protected Point pickPoint = null;
    /**
     * The pick rectangle in AWT screen coordinates, or <code>null</code> if the pick rectangle is disabled. Initially
     * <code>null</code>.
     */
    protected Rectangle pickRect = null;
    protected boolean deepPick = false;
    protected GpuResourceCache gpuResourceCache;
    protected TextRendererCache textRendererCache = new TextRendererCache();
    protected Set<String> perFrameStatisticsKeys = new HashSet<String>();
    protected Collection<PerformanceStatistic> perFrameStatistics = new ArrayList<PerformanceStatistic>();
    protected Collection<Throwable> renderingExceptions = new ArrayList<Throwable>();
    protected ScreenCreditController screenCreditController;
    protected GLRuntimeCapabilities glRuntimeCaps = new GLRuntimeCapabilities();
    protected ArrayList<Point> pickPoints = new ArrayList<Point>();
    /**
     * Support class used to build the composite representation of surface objects as a list of SurfaceTiles. We keep a
     * reference to the tile builder instance used to build tiles because it acts as a cache key to the tiles and
     * determines when the tiles must be updated. The tile builder does not retain any references the SurfaceObjects, so
     * keeping a reference to it does not leak memory.
     */
    protected SurfaceObjectTileBuilder surfaceObjectTileBuilder = new SurfaceObjectTileBuilder();
    /** The display name for the surface object tile count performance statistic. */
    protected static final String SURFACE_OBJECT_TILE_COUNT_NAME = "Surface Object Tiles";
    protected ClutterFilter clutterFilter = new BasicClutterFilter();
    //protected Map<String, GroupingFilter> groupingFilters = new HashMap<String, GroupingFilter>();
    protected boolean deferOrderedRendering;

    public AbstractSceneController()
    {
        this.setVerticalExaggeration(Configuration.getDoubleValue(AVKey.VERTICAL_EXAGGERATION, 1d));
    }

    public void reinitialize()
    {
        if (this.textRendererCache != null)
            this.textRendererCache.dispose();
        this.textRendererCache = new TextRendererCache();
    }

    /** Releases resources associated with this scene controller. */
    public void dispose()
    {
        if (this.lastPickedObjects != null)
            this.lastPickedObjects.clear();
        this.lastPickedObjects = null;

        if (this.lastObjectsInPickRect != null)
            this.lastObjectsInPickRect.clear();
        this.lastObjectsInPickRect = null;

        if (this.dc != null)
            this.dc.dispose();

        if (this.textRendererCache != null)
            this.textRendererCache.dispose();
    }

    public GpuResourceCache getGpuResourceCache()
    {
        return this.gpuResourceCache;
    }

    public void setGpuResourceCache(GpuResourceCache gpuResourceCache)
    {
        this.gpuResourceCache = gpuResourceCache;
    }

    public TextRendererCache getTextRendererCache()
    {
        return textRendererCache;
    }

    public Model getModel()
    {
        return this.model;
    }

    public View getView()
    {
        return this.view;
    }

    public void setModel(Model model)
    {
        if (this.model != null)
            this.model.removePropertyChangeListener(this);
        if (model != null)
            model.addPropertyChangeListener(this);

        Model oldModel = this.model;
        this.model = model;
        this.firePropertyChange(AVKey.MODEL, oldModel, model);
    }

    public void setView(View view)
    {
        if (this.view != null)
            this.view.removePropertyChangeListener(this);
        if (view != null)
            view.addPropertyChangeListener(this);

        View oldView = this.view;
        this.view = view;

        this.firePropertyChange(AVKey.VIEW, oldView, view);
    }

    public void setVerticalExaggeration(double verticalExaggeration)
    {
        Double oldVE = this.verticalExaggeration;
        this.verticalExaggeration = verticalExaggeration;
        this.firePropertyChange(AVKey.VERTICAL_EXAGGERATION, oldVE, verticalExaggeration);
    }

    public double getVerticalExaggeration()
    {
        return this.verticalExaggeration;
    }

    /** {@inheritDoc} */
    public void setPickPoint(Point pickPoint)
    {
        this.pickPoint = pickPoint;
    }

    /** {@inheritDoc} */
    public Point getPickPoint()
    {
        return this.pickPoint;
    }

    /** {@inheritDoc} */
    public void setPickRectangle(Rectangle pickRect)
    {
        this.pickRect = pickRect;
    }

    /** {@inheritDoc} */
    public Rectangle getPickRectangle()
    {
        return this.pickRect;
    }

    /** {@inheritDoc} */
    public PickedObjectList getPickedObjectList()
    {
        return this.lastPickedObjects;
    }

    protected void setPickedObjectList(PickedObjectList pol)
    {
        this.lastPickedObjects = pol;
    }

    /** {@inheritDoc} */
    public PickedObjectList getObjectsInPickRectangle()
    {
        return this.lastObjectsInPickRect;
    }

    public void setDeepPickEnabled(boolean tf)
    {
        this.deepPick = tf;
    }

    public boolean isDeepPickEnabled()
    {
        return this.deepPick;
    }

    public SectorGeometryList getTerrain()
    {
        return this.dc.getSurfaceGeometry();
    }

    public DrawContext getDrawContext()
    {
        return this.dc;
    }

    public double getFramesPerSecond()
    {
        return this.framesPerSecond;
    }

    public double getFrameTime()
    {
        return this.frameTime;
    }

    public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        this.perFrameStatisticsKeys.clear();
        if (keys == null)
            return;

        for (String key : keys)
        {
            if (key != null)
                this.perFrameStatisticsKeys.add(key);
        }
    }

    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return perFrameStatistics;
    }

    public Collection<Throwable> getRenderingExceptions()
    {
        return this.renderingExceptions;
    }

    public ScreenCreditController getScreenCreditController()
    {
        return screenCreditController;
    }

    public void setScreenCreditController(ScreenCreditController screenCreditController)
    {
        this.screenCreditController = screenCreditController;
    }

    /** {@inheritDoc} */
    public GLRuntimeCapabilities getGLRuntimeCapabilities()
    {
        return this.glRuntimeCaps;
    }

    /** {@inheritDoc} */
    public void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities)
    {
        if (capabilities == null)
        {
            String message = Logging.getMessage("nullValue.GLRuntimeCapabilitiesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.glRuntimeCaps = capabilities;
    }

    @Override
    public ClutterFilter getClutterFilter()
    {
        return clutterFilter;
    }

    @Override
    public void setClutterFilter(ClutterFilter clutterFilter)
    {
        this.clutterFilter = clutterFilter;
    }
//
//    @Override
//    public GroupingFilter getGroupingFilter(String filterName)
//    {
//        if (WWUtil.isEmpty(filterName))
//        {
//            String msg = Logging.getMessage("nullValue.OrderedRenderable"); // TODO: proper log message
//            Logging.logger().warning(msg);
//            return null;
//        }
//
//        return this.dc.getGroupingFilter(filterName);
//    }
//
//    @Override
//    public void addGroupingFilter(String filterName, GroupingFilter filter)
//    {
//        if (WWUtil.isEmpty(filterName))
//        {
//            String msg = Logging.getMessage("nullValue.OrderedRenderable"); // TODO: proper log message
//            Logging.logger().warning(msg);
//            return;
//        }
//
//        if (filter == null)
//        {
//            String msg = Logging.getMessage("nullValue.OrderedRenderable"); // TODO: proper log message
//            Logging.logger().warning(msg);
//            return; // benign event
//        }
//
//        this.groupingFilters.put(filterName, filter);
//    }
//
//    @Override
//    public void removeGroupingFilter(String filterName)
//    {
//        if (WWUtil.isEmpty(filterName))
//        {
//            String msg = Logging.getMessage("nullValue.OrderedRenderable"); // TODO: proper log message
//            Logging.logger().warning(msg);
//            return;
//        }
//
//        this.groupingFilters.remove(filterName);
//    }
//
//    @Override
//    public void removeAllGroupingFilters()
//    {
//        this.groupingFilters.clear();
//    }
//
//    protected void resetGroupingFilters()
//    {
//        if (this.getView() == null)
//            return;
//
//        Rectangle2D vp = this.getView().getViewport();
//        if (vp == null)
//            return;
//
//        for (GroupingFilter filter : this.groupingFilters.values())
//        {
//            filter.clear();
//            filter.setDimensions((int) vp.getWidth(), (int) vp.getHeight());
//        }
//    }

    public boolean isDeferOrderedRendering()
    {
        return deferOrderedRendering;
    }

    public void setDeferOrderedRendering(boolean deferOrderedRendering)
    {
        this.deferOrderedRendering = deferOrderedRendering;
    }

    public int repaint()
    {
        this.frameTime = System.currentTimeMillis();

        this.perFrameStatistics.clear();
        this.renderingExceptions.clear(); // Clear the rendering exceptions accumulated during the last frame.
        this.glRuntimeCaps.initialize(GLContext.getCurrent());
        this.initializeDrawContext(this.dc);
        this.doRepaint(this.dc);

        ++this.frame;
        long time = System.currentTimeMillis();
        this.frameTime = System.currentTimeMillis() - this.frameTime;
        if (time - this.timebase > 2000) // recalculate every two seconds
        {
            this.framesPerSecond = frame * 1000d / (time - timebase);
            this.timebase = time;
            this.frame = 0;
        }
        this.dc.setPerFrameStatistic(PerformanceStatistic.FRAME_TIME, "Frame Time (ms)", (int) this.frameTime);
        this.dc.setPerFrameStatistic(PerformanceStatistic.FRAME_RATE, "Frame Rate (fps)", (int) this.framesPerSecond);
        this.dc.setPerFrameStatistic(PerformanceStatistic.PICK_TIME, "Pick Time (ms)", (int) this.pickTime);

        Set<String> perfKeys = dc.getPerFrameStatisticsKeys();
        if (perfKeys == null)
            return dc.getRedrawRequested();

        if (perfKeys.contains(PerformanceStatistic.MEMORY_CACHE) || perfKeys.contains(PerformanceStatistic.ALL))
        {
            this.dc.setPerFrameStatistics(WorldWind.getMemoryCacheSet().getPerformanceStatistics());
        }

        if (perfKeys.contains(PerformanceStatistic.TEXTURE_CACHE) || perfKeys.contains(PerformanceStatistic.ALL))
        {
            if (dc.getTextureCache() != null)
                this.dc.setPerFrameStatistic(PerformanceStatistic.TEXTURE_CACHE,
                    "Texture Cache size (Kb)", this.dc.getTextureCache().getUsedCapacity() / 1000);
        }

        if (perfKeys.contains(PerformanceStatistic.JVM_HEAP) || perfKeys.contains(PerformanceStatistic.ALL))
        {
            long totalMemory = Runtime.getRuntime().totalMemory();
            this.dc.setPerFrameStatistic(PerformanceStatistic.JVM_HEAP,
                "JVM total memory (Kb)", totalMemory / 1000);

            this.dc.setPerFrameStatistic(PerformanceStatistic.JVM_HEAP_USED,
                "JVM used memory (Kb)", (totalMemory - Runtime.getRuntime().freeMemory()) / 1000);
        }

        return dc.getRedrawRequested();
    }

    abstract protected void doRepaint(DrawContext dc);

    protected void initializeDrawContext(DrawContext dc)
    {
        dc.initialize(GLContext.getCurrent());
        dc.setGLRuntimeCapabilities(this.glRuntimeCaps);
        dc.setPerFrameStatisticsKeys(this.perFrameStatisticsKeys, this.perFrameStatistics);
        dc.setRenderingExceptions(this.renderingExceptions);
        dc.setGpuResourceCache(this.gpuResourceCache);
        dc.setTextRendererCache(this.textRendererCache);
        dc.setModel(this.model);
        dc.setView(this.view);
        dc.setVerticalExaggeration(this.verticalExaggeration);
        dc.setPickPoint(this.pickPoint);
        dc.setPickRectangle(this.pickRect);
        dc.setViewportCenterScreenPoint(this.getViewportCenter(dc));
        dc.setViewportCenterPosition(null);
        dc.setClutterFilter(this.getClutterFilter());
//        dc.setGroupingFilters(this.groupingFilters);

        long frameTimeStamp = System.currentTimeMillis();
        // Ensure that the frame time stamps differ between frames. This is necessary on machines with low-resolution
        // JVM clocks or that are so fast that they render under 1 millisecond.
        if (frameTimeStamp == dc.getFrameTimeStamp())
        {
            ++frameTimeStamp;
        }
        dc.setFrameTimeStamp(frameTimeStamp);
        // Indicate the frame time stamp to apps.
        this.setValue(AVKey.FRAME_TIMESTAMP, frameTimeStamp);
    }

    protected Point getViewportCenter(DrawContext dc)
    {
        View view = dc.getView();
        if (view == null)
            return null;

        Rectangle viewport = view.getViewport();
        if (viewport == null)
            return null;

        return new Point((int) (viewport.getCenterX() + 0.5), (int) (viewport.getCenterY() + 0.5));
    }

    protected void initializeFrame(DrawContext dc)
    {
        if (dc.getGLContext() == null)
        {
            String message = Logging.getMessage("BasicSceneController.GLContextNullStartRedisplay");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glPushAttrib(GL2.GL_VIEWPORT_BIT | GL2.GL_ENABLE_BIT | GL2.GL_TRANSFORM_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    protected void clearFrame(DrawContext dc)
    {
        Color cc = dc.getClearColor();
        dc.getGL().glClearColor(cc.getRed(), cc.getGreen(), cc.getBlue(), cc.getAlpha());
        dc.getGL().glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    }

    protected void finalizeFrame(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();

        gl.glPopAttrib();

//        checkGLErrors(dc);
    }

    protected void applyView(DrawContext dc)
    {
        if (dc.getView() != null)
            dc.getView().apply(dc);
//
//        this.resetGroupingFilters();
    }

    protected void createPickFrustum(DrawContext dc)
    {
        dc.addPickPointFrustum();
        dc.addPickRectangleFrustum();
    }

    protected void createTerrain(DrawContext dc)
    {
        if (dc.getSurfaceGeometry() == null)
        {
            if (dc.getModel() != null && dc.getModel().getGlobe() != null)
            {
                SectorGeometryList sgl = dc.getModel().getGlobe().tessellate(dc);
                dc.setSurfaceGeometry(sgl);
                dc.setVisibleSector(sgl.getSector());
            }

            if (dc.getSurfaceGeometry() == null)
            {
                Logging.logger().warning("generic.NoSurfaceGeometry");
                dc.setPerFrameStatistic(PerformanceStatistic.TERRAIN_TILE_COUNT, "Terrain Tiles", 0);
                // keep going because some layers, etc. may have meaning w/o surface geometry
            }

            dc.setPerFrameStatistic(PerformanceStatistic.TERRAIN_TILE_COUNT, "Terrain Tiles",
                dc.getSurfaceGeometry().size());
        }
    }

    protected void preRender(DrawContext dc)
    {
        try
        {
            dc.setPreRenderMode(true);

            // Pre-render the layers.
            if (dc.getLayers() != null)
            {
                for (Layer layer : dc.getLayers())
                {
                    try
                    {
                        dc.setCurrentLayer(layer);
                        layer.preRender(dc);
                    }
                    catch (Exception e)
                    {
                        String message = Logging.getMessage("SceneController.ExceptionWhilePreRenderingLayer",
                            (layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown")));
                        Logging.logger().log(Level.SEVERE, message, e);
                        // Don't abort; continue on to the next layer.
                    }
                }

                dc.setCurrentLayer(null);
            }

            // Pre-render the deferred/ordered surface renderables.
            this.preRenderOrderedSurfaceRenderables(dc);
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, Logging.getMessage("BasicSceneController.ExceptionDuringPreRendering"),
                e);
        }
        finally
        {
            dc.setPreRenderMode(false);
        }
    }

    protected void pickTerrain(DrawContext dc)
    {
        if (dc.isPickingMode() && dc.getVisibleSector() != null && dc.getSurfaceGeometry() != null &&
            dc.getSurfaceGeometry().size() > 0)
        {
            this.pickPoints.clear();
            if (dc.getPickPoint() != null)
                this.pickPoints.add(dc.getPickPoint());

            Point vpc = dc.getViewportCenterScreenPoint();
            if (vpc != null && dc.getViewportCenterPosition() == null)
                this.pickPoints.add(vpc);

            if (this.pickPoints.size() == 0)
                return;

            List<PickedObject> pickedObjects = dc.getSurfaceGeometry().pick(dc, this.pickPoints);
            if (pickedObjects == null || pickedObjects.size() == 0)
                return;

            for (PickedObject po : pickedObjects)
            {
                if (po == null)
                    continue;
                if (po.getPickPoint().equals(dc.getPickPoint()))
                    dc.addPickedObject(po);
                else if (po.getPickPoint().equals(vpc))
                    dc.setViewportCenterPosition((Position) po.getObject());
            }
        }
    }

    protected void pickLayers(DrawContext dc)
    {
        if (dc.getLayers() != null)
        {
            for (Layer layer : dc.getLayers())
            {
                try
                {
                    if (layer != null && layer.isPickEnabled())
                    {
                        dc.setCurrentLayer(layer);
                        layer.pick(dc, dc.getPickPoint());
                    }
                }
                catch (Exception e)
                {
                    String message = Logging.getMessage("SceneController.ExceptionWhilePickingInLayer",
                        (layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown")));
                    Logging.logger().log(Level.SEVERE, message, e);
                    // Don't abort; continue on to the next layer.
                }
            }

            dc.setCurrentLayer(null);
        }
    }

    protected void resolveTopPick(DrawContext dc)
    {
        // Resolve the top object at the pick point, if the pick point is enabled.
        if (dc.getPickPoint() != null)
            this.doResolveTopPick(dc, dc.getPickPoint());

        // Resolve the top objects in the pick rectangle, if the pick rectangle is enabled.
        if (dc.getPickRectangle() != null && !dc.getPickRectangle().isEmpty())
            this.doResolveTopPick(dc, dc.getPickRectangle());
    }

    protected void doResolveTopPick(DrawContext dc, Point pickPoint)
    {
        PickedObjectList pol = dc.getPickedObjects();
        if (pol != null && pol.size() == 1)
        {
            // If there is only one picked object, then it must be the top object so we're done.
            pol.get(0).setOnTop();
        }
        else if (pol != null && pol.size() > 1)
        {
            // If there is more than one picked object, then find the picked object corresponding to the top color at
            // the pick point, and mark it as on top
            int colorCode = dc.getPickColorAtPoint(pickPoint);
            if (colorCode != 0)
            {
                for (PickedObject po : pol)
                {
                    if (po != null && po.getColorCode() == colorCode)
                    {
                        po.setOnTop();
                        break; // No need to check the remaining picked objects.
                    }
                }
            }
        }
    }

    protected void doResolveTopPick(DrawContext dc, Rectangle pickRect)
    {
        PickedObjectList pol = dc.getObjectsInPickRectangle();
        if (pol != null && pol.size() == 1)
        {
            // If there is only one picked object, then it must be the top object so we're done.
            pol.get(0).setOnTop();
        }
        else if (pol != null && pol.size() > 1)
        {
            int[] minAndMaxColorCodes = null;

            for (PickedObject po : pol)
            {
                int colorCode = po.getColorCode();

                // Put all of the eligible picked objects in a map to provide constant time access to a picked object
                // by its color code. Since the number of unique color codes and picked objects may both be large, using
                // a hash map reduces the complexity of the next loop from O(n*m) to O(n*c), where n and m are the
                // lengths of the unique color list and picked object list, respectively, and c is the constant time
                // associated with a hash map access.
                this.pickableObjects.put(colorCode, po);

                // Keep track of the minimum and maximum color codes of the scene's picked objects. These values are
                // used to cull the number of colors that the draw context must consider with identifying the unique
                // pick colors in the specified screen rectangle.
                if (minAndMaxColorCodes == null)
                    minAndMaxColorCodes = new int[] {colorCode, colorCode};
                else
                {
                    if (minAndMaxColorCodes[0] > colorCode)
                        minAndMaxColorCodes[0] = colorCode;
                    if (minAndMaxColorCodes[1] < colorCode)
                        minAndMaxColorCodes[1] = colorCode;
                }
            }

            // If there is more than one picked object, then find the picked objects corresponding to each of the top
            // colors in the pick rectangle, and mark them all as on top.
            int[] colorCodes = dc.getPickColorsInRectangle(pickRect, minAndMaxColorCodes);
            if (colorCodes != null && colorCodes.length > 0)
            {
                // Find the top picked object for each unique color code, if any, and mark it as on top.
                for (int colorCode : colorCodes)
                {
                    if (colorCode != 0) // This should never happen, but we check anyway.
                    {
                        PickedObject po = this.pickableObjects.get(colorCode);
                        if (po != null)
                            po.setOnTop();
                    }
                }
            }

            // Clear the map of eligible picked objects so that the picked objects from this frame do not affect the
            // next frame. This also ensures that we do not leak memory by retaining references to picked objects.
            this.pickableObjects.clear();
        }
    }

    protected void pick(DrawContext dc)
    {
        this.pickTime = System.currentTimeMillis();
        this.lastPickedObjects = null;
        this.lastObjectsInPickRect = null;

        try
        {
            dc.enablePickingMode();
            this.pickTerrain(dc);
            this.doNonTerrainPick(dc);

            if (this.isDeferOrderedRendering())
                return;

            this.resolveTopPick(dc);
            this.lastPickedObjects = new PickedObjectList(dc.getPickedObjects());
            this.lastObjectsInPickRect = new PickedObjectList(dc.getObjectsInPickRectangle());

            if (this.isDeepPickEnabled() &&
                (this.lastPickedObjects.hasNonTerrainObjects() || this.lastObjectsInPickRect.hasNonTerrainObjects()))
            {
                this.doDeepPick(dc);
            }
        }
        catch (Throwable e)
        {
            Logging.logger().log(Level.SEVERE, Logging.getMessage("BasicSceneController.ExceptionDuringPick"), e);
        }
        finally
        {
            dc.disablePickingMode();
            this.pickTime = System.currentTimeMillis() - this.pickTime;
        }
    }

    protected void doNonTerrainPick(DrawContext dc)
    {
        // Don't do the pick if there's no current pick point and no current pick rectangle.
        if (dc.getPickPoint() == null && (dc.getPickRectangle() == null || dc.getPickRectangle().isEmpty()))
            return;

        // Pick against the layers.
        this.pickLayers(dc);

        // Pick against the deferred/ordered surface renderables.
        this.pickOrderedSurfaceRenderables(dc);

        if (this.isDeferOrderedRendering())
            return;

        // Pick against the screen credits.
        if (this.screenCreditController != null)
            this.screenCreditController.pick(dc, dc.getPickPoint());

        // Pick against the deferred/ordered renderables.
        dc.setOrderedRenderingMode(true);
//        dc.applyGroupingFilters();
        dc.applyClutterFilter();
        while (dc.peekOrderedRenderables() != null)
        {
            dc.pollOrderedRenderables().pick(dc, dc.getPickPoint());
        }
        dc.setOrderedRenderingMode(false);
    }

    protected void doDeepPick(DrawContext dc)
    {
        PickedObjectList currentPickedObjects = this.lastPickedObjects;
        PickedObjectList currentObjectsInPickRect = this.lastObjectsInPickRect;

        dc.setDeepPickingEnabled(true);
        this.doNonTerrainPick(dc);
        dc.setDeepPickingEnabled(false);

        this.lastPickedObjects = this.mergePickedObjectLists(currentPickedObjects, dc.getPickedObjects());
        this.lastObjectsInPickRect = this.mergePickedObjectLists(currentObjectsInPickRect,
            dc.getObjectsInPickRectangle());
    }

    protected PickedObjectList mergePickedObjectLists(PickedObjectList listA, PickedObjectList listB)
    {
        if (listA == null || listB == null || !listA.hasNonTerrainObjects() || !listB.hasNonTerrainObjects())
            return listA;

        for (PickedObject pb : listB)
        {
            if (pb.isTerrain())
                continue;

            boolean common = false; // cannot modify listA within its iterator, so use a flag to indicate commonality
            for (PickedObject pa : listA)
            {
                if (pa.isTerrain())
                    continue;

                if (pa.getObject() == pb.getObject())
                {
                    common = true;
                    break;
                }
            }

            if (!common)
                listA.add(pb);
        }

        return listA;
    }

    protected void draw(DrawContext dc)
    {
        try
        {
            // Draw the layers.
            if (dc.getLayers() != null)
            {
                for (Layer layer : dc.getLayers())
                {
                    try
                    {
                        if (layer != null)
                        {
                            dc.setCurrentLayer(layer);
                            layer.render(dc);
                        }
                    }
                    catch (Exception e)
                    {
                        String message = Logging.getMessage("SceneController.ExceptionWhileRenderingLayer",
                            (layer != null ? layer.getClass().getName() : Logging.getMessage("term.unknown")));
                        Logging.logger().log(Level.SEVERE, message, e);
                        // Don't abort; continue on to the next layer.
                    }
                }

                dc.setCurrentLayer(null);
            }

            // Draw the deferred/ordered surface renderables.
            this.drawOrderedSurfaceRenderables(dc);

            if (this.isDeferOrderedRendering())
                return;

            if (this.screenCreditController != null)
                this.screenCreditController.render(dc);

            // Draw the deferred/ordered renderables.
            dc.setOrderedRenderingMode(true);
//            dc.applyGroupingFilters();
            dc.applyClutterFilter();
            while (dc.peekOrderedRenderables() != null)
            {
                try
                {
                    dc.pollOrderedRenderables().render(dc);
                }
                catch (Exception e)
                {
                    Logging.logger().log(Level.WARNING,
                        Logging.getMessage("BasicSceneController.ExceptionDuringRendering"), e);
                }
            }
            dc.setOrderedRenderingMode(false);

            // Draw the diagnostic displays.
            if (dc.getSurfaceGeometry() != null && dc.getModel() != null && (dc.getModel().isShowWireframeExterior() ||
                dc.getModel().isShowWireframeInterior() || dc.getModel().isShowTessellationBoundingVolumes()))
            {
                Model model = dc.getModel();

                float[] previousColor = new float[4];
                GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
                gl.glGetFloatv(GL2.GL_CURRENT_COLOR, previousColor, 0);

                for (SectorGeometry sg : dc.getSurfaceGeometry())
                {
                    if (model.isShowWireframeInterior() || model.isShowWireframeExterior())
                        sg.renderWireframe(dc, model.isShowWireframeInterior(), model.isShowWireframeExterior());

                    if (model.isShowTessellationBoundingVolumes())
                    {
                        gl.glColor3d(1, 0, 0);
                        sg.renderBoundingVolume(dc);
                    }
                }

                gl.glColor4fv(previousColor, 0);
            }
        }
        catch (Throwable e)
        {
            Logging.logger().log(Level.SEVERE, Logging.getMessage("BasicSceneController.ExceptionDuringRendering"), e);
        }
    }

    /**
     * Called to check for openGL errors. This method includes a "round-trip" between the application and renderer,
     * which is slow. Therefore, this method is excluded from the "normal" render pass. It is here as a matter of
     * convenience to developers, and is not part of the API.
     *
     * @param dc the relevant <code>DrawContext</code>
     */
    @SuppressWarnings({"UNUSED_SYMBOL", "UnusedDeclaration"})
    protected void checkGLErrors(DrawContext dc)
    {
        GL gl = dc.getGL();

        for (int err = gl.glGetError(); err != GL.GL_NO_ERROR; err = gl.glGetError())
        {
            String msg = dc.getGLU().gluErrorString(err);
            msg += err;
            Logging.logger().severe(msg);
        }
    }

    //**************************************************************//
    //********************  Ordered Surface Renderable  ************//
    //**************************************************************//

    protected void preRenderOrderedSurfaceRenderables(DrawContext dc)
    {
        if (dc.getOrderedSurfaceRenderables().isEmpty())
            return;

        dc.setOrderedRenderingMode(true);

        // Build a composite representation of the SurfaceObjects. This operation potentially modifies the framebuffer
        // contents to update surface tile textures, therefore it must be executed during the preRender phase.
        this.buildCompositeSurfaceObjects(dc);

        // PreRender the individual deferred/ordered surface renderables.
        int logCount = 0;
        while (dc.getOrderedSurfaceRenderables().peek() != null)
        {
            try
            {
                OrderedRenderable or = dc.getOrderedSurfaceRenderables().poll();
                if (or instanceof PreRenderable)
                    ((PreRenderable) or).preRender(dc);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.WARNING,
                    Logging.getMessage("BasicSceneController.ExceptionDuringPreRendering"), e);

                // Limit how many times we log a problem.
                if (++logCount > Logging.getMaxMessageRepeatCount())
                    break;
            }
        }

        dc.setOrderedRenderingMode(false);
    }

    protected void pickOrderedSurfaceRenderables(DrawContext dc)
    {
        dc.setOrderedRenderingMode(true);

        // Pick the individual deferred/ordered surface renderables. We don't use the composite representation of
        // SurfaceObjects because we need to distinguish between individual objects. Therefore we let each object handle
        // drawing and resolving picking.
        while (dc.getOrderedSurfaceRenderables().peek() != null)
        {
            dc.getOrderedSurfaceRenderables().poll().pick(dc, dc.getPickPoint());
        }

        dc.setOrderedRenderingMode(false);
    }

    protected void drawOrderedSurfaceRenderables(DrawContext dc)
    {
        dc.setOrderedRenderingMode(true);

        // Draw the composite representation of the SurfaceObjects created during preRendering.
        this.drawCompositeSurfaceObjects(dc);

        // Draw the individual deferred/ordered surface renderables. SurfaceObjects that add themselves to the ordered
        // surface renderable queue during preRender are drawn in drawCompositeSurfaceObjects. Since this invokes
        // SurfaceObject.render during preRendering, SurfaceObjects should not add themselves to the ordered surface
        // renderable queue for rendering. We assume this queue is not populated with SurfaceObjects that participated
        // in the composite representation created during preRender.
        while (dc.getOrderedSurfaceRenderables().peek() != null)
        {
            try
            {
                dc.getOrderedSurfaceRenderables().poll().render(dc);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.WARNING,
                    Logging.getMessage("BasicSceneController.ExceptionDuringRendering"), e);
            }
        }

        dc.setOrderedRenderingMode(false);
    }

    /**
     * Builds a composite representation for all {@link gov.nasa.worldwind.render.SurfaceObject} instances in the draw
     * context's ordered surface renderable queue. While building the composite representation this invokes {@link
     * gov.nasa.worldwind.render.SurfaceObject#render(gov.nasa.worldwind.render.DrawContext)} in ordered rendering mode.
     * This does nothing if the ordered surface renderable queue is empty, or if it does not contain any
     * SurfaceObjects.
     * <p/>
     * This method is called during the preRender phase, and is therefore free to modify the framebuffer contents to
     * create the composite representation.
     *
     * @param dc The drawing context containing the surface objects to build a composite representation for.
     *
     * @see gov.nasa.worldwind.render.DrawContext#getOrderedSurfaceRenderables()
     */
    protected void buildCompositeSurfaceObjects(DrawContext dc)
    {
        if (dc.getOrderedSurfaceRenderables().size() > 0)
        {
            this.surfaceObjectTileBuilder.buildTiles(dc, dc.getOrderedSurfaceRenderables());
        }
    }

    /**
     * Causes the scene controller to draw the composite representation of all {@link
     * gov.nasa.worldwind.render.SurfaceObject} instances in the draw context's ordered surface renderable queue. This
     * representation was built during the preRender phase. This does nothing if the ordered surface renderable queue is
     * empty, or if it does not contain any SurfaceObjects.
     *
     * @param dc The drawing context containing the surface objects who's composite representation is drawn.
     */
    protected void drawCompositeSurfaceObjects(DrawContext dc)
    {
        int tileCount = this.surfaceObjectTileBuilder.getTileCount(dc);
        if (tileCount == 0)
            return;

        int attributeMask =
            GL2.GL_COLOR_BUFFER_BIT   // For alpha test enable, blend enable, alpha func, blend func, blend ref.
                | GL2.GL_POLYGON_BIT; // For cull face enable, cull face, polygon mode.

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushAttrib(gl, attributeMask);
        try
        {
            gl.glEnable(GL.GL_BLEND);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            OGLUtil.applyBlending(gl, true); // the RGB colors in surface object tiles are premultiplied

            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.surfaceObjectTileBuilder.getTiles(dc));
            dc.setPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT, SURFACE_OBJECT_TILE_COUNT_NAME, tileCount);
        }
        finally
        {
            ogsh.pop(gl);
            this.surfaceObjectTileBuilder.clearTiles(dc);
        }
    }
}
