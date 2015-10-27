/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;

/**
 * Common interface for surface conforming shapes such as {@link gov.nasa.worldwind.render.SurfacePolygon}, {@link
 * gov.nasa.worldwind.render.SurfacePolyline}, {@link gov.nasa.worldwind.render.SurfaceEllipse}, {@link
 * gov.nasa.worldwind.render.SurfaceQuad}, and {@link gov.nasa.worldwind.render.SurfaceSector}.
 * <p/>
 * SurfaceShape extends the {@link gov.nasa.worldwind.render.SurfaceObject} interface, and inherits SurfaceObject's
 * batch rendering capabilities.
 *
 * @author dcollins
 * @version $Id: SurfaceShape.java 2339 2014-09-22 18:22:37Z tgaskins $
 */
public interface SurfaceShape
    extends SurfaceObject, Highlightable, ExtentHolder, MeasurableArea, MeasurableLength, Restorable, Attributable
{
    /**
     * Indicates whether to highlight the surface shape.
     *
     * @return true if highlighted, otherwise false.
     *
     * @see #setHighlighted(boolean)
     * @see #setHighlightAttributes(ShapeAttributes)
     */
    boolean isHighlighted();

    /**
     * Specifies whether the surface shape is highlighted.
     *
     * @param highlighted true to highlight the surface shape, otherwise false. The default value is false.
     *
     * @see #setHighlightAttributes(ShapeAttributes)
     */
    void setHighlighted(boolean highlighted);

    /**
     * Returns the surface shape's normal (as opposed to highlight) attributes. Modifying the contents of the returned
     * reference causes this shape's appearance to change accordingly.
     *
     * @return the surface shape's normal attributes. May be null.
     */
    ShapeAttributes getAttributes();

    /**
     * Specifies the surface shape's normal (as opposed to highlight) attributes. Modifying the attribute reference
     * after calling setAttributes() causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    void setAttributes(ShapeAttributes normalAttrs);

    /**
     * Returns the surface shape's highlight attributes. Modifying the contents of the returned reference causes this
     * shape's appearance to change accordingly.
     *
     * @return the surface shape's highlight attributes. May be null.
     */
    ShapeAttributes getHighlightAttributes();

    /**
     * Specifies the surface shape's highlight attributes. Modifying the attribute reference after calling
     * setHighlightAttributes() causes this shape's appearance to change accordingly.
     *
     * @param highlightAttrs the highlight attributes. May be null, in which case default attributes are used.
     */
    void setHighlightAttributes(ShapeAttributes highlightAttrs);

    /**
     * Returns the path type used to interpolate between locations on this SurfaceShape.
     *
     * @return path interpolation type.
     */
    String getPathType();

    /**
     * Sets the path type used to interpolate between locations on this SurfaceShape. This should be one of <ul>
     * <li>gov.nasa.worldwind.avlist.AVKey.GREAT_CIRCLE</li> <li>gov.nasa.worldwind.avlist.AVKey.LINEAR</li>
     * <li>gov.nasa.worldwind.avlist.AVKey.LOXODROME</li> <li>gov.nasa.worldwind.avlist.AVKey.RHUMB</li> </ul>
     *
     * @param pathType path interpolation type.
     *
     * @throws IllegalArgumentException if <code>pathType</code> is null.
     */
    void setPathType(String pathType);

    /**
     * Returns the number of texels per shape edge interval.
     *
     * @return texels per shape edge interval.
     *
     * @see #setTexelsPerEdgeInterval(double)
     */
    double getTexelsPerEdgeInterval();

    /**
     * Sets the number of texels per shape edge interval. This value controls how many interpolated intervals are added
     * to each shape edge, depending on size of the original edge in texels. Each shape is responsible for defining what
     * an edge is, though for most shapes it is defined as the edge between implicit or caller-specified shape
     * locations. The number of interpolated intervals is limited by the values set in a call to {@link
     * #setMinAndMaxEdgeIntervals(int, int)}.
     *
     * @param texelsPerEdgeInterval the size, in texels, of each interpolated edge interval.
     *
     * @throws IllegalArgumentException if <code>texelsPerEdgeInterval</code> is less than or equal to zero.
     * @see #setMinAndMaxEdgeIntervals(int, int)
     */
    void setTexelsPerEdgeInterval(double texelsPerEdgeInterval);

    /**
     * Returns the minimum and maximum number of interpolated intervals that may be added to each shape edge.
     *
     * @return array of two elements, the first element is minEdgeIntervals, the second element is maxEdgeIntervals.
     *
     * @see #setMinAndMaxEdgeIntervals(int, int)
     */
    int[] getMinAndMaxEdgeIntervals();

    /**
     * Sets the minimum and maximum number of interpolated intervals that may be added to each shape edge. The minimum
     * and maximum values may be 0, or any positive integer. Note that Setting either of <code>minEdgeIntervals</code>
     * or <code>maxEdgeIntervals</code> too large may adversely impact surface shape rendering performance.
     *
     * @param minEdgeIntervals the minimum number of interpolated edge intervals.
     * @param maxEdgeIntervals the maximum number of interpolated edge intervals.
     *
     * @throws IllegalArgumentException if either of <code>minEdgeIntervals</code> or <code>maxEdgeIntervals</code> is
     *                                  less than or equal to zero.
     * @see #setTexelsPerEdgeInterval(double)
     */
    void setMinAndMaxEdgeIntervals(int minEdgeIntervals, int maxEdgeIntervals);

    /**
     * Returns the shape's locations as they appear on the specified <code>globe</code>, or null if the shape has no
     * locations.
     *
     * @param globe the globe the shape is related to.
     *
     * @return the shapes locations on the globe, or null if the shape has no locations.
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    Iterable<? extends LatLon> getLocations(Globe globe);

    /**
     * Returns the shapes's area in square meters. If <code>terrainConformant</code> is true, the area returned is the
     * surface area of the terrain, including its hillsides and other undulations.
     *
     * @param globe             the globe the shape is related to.
     * @param terrainConformant whether or not the returned area should treat the shape as conforming to the terrain.
     *
     * @return the shape's area in square meters. Returns -1 if the object does not form an area due to an insufficient
     *         number of vertices or any other condition.
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    double getArea(Globe globe, boolean terrainConformant);
}
