/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.cache.ShapeDataCache;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Represents the KML <i>Region</i> element and provides access to its contents. Regions define an area of interest
 * described by a geographic bounding box and an optional minimum and maximum altitude.
 * <p/>
 * <strong>Bounding Box</strong> </br> A Region's bounding box controls when the Region is active by defining a volume
 * that must intersect the viewing frustum. The bounding box is computed according to the <code>altitudeMode</code>
 * attribute of a Region's geographic <code>LatLonAltBox</code> as follows:
 * <p/>
 * <ul> <li><strong>clampToGround (default)</strong>: The bounding box encloses the terrain surface in the sector
 * defined by the north, south, east, and west limits of this Region's <code>LatLonAltBox</code>.</li>
 * <li><strong>relativeToGround</strong>: The bounding box encloses the volume in the sector defined by the north,
 * south, east, and west limits of the Region's <code>LatLonAltBox</code>, and who's upper and lower altitude are
 * specified by its minAltitude and maxAltitude, relative to ground level.</li> <li><strong>absolute</strong>: The
 * bounding box encloses the volume in the sector defined by the north, south, east, and west limits of the Region's
 * <code>LatLonAltBox</code>, and who's upper and lower altitude are specified by its minAltitude and maxAltitude,
 * relative to mean sea level.</li> </ul>
 * <p/>
 * <strong>Level of Detail</strong> <br/> A Region's level of detail determines when it is active by defining an upper
 * and lower boundary on the Region's screen area or the Region's distance to the view. The level of detail is computed
 * according to the <code>altitudeMode</code> attribute of a Region's geographic <code>LatLonAltBox</code> as follows:
 * <p/>
 * <ul> <li><strong>clampToGround (default)</strong>: The level of detail is determined by computing the distance from
 * the eye point and Region sector, scaling the distance by the <code>KMLTraversalContext's</code> detail hint, then
 * comparing that scaled distance to the Region's min and max pixel sizes in meters (the Region sector's area divided by
 * <code>minLodPixels</code> and <code>maxLodPixels</code>, respectively). The Region is active when the scaled distance
 * is less than or equal to the min pixel size in meters and greater than the max pixel size in meters. The detail hint
 * may be specified by calling <code>setDetailHint</code> on the top level <code>KMLRoot</code> (the KMLRoot loaded by
 * the application).</li> <li><strong>relativeToGround</strong>: The level of detail is determined by computing the
 * number of pixels the Region occupies on screen, and comparing that pixel count to the Region's
 * <code>minLodPixels</code> and <code>maxLodPixels</code>. The Region is active when the pixel count is greater or
 * equal to <code>minLodPixels</code> and less than <code>maxLodPixels</code>.</li> <li><strong>absolute</strong>: The
 * level of detail is determined by computing the number of pixels the Region occupies on screen, and comparing that
 * pixel count to the Region's <code>minLodPixels</code> and <code>maxLodPixels</code>. The Region is active when the
 * pixel count is greater or equal to <code>minLodPixels</code> and less than <code>maxLodPixels</code>.</li> </ul>
 * <p/>
 * In order to prevent Regions with adjacent level of detail ranges from activating at the same time, Region gives
 * priority to higher level of detail ranges. For example, suppose that two KML features representing different detail
 * levels of a Collada model have Regions with LOD range 100-200 and 200-300. Region avoids activating both features in
 * the event that both their level of detail criteria are met by giving priority to the second range: 200-300.
 * <p/>
 * <strong>KML Feature Hierarchies</strong> <br/> When a Region is attached to a KML feature, the feature and its
 * descendants are displayed only when the Region is active. A Region is active when its bounding box is in view and its
 * level of detail criteria are met. Region provides the <code>isActive</code> method for determining if a Region is
 * active for a specified <code>DrawContext</code>.
 * <p/>
 * Regions do not apply directly to KML containers, because a descendant feature can override the container's Region
 * with its own Region. If a feature does not specify a Region it inherits the Region of its nearest ancestor. Since a
 * child feature's Region may be larger or have a less restrictive level of detail range than its ancestor's Region, the
 * visibility of an entire KML feature tree cannot be determined based on a container's Region. Instead, visibility must
 * be determined at each leaf feature.
 * <p/>
 * <strong>Limitations</strong> <br/> The Region bounding box must lie between -90 to 90 degrees latitude, and -180 to
 * 180 degrees longitude. Regions that span the date line are currently not supported.
 *
 * @author tag
 * @version $Id: KMLRegion.java 2029 2014-05-23 21:22:23Z pabercrombie $
 */
public class KMLRegion extends KMLAbstractObject
{
    /**
     * The default time in milliseconds a <code>RegionData</code> element may exist in this Region's
     * <code>regionDataCache</code> before it must be regenerated: 6 seconds.
     */
    protected static final int DEFAULT_DATA_GENERATION_INTERVAL = 6000;
    /**
     * The default time in milliseconds a <code>RegionData</code> element may exist in this Region's
     * <code>regionDataCache</code> without being used before it is evicted: 1 minute.
     */
    protected static final int DEFAULT_UNUSED_DATA_LIFETIME = 60000;
    /**
     * The default value that configures KML scene resolution to screen resolution as the viewing distance changes:
     * 2.8.
     */
    protected static final double DEFAULT_DETAIL_HINT_ORIGIN = 2.8;

    /**
     * <code>RegionData</code> holds a Region's computed data used during a single call to <code>Region.isActive</code>,
     * and is unique to a particular <code>Globe</code>.
     * <p/>
     * RegionData entries are places in a Region's <code>regionDataCache</code>, and are retrieved during each call to
     * <code>isActive</code> using the current <code>Globe</code> as the cache key. RegionData's elements depend on the
     * <code>Globe's</code> <code>ElevationModel</code>, and therefore cannot be permanently cached. Each RegionData
     * entry is valid for a random amount of time between its <code>minExpiryTime</code> and its
     * <code>maxExpiryTime</code>, after which it must be regenerated. The time is randomized to amortize the cost of
     * regenerating data for multiple Regions over multiple frames.
     * <p/>
     * <strong>isActive</strong> <br/> RegionData's <code>isActive</code> property indicates whether the Region
     * associated with a RegionData entry is active. This is used to share the result of computing <code>isActive</code>
     * among multiple calls during the same frame. For example, the preRender and render passes need not each compute
     * <code>isActive</code>, and can therefore share the same computation by ensuring that this property is set at most
     * once per frame. Callers determine when to recompute <code>isActive</code> by comparing the
     * <code>DrawContext's</code> current frame number against the RegionData's <code>activeFrameNumber</code>. This
     * property is accessed by calling <code>isActive</code> and <code>setActive</code>.
     * <p/>
     * <strong>extent</strong> <br/> RegionData's <code>extent</code> property is an <code>Extent</code> used to
     * determine if a Region's bounding box is in view. This property is accessed by calling <code>getExtent</code> and
     * <code>setExtent</code>. May be <code>null</code>.
     * <p/>
     * <strong>sector</strong> <br/> RegionData's <code>sector</code> property is a <code>Sector</code> used to
     * determine if Regions with an <code>altitudeMode</code> of <code>clampToGround</code> are in view. Accessed by
     * calling <code>getSector</code> and <code>setSector</code>. When a Region's <code>altitudeMode</code> is
     * <code>clampToGround</code>, the Region's sector can be used to determine visibility because the Region is defined
     * to be on the <code>Globe's</code> surface.
     * <p/>
     * <strong>points</strong> <br/> RegionData's <code>points</code> property indicates a list of model-coordinate
     * points representing the corners and interior of the Region. These points are used to determine the distance
     * between the Region and the <code>View's</code> eye point. If the Region has altitude mode of
     * <code>clampToGround</code>, this list must contain five points: the model-coordinate points of the Region's four
     * corners and center point on the surface terrain.
     */
    protected static class RegionData extends ShapeDataCache.ShapeDataCacheEntry
    {
        /** Identifies the frame used to calculate this entry's values. Initially -1. */
        protected long frameNumber = -1;
        /** Identifies the frame used to determine if this entry's Region is active. Initially -1. */
        protected long activeFrameNumber = -1;
        /** Identifies whether this entry's Region is active. Initially <code>false</code>. */
        protected boolean isActive;
        /**
         * Indicates the vertical datum against which the altitudes values in this entry's Region are interpreted. One
         * of <code>WorldWind.ABSOLUTE</code>, <code>WorldWind.CLAMP_TO_GROUND</code>, or
         * <code>WorldWind.RELATIVE_TO_GROUND</code>. Initially -1.
         */
        protected int altitudeMode = -1;
        /**
         * Indicates the <code>Sector</code> used to determine if a Region who's <code>altitudeMode</code> is
         * <code>clampToGround</code> is visible. Initially <code>null</code>.
         */
        protected Sector sector;
        /**
         * Indicates the model-coordinate points representing the corners and interior of this entry's Region. These
         * points are used to determine the distance between this entry's Region and the <code>View's</code> eye point.
         * Initially <code>null</code>.
         */
        protected List<Vec4> points;

        /**
         * Constructs a new <code>RegionData</code> entry from the <code>Globe</code> and vertical exaggeration of a
         * specified draw context.
         *
         * @param dc            the draw context. Must contain a <code>Globe</code>.
         * @param minExpiryTime the minimum expiration duration, in milliseconds.
         * @param maxExpiryTime the maximum expiration duration, in milliseconds.
         */
        public RegionData(DrawContext dc, long minExpiryTime, long maxExpiryTime)
        {
            super(dc, minExpiryTime, maxExpiryTime);
        }

        /**
         * Identifies the frame used to calculate this entry's values.
         *
         * @return the frame used to calculate this entry's values.
         */
        public long getFrameNumber()
        {
            return frameNumber;
        }

        /**
         * Specifies the frame used to calculate this entry's values.
         *
         * @param frameNumber the frame used to calculate this entry's values.
         */
        public void setFrameNumber(long frameNumber)
        {
            this.frameNumber = frameNumber;
        }

        /**
         * Identifies the frame used to determine if this entry's Region is active.
         *
         * @return the frame used to determine if this entry's Region is active.
         */
        public long getActiveFrameNumber()
        {
            return activeFrameNumber;
        }

        /**
         * Specifies the frame used to determine if this entry's Region is active.
         *
         * @param frameNumber the frame used to determine if this entry's Region is active.
         */
        public void setActiveFrameNumber(long frameNumber)
        {
            this.activeFrameNumber = frameNumber;
        }

        /**
         * Indicates whether this entry's Region is active.
         *
         * @return <code>true</code> if this entry's Region is active, otherwise <code>false</code>.
         */
        public boolean isActive()
        {
            return this.isActive;
        }

        /**
         * Specifies whether this entry's Region is active.
         *
         * @param active <code>true</code> to specify that this entry's Region is active, otherwise <code>false</code>.
         */
        public void setActive(boolean active)
        {
            this.isActive = active;
        }

        /**
         * Indicates the vertical datum against which the altitudes values in this entry's Region are interpreted.
         *
         * @return the altitude mode of this entry's Region. One of <code>WorldWind.ABSOLUTE</code>,
         *         <code>WorldWind.CLAMP_TO_GROUND</code>, or <code>WorldWind.RELATIVE_TO_GROUND</code>.
         */
        public int getAltitudeMode()
        {
            return this.altitudeMode;
        }

        /**
         * Specifies the vertical datum against which the altitudes values in this entry's Region should be interpreted.
         * Must be one of <code>WorldWind.ABSOLUTE</code>, <code>WorldWind.CLAMP_TO_GROUND</code>, or
         * <code>WorldWind.RELATIVE_TO_GROUND</code>.
         *
         * @param altitudeMode the vertical datum to use.
         */
        public void setAltitudeMode(int altitudeMode)
        {
            this.altitudeMode = altitudeMode;
        }

        /**
         * Indicates the <code>Sector</code> used to determine if a Region who's <code>altitudeMode</code> is
         * <code>clampToGround</code> is visible. This returns <code>null</code> if this entry's Region's has no
         * geographic bounding box.
         *
         * @return the <code>Sector</code> used to determine if a Region is visible, or <code>null</code> to specify
         *         that this entry's Region has no bounding box.
         */
        public Sector getSector()
        {
            return this.sector;
        }

        /**
         * Specifies the <code>Sector</code> that defines a Region's surface sector on the <code>Globe</code>. Specify
         * <code>null</code> to indicate that this entry' Region has no geographic bounding box.
         *
         * @param sector the <code>Sector</code> that is used to determine if a <code>clampToGround</code> Region is
         *               visible, or <code>null</code> to specify that the entry's Region's has no bounding box.
         */
        public void setSector(Sector sector)
        {
            this.sector = sector;
        }

        /**
         * Indicates the model-coordinate points representing the corners and interior of this entry's Region. This
         * returns <code>null</code> if this entry's Region has no geographic bounding box.
         *
         * @return the points representing the corners and interior of this entry's Region, or <code>null</code> if the
         *         Region has no bounding box.
         */
        public List<Vec4> getPoints()
        {
            return this.points;
        }

        /**
         * Specifies the model-coordinate points representing the corners and interior of this entry's Region. These
         * points are used to determine the distance between this entry's Region and the <code>View's</code> eye point.
         * Specify <code>null</code> to indicate that this entry' Region has no geographic bounding box.
         * <p/>
         * If this entry's Region has altitude mode <code>clampToGround</code>, this list must contain five points: the
         * model-coordinate points of the Region's four corners and center point on the surface terrain.
         *
         * @param points the points representing the corners and interior of this entry's Region, or <code>null</code>
         *               to specify that this entry's Region has no bounding box.
         */
        public void setPoints(List<Vec4> points)
        {
            this.points = points;
        }
    }

    /**
     * The maximum lifespan of this Region's computed data, in milliseconds. Initialized to
     * <code>DEFAULT_DATA_GENERATION_INTERVAL</code>.
     */
    protected long maxExpiryTime = DEFAULT_DATA_GENERATION_INTERVAL;
    /**
     * The minimum lifespan of this Region's computed data, in milliseconds. Initialized to
     * <code>DEFAULT_DATA_GENERATION_INTERVAL - 1000</code>.
     */
    protected long minExpiryTime = Math.max(DEFAULT_DATA_GENERATION_INTERVAL - 1000, 0);
    /**
     * Holds globe-dependent computed Region data. One entry per globe encountered during <code>isActive</code>.
     * Initialized to a new <code>ShapeDataCache</code> with <code>maxTimeSinceLastUsed</code> set to
     * <code>DEFAULT_UNUSED_DATA_LIFETIME</code>.
     */
    protected ShapeDataCache regionDataCache = new ShapeDataCache(DEFAULT_UNUSED_DATA_LIFETIME);
    /**
     * Identifies the active globe-dependent data for the current invocation of <code>isActive</code>. The active data
     * is drawn from the <code>regionDataCache</code> at the beginning of the <code>isActive</code> method.
     */
    protected RegionData currentData;
    /**
     * The default value that configures KML scene resolution to screen resolution as the viewing distance changes. The
     * <code>KMLRoot's</code> detail hint specifies deviations from this default. Initially
     * <code>DEFAULT_DETAIL_HINT_ORIGIN</code>.
     */
    protected double detailHintOrigin = DEFAULT_DETAIL_HINT_ORIGIN;

    /**
     * Creates a new <code>KMLRegion</code> with the specified namespace URI, but otherwise does nothing. The new Region
     * has no bounding box and no level of detail range.
     *
     * @param namespaceURI the qualifying namespace URI. May be <code>null</code> to indicate no namespace
     *                     qualification.
     */
    public KMLRegion(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Indicates the bounding box that must be in view for this this Region to be considered active. May be
     * <code>null</code>.
     *
     * @return this Region's geographic bounding box, or <code>null</code> indicating that this Region has no bounding
     *         box restriction.
     */
    public KMLLatLonAltBox getLatLonAltBox()
    {
        return (KMLLatLonAltBox) this.getField("LatLonAltBox");
    }

    /**
     * Indicates the level of detail criteria that must be satisfied for this Region to be considered active. May be
     * <code>null</code>.
     *
     * @return this Region's level of detail range, or <code>null</code> indicating that this Region has no level of
     *         detail restriction.
     */
    public KMLLod getLod()
    {
        return (KMLLod) this.getField("Lod");
    }

    /**
     * Indicates whether this Region is active on the specified <code>DrawContext</code>. A Region is active if its
     * bounding box intersects the viewing frustum, and its level of detail criteria are met for the specified traversal
     * context and draw context.
     * <p/>
     * This always returns <code>true</code> if this Region has no bounding box, or if its bounding box is in the
     * viewing frustum and this Region has no lod criteria.
     *
     * @param tc the current KML traversal context.
     * @param dc the <code>DrawContext</code> used to determine whether this Region is active.
     *
     * @return <code>true</code> if this Region is active; otherwise <code>false</code>.
     *
     * @throws IllegalArgumentException if either the <code>KMLTraversalContext</code> or the <code>DrawContext</code>
     *                                  is <code>null</code>.
     */
    public boolean isActive(KMLTraversalContext tc, DrawContext dc)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeRegionData(dc);

        // Attempt to re-use the result of isActive during the preRender and render passes for the same Globe. These
        // calls use the same frustum to determine visibility and can therefore share the result of isActive. We
        // recompute isActive when the frame changes or when Globe changes, and return the computed value below.
        // Note that we use the same frustum intersection for both picking and rendering. We cannot cull against
        // the pick frustums because content (e.g. an open balloon) may extend beyond the region's bounding box.
        if (dc.getFrameTimeStamp() != this.getCurrentData().getActiveFrameNumber())
        {
            this.getCurrentData().setActive(this.isRegionActive(tc, dc));
            this.getCurrentData().setActiveFrameNumber(dc.getFrameTimeStamp());
        }

        return this.getCurrentData().isActive();
    }

    /**
     * Produces the data used to determine whether this Region is active for the specified <code>DrawContext</code>.
     * This attempts to re-use <code>RegionData</code> already been calculated this frame, or previously calculated
     * <code>RegionData</code> that is still valid and has not expired. This method is called by <code>isActive</code>
     * prior to determining if this Region is actually active.
     *
     * @param dc the current draw context.
     *
     * @see #isActive
     */
    protected void makeRegionData(DrawContext dc)
    {
        // Retrieve the cached data for the current globe. If it doesn't yet exist, create it. Most code subsequently
        // executed depends on currentData being non-null.
        this.currentData = (RegionData) this.regionDataCache.getEntry(dc.getGlobe());
        if (this.currentData == null)
        {
            this.currentData = this.createCacheEntry(dc);
            this.regionDataCache.addEntry(this.currentData);
        }

        // Re-use values already calculated this frame.
        if (dc.getFrameTimeStamp() != this.getCurrentData().getFrameNumber())
        {
            // Regenerate the region and data at a specified frequency.
            if (this.mustRegenerateData(dc))
            {
                this.doMakeRegionData(dc);
                this.getCurrentData().restartTimer(dc);
                this.getCurrentData().setGlobeStateKey(dc.getGlobe().getGlobeStateKey(dc));
                this.getCurrentData().setVerticalExaggeration(dc.getVerticalExaggeration());
            }

            this.getCurrentData().setFrameNumber(dc.getFrameTimeStamp());
        }
    }

    /**
     * Returns the data cache entry for the current invocation of <code>isActive</code>.
     *
     * @return the data cache entry for the current invocation of <code>isActive</code>.
     */
    protected RegionData getCurrentData()
    {
        return this.currentData;
    }

    /**
     * Creates and returns a new <code>RegionData</code> instance specific to this Region instance.
     *
     * @param dc the current draw context.
     *
     * @return data cache entry for the state in the specified draw context.
     */
    protected RegionData createCacheEntry(DrawContext dc)
    {
        return new RegionData(dc, this.minExpiryTime, this.maxExpiryTime);
    }

    /**
     * Indicates whether this Region's data must be recomputed, either as a result of a change in the
     * <code>Globe's</code> state or the expiration of the geometry regeneration interval.
     * <p/>
     * A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be current when this method is
     * called.
     *
     * @param dc the current draw context.
     *
     * @return <code>true</code> if this Region's data must be regenerated, otherwise <code>false</code>.
     */
    protected boolean mustRegenerateData(DrawContext dc)
    {
        return this.getCurrentData().isExpired(dc) || !this.getCurrentData().isValid(dc);
    }

    /**
     * Produces the data used to determine whether this Region is active for the specified <code>DrawContext</code>.
     * This method is called by <code>makeRegionData</code> upon determining that the current RegionData must be
     * recomputed, either as a result of a change in the <code>Globe's</code> state or the expiration of the geometry
     * regeneration interval. A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be current
     * when this method is called.
     *
     * @param dc the current draw context.
     *
     * @see #makeRegionData
     */
    protected void doMakeRegionData(DrawContext dc)
    {
        this.getCurrentData().setExtent(null);
        this.getCurrentData().setSector(null);
        this.getCurrentData().setPoints(null);

        KMLLatLonAltBox box = this.getLatLonAltBox();
        if (box == null)
            return;

        int altitudeMode = KMLUtil.convertAltitudeMode(box.getAltitudeMode(), WorldWind.CLAMP_TO_GROUND); // KML default
        this.getCurrentData().setAltitudeMode(altitudeMode);

        if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
        {
            this.doMakeClampToGroundRegionData(dc, box);
        }
        else if (altitudeMode == WorldWind.RELATIVE_TO_GROUND)
        {
            this.doMakeRelativeToGroundRegionData(dc, box);
        }
        else // Default to WorldWind.ABSOLUTE.
        {
            this.doMakeAbsoluteRegionData(dc, box);
        }
    }

    /**
     * Produces the <code>Extent</code> and the <code>Sector</code> for this Region. Assumes this region's altitude mode
     * is <code>clampToGround</code>. A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be
     * current when this method is called.
     *
     * @param dc  the current draw context.
     * @param box the Region's geographic bounding box.
     */
    protected void doMakeClampToGroundRegionData(DrawContext dc, KMLLatLonAltBox box)
    {
        Sector sector = KMLUtil.createSectorFromLatLonBox(box);
        if (sector == null)
            return;

        // TODO: Regions outside of the normal lat/lon bounds ([-90, 90], [-180, 180]) are not supported. Remove
        // TODO: this warning when such regions are supported. See WWJINT-482.
        if (!this.isSectorSupported(sector))
        {
            String message = Logging.getMessage("KML.UnsupportedRegion", sector);
            Logging.logger().warning(message);
            return;
        }

        double[] extremeElevations = dc.getGlobe().getMinAndMaxElevations(sector);
        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), sector,
            extremeElevations[0], extremeElevations[1]);
        this.getCurrentData().setExtent(extent);
        this.getCurrentData().setSector(sector);

        // Cache the model-coordinate points of the Region's four corners and center point on the surface terrain. It is
        // safe to cache this value since these points are regenerated every 5-6 seconds along with the RegionData's
        // extent and sector. Caching these points rather than computing them every frame reduces the average time of
        // Region.isActive by 50%.
        Vec4[] corners = sector.computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration());
        Vec4 centerPoint = sector.computeCenterPoint(dc.getGlobe(), dc.getVerticalExaggeration());
        this.getCurrentData().setPoints(Arrays.asList(corners[0], corners[1], corners[2], corners[3], centerPoint));
    }

    /**
     * Produces the <code>Extent</code> and the <code>Sector</code> for this Region. Assumes this region's altitude mode
     * is <code>relativeToGround</code>. A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be
     * current when this method is called.
     *
     * @param dc  the current draw context.
     * @param box the Region's geographic bounding box.
     */
    protected void doMakeRelativeToGroundRegionData(DrawContext dc, KMLLatLonAltBox box)
    {
        Sector sector = KMLUtil.createSectorFromLatLonBox(box);
        if (sector == null)
            return;

        if (!this.isSectorSupported(sector))
        {
            String message = Logging.getMessage("KML.UnsupportedRegion", sector);
            Logging.logger().warning(message);
            return;
        }

        Double minAltitude = box.getMinAltitude();
        if (minAltitude == null)
            minAltitude = 0d; // The default minAltitude is zero.

        Double maxAltitude = box.getMaxAltitude();
        if (maxAltitude == null)
            maxAltitude = 0d; // The default maxAltitude is zero.

        double[] extremeElevations = dc.getGlobe().getMinAndMaxElevations(sector);
        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), sector,
            extremeElevations[0] + minAltitude, extremeElevations[1] + maxAltitude);
        this.getCurrentData().setExtent(extent);
        this.getCurrentData().setSector(sector);
    }

    /**
     * Produces the <code>Extent</code> and the <code>Sector</code> for this Region. Assumes this region's altitude mode
     * is <code>absolute</code>. A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be current
     * when this method is called.
     *
     * @param dc  the current draw context.
     * @param box the Region's geographic bounding box.
     */
    protected void doMakeAbsoluteRegionData(DrawContext dc, KMLLatLonAltBox box)
    {
        Sector sector = KMLUtil.createSectorFromLatLonBox(box);
        if (sector == null)
            return;

        if (!this.isSectorSupported(sector))
        {
            String message = Logging.getMessage("KML.UnsupportedRegion", sector);
            Logging.logger().warning(message);
            return;
        }

        Double minAltitude = box.getMinAltitude();
        if (minAltitude == null)
            minAltitude = 0d; // The default minAltitude is zero.

        Double maxAltitude = box.getMaxAltitude();
        if (maxAltitude == null)
            maxAltitude = 0d; // The default maxAltitude is zero.

        Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), sector,
            minAltitude, maxAltitude);
        this.getCurrentData().setExtent(extent);
        this.getCurrentData().setSector(sector);
    }

    /**
     * Determines if a Sector is supported by this Region implementation. This implementation does not support sectors
     * with latitude values outside of [-90, 90], or longitude values outside of [-180, 180].
     *
     * @param sector Sector to test.
     *
     * @return {@code true} if {@code sector} is with [-90, 90] latitude and [-180, 180] longitude.
     */
    protected boolean isSectorSupported(Sector sector)
    {
        return sector.isWithinLatLonLimits();
    }

    /**
     * Indicates whether this Region is active on the specified <code>DrawContext</code>. A Region is active if its
     * bounding box intersects the viewing frustum, and its level of detail criteria are met for the specified traversal
     * context and draw context. This is called by <code>isActive</code> and its return value may be cached during a
     * single frame.
     *
     * @param tc the current KML traversal context.
     * @param dc the <code>DrawContext</code> used to determine whether this Region is active.
     *
     * @return <code>true</code> if this Region is active; otherwise <code>false</code>.
     */
    protected boolean isRegionActive(KMLTraversalContext tc, DrawContext dc)
    {
        return this.isRegionVisible(dc) && this.meetsLodCriteria(tc, dc);
    }

    /**
     * Indicates whether this Region is visible for the specified <code>DrawContext</code>. If this Region's
     * <code>altitudeMode</code> is <code>clampToGround</code>, this Region is considered visible if its sector
     * intersects the <code>DrawContext's</code> visible sector and its frustum intersects the
     * <code>DrawContext's</code> viewing frustum. Otherwise, this Region is considered visible its frustum intersects
     * the <code>DrawContext's</code> viewing frustum.
     *
     * @param dc the <code>DrawContext</code> used to test this Region for visibility.
     *
     * @return <code>true</code> if this Region is visible, otherwise <code>false</code>.
     */
    protected boolean isRegionVisible(DrawContext dc)
    {
        // If this Region's altitude mode is clampToGround and it has a non-null sector, compare its sector against the
        // DrawContext's visible sector to determine if the Region is visible. In this case the sector can be used to
        // determine visibility because the Region is defined to be on the Globe's surface.
        if (this.getCurrentData().getAltitudeMode() == WorldWind.CLAMP_TO_GROUND
            && dc.getVisibleSector() != null && this.getCurrentData().getSector() != null
            && !dc.getVisibleSector().intersects(this.getCurrentData().getSector()))
        {
            return false;
        }

        // Treat this Region as not visible if its extent occupies less than one pixel on screen. Features that exceed
        // the Region's boundary - such as a ScreenOverlay - are not displayed when the Region is insignificantly
        // visible. Though it may be the intent of a KML document's author to display such a feature whenever the Region
        // is in the frustum, we treat the Region just as though its outside of the frustum and prevent the display of
        // any features associated with it.
        //noinspection SimplifiableIfStatement
        if (this.getCurrentData().getExtent() != null && dc.isSmall(this.getCurrentData().getExtent(), 1))
            return false;

        return this.intersectsFrustum(dc);
    }

    /**
     * Indicates whether this Region intersects the viewing frustum for the specified <code>DrawContext</code>. A
     * <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be current when this method is called.
     * <p/>
     * This returns <code>true</code> if this Region has no bounding box, or if its bounding box cannot be computed for
     * any reason.
     *
     * @param dc the <code>DrawContext</code> who's frustum is tested against this Region's bounding box.
     *
     * @return <code>true</code> if this Region's bounding box intersects the <code>DrawContext's</code> frustum,
     *         otherwise <code>false</code>.
     */
    protected boolean intersectsFrustum(DrawContext dc)
    {
        Extent extent = this.getCurrentData().getExtent();
        //noinspection SimplifiableIfStatement
        if (extent == null)
            return true; // We do not know the visibility; assume it intersects the frustum.

        // Test against the view frustum even in picking mode. We cannot cull against the pick frustums because visible
        // content (e.g. an open balloon) may extend beyond the region's bounding box.
        return dc.getView().getFrustumInModelCoordinates().intersects(extent);
    }

    /**
     * Indicates whether the specified <code>DrawContext</code> meets this Region's level of detail criteria. A
     * <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be current when this method is called.
     * <p/>
     * This returns <code>true</code> if this Region has no level of criteria, or if its level of detail cannot be
     * compared against the bounding box for any reason.
     *
     * @param tc the current KML traversal context.
     * @param dc the <code>DrawContext</code> to test.
     *
     * @return <code>true</code> if the <code>DrawContext's</code> meets this Region's level of detail criteria,
     *         otherwise <code>false</code>.
     */
    protected boolean meetsLodCriteria(KMLTraversalContext tc, DrawContext dc)
    {
        KMLLod lod = this.getLod();
        if (lod == null)
            return true; // No level of detail specified; assume the DrawContext meets the level of detail criteria.

        if ((lod.getMinLodPixels() == null || lod.getMinLodPixels() <= 0d)
            && (lod.getMaxLodPixels() == null || lod.getMaxLodPixels() < 0d))
            return true; // The level of detail range is infinite, so this Region always meets the lod criteria.

        if (lod.getMaxLodPixels() != null && lod.getMaxLodPixels() == 0d)
            return false; // The maximum number of pixels is zero, so this Region never meets the lod criteria.

        int altitudeMode = this.getCurrentData().getAltitudeMode();

        if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
        {
            return this.meetsClampToGroundLodCriteria(tc, dc, lod);
        }
        else if (altitudeMode == WorldWind.RELATIVE_TO_GROUND)
        {
            return this.meetsRelativeToGroundLodCriteria(tc, dc, lod);
        }
        else // Default to WorldWind.ABSOLUTE.
        {
            return this.meetsAbsoluteLodCriteria(tc, dc, lod);
        }
    }

    /**
     * Indicates whether the specified <code>DrawContext</code> meets this Region's level of detail criteria. Assumes
     * this region's altitude mode is <code>clampToGround</code>. A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code>
     * must be current when this method is called.
     *
     * @param tc  the current KML traversal context.
     * @param dc  the <code>DrawContext</code> to test.
     * @param lod the level of detail criteria that must be met.
     *
     * @return <code>true</code> if the <code>DrawContext's</code> meets this Region's level of detail criteria,
     *         otherwise <code>false</code>.
     */
    protected boolean meetsClampToGroundLodCriteria(KMLTraversalContext tc, DrawContext dc, KMLLod lod)
    {
        // Neither the OGC KML specification nor the Google KML reference specify how to compute a clampToGround
        // Region's projected screen area. However, the Google Earth outreach tutorials, and an official post from a
        // Google engineer on the Google forums both indicate that clampToGround Regions are represented by a flat
        // rectangle on the terrain surface:
        // KML Specification version 2.2, section 6.3.4.
        // http://groups.google.com/group/kml-support-getting-started/browse_thread/thread/bbba32541bace3cc/df4e1dc64a3018d4?lnk=gst#df4e1dc64a3018d4
        // http://earth.google.com/outreach/tutorial_region.html

        Sector sector = this.getCurrentData().getSector();
        List<Vec4> points = this.getCurrentData().getPoints();
        if (sector == null || points == null || points.size() != 5)
            return true; // Assume the criteria is met if we don't know this Region's sector or its surface points.

        // Get the eye distance for each of the sector's corners and its center.
        View view = dc.getView();
        double d1 = view.getEyePoint().distanceTo3(points.get(0));
        double d2 = view.getEyePoint().distanceTo3(points.get(1));
        double d3 = view.getEyePoint().distanceTo3(points.get(2));
        double d4 = view.getEyePoint().distanceTo3(points.get(3));
        double d5 = view.getEyePoint().distanceTo3(points.get(4));

        // Find the minimum eye distance. Compute the sector's size in meters by taking the square root of the sector's
        // area in radians, and multiplying that by the globe's radius at the nearest corner. We take the square root
        // of the area in radians to match the units of this Region's minLodPixels and maxLodPixels, which are the
        // square root of a screen area.
        double minDistance = d1;
        double numRadians = Math.sqrt(sector.getDeltaLatRadians() * sector.getDeltaLonRadians());
        double numMeters = points.get(0).getLength3() * numRadians;

        if (d2 < minDistance)
        {
            minDistance = d2;
            numMeters = points.get(1).getLength3() * numRadians;
        }
        if (d3 < minDistance)
        {
            minDistance = d3;
            numMeters = points.get(2).getLength3() * numRadians;
        }
        if (d4 < minDistance)
        {
            minDistance = d4;
            numMeters = points.get(3).getLength3() * numRadians;
        }
        if (d5 < minDistance)
        {
            minDistance = d5;
            numMeters = points.get(4).getLength3() * numRadians;
        }

        // Compare the scaled distance to the minimum and maximum pixel size in meters, according to the sector's size
        // and this Region's minLodPixels and maxLodPixels. This Region's level of detail criteria are met when the
        // scaled distance is less than or equal to the minimum pixel size, and greater than the maximum pixel size.
        // Said another way, this Region is used when a pixel in the Region's sector is close enough to meet the minimum
        // pixel size criteria, yet far enough away not to exceed the maximum pixel size criteria.

        // NOTE: It's tempting to instead compare a screen pixel count to the minLodPixels and maxLodPixels, but that
        // calculation is window-size dependent and results in activating an excessive number of Regions when a KML
        // super overlay is displayed, especially if the window size is large.

        Double lodMinPixels = lod.getMinLodPixels();
        Double lodMaxPixels = lod.getMaxLodPixels();
        double distanceFactor = minDistance * Math.pow(10, -this.getDetailFactor(tc));

        // We ignore minLodPixels if it's unspecified, zero, or less than zero. We ignore maxLodPixels if it's
        // unspecified or less than 0 (infinity). In these cases any distance passes the test against minLodPixels or
        // maxLodPixels.
        return (lodMinPixels == null || lodMinPixels <= 0d || (numMeters / lodMinPixels) >= distanceFactor)
            && (lodMaxPixels == null || lodMaxPixels < 0d || (numMeters / lodMaxPixels) < distanceFactor);
    }

    /**
     * Indicates whether the specified <code>DrawContext</code> meets this Region's level of detail criteria. Assumes
     * this region's altitude mode is <code>relativeToGround</code>. A <code>{@link
     * gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code> must be current when this method is called.
     *
     * @param tc  the current KML traversal context.
     * @param dc  the <code>DrawContext</code> to test.
     * @param lod the level of detail criteria that must be met.
     *
     * @return <code>true</code> if the <code>DrawContext's</code> meets this Region's level of detail criteria,
     *         otherwise <code>false</code>.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean meetsRelativeToGroundLodCriteria(KMLTraversalContext tc, DrawContext dc, KMLLod lod)
    {
        return this.meetsScreenAreaCriteria(dc, lod);
    }

    /**
     * Indicates whether the specified <code>DrawContext</code> meets this Region's level of detail criteria. Assumes
     * this region's altitude mode is <code>absolute</code>. A <code>{@link gov.nasa.worldwind.ogc.kml.KMLRegion.RegionData}</code>
     * must be current when this method is called.
     *
     * @param tc  the current KML traversal context.
     * @param dc  the <code>DrawContext</code> to test.
     * @param lod the level of detail criteria that must be met.
     *
     * @return <code>true</code> if the <code>DrawContext's</code> meets this Region's level of detail criteria,
     *         otherwise <code>false</code>.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected boolean meetsAbsoluteLodCriteria(KMLTraversalContext tc, DrawContext dc, KMLLod lod)
    {
        return this.meetsScreenAreaCriteria(dc, lod);
    }

    /**
     * Indicates whether this Region's projected screen area on the specified <code>DrawContext</code> is in the range
     * specified by <code>lod</code>.
     *
     * @param dc  the <code>DrawContext</code> to test.
     * @param lod the level of detail criteria that must be met.
     *
     * @return <code>true</code> if this Region's screen area meets the level of detail criteria, otherwise
     *         <code>false</code>.
     */
    protected boolean meetsScreenAreaCriteria(DrawContext dc, KMLLod lod)
    {
        // The DrawContext does not meet this region's minLodPixels criteria if minLodPixels is specified and this
        // region's projected screen pixel count is less than minLodPixels.

        // The DrawContext does not meet this region's maxLodPixels criteria if maxLodPixels is specified, is not
        // negative, and this region's projected screen pixel count is greater than or equal to maxLodPixels. If
        // maxLodPixels is negative, this indicates that maxLodPixels is positive infinity and therefore accepts any
        // value.

        Extent extent = this.getCurrentData().getExtent();
        if (extent == null)
            return true; // Assume the criteria is met if we don't know this Region's extent.

        // Compute the projected screen area of this Region's extent in square pixels in the DrawContext's View.
        // According to the KML specification, we take the square root of this value to get a value that is comparable
        // against minLodPixels and maxLodPixels. The projected area is positive infinity if the view's eye point is
        // inside the extent, or if part of the extent is behind the eye point. In either case we do not take the square
        // root, and leave the value as positive infinity.
        double numPixels = extent.getProjectedArea(dc.getView());
        if (numPixels != Double.POSITIVE_INFINITY)
            numPixels = Math.sqrt(numPixels);

        // This Region's level of detail criteria are met if the number of pixels is greater than or equal to
        // minLodPixels and less than maxLodPixels. We ignore minLodPixels if it's unspecified, zero, or less than zero.
        // We ignore maxLodPixels if it's unspecified or less than 0 (infinity). In these cases any distance passes the
        // test against minLodPixels or maxLodPixels.

        Double lodMinPixels = lod.getMinLodPixels();
        Double lodMaxPixels = lod.getMaxLodPixels();

        return (lodMinPixels == null || lodMinPixels <= 0d || lodMinPixels <= numPixels)
            && (lodMaxPixels == null || lodMaxPixels < 0d || lodMaxPixels > numPixels);
    }

    /**
     * Indicates the detail factor that configures KML scene resolution to screen resolution as the viewing distance
     * changes. This returns the Region's <code>detailHintOrigin</code> plus the <code>KMLTraversalContext's</code>
     * detail hint.
     *
     * @param tc the KML traversal context that specifies the detail hint.
     *
     * @return this Region's <code>detailHintOrigin</code> plus the traversal context's <code>detailHintOrigin</code>.
     */
    protected double getDetailFactor(KMLTraversalContext tc)
    {
        return this.detailHintOrigin + tc.getDetailHint();
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLRegion))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        this.reset();

        super.applyChange(sourceValues);
    }

    @Override
    public void onChange(Message msg)
    {
        if (KMLAbstractObject.MSG_BOX_CHANGED.equals(msg.getName()))
            this.reset();

        super.onChange(msg);
    }

    protected void reset()
    {
        this.regionDataCache.removeAllEntries();
        this.currentData = null;
    }
}
