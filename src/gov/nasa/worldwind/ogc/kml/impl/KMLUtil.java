/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: KMLUtil.java 1838 2014-02-05 20:48:12Z dcollins $
 */
public class KMLUtil
{
    public static final String KML_PIXELS = "pixels";
    public static final String KML_FRACTION = "fraction";
    public static final String KML_INSET_PIXELS = "insetPixels";

    public static ShapeAttributes assembleLineAttributes(ShapeAttributes attrs, KMLLineStyle style)
    {
        // Assign the attributes defined in the KML Feature element.

        if (style.getWidth() != null)
            attrs.setOutlineWidth(style.getWidth());

        if (style.getColor() != null)
            attrs.setOutlineMaterial(new Material(WWUtil.decodeColorABGR(style.getColor())));

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(attrs.getOutlineMaterial().getDiffuse())));

        return attrs;
    }

    public static ShapeAttributes assembleInteriorAttributes(ShapeAttributes attrs, KMLPolyStyle style)
    {
        // Assign the attributes defined in the KML Feature element.

        if (style.getColor() != null)
        {
            Color color = WWUtil.decodeColorABGR(style.getColor());

            attrs.setInteriorMaterial(new Material(color));
            attrs.setInteriorOpacity((double) color.getAlpha() / 255);
        }

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setInteriorMaterial(new Material(WWUtil.makeRandomColor(attrs.getOutlineMaterial().getDiffuse())));

        return attrs;
    }

    /**
     * Indicate whether a specified sub-style has the "highlight" style-state field.
     *
     * @param subStyle the sub-style to test. May be null, in which case this method returns false.
     *
     * @return true if the sub-style has the "highlight" field, otherwise false.
     */
    public static boolean isHighlightStyleState(KMLAbstractSubStyle subStyle)
    {
        if (subStyle == null)
            return false;

        String styleState = (String) subStyle.getField(KMLConstants.STYLE_STATE);
        return styleState != null && styleState.equals(KMLConstants.HIGHLIGHT);
    }

    public static int convertAltitudeMode(String altMode, int defaultAltMode)
    {
        if ("clampToGround".equals(altMode))
            return WorldWind.CLAMP_TO_GROUND;
        else if ("relativeToGround".equals(altMode))
            return WorldWind.RELATIVE_TO_GROUND;
        else if ("absolute".equals(altMode))
            return WorldWind.ABSOLUTE;
        else
            return defaultAltMode;
    }

    /**
     * Translate a KML units string ("pixels", "insetPixels", or "fraction") into the corresponding WW unit constant
     * ({@link AVKey#PIXELS}, {@link AVKey#INSET_PIXELS}, or {@link AVKey#FRACTION}.
     *
     * @param units KML units to translate.
     *
     * @return WW units, or null if the argument is not a valid KML unit.
     */
    public static String kmlUnitsToWWUnits(String units)
    {
        if (KML_PIXELS.equals(units))
            return AVKey.PIXELS;
        else if (KML_FRACTION.equals(units))
            return AVKey.FRACTION;
        else if (KML_INSET_PIXELS.equals(units))
            return AVKey.INSET_PIXELS;
        else
            return null;
    }

    /**
     * Translate a WorldWind units constant ({@link AVKey#PIXELS}, {@link AVKey#INSET_PIXELS}, or {@link AVKey#FRACTION}
     * to the corresponding KML unit string ("pixels", "insetPixels", or "fraction").
     *
     * @param units WorldWind units to translate.
     *
     * @return KML units, or null if the argument is not a valid WW unit.
     */
    public static String wwUnitsToKMLUnits(String units)
    {
        if (AVKey.PIXELS.equals(units))
            return KML_PIXELS;
        else if (AVKey.FRACTION.equals(units))
            return KML_FRACTION;
        else if (AVKey.INSET_PIXELS.equals(units))
            return KML_INSET_PIXELS;
        else
            return null;
    }

    /**
     * Creates a <code>Sector</code> from a <code>KMLAbstractLatLonBoxType's</code> <code>north</code>,
     * <code>south</code>, <code>east</code>, and <code>west</code> coordinates. This returns <code>null</code> if any
     * of these coordinates are unspecified.
     *
     * @param box a box who's coordinates define a <code>Sector</code>.
     *
     * @return the <code>Sector</code> that bounds the specified <code>box's</code> coordinates.
     *
     * @throws IllegalArgumentException if the <code>box</code> is <code>null</code>.
     */
    public static Sector createSectorFromLatLonBox(KMLAbstractLatLonBoxType box)
    {
        if (box == null)
        {
            String message = Logging.getMessage("nullValue.BoxIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (box.getNorth() == null || box.getSouth() == null || box.getEast() == null || box.getWest() == null)
            return null;

        double north = box.getNorth();
        double south = box.getSouth();
        double east = box.getEast();
        double west = box.getWest();

        double minLat = Math.min(north, south);
        double maxLat = Math.max(north, south);
        double minLon = Math.min(east, west);
        double maxLon = Math.max(east, west);

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    /**
     * Get all of the positions that make up a {@link KMLAbstractGeometry}. If the geometry contains other geometries,
     * this method collects all the points from all of the geometries.
     *
     * @param globe     Globe to use to determine altitude above terrain.
     * @param geometry  Geometry to collect positions from.
     * @param positions Placemark positions will be added to this list.
     */
    public static void getPositions(Globe globe, KMLAbstractGeometry geometry, java.util.List<Position> positions)
    {
        if (geometry instanceof KMLPoint)
        {
            KMLPoint kmlPoint = (KMLPoint) geometry;
            Position pos = kmlPoint.getCoordinates();

            if (pos != null)
                positions.add(computeAltitude(globe, pos, kmlPoint.getAltitudeMode()));
        }
        else if (geometry instanceof KMLModel)
        {
            KMLModel model = (KMLModel) geometry;
            KMLLocation location = model.getLocation();
            if (location != null)
            {
                Position pos = location.getPosition();
                if (pos != null)
                    positions.add(computeAltitude(globe, pos, model.getAltitudeMode()));
            }
        }
        else if (geometry instanceof KMLLineString) // Also handles KMLLinearRing, since KMLLineString is a subclass of KMLLinearRing
        {
            KMLLineString lineString = (KMLLineString) geometry;
            Position.PositionList positionList = lineString.getCoordinates();
            if (positionList != null)
            {
                positions.addAll(computeAltitude(globe, positionList.list, lineString.getAltitudeMode()));
            }
        }
        else if (geometry instanceof KMLPolygon)
        {
            KMLLinearRing ring = ((KMLPolygon) geometry).getOuterBoundary();
            // Recurse and let the LineString/LinearRing code handle the boundary positions
            getPositions(globe, ring, positions);
        }
        else if (geometry instanceof KMLMultiGeometry)
        {
            java.util.List<KMLAbstractGeometry> geoms = ((KMLMultiGeometry) geometry).getGeometries();
            for (KMLAbstractGeometry g : geoms)
            {
                // Recurse, adding positions for the sub-geometry
                getPositions(globe, g, positions);
            }
        }
    }

    /**
     * Compute the altitude of each position in a list, based on altitude mode.
     *
     * @param globe        Globe to use to determine altitude above terrain.
     * @param positions    Positions to compute altitude for.
     * @param altitudeMode A KML altitude mode string.
     *
     * @return A new list of positions with altitudes set based on {@code altitudeMode}.
     */
    public static java.util.List<Position> computeAltitude(Globe globe, java.util.List<? extends Position> positions,
        String altitudeMode)
    {
        java.util.List<Position> outPositions = new ArrayList<Position>(positions.size());
        for (Position p : positions)
        {
            outPositions.add(computeAltitude(globe, p, altitudeMode));
        }

        return outPositions;
    }

    /**
     * Create a {@link Position}, taking into account an altitude mode.
     *
     * @param globe        Globe to use to determine altitude above terrain.
     * @param position     Position to evaluate.
     * @param altitudeMode A KML altitude mode string.
     *
     * @return New Position.
     */
    public static Position computeAltitude(Globe globe, Position position, String altitudeMode)
    {
        double height;
        Angle latitude = position.getLatitude();
        Angle longitude = position.getLongitude();

        int altMode = convertAltitudeMode(altitudeMode, WorldWind.CLAMP_TO_GROUND); // KML default
        if (altMode == WorldWind.CLAMP_TO_GROUND)
            height = globe.getElevation(latitude, longitude);
        else if (altMode == WorldWind.RELATIVE_TO_GROUND)
            height = globe.getElevation(latitude, longitude) + position.getAltitude();
        else
            height = position.getAltitude();

        return new Position(latitude, longitude, height);
    }

    /**
     * Rotate the corners of a sector around a normal vector through the sector centroid.
     *
     * @param globe    Globe to use to compute rotated positions.
     * @param sector   Sector to rotate.
     * @param rotation Rotation angle. Positive angles produce counterclockwise rotation.
     *
     * @return List of rotated corners.
     */
    public static java.util.List<LatLon> rotateSector(Globe globe, Sector sector, Angle rotation)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (rotation == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LatLon[] corners = sector.getCorners();
        java.util.List<LatLon> transformedCorners = new ArrayList<LatLon>(corners.length);

        // Using the four corners of the sector to compute the rotation axis avoids problems with dateline
        // spanning polygons.
        Vec4[] verts = sector.computeCornerPoints(globe, 1);
        Vec4 normalVec = verts[2].subtract3(verts[0]).cross3(verts[3].subtract3(verts[1])).normalize3();
        Matrix rotationMatrix = Matrix.fromAxisAngle(rotation, normalVec);

        Vec4 centerPoint = sector.computeCenterPoint(globe, 1);

        // Rotate each point around the surface normal, and convert back to geographic
        for (Vec4 point : verts)
        {
            point = point.subtract3(centerPoint).transformBy3(rotationMatrix).add3(centerPoint);
            LatLon ll = globe.computePositionFromPoint(point);

            transformedCorners.add(ll);
        }

        return transformedCorners;
    }
}
