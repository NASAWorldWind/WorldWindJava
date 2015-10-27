/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.*;

/**
 * BasicNamespaceContext provides a mutable implementation of the {@link javax.xml.namespace.NamespaceContext}
 * interface.
 *
 * @author dcollins
 * @version $Id: BasicNamespaceContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicNamespaceContext implements NamespaceContext
{
    public static final String XLINK_NS_PREFIX = "xlink";
    public static final String XLINK_NS_URI = "http://www.w3.org/1999/xlink";

    private Map<String, String> urisByPrefix = new HashMap<String, String>();
    private Map<String, Set<String>> prefixesByURI = new HashMap<String, Set<String>>();

    /**
     * Sole constructor for BasicNamespaceContext. This configures the following namespaces: <table>
     * <tr><th>Prefix</th><th>URI</th></tr> <tr><td>xml</td><td>http://www.w3.org/XML/1998/namespace</td></tr>
     * <tr><td>xmlns</td><td>http://www.w3.org/2000/xmlns/</td></tr> <tr><td>xlink</td><td>http://www.w3.org/1999/xlink</td></tr>
     * </table>
     */
    public BasicNamespaceContext()
    {
        // Configure the default xml and xmlns namespaces according to the documentation of the NamespaceContext
        // interface.
        this.addNamespace(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        this.addNamespace(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        this.addNamespace(XLINK_NS_PREFIX, XLINK_NS_URI);
    }

    /**
     * Adds a namepsace binding to this XML namespace context. The specified URI is bound to the specified prefix.
     *
     * @param prefix       the namespace prefix.
     * @param namespaceURI the namespace URI.
     *
     * @throws IllegalArgumentException if either the prefix or the namepsace URI are null.
     */
    public synchronized void addNamespace(String prefix, String namespaceURI)
    {
        if (prefix == null)
        {
            String message = Logging.getMessage("nullValue.PrefixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (namespaceURI == null)
        {
            String message = Logging.getMessage("nullValue.NamespaceURIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.urisByPrefix.put(prefix, namespaceURI);

        if (this.prefixesByURI.containsKey(namespaceURI))
        {
            this.prefixesByURI.get(namespaceURI).add(prefix);
        }
        else
        {
            Set<String> set = new HashSet<String>();
            set.add(prefix);
            this.prefixesByURI.put(namespaceURI, set);
        }
    }

    /** {@inheritDoc} */
    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
        {
            String message = Logging.getMessage("nullValue.PrefixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.urisByPrefix.containsKey(prefix))
        {
            return this.urisByPrefix.get(prefix);
        }
        else
        {
            return XMLConstants.NULL_NS_URI;
        }
    }

    /** {@inheritDoc} */
    public String getPrefix(String namespaceURI)
    {
        if (namespaceURI == null)
        {
            String message = Logging.getMessage("nullValue.NamespaceURIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (String) this.getPrefixes(namespaceURI).next();
    }

    /** {@inheritDoc} */
    public Iterator getPrefixes(String namespaceURI)
    {
        if (namespaceURI == null)
        {
            String message = Logging.getMessage("nullValue.NamespaceURIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.prefixesByURI.containsKey(namespaceURI))
        {
            return Collections.unmodifiableSet(this.prefixesByURI.get(namespaceURI)).iterator();
        }
        else
        {
            return Collections.EMPTY_SET.iterator();
        }
    }
}
