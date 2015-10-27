/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>triangles</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaTriangles.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaTriangles extends ColladaAbstractGeometry
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaTriangles(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the number of vertices for each triangle.
     *
     * @return Three
     */
    @Override
    protected int getVerticesPerShape()
    {
        return 3;
    }
}
