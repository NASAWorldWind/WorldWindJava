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

/**
 * @author dcollins
 * @version $Id: VPFRelation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFRelation
{
    private String table1;
    private String table1Key;
    private String table2;
    private String table2Key;

    public VPFRelation(String table1, String table1Key, String table2, String table2Key)
    {
        this.table1 = table1;
        this.table1Key = table1Key;
        this.table2 = table2;
        this.table2Key = table2Key;
    }

    public String getTable1()
    {
        return this.table1;
    }

    public String getTable1Key()
    {
        return this.table1Key;
    }

    public String getTable2()
    {
        return this.table2;
    }

    public String getTable2Key()
    {
        return this.table2Key;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VPFRelation that = (VPFRelation) o;

        if (this.table1 != null ? !this.table1.equals(that.table1) : that.table1 != null)
            return false;
        if (this.table1Key != null ? !this.table1Key.equals(that.table1Key) : that.table1Key != null)
            return false;
        if (this.table2 != null ? !this.table2.equals(that.table2) : that.table2 != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.table2Key != null ? !this.table2Key.equals(that.table2Key) : that.table2Key != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = this.table1 != null ? this.table1.hashCode() : 0;
        result = 31 * result + (this.table1Key != null ? this.table1Key.hashCode() : 0);
        result = 31 * result + (this.table2 != null ? this.table2.hashCode() : 0);
        result = 31 * result + (this.table2Key != null ? this.table2Key.hashCode() : 0);
        return result;
    }
}
