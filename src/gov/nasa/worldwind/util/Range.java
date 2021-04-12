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