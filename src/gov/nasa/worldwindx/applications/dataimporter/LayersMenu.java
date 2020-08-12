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

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;

import javax.swing.*;
import java.awt.event.*;
import java.beans.*;

/**
 * Manages layer visibility for currently active layers.
 *
 * @author tag
 * @version $Id: LayersMenu.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class LayersMenu extends JMenu
{
    public LayersMenu(final WorldWindow wwd)
    {
        super("Layers");

        this.fill(wwd);

        wwd.getModel().getLayers().addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                if (propertyChangeEvent.getPropertyName().equals(AVKey.LAYERS))
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            update(wwd);
                        }
                    });
            }
        });
    }

    public void update(WorldWindow wwd)
    {
        this.fill(wwd);
    }

    protected void fill(WorldWindow wwd)
    {
        // First remove all the existing menu items.
        this.removeAll();

        // Fill the layers panel with the titles of all layers in the WorldWindow's current model.
        for (Layer layer : wwd.getModel().getLayers())
        {
            if (layer.getValue(AVKey.IGNORE) != null)
                continue;

            LayerAction action = new LayerAction(layer, wwd, layer.isEnabled());
            JCheckBoxMenuItem jcb = new JCheckBoxMenuItem(action);
            jcb.setSelected(action.selected);
            this.add(jcb);
        }
    }

    protected static class LayerAction extends AbstractAction
    {
        WorldWindow wwd;
        protected Layer layer;
        protected boolean selected;

        public LayerAction(Layer layer, WorldWindow wwd, boolean selected)
        {
            super(layer.getName());
            this.wwd = wwd;
            this.layer = layer;
            this.selected = selected;
            this.layer.setEnabled(this.selected);
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            // Simply enable or disable the layer based on its toggle button.
            if (((JCheckBoxMenuItem) actionEvent.getSource()).isSelected())
                this.layer.setEnabled(true);
            else
                this.layer.setEnabled(false);

            wwd.redraw();
        }
    }
}
