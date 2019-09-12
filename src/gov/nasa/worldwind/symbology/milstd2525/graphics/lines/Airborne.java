/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Airborne graphic (hierarchy 2.X.2.5.2.1.2, SIDC: G*GPOLAA--****X).
 *
 * @author pabercrombie
 * @version $Id: Airborne.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public class Airborne extends Aviation
{
    /** Symbol drawn at the center of the range fan. */
    protected TacticalSymbol symbol;
    /** Attributes applied to the symbol. */
    protected TacticalSymbolAttributes symbolAttributes;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_OFF_LNE_AXSADV_ABN);
    }

    /**
     * Create a new Airborne graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Airborne(String sidc)
    {
        super(sidc, 1);
    }

    /** {@inheritDoc} */
    @Override
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier) && value instanceof String)
            this.setSymbol((String) value);
        else
            super.setModifier(modifier, value);
    }

    /** {@inheritDoc} */
    @Override
    public Object getModifier(String modifier)
    {
        if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier))
            return this.symbol != null ? this.symbol.getIdentifier() : null;
        else
            return super.getModifier(modifier);
    }

    /**
     * Indicates a symbol drawn at the center of the range fan.
     *
     * @return The symbol drawn at the center of the range fan. May be null.
     */
    public String getSymbol()
    {
        return this.symbol != null ? this.symbol.getIdentifier() : null;
    }

    /**
     * Specifies a symbol to draw between the first two points of the arrow. Equivalent to setting the {@link
     * SymbologyConstants#SYMBOL_INDICATOR} modifier. The symbol's position will be set to a position between the first
     * two control points of the Airborne arrow.
     *
     * @param sidc The identifier of a symbol in the MIL-STD-2525C symbology set, or null to indicate that no symbol
     *             will be drawn.
     */
    public void setSymbol(String sidc)
    {
        if (sidc != null)
        {
            if (this.symbolAttributes == null)
                this.symbolAttributes = new BasicTacticalSymbolAttributes();

            this.symbol = this.createSymbol(sidc, this.computeSymbolPosition(), this.symbolAttributes);
        }
        else
        {
            // Null value indicates no symbol.
            this.symbol = null;
            this.symbolAttributes = null;
        }
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void setPositions(Iterable<? extends Position> positions)
    {
        super.setPositions(positions);

        // Update the position of the symbol.
        if (this.symbol != null)
        {
            this.symbol.setPosition(this.computeSymbolPosition());
        }
    }

    /** {@inheritDoc} Overridden to render tactical symbol. */
    @Override
    public void doRenderGraphicModifiers(DrawContext dc)
    {
        super.doRenderGraphicModifiers(dc);

        if (this.symbol != null)
            this.symbol.render(dc);
    }

    /**
     * Compute the position of the symbol drawn between the first two control points.
     *
     * @return Position of the symbol, or null if the graphic has no positions.
     */
    protected Position computeSymbolPosition()
    {
        Iterable<? extends Position> positions = this.getPositions();
        if (positions == null)
            return null;

        Iterator<? extends Position> iterator = positions.iterator();
        Position pos1 = iterator.next();
        Position pos2 = iterator.next();

        return new Position(LatLon.interpolateGreatCircle(0.1, pos2, pos1), 0);
    }

    /** {@inheritDoc} Overridden to update symbol attributes. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Apply active attributes to the symbol.
        if (this.symbolAttributes != null)
        {
            ShapeAttributes activeAttributes = this.getActiveShapeAttributes();
            this.symbolAttributes.setOpacity(activeAttributes.getInteriorOpacity());
            this.symbolAttributes.setScale(this.activeOverrides.getScale());
        }
    }
}