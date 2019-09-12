/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.event;

/**
 * Listener for general purpose message events.
 *
 * @author pabercrombie
 * @version $Id: MessageListener.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface MessageListener
{
    /**
     * Invoked when a message is received.
     *
     * @param msg The message that was received.
     */
    void onMessage(Message msg);
}
