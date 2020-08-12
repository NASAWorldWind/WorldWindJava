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
 * Represents the COLLADA <i>technique</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaTechnique.java 675 2012-07-02 18:47:47Z pabercrombie $
 */
public class ColladaTechnique extends ColladaAbstractParamContainer
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaTechnique(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the shader contained by the technique. Supported shaders are <i>lambert</i> and <i>phong</i>.
     *
     * @return The shader for this technique, or null if the shader is not set, or is not supported.
     */
    public ColladaAbstractShader getShader()
    {
        Object o = this.getField("lambert");
        if (o != null)
            return (ColladaAbstractShader) o;

        o = this.getField("phong");
        if (o != null)
            return (ColladaAbstractShader) o;

        // TODO handle other shaders
        return null;
    }

    /**
     * Indicates the value of the <i>profile</i> field.
     *
     * @return The value of the profile field, or null if the field is not set.
     */
    public String getProfile()
    {
        return (String) this.getField("profile");
    }
}
