/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwindx.applications.sar.tracks.*;
import gov.nasa.worldwind.formats.csv.CSVWriter;
import gov.nasa.worldwind.formats.gpx.GpxWriter;
import gov.nasa.worldwind.formats.nmea.NmeaWriter;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.Logging;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: SARTrack.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARTrack extends WWObjectImpl implements Iterable<Position>
{
    public static final int FORMAT_GPX = 1;
    public static final int FORMAT_CSV = 2;
    public static final int FORMAT_NMEA = 3;

    private static class FormatInfo
    {
        private TrackReader reader;
        private int format;

        private FormatInfo(TrackReader reader, int format)
        {
            this.reader = reader;
            this.format = format;
        }
    }

    private static int nextColor = 0;
    private static Color[] colors = new Color[]
        {
            Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK, Color.YELLOW
        };

    private static Color nextColor()
    {
        return colors[nextColor++ % colors.length];
    }

    // Meta-track properties.
    private File file = null;
    private String name = null;
    private int format = 0;
    private long lastSaveTime = 0L;
    private long lastModifiedTime = 0L;
    // Track properties.
    private double offset = 0;
    private Color color = nextColor();
    private ArrayList<SARPosition> positions;
    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    public static SARTrack fromFile(String filePath) throws IOException
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = new File(filePath);

        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", filePath);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        SARTrack track = null;

        FormatInfo[] formatInfoArray = new FormatInfo[]
            {
                new FormatInfo(new CSVTrackReader(), FORMAT_CSV),
                new FormatInfo(new GPXTrackReader(), FORMAT_GPX),
                new FormatInfo(new NMEATrackReader(), FORMAT_NMEA),
            };

        int formatIndex;
        for (formatIndex = 0; formatIndex < formatInfoArray.length; formatIndex++)
        {
            track = readTrack(filePath, formatInfoArray[formatIndex]);
            if (track != null)
                break;
        }

        if (track != null)
        {
            track.setFile(file);
            track.setFormat(formatIndex);
            track.setName(file.getName());
        }

        return track;
    }

    private static SARTrack readTrack(String filePath, FormatInfo format)
    {
        if (!format.reader.canRead(filePath))
            return null;

        Track[] tracks = null;
        try
        {
            tracks = format.reader.read(filePath);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadFile", filePath);
            Logging.logger().severe(message);
        }

        if (tracks == null)
            return null;

        TrackPointIterator tpi = new TrackPointIteratorImpl(Arrays.asList(tracks));
        return makeTrackFromTrackPointIterator(tpi);
    }

    public static void toFile(SARTrack track, String filePath, int format) throws IOException
    {
        if (track == null)
            throw new IllegalArgumentException("track is null");
        if (filePath == null)
            throw new IllegalArgumentException("filePath is null");

        if (format == FORMAT_GPX)
            writeGPX(track, filePath);
        else if (format == FORMAT_CSV)
            writeCSV(track, filePath);
        else if (format == FORMAT_NMEA)
            writeNMEA(track, filePath);
        // If no format is specified, then do nothing.
    }

    private SARTrack()
    {
    }

    public SARTrack(String name)
    {
        this.name = name;
        this.positions = new ArrayList<SARPosition>();
    }

    public File getFile()
    {
        return this.file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        this.firePropertyChange(TrackController.TRACK_NAME, null, this);
    }

    public int getFormat()
    {
        return format;
    }

    public void setFormat(int format)
    {
        this.format = format;
    }

    public long getLastSaveTime()
    {
        return this.lastSaveTime;
    }

    public long getLastModifiedTime()
    {
        return this.lastModifiedTime;
    }

    public boolean isDirty()
    {
        return this.lastModifiedTime == 0L || this.lastSaveTime == 0L || (this.lastModifiedTime > this.lastSaveTime);
    }

    public void markDirty()
    {
        this.lastModifiedTime = System.currentTimeMillis();
        this.firePropertyChange(TrackController.TRACK_DIRTY_BIT, null, this);
    }

    public void clearDirtyBit()
    {
        long time = System.currentTimeMillis();
        this.lastSaveTime = time;
        this.lastModifiedTime = time;
        this.firePropertyChange(TrackController.TRACK_DIRTY_BIT, null, this);
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public int size()
    {
        return this.positions.size();
    }

    public ArrayList<SARPosition> getPositions()
    {
        return this.positions;
    }

    public SARPosition get(int index)
    {
        return this.positions.size() > index ? this.positions.get(index) : null;
    }

    public void set(int index, SARPosition position)
    {
        if (position == null)
            return;

        if (index >= this.positions.size())
            this.positions.add(position);
        else
            this.positions.set(index, position);

        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, index);
    }

    public void add(int index, SARPosition position)
    {
        if (position == null)
            return;

        if (index >= this.positions.size())
            this.positions.add(position);
        else
            this.positions.add(index, position);

        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, this);
    }

    public double getOffset()
    {
        return offset;
    }

    public void setOffset(double offset)
    {
        double oldOffset = this.offset;
        this.offset = offset;

        this.firePropertyChange(TrackController.TRACK_OFFSET, oldOffset, this.offset);
    }

    public Iterator<Position> iterator()
    {
        return new Iterator<Position>()
        {
            private Iterator<SARPosition> iter = SARTrack.this.positions.iterator();

            public boolean hasNext()
            {
                return this.iter.hasNext();
            }

            public Position next()
            {
                return this.iter.next();
            }

            public void remove()
            {
                throw new UnsupportedOperationException("Remove operation not supported for SARTrack iterator");
            }
        };
    }

    public void removePosition(int index)
    {
        if (index < 0 || index >= this.positions.size())
            return;

        this.positions.remove(index);
        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, this);
    }

    public void removePositions(int[] positionNumbers)
    {
        Arrays.sort(positionNumbers);
        for (int i = positionNumbers.length - 1; i >= 0; i--)
        {
            if (positionNumbers[i] < 0 || positionNumbers[i] >= this.positions.size())
                continue;

            this.positions.remove(positionNumbers[i]);
        }

        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, this);
    }

    public void appendPosition(SARPosition position)
    {
        if (position == null)
            return;

        this.positions.add(position);
        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, this);
    }

    public void insertPosition(int index, SARPosition position)
    {
        if (position == null || index < 0)
            return;

        this.positions.add(index, position);
        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, this);
    }

    public void setPosition(int index, SARPosition position)
    {
        if (position == null || index < 0)
            return;

        this.positions.set(index, position);
        this.markDirty();
        this.firePropertyChange(TrackController.TRACK_MODIFY, null, index);
    }

    private static void writeNMEA(SARTrack track, String filePath) throws IOException
    {
        NmeaWriter writer = new NmeaWriter(filePath);
        Track trk = makeTrackFromSARTrack(track);
        writer.writeTrack(trk);
        writer.close();
    }

    private static void writeGPX(SARTrack track, String filePath) throws IOException
    {
        try
        {
            GpxWriter writer = new GpxWriter(filePath);
            Track trk = makeTrackFromSARTrack(track);
            writer.writeTrack(trk);
            writer.close();
        }
        catch (ParserConfigurationException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (javax.xml.transform.TransformerException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private static void writeCSV(SARTrack track, String filePath) throws IOException
    {
        CSVWriter writer = new CSVWriter(filePath);
        Track trk = makeTrackFromSARTrack(track);
        writer.writeTrack(trk);
        writer.close();
    }

    private static SARTrack makeTrackFromTrackPointIterator(TrackPointIterator tpi)
    {
        ArrayList<SARPosition> positions = new ArrayList<SARPosition>();
        while (tpi.hasNext())
        {
            TrackPoint tp = tpi.next();
            SARPosition sp = new SARPosition(
                Angle.fromDegrees(tp.getLatitude()), Angle.fromDegrees(tp.getLongitude()), tp.getElevation());
            positions.add(sp);
        }

        SARTrack st = new SARTrack();
        st.positions = positions;

        return st;
    }

    private static Track makeTrackFromSARTrack(SARTrack sarTrack)
    {
        return new TrackWrapper(sarTrack);
    }

    private static class TrackWrapper implements Track, TrackSegment
    {
        private final SARTrack sarTrack;
        private final ArrayList<TrackSegment> segments = new ArrayList<TrackSegment>();

        public TrackWrapper(SARTrack sarTrack)
        {
            this.sarTrack = sarTrack;
            this.segments.add(this);
        }

        public java.util.List<TrackSegment> getSegments()
        {
            return this.segments;
        }

        public String getName()
        {
            return this.sarTrack.getName();
        }

        public int getNumPoints()
        {
            return this.sarTrack.size();
        }

        public java.util.List<TrackPoint> getPoints()
        {
            ArrayList<TrackPoint> trkPoints = new ArrayList<TrackPoint>();
            for (SARPosition sarPos : this.sarTrack.positions)
            {
                trkPoints.add(sarPos != null ? new TrackPointWrapper(sarPos) : null);
            }
            return trkPoints;
        }
    }

    private static class TrackPointWrapper implements TrackPoint
    {
        private final SARPosition sarPosition;

        public TrackPointWrapper(SARPosition sarPosition)
        {
            this.sarPosition = sarPosition;
        }

        public double getLatitude()
        {
            return this.sarPosition.getLatitude().degrees;
        }

        public void setLatitude(double latitude)
        {
        }

        public double getLongitude()
        {
            return this.sarPosition.getLongitude().degrees;
        }

        public void setLongitude(double longitude)
        {
        }

        public double getElevation()
        {
            return this.sarPosition.getElevation();
        }

        public void setElevation(double elevation)
        {
        }

        public String getTime()
        {
            return null;
        }

        public void setTime(String time)
        {
        }

        public Position getPosition()
        {
            return this.sarPosition;
        }

        public void setPosition(Position position)
        {
        }
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        this.propChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propChangeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        this.propChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
