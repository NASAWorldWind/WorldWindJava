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

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

/**
 * Panel that allows the user to input different coordinates and displays the corresponding latitude and longitude in
 * decimal degrees.
 * <p>
 * Supported format are:
 * </p>
 * <ul>
 * <li>MGRS strings with or without separating spaces.</li>
 * <li>Decimal degrees with sign prefix or N, S, E, W suffix.</li>
 * <li>Degrees, minutes and seconds with sign prefix or N, S, E, W suffix.</li>
 * </ul>
 * The separator between lat/lon pairs can be ',', ', ' or any number of spaces.
 * <p>
 * Examples:
 * </p>
 * <pre>
 * 11sku528111
 * 11S KU 528 111
 *
 * 45N 123W
 * +45.1234, -123.12
 * 45.1234N 123.12W
 *
 * 45 30 N 50 30 W
 * </pre>
 *
 * @author Patrick Murris
 * @version $Id: GoToCoordinatePanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class GoToCoordinatePanel extends JPanel
{
    private WorldWindow wwd;
    private JTextField coordInput;
    private JLabel resultLabel;

    public GoToCoordinatePanel(WorldWindow wwd)
    {
        super(new GridLayout(0, 1, 0, 0));
        this.wwd = wwd;
        this.makePanel();
    }

    private JPanel makePanel()
    {
        JPanel controlPanel = this;

        // Coord input
        JPanel coordPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        coordPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.coordInput = new JTextField(10);
        this.coordInput.setToolTipText("Type coordinates and press Enter");
        this.coordInput.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                LatLon latLon = null;
                try
                {
                    MGRSCoord coordinate = MGRSCoord.fromString(coordInput.getText(), wwd.getModel().getGlobe());
                    latLon = new LatLon(coordinate.getLatitude(), coordinate.getLongitude());
                }
                catch (Exception e)
                {
                    latLon = LatLon.parseLatLon(coordInput.getText());
                }
                updateResult(latLon);
            }
        });
        coordPanel.add(this.coordInput);

        // result panel
        JPanel resultPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.resultLabel = new JLabel();
        resultPanel.add(this.resultLabel);

        // goto button
        JPanel gotoPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        gotoPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JButton gotoButton = new JButton("Go to location");
        gotoButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                LatLon latLon = null;
                try
                {
                    MGRSCoord coordinate = MGRSCoord.fromString(coordInput.getText(), wwd.getModel().getGlobe());
                    latLon = new LatLon(coordinate.getLatitude(), coordinate.getLongitude());
                }
                catch (Exception e)
                {
                    latLon = LatLon.parseLatLon(coordInput.getText());
                }
                updateResult(latLon);
                if (latLon != null)
                {
                    View view = wwd.getView();
                    double distance = view.getCenterPoint().distanceTo3(view.getEyePoint());
                    view.goTo(new Position(latLon, 0), distance);

                }
            }
        });
        gotoPanel.add(gotoButton);

        controlPanel.add(coordPanel);
        controlPanel.add(resultPanel);
        controlPanel.add(gotoPanel);
        controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Go to")));
        return controlPanel;
    }

    private void updateResult(LatLon latLon)
    {
        if (latLon != null)
        {
            coordInput.setText(coordInput.getText().toUpperCase());
            resultLabel.setText(String.format("Lat %7.4f\u00B0 Lon %7.4f\u00B0",
                    latLon.getLatitude().degrees,  latLon.getLongitude().degrees));
        }
        else
            resultLabel.setText("Invalid coordinates");

    }
}
