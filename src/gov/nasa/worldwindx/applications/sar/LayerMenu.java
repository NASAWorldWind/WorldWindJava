/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
