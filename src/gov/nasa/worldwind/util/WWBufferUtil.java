/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;

import java.nio.*;

/**
 * A collection of useful {@link Buffer} methods, all static.
 *
 * @author dcollins
 * @version $Id: WWBufferUtil.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWBufferUtil
{
    /** The size of a short primitive type, in bytes. */
    public static final int SIZEOF_SHORT = 2;
    /** The size of a int primitive type, in bytes. */
    public static final int SIZEOF_INT = 4;
    /** The size of a float primitive type, in bytes. */
    public static final int SIZEOF_FLOAT = 4;
    /** The size of a double primitive type, in bytes. */
    public static final int SIZEOF_DOUBLE = 8;
    /** The size of a char primitive type, in bytes. */
    public static final int SIZEOF_CHAR = 2;

    /**
     * Allocates a new direct {@link java.nio.ByteBuffer} of the specified size, in chars.
     *
     * @param size           the new ByteBuffer's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new buffer.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static ByteBuffer newByteBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return allocateDirect ? newDirectByteBuffer(size) : ByteBuffer.allocate(size);
    }

    /**
     * Allocates a new direct {@link java.nio.ShortBuffer} of the specified size, in chars.
     *
     * @param size           the new ShortBuffer's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new buffer.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static ShortBuffer newShortBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return allocateDirect ? newDirectByteBuffer(SIZEOF_SHORT * size).asShortBuffer() : ShortBuffer.allocate(size);
    }

    /**
     * Allocates a new direct {@link java.nio.IntBuffer} of the specified size, in chars.
     *
     * @param size           the new IntBuffer's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new buffer.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static IntBuffer newIntBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return allocateDirect ? newDirectByteBuffer(SIZEOF_INT * size).asIntBuffer() : IntBuffer.allocate(size);
    }

    /**
     * Allocates a new direct {@link java.nio.FloatBuffer} of the specified size, in chars.
     *
     * @param size           the new FloatBuffer's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new buffer.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static FloatBuffer newFloatBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return allocateDirect ? newDirectByteBuffer(SIZEOF_FLOAT * size).asFloatBuffer() : FloatBuffer.allocate(size);
    }

    /**
     * Allocates a new direct {@link java.nio.DoubleBuffer} of the specified size, in chars.
     *
     * @param size           the new DoubleBuffer's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new buffer.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static DoubleBuffer newDoubleBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return allocateDirect ? newDirectByteBuffer(SIZEOF_DOUBLE * size).asDoubleBuffer()
            : DoubleBuffer.allocate(size);
    }

    /**
     * Allocates a new direct {@link java.nio.CharBuffer} of the specified size, in chars.
     *
     * @param size           the new CharBuffer's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new buffer.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static CharBuffer newCharBuffer(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (allocateDirect ? newDirectByteBuffer(SIZEOF_CHAR * size).asCharBuffer() : CharBuffer.allocate(size));
    }

    /**
     * Allocates a new {@link BufferWrapper} of the specified size, in bytes. The BufferWrapper is backed by a Buffer of
     * bytes.
     *
     * @param size           the new BufferWrapper's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new BufferWrapper.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static BufferWrapper newByteBufferWrapper(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ByteBuffer buffer = newByteBuffer(size, allocateDirect);
        return new BufferWrapper.ByteBufferWrapper(buffer);
    }

    /**
     * Allocates a new {@link BufferWrapper} of the specified size, in shorts. The BufferWrapper is backed by a Buffer
     * of shorts.
     *
     * @param size           the new BufferWrapper's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new BufferWrapper.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static BufferWrapper newShortBufferWrapper(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ShortBuffer buffer = newShortBuffer(size, allocateDirect);
        return new BufferWrapper.ShortBufferWrapper(buffer);
    }

    /**
     * Allocates a new {@link BufferWrapper} of the specified size, in ints. The BufferWrapper is backed by a Buffer of
     * ints.
     *
     * @param size           the new BufferWrapper's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new BufferWrapper.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static BufferWrapper newIntBufferWrapper(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IntBuffer buffer = newIntBuffer(size, allocateDirect);
        return new BufferWrapper.IntBufferWrapper(buffer);
    }

    /**
     * Allocates a new {@link BufferWrapper} of the specified size, in floats. The BufferWrapper is backed by a Buffer
     * of floats.
     *
     * @param size           the new BufferWrapper's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new BufferWrapper.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static BufferWrapper newFloatBufferWrapper(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FloatBuffer buffer = newFloatBuffer(size, allocateDirect);
        return new BufferWrapper.FloatBufferWrapper(buffer);
    }

    /**
     * Allocates a new {@link BufferWrapper} of the specified size, in doubles. The BufferWrapper is backed by a Buffer
     * of doubles.
     *
     * @param size           the new BufferWrapper's size.
     * @param allocateDirect true to allocate and return a direct buffer, false to allocate and return a non-direct
     *                       buffer.
     *
     * @return the new BufferWrapper.
     *
     * @throws IllegalArgumentException if size is negative.
     */
    public static BufferWrapper newDoubleBufferWrapper(int size, boolean allocateDirect)
    {
        if (size < 0)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        DoubleBuffer buffer = newDoubleBuffer(size, allocateDirect);
        return new BufferWrapper.DoubleBufferWrapper(buffer);
    }

    /**
     * Returns a copy of the specified buffer, with the specified new size. The new size must be greater than or equal
     * to the specified buffer's size. If the new size is greater than the specified buffer's size, this returns a new
     * buffer which is partially filled with the contents of the specified buffer.The returned buffer is a direct
     * ByteBuffer if and only if the specified buffer is direct.
     *
     * @param buffer  the buffer to copy.
     * @param newSize the new buffer's size, in bytes.
     *
     * @return the new buffer, with the specified size.
     *
     * @throws IllegalArgumentException if the buffer is null, if the new size is negative, or if the new size is less
     *                                  than the buffer's remaing elements.
     */
    public static ByteBuffer copyOf(ByteBuffer buffer, int newSize)
    {
        if (newSize < 0 || newSize < buffer.remaining())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ByteBuffer newBuffer = newByteBuffer(newSize, buffer.isDirect());

        int pos = buffer.position(); // Save the input buffer's current position.
        try
        {
            newBuffer.put(buffer);
            newBuffer.rewind();
        }
        finally
        {
            buffer.position(pos); // Restore the input buffer's original position.
        }

        return newBuffer;
    }

    /**
     * Returns a copy of the specified buffer, with the specified new size. The new size must be greater than or equal
     * to the specified buffer's size. If the new size is greater than the specified buffer's size, this returns a new
     * buffer which is partially filled with the contents of the specified buffer. The returned buffer is a backed by a
     * direct ByteBuffer if and only if the specified buffer is direct.
     *
     * @param buffer  the buffer to copy.
     * @param newSize the new buffer's size, in chars.
     *
     * @return the new buffer, with the specified size.
     *
     * @throws IllegalArgumentException if the buffer is null, if the new size is negative, or if the new size is less
     *                                  than the buffer's remaing elements.
     */
    public static CharBuffer copyOf(CharBuffer buffer, int newSize)
    {
        if (newSize < 0 || newSize < buffer.remaining())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        CharBuffer newBuffer = newCharBuffer(newSize, buffer.isDirect());

        int pos = buffer.position(); // Save the input buffer's current position.
        try
        {
            newBuffer.put(buffer);
            newBuffer.rewind();
        }
        finally
        {
            buffer.position(pos); // Restore the input buffer's original position.
        }

        return newBuffer;
    }

    /**
     * Returns a copy of the specified buffer, with the specified new size. The new size must be greater than or equal
     * to the specified buffer's size. If the new size is greater than the specified buffer's size, this returns a new
     * buffer which is partially filled with the contents of the specified buffer. The returned buffer is a backed by a
     * direct ByteBuffer if and only if the specified buffer is direct.
     *
     * @param buffer  the buffer to copy.
     * @param newSize the new buffer's size, in shorts.
     *
     * @return the new buffer, with the specified size.
     *
     * @throws IllegalArgumentException if the buffer is null, if the new size is negative, or if the new size is less
     *                                  than the buffer's remaing elements.
     */
    public static ShortBuffer copyOf(ShortBuffer buffer, int newSize)
    {
        if (newSize < 0 || newSize < buffer.remaining())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ShortBuffer newBuffer = newShortBuffer(newSize, buffer.isDirect());

        int pos = buffer.position(); // Save the input buffer's current position.
        try
        {
            newBuffer.put(buffer);
            newBuffer.rewind();
        }
        finally
        {
            buffer.position(pos); // Restore the input buffer's original position.
        }

        return newBuffer;
    }

    /**
     * Returns a copy of the specified buffer, with the specified new size. The new size must be greater than or equal
     * to the specified buffer's size. If the new size is greater than the specified buffer's size, this returns a new
     * buffer which is partially filled with the contents of the specified buffer.The returned buffer is a backed by a
     * direct ByteBuffer if and only if the specified buffer is direct.
     *
     * @param buffer  the buffer to copy.
     * @param newSize the new buffer's size, in ints.
     *
     * @return the new buffer, with the specified size.
     *
     * @throws IllegalArgumentException if the buffer is null, if the new size is negative, or if the new size is less
     *                                  than the buffer's remaing elements.
     */
    public static IntBuffer copyOf(IntBuffer buffer, int newSize)
    {
        if (newSize < 0 || newSize < buffer.remaining())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IntBuffer newBuffer = newIntBuffer(newSize, buffer.isDirect());

        int pos = buffer.position(); // Save the input buffer's current position.
        try
        {
            newBuffer.put(buffer);
            newBuffer.rewind();
        }
        finally
        {
            buffer.position(pos); // Restore the input buffer's original position.
        }

        return newBuffer;
    }

    /**
     * Returns a copy of the specified buffer, with the specified new size. The new size must be greater than or equal
     * to the specified buffer's size. If the new size is greater than the specified buffer's size, this returns a new
     * buffer which is partially filled with the contents of the specified buffer. The returned buffer is a backed by a
     * direct ByteBuffer if and only if the specified buffer is direct.
     *
     * @param buffer  the buffer to copy.
     * @param newSize the new buffer's size, in floats.
     *
     * @return the new buffer, with the specified size.
     *
     * @throws IllegalArgumentException if the buffer is null, if the new size is negative, or if the new size is less
     *                                  than the buffer's remaing elements.
     */
    public static FloatBuffer copyOf(FloatBuffer buffer, int newSize)
    {
        if (newSize < 0 || newSize < buffer.remaining())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        FloatBuffer newBuffer = newFloatBuffer(newSize, buffer.isDirect());

        int pos = buffer.position(); // Save the input buffer's current position.
        try
        {
            newBuffer.put(buffer);
            newBuffer.rewind();
        }
        finally
        {
            buffer.position(pos); // Restore the input buffer's original position.
        }

        return newBuffer;
    }

    /**
     * Returns a copy of the specified buffer, with the specified new size. The new size must be greater than or equal
     * to the specified buffer's size. If the new size is greater than the specified buffer's size, this returns a new
     * buffer which is partially filled with the contents of the specified buffer. The returned buffer is a backed by a
     * direct ByteBuffer if and only if the specified buffer is direct.
     *
     * @param buffer  the buffer to copy.
     * @param newSize the new buffer's size, in doubles.
     *
     * @return the new buffer, with the specified size.
     *
     * @throws IllegalArgumentException if the buffer is null, if the new size is negative, or if the new size is less
     *                                  than the buffer's remaing elements.
     */
    public static DoubleBuffer copyOf(DoubleBuffer buffer, int newSize)
    {
        if (newSize < 0 || newSize < buffer.remaining())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        DoubleBuffer newBuffer = newDoubleBuffer(newSize, buffer.isDirect());

        int pos = buffer.position(); // Save the input buffer's current position.
        try
        {
            newBuffer.put(buffer);
            newBuffer.rewind();
        }
        finally
        {
            buffer.position(pos); // Restore the input buffer's original position.
        }

        return newBuffer;
    }

    /**
     * Returns the size in bytes of the specified primitive data type, or -1 if the specified type is unrecognized.
     * Recognized primitive types are as follows: <ul> <li>{@link gov.nasa.worldwind.avlist.AVKey#INT8} <li>{@link
     * gov.nasa.worldwind.avlist.AVKey#INT16} <li>{@link gov.nasa.worldwind.avlist.AVKey#INT32} <li>{@link
     * gov.nasa.worldwind.avlist.AVKey#FLOAT32} <li>{@link gov.nasa.worldwind.avlist.AVKey#FLOAT64} </ul>
     *
     * @param dataType the primitive data type.
     *
     * @return the size of the primitive data type, in bytes.
     *
     * @throws IllegalArgumentException if the data type is null.
     */
    public static int sizeOfPrimitiveType(Object dataType)
    {
        if (dataType == null)
        {
            String message = Logging.getMessage("nullValue.DataTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (AVKey.INT8.equals(dataType))
            return 1;
        else if (AVKey.INT16.equals(dataType))
            return SIZEOF_SHORT;
        else if (AVKey.INT32.equals(dataType))
            return SIZEOF_INT;
        else if (AVKey.FLOAT32.equals(dataType))
            return SIZEOF_FLOAT;
        else if (AVKey.FLOAT64.equals(dataType))
            return SIZEOF_DOUBLE;

        return -1;
    }

    /**
     * Returns the minimum and maximum floating point values in the specified buffer. Values equivalent to the specified
     * <code>missingDataSignal</code> are ignored. This returns null if the buffer is empty or contains only missing
     * values.
     *
     * @param buffer            the buffer to search for the minimum and maximum values.
     * @param missingDataSignal the number indicating a specific floating point value to ignore.
     *
     * @return an array containing the minimum value in index 0 and the maximum value in index 1, or null if the buffer
     *         is empty or contains only missing values.
     *
     * @throws IllegalArgumentException if the buffer is null.
     */
    public static double[] computeExtremeValues(BufferWrapper buffer, double missingDataSignal)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (int i = 0; i < buffer.length(); i++)
        {
            double value = buffer.getDouble(i);

            if (Double.compare(value, missingDataSignal) == 0)
                continue;

            if (min > value)
                min = value;
            if (max < value)
                max = value;
        }

        if (Double.compare(min, Double.MAX_VALUE) == 0 || Double.compare(max, -Double.MAX_VALUE) == 0)
            return null;

        return new double[] {min, max};
    }

    /**
     * Returns the minimum and maximum floating point values in the specified buffer. Values equivalent to
     * <code>Double.NaN</code> are ignored. This returns null if the buffer is empty or contains only NaN values.
     *
     * @param buffer the buffer to search for the minimum and maximum values.
     *
     * @return an array containing the minimum value in index 0 and the maximum value in index 1, or null if the buffer
     *         is empty or contains only NaN values.
     *
     * @throws IllegalArgumentException if the buffer is null.
     */
    public static double[] computeExtremeValues(BufferWrapper buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return computeExtremeValues(buffer, Double.NaN);
    }

    protected static ByteBuffer newDirectByteBuffer(int size)
    {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    /**
     * Copies a specified array of vertices to a specified vertex buffer. This method calls {@link
     * java.nio.FloatBuffer#flip()} prior to returning.
     *
     * @param array  the vertices to copy.
     * @param buffer the buffer to copy the vertices to. Must have enough remaining space to hold the vertices.
     *
     * @return the buffer specified as input, with its limit incremented by the number of vertices copied, and its
     *         position set to 0.
     */
    public static FloatBuffer copyArrayToBuffer(Vec4[] array, FloatBuffer buffer)
    {
        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (Vec4 v : array)
        {
            buffer.put((float) v.x).put((float) v.y).put((float) v.z);
        }

        buffer.flip(); // sets the limit to the position and then the position to 0.

        return buffer;
    }
}
