/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.gl2.GLUgl2;
import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.Iterator;

/**
 * A simplified version of {@link GeographicTextRenderer} that participates in globe text decluttering. See {@link
 * ClutterFilter} for more information on decluttering.
 *
 * @author tag
 * @version $Id: DeclutteringTextRenderer.java 2392 2014-10-20 20:02:44Z tgaskins $
 */
public class DeclutteringTextRenderer
{
    protected static final Font DEFAULT_FONT = Font.decode("Arial-PLAIN-12");
    protected static final Color DEFAULT_COLOR = Color.white;

    protected final GLU glu = new GLUgl2();

    // Flag indicating a JOGL text rendering problem. Set to avoid continual exception logging.
    protected boolean hasJOGLv111Bug = false;

    public Font getDefaultFont()
    {
        return DEFAULT_FONT;
    }

    /**
     * Returns either a cached or new text renderer for a specified font.
     *
     * @param dc   the current draw context.
     * @param font the text font.
     *
     * @return a text renderer.
     */
    public TextRenderer getTextRenderer(DrawContext dc, Font font)
    {
        return OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
    }

    /**
     * Adds ordered renderables to the ordered renderable list.
     *
     * @param dc           the current draw context.
     * @param textIterable a collection of text shapes to add to the ordered-renderable list.
     */
    public void render(DrawContext dc, Iterable<? extends GeographicText> textIterable)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (textIterable == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList surfaceGeometry = dc.getSurfaceGeometry();
        if (surfaceGeometry == null)
            return;

        Iterator<? extends GeographicText> iterator = textIterable.iterator();
        if (!iterator.hasNext())
            return;

        Frustum frustumInModelCoords = dc.getView().getFrustumInModelCoordinates();
        double horizon = dc.getView().getHorizonDistance();

        while (iterator.hasNext())
        {
            GeographicText text = iterator.next();

            if (!isTextValid(text, true))
                continue;

            if (!text.isVisible())
                continue;

            if (dc.is2DGlobe())
            {
                Sector limits = ((Globe2D)dc.getGlobe()).getProjection().getProjectionLimits();
                if (limits != null && !limits.contains(text.getPosition()))
                    continue;
            }

            Angle lat = text.getPosition().getLatitude();
            Angle lon = text.getPosition().getLongitude();

            if (!dc.getVisibleSector().contains(lat, lon))
                continue;

            Vec4 textPoint = surfaceGeometry.getSurfacePoint(lat, lon,
                text.getPosition().getElevation() * dc.getVerticalExaggeration());
            if (textPoint == null)
                continue;

            double eyeDistance = dc.getView().getEyePoint().distanceTo3(textPoint);
            if (!dc.is2DGlobe() && eyeDistance > horizon)
                continue;

            if (!frustumInModelCoords.contains(textPoint))
                continue;

            dc.addOrderedRenderable(new DeclutterableText(text, textPoint, eyeDistance, this));
        }
    }

    protected void beginRendering(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        int attribBits =
            GL2.GL_ENABLE_BIT // for enable/disable changes
                | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL2.GL_CURRENT_BIT      // for current color
                | GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth func, and depth mask
                | GL2.GL_TRANSFORM_BIT    // for modelview and perspective
                | GL2.GL_VIEWPORT_BIT;    // for depth range
        gl.glPushAttrib(attribBits);

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, dc.getView().getViewport().width, 0, dc.getView().getViewport().height);

        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        // Enable the depth test but don't write to the depth buffer.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // Suppress polygon culling.
        gl.glDisable(GL.GL_CULL_FACE);

        // Suppress any fully transparent image pixels
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);
    }

    protected void endRendering(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }

    protected Vec4 drawText(DrawContext dc, DeclutterableText uText, double scale, double opacity) throws Exception
    {
        if (uText.getPoint() == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            return null;
        }

        GeographicText geographicText = uText.getText();
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        final CharSequence charSequence = geographicText.getText();
        if (charSequence == null)
            return null;

        final Vec4 screenPoint = dc.getView().project(uText.getPoint());
        if (screenPoint == null)
            return null;

        Font font = geographicText.getFont();
        if (font == null)
            font = DEFAULT_FONT;

        TextRenderer textRenderer = this.getTextRenderer(dc, font);

        this.beginRendering(dc);
        try
        {
            textRenderer.begin3DRendering();

            this.setDepthFunc(dc, screenPoint);

            Rectangle2D textBounds = uText.getBounds(dc);
            if (textBounds == null)
                return null;

            Point.Float drawPoint = this.computeDrawPoint(textBounds, screenPoint);

            if (drawPoint != null)
            {
                if (scale != 1d)
                {
                    gl.glScaled(scale, scale, 1d);
                    drawPoint.setLocation(drawPoint.x / (float) scale, drawPoint.y / (float) scale);
                }

                Color color = geographicText.getColor();
                if (color == null)
                    color = DEFAULT_COLOR;
                color = this.applyOpacity(color, opacity);

                Color background = geographicText.getBackgroundColor();
                if (background != null)
                {
                    background = this.applyOpacity(background, opacity);
                    textRenderer.setColor(background);
                    textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y - 1, 0, 1);
                }

                textRenderer.setColor(color);
                textRenderer.draw3D(charSequence, drawPoint.x, drawPoint.y, 0, 1);
                textRenderer.flush();

                if (scale != 1d)
                    gl.glLoadIdentity();
            }
        }
        catch (Exception e)
        {
            handleTextRendererExceptions(e);
        }
        finally
        {
            textRenderer.end3DRendering();
            this.endRendering(dc);
        }

        return screenPoint;
    }

    protected void setDepthFunc(DrawContext dc, Vec4 screenPoint)
    {
        GL gl = dc.getGL();

        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
            return;
        }

        double altitude = eyePos.getElevation();
        if (altitude < (dc.getGlobe().getMaxElevation() * dc.getVerticalExaggeration()))
        {
            double depth = screenPoint.z - (8d * 0.00048875809d);
            depth = depth < 0d ? 0d : (depth > 1d ? 1d : depth);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthRange(depth, depth);
        }
        else
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
        }
    }

    /**
     * Computes the final draw point for the given rectangle lower left corner and target screen point. If the returned
     * point is <code>null</code> the text will not be drawn.
     *
     * @param rect        the text rectangle to draw.
     * @param screenPoint the projected screen point the text relates to.
     *
     * @return the final draw point for the given rectangle lower left corner or <code>null</code>.
     */
    protected Point.Float computeDrawPoint(Rectangle2D rect, Vec4 screenPoint)
    {
        return new Point.Float((float) (screenPoint.x - rect.getWidth() / 2d), (float) (screenPoint.y));
    }

    protected static boolean isTextValid(GeographicText text, boolean checkPosition)
    {
        if (text == null || text.getText() == null)
            return false;

        if (checkPosition && text.getPosition() == null)
            return false;

        return true;
    }

    protected Color applyOpacity(Color color, double opacity)
    {
        if (opacity >= 1)
            return color;

        float[] compArray = color.getRGBComponents(null);
        return new Color(compArray[0], compArray[1], compArray[2], compArray[3] * (float) opacity);
    }

    protected Rectangle2D computeTextBounds(DrawContext dc, DeclutterableText text) throws Exception
    {
        GeographicText geographicText = text.getText();

        final CharSequence charSequence = geographicText.getText();
        if (charSequence == null)
            return null;

        final Vec4 screenPoint = dc.getView().project(text.getPoint());
        if (screenPoint == null)
            return null;

        Font font = geographicText.getFont();
        if (font == null)
            font = this.getDefaultFont();

        try
        {
            TextRenderer textRenderer = this.getTextRenderer(dc, font);

            Rectangle2D textBound = textRenderer.getBounds(charSequence);
            double x = screenPoint.x - textBound.getWidth() / 2d;
            Rectangle2D bounds = new Rectangle2D.Float();
            bounds.setRect(x, screenPoint.y, textBound.getWidth(), textBound.getHeight());

            return bounds;
        }
        catch (Exception e)
        {
            handleTextRendererExceptions(e);
            return null;
        }
    }

    protected void handleTextRendererExceptions(Exception e) throws Exception
    {
        if (e instanceof IOException)
        {
            if (!this.hasJOGLv111Bug)
            {
                // This is likely a known JOGL 1.1.1 bug - see AMZN-287 or 343
                // Log once and then ignore.
                Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
                this.hasJOGLv111Bug = true;
            }
        }
        else
        {
            throw e;
        }
    }
}
