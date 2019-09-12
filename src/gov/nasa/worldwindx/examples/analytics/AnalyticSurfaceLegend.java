/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.analytics;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.text.Format;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: AnalyticSurfaceLegend.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class AnalyticSurfaceLegend implements Renderable
{
    public interface LabelAttributes
    {
        double getValue();

        String getText();

        Font getFont();

        Color getColor();

        Point2D getOffset();
    }

    protected static final Font DEFAULT_FONT = Font.decode("Arial-PLAIN-12");
    protected static final Color DEFAULT_COLOR = Color.WHITE;
    protected static final int DEFAULT_WIDTH = 32;
    protected static final int DEFAULT_HEIGHT = 256;

    protected boolean visible = true;
    protected ScreenImage screenImage;
    protected Iterable<? extends Renderable> labels;

    public static AnalyticSurfaceLegend fromColorGradient(int width, int height, double minValue, double maxValue,
        double minHue, double maxHue, Color borderColor, Iterable<? extends LabelAttributes> labels,
        LabelAttributes titleLabel)
    {
        AnalyticSurfaceLegend legend = new AnalyticSurfaceLegend();
        legend.screenImage = new ScreenImage();
        legend.screenImage.setImageSource(legend.createColorGradientLegendImage(width, height, minHue, maxHue,
            borderColor));
        legend.labels = legend.createColorGradientLegendLabels(width, height, minValue, maxValue, labels, titleLabel);

        return legend;
    }

    public static AnalyticSurfaceLegend fromColorGradient(double minValue, double maxValue, double minHue,
        double maxHue, Iterable<? extends LabelAttributes> labels, LabelAttributes titleLabel)
    {
        return fromColorGradient(DEFAULT_WIDTH, DEFAULT_HEIGHT, minValue, maxValue, minHue, maxHue, DEFAULT_COLOR,
            labels,
            titleLabel);
    }

    protected AnalyticSurfaceLegend()
    {
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public double getOpacity()
    {
        return this.screenImage.getOpacity();
    }

    public void setOpacity(double opacity)
    {
        if (opacity < 0d || opacity > 1d)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.screenImage.setOpacity(opacity);
    }

    public Point getScreenLocation(DrawContext dc)
    {
        return this.screenImage.getScreenLocation(dc);
    }

    public void setScreenLocation(Point point)
    {
        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.screenImage.setScreenLocation(point);
    }

    public int getWidth(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.screenImage.getImageWidth(dc);
    }

    public int getHeight(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.screenImage.getImageHeight(dc);
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isVisible())
            return;

        this.doRender(dc);
    }

    //**************************************************************//
    //********************  Legend Utilities  **********************//
    //**************************************************************//

    public static Iterable<? extends AnalyticSurfaceLegend.LabelAttributes> createDefaultColorGradientLabels(
        double minValue, double maxValue, Format format)
    {
        if (format == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<AnalyticSurfaceLegend.LabelAttributes> labels
            = new ArrayList<AnalyticSurfaceLegend.LabelAttributes>();

        int numLabels = 5;
        Font font = Font.decode("Arial-BOLD-12");

        for (int i = 0; i < numLabels; i++)
        {
            double value = WWMath.mix(i / (double) (numLabels - 1), minValue, maxValue);

            String text = format.format(value);
            if (!WWUtil.isEmpty(text))
            {
                labels.add(createLegendLabelAttributes(value, text, font, Color.WHITE, 5d, 0d));
            }
        }

        return labels;
    }

    public static AnalyticSurfaceLegend.LabelAttributes createDefaultTitle(String text)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Font font = Font.decode("Arial-BOLD-16");
        return createLegendLabelAttributes(0d, text, font, Color.WHITE, 0d, -20d);
    }

    public static AnalyticSurfaceLegend.LabelAttributes createLegendLabelAttributes(final double value,
        final String text, final Font font, final Color color, final double xOffset, final double yOffset)
    {
        return new AnalyticSurfaceLegend.LabelAttributes()
        {
            public double getValue()
            {
                return value;
            }

            public String getText()
            {
                return text;
            }

            public Font getFont()
            {
                return font;
            }

            public Color getColor()
            {
                return color;
            }

            public Point2D getOffset()
            {
                return new Point2D.Double(xOffset, yOffset);
            }
        };
    }

    //**************************************************************//
    //********************  Legend Rendering  **********************//
    //**************************************************************//

    protected void doRender(DrawContext dc)
    {
        this.screenImage.render(dc);

        if (!dc.isPickingMode() && this.labels != null)
        {
            for (Renderable renderable : this.labels)
            {
                if (renderable != null)
                    renderable.render(dc);
            }
        }
    }

    protected void drawLabel(DrawContext dc, LabelAttributes attr, double x, double y, String halign, String valign)
    {
        String text = attr.getText();
        if (WWUtil.isEmpty(text))
            return;

        Font font = attr.getFont();
        if (font == null)
            font = DEFAULT_FONT;

        Color color = DEFAULT_COLOR;
        if (attr.getColor() != null)
            color = attr.getColor();

        Point location = this.getScreenLocation(dc);
        if (location != null)
        {
            x += location.getX() - this.screenImage.getImageWidth(dc) / 2;
            y += location.getY() - this.screenImage.getImageHeight(dc) / 2;
        }

        Point2D offset = attr.getOffset();
        if (offset != null)
        {
            x += offset.getX();
            y += offset.getY();
        }

        TextRenderer tr = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
        if (tr == null)
            return;

        Rectangle2D bounds = tr.getBounds(text);
        if (bounds != null)
        {
            if (AVKey.CENTER.equals(halign))
                x += -(bounds.getWidth() / 2d);
            if (AVKey.RIGHT.equals(halign))
                x += -bounds.getWidth();

            if (AVKey.CENTER.equals(valign))
                y += (bounds.getHeight() + bounds.getY());
            if (AVKey.TOP.equals(valign))
                y += bounds.getHeight();
        }

        Rectangle viewport = dc.getView().getViewport();
        tr.beginRendering(viewport.width, viewport.height);
        try
        {
            double yInGLCoords = viewport.height - y - 1;

            // Draw the text outline, in a contrasting color.
            tr.setColor(WWUtil.computeContrastingColor(color));
            tr.draw(text, (int) x - 1, (int) yInGLCoords - 1);
            tr.draw(text, (int) x + 1, (int) yInGLCoords - 1);
            tr.draw(text, (int) x + 1, (int) yInGLCoords + 1);
            tr.draw(text, (int) x - 1, (int) yInGLCoords + 1);

            // Draw the text over its outline, in the specified color.
            tr.setColor(color);
            tr.draw(text, (int) x, (int) yInGLCoords);
        }
        finally
        {
            tr.endRendering();
        }
    }

    //**************************************************************//
    //********************  Hue Gradient Legend  *******************//
    //**************************************************************//

    protected BufferedImage createColorGradientLegendImage(int width, int height, double minHue, double maxHue,
        Color borderColor)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = image.createGraphics();
        try
        {
            for (int y = 0; y < height; y++)
            {
                double hue = WWMath.mix(1d - y / (double) (height - 1), minHue, maxHue);
                g2d.setColor(Color.getHSBColor((float) hue, 1f, 1f));
                g2d.drawLine(0, y, width - 1, y);
            }

            if (borderColor != null)
            {
                g2d.setColor(borderColor);
                g2d.drawRect(0, 0, width - 1, height - 1);
            }
        }
        finally
        {
            g2d.dispose();
        }

        return image;
    }

    protected Iterable<? extends Renderable> createColorGradientLegendLabels(int width, int height,
        double minValue, double maxValue, Iterable<? extends LabelAttributes> labels, LabelAttributes titleLabel)
    {
        ArrayList<Renderable> list = new ArrayList<Renderable>();

        if (labels != null)
        {
            for (LabelAttributes attr : labels)
            {
                if (attr == null)
                    continue;

                double factor = WWMath.computeInterpolationFactor(attr.getValue(), minValue, maxValue);
                double y = (1d - factor) * (height - 1);
                list.add(new LabelRenderable(this, attr, width, y, AVKey.LEFT, AVKey.CENTER));
            }
        }

        if (titleLabel != null)
        {
            list.add(new LabelRenderable(this, titleLabel, width / 2d, 0d, AVKey.CENTER, AVKey.BOTTOM));
        }

        return list;
    }

    //**************************************************************//
    //********************  Legend Label  **************************//
    //**************************************************************//

    protected static class LabelRenderable implements Renderable
    {
        protected final OrderedLabel orderedLabel;

        public LabelRenderable(AnalyticSurfaceLegend legend, LabelAttributes attr, double x, double y,
            String halign, String valign)
        {
            this.orderedLabel = new OrderedLabel(legend, attr, x, y, halign, valign);
        }

        public void render(DrawContext dc)
        {
            dc.addOrderedRenderable(this.orderedLabel);
        }
    }

    protected static class OrderedLabel implements OrderedRenderable
    {
        protected final AnalyticSurfaceLegend legend;
        protected final LabelAttributes attr;
        protected final double x;
        protected final double y;
        protected final String halign;
        protected final String valign;

        public OrderedLabel(AnalyticSurfaceLegend legend, LabelAttributes attr, double x, double y,
            String halign, String valign)
        {
            this.legend = legend;
            this.attr = attr;
            this.x = x;
            this.y = y;
            this.halign = halign;
            this.valign = valign;
        }

        public double getDistanceFromEye()
        {
            return 0;
        }

        public void render(DrawContext dc)
        {
            this.legend.drawLabel(dc, this.attr, this.x, this.y, this.halign, this.valign);
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            // Intentionally left blank.
        }
    }
}
