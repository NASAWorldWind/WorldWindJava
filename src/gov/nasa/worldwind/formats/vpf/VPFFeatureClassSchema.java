/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: VPFFeatureClassSchema.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFFeatureClassSchema
{
    protected String className;
    protected VPFFeatureType type;
    protected String featureTableName;

    public VPFFeatureClassSchema(String className, VPFFeatureType type, String featureTableName)
    {
        this.className = className;
        this.type = type;
        this.featureTableName = featureTableName;
    }

    public String getClassName()
    {
        return this.className;
    }

    public VPFFeatureType getType()
    {
        return this.type;
    }

    public String getFeatureTableName()
    {
        return this.featureTableName;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        VPFFeatureClassSchema that = (VPFFeatureClassSchema) o;

        if (this.className != null ? !this.className.equals(that.className) : that.className != null)
            return false;
        if (this.featureTableName != null ? !this.featureTableName.equals(that.featureTableName)
            : that.featureTableName != null)
            return false;
        //noinspection RedundantIfStatement
        if (this.type != null ? !this.type.equals(that.type) : that.type != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = this.className != null ? this.className.hashCode() : 0;
        result = 31 * result + (this.type != null ? this.type.hashCode() : 0);
        result = 31 * result + (this.featureTableName != null ? this.featureTableName.hashCode() : 0);
        return result;
    }
}
