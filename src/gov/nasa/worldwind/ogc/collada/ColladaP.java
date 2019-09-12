/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Represents the COLLADA <i>p</i> element and provides access to its contents. The <i>p</i> element is a sort of
 * indirect index that determines how vertices, normals, and texture coordinates are read from the <i>input</i>s. This
 * tutorial is helpful for understanding how the <i>p</i> element relates to COLLADA geometry: <a
 * href="http://www.wazim.com/Collada_Tutorial_1.htm" target="_blank">http://www.wazim.com/Collada_Tutorial_1.htm</a>.
 *
 * @author pabercrombie
 * @version $Id: ColladaP.java 662 2012-06-26 19:05:46Z pabercrombie $
 */
public class ColladaP extends ColladaAbstractObject
{
    /** Indices contained in this element. */
    protected int[] indices;

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaP(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the contents of the P element.
     *
     * @return Array of indices defined by this element.
     */
    public int[] getIndices()
    {
        return this.indices;
    }

    /** {@inheritDoc} */
    @Override
    public Object parse(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException
    {
        super.parse(ctx, event, args);

        if (this.hasField(CHARACTERS_CONTENT))
        {
            String s = (String) this.getField(CHARACTERS_CONTENT);
            if (!WWUtil.isEmpty(s))
                this.indices = this.parseInts(s);

            // Don't need to keep string version of the ints
            this.removeField(CHARACTERS_CONTENT);
        }

        return this;
    }

    /**
     * Parse an string of integers into an array.
     *
     * @param intArrayString String of integers separated by spaces.
     *
     * @return Array of integers parsed from the input string.
     */
    protected int[] parseInts(String intArrayString)
    {
        String[] arrayOfNumbers = intArrayString.split("\\s");
        int[] ints = new int[arrayOfNumbers.length];

        int i = 0;
        for (String s : arrayOfNumbers)
        {
            if (!WWUtil.isEmpty(s))
                ints[i++] = Integer.parseInt(s);
        }

        return ints;
    }
}
