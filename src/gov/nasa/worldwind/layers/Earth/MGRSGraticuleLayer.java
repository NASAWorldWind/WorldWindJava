/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.GraticuleRenderingParams;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * @author Patrick Murris
 * @version $Id: MGRSGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */

public class MGRSGraticuleLayer extends UTMBaseGraticuleLayer
{
    /** Graticule for the UTM grid. */
    public static final String GRATICULE_UTM_GRID = "Graticule.UTM.Grid";
    /** Graticule for the 100,000 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_100000M = "Graticule.100000m";
    /** Graticule for the 10,000 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_10000M = "Graticule.10000m";
    /** Graticule for the 1,000 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_1000M = "Graticule.1000m";
    /** Graticule for the 100 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_100M = "Graticule.100m";
    /** Graticule for the 10 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_10M = "Graticule.10m";
    /** Graticule for the 1 meter grid, nested inside the UTM grid. */
    public static final String GRATICULE_1M = "Graticule.1m";

    private GridZone[][] gridZones = new GridZone[20][60]; // row/col
    private GridZone[] poleZones = new GridZone[4]; // North x2 + South x2
    private double zoneMaxAltitude = 5000e3;
    private double squareMaxAltitude = 3000e3;

    /** Creates a new <code>MGRSGraticuleLayer</code>, with default graticule attributes. */
    public MGRSGraticuleLayer()
    {
        initRenderingParams();
        this.metricScaleSupport.setScaleModulo((int) 100e3);
        this.setName(Logging.getMessage("layers.Earth.MGRSGraticule.Name"));
    }

    /**
     * Returns the maxiumum resolution graticule that will be rendered, or null if no graticules will be rendered. By
     * default, all graticules are rendered, and this will return GRATICULE_1M.
     *
     * @return maximum resolution rendered.
     */
    public String getMaximumGraticuleResolution()
    {
        String maxTypeDrawn = null;
        String[] orderedTypeList = getOrderedTypes();
        for (String type : orderedTypeList)
        {
            GraticuleRenderingParams params = getRenderingParams(type);
            if (params.isDrawLines())
            {
                maxTypeDrawn = type;
            }
        }
        return maxTypeDrawn;
    }

    /**
     * Sets the maxiumum resolution graticule that will be rendered.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setMaximumGraticuleResolution(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean pastTarget = false;
        String[] orderedTypeList = getOrderedTypes();
        for (String type : orderedTypeList)
        {
            // Enable all graticulte BEFORE and INCLUDING the target.
            // Disable all graticules AFTER the target.
            GraticuleRenderingParams params = getRenderingParams(type);
            params.setDrawLines(!pastTarget);
            params.setDrawLabels(!pastTarget);
            if (!pastTarget && type.equals(graticuleType))
            {
                pastTarget = true;
            }
        }
    }

    /**
     * Returns the line color of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return Color of the the graticule line.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public Color getGraticuleLineColor(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLineColor();
    }

    /**
     * Sets the line rendering color for the specified graticule.
     *
     * @param color         the line color for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineColor(Color color, String graticuleType)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLineColor(color);
    }

    /**
     * Sets the line rendering color for the specified graticules.
     *
     * @param color         the line color for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineColor(Color color, Iterable<String> graticuleType)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setGraticuleLineColor(color, type);
        }
    }

    /**
     * Sets the line rendering color for all graticules.
     *
     * @param color the line color.
     *
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setGraticuleLineColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setGraticuleLineColor(color, type);
        }
    }

    /**
     * Returns the line width of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return width of the graticule line.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public double getGraticuleLineWidth(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLineWidth();
    }

    /**
     * Sets the line rendering width for the specified graticule.
     *
     * @param lineWidth     the line rendering width for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setGraticuleLineWidth(double lineWidth, String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLineWidth(lineWidth);
    }

    /**
     * Sets the line rendering width for the specified graticules.
     *
     * @param lineWidth     the line rendering width for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setGraticuleLineWidth(double lineWidth, Iterable<String> graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setGraticuleLineWidth(lineWidth, type);
        }
    }

    /**
     * Sets the line rendering width for all graticules.
     *
     * @param lineWidth the line rendering width.
     */
    public void setGraticuleLineWidth(double lineWidth)
    {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setGraticuleLineWidth(lineWidth, type);
        }
    }

    /**
     * Returns the line rendering style of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return line rendering style of the graticule.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public String getGraticuleLineStyle(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLineStyle();
    }

    /**
     * Sets the line rendering style for the specified graticule.
     *
     * @param lineStyle     the line rendering style for the specified graticule. One of LINE_STYLE_PLAIN,
     *                      LINE_STYLE_DASHED, or LINE_STYLE_DOTTED.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M
     *
     * @throws IllegalArgumentException if <code>lineStyle</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineStyle(String lineStyle, String graticuleType)
    {
        if (lineStyle == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLineStyle(lineStyle);
    }

    /**
     * Sets the line rendering style for the specified graticules.
     *
     * @param lineStyle     the line rendering style for the specified graticules. One of LINE_STYLE_PLAIN,
     *                      LINE_STYLE_DASHED, or LINE_STYLE_DOTTED.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M
     *
     * @throws IllegalArgumentException if <code>lineStyle</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineStyle(String lineStyle, Iterable<String> graticuleType)
    {
        if (lineStyle == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setGraticuleLineStyle(lineStyle, type);
        }
    }

    /**
     * Sets the line rendering style for all graticules.
     *
     * @param lineStyle the line rendering style. One of LINE_STYLE_PLAIN, LINE_STYLE_DASHED, or LINE_STYLE_DOTTED.
     *
     * @throws IllegalArgumentException if <code>lineStyle</code> is null.
     */
    public void setGraticuleLineStyle(String lineStyle)
    {
        if (lineStyle == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setGraticuleLineStyle(lineStyle, type);
        }
    }

    /**
     * Returns whether specified graticule labels will be rendered.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return true if graticule labels are will be rendered; false otherwise.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public boolean isDrawLabels(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).isDrawLabels();
    }

    /**
     * Sets whether the specified graticule labels will be rendered. If true, the graticule labels will be rendered.
     * Otherwise, the graticule labels will not be rendered, but other graticules will not be affected.
     *
     * @param drawLabels    true to render graticule labels; false to disable rendering.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setDrawLabels(boolean drawLabels, String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setDrawLabels(drawLabels);
    }

    /**
     * Sets whether the specified graticule labels will be rendered. If true, the graticule labels will be rendered.
     * Otherwise, the graticule labels will not be rendered, but other graticules will not be affected.
     *
     * @param drawLabels    true to render graticule labels; false to disable rendering.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setDrawLabels(boolean drawLabels, Iterable<String> graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setDrawLabels(drawLabels, type);
        }
    }

    /**
     * Sets whether all graticule labels will be rendered. If true, all graticule labels will be rendered. Otherwise,
     * all graticule labels will not be rendered.
     *
     * @param drawLabels true to render all graticule labels; false to disable rendering.
     */
    public void setDrawLabels(boolean drawLabels)
    {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setDrawLabels(drawLabels, type);
        }
    }

    /**
     * Returns the label color of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return Color of the the graticule label.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public Color getLabelColor(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLabelColor();
    }

    /**
     * Sets the label rendering color for the specified graticule.
     *
     * @param color         the label color for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setLabelColor(Color color, String graticuleType)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLabelColor(color);
    }

    /**
     * Sets the label rendering color for the specified graticules.
     *
     * @param color         the label color for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setLabelColor(Color color, Iterable<String> graticuleType)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setLabelColor(color, type);
        }
    }

    /**
     * Sets the label rendering color for all graticules.
     *
     * @param color the label color.
     *
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setLabelColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setLabelColor(color, type);
        }
    }

    /**
     * Returns the label font of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return Font of the graticule label.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public Font getLabelFont(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLabelFont();
    }

    /**
     * Sets the label rendering font for the specified graticule.
     *
     * @param font          the label font for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>font</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setLabelFont(Font font, String graticuleType)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLabelFont(font);
    }

    /**
     * Sets the label rendering font for the specified graticules.
     *
     * @param font          the label font for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>font</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setLabelFont(Font font, Iterable<String> graticuleType)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setLabelFont(font, type);
        }
    }

    /**
     * Sets the label rendering font for all graticules.
     *
     * @param font the label font.
     *
     * @throws IllegalArgumentException if <code>font</code> is null.
     */
    public void setLabelFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setLabelFont(font, type);
        }
    }

    protected void initRenderingParams()
    {
        GraticuleRenderingParams params;
        // UTM graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.YELLOW);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.YELLOW);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-16"));
        setRenderingParams(GRATICULE_UTM_GRID, params);
        // 100,000 meter graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.GREEN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-14"));
        setRenderingParams(GRATICULE_100000M, params);
        // 10,000 meter graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 102, 255));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 102, 255));
        setRenderingParams(GRATICULE_10000M, params);
        // 1,000 meter graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, Color.CYAN);
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, Color.CYAN);
        setRenderingParams(GRATICULE_1000M, params);
        // 100 meter graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 153, 153));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 153, 153));
        setRenderingParams(GRATICULE_100M, params);
        // 10 meter graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(102, 255, 204));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(102, 255, 204));
        setRenderingParams(GRATICULE_10M, params);
        // 1 meter graticule
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(153, 153, 255));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(153, 153, 255));
        setRenderingParams(GRATICULE_1M, params);
    }

    protected String[] getOrderedTypes()
    {
        return new String[] {
            GRATICULE_UTM_GRID,
            GRATICULE_100000M,
            GRATICULE_10000M,
            GRATICULE_1000M,
            GRATICULE_100M,
            GRATICULE_10M,
            GRATICULE_1M,
        };
    }

    protected String getTypeFor(int resolution)
    {
        String graticuleType = null;
        switch (resolution)
        {
            case 100000: // 100,000 meters
                graticuleType = GRATICULE_100000M;
                break;
            case 10000:  // 10,000 meters
                graticuleType = GRATICULE_10000M;
                break;
            case 1000:   // 1000 meters
                graticuleType = GRATICULE_1000M;
                break;
            case 100:    // 100 meters
                graticuleType = GRATICULE_100M;
                break;
            case 10:     // 10 meters
                graticuleType = GRATICULE_10M;
                break;
            case 1:      // 1 meter
                graticuleType = GRATICULE_1M;
                break;
        }

        return graticuleType;
    }

    // --- Renderable layer --------------------------------------------------------------

    protected void clear(DrawContext dc)
    {
        super.clear(dc);

        this.frameCount++;
        this.applyTerrainConformance();

        this.metricScaleSupport.clear();
        this.metricScaleSupport.computeZone(dc);
    }

    private void applyTerrainConformance()
    {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            getRenderingParams(type).setValue(
                GraticuleRenderingParams.KEY_LINE_CONFORMANCE, this.terrainConformance);
        }
    }

    protected Sector computeVisibleSector(DrawContext dc)
    {
        return dc.getVisibleSector();
    }

    protected void selectRenderables(DrawContext dc)
    {
        if (dc.getView().getEyePosition().getElevation() <= this.zoneMaxAltitude)
        {
            this.selectMGRSRenderables(dc, this.computeVisibleSector(dc));
            this.metricScaleSupport.selectRenderables(dc);
        }
        else
        {
            super.selectRenderables(dc);
        }
    }

    protected void selectMGRSRenderables(DrawContext dc, Sector vs)
    {
        ArrayList<GridZone> zoneList = getVisibleZones(dc);
        if (zoneList.size() > 0)
        {
            for (GridZone gz : zoneList)
            {
                // Select visible grid zones elements
                gz.selectRenderables(dc, vs, this);
            }
        }
    }

    private ArrayList<GridZone> getVisibleZones(DrawContext dc)
    {
        ArrayList<GridZone> zoneList = new ArrayList<GridZone>();
        Sector vs = dc.getVisibleSector();
        if (vs != null)
        {
            // UTM Grid
            Rectangle2D gridRectangle = getGridRectangleForSector(vs);
            if (gridRectangle != null)
            {
                for (int row = (int) gridRectangle.getY(); row <= gridRectangle.getY() + gridRectangle.getHeight();
                    row++)
                {
                    for (int col = (int) gridRectangle.getX(); col <= gridRectangle.getX() + gridRectangle.getWidth();
                        col++)
                    {
                        if (row != 19 || (col != 31 && col != 33 && col != 35)) // ignore X32, 34 and 36
                        {
                            if (gridZones[row][col] == null)
                                gridZones[row][col] = new GridZone(getGridSector(row, col));
                            if (gridZones[row][col].isInView(dc))
                                zoneList.add(gridZones[row][col]);
                            else
                                gridZones[row][col].clearRenderables();
                        }
                    }
                }
            }
            // Poles
            if (vs.getMaxLatitude().degrees > 84)
            {
                // North pole
                if (poleZones[2] == null)
                    poleZones[2] = new GridZone(Sector.fromDegrees(84, 90, -180, 0)); // Y
                if (poleZones[3] == null)
                    poleZones[3] = new GridZone(Sector.fromDegrees(84, 90, 0, 180));  // Z
                zoneList.add(poleZones[2]);
                zoneList.add(poleZones[3]);
            }
            if (vs.getMinLatitude().degrees < -80)
            {
                // South pole
                if (poleZones[0] == null)
                    poleZones[0] = new GridZone(Sector.fromDegrees(-90, -80, -180, 0)); // B
                if (poleZones[1] == null)
                    poleZones[1] = new GridZone(Sector.fromDegrees(-90, -80, 0, 180));  // A
                zoneList.add(poleZones[0]);
                zoneList.add(poleZones[1]);
            }
        }
        return zoneList;
    }

    private Rectangle2D getGridRectangleForSector(Sector sector)
    {
        Rectangle2D rectangle = null;
        if (sector.getMinLatitude().degrees < 84 && sector.getMaxLatitude().degrees > -80)
        {
            Sector gridSector = Sector.fromDegrees(
                Math.max(sector.getMinLatitude().degrees, -80), Math.min(sector.getMaxLatitude().degrees, 84),
                sector.getMinLongitude().degrees, sector.getMaxLongitude().degrees);
            int x1 = getGridColumn(gridSector.getMinLongitude().degrees);
            int x2 = getGridColumn(gridSector.getMaxLongitude().degrees);
            int y1 = getGridRow(gridSector.getMinLatitude().degrees);
            int y2 = getGridRow(gridSector.getMaxLatitude().degrees);
            // Adjust rectangle to include special zones
            if (y1 <= 17 && y2 >= 17 && x2 == 30) // 32V Norway
                x2 = 31;
            if (y1 <= 19 && y2 >= 19) // X band
            {
                if (x1 == 31) // 31X
                    x1 = 30;
                if (x2 == 31) // 33X
                    x2 = 32;
                if (x1 == 33) // 33X
                    x1 = 32;
                if (x2 == 33) // 35X
                    x2 = 34;
                if (x1 == 35) // 35X
                    x1 = 34;
                if (x2 == 35) // 37X
                    x2 = 36;
            }
            rectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        }
        return rectangle;
    }

    private int getGridColumn(Double longitude)
    {
        int col = (int) Math.floor((longitude + 180) / 6d);
        return Math.min(col, 59);
    }

    private int getGridRow(Double latitude)
    {
        int row = (int) Math.floor((latitude + 80) / 8d);
        return Math.min(row, 19);
    }

    private Sector getGridSector(int row, int col)
    {
        int minLat = -80 + row * 8;
        int maxLat = minLat + (minLat != 72 ? 8 : 12);
        int minLon = -180 + col * 6;
        int maxLon = minLon + 6;
        // Special sectors
        if (row == 17 && col == 30)         // 31V
            maxLon -= 3;
        else if (row == 17 && col == 31)    // 32V
            minLon -= 3;
        else if (row == 19 && col == 30)   // 31X
            maxLon += 3;
        else if (row == 19 && col == 31)   // 32X does not exist
        {
            minLon += 3;
            maxLon -= 3;
        }
        else if (row == 19 && col == 32)   // 33X
        {
            minLon -= 3;
            maxLon += 3;
        }
        else if (row == 19 && col == 33)   // 34X does not exist
        {
            minLon += 3;
            maxLon -= 3;
        }
        else if (row == 19 && col == 34)   // 35X
        {
            minLon -= 3;
            maxLon += 3;
        }
        else if (row == 19 && col == 35)   // 36X does not exist
        {
            minLon += 3;
            maxLon -= 3;
        }
        else if (row == 19 && col == 36)   // 37X
            minLon -= 3;
        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    private boolean isNorthNeighborInView(GridZone gz, DrawContext dc)
    {
        if (gz.isUPS)
            return true;

        int row = getGridRow(gz.sector.getCentroid().getLatitude().degrees);
        int col = getGridColumn(gz.sector.getCentroid().getLongitude().degrees);
        GridZone neighbor = row + 1 <= 19 ? this.gridZones[row + 1][col] : null;
        return neighbor != null && neighbor.isInView(dc);
    }

    private boolean isEastNeighborInView(GridZone gz, DrawContext dc)
    {
        if (gz.isUPS)
            return true;

        int row = getGridRow(gz.sector.getCentroid().getLatitude().degrees);
        int col = getGridColumn(gz.sector.getCentroid().getLongitude().degrees);
        GridZone neighbor = col + 1 <= 59 ? this.gridZones[row][col + 1] : null;
        return neighbor != null && neighbor.isInView(dc);
    }

    //--- Grid zone ----------------------------------------------------------------------

    /** Represent a UTM zone / latitude band intersection */
    private class GridZone
    {
        private static final double ONEHT = 100e3;
        private static final double TWOMIL = 2e6;

        private Sector sector;
        private boolean isUPS = false;
        private String name = "";
        private int UTMZone = 0;
        private String hemisphere = null;

        private ArrayList<GridElement> gridElements;
        private ArrayList<SquareZone> squares;

        public GridZone(Sector sector)
        {
            this.sector = sector;
            this.isUPS = (sector.getMaxLatitude().degrees > UTM_MAX_LATITUDE
                || sector.getMinLatitude().degrees < UTM_MIN_LATITUDE);
            try
            {
                MGRSCoord MGRS = MGRSCoord.fromLatLon(sector.getCentroid().getLatitude(),
                    sector.getCentroid().getLongitude(), globe);
                if (this.isUPS)
                {
                    this.name = MGRS.toString().substring(2, 3);
                    this.hemisphere = sector.getMinLatitude().degrees > 0 ? AVKey.NORTH : AVKey.SOUTH;
                }
                else
                {
                    this.name = MGRS.toString().substring(0, 3);
                    UTMCoord UTM = UTMCoord.fromLatLon(sector.getCentroid().getLatitude(),
                        sector.getCentroid().getLongitude(), globe);
                    this.UTMZone = UTM.getZone();
                    this.hemisphere = UTM.getHemisphere();
                }
            }
            catch (IllegalArgumentException ignore)
            {
            }
        }

        public Extent getExtent(Globe globe, double ve)
        {
            return Sector.computeBoundingCylinder(globe, ve, this.sector);
        }

        public boolean isInView(DrawContext dc)
        {
            return dc.getView().getFrustumInModelCoordinates().intersects(
                this.getExtent(dc.getGlobe(), dc.getVerticalExaggeration()));
        }

        public void selectRenderables(DrawContext dc, Sector vs, MGRSGraticuleLayer layer)
        {
            // Select zone elements
            if (this.gridElements == null)
                createRenderables();

            for (GridElement ge : this.gridElements)
            {
                if (ge.isInView(dc, vs))
                {
                    if (ge.type.equals(GridElement.TYPE_LINE_NORTH) && isNorthNeighborInView(this, dc))
                        continue;
                    if (ge.type.equals(GridElement.TYPE_LINE_EAST) && isEastNeighborInView(this, dc))
                        continue;

                    layer.addRenderable(ge.renderable, GRATICULE_UTM_GRID);
                }
            }

            if (dc.getView().getEyePosition().getElevation() > MGRSGraticuleLayer.this.squareMaxAltitude)
                return;

            // Select 100km squares elements
            if (this.squares == null)
                createSquares();
            for (SquareZone sz : this.squares)
            {
                if (sz.isInView(dc))
                {
                    sz.selectRenderables(dc, vs);
                }
                else
                    sz.clearRenderables();
            }
        }

        public void clearRenderables()
        {
            if (this.gridElements != null)
            {
                this.gridElements.clear();
                this.gridElements = null;
            }
            if (this.squares != null)
            {
                for (SquareZone sz : this.squares)
                {
                    sz.clearRenderables();
                }
                this.squares.clear();
                this.squares = null;
            }
        }

        private void createSquares()
        {
            if (this.isUPS)
                createSquaresUPS();
            else
                createSquaresUTM();
        }

        private void createSquaresUTM()
        {
            try
            {
                // Find grid zone easting and northing boundaries
                UTMCoord UTM;
                UTM = UTMCoord.fromLatLon(this.sector.getMinLatitude(), this.sector.getCentroid().getLongitude(),
                    globe);
                double minNorthing = UTM.getNorthing();
                UTM = UTMCoord.fromLatLon(this.sector.getMaxLatitude(), this.sector.getCentroid().getLongitude(),
                    globe);
                double maxNorthing = UTM.getNorthing();
                maxNorthing = maxNorthing == 0 ? 10e6 : maxNorthing;
                UTM = UTMCoord.fromLatLon(this.sector.getMinLatitude(), this.sector.getMinLongitude(), globe);
                double minEasting = UTM.getEasting();
                UTM = UTMCoord.fromLatLon(this.sector.getMaxLatitude(), this.sector.getMinLongitude(), globe);
                minEasting = UTM.getEasting() < minEasting ? UTM.getEasting() : minEasting;
                double maxEasting = 1e6 - minEasting;

                // Compensate for some distorted zones
                if (this.name.equals("32V")) // catch KS and LS in 32V
                    maxNorthing += 20e3;
                if (this.name.equals("31X")) // catch GA and GV in 31X
                    maxEasting += ONEHT;

                // Create squares
                this.squares = createSquaresGrid(this.UTMZone, this.hemisphere, this.sector, minEasting, maxEasting,
                    minNorthing, maxNorthing);
                this.setSquareNames();
            }
            catch (IllegalArgumentException ignore)
            {
            }
        }

        private void createSquaresUPS()
        {
            this.squares = new ArrayList<SquareZone>();
            double minEasting, maxEasting, minNorthing, maxNorthing;

            if (AVKey.NORTH.equals(this.hemisphere))
            {
                minNorthing = TWOMIL - ONEHT * 7;
                maxNorthing = TWOMIL + ONEHT * 7;
                minEasting = this.name.equals("Y") ? TWOMIL - ONEHT * 7 : TWOMIL;
                maxEasting = this.name.equals("Y") ? TWOMIL : TWOMIL + ONEHT * 7;
            }
            else // AVKey.SOUTH.equals(this.hemisphere)
            {
                minNorthing = TWOMIL - ONEHT * 12;
                maxNorthing = TWOMIL + ONEHT * 12;
                minEasting = this.name.equals("A") ? TWOMIL - ONEHT * 12 : TWOMIL;
                maxEasting = this.name.equals("A") ? TWOMIL : TWOMIL + ONEHT * 12;
            }

            // Create squares
            this.squares = createSquaresGrid(this.UTMZone, this.hemisphere, this.sector, minEasting, maxEasting,
                minNorthing, maxNorthing);
            this.setSquareNames();
        }

        private void setSquareNames()
        {
            for (SquareZone sz : this.squares)
            {
                this.setSquareName(sz);
            }
        }

        private void setSquareName(SquareZone sz)
        {
            // Find out MGRS 100Km square name
            double tenMeterRadian = 10d / 6378137d;
            try
            {
                MGRSCoord MGRS = null;
                if (sz.centroid != null && sz.isPositionInside(new Position(sz.centroid, 0)))
                    MGRS = MGRSCoord.fromLatLon(sz.centroid.latitude, sz.centroid.longitude, globe);
                else if (sz.isPositionInside(sz.sw))
                    MGRS = MGRSCoord.fromLatLon(
                        Angle.fromRadiansLatitude(sz.sw.getLatitude().radians + tenMeterRadian),
                        Angle.fromRadiansLongitude(sz.sw.getLongitude().radians + tenMeterRadian), globe);
                else if (sz.isPositionInside(sz.se))
                    MGRS = MGRSCoord.fromLatLon(
                        Angle.fromRadiansLatitude(sz.se.getLatitude().radians + tenMeterRadian),
                        Angle.fromRadiansLongitude(sz.se.getLongitude().radians - tenMeterRadian), globe);
                else if (sz.isPositionInside(sz.nw))
                    MGRS = MGRSCoord.fromLatLon(
                        Angle.fromRadiansLatitude(sz.nw.getLatitude().radians - tenMeterRadian),
                        Angle.fromRadiansLongitude(sz.nw.getLongitude().radians + tenMeterRadian), globe);
                else if (sz.isPositionInside(sz.ne))
                    MGRS = MGRSCoord.fromLatLon(
                        Angle.fromRadiansLatitude(sz.ne.getLatitude().radians - tenMeterRadian),
                        Angle.fromRadiansLongitude(sz.ne.getLongitude().radians - tenMeterRadian), globe);
                // Set square zone name
                if (MGRS != null)
                    sz.setName(MGRS.toString().substring(3, 5));
            }
            catch (IllegalArgumentException ignore)
            {
            }
        }

        private void createRenderables()
        {
            this.gridElements = new ArrayList<GridElement>();

            ArrayList<Position> positions = new ArrayList<Position>();

            // left meridian segment
            positions.clear();
            positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMinLongitude(), 10e3));
            positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMinLongitude(), 10e3));
            Object polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
            Sector lineSector = new Sector(this.sector.getMinLatitude(), this.sector.getMaxLatitude(),
                this.sector.getMinLongitude(), this.sector.getMinLongitude());
            this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_WEST));

            if (!this.isUPS)
            {
                // right meridian segment
                positions.clear();
                positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMaxLongitude(), 10e3));
                positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMaxLongitude(), 10e3));
                polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
                lineSector = new Sector(this.sector.getMinLatitude(), this.sector.getMaxLatitude(),
                    this.sector.getMaxLongitude(), this.sector.getMaxLongitude());
                this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EAST));

                // bottom parallel segment
                positions.clear();
                positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMinLongitude(), 10e3));
                positions.add(new Position(this.sector.getMinLatitude(), this.sector.getMaxLongitude(), 10e3));
                polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
                lineSector = new Sector(this.sector.getMinLatitude(), this.sector.getMinLatitude(),
                    this.sector.getMinLongitude(), this.sector.getMaxLongitude());
                this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_SOUTH));

                // top parallel segment
                positions.clear();
                positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMinLongitude(), 10e3));
                positions.add(new Position(this.sector.getMaxLatitude(), this.sector.getMaxLongitude(), 10e3));
                polyline = createLineRenderable(new ArrayList<Position>(positions), AVKey.LINEAR);
                lineSector = new Sector(this.sector.getMaxLatitude(), this.sector.getMaxLatitude(),
                    this.sector.getMinLongitude(), this.sector.getMaxLongitude());
                this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTH));
            }

            // Label
            GeographicText text = new UserFacingText(this.name, new Position(this.sector.getCentroid(), 0));
            text.setPriority(10e6);
            this.gridElements.add(new GridElement(this.sector, text, GridElement.TYPE_GRIDZONE_LABEL));
        }
    }
}
