/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;

/**
 * VecBufferSequence provides storage and retrieval of a sequence of logical VecBuffers in a single VecBuffer that
 * expands when more capacity is needed. VecBuffers added to a VecBufferSequence by calling {@link #append(VecBuffer)}.
 * This copies the specified VecBuffer's data to the VecBuffer backing the VecBufferSequence, and expands the backing
 * VecBuffer if necessary.
 *
 * @author dcollins
 * @version $Id: VecBufferSequence.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VecBufferSequence extends CompoundVecBuffer
{
    protected int vecCount;
    protected VecBuffer buffer;

    /**
     * Constructs a PackedCompoundVecBuffer with the specified backing VecBuffer and the specified initial capacity.
     *
     * @param buffer   the backing VecBuffer.
     * @param capacity the PackedCompoundVecBuffer's initial capacity, in number of sub-buffers.
     *
     * @throws IllegalArgumentException if the buffer is null, or if the capacity is less than 1.
     */
    public VecBufferSequence(VecBuffer buffer, int capacity)
    {
        super(capacity);

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer = buffer;
    }

    /**
     * Constructs a PackedCompoundVecBuffer with the specified backing VecBuffer and the default initial capacity.
     *
     * @param buffer the backing VecBuffer.
     *
     * @throws IllegalArgumentException if the buffer is null.
     */
    public VecBufferSequence(VecBuffer buffer)
    {
        this(buffer, DEFAULT_INITIAL_CAPACITY);
    }

    protected VecBufferSequence(VecBufferSequence that, int beginIndex, int endIndex)
    {
        super(that, beginIndex, endIndex);

        this.vecCount = that.vecCount;
        this.buffer = that.buffer;
    }

    protected VecBufferSequence(VecBufferSequence that, int[] indices, int offset, int length)
    {
        super(that, indices, offset, length);

        this.vecCount = that.vecCount;
        this.buffer = that.buffer;
    }

    /**
     * Returns an empty VecBufferSequence. The returned VecBufferSequence has a size of zero and contains no
     * sub-buffers.
     *
     * @param coordsPerVec the number of coordinates per logical vector.
     *
     * @return the empty VecBufferSequence.
     */
    public static VecBufferSequence emptyVecBufferSequence(int coordsPerVec)
    {
        if (coordsPerVec < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", coordsPerVec);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new VecBufferSequence(VecBuffer.emptyVecBuffer(coordsPerVec));
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

        return this.lengths.get(index);
    }

    /** {@inheritDoc} */
    public void clear()
    {
        super.clear();
        this.vecCount = 0;
    }

    /** {@inheritDoc} */
    public int getCoordsPerVec()
    {
        return this.buffer.getCoordsPerVec();
    }

    /**
     * Returns the VecBuffer that stores this PackedCompoundVecBuffer's sub-buffers.
     *
     * @return this PackedCompoundVecBuffer's backing VecBuffer.
     */
    public VecBuffer getVecBuffer()
    {
        return this.buffer;
    }

    /**
     * Appends the contents of the specified sub-buffer to the end of this PackedCompoundVecBuffer, incrementing the
     * number of sub-buffers by one. The backing buffer grows to accomodate the sub-buffer if it does not already have
     * enough capacity to hold it.
     *
     * @param buffer the sub-buffer to append.
     *
     * @return the sub-buffer's index.
     *
     * @throws IllegalArgumentException if the subBuffer is null.
     */
    public int append(VecBuffer buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int minVecCount = buffer.getSize() + this.vecCount;
        if (minVecCount > this.buffer.getSize())
            this.expandBufferCapacity(minVecCount);

        int newBufferPos = this.vecCount;
        this.buffer.putSubBuffer(newBufferPos, buffer);
        this.vecCount += buffer.getSize();

        return this.addSubBuffer(newBufferPos, buffer.getSize());
    }

    //**************************************************************//
    //********************  Protected Interface  *******************//
    //**************************************************************//

    protected VecBuffer createSubBuffer(int offset, int length)
    {
        return this.buffer.getSubBuffer(offset, length);
    }

    protected CompoundVecBuffer createSlice(int[] indices, int offset, int length)
    {
        return new VecBufferSequence(this, indices, offset, length);
    }

    protected CompoundVecBuffer createSlice(int beginIndex, int endIndex)
    {
        return new VecBufferSequence(this, beginIndex, endIndex);
    }

    protected void expandBufferCapacity(int minCapacity)
    {
        int newCapacity = 2 * this.buffer.getSize();

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

        this.buffer = this.buffer.copyOf(newCapacity);
    }

    //**************************************************************//
    //********************  OpenGL Vertex Buffer Interface  ********//
    //**************************************************************//

    /**
     * Binds this buffer as the source of normal coordinates to use when rendering OpenGL primitives. The normal type is
     * equal to buffer's underlying BufferWrapper GL type, the stride is 0, and the vertex data itself is this buffer's
     * backing NIO {@link java.nio.Buffer}. This buffer's vector size must be 3.
     *
     * @param dc the current {@link gov.nasa.worldwind.render.DrawContext}.
     *
     * @throws IllegalArgumentException if the DrawContext is null, or if this buffer is not compatible as a normal
     *                                  buffer.
     */
    public void bindAsNormalBuffer(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer.bindAsNormalBuffer(dc);
    }

    /**
     * Binds this buffer as the source of vertex coordinates to use when rendering OpenGL primitives. The vertex size is
     * equal to coordsPerVertex, the vertex type is equal to buffer's underlying BufferWrapper GL type, the stride is 0,
     * and the normal data itself is this buffer's backing NIO Buffer. This buffer's vector size must be 2, 3, or 4.
     *
     * @param dc the current DrawContext.
     *
     * @throws IllegalArgumentException if the DrawContext is null, or if this buffer is not compatible as a vertex
     *                                  buffer.
     */
    public void bindAsVertexBuffer(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer.bindAsVertexBuffer(dc);
    }

    /**
     * Binds this buffer as the source of texture coordinates to use when rendering OpenGL primitives.  The texture
     * coordinate size is equal to coordsPerVertex, the texture coordinate type is equal to buffer's underlying
     * BufferWrapper GL type, the stride is 0, and the texture coordinate data itself is this buffer's backing NIO
     * Buffer. This buffer's vector size must be 1, 2, 3, or 4.
     *
     * @param dc the current DrawContext.
     *
     * @throws IllegalArgumentException if the DrawContext is null, or if this buffer is not compatible as a normal
     *                                  buffer.
     */
    public void bindAsTexCoordBuffer(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer.bindAsTexCoordBuffer(dc);
    }

    /**
     * Renders <code>getTotalBufferSize()</code> elements from the currently bounds OpenGL coordinate buffers, beginning
     * with element 0. The specified drawMode indicates which type of OpenGL primitives to render.
     *
     * @param dc       the current DrawContext.
     * @param drawMode the type of OpenGL primtives to render.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public void drawArrays(DrawContext dc, int drawMode)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer.drawArrays(dc, drawMode);
    }

    /**
     * Renders elements from the currently bounds OpenGL coordinate buffers. This behaves exactly like {@link
     * #drawArrays(gov.nasa.worldwind.render.DrawContext, int)}, except that each sub-buffer is rendered independently.
     * The specified drawMode indicates which type of OpenGL primitives to render.
     *
     * @param dc       the current DrawContext.
     * @param drawMode the type of OpenGL primtives to render.
     *
     * @throws IllegalArgumentException if the DrawContext is null.
     */
    public void multiDrawArrays(DrawContext dc, int drawMode)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (this.haveMultiDrawArrays(dc))
        {
            gl.glMultiDrawArrays(drawMode, this.offsets, this.lengths, this.count);
        }
        else
        {
            for (int i = 0; i < this.count; i++)
            {
                gl.glDrawArrays(drawMode, this.offsets.get(i), this.lengths.get(i));
            }
        }
    }

    protected boolean haveMultiDrawArrays(DrawContext dc)
    {
        return dc.getGL().isFunctionAvailable("glMultiDrawArrays");
    }
}
