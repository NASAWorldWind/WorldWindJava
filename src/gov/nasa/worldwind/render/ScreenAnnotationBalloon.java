/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * An AnnotationBalloon that is attached to a point on the screen.
 *
 * @author pabercrombie
 * @version $Id: ScreenAnnotationBalloon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScreenAnnotationBalloon extends AbstractAnnotationBalloon implements ScreenBalloon
{
    protected Point screenPoint;
    /** Annotation used to render the balloon. */
    protected ScreenAnnotation annotation;

    /**
     * Create the balloon.
     *
     * @param text  Text to display in the balloon. May not be null.
     * @param point The balloon's screen point. This point is interpreted in a coordinate system with the origin at the
     *              upper left corner of the screen.
     */
    public ScreenAnnotationBalloon(String text, Point point)
    {
        super(text);

        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.screenPoint = point;

        this.annotation = this.createAnnotation();
    }

    /** {@inheritDoc} */
    protected ScreenAnnotation createAnnotation()
    {
        ScreenAnnotation annotation = new ScreenAnnotation(this.getDecodedText(), this.screenPoint);

        // Don't make the balloon bigger when it is highlighted, the text looks blurry when it is scaled up.
        annotation.getAttributes().setHighlightScale(1);

        return annotation;
    }

    /** {@inheritDoc} */
    protected ScreenAnnotation getAnnotation()
    {
        return this.annotation;
    }

    /** {@inheritDoc} */
    protected void computePosition(DrawContext dc)
    {
        Rectangle viewport = dc.getView().getViewport();

        int y = (int) viewport.getHeight() - this.screenPoint.y - 1;
        this.getAnnotation().setScreenPoint(new Point(this.screenPoint.x, y));
    }

    /** {@inheritDoc} */
    public void setScreenLocation(Point point)
    {
        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.screenPoint = point;
    }

    /** {@inheritDoc} */
    public Point getScreenLocation()
    {
        return this.screenPoint;
    }
}
