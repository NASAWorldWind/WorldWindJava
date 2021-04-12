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

import gov.nasa.worldwind.geom.Matrix;
import java.util.HashMap;

import gov.nasa.worldwind.ogc.gltf.*;
import gov.nasa.worldwind.ogc.gltf.impl.GLTFTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import gov.nasa.worldwind.util.typescript.TypeScript;

@TypeScriptImports(imports = "../GLTFScene,../GLTFNode,./GLTFTraversalContext,./GLTFRenderableMesh,../GLTFRoot,../GLTFMesh,../../../shapes/ShapeAttributes,../../../render/DrawContext")
public class GLTFRenderer {

    private final GLTFRoot root;
    private final HashMap<GLTFMesh, GLTFRenderableMesh> meshMap;

    public GLTFRenderer(GLTFRoot root) {
        this.meshMap = new HashMap<>();
        this.root = root;
    }

    public ShapeAttributes getAttributes() {
        return this.root.getAttributes();

    }
    
    @TypeScript(substitute = "put(|set(")
    protected void preRenderMesh(GLTFMesh mesh, GLTFTraversalContext tc, DrawContext dc) {
        GLTFRenderableMesh renderableMesh = this.meshMap.get(mesh);
        if (renderableMesh == null) {
            renderableMesh = new GLTFRenderableMesh(mesh, this);
            this.meshMap.put(mesh, renderableMesh);
        }
        renderableMesh.setModelPosition(this.root.getPosition());
        renderableMesh.preRender(dc);
    }

    @TypeScript(substitute = "put(|set(")
    protected void renderMesh(GLTFMesh mesh, GLTFTraversalContext tc, DrawContext dc) {
        GLTFRenderableMesh renderableMesh = this.meshMap.get(mesh);
        if (renderableMesh == null) {
            renderableMesh = new GLTFRenderableMesh(mesh, this);
            this.meshMap.put(mesh, renderableMesh);
        }
        renderableMesh.setModelPosition(this.root.getPosition());
        renderableMesh.renderOriented(dc, tc.peekMatrix());
    }

    protected void preRenderNode(GLTFNode node, GLTFTraversalContext tc, DrawContext dc) {
        GLTFMesh mesh = node.getMesh();
        if (mesh != null) {
            this.preRenderMesh(mesh, tc, dc);
        }

    }

    protected void renderNode(GLTFNode node, GLTFTraversalContext tc, DrawContext dc) {
        if (node.hasMatrix()) {
            tc.pushMatrix(null);
            tc.multiplyMatrix(node.getMatrix());
        }
        GLTFMesh mesh = node.getMesh();
        if (mesh != null) {
            this.renderMesh(mesh, tc, dc);
        }
        GLTFNode[] children = node.getChildren();
        if (children != null) {
            for (GLTFNode child : children) {
                this.renderNode(child, tc, dc);
            }
        }
        if (node.hasMatrix()) {
            tc.popMatrix();
        }
    }

    public void preRender(GLTFScene scene, GLTFTraversalContext tc, DrawContext dc) {
        GLTFNode[] nodes = scene.getNodes();
        for (GLTFNode node : nodes) {
            this.preRenderNode(node, tc, dc);
        }
    }

    public void render(GLTFScene scene, GLTFTraversalContext tc, DrawContext dc) {
        GLTFNode[] nodes = scene.getNodes();
        for (GLTFNode node : nodes) {
            this.renderNode(node, tc, dc);
        }

    }

}
