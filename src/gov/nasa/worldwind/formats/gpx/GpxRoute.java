/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.gpx;

import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.tracks.TrackSegment;

import java.util.Arrays;
import java.util.List;

/**
 * @author tag
 * @version $Id: GpxRoute.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GpxRoute extends gov.nasa.worldwind.formats.gpx.ElementParser implements Track, TrackSegment
{
    private String name;
    private java.util.List<TrackPoint> points = new java.util.ArrayList<TrackPoint>();

    @SuppressWarnings({"UnusedDeclaration"})
    public GpxRoute(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
    {
        super("rte");

        // dont' validate uri, lname, qname or attributes as they aren't used.
    }

    public List<TrackSegment> getSegments()
    {
        return Arrays.asList((TrackSegment) this);
    }

    public String getName()
    {
        return this.name;
    }

    public int getNumPoints()
    {
        return this.points.size();
    }

    public java.util.List<TrackPoint> getPoints()
    {
        return this.points;
    }

    @Override
    public void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
        if (lname == null)
        {
            throw new IllegalArgumentException();
        }

        if (uri == null)
        {
            throw new IllegalArgumentException();
        }
        if (qname == null)
        {
            throw new IllegalArgumentException();
        }
        if (attributes == null)
        {
            throw new IllegalArgumentException();
        }

        if (lname.equalsIgnoreCase("rtept"))
        {
            this.currentElement = new GpxRoutePoint(uri, lname, qname, attributes);
            this.points.add((TrackPoint) this.currentElement);
        }
    }
    
    @Override
    public void doEndElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
    {
        // don't validate uri or qname - they aren't used
        if (lname == null)
        {
            throw new IllegalArgumentException();
        }
        
        if (lname.equalsIgnoreCase("name"))
        {
            this.name = this.currentCharacters;
        }
    }
}
