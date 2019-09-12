/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import java.awt.*;

/**
 * Represents a COLLADA <i>texture</i> or <i>color</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaTextureOrColor.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaTextureOrColor extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaTextureOrColor(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the <i>texture</i> field.
     *
     * @return The value of the texture field, or null if the field is not set.
     */
    public ColladaTexture getTexture()
    {
        return (ColladaTexture) this.getField("texture");
    }

    /**
     * Indicates the value of the <i>color</i> field.
     *
     * @return The value of the color field, or null if the field is not set.
     */
    public Color getColor()
    {
        ColladaColor color = (ColladaColor) this.getField("color");
        if (color == null)
            return null;

        String colorString = color.getCharacters();
        float[] values = this.parseFloatArray(colorString);

        float r = values[0];
        float g = values[1];
        float b = values[2];
        float a = (values.length > 3) ? values[3] : 1.0f;

        return new Color(r, g, b, a);
    }

    /**
     * Parse a string of floats into a float[]
     *
     * @param floatArrayString String of floats, separated by whitespace.
     *
     * @return Parsed float[].
     */
    protected float[] parseFloatArray(String floatArrayString)
    {
        String[] arrayOfNumbers = floatArrayString.trim().split("\\s+");
        float[] floats = new float[arrayOfNumbers.length];

        int i = 0;
        for (String s : arrayOfNumbers)
        {
            floats[i++] = Float.parseFloat(s);
        }

        return floats;
    }
}
