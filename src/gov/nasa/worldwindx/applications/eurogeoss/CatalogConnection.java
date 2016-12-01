/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.eurogeoss;

import gov.nasa.worldwind.util.*;

import javax.xml.stream.*;
import java.io.*;
import java.net.*;

/**
 * @author dcollins
 * @version $Id: CatalogConnection.java 1584 2013-09-05 23:39:15Z dcollins $
 */
public class CatalogConnection
{
    protected String serviceUrl;
    protected int connectTimeout = 10000;
    protected int readTimeout = 10000;

    public CatalogConnection(String serviceUrl)
    {
        if (serviceUrl == null)
        {
            String msg = Logging.getMessage("nullValue.ServiceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.serviceUrl = serviceUrl;
    }

    public String getServiceUrl()
    {
        return this.serviceUrl;
    }

    public int getConnectTimeout()
    {
        return this.connectTimeout;
    }

    public void setConnectTimeout(int timeout)
    {
        this.connectTimeout = timeout;
    }

    public int getReadTimeout()
    {
        return this.readTimeout;
    }

    public void setReadTimeout(int timeout)
    {
        this.readTimeout = timeout;
    }

    public GetRecordsResponse getRecords(GetRecordsRequest request) throws IOException, XMLStreamException
    {
        if (request == null)
        {
            String msg = Logging.getMessage("nullValue.RequestIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        HttpURLConnection conn = null;
        GetRecordsResponse response = null;

        try
        {
            URL url = new URL(this.serviceUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(this.getConnectTimeout());
            conn.setReadTimeout(this.getReadTimeout());
            response = this.sendGetRecordsRequest(request, conn);
        }
        finally
        {
            if (conn != null)
                conn.disconnect();
        }

        return response;
    }

    protected GetRecordsResponse sendGetRecordsRequest(GetRecordsRequest request, HttpURLConnection connection)
        throws IOException, XMLStreamException
    {
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml; charset=utf-8");

        OutputStream out = connection.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(out), "UTF-8");
        writer.write(request.toXMLString());
        writer.close();

        if (Thread.interrupted())
        {
            return null;
        }

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
        {
            String msg = Logging.getMessage("HTTP.ResponseCode", connection.getResponseCode(), connection.getURL());
            throw new IOException(msg);
        }

        InputStream in = connection.getInputStream();
        return this.parseResponse(in);
    }

    protected GetRecordsResponse parseResponse(InputStream in) throws XMLStreamException
    {
        XMLEventReader reader = null;

        try
        {
            reader = WWXML.openEventReaderStream(new BufferedInputStream(in));
            return new GetRecordsResponse(reader);
        }
        finally
        {
            if (reader != null)
                reader.close();
        }
    }
}
