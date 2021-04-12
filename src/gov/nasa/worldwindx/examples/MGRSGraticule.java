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

import gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the globe with a MGRS/UTM graticule. The graticule is its own layer and can be turned on and off independent
 * of other layers. As the view zooms in, the graticule adjusts to display a finer grid. The example provides controls
 * to customize the color and opacity of the grid.
 *
 * @author Patrick Murris
 * @version $Id: MGRSGraticule.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class MGRSGraticule extends ApplicationTemplate
{

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            MGRSGraticuleLayer layer = new MGRSGraticuleLayer();

            // Add MGRS/UTM Graticule layer
            insertBeforePlacenames(this.getWwd(), layer);

            // Replace status bar with MGRS version
            this.getStatusBar().setEventSource(null);
            this.getWwjPanel().remove(this.getStatusBar());
            StatusBar sb = new StatusBarMGRS();
            sb.setEventSource(this.getWwd());
            this.getWwjPanel().add(sb, BorderLayout.SOUTH);

            // Add go to coordinate input panel
            this.getControlPanel().add(new GoToCoordinatePanel(this.getWwd()),  BorderLayout.SOUTH);

            // Add MGRS graticule properties frame
            JDialog dialog = MGRSAttributesPanel.showDialog(this, "MGRS Graticule Properties", layer);
            Rectangle bounds = this.getBounds();
            dialog.setLocation(bounds.x + bounds.width, bounds.y);  
        }
    }


    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind UTM/MGRS Graticule", AppFrame.class);
    }
}