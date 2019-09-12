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
 * Represents the COLLADA <i>float_array</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaFloatArray.java 662 2012-06-26 19:05:46Z pabercrombie $
 */
public class ColladaFloatArray extends ColladaAbstractObject
{
    /** Floats parsed from this element. */
    protected float[] floats;

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaFloatArray(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the float array contained in this element.
     *
     * @return Floats contained in this element. May return an empty array, but will not return null.
     */
    public float[] getFloats()
    {
        return (this.floats != null) ? this.floats : new float[0];
    }

    /** {@inheritDoc} Overridden to parse character content into a float[]. */
    @Override
    public Object parse(XMLEventParserContext ctx, XMLEvent event, Object... args) throws XMLStreamException
    {
        super.parse(ctx, event, args);

        if (this.hasField(CHARACTERS_CONTENT))
        {
            String s = (String) this.getField(CHARACTERS_CONTENT);
            if (!WWUtil.isEmpty(s))
                this.floats = this.parseFloats(s);

            // Don't need to keep string version of the floats
            this.removeField(CHARACTERS_CONTENT);
        }

        return this;
    }

    /**
     * Parse a string of floats into an array.
     *
     * @param floatArrayString String of floats separated by whitespace.
     *
     * @return Array of parsed floats.
     */
    protected float[] parseFloats(String floatArrayString)
    {
        String[] arrayOfNumbers = floatArrayString.split("\\s");
        float[] ary = new float[arrayOfNumbers.length];

        int i = 0;
        for (String s : arrayOfNumbers)
        {
            if (!WWUtil.isEmpty(s))
                ary[i++] = Float.parseFloat(s);
        }

        return ary;
    }
}
