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

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * @author tag
 * @version $Id: EditorAnnotation.java 2306 2014-09-15 17:32:55Z tgaskins $
 */
public class EditorAnnotation extends ScreenAnnotation
{
    private Point tooltipOffset = new Point(5, 5);

    /**
     * Create a tool tip using specified text.
     *
     * @param text the text to display in the tool tip.
     */
    public EditorAnnotation(String text)
    {
        super(text, new Point(0, 0)); // (0,0) is a dummy; the actual point is determined when rendering

        this.initializeAttributes();
    }

    protected void initializeAttributes()
    {
        this.attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        this.attributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.attributes.setTextColor(Color.BLACK);
        this.attributes.setBackgroundColor(new Color(1f, 1f, 1f, 0.8f));
        this.attributes.setCornerRadius(5);
        this.attributes.setBorderColor(new Color(0xababab));
        this.attributes.setFont(Font.decode("Arial-PLAIN-12"));
        this.attributes.setTextAlign(AVKey.CENTER);
        this.attributes.setInsets(new Insets(5, 5, 5, 5));
    }

    protected int getOffsetX()
    {
        return this.tooltipOffset != null ? this.tooltipOffset.x : 0;
    }

    protected int getOffsetY()
    {
        return this.tooltipOffset != null ? this.tooltipOffset.y : 0;
    }

    @Override
    protected void doRenderNow(DrawContext dc)
    {
        this.getAttributes().setDrawOffset(
            new Point(this.getBounds(dc).width / 2 + this.getOffsetX(), this.getOffsetY()));
        this.setScreenPoint(this.getScreenPoint());

        super.doRenderNow(dc);
    }
}
