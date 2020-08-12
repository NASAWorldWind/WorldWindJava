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
package gov.nasa.worldwind.tracks;

import gov.nasa.worldwind.util.Logging;

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
            String msg = Logging.getMessage("nullValue.TracksIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
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
            String msg = Logging.getMessage("TrackPointIterator.NoMoreTrackPoints");
            Logging.logger().severe(msg);
            throw new NoSuchElementException(msg);
        }

        return this.positions.next();
    }

    public void remove()
    {
        String msg = Logging.getMessage("TrackPointIterator.RemoveNotSupported");
        Logging.logger().severe(msg);
        throw new UnsupportedOperationException(msg);
    }

    public int getNumPoints()
    {
        int numPoints;
        for (numPoints = 0; this.hasNext(); this.next())
            ++numPoints;

        return numPoints;
    }
}
