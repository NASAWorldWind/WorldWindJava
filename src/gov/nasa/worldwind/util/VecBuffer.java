/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;
import java.nio.Buffer;
import java.util.*;

/**
 * VecBuffer provides an logical interface on {@link BufferWrapper} to interpret its contents as a series of vector
 * tuples (rather than individual primitive types). The number of coordinates in each logical vector is specified by the
 * property <code>coordsPerElem</code>. For example, if a VecBuffer is composed of (x, y, z) tuples then coordsPerElem
 * would be 3.
 *
 * @author dcollins
 * @version $Id: VecBuffer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VecBuffer
{
    protected int coordsPerVec;
    protected BufferWrapper buffer;

    /**
     * Constructs a new VecBuffer with the specified vector size, and backing BufferWrapper.
     *
     * @param coordsPerVec the number of coordinates per logical vector.
     * @param buffer       the backing BufferWrapper.
     *
     * @throws IllegalArgumentException if coordsPerElem is 0 or negative, or if the buffer is null.
     */
    public VecBuffer(int coordsPerVec, BufferWrapper buffer)
    {
        if (coordsPerVec < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "coordsPerVec < 1");
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
        this.buffer = buffer;
    }

    /**
     * Returns the empty VecBuffer. The returned VecBuffer has no backing buffer, and is immutable.
     *
     * @param coordsPerVec the number of coordinates per logical vector.
     *
     * @return the empty VecBuffer.
     */
    public static VecBuffer emptyVecBuffer(int coordsPerVec)
    {
        return new VecBuffer(coordsPerVec, BufferWrapper.emptyBufferWrapper());
    }

    /**
     * Returns the number of coordinates per logical vector element.
     *
     * @return the cardinality of a logical vector element.
     */
    public int getCoordsPerVec()
    {
        return this.coordsPerVec;
    }

    /**
     * Returns the number of logical vector elements contained in the VecBuffer.
     *
     * @return the size of this VecBuffer, in units of logical vectors.
     */
    public int getSize()
    {
        return this.buffer.length() / this.coordsPerVec;
    }

    /**
     * Returns the backing BufferWrapper.
     *
     * @return the backing buffer.
     */
    public BufferWrapper getBufferWrapper()
    {
        return this.buffer;
    }

    /**
     * Returns the vector element at the specified position. The position is a logical vector position, position n
     * corresponds to the buffer's nth vector. If the specified array length is smaller than the logical vector size,
     * only the specified portion of the vector element is returned.
     *
     * @param position the logical vector position.
     * @param array    the destination array.
     *
     * @return an array of vector elements.
     *
     * @throws IllegalArgumentException if the position is out of range, or if the array is null.
     */
    public double[] get(int position, double[] array)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int length = array.length;
        if (length > this.coordsPerVec)
            length = this.coordsPerVec;

        this.buffer.getDouble(index, array, 0, length);

        return array;
    }

    /**
     * Returns the vector element at the specified position. The position is a logical vector position, position n
     * corresponds to the buffer's nth vector. If the specified array length is smaller than the logical vector size,
     * only the specified portion of the vector element is returned.
     *
     * @param position the logical vector position.
     * @param array    the destination array.
     *
     * @return an array of vector elements.
     *
     * @throws IllegalArgumentException if the position is out of range, or if the array is null.
     */
    public float[] getFloat(int position, float[] array)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int length = array.length;
        if (length > this.coordsPerVec)
            length = this.coordsPerVec;

        this.buffer.getFloat(index, array, 0, length);

        return array;
    }

    /**
     * Sets the vector element at the specified position. The position is a logical vector position, position n
     * corresponds to the buffer's nth vector. If the specified array length is smaller than the logical vector size,
     * only the specified portion of the vector element is set.
     *
     * @param position the logical vector position.
     * @param array    the source array.
     *
     * @throws IllegalArgumentException if the position is out of range, or if the array is null.
     */
    public void put(int position, double[] array)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int length = array.length;
        if (length > this.coordsPerVec)
            length = this.coordsPerVec;

        this.buffer.putDouble(index, array, 0, length);
    }

    /**
     * Sets the vector element at the specified position. The position is a logical vector position, position n
     * corresponds to the buffer's nth vector. If the specified array length is smaller than the logical vector size,
     * only the specified portion of the vector element is set.
     *
     * @param position the logical vector position.
     * @param array    the source array.
     *
     * @throws IllegalArgumentException if the position is out of range, or if the array is null.
     */
    public void putFloat(int position, float[] array)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int length = array.length;
        if (length > this.coordsPerVec)
            length = this.coordsPerVec;

        this.buffer.putFloat(index, array, 0, length);
    }

    /**
     * Sets the vector elements starting at the specified position, and ending at the specified position + count. The
     * position is a logical vector position, position n corresponds to the buffer's nth vector. The array must have
     * sufficient length to represent count separate logical vectors (each with size equal to coordsPerVec) tightly
     * packed into the array, starting at index 0.
     *
     * @param position the starting logical vector position.
     * @param array    the source array.
     * @param count    the number of logical arrays to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the array is null, or if the array has
     *                                  insufficient length.
     */
    public void putAll(int position, double[] array, int count)
    {
        if (position < 0 || position + count > this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange",
                "position < 0 or position + count >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (array == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int length = this.indexFromVectorPosition(count);

        if (array.length < length)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer.putDouble(index, array, 0, length);
    }

    /**
     * Returns a new VecBuffer which is a subsequence of this buffer. The new buffer starts with the vector at the
     * specified position, and has the specified length. The two buffers share the same backing store, so changes to
     * this buffer are reflected in the new buffer, and visa versa.
     *
     * @param position the new buffer's staring position, in logical vectors.
     * @param size     the new buffer's size, in logical vectors.
     *
     * @return a subsequence of this buffer.
     */
    public VecBuffer getSubBuffer(int position, int size)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int length = this.indexFromVectorPosition(size);
        BufferWrapper subBuffer = this.buffer.getSubBuffer(index, length);

        return new VecBuffer(this.coordsPerVec, subBuffer);
    }

    /**
     * Sets a subsequence of this buffer with the contents of the specified buffer. The subsequence to set starts with
     * the vector at the specified position, and has size equal to the specified buffer's size. The specified buffer
     * must have the same logical vector size as this buffer (coordsPerVec must be equivalent).
     *
     * @param position the starting vector position to set.
     * @param buffer   the input buffer.
     *
     * @throws IllegalArgumentException if the position is out of range, if the buffer is null or incompatible, or if
     *                                  this buffer has insufficient length to store the sub-buffer at the specified
     *                                  position.
     */
    public void putSubBuffer(int position, VecBuffer buffer)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.putSubBuffer(position, buffer, 0, buffer.getSize());
    }

    /**
     * Sets a subsequence of this buffer with the contents of the specified buffer. The subsequence to set starts with
     * the vector at the specified position, and has size equal to the specified size. The specified buffer must have
     * the same logical vector size as this buffer (coordsPerVec must be equivalent).
     *
     * @param position the starting vector position to set.
     * @param buffer   the input buffer.
     * @param offset   the vector position to start copying values from the specified buffer.
     * @param size     the number of vectors to read copy form the specified buffer.
     *
     * @throws IllegalArgumentException if the position is out of range, if the buffer is null or incompatible, if this
     *                                  buffer has insufficient length to store the sub-buffer at the specified
     *                                  position, or if the specified offset and size define a range outside of the
     *                                  specified buffer.
     */
    public void putSubBuffer(int position, VecBuffer buffer, int offset, int size)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Not enough room in buffer.
        if (buffer.getSize() < (offset + size))
        {
            String message = Logging.getMessage("generic.BufferOverflow", buffer.getSize(), size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Buffer is incompatible.
        if (this.coordsPerVec != buffer.coordsPerVec)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", buffer);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Buffer is too large.
        int sizeNeeded = position + size;
        if (this.getSize() < sizeNeeded)
        {
            String message = Logging.getMessage("generic.BufferOverflow", this.getSize(), sizeNeeded);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int index = this.indexFromVectorPosition(position);
        int off = this.indexFromVectorPosition(offset);
        int length = this.indexFromVectorPosition(size);
        this.buffer.putSubBuffer(index, buffer.getBufferWrapper(), off, length);
    }

    /**
     * Returns the vector element at the specified position, as a {@link Vec4}. This buffer's logical vector size must
     * be either 2, 3 or 4.
     *
     * @param position the logical vector position.
     *
     * @return the vector at the specified vector position.
     *
     * @throws IllegalArgumentException if the position is out of range, or if this buffer cannot store a Vec4.
     */
    public Vec4 getVector(int position)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec != 2 && this.coordsPerVec != 3 && this.coordsPerVec != 4)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", this);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] compArray = new double[this.coordsPerVec];
        this.get(position, compArray);
        return Vec4.fromDoubleArray(compArray, 0, this.coordsPerVec);
    }

    /**
     * Sets the vector element at the specified position, as a Vec4. This buffer's logical vector size must be either 2,
     * 3 or 4.
     *
     * @param position the logical vector position.
     * @param vec      the vector to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the vector is null, or if this buffer cannot
     *                                  store a Vec4.
     */
    public void putVector(int position, Vec4 vec)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (vec == null)
        {
            String message = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec != 2 && this.coordsPerVec != 3 && this.coordsPerVec != 4)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", this);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] compArray = new double[this.coordsPerVec];
        vec.toDoubleArray(compArray, 0, this.coordsPerVec);
        this.put(position, compArray);
    }

    /**
     * Returns the vector element at the specified position, as a geographic {@link LatLon}. This buffer's logical
     * vector size must be at least 2.
     *
     * @param position the logical vector position.
     *
     * @return the geographic location at the specified vector position.
     *
     * @throws IllegalArgumentException if the position is out of range, or if this buffer cannot store a LatLon.
     */
    public LatLon getLocation(int position)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec < 2)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", this);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] compArray = new double[2];
        this.get(position, compArray);

        return LatLon.fromDegrees(compArray[1], compArray[0]);
    }

    /**
     * Sets the vector element at the specified position, as a geographic LatLon. This buffer's logical vector size must
     * be at least 2.
     *
     * @param position the logical vector position.
     * @param ll       the geographic location to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the LatLon is null, or if this buffer cannot
     *                                  store a LatLon.
     */
    public void putLocation(int position, LatLon ll)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (ll == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec < 2)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", this);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] compArray = new double[2];
        compArray[1] = ll.getLatitude().degrees;
        compArray[0] = ll.getLongitude().degrees;

        this.put(position, compArray);
    }

    /**
     * Returns the vector element at the specified position, as a geographic {@link Position}. This buffer's logical
     * vector size must be at least 2.
     *
     * @param position the logical vector position.
     *
     * @return the geographic Position at the specified vector position.
     *
     * @throws IllegalArgumentException if the position is out of range, or if this buffer cannot store a Position.
     */
    public Position getPosition(int position)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec < 2)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", this);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] compArray = new double[this.coordsPerVec];
        this.get(position, compArray);

        return Position.fromDegrees(
            compArray[1],
            compArray[0],
            (this.coordsPerVec > 2) ? compArray[2] : 0);
    }

    /**
     * Sets the vector element at the specified position, as a geographic Position. This buffer's logical vector size
     * must be at least 2.
     *
     * @param position the logical vector position.
     * @param p        the geographic Position to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the Position is null, or if this buffer
     *                                  cannot store a Position.
     */
    public void putPosition(int position, Position p)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (p == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec < 2)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", this);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double[] compArray = new double[3];
        compArray[1] = p.getLatitude().degrees;
        compArray[0] = p.getLongitude().degrees;
        compArray[2] = p.getElevation();

        this.put(position, compArray);
    }

    /**
     * Returns a copy of this VecBuffer with the specified new size. The new size must be greater than or equal to this
     * VecBuffer's size. If the new size is greater than this buffer's size, this returns a new buffer which is
     * partially filled with the contents of this buffer. The returned VecBuffer has the same number of coordinates per
     * tuple and the same backing buffer type, but its contents are independent from this VecBuffer.
     *
     * @param newSize the new buffer's size.
     *
     * @return the new buffer, with the specified size.
     */
    public VecBuffer copyOf(int newSize)
    {
        if (newSize < this.getSize())
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", newSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BufferWrapper newBuffer = this.buffer.copyOf(this.coordsPerVec * newSize);
        return new VecBuffer(this.coordsPerVec, newBuffer);
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as double[] coordinate arrays. The array returned from
     * each call to Iterator.next() will be newly allocated, and will have length equal to coordsPerVec.
     *
     * @return iterator over this buffer's vectors, as double[] arrays.
     */
    public Iterable<double[]> getCoords()
    {
        return this.getCoords(this.coordsPerVec);
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as double[] coordinate arrays. The array returned from a
     * call to Iterator.next() will be newly allocated, and will have length equal to coordsPerVec or minCoordsPerVec,
     * whichever is larger. If minCoordsPerVec is larger than coordsPerVec, then the elements in the returned array will
     * after index "coordsPerVec - 1" will be undefined.
     *
     * @param minCoordsPerVec the minimum number of coordinates returned in each double[] array.
     *
     * @return iterator over this buffer's vectors, as double[] arrays.
     */
    public Iterable<double[]> getCoords(final int minCoordsPerVec)
    {
        return new Iterable<double[]>()
        {
            public Iterator<double[]> iterator()
            {
                return new BasicIterator<double[]>(new CoordAccessor(minCoordsPerVec));
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as double[] coordinate arrays. The array returned
     * from a call to Iterator.next() will be newly allocated, and will have length equal to coordsPerVec or
     * minCoordsPerVec, whichever is larger. If minCoordsPerVec is larger than coordsPerVec, then the elements in the
     * returned array will after index "coordsPerVec - 1" will be undefined.
     *
     * @param minCoordsPerVec the minimum number of coordinates returned in each double[] array.
     *
     * @return reverse iterator over this buffer's vectors, as double[] arrays.
     */
    public Iterable<double[]> getReverseCoords(final int minCoordsPerVec)
    {
        return new Iterable<double[]>()
        {
            public Iterator<double[]> iterator()
            {
                return new ReverseIterator<double[]>(new CoordAccessor(minCoordsPerVec));
            }
        };
    }

    /**
     * Sets a subsequence of this buffer with the contents of the specified Iterable. The subsequence to set starts with
     * the vector at the specified position, and has size equal to the number of elements in the Iterable or the number
     * of remaining vectors in the buffer, whichever is less.
     *
     * @param position the starting vector position to set.
     * @param iterable iterator over the elements to set.
     *
     * @throws IllegalArgumentException if the position is out of range, or if the iterable is null.
     */
    public void putCoords(int position, Iterable<double[]> iterable)
    {
        if (position < 0 || position >= this.getSize())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "position < 0 or position >= size");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int pos = position;

        for (double[] coords : iterable)
        {
            this.put(pos, coords);

            if (++pos >= this.getSize())
                break;
        }
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as Vec4 references.
     *
     * @return iterator over this buffer's vectors, as Vec4 references.
     */
    public Iterable<Vec4> getVectors()
    {
        return new Iterable<Vec4>()
        {
            public Iterator<Vec4> iterator()
            {
                return new BasicIterator<Vec4>(new VectorAccessor());
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as Vec4 references.
     *
     * @return reverse iterator over this buffer's vectors, as Vec4 references.
     */
    public Iterable<Vec4> getReverseVectors()
    {
        return new Iterable<Vec4>()
        {
            public Iterator<Vec4> iterator()
            {
                return new ReverseIterator<Vec4>(new VectorAccessor());
            }
        };
    }

    /**
     * Sets a subsequence of this buffer with the contents of the specified Iterable. The subsequence to set starts with
     * the vector at the specified position, and has size equal to the number of elements in the Iterable or the number
     * of remaining vectors in the buffer, whichever is less. This buffer's logical vector size must be either 2, 3 or
     * 4.
     *
     * @param position the starting vector position to set.
     * @param iterable iterator over the elements to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the iterable is null, or if this buffer
     *                                  cannot store a Vec4.
     */
    public void putVectors(int position, Iterable<? extends Vec4> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int pos = position;

        for (Vec4 vec : iterable)
        {
            this.putVector(pos, vec);

            if (++pos >= this.getSize())
                break;
        }
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as LatLon locations.
     *
     * @return iterator over this buffer's vectors, as LatLon locations.
     */
    public Iterable<LatLon> getLocations()
    {
        return new Iterable<LatLon>()
        {
            public Iterator<LatLon> iterator()
            {
                return new BasicIterator<LatLon>(new LocationAccessor());
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as LatLon locations.
     *
     * @return reverse iterator over this buffer's vectors, as LatLon locations.
     */
    public Iterable<LatLon> getReverseLocations()
    {
        return new Iterable<LatLon>()
        {
            public Iterator<LatLon> iterator()
            {
                return new ReverseIterator<LatLon>(new LocationAccessor());
            }
        };
    }

    /**
     * Sets a subsequence of this buffer with the contents of the specified Iterable. The subsequence to set starts with
     * the vector at the specified position, and has size equal to the number of elements in the Iterable or the number
     * of remaining vectors in the buffer, whichever is less. This buffer's logical vector size must be at least 2.
     *
     * @param position the starting vector position to set.
     * @param iterable iterator over the elements to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the iterable is null, or if this buffer
     *                                  cannot store a LatLon.
     */
    public void putLocations(int position, Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int pos = position;

        for (LatLon ll : iterable)
        {
            this.putLocation(pos, ll);

            if (++pos >= this.getSize())
                break;
        }
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as geographic Positions.
     *
     * @return iterator over this buffer's vectors, as geographic Positions.
     */
    public Iterable<Position> getPositions()
    {
        return new Iterable<Position>()
        {
            public Iterator<Position> iterator()
            {
                return new BasicIterator<Position>(new PositionAccessor());
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as geographic Positions.
     *
     * @return reverse iterator over this buffer's vectors, as geographic Positions.
     */
    public Iterable<Position> getReversePositions()
    {
        return new Iterable<Position>()
        {
            public Iterator<Position> iterator()
            {
                return new ReverseIterator<Position>(new PositionAccessor());
            }
        };
    }

    /**
     * Sets a subsequence of this buffer with the contents of the specified Iterable. The subsequence to set starts with
     * the vector at the specified position, and has size equal to the number of elements in the Iterable or the number
     * of remaining vectors in the buffer, whichever is less. This buffer's logical vector size must be at least 2.
     *
     * @param position the starting vector position to set.
     * @param iterable iterator over the elements to set.
     *
     * @throws IllegalArgumentException if the position is out of range, if the iterable is null, or if this buffer
     *                                  cannot store a LatLon.
     */
    public void putPositions(int position, Iterable<? extends Position> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int pos = position;

        for (Position p : iterable)
        {
            this.putPosition(pos, p);

            if (++pos >= this.getSize())
                break;
        }
    }

    /**
     * Binds this buffer as the source of color values to use when rendering OpenGL primitives. The color type is equal
     * to buffer's underlying BufferWrapper GL type, the stride is 0, and the vertex data itself is this buffer's
     * backing NIO {@link Buffer}. This buffer's vector size must be 3, or 4.
     *
     * @param dc the current {@link DrawContext}.
     *
     * @throws IllegalArgumentException if the DrawContext is null, or if this buffer is not compatible as a color
     *                                  buffer.
     */
    public void bindAsColorBuffer(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.coordsPerVec != 3 && this.coordsPerVec != 4)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange",
                "coordinates per vertex = " + this.coordsPerVec);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glColorPointer(this.coordsPerVec, this.buffer.getGLDataType(), 0, this.buffer.getBackingBuffer());
    }

    /**
     * Binds this buffer as the source of normal coordinates to use when rendering OpenGL primitives. The normal type is
     * equal to buffer's underlying BufferWrapper GL type, the stride is 0, and the vertex data itself is this buffer's
     * backing NIO {@link Buffer}. This buffer's vector size must be 3.
     *
     * @param dc the current {@link DrawContext}.
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

        if (this.coordsPerVec != 3)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange",
                "coordinates per vertex = " + this.coordsPerVec);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glNormalPointer(this.buffer.getGLDataType(), 0, this.buffer.getBackingBuffer());
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

        if (this.coordsPerVec != 2 && this.coordsPerVec != 3 && this.coordsPerVec != 4)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange",
                "coordinates per vertex = " + this.coordsPerVec);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glVertexPointer(this.coordsPerVec, this.buffer.getGLDataType(), 0, this.buffer.getBackingBuffer());
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

        if (this.coordsPerVec != 1 && this.coordsPerVec != 2 && this.coordsPerVec != 3 && this.coordsPerVec != 4)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange",
                "coordinates per vertex = " + this.coordsPerVec);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glTexCoordPointer(this.coordsPerVec, this.buffer.getGLDataType(), 0, this.buffer.getBackingBuffer());
    }

    /**
     * Renders <code>getSize()</code> elements from the currently bounds OpenGL coordinate buffers, beginning with
     * element 0. The specified drawMode indicates which type of OpenGL primitives to render.
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

        dc.getGL().glDrawArrays(drawMode, 0, this.getSize());
    }

    /**
     * Maps the logical vector position to a physical buffer index.
     *
     * @param position the vector position.
     *
     * @return the physical buffer index.
     */
    protected int indexFromVectorPosition(int position)
    {
        return this.coordsPerVec * position;
    }

    /**
     * Maps the physical buffer index to a logical vector position.
     *
     * @param index the physical buffer index.
     *
     * @return the vector position.
     */
    protected int vectorPositionFromIndex(int index)
    {
        return index / this.coordsPerVec;
    }

    //**************************************************************//
    //********************  Iterators  *****************************//
    //**************************************************************//

    protected class BasicIterator<T> implements Iterator<T>
    {
        protected int position;
        protected final int size;
        protected ElementAccessor<T> accessor;

        public BasicIterator(ElementAccessor<T> accessor)
        {
            this.position = -1;
            this.size = getSize();
            this.accessor = accessor;
        }

        public boolean hasNext()
        {
            return this.position < (this.size - 1);
        }

        public T next()
        {
            this.position++;

            if (this.position < this.size)
            {
                return this.accessor.getElement(this.position);
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    protected class ReverseIterator<T> implements Iterator<T>
    {
        protected int position;
        protected ElementAccessor<T> accessor;

        public ReverseIterator(ElementAccessor<T> accessor)
        {
            this.position = getSize();
            this.accessor = accessor;
        }

        public boolean hasNext()
        {
            return this.position > 0;
        }

        public T next()
        {
            this.position--;

            if (this.position >= 0)
            {
                return this.accessor.getElement(this.position);
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    protected interface ElementAccessor<T>
    {
        T getElement(int position);
    }

    protected class CoordAccessor implements ElementAccessor<double[]>
    {
        private int numCoords;

        public CoordAccessor(int minCoordsPerVec)
        {
            this.numCoords = coordsPerVec;
            if (this.numCoords < minCoordsPerVec)
                this.numCoords = minCoordsPerVec;
        }

        public double[] getElement(int position)
        {
            double[] compArray = new double[this.numCoords];
            get(position, compArray);
            return compArray;
        }
    }

    protected class VectorAccessor implements ElementAccessor<Vec4>
    {
        public Vec4 getElement(int position)
        {
            return getVector(position);
        }
    }

    protected class LocationAccessor implements ElementAccessor<LatLon>
    {
        public LatLon getElement(int position)
        {
            return getLocation(position);
        }
    }

    protected class PositionAccessor implements ElementAccessor<Position>
    {
        public Position getElement(int position)
        {
            return getPosition(position);
        }
    }
}
