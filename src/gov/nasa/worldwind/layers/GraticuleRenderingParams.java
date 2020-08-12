/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: GraticuleRenderingParams.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GraticuleRenderingParams extends AVListImpl
{
    public static final String KEY_DRAW_LINES = "DrawGraticule";
    public static final String KEY_LINE_COLOR = "GraticuleLineColor";
    public static final String KEY_LINE_WIDTH = "GraticuleLineWidth";
    public static final String KEY_LINE_STYLE = "GraticuleLineStyle";
    public static final String KEY_LINE_CONFORMANCE = "GraticuleLineConformance";
    public static final String KEY_DRAW_LABELS = "DrawLabels";
    public static final String KEY_LABEL_COLOR = "LabelColor";
    public static final String KEY_LABEL_FONT = "LabelFont";
    public static final String VALUE_LINE_STYLE_SOLID = "LineStyleSolid";
    public static final String VALUE_LINE_STYLE_DASHED = "LineStyleDashed";
    public static final String VALUE_LINE_STYLE_DOTTED = "LineStyleDotted";

    public GraticuleRenderingParams()
    {
    }

    public boolean isDrawLines()
    {
        Object value = getValue(KEY_DRAW_LINES);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    public void setDrawLines(boolean drawLines)
    {
        setValue(KEY_DRAW_LINES, drawLines);
    }

    public Color getLineColor()
    {
        Object value = getValue(KEY_LINE_COLOR);
        return value instanceof Color ? (Color) value : null;
    }

    public void setLineColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setValue(KEY_LINE_COLOR, color);
    }

    public double getLineWidth()
    {

        Object value = getValue(KEY_LINE_WIDTH);
        return value instanceof Double ? (Double) value : 0;
    }

    public void setLineWidth(double lineWidth)
    {
        setValue(KEY_LINE_WIDTH, lineWidth);
    }

    public String getLineStyle()
    {
        Object value = getValue(KEY_LINE_STYLE);
        return value instanceof String ? (String) value : null;
    }

    public void setLineStyle(String lineStyle)
    {
        if (lineStyle == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setValue(KEY_LINE_STYLE, lineStyle);
    }

    public boolean isDrawLabels()
    {
        Object value = getValue(KEY_DRAW_LABELS);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    public void setDrawLabels(boolean drawLabels)
    {
        setValue(KEY_DRAW_LABELS, drawLabels);
    }

    public Color getLabelColor()
    {
        Object value = getValue(KEY_LABEL_COLOR);
        return value instanceof Color ? (Color) value : null;
    }

    public void setLabelColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setValue(KEY_LABEL_COLOR, color);
    }

    public Font getLabelFont()
    {
        Object value = getValue(KEY_LABEL_FONT);
        return value instanceof Font ? (Font) value : null;
    }

    public void setLabelFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        setValue(KEY_LABEL_FONT, font);
    }
}
