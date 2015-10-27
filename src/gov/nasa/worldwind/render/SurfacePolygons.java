/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Renders fast multiple polygons with or without holes in one pass. It relies on a {@link
 * gov.nasa.worldwind.util.CompoundVecBuffer}.
 * <p/>
 * Whether a polygon ring is filled or is a hole in another polygon depends on the vertices winding order and the
 * winding rule used - see setWindingRule(String).
 *
 * @author Dave Collins
 * @author Patrick Murris
 * @version $Id: SurfacePolygons.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfacePolygons extends SurfacePolylines // TODO: Review
{
    protected int[] polygonRingGroups;
    protected String windingRule = AVKey.CLOCKWISE;
    protected boolean needsInteriorTessellation = true;
    protected WWTexture texture;
    protected Object interiorDisplayListCacheKey = new Object();

    public SurfacePolygons(CompoundVecBuffer buffer)
    {
        super(buffer);
    }

    public SurfacePolygons(Sector sector, CompoundVecBuffer buffer)
    {
        super(sector, buffer);
    }

    /**
     * Get a copy of the polygon ring groups array - can be null.
     * <p/>
     * When not null the polygon ring groups array identifies the starting sub buffer index for each polygon. In that
     * case rings from a same group will be tesselated together as part of the same polygon.
     * <p/>
     * When <code>null</code> polygon rings that follow the current winding rule are tessellated separatly as different
     * polygons. Rings that are reverse winded are considered holes to be applied to the last straight winded ring
     * polygon.
     *
     * @return a copy of the polygon ring groups array - can be null.
     */
    public int[] getPolygonRingGroups()
    {
        return this.polygonRingGroups.clone();
    }

    /**
     * Set the polygon ring groups array - can be null.
     * <p/>
     * When not null the polygon ring groups array identifies the starting sub buffer index for each polygon. In that
     * case rings from a same group will be tesselated together as part of the same polygon.
     * <p/>
     * When <code>null</code> polygon rings that follow the current winding rule are tessellated separatly as different
     * polygons. Rings that are reverse winded are considered holes to be applied to the last straight winded ring
     * polygon.
     *
     * @param ringGroups a copy of the polygon ring groups array - can be null.
     */
    public void setPolygonRingGroups(int[] ringGroups)
    {
        this.polygonRingGroups = ringGroups.clone();
        this.onGeometryChanged();
    }

    /**
     * Get the winding rule used when tessellating polygons. Can be one of {@link AVKey#CLOCKWISE} (default) or {@link
     * AVKey#COUNTER_CLOCKWISE}.
     * <p/>
     * When set to {@link AVKey#CLOCKWISE} polygons which run clockwise will be filled and those which run counter
     * clockwise will produce 'holes'. The interpretation is reversed when the winding rule is set to {@link
     * AVKey#COUNTER_CLOCKWISE}.
     *
     * @return the winding rule used when tessellating polygons.
     */
    public String getWindingRule()
    {
        return this.windingRule;
    }

    /**
     * Set the winding rule used when tessellating polygons. Can be one of {@link AVKey#CLOCKWISE} (default) or {@link
     * AVKey#COUNTER_CLOCKWISE}.
     * <p/>
     * When set to {@link AVKey#CLOCKWISE} polygons which run clockwise will be filled and those which run counter
     * clockwise will produce 'holes'. The interpretation is reversed when the winding rule is set to {@link
     * AVKey#COUNTER_CLOCKWISE}.
     *
     * @param windingRule the winding rule to use when tessellating polygons.
     */
    public void setWindingRule(String windingRule)
    {
        this.windingRule = windingRule;
        this.onGeometryChanged();
    }

    protected void onGeometryChanged()
    {
        this.needsInteriorTessellation = true;
        super.onGeometryChanged();
    }

    protected void drawInterior(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Exit immediately if the polygon has no coordinate data.
        if (this.buffer.size() == 0)
            return;

        Position referencePos = this.getReferencePosition();
        if (referencePos == null)
            return;

        // Attempt to tessellate the polygon's interior if the polygon's interior display list is uninitialized, or if
        // the polygon is marked as needing tessellation.
        int[] dlResource = (int[]) dc.getGpuResourceCache().get(this.interiorDisplayListCacheKey);
        if (dlResource == null || this.needsInteriorTessellation)
            dlResource = this.tessellateInterior(dc, referencePos);

        // Exit immediately if the polygon's interior failed to tessellate. The cause has already been logged by
        // tessellateInterior().
        if (dlResource == null)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.applyInteriorState(dc, sdc, this.getActiveAttributes(), this.getTexture(), referencePos);
        gl.glCallList(dlResource[0]);

        if (this.crossesDateLine)
        {
            gl.glPushMatrix();
            try
            {
                // Apply hemisphere offset and draw again
                double hemisphereSign = Math.signum(referencePos.getLongitude().degrees);
                gl.glTranslated(360 * hemisphereSign, 0, 0);
                gl.glCallList(dlResource[0]);
            }
            finally
            {
                gl.glPopMatrix();
            }
        }
    }

    protected WWTexture getTexture()
    {
        if (this.getActiveAttributes().getImageSource() == null)
            return null;

        if (this.texture == null && this.getActiveAttributes().getImageSource() != null)
            this.texture = new BasicWWTexture(this.getActiveAttributes().getImageSource(), true);

        return this.texture;
    }

    //**************************************************************//
    //********************  Interior Tessellation  *****************//
    //**************************************************************//

    protected int[] tessellateInterior(DrawContext dc, LatLon referenceLocation)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            return this.doTessellateInterior(dc, referenceLocation);
        }
        catch (OutOfMemoryError e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(Level.SEVERE, message, e);

            //noinspection ThrowableInstanceNeverThrown
            dc.addRenderingException(new WWRuntimeException(message, e));

            this.handleUnsuccessfulInteriorTessellation(dc);

            return null;
        }
    }

    protected int[] doTessellateInterior(DrawContext dc, LatLon referenceLocation)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        GLUtessellatorCallback cb = GLUTessellatorSupport.createOGLDrawPrimitivesCallback(gl);

        int[] dlResource = new int[] {gl.glGenLists(1), 1};
        GLUTessellatorSupport glts = new GLUTessellatorSupport();

        try
        {
            glts.beginTessellation(cb, new Vec4(0, 0, 1));
            gl.glNewList(dlResource[0], GL2.GL_COMPILE);
            int numBytes = this.tessellateInteriorVertices(glts.getGLUtessellator(), referenceLocation);
            glts.endTessellation();
            gl.glEndList();
            this.needsInteriorTessellation = false;

            dc.getGpuResourceCache().put(this.interiorDisplayListCacheKey, dlResource, GpuResourceCache.DISPLAY_LISTS,
                numBytes);

            return dlResource;
        }
        catch (Throwable e)
        {
            // Free any heap memory used for tessellation immediately. If tessellation has consumed all available heap
            // memory, we must free memory used by tessellation immediately or subsequent operations such as message
            // logging will fail.
            gl.glEndList();
            glts.endTessellation();
            gl.glDeleteLists(dlResource[0], dlResource[1]);

            String message = Logging.getMessage("generic.ExceptionWhileTessellating", this);
            Logging.logger().log(Level.SEVERE, message, e);

            //noinspection ThrowableInstanceNeverThrown
            dc.addRenderingException(new WWRuntimeException(message, e));

            this.handleUnsuccessfulInteriorTessellation(dc);

            return null;
        }
    }

    protected void handleUnsuccessfulInteriorTessellation(DrawContext dc)
    {
        // If tessellating the polygon's interior was unsuccessful, we modify the polygon to avoid any additional
        // tessellation attempts, and free any resources that the polygon won't use.

        // Replace the polygon's coordinate buffer with an empty CompoundVecBuffer. This ensures that any rendering
        // code won't attempt to re-tessellate this polygon.
        this.buffer = CompoundVecBuffer.emptyCompoundVecBuffer(2);
        // Flag the polygon as having changed, since we've replaced its coordinate buffer with an empty
        // CompoundVecBuffer.
        this.onGeometryChanged();
    }

    protected int tessellateInteriorVertices(GLUtessellator tess, LatLon referenceLocation)
    {
        // Setup the winding order to correctly tessellate the outer and inner rings.
        GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, this.windingRule.equals(AVKey.CLOCKWISE) ?
            GLU.GLU_TESS_WINDING_NEGATIVE : GLU.GLU_TESS_WINDING_POSITIVE);

        this.crossesDateLine = false;

        int numBytes = 0;
        int numRings = this.buffer.size();
        if (this.polygonRingGroups == null)
        {
            boolean inBeginPolygon = false;

            // Polygon rings are drawn following the sub buffers order. If the winding rule is CW all clockwise
            // rings are considered an outer ring possibly followed by counter clock wise inner rings.
            for (int i = 0; i < numRings; i++)
            {
                VecBuffer vecBuffer = this.buffer.subBuffer(i);
                numBytes += vecBuffer.getSize() * 3 * 4; // 3 float coords per vertex

                // Start a new polygon for each outer ring
                if (WWMath.computeWindingOrderOfLocations(vecBuffer.getLocations()).equals(this.getWindingRule()))
                {
                    if (inBeginPolygon)
                        GLU.gluTessEndPolygon(tess);

                    GLU.gluTessBeginPolygon(tess, null);
                    inBeginPolygon = true;
                }

                if (tessellateRing(tess, vecBuffer, referenceLocation))
                    this.crossesDateLine = true;
            }

            if (inBeginPolygon)
                GLU.gluTessEndPolygon(tess);
        }
        else
        {
            // Tessellate one polygon per ring group
            int numGroups = this.polygonRingGroups.length;
            for (int group = 0; group < numGroups; group++)
            {
                int groupStart = this.polygonRingGroups[group];
                int groupLength = (group == numGroups - 1) ? numRings - groupStart
                    : this.polygonRingGroups[group + 1] - groupStart;

                GLU.gluTessBeginPolygon(tess, null);
                for (int i = 0; i < groupLength; i++)
                {
                    VecBuffer subBuffer = this.buffer.subBuffer(groupStart + i);
                    numBytes += subBuffer.getSize() * 3 * 4; // 3 float coords per vertex
                    if (tessellateRing(tess, subBuffer, referenceLocation))
                        this.crossesDateLine = true;
                }
                GLU.gluTessEndPolygon(tess);
            }
        }

        return numBytes;
    }

    protected boolean tessellateRing(GLUtessellator tess, VecBuffer vecBuffer, LatLon referenceLocation)
    {
        // Check for pole wrapping shape
        List<double[]> dateLineCrossingPoints = this.computeDateLineCrossingPoints(vecBuffer);
        int pole = this.computePole(dateLineCrossingPoints);
        double[] poleWrappingPoint = this.computePoleWrappingPoint(pole, dateLineCrossingPoints);

        GLU.gluTessBeginContour(tess);
        Iterable<double[]> iterable = vecBuffer.getCoords(3);
        boolean dateLineCrossed = false;
        int sign = 0;
        double[] previousPoint = null;
        for (double[] coords : iterable)
        {
            if (poleWrappingPoint != null && previousPoint != null
                && poleWrappingPoint[0] == previousPoint[0] && poleWrappingPoint[1] == previousPoint[1])
            {
                previousPoint = coords.clone();

                // Wrapping a pole
                double[] dateLinePoint1 = this.computeDateLineEntryPoint(poleWrappingPoint, coords);
                double[] polePoint1 = new double[] {180 * Math.signum(poleWrappingPoint[0]), 90d * pole, 0};
                double[] dateLinePoint2 = dateLinePoint1.clone();
                double[] polePoint2 = polePoint1.clone();
                dateLinePoint2[0] *= -1;
                polePoint2[0] *= -1;

                // Move to date line then to pole
                tessVertex(tess, dateLinePoint1, referenceLocation);
                tessVertex(tess, polePoint1, referenceLocation);

                // Move to the other side of the date line
                tessVertex(tess, polePoint2, referenceLocation);
                tessVertex(tess, dateLinePoint2, referenceLocation);

                // Finally, draw current point past the date line
                tessVertex(tess, coords, referenceLocation);

                dateLineCrossed = true;
            }
            else
            {
                if (previousPoint != null && Math.abs(previousPoint[0] - coords[0]) > 180)
                {
                    // Crossing date line, sum departure point longitude sign for hemisphere offset
                    sign += (int) Math.signum(previousPoint[0]);
                    dateLineCrossed = true;
                }

                previousPoint = coords.clone();

                coords[0] += sign * 360;   // apply hemisphere offset
                tessVertex(tess, coords, referenceLocation);
            }
        }
        GLU.gluTessEndContour(tess);

        return dateLineCrossed;
    }

    private static void tessVertex(GLUtessellator tess, double[] coords, LatLon referenceLocation)
    {
        double[] vertex = new double[3];
        vertex[0] = coords[0] - referenceLocation.getLongitude().degrees;
        vertex[1] = coords[1] - referenceLocation.getLatitude().degrees;
        GLU.gluTessVertex(tess, vertex, 0, vertex);
    }

    // --- Pole wrapping shapes handling ---

    protected List<double[]> computeDateLineCrossingPoints(VecBuffer vecBuffer)
    {
        // Shapes that include a pole will yield an odd number of points
        List<double[]> list = new ArrayList<double[]>();
        Iterable<double[]> iterable = vecBuffer.getCoords(3);
        double[] previousPoint = null;
        for (double[] coords : iterable)
        {
            if (previousPoint != null && Math.abs(previousPoint[0] - coords[0]) > 180)
                list.add(previousPoint);
            previousPoint = coords;
        }

        return list;
    }

    protected int computePole(List<double[]> dateLineCrossingPoints)
    {
        int sign = 0;
        for (double[] point : dateLineCrossingPoints)
        {
            sign += Math.signum(point[0]);
        }

        if (sign == 0)
            return 0;

        // If we cross the date line going west (from a negative longitude) with a clockwise polygon,
        // then the north pole (positive) is included.
        return this.getWindingRule().equals(AVKey.CLOCKWISE) && sign < 0 ? 1 : -1;
    }

    protected double[] computePoleWrappingPoint(int pole, List<double[]> dateLineCrossingPoints)
    {
        if (pole == 0)
            return null;

        // Find point with latitude closest to pole
        int idx = -1;
        double max = pole < 0 ? 90 : -90;
        for (int i = 0; i < dateLineCrossingPoints.size(); i++)
        {
            double[] point = dateLineCrossingPoints.get(i);
            if (pole < 0 && point[1] < max) // increasing latitude toward north pole
            {
                idx = i;
                max = point[1];
            }
            if (pole > 0 && point[1] > max) // decreasing latitude toward south pole
            {
                idx = i;
                max = point[1];
            }
        }

        return dateLineCrossingPoints.get(idx);
    }

    protected double[] computeDateLineEntryPoint(double[] from, double[] to)
    {
        // Linear interpolation between from and to at the date line
        double dLat = to[1] - from[1];
        double dLon = 360 - Math.abs(to[0] - from[0]);
        double s = Math.abs(180 * Math.signum(from[0]) - from[0]) / dLon;
        double lat = from[1] + dLat * s;
        double lon = 180 * Math.signum(from[0]); // same side as from

        return new double[] {lon, lat, 0};
    }
}
