/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.media.opengl.*;
import java.awt.*;
import java.io.*;

/**
 * @author tag
 * @version $Id: CompassLayer.java 2121 2014-07-03 03:10:54Z tgaskins $
 */
public class CompassLayer extends AbstractLayer
{
    protected String iconFilePath = "images/notched-compass.dds"; // TODO: make configurable
    protected double compassToViewportScale = 0.2; // TODO: make configurable
    protected double iconScale = 0.5;
    protected int borderWidth = 20; // TODO: make configurable
    protected String position = AVKey.NORTHEAST; // TODO: make configurable
    protected String resizeBehavior = AVKey.RESIZE_SHRINK_ONLY;
    protected int iconWidth;
    protected int iconHeight;
    protected Vec4 locationCenter = null;
    protected Vec4 locationOffset = null;
    protected boolean showTilt = true;
    protected PickSupport pickSupport = new PickSupport();
    protected long frameStampForPicking;
    protected long frameStampForDrawing;

    // Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
    protected OrderedIcon orderedImage = new OrderedIcon();

    protected class OrderedIcon implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            CompassLayer.this.draw(dc);
        }

        public void render(DrawContext dc)
        {
            CompassLayer.this.draw(dc);
        }
    }

    public CompassLayer()
    {
        this.setOpacity(0.8); // TODO: make configurable
        this.setPickEnabled(false);  // Default to no picking
    }

    public CompassLayer(String iconFilePath)
    {
        this.setIconFilePath(iconFilePath);
        this.setOpacity(0.8); // TODO: make configurable
        this.setPickEnabled(false); // Default to no picking
    }

    /**
     * Returns the layer's current icon file path.
     *
     * @return the icon file path
     */
    public String getIconFilePath()
    {
        return iconFilePath;
    }

    /**
     * Sets the compass icon's image location. The layer first searches for this location in the current Java classpath.
     * If not found then the specified path is assumed to refer to the local file system. found there then the
     *
     * @param iconFilePath the path to the icon's image file
     */
    public void setIconFilePath(String iconFilePath)
    {
        if (iconFilePath == null)
        {
            String message = Logging.getMessage("nullValue.IconFilePath");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.iconFilePath = iconFilePath;
    }

    /**
     * Returns the layer's compass-to-viewport scale factor.
     *
     * @return the compass-to-viewport scale factor
     */
    public double getCompassToViewportScale()
    {
        return compassToViewportScale;
    }

    /**
     * Sets the scale factor applied to the viewport size to determine the displayed size of the compass icon. This
     * scale factor is used only when the layer's resize behavior is AVKey.RESIZE_STRETCH or AVKey.RESIZE_SHRINK_ONLY.
     * The icon's width is adjusted to occupy the proportion of the viewport's width indicated by this factor. The
     * icon's height is adjusted to maintain the compass image's native aspect ratio.
     *
     * @param compassToViewportScale the compass to viewport scale factor
     */
    public void setCompassToViewportScale(double compassToViewportScale)
    {
        this.compassToViewportScale = compassToViewportScale;
    }

    /**
     * Returns the icon scale factor. See {@link #setIconScale(double)} for a description of the scale factor.
     *
     * @return the current icon scale
     */
    public double getIconScale()
    {
        return iconScale;
    }

    /**
     * Sets the scale factor defining the displayed size of the compass icon relative to the icon's width and height in
     * its image file. Values greater than 1 magify the image, values less than one minify it. If the layer's resize
     * behavior is other than AVKey.RESIZE_KEEP_FIXED_SIZE, the icon's displayed sized is further affected by the value
     * specified by {@link #setCompassToViewportScale(double)} and the current viewport size.
     * <p/>
     * The default icon scale is 0.5.
     *
     * @param iconScale the icon scale factor
     */
    public void setIconScale(double iconScale)
    {
        this.iconScale = iconScale;
    }

    /**
     * Returns the compass icon's resize behavior.
     *
     * @return the icon's resize behavior
     */
    public String getResizeBehavior()
    {
        return resizeBehavior;
    }

    /**
     * Sets the behavior the layer uses to size the compass icon when the viewport size changes, typically when the
     * World Wind window is resized. If the value is AVKey.RESIZE_KEEP_FIXED_SIZE, the icon size is kept to the size
     * specified in its image file scaled by the layer's current icon scale. If the value is AVKey.RESIZE_STRETCH, the
     * icon is resized to have a constant size relative to the current viewport size. If the viewport shrinks the icon
     * size decreases; if it expands then the icon file enlarges. The relative size is determined by the current
     * compass-to-viewport scale and by the icon's image file size scaled by the current icon scale. If the value is
     * AVKey.RESIZE_SHRINK_ONLY (the default), icon sizing behaves as for AVKey.RESIZE_STRETCH but the icon will not
     * grow larger than the size specified in its image file scaled by the current icon scale.
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
     * Sets the compass icon offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the compass icon from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    /**
     * Returns the current relative compass icon position.
     *
     * @return the current compass position
     */
    public String getPosition()
    {
        return position;
    }

    /**
     * Sets the relative viewport location to display the compass icon. Can be one of AVKey.NORTHEAST (the default),
     * AVKey.NORTHWEST, AVKey.SOUTHEAST, or AVKey.SOUTHWEST. These indicate the corner of the viewport to place the
     * icon.
     *
     * @param position the desired compass position
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.CompassPositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.position = position;
    }

    /**
     * Returns the current compass image location.
     *
     * @return the current location center. May be null.
     */
    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    /**
     * Specifies the screen location of the compass image, relative to the image's center. May be null. If this value is
     * non-null, it overrides the position specified by {@link #setPosition(String)}. The location is specified in
     * pixels. The origin is the window's lower left corner. Positive X values are to the right of the origin, positive
     * Y values are upwards from the origin. The final image location will be affected by the currently specified
     * location offset if a non-null location offset has been specified (see {@link
     * #setLocationOffset(gov.nasa.worldwind.geom.Vec4)}).
     *
     * @param locationCenter the location center. May be null.
     *
     * @see #setPosition(String)
     * @see #setLocationOffset(gov.nasa.worldwind.geom.Vec4)
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
     * Specifies a placement offset from the compass' position on the screen.
     *
     * @param locationOffset the number of pixels to shift the compass image from its specified screen position. A
     *                       positive X value shifts the image to the right. A positive Y value shifts the image up. If
     *                       null, no offset is applied. The default offset is null.
     *
     * @see #setLocationCenter(gov.nasa.worldwind.geom.Vec4)
     * @see #setPosition(String)
     */
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset;
    }

    protected void doRender(DrawContext dc)
    {
        if (dc.isContinuous2DGlobe() && this.frameStampForDrawing == dc.getFrameTimeStamp())
            return;

        dc.addOrderedRenderable(this.orderedImage);

        this.frameStampForDrawing = dc.getFrameTimeStamp();
    }

    protected void doPick(DrawContext dc, Point pickPoint)
    {
        if (dc.isContinuous2DGlobe() && this.frameStampForPicking == dc.getFrameTimeStamp())
            return;

        dc.addOrderedRenderable(this.orderedImage);

        this.frameStampForPicking = dc.getFrameTimeStamp();
    }

    public boolean isShowTilt()
    {
        return showTilt;
    }

    public void setShowTilt(boolean showTilt)
    {
        this.showTilt = showTilt;
    }

    protected void draw(DrawContext dc)
    {
        if (this.getIconFilePath() == null)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();

        try
        {
            gl.glDisable(GL.GL_DEPTH_TEST);

            Texture iconTexture = dc.getTextureCache().getTexture(this.getIconFilePath());
            if (iconTexture == null)
            {
                this.initializeTexture(dc);
                iconTexture = dc.getTextureCache().getTexture(this.getIconFilePath());
                if (iconTexture == null)
                {
                    String msg = Logging.getMessage("generic.ImageReadFailed");
                    Logging.logger().finer(msg);
                    return;
                }
            }

            // Need to assign the width and height here to address the case in which the texture was already
            // loaded into the cache by another layer or a previous instance of this one.
            this.iconWidth = iconTexture.getWidth();
            this.iconHeight = iconTexture.getHeight();

            double width = this.getScaledIconWidth();
            double height = this.getScaledIconHeight();

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            ogsh.pushProjectionIdentity(gl);
            double maxwh = width > height ? width : height;
            if (maxwh == 0)
                maxwh = 1;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            ogsh.pushModelviewIdentity(gl);
            double scale = this.computeScale(viewport);
            Vec4 locationSW = this.computeLocation(viewport, scale);
            double heading = this.computeHeading(dc.getView());
            double pitch = this.computePitch(dc.getView());

            gl.glTranslated(locationSW.x, locationSW.y, locationSW.z);
            gl.glScaled(scale, scale, 1);

            if (!dc.isPickingMode())
            {
                gl.glTranslated(width / 2, height / 2, 0);
                if (this.showTilt) // formula contributed by Ty Hayden
                    gl.glRotated(70d * (pitch / 90.0), 1d, 0d, 0d);
                gl.glRotated(heading, 0d, 0d, 1d);
                gl.glTranslated(-width / 2, -height / 2, 0);

                gl.glEnable(GL.GL_TEXTURE_2D);
                iconTexture.bind(gl);

                gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                TextureCoords texCoords = iconTexture.getImageTexCoords();
                gl.glScaled(width, height, 1d);
                dc.drawUnitQuad(texCoords);
            }
            else
            {
                // Picking
                this.pickSupport.clearPickList();
                this.pickSupport.beginPicking(dc);
                try
                {
                    // Add a picked object for the compass to the list of pickable objects.
                    Color color = dc.getUniquePickColor();
                    PickedObject po = new PickedObject(color.getRGB(), this, null, false);
                    this.pickSupport.addPickableObject(po);

                    if (dc.getPickPoint() != null)
                    {
                        // If the pick point is not null, compute the pick point 'heading' relative to the compass
                        // center and set the picked heading on our picked object. The pick point is null if a pick
                        // rectangle is specified but a pick point is not.
                        Vec4 center = new Vec4(locationSW.x + width * scale / 2, locationSW.y + height * scale / 2,
                            0);
                        double px = dc.getPickPoint().x - center.x;
                        double py = viewport.getHeight() - dc.getPickPoint().y - center.y;
                        Angle pickHeading = Angle.fromRadians(Math.atan2(px, py));
                        pickHeading = pickHeading.degrees >= 0 ? pickHeading : pickHeading.addDegrees(360);
                        po.setValue("Heading", pickHeading);
                    }

                    // Draw the compass in the unique pick color.
                    gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                    gl.glScaled(width, height, 1d);
                    dc.drawUnitQuad();
                }
                finally
                {
                    // Done picking
                    this.pickSupport.endPicking(dc);
                    this.pickSupport.resolvePick(dc, dc.getPickPoint(), this);
                }
            }
        }
        finally
        {
            dc.restoreDefaultDepthTesting();
            dc.restoreDefaultCurrentColor();

            if (!dc.isPickingMode())
            {
                gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
                gl.glDisable(GL.GL_TEXTURE_2D); // restore to default texture state
                dc.restoreDefaultBlending();
            }

            ogsh.pop(gl);
        }
    }

    protected double computeScale(java.awt.Rectangle viewport)
    {
        if (this.resizeBehavior.equals(AVKey.RESIZE_SHRINK_ONLY))
        {
            return Math.min(1d, (this.compassToViewportScale) * viewport.width / this.getScaledIconWidth());
        }
        else if (this.resizeBehavior.equals(AVKey.RESIZE_STRETCH))
        {
            return (this.compassToViewportScale) * viewport.width / this.getScaledIconWidth();
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

    protected double getScaledIconWidth()
    {
        return this.iconWidth * this.iconScale;
    }

    protected double getScaledIconHeight()
    {
        return this.iconHeight * this.iconScale;
    }

    protected Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
    {
        double width = this.getScaledIconWidth();
        double height = this.getScaledIconHeight();

        double scaledWidth = scale * width;
        double scaledHeight = scale * height;

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
        else // use North East as default
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }

        if (this.locationOffset != null)
        {
            x += this.locationOffset.x;
            y += this.locationOffset.y;
        }

        return new Vec4(x, y, 0);
    }

    protected double computeHeading(View view)
    {
        if (view == null)
            return 0.0;

        return view.getHeading().getDegrees();
    }

    protected double computePitch(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;
        return orbitView.getPitch().getDegrees();
    }

    protected void initializeTexture(DrawContext dc)
    {
        Texture iconTexture = dc.getTextureCache().getTexture(this.getIconFilePath());
        if (iconTexture != null)
            return;

        GL gl = dc.getGL();

        try
        {
            InputStream iconStream = this.getClass().getResourceAsStream("/" + this.getIconFilePath());
            if (iconStream == null)
            {
                File iconFile = new File(this.iconFilePath);
                if (iconFile.exists())
                {
                    iconStream = new FileInputStream(iconFile);
                }
            }

            TextureData textureData = OGLUtil.newTextureData(gl.getGLProfile(), iconStream, false);
            iconTexture = TextureIO.newTexture(textureData);
            iconTexture.bind(gl);
            this.iconWidth = iconTexture.getWidth();
            this.iconHeight = iconTexture.getHeight();
            dc.getTextureCache().put(this.getIconFilePath(), iconTexture);
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("layers.IOExceptionDuringInitialization");
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg, e);
        }

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);//_MIPMAP_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        // Enable texture anisotropy, improves "tilted" compass quality.
        int[] maxAnisotropy = new int[1];
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.CompassLayer.Name");
    }
}
