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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A linked GLSL program. The program must be created, used, and deleted while its GL context is current.
 */
public class GLSLProgram
{
    protected int program;

    protected GLSLProgram(int program)
    {
        this.program = program;
    }

    /**
     * Loads vertex and fragment shaders from classpath resources and links them into a program.
     *
     * @param gl the current GL interface supporting shader objects.
     * @param resourceOwner class used to locate the shader resources.
     * @param vertexResource classpath resource name for the vertex shader.
     * @param fragmentResource classpath resource name for the fragment shader.
     *
     * @return the linked shader program.
     *
     * @throws IOException if a shader resource cannot be read.
     * @throws GLException if shader compilation or program linking fails.
     */
    public static GLSLProgram fromResources(GL2ES2 gl, Class<?> resourceOwner, String vertexResource,
        String fragmentResource) throws IOException
    {
        if (gl == null || resourceOwner == null || vertexResource == null || fragmentResource == null)
            throw new IllegalArgumentException("GL interface, resource owner, and shader resource names are required");

        String vertexSource = readResource(resourceOwner, vertexResource);
        String fragmentSource = readResource(resourceOwner, fragmentResource);
        return create(gl, vertexSource, fragmentSource, vertexResource, fragmentResource);
    }

    /**
     * Compiles the specified vertex and fragment shaders and links them into a program.
     *
     * @param gl the current GL interface supporting shader objects.
     * @param vertexSource vertex shader source.
     * @param fragmentSource fragment shader source.
     *
     * @return the linked shader program.
     *
     * @throws GLException if shader compilation or program linking fails.
     */
    public static GLSLProgram create(GL2ES2 gl, String vertexSource, String fragmentSource)
    {
        return create(gl, vertexSource, fragmentSource, "vertex shader", "fragment shader");
    }

    protected static GLSLProgram create(GL2ES2 gl, String vertexSource, String fragmentSource,
        String vertexLabel, String fragmentLabel)
    {
        if (gl == null || vertexSource == null || fragmentSource == null)
            throw new IllegalArgumentException("GL interface and shader sources are required");

        int vertexShader = compileShader(gl, GL2ES2.GL_VERTEX_SHADER, vertexSource, vertexLabel);
        int fragmentShader = 0;
        int program = 0;

        try
        {
            fragmentShader = compileShader(gl, GL2ES2.GL_FRAGMENT_SHADER, fragmentSource, fragmentLabel);
            program = gl.glCreateProgram();
            if (program == 0)
                throw new GLException("Unable to create GLSL program");

            gl.glAttachShader(program, vertexShader);
            gl.glAttachShader(program, fragmentShader);
            gl.glLinkProgram(program);

            int[] status = new int[1];
            gl.glGetProgramiv(program, GL2ES2.GL_LINK_STATUS, status, 0);
            if (status[0] == GL.GL_FALSE)
                throw new GLException("Unable to link GLSL program: " + getProgramInfoLog(gl, program));

            return new GLSLProgram(program);
        }
        catch (RuntimeException e)
        {
            if (program != 0)
                gl.glDeleteProgram(program);
            throw e;
        }
        finally
        {
            gl.glDeleteShader(vertexShader);
            if (fragmentShader != 0)
                gl.glDeleteShader(fragmentShader);
        }
    }

    protected static int compileShader(GL2ES2 gl, int type, String source, String label)
    {
        int shader = gl.glCreateShader(type);
        if (shader == 0)
            throw new GLException("Unable to create " + label);

        try
        {
            gl.glShaderSource(shader, 1, new String[] {source}, new int[] {source.length()}, 0);
            gl.glCompileShader(shader);

            int[] status = new int[1];
            gl.glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, status, 0);
            if (status[0] == GL.GL_FALSE)
                throw new GLException("Unable to compile " + label + ": " + getShaderInfoLog(gl, shader));

            return shader;
        }
        catch (RuntimeException e)
        {
            gl.glDeleteShader(shader);
            throw e;
        }
    }

    protected static String readResource(Class<?> owner, String resourceName) throws IOException
    {
        try (InputStream stream = owner.getResourceAsStream(resourceName))
        {
            if (stream == null)
                throw new IOException("Unable to find GLSL shader resource: " + resourceName);

            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected static String getShaderInfoLog(GL2ES2 gl, int shader)
    {
        int[] length = new int[1];
        gl.glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, length, 0);
        byte[] log = new byte[Math.max(length[0], 1)];
        gl.glGetShaderInfoLog(shader, log.length, length, 0, log, 0);
        return new String(log, 0, Math.min(length[0], log.length), StandardCharsets.UTF_8).trim();
    }

    protected static String getProgramInfoLog(GL2ES2 gl, int program)
    {
        int[] length = new int[1];
        gl.glGetProgramiv(program, GL2ES2.GL_INFO_LOG_LENGTH, length, 0);
        byte[] log = new byte[Math.max(length[0], 1)];
        gl.glGetProgramInfoLog(program, log.length, length, 0, log, 0);
        return new String(log, 0, Math.min(length[0], log.length), StandardCharsets.UTF_8).trim();
    }

    public int getProgram()
    {
        return this.program;
    }

    public void use(GL2ES2 gl)
    {
        if (this.program == 0)
            throw new IllegalStateException("GLSL program has been disposed");

        gl.glUseProgram(this.program);
    }

    public void dispose(GL2ES2 gl)
    {
        if (this.program != 0)
        {
            gl.glDeleteProgram(this.program);
            this.program = 0;
        }
    }
}
