/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.geom.Angle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.beans.*;
import java.text.NumberFormat;

/**
 * @author tag
 * @version $Id: PositionTable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PositionTable extends JTable
{
    private static final int ITEM_NUM_COLUMN = 0;
    private static final int LATITUDE_COLUMN = 1;
    private static final int LONGITUDE_COLUMN = 2;
    private static final int ALTITUDE_COLUMN = 3;

    private SARTrack sarTrack;
    private String elevationUnit;
    private String angleFormat;

    private final PropertyChangeListener propertyListener = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent)
        {
            Object newValue = propertyChangeEvent.getNewValue();
            if (newValue != null && newValue instanceof Integer)
            {
                updateTableRow((Integer) newValue);
            }
            else
            {
                updateTableData();
            }
        }
    };

    public PositionTable()
    {
        this.setToolTipText("Track Positions");
        this.setModel(new MyTableModel(this));

        // Force the JTable to commit a cell edit if that cell looses focus.
        this.putClientProperty("terminateEditOnFocusLost", true);

        TableCellRenderer tcr = this.getTableHeader().getDefaultRenderer();

        // Setup latitude and longitude columns
        this.getTableHeader().getColumnModel().getColumn(LATITUDE_COLUMN).setHeaderRenderer(
            new AngleHeaderRenderer(tcr, this));
        this.getTableHeader().getColumnModel().getColumn(LONGITUDE_COLUMN).setHeaderRenderer(
            new AngleHeaderRenderer(tcr, this));
        this.getColumnModel().getColumn(LATITUDE_COLUMN).setCellRenderer(new AngleCellRenderer(this));
        this.getColumnModel().getColumn(LONGITUDE_COLUMN).setCellRenderer(new AngleCellRenderer(this));
        this.getColumnModel().getColumn(LATITUDE_COLUMN).setCellEditor(new AngleCellEditor(this, -90, 90));
        this.getColumnModel().getColumn(LONGITUDE_COLUMN).setCellEditor(new AngleCellEditor(this, -180, 180));

        // Setup altitude column
        this.getTableHeader().getColumnModel().getColumn(ALTITUDE_COLUMN).setHeaderRenderer(
            new AltitudeHeaderRenderer(tcr, this));
        this.getColumnModel().getColumn(ALTITUDE_COLUMN).setCellRenderer(new AltitudeCellRenderer(this));
        this.getColumnModel().getColumn(ALTITUDE_COLUMN).setCellEditor(new AltitudeCellEditor(this));

        {
            TableColumnModel cm = this.getColumnModel();
            cm.getColumn(0).setResizable(false);
            cm.getColumn(0).setMinWidth(35);
            cm.getColumn(0).setPreferredWidth(35);

            cm.getColumn(1).setResizable(false);
            cm.getColumn(1).setMinWidth(70);
            cm.getColumn(1).setPreferredWidth(80);

            cm.getColumn(2).setResizable(false);
            cm.getColumn(2).setMinWidth(70);
            cm.getColumn(2).setPreferredWidth(80);

            cm.getColumn(3).setResizable(false);
            cm.getColumn(3).setMinWidth(70);
            cm.getColumn(3).setPreferredWidth(70);
        }
    }

    public SARTrack getSarTrack()
    {
        return sarTrack;
    }

    public void setSarTrack(SARTrack sarTrack)
    {
        if (this.sarTrack == sarTrack)
            return;

        if (this.sarTrack != null)
            this.sarTrack.removePropertyChangeListener(this.propertyListener);

        this.sarTrack = sarTrack;

        if (this.sarTrack != null)
            this.sarTrack.addPropertyChangeListener(this.propertyListener);

        this.setTableColors(this.sarTrack);
        this.updateTableData();
    }

    public String getElevationUnit()
    {
        return this.elevationUnit;
    }

    public void setElevationUnit(String unit)
    {
        this.elevationUnit = unit;
    }

    public String getAngleFormat()
    {
        return this.angleFormat;
    }

    public void setAngleFormat(String format)
    {
        this.angleFormat = format;
    }

    public void updateTableData()
    {
        ((AbstractTableModel) this.getModel()).fireTableDataChanged();
    }

    public void updateTableRow(int row)
    {
        ((AbstractTableModel) this.getModel()).fireTableRowsUpdated(row, row);
    }

    protected Color getTableColorForTrack(SARTrack track)
    {
        if (track == null)
            return null;

        Color color = track.getColor();

        float[] hsbComponents = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
        float hue = hsbComponents[0];
        float saturation = hsbComponents[1];
        float brightness = hsbComponents[2];

        saturation *= 0.2f;
        
        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);

        return new Color(rgbInt);
    }

    protected void setTableColors(SARTrack track)
    {
        Color tableBackground = this.getTableColorForTrack(track);
        Color selectionBackground = (tableBackground != null) ? Color.DARK_GRAY : null;
        Color selectionForeground = (tableBackground != null) ? Color.WHITE : null;

        this.setBackground(tableBackground);
        this.setSelectionForeground(selectionForeground);
        this.setSelectionBackground(selectionBackground);
        this.setOpaque(true);

        Container c = this.getParent();
        if (c != null)
        {
            c.setBackground(tableBackground);
            if (c instanceof JComponent)
                ((JComponent) c).setOpaque(true);
        }
    }

    private class MyTableModel extends AbstractTableModel
    {
        String[] columnNames = new String[] {
            "#", "Latitude", "Longitude", "Altitude"
        };

        Class[] columnTypes = new Class[] {
            Integer.class, String.class, String.class, Double.class
        };

        boolean[] columnEditable = new boolean[] {
            false, true, true, true
        };

        private PositionTable table;

        public MyTableModel(PositionTable table)
        {
            this.table = table;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return this.columnTypes[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return this.columnEditable[columnIndex];
        }

        public int getRowCount()
        {
            return sarTrack != null ? sarTrack.size() : 0;
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return this.columnNames[columnIndex];
        }

        public int getColumnCount()
        {
            return 4;
        }

        public Object getValueAt(int row, int col)
        {
            if (sarTrack == null)
                return null;

            switch (col)
            {
                case ITEM_NUM_COLUMN:
                    return row;
                case LATITUDE_COLUMN:
                    return sarTrack.get(row).getLatitude().degrees;
                case LONGITUDE_COLUMN:
                    return sarTrack.get(row).getLongitude().degrees;
                case ALTITUDE_COLUMN:
                    return sarTrack.get(row).getElevation();
            }

            return null;
        }

        @Override
        public void setValueAt(Object object, int row, int col)
        {
            if (sarTrack == null)
                return;

            SARPosition curPos = sarTrack.get(row);
            SARPosition newPos;
            Angle newAngle;

            switch (col)
            {
                case LATITUDE_COLUMN:
                    if (!(object instanceof String))
                        return;
                    if ((newAngle = table.toAngle((String) object)) == null)
                        return;
                    newPos = new SARPosition(newAngle, curPos.getLongitude(), curPos.getElevation());
                    break;
                case LONGITUDE_COLUMN:
                    if (!(object instanceof String))
                        return;
                    if ((newAngle = table.toAngle((String) object)) == null)
                        return;
                    newPos = new SARPosition(curPos.getLatitude(), newAngle, curPos.getElevation());
                    break;
                case ALTITUDE_COLUMN:
                    // The value stored in a SARPosition's elevation will always be in meters.
                    // So when the altitude is displayed in feet, we will convert the incoming
                    // value back to meters. This allows the user entring a value to operate in
                    // whatever units are being displayed without thinking about conversion.
                    if (!(object instanceof Double))
                        return;
                    double newVal = (Double) object;
                    if (SAR2.UNIT_IMPERIAL.equals(elevationUnit))
                        newVal = SAR2.feetToMeters(newVal);
                    newPos = new SARPosition(curPos.getLatitude(), curPos.getLongitude(), newVal);
                    break;
                default:
                    return;
            }

            sarTrack.set(row, newPos);
        }
    }

    private Angle toAngle(String string)
    {
        if (Angle.ANGLE_FORMAT_DMS.equals(this.angleFormat))
        {
            try
            {
                return Angle.fromDMS(string);
            }
            catch (Exception ignore)
            {
                return null;
            }
        }
        else
        {
            try
            {
                Number number = NumberFormat.getInstance().parse(string.trim());
                return Angle.fromDegrees(number.doubleValue());
            }
            catch (Exception ignore)
            {
                return null;
            }
        }
    }

    private String makeAngleDescription(double degrees)
    {
        return SAR2.formatAngle(this.angleFormat, Angle.fromDegrees(degrees));
    }

    private String makeElevationDescription(double metersElevation)
    {
        String s;
        if (SAR2.UNIT_IMPERIAL.equals(this.elevationUnit))
            s = NumberFormat.getInstance().format(SAR2.metersToFeet(metersElevation));
        else // Default to metric units.
            s = NumberFormat.getInstance().format(metersElevation);
        return s;
    }

    private static class AltitudeHeaderRenderer implements TableCellRenderer
    {
        private TableCellRenderer delegate;
        private PositionTable table;

        public AltitudeHeaderRenderer(TableCellRenderer delegate, PositionTable table)
        {
            this.delegate = delegate;
            this.table = table;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
        {
            if (this.delegate == null)
                return null;

            Component c = this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c == null || !(c instanceof JLabel))
                return c;

            JLabel label = (JLabel) c;
            if (label.getText() == null)
                return c;

            if (SAR2.UNIT_IMPERIAL.equals(this.table.elevationUnit))
                label.setText(label.getText() + " (ft)");
            else // Default to metric units.
                label.setText(label.getText() + " (m)");
            return label;
        }
    }

    private static class AngleHeaderRenderer implements TableCellRenderer
    {
        private TableCellRenderer delegate;
        private PositionTable table;

        public AngleHeaderRenderer(TableCellRenderer delegate, PositionTable table)
        {
            this.delegate = delegate;
            this.table = table;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column)
        {
            if (this.delegate == null)
                return null;

            Component c = this.delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c == null || !(c instanceof JLabel))
                return c;

            JLabel label = (JLabel) c;
            if (label.getText() == null)
                return c;

            if (Angle.ANGLE_FORMAT_DMS.equals(this.table.angleFormat))
                label.setText(label.getText() + " (dms)");
            else // Default to decimal degrees.
                label.setText(label.getText() + " (dd)");
            return label;
        }
    }

    private static class AngleCellRenderer extends DefaultTableCellRenderer
    {
        private PositionTable table;

        private AngleCellRenderer(PositionTable table)
        {
            this.table = table;
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        public void setValue(Object value)
        {
            setText(value != null ? this.table.makeAngleDescription((Double) value) : "");
        }
    }

    private static class AltitudeCellRenderer extends DefaultTableCellRenderer
    {
        private PositionTable table;

        private AltitudeCellRenderer(PositionTable table)
        {
            this.table = table;
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        protected void setValue(Object value)
        {
            setText(this.table.makeElevationDescription((Double) value));
        }
    }

    private static class GeneralCellEditor extends DefaultCellEditor
    {
        private PositionTable table;
        private Object value;

        public GeneralCellEditor(JTextField textField, PositionTable table)
        {
            super(textField);
            this.table = table;
        }

        public PositionTable getTable()
        {
            return table;
        }

        public Object getCellEditorValue()
        {
            return this.value;
        }

        public boolean stopCellEditing()
        {
            String s = (String) super.getCellEditorValue();
            try
            {
                this.value = this.validateEditorText(s);
            }
            catch (Exception e)
            {
                ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column)
        {
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
            this.value = null;
            try
            {
                this.value = this.createEditorText(value);
            }
            catch (Exception e)
            {
                return null;
            }
            return super.getTableCellEditorComponent(table, this.value, isSelected, row, column);
        }

        protected Object validateEditorText(String text) throws Exception
        {
            return text;
        }

        protected String createEditorText(Object value) throws Exception
        {
            return value.toString();
        }
    }

    private class AngleCellEditor extends GeneralCellEditor
    {
        double min, max;

        public AngleCellEditor(PositionTable table, double min, double max)
        {
            super(new JTextField(), table);
            this.min = min;
            this.max = max;
            ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }

        protected Object validateEditorText(String text) throws Exception
        {
            Angle angle = this.getTable().toAngle(text);
            if (angle == null)
                throw new IllegalArgumentException(text);
            if (angle.degrees < min || angle.degrees > max)
                throw new IllegalArgumentException(text);
            return text;
        }

        protected String createEditorText(Object value) throws Exception
        {
            String text = this.getTable().makeAngleDescription((Double) value);
            text = text.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", " ").replaceAll("\\s+", " ");
            return text;
        }
    }

    private static class AltitudeCellEditor extends GeneralCellEditor
    {
        public AltitudeCellEditor(PositionTable table)
        {
            super(new JTextField(), table);
            ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }

        protected Object validateEditorText(String text) throws Exception
        {
            Number number = NumberFormat.getInstance().parse(text);
            return number.doubleValue();
        }

        protected String createEditorText(Object value) throws Exception
        {
            return this.getTable().makeElevationDescription((Double) value);
        }
    }
}
