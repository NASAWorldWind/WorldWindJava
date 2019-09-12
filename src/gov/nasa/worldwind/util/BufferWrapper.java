/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;

import com.jogamp.opengl.*;
import java.nio.*;

/**
 * BufferWrapper provides an interface for reading and writing primitive data to and from data buffers, without having
 * to know the underlying data type. BufferWrapper may be backed by a primitive data buffer of any type.
 *
 * @author tag
 * @version $Id: BufferWrapper.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class BufferWrapper
{
    /**
     * Returns the length of the buffer, in units of the underlying data type (e.g. bytes, shorts, ints, floats,
     * doubles).
     *
     * @return the buffer's length.
     */
    public abstract int length();

    /**
     * Returns the OpenGL data type corresponding to the buffer's underlying data type (e.g. GL_BYTE, GL_SHORT, GL_INT,
     * GL_FLOAT, GL_DOUBLE).
     *
     * @return the buffer's OpenGL data type.
     */
    public abstract int getGLDataType();

    /**
     * Returns the size of this buffer, in bytes.
     *
     * @return the buffer's size in bytes.
     */
    public abstract long getSizeInBytes();

    /**
     * Returns the value at the specified index, cast to a byte.
     *
     * @param index the index of the value to be returned.
     *
     * @return the byte at the specified index.
     */
    public abstract byte getByte(int index);

    /**
     * Sets the value at the specified index as a byte. The byte is cast to the underlying data type.
     *
     * @param index the index of the value to be returned.
     * @param value the byte value to be set.
     */
    public abstract void putByte(int index, byte value);

    /**
     * Returns the value at the specified index, cast to a short.
     *
     * @param index the index of the value to be returned.
     *
     * @return the short at the specified index.
     */
    public abstract short getShort(int index);

    /**
     * Sets the value at the specified index as a short. The short is cast to the underlying data type.
     *
     * @param index the index of the value to be returned.
     * @param value the short value to be set.
     */
    public abstract void putShort(int index, short value);

    /**
     * Returns the value at the specified index, cast to an int.
     *
     * @param index the index of the value to be returned.
     *
     * @return the int at the specified index.
     */
    public abstract int getInt(int index);

    /**
     * Sets the value at the specified index as an int. The int is cast to the underlying data type.
     *
     * @param index the index of the value to be returned.
     * @param value the int value to be set.
     */
    public abstract void putInt(int index, int value);

    /**
     * Returns the value at the specified index, cast to a float.
     *
     * @param index the index of the value to be returned.
     *
     * @return the float at the specified index.
     */
    public abstract float getFloat(int index);

    /**
     * Sets the value at the specified index as a float. The float is cast to the underlying data type.
     *
     * @param index the index of the value to be returned.
     * @param value the float value to be set.
     */
    public abstract void putFloat(int index, float value);

    /**
     * Returns the value at the specified index, cast to a double.
     *
     * @param index the index of the value to be returned.
     *
     * @return the double at the specified index.
     */
    public abstract double getDouble(int index);

    /**
     * Sets the value at the specified index as a double. The double is cast to the underlying data type.
     *
     * @param index the index of the value to be returned.
     * @param value the double value to be set.
     */
    public abstract void putDouble(int index, double value);

    /**
     * Returns the sequence of values starting at the specified index and with the specified length, cast to bytes.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to get.
     */
    public abstract void getByte(int index, byte[] array, int offset, int length);

    /**
     * Sets the sequence of values starting at the specified index and with the specified length, as bytes. The bytes
     * are cast to the underlying data type.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to put.
     */
    public abstract void putByte(int index, byte[] array, int offset, int length);

    /**
     * Returns the sequence of values starting at the specified index and with the specified length, cast to shorts.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to get.
     */
    public abstract void getShort(int index, short[] array, int offset, int length);

    /**
     * Sets the sequence of values starting at the specified index and with the specified length, as ints. The ints are
     * cast to the underlying data type.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to put.
     */
    public abstract void putShort(int index, short[] array, int offset, int length);

    /**
     * Returns the sequence of values starting at the specified index and with the specified length, cast to ints.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to get.
     */
    public abstract void getInt(int index, int[] array, int offset, int length);

    /**
     * Sets the sequence of values starting at the specified index and with the specified length, as ints. The ints are
     * cast to the underlying data type.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to put.
     */
    public abstract void putInt(int index, int[] array, int offset, int length);

    /**
     * Returns the sequence of values starting at the specified index and with the specified length, cast to floats.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to get.
     */
    public abstract void getFloat(int index, float[] array, int offset, int length);

    /**
     * Sets the sequence of values starting at the specified index and with the specified length, as floats. The floats
     * are cast to the underlying data type.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to put.
     */
    public abstract void putFloat(int index, float[] array, int offset, int length);

    /**
     * Returns the sequence of values starting at the specified index and with the specified length, cast to doubles.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to get.
     */
    public abstract void getDouble(int index, double[] array, int offset, int length);

    /**
     * Sets the sequence of values starting at the specified index and with the specified length, as doubles. The
     * doubles are cast to the underlying data type.
     *
     * @param index  the buffer starting index.
     * @param array  the array.
     * @param offset the array starting index.
     * @param length the number of values to put.
     */
    public abstract void putDouble(int index, double[] array, int offset, int length);

    /**
     * Returns a new BufferWrapper which is a subsequence of this buffer. The new buffer starts with the value at the
     * specified index, and has the specified length. The two buffers share the same backing store, so changes to this
     * buffer are reflected in the new buffer, and vice versa.
     *
     * @param index  the new buffer's starting index.
     * @param length the new buffer's length.
     *
     * @return a subsequence of this buffer.
     */
    public abstract BufferWrapper getSubBuffer(int index, int length);

    /**
     * Sets a subsequence of this buffer with the contents of the specified buffer. The subsequence to set starts with
     * the value at the specified index, and has length equal to the specified buffer's length.
     *
     * @param index  the starting index to set.
     * @param buffer the buffer.
     */
    public abstract void putSubBuffer(int index, BufferWrapper buffer);

    /**
     * Sets a subsequence of this buffer with the contents of the specified buffer. The subsequence to set starts with
     * the value at the specified index, and has length equal to the specified length.
     *
     * @param index  the starting index to set.
     * @param buffer the buffer.
     * @param offset the starting index to get from the buffer.
     * @param length the number of values to get from the buffer.
     */
    public abstract void putSubBuffer(int index, BufferWrapper buffer, int offset, int length);

    /**
     * Returns a copy of this buffer with the specified new size. The new size must be greater than or equal to this
     * buffer's size. If the new size is greater than this buffer's size, this returns a new buffer which is partially
     * filled with the contents of this buffer. The returned buffer has the same backing buffer type, but its contents
     * are independent from this VecBuffer.
     *
     * @param newSize the new buffer's size.
     *
     * @return the new buffer, with the specified size.
     */
    public abstract BufferWrapper copyOf(int newSize);

    /**
     * Returns the buffer's backing data sture. For the standard BufferWrapper types (ByteBufferWrapper,
     * ShortBufferWrapper, IntBufferWrapper, FloatBufferWrapper, and DoubleBufferWrapper), this returns the backing
     * {@link Buffer}.
     *
     * @return the backing data store.
     */
    public abstract Buffer getBackingBuffer();

    //**************************************************************//
    //********************  Static Utilities  **********************//
    //**************************************************************//

    /**
     * Returns the empty BufferWrapper. The returned BufferWrapper is immutable and has no backing Buffer.
     *
     * @return the empty BufferWrapper.
     */
    public static BufferWrapper emptyBufferWrapper()
    {
        return EMPTY_BUFFER_WRAPPER;
    }

    /**
     * Wraps the specified {@link ByteBuffer} with a BufferWrapper according to the specified primitive dataType and
     * byteOrder. The dataType describes the primitive data type stored in the ByteBuffer: shorts, ints, floats, or
     * doubles. The byteOrder describes the ByteBuffer's byte ordering. A null byteOrder indicates that the ByteBuffer's
     * current byte ordering should be used.
     *
     * @param byteBuffer the buffer to wrap.
     * @param dataType   the primitive data type stored in the ByteBuffer.
     * @param byteOrder  the primitive byte ordering of the ByteBuffer, or null to use the ByteBuffer's current
     *                   ordering.
     *
     * @return a new BufferWrapper backed by the specified byteBuffer.
     *
     * @throws IllegalArgumentException if either the byteBuffer or the data type are null.
     */
    public static BufferWrapper wrap(ByteBuffer byteBuffer, Object dataType, Object byteOrder)
    {
        if (byteBuffer == null)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dataType == null)
        {
            String message = Logging.getMessage("nullValue.DataTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (byteOrder != null)
        {
            byteBuffer.order(AVKey.LITTLE_ENDIAN.equals(byteOrder) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        }

        if (AVKey.INT8.equals(dataType))
            return new ByteBufferWrapper(byteBuffer.slice());
        else if (AVKey.INT16.equals(dataType))
            return new ShortBufferWrapper(byteBuffer.asShortBuffer());
        else if (AVKey.INT32.equals(dataType))
            return new IntBufferWrapper(byteBuffer.asIntBuffer());
        else if (AVKey.FLOAT32.equals(dataType))
            return new FloatBufferWrapper(byteBuffer.asFloatBuffer());
        else if (AVKey.FLOAT64.equals(dataType))
            return new DoubleBufferWrapper(byteBuffer.asDoubleBuffer());

        return null;
    }

    /**
     * Wraps the specified {@link ByteBuffer} with a BufferWrapper according to the specified primitive dataType. The
     * dataType describes the primitive data type stored in the ByteBuffer: shorts, ints, floats, or doubles. This
     * assumes the ByteBuffer's current byte ordering.
     *
     * @param byteBuffer the buffer to wrap.
     * @param dataType   the primitive data type stored in the ByteBuffer.
     *
     * @return a new BufferWrapper backed by the specified byteBuffer.
     *
     * @throws IllegalArgumentException if either the byteBuffer or the data type are null.
     */
    public static BufferWrapper wrap(ByteBuffer byteBuffer, Object dataType)
    {
        if (byteBuffer == null)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dataType == null)
        {
            String message = Logging.getMessage("nullValue.DataTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return wrap(byteBuffer, dataType, null);
    }

    /**
     * Wraps the specified {@link ByteBuffer} with a BufferWrapper according to the specified parameters. The {@link
     * AVKey#DATA_TYPE} parameter is required, and describes the primitive data type stored in the ByteBuffer: shorts,
     * ints, floats, or doubles. The {@link AVKey#BYTE_ORDER} parameter is optional, and describes the ByteBuffer's byte
     * ordering.
     *
     * @param byteBuffer the buffer to wrap.
     * @param params     the parameters which describe how to interpret the buffer.
     *
     * @return a new BufferWrapper backed by the specified byteBuffer.
     *
     * @throws IllegalArgumentException if either the byteBuffer or the parameters are null, or if AVKey.DATA_TYPE
     *                                  parameter is missing.
     */
    public static BufferWrapper wrap(ByteBuffer byteBuffer, AVList params)
    {
        if (byteBuffer == null)
        {
            String message = Logging.getMessage("nullValue.ByteBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params.getValue(AVKey.DATA_TYPE) == null)
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter",
                Logging.getMessage("term.dataType"));
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return wrap(byteBuffer, params.getValue(AVKey.DATA_TYPE), params.getValue(AVKey.BYTE_ORDER));
    }

    //**************************************************************//
    //********************  BufferWrapper Implementations  *********//
    //**************************************************************//

    public abstract static class AbstractBufferWrapper<T extends Buffer> extends BufferWrapper
    {
        protected T buffer;

        public AbstractBufferWrapper(T buffer)
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.BufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.buffer = buffer;
        }

        public int length()
        {
            return this.buffer.remaining();
        }

        public void getByte(int index, byte[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doGetByte(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void putByte(int index, byte[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doPutByte(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void getShort(int index, short[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doGetShort(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void putShort(int index, short[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doPutShort(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void getInt(int index, int[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doGetInt(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void putInt(int index, int[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doPutInt(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void getFloat(int index, float[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doGetFloat(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void putFloat(int index, float[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doPutFloat(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void getDouble(int index, double[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doGetDouble(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public void putDouble(int index, double[] array, int offset, int length)
        {
            if (array == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            int pos = this.buffer.position(); // Save the buffer's current position.
            try
            {
                this.buffer.position(index);
                this.doPutDouble(array, offset, length);
            }
            finally
            {
                this.buffer.position(pos);  // Restore the buffer's previous position.
            }
        }

        public BufferWrapper getSubBuffer(int index, int length)
        {
            if (length <= 0)
            {
                return EMPTY_BUFFER_WRAPPER;
            }

            BufferWrapper subBuffer = null;

            // Save the buffer's current limit and position.
            int lim = this.buffer.limit();
            int pos = this.buffer.position();
            try
            {
                this.buffer.limit(index + length);
                this.buffer.position(index);
                subBuffer = this.doGetSubBuffer();
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

        public void putSubBuffer(int index, BufferWrapper buffer)
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.BufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.putSubBuffer(index, buffer, 0, buffer.length());
        }

        public void putSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            if (buffer == null)
            {
                String message = Logging.getMessage("nullValue.BufferIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (buffer.getBackingBuffer() == this.buffer)
            {
                String message = Logging.getMessage("generic.CannotCopyBufferToSelf");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (length <= 0)
                return;

            // Attempt to put the specified buffer's contents directly into this buffer. This returns false if the
            // specified buffer's primitive type is not equivalent to this buffer's primitive type.
            if (this.doPutSubBuffer(index, buffer, offset, length))
                return;

            // The specified buffer's primitive type differs from this buffer's type. Use an intermediate double array
            // to put the sub-buffer content.
            double[] array = new double[length];
            buffer.getDouble(offset, array, 0, length);
            this.putDouble(index, array, 0, length);
        }

        public Buffer getBackingBuffer()
        {
            return this.buffer;
        }

        protected abstract void doGetByte(byte[] array, int offset, int length);

        protected abstract void doPutByte(byte[] array, int offset, int length);

        protected abstract void doGetShort(short[] array, int offset, int length);

        protected abstract void doPutShort(short[] array, int offset, int length);

        protected abstract void doGetInt(int[] array, int offset, int length);

        protected abstract void doPutInt(int[] array, int offset, int length);

        protected abstract void doGetFloat(float[] array, int offset, int length);

        protected abstract void doPutFloat(float[] array, int offset, int length);

        protected abstract void doGetDouble(double[] array, int offset, int length);

        protected abstract void doPutDouble(double[] array, int offset, int length);

        protected abstract BufferWrapper doGetSubBuffer();

        protected abstract boolean doPutSubBuffer(int index, BufferWrapper buffer, int offset, int length);
    }

    public static class ByteBufferWrapper extends BufferWrapper.AbstractBufferWrapper<ByteBuffer>
    {
        public ByteBufferWrapper(ByteBuffer buffer)
        {
            super(buffer);
        }

        public ByteBuffer getBackingByteBuffer()
        {
            return this.buffer;
        }

        public int getGLDataType()
        {
            return GL.GL_BYTE;
        }

        public long getSizeInBytes()
        {
            return this.buffer.capacity();
        }

        public byte getByte(int index)
        {
            return this.buffer.get(index);
        }

        public void putByte(int index, byte value)
        {
            this.buffer.put(index, value);
        }

        public short getShort(int index)
        {
            return this.buffer.get(index);
        }

        public void putShort(int index, short value)
        {
            this.buffer.put(index, (byte) value);
        }

        public int getInt(int index)
        {
            return this.buffer.get(index);
        }

        public void putInt(int index, int value)
        {
            this.buffer.put(index, (byte) value);
        }

        public float getFloat(int index)
        {
            return this.buffer.get(index);
        }

        public void putFloat(int index, float value)
        {
            this.buffer.put(index, (byte) value);
        }

        public double getDouble(int index)
        {
            return this.buffer.get(index);
        }

        public void putDouble(int index, double value)
        {
            this.buffer.put(index, (byte) value);
        }

        public BufferWrapper copyOf(int newSize)
        {
            if (newSize < this.length())
            {
                String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            ByteBuffer thatBuffer = WWBufferUtil.copyOf(this.buffer, newSize);
            return new ByteBufferWrapper(thatBuffer);
        }

        protected void doGetByte(byte[] array, int offset, int length)
        {
            this.buffer.get(array, offset, length);
        }

        protected void doPutByte(byte[] array, int offset, int length)
        {
            this.buffer.put(array, offset, length);
        }

        protected void doGetShort(short[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutShort(short[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (byte) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetInt(int[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutInt(int[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (byte) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetFloat(float[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutFloat(float[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (byte) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetDouble(double[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutDouble(double[] array, int offset, int length)
        {
            byte[] tmp = new byte[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (byte) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected BufferWrapper doGetSubBuffer()
        {
            return new ByteBufferWrapper(this.buffer.slice());
        }

        protected boolean doPutSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            Buffer that = buffer.getBackingBuffer();
            if (that instanceof ByteBuffer)
            {
                // Save this buffer's current position.
                int thisPos = this.buffer.position();
                // Save the input buffer's current limit and position.
                int lim = that.limit();
                int pos = that.position();
                try
                {
                    that.limit(offset + length);
                    that.position(offset);
                    this.buffer.position(index);
                    this.buffer.put((ByteBuffer) that);
                }
                finally
                {
                    // Restore this buffer's previous position.
                    this.buffer.position(thisPos);
                    // Restore the input buffer's previous limit and position. Restore limit first in case the position
                    // is greater than the current limit.
                    that.limit(lim);
                    that.position(pos);
                }
                return true;
            }

            return false;
        }
    }

    public static class ShortBufferWrapper extends AbstractBufferWrapper<ShortBuffer>
    {
        public ShortBufferWrapper(ShortBuffer buffer)
        {
            super(buffer);
        }

        public ShortBuffer getBackingShortBuffer()
        {
            return this.buffer;
        }

        public int getGLDataType()
        {
            return GL.GL_SHORT;
        }

        public long getSizeInBytes()
        {
            return WWBufferUtil.SIZEOF_SHORT * this.buffer.capacity();
        }

        public byte getByte(int index)
        {
            return (byte) this.buffer.get(index);
        }

        public void putByte(int index, byte value)
        {
            this.buffer.put(index, value);
        }

        public short getShort(int index)
        {
            return this.buffer.get(index);
        }

        public void putShort(int index, short value)
        {
            this.buffer.put(index, value);
        }

        public int getInt(int index)
        {
            return this.buffer.get(index);
        }

        public void putInt(int index, int value)
        {
            this.buffer.put(index, (short) value);
        }

        public float getFloat(int index)
        {
            return this.buffer.get(index);
        }

        public void putFloat(int index, float value)
        {
            this.buffer.put(index, (short) value);
        }

        public double getDouble(int index)
        {
            return this.buffer.get(index);
        }

        public void putDouble(int index, double value)
        {
            this.buffer.put(index, (short) value);
        }

        public BufferWrapper copyOf(int newSize)
        {
            if (newSize < this.length())
            {
                String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            ShortBuffer thatBuffer = WWBufferUtil.copyOf(this.buffer, newSize);
            return new ShortBufferWrapper(thatBuffer);
        }

        protected void doGetByte(byte[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (byte) tmp[i];
            }
        }

        protected void doPutByte(byte[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetShort(short[] array, int offset, int length)
        {
            this.buffer.get(array, offset, length);
        }

        protected void doPutShort(short[] array, int offset, int length)
        {
            this.buffer.put(array, offset, length);
        }

        protected void doGetInt(int[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutInt(int[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (short) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetFloat(float[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutFloat(float[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (short) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetDouble(double[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutDouble(double[] array, int offset, int length)
        {
            short[] tmp = new short[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (short) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected BufferWrapper doGetSubBuffer()
        {
            return new ShortBufferWrapper(this.buffer.slice());
        }

        protected boolean doPutSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            Buffer that = buffer.getBackingBuffer();
            if (that instanceof ShortBuffer)
            {
                // Save this buffer's current position.
                int thisPos = this.buffer.position();
                // Save the input buffer's current limit and position.
                int lim = that.limit();
                int pos = that.position();
                try
                {
                    that.limit(offset + length);
                    that.position(offset);
                    this.buffer.position(index);
                    this.buffer.put((ShortBuffer) that);
                }
                finally
                {
                    // Restore this buffer's previous position.
                    this.buffer.position(thisPos);
                    // Restore the input buffer's previous limit and position. Restore limit first in case the position
                    // is greater than the current limit.
                    that.limit(lim);
                    that.position(pos);
                }
                return true;
            }

            return false;
        }
    }

    public static class IntBufferWrapper extends AbstractBufferWrapper<IntBuffer>
    {
        public IntBufferWrapper(IntBuffer buffer)
        {
            super(buffer);
        }

        public IntBuffer getBackingIntBuffer()
        {
            return this.buffer;
        }

        public int getGLDataType()
        {
            return GL2.GL_INT;
        }

        public long getSizeInBytes()
        {
            return WWBufferUtil.SIZEOF_INT * this.buffer.capacity();
        }

        public byte getByte(int index)
        {
            return (byte) this.buffer.get(index);
        }

        public void putByte(int index, byte value)
        {
            this.buffer.put(index, value);
        }

        public short getShort(int index)
        {
            return (short) this.buffer.get(index);
        }

        public void putShort(int index, short value)
        {
            this.buffer.put(index, value);
        }

        public int getInt(int index)
        {
            return this.buffer.get(index);
        }

        public void putInt(int index, int value)
        {
            this.buffer.put(index, value);
        }

        public float getFloat(int index)
        {
            return this.buffer.get(index);
        }

        public void putFloat(int index, float value)
        {
            this.buffer.put(index, (int) value);
        }

        public double getDouble(int index)
        {
            return this.buffer.get(index);
        }

        public void putDouble(int index, double value)
        {
            this.buffer.put(index, (int) value);
        }

        public BufferWrapper copyOf(int newSize)
        {
            if (newSize < this.length())
            {
                String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            IntBuffer thatBuffer = WWBufferUtil.copyOf(this.buffer, newSize);
            return new IntBufferWrapper(thatBuffer);
        }

        protected void doGetByte(byte[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (byte) tmp[i];
            }
        }

        protected void doPutByte(byte[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetShort(short[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (short) tmp[i];
            }
        }

        protected void doPutShort(short[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetInt(int[] array, int offset, int length)
        {
            this.buffer.get(array, offset, length);
        }

        protected void doPutInt(int[] array, int offset, int length)
        {
            this.buffer.put(array, offset, length);
        }

        protected void doGetFloat(float[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutFloat(float[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (int) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetDouble(double[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutDouble(double[] array, int offset, int length)
        {
            int[] tmp = new int[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (int) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected BufferWrapper doGetSubBuffer()
        {
            return new IntBufferWrapper(this.buffer.slice());
        }

        protected boolean doPutSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            Buffer that = buffer.getBackingBuffer();
            if (that instanceof IntBuffer)
            {
                // Save this buffer's current position.
                int thisPos = this.buffer.position();
                // Save the input buffer's current limit and position.
                int lim = that.limit();
                int pos = that.position();
                try
                {
                    that.limit(offset + length);
                    that.position(offset);
                    this.buffer.position(index);
                    this.buffer.put((IntBuffer) that);
                }
                finally
                {
                    // Restore this buffer's previous position.
                    this.buffer.position(thisPos);
                    // Restore the input buffer's previous limit and position. Restore limit first in case the position
                    // is greater than the current limit.
                    that.limit(lim);
                    that.position(pos);
                }
                return true;
            }

            return false;
        }
    }

    public static class FloatBufferWrapper extends AbstractBufferWrapper<FloatBuffer>
    {
        public FloatBufferWrapper(FloatBuffer buffer)
        {
            super(buffer);
        }

        public FloatBuffer getBackingFloatBuffer()
        {
            return this.buffer;
        }

        public int getGLDataType()
        {
            return GL.GL_FLOAT;
        }

        public long getSizeInBytes()
        {
            return WWBufferUtil.SIZEOF_FLOAT * this.buffer.capacity();
        }

        public byte getByte(int index)
        {
            return (byte) this.buffer.get(index);
        }

        public void putByte(int index, byte value)
        {
            this.buffer.put(index, value);
        }

        public short getShort(int index)
        {
            return (short) this.buffer.get(index);
        }

        public void putShort(int index, short value)
        {
            this.buffer.put(index, value);
        }

        public int getInt(int index)
        {
            return (int) this.buffer.get(index);
        }

        public void putInt(int index, int value)
        {
            this.buffer.put(index, value);
        }

        public float getFloat(int index)
        {
            return this.buffer.get(index);
        }

        public void putFloat(int index, float value)
        {
            this.buffer.put(index, value);
        }

        public double getDouble(int index)
        {
            return this.buffer.get(index);
        }

        public void putDouble(int index, double value)
        {
            this.buffer.put(index, (float) value);
        }

        public BufferWrapper copyOf(int newSize)
        {
            if (newSize < this.length())
            {
                String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            FloatBuffer thatBuffer = WWBufferUtil.copyOf(this.buffer, newSize);
            return new FloatBufferWrapper(thatBuffer);
        }

        protected void doGetByte(byte[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (byte) tmp[i];
            }
        }

        protected void doPutByte(byte[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetShort(short[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (short) tmp[i];
            }
        }

        protected void doPutShort(short[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetInt(int[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (int) tmp[i];
            }
        }

        protected void doPutInt(int[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (float) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetFloat(float[] array, int offset, int length)
        {
            this.buffer.get(array, offset, length);
        }

        protected void doPutFloat(float[] array, int offset, int length)
        {
            this.buffer.put(array, offset, length);
        }

        protected void doGetDouble(double[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = tmp[i];
            }
        }

        protected void doPutDouble(double[] array, int offset, int length)
        {
            float[] tmp = new float[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = (float) array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected BufferWrapper doGetSubBuffer()
        {
            return new FloatBufferWrapper(this.buffer.slice());
        }

        protected boolean doPutSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            Buffer that = buffer.getBackingBuffer();
            if (that instanceof FloatBuffer)
            {
                // Save this buffer's current position.
                int thisPos = this.buffer.position();
                // Save the input buffer's current limit and position.
                int lim = that.limit();
                int pos = that.position();
                try
                {
                    that.limit(offset + length);
                    that.position(offset);
                    this.buffer.position(index);
                    this.buffer.put((FloatBuffer) that);
                }
                finally
                {
                    // Restore this buffer's previous position.
                    this.buffer.position(thisPos);
                    // Restore the input buffer's previous limit and position. Restore limit first in case the position
                    // is greater than the current limit.
                    that.limit(lim);
                    that.position(pos);
                }
                return true;
            }

            return false;
        }
    }

    public static class DoubleBufferWrapper extends AbstractBufferWrapper<DoubleBuffer>
    {
        public DoubleBufferWrapper(DoubleBuffer buffer)
        {
            super(buffer);
        }

        public DoubleBuffer getBackingDoubleBuffer()
        {
            return this.buffer;
        }

        public int getGLDataType()
        {
            return GL2.GL_DOUBLE;
        }

        public long getSizeInBytes()
        {
            return WWBufferUtil.SIZEOF_DOUBLE * this.buffer.capacity();
        }

        public byte getByte(int index)
        {
            return (byte) this.buffer.get(index);
        }

        public void putByte(int index, byte value)
        {
            this.buffer.put(index, value);
        }

        public short getShort(int index)
        {
            return (short) this.buffer.get(index);
        }

        public void putShort(int index, short value)
        {
            this.buffer.put(index, value);
        }

        public int getInt(int index)
        {
            return (int) this.buffer.get(index);
        }

        public void putInt(int index, int value)
        {
            this.buffer.put(index, value);
        }

        public float getFloat(int index)
        {
            return (float) this.buffer.get(index);
        }

        public void putFloat(int index, float value)
        {
            this.buffer.put(index, value);
        }

        public double getDouble(int index)
        {
            return this.buffer.get(index);
        }

        public void putDouble(int index, double value)
        {
            this.buffer.put(index, value);
        }

        public BufferWrapper copyOf(int newSize)
        {
            if (newSize < this.length())
            {
                String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            DoubleBuffer thatBuffer = WWBufferUtil.copyOf(this.buffer, newSize);
            return new DoubleBufferWrapper(thatBuffer);
        }

        protected void doGetByte(byte[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (byte) tmp[i];
            }
        }

        protected void doPutByte(byte[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetShort(short[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (short) tmp[i];
            }
        }

        protected void doPutShort(short[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetInt(int[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (int) tmp[i];
            }
        }

        protected void doPutInt(int[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetFloat(float[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            this.buffer.get(tmp, 0, length);

            for (int i = 0; i < length; i++)
            {
                array[i + offset] = (float) tmp[i];
            }
        }

        protected void doPutFloat(float[] array, int offset, int length)
        {
            double[] tmp = new double[length];
            for (int i = 0; i < length; i++)
            {
                tmp[i] = array[i + offset];
            }

            this.buffer.put(tmp, 0, length);
        }

        protected void doGetDouble(double[] array, int offset, int length)
        {
            this.buffer.get(array, offset, length);
        }

        protected void doPutDouble(double[] array, int offset, int length)
        {
            this.buffer.put(array, offset, length);
        }

        protected BufferWrapper doGetSubBuffer()
        {
            return new DoubleBufferWrapper(this.buffer.slice());
        }

        protected boolean doPutSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            Buffer that = buffer.getBackingBuffer();
            if (that instanceof DoubleBuffer)
            {
                // Save this buffer's current position.
                int thisPos = this.buffer.position();
                // Save the input buffer's current limit and position.
                int lim = that.limit();
                int pos = that.position();
                try
                {
                    that.limit(offset + length);
                    that.position(offset);
                    this.buffer.position(index);
                    this.buffer.put((DoubleBuffer) that);
                }
                finally
                {
                    // Restore this buffer's previous position.
                    this.buffer.position(thisPos);
                    // Restore the input buffer's previous limit and position. Restore limit first in case the position
                    // is greater than the current limit.
                    that.limit(lim);
                    that.position(pos);
                }
                return true;
            }

            return false;
        }
    }

    //**************************************************************//
    //********************  Empty BufferWrapper  *******************//
    //**************************************************************//

    protected static final BufferWrapper EMPTY_BUFFER_WRAPPER = new EmptyBufferWrapper();

    protected static class EmptyBufferWrapper extends BufferWrapper
    {
        public int length()
        {
            return 0;
        }

        public int getGLDataType()
        {
            return 0;
        }

        public long getSizeInBytes()
        {
            return 0;
        }

        public byte getByte(int index)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putByte(int index, byte value)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public short getShort(int index)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putShort(int index, short value)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public int getInt(int index)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putInt(int index, int value)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public float getFloat(int index)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putFloat(int index, float value)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public double getDouble(int index)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putDouble(int index, double value)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void getByte(int index, byte[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putByte(int index, byte[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void getShort(int index, short[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putShort(int index, short[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void getInt(int index, int[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putInt(int index, int[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void getFloat(int index, float[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putFloat(int index, float[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void getDouble(int index, double[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putDouble(int index, double[] array, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public BufferWrapper getSubBuffer(int index, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putSubBuffer(int index, BufferWrapper buffer)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public void putSubBuffer(int index, BufferWrapper buffer, int offset, int length)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        public BufferWrapper copyOf(int newSize)
        {
            return new EmptyBufferWrapper();
        }

        public Buffer getBackingBuffer()
        {
            return null;
        }
    }
}
