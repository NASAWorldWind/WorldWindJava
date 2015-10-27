/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.*;

/**
 * This class of static methods provides the interface to logging for World Wind components. Logging is performed via
 * {@link java.util.logging}. The default logger name is <code>gov.nasa.worldwind</code>. The logger name is
 * configurable via {@link gov.nasa.worldwind.Configuration}.
 *
 * @author tag
 * @version $Id: Logging.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.Configuration
 * @see java.util.logging
 */
public class Logging
{
    protected static final String MESSAGE_BUNDLE_NAME = Logging.class.getPackage().getName() + ".MessageStrings";
    protected static final int MAX_MESSAGE_REPEAT = Configuration.getIntegerValue(AVKey.MAX_MESSAGE_REPEAT, 10);

    private Logging()
    {
    } // Prevent instantiation

    /**
     * Returns the World Wind logger.
     *
     * @return The logger.
     */
    public static Logger logger()
    {
        try
        {
            // The Configuration singleton may not be established yet, so catch the exception that occurs if it's not
            // and use the default logger name.
            String loggerName = Configuration.getStringValue(AVKey.LOGGER_NAME, Configuration.DEFAULT_LOGGER_NAME);
            return logger(loggerName);
        }
        catch (Exception e)
        {
            return logger(Configuration.DEFAULT_LOGGER_NAME);
        }
    }

    /**
     * Returns a specific logger. Does not access {@link gov.nasa.worldwind.Configuration} to determine the configured
     * World Wind logger.
     * <p/>
     * This is needed by {@link gov.nasa.worldwind.Configuration} to avoid calls back into itself when its singleton
     * instance is not yet instantiated.
     *
     * @param loggerName the name of the logger to use.
     *
     * @return The logger.
     */
    public static Logger logger(String loggerName)
    {
        return Logger.getLogger(loggerName != null ? loggerName : "", MESSAGE_BUNDLE_NAME);
    }

    /**
     * Retrieves a message from the World Wind message resource bundle.
     *
     * @param property the property identifying which message to retrieve.
     *
     * @return The requested message.
     */
    public static String getMessage(String property)
    {
        try
        {
            return (String) ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, Locale.getDefault()).getObject(property);
        }
        catch (Exception e)
        {
            String message = "Exception looking up message from bundle " + MESSAGE_BUNDLE_NAME;
            logger().log(java.util.logging.Level.SEVERE, message, e);
            return message;
        }
    }

    /**
     * Retrieves a message from the World Wind message resource bundle formatted with a single argument. The argument is
     * inserted into the message via {@link java.text.MessageFormat}.
     *
     * @param property the property identifying which message to retrieve.
     * @param arg      the single argument referenced by the format string identified <code>property</code>.
     *
     * @return The requested string formatted with the argument.
     *
     * @see java.text.MessageFormat
     */
    public static String getMessage(String property, String arg)
    {
        return arg != null ? getMessage(property, (Object) arg) : getMessage(property);
    }

    /**
     * Retrieves a message from the World Wind message resource bundle formatted with specified arguments. The arguments
     * are inserted into the message via {@link java.text.MessageFormat}.
     *
     * @param property the property identifying which message to retrieve.
     * @param args     the arguments referenced by the format string identified <code>property</code>.
     *
     * @return The requested string formatted with the arguments.
     *
     * @see java.text.MessageFormat
     */
    public static String getMessage(String property, Object... args)
    {
        String message;

        try
        {
            message = (String) ResourceBundle.getBundle(MESSAGE_BUNDLE_NAME, Locale.getDefault()).getObject(property);
        }
        catch (Exception e)
        {
            message = "Exception looking up message from bundle " + MESSAGE_BUNDLE_NAME;
            logger().log(Level.SEVERE, message, e);
            return message;
        }

        try
        {
            // TODO: This is no longer working with more than one arg in the message string, e.g., {1}
            return args == null ? message : MessageFormat.format(message, args);
        }
        catch (IllegalArgumentException e)
        {
            message = "Message arguments do not match format string: " + property;
            logger().log(Level.SEVERE, message, e);
            return message;
        }
    }

    /**
     * Indicates the maximum number of times the same log message should be repeated when generated in the same context,
     * such as within a loop over renderables when operations in the loop consistently fail.
     *
     * @return the maximum number of times to repeat a message.
     */
    public static int getMaxMessageRepeatCount()
    {
        return MAX_MESSAGE_REPEAT;
    }
}
