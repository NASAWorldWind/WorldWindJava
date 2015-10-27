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
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * RigidShapeBuilder is a tool for creating and editing 3D shapes and placing them on the globe. Possible shapes include
 * the Rigid Shapes ({@link gov.nasa.worldwind.render.Ellipsoid}, {@link gov.nasa.worldwind.render.Box}, {@link
 * gov.nasa.worldwind.render.Cylinder}, {@link gov.nasa.worldwind.render.Cone}, {@link
 * gov.nasa.worldwind.render.Pyramid} and {@link gov.nasa.worldwind.render.Wedge}) as well as {@link
 * gov.nasa.worldwind.render.ExtrudedPolygon}.  The RigidShapeBuilder user interface allows the user to select the
 * desired shape from a dropdown menu, create an instance of it with the click of a button, and specify an "edit mode"
 * for modifying the shape: move, scale, rotate, skew, or texture.  Numerous shapes may be created and placed on the
 * globe together, but only one may be selected and edited at any given time.
 * <p/>
 * Keyboard shortcuts allow the user to toggle easily between the various edit modes.  The shortcuts are as follows:
 * <p/>
 * Ctrl-Z:  move Ctrl-X:  scale Ctrl-C:  rotate Ctrl-V:  skew Ctrl-B:  texture
 * <p/>
 * Edited shapes are Restorable and may be saved to or loaded from a file using options in the File menu.
 *
 * @author ccrick
 * @version $Id: RigidShapeBuilder.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class RigidShapeBuilder extends ApplicationTemplate
{
    /* string constants used in the example application */

    protected static final String SHAPE_LAYER_NAME = "Rigid Shapes";
    /* action-related string constants */
    protected static final String CLEAR_SELECTION = "AbstractShapeBuilder.ClearSelection";
    protected static final String SIZE_NEW_SHAPES_TO_VIEWPORT = "AbstractShapeBuilder.SizeNewShapesToViewport";
    protected static final String ENABLE_EDIT = "AbstractShapeBuilder.EnableEdit";
    protected static final String KEEP_SHAPE_ABOVE_SURFACE = "AbstractShapeBuilder.KeepShapeAboveSurface";
    protected static final String SET_EDIT_MODE = "AbstractShapeBuilder.SetEditMode";
    protected static final String NEW_ABSTRACT_SHAPE = "AbstractShapeBuilder.NewAbstractShape";
    protected static final String REMOVE_SELECTED = "AbstractShapeBuilder.RemoveSelected";
    protected static final String OPEN = "AbstractShapeBuilder.Open";
    protected static final String SAVE = "AbstractShapeBuilder.Save";
    protected static final String SELECTION_CHANGED = "AbstractShapeBuilder.SelectionChanged";
    protected static final String EDIT_TEXTURE = "AbstractShapeBuilder.EditTexture";
    protected static final String OPEN_IMAGE_FILE = "OpenImageFile";

    //*************************************************************//
    //********************  Rigid Shape Builder Model  ****************//
    //*************************************************************//

    /**
     * The AbstractShapeEntry class defines a shape entry in the AbstractShapeBuilderModel.  Each entry contains an
     * instance of an AbstractShape, its associated editor and attributes, and state variables indicating whether the
     * entry is currently selected or being edited.
     */
    protected static class AbstractShapeEntry extends WWObjectImpl
    {
        protected AbstractShape shape;
        protected AbstractShapeEditor editor;
        protected ShapeAttributes attributes;
        protected boolean editing = false;
        protected boolean selected = false;

        public AbstractShapeEntry(AbstractShape shape, AbstractShapeEditor editor)
        {
            this.shape = shape;
            this.editor = editor;
            this.attributes = this.shape.getAttributes();
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

        public AbstractShape getShape()
        {
            return shape;
        }

        public AbstractShapeEditor getEditor()
        {
            return editor;
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
                value = this.shape.getValue(key);
            }
            return value;
        }

        public Object setValue(String key, Object value)
        {
            //noinspection StringEquality
            if (key == AVKey.DISPLAY_NAME)
            {
                return this.shape.setValue(key, value);
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
                this.shape.setAttributes(getSelectionAttributes());
            }
            else
            {
                this.shape.setAttributes(this.getAttributes());
            }
        }
    }

    protected static class AbstractShapeBuilderModel extends AbstractTableModel
    {
        protected static String[] columnName = {"Name"};
        protected static Class[] columnClass = {String.class};
        protected static String[] columnAttribute = {AVKey.DISPLAY_NAME};

        protected ArrayList<AbstractShapeEntry> entryList = new ArrayList<AbstractShapeEntry>();

        public AbstractShapeBuilderModel()
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
            AbstractShapeEntry entry = this.entryList.get(rowIndex);
            return entry.getValue(columnAttribute[columnIndex]);
        }

        public void setValueAt(Object aObject, int rowIndex, int columnIndex)
        {
            AbstractShapeEntry entry = this.entryList.get(rowIndex);
            String key = columnAttribute[columnIndex];
            entry.setValue(key, aObject);
        }

        public java.util.List<AbstractShapeEntry> getEntries()
        {
            return Collections.unmodifiableList(this.entryList);
        }

        public void setEntries(Iterable<? extends AbstractShapeEntry> entries)
        {
            this.entryList.clear();
            if (entries != null)
            {
                for (AbstractShapeEntry entry : entries)
                {
                    this.entryList.add(entry);
                }
            }

            this.fireTableDataChanged();
        }

        public void addEntry(AbstractShapeEntry entry)
        {
            this.entryList.add(entry);
            int index = this.entryList.size() - 1;
            this.fireTableRowsInserted(index, index);
        }

        public void removeEntry(AbstractShapeEntry entry)
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

        public AbstractShapeEntry getEntry(int index)
        {
            return this.entryList.get(index);
        }

        public AbstractShapeEntry setEntry(int index, AbstractShapeEntry entry)
        {
            return this.entryList.set(index, entry);
        }

        public int getIndexForEntry(AbstractShapeEntry entry)
        {
            return this.entryList.indexOf(entry);
        }
    }

    protected static EditMode[] defaultEditModes =
        {
            new EditMode("move", KeyEvent.VK_Z),
            new EditMode("scale", KeyEvent.VK_X),
            new EditMode("rotate", KeyEvent.VK_C),
            new EditMode("skew", KeyEvent.VK_V),
            new EditMode("texture", KeyEvent.VK_B)
        };

    protected static class EditMode
    {
        protected String mode;
        protected int shortcut;

        public EditMode(String mode, int shortcut)
        {
            this.mode = mode;
            this.shortcut = shortcut;
        }

        public String getMode()
        {
            return this.mode;
        }

        public int getShortcut()
        {
            return this.shortcut;
        }
    }

    protected static AbstractShapeFactory[] defaultAbstractShapeFactories = new AbstractShapeFactory[]
        {
            new EllipsoidFactory(),
            new BoxFactory(),
            new CylinderFactory(),
            new PyramidFactory(),
            new ConeFactory(),
            new WedgeFactory(),
            new ExtrudedPolygonFactory()
        };

    protected static final double DEFAULT_SHAPE_SIZE_METERS = 100000.0; // 200 km

    protected interface AbstractShapeFactory
    {
        AbstractShape createShape(WorldWindow wwd, boolean fitShapeToViewport);

        AbstractShapeEditor createEditor(AbstractShape shape);
    }

    protected static class EllipsoidFactory implements AbstractShapeFactory
    {
        public EllipsoidFactory()
        {
        }

        public RigidShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            RigidShape shape = new Ellipsoid();
            shape.setAttributes(getDefaultAttributes());
            shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializeShape(wwd, shape, fitShapeToViewport);

            return shape;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            RigidShapeEditor editor = new RigidShapeEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
        }

        protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport)
        {
            // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

            double radius = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
            Position position = ShapeUtils.getNewShapePosition(wwd);

            // adjust position height so shape sits on terrain surface
            Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
                position.getLongitude(), radius);
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

            shape.setCenterPosition(centerPosition);
            shape.setEastWestRadius(radius);
            shape.setVerticalRadius(radius);
            shape.setNorthSouthRadius(radius);
            shape.setHeading(Angle.ZERO);
            shape.setTilt(Angle.ZERO);
            shape.setRoll(Angle.ZERO);
            shape.setAltitudeMode(WorldWind.ABSOLUTE);
        }

        public String toString()
        {
            return "Ellipsoid";
        }
    }

    protected static class BoxFactory implements AbstractShapeFactory
    {
        public BoxFactory()
        {
        }

        public RigidShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            RigidShape shape = new Box();
            shape.setAttributes(getDefaultAttributes());
            shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializeShape(wwd, shape, fitShapeToViewport);

            return shape;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            RigidShapeEditor editor = new BoxEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
        }

        protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport)
        {
            // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

            double radius = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
            Position position = ShapeUtils.getNewShapePosition(wwd);

            // adjust position height so shape sits on terrain surface
            Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
                position.getLongitude(), radius);
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

            shape.setCenterPosition(centerPosition);
            shape.setEastWestRadius(radius);
            shape.setVerticalRadius(radius);
            shape.setNorthSouthRadius(radius);
            shape.setHeading(Angle.ZERO);
            shape.setTilt(Angle.ZERO);
            shape.setRoll(Angle.ZERO);
            shape.setAltitudeMode(WorldWind.ABSOLUTE);
        }

        public String toString()
        {
            return "Box";
        }
    }

    protected static class CylinderFactory implements AbstractShapeFactory
    {
        public CylinderFactory()
        {
        }

        public RigidShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            RigidShape shape = new Cylinder();
            shape.setAttributes(getDefaultAttributes());
            shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializeShape(wwd, shape, fitShapeToViewport);

            return shape;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            RigidShapeEditor editor = new CylinderEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
        }

        protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport)
        {
            // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

            double radius = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
            Position position = ShapeUtils.getNewShapePosition(wwd);

            // adjust position height so shape sits on terrain surface
            Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
                position.getLongitude(), radius);
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

            shape.setCenterPosition(centerPosition);
            shape.setEastWestRadius(radius);
            shape.setVerticalRadius(radius);
            shape.setNorthSouthRadius(radius);
            shape.setHeading(Angle.ZERO);
            shape.setTilt(Angle.ZERO);
            shape.setRoll(Angle.ZERO);
            shape.setAltitudeMode(WorldWind.ABSOLUTE);
        }

        public String toString()
        {
            return "Cylinder";
        }
    }

    protected static class PyramidFactory implements AbstractShapeFactory
    {
        public PyramidFactory()
        {
        }

        public RigidShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            RigidShape shape = new Pyramid();
            shape.setAttributes(getDefaultAttributes());
            shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializeShape(wwd, shape, fitShapeToViewport);

            return shape;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            RigidShapeEditor editor = new PyramidEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
        }

        protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport)
        {
            // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

            double radius = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
            Position position = ShapeUtils.getNewShapePosition(wwd);

            // adjust position height so shape sits on terrain surface
            Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
                position.getLongitude(), radius);
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

            shape.setCenterPosition(centerPosition);
            shape.setEastWestRadius(radius);
            shape.setVerticalRadius(radius);
            shape.setNorthSouthRadius(radius);

            shape.setHeading(Angle.ZERO);
            shape.setTilt(Angle.ZERO);
            shape.setRoll(Angle.ZERO);

            shape.setAltitudeMode(WorldWind.ABSOLUTE);
        }

        public String toString()
        {
            return "Pyramid";
        }
    }

    protected static class ConeFactory implements AbstractShapeFactory
    {
        public ConeFactory()
        {
        }

        public RigidShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            RigidShape shape = new Cone();
            shape.setAttributes(getDefaultAttributes());
            shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializeShape(wwd, shape, fitShapeToViewport);

            return shape;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            RigidShapeEditor editor = new ConeEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
        }

        protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport)
        {
            // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

            double radius = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
            Position position = ShapeUtils.getNewShapePosition(wwd);

            // adjust position height so shape sits on terrain surface
            Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
                position.getLongitude(), radius);
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

            shape.setCenterPosition(centerPosition);
            shape.setEastWestRadius(radius);
            shape.setVerticalRadius(radius);
            shape.setNorthSouthRadius(radius);
            shape.setHeading(Angle.ZERO);
            shape.setTilt(Angle.ZERO);
            shape.setRoll(Angle.ZERO);
            shape.setAltitudeMode(WorldWind.ABSOLUTE);
        }

        public String toString()
        {
            return "Cone";
        }
    }

    protected static class WedgeFactory implements AbstractShapeFactory
    {
        public WedgeFactory()
        {
        }

        public RigidShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            RigidShape shape = new Wedge();
            shape.setAttributes(getDefaultAttributes());
            shape.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializeShape(wwd, shape, fitShapeToViewport);

            return shape;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            RigidShapeEditor editor = new WedgeEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
        }

        protected void initializeShape(WorldWindow wwd, RigidShape shape, boolean fitShapeToViewport)
        {
            // Creates a shape in the center of the viewport. Attempts to guess at a reasonable size and height.

            double radius = fitShapeToViewport ?
                ShapeUtils.getViewportScaleFactor(wwd) / 2 : DEFAULT_SHAPE_SIZE_METERS;
            Position position = ShapeUtils.getNewShapePosition(wwd);

            // adjust position height so shape sits on terrain surface
            Vec4 centerPoint = wwd.getSceneController().getTerrain().getSurfacePoint(position.getLatitude(),
                position.getLongitude(), radius);
            Position centerPosition = wwd.getModel().getGlobe().computePositionFromPoint(centerPoint);

            shape.setCenterPosition(centerPosition);
            shape.setEastWestRadius(radius);
            shape.setVerticalRadius(radius);
            shape.setNorthSouthRadius(radius);
            shape.setHeading(Angle.ZERO);
            shape.setTilt(Angle.ZERO);
            shape.setRoll(Angle.ZERO);

            shape.setAltitudeMode(WorldWind.ABSOLUTE);

            // wedges only
            Wedge wedgeShape = (Wedge) shape;
            wedgeShape.setWedgeAngle(Angle.fromDegrees(220));
        }

        public String toString()
        {
            return "Wedge";
        }
    }

    protected static class ExtrudedPolygonFactory implements AbstractShapeFactory
    {
        public ExtrudedPolygonFactory()
        {
        }

        public AbstractShape createShape(WorldWindow wwd, boolean fitShapeToViewport)
        {
            ExtrudedPolygon poly = new ExtrudedPolygon();
            poly.setAttributes(getDefaultAttributes());
            poly.setValue(AVKey.DISPLAY_NAME, getNextName(toString()));
            this.initializePolygon(wwd, poly, fitShapeToViewport);

            return poly;
        }

        public AbstractShapeEditor createEditor(AbstractShape shape)
        {
            ExtrudedPolygonEditor editor = new ExtrudedPolygonEditor();
            shape.setAltitudeMode(editor.getAltitudeMode());
            editor.setShape(shape);
            return editor;
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
            polygon.setAltitudeMode(WorldWind.ABSOLUTE);
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
        attributes.setDrawOutline(false);
        attributes.setInteriorOpacity(.75);
        attributes.setOutlineOpacity(.95);
        attributes.setOutlineWidth(2);
        attributes.setEnableLighting(true);
        return attributes;
    }

    public static ShapeAttributes getSelectionAttributes()
    {
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setInteriorMaterial(Material.WHITE);
        attributes.setOutlineMaterial(Material.BLACK);
        attributes.setDrawOutline(false);
        attributes.setInteriorOpacity(0.85);
        attributes.setOutlineOpacity(0.8);
        attributes.setOutlineWidth(2);
        attributes.setEnableLighting(true);
        return attributes;
    }

    @SuppressWarnings( {"UnusedParameters"})
    public static void setEditorAttributes(AbstractShapeEditor editor)
    {
        // TODO
    }

    public static String getNextName(String base)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(nextEntryNumber++);
        return sb.toString();
    }

    private static AbstractShapeEditor getEditorFor(AbstractShape shape)
    {
        if (shape instanceof ExtrudedPolygon)
        {
            // TODO
        }
        else if (shape instanceof Ellipsoid)
        {
            RigidShapeEditor editor = new RigidShapeEditor();
            editor.setShape(shape);
            //setEditorAttributes(editor);
            return editor;
        }
        else if (shape instanceof Box)
        {
            RigidShapeEditor editor = new BoxEditor();
            editor.setShape(shape);
            //setEditorAttributes(editor);
            return editor;
        }
        else if (shape instanceof Cylinder)
        {
            RigidShapeEditor editor = new CylinderEditor();
            editor.setShape(shape);
            //setEditorAttributes(editor);
            return editor;
        }
        else if (shape instanceof Pyramid)
        {
            RigidShapeEditor editor = new PyramidEditor();
            editor.setShape(shape);
            //setEditorAttributes(editor);
            return editor;
        }
        else if (shape instanceof Cone)
        {
            RigidShapeEditor editor = new ConeEditor();
            editor.setShape(shape);
            //setEditorAttributes(editor);
            return editor;
        }
        else if (shape instanceof Wedge)
        {
            RigidShapeEditor editor = new WedgeEditor();
            editor.setShape(shape);
            //setEditorAttributes(editor);
            return editor;
        }

        return null;
    }

    protected static long nextEntryNumber = 1;

    //********************************************************************//
    //********************  Abstract Shape Builder Panel  ****************//
    //********************************************************************//

    @SuppressWarnings("unchecked")
    protected static class AbstractShapeBuilderPanel extends JPanel
    {
        private JComboBox factoryComboBox;
        private JComboBox editModeComboBox;
        protected JTable entryTable;
        protected JTextField textureBox;
        protected boolean ignoreSelectEvents = false;

        public AbstractShapeBuilderPanel(AbstractShapeBuilderModel model, AbstractShapeBuilderController controller)
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

        public AbstractShapeFactory getSelectedFactory()
        {
            return (AbstractShapeFactory) this.factoryComboBox.getSelectedItem();
        }

        public void setSelectedFactory(AbstractShapeFactory factory)
        {
            this.factoryComboBox.setSelectedItem(factory);
        }

        public String getSelectedEditMode()
        {
            return (String) this.editModeComboBox.getSelectedItem();
        }

        public void setSelectedEditMode(String editMode)
        {
            this.editModeComboBox.setSelectedItem(editMode);
        }

        protected void initComponents(AbstractShapeBuilderModel model, final AbstractShapeBuilderController controller)
        {
            final JCheckBox resizeNewShapesCheckBox;
            final JCheckBox enableEditCheckBox;
            final JCheckBox aboveGroundCheckBox;

            JPanel newShapePanel = new JPanel();
            {
                JButton newShapeButton = new JButton("New shape");
                newShapeButton.setActionCommand(NEW_ABSTRACT_SHAPE);
                newShapeButton.addActionListener(controller);
                newShapeButton.setToolTipText("Create a new shape centered in the viewport");

                this.factoryComboBox = new JComboBox(defaultAbstractShapeFactories);
                this.factoryComboBox.setEditable(false);
                this.factoryComboBox.setToolTipText("Choose shape type to create");

                resizeNewShapesCheckBox = new JCheckBox("Fit new shapes to viewport");
                resizeNewShapesCheckBox.setActionCommand(SIZE_NEW_SHAPES_TO_VIEWPORT);
                resizeNewShapesCheckBox.addActionListener(controller);
                resizeNewShapesCheckBox.setSelected(controller.isResizeNewShapesToViewport());
                resizeNewShapesCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                resizeNewShapesCheckBox.setToolTipText("New shapes are sized to fit the geographic viewport");

                JLabel editModeLabel = new JLabel("Edit Mode:");
                this.editModeComboBox = new JComboBox();
                for (EditMode editMode : defaultEditModes)
                {
                    this.editModeComboBox.addItem(editMode.getMode());
                }
                this.editModeComboBox.setActionCommand(SET_EDIT_MODE);
                this.editModeComboBox.addActionListener(controller);
                this.editModeComboBox.setEditable(false);
                this.editModeComboBox.setToolTipText("Choose edit mode (Ctrl-Z, Ctrl-X, Ctrl-C, Ctrl-V)");
                this.editModeComboBox.setName("Edit Mode");

                enableEditCheckBox = new JCheckBox("Enable shape editing");
                enableEditCheckBox.setActionCommand(ENABLE_EDIT);
                enableEditCheckBox.addActionListener(controller);
                enableEditCheckBox.setSelected(controller.isEnableEdit());
                enableEditCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                enableEditCheckBox.setToolTipText("Allow modifications to shapes");

                aboveGroundCheckBox = new JCheckBox("Keep shape above ground");
                aboveGroundCheckBox.setActionCommand(KEEP_SHAPE_ABOVE_SURFACE);
                aboveGroundCheckBox.addActionListener(controller);
                aboveGroundCheckBox.setSelected(controller.isAboveGround());
                aboveGroundCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                aboveGroundCheckBox.setToolTipText("Restrict shape movement to stay above ground");

                javax.swing.Box newShapeBox = javax.swing.Box.createHorizontalBox();
                newShapeBox.add(newShapeButton);
                newShapeBox.add(javax.swing.Box.createHorizontalStrut(5));
                newShapeBox.add(this.factoryComboBox);
                newShapeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel gridPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap, vgap
                gridPanel.add(newShapeBox);
                gridPanel.add(enableEditCheckBox);
                gridPanel.add(aboveGroundCheckBox);
                gridPanel.add(editModeLabel);
                gridPanel.add(this.editModeComboBox);

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
                JButton deselectButton = new JButton("Deselect");
                deselectButton.setActionCommand(CLEAR_SELECTION);
                deselectButton.addActionListener(controller);
                deselectButton.setToolTipText("Clear the selection");

                JButton deleteButton = new JButton("Delete Selected");
                deleteButton.setActionCommand(REMOVE_SELECTED);
                deleteButton.addActionListener(controller);
                deleteButton.setToolTipText("Delete selected shapes");

                JButton openFileButton = new JButton("Open Image File...");
                openFileButton.setActionCommand(OPEN_IMAGE_FILE);
                openFileButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                openFileButton.addActionListener(controller);

                JLabel textureLabel = new JLabel("Shape texture:");
                this.textureBox = new JTextField(18);
                this.textureBox.setActionCommand(EDIT_TEXTURE);
                this.textureBox.addActionListener(controller);
                this.textureBox.addCaretListener(controller);
                this.textureBox.setToolTipText("Set shape texture");

                JPanel gridPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap, vgap
                gridPanel.add(deselectButton);
                gridPanel.add(deleteButton);
                gridPanel.add(textureLabel);
                gridPanel.add(openFileButton);
                gridPanel.add(textureBox);

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
                        else //noinspection StringEquality
                            if (e.getPropertyName() == KEEP_SHAPE_ABOVE_SURFACE)
                            {
                                aboveGroundCheckBox.setSelected(controller.isAboveGround());
                            }
                }
            });
        }
    }

    //******************************************************************//
    //********************  Rigid Shape Builder Controller  ************//
    //******************************************************************//

    protected static class AbstractShapeBuilderController extends WWObjectImpl implements ActionListener,
        MouseListener, CaretListener
    {
        protected AppFrame app;
        protected AbstractShapeBuilderModel model;
        protected AbstractShapeBuilderPanel view;
        protected AbstractShapeEntry selectedEntry;
        protected boolean enabled = true;
        protected boolean enableEdit = true;
        protected boolean aboveGround = false;
        protected boolean resizeNewShapes;
        protected String editMode;
        // UI components.
        private JFileChooser fileChooser;
        // editors
        protected AbstractShapeEditor editor;

        public AbstractShapeBuilderController(AppFrame app)
        {
            this.app = app;
            this.app.getWwd().getInputHandler().addMouseListener(this);
        }

        public AppFrame getApp()
        {
            return this.app;
        }

        public AbstractShapeBuilderModel getModel()
        {
            return this.model;
        }

        public void setModel(AbstractShapeBuilderModel model)
        {
            this.model = model;
        }

        public AbstractShapeEditor getActiveEditor()
        {
            return this.editor;
        }

        public void setActiveEditor(AbstractShapeEditor editor)
        {
            this.editor = editor;
        }

        public AbstractShapeBuilderPanel getView()
        {
            return this.view;
        }

        public void setView(AbstractShapeBuilderPanel view)
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

        public boolean isAboveGround()
        {
            return this.aboveGround;
        }

        public void setEnableEdit(boolean enable)
        {
            this.enableEdit = enable;
            this.handleEnableEdit(enable);
            this.firePropertyChange(ENABLE_EDIT, null, enable);
        }

        public void setAboveGround(boolean enable)
        {
            this.aboveGround = enable;
            this.handleAboveGround(enable);
            this.firePropertyChange(KEEP_SHAPE_ABOVE_SURFACE, null, enable);
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

        public String getEditMode()
        {
            return this.editMode;
        }

        public void setEditMode(String mode)
        {
            this.editMode = mode;
            view.setSelectedEditMode(mode);
            if (editor != null)
                editor.setEditMode(mode);
            this.getApp().getWwd().redraw();
        }

        public String getImageSource()
        {
            return view.textureBox.getText();
        }

        public void setImageSource(String imageSource)
        {
            AbstractShape shape = null;
            if (this.getSelectedEntry() != null)
                shape = this.getSelectedEntry().getShape();

            if (shape instanceof RigidShape)
            {
                RigidShapeEditor editor = (RigidShapeEditor) this.getSelectedEntry().getEditor();
                RigidShape myShape = (RigidShape) shape;
                int selected = editor.getSelectedFace();

                if (imageSource.endsWith(".jpeg") || imageSource.endsWith(".jpg") || imageSource.endsWith(".bmp")
                    || imageSource.endsWith(".png") || imageSource.endsWith(".gif"))
                {
                    myShape.setImageSource(selected, imageSource);
                    this.getApp().getWwd().redraw();
                }
                else
                {
                    myShape.setImageSource(selected, null);
                    this.getApp().getWwd().redraw();
                }
            }
        }

        @SuppressWarnings( {"StringEquality"})
        public void actionPerformed(ActionEvent e)
        {
            if (!this.isEnabled())
            {
                return;
            }

            String actionCommand = e.getActionCommand();
            if (WWUtil.isEmpty(actionCommand))
                return;

            if (actionCommand == NEW_ABSTRACT_SHAPE)
            {
                AbstractShapeFactory factory = this.getView().getSelectedFactory();
                this.createNewEntry(factory);
            }
            else if (actionCommand == CLEAR_SELECTION)
            {
                this.selectEntry(null, true);
            }
            else if (actionCommand == SIZE_NEW_SHAPES_TO_VIEWPORT)
            {
                if (e.getSource() instanceof AbstractButton)
                {
                    boolean selected = ((AbstractButton) e.getSource()).isSelected();
                    this.setResizeNewShapesToViewport(selected);
                }
            }
            else if (actionCommand == ENABLE_EDIT)
            {
                if (e.getSource() instanceof AbstractButton)
                {
                    boolean selected = ((AbstractButton) e.getSource()).isSelected();
                    this.setEnableEdit(selected);
                }
            }
            else if (actionCommand == KEEP_SHAPE_ABOVE_SURFACE)
            {
                if (e.getSource() instanceof AbstractButton)
                {
                    boolean selected = ((AbstractButton) e.getSource()).isSelected();
                    this.setAboveGround(selected);
                }
            }
            else if (actionCommand == OPEN)
            {
                this.openFromFile();
            }
            else if (actionCommand == REMOVE_SELECTED)
            {
                this.removeEntries(Arrays.asList(this.getSelectedEntries()));
            }
            else if (actionCommand == SAVE)
            {
                this.saveToFile();
            }
            else if (actionCommand == SELECTION_CHANGED)
            {
                this.viewSelectionChanged();
            }
            else if (actionCommand == SET_EDIT_MODE)
            {
                this.setEditMode(this.getView().getSelectedEditMode());
            }
            else if (actionCommand == EDIT_TEXTURE)
            {
                String imageSource = this.getView().textureBox.getText();
                this.setImageSource(imageSource);
                view.textureBox.setText(imageSource);
            }
            if (actionCommand == OPEN_IMAGE_FILE)
            {
                this.doOpenImageFile();
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

        public void caretUpdate(CaretEvent e)
        {
            this.setImageSource(this.getView().textureBox.getText());
        }

        protected void handleSelect()
        {
            // If the picked object is null or something other than a rigid shape, then ignore the mouse click. If we
            // deselect the current entry at this point, the user cannot easily navigate without loosing the selection.

            PickedObjectList pickedObjects = this.getApp().getWwd().getObjectsAtCurrentPosition();

            Object topObject = pickedObjects.getTopObject();
            if (!(topObject instanceof AbstractShape))
                return;

            AbstractShapeEntry pickedEntry = this.getEntryFor((AbstractShape) topObject);
            if (pickedEntry == null)
                return;

            if (this.getSelectedEntry() != pickedEntry)
            {
                this.selectEntry(pickedEntry, true);
            }
            else if (pickedEntry.getShape() instanceof RigidShape)  // picked object is currently selected entry
            {
                RigidShape shape = (RigidShape) this.selectedEntry.getShape();
                RigidShapeEditor editor = (RigidShapeEditor) this.selectedEntry.getEditor();
                int selected = editor.getSelectedFace();
                view.textureBox.setText(shape.getImageSource(selected).toString());
            }
        }

        protected void handleEnableEdit(boolean enable)
        {
            if (this.getSelectedEntry() == null)
                return;

            if (this.isSelectionEditing() != enable)
                this.setSelectionEditing(enable);
        }

        protected void handleAboveGround(boolean enable)
        {
            if (this.getSelectedEntry() == null)
                return;

            if (this.getSelectedEntry().getEditor() != null)
                this.getSelectedEntry().getEditor().setAboveGround(enable);
        }

        public void createNewEntry(AbstractShapeFactory factory)
        {
            AbstractShape shape = factory.createShape(this.getApp().getWwd(), this.isResizeNewShapesToViewport());
            AbstractShapeEditor editor = factory.createEditor(shape);
            editor.setAboveGround(this.isAboveGround());
            AbstractShapeEntry entry = new AbstractShapeEntry(shape, editor);

            this.addEntry(entry);

            this.selectEntry(entry, true);
        }

        public void removeEntries(Iterable<? extends AbstractShapeEntry> entries)
        {
            if (entries != null)
            {
                for (AbstractShapeEntry entry : entries)
                {
                    this.removeEntry(entry);
                }
            }
        }

        public void addEntry(AbstractShapeEntry entry)
        {
            this.getModel().addEntry(entry);

            this.getApp().getShapeLayer().addRenderable(entry.getShape());
            this.getApp().getWwd().redraw();
        }

        public void removeEntry(AbstractShapeEntry entry)
        {
            if (this.getSelectedEntry() == entry)
            {
                this.selectEntry(null, true);
            }

            this.getModel().removeEntry(entry);

            this.getApp().getShapeLayer().removeRenderable(entry.getShape());
            this.getApp().getWwd().redraw();
        }

        public AbstractShapeEntry getSelectedEntry()
        {
            return this.selectedEntry;
        }

        public void selectEntry(AbstractShapeEntry entry, boolean updateView)
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

            if (this.getSelectedEntry() != null)
            {
                this.getSelectedEntry().getEditor().setAboveGround(this.isAboveGround());
            }

            this.getApp().getWwd().redraw();
        }

        protected void setSelectedEntry(AbstractShapeEntry entry)
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

            AbstractShapeEditor activeEditor = this.selectedEntry.getEditor();
            activeEditor.setArmed(editing);
            activeEditor.setEditMode(this.getEditMode());

            if (editing)
            {
                if (this.selectedEntry.getShape() instanceof RigidShape)
                {
                    RigidShape shape = (RigidShape) this.getSelectedEntry().getShape();
                    RigidShapeEditor editor = (RigidShapeEditor) this.getSelectedEntry().getEditor();
                    editor.setSelectedFace(0);     // set an arbitrary piece of the shape as selected initially
                    if (shape.getImageSource(0) == null)
                        view.textureBox.setText("");
                    else
                        view.textureBox.setText(
                            ((RigidShape) this.selectedEntry.getShape()).getImageSource(0).toString());
                }

                activeEditor.setWorldWindow(this.app.getWwd());
                this.setActiveEditor(activeEditor);
                insertBeforePlacenames(this.getApp().getWwd(), activeEditor);
            }
            else
            {
                activeEditor.setWorldWindow(null);
                this.setActiveEditor(null);
                this.getApp().getWwd().getModel().getLayers().remove(activeEditor);
            }

            int index = this.getModel().getIndexForEntry(this.selectedEntry);
            this.getModel().fireTableRowsUpdated(index, index);
        }

        protected void viewSelectionChanged()
        {
            int[] indices = this.getView().getSelectedIndices();
            if (indices != null)
            {
                for (AbstractShapeEntry entry : this.getEntriesFor(indices))
                {
                    this.selectEntry(entry, false);
                }
            }

            this.getApp().getWwd().redraw();
        }

        protected AbstractShapeEntry[] getSelectedEntries()
        {
            int[] indices = this.getView().getSelectedIndices();
            if (indices != null)
            {
                return this.getEntriesFor(indices);
            }

            return new AbstractShapeEntry[0];
        }

        protected AbstractShapeEntry[] getEntriesFor(int[] indices)
        {
            AbstractShapeEntry[] entries = new AbstractShapeEntry[indices.length];
            for (int i = 0; i < indices.length; i++)
            {
                entries[i] = this.getModel().getEntry(indices[i]);
            }
            return entries;
        }

        protected AbstractShapeEntry getEntryFor(AbstractShape shape)
        {
            for (AbstractShapeEntry entry : this.getModel().getEntries())
            {
                if (entry.getShape() == shape)
                {
                    return entry;
                }
            }
            return null;
        }

        protected void saveToFile()
        {
            if (this.fileChooser == null)
            {
                this.fileChooser = new JFileChooser();
                this.fileChooser.setCurrentDirectory(new File(Configuration.getUserHomeDirectory()));
            }

            this.fileChooser.setDialogTitle("Choose Directory to Place Shapes");
            this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            this.fileChooser.setMultiSelectionEnabled(false);
            int status = this.fileChooser.showSaveDialog(null);
            if (status != JFileChooser.APPROVE_OPTION)
                return;

            final File dir = this.fileChooser.getSelectedFile();
            if (dir == null)
                return;

            if (!dir.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            final Iterable<AbstractShapeEntry> entries = this.getModel().getEntries();

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        java.text.DecimalFormat f = new java.text.DecimalFormat("####");
                        f.setMinimumIntegerDigits(4);
                        int counter = 0;

                        for (AbstractShapeEntry entry : entries)
                        {
                            AbstractShape a = entry.getShape();
                            ShapeAttributes currentAttribs = a.getAttributes();
                            a.setAttributes(entry.getAttributes());

                            String xmlString = a.getRestorableState();
                            if (xmlString != null)
                            {
                                try
                                {
                                    PrintWriter of = new PrintWriter(new File(dir,
                                        a.getClass().getName() + "-" + entry.getName() + "-" + f.format(counter++)
                                            + ".xml"));
                                    of.write(xmlString);
                                    of.flush();
                                    of.close();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            a.setAttributes(currentAttribs);
                        }
                    }
                    finally
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                setEnabled(true);
                                getApp().setCursor(null);
                                getApp().getWwd().redraw();
                            }
                        });
                    }
                }
            });
            this.setEnabled(false);
            getApp().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            t.start();
        }

        protected void openFromFile()
        {
            if (this.fileChooser == null)
            {
                this.fileChooser = new JFileChooser();
                this.fileChooser.setCurrentDirectory(new File(Configuration.getUserHomeDirectory()));
            }

            this.fileChooser.setDialogTitle("Choose Shape File Directory");
            this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            this.fileChooser.setMultiSelectionEnabled(false);
            int status = this.fileChooser.showOpenDialog(null);
            if (status != JFileChooser.APPROVE_OPTION)
                return;

            final File dir = this.fileChooser.getSelectedFile();
            if (dir == null)
                return;

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    final ArrayList<AbstractShape> shapes = new ArrayList<AbstractShape>();
                    try
                    {
                        File[] files = dir.listFiles(new FilenameFilter()
                        {
                            public boolean accept(File dir, String name)
                            {
                                return name.startsWith("gov.nasa.worldwind.render") && name.endsWith(".xml");
                            }
                        });

                        for (File file : files)
                        {
                            String[] name = file.getName().split("-");
                            try
                            {
                                Class c = Class.forName(name[0]);
                                AbstractShape newShape = (AbstractShape) c.newInstance();
                                BufferedReader input = new BufferedReader(new FileReader(file));
                                String s = input.readLine();
                                newShape.restoreState(s);
                                shapes.add(newShape);

                                if (name.length >= 2)
                                {
                                    newShape.setValue(AVKey.DISPLAY_NAME, name[1]);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    finally
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                setAbstractShapes(shapes);
                                setEnabled(true);
                                getApp().setCursor(null);
                                getApp().getWwd().redraw();
                            }
                        });
                    }
                }
            });
            this.setEnabled(false);
            getApp().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            t.start();
        }

        protected void doOpenImageFile()
        {
            if (this.fileChooser == null)
            {
                this.fileChooser = new JFileChooser(Configuration.getUserHomeDirectory());
                this.fileChooser.setAcceptAllFileFilterUsed(false);
                this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                this.fileChooser.setMultiSelectionEnabled(true);
                this.fileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Images", ImageIO.getReaderFormatNames()));
            }

            int retVal = this.fileChooser.showOpenDialog(null);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            File[] files = this.fileChooser.getSelectedFiles();
            this.loadFiles(files);
        }

        protected void loadFiles(final File[] files)
        {
            this.app.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    for (File f : files)
                    {
                        loadFile(f);
                    }

                    app.setCursor(null);
                }
            });
            thread.start();
        }

        protected void loadFile(final File file)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    String filename = file.getAbsolutePath();

                    AbstractShape shape = null;
                    if (getSelectedEntry() != null)
                        shape = getSelectedEntry().getShape();

                    if (filename != null && shape != null)
                    {
                        setImageSource(filename);
                        view.textureBox.setText(filename);
                    }
                }
            });
        }

        protected BufferedImage readImage(File file)
        {
            try
            {
                return ImageIO.read(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        private void setAbstractShapes(Iterable<? extends AbstractShape> shapes)
        {
            ArrayList<AbstractShapeEntry> entryList = new ArrayList<AbstractShapeEntry>(this.getModel().getEntries());
            this.removeEntries(entryList);

            for (AbstractShape shape : shapes)
            {
                shape.setAttributes(getDefaultAttributes());
                AbstractShapeEntry entry = new AbstractShapeEntry(shape, getEditorFor(shape));
                this.addEntry(entry);
            }
        }
    }

    //**************************************************************//
    //********************  Main  **********************************//
    //**************************************************************//

    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        // Polygon layer and editor UI components.
        protected RenderableLayer shapeLayer;
        protected AbstractShapeBuilderModel builderModel;
        protected AbstractShapeBuilderPanel builderView;
        protected AbstractShapeBuilderController builderController;

        public AppFrame()
        {
            this.shapeLayer = new RenderableLayer();
            this.shapeLayer.setName(SHAPE_LAYER_NAME);
            insertBeforePlacenames(this.getWwd(), this.shapeLayer);

            this.builderController = new AbstractShapeBuilderController(this);
            this.builderModel = new AbstractShapeBuilderModel();
            this.builderView = new AbstractShapeBuilderPanel(this.builderModel, this.builderController);
            this.getContentPane().add(this.builderView, BorderLayout.SOUTH);

            this.builderController.setModel(this.builderModel);
            this.builderController.setView(this.builderView);
            this.builderController.setResizeNewShapesToViewport(true);
            this.builderController.setEditMode(builderView.getSelectedEditMode());

            makeMenuBar(this, this.builderController);
        }

        public AbstractShapeBuilderPanel getRigidShapeBuilderPanel()
        {
            return this.builderView;
        }

        public RenderableLayer getShapeLayer()
        {
            return this.shapeLayer;
        }

        public static void makeMenuBar(JFrame frame, final AbstractShapeBuilderController controller)
        {
            JMenuBar menuBar = new JMenuBar();
            final JCheckBoxMenuItem resizeNewShapesItem;
            final JCheckBoxMenuItem enableEditItem;
            final JCheckBoxMenuItem aboveGroundItem;

            JMenu menu = new JMenu("File");
            {
                JMenuItem item = new JMenuItem("Open...");
                item.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                item.setActionCommand(OPEN);
                item.addActionListener(controller);
                menu.add(item);

                item = new JMenuItem("Save...");
                item.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                item.setActionCommand(SAVE);
                item.addActionListener(controller);
                menu.add(item);
            }
            menuBar.add(menu);

            menu = new JMenu("Shape");
            {
                JMenu subMenu = new JMenu("New");
                for (final AbstractShapeFactory factory : defaultAbstractShapeFactories)
                {
                    JMenuItem item = new JMenuItem(factory.toString());
                    item.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            controller.createNewEntry(factory);
                        }
                    });
                    subMenu.add(item);
                }
                menu.add(subMenu);

                subMenu = new JMenu("Edit Mode");

                for (final EditMode mode : defaultEditModes)
                {
                    JMenuItem item = new JMenuItem(mode.getMode());
                    item.setAccelerator(KeyStroke.getKeyStroke(mode.getShortcut(),
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                    item.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            controller.setEditMode(mode.getMode());
                        }
                    });
                    subMenu.add(item);
                }
                menu.add(subMenu);

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

                aboveGroundItem = new JCheckBoxMenuItem("Keep shape above ground");
                aboveGroundItem.setActionCommand(KEEP_SHAPE_ABOVE_SURFACE);
                aboveGroundItem.addActionListener(controller);
                aboveGroundItem.setState(controller.isAboveGround());
                menu.add(aboveGroundItem);
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
                        else //noinspection StringEquality
                            if (e.getPropertyName() == KEEP_SHAPE_ABOVE_SURFACE)
                            {
                                aboveGroundItem.setSelected(controller.isAboveGround());
                            }
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("3D Shape Builder", AppFrame.class);
    }
}
