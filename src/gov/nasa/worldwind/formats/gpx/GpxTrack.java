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
 * @version $Id: GpxTrack.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GpxTrack extends gov.nasa.worldwind.formats.gpx.ElementParser implements Track
{
    private String name;
    private int numPoints = -1;
    private java.util.List<TrackSegment> segments =
        new java.util.ArrayList<TrackSegment>();

    @SuppressWarnings({"UNUSED_SYMBOL", "UnusedDeclaration"})
    public GpxTrack(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        super("trk");
        // don't validate uri, lname, qname or attributes - they aren't used.
    }

    public java.util.List<TrackSegment> getSegments()
    {
        return segments;
    }

    public String getName()
    {
        return name;
    }

    public int getNumPoints()
    {
        if (this.segments == null)
            return 0;

        if (this.numPoints >= 0)
            return this.numPoints;

        this.numPoints = 0;
        for (TrackSegment segment : this.segments)
        {
            //noinspection UNUSED_SYMBOL,UnusedDeclaration
            for (TrackPoint point : segment.getPoints())
            {
                ++this.numPoints;
            }
        }

        return this.numPoints;
    }

    /**
     * @param uri The element URI.
     * @param lname the element lname.
     * @param qname the element qname.
     * @param attributes the element attributes.
     * @throws IllegalArgumentException if <code>lname</code> is null
     * @throws org.xml.sax.SAXException if a parsing error has occurred.
     */
    @Override
    public void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
        // don't validate uri, qname or attributes - they aren't used
        if (lname == null)
        {
            String msg = Logging.getMessage("nullValue.LNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (qname == null)
        {
            String msg = Logging.getMessage("nullValue.QNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (uri == null)
        {
            String msg = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (lname.equalsIgnoreCase("trkSeg"))
        {
            this.currentElement = new GpxTrackSegment(uri, lname, qname, attributes);
            this.segments.add((TrackSegment) this.currentElement);
        }
    }

    /**
     * @param uri The element URI.
     * @param lname the element lname.
     * @param qname the element qname.
     * @throws IllegalArgumentException if <code>lname</code> is null
     * @throws org.xml.sax.SAXException if a parsing error occurs.
     */
    @Override
    public void doEndElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
    {
        // don't validate uri or qname - they aren't used
        if (lname == null)
        {
            String msg = Logging.getMessage("nullValue.LNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (lname.equalsIgnoreCase("name"))
        {
            this.name = this.currentCharacters;
        }
    }
}
