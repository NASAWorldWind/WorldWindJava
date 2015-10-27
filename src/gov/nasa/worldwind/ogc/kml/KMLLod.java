/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>Lod</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLod.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLLod extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLod(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getMinLodPixels()
    {
        return (Double) this.getField("minLodPixels");
    }

    public Double getMaxLodPixels()
    {
        return (Double) this.getField("maxLodPixels");
    }

    public Double getMinFadeExtent()
    {
        return (Double) this.getField("minFadeExtent");
    }

    public Double getMaxFadeExtent()
    {
        return (Double) this.getField("maxFadeExtent");
    }
}
