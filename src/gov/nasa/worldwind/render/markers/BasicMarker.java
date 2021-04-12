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

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: BasicMarker.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicMarker implements Marker
{
    protected Position position; // may be null
    protected Angle heading; // may be null
    protected Angle pitch; // may be null
    protected Angle roll; // may be null

    // To avoid the memory overhead of creating an attributes object for every new marker, attributes are
    // required to be specified at construction.
    protected MarkerAttributes attributes;

    public BasicMarker(Position position, MarkerAttributes attrs)
    {
        if (attrs == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
        this.attributes = attrs;
    }

    public BasicMarker(Position position, MarkerAttributes attrs, Angle heading)
    {
        if (attrs == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
        this.heading = heading;
        this.attributes = attrs;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    /** {@inheritDoc} */
    public Angle getHeading()
    {
        return this.heading;
    }

    /** {@inheritDoc} */
    public void setHeading(Angle heading)
    {
        this.heading = heading;
    }

    /** {@inheritDoc} */
    public Angle getRoll()
    {
        return this.roll;
    }

    /** {@inheritDoc} */
    public void setRoll(Angle roll)
    {
        this.roll = roll;
    }

    /** {@inheritDoc} */
    public Angle getPitch()
    {
        return this.pitch;
    }

    /** {@inheritDoc} */
    public void setPitch(Angle pitch)
    {
        this.pitch = pitch;
    }

    public MarkerAttributes getAttributes()
    {
        return attributes;
    }

    public void setAttributes(MarkerAttributes attributes)
    {
        this.attributes = attributes;
    }

    public void render(DrawContext dc, Vec4 point, double radius, boolean isRelative)
    {
        this.attributes.getShape(dc).render(dc, this, point, radius, isRelative);
    }

    public void render(DrawContext dc, Vec4 point, double radius)
    {
        this.attributes.getShape(dc).render(dc, this, point, radius, false);
    }
}
