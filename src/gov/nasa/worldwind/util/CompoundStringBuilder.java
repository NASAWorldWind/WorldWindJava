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
package gov.nasa.worldwind.util;

import java.util.Arrays;

/**
 * CompoundStringBuilder provides a mechanism for storing and retrieving a collection of variable length strings in a
 * single {@link StringBuilder}.
 *
 * @author dcollins
 * @version $Id: CompoundStringBuilder.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CompoundStringBuilder
{
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;

    protected StringBuilder buffer;
    protected int count;
    protected int capacity;
    protected int[] offsets;
    protected int[] lengths;

    /**
     * Constructs a CompoundStringBuilder with the specified backing StringBuilder and initial capacity.
     *
     * @param stringBuilder the StringBuilder in which to store the string data.
     * @param capacity      the compound buffer's initial capacity.
     *
     * @throws IllegalArgumentException if the stringBuilder is null or if the capacity is less than 1.
     */
    public CompoundStringBuilder(StringBuilder stringBuilder, int capacity)
    {
        if (stringBuilder == null)
        {
            String message = Logging.getMessage("nullValue.StringBuilderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (capacity < 1)
        {
            String message = Logging.getMessage("generic.CapacityIsInvalid", capacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.buffer = stringBuilder;
        this.capacity = capacity;
        this.offsets = new int[this.capacity];
        this.lengths = new int[this.capacity];
    }

    /**
     * Constructs a CompoundStringBuilder with a default backing StringBuilder, and the specified initial capacity.
     *
     * @param capacity the compound buffer's initial capacity.
     *
     * @throws IllegalArgumentException if the capacity is less than 1.
     */
    public CompoundStringBuilder(int capacity)
    {
        this(new StringBuilder(), capacity);
    }

    /** Constructs a CompoundStringBuilder with a default backing StringBuilder, and the default initial capacity. */
    public CompoundStringBuilder()
    {
        this(new StringBuilder(), DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Returns the number of strings stored in this CompoundStringBuilder.
     *
     * @return the number of strings in this CompoundStringBuilder.
     */
    public int size()
    {
        return this.count;
    }

    /**
     * Returns the length of the substring with the specified index.
     *
     * @param index the index for the substring who's length is returned.
     *
     * @return the length of the specified substring.
     *
     * @throws IllegalArgumentException if the index is out of range.
     */
    public int substringLength(int index)
    {
        if (index < 0 || index >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.lengths[index];
    }

    /**
     * Returns the substring at the specified index as a {@link String}.
     *
     * @param index the index of the substring to return.
     *
     * @return the substring at the specified index.
     *
     * @throws IllegalArgumentException if the index is out of range.
     */
    public String substring(int index)
    {
        if (index < 0 || index >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        CharSequence cs = this.subSequence(index);
        return cs != null ? cs.toString() : null;
    }

    /**
     * Returns the substring at the specified index as a {@link CharSequence}.
     *
     * @param index the index of the substring to return.
     *
     * @return the substring at the specified index.
     *
     * @throws IllegalArgumentException if the index is out of range.
     */
    public CharSequence subSequence(int index)
    {
        if (index < 0 || index >= this.count)
        {
            String message = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int off = this.offsets[index];
        int len = this.lengths[index];
        return this.buffer.subSequence(off, off + len);
    }

    /**
     * Appends the contents of the specified substring to the end of this CompoundStringBuilder, incrementing the number
     * of substrings by one. The backing buffer grows to accomodate the sub-buffer if it does not already have enough
     * capacity to hold it.
     *
     * @param charSequence the substring to append.
     *
     * @return the substring's index.
     *
     * @throws IllegalArgumentException if the charSequence is null.
     */
    public int append(CharSequence charSequence)
    {
        if (charSequence == null)
        {
            String message = Logging.getMessage("nullValue.CharSequenceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int newCount = 1 + this.count;
        if (newCount > this.capacity)
            this.expandCapacity(newCount);

        int index = this.count;
        this.offsets[index] = this.buffer.length();
        this.lengths[index] = charSequence.length();
        this.buffer.append(charSequence, 0, charSequence.length());
        this.count++;

        return index;
    }

    /**
     * Clears this CompoundStringBuilder's backing StringBuilder and sets the number of substrings to zero. This does
     * not free any memory associated with this CompoundStringBuilder.
     */
    public void clear()
    {
        this.buffer.delete(0, this.buffer.length());
        this.count = 0;
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

        this.offsets = Arrays.copyOf(this.offsets, newCapacity);
        this.lengths = Arrays.copyOf(this.lengths, newCapacity);
        this.capacity = newCapacity;
    }
}
