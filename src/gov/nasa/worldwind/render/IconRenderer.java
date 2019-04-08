/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * IconRenderer processes collections of {@link gov.nasa.worldwind.render.WWIcon} instances for picking and rendering.
 * IconRenderer applies batch processing techniques to improve the runtime performance of picking and rendering large
 * collections of icons.
 * <p/>
 * During the draw pass, IconRenderer records feedback information for each WWIcon which has the property key {@link
 * gov.nasa.worldwind.avlist.AVKey#FEEDBACK_ENABLED} set to <code>true</code>. IconRenderer does not record any feedback
 * information during the pick pass. When feedback is enabled, IconRenderer puts properties which describe how each
 * WWIcon has been processed in key-value pairs attached to the WWIcon. Any of these properties may be null, indicating
 * that processing of the WWIcon was terminated before this information became available. The feedback properties for
 * WWIcon are as follows: <table> <tr><th>Key</th><th>Description</th></tr> <tr><td>{@link
 * gov.nasa.worldwind.avlist.AVKey#FEEDBACK_REFERENCE_POINT}</td><td>The icon's reference point in model
 * coordinates.</td></tr> <tr><td>{@link gov.nasa.worldwind.avlist.AVKey#FEEDBACK_SCREEN_BOUNDS}</td><td>The icon's
 * bounding rectangle in screen coordinates.</td></tr> </table>
 *
 * @author tag
 * @version $Id: IconRenderer.java 2260 2014-08-23 00:14:06Z tgaskins $
 */
public class IconRenderer
{
    protected Pedestal pedestal;
    protected boolean horizonClippingEnabled = false;
    protected boolean viewClippingEnabled = true;
    protected boolean pickFrustumClippingEnabled = true;
    protected boolean alwaysUseAbsoluteElevation = false;
    protected OGLStackHandler oglStackHandler = new OGLStackHandler();
    protected boolean allowBatchPicking = true;

    protected PickSupport pickSupport = new PickSupport();

    public IconRenderer()
    {
    }

    public Pedestal getPedestal()
    {
        return pedestal;
    }

    public void setPedestal(Pedestal pedestal)
    {
        this.pedestal = pedestal;
    }

    /**
     * Indicates whether horizon clipping is performed.
     *
     * @return <code>true</code> if horizon clipping is performed, otherwise <code>false</code>.
     *
     * @see #setHorizonClippingEnabled(boolean)
     */
    public boolean isHorizonClippingEnabled()
    {
        return horizonClippingEnabled;
    }

    /**
     * Indicates whether to render icons beyond the horizon. If view culling is enabled, the icon is also tested for
     * view volume inclusion. The default is <code>false</code>, horizon clipping is not performed.
     *
     * @param horizonClippingEnabled <code>true</code> if horizon clipping should be performed, otherwise
     *                               <code>false</code>.
     *
     * @see #setViewClippingEnabled(boolean)
     */
    public void setHorizonClippingEnabled(boolean horizonClippingEnabled)
    {
        this.horizonClippingEnabled = horizonClippingEnabled;
    }

    /**
     * Indicates whether view volume clipping is performed.
     *
     * @return <code>true</code> if view volume clipping is performed, otherwise <code>false</code>.
     *
     * @see #setViewClippingEnabled(boolean)
     */
    public boolean isViewClippingEnabled()
    {
        return viewClippingEnabled;
    }

    /**
     * Indicates whether to render icons outside the view volume. This is primarily to control icon visibility beyond
     * the far view clipping plane. Some important use cases demand that clipping not be performed. If horizon clipping
     * is enabled, the icon is also tested for horizon clipping. The default is <code>true</code>, view volume clipping
     * is not performed.
     *
     * @param viewClippingEnabled <code>true</code> if view clipping should be performed, otherwise <code>false</code>.
     *
     * @see #setHorizonClippingEnabled(boolean)
     */
    public void setViewClippingEnabled(boolean viewClippingEnabled)
    {
        this.viewClippingEnabled = viewClippingEnabled;
    }

    /**
     * Indicates whether picking volume clipping is performed.
     *
     * @return <code>true</code> if picking volume clipping is performed, otherwise <code>false</code>.
     *
     * @see #setPickFrustumClippingEnabled(boolean)
     */
    public boolean isPickFrustumClippingEnabled()
    {
        return pickFrustumClippingEnabled;
    }

    /**
     * Indicates whether to render icons outside the picking volume when in pick mode. This increases performance by
     * only drawing the icons within the picking volume when picking is enabled. Some important use cases demand that
     * clipping not be performed. The default is <code>false</code>, picking volume clipping is not performed.
     *
     * @param pickFrustumClippingEnabled <code>true</code> if picking clipping should be performed, otherwise
     *                                   <code>false</code>.
     */
    public void setPickFrustumClippingEnabled(boolean pickFrustumClippingEnabled)
    {
        this.pickFrustumClippingEnabled = pickFrustumClippingEnabled;
    }

    protected static boolean isIconValid(WWIcon icon, boolean checkPosition)
    {
        if (icon == null || icon.getImageTexture() == null)
            return false;

        //noinspection RedundantIfStatement
        if (checkPosition && icon.getPosition() == null)
            return false;

        return true;
    }

    /**
     * Indicates whether an icon's elevation is treated as an offset from the terrain or an absolute elevation above sea
     * level.
     *
     * @return <code>true</code> if icon elevations are treated as absolute, <code>false</code> if they're treated as
     *         offsets from the terrain.
     */
    public boolean isAlwaysUseAbsoluteElevation()
    {
        return alwaysUseAbsoluteElevation;
    }

    /**
     * Normally, an icon's elevation is treated as an offset from the terrain when it is less than the globe's maximum
     * elevation. Setting #setAlwaysUseAbsoluteElevation to <code>true</code> causes the elevation to be treated as an
     * absolute elevation above sea level.
     *
     * @param alwaysUseAbsoluteElevation <code>true</code> to treat icon elevations as absolute, <code>false</code> to
     *                                   treat them as offsets from the terrain.
     */
    public void setAlwaysUseAbsoluteElevation(boolean alwaysUseAbsoluteElevation)
    {
        this.alwaysUseAbsoluteElevation = alwaysUseAbsoluteElevation;
    }

    /**
     * Indicates whether icons are picked as a batch and therefore a {@link gov.nasa.worldwind.event.SelectEvent} will
     * contain only one icon from a given layer. Batch picking is much faster than individual picking, so this attribute
     * should be used judiciously.
     *
     * @return true if batch picking is allowed, otherwise false.
     *
     * @see #setAllowBatchPicking(boolean)
     */
    public boolean isAllowBatchPicking()
    {
        return this.allowBatchPicking;
    }

    /**
     * Specifies whether batch picking is allowed. If so, a {@link gov.nasa.worldwind.event.SelectEvent} from a layer
     * will contain only one icon even if several overlapping icons are at the pick point. Batch picking is much faster
     * than individual picking so the default value is true.
     *
     * @param allowBatchPicking true if batch picking is allowed, otherwise false.
     */
    public void setAllowBatchPicking(boolean allowBatchPicking)
    {
        this.allowBatchPicking = allowBatchPicking;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void pick(DrawContext dc, Iterable<? extends WWIcon> icons, java.awt.Point pickPoint, Layer layer)
    {
        this.drawMany(dc, icons, layer);
    }

    public void render(DrawContext dc, Iterable<? extends WWIcon> icons)
    {
        this.drawMany(dc, icons, null);
    }

    protected void drawMany(DrawContext dc, Iterable<? extends WWIcon> icons, Layer layer)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        //noinspection RedundantIfStatement
        if (geos == null)
            return;

        if (icons == null)
        {
            String msg = Logging.getMessage("nullValue.IconIterator");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Iterator<? extends WWIcon> iterator = icons.iterator();

        if (!iterator.hasNext())
            return;

        double horizon = dc.getView().getHorizonDistance();

        while (iterator.hasNext())
        {
            WWIcon icon = iterator.next();
            if (!isIconValid(icon, true))
            {
                // Record feedback data for this WWIcon if feedback is enabled.
                if (icon != null)
                    this.recordFeedback(dc, icon, null, null);

                continue;
            }

            if (!icon.isVisible())
            {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, null, null);

                continue;
            }

            // Determine Cartesian position from the surface geometry if the icon is near the surface,
            // otherwise draw it from the globe.
            Position pos = icon.getPosition();
            Vec4 iconPoint = null;
            if (dc.is2DGlobe())
            {
                iconPoint = dc.getGlobe().computePointFromLocation(pos);
            }
            else if (pos.getElevation() < dc.getGlobe().getMaxElevation() && !this.isAlwaysUseAbsoluteElevation())
            {
                iconPoint = dc.getSurfaceGeometry().getSurfacePoint(icon.getPosition());
            }

            if (iconPoint == null)
            {
                Angle lat = pos.getLatitude();
                Angle lon = pos.getLongitude();
                double elevation = pos.getElevation();
                if (!this.isAlwaysUseAbsoluteElevation())
                    elevation += dc.getGlobe().getElevation(lat, lon);
                iconPoint = dc.getGlobe().computePointFromPosition(lat, lon, elevation);
            }

            double eyeDistance = icon.isAlwaysOnTop() ? 0 : dc.getView().getEyePoint().distanceTo3(iconPoint);

            if (this.isHorizonClippingEnabled() && !dc.is2DGlobe() && eyeDistance > horizon)
            {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, iconPoint, null);

                continue; // don't render horizon-clipped icons
            }

            // If enabled, eliminate icons outside the view volume. Primarily used to control icon visibility beyond
            // the view volume's far clipping plane.
            if (this.isViewClippingEnabled() && !dc.getView().getFrustumInModelCoordinates().contains(iconPoint))
            {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, iconPoint, null);

                continue; // don't render frustum-clipped icons
            }

            // The icons aren't drawn here, but added to the ordered queue to be drawn back-to-front.
            dc.addOrderedRenderable(new OrderedIcon(icon, iconPoint, layer, eyeDistance, horizon));

            if (icon.isShowToolTip())
                this.addToolTip(dc, icon, iconPoint);
        }
    }

    protected void addToolTip(DrawContext dc, WWIcon icon, Vec4 iconPoint)
    {
        if (icon.getToolTipFont() == null && icon.getToolTipText() == null)
            return;

        Vec4 screenPoint = dc.getView().project(iconPoint);
        if (screenPoint == null)
            return;

        if (icon.getToolTipOffset() != null)
            screenPoint = screenPoint.add3(icon.getToolTipOffset());

        OrderedText tip = new OrderedText(icon.getToolTipText(), icon.getToolTipFont(), screenPoint,
            icon.getToolTipTextColor(), 0d);
        dc.addOrderedRenderable(tip);
    }

    protected class OrderedText implements OrderedRenderable
    {
        protected Font font;
        protected String text;
        protected Vec4 point;
        protected double eyeDistance;
        protected java.awt.Point pickPoint;
        protected Layer layer;
        protected java.awt.Color color;

        public OrderedText(String text, Font font, Vec4 point, java.awt.Color color, double eyeDistance)
        {
            this.text = text;
            this.font = font;
            this.point = point;
            this.eyeDistance = eyeDistance;
            this.color = color;
        }

        public OrderedText(String text, Font font, Vec4 point, java.awt.Point pickPoint, Layer layer,
            double eyeDistance)
        {
            this.text = text;
            this.font = font;
            this.point = point;
            this.eyeDistance = eyeDistance;
            this.pickPoint = pickPoint;
            this.layer = layer;
        }

        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        public void render(DrawContext dc)
        {
            ToolTipRenderer toolTipRenderer = this.getToolTipRenderer(dc);
            toolTipRenderer.render(dc, this.text, (int) this.point.x, (int) this.point.y);
        }

        public void pick(DrawContext dc, java.awt.Point pickPoint)
        {
        }

        @SuppressWarnings({"UnusedDeclaration"})
        protected ToolTipRenderer getToolTipRenderer(DrawContext dc)
        {
            ToolTipRenderer tr = (this.font != null) ? new ToolTipRenderer(this.font) : new ToolTipRenderer();

            if (this.color != null)
            {
                tr.setTextColor(this.color);
                tr.setOutlineColor(this.color);
                tr.setInteriorColor(ToolTipRenderer.getContrastingColor(this.color));
            }
            else
            {
                tr.setUseSystemLookAndFeel(true);
            }

            return tr;
        }
    }

    protected class OrderedIcon implements OrderedRenderable, Locatable
    {
        protected WWIcon icon;
        protected Vec4 point;
        protected double eyeDistance;
        protected double horizonDistance;
        protected Layer layer;

        public OrderedIcon(WWIcon icon, Vec4 point, Layer layer, double eyeDistance, double horizonDistance)
        {
            this.icon = icon;
            this.point = point;
            this.eyeDistance = eyeDistance;
            this.horizonDistance = horizonDistance;
            this.layer = layer;
        }

        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        public Position getPosition()
        {
            return this.icon.getPosition();
        }

        public IconRenderer getRenderer()
        {
            return IconRenderer.this;
        }

        public Vec4 getPoint()
        {
            return this.point;
        }

        public WWIcon getIcon()
        {
            return this.icon;
        }

        public double getHorizonDistance()
        {
            return horizonDistance;
        }

        public Layer getLayer()
        {
            return layer;
        }

        public void render(DrawContext dc)
        {
            IconRenderer.this.beginDrawIcons(dc);

            try
            {
                IconRenderer.this.drawIconsInBatch(dc, this);
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingIcon", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingIcon", e);
            }
            finally
            {
                IconRenderer.this.endDrawIcons(dc);
            }
        }

        public void pick(DrawContext dc, java.awt.Point pickPoint)
        {
            IconRenderer.this.pickSupport.clearPickList();
            IconRenderer.this.beginDrawIcons(dc);
            try
            {
                if (IconRenderer.this.isAllowBatchPicking())
                    IconRenderer.this.pickIconsInBatch(dc, this);
                else
                    IconRenderer.this.drawIcon(dc, this);
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingIcon", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhilePickingIcon", e);
            }
            finally
            {
                IconRenderer.this.endDrawIcons(dc);
                IconRenderer.this.pickSupport.resolvePick(dc, pickPoint, layer);
                IconRenderer.this.pickSupport.clearPickList(); // to ensure entries can be garbage collected
            }
        }
    }

    protected void beginDrawIcons(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.oglStackHandler.clear();

        int attributeMask =
            GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
                | GL2.GL_TRANSFORM_BIT // for modelview and perspective
                | GL2.GL_VIEWPORT_BIT // for depth range
                | GL2.GL_CURRENT_BIT // for current color
                | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL2.GL_DEPTH_BUFFER_BIT // for depth func
                | GL2.GL_ENABLE_BIT; // for enable/disable changes
        this.oglStackHandler.pushAttrib(gl, attributeMask);

        // Apply the depth buffer but don't change it.
        if ((!dc.isDeepPickingEnabled()))
            gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // Suppress any fully transparent image pixels
        gl.glEnable(GL2.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL2.GL_GREATER, 0.001f);

        // Load a parallel projection with dimensions (viewportWidth, viewportHeight)
        this.oglStackHandler.pushProjectionIdentity(gl);
        gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

        this.oglStackHandler.pushModelview(gl);
        this.oglStackHandler.pushTexture(gl);

        if (dc.isPickingMode())
        {
            this.pickSupport.beginPicking(dc);

            // Set up to replace the non-transparent texture colors with the single pick color.
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);
        }
        else
        {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    protected void endDrawIcons(DrawContext dc)
    {
        if (dc.isPickingMode())
            this.pickSupport.endPicking(dc);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (dc.isPickingMode())
        {
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, OGLUtil.DEFAULT_TEX_ENV_MODE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, OGLUtil.DEFAULT_SRC0_RGB);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, OGLUtil.DEFAULT_COMBINE_RGB);
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

        this.oglStackHandler.pop(gl);
    }

    protected void drawIconsInBatch(DrawContext dc, OrderedIcon uIcon)
    {
        this.drawIcon(dc, uIcon);

        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();
        while (nextItem != null && nextItem instanceof OrderedIcon)
        {
            OrderedIcon oi = (OrderedIcon) nextItem;
            if (oi.getRenderer() != this)
                return;

            dc.pollOrderedRenderables(); // take it off the queue
            this.drawIcon(dc, oi);

            nextItem = dc.peekOrderedRenderables();
        }
    }

    protected void pickIconsInBatch(DrawContext dc, OrderedIcon uIcon)
    {
        this.drawIcon(dc, uIcon);

        // Draw as many as we can in a batch to save ogl state switching.
        // Note that there's a further qualification here than in render(): only items associated with the
        // same layer can be batched because the pick resolution step at the end of batch rendering
        // associates the item's layer with the resolved picked object.
        Object nextItem = dc.peekOrderedRenderables();
        while (nextItem != null && nextItem instanceof OrderedIcon
            && ((OrderedIcon) nextItem).layer == uIcon.layer)
        {
            OrderedIcon oi = (OrderedIcon) nextItem;
            if (oi.getRenderer() != this)
                return;

            dc.pollOrderedRenderables(); // take it off the queue
            this.drawIcon(dc, oi);

            nextItem = dc.peekOrderedRenderables();
        }
    }

    protected Vec4 drawIcon(DrawContext dc, OrderedIcon uIcon)
    {
        if (uIcon.point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);

            // Record feedback data for this WWIcon if feedback is enabled.
            if (uIcon.icon != null)
                this.recordFeedback(dc, uIcon.icon, null, null);

            return null;
        }

        WWIcon icon = uIcon.icon;
        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(uIcon.point) < 0)
        {
            // Record feedback data for this WWIcon if feedback is enabled.
            this.recordFeedback(dc, icon, uIcon.point, null);

            return null;
        }

        final Vec4 screenPoint = dc.getView().project(uIcon.point);
        if (screenPoint == null)
        {
            // Record feedback data for this WWIcon if feedback is enabled.
            this.recordFeedback(dc, icon, uIcon.point, null);

            return null;
        }

        double pedestalScale;
        double pedestalSpacing;
        if (this.pedestal != null)
        {
            pedestalScale = this.pedestal.getScale();
            pedestalSpacing = pedestal.getSpacingPixels();
        }
        else
        {
            pedestalScale = 0d;
            pedestalSpacing = 0d;
        }

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.setDepthFunc(dc, uIcon, screenPoint);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        Dimension size = icon.getSize();
        double width = size != null ? size.getWidth() : icon.getImageTexture().getWidth(dc);
        double height = size != null ? size.getHeight() : icon.getImageTexture().getHeight(dc);
        gl.glTranslated(screenPoint.x - width / 2, screenPoint.y + (pedestalScale * height) + pedestalSpacing, 0d);

        if (icon.isHighlighted())
        {
            double heightDelta = this.pedestal != null ? 0 : height / 2; // expand only above the pedestal
            gl.glTranslated(width / 2, heightDelta, 0);
            gl.glScaled(icon.getHighlightScale(), icon.getHighlightScale(), icon.getHighlightScale());
            gl.glTranslated(-width / 2, -heightDelta, 0);
        }

        Rectangle rect = new Rectangle((int) (screenPoint.x - width / 2), (int) (screenPoint.y), (int) width,
            (int) (height + (pedestalScale * height) + pedestalSpacing));

        if (dc.isPickingMode())
        {
            //If in picking mode and pick clipping is enabled, check to see if the icon is within the pick volume.
            if (this.isPickFrustumClippingEnabled() && !dc.getPickFrustums().intersectsAny(rect))
            {
                // Record feedback data for this WWIcon if feedback is enabled.
                this.recordFeedback(dc, icon, uIcon.point, rect);

                return screenPoint;
            }
            else
            {
                java.awt.Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                this.pickSupport.addPickableObject(colorCode, icon, uIcon.getPosition(), false);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
            }
        }

        if (icon.getBackgroundTexture() != null)
            this.applyBackground(dc, icon, screenPoint, width, height, pedestalSpacing, pedestalScale);

        if (icon.getImageTexture().bind(dc))
        {
            TextureCoords texCoords = icon.getImageTexture().getTexCoords();
            gl.glScaled(width, height, 1d);
            dc.drawUnitQuad(texCoords);
        }

        if (this.pedestal != null && this.pedestal.getImageTexture() != null)
        {
            gl.glLoadIdentity();
            gl.glTranslated(screenPoint.x - (pedestalScale * (width / 2)), screenPoint.y, 0d);
            gl.glScaled(width * pedestalScale, height * pedestalScale, 1d);

            if (this.pedestal.getImageTexture().bind(dc))
            {
                TextureCoords texCoords = this.pedestal.getImageTexture().getTexCoords();
                dc.drawUnitQuad(texCoords);
            }
        }

        // Record feedback data for this WWIcon if feedback is enabled.
        this.recordFeedback(dc, icon, uIcon.point, rect);

        return screenPoint;
    }

    protected void applyBackground(DrawContext dc, WWIcon icon, Vec4 screenPoint, double width, double height,
        double pedestalSpacing, double pedestalScale)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        double backgroundScale;
        backgroundScale = icon.getBackgroundScale();

        if (icon.getBackgroundTexture() != null)
        {
            if (icon.getBackgroundTexture().bind(dc))
            {
                TextureCoords texCoords = icon.getBackgroundTexture().getTexCoords();
                gl.glPushMatrix();
                gl.glLoadIdentity();
                double bgwidth = backgroundScale * width;
                double bgheight = backgroundScale * height;
                // Offset the background for the highlighted scale.
                //if (icon.isHighlighted())
                //{
                //    gl.glTranslated(0d, height * (icon.getHighlightScale() - 1) / 2, 0d);
                //}
                // Offset the background for the pedestal height.
                gl.glTranslated(0d, (pedestalScale * height) + pedestalSpacing, 0d);
                // Place the background centered behind the icon.
                gl.glTranslated(screenPoint.x - bgwidth / 2, screenPoint.y - (bgheight - height) / 2, 0d);
                // Scale to the background image dimension.
                gl.glScaled(bgwidth, bgheight, 1d);
                dc.drawUnitQuad(texCoords);
                gl.glPopMatrix();
            }
        }
    }

    protected void setDepthFunc(DrawContext dc, OrderedIcon uIcon, Vec4 screenPoint)
    {
        GL gl = dc.getGL();

        if (uIcon.icon.isAlwaysOnTop())
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
            return;
        }

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
        else if (uIcon.eyeDistance > uIcon.horizonDistance)
        {
            gl.glDepthFunc(GL.GL_EQUAL);
            gl.glDepthRange(1d, 1d);
        }
        else
        {
            gl.glDepthFunc(GL.GL_ALWAYS);
        }
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.IconLayer.Name");
    }

    //**************************************************************//
    //********************  Feedback  ******************************//
    //**************************************************************//

    /**
     * Returns true if the IconRenderer should record feedback about how the specified WWIcon has been processed.
     *
     * @param dc   the current DrawContext.
     * @param icon the WWIcon to record feedback information for.
     *
     * @return true to record feedback; false otherwise.
     */
    protected boolean isFeedbackEnabled(DrawContext dc, WWIcon icon)
    {
        if (dc.isPickingMode())
            return false;

        Boolean b = (Boolean) icon.getValue(AVKey.FEEDBACK_ENABLED);
        return (b != null && b);
    }

    /**
     * If feedback is enabled for the specified WWIcon, this method records feedback about how the specified WWIcon has
     * been processed.
     *
     * @param dc         the current DrawContext.
     * @param icon       the icon which the feedback information refers to.
     * @param modelPoint the icon's reference point in model coordinates.
     * @param screenRect the icon's bounding rectangle in screen coordinates.
     */
    protected void recordFeedback(DrawContext dc, WWIcon icon, Vec4 modelPoint, Rectangle screenRect)
    {
        if (!this.isFeedbackEnabled(dc, icon))
            return;

        this.doRecordFeedback(dc, icon, modelPoint, screenRect);
    }

    /**
     * Records feedback about how the specified WWIcon has been processed.
     *
     * @param dc         the current DrawContext.
     * @param icon       the icon which the feedback information refers to.
     * @param modelPoint the icon's reference point in model coordinates.
     * @param screenRect the icon's bounding rectangle in screen coordinates.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void doRecordFeedback(DrawContext dc, WWIcon icon, Vec4 modelPoint, Rectangle screenRect)
    {
        icon.setValue(AVKey.FEEDBACK_REFERENCE_POINT, modelPoint);
        icon.setValue(AVKey.FEEDBACK_SCREEN_BOUNDS, screenRect);
    }
}
