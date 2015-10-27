/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

import java.util.logging.Level;

/**
 * @author tag
 * @version $Id: GMLPos.java 2066 2014-06-20 20:41:46Z tgaskins $
 */
public class GMLPos extends AbstractXMLEventParser
{
    public GMLPos(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getDimension()
    {
        return (String) this.getField("dimension");
    }

    public String getPosString()
    {
        return (String) this.getField("CharactersContent");
    }

    public double[] getPos2()
    {
        String[] strings = this.getPosString().split(" ");

        if (strings.length < 2)
            return null;

        try
        {
            return new double[] {Double.parseDouble(strings[0]), Double.parseDouble(strings[1])};
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.NumberFormatException");
            Logging.logger().log(Level.WARNING, message, e);
            return null;
        }
    }
}
