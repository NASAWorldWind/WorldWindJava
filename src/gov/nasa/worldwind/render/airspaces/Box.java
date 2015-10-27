/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.nio.*;
import java.util.*;

/**
 * @author lado
 * @version $Id: Box.java 2563 2014-12-12 19:29:38Z dcollins $
 */
public class Box extends AbstractAirspace
{
    protected static final int DEFAULT_PILLARS = 8;
    protected static final int DEFAULT_STACKS = 2;

    protected static final int DEFAULT_CENTER_LINE_STIPPLE_FACTOR = 2;
    protected static final short DEFAULT_CENTER_LINE_STIPPLE_PATTERN = (short) 0xEEEE;
    protected static final double DEFAULT_CENTER_LINE_OFFSET = 0.999;

    private LatLon beginLocation = LatLon.ZERO;
    private LatLon endLocation = LatLon.ZERO;
    private double leftWidth = 1.0;
    private double rightWidth = 1.0;
    private Angle beginLeftAzimuth = null;
    private Angle beginRightAzimuth = null;
    private Angle endLeftAzimuth = null;
    private Angle endRightAzimuth = null;
    private boolean enableStartCap = true;
    private boolean enableEndCap = true;
    private boolean enableCenterLine;

    private boolean forceCullFace = false;
    private int pillars = DEFAULT_PILLARS;
    private int stacks = DEFAULT_STACKS;

    private Object geometryCacheKey = new Object();

    public Box(LatLon beginLocation, LatLon endLocation, double leftWidth, double rightWidth)
    {
        if (beginLocation == null || endLocation == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (leftWidth < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "leftWidth < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (rightWidth < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "rightWidth < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.beginLocation = beginLocation;
        this.endLocation = endLocation;
        this.leftWidth = leftWidth;
        this.rightWidth = rightWidth;
        this.makeDefaultDetailLevels();
    }

    public Box(Box source)
    {
        super(source);

        this.beginLocation = source.beginLocation;
        this.endLocation = source.endLocation;
        this.leftWidth = source.leftWidth;
        this.rightWidth = source.rightWidth;
        this.beginLeftAzimuth = source.beginLeftAzimuth;
        this.beginRightAzimuth = source.beginRightAzimuth;
        this.endLeftAzimuth = source.endLeftAzimuth;
        this.endRightAzimuth = source.endRightAzimuth;
        this.enableStartCap = source.enableStartCap;
        this.enableEndCap = source.enableEndCap;
        this.enableCenterLine = source.enableCenterLine;
        this.forceCullFace = source.forceCullFace;
        this.pillars = source.pillars;
        this.stacks = source.stacks;

        this.makeDefaultDetailLevels();
    }

    public Box(AirspaceAttributes attributes)
    {
        super(attributes);
        this.makeDefaultDetailLevels();
    }

    public Box()
    {
        this.makeDefaultDetailLevels();
    }

    private void makeDefaultDetailLevels()
    {
        List<DetailLevel> levels = new ArrayList<DetailLevel>();
        double[] ramp = ScreenSizeDetailLevel.computeDefaultScreenSizeRamp(5);

        DetailLevel level;
        level = new ScreenSizeDetailLevel(ramp[0], "Detail-Level-0");
        level.setValue(PILLARS, 8);
        level.setValue(STACKS, 2);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[1], "Detail-Level-1");
        level.setValue(PILLARS, 6);
        level.setValue(STACKS, 2);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[2], "Detail-Level-2");
        level.setValue(PILLARS, 4);
        level.setValue(STACKS, 2);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[3], "Detail-Level-3");
        level.setValue(PILLARS, 2);
        level.setValue(STACKS, 1);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, false);
        levels.add(level);

        level = new ScreenSizeDetailLevel(ramp[4], "Detail-Level-4");
        level.setValue(PILLARS, 1);
        level.setValue(STACKS, 1);
        level.setValue(DISABLE_TERRAIN_CONFORMANCE, true);
        levels.add(level);

        this.setDetailLevels(levels);
    }

    public LatLon[] getLocations()
    {
        return new LatLon[] {this.beginLocation, this.endLocation};
    }

    /**
     * Sets the leg's locations, in geographic coordinates.
     *
     * @param beginLocation geographic coordinates(latitude and longitude) specifying the center of the begining edge.
     * @param endLocation geographic coordinates(latitude and longitude) specifying the center of the ending edge.
     *
     * @throws IllegalArgumentException if location1 or location2 is null
     */
    public void setLocations(LatLon beginLocation, LatLon endLocation)
    {
        if (beginLocation == null || endLocation == null)
        {
            String message = Logging.getMessage("nullValue.LocationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.beginLocation = beginLocation;
        this.endLocation = endLocation;
        this.invalidateGeometry();
    }

    public double[] getWidths()
    {
        double[] array = new double[2];
        array[0] = this.leftWidth;
        array[1] = this.rightWidth;
        return array;
    }

    public void setWidths(double leftWidth, double rightWidth)
    {
        if (leftWidth < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "leftWidth=" + leftWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (rightWidth < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "rightWidth=" + rightWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.leftWidth = leftWidth;
        this.rightWidth = rightWidth;
        this.invalidateGeometry();
    }

    /**
     * Indicates the azimuth angles for this box's four corners, relative to geographic north. Angles are organized in
     * the returned array as follows: begin left, begin right, end left, end right. Null elements in any index indicate
     * that the default angle is used.
     *
     * @return an array of length four indicating this box's corner azimuths.
     */
    public Angle[] getCornerAzimuths()
    {
        return new Angle[] {this.beginLeftAzimuth, this.beginRightAzimuth, this.endLeftAzimuth, this.endRightAzimuth};
    }

    /**
     * Specifies the azimuth angles for this box's four corners, relative to geographic north. Specifying a null
     * argument indicates that the default angle should be used.
     */
    public void setCornerAzimuths(Angle beginLeftAzimuth, Angle beginRightAzimuth, Angle endLeftAzimuth,
        Angle endRightAzimuth)
    {
        this.beginLeftAzimuth = beginLeftAzimuth;
        this.beginRightAzimuth = beginRightAzimuth;
        this.endLeftAzimuth = endLeftAzimuth;
        this.endRightAzimuth = endRightAzimuth;
        this.invalidateGeometry();
    }

    public boolean[] isEnableCaps()
    {
        boolean[] array = new boolean[2];
        array[0] = this.enableStartCap;
        array[1] = this.enableEndCap;
        return array;
    }

    public void setEnableCaps(boolean enableStartCap, boolean enableEndCap)
    {
        this.enableStartCap = enableStartCap;
        this.enableEndCap = enableEndCap;
        this.invalidateGeometry();
    }

    public void setEnableCaps(boolean enable)
    {
        this.setEnableCaps(enable, enable);
    }

    public void setEnableStartCap(boolean enable)
    {
        this.setEnableCaps(enable, this.enableEndCap);
    }

    public void setEnableEndCap(boolean enable)
    {
        this.setEnableCaps(this.enableStartCap, enable);
    }

    public boolean isEnableCenterLine()
    {
        return this.enableCenterLine;
    }

    public void setEnableCenterLine(boolean enable)
    {
        this.enableCenterLine = enable;
        this.invalidateGeometry();
    }

    public Position getReferencePosition()
    {
        double[] altitudes = this.getAltitudes();
        return new Position(this.beginLocation, altitudes[0]);
    }

    protected void invalidateGeometry()
    {
        this.invalidateAirspaceData();
        this.geometryCacheKey = new Object();
    }

    protected gov.nasa.worldwind.geom.Box computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Vec4> points = this.computeMinimalGeometry(globe, verticalExaggeration);
        if (points == null || points.isEmpty())
            return null;

        return gov.nasa.worldwind.geom.Box.computeBoundingBox(points);
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        List<LatLon> locations = this.makeCapLocations(globe, DEFAULT_PILLARS, DEFAULT_STACKS);
        List<Vec4> points = new ArrayList<Vec4>();
        this.makeExtremePoints(globe, verticalExaggeration, locations, points);

        return points;
    }

    @Override
    protected SurfaceShape createSurfaceShape()
    {
        return new SurfaceBox();
    }

    @Override
    protected void regenerateSurfaceShape(DrawContext dc, SurfaceShape shape)
    {
        int lengthSegments = this.getPillars();
        int widthSegments = this.getStacks();
        List<LatLon> locations = this.makeSideLocations(dc.getGlobe(), lengthSegments, widthSegments);

        ((SurfaceBox) shape).setLocations(locations);
        ((SurfaceBox) shape).setLengthSegments(lengthSegments);
        ((SurfaceBox) shape).setWidthSegments(widthSegments);
        ((SurfaceBox) shape).setEnableCaps(this.isEnableCaps()[0], this.isEnableCaps()[1]);
        ((SurfaceBox) shape).setEnableCenterLine(this.isEnableCenterLine());
    }

    protected void doMoveTo(Globe globe, Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<LatLon> locations = new ArrayList<LatLon>(2);
        locations.add(this.getLocations()[0]);
        locations.add(this.getLocations()[1]);

        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldRef, newRef, locations);
        this.setLocations(newLocations.get(0), newLocations.get(1));

        super.doMoveTo(oldRef, newRef);
    }

    protected void doMoveTo(Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.doMoveTo(oldRef, newRef);

        LatLon[] locations = this.getLocations();
        int count = locations.length;
        for (int i = 0; i < count; i++)
        {
            double distance = LatLon.greatCircleDistance(oldRef, locations[i]).radians;
            double azimuth = LatLon.greatCircleAzimuth(oldRef, locations[i]).radians;
            locations[i] = LatLon.greatCircleEndPosition(newRef, azimuth, distance);
        }
        this.setLocations(locations[0], locations[1]);
    }

    protected boolean isForceCullFace()
    {
        return this.forceCullFace;
    }

    protected void setForceCullFace(boolean forceCullFace)
    {
        this.forceCullFace = forceCullFace;
    }

    protected int getPillars()
    {
        return this.pillars;
    }

    protected void setPillars(int pillars)
    {
        if (pillars < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.pillars = pillars;
    }

    protected int getStacks()
    {
        return this.stacks;
    }

    protected void setStacks(int stacks)
    {
        if (stacks < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.stacks = stacks;
    }

    protected int getHeightStacks()
    {
        return 1;
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    protected Vec4 computeReferenceCenter(DrawContext dc)
    {
        Extent extent = this.getExtent(dc);
        return extent != null ? extent.getCenter() : null;
    }

    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] altitudes = this.getAltitudes(dc.getVerticalExaggeration());
        boolean[] terrainConformant = this.isTerrainConforming();
        int lengthSegments = this.getPillars();
        int widthSegments = this.getStacks();

        if (this.isEnableLevelOfDetail())
        {
            DetailLevel level = this.computeDetailLevel(dc);

            Object o = level.getValue(PILLARS);
            if (o != null && o instanceof Integer)
                lengthSegments = (Integer) o;

            o = level.getValue(STACKS);
            if (o != null && o instanceof Integer)
                widthSegments = (Integer) o;

            o = level.getValue(DISABLE_TERRAIN_CONFORMANCE);
            if (o != null && o instanceof Boolean && (Boolean) o)
                terrainConformant[0] = terrainConformant[1] = false;
        }

        this.setExpiryTime(this.nextExpiryTime(dc, this.isTerrainConforming()));
        this.clearElevationMap();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        try
        {
            if (this.forceCullFace || !this.enableStartCap || !this.enableEndCap)
            {
                ogsh.pushAttrib(gl, GL2.GL_POLYGON_BIT);
                gl.glEnable(GL.GL_CULL_FACE);
                gl.glFrontFace(GL.GL_CCW);
            }

            if (Airspace.DRAW_STYLE_FILL.equals(drawStyle))
            {
                this.drawBox(dc, altitudes, terrainConformant, lengthSegments, widthSegments);
            }
            else if (Airspace.DRAW_STYLE_OUTLINE.equals(drawStyle))
            {
                this.drawBoxOutline(dc, altitudes, terrainConformant, lengthSegments, widthSegments);

                if (this.enableCenterLine)
                {
                    this.drawBoxCenterLine(dc, altitudes, terrainConformant, lengthSegments, widthSegments);
                }
            }
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    protected void applyCenterLineState(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        AirspaceAttributes attrs = this.getActiveAttributes();

        if (attrs.getOutlineStippleFactor() <= 0) // override stipple in attributes
        {
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.glLineStipple(Box.DEFAULT_CENTER_LINE_STIPPLE_FACTOR, Box.DEFAULT_CENTER_LINE_STIPPLE_PATTERN);
        }
    }

    //**************************************************************//
    //********************  Box  ***********************************//
    //**************************************************************//

    private static class BoxGeometry implements Cacheable
    {
        public Geometry sideGeometry = new Geometry();
        public Geometry capGeometry = new Geometry();
        public Geometry outlineIndices = new Geometry();
        public Geometry centerLineIndices = new Geometry();
        public Vec4 referencePoint;

        @Override
        public long getSizeInBytes()
        {
            return this.sideGeometry.getSizeInBytes()
                + this.capGeometry.getSizeInBytes()
                + this.outlineIndices.getSizeInBytes()
                + this.centerLineIndices.getSizeInBytes();
        }
    }

    private static class BoxCorners
    {
        public LatLon beginLeft;
        public LatLon beginRight;
        public LatLon endLeft;
        public LatLon endRight;

        public LatLon beginLeftProj;
        public LatLon beginRightProj;
        public LatLon endLeftProj;
        public LatLon endRightProj;

        public double leftArcLength;
        public double rightArcLength;
    }

    private void drawBox(DrawContext dc, double[] altitudes, boolean[] terrainConformant, int lengthSegments,
        int widthSegments)
    {
        BoxGeometry geom = this.getBoxGeometry(dc, altitudes, terrainConformant, lengthSegments, widthSegments);
        try
        {
            dc.getView().pushReferenceCenter(dc, geom.referencePoint);
            this.drawGeometry(dc, geom.sideGeometry, geom.sideGeometry);
            this.drawGeometry(dc, geom.capGeometry, geom.capGeometry);
        }
        finally
        {
            dc.getView().popReferenceCenter(dc);
        }
    }

    private void drawBoxOutline(DrawContext dc, double[] altitudes, boolean[] terrainConformant, int lengthSegments,
        int widthSegments)
    {
        BoxGeometry geom = this.getBoxGeometry(dc, altitudes, terrainConformant, lengthSegments, widthSegments);
        try
        {
            dc.getView().pushReferenceCenter(dc, geom.referencePoint);
            this.drawGeometry(dc, geom.outlineIndices, geom.sideGeometry);
        }
        finally
        {
            dc.getView().popReferenceCenter(dc);
        }
    }

    private void drawBoxCenterLine(DrawContext dc, double[] altitudes, boolean[] terrainConformant, int lengthSegments,
        int widthSegments)
    {
        BoxGeometry geom = this.getBoxGeometry(dc, altitudes, terrainConformant, lengthSegments, widthSegments);
        try
        {
            dc.getView().pushReferenceCenter(dc, geom.referencePoint);
            dc.pushProjectionOffest(DEFAULT_CENTER_LINE_OFFSET); // move center line depth slightly in front of fill
            this.applyCenterLineState(dc);
            this.drawGeometry(dc, geom.centerLineIndices, geom.capGeometry);
        }
        finally
        {
            dc.popProjectionOffest();
            dc.getView().popReferenceCenter(dc);
        }
    }

    private BoxGeometry getBoxGeometry(DrawContext dc, double[] altitudes, boolean[] terrainConformant,
        int lengthSegments, int widthSegments)
    {
        Object cacheKey = new Geometry.CacheKey(dc.getGlobe(), this.getClass(), "Box.Geometry", this.geometryCacheKey,
            altitudes, terrainConformant, lengthSegments, widthSegments);
        BoxGeometry geom = (BoxGeometry) this.getGeometryCache().getObject(cacheKey);

        if (geom != null && !this.isExpired(dc, geom.sideGeometry))
            return geom;

        if (geom == null)
            geom = new BoxGeometry();

        this.makeBoxGeometry(dc, altitudes, terrainConformant, lengthSegments, widthSegments, geom);
        this.updateExpiryCriteria(dc, geom.sideGeometry);
        this.getGeometryCache().add(cacheKey, geom);

        return geom;
    }

    private void makeBoxGeometry(DrawContext dc, double[] altitudes, boolean[] terrainConformant, int lengthSegments,
        int widthSegments, BoxGeometry geom)
    {
        geom.referencePoint = this.computeReferenceCenter(dc);
        this.makeSideGeometry(dc.getTerrain(), altitudes, terrainConformant, lengthSegments, widthSegments, geom);
        this.makeCapGeometry(dc.getTerrain(), altitudes, terrainConformant, lengthSegments, widthSegments, geom);
    }

    private void makeSideGeometry(Terrain terrain, double[] altitudes, boolean[] terrainConformant, int lengthSegments,
        int widthSegments, BoxGeometry geom)
    {
        List<LatLon> locations = this.makeSideLocations(terrain.getGlobe(), lengthSegments, widthSegments);

        // Compute model coordinate vertex points.
        int vertexCount = 2 * locations.size(); // upper altitude and lower altitude vertices
        float[] pointArray = new float[3 * vertexCount];
        FloatBuffer pointBuffer = FloatBuffer.wrap(pointArray);

        for (LatLon ll : locations)
        {
            for (int i = 1; i >= 0; i--) // upper altitude then lower altitude
            {
                Vec4 p = terrainConformant[i] ?
                    terrain.getSurfacePoint(ll.latitude, ll.longitude, altitudes[i]) :
                    terrain.getGlobe().computePointFromPosition(ll.latitude, ll.longitude, altitudes[i]);
                pointBuffer.put((float) (p.x - geom.referencePoint.x));
                pointBuffer.put((float) (p.y - geom.referencePoint.y));
                pointBuffer.put((float) (p.z - geom.referencePoint.z));
            }
        }

        // Compute triangle indices and line segment indices.
        int[] sideSegments = {2 * widthSegments, lengthSegments, 2 * widthSegments, lengthSegments};
        boolean[] sideFlag = {this.enableStartCap, true, this.enableEndCap, true};
        int indexCount = 0;
        int outlineCount = 8;

        // Count the number of triangle and line segment indices, depending on whether each end cap is enabled.
        for (int i = 0; i < 4; i++)
        {
            if (sideFlag[i])
            {
                indexCount += 6 * sideSegments[i];
                outlineCount += 4 * sideSegments[i];
            }
        }

        int index = 0;
        int[] indexArray = new int[indexCount];
        int[] outlineArray = new int[outlineCount];
        IntBuffer indexBuffer = IntBuffer.wrap(indexArray);
        IntBuffer outlineBuffer = IntBuffer.wrap(outlineArray);

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < sideSegments[i]; j++)
            {
                if (sideFlag[i])
                {
                    indexBuffer.put(index).put(index + 1).put(index + 2); // upper left triangle
                    indexBuffer.put(index + 2).put(index + 1).put(index + 3); // lower right triangle
                    outlineBuffer.put(index).put(index + 2); // upper altitude segment
                    outlineBuffer.put(index + 1).put(index + 3); // lower altitude segment
                }
                index += 2;
            }

            outlineBuffer.put(index).put(index + 1); // vertical segment
            index += 2; // advance to the beginning of the next segment
        }

        // Compute model coordinate vertex normals.
        float[] normalArray = new float[3 * vertexCount];
        GeometryBuilder gb = new GeometryBuilder();
        gb.makeIndexedTriangleArrayNormals(0, indexCount, indexArray, 0, vertexCount, pointArray, normalArray);

        geom.sideGeometry.setVertexData(vertexCount, pointArray);
        geom.sideGeometry.setNormalData(vertexCount, normalArray);
        geom.sideGeometry.setElementData(GL.GL_TRIANGLES, indexCount, indexArray);
        geom.outlineIndices.setElementData(GL.GL_LINES, outlineCount, outlineArray);
    }

    private void makeCapGeometry(Terrain terrain, double[] altitudes, boolean[] terrainConformant, int lengthSegments,
        int widthSegments, BoxGeometry geom)
    {
        List<LatLon> locations = this.makeCapLocations(terrain.getGlobe(), lengthSegments, widthSegments);

        // Compute model coordinate vertex points.
        int vertexCount = 2 * locations.size(); // upper altitude and lower altitude vertices
        float[] pointArray = new float[3 * vertexCount];
        FloatBuffer pointBuffer = FloatBuffer.wrap(pointArray);

        for (LatLon ll : locations)
        {
            for (int i = 1; i >= 0; i--) // upper altitude then lower altitude
            {
                Vec4 p = terrainConformant[i] ?
                    terrain.getSurfacePoint(ll.latitude, ll.longitude, altitudes[i]) :
                    terrain.getGlobe().computePointFromPosition(ll.latitude, ll.longitude, altitudes[i]);
                pointBuffer.put((float) (p.x - geom.referencePoint.x));
                pointBuffer.put((float) (p.y - geom.referencePoint.y));
                pointBuffer.put((float) (p.z - geom.referencePoint.z));
            }
        }

        // Compute triangle indices.
        int cellCount = 4 * lengthSegments * widthSegments; // top/bottom cells for left and right sides
        int indexCount = 6 * cellCount; // six indices per cell
        int[] indexArray = new int[indexCount];
        IntBuffer indexBuffer = IntBuffer.wrap(indexArray);

        int index = 0;
        int rowStride = 4 * widthSegments + 2;

        // left top and bottom
        for (int i = 0; i < lengthSegments; i++)
        {
            for (int j = 0; j < 2 * widthSegments; j++)
            {
                // upper altitude triangles
                indexBuffer.put(index).put(index + 2).put(index + rowStride);
                indexBuffer.put(index + rowStride).put(index + 2).put(index + rowStride + 2);
                index++;
                // lower altitude triangles
                indexBuffer.put(index).put(index + rowStride).put(index + 2);
                indexBuffer.put(index + 2).put(index + rowStride).put(index + rowStride + 2);
                index++;
            }

            index += 2; // skip the last row of side vertices
        }

        // Compute center line indices.
        int segmentCount = 2 * lengthSegments + (this.enableStartCap ? 1 : 0) + (this.enableEndCap ? 1 : 0);
        int centerLineCount = 2 * segmentCount; // two indices per segment
        int[] centerLineArray = new int[centerLineCount];
        IntBuffer centerLineBuffer = IntBuffer.wrap(centerLineArray);

        index = 2 * widthSegments; // start at the first center vertex

        if (this.enableStartCap)
        {
            centerLineBuffer.put(index).put(index + 1); // begin vertical segment
        }

        for (int i = 0; i < lengthSegments; i++)
        {
            centerLineBuffer.put(index).put(index + rowStride); // upper altitude segment
            centerLineBuffer.put(index + 1).put(index + rowStride + 1); // lower altitude segment
            index += rowStride;
        }

        if (this.enableEndCap)
        {
            centerLineBuffer.put(index).put(index + 1); // end vertical segment
        }

        // Compute model coordinate vertex normals.
        float[] normalArray = new float[3 * vertexCount];
        GeometryBuilder gb = new GeometryBuilder();
        gb.makeIndexedTriangleArrayNormals(0, indexCount, indexArray, 0, vertexCount, pointArray, normalArray);

        geom.capGeometry.setVertexData(vertexCount, pointArray);
        geom.capGeometry.setNormalData(vertexCount, normalArray);
        geom.capGeometry.setElementData(GL.GL_TRIANGLES, indexCount, indexArray);
        geom.centerLineIndices.setElementData(GL.GL_LINES, centerLineCount, centerLineArray);
    }

    private List<LatLon> makeSideLocations(Globe globe, int lengthSegments, int widthSegments)
    {
        ArrayList<LatLon> locations = new ArrayList<LatLon>();
        BoxCorners corners = this.computeBoxCorners(globe);

        // begin side
        this.appendLocations(corners.beginLeft, this.beginLocation, corners.beginRight, widthSegments, locations);

        // right side
        locations.add(corners.beginRight);
        for (int i = 1; i < lengthSegments; i++)
        {
            double amount = (double) i / (double) lengthSegments;
            LatLon rightProj = LatLon.interpolateGreatCircle(amount, corners.beginRightProj, corners.endRightProj);
            double rightAzimuth = LatLon.greatCircleAzimuth(rightProj, corners.endRightProj).radians + (Math.PI / 2);
            LatLon right = LatLon.greatCircleEndPosition(rightProj, rightAzimuth, corners.rightArcLength);
            locations.add(right);
        }
        locations.add(corners.endRight);

        // end side
        this.appendLocations(corners.endRight, this.endLocation, corners.endLeft, widthSegments, locations);

        // left side
        locations.add(corners.endLeft);
        for (int i = 1; i < lengthSegments; i++)
        {
            double amount = (double) i / (double) lengthSegments;
            LatLon leftProj = LatLon.interpolateGreatCircle(amount, corners.endLeftProj, corners.beginLeftProj);
            double leftAzimuth = LatLon.greatCircleAzimuth(leftProj, corners.endLeftProj).radians - (Math.PI / 2);
            LatLon left = LatLon.greatCircleEndPosition(leftProj, leftAzimuth, corners.leftArcLength);
            locations.add(left);
        }
        locations.add(corners.beginLeft);

        return locations;
    }

    private List<LatLon> makeCapLocations(Globe globe, int lengthSegments, int widthSegments)
    {
        ArrayList<LatLon> locations = new ArrayList<LatLon>();
        BoxCorners corners = this.computeBoxCorners(globe);

        // begin row
        this.appendLocations(corners.beginLeft, this.beginLocation, corners.beginRight, widthSegments, locations);

        // interior rows
        for (int i = 1; i < lengthSegments; i++)
        {
            double amount = (double) i / (double) lengthSegments;
            LatLon center = LatLon.interpolateGreatCircle(amount, this.beginLocation, this.endLocation);
            LatLon leftProj = LatLon.interpolateGreatCircle(amount, corners.beginLeftProj, corners.endLeftProj);
            LatLon rightProj = LatLon.interpolateGreatCircle(amount, corners.beginRightProj, corners.endRightProj);
            double leftAzimuth = LatLon.greatCircleAzimuth(leftProj, corners.endLeftProj).radians - (Math.PI / 2);
            double rightAzimuth = LatLon.greatCircleAzimuth(rightProj, corners.endRightProj).radians + (Math.PI / 2);
            LatLon left = LatLon.greatCircleEndPosition(leftProj, leftAzimuth, corners.leftArcLength);
            LatLon right = LatLon.greatCircleEndPosition(rightProj, rightAzimuth, corners.rightArcLength);

            this.appendLocations(left, center, right, widthSegments, locations);
        }

        // end row
        this.appendLocations(corners.endLeft, this.endLocation, corners.endRight, widthSegments, locations);

        return locations;
    }

    private BoxCorners computeBoxCorners(Globe globe)
    {
        BoxCorners corners = new BoxCorners();
        double beginAzimuth = LatLon.greatCircleAzimuth(this.beginLocation, this.endLocation).radians;
        double endAzimuth = LatLon.greatCircleAzimuth(this.endLocation, this.beginLocation).radians;
        double centerArcLength = LatLon.greatCircleDistance(this.beginLocation, this.endLocation).radians;
        corners.leftArcLength = this.leftWidth / globe.getRadius();
        corners.rightArcLength = this.rightWidth / globe.getRadius();

        corners.beginLeft = LatLon.greatCircleEndPosition(this.beginLocation, beginAzimuth - (Math.PI / 2),
            corners.leftArcLength);
        corners.beginLeftProj = this.beginLocation;
        if (this.beginLeftAzimuth != null)
        {
            double arcAngle = beginAzimuth - this.beginLeftAzimuth.radians;
            double arcLength = Math.asin(Math.cos(arcAngle) * Math.sin(corners.leftArcLength) / Math.sin(arcAngle));
            double sideLength = Math.asin(Math.sin(corners.leftArcLength) / Math.sin(arcAngle));
            if (arcLength < centerArcLength)
            {
                corners.beginLeft = LatLon.greatCircleEndPosition(this.beginLocation, this.beginLeftAzimuth.radians,
                    sideLength);
                corners.beginLeftProj = LatLon.greatCircleEndPosition(this.beginLocation, beginAzimuth, arcLength);
            }
        }

        corners.beginRight = LatLon.greatCircleEndPosition(this.beginLocation, beginAzimuth + (Math.PI / 2),
            corners.rightArcLength);
        corners.beginRightProj = this.beginLocation;
        if (this.beginRightAzimuth != null)
        {
            double arcAngle = this.beginRightAzimuth.radians - beginAzimuth;
            double arcLength = Math.asin(Math.cos(arcAngle) * Math.sin(corners.rightArcLength) / Math.sin(arcAngle));
            double sideLength = Math.asin(Math.sin(corners.rightArcLength) / Math.sin(arcAngle));
            if (arcLength < centerArcLength)
            {
                corners.beginRight = LatLon.greatCircleEndPosition(this.beginLocation, this.beginRightAzimuth.radians,
                    sideLength);
                corners.beginRightProj = LatLon.greatCircleEndPosition(this.beginLocation, beginAzimuth, arcLength);
            }
        }

        corners.endLeft = LatLon.greatCircleEndPosition(this.endLocation, endAzimuth + (Math.PI / 2),
            corners.leftArcLength);
        corners.endLeftProj = this.endLocation;
        if (this.endLeftAzimuth != null)
        {
            double arcAngle = this.endLeftAzimuth.radians - endAzimuth;
            double arcLength = Math.asin(Math.cos(arcAngle) * Math.sin(corners.leftArcLength) / Math.sin(arcAngle));
            double sideLength = Math.asin(Math.sin(corners.leftArcLength) / Math.sin(arcAngle));
            if (arcLength < centerArcLength)
            {
                corners.endLeft = LatLon.greatCircleEndPosition(this.endLocation, this.endLeftAzimuth.radians,
                    sideLength);
                corners.endLeftProj = LatLon.greatCircleEndPosition(this.endLocation, endAzimuth, arcLength);
            }
        }

        corners.endRight = LatLon.greatCircleEndPosition(this.endLocation, endAzimuth - (Math.PI / 2),
            corners.rightArcLength);
        corners.endRightProj = this.endLocation;
        if (this.endRightAzimuth != null)
        {
            double arcAngle = endAzimuth - this.endRightAzimuth.radians;
            double arcLength = Math.asin(Math.cos(arcAngle) * Math.sin(corners.rightArcLength) / Math.sin(arcAngle));
            double sideLength = Math.asin(Math.sin(corners.rightArcLength) / Math.sin(arcAngle));
            if (arcLength < centerArcLength)
            {
                corners.endRight = LatLon.greatCircleEndPosition(this.endLocation, this.endRightAzimuth.radians,
                    sideLength);
                corners.endRightProj = LatLon.greatCircleEndPosition(this.endLocation, endAzimuth, arcLength);
            }
        }

        return corners;
    }

    private void appendLocations(LatLon begin, LatLon middle, LatLon end, int numSegments, List<LatLon> result)
    {
        for (int i = 0; i <= numSegments; i++)
        {
            double amount = (double) i / (double) numSegments;
            result.add(LatLon.interpolateGreatCircle(amount, begin, middle));
        }

        for (int i = 1; i <= numSegments; i++) // skip the first segment, it's already covered in the above loop
        {
            double amount = (double) i / (double) numSegments;
            result.add(LatLon.interpolateGreatCircle(amount, middle, end));
        }
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsLatLon(context, "location1", this.beginLocation);
        rs.addStateValueAsLatLon(context, "location2", this.endLocation);
        rs.addStateValueAsDouble(context, "leftWidth", this.leftWidth);
        rs.addStateValueAsDouble(context, "rightWidth", this.rightWidth);
        rs.addStateValueAsBoolean(context, "enableStartCap", this.enableStartCap);
        rs.addStateValueAsBoolean(context, "enableEndCap", this.enableEndCap);
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        LatLon loc1 = rs.getStateValueAsLatLon(context, "location1");
        if (loc1 == null)
            loc1 = this.getLocations()[0];

        LatLon loc2 = rs.getStateValueAsLatLon(context, "location2");
        if (loc2 == null)
            loc2 = this.getLocations()[1];

        this.setLocations(loc1, loc2);

        Double lw = rs.getStateValueAsDouble(context, "leftWidth");
        if (lw == null)
            lw = this.getWidths()[0];

        Double rw = rs.getStateValueAsDouble(context, "rightWidth");
        if (rw == null)
            rw = this.getWidths()[1];

        this.setWidths(lw, rw);

        Boolean enableStart = rs.getStateValueAsBoolean(context, "enableStartCap");
        if (enableStart == null)
            enableStart = this.isEnableCaps()[0];

        Boolean enableEnd = rs.getStateValueAsBoolean(context, "enableEndCap");
        if (enableEnd == null)
            enableEnd = this.isEnableCaps()[1];

        this.setEnableCaps(enableStart, enableEnd);
    }
}
