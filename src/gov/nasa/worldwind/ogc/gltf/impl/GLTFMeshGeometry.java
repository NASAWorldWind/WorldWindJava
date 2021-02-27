package gov.nasa.worldwind.ogc.gltf.impl;

import gov.nasa.worldwind.render.meshes.AbstractGeometry;
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
