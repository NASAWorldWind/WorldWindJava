/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.exception;

/**
 * @author Tom Gaskins
 * @version $Id: WWRuntimeException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWRuntimeException extends RuntimeException
{
    public WWRuntimeException()
    {
    }

    public WWRuntimeException(String s)
    {
        super(s);
    }

    public WWRuntimeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public WWRuntimeException(Throwable throwable)
    {
        super(throwable);
    }
}
