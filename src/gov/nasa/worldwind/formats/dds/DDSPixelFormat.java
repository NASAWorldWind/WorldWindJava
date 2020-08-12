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
package gov.nasa.worldwind.formats.dds;

/**
 * Documentation on the DDS pixel format is available at http://msdn.microsoft.com/en-us/library/bb943982(VS.85).aspx
 *
 * @author dcollins
 * @version $Id: DDSPixelFormat.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DDSPixelFormat
{
    protected final int size = DDSConstants.DDS_PIXEL_FORMAT_SIZE;
    protected int flags;
    protected int fourCC;
    protected int rgbBitCount;
    protected int rBitMask;
    protected int gBitMask;
    protected int bBitMask;
    protected int aBitMask;

    public DDSPixelFormat()
    {
    }

    /**
     * Returns the pixel format structure size in bytes. Will always return 32.
     *
     * @return pixel format structure size in bytes.
     */
    public final int getSize()
    {
        return this.size;
    }

    public int getFlags()
    {
        return this.flags;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    public int getFourCC()
    {
        return this.fourCC;
    }

    public void setFourCC(int fourCC)
    {
        this.fourCC = fourCC;
    }

    public int getRGBBitCount()
    {
        return this.rgbBitCount;
    }

    public void setRGBBitCount(int bitCount)
    {
        this.rgbBitCount = bitCount;
    }

    public int getRBitMask()
    {
        return this.rBitMask;
    }

    public void setRBitMask(int rBitMask)
    {
        this.rBitMask = rBitMask;
    }

    public int getGBitMask()
    {
        return this.gBitMask;
    }

    public void setGBitMask(int gBitMask)
    {
        this.gBitMask = gBitMask;
    }

    public int getBBitMask()
    {
        return this.bBitMask;
    }

    public void setBBitMask(int bBitMask)
    {
        this.bBitMask = bBitMask;
    }

    public int getABitMask()
    {
        return this.aBitMask;
    }

    public void setABitMask(int aBitMask)
    {
        this.aBitMask = aBitMask;
    }
}
