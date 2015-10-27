/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.geojson;

import com.jogamp.common.nio.Buffers;
import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.Logging;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: GeoJSONCoordinateParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoJSONCoordinateParser extends GeoJSONEventParser
{
    protected static final int INITIAL_POSITION_BUFFER_CAPACITY = 2;

    protected DoubleBuffer posBuffer;
    protected int startPos;
    protected int endPos;

    public GeoJSONCoordinateParser()
    {
    }

    @Override
    protected Object parseArray(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        JSONEvent nextEvent = ctx.peek();

        // This array may either represent a single tuple, or a complex array of tuples or arrays. We peek at the next
        // event to determine if this array is a simple tuple or a complex structure.

        if (nextEvent != null && nextEvent.isNumericValue())
        {
            return this.parseSimpleArray(ctx, event);
        }
        else if (nextEvent != null && nextEvent.isStartArray())
        {
            return this.parseComplexArray(ctx, event);
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", event));
            return null;
        }
    }

    //**************************************************************//
    //********************  Position Parsing  **********************//
    //**************************************************************//

    protected void startPositionArray()
    {
        this.startPos = (this.posBuffer != null) ? this.posBuffer.position() : 0;
    }

    protected void endPositionArray()
    {
        this.endPos = (this.posBuffer != null) ? this.posBuffer.position() : 0;
    }

    protected int parsePosition(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numRead = 0;

        // The first iteration consumes the position's start array event.
        for (event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isEndArray())
                break;

            if (!event.isNumericValue())
            {
                Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", event));
                continue;
            }

            if (this.posBuffer == null)
                this.posBuffer = this.allocatePositionBuffer(INITIAL_POSITION_BUFFER_CAPACITY);
            else if (this.posBuffer.remaining() == 0)
                this.expandPositionBuffer(1 + this.posBuffer.capacity());

            this.posBuffer.put(event.asNumericValue());
            numRead++;
        }

        return numRead;
    }

    protected Object resolvePositionArray(int positionSize)
    {
        if (this.posBuffer == null || this.startPos == this.endPos)
            return null;

        return new GeoJSONPositionArray(positionSize, this.posBuffer, this.startPos, this.endPos);
    }

    protected DoubleBuffer allocatePositionBuffer(int capacity)
    {
        return Buffers.newDirectDoubleBuffer(capacity);
    }

    protected void expandPositionBuffer(int minCapacity)
    {
        int newCapacity = 2 * this.posBuffer.capacity();

        // If the new capacity overflows the range of 32-bit integers, then use the largest 32-bit integer.
        if (newCapacity < 0)
        {
            newCapacity = Integer.MAX_VALUE;
        }
        // If the new capacity is still not large enough for the minimum capacity specified, then just use the minimum
        // capacity specified.
        else if (newCapacity < minCapacity)
        {
            newCapacity = minCapacity;
        }

        this.posBuffer.flip();
        this.posBuffer = this.allocatePositionBuffer(newCapacity).put(this.posBuffer);
    }

    //**************************************************************//
    //********************  Simple Array Parsing  ******************//
    //**************************************************************//

    protected Object parseSimpleArray(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.startPositionArray();
        int positionSize = this.parsePosition(ctx, event);
        this.endPositionArray();

        return this.resolvePositionArray(positionSize);
    }

    //**************************************************************//
    //********************  Complex Array Parsing  *****************//
    //**************************************************************//

    protected Object parseComplexArray(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // This array may either represent an array of tuples or an array of arrays. We advance to the first child
        // array's start element and peek at its first value to determine which. Both parseArrayOfPositions() and
        // parseArrayOfArrays() expect to start at the first child array's start element.

        event = ctx.nextEvent();
        if (event == null || !event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        JSONEvent peek = ctx.peek();

        if (peek != null && peek.isNumericValue())
            return this.parseArrayOfPositions(ctx, event);

        else if (peek != null && peek.isStartArray())
            return this.parseArrayOfArrays(ctx, event);

        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnexpectedEvent", peek));
            return null;
        }
    }

    protected Object parseArrayOfPositions(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int posSize = -1;
        this.startPositionArray();

        // Assume that we start parsing at a position's start array element. We let parsePosition() consume the
        // position's start element.
        for (; ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isEndArray())
                break;

            int numRead = this.parsePosition(ctx, event);

            // Compute the size of a position in this array. Assume that the number of coordinates in the first position
            // is consistent with the remaining positions in this array.
            if (posSize < 0)
                posSize = numRead;
        }

        this.endPositionArray();

        return this.resolvePositionArray(posSize);
    }

    protected Object parseArrayOfArrays(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        if (!event.isStartArray())
        {
            String message = Logging.getMessage("generic.InvalidEvent", event);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<Object> list = null; // List is lazily constructed below.

        // Assume that we start parsing at a child array's start array element. We let parseComplexArray() consume the
        // child array's start element.
        for (; ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (event.isEndArray())
                break;

            Object o = this.parseComplexArray(ctx, event);
            if (o == null)
                continue;

            if (list == null)
                list = new ArrayList<Object>();

            list.add(o);
        }

        return this.resolveArrayOfArrays(list);
    }

    protected Object resolveArrayOfArrays(List<?> list)
    {
        if (list == null || list.size() == 0)
            return null;

        if (list.get(0) instanceof GeoJSONPositionArray)
        {
            GeoJSONPositionArray[] a = new GeoJSONPositionArray[list.size()];
            for (int i = 0; i < list.size(); i++)
            {
                a[i] = (GeoJSONPositionArray) list.get(i);
            }
            return a;
        }
        else if (list.get(0) instanceof List)
        {
            GeoJSONPositionArray[][] a = new GeoJSONPositionArray[list.size()][];
            for (int i = 0; i < list.size(); i++)
            {
                for (int j = 0; j < ((List) list.get(i)).size(); j++)
                {
                    a[i][j] = (GeoJSONPositionArray) ((List) list.get(i)).get(j);
                }
            }
            return a;
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnexpectedObjectType", list.get(0)));
            return null;
        }
    }
}
