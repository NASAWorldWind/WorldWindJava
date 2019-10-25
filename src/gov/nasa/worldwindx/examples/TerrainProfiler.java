/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.TerrainProfileLayer;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This application shows the {@link gov.nasa.worldwind.layers.TerrainProfileLayer} in action with its various controls.
 * It allows you to view a real-time section profile graph for any place on the planet, at any scale - continent,
 * country or mountain range - just by moving the mouse.
 * <p>
 * It proves particularly useful to explore the ocean floors where the bathymetry data reveals important geologic
 * features.
 *
 * @author tag
 * @version $Id: TerrainProfiler.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class TerrainProfiler extends ApplicationTemplate {

    @SuppressWarnings("unchecked")
    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private String follow;
        private boolean showEyePosition;
        private boolean keepProportions;
        private boolean zeroBased;
        private Dimension graphDimension;
        private double profileLengthFactor;

        private JLabel helpLabel;
        private JSlider lengthSlider;
        private JCheckBox showEyeCheck;
        private TerrainProfileLayer tpl;

        public AppFrame() {
            super(true, true, false);

            try {
                // Add TerrainProfileLayer
                this.tpl = new TerrainProfileLayer();
                this.tpl.setEventSource(this.getWwd());
                this.tpl.setStartLatLon(LatLon.fromDegrees(0, -10));
                this.tpl.setEndLatLon(LatLon.fromDegrees(0, 65));
                insertBeforeCompass(this.getWwd(), tpl);

                // retreive default values
                this.follow = this.tpl.getFollow();
                this.showEyePosition = this.tpl.getShowEyePosition();
                this.keepProportions = this.tpl.getKeepProportions();
                this.zeroBased = this.tpl.getZeroBased();
                this.graphDimension = tpl.getSize();
                this.profileLengthFactor = tpl.getProfileLenghtFactor();

                // Add control panel
                this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private JPanel makeControlPanel() {
            JPanel controlPanel = new JPanel(new GridLayout(0, 1, 0, 4));

            // Show eye position check box
            JPanel buttonsPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            this.showEyeCheck = new JCheckBox("Show eye");
            this.showEyeCheck.addActionListener((ActionEvent actionEvent) -> {
                showEyePosition = ((JCheckBox) actionEvent.getSource()).isSelected();
                update();
            });
            this.showEyeCheck.setSelected(this.showEyePosition);
            this.showEyeCheck.setEnabled(this.follow.equals(TerrainProfileLayer.FOLLOW_EYE));
            buttonsPanel.add(this.showEyeCheck);
            // Keep proportions check box
            JCheckBox cbKeepProportions = new JCheckBox("Keep proportions");
            cbKeepProportions.addActionListener((ActionEvent actionEvent) -> {
                keepProportions = ((JCheckBox) actionEvent.getSource()).isSelected();
                update();
            });
            cbKeepProportions.setSelected(this.keepProportions);
            buttonsPanel.add(cbKeepProportions);

            // Zero based graph check box
            JPanel buttonsPanel2 = new JPanel(new GridLayout(0, 2, 0, 0));
            JCheckBox cb = new JCheckBox("Zero based");
            cb.addActionListener((ActionEvent actionEvent) -> {
                zeroBased = ((JCheckBox) actionEvent.getSource()).isSelected();
                update();
            });
            cb.setSelected(this.zeroBased);
            buttonsPanel2.add(new JLabel("")); // Dummy
            buttonsPanel2.add(cb);

            // Dimension combo
            JPanel dimensionPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            dimensionPanel.add(new JLabel("  Dimension:"));
            final JComboBox cbDimension = new JComboBox(new String[]{"Small", "Medium", "Large"});
            cbDimension.addActionListener((ActionEvent actionEvent) -> {
                String size = (String) cbDimension.getSelectedItem();
                switch (size) {
                    case "Small":
                        graphDimension = new Dimension(250, 100);
                        break;
                    case "Medium":
                        graphDimension = new Dimension(450, 140);
                        break;
                    case "Large":
                        graphDimension = new Dimension(655, 240);
                        break;
                    default:
                        break;
                }
                update();
            });
            cbDimension.setSelectedItem("Small");
            dimensionPanel.add(cbDimension);

            // Profile length factor slider
            JPanel sliderPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            JSlider s = new JSlider(JSlider.HORIZONTAL, 0, 30,
                    (int) (this.profileLengthFactor * 10));  // -5 - 5 in tenth
            s.setMajorTickSpacing(10);
            s.setMinorTickSpacing(1);
            //s.setPaintTicks(true);
            //s.setPaintLabels(true);
            s.setToolTipText("Profile length");
            s.addChangeListener((ChangeEvent event) -> {
                JSlider s1 = (JSlider) event.getSource();
                if (!s1.getValueIsAdjusting()) {
                    profileLengthFactor = (double) s1.getValue() / 10;
                    update();
                }
            });
            sliderPanel.add(s);
            this.lengthSlider = s;

            // Help label
            JPanel textPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            this.helpLabel = new JLabel("Tip: move mouse over the graph.");
            this.helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
            textPanel.add(this.helpLabel);

            // Follow behavior combo
            JPanel followPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            followPanel.add(new JLabel("  Follow:"));
            final JComboBox cbFollow = new JComboBox(new String[]{"View", "Cursor", "Eye", "None", "Object"});
            cbFollow.addActionListener((ActionEvent actionEvent) -> {
                String size = (String) cbFollow.getSelectedItem();
                switch (size) {
                    case "View":
                        follow = TerrainProfileLayer.FOLLOW_VIEW;
                        helpLabel.setEnabled(true);
                        showEyeCheck.setEnabled(false);
                        lengthSlider.setEnabled(true);
                        break;
                    case "Cursor":
                        follow = TerrainProfileLayer.FOLLOW_CURSOR;
                        helpLabel.setEnabled(false);
                        showEyeCheck.setEnabled(false);
                        lengthSlider.setEnabled(true);
                        break;
                    case "Eye":
                        follow = TerrainProfileLayer.FOLLOW_EYE;
                        helpLabel.setEnabled(true);
                        showEyeCheck.setEnabled(true);
                        lengthSlider.setEnabled(true);
                        break;
                    case "None":
                        follow = TerrainProfileLayer.FOLLOW_NONE;
                        helpLabel.setEnabled(true);
                        showEyeCheck.setEnabled(false);
                        lengthSlider.setEnabled(false);
                        break;
                    case "Object":
                        follow = TerrainProfileLayer.FOLLOW_OBJECT;
                        helpLabel.setEnabled(true);
                        showEyeCheck.setEnabled(true);
                        lengthSlider.setEnabled(true);
                        OrbitView view = (OrbitView) getWwd().getView();
                        tpl.setObjectPosition(getWwd().getView().getEyePosition());
                        tpl.setObjectHeading(view.getHeading());
                        break;
                    default:
                        break;
                }
                update();
            });
            cbFollow.setSelectedItem("View");
            followPanel.add(cbFollow);

            // Assembly
            controlPanel.add(dimensionPanel);
            controlPanel.add(followPanel);
            controlPanel.add(buttonsPanel);
            controlPanel.add(buttonsPanel2);
            controlPanel.add(sliderPanel);
            controlPanel.add(textPanel);
            controlPanel.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Terrain profile")));
            controlPanel.setToolTipText("Terrain profile controls");
            return controlPanel;
        }

        // Update worldwind
        private void update() {
            this.tpl.setFollow(this.follow);
            this.tpl.setKeepProportions(this.keepProportions);
            this.tpl.setZeroBased(this.zeroBased);
            this.tpl.setSize(this.graphDimension);
            this.tpl.setShowEyePosition(this.showEyePosition);
            this.tpl.setProfileLengthFactor(this.profileLengthFactor);
            this.getWwd().redraw();
        }
    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Terrain Profiler", AppFrame.class);
    }
}
