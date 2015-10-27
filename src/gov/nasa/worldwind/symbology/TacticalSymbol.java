/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.UnitsFormat;

/**
 * TacticalSymbol provides a common interface for displaying tactical point symbols from symbology sets. A tactical
 * symbol displays graphic and textual information about an object at a single geographic position at a particular point
 * in time. See the <a title="Tactical Symbol Usage Guide" href="http://goworldwind.org/developers-guide/symbology/tactical-symbols/"
 * target="_blank">Tactical Symbol Usage Guide</a> for instructions on using TacticalSymbol in an application.
 * <p/>
 * <h2>Construction</h2> Implementations of this interface provide support for symbols belonging to a specific symbology
 * set. For example, class {@link gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalSymbol} provides support for
 * tactical symbols from the MIL-STD-2525 symbology specification.
 * <p/>
 * To create a tactical symbol, instantiate a concrete implementation appropriate for the desired symbology set. Pass a
 * string identifier, the desired geographic position, and (optionally) one or more symbol modifier key-value pairs to
 * the symbol's constructor. The tactical symbol creates a graphic appropriate for the string identifier and optional
 * symbol modifiers, and draws that graphic at the specified position when its render method is called. For example, a
 * symbol implementation may display a 3D object at the position, or display a screen space icon who's screen location
 * tracks the position. MIL-STD-2525 tactical symbols display a screen space icon with graphic and text modifiers
 * surrounding the icon.
 * <p/>
 * The format of the string identifier and the modifier key-value pairs are implementation dependent. For MIL-STD-2525,
 * the string identifier must be a 15-character alphanumeric symbol identification code (SIDC), and the modifier keys
 * must be one of the constants defined in MilStd2525TacticalSymbol's documentation.
 * <p/>
 * Since TacticalSymbol extends the Renderable interface, a tactical symbol is displayed either by adding it to a layer,
 * or by calling its render method from within a custom layer or renderable object. The simplest way to display a
 * tactical symbol is to add it to a {@link gov.nasa.worldwind.layers.RenderableLayer}. Here's an example of creating
 * and displaying a tactical symbol for a MIL-STD-2525 friendly ground unit using a RenderableLayer:
 * <p/>
 * <pre>
 * // Create a tactical symbol for a MIL-STD-2525 friendly ground unit. Since the SIDC specifies a ground symbol, the
 * // tactical symbol's altitude mode is automatically configured as WorldWind.CLAMP_TO_GROUND.
 * TacticalSymbol symbol = new MilStd2525TacticalSymbol("SFGPU---------G", Position.fromDegrees(40, -120, 0));
 *
 * // Create a renderable layer to display the tactical symbol. This example adds only a single symbol, but many
 * // symbols can be added to a single layer.
 * RenderableLayer symbolLayer = new RenderableLayer();
 * symbolLayer.addRenderable(symbol);
 *
 * // Add the layer to the world window's model and request that the window redraw itself. The world window draws the
 * // symbol on the globe at the specified position. Interactions between the symbol and the cursor are returned in the
 * // world window's picked object list, and reported to the world window's select listeners.
 * WorldWindow wwd = ... // A reference to your application's WorldWindow instance.
 * wwd.getModel().getLayers().add(symbolLayer);
 * wwd.redraw();
 * </pre>
 * <p/>
 * <h2>Position</h2> A symbol's geographic position defines where the symbol displays its graphic. Either the graphic's
 * geometric center is displayed at the position, or a specific location within the graphic (such as the bottom of a
 * leader line) is displayed at the position. This behavior depends on the symbol implementation, the string identifier,
 * and the symbol modifiers (if any).
 * <p/>
 * A symbol's altitude mode defines how the altitude component if the position is interpreted. Altitude mode may be
 * specified by calling {@link #setAltitudeMode(int)}. Recognized modes are: <ul> <li>WorldWind.CLAMP_TO_GROUND -- the
 * symbol graphic is placed on the terrain at the latitude and longitude of its position.</li>
 * <li>WorldWind.RELATIVE_TO_GROUND -- the symbol graphic is placed above the terrain at the latitude and longitude of
 * its position and the distance specified by its elevation.</li> <li>WorldWind.ABSOLUTE -- the symbol graphic is placed
 * at its specified position.</li> </ul>
 * <p/>
 * Tactical symbol implementations configure the altitude mode from the string identifier specified during construction.
 * For example, specifying the MIL-STD-2525 SIDC "SFGPU---------G" specifies a friendly ground unit symbol, and causes a
 * tactical symbol to configure the altitude mode as WorldWind.CLAMP_TO_GROUND. The automatically configured mode can be
 * overridden by calling setAltitudeMode.
 * <p/>
 * <h2>Modifiers</h2> Symbols modifiers are optional attributes that augment or change a symbol's graphic. Modifiers can
 * be specified at construction by passing a list of key-value pairs, or after construction by calling {@link
 * #setModifier(String, Object)} with the modifier key and value. Which modifier keys are recognized by a tactical
 * symbol and how they affect the symbol's graphic is implementation dependent. Here's an example of setting the the
 * heading (direction of movement) modifier at construction for a MIL-STD-2525 friendly ground unit:
 * <p/>
 * <pre>
 * // Create a tactical symbol for a MIL-STD-2525 friendly ground unit, specifying the optional Direction of Movement
 * // modifier by passing in a list of key-value pairs.
 * AVList modifiers = new AVListImpl();
 * modifiers.setValue(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(45));
 * TacticalSymbol symbol = new MilStd2525TacticalSymbol("SFGPU---------G", Position.fromDegrees(40, -120, 0),
 *     modifiers);
 * </pre>
 * <p/>
 * Here's an example of setting the same modifier after construction:
 * <p/>
 * <pre>
 * // Create a tactical symbol for a MIL-STD-2525 friendly ground unit.
 * TacticalSymbol symbol = new MilStd2525TacticalSymbol("SFGPU---------G", Position.fromDegrees(40, -120, 0));
 *
 * // Set the heading (direction of movement) modifier.
 * symbol.setModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT, Angle.fromDegrees(45));
 * </pre>
 * <p/>
 * Tactical symbol implementations apply modifiers from the string identifier specified during construction. For
 * example, given a MIL-STD-2525 symbol representing units, installation, or equipment, SIDC positions 11-12 specify the
 * echelon and task force modifiers (See MIL-STD-2525C, Appendix A). Here's an example of setting the echelon and task
 * force modifiers at construction for a MIL-STD-2525 friendly ground unit:
 * <p/>
 * <pre>
 * // Create a tactical symbol for a MIL-STD-2525 friendly ground unit. Specify the echelon modifier and task force
 * // modifiers by setting the SIDC characters 11-12 to "EA". This indicates that the ground unit is a Task Force with
 * // a Team/Crew Echelon (see MIL-STD-2525C, Appendix A, Table A-II).
 * TacticalSymbol symbol = new MilStd2525TacticalSymbol("SFGPU-----EA--G", Position.fromDegrees(40, -120, 0));
 * </pre>
 *
 * @author dcollins
 * @version $Id: TacticalSymbol.java 2370 2014-10-06 22:37:50Z tgaskins $
 */
public interface TacticalSymbol extends WWObject, Renderable, Highlightable
{
    /**
     * An interface to enable application selection of tactical symbol level of detail.
     */
    public interface LODSelector
    {
        /**
         * Modifies the symbol's attributes and properties to achieve a desired level of detail during rendering. This
         * method is called during rendering in order to provide the application an opportunity to adjust the symbol's
         * attributes and properties to achieve a level of detail based on the symbol's distance from the view's eye
         * point or other criteria.
         *
         * @param dc          the current draw context.
         * @param symbol      the symbol about to be rendered.
         * @param eyeDistance the distance in meters from the view's eye point to the symbol's geographic position.
         */
        public void selectLOD(DrawContext dc, TacticalSymbol symbol, double eyeDistance);
    }

    /**
     * Indicates this symbol's level of detail selector.
     *
     * @return this symbol's level of detail selector, or null if one has not been specified.
     */
    LODSelector getLODSelector();

    /**
     * Specifies this symbols level of detail selector.
     *
     * @param LODSelector the level of detail selector. May be null, the default, to indicate no level of detail
     *                    selector.
     */
    void setLODSelector(LODSelector LODSelector);

    /**
     * Indicates whether this symbol is drawn when in view.
     *
     * @return true if this symbol is drawn when in view, otherwise false.
     */
    boolean isVisible();

    /**
     * Specifies whether this symbol is drawn when in view.
     *
     * @param visible true if this symbol should be drawn when in view, otherwise false.
     */
    void setVisible(boolean visible);

    /**
     * Indicates a string identifier for this symbol. The format of the identifier depends on the symbol set to which
     * this symbol belongs. For symbols belonging to the MIL-STD-2525 symbol set, this returns a 15-character
     * alphanumeric symbol identification code (SIDC).
     *
     * @return an identifier for this symbol.
     */
    String getIdentifier();

    /**
     * Indicates this symbol's geographic position. See {@link #setPosition(gov.nasa.worldwind.geom.Position)} for a
     * description of how tactical symbols interpret their position.
     *
     * @return this symbol's current geographic position.
     */
    Position getPosition();

    /**
     * Specifies this symbol's geographic position. The specified position must be non-null, and defines where on the
     * globe this symbol displays its graphic. The position's altitude component is interpreted according to the
     * altitudeMode. The type of graphic this symbol displays at the position is implementation dependent.
     *
     * @param position this symbol's new position.
     *
     * @throws IllegalArgumentException if the position is <code>null</code>.
     */
    void setPosition(Position position);

    /**
     * Indicates this symbol's altitude mode. See {@link #setAltitudeMode(int)} for a description of the valid altitude
     * modes.
     *
     * @return this symbol's altitude mode.
     */
    int getAltitudeMode();

    /**
     * Specifies this symbol's altitude mode. Altitude mode defines how the altitude component of this symbol's position
     * is interpreted. Recognized modes are: <ul> <li>WorldWind.CLAMP_TO_GROUND -- this symbol's graphic is placed on
     * the terrain at the latitude and longitude of its position.</li> <li>WorldWind.RELATIVE_TO_GROUND -- this symbol's
     * graphic is placed above the terrain at the latitude and longitude of its position and the distance specified by
     * its elevation.</li> <li>WorldWind.ABSOLUTE -- this symbol's graphic is placed at its specified position.</li>
     * </ul>
     * <p/>
     * This symbol assumes the altitude mode WorldWind.ABSOLUTE if the specified mode is not recognized.
     *
     * @param altitudeMode this symbol's new altitude mode.
     */
    void setAltitudeMode(int altitudeMode);

    /**
     * Indicates whether this symbol draws its supplemental graphic modifiers.
     *
     * @return true if this symbol draws its graphic modifiers, otherwise false.
     */
    boolean isShowGraphicModifiers();

    /**
     * Specifies whether to draw this symbol's supplemental graphic modifiers.
     *
     * @param showGraphicModifiers true if this symbol should draw its graphic modifiers, otherwise false.
     */
    void setShowGraphicModifiers(boolean showGraphicModifiers);

    /**
     * Indicates whether this symbol draws its supplemental text modifiers.
     *
     * @return true if this symbol draws its text modifiers, otherwise false.
     */
    boolean isShowTextModifiers();

    /**
     * Specifies whether to draw this symbol's supplemental text modifiers.
     *
     * @param showTextModifiers true if this symbol should draw its text modifiers, otherwise false.
     */
    void setShowTextModifiers(boolean showTextModifiers);

    /**
     * Indicates whether or not to display this symbol's location as a text modifier. Not all symbols support the
     * location modifier.
     *
     * @return true if the symbol will display the location modifier. Note that not some symbols may not support this
     * modifier.
     */
    boolean isShowLocation();

    /**
     * Specifies whether or not to display this symbol's location as a text modifier. Not all symbols support the
     * location modifier. Setting showLocation on a symbol that does not support the modifier will have no effect.
     *
     * @param show true if the symbol will display the location modifier. Note that not some symbols may not support
     *             this modifier.
     */
    void setShowLocation(boolean show);

    /**
     * Indicates whether or not to display an indicator when the symbol represents a hostile entity. See comments on
     * {@link #setShowHostileIndicator(boolean) setShowHostileIndicator} for more information.
     *
     * @return true if an indicator will be drawn when this symbol represents a hostile entity, if supported by the
     * symbol specification.
     */
    boolean isShowHostileIndicator();

    /**
     * Specifies whether or not to display an indicator when the symbol represents a hostile entity. The indicator is
     * determined by the symbology set, and may not apply to all symbols in the symbol set.  In the case of
     * MIL-STD-2525C, the indicator is the letters "ENY" displayed at the lower right corner of the symbol.
     *
     * @param show true if this symbol will display an indicator when this symbol represents a hostile entity and the
     *             symbol specification supports such an indicator.
     */
    void setShowHostileIndicator(boolean show);

    /**
     * Indicates the current value of a text or graphic modifier. See {@link #setModifier(String, Object)} for a
     * description of how modifiers values are interpreted.
     *
     * @param modifier the modifier key.
     *
     * @return the modifier value. May be <code>null</code>, indicating that this symbol does not display the specified
     * modifier.
     *
     * @throws IllegalArgumentException if the modifier is <code>null</code>.
     */
    Object getModifier(String modifier);

    /**
     * Specifies the value of a text or graphic modifier. Which modifier keys are recognized how they affect the
     * symbol's graphic is implementation dependent. If the modifier has an implicit value and only needs to be enabled
     * (e.g. the MIL-STD-2525 location modifier), specify true as the modifier value. If the specified value is
     * <code>null</code>, the modifier is removed from this symbol.
     * <p/>
     * If the specified modifier represents a graphic or text modifier, its display is suppressed if
     * isShowGraphicModifiers or isShowTextModifiers, respectively, returns false.
     *
     * @param modifier the modifier key.
     * @param value    the modifier value. May be <code>null</code>, indicating that the modifier should be removed from
     *                 this symbol.
     *
     * @throws IllegalArgumentException if the modifier is <code>null</code>.
     */
    void setModifier(String modifier, Object value);

    /**
     * Returns this symbol's normal (as opposed to highlight) attributes.
     *
     * @return this symbol's normal attributes. May be <code>null</code>, indicating that the default highlight
     * attributes are used.
     */
    TacticalSymbolAttributes getAttributes();

    /**
     * Specifies this symbol's normal (as opposed to highlight) attributes.
     *
     * @param normalAttrs the normal attributes. May be <code>null</code>, in which case default attributes are used.
     */
    void setAttributes(TacticalSymbolAttributes normalAttrs);

    /**
     * Returns this symbol's highlight attributes.
     *
     * @return this symbol's highlight attributes. May be <code>null</code>, indicating that the default attributes are
     * used.
     */
    TacticalSymbolAttributes getHighlightAttributes();

    /**
     * Specifies this symbol's highlight attributes.
     *
     * @param highlightAttrs the highlight attributes. May be <code>null</code>, in which case default highlight
     *                       attributes are used.
     */
    void setHighlightAttributes(TacticalSymbolAttributes highlightAttrs);

    /**
     * Indicates the delegate owner of this symbol. If non-null, the returned object replaces the symbol as the pickable
     * object returned during picking. If null, the symbol itself is the pickable object returned during picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the symbol is
     * returned during picking.
     */
    Object getDelegateOwner();

    /**
     * Specifies the delegate owner of this symbol. If non-null, the delegate owner replaces the symbol as the pickable
     * object returned during picking. If null, the symbol itself is the pickable object returned during picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the symbol.
     */
    void setDelegateOwner(Object owner);

    /**
     * Indicates the unit format used to format values in text modifiers.
     *
     * @return Units format used to format text modifiers.
     */
    UnitsFormat getUnitsFormat();

    /**
     * Specifies the unit format used to format values in text modifiers.
     *
     * @param unitsFormat Format used to format text modifiers.
     */
    void setUnitsFormat(UnitsFormat unitsFormat);
}

