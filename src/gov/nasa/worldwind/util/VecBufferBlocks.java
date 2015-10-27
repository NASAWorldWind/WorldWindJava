/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import java.nio.ByteBuffer;

/**
 * VecBufferBlocks provides storage and retrieval of a set of potentially random VecBuffer blocks in a single backing
 * ByteBuffer. Its is assumeed that the backing ByteBuffer already contains the necessary data, and the caller defines
 * which regions in the ByteBuffer define each individual VecBuffer. VecBuffers are defined by calling {@link
 * #addBlock(int, int)}, where the positions define the byte range containing a VecBuffer's data.
 *
 * @author dcollins
 * @version $Id: VecBufferBlocks.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VecBufferBlocks extends CompoundVecBuffer
{
    protected int coordsPerVec;
    protected String dataType;
    protected ByteBuffer buffer;

    /**
     * Constructs a VecBufferBlocks with the specified number of coordinates per logical vector, primitive data type,
     * backing ByteBuffer and the specified initial capacity. This interprets the ByteBuffer according to the specified
     * primitive data type and number of coordinates per logical vector, and does not modify the ByteBuffer in any way.
     * This assumes the buffer's position and limit are not changed by the caller for the lifetime of this instance.
     *
     * @param coordsPerVec the number of coordinates per logical vector.
     * @param dataType     the primitive data type.
     * @param buffer       the backing ByteBuffer.
     * @param capacity     the PackedCompoundVecBuffer's initial capacity, in number of sub-buffers.
     *
     * @throws IllegalArgumentException if the coordsPerVec is less than 1, if the dataType is null, if the buffer is
     *                                  null, or if the capacity is less than 1.
     */
    public VecBufferBlocks(int coordsPerVec, String dataType, ByteBuffer buffer, int capacity)
    {
        super(capacity);

        if (coordsPerVec < 1)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dataType == null)
        {
            String message = Logging.getMessage("nullValue.DataTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.coordsPerVec = coordsPerVec;
        this.dataType = dataType;
        this.buffer = buffer;
    }

    /**
     * Constructs a VecBufferBlocks with the specified number of coordinates per logical vector, primitive data type,
     * and backing ByteBuffer. This interprets the ByteBuffer according to the specified primitive data type and number
     * of coordinates per logical vector, and does not modify the ByteBuffer in any way. This assumes the buffer's
     * position and limit are not changed by the caller for the lifetime of this instance.
     *
     * @param coordsPerVec the number of coordinates per logical vector.
     * @param dataType     the primitive data type.
     * @param buffer       the backing ByteBuffer.
     *
     * @throws IllegalArgumentException if the coordsPerVec is less than 1, if the dataType is null, or if the buffer is
     *                                  null.
     */
    public VecBufferBlocks(int coordsPerVec, String dataType, ByteBuffer buffer)
    {
        this(coordsPerVec, dataType, buffer, DEFAULT_INITIAL_CAPACITY);
    }

    protected VecBufferBlocks(VecBufferBlocks that, int beginIndex, int endIndex)
    {
        super(that, beginIndex, endIndex);

        this.coordsPerVec = that.coordsPerVec;
        this.dataType = that.dataType;
        this.buffer = that.buffer;
    }

    protected VecBufferBlocks(VecBufferBlocks that, int[] indices, int offset, int length)
    {
        super(that, indices, offset, length);

        this.coordsPerVec = that.coordsPerVec;
        this.dataType = that.dataType;
        this.buffer = that.buffer;
    }

    /** {@inheritDoc} */
    public int subBufferSize(int index)
    {
        if (index < 0 || index >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.lengths.get(index) / WWBufferUtil.sizeOfPrimitiveType(this.dataType) / this.coordsPerVec;
    }

    /** {@inheritDoc} */
    public int getCoordsPerVec()
    {
        return this.coordsPerVec;
    }

    /**
     * Returns the primitive data type used to interpret the elements of this VecBufferBlocks' backing ByteBuffer.
     *
     * @return this VecBufferBlocks' primitive data type.
     */
    public String getDataType()
    {
        return dataType;
    }

    /**
     * Returns the ByteBuffer that stores this VecBufferBlocks' sub-buffers.
     *
     * @return this VecBufferBlocks' backing ByteBuffer.
     */
    public ByteBuffer getBuffer()
    {
        return buffer;
    }

    /**
     * Adds a range of bytes that define a new sub-buffer within this VecBufferBlocks. This contents of this buffer's
     * backing ByteBuffer is not changed, nor are its position and limit. The specified range must define a sequence of
     * bytes representing logical vector elements according to this buffer's number of coordinates per logical vector,
     * and this buffer's primitive data type.
     *
     * @param beginPos the byte range's beginning position.
     * @param endPos   the byte range's ending position (inclusive).
     *
     * @return the sub-buffer's index.
     *
     * @throws IllegalArgumentException if either the position are less than zero, if either position is greater than
     *                                  the backing buffer's capacity, or if the begin position is greater than the end
     *                                  position.
     */
    public int addBlock(int beginPos, int endPos)
    {
        if (endPos < 0 || endPos > this.buffer.capacity())
        {
            String message = Logging.getMessage("generic.indexOutOfRange", endPos);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (beginPos < 0 || beginPos > endPos)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", beginPos);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.addSubBuffer(beginPos, endPos - beginPos + 1);
    }

    //**************************************************************//
    //********************  Protected Interface  *******************//
    //**************************************************************//

    protected VecBuffer createSubBuffer(int offsetInBytes, int lengthInBytes)
    {
        VecBuffer subBuffer;

        // Save the buffer's current position and limit.
        int lim = this.buffer.limit();
        int pos = this.buffer.position();
        try
        {
            this.buffer.limit(offsetInBytes + lengthInBytes);
            this.buffer.position(offsetInBytes);
            BufferWrapper slice = BufferWrapper.wrap(this.buffer, this.dataType);
            subBuffer = new VecBuffer(this.coordsPerVec, slice);
        }
        finally
        {
            // Restore the buffer's previous limit and position. Restore limit first in case the position is greater
            // than the current limit.
            this.buffer.limit(lim);
            this.buffer.position(pos);
        }

        return subBuffer;
    }

    protected CompoundVecBuffer createSlice(int[] indices, int offset, int length)
    {
        return new VecBufferBlocks(this, indices, offset, length);
    }

    protected CompoundVecBuffer createSlice(int beginIndex, int endIndex)
    {
        return new VecBufferBlocks(this, beginIndex, endIndex);
    }
}
