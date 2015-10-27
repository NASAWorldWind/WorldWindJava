/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.exception;

/**
 * Thrown to indicate a service has failed.
 *
 * @author tag
 * @version $Id: ServiceException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ServiceException extends WWRuntimeException
{
    public ServiceException(String message)
    {
        super(message);
    }
}
