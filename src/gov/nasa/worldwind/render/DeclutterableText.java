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

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.geom.*;

/**
 * A wrapper around {@link GeographicText} that allows provides participation in global text decluttering.
 *
 * @author tag
 * @version $Id: DeclutterableText.java 704 2012-07-21 03:16:21Z tgaskins $
 */
public class DeclutterableText implements Declutterable
{
    protected GeographicText text;
    protected Vec4 point;
    protected double eyeDistance;
    protected DeclutteringTextRenderer textRenderer;
    protected boolean enableDecluttering = true;
    protected Rectangle2D textBounds; // cached text bounds
    protected Font boundsFont; // font used by cached text bounds

    /**
     * Construct an object for specified text and position.
     *
     * @param text         the text to display.
     * @param point        the Cartesian location of the text.
     * @param eyeDistance  the distance to consider the text from the eye.
     * @param textRenderer the text renderer to use to draw the text.
     */
    DeclutterableText(GeographicText text, Vec4 point, double eyeDistance, DeclutteringTextRenderer textRenderer)
    {
        this.text = text;
        this.point = point;
        this.eyeDistance = eyeDistance;
        this.textRenderer = textRenderer;
    }

    /**
     * Indicates whether this text should participate in decluttering.
     *
     * @return true (the default) if it should participate, otherwise false.
     */
    public boolean isEnableDecluttering()
    {
        return this.enableDecluttering;
    }

    public double getDistanceFromEye()
    {
        return this.eyeDistance;
    }

    public GeographicText getText()
    {
        return text;
    }

    public Vec4 getPoint()
    {
        return point;
    }

    public Rectangle2D getBounds(DrawContext dc)
    {
        Font font = this.getText().getFont();
        if (font == null)
            font = this.textRenderer.getDefaultFont();

        if (this.textBounds != null && this.boundsFont == font)
            return this.textBounds;

        try
        {
            this.textBounds = this.textRenderer.computeTextBounds(dc, this);
            this.boundsFont = font;
        }
        catch (Exception e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
        }

        return this.textBounds;
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        try
        {
            if (this.getBounds(dc) == null)
                return;

            this.textRenderer.drawText(dc, this, 1, 1);
        }
        catch (Exception e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionWhileRenderingText", e);
        }
    }

    public void pick(DrawContext dc, java.awt.Point pickPoint)
    {
        // TODO
    }
}
