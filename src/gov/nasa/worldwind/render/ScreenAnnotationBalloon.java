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
    @Override
	protected ScreenAnnotation createAnnotation()
    {
        ScreenAnnotation annotation = new ScreenAnnotation(this.getDecodedText(), this.screenPoint);

        // Don't make the balloon bigger when it is highlighted, the text looks blurry when it is scaled up.
        annotation.getAttributes().setHighlightScale(1);

        return annotation;
    }

    /** {@inheritDoc} */
    @Override
	protected ScreenAnnotation getAnnotation()
    {
        return this.annotation;
    }

    /** {@inheritDoc} */
    @Override
	protected void computePosition(DrawContext dc)
    {
        this.getAnnotation().setScreenPoint(new Point(this.screenPoint));
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
	public Point getScreenLocation()
    {
        return this.screenPoint;
    }
}
