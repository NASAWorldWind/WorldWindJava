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

package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFArray,./GLTFAttributes,./GLTFParserContext,./GLTFUtil,../../avlist/AVListImpl")
public class GLTFPrimitive extends GLTFArray {

    private GLTFAttributes attributes;
    private int indicesAccessorIdx;
    private int modeIdx;
    private int materialIdx;

    public GLTFPrimitive(AVListImpl properties) {
        super();
        this.indicesAccessorIdx = -1;
        this.materialIdx=-1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_ATTRIBUTES:
                    this.attributes = new GLTFAttributes(GLTFUtil.asAVList(properties.getValue(propName)));
                    break;
                case GLTFParserContext.KEY_INDICES:
                    this.indicesAccessorIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MODE:
                    this.modeIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MATERIAL:
                    this.materialIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFPrimitive: Unsupported " + propName);
                    break;

            }
        }
    }

    public int getVertexAccessorIdx() {
        if (this.attributes != null) {
            return this.attributes.getVertexAccessorIdx();
        }
        return -1;
    }

    public int getNormalAccessorIdx() {
        if (this.attributes != null) {
            return this.attributes.getNormalAccessorIdx();
        }
        return -1;
    }

    public int getVertexIndicesAccessorIdx() {
        return this.indicesAccessorIdx;
    }

    public int getMaterialIdx() {
        return this.materialIdx;
    }

}
