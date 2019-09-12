/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.awt.image.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * Static class to creates tilable patterns.
 * <p>
 * The <code>createPattern()</code> method draws a shape inside a usually square bitmap, so that it will match if tiled.
 * </p>
 * <p>
 * Each pattern supports a <code>scale</code> factor between <code>zero</code> and <code>one</code> - default is .5.
 * With a scale of <code>zero</code> no pattern will be produced. With a scale of <code>one</code> the pattern will
 * cover all the background.
 * </p>
 * @author Patrick Murris
 * @version $Id: PatternFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PatternFactory {

    public final static String PATTERN_CIRCLE = "PatternFactory.PatternCircle";
    public final static String PATTERN_CIRCLES = "PatternFactory.PatternCircles";
    public final static String PATTERN_SQUARE = "PatternFactory.PatternSquare";
    public final static String PATTERN_SQUARES = "PatternFactory.PatternSquares";
    public final static String PATTERN_TRIANGLE_UP = "PatternFactory.PatternTriangleUp";
    public final static String PATTERN_HLINE = "PatternFactory.PatternHLine";
    public final static String PATTERN_VLINE = "PatternFactory.PatternVLine";
    public final static String PATTERN_HVLINE = "PatternFactory.PatternHVLine";
    public final static String PATTERN_DIAGONAL_UP = "PatternFactory.PatternDiagonalUp";
    public final static String PATTERN_DIAGONAL_DOWN = "PatternFactory.PatternDiagonalDown";

    public final static String GRADIENT_HLINEAR = "PatternFactory.GradientHLinear";
    public final static String GRADIENT_VLINEAR = "PatternFactory.GradientVLinear";

    private static Dimension defaultDimension = new Dimension(32, 32);
    private static float defaultScale = .5f;
    private static Color defaultLineColor = Color.LIGHT_GRAY;
    private static Color defaultBackColor = new Color(0f, 0f, 0f, 0f);

    /**
     * Draws a pattern using the default scale (.5), bitmap dimensions (32x32) and colors (light grey over
     * a transparent background).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern)
    {
        return createPattern(pattern, defaultDimension, defaultScale, defaultLineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>Color</code> using the default scale (.5), bitmap dimensions (32x32)
     * and backgound color (transparent).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param lineColor the pattern <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, Color lineColor)
    {
        return createPattern(pattern, defaultDimension, defaultScale, lineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code> using the default bitmap dimensions (32x32) and colors
     * (light grey over a transparent background).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, float scale)
    {
        return createPattern(pattern, defaultDimension, scale, defaultLineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code> and <code>Color</code> using the default bitmap
     * dimensions (32x32) and backgound color (transparent).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, float scale, Color lineColor)
    {
        return createPattern(pattern, defaultDimension, scale, lineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code> and <code>Color</code>s using the default bitmap
     * dimensions (32x32).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @param backColor the pattern background <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, float scale, Color lineColor, Color backColor)
    {
        return createPattern(pattern, defaultDimension, scale, lineColor, backColor);
    }

    /**
     * Draws a pattern with a given <code>scale</code>, <code>Color</code> and bitmap
     * dimensions, using the default backgound color (transparent).
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param size the <code>Dimension</code> of the <code>BufferedImage</code> produced.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, Dimension size, float scale, Color lineColor)
    {
        return createPattern(pattern, size, scale, lineColor, defaultBackColor);
    }

    /**
     * Draws a pattern with the given <code>scale</code>, <code>Color</code>s and bitmap dimensions.
     * @param pattern the pattern to draw. See {@link PatternFactory} static constants.
     * @param size the <code>Dimension</code> of the <code>BufferedImage</code> produced.
     * @param scale the scale at which the pattern should be drawn (0 to 1).
     * @param lineColor the pattern <code>Color</code>.
     * @param backColor the pattern background <code>Color</code>.
     * @return the corresponding <code>BufferedImage</code>.
     */
    public static BufferedImage createPattern(String pattern, Dimension size, float scale, Color lineColor, Color backColor)
    {
        int halfWidth = size.width / 2;
        int halfHeight = size.height / 2;
        int dim = (int)(size.width * scale);
        BufferedImage image = new BufferedImage(size.width,  size.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setPaint(backColor);
        g2.fillRect(0, 0, size.width, size.height);
        if (scale <= 0)
            return image;

        // Pattern
        g2.setPaint(lineColor);
        g2.setStroke(new BasicStroke(dim));
        if (pattern.equals(PATTERN_HLINE))
        {
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(0, y, size.width, dim);
        }
        else if (pattern.equals(PATTERN_VLINE))
        {
            int x = halfWidth - 1 - dim / 2;
            g2.fillRect(x, 0, dim, size.height);
        }
        if (pattern.equals(PATTERN_HVLINE))
        {
            int x = halfWidth - 1 - dim / 2;
            g2.fillRect(x, 0, dim, size.height);
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(0, y, size.width, dim);
        }
        else if (pattern.equals(PATTERN_SQUARE))
        {
            int x = halfWidth - dim / 2;
            int y = halfHeight - dim / 2;
            g2.fillRect(x, y, dim, dim);
        }
        else if (pattern.equals(PATTERN_SQUARES))
        {
            int x = halfWidth - 1 - dim / 2;
            int y = halfHeight - 1 - dim / 2;
            g2.fillRect(x, y, dim, dim);
            g2.fillRect(x - halfWidth, y - halfHeight, dim, dim);
            g2.fillRect(x - halfWidth, y + halfHeight, dim, dim);
            g2.fillRect(x + halfWidth, y - halfHeight, dim, dim);
            g2.fillRect(x + halfWidth, y + halfHeight, dim, dim);
        }
        else if (pattern.equals(PATTERN_CIRCLE))
        {
            int x = halfWidth - dim / 2;
            int y = halfHeight - dim / 2;
            g2.fillOval(x, y, dim, dim);
        }
        else if (pattern.equals(PATTERN_CIRCLES))
        {
            int x = halfWidth - 1 - dim / 2;
            int y = halfHeight - 1 - dim / 2;
            g2.fillOval(x, y, dim, dim);
            g2.fillOval(x - halfWidth, y - halfHeight, dim, dim);
            g2.fillOval(x - halfWidth, y + halfHeight, dim, dim);
            g2.fillOval(x + halfWidth, y - halfHeight, dim, dim);
            g2.fillOval(x + halfWidth, y + halfHeight, dim, dim);
        }
        else if (pattern.equals(PATTERN_TRIANGLE_UP))
        {
            GeneralPath path = new GeneralPath();
            path.moveTo(halfWidth - 1 - dim / 2, halfHeight - 1 + dim / 2);
            path.lineTo(halfWidth - 1, halfHeight - 1 - dim / 2);
            path.lineTo(halfWidth - 1 + dim / 2, halfHeight - 1 + dim / 2);
            path.lineTo(halfWidth - 1 - dim / 2, halfHeight - 1 + dim / 2);
            g2.fill(path);
        }
        else if (pattern.equals(PATTERN_DIAGONAL_UP) || pattern.equals(PATTERN_DIAGONAL_DOWN))
        {
            if (pattern.equals(PATTERN_DIAGONAL_DOWN))
            {
                AffineTransform at = AffineTransform.getScaleInstance(-1, 1);
                at.translate(-size.width, 0);
                g2.setTransform(at);
            }
            g2.drawLine(-dim, size.height - 1 + dim, size.width - 1 + dim, - dim);
            g2.drawLine(-dim - 1, dim, dim - 1, - dim);
            g2.drawLine(size.width - dim, size.height - 1 + dim, size.width + dim, size.height - 1 - dim);
        }
        else if (pattern.equals(GRADIENT_VLINEAR))
        {
            g2.setPaint(new GradientPaint((float)halfWidth, 0f, lineColor, (float)halfWidth, (float)size.height - 1, backColor));
            g2.fillRect(0, 0, size.width, size.height);
        }
        else if (pattern.equals(GRADIENT_HLINEAR))
        {
            g2.setPaint(new GradientPaint(0f, halfHeight, lineColor, (float)size.width - 1, halfHeight, backColor));
            g2.fillRect(0, 0, size.width, size.height);
        }

        return image;
    }

    // Convolution processing

    /**
     * Blurs an image.
     * @param sourceImage the image to blur.
     * @return the blurred image.
     */
    public static BufferedImage blur(BufferedImage sourceImage)
    {
        return blur(sourceImage, 3);
    }

    /**
     * Blurs an image with a specified convolution matrix size.
     * @param sourceImage the image to blur.
     * @param kernelSize the convolution matrix size.
     * @return the blurred image.
     */
    public static BufferedImage blur(BufferedImage sourceImage, int kernelSize)
    {
        int size = kernelSize * kernelSize;
        float value = 1f / size;
        float[] matrix = new float[size];
        for (int i = 0; i < size; i++)
            matrix[i] = value;
        BufferedImage destImage = new BufferedImage(sourceImage.getWidth(),  sourceImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImageOp op = new ConvolveOp( new Kernel(kernelSize, kernelSize, matrix) );
        op.filter(sourceImage, destImage);
        return destImage;
    }

}
