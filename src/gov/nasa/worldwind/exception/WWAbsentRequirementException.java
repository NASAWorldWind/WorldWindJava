/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.exception;

/**
 * @author tag
 * @version $Id: WWAbsentRequirementException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWAbsentRequirementException extends WWRuntimeException
{
    public WWAbsentRequirementException()
    {
    }

    public WWAbsentRequirementException(String s)
    {
        super(s);
    }

    public WWAbsentRequirementException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public WWAbsentRequirementException(Throwable throwable)
    {
        super(throwable);
    }
}
