/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.tracks;

import java.util.NoSuchElementException;

/**
 * @author tag
 * @version $Id: TrackPointIteratorImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TrackPointIteratorImpl implements TrackPointIterator
{
    private Iterable<Track> trackIterable;
    private java.util.Iterator<Track> tracks;
    private java.util.Iterator<TrackSegment> segments;
    private java.util.Iterator<TrackPoint> positions;

    public TrackPointIteratorImpl(Iterable<Track> trackIterable)
    {
        this.trackIterable = trackIterable;
        this.reset();
    }

    public TrackPointIteratorImpl reset()
    {
        if (this.trackIterable == null)
        {
            throw new IllegalArgumentException();
        }

        this.tracks = this.trackIterable.iterator();
        this.segments = null;
        this.positions = null;
        this.loadNextPositions();

        return this;
    }

    public boolean hasNext()
    {
        if (this.positions != null && this.positions.hasNext())
            return true;

        this.loadNextPositions();

        return (this.positions != null && this.positions.hasNext());
    }

    private void loadNextPositions()
    {
        if (this.segments != null && this.segments.hasNext())
        {
            TrackSegment segment = this.segments.next();
            this.positions = segment.getPoints().iterator();
            return;
        }

        if (this.tracks.hasNext())
        {
            Track track = this.tracks.next();
            this.segments = track.getSegments().iterator();
            this.loadNextPositions();
        }
    }

    public TrackPoint next()
    {
        if (!this.hasNext())
        {
            throw new NoSuchElementException();
        }

        return this.positions.next();
    }

    public void remove()
    {
        String msg = null;
        throw new UnsupportedOperationException();
    }

    public int getNumPoints()
    {
        int numPoints;
        for (numPoints = 0; this.hasNext(); this.next())
            ++numPoints;

        return numPoints;
    }
}
