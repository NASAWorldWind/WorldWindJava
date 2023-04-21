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

import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: GpxTrackSegment.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GpxTrackSegment extends gov.nasa.worldwind.formats.gpx.ElementParser
    implements TrackSegment
{
    private java.util.List<TrackPoint> points =
        new java.util.ArrayList<TrackPoint>();

    public GpxTrackSegment(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        super("trkseg");

        // dont' validate uri, lname, qname or attributes as they aren't used.
    }

    public java.util.List<TrackPoint> getPoints()
    {
        return this.points;
    }

    /**
     * @param uri The element URI.
     * @param lname the element lname.
     * @param qname the element qname.
     * @param attributes The element attributes.
     * @throws IllegalArgumentException if any parameter is null
     * @throws org.xml.sax.SAXException if a parsing error occurs.
     */
    @Override
    public void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
        if (lname == null)
        {
            String msg = Logging.getMessage("nullValue.LNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (uri == null)
        {
            String msg = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (qname == null)
        {
            String msg = Logging.getMessage("nullValue.QNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (lname.equalsIgnoreCase("trkpt"))
        {
            this.currentElement = new gov.nasa.worldwind.formats.gpx.GpxTrackPoint(uri, lname, qname, attributes);
            this.points.add((TrackPoint) this.currentElement);
        }
    }
}
