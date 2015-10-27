/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>ViewVolume</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLViewVolume.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLViewVolume extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLViewVolume(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getNear()
    {
        return (Double) this.getField("near");
    }

    public Double getLeftFov()
    {
        return (Double) this.getField("leftFov");
    }

    public Double getRightFov()
    {
        return (Double) this.getField("rightFov");
    }

    public Double getTopFov()
    {
        return (Double) this.getField("topFov");
    }

    public Double getBottomFov()
    {
        return (Double) this.getField("bottomFov");
    }
}
