/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * This example demonstrates how to 'look around' a scene by controlling the view's pitch, heading, roll and field of
 * view, in this case by using a simple set of sliders.
 *
 * @author Patrick Murris
 * @version $Id: ViewLookAround.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ViewLookAround extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private ViewControlPanel vcp;

        public AppFrame()
        {
            super(true, true, false);

            // Add view control panel to the layer panel
            this.vcp = new ViewControlPanel(getWwd());
            getWwd().setView(new BasicFlyView());
            this.getControlPanel().add(this.vcp, BorderLayout.SOUTH);
            Position pos = new Position(new LatLon(Angle.fromDegrees(45), Angle.fromDegrees(-120)), 2000);
            getWwd().getView().setEyePosition(pos);
        }

        private class ViewControlPanel extends JPanel
        {
            private WorldWindow wwd;
            private JSlider pitchSlider;
            private JSlider headingSlider;
            private JSlider rollSlider;
            private JSlider fovSlider;

            private boolean suspendEvents = false;

            public ViewControlPanel(WorldWindow wwd)
            {
                this.wwd = wwd;
                // Add view property listener
                this.wwd.getView().addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                    {
                        update();
                    }
                });

                // Compose panel
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                insertBeforeCompass(getWwd(), new CrosshairLayer());

                // Pitch slider
                JPanel pitchPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                pitchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                pitchPanel.add(new JLabel("Pitch:"));
                pitchSlider = new JSlider(0, 180, 90);
                pitchSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent changeEvent)
                    {
                        updateView();
                    }
                });
                pitchPanel.add(pitchSlider);

                // Heading slider
                JPanel headingPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                headingPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                headingPanel.add(new JLabel("Heading:"));
                headingSlider = new JSlider(-180, 180, 0);
                headingSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent changeEvent)
                    {
                        updateView();
                    }
                });
                headingPanel.add(headingSlider);

                // Roll slider
                JPanel rollPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                rollPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                rollPanel.add(new JLabel("Roll:"));
                rollSlider = new JSlider(-180, 180, 0);
                rollSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent changeEvent)
                    {
                        updateView();
                    }
                });
                rollPanel.add(rollSlider);

                // Field of view slider
                JPanel fovPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                fovPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                fovPanel.add(new JLabel("Field of view:"));
                fovSlider = new JSlider(10, 120, 45);
                fovSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent changeEvent)
                    {
                        updateView();
                    }
                });
                fovPanel.add(fovSlider);

                // Assembly
                this.add(pitchPanel);
                this.add(headingPanel);
                this.add(rollPanel);
                this.add(fovPanel);

                JButton resetBut = new JButton("Reset");
                resetBut.addActionListener(new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        pitchSlider.setValue(90);
                        rollSlider.setValue(0);
                        headingSlider.setValue(0);
                        fovSlider.setValue(45);
                        updateView();
                    }
                });
                this.add(resetBut);

                this.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("View")));
                this.setToolTipText("View controls");
            }

            // Update view settings from control panel in a 'first person' perspective
            private void updateView()
            {
                if (!suspendEvents)
                {
                    BasicFlyView view = (BasicFlyView) this.wwd.getView();

                    // Stop iterators first
                    view.stopAnimations();

                    // Save current eye position
                    final Position pos = view.getEyePosition();

                    // Set view heading, pitch and fov
                    view.setHeading(Angle.fromDegrees(this.headingSlider.getValue()));
                    view.setPitch(Angle.fromDegrees(this.pitchSlider.getValue()));
                    view.setFieldOfView(Angle.fromDegrees(this.fovSlider.getValue()));
                    view.setRoll(Angle.fromDegrees(this.rollSlider.getValue()));
                    //view.setZoom(0);

                    // Restore eye position
                    view.setEyePosition(pos);
//                    System.out.println("Eye Position: " + pos.latitude.toString() + " , " + pos.longitude.toString() + ", " + pos.getElevation());
//                    System.out.println("Orient: " + view.getHeading() + ", " + view.getPitch() + ", " + view.getRoll() );

                    // Redraw
                    this.wwd.redraw();
                }
            }

            // Update control panel from view
            public void update()
            {
                this.suspendEvents = true;
                {
                    OrbitView view = (OrbitView) wwd.getView();
                    this.pitchSlider.setValue((int) view.getPitch().degrees);
                    this.headingSlider.setValue((int) view.getHeading().degrees);
                    this.fovSlider.setValue((int) view.getFieldOfView().degrees);
                }
                this.suspendEvents = false;
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind View Look Around", AppFrame.class);
    }
}