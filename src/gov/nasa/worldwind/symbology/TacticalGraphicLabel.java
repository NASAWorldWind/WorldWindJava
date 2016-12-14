/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A label drawn as part of a tactical graphic. The label is drawn at constant screen size. The label can include
 * multiple lines of text, and can optionally be kept aligned with features on the globe. To align a label with the
 * globe specify an {@link #setOrientationPosition(gov.nasa.worldwind.geom.Position) orientationPosition} for the label.
 * The label will be drawn along a line connecting the label's position to the orientation position.
 *
 * @author pabercrombie
 * @version $Id: TacticalGraphicLabel.java 2200 2014-08-07 18:05:43Z tgaskins $
 */
public class TacticalGraphicLabel
{
    protected class OrderedLabel implements OrderedRenderable
    {
        /** Geographic position in cartesian coordinates. */
        protected Vec4 placePoint;
        /** Location of the place point projected onto the screen. */
        protected Vec4 screenPlacePoint;
        /**
         * Location of the upper left corner of the text measured from the lower left corner of the viewport. This point
         * in OGL coordinates.
         */
        protected Point screenPoint;
        /** Rotation applied to the label. This is computed each frame based on the orientation position. */
        protected Angle rotation;
        /** Extent of the label on the screen. */
        protected Rectangle screenExtent;
        /** Distance from the eye point to the label's geographic location. */
        protected double eyeDistance;

        @Override
        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        @Override
        public void pick(DrawContext dc, Point pickPoint)
        {
            TacticalGraphicLabel.this.pick(dc, pickPoint, this);
        }

        @Override
        public void render(DrawContext dc)
        {

            TacticalGraphicLabel.this.drawOrderedRenderable(dc, this);
        }

        public boolean isEnableBatchRendering()
        {
            return TacticalGraphicLabel.this.isEnableBatchRendering();
        }

        public boolean isEnableBatchPicking()
        {
            return TacticalGraphicLabel.this.isEnableBatchPicking();
        }

        public Layer getPickLayer()
        {
            return TacticalGraphicLabel.this.pickLayer;
        }

        protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates)
        {
            TacticalGraphicLabel.this.doDrawOrderedRenderable(dc, pickCandidates, this);
        }

        protected Font getFont()
        {
            return TacticalGraphicLabel.this.getFont();
        }

        protected boolean isDrawInterior()
        {
            return TacticalGraphicLabel.this.isDrawInterior();
        }

        protected void doDrawText(TextRenderer textRenderer)
        {
            TacticalGraphicLabel.this.doDrawText(textRenderer, this);
        }
    }

    /** Default font. */
    public static final Font DEFAULT_FONT = Font.decode("Arial-BOLD-16");
    /**
     * Default offset. The default offset aligns the label horizontal with the text alignment position, and centers the
     * label vertically. For example, if the text alignment is <code>AVKey.LEFT</code>, then the left edge of the text
     * will be aligned with the geographic position, and the label will be centered vertically.
     */
    public static final Offset DEFAULT_OFFSET = new Offset(0d, -0.5d, AVKey.FRACTION, AVKey.FRACTION);
    /** Default insets around the label. */
    public static final Insets DEFAULT_INSETS = new Insets(5, 5, 5, 5);
    /** Default interior opacity. */
    public static final double DEFAULT_INTERIOR_OPACITY = 0.7;
    /** Default text effect (shadow). */
    public static final String DEFAULT_TEXT_EFFECT = AVKey.TEXT_EFFECT_SHADOW;

    /** Text split into separate lines. */
    protected String[] lines;
    /** The label's geographic position. */
    protected Position position;
    /** Offset from the geographic position at which to draw the label. */
    protected Offset offset = DEFAULT_OFFSET;
    /** Text alignment for multi-line labels. */
    protected String textAlign = AVKey.LEFT;
    /** The label is drawn along a line from the label position to the orientation position. */
    protected Position orientationPosition;

    /** Material used to draw the label. */
    protected Material material = Material.BLACK;
    /** Opacity of the text, as a value between 0 and 1. */
    protected double opacity = 1.0;
    protected double interiorOpacity = DEFAULT_INTERIOR_OPACITY;
    /** Font used to draw the label. */
    protected Font font = DEFAULT_FONT;
    /** Space (in pixels) between lines in a multi-line label. */
    protected int lineSpacing = 5; // TODO compute default based on font size

    /** Effect applied to the text. May be {@link AVKey#TEXT_EFFECT_SHADOW} or {@link AVKey#TEXT_EFFECT_NONE}. */
    protected String effect = DEFAULT_TEXT_EFFECT;
    /** Insets that separate the text from its frame. Only applies when the text interior is rendered. */
    protected Insets insets = DEFAULT_INSETS;
    /** Indicates whether or not to draw the label interior. */
    protected boolean drawInterior;

    /** Indicates whether or not batch rendering is enabled. */
    protected boolean enableBatchRendering = true;
    /** Indicates whether or not batch picking is enabled. */
    protected boolean enableBatchPicking = true;

    /** Indicates an object that represents the label during picking. */
    protected Object delegateOwner;

    // Computed each frame
    protected long frameTimeStamp = -1L;
    protected OrderedLabel thisFramesOrderedLabel;

    // Computed only when text or font changes
    /** Size of the label. */
    protected Rectangle2D bounds;
    /** Cached bounds for each line of text. */
    protected Rectangle2D[] lineBounds;
    /**
     * Height of a line of text, computed in {@link #computeBoundsIfNeeded(gov.nasa.worldwind.render.DrawContext)}.
     */
    protected int lineHeight;

    /** Stack handler used for beginDrawing/endDrawing state. */
    protected OGLStackHandler BEogsh = new OGLStackHandler();
    /** Support object used during picking. */
    protected PickSupport pickSupport = new PickSupport();
    /** Active layer. */
    protected Layer pickLayer;

    /** Create a new empty label. */
    public TacticalGraphicLabel()
    {
    }

    /**
     * Create a new label.
     *
     * @param text Label text.
     */
    public TacticalGraphicLabel(String text)
    {
        this.setText(text);
    }

    /**
     * Indicates the text of this label.
     *
     * @return The label's text.
     */
    public String getText()
    {
        if (this.lines != null)
        {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < this.lines.length - 1; i++)
            {
                sb.append(this.lines[i]).append("\n");
            }
            sb.append(this.lines[this.lines.length - 1]);

            return sb.toString();
        }

        return null;
    }

    /**
     * Specifies the text of this label. The text may include multiple lines, separated by newline characters.
     *
     * @param text New text.
     */
    public void setText(String text)
    {
        if (text != null)
            this.lines = text.split("\n");
        else
            this.lines = null;

        this.bounds = null; // Need to recompute
    }

    /**
     * Indicates the label's position. The label is drawn at an offset from this position.
     *
     * @return The label's geographic position.
     *
     * @see #getOffset()
     */
    public Position getPosition()
    {
        return this.position;
    }

    /**
     * Indicates the label's geographic position. The label is drawn at an offset from this position.
     *
     * @param position New position.
     *
     * @see #getOffset()
     */
    public void setPosition(Position position)
    {
        this.position = position;

        // Label has moved, need to recompute screen extent. Explicitly set the extent to null so that it will be
        // recomputed even if the application calls setPosition multiple times per frame.
        this.thisFramesOrderedLabel = null;
    }

    /**
     * Indicates the current text alignment. Can be one of {@link AVKey#LEFT} (default), {@link AVKey#CENTER} or {@link
     * AVKey#RIGHT}.
     *
     * @return the current text alignment.
     */
    public String getTextAlign()
    {
        return this.textAlign;
    }

    /**
     * Specifies the text alignment. Can be one of {@link AVKey#LEFT} (default), {@link AVKey#CENTER}, or {@link
     * AVKey#RIGHT}.
     *
     * @param textAlign New text alignment.
     */
    public void setTextAlign(String textAlign)
    {
        if (textAlign == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.textAlign = textAlign;
    }

    /**
     * Indicates the offset from the geographic position at which to draw the label. See {@link
     * #setOffset(gov.nasa.worldwind.render.Offset) setOffset} for more information on how the offset is interpreted.
     *
     * @return The offset at which to draw the label.
     */
    public Offset getOffset()
    {
        return this.offset;
    }

    /**
     * Specifies the offset from the geographic position at which to draw the label. The default offset aligns the label
     * horizontal with the text alignment position, and centers the label vertically. For example, if the text alignment
     * is <code>AVKey.LEFT</code>., then the left edge of the text will be aligned with the geographic position, and the
     * label will be centered vertically.
     * <p/>
     * When the text is rotated a horizontal offset moves the text along the orientation line, and a vertical offset
     * moves the text perpendicular to the orientation line.
     *
     * @param offset The offset at which to draw the label.
     */
    public void setOffset(Offset offset)
    {
        if (offset == null)
        {
            String message = Logging.getMessage("nullValue.OffsetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.offset = offset;
    }

    /**
     * Indicates the font used to draw the label.
     *
     * @return The label's font.
     */
    public Font getFont()
    {
        return this.font;
    }

    /**
     * Specifies the font used to draw the label.
     *
     * @param font New font.
     */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (font != this.font)
        {
            this.font = font;
            this.bounds = null; // Need to recompute
        }
    }

    /**
     * Indicates the line spacing applied to multi-line labels.
     *
     * @return The space (in pixels) between lines of a multi-line label.
     */
    public int getLineSpacing()
    {
        return lineSpacing;
    }

    /**
     * Specifies the line spacing applied to multi-line labels.
     *
     * @param lineSpacing New line spacing.
     */
    public void setLineSpacing(int lineSpacing)
    {
        if (lineSpacing < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.lineSpacing = lineSpacing;
    }

    /**
     * Indicates the material used to draw the label.
     *
     * @return The label's material.
     */
    public Material getMaterial()
    {
        return this.material;
    }

    /**
     * Specifies the material used to draw the label.
     *
     * @param material New material.
     */
    public void setMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.material = material;
    }

    /**
     * Indicates whether or not to draw a colored frame behind the label.
     *
     * @return <code>true</code> if the label's interior is drawn, otherwise <code>false</code>.
     *
     * @see #setDrawInterior(boolean)
     */
    public boolean isDrawInterior()
    {
        return this.drawInterior;
    }

    /**
     * Specifies whether or not to draw a colored frame behind the label.
     *
     * @param drawInterior <code>true</code> if the label's interior is drawn, otherwise <code>false</code>.
     *
     * @see #isDrawInterior()
     */
    public void setDrawInterior(boolean drawInterior)
    {
        this.drawInterior = drawInterior;
    }

    /**
     * Indicates the opacity of the text as a floating-point value in the range 0.0 to 1.0. A value of 1.0 specifies a
     * completely opaque text, and 0.0 specifies a completely transparent text. Values in between specify a partially
     * transparent text.
     *
     * @return the opacity of the text as a floating-point value from 0.0 to 1.0.
     */
    public double getOpacity()
    {
        return this.opacity;
    }

    /**
     * Specifies the opacity of the text as a floating-point value in the range 0.0 to 1.0. A value of 1.0 specifies a
     * completely opaque text, and 0.0 specifies a completely transparent text. Values in between specify a partially
     * transparent text.
     *
     * @param opacity the opacity of text as a floating-point value from 0.0 to 1.0.
     *
     * @throws IllegalArgumentException if <code>opacity</code> is less than 0.0 or greater than 1.0.
     */
    public void setOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.opacity = opacity;
    }

    /**
     * Indicates the opacity of label's interior as a floating-point value in the range 0.0 to 1.0. A value of 1.0
     * specifies a completely opaque interior, and 0.0 specifies a completely transparent interior. Values in between
     * specify a partially transparent interior.
     *
     * @return the opacity of the interior as a floating-point value from 0.0 to 1.0.
     */
    public double getInteriorOpacity()
    {
        return this.interiorOpacity;
    }

    /**
     * Specifies the opacity of the label's interior as a floating-point value in the range 0.0 to 1.0. A value of 1.0
     * specifies a completely opaque interior, and 0.0 specifies a completely transparent interior. Values in between
     * specify a partially transparent interior.
     *
     * @param interiorOpacity the opacity of label's interior as a floating-point value from 0.0 to 1.0.
     *
     * @throws IllegalArgumentException if <code>opacity</code> is less than 0.0 or greater than 1.0.
     */
    public void setInteriorOpacity(double interiorOpacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorOpacity = interiorOpacity;
    }

    /**
     * Indicates the orientation position. The label oriented on a line drawn from the label's position to the
     * orientation position.
     *
     * @return Position used to orient the label. May be null.
     */
    public Position getOrientationPosition()
    {
        return this.orientationPosition;
    }

    /**
     * Specifies the orientation position. The label is oriented on a line drawn from the label's position to the
     * orientation position. If the orientation position is null then the label is drawn with no rotation.
     *
     * @param orientationPosition Draw label oriented toward this position.
     */
    public void setOrientationPosition(Position orientationPosition)
    {
        this.orientationPosition = orientationPosition;
    }

    /**
     * Indicates the amount of space between the label's content and its frame, in pixels.
     *
     * @return the padding between the label's content and its frame, in pixels.
     *
     * @see #setInsets(java.awt.Insets)
     */
    public Insets getInsets()
    {
        return this.insets;
    }

    /**
     * Specifies the amount of space (in pixels) between the label's content and the edges of the label's frame.
     *
     * @param insets the desired padding between the label's content and its frame, in pixels.
     *
     * @throws IllegalArgumentException if <code>insets</code> is <code>null</code>.
     * @see #getInsets()
     */
    public void setInsets(Insets insets)
    {
        if (insets == null)
        {
            String message = Logging.getMessage("nullValue.InsetsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.insets = insets;
    }

    /**
     * Indicates an effect used to decorate the text. Can be one of {@link AVKey#TEXT_EFFECT_SHADOW} (default), or
     * {@link AVKey#TEXT_EFFECT_NONE}.
     *
     * @return the effect used for text rendering
     */
    public String getEffect()
    {
        return this.effect;
    }

    /**
     * Specifies an effect used to decorate the text. Can be one of {@link AVKey#TEXT_EFFECT_SHADOW} (default), or
     * {@link AVKey#TEXT_EFFECT_NONE}.
     *
     * @param effect the effect to use for text rendering
     */
    public void setEffect(String effect)
    {
        if (effect == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.effect = effect;
    }

    /**
     * Returns the delegate owner of this label. If non-null, the returned object replaces the label as the pickable
     * object returned during picking. If null, the label itself is the pickable object returned during picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the label is
     * returned during picking.
     */
    public Object getDelegateOwner()
    {
        return this.delegateOwner;
    }

    /**
     * Specifies the delegate owner of this label. If non-null, the delegate owner replaces the label as the pickable
     * object returned during picking. If null, the label itself is the pickable object returned during picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the label.
     */
    public void setDelegateOwner(Object owner)
    {
        this.delegateOwner = owner;
    }

    /**
     * Indicates whether batch picking is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchPicking(boolean).
     */
    public boolean isEnableBatchPicking()
    {
        return this.enableBatchPicking;
    }

    /**
     * Specifies whether adjacent Labels in the ordered renderable list may be pick-tested together if they are
     * contained in the same layer. This increases performance but allows only the top-most of the label to be reported
     * in a {@link gov.nasa.worldwind.event.SelectEvent} even if several of the labels are at the pick position.
     * <p/>
     * Batch rendering ({@link #setEnableBatchRendering(boolean)}) must be enabled in order for batch picking to occur.
     *
     * @param enableBatchPicking true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchPicking(boolean enableBatchPicking)
    {
        this.enableBatchPicking = enableBatchPicking;
    }

    /**
     * Indicates whether batch rendering is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchRendering(boolean).
     */
    public boolean isEnableBatchRendering()
    {
        return this.enableBatchRendering;
    }

    /**
     * Specifies whether adjacent Labels in the ordered renderable list may be rendered together if they are contained
     * in the same layer. This increases performance and there is seldom a reason to disable it.
     *
     * @param enableBatchRendering true to enable batch rendering, otherwise false.
     */
    public void setEnableBatchRendering(boolean enableBatchRendering)
    {
        this.enableBatchRendering = enableBatchRendering;
    }

    /**
     * Get the label bounding {@link java.awt.Rectangle} using OGL coordinates - bottom-left corner x and y relative to
     * the {@link gov.nasa.worldwind.WorldWindow} bottom-left corner. If the label is rotated then the returned
     * rectangle is the bounding rectangle of the rotated label.
     *
     * @param dc the current DrawContext.
     *
     * @return the label bounding {@link java.awt.Rectangle} using OGL viewport coordinates.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null.
     */
    public Rectangle getBounds(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.computeGeometryIfNeeded(dc);

        return this.thisFramesOrderedLabel.screenExtent;
    }

    protected void computeGeometryIfNeeded(DrawContext dc)
    {
        // Re-use rendering state values already calculated this frame. If the screenExtent is null, recompute even if
        // the timestamp is the same. This prevents using a stale position if the application calls setPosition and
        // getBounds multiple times before the label is rendered.

        if (dc.getFrameTimeStamp() != this.frameTimeStamp || this.thisFramesOrderedLabel == null
            || dc.isContinuous2DGlobe())
        {
            OrderedLabel olbl = new OrderedLabel();
            this.computeGeometry(dc, olbl);
            this.thisFramesOrderedLabel = olbl;
            this.frameTimeStamp = dc.getFrameTimeStamp();
        }
    }

    /**
     * Compute the bounds of the text, if necessary.
     *
     * @param dc the current DrawContext.
     */
    protected void computeBoundsIfNeeded(DrawContext dc)
    {
        // Do not compute bounds if they are available. Computing text bounds is expensive, so only do this
        // calculation if necessary.
        if (this.bounds != null)
            return;

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            this.getFont());

        int width = 0;
        int maxLineHeight = 0;
        this.lineBounds = new Rectangle2D[this.lines.length];

        for (int i = 0; i < this.lines.length; i++)
        {
            Rectangle2D lineBounds = textRenderer.getBounds(lines[i]);
            width = (int) Math.max(lineBounds.getWidth(), width);

            double thisLineHeight = Math.abs(lineBounds.getY());
            maxLineHeight = (int) Math.max(thisLineHeight, maxLineHeight);

            this.lineBounds[i] = lineBounds;
        }
        this.lineHeight = maxLineHeight;

        // Compute final height using maxLineHeight and number of lines
        this.bounds = new Rectangle(this.lines.length, maxLineHeight, width,
            this.lines.length * maxLineHeight + this.lines.length * this.lineSpacing);
    }

    /**
     * Compute the label's screen position from its geographic position.
     *
     * @param dc   Current draw context.
     * @param olbl The ordered label to compute geometry for.
     */
    protected void computeGeometry(DrawContext dc, OrderedLabel olbl)
    {
        // Project the label position onto the viewport
        Position pos = this.getPosition();
        if (pos == null)
            return;

        olbl.placePoint = dc.computeTerrainPoint(pos.getLatitude(), pos.getLongitude(), 0);
        olbl.screenPlacePoint = dc.getView().project(olbl.placePoint);

        olbl.eyeDistance = olbl.placePoint.distanceTo3(dc.getView().getEyePoint());

        boolean orientationReversed = false;
        if (this.orientationPosition != null)
        {
            // Project the orientation point onto the screen
            Vec4 orientationPlacePoint = dc.computeTerrainPoint(this.orientationPosition.getLatitude(),
                this.orientationPosition.getLongitude(), 0);
            Vec4 orientationScreenPoint = dc.getView().project(orientationPlacePoint);

            olbl.rotation = this.computeRotation(olbl.screenPlacePoint, orientationScreenPoint);

            // The orientation is reversed if the orientation point falls to the right of the screen point. Text is
            // never drawn upside down, so when the orientation is reversed the text flips vertically to keep the text
            // right side up.
            orientationReversed = (orientationScreenPoint.x <= olbl.screenPlacePoint.x);
        }

        this.computeBoundsIfNeeded(dc);

        Offset offset = this.getOffset();
        Point2D offsetPoint = offset.computeOffset(this.bounds.getWidth(), this.bounds.getHeight(), null, null);

        // If a rotation is applied to the text, then rotate the offset as well. An offset in the x direction
        // will move the text along the orientation line, and a offset in the y direction will move the text
        // perpendicular to the orientation line.
        if (olbl.rotation != null)
        {
            double dy = offsetPoint.getY();

            // If the orientation is reversed we need to adjust the vertical offset to compensate for the flipped
            // text. For example, if the offset normally aligns the top of the text with the place point then without
            // this adjustment the bottom of the text would align with the place point when the orientation is
            // reversed.
            if (orientationReversed)
            {
                dy = -(dy + this.bounds.getHeight());
            }

            Vec4 pOffset = new Vec4(offsetPoint.getX(), dy);
            Matrix rot = Matrix.fromRotationZ(olbl.rotation.multiply(-1));

            pOffset = pOffset.transformBy3(rot);

            offsetPoint = new Point((int) pOffset.getX(), (int) pOffset.getY());
        }

        int x = (int) (olbl.screenPlacePoint.x + offsetPoint.getX());
        int y = (int) (olbl.screenPlacePoint.y - offsetPoint.getY());

        olbl.screenPoint = new Point(x, y);
        olbl.screenExtent = this.computeTextExtent(x, y, olbl);
    }

    /**
     * Determine if this label intersects the view or pick frustum.
     *
     * @param dc   Current draw context.
     * @param olbl The ordered label to intersect.
     *
     * @return True if this label intersects the active frustum (view or pick). Otherwise false.
     */
    protected boolean intersectsFrustum(DrawContext dc, OrderedLabel olbl)
    {
        View view = dc.getView();
        Frustum frustum = view.getFrustumInModelCoordinates();

        // Test the label's model coordinate point against the near and far clipping planes.
        if (olbl.placePoint != null
            && (frustum.getNear().distanceTo(olbl.placePoint) < 0
            || frustum.getFar().distanceTo(olbl.placePoint) < 0))
        {
            return false;
        }

        if (dc.isPickingMode())
            return dc.getPickFrustums().intersectsAny(olbl.screenExtent);
        else
            return view.getViewport().intersects(olbl.screenExtent);
    }

    /**
     * Compute the amount of rotation to apply to a label in order to keep it oriented toward its orientation position.
     *
     * @param screenPoint            Geographic position of the text, projected onto the screen.
     * @param orientationScreenPoint Orientation position, projected onto the screen.
     *
     * @return The rotation angle to apply when drawing the label.
     */
    protected Angle computeRotation(Vec4 screenPoint, Vec4 orientationScreenPoint)
    {
        // Determine delta between the orientation position and the label position
        double deltaX = screenPoint.x - orientationScreenPoint.x;
        double deltaY = screenPoint.y - orientationScreenPoint.y;

        if (deltaX != 0)
        {
            double angle = Math.atan(deltaY / deltaX);
            return Angle.fromRadians(angle);
        }
        else
        {
            return Angle.POS90; // Vertical label
        }
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        // This render method is called twice during frame generation. It's first called as a Renderable
        // during Renderable picking. It's called again during normal rendering.

        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.makeOrderedRenderable(dc);
    }

    public void pick(DrawContext dc, Point pickPoint, OrderedLabel olbl)
    {
        // This method is called only when ordered renderables are being drawn.
        // Arg checked within call to render.

        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.pickSupport.clearPickList();
        try
        {
            this.pickSupport.beginPicking(dc);
            this.drawOrderedRenderable(dc, olbl);
        }
        finally
        {
            this.pickSupport.endPicking(dc);
            this.pickSupport.resolvePick(dc, pickPoint, this.pickLayer);
        }
    }

    /**
     * Draws the graphic as an ordered renderable.
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        if (this.lines == null || this.position == null)
            return;

        this.computeGeometryIfNeeded(dc);

        // Don't draw if beyond the horizon.
        double horizon = dc.getView().getHorizonDistance();
        if (!dc.is2DGlobe() && this.thisFramesOrderedLabel.eyeDistance > horizon)
            return;

        if (this.intersectsFrustum(dc, this.thisFramesOrderedLabel))
            dc.addOrderedRenderable(this.thisFramesOrderedLabel);

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();
    }

    /**
     * Draws the graphic as an ordered renderable.
     *
     * @param dc   the current draw context.
     * @param olbl The ordered label to draw.
     */
    protected void drawOrderedRenderable(DrawContext dc, OrderedLabel olbl)
    {
        this.beginDrawing(dc);
        try
        {
            this.doDrawOrderedRenderable(dc, this.pickSupport, olbl);

            if (this.isEnableBatchRendering())
                this.drawBatched(dc, olbl);
        }
        finally
        {
            this.endDrawing(dc);
        }
    }

    /**
     * Draw this label during ordered rendering.
     *
     * @param dc          Current draw context.
     * @param pickSupport Support object used during picking.
     * @param olbl        The ordered label to draw.
     */
    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickSupport, OrderedLabel olbl)
    {
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
        if (dc.isPickingMode())
        {
            this.doPick(dc, pickSupport, olbl);
        }
        else
        {
            this.drawText(dc, textRenderer, olbl);
        }
    }

    /**
     * Establish the OpenGL state needed to draw text.
     *
     * @param dc the current draw context.
     */
    protected void beginDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        int attrMask =
            GL2.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
                | GL2.GL_TRANSFORM_BIT // for modelview and perspective
                | GL2.GL_VIEWPORT_BIT // for depth range
                | GL2.GL_CURRENT_BIT // for current color
                | GL2.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL2.GL_DEPTH_BUFFER_BIT // for depth func
                | GL2.GL_ENABLE_BIT; // for enable/disable changes

        this.BEogsh.pushAttrib(gl, attrMask);

        if (!dc.isPickingMode())
        {
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
        }

        // Do not depth buffer the label. (Labels beyond the horizon are culled above.)
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // The image is drawn using a parallel projection.
        this.BEogsh.pushProjectionIdentity(gl);
        gl.glOrtho(0d, dc.getView().getViewport().width, 0d, dc.getView().getViewport().height, -1d, 1d);

        this.BEogsh.pushModelviewIdentity(gl);
    }

    /**
     * Pop the state set in beginDrawing.
     *
     * @param dc the current draw context.
     */
    protected void endDrawing(DrawContext dc)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        this.BEogsh.pop(gl);
    }

    /**
     * Draw labels for picking.
     *
     * @param dc          Current draw context.
     * @param pickSupport the PickSupport instance to be used.
     * @param olbl        The ordered label to pick.
     */
    protected void doPick(DrawContext dc, PickSupport pickSupport, OrderedLabel olbl)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Angle heading = olbl.rotation;

        double headingDegrees;
        if (heading != null)
            headingDegrees = heading.degrees;
        else
            headingDegrees = 0;

        int x = olbl.screenPoint.x;
        int y = olbl.screenPoint.y;

        boolean matrixPushed = false;
        try
        {
            if (headingDegrees != 0)
            {
                gl.glPushMatrix();
                matrixPushed = true;

                gl.glTranslated(x, y, 0);
                gl.glRotated(headingDegrees, 0, 0, 1);
                gl.glTranslated(-x, -y, 0);
            }

            for (int i = 0; i < this.lines.length; i++)
            {
                Rectangle2D bounds = this.lineBounds[i];
                double width = bounds.getWidth();
                double height = bounds.getHeight();

                x = olbl.screenPoint.x;
                if (this.textAlign.equals(AVKey.CENTER))
                    x = x - (int) (width / 2.0);
                else if (this.textAlign.equals(AVKey.RIGHT))
                    x = x - (int) width;
                y -= this.lineHeight;

                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                PickedObject po = new PickedObject(colorCode, this.getPickedObject(), this.position, false);
                pickSupport.addPickableObject(po);

                // Draw line rectangle
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                try
                {
                    gl.glBegin(GL2.GL_POLYGON);
                    gl.glVertex3d(x, y, 0);
                    gl.glVertex3d(x + width - 1, y, 0);
                    gl.glVertex3d(x + width - 1, y + height - 1, 0);
                    gl.glVertex3d(x, y + height - 1, 0);
                    gl.glVertex3d(x, y, 0);
                }
                finally
                {
                    gl.glEnd();
                }

                y -= this.lineSpacing;
            }
        }
        finally
        {
            if (matrixPushed)
            {
                gl.glPopMatrix();
            }
        }
    }

    /**
     * Draw the label's text. This method sets up the text renderer, and then calls {@link #doDrawText(TextRenderer,
     * gov.nasa.worldwind.symbology.TacticalGraphicLabel.OrderedLabel) doDrawText} to actually draw the text.
     *
     * @param dc           Current draw context.
     * @param textRenderer Text renderer.
     * @param olbl         The ordered label to draw.
     */
    protected void drawText(DrawContext dc, TextRenderer textRenderer, OrderedLabel olbl)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Angle heading = olbl.rotation;

        double headingDegrees;
        if (heading != null)
            headingDegrees = heading.degrees;
        else
            headingDegrees = 0;

        boolean matrixPushed = false;
        try
        {
            int x = olbl.screenPoint.x;
            int y = olbl.screenPoint.y;

            if (headingDegrees != 0)
            {
                gl.glPushMatrix();
                matrixPushed = true;

                gl.glTranslated(x, y, 0);
                gl.glRotated(headingDegrees, 0, 0, 1);
                gl.glTranslated(-x, -y, 0);
            }

            if (this.isDrawInterior())
                this.drawInterior(dc, olbl);

            textRenderer.begin3DRendering();
            try
            {
                this.doDrawText(textRenderer, olbl);

                // Draw other labels that share the same text renderer configuration, if possible.
                if (this.isEnableBatchRendering())
                    this.drawBatchedText(dc, textRenderer, olbl);
            }
            finally
            {
                textRenderer.end3DRendering();
            }
        }
        finally
        {
            if (matrixPushed)
            {
                gl.glPopMatrix();
            }
        }
    }

    /**
     * Render the label interior as a filled rectangle.
     *
     * @param dc   Current draw context.
     * @param olbl The ordered label to draw.
     */
    protected void drawInterior(DrawContext dc, OrderedLabel olbl)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        double width = this.bounds.getWidth();
        double height = this.bounds.getHeight();

        int x = olbl.screenPoint.x;
        int y = olbl.screenPoint.y;

        // Adjust x to account for text alignment
        int xAligned = x;
        if (AVKey.CENTER.equals(textAlign))
            xAligned = x - (int) (width / 2);
        else if (AVKey.RIGHT.equals(textAlign))
            xAligned = x - (int) width;

        // We draw text top-down, so adjust y to compensate.
        int yAligned = (int) (y - height);

        // Apply insets
        Insets insets = this.getInsets();
        xAligned -= insets.left;
        width = width + insets.left + insets.right;
        yAligned -= insets.bottom;
        height = height + insets.bottom + insets.top;

        if (!dc.isPickingMode())
        {
            // Apply the frame background color and opacity if we're in normal rendering mode.
            Color color = this.computeBackgroundColor(this.getMaterial().getDiffuse());
            gl.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) (this.interiorOpacity < 1 ? (int) (this.interiorOpacity * 255 + 0.5) : 255));
        }

        try
        {
            // Draw a quad
            gl.glPushMatrix();
            gl.glTranslated(xAligned, yAligned, 0);
            gl.glScaled(width, height, 1.0);
            dc.drawUnitQuad();
        }
        finally
        {
            gl.glPopMatrix();
        }
    }

    /**
     * Draw the label's text. This method assumes that the text renderer context has already been set up.
     *
     * @param textRenderer renderer to use.
     * @param olbl         The ordered label to draw.
     */
    protected void doDrawText(TextRenderer textRenderer, OrderedLabel olbl)
    {
        Color color = this.material.getDiffuse();
        Color backgroundColor = this.computeBackgroundColor(color);
        float opacity = (float) this.getOpacity();

        int x = olbl.screenPoint.x;
        int y = olbl.screenPoint.y;

        float[] compArray = new float[3];
        if (AVKey.TEXT_EFFECT_SHADOW.equals(this.effect) && backgroundColor != null)
        {
            backgroundColor.getRGBColorComponents(compArray);

            textRenderer.setColor(compArray[0], compArray[1], compArray[2], opacity);
            this.drawMultiLineText(textRenderer, x + 1, y - 1, olbl);
        }

        color.getRGBColorComponents(compArray);
        textRenderer.setColor(compArray[0], compArray[1], compArray[2], opacity);
        this.drawMultiLineText(textRenderer, x, y, olbl);
    }

    protected void drawMultiLineText(TextRenderer textRenderer, int x, int y, OrderedLabel olbl)
    {
        if (this.lines == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (int i = 0; i < this.lines.length; i++)
        {
            String line = this.lines[i];
            Rectangle2D bounds = this.lineBounds[i];

            int xAligned = x;
            if (this.textAlign.equals(AVKey.CENTER))
                xAligned = x - (int) (bounds.getWidth() / 2);
            else if (this.textAlign.equals(AVKey.RIGHT))
                xAligned = x - (int) (bounds.getWidth());

            y -= this.lineHeight;
            textRenderer.draw3D(line, xAligned, y, 0, 1);
            y -= this.lineSpacing;
        }
    }

    /**
     * Draws this ordered renderable and all subsequent Label ordered renderables in the ordered renderable list. This
     * method differs from {@link #drawBatchedText(gov.nasa.worldwind.render.DrawContext, TextRenderer,
     * gov.nasa.worldwind.symbology.TacticalGraphicLabel.OrderedLabel) drawBatchedText} in that this method
     * re-initializes the text renderer to draw the next label, while {@code drawBatchedText} re-uses the active text
     * renderer context. That is, {@code drawBatchedText} attempts to draw as many labels as possible that share same
     * text renderer configuration as this label, and this method attempts to draw as many labels as possible regardless
     * of the text renderer configuration of the subsequent labels.
     *
     * @param dc         the current draw context.
     * @param firstLabel the label drawn prior to calling this method.
     */
    protected void drawBatched(DrawContext dc, OrderedLabel firstLabel)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem != null && nextItem instanceof OrderedLabel)
            {
                OrderedLabel nextLabel = (OrderedLabel) nextItem;
                if (!nextLabel.isEnableBatchRendering())
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                nextLabel.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
        else if (this.isEnableBatchPicking())
        {
            while (nextItem != null && nextItem instanceof OrderedLabel)
            {
                OrderedLabel nextLabel = (OrderedLabel) nextItem;
                if (!nextLabel.isEnableBatchRendering() || !nextLabel.isEnableBatchPicking())
                    break;

                if (nextLabel.getPickLayer() != firstLabel.getPickLayer()) // batch pick only within a single layer
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                nextLabel.doDrawOrderedRenderable(dc, this.pickSupport);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    /**
     * Draws text for subsequent Label ordered renderables in the ordered renderable list. This method is called after
     * the text renderer has been set up (after beginRendering has been called), so this method can only draw text for
     * subsequent labels that use the same font and rotation as this label. This method differs from {@link
     * #drawBatched(gov.nasa.worldwind.render.DrawContext, gov.nasa.worldwind.symbology.TacticalGraphicLabel.OrderedLabel)
     * drawBatched} in that this method reuses the active text renderer context to draw as many labels as possible
     * without switching text renderer state.
     *
     * @param dc           the current draw context.
     * @param textRenderer Text renderer used to draw the label.
     * @param firstLabel   The first ordered renderable in the batch.
     */
    protected void drawBatchedText(DrawContext dc, TextRenderer textRenderer, OrderedLabel firstLabel)
    {
        // Draw as many as we can in a batch to save ogl state switching.
        Object nextItem = dc.peekOrderedRenderables();

        if (!dc.isPickingMode())
        {
            while (nextItem != null && nextItem instanceof OrderedLabel)
            {
                OrderedLabel nextLabel = (OrderedLabel) nextItem;
                if (!nextLabel.isEnableBatchRendering())
                    break;

                boolean sameFont = firstLabel.getFont().equals(nextLabel.getFont());
                boolean sameRotation = (firstLabel.rotation == null && nextLabel.rotation == null)
                    || (firstLabel.rotation != null && firstLabel.rotation.equals(nextLabel.rotation));
                boolean drawInterior = nextLabel.isDrawInterior();

                // We've already set up the text renderer state, so we can can't change the font or text rotation.
                // Also can't batch render if the next label needs an interior since that will require tearing down the
                // text renderer context.
                if (!sameFont || !sameRotation || drawInterior)
                    break;

                dc.pollOrderedRenderables(); // take it off the queue
                nextLabel.doDrawText(textRenderer);

                nextItem = dc.peekOrderedRenderables();
            }
        }
    }

    /**
     * Indicates the object that represents this label during picking.
     *
     * @return If a delegate owner is set, returns the delegate owner. Otherwise returns this label.
     */
    protected Object getPickedObject()
    {
        Object owner = this.getDelegateOwner();
        return (owner != null) ? owner : this;
    }

    /**
     * Determine the screen rectangle covered by a label. The input coordinate identifies either the top left, top
     * center, or top right corner of the label, depending on the text alignment. If the label is rotated to align with
     * features on the surface then the extent will be the smallest screen rectangle that completely encloses the
     * rotated label.
     *
     * @param x    X coordinate at which to draw the label.
     * @param y    Y coordinate at which to draw the label.
     * @param olbl The ordered label to compute extents for.
     *
     * @return The rectangle, in OGL screen coordinates (origin at bottom left corner), that is covered by the label.
     */
    protected Rectangle computeTextExtent(int x, int y, OrderedLabel olbl)
    {
        double width = this.bounds.getWidth();
        double height = this.bounds.getHeight();

        String textAlign = this.getTextAlign();

        int xAligned = x;
        if (AVKey.CENTER.equals(textAlign))
            xAligned = x - (int) (width / 2);
        else if (AVKey.RIGHT.equals(textAlign))
            xAligned = x - (int) width;

        int yAligned = (int) (y - height);

        Rectangle screenRect = new Rectangle(xAligned, yAligned, (int) width, (int) height);

        // Compute bounds of the rotated rectangle, if there is a rotation angle.
        if (olbl.rotation != null && olbl.rotation.degrees != 0)
        {
            screenRect = this.computeRotatedScreenExtent(screenRect, x, y, olbl.rotation);
        }

        return screenRect;
    }

    /**
     * Compute the bounding screen extent of a rotated rectangle.
     *
     * @param rect     Rectangle to rotate.
     * @param x        X coordinate of the rotation point.
     * @param y        Y coordinate of the rotation point.
     * @param rotation Rotation angle.
     *
     * @return The smallest rectangle that completely contains {@code rect} when rotated by the specified angle.
     */
    protected Rectangle computeRotatedScreenExtent(Rectangle rect, int x, int y, Angle rotation)
    {
        Rectangle r = new Rectangle(rect);

        // Translate the rectangle to the rotation point.
        r.translate(-x, -y);

        // Compute corner points
        Vec4[] corners = {
            new Vec4(r.getMaxX(), r.getMaxY()),
            new Vec4(r.getMaxX(), r.getMinY()),
            new Vec4(r.getMinX(), r.getMaxY()),
            new Vec4(r.getMinX(), r.getMinY())
        };

        // Rotate the rectangle
        Matrix rotationMatrix = Matrix.fromRotationZ(rotation);
        for (int i = 0; i < corners.length; i++)
        {
            corners[i] = corners[i].transformBy3(rotationMatrix);
        }

        // Find the bounding rectangle of rotated points.
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -Integer.MAX_VALUE;
        int maxY = -Integer.MAX_VALUE;

        for (Vec4 v : corners)
        {
            if (v.x > maxX)
                maxX = (int) v.x;

            if (v.x < minX)
                minX = (int) v.x;

            if (v.y > maxY)
                maxY = (int) v.y;

            if (v.y < minY)
                minY = (int) v.y;
        }

        // Set bounds and translate the rectangle back to where it started.
        r.setBounds(minX, minY, maxX - minX, maxY - minY);
        r.translate(x, y);

        return r;
    }

    /**
     * Compute a contrasting background color to draw the label's outline.
     *
     * @param color Label color.
     *
     * @return A color that contrasts with {@code color}.
     */
    protected Color computeBackgroundColor(Color color)
    {
        float[] colorArray = new float[4];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), colorArray);

        if (colorArray[2] > 0.5)
            return new Color(0, 0, 0, 0.7f);
        else
            return new Color(1, 1, 1, 0.7f);
    }
}
