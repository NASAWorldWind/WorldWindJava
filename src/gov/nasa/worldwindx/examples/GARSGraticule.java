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
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.layers.GARSGraticuleLayer;

import java.awt.*;

/**
 * Displays the globe with a GARS graticule. The graticule is its own layer and can be turned on and off independent
 * of other layers. As the view zooms in, the graticule adjusts to display a finer grid. The example provides controls
 * to customize the color and opacity of the grid.
 *
 * @version $Id: GARSGraticule.java 2385 2014-10-14 21:56:07Z tgaskins $
 */
public class GARSGraticule extends ApplicationTemplate
{

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            GARSGraticuleLayer layer = new GARSGraticuleLayer();

            layer.setGraticuleLineColor(Color.WHITE, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_0);
            layer.setGraticuleLineColor(Color.YELLOW, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_1);
            layer.setGraticuleLineColor(Color.GREEN, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_2);
            layer.setGraticuleLineColor(Color.CYAN, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_3);

            layer.set30MinuteThreshold(1200e3);
            layer.set15MinuteThreshold(600e3);
            layer.set5MinuteThreshold(180e3);

            insertBeforePlacenames(this.getWwd(), layer);
        }
    }


    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind GARS Graticule", AppFrame.class);
    }
}