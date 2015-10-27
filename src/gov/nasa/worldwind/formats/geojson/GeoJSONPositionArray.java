/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

import java.nio.DoubleBuffer;
import java.util.Iterator;

/**
 * @author dcollins
 * @version $Id: GeoJSONPositionArray.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONPositionArray implements Iterable<Position>
{
    protected int positionSize;
    protected DoubleBuffer buffer;
    protected int startPos;
    protected int endPos;

    public GeoJSONPositionArray(int positionSize, DoubleBuffer buffer, int startPos, int endPos)
    {
        if (positionSize < 2)
        {
            String message = Logging.getMessage("generic.InvalidTupleSize", positionSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positionSize = positionSize;
        this.buffer = buffer;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public int length()
    {
        return (this.endPos - this.startPos) / this.positionSize;
    }

    public Position getPosition(int index)
    {
        // GeoJSON coordinates are stored as lon,lat or lon,lat,altitude.
        int pos = this.startPos + this.positionSize * index;
        return (this.positionSize >= 3) ?
            Position.fromDegrees(this.buffer.get(pos + 1), this.buffer.get(pos), this.buffer.get(pos + 2)) :
            Position.fromDegrees(this.buffer.get(pos + 1), this.buffer.get(pos));
    }

    public Iterator<Position> iterator()
    {
        return new PositionIterator(this);
    }

    protected static class PositionIterator implements Iterator<Position>
    {
        protected GeoJSONPositionArray array;
        protected int index;

        public PositionIterator(GeoJSONPositionArray array)
        {
            this.array = array;
            this.index = 0;
        }

        public boolean hasNext()
        {
            return this.index < this.array.length();
        }

        public Position next()
        {
            return this.array.getPosition(this.index++);
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
