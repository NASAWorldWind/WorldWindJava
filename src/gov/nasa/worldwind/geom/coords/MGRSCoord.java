/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.geom.coords;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

/**
 * This class holds an immutable MGRS coordinate string along with
 * the corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id: MGRSCoord.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class MGRSCoord
{
    private final String MGRSString;
    private final Angle latitude;
    private final Angle longitude;

    /**
     * Create a WGS84 MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the maximum precision of five digits (one meter).
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(Angle latitude, Angle longitude)
    {
        return fromLatLon(latitude, longitude, null, 5);
    }

    /**
     * Create a WGS84 MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the given precision or number of digits.
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param precision the number of digits used for easting and northing (1 to 5).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(Angle latitude, Angle longitude, int precision)
    {
        return fromLatLon(latitude, longitude, null, precision);
    }

    /**
     * Create a MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the maximum precision of five digits (one meter).
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe the <code>Globe</code>.
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * the <code>globe</code> is null, or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(Angle latitude, Angle longitude, Globe globe)
    {
        return fromLatLon(latitude, longitude, globe, 5);
    }

    /**
     * Create a MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the given precision or number of digits (1 to 5).
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe the <code>Globe</code> - can be null (will use WGS84).
     * @param precision the number of digits used for easting and northing (1 to 5).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(Angle latitude, Angle longitude, Globe globe, int precision)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final MGRSCoordConverter converter = new MGRSCoordConverter(globe);
        long err = converter.convertGeodeticToMGRS(latitude.radians, longitude.radians, precision);

        if (err != MGRSCoordConverter.MGRS_NO_ERROR)
        {
            String message = Logging.getMessage("Coord.MGRSConversionError");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new MGRSCoord(latitude, longitude, converter.getMGRSString());
    }

    /**
     * Create a MGRS coordinate from a standard MGRS coordinate text string.
     * <p>
     * The string will be converted to uppercase and stripped of all spaces before being evaluated.
     * </p>
     * <p>Valid examples:<br>
     * 32TLP5626635418<br>
     * 32 T LP 56266 35418<br>
     * 11S KU 528 111<br>
     * </p>
     * @param MGRSString the MGRS coordinate text string.
     * @param globe the <code>Globe</code> - can be null (will use WGS84).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if the <code>MGRSString</code> is null or empty,
     * the <code>globe</code> is null, or the conversion to geodetic coordinates fails (invalid coordinate string).
     */
    public static MGRSCoord fromString(String MGRSString, Globe globe)
    {
        if (MGRSString == null || MGRSString.length() == 0)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        MGRSString = MGRSString.toUpperCase().replaceAll(" ", "");

        final MGRSCoordConverter converter = new MGRSCoordConverter(globe);
        long err = converter.convertMGRSToGeodetic(MGRSString);

        if (err != MGRSCoordConverter.MGRS_NO_ERROR)
        {
            String message = Logging.getMessage("Coord.MGRSConversionError");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new MGRSCoord(Angle.fromRadians(converter.getLatitude()), Angle.fromRadians(converter.getLongitude()), MGRSString);
    }

    /**
     * Create an arbitrary MGRS coordinate from a pair of latitude-longitude <code>Angle</code>
     * and the corresponding MGRS coordinate string.
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param MGRSString the corresponding MGRS coordinate string.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the MGRSString is null or empty.
     */
    public MGRSCoord(Angle latitude, Angle longitude, String MGRSString)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (MGRSString == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (MGRSString.length() == 0)
        {
            String message = Logging.getMessage("generic.StringIsEmpty");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.MGRSString = MGRSString;
    }

    public Angle getLatitude()
    {
        return this.latitude;
    }

    public Angle getLongitude()
    {
        return this.longitude;
    }

    public String toString()
    {
        return this.MGRSString;
    }

}
