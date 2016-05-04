/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.event.Message;

/**
 * Represents the KML <i>AbstractView</i> element.
 *
 * @author tag
 * @version $Id: KMLAbstractView.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class KMLAbstractView extends KMLAbstractObject
{
    protected KMLAbstractView(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLAbstractView))
        {
            throw new IllegalArgumentException();
        }

        super.applyChange(sourceValues);

        this.onChange(new Message(KMLAbstractObject.MSG_VIEW_CHANGED, this));
    }
}
