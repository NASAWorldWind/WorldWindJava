/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.Restorable;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: Airspace.java 2394 2014-10-22 01:16:43Z tgaskins $
 */
public interface Airspace extends Renderable, Restorable, AVList, ExtentHolder, Highlightable, Attributable
{
    public static final String DRAW_STYLE_FILL = "Airspace.DrawStyleFill";
    public static final String DRAW_STYLE_OUTLINE = "Airspace.DrawStyleOutline";

    boolean isVisible();

    void setVisible(boolean visible);

    AirspaceAttributes getAttributes();

    void setAttributes(AirspaceAttributes attributes);

    /**
     * Returns this shape's highlight attributes.
     *
     * @return this shape's highlight attributes. May be null.
     */
    AirspaceAttributes getHighlightAttributes();

    /**
     * Specifies this shape's highlight attributes.
     *
     * @param highlightAttrs the highlight attributes. May be null, in which case default attributes are used.
     */
    void setHighlightAttributes(AirspaceAttributes highlightAttrs);

    /**
     * Returns the current airspace surface altitudes.
     *
     * @return a two-element array of <code>double</code> with element 0 containing the lower surface altitude, and
     * element 1 containing the upper surface altitude.
     *
     * @see #setAltitudes(double, double)
     * @see #setAltitudeDatum
     * @see #setGroundReference
     */
    double[] getAltitudes();

    /**
     * Sets the lower and upper airspace surface altitudes. The altitudes are interpreted according to the current
     * altitude datum of the respective surface. See {@link #setAltitudeDatum(String, String)} for a description of the
     * possible interpretations and the means to specify them.
     *
     * @param lowerAltitude the lower surface altitude, in meters.
     * @param upperAltitude the upper surface altitude, in meters.
     *
     * @see #setAltitudes(double, double)
     * @see #setAltitudeDatum
     * @see #setGroundReference
     */
    void setAltitudes(double lowerAltitude, double upperAltitude);

    /**
     * Sets the lower and upper airspace surface altitudes to the same value. The lower and upper altitudes are
     * interpreted according to the current altitude datum of the respective surface. See {@link
     * #setAltitudeDatum(String, String)} for a description of the possible interpretations and the means to specify
     * them.
     *
     * @param altitude the lower surface altitude, in meters.
     *
     * @see #setAltitudes(double, double)
     * @see #setAltitudeDatum
     * @see #setGroundReference
     */
    void setAltitude(double altitude);

    /**
     * Returns the old-style indicators of the airspace's lower and upper surface datums.
     *
     * @return the lower and upper surface datums.
     */
    boolean[] isTerrainConforming();

    /**
     * Sets the altitude datum, which indicates whether airspace altitudes are relative to mean sea level, ground level
     * or a single ground reference location. The datum is normally set via {@link #setAltitudeDatum(String, String)},
     * but this method is provided for backwards compatibility with the means of originally setting the datum. See the
     * argument descriptions below for the mapping of the boolean values of this method to the altitude-datum values.
     *
     * @param lowerTerrainConformant the lower altitude datum. A value of true indicates a lower altitude datum of
     *                               {@link AVKey#ABOVE_GROUND_LEVEL} (terrain conforming), a value of false indicates a
     *                               lower altitude datum of {link AVKey#ABOVE_MEAN_SEA_LEVEL} (not terrain conforming).
     *                               the terrain-conforming, a value of false indicates that it's not.
     * @param upperTerrainConformant the upper altitude datum. A value of true indicates an upper altitude datum of
     *                               {@link AVKey#ABOVE_GROUND_LEVEL} (terrain conforming), a value of false indicates
     *                               an upper altitude datum of {link AVKey#ABOVE_MEAN_SEA_LEVEL} (not terrain
     *                               conforming. the terrain-conforming, a value of false indicates that it's not.
     *
     * @see #setAltitudeDatum(String, String)
     */
    void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant);

    /**
     * Indicates the state of this airspace's always-on-top flag.
     *
     * @return the state of this airspace's always-on-top flag.
     *
     * @see #isAlwaysOnTop()
     */
    boolean isAlwaysOnTop();

    /**
     * Specifies whether this airspace should have visual priority over other displayed shapes in 3D mode. If
     * <code>true</code>, this shape is drawn after all others. This property is ignored by {@link
     * gov.nasa.worldwind.render.airspaces.Cake} airspaces.
     *
     * @param alwaysOnTop if <code>true</code>, this airspace is drawn after all others. Otherwise this airspace is
     *                    drawn with its normal priority, which is its relative distance to the eye point.
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

    /**
     * Indicates whether this surface shape is always drawn flat and on the surface.
     *
     * @return <code>true</code> if this shape is drawn flat and on the surface, otherwise <code>false</code>.
     */
    boolean isDrawSurfaceShape();

    /**
     * Specifies whether this airspace should be drawn flat and on the surface, ignoring any altitude information
     * specified in the shape.
     *
     * @param drawSurfaceShape <code>true</code> if this shape is drawn flat and on the surface, otherwise
     *                         <code>false</code>.
     */
    void setDrawSurfaceShape(boolean drawSurfaceShape);

    /**
     * Sets the altitude datum for both the lower and upper airspace surface to the same specified value. The datum is
     * normally set via {@link #setAltitudeDatum(String, String)}, but this method is provided for backwards
     * compatibility with the means of originally setting the datum. See the argument descriptions for the mapping of
     * the boolean values of this method to the altitude-datum values.
     *
     * @param terrainConformant the altitude datum. See {@link #setTerrainConforming(boolean, boolean)} for a
     *                          description of the possible values.
     */
    void setTerrainConforming(boolean terrainConformant);

    boolean isEnableLevelOfDetail();

    void setEnableLevelOfDetail(boolean enableLevelOfDetail);

    Iterable<DetailLevel> getDetailLevels();

    void setDetailLevels(Collection<DetailLevel> detailLevels);

    /**
     * Test if this airspace is visible in the specified draw context. During picking mode, this tests intersection
     * against all of the draw context's pick frustums. During rendering mode, this tests intersection against the draw
     * context's viewing frustum.
     *
     * @param dc the draw context the airspace is related to.
     *
     * @return true if this airspace is visible; false otherwise.
     */
    boolean isAirspaceVisible(DrawContext dc);

    /**
     * Returns this Airspace's enclosing volume as an {@link gov.nasa.worldwind.geom.Extent} in model coordinates, given
     * a specified {@link gov.nasa.worldwind.globes.Globe} and vertical exaggeration (see {@link
     * gov.nasa.worldwind.SceneController#getVerticalExaggeration()}.
     *
     * @param globe                the Globe this Airspace is related to.
     * @param verticalExaggeration the vertical exaggeration of the scene containing this Airspace.
     *
     * @return this Airspace's Extent in model coordinates.
     *
     * @throws IllegalArgumentException if the Globe is null.
     */
    Extent getExtent(Globe globe, double verticalExaggeration);

    /**
     * Returns this Airspace's enclosing volume as an {@link gov.nasa.worldwind.geom.Extent} in model coordinates, given
     * a specified {@link gov.nasa.worldwind.render.DrawContext}. The returned Extent may be different than the Extent
     * returned by calling {@link #getExtent(gov.nasa.worldwind.globes.Globe, double)} with the DrawContext's Globe and
     * vertical exaggeration. Additionally, this may cache the computed extent and is therefore potentially faster than
     * calling {@link #getExtent(gov.nasa.worldwind.globes.Globe, double)}.
     *
     * @param dc the current DrawContext.
     *
     * @return this Airspace's Extent in model coordinates.
     *
     * @throws IllegalArgumentException if the DrawContext is null, or if the Globe held by the DrawContext is null.
     */
    Extent getExtent(DrawContext dc);

    /**
     * Sets the altitude datum, which indicates whether airspace altitudes are relative to mean sea level, ground level
     * or a single ground reference location.
     * <p/>
     * A value of {@link AVKey#ABOVE_MEAN_SEA_LEVEL}, the default for both lower and upper datums, indicates a datum of
     * mean sea level. The respective lower or upper surface of the airspace is drawn at the constant altitude specified
     * by {@link #setAltitude(double)}.
     * <p/>
     * A datum of {@link AVKey#ABOVE_GROUND_LEVEL} indicates that each position of the respective airspace surface is
     * offset vertically from the altitude specified to {@link #setAltitude(double)} by an amount equal to the terrain
     * elevation at that position. For example, if the specified lower altitude is zero, the lower surface lies on and
     * conforms to the terrain. If non-zero, the surface undulates in tandem with the terrain but relative to the
     * specified altitude.
     * <p/>
     * A datum of {@link AVKey#ABOVE_GROUND_REFERENCE} combines both of the above datums. It indicates that the
     * respective surface is drawn at the altitude specified to {@link #setAltitude(double)} but offset vertically by an
     * amount equal to the elevation at a single reference location on the ground. This is useful for displaying
     * surfaces that are "flat" but are positioned relative to the ground. An example is the roof of a building, which
     * maintains a constant altitude even as the base of its building may conform to varying terrain. One method of
     * representing buildings is to specify a lower altitude of 0, a lower altitude datum of {@link
     * AVKey#ABOVE_GROUND_LEVEL}, an upper altitude that's the building's height, and an upper altitude datum of {@link
     * AVKey#ABOVE_GROUND_REFERENCE}, where the ground reference is a location at the building's base. The reference
     * position is specifed by {@link #setGroundReference(LatLon)}.
     *
     * @param lowerAltitudeDatum the lower altitude datum.
     * @param upperAltitudeDatum the upper altitude datum
     *
     * @throws IllegalArgumentException if either the lower or upper altitude datum is null.
     * @see #setGroundReference(gov.nasa.worldwind.geom.LatLon)
     * @see #setAltitudes
     */
    void setAltitudeDatum(String lowerAltitudeDatum, String upperAltitudeDatum);

    /**
     * Returns the current altitude datum of the airspace's lower and upper surfaces.
     *
     * @return a two-element array containing at position 0 the lower altitude datum, and at position 1 the upper
     * altitude datum.
     *
     * @see #setAltitudeDatum(String, String)
     */
    String[] getAltitudeDatum();

    /**
     * Sets the reference location used to determine the elevation offset for airspace surfaces whose altitude datum is
     * {@link AVKey#ABOVE_GROUND_REFERENCE}. The reference location is unused if the altitude datum is a value other
     * than this.
     *
     * @param groundReference the location at which to compute the terrain elevation used to offset an upper or lower
     *                        airspace surface. The location need not be within the airspace's bounds. If null, an
     *                        airspace-specific position is chosen from those defining the airspace. See the method
     *                        descriptions for the individual airspaces to determine the position used.
     *
     * @see #setAltitudeDatum(String, String)
     */
    void setGroundReference(LatLon groundReference);

    /**
     * Returns the current ground reference location.
     *
     * @return the current ground reference location.
     */
    LatLon getGroundReference();

    /**
     * Indicates whether batch rendering is enabled for the concrete shape type of this shape.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchRendering(boolean).
     */
    boolean isEnableBatchRendering();

    /**
     * Specifies whether adjacent shapes of this shape's concrete type in the ordered renderable list may be rendered
     * together if they are contained in the same layer. This increases performance. There is seldom a reason to disable
     * it.
     *
     * @param enableBatchRendering true to enable batch rendering, otherwise false.
     */
    void setEnableBatchRendering(boolean enableBatchRendering);

    /**
     * Indicates whether batch picking is enabled.
     *
     * @return true if batch rendering is enabled, otherwise false.
     *
     * @see #setEnableBatchPicking(boolean).
     */
    boolean isEnableBatchPicking();

    /**
     * Specifies whether adjacent shapes of this shape's concrete type in the ordered renderable list may be pick-tested
     * together if they are contained in the same layer. This increases performance but allows only the top-most of the
     * polygons to be reported in a {@link gov.nasa.worldwind.event.SelectEvent} even if several of the polygons are at
     * the pick position.
     * <p/>
     * Batch rendering ({@link #setEnableBatchRendering(boolean)}) must be enabled in order for batch picking to occur.
     *
     * @param enableBatchPicking true to enable batch rendering, otherwise false.
     */
    void setEnableBatchPicking(boolean enableBatchPicking);

    /**
     * Indicates whether the filled sides of this shape should be offset towards the viewer to help eliminate artifacts
     * when two or more faces of this or other filled shapes are coincident.
     *
     * @return true if depth offset is applied, otherwise false.
     */
    boolean isEnableDepthOffset();

    /**
     * Specifies whether the filled sides of this shape should be offset towards the viewer to help eliminate artifacts
     * when two or more faces of this or other filled shapes are coincident.
     *
     * @param enableDepthOffset true if depth offset is applied, otherwise false.
     */
    void setEnableDepthOffset(boolean enableDepthOffset);

    /**
     * Indicates the outline line width to use during picking. A larger width than normal typically makes the outline
     * easier to pick.
     *
     * @return the outline line width used during picking.
     */
    int getOutlinePickWidth();

    /**
     * Specifies the outline line width to use during picking. A larger width than normal typically makes the outline
     * easier to pick.
     * <p/>
     * Note that the size of the pick aperture also affects the precision necessary to pick.
     *
     * @param outlinePickWidth the outline pick width. The default is 10.
     *
     * @throws IllegalArgumentException if the width is less than 0.
     */
    void setOutlinePickWidth(int outlinePickWidth);

    Object getDelegateOwner();

    void setDelegateOwner(Object delegateOwner);
}
