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
