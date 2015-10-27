/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
