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

import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.terrain.ZeroElevationModel;

import javax.swing.*;

/**
 * Shows how to use {@link ZeroElevationModel} to eliminate all elevations on the globe.
 *
 * @author tag
 * @version $Id: ElevationsAllZero.java 699 2012-07-13 17:53:47Z tgaskins $
 */
public class ElevationsAllZero
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected SurfaceImage surfaceImage;
        protected JSlider opacitySlider;

        public AppFrame()
        {
            super(true, true, false);

            // Eliminate elevations by simply setting the globe's elevation model to ZeroElevationModel.

            this.getWwd().getModel().getGlobe().setElevationModel(new ZeroElevationModel());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Zero Elevations", AppFrame.class);
    }
}
