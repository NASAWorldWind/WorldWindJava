package gov.nasa.worldwind.ogc.gltf.impl;

import gov.nasa.worldwind.geom.Matrix;
import java.util.HashMap;

import gov.nasa.worldwind.ogc.gltf.*;
import gov.nasa.worldwind.ogc.gltf.impl.GLTFTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "../GLTFRoot,../GLTFMesh")
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

    protected void preRender(GLTFMesh mesh, GLTFTraversalContext tc, DrawContext dc) {
        GLTFRenderableMesh renderableMesh = meshMap.get(mesh);
        if (renderableMesh == null) {
            renderableMesh = new GLTFRenderableMesh(mesh, this);
        }
        renderableMesh.setModelPosition(root.getPosition());
        renderableMesh.preRender(dc);
    }

    protected void render(GLTFMesh mesh, GLTFTraversalContext tc, DrawContext dc) {
        GLTFRenderableMesh renderableMesh = meshMap.get(mesh);
        if (renderableMesh == null) {
            renderableMesh = new GLTFRenderableMesh(mesh, this);
        }
        renderableMesh.setModelPosition(root.getPosition());
        renderableMesh.render(dc, tc.peekMatrix());
    }

    protected void preRender(GLTFNode node, GLTFTraversalContext tc, DrawContext dc) {
        GLTFMesh mesh = node.getMesh();
        if (mesh != null) {
            this.preRender(mesh, tc, dc);
        }

    }

    protected void render(GLTFNode node, GLTFTraversalContext tc, DrawContext dc) {
        if (node.hasMatrix()) {
            tc.pushMatrix();
            tc.multiplyMatrix(node.getMatrix());
        }
        GLTFMesh mesh = node.getMesh();
        if (mesh != null) {
            this.render(mesh, tc, dc);
        }
        GLTFNode[] children = node.getChildren();
        if (children != null) {
            for (GLTFNode child : children) {
                this.render(child, tc, dc);
            }
        }
        if (node.hasMatrix()) {
            tc.popMatrix();
        }
    }

    public void preRender(GLTFScene scene, GLTFTraversalContext tc, DrawContext dc) {
        GLTFNode[] nodes = scene.getNodes();
        for (GLTFNode node : nodes) {
            this.preRender(node, tc, dc);
        }
    }

    public void render(GLTFScene scene, GLTFTraversalContext tc, DrawContext dc) {
        GLTFNode[] nodes = scene.getNodes();
        for (GLTFNode node : nodes) {
            this.render(node, tc, dc);
        }

    }

}
