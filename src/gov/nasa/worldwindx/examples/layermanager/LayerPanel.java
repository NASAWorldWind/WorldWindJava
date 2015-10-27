/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.layermanager;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Represents one layer in the layer manager's layer list.
 *
 * @author tag
 * @version $Id: LayerPanel.java 1179 2013-02-15 17:47:37Z tgaskins $
 */
public class LayerPanel extends JPanel
{
    public static final ImageIcon UP_ARROW =
        new ImageIcon(LayerPanel.class.getResource("/images/up_arrow_16x16.png"));
    public static final ImageIcon DOWN_ARROW =
        new ImageIcon(LayerPanel.class.getResource("/images/down_arrow_16x16.png"));

    protected Layer layer; // the layer represented by this instance

    protected JCheckBox checkBox; // the checkbox of this instance
    protected JButton upButton;
    protected JButton downButton;

    public LayerPanel(final WorldWindow wwd, final Layer layer)
    {
        super(new BorderLayout(10, 10));

        this.layer = layer;

        SelectLayerAction action = new SelectLayerAction(wwd, layer, layer.isEnabled());
        this.checkBox = new JCheckBox(action);
        this.checkBox.setSelected(action.selected);
        this.add(this.checkBox, BorderLayout.CENTER);

        this.upButton = new JButton(UP_ARROW);
        this.upButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                moveLayer(wwd, layer, -1);
            }
        });

        this.downButton = new JButton(DOWN_ARROW);
        this.downButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                moveLayer(wwd, layer, +1);
            }
        });

        // The buttons shouldn't look like actual JButtons.
        this.upButton.setBorderPainted(false);
        this.upButton.setContentAreaFilled(false);
        this.upButton.setPreferredSize(new Dimension(24, 24));
        this.downButton.setBorderPainted(false);
        this.downButton.setContentAreaFilled(false);
        this.downButton.setPreferredSize(new Dimension(24, 24));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 5, 0));
        buttonPanel.add(this.upButton);
        buttonPanel.add(this.downButton);
        this.add(buttonPanel, BorderLayout.EAST);

        int index = this.findLayerPosition(wwd, layer);
        this.upButton.setEnabled(index != 0);
        this.downButton.setEnabled(index != wwd.getModel().getLayers().size() - 1);
    }

    public Layer getLayer()
    {
        return this.layer;
    }

    public Font getLayerNameFont()
    {
        return this.checkBox.getFont();
    }

    public void setLayerNameFont(Font font)
    {
        this.checkBox.setFont(font);
    }

    protected void moveLayer(WorldWindow wwd, Layer layer, int direction)
    {
        // Moves the layer associated with this instance in the direction indicated relative to the other layers.

        int index = this.findLayerPosition(wwd, layer);
        if (index < 0)
            return; // layer not found

        LayerList layerList = wwd.getModel().getLayers();

        this.upButton.setEnabled(true);
        this.downButton.setEnabled(true);

        if (direction < 0 && index == 0) // can't move lowest layer any lower
        {
            this.upButton.setEnabled(false);
            return;
        }

        if (direction > 0 && index == layerList.size() - 1) // can't move highest layer any higher
        {
            this.downButton.setEnabled(false);
            return;
        }

        // Remove the layer from the layer list and then re-insert it.

        layerList.remove(layer);

        if (direction > 0)
            layerList.add(index + 1, layer);
        else if (direction < 0)
            layerList.add(index - 1, layer);

        // Update WorldWind so the change is visible.
        wwd.redraw();
    }

    protected int findLayerPosition(WorldWindow wwd, Layer layer)
    {
        // Determines the ordinal location of a layer in the layer list.

        for (int i = 0; i < wwd.getModel().getLayers().size(); i++)
        {
            if (layer == wwd.getModel().getLayers().get(i))
                return i;
        }

        return -1;
    }

    protected static class SelectLayerAction extends AbstractAction
    {
        // This action handles layer selection and de-selection.

        protected WorldWindow wwd;
        protected Layer layer;
        protected boolean selected;

        public SelectLayerAction(WorldWindow wwd, Layer layer, boolean selected)
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
            if (((JCheckBox) actionEvent.getSource()).isSelected())
                this.layer.setEnabled(true);
            else
                this.layer.setEnabled(false);

            wwd.redraw();
        }
    }
}
