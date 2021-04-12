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
package gov.nasa.worldwind.retrieve;

/**
 * Stores progress information.
 *
 * @author Patrick Murris
 * @version $Id: Progress.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Progress
{
    private long startTime;         // from System.currentTimeMillis
    private long lastUpdateTime;    // from System.currentTimeMillis
    private long totalSize;
    private long currentSize;
    private long totalCount;
    private long currentCount;

    public Progress()
    {
        this.startTime = System.currentTimeMillis();
    }

    public long getStartTime()
    {
        return this.startTime;
    }

    public void setStartTime(long time)
    {
        this.startTime = time;
    }

    public long getLastUpdateTime()
    {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(long time)
    {
        this.lastUpdateTime = time;
    }

    public long getTotalSize()
    {
        return this.totalSize;
    }

    public void setTotalSize(long size)
    {
        this.totalSize = size;
    }

    public long getCurrentSize()
    {
        return this.currentSize;
    }

    public void setCurrentSize(long size)
    {
        this.currentSize = size;
    }

    public long getTotalCount()
    {
        return this.totalCount;
    }

    public void setTotalCount(long count)
    {
        this.totalCount = count;
    }

    public long getCurrentCount()
    {
        return this.currentCount;
    }

    public void setCurrentCount(long count)
    {
        this.currentCount = count;
    }
}

