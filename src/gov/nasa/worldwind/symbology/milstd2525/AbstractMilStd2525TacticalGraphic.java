/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.Logging;

/**
 * Base class for MIL-STD-2525 tactical graphics.
 *
 * @author pabercrombie
 * @version $Id: AbstractMilStd2525TacticalGraphic.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractMilStd2525TacticalGraphic extends AbstractTacticalGraphic
    implements MilStd2525TacticalGraphic, Renderable
{
    /** Factor applied to the stipple pattern used to draw graphics in present state. */
    protected static final int OUTLINE_STIPPLE_FACTOR_PRESENT = 0;
    /** Factor applied to the stipple pattern used to draw graphics in anticipated state. */
    protected static final int OUTLINE_STIPPLE_FACTOR_ANTICIPATED = 6;
    /** Stipple pattern applied to graphics in the anticipated state. */
    protected static final short OUTLINE_STIPPLE_PATTERN = (short) 0xAAAA;

    /**
     * Indicates a string identifier for this symbol. The format of the identifier depends on the symbol set to which
     * this graphic belongs. For symbols belonging to the MIL-STD-2525 symbol set, this returns a 15-character
     * alphanumeric symbol identification code (SIDC). Calculated from the current modifiers at construction and during
     * each call to {@link #setModifier(String, Object)}. Initially <code>null</code>.
     */
    protected SymbolCode symbolCode;
    /**
     * Symbol identifier with fields that do not influence the type of graphic replaced with hyphens. See {@link
     * SymbolCode#toMaskedString}.
     */
    protected String maskedSymbolCode;

    protected AbstractMilStd2525TacticalGraphic(String symbolCode)
    {
        this.symbolCode = new SymbolCode(symbolCode);
        this.maskedSymbolCode = this.symbolCode.toMaskedString();

        // Use the same default units format as 2525 tactical symbols.
        this.setUnitsFormat(MilStd2525TacticalSymbol.DEFAULT_UNITS_FORMAT);
    }

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        return this.symbolCode.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Object getModifier(String modifier)
    {
        if (SymbologyConstants.UNIQUE_DESIGNATION.equals(modifier) && this.text != null)
        {
            return this.text;
        }
        return super.getModifier(modifier);
    }

    /** {@inheritDoc} */
    @Override
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.UNIQUE_DESIGNATION.equals(modifier) && (value instanceof String))
        {
            this.setText((String) value);
        }
        else
        {
            super.setModifier(modifier, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    public String getStatus()
    {
        return this.symbolCode.getStatus();
    }

    /** {@inheritDoc} */
    public void setStatus(String value)
    {
        if (value == null)
        {
            String msg = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!SymbologyConstants.STATUS_ALL.contains(value.toUpperCase()))
        {
            String msg = Logging.getMessage("Symbology.InvalidStatus", value);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.symbolCode.setStatus(value);
    }

    /**
     * Indicates whether or not the graphic must display the hostile/enemy indicator, if the graphic supports the
     * indicator.
     *
     * @return true if {@link #isShowHostileIndicator()} is true, and the graphic represents a hostile entity.
     */
    protected boolean mustShowHostileIndicator()
    {
        String id = this.symbolCode.getStandardIdentity();
        boolean isHostile = SymbologyConstants.STANDARD_IDENTITY_HOSTILE.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_SUSPECT.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_FAKER.equalsIgnoreCase(id)
            || SymbologyConstants.STANDARD_IDENTITY_JOKER.equalsIgnoreCase(id);

        return this.isShowHostileIndicator() && isHostile;
    }

    /**
     * Apply defaults to the active attributes bundle. The default attributes are determined by the type of graphic.
     * This method is called each frame to reset the active shape attributes to the appropriate default state. Override
     * attributes specified by the application may be applied after the defaults have been set.
     *
     * @param attributes Attributes bundle to receive defaults.
     */
    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        Material material = this.getDefaultMaterial();
        attributes.setOutlineMaterial(material);
        attributes.setInteriorMaterial(material);

        // MIL-STD-2525C section 5.5.1.2 (pg. 37) states that graphics (in general) must be drawn with solid lines
        // when in the Present status, and dashed lines when the status is not Present. Note that the default is
        //  overridden by some graphics, which always draw with dashed lines.
        String status = this.getStatus();
        if (!SymbologyConstants.STATUS_PRESENT.equalsIgnoreCase(status))
        {
            attributes.setOutlineStippleFactor(this.getOutlineStippleFactor());
            attributes.setOutlineStipplePattern(this.getOutlineStipplePattern());
        }
        else
        {
            attributes.setOutlineStippleFactor(OUTLINE_STIPPLE_FACTOR_PRESENT);
        }

        // Most 2525 area graphic do not have a fill.
        attributes.setDrawInterior(false);
    }

    /**
     * Indicates the factor applied to the stipple pattern used to draw dashed lines when the graphic is "anticipated".
     * This value is not used when the graphic is "present".
     *
     * @return Factor applied to the stipple pattern.
     *
     * @see gov.nasa.worldwind.render.ShapeAttributes#getOutlineStippleFactor()
     */
    protected int getOutlineStippleFactor()
    {
        return OUTLINE_STIPPLE_FACTOR_ANTICIPATED;
    }

    /**
     * Indicates the stipple pattern used to draw dashed lines when the graphic is "anticipated".
     *
     * @return Factor applied to the stipple pattern.
     *
     * @see gov.nasa.worldwind.render.ShapeAttributes#getOutlineStipplePattern()
     */
    protected short getOutlineStipplePattern()
    {
        return OUTLINE_STIPPLE_PATTERN;
    }

    /**
     * Indicates the default Material for this graphic.
     *
     * @return The default material, determined by the graphic's standard identity.
     */
    protected Material getDefaultMaterial()
    {
        return MilStd2525Util.getDefaultGraphicMaterial(this.symbolCode);
    }

    protected TacticalSymbol createSymbol(String sidc, Position position, TacticalSymbolAttributes attrs)
    {
        TacticalSymbol symbol = new MilStd2525TacticalSymbol(sidc,
            position != null ? position : Position.ZERO);
        symbol.setDelegateOwner(this);
        symbol.setAttributes(attrs);
        symbol.setShowTextModifiers(false);
        return symbol;
    }
}
