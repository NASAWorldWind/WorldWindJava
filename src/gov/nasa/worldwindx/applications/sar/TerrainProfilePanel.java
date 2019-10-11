/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.TerrainProfileLayer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author tag
 * @version $Id: TerrainProfilePanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings({"FieldCanBeLocal", "unchecked"})
public class TerrainProfilePanel extends JPanel implements Restorable {

    public static final String TERRAIN_PROFILE_OPEN = "TerrainProfilePanel.Open";
    public static final String TERRAIN_PROFILE_CHANGE = "TerrainProfilePanel.Change";

    private final TerrainProfileController controller;
    private static final String GRAPH_SIZE_SMALL_TEXT = "Small Graph";
    private static final String GRAPH_SIZE_MEDIUM_TEXT = "Medium Graph";
    private static final String GRAPH_SIZE_LARGE_TEXT = "Large Graph";
    private static final String FOLLOW_VIEW_TEXT = "Profile At Screen Center";
    private static final String FOLLOW_CURSOR_TEXT = "Profile Under Cursor";
    //private static final String FOLLOW_EYE_TEXT    = "Profile Under Eye";
    private static final String FOLLOW_OBJECT_TEXT = "Profile Under Aircraft";
    private static final String FOLLOW_NONE_TEXT = "No Profile";

    public TerrainProfilePanel() {
        initComponents();
        this.controller = new TerrainProfileController();
    }

    public WorldWindow getWwd() {
        return this.controller.getWwd();
    }

    public void setWwd(WorldWindow wwd) {
        this.controller.setWwd(wwd);
        this.matchProfileToPanel();
    }

    private void matchProfileToPanel() {
        this.setFollow();
        this.controller.setProfileSize((String) this.sizeComboBox.getSelectedItem());
        this.controller.setKeepProportions(this.proportionalCheckBox.isSelected());
        this.controller.setShowEyePosition(this.showEyeCheckBox.isSelected());
        this.controller.setZeroBased(this.zeroBaseCheckBox.isSelected());
        this.controller.setProfileWidthFactor(
                Double.parseDouble(((String) this.profileWidthSpinner.getValue()).replace("x", "")));
        this.controller.setProfileLengthFactor(
                Double.parseDouble(((String) this.profileLengthSpinner.getValue()).replace("x", "")));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void sizeComboBoxActionPerformed(ActionEvent e) {
        this.controller.setProfileSize((String) this.sizeComboBox.getSelectedItem());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void followComboBoxActionPerformed(ActionEvent e) {
        this.setFollow();
    }

    //public void setFollowEye()
    //{
    //    this.followComboBox.getModel().setSelectedItem(FOLLOW_EYE_TEXT);
    //}/
    public void setFollowObject() {
        this.followComboBox.getModel().setSelectedItem(FOLLOW_OBJECT_TEXT);
    }

    @SuppressWarnings({"StringEquality"})
    private void setFollow() {
        this.controller.setFollow((String) this.followComboBox.getSelectedItem());
        String follow = this.controller.getFollow();

        if (follow == TerrainProfileLayer.FOLLOW_VIEW) {
            if (this.showEyeCheckBox.isEnabled()) {
                this.showEyeCheckBox.setEnabled(false);
            }
            if (!this.profileWidthSpinner.isEnabled()) {
                this.profileWidthSpinner.setEnabled(true);
            }
            if (this.profileLengthSpinner.isEnabled()) {
                this.profileLengthSpinner.setEnabled(false);
            }
        } else if (follow == TerrainProfileLayer.FOLLOW_CURSOR) {
            if (this.showEyeCheckBox.isEnabled()) {
                this.showEyeCheckBox.setEnabled(false);
            }
            if (!this.profileWidthSpinner.isEnabled()) {
                this.profileWidthSpinner.setEnabled(true);
            }
            if (this.profileLengthSpinner.isEnabled()) {
                this.profileLengthSpinner.setEnabled(false);
            }
        } //else if (follow == TerrainProfileLayer.FOLLOW_EYE)
        //{
        //    if (!this.showEyeCheckBox.isEnabled())
        //        this.showEyeCheckBox.setEnabled(true);
        //    if (!this.profileWidthSlider.isEnabled())
        //        this.profileWidthSlider.setEnabled(true);
        //    if (!this.profileLengthSlider.isEnabled())
        //        this.profileLengthSlider.setEnabled(true);
        //}
        else if (follow == TerrainProfileLayer.FOLLOW_OBJECT) {
            if (!this.showEyeCheckBox.isEnabled()) {
                this.showEyeCheckBox.setEnabled(true);
            }
            if (!this.profileWidthSpinner.isEnabled()) {
                this.profileWidthSpinner.setEnabled(true);
            }
            if (!this.profileLengthSpinner.isEnabled()) {
                this.profileLengthSpinner.setEnabled(true);
            }
        } else if (follow == TerrainProfileLayer.FOLLOW_NONE) {
            if (this.showEyeCheckBox.isEnabled()) {
                this.showEyeCheckBox.setEnabled(false);
            }
            if (this.profileWidthSpinner.isEnabled()) {
                this.profileWidthSpinner.setEnabled(false);
            }
            if (this.profileLengthSpinner.isEnabled()) {
                this.profileLengthSpinner.setEnabled(false);
            }
        }
    }

    private void proportionalCheckBoxItemStateChanged(ItemEvent e) {
        this.controller.setKeepProportions(((JCheckBox) e.getSource()).isSelected());
    }

    private void showEyeCheckBoxItemStateChanged(ItemEvent e) {
        this.controller.setShowEyePosition(((JCheckBox) e.getSource()).isSelected());
    }

    private void zeroBaseCheckBoxItemStateChanged(ItemEvent e) {
        this.controller.setZeroBased(((JCheckBox) e.getSource()).isSelected());
    }

    private void profileWidthSpinnerStateChanged(ChangeEvent e) {
        String value = (String) ((JSpinner) e.getSource()).getValue();
        value = value.replace("x", "");
        this.controller.setProfileWidthFactor(Double.parseDouble(value));
        if (this.profilesSameSize.isSelected()) {
            this.profileLengthSpinner.setValue(this.profileWidthSpinner.getValue());
        }
    }

    private void profileLengthSpinnerStateChanged(ChangeEvent e) {
        String value = (String) ((JSpinner) e.getSource()).getValue();
        value = value.replace("x", "");
        this.controller.setProfileLengthFactor(Double.parseDouble(value));
        if (this.profilesSameSize.isSelected()) {
            this.profileWidthSpinner.setValue(this.profileLengthSpinner.getValue());
        }
    }

    public void profilesSameSizeStateChanged(ChangeEvent e) {
        if (((JCheckBox) e.getSource()).isSelected()) {
            this.profileLengthSpinner.setValue(this.profileWidthSpinner.getValue());
        }
    }

    public void profileFollowPathStateChanged(ChangeEvent e) {
        this.controller.setWholeTrackLength(((JCheckBox) e.getSource()).isSelected());
    }

    public void updatePosition(Position position, Angle heading) {
        this.controller.updatePosition(position, heading);
    }

    public void updatePath(ArrayList<? extends LatLon> positions) {
        this.controller.updatePath(positions);
    }

    public String getFollow() {
        return this.controller.getFollow();
    }

    private void initComponents() {
        this.panel1 = new JPanel();
        this.panel2 = new JPanel();
        this.panel6 = new JPanel();
        this.panel5 = new JPanel();
        this.sizeComboBox = new JComboBox();
        this.panel7 = new JPanel();
        this.followComboBox = new JComboBox();
        this.panel3 = new JPanel();
        this.proportionalCheckBox = new JCheckBox();
        this.showEyeCheckBox = new JCheckBox();
        this.zeroBaseCheckBox = new JCheckBox();
        this.panel4 = new JPanel();
        this.profileWidthSpinner = new JSpinner();
        this.profileWidthLabel = new JLabel();
        this.panel8 = new JPanel();
        this.panel4b = new JPanel();
        this.profileLengthSpinner = new JSpinner();
        this.profileLengthLabel = new JLabel();
        this.panel4c = new JPanel();
        this.profilesSameSize = new JCheckBox();
        this.panel4d = new JPanel();
        this.profileFollowPath = new JCheckBox();

        //======== this ========
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(20, 20));

        //======== panel1 ========
        {
            this.panel1.setLayout(new BorderLayout(20, 20));

            //======== panel2 ========
            {
                this.panel2.setLayout(new GridLayout(1, 2, 20, 10));

                //======== panel6 ========
                {
                    this.panel6.setLayout(new GridLayout(1, 2, 20, 10));

                    //======== panel5 ========
                    {
                        this.panel5.setLayout(new BorderLayout(5, 5));

                        //---- sizeComboBox ----
                        this.sizeComboBox.setModel(new DefaultComboBoxModel(new String[]{
                            GRAPH_SIZE_SMALL_TEXT,
                            GRAPH_SIZE_MEDIUM_TEXT,
                            GRAPH_SIZE_LARGE_TEXT
                        }));
                        this.sizeComboBox.setToolTipText("Size of profile graph");
                        this.sizeComboBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                sizeComboBoxActionPerformed(e);
                            }
                        });
                        this.panel5.add(this.sizeComboBox, BorderLayout.CENTER);
                    }
                    this.panel6.add(this.panel5);

                    //======== panel7 ========
                    {
                        this.panel7.setLayout(new BorderLayout(5, 5));

                        //---- followComboBox ----
                        this.followComboBox.setModel(new DefaultComboBoxModel(new String[]{
                            FOLLOW_VIEW_TEXT,
                            FOLLOW_CURSOR_TEXT,
                            //FOLLOW_EYE_TEXT,
                            FOLLOW_OBJECT_TEXT,
                            FOLLOW_NONE_TEXT
                        }));
                        this.followComboBox.setToolTipText("Set profile behavior");
                        this.followComboBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                followComboBoxActionPerformed(e);
                            }
                        });
                        this.panel7.add(this.followComboBox, BorderLayout.CENTER);
                    }
                    this.panel6.add(this.panel7);
                }
                this.panel2.add(this.panel6);
            }
            this.panel1.add(this.panel2, BorderLayout.NORTH);

            //======== panel3 ========
            {
                this.panel3.setLayout(new GridLayout(1, 3, 10, 10));

                //---- proportionalCheckBox ----
                this.proportionalCheckBox.setText("Proportional");
                this.proportionalCheckBox.setToolTipText("Maintain 1:1 profile dimensions");
                this.proportionalCheckBox.setAlignmentX(0.5F);
                this.proportionalCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        proportionalCheckBoxItemStateChanged(e);
                    }
                });
                this.panel3.add(this.proportionalCheckBox);

                //---- showEyeCheckBox ----
                this.showEyeCheckBox.setText("Show A/C Position");
                this.showEyeCheckBox.setToolTipText("Show aircraft position in profile graph");
                this.showEyeCheckBox.setAlignmentX(0.5F);
                this.showEyeCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
                this.showEyeCheckBox.setSelected(true);
                this.showEyeCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        showEyeCheckBoxItemStateChanged(e);
                    }
                });
                this.panel3.add(this.showEyeCheckBox);

                //---- zeroBaseCheckBox ----
                this.zeroBaseCheckBox.setText("MSL Base");
                this.zeroBaseCheckBox.setToolTipText("Show mean sea level in profile graph");
                this.zeroBaseCheckBox.setAlignmentX(0.5F);
                this.zeroBaseCheckBox.setHorizontalAlignment(SwingConstants.TRAILING);
                this.zeroBaseCheckBox.setSelected(true);
                this.zeroBaseCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        zeroBaseCheckBoxItemStateChanged(e);
                    }
                });
                this.panel3.add(this.zeroBaseCheckBox);
            }
            this.panel1.add(this.panel3, BorderLayout.CENTER);

            //======== panel8 ========
            {
                this.panel8.setLayout(new GridLayout(2, 2, 20, 10));
                String[] profileSizeValues = new String[]{"x1", "x2", "x3", "x4", "x5", "x7", "x10"};

                //======== panel4 ========
                {
                    this.panel4.setBorder(BorderFactory.createEmptyBorder());
                    this.panel4.setLayout(new BorderLayout(5, 0));

                    //---- profileWidthSpinner ----
                    this.profileWidthSpinner.setToolTipText("Profile width");
                    this.profileWidthSpinner.setBorder(new EmptyBorder(5, 5, 5, 5));
                    this.profileWidthSpinner.setAlignmentX(1.5F);
                    this.profileWidthSpinner.setModel(new SpinnerListModel(profileSizeValues));
                    this.profileWidthSpinner.setValue("x1");
                    this.profileWidthSpinner.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            profileWidthSpinnerStateChanged(e);
                        }
                    });
                    this.panel4.add(this.profileWidthSpinner, BorderLayout.CENTER);

                    //---- profileWidthLabel ----
                    this.profileWidthLabel.setText("Profile Width:");
                    this.panel4.add(this.profileWidthLabel, BorderLayout.WEST);
                }
                this.panel8.add(this.panel4);

                //======== panel4b ========
                {
                    this.panel4b.setBorder(BorderFactory.createEmptyBorder());
                    this.panel4b.setLayout(new BorderLayout(5, 0));

                    //---- profileLengthSpinner ----
                    this.profileLengthSpinner.setToolTipText("Profile Length along track");
                    this.profileLengthSpinner.setBorder(new EmptyBorder(5, 5, 5, 5));
                    this.profileLengthSpinner.setAlignmentX(1.5F);
                    this.profileLengthSpinner.setModel(new SpinnerListModel(profileSizeValues));
                    this.profileLengthSpinner.setValue("x1");
                    this.profileLengthSpinner.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            profileLengthSpinnerStateChanged(e);
                        }
                    });
                    this.panel4b.add(this.profileLengthSpinner, BorderLayout.CENTER);

                    //---- profileLengthLabel ----
                    this.profileLengthLabel.setText("Profile Length:");
                    this.panel4b.add(this.profileLengthLabel, BorderLayout.WEST);
                }
                this.panel8.add(this.panel4b);

                //======== panel4c ========
                {
                    this.panel4c.setBorder(BorderFactory.createEmptyBorder());
                    this.panel4c.setLayout(new BorderLayout(5, 0));

                    //---- profileSameSize ----
                    this.profilesSameSize.setText("Same size");
                    this.profilesSameSize.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            profilesSameSizeStateChanged(e);
                        }
                    });
                    this.panel4c.add(this.profilesSameSize, BorderLayout.CENTER);
                }
                this.panel8.add(this.panel4c);

                //======== panel4d ========
                {
                    this.panel4d.setBorder(BorderFactory.createEmptyBorder());
                    this.panel4d.setLayout(new BorderLayout(5, 0));

                    //---- profileFollowPath ----
                    this.profileFollowPath.setText("Whole track length");
                    this.profileFollowPath.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            profileFollowPathStateChanged(e);
                        }
                    });
                    this.panel4d.add(this.profileFollowPath, BorderLayout.CENTER);
                }
                this.panel8.add(this.panel4d);

            }
            this.panel1.add(this.panel8, BorderLayout.SOUTH);
        }
        add(this.panel1, BorderLayout.CENTER);
    }

    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel6;
    private JPanel panel5;
    private JComboBox sizeComboBox;
    private JPanel panel7;
    private JComboBox followComboBox;
    private JPanel panel3;
    private JCheckBox proportionalCheckBox;
    private JCheckBox showEyeCheckBox;
    private JCheckBox zeroBaseCheckBox;
    private JPanel panel4;
    private JSpinner profileWidthSpinner;
    private JLabel profileWidthLabel;
    private JPanel panel8;
    private JPanel panel4b;
    private JSpinner profileLengthSpinner;
    private JLabel profileLengthLabel;
    private JPanel panel4c;
    private JCheckBox profilesSameSize;
    private JPanel panel4d;
    private JCheckBox profileFollowPath;

    private static class TerrainProfileController {

        private static final HashMap<String, Dimension> sizes = new HashMap<String, Dimension>();

        public static String[] getSizeKeys() {
            return sizes.keySet().toArray(new String[1]);
        }

        static {
            sizes.put(GRAPH_SIZE_SMALL_TEXT, new Dimension(250, 100));
            sizes.put(GRAPH_SIZE_MEDIUM_TEXT, new Dimension(450, 140));
            sizes.put(GRAPH_SIZE_LARGE_TEXT, new Dimension(655, 240));
        }

        private static final HashMap<String, String> follows = new HashMap<String, String>();

        public static String[] getFollowKeys() {
            return follows.keySet().toArray(new String[1]);
        }

        static {
            follows.put(FOLLOW_VIEW_TEXT, TerrainProfileLayer.FOLLOW_VIEW);
            follows.put(FOLLOW_CURSOR_TEXT, TerrainProfileLayer.FOLLOW_CURSOR);
            //follows.put(FOLLOW_EYE_TEXT, TerrainProfileLayer.FOLLOW_EYE);
            follows.put(FOLLOW_OBJECT_TEXT, TerrainProfileLayer.FOLLOW_OBJECT);
            follows.put(FOLLOW_NONE_TEXT, TerrainProfileLayer.FOLLOW_NONE);
        }

        private WorldWindow wwd;
        private TerrainProfileLayer tpl;  // Perpendicular to track
        private TerrainProfileLayer tpl2; // Parallel to the track
        private boolean wholeTrackLength = false;

        public TerrainProfileController() {
            this.tpl = new TerrainProfileLayer();
            this.tpl.setZeroBased(true);
            this.tpl2 = new TerrainProfileLayer();
            this.tpl2.setZeroBased(true);
            this.tpl2.setPosition(AVKey.SOUTHEAST);
        }

        public WorldWindow getWwd() {
            return wwd;
        }

        public void setWwd(WorldWindow wwd) {
            this.wwd = wwd;
            if (this.wwd != null) {
                ApplicationTemplate.insertBeforeCompass(wwd, tpl);
                this.tpl.setEventSource(wwd);
                ApplicationTemplate.insertBeforeCompass(wwd, tpl2);
                this.tpl2.setEventSource(wwd);
                this.tpl2.setPathType(AVKey.RHUMB_LINE);
                // Move scalebar to north west
                for (Layer layer : wwd.getModel().getLayers()) {
                    if (layer instanceof ScalebarLayer) {
                        ((ScalebarLayer) layer).setPosition(AVKey.NORTHWEST);
                    }
                }
                update();
            }
        }

        private void update() {
            if (this.wwd != null) {
                this.wwd.redraw();
            }
        }

        public void setShowEyePosition(boolean showEye) {
            this.tpl.setShowEyePosition(showEye);
            this.tpl2.setShowEyePosition(showEye);
            this.update();
        }

        public boolean getShowEyePosition() {
            return this.tpl.getShowEyePosition();
        }

        public void setZeroBased(boolean keepProportions) {
            this.tpl.setZeroBased(keepProportions);
            this.tpl2.setZeroBased(keepProportions);
            this.update();
        }

        public boolean getShowZeroBased() {
            return this.tpl.getZeroBased();
        }

        public void setKeepProportions(boolean keepProportions) {
            this.tpl.setKeepProportions(keepProportions);
            this.tpl2.setKeepProportions(keepProportions);
            this.update();
        }

        public boolean getKeepProportions() {
            return this.tpl.getKeepProportions();
        }

        public void setProfileSize(String size) {
            Dimension dim = sizes.get(size);
            if (dim != null) {
                this.tpl.setSize(dim);
                this.tpl2.setSize(dim);
                this.update();
            }
        }

        public Dimension getProfileSize() {
            return this.tpl.getSize();
        }

        public void setFollow(String followName) {
            String follow = follows.get(followName);
            if (follow != null) {
                this.tpl.setFollow(follow);
                if (follow.equals(TerrainProfileLayer.FOLLOW_OBJECT) || follow.equals(TerrainProfileLayer.FOLLOW_EYE)) {
                    this.tpl2.setFollow(
                            this.wholeTrackLength ? TerrainProfileLayer.FOLLOW_PATH : TerrainProfileLayer.FOLLOW_OBJECT);
                } else {
                    this.tpl2.setFollow(TerrainProfileLayer.FOLLOW_NONE);
                }
                this.update();
            }
        }

        public String getFollow() {
            return this.tpl.getFollow();
        }

        public void setWholeTrackLength(boolean state) {
            if (this.wholeTrackLength != state) {
                this.wholeTrackLength = state;
                if (!this.tpl2.getFollow().equals(TerrainProfileLayer.FOLLOW_NONE)) {
                    this.tpl2.setFollow(
                            this.wholeTrackLength ? TerrainProfileLayer.FOLLOW_PATH : TerrainProfileLayer.FOLLOW_OBJECT);
                }
                this.update();
            }
        }

        public void setProfileWidthFactor(double factor) {
            this.tpl.setProfileLengthFactor(factor);  // perpendicular profile
            this.update();
        }

        public void setProfileLengthFactor(double factor) {
            this.tpl2.setProfileLengthFactor(factor);   // along track rofile
            this.update();
        }

        public double getProfileWidthFactor() {
            return this.tpl.getProfileLenghtFactor();
        }

        public double getProfileLengthFactor() {
            return this.tpl2.getProfileLenghtFactor();
        }

        public void updatePosition(Position position, Angle heading) {
            this.tpl.setObjectPosition(position);
            this.tpl.setObjectHeading(heading);
            this.tpl2.setObjectPosition(position);
            this.tpl2.setObjectHeading(heading.addDegrees(-90));
            this.update();
        }

        public void updatePath(ArrayList<? extends LatLon> positions) {
            this.tpl2.setPathPositions(positions);
            this.update();
        }
    }

    // *** Restorable interface ***
    public String getRestorableState() {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        this.doGetRestorableState(rs, null);

        return rs.getStateAsXml();
    }

    public void restoreState(String stateInXml) {
        if (stateInXml == null) {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try {
            rs = RestorableSupport.parse(stateInXml);
        } catch (Exception e) {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }

        this.doRestoreState(rs, null);
    }

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context) {
        // Add state values
        rs.addStateValueAsString(context, "size", (String) this.sizeComboBox.getSelectedItem());
        rs.addStateValueAsString(context, "follow", (String) this.followComboBox.getSelectedItem());
        rs.addStateValueAsBoolean(context, "proportional", this.proportionalCheckBox.isSelected());
        rs.addStateValueAsBoolean(context, "zeroBase", this.zeroBaseCheckBox.isSelected());
        rs.addStateValueAsBoolean(context, "showEye", this.showEyeCheckBox.isSelected());
        rs.addStateValueAsString(context, "width", (String) this.profileWidthSpinner.getValue());
        rs.addStateValueAsString(context, "length", (String) this.profileLengthSpinner.getValue());
        rs.addStateValueAsBoolean(context, "sameSize", this.profilesSameSize.isSelected());
        rs.addStateValueAsBoolean(context, "followPath", this.profileFollowPath.isSelected());
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context) {
        // Retrieve state values
        String sizeState = rs.getStateValueAsString(context, "size");
        if (sizeState != null) {
            this.sizeComboBox.setSelectedItem(sizeState);
        }

        String followState = rs.getStateValueAsString(context, "follow");
        if (followState != null) {
            this.followComboBox.setSelectedItem(followState);
        }

        Boolean proportionalState = rs.getStateValueAsBoolean(context, "proportional");
        if (proportionalState != null) {
            this.proportionalCheckBox.setSelected(proportionalState);
        }

        Boolean zeroBaseState = rs.getStateValueAsBoolean(context, "zeroBase");
        if (zeroBaseState != null) {
            this.zeroBaseCheckBox.setSelected(zeroBaseState);
        }

        Boolean showEyeState = rs.getStateValueAsBoolean(context, "showEye");
        if (showEyeState != null) {
            this.showEyeCheckBox.setSelected(showEyeState);
        }

        String widthState = rs.getStateValueAsString(context, "width");
        if (widthState != null) {
            this.profileWidthSpinner.setValue(widthState);
        }

        String lengthState = rs.getStateValueAsString(context, "length");
        if (lengthState != null) {
            this.profileLengthSpinner.setValue(lengthState);
        }

        Boolean sameSizeState = rs.getStateValueAsBoolean(context, "sameSize");
        if (sameSizeState != null) {
            this.profilesSameSize.setSelected(sameSizeState);
        }

        Boolean followPathState = rs.getStateValueAsBoolean(context, "followPath");
        if (followPathState != null) {
            this.profileFollowPath.setSelected(followPathState);
        }
    }

}
