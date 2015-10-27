/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>LineStyle</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLineStyle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLLineStyle extends KMLAbstractColorStyle
{
    public KMLLineStyle(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getWidth()
    {
        return (Double) this.getField("width");
    }
}
