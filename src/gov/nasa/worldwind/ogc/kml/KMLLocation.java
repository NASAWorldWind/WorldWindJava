/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;

/**
 * Represents the KML <i>Location</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLocation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLLocation extends KMLAbstractObject
{
    private static final String LATITUDE_KEY="latitude";
    private static final String LONGITUDE_KEY="longitude";
    private static final String ALTITUDE_KEY="altitude";
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLocation(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getLongitude()
    {
        return (Double) this.getField(LONGITUDE_KEY);
    }

    public Double getLatitude()
    {
        return (Double) this.getField(LATITUDE_KEY);
    }

    public Double getAltitude()
    {
        return (Double) this.getField(ALTITUDE_KEY);
    }

    /**
     * Retrieves this location as a {@link Position}. Fields that are not set are treated as zero.
     *
     * @return Position object representing this location.
     */
    public Position getPosition()
    {
        Double lat = this.getLatitude();
        Double lon = this.getLongitude();
        Double alt = this.getAltitude();

        return Position.fromDegrees(
            lat != null ? lat : 0,
            lon != null ? lon : 0,
            alt != null ? alt : 0);
    }
    
    public void setPosition(Position pos) {
        this.setField(LATITUDE_KEY, pos.latitude.degrees);
        this.setField(LONGITUDE_KEY, pos.longitude.degrees);
        this.setField(ALTITUDE_KEY, pos.elevation);
    }
}
