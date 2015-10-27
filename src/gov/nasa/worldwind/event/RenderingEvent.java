/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: RenderingEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RenderingEvent extends WWEvent
{
    public static final String BEFORE_RENDERING = "gov.nasa.worldwind.RenderingEvent.BeforeRendering";
    public static final String BEFORE_BUFFER_SWAP = "gov.nasa.worldwind.RenderingEvent.BeforeBufferSwap";
    public static final String AFTER_BUFFER_SWAP = "gov.nasa.worldwind.RenderingEvent.AfterBufferSwap";

    private String stage;

    public RenderingEvent(Object source, String stage)
    {
        super(source);
        this.stage = stage;
    }

    public String getStage()
    {
        return this.stage != null ? this.stage : "gov.nasa.worldwind.RenderingEvent.UnknownStage";
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + " "
            + (this.stage != null ? this.stage : Logging.getMessage("generic.Unknown"));
    }
}
