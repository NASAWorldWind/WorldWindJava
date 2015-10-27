/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;

/**
 * Parses KML <i>coordinates</i> elements.
 *
 * @author tag
 * @version $Id: KMLCoordinatesParser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLCoordinatesParser extends AbstractXMLEventParser
{
    public KMLCoordinatesParser()
    {
    }

    public KMLCoordinatesParser(String namespaceURI)
    {
        super(namespaceURI);
    }

    @SuppressWarnings( {"UnnecessaryContinue"})
    public Position.PositionList parse(XMLEventParserContext ctx, XMLEvent doubleEvent, Object... args)
        throws XMLStreamException
    {
        String s = ctx.getStringParser().parseString(ctx, doubleEvent);
        if (s == null || s.length() < 3) // "a,b" is the smallest possible coordinate string
            return null;

        ArrayList<Position> positions = new ArrayList<Position>();

        KMLCoordinateTokenizer tokenizer = new KMLCoordinateTokenizer(s);

        while (tokenizer.hasMoreTokens())
        {
            try
            {
                positions.add(tokenizer.nextPosition());
            }
            catch (NumberFormatException e)
            {
                continue; // TODO: issue warning?
            }
            catch (NullPointerException e)
            {
                continue; // TODO: issue warning?
            }
            catch (Exception e)
            {
                continue; // TODO: issue warning
            }
        }

        return new Position.PositionList(positions);
    }
}
