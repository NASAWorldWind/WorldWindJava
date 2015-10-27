/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: TrackPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TrackPanel extends JPanel
{
    private String elevationUnit;
    private String angleFormat;
    
    private JCheckBox visibilityFlag;
    private JScrollPane scrollPane;
    private PositionTable positionTable;
    // Track offset components
    private JCheckBox offsetToggleCheckBox;
    private JSpinner offsetSpinner;
    private JLabel offsetUnitLabel;

    public TrackPanel()
    {
        this.initComponents();
        this.layoutComponents();

        this.scrollPane.addMouseListener(new PositionsContextMenu(this.positionTable));
        this.positionTable.addMouseListener(new PositionsContextMenu(this.positionTable));
    }

    public void setTrack(SARTrack sarTrack)
    {
        this.positionTable.setSarTrack(sarTrack);
    }

    public SARTrack getTrack()
    {
        return this.positionTable.getSarTrack();
    }

    public String getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(String unit)
    {
        String oldValue = this.elevationUnit;
        this.elevationUnit = unit;

        this.positionTable.setElevationUnit(unit);
        this.positionTable.updateTableData();
        this.changeOffsetUnit(oldValue, this.elevationUnit);
    }

    public String getAngleFormat()
    {
        return this.angleFormat;
    }

    public void setAngleFormat(String format)
    {
        this.angleFormat = format;
        this.positionTable.setAngleFormat(format);
        this.positionTable.updateTableData();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void visibilityActionPerformed(ActionEvent e)
    {
        String vis = this.visibilityFlag.isSelected() ? TrackController.TRACK_ENABLE : TrackController.TRACK_DISABLE;
        this.positionTable.getSarTrack().firePropertyChange(vis, null, this.positionTable.getSarTrack());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void nextTrackPositionActionPerformed(ActionEvent e)
    {
        this.positionTable.getSarTrack().firePropertyChange(TrackController.MOVE_TO_NEXT_POINT, null, 
            this.positionTable.getSarTrack());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void removeTrackPositionActionPerformed(ActionEvent e)
    {
        this.positionTable.getSarTrack().firePropertyChange(TrackController.REMOVE_LAST_POINT, null,
            this.positionTable.getSarTrack());
    }

    // Track offset control

    @SuppressWarnings({"UnusedDeclaration"})
    private void offsetSpinnerStateChanged(ChangeEvent e)
    {
        applyTrackOffset(parseOffsetInput());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void offsetToggleCheckBoxItemStateChanged(ItemEvent e)
    {
        this.offsetSpinner.setEnabled(this.offsetToggleCheckBox.isSelected());
        double offset = this.offsetToggleCheckBox.isSelected() ? parseOffsetInput() : 0d;
        applyTrackOffset(offset);
    }

    private double parseOffsetInput()
    {
        return ((SpinnerNumberModel)this.offsetSpinner.getModel()).getNumber().doubleValue();
    }

    private void applyTrackOffset(double offset)
    {
        // The actual track offset will always be in meters. If the
        // user is working in imperial units, convert the slider
        // value to meters before passing it to SarTrack.
        double trackOffset;
        if (SAR2.UNIT_IMPERIAL.equals(this.elevationUnit))
            trackOffset = SAR2.feetToMeters(offset);
        else // Default to metric units.
            trackOffset = offset;

        this.positionTable.getSarTrack().setOffset(trackOffset);
        this.positionTable.getSarTrack().firePropertyChange(TrackController.TRACK_MODIFY, null,
            this.positionTable.getSarTrack());
    }

    private void changeOffsetUnit(String oldUnit, String newUnit)
    {
        if (newUnit.equals(oldUnit))
            return;

        double offset = parseOffsetInput();
        SpinnerNumberModel sm;
        if (SAR2.UNIT_IMPERIAL.equals(newUnit))
        {
            offset = SAR2.metersToFeet(offset);
            this.offsetUnitLabel.setText("ft");
            sm = new SpinnerNumberModel((int)offset, -100000, 100000, 100);
        }
        else // SAR2.UNIT_METRIC
        {
            offset = SAR2.feetToMeters(offset);
            this.offsetUnitLabel.setText("m");
            sm = new SpinnerNumberModel((int)offset, -100000, 100000, 100);
        }
        this.offsetSpinner.setModel(sm);
    }

    private void initComponents()
    {
        this.setToolTipText("Track Positions");

        this.visibilityFlag = new JCheckBox();
        this.scrollPane = new JScrollPane();
        this.positionTable = new PositionTable();
        this.offsetSpinner = new JSpinner();
        this.offsetToggleCheckBox = new JCheckBox();
        this.offsetUnitLabel = new JLabel();
    }

    protected void layoutComponents()
    {
        setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.setOpaque(false);

        //======== topPanel ========
        JPanel topPanel = new JPanel();
        {
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
            topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5)); // top, left, bottom, right
            topPanel.setOpaque(false);

            //---- visibilityFlag ----
            this.visibilityFlag.setText("Show Track");
            this.visibilityFlag.setSelected(true);
            this.visibilityFlag.setOpaque(false);
            this.visibilityFlag.setToolTipText("Display track on the globe");
            this.visibilityFlag.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    visibilityActionPerformed(e);
                }
            });
            topPanel.add(this.visibilityFlag);
            topPanel.add(Box.createHorizontalStrut(15));

            //---- track offset ----
            this.offsetToggleCheckBox.setText("Offset Altitude");
            this.offsetToggleCheckBox.setSelected(true);
            this.offsetToggleCheckBox.setOpaque(false);
            this.offsetToggleCheckBox.setToolTipText("Visually offset track altitude on the globe");
            this.offsetToggleCheckBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    offsetToggleCheckBoxItemStateChanged(e);
                }
            });
            topPanel.add(this.offsetToggleCheckBox);
            topPanel.add(Box.createHorizontalStrut(3));

            SpinnerModel sm = new SpinnerNumberModel(0, -100000, 100000, 100);
            this.offsetSpinner.setModel(sm);
            this.offsetSpinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    offsetSpinnerStateChanged(e);
                }
            });
            topPanel.add(this.offsetSpinner);
            topPanel.add(Box.createHorizontalStrut(5));

            this.offsetUnitLabel.setText("ft");
            topPanel.add(this.offsetUnitLabel);
            topPanel.add(Box.createHorizontalGlue());
        }
        this.add(topPanel, BorderLayout.NORTH);

        //======== scrollPane ========
        {
            this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            //---- positionTable ----
            this.positionTable.setPreferredScrollableViewportSize(new Dimension(340, 300));
            this.scrollPane.setViewportView(this.positionTable);
        }
        this.add(this.scrollPane, BorderLayout.CENTER);
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
        rs.addStateValueAsBoolean(context, "offsetEnabled", this.offsetToggleCheckBox.isSelected());

        double value = parseOffsetInput();
        if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
            value = SAR2.feetToMeters(value); // convert to meter if needed
        rs.addStateValueAsDouble(context, "offsetValue", value);
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Retrieve state values
        Boolean offsetEnabledState = rs.getStateValueAsBoolean(context, "offsetEnabled");
        if (offsetEnabledState != null)
            this.offsetToggleCheckBox.setSelected(offsetEnabledState);

        Double valueState = rs.getStateValueAsDouble(context, "offsetValue");
        if (valueState != null)
        {
            if (this.elevationUnit.equals(SAR2.UNIT_IMPERIAL))
                valueState = SAR2.metersToFeet(valueState); // convert to feet if needed
            this.offsetSpinner.setValue(valueState);
        }
    }

}
