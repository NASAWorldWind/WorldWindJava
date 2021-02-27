package gov.nasa.worldwind.ogc.gltf.impl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import java.util.ArrayList;
import java.nio.FloatBuffer;

import gov.nasa.worldwind.render.meshes.Mesh3D;
import gov.nasa.worldwind.ogc.gltf.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.meshes.AbstractGeometry;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;

public class GLTFRenderableMesh extends Mesh3D {


    private GLTFMesh srcMesh;
    private ArrayList<GLTFMeshGeometry> renderableGeometries;

    public GLTFRenderableMesh(GLTFMesh srcMesh, GLTFRenderer renderContext) {
        super();
        this.srcMesh = srcMesh;
        this.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        this.setElementType(GL.GL_TRIANGLES);
        ShapeAttributes attrs = renderContext.getAttributes();
        if (srcMesh.getMaterial() != null) {
            attrs = new BasicShapeAttributes(attrs);
            GLTFUtil.computeMaterialAttrs(attrs, srcMesh.getMaterial());
        }
        this.setAttributes(attrs);
    }

    protected void assembleRenderableGeometries(DrawContext dc) {
        this.setVertsPerShape(3);
        this.renderableGeometries = new ArrayList<>();
        Vec4[] vtxBuffer = this.srcMesh.getVertexBuffer();
        Vec4[] normalBuffer = this.srcMesh.getNormalBuffer();
        int[] bufferIndices = this.srcMesh.getBufferIndices();
        FloatBuffer vertices = Buffers.newDirectFloatBuffer(bufferIndices.length * 3);
        FloatBuffer normals = null;
        if (normalBuffer != null) {
            normals = Buffers.newDirectFloatBuffer(bufferIndices.length * 3);
        }

        for (int i = 0; i < bufferIndices.length; i++) {
            int idx = bufferIndices[i];
            Vec4 vtx = vtxBuffer[idx];
            vertices.put((float) vtx.x);
            vertices.put((float) vtx.y);
            vertices.put((float) vtx.z);
            if (normals != null) {
                Vec4 normal = normalBuffer[idx];
                normals.put((float) normal.x);
                normals.put((float) normal.y);
                normals.put((float) normal.z);
            }
        }
        this.renderableGeometries.add(new GLTFMeshGeometry(vertices, normals));
        this.setRenderableGeometries(this.renderableGeometries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(DrawContext dc, Matrix transform) {
        if (this.renderableGeometries == null) {
            this.assembleRenderableGeometries(dc);
        }

        super.render(dc, transform);
    }

    @Override
    protected boolean isDoubleSided(AbstractGeometry geometry) {
        return true;
    }
}
