/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;

/**
 * {@link Annotation} attributes set. All {@link AbstractAnnotation} objects start life referencing a new instance of
 * this object. <p> This class also defines a static <b>default</b> attributes bundle containing default values for all
 * attributes. New <code>AnnotationAttributes</code> refer this static bundle as their default values source when an
 * attribute has not been set. </p> <p> New <code>AnnotationAttributes</code> set have all their attributes pointing to
 * the default values until they are set by the application. Most attributes refer to the default value by using minus
 * one (<code>-1</code>) for numerics and <code>null</code> for objects. </p> <p> The default attributes set can be
 * changed for a non static one under the application control. The process can be extended or cascaded to handle
 * multiple levels of inheritance for default attributes. </p>
 *
 * @author Patrick Murris
 * @version $Id: AnnotationAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see AbstractAnnotation
 * @see MultiLineTextRenderer
 */
public class AnnotationAttributes implements Restorable
{
    private static final AnnotationAttributes defaults = new AnnotationAttributes();

    static
    {
        defaults.setFrameShape(AVKey.SHAPE_RECTANGLE);
        defaults.setSize(new Dimension(160, 0));
        defaults.setScale(1);
        defaults.setOpacity(1);
        defaults.setLeader(AVKey.SHAPE_TRIANGLE);
        defaults.setLeaderGapWidth(40);
        defaults.setCornerRadius(20);
        defaults.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        defaults.setDrawOffset(new Point(40, 60));
        defaults.setHighlightScale(1.2);
        defaults.setInsets(new Insets(20, 15, 15, 15));
        defaults.setFont(Font.decode("Arial-PLAIN-12"));
        defaults.setTextAlign(AVKey.LEFT);
        defaults.setTextColor(Color.BLACK);
        defaults.setBackgroundColor(Color.WHITE);
        defaults.setBorderColor(new Color(171, 171, 171));
        defaults.setBorderWidth(1);
        defaults.setBorderStippleFactor(0);
        defaults.setBorderStipplePattern((short) 0xAAAA);
        defaults.setAntiAliasHint(Annotation.ANTIALIAS_NICEST);
        defaults.setImageScale(1);
        defaults.setImageOffset(new Point(0, 0));
        defaults.setImageOpacity(1);
        defaults.setImageRepeat(AVKey.REPEAT_XY);
        defaults.setDistanceMinScale(1);
        defaults.setDistanceMaxScale(1);
        defaults.setDistanceMinOpacity(.3);
        defaults.setEffect(AVKey.TEXT_EFFECT_NONE);
    }

    private AnnotationAttributes defaultAttributes = defaults;

    private String frameShape;                              // Use default (null)
    private Dimension size;                                 // Use default (null)
    private double scale = -1;                              // Use default (-1)
    private double opacity = -1;                            // Use default (-1)
    private String leader;                                  // Use default (null)
    private int leaderGapWidth = -1;                        // Use default (-1)
    private int cornerRadius = -1;                          // Use default (-1)
    private String adjustWidthToText;                       // Use default (null)
    private Point drawOffset;                               // Use default (null)
    private boolean isHighlighted = false;
    private boolean isVisible = true;
    private double highlightScale = -1;                     // Use default (-1)
    private Font font;                                      // Use default (null)
    private String textAlign;                               // Use default (null)
    private Color textColor;                                // Use default (null)
    private Color backgroundColor;                          // Use default (null)
    private Color borderColor;                              // Use default (null)
    private double borderWidth = -1;                        // Use default (-1)
    private int borderStippleFactor = -1;                   // Use default (-1)
    private short borderStipplePattern = (short) 0x0000;    // Use default (zero)
    private int antiAliasHint = -1;                         // Use default (-1)
    private Insets insets;                                  // Use default (null)
    private WWTexture backgroundTexture;
    private WWTexture previousBackgroundTexture;
    private double imageScale = -1;                         // Use default (-1)
    private Point imageOffset;                              // Use default (null)
    private double imageOpacity = -1;                       // Use default (-1)
    private String imageRepeat;                             // Use default (null)
    private double distanceMinScale = -1;                   // Use default (-1)
    private double distanceMaxScale = -1;                   // Use default (-1)
    private double distanceMinOpacity = -1;                 // Use default (-1)
    private String effect;                                  // Use default (null)
    protected boolean unresolved;

    //** Public properties **********************************************************************

    /**
     * Set the fallback default attributes set.
     *
     * @param attr the default attributes set.
     */
    public void setDefaults(AnnotationAttributes attr)
    {
        if (attr == null)
        {
            String message = Logging.getMessage("nullValue.AnnotationAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.defaultAttributes = attr;
    }

    /**
     * Get the callout frame shape. Can be one of <code>AVKey.SHAPE_RECTANGLE</code> (default),
     * <code>AVKey.SHAPE_ELLIPSE</code> or <code>AVKey.SHAPE_NONE</code>.
     *
     * @return the callout frame shape.
     */
    public String getFrameShape()
    {
        return this.frameShape != null ? this.frameShape : defaultAttributes.getFrameShape();
    }

    /**
     * Set the callout frame shape. Can be one of <code>AVKey.SHAPE_RECTANGLE</code> (default),
     * <code>AVKey.SHAPE_ELLIPSE</code> or <code>AVKey.SHAPE_NONE</code>. Set to <code>null</code> to use the default
     * shape. <p> Note that <code>AVKey.SHAPE_ELLIPSE</code> draws an ellipse <u>inside</u> the callout bounding
     * rectangle set by its size (see setSize()) or its text bounding rectangle (see setAdjustWidthToText() and
     * setSize() with height set to zero). It is often necessary to have larger Insets dimensions (see setInsets()) to
     * avoid having the text drawn outside the shape border. </p>
     *
     * @param shape the callout frame shape.
     */
    public void setFrameShape(String shape)
    {
        this.frameShape = shape;
    }

    /**
     * Get whether the <code>Annotation</code> is highlighted and should be drawn bigger - see setHighlightScale().
     *
     * @return true if highlighted.
     */
    public boolean isHighlighted()
    {
        return isHighlighted;
    }

    /**
     * Set whether the <code>Annotation</code> is highlighted and should be drawn bigger - see setHighlightScale().
     *
     * @param highlighted true if highlighted.
     */
    public void setHighlighted(boolean highlighted)
    {
        isHighlighted = highlighted;
    }

    /**
     * Get the scaling factor applied to highlighted <code>Annotations</code>.
     *
     * @return the scaling factor applied to highlighted <code>Annotations</code>.
     */
    public double getHighlightScale()
    {
        return highlightScale > 0 ? this.highlightScale : defaultAttributes.getHighlightScale();
    }

    /**
     * Set the scaling factor applied to highlighted <code>Annotations</code>. Set to minus one (<code>-1</code>) to use
     * the default value.
     *
     * @param highlightScale the scaling factor applied to highlighted <code>Annotations</code>.
     */
    public void setHighlightScale(double highlightScale)
    {
        this.highlightScale = highlightScale;
    }

    /**
     * Get the annotation callout preferred total dimension in pixels.
     *
     * @return the callout preferred total dimension in pixels.
     */
    public Dimension getSize()
    {
        return this.size != null ? this.size : defaultAttributes.getSize();
    }

    /**
     * Set the annotation callout preferred total dimension in pixels. <p> If necessary, the text will be wrapped into
     * several lines so as not to exceed the callout preferred <code><b>width</b></code> (minus the <code>Insets</code>
     * <code>left</code> and <code>right</code> dimensions - see {@link #setInsets(java.awt.Insets) setInsets}).
     * However, if {@link #setAdjustWidthToText(String) setAdjustWidthToText} is set to AVKey.SIZE_FIT_TEXT, the final
     * callout width will follow that of the final text bounding rectangle. </p> <p> If necessary, the text will also be
     * truncated so as not to exceed the given <code><b>height</b></code>. A <code>zero</code> value (default) will have
     * the callout follow the final text bounding rectangle height (including the <code>Insets</code> <code>top</code>
     * and <code>bottom</code>). </p> Set to <code>null</code> to use the default size.
     *
     * @param size the callout preferred total dimension in pixels.
     */
    public void setSize(Dimension size)
    {
        this.size = size;
    }

    /**
     * Get the scaling factor applied to the annotation. Default is 1.
     *
     * @return the scaling factor applied to the annotation
     */
    public double getScale()
    {
        return this.scale >= 0 ? this.scale : defaultAttributes.getScale();
    }

    /**
     * Set the scaling factor to apply to the annotation. Default is 1. Set to minus one (<code>-1</code>) to use the
     * default value.
     *
     * @param scale the scaling factor to apply to the annotation
     */
    public void setScale(double scale)
    {
        this.scale = scale;
    }

    /**
     * Get the opacity factor applied to the annotation. Default is 1.
     *
     * @return the opacity factor applied to the annotation
     */
    public double getOpacity()
    {
        return this.opacity >= 0 ? this.opacity : defaultAttributes.getOpacity();
    }

    /**
     * Set the opacity factor to apply to the annotation. Default is 1. Set to minus one (<code>-1</code>) to use the
     * default value.
     *
     * @param opacity the opacity factor to apply to the annotation
     */
    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
    }

    /**
     * Get the callout shape leader type. Can be one of <code>AVKey.SHAPE_TRIANGLE</code> (default) or
     * <code>AVKey.SHAPE_NONE</code>.
     *
     * @return the callout shape leader type.
     */
    public String getLeader()
    {
        return this.leader != null ? this.leader : defaultAttributes.getLeader();
    }

    /**
     * Set the callout shape leader type. Can be one of <code>AVKey.SHAPE_TRIANGLE</code> (default) or
     * <code>AVKey.SHAPE_NONE</code>.
     *
     * @param leader the callout shape leader type.
     */
    public void setLeader(String leader)
    {
        this.leader = leader;
    }

    /**
     * Get the callout shape leader gap width in pixels. Default is 40.
     *
     * @return the callout shape leader gap width.
     */
    public int getLeaderGapWidth()
    {
        return this.leaderGapWidth >= 0 ? this.leaderGapWidth : defaultAttributes.getLeaderGapWidth();
    }

    /**
     * Set the callout shape leader gap width in pixels. Set this attribute to minus one (<code>-1</code>) to use the
     * default value.
     *
     * @param width the callout shape leader gap width in pixels.
     */
    public void setLeaderGapWidth(int width)
    {
        this.leaderGapWidth = width;
    }

    /**
     * Get the callout shape rounded corners radius in pixels. A value of <code>zero</code> means no rounded corners.
     *
     * @return the callout shape rounded corners radius in pixels.
     */
    public int getCornerRadius()
    {
        return this.cornerRadius >= 0 ? this.cornerRadius : defaultAttributes.getCornerRadius();
    }

    /**
     * Set the callout shape rounded corners radius in pixels. A value of <code>zero</code> means no rounded corners.
     * Set this attribute to minus one (<code>-1</code>) to use the default value.
     *
     * @param radius the callout shape rounded corners radius in pixels.
     */
    public void setCornerRadius(int radius)
    {
        this.cornerRadius = radius;
    }

    /**
     * Get whether the callout width should adjust to follow the wrapped text bounding rectangle width, which may be
     * smaller or larger then the preferred size depending on the text. Can be one of {@link AVKey#SIZE_FIXED} or {@link
     * AVKey#SIZE_FIT_TEXT}.
     *
     * @return whether the callout width is adjusted to follow the text bounding rectangle width.
     */
    public String getAdjustWidthToText()
    {
        return this.adjustWidthToText != null ? this.adjustWidthToText : defaultAttributes.getAdjustWidthToText();
    }

    /**
     * Set whether the callout width should adjust to follow the wrapped text bounding rectangle width which may be
     * smaller or larger then the preferred size depending on the text. Can be one of {@link AVKey#SIZE_FIXED} (default)
     * or {@link AVKey#SIZE_FIT_TEXT}. Setting this attribute to <code>SIZE_FIT_TEXT</code> would have the callout drawn
     * at its exact width (see setSize()).
     *
     * @param state whether the callout width should adjust to follow the text bounding rectangle width.
     */
    public void setAdjustWidthToText(String state)
    {
        this.adjustWidthToText = state;
    }

    /**
     * Get the callout displacement offset in pixels from the globe Position or screen point at which it is associated.
     * When the callout has a leader (see setLeader(String leader)), it will lead to the original point. In the actual
     * implementation, the callout is drawn above its associated point and the leader connects at the bottom of the
     * frame, in the middle. Positive X increases toward the right and positive Y in the up direction.
     *
     * @return the callout displacement offset in pixels
     */
    public Point getDrawOffset()
    {
        return this.drawOffset != null ? this.drawOffset : defaultAttributes.getDrawOffset();
    }

    /**
     * Set the callout displacement offset in pixels from the globe Position or screen point at which it is associated.
     * When the callout has a leader (see setLeader(String leader)), it will lead to the original point. In the actual
     * implementation, the callout is drawn above its associated point and the leader connects at the bottom of the
     * frame, in the middle. Positive X increases toward the right and positive Y in the up direction. Set to
     * <code>null</code> to use the default offset.
     *
     * @param offset the callout displacement offset in pixels
     */
    public void setDrawOffset(Point offset)
    {
        this.drawOffset = offset;
    }

    /**
     * Get the callout <code>Insets</code> dimensions in pixels. The text is drawn inside the callout frame while
     * keeping a distance from the callout border defined in the Insets.
     *
     * @return the callout <code>Insets</code> dimensions in pixels.
     */
    public Insets getInsets()
    {
        return this.insets != null ? this.insets : defaultAttributes.getInsets();
    }

    /**
     * Set the callout <code>Insets</code> dimensions in pixels. The text will be drawn inside the callout frame while
     * keeping a distance from the callout border defined in the Insets. Set to <code>null</code> to use the default
     * Insets.
     *
     * @param insets the callout <code>Insets</code> dimensions in pixels.
     */
    public void setInsets(Insets insets)
    {
        this.insets = insets;
    }

    /**
     * Get the callout border line width. A value of <code>zero</code> means no border is being drawn.
     *
     * @return the callout border line width.
     */
    public double getBorderWidth()
    {
        return this.borderWidth >= 0 ? this.borderWidth : defaultAttributes.getBorderWidth();
    }

    /**
     * Set the callout border line width. A value of <code>zero</code> means no border will is drawn. Set to minus one
     * (<code>-1</code>) to use the default value.
     *
     * @param width the callout border line width.
     */
    public void setBorderWidth(double width)
    {
        this.borderWidth = width;
    }

    /**
     * Get the stipple factor used for the callout border line. A value of <code>zero</code> (default) means no pattern
     * is applied.
     *
     * @return the stipple factor used for the callout border line.
     */
    public int getBorderStippleFactor()
    {
        return this.borderStippleFactor >= 0 ? this.borderStippleFactor : defaultAttributes.getBorderStippleFactor();
    }

    /**
     * Set the stipple factor used for the callout border line. A value of <code>zero</code> (default) means no pattern
     * will be applied. Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param factor the stipple factor used for the callout border line.
     */
    public void setBorderStippleFactor(int factor)
    {
        this.borderStippleFactor = factor;
    }

    /**
     * Get the stipple pattern used for the callout border line.
     *
     * @return the stipple pattern used for the callout border line.
     */
    public short getBorderStipplePattern()
    {
        return this.borderStipplePattern != 0x0000 ? this.borderStipplePattern
            : defaultAttributes.getBorderStipplePattern();
    }

    /**
     * Set the stipple pattern used for the callout border line. Set to <code>0x0000</code> to use the default value.
     *
     * @param pattern the stipple pattern used for the callout border line.
     */
    public void setBorderStipplePattern(short pattern)
    {
        this.borderStipplePattern = pattern;
    }

    /**
     * Get the <code>GL</code> antialias hint used for rendering the callout border line. Can be one of {@link
     * Annotation}.ANTIALIAS_DONT_CARE, ANTIALIAS_FASTEST (default) or ANTIALIAS_NICEST.
     *
     * @return the <code>GL</code> antialias hint used for rendering the callout border line.
     */
    protected int getAntiAliasHint()
    {
        return this.antiAliasHint >= 0 ? this.antiAliasHint : defaultAttributes.getAntiAliasHint();
    }

    /**
     * Set the <code>GL</code> antialias hint used for rendering the callout border line. Can be one of {@link
     * Annotation}.ANTIALIAS_DONT_CARE, ANTIALIAS_FASTEST (default) or ANTIALIAS_NICEST. Set to minus one
     * (<code>-1</code>) to use the default value.
     *
     * @param hint the <code>GL</code> antialias hint used for rendering the callout border line.
     */
    protected void setAntiAliasHint(int hint)
    {
        this.antiAliasHint = hint;
    }

    /**
     * Get whether the annotation is visible and should be rendered.
     *
     * @return true if the annotation is visible and should be rendered.
     */
    public boolean isVisible()
    {
        return isVisible;
    }

    /**
     * Set whether the annotation is visible and should be rendered.
     *
     * @param visible true if the annotation is visible and should be rendered.
     */
    public void setVisible(boolean visible)
    {
        isVisible = visible;
    }

    /**
     * Get the <code>Font</code> used for text rendering.
     *
     * @return the <code>Font</code> used for text rendering.
     */
    public Font getFont()
    {
        return this.font != null ? this.font : defaultAttributes.getFont();
    }

    /**
     * Set the <code>Font</code> used for text rendering. Set to <code>null</code> to use the default value.
     *
     * @param font the <code>Font</code> used for text rendering.
     */
    public void setFont(Font font)
    {
        this.font = font;
    }

    /**
     * Get the text alignement. Can be one of {@link AVKey#LEFT} (default), {@link AVKey#CENTER} or {@link
     * AVKey#RIGHT}.
     *
     * @return align the text alignement. Can be one of MultiLineTextRenderer.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT.
     */
    public String getTextAlign()
    {
        return this.textAlign != null ? this.textAlign : defaultAttributes.getTextAlign();
    }

    /**
     * Set the text alignement. Can be one of {@link AVKey#LEFT} (default), {@link AVKey#CENTER} or {@link AVKey#RIGHT}.
     * Set to <code>null</code> to use the default value.
     *
     * @param align the text alignement.
     */
    public void setTextAlign(String align)
    {
        this.textAlign = align;
    }

    /**
     * Get the text <code>Color</code>.
     *
     * @return the text <code>Color</code>.
     */
    public Color getTextColor()
    {
        return this.textColor != null ? this.textColor : defaultAttributes.getTextColor();
    }

    /**
     * Set the text <code>Color</code>. Set to <code>null</code> to use the default value.
     *
     * @param color the text <code>Color</code>.
     */
    public void setTextColor(Color color)
    {
        this.textColor = color;
    }

    /**
     * Get the callout background <code>Color</code>.
     *
     * @return the callout background <code>Color</code>.
     */
    public Color getBackgroundColor()
    {
        return this.backgroundColor != null ? this.backgroundColor : defaultAttributes.getBackgroundColor();
    }

    /**
     * Set the callout background <code>Color</code>. Set to <code>null</code> to use the default value.
     *
     * @param color the callout background <code>Color</code>.
     */
    public void setBackgroundColor(Color color)
    {
        this.backgroundColor = color;
    }

    /**
     * Get the callout border <code>Color</code>.
     *
     * @return the callout border <code>Color</code>.
     */
    public Color getBorderColor()
    {
        return this.borderColor != null ? this.borderColor : defaultAttributes.getBorderColor();
    }

    /**
     * Set the callout border <code>Color</code>. Set to <code>null</code> to use the default value.
     *
     * @param color the callout border <code>Color</code>.
     */
    public void setBorderColor(Color color)
    {
        this.borderColor = color;
    }

    /**
     * Get the background image source. Can be a <code>String</code> providing the path to a local image, a {@link
     * java.awt.image.BufferedImage} or <code>null</code>.
     *
     * @return the background image source.
     */
    public Object getImageSource()
    {
        return (this.backgroundTexture != null) ? this.backgroundTexture.getImageSource() : null;
    }

    /**
     * Set the background image source. Can be a <code>String</code> providing the path to a local image or a {@link
     * java.awt.image.BufferedImage}. Set to null for no background image rendering.
     *
     * @param imageSource the background image source.
     */
    public void setImageSource(Object imageSource)
    {
        this.previousBackgroundTexture = this.backgroundTexture;
        this.backgroundTexture = null;

        if (imageSource != null)
        {
            this.backgroundTexture = new BasicWWTexture(imageSource, true);
        }
    }

    /**
     * Get the background image as a {@link WWTexture} for the specified draw context. This returns null if the
     * background image source returned by {@link #getImageSource()} is null.
     *
     * @param dc the current draw context.
     *
     * @return the background image as a WWTexture, or null if this AnnotationAttributes has no background image
     *         source.
     */
    public WWTexture getBackgroundTexture(DrawContext dc)
    {
        if (this.previousBackgroundTexture != null)
        {
            dc.getTextureCache().remove(this.previousBackgroundTexture.getImageSource());
            this.previousBackgroundTexture = null;
        }

        return this.backgroundTexture;
    }

    /**
     * Get the background image scaling factor.
     *
     * @return the background image scaling factor.
     */
    public double getImageScale()
    {
        return this.imageScale >= 0 ? this.imageScale : defaultAttributes.getImageScale();
    }

    /**
     * Set the background image scaling factor. Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param scale the background image scaling factor.
     */
    public void setImageScale(double scale)
    {
        this.imageScale = scale;
    }

    /**
     * Get the background image offset in pixels (before background scaling).
     *
     * @return the background image offset in pixels
     */
    public Point getImageOffset()
    {
        return this.imageOffset != null ? this.imageOffset : defaultAttributes.getImageOffset();
    }

    /**
     * Set the background image offset in pixels (before background scaling). Set to <code>null</code> to use the
     * default value.
     *
     * @param offset the background image offset in pixels
     */
    public void setImageOffset(Point offset)
    {
        this.imageOffset = offset;
    }

    /**
     * Get the opacity of the background image (0 to 1).
     *
     * @return the opacity of the background image (0 to 1).
     */
    public double getImageOpacity()
    {
        return this.imageOpacity >= 0 ? this.imageOpacity : defaultAttributes.getImageOpacity();
    }

    /**
     * Set the opacity of the background image (0 to 1). Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param opacity the opacity of the background image (0 to 1).
     */
    public void setImageOpacity(double opacity)
    {
        this.imageOpacity = opacity;
    }

    /**
     * Get the repeat behavior or the background image. Can be one of {@link Annotation}.IMAGE_REPEAT_X, IMAGE_REPEAT_Y,
     * IMAGE_REPEAT_XY (default) or IMAGE_REPEAT_NONE.
     *
     * @return the repeat behavior or the background image.
     */
    public String getImageRepeat()
    {
        return this.imageRepeat != null ? this.imageRepeat : defaultAttributes.getImageRepeat();
    }

    /**
     * Set the repeat behavior or the background image. Can be one of {@link Annotation}.IMAGE_REPEAT_X, IMAGE_REPEAT_Y,
     * IMAGE_REPEAT_XY (default) or IMAGE_REPEAT_NONE. Set to <code>null</code> to use the default value.
     *
     * @param repeat the repeat behavior or the background image.
     */
    public void setImageRepeat(String repeat)
    {
        this.imageRepeat = repeat;
    }

    /**
     * Get the path to the image used for background image. Returns <code>null</code> if the image source is null or a
     * memory BufferedImage.
     *
     * @return the path to the image used for background image.
     */
    public String getPath()
    {
        Object imageSource = this.getImageSource();
        return (imageSource instanceof String) ? (String) imageSource : null;
    }

    /**
     * Get the minimum scale that can be applied to an annotation when it gets farther away from the eye than the view
     * lookat point.
     *
     * @return the minimum scale that can be applied to an annotation when it gets away from the eye
     */
    public double getDistanceMinScale()
    {
        return this.distanceMinScale >= 0 ? this.distanceMinScale : defaultAttributes.getDistanceMinScale();
    }

    /**
     * Set the minimum scale that can be applied to an annotation when it gets farther away from the eye than the view
     * lookat point. Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param scale the minimum scale that can be applied to an annotation when it gets away from the eye
     */
    public void setDistanceMinScale(double scale)
    {
        this.distanceMinScale = scale;
    }

    /**
     * Get the maximum scale that can be applied to an annotation when it gets closer to the eye than the view lookat
     * point.
     *
     * @return the maximum scale that can be applied to an annotation when it gets closer to the eye
     */
    public double getDistanceMaxScale()
    {
        return this.distanceMaxScale >= 0 ? this.distanceMaxScale : defaultAttributes.getDistanceMaxScale();
    }

    /**
     * Set the maximum scale that can be applied to an annotation when it gets closer to the eye than the view lookat
     * point. Set to minus one (<code>-1</code>) to use the default value.
     *
     * @param scale the maximum scale that can be applied to an annotation when it gets closer to the eye
     */
    public void setDistanceMaxScale(double scale)
    {
        this.distanceMaxScale = scale;
    }

    /**
     * Get the minimum opacity an annotation can have when fading away from the eye (0 to 1).
     *
     * @return the minimum opacity an annotation can have when fading away from the eye.
     */
    public double getDistanceMinOpacity()
    {
        return this.distanceMinOpacity >= 0 ? this.distanceMinOpacity : defaultAttributes.getDistanceMinOpacity();
    }

    /**
     * Set the minimum opacity an annotation can have when fading away from the eye (0 to 1). Set to minus one
     * (<code>-1</code>) to use the default value.
     *
     * @param opacity the minimum opacity an annotation can have when fading away from the eye.
     */
    public void setDistanceMinOpacity(double opacity)
    {
        this.distanceMinOpacity = opacity;
    }

    /**
     * Get the effect used to decorate the text. Can be one of {@link AVKey#TEXT_EFFECT_SHADOW}, {@link
     * AVKey#TEXT_EFFECT_OUTLINE} or {@link AVKey#TEXT_EFFECT_NONE} (default).
     *
     * @return the effect used for text rendering
     */
    public String getEffect()
    {
        return this.effect != null ? this.effect : defaultAttributes.getEffect();
    }

    /**
     * Set the effect used to decorate the text. Can be one of {@link AVKey#TEXT_EFFECT_SHADOW}, {@link
     * AVKey#TEXT_EFFECT_OUTLINE} or {@link AVKey#TEXT_EFFECT_NONE} (default). Set to <code>null</code> to use the
     * default value.
     *
     * @param effect the effect to use for text rendering
     */
    public void setEffect(String effect)
    {
        this.effect = effect;
    }

    /**
     * Indicates whether one or more members of <i>this</i> remain unresolved because they must be retrieved from an
     * external source.
     *
     * @return true if there are unresolved fields, false if no fields remain unresolved.
     */
    public boolean isUnresolved()
    {
        return unresolved;
    }

    /**
     * Specifies whether one or more fields of <i>this</> remain unresolved because they must be retrieved from an
     * external source.
     *
     * @param unresolved true if there are unresolved fields, false if no fields remain unresolved.
     */
    public void setUnresolved(boolean unresolved)
    {
        this.unresolved = unresolved;
    }

    /**
     * Returns an XML state document String describing attributes that have been set by the application (attributes not
     * pointing to their default value).
     *
     * @return XML state document string describing this AnnotationAttributes.
     */
    public String getRestorableState()
    {
        RestorableSupport restorableSupport = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (restorableSupport == null)
            return null;

        // Save application set attributes to the document root.
        saveAttributes(this, restorableSupport, null);

        // We only save this AnnotationAttributes' defaultAttributes when the application has set them to
        // something other than the static member "defaults".
        if (this.defaultAttributes != AnnotationAttributes.defaults)
        {
            RestorableSupport.StateObject defaultAttributesStateObj =
                restorableSupport.addStateObject("defaultAttributes");
            saveAttributes(this.defaultAttributes, restorableSupport, defaultAttributesStateObj);
        }

        return restorableSupport.getStateAsXml();
    }

    /**
     * Restores attribute values found in the specified XML state document String. The document specified by
     * <code>stateInXml</code> must be a well formed XML document String, or this will throw an
     * IllegalArgumentException. Unknown structures in <code>stateInXml</code> are benign, because they will simply be
     * ignored.
     *
     * @param stateInXml an XML document String describing an AnnotationAttributes.
     *
     * @throws IllegalArgumentException If <code>stateInXml</code> is null, or if <code>stateInXml</code> is not a well
     *                                  formed XML document String.
     */
    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport restorableSupport;
        try
        {
            restorableSupport = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        // Restore application set attributes from under the document root.
        restoreAttributes(restorableSupport, null, this);

        // Restore application set default attributes from under the "defaultAttributes" state element.
        RestorableSupport.StateObject defaultAttributesStateObj =
            restorableSupport.getStateObject("defaultAttributes");
        if (defaultAttributesStateObj != null)
        {
            AnnotationAttributes newDefaultAttributes = this.defaultAttributes;
            // We do not want to write to the static member "defaults". So if this AnnotationAttributes' does not
            // have it's own defaultAttributes instance, we create one for it
            if (newDefaultAttributes == AnnotationAttributes.defaults)
                newDefaultAttributes = new AnnotationAttributes();
            restoreAttributes(restorableSupport, defaultAttributesStateObj, newDefaultAttributes);
            setDefaults(newDefaultAttributes);
        }
    }

    /**
     * Save the attributes specified by <code>source</code> in the specified <code>restorableSupport</code>. Only
     * attributes that have been set by the application (attributes not pointing to their default value) will be saved.
     * If <code>context</code> is not null, attributes will be saved beneath it. Otherwise, they will be saved at the
     * document root.
     *
     * @param source            the AnnotationAttriubutes to save.
     * @param restorableSupport RestorableSupport to write attribute values to.
     * @param context           RestorableSupport.StateObject that attributes will be saved under, if not null.
     *
     * @throws IllegalArgumentException If either <code>source</code> or <code>restorableSupport</code> is null.
     */
    private static void saveAttributes(AnnotationAttributes source,
        RestorableSupport restorableSupport,
        RestorableSupport.StateObject context)
    {
        if (source == null || restorableSupport == null)
            throw new IllegalArgumentException();

        if (source.frameShape != null)
            restorableSupport.addStateValueAsString(context, "frameShape", source.frameShape);

        restorableSupport.addStateValueAsBoolean(context, "highlighted", source.isHighlighted);

        if (source.highlightScale >= 0)
            restorableSupport.addStateValueAsDouble(context, "highlightScale", source.highlightScale);

        if (source.size != null)
        {
            RestorableSupport.StateObject sizeStateObj = restorableSupport.addStateObject(context, "size");
            if (sizeStateObj != null)
            {
                restorableSupport.addStateValueAsDouble(sizeStateObj, "width", source.size.getWidth());
                restorableSupport.addStateValueAsDouble(sizeStateObj, "height", source.size.getHeight());
            }
        }

        if (source.scale >= 0)
            restorableSupport.addStateValueAsDouble(context, "scale", source.scale);

        if (source.opacity >= 0)
            restorableSupport.addStateValueAsDouble(context, "opacity", source.opacity);

        if (source.leader != null)
            restorableSupport.addStateValueAsString(context, "leader", source.leader);

        if (source.leaderGapWidth >= 0)
            restorableSupport.addStateValueAsInteger(context, "leaderGapWidth", source.leaderGapWidth);

        if (source.cornerRadius >= 0)
            restorableSupport.addStateValueAsInteger(context, "cornerRadius", source.cornerRadius);

        if (source.adjustWidthToText != null)
            restorableSupport.addStateValueAsString(context, "adjustWidthToText", source.adjustWidthToText);

        if (source.drawOffset != null)
        {
            RestorableSupport.StateObject drawOffsetStateObj = restorableSupport.addStateObject(context, "drawOffset");
            if (drawOffsetStateObj != null)
            {
                restorableSupport.addStateValueAsDouble(drawOffsetStateObj, "x", source.drawOffset.getX());
                restorableSupport.addStateValueAsDouble(drawOffsetStateObj, "y", source.drawOffset.getY());
            }
        }

        if (source.insets != null)
        {
            RestorableSupport.StateObject insetsStateObj = restorableSupport.addStateObject(context, "insets");
            if (insetsStateObj != null)
            {
                restorableSupport.addStateValueAsInteger(insetsStateObj, "top", source.insets.top);
                restorableSupport.addStateValueAsInteger(insetsStateObj, "left", source.insets.left);
                restorableSupport.addStateValueAsInteger(insetsStateObj, "bottom", source.insets.bottom);
                restorableSupport.addStateValueAsInteger(insetsStateObj, "right", source.insets.right);
            }
        }

        if (source.borderWidth >= 0)
            restorableSupport.addStateValueAsDouble(context, "borderWidth", source.borderWidth);

        if (source.borderStippleFactor >= 0)
            restorableSupport.addStateValueAsInteger(context, "borderStippleFactor", source.borderStippleFactor);

        if (source.borderStipplePattern != (short) 0x0000)
            restorableSupport.addStateValueAsInteger(context, "borderStipplePattern", source.borderStipplePattern);

        if (source.antiAliasHint >= 0)
            restorableSupport.addStateValueAsInteger(context, "antiAliasHint", source.antiAliasHint);

        restorableSupport.addStateValueAsBoolean(context, "visible", source.isVisible);

        // Save the name, style, and size of the font. These will be used to restore the font using the
        // constructor: new Font(name, style, size).
        if (source.font != null)
        {
            RestorableSupport.StateObject fontStateObj = restorableSupport.addStateObject(context, "font");
            if (fontStateObj != null)
            {
                restorableSupport.addStateValueAsString(fontStateObj, "name", source.font.getName());
                restorableSupport.addStateValueAsInteger(fontStateObj, "style", source.font.getStyle());
                restorableSupport.addStateValueAsInteger(fontStateObj, "size", source.font.getSize());
            }
        }

        if (source.textAlign != null)
            restorableSupport.addStateValueAsString(context, "textAlign", source.textAlign);

        if (source.textColor != null)
        {
            String encodedColor = RestorableSupport.encodeColor(source.textColor);
            if (encodedColor != null)
                restorableSupport.addStateValueAsString(context, "textColor", encodedColor);
        }

        if (source.backgroundColor != null)
        {
            String encodedColor = RestorableSupport.encodeColor(source.backgroundColor);
            if (encodedColor != null)
                restorableSupport.addStateValueAsString(context, "backgroundColor", encodedColor);
        }

        if (source.borderColor != null)
        {
            String encodedColor = RestorableSupport.encodeColor(source.borderColor);
            if (encodedColor != null)
                restorableSupport.addStateValueAsString(context, "borderColor", encodedColor);
        }

        // Save the imagePath property only when the imageSource property is a simple String path. If the imageSource
        // property is a BufferedImage (or some other object), we make no effort to save that state. We save under
        // the name "imagePath" to denote that it is a special case of "imageSource".
        if (source.getPath() != null)
            restorableSupport.addStateValueAsString(context, "imagePath", source.getPath(), true);

        if (source.imageScale >= 0)
            restorableSupport.addStateValueAsDouble(context, "imageScale", source.imageScale);

        if (source.imageOffset != null)
        {
            RestorableSupport.StateObject imageOffsetStateObj =
                restorableSupport.addStateObject(context, "imageOffset");
            if (imageOffsetStateObj != null)
            {
                restorableSupport.addStateValueAsDouble(imageOffsetStateObj, "x", source.imageOffset.getX());
                restorableSupport.addStateValueAsDouble(imageOffsetStateObj, "y", source.imageOffset.getY());
            }
        }

        if (source.imageOpacity >= 0)
            restorableSupport.addStateValueAsDouble(context, "imageOpacity", source.imageOpacity);

        if (source.imageRepeat != null)
            restorableSupport.addStateValueAsString(context, "imageRepeat", source.imageRepeat);

        if (source.distanceMinScale >= 0)
            restorableSupport.addStateValueAsDouble(context, "distanceMinScale", source.distanceMinScale);

        if (source.distanceMaxScale >= 0)
            restorableSupport.addStateValueAsDouble(context, "distanceMaxScale", source.distanceMaxScale);

        if (source.distanceMinOpacity >= 0)
            restorableSupport.addStateValueAsDouble(context, "distanceMinOpacity", source.distanceMinOpacity);

        if (source.effect != null)
            restorableSupport.addStateValueAsString(context, "effect", source.effect);
    }

    /**
     * Restores the any attributes appearing in the specified <code>restorableSupport</code>. If <code>context</code> is
     * not null, this will search for attributes beneath it. Otherwise, this will search for attributes beneath the
     * document root.
     *
     * @param restorableSupport RestorableSupport to read attribute values from.
     * @param context           RestorableSupport.StateObject under which attributes will be looked, if not null.
     * @param dest              the AnnotationAttributes to restore.
     *
     * @throws IllegalArgumentException If either <code>restorableSupport</code> or <code>dest</code> is null.
     */
    private static void restoreAttributes(RestorableSupport restorableSupport,
        RestorableSupport.StateObject context,
        AnnotationAttributes dest)
    {
        // Map legacy versions of the Annotation constants and FrameFactory constants to the new AVKey constants.
        Map<String, String> legacySupport = new HashMap<String, String>();
        legacySupport.put("render.Annotation.RepeatNone", AVKey.REPEAT_NONE);
        legacySupport.put("render.Annotation.RepeatX", AVKey.REPEAT_X);
        legacySupport.put("render.Annotation.RepeatY", AVKey.REPEAT_Y);
        legacySupport.put("render.Annotation.RepeatXY", AVKey.REPEAT_XY);
        legacySupport.put("render.Annotation.SizeFixed", AVKey.SIZE_FIXED);
        legacySupport.put("render.Annotation.SizeFitText", AVKey.SIZE_FIT_TEXT);
        legacySupport.put("Render.FrameFactory.ShapeRectangle", AVKey.SHAPE_RECTANGLE);
        legacySupport.put("Render.FrameFactory.ShapeEllipse", AVKey.SHAPE_ELLIPSE);
        legacySupport.put("Render.FrameFactory.ShapeNone", AVKey.SHAPE_NONE);
        legacySupport.put("Render.FrameFactory.LeaderTriangle", AVKey.SHAPE_TRIANGLE);
        legacySupport.put("Render.FrameFactory.LeaderNone", AVKey.SHAPE_NONE);

        if (restorableSupport == null || dest == null)
            throw new IllegalArgumentException();

        String frameShapeState = restorableSupport.getStateValueAsString(context, "frameShape");
        if (frameShapeState != null)
        {
            // Map legacy versions using FrameFactory frame shape constants to the new AVKey constants.
            String updatedValue = legacySupport.get(frameShapeState);
            if (updatedValue != null)
                frameShapeState = updatedValue;

            dest.setFrameShape(frameShapeState);
        }

        Boolean highlightedState = restorableSupport.getStateValueAsBoolean(context, "highlighted");
        if (highlightedState != null)
            dest.setHighlighted(highlightedState);

        Double highlightScaleState = restorableSupport.getStateValueAsDouble(context, "highlightScale");
        if (highlightScaleState != null)
            dest.setHighlightScale(highlightScaleState);

        // Restore the size property only if all parts are available.
        // We will not restore a partial size (for example, just the width).
        RestorableSupport.StateObject sizeStateObj = restorableSupport.getStateObject(context, "size");
        if (sizeStateObj != null)
        {
            Double widthState = restorableSupport.getStateValueAsDouble(sizeStateObj, "width");
            Double heightState = restorableSupport.getStateValueAsDouble(sizeStateObj, "height");
            if (widthState != null && heightState != null)
                dest.setSize(new Dimension(widthState.intValue(), heightState.intValue()));
        }

        Double scaleState = restorableSupport.getStateValueAsDouble(context, "scale");
        if (scaleState != null)
            dest.setScale(scaleState);

        Double opacityState = restorableSupport.getStateValueAsDouble(context, "opacity");
        if (opacityState != null)
            dest.setOpacity(opacityState);

        String leaderState = restorableSupport.getStateValueAsString(context, "leader");
        if (leaderState != null)
        {
            // Map legacy versions using FrameFactory leader shape constants to the new AVKey constants.
            String updatedValue = legacySupport.get(leaderState);
            if (updatedValue != null)
                leaderState = updatedValue;

            dest.setLeader(leaderState);
        }

        Integer leaderGapWidthState = restorableSupport.getStateValueAsInteger(context, "leaderGapWidth");
        if (leaderGapWidthState != null)
            dest.setLeaderGapWidth(leaderGapWidthState);

        Integer cornerRadiusState = restorableSupport.getStateValueAsInteger(context, "cornerRadius");
        if (cornerRadiusState != null)
            dest.setCornerRadius(cornerRadiusState);

        String adjustWidthToTextState = restorableSupport.getStateValueAsString(context, "adjustWidthToText");
        if (adjustWidthToTextState != null)
        {
            // Map legacy versions using Annotation size constants to the new AVKey constants.
            String updatedValue = legacySupport.get(adjustWidthToTextState);
            if (updatedValue != null)
                adjustWidthToTextState = updatedValue;

            dest.setAdjustWidthToText(adjustWidthToTextState);
        }

        // Restore the drawOffset property only if all parts are available.
        // We will not restore a partial drawOffset (for example, just the x value).
        RestorableSupport.StateObject drawOffsetStateObj = restorableSupport.getStateObject(context, "drawOffset");
        if (drawOffsetStateObj != null)
        {
            Double xState = restorableSupport.getStateValueAsDouble(drawOffsetStateObj, "x");
            Double yState = restorableSupport.getStateValueAsDouble(drawOffsetStateObj, "y");
            if (xState != null && yState != null)
                dest.setDrawOffset(new Point(xState.intValue(), yState.intValue()));
        }

        // Restore the insets property only if all parts are available.
        // We will not restore a partial insets (for example, just the top value).
        RestorableSupport.StateObject insetsStateObj = restorableSupport.getStateObject(context, "insets");
        if (insetsStateObj != null)
        {
            Integer topState = restorableSupport.getStateValueAsInteger(insetsStateObj, "top");
            Integer leftState = restorableSupport.getStateValueAsInteger(insetsStateObj, "left");
            Integer bottomState = restorableSupport.getStateValueAsInteger(insetsStateObj, "bottom");
            Integer rightState = restorableSupport.getStateValueAsInteger(insetsStateObj, "right");
            if (topState != null && leftState != null && bottomState != null && rightState != null)
                dest.setInsets(new Insets(topState, leftState, bottomState, rightState));
        }

        Double borderWidthState = restorableSupport.getStateValueAsDouble(context, "borderWidth");
        if (borderWidthState != null)
            dest.setBorderWidth(borderWidthState);

        Integer borderStippleFactorState = restorableSupport.getStateValueAsInteger(context, "borderStippleFactor");
        if (borderStippleFactorState != null)
            dest.setBorderStippleFactor(borderStippleFactorState);

        Integer borderStipplePatternState = restorableSupport.getStateValueAsInteger(context, "borderStipplePattern");
        if (borderStipplePatternState != null)
            dest.setBorderStipplePattern(borderStipplePatternState.shortValue());

        Integer antiAliasHintState = restorableSupport.getStateValueAsInteger(context, "antiAliasHint");
        if (antiAliasHintState != null)
            dest.setAntiAliasHint(antiAliasHintState);

        Boolean visibleState = restorableSupport.getStateValueAsBoolean(context, "visible");
        if (visibleState != null)
            dest.setVisible(visibleState);

        // Restore the font property only if all parts are available.
        // We will not restore a partial font (for example, just the size).
        RestorableSupport.StateObject fontStateObj = restorableSupport.getStateObject(context, "font");
        if (fontStateObj != null)
        {
            // The "font name" of toolTipFont.
            String nameState = restorableSupport.getStateValueAsString(fontStateObj, "name");
            // The style attributes.
            Integer styleState = restorableSupport.getStateValueAsInteger(fontStateObj, "style");
            // The simple font size.
            Integer sizeState = restorableSupport.getStateValueAsInteger(fontStateObj, "size");
            if (nameState != null && styleState != null && sizeState != null)
                dest.setFont(new Font(nameState, styleState, sizeState));
        }

        String textAlignState = restorableSupport.getStateValueAsString(context, "textAlign");
        if (textAlignState != null)
        {
            // Attempt to convert the textAlign string to an integer to handle legacy textAlign restorable state.
            // WWUtil.makeInteger returns null without logging a message if the string cannot be converted to an int.
            Integer textAlignInt = WWUtil.makeInteger(textAlignState);
            if (textAlignInt != null)
            {
                dest.setTextAlign(textAlignInt == 0 ? AVKey.LEFT : (textAlignInt == 1 ? AVKey.CENTER : AVKey.RIGHT));
            }
            else
            {
                dest.setTextAlign(textAlignState);
            }
        }

        String textColorState = restorableSupport.getStateValueAsString(context, "textColor");
        if (textColorState != null)
        {
            Color color = RestorableSupport.decodeColor(textColorState);
            if (color != null)
                dest.setTextColor(color);
        }

        String backgroundColorState = restorableSupport.getStateValueAsString(context, "backgroundColor");
        if (backgroundColorState != null)
        {
            Color color = RestorableSupport.decodeColor(backgroundColorState);
            if (color != null)
                dest.setBackgroundColor(color);
        }

        String borderColorState = restorableSupport.getStateValueAsString(context, "borderColor");
        if (borderColorState != null)
        {
            Color color = RestorableSupport.decodeColor(borderColorState);
            if (color != null)
                dest.setBorderColor(color);
        }

        // The imagePath property should exist only if the imageSource property was a simple String path.
        // If the imageSource property was a BufferedImage (or some other object), it should not exist in the
        // state document. We save under the name "imagePath" to denote that it is a special case of "imageSource".
        String imagePathState = restorableSupport.getStateValueAsString(context, "imagePath");
        if (imagePathState != null)
            dest.setImageSource(imagePathState);

        Double imageScaleState = restorableSupport.getStateValueAsDouble(context, "imageScale");
        if (imageScaleState != null)
            dest.setImageScale(imageScaleState);

        // Restore the imageOffset property only if all parts are available.
        // We will not restore a partial imageOffset (for example, just the x value).
        RestorableSupport.StateObject imageOffsetStateObj = restorableSupport.getStateObject(context, "imageOffset");
        if (imageOffsetStateObj != null)
        {
            Double xState = restorableSupport.getStateValueAsDouble(imageOffsetStateObj, "x");
            Double yState = restorableSupport.getStateValueAsDouble(imageOffsetStateObj, "y");
            if (xState != null && yState != null)
                dest.setImageOffset(new Point(xState.intValue(), yState.intValue()));
        }

        Double imageOpacityState = restorableSupport.getStateValueAsDouble(context, "imageOpacity");
        if (imageOpacityState != null)
            dest.setImageOpacity(imageOpacityState);

        String imageRepeatState = restorableSupport.getStateValueAsString(context, "imageRepeat");
        if (imageRepeatState != null)
        {
            // Map legacy versions using Annotation repeat constants to the new AVKey constants.
            String updatedValue = legacySupport.get(imageRepeatState);
            if (updatedValue != null)
                imageRepeatState = updatedValue;

            dest.setImageRepeat(imageRepeatState);
        }

        Double distanceMinScaleState = restorableSupport.getStateValueAsDouble(context, "distanceMinScale");
        if (distanceMinScaleState != null)
            dest.setDistanceMinScale(distanceMinScaleState);

        Double distanceMaxScaleState = restorableSupport.getStateValueAsDouble(context, "distanceMaxScale");
        if (distanceMaxScaleState != null)
            dest.setDistanceMaxScale(distanceMaxScaleState);

        Double distanceMinOpacityState = restorableSupport.getStateValueAsDouble(context, "distanceMinOpacity");
        if (distanceMinOpacityState != null)
            dest.setDistanceMinOpacity(distanceMinOpacityState);

        String effectState = restorableSupport.getStateValueAsString(context, "effect");
        if (effectState != null)
            dest.setEffect(effectState);
    }
}
