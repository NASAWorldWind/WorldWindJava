/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

/**
 * @author tag
 * @version $Id: PreRenderable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface PreRenderable
{
    void preRender(DrawContext dc);
}
