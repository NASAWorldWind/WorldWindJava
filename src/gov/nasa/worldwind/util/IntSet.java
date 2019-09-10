/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

/**
 * A collection of 32-bit integer primitives that contains no duplicate elements. IntSet provides the minimal operations
 * for working with on a set of integers: add, remove, contains, and clear. Additionally, IntSet provides methods for
 * determining the number of integers in the set, and for retrieving the integers as an array.
 * <p>
 * IntSet
 *
 * @author dcollins
 * @version $Id: IntSet.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class IntSet
{
    protected static final int DEFAULT_NUM_BUCKETS = 128;
    protected static final int DEFAULT_BUCKET_CAPACITY = 8;

    protected static class Bucket
    {
        public int[] values;
        public int length;

        public Bucket(int initialCapacity)
        {
            if (initialCapacity < 1)
            {
                String msg = Logging.getMessage("generic.SizeOutOfRange", initialCapacity);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.values = new int[initialCapacity];
            this.length = 0;
        }
    }

    protected Bucket[] buckets;
    protected int numBuckets;
    protected int bucketInitialCapacity;
    protected int size;

    /** Creates an empty IntSet with the default number of buckets and initial bucket capacity. */
    public IntSet()
    {
        this(DEFAULT_NUM_BUCKETS, DEFAULT_BUCKET_CAPACITY);
    }

    /**
     * Creates an empty IntSet with the specified number of buckets and initial bucket capacity. Both the number of
     * buckets and the initial bucket capacity must be greater than zero. For optimal performance with more than a few
     * unique values, the number of buckets should be configured to a large value such as 128. The bucket initial
     * capacity does not significantly affect performance, as each bucket eventually grows to fit its entries.
     *
     * @param numBuckets            the number of buckets this IntSet uses to
     * @param bucketInitialCapacity the initial capacity for each bucket.
     *
     * @throws IllegalArgumentException if either numBuckets or bucketInitialCapacity is less than 1.
     */
    public IntSet(int numBuckets, int bucketInitialCapacity)
    {
        if (numBuckets < 1)
        {
            String msg = Logging.getMessage("generic.SizeOutOfRange", numBuckets);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bucketInitialCapacity < 1)
        {
            String msg = Logging.getMessage("generic.SizeOutOfRange", bucketInitialCapacity);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.buckets = new Bucket[numBuckets];
        this.numBuckets = numBuckets;
        this.bucketInitialCapacity = bucketInitialCapacity;
    }

    /**
     * Returns the number of unique integers in this set.
     *
     * @return the set's size.
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Adds the specified value to this set. If this set does not contain the value, it is added to this set and this
     * returns true. Otherwise this does nothing and returns false.
     *
     * @param value the value to add.
     *
     * @return true if the value is added to this set, otherwise false.
     */
    public boolean add(int value)
    {
        int index = value % this.numBuckets;
        Bucket bucket = this.buckets[index];

        if (bucket == null)
        {
            bucket = this.buckets[index] = new Bucket(this.bucketInitialCapacity);
        }
        else
        {
            for (int i = 0; i < bucket.length; i++)
            {
                if (bucket.values[i] == value)
                    return false;
            }
        }

        if (bucket.values.length <= bucket.length)
        {
            int[] tmp = new int[2 * bucket.values.length];
            System.arraycopy(bucket.values, 0, tmp, 0, bucket.values.length);
            bucket.values = tmp;
        }

        bucket.values[bucket.length++] = value;
        this.size++;

        return true;
    }

    /**
     * Removes the specified value from this set. If this set does not contain the value, this does nothing and returns
     * false. Otherwise this removes the value and returns true.
     *
     * @param value the value to remove.
     *
     * @return true of the value is removed from this set, otherwise false.
     */
    public boolean remove(int value)
    {
        int index = value % this.numBuckets;
        Bucket bucket = this.buckets[index];

        if (bucket == null || bucket.length == 0)
            return false;

        int i;
        for (i = 0; i < bucket.length; i++)
        {
            if (bucket.values[i] == value)
                break;
        }

        if (i == bucket.length)
            return false;

        if (i < bucket.length - 1)
        {
            System.arraycopy(bucket.values, i + 1, bucket.values, i, bucket.length - i - 1);
        }

        bucket.length--;
        this.size--;

        return true;
    }

    /**
     * Indicates whether this set contains the specified value.
     *
     * @param value the value to test.
     *
     * @return true if this set contains the value, otherwise false.
     */
    public boolean contains(int value)
    {
        int index = value % this.numBuckets;
        Bucket bucket = this.buckets[index];

        if (bucket == null)
            return false;

        for (int i = 0; i < bucket.length; i++)
        {
            if (bucket.values[i] == value)
                return true;
        }

        return false;
    }

    /** Removes all of the values from this set. This set is empty after this call returns. */
    public void clear()
    {
        for (int i = 0; i < this.numBuckets; i++)
        {
            if (this.buckets[i] != null)
                this.buckets[i].length = 0;
        }

        this.size = 0;
    }

    /**
     * Returns the values in this set as a 32-bit integer array. The values are stored in the specified array if it has
     * enough room. If the array is <code>null</code> or is not large enough, this allocates and returns a new array
     * with length equal to this set's size.
     *
     * @param array the array into which the values are stored.
     *
     * @return the array of values in this set, or a new array if the specified array is <code>null</code> or not large
     *         enough.
     */
    public int[] toArray(int[] array)
    {
        if (array == null || array.length < this.size)
            array = new int[this.size];

        int offset = 0;

        for (int i = 0; i < this.numBuckets; i++)
        {
            Bucket bucket = this.buckets[i];
            if (bucket != null)
            {
                System.arraycopy(bucket.values, 0, array, offset, bucket.length);
                offset += bucket.length;
            }
        }

        return array;
    }
}
