/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.event.Message;

/**
 * Represents the KML <i>Geometry</i> element.
 *
 * @author tag
 * @version $Id: KMLAbstractGeometry.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLAbstractGeometry extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractGeometry(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLAbstractGeometry))
        {
            throw new IllegalArgumentException();
        }

        super.applyChange(sourceValues);

        this.onChange(new Message(KMLAbstractObject.MSG_GEOMETRY_CHANGED, this));
    }
}
