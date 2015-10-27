/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.gpx;

import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.geom.Position;

import java.util.Iterator;

/**
 * @author tag
 * @version $Id: GpxReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GpxReader // TODO: I18N, proper exception handling, remove stack-trace prints
{
    private javax.xml.parsers.SAXParser parser;
    private java.util.List<Track> tracks = new java.util.ArrayList<Track>();

    public GpxReader() throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException
    {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        this.parser = factory.newSAXParser();
    }

    /**
     * @param path
     * @throws IllegalArgumentException if <code>path</code> is null
     * @throws java.io.IOException      if no file exists at the location specified by <code>path</code>
     * @throws org.xml.sax.SAXException
     */
    public void readFile(String path) throws java.io.IOException, org.xml.sax.SAXException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists())
        {
            String msg = Logging.getMessage("generic.FileNotFound", path);
            Logging.logger().severe(msg);
            throw new java.io.FileNotFoundException(path);
        }

        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        this.doRead(fis);
    }

    /**
     * @param stream
     * @throws IllegalArgumentException if <code>stream</code> is null
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void readStream(java.io.InputStream stream) throws java.io.IOException, org.xml.sax.SAXException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.doRead(stream);
    }

    public java.util.List<Track> getTracks()
    {
        return this.tracks;
    }

    public Iterator<Position> getTrackPositionIterator()
    {
        return new Iterator<Position>()
        {
            private TrackPointIterator trackPoints = new TrackPointIteratorImpl(GpxReader.this.tracks);

            public boolean hasNext()
            {
                return this.trackPoints.hasNext();
            }

            public Position next()
            {
                return this.trackPoints.next().getPosition();
            }

            public void remove()
            {
                this.trackPoints.remove();
            }
        };
    }

    private void doRead(java.io.InputStream fis) throws java.io.IOException, org.xml.sax.SAXException
    {
        this.parser.parse(fis, new Handler());
    }

    private class Handler extends org.xml.sax.helpers.DefaultHandler
    {
        // this is a private class used solely by the containing class, so no validation occurs in it.

        private gov.nasa.worldwind.formats.gpx.ElementParser currentElement = null;

        @Override
        public void warning(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.warning(saxParseException);
        }

        @Override
        public void error(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.error(saxParseException);
        }

        @Override
        public void fatalError(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.fatalError(saxParseException);
        }

        private boolean firstElement = true;

        @Override
        public void startElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
            throws org.xml.sax.SAXException
        {
            if (this.firstElement)
            {
                if (!lname.equalsIgnoreCase("gpx"))
                    throw new IllegalArgumentException(Logging.getMessage("formats.notGPX", uri));
                else
                    this.firstElement = false;
            }

            if (this.currentElement != null)
            {
                this.currentElement.startElement(uri, lname, qname, attributes);
            }
            else if (lname.equalsIgnoreCase("trk"))
            {
                GpxTrack track = new GpxTrack(uri, lname, qname, attributes);
                this.currentElement = track;
                GpxReader.this.tracks.add(track);
            }
            else if (lname.equalsIgnoreCase("rte"))
            {
                GpxRoute route = new GpxRoute(uri, lname, qname, attributes);
                this.currentElement = route;
                GpxReader.this.tracks.add(route);
            }
        }

        @Override
        public void endElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
            {
                this.currentElement.endElement(uri, lname, qname);

                if (lname.equalsIgnoreCase(this.currentElement.getElementName()))
                    this.currentElement = null;
            }
        }

        @Override
        public void characters(char[] data, int start, int length) throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
                this.currentElement.characters(data, start, length);
        }
    }
}
