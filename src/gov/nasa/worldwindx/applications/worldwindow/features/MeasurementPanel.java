/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.*;
import gov.nasa.worldwindx.applications.worldwindow.util.measuretool.WWOMeasureTool;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * @author tag
 * @version $Id: MeasurementPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class MeasurementPanel extends AbstractFeaturePanel
{
    private static final String NAME = "Measurement";

    private WWOMeasureTool measureTool;
    private RenderableLayer shapeLayer;
    private RenderableLayer controlPointsLayer;
    private PropertyChangeListener toolListener;

    private JButton newButton;
    private JButton pauseButton;
    private JButton endButton;
    private JComboBox pathTypeCombo;
    private JComboBox shapeCombo;

    private Color lineColor = Color.WHITE;
    private Color fillColor = Color.WHITE;
    private String pathType = AVKey.GREAT_CIRCLE;

    public MeasurementPanel(Registry registry)
    {
        super(NAME + " Panel", Constants.FEATURE_MEASUREMENT_PANEL, new ShadedPanel(new BorderLayout()), registry);
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        JPanel np = new JPanel();
        np.setOpaque(false);
        createComponents(np);

        JPanel np2 = new JPanel(new BorderLayout());
        np2.setOpaque(false);
        np2.setBorder(new EmptyBorder(10, 10, 10, 10));
        np2.add(np, BorderLayout.CENTER);

        this.getJPanel().setOpaque(false);
        this.getJPanel().add(np2, BorderLayout.CENTER);
        this.getJPanel().setToolTipText("Measure distance or area");
        this.getJPanel().putClientProperty(Constants.FEATURE_OWNER_PROPERTY, this);
        this.getJPanel().add(new PanelTitle(NAME, SwingConstants.CENTER), BorderLayout.NORTH);

        this.makeToolListener();
    }

    public void setLayers(RenderableLayer shapeLayer, RenderableLayer controlPointsLayer)
    {
        this.shapeLayer = shapeLayer;
        this.controlPointsLayer = controlPointsLayer;
        this.shapeCombo.setSelectedItem(LINE);
    }

    public RenderableLayer getShapeLayer()
    {
        return this.shapeLayer;
    }

    public Renderable getShape()
    {
        return this.measureTool != null ? this.measureTool.getShape() : null;
    }

    public JComponent[] getDialogControls()
    {
        return null;
    }

    public WWOMeasureTool getMeasureTool()
    {
        return this.measureTool;
    }

    public void clearPanel()
    {
        this.disposeCurrentMeasureTool();
        this.shapeCombo.setSelectedItem(LINE);
    }

    public void deletePanel()
    {
        this.disposeCurrentMeasureTool();
    }

    public void setLineColor(Color color)
    {
        if (color == null || this.measureTool == null || this.measureTool.getShape() == null)
            return;

        this.lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (0.3 * color.getAlpha()));
        this.setShapeColor(this.measureTool.getShape(), this.lineColor, this.fillColor);
    }

    public void setFillColor(Color color)
    {
        if (color == null || this.measureTool == null || this.measureTool.getShape() == null)
            return;

        this.fillColor = color;
        this.setShapeColor(this.measureTool.getShape(), this.lineColor, this.fillColor);
    }

    public void setShapeColor(Renderable shape, Color lineColor, Color fillColor)
    {
        if (shape instanceof Polyline)
            ((Polyline) shape).setColor(lineColor);

        if (shape instanceof SurfaceShape)
        {
            SurfaceShape sShape = (SurfaceShape) shape;
            ShapeAttributes attrs = sShape.getAttributes();
            if (attrs == null)
                attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(new Material(fillColor));
            attrs.setInteriorOpacity(0.3);
            attrs.setDrawOutline(false);
            sShape.setAttributes(attrs);
        }
    }

    protected void setPathType(String pathType)
    {
        if (pathType == null || this.measureTool == null || this.measureTool.getShape() == null)
            return;

        this.pathType = pathType;

        if (this.measureTool.getShape() instanceof Polyline)
            ((Polyline) this.measureTool.getShape()).setPathType(pathType);
        else if (this.measureTool.getShape() instanceof SurfaceShape)
            ((SurfaceShape) this.measureTool.getShape()).setPathType(pathType);
    }

    protected void installNewMeasureTool(String shapeType)
    {
        Renderable shape = this.makeMeasureShape(shapeType);
        this.measureTool = new WWOMeasureTool(this.controller.getWWd(), shape,
            shapeType.equals(PATH) || shapeType.equals(FREEHAND) ? AVKey.SHAPE_PATH : null, this.controlPointsLayer);
        if (shapeType.equals(FREEHAND))
            this.measureTool.setFreeHand(true);

        this.measureTool.setUnitsFormat(this.controller.getUnits());
        this.setShapeColor(this.measureTool.getShape(), this.lineColor, this.fillColor);
        this.updatePanelValues();
        this.newButton.setEnabled(true);
        this.pauseButton.setEnabled(false);
        this.endButton.setEnabled(false);
        this.measureTool.addPropertyChangeListener(this.toolListener);
    }

    protected void disposeCurrentMeasureTool()
    {
        if (this.measureTool == null)
            return;

        this.shapeLayer.removeRenderable(this.measureTool.getShape());
        if (this.measureTool.getShape() instanceof Disposable)
            ((Disposable) this.measureTool.getShape()).dispose();

        this.measureTool.removePropertyChangeListener(this.toolListener);
        this.measureTool.dispose();
        this.measureTool = null;
    }

    private Renderable makeMeasureShape(String shapeType)
    {
        if (shapeType.equals(LINE) || shapeType.equals(PATH) || shapeType.equals(FREEHAND))
        {
            Polyline line = new Polyline();
            line.setFollowTerrain(true);
            line.setLineWidth(shapeType.equals(FREEHAND) ? 2 : 4);
            return line;
        }
        if (shapeType.equals(CIRCLE))
            return new SurfaceCircle();
        if (shapeType.equals(ELLIPSE))
            return new SurfaceEllipse();
        if (shapeType.equals(SQUARE))
            return new SurfaceSquare();
        if (shapeType.equals(RECTANGLE))
            return new SurfaceQuad();
        if (shapeType.equals(POLYGON))
            return new SurfacePolygon();

        return null;
    }

    private boolean layerContains(RenderableLayer layer, Renderable renderable)
    {
        for (Renderable r : layer.getRenderables())
        {
            if (r == renderable)
                return true;
        }

        return false;
    }

    protected void makeToolListener()
    {
        this.toolListener = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                // The tool was armed / disarmed
                if (event.getPropertyName().equals(WWOMeasureTool.EVENT_ARMED))
                {
                    if (getMeasureTool().isArmed())
                    {
                        newButton.setEnabled(false);
                        pauseButton.setText("Pause");
                        pauseButton.setEnabled(true);
                        endButton.setEnabled(true);
                        controller.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        ((Component) controller.getWWd()).requestFocus();
                    }
                    else
                    {
                        newButton.setEnabled(true);
                        pauseButton.setText("Pause");
                        pauseButton.setEnabled(false);
                        endButton.setEnabled(false);
                        controller.setCursor(Cursor.getDefaultCursor());
                    }
                }
                else if (event.getPropertyName().equals(WWOMeasureTool.EVENT_POSITION_ADD))
                {
                    if (getMeasureTool().getShape() != null
                        && !layerContains(getShapeLayer(), getMeasureTool().getShape()))
                    {
                        getShapeLayer().addRenderable(getMeasureTool().getShape());
                    }
                } // TODO: remove shape when it becomes undefined? such as no points in line
            }
        };
    }

    protected void updatePanelValues()
    {
        if (this.pathType.equals(AVKey.LINEAR))
            this.pathTypeCombo.setSelectedIndex(0);
        else if (this.pathType.equals(AVKey.RHUMB_LINE))
            this.pathTypeCombo.setSelectedIndex(1);
        else if (this.pathType.equals(AVKey.GREAT_CIRCLE))
            this.pathTypeCombo.setSelectedIndex(2);
    }

    private static final String LINE = "Line";
    private static final String PATH = "Path";
    private static final String POLYGON = "Polygon";
    private static final String CIRCLE = "Circle";
    private static final String ELLIPSE = "Ellipse";
    private static final String SQUARE = "Square";
    private static final String RECTANGLE = "Rectangule";
    private static final String FREEHAND = "Freehand";
    private static final String GREAT_CIRCLE = "Great Circle";
    private static final String RHUMB = "Rhumb";
    private static final String LINEAR = "Linear";

    private void createComponents(JPanel panel)
    {
        // Shape combo
        JPanel shapePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        shapePanel.setOpaque(false);
        shapePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        shapePanel.add(new JLabel("Measurement Type:"));
        this.shapeCombo = new JComboBox(
            new String[] {LINE, PATH, POLYGON, CIRCLE, ELLIPSE, SQUARE, RECTANGLE, FREEHAND});
        this.shapeCombo.setToolTipText("Choose a measurement type");
        this.shapeCombo.setOpaque(false);
        this.shapeCombo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
                disposeCurrentMeasureTool();
                installNewMeasureTool(item);
            }
        });
        shapePanel.add(this.shapeCombo);

        // Path type combo
        JPanel pathTypePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        pathTypePanel.setOpaque(false);
        pathTypePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        pathTypePanel.add(new JLabel("Route type:"));
        this.pathTypeCombo = new JComboBox(new String[] {LINEAR, RHUMB, GREAT_CIRCLE});
        this.pathTypeCombo.setToolTipText("Choose a route type");
        this.pathTypeCombo.setOpaque(false);
        this.pathTypeCombo.setSelectedIndex(2);
        this.pathTypeCombo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                String item = (String) ((JComboBox) event.getSource()).getSelectedItem();
                if (item.equals(LINEAR))
                    setPathType(AVKey.LINEAR);
                else if (item.equals(RHUMB))
                    setPathType(AVKey.RHUMB_LINE);
                else if (item.equals(GREAT_CIRCLE))
                    setPathType(AVKey.GREAT_CIRCLE);

                controller.redraw();
            }
        });
        pathTypePanel.add(this.pathTypeCombo);

        // Action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        buttonPanel.setOpaque(false);

        this.newButton = new JButton("Start");
        this.newButton.setToolTipText("Start creating a measurement");
        this.newButton.setOpaque(false);
        this.newButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                getMeasureTool().clear();
                if (shapeLayer != null && measureTool.getShape() != null)
                    shapeLayer.removeRenderable(measureTool.getShape());
                getMeasureTool().setArmed(true);
            }
        });
        buttonPanel.add(this.newButton);
        this.newButton.setEnabled(true);

        this.pauseButton = new JButton("Pause");
        this.pauseButton.setToolTipText("Pause temporarily during measurement creation");
        this.pauseButton.setOpaque(false);
        this.pauseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                getMeasureTool().setArmed(!getMeasureTool().isArmed());
                pauseButton.setText(!getMeasureTool().isArmed() ? "Resume" : "Pause");
                pauseButton.setEnabled(true);
                ((Component) controller.getWWd()).setCursor(!getMeasureTool().isArmed() ? Cursor.getDefaultCursor()
                    : Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
        });
        buttonPanel.add(this.pauseButton);
        this.pauseButton.setEnabled(false);

        this.endButton = new JButton("Finish");
        this.endButton.setToolTipText("Press when measurement shape is complete");
        this.endButton.setOpaque(false);
        this.endButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                getMeasureTool().setArmed(false);
            }
        });
        buttonPanel.add(endButton);
        this.endButton.setEnabled(false);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(shapePanel);
        panel.add(pathTypePanel);
        panel.add(buttonPanel);
    }
}
