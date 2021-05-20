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
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: RandomShapeAttributes.java 2326 2014-09-17 22:35:45Z dcollins $
 */
public class RandomShapeAttributes
{
    protected int attrIndex = -1;
    protected PointPlacemarkAttributes[] pointAttrs;
    protected ShapeAttributes[] shapeAttrs;
    protected AirspaceAttributes[] airspaceAttrs;

    public RandomShapeAttributes()
    {
        this.initialize();
    }

    protected void initialize()
    {
        Color[] shapeColors = {
            new Color(255, 9, 84), // red
            new Color(255, 133, 0), // orange
            new Color(255, 198, 0), // yellow
            new Color(79, 213, 33), // green
            new Color(7, 152, 249), // blue
            new Color(193, 83, 220), // purple
        };

        this.pointAttrs = new PointPlacemarkAttributes[shapeColors.length];
        this.shapeAttrs = new ShapeAttributes[shapeColors.length];
        this.airspaceAttrs = new AirspaceAttributes[shapeColors.length];

        for (int i = 0; i < shapeColors.length; i++)
        {
            this.pointAttrs[i] = this.createPointAttributes(shapeColors[i]);
            this.shapeAttrs[i] = this.createShapeAttributes(shapeColors[i]);
            this.airspaceAttrs[i] = this.createAirspaceAttributes(shapeColors[i]);
        }
    }

    public RandomShapeAttributes nextAttributes()
    {
        this.attrIndex++;

        return this;
    }

    public PointPlacemarkAttributes asPointAttributes()
    {
        return this.pointAttrs[this.attrIndex % this.pointAttrs.length];
    }

    public ShapeAttributes asShapeAttributes()
    {
        return this.shapeAttrs[this.attrIndex % this.shapeAttrs.length];
    }

    public AirspaceAttributes asAirspaceAttributes()
    {
        return this.airspaceAttrs[this.attrIndex % this.airspaceAttrs.length];
    }

    protected PointPlacemarkAttributes createPointAttributes(Color color)
    {
        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
        attrs.setUsePointAsDefaultImage(true);
        attrs.setLineMaterial(new Material(color));
        attrs.setScale(7d);
        return attrs;
    }

    protected ShapeAttributes createShapeAttributes(Color color)
    {
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(new Material(color));
        attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(color)));
        attrs.setInteriorOpacity(0.5);
        attrs.setOutlineWidth(2);
        return attrs;
    }

    protected AirspaceAttributes createAirspaceAttributes(Color color)
    {
        AirspaceAttributes attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(new Material(color));
        attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(color)));
        attrs.setInteriorOpacity(0.7);
        attrs.setOutlineWidth(2);
        attrs.setDrawOutline(true);
        attrs.setEnableAntialiasing(true);
        attrs.setEnableLighting(true);
        return attrs;
    }
}
