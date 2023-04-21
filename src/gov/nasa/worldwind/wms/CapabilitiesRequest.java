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
package gov.nasa.worldwind.wms;

import gov.nasa.worldwind.util.Logging;

import java.net.*;

/**
 * @author tag
 * @version $Id: CapabilitiesRequest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public final class CapabilitiesRequest extends Request
{
    /** Construct an OGC GetCapabilities request using the default service. */
    public CapabilitiesRequest()
    {
    }

    /**
     * Constructs a request for the default service, WMS, and a specified server.
     *
     * @param uri the address of the web service.
     *
     * @throws IllegalArgumentException if the uri is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */

    public CapabilitiesRequest(URI uri) throws URISyntaxException
    {
        super(uri, null);

        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Constructs a request for a specified service at a specified server.
     *
     * @param uri     the address of the web service.
     * @param service the service name. Common names are WMS, WFS, WCS, etc.
     *
     * @throws IllegalArgumentException if the uri or service name is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */
    public CapabilitiesRequest(URI uri, String service) throws URISyntaxException
    {
        super(uri, service);

        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (service == null)
        {
            String message = Logging.getMessage("nullValue.WMSServiceNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    protected void initialize(String service)
    {
        super.initialize(service);
        this.setParam("REQUEST", "GetCapabilities");
        this.setParam("VERSION", "1.3.0");
    }
}
