/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.*;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Example of how to keep the view within certain bounds. This example defines a geographic bounding box, and requires
 * the view center position to stay within this box. The view can also be constrained by heading, pitch, and zoom.
 *
 * @author dcollins
 * @version $Id: ViewLimits.java 2259 2014-08-22 23:03:59Z dcollins $
 */
public class ViewLimits extends ApplicationTemplate
{
    protected static final String SECTOR_LIMITS_CHANGED = "SectorLimitsChanged";
    protected static final String HEADING_LIMITS_CHANGED = "HeadingLimitsChanged";
    protected static final String PITCH_LIMITS_CHANGED = "PitchLimitsChanged";
    protected static final String ZOOM_LIMITS_CHANGED = "ZoomLimitsChanged";
    protected static final String SAVE = "Save";
    protected static final String LOAD = "Load";

    public static class AppFrame extends ApplicationTemplate.AppFrame implements ActionListener
    {
        private Controller controller;
        // UI components.
        private JSpinner minLatitude;
        private JSpinner maxLatitude;
        private JSpinner minLongitude;
        private JSpinner maxLongitude;
        private JSpinner minHeading;
        private JSpinner maxHeading;
        private JSpinner minPitch;
        private JSpinner maxPitch;
        private JSpinner minZoom;
        private JSpinner maxZoom;
        private boolean ignoreComponentEvents = false;

        public AppFrame()
        {
            this.controller = new Controller(this);
            this.initComponents();
            this.updateComponents();
        }

        public Sector getSectorLimits()
        {
            return Sector.fromDegrees(
                (Double) this.minLatitude.getValue(), (Double) this.maxLatitude.getValue(),
                (Double) this.minLongitude.getValue(), (Double) this.maxLongitude.getValue());
        }

        public void setSectorLimits(Sector sector)
        {
            if (sector == null)
            {
                String message = Logging.getMessage("nullValue.SectorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.ignoreComponentEvents = true;
            try
            {
                this.minLatitude.setValue(sector.getMinLatitude().degrees);
                this.maxLatitude.setValue(sector.getMaxLatitude().degrees);
                this.minLongitude.setValue(sector.getMinLongitude().degrees);
                this.maxLongitude.setValue(sector.getMaxLongitude().degrees);
            }
            finally
            {
                this.ignoreComponentEvents = false;
            }
        }

        public Angle[] getHeadingLimits()
        {
            return new Angle[]
                {
                    Angle.fromDegrees((Double) this.minHeading.getValue()),
                    Angle.fromDegrees((Double) this.maxHeading.getValue())
                };
        }

        public void setHeadingLimits(Angle min, Angle max)
        {
            if (min == null || max == null)
            {
                String message = Logging.getMessage("nullValue.MinOrMaxAngleIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.ignoreComponentEvents = true;
            try
            {
                this.minHeading.setValue(min.degrees);
                this.maxHeading.setValue(max.degrees);
            }
            finally
            {
                this.ignoreComponentEvents = false;
            }
        }

        public Angle[] getPitchLimits()
        {
            return new Angle[]
                {
                    Angle.fromDegrees((Double) this.minPitch.getValue()),
                    Angle.fromDegrees((Double) this.maxPitch.getValue())
                };
        }

        public void setPitchLimits(Angle min, Angle max)
        {
            if (min == null || max == null)
            {
                String message = Logging.getMessage("nullValue.MinOrMaxAngleIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.ignoreComponentEvents = true;
            try
            {
                this.minPitch.setValue(min.degrees);
                this.maxPitch.setValue(max.degrees);
            }
            finally
            {
                this.ignoreComponentEvents = false;
            }
        }

        public double[] getZoomLimits()
        {
            return new double[]
                {
                    (Double) this.minZoom.getValue(),
                    (Double) this.maxZoom.getValue()
                };
        }

        public void setZoomLimits(double min, double max)
        {
            try
            {
                this.minZoom.setValue(min);
                this.maxZoom.setValue(max);
            }
            finally
            {
                this.ignoreComponentEvents = false;
            }
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            if (this.ignoreComponentEvents)
                return;

            if (this.controller != null)
            {
                this.controller.actionPerformed(actionEvent);
            }
        }

        public void updateComponents()
        {
            OrbitView view = this.controller.getOrbitView();
            if (view == null)
                return;

            OrbitViewLimits limits = view.getOrbitViewLimits();
            if (limits == null)
                return;

            Sector sector = limits.getCenterLocationLimits();
            if (sector != null)
                this.setSectorLimits(sector);

            Angle[] angles = limits.getHeadingLimits();
            if (angles != null)
                this.setHeadingLimits(angles[0], angles[1]);

            angles = limits.getPitchLimits();
            if (angles != null)
                this.setPitchLimits(angles[0], angles[1]);

            double[] values = limits.getZoomLimits();
            if (values != null)
                this.setZoomLimits(values[0], values[1]);
        }

        protected void initComponents()
        {
            this.minLatitude = this.createAngleSpinner(SECTOR_LIMITS_CHANGED, Angle.NEG90, Angle.NEG90, Angle.POS90);
            this.maxLatitude = this.createAngleSpinner(SECTOR_LIMITS_CHANGED, Angle.POS90, Angle.NEG90, Angle.POS90);
            this.minLongitude = this.createAngleSpinner(SECTOR_LIMITS_CHANGED, Angle.NEG180, Angle.NEG180,
                Angle.POS180);
            this.maxLongitude = this.createAngleSpinner(SECTOR_LIMITS_CHANGED, Angle.POS180, Angle.NEG180,
                Angle.POS180);

            this.minHeading = this.createAngleSpinner(HEADING_LIMITS_CHANGED, Angle.NEG180, Angle.NEG180, Angle.POS180);
            this.maxHeading = this.createAngleSpinner(HEADING_LIMITS_CHANGED, Angle.POS180, Angle.NEG180, Angle.POS180);

            this.minPitch = this.createAngleSpinner(PITCH_LIMITS_CHANGED, Angle.NEG180, Angle.NEG180, Angle.POS180);
            this.maxPitch = this.createAngleSpinner(PITCH_LIMITS_CHANGED, Angle.POS180, Angle.NEG180, Angle.POS180);

            this.minZoom = this.createDoubleSpinner(ZOOM_LIMITS_CHANGED, 0, 0, Double.MAX_VALUE);
            this.maxZoom = this.createDoubleSpinner(ZOOM_LIMITS_CHANGED, Double.MAX_VALUE, 0, Double.MAX_VALUE);

            JPanel controlPanel = new JPanel(new BorderLayout(0, 0)); // hgap, vgap
            {
                controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 0)); // top, left, bottom, right

                Box box = Box.createVerticalBox();

                JLabel label = new JLabel("<html><b>Sector Limits</b></html>");
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.add(label);

                Box hbox = Box.createHorizontalBox();
                hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                hbox.add(new JLabel("Lat Min/Max"));
                hbox.add(this.minLatitude);
                hbox.add(this.maxLatitude);
                box.add(hbox);

                hbox = Box.createHorizontalBox();
                hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                hbox.add(new JLabel("Lon Min/Max"));
                hbox.add(this.minLongitude);
                hbox.add(this.maxLongitude);
                box.add(hbox);

                label = new JLabel("<html><b>Heading Limits</b></html>");
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.add(label);

                hbox = Box.createHorizontalBox();
                hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                hbox.add(new JLabel("Min/Max"));
                hbox.add(this.minHeading);
                hbox.add(this.maxHeading);
                box.add(hbox);

                label = new JLabel("<html><b>Pitch Limits</b></html>");
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.add(label);

                hbox = Box.createHorizontalBox();
                hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                hbox.add(new JLabel("Min/Max"));
                hbox.add(this.minPitch);
                hbox.add(this.maxPitch);
                box.add(hbox);

                label = new JLabel("<html><b>Zoom Limits</b></html>");
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                box.add(label);

                hbox = Box.createHorizontalBox();
                hbox.setAlignmentX(Component.LEFT_ALIGNMENT);
                hbox.add(new JLabel("Min/Max"));
                hbox.add(this.minZoom);
                hbox.add(this.maxZoom);
                box.add(hbox);

                controlPanel.add(box, BorderLayout.NORTH);
            }
            this.getControlPanel().add(controlPanel, BorderLayout.SOUTH);

            JMenuBar menuBar = new JMenuBar();
            {
                JMenu menu = new JMenu("File");

                JMenuItem item = new JMenuItem("Open");
                item.setActionCommand(LOAD);
                item.addActionListener(this.controller);
                menu.add(item);

                item = new JMenuItem("Save");
                item.setActionCommand(SAVE);
                item.addActionListener(this.controller);
                menu.add(item);

                menuBar.add(menu);
            }
            this.setJMenuBar(menuBar);
        }

        protected JSpinner createDoubleSpinner(final String actionCommand, double initialValue, double min, double max)
        {
            final JSpinner spinner = new JSpinner(new SpinnerNumberModel(
                initialValue, min, max, 0.01));
            spinner.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    ActionEvent actionEvent = new ActionEvent(spinner, 0, actionCommand);
                    actionPerformed(actionEvent);
                }
            });

            Dimension preferredSize = spinner.getPreferredSize();
            preferredSize.width = 70;
            spinner.setPreferredSize(preferredSize);

            return spinner;
        }

        protected JSpinner createAngleSpinner(final String actionCommand, Angle initialValue, Angle min, Angle max)
        {
            return createDoubleSpinner(actionCommand, initialValue.degrees, min.degrees, max.degrees);
        }
    }

    public static class Controller implements ActionListener
    {
        protected AppFrame appFrame;
        protected SurfaceSector surfaceSector;
        protected RenderableLayer layer;

        protected static final Sector DEFAULT_SECTOR_LIMITS = Sector.fromDegrees(40, 50, -130, -120);

        public Controller(AppFrame appFrame)
        {
            this.appFrame = appFrame;

            this.surfaceSector = new SurfaceSector();
            this.surfaceSector.setPathType(AVKey.LINEAR);
            ShapeAttributes attr = new BasicShapeAttributes();
            attr.setInteriorMaterial(Material.WHITE);
            attr.setOutlineMaterial(Material.GREEN);
            attr.setInteriorOpacity(0.1);
            attr.setOutlineOpacity(0.7);
            attr.setOutlineWidth(3);
            this.surfaceSector.setAttributes(attr);

            this.layer = new RenderableLayer();
            this.layer.setName("Sector Limits");
            this.layer.setPickEnabled(false);
            this.layer.addRenderable(this.surfaceSector);
            insertBeforePlacenames(this.appFrame.getWwd(), this.layer);

            OrbitView view = this.getOrbitView();
            if (view != null)
            {
                view.getOrbitViewLimits().setCenterLocationLimits(DEFAULT_SECTOR_LIMITS);
                view.getOrbitViewLimits().setZoomLimits(0, 20e6);
            }
        }

        @SuppressWarnings( {"StringEquality"})
        public void actionPerformed(ActionEvent actionEvent)
        {
            if (actionEvent == null)
            {
                return;
            }

            String actionCommand = actionEvent.getActionCommand();
            if (actionCommand == null)
            {
                return;
            }

            if (actionCommand == SECTOR_LIMITS_CHANGED
                || actionCommand == HEADING_LIMITS_CHANGED
                || actionCommand == PITCH_LIMITS_CHANGED
                || actionCommand == ZOOM_LIMITS_CHANGED)
            {
                this.updateViewLimits();
            }
            else if (actionCommand == LOAD)
            {
                this.loadObjects();
            }
            else if (actionCommand == SAVE)
            {
                this.saveObjects();
            }
        }

        public void updateViewLimits()
        {
            OrbitView view = this.getOrbitView();
            if (view == null)
                return;

            OrbitViewLimits limits = view.getOrbitViewLimits();
            if (limits == null)
                return;

            Sector sector = this.appFrame.getSectorLimits();
            if (sector != null)
                limits.setCenterLocationLimits(sector);

            Angle[] angles = this.appFrame.getHeadingLimits();
            if (angles != null)
                limits.setHeadingLimits(angles[0], angles[1]);

            angles = this.appFrame.getPitchLimits();
            if (angles != null)
                limits.setPitchLimits(angles[0], angles[1]);

            double[] values = this.appFrame.getZoomLimits();
            if (values != null)
                limits.setZoomLimits(values[0], values[1]);

            this.updateRenderables();
        }

        public void saveObjects()
        {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(Configuration.getUserHomeDirectory()));

            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            int status = fc.showSaveDialog(this.appFrame);
            if (status != JFileChooser.APPROVE_OPTION)
                return;

            File file = fc.getSelectedFile();
            if (file == null)
                return;

            View view = this.appFrame.getWwd().getView();
            String xmlString = view.getRestorableState();

            WWIO.writeTextFile(xmlString, file);
        }

        public void loadObjects()
        {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(Configuration.getUserHomeDirectory()));

            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            int status = fc.showOpenDialog(this.appFrame);
            if (status != JFileChooser.APPROVE_OPTION)
                return;

            File file = fc.getSelectedFile();
            if (file == null)
                return;

            View view = this.appFrame.getWwd().getView();
            String xmlString = WWIO.readTextFile(file);
            view.restoreState(xmlString);

            this.appFrame.updateComponents();
        }

        public OrbitView getOrbitView()
        {
            View view = this.appFrame.getWwd().getView();
            return (view != null && view instanceof OrbitView) ? (OrbitView) view : null;
        }

        public void updateRenderables()
        {
            View view = this.appFrame.getWwd().getView();
            if (view == null || !(view instanceof OrbitView))
            {
                return;
            }

            OrbitView orbitView = (OrbitView) view;
            OrbitViewLimits limits = orbitView.getOrbitViewLimits();

            this.surfaceSector.setSector(limits.getCenterLocationLimits());

            this.appFrame.getWwd().redraw();
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("View Limits", AppFrame.class);
    }
}
