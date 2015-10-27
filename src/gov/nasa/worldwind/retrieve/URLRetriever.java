/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.util.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Level;
import java.util.regex.*;
import java.util.zip.*;

/**
 * @author Tom Gaskins
 * @version $Id: URLRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class URLRetriever extends WWObjectImpl implements Retriever
{
    /**
     * Applications never need to use this constant. It provides compatibility with very old World Wind tile servers
     * that deliver zipped content without identifying the content type as other than application/zip. In these cases,
     * the object requesting the content must know the content type to expect, and also requires that the zip file be
     * opened and only its first entry returned.
     */
    public static final String EXTRACT_ZIP_ENTRY = "URLRetriever.ExtractZipEntry";

    protected volatile String state = RETRIEVER_STATE_NOT_STARTED;
    protected volatile int contentLength = 0;
    protected AtomicInteger contentLengthRead = new AtomicInteger(0);
    protected volatile String contentType;
    protected AtomicLong expiration = new AtomicLong(0);
    protected volatile ByteBuffer byteBuffer;
    protected volatile URLConnection connection;
    protected final URL url;
    protected final RetrievalPostProcessor postProcessor;
    protected int connectTimeout = Configuration.getIntegerValue(AVKey.URL_CONNECT_TIMEOUT, 8000);
    protected int readTimeout = Configuration.getIntegerValue(AVKey.URL_READ_TIMEOUT, 5000);
    protected int staleRequestLimit = -1;
    protected long submitTime;
    protected long beginTime;
    protected long endTime;

    /**
     * Create the appropriate retriever for a URL's protocol.
     *
     * @param url           the url that will be the source of the retrieval.
     * @param postProcessor the retriever's post-processor.
     *
     * @return a retriever for the protocol specified in the url, or null if no retriever exists for the protocol.
     *
     * @throws IllegalArgumentException if the url is null.
     */
    public static URLRetriever createRetriever(URL url, RetrievalPostProcessor postProcessor)
    {
        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String protocol = url.getProtocol();

        if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))
            return new HTTPRetriever(url, postProcessor);
        else if ("jar".equalsIgnoreCase(protocol))
            return new JarRetriever(url, postProcessor);
        else
            return null;
    }

    /**
     * @param url           the URL of the resource to retrieve.
     * @param postProcessor the retrieval post-processor to invoke when the resource is retrieved. May be null.
     *
     * @throws IllegalArgumentException if <code>url</code>.
     */
    public URLRetriever(URL url, RetrievalPostProcessor postProcessor)
    {
        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.url = url;
        this.postProcessor = postProcessor;
    }

    public final URL getUrl()
    {
        return url;
    }

    public final int getContentLength()
    {
        return this.contentLength;
    }

    protected void setContentLengthRead(int length)
    {
        this.contentLengthRead.set(length);
    }

    public final int getContentLengthRead()
    {
        return this.contentLengthRead.get();
    }

    public final String getContentType()
    {
        return this.contentType;
    }

    /**
     * {@inheritDoc} Expiration time is determined by either the Expires header, or the max-age directive of the
     * Cache-Control header. Cache-Control has priority if both headers are specified (see section 14.9.3 of the <a
     * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html" target="_blank">HTTP Specification</a>).
     */
    public long getExpirationTime()
    {
        return this.expiration.get();
    }

    public final ByteBuffer getBuffer()
    {
        return this.byteBuffer;
    }

    public final String getName()
    {
        return this.url.toString();
    }

    public final URL getURL()
    {
        return this.url;
    }

    public final String getState()
    {
        return this.state;
    }

    protected final URLConnection getConnection()
    {
        return this.connection;
    }

    public final RetrievalPostProcessor getPostProcessor()
    {
        return postProcessor;
    }

    public final int getConnectTimeout()
    {
        return connectTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    public int getStaleRequestLimit()
    {
        return staleRequestLimit;
    }

    public void setStaleRequestLimit(int staleRequestLimit)
    {
        this.staleRequestLimit = staleRequestLimit;
    }

    public final void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public long getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(long submitTime)
    {
        this.submitTime = submitTime;
    }

    public long getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(long beginTime)
    {
        this.beginTime = beginTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setEndTime(long endTime)
    {
        this.endTime = endTime;
    }

    public final Retriever call() throws Exception
    {
        if (this.interrupted())
            return this;

        try
        {
            this.setState(RETRIEVER_STATE_STARTED);

            if (!this.interrupted())
            {
                this.setState(RETRIEVER_STATE_CONNECTING);
                this.connection = this.openConnection();
            }

            if (!this.interrupted())
            {
                this.setState(RETRIEVER_STATE_READING);
                this.byteBuffer = this.read();
            }

            if (!this.interrupted())
                this.setState(RETRIEVER_STATE_SUCCESSFUL);

            WorldWind.getNetworkStatus().logAvailableHost(this.url);
        }
        catch (UnknownHostException e)
        {
            this.setState(RETRIEVER_STATE_ERROR);
            WorldWind.getNetworkStatus().logUnavailableHost(this.url);
            throw e;
        }
        catch (SocketException e)
        {
            this.setState(RETRIEVER_STATE_ERROR);
            WorldWind.getNetworkStatus().logUnavailableHost(this.url);
            throw e;
        }
        catch (ClosedByInterruptException e)
        {
            this.interrupted();
        }
        catch (Exception e)
        {
            this.setState(RETRIEVER_STATE_ERROR);
            if (!(e instanceof java.net.SocketTimeoutException))
            {
                Logging.logger().log(Level.SEVERE,
                    Logging.getMessage("URLRetriever.ErrorAttemptingToRetrieve", this.url.toString()), e);
            }
            throw e;
        }
        finally
        {
            this.end();
        }

        return this;
    }

    protected void setState(String state)
    {
        String oldState = this.state;
        this.state = state;
        this.firePropertyChange(AVKey.RETRIEVER_STATE, oldState, this.state);
    }

    protected boolean interrupted()
    {
        if (Thread.currentThread().isInterrupted())
        {
            this.setState(RETRIEVER_STATE_INTERRUPTED);
            String message = Logging.getMessage("URLRetriever.RetrievalInterruptedFor", this.url.toString());
            Logging.logger().fine(message);
            return true;
        }
        return false;
    }

    protected URLConnection openConnection() throws IOException
    {
        try
        {
            Proxy proxy = WWIO.configureProxy();
            if (proxy != null)
                this.connection = this.url.openConnection(proxy);
            else
                this.connection = this.url.openConnection();
        }
        catch (java.io.IOException e)
        {
            Logging.logger().log(Level.SEVERE,
                Logging.getMessage("URLRetriever.ErrorOpeningConnection", this.url.toString()), e);
            throw e;
        }

        if (this.connection == null) // java.net.URL docs imply that this won't happen. We check anyway.
        {
            String message = Logging.getMessage("URLRetriever.NullReturnedFromOpenConnection", this.url);
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (this.connection instanceof HttpsURLConnection)
            this.configureSSLContext((HttpsURLConnection) this.connection);

        this.connection.setConnectTimeout(this.connectTimeout);
        this.connection.setReadTimeout(this.readTimeout);

        return connection;
    }

    protected void configureSSLContext(HttpsURLConnection connection)
    {
        SSLContext sslContext = (SSLContext) WorldWind.getValue(AVKey.HTTP_SSL_CONTEXT);

        if (sslContext != null)
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
    }

    protected void end() throws Exception
    {
        try
        {
            if (this.postProcessor != null)
            {
                this.byteBuffer = this.postProcessor.run(this);
            }
        }
        catch (Exception e)
        {
            this.setState(RETRIEVER_STATE_ERROR);
            Logging.logger().log(Level.SEVERE,
                Logging.getMessage("Retriever.ErrorPostProcessing", this.url.toString()), e);
            throw e;
        }
    }

    protected ByteBuffer read() throws Exception
    {
        try
        {
            ByteBuffer buffer = this.doRead(this.connection);
            if (buffer == null)
                this.contentLength = 0;
            return buffer;
        }
        catch (Exception e)
        {
            if (!(e instanceof java.net.SocketTimeoutException || e instanceof UnknownHostException
                || e instanceof SocketException))
            {
                Logging.logger().log(Level.SEVERE,
                    Logging.getMessage("URLRetriever.ErrorReadingFromConnection", this.url.toString()), e);
            }
            throw e;
        }
    }

    /**
     * @param connection the connection to read from.
     *
     * @return a buffer containing the content read from the connection
     *
     * @throws Exception                if <code>connection</code> is null or an exception occurs during reading.
     * @throws IllegalArgumentException if <code>connection</code> is null
     */
    protected ByteBuffer doRead(URLConnection connection) throws Exception
    {
        if (connection == null)
        {
            String msg = Logging.getMessage("nullValue.ConnectionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.contentLength = this.connection.getContentLength();

        ByteBuffer buffer;
        InputStream inputStream = null;
        try
        {
            inputStream = this.connection.getInputStream();
            if (inputStream == null)
            {
                Logging.logger().log(Level.SEVERE, "URLRetriever.InputStreamFromConnectionNull", connection.getURL());
                return null;
            }

            this.expiration.set(this.getExpiration(connection));

            // The legacy WW servers send data with application/zip as the content type, and the retrieval initiator is
            // expected to know what type the unzipped content is. This is a kludge, but we have to deal with it. So
            // automatically unzip the content if the content type is application/zip.
            this.contentType = connection.getContentType();
            if (this.contentType != null && this.contentType.equalsIgnoreCase("application/zip")
                && !WWUtil.isEmpty(this.getValue(EXTRACT_ZIP_ENTRY)))
                // Assume single file in zip and decompress it
                buffer = this.readZipStream(inputStream, connection.getURL());
            else
                buffer = this.readNonSpecificStream(inputStream, connection);
        }
        finally
        {
            WWIO.closeStream(inputStream, connection.getURL().toString());
        }

        return buffer;
    }

    protected ByteBuffer readNonSpecificStream(InputStream inputStream, URLConnection connection) throws IOException
    {
        if (inputStream == null)
        {
            String message = Logging.getMessage("URLRetriever.InputStreamNullFor", connection.getURL());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.contentLength < 1)
        {
            return readNonSpecificStreamUnknownLength(inputStream);
        }

        ReadableByteChannel channel = Channels.newChannel(inputStream);
        ByteBuffer buffer = ByteBuffer.allocate(this.contentLength);
//        System.out.println(this.contentLength + " bytes to read");

        int numBytesRead = 0;
        while (!this.interrupted() && numBytesRead >= 0 && numBytesRead < buffer.limit())
        {
            int count = channel.read(buffer);
            if (count > 0)
            {
                numBytesRead += count;
                this.contentLengthRead.getAndAdd(count);
            }
            if (count < 0)
                throw new WWRuntimeException("Premature end of stream from server.");
        }

        if (buffer != null)
            buffer.flip();

        return buffer;
    }

    protected ByteBuffer readNonSpecificStreamUnknownLength(InputStream inputStream) throws IOException
    {
        final int pageSize = (int) Math.ceil(Math.pow(2, 15));

        ReadableByteChannel channel = Channels.newChannel(inputStream);
        ByteBuffer buffer = ByteBuffer.allocate(pageSize);

        int count = 0;
        int numBytesRead = 0;
        while (!this.interrupted() && count >= 0)
        {
            count = channel.read(buffer);
            if (count > 0)
            {
                numBytesRead += count;
                this.contentLengthRead.getAndAdd(count);
            }

            if (count > 0 && !buffer.hasRemaining())
            {
                ByteBuffer biggerBuffer = ByteBuffer.allocate(buffer.limit() + pageSize);
                biggerBuffer.put((ByteBuffer) buffer.rewind());
                buffer = biggerBuffer;
            }
        }

        if (buffer != null)
            buffer.flip();

        return buffer;
    }

    /**
     * @param inputStream a stream to the zip connection.
     * @param url         the URL of the zip resource.
     *
     * @return a buffer containing the content read from the zip stream.
     *
     * @throws java.io.IOException      if the stream does not refer to a zip resource or an exception occurs during
     *                                  reading.
     * @throws IllegalArgumentException if <code>inputStream</code> is null
     */
    protected ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
    {
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry ze = zis.getNextEntry();
        if (ze == null)
        {
            Logging.logger().severe(Logging.getMessage("URLRetriever.NoZipEntryFor") + url);
            return null;
        }

        ByteBuffer buffer = null;
        if (ze.getSize() > 0)
        {
            buffer = ByteBuffer.allocate((int) ze.getSize());

            byte[] inputBuffer = new byte[8192];
            while (buffer.hasRemaining())
            {
                int count = zis.read(inputBuffer);
                if (count > 0)
                {
                    buffer.put(inputBuffer, 0, count);
                    this.contentLengthRead.getAndAdd(buffer.position() + 1);
                }
            }
        }
        if (buffer != null)
            buffer.flip();

        return buffer;
    }

    /**
     * Indicates the expiration time specified by either the Expires header or the max-age directive of the
     * Cache-Control header. If both are present, then Cache-Control is given priority (See section 14.9.3 of the HTTP
     * Specification: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html).
     * <p/>
     * If both the Expires and Date headers are present then the expiration time is calculated as current time +
     * (expires - date). This helps guard against clock skew between the client and server.
     *
     * @param connection Connection for which to get expiration time.
     *
     * @return The expiration time, in milliseconds since the Epoch, specified by the HTTP headers, or zero if there is
     *         no expiration time.
     */
    protected long getExpiration(URLConnection connection)
    {
        // Read the expiration time from either the Cache-Control header or the Expires header. Cache-Control has
        // priority if both headers are specified.
        String cacheControl = connection.getHeaderField("cache-control");
        if (cacheControl != null)
        {
            Pattern pattern = Pattern.compile("max-age=(\\d+)");
            Matcher matcher = pattern.matcher(cacheControl);
            if (matcher.find())
            {
                Long maxAgeSec = WWUtil.makeLong(matcher.group(1));
                if (maxAgeSec != null)
                    return maxAgeSec * 1000 + System.currentTimeMillis();
            }
        }

        // If the Cache-Control header is not present, or does not contain max-age, then look for the Expires header.
        // If the Date header is also present then compute the expiration time based on the server reported response
        // time. This helps guard against clock skew between client and server.
        long expiration = connection.getExpiration();
        long date = connection.getDate();

        if (date > 0 && expiration > date)
            return System.currentTimeMillis() + (expiration - date);

        return expiration;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final URLRetriever that = (URLRetriever) o;

        // Retrievers are considered identical if they are for the same URL. This convention is used by the
        // retrieval service to filter out duplicate retrieval requests.
        return !(url != null ? !url.toString().contentEquals(that.url.toString()) : that.url != null);
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return this.getName() != null ? this.getName() : super.toString();
    }
}
