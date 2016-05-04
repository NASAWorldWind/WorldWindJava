/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.gpx;

import gov.nasa.worldwind.tracks.*;

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
     * @param uri
     * @param lname
     * @param qname
     * @param attributes
     * @throws IllegalArgumentException if <code>lname</code> is null
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
        // don't validate uri, qname or attributes - they aren't used
        if (lname == null)
        {
            throw new IllegalArgumentException();
        }
        if (qname == null)
        {
            throw new IllegalArgumentException();
        }
        if (uri == null)
        {
            throw new IllegalArgumentException();
        }
        if (attributes == null)
        {
            throw new IllegalArgumentException();
        }

        if (lname.equalsIgnoreCase("trkSeg"))
        {
            this.currentElement = new GpxTrackSegment(uri, lname, qname, attributes);
            this.segments.add((TrackSegment) this.currentElement);
        }
    }

    /**
     * @param uri
     * @param lname
     * @param qname
     * @throws IllegalArgumentException if <code>lname</code> is null
     * @throws org.xml.sax.SAXException
     */
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
