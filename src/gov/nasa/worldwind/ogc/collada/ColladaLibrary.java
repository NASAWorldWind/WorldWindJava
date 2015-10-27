/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the COLLADA Library element and provides access to its contents. This class can be used for any library
 * element (library_nodes, library_effects, etc.) by specifying a generic parameter that defines the parser for the
 * elements in the library. For example new ColladaLibrary&lt;ColladaNode&gt;(ns) creates a library of ColladaNodes.
 *
 * @author pabercrombie
 * @version $Id: ColladaLibrary.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaLibrary<T> extends ColladaAbstractObject
{
    /**
     * Local name of the elements in the library. This is determined from the name of the library element. For example,
     * if the library element is "library_nodes" then the element name is "node".
     */
    protected String elementName;
    /** Elements in the library. */
    protected List<T> elements = new ArrayList<T>();

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaLibrary(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the elements in the library.
     *
     * @return Elements in the library. Returns an empty list if the library is empty.
     */
    public List<T> getElements()
    {
        return this.elements;
    }

    /** {@inheritDoc} */
    @Override
    public Object parse(XMLEventParserContext context, XMLEvent event, Object... args) throws XMLStreamException
    {
        if (event.isStartDocument())
        {
            String name = event.asStartElement().getName().getLocalPart();
            this.elementName = this.getElementName(name);
        }
        return super.parse(context, event, args);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void setField(String keyName, Object value)
    {
        if (keyName.equals(this.elementName))
        {
            this.elements.add((T) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    protected String getElementName(String libraryName)
    {
        if ("library_nodes".equals(libraryName))
            return "node";
        else if ("library_effects".equals(libraryName))
            return "effect";
        else if ("library_materials".equals(libraryName))
            return "material";
        else if ("library_geometries".equals(libraryName))
            return "geometry";
        else if ("library_images".equals(libraryName))
            return "image";
        else if ("library_visual_scenes".equals(libraryName))
            return "visual_scene";
        return null;
    }
}
