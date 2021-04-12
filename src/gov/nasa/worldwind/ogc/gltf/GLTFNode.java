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
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFArray,./GLTFRoot,./GLTFParserContext,../../geom/Matrix,./GLTFMesh,./GLTFUtil,../../avlist/AVListImpl")
public class GLTFNode extends GLTFArray {

    private int meshIdx;
    private GLTFMesh mesh;
    private Matrix matrix;
    private int[] childIndices;
    private GLTFNode[] children;
    private int cameraIdx;
    private double[] rotation;
    private double[] scale;
    private double[] translation;
    private String name;

    public GLTFNode(AVListImpl properties) {
        super();
        this.meshIdx = -1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_MESH:
                    this.meshIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MATRIX:
                    double[] matrixValues = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    this.matrix = Matrix.fromArray(matrixValues, 0, false);
                    // System.out.println(this.matrix.toString());
                    break;
                case GLTFParserContext.KEY_CHILDREN:
                    this.childIndices = GLTFUtil.retrieveIntArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_CAMERA:
                    this.cameraIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_ROTATION:
                    this.rotation = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_SCALE:
                    this.scale = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_TRANSLATION:
                    this.translation = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_NAME:
                    this.name = properties.getStringValue(propName);
                    break;
                default:
                    System.out.println("GLTFNode: Unsupported " + propName);
                    break;
            }
        }
    }

    public void assembleGeometry(GLTFRoot root) {
        if (this.meshIdx >= 0) {
            this.mesh = root.getMeshForIdx(this.meshIdx);
            this.mesh.assembleGeometry(root);
        }
        if (this.childIndices != null) {
            GLTFNode[] allNodes = root.getNodes();
            this.children = new GLTFNode[this.childIndices.length];
            for (int i = 0; i < this.childIndices.length; i++) {
                this.children[i] = allNodes[this.childIndices[i]];
                this.children[i].assembleGeometry(root);
            }
        }
    }

    public GLTFNode[] getChildren() {
        return this.children;
    }

    public GLTFMesh getMesh() {
        return this.mesh;
    }

    public boolean hasMatrix() {
        return this.matrix != null;
    }

    public Matrix getMatrix() {
        return this.matrix;
    }
    
    public String getName() {
        return this.name;
    }

}
