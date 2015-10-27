/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * MIL-DTL-89045, section 3.5.3.1
 *
 * @author dcollins
 * @version $Id: GeoSymTableHeader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymTableHeader
{
    protected String fileName;
    protected String description;
    // Use LinkedHashMap to acheive predictable ordering of table columns.
    protected LinkedHashMap<String, GeoSymColumn> columnMap;

    public GeoSymTableHeader()
    {
        this.columnMap = new LinkedHashMap<String, GeoSymColumn>();
    }

    public String getFileName()
    {
        return this.fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getNumColumns()
    {
        return this.columnMap.size();
    }

    public boolean containsColumn(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.columnMap.containsKey(name);
    }

    public GeoSymColumn getColumn(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.columnMap.get(name);
    }

    public Set<String> getColumnNames()
    {
        return Collections.unmodifiableSet(this.columnMap.keySet());
    }

    public Collection<GeoSymColumn> getColumns()
    {
        return Collections.unmodifiableCollection(this.columnMap.values());
    }

    public void setColumns(Collection<? extends GeoSymColumn> collection)
    {
        this.removeAllColumns();

        if (collection != null)
            this.addAllColumns(collection);
    }

    public void addColumn(GeoSymColumn column)
    {
        if (column == null)
        {
            String message = Logging.getMessage("nullValue.ColumnIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.columnMap.put(column.getName(), column);
    }

    public void addAllColumns(Collection<? extends GeoSymColumn> collection)
    {
        if (collection == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNulln");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (GeoSymColumn col : collection)
        {
            this.addColumn(col);
        }
    }

    public void removeColumn(GeoSymColumn column)
    {
        if (column == null)
        {
            String message = Logging.getMessage("nullValue.ColumnIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.columnMap.remove(column.getName());
    }

    public void removeAllColumns()
    {
        this.columnMap.clear();
    }
}