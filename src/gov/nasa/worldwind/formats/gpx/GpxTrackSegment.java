/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.gpx;

import gov.nasa.worldwind.tracks.*;

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
     * @param uri
     * @param lname
     * @param qname
     * @param attributes
     * @throws IllegalArgumentException if any parameter is null
     * @throws org.xml.sax.SAXException
     */
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

        if (lname.equalsIgnoreCase("trkpt"))
        {
            this.currentElement = new gov.nasa.worldwind.formats.gpx.GpxTrackPoint(uri, lname, qname, attributes);
            this.points.add((TrackPoint) this.currentElement);
        }
    }
}
