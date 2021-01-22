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

public class GLTFRenderableMesh extends Mesh3D {

    protected class Geometry implements AbstractGeometry {

        protected FloatBuffer vertices;
        protected FloatBuffer texCoords;
        protected FloatBuffer normals;

        public Geometry(FloatBuffer vertices, FloatBuffer normals) {
            init(vertices, normals);
        }

        protected final void init(FloatBuffer vertices, FloatBuffer normals) {
            this.vertices = vertices;
            this.normals = normals;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getVertices(FloatBuffer buffer) {
            this.vertices.rewind();
            buffer.put(this.vertices);
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
        public int getCount() {
            return this.vertices.capacity() / 9;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getTextureCoordinates(FloatBuffer buffer) {
            if (this.hasTexCoords()) {
                this.texCoords.rewind();
                buffer.put(this.texCoords);
            }
        }

    }

    private GLTFMesh srcMesh;
    private ArrayList<Geometry> geometries;

    public GLTFRenderableMesh(GLTFMesh srcMesh) {
        this.srcMesh = srcMesh;
        this.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        this.setElementType(GL.GL_TRIANGLES);
    }

    protected void assembleGeometries(DrawContext dc) {
        this.setVertsPerShape(3);
        this.geometries = new ArrayList<>();
        float[] vtxBuffer = this.srcMesh.getVertexBuffer();
        float[] normalBuffer = this.srcMesh.getNormalBuffer();
        int[] bufferIndices = this.srcMesh.getBufferIndices();
        FloatBuffer vertices = Buffers.newDirectFloatBuffer(bufferIndices.length * 3);
        FloatBuffer normals = null;
        if (normalBuffer != null) {
            normals = Buffers.newDirectFloatBuffer(normalBuffer.length * 3);
        }

        for (int i = 0; i < bufferIndices.length; i++) {
            int offset = bufferIndices[i] * 3;
            vertices.put(vtxBuffer[offset]);
            vertices.put(vtxBuffer[offset + 1]);
            vertices.put(vtxBuffer[offset + 2]);
            if (normals != null) {
                normals.put(normalBuffer[offset]);
                normals.put(normalBuffer[offset + 1]);
                normals.put(normalBuffer[offset + 2]);
            }
        }
        this.geometries.add(new Geometry(vertices, normals));
        this.setGeometries(this.geometries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(DrawContext dc, Matrix transform) {
        if (this.geometries == null) {
            this.assembleGeometries(dc);
        }

        super.render(dc, transform);
    }

    @Override
    protected boolean isDoubleSided(AbstractGeometry geometry) {
        return true;
    }
}
