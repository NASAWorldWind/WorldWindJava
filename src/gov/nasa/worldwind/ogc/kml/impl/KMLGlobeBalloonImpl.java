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

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

/**
 * A KML Balloon attached to a point on the globe.
 *
 * @author pabercrombie
 * @version $Id: KMLGlobeBalloonImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLGlobeBalloonImpl extends KMLAbstractBalloon implements GlobeBalloon
{
    /** The contained balloon. */
    protected GlobeBalloon balloon;

    /**
     * Create the balloon.
     *
     * @param balloon Balloon to apply KML styling to.
     * @param feature The feature that defines the balloon style.
     */
    public KMLGlobeBalloonImpl(GlobeBalloon balloon, KMLAbstractFeature feature)
    {
        super(feature);

        if (balloon == null)
        {
            String msg = Logging.getMessage("nullValue.BalloonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.balloon = balloon;
        this.initialize(balloon);
    }

    /** {@inheritDoc} */
    public GlobeBalloon getBalloon()
    {
        return this.balloon;
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setPosition(Position position)
    {
        this.getBalloon().setPosition(position);
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public Position getPosition()
    {
        return this.getBalloon().getPosition();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public int getAltitudeMode()
    {
        return this.getBalloon().getAltitudeMode();
    }

    /** {@inheritDoc}. This method passes through to the contained balloon. */
    public void setAltitudeMode(int altitudeMode)
    {
        this.getBalloon().setAltitudeMode(altitudeMode);
    }
}
