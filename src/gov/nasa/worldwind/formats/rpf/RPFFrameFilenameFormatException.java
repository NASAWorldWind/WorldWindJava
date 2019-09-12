/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

/**
 * @author dcollins
 * @version $Id: RPFFrameFilenameFormatException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFFrameFilenameFormatException extends IllegalArgumentException
{
    public RPFFrameFilenameFormatException()
    {
    }

    public RPFFrameFilenameFormatException(String message)
    {
        super(message);
    }

    public RPFFrameFilenameFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RPFFrameFilenameFormatException(Throwable cause)
    {
        super(cause);
    }
}
