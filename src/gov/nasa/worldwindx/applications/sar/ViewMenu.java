/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.applications.sar.render.PlaneModel;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.layers.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/**
 * @author jparsons
 * @version $Id: ViewMenu.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ViewMenu extends JMenu
{
    private WorldWindow wwd;

    public ViewMenu()
    {
        super("View");
    }

    public void setWwd(WorldWindow wwdInstance)
    {
        this.wwd = wwdInstance;

        // Layers
        for (Layer layer : wwd.getModel().getLayers())
        {
            if (isAbstractLayerMenuItem(layer))
            {
                JCheckBoxMenuItem mi = new JCheckBoxMenuItem(new LayerVisibilityAction(wwd, layer));
                mi.setState(layer.isEnabled());
                this.add(mi);
            }
        }

        // Terrain profile
        JMenuItem mi = new JMenuItem("Terrain profile...");
        mi.setMnemonic('T');
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                wwd.firePropertyChange(TerrainProfilePanel.TERRAIN_PROFILE_OPEN, null, null);
            }
        });
        this.add(mi);

        // Cloud ceiling contour
        mi = new JMenuItem("Cloud Contour...");
        mi.setMnemonic('C');
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                wwd.firePropertyChange(CloudCeilingPanel.CLOUD_CEILING_OPEN, null, null);
            }
        });
        this.add(mi);
    }

    private boolean isAbstractLayerMenuItem(Layer layer)
    {
        if (layer instanceof RenderableLayer)  //detect PlaneModel layer
        {
            Iterable<Renderable> iter = ((RenderableLayer)layer).getRenderables();
            for (Renderable rend: iter)
            {
                if (rend instanceof PlaneModel)
                    return true;
            }
        }

        return ((layer instanceof ScalebarLayer
                || layer instanceof CrosshairLayer
                || layer instanceof CompassLayer));  
    }

    private static class LayerVisibilityAction extends AbstractAction
    {
        private final Layer layer;
        private final WorldWindow wwd;

        public LayerVisibilityAction(WorldWindow wwd, Layer layer)
        {
            super(layer.getName());
            this.layer = layer;
            this.wwd = wwd;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            layer.setEnabled(((JCheckBoxMenuItem) actionEvent.getSource()).getState());
            this.wwd.redraw();
        }
    }
}
