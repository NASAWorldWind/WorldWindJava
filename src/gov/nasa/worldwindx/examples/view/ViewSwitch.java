/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.view;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwindx.examples.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

/**
 * This example demonstrates the difference between {@link gov.nasa.worldwind.view.orbit.BasicOrbitView} and {@link
 * gov.nasa.worldwind.view.firstperson.BasicFlyView} by allowing the user to select the preferred view type and then
 * enter the name or lat-lon coordinates of a new location to move the view to.  Press the enter key and watch WorldWind
 * smoothly and automatically animate to your new location.
 *
 * @author jym
 * @version $Id: ViewSwitch.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ViewSwitch extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame implements ActionListener
    {
        @SuppressWarnings("unchecked")
        public class ViewDisplay extends JPanel implements PositionListener, RenderingListener, ActionListener
        {

            // Units constants
            public final static String UNIT_METRIC = "gov.nasa.worldwind.StatusBar.Metric";
            public final static String UNIT_IMPERIAL = "gov.nasa.worldwind.StatusBar.Imperial";
            //private final static double METER_TO_FEET = 3.280839895;
            private final static double METER_TO_MILE = 0.000621371192;

            private WorldWindow eventSource;
            protected final JLabel latDisplay = new JLabel("");
            protected final JLabel lonDisplay = new JLabel(Logging.getMessage("term.OffGlobe"));
            protected final JLabel eleDisplay = new JLabel("");
            protected final JLabel headingDisplay = new JLabel("");
            protected final JLabel pitchDisplay = new JLabel("");
            private String elevationUnit = UNIT_METRIC;
            private String angleFormat = Angle.ANGLE_FORMAT_DD;

            // Viewer class information.  view and inputHandler member variables are lazy initialized.
            // Constructor gets the class names for the View, ViewInputHandler pair.
            public class ViewerClass
            {
                protected String viewClassName;
                protected String inputHandlerClassName;
                protected View view;
                protected ViewInputHandler viewInputHandler;

                ViewerClass(String viewClassName, String inputHandlerClassName)
                {
                    this.viewClassName = viewClassName;
                    this.inputHandlerClassName = inputHandlerClassName;
                    this.view = null;
                    this.viewInputHandler = null;
                }
            }

            // Maps the combo box label to class information.
            public class ViewerClassMap extends HashMap<String, ViewerClass>
            {
            }

            public ViewerClassMap classNameList = new ViewerClassMap();

            // Orbit view class information
            public ViewerClass orbitViewer = new ViewerClass(
                "gov.nasa.worldwind.view.orbit.BasicOrbitView",
                "gov.nasa.worldwind.view.orbit.OrbitViewInputHandler");
            // Fly viewer class information
            public ViewerClass flyViewer = new ViewerClass(
                "gov.nasa.worldwind.view.firstperson.BasicFlyView",
                "gov.nasa.worldwind.view.firstperson.FlyViewInputHandler");

            // Viewer class array used for loop that initializes the map.
            ViewerClass[] viewerClasses =
                {
                    flyViewer,
                    orbitViewer
                };

            // Viewer names for the combo box
            String[] viewerNames = {"Fly", "Orbit"};
            String currentName;
            DefaultComboBoxModel viewerClassNames;
            JComboBox viewList;

            // The class currently being used.
            ViewerClass currentViewer = null;

            public ViewDisplay()
            {
                super(new GridLayout(0, 1));

                // Initialize the viewer label -> viewer class map
                for (int i = 0; i < 2; i++)
                {
                    classNameList.put(viewerNames[i], viewerClasses[i]);
                }

                setViewer(viewerClasses[0], false);
                currentName = viewerNames[0];
                currentViewer = viewerClasses[0];

                // Set up the combo box for choosing viewers
                viewerClassNames = new DefaultComboBoxModel();
                viewList = new JComboBox(viewerNames);
                viewList.addActionListener(this);

                // Set up the viewer parameter display
                headingDisplay.setHorizontalAlignment(SwingConstants.CENTER);
                pitchDisplay.setHorizontalAlignment(SwingConstants.CENTER);
                latDisplay.setHorizontalAlignment(SwingConstants.CENTER);
                lonDisplay.setHorizontalAlignment(SwingConstants.CENTER);
                eleDisplay.setHorizontalAlignment(SwingConstants.CENTER);

                this.add(viewList);
                try
                {
                    this.add(new GazetteerPanel(getWwd(),
                        "gov.nasa.worldwind.poi.YahooGazetteer"), SwingConstants.CENTER);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(this, "Error creating Gazetteer");
                }
                this.add(latDisplay);
                this.add(lonDisplay);
                this.add(eleDisplay);
                this.add(headingDisplay);
                this.add(pitchDisplay);
            }

            public void setViewer(ViewerClass vc, boolean copyValues)
            {
                if (vc.view == null)
                {
                    vc.view = (View) WorldWind.createComponent(vc.viewClassName);
                    vc.viewInputHandler =
                        vc.view.getViewInputHandler();
                }
                if (copyValues)
                {
                    View viewToCopy = getWwd().getView();

                    try
                    {
                        vc.view.copyViewState(viewToCopy);
                        getWwd().setView(vc.view);
                    }
                    catch (IllegalArgumentException iae)
                    {
                        JOptionPane.showMessageDialog(this,
                            "Cannot switch to new view from this position/orientation");
                        viewList.setSelectedItem(currentName);
                    }
                }
                else
                {
                    getWwd().setView(vc.view);
                }
            }

            public void actionPerformed(ActionEvent event)
            {
                if (event.getSource() == viewList)
                {
                    String classLabel = (String) viewList.getSelectedItem();
                    ViewerClass vc = classNameList.get(classLabel);

                    setViewer(vc, true);
                }
            }

            public void moved(PositionEvent event)
            {

            }

            public void setEventSource(WorldWindow newEventSource)
            {
                if (this.eventSource != null)
                {
                    this.eventSource.removePositionListener(this);
                    this.eventSource.removeRenderingListener(this);
                }

                if (newEventSource != null)
                {
                    newEventSource.addPositionListener(this);
                    newEventSource.addRenderingListener(this);
                }

                this.eventSource = newEventSource;
            }

            protected String makeEyeAltitudeDescription(double metersAltitude)
            {
                String s;
                String altitude = Logging.getMessage("term.Altitude");
                if (UNIT_IMPERIAL.equals(elevationUnit))
                    s = String.format(altitude + " %,7d mi", (int) Math.round(metersAltitude * METER_TO_MILE));
                else // Default to metric units.
                    s = String.format(altitude + " %,7d m", (int) Math.round(metersAltitude));
                return s;
            }

            protected String makeAngleDescription(String label, Angle angle)
            {
                String s;
                if (Angle.ANGLE_FORMAT_DMS.equals(angleFormat))
                    s = String.format("%s %s", label, angle.toDMSString());
                else
                    s = String.format("%s %7.4f\u00B0", label, angle.degrees);
                return s;
            }

            public void stageChanged(RenderingEvent event)
            {
                if (!event.getStage().equals(RenderingEvent.BEFORE_BUFFER_SWAP))
                    return;

                EventQueue.invokeLater(new Runnable()
                {
                    public void run()
                    {

                        if (eventSource.getView() != null && eventSource.getView().getEyePosition() != null)
                        {
                            Position newPos = eventSource.getView().getEyePosition();

                            if (newPos != null)
                            {
                                String las = makeAngleDescription("Lat", newPos.getLatitude());
                                String los = makeAngleDescription("Lon", newPos.getLongitude());
                                String heading = makeAngleDescription("Heading", eventSource.getView().getHeading());
                                String pitch = makeAngleDescription("Pitch", eventSource.getView().getPitch());

                                latDisplay.setText(las);
                                lonDisplay.setText(los);
                                eleDisplay.setText(makeEyeAltitudeDescription(
                                    newPos.getElevation()));
                                headingDisplay.setText(heading);
                                pitchDisplay.setText(pitch);
                            }
                            else
                            {
                                latDisplay.setText("");
                                lonDisplay.setText(Logging.getMessage("term.OffGlobe"));
                                eleDisplay.setText("");
                                pitchDisplay.setText("");
                                headingDisplay.setText("");
                            }
                        }
                        else
                        {
                            eleDisplay.setText(Logging.getMessage("term.Altitude"));
                        }
                    }
                });
            }
        }

        ViewDisplay viewDisplay;

        public AppFrame()
        {
            super(true, true, true);
            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);

            viewDisplay.setEventSource(this.getWwd());
        }

        public void actionPerformed(ActionEvent event)
        {

        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    new TitledBorder("View Controls")));
            controlPanel.setToolTipText("Select active view controls");
            viewDisplay = new ViewDisplay();
            controlPanel.add(viewDisplay);

            return (controlPanel);
        }
    }

    public static void main(String[] args)
    {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        ApplicationTemplate.start("World Wind Switch Views", AppFrame.class);
    }
}
