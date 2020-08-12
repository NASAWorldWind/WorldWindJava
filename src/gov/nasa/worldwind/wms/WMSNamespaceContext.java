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

import gov.nasa.worldwind.util.BasicNamespaceContext;

import javax.xml.XMLConstants;

/**
 * WMSNamespaceContext is an implementation of {@link javax.xml.namespace.NamespaceContext} which provides an XML
 * Namespace context necessary for parsing WMS documents, such as a WMS Capabilities document.
 *
 * @author dcollins
 * @version $Id: WMSNamespaceContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WMSNamespaceContext extends BasicNamespaceContext
{
    public static final String WMS_NS_PREFIX = "wms";
    public static final String WMS_NS_URI = "http://www.opengis.net/wms";

    /**
     * Sole constructor for WMSNamespaceContext. In addition to the namespaces configured by the superclass constructor
     * {@link gov.nasa.worldwind.util.BasicNamespaceContext#BasicNamespaceContext()}, this configures the following
     * namespaces: <table> <caption style="font-weight: bold;">Mapping</caption>
     * <tr><th>Prefix</th><th>URI</th></tr> <tr><td>wms</td><td>http://www.opengis.net/wms</td></tr>
     * <tr><td><code>DEFAULT_NS_PREFIX ("")</code></td><td>http://www.opengis.net/wms</td></tr> </table>
     */
    public WMSNamespaceContext()
    {
        this.addNamespace(WMS_NS_PREFIX, WMS_NS_URI);
        this.addNamespace(XMLConstants.DEFAULT_NS_PREFIX, WMS_NS_URI);
    }
}