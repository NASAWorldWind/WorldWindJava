/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.tracks;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: AbstractTrackReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractTrackReader implements TrackReader
{
    protected abstract Track[] doRead(InputStream inputStream) throws IOException;

    public boolean canRead(Object source)
    {
        return (source != null) && this.doCanRead(source);
    }

    public Track[] read(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            return this.doRead(source);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadFrom", source);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
        catch (WWUnrecognizedException e)
        {
            // Source type is passed up the call stack in the WWUnrecognizedException's message.
            String message = Logging.getMessage("generic.UnrecognizedSourceType", e.getMessage());
            Logging.logger().severe(message);
            throw new WWRuntimeException(message, e);
        }
    }

    protected boolean doCanRead(Object source)
    {
        if (source instanceof File)
            return this.doCanRead(((File) source).getPath());
        else if (source instanceof String)
            return this.doCanRead((String) source);
        else if (source instanceof URL)
            return this.doCanRead((URL) source);
        else if (source instanceof InputStream)
            return this.doCanRead((InputStream) source);

        return false;
    }

    protected boolean doCanRead(String filePath)
    {
        if (!this.acceptFilePath(filePath))
            return false;

        try
        {
            return this.doRead(filePath) != null;
        }
        catch (Exception e)
        {
            // Not interested in logging the exception. We just want to return false to indicate that the source
            // cannot be read.
        }

        return false;
    }

    protected boolean doCanRead(URL url)
    {
        File file = WWIO.convertURLToFile(url);
        if (file != null)
            return this.doCanRead(file.getPath());

        try
        {
            return this.doRead(url) != null;
        }
        catch (Exception e)
        {
            // Not interested in logging the exception. We just want to return false to indicate that the source
            // cannot be read.
        }

        return false;
    }

    protected boolean doCanRead(InputStream inputStream)
    {
        try
        {
            return this.doRead(inputStream) != null;
        }
        catch (Exception e)
        {
            // Not interested in logging the exception. We just want to return false to indicate that the source
            // cannot be read.
        }

        return false;
    }

    protected boolean acceptFilePath(String filePath)
    {
        return true;
    }

    protected Track[] doRead(Object source) throws IOException
    {
        if (source instanceof File)
            return this.doRead(((File) source).getPath());
        else if (source instanceof String)
            return this.doRead((String) source);
        else if (source instanceof URL)
            return this.doRead((URL) source);
        else if (source instanceof InputStream)
            return this.doRead((InputStream) source);

        // Pass the source type up the call stack in the WWUnrecognizedException's message. This enables us to be more
        // specific about the source type if a subclass has more detailed information to offer.
        throw new WWUnrecognizedException(source.toString());
    }

    protected Track[] doRead(String filePath) throws IOException
    {
        InputStream inputStream = null;
        try
        {
            inputStream = WWIO.openFileOrResourceStream(filePath, this.getClass());
            return this.doRead(inputStream);
        }
        finally
        {
            WWIO.closeStream(inputStream, filePath);
        }
    }

    protected Track[] doRead(URL url) throws IOException
    {
        InputStream inputStream = null;
        try
        {
            inputStream = url.openStream();
            return this.doRead(inputStream);
        }
        finally
        {
            WWIO.closeStream(inputStream, url.toString());
        }
    }

    protected Track[] asArray(List<Track> trackList)
    {
        if (trackList == null)
            return null;

        Track[] trackArray = new Track[trackList.size()];
        trackList.toArray(trackArray);
        return trackArray;
    }
}
