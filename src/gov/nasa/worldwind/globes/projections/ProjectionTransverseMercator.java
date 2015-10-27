/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.TMCoord;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.*;

/**
 * Provides a Transverse Mercator ellipsoidal projection using the WGS84 ellipsoid. The projection's central meridian
 * may be specified and defaults to the Prime Meridian (0 longitude). By default, the projection computes values for 30
 * degrees either side of the central meridian. This may be changed via the {@link
 * #setWidth(gov.nasa.worldwind.geom.Angle)} method, but the projection may fail for large widths.
 * <p/>
 * The projection limits are modified to reflect the central meridian and the width, however the projection limits are
 * clamped to a minimum of -180 degrees and a maximum of +180 degrees. It's therefore not possible to display a band
 * whose central meridian is plus or minus 180.
 *
 * @author tag
 * @version $Id: ProjectionTransverseMercator.java 2393 2014-10-20 20:21:55Z tgaskins $
 */
public class ProjectionTransverseMercator extends AbstractGeographicProjection
{
    protected static Angle DEFAULT_WIDTH = Angle.fromDegrees(30);
    protected static Angle DEFAULT_CENTRAL_MERIDIAN = Angle.ZERO;
    protected static Angle DEFAULT_CENTRAL_LATITUDE = Angle.ZERO;

    protected Angle width = DEFAULT_WIDTH;
    protected Angle centralMeridian = DEFAULT_CENTRAL_MERIDIAN;
    protected Angle centralLatitude = DEFAULT_CENTRAL_LATITUDE;

    /** Creates a projection whose central meridian is the Prime Meridian and central latitude is 0. */
    public ProjectionTransverseMercator()
    {
        super(makeProjectionLimits(DEFAULT_CENTRAL_MERIDIAN, DEFAULT_WIDTH));
    }

    /**
     * Creates a projection with a specified central meridian and a central latitude of 0.
     *
     * @param centralMeridian The projection's central meridian.
     */
    public ProjectionTransverseMercator(Angle centralMeridian)
    {
        super(makeProjectionLimits(centralMeridian, DEFAULT_WIDTH));

        if (centralMeridian == null)
        {
            String message = Logging.getMessage("nullValue.CentralMeridianIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centralMeridian = centralMeridian;
    }

    /**
     * Creates a projection with a specified central meridian and central latitude.
     *
     * @param centralMeridian The projection's central meridian.
     * @param centralLatitude The projection's central latitude.
     */
    public ProjectionTransverseMercator(Angle centralMeridian, Angle centralLatitude)
    {
        super(makeProjectionLimits(centralMeridian, DEFAULT_WIDTH));

        if (centralMeridian == null)
        {
            String message = Logging.getMessage("nullValue.CentralMeridianIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (centralLatitude == null)
        {
            String message = Logging.getMessage("nullValue.CentralLatitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centralMeridian = centralMeridian;
        this.centralLatitude = centralLatitude;
    }

    @Override
    public String getName()
    {
        return "Transverse Mercator";
    }

    /**
     * Indicates this projection's central meridian.
     *
     * @return This projection's central meridian.
     */
    public Angle getCentralMeridian()
    {
        return centralMeridian;
    }

    /**
     * Specifies this projections central meridian.
     *
     * @param centralMeridian This projection's central meridian. The default is 0.
     */
    public void setCentralMeridian(Angle centralMeridian)
    {
        if (centralMeridian == null)
        {
            String message = Logging.getMessage("nullValue.CentralMeridianIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centralMeridian = centralMeridian;
        this.setProjectionLimits(makeProjectionLimits(this.getCentralMeridian(), this.getWidth()));
    }

    /**
     * Indicates this projection's central latitude.
     *
     * @return This projection's central latitude.
     */
    public Angle getCentralLatitude()
    {
        return centralLatitude;
    }

    /**
     * Set this projection's central latitude.
     *
     * @param centralLatitude This projection's central latitude. The default is 0.
     */
    public void setCentralLatitude(Angle centralLatitude)
    {
        if (centralLatitude == null)
        {
            String message = Logging.getMessage("nullValue.CentralLatitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.centralLatitude = centralLatitude;
    }

    /**
     * Indicates the region in which positions are mapped. The default is 30 degrees either side of this projection's
     * central meridian.
     *
     * @return This projection's width.
     */
    public Angle getWidth()
    {
        return width;
    }

    /**
     * Specifies the region in which positions are mapped. The default is 30 degrees either side of this projection's
     * central meridian.
     *
     * @param width This projection's width.
     */
    public void setWidth(Angle width)
    {
        if (width == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.width = width;
        this.setProjectionLimits(makeProjectionLimits(this.getCentralMeridian(), this.getWidth()));
    }

    protected static Sector makeProjectionLimits(Angle centralMeridian, Angle width)
    {
        double minLon = centralMeridian.degrees - width.degrees;
        if (minLon < -180)
            minLon = -180;

        double maxLon = centralMeridian.degrees + width.degrees;
        if (maxLon > 180)
            maxLon = 180;

        return Sector.fromDegrees(-90, 90, minLon, maxLon);
    }

    protected double getScale()
    {
        return 1.0;
    }

    @Override
    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
    {
        if (latitude.degrees > 86)
            latitude = Angle.fromDegrees(86);
        else if (latitude.degrees < -82)
            latitude = Angle.fromDegrees(-82);

        if (longitude.degrees > this.centralMeridian.degrees + this.width.degrees)
            longitude = Angle.fromDegrees(this.centralMeridian.degrees + this.width.degrees);
        else if (longitude.degrees < this.centralMeridian.degrees - this.width.degrees)
            longitude = Angle.fromDegrees(this.centralMeridian.degrees - this.width.degrees);

        TMCoord tm = TMCoord.fromLatLon(latitude, longitude,
            globe, null, null, this.centralLatitude, this.centralMeridian, 0, 0, this.getScale());

        return new Vec4(tm.getEasting(), tm.getNorthing(), metersElevation);
    }

    @Override
    public void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out)
    {
        double minLat = sector.getMinLatitude().radians;
        double maxLat = sector.getMaxLatitude().radians;
        double minLon = sector.getMinLongitude().radians;
        double maxLon = sector.getMaxLongitude().radians;
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);
        double minLatLimit = -82 * Math.PI / 180;
        double maxLatLimit = 86 * Math.PI / 180;
        double minLonLimit = this.centralMeridian.radians - this.width.radians;
        double maxLonLimit = this.centralMeridian.radians + this.width.radians;
        int pos = 0;

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
        // corresponding to each latitude and longitude.
        double lat = minLat;
        for (int j = 0; j < numLat; j++, lat += deltaLat)
        {
            if (j == numLat - 1) // explicitly set the last lat to the max latitude to ensure alignment
                lat = maxLat;
            lat = WWMath.clamp(lat, minLatLimit, maxLatLimit); // limit lat to projection limits

            double lon = minLon;
            for (int i = 0; i < numLon; i++, lon += deltaLon)
            {
                if (i == numLon - 1) // explicitly set the last lon to the max longitude to ensure alignment
                    lon = maxLon;
                lon = WWMath.clamp(lon, minLonLimit, maxLonLimit); // limit lon to projection limits

                TMCoord tm = TMCoord.fromLatLon(Angle.fromRadians(lat), Angle.fromRadians(lon),
                    globe, null, null, this.centralLatitude, this.centralMeridian, 0, 0, this.getScale());
                double x = tm.getEasting();
                double y = tm.getNorthing();
                double z = metersElevation[pos];
                out[pos++] = new Vec4(x, y, z);
            }
        }
    }

    @Override
    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
    {
        TMCoord tm = TMCoord.fromTM(cart.x, cart.y, globe, this.centralLatitude, this.centralMeridian, 0, 0,
            this.getScale());

        return new Position(tm.getLatitude(), tm.getLongitude(), cart.z);
    }
// These are spherical forms from Map Projections -- A Working Manual, but I can't get them to fully work. -- tag 6/25/14
//    @Override
//    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
//    {
//        double B = Math.cos(latitude.radians) * Math.sin(longitude.radians - this.getCentralMeridian().radians);
//        if (B == 1)
//            B = 0.9999; // TODO
//
//        double x = globe.getEquatorialRadius() * this.getScale() * 0.5 * Math.log((1 + B) / (1 - B));
//        double y;
//        if (Math.abs(latitude.degrees) == 90 || Math.abs(longitude.degrees - this.centralMeridian.degrees) == 90)
//            y = globe.getEquatorialRadius() * this.getScale() * (Math.signum(latitude.degrees) * Math.PI/2);
//        else
//        {
//            double s = Math.tan(latitude.radians) / Math.cos(longitude.radians - this.getCentralMeridian().radians);
//            y = globe.getEquatorialRadius() * this.getScale() * Math.atan(s);
//        }
//
//        return new Vec4(x + offset.x, y, metersElevation);
//    }
//
//    @Override
//    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
//    {
//        double rk0 = globe.getEquatorialRadius() * this.getScale();
//        double D = cart.y / rk0;
//
//        double lat = Math.asin(
//            Math.sin(D) / Math.cosh((cart.x - offset.x) / rk0));
//        double lon = Math.atan(Math.sinh((cart.x() - offset.x) / rk0) / Math.cos(D));
//
//        return Position.fromRadians(lat, lon + this.getCentralMeridian().radians, cart.z);
//    }

    @Override
    public Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude)
    {
        // Choose a small angle that we'll use as an increment in order to estimate the north pointing tangent by
        // computing the vector resulting from a small increment in latitude. Using 1e-7 in radians gives a tangent
        // resolution of approximately 1/2 meter. We specify the value in radians since geodeticToCartesian performs
        // arithmetic using angles in radians.
        Angle deltaLat = Angle.fromRadians(1.0e-7);

        if (latitude.degrees + deltaLat.degrees >= 86) // compute the incremental vector below the location
        {
            Vec4 p1 = this.geographicToCartesian(globe, latitude, longitude, 0, null);
            Vec4 p2 = this.geographicToCartesian(globe, latitude.subtract(deltaLat), longitude, 0, null);
            return p1.subtract3(p2).normalize3();
        }
        else if (latitude.degrees - deltaLat.degrees <= -82) // compute the incremental vector above the location
        {
            Vec4 p1 = this.geographicToCartesian(globe, latitude.add(deltaLat), longitude, 0, null);
            Vec4 p2 = this.geographicToCartesian(globe, latitude, longitude, 0, null);
            return p1.subtract3(p2).normalize3();
        }
        else // compute the average of the incremental vector above and below the location
        {
            Vec4 p1 = this.geographicToCartesian(globe, latitude.add(deltaLat), longitude, 0, null);
            Vec4 p2 = this.geographicToCartesian(globe, latitude.subtract(deltaLat), longitude, 0, null);
            return p1.subtract3(p2).normalize3();
        }
    }

    @Override
    public boolean isContinuous()
    {
        return false;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ProjectionTransverseMercator that = (ProjectionTransverseMercator) o;

        if (!centralMeridian.equals(that.centralMeridian))
            return false;
        if (!centralLatitude.equals(that.centralLatitude))
            return false;
        if (!width.equals(that.width))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = width.hashCode();
        result = 31 * result + centralMeridian.hashCode();
        result = 31 * result + centralLatitude.hashCode();
        return result;
    }
}
