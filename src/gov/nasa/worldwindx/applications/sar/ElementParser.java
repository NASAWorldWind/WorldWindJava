/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: ElementParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ElementParser
{
    protected final String elementName;
    protected gov.nasa.worldwindx.applications.sar.ElementParser currentElement = null;
    protected String currentCharacters = null;

    /**
     * @param elementName the element's name, may not be null
     * @throws IllegalArgumentException if <code>elementName</code> is null
     */
    protected ElementParser(String elementName)
    {
        if (elementName == null)
        {
            String msg = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.elementName = elementName;
    }

    public String getElementName()
    {
        return this.elementName;
    }

    /**
     * Starts an element. No parameters may be null.
     *
     * @param uri
     * @param lname
     * @param qname
     * @param attributes
     * @throws org.xml.sax.SAXException
     * @throws IllegalArgumentException if any argument is null
     */
    public void startElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
        if (uri == null)
        {
            String msg = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (lname == null)
        {
            String msg = Logging.getMessage("nullValue.LNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (qname == null)
        {
            String msg = Logging.getMessage("nullValue.QNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.org.xml.sax.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.currentElement != null)
            this.currentElement.startElement(uri, lname, qname, attributes);
        else
            this.doStartElement(uri, lname, qname, attributes);
    }

    /**
     * Finishes an element. No parameters may be null.
     *
     * @param uri
     * @param lname
     * @param qname
     * @throws org.xml.sax.SAXException
     * @throws IllegalArgumentException if any argument is null
     */
    public void endElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
    {
        if (uri == null)
        {
            String msg = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (lname == null)
        {
            String msg = Logging.getMessage("nullValue.LNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (qname == null)
        {
            String msg = Logging.getMessage("nullValue.QNameIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (this.currentElement != null)
        {
            this.currentElement.endElement(uri, lname, qname);
            if (lname.equalsIgnoreCase(this.currentElement.elementName))
                this.currentElement = null;
        }

        this.doEndElement(uri, lname, qname);

        this.currentCharacters = null;
    }

    protected void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        throws org.xml.sax.SAXException
    {
    }

    protected void doEndElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
    {
    }

    /**
     * @param data
     * @param start
     * @param length
     * @throws IllegalArgumentException if <code>data</code> has length less than 1
     */
    public void characters(char[] data, int start, int length)
    {
        if (data == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (data.length < 1)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", data.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (start < 0)
        {
            String msg = Logging.getMessage("generic.indexOutOfRange", start);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (start + length > data.length)
        {
            String msg = Logging.getMessage("generic.indexOutOfRange", start + length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.currentElement != null)
            this.currentElement.characters(data, start, length);
        else if (this.currentCharacters != null)
            this.currentCharacters += new String(data, start, length);
        else
            this.currentCharacters = new String(data, start, length);
    }
}