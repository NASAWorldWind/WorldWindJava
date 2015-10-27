/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.tracks;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

/**
 * @author tag
 * @version $Id: TrackPointImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TrackPointImpl implements TrackPoint
{
    private Position position;
    private String time;

    public TrackPointImpl(Angle lat, Angle lon, double elevation, String time)
    {
        this(new Position(lat, lon, elevation), time);
    }

    public TrackPointImpl(LatLon latLon, double elevation, String time)
    {
        this(new Position(latLon.getLatitude(), latLon.getLongitude(), elevation), time);
    }

    public TrackPointImpl(Position position, String time)
    {
        this.position = position;
    }

    public TrackPointImpl(Position position)
    {
        this(position, null);
    }

    public double getLatitude()
    {
        return this.position.getLatitude().degrees;
    }

    public void setLatitude(double latitude)
    {
        this.position = new Position(Angle.fromDegrees(latitude), this.position.getLongitude(),
            this.position.getElevation());
    }

    public double getLongitude()
    {
        return this.position.getLongitude().degrees;
    }

    public void setLongitude(double longitude)
    {
        this.position = new Position(this.position.getLatitude(), Angle.fromDegrees(longitude),
            this.position.getElevation());
    }

    public double getElevation()
    {
        return this.position.getElevation();
    }

    public void setElevation(double elevation)
    {
        this.position = new Position(this.position.getLatitude(), this.position.getLongitude(), elevation);
    }

    public String getTime()
    {
        return this.time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public Position getPosition()
    {
        return this.position;
    }

    public void setPosition(Position position)
    {
    }
}
