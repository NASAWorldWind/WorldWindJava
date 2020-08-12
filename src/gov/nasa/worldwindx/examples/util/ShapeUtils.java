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
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: ShapeUtils.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ShapeUtils
{
    public static double getViewportScaleFactor(WorldWindow wwd)
    {
        return ((OrbitView) wwd.getView()).getZoom() / 8.0;
    }

    public static Position getNewShapePosition(WorldWindow wwd)
    {
        Line ray = new Line(wwd.getView().getEyePoint(), wwd.getView().getForwardVector());
        Intersection[] intersection = wwd.getSceneController().getTerrain().intersect(ray);

        if (intersection != null && intersection.length != 0)
        {
            return wwd.getModel().getGlobe().computePositionFromPoint(intersection[0].getIntersectionPoint());
        }
        else if (wwd.getView() instanceof OrbitView)
        {
            return ((OrbitView) wwd.getView()).getCenterPosition();
        }

        return Position.ZERO;
    }

    public static Angle getNewShapeHeading(WorldWindow wwd, boolean matchViewHeading)
    {
        if (matchViewHeading)
        {
            if (wwd.getView() instanceof OrbitView)
            {
                return ((OrbitView) wwd.getView()).getHeading();
            }
        }

        return Angle.ZERO;
    }

    public static List<LatLon> createSquareInViewport(WorldWindow wwd, Position position, Angle heading,
        double sizeInMeters)
    {
        Globe globe = wwd.getModel().getGlobe();
        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(globe.computeSurfaceOrientationAtPosition(position));
        transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));

        double widthOver2 = sizeInMeters / 2.0;
        double heightOver2 = sizeInMeters / 2.0;
        Vec4[] points = new Vec4[]
            {
                new Vec4(-widthOver2, -heightOver2, 0.0).transformBy4(transform), // lower left
                new Vec4(widthOver2, -heightOver2, 0.0).transformBy4(transform), // lower right
                new Vec4(widthOver2, heightOver2, 0.0).transformBy4(transform), // upper right
                new Vec4(-widthOver2, heightOver2, 0.0).transformBy4(transform)  // upper left
            };

        LatLon[] locations = new LatLon[points.length];
        for (int i = 0; i < locations.length; i++)
        {
            locations[i] = new LatLon(globe.computePositionFromPoint(points[i]));
        }

        return Arrays.asList(locations);
    }

    public static List<Position> createPositionSquareInViewport(WorldWindow wwd, Position position, Angle heading,
        double sizeInMeters)
    {
        Globe globe = wwd.getModel().getGlobe();
        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(globe.computeSurfaceOrientationAtPosition(position));
        transform = transform.multiply(Matrix.fromRotationZ(heading.multiply(-1)));

        double widthOver2 = sizeInMeters / 2.0;
        double heightOver2 = sizeInMeters / 2.0;
        double depthOver2 = sizeInMeters / 2.0;
        Vec4[] points = new Vec4[]
            {
                new Vec4(-widthOver2, -heightOver2, depthOver2).transformBy4(transform), // lower left
                new Vec4(widthOver2, -heightOver2, depthOver2).transformBy4(transform), // lower right
                new Vec4(widthOver2, heightOver2, depthOver2).transformBy4(transform), // upper right
                new Vec4(-widthOver2, heightOver2, depthOver2).transformBy4(transform)  // upper left
            };

        Position[] locations = new Position[points.length];
        for (int i = 0; i < locations.length; i++)
        {
            locations[i] = globe.computePositionFromPoint(points[i]);
        }

        return Arrays.asList(locations);
    }
}
