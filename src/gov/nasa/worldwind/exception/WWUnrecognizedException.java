/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.exception;

/**
 * Indicates that a value, request or other item or action is not recognized.
 *
 * @author tag
 * @version $Id: WWUnrecognizedException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWUnrecognizedException extends WWRuntimeException
{
    /**
     * Construct an exception with a message string.
     *
     */
    public WWUnrecognizedException()
    {
        super();
    }

    /**
     * Construct an exception with a message string and a intial-cause exception.
     *
     * @param t   the exception causing this exception.
     */
    public WWUnrecognizedException(Throwable t)
    {
        super(t);
    }
}
