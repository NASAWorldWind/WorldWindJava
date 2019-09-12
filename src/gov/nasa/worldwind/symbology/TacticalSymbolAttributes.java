/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.render.Material;

import java.awt.*;

/**
 * Holds attributes for a {@link TacticalSymbol}. Changes made to the attributes are applied to the symbol when the
 * WorldWindow renders the next frame. Instances of TacticalSymbolAttributes may be shared by many symbols, thereby
 * reducing the memory normally required to store attributes for each symbol.
 *
 * @author dcollins
 * @version $Id: TacticalSymbolAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface TacticalSymbolAttributes
{
    /**
     * Copies the specified TacticalSymbolAttributes' properties into this object's properties. This does nothing if the
     * specified attributes is <code>null</code>.
     *
     * @param attributes the attributes to copy.
     */
    void copy(TacticalSymbolAttributes attributes);

    /**
     * Indicates the symbol scale as a ratio of the symbol's original size. See {@link #setScale(Double)} for a
     * description of how scale is used.
     *
     * @return the symbol's scale. May be <code>null</code>, indicating that the default scale is used.
     */
    Double getScale();

    /**
     * Specifies the symbol scale as a ratio of the symbol's original size. The specified scale is a floating point
     * number greater than 0.0: values less than 1.0 make the symbol smaller, while values greater than 1.0 make the
     * symbol larger. The scale applies to both the symbol graphic and the symbol modifiers. The specified scale must be
     * either <code>null</code> or greater than or equal to 0.0.
     *
     * @param scale the symbol's scale. May be <code>null</code>, indicating that the default scale should be used.
     *
     * @throws IllegalArgumentException if the scale is less than 0.0.
     */
    void setScale(Double scale);

    /**
     * Indicates the material properties of the symbols's interior. See {@link #setInteriorMaterial(gov.nasa.worldwind.render.Material)
     * setInteriorMaterial} for more information on how this material is interpreted.
     *
     * @return the material applied to the symbols's interior.
     *
     * @see #setInteriorMaterial(Material)
     */
    Material getInteriorMaterial();

    /**
     * Specifies the material properties of the symbols's interior. If lighting is applied to the graphic, this
     * indicates the interior's ambient, diffuse, and specular colors, its shininess, and the color of any emitted
     * light. Otherwise, the material's diffuse color indicates the symbols's constant interior color.
     *
     * @param material the material to apply to the symbol's interior.
     *
     * @see #getInteriorMaterial()
     */
    void setInteriorMaterial(Material material);

    /**
     * Indicates the symbol opacity as a floating point number between 0.0 and 1.0 (inclusive). See {@link
     * #setOpacity(Double)} for a description of how opacity is used.
     *
     * @return the symbol's opacity. May be <code>null</code>, indicating that the default opacity is used.
     */
    Double getOpacity();

    /**
     * Specifies the symbol opacity as a floating point number between 0.0 and 1.0 (inclusive). An opacity of 0.0 is
     * completely transparent and an opacity of 1.0 is completely opaque. The opacity applies to both the symbol graphic
     * and the symbol modifiers. The specified opacity must either <code>null</code> or a value between 0.0 and 1.0
     * (inclusive).
     *
     * @param opacity the symbol opacity. May be <code>null</code>, indicating that the default opacity should be used.
     *
     * @throws IllegalArgumentException if the opacity is less than 0.0 or greater than 1.0.
     */
    void setOpacity(Double opacity);

    /**
     * Indicates the font used to draw text modifiers. See {@link #setTextModifierFont(java.awt.Font)} for a description
     * of how the text modifier font is used.
     *
     * @return the text modifier font. May be <code>null</code>, indicating that the default font is used.
     */
    Font getTextModifierFont();

    /**
     * Specifies the font to use when drawing text modifiers. If the specified font is <code>null</code>, the symbol
     * implementation determines an default font appropriate for the symbol's size and scale. MIL-STD-2525 tactical
     * symbols determine a default font that who's height is approximately 0.3*H, where H is the symbol's scaled frame
     * height.
     *
     * @param font the text modifier font. May be <code>null</code>, indicating that the default font should be used.
     */
    void setTextModifierFont(Font font);

    /**
     * Indicates the material used to draw text modifiers. See {@link #setTextModifierMaterial(gov.nasa.worldwind.render.Material)}
     * for a description of how the text modifier material is used.
     *
     * @return the text modifier material. May be <code>null</code>, indicating that the default material is used.
     */
    Material getTextModifierMaterial();

    /**
     * Specifies the material to use when drawing text modifiers. How the material is used depends on the symbol
     * implementation. For example, symbols may draw 3D text that uses all of the specified material components, or draw
     * 2D text that uses only the diffuse component. MIL-STD-2525 tactical symbols use the diffuse component to specify
     * the color of 2D text.
     *
     * @param material the text modifier material. May be <code>null</code>, indicating that the default material should
     *                 be used.
     */
    void setTextModifierMaterial(Material material);
}
