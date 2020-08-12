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

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.terrain.BathymetryFilterElevationModel;

/**
 * Illustrates how to suppress the WorldWind <code>{@link gov.nasa.worldwind.globes.Globe}'s</code> bathymetry
 * (elevations below mean sea level) by using a <code>{@link BathymetryFilterElevationModel}</code>.
 *
 * @author tag
 * @version $Id: BathymetryRemoval.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BathymetryRemoval extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            // Get the current elevation model.
            ElevationModel currentElevationModel = this.getWwd().getModel().getGlobe().getElevationModel();

            // Wrap it with the no-bathymetry elevation model.
            BathymetryFilterElevationModel noDepthModel = new BathymetryFilterElevationModel(currentElevationModel);

            // Have the globe use the no-bathymetry elevation model.
            this.getWwd().getModel().getGlobe().setElevationModel(noDepthModel);

            // Increase vertical exaggeration to make it clear that bathymetry is suppressed.
            this.getWwd().getSceneController().setVerticalExaggeration(5d);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Bathymetry Removal", AppFrame.class);
    }
}
