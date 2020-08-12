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

package gov.nasa.worldwindx.examples.lineofsight;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.terrain.Terrain;

import java.util.*;

/**
 * Computes the intersections of a collection of lines with the terrain. The lines are specified with a common origin
 * and multiple end positions. For each end position this class computes the intersections of a line between that
 * position and a reference position. See {@link #setReferencePosition(gov.nasa.worldwind.geom.Position)} and {@link
 * #setPositions(Iterable)}.
 *
 * @author tag
 * @version $Id: TerrainLineIntersector.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TerrainLineIntersector extends LineIntersector
{
    /**
     * Consruct an terrain intersector.
     *
     * @param terrain    the terrain to use to determine terrain geometry.
     * @param numThreads the number of threads to use.
     */
    public TerrainLineIntersector(Terrain terrain, int numThreads)
    {
        super(terrain, numThreads);
    }

    protected void doPerformIntersection(Position position) throws InterruptedException
    {
        // Intersect the line between this grid point and the selected position.
        Intersection[] intersections = this.terrain.intersect(this.referencePosition, position);
        if (intersections == null || intersections.length == 0)
            return; // No intersection

        // Check to see whether the first intersection is beyond the grid point.
        Vec4 iPoint = intersections[0].getIntersectionPoint();
        Vec4 gPoint = terrain.getSurfacePoint(position.getLatitude(), position.getLongitude(), position.getAltitude());

        if (iPoint.distanceTo3(this.referencePoint) >= gPoint.distanceTo3(this.referencePoint))
            return; // Intersection is beyond the position.

        Position iPosition = this.terrain.getGlobe().computePositionFromPoint(iPoint);

        List<Intersection> iList = new ArrayList<Intersection>();
        iList.add(new Intersection(iPoint, new Position(iPosition, 0), false, null));

        for (int i = 1; i < intersections.length; i++)
        {
            iPoint = intersections[i].getIntersectionPoint();
            iPosition = this.terrain.getGlobe().computePositionFromPoint(iPoint);
            iList.add(new Intersection(iPoint, new Position(iPosition, 0), false, null));
        }

        this.allIntersections.put(position, iList);
    }
}
