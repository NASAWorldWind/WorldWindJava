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

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * Displays the installed data sets.
 *
 * @author tag
 * @version $Id: FileStoreTable.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileStoreTable extends JTable
{
    public static final String VISIBLE = "gov.nasa.worldwindx.dataimport.FileStoreTable.Visible";

    public FileStoreTable()
    {
        super(new FileStoreTableModel());

        this.setIntercellSpacing(new Dimension(10, 1));

        this.setDefaultRenderer(Color.class, new TableCellColorRenderer(true));

        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(true);
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        TableColumn column = this.getColumnModel().getColumn(0); // color column
        column.setPreferredWidth(10);

        column = this.getColumnModel().getColumn(3); // size column
        column.setPreferredWidth(20);

        // The table cell renderers manage the enablement of the dataset rows based on view inclusion
        NameRenderer cellRenderer = new NameRenderer();
        column = this.getColumnModel().getColumn(1);
        column.setCellRenderer(cellRenderer);
        column = this.getColumnModel().getColumn(2);
        column.setCellRenderer(cellRenderer);
        column = this.getColumnModel().getColumn(3);
        column.setCellRenderer(cellRenderer);
    }

    public void setDataSets(java.util.List<FileStoreDataSet> sets)
    {
        ((FileStoreTableModel) this.getModel()).setDataSets(sets);
        this.setPreferredColumnWidths();
    }

    public java.util.List<FileStoreDataSet> getSelectedDataSets()
    {
        int[] rows = this.getSelectedRows();

        if (rows.length == 0)
            return Collections.emptyList();

        ArrayList<FileStoreDataSet> selected = new ArrayList<FileStoreDataSet>();
        for (int i : rows)
        {
            int modelRow = this.convertRowIndexToModel(i);
            if (modelRow < ((FileStoreTableModel) this.getModel()).dataSets.size())
            {
                // Don't add disabled (non-visible) datasets to the selection list.
                if ((((FileStoreTableModel) this.getModel()).dataSets.get(modelRow).getValue(VISIBLE) == null))
                    continue;

                selected.add(((FileStoreTableModel) this.getModel()).dataSets.get(modelRow));
            }
        }

        return selected;
    }

    protected void setPreferredColumnWidths()
    {
        for (int col = 1; col < getColumnModel().getColumnCount(); col++)
        {
            // Start with size of column header
            JLabel label = new JLabel(this.getColumnName(col));
            int size = label.getPreferredSize().width;

            // Find any cells in column that have a wider value
            TableColumn column = getColumnModel().getColumn(col);
            for (int row = 0; row < this.getModel().getRowCount(); row++)
            {
                label = new JLabel(this.getValueAt(row, col).toString());
                if (label.getPreferredSize().width > size)
                    size = label.getPreferredSize().width;
            }

            column.setPreferredWidth(size);
        }
    }

    public void scrollToDataSet(FileStoreDataSet dataSet)
    {
        Integer row = ((FileStoreTableModel) this.getModel()).getRowForDataSet(dataSet);

        if (row != null)
            this.scrollToVisible(row, 0);
    }

    public void scrollToVisible(int rowIndex, int vColIndex)
    {
        if (!(this.getParent() instanceof JViewport))
            return;

        JViewport viewport = (JViewport) this.getParent();

        // This rectangle is relative to the table where the
        // northwest corner of cell (0,0) is always (0,0).
        Rectangle rect = this.getCellRect(rowIndex, vColIndex, true);

        // The location of the viewport relative to the table
        Point pt = viewport.getViewPosition();

        // Translate the cell location so that it is relative
        // to the view, assuming the northwest corner of the
        // view is (0,0)
        rect.setLocation(rect.x - pt.x, rect.y - pt.y);

        // Scroll the area into view
        viewport.scrollRectToVisible(rect);
    }

    class NameRenderer extends DefaultTableCellRenderer
    {
        protected boolean enabled;

        public void setValue(Object value)
        {
            super.setValue(value);

            FileStoreDataSet dataSet =
                ((FileStoreTableModel) FileStoreTable.this.getModel()).getDataSetByName(value.toString());

            // Determine whether the table row is enabled or not. This logic relies on the name cell being the first
            // cell for which this renderer is called. It uses the name to find the dataset and set the dataset's
            // visibility flag. The subsequent cells -- data type and data size -- inherit the enabled setting
            // determined during rendering of the name cell.
            if (dataSet != null)
                this.enabled = dataSet.getValue(VISIBLE) != null;

            this.setEnabled(this.enabled);

            // Make the dataset sizes right aligned in their cells.
            this.setHorizontalAlignment(value instanceof Formatter ? JLabel.RIGHT : JLabel.LEFT);
        }
    }
}
