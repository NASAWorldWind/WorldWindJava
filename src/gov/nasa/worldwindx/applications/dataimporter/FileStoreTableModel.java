/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * Table model for installed data sets.
 *
 * @author tag
 * @version $Id: FileStoreTableModel.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileStoreTableModel extends AbstractTableModel
{
    protected static final String[] columnTitles =
        new String[]{"Key", "Dataset", "Type", "Size (MB)"};

    protected static final Class[] columnTypes =
        new Class[]{Color.class, String.class, String.class, Long.class};

    protected java.util.List<FileStoreDataSet> dataSets = new ArrayList<FileStoreDataSet>();

    public void setDataSets(java.util.List<FileStoreDataSet> sets)
    {
        this.dataSets.clear();
        if (sets != null)
            this.dataSets.addAll(sets);

        this.fireTableDataChanged();
    }

    public java.util.List<FileStoreDataSet> getDataSets()
    {
        return this.dataSets;
    }

    @Override
    public int getRowCount()
    {
        return this.dataSets.size();
    }

    @Override
    public int getColumnCount()
    {
        return columnTitles.length;
    }

    @Override
    public String getColumnName(int i)
    {
        return columnTitles[i];
    }

    @Override
    public Class<?> getColumnClass(int i)
    {
        return columnTypes[i];
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        FileStoreDataSet ds = this.dataSets.get(row);

        switch (col)
        {
            case 0:
            {
                return ds.getColor();
            }
            case 1:
            {
                return ds.getName();
            }
            case 2:
            {
                return ds.getDatasetType();
            }
            case 3:
            {
                Formatter formatter = new Formatter();
                return formatter.format("%5.1f", ((float) ds.getSize()) / 1e6);
            }
        }

        return null;
    }

    public Integer getRowForDataSet(FileStoreDataSet dataSet)
    {
        int index = 0;
        for (FileStoreDataSet ds : this.dataSets)
        {
            if (ds == dataSet)
                return index;

            ++index;
        }

        return null;
    }

    public FileStoreDataSet getRow(int row)
    {
        Iterator<FileStoreDataSet> iter = this.dataSets.iterator();

        for (int i = 0; i < row; i++)
        {
            iter.next();
        }

        return iter.next();
    }

    public void removeDataSet(FileStoreDataSet dataSet)
    {
        this.dataSets.remove(dataSet);

        this.fireTableDataChanged();
    }

    public FileStoreDataSet getDataSetByName(String name)
    {
        for (FileStoreDataSet ds : this.dataSets)
        {
            if (ds.getName().equals(name))
                return ds;
        }

        return null;
    }
}
