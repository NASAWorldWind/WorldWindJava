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
