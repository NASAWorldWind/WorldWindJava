/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.lineofsight;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: PointGrid.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PointGrid extends WWObjectImpl implements OrderedRenderable, Highlightable
{
    /** The scale to use when highlighting if no highlight attributes are specified. */
    protected static final Double DEFAULT_HIGHLIGHT_SCALE = 1.3;
    /** The point size to use when none is specified. */
    protected static final Double DEFAULT_POINT_SIZE = 10d;
    /** The color to use when none is specified */
    protected static final Color DEFAULT_POINT_COLOR = Color.YELLOW;
    /** The highlight color to use when none is specified */
    protected static final Color DEFAULT_HIGHLIGHT_POINT_COLOR = Color.WHITE;
    /** The default geometry regeneration interval, in milliseconds. */
    protected static final long DEFAULT_GEOMETRY_GENERATION_INTERVAL = 5000;

    /** The attributes used if attributes are not specified. */
    protected static final Attributes defaultAttributes =
        new Attributes(DEFAULT_POINT_SIZE, DEFAULT_POINT_COLOR);

    protected static class Attributes
    {
        protected Double pointSize;
        protected Color pointColor;
        protected boolean enablePointSmoothing = true;

        public Attributes()
        {
        }

        public Attributes(Double scale, Color pointColor)
        {
            this.pointSize = scale;
            this.pointColor = pointColor;
        }

        public void copy(Attributes attrs)
        {
            if (attrs != null)
            {
                this.setPointSize(attrs.getPointSize());
                this.setPointColor(attrs.getPointColor());
            }
        }

        public Double getPointSize()
        {
            return this.pointSize;
        }

        public void setPointSize(Double pointSize)
        {
            this.pointSize = pointSize;
        }

        public Color getPointColor()
        {
            return this.pointColor;
        }

        public void setPointColor(Color pointColor)
        {
            this.pointColor = pointColor;
        }

        public boolean isEnablePointSmoothing()
        {
            return enablePointSmoothing;
        }

        public void setEnablePointSmoothing(Boolean enablePointSmoothing)
        {
            this.enablePointSmoothing = enablePointSmoothing;
        }
    }

    protected List<Position> corners;
    protected Iterable<? extends Position> positions;
    protected int numPositions;

    protected boolean highlighted;
    protected boolean highlightOnePosition = true;
    protected boolean visible = true;
    protected int altitudeMode = WorldWind.CLAMP_TO_GROUND;
    protected boolean applyVerticalExaggeration = true;
    protected long geometryRegenerationInterval = DEFAULT_GEOMETRY_GENERATION_INTERVAL;

    protected Attributes normalAttrs;
    protected Attributes highlightAttrs;
    protected Attributes activeAttributes = new Attributes(); // re-determined each frame

    protected Extent extent;
    protected double eyeDistance;
    protected FloatBuffer currentPoints;

    protected PickSupport pickSupport = new PickSupport();
    protected Layer pickLayer;

    // Values computed once per frame and reused during the frame as needed.
    protected long frameID; // the ID of the most recent rendering frame
    protected double previousExaggeration = -1;
    protected long visGeomRegenFrame = -1;

    public PointGrid()
    {
    }

    public PointGrid(List<Position> corners, Iterable<? extends Position> positions, Integer numPositions)
    {
        this.corners = corners;
        this.setPositions(positions, numPositions);
    }

    public List<Position> getCorners()
    {
        return corners;
    }

    public void setCorners(List<Position> corners)
    {
        this.corners = corners;
    }

    public int getNumPositions()
    {
        return this.numPositions;
    }

    public Iterable<? extends Position> getPositions()
    {
        return this.positions;
    }

    public void setPositions(Iterable<? extends Position> positions, Integer numPositions)
    {
        if (numPositions == null)
        {
            this.numPositions = 0;
            Iterator<? extends Position> posIter = positions.iterator();
            while (posIter.hasNext())
            {
                ++this.numPositions;
            }
        }
        else
        {
            this.numPositions = numPositions;
        }

        this.positions = positions;
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /** {@inheritDoc} */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    public boolean isHighlightOnePosition()
    {
        return highlightOnePosition;
    }

    public void setHighlightOnePosition(boolean highlightOnePosition)
    {
        this.highlightOnePosition = highlightOnePosition;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }

    public boolean isApplyVerticalExaggeration()
    {
        return applyVerticalExaggeration;
    }

    public void setApplyVerticalExaggeration(boolean applyVerticalExaggeration)
    {
        this.applyVerticalExaggeration = applyVerticalExaggeration;
    }

    public long getGeometryRegenerationInterval()
    {
        return geometryRegenerationInterval;
    }

    public Attributes getAttributes()
    {
        return this.normalAttrs;
    }

    public void setAttributes(Attributes normalAttrs)
    {
        this.normalAttrs = normalAttrs;
    }

    public Attributes getHighlightAttributes()
    {
        return highlightAttrs;
    }

    public void setHighlightAttributes(Attributes highlightAttrs)
    {
        this.highlightAttrs = highlightAttrs;
    }

    public Attributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    @SuppressWarnings({"UnusedDeclaration", "SimplifiableIfStatement"})
    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        if (this.currentPoints == null || dc.getVerticalExaggeration() != this.previousExaggeration)
            return true;

        return this.getAltitudeMode() != WorldWind.ABSOLUTE
            && this.frameID - this.visGeomRegenFrame > this.getGeometryRegenerationInterval();
    }

    protected void determineActiveAttributes()
    {
        Attributes actAttrs = this.getActiveAttributes();

        if (this.isHighlighted() && !this.isHighlightOnePosition())
        {
            if (this.getHighlightAttributes() != null)
            {
                actAttrs.copy(this.getHighlightAttributes());
            }
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // for highlighting.
                if (this.getAttributes() != null)
                {
                    actAttrs.copy(this.getAttributes());
                    if (getAttributes().getPointSize() != null)
                        actAttrs.setPointSize(DEFAULT_HIGHLIGHT_SCALE * this.getAttributes().getPointSize());
                }
                else
                {
                    actAttrs.copy(defaultAttributes);
                    if (defaultAttributes.getPointSize() != null)
                        actAttrs.setPointSize(DEFAULT_HIGHLIGHT_SCALE * defaultAttributes.getPointSize());
                }
            }

            if (actAttrs.getPointSize() == null)
                actAttrs.setPointSize(defaultAttributes.getPointSize() * DEFAULT_HIGHLIGHT_SCALE);
            if (actAttrs.getPointColor() == null)
                actAttrs.setPointColor(DEFAULT_HIGHLIGHT_POINT_COLOR);
        }
        else if (this.getAttributes() != null)
        {
            actAttrs.copy(this.getAttributes());
        }
        else
        {
            actAttrs.copy(defaultAttributes);
        }
    }

    protected Color determineHighlightColor()
    {
        Color color = null;

        if (this.getHighlightAttributes() != null)
            color = this.getHighlightAttributes().getPointColor();
        else if (this.getAttributes() != null)
            color = this.getAttributes().getPointColor();

        return color != null ? color : DEFAULT_HIGHLIGHT_POINT_COLOR;
    }

    protected Double determineHighlightPointSize()
    {
        Double size = null;

        if (this.getHighlightAttributes() != null)
        {
            size = this.getHighlightAttributes().getPointSize();
        }
        else if (this.getAttributes() != null)
        {
            size = this.getAttributes().getPointSize();
            if (size != null)
                size = size * DEFAULT_HIGHLIGHT_SCALE;
        }

        return size != null ? size : DEFAULT_HIGHLIGHT_SCALE * DEFAULT_POINT_SIZE;
    }

    /** {@inheritDoc} */
    public double getDistanceFromEye()
    {
        return this.eyeDistance;
    }

    public Extent getExtent()
    {
        return this.extent;
    }

    protected void setExtent(Extent extent)
    {
        this.extent = extent;
    }

    protected Extent computeExtentAndEyeDistance(DrawContext dc)
    {
        if (WWUtil.isEmpty(this.corners))
            return null;

        List<Vec4> cornerPoints = new ArrayList<Vec4>(this.getCorners().size());

        this.eyeDistance = Double.MAX_VALUE;

        for (Position pos : this.corners)
        {
            if (pos == null)
                continue;

            Vec4 pt = this.computePoint(dc, pos);
            if (pt == null)
                continue;

            cornerPoints.add(pt);

            double d = pt.distanceTo3(dc.getView().getEyePoint());
            if (d < this.eyeDistance)
                this.eyeDistance = d;
        }

        return Box.computeBoundingBox(cornerPoints);
    }

    /** {@inheritDoc} */
    public void pick(DrawContext dc, Point pickPoint)
    {
        // This method is called only when ordered renderables are being drawn.
        // Arg checked within call to render.

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.render(dc);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        // This render method is called three times during frame generation. It's first called as a {@link Renderable}
        // during <code>Renderable</code> picking. It's called again during normal rendering. And it's called a third
        // time as an OrderedRenderable. The first two calls determine whether to add the placemark  and its optional
        // line to the ordered renderable list during pick and render. The third call just draws the ordered renderable.
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getSurfaceGeometry() == null)
            return;

        if (!this.isVisible())
            return;

        if (dc.isOrderedRenderingMode())
            this.drawOrderedRenderable(dc);
        else
            this.makeOrderedRenderable(dc);

        this.frameID = dc.getFrameTimeStamp();
    }

    /**
     * If the scene controller is rendering ordered renderables, this method draws this placemark's image as an ordered
     * renderable. Otherwise the method determines whether this instance should be added to the ordered renderable
     * list.
     * <p/>
     * The Cartesian and screen points of the placemark are computed during the first call per frame and re-used in
     * subsequent calls of that frame.
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Determine whether to queue an ordered renderable for the grid.

        // Re-use values already calculated this frame.
        if (this.mustRegenerateGeometry(dc))
        {
            this.extent = this.computeExtentAndEyeDistance(dc);
            if (!this.intersectsFrustum(dc))
                return;

            this.currentPoints = this.computeGridPoints(dc, this.currentPoints);
            if (this.currentPoints == null || this.currentPoints.limit() == 0)
                return;

            this.determineActiveAttributes();

            this.visGeomRegenFrame = dc.getFrameTimeStamp();
            this.previousExaggeration = dc.getVerticalExaggeration();
        }

        if (this.intersectsFrustum(dc))
        {
            if (dc.isPickingMode())
                this.pickLayer = dc.getCurrentLayer();

            dc.addOrderedRenderable(this); // add the image ordered renderable
        }
    }

    /**
     * Draws the path as an ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void drawOrderedRenderable(DrawContext dc)
    {
        if (this.currentPoints == null) // verify that we have something to draw
            return;

        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    /**
     * Establish the OpenGL state needed to draw Paths.
     *
     * @param dc the current draw context.
     */
    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attrMask = GL2.GL_POINT_BIT
            | GL2.GL_CURRENT_BIT // for current color
            | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
            | GL2.GL_ENABLE_BIT; // for enable/disable changes

        if (dc.isPickingMode())
            attrMask |=
                GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
                    | GL2.GL_TRANSFORM_BIT // for modelview and perspective
                    | GL2.GL_VIEWPORT_BIT // for depth range
                    | GL2.GL_DEPTH_BUFFER_BIT; // for depth func

        gl.glPushAttrib(attrMask);

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
        }
    }

    /**
     * Pop the state set in beginDrawing.
     *
     * @param dc the current draw context.
     */
    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glPopAttrib();
    }

    /**
     * Draws the points as an ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void doDrawOrderedRenderable(DrawContext dc)
    {
        if (dc.isPickingMode())
            this.pickPoints(dc);
        else
            this.drawPoints(dc);
    }

    protected void pickPoints(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler osh = new OGLStackHandler();
        try
        {
            this.setPointSize(dc, null);

            // The point is drawn using a parallel projection.
            osh.pushProjectionIdentity(gl);
            gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

            osh.pushModelviewIdentity(gl);

            // Apply the depth buffer but don't change it (for screen-space shapes).
            if ((!dc.isDeepPickingEnabled()))
                gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);
            gl.glDepthFunc(GL.GL_LESS);

            Iterator<? extends Position> posIter = this.positions.iterator();

            FloatBuffer points = this.currentPoints;
            points.rewind();
            gl.glBegin(GL2.GL_POINTS);
            try
            {
                while (points.hasRemaining())
                {
                    Position position = posIter.next();
                    Vec4 pt = new Vec4(points.get(), points.get(), points.get());

                    if (!dc.getView().getFrustumInModelCoordinates().contains(pt))
                        continue;

                    Vec4 sp = dc.getView().project(pt);

                    if (!dc.getPickFrustums().containsInAny(sp.x, sp.y))
                        continue;

                    // Adjust depth of point to bring it slightly forward
                    double depth = sp.z - (8d * 0.00048875809d);
                    depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
                    gl.glDepthRange(depth, depth);

                    Color pickColor = dc.getUniquePickColor();
                    this.pickSupport.addPickableObject(pickColor.getRGB(), this, position);
                    gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());

                    gl.glVertex3d(sp.x, sp.y, 0);
                }
            }
            finally
            {
                gl.glEnd();
            }

            gl.glDepthRange(0, 1); // reset depth range to the OGL default
        }
        finally
        {
            osh.pop(gl);
        }
    }

    protected void drawPoints(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.setPointColor(dc, null);
        this.setPointSize(dc, null);

        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        dc.pushProjectionOffest(0.99);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, this.currentPoints.rewind());
        gl.glDrawArrays(GL.GL_POINTS, 0, this.currentPoints.limit() / 3);
        dc.popProjectionOffest();
        gl.glPopClientAttrib();

        if (dc.getPickedObjects().getTopObject() == this)
        {
            Position pickedPosition = dc.getPickedObjects().getTopPickedObject().getPosition();
            if (pickedPosition != null)
            {
                Vec4 highlightPoint = this.computePoint(dc, pickedPosition);
                if (highlightPoint != null)
                {
                    // Set up to draw a picked grid position in the highlight color.
                    this.setPointColor(dc, this.determineHighlightColor());
                    this.setPointSize(dc, this.determineHighlightPointSize());

                    dc.pushProjectionOffest(0.98);
                    gl.glBegin(GL2.GL_POINTS);
                    gl.glVertex3d(highlightPoint.x, highlightPoint.y, highlightPoint.z);
                    gl.glEnd();
                    dc.popProjectionOffest();
                }
            }
        }
    }

    protected void setPointColor(DrawContext dc, Color color)
    {
        if (color == null)
            color = this.getActiveAttributes().getPointColor();

        if (color == null)
            color = DEFAULT_POINT_COLOR;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) color.getAlpha());
    }

    protected void setPointSize(DrawContext dc, Double size)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (size == null)
            size = this.getActiveAttributes().getPointSize();

        if (size == null)
            size = DEFAULT_POINT_SIZE;

        double altitude = dc.getView().getEyePosition().getAltitude();
        if (dc.isPickingMode())
        {
            size *= 2; // makes points easier to pick
        }
        else if (altitude > 100e3)
        {
            if (altitude < 200e3)
                size = Math.max(0.8 * size, 1);
            else if (altitude < 400e3)
                size = Math.max(0.6 * size, 1);
            else if (altitude < 700e3)
                size = Math.max(0.4 * size, 1);
            else if (altitude < 1e6)
                size = Math.max(0.2 * size, 1);
            else
                size = Math.max(0.1 * size, 1);
        }

        gl.glPointSize(size.floatValue());

        if (!dc.isPickingMode() && this.getActiveAttributes().isEnablePointSmoothing())
        {
            gl.glEnable(GL2.GL_POINT_SMOOTH);
            gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
        }
    }

    /**
     * Determines whether the points intersect the view frustum.
     *
     * @param dc the current draw context.
     *
     * @return true if the points intersect the frustum, otherwise false.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        if (this.getExtent() == null)
        {
            this.setExtent(this.computeExtentAndEyeDistance(dc));
            if (this.getExtent() == null)
                return false;
        }

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(this.getExtent());
        else
            return dc.getView().getFrustumInModelCoordinates().intersects(this.getExtent());
    }

    protected FloatBuffer computeGridPoints(DrawContext dc, FloatBuffer coords)
    {
        int numCoords = 3 * this.numPositions;

        if (coords == null || coords.capacity() < numCoords || coords.capacity() > 1.5 * numCoords)
            coords = Buffers.newDirectFloatBuffer(numCoords);
        coords.rewind();

        Iterator<? extends Position> posIter = this.positions.iterator();

        while (posIter.hasNext())
        {
            Vec4 pt = this.computePoint(dc, posIter.next());
            if (pt == null)
                continue;

            coords.put((float) pt.x).put((float) pt.y).put((float) pt.z);
        }

        coords.flip();

        return coords;
    }

    protected Vec4 computePoint(DrawContext dc, Position pos)
    {
        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return dc.getTerrain().getSurfacePoint(pos.getLatitude(), pos.getLongitude(), 0);

        if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            return dc.getTerrain().getSurfacePoint(pos);

        if (this.applyVerticalExaggeration)
            pos = new Position(pos, dc.getVerticalExaggeration() * pos.getAltitude());

        return dc.getGlobe().computePointFromPosition(pos);
    }
}
