/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.*;
import java.awt.*;

/**
 * Display an horizontal line across the viewport when a plane at a given elevation cuts through the view near plane.
 *
 * @author Patrick Murris
 * @version $Id: ScreenElevationLine.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenElevationLine implements Renderable
{
    private double elevation = 0;
    private Color color = Color.WHITE;
    private boolean enabled = true;

    /**
     * Get the line current elevation.
     *
     * @return the line current elevation.
     */
    public double getElevation()
    {
        return this.elevation;
    }

    /**
     * Set the line elevation.
     *
     * @param elevation the line elevation.
     */
    public void setElevation(double elevation)
    {
        this.elevation = elevation;
    }

    /**
     * Get the line color.
     *
     * @return the line color.
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the line color.
     *
     * @param color the line color.
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.color = color;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean state)
    {
        this.enabled = state;
    }

    public void render(DrawContext dc)
    {
        if (this.isEnabled())
            dc.addOrderedRenderable(new OrderedItem());
    }

    private class OrderedItem implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 1;
        }

        public void render(DrawContext dc)
        {
            draw(dc);
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            draw(dc);
        }
    }

    private void draw(DrawContext dc)
    {
        Double lineY = computeLineY(dc);
        if (lineY == null)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
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

            if (!dc.isPickingMode())
            {
                // Set color
                gl.glColor4ub((byte) this.color.getRed(), (byte) this.color.getGreen(),
                    (byte) this.color.getBlue(), (byte) this.color.getAlpha());
            }

            // Draw line
            gl.glBegin(GL2.GL_LINE_STRIP);
            gl.glVertex3d(0, lineY, 0);
            gl.glVertex3d(viewport.width, lineY, 0);
            gl.glEnd();
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

    private Double computeLineY(DrawContext dc)
    {
        Vec4 point = dc.getGlobe().computePointFromPosition(
            new Position(dc.getView().getEyePosition(), this.elevation));
        Vec4 direction = dc.getView().getForwardVector().perpendicularTo3(point); // Round globe only
        Vec4 intersection = dc.getView().getFrustumInModelCoordinates().getNear().intersect(new Line(point, direction));
        if (intersection != null)
        {
            Vec4 screenPoint = dc.getView().project(intersection);
            if (screenPoint != null)
                return screenPoint.y;
        }
        return null;
    }
}
