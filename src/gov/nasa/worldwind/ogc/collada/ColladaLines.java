/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>lines</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id$
 */
public class ColladaLines extends ColladaAbstractGeometry
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaLines(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the number of vertices per line.
     *
     * @return Two
     */
    @Override
    protected int getVerticesPerShape()
    {
        return 2;
    }
}
