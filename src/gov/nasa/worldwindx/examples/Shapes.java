/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.BasicDragger;

import com.jogamp.opengl.util.awt.TextRenderer;
import gov.nasa.worldwind.WorldWind;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * This example allows the user to create path and surface shapes on the globe and modify their parameters with a simple
 * user interface.
 *
 * @author tag
 * @version $Id: Shapes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Shapes {
    
    private static class Info {
        
        private final Object object;
        private final String name;
        
        public Info(String name, Object object) {
            this.object = object;
            this.name = name;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected static class AppFrame extends JFrame {
        
        private Dimension canvasSize = new Dimension(800, 600);
        private ApplicationTemplate.AppPanel wwjPanel;
        private RenderableLayer layer = new RenderableLayer();
        private TextRenderer textRenderer = new TextRenderer(java.awt.Font.decode("Arial-Plain-13"), true, false);
        
        public AppFrame() {
            // Create the WorldWindow.
            this.wwjPanel = new ApplicationTemplate.AppPanel(this.canvasSize, true);
            this.wwjPanel.setPreferredSize(canvasSize);
            
            ApplicationTemplate.insertBeforePlacenames(this.wwjPanel.getWwd(), layer);
            
            JPanel shapesPanel = makeShapeSelectionPanel();
            shapesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            JPanel attrsPanel = makeAttributesPanel();
            attrsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Put the pieces together.
            JPanel controlPanel = new JPanel(new BorderLayout());
            controlPanel.add(shapesPanel, BorderLayout.CENTER);
            JPanel p = new JPanel(new BorderLayout(6, 6));
            p.add(attrsPanel, BorderLayout.CENTER);
            controlPanel.add(p, BorderLayout.SOUTH);
            
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
            this.getContentPane().add(controlPanel, BorderLayout.WEST);
            this.pack();

            // Center the application on the screen.
            Dimension prefSize = this.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
            
            wwjPanel.getWwd().addRenderingListener((RenderingEvent event) -> {
                if (!event.getStage().equals(RenderingEvent.BEFORE_BUFFER_SWAP)) {
                    return;
                }
                if (currentShape instanceof Path) {
                    Path p1 = (Path) currentShape;
                    String length = Double.toString(p1.getLength());
                    textRenderer.beginRendering(wwjPanel.getWidth(), wwjPanel.getHeight());
                    textRenderer.draw(length, 100, 100);
                    textRenderer.endRendering();
                }
            });

            // Enable dragging and other selection responses
            this.setupSelection();
        }
        
        private Renderable currentShape;
        
        private String currentPathColor = "Yellow";
        private int currentPathOpacity = 10;
        private double currentPathWidth = 1;
        private String currentPathType = "Great Circle";
        private String currentPathStyle = "Solid";
        private boolean currentFollowTerrain = true;
        private float currentOffset = 0;
        private int currentTerrainConformance = 10;
        private int currentNumSubsegments = 10;
        
        private String currentBorderColor = "Yellow";
        private double currentBorderWidth = 1;
        private int currentBorderOpacity = 10;
        private String currentBorderStyle = "Solid";
        
        private String currentInteriorColor = "Yellow";
        private int currentInteriorOpacity = 10;
        private String currentInteriorStyle = "Solid";
        
        private ArrayList<JComponent> onTerrainOnlyItems = new ArrayList<>();
        private ArrayList<JComponent> offTerrainOnlyItems = new ArrayList<>();
        
        private void update() {
            onTerrainOnlyItems.forEach((c) -> {
                c.setEnabled(currentFollowTerrain);
            });
            
            offTerrainOnlyItems.forEach((c) -> {
                c.setEnabled(!currentFollowTerrain);
            });
            
            if (this.currentShape instanceof SurfaceShape) {
                SurfaceShape shape = (SurfaceShape) currentShape;
                ShapeAttributes attr = shape.getAttributes();
                
                if (attr == null) {
                    attr = new BasicShapeAttributes();
                }
                
                if (!currentBorderStyle.equals("None")) {
                    float alpha = currentBorderOpacity >= 10 ? 1f : currentBorderOpacity <= 0 ? 0f
                            : currentBorderOpacity / 10f;
                    Color color = null;
                    switch (currentBorderColor) {
                        case "Yellow":
                            color = new Color(1f, 1f, 0f);
                            break;
                        case "Red":
                            color = new Color(1f, 0f, 0f);
                            break;
                        case "Green":
                            color = new Color(0f, 1f, 0f);
                            break;
                        case "Blue":
                            color = new Color(0f, 0f, 1f);
                            break;
                        default:
                            break;
                    }
                    
                    attr.setDrawOutline(true);
                    attr.setOutlineMaterial(new Material(color));
                    attr.setOutlineOpacity(alpha);
                    attr.setOutlineWidth(currentBorderWidth);
                } else {
                    attr.setDrawOutline(false);
                }
                
                if (!currentInteriorStyle.equals("None")) {
                    float alpha = currentInteriorOpacity >= 10 ? 1f : currentInteriorOpacity <= 0 ? 0f
                            : currentInteriorOpacity / 10f;
                    Color color = null;
                    switch (currentInteriorColor) {
                        case "Yellow":
                            color = new Color(1f, 1f, 0f);
                            break;
                        case "Red":
                            color = new Color(1f, 0f, 0f);
                            break;
                        case "Green":
                            color = new Color(0f, 1f, 0f);
                            break;
                        case "Blue":
                            color = new Color(0f, 0f, 1f);
                            break;
                        default:
                            break;
                    }
                    
                    attr.setInteriorMaterial(new Material(color));
                    attr.setInteriorOpacity(alpha);
                    attr.setDrawInterior(true);
                } else {
                    attr.setDrawInterior(false);
                }
                
                shape.setAttributes(attr);
            } else {
                float alpha = currentPathOpacity >= 10 ? 1f : currentPathOpacity <= 0 ? 0f
                        : currentPathOpacity / 10f;
                Color color = null;
                switch (currentPathColor) {
                    case "Yellow":
                        color = new Color(1f, 1f, 0f, alpha);
                        break;
                    case "Red":
                        color = new Color(1f, 0f, 0f, alpha);
                        break;
                    case "Green":
                        color = new Color(0f, 1f, 0f, alpha);
                        break;
                    case "Blue":
                        color = new Color(0f, 0f, 1f, alpha);
                        break;
                    default:
                        break;
                }
                
                if (currentShape instanceof Path) {
                    Path path = (Path) currentShape;
                    path.setFollowTerrain(currentFollowTerrain);
                    if (path.isFollowTerrain()) {
                        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    } else {
                        path.setAltitudeMode(WorldWind.ABSOLUTE);
                    }
                    path.setTerrainConformance(currentTerrainConformance);
                    path.setNumSubsegments(currentNumSubsegments);
                    
                    if (currentPathType.equalsIgnoreCase("linear")) {
                        path.setPathType(AVKey.LINEAR);
                    } else if (currentPathType.equalsIgnoreCase("rhumb line")) {
                        path.setPathType(AVKey.RHUMB_LINE);
                    } else {
                        path.setPathType(AVKey.GREAT_CIRCLE);
                    }
                    
                    path.setOffset(currentOffset);
                    
                    BasicShapeAttributes attrs = new BasicShapeAttributes();
                    attrs.setOutlineMaterial(new Material(color));
                    attrs.setInteriorMaterial(new Material(color));
                    attrs.setOutlineWidth(currentPathWidth);
                    attrs.setOutlineOpacity(alpha);
                    if (currentPathStyle.equals("Dash")) {
                        attrs.setOutlineStippleFactor(5);
                        attrs.setOutlineStipplePattern((short) 0xAAAA);
                    } else {
                        attrs.setOutlineStippleFactor(0);
                    }
                    path.setAttributes(attrs);
                }
            }
            this.layer.removeAllRenderables();
            if (this.currentShape != null) {
                this.layer.addRenderable(this.currentShape);
            }
            this.wwjPanel.getWwd().redraw();
        }
        
        private Info[] buildSurfaceShapes() {
            LatLon position = new LatLon(Angle.fromDegrees(38), Angle.fromDegrees(-105));
            
            ArrayList<LatLon> surfaceLinePositions = new ArrayList<>();
            surfaceLinePositions.add(position);
            surfaceLinePositions.add(LatLon.fromDegrees(39, -104));
            surfaceLinePositions.add(LatLon.fromDegrees(39, -105));
            surfaceLinePositions.add(position);
            
            return new Info[]{
                new Info("Circle", new SurfaceCircle(position, 100e3)),
                new Info("Ellipse", new SurfaceEllipse(position, 100e3, 90e3, Angle.ZERO)),
                new Info("Square", new SurfaceSquare(position, 100e3)),
                new Info("Quad", new SurfaceQuad(position, 100e3, 60e3, Angle.ZERO)),
                new Info("Sector", new SurfaceSector(Sector.fromDegrees(38, 40, -105, -103))),
                new Info("Polygon", new SurfacePolygon(surfaceLinePositions)),};
        }
        
        private Info[] buildFreeShapes() {
            double elevation = 10e3;
            ArrayList<Position> positions = new ArrayList<>();
            positions.add(new Position(Angle.fromDegrees(37.8484), Angle.fromDegrees(-119.9754), elevation));
            positions.add(new Position(Angle.fromDegrees(39.3540), Angle.fromDegrees(-110.1526), elevation));
            positions.add(new Position(Angle.fromDegrees(38.3540), Angle.fromDegrees(-100.1526), elevation));
            
            ArrayList<Position> positions2 = new ArrayList<>();
            positions2.add(new Position(Angle.fromDegrees(0), Angle.fromDegrees(-150), elevation));
            positions2.add(new Position(Angle.fromDegrees(25), Angle.fromDegrees(-75), elevation));
            positions2.add(new Position(Angle.fromDegrees(50), Angle.fromDegrees(0), elevation));
            
            ArrayList<Position> positions3 = new ArrayList<>();
            for (double lat = 42, lon = -100; lat <= 45; lat += .1, lon += .1) {
                positions3.add(new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), elevation));
            }
            
            ArrayList<Position> positions4 = new ArrayList<>();
            positions4.add(new Position(Angle.fromDegrees(90), Angle.fromDegrees(-110), elevation));
            positions4.add(new Position(Angle.fromDegrees(-90), Angle.fromDegrees(-110), elevation));
            
            ArrayList<Position> positions5 = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                positions5.add(Position.fromDegrees(38.0 + i * 0.0001, 30.0 + i * 0.0001, 1000.0 + i * 5.0));
            }
            
            Info[] infos = new Info[]{
                new Info("Short Path", new Path(positions)),
                new Info("Long Path", new Path(positions2)),
                new Info("Incremental Path", new Path(positions3)),
                new Info("Vertical Path", new Path(positions4)),
                new Info("Small-segment Path", new Path(positions5)),
                new Info("Quad", new Quadrilateral(Sector.fromDegrees(38, 40, -104, -105), elevation)),
                new Info("None", null)
            };
            
            return infos;
        }
        
        private JPanel makeShapeSelectionPanel() {
            final Info[] surfaceShapeInfos = this.buildSurfaceShapes();
            GridLayout layout = new GridLayout(surfaceShapeInfos.length, 1);
            JPanel ssPanel = new JPanel(layout);
            ButtonGroup group = new ButtonGroup();
            for (final Info info : surfaceShapeInfos) {
                JRadioButton b = new JRadioButton(info.name);
                b.addActionListener((ActionEvent actionEvent) -> {
                    currentShape = (Renderable) info.object;
                    update();
                });
                group.add(b);
                ssPanel.add(b);
                if (info.name.equalsIgnoreCase("none")) {
                    b.setSelected(true);
                }
            }
            ssPanel.setBorder(this.createTitleBorder("Surface Shapes"));
            
            final Info[] freeShapeInfos = this.buildFreeShapes();
            layout = new GridLayout(freeShapeInfos.length, 1);
            JPanel fsPanel = new JPanel(layout);
            for (final Info info : freeShapeInfos) {
                JRadioButton b = new JRadioButton(info.name);
                b.addActionListener((ActionEvent actionEvent) -> {
                    currentShape = (Renderable) info.object;
                    update();
                });
                group.add(b);
                fsPanel.add(b);
                if (info.name.equalsIgnoreCase("none")) {
                    b.setSelected(true);
                }
            }
            fsPanel.setBorder(this.createTitleBorder("Path Shapes"));
            
            JPanel shapesPanel = new JPanel(new GridLayout(1, 2, 8, 1));
            shapesPanel.add(fsPanel);
            shapesPanel.add(ssPanel);
            
            return shapesPanel;
        }
        
        private Border createTitleBorder(String title) {
            TitledBorder b = BorderFactory.createTitledBorder(title);
            return new CompoundBorder(b, BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }
        
        private JPanel makeAttributesPanel() {
            JPanel panel = new JPanel(new GridLayout(1, 2, 8, 8));
            panel.add(this.makePathAttributesPanel());
            panel.add(this.makeInteriorAttributesPanel());
            
            return panel;
        }
        
        private JPanel makePathAttributesPanel() {
            JPanel outerPanel = new JPanel(new BorderLayout(6, 6));
            outerPanel.setBorder(this.createTitleBorder("Path Attributes"));
            
            GridLayout nameLayout = new GridLayout(0, 1, 6, 6);
            JPanel namePanel = new JPanel(nameLayout);
            
            GridLayout valueLayout = new GridLayout(0, 1, 6, 6);
            JPanel valuePanel = new JPanel(valueLayout);
            
            namePanel.add(new JLabel("Follow Terrain"));
            JCheckBox ckb = new JCheckBox();
            ckb.setSelected(currentFollowTerrain);
            ckb.addActionListener((ActionEvent actionEvent) -> {
                currentFollowTerrain = ((JCheckBox) actionEvent.getSource()).isSelected();
                update();
            });
            valuePanel.add(ckb);
            
            JLabel label;
            
            namePanel.add(label = new JLabel("Conformance"));
            int[] values = new int[]{1, 2, 4, 8, 10, 15, 20, 30, 40, 50};
            String[] strings = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strings[i] = Integer.toString(values[i]) + " pixels";
            }
            JSpinner sp = new JSpinner(
                    new SpinnerListModel(strings));
            onTerrainOnlyItems.add(label);
            onTerrainOnlyItems.add(sp);
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                String v = (String) ((JSpinner) changeEvent.getSource()).getValue();
                currentTerrainConformance = Integer.parseInt(v.substring(0, v.indexOf(" ")));
                update();
            });
            sp.setValue(Integer.toString(currentTerrainConformance) + " pixels");
            valuePanel.add(sp);
            
            namePanel.add(label = new JLabel("Subsegments"));
            sp = new JSpinner(new SpinnerListModel(new String[]{"1", "2", "5", "10", "20", "40", "50"}));
            offTerrainOnlyItems.add(label);
            offTerrainOnlyItems.add(sp);
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                String v = (String) ((JSpinner) changeEvent.getSource()).getValue();
                currentNumSubsegments = Integer.parseInt(v);
                update();
            });
            sp.setValue(Integer.toString(currentNumSubsegments));
            valuePanel.add(sp);
            
            namePanel.add(new JLabel("Type"));
            final JComboBox cb = new JComboBox(new String[]{"Great Circle", "Linear", "Rhumb Line"});
            cb.addActionListener((ActionEvent actionEvent) -> {
                currentPathType = (String) cb.getSelectedItem();
                update();
            });
            cb.setSelectedItem("Great Circle");
            valuePanel.add(cb);
            
            namePanel.add(new JLabel("Style"));
            final JComboBox cb1 = new JComboBox(new String[]{"None", "Solid", "Dash"});
            cb1.addActionListener((ActionEvent actionEvent) -> {
                currentPathStyle = (String) cb1.getSelectedItem();
                update();
            });
            cb1.setSelectedItem("Solid");
            valuePanel.add(cb1);
            
            namePanel.add(new JLabel("Width"));
            sp = new JSpinner(new SpinnerNumberModel(this.currentPathWidth, 1d, 10d, 1d));
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                currentPathWidth = (Double) ((JSpinner) changeEvent.getSource()).getValue();
                update();
            });
            sp.setValue(currentPathWidth);
            valuePanel.add(sp);
            
            namePanel.add(new JLabel("Color"));
            JComboBox cb2 = new JComboBox(new String[]{"Red", "Green", "Blue", "Yellow"});
            cb2.setSelectedItem(currentPathColor);
            cb2.addActionListener((ActionEvent actionEvent) -> {
                currentPathColor = (String) ((JComboBox) actionEvent.getSource()).getSelectedItem();
                update();
            });
            valuePanel.add(cb2);
            
            namePanel.add(new JLabel("Opacity"));
            sp = new JSpinner(new SpinnerNumberModel(this.currentPathOpacity, 0, 10, 1));
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                currentPathOpacity = (Integer) ((JSpinner) changeEvent.getSource()).getValue();
                update();
            });
            valuePanel.add(sp);
            
            namePanel.add(new JLabel("Offset"));
            sp = new JSpinner(
                    new SpinnerListModel(new String[]{"0", "10", "100", "1000", "10000", "100000", "1000000"}));
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                currentOffset = Float.parseFloat((String) ((JSpinner) changeEvent.getSource()).getValue());
                update();
            });
            sp.setValue("0");
            valuePanel.add(sp);
            
            outerPanel.add(namePanel, BorderLayout.WEST);
            outerPanel.add(valuePanel, BorderLayout.CENTER);
            
            return outerPanel;
        }
        
        private JPanel makeInteriorAttributesPanel() {
            JPanel outerPanel = new JPanel(new BorderLayout(6, 6));
            outerPanel.setBorder(this.createTitleBorder("Surface Attributes"));
            
            GridLayout nameLayout = new GridLayout(0, 1, 6, 6);
            JPanel namePanel = new JPanel(nameLayout);
            
            GridLayout valueLayout = new GridLayout(0, 1, 6, 6);
            JPanel valuePanel = new JPanel(valueLayout);
            
            namePanel.add(new JLabel("Style"));
            final JComboBox cb1 = new JComboBox(new String[]{"None", "Solid"});
            cb1.addActionListener((ActionEvent actionEvent) -> {
                currentInteriorStyle = (String) cb1.getSelectedItem();
                update();
            });
            cb1.setSelectedItem("Solid");
            valuePanel.add(cb1);
            
            namePanel.add(new JLabel("Opacity"));
            JSpinner sp = new JSpinner(new SpinnerNumberModel(this.currentBorderOpacity, 0, 10, 1));
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                currentInteriorOpacity = (Integer) ((JSpinner) changeEvent.getSource()).getValue();
                update();
            });
            valuePanel.add(sp);
            
            namePanel.add(new JLabel("Color"));
            final JComboBox cb2 = new JComboBox(new String[]{"Red", "Green", "Blue", "Yellow"});
            cb2.setSelectedItem(currentInteriorColor);
            cb2.addActionListener((ActionEvent actionEvent) -> {
                currentInteriorColor = (String) ((JComboBox) actionEvent.getSource()).getSelectedItem();
                update();
            });
            valuePanel.add(cb2);
            
            namePanel.add(new JLabel("Border"));
            final JComboBox cb5 = new JComboBox(new String[]{"None", "Solid"});
            cb5.addActionListener((ActionEvent actionEvent) -> {
                currentBorderStyle = (String) cb5.getSelectedItem();
                update();
            });
            cb5.setSelectedItem("Solid");
            valuePanel.add(cb5);
            
            namePanel.add(new JLabel("Border Width"));
            sp = new JSpinner(new SpinnerNumberModel(this.currentBorderWidth, 1d, 10d, 1d));
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                currentBorderWidth = (Double) ((JSpinner) changeEvent.getSource()).getValue();
                update();
            });
            sp.setValue(currentBorderWidth);
            valuePanel.add(sp);
            
            namePanel.add(new JLabel("Border Color"));
            JComboBox cb4 = new JComboBox(new String[]{"Red", "Green", "Blue", "Yellow"});
            cb4.setSelectedItem(currentBorderColor);
            cb4.addActionListener((ActionEvent actionEvent) -> {
                currentBorderColor = (String) ((JComboBox) actionEvent.getSource()).getSelectedItem();
                update();
            });
            valuePanel.add(cb4);
            
            namePanel.add(new JLabel("Border Opacity"));
            sp = new JSpinner(new SpinnerNumberModel(this.currentBorderOpacity, 0, 10, 1));
            sp.addChangeListener((ChangeEvent changeEvent) -> {
                currentBorderOpacity = (Integer) ((JSpinner) changeEvent.getSource()).getValue();
                update();
            });
            valuePanel.add(sp);
            
            outerPanel.add(namePanel, BorderLayout.WEST);
            outerPanel.add(valuePanel, BorderLayout.CENTER);
            
            return outerPanel;
        }
        
        private void setupSelection() {
            this.wwjPanel.getWwd().addSelectListener(new SelectListener() {
                private WWIcon lastToolTipIcon = null;
                private final BasicDragger dragger = new BasicDragger(AppFrame.this.wwjPanel.getWwd());
                
                @Override
                public void selected(SelectEvent event) {
                    // Have hover selections show a picked icon's tool tip.
                    if (event.getEventAction().equals(SelectEvent.HOVER)) {
                        // If a tool tip is already showing, undisplay it.
                        if (lastToolTipIcon != null) {
                            lastToolTipIcon.setShowToolTip(false);
                            this.lastToolTipIcon = null;
                            AppFrame.this.wwjPanel.getWwd().redraw();
                        }

                        // If there's a selection, we're not dragging, and the selection is an icon, show tool tip.
                        if (event.hasObjects() && !this.dragger.isDragging()) {
                            if (event.getTopObject() instanceof WWIcon) {
                                this.lastToolTipIcon = (WWIcon) event.getTopObject();
                                lastToolTipIcon.setShowToolTip(true);
                                AppFrame.this.wwjPanel.getWwd().redraw();
                            }
                        }
                    } // Have rollover events highlight the rolled-over object.
                    else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging()) {
//                        AppFrame.this.highlight(event.getTopObject());
                    } // Have drag events drag the selected object.
                    else if (event.getEventAction().equals(SelectEvent.DRAG_END)
                            || event.getEventAction().equals(SelectEvent.DRAG)) {
                        // Delegate dragging computations to a dragger.
                        this.dragger.selected(event);

                        // We missed any roll-over events while dragging, so highlight any under the cursor now,
                        // or de-highlight the dragged shape if it's no longer under the cursor.
                        if (event.getEventAction().equals(SelectEvent.DRAG_END)) {
                            PickedObjectList pol = wwjPanel.getWwd().getObjectsAtCurrentPosition();
                            if (pol != null) {
//                                AppFrame.this.highlight(pol.getTopObject());
                                AppFrame.this.wwjPanel.getWwd().redraw();
                            }
                        }
                    }
                }
            });
        }
    }
    
    private static final String APP_NAME = "WorldWind Shapes";
    
    static {
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }
    
    public static void main(String[] args) {
        try {
            AppFrame frame = new AppFrame();
            frame.setTitle(APP_NAME);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
