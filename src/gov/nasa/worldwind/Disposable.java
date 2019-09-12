/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

/**
 * @author tag
 * @version $Id: Disposable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Disposable
{
    /** Disposes of any internal resources allocated by the object. */
    public void dispose();
}
