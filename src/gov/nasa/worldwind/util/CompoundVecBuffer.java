/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;

import java.nio.IntBuffer;
import java.util.*;

/**
 * CompoundVecBuffer defines an interface for storing and retrieving a collection of variable length {@link
 * gov.nasa.worldwind.util.VecBuffer} objects. Each VecBuffer is retrieved via an index. The range of valid indices in a
 * CompoundVecBuffer is [0, size() - 1], inclusive. Implementations of CompoundVecBuffer define how each VecBuffer is
 * stored and retrieved according to its index.
 * <p/>
 * To retrieve a single VecBuffer given an index, invoke {@link #subBuffer(int)}. To retrieve a VecBuffer's size, in
 * number of logical tuples, invoke {@link #subBufferSize(int)}.
 * <p/>
 * To create a new view of this CompoundVecBuffer from one or many VecBuffers, invoke one of the <code>slice</code>
 * methods: <ul> <li>{@link #slice(int, int)} creates a view of this CompoundVecbufer given a contiguous sequence of
 * VecBuffer indices.</li> <li>{@link #slice(int[], int, int)} creates a view of this CompoundVecBuffer given an array
 * of VecBuffer indices.</li> </ul>
 *
 * @author dcollins
 * @version $Id: CompoundVecBuffer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class CompoundVecBuffer
{
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;
    protected static final boolean ALLOCATE_DIRECT_BUFFERS = true;

    protected int count;
    protected int capacity;
    protected IntBuffer offsets;
    protected IntBuffer lengths;

    /**
     * Constructs a CompoundVecBuffer with the specified initial capacity.
     *
     * @param capacity the CompoundVecBuffer's initial capacity, in number of sub-buffers.
     *
     * @throws IllegalArgumentException if the capacity is less than 1.
     */
    public CompoundVecBuffer(int capacity)
    {
        if (capacity < 1)
        {
            String message = Logging.getMessage("generic.CapacityIsInvalid", capacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.capacity = capacity;
        this.offsets = WWBufferUtil.newIntBuffer(capacity, ALLOCATE_DIRECT_BUFFERS);
        this.lengths = WWBufferUtil.newIntBuffer(capacity, ALLOCATE_DIRECT_BUFFERS);
    }

    /** Constructs a CompoundVecBuffer with the default initial capacity. */
    public CompoundVecBuffer()
    {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    protected CompoundVecBuffer(CompoundVecBuffer that, int beginIndex, int endIndex)
    {
        int length = endIndex - beginIndex + 1;

        this.count = length;
        this.capacity = length;

        this.offsets = WWBufferUtil.newIntBuffer(length, ALLOCATE_DIRECT_BUFFERS);
        that.offsets.limit(endIndex + 1);
        that.offsets.position(beginIndex);
        this.offsets.put(that.offsets);
        this.offsets.rewind();
        that.offsets.clear();

        this.lengths = WWBufferUtil.newIntBuffer(length, ALLOCATE_DIRECT_BUFFERS);
        that.lengths.limit(endIndex + 1);
        that.lengths.position(beginIndex);
        this.lengths.put(that.lengths);
        this.lengths.rewind();
        that.lengths.clear();
    }

    protected CompoundVecBuffer(CompoundVecBuffer that, int[] indices, int offset, int length)
    {
        this.count = length;
        this.capacity = length;

        this.offsets = WWBufferUtil.newIntBuffer(length, ALLOCATE_DIRECT_BUFFERS);
        this.lengths = WWBufferUtil.newIntBuffer(length, ALLOCATE_DIRECT_BUFFERS);

        for (int i = offset; i < offset + length; i++)
        {
            this.offsets.put(that.offsets.get(indices[i]));
            this.lengths.put(that.lengths.get(indices[i]));
        }

        this.offsets.rewind();
        this.lengths.rewind();
    }

    /**
     * Returns an empty CompoundVecBuffer. The returned CompoundVecBuffer has a size of zero and contains no
     * sub-buffers.
     *
     * @param coordsPerVec the number of coordinates per logical vector.
     *
     * @return the empty CompoundVecBuffer.
     */
    public static CompoundVecBuffer emptyCompoundVecBuffer(int coordsPerVec)
    {
        if (coordsPerVec < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", coordsPerVec);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new EmptyCompoundVecBuffer(coordsPerVec);
    }

    /**
     * Returns the number of VecBuffers stored in this CompoundVecBuffer.
     *
     * @return the number of VecBuffers in this CompoundVecBuffer.
     */
    public int size()
    {
        return this.count;
    }

    /**
     * Returns the size in logical vectors of the VecBuffer with the specified index.
     *
     * @param index the index for the VecBuffer who's size is returned.
     *
     * @return the size of the specified VecBuffer.
     *
     * @throws IllegalArgumentException if the index is out of range.
     */
    public abstract int subBufferSize(int index);

    /**
     * Returns the sub-buffer at the specified index as a {@link gov.nasa.worldwind.util.VecBuffer}.
     *
     * @param index the index of the VecBuffer to return.
     *
     * @return the VecBuffer at the specified index.
     *
     * @throws IllegalArgumentException if the index is out of range.
     */
    public VecBuffer subBuffer(int index)
    {
        if (index < 0 || index >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int off = this.offsets.get(index);
        int len = this.lengths.get(index);

        if (len > 0)
        {
            return this.createSubBuffer(off, len);
        }
        else
        {
            return VecBuffer.emptyVecBuffer(this.getCoordsPerVec());
        }
    }

    /**
     * Returns a new logical view of this CompoundVecBuffer. The returned buffer has length <code>endIndex - beginIndex
     * + 1</code> and references this buffer's contents starting at <code>beginIndex</code>, and ending at
     * <code>endIndex</code>. The returned buffer shares this buffers's backing data. Changes to this buffer are
     * reflected in the returned buffer, and vice versa.
     *
     * @param beginIndex the index of the first sub-buffer to include in the subset.
     * @param endIndex   the index of the last sub-buffer to include in the subset.
     *
     * @return a new CompoundVecBuffer representing a subset of this CompoundVecBuffer.
     *
     * @throws IllegalArgumentException if beginIndex is out of range, if endIndex is out of range, or if beginIndex >
     *                                  endIndex.
     */
    public CompoundVecBuffer slice(int beginIndex, int endIndex)
    {
        if (beginIndex < 0 || beginIndex >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", beginIndex);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (endIndex < 0 || endIndex >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", endIndex);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (beginIndex > endIndex)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", beginIndex);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.createSlice(beginIndex, endIndex);
    }

    /**
     * Returns a new logical view of this CompoundVecBuffer. The returned buffer's length is equal to the specified
     * <code>length</code>, and contains this buffer's contents for each index in <code>indices</code>. The returned
     * buffer shares this buffers's backing data. Changes to this buffer are reflected in the returned buffer, and vice
     * versa.
     *
     * @param indices an array containing the indices include in the subset.
     * @param offset  the array starting index.
     * @param length  the number of array values to use.
     *
     * @return a new CompoundVecBuffer representing a subset of this CompoundVecBuffer.
     *
     * @throws IllegalArgumentException if the array of indices is null, if the offset or length are invalid, or if any
     *                                  of the indices is out of range.
     */
    public CompoundVecBuffer slice(int[] indices, int offset, int length)
    {
        if (indices == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (length < 0 || length > indices.length)
        {
            String message = Logging.getMessage("generic.LengthIsInvalid", length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (offset < 0 || offset + length > indices.length)
        {
            String message = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = offset; i < offset + length; i++)
        {
            if (indices[i] < 0 || indices[i] >= this.count)
            {
                String message = Logging.getMessage("generic.indexOutOfRange", indices[i]);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        return this.createSlice(indices, offset, length);
    }

    /**
     * Returns a new logical view of this CompoundVecBuffer. The returned buffer's length is equal to the length of
     * <code>indices</code>, and contains this buffer's contents for each index in <code>indices</code>. The returned
     * buffer shares this buffers's backing data. Changes to this buffer are reflected in the returned buffer, and vice
     * versa.
     *
     * @param indices an array containing the indices include in the subset.
     *
     * @return a new CompoundVecBuffer representing a subset of this CompoundVecBuffer.
     *
     * @throws IllegalArgumentException if the array of indices is null, or if any of the indices is out of range.
     */
    public CompoundVecBuffer slice(int[] indices)
    {
        if (indices == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.slice(indices, 0, indices.length);
    }

    /** Sets the number sub-buffers to zero. This does not free any memory associated with this CompoundVecBuffer. */
    public void clear()
    {
        this.count = 0;
    }

    /**
     * Returns the number of coordinates per logical vector element.
     *
     * @return the cardinality of a logical vector element.
     */
    public abstract int getCoordsPerVec();

    //**************************************************************//
    //********************  Protected Interface  *******************//
    //**************************************************************//

    protected abstract VecBuffer createSubBuffer(int offset, int length);

    protected abstract CompoundVecBuffer createSlice(int[] indices, int offset, int length);

    protected abstract CompoundVecBuffer createSlice(int beginIndex, int endIndex);

    protected int addSubBuffer(int offset, int length)
    {
        int minCount = 1 + this.count;
        if (minCount > this.capacity)
            this.expandCapacity(minCount);

        int index = this.count;
        this.offsets.put(index, offset);
        this.lengths.put(index, length);
        this.count++;

        return index;
    }

    protected void expandCapacity(int minCapacity)
    {
        int newCapacity = 2 * this.capacity;

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

        this.offsets = WWBufferUtil.copyOf(this.offsets, newCapacity);
        this.lengths = WWBufferUtil.copyOf(this.lengths, newCapacity);
        this.capacity = newCapacity;
    }

    //**************************************************************//
    //********************  Iterable Methods  **********************//
    //**************************************************************//

    /**
     * Returns an iterator over this buffer's logical vectors, as double[] coordinate arrays. The array returned from
     * each call to Iterator.next() will be newly allocated, and will have length equal to coordsPerVec.
     *
     * @return iterator over this buffer's vectors, as double[] arrays.
     */
    public Iterable<double[]> getCoords()
    {
        return this.getCoords(this.getCoordsPerVec());
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
                return new CompoundIterator<double[]>(new CoordIterable(minCoordsPerVec));
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
                return new ReverseCompoundIterator<double[]>(new CoordIterable(minCoordsPerVec));
            }
        };
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as Vec4 references.
     *
     * @return iterator over this buffer's vectors, as Vec4 references.
     */
    public Iterable<? extends Vec4> getVectors()
    {
        return new Iterable<Vec4>()
        {
            public Iterator<Vec4> iterator()
            {
                return new CompoundIterator<Vec4>(new VectorIterable());
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as Vec4 references.
     *
     * @return reverse iterator over this buffer's vectors, as Vec4 references.
     */
    public Iterable<? extends Vec4> getReverseVectors()
    {
        return new Iterable<Vec4>()
        {
            public Iterator<Vec4> iterator()
            {
                return new ReverseCompoundIterator<Vec4>(new VectorIterable());
            }
        };
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as LatLon locations.
     *
     * @return iterator over this buffer's vectors, as LatLon locations.
     */
    public Iterable<? extends LatLon> getLocations()
    {
        return new Iterable<LatLon>()
        {
            public Iterator<LatLon> iterator()
            {
                return new CompoundIterator<LatLon>(new LocationIterable());
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as LatLon locations.
     *
     * @return reverse iterator over this buffer's vectors, as LatLon locations.
     */
    public Iterable<? extends LatLon> getReverseLocations()
    {
        return new Iterable<LatLon>()
        {
            public Iterator<LatLon> iterator()
            {
                return new ReverseCompoundIterator<LatLon>(new LocationIterable());
            }
        };
    }

    /**
     * Returns an iterator over this buffer's logical vectors, as geographic Positions.
     *
     * @return iterator over this buffer's vectors, as geographic Positions.
     */
    public Iterable<? extends Position> getPositions()
    {
        return new Iterable<Position>()
        {
            public Iterator<Position> iterator()
            {
                return new CompoundIterator<Position>(new PositionIterable());
            }
        };
    }

    /**
     * Returns a reverse iterator over this buffer's logical vectors, as geographic Positions.
     *
     * @return reverse iterator over this buffer's vectors, as geographic Positions.
     */
    public Iterable<? extends Position> getReversePositions()
    {
        return new Iterable<Position>()
        {
            public Iterator<Position> iterator()
            {
                return new ReverseCompoundIterator<Position>(new PositionIterable());
            }
        };
    }

    //**************************************************************//
    //********************  Iterator Implementations  **************//
    //**************************************************************//

    protected class CompoundIterator<T> implements Iterator<T>
    {
        protected int subBuffer;
        protected Iterator<T> subIterator;
        protected final int subBufferCount;
        protected final SubBufferIterable<T> subBufferIterable;

        protected CompoundIterator(SubBufferIterable<T> subBufferIterable)
        {
            this.subBuffer = 0;
            this.subBufferCount = size();
            this.subBufferIterable = subBufferIterable;
        }

        public boolean hasNext()
        {
            this.updateSubIterator();

            return this.subIterator != null && this.subIterator.hasNext();
        }

        public T next()
        {
            this.updateSubIterator();

            if (this.subIterator != null && this.subIterator.hasNext())
            {
                return this.subIterator.next();
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

        protected void updateSubIterator()
        {
            while (this.subBuffer < this.subBufferCount && (this.subIterator == null || !this.subIterator.hasNext()))
            {
                this.subIterator = this.subBufferIterable.iterator(this.subBuffer);
                this.subBuffer++;
            }
        }
    }

    protected class ReverseCompoundIterator<T> extends CompoundIterator<T>
    {
        public ReverseCompoundIterator(SubBufferIterable<T> subBufferIterable)
        {
            super(subBufferIterable);
            this.subBuffer = this.subBufferCount - 1;
        }

        protected void updateSubIterator()
        {
            while (this.subBuffer >= 0 && (this.subIterator == null || !this.subIterator.hasNext()))
            {
                this.subIterator = this.subBufferIterable.reverseIterator(this.subBuffer);
                this.subBuffer--;
            }
        }
    }

    protected interface SubBufferIterable<T>
    {
        Iterator<T> iterator(int index);

        Iterator<T> reverseIterator(int index);
    }

    protected class CoordIterable implements SubBufferIterable<double[]>
    {
        private int minCoordsPerVec;

        public CoordIterable(int minCoordsPerVec)
        {
            this.minCoordsPerVec = minCoordsPerVec;
        }

        public Iterator<double[]> iterator(int index)
        {
            return subBuffer(index).getCoords(this.minCoordsPerVec).iterator();
        }

        public Iterator<double[]> reverseIterator(int index)
        {
            return subBuffer(index).getReverseCoords(this.minCoordsPerVec).iterator();
        }
    }

    protected class VectorIterable implements SubBufferIterable<Vec4>
    {
        public Iterator<Vec4> iterator(int index)
        {
            return subBuffer(index).getVectors().iterator();
        }

        public Iterator<Vec4> reverseIterator(int index)
        {
            return subBuffer(index).getReverseVectors().iterator();
        }
    }

    protected class LocationIterable implements SubBufferIterable<LatLon>
    {
        public Iterator<LatLon> iterator(int index)
        {
            return subBuffer(index).getLocations().iterator();
        }

        public Iterator<LatLon> reverseIterator(int index)
        {
            return subBuffer(index).getReverseLocations().iterator();
        }
    }

    protected class PositionIterable implements SubBufferIterable<Position>
    {
        public Iterator<Position> iterator(int index)
        {
            return subBuffer(index).getPositions().iterator();
        }

        public Iterator<Position> reverseIterator(int index)
        {
            return subBuffer(index).getReversePositions().iterator();
        }
    }

    //**************************************************************//
    //********************  Empty CompoundVecBuffer  ***************//
    //**************************************************************//

    protected static class EmptyCompoundVecBuffer extends CompoundVecBuffer
    {
        protected int coordsPerVec;

        public EmptyCompoundVecBuffer(int coordsPerVec)
        {
            super(1);

            if (coordsPerVec < 1)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", coordsPerVec);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.coordsPerVec = coordsPerVec;
        }

        protected EmptyCompoundVecBuffer(EmptyCompoundVecBuffer that, int beginIndex, int endIndex)
        {
            super(that, beginIndex, endIndex);
        }

        protected EmptyCompoundVecBuffer(EmptyCompoundVecBuffer that, int[] indices, int offset, int length)
        {
            super(that, indices, offset, length);
        }

        public int subBufferSize(int index)
        {
            if (index < 0 || index >= this.count)
            {
                String message = Logging.getMessage("generic.indexOutOfRange", index);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return 0;
        }

        public int getCoordsPerVec()
        {
            return this.coordsPerVec;
        }

        protected VecBuffer createSubBuffer(int offset, int length)
        {
            return VecBuffer.emptyVecBuffer(this.coordsPerVec);
        }

        protected CompoundVecBuffer createSlice(int[] indices, int offset, int length)
        {
            return new EmptyCompoundVecBuffer(this, indices, offset, length);
        }

        protected CompoundVecBuffer createSlice(int beginIndex, int endIndex)
        {
            return new EmptyCompoundVecBuffer(this, beginIndex, endIndex);
        }
    }
}
