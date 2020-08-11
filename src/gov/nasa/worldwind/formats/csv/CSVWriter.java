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
