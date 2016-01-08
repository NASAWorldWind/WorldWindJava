/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.TimedExpirySupport;

import java.util.*;

/**
 * Provides a mechanism to manage globe-specific representations of shapes. Typically used to manage per-globe state
 * when the application associates the same shape with multiple {@link gov.nasa.worldwind.WorldWindow}s.
 * <p/>
 * This cache limits the amount of time an entry remains in the cache unused. The maximum unused time may be specified.
 * Entries unused within the specified duration are removed from the cache each time {@link
 * #getEntry(gov.nasa.worldwind.globes.Globe)} is called.
 *
 * @author tag
 * @version $Id: ShapeDataCache.java 2152 2014-07-16 00:00:33Z tgaskins $
 */
public class ShapeDataCache implements Iterable<ShapeDataCache.ShapeDataCacheEntry>
{
    public static class ShapeDataCacheEntry extends AVListImpl
    {
        /** Determines whether the cache entry has expired. */
        protected TimedExpirySupport timer;
        /** Indicates the last time, in milliseconds, the entry was requested or added. */
        protected long lastUsed = System.currentTimeMillis();
        /** Identifies the associated globe's state at the time the entry was created. */
        protected GlobeStateKey globeStateKey;
        /** Indicates the vertical exaggeration in effect when the entry was cached. */
        protected double verticalExaggeration;
        /** Indicates the associated shape's extent, in model coordinates relative to the associated globe. */
        protected Extent extent;
        /** Indicates the eye distance of the shape in the globe-relative coordinate system. */
        protected double eyeDistance;
        /** Indicates the eye distance current when the cache entry's remaining time was last adjusted. */
        protected double timerAdjustedEyeDistance;

        /**
         * Constructs an entry using the globe and vertical exaggeration of a specified draw context.
         *
         * @param dc            the draw context. Must contain a globe.
         * @param minExpiryTime the minimum expiration duration, in milliseconds.
         * @param maxExpiryTime the maximum expiration duration, in milliseconds.
         *
         * @throws IllegalArgumentException if the draw context is null.
         */
        public ShapeDataCacheEntry(DrawContext dc, long minExpiryTime, long maxExpiryTime)
        {
            this.timer = new TimedExpirySupport(Math.max(minExpiryTime, 0), Math.max(maxExpiryTime, 0));
            this.globeStateKey = dc != null ? dc.getGlobe().getGlobeStateKey() : null;
            this.verticalExaggeration = dc != null ? dc.getVerticalExaggeration() : 1d;
        }

        /**
         * Indicates whether this shape data's globe state and vertical exaggeration are the same as that in the current
         * draw context.
         *
         * @param dc the current draw context.
         *
         * @return true if the shape is valid, otherwise false.
         */
        public boolean isValid(DrawContext dc)
        {
            return this.verticalExaggeration == dc.getVerticalExaggeration()
                && (this.globeStateKey != null && globeStateKey.equals(dc.getGlobe().getGlobeStateKey(dc)));
        }

        /**
         * Indicates whether this entry has expired.
         *
         * @param dc the current draw context.
         *
         * @return true if the entry has expired, otherwise false.
         */
        public boolean isExpired(DrawContext dc)
        {
            return dc != null ? timer.isExpired(dc) : timer.isExpired(System.currentTimeMillis());
        }

        /**
         * Sets this entry's expiration state.
         *
         * @param isExpired true to expire the entry, otherwise false.
         */
        public void setExpired(boolean isExpired)
        {
            this.timer.setExpired(isExpired);
        }

        /**
         * Resets the timer to the current time.
         *
         * @param dc the current draw context.
         *
         * @throws IllegalArgumentException if the draw context is null.
         */
        public void restartTimer(DrawContext dc)
        {
            this.timer.restart(dc);
        }

        /**
         * Adjust the timer's expiration time by comparing the cached eye distance to the current eye distance. The
         * remaining expiration time is reduced by 50% each time the eye distance decreases by 50%. This method may be
         * called many times, and the remaining expiration time is reduced only after the eye distance is reduced by
         * 50%. This has no effect if the cached eye distance is unknown, or if the expiration time has already been
         * reached.
         *
         * @param dc             the current draw context.
         * @param newEyeDistance the current eye distance.
         */
        public void adjustTimer(DrawContext dc, double newEyeDistance)
        {
            if (this.timerAdjustedEyeDistance == 0) // do nothing, there's previous eye distance to compare with
                return;

            if (this.timer.isExpired(dc)) // do nothing, the timer has already expired
                return;

            double oldPixelSize = dc.getView().computePixelSizeAtDistance(this.timerAdjustedEyeDistance);
            double newPixelSize = dc.getView().computePixelSizeAtDistance(newEyeDistance);
            if (newPixelSize < oldPixelSize / 2)
            {
                long remainingTime = this.timer.getExpiryTime() - dc.getFrameTimeStamp();
                this.timer.setExpiryTime(dc.getFrameTimeStamp() + remainingTime / 2);
                this.timerAdjustedEyeDistance = newEyeDistance;
            }
        }

        /**
         * Indicates this entry's eye distance.
         *
         * @return this entry's eye distance, in meters.
         */
        public double getEyeDistance()
        {
            return eyeDistance;
        }

        /**
         * Specifies this entry's eye distance.
         *
         * @param eyeDistance the eye distance, in meters.
         */
        public void setEyeDistance(double eyeDistance)
        {
            this.eyeDistance = eyeDistance;
            this.timerAdjustedEyeDistance = eyeDistance; // reset the eye distance used by adjustTimer
        }

        /**
         * Returns this entry's extent.
         *
         * @return this entry's extent.
         */
        public Extent getExtent()
        {
            return this.extent;
        }

        /**
         * Specifies this entry's extent.
         *
         * @param extent the new extent. May be null.
         */
        public void setExtent(Extent extent)
        {
            this.extent = extent;
        }

        /**
         * Returns this entry's expiration timer.
         *
         * @return this entry's expiration timer.
         */
        public TimedExpirySupport getTimer()
        {
            return timer;
        }

        /**
         * Specifies this entry's expiration timer.
         *
         * @param timer the new expiration timer.
         */
        public void setTimer(TimedExpirySupport timer)
        {
            this.timer = timer;
        }

        /**
         * Indicates this entry's globe state key, captured when this entry was constructed or when explicitly set.
         *
         * @return this entry's globe state key.
         */
        public GlobeStateKey getGlobeStateKey()
        {
            return globeStateKey;
        }

        /**
         * Specifies this entry's globe state key.
         *
         * @param globeStateKey the new globe state key.
         */
        public void setGlobeStateKey(GlobeStateKey globeStateKey)
        {
            this.globeStateKey = globeStateKey;
        }

        /**
         * Indicates this entry's vertical exaggeration, captured when the entry was constructed or when explicitly
         * set.
         *
         * @return this entry's vertical exaggeration.
         */
        public double getVerticalExaggeration()
        {
            return verticalExaggeration;
        }

        public void setVerticalExaggeration(double verticalExaggeration)
        {
            this.verticalExaggeration = verticalExaggeration;
        }
    }

    // usually only 1, but few at most
    /** This cache's map of entries. Typically one entry per open window. */
    protected HashMap<GlobeStateKey, ShapeDataCacheEntry> entries = new HashMap<GlobeStateKey, ShapeDataCacheEntry>(1);
    /** The maximum number of milliseconds an entry may remain in the cache without being used. */
    protected long maxTimeSinceLastUsed;

    /**
     * Construct a cache with a specified entry lifetime.
     *
     * @param maxTimeSinceLastUsed the maximum number of milliseconds an entry may remain in the cache without being
     *                             used.
     */
    public ShapeDataCache(long maxTimeSinceLastUsed)
    {
        this.maxTimeSinceLastUsed = maxTimeSinceLastUsed;
    }

    public Iterator<ShapeDataCacheEntry> iterator()
    {
        return this.entries.values().iterator();
    }

    /**
     * Adds a specified entry to the cache or replaces an entry associated with the same globe.
     *
     * @param entry the entry to add. If null, the cache remains unchanged.
     */
    public void addEntry(ShapeDataCacheEntry entry)
    {
        if (entry == null)
            return;

        this.entries.put(entry.globeStateKey, entry);
        entry.lastUsed = System.currentTimeMillis();
    }

    /**
     * Retrieves a specified entry from the cache.
     * <p/>
     * Note: Each time this method is called the cache is cleared of dead entries, as defined by their last-used time
     * relative to this cache's maximum unused time.
     *
     * @param globe the globe the entry is associated with.
     *
     * @return the entry if it exists, otherwise null.
     */
    public ShapeDataCacheEntry getEntry(Globe globe)
    {
        long now = System.currentTimeMillis();
//        this.removeDeadEntries(now);

        if (globe == null)
            return null;

        ShapeDataCacheEntry entry = this.entries.get(globe.getGlobeStateKey());
        if (entry != null)
            entry.lastUsed = now;

        return entry;
    }

    /**
     * Set all entries in this cache to a specified expiration state.
     *
     * @param isExpired the expiration state.
     */
    public void setAllExpired(boolean isExpired)
    {
        for (ShapeDataCacheEntry entry : this.entries.values())
        {
            entry.setExpired(isExpired);
        }
    }

    /** Set to null the extent field of all entries in this cache. */
    public void clearExtents()
    {
        for (ShapeDataCacheEntry entry : this.entries.values())
        {
            entry.setExtent(null);
        }
    }

    /** Remove all entries from this cache. */
    public void removeAllEntries()
    {
        this.entries.clear();
    }
//
//    /**
//     * Remove entries from the cache that have not been used within this cache's maximum unused time.
//     *
//     * @param now the time to compare with entries' last-used time.
//     */
//    protected void removeDeadEntries(long now)
//    {
//        List<Globe> deadEntries = new ArrayList<Globe>();
//
//        for (Map.Entry<Globe, ShapeDataCacheEntry> mapEntry : this.entries.entrySet())
//        {
//            if (mapEntry.getValue().lastUsed + this.maxTimeSinceLastUsed < now)
//                deadEntries.add(mapEntry.getKey());
//        }
//
//        for (Globe key : deadEntries)
//        {
//            this.entries.remove(key);
//        }
//    }
}
