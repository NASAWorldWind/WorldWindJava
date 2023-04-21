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
package gov.nasa.worldwind.formats.gpx;

import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.geom.Position;

/**
 * @author tag
 * @version $Id: GpxTrackPoint.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GpxTrackPoint extends gov.nasa.worldwind.formats.gpx.ElementParser implements TrackPoint
{
    private double latitude;
    private double longitude;
    private double elevation;
    private String time;
    
    public GpxTrackPoint(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        this("trkpt", uri, lname, qname, attributes);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected GpxTrackPoint(String pointType, String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        super(pointType);

        //don't validate uri, lname or qname - they aren't used.

        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.org.xml.sax.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < attributes.getLength(); i++)
        {
            String attrName = attributes.getLocalName(i);
            String attrValue = attributes.getValue(i);
            if (attrName.equalsIgnoreCase("lat"))
            {
                this.latitude = Double.parseDouble(attrValue);
            }
            else if (attrName.equalsIgnoreCase("lon"))
            {
                this.longitude = Double.parseDouble(attrValue);
            }
        }
    }

    @Override
    public void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
        //don't perform validation here - no parameters are actually used
    }

    /**
     * @param uri The element URI.
     * @param lname The element lname.
     * @param qname The element qname.
     * @throws IllegalArgumentException if <code>lname</code> is null
     * @throws org.xml.sax.SAXException if a parsing error occurs.
     */
    @Override
    public void doEndElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
    {
        if (lname == null)
        {
            String msg = Logging.getMessage("nullValue.LNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        // don't validate uri or qname - they aren't used.

        if (lname.equalsIgnoreCase("ele"))
        {
            this.elevation = Double.parseDouble(this.currentCharacters);
        }
        else if (lname.equalsIgnoreCase("time"))
        {
            this.time = this.currentCharacters.trim();
        }
    }

    public double getLatitude()
    {
        return latitude;
    }

    /**
     * @param latitude The new latitude.
     * @throws IllegalArgumentException if <code>latitude</code> is less than -90 or greater than 90
     */
    public void setLatitude(double latitude)
    {
        if (latitude > 90 || latitude < -90)
        {
            String msg = Logging.getMessage("generic.AngleOutOfRange", latitude);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    /**
     * @param longitude The new longitude.
     * @throws IllegalArgumentException if <code>longitude</code> is less than -180 or greater than 180
     */
    public void setLongitude(double longitude)
    {
        if (longitude > 180 || longitude < -180)
        {
            String msg = Logging.getMessage("generic.AngleOutOfRange", longitude);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.longitude = longitude;
    }

    public double getElevation()
    {
        return elevation;
    }

    public void setElevation(double elevation)
    {
        this.elevation = elevation;
    }

    public Position getPosition()
    {
        return Position.fromDegrees(this.latitude, this.longitude, this.elevation);
    }

    public void setPosition(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.latitude = position.getLatitude().getDegrees();
        this.longitude = position.getLongitude().getDegrees();
        this.elevation = position.getElevation();
    }

    public String getTime()
    {
        return time;
    }

    /**
     * @param time The new time.
     * @throws IllegalArgumentException if <code>time</code> is null
     */
    public void setTime(String time)
    {
        if (time == null)
        {
            String msg = Logging.getMessage("nullValue.TimeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.time = time;
    }

    @Override
    public String toString()
    {
        return String.format("(%10.6f\u00B0, %11.6f\u00B0, %10.4g m, %s)", this.latitude, this.longitude,
            this.elevation, this.time);
    }
}
