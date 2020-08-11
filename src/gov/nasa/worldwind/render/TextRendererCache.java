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
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.util.Logging;

/**
 * http://java.sun.com/products/java-media/2D/reference/faqs/index.html#Q_What_are_fractional_metrics_Wh
 *
 * @author tag
 * @version $Id: TextRendererCache.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class TextRendererCache implements Disposable
{
    public static class CacheKey
    {
        private final java.awt.Font font;
        private final boolean antialiased;
        private final boolean useFractionalMetrics;
        private final boolean mipmap;

        public CacheKey(java.awt.Font font, boolean antialiased, boolean useFractionalMetrics, boolean mipmap)
        {
            if (font == null)
            {
                String message = Logging.getMessage("nullValue.FontIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.font = font;
            this.antialiased = antialiased;
            this.useFractionalMetrics = useFractionalMetrics;
            this.mipmap = mipmap;
        }

        public final java.awt.Font getFont()
        {
            return this.font;
        }

        public final boolean isAntialiased()
        {
            return this.antialiased;
        }

        public final boolean isUseFractionalMetrics()
        {
            return this.useFractionalMetrics;
        }

        public final boolean isMipmap()
        {
            return this.mipmap;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            CacheKey that = (CacheKey) o;

            return (this.antialiased == that.antialiased)
                && (this.useFractionalMetrics == that.useFractionalMetrics)
                && (this.mipmap == that.mipmap)
                && (this.font.equals(that.font));
        }

        public int hashCode()
        {
            int result = this.font.hashCode();
            result = 31 * result + (this.antialiased ? 1 : 0);
            result = 31 * result + (this.useFractionalMetrics ? 1 : 0);
            result = 31 * result + (this.mipmap ? 1 : 0);
            return result;
        }
    }

    protected java.util.concurrent.ConcurrentHashMap<Object, TextRenderer> textRendererMap;

    public TextRendererCache()
    {
        this.textRendererMap = new java.util.concurrent.ConcurrentHashMap<Object, TextRenderer>();
    }

    public void dispose()
    {
        this.disposeAll();
        this.textRendererMap.clear();
    }

    public int getNumObjects()
    {
        return this.textRendererMap.size();
    }

    public TextRenderer get(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.textRendererMap.get(key);
    }

    public void put(Object key, TextRenderer textRenderer)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TextRenderer oldTextRenderer = this.textRendererMap.put(key, textRenderer);

        if (oldTextRenderer != null)
        {
            this.dispose(oldTextRenderer);
        }
    }

    public void remove(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TextRenderer textRenderer = this.textRendererMap.remove(key);

        if (textRenderer != null)
        {
            this.dispose(textRenderer);
        }
    }

    public boolean contains(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.textRendererMap.containsKey(key);
    }

    public void clear()
    {
        this.disposeAll();
        this.textRendererMap.clear();
    }

    protected void dispose(TextRenderer textRenderer)
    {
        if (textRenderer != null)
        {
            textRenderer.dispose();
        }
    }

    protected void disposeAll()
    {
        for (java.util.Map.Entry<Object, TextRenderer> e : this.textRendererMap.entrySet())
        {
            if (e.getValue() != null)
            {
                this.dispose(e.getValue());
            }
        }
    }
}
