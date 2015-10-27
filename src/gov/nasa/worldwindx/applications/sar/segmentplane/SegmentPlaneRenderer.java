/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.segmentplane;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.nio.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: SegmentPlaneRenderer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class SegmentPlaneRenderer
{
    protected static class RenderInfo
    {
        protected Globe globe;
        protected Object segmentPlaneKey;

        // Plane geometric properties.
        protected Vec4 planeReferenceCenter;
        protected int planeFillIndexCount;
        protected int planeOutlineIndexCount;
        protected int planeGridIndexCount;
        protected IntBuffer planeFillIndices;
        protected IntBuffer planeOutlineIndices;
        protected IntBuffer planeGridIndices;
        protected DoubleBuffer planeVertices;
        protected DoubleBuffer planeNormals;
        // Border geometric properties.
        protected int borderCylinderIndexCount;
        protected int borderCapIndexCount;
        protected IntBuffer borderCylinderIndices;
        protected IntBuffer borderCapIndices;
        protected FloatBuffer borderCylinderVertices;
        protected FloatBuffer borderCapVertices;
        protected FloatBuffer borderCylinderNormals;
        protected FloatBuffer borderCapNormals;
        // Control point geometric properties.
        protected Map<String, MarkerShape> markerShapeMap;

        public boolean isExpired(Globe globe, SegmentPlane segmentPlane)
        {
            return this.globe == null
                || this.segmentPlaneKey == null
                || !this.globe.equals(globe)
                || !this.segmentPlaneKey.equals(segmentPlane.getStateKey());
        }

        public void makeCurrent(Globe globe, SegmentPlane segmentPlane)
        {
            this.globe = globe;
            this.segmentPlaneKey = segmentPlane.getStateKey();
        }

        public MarkerShape getMarkerShape(String shapeType)
        {
            if (shapeType == null)
                return null;

            MarkerShape shape = this.markerShapeMap.get(shapeType);

            // The shapeType may point to a null reference in the map. If that's the case, then do not try to create
            // that shape, just return a null reference.
            if (shape == null && !this.markerShapeMap.containsKey(shapeType))
            {
                shape = BasicMarkerShape.createShapeInstance(shapeType);
                this.markerShapeMap.put(shapeType, shape);
            }

            return shape;
        }
    }

    protected static class ControlPointInfo
    {
        protected SegmentPlane.ControlPoint controlPoint;
        protected Position position;
        protected MarkerShape shape;

        public ControlPointInfo(SegmentPlane.ControlPoint controlPoint, Position position, MarkerShape shape)
        {
            this.controlPoint = controlPoint;
            this.position = position;
            this.shape = shape;
        }
    }

    protected Map<SegmentPlane, RenderInfo> renderInfoMap;
    protected double minObjectSize = 0.01;
    protected double maxObjectSizeCoefficient = 0.005;
    protected final PickSupport pickSupport = new PickSupport();

    public SegmentPlaneRenderer()
    {
        this.renderInfoMap = new HashMap<SegmentPlane, RenderInfo>();
    }

    public double getMinObjectSize()
    {
        return minObjectSize;
    }

    public void setMinObjectSize(double size)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "size < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minObjectSize = size;
    }

    public double getMaxObjectSizeCoefficient()
    {
        return this.maxObjectSizeCoefficient;
    }

    public void setMaxObjectSizeCoefficient(double coefficient)
    {
        if (coefficient < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "coefficient < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxObjectSizeCoefficient = coefficient;
    }

    public void render(DrawContext dc, SegmentPlane segmentPlane)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (segmentPlane == null)
        {
            String message = Logging.getMessage("nullValue.SegmentPlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.draw(dc, segmentPlane, null, null);
    }

    public void pick(DrawContext dc, SegmentPlane segmentPlane, java.awt.Point pickPoint, Layer layer)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (segmentPlane == null)
        {
            String message = Logging.getMessage("nullValue.SegmentPlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pickSupport.beginPicking(dc);
        try
        {
            this.draw(dc, segmentPlane, pickPoint, layer);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.clearPickList();
        }
    }

    public Vec4 intersect(Globe globe, Line ray, SegmentPlane segmentPlane)
    {
        if (ray == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (segmentPlane == null)
        {
            String message = Logging.getMessage("nullValue.SegmentPlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RenderInfo renderInfo = this.getRenderInfoFor(globe, segmentPlane);
        if (renderInfo == null)
        {
            return null;
        }

        return this.intersectRayWithFill(ray, renderInfo);
    }

    public Position computeControlPointPosition(SectorGeometryList sgl, Globe globe, SegmentPlane segmentPlane,
        SegmentPlane.ControlPoint controlPoint)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (segmentPlane == null)
        {
            String message = Logging.getMessage("nullValue.SegmentPlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoint == null)
        {
            String message = Logging.getMessage("nullValue.ControlPointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] coords = controlPoint.getCoordinates();
        return this.computePositionOnPlane(sgl, globe, segmentPlane, coords[0], coords[1],
            controlPoint.isRelativeToSurface());
    }

    public double computeObjectSize(View view, Globe globe, SegmentPlane segmentPlane, Object key, Vec4 point)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (segmentPlane == null)
        {
            String message = Logging.getMessage("nullValue.SegmentPlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.computeObjectSize(view, globe, segmentPlane, key, point, false);
    }

    protected RenderInfo getRenderInfoFor(Globe globe, SegmentPlane segmentPlane)
    {
        RenderInfo renderInfo = this.renderInfoMap.get(segmentPlane);
        if (renderInfo == null || renderInfo.isExpired(globe, segmentPlane))
        {
            if (renderInfo == null)
                renderInfo = new RenderInfo();
            this.createSegmentPlaneGeometry(globe, segmentPlane, renderInfo);
            this.createBorderGeometry(globe, segmentPlane, renderInfo);
            this.createControlPointGeometry(globe, segmentPlane, renderInfo);

            renderInfo.makeCurrent(globe, segmentPlane);
            this.renderInfoMap.put(segmentPlane, renderInfo);
        }

        return renderInfo;
    }

    protected MultiLineTextRenderer getTextRendererFor(DrawContext dc, Font font)
    {
        TextRenderer tr = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
        return new MultiLineTextRenderer(tr);
    }

    protected void draw(DrawContext dc, SegmentPlane segmentPlane, java.awt.Point pickPoint, Layer layer)
    {
        if (!segmentPlane.isVisible())
            return;

        RenderInfo renderInfo = this.getRenderInfoFor(dc.getGlobe(), segmentPlane);

        OGLStackHandler ogsh = new OGLStackHandler();

        this.begin(dc, ogsh);
        try
        {
            this.drawSegmentPlane(dc, segmentPlane, renderInfo, pickPoint, layer);
        }
        finally
        {
            this.end(dc, ogsh);
        }
    }

    protected void drawSegmentPlane(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        this.drawPlaneGeometry(dc, segmentPlane, renderInfo, pickPoint, layer);
        this.drawPlaneBorder(dc, segmentPlane, renderInfo, pickPoint, layer);
        this.drawSegmentAltimeter(dc, segmentPlane, renderInfo, pickPoint, layer);
        this.drawControlPoints(dc, segmentPlane, renderInfo, pickPoint, layer);
        this.drawAxisLabels(dc, segmentPlane, renderInfo, pickPoint, layer);
    }

    protected void begin(DrawContext dc, OGLStackHandler ogsh)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

        int attribMask = GL2.GL_CURRENT_BIT  // For current RGBA color.
            | GL2.GL_LINE_BIT     // For line width.
            | GL2.GL_POLYGON_BIT // For cull face, polygon offset.
            | (!dc.isPickingMode() ? GL2.GL_COLOR_BUFFER_BIT : 0) // for blend func
            | (!dc.isPickingMode() ? GL2.GL_LIGHTING_BIT : 0) // for lighting.
            | (!dc.isPickingMode() ? GL2.GL_TRANSFORM_BIT : 0); // for normalize state.
        ogsh.pushAttrib(gl, attribMask);

        gl.glDisable(GL.GL_CULL_FACE);

        if (!dc.isPickingMode())
        {
            // Enable blending in non-premultiplied color mode. Premultiplied colors don't work with GL fixed
            // functionality lighting.
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);

            // Enable lighting with GL_LIGHT1.
            gl.glDisable(GL2.GL_COLOR_MATERIAL);
            gl.glDisable(GL2.GL_LIGHT0);
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glEnable(GL2.GL_LIGHT1);
            gl.glEnable(GL2.GL_NORMALIZE);
            // Configure the lighting model for two-sided smooth shading.
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE);
            gl.glShadeModel(GL2.GL_SMOOTH);
            // Configure GL_LIGHT1 as a white light eminating from the viewer's eye point.
            OGLUtil.applyLightingDirectionalFromViewer(gl, GL2.GL_LIGHT1, new Vec4(1.0, 0.5, 1.0).normalize3());
        }
    }

    protected void end(DrawContext dc, OGLStackHandler ogsh)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Restore default GL client vertex array state.
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

        ogsh.pop(gl);
    }

    protected boolean bindGeometryAttributes(DrawContext dc, SegmentPlane segmentPlane, Object key,
        boolean disablePicking)
    {
        SegmentPlaneAttributes.GeometryAttributes attributes = segmentPlane.getAttributes().getGeometryAttributes(key);
        if (attributes == null || !attributes.isVisible())
            return false;

        if (dc.isPickingMode() && (disablePicking || !attributes.isEnablePicking()))
            return false;

        if (dc.isPickingMode())
        {
            this.bindPickableObject(dc, segmentPlane, key);
        }

        SegmentPlaneAttributes.applyGeometryAttributes(dc, attributes, true);

        return true;
    }

    protected boolean bindGeometryAttributesAsLine(DrawContext dc, SegmentPlane segmentPlane, Object key,
        boolean disablePicking)
    {
        SegmentPlaneAttributes.GeometryAttributes attributes = segmentPlane.getAttributes().getGeometryAttributes(key);
        if (attributes == null || !attributes.isVisible())
            return false;

        if (dc.isPickingMode() && (disablePicking || !attributes.isEnablePicking()))
            return false;

        if (dc.isPickingMode())
        {
            this.bindPickableObject(dc, segmentPlane, key);
        }

        SegmentPlaneAttributes.applyGeometryAttributes(dc, attributes, false);
        SegmentPlaneAttributes.applyGeometryAttributesAsLine(dc, attributes);

        return true;
    }

    protected boolean bindLabelAttributes(DrawContext dc, SegmentPlane segmentPlane, Object key)
    {
        if (dc.isPickingMode())
            return false;

        SegmentPlaneAttributes.LabelAttributes attributes = segmentPlane.getAttributes().getLabelAttributes(key);
        //noinspection RedundantIfStatement
        if (attributes == null || !attributes.isVisible())
            return false;

        return true;
    }

    protected PickedObject bindPickableObject(DrawContext dc, Object userObject, Object objectId)
    {
        java.awt.Color pickColor = dc.getUniquePickColor();
        int colorCode = pickColor.getRGB();
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());

        PickedObject po = new PickedObject(colorCode, userObject);
        po.setValue(AVKey.PICKED_OBJECT_ID, objectId);
        this.pickSupport.addPickableObject(po);

        return po;
    }

    protected PickedObject getTopPickedObject(DrawContext dc, java.awt.Point pickPoint, Object pickedObjectId)
    {
        PickedObject topObject = this.pickSupport.getTopObject(dc, pickPoint);
        if (topObject == null)
        {
            return null;
        }

        Object id = topObject.getValue(AVKey.PICKED_OBJECT_ID);
        if (id != pickedObjectId)
        {
            return null;
        }

        return topObject;
    }

    protected void registerPickedObject(DrawContext dc, PickedObject pickedObject, Layer layer)
    {
        if (layer != null)
        {
            pickedObject.setParentLayer(layer);
        }

        dc.addPickedObject(pickedObject);
    }

    //**************************************************************//
    //********************  Plane Geometry  ************************//
    //**************************************************************//

    protected Position computePositionOnPlane(SectorGeometryList sgl, Globe globe, SegmentPlane segmentPlane,
        double u, double v, boolean relativeToSurface)
    {
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        LatLon[] locations = segmentPlane.getPlaneLocations();

        Angle heading = LatLon.rhumbAzimuth(locations[0], locations[1]);
        Angle distance = LatLon.rhumbDistance(locations[0], locations[1]);

        Angle d = Angle.fromDegrees(distance.degrees * u);
        LatLon location = LatLon.rhumbEndPosition(locations[0], heading, d);
        double altitude;

        if (relativeToSurface)
        {
            double surfaceElevation = this.computeSurfaceElevation(sgl, globe,
                location.getLatitude(), location.getLongitude());
            altitude = surfaceElevation + v * (altitudes[1] - surfaceElevation);
        }
        else
        {
            altitude = altitudes[0] + v * (altitudes[1] - altitudes[0]);
        }

        return new Position(location, altitude);
    }

    protected double computeSurfaceElevation(SectorGeometryList sgl, Globe globe, Angle latitude, Angle longitude)
    {
        if (sgl != null)
        {
            Vec4 surfacePoint = sgl.getSurfacePoint(latitude, longitude);
            if (surfacePoint != null)
            {
                Position surfacePos = globe.computePositionFromPoint(surfacePoint);
                return surfacePos.getElevation();
            }
        }

        return globe.getElevation(latitude, longitude);
    }

    protected void computePlaneParameterization(Globe globe, SegmentPlane segmentPlane,
        int[] gridCellCounts, double[] gridCellParams)
    {
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        LatLon[] locations = segmentPlane.getPlaneLocations();
        double[] gridSizes = segmentPlane.getGridCellDimensions();

        double width = LatLon.rhumbDistance(locations[0], locations[1]).radians * globe.getRadius();
        double height = Math.abs(altitudes[1] - altitudes[0]);

        gridCellCounts[0] = (int) Math.ceil(width / gridSizes[0]);
        gridCellCounts[1] = (int) Math.ceil(height / gridSizes[1]);
        gridCellParams[0] = (width != 0) ? (gridSizes[0] / width) : 0;
        gridCellParams[1] = (height != 0) ? (gridSizes[1] / height) : 0;
    }

    protected double computeObjectSize(View view, Globe globe, SegmentPlane segmentPlane, Object key, Vec4 point,
        boolean usePickSize)
    {
        SegmentPlaneAttributes.GeometryAttributes attributes =
            segmentPlane.getAttributes().getGeometryAttributes(key);
        if (attributes == null)
        {
            return 0.0;
        }

        double minSize = this.getMinObjectSize();
        double maxSize = this.computeMaxSizeForPixels(globe, segmentPlane);
        double sizeScale = this.computeSizeForPixels(view, point, 1.0, minSize, maxSize);

        return sizeScale * (usePickSize ? attributes.getPicksize() : attributes.getSize());
    }

    // TODO: identical to a method in MarkerRenderer; consolidate usage in a general place
    protected double computeSizeForPixels(View view, Vec4 point, double pixels, double minSize, double maxSize)
    {
        double d = point.distanceTo3(view.getEyePoint());
        double radius = pixels * view.computePixelSizeAtDistance(d);
        if (radius < minSize)
            radius = minSize;
        else if (radius > maxSize)
            radius = maxSize;

        return radius;
    }

    protected double computeMaxSizeForPixels(Globe globe, SegmentPlane segmentPlane)
    {
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        LatLon[] locations = segmentPlane.getPlaneLocations();

        Vec4[] corners = new Vec4[] {
            globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[0]),
            globe.computePointFromPosition(locations[1].getLatitude(), locations[1].getLongitude(), altitudes[0]),
            globe.computePointFromPosition(locations[1].getLatitude(), locations[1].getLongitude(), altitudes[1]),
            globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[1])};

        double distance = Vec4.getAverageDistance(Arrays.asList(corners));
        return distance * this.getMaxObjectSizeCoefficient();
    }

    //**************************************************************//
    //********************  Plane Rendering  ***********************//
    //**************************************************************//

    protected void drawPlaneGeometry(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        dc.getView().pushReferenceCenter(dc, renderInfo.planeReferenceCenter);
        try
        {
            this.bindPlaneVertexGeometry(dc, renderInfo);
            this.drawPlaneBackground(dc, segmentPlane, renderInfo, pickPoint, layer);
            this.drawPlaneGrid(dc, segmentPlane, renderInfo, pickPoint, layer);
            this.drawPlaneOutline(dc, segmentPlane, renderInfo, pickPoint, layer);
        }
        finally
        {
            dc.getView().popReferenceCenter(dc);
        }
    }

    protected void drawPlaneBackground(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        if (!this.bindGeometryAttributes(dc, segmentPlane, SegmentPlane.PLANE_BACKGROUND, false))
            return;

        this.drawPlaneFillElements(dc, renderInfo);

        if (dc.isPickingMode())
        {
            this.resolvePlaneBackgroundPick(dc, segmentPlane, renderInfo, pickPoint, layer);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void drawPlaneOutline(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        if (!this.bindGeometryAttributesAsLine(dc, segmentPlane, SegmentPlane.PLANE_OUTLINE, true))
            return;

        if (!dc.isPickingMode())
        {
            dc.getGL().glDisable(GL2.GL_LIGHTING);
        }

        this.drawPlaneOutlineElements(dc, renderInfo);

        if (!dc.isPickingMode())
        {
            dc.getGL().glEnable(GL2.GL_LIGHTING);
        }
    }

    protected void drawPlaneGrid(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        if (!this.bindGeometryAttributesAsLine(dc, segmentPlane, SegmentPlane.PLANE_GRID, false))
            return;

        if (!dc.isPickingMode())
        {
            dc.getGL().glDisable(GL2.GL_LIGHTING);
        }

        this.drawPlaneGridElements(dc, renderInfo);

        if (!dc.isPickingMode())
        {
            dc.getGL().glEnable(GL2.GL_LIGHTING);
        }
        else
        {
            this.resolvePlaneGridPick(dc, segmentPlane, renderInfo, pickPoint, layer);
        }
    }

    protected void bindPlaneVertexGeometry(DrawContext dc, RenderInfo renderInfo)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, renderInfo.planeVertices);
        gl.glNormalPointer(GL2.GL_DOUBLE, 0, renderInfo.planeNormals);
    }

    protected void drawPlaneFillElements(DrawContext dc, RenderInfo renderInfo)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(1f, 1f);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, renderInfo.planeFillIndexCount, GL.GL_UNSIGNED_INT,
            renderInfo.planeFillIndices);
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
    }

    protected void drawPlaneOutlineElements(DrawContext dc, RenderInfo renderInfo)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glDrawElements(GL.GL_LINES, renderInfo.planeOutlineIndexCount, GL.GL_UNSIGNED_INT,
            renderInfo.planeOutlineIndices);
    }

    protected void drawPlaneGridElements(DrawContext dc, RenderInfo renderInfo)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glDrawElements(GL.GL_LINES, renderInfo.planeGridIndexCount, GL.GL_UNSIGNED_INT,
            renderInfo.planeGridIndices);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void resolvePlaneBackgroundPick(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        // The pick point is null when a pick rectangle is specified but a pick point is not. In this case, there's
        // nothing for the segment plane to resolve.
        if (pickPoint == null)
            return;

        PickedObject topObject = this.getTopPickedObject(dc, pickPoint, SegmentPlane.PLANE_BACKGROUND);
        if (topObject == null)
            return;

        Line ray = dc.getView().computeRayFromScreenPoint(pickPoint.getX(), pickPoint.getY());
        Vec4 point = this.intersectRayWithFill(ray, renderInfo);
        if (point == null)
            return;

        Position pos = dc.getGlobe().computePositionFromPoint(point);
        topObject.setPosition(pos);

        this.registerPickedObject(dc, topObject, layer);
    }

    protected void resolvePlaneOutlinePick(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        PickedObject topObject = this.getTopPickedObject(dc, pickPoint, SegmentPlane.PLANE_OUTLINE);
        if (topObject == null)
            return;

        Line ray = dc.getView().computeRayFromScreenPoint(pickPoint.getX(), pickPoint.getY());
        Plane plane = segmentPlane.computeInfinitePlane(dc.getGlobe());
        if (plane == null)
            return;

        Vec4 point = plane.intersect(ray);
        if (point == null)
            return;

        Vec4 outlinePoint = this.computeNearestOutlineToPoint(point, renderInfo);
        if (outlinePoint == null)
            return;

        Position pos = dc.getGlobe().computePositionFromPoint(outlinePoint);
        topObject.setPosition(pos);

        this.registerPickedObject(dc, topObject, layer);
    }

    protected void resolvePlaneGridPick(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        // The pick point is null when a pick rectangle is specified but a pick point is not. In this case, there's
        // nothing for the segment plane to resolve.
        if (pickPoint == null)
            return;

        PickedObject topObject = this.getTopPickedObject(dc, pickPoint, SegmentPlane.PLANE_GRID);
        if (topObject == null)
            return;

        Line ray = dc.getView().computeRayFromScreenPoint(pickPoint.getX(), pickPoint.getY());
        Plane plane = segmentPlane.computeInfinitePlane(dc.getGlobe());
        if (plane == null)
            return;

        Vec4 point = plane.intersect(ray);
        if (point == null)
            return;

        Vec4 gridPoint = this.computeNearestGridLineToPoint(point, renderInfo);
        if (gridPoint == null)
            return;

        Position pos = dc.getGlobe().computePositionFromPoint(gridPoint);
        topObject.setPosition(pos);

        this.registerPickedObject(dc, topObject, layer);
    }

    //**************************************************************//
    //********************  Border Rendering  **********************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void drawPlaneBorder(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        if (!this.bindGeometryAttributes(dc, segmentPlane, SegmentPlane.PLANE_BORDER, true))
            return;

        SegmentPlaneAttributes.GeometryAttributes attributes = segmentPlane.getAttributes().getGeometryAttributes(
            SegmentPlane.PLANE_BORDER);

        View view = dc.getView();
        Globe globe = dc.getGlobe();
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        LatLon[] locations = segmentPlane.getPlaneLocations();
        int mask = segmentPlane.getBorderMask();

        Vec4 p1 = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(),
            altitudes[0]);
        Vec4 p2 = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(),
            altitudes[1]);
        Vec4 referencePoint = p1.add3(p2).divide3(2.0);

        double size = this.computeObjectSize(view, globe, segmentPlane, SegmentPlane.PLANE_BORDER, referencePoint,
            dc.isPickingMode());
        double height = altitudes[1] - altitudes[0];

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler oglsh = new OGLStackHandler();
        oglsh.pushModelview(gl);
        try
        {
            if ((mask & SegmentPlane.LEFT) != 0)
            {
                Matrix modelview = view.getModelviewMatrix();
                modelview = modelview.multiply(globe.computeSurfaceOrientationAtPosition(
                    locations[0].getLatitude(), locations[0].getLongitude(), altitudes[0]));

                this.drawBorder(dc, renderInfo, modelview, size, height);
            }
        }
        finally
        {
            oglsh.pop(gl);
        }
    }

    protected void drawBorder(DrawContext dc, RenderInfo renderInfo, Matrix modelview, double radius, double height)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        double[] compArray = new double[16];

        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(modelview);
        transform = transform.multiply(Matrix.fromScale(radius, radius, height));
        transform.toArray(compArray, 0, false);
        gl.glLoadMatrixd(compArray, 0);
        this.drawBorderCylinder(dc, renderInfo);

        transform = Matrix.IDENTITY;
        transform = transform.multiply(modelview);
        transform = transform.multiply(Matrix.fromScale(radius));
        transform.toArray(compArray, 0, false);
        gl.glLoadMatrixd(compArray, 0);
        this.drawBorderCap(dc, renderInfo);

        transform = Matrix.IDENTITY;
        transform = transform.multiply(modelview);
        transform = transform.multiply(Matrix.fromTranslation(0, 0, height));
        transform = transform.multiply(Matrix.fromScale(radius));
        transform.toArray(compArray, 0, false);
        gl.glLoadMatrixd(compArray, 0);
        this.drawBorderCap(dc, renderInfo);
    }

    protected void drawBorderCylinder(DrawContext dc, RenderInfo renderInfo)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, renderInfo.borderCylinderVertices);
        gl.glNormalPointer(GL.GL_FLOAT, 0, renderInfo.borderCylinderNormals);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, renderInfo.borderCylinderIndexCount, GL.GL_UNSIGNED_INT,
            renderInfo.borderCylinderIndices);
    }

    protected void drawBorderCap(DrawContext dc, RenderInfo renderInfo)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, renderInfo.borderCapVertices);
        gl.glNormalPointer(GL.GL_FLOAT, 0, renderInfo.borderCapNormals);
        gl.glDrawElements(GL.GL_TRIANGLE_STRIP, renderInfo.borderCapIndexCount, GL.GL_UNSIGNED_INT,
            renderInfo.borderCapIndices);
    }

    //**************************************************************//
    //********************  Segment Altimeter Rendering  ***********//
    //**************************************************************//

    protected void drawSegmentAltimeter(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        this.drawSegmentAltimeterGeometry(dc, segmentPlane, renderInfo, pickPoint, layer);
        this.drawSegmentAltimeterLabel(dc, segmentPlane, renderInfo, pickPoint, layer);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void drawSegmentAltimeterGeometry(DrawContext dc, SegmentPlane segmentPlane,
        RenderInfo renderInfo, java.awt.Point pickPoint, Layer layer)
    {
        if (!this.bindGeometryAttributesAsLine(dc, segmentPlane, SegmentPlane.ALTIMETER, true))
            return;

        Globe globe = dc.getGlobe();
        Position position = segmentPlane.getSegmentPositions()[1];
        double surfaceElevation = this.computeSurfaceElevation(dc.getSurfaceGeometry(), globe,
            position.getLatitude(), position.getLongitude());

        Vec4 v1 = globe.computePointFromPosition(position.getLatitude(), position.getLongitude(),
            position.getElevation());
        Vec4 v2 = globe.computePointFromPosition(position.getLatitude(), position.getLongitude(),
            surfaceElevation);
        Vec4 referenceCenter = v1;
        v1 = v1.subtract3(referenceCenter);
        v2 = v2.subtract3(referenceCenter);

        if (!dc.isPickingMode())
        {
            dc.getGL().glDisable(GL2.GL_LIGHTING);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler oglsh = new OGLStackHandler();

        // Modify the projection transform to shift the depth values slightly toward the camera in order to
        // ensure the lines are selected during depth buffering.
        double[] pm = new double[16];
        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, pm, 0);
        pm[10] *= 0.99; // TODO: See Lengyel 2 ed. Section 9.1.2 to compute optimal/minimal offset
        oglsh.pushProjectionIdentity(gl);
        gl.glLoadMatrixd(pm, 0);

        dc.getView().pushReferenceCenter(dc, referenceCenter);
        gl.glBegin(GL2.GL_LINES);

        try
        {
            gl.glVertex3d(v1.x, v1.y, v1.z);
            gl.glVertex3d(v2.x, v2.y, v2.z);
        }
        finally
        {
            gl.glEnd();
            dc.getView().popReferenceCenter(dc);
            oglsh.pop(gl);
        }

        if (!dc.isPickingMode())
        {
            dc.getGL().glEnable(GL2.GL_LIGHTING);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void drawSegmentAltimeterLabel(DrawContext dc, SegmentPlane segmentPlane,
        RenderInfo renderInfo, java.awt.Point pickPoint, Layer layer)
    {
        if (!this.bindLabelAttributes(dc, segmentPlane, SegmentPlane.ALTIMETER))
            return;

        SectorGeometryList sgl = dc.getSurfaceGeometry();
        Globe globe = dc.getGlobe();
        Position position = segmentPlane.getSegmentPositions()[1];
        double surfaceElevation = this.computeSurfaceElevation(sgl, globe,
            position.getLatitude(), position.getLongitude());
        double height = position.getElevation() - surfaceElevation;

        Position centerPos = new Position(position,
            surfaceElevation + (height / 2.0));

        AVList values = new AVListImpl();
        values.setValue(AVKey.HEIGHT, height);

        this.drawLabel(dc, segmentPlane, centerPos, values, SegmentPlane.ALTIMETER);
    }

    //**************************************************************//
    //********************  Control Point Rendering  ***************//
    //**************************************************************//

    protected void drawControlPoints(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        SectorGeometryList sgl = dc.getSurfaceGeometry();
        Globe globe = dc.getGlobe();

        // Draw user-defined control points.
        for (SegmentPlane.ControlPoint controlPoint : segmentPlane.getControlPoints())
        {
            Position pos = this.computeControlPointPosition(sgl, globe, segmentPlane, controlPoint);
            MarkerShape shape = renderInfo.getMarkerShape(controlPoint.getShapeType());
            if (pos != null && shape != null)
            {
                this.drawControlPoint(dc, segmentPlane, controlPoint, pos, shape);
            }
        }

        // Draw segment begin/end control points.
        Object[] keys = new Object[] {SegmentPlane.SEGMENT_BEGIN, SegmentPlane.SEGMENT_END};
        Position[] positions = segmentPlane.getSegmentPositions();
        for (int i = 0; i < 2; i++)
        {
            SegmentPlane.ControlPoint controlPoint = new SegmentPlane.ControlPoint(segmentPlane, keys[i], -1, -1,
                false, BasicMarkerShape.SPHERE);

            MarkerShape shape = renderInfo.getMarkerShape(controlPoint.getShapeType());
            if (shape != null)
            {
                this.drawControlPoint(dc, segmentPlane, controlPoint, positions[i], shape);
            }
        }

        if (dc.isPickingMode())
        {
            this.resolveControlPointPick(dc, segmentPlane, renderInfo, pickPoint, layer);
        }
    }

    protected void drawControlPoint(DrawContext dc, SegmentPlane segmentPlane, SegmentPlane.ControlPoint controlPoint,
        Position position, MarkerShape shape)
    {
        ControlPointInfo controlPointInfo = new ControlPointInfo(controlPoint, position, shape);
        this.drawControlPointGeometry(dc, segmentPlane, controlPointInfo);
        this.drawControlPointLabel(dc, segmentPlane, controlPoint, position);
    }

    protected void drawControlPointGeometry(DrawContext dc, SegmentPlane segmentPlane,
        ControlPointInfo controlPointInfo)
    {
        Object key = controlPointInfo.controlPoint.getKey();

        if (!this.bindGeometryAttributes(dc, segmentPlane, key, false))
            return;

        SegmentPlaneAttributes.GeometryAttributes attributes = segmentPlane.getAttributes().getGeometryAttributes(key);
        if (attributes == null || !attributes.isVisible())
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        View view = dc.getView();
        Globe globe = dc.getGlobe();

        Vec4 point = globe.computePointFromPosition(controlPointInfo.position);
        double minSize = this.getMinObjectSize();
        double maxSize = this.computeMaxSizeForPixels(globe, segmentPlane);
        double sizeScale = this.computeSizeForPixels(view, point, 1.0, minSize, maxSize);

        // Apply the control point offset in the local coordinate system at the control point's position. Treat offset
        // coordinates as pixel sizes, so the final coordinate must also be scaled by the eye distance. Use the
        // original point to compute the scale factor, so that the offset doesn't change the control point's size.
        Matrix transformToPosition = globe.computeSurfaceOrientationAtPosition(controlPointInfo.position);
        Vec4 offset = attributes.getOffset();
        offset = offset.multiply3(sizeScale);
        offset = offset.transformBy3(transformToPosition);

        // Add the adjusted offset to the Cartesian point, and recompute the control point's offset geographic position.
        point = point.add3(offset);
        controlPointInfo.position = globe.computePositionFromPoint(point);

        if (dc.isPickingMode())
        {
            PickedObject po = this.bindPickableObject(dc, controlPointInfo.controlPoint,
                controlPointInfo.controlPoint.getKey());
            po.setPosition(controlPointInfo.position);
        }

        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushModelview(gl);
        try
        {
            LatLon[] planeLocations = segmentPlane.getPlaneLocations();
            Angle heading = LatLon.rhumbAzimuth(planeLocations[0], planeLocations[1]);
            double size = sizeScale * (dc.isPickingMode() ? attributes.getPicksize() : attributes.getSize());
            Marker marker = new BasicMarker(controlPointInfo.position, new BasicMarkerAttributes(), heading);
            controlPointInfo.shape.render(dc, marker, point, size);
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    protected void drawControlPointLabel(DrawContext dc, SegmentPlane segmentPlane,
        SegmentPlane.ControlPoint controlPoint, Position position)
    {
        if (!this.bindLabelAttributes(dc, segmentPlane, controlPoint.getKey()))
            return;

        double surfaceElevation = this.computeSurfaceElevation(dc.getSurfaceGeometry(), dc.getGlobe(),
            position.getLatitude(), position.getLongitude());
        double height = position.getElevation() - surfaceElevation;

        AVList values = new AVListImpl();
        values.setValue(AVKey.HEIGHT, height);

        this.drawLabel(dc, segmentPlane, position, values, controlPoint.getKey());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void resolveControlPointPick(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        // The pick point is null when a pick rectangle is specified but a pick point is not. In this case, there's
        // nothing for the segment plane to resolve.
        if (pickPoint == null)
            return;

        PickedObject topObject = null;

        // Pick user-defined control points.
        for (SegmentPlane.ControlPoint controlPoint : segmentPlane.getControlPoints())
        {
            if ((topObject = this.getTopPickedObject(dc, pickPoint, controlPoint.getKey())) != null)
            {
                break;
            }
        }

        if (topObject == null)
        {
            // Pick segment begin/end control points.
            Object[] keys = new Object[] {SegmentPlane.SEGMENT_BEGIN, SegmentPlane.SEGMENT_END};
            for (Object key : keys)
            {
                if ((topObject = this.getTopPickedObject(dc, pickPoint, key)) != null)
                {
                    break;
                }
            }
        }

        if (topObject == null)
            return;

        this.registerPickedObject(dc, topObject, layer);
    }

    //**************************************************************//
    //********************  Axis Label Rendering  ******************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void drawAxisLabels(DrawContext dc, SegmentPlane segmentPlane, RenderInfo renderInfo,
        java.awt.Point pickPoint, Layer layer)
    {
        this.drawHorizontalAxisLabels(dc, segmentPlane);
        this.drawVerticalAxisLabels(dc, segmentPlane);
    }

    protected void drawHorizontalAxisLabels(DrawContext dc, SegmentPlane segmentPlane)
    {
        if (!this.bindLabelAttributes(dc, segmentPlane, SegmentPlane.HORIZONTAL_AXIS_LABELS))
            return;

        SectorGeometryList sgl = dc.getSurfaceGeometry();
        Globe globe = dc.getGlobe();
        double[] gridCellSizes = segmentPlane.getGridCellDimensions();
        int[] gridCellCounts = new int[2];
        double[] gridCellParams = new double[2];
        this.computePlaneParameterization(globe, segmentPlane, gridCellCounts, gridCellParams);

        int uStacks = gridCellCounts[0];
        double uStep = gridCellParams[0];

        // Draw the horizontal axis labels. The horizontal axis labels are drawn along the bottom of the plane, but
        // are always drawn at or above the surface.
        OrderedText[] labels = new OrderedText[uStacks];

        for (int ui = 0; ui < uStacks; ui++)
        {
            double u = clamp(ui * uStep, 0, 1);
            double width = ui * gridCellSizes[0];

            AVList values = new AVListImpl();
            values.setValue(AVKey.WIDTH, width);

            Position pos = this.computePositionOnPlane(sgl, globe, segmentPlane, u, 0, true);
            double surfaceElevation = this.computeSurfaceElevation(sgl, globe, pos.getLatitude(), pos.getLongitude());
            if (pos.getElevation() < surfaceElevation)
                pos = new Position(pos, surfaceElevation);

            labels[ui] = this.createLabel(dc, segmentPlane, pos, values, SegmentPlane.HORIZONTAL_AXIS_LABELS);
        }

        java.awt.Rectangle size = this.computeAverageLabelSize(labels, uStacks);
        double d = this.computeMinDistanceBetweenLabels(dc, labels, uStacks);

        this.drawAxisLabels(dc, labels, 1, uStacks, size.getWidth(), d);
    }

    protected void drawVerticalAxisLabels(DrawContext dc, SegmentPlane segmentPlane)
    {
        if (!this.bindLabelAttributes(dc, segmentPlane, SegmentPlane.VERTICAL_AXIS_LABELS))
            return;

        double[] gridCellSizes = segmentPlane.getGridCellDimensions();

        SectorGeometryList sgl = dc.getSurfaceGeometry();
        Globe globe = dc.getGlobe();
        int[] gridCellCounts = new int[2];
        double[] gridCellParams = new double[2];
        this.computePlaneParameterization(globe, segmentPlane, gridCellCounts, gridCellParams);

        int vStacks = gridCellCounts[1];
        double vStep = gridCellParams[1];

        // Draw the vertical axis labels. The verical axis labels are drawn along the right side of the plane. Labels
        // beneath the terrain are not drawn.
        OrderedText[] labels = new OrderedText[vStacks];

        for (int vi = 0; vi < vStacks; vi++)
        {
            double v = clamp(vi * vStep, 0, 1);
            double height = vi * gridCellSizes[1];

            AVList values = new AVListImpl();
            values.setValue(AVKey.HEIGHT, height);

            Position pos = this.computePositionOnPlane(sgl, globe, segmentPlane, 1, v, false);
            double surfaceElevation = this.computeSurfaceElevation(sgl, globe, pos.getLatitude(), pos.getLongitude());
            if (pos.getElevation() < surfaceElevation)
                continue;

            labels[vi] = this.createLabel(dc, segmentPlane, pos, values, SegmentPlane.VERTICAL_AXIS_LABELS);
        }

        java.awt.Rectangle size = this.computeAverageLabelSize(labels, vStacks);
        double d = this.computeMinDistanceBetweenLabels(dc, labels, vStacks);

        this.drawAxisLabels(dc, labels, 1, vStacks, size.getHeight(), d);
    }

    protected void drawAxisLabels(DrawContext dc, OrderedText[] text, int startPos, int count,
        double averageSize, double minDistance)
    {
        int step = (int) Math.round(1.5 * averageSize / minDistance);
        if (step < 1)
            step = 1;

        for (int i = startPos; i < count; i += step)
        {
            if (text[i] != null)
            {
                dc.addOrderedRenderable(text[i]);
            }
        }
    }

    //**************************************************************//
    //********************  Label Rendering  ***********************//
    //**************************************************************//

    protected void drawLabel(DrawContext dc, SegmentPlane segmentPlane, Position position, AVList values, Object key)
    {
        OrderedText orderedText = this.createLabel(dc, segmentPlane, position, values, key);
        if (orderedText == null)
            return;

        dc.addOrderedRenderable(orderedText);
    }

    protected OrderedText createLabel(DrawContext dc, SegmentPlane segmentPlane, Position position, AVList values,
        Object key)
    {
        SegmentPlaneAttributes.LabelAttributes attributes = segmentPlane.getAttributes().getLabelAttributes(key);
        if (attributes == null)
            return null;

        Vec4 point = dc.getGlobe().computePointFromPosition(position);
        double distanceFromEye = dc.getView().getEyePoint().distanceTo3(point);
        if (distanceFromEye < attributes.getMinActiveDistance() || distanceFromEye > attributes.getMaxActiveDistance())
        {
            return null;
        }

        Font font = attributes.getFont();
        MultiLineTextRenderer textRenderer = this.getTextRendererFor(dc, font);

        return new OrderedText(segmentPlane, position, distanceFromEye, values, attributes, textRenderer);
    }

    protected java.awt.Rectangle computeAverageLabelSize(OrderedText[] text, int textCount)
    {
        double width = 0;
        double height = 0;
        int count = 0;

        for (int i = 0; i < textCount; i++)
        {
            if (text[i] != null)
            {
                java.awt.Rectangle bounds = text[i].textRenderer.getBounds(text[i].getText());
                width += bounds.getWidth();
                height += bounds.getHeight();
                count++;
            }
        }

        if (count > 1)
        {
            width /= (double) count;
            height /= (double) count;
        }

        return new java.awt.Rectangle((int) width, (int) height);
    }

    protected double computeMinDistanceBetweenLabels(DrawContext dc, OrderedText[] text, int textCount)
    {
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < textCount - 1; i++)
        {
            if (text[i] != null)
            {
                for (int j = i + 1; j < textCount; j++)
                {
                    if (text[j] != null)
                    {
                        Vec4 v1 = text[i].getScreenPoint(dc);
                        Vec4 v2 = text[j].getScreenPoint(dc);

                        double d = v1.distanceToSquared3(v2);
                        if (d < minDistance)
                            minDistance = d;
                    }
                }
            }
        }

        if (minDistance > 0)
            minDistance = Math.sqrt(minDistance);

        return minDistance;
    }

    protected static class OrderedText implements OrderedRenderable
    {
        protected SegmentPlane segmentPlane;
        protected final Position position;
        protected final double distanceFromEye;
        protected AVList values;
        protected SegmentPlaneAttributes.LabelAttributes attributes;
        protected MultiLineTextRenderer textRenderer;

        public OrderedText(SegmentPlane segmentPlane, Position position, double distanceFromEye, AVList values,
            SegmentPlaneAttributes.LabelAttributes attributes, MultiLineTextRenderer textRenderer)
        {
            this.segmentPlane = segmentPlane;
            this.position = position;
            this.distanceFromEye = distanceFromEye;
            this.values = values;
            this.attributes = attributes;
            this.textRenderer = textRenderer;
        }

        public String getText()
        {
            return this.attributes.getText(this.segmentPlane, this.position, this.values);
        }

        public double getDistanceFromEye()
        {
            return this.distanceFromEye;
        }

        public Vec4 getScreenPoint(DrawContext dc)
        {
            if (dc.getGlobe() == null || dc.getView() == null)
                return null;

            Vec4 modelPoint = dc.getGlobe().computePointFromPosition(this.position.getLatitude(),
                this.position.getLongitude(), this.position.getElevation());
            if (modelPoint == null)
                return null;

            return dc.getView().project(modelPoint).add3(attributes.getOffset());
        }

        protected Vec4 getScreenPoint(DrawContext dc, Position position)
        {
            if (dc.getGlobe() == null || dc.getView() == null)
                return null;

            Vec4 modelPoint = dc.getGlobe().computePointFromPosition(position.getLatitude(), position.getLongitude(),
                position.getElevation());
            if (modelPoint == null)
                return null;

            return dc.getView().project(modelPoint);
        }

        public void render(DrawContext dc)
        {
            OGLStackHandler ogsh = new OGLStackHandler();

            this.begin(dc, ogsh);
            try
            {
                this.draw(dc);
            }
            finally
            {
                this.end(dc, ogsh);
            }
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            // Label text is not pickable.
        }

        protected void draw(DrawContext dc)
        {
            String text = this.getText();
            if (text == null)
                return;

            Vec4 point = this.getScreenPoint(dc);
            if (point == null)
                return;

            java.awt.Rectangle viewport = dc.getView().getViewport();
            java.awt.Color color = attributes.getColor();

            this.textRenderer.getTextRenderer().beginRendering(viewport.width, viewport.height);
            this.textRenderer.setTextColor(color);
            this.textRenderer.setBackColor(Color.BLACK);

            this.drawText(text, point, attributes, this.textRenderer);

            this.textRenderer.getTextRenderer().endRendering();
        }

        protected void begin(DrawContext dc, OGLStackHandler ogsh)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            int attribBits = GL2.GL_CURRENT_BIT; // For current color.

            ogsh.pushAttrib(gl, attribBits);
        }

        protected void end(DrawContext dc, OGLStackHandler ogsh)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            ogsh.pop(gl);
        }

        protected void drawText(String text, Vec4 screenPoint,
            SegmentPlaneAttributes.LabelAttributes attributes, MultiLineTextRenderer mltr)
        {
            double x = screenPoint.x;
            double y = screenPoint.y;

            if (attributes != null)
            {
                String horizontal = attributes.getHorizontalAlignment();
                String vertical = attributes.getVerticalAlignment();
                java.awt.Rectangle textBounds = mltr.getBounds(text);
                double w = textBounds.getWidth();
                double h = textBounds.getHeight();
                double hw = textBounds.getWidth() / 2.0;
                double hh = textBounds.getHeight() / 2.0;

                //noinspection StringEquality
                if (horizontal == AVKey.LEFT)
                {
                    // MultiLineTextRenderer anchors text to the upper left corner by default.
                }
                else //noinspection StringEquality
                    if (horizontal == AVKey.CENTER)
                    {
                        x -= hw;
                    }
                    else //noinspection StringEquality
                        if (horizontal == AVKey.RIGHT)
                        {
                            x -= w;
                        }

                //noinspection StringEquality
                if (vertical == AVKey.TOP)
                {
                    // MultiLineTextRenderer anchors text to the upper left corner by default.
                }
                else //noinspection StringEquality
                    if (vertical == AVKey.CENTER)
                    {
                        y += hh;
                    }
                    else //noinspection StringEquality
                        if (vertical == AVKey.BOTTOM)
                        {
                            y += h;
                        }
            }

            mltr.draw(text, (int) x, (int) y, AVKey.TEXT_EFFECT_OUTLINE);
        }
    }

    //**************************************************************//
    //********************  Segment Plane Construction  ************//
    //**************************************************************//

    protected void createSegmentPlaneGeometry(Globe globe, SegmentPlane segmentPlane, RenderInfo renderInfo)
    {
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        LatLon[] locations = segmentPlane.getPlaneLocations();
        int mask = segmentPlane.getPlaneOutlineMask();

        renderInfo.planeReferenceCenter = globe.computePointFromPosition(
            locations[0].getLatitude(), locations[0].getLongitude(), altitudes[0]);

        int[] gridCellCounts = new int[2];
        double[] gridCellParams = new double[2];
        this.computePlaneParameterization(globe, segmentPlane, gridCellCounts, gridCellParams);

        int uStacks = gridCellCounts[0];
        int vStacks = gridCellCounts[1];
        double uStep = gridCellParams[0];
        double vStep = gridCellParams[1];

        renderInfo.planeFillIndexCount = getPlaneFillIndexCount(uStacks, vStacks);
        if (renderInfo.planeFillIndices == null
            || renderInfo.planeFillIndices.capacity() < renderInfo.planeFillIndexCount)
        {
            renderInfo.planeFillIndices = Buffers.newDirectIntBuffer(renderInfo.planeFillIndexCount);
        }

        renderInfo.planeOutlineIndexCount = getPlaneOutlineIndexCount(uStacks, vStacks, mask);
        if (renderInfo.planeOutlineIndices == null
            || renderInfo.planeOutlineIndices.capacity() < renderInfo.planeOutlineIndexCount)
        {
            renderInfo.planeOutlineIndices = Buffers.newDirectIntBuffer(renderInfo.planeOutlineIndexCount);
        }

        renderInfo.planeGridIndexCount = getPlaneGridIndexCount(uStacks, vStacks);
        if (renderInfo.planeGridIndices == null
            || renderInfo.planeGridIndices.capacity() < renderInfo.planeGridIndexCount)
        {
            renderInfo.planeGridIndices = Buffers.newDirectIntBuffer(renderInfo.planeGridIndexCount);
        }

        int vertexCount = getPlaneVertexCount(uStacks, vStacks);
        int coordCount = 3 * vertexCount;
        if (renderInfo.planeVertices == null || renderInfo.planeVertices.capacity() < coordCount)
        {
            renderInfo.planeVertices = Buffers.newDirectDoubleBuffer(coordCount);
        }
        if (renderInfo.planeNormals == null || renderInfo.planeNormals.capacity() < coordCount)
        {
            renderInfo.planeNormals = Buffers.newDirectDoubleBuffer(coordCount);
        }

        computePlaneFillIndices(uStacks, vStacks, renderInfo.planeFillIndices);
        renderInfo.planeFillIndices.rewind();

        computePlaneOutlineIndices(uStacks, vStacks, mask, renderInfo.planeOutlineIndices);
        renderInfo.planeOutlineIndices.rewind();

        computePlaneGridIndices(uStacks, vStacks, renderInfo.planeGridIndices);
        renderInfo.planeGridIndices.rewind();

        this.computePlaneVertices(globe, segmentPlane, uStacks, vStacks, uStep, vStep, renderInfo.planeReferenceCenter,
            renderInfo.planeVertices);
        renderInfo.planeVertices.rewind();

        this.computePlaneNormals(globe, segmentPlane, renderInfo.planeFillIndexCount, vertexCount,
            renderInfo.planeFillIndices, renderInfo.planeVertices, renderInfo.planeNormals);
        renderInfo.planeNormals.rewind();
    }

    // TODO: consolidate the following geometry construction code with GeometryBuilder

    protected static int getPlaneFillIndexCount(int uStacks, int vStacks)
    {
        int count = 2 * (uStacks + 1) * vStacks; // Triangle strips for each row.
        if (vStacks > 1)
            count += 2 * (vStacks - 1);          // Degenerate connection triangles.

        return count;
    }

    protected static int getPlaneOutlineIndexCount(int uStacks, int vStacks, int mask)
    {
        int count = 0;
        if ((mask & SegmentPlane.TOP) != 0)
            count += 2 * uStacks;
        if ((mask & SegmentPlane.BOTTOM) != 0)
            count += 2 * uStacks;
        if ((mask & SegmentPlane.LEFT) != 0)
            count += 2 * vStacks;
        if ((mask & SegmentPlane.RIGHT) != 0)
            count += 2 * vStacks;

        return count;
    }

    protected static int getPlaneGridIndexCount(int uStacks, int vStacks)
    {
        return 2 * uStacks * (vStacks - 1)  // Horizontal gridlines.
            + 2 * vStacks * (uStacks - 1); // Vertical gridlines.
    }

    protected static int getPlaneVertexCount(int uStacks, int vStacks)
    {
        return (uStacks + 1) * (vStacks + 1);
    }

    protected static void computePlaneFillIndices(int uStacks, int vStacks, IntBuffer buffer)
    {
        int vertex;

        for (int vi = 0; vi < vStacks; vi++)
        {
            if (vi != 0)
            {
                vertex = uStacks + (vi - 1) * (uStacks + 1);
                buffer.put(vertex);
                vertex = vi * (uStacks + 1) + (uStacks + 1);
                buffer.put(vertex);
            }

            for (int ui = 0; ui <= uStacks; ui++)
            {
                vertex = ui + (vi + 1) * (uStacks + 1);
                buffer.put(vertex);
                vertex = ui + vi * (uStacks + 1);
                buffer.put(vertex);
            }
        }
    }

    protected static void computePlaneOutlineIndices(int uStacks, int vStacks, int mask, IntBuffer buffer)
    {
        int vertex;

        // Top edge.
        if ((mask & SegmentPlane.TOP) != 0)
        {
            for (int ui = 0; ui < uStacks; ui++)
            {
                vertex = ui + vStacks * (uStacks + 1);
                buffer.put(vertex);
                vertex = (ui + 1) + vStacks * (uStacks + 1);
                buffer.put(vertex);
            }
        }

        // Bottom edge.
        if ((mask & SegmentPlane.BOTTOM) != 0)
        {
            for (int ui = 0; ui < uStacks; ui++)
            {
                vertex = ui;
                buffer.put(vertex);
                vertex = (ui + 1);
                buffer.put(vertex);
            }
        }

        // Left edge.
        if ((mask & SegmentPlane.LEFT) != 0)
        {
            for (int vi = 0; vi < vStacks; vi++)
            {
                vertex = vi * (uStacks + 1);
                buffer.put(vertex);
                vertex = (vi + 1) * (uStacks + 1);
                buffer.put(vertex);
            }
        }

        // Right edge.
        if ((mask & SegmentPlane.RIGHT) != 0)
        {
            for (int vi = 0; vi < vStacks; vi++)
            {
                vertex = uStacks + vi * (uStacks + 1);
                buffer.put(vertex);
                vertex = uStacks + (vi + 1) * (uStacks + 1);
                buffer.put(vertex);
            }
        }
    }

    protected static void computePlaneGridIndices(int uStacks, int vStacks, IntBuffer buffer)
    {
        int vertex;

        // Horizontal gridlines.
        for (int vi = 1; vi < vStacks; vi++)
        {
            for (int ui = 0; ui < uStacks; ui++)
            {
                vertex = ui + vi * (uStacks + 1);
                buffer.put(vertex);
                vertex = (ui + 1) + vi * (uStacks + 1);
                buffer.put(vertex);
            }
        }

        // Vertical gridlines.
        for (int ui = 1; ui < uStacks; ui++)
        {
            for (int vi = 0; vi < vStacks; vi++)
            {
                vertex = ui + vi * (uStacks + 1);
                buffer.put(vertex);
                vertex = ui + (vi + 1) * (uStacks + 1);
                buffer.put(vertex);
            }
        }
    }

    protected void computePlaneVertices(Globe globe, SegmentPlane segmentPlane,
        int uStacks, int vStacks, double uStep, double vStep,
        Vec4 referenceCenter, DoubleBuffer buffer)
    {
        int index = 0;

        for (int vi = 0; vi <= vStacks; vi++)
        {
            double v = clamp(vi * vStep, 0, 1);

            for (int ui = 0; ui <= uStacks; ui++)
            {
                double u = clamp(ui * uStep, 0, 1);

                Position pos = this.computePositionOnPlane(null, globe, segmentPlane, u, v, false);
                Vec4 vertex = globe.computePointFromPosition(pos);
                vertex = vertex.subtract3(referenceCenter);
                putVertex3(vertex, index++, buffer);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void computePlaneNormals(Globe globe, SegmentPlane segmentPlane, int indexCount, int vertexCount,
        IntBuffer indices, DoubleBuffer vertices, DoubleBuffer buffer)
    {
        double[] altitudes = segmentPlane.getPlaneAltitudes();
        LatLon[] locations = segmentPlane.getPlaneLocations();

        Vec4 p1 = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[0]);
        Vec4 p2 = globe.computePointFromPosition(locations[1].getLatitude(), locations[1].getLongitude(), altitudes[0]);
        Vec4 p3 = globe.computePointFromPosition(locations[0].getLatitude(), locations[0].getLongitude(), altitudes[1]);

        Vec4 e1 = p2.subtract3(p1);
        Vec4 e2 = p3.subtract3(p1);

        Vec4 normal = e1.cross3(e2).normalize3();

        for (int v = 0; v < vertexCount; v++)
        {
            putVertex3(normal, v, buffer);
        }
    }

    private static double clamp(double x, double min, double max)
    {
        return (x < min) ? min : ((x > max) ? max : x);
    }

    //**************************************************************//
    //********************  Border Construction  *******************//
    //**************************************************************//

    // TODO: investigate necessary changes to create a general-use cylinder with caps, a height, and a radius.

    @SuppressWarnings({"UnusedDeclaration"})
    protected void createBorderGeometry(Globe globe, SegmentPlane segmentPlane, RenderInfo renderInfo)
    {
        int slices = 16;
        int stacks = 32;
        int loops = 8;

        GeometryBuilder gb = new GeometryBuilder();

        renderInfo.borderCylinderIndexCount = gb.getCylinderIndexCount(slices, stacks);
        if (renderInfo.borderCylinderIndices == null
            || renderInfo.borderCylinderIndices.capacity() < renderInfo.borderCylinderIndexCount)
        {
            renderInfo.borderCylinderIndices = Buffers.newDirectIntBuffer(renderInfo.borderCylinderIndexCount);
        }

        renderInfo.borderCapIndexCount = gb.getDiskIndexCount(slices, loops);
        if (renderInfo.borderCapIndices == null
            || renderInfo.borderCapIndices.capacity() < renderInfo.borderCapIndexCount)
        {
            renderInfo.borderCapIndices = Buffers.newDirectIntBuffer(renderInfo.borderCapIndexCount);
        }

        int cylinderVertexCount = gb.getCylinderVertexCount(slices, stacks);
        int cylinderCoordCount = 3 * cylinderVertexCount;
        if (renderInfo.borderCylinderVertices == null
            || renderInfo.borderCylinderVertices.capacity() < cylinderCoordCount)
        {
            renderInfo.borderCylinderVertices = Buffers.newDirectFloatBuffer(cylinderCoordCount);
        }
        if (renderInfo.borderCylinderNormals == null
            || renderInfo.borderCylinderNormals.capacity() < cylinderCoordCount)
        {
            renderInfo.borderCylinderNormals = Buffers.newDirectFloatBuffer(cylinderCoordCount);
        }

        int capVertexCount = gb.getDiskVertexCount(slices, loops);
        int capCoordCount = 3 * capVertexCount;
        if (renderInfo.borderCapVertices == null
            || renderInfo.borderCapVertices.capacity() < capCoordCount)
        {
            renderInfo.borderCapVertices = Buffers.newDirectFloatBuffer(capCoordCount);
        }
        if (renderInfo.borderCapNormals == null
            || renderInfo.borderCapNormals.capacity() < capCoordCount)
        {
            renderInfo.borderCapNormals = Buffers.newDirectFloatBuffer(capCoordCount);
        }

        int[] indices = new int[renderInfo.borderCylinderIndexCount];
        gb.makeCylinderIndices(slices, stacks, indices);
        renderInfo.borderCylinderIndices.put(indices);
        renderInfo.borderCylinderIndices.rewind();

        indices = new int[renderInfo.borderCapIndexCount];
        gb.makeDiskIndices(slices, loops, indices);
        renderInfo.borderCapIndices.put(indices);
        renderInfo.borderCapIndices.rewind();

        float[] vertices = new float[cylinderCoordCount];
        gb.makeCylinderVertices(1.0f, 1.0f, slices, stacks, vertices);
        renderInfo.borderCylinderVertices.put(vertices);
        renderInfo.borderCylinderVertices.rewind();

        float[] normals = new float[cylinderCoordCount];
        gb.makeCylinderNormals(slices, stacks, normals);
        renderInfo.borderCylinderNormals.put(normals);
        renderInfo.borderCylinderNormals.rewind();

        vertices = new float[capCoordCount];
        gb.makeDiskVertices(0.0f, 1.0f, slices, loops, vertices);
        renderInfo.borderCapVertices.put(vertices);
        renderInfo.borderCapVertices.rewind();

        normals = new float[capCoordCount];
        gb.makeDiskNormals(slices, loops, normals);
        renderInfo.borderCapNormals.put(normals);
        renderInfo.borderCapNormals.rewind();
    }

    //**************************************************************//
    //********************  Control Point Construction  ************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected void createControlPointGeometry(Globe globe, SegmentPlane segmentPlane, RenderInfo renderInfo)
    {
        if (renderInfo.markerShapeMap == null)
            renderInfo.markerShapeMap = new HashMap<String, MarkerShape>();
    }

    //**************************************************************//
    //********************  Ray-Geometry Intersection  *************//
    //**************************************************************//

    protected Vec4 intersectRayWithFill(Line ray, RenderInfo renderInfo)
    {
        if (renderInfo.planeFillIndices != null && renderInfo.planeVertices != null)
        {
            return this.intersectRayWithTriangleStrip(ray,
                renderInfo.planeFillIndexCount, renderInfo.planeFillIndices,
                renderInfo.planeVertices, renderInfo.planeReferenceCenter);
        }

        return null;
    }

    protected Vec4 computeNearestOutlineToPoint(Vec4 point, RenderInfo renderInfo)
    {
        if (renderInfo.planeOutlineIndices != null && renderInfo.planeVertices != null)
        {
            return this.computeNearestLineToPoint(point,
                renderInfo.planeOutlineIndexCount, renderInfo.planeOutlineIndices,
                renderInfo.planeVertices, renderInfo.planeReferenceCenter);
        }

        return null;
    }

    protected Vec4 computeNearestGridLineToPoint(Vec4 point, RenderInfo renderInfo)
    {
        if (renderInfo.planeGridIndices != null && renderInfo.planeVertices != null)
        {
            return this.computeNearestLineToPoint(point,
                renderInfo.planeGridIndexCount, renderInfo.planeGridIndices,
                renderInfo.planeVertices, renderInfo.planeReferenceCenter);
        }

        return null;
    }

    // TODO: this method could be of general use
    protected Vec4 computeNearestLineToPoint(Vec4 point, int count, IntBuffer indices, DoubleBuffer vertices,
        Vec4 referenceCenter)
    {
        Vec4 intersectionPoint = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int i = 0; i < (count - 1); i += 2)
        {
            int position = indices.get(i);
            Vec4 v1 = getVertex3(position, vertices).add3(referenceCenter);
            position = indices.get(i + 1);
            Vec4 v2 = getVertex3(position, vertices).add3(referenceCenter);

            Vec4 vec = nearestPointOnSegment(v1, v2, point);
            if (vec != null)
            {
                double d = point.distanceTo3(vec);
                if (d < nearestDistance)
                {
                    nearestDistance = d;
                    intersectionPoint = vec;
                }
            }
        }

        indices.rewind();
        vertices.rewind();

        return intersectionPoint;
    }

    // TODO: this method could be of general use
    protected Vec4 intersectRayWithTriangleStrip(Line ray, int count, IntBuffer indices, DoubleBuffer vertices,
        Vec4 referenceCenter)
    {
        Vec4 intersectionPoint = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int i = 0; i < (count - 2); i++)
        {
            int position = indices.get(i);
            Vec4 v1 = getVertex3(position, vertices).add3(referenceCenter);
            position = indices.get(i + 1);
            Vec4 v2 = getVertex3(position, vertices).add3(referenceCenter);
            position = indices.get(i + 2);
            Vec4 v3 = getVertex3(position, vertices).add3(referenceCenter);

            Triangle triangle;
            if ((i % 2) == 0)
            {
                triangle = new Triangle(v1, v2, v3);
            }
            else
            {
                triangle = new Triangle(v2, v1, v3);
            }

            Vec4 vec = triangle.intersect(ray);
            if (vec != null)
            {
                double d = ray.getOrigin().distanceTo3(vec);
                if (d < nearestDistance)
                {
                    nearestDistance = d;
                    intersectionPoint = vec;
                }
            }
        }

        indices.rewind();
        vertices.rewind();

        return intersectionPoint;
    }

    protected static Vec4 getVertex3(int position, DoubleBuffer vertices)
    {
        double[] compArray = new double[3];
        vertices.position(3 * position);
        vertices.get(compArray, 0, 3);
        return Vec4.fromArray3(compArray, 0);
    }

    protected static void putVertex3(Vec4 vec, int position, DoubleBuffer vertices)
    {
        double[] compArray = new double[3];
        vec.toArray3(compArray, 0);
        vertices.position(3 * position);
        vertices.put(compArray, 0, 3);
    }

    // TODO: identical to a method in AirspaceEditorUtil; consolidate usage in a general place
    private static Vec4 nearestPointOnSegment(Vec4 p1, Vec4 p2, Vec4 point)
    {
        Vec4 segment = p2.subtract3(p1);
        Vec4 dir = segment.normalize3();

        double dot = point.subtract3(p1).dot3(dir);
        if (dot < 0.0)
        {
            return p1;
        }
        else if (dot > segment.getLength3())
        {
            return p2;
        }
        else
        {
            return Vec4.fromLine3(p1, dot, dir);
        }
    }
}
