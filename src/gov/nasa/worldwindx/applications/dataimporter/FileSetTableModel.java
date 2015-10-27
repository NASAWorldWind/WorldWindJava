/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.AVKey;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.beans.*;
import java.util.*;

/**
 * The table model for the file-set table.
 *
 * @author tag
 * @version $Id: FileSetTableModel.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSetTableModel extends AbstractTableModel implements PropertyChangeListener
{
    protected static final String[] columnTitles =
        new String[]{"Key", "Preview", "Dataset Name", "Scale", "Type", "Files"};

    protected Set<FileSet> fileSets = new TreeSet<FileSet>(new Comparator<FileSet>()
    {
        @Override
        public int compare(FileSet fileSet, FileSet fileSet1)
        {
            return fileSet.getName().compareTo(fileSet1.getName());
        }
    });

    public FileSetTableModel(FileSetMap fileSetMap)
    {
        this.setFileSetMap(fileSetMap);
    }

    public void setFileSetMap(FileSetMap fileSetMap)
    {
        this.clearFileSets();

        if (fileSetMap != null)
        {
            for (Map.Entry<Object, FileSet> entry : fileSetMap.entrySet())
            {
                this.fileSets.add(entry.getValue());
                entry.getValue().addPropertyChangeListener(this);
            }
        }

        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount()
    {
        return this.fileSets.size();
    }

    @Override
    public int getColumnCount()
    {
        return columnTitles.length;
    }

    @Override
    public String getColumnName(int col)
    {
        return columnTitles[col];
    }

    @Override
    public Class getColumnClass(int col)
    {
        switch (col)
        {
            case 0:
                return Color.class;

            case 1:
                return ImageIcon.class;

            case 5:
                return Number.class;

            default:
                return String.class;
        }
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        FileSet fs = getRow(row);

        switch (col)
        {
            case 0:
                return fs.getColor();

            case 1:
                return this.getImageIcon(row);

            case 2:
                return fs.getName() != null ? fs.getName() : "";

            case 3:
                return fs.getScale() != null ? fs.getScale() : "";

            case 4:
                return fs.getDataType() != null ? fs.getDataType() : "";

            case 5:
                return fs.getLength();

            default:
                return "unknown";
        }
    }

    public Integer getRowForFileSet(FileSet fileSet)
    {
        int index = 0;
        for (FileSet fs : this.fileSets)
        {
            if (fs == fileSet)
                return index;

            ++index;
        }

        return null;
    }

    public FileSet getRow(int row)
    {
        Iterator<FileSet> iter = this.fileSets.iterator();

        for (int i = 0; i < row; i++)
        {
            iter.next();
        }

        return iter.next();
    }

    protected ImageIcon getImageIcon(final int row)
    {
        FileSet fileSet = this.getRow(row);

        if (fileSet.getImageIcon() != null)
            return fileSet.getImageIcon();

        // Register to be notified when the file set's preview image is ready.
        fileSet.addPropertyChangeListener(AVKey.IMAGE, this);

        return null;
//        return new ImageIcon(FileSetTableModel.class.getResource("/images/indicator-16.gif"));
//        return new ImageIcon(FileSetTableModel.class.getResource("/images/32x32-icon-earth.png"));
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        if (propertyChangeEvent.getPropertyName().equals(AVKey.IMAGE))
        {
            FileSet fileSet = (FileSet) propertyChangeEvent.getSource();
            fireTableCellUpdated(this.getRowForFileSet(fileSet), 1);

            // Preview image monitoring is no longer needed.
            fileSet.removePropertyChangeListener(AVKey.IMAGE, this);
        }
    }

    protected void clearFileSets()
    {
        if (this.fileSets != null)
        {
            for (FileSet fileSet : this.fileSets)
            {
                fileSet.clear();
            }
        }

        this.fileSets.clear();
    }
}
