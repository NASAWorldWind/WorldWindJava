/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
