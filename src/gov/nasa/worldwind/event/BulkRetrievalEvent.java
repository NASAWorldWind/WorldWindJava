/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.event;

import gov.nasa.worldwind.retrieve.BulkRetrievable;

/**
 * Notifies of bulk retrieval events.
 *
 * @author tag
 * @version $Id: BulkRetrievalEvent.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.retrieve.BulkRetrievable
 */
public class BulkRetrievalEvent extends WWEvent
{
    /** Constant indicating retrieval failure. */
    public static String RETRIEVAL_FAILED = "gov.nasa.worldwind.retrieve.BulkRetrievable.RetrievalFailed";

    /** Constant indicating retrieval success. */
    public static String RETRIEVAL_SUCCEEDED = "gov.nasa.worldwind.retrieve.BulkRetrievable.RetrievalSucceeded";

    protected String eventType;
    protected String item;

    /**
     * Creates a new event.
     *
     * @param source    the event source, typically either a tiled image layer, elevation model or placename layer.
     * @param eventType indicates success or failure. One of {@link #RETRIEVAL_SUCCEEDED} or {@link #RETRIEVAL_FAILED}.
     * @param item      the cache location of the item whose retrieval succeeded or failed.
     *
     * @see gov.nasa.worldwind.retrieve.BulkRetrievable
     */
    public BulkRetrievalEvent(BulkRetrievable source, String eventType, String item)
    {
        super(source);

        this.eventType = eventType;
        this.item = item;
    }

    /**
     * Returns the event source.
     *
     * @return the event source, typically either a tiled image layer, elevation model or placename layer.
     *
     * @see gov.nasa.worldwind.retrieve.BulkRetrievable
     */
    public BulkRetrievable getSource()
    {
        return super.getSource() instanceof BulkRetrievable ? (BulkRetrievable) super.getSource() : null;
    }

    /**
     * Returns the event type, one of {@link #RETRIEVAL_SUCCEEDED} or {@link #RETRIEVAL_FAILED}.
     *
     * @return the event type.
     */
    public String getEventType()
    {
        return eventType;
    }

    /**
     * Returns the filestore location of the item whose retrieval succeeded or failed.
     *
     * @return the filestore location of the item.
     */
    public String getItem()
    {
        return item;
    }
}
