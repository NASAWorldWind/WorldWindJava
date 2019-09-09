/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Renders a scalebar graphic in a screen corner.
 *
 * @author Patrick Murris
 * @version $Id: ScalebarLayer.java 2126 2014-07-04 00:35:06Z tgaskins $
 */
public class ScalebarLayer extends AbstractLayer
{
    // Units constants
    public final static String UNIT_METRIC = "gov.nasa.worldwind.ScalebarLayer.Metric";
    public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.ScalebarLayer.Imperial";
    public final static String UNIT_NAUTICAL = "gov.nasa.worldwind.ScalebarLayer.Nautical";

    // Display parameters
    protected Dimension size = new Dimension(150, 10);
    protected Color color = Color.white;
    protected int borderWidth = 20;
    protected String position = AVKey.SOUTHEAST;
    protected String resizeBehavior = AVKey.RESIZE_SHRINK_ONLY;
    protected String unit = UNIT_METRIC;
    protected Font defaultFont = Font.decode("Arial-PLAIN-12");
    protected double toViewportScale = 0.2;

    protected PickSupport pickSupport = new PickSupport();
    protected Vec4 locationCenter = null;
    protected Vec4 locationOffset = null;
    protected long frameStampForPicking;
    protected long frameStampForDrawing;

    protected class OrderedImage implements OrderedRenderable
    {
        protected Position referencePosition;
        protected double pixelSize;

        public OrderedImage(Position referencePosition, double pixelSize)
        {
            this.referencePosition = referencePosition;
            this.pixelSize = pixelSize;
        }

        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            ScalebarLayer.this.draw(dc, this);
        }

        public void render(DrawContext dc)
        {
            ScalebarLayer.this.draw(dc, this);
        }
    }

    /** Renders a scalebar graphic in a screen corner */
    public ScalebarLayer()
    {
        setPickEnabled(false);
    }

    // Public properties

    /**
     * Get the scalebar graphic Dimension (in pixels)
     *
     * @return the scalebar graphic Dimension
     */
    public Dimension getSize()
    {
        return this.size;
    }

    /**
     * Set the scalebar graphic Dimenion (in pixels)
     *
     * @param size the scalebar graphic Dimension
     */
    public void setSize(Dimension size)
    {
        if (size == null)
        {
            String message = Logging.getMessage("nullValue.DimensionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.size = size;
    }

    /**
     * Get the scalebar color
     *
     * @return the scalebar Color
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the scalbar Color
     *
     * @param color the scalebar Color
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

    /**
     * Returns the scalebar-to-viewport scale factor.
     *
     * @return the scalebar-to-viewport scale factor
     */
    public double getToViewportScale()
    {
        return toViewportScale;
    }

    /**
     * Sets the scale factor applied to the viewport size to determine the displayed size of the scalebar. This scale
     * factor is used only when the layer's resize behavior is AVKey.RESIZE_STRETCH or AVKey.RESIZE_SHRINK_ONLY. The
     * scalebar's width is adjusted to occupy the proportion of the viewport's width indicated by this factor. The
     * scalebar's height is adjusted to maintain the scalebar's Dimension aspect ratio.
     *
     * @param toViewportScale the scalebar to viewport scale factor
     */
    public void setToViewportScale(double toViewportScale)
    {
        this.toViewportScale = toViewportScale;
    }

    public String getPosition()
    {
        return this.position;
    }

    /**
     * Sets the relative viewport location to display the scalebar. Can be one of AVKey.NORTHEAST, AVKey.NORTHWEST,
     * AVKey.SOUTHEAST (the default), or AVKey.SOUTHWEST. These indicate the corner of the viewport.
     *
     * @param position the desired scalebar position
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.position = position;
    }

    /**
     * Returns the current scalebar center location.
     *
     * @return the current location center. May be null.
     */
    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    /**
     * Specifies the screen location of the scalebar center. May be null. If this value is non-null, it overrides the
     * position specified by #setPosition. The location is specified in pixels. The origin is the window's lower left
     * corner. Positive X values are to the right of the origin, positive Y values are upwards from the origin. The
     * final scalebar location will be affected by the currently specified location offset if a non-null location offset
     * has been specified (see #setLocationOffset).
     *
     * @param locationCenter the scalebar center. May be null.
     *
     * @see #setPosition
     * @see #setLocationOffset
     */
    public void setLocationCenter(Vec4 locationCenter)
    {
        this.locationCenter = locationCenter;
    }

    /**
     * Returns the current location offset. See #setLocationOffset for a description of the offset and its values.
     *
     * @return the location offset. Will be null if no offset has been specified.
     */
    public Vec4 getLocationOffset()
    {
        return locationOffset;
    }

    /**
     * Specifies a placement offset from the scalebar's position on the screen.
     *
     * @param locationOffset the number of pixels to shift the scalebar from its specified screen position. A positive X
     *                       value shifts the image to the right. A positive Y value shifts the image up. If null, no
     *                       offset is applied. The default offset is null.
     *
     * @see #setLocationCenter
     * @see #setPosition
     */
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset;
    }

    /**
     * Returns the layer's resize behavior.
     *
     * @return the layer's resize behavior
     */
    public String getResizeBehavior()
    {
        return resizeBehavior;
    }

    /**
     * Sets the behavior the layer uses to size the scalebar when the viewport size changes, typically when the World
     * Wind window is resized. If the value is AVKey.RESIZE_KEEP_FIXED_SIZE, the scalebar size is kept to the size
     * specified in its Dimension scaled by the layer's current icon scale. If the value is AVKey.RESIZE_STRETCH, the
     * scalebar is resized to have a constant size relative to the current viewport size. If the viewport shrinks the
     * scalebar size decreases; if it expands then the scalebar enlarges. If the value is AVKey.RESIZE_SHRINK_ONLY (the
     * default), scalebar sizing behaves as for AVKey.RESIZE_STRETCH but it will not grow larger than the size specified
     * in its Dimension.
     *
     * @param resizeBehavior the desired resize behavior
     */
    public void setResizeBehavior(String resizeBehavior)
    {
        this.resizeBehavior = resizeBehavior;
    }

    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * Sets the scalebar offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the scalebar from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    public String getUnit()
    {
        return this.unit;
    }

    /**
     * Sets the unit the scalebar uses to display distances. Can be one of {@link #UNIT_METRIC} (the default), or {@link
     * #UNIT_IMPERIAL}.
     *
     * @param unit the desired unit
     */
    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    /**
     * Get the scalebar legend Fon
     *
     * @return the scalebar legend Font
     */
    public Font getFont()
    {
        return this.defaultFont;
    }

    /**
     * Set the scalebar legend Fon
     *
     * @param font the scalebar legend Font
     */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String msg = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.defaultFont = font;
    }

    // Rendering
    @Override
    public void doRender(DrawContext dc)
    {
        if (dc.isContinuous2DGlobe() && this.frameStampForDrawing == dc.getFrameTimeStamp())
            return;

        this.addOrderedImage(dc);

        this.frameStampForDrawing = dc.getFrameTimeStamp();
    }

    @Override
    public void doPick(DrawContext dc, Point pickPoint)
    {
        if (dc.isContinuous2DGlobe() && this.frameStampForPicking == dc.getFrameTimeStamp())
            return;

        this.addOrderedImage(dc);

        this.frameStampForPicking = dc.getFrameTimeStamp();
    }

    protected void addOrderedImage(DrawContext dc)
    {
        // Capture the current reference position and pixel size and create an ordered renderable to defer drawing.

        Position referencePosition = dc.getViewportCenterPosition();
        dc.addOrderedRenderable(new OrderedImage(referencePosition, this.computePixelSize(dc, referencePosition)));
    }

    protected double computePixelSize(DrawContext dc, Position referencePosition)
    {
        if (referencePosition == null)
            return -1;

        Vec4 groundTarget = dc.getGlobe().computePointFromPosition(referencePosition);
        double eyeDistance = dc.getView().getEyePoint().distanceTo3(groundTarget);
        return dc.getView().computePixelSizeAtDistance(eyeDistance);
    }

    // Rendering
    public void draw(DrawContext dc, OrderedImage orderedImage)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        OGLStackHandler ogsh = new OGLStackHandler();

        try
        {
            ogsh.pushAttrib(gl, GL2.GL_TRANSFORM_BIT);

            gl.glDisable(GL.GL_DEPTH_TEST);

            double width = this.size.width;
            double height = this.size.height;

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            ogsh.pushProjectionIdentity(gl);
            double maxwh = width > height ? width : height;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            ogsh.pushModelviewIdentity(gl);

            // Scale to a width x height space
            // located at the proper position on screen
            double scale = this.computeScale(viewport);
            Vec4 locationSW = this.computeLocation(viewport, scale);
            gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
            gl.glScaled(scale, scale, 1);

            // Compute scale size in real world
            if (orderedImage.pixelSize > 0)
            {
                Double scaleSize = orderedImage.pixelSize * width * scale;  // meter
                String unitLabel = "m";
                if (this.unit.equals(UNIT_METRIC))
                {
                    if (scaleSize > 10000)
                    {
                        scaleSize /= 1000;
                        unitLabel = "Km";
                    }
                }
                else if (this.unit.equals(UNIT_IMPERIAL))
                {
                    scaleSize *= 3.280839895; // feet
                    unitLabel = "ft";
                    if (scaleSize > 5280)
                    {
                        scaleSize /= 5280;
                        unitLabel = "mile(s)";
                    }
                }
                else if (this.unit.equals(UNIT_NAUTICAL))
                {
                    scaleSize *= 3.280839895; // feet
                    unitLabel = "ft";
                    if (scaleSize > 6076)
                    {
                        scaleSize /= 6076;
                        unitLabel = "Nautical mile(s)";
                    }
                }
                // Rounded division size
                int pot = (int) Math.floor(Math.log10(scaleSize));
                if (!Double.isNaN(pot))
                {
                    int digit = Integer.parseInt(String.format("%.0f", scaleSize).substring(0, 1));
                    double divSize = digit * Math.pow(10, pot);
                    if (digit >= 5)
                        divSize = 5 * Math.pow(10, pot);
                    else if (digit >= 2)
                        divSize = 2 * Math.pow(10, pot);
                    double divWidth = width * divSize / scaleSize;

                    // Draw scale
                    if (!dc.isPickingMode())
                    {
                        gl.glEnable(GL.GL_BLEND);
                        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

                        // Set color using current layer opacity
                        Color backColor = this.getBackgroundColor(this.color);
                        float[] colorRGB = backColor.getRGBColorComponents(null);
                        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], (double) backColor.getAlpha() / 255d
                            * this.getOpacity());
                        gl.glTranslated((width - divWidth) / 2, 0d, 0d);
                        this.drawScale(dc, divWidth, height);

                        colorRGB = this.color.getRGBColorComponents(null);
                        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity());
                        gl.glTranslated(-1d / scale, 1d / scale, 0d);
                        this.drawScale(dc, divWidth, height);

                        // Draw label
                        String label = String.format("%.0f ", divSize) + unitLabel;
                        gl.glLoadIdentity();
                        gl.glDisable(GL.GL_CULL_FACE);
                        drawLabel(dc, label,
                            locationSW.add3(
                                new Vec4(divWidth * scale / 2 + (width - divWidth) / 2, height * scale, 0)));
                    }
                    else
                    {
                        // Picking
                        this.pickSupport.clearPickList();
                        this.pickSupport.beginPicking(dc);
                        // Draw unique color across the map
                        Color color = dc.getUniquePickColor();
                        int colorCode = color.getRGB();
                        // Add our object(s) to the pickable list
                        this.pickSupport.addPickableObject(colorCode, this, orderedImage.referencePosition, false);
                        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                        gl.glTranslated((width - divWidth) / 2, 0d, 0d);
                        this.drawRectangle(dc, divWidth, height);
                        // Done picking
                        this.pickSupport.endPicking(dc);
                        this.pickSupport.resolvePick(dc, dc.getPickPoint(), this);
                    }
                }
            }
        }
        finally
        {
            gl.glColor4d(1d, 1d, 1d, 1d); // restore the default OpenGL color
            gl.glEnable(GL.GL_DEPTH_TEST);

            if (!dc.isPickingMode())
            {
                gl.glBlendFunc(GL.GL_ONE, GL.GL_ZERO); // restore to default blend function
                gl.glDisable(GL.GL_BLEND); // restore to default blend state
            }

            ogsh.pop(gl);
        }
    }

    // Draw scale rectangle
    private void drawRectangle(DrawContext dc, double width, double height)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glBegin(GL2.GL_POLYGON);
        gl.glVertex3d(0, height, 0);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(width, 0, 0);
        gl.glVertex3d(width, height, 0);
        gl.glVertex3d(0, height, 0);
        gl.glEnd();
    }

    // Draw scale graphic
    private void drawScale(DrawContext dc, double width, double height)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex3d(0, height, 0);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(width, 0, 0);
        gl.glVertex3d(width, height, 0);
        gl.glEnd();
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glVertex3d(width / 2, 0, 0);
        gl.glVertex3d(width / 2, height / 2, 0);
        gl.glEnd();
    }

    // Draw the scale label
    private void drawLabel(DrawContext dc, String text, Vec4 screenPoint)
    {
        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            this.defaultFont);

        Rectangle2D nameBound = textRenderer.getBounds(text);
        int x = (int) (screenPoint.x() - nameBound.getWidth() / 2d);
        int y = (int) screenPoint.y();

        textRenderer.begin3DRendering();

        textRenderer.setColor(this.getBackgroundColor(this.color));
        textRenderer.draw(text, x + 1, y - 1);
        textRenderer.setColor(this.color);
        textRenderer.draw(text, x, y);

        textRenderer.end3DRendering();
    }

    private final float[] compArray = new float[4];

    // Compute background color for best contrast
    private Color getBackgroundColor(Color color)
    {
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        if (compArray[2] > 0.5)
            return new Color(0, 0, 0, 0.7f);
        else
            return new Color(1, 1, 1, 0.7f);
    }

    private double computeScale(java.awt.Rectangle viewport)
    {
        if (this.resizeBehavior.equals(AVKey.RESIZE_SHRINK_ONLY))
        {
            return Math.min(1d, (this.toViewportScale) * viewport.width / this.size.width);
        }
        else if (this.resizeBehavior.equals(AVKey.RESIZE_STRETCH))
        {
            return (this.toViewportScale) * viewport.width / this.size.width;
        }
        else if (this.resizeBehavior.equals(AVKey.RESIZE_KEEP_FIXED_SIZE))
        {
            return 1d;
        }
        else
        {
            return 1d;
        }
    }

    private Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
    {
        double scaledWidth = scale * this.size.width;
        double scaledHeight = scale * this.size.height;

        double x;
        double y;

        if (this.locationCenter != null)
        {
            x = this.locationCenter.x - scaledWidth / 2;
            y = this.locationCenter.y - scaledHeight / 2;
        }
        else if (this.position.equals(AVKey.NORTHEAST))
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHEAST))
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else if (this.position.equals(AVKey.NORTHWEST))
        {
            x = 0d + this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHWEST))
        {
            x = 0d + this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else // use North East
        {
            x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
            y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
        }

        if (this.locationOffset != null)
        {
            x += this.locationOffset.x;
            y += this.locationOffset.y;
        }

        return new Vec4(x, y, 0);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.ScalebarLayer.Name");
    }
}
