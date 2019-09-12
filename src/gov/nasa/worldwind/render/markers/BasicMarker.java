/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
