/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.csv;

import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.geom.Position;

import java.io.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: CSVReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CSVReader implements Track, TrackSegment
{
    private List<Track> tracks = new ArrayList<Track>();
    private List<TrackSegment> segments = new ArrayList<TrackSegment>();
    private List<TrackPoint> points = new ArrayList<TrackPoint>();
    private String name;
//    private int lineNumber = 0;

    public CSVReader()
    {
        this.tracks.add(this);
        this.segments.add(this);
    }

    public List<TrackSegment> getSegments()
    {
        return this.segments;
    }

    public String getName()
    {
        return this.name;
    }

    public int getNumPoints()
    {
        return this.points.size();
    }

    public List<TrackPoint> getPoints()
    {
        return this.points;
    }

    /**
     * @param path File spec to read from.
     * @throws IllegalArgumentException if <code>path</code> is null
     * @throws java.io.IOException If there are issues reading from the file.
     */
    public void readFile(String path) throws IOException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.name = path;

        java.io.File file = new java.io.File(path);
        if (!file.exists())
        {
            String msg = Logging.getMessage("generic.FileNotFound", path);
            Logging.logger().severe(msg);
            throw new FileNotFoundException(path);
        }

        FileInputStream fis = new FileInputStream(file);
        this.doReadStream(fis);
    }

    /**
     * @param stream The stream to read from.
     * @param name The name of the stream.
     * @throws IllegalArgumentException if <code>stream</code> is null
     * @throws java.io.IOException If there are issues reading the stream.
     */
    public void readStream(InputStream stream, String name) throws IOException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.name = name != null ? name : "Un-named stream";
        this.doReadStream(stream);
    }

    public List<Track> getTracks()
    {
        return this.tracks;
    }

    public Iterator<Position> getTrackPositionIterator()
    {
        return new Iterator<Position>()
        {
            private TrackPointIterator trackPoints = new TrackPointIteratorImpl(CSVReader.this.tracks);

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

    private void doReadStream(InputStream stream)
    {
        String sentence;
        Scanner scanner = new Scanner(stream);

        try
        {
            do
            {
                sentence = scanner.nextLine();
                if (sentence != null)
                {
//                    ++this.lineNumber;
                    this.parseLine(sentence);
                }
            } while (sentence != null);
        }
        catch (NoSuchElementException e)
        {
            //noinspection UnnecessaryReturnStatement
            return;
        }
    }

    private void parseLine(String sentence)
    {
//        try
//        {
        if ( sentence.trim().length() > 0)
        {
            CSVTrackPoint point = new CSVTrackPoint(sentence.split(","));
            this.points.add(point);
        }
//        }
//        catch (Exception e)
//        {
//            System.out.printf("Exception %s at sentence number %d for %s\n",
//                e.getMessage(), this.lineNumber, this.name);
//        }
    }
}
