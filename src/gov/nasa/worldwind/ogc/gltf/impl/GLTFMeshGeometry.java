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

package gov.nasa.worldwind.ogc.gltf.impl;

import gov.nasa.worldwind.render.meshes.AbstractGeometry;
import gov.nasa.worldwind.util.typescript.TypeScript;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import java.nio.FloatBuffer;

@TypeScriptImports(imports = "../../../util/FloatBuffer,../../../render/meshes/AbstractGeometry")
public class GLTFMeshGeometry implements AbstractGeometry {

    protected FloatBuffer vertices;
    protected FloatBuffer texCoords;
    protected FloatBuffer normals;

    public GLTFMeshGeometry(FloatBuffer vertices, FloatBuffer normals) {
        this.init(vertices, normals);
    }

    protected final void init(FloatBuffer vertices, FloatBuffer normals) {
        this.vertices = vertices;
        this.normals = normals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TypeScript(substitute = "put(|putBuffer(")
    public void getVertices(FloatBuffer buffer) {
        this.vertices.rewind();
        buffer.put(this.vertices);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatBuffer getVertexBuffer() {
        return this.vertices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNormals() {
        return this.normals != null;
    }

    public boolean hasTexCoords() {
        return this.texCoords != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TypeScript(substitute = "put(|putBuffer(")
    public void getNormals(FloatBuffer buffer) {
        if (this.hasNormals()) {
            this.normals.rewind();
            buffer.put(this.normals);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatBuffer getNormalBuffer() {
        return this.normals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return this.vertices.capacity() / 9;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TypeScript(substitute = "put(|putBuffer(")
    public void getTextureCoordinates(FloatBuffer buffer) {
        if (this.hasTexCoords()) {
            this.texCoords.rewind();
            buffer.put(this.texCoords);
        }
    }

}
