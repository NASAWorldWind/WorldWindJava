/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import java.awt.*;
import java.nio.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * @author Tom Gaskins
 * @version $Id: DrawContextImpl.java 2281 2014-08-29 23:08:04Z dcollins $
 */
public class DrawContextImpl extends WWObjectImpl implements DrawContext
{
    protected long frameTimestamp;
    protected GLContext glContext;
    protected GLRuntimeCapabilities glRuntimeCaps;
    protected GLU glu = new GLUgl2();
    protected View view;
    protected Model model;
    protected Globe globe;
    protected double verticalExaggeration = 1d;
    protected Sector visibleSector;
    protected SectorGeometryList surfaceGeometry;
    /**
     * The list of objects at the pick point during the most recent pick traversal. Initialized to an empty
     * PickedObjectList.
     */
    protected PickedObjectList pickedObjects = new PickedObjectList();
    /**
     * The list of objects intersecting the pick rectangle during the most recent pick traversal. Initialized to an
     * empty PickedObjectList.
     */
    protected PickedObjectList objectsInPickRect = new PickedObjectList();
    protected int uniquePickNumber = 0;
    protected Color clearColor = new Color(0, 0, 0, 0);
    /** Buffer of RGB colors used to read back the framebuffer's colors and store them in client memory. */
    protected ByteBuffer pixelColors;
    /**
     * Set of ints used by {@link #getPickColorsInRectangle(java.awt.Rectangle, int[])} to identify the unique color
     * codes in the specified rectangle. This consolidates duplicate colors to a single entry. We use IntSet to achieve
     * constant time insertion, and to reduce overhead associated associated with storing integer primitives in a
     * HashSet.
     */
    protected IntSet uniquePixelColors = new IntSet();
    protected boolean pickingMode = false;
    protected boolean deepPickingMode = false;
    /**
     * Indicates the current pick point in GL screen coordinates, or <code>null</code> to indicate that there is no
     * pick point. Initially <code>null</code>.
     */
    private Point pickPoint = null;
    /**
     * Indicates the current pick rectangle in GL screen coordinates, or <code>null</code> to indicate that there is no
     * pick rectangle. Initially <code>null</code>.
     */
    private Rectangle pickRect = null;
    protected boolean isOrderedRenderingMode = false;
    protected boolean preRenderMode = false;
    protected Point viewportCenterScreenPoint = null;
    protected Position viewportCenterPosition = null;
    protected SurfaceTileRenderer geographicSurfaceTileRenderer = new GeographicSurfaceTileRenderer();
    protected AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();
    protected GpuResourceCache gpuResourceCache;
    protected TextRendererCache textRendererCache;
    protected Set<String> perFrameStatisticsKeys;
    protected Collection<PerformanceStatistic> perFrameStatistics;
    protected SectorVisibilityTree visibleSectors;
    protected Layer currentLayer;
    protected int redrawRequested = 0;
    protected PickPointFrustumList pickFrustumList = new PickPointFrustumList();
    protected Collection<Throwable> renderingExceptions;
    protected Dimension pickPointFrustumDimension = new Dimension(3, 3);
    protected LightingModel standardLighting = new BasicLightingModel();
    protected DeclutteringTextRenderer declutteringTextRenderer = new DeclutteringTextRenderer();
    protected ClutterFilter clutterFilter;
//    protected Map<String, GroupingFilter> groupingFilters;

    protected static class OrderedRenderableEntry
    {
        protected OrderedRenderable or;
        protected double distanceFromEye;
        protected long time;
        protected int globeOffset;
        protected SectorGeometryList surfaceGeometry;

        public OrderedRenderableEntry(OrderedRenderable orderedRenderable, long insertionTime, DrawContext dc)
        {
            this.or = orderedRenderable;
            this.distanceFromEye = orderedRenderable.getDistanceFromEye();
            this.time = insertionTime;
            if (dc.isContinuous2DGlobe())
            {
                this.globeOffset = ((Globe2D) dc.getGlobe()).getOffset();
                this.surfaceGeometry = dc.getSurfaceGeometry();
            }
        }

        public OrderedRenderableEntry(OrderedRenderable orderedRenderable, double distanceFromEye, long insertionTime,
            DrawContext dc)
        {
            this.or = orderedRenderable;
            this.distanceFromEye = distanceFromEye;
            this.time = insertionTime;
            if (dc.isContinuous2DGlobe())
            {
                this.globeOffset = ((Globe2D) dc.getGlobe()).getOffset();
                this.surfaceGeometry = dc.getSurfaceGeometry();
            }
        }
    }

    protected PriorityQueue<OrderedRenderableEntry> orderedRenderables =
        new PriorityQueue<OrderedRenderableEntry>(100, new Comparator<OrderedRenderableEntry>()
        {
            @Override
			public int compare(OrderedRenderableEntry orA, OrderedRenderableEntry orB)
            {
                double eA = orA.distanceFromEye;
                double eB = orB.distanceFromEye;

                return eA > eB ? -1 : eA == eB ? (orA.time < orB.time ? -1 : orA.time == orB.time ? 0 : 1) : 1;
            }
        });
    // Use a standard Queue to store the ordered surface object renderables. Ordered surface renderables are processed
    // in the order they were submitted.
    protected Queue<OrderedRenderable> orderedSurfaceRenderables = new ArrayDeque<OrderedRenderable>();

    /**
     * Free internal resources held by this draw context. A GL context must be current when this method is called.
     *
     * @throws com.jogamp.opengl.GLException - If an OpenGL context is not current when this method is called.
     */
    @Override
	public void dispose()
    {
        this.geographicSurfaceTileRenderer.dispose();
    }

    @Override
	public final GL getGL()
    {
        return this.getGLContext().getGL();
    }

    @Override
	public final GLU getGLU()
    {
        return this.glu;
    }

    @Override
	public final GLContext getGLContext()
    {
        return this.glContext;
    }

    @Override
	public final int getDrawableHeight()
    {
        return this.getGLDrawable().getSurfaceHeight();
    }

    @Override
	public final int getDrawableWidth()
    {
        return this.getGLDrawable().getSurfaceWidth();
    }

    @Override
	public final GLDrawable getGLDrawable()
    {
        return this.getGLContext().getGLDrawable();
    }

    @Override
	public GLRuntimeCapabilities getGLRuntimeCapabilities()
    {
        return this.glRuntimeCaps;
    }

    @Override
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
	public final void initialize(GLContext glContext)
    {
        if (glContext == null)
        {
            String message = Logging.getMessage("nullValue.GLContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.glContext = glContext;

        this.visibleSector = null;
        if (this.surfaceGeometry != null)
            this.surfaceGeometry.clear();
        this.surfaceGeometry = null;

        this.pickedObjects.clear();
        this.objectsInPickRect.clear();
        this.orderedRenderables.clear();
        this.orderedSurfaceRenderables.clear();
        this.uniquePickNumber = 0;
        this.deepPickingMode = false;
        this.redrawRequested = 0;

        this.pickFrustumList.clear();

        this.currentLayer = null;
    }

    @Override
	public final void setModel(Model model)
    {
        this.model = model;
        if (this.model == null)
            return;

        Globe g = this.model.getGlobe();
        if (g != null)
            this.globe = g;
    }

    @Override
	public final Model getModel()
    {
        return this.model;
    }

    @Override
	public final LayerList getLayers()
    {
        return this.model.getLayers();
    }

    @Override
	public final Sector getVisibleSector()
    {
        return this.visibleSector;
    }

    @Override
	public final void setVisibleSector(Sector s)
    {
        // don't check for null - it is possible that no globe is active, no view is active, no sectors visible, etc.
        this.visibleSector = s;
    }

    @Override
	public void setSurfaceGeometry(SectorGeometryList surfaceGeometry)
    {
        this.surfaceGeometry = surfaceGeometry;
    }

    @Override
	public SectorGeometryList getSurfaceGeometry()
    {
        return surfaceGeometry;
    }

    @Override
	public final Globe getGlobe()
    {
        return this.globe != null ? this.globe : this.model.getGlobe();
    }

    @Override
	public final void setView(View view)
    {
        this.view = view;
    }

    @Override
	public final View getView()
    {
        return this.view;
    }

    @Override
	public final void setGLContext(GLContext glContext)
    {
        if (glContext == null)
        {
            String message = Logging.getMessage("nullValue.GLContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.glContext = glContext;
    }

    @Override
	public final double getVerticalExaggeration()
    {
        return verticalExaggeration;
    }

    @Override
	public final void setVerticalExaggeration(double verticalExaggeration)
    {
        this.verticalExaggeration = verticalExaggeration;
    }

    @Override
	public GpuResourceCache getTextureCache()
    {
        return this.gpuResourceCache;
    }

    @Override
	public GpuResourceCache getGpuResourceCache()
    {
        return this.gpuResourceCache;
    }

    @Override
	public void setGpuResourceCache(GpuResourceCache gpuResourceCache)
    {
        if (gpuResourceCache == null)
        {
            String msg = Logging.getMessage("nullValue.GpuResourceCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.gpuResourceCache = gpuResourceCache;
    }

    @Override
	public TextRendererCache getTextRendererCache()
    {
        return textRendererCache;
    }

    @Override
	public void setTextRendererCache(TextRendererCache textRendererCache)
    {
        if (textRendererCache == null)
        {
            String msg = Logging.getMessage("nullValue.TextRendererCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.textRendererCache = textRendererCache;
    }

    @Override
	public AnnotationRenderer getAnnotationRenderer()
    {
        return annotationRenderer;
    }

    @Override
	public void setAnnotationRenderer(AnnotationRenderer ar)
    {
        if (ar == null)
        {
            String msg = Logging.getMessage("nullValue.RendererIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        annotationRenderer = ar;
    }

    @Override
	public LightingModel getStandardLightingModel()
    {
        return standardLighting;
    }

    @Override
	public void setStandardLightingModel(LightingModel standardLighting)
    {
        this.standardLighting = standardLighting;
    }

    @Override
	public Point getPickPoint()
    {
        return this.pickPoint;
    }

    @Override
	public void setPickPoint(Point pickPoint)
    {
    	this.pickPoint = pickPoint;
    }

    @Override
	public Rectangle getPickRectangle()
    {
        return this.pickRect;
    }

    @Override
	public void setPickRectangle(Rectangle pickRect)
    {
        this.pickRect = pickRect;
    }

    @Override
	public Point getViewportCenterScreenPoint()
    {
        return viewportCenterScreenPoint;
    }

    @Override
	public void setViewportCenterScreenPoint(Point viewportCenterScreenPoint)
    {
        this.viewportCenterScreenPoint = viewportCenterScreenPoint;
    }

    @Override
	public Position getViewportCenterPosition()
    {
        return viewportCenterPosition;
    }

    @Override
	public void setViewportCenterPosition(Position viewportCenterPosition)
    {
        this.viewportCenterPosition = viewportCenterPosition;
    }

    @Override
	public void addPickedObjects(PickedObjectList pickedObjects)
    {
        if (pickedObjects == null)
        {
            String msg = Logging.getMessage("nullValue.PickedObjectList");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.pickedObjects == null)
        {
            this.pickedObjects = pickedObjects;
            return;
        }

        for (PickedObject po : pickedObjects)
        {
            this.pickedObjects.add(po);
        }
    }

    @Override
	public void addPickedObject(PickedObject pickedObject)
    {
        if (null == pickedObject)
        {
            String msg = Logging.getMessage("nullValue.PickedObject");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (null == this.pickedObjects)
            this.pickedObjects = new PickedObjectList();

        this.pickedObjects.add(pickedObject);
    }

    @Override
	public PickedObjectList getPickedObjects()
    {
        return this.pickedObjects;
    }

    @Override
	public PickedObjectList getObjectsInPickRectangle()
    {
        return this.objectsInPickRect;
    }

    @Override
	public void addObjectInPickRectangle(PickedObject pickedObject)
    {
        if (pickedObject == null)
        {
            String msg = Logging.getMessage("nullValue.PickedObject");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.objectsInPickRect.add(pickedObject);
    }

    @Override
	public Color getUniquePickColor()
    {
        this.uniquePickNumber++;

        int clearColorCode = this.getClearColor().getRGB();
        if (clearColorCode == this.uniquePickNumber) // skip the clear color
            this.uniquePickNumber++;

        if (this.uniquePickNumber >= 0x00FFFFFF)
        {
            this.uniquePickNumber = 1;  // no black, no white
            if (clearColorCode == this.uniquePickNumber) // skip the clear color
                this.uniquePickNumber++;
        }

        return new Color(this.uniquePickNumber, true); // has alpha
    }

    @Override
	public Color getUniquePickColorRange(int count)
    {
        if (count < 1)
            return null;

        Range range = new Range(this.uniquePickNumber + 1, count); // compute the requested range

        int clearColorCode = this.getClearColor().getRGB();
        if (range.contains(clearColorCode)) // skip the clear color when it's in the range
            range.location = clearColorCode + 1;

        int maxColorCode = range.location + range.length - 1;
        if (maxColorCode >= 0x00FFFFFF) // not enough colors to satisfy the requested range
            return null;

        this.uniquePickNumber = maxColorCode; // set the unique color to the last color in the requested range

        return new Color(range.location, true); // return a pointer to the beginning of the requested range
    }

    @Override
	public Color getClearColor()
    {
        return this.clearColor;
    }

    /** {@inheritDoc} */
    @Override
	public int getPickColorAtPoint(Point point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int x = point.x;
        int y = point.y;

        // Read the framebuffer color at the specified point in OpenGL screen coordinates as a 24-bit RGB value.
        if (this.pixelColors == null || this.pixelColors.capacity() < 3)
            this.pixelColors = Buffers.newDirectByteBuffer(3);
        this.pixelColors.clear();
        this.getGL().glReadPixels(x, y, 1, 1, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, this.pixelColors);

        int colorCode = ((this.pixelColors.get(0) & 0xff) << 16) // Red, bits 16-23
            | ((this.pixelColors.get(1) & 0xff) << 8) // Green, bits 8-16
            | (this.pixelColors.get(2) & 0xff); // Blue, bits 0-7

        return colorCode != this.clearColor.getRGB() ? colorCode : 0;
    }

    /** {@inheritDoc} */
    @Override
	public int[] getPickColorsInRectangle(Rectangle rectangle, int[] minAndMaxColorCodes)
    {
        if (rectangle == null)
        {
            String msg = Logging.getMessage("nullValue.RectangleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Rectangle viewport = this.getView().getViewport();

        Rectangle r = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        r = r.intersection(viewport);

        if (r.isEmpty()) // Return null if the rectangle is empty.
            return null;

        if (minAndMaxColorCodes == null)
            minAndMaxColorCodes = new int[] {0, Integer.MAX_VALUE};

        // Allocate a native byte buffer to hold the framebuffer RGB colors.
        int numPixels = r.width * r.height;
        if (this.pixelColors == null || this.pixelColors.capacity() < 3 * numPixels)
            this.pixelColors = Buffers.newDirectByteBuffer(3 * numPixels);
        this.pixelColors.clear();

        GL gl = this.getGL();
        int[] packAlignment = new int[1];
        gl.glGetIntegerv(GL.GL_PACK_ALIGNMENT, packAlignment, 0);
        try
        {
            // Read the framebuffer colors in the specified rectangle as 24-bit RGB values. We're reading multiple rows
            // of pixels, and our row lengths are not aligned with the default 4-byte boundary, so we must set the GL
            // pack alignment state to 1.
            gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
            gl.glReadPixels(r.x, r.y, r.width, r.height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, this.pixelColors);
        }
        finally
        {
            // Restore the previous GL pack alignment state.
            gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, packAlignment[0]);
        }

        // Compute the set of unique color codes in the pick rectangle, ignoring the clear color and colors outside the
        // specified range. We place each color in a set to consolidates duplicate pick colors to a single entry. This
        // reduces the number of colors we need to return to the caller, and ensures that callers creating picked
        // objects based on the returned colors do not create duplicates.
        int clearColorCode = this.clearColor.getRGB();
        for (int i = 0; i < numPixels; i++)
        {
            int colorCode = ((this.pixelColors.get() & 0xff) << 16) // Red, bits 16-23
                | ((this.pixelColors.get() & 0xff) << 8) // Green, bits 8-16
                | (this.pixelColors.get() & 0xff); // Blue, bits 0-7

            // Add a 24-bit integer corresponding to each unique RGB color that's not the clear color, and is in the
            // specifies range of colors.
            if (colorCode != clearColorCode && colorCode >= minAndMaxColorCodes[0]
                && colorCode <= minAndMaxColorCodes[1])
            {
                this.uniquePixelColors.add(colorCode);
            }
        }

        // Copy the unique color set to an int array that we return to the caller, then clear the set to ensure that the
        // colors computed during this call do not affect the next call.
        int[] array = new int[this.uniquePixelColors.size()];
        this.uniquePixelColors.toArray(array);
        this.uniquePixelColors.clear();

        return array;
    }

    @Override
	public boolean isPickingMode()
    {
        return this.pickingMode;
    }

    @Override
	public void enablePickingMode()
    {
        this.pickingMode = true;
    }

    @Override
	public void disablePickingMode()
    {
        this.pickingMode = false;
    }

    @Override
	public boolean isDeepPickingEnabled()
    {
        return this.deepPickingMode;
    }

    @Override
	public void setDeepPickingEnabled(boolean tf)
    {
        this.deepPickingMode = tf;
    }

    @Override
	public boolean isPreRenderMode()
    {
        return preRenderMode;
    }

    @Override
	public void setPreRenderMode(boolean preRenderMode)
    {
        this.preRenderMode = preRenderMode;
    }

    @Override
	public boolean isOrderedRenderingMode()
    {
        return this.isOrderedRenderingMode;
    }

    @Override
	public void setOrderedRenderingMode(boolean tf)
    {
        this.isOrderedRenderingMode = tf;
    }

    @Override
	public DeclutteringTextRenderer getDeclutteringTextRenderer()
    {
        return declutteringTextRenderer;
    }

    @Override
    public boolean is2DGlobe()
    {
        return this.globe instanceof Globe2D;
    }

    @Override
    public boolean isContinuous2DGlobe()
    {
        return this.globe instanceof Globe2D && ((Globe2D) this.getGlobe()).isContinuous();
    }

    @Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable)
    {
        if (null == orderedRenderable)
        {
            String msg = Logging.getMessage("nullValue.OrderedRenderable");
            Logging.logger().warning(msg);
            return; // benign event
        }

        this.orderedRenderables.add(new OrderedRenderableEntry(orderedRenderable, System.nanoTime(), this));
    }

    /** {@inheritDoc} */
    @Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable, boolean isBehind)
    {
        if (null == orderedRenderable)
        {
            String msg = Logging.getMessage("nullValue.OrderedRenderable");
            Logging.logger().warning(msg);
            return; // benign event
        }

        // If the caller has specified that the ordered renderable should be treated as behind other ordered
        // renderables, then treat it as having an eye distance of Double.MAX_VALUE and ignore the actual eye distance.
        // If multiple ordered renderables are added in this way, they are drawn according to the order in which they
        // are added.
        double eyeDistance = isBehind ? Double.MAX_VALUE : orderedRenderable.getDistanceFromEye();
        this.orderedRenderables.add(
            new OrderedRenderableEntry(orderedRenderable, eyeDistance, System.nanoTime(), this));
    }

    @Override
	public OrderedRenderable peekOrderedRenderables()
    {
        OrderedRenderableEntry ore = this.orderedRenderables.peek();

        return ore != null ? ore.or : null;
    }

    @Override
	public OrderedRenderable pollOrderedRenderables()
    {
        OrderedRenderableEntry ore = this.orderedRenderables.poll();

        if (ore != null && this.isContinuous2DGlobe())
        {
            ((Globe2D) this.getGlobe()).setOffset(ore.globeOffset);
            this.setSurfaceGeometry(ore.surfaceGeometry);
        }

        return ore != null ? ore.or : null;
    }
//
//    public void applyDeclutterFilter2()
//    {
//        // Collect all the active declutterables.
//        ArrayList<OrderedRenderableEntry> declutterableArray = new ArrayList<OrderedRenderableEntry>();
//        for (OrderedRenderableEntry ore : this.orderedRenderables)
//        {
//            if (ore.or instanceof Declutterable && ((Declutterable) ore.or).isEnableDecluttering())
//                declutterableArray.add(ore);
//        }
//
//        // Sort the declutterables front-to-back.
//        Collections.sort(declutterableArray, new Comparator<OrderedRenderableEntry>()
//        {
//            public int compare(OrderedRenderableEntry orA, OrderedRenderableEntry orB)
//            {
//                double eA = orA.distanceFromEye;
//                double eB = orB.distanceFromEye;
//
//                return eA < eB ? -1 : eA == eB ? (orA.time < orB.time ? -1 : orA.time == orB.time ? 0 : 1) : 1;
//            }
//        });
//
//        if (declutterableArray.size() == 0)
//            return;
//
//        // Remove eliminated ordered renderables from the priority queue.
//        ClutterFilter clutterFilter = new ClutterFilter();
//
//        for (OrderedRenderableEntry ore : declutterableArray)
//        {
//            Rectangle2D bounds = ((Declutterable) ore.or).getBounds(this);
//
//            Rectangle2D intersectingRegion = clutterFilter.intersects(bounds);
//            if (intersectingRegion != null)
//                clutterFilter.addShape(intersectingRegion, (Declutterable) ore.or);
//            else if (bounds != null)
//                clutterFilter.addShape(bounds, (Declutterable) ore.or);
//
//            orderedRenderables.remove(ore);
//        }
//
//        clutterFilter.render(this);
//    }

    @Override
    public void setClutterFilter(ClutterFilter filter)
    {
        this.clutterFilter = filter;
    }

    @Override
    public ClutterFilter getClutterFilter()
    {
        return this.clutterFilter;
    }

    @Override
	public void applyClutterFilter()
    {
        if (this.getClutterFilter() == null)
            return;

        // Collect all the active declutterables.
        ArrayList<OrderedRenderableEntry> declutterableArray = new ArrayList<OrderedRenderableEntry>();
        for (OrderedRenderableEntry ore : this.orderedRenderables)
        {
            if (ore.or instanceof Declutterable && ((Declutterable) ore.or).isEnableDecluttering())
                declutterableArray.add(ore);
        }

        // Sort the declutterables front-to-back.
        Collections.sort(declutterableArray, new Comparator<OrderedRenderableEntry>()
        {
            @Override
			public int compare(OrderedRenderableEntry orA, OrderedRenderableEntry orB)
            {
                double eA = orA.distanceFromEye;
                double eB = orB.distanceFromEye;

                return eA < eB ? -1 : eA == eB ? (orA.time < orB.time ? -1 : orA.time == orB.time ? 0 : 1) : 1;
            }
        });

        if (declutterableArray.size() == 0)
            return;

        // Prepare the declutterable list for the filter and remove eliminated ordered renderables from the renderable
        // list. The clutter filter will add those it wants displayed back to the list, or it will add some other
        // representation.
        List<Declutterable> declutterables = new ArrayList<Declutterable>(declutterableArray.size());
        for (OrderedRenderableEntry ore : declutterableArray)
        {
            declutterables.add((Declutterable) ore.or);

            orderedRenderables.remove(ore);
        }

        // Tell the filter to apply itself and draw whatever it draws.
        this.getClutterFilter().apply(this, declutterables);
    }

    /** {@inheritDoc} */
    @Override
	public void addOrderedSurfaceRenderable(OrderedRenderable orderedRenderable)
    {
        if (orderedRenderable == null)
        {
            String msg = Logging.getMessage("nullValue.OrderedRenderable");
            Logging.logger().warning(msg);
            return; // benign event
        }

        this.orderedSurfaceRenderables.add(orderedRenderable);
    }

    /** {@inheritDoc} */
    @Override
	public Queue<OrderedRenderable> getOrderedSurfaceRenderables()
    {
        return this.orderedSurfaceRenderables;
    }
//
//    @Override
//    public void setGroupingFilters(Map<String, GroupingFilter> filters)
//    {
//        this.groupingFilters = filters;
//    }
//
//    protected static final String DEFAULT_GROUPING_FILTER_NAME = "Default";
//
//    @Override
//    public GroupingFilter getGroupingFilter(String filterName)
//    {
//        if (filterName == null)
//        {
//            GroupingFilter filter = this.groupingFilters.get(DEFAULT_GROUPING_FILTER_NAME);
//            if (filter == null)
//                this.groupingFilters.put(DEFAULT_GROUPING_FILTER_NAME, new GroupingFilter());
//
//            return filter;
//        }
//
//        return this.groupingFilters.get(filterName);
//    }
//
//    public void applyGroupingFilters()
//    {
//        for (GroupingFilter filter : this.groupingFilters.values())
//        {
//            filter.apply(this);
//        }
//    }

    @Override
	public void drawUnitQuad()
    {
        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2d(0d, 0d);
        gl.glVertex2d(1, 0d);
        gl.glVertex2d(1, 1);
        gl.glVertex2d(0d, 1);
        gl.glEnd();
    }

    @Override
	public void drawUnitQuad(TextureCoords texCoords)
    {
        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2d(texCoords.left(), texCoords.bottom());
        gl.glVertex2d(0d, 0d);
        gl.glTexCoord2d(texCoords.right(), texCoords.bottom());
        gl.glVertex2d(1, 0d);
        gl.glTexCoord2d(texCoords.right(), texCoords.top());
        gl.glVertex2d(1, 1);
        gl.glTexCoord2d(texCoords.left(), texCoords.top());
        gl.glVertex2d(0d, 1);
        gl.glEnd();
    }

    @Override
	public void drawUnitQuadOutline()
    {
        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex2d(0d, 0d);
        gl.glVertex2d(1, 0d);
        gl.glVertex2d(1, 1);
        gl.glVertex2d(0d, 1);
        gl.glEnd();
    }

    @Override
	public void drawNormals(float length, FloatBuffer vBuf, FloatBuffer nBuf)
    {
        if (vBuf == null || nBuf == null)
            return;

        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        vBuf.rewind();
        nBuf.rewind();

        gl.glBegin(GL2.GL_LINES);

        while (nBuf.hasRemaining())
        {
            float x = vBuf.get();
            float y = vBuf.get();
            float z = vBuf.get();
            float nx = nBuf.get() * length;
            float ny = nBuf.get() * length;
            float nz = nBuf.get() * length;

            gl.glVertex3f(x, y, z);
            gl.glVertex3f(x + nx, y + ny, z + nz);
        }

        gl.glEnd();
    }

    @Override
	public Vec4 getPointOnTerrain(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.getVisibleSector() == null)
            return null;

        if (!this.getVisibleSector().contains(latitude, longitude))
            return null;

        SectorGeometryList sectorGeometry = this.getSurfaceGeometry();
        if (sectorGeometry != null)
        {
            Vec4 p = sectorGeometry.getSurfacePoint(latitude, longitude);
            if (p != null)
                return p;
        }

        return null;
    }

    @Override
	public SurfaceTileRenderer getGeographicSurfaceTileRenderer()
    {
        return this.geographicSurfaceTileRenderer;
    }

    @Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return this.perFrameStatistics;
    }

    @Override
	public void setPerFrameStatisticsKeys(Set<String> statKeys, Collection<PerformanceStatistic> stats)
    {
        this.perFrameStatisticsKeys = statKeys;
        this.perFrameStatistics = stats;
    }

    @Override
	public Set<String> getPerFrameStatisticsKeys()
    {
        return perFrameStatisticsKeys;
    }

    @Override
	public void setPerFrameStatistic(String key, String displayName, Object value)
    {
        if (this.perFrameStatistics == null || this.perFrameStatisticsKeys == null)
            return;

        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (displayName == null)
        {
            String message = Logging.getMessage("nullValue.DisplayNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.perFrameStatisticsKeys.contains(key) || this.perFrameStatisticsKeys.contains(PerformanceStatistic.ALL))
            this.perFrameStatistics.add(new PerformanceStatistic(key, displayName, value));
    }

    @Override
	public void setPerFrameStatistics(Collection<PerformanceStatistic> stats)
    {
        if (stats == null)
        {
            String message = Logging.getMessage("nullValue.ListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.perFrameStatistics == null || this.perFrameStatisticsKeys == null)
            return;

        for (PerformanceStatistic stat : stats)
        {
            this.perFrameStatistics.add(stat);
        }
    }

    @Override
	public long getFrameTimeStamp()
    {
        return this.frameTimestamp;
    }

    @Override
	public void setFrameTimeStamp(long frameTimeStamp)
    {
        this.frameTimestamp = frameTimeStamp;
    }

    @Override
	public List<Sector> getVisibleSectors(double[] resolutions, long timeLimit, Sector sector)
    {
        if (resolutions == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (timeLimit <= 0)
        {
            String message = Logging.getMessage("generic.TimeNegative", timeLimit);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
            sector = this.visibleSector;

        if (this.visibleSectors == null)
            this.visibleSectors = new SectorVisibilityTree();
        else if (this.visibleSectors.getSectorSize() == resolutions[resolutions.length - 1]
            && this.visibleSectors.getTimeStamp() == this.frameTimestamp)
            return this.visibleSectors.getSectors();

        long start = System.currentTimeMillis();
        List<Sector> sectors = this.visibleSectors.refresh(this, resolutions[0], sector);
        for (int i = 1; i < resolutions.length && (System.currentTimeMillis() < start + timeLimit); i++)
        {
            sectors = this.visibleSectors.refresh(this, resolutions[i], sectors);
        }

        this.visibleSectors.setTimeStamp(this.frameTimestamp);

        return this.visibleSectors.getSectors();
    }

    @Override
	public void setCurrentLayer(Layer layer)
    {
        this.currentLayer = layer;
    }

    @Override
	public Layer getCurrentLayer()
    {
        return this.currentLayer;
    }

    protected LinkedHashMap<ScreenCredit, Long> credits = new LinkedHashMap<ScreenCredit, Long>();

    @Override
	public void addScreenCredit(ScreenCredit credit)
    {
        if (credit == null)
        {
            String message = Logging.getMessage("nullValue.ScreenCreditIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.credits.put(credit, this.frameTimestamp);
    }

    @Override
	public Map<ScreenCredit, Long> getScreenCredits()
    {
        return this.credits;
    }

    @Override
	public int getRedrawRequested()
    {
        return redrawRequested;
    }

    @Override
	public void setRedrawRequested(int redrawRequested)
    {
        this.redrawRequested = redrawRequested;
    }

    @Override
	public PickPointFrustumList getPickFrustums()
    {
        return this.pickFrustumList;
    }

    @Override
	public void setPickPointFrustumDimension(Dimension dim)
    {
        if (dim == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dim.width < 3 || dim.height < 3)
        {
            String message = Logging.getMessage("DrawContext.PickPointFrustumDimensionTooSmall");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pickPointFrustumDimension = new Dimension(dim);
    }

    @Override
	public Dimension getPickPointFrustumDimension()
    {
        return this.pickPointFrustumDimension;
    }

    @Override
	public void addPickPointFrustum()
    {
        //Compute the current picking frustum
        if (getPickPoint() != null)
        {
            Point ptCenter = new Point(getPickPoint());
            ptCenter.translate(-viewportCenterScreenPoint.x, -viewportCenterScreenPoint.y);

            //Number of pixels around pick point to include in frustum
            int offsetX = pickPointFrustumDimension.width / 2;
            int offsetY = pickPointFrustumDimension.height / 2;

            //If the frustum is not valid then don't add it and return silently
            if (offsetX == 0 || offsetY == 0)
                return;

            //Compute the distance to the near plane in screen coordinates
            double width = getView().getFieldOfView().tanHalfAngle() * getView().getNearClipDistance();
            double viewportWidth = getView().getViewport().getWidth();
            if (viewportWidth <= 0.0) viewportWidth = 1.0;
            double x = width / (viewportWidth/2);
            double screenDist = getView().getNearClipDistance() / x;

            //Create the four vectors that define the top-left, top-right, bottom-left, and bottom-right vectors
            Vec4 vTL = new Vec4(ptCenter.x - offsetX, ptCenter.y + offsetY, -screenDist);
            Vec4 vTR = new Vec4(ptCenter.x + offsetX, ptCenter.y + offsetY, -screenDist);
            Vec4 vBL = new Vec4(ptCenter.x - offsetX, ptCenter.y - offsetY, -screenDist);
            Vec4 vBR = new Vec4(ptCenter.x + offsetX, ptCenter.y - offsetY, -screenDist);

            //Compute the frustum from these four vectors
            Frustum frustum = Frustum.fromPerspectiveVecs(vTL, vTR, vBL, vBR,
                getView().getNearClipDistance(), getView().getFarClipDistance());

            //Create the screen rectangle associated with this frustum
            Rectangle rectScreen = new Rectangle(getPickPoint().x - offsetX + 1,
            									 getPickPoint().y - offsetY - 1,
            									 pickPointFrustumDimension.width,
            									 pickPointFrustumDimension.height);

            //Transform the frustum to Model Coordinates
            Matrix modelviewTranspose = getView().getModelviewMatrix().getTranspose();
            if (modelviewTranspose != null)
                frustum = frustum.transformBy(modelviewTranspose);

            this.pickFrustumList.add(new PickPointFrustum(frustum, rectScreen));
        }
    }

    @Override
	public void addPickRectangleFrustum()
    {
        // Do nothing if the pick rectangle is either null or has zero dimension.
        if (this.getPickRectangle() == null || this.getPickRectangle().isEmpty())
            return;

        // Get the pick rectangle, transform it from GL surface coordinates to OpenGL screen coordinates, then translate
        // it such that the screen's center is at the origin.
        Rectangle pr = new Rectangle(this.getPickRectangle());
        pr.translate(-viewportCenterScreenPoint.x, -viewportCenterScreenPoint.y);

        double viewportWidth = view.getViewport().getWidth();
        if (viewportWidth <= 0.0) viewportWidth = 1.0;
      
        // Create the four vectors that define the top-left, top-right, bottom-left, and bottom-right corners of the
        // pick rectangle in screen coordinates.
        double screenDist = viewportWidth / (2 * view.getFieldOfView().tanHalfAngle());
        Vec4 vTL = new Vec4(pr.getMinX(), pr.getMaxY(), -screenDist);
        Vec4 vTR = new Vec4(pr.getMaxX(), pr.getMaxY(), -screenDist);
        Vec4 vBL = new Vec4(pr.getMinX(), pr.getMinY(), -screenDist);
        Vec4 vBR = new Vec4(pr.getMaxX(), pr.getMinY(), -screenDist);

        // Compute the frustum from these four vectors.
        Frustum frustum = Frustum.fromPerspectiveVecs(vTL, vTR, vBL, vBR, view.getNearClipDistance(),
            view.getFarClipDistance());

        // Transform the frustum from eye coordinates to model coordinates.
        Matrix modelviewTranspose = view.getModelviewMatrix().getTranspose();
        if (modelviewTranspose != null)
            frustum = frustum.transformBy(modelviewTranspose);

        // Create the screen rectangle in OpenGL screen coordinates associated with this frustum. We translate the
        // specified pick rectangle from AWT coordinates to GL coordinates by inverting the y axis.
        Rectangle screenRect = new Rectangle(this.getPickRectangle());

        this.pickFrustumList.add(new PickPointFrustum(frustum, screenRect));
    }

    @Override
	public Collection<Throwable> getRenderingExceptions()
    {
        return this.renderingExceptions;
    }

    @Override
	public void setRenderingExceptions(Collection<Throwable> exceptions)
    {
        this.renderingExceptions = exceptions;
    }

    @Override
	public void addRenderingException(Throwable t)
    {
        // If the renderingExceptions Collection is non-null, it's used as the data structure that accumulates rendering
        // exceptions. Otherwise this DrawContext ignores all rendering exceptions passed to this method.
        if (this.renderingExceptions == null)
            return;

        if (t == null)
        {
            String message = Logging.getMessage("nullValue.ThrowableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.renderingExceptions.add(t);
    }

    @Override
	public void pushProjectionOffest(Double offset)
    {
        // Modify the projection transform to shift the depth values slightly toward the camera in order to
        // ensure the lines are selected during depth buffering.
        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        float[] pm = new float[16];
        gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, pm, 0);
        pm[10] *= offset != null ? offset : 0.99; // TODO: See Lengyel 2 ed. Section 9.1.2 to compute optimal offset

        gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadMatrixf(pm, 0);
    }

    @Override
	public void popProjectionOffest()
    {
        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glPopAttrib();
    }

    public static final float DEFAULT_DEPTH_OFFSET_FACTOR = 1f;
    public static final float DEFAULT_DEPTH_OFFSET_UNITS = 1f;

    @Override
	public void drawOutlinedShape(OutlinedShape renderer, Object shape)
    {
        // Draw the outlined shape using a multiple pass algorithm. The motivation for this algorithm is twofold:
        //
        // * The outline appears both in front of and behind the shape. If the outline is drawn using GL line smoothing
        // or GL blending, then either the line must be broken into two parts, or rendered in two passes.
        //
        // * If depth offset is enabled, we want draw the shape on top of other intersecting shapes with similar depth
        // values to eliminate z-fighting between shapes. However we do not wish to offset both the depth and color
        // values, which would cause a cascading increase in depth offset when many shapes are drawn.
        //
        // These issues are resolved by making several passes for the interior and outline, as follows:

        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (this.isDeepPickingEnabled())
        {
            if (renderer.isDrawInterior(this, shape))
                renderer.drawInterior(this, shape);

            if (renderer.isDrawOutline(this, shape)) // the line might extend outside the interior's projection
                renderer.drawOutline(this, shape);

            return;
        }

        // Optimize the outline-only case.
        if (renderer.isDrawOutline(this, shape) && !renderer.isDrawInterior(this, shape))
        {
            renderer.drawOutline(this, shape);
            return;
        }

        OGLStackHandler ogsh = new OGLStackHandler();
        int attribMask = GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_POLYGON_BIT;
        ogsh.pushAttrib(gl, attribMask);

        try
        {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(GL.GL_LEQUAL);

            // If the outline and interior are enabled, then draw the outline but do not affect the depth buffer. The
            // fill pixels contribute the depth values. When the interior is drawn, it draws on top of these colors, and
            // the outline is be visible behind the potentially transparent interior.
            if (renderer.isDrawOutline(this, shape) && renderer.isDrawInterior(this, shape))
            {
                gl.glColorMask(true, true, true, true);
                gl.glDepthMask(false);

                renderer.drawOutline(this, shape);
            }

            // If the interior is enabled, then make two passes as follows. The first pass draws the interior depth
            // values with a depth offset (likely away from the eye). This enables the shape to contribute to the
            // depth buffer and occlude other geometries as it normally would. The second pass draws the interior color
            // values without a depth offset, and does not affect the depth buffer. This giving the shape outline depth
            // priority over the fill, and gives the fill depth priority over other shapes drawn with depth offset
            // enabled. By drawing the colors without depth offset, we avoid the problem of having to use ever
            // increasing depth offsets.
            if (renderer.isDrawInterior(this, shape))
            {
                if (renderer.isEnableDepthOffset(this, shape))
                {
                    // Draw depth.
                    gl.glColorMask(false, false, false, false);
                    gl.glDepthMask(true);
                    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
                    Double depthOffsetFactor = renderer.getDepthOffsetFactor(this, shape);
                    Double depthOffsetUnits = renderer.getDepthOffsetUnits(this, shape);
                    gl.glPolygonOffset(
                        depthOffsetFactor != null ? depthOffsetFactor.floatValue() : DEFAULT_DEPTH_OFFSET_FACTOR,
                        depthOffsetUnits != null ? depthOffsetUnits.floatValue() : DEFAULT_DEPTH_OFFSET_UNITS);

                    renderer.drawInterior(this, shape);

                    // Draw color.
                    gl.glColorMask(true, true, true, true);
                    gl.glDepthMask(false);
                    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);

                    renderer.drawInterior(this, shape);
                }
                else
                {
                    gl.glColorMask(true, true, true, true);
                    gl.glDepthMask(true);

                    renderer.drawInterior(this, shape);
                }
            }

            // If the outline is enabled, then draw the outline color and depth values. This blends outline colors with
            // the interior colors.
            if (renderer.isDrawOutline(this, shape))
            {
                gl.glColorMask(true, true, true, true);
                gl.glDepthMask(true);

                renderer.drawOutline(this, shape);
            }
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    @Override
	public void beginStandardLighting()
    {
        if (this.standardLighting != null)
        {
            this.standardLighting.beginLighting(this);
            this.getGL().glEnable(GL2.GL_LIGHTING);
        }
    }

    @Override
	public void endStandardLighting()
    {
        if (this.standardLighting != null)
        {
            this.standardLighting.endLighting(this);
        }
    }

    @Override
	public boolean isSmall(Extent extent, int numPixels)
    {
        return extent != null && extent.getDiameter() <= numPixels * this.getView().computePixelSizeAtDistance(
            // burkey couldnt we make this minimum dimension
            this.getView().getEyePoint().distanceTo3(
                extent.getCenter()));                                                    // -- so box could return small when one dim is narrow?
    }                                                                                                                           // i see really skinny telephone poles that dont need to be rendered at distance but  are tall

    @Override
	public Terrain getTerrain()
    {
        return this.terrain;
    }

    @Override
	public Vec4 computeTerrainPoint(Angle lat, Angle lon, double offset)
    {
        return this.getTerrain().getSurfacePoint(lat, lon, offset);
    }

    protected Terrain terrain = new Terrain()
    {
        @Override
		public Globe getGlobe()
        {
            return DrawContextImpl.this.getGlobe();
        }

        @Override
		public double getVerticalExaggeration()
        {
            return DrawContextImpl.this.getVerticalExaggeration();
        }

        @Override
		public Vec4 getSurfacePoint(Position position)
        {
            if (position == null)
            {
                String msg = Logging.getMessage("nullValue.PositionIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            SectorGeometryList sectorGeometry = DrawContextImpl.this.getSurfaceGeometry();
            if (sectorGeometry == null)
                return null;

            Vec4 pt = sectorGeometry.getSurfacePoint(position);
            if (pt == null)
            {
                double elevation = this.getGlobe().getElevation(position.getLatitude(), position.getLongitude());
                pt = this.getGlobe().computePointFromPosition(position,
                    position.getAltitude() + elevation * this.getVerticalExaggeration());
            }

            return pt;
        }

        @Override
		public Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset)
        {
            if (latitude == null || longitude == null)
            {
                String msg = Logging.getMessage("nullValue.LatLonIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            SectorGeometryList sectorGeometry = DrawContextImpl.this.getSurfaceGeometry();
            if (sectorGeometry == null)
                return null;

            Vec4 pt = sectorGeometry.getSurfacePoint(latitude, longitude, metersOffset);

            if (pt == null)
            {
                double elevation = this.getGlobe().getElevation(latitude, longitude);
                pt = this.getGlobe().computePointFromPosition(latitude, longitude,
                    metersOffset + elevation * this.getVerticalExaggeration());
            }

            return pt;
        }

        @Override
		public Intersection[] intersect(Position pA, Position pB)
        {
            SectorGeometryList sectorGeometry = DrawContextImpl.this.getSurfaceGeometry();
            if (sectorGeometry == null)
                return null;

            Vec4 ptA = this.getSurfacePoint(pA);
            Vec4 ptB = this.getSurfacePoint(pB);

            if (pA == null || pB == null)
                return null;

            return sectorGeometry.intersect(new Line(ptA, ptB.subtract3(ptA)));
        }

        @Override
		public Intersection[] intersect(Position pA, Position pB, int altitudeMode)
        {
            if (pA == null || pB == null)
            {
                String msg = Logging.getMessage("nullValue.PositionIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            // The intersect method expects altitudes to be relative to ground, so make them so if they aren't already.
            double altitudeA = pA.getAltitude();
            double altitudeB = pB.getAltitude();
            if (altitudeMode == WorldWind.ABSOLUTE)
            {
                altitudeA -= this.getElevation(pA);
                altitudeB -= this.getElevation(pB);
            }
            else if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
            {
                altitudeA = 0;
                altitudeB = 0;
            }

            return this.intersect(new Position(pA, altitudeA), new Position(pB, altitudeB));
        }

        @Override
		public Double getElevation(LatLon location)
        {
            if (location == null)
            {
                String msg = Logging.getMessage("nullValue.LatLonIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            Vec4 pt = this.getSurfacePoint(location.getLatitude(), location.getLongitude(), 0);
            if (pt == null)
                return null;

            Vec4 p = this.getGlobe().computePointFromPosition(location.getLatitude(), location.getLongitude(), 0);

            return p.distanceTo3(pt);
        }
    };

    @Override
	public void restoreDefaultBlending()
    {
        this.getGL().glBlendFunc(GL.GL_ONE, GL.GL_ZERO);
        this.getGL().glDisable(GL.GL_BLEND);
    }

    @Override
	public void restoreDefaultCurrentColor()
    {
        GL2 gl = this.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor4f(1, 1, 1, 1);
    }

    @Override
	public void restoreDefaultDepthTesting()
    {
        this.getGL().glEnable(GL.GL_DEPTH_TEST);
        this.getGL().glDepthMask(true);
    }

    @Override
	public Vec4 computePointFromPosition(Position position, int altitudeMode)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 point;

        if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
        {
            point = this.computeTerrainPoint(position.getLatitude(), position.getLongitude(), 0);
        }
        else if (altitudeMode == WorldWind.RELATIVE_TO_GROUND)
        {
            point = this.computeTerrainPoint(position.getLatitude(), position.getLongitude(),
                position.getAltitude());
        }
        else  // ABSOLUTE
        {
            double height = position.getElevation() * this.getVerticalExaggeration();
            point = this.getGlobe().computePointFromPosition(position.getLatitude(),
                position.getLongitude(), height);
        }

        return point;
    }

    /** {@inheritDoc} */
	@Override
	public int [] awtPointToGLpoint(Point pt) {
		if (pt == null) return null;
		
		int [] awtPt = { pt.x, pt.y };
		if (glContext == null) return awtPt;
	
		GLDrawable drawable = glContext.getGLDrawable();
		if (drawable == null) return awtPt;

		// Convert to GL surface coordinates
		int [] glSurfacePt = drawable.getNativeSurface().convertToPixelUnits(awtPt);
		int glSurfaceHeight = drawable.getSurfaceHeight();
		glSurfacePt[1] = glSurfaceHeight-1 - glSurfacePt[1];
		return glSurfacePt;
	}
	    
    public Point glPointToAwtPoint(Point glPoint) {
		GLDrawable drawable = glContext.getGLDrawable();
		if (drawable == null) return glPoint;

		final int viewportHeight = getView().getViewport().height;
		int [] glPt = { glPoint.x, viewportHeight-1 - glPoint.y };
        getGLDrawable().getNativeSurface().convertToWindowUnits(glPt);
        return new Point(glPt[0], glPt[1]);    	
    }
}
