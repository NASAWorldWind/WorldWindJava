/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * @author tag
 * @version $Id: LayerTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerTree extends JTree
{
    public LayerTree(LayerTreeModel model)
    {
        super(model);

        this.initialize();
    }

    protected void initialize()
    {
        this.setOpaque(false);
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.setCellRenderer(new LayerNodeTreeCellRenderer());

        this.setEditable(true); // allows use of customized cell editor for the check boxes
        this.setCellEditor(new LayerNodeTreeCellEditor(this));

        ToolTipManager.sharedInstance().registerComponent(this);

        this.expandRow(0); // ensure that the top level entry is expanded
    }

    /** This is the component displayed by the individual tree entries. It's a panel with icon, check box and name. */
    protected static class CellPanel extends JPanel
    {
        private static final ImageIcon EARTH_ICON =
            new ImageIcon(LayerNodeTreeCellRenderer.class.getResource("/images/16x16-icon-earth.png"));

        protected JCheckBox checkBox;
        protected JLabel layerTitle = new JLabel(EARTH_ICON);

        public CellPanel()
        {
            super(new BorderLayout(5, 5));

            this.setOpaque(false);
            Font font = UIManager.getFont("Tree.font");
            if (font != null)
                this.setFont(font);

            this.setOpaque(false);

            JPanel boxAndIconPanel = new JPanel(new GridLayout(1, 0, 0, 0));
            boxAndIconPanel.setOpaque(false);

            this.checkBox = new JCheckBox();
            this.checkBox.setOpaque(false);
            this.checkBox.setHorizontalTextPosition(0);
            this.checkBox.setIconTextGap(0);
            Boolean drawFocus = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
            this.checkBox.setFocusPainted((drawFocus != null) && drawFocus);
            boxAndIconPanel.add(this.checkBox);

            JLabel iconLabel = new JLabel(EARTH_ICON);
            iconLabel.setOpaque(false);
            boxAndIconPanel.add(iconLabel);

            this.layerTitle = new JLabel();
            this.layerTitle.setOpaque(false);
            this.layerTitle.setAlignmentX(SwingConstants.LEFT);

            this.add(boxAndIconPanel, BorderLayout.WEST);
            this.add(this.layerTitle, BorderLayout.CENTER);
        }
    }

    public boolean isShowInternalNodes()
    {
        return ((LayerTreeModel) this.getModel()).isIncludeInternalLayers();
    }

    public void setShowInternalNodes(boolean showInternalNodes)
    {
        ((LayerTreeModel) this.getModel()).setIncludeInternalLayers(showInternalNodes);
    }

    // Removes the entries from the tree by assigning the tree a new model
    public void clearTree()
    {
        LayerTreeModel layerModel = (LayerTreeModel) this.getModel();
        if (layerModel == null || layerModel.getRoot() == null)
            return;

        this.setModel(new LayerTreeModel());
    }

    /** The customized tree cell renderer. */
    protected static class LayerNodeTreeCellRenderer extends DefaultTreeCellRenderer
    {
        private CellPanel leafRenderer = new CellPanel();
        private CellPanel groupRenderer = new CellPanel();

        private LayerNodeTreeCellRenderer()
        {
            this.groupRenderer.setOpaque(false);
        }

        protected CellPanel getLeafRenderer()
        {
            return this.leafRenderer;
        }

        protected CellPanel getGroupRenderer()
        {
            return this.groupRenderer;
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus)
        {
            LayerNode layerNode = (LayerNode) value;

            if (!leaf) // is a group node
            {
                if (!layerNode.isEnableSelectionBox())
                {
                    this.groupRenderer.layerTitle.setText(layerNode.toString());
                    this.groupRenderer.setToolTipText(layerNode.getToolTipText());
                    this.groupRenderer.checkBox.setEnabled(false);
                    this.groupRenderer.checkBox.setSelected(true);

                    return this.groupRenderer;
                }
                else
                {
                    this.groupRenderer.layerTitle.setText(layerNode.toString());
                    this.groupRenderer.checkBox.setEnabled(true);
                    this.groupRenderer.checkBox.setSelected(layerNode.isSelected());
                    this.groupRenderer.setToolTipText(layerNode.getToolTipText());

                    return this.groupRenderer;
                }
            }
            else // is a leaf
            {
                this.leafRenderer.layerTitle.setText(layerNode.toString());
                this.leafRenderer.checkBox.setSelected(layerNode.isSelected());
                this.leafRenderer.setToolTipText(layerNode.getToolTipText());

                if (layerNode.getLayer() != null)
                {
                    // Ensure that renderer is reset
                    this.leafRenderer.layerTitle.setEnabled(true);
                    this.leafRenderer.checkBox.setEnabled(true);
                }

                return this.leafRenderer;
            }
        }
    }

    /** The customized tree cell editor. The only editable part is the check box. */
    private static class LayerNodeTreeCellEditor extends AbstractCellEditor implements TreeCellEditor
    {
        private LayerTree tree;
        private LayerNodeTreeCellRenderer renderer;
        private LayerNode currentValue;

        public LayerNodeTreeCellEditor(JTree tree)
        {
            this.tree = (LayerTree) tree;
            this.renderer = new LayerNodeTreeCellRenderer();

            this.renderer.getLeafRenderer().checkBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent itemEvent)
                {
                    currentValue.setSelected(itemEvent.getStateChange() == ItemEvent.SELECTED);
                    if (LayerNodeTreeCellEditor.this.stopCellEditing())
                        fireEditingStopped();
                }
            });

            this.renderer.getGroupRenderer().checkBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent itemEvent)
                {
                    boolean selected = itemEvent.getStateChange() == ItemEvent.SELECTED;
                    currentValue.setSelected(selected);
                    if (LayerNodeTreeCellEditor.this.stopCellEditing())
                        fireEditingStopped();
                }
            });
        }

        public Object getCellEditorValue()
        {
            return this.currentValue;
        }

        public boolean isCellEditable(EventObject event)
        {
            if (!(event instanceof MouseEvent))
                return false;

            MouseEvent mouseEvent = (MouseEvent) event;
            TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
            if (path == null)
                return false;

            LayerNode layerNode = (LayerNode) path.getLastPathComponent();

            return layerNode != null && layerNode.isEnableSelectionBox();
        }

        public Component getTreeCellEditorComponent(JTree tree, final Object value, boolean selected, boolean expanded,
            boolean leaf, int row)
        {
            this.currentValue = (LayerNode) value;
            return this.renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
        }
    }
}
