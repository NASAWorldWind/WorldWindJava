/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.render.DrawContext;

/**
 * @author Tom Gaskins
 * @version $Id: Layer.java 1824 2014-01-22 22:41:10Z dcollins $
 */
public interface Layer extends WWObject, Disposable, Restorable
{
    /**
     * Indicates whether the layer is enabled for rendering and selection.
     *
     * @return true if the layer is enabled, else false.
     */
    boolean isEnabled();

    /**
     * Controls whether the layer is enabled for rendering and selection.
     *
     * @param enabled <code>true</code> if the layer is enabled, else <code>false</code>.
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the layer's name, as specified in the most recent call to {@link #setName}.
     *
     * @return the layer's name.
     */
    String getName();

    /**
     * Set the layer's name. The name is a convenience attribute typically used to identify the layer in user
     * interfaces. By default, a layer has no name.
     *
     * @param name the name to assign to the layer.
     */
    void setName(String name);

    /**
     * Returns the layer's opacity, the degree to which it is blended with underlying layers.
     * <p/>
     * Many layers apply special usage of opacity, and some ignore it in favor of the opacity settings of their internal
     * renderables. See the description of this method in specific layers to determine usage there.
     *
     * @return The layer's opacity, a value between 0 and 1.
     */
    double getOpacity();

    /**
     * Sets the layer's opacity, the degree to which it is blended with underlying layers.
     * <p/>
     * Many layers apply special usage of opacity, and some ignore it in favor of the opacity settings of their internal
     * renderables. See the description of this method in specific layers to determine usage there.
     *
     * @param opacity The layer opacity, a value between 0 and 1. 0 indicates non-opaque (fully transparent), 1
     *                indicates fully opaque. Values between 0 and 1 indicate partial opacity.
     */
    void setOpacity(double opacity);

    /**
     * Indicates whether the layer performs selection during picking.
     * <p/>
     * Most layers enable picking by default. However, this becomes inconvenient for {@link
     * gov.nasa.worldwind.render.SurfaceImage} and {@link gov.nasa.worldwind.layers.SurfaceImageLayer}} when the image
     * covers a large area because the view input handlers detect the surface image rather than the terrain as the top
     * picked object, and will not respond to the user's attempts at navigation. The solution is to disable picking for
     * the layer.
     *
     * @return <code>true</code> if picking is enabled, else <code>false</code>.
     */
    boolean isPickEnabled();

    /**
     * Controls whether the layer should perform picking.
     *
     * @param isPickable <code>true</code> if the layer should perform picking, else <code>false</code>.
     */
    void setPickEnabled(boolean isPickable);

    /**
     * Causes the layer to perform any actions necessary to subsequently render the layer. The layer has exclusive
     * access to the frame buffer during the call, and may use it to generate images or other information that is
     * subsequently used to render the layer's contents. Upon return, the OpenGL state must be restored to its
     * original.
     *
     * @param dc the current draw context.
     */
    void preRender(DrawContext dc);

    /**
     * Cause the layer to draw its representation.
     *
     * @param dc the current draw context for rendering.
     */
    void render(DrawContext dc);

    /**
     * Cause the layer to perform picking, which determines whether the object or its components intersect a given point
     * on the screen. Objects that intersect that point are added to the draw context's pick list and are conveyed to
     * the application via selection events or by a direct query of {@link WorldWindow#getObjectsAtCurrentPosition()}.
     *
     * @param dc        the current draw context for rendering.
     * @param pickPoint the screen coordinate point
     *
     * @see SelectEvent
     */
    void pick(DrawContext dc, java.awt.Point pickPoint);

    /**
     * Indicates whether the most recent rendering of the layer rendered the highest resolution imagery or other data
     * available. Some layers do not track resolution. For those layers this value will always be <code>true</code>.
     * Typically such layers also return <code>false</code> from {@link #isMultiResolution}.
     *
     * @return <code>true</code> if the layer is at maximum resolution, otherwise <code>false</code>.
     */
    boolean isAtMaxResolution();

    /**
     * Indicates whether the layer provides multiple resolutions of imagery or other data.
     *
     * @return <code>true</code> if the layer provides multiple resolutions, else <code>false</code>.
     */
    boolean isMultiResolution();

    /**
     * Returns the map scale, in terms of the ratio of 1 to the value returned, e.g., 1:24000.
     *
     * @return the map scale.
     */
    double getScale();

    /**
     * Indicates whether the layer is allowed to retrieve data from the network. Many layers have no need to retrieve
     * data from the network. This state is meaningless for such layers.
     *
     * @return <code>true</code> if the layer is enabled to retrieve network data, else <code>false</code>.
     */
    boolean isNetworkRetrievalEnabled();

    /**
     * Controls whether the layer is allowed to retrieve data from the network. Many layers have no need for data from
     * the network. This state may be set but is meaningless for such layers.
     *
     * @param networkRetrievalEnabled <code>true</code> if network retrieval is allowed, else <code>false</code>.
     */
    void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled);

    /**
     * Specifies the time of the layer's most recent dataset update. If greater than zero, the layer ignores and
     * eliminates any previously cached data older than the time specified, and requests new information from the data
     * source. If zero, the layer uses any expiry times intrinsic to the layer, typically initialized at layer
     * construction. The default expiry time is 0, thereby enabling a layer's intrinsic expiration criteria.
     *
     * @param expiryTime the expiry time of any cached data, expressed as a number of milliseconds beyond the epoch.
     *
     * @see System#currentTimeMillis() for a description of milliseconds beyond the epoch.
     */
    void setExpiryTime(long expiryTime);

    /**
     * Returns the current expiry time.
     *
     * @return the current expiry time.
     */
    long getExpiryTime();

    /**
     * Returns the minimum altitude at which the layer is displayed.
     *
     * @return the minimum altitude at which the layer is displayed.
     */
    double getMinActiveAltitude();

    /**
     * Specifies the minimum altitude at which to display the layer.
     *
     * @param minActiveAltitude the minimum altitude at which to display the layer.
     */
    void setMinActiveAltitude(double minActiveAltitude);

    /**
     * Returns the maximum altitude at which to display the layer.
     *
     * @return the maximum altitude at which to display the layer.
     */
    double getMaxActiveAltitude();

    /**
     * Specifies the maximum altitude at which to display the layer.
     *
     * @param maxActiveAltitude the maximum altitude at which to display the layer.
     */
    void setMaxActiveAltitude(double maxActiveAltitude);

    /**
     * Indicates whether the layer is in the view. The method implemented here is a default indicating the layer is in
     * view. Subclasses able to determine their presence in the view should override this implementation.
     *
     * @param dc the current draw context
     *
     * @return <code>true</code> if the layer is in the view, <code>false</code> otherwise.
     */
    boolean isLayerInView(DrawContext dc);

    /**
     * Indicates whether the layer is active based on arbitrary criteria. The method implemented here is a default
     * indicating the layer is active if the current altitude is within the layer's min and max active altitudes.
     * Subclasses able to consider more criteria should override this implementation.
     *
     * @param dc the current draw context
     *
     * @return <code>true</code> if the layer is active, <code>false</code> otherwise.
     */
    boolean isLayerActive(DrawContext dc);

    /**
     * Indicates the altitude above which this layer likely has low value or is not expected to be active. This value is
     * independent of the maximum active altitude, {@link #setMaxActiveAltitude(double)} and does not reflect it.
     * <p/>
     * The returned altitude is valid when the field of view indicated by {@link gov.nasa.worldwind.View#getFieldOfView()}
     * is set to its default value. Changing the field of view to any value other than the default may change this
     * layer's maximum effective altitude, but the returned altitude will not reflect that change.
     *
     * @param radius the radius of the {@link gov.nasa.worldwind.globes.Globe} the layer is associated with. May be
     *               null, in which case the Earth's equatorial radius is used, {@link gov.nasa.worldwind.globes.Earth#WGS84_EQUATORIAL_RADIUS}.
     *
     * @return the layer's maximum effective altitude.
     */
    Double getMaxEffectiveAltitude(Double radius);

    /**
     * Indicates the altitude below which this layer likely has low value or is not expected to be active. This value is
     * independent of the minimum active altitude, {@link #setMinActiveAltitude(double)} and does not reflect it.
     * <p/>
     * The returned altitude is valid when the field of view indicated by {@link gov.nasa.worldwind.View#getFieldOfView()}
     * is set to its default value. Changing the field of view to any value other than the default may change this
     * layer's minimum effective altitude, but the returned altitude will not reflect that change.
     *
     * @param radius the radius of the {@link gov.nasa.worldwind.globes.Globe} the layer is associated with. May be
     *               null, in which case the Earth's equatorial radius is used, {@link gov.nasa.worldwind.globes.Earth#WGS84_EQUATORIAL_RADIUS}.
     *
     * @return the layer's minimum effective altitude.
     */
    Double getMinEffectiveAltitude(Double radius);
}
