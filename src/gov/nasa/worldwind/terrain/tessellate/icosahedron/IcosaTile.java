package gov.nasa.worldwind.terrain.tessellate.icosahedron;

import com.jogamp.opengl.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import java.awt.Point;
import java.nio.DoubleBuffer;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.geom.PolarPoint;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import java.util.List;
import gov.nasa.worldwind.terrain.*;

public class IcosaTile implements SectorGeometry {

    private static java.util.HashMap<Integer, double[]> parameterizations = new java.util.HashMap<Integer, double[]>();

    protected final int level;
    protected final GlobeInfo globeInfo;
    protected final LatLon g0, g1, g2;
    protected Sector sector; // lazily evaluated
    protected final Vec4 unitp0, unitp1, unitp2; // points on unit sphere
    protected final Vec4 p0;
    protected final Vec4 p1;
    protected final Vec4 p2;
    protected final Vec4 pCentroid;
    protected final Vec4 normal; // ellipsoids's normal vector at tile centroid
    protected final Cylinder extent; // extent of triangle in object coordinates
    protected final double edgeLength;
    protected int density = IcoSphereTessellator.DEFAULT_DENSITY;
    protected long byteSize;
    static final double ROOT3_OVER4 = Math.sqrt(3) / 4d;
    protected RenderInfo ri;

    // Icosatile from point vectors
    public IcosaTile(GlobeInfo globeInfo, int level, Vec4 unitp0, Vec4 unitp1, Vec4 unitp2) {
        // TODO: Validate args
        this.level = level;
        this.globeInfo = globeInfo;

        this.unitp0 = unitp0;
        this.unitp1 = unitp1;
        this.unitp2 = unitp2;

        // Compute lat/lon at tile vertices.
        Angle lat = Angle.fromRadians(Math.asin(this.unitp0.y));
        Angle lon = Angle.fromRadians(Math.atan2(this.unitp0.x, this.unitp0.z));
        this.g0 = new LatLon(lat, lon);
        lat = Angle.fromRadians(Math.asin(this.unitp1.y));
        lon = Angle.fromRadians(Math.atan2(this.unitp1.x, this.unitp1.z));
        this.g1 = new LatLon(lat, lon);
        lat = Angle.fromRadians(Math.asin(this.unitp2.y));
        lon = Angle.fromRadians(Math.atan2(this.unitp2.x, this.unitp2.z));
        this.g2 = new LatLon(lat, lon);

        // Compute the triangle corner points on the ellipsoid at mean, max and min elevations.
        this.p0 = this.scaleUnitPointToEllipse(this.unitp0, this.globeInfo.invAsq, this.globeInfo.invCsq);
        this.p1 = this.scaleUnitPointToEllipse(this.unitp1, this.globeInfo.invAsq, this.globeInfo.invCsq);
        this.p2 = this.scaleUnitPointToEllipse(this.unitp2, this.globeInfo.invAsq, this.globeInfo.invCsq);

        double a = 1d / 3d;
        Vec4 unitCentroid = getUnitPoint(a, a, this.unitp0, this.unitp1, this.unitp2);
        this.pCentroid = this.scaleUnitPointToEllipse(unitCentroid, this.globeInfo.invAsq, this.globeInfo.invCsq);

        // Compute the tile normal, which is the gradient of the ellipse at the centroid.
        double nx = 2 * this.pCentroid.x() * this.globeInfo.invAsq;
        double ny = 2 * this.pCentroid.y() * this.globeInfo.invCsq;
        double nz = 2 * this.pCentroid.z() * this.globeInfo.invAsq;
        this.normal = new Vec4(nx, ny, nz).normalize3();
        // this.extent = globeInfo.globe.computeBoundingCylinder(1d, this.getSector()); // original
        this.extent = Sector.computeBoundingCylinder(globeInfo.globe, 1d, this.getSector());

        this.edgeLength = this.globeInfo.level0EdgeLength / Math.pow(2, this.level);
    }

    // Icosatile from LatLons
    public IcosaTile(GlobeInfo globeInfo, int level, LatLon g0, LatLon g1, LatLon g2) {
        // TODO: Validate args
        this.level = level;
        this.globeInfo = globeInfo;

        this.g0 = g0;
        this.g1 = g1;
        this.g2 = g2;

        this.unitp0 = PolarPoint.toCartesian(this.g0.getLatitude(), this.g0.getLongitude(), 1);
        this.unitp1 = PolarPoint.toCartesian(this.g1.getLatitude(), this.g1.getLongitude(), 1);
        this.unitp2 = PolarPoint.toCartesian(this.g2.getLatitude(), this.g2.getLongitude(), 1);

        // Compute the triangle corner points on the ellipsoid at mean, max and min elevations.
        this.p0 = this.scaleUnitPointToEllipse(this.unitp0, this.globeInfo.invAsq, this.globeInfo.invCsq);
        this.p1 = this.scaleUnitPointToEllipse(this.unitp1, this.globeInfo.invAsq, this.globeInfo.invCsq);
        this.p2 = this.scaleUnitPointToEllipse(this.unitp2, this.globeInfo.invAsq, this.globeInfo.invCsq);

        double a = 1d / 3d;
        Vec4 unitCentroid = getUnitPoint(a, a, this.unitp0, this.unitp1, this.unitp2);
        this.pCentroid = this.scaleUnitPointToEllipse(unitCentroid, this.globeInfo.invAsq, this.globeInfo.invCsq);

        // Compute the tile normal, which is the gradient of the ellipse at the centroid.
        double nx = 2 * this.pCentroid.x() * this.globeInfo.invAsq;
        double ny = 2 * this.pCentroid.y() * this.globeInfo.invCsq;
        double nz = 2 * this.pCentroid.z() * this.globeInfo.invAsq;
        this.normal = new Vec4(nx, ny, nz).normalize3();
        //this.extent = globeInfo.globe.computeBoundingCylinder(1d, this.getSector()); // original
        this.extent = Sector.computeBoundingCylinder(globeInfo.globe, 1d, this.getSector());

        this.edgeLength = this.globeInfo.level0EdgeLength / Math.pow(2, this.level);
    }

    protected static double[] getParameterization(int density) {
        double[] p = parameterizations.get(density);
        if (p != null) {
            return p;
        }

        int coordCount = (density * density + 3 * density + 2) / 2;
        p = new double[2 * coordCount];
        double delta = 1d / density;
        int k = 0;
        for (int j = 0; j <= density; j++) {
            double v = j * delta;
            for (int i = 0; i <= density - j; i++) {
                p[k++] = i * delta; // u
                p[k++] = v;
            }
        }

        parameterizations.put(density, p);
        return p;
    }

    protected static java.nio.IntBuffer getIndices(int density) {
        java.nio.IntBuffer buffer = IcoSphereTessellator.indexLists.get(density);
        if (buffer != null) {
            return buffer;
        }

        int indexCount = density * density + 4 * density - 2;
        buffer = Buffers.newDirectIntBuffer(indexCount);
        int k = 0;
        for (int i = 0; i < density; i++) {
            buffer.put(k);
            if (i > 0) {
                k = buffer.get(buffer.position() - 3);
                buffer.put(k);
                buffer.put(k);
            }

            if (i % 2 == 0) // even
            {
                for (int j = 0; j < density - i; j++) {
                    ++k;
                    buffer.put(k);
                    k += density - j;
                    buffer.put(k);
                }
            } else // odd
            {
                for (int j = density - i - 1; j >= 0; j--) {
                    k -= density - j;
                    buffer.put(k);
                    --k;
                    buffer.put(k);
                }
            }
        }

        IcoSphereTessellator.indexLists.put(density, buffer);

        return buffer;
    }

    public static Vec4 getUnitPoint(double u, double v, Vec4 p0, Vec4 p1, Vec4 p2) {
        double w = 1d - u - v;
        double x = u * p1.x + v * p2.x + w * p0.x;
        double y = u * p1.y + v * p2.y + w * p0.y;
        double z = u * p1.z + v * p2.z + w * p0.z;
        double invLength = 1d / Math.sqrt(x * x + y * y + z * z);

        return new Vec4(x * invLength, y * invLength, z * invLength);
    }

    public static Vec4 getMidPoint(Vec4 p0, Vec4 p1) {
        return new Vec4(
                (p0.x + p1.x) / 2.0,
                (p0.y + p1.y) / 2.0,
                (p0.z + p1.z) / 2.0);
    }

    public ArrayList<LatLon> getLatLons() {
        ArrayList<LatLon> latLons = new ArrayList<LatLon>();
        latLons.add(this.g0);
        latLons.add(this.g1);
        latLons.add(this.g2);
        return latLons;
    }

    public Sector getSector() {
        if (this.sector != null) {
            return this.sector;
        }

        double m;

        m = this.g0.getLatitude().getRadians();
        if (this.g1.getLatitude().getRadians() < m) {
            m = this.g1.getLatitude().getRadians();
        }
        if (this.g2.getLatitude().getRadians() < m) {
            m = this.g2.getLatitude().getRadians();
        }
        Angle minLat = Angle.fromRadians(m);

        m = this.g0.getLatitude().getRadians();
        if (this.g1.getLatitude().getRadians() > m) {
            m = this.g1.getLatitude().getRadians();
        }
        if (this.g2.getLatitude().getRadians() > m) {
            m = this.g2.getLatitude().getRadians();
        }
        Angle maxLat = Angle.fromRadians(m);

        m = this.g0.getLongitude().getRadians();
        if (this.g1.getLongitude().getRadians() < m) {
            m = this.g1.getLongitude().getRadians();
        }
        if (this.g2.getLongitude().getRadians() < m) {
            m = this.g2.getLongitude().getRadians();
        }
        Angle minLon = Angle.fromRadians(m);

        m = this.g0.getLongitude().getRadians();
        if (this.g1.getLongitude().getRadians() > m) {
            m = this.g1.getLongitude().getRadians();
        }
        if (this.g2.getLongitude().getRadians() > m) {
            m = this.g2.getLongitude().getRadians();
        }
        Angle maxLon = Angle.fromRadians(m);

        return this.sector = new Sector(minLat, maxLat, minLon, maxLon);
    }

    public void renderBoundingVolume(DrawContext dc) {
        this.renderBoundingVolume(dc, this);
    }

    private Vec4 scaleUnitPointToEllipse(Vec4 up, double invAsq, double invCsq) {
        double f = up.x * up.x * invAsq + up.y * up.y * invCsq + up.z * up.z * invAsq;
        f = 1 / Math.sqrt(f);
        return new Vec4(up.x * f, up.y * f, up.z * f);
    }

    protected IcosaTile[] split() {
        Vec4 up01 = getMidPoint(this.p0, this.p1);
        Vec4 up12 = getMidPoint(this.p1, this.p2);
        Vec4 up20 = getMidPoint(this.p2, this.p0);
        up01 = up01.multiply3(1d / up01.getLength3());
        up12 = up12.multiply3(1d / up12.getLength3());
        up20 = up20.multiply3(1d / up20.getLength3());

        IcosaTile[] subTiles = new IcosaTile[4];
        subTiles[0] = new IcosaTile(this.globeInfo, this.level + 1, this.unitp0, up01, up20);
        subTiles[1] = new IcosaTile(this.globeInfo, this.level + 1, up01, this.unitp1, up12);
        subTiles[2] = new IcosaTile(this.globeInfo, this.level + 1, up20, up12, this.unitp2);
        subTiles[3] = new IcosaTile(this.globeInfo, this.level + 1, up12, up20, up01);

        return subTiles;
    }

    public String toString() {
        return this.level + ": (" + this.g0.toString() + ", " + this.g1.toString() + ", " + this.g2.toString() + ")";
    }

    public Extent getExtent() {
        return this.extent;
    }

    // Functions not implemented - everything is done in render()
    // This resulted in having these functions commented out in SectorGeometryList - which works with the
    // Tessellator class to tessellate the globe
    @Override
    public void beginRendering(DrawContext dc, int numTextureUnits) {

    }

    @Override
    public void endRendering(DrawContext dc) {
    }

    @Override
    public void renderTileID(DrawContext dc) {
    }

    @Override
    public PickedObject[] pick(DrawContext dc, List<? extends Point> pickPoints) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public Intersection[] intersect(Line line) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Intersection[] intersect(double elevation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DoubleBuffer makeTextureCoordinates(SectorGeometry.GeographicTextureCoordinateComputer computer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void renderMultiTexture(DrawContext dc, int numTextureUnits, boolean beginRenderingCalled) {
    }

    @Override
    public void render(DrawContext dc, boolean beginRenderingCalled) {
    }

    @Override
    public void renderMultiTexture(DrawContext dc, int numTextureUnits) {
        // TODO: Validate args
        this.render(dc, this.density, numTextureUnits);
    }

    @Override
    public void render(DrawContext dc) {
        // TODO: Validate args
        this.render(dc, this.density, 2);
    }

    // Renders the globe
    private long render(DrawContext dc, int density, int numTextureUnits) {
        dc.getView().pushReferenceCenter(dc, this.pCentroid);

        GL2 gl = dc.getGL().getGL2();
        gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, ri.vertices.rewind());

        for (int i = 0; i < numTextureUnits; i++) {
            gl.glClientActiveTexture(GL2.GL_TEXTURE0 + i);
            gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL2.GL_FLOAT, 0, this.ri.textureCoords.rewind());
        }

        // This is what actually draws the globe on the screen - using the indices
        gl.glDrawElements(GL2.GL_TRIANGLE_STRIP, ri.indices.limit(), GL2.GL_UNSIGNED_INT, ri.indices.rewind());

        gl.glPopClientAttrib();

        dc.getView().popReferenceCenter(dc);

        return ri.indices.limit() - 2; // return of triangles rendered
    }

    public void renderWireframe(DrawContext dc, boolean showTriangles, boolean showTileBoundary) {
        // RenderInfo ri = this.makeVerts(dc, this.density);
        java.nio.IntBuffer indices = getIndices(ri.density);
        indices.rewind();

        dc.getView().pushReferenceCenter(dc, this.pCentroid);

        GL2 gl = dc.getGL().getGL2();
        // TODO: Could be overdoing the attrib push here. Check that all needed and perhaps save/retore instead.
        gl.glPushAttrib(
                GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_POLYGON_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glColor4d(1d, 1d, 1d, 0.2);
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);

        if (showTriangles) {
            gl.glPushClientAttrib(GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

            gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, ri.vertices);
            gl.glDrawElements(GL2.GL_TRIANGLE_STRIP, indices.limit(), GL2.GL_UNSIGNED_INT, indices);

            gl.glPopClientAttrib();
        }

        dc.getView().popReferenceCenter(dc);

        if (showTileBoundary) {
            this.renderPatchBoundary(gl);
        }

        gl.glPopAttrib();
    }

    private void renderPatchBoundary(GL2 gl) {
        gl.glColor4d(1d, 0, 0, 1d);
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glVertex3d(this.p0.x, this.p0.y, this.p0.z);
        gl.glVertex3d(this.p1.x, this.p1.y, this.p1.z);
        gl.glVertex3d(this.p2.x, this.p2.y, this.p2.z);
        gl.glEnd();
    }

    public void renderBoundingVolume(DrawContext dc, IcosaTile tile) {
        Extent extent = tile.getExtent();
        if (extent == null) {
            return;
        }

        if (extent instanceof Renderable) {
            ((Renderable) extent).render(dc);
        }
    }

    public void renderBoundary(DrawContext dc) {
        this.renderWireframe(dc, false, true);
    }

    public Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset) {
        // TODO: Replace below with interpolation over containing triangle.
        return this.globeInfo.globe.computePointFromPosition(latitude, longitude, metersOffset);
    }

    @Override
    public void pick(DrawContext dc, Point pickPoint) {

    }

}
