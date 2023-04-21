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
 * 24 bit 888 RGB color
 *
 * @author Lado Garakanidze
 * @version $Id: Color24.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class Color24
{
    /**
     * The red color component.
     */
    public int r;
    /**
     * The green color component.
     */
    public int g;
    /**
     * The blue color component.
     */
    public int b;

    /**
     * Creates a 24 bit 888 RGB color with all values set to 0.
     */
    public Color24()
    {
        this.r = this.g = this.b = 0;
    }

    public Color24(int r, int g, int b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getPixel888()
    {
        return (this.r << 16 | this.g << 8 | this.b);
    }

    public static Color24 fromPixel565(int pixel)
    {
        Color24 color = new Color24();

        color.r = (int) (((long) pixel) & 0xf800) >>> 8;
        color.g = (int) (((long) pixel) & 0x07e0) >>> 3;
        color.b = (int) (((long) pixel) & 0x001f) << 3;

        return color;
    }

    public static Color24 multiplyAlpha(Color24 color, int alpha)
    {
        Color24 result = new Color24();

        double alphaF = alpha / 256.0;

        result.r = (int) (color.r * alphaF);
        result.g = (int) (color.g * alphaF);
        result.b = (int) (color.b * alphaF);

        return result;
    }

    public static Color24[] expandLookupTable(short minColor, short maxColor)
    {
        Color24 colorMin = Color24.fromPixel565(minColor);
        Color24 colorMax = Color24.fromPixel565(maxColor);

        Color24 color3 = new Color24();
        Color24 color4 = new Color24();

        color3.r = (2 * colorMin.r + colorMax.r + 1) / 3;
        color3.g = (2 * colorMin.g + colorMax.g + 1) / 3;
        color3.b = (2 * colorMin.b + colorMax.b + 1) / 3;

        color4.r = (colorMin.r + 2 * colorMax.r + 1) / 3;
        color4.g = (colorMin.g + 2 * colorMax.g + 1) / 3;
        color4.b = (colorMin.b + 2 * colorMax.b + 1) / 3;

        return new Color24[]{colorMin, colorMax, color3, color4};
    }

}
