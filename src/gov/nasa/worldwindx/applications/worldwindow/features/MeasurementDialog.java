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

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * @author tag
 * @version $Id: MeasurementDialog.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MeasurementDialog extends AbstractFeatureDialog
{
    private static final String LAYER_NAME = "Measurement";

    private JTabbedPane tabbedPane = new JTabbedPane();
    private int labelSequence = 0;
    private RenderableLayer shapeLayer;
    private RenderableLayer controlPointsLayer;

    private static int nextColor = 0;
    private static Color[] colors = new Color[]
        {
            Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK, Color.YELLOW
        };

    private static Color getNextColor()
    {
        return colors[nextColor++ % colors.length];
    }

    public MeasurementDialog(Registry registry)
    {
        super("Measurement", Constants.FEATURE_MEASUREMENT_DIALOG, registry);
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        this.shapeLayer = new RenderableLayer();
        this.shapeLayer.setName(LAYER_NAME);
        this.controller.addInternalActiveLayer(shapeLayer);

        this.controlPointsLayer = this.shapeLayer; // use same layer for both in MeasureTool

        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.setOpaque(false);

        this.tabbedPane.add(new JPanel());
        this.tabbedPane.setTitleAt(0, "+");
        this.tabbedPane.setToolTipTextAt(0, "Create measurement");

        this.tabbedPane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent changeEvent)
            {
                if (tabbedPane.getSelectedIndex() == 0)
                {
                    addNewPanel(tabbedPane); // Add new panel when '+' is selected
                }
            }
        });

        // Add an initial measure panel to tabbed pane
        this.addNewPanel(this.tabbedPane);
        tabbedPane.setSelectedIndex(1);

        this.setTaskComponent(this.tabbedPane);

        this.setLocation(SwingConstants.WEST, SwingConstants.NORTH);
        this.getJDialog().setResizable(true);

        JButton deleteButton = new JButton(
            new ImageIcon(
                this.getClass().getResource("/gov/nasa/worldwindx/applications/worldwindow/images/delete-20x20.png")));
        deleteButton.setToolTipText("Remove current measurement");
        deleteButton.setOpaque(false);
        deleteButton.setBackground(new Color(0, 0, 0, 0));
        deleteButton.setBorderPainted(false);
        deleteButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteCurrentPanel();
                controller.redraw();
            }
        });
        deleteButton.setEnabled(true);
        this.insertLeftDialogComponent(deleteButton);
    }

    @Override
    public void setVisible(boolean tf)
    {
        // Hide the shape layer if it's empty when the dialog closes. There will be fewer than 3 renderables in that
        // case: the control points and the annotation.
        if (!tf && countRenderables(this.shapeLayer) < 3)
        {
            this.controller.getActiveLayers().remove(this.shapeLayer);
        }

        // Un-hide the shape layer when the dialog is raised
        if (tf)
        {
            if (!this.controller.getActiveLayers().contains(this.shapeLayer))
                this.controller.addInternalActiveLayer(this.shapeLayer);
        }

        super.setVisible(tf);
    }

    private int countRenderables(RenderableLayer layer)
    {
        int count = 0;

        //noinspection UnusedDeclaration
        for (Renderable r : layer.getRenderables())
        {
            ++count;
        }

        return count;
    }

    private void deleteCurrentPanel()
    {
        MeasurementPanel mp = getCurrentPanel();
        if (tabbedPane.getTabCount() > 2)
        {
            mp.deletePanel();
            tabbedPane.remove(tabbedPane.getSelectedComponent());
        }
        else
        {
            mp.clearPanel();
        }
    }

    private void addNewPanel(JTabbedPane tabPane)
    {
        final MeasurementPanel mp = new MeasurementPanel(null);
        mp.initialize(this.controller);
        mp.setLayers(this.shapeLayer, this.controlPointsLayer);

        Color color = getNextColor();
        mp.setLineColor(color);
        mp.setFillColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 32));

        tabPane.addTab("" + ++this.labelSequence, makeColorCircle(color), mp.getJPanel());
        tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
        tabPane.setToolTipTextAt(tabbedPane.getSelectedIndex(), "Select measurement");

        this.controller.getWWd().addSelectListener(new SelectListener()
        {
            public void selected(SelectEvent event)
            {
                if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                {
                    if (mp.getShape() == null || mp.getShape() != event.getTopObject())
                        return;

                    for (Component c : tabbedPane.getComponents())
                    {
                        if (!(c instanceof JComponent))
                            continue;

                        Object o = ((JComponent) c).getClientProperty(Constants.FEATURE_OWNER_PROPERTY);
                        if (o instanceof MeasurementPanel && o == mp)
                        {
                            tabbedPane.setSelectedComponent(c);
                        }
                    }
                }
            }
        });
    }

    private MeasurementPanel getCurrentPanel()
    {
        JComponent p = (JComponent) tabbedPane.getSelectedComponent();
        return (MeasurementPanel) p.getClientProperty(Constants.FEATURE_OWNER_PROPERTY);
    }

    private static Icon makeColorCircle(Color color)
    {
        BufferedImage bi = PatternFactory.createPattern(
            PatternFactory.PATTERN_CIRCLE, new Dimension(16, 16), .9f, color);

        return new ImageIcon(bi);
    }
}
