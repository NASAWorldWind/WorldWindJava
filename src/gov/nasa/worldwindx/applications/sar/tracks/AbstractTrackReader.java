/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
