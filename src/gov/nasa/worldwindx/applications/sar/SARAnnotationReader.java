/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.geom.Position;

/**
 * @author dcollins
 * @version $Id: SARAnnotationReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SARAnnotationReader
{
    private javax.xml.parsers.SAXParser parser;
    private java.util.List<SARAnnotation> sarAnnotations = new java.util.ArrayList<SARAnnotation>();

    public SARAnnotationReader() throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException
    {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        this.parser = factory.newSAXParser();
    }

    public void readFile(String path) throws java.io.IOException, org.xml.sax.SAXException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists())
        {
            String msg = Logging.getMessage("generic.FileNotFound", path);
            Logging.logger().severe(msg);
            throw new java.io.FileNotFoundException(path);
        }

        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        this.doRead(fis);
    }

    public void readStream(java.io.InputStream stream) throws java.io.IOException, org.xml.sax.SAXException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.doRead(stream);
    }

    public java.util.List<SARAnnotation> getSARAnnotations()
    {
        return this.sarAnnotations;
    }

    private void doRead(java.io.InputStream fis) throws java.io.IOException, org.xml.sax.SAXException
    {
        this.parser.parse(fis, new Handler());
    }

    private class Handler extends org.xml.sax.helpers.DefaultHandler
    {
        // this is a private class used solely by the containing class, so no validation occurs in it.

        private gov.nasa.worldwindx.applications.sar.ElementParser currentElement = null;

        @Override
        public void warning(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.warning(saxParseException);
        }

        @Override
        public void error(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.error(saxParseException);
        }

        @Override
        public void fatalError(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.fatalError(saxParseException);
        }

        private boolean firstElement = true;

        @Override
        public void startElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
            throws org.xml.sax.SAXException
        {
            if (this.firstElement)
            {
                if (!lname.equalsIgnoreCase("sarTrackAnnotations"))
                    throw new IllegalArgumentException("Not a SAR Track Annotations file");
                else
                    this.firstElement = false;
            }

            if (this.currentElement != null)
            {
                this.currentElement.startElement(uri, lname, qname, attributes);
            }
            else if (lname.equalsIgnoreCase("sarAnnotation"))
            {
                this.currentElement = new SARAnnotationElement(uri, lname, qname, attributes);
            }
        }

        @Override
        public void endElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
            {
                this.currentElement.endElement(uri, lname, qname);

                if (lname.equalsIgnoreCase(this.currentElement.getElementName()))
                {
                    // Get the SARAnnotation once the element is completely constructed.
                    if (this.currentElement instanceof SARAnnotationElement)
                        SARAnnotationReader.this.sarAnnotations.add(((SARAnnotationElement) this.currentElement).getSARAnnotation());
                    this.currentElement = null;
                }
            }
        }

        @Override
        public void characters(char[] data, int start, int length) throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
                this.currentElement.characters(data, start, length);
        }
    }

    private class SARAnnotationElement extends ElementParser
    {
        private double latitutde;
        private double longitude;
        private String id;
        private String text;

        public SARAnnotationElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
        {
            super("sarAnnotation");
            // don't perform validation here - no parameters are actually used
        }

        public SARAnnotation getSARAnnotation()
        {
            Position pos = Position.fromDegrees(this.latitutde, this.longitude, 0);
            SARAnnotation sa = new SARAnnotation(this.text, pos);
            sa.setId(this.id);
            return sa;
        }

        @Override
        public void doStartElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
            throws org.xml.sax.SAXException
        {
            // don't perform validation here - no parameters are actually used
        }

        /**
         * @param uri
         * @param lname
         * @param qname
         * @throws IllegalArgumentException if <code>lname</code> is null
         * @throws org.xml.sax.SAXException
         */
        @Override
        public void doEndElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
        {
            if (lname == null)
            {
                String msg = Logging.getMessage("nullValue.LNameIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            // don't validate uri or qname - they aren't used.

            if (lname.equalsIgnoreCase("latitude"))
            {
                this.latitutde = Double.parseDouble(this.currentCharacters);
            }
            else if (lname.equalsIgnoreCase("longitude"))
            {
                this.longitude = Double.parseDouble(this.currentCharacters);
            }
            else if (lname.equalsIgnoreCase("id"))
            {
                this.id = this.currentCharacters.trim();
            }
            else if (lname.equalsIgnoreCase("text"))
            {
                this.text = this.currentCharacters.trim();
            }
        }
    }
}
