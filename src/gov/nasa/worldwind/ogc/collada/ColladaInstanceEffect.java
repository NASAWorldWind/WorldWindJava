/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>instance_effect</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaInstanceEffect.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaInstanceEffect extends ColladaAbstractInstance<ColladaEffect>
{
    public ColladaInstanceEffect(String ns)
    {
        super(ns);
    }
}
