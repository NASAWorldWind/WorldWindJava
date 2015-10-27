/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>asset</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaAsset.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaAsset extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaAsset(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the {@code unit} element.
     *
     * @return This Asset's unit field, or null if no unit is set.
     */
    public ColladaUnit getUnit()
    {
        return (ColladaUnit) this.getField("unit");
    }
}
