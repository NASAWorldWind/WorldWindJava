/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeature;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: WWPanelImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWPanelImpl extends AbstractFeature implements WWPanel
{
    private JPanel panel;
    private WorldWindowGLCanvas wwd;

    public WWPanelImpl(Registry registry)
    {
        super("WorldWind Panel", Constants.WW_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
        this.wwd = new WorldWindowGLCanvas();
        this.wwd.addRenderingExceptionListener(new RenderingExceptionListener()
        {
            public void exceptionThrown(Throwable t)
            {
                if (t instanceof WWAbsentRequirementException)
                {
                    String msg = "This computer is not capable of running ";
                    msg += Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME);
                    msg += ".";
                    Util.getLogger().severe(msg);
                    System.exit(-1);
                }
            }
        });

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        // Disable screen credits.
        this.wwd.getSceneController().getScreenCreditController().setEnabled(false);

        this.wwd.setPreferredSize(new Dimension(1024, 768));
        this.panel.add(this.wwd, BorderLayout.CENTER);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);
    }

    public WorldWindow getWWd()
    {
        return wwd;
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }

    public Dimension getSize()
    {
        return this.panel.getSize();
    }

    public void addLayer(Layer layer)
    {
        if (layer != null)
            this.wwd.getModel().getLayers().add(layer);
    }

    public void removeLayer(Layer layer)
    {
        this.wwd.getModel().getLayers().remove(layer);
    }

    public void insertBeforeNamedLayer(Layer layer, String targetLayerName)
    {
        if (layer == null)
            return;

        if (targetLayerName == null)
        {
            this.wwd.getModel().getLayers().add(layer);
            return;
        }

        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetLayerName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    public void insertAfterNamedLayer(Layer layer, String targetLayerName)
    {
        if (layer == null)
            return;

        if (targetLayerName == null)
        {
            this.wwd.getModel().getLayers().add(layer);
            return;
        }

        // Insert the layer into the layer list just after the target layer.
        int targetPosition = 0;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetLayerName) != -1)
                targetPosition = layers.indexOf(l);
        }
        layers.add(targetPosition + 1, layer);
    }
}
