/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.awt.*;

/**
 * Paints the sky color background depending on altitude.
 *
 * @author Patrick Murris
 * @version $Id: SkyColorLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SkyColorLayer extends RenderableLayer
{
    private Color color = new Color(73, 131, 204); // Sky blue
    private double fadeBottomAltitude = 50e3;
    private double fadeTopAltitude = 140e3;

    /**
     * Paints the sky color background depending on altitude
     */
    public SkyColorLayer() {
    }

    /**
     * Paints the sky color background depending on altitude
     * @param color the sky Color
     */
    public SkyColorLayer(Color color) {
        this.setSkyColor(color);
    }
    /**
     * Get the sky Color
     * @return  the sky color
     */
    public Color getSkyColor()
    {
        return this.color;
    }

    /**
     * Set the sky Color
     * @param color the sky color
     */
    public void setSkyColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.color = color;
    }

    /**
     * Get the bottom altitude for the fade effect (meters)
     * @return  the bottom altitude in meters
     */
    public double getFadeBottomAltitude()
    {
        return this.fadeBottomAltitude;
    }

    /**
     * Set the bottom altitude for the fade effect (meters)
     * @param alt the bottom altitude in meters
     */
    public void setFadeBottomAltitude(double alt)
    {
        this.fadeBottomAltitude = alt;
    }

    /**
     * Get the top altitude for the fade effect (meters)
     * @return  the top altitude in meters
     */
    public double getFadeTopAltitude()
    {
        return this.fadeTopAltitude;
    }

    /**
     * Set the top altitude for the fade effect (meters)
     * @param alt the top altitude in meters
     */
    public void setFadeTopAltitude(double alt)
    {
        this.fadeTopAltitude = alt;
    }

    public void doRender(DrawContext dc)
    {
        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
            return;

        double alt = eyePos.getElevation();
        if(alt > this.fadeTopAltitude)
            return;
        // Compute fade factor
        float fadeFactor = (alt < this.fadeBottomAltitude) ? 1f :
            (float)((this.fadeTopAltitude - alt) / (this.fadeTopAltitude - this.fadeBottomAltitude));

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
            // GL setup
            gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT
                | GL2.GL_COLOR_BUFFER_BIT
                | GL2.GL_ENABLE_BIT
                | GL2.GL_TRANSFORM_BIT
                | GL2.GL_VIEWPORT_BIT
                | GL2.GL_CURRENT_BIT);
            attribsPushed = true;

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -1, 1);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();
            gl.glScaled(viewport.width, viewport.height, 1d);

            // Set color
            Color cc = this.color;
            gl.glColor4d((float)cc.getRed() / 255f * fadeFactor,
                (float)cc.getGreen() / 255f * fadeFactor,
                (float)cc.getBlue() / 255f * fadeFactor,
                (float)cc.getAlpha() / 255f * fadeFactor);
            // Draw
            gl.glDisable(GL.GL_TEXTURE_2D);		// no textures
            dc.drawUnitQuad();
        }
        finally
        {
            if (projectionPushed)
            {
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.SkyColorLayer.Name");
    }

}
