/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVList;

/**
 * General factory interface.
 *
 * @author tag
 * @version $Id: Factory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Factory
{
    /**
     * Creates an object from a general configuration source.
     *
     * @param configSource the configuration source.
     * @param params       properties to apply during object creation.
     *
     * @return the new object.
     *
     * @throws IllegalArgumentException if the configuration source is null or an empty string.
     * @throws gov.nasa.worldwind.exception.WWUnrecognizedException
     *                                  if the type of source or some object-specific value is unrecognized.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if object creation fails. The exception indicating the source of the failure is
     *                                  included as the {@link Exception#initCause(Throwable)}.
     */
    Object createFromConfigSource(Object configSource, AVList params);
}
