/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.beans.*;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.Point;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.*;
import gov.nasa.worldwind.ogc.collada.*;
import gov.nasa.worldwind.ogc.collada.impl.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.animation.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.geojson.*;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.ScreenRelativeAnnotation;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;
import java.awt.Dimension;
import java.awt.Font;
import gov.nasa.worldwind.ogc.kml.io.*;
import gov.nasa.worldwind.util.Logging;
import javax.xml.stream.XMLStreamException;
import gov.nasa.worldwind.render.airspaces.*;

public class ObjectAnimations extends ApplicationTemplate {

    private static void printFields(String indent, AbstractXMLEventParser node) {
        AVList fields = node.getFields();
        if (fields != null) {
            Set<Map.Entry<String, Object>> fieldSet = fields.getEntries();
            for (Map.Entry<String, Object> e : fieldSet) {
                Object v = e.getValue();
                System.out.println(indent + e.getKey() + " = " + v);
                if (v instanceof AbstractXMLEventParser) {
                    printFields(indent + "  ", (AbstractXMLEventParser) v);
                }
            }
        }
    }

    private static void printIdTable(AbstractXMLEventParser root) {
        Map<String, Object> colladaMap = root.getParserContext().getIdTable();
        for (String key : colladaMap.keySet()) {
            Object v = colladaMap.get(key);
            System.out.println(key + ":" + v);
            if (v instanceof AbstractXMLEventParser) {
                printFields("  ", (AbstractXMLEventParser) v);
            }
        }
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        private final ViewControlPanel vcp;

        private int updateInterval = 500;
        private long lastUpdate;

        public AppFrame() {
            super(true, true, false);

            WorldWindow wwd = this.getWwd();
            // Add view control panel to the layer panel
            this.vcp = new ViewControlPanel(wwd);
            BasicFlyView flyView = new BasicFlyView();
            //getWwd().setView(flyView);
            this.getControlPanel().add(this.vcp, BorderLayout.SOUTH);
//            Position eyePos = new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 25000.0); // DFW
            Position eyePos = new Position(Angle.fromDegreesLatitude(39.52616886908606), Angle.fromDegreesLongitude(-119.81207373509578), 6000.0); // Reno
            View v = wwd.getView();
            v.setEyePosition(eyePos);
            flyView.setEyePosition(eyePos);
            flyView.setHeading(Angle.fromDegrees(0));
            flyView.setPitch(Angle.fromDegrees(90));
            flyView.setFieldOfView(Angle.fromDegrees(45));
            flyView.setRoll(Angle.fromDegrees(0));
            for (Layer layer : wwd.getModel().getLayers()) {
                if (layer.getName().toLowerCase().contains("bing")) {
                    layer.setEnabled(true);
                }
            }
            try {
                //planeModel.setPosition(new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 1500.0));
                AnnotationAttributes infoAttrs = new AnnotationAttributes();
                infoAttrs.setCornerRadius(0);
                infoAttrs.setInsets(new Insets(4, 4, 4, 4));
                infoAttrs.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
                infoAttrs.setTextColor(Color.WHITE);
                infoAttrs.setBorderColor(Color.yellow);
                infoAttrs.setBorderWidth(1);
                infoAttrs.setLeaderGapWidth(4);
                infoAttrs.setDrawOffset(new Point(0, 40));
                RenderableLayer infoLayer = new RenderableLayer();;
                wwd.getModel().getLayers().add(infoLayer);
                //Position.PositionList flightPositions = getPositionsFromKml("testData/KML/dfw-path.kml");
                int nFlights = 2;
                double curAltitude = 2500;
                //AnimatedObjectController controller = new AnimatedObjectController(wwd, layer, infoLayer);
                // ShapeAttributes attrs = new BasicShapeAttributes();
//                attrs.setOutlineMaterial(new Material(Color.RED));
//                attrs.setOutlineWidth(2d);
                //int nPositions = flightPositions.list.size();
                TestParser messageParser = new TestParser();
                messageParser.parseMessages("/home/mpeterson/d/temp/aol-data");
                ArrayList<AOLFlightPlan> plans = messageParser.getPlans();
                final Color[] colorMap = {Color.red, Color.YELLOW, Color.white, Color.blue, Color.CYAN};
                HashMap<String, Color> colors = new HashMap<>();
                HashMap<String, String> callSigns = new HashMap<>();
                int colorIdx = 0;
                RenderableLayer layer = new RenderableLayer();
                for (AOLFlightPlan p : plans) {
                    String key = p.getGufi();
                    if (!colors.containsKey(key)) {
                        colors.put(key, colorMap[colorIdx]);
                        colorIdx++;
                        if (colorIdx >= colorMap.length) {
                            colorIdx = 0;
                        }
                    }

                    if (!callSigns.containsKey(key)) {
                        callSigns.put(key, p.getCallSign());
                    }
                    ArrayList<OperationVolume> opVolumes = p.getOpVolumes();
                    AirspaceAttributes attrs = new BasicAirspaceAttributes();
                    Color color = colors.get(key);
                    attrs.setInteriorMaterial(new Material(color));
                    attrs.setOutlineMaterial(new Material(WWUtil.makeColorBrighter(color)));
                    attrs.setInteriorOpacity(0.7);
                    attrs.setOutlineWidth(2);
                    attrs.setDrawOutline(true);
                    attrs.setEnableAntialiasing(true);
                    attrs.setEnableLighting(true);
                    for (OperationVolume ov : opVolumes) {
                        Polygon poly = new Polygon(attrs);
                        GeoJSONPolygon geo = ov.getFlight_geography();
                        GeoJSONPositionArray[] positions = geo.getCoordinates();
                        poly.setLocations(positions[0]);
                        poly.setAltitudes(ov.getMin_altitude().getAltitudeValue(), ov.getMax_altitude().getAltitudeValue());
                        poly.setAltitudeDatum(AVKey.ABOVE_GROUND_LEVEL, AVKey.ABOVE_GROUND_REFERENCE);
                        poly.setValue(AVKey.DISPLAY_NAME, p.getCallSign());
                        layer.addRenderable(poly);
                    }
                }
                ArrayList<AOLPosition> positions = messageParser.getPositions();
                positions.forEach((p) -> {
                    PointPlacemark pp = new PointPlacemark(p.getLLA());
                    //pp.setLabelText(callSigns.get(p.getGufi()));
                    pp.setValue(AVKey.DISPLAY_NAME, callSigns.get(p.getGufi()));
                    pp.setLineEnabled(false);
                    pp.setAltitudeMode(WorldWind.ABSOLUTE);
                    pp.setEnableLabelPicking(true); // enable label picking for this placemark
                    PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
                    attrs.setImageAddress("/home/mpeterson/d/nasa/WorldWindJava/src/images/plane-icon.png");
                    Color c = colors.get(p.getGufi());
                    c = (c == null) ? Color.MAGENTA : c;
                    attrs.setImageColor(c);
                    //attrs.setScale(0.6);
                    attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
                    pp.setAttributes(attrs);
                    layer.addRenderable(pp);
                });
//                KMLRoot model1 = openKML("/home/mpeterson/d/foo/aol-data/airbus-popup/Airbus_Popup.kmz");
//                KMLRoot model2 = openKML("/home/mpeterson/d/foo/aol-data/octocopter/Octocopter.kmz");
                // KMLRoot model3 = openKML("/home/mpeterson/d/nasa/WorldWindJava/testData/KML/models/macky-normal.kmz");
                //KMLPlacemark pm=(KMLPlacemark) model1.getField("Placemark");
                //KMLModel foo=(KMLModel) pm.getGeometry();
//                model1.setPosition(new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 1500.0));
//                model1.setScale(new Vec4(200, 200, 200));
//                model2.setPosition(new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 2000.0));
//                model2.setScale(new Vec4(1000, 1000, 1000));
//                model3.setPosition(new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 2000.0));
//                model3.setScale(new Vec4(1000, 1000, 1000));
                //System.out.println(foo);
                //KMLController kmlController = new KMLController(model);
//                layer.addRenderable(new KMLController(model1));
//                layer.addRenderable(new KMLController(model2));
//                layer.addRenderable(new KMLController(model3));
//                for (int i = 0; i < nFlights; i++) {
////                    ColladaRoot planeModel = ColladaRoot.createAndParse("testData/collada/airliner.dae");
//                    //ColladaRoot planeModel = ColladaRoot.createAndParse("/home/mpeterson/d/foo/aol-data/airbus-popup/files/model0.dae");
//                    ColladaRoot planeModel = null;
//                    if (i == 0) {
//                        KMLRoot kmzModel = openKML("/home/mpeterson/d/foo/aol-data/airbus-popup/Airbus_Popup.kmz");
//                        System.out.println(kmzModel);
//                    }
//                    //printColladaTable(planeModel);
//                    planeModel.setAltitudeMode(WorldWind.ABSOLUTE);
//                    planeModel.setModelScale(new Vec4(40, 40, 40));
//                    planeModel.setHeading(Angle.ZERO);
//                    AnimatedObject plane = new AnimatedObject(planeModel);
//                    ArrayList<Position> positions3D = new ArrayList<>();
//                    int curPos = 0; // (int) Math.floor(Math.random() * nPositions);
//                    int posDir = 1; //(int) Math.floor(Math.random() * 2);
//                    for (int j = 0; j < nPositions; j++) {
//                        Position p = flightPositions.list.get(curPos);
//                        positions3D.add(new Position(p, curAltitude));
//                        switch (posDir) {
//                            case 1:
//                                curPos++;
//                                if (curPos >= nPositions) {
//                                    curPos = 0;
//                                }
//                                break;
//                            default:
//                                curPos--;
//                                if (curPos < 0) {
//                                    curPos = nPositions - 1;
//                                }
//                                break;
//                        }
//                    }
//                    curAltitude += 500;
//                    Path flightPath = new Path(positions3D);
//                    flightPath.makeClosed();
//                    flightPath.setAltitudeMode(WorldWind.ABSOLUTE);
//                    flightPath.setAttributes(attrs);
//                    plane.setRoute(flightPath);
//                    plane.setVelocity(100 + Math.random() * 150);
//                    plane.setMetadata(String.format("N%4d\n%f MPH", i, ((plane.getVelocity() * 3600) / 1000) * 0.6));
//                    controller.addObject(plane);
//                    layer.addRenderable(flightPath);
//                    layer.addRenderable(new ColladaController(planeModel));
//                }
//
//                this.addFpsText(layer);
//                controller.startAnimations();
                wwd.getModel().getLayers().add(layer);
                wwd.setPerFrameStatisticsKeys(PerformanceStatistic.ALL_STATISTICS_SET);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private KMLRoot openKML(String source) throws Exception {
            KMLRoot kmlRoot = KMLRoot.create(source);
            kmlRoot.parse();
            return kmlRoot;
        }

        private void addFpsText(RenderableLayer layer) {
            AnnotationAttributes fpsAttrs = new AnnotationAttributes();
            fpsAttrs.setBackgroundColor(new Color(0f, 0f, 0f, 0f));
            fpsAttrs.setTextColor(Color.YELLOW);
            fpsAttrs.setLeaderGapWidth(14);
            fpsAttrs.setCornerRadius(0);
            fpsAttrs.setSize(new Dimension(300, 0));
            fpsAttrs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT); // use strict dimension width - 200
            fpsAttrs.setFont(Font.decode("Arial-BOLD-24"));
            fpsAttrs.setBorderWidth(0);
            fpsAttrs.setHighlightScale(1);             // No highlighting either
            fpsAttrs.setCornerRadius(0);

            ScreenRelativeAnnotation fpsText = new ScreenRelativeAnnotation("", 0.5, 0.01);
            fpsText.setKeepFullyVisible(true);
            fpsText.setXMargin(5);
            fpsText.setYMargin(5);
            fpsText.getAttributes().setDefaults(fpsAttrs);
            layer.addRenderable(fpsText);
            WorldWindow wwd = this.getWwd();
            wwd.addRenderingListener(new RenderingListener() {
                @Override
                public void stageChanged(RenderingEvent event) {
                    long now = System.currentTimeMillis();
                    if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP)
                            && event.getSource() instanceof WorldWindow && now - lastUpdate > updateInterval) {
                        EventQueue.invokeLater(() -> {
                            Collection<PerformanceStatistic> pfs = wwd.getPerFrameStatistics();
                            pfs.forEach((perf) -> {
                                if (perf.getKey().equals(PerformanceStatistic.FRAME_RATE)) {
                                    fpsText.setText(perf.getDisplayString() + ": " + perf.getValue());
                                }
                            });
                        });
                        lastUpdate = now;
                    }
                }
            });
        }

        private Position.PositionList getPositionsFromFeature(KMLAbstractFeature feature) {
            if (feature instanceof KMLAbstractContainer) {
                List<KMLAbstractFeature> childFeatures = ((KMLAbstractContainer) feature).getFeatures();
                for (KMLAbstractFeature kaf : childFeatures) {
                    Position.PositionList p = getPositionsFromFeature(kaf);
                    if (p != null) {
                        return p;
                    }
                }
            } else if (feature instanceof KMLPlacemark) {
                KMLAbstractGeometry geometry = ((KMLPlacemark) feature).getGeometry();
                if (geometry instanceof KMLPolygon) {
                    return ((KMLPolygon) geometry).getOuterBoundary().getCoordinates();
                }
            }
            return null;
        }

        private Position.PositionList getPositionsFromKml(String kmlFile) throws Exception {
            KMLRoot kml = KMLRoot.createAndParse(kmlFile);
            return getPositionsFromFeature(kml.getFeature());
        }

        private class ViewControlPanel extends JPanel {

            private final WorldWindow wwd;
            private JSlider pitchSlider;
            private JSlider headingSlider;
            private JSlider rollSlider;
            private JSlider fovSlider;

            private boolean suspendEvents = false;

            public ViewControlPanel(WorldWindow wwd) {
                this.wwd = wwd;
                // Add view property listener
                this.wwd.getView().addPropertyChangeListener((PropertyChangeEvent propertyChangeEvent) -> {
                    update();
                });

                // Compose panel
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                insertBeforeCompass(getWwd(), new CrosshairLayer());

                // Pitch slider
                JPanel pitchPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                pitchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                pitchPanel.add(new JLabel("Pitch:"));
                pitchSlider = new JSlider(0, 180, 90);
                pitchSlider.addChangeListener((ChangeEvent changeEvent) -> {
                    updateView();
                });
                pitchPanel.add(pitchSlider);

                // Heading slider
                JPanel headingPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                headingPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                headingPanel.add(new JLabel("Heading:"));
                headingSlider = new JSlider(-180, 180, 0);
                headingSlider.addChangeListener((ChangeEvent changeEvent) -> {
                    updateView();
                });
                headingPanel.add(headingSlider);

                // Roll slider
                JPanel rollPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                rollPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                rollPanel.add(new JLabel("Roll:"));
                rollSlider = new JSlider(-180, 180, 0);
                rollSlider.addChangeListener((ChangeEvent changeEvent) -> {
                    updateView();
                });
                rollPanel.add(rollSlider);

                // Field of view slider
                JPanel fovPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                fovPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                fovPanel.add(new JLabel("Field of view:"));
                fovSlider = new JSlider(10, 120, 45);
                fovSlider.addChangeListener((ChangeEvent changeEvent) -> {
                    updateView();
                });
                fovPanel.add(fovSlider);

                // Assembly
                this.add(pitchPanel);
                this.add(headingPanel);
                this.add(rollPanel);
                this.add(fovPanel);

                JButton resetBut = new JButton("Reset");
                resetBut.addActionListener((ActionEvent e) -> {
//                    pitchSlider.setValue(90);
//                    rollSlider.setValue(0);
//                    headingSlider.setValue(0);
//                    fovSlider.setValue(45);
//                    KMLRoot debugModel = model;
//                    printIdTable(debugModel);
//                    KMLController debugController=kmlController;
//                    System.out.println(debugController);
//                    updateView();
                });
                this.add(resetBut);

                this.setBorder(
                        new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("View")));
                this.setToolTipText("View controls");
            }

            // Update view settings from control panel in a 'first person' perspective
            private void updateView() {
                if (!suspendEvents) {
//                    BasicFlyView view = (BasicFlyView) this.wwd.getView();
                    View view = this.wwd.getView();

                    // Stop iterators first
                    view.stopAnimations();

                    // Save current eye position
                    final Position pos = view.getEyePosition();

                    // Set view heading, pitch and fov
                    view.setHeading(Angle.fromDegrees(this.headingSlider.getValue()));
                    view.setPitch(Angle.fromDegrees(this.pitchSlider.getValue()));
                    view.setFieldOfView(Angle.fromDegrees(this.fovSlider.getValue()));
                    view.setRoll(Angle.fromDegrees(this.rollSlider.getValue()));

                    // Restore eye position
                    view.setEyePosition(pos);

                    // Redraw
                    this.wwd.redraw();
                }
            }

            // Update control panel from view
            public void update() {
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

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Object Animations", AppFrame.class);
    }
}
