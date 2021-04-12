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
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;

import java.util.*;

/**
 * Computes the intersections of a collection of lines with collection of {@link Renderable}s. The lines are specified
 * with a common origin and multiple end positions. For each end position this class computes the intersections of all
 * specified renderables with a line between that position and a reference position. See {@link
 * #setReferencePosition(gov.nasa.worldwind.geom.Position)} and {@link #setRenderables(Iterable)}.
 *
 * @author tag
 * @version $Id: ShapeLineIntersector.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ShapeLineIntersector extends LineIntersector
{
    protected Iterable<Renderable> renderables;

    public ShapeLineIntersector(Terrain terrain, int numThreads)
    {
        super(terrain, numThreads);
    }

    public Iterable<Renderable> getRenderables()
    {
        return renderables;
    }

    /**
     * Specifies the renderables to intersect with the line.
     *
     * @param renderables the renderables to intersect.
     */
    public void setRenderables(Iterable<Renderable> renderables)
    {
        this.renderables = renderables;
    }

    /**
     * Indicates whether this intersector has renderables specified.
     *
     * @return true if this intersector has renderables, otherwise false.
     */
    public boolean hasRenderables()
    {
        return this.renderables != null && this.renderables.iterator().hasNext();
    }

    protected void doPerformIntersection(Position position) throws InterruptedException
    {
        if (this.renderables == null)
            return;

        Vec4 point = this.terrain.getSurfacePoint(position);
        Line line = new Line(this.referencePoint, point.subtract3(this.referencePoint));
        double length = point.distanceTo3(this.referencePoint);

        List<Intersection> losList = new ArrayList<Intersection>();

        for (Renderable renderable : this.renderables)
        {
            try
            {
                List<Intersection> renderableIntersections = performRenderableIntersection(line, renderable);
                if (renderableIntersections != null)
                {
                    // Filter out intersections beyond the position.
                    for (Intersection los : renderableIntersections)
                    {
                        if (los.getIntersectionPoint().distanceTo3(this.referencePoint) <= length)
                            losList.add(los);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (!losList.isEmpty())
            this.allIntersections.put(position, losList);
    }

    protected List<Intersection> performRenderableIntersection(Line line, Renderable renderable)
        throws InterruptedException
    {
        List<Intersection> intersections = null;

        if (renderable instanceof ExtrudedPolygon)
            intersections = ((ExtrudedPolygon) renderable).intersect(line, this.terrain);

        return intersections;
    }
}
