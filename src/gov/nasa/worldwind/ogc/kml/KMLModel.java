/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

/**
 * Represents the KML <i>Model</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLModel.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLModel extends KMLAbstractGeometry implements KMLMutable
{
    private static final String LOCATION_KEY="Location";
    private static final String SCALE_KEY="Scale";
    
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
    
    public void setLocation(KMLLocation loc) {
        this.setField(LOCATION_KEY, loc);
    }

    public KMLLocation getLocation()
    {
        return (KMLLocation) this.getField(LOCATION_KEY);
    }

    public KMLOrientation getOrientation()
    {
        return (KMLOrientation) this.getField("Orientation");
    }
    
    public void setScale(KMLScale scale) {
        this.setField(SCALE_KEY, scale);
    }
    
    public KMLScale getScale()
    {
        return (KMLScale) this.getField(SCALE_KEY);
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

    @Override
    public void setPosition(Position position) {
        KMLLocation loc = this.getLocation();
        if (loc == null) {
            loc = new KMLLocation(this.getNamespaceURI());
            this.setLocation(loc);
        }
        loc.setPosition(position);
    }

    @Override
    public Position getPosition() {
        KMLLocation loc = this.getLocation();
        if (loc != null) {
            return loc.getPosition();
        }

        return null;
    }

    @Override
    public void setScale(Vec4 scale) {
        KMLScale curScale = this.getScale();
        if (curScale == null) {
            curScale = new KMLScale(this.getNamespaceURI());
            setScale(curScale);
        }
        curScale.setScale(scale);
    }
}
