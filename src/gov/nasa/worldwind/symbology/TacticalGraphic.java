/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.UnitsFormat;

/**
 * TacticalGraphic provides a common interface for displaying a graphic from a symbology set. A graphic can be an icon
 * that is drawn a geographic position, a vector graphic that is positioned using one or more control points, or a line
 * or polygon that is styled according to the symbol set's specification. See the TacticalGraphic <a title="Tactical
 * Graphic Usage Guide" href="http://goworldwind.org/developers-guide/symbology/tactical-graphics/"
 * target="_blank">Usage Guide</a> for instructions on using TacticalGraphic in an application.
 * <p/>
 * See the {@link gov.nasa.worldwindx.examples.symbology.Symbology} and {@link gov.nasa.worldwindx.examples.symbology.TacticalGraphics}
 * example applications for examples of how to use tactical graphics.
 * <p/>
 * <h1>Construction</h1>
 * <p/>
 * TacticalGraphics are typically created by an instance of {@link TacticalGraphicFactory}. Each graphic within a symbol
 * set is identified by a string identifier. The format of this identifier depends on the symbol set. For example, a
 * MIL-STD-2525 Symbol Identification Code (SIDC) is a string of 15 characters.
 * <p/>
 * You will need to instantiate the appropriate factory for the symbol set that you intend to use.  For example, {@link
 * gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory} creates graphics for the MIL-STD-2525 symbology
 * set.
 * <p/>
 * The TacticalGraphic interface provides access to settings common to all tactical graphics. TacticalGraphic extends
 * the {@link Renderable} interface, so you can add a TacticalGraphic directly to a {@link
 * gov.nasa.worldwind.layers.RenderableLayer}. Here's an example of creating a graphic from the MIL-STD-2525 symbol
 * set:
 * <p/>
 * <pre>
 * // Create a graphic factory for MIL-STD-2525
 * TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
 *
 * // Specify the control points for the line
 * List<Position> positions = Arrays.asList(
 *     Position.fromDegrees(34.7327, -117.8347, 0),
 *     Position.fromDegrees(34.7328, -117.7305, 0));
 *
 * // Specify a text modifier
 * AVList modifiers = new AVListImpl();
 * modifiers.setValue(SymbologyConstants.UNIQUE_DESIGNATION, "Alpha");
 *
 * // Create a graphic for a MIL-STD-2525 hostile phase line. The first argument is the symbol identification code
 * // (SIDC) that identifies the type of graphic to create.
 * TacticalGraphic graphic = factory.createGraphic("GHGPGLP----AUSX", positions, modifiers);
 *
 * // Create a renderable layer to display the tactical graphic. This example adds only a single graphic, but many
 * // graphics can be added to a single layer.
 * RenderableLayer graphicLayer = new RenderableLayer();
 * graphicLayer.addRenderable(graphic);
 *
 * // Add the layer to the world window's model and request that the layer redraw itself. The world window draws the
 * // graphic on the globe at the specified position. Interactions between the graphic and the cursor are returned in
 * // the world window's picked object list, and reported to the world window's select listeners.
 * WorldWindow wwd = ... // A reference to your application's WorldWind instance.
 * wwd.getModel().getLayers().add(graphicLayer);
 * wwd.redraw();
 * </pre>
 * <p/>
 * The symbol identifier ({@code GHGPGLP----AUSX}) tells the factory what type of graphic to create,  and how the
 * graphic should be styled. In the example above we added a text modifier of "Alpha" to identify our shape. These
 * parameters can be specified using a parameter list when the TacticalGraphic is created, as shown above. They can also
 * be set after creation using setters in the TacticalGraphic interface.
 * <p/>
 * <h1>Modifiers</h1>
 * <p/>
 * Many graphics support text or graphic modifiers. Each modifier is identified by a String key. The set of possible
 * modifiers is determined by the symbol set. Modifiers can be specified in the parameter list when a graphic is
 * created, or using {@link #setModifier(String, Object) setModifier} after the graphic has been created.
 * <p/>
 * For example, a MIL-STD-2525 General Area graphic can have a text modifier that identifies the area. Here's an example
 * of how to specify the modifier when the graphic is created:
 * <p/>
 * <pre>
 * AVList modifiers = new AVListImpl();
 * modifiers.setValue(SymbologyConstants.UNIQUE_DESIGNATION, "Boston"); // Text that identifies the area enclosed by
 *                                                                      //  the  graphic.
 *
 * List<Position> positions = ...; // List of positions that define the boundary of the area.
 * TacticalGraphic graphic = milstd2525Factory.createGraphic("GHGPGAG----AUSX", positions, modifiers);
 * </pre>
 * <p/>
 * The modifier can also be set (or changed) after the graphic is created:
 * <p/>
 * <pre>
 * // Create the graphic
 * TacticalGraphic graphic = milstd2525Factory.createGraphic("GHGPGAG----AUSX", positions, null);
 * graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, "Boston");
 * </pre>
 * <p/>
 * <h1>Position</h1>
 * <p/>
 * Each tactical graphic is positioned by one or more control points. How many points are required depends on the type
 * of graphic.  A point graphic will only require one point. A more complex shape may require three or four, and a line
 * or area may allow any number.
 * <p/>
 * Here is an example of how to create a point graphic in the MIL-STD-2525 symbol set:
 * <p/>
 * <pre>
 * Position position = Position.fromDegrees(34.9362, -118.2559, 0);
 * TacticalGraphic graphic = milstd2525Factory.createPoint("GFGPAPD----AUSX", position, null);
 * </pre>
 * <p/>
 * More complicated graphics will require more control points. MIL-STD-2525 defines a template for each type of tactical
 * graphic. Each template identifies how many control points are required for the graphic, and how the points are
 * interpreted. The TacticalGraphic requires a list of Position objects, which identify the control points in the same
 * order as in the specification. For example, in order to create a graphic that requires three control points we need
 * to create a list of positions that specifies the three points in order:
 * <p/>
 * <pre>
 * List<Position> positions = Arrays.asList(
 *     Position.fromDegrees(34.5073, -117.8380, 0), // PT. 1
 *     Position.fromDegrees(34.8686, -117.5088, 0), // PT. 2
 *     Position.fromDegrees(34.4845, -117.8495, 0)); // PT. 3
 *
 * TacticalGraphic graphic = milstd2525Factory.createGraphic("GFGPSLA----AUSX", positions, null);
 * </pre>
 * <p/>
 * <h1>Sub-interfaces of TacticalGraphic</h1>
 * <p/>
 * TacticalGraphic describes any tactical graphic in the most general terms: a list of positions and modifiers. However,
 * this general interface is not convenient for all graphics. For example, when creating a circle graphic it is more
 * convenient to access the radius of the circle directly than to set a modifier that affects the radius. Sub-interfaces
 * of tactical graphic provide more convenient methods for manipulating common types of graphics. Instances of these
 * sub-interfaces can be created directly using a TacticalGraphicFactory. The sub-interfaces are:
 * <p/>
 * <ul> <li>{@link TacticalPoint}- Graphics positioned by a single point.</li> <li>{@link TacticalCircle} - Graphics
 * positioned by a center point and radius.</li> <li>{@link TacticalQuad} - Rectangles with a length and width.</li>
 * <li>{@link TacticalRoute} - A series of point graphics connected by lines and treated as a single graphic.</li>
 * </ul>
 *
 * @author pabercrombie
 * @version $Id: TacticalGraphic.java 1171 2013-02-11 21:45:02Z dcollins $
 * @see TacticalGraphicFactory
 */
public interface TacticalGraphic extends Renderable, Highlightable, Movable, AVList
{
    /**
     * Indicates whether this graphic is drawn when in view.
     *
     * @return true if this graphic is drawn when in view, otherwise false.
     */
    boolean isVisible();

    /**
     * Specifies whether this graphic is drawn when in view.
     *
     * @param visible true if this graphic should be drawn when in view, otherwise false.
     */
    void setVisible(boolean visible);

    /**
     * Indicates the current value of a text or graphic modifier.
     *
     * @param modifier Key that identifies the modifier to retrieve. The possible modifiers depends on the symbol set.
     *
     * @return The value of the modifier, or {@code null} if the modifier is not set.
     */
    Object getModifier(String modifier);

    /**
     * Specifies the value of a text or graphic modifier.
     *
     * @param modifier Key that identifies the modifier to set. The possible modifiers depends on the symbol set.
     * @param value    New value for the modifier.
     */
    void setModifier(String modifier, Object value);

    /**
     * Indicates whether this graphic draws its supplemental graphic modifiers.
     *
     * @return true if this graphic draws its graphic modifiers, otherwise false.
     */
    boolean isShowGraphicModifiers();

    /**
     * Specifies whether to draw this graphic's supplemental graphic modifiers.
     *
     * @param showGraphicModifiers true if this graphic should draw its graphic modifiers, otherwise false.
     */
    void setShowGraphicModifiers(boolean showGraphicModifiers);

    /**
     * Indicates whether this graphic draws its supplemental text modifiers.
     *
     * @return true if this graphic draws its text modifiers, otherwise false.
     */
    boolean isShowTextModifiers();

    /**
     * Specifies whether to draw this graphic's supplemental text modifiers.
     *
     * @param showTextModifiers true if this graphic should draw its text modifiers, otherwise false.
     */
    void setShowTextModifiers(boolean showTextModifiers);

    /**
     * Indicates whether or not the graphic should display its location as a text modifier. Not all graphics support the
     * location modifier.
     *
     * @return true if the graphic will display the location modifier. Note that not all graphics support this
     *         modifier.
     */
    boolean isShowLocation();

    /**
     * Specifies whether or not the graphic should display its location as a text modifier. Not all graphics support the
     * location modifier. Setting showLocation on a graphic that does not support the modifier will have no effect.
     *
     * @param show true if the graphic will display the location modifier. Note that not all graphics support this
     *             modifier.
     */
    void setShowLocation(boolean show);

    /**
     * Indicates whether or not this graphic will display a text indicator when the graphic represents a hostile entity.
     * See comments on {@link #setShowHostileIndicator(boolean) setShowHostileIndicator} for more information.
     *
     * @return true if an indicator may be drawn when this graphic represents a hostile entity, if supported by the
     *         graphic implementation. Note that some graphics may not display an indicator, even when representing a
     *         hostile entity.
     */
    boolean isShowHostileIndicator();

    /**
     * Specifies whether or not to display a text indicator when the symbol or graphic represents a hostile entity. In
     * the case of MIL-STD-2525C, the indicator is the letters "ENY". The indicator is determined by the symbology set,
     * and may not apply to all graphics in the symbol set.
     *
     * @param show true if this graphic should display an indicator when this graphic represents a hostile entity and
     *             the graphic implementation supports such an indicator. Note that some graphics may not display an
     *             indicator, even when representing a hostile entity.
     */
    void setShowHostileIndicator(boolean show);

    /**
     * Indicates a string identifier for this graphic. The format of the identifier depends on the symbol set to which
     * the graphic belongs.
     *
     * @return An identifier for this graphic.
     */
    String getIdentifier();

    /**
     * Convenience method to specify a text modifier for the graphic. Calling this method is equivalent to calling
     * <code>setModifier(SymbologyConstants.UNIQUE_DESIGNATION, text)</code>.
     *
     * @param text New text modifier. May be null.
     *
     * @see #setModifier(String, Object)
     */
    void setText(String text);

    /**
     * Convenience method to access the text modifier of the graphic. Calling this method is equivalent to calling
     * <code>getModifier(SymbologyConstants.UNIQUE_DESIGNATION)</code>.
     *
     * @return Descriptive text for this graphic.
     *
     * @see #getModifier(String)
     */
    String getText();

    /**
     * Indicates the positions of the control points that place and orient the graphic.
     *
     * @return positions that orient the graphic. How many positions are returned depends on the type of graphic. Some
     *         graphics require only a single position, others require many.
     */
    Iterable<? extends Position> getPositions();

    /**
     * Specifies the positions of the control points that place and orient the graphic.
     *
     * @param positions Positions that orient the graphic. How many positions are returned depends on the type of
     *                  graphic. Some graphics require only a single position, others require many. The positions must
     *                  be specified in the same order as the control points defined by the symbology set's template for
     *                  this type of graphic.
     */
    void setPositions(Iterable<? extends Position> positions);

    /**
     * Indicates this graphic's attributes when it is in the normal (as opposed to highlighted) state.
     *
     * @return this graphic's attributes. May be null.
     */
    TacticalGraphicAttributes getAttributes();

    /**
     * Specifies attributes for this graphic in the normal (as opposed to highlighted) state. If any fields in the
     * attribute bundle are null, the default attribute will be used instead. For example, if the attribute bundle
     * includes a setting for outline material but not for interior material the new outline material will override the
     * default outline material, but the interior material will remain the default. The default attributes are
     * determined by the symbol set, and may differ depending on the type of graphic.
     *
     * @param attributes new attributes. May be null, in which case default attributes are used.
     */
    void setAttributes(TacticalGraphicAttributes attributes);

    /**
     * Indicate this graphic's attributes when it is in the highlighted state.
     *
     * @return this graphic's highlight attributes. May be null.
     */
    TacticalGraphicAttributes getHighlightAttributes();

    /**
     * Specifies attributes for this graphic in the highlighted state. See comments on {@link
     * #setAttributes(TacticalGraphicAttributes) setAttributes} for more information on how the attributes are
     * interpreted.
     *
     * @param attributes Attributes to apply to the graphic when it is highlighted. May be null, in which default
     *                   attributes are used.
     */
    void setHighlightAttributes(TacticalGraphicAttributes attributes);

    /**
     * Indicates an offset used to position the graphic's main label relative to the label's geographic position. See
     * comments on {@link #setLabelOffset(gov.nasa.worldwind.render.Offset) setLabelOffset} for more information.
     *
     * @return The offset that determines how the graphic's label is placed relative to the graphic.
     */
    Offset getLabelOffset();

    /**
     * Specifies an offset used to position this graphic's main label relative to the label's geographic position. The
     * geographic position is determined by the type of graphic. For example, the label for an area graphic is typically
     * placed at the center of the area polygon. Note that not all graphics have labels.
     * <p/>
     * The offset can specify an absolute pixel value, or a an offset relative to the size of the label. For example, an
     * offset of (-0.5, -0.5) in fraction units will center the label on its geographic position both horizontally and
     * vertically.
     *
     * @param offset The offset that determines how the graphic's label is placed relative to the graphic.
     */
    void setLabelOffset(Offset offset);

    /**
     * Returns the delegate owner of the graphic. If non-null, the returned object replaces the graphic as the pickable
     * object returned during picking. If null, the graphic itself is the pickable object returned during picking.
     *
     * @return the object used as the pickable object returned during picking, or null to indicate the the graphic is
     *         returned during picking.
     */
    Object getDelegateOwner();

    /**
     * Specifies the delegate owner of the graphic. If non-null, the delegate owner replaces the graphic as the pickable
     * object returned during picking. If null, the graphic itself is the pickable object returned during picking.
     *
     * @param owner the object to use as the pickable object returned during picking, or null to return the graphic.
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
