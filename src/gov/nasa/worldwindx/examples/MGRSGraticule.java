/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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