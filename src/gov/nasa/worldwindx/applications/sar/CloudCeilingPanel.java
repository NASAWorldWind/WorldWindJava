/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author Patrick Murris
 * @version $Id: CloudCeilingPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedParameters", "unchecked"})
public class CloudCeilingPanel extends JPanel implements Restorable
{
    public static final String CLOUD_CEILING_OPEN = "CloudCeilingPanel.CloudCeilingOpen";
    public static final String CLOUD_CEILING_CHANGE = "CloudCeilingPanel.CloudCeilingChange";

    private final static String SIZE_SMALL = "Small";
    private final static String SIZE_MEDIUM = "Medium";
    private final static String SIZE_LARGE = "Large";

    private final static String PATTERN_STRIPES = "Stripes";
    private final static String PATTERN_CROSS_HATCHED = "Cross Hatched";
    private final static String PATTERN_DOTS = "Dots";

    private CloudCeiling cloudCeiling;
    private SARTrack track;
    private int trackCurrentPositionNumber;
    private String elevationUnit = SAR2.UNIT_IMPERIAL;
    private boolean suspendEvents = false;

    private Box controlPanel;
    private Box advancedPanel;
    // Control panel
    private JLabel descriptionLabel;
    private JTextField descriptionTextField;
    private JCheckBox enabledCheckBox;
    private JLabel baseElevationLabel;
    private JTextField baseElevationTextField;
    private JLabel elevationUnitLabel1;
    private JLabel elevationUnitLabel2;
    private JLabel deltaElevationLabel;
    private JTextField deltaElevationTextField;
    private ButtonGroup deltaModeGroup;
    private JRadioButton deltaPlusRadioButton;
    private JRadioButton deltaMinusRadioButton;
    private JRadioButton deltaBothRadioButton;
    private JLabel incrementLabel;
    private JSpinner incrementSpinner;
    private JButton incrementPlusButton;
    private JButton incrementMinusButton;
    private JButton incrementResetButton;
    private JLabel incrementedBaseLabel;
    private JButton advancedButton;

    // Advanced panel
    private JLabel segmentsFromLabel;
    private JLabel segmentsToLabel;
    private JSpinner segmentStartSpinner;
    private JSpinner segmentEndSpinner;
    private JCheckBox currentSegmentCheckBox;
    private JCheckBox wholeTrackCheckBox;
    private JLabel patternLabel;
    private JComboBox patternCombo;
    private JComboBox patternSizeCombo;
    private JLabel opacityLabel;
    private JSpinner opacitySpinner;
    private JButton colorButton;


    public CloudCeilingPanel()
    {
        this.initComponents();
    }

    public CloudCeiling getCloudCeiling()
    {
        return this.cloudCeiling;
    }

    public void setCloudCeiling(CloudCeiling cloudCeiling)
    {
        this.cloudCeiling = cloudCeiling;
        this.updateCloudCeiling();
    }

    public SARTrack getTrack()
    {
        return this.track;
    }

    public void setTrack(SARTrack track)
    {
        this.track = track;
        this.trackCurrentPositionNumber = this.clampTrackCurrentPosition(this.trackCurrentPositionNumber, track);
        this.updateSegmentSpinnersList();
        this.updateCloudCeiling();
        this.cloudCeiling.relocateLayerOnTop();
    }

    public void setTrackCurrentPositionNumber(int positionNumber)
    {
        if (this.trackCurrentPositionNumber == positionNumber)
            return;

        this.trackCurrentPositionNumber = positionNumber;
        if (this.currentSegmentCheckBox.isSelected())
        {
            this.suspendEvents = true;
            this.setSegmentSpinnerValue(this.segmentStartSpinner, positionNumber);
            this.setSegmentSpinnerValue(this.segmentEndSpinner, positionNumber);
            this.suspendEvents = false;
            this.updateCloudCeiling();
        }
    }

    public String getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(String elevationUnit)
    {
        if (!this.elevationUnit.equals(elevationUnit))
        {
            this.cloudCeiling.setElevationUnit(elevationUnit);
            double baseElevation = getNumberValue(this.baseElevationTextField.getText());
            double deltaElevation = getNumberValue(this.deltaElevationTextField.getText());
            double incrementedElevation = getNumberValue(this.incrementedBaseLabel.getText());
            if (SAR2.UNIT_IMPERIAL.equals(this.elevationUnit))
            {
                baseElevation = SAR2.feetToMeters(baseElevation);
                deltaElevation = SAR2.feetToMeters(deltaElevation);
                incrementedElevation = SAR2.feetToMeters(incrementedElevation);
                this.baseElevationTextField.setText(String.format("%.0f", baseElevation));
                this.deltaElevationTextField.setText(String.format("%.0f", deltaElevation));
                this.incrementedBaseLabel.setText(String.format("%.0f", incrementedElevation));
                this.elevationUnitLabel1.setText("m");
                this.elevationUnitLabel2.setText("m");
            }
            else
            {
                baseElevation = SAR2.metersToFeet(baseElevation);
                deltaElevation = SAR2.metersToFeet(deltaElevation);
                incrementedElevation = SAR2.metersToFeet(incrementedElevation);
                this.baseElevationTextField.setText(String.format("%.0f", baseElevation));
                this.deltaElevationTextField.setText(String.format("%.0f", deltaElevation));
                this.incrementedBaseLabel.setText(String.format("%.0f", incrementedElevation));
                this.elevationUnitLabel1.setText("ft");
                this.elevationUnitLabel2.setText("ft");
            }
            this.elevationUnit = elevationUnit;
        }
    }

    private int clampTrackCurrentPosition(int position, SARTrack track)
    {
        if (track.size() == 0)
            return 0;

        return WWMath.clamp(position, 0, track.size() - 1);
    }

    private void descriptionTextFieldActionPerformed(ActionEvent event)
    {
        updateCloudCeiling();
    }

    private void enabledCheckBoxActionPerformed(ActionEvent event)
    {
        updateCloudCeiling();
    }

    private void baseElevationTextFieldActionPerformed(ActionEvent event)
    {
        Double baseElevation = getNumberValue(this.baseElevationTextField.getText());
        this.baseElevationTextField.setText(String.format("%.0f", baseElevation));
        this.incrementedBaseLabel.setText(String.format("%.0f", baseElevation));
        updateCloudCeiling();
    }

    private void deltaElevationTextFieldActionPerformed(ActionEvent event)
    {
        this.deltaElevationTextField.setText(String.format("%.0f",
            getNumberValue(this.deltaElevationTextField.getText())));
        updateCloudCeiling();
    }

    private void deltaModeRadioButtonActionPerformed(ActionEvent event) {updateCloudCeiling();}

    private void advancedButtonActionPerformed(ActionEvent event)
    {
        Dimension topSize = this.getTopLevelAncestor().getSize();
        int marginH = topSize.width - this.getPreferredSize().width;
        int marginV = topSize.height - this.getPreferredSize().height;

        if (this.advancedPanel.isVisible())
        {
            this.advancedPanel.setVisible(false);
            this.advancedButton.setText("Advanced...");
        }
        else
        {
            this.advancedPanel.setVisible(true);
            this.advancedButton.setText("...Simple");
        }
        this.validate();
        this.getTopLevelAncestor().setSize(new Dimension(this.getPreferredSize().width + marginH,
            this.getPreferredSize().height + marginV));
        this.getTopLevelAncestor().validate();
    }

    private void segmentSpinnerStateChanged(ChangeEvent event)
    {
        int start = Integer.parseInt(((String)this.segmentStartSpinner.getValue()).trim());
        int end = Integer.parseInt(((String)this.segmentEndSpinner.getValue()).trim());
        if (start > end)
            if (event.getSource().equals(this.segmentStartSpinner))
                setSegmentSpinnerValue(this.segmentEndSpinner, start);
            else
                setSegmentSpinnerValue(this.segmentStartSpinner, end);

        this.currentSegmentCheckBox.setSelected(false);
        this.wholeTrackCheckBox.setSelected(false);

        updateCloudCeiling();
    }

    private void wholeTrackCheckBoxActionPerformed(ActionEvent event)
    {
        if (this.wholeTrackCheckBox.isSelected())
        {
            this.currentSegmentCheckBox.setSelected(false);
            if (this.track != null && this.track.size() > 0)
            {
                this.suspendEvents = true;
                setSegmentSpinnerValue(this.segmentStartSpinner, 0);
                setSegmentSpinnerValue(this.segmentEndSpinner, this.track.size() - 1);
                this.suspendEvents = false;
                updateCloudCeiling();
            }
        }
    }

    private void currentSegmentCheckBoxActionPerformed(ActionEvent event)
    {
        if (this.currentSegmentCheckBox.isSelected())
        {
            this.wholeTrackCheckBox.setSelected(false);
            if (this.track != null && this.track.size() > 0)
            {
                this.suspendEvents = true;
                setSegmentSpinnerValue(this.segmentStartSpinner, this.trackCurrentPositionNumber);
                setSegmentSpinnerValue(this.segmentEndSpinner, this.trackCurrentPositionNumber);
                this.suspendEvents = false;
                updateCloudCeiling();
            }
        }
    }

    private void incrementButtonActionPerformed(ActionEvent event)
    {
        Double baseElevation;
        if (event.getSource().equals(this.incrementResetButton))
        {
            baseElevation = getNumberValue(this.baseElevationTextField.getText());
        }
        else
        {
            baseElevation = getNumberValue(this.incrementedBaseLabel.getText());
            int step = Integer.parseInt(((String)this.incrementSpinner.getValue()).trim());
            if (event.getSource().equals(this.incrementPlusButton))
                baseElevation += step;
            else
                baseElevation -= step;
        }
        this.incrementedBaseLabel.setText(String.format("%.0f", baseElevation));
        updateCloudCeiling();
    }

    private void colorButtonActionPerformed(ActionEvent event)
    {
        Color c = JColorChooser.showDialog(this, "Choose a color...", ((JButton)event.getSource()).getBackground());
        if (c != null)
        {
            ((JButton)event.getSource()).setBackground(c);
            updateCloudCeiling();
        }
    }

    private void patternComboActionPerformed(ActionEvent event)
    {
        updateCloudCeiling();
    }

    private void patternSizeComboActionPerformed(ActionEvent event)
    {
        updateCloudCeiling();
    }

    private void opacitySpinnerChanged(ChangeEvent event)
    {
        updateCloudCeiling();
    }

    private Double getNumberValue(String s)
    {
        double value;
        try
        {
            value = Double.parseDouble(s);
        }
        catch (Exception e)
        {
            value = 0;
        }
        return value;
    }

    private void updateSegmentSpinnersList()
    {
        String[] strings = new String[this.track != null ? this.track.size() : 0];

        for (int i = 0; i < strings.length; i++)
            strings[i] = String.format("%,4d", i);

        if (strings.length == 0)
            strings = new String[] {"   0"};

        this.suspendEvents = true;
        {
            int start = Math.min(Integer.parseInt(((String)this.segmentStartSpinner.getValue()).trim()), strings.length - 1);
            int end = Math.min(Integer.parseInt(((String)this.segmentEndSpinner.getValue()).trim()), strings.length - 1);
            this.segmentStartSpinner.setModel(new SpinnerListModel(strings));
            this.segmentStartSpinner.setValue(strings[start]);
            this.segmentEndSpinner.setModel(new SpinnerListModel(strings));
            this.segmentEndSpinner.setValue(strings[end]);

            if (this.currentSegmentCheckBox.isSelected())
                currentSegmentCheckBoxActionPerformed(null);
            else if (this.wholeTrackCheckBox.isSelected())
                wholeTrackCheckBoxActionPerformed(null);
        }
        this.suspendEvents = false;
    }

    private void setSegmentSpinnerValue(JSpinner spinner, int n)
    {
        spinner.setValue(String.format("%,4d", n));
    }

    private void updateCloudCeiling()
    {
        if (this.cloudCeiling == null)
            return;
        // Update cloud ceiling
        this.cloudCeiling.setName(this.descriptionTextField.getText());
        this.cloudCeiling.setEnabled(this.enabledCheckBox.isSelected());
        this.cloudCeiling.setElevationBase(getNumberValue(this.incrementedBaseLabel.getText()));
        this.cloudCeiling.setElevationDelta(getNumberValue(this.deltaElevationTextField.getText()));
        if (this.deltaPlusRadioButton.isSelected())
            this.cloudCeiling.setDeltaMode(CloudCeiling.DELTA_MODE_PLUS);
        else if (this.deltaMinusRadioButton.isSelected())
            this.cloudCeiling.setDeltaMode(CloudCeiling.DELTA_MODE_MINUS);
        else if (this.deltaBothRadioButton.isSelected())
            this.cloudCeiling.setDeltaMode(CloudCeiling.DELTA_MODE_BOTH);
        this.cloudCeiling.setColor(this.colorButton.getBackground());
        this.cloudCeiling.setPattern(this.getPattern());
        this.cloudCeiling.setPatternSize(this.getPatternSize());
        this.cloudCeiling.setPlaneOpacity(Double.parseDouble((String)this.opacitySpinner.getValue()) / 10);
        // Track positions
        if (this.track != null && this.track.getPositions().size() > 0)
        {
            int start = Integer.parseInt(((String)this.segmentStartSpinner.getValue()).trim());
            int end = Integer.parseInt(((String)this.segmentEndSpinner.getValue()).trim());
            if (end < this.track.getPositions().size() - 1)
                end++;
            ArrayList<LatLon> positions = new ArrayList<LatLon>(end - start + 1);
            for (int i = start; i <= end; i++)
                positions.add(this.track.getPositions().get(i));
            this.cloudCeiling.setPositions(positions);

        }
        // Fire change event
        this.firePropertyChange(CLOUD_CEILING_CHANGE, -1, 0);
        // Update panel enabled components state
        enableComponents(this.enabledCheckBox.isSelected());
    }

    private String getPattern()
    {
        String value = (String)this.patternCombo.getSelectedItem();
        if (PATTERN_CROSS_HATCHED.equals(value))
            return PatternFactory.PATTERN_HVLINE;
        else if (PATTERN_DOTS.equals(value))
            return PatternFactory.PATTERN_CIRCLES;
        else // default to PATTERN_STRIPES
            return PatternFactory.PATTERN_HLINE;
    }

    private double getPatternSize()
    {
        String value = (String)this.patternSizeCombo.getSelectedItem();
        if (SIZE_SMALL.equals(value))
            return 50;
        else if (SIZE_LARGE.equals(value))
            return 500;
        else // default to SIZE_MEDIUM
            return 150;
    }

    private void initComponents()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Control panel
        this.controlPanel = Box.createVerticalBox();
        {
            controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            // Description and enabled panel
            Box descPanel = Box.createHorizontalBox();
            descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            {
                this.descriptionLabel = new JLabel("Name:");
                descPanel.add(this.descriptionLabel);
                descPanel.add(Box.createHorizontalStrut(20));
                this.descriptionTextField = new JTextField("Cloud contour", 20);
                this.descriptionTextField.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            descriptionTextFieldActionPerformed(event);
                    }
                });
                descPanel.add(this.descriptionTextField);
                descPanel.add(Box.createHorizontalStrut(20));
                this.enabledCheckBox = new JCheckBox("Show contour");
                this.enabledCheckBox.setSelected(false);
                this.enabledCheckBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            enabledCheckBoxActionPerformed(event);
                    }
                });
                descPanel.add(this.enabledCheckBox);
            }
            controlPanel.add(descPanel);
            controlPanel.add(Box.createVerticalStrut(10));

            // Base elevation and delta elevation/mode
            Box elevationPanel = Box.createHorizontalBox();
            elevationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            {
                this.baseElevationLabel = new JLabel("Base:");
                elevationPanel.add(this.baseElevationLabel);
                elevationPanel.add(Box.createHorizontalStrut(10));
                this.baseElevationTextField = new JTextField("0", 5);
                this.baseElevationTextField.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            baseElevationTextFieldActionPerformed(event);
                    }
                });
                elevationPanel.add(this.baseElevationTextField);
                elevationPanel.add(Box.createHorizontalStrut(5));
                this.elevationUnitLabel1 = new JLabel("ft");
                elevationPanel.add(this.elevationUnitLabel1);
                elevationPanel.add(Box.createHorizontalStrut(20));
                this.deltaElevationLabel = new JLabel("Delta:");
                elevationPanel.add(this.deltaElevationLabel);
                elevationPanel.add(Box.createHorizontalStrut(10));
                this.deltaElevationTextField = new JTextField("0", 5);
                this.deltaElevationTextField.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            deltaElevationTextFieldActionPerformed(event);
                    }
                });
                elevationPanel.add(this.deltaElevationTextField);
                elevationPanel.add(Box.createHorizontalStrut(20));

                this.deltaPlusRadioButton = new JRadioButton("+");
                this.deltaPlusRadioButton.setSelected(true);
                this.deltaPlusRadioButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            deltaModeRadioButtonActionPerformed(event);
                    }
                });
                elevationPanel.add(this.deltaPlusRadioButton);
                this.deltaMinusRadioButton = new JRadioButton("-");
                this.deltaMinusRadioButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            deltaModeRadioButtonActionPerformed(event);
                    }
                });
                elevationPanel.add(this.deltaMinusRadioButton);
                this.deltaBothRadioButton = new JRadioButton("+/-");
                this.deltaBothRadioButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            deltaModeRadioButtonActionPerformed(event);
                    }
                });
                elevationPanel.add(this.deltaBothRadioButton);
                this.deltaModeGroup = new ButtonGroup();
                this.deltaModeGroup.add(this.deltaPlusRadioButton);
                this.deltaModeGroup.add(this.deltaMinusRadioButton);
                this.deltaModeGroup.add(this.deltaBothRadioButton);
            }
            controlPanel.add(elevationPanel);
            controlPanel.add(Box.createVerticalStrut(10));

            // Increment
            Box incrementPanel = Box.createHorizontalBox();
            incrementPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            {
                this.incrementLabel = new JLabel("Base Increment:");
                incrementPanel.add(this.incrementLabel);
                incrementPanel.add(Box.createHorizontalStrut(20));
                this.incrementSpinner = new JSpinner(new SpinnerListModel(
                    new String[] {"10", "50", "100", "200", "500", "1000"}));
                this.incrementSpinner.setValue("100");
                incrementPanel.add(this.incrementSpinner);
                incrementPanel.add(Box.createHorizontalStrut(20));
                this.incrementMinusButton = new JButton("Down");
                this.incrementMinusButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            incrementButtonActionPerformed(event);
                    }
                });
                incrementPanel.add(this.incrementMinusButton);
                incrementPanel.add(Box.createHorizontalStrut(10));
                this.incrementedBaseLabel = new JLabel("0");
                this.incrementedBaseLabel.setPreferredSize(new Dimension(60, 16));
                this.incrementedBaseLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                incrementPanel.add(this.incrementedBaseLabel);
                incrementPanel.add(Box.createHorizontalStrut(5));
                this.elevationUnitLabel2 = new JLabel("ft");
                incrementPanel.add(this.elevationUnitLabel2);
                incrementPanel.add(Box.createHorizontalStrut(10));
                this.incrementPlusButton = new JButton("Up");
                this.incrementPlusButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            incrementButtonActionPerformed(event);
                    }
                });
                incrementPanel.add(this.incrementPlusButton);
                incrementPanel.add(Box.createHorizontalStrut(10));
                this.incrementResetButton = new JButton("Reset");
                this.incrementResetButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            incrementButtonActionPerformed(event);
                    }
                });
                incrementPanel.add(this.incrementResetButton);
            }
            controlPanel.add(incrementPanel);
            controlPanel.add(Box.createVerticalStrut(10));

            Box advancedButtonPanel = Box.createHorizontalBox();
            advancedButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            {
                this.advancedButton = new JButton("Advanced...");
                this.advancedButton.setContentAreaFilled(false);
                this.advancedButton.setBorderPainted(false);
                this.advancedButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (!suspendEvents)
                            advancedButtonActionPerformed(e);
                    }
                });
                advancedButtonPanel.add(Box.createHorizontalGlue());
                advancedButtonPanel.add(this.advancedButton);
            }
            controlPanel.add(advancedButtonPanel);
        }

        // Advanced panel
        this.advancedPanel = Box.createVerticalBox();
        {
            advancedPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            // Track segments
            Box segmentsPanel = Box.createHorizontalBox();
            segmentsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            {
                this.segmentsFromLabel = new JLabel("Track points:");
                segmentsPanel.add(this.segmentsFromLabel);
                segmentsPanel.add(Box.createHorizontalStrut(20));
                this.segmentStartSpinner = new JSpinner(new SpinnerListModel(new String[] {"   0"}));
                this.segmentStartSpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent event)
                    {
                        if (!suspendEvents)
                            segmentSpinnerStateChanged(event);
                    }
                });
                segmentsPanel.add(this.segmentStartSpinner);
                segmentsPanel.add(Box.createHorizontalStrut(20));
                this.segmentsToLabel = new JLabel("To:");
                segmentsPanel.add(this.segmentsToLabel);
                segmentsPanel.add(Box.createHorizontalStrut(20));
                this.segmentEndSpinner = new JSpinner(new SpinnerListModel(new String[] {"   0"}));
                this.segmentEndSpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent event)
                    {
                        if (!suspendEvents)
                            segmentSpinnerStateChanged(event);
                    }
                });
                segmentsPanel.add(this.segmentEndSpinner);
                segmentsPanel.add(Box.createHorizontalStrut(10));
                this.currentSegmentCheckBox = new JCheckBox("Current segment");
                this.currentSegmentCheckBox.setSelected(true);
                this.currentSegmentCheckBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            currentSegmentCheckBoxActionPerformed(event);
                    }
                });
                segmentsPanel.add(this.currentSegmentCheckBox);
                segmentsPanel.add(Box.createHorizontalStrut(10));
                this.wholeTrackCheckBox = new JCheckBox("Whole track");
                this.wholeTrackCheckBox.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            wholeTrackCheckBoxActionPerformed(event);
                    }
                });
                segmentsPanel.add(this.wholeTrackCheckBox);

            }
            advancedPanel.add(segmentsPanel);
            advancedPanel.add(Box.createVerticalStrut(10));

            // Pattern
            Box patternPanel = Box.createHorizontalBox();
            patternPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            {
                this.patternLabel = new JLabel("Pattern:");
                patternPanel.add(this.patternLabel);
                patternPanel.add(Box.createHorizontalStrut(20));
                this.patternCombo = new JComboBox(new String[] {PATTERN_STRIPES, PATTERN_CROSS_HATCHED, PATTERN_DOTS});
                this.patternCombo.setSelectedItem(PATTERN_DOTS);
                this.patternCombo.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (!suspendEvents)
                            patternComboActionPerformed(e);
                    }
                });
                patternPanel.add(this.patternCombo);
                patternPanel.add(Box.createHorizontalStrut(10));
                this.patternSizeCombo = new JComboBox(new String[] {SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE});
                this.patternSizeCombo.setSelectedItem(SIZE_MEDIUM);
                this.patternSizeCombo.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (!suspendEvents)
                            patternSizeComboActionPerformed(e);
                    }
                });
                patternPanel.add(this.patternSizeCombo);
                patternPanel.add(Box.createHorizontalStrut(20));

                this.opacityLabel = new JLabel("Opacity:");
                patternPanel.add(this.opacityLabel);
                patternPanel.add(Box.createHorizontalStrut(10));
                this.opacitySpinner = new JSpinner(new SpinnerListModel(
                    new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}));
                this.opacitySpinner.setValue("3");
                this.opacitySpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        if (!suspendEvents)
                            opacitySpinnerChanged(e);
                    }
                });
                patternPanel.add(this.opacitySpinner);
                patternPanel.add(Box.createHorizontalStrut(20));
                this.colorButton = new JButton("Color");
                this.colorButton.setBackground(Color.CYAN);
                this.colorButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (!suspendEvents)
                            colorButtonActionPerformed(event);
                    }
                });
                patternPanel.add(this.colorButton);

            }
            advancedPanel.add(patternPanel);

        }

        this.add(this.controlPanel);
        this.add(this.advancedPanel);
        this.advancedPanel.setVisible(false);

        enableComponents(this.enabledCheckBox.isSelected());
        
    }

    private void enableComponents(boolean state)
    {
        this.descriptionTextField.setEnabled(state);
        this.baseElevationTextField.setEnabled(state);
        this.deltaElevationTextField.setEnabled(state);
        this.deltaPlusRadioButton.setEnabled(state);
        this.deltaMinusRadioButton.setEnabled(state);
        this.deltaBothRadioButton.setEnabled(state);
        this.segmentStartSpinner.setEnabled(state);
        this.segmentEndSpinner.setEnabled(state);
        this.currentSegmentCheckBox.setEnabled(state);
        this.wholeTrackCheckBox.setEnabled(state);
        this.colorButton.setEnabled(state);
        this.incrementSpinner.setEnabled(state);
        this.incrementMinusButton.setEnabled(state);
        this.incrementPlusButton.setEnabled(state);
        this.patternCombo.setEnabled(state);
        this.patternSizeCombo.setEnabled(state);
        this.opacitySpinner.setEnabled(state);
        this.advancedButton.setEnabled(state);
    }

    // *** Restorable interface ***

    public String getRestorableState()
    {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
    }

    public void restoreState(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        this.doRestoreState(rs, null);
    }

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Add state values
        rs.addStateValueAsString(context, "description", this.descriptionTextField.getText());
        rs.addStateValueAsBoolean(context, "enabled", this.enabledCheckBox.isSelected());

        double base = getNumberValue(this.baseElevationTextField.getText());
        if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
            base = SAR2.feetToMeters(base); // convert to meter if needed
        rs.addStateValueAsDouble(context, "base", base);

        double delta = getNumberValue(this.deltaElevationTextField.getText());
        if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
            delta = SAR2.feetToMeters(delta); // convert to meter if needed
        rs.addStateValueAsDouble(context, "delta", delta);

        double incrementedBase = getNumberValue(this.incrementedBaseLabel.getText());
        if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
            incrementedBase = SAR2.feetToMeters(incrementedBase); // convert to meter if needed
        rs.addStateValueAsDouble(context, "incrementedBase", incrementedBase);

        rs.addStateValueAsBoolean(context, "deltaPlus", this.deltaPlusRadioButton.isSelected());
        rs.addStateValueAsBoolean(context, "deltaMinus", this.deltaMinusRadioButton.isSelected());
        rs.addStateValueAsBoolean(context, "deltaBoth", this.deltaBothRadioButton.isSelected());
        rs.addStateValueAsString(context, "segmentStart", (String)this.segmentStartSpinner.getValue());
        rs.addStateValueAsString(context, "segmentEnd", (String)this.segmentEndSpinner.getValue());
        rs.addStateValueAsBoolean(context, "currentSegment", this.currentSegmentCheckBox.isSelected());
        rs.addStateValueAsBoolean(context, "wholeTrack", this.wholeTrackCheckBox.isSelected());
        rs.addStateValueAsString(context, "increment", (String)this.incrementSpinner.getValue());
        rs.addStateValueAsString(context, "pattern", (String)this.patternCombo.getSelectedItem());
        rs.addStateValueAsString(context, "patternSize", (String)this.patternSizeCombo.getSelectedItem());
        rs.addStateValueAsString(context, "opacity", (String)this.opacitySpinner.getValue());
        String encodedColor = RestorableSupport.encodeColor(this.colorButton.getBackground());
        if (encodedColor != null)
            rs.addStateValueAsString(context, "color", encodedColor);

    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        this.suspendEvents = true;
        {
            // Retrieve state values
            String descriptionState = rs.getStateValueAsString(context, "description");
            if (descriptionState != null)
                this.descriptionTextField.setText(descriptionState);

            Boolean enabledState = rs.getStateValueAsBoolean(context, "enabled");
            if (enabledState != null)
                this.enabledCheckBox.setSelected(enabledState);

            Double baseState = rs.getStateValueAsDouble(context, "base");
            if (baseState != null)
            {
                if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
                    baseState = SAR2.metersToFeet(baseState); // convert to feet if needed
                this.baseElevationTextField.setText(String.format("%.0f", baseState));
            }

            Double deltaState = rs.getStateValueAsDouble(context, "delta");
            if (deltaState != null)
            {
                if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
                    deltaState = SAR2.metersToFeet(deltaState); // convert to feet if needed
                this.deltaElevationTextField.setText(String.format("%.0f", deltaState));
            }

            Double incrementedBaseState = rs.getStateValueAsDouble(context, "incrementedBase");
            if (incrementedBaseState != null)
            {
                if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
                    incrementedBaseState = SAR2.metersToFeet(incrementedBaseState); // convert to feet if needed
                this.incrementedBaseLabel.setText(String.format("%.0f", incrementedBaseState));
            }

            Boolean deltaPlusState = rs.getStateValueAsBoolean(context, "deltaPlus");
            if (deltaPlusState != null)
                this.deltaPlusRadioButton.setSelected(deltaPlusState);

            Boolean deltaMinusState = rs.getStateValueAsBoolean(context, "deltaMinus");
            if (deltaMinusState != null)
                this.deltaMinusRadioButton.setSelected(deltaMinusState);

            Boolean deltaBothState = rs.getStateValueAsBoolean(context, "deltaBoth");
            if (deltaBothState != null)
                this.deltaBothRadioButton.setSelected(deltaBothState);

            String incrementState = rs.getStateValueAsString(context, "increment");
            if (incrementState != null)
                this.incrementSpinner.setValue(incrementState);

            String segmentStartState = rs.getStateValueAsString(context, "segmentStart");
            if (segmentStartState != null)
                this.segmentStartSpinner.setValue(segmentStartState);

            String segmentEndState = rs.getStateValueAsString(context, "segmentEnd");
            if (segmentEndState != null)
                this.segmentEndSpinner.setValue(segmentEndState);

            Boolean currentSegmentState = rs.getStateValueAsBoolean(context, "currentSegment");
            if (currentSegmentState != null)
                this.currentSegmentCheckBox.setSelected(currentSegmentState);

            Boolean wholeTrackState = rs.getStateValueAsBoolean(context, "wholeTrack");
            if (wholeTrackState != null)
                this.wholeTrackCheckBox.setSelected(wholeTrackState);

            String patternState = rs.getStateValueAsString(context, "pattern");
            if (patternState != null)
                this.patternCombo.setSelectedItem(patternState);

            String patternSizeState = rs.getStateValueAsString(context, "patternSize");
            if (patternSizeState != null)
                this.patternSizeCombo.setSelectedItem(patternSizeState);

            String opacityState = rs.getStateValueAsString(context, "opacity");
            if (opacityState != null)
                this.opacitySpinner.setValue(opacityState);

            String colorState = rs.getStateValueAsString(context, "color");
            if (colorState != null)
            {
                Color color = RestorableSupport.decodeColor(colorState);
                if (color != null)
                    this.colorButton.setBackground(color);
            }
        }
        this.suspendEvents = false;
        this.updateCloudCeiling();
    }


}
