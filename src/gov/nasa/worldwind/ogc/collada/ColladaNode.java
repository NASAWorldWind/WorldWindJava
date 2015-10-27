/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.collada.impl.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Represents the COLLADA <i>node</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaNode.java 1696 2013-10-31 18:46:55Z tgaskins $
 */
public class ColladaNode extends ColladaAbstractObject implements ColladaRenderable
{
    /**
     * Children of this node. Children may be ColladaNode (direct child of this node) or ColladaInstanceNode (reference
     * to a node elsewhere in the current document, or another document).
     */
    protected List<ColladaRenderable> children;
    /** Geometries defined in this node. */
    protected List<ColladaInstanceGeometry> geometries;

    /** Shape used to render geometry in this node. */
    protected List<ColladaMeshShape> shapes;

    /** Transform matrix for this node. */
    protected Matrix matrix;

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaNode(String ns)
    {
        super(ns);
    }

    public Box getLocalExtent(ColladaTraversalContext tc)
    {
        if (tc == null)
        {
            String msg = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Create shapes for this node, if necessary
        if (this.shapes == null)
            this.shapes = this.createShapes();

        if (this.getMatrix() != null)
        {
            tc.pushMatrix();
            tc.multiplyMatrix(this.getMatrix());
        }

        ArrayList<Box> extents = new ArrayList<Box>();

        for (ColladaMeshShape shape : this.shapes)
        {
            Box extent = shape.getLocalExtent(tc);
            if (extent != null)
                extents.add(extent);
        }

        if (this.children != null)
        {
            for (ColladaRenderable node : children)
            {
                Box extent = node.getLocalExtent(tc);
                if (extent != null)
                    extents.add(extent);
            }
        }

        if (this.getMatrix() != null)
            tc.popMatrix();

        if (extents.isEmpty())
            return null;

        return Box.union(extents);
    }

    /** {@inheritDoc} */
    public void preRender(ColladaTraversalContext tc, DrawContext dc)
    {
        List<ColladaRenderable> children = this.getChildren();
        if (WWUtil.isEmpty(children))
            return;

        Matrix matrix = this.getMatrix();
        try
        {
            if (matrix != null && matrix != Matrix.IDENTITY)
            {
                tc.pushMatrix();
                tc.multiplyMatrix(matrix);
            }

            for (ColladaRenderable node : children)
            {
                node.preRender(tc, dc);
            }
        }
        finally
        {
            if (matrix != null && matrix != Matrix.IDENTITY)
                tc.popMatrix();
        }
    }

    /** {@inheritDoc} */
    public void render(ColladaTraversalContext tc, DrawContext dc)
    {
        // Create shapes for this node, if necessary
        if (this.shapes == null)
            this.shapes = this.createShapes();

        Matrix matrix = this.getMatrix();
        try
        {
            if (matrix != null && matrix != Matrix.IDENTITY)
            {
                tc.pushMatrix();
                tc.multiplyMatrix(matrix);
            }

            ColladaRoot root = this.getRoot();

            // Apply the current root position and highlight state to shapes in this node. Do this every frame so that
            // the node will pickup changes in the root's state.
            boolean highlighted = root.isHighlighted();
            int altitudeMode = root.getAltitudeMode();
            Position position = root.getPosition();

            Matrix traversalMatrix = tc.peekMatrix();
            for (ColladaMeshShape shape : this.shapes)
            {
                shape.setModelPosition(position);
                shape.setAltitudeMode(altitudeMode);
                shape.setHighlighted(highlighted);

                shape.render(dc, traversalMatrix);
            }

            for (ColladaRenderable node : this.getChildren())
            {
                node.render(tc, dc);
            }
        }
        finally
        {
            if (matrix != null && matrix != Matrix.IDENTITY)
                tc.popMatrix();
        }
    }

    /**
     * Create shapes to render this node.
     *
     * @return List shapes. The list may be empty, but will never be null.
     */
    protected List<ColladaMeshShape> createShapes()
    {
        if (WWUtil.isEmpty(this.geometries))
            return Collections.emptyList();

        List<ColladaMeshShape> shapes = new ArrayList<ColladaMeshShape>();
        for (ColladaInstanceGeometry geometry : this.geometries)
        {
            this.createShapesForGeometry(geometry, shapes);
        }
        return shapes;
    }

    /**
     * Create shapes for a geometry.
     *
     * @param geomInstance Geometry for which to create shapes.
     * @param shapes       List to collect the new shapes.
     */
    protected void createShapesForGeometry(ColladaInstanceGeometry geomInstance, List<ColladaMeshShape> shapes)
    {
        ColladaGeometry geometry = geomInstance.get();
        if (geometry == null)
            return;

        ColladaMesh mesh = geometry.getMesh();
        if (mesh == null)
            return;

        ColladaBindMaterial bindMaterial = geomInstance.getBindMaterial();
        ColladaRoot root = this.getRoot();

        List<ColladaTriangles> triangles = mesh.getTriangles();
        if (!WWUtil.isEmpty(triangles))
        {
            ColladaMeshShape newShape = ColladaMeshShape.createTriangleMesh(triangles, bindMaterial);
            newShape.setDelegateOwner(root);

            shapes.add(newShape);
        }

        List<ColladaLines> lines = mesh.getLines();
        if (!WWUtil.isEmpty(lines))
        {
            ColladaMeshShape newShape = ColladaMeshShape.createLineMesh(lines, bindMaterial);
            newShape.setDelegateOwner(root);

            shapes.add(newShape);
        }
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if ("node".equals(keyName) || "instance_node".equals(keyName))
        {
            if (this.children == null)
                this.children = new ArrayList<ColladaRenderable>();

            this.children.add((ColladaRenderable) value);
        }
        else if ("instance_geometry".equals(keyName))
        {
            if (this.geometries == null)
                this.geometries = new ArrayList<ColladaInstanceGeometry>();

            this.geometries.add((ColladaInstanceGeometry) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    /**
     * Indicates the children of this node. Children may be other node elements contained directly within this node, or
     * other nodes referenced indirectly by a instance_node element.
     *
     * @return List of children. The list may be empty, but will never be null.
     */
    protected List<ColladaRenderable> getChildren()
    {
        return this.children != null ? this.children : Collections.<ColladaRenderable>emptyList();
    }

    /**
     * Indicates the transform matrix specified in this node.
     *
     * @return The matrix specified in this node. Returns the identity matrix if the node does not specify a matrix.
     */
    protected Matrix getMatrix()
    {
        if (this.matrix != null)
            return this.matrix;

        // TODO a node can have more than one matrix
        ColladaMatrix matrix = (ColladaMatrix) this.getField("matrix");
        if (matrix == null)
        {
            // Set matrix to identity so that we won't look for it again.
            this.matrix = Matrix.IDENTITY;
            return this.matrix;
        }

        String matrixAsString = matrix.getCharacters();
        String linesCleaned = matrixAsString.replaceAll("\n", " ");

        double[] doubles = this.parseDoubleArray(linesCleaned);

        this.matrix = Matrix.fromArray(doubles, 0, true);
        return this.matrix;
    }

    /**
     * Parse a string of doubles into a double[].
     *
     * @param doubleArrayString String of doubles, separated by whitespace.
     *
     * @return Parsed double[]
     */
    protected double[] parseDoubleArray(String doubleArrayString)
    {
        String[] arrayOfNumbers = doubleArrayString.trim().split("\\s+");
        double[] doubles = new double[arrayOfNumbers.length];

        int i = 0;
        for (String s : arrayOfNumbers)
        {
            doubles[i++] = Double.parseDouble(s);
        }

        return doubles;
    }
}
