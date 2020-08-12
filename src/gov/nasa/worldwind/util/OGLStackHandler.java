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

import com.jogamp.opengl.GL2;

/**
 * @author tag
 * @version $Id: OGLStackHandler.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OGLStackHandler
{
    private boolean attribsPushed;
    private boolean clientAttribsPushed;
    private boolean modelviewPushed;
    private boolean projectionPushed;
    private boolean texturePushed;

    public void clear()
    {
        this.attribsPushed = false;
        this.clientAttribsPushed = false;
        this.modelviewPushed = false;
        this.projectionPushed = false;
        this.texturePushed = false;
    }

    public boolean isActive()
    {
        return this.attribsPushed || this.clientAttribsPushed || this.modelviewPushed || this.projectionPushed
            || this.texturePushed;
    }

    public void pushAttrib(GL2 gl, int mask)
    {
        gl.glPushAttrib(mask);
        this.attribsPushed = true;
    }

    public void pushClientAttrib(GL2 gl, int mask)
    {
        gl.glPushClientAttrib(mask);
        this.clientAttribsPushed = true;
    }

    public void pushModelview(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        this.modelviewPushed = true;
    }

    public void pushProjection(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        this.projectionPushed = true;
    }

    public void pushTexture(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPushMatrix();
        this.texturePushed = true;
    }

    public void pop(GL2 gl)
    {
        if (this.attribsPushed)
        {
            gl.glPopAttrib();
            this.attribsPushed = false;
        }

        if (this.clientAttribsPushed)
        {
            gl.glPopClientAttrib();
            this.clientAttribsPushed = false;
        }

        if (this.modelviewPushed)
        {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPopMatrix();
            this.modelviewPushed = false;
        }

        if (this.projectionPushed)
        {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            this.projectionPushed = false;
        }

        if (this.texturePushed)
        {
            gl.glMatrixMode(GL2.GL_TEXTURE);
            gl.glPopMatrix();
            this.texturePushed = false;
        }
    }

    public void pushModelviewIdentity(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        this.modelviewPushed = true;
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }

    public void pushProjectionIdentity(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        this.projectionPushed = true;
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }

    public void pushTextureIdentity(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_TEXTURE);
        this.texturePushed = true;
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }
}
