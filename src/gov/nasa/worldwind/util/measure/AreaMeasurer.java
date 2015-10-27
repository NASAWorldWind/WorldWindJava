/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.measure;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import java.util.ArrayList;

/**
 * Utility class to compute approximations of projected and surface (terrain following) area on a globe.
 *
 * <p>To properly compute surface area the measurer must be provided with a list of positions that describe a
 * closed path - one which last position is equal to the first.</p>
 *
 * <p>Segments which are longer then the current maxSegmentLength will be subdivided along lines following the current
 * pathType - {@link gov.nasa.worldwind.render.Polyline#LINEAR}, {@link gov.nasa.worldwind.render.Polyline#RHUMB_LINE}
 * or {@link gov.nasa.worldwind.render.Polyline#GREAT_CIRCLE}.</p>
 *
 * <p>Projected or non terrain following area is computed in a sinusoidal projection which is equivalent or equal area.
 * Surface or terrain following area is approximated by sampling the path bounding sector with square cells along a
 * grid. Cells which center is inside the path  have their area estimated and summed according to the overall slope
 * at the cell south-west corner.</p>
 *
 * @author Patrick Murris
 * @version $Id: AreaMeasurer.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see MeasureTool
 * @see LengthMeasurer
 */
public class AreaMeasurer extends LengthMeasurer implements MeasurableArea
{
    private static final double DEFAULT_AREA_SAMPLING_STEPS = 32; // sampling grid max rows or cols

    private ArrayList<? extends Position> subdividedPositions;
    private Cell[][] sectorCells;
    private Double[][] sectorElevations;
    private double areaTerrainSamplingSteps = DEFAULT_AREA_SAMPLING_STEPS;
    protected double surfaceArea = -1;
    protected double projectedArea = -1;

    public AreaMeasurer()
    {
    }

    public AreaMeasurer(ArrayList<? extends Position> positions)
    {
        super(positions);
    }

    protected void clearCachedValues()
    {
        super.clearCachedValues();
        this.subdividedPositions = null;
        this.projectedArea = -1;
        this.surfaceArea = -1;
    }

    public void setPositions(ArrayList<? extends Position> positions)
    {
        Sector oldSector = getBoundingSector();
        super.setPositions(positions); // will call clearCachedData()

        if (getBoundingSector() == null || !getBoundingSector().equals(oldSector))
        {
            this.sectorCells = null;
            this.sectorElevations = null;
        }
    }

    /**
     * Get the sampling grid maximum number of rows or columns for terrain following surface area approximation.
     *
     * @return  the sampling grid maximum number of rows or columns.
     */
    public double getAreaTerrainSamplingSteps()
    {
        return this.areaTerrainSamplingSteps;
    }

    /**
     * Set the sampling grid maximum number of rows or columns for terrain following surface area approximation.
     *
     * @param steps the sampling grid maximum number of rows or columns.
     * @throws IllegalArgumentException if steps is less then one.
     */
    public void setAreaTerrainSamplingSteps(double steps)
    {
        if (steps < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", steps);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.areaTerrainSamplingSteps != steps)
        {
            this.areaTerrainSamplingSteps = steps;
            this.surfaceArea = -1;
            this.projectedArea = -1;
            // Invalidate cached data
            this.sectorCells = null;
            this.sectorElevations = null;
        }
    }

    /**
     * Get the surface area approximation for the current path or shape.
     *
     * <p>If the measurer is set to follow terrain, the computed area will account for terrain deformations. Otherwise
     * the area is that of the path once projected at sea level - elevation zero.</p>
     *
     * @param globe the globe to draw terrain information from.
     * @return the current shape surface area or -1 if the position list does not describe a closed path or is too short.
     * @throws IllegalArgumentException if globe is <code>null</code>.
     */
    public double getArea(Globe globe)
    {
        return this.isFollowTerrain() ? getSurfaceArea(globe) : getProjectedArea(globe);
    }

    public double getSurfaceArea(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.surfaceArea < 0)
            this.surfaceArea = this.computeSurfaceAreaSampling(globe, this.areaTerrainSamplingSteps);

        return this.surfaceArea;
    }

    public double getProjectedArea(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.projectedArea < 0)
            this.projectedArea = this.computeProjectedAreaGeometry(globe);

        return this.projectedArea;
    }

    public double getPerimeter(Globe globe)
    {
        return getLength(globe);
    }

    public double getWidth(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Sector sector = getBoundingSector();
        if (sector != null)
            return globe.getRadiusAt(sector.getCentroid()) * sector.getDeltaLon().radians
                    * Math.cos(sector.getCentroid().getLatitude().radians);

        return -1;
    }

    public double getHeight(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Sector sector = getBoundingSector();
        if (sector != null)
            return globe.getRadiusAt(sector.getCentroid()) * sector.getDeltaLat().radians;

        return -1;
    }

    // *** Computing area ******************************************************************

    protected class Cell
    {
        Sector sector;
        double projectedArea, surfaceArea;

        public Cell(Sector sector, double projected, double surface)
        {
            this.sector = sector;
            this.projectedArea = projected;
            this.surfaceArea = surface;
        }
    }

    // *** Projected area ***

    // Tessellate the path in lat-lon space, then sum each triangle area.
    protected double computeProjectedAreaGeometry(Globe globe)
    {
        Sector sector = getBoundingSector();
        if (sector != null && this.isClosedShape())
        {
            // Subdivide long segments if needed
            if (this.subdividedPositions == null)
                this.subdividedPositions = subdividePositions(globe, getPositions(), getMaxSegmentLength()
                        , isFollowTerrain(), getPathType());
            // First: tessellate polygon
            int verticesCount = this.subdividedPositions.size() - 1; // trim last pos which is same as first
            float[] verts = new float[verticesCount * 3];
            // Prepare vertices
            int idx = 0;
            for (int i = 0; i < verticesCount; i++)
            {
                // Vertices coordinates are x=lon y=lat in radians, z = elevation zero
                verts[idx++] = (float)this.subdividedPositions.get(i).getLongitude().radians;
                verts[idx++] = (float)this.subdividedPositions.get(i).getLatitude().radians;
                verts[idx++] = 0f;
            }
            // Tessellate
            GeometryBuilder gb = new GeometryBuilder();
            GeometryBuilder.IndexedTriangleArray ita = gb.tessellatePolygon2(0, verticesCount, verts);
            // Second: sum triangles area
            double area = 0;
            int[] indices = ita.getIndices();
            int triangleCount = ita.getIndexCount() / 3;
            for (int i = 0; i < triangleCount; i++)
            {
                idx = i * 3;
                area += computeTriangleProjectedArea(globe, ita.getVertices(), indices[idx] * 3
                        , indices[idx + 1] * 3, indices[idx + 2] * 3);
            }
            return area;
        }
        return -1;
    }

    // Compute triangle area in a sinusoidal projection centered at the triangle center.
    // Note sinusoidal projection is equivalent or equal erea.
    protected double computeTriangleProjectedArea(Globe globe, float[] verts, int a, int b, int c)
    {
        // http://www.mathopenref.com/coordtrianglearea.html
        double area = Math.abs(verts[a] * (verts[b + 1] - verts[c + 1])
                + verts[b] * (verts[c + 1] - verts[a + 1])
                + verts[c] * (verts[a + 1] - verts[b + 1])) / 2; // square radians
        // Compute triangle center
        double centerLat = (verts[a + 1] + verts[b + 1] + verts[c + 1]) / 3;
        double centerLon = (verts[a] + verts[b] + verts[c]) / 3;
        // Apply globe radius at triangle center and scale down area according to center latitude cosine
        double radius = globe.getRadiusAt(Angle.fromRadians(centerLat), Angle.fromRadians(centerLon));
        area *= Math.cos(centerLat) * radius * radius; // Square meter

        return area;
    }

    // *** Surface area - terrain following ***

    // Sample the path bounding sector with square cells which area are approximated according to the surface normal at
    // the cell south-west corner.
    protected double computeSurfaceAreaSampling(Globe globe, double steps)
    {
        Sector sector = getBoundingSector();
        if (sector != null && this.isClosedShape())
        {
            // Subdivide long segments if needed
            if (this.subdividedPositions == null)
                this.subdividedPositions = subdividePositions(globe, getPositions(), getMaxSegmentLength(),
                        true, getPathType());

            // Sample the bounding sector with cells about the same length in side - squares
            double stepRadians = Math.max(sector.getDeltaLatRadians() / steps, sector.getDeltaLonRadians() / steps);
            int latSteps = (int)Math.round(sector.getDeltaLatRadians() / stepRadians);
            int lonSteps = (int)Math.round(sector.getDeltaLonRadians() / stepRadians
                    * Math.cos(sector.getCentroid().getLatitude().radians));
            double latStepRadians = sector.getDeltaLatRadians() / latSteps;
            double lonStepRadians = sector.getDeltaLonRadians() / lonSteps;

            if (this.sectorCells == null)
                this.sectorCells = new Cell[latSteps][lonSteps];
            if (this.sectorElevations == null)
                this.sectorElevations = new Double[latSteps + 1][lonSteps + 1];

            double area = 0;
            for (int i = 0; i < latSteps; i++)
            {
                double lat = sector.getMinLatitude().radians + latStepRadians * i;
                // Compute this latitude row cells area
                double radius = globe.getRadiusAt(Angle.fromRadians(lat + latStepRadians / 2),
                        sector.getCentroid().getLongitude());
                double cellWidth = lonStepRadians * radius * Math.cos(lat + latStepRadians / 2);
                double cellHeight = latStepRadians * radius;
                double cellArea = cellWidth * cellHeight;

                for (int j = 0; j < lonSteps; j++)
                {
                    double lon = sector.getMinLongitude().radians + lonStepRadians * j;
                    Sector cellSector = Sector.fromRadians(lat, lat + latStepRadians, lon, lon + lonStepRadians);
                    // Select cells which center is inside the shape
                    if (WWMath.isLocationInside(cellSector.getCentroid(), this.subdividedPositions))
                    {
                        Cell cell = this.sectorCells[i][j];
                        if (cell == null || cell.surfaceArea == -1)
                        {
                            // Compute suface area using terrain normal in SW corner
                            // Corners elevation
                            double eleSW = sectorElevations[i][j] != null ? sectorElevations[i][j]
                                    : globe.getElevation(Angle.fromRadians(lat), Angle.fromRadians(lon));
                            double eleSE = sectorElevations[i][j + 1] != null ? sectorElevations[i][j + 1]
                                    : globe.getElevation(Angle.fromRadians(lat), Angle.fromRadians(lon + lonStepRadians));
                            double eleNW = sectorElevations[i + 1][j] != null ? sectorElevations[i + 1][j]
                                    : globe.getElevation(Angle.fromRadians(lat + latStepRadians), Angle.fromRadians(lon));
                            // Cache elevations
                            sectorElevations[i][j] = eleSW;
                            sectorElevations[i][j + 1] = eleSE;
                            sectorElevations[i + 1][j] = eleNW;
                            // Compute normal
                            Vec4 vx = new Vec4(cellWidth, 0, eleSE - eleSW).normalize3();
                            Vec4 vy = new Vec4(0, cellHeight, eleNW - eleSW).normalize3();
                            Vec4 normalSW = vx.cross3(vy).normalize3(); // point toward positive Z
                            // Compute slope factor
                            double tan = Math.tan(Vec4.UNIT_Z.angleBetween3(normalSW).radians);
                            double slopeFactor = Math.sqrt(1 + tan * tan);
                            // Create and cache cell
                            cell = new Cell(cellSector, cellArea, cellArea * slopeFactor);
                            this.sectorCells[i][j] = cell;
                        }
                        // Add cell area
                        area += cell.surfaceArea;
                    }
                }
            }
            return area;
        }
        return -1;
    }

// Below code is an attempt at computing the surface area using geometry.

//    private static final double DEFAULT_AREA_CONVERGENCE_PERCENT = 2;   // stop sudividing when increase in area
                                                                        // is less then this percent
//    private double areaTerrainConvergencePercent = DEFAULT_AREA_CONVERGENCE_PERCENT;

//    private int triangleCount = 0;
//    // Tessellate the path in lat-lon space, then sum each triangle surface area.
//    protected double computeSurfaceAreaGeometry(Globe globe)
//    {
//        long t0 = System.nanoTime();
//        this.triangleCount = 0;
//        Sector sector = getBoundingSector();
//        if (sector != null && this.isClosedShape())
//        {
//            // Subdivide long segments if needed
//            if (this.subdividedPositions == null)
//                this.subdividedPositions = subdividePositions(globe, getPositions(), getMaxSegmentLength()
//                        , isFollowTerrain(), getPathType());
//            // First: tessellate polygon
//            int verticesCount = this.subdividedPositions.size() - 1; // trim last pos which is same as first
//            float[] verts = new float[verticesCount * 3];
//            // Prepare vertices
//            int idx = 0;
//            for (int i = 0; i < verticesCount; i++)
//            {
//                // Vertices coordinates are x=lon y=lat in radians, z = elevation zero
//                verts[idx++] = (float)this.subdividedPositions.get(i).getLongitude().radians;
//                verts[idx++] = (float)this.subdividedPositions.get(i).getLatitude().radians;
//                verts[idx++] = 0f;
//            }
//            // Tessellate
//            GeometryBuilder gb = new GeometryBuilder();
//            GeometryBuilder.IndexedTriangleArray ita = gb.tessellatePolygon2(0, verticesCount, verts);
//            // Second: sum triangles area
//            double area = 0;
//            int triangleCount = ita.getIndexCount() / 3;
//            for (int i = 0; i < triangleCount; i++)
//            {
//                idx = i * 3;
//                area += computeIndexedTriangleSurfaceArea(globe, ita, idx);
//            }
//            long t1 = System.nanoTime();
//            System.out.println("Surface area geometry: " + area + " - " + (t1 - t0) / 1e3 + " micro sec for " + this.triangleCount + " triangles");
//            return area;
//        }
//        return -1;
//    }
//
//    private double computeIndexedTriangleSurfaceArea(Globe globe, GeometryBuilder.IndexedTriangleArray ita, int idx)
//    {
//        // Create a one triangle indexed array
//        GeometryBuilder gb = new GeometryBuilder();
//        int[] indices = new int[] {0, 1, 2};
//        float[] vertices = new float[9];
//        System.arraycopy(ita.getVertices(), ita.getIndices()[idx] * 3, vertices, 0, 3);
//        System.arraycopy(ita.getVertices(), ita.getIndices()[idx + 1] * 3, vertices, 3, 3);
//        System.arraycopy(ita.getVertices(), ita.getIndices()[idx + 2] * 3, vertices, 6, 3);
//        GeometryBuilder.IndexedTriangleArray triangleIta = new GeometryBuilder.IndexedTriangleArray(3, indices, 3, vertices);
//
//        // Get triangle area
//        double area = computeIndexedTriangleArraySurfaceArea(globe, triangleIta);
//        if (area < 10)
//        {
//            // Do not subdivide below some area
//            this.triangleCount++;
//            return area;
//        }
//
//        // Subdivide and get area again. If increase is larger then some percentage, recurse on each of four triangles
//        gb.subdivideIndexedTriangleArray(triangleIta);
//        double subArea = computeIndexedTriangleArraySurfaceArea(globe, triangleIta);
//        double delta = subArea - area;
//
//        // *** Debug ***
//        System.out.println((delta > 1 && delta > area * this.areaTerrainConvergencePercent / 100 ? "more" : "OK")
//                + " Delta: " + delta + ", area: " + area + ", sub area: " + subArea);
//
//        if (delta > 1 && delta > area * this.areaTerrainConvergencePercent / 100)
//        {
//            // Recurse on four sub triangles
//            subArea = 0;
//            for (int i = 0; i < 4; i++)
//                subArea += computeIndexedTriangleSurfaceArea(globe, triangleIta, i * 3);
//        }
//        else
//            this.triangleCount += 4;
//
//        return subArea;
//    }

//    private double computeIndexedTriangleSurfaceAreaIteration(Globe globe, GeometryBuilder.IndexedTriangleArray ita, int idx)
//    {
//        // Create a one triangle indexed array
//        GeometryBuilder gb = new GeometryBuilder();
//        int[] indices = new int[] {0, 1, 2};
//        float[] vertices = new float[9];
//        System.arraycopy(ita.getVertices(), ita.getIndices()[idx] * 3, vertices, 0, 3);
//        System.arraycopy(ita.getVertices(), ita.getIndices()[idx + 1] * 3, vertices, 3, 3);
//        System.arraycopy(ita.getVertices(), ita.getIndices()[idx + 2] * 3, vertices, 6, 3);
//        GeometryBuilder.IndexedTriangleArray triangleIta = new GeometryBuilder.IndexedTriangleArray(3, indices, 3, vertices);
//
//        // Get triangle area
//        double area = computeIndexedTriangleArraySurfaceArea(globe, triangleIta);
//
//        // Subdivide and get area again until increase is smaller then some percentage
//        double delta = Double.MAX_VALUE;
//        while (delta > (area - delta) * this.areaTerrainConvergencePercent / 100)
//        {
//            gb.subdivideIndexedTriangleArray(triangleIta);
//            double subArea = computeIndexedTriangleArraySurfaceArea(globe, triangleIta);
//            delta = subArea - area;
//            area = subArea;
//        }
//        System.out.println("Triangle " + idx / 3 + " tot triangles: " + triangleIta.getIndexCount() / 3);
//        return area;
//    }

//    private double computeIndexedTriangleArraySurfaceArea(Globe globe, GeometryBuilder.IndexedTriangleArray ita)
//    {
//        int a, b, c;
//        double area = 0;
//        for (int i = 0; i < ita.getIndexCount(); i += 3)
//        {
//            // Sum each triangle area
//            a = ita.getIndices()[i] * 3;
//            b = ita.getIndices()[i + 1] * 3;
//            c = ita.getIndices()[i + 2] * 3;
//            area += computeTriangleSurfaceArea(globe, ita.getVertices(), a, b, c);
//        }
//        return area;
//    }
//
//    protected double computeTriangleSurfaceArea(Globe globe, float[] verts, int a, int b, int c)
//    {
//        // Triangle surface area is half the cross product length of any two edges
//        Vec4 pa = getSurfacePointSinusoidal(globe, verts[a + 1], verts[a]);
//        Vec4 pb = getSurfacePointSinusoidal(globe, verts[b + 1], verts[b]);
//        Vec4 pc = getSurfacePointSinusoidal(globe, verts[c + 1], verts[c]);
//        Vec4 AB = pb.subtract3(pa);
//        Vec4 AC = pc.subtract3(pa);
//        return 0.5 * AB.cross3(AC).getLength3();
//    }

//    protected Vec4 getSurfacePoint(Globe globe, float latRadians, float lonRadians)
//    {
//        Angle latitude = Angle.fromRadians(latRadians);
//        Angle longitude = Angle.fromRadians(lonRadians);
//        return globe.computePointFromPosition(latitude, longitude, globe.getElevation(latitude, longitude));
//    }

//    protected Vec4 getSurfacePointSinusoidal(Globe globe, float latRadians, float lonRadians)
//    {
//        Angle latitude = Angle.fromRadians(latRadians);
//        Angle longitude = Angle.fromRadians(lonRadians);
//        double radius = globe.getRadiusAt(latitude, longitude);
//        return new Vec4(radius * lonRadians * latitude.cos(), radius * latRadians,
//                globe.getElevation(latitude, longitude));
//    }


}
