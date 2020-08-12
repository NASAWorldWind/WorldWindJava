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

package gov.nasa.worldwind.formats.nitfs;

import gov.nasa.worldwind.util.Logging;

/**
 * @author Lado Garakanidze
 * @version $Id: NITFSRuntimeException.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class NITFSRuntimeException extends java.lang.RuntimeException
{
    public NITFSRuntimeException()
    {
        super();
    }

    public NITFSRuntimeException(String messageID)
    {
        super(Logging.getMessage(messageID));
        log(this.getMessage());
    }

    public NITFSRuntimeException(String messageID, String params)
    {
        super(Logging.getMessage(messageID) + params);
        log(this.getMessage());
    }

    public NITFSRuntimeException(Throwable throwable)
    {
        super(throwable);
        log(this.getMessage());
    }

    public NITFSRuntimeException(String messageID, Throwable throwable)
    {
        super(Logging.getMessage(messageID), throwable);
        log(this.getMessage());
    }

    public NITFSRuntimeException(String messageID, String params, Throwable throwable)
    {
        super(Logging.getMessage(messageID) + params, throwable);
        log(this.getMessage());
    }

    // TODO: Calling the logger from here causes the wrong method to be listed in the log record. Must call the
    // logger from the site with the problem and generating the exception.
    private void log(String s)
    {
        Logging.logger().fine(s);
    }
}