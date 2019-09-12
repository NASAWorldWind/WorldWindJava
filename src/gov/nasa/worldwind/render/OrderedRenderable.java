/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

/**
 * @author tag
 * @version $Id: OrderedRenderable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface OrderedRenderable extends Renderable
{
    /**
     * Returns the ordered renderable's distance from the current view's eye point. Intended to be used only to sort a
     * list of ordered renderables according to eye distance, and only during frame generation when a view is active.
     *
     * @return the distance of the ordered renderable from the current view's eye point.
     */
    double getDistanceFromEye();

    /**
     * Executes a pick of the ordered renderable.
     *
     * @param dc        the current draw context.
     * @param pickPoint the pick point.
     */
    public void pick(DrawContext dc, java.awt.Point pickPoint);
}
