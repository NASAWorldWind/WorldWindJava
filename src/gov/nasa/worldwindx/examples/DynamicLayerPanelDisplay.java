/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Shows how to use the scene controller's frame time stamp to identify whether tiled image layers have rendered during
 * the most recent frame, and convey this information to the user.
 *
 * @author tag
 * @version $Id: DynamicLayerPanelDisplay.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class DynamicLayerPanelDisplay
{
    // Derive a layer panel from the default one. This derived version keeps an association between a layer and the
    // layer's check box in the layer panel. It uses this info to update the check boxes label font to indicate
    // whether the corresponding layer has been rendered.

    protected static class DynamicLayerPanel extends LayerPanel
    {
        protected java.util.Map<Layer, JCheckBox> checkBoxes;

        public DynamicLayerPanel(WorldWindow wwd)
        {
            super(wwd);
        }

        protected void fill(WorldWindow wwd)
        {
            if (this.checkBoxes == null)
                this.checkBoxes = new HashMap<Layer, JCheckBox>();

            this.checkBoxes.clear();

            for (Layer layer : wwd.getModel().getLayers())
            {
                if (!(layer instanceof TiledImageLayer))
                    continue;

//                LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
//                JCheckBox jcb = new JCheckBox(action);
//                jcb.setSelected(action.selected);
//                this.layersPanel.add(jcb);
//                this.checkBoxes.put(layer, jcb);
            }

            this.updateLayerActivity(wwd);
        }

        /**
         * Loops through this layer panel's layer/checkbox map and updates the checkbox font to indicate whether the
         * corresponding layer was just rendered. This method is called by a rendering listener -- see below.
         *
         * @param wwd the world window.
         */
        protected void updateLayerActivity(WorldWindow wwd)
        {
            for (Map.Entry<Layer, JCheckBox> entry : this.checkBoxes.entrySet())
            {
                // The frame timestamp from the layer indicates the last frame in which it rendered something. If that
                // timestamp matches the current timestamp of the scene controller, then the layer rendered something
                // during the most recent frame. Note that this frame timestamp protocol is only in place by default
                // for TiledImageLayer and its subclasses. Applications could, however, implement it for the layers
                // they design.

                Long layerTimeStamp = (Long) entry.getKey().getValue(AVKey.FRAME_TIMESTAMP);
                Long frameTimeStamp = (Long) wwd.getSceneController().getValue(AVKey.FRAME_TIMESTAMP);

                if (layerTimeStamp != null && frameTimeStamp != null
                    && layerTimeStamp.longValue() == frameTimeStamp.longValue())
                {
                    // Set the font to bold if the layer was just rendered.
                    entry.getValue().setFont(entry.getValue().getFont().deriveFont(Font.BOLD));
                }
                else
                {
                    // Set the font to plain if the layer was not just rendered.
                    entry.getValue().setFont(entry.getValue().getFont().deriveFont(Font.PLAIN));
                }
            }
        }
    }

    // The rest of this class just instantiates the World Window and installs the layer panel. It's a stripped down
    // version of the code in ApplicationTemplate. The only part that is special to this example is the instantiation
    // of the DynamicLayerPanel and the registration of a rendering listener to update the layer panel each frame.

    public static class AppPanel extends JPanel
    {
        protected WorldWindow wwd;
        protected StatusBar statusBar;

        public AppPanel(Dimension canvasSize)
        {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            ((Component) this.wwd).setPreferredSize(canvasSize);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            this.add((Component) this.wwd, BorderLayout.CENTER);

            this.statusBar = new StatusBar();
            this.add(statusBar, BorderLayout.PAGE_END);
            this.statusBar.setEventSource(wwd);
        }
    }

    protected static class AppFrame extends JFrame
    {
        protected Dimension canvasSize = new Dimension(800, 600);

        protected AppPanel wwjPanel;
        protected DynamicLayerPanel layerPanel;

        public AppFrame()
        {
            this.initialize();
        }

        protected void initialize()
        {
            // Create the WorldWindow.
            this.wwjPanel = new AppPanel(this.canvasSize);
            this.wwjPanel.setPreferredSize(canvasSize);

            // Put the pieces together.
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
            this.layerPanel = new DynamicLayerPanel(this.wwjPanel.wwd);

            this.getContentPane().add(this.layerPanel, BorderLayout.WEST);

            // Register a rendering listener that's notified when exceptions occur during rendering.
            this.wwjPanel.wwd.addRenderingListener(new RenderingListener()
            {
                @Override
                public void stageChanged(RenderingEvent event)
                {
                    layerPanel.updateLayerActivity(wwjPanel.wwd);
                }
            });

            this.pack();

            // Center the application on the screen.
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
        }
    }

    static
    {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static AppFrame start(String appName, Class appFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = (AppFrame) appFrameClass.newInstance();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });

            return frame;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Application", AppFrame.class);
    }
}
