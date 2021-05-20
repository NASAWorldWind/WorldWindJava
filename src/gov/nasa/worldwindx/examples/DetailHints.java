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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.terrain.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.Hashtable;

/**
 * Illustrates how to control the detail of <code>{@link ElevationModel}</code> and <code>{@link TiledImageLayer}</code>
 * using their detail hint properties. Dragging the "Scene Detail" slider specifies the detail hint of the current
 * elevation model and tiled image layers.
 *
 * @author Patrick Murris
 * @version $Id: DetailHints.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class DetailHints extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            this.makeDetailHintControls();
        }

        protected void setElevationModelDetailHint(double detailHint)
        {
            this.setElevationModelDetailHint(getWwd().getModel().getGlobe().getElevationModel(), detailHint);
            System.out.println("Terrain detail hint set to " + detailHint);
        }

        protected void setElevationModelDetailHint(ElevationModel em, double detailHint)
        {
            if (em instanceof BasicElevationModel)
            {
                ((BasicElevationModel) em).setDetailHint(detailHint);
            }
            else if (em instanceof CompoundElevationModel)
            {
                for (ElevationModel m : ((CompoundElevationModel) em).getElevationModels())
                {
                    this.setElevationModelDetailHint(m, detailHint);
                }
            }
        }

        protected void setTiledImageLayerDetailHint(double detailHint)
        {
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer instanceof TiledImageLayer)
                {
                    ((TiledImageLayer) layer).setDetailHint(detailHint);
                }
            }

            System.out.println("Image detail hint set to " + detailHint);
        }

        protected void makeDetailHintControls()
        {
            Box vbox = Box.createVerticalBox();
            vbox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put(-10, new JLabel("-1.0"));
            labelTable.put(0, new JLabel("0.0"));
            labelTable.put(10, new JLabel("1.0"));

            JSlider elevationDetailSlider = new JSlider(-10, 10,
                (int) (this.getWwd().getModel().getGlobe().getElevationModel().getDetailHint(Sector.FULL_SPHERE) * 10));
            elevationDetailSlider.setMajorTickSpacing(5);
            elevationDetailSlider.setMinorTickSpacing(1);
            elevationDetailSlider.setPaintTicks(true);
            elevationDetailSlider.setPaintLabels(true);
            elevationDetailSlider.setLabelTable(labelTable);
            elevationDetailSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    double newDetailHint = ((JSlider) e.getSource()).getValue() / 10d;
                    setElevationModelDetailHint(newDetailHint);
                    getWwd().redraw();
                }
            });

            JLabel label = new JLabel("Terrain Detail");
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            elevationDetailSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
            vbox.add(label);
            vbox.add(elevationDetailSlider);

            JSlider imageDetailSlider = new JSlider(-10, 10, 0);
            imageDetailSlider.setMajorTickSpacing(5);
            imageDetailSlider.setMinorTickSpacing(1);
            imageDetailSlider.setPaintTicks(true);
            imageDetailSlider.setPaintLabels(true);
            imageDetailSlider.setLabelTable(labelTable);
            imageDetailSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    double detailHint = ((JSlider) e.getSource()).getValue() / 10d;
                    setTiledImageLayerDetailHint(detailHint);
                    getWwd().redraw();
                }
            });

            label = new JLabel("Image Detail");
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageDetailSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
            vbox.add(Box.createVerticalStrut(10));
            vbox.add(label);
            vbox.add(imageDetailSlider);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                new TitledBorder("Scene Detail")));
            panel.add(vbox, BorderLayout.CENTER);
            this.getControlPanel().add(panel, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Detail Hints", AppFrame.class);
    }
}
