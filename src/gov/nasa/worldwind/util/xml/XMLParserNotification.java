/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.Logging;

import javax.xml.stream.events.XMLEvent;
import java.beans.PropertyChangeEvent;

/**
 * This class identifies the type and data content of notifications from parsers and parser contexts. Notifications are
 * sent to inform of important occurrences that occur during parsing, such as exceptions and unrecognized element
 * types.
 *
 * @author tag
 * @version $Id: XMLParserNotification.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.util.xml.XMLEventParserContext#setNotificationListener(XMLParserNotificationListener)
 * @see gov.nasa.worldwind.util.xml.XMLParserNotificationListener
 */
public class XMLParserNotification extends PropertyChangeEvent
{
    /** A notification type indicating that an exception occurred during parsing. */
    public static final String EXCEPTION = "gov.nasa.worldwind.util.xml.XMLParserNotification.Exception";
    /** A notification type indicating that a parser encounter an element it did not recognize. */
    public static final String UNRECOGNIZED = "gov.nasa.worldwind.util.xml.XMLParserNotification.Unrecognized";
    /** Indicates the cause of the notification. */
    protected final String notificationType;
    /** The message sent from the object sending the notification. */
    protected final String message;
    /** The <code>XMLEvent</code> associated with the notification, if any. */
    protected final XMLEvent event;
    /** For exception notifications, the exception that occurred. */
    protected Exception exception;
    /** The object initiating the notification. */
    protected Object notificationSource;

    /**
     * Construct a notification object.
     *
     * @param source           the object initiating the notification.
     * @param notificationType the notification type, such as {@link #EXCEPTION} or {@link #UNRECOGNIZED}.
     * @param event            if an event is associated with the notification, that event. May be null.
     * @param msg              a message from the notification source suitable for logging.
     * @param oldValue         any old value associated with the notification. Not typically used.
     * @param newValue         any new value associated with the notification. if this is an exception notification, the
     *                         exception that occurred is passed via this parameter. May be null.
     */
    public XMLParserNotification(Object source, String notificationType, XMLEvent event, String msg, Object oldValue,
        Object newValue)
    {
        super(source, notificationType, oldValue, newValue);

        this.notificationSource = source;
        this.notificationType = notificationType;
        this.event = event;
        this.message = msg;

        if (newValue instanceof Exception)
            this.exception = (Exception) newValue;
    }

    /**
     * Return the event associated with the notification, if any.
     *
     * @return the event associated with the exception.
     */
    public XMLEvent getEvent()
    {
        return this.event;
    }

    /**
     * The message associated with the exception, suitable for writing to a log.
     *
     * @return the message associated with the exception.
     */
    public String getMessage()
    {
        return this.message;
    }

    /**
     * The notification type.
     *
     * @return the notification type.
     *
     * @see #EXCEPTION
     * @see #UNRECOGNIZED
     */
    public String getNotificationType()
    {
        return this.notificationType;
    }

    /**
     * Return the exception associated with an exception notification.
     *
     * @return the associated exception, or null if this is not an exception notification.
     */
    public Exception getException()
    {
        return this.exception;
    }

    /**
     * Return the object initiating the notification.
     *
     * @return the object initiating the exception.
     */
    public Object getSource()
    {
        return this.notificationSource;
    }

    /**
     * Respecifies the notification source. Used to forward the notification so that it appears to be from the new
     * source.
     *
     * @param notificationSource the source to assign the exception.
     */
    public void setSource(Object notificationSource)
    {
        this.notificationSource = notificationSource;
    }

    @Override
    public String toString()
    {
        String msg;

        if (this.event != null)
        {
            msg = Logging.getMessage(this.message, this.event.toString(),
                this.event.getLocation().getLineNumber(),
                this.event.getLocation().getColumnNumber(),
                this.event.getLocation().getCharacterOffset());
        }
        else
        {
            msg = Logging.getMessage(this.message, "", "", "");
        }

        return this.notificationType.substring(1 + this.notificationType.lastIndexOf(".")) + ": " + msg;
    }
}
