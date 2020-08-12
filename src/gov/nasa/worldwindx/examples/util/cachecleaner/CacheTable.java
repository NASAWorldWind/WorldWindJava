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

package gov.nasa.worldwindx.examples.util.cachecleaner;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.util.FileStoreDataSet;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: CacheTable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CacheTable extends JTable
{
    private CacheModel model;

    private static class CacheModel extends AbstractTableModel
    {
        private static final String[] columnTitles =
            new String[] {"Dataset", "Last Used", "Size (MB)", "Day Old", "Week Old", "Month Old", "Year Old"};
        private static final Class[] columnTypes =
            new Class[] {String.class, String.class, Long.class, Long.class, Long.class, Long.class, Long.class};

        private ArrayList<FileStoreDataSet> datasets = new ArrayList<FileStoreDataSet>();
        private String rootName;

        public void setDataSets(String rootName, List<FileStoreDataSet> sets)
        {
            this.datasets.clear();
            this.rootName = rootName;
            this.datasets.addAll(sets);
        }

        public int getRowCount()
        {
            return this.datasets.size() + 1;
        }

        public int getColumnCount()
        {
            return columnTitles.length;
        }

        @Override
        public String getColumnName(int column)
        {
            return columnTitles[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnTypes[columnIndex];
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (rowIndex == this.datasets.size()) // Summary row
            {
                if (columnIndex == 0)
                    return "Total Size";

                if (columnIndex == 1)
                    return "";

                Formatter formatter = new Formatter();
                return formatter.format("%5.1f", ((float) this.computeColumnSum(columnIndex)) / 1e6);
            }

            FileStoreDataSet ds = this.datasets.get(rowIndex);

            switch (columnIndex)
            {
                case 0:
                {
                    return ds.getPath().replace(this.rootName.subSequence(0, this.rootName.length()),
                        "".subSequence(0, 0));
                }
                case 1:
                {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(ds.getLastModified());
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, hh:mm a");
                    return sdf.format(cal.getTime());
                }
                case 2:
                {
                    Formatter formatter = new Formatter();
                    return formatter.format("%5.1f", ((float) ds.getSize()) / 1e6);
                }
                case 3:
                {
                    Formatter formatter = new Formatter();
                    return formatter.format("%5.1f", ((float) ds.getOutOfScopeSize(FileStoreDataSet.DAY, 1)) / 1e6);
                }
                case 4:
                {
                    Formatter formatter = new Formatter();
                    return formatter.format("%5.1f", ((float) ds.getOutOfScopeSize(FileStoreDataSet.WEEK, 1)) / 1e6);
                }
                case 5:
                {
                    Formatter formatter = new Formatter();
                    return formatter.format("%5.1f", ((float) ds.getOutOfScopeSize(FileStoreDataSet.MONTH, 1)) / 1e6);
                }
                case 6:
                {
                    Formatter formatter = new Formatter();
                    return formatter.format("%5.1f", ((float) ds.getOutOfScopeSize(FileStoreDataSet.YEAR, 1)) / 1e6);
                }
            }

            return null;
        }

        private long computeColumnSum(int columnIndex)
        {
            long size = 0;

            for (int row = 0; row < this.datasets.size(); row++)
            {
                String s = this.getValueAt(row, columnIndex).toString();
                Double cs = WWUtil.makeDoubleForLocale(s);
                size += cs != null ? cs * 1e6 : 0;
            }

            return size;
        }
    }

    public CacheTable()
    {
        super(new CacheModel());

        this.model = ((CacheModel) this.getModel());
//        this.setAutoResizeMode(AUTO_RESIZE_OFF);
        this.setShowGrid(true);
        this.setGridColor(Color.BLACK);
        this.setShowHorizontalLines(true);
        this.setShowVerticalLines(true);
        this.setIntercellSpacing(new Dimension(5, 5));
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(true);
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public void setDataSets(String rootDir, List<FileStoreDataSet> sets)
    {
        this.model.setDataSets(rootDir, sets);
        this.setPreferredColumnWidths();
    }

    public void deleteDataSet(FileStoreDataSet dataset)
    {
        this.model.datasets.remove(dataset);
        this.resizeAndRepaint();
    }

    public List<FileStoreDataSet> getSelectedDataSets()
    {
        int[] rows = this.getSelectedRows();

        if (rows.length == 0)
            return Collections.emptyList();

        ArrayList<FileStoreDataSet> selected = new ArrayList<FileStoreDataSet>();
        for (int i : rows)
        {
            if (i < this.model.datasets.size())
                selected.add(this.model.datasets.get(i));
        }

        return selected;
    }

    private void setPreferredColumnWidths()
    {
        for (int col = 0; col < getColumnModel().getColumnCount(); col++)
        {
            // Start with size of column header
            JLabel label = new JLabel(this.getColumnName(col));
            int size = label.getPreferredSize().width;

            // Find any cells in column that have a wider value
            TableColumn column = getColumnModel().getColumn(col);
            for (int row = 0; row < this.model.getRowCount(); row++)
            {
                label = new JLabel(this.getValueAt(row, col).toString());
                if (label.getPreferredSize().width > size)
                    size = label.getPreferredSize().width;
            }

            column.setPreferredWidth(size);
        }
    }
}
