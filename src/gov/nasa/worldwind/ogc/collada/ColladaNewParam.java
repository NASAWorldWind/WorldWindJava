/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>newparam</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaNewParam.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaNewParam extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaNewParam(String ns)
    {
        super(ns);
    }

    public ColladaSampler2D getSampler2D()
    {
        return (ColladaSampler2D) this.getField("sampler2D");
    }

    public ColladaSurface getSurface()
    {
        return (ColladaSurface) this.getField("surface");
    }
}
