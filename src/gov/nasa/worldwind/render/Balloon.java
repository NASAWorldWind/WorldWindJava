/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.TextDecoder;

/**
 * A text label that can be attached to a point on the screen, or a point on the globe.
 *
 * @author pabercrombie
 * @version $Id: Balloon.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see BalloonAttributes
 */
public interface Balloon extends Renderable, Highlightable, AVList
{
    /**
     * Is the balloon always on top?
     *
     * @return True if the balloon will always render above other items.
     */
    boolean isAlwaysOnTop();

    /**
     * Set the balloon to always on top or not.
     *
     * @param alwaysOnTop True if the balloon should always render above other objects.
     */
    void setAlwaysOnTop(boolean alwaysOnTop);

    /**
     * Is the balloon enabled for picking?
     *
     * @return True if the balloon can be picked, false if not.
     */
    boolean isPickEnabled();

    /**
     * Set the balloon to be pick enabled or not.
     *
     * @param enable True if the balloon can be picked, false if not.
     */
    void setPickEnabled(boolean enable);

    /**
     * Get the balloon text. The method returns the raw text, before decoding.
     *
     * @return The balloon text.
     *
     * @see #getTextDecoder()
     * @see #setTextDecoder(gov.nasa.worldwind.util.TextDecoder)
     */
    String getText();

    /**
     * Set the balloon text.
     *
     * @param text New balloon text.
     */
    void setText(String text);

    /**
     * Get the "normal" balloon attributes.
     *
     * @return Balloon attributes.
     */
    BalloonAttributes getAttributes();

    /**
     * Set the "normal" balloon attributes.
     *
     * @param attrs New attributes
     */
    void setAttributes(BalloonAttributes attrs);

    /**
     * Get the highlight attributes.
     *
     * @return Balloon's highlight attributes.
     */
    BalloonAttributes getHighlightAttributes();

    /**
     * Set the highlight attributes.
     *
     * @param attrs Attributes to use when the balloon is highlighted.
     */
    void setHighlightAttributes(BalloonAttributes attrs);

    /**
     * Get the text decoder that will process the balloon text.
     *
     * @return Active text decoder.
     */
    TextDecoder getTextDecoder();

    /**
     * Set a text decoder to process the balloon text.
     *
     * @param decoder New decoder.
     */
    void setTextDecoder(TextDecoder decoder);

    /**
     * Returns the delegate owner of the balloon. If non-null, the returned object replaces the balloon as the pickable
     * object returned during picking. If null, the balloon itself is the pickable object returned during picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the balloon is
     *         returned during picking.
     */
    Object getDelegateOwner();

    /**
     * Specifies the delegate owner of the balloon. If non-null, the delegate owner replaces the balloon as the
     * pickable object returned during picking. If null, the balloon itself is the pickable object returned during
     * picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the balloon.
     */
    void setDelegateOwner(Object owner);

    /**
     * Get whether the annotation is visible and should be rendered.
     *
     * @return true if the annotation is visible and should be rendered.
     */
    public boolean isVisible();

    /**
     * Set whether the balloon is visible and should be rendered.
     *
     * @param visible true if the balloon is visible and should be rendered.
     */
    public void setVisible(boolean visible);

    /**
     * Get the balloon bounding {@link java.awt.Rectangle} using OGL coordinates - bottom-left corner x and y relative
     * to the {@link gov.nasa.worldwind.WorldWindow} bottom-left corner, and the balloon callout width and height.
     * <p/>
     * The balloon offset from it's reference point is factored in such that the callout leader shape and reference
     * point are included in the bounding rectangle.
     *
     * @param dc the current DrawContext.
     *
     * @return the balloon bounding {@link java.awt.Rectangle} using OGL viewport coordinates.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null.
     */
    java.awt.Rectangle getBounds(DrawContext dc);

    /**
     * Returns the minimum eye altitude, in meters, for which the balloon is displayed.
     *
     * @return the minimum altitude, in meters, for which the balloon is displayed.
     *
     * @see #setMinActiveAltitude(double)
     * @see #getMaxActiveAltitude()
     */
    double getMinActiveAltitude();

    /**
     * Specifies the minimum eye altitude, in meters, for which the balloon is displayed.
     *
     * @param minActiveAltitude the minimum altitude, in meters, for which the balloon is displayed.
     *
     * @see #getMinActiveAltitude()
     * @see #setMaxActiveAltitude(double)
     */
    void setMinActiveAltitude(double minActiveAltitude);

    /**
     * Returns the maximum eye altitude, in meters, for which the balloon is displayed.
     *
     * @return the maximum altitude, in meters, for which the balloon is displayed.
     *
     * @see #setMaxActiveAltitude(double)
     * @see #getMinActiveAltitude()
     */
    double getMaxActiveAltitude();

    /**
     * Specifies the maximum eye altitude, in meters, for which the balloon is displayed.
     *
     * @param maxActiveAltitude the maximum altitude, in meters, for which the balloon is displayed.
     *
     * @see #getMaxActiveAltitude()
     * @see #setMinActiveAltitude(double)
     */
    void setMaxActiveAltitude(double maxActiveAltitude);
}
