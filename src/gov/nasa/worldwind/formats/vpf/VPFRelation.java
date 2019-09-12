/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
