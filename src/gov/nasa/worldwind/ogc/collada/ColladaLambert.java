/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA Lambert shader element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaLambert.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaLambert extends ColladaAbstractShader
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaLambert(String namespaceURI)
    {
        super(namespaceURI);
    }
}
