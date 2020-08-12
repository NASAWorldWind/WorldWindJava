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

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.symbology.TacticalGraphicLabel;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.Arrays;

/**
 * Implementation of the Weapons Free Zone graphic (2.X.2.2.3.5).
 *
 * @author pabercrombie
 * @version $Id: WeaponsFreeZone.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WeaponsFreeZone extends AviationZone
{
    /** Path to the image used for the polygon fill pattern. */
    protected static final String DIAGONAL_FILL_PATH = "images/diagonal-fill-16x16.png";

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static java.util.List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_AVN_ARS_WFZ);
    }

    public WeaponsFreeZone(String sidc)
    {
        super(sidc);
    }

    /** {@inheritDoc} */
    @Override
    protected String getGraphicLabel()
    {
        return "WFZ";
    }

    @Override
    protected void createLabels()
    {
        TacticalGraphicLabel label = this.addLabel(this.createLabelText());
        label.setTextAlign(AVKey.LEFT);
        label.setEffect(AVKey.TEXT_EFFECT_NONE);
        label.setDrawInterior(true);
    }

    /** {@inheritDoc} */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        // Enable the polygon interior and set the image source to draw a fill pattern of diagonal lines.
        attributes.setDrawInterior(true);
        attributes.setImageSource(this.getImageSource());
    }

    /**
     * {@inheritDoc} Overridden to not include the altitude modifier in the label. This graphic does not support the
     * altitude modifier.
     */
    @Override
    protected String createLabelText()
    {
        return this.doCreateLabelText(false);
    }

    /**
     * Indicates the source of the image that provides the polygon fill pattern.
     *
     * @return The source of the polygon fill pattern.
     */
    protected Object getImageSource()
    {
        return DIAGONAL_FILL_PATH;
    }
}
