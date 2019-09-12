/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.exception;

/**
 * Thrown to indicate that an item is not available from a request or search.
 *
 * @author tag
 * @version $Id: NoItemException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NoItemException extends WWRuntimeException
{
    public NoItemException(String string)
    {
        super(string);
    }
}
