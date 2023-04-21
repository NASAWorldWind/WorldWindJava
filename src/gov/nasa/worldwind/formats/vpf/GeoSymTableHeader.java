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