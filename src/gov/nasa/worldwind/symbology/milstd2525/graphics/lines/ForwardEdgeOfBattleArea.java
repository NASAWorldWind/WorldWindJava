/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementation of the Forward Edge of Battle Area (FEBA) graphic (2.X.2.4.2.1).
 *
 * @author pabercrombie
 * @version $Id: ForwardEdgeOfBattleArea.java 2196 2014-08-06 19:42:15Z tgaskins $
 */
public class ForwardEdgeOfBattleArea extends AbstractMilStd2525TacticalGraphic
{
    protected final static Offset LEFT_CENTER = Offset.fromFraction(-0.1, 0.5);
    protected final static Offset RIGHT_CENTER = Offset.fromFraction(1.1, 0.5);

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_DEF_LNE_FEBA);
    }

    /** Implementation of TacticalSymbol to draw a symbol at the end of a FEBA line. */
    protected static class FEBASymbol extends AbstractTacticalSymbol
    {
        protected String symbolCode;

        /** Indicates if the text ("FEBA") is right aligned or left aligned. */
        protected boolean leftAlign;

        /**
         * Constructs a new symbol with the specified position. The position specifies the latitude, longitude, and
         * altitude where this symbol is drawn on the globe. The position's altitude component is interpreted according
         * to the altitudeMode.
         *
         * @param symbolCode MIL-STD-2525C SIDC that identifies the graphic.
         */
        protected FEBASymbol(String symbolCode)
        {
            super();
            this.symbolCode = symbolCode;

            this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

            // Configure this tactical point graphic's icon retriever and modifier retriever with either the
            // configuration value or the default value (in that order of precedence).
            String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
                MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
            this.setIconRetriever(new MilStd2525PointGraphicRetriever(iconRetrieverPath));
        }

        /** {@inheritDoc} */
        public String getIdentifier()
        {
            return this.symbolCode;
        }

        /**
         * Specifies whether the text ("FEBA") will be drawn on the left or right side of the symbol.
         *
         * @param align AVKey.LEFT or AVKey.RIGHT. An alignment of AVKey.LEFT indicates that the left edge of the text
         *              aligns with the graphic (which puts the text on the right side of the circle). If the alignment
         *              is any value other than AVKey.LEFT or AVKey.RIGHT the alignment is assumed to be AVKey.RIGHT.
         */
        public void setTextAlign(String align)
        {
            // We only handle left and right alignment. If the alignment string is anything other than left we treat it
            // as right align.
            this.leftAlign = AVKey.LEFT.equals(align);
        }

        /** {@inheritDoc} */
        @Override
        protected void layoutTextModifiers(DrawContext dc, AVList modifiers, OrderedSymbol osym)
        {
            this.currentLabels.clear();

            Font font = this.getActiveAttributes().getTextModifierFont();
            Offset imgOffset = this.leftAlign ? RIGHT_CENTER : LEFT_CENTER;
            Offset txtOffset = this.leftAlign ? LEFT_CENTER : RIGHT_CENTER;

            this.addLabel(dc, imgOffset, txtOffset, this.getText(), font, null, null, osym);
        }

        /**
         * Specifies this graphic's Status/Operational Condition field. A graphic's Status defines whether the
         * represented object exists at the time the symbol was generated, or is anticipated to exist in the future.
         * Additionally, a graphic's Status can define its operational condition. The recognized values are <ul>
         * <li>STATUS_ANTICIPATED</li> <li>STATUS_SUSPECTED</li> <li>STATUS_PRESENT</li> <li>STATUS_KNOWN</li> </ul>.
         *
         * @param status the new value for the Status/Operational Condition field.
         *
         * @throws IllegalArgumentException if the specified value is <code>null</code> or is not one of the accepted
         *                                  status values.
         */
        public void setStatus(String status)
        {
            if (status == null)
            {
                String msg = Logging.getMessage("nullValue.StringIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (!SymbologyConstants.STATUS_ALL.contains(status.toUpperCase()))
            {
                String msg = Logging.getMessage("Symbology.InvalidStatus", status);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            SymbolCode code = new SymbolCode(this.symbolCode);
            code.setStatus(status);
            this.symbolCode = code.toString();
        }

        /**
         * Indicates the text in the label.
         *
         * @return The string "FEBA".
         */
        protected String getText()
        {
            return "FEBA";
        }

        @Override
        protected int getMaxLabelLines(AVList modifiers)
        {
            return 1; // Only one line of text.
        }
    }

    /** Symbol drawn at first control point. */
    protected FEBASymbol symbol1;
    /** Symbol drawn at second control point. */
    protected FEBASymbol symbol2;

    /** Attribute bundle shared by the two symbols. */
    protected TacticalSymbolAttributes activeSymbolAttributes = new BasicTacticalSymbolAttributes();

    /**
     * Create a new FEBA line.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public ForwardEdgeOfBattleArea(String sidc)
    {
        super(sidc);
        this.init(sidc);
    }

    /**
     * Create the symbols used to render the graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    protected void init(String sidc)
    {
        this.symbol1 = new FEBASymbol(sidc);
        this.symbol2 = new FEBASymbol(sidc);

        this.symbol1.setAttributes(this.activeSymbolAttributes);
        this.symbol2.setAttributes(this.activeSymbolAttributes);
    }

    /** {@inheritDoc} */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            Iterator<? extends Position> iterator = positions.iterator();
            this.symbol1.setPosition(iterator.next());
            this.symbol2.setPosition(iterator.next());
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        Position position1 = this.symbol1.getPosition();
        Position position2 = this.symbol2.getPosition();

        // This graphic requires exactly two positions. If we don't have two positions
        // for some reason return an empty list.
        if (position1 != null && position2 != null)
            return Arrays.asList(position1, position2);
        else
            return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public void setStatus(String value)
    {
        super.setStatus(value);
        this.symbol1.setStatus(value);
        this.symbol2.setStatus(value);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.symbol1.getPosition();
    }

    /** {@inheritDoc} */
    @Override
    public void setShowTextModifiers(boolean show)
    {
        super.setShowTextModifiers(show);
        this.symbol1.setShowTextModifiers(show);
        this.symbol2.setShowTextModifiers(show);
    }

    @Override
    protected void computeGeometry(DrawContext dc)
    {
        super.computeGeometry(dc);

        Position position1 = this.symbol1.getPosition();
        Position position2 = this.symbol2.getPosition();

        if (position1 == null || position2 == null)
            return;

        // Project the first control point onto the screen
        Vec4 placePoint1 = dc.computeTerrainPoint(position1.getLatitude(), position1.getLongitude(), 0);
        Vec4 screenPoint1 = dc.getView().project(placePoint1);

        // Project the second control point onto the screen
        Vec4 placePoint2 = dc.computeTerrainPoint(position2.getLatitude(), position2.getLongitude(), 0);
        Vec4 screenPoint2 = dc.getView().project(placePoint2);

        // The orientation is reversed if the first point falls to the right of the second point.
        boolean orientationNormal = (screenPoint1.x < screenPoint2.x);

        // Set text alignment on the end points so that the text will always point away from the line.
        if (orientationNormal)
        {
            this.symbol1.setTextAlign(AVKey.RIGHT);
            this.symbol2.setTextAlign(AVKey.LEFT);
        }
        else
        {
            this.symbol1.setTextAlign(AVKey.LEFT);
            this.symbol2.setTextAlign(AVKey.RIGHT);
        }
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.symbol1.render(dc);
        this.symbol2.render(dc);
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.symbol1.setDelegateOwner(owner);
        this.symbol2.setDelegateOwner(owner);
    }

    /** Determine active attributes for this frame. */
    protected void determineActiveAttributes()
    {
        if (this.isHighlighted())
        {
            TacticalGraphicAttributes highlightAttributes = this.getHighlightAttributes();

            // If the application specified overrides to the highlight attributes, then apply the overrides
            if (highlightAttributes != null)
            {
                // Apply overrides specified by application
                this.applyAttributesToSymbol(highlightAttributes, this.activeSymbolAttributes);
            }
        }
        else
        {
            // Apply overrides specified by application
            TacticalGraphicAttributes normalAttributes = this.getAttributes();
            if (normalAttributes != null)
            {
                this.applyAttributesToSymbol(normalAttributes, this.activeSymbolAttributes);
            }
        }
    }

    /**
     * Apply graphic attributes to the symbol.
     *
     * @param graphicAttributes Tactical graphic attributes to apply to the tactical symbol.
     * @param symbolAttributes  Symbol attributes to be modified.
     */
    protected void applyAttributesToSymbol(TacticalGraphicAttributes graphicAttributes,
        TacticalSymbolAttributes symbolAttributes)
    {
        // Line and area graphics distinguish between interior and outline opacity. Tactical symbols only support one
        // opacity, so use the interior opacity.
        Double value = graphicAttributes.getInteriorOpacity();
        if (value != null)
        {
            symbolAttributes.setOpacity(value);
        }

        Font font = graphicAttributes.getTextModifierFont();
        if (font != null)
        {
            symbolAttributes.setTextModifierFont(font);
        }

        Material material = graphicAttributes.getTextModifierMaterial();
        if (material != null)
        {
            symbolAttributes.setTextModifierMaterial(material);
        }
    }
}