/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

import java.util.*;

/**
 * Determines the visible sectors at a specifed resolution within the draw context's current visible sector.
 *
 * @author Tom Gaskins
 * @version $Id: SectorVisibilityTree.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SectorVisibilityTree
{
    protected static class Context
    {
        private final DrawContext dc;
        private final double sectorSize;
        private final List<Sector> sectors;

        public Context(DrawContext dc, double sectorSize, List<Sector> sectors)
        {
            this.dc = dc;
            this.sectorSize = sectorSize;
            this.sectors = sectors;
        }
    }

    protected double sectorSize;
    protected Object globeStateKey;
    protected HashMap<Sector, Extent> prevExtents = new HashMap<Sector, Extent>();
    protected HashMap<Sector, Extent> newExtents = new HashMap<Sector, Extent>();
    protected ArrayList<Sector> sectors = new ArrayList<Sector>();
    protected long timeStamp;

    public SectorVisibilityTree()
    {
    }

    public double getSectorSize()
    {
        return sectorSize;
    }

    public List<Sector> getSectors()
    {
        return this.sectors;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public void clearSectors()
    {
        this.sectors.clear();
    }

    protected DecisionTree<Sector, Context> tree = new DecisionTree<Sector, Context>(
        new DecisionTree.Controller<Sector, Context>()
        {
            public boolean isTerminal(Sector s, Context context)
            {
                if (s.getDeltaLat().degrees > context.sectorSize)
                    return false;

                context.sectors.add(s);
                return true;
            }

            public Sector[] split(Sector s, Context context)
            {
                return s.subdivide();
            }

            public boolean isVisible(Sector s, Context c)
            {
                Extent extent = prevExtents.get(s);
                if (extent == null)
                    extent = Sector.computeBoundingBox(c.dc.getGlobe(), c.dc.getVerticalExaggeration(), s);

                if (extent.intersects(c.dc.getView().getFrustumInModelCoordinates()))
                {
                    newExtents.put(s, extent);
                    return true;
                }

                return false;
            }
        });

    /**
     * Determines the visible sectors at a specifed resolution within the draw context's current visible sector.
     *
     * @param dc         the current draw context
     * @param sectorSize the granularity of sector visibility, in degrees. All visible sectors of this size are found.
     *                   The value must be in the range, 1 second <= sectorSize <= 180 degrees.
     *
     * @return the list of visible sectors. The list will be empty if no sectors are visible.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    public List<Sector> refresh(DrawContext dc, double sectorSize)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sectorSize < Angle.SECOND.degrees || sectorSize > 180)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", sectorSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getVisibleSector() == null)
            return Collections.emptyList();

        this.sectors = new ArrayList<Sector>();
        this.sectorSize = sectorSize;
        this.swapCylinderLists(dc);
        this.tree.traverse(dc.getVisibleSector(), new Context(dc, sectorSize, this.sectors));

        Collections.sort(this.sectors);
        return this.sectors;
    }

    /**
     * Determines the visible sectors at a specified resolution within a specified sector.
     *
     * @param dc           the current draw context
     * @param sectorSize   the granularity of sector visibility, in degrees. All visible sectors of this size are found.
     *                     The value must be in the range, 1 second <= sectorSize <= 180 degrees.
     * @param searchSector the overall sector for which to determine visibility. May be null, in which case the current
     *                     visible sector of the draw context is used.
     *
     * @return the list of visible sectors. The list will be empty if no sectors are visible.
     *
     * @throws IllegalArgumentException if the draw context is null, the sector size is less than or equal to zero, or
     *                                  the search sector list is null.
     */
    public List<Sector> refresh(DrawContext dc, double sectorSize, Sector searchSector)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sectorSize < Angle.SECOND.degrees || sectorSize > 180)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", sectorSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (searchSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.sectors = new ArrayList<Sector>();
        this.sectorSize = sectorSize;
        this.swapCylinderLists(dc);
        this.tree.traverse(searchSector, new Context(dc, sectorSize, this.sectors));

        Collections.sort(this.sectors);
        return this.sectors;
    }

    /**
     * Determines the visible sectors at a specified resolution within a collection of sectors. This method can be used
     * to recursively determine visible sectors: the output of one invocation can be passed as an argument to the next
     * invocation.
     *
     * @param dc            the current draw context
     * @param sectorSize    the granularity of sector visibility, in degrees. All visible sectors of this size are The
     *                      value must be in the range, 1 second <= sectorSize <= 180 degrees. found.
     * @param searchSectors the sectors for which to determine visibility.
     *
     * @return the list of visible sectors. The list will be empty if no sectors are visible.
     *
     * @throws IllegalArgumentException if the draw context is null, the sector size is less than or equal to zero or
     *                                  the search sector list is null.
     */
    public List<Sector> refresh(DrawContext dc, double sectorSize, List<Sector> searchSectors)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sectorSize < Angle.SECOND.degrees || sectorSize > 180)
        {
            String message = Logging.getMessage("generic.SizeOutOfRange", sectorSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (searchSectors == null)
        {
            String message = Logging.getMessage("nullValue.SectorListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.swapCylinderLists(dc);
        this.sectors = new ArrayList<Sector>();
        this.sectorSize = sectorSize;
        for (Sector s : searchSectors)
        {
            this.tree.traverse(s, new Context(dc, sectorSize, this.sectors));
        }

        Collections.sort(this.sectors);
        return this.sectors;
    }

    protected void swapCylinderLists(DrawContext dc)
    {
        if (this.globeStateKey != null && !dc.getGlobe().getStateKey(dc).equals(this.globeStateKey))
            this.newExtents.clear();

        this.prevExtents.clear();
        HashMap<Sector, Extent> temp = this.prevExtents;
        this.prevExtents = newExtents;
        this.newExtents = temp;

        this.globeStateKey = dc.getGlobe().getStateKey(dc);
    }
}
