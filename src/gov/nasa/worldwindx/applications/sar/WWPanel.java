/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.StatusBar;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * @author tag
 * @version $Id: WWPanel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWPanel extends JPanel
{
    protected static class FocusablePanel extends JPanel
    {
        private Component focusContext;

        public FocusablePanel(LayoutManager layoutManager, Component focusContext)
        {
            super(layoutManager);
            this.focusContext = focusContext;
        }

        protected void paintComponent(Graphics graphics)
        {
            super.paintComponent(graphics);

            if (this.focusContext.isFocusOwner())
            {
                Rectangle bounds = this.getBounds();
                BasicGraphicsUtils.drawDashedRect(graphics, 0, 0, bounds.width, bounds.height);
            }
        }
    }

    private FocusablePanel panel;
    private WorldWindowGLCanvas wwd;
    private StatusBar statusBar;

    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
    {
        @SuppressWarnings({"StringEquality"})
        public void propertyChange(PropertyChangeEvent propertyChangeEvent)
        {
            if (propertyChangeEvent.getPropertyName() == SARKey.ELEVATION_UNIT)
                updateElevationUnit(propertyChangeEvent.getNewValue());
            if (propertyChangeEvent.getPropertyName() == SARKey.ANGLE_FORMAT)
                updateAngleFormat(propertyChangeEvent.getNewValue());
        }
    };

    private final FocusListener focusListener = new FocusListener()
    {
        public void focusGained(FocusEvent focusEvent)
        {
            this.focusChanged(focusEvent);
        }

        public void focusLost(FocusEvent focusEvent)
        {
            this.focusChanged(focusEvent);
        }

        protected void focusChanged(FocusEvent focusEvent)
        {
            repaint();
        }
    };

    public WWPanel()
    {
        super(new BorderLayout(0, 0)); // hgap, vgap

        this.wwd = new WorldWindowGLCanvas();
        this.wwd.setPreferredSize(new Dimension(800, 800));

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        this.wwd.addPropertyChangeListener(this.propertyChangeListener);
        this.wwd.addFocusListener(this.focusListener);
        this.wwd.setFocusable(true);

        this.statusBar = new StatusBar();
        this.statusBar.setEventSource(wwd);

        this.panel = new FocusablePanel(new BorderLayout(0, 0), this.wwd); // hgap, vgap
        this.panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.panel.add(this.wwd, BorderLayout.CENTER);
        this.add(this.panel, BorderLayout.CENTER);
        this.add(this.statusBar, BorderLayout.PAGE_END);
    }

    public WorldWindowGLCanvas getWwd()
    {
        return wwd;
    }

    public StatusBar getStatusBar()
    {
        return statusBar;
    }

    private void updateElevationUnit(Object newValue)
    {
        for (Layer layer : this.wwd.getModel().getLayers())
        {
            if (layer instanceof ScalebarLayer)
            {
                if (SAR2.UNIT_IMPERIAL.equals(newValue))
                    ((ScalebarLayer) layer).setUnit(ScalebarLayer.UNIT_IMPERIAL);
                else // Default to metric units.
                    ((ScalebarLayer) layer).setUnit(ScalebarLayer.UNIT_METRIC);
            }
            else if (layer instanceof TerrainProfileLayer)
            {
                if (SAR2.UNIT_IMPERIAL.equals(newValue))
                    ((TerrainProfileLayer) layer).setUnit(TerrainProfileLayer.UNIT_IMPERIAL);
                else // Default to metric units.
                    ((TerrainProfileLayer) layer).setUnit(TerrainProfileLayer.UNIT_METRIC);
            }
        }

        if (SAR2.UNIT_IMPERIAL.equals(newValue))
            this.statusBar.setElevationUnit(StatusBar.UNIT_IMPERIAL);
        else // Default to metric units.
            this.statusBar.setElevationUnit(StatusBar.UNIT_METRIC);
    }

    private void updateAngleFormat(Object newValue)
    {
        this.statusBar.setAngleFormat((String)newValue);
    }
}
