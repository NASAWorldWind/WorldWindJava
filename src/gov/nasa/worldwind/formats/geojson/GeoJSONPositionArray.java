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
