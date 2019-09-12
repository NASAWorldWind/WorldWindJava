/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;

/**
 * The PowerOfTwoPaddedImage class converts images with non-power-of-two dimensions to images with power-of-two
 * dimensions. The original image is copied into a power-of-two image, where any pixels not covered by the original
 * image are completely transparent. This is a useful property when converting images to OpenGL textures.
 * Non-power-of-two textures are handled inconsistently by graphics hardware. Not all hardware supports them, and many
 * that do lack full support for the the texturing functionality avialable for power-of-two textures.
 * <p>
 * PowerOfTwoPaddedImage provides accessors to the converted power-of-two image, the power-of-two image's width and
 * height, and the original image's width and height.
 *
 * @author dcollins
 * @version $Id: PowerOfTwoPaddedImage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PowerOfTwoPaddedImage
{
    protected BufferedImage image;
    protected int width;
    protected int height;

    protected PowerOfTwoPaddedImage(BufferedImage image, int width, int height)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.image = image;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns a new PowerOfTwoPaddedImage by converting the specified <code>image</code> to a new image with
     * power-of-two dimensions. Any pixels not covered by the original image are completely transparent. If the
     * specified <code>image</code> has power-of-two dimensions, this maintains a reference to the original image
     * instead of creating a copy. However, if the specified <code>image</code> has no alpha channel, this creates a
     * copy of the original image with an alpha channel, regardless of the specified <code>image's</code> dimensions.
     * This guarantees that the method {@link #getPowerOfTwoImage()} always returns a BufferedImage with an alpha
     * channel. This is a useful property when converting images to OpenGL texture's, when both power-of-two and
     * non-power-of-two textures must have an alpha channel for consistent handling.
     *
     * @param image the BufferedImage to convert to an image with power-of-two dimensions.
     *
     * @return a new PowerOfTwoPaddedImage representing a power-of-two copy of the specified <code>image</code>.
     *
     * @throws IllegalArgumentException if the image is null.
     */
    public static PowerOfTwoPaddedImage fromBufferedImage(BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BufferedImage potImage = image;

        // Create a new image with power of two dimensions and an alpha channel. If the original image has non power
        // of two dimensions, or if it does not have alpha channel, it won't display correctly as an Annotation
        // background image.
        if (!WWMath.isPowerOfTwo(image.getWidth()) || !WWMath.isPowerOfTwo(image.getHeight())
            || image.getTransparency() == Transparency.OPAQUE)
        {
            int potWidth = WWMath.powerOfTwoCeiling(image.getWidth());
            int potHeight = WWMath.powerOfTwoCeiling(image.getHeight());

            potImage = ImageUtil.createCompatibleImage(potWidth, potHeight, BufferedImage.TRANSLUCENT);
            Graphics2D g2d = potImage.createGraphics();
            try
            {
                g2d.drawImage(image, 0, 0, null);
            }
            finally
            {
                g2d.dispose();
            }
        }

        return new PowerOfTwoPaddedImage(potImage, image.getWidth(), image.getHeight());
    }

    /**
     * Returns a new PowerOfTwoPaddedImage from the specified <code>path</code>, or null if the file referenced by
     * <code>path</code> cannot be read, or is not a readable image. The <code>path</code> must be a local file path, or
     * a valid resource on the classpath. This uses {@link javax.imageio.ImageIO} to read the specified
     * <code>path</code> as a {@link java.awt.image.BufferedImage}. Otherwise, this treats the resultant BufferedImage
     * exactly as {@link #fromBufferedImage(java.awt.image.BufferedImage)}.
     *
     * @param path a local file path, or a valid resource on the classpath.
     *
     * @return a new PowerOfTwoPaddedImage representing a power-of-two copy of the image located at the specified
     *         <code>path</code>, or null if the image file reference by <code>path</code> cannot be read.
     *
     * @throws IllegalArgumentException if the path is null.
     */
    public static PowerOfTwoPaddedImage fromPath(String path)
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object streamOrException = WWIO.getFileOrResourceAsStream(path, null);
        if (streamOrException == null || streamOrException instanceof Exception)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionAttemptingToReadImageFile",
                streamOrException != null ? streamOrException : path);
            return null;
        }

        try
        {
            BufferedImage image = ImageIO.read((InputStream) streamOrException);
            return fromBufferedImage(image);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadImageFile", path);
            Logging.logger().severe(message);
            return null;
        }
    }

    /**
     * Returns the width of the original non-power-of-two image, in pixels.
     *
     * @return the original image's width, in pixels.
     */
    public int getOriginalWidth()
    {
        return this.width;
    }

    /**
     * Returns the height of the original non-power-of-two image, in pixels.
     *
     * @return the original image's height, in pixels.
     */
    public int getOriginalHeight()
    {
        return this.height;
    }

    /**
     * Returns a copy of the original image as a {@link java.awt.image.BufferedImage} with power-of-two dimensions. Any
     * pixels not covered by the original image are completely transparent.
     *
     * @return a copy of the original image as a BufferedImage.
     */
    public BufferedImage getPowerOfTwoImage()
    {
        return this.image;
    }

    /**
     * Returns the width of the power-of-two image, in pixels.
     *
     * @return the power-of-two image's width, in pixels.
     */
    public int getPowerOfTwoWidth()
    {
        return this.image.getWidth();
    }

    /**
     * Returns the height of the power-of-two image, in pixels.
     *
     * @return the power-of-two image's height, in pixels.
     */
    public int getPowerOfTwoHeight()
    {
        return this.image.getHeight();
    }
}