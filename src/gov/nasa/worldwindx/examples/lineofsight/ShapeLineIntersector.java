/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
