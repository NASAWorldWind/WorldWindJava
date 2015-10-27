/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
        ApplicationTemplate.start("World Wind Detail Hints", AppFrame.class);
    }
}
