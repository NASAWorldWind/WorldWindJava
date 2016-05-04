/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

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
            throw new IllegalArgumentException();
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
            throw new IllegalArgumentException();
        }
        if (lname == null)
        {
            throw new IllegalArgumentException();
        }
        if (qname == null)
        {
            throw new IllegalArgumentException();
        }
        if (attributes == null)
        {
            throw new IllegalArgumentException();
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
            throw new IllegalArgumentException();
        }
        if (lname == null)
        {
            throw new IllegalArgumentException();
        }
        if (qname == null)
        {
            throw new IllegalArgumentException();
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
            throw new IllegalArgumentException();
        }
        if (data.length < 1)
        {
            throw new IllegalArgumentException();
        }
        if (start < 0)
        {
            throw new IllegalArgumentException();
        }
        if (start + length > data.length)
        {
            throw new IllegalArgumentException();
        }

        if (this.currentElement != null)
            this.currentElement.characters(data, start, length);
        else if (this.currentCharacters != null)
            this.currentCharacters += new String(data, start, length);
        else
            this.currentCharacters = new String(data, start, length);
    }
}