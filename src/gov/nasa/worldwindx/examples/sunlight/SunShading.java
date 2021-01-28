/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.sunlight;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Earth.USGSTopoHighRes;
import gov.nasa.worldwind.layers.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Sun light and shading usage.
 *
 * @author Patrick Murris
 * @version $Id: SunShading.java 12584 2009-09-14 19:25:59Z dcollins $
 * @see RectangularNormalTessellator
 * @see AtmosphereLayer, AtmosphericScatteringComputer
 * @see LensFlareLayer
 * @see BasicSunPositionProvider, SunCalculator
 */
public class SunShading extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private JCheckBox enableCheckBox;
        private JButton colorButton;
        private JButton ambientButton;
        private JRadioButton relativeRadioButton;
        private JRadioButton absoluteRadioButton;
        private JSlider azimuthSlider;
        private JSlider elevationSlider;

        private RectangularNormalTessellator tessellator;
        private LensFlareLayer lensFlareLayer;
        private AtmosphereLayer atmosphereLayer;
        private SunPositionProvider spp = new BasicSunPositionProvider();

        public AppFrame() {
            super(true, true, false);

            // Add USGS Topo maps
            insertBeforePlacenames(getWwd(), new USGSTopoHighRes());

            // Replace sky gradient with atmosphere layer
            this.atmosphereLayer = new AtmosphereLayer();
            for (int i = 0; i < this.getWwd().getModel().getLayers().size(); i++) {
                Layer l = this.getWwd().getModel().getLayers().get(i);
                if (l instanceof SkyGradientLayer) {
                    this.getWwd().getModel().getLayers().set(i, this.atmosphereLayer);
                }
            }

            // Add lens flare layer
            this.lensFlareLayer = LensFlareLayer.getPresetInstance(LensFlareLayer.PRESET_BOLD);
            this.getWwd().getModel().getLayers().add(this.lensFlareLayer);

            // Update layer panel
            this.getLayerPanel().update(getWwd());

            // Get tessellator
            this.tessellator = (RectangularNormalTessellator) getWwd().getModel().getGlobe().getTessellator();

            // Add control panel
            this.getLayerPanel().add(makeControlPanel(), BorderLayout.SOUTH);

            // Add position listener to update light direction relative to the eye
            getWwd().addPositionListener(new PositionListener() {
                Vec4 eyePoint;

                public void moved(PositionEvent event) {
                    if (eyePoint == null || eyePoint.distanceTo3(getWwd().getView().getEyePoint()) > 1000) {
                        update();
                        eyePoint = getWwd().getView().getEyePoint();
                    }
                }
            });

            // Add one minute update timer
            Timer updateTimer = new Timer(60000, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    update();
                }
            });
            updateTimer.start();

            update();
        }

        private JPanel makeControlPanel() {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    new TitledBorder("Sun Light")));
            controlPanel.setToolTipText("Set the Sun light direction and color");

            // Enable and Color
            final JPanel colorPanel = new JPanel(new GridLayout(0, 3, 0, 0));
            colorPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            enableCheckBox = new JCheckBox("Enable");
            enableCheckBox.setSelected(true);
            enableCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    update();
                }
            });
            colorPanel.add(enableCheckBox);

            colorButton = new JButton("Light");
            colorButton.setBackground(this.tessellator.getLightColor());
            colorButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Color c = JColorChooser.showDialog(colorPanel, "Choose a color...",
                            ((JButton) event.getSource()).getBackground());
                    if (c != null) {
                        ((JButton) event.getSource()).setBackground(c);
                        update();
                    }
                }
            });
            colorPanel.add(colorButton);

            ambientButton = new JButton("Shade");
            ambientButton.setBackground(this.tessellator.getAmbientColor());
            ambientButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Color c = JColorChooser.showDialog(colorPanel, "Choose a color...",
                            ((JButton) event.getSource()).getBackground());
                    if (c != null) {
                        ((JButton) event.getSource()).setBackground(c);
                        update();
                    }
                }
            });
            colorPanel.add(ambientButton);

            // Relative vs absolute Sun position
            final JPanel positionTypePanel = new JPanel(new GridLayout(0, 2, 0, 0));
            positionTypePanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            relativeRadioButton = new JRadioButton("Relative");
            relativeRadioButton.setSelected(false);
            relativeRadioButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    update();
                }
            });
            positionTypePanel.add(relativeRadioButton);
            absoluteRadioButton = new JRadioButton("Absolute");
            absoluteRadioButton.setSelected(true);
            absoluteRadioButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    update();
                }
            });
            positionTypePanel.add(absoluteRadioButton);
            ButtonGroup group = new ButtonGroup();
            group.add(relativeRadioButton);
            group.add(absoluteRadioButton);

            // Azimuth slider
            JPanel azimuthPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            azimuthPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            azimuthPanel.add(new JLabel("Azimuth:"));
            azimuthSlider = new JSlider(0, 360, 125);
            azimuthSlider.setPaintTicks(true);
            azimuthSlider.setPaintLabels(true);
            azimuthSlider.setMajorTickSpacing(90);
            azimuthSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    update();
                }
            });
            azimuthPanel.add(azimuthSlider);

            // Elevation slider
            JPanel elevationPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            elevationPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            elevationPanel.add(new JLabel("Elevation:"));
            elevationSlider = new JSlider(-10, 90, 50);
            elevationSlider.setPaintTicks(true);
            elevationSlider.setPaintLabels(true);
            elevationSlider.setMajorTickSpacing(10);
            elevationSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    update();
                }
            });
            elevationPanel.add(elevationSlider);

            // Control panel assembly
            controlPanel.add(colorPanel);
            controlPanel.add(positionTypePanel);
            controlPanel.add(azimuthPanel);
            controlPanel.add(elevationPanel);
            return controlPanel;
        }

        // Update worldwind
        private void update() {
            if (this.enableCheckBox.isSelected()) {
                // Enable UI controls
                this.colorButton.setEnabled(true);
                this.ambientButton.setEnabled(true);
                this.absoluteRadioButton.setEnabled(true);
                this.relativeRadioButton.setEnabled(true);
                this.azimuthSlider.setEnabled(true);
                this.elevationSlider.setEnabled(true);
                // Update colors
                this.tessellator.setLightColor(this.colorButton.getBackground());
                this.tessellator.setAmbientColor(this.ambientButton.getBackground());
                // Compute Sun direction
                Vec4 sun, light;
                if (this.relativeRadioButton.isSelected()) {
                    // Enable UI controls
                    this.azimuthSlider.setEnabled(true);
                    this.elevationSlider.setEnabled(true);
                    // Compute Sun position relative to the eye position
                    Angle elevation = Angle.fromDegrees(this.elevationSlider.getValue());
                    Angle azimuth = Angle.fromDegrees(this.azimuthSlider.getValue());
                    Position eyePos = getWwd().getView().getEyePosition();
                    sun = Vec4.UNIT_Y;
                    sun = sun.transformBy3(Matrix.fromRotationX(elevation));
                    sun = sun.transformBy3(Matrix.fromRotationZ(azimuth.multiply(-1)));
                    sun = sun.transformBy3(getWwd().getModel().getGlobe().computeModelCoordinateOriginTransform(
                            eyePos.getLatitude(), eyePos.getLongitude(), 0));
                } else {
                    // Disable UI controls
                    this.azimuthSlider.setEnabled(false);
                    this.elevationSlider.setEnabled(false);
                    // Compute Sun position according to current date and time
                    LatLon sunPos = spp.getPosition();
                    sun = getWwd().getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
                }
                light = sun.getNegative3();
                this.tessellator.setLightDirection(light);
                this.lensFlareLayer.setSunDirection(sun);
                this.atmosphereLayer.setSunDirection(sun);
            } else {
                // Disable UI controls
                this.colorButton.setEnabled(false);
                this.ambientButton.setEnabled(false);
                this.absoluteRadioButton.setEnabled(false);
                this.relativeRadioButton.setEnabled(false);
                this.azimuthSlider.setEnabled(false);
                this.elevationSlider.setEnabled(false);
                // Turn off lighting
                this.tessellator.setLightDirection(null);
                this.lensFlareLayer.setSunDirection(null);
                this.atmosphereLayer.setSunDirection(null);
            }
            // Redraw
            this.getWwd().redraw();
        }
    }

    public static void main(String[] args) {
        // Use normal/shading tessellator
        Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, RectangularNormalTessellator.class.getName());

        ApplicationTemplate.start("World Wind Sun Shading", AppFrame.class);
    }
}
