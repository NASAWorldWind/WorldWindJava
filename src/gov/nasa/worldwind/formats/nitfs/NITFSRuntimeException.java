/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.formats.nitfs;

/**
 * @author Lado Garakanidze
 * @version $Id: NITFSRuntimeException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class NITFSRuntimeException extends java.lang.RuntimeException
{
    public NITFSRuntimeException()
    {
        super((String) null);
        log();
    }

    public NITFSRuntimeException(String params)
    {
        super(null + params);
        log();
    }

    public NITFSRuntimeException(Throwable throwable)
    {
        super(null, throwable);
        log();
    }

    public NITFSRuntimeException(String params, Throwable throwable)
    {
        super(null + params, throwable);
        log();
    }

    // TODO: Calling the logger from here causes the wrong method to be listed in the log record. Must call the
    // logger from the site with the problem and generating the exception.
    private void log()
    {
    }
}