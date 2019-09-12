/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * @author tag
 * @version $Id: TrackViewPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TrackViewPanel extends JPanel
{
    // SAR logical components.
    private AnalysisPanel analysisPanel;
    private SARTrack sarTrack;
    private PropertyChangeListener trackPropertyChangeListener;
    private String elevationUnit;
    private String angleFormat;
    // Viewing mode
    private String viewMode = VIEW_MODE_FREE;
    // "Position" panel components
    private boolean suspendPositionEvents = false;
    private double positionDelta = 0;
    private JLabel latLabel;
    private JLabel lonLabel;
    private JLabel altLabel;
    private JLabel latReadout;
    private JLabel lonReadout;
    private JLabel altReadout;
    private JSpinner positionSpinner;
    private JSlider positionSlider;
    private JButton fastReverseButton;
    private JButton reverseButton;
    private JButton stopButton;
    private JButton forwardButton;
    private JButton fastForwardButton;
    private JLabel speedLabel;
    private JSpinner speedSpinner;
    private JSpinner speedFactorSpinner;
    // "Player" logical components.
    private static final int PLAY_FORWARD = 1;
    private static final int PLAY_BACKWARD = -1;
    private static final int PLAY_STOP = 0;
    private int playMode = PLAY_STOP;
    private Timer player;
    private long previousStepTime = -1;

    public static final String POSITION_CHANGE = "TrackViewPanel.PositionChange";
    public static final String VIEW_CHANGE = "TrackViewPanel.ViewChange";
    public static final String VIEW_MODE_CHANGE = "TrackViewPanel.ViewModeChange";
    public static final String VIEW_MODE_EXAMINE = "TrackViewPanel.ViewModeExamine";
    public static final String VIEW_MODE_FOLLOW = "TrackViewPanel.ViewModeFollow";
    public static final String VIEW_MODE_FREE = "TrackViewPanel.ViewModeFree";
    public static final String SHOW_TRACK_INFORMATION = "TrackViewPanel.ShowTrackInformation";
    public static final String CURRENT_SEGMENT = "TrackViewPanel.CurrentSegment";

    public TrackViewPanel(AnalysisPanel analysisPanel)
    {
        this.analysisPanel = analysisPanel;
        initComponents();
        this.updateEnabledState();
        this.trackPropertyChangeListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (event.getPropertyName().equals(TrackController.TRACK_MODIFY))
                {
                    updatePositionList(false);
                }
            }
        };
    }

    public WorldWindow getWwd()
    {
        return this.analysisPanel.getWwd();
    }

    public void setCurrentTrack(SARTrack sarTrack)
    {
        if (this.sarTrack != null)
        {
            this.sarTrack.removePropertyChangeListener(this.trackPropertyChangeListener);
        }
        this.sarTrack = sarTrack;
        if (this.sarTrack != null)
        {
            this.sarTrack.addPropertyChangeListener(this.trackPropertyChangeListener);
        }

        this.updatePositionList(true);
        this.updateEnabledState();
    }

    public String getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(String elevationUnit)
    {
        this.elevationUnit = elevationUnit;
    }

    public String getAngleFormat()
    {
        return this.angleFormat;
    }

    public void setAngleFormat(String format)
    {
        this.angleFormat = format;
    }

    public String getViewMode()
    {
        return this.viewMode;
    }

    public void setViewMode(String viewMode)
    {
        if (this.viewMode.equals(viewMode))
            return;
        this.viewMode = viewMode;
        this.firePropertyChange(VIEW_CHANGE, -1, 0);
    }

    private void updatePositionList(boolean resetPosition)
    {
        String[] strings = new String[this.sarTrack != null ? this.sarTrack.size() : 0];

        for (int i = 0; i < strings.length; i++)
        {
            strings[i] = String.format("%,4d", i);
        }

        if (strings.length == 0)
            strings = new String[] {"   0"};

        int currentPosition = Math.min(this.getCurrentPositionNumber(), strings.length - 1);
        int currentSliderValue = this.positionSlider.getValue();
        this.positionSpinner.setModel(new SpinnerListModel(strings));
        this.positionSpinner.setValue(resetPosition ? strings[0] : strings[currentPosition]);
        this.positionSlider.setValue(resetPosition ? 0 : currentSliderValue);
    }

    private void setPositionSpinnerNumber(int n)
    {
        this.positionSpinner.setValue(String.format("%,4d", n));
    }

    private void updateEnabledState()
    {
        boolean state = this.sarTrack != null;

        this.positionSpinner.setEnabled(state);
        this.positionSlider.setEnabled(state);
        this.latLabel.setEnabled(state);
        this.lonLabel.setEnabled(state);
        this.altLabel.setEnabled(state);

        this.fastReverseButton.setEnabled(state);
        this.reverseButton.setEnabled(state);
//        this.stopButton.setEnabled(state);
        this.forwardButton.setEnabled(state);
        this.fastForwardButton.setEnabled(state);
        this.speedLabel.setEnabled(state);
        this.speedSpinner.setEnabled(state);
        this.speedFactorSpinner.setEnabled(state);

        this.updateReadout(this.sarTrack != null && sarTrack.size() > 0 ? sarTrack.get(0) : null);
    }

    private void positionSpinnerStateChanged()
    {
        if (!this.suspendPositionEvents)
        {
            setPositionDelta(getCurrentPositionNumber(), 0);
            this.firePropertyChange(POSITION_CHANGE, -1, 0);
        }
    }

    private void positionSliderStateChanged()
    {
        if (!this.suspendPositionEvents)
        {
            updatePositionDelta();
            this.firePropertyChange(POSITION_CHANGE, -1, 0);
        }
    }

    public int getCurrentPositionNumber()
    {
        Object o = this.positionSpinner.getValue();
        if (o == null)
            return -1;

        return Integer.parseInt(o.toString().trim().replaceAll(",", ""));
    }

    private boolean isLastPosition(int n)
    {
        return n >= this.sarTrack.size() - 1;
    }

    public double getPositionDelta()
    {
        // Portion of the current segment 0.0 .. 1.0
        return this.positionDelta;
    }

    private void updatePositionDelta()
    {
        // From UI control
        int i = this.positionSlider.getValue();
        int min = this.positionSlider.getMinimum();
        int max = this.positionSlider.getMaximum();
        this.positionDelta = (double) i / ((double) max - (double) min);
    }

    public void gotoTrackEnd()
    {
        if(this.sarTrack != null && this.sarTrack.size() > 0)
        {
            this.setPositionDelta(this.sarTrack.size() - 1, 0);
            this.firePropertyChange(POSITION_CHANGE, -1, 0);
        }
    }

    public void setPositionDelta(int positionNumber, double positionDelta)
    {
        // Update UI controls without firing events
        this.suspendPositionEvents = true;
        {
            setPositionSpinnerNumber(positionNumber);
            int min = this.positionSlider.getMinimum();
            int max = this.positionSlider.getMaximum();
            int value = (int) (min + (double) (max - min) * positionDelta);
            this.positionSlider.setValue(value);
        }
        this.suspendPositionEvents = false;

        this.positionDelta = positionDelta;
    }

    public boolean isExamineViewMode()
    {
        return this.viewMode.equals(VIEW_MODE_EXAMINE);
    }

    public boolean isFollowViewMode()
    {
        return this.viewMode.equals(VIEW_MODE_FOLLOW);
    }

    public boolean isFreeViewMode()
    {
        return this.viewMode.equals(VIEW_MODE_FREE);
    }

    public void updateReadout(Position pos)
    {
        this.latReadout.setText(pos == null ? "" : SAR2.formatAngle(angleFormat, pos.getLatitude()));
        this.lonReadout.setText(pos == null ? "" : SAR2.formatAngle(angleFormat, pos.getLongitude()));

        if (SAR2.UNIT_IMPERIAL.equals(this.elevationUnit))
            this.altReadout.setText(
                pos == null ? "" : String.format("% 8.0f ft", SAR2.metersToFeet(pos.getElevation())));
        else // Default to metric units.
            this.altReadout.setText(pos == null ? "" : String.format("% 8.0f m", pos.getElevation()));

        this.speedLabel.setText(SAR2.UNIT_IMPERIAL.equals(this.elevationUnit) ? "MPH: " : "KMH: ");
    }

    public double getSpeedKMH()
    {
        String speedValue = (String)this.speedSpinner.getValue();
        double speed = Double.parseDouble(speedValue) * getSpeedFactor();
        if (SAR2.UNIT_IMPERIAL.equals(this.elevationUnit))
            speed *= 1.609344; // mph to kmh
        return speed;
    }

    public double getSpeedFactor()
    {
        String speedFactor = ((String)this.speedFactorSpinner.getValue()).replace("x", "");
        return Double.parseDouble(speedFactor);
    }

    // Player Controls

    private void fastReverseButtonActionPerformed()
    {
        if (this.getCurrentPositionNumber() > 0)
            setPositionSpinnerNumber(this.getCurrentPositionNumber() - 1);
    }

    private void reverseButtonActionPerformed()
    {
        setPlayMode(PLAY_BACKWARD);
    }

    private void stopButtonActionPerformed()
    {
        setPlayMode(PLAY_STOP);
    }

    private void forwardButtonActionPerformed()
    {
        setPlayMode(PLAY_FORWARD);
    }

    private void fastForwardButtonActionPerformed()
    {
        if (!isLastPosition(this.getCurrentPositionNumber()))
            setPositionSpinnerNumber(this.getCurrentPositionNumber() + 1);
    }

    public boolean isPlayerActive()
    {
        return this.playMode != PLAY_STOP;
    }

    private void setPlayMode(int mode)
    {
        this.playMode = mode;
        if (player == null)
            initPlayer();
        player.start();
    }

    private void initPlayer()
    {
        if (player != null)
            return;

        player = new Timer(50, new ActionListener()
        {
            // Animate the view motion by controlling the positionSpinner and positionDelta
            public void actionPerformed(ActionEvent actionEvent)
            {
                runPlayer();
            }
        });
    }

    private void runPlayer()
    {
        int positionNumber = getCurrentPositionNumber();
        double curDelta = getPositionDelta();
        double speedKMH = getSpeedKMH();

        if (this.playMode == PLAY_STOP)
        {
            this.stopButton.setEnabled(false);
            this.player.stop();
            this.previousStepTime = -1;
        }
        else if (this.playMode == PLAY_FORWARD)
        {
            this.stopButton.setEnabled(true);
            if (positionNumber >= (this.sarTrack.size() - 1))
            {
                setPositionDelta(this.sarTrack.size() - 1, 0);
                this.playMode = PLAY_STOP;
            }
            else
            {
                double distanceToGo = computeDistanceToGo(speedKMH);
                while (distanceToGo > 0)
                {
                    double segmentLength = this.analysisPanel.getSegmentLength(positionNumber);
                    if (segmentLength * curDelta + distanceToGo <= segmentLength)
                    {
                        // enough space inside this segment
                        curDelta += distanceToGo / segmentLength;
                        setPositionDelta(positionNumber, curDelta);
                        distanceToGo = 0;
                    }
                    else
                    {
                        // move to next segment
                        if (!this.isLastPosition(positionNumber + 1))
                        {
                            distanceToGo -= segmentLength * (1d - curDelta);
                            positionNumber++;
                            curDelta = 0;
                        }
                        else
                        {
                            // reached end of track
                            setPositionDelta(positionNumber + 1, 0);
                            this.playMode = PLAY_STOP;
                            break;
                        }
                    }
                }
                this.firePropertyChange(POSITION_CHANGE, -1, 0);
            }
        }
        else if (this.playMode == PLAY_BACKWARD)
        {
            this.stopButton.setEnabled(true);
            if (positionNumber <= 0 && curDelta <= 0)
            {
                setPositionDelta(0, 0);
                this.playMode = PLAY_STOP;
            }
            else
            {
                double distanceToGo = computeDistanceToGo(speedKMH);
                while (distanceToGo > 0)
                {
                    double segmentLength = this.analysisPanel.getSegmentLength(positionNumber);
                    if (segmentLength * curDelta - distanceToGo >= 0)
                    {
                        // enough space inside this segment
                        curDelta -= distanceToGo / segmentLength;
                        setPositionDelta(positionNumber, curDelta);
                        distanceToGo = 0;
                    }
                    else
                    {
                        // move to previous segment
                        if (positionNumber > 0)
                        {
                            distanceToGo -= segmentLength * curDelta;
                            positionNumber--;
                            curDelta = 1;
                        }
                        else
                        {
                            // reached start of track
                            setPositionDelta(0, 0);
                            this.playMode = PLAY_STOP;
                            break;
                        }
                    }
                }
                this.firePropertyChange(POSITION_CHANGE, -1, 0);
            }
        }
    }

    private double computeDistanceToGo(double speedKMH)
    {
        long stepTime = System.nanoTime();
        double distance = 0;
        if (this.previousStepTime > 0)
        {
            double ellapsedMillisec = (stepTime - this.previousStepTime) / 1e6;
            distance = speedKMH / 3600d * ellapsedMillisec; // meters
        }
        this.previousStepTime = stepTime;
        return distance;
    }

    private void initComponents()
    {
        //======== this ========
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //======== "Position" Section ========
        Box positionPanel = Box.createVerticalBox();
        {
            //======== Position Readout ========
            JPanel readoutPanel = new JPanel(new GridLayout(1, 3, 0, 0)); // nrows, ncols, hgap, vgap
            readoutPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                //======== Latitude ========
                Box lat = Box.createHorizontalBox();
                {
                    lat.add(Box.createHorizontalGlue());
                    this.latLabel = new JLabel();
                    this.latLabel.setText("Lat:");
                    lat.add(this.latLabel);
                    lat.add(Box.createHorizontalStrut(3));

                    this.latReadout = new JLabel();
                    this.latReadout.setText("-90.0000");
                    lat.add(this.latReadout);
                    lat.add(Box.createHorizontalGlue());
                }
                readoutPanel.add(lat);

                //======== Longitude ========
                Box lon = Box.createHorizontalBox();
                {
                    lon.add(Box.createHorizontalGlue());
                    this.lonLabel = new JLabel();
                    this.lonLabel.setText("Lon:");
                    lon.add(this.lonLabel);
                    lon.add(Box.createHorizontalStrut(3));

                    //---- lonReadout ----
                    this.lonReadout = new JLabel();
                    this.lonReadout.setText("-180.0000");
                    lon.add(this.lonReadout);
                    lon.add(Box.createHorizontalGlue());
                }
                readoutPanel.add(lon);

                //======== Altitude ========
                Box alt = Box.createHorizontalBox();
                {
                    alt.add(Box.createHorizontalGlue());
                    this.altLabel = new JLabel();
                    this.altLabel.setText("Alt:");
                    alt.add(this.altLabel);
                    alt.add(Box.createHorizontalStrut(3));

                    this.altReadout = new JLabel();
                    this.altReadout.setText("50,000.000");
                    alt.add(this.altReadout);
                    alt.add(Box.createHorizontalGlue());
                }
                readoutPanel.add(alt);
            }
            positionPanel.add(readoutPanel);
            positionPanel.add(Box.createVerticalStrut(16));

            //======== Position Spinner, Slider ========
            Box positionControlPanel = Box.createHorizontalBox();
            positionControlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                //---- Position Spinner ----
                this.positionSpinner = new JSpinner();
                this.positionSpinner.setModel(new SpinnerListModel(new String[] {"   0"}));
                this.positionSpinner.setEnabled(false);
                Dimension size = new Dimension(50, this.positionSpinner.getPreferredSize().height);
                this.positionSpinner.setMinimumSize(size);
                this.positionSpinner.setPreferredSize(size);
                this.positionSpinner.setMaximumSize(size);
                this.positionSpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        positionSpinnerStateChanged();
                    }
                });
                positionControlPanel.add(this.positionSpinner, BorderLayout.WEST);
                positionControlPanel.add(Box.createHorizontalStrut(10));

                //---- Position Slider ----
                this.positionSlider = new JSlider();
                this.positionSlider.setMaximum(1000);
                this.positionSlider.setValue(0);
                this.positionSlider.setEnabled(false);
                this.positionSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        positionSliderStateChanged();
                    }
                });
                positionControlPanel.add(this.positionSlider, BorderLayout.CENTER);
            }
            positionPanel.add(positionControlPanel);
            positionPanel.add(Box.createVerticalStrut(16));

            //======== "VCR" Panel ========
            Box vcrPanel = Box.createHorizontalBox();
            vcrPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                vcrPanel.add(Box.createHorizontalGlue());
                //---- "<<" Button ----
                this.fastReverseButton = new JButton();
                this.fastReverseButton.setText("<<");
                this.fastReverseButton.setEnabled(false);
                this.fastReverseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        fastReverseButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.fastReverseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- "<" Button----
                this.reverseButton = new JButton();
                this.reverseButton.setText("<");
                this.reverseButton.setEnabled(false);
                this.reverseButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        reverseButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.reverseButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- "Stop" Button ----
                this.stopButton = new JButton();
                this.stopButton.setText("Stop");
                this.stopButton.setEnabled(false);
                this.stopButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        stopButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.stopButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">" Button ----
                this.forwardButton = new JButton();
                this.forwardButton.setText(">");
                this.forwardButton.setBorder(UIManager.getBorder("Button.border"));
                this.forwardButton.setEnabled(false);
                this.forwardButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        forwardButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.forwardButton);
                vcrPanel.add(Box.createHorizontalStrut(3));

                //---- ">>" Button ----
                this.fastForwardButton = new JButton();
                this.fastForwardButton.setText(">>");
                this.fastForwardButton.setEnabled(false);
                this.fastForwardButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        fastForwardButtonActionPerformed();
                    }
                });
                vcrPanel.add(this.fastForwardButton);

                //--------
                vcrPanel.add(Box.createHorizontalGlue());
            }
            positionPanel.add(vcrPanel);
            positionPanel.add(Box.createVerticalStrut(16));

            //======== "Speed" Panel ========
            Box speedPanel = Box.createHorizontalBox();
            speedPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            {
                speedPanel.add(Box.createHorizontalGlue());
                //---- "Speed:" Label ----
                this.speedLabel = new JLabel();
                this.speedLabel.setText("Speed:");
                speedPanel.add(this.speedLabel);
                speedPanel.add(Box.createHorizontalStrut(10));

                //---- Speed Spinner ----
                int numValues = 100;
                String[] speedValues = new String[numValues];
                for (int i = 1; i <= numValues; i++)
                {
                    speedValues[i - 1] = "" + (i * 10);
                }
                this.speedSpinner = new JSpinner();
                this.speedSpinner.setModel(new SpinnerListModel(speedValues));
                this.speedSpinner.setValue("200");
                this.speedSpinner.setEnabled(false);
                Dimension size = new Dimension(60, this.speedSpinner.getPreferredSize().height);
                this.speedSpinner.setMinimumSize(size);
                this.speedSpinner.setPreferredSize(size);
                this.speedSpinner.setMaximumSize(size);
                speedPanel.add(this.speedSpinner);
                speedPanel.add(Box.createHorizontalStrut(10));

                //---- Speed Multiplier Spinner ----
                this.speedFactorSpinner = new JSpinner();
                this.speedFactorSpinner.setModel(new SpinnerListModel(
                    new String[] {"x.12", "x.25", "x.50", "x1", "x2", "x3", "x4", "x5", "x7", "x10"}));
                this.speedFactorSpinner.setValue("x1");
                this.speedFactorSpinner.setEnabled(false);
                size = new Dimension(60, this.speedFactorSpinner.getPreferredSize().height);
                this.speedFactorSpinner.setMinimumSize(size);
                this.speedFactorSpinner.setPreferredSize(size);
                this.speedFactorSpinner.setMaximumSize(size);
                speedPanel.add(this.speedFactorSpinner);
                speedPanel.add(Box.createHorizontalGlue());
            }
            positionPanel.add(speedPanel);
            positionPanel.add(Box.createVerticalGlue());
        }
        positionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(positionPanel);
        this.add(Box.createVerticalGlue());
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
        rs.addStateValueAsInteger(context, "positionNumber", this.getCurrentPositionNumber());
        rs.addStateValueAsDouble(context, "positionDelta", this.getPositionDelta());
        rs.addStateValueAsString(context, "viewMode", this.getViewMode());
        rs.addStateValueAsString(context, "speed", (String)this.speedSpinner.getValue());
        rs.addStateValueAsString(context, "speedFactor", (String)this.speedFactorSpinner.getValue());
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        // Retrieve state values
        Integer positionNumberState = rs.getStateValueAsInteger(context, "positionNumber");
        Double positionDeltaState = rs.getStateValueAsDouble(context, "positionDelta");
        if (positionNumberState != null && positionDeltaState != null)
            this.setPositionDelta(positionNumberState, positionDeltaState);

        String speedState = rs.getStateValueAsString(context, "speed");
        if (speedState != null)
            this.speedSpinner.setValue(speedState);

        String speedFactorState = rs.getStateValueAsString(context, "speedFactor");
        if (speedFactorState != null)
            this.speedFactorSpinner.setValue(speedFactorState);

        String viewModeState = rs.getStateValueAsString(context, "viewMode");
        if (viewModeState != null)
        {
            this.viewMode = viewModeState;
            // Update analysis panel
            this.firePropertyChange(VIEW_CHANGE, -1, 0);
            // Update tool bar
            this.getWwd().firePropertyChange(TrackViewPanel.VIEW_MODE_CHANGE, null, viewModeState);
        }
    }


}
