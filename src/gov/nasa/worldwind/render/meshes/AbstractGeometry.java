package gov.nasa.worldwind.render.meshes;

import java.nio.FloatBuffer;

public interface AbstractGeometry {

    /**
     * Number of coordinates per vertex.
     */
    public static final int COORDS_PER_VERTEX = 3;

    /**
     * Number of texture coordinates per vertex.
     */
    public static final int TEX_COORDS_PER_VERTEX = 2;

    /**
     * Retrieves the coordinates of vertices in this geometry.
     *
     * @param buffer Buffer to receive coordinates.
     */
    public void getVertices(FloatBuffer buffer);

    /**
     * Retrieves the coordinates of vertices in this geometry.
     *
     * @return A {@link #FloatBuffer} containing the vertices.
     */
    public FloatBuffer getVertexBuffer();

    /**
     * Returns a flag indicating whether the geometry has normals.
     *
     * @return True if the geometry has normals, false otherwise.
     */
    public boolean hasNormals();

    /**
     * Retrieves normal vectors in this geometry.
     *
     * @param buffer Buffer to receive coordinates.
     */
    public void getNormals(FloatBuffer buffer);

    /**
     * Retrieves the coordinates of normals in this geometry.
     *
     * @return A {@link #FloatBuffer} containing the normals.
     */
    public FloatBuffer getNormalBuffer();

    /**
     * Indicates the number of shapes (lines or triangles) in the geometry.
     *
     * @return The number of shapes in the geometry.
     */
    public int getCount();

    /**
     * Retrieves the texture coordinates of vertices in this geometry.
     *
     * @param buffer Buffer to receive coordinates.
     */
    public void getTextureCoordinates(FloatBuffer buffer);
}
