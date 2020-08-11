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
package gov.nasa.worldwindx.examples.analytics;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: AnalyticSurfaceAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AnalyticSurfaceAttributes
{
    protected boolean drawInterior = true;
    protected boolean drawOutline = true;
    protected boolean drawShadow = true;
    protected Material interiorMaterial = Material.GRAY;
    protected Material outlineMaterial = Material.WHITE;
    protected double interiorOpacity = 1d;
    protected double outlineOpacity = 1d;
    protected double shadowOpacity = 1d;
    protected double outlineWidth = 1d;

    public AnalyticSurfaceAttributes(Material material, double opacity)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorMaterial = material;
        this.interiorOpacity = opacity;
    }

    public AnalyticSurfaceAttributes()
    {
        this(Material.GRAY, 1d);
    }

    public AnalyticSurfaceAttributes(AnalyticSurfaceAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.drawInterior = attributes.isDrawInterior();
        this.drawOutline = attributes.isDrawOutline();
        this.drawShadow = attributes.isDrawShadow();
        this.interiorMaterial = attributes.getInteriorMaterial();
        this.outlineMaterial = attributes.getOutlineMaterial();
        this.interiorOpacity = attributes.getInteriorOpacity();
        this.outlineOpacity = attributes.getOutlineOpacity();
        this.shadowOpacity = attributes.getShadowOpacity();
        this.outlineWidth = attributes.getOutlineWidth();
    }

    public AnalyticSurfaceAttributes copy()
    {
        return new AnalyticSurfaceAttributes(this);
    }

    public boolean isDrawInterior()
    {
        return this.drawInterior;
    }

    public void setDrawInterior(boolean draw)
    {
        this.drawInterior = draw;
    }

    public boolean isDrawOutline()
    {
        return this.drawOutline;
    }

    public void setDrawOutline(boolean draw)
    {
        this.drawOutline = draw;
    }

    public boolean isDrawShadow()
    {
        return this.drawShadow;
    }

    public void setDrawShadow(boolean draw)
    {
        this.drawShadow = draw;
    }

    public Material getInteriorMaterial()
    {
        return this.interiorMaterial;
    }

    public void setInteriorMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorMaterial = material;
    }

    public Material getOutlineMaterial()
    {
        return this.outlineMaterial;
    }

    public void setOutlineMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineMaterial = material;
    }

    public double getInteriorOpacity()
    {
        return this.interiorOpacity;
    }

    public void setInteriorOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorOpacity = opacity;
    }

    public double getOutlineOpacity()
    {
        return this.outlineOpacity;
    }

    public void setOutlineOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineOpacity = opacity;
    }

    public double getShadowOpacity()
    {
        return this.shadowOpacity;
    }

    public void setShadowOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.shadowOpacity = opacity;
    }

    public double getOutlineWidth()
    {
        return this.outlineWidth;
    }

    public void setOutlineWidth(double width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.LineWidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineWidth = width;
    }
}
