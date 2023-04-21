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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for intersectors that compute intersections with a line. The line is specified by a single origin (see
 * {@link #setReferencePosition(gov.nasa.worldwind.geom.Position)}) and a list of end positions (see {@link
 * #setPositions(Iterable)}).
 *
 * @author tag
 * @version $Id: LineIntersector.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class LineIntersector implements Runnable
{
    protected Terrain terrain;
    protected int numThreads;

    protected Position referencePosition;
    protected Iterable<Position> positions;

    protected int numPositions;
    protected Vec4 referencePoint;
    protected ThreadPoolExecutor threadPool;
    protected AtomicInteger numProcessedPositions = new AtomicInteger();
    protected long startTime;
    protected long endTime; // for reporting calculation duration

    // Create a container to hold the intersections.
    protected Map<Position, List<Intersection>> allIntersections;

    protected LineIntersector(Terrain terrain, int numThreads)
    {
        this.terrain = terrain;
        this.numThreads = numThreads;

        this.threadPool = new ThreadPoolExecutor(numThreads, numThreads, 200, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

        this.allIntersections = new ConcurrentHashMap<Position, List<Intersection>>();
    }

    /**
     * Called to execute an intersection test for one position.
     *
     * @param position the position to test.
     *
     * @throws InterruptedException if the operation is interrupted.s
     */
    abstract protected void doPerformIntersection(Position position) throws InterruptedException;

    public Terrain getTerrain()
    {
        return this.terrain;
    }

    public int getNumThreads()
    {
        return this.numThreads;
    }

    public Position getReferencePosition()
    {
        return this.referencePosition;
    }

    /**
     * Sets the origin of the lines to intersect.
     *
     * @param referencePosition the origin to use for all lines.
     */
    public void setReferencePosition(Position referencePosition)
    {
        this.referencePosition = referencePosition;
    }

    public Iterable<Position> getPositions()
    {
        return positions;
    }

    /**
     * Specifies the end positions of all lines to intersect. Intersection tests are performed for each position, using
     * the position as the line's end and the current reference position as the line's origin.
     *
     * @param positions the positions.
     */
    public void setPositions(Iterable<Position> positions)
    {
        this.positions = positions;

        //noinspection UnusedDeclaration
        for (Position p : this.positions)
        {
            ++this.numPositions;
        }
    }

    public long getStartTime()
    {
        return this.startTime;
    }

    public long getEndTime()
    {
        return this.endTime;
    }

    public int getNumProcessedPositions()
    {
        return this.numProcessedPositions.get();
    }

    public Map<Position, List<Intersection>> getAllIntersections()
    {
        return allIntersections;
    }

    public List<Intersection> getIntersections(Position position)
    {
        return position != null ? this.allIntersections.get(position) : null;
    }

    public void run()
    {
        if (this.referencePosition == null || this.positions == null)
            throw new IllegalStateException("No reference positions or grid positions specified.");

        this.startTime = System.currentTimeMillis();
        this.numProcessedPositions.set(0);
        this.allIntersections.clear();

        try
        {
            this.referencePoint = terrain.getSurfacePoint(referencePosition.getLatitude(),
                referencePosition.getLongitude(), referencePosition.getAltitude());

            for (Position position : this.positions)
            {
                if (this.numThreads > 1)
                    this.threadPool.execute(new InternalIntersector(position));
                else// if (!position.equals(this.referencePosition))
                    this.performIntersection(position);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    protected class InternalIntersector implements Runnable
    {
        protected final Position position;

        public InternalIntersector(Position position)
        {
            this.position = position;
        }

        public void run()
        {
            try
            {
                performIntersection(position);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void performIntersection(Position position) throws InterruptedException
    {
        try
        {
            this.doPerformIntersection(position);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (this.numProcessedPositions.addAndGet(1) >= this.numPositions)
            this.endTime = System.currentTimeMillis();
    }
}
