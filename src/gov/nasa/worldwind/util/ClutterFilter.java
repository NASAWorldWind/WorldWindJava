/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.render.*;

import java.util.List;

/**
 * Provides a mechanism to track the screen region of rendered items and determine whether that region overlaps with
 * regions already rendered. Used by global text decluttering.
 *
 * @author tag
 * @version $Id: ClutterFilter.java 726 2012-08-29 03:16:03Z tgaskins $
 */
public interface ClutterFilter
{
    /**
     * Applies the filter for a specified list of {@link Declutterable} shapes.
     *
     * @param dc     the current draw context.
     * @param shapes the shapes to declutter.
     */
    void apply(DrawContext dc, List<Declutterable> shapes);
}
