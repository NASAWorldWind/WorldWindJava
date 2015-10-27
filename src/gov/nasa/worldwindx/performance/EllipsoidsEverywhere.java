/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
Performance statistics for:
MacBook Pro (Windows 7)  -   2.8 Ghz  -  4 GB RAM  -  Intel Core i7 CPU  -  Nvidia GeForce GT 330

                Maximum Frame Rate (fps) :  VBOs / vertex arrays

                default view (19,000 Km)      view 1 (4,000 Km)        view 2 (1,000 Km)
Ellipsoids      / 6 fps                       / 23 fps                     / 30 fps
Boxes           5 fps / 7 fps                 10 fps / 23 fps              30 fps / 30 fps
Pyramids        5 fps / 7 fps                 12 fps / 26 fps              30 fps / 30 fps
Cylinders       4 fps / 6 fps                 11 fps / 23 fps              32 fps / 30 fps
Cones           5 fps / 6 fps                 11 fps / 25 fps              30 fps / 30 fps
Wedges          4 fps / 6 fps                 11 fps / 21 fps              30 fps / 30 fps


Dell Latitude 56400 XFR - 2.8 GHz - 3.48 GB - Intel Core2 Duo CPU - NVIDIA Quadro NVS 160M, 512 MB
Note: This machine was not connected to a network, so I did not have any imagery loaded at 4,000 Km and 1,000 Km.

                Maximum Frame Rate (fps) :  vertex arrays

                default view (19,000 Km)      view 1 (4,000 Km)        view 2 (1,00 Km)
Ellipsoid       2 fps                         16 fps                   22 fps
Box             2 fps                         15 fps                   22 fps
Pyramid         2 fps                         15 fps                   22 fps
Cylinder        3 fps                         16 fps                   22 fps
Cone            3 fps                         18 fps                   22 fps
Wedge           2 fps                         17 fps                   22 fps


Panasonic CF-30 - 1.6 GHz - 2 GB RAM - Intel Core2 Duo CPU - Mobile Intel 965 Express, 384 MB

                Maximum Frame Rate (fps) :  vertex arrays

                default view (19,000 Km)      view 1 (4,000 Km)        view 2 (1,00 Km)
Ellipsoid       1 fps                         10 fps                   14 fps
Box             1 fps                         9 fps                    17 fps
Pyramid         1 fps                         9 fps                    18 fps
Cylinder        1 fps                         9 fps                    18 fps
Cone            1 fps                         9 fps                    18 fps
Wedge           1 fps                         10 fps                   19 fps


~~~

5/20/2011  -  new multiple faces version

Performance statistics for:
MacBook Pro (Windows 7)  -   2.8 Ghz  -  4 GB RAM  -  Intel Core i7 CPU  -  Nvidia GeForce GT 330

                Maximum Frame Rate (fps) :  VBOs / vertex arrays

                default view (19,000 Km)      view 1 (4,000 Km)        view 2 (1,000 Km)
Ellipsoids      4 fps / 5 fps                 29 fps / 28 fps          60 fps / 59 fps
Boxes           3 fps / 4 fps                 20 fps / 28 fps          57 fps / 58 fps
Pyramids        3 fps / 5 fps                 20 fps / 28 fps          56 fps / 58 fps
Cylinders       3 fps / 5 fps                 23 fps / 28 fps          58 fps / 58 fps
Cones           4 fps / 5 fps                 26 fps / 28 fps          58 fps / 58 fps
Wedges          3 fps / 4 fps                 20 fps / 23 fps          57 fps / 57 fps

*/

package gov.nasa.worldwindx.performance;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

/**
 * @author tag
 * @version $Id: EllipsoidsEverywhere.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class EllipsoidsEverywhere extends ApplicationTemplate
{
    @SuppressWarnings("unchecked")
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        String shapeType = "Ellipsoid";

        public AppFrame()
        {
            super(true, true, false);

            //getWwd().getSceneController().getGLRuntimeCapabilities().setVertexBufferObjectEnabled(true);

            this.getControlPanel().add(makeShapesControlPanel(), BorderLayout.SOUTH);

            RenderableLayer layer = new RenderableLayer();
            //layer.setPickEnabled(true);
            insertBeforeCompass(getWwd(), layer);

            makeMany();
        }

        protected void makeMany()
        {
            int altitudeMode = WorldWind.ABSOLUTE;
            RigidShape shape;

            double minLat = -50, maxLat = 50, minLon = -140, maxLon = -10;
            double delta = 1.5;
            double intervals = 5;
            double dLat = 1 / intervals;
            double dLon = 1 / intervals;

            Position position;
            RenderableLayer layer = getLayer();

            int count = 0;
            for (double lat = minLat; lat <= maxLat; lat += delta)
            {
                for (double lon = minLon; lon <= maxLon; lon += delta)
                {
                    position = new Position(Angle.fromDegreesLatitude(lat),
                        Angle.fromDegreesLongitude(lon), 5e4);

                    if (shapeType.equalsIgnoreCase("ellipsoid"))
                        shape = new Ellipsoid(position, 50000, 10000, 50000);
                    else if (shapeType.equalsIgnoreCase("box"))
                        shape = new Box(position, 50000, 10000, 50000);
                    else if (shapeType.equalsIgnoreCase("pyramid"))
                        shape = new Pyramid(position, 50000, 10000, 50000);
                    else if (shapeType.equalsIgnoreCase("cylinder"))
                        shape = new Cylinder(position, 50000, 10000, 50000);
                    else if (shapeType.equalsIgnoreCase("cone"))
                        shape = new Cone(position, 50000, 10000, 50000);
                    else
                        shape = new Wedge(position, Angle.fromDegrees(227), 50000, 10000, 50000);

                    shape.setAltitudeMode(altitudeMode);
                    ShapeAttributes attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(false);
                    attrs.setInteriorMaterial(Material.RED);
                    attrs.setEnableLighting(true);
                    shape.setAttributes(attrs);
                    layer.addRenderable(shape);
                    ++count;
                }
            }
            System.out.printf("%d %s, Altitude mode = %s\n", count, shapeType,
                altitudeMode == WorldWind.RELATIVE_TO_GROUND ? "RELATIVE_TO_GROUND" : "ABSOLUTE");
        }

        protected JPanel makeShapesControlPanel()
        {
            JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Shape selection")));

            JPanel shapeSelectionPanel = new JPanel(new BorderLayout(0, 5));
            {
                String[] shapeStrings = {"Ellipsoid", "Box", "Pyramid", "Cylinder", "Cone", "Wedge"};

                //Create the combo box, select the item at index 4.
                //Indices start at 0, so 4 specifies the pig.
                JComboBox shapeList = new JComboBox(shapeStrings);
                shapeList.setSelectedIndex(0);
                shapeList.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        JComboBox cb = (JComboBox) e.getSource();
                        shapeType = (String) cb.getSelectedItem();
                        getLayer().removeAllRenderables();
                        makeMany();
                        getWwd().redraw();
                    }
                });
                shapeSelectionPanel.add(shapeList, BorderLayout.SOUTH);
            }

            JPanel elevationSliderPanel = new JPanel(new BorderLayout(0, 5));
            {
                int MIN = -10;
                int MAX = 10;
                int cur = 0;
                JSlider slider = new JSlider(MIN, MAX, cur);
                slider.setMajorTickSpacing(10);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
                labelTable.put(-10, new JLabel("-1.0"));
                labelTable.put(0, new JLabel("0.0"));
                labelTable.put(10, new JLabel("1.0"));
                slider.setLabelTable(labelTable);
                slider.setPaintLabels(true);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        double hint = ((JSlider) e.getSource()).getValue() / 10d;
                        getLayer().removeAllRenderables();
                        makeMany();
                        setDetailHint(hint);
                        getWwd().redraw();
                    }
                });
                elevationSliderPanel.add(slider, BorderLayout.SOUTH);
            }

            JPanel selectionPanel = new JPanel(new GridLayout(2, 0));
            selectionPanel.add(shapeSelectionPanel);

            JLabel detailHintLabel = new JLabel("Detail Hint:");

            JPanel sliderPanel = new JPanel(new GridLayout(2, 0));
            sliderPanel.add(elevationSliderPanel);

            controlPanel.add(selectionPanel, BorderLayout.NORTH);
            controlPanel.add(detailHintLabel, BorderLayout.CENTER);
            controlPanel.add(sliderPanel, BorderLayout.SOUTH);
            return controlPanel;
        }

        protected RenderableLayer getLayer()
        {
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer.getName().contains("Renderable"))
                {
                    return (RenderableLayer) layer;
                }
            }

            return null;
        }

        protected void setDetailHint(double hint)
        {
            for (Renderable renderable : getLayer().getRenderables())
            {

                if (shapeType.equalsIgnoreCase("ellipsoid"))
                {
                    Ellipsoid current;
                    current = (Ellipsoid) renderable;
                    current.setDetailHint(hint);
                }
                else if (shapeType.equalsIgnoreCase("cylinder"))
                {
                    Cylinder current;
                    current = (Cylinder) renderable;
                    current.setDetailHint(hint);
                }
                else if (shapeType.equalsIgnoreCase("cone"))
                {
                    Cone current;
                    current = (Cone) renderable;
                    current.setDetailHint(hint);
                }
                else if (shapeType.equalsIgnoreCase("wedge"))
                {
                    Wedge current;
                    current = (Wedge) renderable;
                    current.setDetailHint(hint);
                }
            }
            System.out.println("wedge detail hint set to " + hint);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Very Many Shapes", AppFrame.class);
    }
}
