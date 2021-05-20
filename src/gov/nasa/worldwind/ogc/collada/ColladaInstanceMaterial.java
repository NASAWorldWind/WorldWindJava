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
 * Represents the COLLADA <i>instance_material</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaInstanceMaterial.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaInstanceMaterial extends ColladaAbstractInstance<ColladaMaterial>
{
    protected List<ColladaBindVertexInput> bindVertexInputs = new ArrayList<ColladaBindVertexInput>();

    public ColladaInstanceMaterial(String ns)
    {
        super(ns);
    }

    public String getTarget()
    {
        return (String) this.getField("target");
    }

    public String getSymbol()
    {
        return (String) this.getField("symbol");
    }

    /** Instance_material uses a "target" attribute instead of the "url" attribute used by other instance elements. */
    @Override
    public String getUrl()
    {
        return this.getTarget();
    }

    /**
     * Indicates the <i>bind_vertex_input</i> element.
     *
     * @return The bind_vertex_input elements, if present. Otherwise null.
     */
    public List<ColladaBindVertexInput> getBindVertexInputs()
    {
        return this.bindVertexInputs;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if ("bind_vertex_input".equals(keyName))
        {
            this.bindVertexInputs.add((ColladaBindVertexInput) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
