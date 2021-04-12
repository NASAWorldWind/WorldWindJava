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
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFUtil,./GLTFAccessor,./GLTFRoot,./GLTFArray,./GLTFPrimitive,../../geom/Vec4,./GLTFMaterial,./GLTFParserContext,../../avlist/AVListImpl")
public class GLTFMesh extends GLTFArray {

    private GLTFPrimitive[] primitives;
    private Vec4[] vertexBuffer;
    private Vec4[] normalBuffer;
    private int[] bufferIndices;
    private String name;
    private GLTFMaterial material;

    public GLTFMesh(AVListImpl properties) {
        super();
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_PRIMITIVES:
                    Object[] sourceArray = (Object[]) properties.getValue(propName);
                    this.primitives = new GLTFPrimitive[sourceArray.length];
                    for (int i = 0; i < sourceArray.length; i++) {
                        this.primitives[i] = new GLTFPrimitive(GLTFUtil.asAVList(sourceArray[i]));
                    }
                    break;
                case GLTFParserContext.KEY_NAME:
                    this.name = properties.getStringValue(propName);
                    break;
                default:
                    System.out.println("GLTFMesh: Unsupported " + propName);
                    break;
            }
        }
    }

    public void assembleGeometry(GLTFRoot root) {
        for (GLTFPrimitive primitive : this.primitives) {
            int materialIdx = primitive.getMaterialIdx();
            if (materialIdx >= 0) {
                this.material = root.getMaterialForIdx(materialIdx);
            }

            int vertexAccessorIdx = primitive.getVertexAccessorIdx();
            GLTFAccessor accessor = root.getAccessorForIdx(vertexAccessorIdx);
            this.vertexBuffer = accessor.getCoordBuffer(root);

            int normalAccessorIdx = primitive.getNormalAccessorIdx();
            if (normalAccessorIdx >= 0) {
                accessor = root.getAccessorForIdx(normalAccessorIdx);
                this.normalBuffer = accessor.getCoordBuffer(root);
            }

            int vertexIndicesAccessorIdx = primitive.getVertexIndicesAccessorIdx();
            accessor = root.getAccessorForIdx(vertexIndicesAccessorIdx);
            this.bufferIndices = accessor.getBufferIndices(root);

        }
    }

    public Vec4[] getVertexBuffer() {
        return this.vertexBuffer;
    }

    public Vec4[] getNormalBuffer() {
        return this.normalBuffer;
    }

    public int[] getBufferIndices() {
        return this.bufferIndices;
    }

    public GLTFMaterial getMaterial() {
        return this.material;
    }
    
    public String getName() {
        return this.name;
    }
}
