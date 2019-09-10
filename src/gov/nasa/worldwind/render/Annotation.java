/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.*;

import com.jogamp.opengl.GL;

/**
 * Represent a text label and its rendering attributes.
 *
 * @author Patrick Murris
 * @version $Id: Annotation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Annotation extends Renderable, Disposable, Restorable
{
    /** @deprecated Use {@link AVKey#REPEAT_NONE} instead. */
    @Deprecated
    public static final String IMAGE_REPEAT_NONE = AVKey.REPEAT_NONE;
    /** @deprecated Use {@link AVKey#REPEAT_X} instead. */
    @Deprecated
    public static final String IMAGE_REPEAT_X = AVKey.REPEAT_X;
    /** @deprecated Use {@link AVKey#REPEAT_Y} instead. */
    @Deprecated
    public static final String IMAGE_REPEAT_Y = AVKey.REPEAT_Y;
    /** @deprecated Use {@link AVKey#REPEAT_XY} instead. */
    @Deprecated
    public static final String IMAGE_REPEAT_XY = AVKey.REPEAT_XY;

    public final static int ANTIALIAS_DONT_CARE = GL.GL_DONT_CARE;
    public final static int ANTIALIAS_FASTEST = GL.GL_FASTEST;
    public final static int ANTIALIAS_NICEST = GL.GL_NICEST;

    /** @deprecated Use {@link AVKey#SIZE_FIXED} instead. */
    @Deprecated
    public final static String SIZE_FIXED = AVKey.SIZE_FIXED;
    /** @deprecated Use {@link AVKey#SIZE_FIT_TEXT} instead. */
    @Deprecated
    public final static String SIZE_FIT_TEXT = AVKey.SIZE_FIT_TEXT;

    boolean isAlwaysOnTop();

    void setAlwaysOnTop(boolean alwaysOnTop);

    boolean isPickEnabled();

    void setPickEnabled(boolean enable);

    String getText();

    void setText(String text);

    AnnotationAttributes getAttributes();

    void setAttributes(AnnotationAttributes attrs);

    java.util.List<? extends Annotation> getChildren();

    void addChild(Annotation annotation);

    boolean removeChild(Annotation annotation);

    void removeAllChildren();

    AnnotationLayoutManager getLayout();

    void setLayout(AnnotationLayoutManager layoutManager);

    PickSupport getPickSupport();

    void setPickSupport(PickSupport pickSupport);

    Object getDelegateOwner();

    void setDelegateOwner(Object delegateOwner);

    java.awt.Dimension getPreferredSize(DrawContext dc);

    /**
     * Draws the annotation immediately on the specified DrawContext. Rendering is not be delayed by use of the
     * DrawContext's ordered mechanism, or any other delayed rendering mechanism. This is typically called by an
     * AnnotationRenderer while batch rendering. The GL should have its model view set to the identity matrix.
     *
     * @param dc the current DrawContext.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null.
     */
    void renderNow(DrawContext dc);

    /**
     * Draws the annotation without transforming to its screen position, or applying any scaling. This Annotation is
     * draw with the specified width, height, and opacity. The GL should have its model view set to whatever
     * transformation is desired.
     *
     * @param dc           the current DrawContext.
     * @param width        the width of the Annotation.
     * @param height       the height of the Annotation.
     * @param opacity      the opacity of the Annotation.
     * @param pickPosition the picked Position assigned to the Annotation, if picking is enabled.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null.
     */
    void draw(DrawContext dc, int width, int height, double opacity, Position pickPosition);

    /**
     * Get the annotation bounding {@link java.awt.Rectangle} using OGL coordinates - bottom-left corner x and y
     * relative to the {@link WorldWindow} bottom-left corner, and the annotation callout width and height.
     * <p>
     * The annotation offset from it's reference point is factored in such that the callout leader shape and reference
     * point are included in the bounding rectangle.
     *
     * @param dc the current DrawContext.
     *
     * @return the annotation bounding {@link java.awt.Rectangle} using OGL viewport coordinates.
     *
     * @throws IllegalArgumentException if <code>dc</code> is null.
     */
    java.awt.Rectangle getBounds(DrawContext dc);

    /**
     * Returns the minimum eye altitude, in meters, for which the annotation is displayed.
     *
     * @return the minimum altitude, in meters, for which the annotation is displayed.
     *
     * @see #setMinActiveAltitude(double)
     * @see #getMaxActiveAltitude()
     */
    double getMinActiveAltitude();

    /**
     * Specifies the minimum eye altitude, in meters, for which the annotation is displayed.
     *
     * @param minActiveAltitude the minimum altitude, in meters, for which the annotation is displayed.
     *
     * @see #getMinActiveAltitude()
     * @see #setMaxActiveAltitude(double)
     */
    void setMinActiveAltitude(double minActiveAltitude);

    /**
     * Returns the maximum eye altitude, in meters, for which the annotation is displayed.
     *
     * @return the maximum altitude, in meters, for which the annotation is displayed.
     *
     * @see #setMaxActiveAltitude(double)
     * @see #getMinActiveAltitude()
     */
    double getMaxActiveAltitude();

    /**
     * Specifies the maximum eye altitude, in meters, for which the annotation is displayed.
     *
     * @param maxActiveAltitude the maximum altitude, in meters, for which the annotation is displayed.
     *
     * @see #getMaxActiveAltitude()
     * @see #setMinActiveAltitude(double)
     */
    void setMaxActiveAltitude(double maxActiveAltitude);
}
