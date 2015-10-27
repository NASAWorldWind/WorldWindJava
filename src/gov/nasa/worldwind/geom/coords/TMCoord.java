/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

/**
 * This class holds a set of Transverse Mercator coordinates along with the
 * corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id: TMCoord.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TMCoordConverter
 */
public class TMCoord
{
    private final Angle latitude;
    private final Angle longitude;
    private final Angle originLatitude;
    private final Angle centralMeridian;
    private final double falseEasting;
    private final double falseNorthing;
    private final double scale;
    private final double easting;
    private final double northing;

    /**
     * Create a set of Transverse Mercator coordinates from a pair of latitude and longitude,
     * for the given <code>Globe</code> and projection parameters.
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe the <code>Globe</code> - can be null (will use WGS84).
     * @param a semi-major ellipsoid radius. If this and argument f are non-null and globe is null, will use the specfied a and f.
     * @param f ellipsoid flattening. If this and argument a are non-null and globe is null, will use the specfied a and f.
     * @param originLatitude the origin latitude <code>Angle</code>.
     * @param centralMeridian the central meridian longitude <code>Angle</code>.
     * @param falseEasting easting value at the center of the projection in meters.
     * @param falseNorthing northing value at the center of the projection in meters.
     * @param scale scaling factor.
     * @return the corresponding <code>TMCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to TM coordinates fails. If the globe is null conversion will default
     * to using WGS84.
     */
    public static TMCoord fromLatLon(Angle latitude, Angle longitude, Globe globe, Double a, Double f,
                   Angle originLatitude, Angle centralMeridian,
                   double falseEasting, double falseNorthing,
                   double scale)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (originLatitude == null || centralMeridian == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final TMCoordConverter converter = new TMCoordConverter();
        if (globe != null)
        {
            a = globe.getEquatorialRadius();
            f = (globe.getEquatorialRadius() - globe.getPolarRadius()) / globe.getEquatorialRadius();
        }
        else if (a == null || f == null)
        {
            a = converter.getA();
            f = converter.getF();
        }
        long err = converter.setTransverseMercatorParameters(a, f, originLatitude.radians, centralMeridian.radians,
                falseEasting, falseNorthing, scale);
        if (err == TMCoordConverter.TRANMERC_NO_ERROR)
            err = converter.convertGeodeticToTransverseMercator(latitude.radians, longitude.radians);

        if (err != TMCoordConverter.TRANMERC_NO_ERROR && err != TMCoordConverter.TRANMERC_LON_WARNING)
        {
            String message = Logging.getMessage("Coord.TMConversionError");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new TMCoord(latitude, longitude, converter.getEasting(), converter.getNorthing(),
                originLatitude, centralMeridian, falseEasting, falseNorthing, scale);
    }

    /**
     * Create a set of Transverse Mercator coordinates for the given <code>Globe</code>,
     * easting, northing and projection parameters.
     *
     * @param easting the easting distance value in meters.
     * @param northing the northing distance value in meters.
     * @param globe the <code>Globe</code> - can be null (will use WGS84).
     * @param originLatitude the origin latitude <code>Angle</code>.
     * @param centralMeridian the central meridian longitude <code>Angle</code>.
     * @param falseEasting easting value at the center of the projection in meters.
     * @param falseNorthing northing value at the center of the projection in meters.
     * @param scale scaling factor.
     * @return the corresponding <code>TMCoord</code>.
     * @throws IllegalArgumentException if <code>originLatitude</code> or <code>centralMeridian</code>
     * is null, or the conversion to geodetic coordinates fails. If the globe is null conversion will default
     * to using WGS84.
     */
    public static TMCoord fromTM(double easting, double northing, Globe globe,
                   Angle originLatitude, Angle centralMeridian,
                   double falseEasting, double falseNorthing,
                   double scale)
    {
        if (originLatitude == null || centralMeridian == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final TMCoordConverter converter = new TMCoordConverter();
        double a, f;
        if (globe != null)
        {
            a = globe.getEquatorialRadius();
            f = (globe.getEquatorialRadius() - globe.getPolarRadius()) / globe.getEquatorialRadius();
        }
        else
        {
            a = converter.getA();
            f = converter.getF();
        }
        long err = converter.setTransverseMercatorParameters(a, f, originLatitude.radians, centralMeridian.radians,
                falseEasting, falseNorthing, scale);
        if (err == TMCoordConverter.TRANMERC_NO_ERROR)
            err = converter.convertTransverseMercatorToGeodetic(easting, northing);

        if (err != TMCoordConverter.TRANMERC_NO_ERROR && err != TMCoordConverter.TRANMERC_LON_WARNING)
        {
            String message = Logging.getMessage("Coord.TMConversionError");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new TMCoord(Angle.fromRadians(converter.getLatitude()), Angle.fromRadians(converter.getLongitude()),
                easting, northing, originLatitude, centralMeridian, falseEasting, falseNorthing, scale);
    }

    /**
     * Create an arbitrary set of Transverse Mercator coordinates with the given values.
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param easting the easting distance value in meters.
     * @param northing the northing distance value in meters.
     * @param originLatitude the origin latitude <code>Angle</code>.
     * @param centralMeridian the central meridian longitude <code>Angle</code>.
     * @param falseEasting easting value at the center of the projection in meters.
     * @param falseNorthing northing value at the center of the projection in meters.
     * @param scale scaling factor.
     * @throws IllegalArgumentException if <code>latitude</code>, <code>longitude</code>, <code>originLatitude</code>
     * or <code>centralMeridian</code> is null.
     */
    public TMCoord(Angle latitude, Angle longitude, double easting, double northing,
                   Angle originLatitude, Angle centralMeridian,
                   double falseEasting, double falseNorthing,
                   double scale)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (originLatitude == null || centralMeridian == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.easting = easting;
        this.northing = northing;
        this.originLatitude = originLatitude;
        this.centralMeridian = centralMeridian;
        this.falseEasting = falseEasting;
        this.falseNorthing = falseNorthing;
        this.scale = scale;
    }

    public Angle getLatitude()
    {
        return this.latitude;
    }

    public Angle getLongitude()
    {
        return this.longitude;
    }

    public Angle getOriginLatitude()
    {
        return this.originLatitude;
    }

    public Angle getCentralMeridian()
    {
        return this.centralMeridian;
    }

    public double getFalseEasting()
    {
        return this.falseEasting;
    }

    public double getFalseNorthing()
    {
        return this.falseNorthing;
    }

    public double getScale()
    {
        return this.scale;
    }

    public double getEasting()
    {
        return this.easting;
    }

    public double getNorthing()
    {
        return this.northing;
    }

}
