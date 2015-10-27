/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: LayerMenu.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerMenu extends JMenu
{
    private WorldWindow wwd;

    public LayerMenu()
    {
        super("Layers");
    }

    public WorldWindow getWwd()
    {
        return wwd;
    }

    public void setWwd(WorldWindow wwd)
    {
        this.wwd = wwd;
        for (Layer layer : this.wwd.getModel().getLayers())
        {
            if (isLayerMenuItem(layer))
            {
                JCheckBoxMenuItem mi = new JCheckBoxMenuItem(new LayerVisibilityAction(this.wwd, layer, this));
                mi.setState(layer.isEnabled());
                this.add(mi);
            }
        }
    }

    private boolean isLayerMenuItem(Layer layer)
    {

        if (layer instanceof RenderableLayer)    //detect surface image layers
        {
            Iterable<Renderable> iter = ((RenderableLayer)layer).getRenderables();
            for (Renderable rend: iter)
            {
                if (rend instanceof SurfaceImage)
                    return true;
            }

            return false;
        }

        return ((layer instanceof TiledImageLayer)
            && !(layer instanceof BMNGWMSLayer));
    }

    private static class LayerVisibilityAction extends AbstractAction
    {
        private final Layer layer;
        private final WorldWindow wwd;
        private final LayerMenu menu;

        public LayerVisibilityAction(WorldWindow wwd, Layer layer, LayerMenu menu)
        {
            super(layer.getName());
            this.layer = layer;
            this.wwd = wwd;
            this.menu = menu;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            layer.setEnabled(((JCheckBoxMenuItem) actionEvent.getSource()).getState());
            if (layer instanceof BMNGOneImage) //toggle other BMNG layers
            {
                for (Layer lyr : this.wwd.getModel().getLayers())
                {
                    if (lyr instanceof BMNGWMSLayer)
                        lyr.setEnabled(((JCheckBoxMenuItem) actionEvent.getSource()).getState());
                }

            }
            menu.doClick(0); // keep layer menu open
            this.wwd.redraw();
        }
    }
}
