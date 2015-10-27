/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.nmea;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.tracks.TrackSegment;
import gov.nasa.worldwind.tracks.TrackPoint;

/**
 * @author dcollins
 * @version $Id: NmeaWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NmeaWriter
{
    private final java.io.PrintStream printStream;
    private final String encoding;
    @SuppressWarnings({"UnusedDeclaration"})
    private int sentenceNumber = 0;
    private static final String DEFAULT_ENCODING = "US-ASCII";

    public NmeaWriter(String path) throws java.io.IOException
    {
        this(path, DEFAULT_ENCODING);
    }

    public NmeaWriter(String path, String encoding) throws java.io.IOException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (encoding == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.encoding = encoding;
        this.printStream = new java.io.PrintStream(
            new java.io.BufferedOutputStream(new java.io.FileOutputStream(path)),
            false, // Disable autoflush.
            this.encoding); // Character mapping from 16-bit UTF characters to bytes.
    }

    public NmeaWriter(java.io.OutputStream stream) throws java.io.IOException
    {
        this(stream, DEFAULT_ENCODING);    
    }

    public NmeaWriter(java.io.OutputStream stream, String encoding) throws java.io.IOException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (encoding == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        this.encoding = encoding;
        this.printStream = new java.io.PrintStream(
            new java.io.BufferedOutputStream(stream),
            false, // Disable autoflush.
            this.encoding); // Character mapping from 16-bit UTF characters to bytes.
    }

    public final String getEncoding()
    {
        return this.encoding;
    }

    public void writeTrack(Track track)
    {
        if (track == null)
        {
            String msg = Logging.getMessage("nullValue.TrackIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        doWriteTrack(track, this.printStream);
        doFlush();
    }

    public void close()
    {
        doFlush();
        this.printStream.close();
    }

    private void doWriteTrack(Track track, java.io.PrintStream out)
    {
        if (track != null && track.getSegments() != null)
        {
            for (TrackSegment ts : track.getSegments())
                doWriteTrackSegment(ts, out);
        }
    }

    private void doWriteTrackSegment(TrackSegment segment, java.io.PrintStream out)
    {
        if (segment != null && segment.getPoints() != null)
        {
            for (TrackPoint tp : segment.getPoints())
            {
                if (tp instanceof NmeaTrackPoint)
                    doWriteNmeaTrackPoint((NmeaTrackPoint) tp, out);
                else
                    doWriteTrackPoint(tp, out);
            }
        }
    }

    private void doWriteTrackPoint(TrackPoint point, java.io.PrintStream out)
    {
        if (point != null)
        {
            writeGGASentence(point.getTime(), point.getLatitude(), point.getLongitude(), point.getElevation(), 0, out);
        }
    }

    private void doWriteNmeaTrackPoint(NmeaTrackPoint point, java.io.PrintStream out)
    {
        if (point != null)
        {
            // TODO: separate elevation and geoid-height
            writeGGASentence(point.getTime(), point.getLatitude(), point.getLongitude(), point.getElevation(), 0, out);
        }
    }

    private void writeGGASentence(String time, double lat, double lon, double altitude, double geoidHeight,
                                  java.io.PrintStream out)
    {
        this.sentenceNumber++;
        // Documentation for NMEA Standard 0183
        // taken from http://www.gpsinformation.org/dale/nmea.htm#GGA
        StringBuilder sb = new StringBuilder();
        sb.append("GP");
        // Global Positioning System Fix Data
        sb.append("GGA");
        sb.append(",");
        // Fix taken at "HHMMSS" UTC
        sb.append(formatTime(time));
        sb.append(",");
        // Latitude "DDMM.MMM,[N|S]"
        sb.append(formatLatitude(lat));
        sb.append(",");
        // Longitude "DDDMM.MMM,[N|S]"
        sb.append(formatLongitude(lon));
        sb.append(",");
        // Fix quality: 0 = invalid
        //              1 = GPS fix (SPS)
        //              2 = DGPS fix
        //              3 = PPS fix
		//              4 = Real Time Kinematic
		//              5 = Float RTK
        //              6 = estimated (dead reckoning) (2.3 feature)
		//              7 = Manual input mode
		//              8 = Simulation mode
        sb.append(""); // Intentionally left blank.
        sb.append(",");
        // Number of satellites being tracked
        sb.append(""); // Intentionally left blank.
        sb.append(",");
        // Horizontal dilution of position
        sb.append(""); // Intentionally left blank
        sb.append(",");
        // Altitude, Meters, above mean sea level
        sb.append(formatElevation(altitude));
        sb.append(",");
        // Height of geoid (mean sea level) above WGS84 ellipsoid
        sb.append(formatElevation(geoidHeight));
        sb.append(",");
        // time in seconds since last DGPS update
        sb.append(""); // Intentionally left blank.
        sb.append(",");
        // DGPS station ID number
        sb.append(""); // Intentionally left blank.
        sb.append(",");
        // the checksum data, always begins with *
        int chksum = computeChecksum(sb, 0, sb.length());
        sb.append("*");
        sb.append(formatChecksum(chksum));

        out.print("$");
        out.print(sb);
        out.print("\r\n");
        doFlush();
    }

    private String formatTime(String time)
    {
        // Format time as "HHMMSS"
        return (time != null) ? time : "";
    }

    private String formatLatitude(double degrees)
    {
        int d = (int) Math.floor(Math.abs(degrees));
        double m = 60 * (Math.abs(degrees) - d);
        // Format latitude as "DDMM.MMM[N|S]"
        return String.format("%02d%06.3f,%s", d, m, degrees < 0 ? "S" : "N");
    }

    private String formatLongitude(double degrees)
    {
        int d = (int) Math.floor(Math.abs(degrees));
        double m = 60 * (Math.abs(degrees) - d);
        // Format longitude as "DDDMM.MMM[N|S]"
        return String.format("%03d%06.3f,%s", d, m, degrees < 0 ? "W" : "E");
    }

    private String formatElevation(double metersElevation)
    {
        // Format elevation with 1 digit of precision.
        // This provides decimeter resolution.
        return String.format("%.1f,M", metersElevation);
    }

    private String formatChecksum(int checksum)
    {
        return Integer.toHexString(checksum);
    }

    private int computeChecksum(CharSequence s, int start, int end)
    {
        int chksum = 0;
        for (int i = start; i < end; i++)
        {
            int c = 0xFF & (int) s.charAt(i);
            chksum ^= c;
        }
        return chksum;
    }

    private void doFlush()
    {
        this.printStream.flush();
    }
}
