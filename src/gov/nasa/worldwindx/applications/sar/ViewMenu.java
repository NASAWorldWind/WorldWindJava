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
