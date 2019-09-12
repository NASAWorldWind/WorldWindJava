/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.symbology.AbstractTacticalSymbol;
import gov.nasa.worldwind.symbology.milstd2525.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.image.*;

/**
 * Implementation of TacticalSymbol that renders a symbol inside a pentagon, for the Limited Access Area graphic.
 *
 * @author pabercrombie
 * @version $Id: LimitedAccessSymbol.java 545 2012-04-24 22:29:21Z pabercrombie $
 * @see LimitedAccessArea
 */
public class LimitedAccessSymbol extends AbstractTacticalSymbol
{
    /** Identifier for the symbol. */
    protected String symbolId;

    public LimitedAccessSymbol(String sidc, Position position)
    {
        super(position);
        this.init(sidc);
    }

    protected void init(String symbolId)
    {
        this.symbolId = symbolId;

        // Configure this tactical symbol's icon retriever and modifier retriever with either the configuration value or
        // the default value (in that order of precedence).
        String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
            MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
        this.setIconRetriever(new IconRetriever(iconRetrieverPath));
        this.setModifierRetriever(new MilStd2525ModifierRetriever(iconRetrieverPath));

        this.setOffset(Offset.fromFraction(0.5, 0.0));
    }

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        return this.symbolId;
    }

    /** Icon retriever to retrieve an icon framed in a pentagon. */
    static class IconRetriever extends MilStd2525IconRetriever
    {
        public IconRetriever(String retrieverPath)
        {
            super(retrieverPath);
        }

        @Override
        public BufferedImage createIcon(String symbolId, AVList params)
        {
            if (symbolId == null)
            {
                String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            SymbolCode symbolCode = new SymbolCode(symbolId);
            BufferedImage image = this.drawIcon(symbolCode, params, null);

            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();

            int pentagonWidth = (int) (imgWidth * 1.1);
            int pentagonHeight = (int) (imgHeight * 1.5);

            // Draw a pentagon around the symbol like this:
            // _____________
            // |           |
            // |           |
            // |   Sym     |
            // |           |
            // |           |
            //  \         /
            //   \       /
            //    \     /
            //     \   /
            //      \ /

            BufferedImage pentagonImg = new BufferedImage(pentagonWidth, pentagonHeight, image.getType());

            int lineWidth = (int) Math.max(imgWidth * 0.03, 1);

            Color color = this.getColorFromParams(params);
            if (color == null)
                color = this.getColorForStandardIdentity(symbolCode);

            Graphics2D g = null;
            try
            {
                g = pentagonImg.createGraphics();
                g.setColor(color);
                g.setStroke(new BasicStroke(lineWidth));
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g.drawImage(image, (pentagonWidth - imgWidth - lineWidth * 2) / 2, 0, null);

                g.drawLine(0, 0, 0, imgHeight); // Left edge of rect
                g.drawLine(0, 0, pentagonWidth, 0); // Top edge of rect
                g.drawLine(pentagonWidth - 1, 0, pentagonWidth - 1, imgHeight); // Right edge of rect
                g.drawLine(0, imgHeight, pentagonWidth / 2, pentagonHeight); // Left side of triangle
                g.drawLine(pentagonWidth, imgHeight, pentagonWidth / 2, pentagonHeight); // Right side of triangle
            }
            finally
            {
                if (g != null)
                    g.dispose();
            }

            return pentagonImg;
        }

        /**
         * Retrieves the value of the AVKey.COLOR parameter.
         *
         * @param params Parameter list.
         *
         * @return The value of the AVKey.COLOR parameter, if such a parameter exists and is of type java.awt.Color.
         *         Returns null if the parameter list is null, if there is no value for key AVKey.COLOR, or if the value
         *         is not a Color.
         */
        protected Color getColorFromParams(AVList params)
        {
            if (params == null)
                return null;

            Object o = params.getValue(AVKey.COLOR);
            return (o instanceof Color) ? (Color) o : null;
        }

        /**
         * Indicates the color to apply to a graphic based on the graphic's standard identity.
         *
         * @param code Symbol code that identifies the graphic.
         *
         * @return Color to apply based on the standard identity. (Red for hostile entities, black for friendly, etc.)
         */
        protected Color getColorForStandardIdentity(SymbolCode code)
        {
            return MilStd2525Util.getDefaultGraphicMaterial(code).getDiffuse();
        }
    }
}
