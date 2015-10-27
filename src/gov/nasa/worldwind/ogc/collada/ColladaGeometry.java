/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>geometry</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaGeometry.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaGeometry extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaGeometry(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the mesh contained by this geometry.
     *
     * @return The mesh element, or null if none is set.
     */
    public ColladaMesh getMesh()
    {
        return (ColladaMesh) this.getField("mesh");
    }
}
