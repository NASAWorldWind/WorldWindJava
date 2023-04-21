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

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>profile_COMMON</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaProfileCommon.java 675 2012-07-02 18:47:47Z pabercrombie $
 */
public class ColladaProfileCommon extends ColladaAbstractParamContainer
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaProfileCommon(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the <i>technique</i> field of this profile.
     *
     * @return The value of the <i>technique</i> field, or null if the field is not set.
     */
    public ColladaTechnique getTechnique()
    {
        return (ColladaTechnique) this.getField("technique");
    }

    /**
     * Indicates the <i>extra</i> field of this profile.
     *
     * @return The value of the <i>technique</i> field, or null if the field is not set.
     */
    public ColladaExtra getExtra()
    {
        return (ColladaExtra) this.getField("extra");
    }

    /** {@inheritDoc} */
    @Override
    public ColladaNewParam getParam(String sid)
    {
        ColladaNewParam param = super.getParam(sid);
        if (param != null)
            return param;

        ColladaTechnique technique = this.getTechnique();
        if (technique == null)
            return null;

        return technique.getParam(sid);
    }
}
