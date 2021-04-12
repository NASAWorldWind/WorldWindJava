/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
