/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe2D;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import java.awt.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: GeographicTextRenderer.java 2392 2014-10-20 20:02:44Z tgaskins $
 */
public class GeographicTextRenderer
{
    private TextRenderer lastTextRenderer = null;
    private final GLU glu = new GLUgl2();

    private static final Font DEFAULT_FONT = Font.decode("Arial-PLAIN-12");
    private static final Color DEFAULT_COLOR = Color.white;
    private boolean cullText = false;
    private int cullTextMargin = 0;
    private String effect = AVKey.TEXT_EFFECT_SHADOW;

    // Distance scaling and fading
    private double distanceMinScale = 1d;
    private double distanceMaxScale = 1d;
    private double distanceMinOpacity = 1d;
    private boolean isDistanceScaling = false;
    private double lookAtDistance = 0;

    private boolean hasJOGLv111Bug = false;

    public GeographicTextRenderer()
    {
    }

    /**
     * Determines whether overlapping text are culled. If <code>true</code> text items are sorted front to back
     * according to their respective priority rather then back to front, and will be drawn only if they do not overlap
     * an already drawn text. If <code>false</code> all text will be drawn back to front whether they overlap or not.
     *
     * @return <code>true</code> if overlapping text are culled.
     */
    public boolean isCullTextEnabled()
    {
        return cullText;
    }

    /**
     * Set whether overlapping text should be culled. If <code>true</code> text items will be sorted front to back
     * according to their respective priority rather then back to front, and will be drawn only if they do not overlap
     * an already drawn text. If <code>false</code> all text will be drawn back to front whether they overlap or not.
     *
     * @param cullText <code>true</code> if overlapping text should be culled.
     */
    public void setCullTextEnabled(boolean cullText)
    {
        this.cullText = cullText;
    }

    /**
     * Get the empty margin that surrounds a text item when considering it's bounding rectangle during text culling -
     * see {@link #setCullTextEnabled(boolean)}. The smaller the margin, the closer text items can get before being
     * considered as overlapping.
     *
     * @return the empty margin that surrounds a text item - in pixels.
     */
    public int getCullTextMargin()
    {
        return this.cullTextMargin;
    }

    /**
     * Set the empty margin that surrounds a text item when considering it's bounding rectangle during text culling -
     * see {@link #setCullTextEnabled(boolean)}. The smaller the margin, the closer text items can get before being
     * considered as overlapping.
     *
     * @param margin the empty margin that surrounds a text item - in pixels.
     */
    public void setCullTextMargin(int margin)
    {
        this.cullTextMargin = margin;
    }

    /**
     * Get the effect used to decorate the text. Can be one of {@link AVKey#TEXT_EFFECT_SHADOW} (default), {@link
     * AVKey#TEXT_EFFECT_OUTLINE} or {@link AVKey#TEXT_EFFECT_NONE}.
     *
     * @return the effect used for text rendering.
     */
    public String getEffect()
    {
        return this.effect;
    }

    /**
     * Set the effect used to decorate the text. Can be one of {@link AVKey#TEXT_EFFECT_SHADOW} (default), {@link
     * AVKey#TEXT_EFFECT_OUTLINE} or {@link AVKey#TEXT_EFFECT_NONE}.
     *
     * @param effect the effect to use for text rendering.
     */
    public void setEffect(String effect)
    {
        if (effect == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        this.effect = effect;
    }

    /**
     * Get the minimum scale that can be applied to a text item when it gets farther away from the eye than the view
     * lookat point - screen center.
     *
     * @return the minimum scale that can be applied to a text item when it gets away from the eye.
     */
    public double getDistanceMinScale()
    {
        return this.distanceMinScale;
    }

    /**
     * Set the minimum scale that can be applied to a text item when it gets farther away from the eye than the view
     * lookat point - screen center. Use a value less then one to have the text 'fade' away. A value of one will have no
     * effect.
     *
     * @param scale the minimum scale that can be applied to a text item when it gets away from the eye.
     */
    public void setDistanceMinScale(double scale)
    {
        this.distanceMinScale = scale;
    }

    /**
     * Get the maximum scale that can be applied to a text item when it gets closer to the eye than the view lookat
     * point - screen center.
     *
     * @return the maximum scale that can be applied to a text item when it closer to the eye.
     */
    public double getDistanceMaxScale()
    {
        return this.distanceMaxScale;
    }

    /**
     * Set the maximum scale that can be applied to a text item when it gets closer to the eye than the view lookat
     * point - screen center. Use a value greater then one to have the text magnified in the foreground. A value of one
     * will have no effect.
     *
     * @param scale the maximum scale that can be applied to a text item when it closer to the eye.
     */
    public void setDistanceMaxScale(double scale)
    {
        this.distanceMaxScale = scale;
    }

    /**
     * Get the minimum opacity that can be applied to a text item when it gets farther away from the eye than the view
     * lookat point - screen center.
     *
     * @return the minimum opacity that can be applied to a text item when it gets away from the eye.
     */
    public double getDistanceMinOpacity()
    {
        return this.distanceMinOpacity;
    }

    /**
     * Set the minimum opacity that can be applied to a text item when it gets farther away from the eye than the view
     * lookat point - screen center. Use a value less then one to have the text 'fade' away. A value of one will have no
     * effect.
     *
     * @param opacity the minimum opacity that can be applied to a text item when it gets away from the eye.
     */
    public void setDistanceMinOpacity(double opacity)
    {
        this.distanceMinOpacity = opacity;
    }

    public void render(DrawContext dc, Iterable<GeographicText> text)
    {
        this.drawMany(dc, text);
    }

    public void render(DrawContext dc, GeographicText text, Vec4 textPoint)
    {
        if (!isTextValid(text, false))
            return;

        this.drawOne(dc, text, textPoint);
    }

    private void drawMany(DrawContext dc, Iterable<GeographicText> textIterable)
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

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return;

        Iterator<GeographicText> iterator = textIterable.iterator();
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

            Vec4 textPoint = geos.getSurfacePoint(lat, lon,
                text.getPosition().getElevation() * dc.getVerticalExaggeration());
            if (textPoint == null)
                continue;

            double eyeDistance = dc.getView().getEyePoint().distanceTo3(textPoint);
            if (!dc.is2DGlobe() && eyeDistance > horizon)
                continue;

            if (!frustumInModelCoords.contains(textPoint))
                continue;

            dc.addOrderedRenderable(new OrderedText(text, textPoint, eyeDistance));
        }
    }

    private void drawOne(DrawContext dc, GeographicText text, Vec4 textPoint)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }
        if (dc.getView() == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return;

        if (!text.isVisible())
            return;

        if (textPoint == null)
        {
            if (text.getPosition() == null)
                return;

            Angle lat = text.getPosition().getLatitude();
            Angle lon = text.getPosition().getLongitude();

            if (!dc.getVisibleSector().contains(lat, lon))
                return;

            textPoint = geos.getSurfacePoint(lat, lon,
                text.getPosition().getElevation() * dc.getVerticalExaggeration());
            if (textPoint == null)
                return;
        }

        double horizon = dc.getView().getHorizonDistance();
        double eyeDistance = dc.getView().getEyePoint().distanceTo3(textPoint);
        if (!dc.is2DGlobe() && eyeDistance > horizon)
            return;

        if (!dc.getView().getFrustumInModelCoordinates().contains(textPoint))
            return;

        dc.addOrderedRenderable(new OrderedText(text, textPoint, eyeDistance));
    }

    protected static boolean isTextValid(GeographicText text, boolean checkPosition)
    {
        if (text == null || text.getText() == null)
            return false;

        //noinspection RedundantIfStatement
        if (checkPosition && text.getPosition() == null)
            return false;

        return true;
    }

    protected class OrderedText implements OrderedRenderable, Comparable<OrderedText>
    {
        GeographicText text;
        Vec4 point;
        double eyeDistance;

        OrderedText(GeographicText text, Vec4 point, double eyeDistance)
        {
            this.text = text;
            this.point = point;
            this.eyeDistance = eyeDistance;
        }

        // When overlapping text are culled we want to sort them front to back by priority.
        public int compareTo(OrderedText t)
        {
            if (t.text.getPriority() - this.text.getPriority() == 0)
            {
                return (int) (this.eyeDistance - t.eyeDistance);
            }
            else
                return (int) (t.text.getPriority() - this.text.getPriority());
        }

        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        private GeographicTextRenderer getRenderer()
        {
            return GeographicTextRenderer.this;
        }

        public void render(DrawContext dc)
        {
            GeographicTextRenderer.this.beginRendering(dc);
            try
            {
                if (cullText)
                {
                    ArrayList<OrderedText> textList = new ArrayList<OrderedText>();
                    textList.add(this);

                    // Draw as many as we can in a batch to save ogl state switching.
                    Object nextItem = dc.peekOrderedRenderables();
                    while (nextItem != null && nextItem instanceof OrderedText)
                    {
                        OrderedText ot = (OrderedText) nextItem;
                        if (ot.getRenderer() != GeographicTextRenderer.this)
                            break;

                        textList.add(ot);
                        dc.pollOrderedRenderables(); // take it off the queue
                        nextItem = dc.peekOrderedRenderables();
                    }

                    Collections.sort(textList); // sort for rendering priority then front to back

                    ArrayList<Rectangle2D> textBounds = new ArrayList<Rectangle2D>();
                    for (OrderedText ot : textList)
                    {
                        double[] scaleAndOpacity = GeographicTextRenderer.this.computeDistanceScaleAndOpacity(dc, ot);
                        Rectangle2D newBounds = GeographicTextRenderer.this.computeTextBounds(dc, ot,
                            scaleAndOpacity[0]);
                        if (newBounds == null)
                            continue;

                        boolean overlap = false;
                        newBounds = GeographicTextRenderer.this.computeExpandedBounds(newBounds, cullTextMargin);
                        for (Rectangle2D rect : textBounds)
                        {
                            if (rect.intersects(newBounds))
                                overlap = true;
                        }

                        if (!overlap)
                        {
                            textBounds.add(newBounds);
                            GeographicTextRenderer.this.drawText(dc, ot, scaleAndOpacity[0], scaleAndOpacity[1]);
                        }
                    }
                }
                else //just draw each label
                {
                    double[] scaleAndOpacity = GeographicTextRenderer.this.computeDistanceScaleAndOpacity(dc, this);
                    GeographicTextRenderer.this.drawText(dc, this, scaleAndOpacity[0], scaleAndOpacity[1]);
                    // Draw as many as we can in a batch to save ogl state switching.
                    Object nextItem = dc.peekOrderedRenderables();
                    while (nextItem != null && nextItem instanceof OrderedText)
                    {
                        OrderedText ot = (OrderedText) nextItem;
                        if (ot.getRenderer() != GeographicTextRenderer.this)
                            break;

                        scaleAndOpacity = GeographicTextRenderer.this.computeDistanceScaleAndOpacity(dc, ot);
                        GeographicTextRenderer.this.drawText(dc, ot, scaleAndOpacity[0], scaleAndOpacity[1]);
                        dc.pollOrderedRenderables(); // take it off the queue
                        nextItem = dc.peekOrderedRenderables();
                    }
                }
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
            }
            finally
            {
                GeographicTextRenderer.this.endRendering(dc);
            }
        }

        public void pick(DrawContext dc, java.awt.Point pickPoint)
        {
        }
    }

    protected Rectangle2D computeTextBounds(DrawContext dc, OrderedText uText, double scale) throws Exception
    {
        GeographicText geographicText = uText.text;

        final CharSequence charSequence = geographicText.getText();
        if (charSequence == null)
            return null;

        final Vec4 screenPoint = dc.getView().project(uText.point);
        if (screenPoint == null)
            return null;

        Font font = geographicText.getFont();
        if (font == null)
            font = DEFAULT_FONT;

        try
        {
            TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
            if (textRenderer != this.lastTextRenderer)
            {
                if (this.lastTextRenderer != null)
                    this.lastTextRenderer.end3DRendering();
                textRenderer.begin3DRendering();
                this.lastTextRenderer = textRenderer;
            }

            Rectangle2D textBound = textRenderer.getBounds(charSequence);
            double x = screenPoint.x - textBound.getWidth() / 2d;
            Rectangle2D bounds = new Rectangle2D.Float();
            bounds.setRect(x, screenPoint.y, textBound.getWidth(), textBound.getHeight());

            return computeScaledBounds(bounds, scale);
        }
        catch (Exception e)
        {
            handleTextRendererExceptions(e);
            return null;
        }
    }

    protected Rectangle2D computeScaledBounds(Rectangle2D bounds, double scale)
    {
        if (scale == 1)
            return bounds;

        // Scale rectangle from bottom center
        double halfWidth = bounds.getWidth() / 2;
        bounds.setRect(bounds.getX() + halfWidth - halfWidth * scale, bounds.getY(),
            bounds.getWidth() * scale, bounds.getHeight() * scale);
        return bounds;
    }

    protected Rectangle2D computeExpandedBounds(Rectangle2D bounds, int margin)
    {
        if (margin == 0)
            return bounds;

        // Add margin around rectangle
        bounds.setRect(bounds.getX() - margin, bounds.getY() - margin,
            bounds.getWidth() + margin * 2, bounds.getHeight() + margin * 2);
        return bounds;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected double[] computeDistanceScaleAndOpacity(DrawContext dc, OrderedText ot)
    {
        if (!this.isDistanceScaling)
            return new double[] {1, 1};

        // Determine scale and opacity factors based on distance from eye vs the distance to the look at point.
        double lookAtDistance = this.lookAtDistance;
        double eyeDistance = ot.getDistanceFromEye();
        double distanceFactor = Math.sqrt(lookAtDistance / eyeDistance);
        double scale = WWMath.clamp(distanceFactor,
            this.getDistanceMinScale(), this.getDistanceMaxScale());
        double opacity = WWMath.clamp(distanceFactor,
            this.getDistanceMinOpacity(), 1);

        return new double[] {scale, opacity};
    }

    protected double computeLookAtDistance(DrawContext dc)
    {
        View view = dc.getView();

        // Get point in the middle of the screen
        // TODO: Get a point on the surface rather then the geoid
        Position groundPos = view.computePositionFromScreenPoint(
            view.getViewport().getCenterX(), view.getViewport().getCenterY());

        // Update look at distance if center point found
        if (groundPos != null)
        {
            // Compute distance from eye to the position in the middle of the screen
            this.lookAtDistance = view.getEyePoint().distanceTo3(dc.getGlobe().computePointFromPosition(groundPos));
        }

        return this.lookAtDistance;
    }

    protected void beginRendering(DrawContext dc)
    {
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
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        // Set model view as current matrix mode
        gl.glMatrixMode(GL2.GL_MODELVIEW);

        // Enable the depth test but don't write to the depth buffer.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // Suppress polygon culling.
        gl.glDisable(GL.GL_CULL_FACE);

        // Suppress any fully transparent image pixels
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

        // Cache distance scaling values
        this.isDistanceScaling = this.getDistanceMinScale() != 1 || this.getDistanceMaxScale() != 1
            || this.distanceMinOpacity != 1;
        this.computeLookAtDistance(dc);
    }

    protected void endRendering(DrawContext dc)
    {
        if (this.lastTextRenderer != null)
        {
            this.lastTextRenderer.end3DRendering();
            this.lastTextRenderer = null;
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

    protected Vec4 drawText(DrawContext dc, OrderedText uText, double scale, double opacity) throws Exception
    {
        if (uText.point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            return null;
        }

        GeographicText geographicText = uText.text;
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        final CharSequence charSequence = geographicText.getText();
        if (charSequence == null)
            return null;

        final Vec4 screenPoint = dc.getView().project(uText.point);
        if (screenPoint == null)
            return null;

        Font font = geographicText.getFont();
        if (font == null)
            font = DEFAULT_FONT;

        try
        {
            TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
            if (textRenderer != this.lastTextRenderer)
            {
                if (this.lastTextRenderer != null)
                    this.lastTextRenderer.end3DRendering();
                textRenderer.begin3DRendering();
                this.lastTextRenderer = textRenderer;
            }

            this.setDepthFunc(dc, uText, screenPoint);

            Rectangle2D textBounds = textRenderer.getBounds(
                charSequence);//note:may already be calculated during culling
            textBounds = this.computeScaledBounds(textBounds, scale);
            Point.Float drawPoint = computeDrawPoint(dc, textBounds, screenPoint);

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
                    if (this.effect.equals(AVKey.TEXT_EFFECT_SHADOW))
                    {
                        textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y - 1, 0, 1);
                    }
                    else if (this.effect.equals(AVKey.TEXT_EFFECT_OUTLINE))
                    {
                        textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y - 1, 0, 1);
                        textRenderer.draw3D(charSequence, drawPoint.x + 1, drawPoint.y + 1, 0, 1);
                        textRenderer.draw3D(charSequence, drawPoint.x - 1, drawPoint.y - 1, 0, 1);
                        textRenderer.draw3D(charSequence, drawPoint.x - 1, drawPoint.y + 1, 0, 1);
                    }
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

        return screenPoint;
    }

    protected Color applyOpacity(Color color, double opacity)
    {
        if (opacity >= 1)
            return color;

        float[] compArray = color.getRGBComponents(null);
        return new Color(compArray[0], compArray[1], compArray[2], compArray[3] * (float) opacity);
    }

    private void handleTextRendererExceptions(Exception e) throws Exception
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

    /**
     * Computes the final draw point for the given rectangle lower left corner and target screen point. If the returned
     * point is <code>null</code> the text will not be drawn.
     *
     * @param dc          the current {@link DrawContext}
     * @param rect        the text rectangle to draw.
     * @param screenPoint the projected screen point the text relates to.
     *
     * @return the final draw point for the given rectangle lower left corner or <code>null</code>.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Point.Float computeDrawPoint(DrawContext dc, Rectangle2D rect, Vec4 screenPoint)
    {
        return new Point.Float((float) (screenPoint.x - rect.getWidth() / 2d), (float) (screenPoint.y));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void setDepthFunc(DrawContext dc, OrderedText uText, Vec4 screenPoint)
    {
        GL gl = dc.getGL();

        //if (uText.text.isAlwaysOnTop())
        //{
        //    gl.glDepthFunc(GL.GL_ALWAYS);
        //    return;
        //}

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
        //else if (screenPoint.z >= 1d)
        //{
        //    gl.glDepthFunc(GL.GL_EQUAL);
        //    gl.glDepthRange(1d, 1d);
        //}
        else
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
        }
    }
}
