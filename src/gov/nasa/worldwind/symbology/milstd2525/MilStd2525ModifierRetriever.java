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

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.image.*;

/**
 * Icon retriever to retriever modifiers (for example, the Feint/Dummy indicator) for the symbols in the MIL-STD-2525C
 * symbol set.
 *
 * @author dcollins
 * @version $Id: MilStd2525ModifierRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MilStd2525ModifierRetriever extends AbstractIconRetriever
{
    protected static final Color DEFAULT_COLOR = Color.BLACK;

    protected static final String PATH_PREFIX = "modifiers";
    protected static final String PATH_SUFFIX = ".png";
    protected static final int[] variableWidths = {88, 93, 114, 119};

    /**
     * Create a new retriever that will retrieve icons from the specified location.
     *
     * @param retrieverPath File path or URL to the symbol directory, for example "http://myserver.com/milstd2525/".
     */
    public MilStd2525ModifierRetriever(String retrieverPath)
    {
        super(retrieverPath);
    }

    /**
     * Create an icon for a symbol modifier.
     *
     * @param symbolId Identifier for the modifier. This identifier is fields 11 and 12 of a MIL-STD-2525C SIDC (see
     *                 MIL-STD-2525C Table A-I, pg. 51).
     * @param params   Parameters that affect icon retrieval. This retriever accepts only one parameter: AVKey.COLOR,
     *                 which determines the color of the modifier (default is black).
     *
     * @return BufferedImage containing the requested modifier, or null if the modifier cannot be retrieved.
     */
    public BufferedImage createIcon(String symbolId, AVList params)
    {
        if (symbolId == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compose a path from the modifier code and value.
        String path = this.composePath(symbolId, params);
        if (path == null)
        {
            String msg = Logging.getMessage("Symbology.SymbolIconNotFound", symbolId);
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        BufferedImage image = this.readImage(path);
        if (image == null)
        {
            String msg = Logging.getMessage("Symbology.SymbolIconNotFound", symbolId);
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        if (this.mustApplyColor(symbolId))
        {
            // Apply the correct color the modifier.
            Color color = this.getColorFromParams(params);
            if (color == null)
                color = DEFAULT_COLOR;
            this.multiply(image, color);
        }

        return image;
    }

    protected String composePath(String symbolModifierCode, AVList params)
    {
        AVList modifierParams = SymbolCode.parseSymbolModifierCode(symbolModifierCode, null);
        if (modifierParams == null)
            return null;

        if (params != null)
            modifierParams.setValues(params);

        StringBuilder sb = new StringBuilder();
        sb.append(PATH_PREFIX).append("/");
        sb.append(symbolModifierCode.toLowerCase());

        if (this.isVariableWidth(modifierParams))
        {
            Integer i = this.chooseBestFittingWidth(modifierParams);
            if (i != null)
                sb.append("_").append(i);
        }

        sb.append(PATH_SUFFIX);
        return sb.toString();
    }

    protected boolean isVariableWidth(AVList params)
    {
        return params.hasKey(SymbologyConstants.FEINT_DUMMY)
            || params.hasKey(SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE);
    }

    /**
     * Indicates whether or not color must be applied to the modifier. Color is applied to all modifiers except for
     * alternate Operational Condition modifiers, which have their own color.
     *
     * @param symbolId Modifier id.
     *
     * @return True if color must be applied to the modifier.
     */
    protected boolean mustApplyColor(String symbolId)
    {
        return !SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE_ALL.contains(symbolId.toUpperCase());
    }

    protected Integer chooseBestFittingWidth(AVList params)
    {
        Object o = params.getValue(AVKey.WIDTH);
        if (o == null || !(o instanceof Number))
            return null;

        int value = ((Number) o).intValue();
        int width = variableWidths[0];
        int minDiff = Math.abs(value - width);

        for (int i = 1; i < variableWidths.length; i++)
        {
            int diff = Math.abs(value - variableWidths[i]);
            if (diff < minDiff)
            {
                width = variableWidths[i];
                minDiff = diff;
            }
        }

        return width;
    }

    /**
     * Retrieves the value of the AVKey.COLOR parameter.
     *
     * @param params Parameter list.
     *
     * @return The value of the AVKey.COLOR parameter, if such a parameter exists and is of type java.awt.Color. Returns
     *         null if the parameter list is null, if there is no value for key AVKey.COLOR, or if the value is not a
     *         Color.
     */
    protected Color getColorFromParams(AVList params)
    {
        if (params == null)
            return null;

        Object o = params.getValue(AVKey.COLOR);
        return (o instanceof Color) ? (Color) o : null;
    }
}
