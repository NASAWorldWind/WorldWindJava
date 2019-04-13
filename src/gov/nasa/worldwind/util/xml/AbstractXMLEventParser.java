/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Base class for XML event parsers. Handles parsing control and creation of new parser instances.
 * <p>
 * A parser holds the information parsed from the event stream. That information can be queried via the parser's
 * accessors. A parser typically does not maintain a reference to the event stream it parsed or the parser context used
 * during parsing.
 * <p>
 * Parsers are created when events of the associated type are encountered in the input stream. An {@link
 * #allocate(XMLEventParserContext, javax.xml.stream.events.XMLEvent)} method in the parser typically creates a default
 * parser prior to consulting the {@link XMLEventParserContext}, which returns a new parser whose type is determined by
 * consulting a table of event types. The default parser is returned if the table contains no entry for the event type.
 * <p>
 * A parser can be associated with a specific namespace. The namespace is used to qualify the parser's association with
 * event types.
 *
 * @author tag
 * @version $Id: AbstractXMLEventParser.java 1981 2014-05-08 03:59:04Z tgaskins $
 */
abstract public class AbstractXMLEventParser implements XMLEventParser
{
    protected static final String CHARACTERS_CONTENT = "CharactersContent";

    protected String namespaceURI;

    protected AVList fields;
    protected XMLEventParser parent;

    /** Construct a parser with no qualifying namespace. */
    public AbstractXMLEventParser()
    {
        this.namespaceURI = null;
    }

    /**
     * Constructs a parser and qualifies it for a specified namespace.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public AbstractXMLEventParser(String namespaceURI)
    {
        this.namespaceURI = namespaceURI;
    }

    /**
     * Returns the qualifying namespace URI specified at construction.
     *
     * @return the namespace URI. Returns null if no name space was specified at construction.
     */
    public String getNamespaceURI()
    {
        return this.namespaceURI;
    }

    protected void setNamespaceURI(String namespaceURI)
    {
        this.namespaceURI = namespaceURI;
    }

    public XMLEventParser newInstance() throws Exception
    {
        Constructor<? extends AbstractXMLEventParser> constructor = this.getAConstructor(String.class);
        if (constructor != null)
            return constructor.newInstance(this.getNamespaceURI());

        constructor = this.getAConstructor();
        if (constructor != null)
            return constructor.newInstance();

        return null;
    }

    public void setField(QName keyName, Object value)
    {
        this.setField(keyName.getLocalPart(), value);
    }

    public void setField(String keyName, Object value)
    {
        if (this.fields == null)
            this.fields = new AVListImpl();

        this.fields.setValue(keyName, value);
    }

    public void setFields(Map<String, Object> newFields)
    {
        if (this.fields == null)
            this.fields = new AVListImpl();

        for (Map.Entry<String, Object> nf : newFields.entrySet())
        {
            this.setField(nf.getKey(), nf.getValue());
        }
    }

    public Object getField(QName keyName)
    {
        return this.fields != null ? this.getField(keyName.getLocalPart()) : null;
    }

    public Object getField(String keyName)
    {
        return this.fields != null ? this.fields.getValue(keyName) : null;
    }

    public boolean hasField(QName keyName)
    {
        return this.hasField(keyName.getLocalPart());
    }

    public boolean hasField(String keyName)
    {
        return this.fields != null && this.fields.hasKey(keyName);
    }

    public void removeField(String keyName)
    {
        if (this.fields != null)
            this.fields.removeKey(keyName);
    }

    public boolean hasFields()
    {
        return this.fields != null;
    }

    public AVList getFields()
    {
        return this.fields;
    }

    protected AbstractXMLEventParser mergeFields(AbstractXMLEventParser s1, AbstractXMLEventParser s2)
    {
        for (Map.Entry<String, Object> entry : s2.getFields().getEntries())
        {
            if (!s1.hasField(entry.getKey()))
                s1.setField(entry.getKey(), entry.getValue());
        }

        return this;
    }

    protected AbstractXMLEventParser overrideFields(AbstractXMLEventParser s1, AbstractXMLEventParser s2)
    {
        if (s2.getFields() != null)
        {
            for (Map.Entry<String, Object> entry : s2.getFields().getEntries())
            {
                s1.setField(entry.getKey(), entry.getValue());
            }
        }

        return this;
    }

    public XMLEventParser getParent()
    {
        return this.parent;
    }

    public void setParent(XMLEventParser parent)
    {
        this.parent = parent;
    }

    public void freeResources()
    {
        // Override in subclass to free any large resources.
    }

    protected Constructor<? extends AbstractXMLEventParser> getAConstructor(Class... parameterTypes)
    {
        try
        {
            return this.getClass().getConstructor(parameterTypes);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    public XMLEventParser getRoot()
    {
        XMLEventParser parser = this;

        while (true)
        {
            XMLEventParser parent = parser.getParent();
            if (parent == null)
                return parser;
            parser = parent;
        }
    }

    /**
     * Create a parser for a specified event.
     *
     * @param ctx   the current parser context.
     * @param event the event for which the parser is created. Only the event type is used; the new parser can operate
     *              on any event of that type.
     *
     * @return the new parser.
     */
    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        if (ctx == null)
        {
            String message = Logging.getMessage("nullValue.ParserContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XMLEventParser parser = ctx.allocate(event);
        if (parser != null)
            parser.setParent(this);

        return parser;
    }

    /** {@inheritDoc} */
    public Object parse(XMLEventParserContext ctx, XMLEvent inputEvent, Object... args) throws XMLStreamException
    {
        if (ctx == null)
        {
            String message = Logging.getMessage("nullValue.ParserContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (inputEvent == null)
        {
            String message = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            // Parse this event's attributes
            this.doParseEventAttributes(ctx, inputEvent, args);

            // Build the symbol table
            String id = (String) this.getField("id");
            if (id != null)
                ctx.addId(id, this);
        }
        catch (XMLStreamException e)
        {
            ctx.firePropertyChange(new XMLParserNotification(ctx, XMLParserNotification.EXCEPTION, inputEvent,
                "XML.ExceptionParsingElement", null, e));
        }

        // Parse the event's subelements.
        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            if (ctx.isEndElement(event, inputEvent))
            {
                if (this.hasField(CHARACTERS_CONTENT))
                {
                    StringBuilder sb = (StringBuilder) this.getField(CHARACTERS_CONTENT);
                    if (sb != null && sb.length() > 0)
                        this.setField(CHARACTERS_CONTENT, sb.toString());
                    else
                        this.removeField(CHARACTERS_CONTENT);
                }

                return this;
            }

            try
            {
                if (event.isCharacters())
                    this.doAddCharacters(ctx, event, args);
                else
                    this.doParseEventContent(ctx, event, args);
            }
            catch (XMLStreamException e)
            {
                ctx.firePropertyChange(
                    new XMLParserNotification(ctx, XMLParserNotification.EXCEPTION, event,
                        "XML.ExceptionParsingElement",
                        null, e));
            }
        }

        return null;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doAddCharacters(XMLEventParserContext ctx, XMLEvent event, Object... args)
    {
        String s = ctx.getCharacters(event);
        if (WWUtil.isEmpty(s))
            return;

        StringBuilder sb = (StringBuilder) this.getField(CHARACTERS_CONTENT);
        if (sb != null)
            sb.append(s);
        else
            this.setField(CHARACTERS_CONTENT, new StringBuilder(s));
    }

    public String getCharacters()
    {
        return (String) this.getField(CHARACTERS_CONTENT);
    }

    /**
     * Parse an event's sub-elements.
     *
     * @param ctx   a current parser context.
     * @param event the event to parse.
     * @param args  an optional list of arguments that may by used by subclasses.
     *
     * @throws XMLStreamException if an exception occurs during event-stream reading.
     */
    protected void doParseEventContent(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        // Override in subclass to parse an event's sub-elements.
        if (event.isStartElement())
        {
            XMLEventParser parser = this.allocate(ctx, event);

            if (parser == null)
            {
                ctx.firePropertyChange(
                    new XMLParserNotification(ctx, XMLParserNotification.UNRECOGNIZED, event, "XML.UnrecognizedElement",
                        null, event));
                parser = ctx.getUnrecognizedElementParser();

                // Register an unrecognized parser for the element type.
                QName elementName = event.asStartElement().getName();
                if (elementName != null)
                    ctx.registerParser(elementName, parser);
            }

            if (parser != null)
            {
                Object o = parser.parse(ctx, event, args);
                if (o == null)
                    return;

                this.doAddEventContent(o, ctx, event, args);
            }
        }
    }

    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        // Override in subclass if need to react to certain elements.
        this.setField(event.asStartElement().getName(), o);
    }

    /**
     * Parse an event's attributes.
     *
     * @param ctx   a current parser context.
     * @param event the event to parse.
     * @param args  an optional list of arguments that may by used by subclasses.
     *
     * @throws XMLStreamException if an exception occurs during event-stream reading.
     */
    protected void doParseEventAttributes(XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        Iterator iter = event.asStartElement().getAttributes();
        if (iter == null)
            return;

        while (iter.hasNext())
        {
            this.doAddEventAttribute((Attribute) iter.next(), ctx, event, args);
        }
    }

    protected void doAddEventAttribute(Attribute attr, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        // Override in subclass if need to react to certain attributes.
        this.setField(attr.getName(), attr.getValue());
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected String parseCharacterContent(XMLEventParserContext ctx, XMLEvent stringEvent, Object... args)
        throws XMLStreamException
    {
        StringBuilder value = new StringBuilder();

        for (XMLEvent event = ctx.nextEvent(); event != null; event = ctx.nextEvent())
        {
            if (ctx.isEndElement(event, stringEvent))
                return value.length() > 0 ? value.toString() : null;

            if (event.isCharacters())
            {
                String s = ctx.getCharacters(event);
                if (s != null)
                    value.append(s);
            }
        }

        return null;
    }
}
