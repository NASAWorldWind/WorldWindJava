/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Shows how to compute a radar volume that considers terrain intersection and how to use the {@link
 * gov.nasa.worldwindx.examples.RadarVolume} shape to display the computed volume.
 *
 * @author tag
 * @version $Id: RadarVolumeExample.java 3233 2015-06-22 17:06:51Z tgaskins $
 */
public class RadarVolumeExample extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected static final boolean CONE_VOLUME = true;

        // Use the HighResolutionTerrain class to get accurate terrain for computing the intersections.
        protected HighResolutionTerrain terrain;
        protected double innerRange = 100;
        protected double outerRange = 30e3;
        protected final int numAz = 25; // number of azimuth samplings
        protected final int numEl = 25; // number of elevation samplings
        protected final Angle minimumElevation = Angle.fromDegrees(0);

        public AppFrame()
        {
            super(true, true, false);

            Position center = Position.fromDegrees(36.8378, -118.8743, 100e2); // radar location
            Angle startAzimuth = Angle.fromDegrees(140);
            Angle endAzimuth = Angle.fromDegrees(270);
            Angle startElevation = Angle.fromDegrees(-50);
            Angle endElevation = Angle.fromDegrees(50);
            Angle coneFieldOfView = Angle.fromDegrees(100);
            Angle coneElevation = Angle.fromDegrees(20);
            Angle coneAzimuth = Angle.fromDegrees(205);

            // Initialize the high-resolution terrain class. Construct it to use 50 meter resolution elevations.
            this.terrain = new HighResolutionTerrain(this.getWwd().getModel().getGlobe(), 50d);

            // Compute a near and far grid of positions that will serve as ray endpoints for computing terrain
            // intersections.
            List<Vec4> vertices;

            if (CONE_VOLUME)
            {
                vertices = this.computeSphereVertices(center, coneFieldOfView, coneAzimuth, coneElevation, innerRange,
                    outerRange, numAz, numEl);
            }
            else
            {
                vertices = this.computeGridVertices(center, startAzimuth, endAzimuth, startElevation,
                    endElevation, innerRange, outerRange, numAz, numEl);
            }

            // Create geographic positions from the computed Cartesian vertices. The terrain intersector works with
            // geographic positions.
            final List<Position> positions = this.makePositions(vertices);
//            this.showPositionsAndRays(positions, null);

            // Intersect the rays defined by the radar center and the computed positions with the terrain. Since
            // this is potentially a long-running operation, perform it in a separate thread.
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    long start = System.currentTimeMillis(); // keep track of how long the intersection operation takes
                    final int[] obstructionFlags = intersectTerrain(positions);
                    long end = System.currentTimeMillis();
                    System.out.println("Intersection calculations took " + (end - start) + " ms");

                    // The computed positions define the radar volume. Set up to show that on the event dispatch thread.
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                showRadarVolume(positions, obstructionFlags, numAz, numEl);
//                                showPositionsAndRays(positions, obstructionFlags);
                                getWwd().redraw();
                            }
                            finally
                            {
                                ((Component) getWwd()).setCursor(Cursor.getDefaultCursor());
                            }
                        }
                    });
                }
            });
            ((Component) this.getWwd()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            thread.start();

            // Show the radar source as a marker.
            MarkerLayer markerLayer = new MarkerLayer();
            markerLayer.setKeepSeparated(false);
            MarkerAttributes markerAttributes = new BasicMarkerAttributes();
            ArrayList<Marker> markers = new ArrayList<Marker>();
            markerLayer.setMarkers(markers);
            markers.add(new BasicMarker(positions.get(0), markerAttributes));
            insertAfterPlacenames(getWwd(), markerLayer);
        }

        List<Vec4> computeGridVertices(Position center, Angle leftAzimuth, Angle rightAzimuth, Angle lowerElevation,
            Angle upperElevation, double innerRange, double outerRange, int numAzimuths, int numElevations)
        {
            // Compute the vertices at the Cartesian origin then transform them to the radar position and
            // orientation. The grid is formed as though we're looking at the back of it -- the side the radar is on.
            // The radar volume shape is expecting this orientation and requires it to make the orientation of the
            // triangle faces consistent with the normal vectors that are parallel to the emanating radar rays.

            List<Vec4> vertices = new ArrayList<Vec4>();
            vertices.add(Vec4.ZERO); // the first vertex is the radar position.

            double dAz = (rightAzimuth.radians - leftAzimuth.radians) / (numAzimuths - 1);
            double dEl = (upperElevation.radians - lowerElevation.radians) / (numElevations - 1);

            // Compute the grid for the inner range.
            for (int iel = 0; iel < numElevations; iel++)
            {
                double elevation = lowerElevation.radians + iel * dEl;

                for (int iaz = 0; iaz < numAzimuths; iaz++)
                {
                    double azimuth = leftAzimuth.radians + iaz * dAz;

                    double x = innerRange * Math.sin(azimuth) * Math.cos(elevation);
                    double y = innerRange * Math.cos(azimuth) * Math.cos(elevation);
                    double z = innerRange * Math.sin(elevation);

                    vertices.add(new Vec4(x, y, z));
                }
            }

            // Compute the grid for the outer range.
            for (int iel = 0; iel < numElevations; iel++)
            {
                double elevation = lowerElevation.radians + iel * dEl;

                for (int iaz = 0; iaz < numAzimuths; iaz++)
                {
                    double azimuth = leftAzimuth.radians + iaz * dAz;

                    double x = outerRange * Math.sin(azimuth) * Math.cos(elevation);
                    double y = outerRange * Math.cos(azimuth) * Math.cos(elevation);
                    double z = outerRange * Math.sin(elevation);

                    vertices.add(new Vec4(x, y, z));
                }
            }

            // The vertices are computed relative to the origin. Transform them to the radar position and orientation.
            return this.transformVerticesToPosition(center, vertices);
        }

        List<Vec4> computeConeVertices(Position apex, Angle fov, Angle azimuth, Angle elevation, double innerRange,
            double outerRange, int width, int height)
        {
            List<Vec4> vertices = new ArrayList<Vec4>();
            vertices.add(Vec4.ZERO); // the first vertex is the radar position.

            // Rotate both grids around the Y axis to the specified elevation.
            Matrix elevationMatrix = Matrix.fromAxisAngle(Angle.NEG90.subtract(elevation), 0, 1, 0);

            // Rotate both grids around the Z axis to the specified azimuth.
            Matrix azimuthMatrix = Matrix.fromAxisAngle(Angle.POS90.subtract(azimuth), 0, 0, 1);

            // Combine the rotations and build the full vertex list.
            Matrix combined = azimuthMatrix.multiply(elevationMatrix);

            double x, y;

            double dTheta = 2 * Math.PI / (width - 1);

            // Compute the near grid.
            double innerWidth = innerRange * fov.divide(2).sin(); // half width of chord
            double R = innerRange; // radius of sphere
            double dRadius = innerWidth / (height - 1);

            // Compute rings of vertices to define the grid.
            for (int j = 0; j < height; j++)
            {
                double radius = innerWidth - j * dRadius;

                for (int i = 0; i < width; i++)
                {
                    double theta = i * dTheta;

                    x = radius * Math.cos(theta);
                    y = radius * Math.sin(theta);

                    // Compute Z on the sphere of inner range radius.
                    double w = Math.sqrt(x * x + y * y); // perpendicular distance from centerline to point on sphere
                    double z = Math.sqrt(Math.max(R * R - w * w, 0));

                    Vec4 v = new Vec4(x, y, -z);
                    vertices.add(v.transformBy3(combined));
                }
            }

            // Compute the far grid.
            double outerWidth = outerRange * fov.divide(2).sin();
            R = outerRange;
            dRadius = outerWidth / (height - 1);

            for (int j = 0; j < height; j++)
            {
                double radius = outerWidth - j * dRadius;

                for (int i = 0; i < width; i++)
                {
                    double theta = i * dTheta;

                    x = radius * Math.cos(theta);
                    y = radius * Math.sin(theta);

                    // Compute Z on the sphere of outer range radius.
                    double w = Math.sqrt(x * x + y * y); // perpendicular distance from centerline to point on sphere
                    double z = Math.sqrt(Math.max(R * R - w * w, 0));

                    Vec4 v = new Vec4(x, y, -z);
                    vertices.add(v.transformBy3(combined));
                }
            }

            // The vertices are computed relative to the origin. Transform them to the radar position and orientation.
            return this.transformVerticesToPosition(apex, vertices);
        }

        List<Vec4> computeSphereVertices(Position apex, Angle fov, Angle azimuth, Angle elevation, double innerRange,
            double outerRange, int width, int height)
        {
            List<Vec4> vertices = new ArrayList<Vec4>();
            vertices.add(Vec4.ZERO); // the first vertex is the radar position.

            // Rotate both grids around the Y axis to the specified elevation.
            Matrix elevationMatrix = Matrix.fromAxisAngle(Angle.NEG90.subtract(elevation), 0, 1, 0);

            // Rotate both grids around the Z axis to the specified azimuth.
            Matrix azimuthMatrix = Matrix.fromAxisAngle(Angle.POS90.subtract(azimuth), 0, 0, 1);

            // Combine the rotations and build the full vertex list.
            Matrix combined = azimuthMatrix.multiply(elevationMatrix);

            double x, y, z;

            double dTheta = 2 * Math.PI / (width - 1);

            // Compute the near grid.
            double phi;
            double dPhi = fov.divide(2).radians / (height - 1);

            for (int j = 0; j < height; j++)
            {
                phi = fov.divide(2).radians - j * dPhi;

                for (int i = 0; i < width; i++)
                {
                    double theta = i * dTheta;

                    x = innerRange * Math.cos(theta) * Math.sin(phi);
                    y = innerRange * Math.sin(theta) * Math.sin(phi);
                    z = innerRange * Math.cos(phi);

                    Vec4 v = new Vec4(x, y, -z);
                    vertices.add(v.transformBy3(combined));
                }
            }

            // Compute the far grid.
            for (int j = 0; j < height; j++)
            {
                phi = fov.divide(2).radians - j * dPhi;

                for (int i = 0; i < width; i++)
                {
                    double theta = i * dTheta;

                    x = outerRange * Math.cos(theta) * Math.sin(phi);
                    y = outerRange * Math.sin(theta) * Math.sin(phi);
                    z = outerRange * Math.cos(phi);

                    Vec4 v = new Vec4(x, y, -z);
                    vertices.add(v.transformBy3(combined));
                }
            }

            // The vertices are computed relative to the origin. Transform them to the radar position and orientation.
            return this.transformVerticesToPosition(apex, vertices);
        }

        List<Vec4> transformVerticesToPosition(Position position, List<Vec4> vertices)
        {
            // Transforms the incoming origin-centered vertices to the radar position and orientation.

            List<Vec4> transformedVertices = new ArrayList<Vec4>(vertices.size());

            // Create the transformation matrix that performs the transform.
            Matrix transform = this.getWwd().getModel().getGlobe().computeEllipsoidalOrientationAtPosition(
                position.getLatitude(), position.getLongitude(),
                this.terrain.getElevation(position) + position.getAltitude());

            for (Vec4 vertex : vertices)
            {
                transformedVertices.add(vertex.transformBy4(transform));
            }

            return transformedVertices;
        }

        int[] intersectTerrain(List<Position> positions)
        {
            int[] obstructionFlags = new int[positions.size() - 1];

            int gridSize = (positions.size() - 1) / 2;
            Globe globe = this.terrain.getGlobe();

            // Perform the intersection tests with the terrain and keep track of which rays intersect.

            Position origin = positions.get(0); // this is the radar position
            Vec4 originPoint = globe.computeEllipsoidalPointFromPosition(origin);

            List<Integer> intersectionIndices = new ArrayList<Integer>();

            for (int i = 1; i < positions.size(); i++)
            {
                Position position = positions.get(i);

                // Mark the position as obstructed if it's below the minimum elevation.
                if (this.isBelowMinimumElevation(position, originPoint))
                {
                    obstructionFlags[i - 1] = RadarVolume.EXTERNAL_OBSTRUCTION;
                    continue;
                }

                // If it's obstructed at the near grid it's obstructed at the far grid.
                if (i > gridSize && obstructionFlags[i - 1 - gridSize] == RadarVolume.EXTERNAL_OBSTRUCTION)
                {
                    obstructionFlags[i - 1] = RadarVolume.EXTERNAL_OBSTRUCTION;
                    continue;
                }

                // Compute the intersection with the terrain of a ray to this position.
                //
                // No need to perform the intersection test if the ray to the position just below this one is
                // unobstructed because no obstruction will occur above an unobstructed position. Cannot perform this
                // optimization on cone volumes because their orientation varies around the cone.
                if (!CONE_VOLUME // can't perform this optimization on cones because their orientation is not constant
                    && ((i > this.numAz && i <= gridSize) // near grid above the first row of elevations
                    || (i > gridSize + this.numAz))) // far grid above the first row of elevations
                {
                    if (obstructionFlags[i - 1 - numAz] == RadarVolume.NO_OBSTRUCTION)
                    {
                        obstructionFlags[i - 1] = RadarVolume.NO_OBSTRUCTION;
                        continue;
                    }
                }

                // Perform the intersection test.
                Intersection[] intersections = this.terrain.intersect(origin, position, WorldWind.ABSOLUTE);

                if (intersections == null || intersections.length == 0)
                {
                    // No intersection so use the grid position.
                    obstructionFlags[i - 1] = RadarVolume.NO_OBSTRUCTION;
                }
                else
                {
                    // An intersection with the terrain occurred. If it's a far grid position and beyond the near grid,
                    // set the grid position to be the intersection position.

                    // First see if the intersection is beyond the far grid, in which case the ray is considered
                    // unobstructed.
                    Vec4 intersectionPoint = intersections[0].getIntersectionPoint();
                    double distance = intersectionPoint.distanceTo3(originPoint);
                    if (distance > this.outerRange)
                    {
                        // No intersection so use the grid position.
                        obstructionFlags[i - 1] = RadarVolume.NO_OBSTRUCTION;
                        continue;
                    }

                    if (i > gridSize) // if this is a far grid position
                    {
                        // The obstruction occurs beyond the near grid.
                        obstructionFlags[i - 1] = RadarVolume.INTERNAL_OBSTRUCTION;
                        Position pos = globe.computePositionFromEllipsoidalPoint(intersectionPoint);
                        double elevation = this.terrain.getElevation(pos);
                        positions.set(i, new Position(pos, elevation));
                        intersectionIndices.add(i);
                    }
                    else // it's a near grid position
                    {
                        if (distance < this.innerRange)
                            obstructionFlags[i - 1] = RadarVolume.EXTERNAL_OBSTRUCTION;
                        else
                            obstructionFlags[i - 1] = RadarVolume.NO_OBSTRUCTION;
                    }
                }
            }

            // Raise the internal intersection positions to the next elevation level above their original one. This
            // provides more clearance between the volume and the terrain.
            for (Integer i : intersectionIndices)
            {
                if (i < positions.size() - numAz)
                {
                    Position position = positions.get(i);
                    Position upper = positions.get(i + this.numAz);
                    Vec4 positionVec = globe.computeEllipsoidalPointFromPosition(position).subtract3(originPoint);
                    Vec4 upperVec = globe.computeEllipsoidalPointFromPosition(upper).subtract3(originPoint);
                    upperVec = upperVec.add3(positionVec).divide3(2);
                    double t = positionVec.getLength3() / upperVec.getLength3();
                    Vec4 newPoint = upperVec.multiply3(t).add3(originPoint);
                    Position newPosition = globe.computePositionFromEllipsoidalPoint(newPoint);
                    positions.set(i, newPosition);
                }
            }

            return obstructionFlags;
        }

        protected boolean isBelowMinimumElevation(Position position, Vec4 cartesianOrigin)
        {
            Globe globe = this.getWwd().getModel().getGlobe();

            Vec4 cartesianPosition = globe.computeEllipsoidalPointFromPosition(position);
            Angle angle = cartesianOrigin.angleBetween3(cartesianPosition.subtract3(cartesianOrigin));

            return angle.radians > (Math.PI / 2 - this.minimumElevation.radians);
        }

        List<Position> makePositions(List<Vec4> vertices)
        {
            // Convert the Cartesian vertices to geographic positions.

            List<Position> positions = new ArrayList<Position>(vertices.size());

            Globe globe = this.getWwd().getModel().getGlobe();

            for (Vec4 vertex : vertices)
            {
                positions.add(globe.computePositionFromEllipsoidalPoint(vertex));
            }

            return positions;
        }

        void showRadarVolume(List<Position> positions, int[] obstructionFlags, int numAz, int numEl)
        {
            RenderableLayer layer = new RenderableLayer();

            // Set the volume's attributes.
            ShapeAttributes attributes = new BasicShapeAttributes();
            attributes.setDrawInterior(true);
            attributes.setInteriorMaterial(Material.WHITE);
            attributes.setEnableLighting(true);
//            attributes.setInteriorOpacity(0.8);

            // Create the volume and add it to the model.
            RadarVolume volume = new RadarVolume(positions.subList(1, positions.size()), obstructionFlags, numAz,
                numEl);
            volume.setAttributes(attributes);
            volume.setEnableSides(!CONE_VOLUME);
            layer.addRenderable(volume);

            // Create two paths to show their interaction with the radar volume. The first path goes through most
            // of the volume. The second path goes mostly under the volume.

            Path path = new Path(Position.fromDegrees(36.9843, -119.4464, 20e3),
                Position.fromDegrees(36.4630, -118.3595, 20e3));
            ShapeAttributes pathAttributes = new BasicShapeAttributes();
            pathAttributes.setOutlineMaterial(Material.RED);
            path.setAttributes(pathAttributes);
            layer.addRenderable(path);

            path = new Path(Position.fromDegrees(36.9843, -119.4464, 5e3),
                Position.fromDegrees(36.4630, -118.3595, 5e3));
            path.setAttributes(pathAttributes);
            layer.addRenderable(path);

            insertAfterPlacenames(getWwd(), layer);
        }

        void showPositionsAndRays(List<Position> positions, int[] obstructionFlags)
        {
            MarkerLayer markerLayer = new MarkerLayer();
            markerLayer.setKeepSeparated(false);
            MarkerAttributes attributes = new BasicMarkerAttributes();
            ArrayList<Marker> markers = new ArrayList<Marker>();

            RenderableLayer lineLayer = new RenderableLayer();
            ShapeAttributes lineAttributes = new BasicShapeAttributes();
            lineAttributes.setOutlineMaterial(Material.RED);

            for (Position position : positions)
            {
                {
                    Marker marker = new BasicMarker(position, attributes);
                    markers.add(marker);
                }
            }
            markerLayer.setMarkers(markers);

            int gridSize = positions.size() / 2;
            for (int i = 1; i < gridSize; i++)
            {
                Path path = new Path(positions.get(0), positions.get(i + gridSize));
                path.setAttributes(lineAttributes);
                path.setAltitudeMode(WorldWind.ABSOLUTE);
                if (obstructionFlags != null)
                {
                    int obstructionFlag = obstructionFlags[i + gridSize - 1];
                    String msg = obstructionFlag == RadarVolume.NO_OBSTRUCTION ? "None"
                        : obstructionFlag == RadarVolume.EXTERNAL_OBSTRUCTION ? "External"
                            : obstructionFlag == RadarVolume.INTERNAL_OBSTRUCTION ? "Internal" : "UNKNOWN";
                    path.setValue(AVKey.DISPLAY_NAME, msg);
                }
                lineLayer.addRenderable(path);
            }

            insertAfterPlacenames(getWwd(), markerLayer);
            insertAfterPlacenames(getWwd(), lineLayer);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 36.8378);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -118.8743);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 2000e3);
        ApplicationTemplate.start("Terrain Shadow Prototype", AppFrame.class);
    }
}
