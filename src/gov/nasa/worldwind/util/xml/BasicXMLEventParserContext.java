/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.Logging;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.beans.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Provides an implementation of {@link gov.nasa.worldwind.util.xml.XMLEventParserContext}. This class is meant to be
 * the base class for schema-specific parsers.
 *
 * @author tag
 * @version $Id: BasicXMLEventParserContext.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
public class BasicXMLEventParserContext extends AVListImpl implements XMLEventParserContext
{
    /** The parser name of the default double parser. */
    public static QName DOUBLE = new QName("Double");
    /** The parser name of the default integer parser. */
    public static QName INTEGER = new QName("Integer");
    /** The parser name of the default string parser. */
    public static QName STRING = new QName("String");
    /** The parser name of the default boolean parser. */
    public static QName BOOLEAN = new QName("Boolean");
    /** The parser name of the default boolean integer parser. */
    public static QName BOOLEAN_INTEGER = new QName("BooleanInteger");
    /** The parser name of the unrecognized-element parser. */
    public static QName UNRECOGNIZED = new QName(UNRECOGNIZED_ELEMENT_PARSER);

    protected XMLEventReader reader;
    protected StringXMLEventParser stringParser;
    protected DoubleXMLEventParser doubleParser;
    protected IntegerXMLEventParser integerParser;
    protected BooleanXMLEventParser booleanParser;
    protected BooleanIntegerXMLEventParser booleanIntegerParser;
    protected String defaultNamespaceURI = XMLConstants.NULL_NS_URI;
    protected XMLParserNotificationListener notificationListener;
    protected ConcurrentHashMap<String, Object> idTable = new ConcurrentHashMap<String, Object>();

    protected ConcurrentHashMap<QName, XMLEventParser> parsers = new ConcurrentHashMap<QName, XMLEventParser>();

    /** Construct an instance. Invokes {@link #initializeParsers()} and {@link #initialize()}. */
    public BasicXMLEventParserContext()
    {
        this.initializeParsers();
        this.initialize();
    }

    /**
     * Construct an instance for a specified event reader. Invokes {@link #initializeParsers()} and {@link
     * #initialize()}.
     *
     * @param eventReader the event reader to use for XML parsing.
     */
    public BasicXMLEventParserContext(XMLEventReader eventReader)
    {
        this.reader = eventReader;

        this.initializeParsers();
        this.initialize();
    }

    /**
     * Construct an instance for a specified event reader and default namespace. Invokes {@link #initializeParsers()}
     * and {@link #initialize()}.
     *
     * @param eventReader      the event reader to use for XML parsing.
     * @param defaultNamespace the namespace URI of the default namespace.
     */
    public BasicXMLEventParserContext(XMLEventReader eventReader, String defaultNamespace)
    {
        this.reader = eventReader;
        this.setDefaultNamespaceURI(defaultNamespace);

        this.initializeParsers();
        this.initialize();
    }

    public BasicXMLEventParserContext(BasicXMLEventParserContext ctx)
    {
        this.parsers = ctx.parsers;
        this.setDefaultNamespaceURI(ctx.getDefaultNamespaceURI());
        this.initialize();
    }

    protected void initialize()
    {
        this.initializeDefaultNotificationListener();
    }

    protected void initializeDefaultNotificationListener()
    {
        this.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent propEvent)
            {
                XMLParserNotification notification = (XMLParserNotification) propEvent;

                if (notificationListener != null)
                {
                    notificationListener.notify(notification);
                    return;
                }

                String msg;
                if (notification.getEvent() != null)
                {
                    msg = Logging.getMessage(notification.getMessage(), notification.getEvent().toString(),
                        notification.getEvent().getLocation().getLineNumber(),
                        notification.getEvent().getLocation().getColumnNumber(),
                        notification.getEvent().getLocation().getCharacterOffset());
                }
                else
                {
                    msg = Logging.getMessage(notification.getMessage(), "", "");
                }

                if (notification.getPropertyName().equals(XMLParserNotification.EXCEPTION))
                    Logging.logger().log(Level.WARNING, msg);
                else if (notification.getPropertyName().equals(XMLParserNotification.UNRECOGNIZED))
                    Logging.logger().log(Level.WARNING, msg);
            }
        });
    }

    /**
     * Initializes the parser table with the default parsers for the strings, integers, etc., qualified for the default
     * namespace.
     */
    protected void initializeParsers()
    {
        this.parsers.put(STRING, new StringXMLEventParser());
        this.parsers.put(DOUBLE, new DoubleXMLEventParser());
        this.parsers.put(INTEGER, new IntegerXMLEventParser());
        this.parsers.put(BOOLEAN, new BooleanXMLEventParser());
        this.parsers.put(BOOLEAN_INTEGER, new BooleanIntegerXMLEventParser());
        this.parsers.put(UNRECOGNIZED, new UnrecognizedXMLEventParser(null));
    }

    @Override
    public void addStringParsers(String namespace, String[] stringFields)
    {
        StringXMLEventParser stringParser = this.getStringParser();
        for (String s : stringFields)
        {
            this.parsers.put(new QName(namespace, s), stringParser);
        }
    }

    @Override
    public void addDoubleParsers(String namespace, String[] doubleFields)
    {
        DoubleXMLEventParser doubleParser = this.getDoubleParser();
        for (String s : doubleFields)
        {
            this.parsers.put(new QName(namespace, s), doubleParser);
        }
    }

    @Override
    public void addIntegerParsers(String namespace, String[] integerFields)
    {
        IntegerXMLEventParser integerParser = this.getIntegerParser();
        for (String s : integerFields)
        {
            this.parsers.put(new QName(namespace, s), integerParser);
        }
    }

    @Override
    public void addBooleanParsers(String namespace, String[] booleanFields)
    {
        BooleanXMLEventParser booleanParser = this.getBooleanParser();
        for (String s : booleanFields)
        {
            this.parsers.put(new QName(namespace, s), booleanParser);
        }
    }

    @Override
    public void addBooleanIntegerParsers(String namespace, String[] booleanIntegerFields)
    {
        BooleanIntegerXMLEventParser booleanIntegerParser = this.getBooleanIntegerParser();
        for (String s : booleanIntegerFields)
        {
            this.parsers.put(new QName(namespace, s), booleanIntegerParser);
        }
    }

    /**
     * Returns the event reader used by this instance.
     *
     * @return the instance's event reader.
     */
    public XMLEventReader getEventReader()
    {
        return this.reader;
    }

    /**
     * Specify the event reader for the parser context to use to parse XML.
     *
     * @param reader the event reader to use.
     */
    public void setEventReader(XMLEventReader reader)
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull"); // TODO
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.reader = reader;
    }

    public String getDefaultNamespaceURI()
    {
        return defaultNamespaceURI;
    }

    public void setDefaultNamespaceURI(String defaultNamespaceURI)
    {
        this.defaultNamespaceURI = defaultNamespaceURI;
    }

    public void setNotificationListener(XMLParserNotificationListener listener)
    {
        this.notificationListener = listener;
    }

    public Map<String, Object> getIdTable()
    {
        return this.idTable;
    }

    public void addId(String id, Object o)
    {
        if (id != null)
            this.getIdTable().put(id, o);
    }

    public boolean hasNext()
    {
        return this.getEventReader().hasNext();
    }

    public XMLEvent nextEvent() throws XMLStreamException
    {
        while (this.hasNext())
        {
            XMLEvent event = this.getEventReader().nextEvent();

            if (event.isCharacters() && event.asCharacters().isWhiteSpace())
                continue;

            return event;
        }

        return null;
    }

    public XMLEventParser allocate(XMLEvent event, XMLEventParser defaultParser)
    {
        return this.getParser(event, defaultParser);
    }

    public XMLEventParser allocate(XMLEvent event)
    {
        return this.getParser(event, null);
    }

    public XMLEventParser getParser(XMLEvent event)
    {
        return this.getParser(event, null);
    }

    protected XMLEventParser getParser(XMLEvent event, XMLEventParser defaultParser)
    {
        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        QName elementName = event.asStartElement().getName();
        if (elementName == null)
            return null;

        XMLEventParser parser = this.getParser(elementName);

        return parser != null ? parser : defaultParser;
    }

    public StringXMLEventParser getStringParser()
    {
        if (this.stringParser == null)
            this.stringParser = (StringXMLEventParser) this.getParser(STRING);

        return this.stringParser;
    }

    public DoubleXMLEventParser getDoubleParser()
    {
        if (this.doubleParser == null)
            this.doubleParser = (DoubleXMLEventParser) this.getParser(DOUBLE);

        return this.doubleParser;
    }

    public IntegerXMLEventParser getIntegerParser()
    {
        if (this.integerParser == null)
            this.integerParser = (IntegerXMLEventParser) this.getParser(INTEGER);

        return this.integerParser;
    }

    public BooleanXMLEventParser getBooleanParser()
    {
        if (this.booleanParser == null)
            this.booleanParser = (BooleanXMLEventParser) this.getParser(BOOLEAN);

        return this.booleanParser;
    }

    public BooleanIntegerXMLEventParser getBooleanIntegerParser()
    {
        if (this.booleanIntegerParser == null)
            this.booleanIntegerParser = (BooleanIntegerXMLEventParser) this.getParser(BOOLEAN_INTEGER);

        return this.booleanIntegerParser;
    }

    /**
     * Returns a parser to handle unrecognized elements. The default unrecognized event parser is {@link
     * gov.nasa.worldwind.util.xml.UnrecognizedXMLEventParser}, and may be replaced by calling {@link
     * #registerParser(javax.xml.namespace.QName, XMLEventParser)} and specifying {@link #UNRECOGNIZED} as the parser
     * name.
     *
     * @return a parser to handle unrecognized elements.
     */
    public XMLEventParser getUnrecognizedElementParser()
    {
        return this.getParser(UNRECOGNIZED);
    }

    public String getCharacters(XMLEvent event)
    {
        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return event.isCharacters() ? event.asCharacters().getData() : null;
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean isSameName(QName qa, QName qb)
    {
        if (qa.equals(qb))
            return true;

        if (!qa.getLocalPart().equals(qb.getLocalPart()))
            return false;

        if (qa.getNamespaceURI().equals(XMLConstants.NULL_NS_URI))
            return qb.getNamespaceURI().equals(this.getDefaultNamespaceURI());

        if (qb.getNamespaceURI().equals(XMLConstants.NULL_NS_URI))
            return qa.getNamespaceURI().equals(this.getDefaultNamespaceURI());

        return false;
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean isSameAttributeName(QName qa, QName qb)
    {
        return qa != null && qb != null && qa.getLocalPart() != null && qa.getLocalPart().equals(qb.getLocalPart());
    }

    public boolean isStartElement(XMLEvent event, QName elementName)
    {
        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (elementName == null)
        {
            String message = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (event.isStartElement() && this.isSameName(event.asStartElement().getName(), elementName));
    }

    public boolean isStartElement(XMLEvent event, String elementName)
    {
        if (event == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (elementName == null)
        {
            String message = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(elementName));
    }

    public boolean isEndElement(XMLEvent event, XMLEvent startElement)
    {
        if (event == null || startElement == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return isEndElementEvent(event, startElement);
    }

    public static boolean isEndElementEvent(XMLEvent event, XMLEvent startElement)
    {
        if (event == null || startElement == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (event.isEndElement()
            && event.asEndElement().getName().equals(startElement.asStartElement().getName()));
    }

    public void registerParser(QName elementName, XMLEventParser parser)
    {
        if (parser == null)
        {
            String message = Logging.getMessage("nullValue.ParserIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (elementName == null)
        {
            String message = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.parsers.put(elementName, parser);
    }

    public XMLEventParser getParser(QName name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.ElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XMLEventParser factoryParser = this.parsers.get(name);
        if (factoryParser == null)
        {
            // Try alternate forms that assume a default namespace in either the input name or the table key.
            if (isNullNamespace(name.getNamespaceURI()))
            {
                // input name has no namespace but table key has the default namespace
                QName altName = new QName(this.getDefaultNamespaceURI(), name.getLocalPart());
                factoryParser = this.parsers.get(altName);
            }
            else if (this.isDefaultNamespace(name.getNamespaceURI()))
            {
                // input name has the default namespace but table name has no namespace
                QName altName = new QName(name.getLocalPart());
                factoryParser = this.parsers.get(altName);
            }
        }

        try
        {
            if (factoryParser == null)
                return null;

            return factoryParser.newInstance();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("XML.ParserCreationException", name);
            Logging.logger().log(java.util.logging.Level.WARNING, message, e);
            return null;
        }
    }

    protected static boolean isNullNamespace(String namespaceURI)
    {
        return namespaceURI == null || XMLConstants.NULL_NS_URI.equals(namespaceURI);
    }

    public boolean isDefaultNamespace(String namespaceURI)
    {
        return this.getDefaultNamespaceURI() != null && this.getDefaultNamespaceURI().equals(namespaceURI);
    }

    @Deprecated
    public void resolveInternalReferences(String referenceName, String fieldName, AbstractXMLEventParser parser)
    {
        if (parser == null || !parser.hasFields())
            return;

        Map<String, Object> newFields = null;

        for (Map.Entry<String, Object> p : parser.getFields().getEntries())
        {
            String key = p.getKey();
            if (key == null || key.equals("id"))
                continue;

            Object v = p.getValue();
            if (v == null)
                continue;

            if (v instanceof String)
            {
                String value = (String) v;

                if (value.startsWith("#") && key.endsWith(referenceName))
                {
                    Object o = this.getIdTable().get(value.substring(1, value.length()));
                    if (/*o instanceof KMLStyle &&*/ !parser.hasField(fieldName))
                    {
                        if (newFields == null)
                            newFields = new HashMap<String, Object>();
                        newFields.put(fieldName, o);
                    }
                }
            }
        }

        if (newFields != null)
            parser.setFields(newFields);
    }
}
