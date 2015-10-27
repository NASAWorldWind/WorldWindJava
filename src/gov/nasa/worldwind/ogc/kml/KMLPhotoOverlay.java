/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>PhotoOverlay</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLPhotoOverlay.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLPhotoOverlay extends KMLAbstractOverlay
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLPhotoOverlay(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getRotation()
    {
        return (Double) this.getField("rotation");
    }

    public KMLViewVolume getViewVolume()
    {
        return (KMLViewVolume) this.getField("ViewVolume");
    }

    public KMLImagePyramid getImagePyramid()
    {
        return (KMLImagePyramid) this.getField("ImagePyramid");
    }

    public KMLPoint getPoint()
    {
        return (KMLPoint) this.getField("Point");
    }

    public String getShape()
    {
        return (String) this.getField("shape");
    }
}
