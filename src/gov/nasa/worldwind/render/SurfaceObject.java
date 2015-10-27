/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Extent;

import java.awt.*;

/**
 * Common interface for renderables that are drawn on the Globe's surface terrain, such as {@link
 * gov.nasa.worldwind.render.SurfaceShape}. SurfaceObject implements the {@link gov.nasa.worldwind.render.Renderable}
 * interface, so a surface object may be aggregated within any layer or within some arbitrary rendering code.
 * <p/>
 * SurfaceObjects automatically aggregate themselves in the DrawContext's ordered surface renderable queue by calling
 * {@link gov.nasa.worldwind.render.DrawContext#addOrderedSurfaceRenderable(OrderedRenderable)} during the preRender,
 * pick, and render stages. This enables SurfaceObjects to be processed in bulk, and reduces texture memory consumption
 * by sharing rendering resources amongst multiple SurfaceObjects.
 * <p/>
 * Implementations of SurfaceObject require that {@link #preRender(DrawContext)} is called before {@link
 * #render(DrawContext)} and {@link #pick(DrawContext, java.awt.Point)}, and that preRender is called at the appropriate
 * stage in the current rendering cycle. Calling preRender locks in the SurfaceObject's visual appearance for any
 * subsequent calls to pick or render until the next call preRender.
 *
 * @author dcollins
 * @version $Id: SurfaceObject.java 2283 2014-08-30 15:58:43Z dcollins $
 */
public interface SurfaceObject extends OrderedRenderable, SurfaceRenderable, PreRenderable, AVList
{
    /**
     * Indicates whether the surface object should be drawn during rendering.
     *
     * @return true if the object is to be drawn, otherwise false.
     */
    boolean isVisible();

    /**
     * Specifies whether the surface object should be drawn during rendering.
     *
     * @param visible true if the object is to be drawn, otherwise false.
     */
    void setVisible(boolean visible);

    /**
     * Indicates whether batch picking is enabled.
     *
     * @return <code>true</code> to enable batch picking; <code>false</code> otherwise.
     *
     * @see #setEnableBatchPicking(boolean)
     */
    boolean isEnableBatchPicking();

    /**
     * Specifies whether adjacent SurfaceObjects in the DrawContext's ordered surface renderable list may be rendered
     * together during picking if they are contained in the same layer. This increases performance and there is seldom a
     * reason to disable it.
     *
     * @param enable <code>true</code> to enable batch picking; <code>false</code> otherwise.
     */
    void setEnableBatchPicking(boolean enable);

    /**
     * Returns zero to indicate that the surface object's distance from the eye is unknown. SurfaceObjects are processed
     * on the DrawContext's ordered surface renderable queue. Ordered surface renderables do not utilize the
     * renderable's distance from the eye to determine draw order.
     *
     * @return zero, to indicate that the object's distance from the eye is unknown.
     */
    double getDistanceFromEye();

    /**
     * Returns the delegate owner of the surface object. If non-null, the returned object replaces the surface object as
     * the pickable object returned during picking. If null, the surface object itself is the pickable object returned
     * during picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the surface
     *         object is returned during picking.
     */
    Object getDelegateOwner();

    /**
     * Specifies the delegate owner of the surface object. If non-null, the delegate owner replaces the surface object
     * as the pickable object returned during picking. If null, the surface object itself is the pickable object
     * returned during picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the surface
     *              object.
     */
    void setDelegateOwner(Object owner);

    /**
     * Returns the surface object's enclosing volume as an {@link gov.nasa.worldwind.geom.Extent} in model coordinates,
     * given a specified {@link gov.nasa.worldwind.render.DrawContext}.
     *
     * @param dc the current draw context.
     *
     * @return the surface object's Extent in model coordinates.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    Extent getExtent(DrawContext dc);

    /**
     * Causes the surface object to prepare a representation of itself which can be drawn on the surface terrain, using
     * the provided draw context.
     *
     * @param dc the current draw context.
     */
    void preRender(DrawContext dc);

    /**
     * Causes the surface object to draw a pickable representation of itself on the surface terrain, using the provided
     * draw context.
     *
     * @param dc        the current draw context.
     * @param pickPoint the pick point.
     *
     * @throws IllegalArgumentException if the draw context is null.
     */
    void pick(DrawContext dc, Point pickPoint);
}
