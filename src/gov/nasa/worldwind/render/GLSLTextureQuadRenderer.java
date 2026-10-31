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
 */
package gov.nasa.worldwind.render;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.cache.GpuResourceCache;

import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * Draws a textured screen-space quad using vertex attributes and a GLSL program.
 */
public class GLSLTextureQuadRenderer
{
    protected static final Object PROGRAM_CACHE_KEY = new Object();
    protected static final String VERTEX_SHADER = "shaders/TextureQuad.vert";
    protected static final String FRAGMENT_SHADER = "shaders/TextureQuad.frag";

    protected final Object vertexArrayCacheKey = new Object();

    /**
     * Draws a textured quad with a lower-left-origin screen rectangle.
     *
     * @param dc current draw context.
     * @param texture texture to draw.
     * @param x lower-left x coordinate in screen pixels.
     * @param y lower-left y coordinate in screen pixels.
     * @param width quad width in screen pixels.
     * @param height quad height in screen pixels.
     * @param opacity multiplier applied to texture alpha.
     */
    public void render(DrawContext dc, Texture texture, double x, double y, double width, double height,
        float opacity)
    {
        if (dc == null || texture == null)
            throw new IllegalArgumentException("Draw context and texture are required");
        if (width <= 0 || height <= 0 || dc.getDrawableWidth() <= 0 || dc.getDrawableHeight() <= 0)
            return;

        GL gl = dc.getGL();
        GLContext context = GLContext.getCurrent();
        if (!gl.isGL2ES3() || context == null || !isOpenGL33OrLater(context.getGLVersionNumber().getMajor(),
            context.getGLVersionNumber().getMinor()))
            throw new GLException("Textured GLSL quads require OpenGL 3.3 or later");

        GL2ES2 gl2es2 = gl.getGL2ES2();
        GL2ES3 gl2es3 = gl.getGL2ES3();
        GLSLProgram program = this.getProgram(dc, gl2es2);
        int[] vertexArrayAndBuffer = this.getVertexArrayAndBuffer(dc, gl2es3);

        int[] previousActiveTexture = new int[1];
        int[] previousTexture = new int[1];
        int[] previousBlendSource = new int[1];
        int[] previousBlendDestination = new int[1];
        int[] previousBlendSourceAlpha = new int[1];
        int[] previousBlendDestinationAlpha = new int[1];
        gl.glGetIntegerv(GL.GL_ACTIVE_TEXTURE, previousActiveTexture, 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glGetIntegerv(GL.GL_TEXTURE_BINDING_2D, previousTexture, 0);
        gl.glGetIntegerv(GL.GL_BLEND_SRC_RGB, previousBlendSource, 0);
        gl.glGetIntegerv(GL.GL_BLEND_DST_RGB, previousBlendDestination, 0);
        gl.glGetIntegerv(GL.GL_BLEND_SRC_ALPHA, previousBlendSourceAlpha, 0);
        gl.glGetIntegerv(GL.GL_BLEND_DST_ALPHA, previousBlendDestinationAlpha, 0);

        boolean depthTestEnabled = gl.glIsEnabled(GL.GL_DEPTH_TEST);
        boolean blendEnabled = gl.glIsEnabled(GL.GL_BLEND);
        int[] previousDepthMask = new int[1];
        gl.glGetIntegerv(GL.GL_DEPTH_WRITEMASK, previousDepthMask, 0);
        int[] previousProgram = new int[1];
        int[] previousVertexArray = new int[1];
        int[] previousArrayBuffer = new int[1];
        gl.glGetIntegerv(GL2ES2.GL_CURRENT_PROGRAM, previousProgram, 0);
        gl.glGetIntegerv(GL2ES3.GL_VERTEX_ARRAY_BINDING, previousVertexArray, 0);
        gl.glGetIntegerv(GL.GL_ARRAY_BUFFER_BINDING, previousArrayBuffer, 0);

        try
        {
            gl.glDisable(GL.GL_DEPTH_TEST);
            gl.glDepthMask(false);
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glActiveTexture(GL.GL_TEXTURE0);
            texture.bind(gl);

            program.use(gl2es2);
            gl2es2.glUniform1i(gl2es2.glGetUniformLocation(program.getProgram(), "uTexture"), 0);
            gl2es2.glUniform1f(gl2es2.glGetUniformLocation(program.getProgram(), "uOpacity"), opacity);
            gl2es2.glUniform2f(gl2es2.glGetUniformLocation(program.getProgram(), "uViewport"),
                dc.getDrawableWidth(), dc.getDrawableHeight());
            gl2es2.glUniform4f(gl2es2.glGetUniformLocation(program.getProgram(), "uRectangle"),
                (float) x, (float) y, (float) width, (float) height);

            TextureCoords texCoords = texture.getImageTexCoords();
            FloatBuffer vertices = Buffers.newDirectFloatBuffer(new float[] {
                0, 0, texCoords.left(), texCoords.bottom(),
                1, 0, texCoords.right(), texCoords.bottom(),
                0, 1, texCoords.left(), texCoords.top(),
                1, 1, texCoords.right(), texCoords.top()
            });

            gl2es3.glBindVertexArray(vertexArrayAndBuffer[0]);
            gl2es3.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexArrayAndBuffer[1]);
            gl2es3.glBufferData(GL.GL_ARRAY_BUFFER, (long) vertices.remaining() * Float.BYTES, vertices,
                GL2ES2.GL_STREAM_DRAW);
            gl2es3.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
        }
        finally
        {
            gl2es3.glBindVertexArray(previousVertexArray[0]);
            gl2es3.glBindBuffer(GL.GL_ARRAY_BUFFER, previousArrayBuffer[0]);
            gl2es2.glUseProgram(previousProgram[0]);
            gl.glBindTexture(GL.GL_TEXTURE_2D, previousTexture[0]);
            gl.glActiveTexture(previousActiveTexture[0]);
            gl.glBlendFuncSeparate(previousBlendSource[0], previousBlendDestination[0],
                previousBlendSourceAlpha[0], previousBlendDestinationAlpha[0]);
            gl.glDepthMask(previousDepthMask[0] != 0);
            if (depthTestEnabled)
                gl.glEnable(GL.GL_DEPTH_TEST);
            else
                gl.glDisable(GL.GL_DEPTH_TEST);
            if (blendEnabled)
                gl.glEnable(GL.GL_BLEND);
            else
                gl.glDisable(GL.GL_BLEND);
        }
    }

    protected GLSLProgram getProgram(DrawContext dc, GL2ES2 gl)
    {
        GLSLProgram program = (GLSLProgram) dc.getGpuResourceCache().get(PROGRAM_CACHE_KEY);
        if (program != null)
            return program;

        try
        {
            program = GLSLProgram.fromResources(gl, GLSLTextureQuadRenderer.class, VERTEX_SHADER, FRAGMENT_SHADER);
        }
        catch (IOException e)
        {
            throw new GLException("Unable to load textured-quad shaders", e);
        }

        dc.getGpuResourceCache().put(PROGRAM_CACHE_KEY, program, GpuResourceCache.GLSL_PROGRAM, 1024);
        return program;
    }

    protected int[] getVertexArrayAndBuffer(DrawContext dc, GL2ES3 gl)
    {
        int[] ids = (int[]) dc.getGpuResourceCache().get(this.vertexArrayCacheKey);
        if (ids != null)
            return ids;

        ids = new int[2];
        int[] previousVertexArray = new int[1];
        int[] previousArrayBuffer = new int[1];
        gl.glGetIntegerv(GL2ES3.GL_VERTEX_ARRAY_BINDING, previousVertexArray, 0);
        gl.glGetIntegerv(GL.GL_ARRAY_BUFFER_BINDING, previousArrayBuffer, 0);

        gl.glGenVertexArrays(1, ids, 0);
        gl.glGenBuffers(1, ids, 1);
        gl.glBindVertexArray(ids[0]);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, ids[1]);
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 4 * Float.BYTES, 0L);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
        gl.glBindVertexArray(previousVertexArray[0]);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, previousArrayBuffer[0]);

        dc.getGpuResourceCache().put(this.vertexArrayCacheKey, ids, GpuResourceCache.VERTEX_ARRAYS,
            4 * Float.BYTES * 4);
        return ids;
    }

    protected static boolean isOpenGL33OrLater(int major, int minor)
    {
        return major > 3 || (major == 3 && minor >= 3);
    }
}
