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
package gov.nasa.worldwind.util.webview;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.*;
import java.awt.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: WebViewTexture.java 1171 2013-02-11 21:45:02Z dcollins $
 * @deprecated 
 */
@Deprecated
public class WebViewTexture extends BasicWWTexture
{
    protected Dimension frameSize;
    protected boolean flipVertically;

    public WebViewTexture(Dimension frameSize, boolean useMipMaps, boolean flipVertically)
    {
        // Create a new unique object to use as the cache key.
        super(new Object(), useMipMaps); // Do not generate mipmaps for the texture.

        this.frameSize = frameSize;
        this.flipVertically = flipVertically;
    }

    @Override
    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        boolean isBound = super.bind(dc);

        if (isBound)
        {
            this.updateIfNeeded(dc);
        }

        return isBound;
    }

    @Override
    protected Texture initializeTexture(DrawContext dc, Object imageSource)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (this.textureInitializationFailed)
            return null;

        Texture t;
        GL gl = dc.getGL();

        try
        {
            // Allocate a texture with the proper dimensions and texture internal format, but with no data.
            TextureData td = new TextureData(
                gl.getGLProfile(), // GL profile
                GL.GL_RGBA, // texture internal format
                this.frameSize.width, // texture image with
                this.frameSize.height, // texture image height
                0, // border
                GL.GL_RGBA, // pixelFormat
                GL.GL_UNSIGNED_BYTE, // pixelType
                false, // mipmap
                false, // dataIsCompressed
                this.flipVertically,
                Buffers.newDirectByteBuffer(4 * this.frameSize.width * this.frameSize.height), // buffer
                null); // flusher
            t = TextureIO.newTexture(td);

            dc.getTextureCache().put(imageSource, t);
            t.bind(gl);

            // Configure the texture to use nearest-neighbor filtering. This ensures that the texels are aligned exactly
            // with screen pixels, and eliminates blurry artifacts from linear filtering.
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
        }
        catch (Exception e)
        {
            // TODO: refactor as generic.ExceptionDuringTextureInitialization
            String message = Logging.getMessage("generic.IOExceptionDuringTextureInitialization");
            Logging.logger().log(Level.SEVERE, message, e);
            this.textureInitializationFailed = true;
            return null;
        }

        this.width = t.getWidth();
        this.height = t.getHeight();
        this.texCoords = t.getImageTexCoords();

        return t;
    }

    protected void updateIfNeeded(DrawContext dc)
    {
    }
}
