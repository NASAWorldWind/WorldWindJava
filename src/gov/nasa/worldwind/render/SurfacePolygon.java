/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import javax.xml.stream.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: SurfacePolygon.java 3436 2015-10-28 17:43:24Z tgaskins $
 */
@SuppressWarnings("unchecked")
public class SurfacePolygon extends AbstractSurfaceShape implements Exportable
{
    protected static class ShapeData
    {
        public int vertexStride;
        public boolean hasTexCoords;
        public FloatBuffer vertices;
        public IntBuffer interiorIndices;
        public IntBuffer outlineIndices;
    }

    protected static class Vertex extends LatLon
    {
        public double u;
        public double v;
        public boolean edgeFlag = true;

        public Vertex(LatLon location)
        {
            super(location);
        }

        public Vertex(Angle latitude, Angle longitude, double u, double v)
        {
            super(latitude, longitude);
            this.u = u;
            this.v = v;
        }
    }

    /* The polygon's boundaries. */
    protected List<Iterable<? extends LatLon>> boundaries = new ArrayList<Iterable<? extends LatLon>>();
    /** If an image source was specified, this is the WWTexture form. */
    protected WWTexture explicitTexture;
    /** This shape's texture coordinates. */
    protected float[] explicitTextureCoords;

    protected Map<Object, ShapeData> shapeDataCache = new HashMap<Object, ShapeData>();
    protected static GLUtessellator tess;
    protected static GLUTessellatorSupport.CollectPrimitivesCallback tessCallback;

    /** Constructs a new surface polygon with the default attributes and no locations. */
    public SurfacePolygon()
    {
    }

    /**
     * Creates a shallow copy of the specified source shape.
     *
     * @param source the shape to copy.
     */
    public SurfacePolygon(SurfacePolygon source)
    {
        super(source);

        this.boundaries.addAll(source.boundaries);
    }

    /**
     * Constructs a new surface polygon with the specified normal (as opposed to highlight) attributes and no locations.
     * Modifying the attribute reference after calling this constructor causes this shape's appearance to change
     * accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public SurfacePolygon(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new surface polygon with the default attributes and the specified iterable of locations.
     * <p/>
     * Note: If fewer than three locations is specified, no polygon is drawn.
     *
     * @param iterable the polygon locations.
     *
     * @throws IllegalArgumentException if the locations iterable is null.
     */
    public SurfacePolygon(Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(iterable);
    }

    /**
     * Constructs a new surface polygon with the specified normal (as opposed to highlight) attributes and the specified
     * iterable of locations. Modifying the attribute reference after calling this constructor causes this shape's
     * appearance to change accordingly.
     * <p/>
     * Note: If fewer than three locations is specified, no polygon is drawn.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param iterable    the polygon locations.
     *
     * @throws IllegalArgumentException if the locations iterable is null.
     */
    public SurfacePolygon(ShapeAttributes normalAttrs, Iterable<? extends LatLon> iterable)
    {
        super(normalAttrs);

        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(iterable);
    }

    public Iterable<? extends LatLon> getLocations(Globe globe)
    {
        return this.getOuterBoundary();
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return this.getOuterBoundary();
    }

    public List<Iterable<? extends LatLon>> getBoundaries()
    {
        return this.boundaries;
    }

    public void setLocations(Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setOuterBoundary(iterable);
    }

    public Iterable<? extends LatLon> getOuterBoundary()
    {
        return this.boundaries.size() > 0 ? this.boundaries.get(0) : null;
    }

    public void setOuterBoundary(Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.boundaries.size() > 0)
            this.boundaries.set(0, iterable);
        else
            this.boundaries.add(iterable);

        this.onShapeChanged();
    }

    public void addInnerBoundary(Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.add(iterable);
        this.onShapeChanged();
    }

    /**
     * Returns this polygon's texture image source.
     *
     * @return the texture image source, or null if no source has been specified.
     */
    public Object getTextureImageSource()
    {
        return this.explicitTexture != null ? this.explicitTexture.getImageSource() : null;
    }

    /**
     * Returns the texture coordinates for this polygon.
     *
     * @return the texture coordinates, or null if no texture coordinates have been specified.
     */
    public float[] getTextureCoords()
    {
        return this.explicitTextureCoords;
    }

    /**
     * Specifies the texture to apply to this polygon.
     *
     * @param imageSource   the texture image source. May be a {@link String} identifying a file path or URL, a {@link
     *                      File}, or a {@link java.net.URL}.
     * @param texCoords     the (s, t) texture coordinates aligning the image to the polygon. There must be one texture
     *                      coordinate pair, (s, t), for each polygon location in the polygon's outer boundary.
     * @param texCoordCount the number of texture coordinates, (s, v) pairs, specified.
     *
     * @throws IllegalArgumentException if the image source is not null and either the texture coordinates are null or
     *                                  inconsistent with the specified texture-coordinate count, or there are fewer
     *                                  than three texture coordinate pairs.
     */
    public void setTextureImageSource(Object imageSource, float[] texCoords, int texCoordCount)
    {
        if (imageSource == null)
        {
            this.explicitTexture = null;
            this.explicitTextureCoords = null;
            this.onShapeChanged();
            return;
        }

        if (texCoords == null)
        {
            String message = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (texCoordCount < 3 || texCoords.length < 2 * texCoordCount)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.explicitTexture = new BasicWWTexture(imageSource, true);
        this.explicitTextureCoords = texCoords;
        this.onShapeChanged();
    }

    public Position getReferencePosition()
    {
        if (this.getOuterBoundary() == null)
            return null;

        Iterator<? extends LatLon> iterator = this.getOuterBoundary().iterator();
        if (!iterator.hasNext())
            return null;

        return new Position(iterator.next(), 0);
    }

    protected void clearCaches()
    {
        super.clearCaches();
        this.shapeDataCache.clear();
    }

    protected void doDrawGeographic(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        if (this.boundaries.isEmpty())
            return;

        Object key = this.createGeometryKey(dc, sdc);
        ShapeData shapeData = this.shapeDataCache.get(key);

        if (shapeData == null)
        {
            Angle degreesPerInterval = Angle.fromDegrees(1.0 / this.computeEdgeIntervalsPerDegree(sdc));
            List<List<Vertex>> contours = this.assembleContours(degreesPerInterval);
            shapeData = this.tessellateContours(contours);

            if (shapeData == null)
            {
                String msg = Logging.getMessage("generic.ExceptionWhileTessellating", this);
                dc.addRenderingException(new WWRuntimeException(msg));
                this.handleUnsuccessfulInteriorTessellation(dc); // clears boundaries, preventing repeat attempts
                return;
            }

            this.shapeDataCache.put(key, shapeData);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(2, GL.GL_FLOAT, shapeData.vertexStride, shapeData.vertices.position(0));

        if (shapeData.hasTexCoords)
        {
            gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL.GL_FLOAT, shapeData.vertexStride, shapeData.vertices.position(2));
        }

        ShapeAttributes attrs = this.getActiveAttributes();
        if (attrs.isDrawInterior())
        {
            this.applyInteriorState(dc, sdc, attrs, this.getInteriorTexture(), this.getReferencePosition());
            IntBuffer indices = shapeData.interiorIndices;
            gl.glDrawElements(GL.GL_TRIANGLES, indices.remaining(), GL.GL_UNSIGNED_INT, indices);
        }

        if (attrs.isDrawOutline())
        {
            this.applyOutlineState(dc, attrs);
            IntBuffer indices = shapeData.outlineIndices;
            gl.glDrawElements(GL.GL_LINES, indices.remaining(), GL.GL_UNSIGNED_INT, indices);
        }

        if (shapeData.hasTexCoords)
        {
            gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        }
    }

    protected void applyInteriorState(DrawContext dc, SurfaceTileDrawContext sdc, ShapeAttributes attributes,
        WWTexture texture, LatLon refLocation)
    {
        if (this.explicitTexture != null && !dc.isPickingMode())
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            OGLUtil.applyBlending(gl, true);
            OGLUtil.applyColor(gl, attributes.getInteriorMaterial().getDiffuse(), attributes.getInteriorOpacity(),
                true);

            if (this.explicitTexture.bind(dc))
            {
                this.explicitTexture.applyInternalTransform(dc);
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glDisable(GL2.GL_TEXTURE_GEN_S);
                gl.glDisable(GL2.GL_TEXTURE_GEN_T);
            }
        }
        else
        {
            super.applyInteriorState(dc, sdc, attributes, this.getInteriorTexture(), this.getReferencePosition());
        }
    }

    protected List<List<Vertex>> assembleContours(Angle maxEdgeLength)
    {
        List<List<Vertex>> result = new ArrayList<List<Vertex>>();

        for (int b = 0; b < this.boundaries.size(); b++)
        {
            Iterable<? extends LatLon> locations = this.boundaries.get(b);
            float[] texCoords = (b == 0) ? this.explicitTextureCoords : null;
            int c = 0;

            // Merge the boundary locations with their respective texture coordinates, if any.
            List<Vertex> contour = new ArrayList<Vertex>();
            for (LatLon location : locations)
            {
                Vertex vertex = new Vertex(location);
                contour.add(vertex);

                if (texCoords != null && texCoords.length > c)
                {
                    vertex.u = texCoords[c++];
                    vertex.v = texCoords[c++];
                }
            }

            // Interpolate the contour vertices according to this polygon's path type and number of edge intervals.
            this.closeContour(contour);
            this.subdivideContour(contour, maxEdgeLength);

            // Modify the contour vertices to compensate for the spherical nature of geographic coordinates.
            String pole = LatLon.locationsContainPole(contour);
            if (pole != null)
            {
                result.add(this.clipWithPole(contour, pole, maxEdgeLength));
            }
            else if (LatLon.locationsCrossDateLine(contour))
            {
                result.addAll(this.clipWithDateline(contour));
            }
            else
            {
                result.add(contour);
            }
        }

        return result;
    }

    protected void closeContour(List<Vertex> contour)
    {
        if (!contour.get(0).equals(contour.get(contour.size() - 1)))
        {
            contour.add(contour.get(0));
        }
    }

    protected void subdivideContour(List<Vertex> contour, Angle maxEdgeLength)
    {
        List<Vertex> original = new ArrayList<Vertex>(contour.size());
        original.addAll(contour);
        contour.clear();

        for (int i = 0; i < original.size() - 1; i++)
        {
            Vertex begin = original.get(i);
            Vertex end = original.get(i + 1);
            contour.add(begin);
            this.subdivideEdge(begin, end, maxEdgeLength, contour);
        }

        Vertex last = original.get(original.size() - 1);
        contour.add(last);
    }

    protected void subdivideEdge(Vertex begin, Vertex end, Angle maxEdgeLength, List<Vertex> result)
    {
        Vertex center = new Vertex(LatLon.interpolate(this.pathType, 0.5, begin, end));
        center.u = 0.5 * (begin.u + end.u);
        center.v = 0.5 * (begin.v + end.v);
        center.edgeFlag = begin.edgeFlag || end.edgeFlag;

        Angle edgeLength = LatLon.linearDistance(begin, end);
        if (edgeLength.compareTo(maxEdgeLength) > 0)
        {
            this.subdivideEdge(begin, center, maxEdgeLength, result);
            result.add(center);
            this.subdivideEdge(center, end, maxEdgeLength, result);
        }
        else
        {
            result.add(center);
        }
    }

    protected List<Vertex> clipWithPole(List<Vertex> contour, String pole, Angle maxEdgeLength)
    {
        List<Vertex> newVertices = new ArrayList<Vertex>();

        Angle poleLat = AVKey.NORTH.equals(pole) ? Angle.POS90 : Angle.NEG90;

        Vertex vertex = null;
        for (Vertex nextVertex : contour)
        {
            if (vertex != null)
            {
                newVertices.add(vertex);
                if (LatLon.locationsCrossDateline(vertex, nextVertex))
                {
                    // Determine where the segment crosses the dateline.
                    LatLon separation = LatLon.intersectionWithMeridian(vertex, nextVertex, Angle.POS180);
                    double sign = Math.signum(vertex.getLongitude().degrees);

                    Angle lat = separation.getLatitude();
                    Angle thisSideLon = Angle.POS180.multiply(sign);
                    Angle otherSideLon = thisSideLon.multiply(-1);

                    // Add locations that run from the intersection to the pole, then back to the intersection. Note
                    // that the longitude changes sign when the path returns from the pole.
                    //         . Pole
                    //      2 ^ | 3
                    //        | |
                    //      1 | v 4
                    // --->---- ------>
                    Vertex in = new Vertex(lat, thisSideLon, 0, 0);
                    Vertex inPole = new Vertex(poleLat, thisSideLon, 0, 0);
                    Vertex centerPole = new Vertex(poleLat, Angle.ZERO, 0, 0);
                    Vertex outPole = new Vertex(poleLat, otherSideLon, 0, 0);
                    Vertex out = new Vertex(lat, otherSideLon, 0, 0);
                    in.edgeFlag = inPole.edgeFlag = centerPole.edgeFlag = outPole.edgeFlag = out.edgeFlag = false;

                    double vertexDistance = LatLon.linearDistance(vertex, in).degrees;
                    double nextVertexDistance = LatLon.linearDistance(nextVertex, out).degrees;
                    double a = vertexDistance / (vertexDistance + nextVertexDistance);
                    in.u = out.u = WWMath.mix(a, vertex.u, nextVertex.u);
                    in.v = out.v = WWMath.mix(a, vertex.v, nextVertex.v);

                    double[] uv = this.uvWeightedAverage(contour, centerPole);
                    inPole.u = outPole.u = centerPole.u = uv[0];
                    inPole.v = outPole.v = centerPole.v = uv[1];

                    newVertices.add(in);
                    newVertices.add(inPole);
                    this.subdivideEdge(inPole, centerPole, maxEdgeLength, newVertices);
                    newVertices.add(centerPole);
                    this.subdivideEdge(centerPole, outPole, maxEdgeLength, newVertices);
                    newVertices.add(outPole);
                    newVertices.add(out);
                }
            }
            vertex = nextVertex;
        }
        newVertices.add(vertex);

        return newVertices;
    }

    protected double[] uvWeightedAverage(List<Vertex> contour, Vertex vertex)
    {
        double[] weight = new double[contour.size()];
        double sumOfWeights = 0;
        for (int i = 0; i < contour.size(); i++)
        {
            double distance = LatLon.greatCircleDistance(contour.get(i), vertex).degrees;
            weight[i] = 1 / distance;
            sumOfWeights += weight[i];
        }

        double u = 0;
        double v = 0;
        for (int i = 0; i < contour.size(); i++)
        {
            double factor = weight[i] / sumOfWeights;
            u += contour.get(i).u * factor;
            v += contour.get(i).v * factor;
        }

        return new double[] {u, v};
    }

    protected List<List<Vertex>> clipWithDateline(List<Vertex> contour)
    {
        List<Vertex> result = new ArrayList<Vertex>();
        Vertex prev = null;
        Angle offset = null;
        boolean applyOffset = false;

        for (Vertex cur : contour)
        {
            if (prev != null && LatLon.locationsCrossDateline(prev, cur))
            {
                if (offset == null)
                    offset = (prev.longitude.degrees < 0 ? Angle.NEG360 : Angle.POS360);
                applyOffset = !applyOffset;
            }

            if (applyOffset)
            {
                result.add(new Vertex(cur.latitude, cur.longitude.add(offset), cur.u, cur.v));
            }
            else
            {
                result.add(cur);
            }

            prev = cur;
        }

        List<Vertex> mirror = new ArrayList<Vertex>();
        for (Vertex cur : result)
        {
            mirror.add(new Vertex(cur.latitude, cur.longitude.subtract(offset), cur.u, cur.v));
        }

        return Arrays.asList(result, mirror);
    }

    protected ShapeData tessellateContours(List<List<Vertex>> contours)
    {
        List<Vertex> polygonData = new ArrayList<Vertex>();
        double[] coords = {0, 0, 0};

        if (tess == null)
        {
            tess = GLU.gluNewTess();
            tessCallback = new GLUTessellatorSupport.CollectPrimitivesCallback();
            tessCallback.attach(tess);
            GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, new GLUtessellatorCallbackAdapter()
            {
                @Override
                public void combineData(double[] coords, Object[] vertexData, float[] weight, Object[] outData, Object polygonData)
                {
                    List<Vertex> vertexList = (List<Vertex>) polygonData;
                    Vertex vertex = new Vertex(LatLon.fromDegrees(coords[1], coords[0]));
                    vertex.edgeFlag = false; // set to true if any of the combined vertices have the edge flag

                    for (int w = 0; w < 4; w++) {
                        if (weight[w] > 0) {
                            int index = ((GLUTessellatorSupport.VertexData) vertexData[w]).index;
                            Vertex tmp = vertexList.get(index);
                            vertex.u += weight[w] * tmp.u;
                            vertex.v += weight[w] * tmp.v;
                            vertex.edgeFlag |= tmp.edgeFlag;
                        }
                    }

                    int index = ((Collection) polygonData).size();
                    vertexList.add(vertex);

                    outData[0] = new GLUTessellatorSupport.VertexData(index, vertex.edgeFlag);
                }
            });
        }

        try
        {
            tessCallback.reset();
            GLU.gluTessNormal(tess, 0, 0, 1);
            GLU.gluTessBeginPolygon(tess, polygonData);

            for (List<Vertex> contour : contours)
            {
                GLU.gluTessBeginContour(tess);

                for (Vertex vertex : contour)
                {
                    coords[0] = vertex.longitude.degrees;
                    coords[1] = vertex.latitude.degrees;
                    int index = polygonData.size();
                    polygonData.add(vertex);
                    Object vertexData = new GLUTessellatorSupport.VertexData(index, vertex.edgeFlag);
                    GLU.gluTessVertex(tess, coords, 0, vertexData);
                }

                GLU.gluTessEndContour(tess);
            }

            GLU.gluTessEndPolygon(tess);
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("generic.ExceptionWhileTessellating", e.getMessage());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            return null;
        }

        if (tessCallback.getError() != 0)
        {
            String msg = Logging.getMessage("generic.ExceptionWhileTessellating",
                GLUTessellatorSupport.convertGLUTessErrorToString(tessCallback.getError()));
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            return null;
        }

        ShapeData shapeData = new ShapeData();
        shapeData.hasTexCoords = this.explicitTextureCoords != null;
        shapeData.vertexStride = shapeData.hasTexCoords ? 16 : 0;
        shapeData.vertices = Buffers.newDirectFloatBuffer(polygonData.size() * (shapeData.hasTexCoords ? 4 : 2));
        double lonOffset = this.getReferencePosition().longitude.degrees;
        double latOffset = this.getReferencePosition().latitude.degrees;
        for (Vertex vertex : polygonData)
        {
            shapeData.vertices.put((float) (vertex.longitude.degrees - lonOffset));
            shapeData.vertices.put((float) (vertex.latitude.degrees - latOffset));

            if (shapeData.hasTexCoords)
            {
                shapeData.vertices.put((float) vertex.u);
                shapeData.vertices.put((float) vertex.v);
            }
        }
        shapeData.vertices.rewind();

        IntBuffer tmp = tessCallback.getTriangleIndices();
        shapeData.interiorIndices = Buffers.newDirectIntBuffer(tmp.remaining());
        shapeData.interiorIndices.put(tmp);
        shapeData.interiorIndices.rewind();

        tmp = tessCallback.getLineIndices();
        shapeData.outlineIndices = Buffers.newDirectIntBuffer(tmp.remaining());
        shapeData.outlineIndices.put(tmp);
        shapeData.outlineIndices.rewind();

        return shapeData;
    }

    protected List<List<LatLon>> createGeometry(Globe globe, double edgeIntervalsPerDegree)
    {
        if (this.boundaries.isEmpty())
            return null;

        ArrayList<List<LatLon>> geom = new ArrayList<List<LatLon>>();

        for (Iterable<? extends LatLon> boundary : this.boundaries)
        {
            ArrayList<LatLon> drawLocations = new ArrayList<LatLon>();

            this.generateIntermediateLocations(boundary, edgeIntervalsPerDegree, true, drawLocations);

            // Ensure all contours have counter-clockwise winding order. The GLU tessellator we'll use to tessellate
            // these contours is configured to recognize interior holes when all contours have counter clockwise winding
            // order.
            //noinspection StringEquality
            if (WWMath.computeWindingOrderOfLocations(drawLocations) != AVKey.COUNTER_CLOCKWISE)
                Collections.reverse(drawLocations);

            geom.add(drawLocations);
        }

        if (geom.isEmpty() || geom.get(0).size() < 3)
            return null;

        return geom;
    }

    protected void doMoveTo(Position oldReferencePosition, Position newReferencePosition)
    {
        if (this.boundaries.isEmpty())
            return;

        for (int i = 0; i < this.boundaries.size(); i++)
        {
            ArrayList<LatLon> newLocations = new ArrayList<LatLon>();

            for (LatLon ll : this.boundaries.get(i))
            {
                Angle heading = LatLon.greatCircleAzimuth(oldReferencePosition, ll);
                Angle pathLength = LatLon.greatCircleDistance(oldReferencePosition, ll);
                newLocations.add(LatLon.greatCircleEndPosition(newReferencePosition, heading, pathLength));
            }

            this.boundaries.set(i, newLocations);
        }

        // We've changed the polygon's list of boundaries; flag the shape as changed.
        this.onShapeChanged();
    }

    protected void doMoveTo(Globe globe, Position oldReferencePosition, Position newReferencePosition)
    {
        if (this.boundaries.isEmpty())
            return;

        for (int i = 0; i < this.boundaries.size(); i++)
        {
            List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldReferencePosition,
                newReferencePosition, this.boundaries.get(i));

            this.boundaries.set(i, newLocations);
        }

        // We've changed the polygon's list of boundaries; flag the shape as changed.
        this.onShapeChanged();
    }

    //**************************************************************//
    //********************  Interior Tessellation  *****************//
    //**************************************************************//

    /**
     * Overridden to clear the polygon's locations iterable upon an unsuccessful tessellation attempt. This ensures the
     * polygon won't attempt to re-tessellate itself each frame.
     *
     * @param dc the current DrawContext.
     */
    @Override
    protected void handleUnsuccessfulInteriorTessellation(DrawContext dc)
    {
        super.handleUnsuccessfulInteriorTessellation(dc);

        // If tessellating the polygon's interior was unsuccessful, we modify the polygon's to avoid any additional
        // tessellation attempts, and free any resources that the polygon won't use. This is accomplished by clearing
        // the polygon's boundary list.
        this.boundaries.clear();
        this.onShapeChanged();
    }

    //**************************************************************//
    //******************** Restorable State  ***********************//
    //**************************************************************//

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        if (!this.boundaries.isEmpty())
        {
            RestorableSupport.StateObject so = rs.addStateObject(context, "boundaries");
            for (Iterable<? extends LatLon> boundary : this.boundaries)
            {
                rs.addStateValueAsLatLonList(so, "boundary", boundary);
            }
        }
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        RestorableSupport.StateObject so = rs.getStateObject(context, "boundaries");
        if (so != null)
        {
            this.boundaries.clear();

            RestorableSupport.StateObject[] sos = rs.getAllStateObjects(so, "boundary");
            if (sos != null)
            {
                for (RestorableSupport.StateObject boundary : sos)
                {
                    if (boundary == null)
                        continue;

                    Iterable<LatLon> locations = rs.getStateObjectAsLatLonList(boundary);
                    if (locations != null)
                        this.boundaries.add(locations);
                }
            }

            // We've changed the polygon's list of boundaries; flag the shape as changed.
            this.onShapeChanged();
        }
    }

    protected void legacyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.legacyRestoreState(rs, context);

        Iterable<LatLon> locations = rs.getStateValueAsLatLonList(context, "locationList");

        if (locations == null)
            locations = rs.getStateValueAsLatLonList(context, "locations");

        if (locations != null)
            this.setOuterBoundary(locations);
    }

    /**
     * Export the polygon to KML as a {@code <Placemark>} element. The {@code output} object will receive the data. This
     * object must be one of: java.io.Writer java.io.OutputStream javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     *
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @throws IOException        if an exception occurs while exporting the data.
     * @see #export(String, Object)
     */
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Placemark");

        String property = getStringValue(AVKey.DISPLAY_NAME);
        if (property != null)
        {
            xmlWriter.writeStartElement("name");
            xmlWriter.writeCharacters(property);
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("visibility");
        xmlWriter.writeCharacters(KMLExportUtil.kmlBoolean(this.isVisible()));
        xmlWriter.writeEndElement();

        String shortDescription = (String) getValue(AVKey.SHORT_DESCRIPTION);
        if (shortDescription != null)
        {
            xmlWriter.writeStartElement("Snippet");
            xmlWriter.writeCharacters(shortDescription);
            xmlWriter.writeEndElement();
        }

        String description = (String) getValue(AVKey.BALLOON_TEXT);
        if (description != null)
        {
            xmlWriter.writeStartElement("description");
            xmlWriter.writeCharacters(description);
            xmlWriter.writeEndElement();
        }

        // KML does not allow separate attributes for cap and side, so just use the side attributes.
        final ShapeAttributes normalAttributes = getAttributes();
        final ShapeAttributes highlightAttributes = getHighlightAttributes();

        // Write style map
        if (normalAttributes != null || highlightAttributes != null)
        {
            xmlWriter.writeStartElement("StyleMap");
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.NORMAL, normalAttributes);
            KMLExportUtil.exportAttributesAsKML(xmlWriter, KMLConstants.HIGHLIGHT, highlightAttributes);
            xmlWriter.writeEndElement(); // StyleMap
        }

        // Write geometry
        xmlWriter.writeStartElement("Polygon");

        xmlWriter.writeStartElement("extrude");
        xmlWriter.writeCharacters("0");
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters("clampToGround");
        xmlWriter.writeEndElement();

        // Outer boundary
        Iterable<? extends LatLon> outerBoundary = this.getOuterBoundary();
        if (outerBoundary != null)
        {
            xmlWriter.writeStartElement("outerBoundaryIs");
            KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, null);
            xmlWriter.writeEndElement(); // outerBoundaryIs
        }

        // Inner boundaries
        Iterator<Iterable<? extends LatLon>> boundaryIterator = boundaries.iterator();
        if (boundaryIterator.hasNext())
            boundaryIterator.next(); // Skip outer boundary, we already dealt with it above

        while (boundaryIterator.hasNext())
        {
            Iterable<? extends LatLon> boundary = boundaryIterator.next();

            xmlWriter.writeStartElement("innerBoundaryIs");
            KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, boundary, null);
            xmlWriter.writeEndElement(); // innerBoundaryIs
        }

        xmlWriter.writeEndElement(); // Polygon
        xmlWriter.writeEndElement(); // Placemark

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }
}
