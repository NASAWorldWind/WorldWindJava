/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;

/**
 * Illustrates how to display contour lines in World Wind on the surface terrain at a specified elevation. This uses the
 * class <code>{@link ContourLine}</code> to compute and display the contour lines.
 *
 * @author Patrick Murris
 * @version $Id: ContourLines.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ContourLines extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ContourLine contourLine;

        public AppFrame()
        {
            // Create a layer to display the contour lines.
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Contour Lines");
            layer.setPickEnabled(false);

            // Add the contour line layer to the World Window and update the layer panel.
            insertBeforePlacenames(getWwd(), layer);

            // Add a global moving contour line to the layer.
            this.contourLine = new ContourLine();
            this.contourLine.setElevation(2125);
            layer.addRenderable(this.contourLine);

            // Add a local contour line to the layer.
            ArrayList<LatLon> positions = new ArrayList<LatLon>();
            positions.add(LatLon.fromDegrees(44.16, 6.82));
            positions.add(LatLon.fromDegrees(44.16, 7.09));
            positions.add(LatLon.fromDegrees(44.30, 6.95));
            positions.add(LatLon.fromDegrees(44.16, 6.82));

            for (int elevation = 0; elevation <= 3000; elevation += 250)
            {
                ContourLinePolygon cl = new ContourLinePolygon(elevation, positions);
                cl.setColor(new Color(0.2f, 0.2f, 0.8f));

                if (elevation % 1000 == 0)
                {
                    cl.setLineWidth(2);
                    cl.setColor(new Color(0.0f, 0.1f, 0.6f));
                }

                if (elevation % 500 == 0)
                    cl.setLineWidth(2);

                layer.addRenderable(cl);
            }

            // Add a contour line control panel to the application window.
            this.getControlPanel().add(this.makeContourLineControlPanel(), BorderLayout.SOUTH);
        }

        protected JPanel makeContourLineControlPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Contour Line Elevation")));

            final JSlider slider = new JSlider(0, 3000, (int) this.contourLine.getElevation());
            slider.setMajorTickSpacing(1000);
            slider.setMinorTickSpacing(250);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    contourLine.setElevation(slider.getValue());
                    getWwd().redraw();
                }
            });
            controlPanel.add(slider);

            Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
            labels.put(0, new JLabel("0km"));
            labels.put(1000, new JLabel("1km"));
            labels.put(2000, new JLabel("2km"));
            labels.put(3000, new JLabel("3km"));
            slider.setLabelTable(labels);

            return controlPanel;
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 44.23);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 6.92);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 30000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 45);

        ApplicationTemplate.start("World Wind Contour Lines", AppFrame.class);
    }
}
