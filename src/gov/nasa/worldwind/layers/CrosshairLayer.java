/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.io.*;

/**
 * Renders a crosshair icon in the viewport center or at a specified location.
 *
 * @author Patrick Murris
 * @version $Id: CrosshairLayer.java 1953 2014-04-21 15:43:35Z tgaskins $
 */
public class CrosshairLayer extends AbstractLayer
{
    private String iconFilePath = "images/32x32-crosshair-simple.png"; // TODO: make configurable
    private double toViewportScale = 1d; // TODO: make configurable
    private double iconScale = 1d;
    private String resizeBehavior = AVKey.RESIZE_SHRINK_ONLY;
    private int iconWidth;
    private int iconHeight;
    private Vec4 locationCenter = null;

    // Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
    private OrderedIcon orderedImage = new OrderedIcon();

    private class OrderedIcon implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            // Not implemented
        }

        public void render(DrawContext dc)
        {
            CrosshairLayer.this.draw(dc);
        }
    }

    public CrosshairLayer()
    {
        this.setOpacity(0.8); // TODO: make configurable
    }

    public CrosshairLayer(String iconFilePath)
    {
        this.setIconFilePath(iconFilePath);
        this.setOpacity(0.8); // TODO: make configurable
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
     * Sets the crosshair icon's image location. The layer first searches for this location in the current Java
     * classpath. If not found then the specified path is assumed to refer to the local file system. found there then
     * the
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
     * @return the crosshair-to-viewport scale factor
     */
    public double getToViewportScale()
    {
        return toViewportScale;
    }

    /**
     * Sets the scale factor applied to the viewport size to determine the displayed size of the crosshair icon. This
     * scale factor is used only when the layer's resize behavior is AVKey.RESIZE_STRETCH or AVKey.RESIZE_SHRINK_ONLY.
     * The icon's width is adjusted to occupy the proportion of the viewport's width indicated by this factor. The
     * icon's height is adjusted to maintain the crosshair image's native aspect ratio.
     *
     * @param toViewportScale the compass to viewport scale factor
     */
    public void setToViewportScale(double toViewportScale)
    {
        this.toViewportScale = toViewportScale;
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
     * Sets the scale factor defining the displayed size of the crosshair icon relative to the icon's width and height
     * in its image file. Values greater than 1 magify the image, values less than one minify it. If the layer's resize
     * behavior is other than AVKey.RESIZE_KEEP_FIXED_SIZE, the icon's displayed sized is further affected by the value
     * specified by {@link #setToViewportScale(double)} and the current viewport size.
     *
     * @param iconScale the icon scale factor
     */
    public void setIconScale(double iconScale)
    {
        this.iconScale = iconScale;
    }

    /**
     * Returns the crosshair icon's resize behavior.
     *
     * @return the icon's resize behavior
     */
    public String getResizeBehavior()
    {
        return resizeBehavior;
    }

    /**
     * Sets the behavior the layer uses to size the crosshair icon when the viewport size changes, typically when the
     * World Wind window is resized. If the value is AVKey.RESIZE_KEEP_FIXED_SIZE, the icon size is kept to the size
     * specified in its image file scaled by the layer's current icon scale. If the value is AVKey.RESIZE_STRETCH, the
     * icon is resized to have a constant size relative to the current viewport size. If the viewport shrinks the icon
     * size decreases; if it expands then the icon file enlarges. The relative size is determined by the current
     * crosshair-to-viewport scale and by the icon's image file size scaled by the current icon scale. If the value is
     * AVKey.RESIZE_SHRINK_ONLY (the default), icon sizing behaves as for AVKey.RESIZE_STRETCH but the icon will not
     * grow larger than the size specified in its image file scaled by the current icon scale.
     *
     * @param resizeBehavior the desired resize behavior
     */
    public void setResizeBehavior(String resizeBehavior)
    {
        this.resizeBehavior = resizeBehavior;
    }

    /**
     * Get the crosshair location inside the viewport. If this location is null, the crosshair is drawn in the viewport
     * center.
     *
     * @return the crosshair location inside the viewport.
     */
    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    /**
     * Set the crosshair location inside the viewport. If this location is null, the crosshair will be drawn in the
     * viewport center.
     *
     * @param locationCenter the crosshair location inside the viewport.
     */
    public void setLocationCenter(Vec4 locationCenter)
    {
        this.locationCenter = locationCenter;
    }

    protected void doRender(DrawContext dc)
    {
        dc.addOrderedRenderable(this.orderedImage);
    }

    private void draw(DrawContext dc)
    {
        if (this.getIconFilePath() == null)
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

            gl.glEnable(GL.GL_TEXTURE_2D);
            iconTexture.bind(gl);

            gl.glColor4d(1d, 1d, 1d, this.getOpacity());
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            double width = this.getScaledIconWidth();
            double height = this.getScaledIconHeight();

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = width > height ? width : height;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            double scale = this.computeScale(viewport);
            Vec4 locationSW = this.computeLocation(viewport, scale);

            gl.glTranslated((int) locationSW.x, (int) locationSW.y, (int) locationSW.z);
            gl.glScaled(scale, scale, 1);

            TextureCoords texCoords = iconTexture.getImageTexCoords();
            gl.glScaled(width, height, 1d);
            dc.drawUnitQuad(texCoords);
        }
        finally
        {
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

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

    private double computeScale(Rectangle viewport)
    {
        if (this.resizeBehavior.equals(AVKey.RESIZE_SHRINK_ONLY))
        {
            return Math.min(1d, (this.toViewportScale) * viewport.width / this.getScaledIconWidth());
        }
        else if (this.resizeBehavior.equals(AVKey.RESIZE_STRETCH))
        {
            return (this.toViewportScale) * viewport.width / this.getScaledIconWidth();
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

    private double getScaledIconWidth()
    {
        return this.iconWidth * this.iconScale;
    }

    private double getScaledIconHeight()
    {
        return this.iconHeight * this.iconScale;
    }

    private Vec4 computeLocation(Rectangle viewport, double scale)
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
        else // viewport center
        {
            x = viewport.getWidth() / 2 - scaledWidth / 2;
            y = viewport.getHeight() / 2 - scaledHeight / 2;
        }

        return new Vec4(x, y, 0);
    }

    private void initializeTexture(DrawContext dc)
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
                File iconFile = new File(this.getIconFilePath());
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
        // Enable texture anisotropy
        int[] maxAnisotropy = new int[1];
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.CrosshairLayer.Name");
    }
}
