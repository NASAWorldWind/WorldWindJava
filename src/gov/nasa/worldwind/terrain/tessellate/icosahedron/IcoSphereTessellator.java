package gov.nasa.worldwind.terrain.tessellate.icosahedron;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.BasicSessionCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.cache.SessionCache;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.*;
import java.nio.DoubleBuffer;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.util.Logging;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tom Gaskins
 * @version $Id: EllipsoidIcosahedralTessellator.java 5377 2008-05-28 20:28:48Z
 * tgaskins $
 */
// kjdickin - attempt at refactoring to work with CMS - https://git.haldean.org/droidcopter/blob/master/jars/worldwind/obsolete/EllipsoidIcosahedralTessellator.java
// Globe is rendered, but texture coordinates are not being placed in the correct locations
public class IcoSphereTessellator extends WWObjectImpl implements Tessellator {

    // TODO: This class works as of 3/15/07 but it is not complete. There is a problem with texture coordinate
    // generation around +-20 degrees latitude, and picking and meridian/parallel lines are not implemented.
    // Also needs skirt creation.
    public static int DEFAULT_DENSITY = 10;
    private static final int DEFAULT_MAX_LEVEL = 14;
    protected SessionCache topLevelTilesCache = new BasicSessionCache(3);
    protected Sector currentCoverage;
    private Globe globe;
    private GlobeInfo globeInfo;
    private java.util.ArrayList<IcosaTile> topLevels;
    private SectorGeometryList currentTiles = new SectorGeometryList();
    private Frustum currentFrustum;
    private int currentLevel;
    private int maxLevel = DEFAULT_MAX_LEVEL;//14; // TODO: Make configurable
    private Sector sector; // union of all tiles selected during call to render()
    private int density = DEFAULT_DENSITY; // TODO: make configurable
    protected long updateFrequency = 2000; // milliseconds
    protected static final HashMap<Integer, FloatBuffer> textureCoords = new HashMap<Integer, FloatBuffer>();

    protected static final String CACHE_NAME = "Terrain";
    protected static final String CACHE_ID = IcoSphereTessellator.class.getName();

    // protected static final HashMap<Integer, FloatBuffer> textureCoords = new HashMap<>();
    protected static final HashMap<Integer, IntBuffer> indexLists = new HashMap<Integer, IntBuffer>();

    // Set the globe, this function is needed to create the tiles for tessellation
    public void setGlobe(Globe globe) {
        if (globe == null) {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.globe = globe;
        this.globeInfo = new GlobeInfo(this.globe);
        // Create the top level tiles
        this.topLevels = makeLevelZeroEquilateralTriangles(this.globeInfo);
    }

    // Angles used to form icosahedral triangles.
    private static Angle P30 = Angle.fromDegrees(30);
    private static Angle N30 = Angle.fromDegrees(-30);
    private static Angle P36 = Angle.fromDegrees(36);
    private static Angle N36 = Angle.fromDegrees(-36);
    private static Angle P72 = Angle.fromDegrees(72);
    private static Angle N72 = Angle.fromDegrees(-72);
    private static Angle P108 = Angle.fromDegrees(108);
    private static Angle N108 = Angle.fromDegrees(-108);
    private static Angle P144 = Angle.fromDegrees(144);
    private static Angle N144 = Angle.fromDegrees(-144);

    // Lat/lon of vertices of icosahedron aligned with lat/lon domain boundaries.
    private static final LatLon[] L0
            = {
                new LatLon(Angle.POS90, N144), // 0
                new LatLon(Angle.POS90, N72), // 1
                new LatLon(Angle.POS90, Angle.ZERO), // 2
                new LatLon(Angle.POS90, P72), // 3
                new LatLon(Angle.POS90, P144), // 4
                new LatLon(P30, Angle.NEG180), // 5
                new LatLon(P30, N144), // 6
                new LatLon(P30, N108), // 7
                new LatLon(P30, N72), // 8
                new LatLon(P30, N36), // 9
                new LatLon(P30, Angle.ZERO), // 10
                new LatLon(P30, P36), // 11
                new LatLon(P30, P72), // 12
                new LatLon(P30, P108), // 13
                new LatLon(P30, P144), // 14
                new LatLon(P30, Angle.POS180), // 15
                new LatLon(N30, N144), // 16
                new LatLon(N30, N108), // 17
                new LatLon(N30, N72), // 18
                new LatLon(N30, N36), // 19
                new LatLon(N30, Angle.ZERO), // 20
                new LatLon(N30, P36), // 21
                new LatLon(N30, P72), // 22
                new LatLon(N30, P108), // 23
                new LatLon(N30, P144), // 24
                new LatLon(N30, Angle.POS180), // 25
                new LatLon(N30, N144), // 26
                new LatLon(Angle.NEG90, N108), // 27
                new LatLon(Angle.NEG90, N36), // 28
                new LatLon(Angle.NEG90, P36), // 29
                new LatLon(Angle.NEG90, P108), // 30
                new LatLon(Angle.NEG90, Angle.POS180), // 31
                new LatLon(P30, Angle.NEG180), // 32
                new LatLon(N30, Angle.NEG180), // 33
                new LatLon(Angle.NEG90, Angle.NEG180) //34
            };

    protected static class TopLevelTiles {

        protected ArrayList<IcosaTile> topLevels;

        public TopLevelTiles(ArrayList<IcosaTile> topLevels) {
            this.topLevels = topLevels;
        }
    }

    public static IcosaTile createTileFromAngles(GlobeInfo globeInfo, int level, LatLon g0, LatLon g1, LatLon g2) {
        return new IcosaTile(globeInfo, level, g0, g1, g2);
    }

    private static java.util.ArrayList<IcosaTile> makeLevelZeroEquilateralTriangles(GlobeInfo globeInfo) {
        java.util.ArrayList<IcosaTile> topLevels = new java.util.ArrayList<IcosaTile>(22);

        // These lines form the level 0 icosahedral triangles. Two of the icosahedral triangles,
        // however, are split to form right triangles whose sides align with the longitude domain
        // limits (-180/180) so that no triangle spans the discontinuity between +180 and -180.
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[5], L0[7], L0[0])); // 1
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[7], L0[9], L0[1])); // 2
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[9], L0[11], L0[2]));    // 3
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[11], L0[13], L0[3]));   // 4
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[13], L0[15], L0[4]));   // 5
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[16], L0[7], L0[5]));    // 6
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[16], L0[18], L0[7]));   // 7
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[18], L0[9], L0[7]));    //8 
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[18], L0[20], L0[9]));   //9
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[20], L0[11], L0[9])); // 10 - triangle centered on 0 lat, 0 lon
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[20], L0[22], L0[11]));  // 11
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[22], L0[13], L0[11]));  // 12
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[22], L0[24], L0[13]));  // 13
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[24], L0[15], L0[13]));  // 14
        //topLevels.add(createTileFromAngles(globeInfo, 0, L0[24], L0[25], L0[15])); // 15 - right triangle on +180 side of seam
        //topLevels.add(createTileFromAngles(globeInfo, 0, L0[33], L0[26], L0[32])); // 16 - right triangle on -180 side of seam
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[15], L0[24], L0[26])); // 15 + 16 as one triangle - L0s: 32 or 15 as top; 25 and 33 as right angle vertices; 24 and 26 as L/R
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[27], L0[18], L0[16]));  // 17
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[28], L0[20], L0[18]));  // 18
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[29], L0[22], L0[20]));  // 19
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[30], L0[24], L0[22]));  // 20
        //topLevels.add(createTileFromAngles(globeInfo, 0, L0[25], L0[24], L0[31])); // 21 - right triangle on +180 side of seam
        //topLevels.add(createTileFromAngles(globeInfo, 0, L0[26], L0[33], L0[34])); // 22 - right triangle on -180 side of seam
        topLevels.add(createTileFromAngles(globeInfo, 0, L0[34], L0[26], L0[24])); // 21 + 22 as one triangle - L0s: 31 or 34 as bottom; 25 and 33 as right angle vertices; 24 and 26 as L/R

        return topLevels;
    }

    public Sector getSector() {
        return this.sector;
    }

    public void beginRendering(DrawContext dc) {
    }

    public void endRendering(DrawContext dc) {
    }

    public SectorGeometryList tessellate(DrawContext dc) {
        this.globe = dc.getGlobe();
        View view = dc.getView();

        if (!WorldWind.getMemoryCacheSet().containsCache(CACHE_ID)) {
            long size = Configuration.getLongValue(AVKey.SECTOR_GEOMETRY_CACHE_SIZE, 10000000L);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName(CACHE_NAME);
            WorldWind.getMemoryCacheSet().addCache(CACHE_ID, cache);
        }

        this.currentTiles.clear();
        this.currentLevel = 0;
        this.sector = null;
        this.currentCoverage = null;

        this.currentFrustum = view.getFrustumInModelCoordinates();

        this.setGlobe(globe);

        for (IcosaTile tile : topLevels) {
            this.selectVisibleTiles(tile, view, dc);
        }

        this.currentTiles.setSector(this.currentCoverage);

        for (SectorGeometry tile : this.currentTiles) {
            this.makeVerts(dc, this.density, (IcosaTile) tile);

        }

        printTopLevels(topLevels);
        printIcosaTileCoordinates(topLevels);
//        printTexCoords();
        printNormals(this.currentTiles, this.currentLevel);

        return this.currentTiles;
    }

    public void makeVerts(DrawContext dc, int density, IcosaTile tile) {
        int resolution = (int) dc.getGlobe().getElevationModel().getBestResolution(this.getSector());
        MemoryCache cache = WorldWind.getMemoryCache(IcoSphereTessellator.class.getName());
        CacheKey key = new CacheKey(tile, resolution, dc.getVerticalExaggeration(), density);
        tile.ri = (RenderInfo) cache.getObject(key);

        if (this.buildVerts(dc, tile)) {
            cache.add(key, tile.ri, tile.byteSize = tile.ri.getSizeInBytes());
        }
    }

    protected static FloatBuffer createTextureCoordinates(IcosaTile tile, double[][] tileVtxLatLons, int density) {
        if (density < 1) {
            density = 1;
        }
        FloatBuffer textureCoords = Buffers.newDirectFloatBuffer(tileVtxLatLons.length * 2);
        double latMin = tile.sector.getMinLatitude().degrees;
        double lonMin = tile.sector.getMinLongitude().degrees;
        double deltaLat = tile.sector.getDeltaLatDegrees();
        double deltaLon = tile.sector.getDeltaLonDegrees();
        int levelCoordCount = 0;
        int nLevelCoords = density + 1;
        for (int i = 0; i < tileVtxLatLons.length; i++) {
            double u = (tileVtxLatLons[i][1] - lonMin) / deltaLon;
            double v = (tileVtxLatLons[i][0] - latMin) / deltaLat;
            textureCoords.put((float) u);
            textureCoords.put((float) v);
//            System.out.print(String.format("%.2f", tileVtxLatLons[i][0]) + "," + String.format("%.2f", tileVtxLatLons[i][1]) + ",");
//            System.out.print(String.format("%.2f", u) + "," + String.format("%.2f", v));
            if (levelCoordCount == nLevelCoords) {
                System.out.println();
                nLevelCoords--;
                levelCoordCount = 0;
            } else {
                levelCoordCount++;
            }
        }
//        System.out.println();
//        System.out.println("**************************");

        return textureCoords;
    }

    public boolean buildVerts(DrawContext dc, IcosaTile tile) {
        if (tile.ri != null) {
            return true;
        }
        // Density is intended to approximate closely the tessellation's number of intervals along a side.
        double[] params = tile.getParameterization(density); // Parameterization is independent of tile location.
        int numVertexCoords = params.length + params.length / 2;

        DoubleBuffer verts = Buffers.newDirectDoubleBuffer(numVertexCoords);
        double[][] tileVtxLatLons = new double[numVertexCoords / 3][2];
        Vec4 pu = tile.unitp1; // unit vectors at triangle vertices at sphere surface
        Vec4 pv = tile.unitp2;
        Vec4 pw = tile.unitp0;
        int i = 0;
        int locationIdx = 0;
        while (verts.hasRemaining()) {
            double paramU = params[i++];
            double paramV = params[i++];
            double w = 1d - paramU - paramV;
            // Compute point on triangle.
            double x = paramU * pu.x + paramV * pv.x + w * pw.x;
            double y = paramU * pu.y + paramV * pv.y + w * pw.y;
            double z = paramU * pu.z + paramV * pv.z + w * pw.z;

            // Compute latitude and longitude of the vector through point on triangle.
            // Do this before applying ellipsoid eccentricity or elevation.
            double lat = Math.atan2(y, Math.sqrt(x * x + z * z));
            double lon = Math.atan2(x, z);
            tileVtxLatLons[locationIdx][0] = Angle.fromRadians(lat).degrees;
            tileVtxLatLons[locationIdx][1] = Angle.fromRadians(lon).degrees;
            locationIdx++;
            double f = 1d / Math.sqrt(
                    x * x * this.globeInfo.invAsq + y * y * this.globeInfo.invCsq + z * z * this.globeInfo.invAsq);
            x *= f;
            y *= f;
            z *= f;

            // Scale the point so that it lies at the given elevation.
            double elevation = dc.getGlobe().getElevation(Angle.fromRadians(lat), Angle.fromRadians(lon));
            // Scale the point so that it lies at the given elevation.

            double nx = 2 * x * this.globeInfo.invAsq;
            double ny = 2 * y * this.globeInfo.invCsq;
            double nz = 2 * z * this.globeInfo.invAsq;
            double scale = elevation * dc.getVerticalExaggeration() / Math.sqrt(nx * nx + ny * ny + nz * nz);
            nx *= scale;
            ny *= scale;
            nz *= scale;
            lat = Math.atan2(y, Math.sqrt(x * x + z * z));
            lon = Math.atan2(x, z);
            x += (nx - tile.pCentroid.x);
            y += (ny - tile.pCentroid.y);
            z += (nz - tile.pCentroid.z);

            // Store point and position
            verts.put(x).put(y).put(z);
            // TODO: store normal as well
            tile.normal.add3(x, y, z);

        }
        verts.rewind();
        FloatBuffer textureCoords = IcoSphereTessellator.createTextureCoordinates(tile, tileVtxLatLons, density);
        tile.ri = new RenderInfo(this, tile, density, verts, globe, currentTiles, currentLevel, textureCoords);
        return true;
    }

    private boolean needToSplit(IcosaTile tile, View view) {
        double d1 = view.getEyePoint().distanceTo3(tile.p0);
        double d2 = view.getEyePoint().distanceTo3(tile.p1);
        double d3 = view.getEyePoint().distanceTo3(tile.p2);
        double d4 = view.getEyePoint().distanceTo3(tile.pCentroid);

        double minDistance = d1;
        if (d2 < minDistance) {
            minDistance = d2;
        }
        if (d3 < minDistance) {
            minDistance = d3;
        }
        if (d4 < minDistance) {
            minDistance = d4;
        }

        // Meets criteria when the texel size is less than the size of some number of pixels.
        double pixelSize = view.computePixelSizeAtDistance(minDistance);
        return tile.edgeLength / this.density >= 30d * (2d * pixelSize); // 2x pixel size to compensate for view bug
    }

    public boolean isMakeTileSkirts() {
        return false;
    }

    public void setMakeTileSkirts(boolean makeTileSkirts) {
        return;
    }

    private void selectVisibleTiles(IcosaTile tile, View view, DrawContext dc) {

        if (dc.is2DGlobe() && this.skipTile(dc, tile.getSector())) {
            return;
        }

        Extent extent = tile.getExtent();
        if (extent != null && !extent.intersects(this.currentFrustum)) {
            return;
        }

        if (this.currentLevel < this.maxLevel - 1 && this.needToSplit(tile, view)) {
            ++this.currentLevel;
            IcosaTile[] subtiles = tile.split();
            for (IcosaTile child : subtiles) {
                this.selectVisibleTiles(child, view, dc);
            }
            --this.currentLevel;
            return;
        }
        this.currentCoverage = tile.getSector().union(this.currentCoverage);
        this.currentTiles.add(tile);
    }

    protected boolean skipTile(DrawContext dc, Sector sector) {
        Sector limits = ((Globe2D) dc.getGlobe()).getProjection().getProjectionLimits();
        if (limits == null || limits.equals(Sector.FULL_SPHERE)) {
            return false;
        }

        return !sector.intersectsInterior(limits);
    }

    protected static void createIndices(int density) {
        int indexCount = density * density + 4 * density - 2;
        IntBuffer buffer = Buffers.newDirectIntBuffer(indexCount);
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

        indexLists.put(density, buffer);
    }

    private void printTopLevels(ArrayList<IcosaTile> tops) {
        if (tops.size() < 1) {
            return;
        }

        File fileToSave = new File("icosatile_output");
        try ( PrintWriter writer = new PrintWriter(fileToSave)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tops.size(); i++) {
                sb.append(i).append(":");
                sb.append("\n");
                sb.append("Lat min: ").append(tops.get(i).sector.getMinLatitude()).append(" Lat max: ").append(tops.get(i).sector.getMaxLatitude());
                sb.append("\n");
                sb.append("Lon min: ").append(tops.get(i).sector.getMinLongitude()).append(" Lon max: ").append(tops.get(i).sector.getMaxLongitude());
                sb.append("\n");
                sb.append("\n");
            }

            writer.write(sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IcoSphereTessellator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printNormals(SectorGeometryList currentTiles, int lvl) {
        File file = new File("tile_normals.txt");
        try ( PrintWriter writer = new PrintWriter(file)) {
            StringBuilder sb = new StringBuilder();
            int count = 1;
            sb.append("LEVEL: ").append(lvl);

            for (SectorGeometry tile : currentTiles) {
                IcosaTile t = (IcosaTile) tile;
                Vec4 norm = t.normal.normalize3().multiply3(Math.pow(10, 8));
                Vec4 centrd = t.pCentroid.normalize3().multiply3(Math.pow(10, 8));

                sb.append("\n\nTILE ").append(count++); // Tile
                sb.append("\nNormal: <"); // Info
                sb.append((int) norm.x).append(", ").append((int) norm.y).append(", ").append((int) norm.z).append("> W: ").append(norm.w); // Print tile normal
                sb.append("\nCenter to Centroid: <"); // Info
                sb.append((int) centrd.x).append(", ").append((int) centrd.y).append(", ").append((int) centrd.z).append("> W: ").append(norm.w); // Print center to centroid vector
                sb.append("\nDirections Match? "); // Info
                sb.append((((int) norm.x) == ((int) centrd.x)) && (((int) norm.y) == ((int) centrd.y)) && (((int) norm.z) == ((int) centrd.z))); // Print whether directions are the same
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IcoSphereTessellator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Function to print out coordinates triangle tiles
    // Each triangle tile is made up of 3 points with latitude and longitude coordinates
    private void printIcosaTileCoordinates(ArrayList<IcosaTile> topLevels) {
        File fileToSave = new File("icosaTile_coords");
        try ( PrintWriter writer = new PrintWriter(fileToSave)) {
            StringBuilder sb = new StringBuilder();

            writer.write(sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IcoSphereTessellator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public long getUpdateFrequency() {
        return this.updateFrequency;
    }

    public void setUpdateFrequency(long updateFrequency) {
        this.updateFrequency = updateFrequency;
    }
}
