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

package gov.nasa.worldwind.symbology;

/**
 * An interface for tactical graphics that depict routes: a series of point graphics connected by lines. For example,
 * the MIL-STD-2525 symbology set defines an Air Control Route that is composed of Air Control Points. The route is
 * composed of many tactical graphics, but it is treated as a single graphic. If the route is highlighted all of the
 * control points will also highlight, if the route is set invisible all the control points will be set invisible,
 * etc.
 *
 * @author pabercrombie
 * @version $Id: TacticalRoute.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TacticalGraphicFactory#createRoute(String, Iterable, gov.nasa.worldwind.avlist.AVList)
 */
public interface TacticalRoute extends TacticalGraphic
{
    /**
     * Indicates the control points along this route.
     *
     * @return This route's control points.
     */
    Iterable<? extends TacticalPoint> getControlPoints();

    /**
     * Specifies the control points along this route.
     *
     * @param points New control points.
     */
    void setControlPoints(Iterable<? extends TacticalPoint> points);
}
