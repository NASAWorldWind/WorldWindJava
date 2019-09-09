/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * LabeledPath draws a {@link gov.nasa.worldwind.render.Annotation} on a specified path. The path itself is not drawn.
 * Instead, the annotation is drawn at the location that maximizes the annotation's visible area in the viewport. The
 * annotation is not drawn if the location list is {@code null}, or if no location in the list is visible.
 * <p>
 * The caller must specify the screen annotation used to draw the path's label by calling {@link
 * #setAnnotation(gov.nasa.worldwind.render.ScreenAnnotation)}. The path sets the specified annotation's screen point to
 * control the label's location, but otherwise does not modify the annotation.
 *
 * @author dcollins
 * @version $Id: LabeledPath.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LabeledPath implements Renderable
{
    /** The labeled path's locations as specified by the application. {@code null} if no locations have been specified. */
    protected Iterable<? extends LatLon> locations;
    /**
     * The altitude mode that defines how to interpret the altitude of locations that have an altitude component.
     * Defaults to {@link gov.nasa.worldwind.WorldWind#ABSOLUTE}.
     */
    protected int altitudeMode = WorldWind.ABSOLUTE;
    /** The screen annotation to use as a label. {@code null} if no annotation has been specified. */
    protected ScreenAnnotation annotation;
    /** The frame number used to place the label. */
    protected long frameNumber = -1;
    /** The index of the label's location in {@link #locations}, or -1 if the path has no label location. */
    protected int labelLocationIndex = -1;

    /** Creates a labeled path with no locations and no label annotation. */
    public LabeledPath()
    {
    }

    /**
     * Creates a labeled path with specified locations.
     *
     * @param locations the labeled path's locations.
     *
     * @throws IllegalArgumentException if locations is {@code null}.
     */
    public LabeledPath(Iterable<? extends LatLon> locations)
    {
        this.setLocations(locations);
    }

    /**
     * Creates a labeled path with the specified label annotation and no locations.
     *
     * @param annotation the screen annotation to use for drawing the label, or {@code null} if no label should be
     *                   drawn.
     */
    public LabeledPath(ScreenAnnotation annotation)
    {
        this.setAnnotation(annotation);
    }

    /**
     * Creates a labeled path with the specified label annotation and locations.
     *
     * @param locations  the labeled path's locations.
     * @param annotation the screen annotation to use for drawing the label, or {@code null} if no label should be
     *                   drawn.
     *
     * @throws IllegalArgumentException if locations is {@code null}.
     */
    public LabeledPath(Iterable<? extends LatLon> locations, ScreenAnnotation annotation)
    {
        this.setLocations(locations);
        this.setAnnotation(annotation);
    }

    /**
     * Returns the labeled path's locations.
     *
     * @return the labeled path's locations. Will be {@code null} if no locations have been specified.
     */
    public Iterable<? extends LatLon> getLocations()
    {
        return locations;
    }

    /**
     * Specifies the labeled path's locations, which replace the path's current locations, if any.
     *
     * @param locations the labeled path's locations.
     *
     * @throws IllegalArgumentException if locations is {@code null}.
     */
    public void setLocations(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.locations = locations;
        this.reset();
    }

    /**
     * Returns the labeled path's altitude mode.
     *
     * @return the labeled path's altitude mode.
     *
     * @see #setAltitudeMode(int)
     */
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    /**
     * Specifies the labeled path's altitude mode, one of {@link gov.nasa.worldwind.WorldWind#ABSOLUTE}, {@link
     * gov.nasa.worldwind.WorldWind#RELATIVE_TO_GROUND} or {@link gov.nasa.worldwind.WorldWind#CLAMP_TO_GROUND}.
     * <p>
     * Note: If the altitude mode is unrecognized, {@link gov.nasa.worldwind.WorldWind#ABSOLUTE} is used.
     *
     * @param altitudeMode the altitude mode. The default value is {@link gov.nasa.worldwind.WorldWind#ABSOLUTE}.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
        this.reset();
    }

    /**
     * Returns the {@link gov.nasa.worldwind.render.ScreenAnnotation} used to draw the label, or {@code null} if the
     * path doesn't draw a label.
     *
     * @return the screen annotation used for drawing the label, or {@code null} if no label is drawn.
     */
    public ScreenAnnotation getAnnotation()
    {
        return this.annotation;
    }

    /**
     * Specifies the {@link gov.nasa.worldwind.render.ScreenAnnotation} to use for drawing the label. The specified
     * screen annotation's screen point is controlled by the labled path. Otherwise the screen annotation's attributes
     * are not modified.
     *
     * @param annotation the screen annotation to use for drawing the label, or {@code null} if no label should be
     *                   drawn.
     */
    public void setAnnotation(ScreenAnnotation annotation)
    {
        this.annotation = annotation;
        this.reset();
    }

    /**
     * Causes the labeled path to draw its label at one of the path locations.
     *
     * @param dc the <code>DrawContext</code> to be used.
     *
     * @throws IllegalArgumentException if dc is {@code null}.
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Determine the label's location only once per frame.
        if (this.frameNumber != dc.getFrameTimeStamp())
        {
            this.determineLabelLocation(dc);
            this.frameNumber = dc.getFrameTimeStamp();
        }

        this.drawLabel(dc);
    }

    /** Resets the labeled path's cached location information. */
    protected void reset()
    {
        this.labelLocationIndex = -1;
    }

    /**
     * Determines the screen location to place the label at, and stores the the index of the corresponding location in
     * {@link #labelLocationIndex}. This assigns {@code labelLocationIndex} to {@code null} if the label cannot be
     * placed at any screen location corresponding to the path.
     *
     * @param dc the current draw context.
     */
    protected void determineLabelLocation(DrawContext dc)
    {
        // Reuse the current label location if its inside the view frustum and the label is completely visible when
        // placed there. Otherwise we find a new location that maximizes the label's visible area and is closest to the
        // current location.
        Vec4 lastPoint = this.getLabelPoint(dc);
        if (lastPoint != null && dc.getView().getFrustumInModelCoordinates().contains(lastPoint))
        {
            // Project the current location's model point into screen coordinates, and place the label at the
            // projected point. We do this to measure the label's visible area when placed at that point.
            Vec4 screenPoint = dc.getView().project(lastPoint);
            this.setLabelLocation(dc, screenPoint);

            // If the label is completely visible, just reuse its current location.
            if (this.isLabelCompletelyVisible(dc))
                return;
        }

        this.labelLocationIndex = -1;

        if (this.getLocations() == null)
            return;

        double maxArea = 0;
        double minDistance = Double.MAX_VALUE;
        int locationIndex = -1;

        for (LatLon ll : this.getLocations())
        {
            ++locationIndex;

            if (ll == null)
                continue;

            // Compute the specified location's point in model coordinates. Ignore locations who's model coordinate
            // point cannot be computed for any reason, or are outside the view frustum.
            Vec4 point = this.computePoint(dc, ll);
            if (point == null || !dc.getView().getFrustumInModelCoordinates().contains(point))
                continue;

            // Project the specified location's model point into screen coordinates, and place the label at the
            // projected point. We do this to measure the label's visible area when placed at that point.
            Vec4 screenPoint = dc.getView().project(point);
            this.setLabelLocation(dc, screenPoint);

            // Find the location that maximizes the label's visible area.
            double area = this.getLabelVisibleArea(dc);
            if (maxArea < area)
            {
                maxArea = area;
                this.labelLocationIndex = locationIndex;

                if (lastPoint != null)
                    minDistance = lastPoint.distanceToSquared3(point);
            }
            // If two or more locations cause the label to have the same visible area, give priority to the location
            // closest to the previous location.
            else if (maxArea == area && lastPoint != null)
            {
                double dist = lastPoint.distanceToSquared3(point);
                if (minDistance > dist)
                {
                    minDistance = dist;
                    this.labelLocationIndex = locationIndex;
                }
            }
        }
    }

    /**
     * Causes the labeled path to draw its label at the path's current label location.
     *
     * @param dc the current draw context.
     */
    protected void drawLabel(DrawContext dc)
    {
        if (this.getAnnotation() == null)
            return;

        // Get the label's model point from the location iterable.
        Vec4 point = this.getLabelPoint(dc);
        if (point == null)
            return;

        // Project the label's model point into screen coordinates, place the annotation at the projected point then
        // draw the annotation.
        Vec4 screenPoint = dc.getView().project(point);
        this.setLabelLocation(dc, screenPoint);
        this.getAnnotation().render(dc);
    }

    /**
     * Places the label at the specified screen point.
     *
     * @param dc          the current draw context.
     * @param screenPoint the screen point to use.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void setLabelLocation(DrawContext dc, Vec4 screenPoint)
    {
        if (this.getAnnotation() != null)
            this.getAnnotation().setScreenPoint(new Point((int) screenPoint.x, (int) screenPoint.y));
    }

    /**
     * Returns the approximate number of square pixels that are visible at the label's current location, or 0 if the
     * label is not visible.
     *
     * @param dc the current draw context.
     *
     * @return the number of square pixels visible.
     */
    protected double getLabelVisibleArea(DrawContext dc)
    {
        if (this.getAnnotation() == null)
            return 0;

        Rectangle bounds = this.getAnnotation().getBounds(dc);
        if (bounds == null)
            return 0;

        Rectangle intersection = dc.getView().getViewport().intersection(bounds);
        return intersection.width * intersection.height;
    }

    /**
     * Returns {@code true} if the label is completely visible at its current location, and {@code false} otherwise.
     *
     * @param dc the current draw context.
     *
     * @return {@code true} if the label is completely visible at its current location, and {@code false} otherwise.
     */
    protected boolean isLabelCompletelyVisible(DrawContext dc)
    {
        if (this.getAnnotation() == null)
            return false;

        Rectangle bounds = this.getAnnotation().getBounds(dc);
        return bounds == null || dc.getView().getViewport().contains(bounds);
    }

    /**
     * Returns the label's model-coordinate point form the path's location iterable, applying the path's altitude mode.
     * If the location is a LatLon it's assumed to have an elevation of 0. This returns {@code null} if the path has no
     * label location, or if the path's locations have changed and no longer contain a value at the cached location
     * index.
     *
     * @param dc the current draw context.
     *
     * @return a model-coordinate point corresponding to the label's position and the path's path type.
     */
    protected Vec4 getLabelPoint(DrawContext dc)
    {
        if (this.getLocations() == null)
            return null;

        if (this.labelLocationIndex == -1)
            return null;

        int i = 0;
        LatLon location = null;
        for (LatLon ll : this.getLocations())
        {
            if (i++ == this.labelLocationIndex)
                location = ll;
        }

        if (location == null)
            return null;

        return this.computePoint(dc, location);
    }

    /**
     * Computes a model-coordinate point from a LatLon or Position, applying the path's altitude mode. If the location
     * is a LatLon it's assumed to have an elevation of 0.
     *
     * @param dc       the current draw context.
     * @param location the location to compute a point for.
     *
     * @return the model-coordinate point corresponding to the position and the path's path type.
     */
    protected Vec4 computePoint(DrawContext dc, LatLon location)
    {
        double elevation = (location instanceof Position) ? ((Position) location).getElevation() : 0;

        if (this.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)
            return dc.computeTerrainPoint(location.getLatitude(), location.getLongitude(), 0d);
        else if (this.getAltitudeMode() == WorldWind.RELATIVE_TO_GROUND)
            return dc.computeTerrainPoint(location.getLatitude(), location.getLongitude(), elevation);

        double height = elevation * dc.getVerticalExaggeration();
        return dc.getGlobe().computePointFromPosition(location.getLatitude(), location.getLongitude(), height);
    }
}
