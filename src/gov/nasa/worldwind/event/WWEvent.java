/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.event;

import java.util.EventObject;

/**
 * WWEvent is the base class which all WorldWind event objects derive from. It extends Java's base {@link
 * java.util.EventObject} by adding the capability to consume the event by calling {@link #consume()}. Consuming a
 * WWEvent prevents is from being processed in the default manner by the source that originated the event. If the event
 * cannot be consumed, calling {@code consume()} has no effect, though {@link #isConsumed()} returns whether or not
 * {@code consume()} has been called.
 *
 * @author dcollins
 * @version $Id: WWEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WWEvent extends EventObject
{
    /** Denotes whether or not the event has been consumed. Initially {@code false}. */
    protected boolean consumed;

    /**
     * Creates a new WWEvent with the object that originated the event.
     *
     * @param source the object that originated the event.
     *
     * @throws IllegalArgumentException if the source is {@code null}.
     */
    public WWEvent(Object source)
    {
        super(source);
    }

    /**
     * Consumes the event so it will not be processed in the default manner by the source which originated it. This does
     * nothing if the event cannot be consumed.
     */
    public void consume()
    {
        this.consumed = true;
    }

    /**
     * Returns whether or not the event has been consumed.
     * <p>
     * Note: if the event cannot be consumed, this still returns {@code true} if {@link #consume()} has been called,
     * though this has no effect.
     *
     * @return {@code true} if the event has been consumed, and {@code false} otherwise.
     */
    public boolean isConsumed()
    {
        return this.consumed;
    }
}
