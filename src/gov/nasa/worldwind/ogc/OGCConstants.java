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

package gov.nasa.worldwind.ogc;

/**
 * Defines constants used in the OGC package and sub-packages.
 *
 * @author tag
 * @version $Id: OGCConstants.java 2057 2014-06-14 01:13:52Z tgaskins $
 */
public interface OGCConstants
{
    /**
     * The name of the OGC Web Service <code>GetCapabilities</code> operation. The <code>GetCapabilities</code>
     * operation returns metadata about the operations and data provided by an OGC Web Service.
     * <code>GetCapabilities</code> is valid value for the <code>request</code> parameter. Used by all versions of all
     * OGC web services.
     */
    final String GET_CAPABILITIES = "GetCapabilities";
    /**
     * The name of the OGC Web Service <code>request</code> parameter. The associated value must be the name of an
     * operation to execute (for example, <code>GetCapabilities</code>). Used by all versions of all OGC web services.
     */
    final String REQUEST = "request";
    /**
     * The name of the OGC Web Service <code>service</code> parameter. The associated value must be the abbreviated OGC
     * Web Service name (for example, <code>WMS</code>). Used by all versions of all OGC web services.
     */
    final String SERVICE = "service";
    /**
     * The name of the OGC Web Service <code>version</code> parameter. The associated value must be the version of the
     * OGC Web Service protocol to use. The version must be formatted as <code>x.y.z</code>, where <code>x, y</code> and
     * <code>z</code> are integers in the range 0-99. Used by all versions of all OGC web services.
     */
    final String VERSION = "version";

    public static final String WMS_SERVICE_NAME = "OGC:WMS";
    public static final String WCS_SERVICE_NAME = "OGC:WCS";

    public static final String GML_NAMESPACE_URI = "http://www.opengis.net/gml";
    public static final String OGS_NAMESPACE_URI = "http://www.opengis.net/ogc";
    public static final String OWS_1_1_0_NAMESPACE_URI = "http://www.opengis.net/ows/1.1";
    public static final String WMS_NAMESPACE_URI = "http://www.opengis.net/wms";
    public static final String WCS_1_0_0_NAMESPACE_URI = "http://www.opengis.net/wcs";
    public static final String WCS_1_1_1_NAMESPACE_URI = "http://www.opengis.net/wcs/1.1.1";
}