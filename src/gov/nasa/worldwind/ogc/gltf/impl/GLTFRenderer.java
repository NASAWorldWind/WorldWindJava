package gov.nasa.worldwind.ogc.gltf.impl;

import gov.nasa.worldwind.geom.Matrix;
import java.util.HashMap;

import gov.nasa.worldwind.ogc.gltf.*;
import gov.nasa.worldwind.ogc.gltf.impl.GLTFTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFRenderableMesh,../GLTFRoot,../GLTFMesh")
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

    protected void preRenderMesh(GLTFMesh mesh, GLTFTraversalContext tc, DrawContext dc) {
        GLTFRenderableMesh renderableMesh = meshMap.get(mesh);
        if (renderableMesh == null) {
            renderableMesh = new GLTFRenderableMesh(mesh, this);
        }
        renderableMesh.setModelPosition(root.getPosition());
        renderableMesh.preRender(dc);
    }

    protected void renderMesh(GLTFMesh mesh, GLTFTraversalContext tc, DrawContext dc) {
        GLTFRenderableMesh renderableMesh = meshMap.get(mesh);
        if (renderableMesh == null) {
            renderableMesh = new GLTFRenderableMesh(mesh, this);
        }
        renderableMesh.setModelPosition(root.getPosition());
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
