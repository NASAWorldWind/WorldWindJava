/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Basic implementation of the {@link TacticalSymbolAttributes} interface.
 *
 * @author dcollins
 * @version $Id: BasicTacticalSymbolAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicTacticalSymbolAttributes implements TacticalSymbolAttributes
{
    public static final double DEFAULT_SCALE = 1d;
    public static final double DEFAULT_OPACITY = 1d;
    public static final Font DEFAULT_TEXT_MODIFIER_FONT = Font.decode("Arial-PLAIN-18");
    public static final Material DEFAULT_TEXT_MODIFIER_MATERIAL = Material.BLACK;

    /**
     * Indicates the symbol scale as a ratio of the symbol's original size, or <code>null</code> to use the symbol's
     * default scale. Initially <code>null</code>.
     */
    protected Double scale;
    /**
     * Indicates the material used to draw the symbol's interior, or <code>null</code> to use the symbol's default
     * interior material. Initially <code>null</code>.
     */
    protected Material interiorMaterial;
    /**
     * Indicates the symbol opacity as a floating point number between 0.0 and 1.0 (inclusive), or <code>null</code> to
     * use the symbol's default scale. Initially <code>null</code>.
     */
    protected Double opacity;
    /**
     * Indicates the font used to draw text modifiers, or <code>null</code> to use the symbol's default scale. Initially
     * <code>null</code>.
     */
    protected Font textModifierFont;
    /**
     * Indicates the material used to draw text modifiers, or <code>null</code> to use the symbol's default scale.
     * Initially <code>null</code>.
     */
    protected Material textModifierMaterial;

    /** Constructs a BasicTacticalSymbolAttributes with all attributes set to <code>null</code>. */
    public BasicTacticalSymbolAttributes()
    {
    }

    /**
     * Constructs a BasicTacticalSymbolAttributes with the specified scale, interior material, opacity, text modifier
     * font, and text modifier material. The scale specifies the symbol scale as a ratio of the symbol's original size,
     * the opacity specifies the symbol opacity as a floating point number between 0.0 and 1.0 (inclusive). The
     * specified scale must be either <code>null</code> or greater than or equal to 0.0, and the opacity must either
     * <code>null</code> or a value between 0.0 and 1.0 (inclusive). The textModifierFont and textModifierMaterial
     * specify the font and material to use when drawing a symbol's text modifiers.
     *
     * @param scale                the symbol's scale. May be <code>null</code>, indicating that the default scale
     *                             should be used.
     * @param interiorMaterial     the interior material. May be <code>null</code>, indicating that the default material
     *                             should be used.
     * @param opacity              the symbol opacity. May be <code>null</code>, indicating that the default opacity
     *                             should be used.
     * @param textModifierFont     the text modifier font. May be <code>null</code>, indicating that the default font
     *                             should be used.
     * @param textModifierMaterial the text modifier material. May be <code>null</code>, indicating that the default
     *                             material should be used.
     *
     * @throws IllegalArgumentException if the scale is less than 0.0, or if the opacity is less than 0.0 or greater
     *                                  than 1.0.
     */
    public BasicTacticalSymbolAttributes(Double scale, Material interiorMaterial, Double opacity, Font textModifierFont,
        Material textModifierMaterial)
    {
        if (scale != null && scale < 0d)
        {
            String msg = Logging.getMessage("generic.ScaleOutOfRange", scale);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (opacity != null && (opacity < 0d || opacity > 1d))
        {
            String msg = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.scale = scale;
        this.interiorMaterial = interiorMaterial;
        this.opacity = opacity;
        this.textModifierFont = textModifierFont;
        this.textModifierMaterial = textModifierMaterial;
    }

    /** {@inheritDoc} */
    public void copy(TacticalSymbolAttributes attributes)
    {
        if (attributes != null)
        {
            this.scale = attributes.getScale();
            this.interiorMaterial = attributes.getInteriorMaterial();
            this.opacity = attributes.getOpacity();
            this.textModifierFont = attributes.getTextModifierFont();
            this.textModifierMaterial = attributes.getTextModifierMaterial();
        }
    }

    /** {@inheritDoc} */
    public Double getScale()
    {
        return this.scale;
    }

    /** {@inheritDoc} */
    public void setScale(Double scale)
    {
        if (scale != null && scale < 0d)
        {
            String msg = Logging.getMessage("generic.ScaleOutOfRange", scale);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.scale = scale;
    }

    public Material getInteriorMaterial()
    {
        return this.interiorMaterial;
    }

    public void setInteriorMaterial(Material material)
    {
        this.interiorMaterial = material;
    }

    /** {@inheritDoc} */
    public Double getOpacity()
    {
        return this.opacity;
    }

    /** {@inheritDoc} */
    public void setOpacity(Double opacity)
    {
        if (opacity != null && (opacity < 0d || opacity > 1d))
        {
            String msg = Logging.getMessage("generic.OpacityOutOfRange", opacity);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.opacity = opacity;
    }

    /** {@inheritDoc} */
    public Font getTextModifierFont()
    {
        return this.textModifierFont;
    }

    /** {@inheritDoc} */
    public void setTextModifierFont(Font font)
    {
        this.textModifierFont = font;
    }

    /** {@inheritDoc} */
    public Material getTextModifierMaterial()
    {
        return this.textModifierMaterial;
    }

    /** {@inheritDoc} */
    public void setTextModifierMaterial(Material material)
    {
        this.textModifierMaterial = material;
    }
}
