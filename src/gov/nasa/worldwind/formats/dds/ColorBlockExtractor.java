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
 * The <code>ColorBlockExtrator</code> interface will copy a 4x4 block of pixel data from an image source. Gaining
 * access to an image source is left to the implementation. This interface makes access to different kinds of image
 * sources transparent by allowing the caller to request the location to extract, and a destination to place the result.
 *
 * @see ColorBlock4x4
 *
 * @author dcollins
 * @version $Id: ColorBlockExtractor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ColorBlockExtractor
{
    /**
     * Extracts a 4x4 block of pixel data at the specified coordinate <code>(x, y)</code>, and places the data in the
     * specified <code>colorBlock</code>. If the coordinate <code>(x, y)</code> with the image, but the entire 4x4
     * block is not, this will either truncate the block to fit the image, or copy nearby pixels to fill the block.
     *
     * @param attributes the DXT compression attributes which may affect how colors are accessed.
     * @param x horizontal coordinate origin to extract pixel data from.
     * @param y vertical coordinate origin to extract pixel data from.
     * @param colorBlock 4x4 block of pixel data that will receive the data.
     *
     * @throws IllegalArgumentException if either <code>attributes</code> or <code>colorBlock</code> is null.
     */
    void extractColorBlock4x4(DXTCompressionAttributes attributes, int x, int y, ColorBlock4x4 colorBlock);
}
