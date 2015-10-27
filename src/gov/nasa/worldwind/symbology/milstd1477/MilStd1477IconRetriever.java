/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd1477;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.symbology.AbstractIconRetriever;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.*;
import java.util.MissingResourceException;

/**
 * @author ccrick
 * @version $Id: MilStd1477IconRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MilStd1477IconRetriever extends AbstractIconRetriever
{
    // TODO: add more error checking

    public MilStd1477IconRetriever(String retrieverPath)
    {
        super(retrieverPath);
    }

    public BufferedImage createIcon(String symbolId, AVList params)
    {
        if (symbolId == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // retrieve desired symbol and convert to bufferedImage

        // SymbolCode symbolCode = new SymbolCode(symbolIdentifier);

        String filename = this.getFilename(symbolId);
        BufferedImage img = this.readImage(filename);

        if (img == null)
        {
            String msg = Logging.getMessage("Symbology.SymbolIconNotFound", symbolId);
            Logging.logger().severe(msg);
            throw new MissingResourceException(msg, BufferedImage.class.getName(), filename);
        }

        return img;
    }

    protected String getFilename(String code)
    {
        return code.toLowerCase() + ".png";
    }
}