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

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.applications.worldwindow.core.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: ActiveLayersList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
@SuppressWarnings("unchecked")
public class ActiveLayersList extends JList
{
    public ActiveLayersList(ListModel listModel)
    {
        super(listModel);

        this.setOpaque(false);
        this.setCellRenderer(new LayerCellRenderer());
    }

    // Indicates whether internal layers, those whose attribute-value list contains {@link Constants#INTERNAL_LAYER}
    // are shown.
    public boolean isIncludeInternalLayers()
    {
        return ((LayerCellRenderer) this.getCellRenderer()).isIncludeInternalLayers();
    }

    // Indicates whether internal layer should be shown.
    public void setIncludeInternalLayers(boolean includeInternalLayers)
    {
        if (includeInternalLayers == this.isIncludeInternalLayers())
            return;

        ((LayerCellRenderer) this.getCellRenderer()).setIncludeInternalLayers(includeInternalLayers);
    }

    // The class provides the cell renderer that enables the layer's check box to be edited.
    private static class LayerCellRenderer extends DefaultListCellRenderer
    {
        private LayerTree.CellPanel renderer = new LayerTree.CellPanel();
        private JPanel zeroSizeComponent;
        private Color selectionForeground, selectionBackground, textForeground, textBackground;
        private boolean includeInternalLayers = false;

        public LayerCellRenderer()
        {
            selectionForeground = UIManager.getColor("List.selectionForeground");
            selectionBackground = UIManager.getColor("List.selectionBackground");
            textForeground = UIManager.getColor("List.textForeground");
            textBackground = UIManager.getColor("List.textBackground");

            this.zeroSizeComponent = new JPanel();
            this.zeroSizeComponent.setOpaque(false);
            this.zeroSizeComponent.setPreferredSize(new Dimension(0, 0));
        }

        public boolean isIncludeInternalLayers()
        {
            return includeInternalLayers;
        }

        public void setIncludeInternalLayers(boolean includeInternalLayers)
        {
            this.includeInternalLayers = includeInternalLayers;
        }

        protected LayerTree.CellPanel getRenderer()
        {
            return this.renderer;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected,
            boolean hasFocus)
        {
            if (!(value instanceof Layer))
                return this.zeroSizeComponent; // Do not display anything but layers

            Layer layer = (Layer) value;

            if ((layer.getValue(Constants.INTERNAL_LAYER) != null && !includeInternalLayers)
                && layer.getValue(Constants.ACTIVE_LAYER) == null)
                return this.zeroSizeComponent; // Do not display internal layers

            renderer.layerTitle.setText(layer.getName());
            renderer.checkBox.setSelected(layer.isEnabled());
            renderer.layerTitle.setForeground(selected ? selectionForeground : textForeground);
            renderer.layerTitle.setBackground(selected ? selectionBackground : textBackground);
            renderer.layerTitle.setOpaque(selected);

            // Ensure that renderer is reset after drawing
            renderer.layerTitle.setEnabled(true);
            renderer.checkBox.setEnabled(true);

            return renderer;
        }
    }
}
