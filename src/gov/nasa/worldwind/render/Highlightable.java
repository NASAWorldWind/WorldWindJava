/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

/**
 * An interface to control a shape's highlighting. Shapes implementing this interface have their own highlighting
 * behaviors and attributes and the means for setting them.
 *
 * @author tag
 * @version $Id: Highlightable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Highlightable
{
    /**
     * Indicates whether to highlight the shape.
     *
     * @return true to highlight the shape, otherwise false.
     */
    boolean isHighlighted();

    /**
     * Specifies whether to highlight the shape.
     *
     * @param highlighted true to highlight the shape, otherwise false.
     */
    void setHighlighted(boolean highlighted);
}
