/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.csv;

import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.tracks.TrackSegment;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: CSVWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CSVWriter
{
    private final java.io.PrintWriter printWriter;
    private int lineNumber = 0;

    public CSVWriter(String path) throws java.io.IOException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.printWriter = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(path)));
    }

    public CSVWriter(java.io.OutputStream stream) throws java.io.IOException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.printWriter = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.OutputStreamWriter(stream)));
    }

    public void writeTrack(Track track)
    {
        if (track == null)
        {
            String msg = Logging.getMessage("nullValue.TrackIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        doWriteTrack(track,this.printWriter);
        doFlush();
    }

    public void close()
    {
        doFlush();
        this.printWriter.close();
    }

    private void doWriteTrack(Track track, java.io.PrintWriter out)
    {
        if (track != null && track.getSegments() != null)
        {
            for (TrackSegment ts : track.getSegments())
                doWriteTrackSegment(ts, out);
        }
    }

    private void doWriteTrackSegment(TrackSegment segment, java.io.PrintWriter out)
    {
        if (segment != null && segment.getPoints() != null)
        {
            for (TrackPoint tp : segment.getPoints())
                doWriteTrackPoint(tp, out);
        }
    }

    private void doWriteTrackPoint(TrackPoint point, java.io.PrintWriter out)
    {
        if (point != null)
        {
            int lineNum = this.lineNumber++;
            out.print(lineNum);
            out.print(",");
            out.print(point.getLatitude());
            out.print(",");
            out.print(point.getLongitude());
            out.print(",");
            out.print(point.getElevation());
            out.print(",");
            out.print(point.getTime() != null ? point.getTime() : "");
            out.println();
        }
    }

    private void doFlush()
    {
        this.printWriter.flush();
    }
}
