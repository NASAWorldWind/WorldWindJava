/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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