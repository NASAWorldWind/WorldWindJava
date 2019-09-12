/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.shapebuilder;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ShapeUtils;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

/**
 * ExtrudedPolygonBuilder is a tool for creating and editing {@link gov.nasa.worldwind.render.ExtrudedPolygon}'s on the
 * surface of the globe.
 *
 * @author pabercrombie
 * @version $Id: ExtrudedPolygonBuilder.java 2109 2014-06-30 16:52:38Z tgaskins $
 * @see ExtrudedPolygon
 */

public class ExtrudedPolygonBuilder extends ApplicationTemplate
{
    protected static final String POLYGON_LAYER_NAME = "Polygons";
    protected static final String CLEAR_SELECTION = "ExtrudedPolygonBuilder.ClearSelection";
    protected static final String SIZE_NEW_SHAPES_TO_VIEWPORT = "ExtrudedPolygonBuilder.SizeNewShapesToViewport";
    protected static final String ENABLE_EDIT = "ExtrudedPolygonBuilder.EnableEdit";
    protected static final String NEW_POLYGON = "ExtrudedPolygonBuilder.NewPolygon";
    protected static final String REMOVE_SELECTED = "ExtrudedPolygonBuilder.RemoveSelected";
    protected static final String SELECTION_CHANGED = "ExtrudedPolygonBuilder.SelectionChanged";

    //*************************************************************//
    //********************  Polygon Builder Model  ****************//
    //*************************************************************//

    protected static class PolygonEntry extends WWObjectImpl
    {
        protected ExtrudedPolygon polygon;
        protected ShapeAttributes attributes;
        protected boolean editing = false;
        protected boolean selected = false;

        public PolygonEntry(ExtrudedPolygon polygon)
        {
            this.polygon = polygon;
            this.attributes = this.polygon.getAttributes();
        }

        public boolean isEditing()
        {
            return this.editing;
        }

        public void setEditing(boolean editing)
        {
            this.editing = editing;
            this.updateAttributes();
        }

        public boolean isSelected()
        {
            return this.selected;
        }

        public void setSelected(boolean selected)
        {
            this.selected = selected;
            this.updateAttributes();
        }

        public String getName()
        {
            return this.getStringValue(AVKey.DISPLAY_NAME);
        }

        public void setName(String name)
        {
            this.setValue(AVKey.DISPLAY_NAME, name);
        }

        public ExtrudedPolygon getPolygon()
        {
            return polygon;
        }

        public ShapeAttributes getAttributes()
        {
            return this.attributes;
        }

        public String toString()
        {
            return this.getName();
        }

        public Object getValue(String key)
        {
            Object value = super.getValue(key);
            if (value == null)
            {
                value = this.polygon.getValue(key);
            }
            return value;
        }

        public Object setValue(String key, Object value)
        {
            //noinspection StringEquality
            if (key == AVKey.DISPLAY_NAME)
            {
                return this.polygon.setValue(key, value);
            }
            else
            {
                return super.setValue(key, value);
            }
        }

        protected void updateAttributes()
        {
            if (this.isSelected())
            {
                this.polygon.setAttributes(getSelectionAttributes());
            }
            else
            {
                this.polygon.setAttributes(this.getAttributes());
            }
        }
    }

    protected static class PolygonBuilderModel extends AbstractTableModel
    {
        protected static String[] columnName = {"Name"};
        protected static Class[] columnClass = {String.class};
        protected static String[] columnAttribute = {AVKey.DISPLAY_NAME};

        protected ArrayList<PolygonEntry> entryList = new ArrayList<PolygonEntry>();

        public PolygonBuilderModel()
        {
        }

        public String getColumnName(int columnIndex)
        {
            return columnName[columnIndex];
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            return columnClass[columnIndex];
        }

        public int getRowCount()
        {
            return this.entryList.size();
        }

        public int getColumnCount()
        {
            return 1;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            PolygonEntry entry = this.entryList.get(rowIndex);
            return entry.getValue(columnAttribute[columnIndex]);
        }

        public void setValueAt(Object aObject, int rowIndex, int columnIndex)
        {
            PolygonEntry entry = this.entryList.get(rowIndex);
            String key = columnAttribute[columnIndex];
            entry.setValue(key, aObject);
        }

        public java.util.List<PolygonEntry> getEntries()
        {
            return Collections.unmodifiableList(this.entryList);
        }

        public void setEntries(Iterable<? extends PolygonEntry> entries)
        {
            this.entryList.clear();
            if (entries != null)
            {
                for (PolygonEntry entry : entries)
                {
                    this.entryList.add(entry);
                }
            }

            this.fireTableDataChanged();
        }

        public void addEntry(PolygonEntry entry)
        {
            this.entryList.add(entry);
            int index = this.entryList.size() - 1;
            this.fireTableRowsInserted(index, index);
        }

        public void removeEntry(PolygonEntry entry)
        {
            int index = this.entryList.indexOf(entry);
            if (index != -1)
            {
                this.entryList.remove(entry);
                this.fireTableRowsDeleted(index, index);
            }
        }

        public void removeAllEntries()
        {
            this.entryList.clear();
            this.fireTableDataChanged();
        }

        public PolygonEntry getEntry(int index)
        {
            return this.entryList.get(index);
        }

        public PolygonEntry setEntry(int index, PolygonEntry entry)
        {
            return this.entryList.set(index, entry);
        }

        public int getIndexForEntry(PolygonEntry entry)
        {
            return this.entryList.indexOf(entry);
        }
    }

    protected static final double DEFAULT_SHAPE_SIZE_METERS = 200000.0; // 200 km

    protected static class ExtrudedPolygonFactory
    {
        public ExtrudedPolygonFactory()
        {
        }

        public ExtrudedPolygon createPolygon(WorldWindow wwd, boolean fitShapeToViewport)
        {
            ExtrudedPolygon poly = new ExtrudedPolygon();
            poly.setAttributes(getDefaultAttributes());
            poly.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializePolygon(wwd, poly, fitShapeToViewport);

            return poly;
        }

        protected void initializePolygon(WorldWindow wwd, ExtrudedPolygon polygon, boolean fitShapeToViewport)
        {
            // Creates a rectangle in the center of the viewport. Attempts to guess at a reasonable size and height.

            Position position = ShapeUtils.getNewShapePosition(wwd);
            Angle heading = ShapeUtils.getNewShapeHeading(wwd, true);
            double heightInMeters = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) : DEFAULT_SHAPE_SIZE_METERS;

            java.util.List<Position> locations = ShapeUtils.createPositionSquareInViewport(wwd, position, heading,
                heightInMeters);

            polygon.setOuterBoundary(locations);
            polygon.setHeight(heightInMeters);
            polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        }

        public String toString()
        {
            return "Polygon";
        }
    }

    public static ShapeAttributes getDefaultAttributes()
    {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(new Material(Color.BLACK, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK, 0.0f));
        attributes.setOutlineMaterial(Material.DARK_GRAY);
        attributes.setDrawOutline(true);
        attributes.setInteriorOpacity(0.95);
        attributes.setOutlineOpacity(.95);
        attributes.setOutlineWidth(2);
        return attributes;
    }

    public static ShapeAttributes getSelectionAttributes()
    {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(Material.WHITE);
        attributes.setOutlineMaterial(Material.BLACK);
        attributes.setDrawOutline(true);
        attributes.setInteriorOpacity(0.8);
        attributes.setOutlineOpacity(0.8);
        attributes.setOutlineWidth(2);
        return attributes;
    }

    public static String getNextName(String base)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(nextEntryNumber++);
        return sb.toString();
    }

    protected static long nextEntryNumber = 1;

    //*************************************************************//
    //********************  Polygon Builder Panel  ****************//
    //*************************************************************//

    protected static class PolygonBuilderPanel extends JPanel
    {
        protected JTable entryTable;
        protected boolean ignoreSelectEvents = false;

        public PolygonBuilderPanel(PolygonBuilderModel model, PolygonBuilderController controller)
        {
            this.initComponents(model, controller);
        }

        public int[] getSelectedIndices()
        {
            return this.entryTable.getSelectedRows();
        }

        public void setSelectedIndices(int[] indices)
        {
            this.ignoreSelectEvents = true;

            if (indices != null && indices.length != 0)
            {
                for (int index : indices)
                {
                    this.entryTable.setRowSelectionInterval(index, index);
                }
            }
            else
            {
                this.entryTable.clearSelection();
            }

            this.ignoreSelectEvents = false;
        }

        protected void initComponents(PolygonBuilderModel model, final PolygonBuilderController controller)
        {
            final JCheckBox resizeNewShapesCheckBox;
            final JCheckBox enableEditCheckBox;

            JPanel newShapePanel = new JPanel();
            {
                JButton newShapeButton = new JButton("New shape");
                newShapeButton.setActionCommand(NEW_POLYGON);
                newShapeButton.addActionListener(controller);
                newShapeButton.setToolTipText("Create a new shape centered in the viewport");

                resizeNewShapesCheckBox = new JCheckBox("Fit new shapes to viewport");
                resizeNewShapesCheckBox.setActionCommand(SIZE_NEW_SHAPES_TO_VIEWPORT);
                resizeNewShapesCheckBox.addActionListener(controller);
                resizeNewShapesCheckBox.setSelected(controller.isResizeNewShapesToViewport());
                resizeNewShapesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                resizeNewShapesCheckBox.setToolTipText("New shapes are sized to fit the geographic viewport");

                enableEditCheckBox = new JCheckBox("Enable shape editing");
                enableEditCheckBox.setActionCommand(ENABLE_EDIT);
                enableEditCheckBox.addActionListener(controller);
                enableEditCheckBox.setSelected(controller.isEnableEdit());
                enableEditCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                enableEditCheckBox.setToolTipText("Allow modifications to shapes");

                Box newShapeBox = Box.createHorizontalBox();
                newShapeBox.add(newShapeButton);
                newShapeBox.add(Box.createHorizontalStrut(5));
                newShapeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel gridPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap, vgap
                gridPanel.add(newShapeBox);
                gridPanel.add(resizeNewShapesCheckBox);
                gridPanel.add(enableEditCheckBox);

                newShapePanel.setLayout(new BorderLayout());
                newShapePanel.add(gridPanel, BorderLayout.NORTH);
            }

            JPanel entryPanel = new JPanel();
            {
                this.entryTable = new JTable(model);
                this.entryTable.setColumnSelectionAllowed(false);
                this.entryTable.setRowSelectionAllowed(true);
                this.entryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                this.entryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!ignoreSelectEvents)
                        {
                            controller.actionPerformed(new ActionEvent(e.getSource(), -1, SELECTION_CHANGED));
                        }
                    }
                });
                this.entryTable.setToolTipText("<html>Click to select<br>Double-Click to rename</html>");

                JScrollPane tablePane = new JScrollPane(this.entryTable);
                tablePane.setPreferredSize(new Dimension(200, 100));

                entryPanel.setLayout(new BorderLayout(0, 0)); // hgap, vgap
                entryPanel.add(tablePane, BorderLayout.CENTER);
            }

            JPanel selectionPanel = new JPanel();
            {
                JButton delselectButton = new JButton("Deselect");
                delselectButton.setActionCommand(CLEAR_SELECTION);
                delselectButton.addActionListener(controller);
                delselectButton.setToolTipText("Clear the selection");

                JButton deleteButton = new JButton("Delete Selected");
                deleteButton.setActionCommand(REMOVE_SELECTED);
                deleteButton.addActionListener(controller);
                deleteButton.setToolTipText("Delete selected shapes");

                JPanel gridPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap, vgap
                gridPanel.add(delselectButton);
                gridPanel.add(deleteButton);

                selectionPanel.setLayout(new BorderLayout());
                selectionPanel.add(gridPanel, BorderLayout.NORTH);
            }

            this.setLayout(new BorderLayout(30, 0)); // hgap, vgap
            this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right
            this.add(newShapePanel, BorderLayout.WEST);
            this.add(entryPanel, BorderLayout.CENTER);
            this.add(selectionPanel, BorderLayout.EAST);

            controller.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent e)
                {
                    //noinspection StringEquality
                    if (e.getPropertyName() == SIZE_NEW_SHAPES_TO_VIEWPORT)
                    {
                        resizeNewShapesCheckBox.setSelected(controller.isResizeNewShapesToViewport());
                    }
                    else //noinspection StringEquality
                        if (e.getPropertyName() == ENABLE_EDIT)
                        {
                            enableEditCheckBox.setSelected(controller.isEnableEdit());
                        }
                }
            });
        }
    }

    //**************************************************************//
    //********************  Polygon Builder Controller  ***********//
    //**************************************************************//

    protected static class PolygonBuilderController extends WWObjectImpl implements ActionListener, MouseListener
    {
        protected AppFrame app;
        protected PolygonBuilderModel model;
        protected PolygonBuilderPanel view;
        protected PolygonEntry selectedEntry;
        protected ExtrudedPolygonEditor editor;
        protected boolean enabled = true;
        protected boolean enableEdit = true;
        protected boolean resizeNewShapes;

        public PolygonBuilderController(AppFrame app)
        {
            this.app = app;
            this.editor = new ExtrudedPolygonEditor();

            // The ordering is important here; we want first pass at mouse events.
            this.editor.setWorldWindow(this.app.getWwd());
            this.app.getWwd().getInputHandler().addMouseListener(this);
        }

        public AppFrame getApp()
        {
            return this.app;
        }

        public PolygonBuilderModel getModel()
        {
            return this.model;
        }

        public void setModel(PolygonBuilderModel model)
        {
            this.model = model;
        }

        public PolygonBuilderPanel getView()
        {
            return this.view;
        }

        public void setView(PolygonBuilderPanel view)
        {
            this.view = view;
        }

        public boolean isEnabled()
        {
            return this.enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
            getView().setEnabled(enabled);
            getApp().setEnabled(enabled);
        }

        public boolean isEnableEdit()
        {
            return this.enableEdit;
        }

        public void setEnableEdit(boolean enable)
        {
            this.enableEdit = enable;
            this.handleEnableEdit(enable);
            this.firePropertyChange(ENABLE_EDIT, null, enable);
        }

        public boolean isResizeNewShapesToViewport()
        {
            return this.resizeNewShapes;
        }

        public void setResizeNewShapesToViewport(boolean resize)
        {
            this.resizeNewShapes = resize;
            this.firePropertyChange(SIZE_NEW_SHAPES_TO_VIEWPORT, null, resize);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!this.isEnabled())
            {
                return;
            }

            //noinspection StringEquality
            if (e.getActionCommand() == NEW_POLYGON)
            {
                this.createNewEntry(new ExtrudedPolygonFactory());
            }
            else //noinspection StringEquality
                if (e.getActionCommand() == CLEAR_SELECTION)
                {
                    this.selectEntry(null, true);
                }
                else //noinspection StringEquality
                    if (e.getActionCommand() == SIZE_NEW_SHAPES_TO_VIEWPORT)
                    {
                        if (e.getSource() instanceof AbstractButton)
                        {
                            boolean selected = ((AbstractButton) e.getSource()).isSelected();
                            this.setResizeNewShapesToViewport(selected);
                        }
                    }
                    else //noinspection StringEquality
                        if (e.getActionCommand() == ENABLE_EDIT)
                        {
                            if (e.getSource() instanceof AbstractButton)
                            {
                                boolean selected = ((AbstractButton) e.getSource()).isSelected();
                                this.setEnableEdit(selected);
                            }
                        }
                        else //noinspection StringEquality
                            if (e.getActionCommand() == REMOVE_SELECTED)
                            {
                                this.removeEntries(Arrays.asList(this.getSelectedEntries()));
                            }
                            else //noinspection StringEquality
                                if (e.getActionCommand() == SELECTION_CHANGED)
                                {
                                    this.viewSelectionChanged();
                                }
        }

        public void mouseClicked(MouseEvent e)
        {
        }

        public void mousePressed(MouseEvent e)
        {
            if (e == null || e.isConsumed())
            {
                return;
            }

            if (!this.isEnabled())
            {
                return;
            }

            //noinspection StringEquality
            if (e.getButton() == MouseEvent.BUTTON1)
            {
                this.handleSelect();
            }
        }

        public void mouseReleased(MouseEvent e)
        {
        }

        public void mouseEntered(MouseEvent e)
        {
        }

        public void mouseExited(MouseEvent e)
        {
        }

        protected void handleSelect()
        {
            // If the picked object is null or something other than a polygon, then ignore the mouse click. If we
            // deselect the current entry at this point, the user cannot easily navigate without losing the selection.

            PickedObjectList pickedObjects = this.getApp().getWwd().getObjectsAtCurrentPosition();

            Object topObject = pickedObjects.getTopObject();
            if (!(topObject instanceof ExtrudedPolygon))
                return;

            PolygonEntry pickedEntry = this.getEntryFor((ExtrudedPolygon) topObject);
            if (pickedEntry == null)
                return;

            if (this.getSelectedEntry() != pickedEntry)
            {
                this.selectEntry(pickedEntry, true);
            }
        }

        protected void handleEnableEdit(boolean enable)
        {
            if (this.getSelectedEntry() == null)
                return;

            if (this.isSelectionEditing() != enable)
                this.setSelectionEditing(enable);
        }

        public void createNewEntry(ExtrudedPolygonFactory factory)
        {
            ExtrudedPolygon polygon = factory.createPolygon(this.getApp().getWwd(), this.isResizeNewShapesToViewport());
            PolygonEntry entry = new PolygonEntry(polygon);

            this.addEntry(entry);

            this.selectEntry(entry, true);
        }

        public void removeEntries(Iterable<? extends PolygonEntry> entries)
        {
            if (entries != null)
            {
                for (PolygonEntry entry : entries)
                {
                    this.removeEntry(entry);
                }
            }
        }

        public void addEntry(PolygonEntry entry)
        {
            this.getModel().addEntry(entry);

            this.getApp().getPolygonLayer().addRenderable(entry.getPolygon());
            this.getApp().getWwd().redraw();
        }

        public void removeEntry(PolygonEntry entry)
        {
            if (this.getSelectedEntry() == entry)
            {
                this.selectEntry(null, true);
            }

            this.getModel().removeEntry(entry);

            this.getApp().getPolygonLayer().removeRenderable(entry.getPolygon());
            this.getApp().getWwd().redraw();
        }

        public PolygonEntry getSelectedEntry()
        {
            return this.selectedEntry;
        }

        public void selectEntry(PolygonEntry entry, boolean updateView)
        {
            this.setSelectedEntry(entry);

            if (updateView)
            {
                if (entry != null)
                {
                    int index = this.getModel().getIndexForEntry(entry);
                    this.getView().setSelectedIndices(new int[] {index});
                }
                else
                {
                    this.getView().setSelectedIndices(new int[0]);
                }
            }

            if (this.isEnableEdit())
            {
                if (this.getSelectedEntry() != null && !this.isSelectionEditing())
                {
                    this.setSelectionEditing(true);
                }
            }

            this.getApp().getWwd().redraw();
        }

        protected void setSelectedEntry(PolygonEntry entry)
        {
            if (this.selectedEntry != null)
            {
                if (this.selectedEntry != entry && this.selectedEntry.isEditing())
                {
                    this.setSelectionEditing(false);
                }

                this.selectedEntry.setSelected(false);
            }

            this.selectedEntry = entry;

            if (this.selectedEntry != null)
            {
                this.selectedEntry.setSelected(true);
            }
        }

        protected boolean isSelectionEditing()
        {
            return this.selectedEntry != null && this.selectedEntry.isEditing();
        }

        protected void setSelectionEditing(boolean editing)
        {
            if (this.selectedEntry == null)
            {
                throw new IllegalStateException();
            }

            if (this.selectedEntry.isEditing() == editing)
            {
                throw new IllegalStateException();
            }

            this.selectedEntry.setEditing(editing);

            this.editor.setPolygon(this.selectedEntry.getPolygon());
            this.editor.setArmed(editing);

            if (editing)
            {
                insertBeforePlacenames(this.getApp().getWwd(), this.editor);
            }
            else
            {
                this.getApp().getWwd().getModel().getLayers().remove(this.editor);
            }

            int index = this.getModel().getIndexForEntry(this.selectedEntry);
            this.getModel().fireTableRowsUpdated(index, index);
        }

        protected void viewSelectionChanged()
        {
            int[] indices = this.getView().getSelectedIndices();
            if (indices != null)
            {
                for (PolygonEntry entry : this.getEntriesFor(indices))
                {
                    this.selectEntry(entry, false);
                }
            }

            this.getApp().getWwd().redraw();
        }

        protected PolygonEntry[] getSelectedEntries()
        {
            int[] indices = this.getView().getSelectedIndices();
            if (indices != null)
            {
                return this.getEntriesFor(indices);
            }

            return new PolygonEntry[0];
        }

        protected PolygonEntry[] getEntriesFor(int[] indices)
        {
            PolygonEntry[] entries = new PolygonEntry[indices.length];
            for (int i = 0; i < indices.length; i++)
            {
                entries[i] = this.getModel().getEntry(indices[i]);
            }
            return entries;
        }

        protected PolygonEntry getEntryFor(ExtrudedPolygon polygon)
        {
            for (PolygonEntry entry : this.getModel().getEntries())
            {
                if (entry.getPolygon() == polygon)
                {
                    return entry;
                }
            }
            return null;
        }
    }

    //**************************************************************//
    //********************  Main  **********************************//
    //**************************************************************//

    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        // Polygon layer and editor UI components.
        protected RenderableLayer polygonLayer;
        protected PolygonBuilderModel builderModel;
        protected PolygonBuilderPanel builderView;
        protected PolygonBuilderController builderController;

        public AppFrame()
        {
            this.polygonLayer = new RenderableLayer();
            this.polygonLayer.setName(POLYGON_LAYER_NAME);
            insertBeforePlacenames(this.getWwd(), this.polygonLayer);

            this.builderController = new PolygonBuilderController(this);
            this.builderModel = new PolygonBuilderModel();
            this.builderView = new PolygonBuilderPanel(this.builderModel, this.builderController);
            this.getContentPane().add(this.builderView, BorderLayout.SOUTH);

            this.builderController.setModel(this.builderModel);
            this.builderController.setView(this.builderView);
            this.builderController.setResizeNewShapesToViewport(true);

            makeMenuBar(this, this.builderController);
        }

        public PolygonBuilderPanel getPolygonBuilderPanel()
        {
            return this.builderView;
        }

        public RenderableLayer getPolygonLayer()
        {
            return this.polygonLayer;
        }

        public static void makeMenuBar(JFrame frame, final PolygonBuilderController controller)
        {
            JMenuBar menuBar = new JMenuBar();
            final JMenuItem newShapeMenuItem;
            final JCheckBoxMenuItem resizeNewShapesItem;
            final JCheckBoxMenuItem enableEditItem;

            JMenu menu = new JMenu("Shape");
            {
                newShapeMenuItem = new JMenuItem("New polygon");
                newShapeMenuItem.setActionCommand(NEW_POLYGON);
                newShapeMenuItem.addActionListener(controller);
                menu.add(newShapeMenuItem);

                resizeNewShapesItem = new JCheckBoxMenuItem("Fit new shapes to viewport");
                resizeNewShapesItem.setActionCommand(SIZE_NEW_SHAPES_TO_VIEWPORT);
                resizeNewShapesItem.addActionListener(controller);
                resizeNewShapesItem.setState(controller.isResizeNewShapesToViewport());
                menu.add(resizeNewShapesItem);

                enableEditItem = new JCheckBoxMenuItem("Enable shape editing");
                enableEditItem.setActionCommand(ENABLE_EDIT);
                enableEditItem.addActionListener(controller);
                enableEditItem.setState(controller.isEnableEdit());
                menu.add(enableEditItem);
            }
            menuBar.add(menu);

            menu = new JMenu("Selection");
            {
                JMenuItem item = new JMenuItem("Deselect");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                item.setActionCommand(CLEAR_SELECTION);
                item.addActionListener(controller);
                menu.add(item);

                item = new JMenuItem("Delete");
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
                item.setActionCommand(REMOVE_SELECTED);
                item.addActionListener(controller);
                menu.add(item);
            }
            menuBar.add(menu);

            frame.setJMenuBar(menuBar);

            controller.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent e)
                {
                    //noinspection StringEquality
                    if (e.getPropertyName() == SIZE_NEW_SHAPES_TO_VIEWPORT)
                    {
                        resizeNewShapesItem.setSelected(controller.isResizeNewShapesToViewport());
                    }
                    else //noinspection StringEquality
                        if (e.getPropertyName() == ENABLE_EDIT)
                        {
                            enableEditItem.setSelected(controller.isEnableEdit());
                        }
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Extruded Polygon Builder", AppFrame.class);
    }
}
