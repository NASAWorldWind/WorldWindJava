/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * A visual marker with position and orientation. The marker can be oriented in 3D space with a heading, pitch, and
 * roll. However, not all implementations apply the orientation to the rendered marker. An implementation may apply any
 * or none or the orientation parameters. For example, an implementation that renders the marker as a cone may ignore
 * roll, because the cone is symmetric along its axis.
 *
 * @author tag
 * @version $Id: Marker.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Marker
{
    void render(DrawContext dc, Vec4 point, double radius, boolean isRelative);

    void render(DrawContext dc, Vec4 point, double radius);

    Position getPosition();

    void setPosition(Position position);

    MarkerAttributes getAttributes();

    void setAttributes(MarkerAttributes attributes);

    /**
     * Indicates heading of this marker. Not all implementations support heading. If the implementation does not support
     * heading, the heading will be ignored.
     *
     * @return The marker heading in degrees clockwise from North. May be null, in which case no heading is applied.
     */
    Angle getHeading();

    /**
     * Specifies the heading of this marker.
     *
     * @param heading the marker heading in degrees clockwise from North. May be null, in which case no heading is
     *                applied.
     */
    void setHeading(Angle heading);

    /**
     * Indicates pitch this marker. Not all implementations support pitch. If the implementation does not support pitch,
     * the pitch will be ignored.
     *
     * @return The marker pitch in degrees from a surface normal. May be null, in which case no heading is applied.
     */
    Angle getPitch();

    /**
     * Specifies the pitch of this marker. Not all implementations support pitch. If the implementation does not support
     * pitch, the pitch will be ignored.
     *
     * @param pitch the marker pitch in degrees from a surface normal. Positive values result in a rotation toward the
     *              marker heading, or toward North if there is no heading. May be null, in which case no pitch is
     *              applied.
     */
    void setPitch(Angle pitch);

    /**
     * Indicates the roll of this marker. Not all implementations support roll. If the implementation does not support
     * roll, the roll will be ignored.
     *
     * @return The marker roll in degrees clockwise. May be null, in which case no roll is applied.
     */
    Angle getRoll();

    /**
     * Specifies the roll of this marker. Not all implementations support roll. If the implementation does not support
     * roll, the roll will be ignored.
     *
     * @param roll the marker roll in degrees clockwise. May be null, in which case no roll is applied.
     */
    void setRoll(Angle roll);
}
