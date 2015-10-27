/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;

/**
 * An AnnotationBalloon that is attached to a position on the globe.
 *
 * @author pabercrombie
 * @version $Id: GlobeAnnotationBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GlobeAnnotationBalloon extends AbstractAnnotationBalloon implements GlobeBalloon
{
    protected Position position;
    protected int altitudeMode;

    /** Annotation used to render the balloon. */
    protected GlobeAnnotation annotation;

    /**
     * Create the balloon.
     *
     * @param text     Text to display in the balloon. May not be null.
     * @param position The balloon's initial position. May not be null.
     */
    public GlobeAnnotationBalloon(String text, Position position)
    {
        super(text);

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;

        this.annotation = this.createAnnotation();
    }

    /** {@inheritDoc} */
    protected GlobeAnnotation createAnnotation()
    {
        GlobeAnnotation annotation = new GlobeAnnotation(this.getDecodedText(), this.position);

        // Don't make the balloon bigger when it is highlighted, the text looks blurry when it is scaled up.
        annotation.getAttributes().setHighlightScale(1);

        return annotation;
    }

    /** {@inheritDoc} */
    protected GlobeAnnotation getAnnotation()
    {
        return this.annotation;
    }

    /** {@inheritDoc} */
    protected void computePosition(DrawContext dc)
    {
        GlobeAnnotation annotation = this.getAnnotation();
        annotation.setPosition(this.getPosition());
        annotation.setAltitudeMode(this.getAltitudeMode());
    }

    /** {@inheritDoc} */
    public void setPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
    }

    /** {@inheritDoc} */
    public Position getPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    /** {@inheritDoc} */
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
    }
}
