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
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GLSLProgramTest
{
    @Test
    public void testCreateUseAndDispose()
    {
        List<String> calls = new ArrayList<String>();
        GL2ES2 gl = createGL(true, calls);

        GLSLProgram program = GLSLProgram.create(gl, "vertex", "fragment");
        assertEquals(3, program.getProgram());

        program.use(gl);
        program.dispose(gl);
        assertEquals(0, program.getProgram());
        assertEquals(true, calls.contains("use:3"));
        assertEquals(true, calls.contains("deleteShader:1"));
        assertEquals(true, calls.contains("deleteShader:2"));
        assertEquals(true, calls.contains("deleteProgram:3"));
    }

    @Test
    public void testCompileFailureDeletesShader()
    {
        List<String> calls = new ArrayList<String>();
        GL2ES2 gl = createGL(false, calls);

        try
        {
            GLSLProgram.create(gl, "invalid vertex shader", "fragment");
            fail("Expected shader compilation to fail");
        }
        catch (GLException expected)
        {
            assertEquals(true, expected.getMessage().contains("vertex shader"));
            assertEquals(true, calls.contains("deleteShader:1"));
            assertEquals(false, calls.contains("createProgram"));
        }
    }

    @Test
    public void testLoadShaderResources() throws Exception
    {
        List<String> calls = new ArrayList<String>();
        GLSLProgram program = GLSLProgram.fromResources(createGL(true, calls), GLSLTextureQuadRenderer.class,
            GLSLTextureQuadRenderer.VERTEX_SHADER, GLSLTextureQuadRenderer.FRAGMENT_SHADER);

        assertEquals(3, program.getProgram());
    }

    @Test
    public void testCoreQuadVersionRequirement()
    {
        assertEquals(false, GLSLTextureQuadRenderer.isOpenGL33OrLater(3, 2));
        assertEquals(true, GLSLTextureQuadRenderer.isOpenGL33OrLater(3, 3));
        assertEquals(true, GLSLTextureQuadRenderer.isOpenGL33OrLater(4, 0));
    }

    protected static GL2ES2 createGL(final boolean compileSucceeds, final List<String> calls)
    {
        InvocationHandler handler = new InvocationHandler()
        {
            private int nextShader = 1;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
            {
                String name = method.getName();
                if ("glCreateShader".equals(name))
                    return this.nextShader++;
                if ("glCompileShader".equals(name))
                    return null;
                if ("glGetShaderiv".equals(name))
                {
                    int[] value = (int[]) args[2];
                    value[(Integer) args[3]] = (Integer) args[1] == GL2ES2.GL_COMPILE_STATUS
                        ? (compileSucceeds ? GL.GL_TRUE : GL.GL_FALSE) : 0;
                    return null;
                }
                if ("glCreateProgram".equals(name))
                {
                    calls.add("createProgram");
                    return 3;
                }
                if ("glGetProgramiv".equals(name))
                {
                    int[] value = (int[]) args[2];
                    value[(Integer) args[3]] = GL.GL_TRUE;
                    return null;
                }
                if ("glUseProgram".equals(name))
                {
                    calls.add("use:" + args[0]);
                    return null;
                }
                if ("glDeleteShader".equals(name))
                {
                    calls.add("deleteShader:" + args[0]);
                    return null;
                }
                if ("glDeleteProgram".equals(name))
                {
                    calls.add("deleteProgram:" + args[0]);
                    return null;
                }

                return null;
            }
        };

        return (GL2ES2) Proxy.newProxyInstance(GL2ES2.class.getClassLoader(),
            new Class<?>[] {GL2ES2.class}, handler);
    }
}
