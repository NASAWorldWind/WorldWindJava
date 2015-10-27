/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.net.*;
import java.nio.*;

/**
 * Utilites for working with points of interest and gazetteers.
 *
 * @author tag
 * @version $Id: POIUtils.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class POIUtils
{
    protected static final String DEFAULT_CHARSET_NAME = "UTF-8";

    /**
     * Invoke a point-of-interest service.
     *
     * @param urlString the URL to use to invoke the service.
     * @return the service results.
     * @throws NoItemException  if <code>HTTP_BAD_REQUEST<code> is returned from the service.
     * @throws ServiceException if there is a problem invoking the service or retrieving its results.
     */
    public static String callService(String urlString) throws NoItemException, ServiceException
    {
        if (urlString == null || urlString.length() < 1)
            return null;

        InputStream inputStream = null;

        try
        {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            HttpURLConnection htpc = (HttpURLConnection) connection;
            int responseCode = htpc.getResponseCode();
            String responseMessage = htpc.getResponseMessage();
            String contentType = htpc.getContentType();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                inputStream = connection.getInputStream();
                ByteBuffer buffer = WWIO.readStreamToBuffer(inputStream);
                String charsetName = getCharsetName(contentType);
                return decodeBuffer(buffer, charsetName);
            }
            else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
            {
                throw new NoItemException(responseMessage);
            }
            else
            {
                throw new ServiceException(responseMessage);
            }
        }
        catch (MalformedURLException e) // occurs only if protocol of URL is unknown
        {
            String msg = Logging.getMessage("generic.MalformedURL", urlString);
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            throw new WWRuntimeException(msg);
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("POI.ServiceError", urlString);
            Logging.logger().log(java.util.logging.Level.SEVERE, msg);
            throw new ServiceException(msg);
        }
        finally
        {
            WWIO.closeStream(inputStream, urlString);
        }
    }

    protected static String decodeBuffer(ByteBuffer buffer, String charsetName)
    {
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        try
        {
            return new String(bytes, charsetName);
        }
        catch (UnsupportedEncodingException e)
        {
            return new String(bytes);
        }
    }

    protected static String getCharsetName(String contentType)
    {
        if (contentType == null || contentType.toLowerCase().indexOf("charset") == -1)
            return DEFAULT_CHARSET_NAME;

        String[] pairs = contentType.split(";");
        for (String pair : pairs)
        {
            if (pair.toLowerCase().trim().startsWith("charset"))
            {
                String[] av = pair.split("=");
                if (av.length > 1 && av[1].trim().length() > 0)
                    return av[1].trim();
            }
        }

        return DEFAULT_CHARSET_NAME;
    }
}
