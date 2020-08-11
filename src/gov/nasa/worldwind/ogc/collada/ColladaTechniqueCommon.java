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

import java.util.*;

/**
 * Represents the COLLADA <i>technique_common</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaTechniqueCommon.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaTechniqueCommon extends ColladaAbstractObject
{
    /** Materials contained by this technique. */
    protected List<ColladaInstanceMaterial> materials = new ArrayList<ColladaInstanceMaterial>();

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaTechniqueCommon(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the materials contained by this technique.
     *
     * @return List of materials. May return an empty list, but never returns null.
     */
    public List<ColladaInstanceMaterial> getMaterials()
    {
        return this.materials;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("instance_material"))
        {
            this.materials.add((ColladaInstanceMaterial) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
