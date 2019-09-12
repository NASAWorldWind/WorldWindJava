/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>Model</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLModel extends KMLAbstractGeometry
{
    /** Flag to indicate that the link has been fetched from the hash map. */
    protected boolean linkFetched = false;
    protected KMLLink link;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLModel(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }

    public KMLLocation getLocation()
    {
        return (KMLLocation) this.getField("Location");
    }

    public KMLOrientation getOrientation()
    {
        return (KMLOrientation) this.getField("Orientation");
    }

    public KMLScale getScale()
    {
        return (KMLScale) this.getField("Scale");
    }

    public KMLLink getLink()
    {
        if (!this.linkFetched)
        {
            this.link = (KMLLink) this.getField("Link");
            this.linkFetched = true;
        }
        return this.link;
    }

    public KMLResourceMap getResourceMap()
    {
        return (KMLResourceMap) this.getField("ResourceMap");
    }
}
