/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: GraticuleSupport.java 2372 2014-10-10 18:32:15Z tgaskins $
 */
public class GraticuleSupport
{
    private static class Pair
    {
        final Object a;
        final Object b;

        Pair(Object a, Object b)
        {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Pair pair = (Pair) o;

            if (a != null ? !a.equals(pair.a) : pair.a != null)
                return false;
            if (b != null ? !b.equals(pair.b) : pair.b != null)
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }
    }

    private Collection<Pair> renderables = new HashSet<Pair>(); // a set to avoid duplicates in multi-pass (2D globes)
    private Map<String, GraticuleRenderingParams> namedParams = new HashMap<String, GraticuleRenderingParams>();
    private Map<String, ShapeAttributes> namedShapeAttributes = new HashMap<String, ShapeAttributes>();
    private AVList defaultParams;
    private GeographicTextRenderer textRenderer = new GeographicTextRenderer();

    public GraticuleSupport()
    {
        this.textRenderer.setEffect(AVKey.TEXT_EFFECT_SHADOW);
        // Keep labels separated by at least two pixels
        this.textRenderer.setCullTextEnabled(true);
        this.textRenderer.setCullTextMargin(1);
        // Shrink and blend labels as they get farther away from the eye
        this.textRenderer.setDistanceMinScale(.5);
        this.textRenderer.setDistanceMinOpacity(.5);
    }

    public void addRenderable(Object renderable, String paramsKey)
    {
        if (renderable == null)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.renderables.add(new Pair(renderable, paramsKey));
    }

    public void removeAllRenderables()
    {
        this.renderables.clear();
    }

    public void render(DrawContext dc)
    {
        this.render(dc, 1);
    }

    public void render(DrawContext dc, double opacity)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.namedShapeAttributes.clear();

        // Render lines and collect text labels
        Collection<GeographicText> text = new ArrayList<GeographicText>();
        for (Pair pair : this.renderables)
        {
            Object renderable = pair.a;
            String paramsKey = (pair.b != null && pair.b instanceof String) ? (String) pair.b : null;
            GraticuleRenderingParams renderingParams = paramsKey != null ? this.namedParams.get(paramsKey) : null;

            if (renderable != null && renderable instanceof Path)
            {
                if (renderingParams == null || renderingParams.isDrawLines())
                {
                    applyRenderingParams(paramsKey, renderingParams, (Path) renderable, opacity);
                    ((Path) renderable).render(dc);
                }
            }
            else if (renderable != null && renderable instanceof GeographicText)
            {
                if (renderingParams == null || renderingParams.isDrawLabels())
                {
                    applyRenderingParams(renderingParams, (GeographicText) renderable, opacity);
                    text.add((GeographicText) renderable);
                }
            }
        }

        // Render text labels
        this.textRenderer.render(dc, text);
    }

    public GraticuleRenderingParams getRenderingParams(String key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GraticuleRenderingParams value = this.namedParams.get(key);
        if (value == null)
        {
            value = new GraticuleRenderingParams();
            initRenderingParams(value);
            if (this.defaultParams != null)
                value.setValues(this.defaultParams);

            this.namedParams.put(key, value);
        }

        return value;
    }

    public Collection<Map.Entry<String, GraticuleRenderingParams>> getAllRenderingParams()
    {
        return this.namedParams.entrySet();
    }

    public void setRenderingParams(String key, GraticuleRenderingParams renderingParams)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        initRenderingParams(renderingParams);
        this.namedParams.put(key, renderingParams);
    }

    public AVList getDefaultParams()
    {
        return this.defaultParams;
    }

    public void setDefaultParams(AVList defaultParams)
    {
        this.defaultParams = defaultParams;
    }

    private AVList initRenderingParams(AVList params)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params.getValue(GraticuleRenderingParams.KEY_DRAW_LINES) == null)
            params.setValue(GraticuleRenderingParams.KEY_DRAW_LINES, Boolean.TRUE);

        if (params.getValue(GraticuleRenderingParams.KEY_LINE_COLOR) == null)
            params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.WHITE);

        if (params.getValue(GraticuleRenderingParams.KEY_LINE_WIDTH) == null)
            //noinspection UnnecessaryBoxing
            params.setValue(GraticuleRenderingParams.KEY_LINE_WIDTH, new Double(1));

        if (params.getValue(GraticuleRenderingParams.KEY_LINE_STYLE) == null)
            params.setValue(GraticuleRenderingParams.KEY_LINE_STYLE, GraticuleRenderingParams.VALUE_LINE_STYLE_SOLID);

        if (params.getValue(GraticuleRenderingParams.KEY_DRAW_LABELS) == null)
            params.setValue(GraticuleRenderingParams.KEY_DRAW_LABELS, Boolean.TRUE);

        if (params.getValue(GraticuleRenderingParams.KEY_LABEL_COLOR) == null)
            params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.WHITE);

        if (params.getValue(GraticuleRenderingParams.KEY_LABEL_FONT) == null)
            params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-12"));

        return params;
    }

    private void applyRenderingParams(AVList params, GeographicText text, double opacity)
    {
        if (params != null && text != null)
        {
            // Apply "label" properties to the GeographicText.
            Object o = params.getValue(GraticuleRenderingParams.KEY_LABEL_COLOR);
            if (o != null && o instanceof Color)
            {
                Color color = applyOpacity((Color) o, opacity);
                float[] compArray = new float[4];
                Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
                int colorValue = compArray[2] < .5f ? 255 : 0;
                text.setColor(color);
                text.setBackgroundColor(new Color(colorValue, colorValue, colorValue, color.getAlpha()));
            }

            o = params.getValue(GraticuleRenderingParams.KEY_LABEL_FONT);
            if (o != null && o instanceof Font)
            {
                text.setFont((Font) o);
            }
        }
    }

    private void applyRenderingParams(String key, AVList params, Path path, double opacity)
    {
        if (key != null && params != null && path != null)
        {
            path.setAttributes(this.getLineShapeAttributes(key, params, opacity));
        }
    }

    private ShapeAttributes getLineShapeAttributes(String key, AVList params, double opacity)
    {
        ShapeAttributes attrs = this.namedShapeAttributes.get(key);
        if (attrs == null)
        {
            attrs = createLineShapeAttributes(params, opacity);
            this.namedShapeAttributes.put(key, attrs);
        }
        return attrs;
    }

    private ShapeAttributes createLineShapeAttributes(AVList params, double opacity)
    {
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setDrawInterior(false);
        attrs.setDrawOutline(true);
        if (params != null)
        {
            // Apply "line" properties.
            Object o = params.getValue(GraticuleRenderingParams.KEY_LINE_COLOR);
            if (o != null && o instanceof Color)
            {
                attrs.setOutlineMaterial(new Material(applyOpacity((Color) o, opacity)));
                attrs.setOutlineOpacity(opacity);
            }

            Double lineWidth = AVListImpl.getDoubleValue(params, GraticuleRenderingParams.KEY_LINE_WIDTH);
            if (lineWidth != null)
            {
                attrs.setOutlineWidth(lineWidth);
            }

            String s = params.getStringValue(GraticuleRenderingParams.KEY_LINE_STYLE);
            // Draw a solid line.
            if (GraticuleRenderingParams.VALUE_LINE_STYLE_SOLID.equalsIgnoreCase(s))
            {
                attrs.setOutlineStipplePattern((short) 0xAAAA);
                attrs.setOutlineStippleFactor(0);
            }
            // Draw the line as longer strokes with space in between.
            else if (GraticuleRenderingParams.VALUE_LINE_STYLE_DASHED.equalsIgnoreCase(s))
            {
                int baseFactor = (int) (lineWidth != null ? Math.round(lineWidth) : 1.0);
                attrs.setOutlineStipplePattern((short) 0xAAAA);
                attrs.setOutlineStippleFactor(3 * baseFactor);
            }
            // Draw the line as a evenly spaced "square" dots.
            else if (GraticuleRenderingParams.VALUE_LINE_STYLE_DOTTED.equalsIgnoreCase(s))
            {
                int baseFactor = (int) (lineWidth != null ? Math.round(lineWidth) : 1.0);
                attrs.setOutlineStipplePattern((short) 0xAAAA);
                attrs.setOutlineStippleFactor(baseFactor);
            }
        }
        return attrs;
    }

    private Color applyOpacity(Color color, double opacity)
    {
        if (opacity >= 1)
            return color;

        float[] compArray = color.getRGBComponents(null);
        return new Color(compArray[0], compArray[1], compArray[2], compArray[3] * (float) opacity);
    }
}
