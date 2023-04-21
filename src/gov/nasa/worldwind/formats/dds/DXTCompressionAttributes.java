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
 * @author dcollins
 * @version $Id: DXTCompressionAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DXTCompressionAttributes
{
    public static final String COLOR_BLOCK_COMPRESSION_BBOX = "ColorBlockCompressionBBox";
    public static final String COLOR_BLOCK_COMPRESSION_EUCLIDEAN_DISTANCE = "ColorBlockCompressionEuclideanDistance";
    public static final String COLOR_BLOCK_COMPRESSION_LUMINANCE_DISTANCE = "ColorBlockCompressionLuminanceDistance";

    private boolean buildMipmaps;
    private boolean premultiplyAlpha;
    private int dxtFormat;
    private boolean enableDXT1Alpha;
    private int dxt1AlphaThreshold;
    private String colorBlockCompressionType;

    protected static final int DEFAULT_DXT1_TRANSPARENCY_THRESHOLD = 128;

    public DXTCompressionAttributes()
    {
        this.buildMipmaps = true;
        this.premultiplyAlpha = true;
        this.dxtFormat = 0;
        this.enableDXT1Alpha = false;
        this.dxt1AlphaThreshold = DEFAULT_DXT1_TRANSPARENCY_THRESHOLD;
        this.colorBlockCompressionType = COLOR_BLOCK_COMPRESSION_EUCLIDEAN_DISTANCE;
    }

    public boolean isBuildMipmaps()
    {
        return this.buildMipmaps;
    }

    public void setBuildMipmaps(boolean buildMipmaps)
    {
        this.buildMipmaps = buildMipmaps;
    }

    public boolean isPremultiplyAlpha()
    {
        return this.premultiplyAlpha;
    }

    public void setPremultiplyAlpha(boolean premultiplyAlpha)
    {
        this.premultiplyAlpha = premultiplyAlpha;
    }

    public int getDXTFormat()
    {
        return this.dxtFormat;
    }

    public void setDXTFormat(int format)
    {
        this.dxtFormat = format;
    }

    public boolean isEnableDXT1Alpha()
    {
        return this.enableDXT1Alpha;
    }

    public void setEnableDXT1Alpha(boolean enable)
    {
        this.enableDXT1Alpha = enable;
    }

    public int getDXT1AlphaThreshold()
    {
        return this.dxt1AlphaThreshold;
    }

    public void setDXT1AlphaThreshold(int threshold)
    {
        this.dxt1AlphaThreshold = threshold;
    }

    public String getColorBlockCompressionType()
    {
        return this.colorBlockCompressionType;
    }

    public void setColorBlockCompressionType(String compressionType)
    {
        this.colorBlockCompressionType = compressionType;
    }
}
