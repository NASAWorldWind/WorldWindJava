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

package gov.nasa.worldwindx.examples;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

/**
 * Determines whether a device supports the OpenGL features necessary for WorldWind.
 *
 * @author tag
 * @version $Id: OpenGLSupportTest.java 1675 2013-10-18 00:32:15Z tgaskins $
 */
public class OpenGLSupportTest implements GLEventListener
{
    @Override
    public void init(GLAutoDrawable glAutoDrawable)
    {
        int status = 0;

        for (String funcName : this.getRequiredOglFunctions())
        {
            if (!glAutoDrawable.getGL().isFunctionAvailable(funcName))
            {
                System.out.println("OpenGL function " + funcName + " is not available.");
                status = 1;
            }
        }

        for (String extName : this.getRequiredOglExtensions())
        {
            if (!glAutoDrawable.getGL().isExtensionAvailable(extName))
            {
                System.out.println("OpenGL extension " + extName + " is not available.");
                status = 2;
            }
        }

        GLCapabilitiesImmutable caps = glAutoDrawable.getChosenGLCapabilities();
        if (caps.getAlphaBits() != 8 || caps.getRedBits() != 8 || caps.getGreenBits() != 8 || caps.getBlueBits() != 8)
        {
            System.out.println("Device canvas color depth is inadequate.");
            status = 3;
        }

        if (caps.getDepthBits() < 16)
        {
            System.out.println("Device canvas depth buffer depth is inadequate.");
            status = 4;
        }

        if (caps.getDoubleBuffered() == false)
        {
            System.out.println("Device canvas is not double buffered.");
            status = 5;
        }

        System.exit(status);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable)
    {
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable)
    {
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3)
    {
    }

    protected String[] getRequiredOglFunctions()
    {
        return new String[] {"glActiveTexture", "glClientActiveTexture"};
    }

    protected String[] getRequiredOglExtensions()
    {
        return new String[] {"GL_EXT_texture_compression_s3tc"};
    }

    public static void main(String[] args)
    {
        java.awt.Frame frame = new java.awt.Frame("OpenGL Support Test");
        frame.setSize(200, 200);
        frame.setLayout(new java.awt.BorderLayout());

        GLCapabilities caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));

        caps.setAlphaBits(8);
        caps.setRedBits(8);
        caps.setGreenBits(8);
        caps.setBlueBits(8);
        caps.setDepthBits(24);
        caps.setDoubleBuffered(true);
        GLCanvas canvas = new GLCanvas(caps);

        OpenGLSupportTest testClass = new OpenGLSupportTest();
        canvas.addGLEventListener(testClass);

        frame.add(canvas, java.awt.BorderLayout.CENTER);
        frame.validate();
        frame.setVisible(true);
    }
}
