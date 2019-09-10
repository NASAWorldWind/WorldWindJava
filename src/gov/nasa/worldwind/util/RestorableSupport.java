/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.OffsetsList;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * RestorableSupport provides convenient read and write access to restorable state located in a simple XML document
 * format. This document is rooted by the <code>restorableState</code> element. State is stored in
 * <code>stateObject</code> elements. Each <code>stateObject</code> element is identified by its <code>name</code>
 * attribute. The value of a <code>stateObject</code> can either be simple text content, or nested
 * <code>stateObject</code> elements.
 * <p>
 * For example, this document stores four states: the string "Hello World!", the largest value an unsigned byte can
 * hold, the value of PI to six digits, and a boolean "true". 
 * <pre>
 * <code>
 * {@literal <?xml version="1.0" encoding="UTF-8"?>}
 * {@literal <restorableState>}
 *   {@literal <stateObject name="helloWorldString">Hello World!</stateObject>}
 *   {@literal <stateObject name="maxUnsignedByteValue">255</stateObject>}
 *   {@literal <stateObject name="pi">3.141592</stateObject>}
 *   {@literal <stateObject name="booleanTrue">true</stateObject>}
 * {@literal </restorableState>}
 * </code> 
 * </pre>
 * Callers can create a new RestorableSupport with no state content, or create a RestorableSupport from an
 * existing XML document string. Callers can then add state by name and value, and query state by name.
 * RestorableSupport provides convenience methods for addding and querying state values as Strings, Integers, Doubles,
 * and Booleans.
 *
 * @author dcollins
 * @version $Id: RestorableSupport.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see gov.nasa.worldwind.Restorable
 */
public class RestorableSupport
{
    protected static final String DEFAULT_DOCUMENT_ELEMENT_TAG_NAME = "restorableState";
    protected static final String DEFAULT_STATE_OBJECT_TAG_NAME = "stateObject";

    protected org.w3c.dom.Document doc;
    protected javax.xml.xpath.XPath xpath;
    protected String stateObjectTagName;

    /**
     * Creates a new RestorableSupport with no contents and using a specified {@link org.w3c.dom.Document}.
     *
     * @param doc the document to hold the restorable state.
     *
     * @throws IllegalArgumentException if the document reference is null.
     */
    protected RestorableSupport(org.w3c.dom.Document doc)
    {
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doc = doc;
        javax.xml.xpath.XPathFactory pathFactory = javax.xml.xpath.XPathFactory.newInstance();
        this.xpath = pathFactory.newXPath();
        this.stateObjectTagName = DEFAULT_STATE_OBJECT_TAG_NAME;
    }

    /**
     * Creates a new RestorableSupport with no contents.
     *
     * @param documentElementName the name of the restorable state document element.
     *
     * @return a new, empty RestorableSupport instance.
     *
     * @throws IllegalArgumentException if the specified element name is null or empty.
     */
    public static RestorableSupport newRestorableSupport(String documentElementName)
    {
        if (WWUtil.isEmpty(documentElementName))
        {
            String message = Logging.getMessage("nullValue.DocumentElementNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        javax.xml.parsers.DocumentBuilderFactory docBuilderFactory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();

        try
        {
            javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.newDocument();
            // Create the "restorableState" document root element.
            createDocumentElement(doc, documentElementName);
            return new RestorableSupport(doc);
        }
        catch (javax.xml.parsers.ParserConfigurationException e)
        {
            String message = Logging.getMessage("generic.ExceptionCreatingParser");
            Logging.logger().severe(message);
            throw new IllegalStateException(message, e);
        }
    }

    /**
     * Creates a new RestorableSupport with no contents.
     *
     * @return a new, empty RestorableSupport instance.
     */
    public static RestorableSupport newRestorableSupport()
    {
        return newRestorableSupport(DEFAULT_DOCUMENT_ELEMENT_TAG_NAME);
    }

    /**
     * Creates a new RestorableSupport with the contents of the specified state document.
     *
     * @param stateInXml the XML document to parse for state.
     *
     * @return a new RestorableSupport instance with the specified state.
     *
     * @throws IllegalArgumentException If <code>stateInXml</code> is null, or the its contents are not a well formed
     *                                  XML document.
     */
    public static RestorableSupport parse(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        javax.xml.parsers.DocumentBuilderFactory docBuilderFactory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance();

        try
        {
            javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = docBuilder.parse(
                new org.xml.sax.InputSource(new java.io.StringReader(stateInXml)));
            return new RestorableSupport(doc);
        }
        catch (java.io.IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }
        catch (org.xml.sax.SAXException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message, e);
        }
        catch (javax.xml.parsers.ParserConfigurationException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.logger().severe(message);
            throw new IllegalStateException(message, e);
        }
    }

    protected org.w3c.dom.Element getDocumentElement()
    {
        return this.doc.getDocumentElement();
    }

    protected static void createDocumentElement(org.w3c.dom.Document doc, String tagName)
    {
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (tagName == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Document already has a root element.
        if (doc.getDocumentElement() != null)
            return;

        org.w3c.dom.Element elem = doc.createElement(tagName);
        doc.appendChild(elem);
    }

    /**
     * Returns an XML document string describing this RestorableSupport's current set of state objects. If this
     * RestorableSupport cannot be converted, this method returns null.
     *
     * @return an XML state document string.
     */
    public String getStateAsXml()
    {
        javax.xml.transform.TransformerFactory transformerFactory =
            javax.xml.transform.TransformerFactory.newInstance();
        try
        {
            // The StringWriter will receive the document xml.
            java.io.StringWriter stringWriter = new java.io.StringWriter();
            // Attempt to write the Document to the StringWriter.
            javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(
                new javax.xml.transform.dom.DOMSource(this.doc),
                new javax.xml.transform.stream.StreamResult(stringWriter));
            // If successful, return the StringWriter contents as a String.
            return stringWriter.toString();
        }
        catch (javax.xml.transform.TransformerConfigurationException e)
        {
            String message = Logging.getMessage("generic.ExceptionWritingXml");
            Logging.logger().severe(message);
            return null;
        }
        catch (javax.xml.transform.TransformerException e)
        {
            String message = Logging.getMessage("generic.ExceptionWritingXml");
            Logging.logger().severe(message);
            return null;
        }
    }

    /**
     * Returns an XML document string describing this RestorableSupport's current set of state objects. Calling
     * <code>toString</code> is equivalent to calling <code>getStateAsXml</code>.
     *
     * @return an XML state document string.
     */
    public String toString()
    {
        return getStateAsXml();
    }

    /**
     * An interface to the <code>stateObject</code> elements in an XML state document, as defined by {@link
     * gov.nasa.worldwind.util.RestorableSupport}. The <code>name</code> and simple String <code>value</code> of a
     * <code>stateObject</code> can be queried or set through StateObject. This also serves as a context through which
     * nested <code>stateObjects</code> can be found or created.
     */
    public static class StateObject
    {
        final org.w3c.dom.Element elem;

        public StateObject(org.w3c.dom.Element element)
        {
            if (element == null)
            {
                String message = Logging.getMessage("nullValue.ElementIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.elem = element;
        }

        /**
         * Returns the name of this StateObject as a String, or null if this StateObject has no name.
         *
         * @return this StateObject's name.
         */
        public String getName()
        {
            return this.elem.getAttribute("name");
        }

        /**
         * Sets the name of this StateObject to the specified String.
         *
         * @param name the new name of this StateObject.
         *
         * @throws IllegalArgumentException If <code>name</code> is null.
         */
        public void setName(String name)
        {
            if (name == null)
            {
                String message = Logging.getMessage("nullValue.StringIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.elem.setAttribute("name", name);
        }

        /**
         * Returns the value of this StateObject as a String, or null if this StateObject has no value. If there are
         * StateObjects nested beneath this one, then the entire tree beneath this StateObject is converted to a String
         * and returned.
         *
         * @return the value of this StateObject as a String.
         */
        public String getValue()
        {
            return this.elem.getTextContent();
        }

        /**
         * Sets the value of this StateObject to the specified String. If there are StateObjects nested beneath this
         * one, then the entire tree beneath this StateObject is replaced with the specified value.
         *
         * @param value String value that replaces this StateObject's value.
         *
         * @throws IllegalArgumentException If <code>value</code> is null.
         */
        public void setValue(String value)
        {
            if (value == null)
            {
                String message = Logging.getMessage("nullValue.StringIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.elem.setTextContent(value);
        }
    }

    /**
     * Returns the String that represents the document element name.
     *
     * @return String for the document element name.
     */
    public String getDocumentElementTagName()
    {
        return getDocumentElement().getTagName();
    }

    /**
     * Returns the String to be used for each state object's tag name. This tag name is used as a search parameter to
     * find a state object, and is used as the tag name when a new state object is created. The default tag name is
     * "stateObject".
     *
     * @return String to be used for each state object's tag name
     */
    public String getStateObjectTagName()
    {
        return this.stateObjectTagName;
    }

    /**
     * Sets the String to be used for each state object's tag name. This tag name is used as a search parameter to find
     * a state object, and is used as the tag name when a new state object is created. Setting this value does not
     * retroactively set tag names for existing state objects. The default tag name is "stateObject".
     *
     * @param stateObjectTagName String to be used for each state object's tag name.
     *
     * @throws IllegalArgumentException If <code>stateObjectTagName</code> is null.
     */
    public void setStateObjectTagName(String stateObjectTagName)
    {
        if (stateObjectTagName == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.stateObjectTagName = stateObjectTagName;
    }

    protected StateObject findStateObject(org.w3c.dom.Node context, String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Search for the state element with the specified name.
        String expression = String.format("%s[@name=\"%s\"]", getStateObjectTagName(), name);
        try
        {
            Object result = this.xpath.evaluate(
                expression,
                // If non-null, search from the specified context. Otherwise, search from the
                // document root element.
                (context != null ? context : getDocumentElement()),
                javax.xml.xpath.XPathConstants.NODE);
            if (result == null)
                return null;

            // If the result is an Element node, return a new StateObject with the result as its content.
            // Otherwise return null.
            return (result instanceof org.w3c.dom.Element) ? new StateObject((org.w3c.dom.Element) result) : null;
        }
        catch (javax.xml.xpath.XPathExpressionException e)
        {
            return null;
        }
    }

    protected StateObject[] findAllStateObjects(org.w3c.dom.Node context, String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Search for the state elements beneath the context with the specified name.
        String expression;
        if (name.length() != 0)
            expression = String.format("%s[@name=\"%s\"]", getStateObjectTagName(), name);
        else
            expression = String.format("%s//.", getStateObjectTagName());

        try
        {
            Object result = this.xpath.evaluate(
                expression,
                // If non-null, search from the specified context. Otherwise, search from the
                // document root element.
                (context != null ? context : getDocumentElement()),
                javax.xml.xpath.XPathConstants.NODESET);
            if (result == null
                || !(result instanceof org.w3c.dom.NodeList)
                || ((org.w3c.dom.NodeList) result).getLength() == 0)
            {
                return null;
            }

            // If the result is a NodeList, return an array of StateObjects for each Element node in that list.
            org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) result;
            ArrayList<StateObject> stateObjectList = new ArrayList<StateObject>();
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node instanceof org.w3c.dom.Element)
                {
                    stateObjectList.add(new StateObject((org.w3c.dom.Element) node));
                }
            }
            StateObject[] stateObjectArray = new StateObject[stateObjectList.size()];
            stateObjectList.toArray(stateObjectArray);
            return stateObjectArray;
        }
        catch (javax.xml.xpath.XPathExpressionException e)
        {
            return null;
        }
    }

    protected StateObject[] extractStateObjects(org.w3c.dom.Element context)
    {
        org.w3c.dom.NodeList nodeList = (context != null ? context : getDocumentElement()).getChildNodes();

        ArrayList<StateObject> stateObjectList = new ArrayList<StateObject>();
        if (nodeList != null)
        {
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node instanceof org.w3c.dom.Element
                    && node.getNodeName() != null
                    && node.getNodeName().equals(getStateObjectTagName()))
                {
                    stateObjectList.add(new StateObject((org.w3c.dom.Element) node));
                }
            }
        }

        StateObject[] stateObjectArray = new StateObject[stateObjectList.size()];
        stateObjectList.toArray(stateObjectArray);
        return stateObjectArray;
    }

    protected StateObject createStateObject(org.w3c.dom.Element context, String name, String value)
    {
        return createStateObject(context, name, value, false);
    }

    protected StateObject createStateObject(org.w3c.dom.Element context, String name, String value, boolean escapeValue)
    {
        org.w3c.dom.Element elem = this.doc.createElement(getStateObjectTagName());

        // If non-null, name goes in an attribute entitled "name".
        if (name != null)
            elem.setAttribute("name", name);

        // If non-null, value goes in the element text content.
        if (value != null)
        {
            // If escapeValue is true, we place value in a CDATA node beneath elem.
            if (escapeValue)
                elem.appendChild(this.doc.createCDATASection(value));
                // Otherwise, just set the text value of elem normally.
            else
                elem.setTextContent(value);
        }

        // If non-null, add the StateObject element to the specified context. Otherwise, add it to the
        // document root element.
        (context != null ? context : getDocumentElement()).appendChild(elem);

        return new StateObject(elem);
    }

    protected boolean containsElement(org.w3c.dom.Element elem)
    {
        if (elem == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return elem.getOwnerDocument().equals(this.doc);
    }

    /**
     * Returns the StateObject with the specified <code>name</code>. This searches the StateObjects directly beneath the
     * document root. If no StateObject with that name exists, this method returns null.
     *
     * @param name the StateObject name to search for.
     *
     * @return the StateObject instance, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public StateObject getStateObject(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getStateObject(null, name);
    }

    /**
     * Returns the StateObject with the specified <code>name</code>. If context is not null, this method searches the
     * StateObjects directly below the specified <code>context</code>. Otherwise, this method searches the StateObjects
     * directly beneath the document root. If no StateObject with that name exists, this method returns null.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the StateObject instance, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public StateObject getStateObject(StateObject context, String name)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return findStateObject(context != null ? context.elem : null, name);
    }

    /**
     * Returns all StateObjects directly beneath the a context StateObject. If context is not null, this method returns
     * all the StateObjects directly below the specified <code>context</code>. Otherwise, this method returns all the
     * StateObjects directly beneath the document root.
     *
     * @param context StateObject context to search, or null to search the document root.
     *
     * @return an array of the StateObject instances, which has zero length if none exist.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public StateObject[] getAllStateObjects(StateObject context)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return extractStateObjects(context != null ? context.elem : null);
    }

    /**
     * Returns any StateObjects directly beneath the document root that have the specified <code>name</code>. If no
     * StateObjects with that name exist, this method returns a valid StateObject array with zero length.
     *
     * @param name the StateObject name to search for.
     *
     * @return an array of the StateObject instances, which has zero length if none exist.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public StateObject[] getAllStateObjects(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return getAllStateObjects(null, name);
    }

    /**
     * Returns all StateObjects with the specified <code>name</code>. If context is not null, this method searches the
     * StateObjects directly below the specified <code>context</code>. Otherwise, this method searches the StateObjects
     * directly beneath the document root. If no StateObjects with that name exist, this method returns a valid
     * StateObject array with zero length.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return an array of the StateObject instances, which has zero length if none exist.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public StateObject[] getAllStateObjects(StateObject context, String name)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return findAllStateObjects(context != null ? context.elem : null, name);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code>. The new StateObject is placed directly beneath the
     * document root. If a StateObject with this name already exists, a new one is still created.
     *
     * @param name the new StateObject's name.
     *
     * @return the new StateObject instance.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public StateObject addStateObject(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return addStateObject(null, name);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code>. If <code>context</code> is not null, the new
     * StateObject is nested directly beneath the specified <code>context</code>. Otherwise, the new StateObject is
     * placed directly beneath the document root. If a StateObject with this name already exists, a new one is still
     * created.
     *
     * @param context the StateObject under which the new StateObject is created, or null to place it under the document
     *                root.
     * @param name    the new StateObject's name.
     *
     * @return the new StateObject instance.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public StateObject addStateObject(StateObject context, String name)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Create the state object with no value.
        return createStateObject(context != null ? context.elem : null, name, null);
    }

    /*************************************************************************************************************/
    /** Convenience methods for adding and querying state values. **/
    /*************************************************************************************************************/

    /**
     * Returns the value of the StateObject as a String.
     *
     * @param stateObject the StateObject that is converted to a String.
     *
     * @return the value of the StateObject as a String.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public String getStateObjectAsString(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return stateObject.getValue();
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a String. This method searches the
     * StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of that
     * StateObject is not a String, this method returns null.
     *
     * @param name the StateObject name to search for.
     *
     * @return the value of the StateObject as a String, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public String getStateValueAsString(String name)
    {
        return getStateValueAsString(null, name);
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a String. If context is not null,
     * this method searches the StateObjects directly below the specified <code>context</code>. Otherwise, this method
     * searches the StateObjects directly beneath the document root. If no StateObject with that name exists, or if the
     * value of that StateObject is not a String, this method returns null.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the value of the StateObject as a String, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public String getStateValueAsString(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsString(stateObject);
    }

    /**
     * Returns the value of the StateObject as an Integer.
     *
     * @param stateObject the StateObject that is converted to an Integer.
     *
     * @return the value of the StateObject as an Integer.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Integer getStateObjectAsInteger(StateObject stateObject)
    {
        String stringValue = getStateObjectAsString(stateObject);
        if (stringValue == null)
            return null;

        try
        {
            return Integer.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", stringValue);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as an Integer. This method searches the
     * StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of that
     * StateObject is not an Integer, this method returns null.
     *
     * @param name the StateObject name to search for.
     *
     * @return the value of the StateObject as an Integer, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public Integer getStateValueAsInteger(String name)
    {
        return getStateValueAsInteger(null, name);
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as an Integer. If context is not null,
     * this method searches the StateObjects directly below the specified <code>context</code>. Otherwise, this method
     * searches the StateObjects directly beneath the document root. If no StateObject with that name exists, or if the
     * value of that StateObject is not an Integer, this method returns null.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the value of the StateObject as an Integer, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public Integer getStateValueAsInteger(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsInteger(stateObject);
    }

    /**
     * Returns the value of the StateObject as a Double.
     *
     * @param stateObject the StateObject that is converted to a Double.
     *
     * @return the value of the StateObject as a Double.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Double getStateObjectAsDouble(StateObject stateObject)
    {
        String stringValue = getStateObjectAsString(stateObject);
        if (stringValue == null)
            return null;

        try
        {
            return Double.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", stringValue);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Double. This method searches the
     * StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of that
     * StateObject is not a Double, this method returns null.
     *
     * @param name the StateObject name to search for.
     *
     * @return the value of the StateObject as a Double, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public Double getStateValueAsDouble(String name)
    {
        return getStateValueAsDouble(null, name);
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Double. If context is not null,
     * this method searches the StateObjects directly below the specified <code>context</code>. Otherwise, this searches
     * the StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of
     * that StateObject is not a Double, this method returns null.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the value of the StateObject as a Double, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public Double getStateValueAsDouble(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsDouble(stateObject);
    }

    /**
     * Returns the value of the StateObject as a Long.
     *
     * @param stateObject the StateObject that is converted to a Long.
     *
     * @return the value of the StateObject as a Long.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Long getStateObjectAsLong(StateObject stateObject)
    {
        String stringValue = getStateObjectAsString(stateObject);
        if (stringValue == null)
            return null;

        try
        {
            return Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", stringValue);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Long. This searches the
     * StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of that
     * StateObject is not a Long, this method returns null.
     *
     * @param name the StateObject name to search for.
     *
     * @return the value of the StateObject as a Long, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public Long getStateValueAsLong(String name)
    {
        return getStateValueAsLong(null, name);
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Long. If context is not null, this
     * method searches the StateObjects directly below the specified <code>context</code>. Otherwise, this method
     * searches the StateObjects directly beneath the document root. If no StateObject with that name exists, or if the
     * value of that StateObject is not a Double, this method returns null.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the value of the StateObject as a Long, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public Long getStateValueAsLong(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsLong(stateObject);
    }

    /**
     * Returns the value of the StateObject as a Float.
     *
     * @param stateObject the StateObject that is converted to a Float.
     *
     * @return the value of the StateObject as a Float.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Float getStateObjectAsFloat(StateObject stateObject)
    {
        String stringValue = getStateObjectAsString(stateObject);
        if (stringValue == null)
            return null;

        try
        {
            return Float.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", stringValue);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Float. This method searches the
     * StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of that
     * StateObject is not a Float, this method returns null.
     *
     * @param name the StateObject name to search for.
     *
     * @return the value of the StateObject as a Float, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public Float getStateValueAsFloat(String name)
    {
        return getStateValueAsFloat(null, name);
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Float. If context is not null,
     * this method searches the StateObjects directly below the specified <code>context</code>. Otherwise, this searches
     * the StateObjects directly beneath the document root. If no StateObject with that name exists, or if the value of
     * that StateObject is not a Float, this method returns null.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the value of the StateObject as a Float, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public Float getStateValueAsFloat(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsFloat(stateObject);
    }

    /**
     * Returns the value of the StateObject as a Boolean. The Boolean value returned is equivalent to passing the
     * StateObject's String value to <code>Boolean.valueOf</code>.
     *
     * @param stateObject the StateObject that is converted to a Boolean.
     *
     * @return the value of the StateObject as a Boolean.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Boolean getStateObjectAsBoolean(StateObject stateObject)
    {
        String stringValue = getStateObjectAsString(stateObject);
        if (stringValue == null)
            return null;

        try
        {
            return Boolean.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", stringValue);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Boolean. This method searches the
     * StateObjects directly beneath the document root. If no StateObject with that name exists, this method returns
     * null. Otherwise, the Boolean value returned is equivalent to passing the StateObject's String value to
     * <code>Boolean.valueOf</code>.
     *
     * @param name the StateObject name to search for.
     *
     * @return the value of the StateObject as a Boolean, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public Boolean getStateValueAsBoolean(String name)
    {
        return getStateValueAsBoolean(null, name);
    }

    /**
     * Returns the value of the StateObject with the specified <code>name</code> as a Boolean. If context is not null,
     * this method searches the StateObjects directly below the specified <code>context</code>. Otherwise, this method
     * searches the StateObjects directly beneath the document root. If no StateObject with that name exists, this
     * method returns null. Otherwise, the Boolean value returned is equivalent to passing the StateObject's String
     * value to <code>Boolean.valueOf</code>.
     *
     * @param context StateObject context to search, or null to search the document root.
     * @param name    the StateObject name to search for.
     *
     * @return the value of the StateObject as a Boolean, or null if none exists.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public Boolean getStateValueAsBoolean(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsBoolean(stateObject);
    }

    /**
     * Returns the value of the StateObject as a LatLon.
     *
     * @param stateObject the StateObject that is converted to a LatLon.
     *
     * @return the value of the StateObject as a LatLon.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public LatLon getStateObjectAsLatLon(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double lat = getStateValueAsDouble(stateObject, "latitudeDegrees");
        Double lon = getStateValueAsDouble(stateObject, "longitudeDegrees");
        if (lat == null || lon == null)
        {
            String message = Logging.getMessage("generic.ConversionError", stateObject.getName());
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            return null;
        }

        return LatLon.fromDegrees(lat, lon);
    }

    public LatLon getStateValueAsLatLon(String name)
    {
        return getStateValueAsLatLon(null, name);
    }

    public LatLon getStateValueAsLatLon(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsLatLon(stateObject);
    }

    /**
     * Returns the value of the StateObject as a Position.
     *
     * @param stateObject the StateObject that is converted to a Position.
     *
     * @return the value of the StateObject as a Position.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Position getStateObjectAsPosition(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double lat = getStateValueAsDouble(stateObject, "latitudeDegrees");
        Double lon = getStateValueAsDouble(stateObject, "longitudeDegrees");
        Double elevation = getStateValueAsDouble(stateObject, "elevation");
        if (lat == null || lon == null || elevation == null)
        {
            String message = Logging.getMessage("generic.ConversionError", stateObject.getName());
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            return null;
        }

        return Position.fromDegrees(lat, lon, elevation);
    }

    public Position getStateValueAsPosition(String name)
    {
        return getStateValueAsPosition(null, name);
    }

    public Position getStateValueAsPosition(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsPosition(stateObject);
    }

    /**
     * Returns the value of the StateObject as a List of LatLons.
     *
     * @param stateObject the StateObject that is converted to a List of LatLons.
     *
     * @return the value of the StateObject as a List of LatLons.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public List<LatLon> getStateObjectAsLatLonList(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject[] llsos = getAllStateObjects(stateObject, "location");
        if (llsos == null || llsos.length == 0)
            return null;

        ArrayList<LatLon> outList = new ArrayList<LatLon>(llsos.length);

        for (RestorableSupport.StateObject llso : llsos)
        {
            if (llso != null)
            {
                LatLon ll = getStateObjectAsLatLon(llso);
                if (ll != null)
                    outList.add(ll);
            }
        }

        return outList;
    }

    public List<LatLon> getStateValueAsLatLonList(String name)
    {
        return getStateValueAsLatLonList(null, name);
    }

    public List<LatLon> getStateValueAsLatLonList(StateObject context, String name)
    {
        RestorableSupport.StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsLatLonList(stateObject);
    }

    /**
     * Returns the value of the StateObject as a HashMap of &lt;Integer, OffsetsList&gt; pairs.
     *
     * @param stateObject the StateObject that is converted to a HashMap of OffsetsLists.
     *
     * @return the value of the StateObject as a HashMap of &lt;Integer, OffsetsList&gt; pairs.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */

    public HashMap<Integer, OffsetsList> getStateObjectAsOffsetsList(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject[] offsetsLists = getAllStateObjects(stateObject, "face");
        if (offsetsLists == null || offsetsLists.length == 0)
            return null;

        HashMap<Integer, OffsetsList> outList = new HashMap<Integer, OffsetsList>();

        int index = 0;
        for (RestorableSupport.StateObject faceOffsets : offsetsLists)
        {
            if (faceOffsets != null)
            {
                OffsetsList offsets = getStateObjectAsOffsets(faceOffsets);
                if (offsets != null)
                    outList.put(index, offsets);
            }
            index++;
        }

        return outList;
    }

    public HashMap<Integer, OffsetsList> getStateValueAsOffsetsList(String name)
    {
        return getStateValueAsOffsetsList(null, name);
    }

    public HashMap<Integer, OffsetsList> getStateValueAsOffsetsList(StateObject context, String name)
    {
        RestorableSupport.StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsOffsetsList(stateObject);
    }

    /**
     * Returns the value of the StateObject as a OffsetsList.
     *
     * @param stateObject the StateObject that is converted to a OffsetsList.
     *
     * @return the value of the StateObject as a OffsetsList.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */

    public OffsetsList getStateObjectAsOffsets(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float[] upperLeftOffset = getStateValueAsOffsetPair(stateObject, "upperLeftOffset");
        float[] upperRightOffset = getStateValueAsOffsetPair(stateObject, "upperRightOffset");
        float[] lowerLeftOffset = getStateValueAsOffsetPair(stateObject, "lowerLeftOffset");
        float[] lowerRightOffset = getStateValueAsOffsetPair(stateObject, "lowerRightOffset");

        if (upperLeftOffset == null || upperRightOffset == null || lowerLeftOffset == null || lowerRightOffset == null)
        {
            String message = Logging.getMessage("generic.ConversionError", stateObject.getName());
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            return null;
        }

        OffsetsList offsets = new OffsetsList();

        offsets.setOffset(0, upperLeftOffset[0], upperLeftOffset[1]);
        offsets.setOffset(1, upperRightOffset[0], upperRightOffset[1]);
        offsets.setOffset(2, lowerLeftOffset[0], lowerLeftOffset[1]);
        offsets.setOffset(3, lowerRightOffset[0], lowerRightOffset[1]);

        return offsets;
    }

    public OffsetsList getStateValueAsOffsets(String name)
    {
        return getStateValueAsOffsets(null, name);
    }

    public OffsetsList getStateValueAsOffsets(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsOffsets(stateObject);
    }

    /**
     * Returns the value of the StateObject as a float[].
     *
     * @param stateObject the StateObject that is converted to a float[].
     *
     * @return the value of the StateObject as a float[].
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */

    public float[] getStateObjectAsOffsetPair(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Float uOffset = getStateValueAsFloat(stateObject, "uOffset");
        Float vOffset = getStateValueAsFloat(stateObject, "vOffset");

        if (uOffset == null || vOffset == null)
        {
            String message = Logging.getMessage("generic.ConversionError", stateObject.getName());
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            return null;
        }

        float[] offsetPair;
        offsetPair = new float[] {uOffset, vOffset};

        return offsetPair;
    }

    public float[] getStateValueAsOffsetPair(String name)
    {
        return getStateValueAsOffsetPair(null, name);
    }

    public float[] getStateValueAsOffsetPair(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsOffsetPair(stateObject);
    }

    /**
     * Returns the value of the StateObject as a HashMap of &lt;Integer, Object&gt; pairs, representing the shape's
     * imageSources.
     *
     * @param stateObject the StateObject that is converted to a HashMap of imageSources.
     *
     * @return the value of the StateObject as a HashMap of &lt;Integer, Object&gt; pairs.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */

    public HashMap<Integer, Object> getStateObjectAsImageSourceList(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject[] imageSourceList = getAllStateObjects(stateObject, "imageSource");
        if (imageSourceList == null || imageSourceList.length == 0)
            return null;

        HashMap<Integer, Object> outList = new HashMap<Integer, Object>();

        int index = 0;
        for (RestorableSupport.StateObject imageSource : imageSourceList)
        {
            if (imageSource != null)
            {
                String path = getStateObjectAsString(imageSource);
                if (path != null)
                    outList.put(index, path);
                else
                    outList.put(index, null);
            }
            index++;
        }

        return outList;
    }

    public HashMap<Integer, Object> getStateValueAsImageSourceList(String name)
    {
        return getStateValueAsImageSourceList(null, name);
    }

    public HashMap<Integer, Object> getStateValueAsImageSourceList(StateObject context, String name)
    {
        RestorableSupport.StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsImageSourceList(stateObject);
    }

    /**
     * Returns the value of the StateObject as a Sector.
     *
     * @param stateObject the StateObject that is converted to a Sector.
     *
     * @return the value of the StateObject as a Sector.
     *
     * @throws IllegalArgumentException If <code>stateObject</code> is null, or does not belong to this
     *                                  RestorableSupport.
     */
    public Sector getStateObjectAsSector(StateObject stateObject)
    {
        if (stateObject == null)
        {
            String message = Logging.getMessage("nullValue.StateObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!containsElement(stateObject.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double minLat = getStateValueAsDouble(stateObject, "minLatitudeDegrees");
        Double maxLat = getStateValueAsDouble(stateObject, "maxLatitudeDegrees");
        Double minLon = getStateValueAsDouble(stateObject, "minLongitudeDegrees");
        Double maxLon = getStateValueAsDouble(stateObject, "maxLongitudeDegrees");
        if (minLat == null || maxLat == null || minLon == null || maxLon == null)
        {
            String message = Logging.getMessage("generic.ConversionError", stateObject.getName());
            Logging.logger().log(java.util.logging.Level.SEVERE, message);
            return null;
        }

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    public Sector getStateValueAsSector(String name)
    {
        return this.getStateValueAsSector(null, name);
    }

    public Sector getStateValueAsSector(StateObject context, String name)
    {
        RestorableSupport.StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsSector(stateObject);
    }

    public java.awt.Color getStateObjectAsColor(StateObject stateObject)
    {
        String stringValue = getStateObjectAsString(stateObject);
        if (stringValue == null)
            return null;

        return decodeColor(stringValue);
    }

    public Color getStateValueAsColor(StateObject context, String name)
    {
        StateObject stateObject = getStateObject(context, name);
        if (stateObject == null)
            return null;

        return getStateObjectAsColor(stateObject);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and String <code>value</code>. The new StateObject is
     * placed beneath the document root. If a StateObject with this name already exists, a new one is still created.
     *
     * @param name  the new StateObject's name.
     * @param value the new StateObject's String value.
     *
     * @throws IllegalArgumentException If either <code>name</code> or <code>value</code> is null.
     */
    public void addStateValueAsString(String name, String value)
    {
        addStateValueAsString(null, name, value, false);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and String <code>value</code>. The new StateObject is
     * placed beneath the document root. If a StateObject with this name already exists, a new one is still created. If
     * <code>escapeValue</code> is true, the text in <code>value</code> is escaped in a CDATA section. Otherwise, no
     * special processing is performed on <code>value</code>. Once <code>value</code> has been escaped and added, it can
     * be extracted exactly like any other String value.
     *
     * @param name        the new StateObject's name.
     * @param value       the new StateObject's String value.
     * @param escapeValue whether to escape the String <code>value</code> or not.
     *
     * @throws IllegalArgumentException If either <code>name</code> or <code>value</code> is null.
     */
    public void addStateValueAsString(String name, String value, boolean escapeValue)
    {
        addStateValueAsString(null, name, value, escapeValue);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and String <code>value</code>. If
     * <code>context</code> is not null, the new StateObject is nested directly beneath the specified
     * <code>context</code>. Otherwise, the new StateObject is placed directly beneath the document root. If a
     * StateObject with this name already exists, a new one is still created.
     *
     * @param context the StateObject context under which the new StateObject is created, or null to place it under the
     *                document root.
     * @param name    the new StateObject's name.
     * @param value   the new StateObject's String value.
     *
     * @throws IllegalArgumentException If either <code>name</code> or <code>value</code> is null, or if
     *                                  <code>context</code> is not null and does not belong to this RestorableSupport.
     */
    public void addStateValueAsString(StateObject context, String name, String value)
    {
        addStateValueAsString(context, name, value, false);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and String <code>value</code>. If
     * <code>context</code> is not null, the new StateObject is nested directly beneath the specified
     * <code>context</code>. Otherwise, the new StateObject is placed directly beneath the document root. If a
     * StateObject with this name already exists, a new one is still created. If <code>escapeValue</code> is true, the
     * text in <code>value</code> is escaped in a CDATA section. Otherwise, no special processing is performed on
     * <code>value</code>. Once <code>value</code> has been escaped and added, it can be extracted exactly like any
     * other String value.
     *
     * @param context     the StateObject context under which the new StateObject is created, or null to place it under
     *                    the document root.
     * @param name        the new StateObject's name.
     * @param value       the new StateObject's String value.
     * @param escapeValue whether to escape the String <code>value</code> or not.
     *
     * @throws IllegalArgumentException If either <code>name</code> or <code>value</code> is null, or if
     *                                  <code>context</code> is not null and does not belong to this RestorableSupport.
     */
    public void addStateValueAsString(StateObject context, String name, String value, boolean escapeValue)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null || value == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        createStateObject(context != null ? context.elem : null, name, value, escapeValue);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and Integer <code>value</code>. The new StateObject
     * is placed beneath the document root. If a StateObject with this name already exists, a new one is still created.
     *
     * @param name     the new StateObject's name.
     * @param intValue the new StateObject's Integer value.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public void addStateValueAsInteger(String name, int intValue)
    {
        addStateValueAsInteger(null, name, intValue);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and Integer <code>value</code>. If
     * <code>context</code> is not null, the new StateObject is nested directly beneath the specified
     * <code>context</code>. Otherwise, the new StateObject is placed directly beneath the document root. If a
     * StateObject with this name already exists, a new one is still created.
     *
     * @param context  the StateObject context under which the new StateObject is created, or null to place it under the
     *                 document root.
     * @param name     the new StateObject's name.
     * @param intValue the new StateObject's Integer value.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public void addStateValueAsInteger(StateObject context, String name, int intValue)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addStateValueAsString(context, name, Integer.toString(intValue));
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and Double <code>value</code>. The new StateObject is
     * placed beneath the document root. If a StateObject with this name already exists, a new one is still created.
     *
     * @param name        the new StateObject's name.
     * @param doubleValue the new StateObject's Double value.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public void addStateValueAsDouble(String name, double doubleValue)
    {
        addStateValueAsDouble(null, name, doubleValue);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and Double <code>value</code>. If
     * <code>context</code> is not null, the new StateObject is nested directly beneath the specified
     * <code>context</code>. Otherwise, the new StateObject is placed directly beneath the document root. If a
     * StateObject with this name already exists, a new one is still created.
     *
     * @param context     the StateObject context under which the new StateObject is created, or null to place it under
     *                    the document root.
     * @param name        the new StateObject's name.
     * @param doubleValue the new StateObject's Double value.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public void addStateValueAsDouble(StateObject context, String name, double doubleValue)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addStateValueAsString(context, name, Double.toString(doubleValue));
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and Boolean <code>value</code>. The new StateObject
     * is placed beneath the document root. If a StateObject with this name already exists, a new one is still created.
     *
     * @param name         the new StateObject's name.
     * @param booleanValue the new StateObject's Boolean value.
     *
     * @throws IllegalArgumentException If <code>name</code> is null.
     */
    public void addStateValueAsBoolean(String name, boolean booleanValue)
    {
        addStateValueAsBoolean(null, name, booleanValue);
    }

    /**
     * Adds a new StateObject with the specified <code>name</code> and Boolean <code>value</code>. If
     * <code>context</code> is not null, the new StateObject is nested directly beneath the specified
     * <code>context</code>. Otherwise, the new StateObject is placed directly beneath the document root. If a
     * StateObject with this name already exists, a new one is still created.
     *
     * @param context      the StateObject context under which the new StateObject is created, or null to place it under
     *                     the document root.
     * @param name         the new StateObject's name.
     * @param booleanValue the new StateObject's Boolean value.
     *
     * @throws IllegalArgumentException If <code>name</code> is null, or if <code>context</code> is not null and does
     *                                  not belong to this RestorableSupport.
     */
    public void addStateValueAsBoolean(StateObject context, String name, boolean booleanValue)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addStateValueAsString(context, name, Boolean.toString(booleanValue));
    }

    public void addStateValueAsLatLon(String name, LatLon location)
    {
        addStateValueAsLatLon(null, name, location);
    }

    public void addStateValueAsLatLon(StateObject context, String name, LatLon location)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (location == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject pStateObj = addStateObject(context, name);
        if (pStateObj != null)
        {
            addStateValueAsDouble(pStateObj, "latitudeDegrees", location.getLatitude().degrees);
            addStateValueAsDouble(pStateObj, "longitudeDegrees", location.getLongitude().degrees);
        }
    }

    public void addStateValueAsPosition(String name, Position position)
    {
        addStateValueAsPosition(null, name, position);
    }

    public void addStateValueAsPosition(StateObject context, String name, Position position)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject pStateObj = addStateObject(context, name);
        if (pStateObj != null)
        {
            addStateValueAsDouble(pStateObj, "latitudeDegrees", position.getLatitude().degrees);
            addStateValueAsDouble(pStateObj, "longitudeDegrees", position.getLongitude().degrees);
            addStateValueAsDouble(pStateObj, "elevation", position.getElevation());
        }
    }

    public void addStateValueAsLatLonList(StateObject context, String name, Iterable<? extends LatLon> locations)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LatLonListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject stateObject = addStateObject(context, name);
        if (stateObject != null)
        {
            for (LatLon ll : locations)
            {
                addStateValueAsLatLon(stateObject, "location", ll);
            }
        }
    }

    public void addStateValueAsOffsetsList(StateObject context, String name, Map<Integer, OffsetsList> offsets)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (offsets == null)
        {
            String message = Logging.getMessage("nullValue.OffsetListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject stateObject = addStateObject(context, name);
        for (Object key : ((Map) offsets).keySet())
        {
            addStateValueAsOffsets(stateObject, "face", offsets.get(key));
        }
    }

    public void addStateValueAsOffsets(String name, OffsetsList offsets)
    {
        addStateValueAsOffsets(null, name, offsets);
    }

    public void addStateValueAsOffsets(StateObject context, String name, OffsetsList offsets)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (offsets == null)
        {
            String message = Logging.getMessage("nullValue.OffsetsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject pStateObj = addStateObject(context, name);
        if (pStateObj != null)
        {
            addStateValueAsOffsetPair(pStateObj, "upperLeftOffset", offsets.getOffset(0));     // upper left uv offset
            addStateValueAsOffsetPair(pStateObj, "upperRightOffset", offsets.getOffset(1));    // upper right uv offset
            addStateValueAsOffsetPair(pStateObj, "lowerLeftOffset", offsets.getOffset(2));     // lower left uv offset
            addStateValueAsOffsetPair(pStateObj, "lowerRightOffset", offsets.getOffset(3));    // lower right uv offset
        }
    }

    public void addStateValueAsOffsetPair(StateObject context, String name, float[] offsetPair)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (offsetPair == null)
        {
            String message = Logging.getMessage("nullValue.OffsetPairIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject pStateObj = addStateObject(context, name);
        if (pStateObj != null)
        {
            addStateValueAsDouble(pStateObj, "uOffset", offsetPair[0]);
            addStateValueAsDouble(pStateObj, "vOffset", offsetPair[1]);
        }
    }

    public void addStateValueAsImageSourceList(String name, Map<Integer, Object> imageSources, int faceCount)
    {
        addStateValueAsImageSourceList(null, name, imageSources, faceCount);
    }

    public void addStateValueAsImageSourceList(StateObject context, String name,
        Map<Integer, Object> imageSources, int faceCount)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (imageSources == null)
        {
            String message = Logging.getMessage("nullValue.ImageSourcesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject stateObject = addStateObject(context, name);
        for (int i = 0; i < faceCount; i++)
        {
            if (imageSources.get(i) == null)
                addStateValueAsString(stateObject, "imageSource", "null");
            else
                addStateValueAsString(stateObject, "imageSource", imageSources.get(i).toString());
        }
    }

    public void addStateValueAsSector(String name, Sector sector)
    {
        addStateValueAsSector(null, name, sector);
    }

    public void addStateValueAsSector(StateObject context, String name, Sector sector)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport.StateObject pStateObj = addStateObject(context, name);
        if (pStateObj != null)
        {
            addStateValueAsDouble(pStateObj, "minLatitudeDegrees", sector.getMinLatitude().degrees);
            addStateValueAsDouble(pStateObj, "maxLatitudeDegrees", sector.getMaxLatitude().degrees);
            addStateValueAsDouble(pStateObj, "minLongitudeDegrees", sector.getMinLongitude().degrees);
            addStateValueAsDouble(pStateObj, "maxLongitudeDegrees", sector.getMaxLongitude().degrees);
        }
    }

    public void addStateValueAsColor(String name, java.awt.Color color)
    {
        addStateValueAsColor(null, name, color);
    }

    public void addStateValueAsColor(StateObject context, String name, java.awt.Color color)
    {
        if (context != null && !containsElement(context.elem))
        {
            String message = Logging.getMessage("RestorableSupport.InvalidStateObject");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String value = encodeColor(color);
        addStateValueAsString(context, name, value);
    }

    /*************************************************************************************************************/
    /** Convenience methods for adding and querying state values. **/
    /*************************************************************************************************************/

    /**
     * Returns a String encoding of the specified <code>color</code>. The Color can be restored with a call to {@link
     * #decodeColor(String)}.
     *
     * @param color Color to encode.
     *
     * @return String encoding of the specified <code>color</code>.
     *
     * @throws IllegalArgumentException If <code>color</code> is null.
     */
    public static String encodeColor(java.awt.Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Encode the red, green, blue, and alpha components
        int rgba = (color.getRed() & 0xFF) << 24
            | (color.getGreen() & 0xFF) << 16
            | (color.getBlue() & 0xFF) << 8
            | (color.getAlpha() & 0xFF);
        return String.format("%#08X", rgba);
    }

    /**
     * Returns the Color described by the String <code>encodedString</code>. This understands Colors encoded with a call
     * to {@link #encodeColor(java.awt.Color)}. If <code>encodedString</code> cannot be decoded, this method returns
     * null.
     *
     * @param encodedString String to decode.
     *
     * @return Color decoded from the specified <code>encodedString</code>, or null if the String cannot be decoded.
     *
     * @throws IllegalArgumentException If <code>encodedString</code> is null.
     */
    public static java.awt.Color decodeColor(String encodedString)
    {
        if (encodedString == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!encodedString.startsWith("0x") && !encodedString.startsWith("0X"))
            return null;

        // The hexadecimal representation for an RGBA color can result in a value larger than
        // Integer.MAX_VALUE (for example, 0XFFFF). Therefore we decode the string as a long,
        // then keep only the lower four bytes.
        Long longValue;
        try
        {
            longValue = Long.parseLong(encodedString.substring(2), 16);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", encodedString);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }

        int i = (int) (longValue & 0xFFFFFFFFL);
        return new java.awt.Color(
            (i >> 24) & 0xFF,
            (i >> 16) & 0xFF,
            (i >> 8) & 0xFF,
            i & 0xFF);
    }

    public static void adjustTitleAndDisplayName(AVList params)
    {
        String displayName = params.getStringValue(AVKey.DISPLAY_NAME);
        if (displayName == null && params.getValue(AVKey.TITLE) != null)
            params.setValue(AVKey.DISPLAY_NAME, params.getValue(AVKey.TITLE));
    }
}
