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

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.util.measure.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.beans.*;
import java.util.ArrayList;

/**
 * Example usage of MeasureTool to draw a shape on the globe and measure length, area, etc. Click the "New" button, and
 * then click and drag on the globe to define a shape. The panel on the left shows the shape's measurement.
 *
 * @author Patrick Murris
 * @version $Id: MeasureToolUsage.java 2117 2014-07-01 20:36:49Z tgaskins $
 * @see gov.nasa.worldwind.util.measure.MeasureTool
 * @see gov.nasa.worldwind.util.measure.MeasureToolController
 * @see MeasureToolPanel
 */
public class MeasureToolUsage extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private int lastTabIndex = -1;
        private final JTabbedPane tabbedPane = new JTabbedPane();
        private final TerrainProfileLayer profile = new TerrainProfileLayer();
        private final PropertyChangeListener measureToolListener = new MeasureToolListener();

        public AppFrame() {
            super(true, true, false); // no layer or statistics panel

            // Add terrain profile layer
            profile.setEventSource(getWwd());
            profile.setFollow(TerrainProfileLayer.FOLLOW_PATH);
            profile.setShowProfileLine(false);
            insertBeforePlacenames(getWwd(), profile);

            // Add + tab
            tabbedPane.add(new JPanel());
            tabbedPane.setTitleAt(0, "+");
            tabbedPane.addChangeListener((ChangeEvent changeEvent) -> {
                if (tabbedPane.getSelectedIndex() == 0) {
                    // Add new measure tool in a tab when '+' selected
                    MeasureTool measureTool = new MeasureTool(getWwd());
                    measureTool.setController(new MeasureToolController());
                    tabbedPane.add(new MeasureToolPanel(getWwd(), measureTool));
                    tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "" + (tabbedPane.getTabCount() - 1));
                    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                    switchMeasureTool();
                } else {
                    switchMeasureTool();
                }
            });

            // Add measure tool control panel to tabbed pane
            MeasureTool measureTool = new MeasureTool(this.getWwd());
            measureTool.setController(new MeasureToolController());
            tabbedPane.add(new MeasureToolPanel(this.getWwd(), measureTool));
            tabbedPane.setTitleAt(1, "1");
            tabbedPane.setSelectedIndex(1);
            switchMeasureTool();

            this.getControlPanel().add(tabbedPane, BorderLayout.EAST);
            this.pack();
        }

        private class MeasureToolListener implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                // Measure shape position list changed - update terrain profile
                if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                        || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                        || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE)) {
                    updateProfile(((MeasureTool) event.getSource()));
                }
            }
        }

        private void switchMeasureTool() {
            // Disarm last measure tool when changing tab and switching tool
            if (lastTabIndex != -1) {
                MeasureTool mt = ((MeasureToolPanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
                mt.setArmed(false);
                mt.removePropertyChangeListener(measureToolListener);
            }
            // Update terrain profile from current measure tool
            lastTabIndex = tabbedPane.getSelectedIndex();
            MeasureTool mt = ((MeasureToolPanel) tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
            mt.addPropertyChangeListener(measureToolListener);
            updateProfile(mt);
        }

        private void updateProfile(MeasureTool mt) {
            ArrayList<? extends LatLon> positions = mt.getPositions();
            if (positions != null && positions.size() > 1) {
                profile.setPathPositions(positions);
                profile.setEnabled(true);
            } else {
                profile.setEnabled(false);
            }

            getWwd().redraw();
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Measure Tool", MeasureToolUsage.AppFrame.class);
    }

}
