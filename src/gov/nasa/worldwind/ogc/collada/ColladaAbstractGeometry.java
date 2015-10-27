/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.Logging;

import java.nio.FloatBuffer;
import java.util.*;

/**
 * Base class for COLLADA geometry (lines and triangles).
 *
 * @author pabercrombie
 * @version $Id: ColladaAbstractGeometry.java 618 2012-06-01 17:35:11Z pabercrombie $
 */
public abstract class ColladaAbstractGeometry extends ColladaAbstractObject
{
    /**
     * Default semantic that identifies texture coordinates. Used the a file does not specify the semantic using a
     * <i>bind_vertex_input</i> element.
     */
    public static final String DEFAULT_TEX_COORD_SEMANTIC = "TEXCOORD";

    /** Number of coordinates per vertex. */
    public static final int COORDS_PER_VERTEX = 3;
    /** Number of texture coordinates per vertex. */
    public static final int TEX_COORDS_PER_VERTEX = 2;

    /** Inputs for the geometry. Inputs provide the geometry with vertices, texture coordinates, etc. */
    protected List<ColladaInput> inputs = new ArrayList<ColladaInput>();

    /**
     * Indicates the number of vertices per shape in the geometry.
     *
     * @return The number of vertices in each shape.
     */
    protected abstract int getVerticesPerShape();

    /**
     * Create a new instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaAbstractGeometry(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the inputs that provide vertices, textures coordinates, etc. to the geometry.
     *
     * @return Inputs to the geometry.
     */
    public List<ColladaInput> getInputs()
    {
        return this.inputs;
    }

    /**
     * Indicates the number of shapes (lines or triangles) in the geometry.
     *
     * @return The number of shapes in the geometry.
     */
    public int getCount()
    {
        return Integer.parseInt((String) this.getField("count"));
    }

    /**
     * Indicates the identifier for the material applied to this geometry.
     *
     * @return The material applied to this geometry. May be null.
     */
    public String getMaterial()
    {
        return (String) this.getField("material");
    }

    /**
     * Retrieves the coordinates of vertices in this geometry.
     *
     * @param buffer Buffer to receive coordinates.
     */
    public void getVertices(FloatBuffer buffer)
    {
        this.getFloatFromAccessor(buffer, this.getVertexAccessor(), "VERTEX", COORDS_PER_VERTEX);
    }

    /**
     * Retrieves normal vectors in this geometry.
     *
     * @param buffer Buffer to receive coordinates.
     */
    public void getNormals(FloatBuffer buffer)
    {
        this.getFloatFromAccessor(buffer, this.getNormalAccessor(), "NORMAL", COORDS_PER_VERTEX);
    }

    /**
     * Retrieves the texture coordinates of vertices in this geometry.
     *
     * @param buffer   Buffer to receive coordinates.
     * @param semantic String to identify which input holds the texture coordinates. May be null, in which case the
     *                 "TEXCOORD" is used.
     */
    public void getTextureCoordinates(FloatBuffer buffer, String semantic)
    {
        if (semantic == null)
            semantic = DEFAULT_TEX_COORD_SEMANTIC;

        this.getFloatFromAccessor(buffer, this.getTexCoordAccessor(semantic), semantic, TEX_COORDS_PER_VERTEX);
    }

    /**
     * Retrieve numbers from an accessor.
     *
     * @param buffer          Buffer to receive floats.
     * @param accessor        Accessor that will provide floats.
     * @param semantic        Semantic that identifiers the set of indices to use (for example, "VERTEX" or "NORMAL").
     * @param floatsPerVertex Number of floats to read for each vertex.
     */
    protected void getFloatFromAccessor(FloatBuffer buffer, ColladaAccessor accessor, String semantic,
        int floatsPerVertex)
    {
        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int vertsPerShape = this.getVerticesPerShape();
        int indexCount = this.getCount() * vertsPerShape;

        if (buffer.remaining() < indexCount * floatsPerVertex)
        {
            String msg = Logging.getMessage("generic.BufferSize");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int[] indices = this.getIndices(semantic);
        float[] vertexCoords = accessor.getFloats();

        for (int i : indices)
        {
            buffer.put(vertexCoords, i * floatsPerVertex, floatsPerVertex);
        }
    }

    protected int[] getIndices(String semantic)
    {
        ColladaInput input = null;
        for (ColladaInput in : this.getInputs())
        {
            if (semantic.equals(in.getSemantic()))
            {
                input = in;
                break;
            }
        }
        if (input == null)
            return null;

        ColladaP primitives = (ColladaP) this.getField("p");

        int vertsPerShape = this.getVerticesPerShape();
        int offset = input.getOffset();

        int[] intData = primitives.getIndices();

        int[] result = new int[this.getCount() * vertsPerShape];
        int ri = 0;

        int sourcesStride = this.getInputs().size();
        for (int i = 0; i < this.getCount(); i++)
        {
            for (int j = 0; j < vertsPerShape; j++)
            {
                int index = i * (vertsPerShape * sourcesStride) + j * sourcesStride;
                result[ri++] = intData[index + offset];
            }
        }

        return result;
    }

    public ColladaAccessor getVertexAccessor()
    {
        String vertexUri = null;
        for (ColladaInput input : this.getInputs())
        {
            if ("VERTEX".equals(input.getSemantic()))
            {
                vertexUri = input.getSource();
                break;
            }
        }

        if (vertexUri == null)
            return null;

        String positionUri = null;
        ColladaVertices vertices = (ColladaVertices) this.getRoot().resolveReference(vertexUri);
        for (ColladaInput input : vertices.getInputs())
        {
            if ("POSITION".equals(input.getSemantic()))
            {
                positionUri = input.getSource();
                break;
            }
        }

        if (positionUri == null)
            return null;

        ColladaSource source = (ColladaSource) this.getRoot().resolveReference(positionUri);
        return (source != null) ? source.getAccessor() : null;
    }

    public ColladaAccessor getNormalAccessor()
    {
        String sourceUri = null;
        for (ColladaInput input : this.getInputs())
        {
            if ("NORMAL".equals(input.getSemantic()))
            {
                sourceUri = input.getSource();
                break;
            }
        }

        if (sourceUri == null)
            return null;

        ColladaSource source = (ColladaSource) this.getRoot().resolveReference(sourceUri);
        return (source != null) ? source.getAccessor() : null;
    }

    /**
     * Indicates the accessor for texture coordinates.
     *
     * @param semantic Semantic that identifies the texture coordinates. May be null, in which case the semantic
     *                 "TEXCOORD" is used.
     *
     * @return The texture coordinates accessor, or null if the accessor cannot be resolved.
     */
    public ColladaAccessor getTexCoordAccessor(String semantic)
    {
        if (semantic == null)
            semantic = DEFAULT_TEX_COORD_SEMANTIC;

        String sourceUri = null;
        for (ColladaInput input : this.getInputs())
        {
            if (semantic.equals(input.getSemantic()))
            {
                sourceUri = input.getSource();
                break;
            }
        }

        if (sourceUri == null)
            return null;

        ColladaSource source = (ColladaSource) this.getRoot().resolveReference(sourceUri);
        return (source != null) ? source.getAccessor() : null;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("input"))
        {
            this.inputs.add((ColladaInput) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
