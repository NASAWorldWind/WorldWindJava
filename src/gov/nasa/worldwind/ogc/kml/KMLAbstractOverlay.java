/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>Overlay</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLAbstractOverlay.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLAbstractOverlay extends KMLAbstractFeature
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractOverlay(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getColor()
    {
        return (String) this.getField("color");
    }

    public Integer getDrawOrder()
    {
        return (Integer) this.getField("drawOrder");
    }

    public KMLIcon getIcon()
    {
        return (KMLIcon) this.getField("Icon");
    }
}
