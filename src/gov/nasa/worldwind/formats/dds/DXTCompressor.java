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
 * The <code>DXTCompressor</code> interface will compress an in-memory image using one of the DXT block compression
 * schemes. The details of each block compression scheme is handled by the implementation.
 * 
 * @author dcollins
 * @version $Id: DXTCompressor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface DXTCompressor
{
    /**
     * Returns the DXT format constant associated with this compressor.
     *
     * @see gov.nasa.worldwind.formats.dds.DDSConstants
     *
     * @return DXT format for this compressor.
     */
    int getDXTFormat();

    /**
     * Returns the compressed size in bytes of the specified <code>image</code>.
     *
     * @param image the image to compute the compressed size for.
     * @param attributes the attributes that may affect the compressed size.
     *
     * @return compressed size in bytes of the specified image.
     * @throws IllegalArgumentException if either <code>image</code> or <code>attributes</code> is null.
     */
    int getCompressedSize(java.awt.image.BufferedImage image, DXTCompressionAttributes attributes);

    /**
     * Encodes the specified <code>image</code> image into a compressed DXT codec, and writes the compressed bytes
     * to the specified <code>buffer</code>. The buffer should be allocated with enough space to hold the compressed
     * output. The correct size should be computed by calling <code>getCompressedSize(image, attributes)</code>.
     *
     * @param image the image to compress.
     * @param attributes the attributes that may affect the compression.
     * @param buffer the buffer that will receive the compressed output.
     *
     * @see #getCompressedSize(java.awt.image.BufferedImage, DXTCompressionAttributes)
     * @throws IllegalArgumentException if any of <code>image</code>, <code>attributes</code>, or
     *                                  <code>buffer</code> are null.
     */
    void compressImage(java.awt.image.BufferedImage image, DXTCompressionAttributes attributes,
        java.nio.ByteBuffer buffer);
}
