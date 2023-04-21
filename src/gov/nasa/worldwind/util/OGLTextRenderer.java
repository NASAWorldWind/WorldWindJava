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

import gov.nasa.worldwind.render.*;

/**
 * @author dcollins
 * @version $Id: OGLTextRenderer.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class OGLTextRenderer extends TextRenderer
{
    // By default enable antialiasing, mipmapping, and smoothing, but disable fractional metrics and vertex arrays.
    // * For the common case where text is rendered without scaling at integral screen coordinates, smoothing and
    //   mipmapping have no effect on the rendering of text. However smoothing and mipmapping will blur text
    //   if it's drawn scaled or if the text is drawn at a non-integral screen point, but that's better than the
    //   ugly aliased appearance without smoothing or mipmapping.
    // * Fractional metrics are deigned for high resolution output devices (i.e. printers), enabling this feature
    //   on a standard screen will result in blurred text. See the following link for a description of why:
    //   http://java.sun.com/products/java-media/2D/reference/faqs/index.html#Q_What_are_fractional_metrics_Wh
    // * Vertex arrays are problematic on some graphics cards. Disable this feature by default to maximize
    //   compatibility.

    protected static final java.awt.Font DEFAULT_FONT = java.awt.Font.decode("Arial-PLAIN-12");
    protected static final boolean DEFAULT_ANTIALIAS = true;
    protected static final boolean DEFAULT_USE_FRACTIONAL_METRICS = false;
    protected static final boolean DEFAULT_MIPMAP = true;
    protected static final boolean DEFAULT_SMOOTHING = true;
    protected static final boolean DEFAULT_USE_VERTEX_ARRAYS = false;

    public OGLTextRenderer(java.awt.Font font, boolean antialiased, boolean useFractionalMetrics,
        RenderDelegate renderDelegate, boolean mipmap)
    {
        super(font, antialiased, useFractionalMetrics, renderDelegate, mipmap);
        this.initialize();
    }

    public OGLTextRenderer(java.awt.Font font, boolean antialiased, boolean useFractionalMetrics,
        RenderDelegate renderDelegate)
    {
        this(font, antialiased, useFractionalMetrics, renderDelegate, DEFAULT_MIPMAP);
    }

    public OGLTextRenderer(java.awt.Font font, boolean antialiased, boolean useFractionalMetrics)
    {
        this(font, antialiased, useFractionalMetrics, null, DEFAULT_MIPMAP);
    }

    public OGLTextRenderer(java.awt.Font font, boolean mipmap)
    {
        this(font, DEFAULT_ANTIALIAS, DEFAULT_USE_FRACTIONAL_METRICS, null, mipmap);
    }

    public OGLTextRenderer(java.awt.Font font)
    {
        this(font, DEFAULT_ANTIALIAS, DEFAULT_USE_FRACTIONAL_METRICS, null, DEFAULT_MIPMAP);
    }

    public OGLTextRenderer()
    {
        this(DEFAULT_FONT, DEFAULT_ANTIALIAS, DEFAULT_USE_FRACTIONAL_METRICS, null, DEFAULT_MIPMAP);
    }

    protected void initialize()
    {
        this.setSmoothing(DEFAULT_SMOOTHING);
        this.setUseVertexArrays(DEFAULT_USE_VERTEX_ARRAYS);
    }

    //**************************************************************//
    //********************  Common Utilities  **********************//
    //**************************************************************//

    public static TextRenderer getOrCreateTextRenderer(TextRendererCache cache,
        java.awt.Font font, boolean antialiased, boolean useFractionalMetrics, boolean mipmap)
    {
        if (cache == null)
        {
            String message = Logging.getMessage("nullValue.CacheIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TextRendererCache.CacheKey key = new TextRendererCache.CacheKey(font, antialiased, useFractionalMetrics,
            mipmap);

        TextRenderer value = cache.get(key);
        if (value == null)
        {
            value = new OGLTextRenderer(font, antialiased, useFractionalMetrics, null, mipmap);
            cache.put(key, value);
        }

        return value;
    }

    public static TextRenderer getOrCreateTextRenderer(TextRendererCache cache, java.awt.Font font)
    {
        if (cache == null)
        {
            String message = Logging.getMessage("nullValue.CacheIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getOrCreateTextRenderer(cache, font, DEFAULT_ANTIALIAS, DEFAULT_USE_FRACTIONAL_METRICS, DEFAULT_MIPMAP);
    }
}
