/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

/**
 * Range describes a contiguous region in a series of items.
 *
 * @author dcollins
 * @version $Id: Range.java 2281 2014-08-29 23:08:04Z dcollins $
 */
public class Range
{
    /** The start index of the range. 0 indicates the first item in the series. */
    public int location;
    /** The number of items in the range. May be 0 to indicate an empty range. */
    public int length;

    /**
     * Creates a new range with the specified start index and number of items.
     *
     * @param location The start index of the range.
     * @param length   The number of items in the range. May be 0 to indicate an empty range.
     */
    public Range(int location, int length)
    {
        this.location = location;
        this.length = length;
    }

    /**
     * Returns a boolean value indicating whether or not the specified location is in this range. The location is in
     * this range if it's greater than or equal to <code>this.location</code> and less than <code>this.location +
     * this.length</code>.
     *
     * @param location the location to test.
     *
     * @return true if the location is in this range, otherwise false.
     */
    public boolean contains(int location)
    {
        return location >= this.location && location < this.location + this.length;
    }
}