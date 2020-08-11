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

package gov.nasa.worldwindx.applications.glider;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Describes a region to highlight. Current highlight method is to draw the region's border in an application specified
 * color.
 *
 * @author tag
 * @version $Id: GliderRegionOfInterest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GliderRegionOfInterest extends AVListImpl
{
    public static final String GLIDER_REGION_OF_INTEREST = "gov.nasa.worldwind.glider.RegionOfInterest";

    private List<LatLon> locations;
    private Color color;

    /**
     * Create a region of interest and assign it a color.
     *
     * @param locations the lat/lon vertices of the region of interest.
     * @param color     the color in which to draw the region.
     *
     * @throws IllegalArgumentException if either argument is null.
     */
    public GliderRegionOfInterest(Iterable<? extends LatLon> locations, Color color)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.locations = new ArrayList<LatLon>();
        for (LatLon ll : locations)
        {
            this.locations.add(ll);
        }

        this.color = color;
    }

    /**
     * Copy constructor. Performs a shallow copy.
     *
     * @param region the region of interest to copy.
     *
     * @throws IllegalArgumentException if <code>region</code> is null.
     */
    public GliderRegionOfInterest(GliderRegionOfInterest region)
    {
        this(region.getLocations(), region.getColor());
    }

    /**
     * Returns the region's vertices.
     *
     * @return the lat/lon vertices of the region.
     */
    public List<LatLon> getLocations()
    {
        return this.locations;
    }

    /**
     * Set the region's location.
     *
     * @param locations the lat/lon vertices of the region.
     *
     * @throws IllegalArgumentException if <code>locations</code> is null.
     */
    public void setLocations(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GliderRegionOfInterest oldRegion = new GliderRegionOfInterest(this);
        this.locations = new ArrayList<LatLon>();
        for (LatLon ll : locations)
        {
            this.locations.add(ll);
        }
        this.firePropertyChange(GLIDER_REGION_OF_INTEREST, oldRegion, this);
    }

    /**
     * Return the region's color.
     *
     * @return the region's color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Set the region's color.
     *
     * @param color the color in which to draw the region.
     *
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GliderRegionOfInterest oldRegion = new GliderRegionOfInterest(this);
        this.color = color;
        this.firePropertyChange(GLIDER_REGION_OF_INTEREST, oldRegion, this);
    }

    @SuppressWarnings({"RedundantIfStatement"})
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GliderRegionOfInterest that = (GliderRegionOfInterest) o;

        if (color != null ? !color.equals(that.color) : that.color != null)
            return false;
        if (locations != null ? !locations.equals(that.locations) : that.locations != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (locations != null ? locations.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }

    /** A class to encapsulate an unmodifiable list of regions, useful when region lists are passed as properties. */
    public static class RegionSet
    {
        /** The unmodifiable list of regions. */
        public final Set<GliderRegionOfInterest> regions;

        /**
         * Create a region set
         *
         * @param regions the regions to include in the set.
         *
         * @throws IllegalArgumentException if <code>regions</code> is null.
         */
        public RegionSet(Set<GliderRegionOfInterest> regions)
        {
            if (regions == null)
            {
                String message = Logging.getMessage("nullValue.RegionListIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.regions = Collections.unmodifiableSet(regions);
        }
    }
}
