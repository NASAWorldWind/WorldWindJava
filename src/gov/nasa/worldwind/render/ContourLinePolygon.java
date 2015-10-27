/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Renders a contour line on the terrain at a given elevation. The controur line extent is bounded by a polygon defined
 * by a list of {@link LatLon}.
 *
 * @author Patrick Murris
 * @version $Id: ContourLinePolygon.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ContourLinePolygon extends ContourLine
{
    private ArrayList<? extends LatLon> positions;

    public ContourLinePolygon()
    {
        super();
    }

    public ContourLinePolygon(double elevation)
    {
        super(elevation);
    }

    public ContourLinePolygon(double elevation, ArrayList<? extends LatLon> positions)
    {
        super(elevation);
        this.setPositions(positions);
    }

    /**
     * Get the list of {@link LatLon} that describe the current bounding polygon.
     *
     * @return the list of {@link LatLon} that describe the current bounding polygon.
     */
    public List<? extends LatLon> getPositions()
    {
        return this.positions;
    }

    /**
     * Set the list of {@link LatLon} that describes a closed polygon - one which last position is equal to the first,
     * used to delineate the extent of the contour line.
     *
     * @param positions the list of {@link LatLon} that describe a closed polygon.
     *
     * @throws IllegalArgumentException if positions is <code>null</code>.
     */
    public void setPositions(ArrayList<? extends LatLon> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.positions = positions;
        this.setSector(Sector.boundingSector(positions));
        this.update();
    }

    /**
     * Filters the given intersection segments list according to some criteria - here the inclusion inside the current
     * polygon.
     *
     * @param dc   the current {@link DrawContext}
     * @param list the list of {@link Intersection} to be filtered.
     *
     * @return the filtered list.
     */
    protected ArrayList<Intersection> filterIntersections(DrawContext dc, ArrayList<Intersection> list)
    {
        // Filter against the bounding sector first
        list = super.filterIntersections(dc, list);

        // Filter the remaining segments against the polygon
        if (this.getPositions() == null)
            return list;

        Globe globe = dc.getGlobe();
        int i = 0;
        while (i < list.size())
        {
            if (WWMath.isLocationInside(globe.computePositionFromPoint(list.get(i).getIntersectionPoint()),
                this.positions) &&
                WWMath.isLocationInside(globe.computePositionFromPoint(list.get(i + 1).getIntersectionPoint()),
                    this.positions))
                // Keep segment
                i += 2;
            else
            {
                // Remove segment
                list.remove(i);
                list.remove(i);
            }
        }
        return list;
    }
}
