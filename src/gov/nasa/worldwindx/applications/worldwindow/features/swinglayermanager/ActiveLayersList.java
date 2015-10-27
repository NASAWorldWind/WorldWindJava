/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
