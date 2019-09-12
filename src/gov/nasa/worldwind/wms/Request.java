/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.wms;

import gov.nasa.worldwind.util.*;

import java.net.*;
import java.util.*;

/**
 * This class provides a means to construct an OGC web service request, such as WMS GetMap or WFS GetCapabilities.
 *
 * @author tag
 * @version $Id: Request.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class Request
{
    private URI uri;

    // Use a TreeMap to hold the query params so that they'll always be attached to the
    // URL query string in the same order. This allows a simple string comparison to
    // determine whether two url strings address the same document.
    private TreeMap<String, String> queryParams = new TreeMap<String, String>();

    /** Constructs a request for the default service, WMS. */
    protected Request()
    {
        this.initialize(null);
    }

    /**
     * Constructs a request for the default service, WMS, and a specified server.
     *
     * @param uri the address of the web service. May be null when this constructor invoked by subclasses.
     *
     * @throws URISyntaxException if the web service address is not a valid URI.
     */
    protected Request(URI uri) throws URISyntaxException
    {
        this(uri, null);
    }

    /**
     * Constructs a request for a specified service at a specified server.
     *
     * @param uri     the address of the web service. May be null.
     * @param service the service name. Common names are WMS, WFS, WCS, etc. May by null when this constructor is
     *                invoked by subclasses.
     *
     * @throws URISyntaxException if the web service address is not a valid URI.
     */
    protected Request(URI uri, String service) throws URISyntaxException
    {
        if (uri != null)
        {
            try
            {
                this.setUri(uri);
            }
            catch (URISyntaxException e)
            {
                Logging.logger().fine(Logging.getMessage("generic.URIInvalid", uri.toString()));
                throw e;
            }
        }

        this.initialize(service);
    }

    /**
     * Copy constructor. Performs a shallow copy.
     *
     * @param sourceRequest the request to copy.
     *
     * @throws IllegalArgumentException if copy source is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */
    public Request(Request sourceRequest) throws URISyntaxException
    {
        if (sourceRequest == null)
        {
            String message = Logging.getMessage("nullValue.CopyConstructorSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        sourceRequest.copyParamsTo(this);
        this.setUri(sourceRequest.getUri());
    }

    protected void initialize(String service)
    {
        this.queryParams.put("SERVICE", service != null ? service : "WMS");
        this.queryParams.put("EXCEPTIONS", "application/vnd.ogc.se_xml");
    }

    private void copyParamsTo(Request destinationRequest)
    {
        if (destinationRequest == null)
        {
            String message = Logging.getMessage("nullValue.CopyTargetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (Map.Entry<String, String> entry : this.queryParams.entrySet())
        {
            destinationRequest.setParam((String) ((Map.Entry) entry).getKey(), (String) ((Map.Entry) entry).getValue());
        }
    }

    protected void setUri(URI uri) throws URISyntaxException
    {
        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                this.buildQueryString(uri.getQuery()), null);
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().fine(message);
            throw e;
        }
    }

    public String getRequestName()
    {
        return this.getParam("REQUEST");
    }

    public String getVersion()
    {
        return this.getParam("VERSION");
    }

    public void setVersion(String version)
    {
        if (version == null)
        {
            String message = Logging.getMessage("nullValue.WMSVersionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setParam("VERSION", version);
    }

    public String getService()
    {
        return this.getParam("SERVICE");
    }

    public void setService(String service)
    {
        if (service == null)
        {
            String message = Logging.getMessage("nullValue.WMSServiceNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setParam("SERVICE", service);
    }

    public void setParam(String key, String value)
    {
        if (key != null)
            this.queryParams.put(key, value);
    }

    public String getParam(String key)
    {
        return key != null ? this.queryParams.get(key) : null;
    }

    public URI getUri() throws URISyntaxException
    {
        if (this.uri == null)
            return null;

        try
        {
            return new URI(this.uri.getScheme(), this.uri.getUserInfo(), this.uri.getHost(), this.uri.getPort(),
                uri.getPath(), this.buildQueryString(uri.getQuery()), null);
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().fine(message);
            throw e;
        }
    }

    private String buildQueryString(String existingQueryString)
    {
        StringBuffer queryString = new StringBuffer(existingQueryString != null ? existingQueryString : "");

        if (queryString.length() > 1 && queryString.lastIndexOf("&") != queryString.length() - 1)
            queryString = queryString.append("&");

        for (Map.Entry<String, String> entry : this.queryParams.entrySet())
        {
            if (((Map.Entry) entry).getKey() != null && ((Map.Entry) entry).getValue() != null)
            {
                queryString.append(((Map.Entry) entry).getKey());
                queryString.append("=");
                queryString.append(((Map.Entry) entry).getValue());
                queryString.append("&");
            }
        }

        // Remove a trailing ampersand
        if (WWUtil.isEmpty(existingQueryString))
        {
            int trailingAmpersandPosition = queryString.lastIndexOf("&");
            if (trailingAmpersandPosition >= 0)
                queryString.deleteCharAt(trailingAmpersandPosition);
        }

        return queryString.toString();
    }

    public String toString()
    {
        String errorMessage = "Error converting wms-request URI to string.";
        try
        {
            java.net.URI fullUri = this.getUri();
            return fullUri != null ? fullUri.toString() : errorMessage;
        }
        catch (URISyntaxException e)
        {
            return errorMessage;
        }
    }
}
