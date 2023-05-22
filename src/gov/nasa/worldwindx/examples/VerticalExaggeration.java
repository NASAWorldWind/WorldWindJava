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

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class VerticalExaggeration extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        class VEInputPanel extends JPanel {

            private final WorldWindow wwd;
            private JTextField veInput;

            public VEInputPanel(WorldWindow wwd) {
                super(new GridLayout(0, 1, 0, 0));
                this.wwd = wwd;
                this.makePanel();
            }

            private JPanel makePanel() {
                JPanel controlPanel = this;

                JPanel vePanel = new JPanel(new GridLayout(0, 1, 0, 0));
                vePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                this.veInput = new JTextField(10);
                this.veInput.setToolTipText("Enter Vertical Exaggeration value and press Enter");
                this.veInput.setText(String.valueOf(wwd.getSceneController().getVerticalExaggeration()));
                this.veInput.addActionListener((ActionEvent event) -> {
                    double newVE = Double.parseDouble(this.veInput.getText());
                    this.wwd.getSceneController().setVerticalExaggeration(newVE);
                    this.wwd.redraw();
                });
                vePanel.add(this.veInput);

                JPanel veButtonPanel = new JPanel(new GridLayout(0, 1, 0, 0));
                veButtonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                JButton gotoButton = new JButton("Update Vertical Exaggeration");
                gotoButton.addActionListener((ActionEvent event) -> {
                    double newVE = Double.parseDouble(this.veInput.getText());
                    this.wwd.getSceneController().setVerticalExaggeration(newVE);
                    this.wwd.redraw();
                });
                veButtonPanel.add(gotoButton);

                controlPanel.add(vePanel);
                controlPanel.add(veButtonPanel);
                controlPanel.setBorder(
                        new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Vertical Exaggeration")));
                return controlPanel;
            }

        }

        public AppFrame() {
            super(true, true, false);

            // Add a dragger to enable shape dragging
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(Color.YELLOW));
            attrs.setOutlineWidth(2d);

            ShapeAttributes cylinderAttrs = new BasicShapeAttributes();
            cylinderAttrs.setInteriorMaterial(Material.YELLOW);
            cylinderAttrs.setDrawOutline(false);
            cylinderAttrs.setEnableLighting(true);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<>();
            double lat = 35.327;
            double lon = -111.677;
            double altitude = 1000;
            pathPositions.add(Position.fromDegrees(lat, lon - 0.1, altitude));
            pathPositions.add(Position.fromDegrees(lat, lon, altitude));
            pathPositions.add(Position.fromDegrees(lat, lon + 0.1, altitude));
            Path path = new Path(pathPositions);
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            path.setValue(AVKey.DISPLAY_NAME, "Yellow Path, relative to ground. Altitude: " + altitude + " meters.");
            layer.addRenderable(path);

            Cylinder cylinder = new Cylinder(Position.fromDegrees(lat, lon, altitude), 500, 500, 500);
            cylinder.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            cylinder.setAttributes(cylinderAttrs);
            cylinder.setVisible(true);
            cylinder.setValue(AVKey.DISPLAY_NAME, "Yellow cylinder, relative to ground. Altitude: " + altitude + " meters.");
            layer.addRenderable(cylinder);

            double polygonAltitude = 3000;
            ArrayList<Position> boundaries = new ArrayList<>();
            boundaries.add(Position.fromDegrees(lat, lon - 0.05, polygonAltitude));
            boundaries.add(Position.fromDegrees(lat, lon + 0.05, polygonAltitude));
            boundaries.add(Position.fromDegrees(lat + 0.05, lon, polygonAltitude));

            Polygon polygon = new Polygon(boundaries);
            polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            polygon.setValue(AVKey.DISPLAY_NAME, "Yellow polygon, relative to ground. Altitude: " + polygonAltitude + " meters.");

            ShapeAttributes polygonAttributes = new BasicShapeAttributes();
            polygonAttributes.setDrawInterior(true);
            polygonAttributes.setDrawOutline(true);
            polygonAttributes.setOutlineMaterial(Material.YELLOW);
            polygonAttributes.setInteriorMaterial(Material.YELLOW);
            polygonAttributes.setEnableLighting(true);
            polygon.setAttributes(polygonAttributes);

            layer.addRenderable(polygon);

            attrs = new BasicShapeAttributes(attrs);
            attrs.setOutlineMaterial(new Material(Color.RED));
            pathPositions = new ArrayList<>();
            lat -= 0.01;
            pathPositions.add(Position.fromDegrees(lat, lon - 0.1, altitude));
            pathPositions.add(Position.fromDegrees(lat, lon, altitude));
            pathPositions.add(Position.fromDegrees(lat, lon + 0.1, altitude));
            path = new Path(pathPositions);
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            path.setValue(AVKey.DISPLAY_NAME, "Red Path, clamp to ground. Altitude: " + altitude + " meters.");
            layer.addRenderable(path);

            cylinderAttrs = new BasicShapeAttributes(cylinderAttrs);
            cylinderAttrs.setInteriorMaterial(Material.RED);

            cylinder = new Cylinder(Position.fromDegrees(lat, lon, altitude), 500, 500, 500);
            cylinder.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            cylinder.setAttributes(cylinderAttrs);
            cylinder.setVisible(true);
            cylinder.setValue(AVKey.DISPLAY_NAME, "Red cylinder, clamp to ground. Altitude: " + altitude + " meters.");
            layer.addRenderable(cylinder);

            polygonAltitude+= 500;
            boundaries = new ArrayList<>();
            boundaries.add(Position.fromDegrees(lat, lon - 0.2, polygonAltitude));
            boundaries.add(Position.fromDegrees(lat, lon + 0.2, polygonAltitude));
            boundaries.add(Position.fromDegrees(lat + 0.2, lon, polygonAltitude));

            polygon = new Polygon(boundaries);
            polygon.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            polygon.setValue(AVKey.DISPLAY_NAME, "Red polygon, clamp to ground. Altitude: " + polygonAltitude + " meters.");

            polygonAttributes = new BasicShapeAttributes(polygonAttributes);
            polygonAttributes.setOutlineMaterial(Material.RED);
            polygonAttributes.setInteriorMaterial(Material.RED);
            polygon.setAttributes(polygonAttributes);

            layer.addRenderable(polygon);

            attrs = new BasicShapeAttributes(attrs);
            attrs.setOutlineMaterial(new Material(Color.MAGENTA));
            pathPositions = new ArrayList<>();
            lat += 0.02;
            altitude = 5000;
            pathPositions.add(Position.fromDegrees(lat, lon - 0.1, altitude));
            pathPositions.add(Position.fromDegrees(lat, lon, altitude));
            pathPositions.add(Position.fromDegrees(lat, lon + 0.1, altitude));
            path = new Path(pathPositions);
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.ABSOLUTE);
            path.setPathType(AVKey.GREAT_CIRCLE);
            path.setValue(AVKey.DISPLAY_NAME, "Magenta Path, absolute altitude mode. Altitude: " + altitude + " meters.");
            layer.addRenderable(path);

            cylinderAttrs = new BasicShapeAttributes(cylinderAttrs);
            cylinderAttrs.setInteriorMaterial(Material.MAGENTA);

            cylinder = new Cylinder(Position.fromDegrees(lat, lon, altitude), 500, 500, 500);
            cylinder.setAltitudeMode(WorldWind.ABSOLUTE);
            cylinder.setAttributes(cylinderAttrs);
            cylinder.setVisible(true);
            cylinder.setValue(AVKey.DISPLAY_NAME, "Magenta cylinder, absolute altitude mode. Altitude: " + altitude + " meters.");
            layer.addRenderable(cylinder);
            
            polygonAltitude+= 500;
            boundaries = new ArrayList<>();
            boundaries.add(Position.fromDegrees(lat, lon - 0.05, polygonAltitude));
            boundaries.add(Position.fromDegrees(lat, lon + 0.05, polygonAltitude));
            boundaries.add(Position.fromDegrees(lat + 0.05, lon, polygonAltitude));

            polygon = new Polygon(boundaries);
            polygon.setAltitudeMode(WorldWind.ABSOLUTE);
            polygon.setValue(AVKey.DISPLAY_NAME, "Magenta polygon, absolute altitude mode. Altitude: " + polygonAltitude + " meters.");

            polygonAttributes = new BasicShapeAttributes(polygonAttributes);
            polygonAttributes.setOutlineMaterial(Material.MAGENTA);
            polygonAttributes.setInteriorMaterial(Material.MAGENTA);
            polygon.setAttributes(polygonAttributes);

            layer.addRenderable(polygon);

            insertBeforeCompass(getWwd(), layer);
            View view = this.getWwd().getView();
            view.setEyePosition(Position.fromDegrees(35.4, -111.669, 20000));
            view.setPitch(Angle.fromDegrees(70));
            this.getControlPanel().add(new VEInputPanel(this.getWwd()), BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Paths", AppFrame.class);
    }
}
