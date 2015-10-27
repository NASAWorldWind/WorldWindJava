/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.combine.*;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

/**
 * @author dcollins
 * @version $Id: ShapeClippingPanel.java 2410 2014-10-29 23:48:07Z dcollins $
 */
public class ShapeClippingPanel extends JPanel implements ActionListener
{
    public static enum ClipMode
    {
        LAND,
        WATER
    }

    protected WorldWindow wwd;
    protected ClipMode clipMode = ClipMode.LAND;
    protected double resolution = 5000; // 5 km
    protected Combinable clipShape;
    protected Combinable landShape;

    public ShapeClippingPanel(WorldWindow wwd)
    {
        this.wwd = wwd;
        this.makePanel();
    }

    public ClipMode getClipMode()
    {
        return this.clipMode;
    }

    protected void setClipMode(ClipMode clipMode)
    {
        this.clipMode = clipMode;
    }

    public double getResolution()
    {
        return this.resolution;
    }

    protected void setResolution(double resolution)
    {
        this.resolution = resolution;
    }

    public Combinable getClipShape()
    {
        return this.clipShape;
    }

    public void setClipShape(Combinable clipShape)
    {
        this.clipShape = clipShape;
    }

    public Combinable getLandShape()
    {
        return this.landShape;
    }

    public void setLandShape(Combinable landShape)
    {
        this.landShape = landShape;
    }

    protected void makePanel()
    {
        this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Clipping")));
        this.setLayout(new BorderLayout());

        Box vbox = Box.createVerticalBox();
        vbox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(vbox);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        vbox.add(buttonPanel);

        ButtonGroup radioButtonGroup = new ButtonGroup();
        JPanel radioButtonPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        buttonPanel.add(radioButtonPanel);

        JRadioButton landButton = new JRadioButton("Land");
        landButton.setSelected(this.getClipMode().equals(ClipMode.LAND));
        landButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setClipMode(ClipMode.LAND);
            }
        });
        radioButtonGroup.add(landButton);
        radioButtonPanel.add(landButton);

        JRadioButton waterButton = new JRadioButton("Water");
        waterButton.setSelected(this.getClipMode().equals(ClipMode.WATER));
        waterButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setClipMode(ClipMode.WATER);
            }
        });
        radioButtonGroup.add(waterButton);
        radioButtonPanel.add(waterButton);

        JButton button = new JButton("Clip");
        button.addActionListener(this);
        buttonPanel.add(button);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(0, new JLabel("1 km"));
        labelTable.put(5000, new JLabel("5 km"));
        labelTable.put(10000, new JLabel("10 km"));

        JSlider resolutionSlider = new JSlider(0, 10000, (int) Math.round(this.resolution));
        resolutionSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        resolutionSlider.setMajorTickSpacing(1000);
        resolutionSlider.setPaintTicks(true);
        resolutionSlider.setPaintLabels(true);
        resolutionSlider.setLabelTable(labelTable);
        resolutionSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                setResolution(((JSlider) e.getSource()).getValue());
            }
        });
        vbox.add(Box.createVerticalStrut(5));
        vbox.add(resolutionSlider);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        ContourList contours = this.clipShape();

        if (contours.getContourCount() > 0)
        {
            this.displayClippedShape(contours);
        }
    }

    protected ContourList clipShape()
    {
        Globe globe = this.wwd.getModel().getGlobe();
        double resolutionMeters = WWMath.clamp(this.getResolution(), 1000, Double.MAX_VALUE); // no less than 1km
        double resolutionRadians = resolutionMeters / globe.getRadius();
        ShapeCombiner combiner = new ShapeCombiner(globe, resolutionRadians);

        if (this.getClipMode().equals(ClipMode.LAND))
        {
            return combiner.intersection(this.getClipShape(), this.getLandShape()); // intersect with land
        }
        else if (this.getClipMode().equals(ClipMode.WATER))
        {
            return combiner.difference(this.getClipShape(), this.getLandShape()); // subtract land from shape
        }
        else
        {
            return new ContourList(); // empty contour list
        }
    }

    protected void displayClippedShape(ContourList contours)
    {
        Color color = this.getClipMode().equals(ClipMode.LAND) ? new Color(79, 213, 33) : new Color(7, 152, 249);
        Color outlineColor = WWUtil.makeColorBrighter(color);

        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(new Material(color));
        attrs.setInteriorOpacity(0.5);
        attrs.setOutlineMaterial(new Material(outlineColor));
        attrs.setOutlineWidth(2);

        SurfaceMultiPolygon shape = new SurfaceMultiPolygon(attrs, contours);
        shape.setPathType(AVKey.LINEAR);

        RenderableLayer layer = new RenderableLayer();
        layer.setName(this.getClipMode().equals(ClipMode.LAND) ? "Clipped Shape (Land)" : "Clipped Shape (Water)");
        layer.setPickEnabled(false);
        layer.addRenderable(shape);

        this.wwd.getModel().getLayers().add(layer);
        this.wwd.redraw();
    }
}
