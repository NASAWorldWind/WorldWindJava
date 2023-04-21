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

import gov.nasa.worldwind.avlist.*;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: GeoSymTable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymTable
{
    private GeoSymTableHeader header;
    private AVList[] records;
    private Map<Integer, Integer> indexOnId;

    public GeoSymTable(GeoSymTableHeader header)
    {
        this.header = header;
        this.indexOnId = new HashMap<Integer, Integer>();
    }

    public GeoSymTableHeader getHeader()
    {
        return header;
    }

    public AVList[] getRecords()
    {
        return this.records;
    }

    public void setRecords(AVList[] records)
    {
        this.records = records;
        this.buildRecordIndices();
    }

    public AVList getRecord(int id)
    {
        Integer index = this.indexOnId.get(id);
        return (index != null && index >= 0 && index < this.records.length) ? this.records[index] : null;
    }

    public static void selectMatchingRows(String columnName, Object value, boolean acceptNullValue,
        List<AVList> outRows)
    {
        Iterator<AVList> iter = outRows.iterator();
        if (!iter.hasNext())
            return;

        AVList record;
        while (iter.hasNext())
        {
            record = iter.next();
            if (record == null)
                continue;

            Object o = record.getValue(columnName);
            if ((o == null && !acceptNullValue) || (o != null && !o.equals(value)))
            {
                iter.remove();
            }
        }
    }

    public static void selectMatchingStringRows(String columnName, String value, boolean acceptNullValue,
        List<AVList> outRows)
    {
        Iterator<AVList> iter = outRows.iterator();
        if (!iter.hasNext())
            return;

        AVList record;
        while (iter.hasNext())
        {
            record = iter.next();
            if (record == null)
                continue;

            Object o = record.getValue(columnName);
            if (o == null || o instanceof String)
            {
                String s = (String) o;
                if (s == null || s.length() == 0)
                {
                    if (!acceptNullValue)
                        iter.remove();
                }
                else
                {
                    if (!s.equalsIgnoreCase(value))
                        iter.remove();
                }
            }
        }
    }

    protected void buildRecordIndices()
    {
        // Build index on record ids.
        this.indexOnId.clear();
        for (int i = 0; i < this.records.length; i++)
        {
            Integer id = AVListImpl.getIntegerValue(this.records[i], "id");
            if (id != null)
            {
                this.indexOnId.put(id, i);
            }
        }
    }
}
