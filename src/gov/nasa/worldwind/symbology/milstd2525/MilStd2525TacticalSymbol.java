/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Implementation of {@link gov.nasa.worldwind.symbology.TacticalSymbol} that provides support for tactical symbols from
 * the <a href="http://www.assistdocs.com/search/document_details.cfm?ident_number=114934">MIL-STD-2525</a> symbology
 * set. See the <a title="Tactical Symbol Usage Guide" href="http://goworldwind.org/developers-guide/symbology/tactical-symbols/"
 * target="_blank">Tactical Symbol Usage Guide</a> for instructions on using TacticalSymbol in an application.
 *
 * @author dcollins
 * @version $Id: MilStd2525TacticalSymbol.java 2196 2014-08-06 19:42:15Z tgaskins $
 */
public class MilStd2525TacticalSymbol extends AbstractTacticalSymbol
{
    /** Default unit format. */
    public static final UnitsFormat DEFAULT_UNITS_FORMAT = new MilStd2525UnitsFormat();

    protected static final Font DEFAULT_FRAME_SHAPE_FONT = Font.decode("Arial-BOLD-24");

    // Static maps and sets providing fast access to attributes about a symbol ID. These data structures are populated
    // in the static block below.
    protected static final Map<String, String> symbolEchelonMap = new HashMap<String, String>();
    protected static final Set<String> exerciseSymbols = new HashSet<String>();

    static
    {
        // The MIL-STD-2525 symbols representing an echelon.
        symbolEchelonMap.put("e-o-bj---------", SymbologyConstants.ECHELON_TEAM_CREW);

        // The MIL-STD-2525 symbols representing a exercise object.
        exerciseSymbols.add("s-u-wmgx-------");
        exerciseSymbols.add("s-u-wmmx-------");
        exerciseSymbols.add("s-u-wmfx-------");
        exerciseSymbols.add("s-u-wmx--------");
        exerciseSymbols.add("s-u-wmsx-------");
    }

    /**
     * Indicates a string identifier for this symbol. The format of the identifier depends on the symbol set to which
     * this symbol belongs. For symbols belonging to the MIL-STD-2525 symbol set, this returns a 15-character
     * alphanumeric symbol identification code (SIDC). Calculated from the current modifiers at construction and during
     * each call to {@link #setModifier(String, Object)}. Initially <code>null</code>.
     */
    protected SymbolCode symbolCode;
    protected boolean isGroundSymbol;
    protected boolean useGroundHeadingIndicator;

    /**
     * Constructs a tactical symbol for the MIL-STD-2525 symbology set with the specified symbol identifier and
     * position. This constructor does not accept any supplemental modifiers, so the symbol contains only the attributes
     * specified by its symbol identifier. This constructor does not accept any icon retrieval path, so the created
     * symbol retrieves its icons from the default location.
     * <p/>
     * The symbolId specifies the tactical symbol's appearance. The symbolId must be a 15-character alphanumeric symbol
     * identification code (SIDC). The symbol's shape, fill color, outline color, and icon are all defined by the symbol
     * identifier. Use the '-' character to specify null entries in the symbol identifier.
     * <p/>
     * The position specifies the latitude, longitude, and altitude where the symbol is drawn on the globe. The
     * position's altitude component is interpreted according to the altitudeMode.
     *
     * @param symbolId a 15-character alphanumeric symbol identification code (SIDC).
     * @param position the latitude, longitude, and altitude where the symbol is drawn.
     *
     * @throws IllegalArgumentException if either the symbolId or the position are <code>null</code>, or if the symbolId
     *                                  is not a valid 15-character alphanumeric symbol identification code (SIDC).
     */
    public MilStd2525TacticalSymbol(String symbolId, Position position)
    {
        super(position);

        this.init(symbolId, null);
    }

    /**
     * Constructs a tactical symbol for the MIL-STD-2525 symbology set with the specified symbol identifier, position,
     * and list of modifiers. This constructor does not accept any icon retrieval path, so the created symbol retrieves
     * its icons from the default location.
     * <p/>
     * The symbolId specifies the tactical symbol's appearance. The symbolId must be a 15-character alphanumeric symbol
     * identification code (SIDC). The symbol's shape, fill color, outline color, and icon are all defined by the symbol
     * identifier. Use the '-' character to specify null entries in the symbol identifier.
     * <p/>
     * The position specifies the latitude, longitude, and altitude where the symbol is drawn on the globe. The
     * position's altitude component is interpreted according to this symbol's altitudeMode.
     * <p/>
     * The modifiers specify supplemental graphic and text attributes as key-value pairs. See the
     * MilStd2525TacticalSymbol class documentation for the list of recognized modifiers. In the case where both the
     * symbol identifier and the modifiers list specify the same attribute, the modifiers list has priority.
     *
     * @param symbolId  a 15-character alphanumeric symbol identification code (SIDC).
     * @param position  the latitude, longitude, and altitude where the symbol is drawn.
     * @param modifiers an optional list of key-value pairs specifying the symbol's modifiers. May be <code>null</code>
     *                  to specify that the symbol contains only the attributes in its symbol identifier.
     *
     * @throws IllegalArgumentException if either the symbolId or the position are <code>null</code>, or if the symbolId
     *                                  is not a valid 15-character alphanumeric symbol identification code (SIDC).
     */
    public MilStd2525TacticalSymbol(String symbolId, Position position, AVList modifiers)
    {
        super(position);

        this.init(symbolId, modifiers);
    }

    protected void init(String symbolId, AVList modifiers)
    {
        // Initialize the symbol code from the symbol identifier specified at construction.
        this.symbolCode = new SymbolCode(symbolId);
        // Parse the symbol code's 2-character modifier code and store the resulting pairs in the modifiers list.
        SymbolCode.parseSymbolModifierCode(this.symbolCode.getSymbolModifier(), this.modifiers);
        // Apply any caller-specified key-value pairs to the modifiers list. We apply these pairs last to give them
        // precedence.
        if (modifiers != null)
            this.modifiers.setValues(modifiers);

        // Configure this tactical symbol's icon retriever and modifier retriever with either the configuration value or
        // the default value (in that order of precedence).
        String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
            MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
        this.setIconRetriever(new MilStd2525IconRetriever(iconRetrieverPath));
        this.setModifierRetriever(new MilStd2525ModifierRetriever(iconRetrieverPath));

        // By default, do not show the hostile indicator (the letters "ENY"). Note that this default is different from
        // MilStd2525TacticalGraphic, which does display the hostile indicator by default. We choose not to display the
        // indicator by default because it is redundant to both the frame shape and fill color.
        this.setShowHostileIndicator(false);

        // Initialize this tactical symbol's icon offset, icon size, and altitude mode from its symbol code.
        this.initIconLayout();

        this.setUnitsFormat(DEFAULT_UNITS_FORMAT);
    }

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        return this.symbolCode.toString();
    }

    /**
     * Indicates the current value of symbol's Status/Operational Condition field.
     *
     * @return this symbol's Status/Operational Condition field.
     *
     * @see #setStatus(String)
     */
    public String getStatus()
    {
        return this.symbolCode.getStatus();
    }

    /**
     * Specifies this symbol's Status/Operational Condition field. A symbol's Status defines whether the represented
     * object exists at the time the symbol was generated, or is anticipated to exist in the future. Additionally, a
     * symbol's Status can define its operational condition. The recognized values depend on the specific MIL-STD-2525
     * symbology scheme the symbol belongs to:
     * <p/>
     * <strong>Warfighting, Signals Intelligence, Stability Operations</strong>
     * <p/>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_PRESENT</li> <li>STATUS_PRESENT_FULLY_CAPABLE</li>
     * <li>STATUS_PRESENT_DAMAGED</li> <li>STATUS_PRESENT_DESTROYED</li> <li>STATUS_PRESENT_FULL_TO_CAPACITY</li> </ul>
     * <p/>
     * <strong>Tactical Graphics</strong>
     * <p/>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_SUSPECTED</li> <li>STATUS_PRESENT</li> <li>STATUS_KNOWN</li> </ul>
     * <p/>
     * <strong>Emergency Management</strong>
     * <p/>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_PRESENT</li> </ul>
     *
     * @param value the new value for the Status/Operational Condition field.
     *
     * @throws IllegalArgumentException if the specified value is <code>null</code> or is not one of the accepted status
     *                                  values.
     */
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
     * Indicates whether this symbol draws its frame.  See {@link #setShowFrame(boolean)} for a description of how this
     * property is used.
     *
     * @return <code>true</code> if this symbol draws its frame, otherwise <code>false</code>.
     */
    public boolean isShowFrame()
    {
        Object o = this.modifiers.getValue(SymbologyConstants.SHOW_FRAME);
        return o == null || o.equals(Boolean.TRUE); // No value indicates the default of true.
    }

    /**
     * Specifies whether to draw this symbol's frame. The showFrame property provides control over this tactical
     * symbol's display option hierarchy as defined by MIL-STD-2525C, section 5.4.5 and table III.
     * <p/>
     * When <code>true</code>, this symbol's frame is drawn. This state corresponds to MIL-STD-2525C, table III, row 1.
     * <p/>
     * When <code>false</code>, this symbol's frame is not drawn. Instead, only the symbol's internal icon is drawn.
     * This state corresponds to MIL-STD-2525C, table III, row 4.
     *
     * @param showFrame <code>true</code> to draw this symbol's frame, otherwise <code>false</code>.
     */
    public void setShowFrame(boolean showFrame)
    {
        this.modifiers.setValue(SymbologyConstants.SHOW_FRAME, showFrame);
    }

    /**
     * Indicates whether this symbol draws its fill.  See {@link #setShowFill(boolean)} for a description of how this
     * property is used.
     *
     * @return <code>true</code> if this symbol draws its fill, otherwise <code>false</code>.
     */
    public boolean isShowFill()
    {
        Object o = this.modifiers.getValue(SymbologyConstants.SHOW_FILL);
        return o == null || o.equals(Boolean.TRUE); // No value indicates the default of true.
    }

    /**
     * Specifies whether to draw this symbol's fill. The showFill property provides control over this tactical symbol's
     * display option hierarchy as defined by MIL-STD-2525C, section 5.4.5 and table III.
     * <p/>
     * When <code>true</code>, this symbol's fill is drawn. This state corresponds to MIL-STD-2525C, table III, row 1.
     * <p/>
     * When <code>false</code>, this symbol's fill is not drawn. Instead, only the symbol's frame and internal icon are
     * drawn. This state corresponds to MIL-STD-2525C, table III, row 2.
     *
     * @param showFill <code>true</code> to draw this symbol's fill, otherwise <code>false</code>.
     */
    public void setShowFill(boolean showFill)
    {
        this.modifiers.setValue(SymbologyConstants.SHOW_FILL, showFill);
    }

    /**
     * Indicates whether this symbol draws its internal icon.  See {@link #setShowIcon(boolean)} for a description of
     * how this property is used.
     *
     * @return <code>true</code> if this symbol draws its icon, otherwise <code>false</code>.
     */
    public boolean isShowIcon()
    {
        Object o = this.modifiers.getValue(SymbologyConstants.SHOW_ICON);
        return o == null || o.equals(Boolean.TRUE); // No value indicates the default of true.
    }

    /**
     * Specifies whether to draw this symbol's internal icon. The showIcon property provides control over this tactical
     * symbol's display option hierarchy as defined by MIL-STD-2525C, section 5.4.5 and table III.
     * <p/>
     * When <code>true</code>, this symbol's icon is drawn. This state corresponds to MIL-STD-2525C, table III, row 1.
     * <p/>
     * When <code>false</code>, this symbol's icon is not drawn. Instead, only the symbol's frame and fill are drawn.
     * This state corresponds to MIL-STD-2525C, table III, row 5.
     *
     * @param showIcon <code>true</code> to draw this symbol's icon, otherwise <code>false</code>.
     */
    public void setShowIcon(boolean showIcon)
    {
        this.modifiers.setValue(SymbologyConstants.SHOW_ICON, showIcon);
    }

    protected void initIconLayout()
    {
        MilStd2525Util.SymbolInfo info = MilStd2525Util.computeTacticalSymbolInfo(this.getIdentifier());
        if (info == null)
            return;

        this.iconOffset = info.iconOffset;
        this.iconSize = info.iconSize;

        if (info.offset != null)
            this.setOffset(info.offset);

        if (info.isGroundSymbol)
        {
            this.isGroundSymbol = true;
            this.useGroundHeadingIndicator = info.offset == null;
            this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        }
    }

    @Override
    protected AVList assembleIconRetrieverParameters(AVList params)
    {
        if (params == null)
            params = new AVListImpl();

        super.assembleIconRetrieverParameters(params);

        Object o = this.modifiers.getValue(SymbologyConstants.SHOW_FILL);
        if (o != null)
            params.setValue(SymbologyConstants.SHOW_FILL, o);

        o = this.modifiers.getValue(SymbologyConstants.SHOW_FRAME);
        if (o != null)
            params.setValue(SymbologyConstants.SHOW_FRAME, o);

        o = this.modifiers.getValue(SymbologyConstants.SHOW_ICON);
        if (o != null)
            params.setValue(SymbologyConstants.SHOW_ICON, o);

        return params;
    }

    @Override
    protected void applyImplicitModifiers(AVList modifiers)
    {
        String maskedCode = this.symbolCode.toMaskedString().toLowerCase();
        String si = this.symbolCode.getStandardIdentity();

        // Set the Echelon modifier value according to the value implied by this symbol ID, if any. Give precedence to
        // the modifier value specified by the application, including null.
        if (!modifiers.hasKey(SymbologyConstants.ECHELON))
        {
            Object o = symbolEchelonMap.get(maskedCode);
            if (o != null)
                modifiers.setValue(SymbologyConstants.ECHELON, o);
        }

        // Set the Frame Shape modifier value according to the value implied by this symbol ID, if any. Give precedence to
        // the modifier value specified by the application, including null.
        if (!modifiers.hasKey(SymbologyConstants.FRAME_SHAPE))
        {
            if (exerciseSymbols.contains(maskedCode))
            {
                modifiers.setValue(SymbologyConstants.FRAME_SHAPE, SymbologyConstants.FRAME_SHAPE_EXERCISE);
            }
            else if (si != null && (si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_PENDING)
                || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_UNKNOWN)
                || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_FRIEND)
                || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_NEUTRAL)
                || si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_EXERCISE_ASSUMED_FRIEND)))
            {
                modifiers.setValue(SymbologyConstants.FRAME_SHAPE, SymbologyConstants.FRAME_SHAPE_EXERCISE);
            }
            else if (si != null && si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_JOKER))
            {
                modifiers.setValue(SymbologyConstants.FRAME_SHAPE, SymbologyConstants.FRAME_SHAPE_JOKER);
            }
            else if (si != null && si.equalsIgnoreCase(SymbologyConstants.STANDARD_IDENTITY_FAKER))
            {
                modifiers.setValue(SymbologyConstants.FRAME_SHAPE, SymbologyConstants.FRAME_SHAPE_FAKER);
            }
        }

        // If this symbol represents a hostile entity, and the "hostile/enemy" indicator is enabled, then set the
        // hostile modifier to "ENY".
        boolean isHostile = SymbologyConstants.STANDARD_IDENTITY_HOSTILE.equalsIgnoreCase(si)
            || SymbologyConstants.STANDARD_IDENTITY_SUSPECT.equalsIgnoreCase(si)
            || SymbologyConstants.STANDARD_IDENTITY_JOKER.equalsIgnoreCase(si)
            || SymbologyConstants.STANDARD_IDENTITY_FAKER.equalsIgnoreCase(si);
        if (!modifiers.hasKey(SymbologyConstants.HOSTILE_ENEMY) && this.isShowHostileIndicator() && isHostile)
        {
            modifiers.setValue(SymbologyConstants.HOSTILE_ENEMY, SymbologyConstants.HOSTILE_ENEMY);
        }

        // Determine location, if location modifier is enabled.
        if (!modifiers.hasKey(SymbologyConstants.LOCATION) && this.isShowLocation())
        {
            modifiers.setValue(SymbologyConstants.LOCATION, this.getFormattedPosition());
        }

        // Determine altitude, if location modifier is enabled.
        if (!modifiers.hasKey(SymbologyConstants.ALTITUDE_DEPTH) && this.isShowLocation())
        {
            Position position = this.getPosition();
            UnitsFormat format = this.getUnitsFormat();

            // If the symbol is clamped to the ground, return "GL" (Ground Level) for the altitude. Otherwise format
            // the altitude using the active units format, and append the datum. See MIL-STD-2525C section 5.5.2.5.2 (pg. 41).
            String altitude;
            int altitudeMode = this.getAltitudeMode();
            if (altitudeMode == WorldWind.CLAMP_TO_GROUND)
                altitude = "GL";
            else if (altitudeMode == WorldWind.RELATIVE_TO_GROUND)
                altitude = format.eyeAltitude(position.getElevation()) + " AGL";
            else
                altitude = format.eyeAltitude(position.getElevation()) + " AMSL";

            modifiers.setValue(SymbologyConstants.ALTITUDE_DEPTH, altitude);
        }
    }

    protected void layoutGraphicModifiers(DrawContext dc, AVList modifiers, OrderedSymbol osym)
    {
        this.currentGlyphs.clear();
        this.currentLines.clear();

        AVList retrieverParams = new AVListImpl();
        retrieverParams.setValue(AVKey.WIDTH, this.iconRect.width);

        // Feint/Dummy Indicator modifier. Placed above the icon.
        String modifierCode = this.getModifierCode(modifiers, SymbologyConstants.FEINT_DUMMY);
        if (modifierCode != null)
        {
            this.addGlyph(dc, Offset.TOP_CENTER, Offset.BOTTOM_CENTER, modifierCode, retrieverParams, null, osym);
        }

        // Installation modifier. Placed at the top of the symbol layout.
        modifierCode = this.getModifierCode(modifiers, SymbologyConstants.INSTALLATION);
        if (modifierCode != null)
        {
            this.addGlyph(dc, Offset.TOP_CENTER, Offset.BOTTOM_CENTER, modifierCode, null, LAYOUT_RELATIVE, osym);
        }

        // Echelon / Task Force Indicator modifier. Placed at the top of the symbol layout.
        modifierCode = this.getModifierCode(modifiers, SymbologyConstants.TASK_FORCE);
        if (modifierCode != null)
        {
            this.addGlyph(dc, Offset.TOP_CENTER, Offset.BOTTOM_CENTER, modifierCode, null, LAYOUT_RELATIVE, osym);
        }
        // Echelon modifier. Placed at the top of the symbol layout.
        else if ((modifierCode = this.getModifierCode(modifiers, SymbologyConstants.ECHELON)) != null)
        {
            this.addGlyph(dc, Offset.TOP_CENTER, Offset.BOTTOM_CENTER, modifierCode, null, LAYOUT_RELATIVE, osym);
        }

        // Mobility Indicator modifier. Placed at the bottom of the symbol layout.
        modifierCode = this.getModifierCode(modifiers, SymbologyConstants.MOBILITY);
        if (modifierCode != null)
        {
            this.addGlyph(dc, Offset.BOTTOM_CENTER, Offset.TOP_CENTER, modifierCode, null, LAYOUT_RELATIVE, osym);
        }

        // Auxiliary Equipment Indicator modifier. Placed at the bottom of the symbol layout.
        modifierCode = this.getModifierCode(modifiers, SymbologyConstants.AUXILIARY_EQUIPMENT);
        if (modifierCode != null)
        {
            this.addGlyph(dc, Offset.BOTTOM_CENTER, Offset.TOP_CENTER, modifierCode, null, LAYOUT_RELATIVE, osym);
        }

        if (this.mustUseAlternateOperationalCondition(modifiers))
        {
            // Alternate Status/Operational Condition. Always used by the Emergency Management scheme (see MIL-STD-2525C
            // spec section G.5.5.14, pg. 1030). May be used for other schemes if the OPERATIONAL_CONDITION_ALTERNATE
            // modifier is set. Placed at the bottom of the symbol layout.
            modifierCode = this.getModifierCode(modifiers, SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE);
            if (modifierCode != null)
            {
                this.addGlyph(dc, Offset.BOTTOM_CENTER, Offset.TOP_CENTER, modifierCode, retrieverParams,
                    LAYOUT_RELATIVE, osym);
            }
        }
        else
        {
            // Status/Operational Condition. Used by all schemes except the Emergency Management scheme. Centered on
            // the icon.
            modifierCode = this.getModifierCode(modifiers, SymbologyConstants.OPERATIONAL_CONDITION);
            if (modifierCode != null)
            {
                this.addGlyph(dc, Offset.CENTER, Offset.CENTER, modifierCode, null, null, osym);
            }
        }
    }

    /**
     * Indicates whether or not the symbol should be displayed using the alternate Operational Condition indicator
     * described in MIL-STD-2525C spec Table III-2, pg. 19. The alternate display is always used for symbols in the
     * Emergency Management scheme (see MIL-STD-2525C // spec section G.5.5.14, pg. 1030). It is be used for other
     * symbols if the SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE modifier is set.
     *
     * @param modifiers Symbol modifiers.
     *
     * @return True if the symbol must use the alternate operational condition indicator.
     */
    protected boolean mustUseAlternateOperationalCondition(AVList modifiers)
    {
        return SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT.equalsIgnoreCase(this.symbolCode.getScheme())
            || modifiers.hasKey(SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE);
    }

    @Override
    protected void layoutDynamicModifiers(DrawContext dc, AVList modifiers, OrderedSymbol osym)
    {
        this.currentLines.clear();

        if (!this.isShowGraphicModifiers())
            return;

        // Direction of Movement indicator. Placed either at the center of the icon or at the bottom of the symbol
        // layout.
        Object o = this.getModifier(SymbologyConstants.DIRECTION_OF_MOVEMENT);
        if (o != null && o instanceof Angle)
        {
            // The length of the direction of movement line is equal to the height of the symbol frame. See
            // MIL-STD-2525C section 5.3.4.1.c, page 33.
            double length = this.iconRect.getHeight();
            Object d = this.getModifier(SymbologyConstants.SPEED_LEADER_SCALE);
            if (d != null && d instanceof Number)
                length *= ((Number) d).doubleValue();

            if (this.useGroundHeadingIndicator)
            {
                List<? extends Point2D> points = MilStd2525Util.computeGroundHeadingIndicatorPoints(dc, osym.placePoint,
                    (Angle) o, length, this.iconRect.getHeight());
                this.addLine(dc, Offset.BOTTOM_CENTER, points, LAYOUT_RELATIVE, points.size() - 1, osym);
            }
            else
            {
                List<? extends Point2D> points = MilStd2525Util.computeCenterHeadingIndicatorPoints(dc,
                    osym.placePoint, (Angle) o, length);
                this.addLine(dc, Offset.CENTER, points, null, 0, osym);
            }
        }
    }

    protected void layoutTextModifiers(DrawContext dc, AVList modifiers, OrderedSymbol osym)
    {
        this.currentLabels.clear();

        StringBuilder sb = new StringBuilder();

        // We compute a default font rather than using a static default in order to choose a font size that is
        // appropriate for the symbol's frame height. According to the MIL-STD-2525C specification, the text modifier
        // height must be 0.3x the symbol's frame height.
        Font font = this.getActiveAttributes().getTextModifierFont();
        Font frameShapeFont = this.getActiveAttributes().getTextModifierFont();
        if (frameShapeFont == null)
            frameShapeFont = DEFAULT_FRAME_SHAPE_FONT;

        // Quantity modifier layout. Placed at the top of the symbol layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.QUANTITY, 9);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.TOP_CENTER, Offset.BOTTOM_CENTER, sb.toString(), font, null, LAYOUT_RELATIVE,
                osym);
            sb.delete(0, sb.length());
        }

        // Special C2 Headquarters modifier layout. Centered on the icon.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.SPECIAL_C2_HEADQUARTERS, 9);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.CENTER, Offset.CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Frame Shape and Reinforced/Reduced modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.FRAME_SHAPE, null);
        String s = this.getReinforcedReducedModifier(modifiers, SymbologyConstants.REINFORCED_REDUCED);
        if (s != null)
            sb.append(sb.length() > 0 ? " " : "").append(s);
        if (sb.length() > 0)
        {
            Offset offset = Offset.fromFraction(1.0, 1.1);
            this.addLabel(dc, offset, Offset.LEFT_CENTER, sb.toString(), frameShapeFont, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Staff Comments modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.STAFF_COMMENTS, 20);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(1.0, 0.8), Offset.LEFT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Additional Information modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.ADDITIONAL_INFORMATION, 20);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(1.0, 0.5), Offset.LEFT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Higher Formation modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.HIGHER_FORMATION, 21);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(1.0, 0.2), Offset.LEFT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Evaluation Rating, Combat Effectiveness, Signature Equipment, Hostile (Enemy), and IFF/SIF modifier
        // layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.EVALUATION_RATING, 2);
        this.appendTextModifier(sb, modifiers, SymbologyConstants.COMBAT_EFFECTIVENESS, 3);
        this.appendTextModifier(sb, modifiers, SymbologyConstants.SIGNATURE_EQUIPMENT, 1);
        this.appendTextModifier(sb, modifiers, SymbologyConstants.HOSTILE_ENEMY, 3);
        this.appendTextModifier(sb, modifiers, SymbologyConstants.IFF_SIF, 5);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(1.0, -0.1), Offset.LEFT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Date-Time-Group (DTG) modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.DATE_TIME_GROUP, 16);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(0.0, 1.1), Offset.RIGHT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Altitude/Depth and Location modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.ALTITUDE_DEPTH, 14);
        this.appendTextModifier(sb, modifiers, SymbologyConstants.LOCATION, 19);

        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(0.0, 0.8), Offset.RIGHT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Type modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.TYPE, 24);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(0.0, 0.5), Offset.RIGHT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Unique Designation modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.UNIQUE_DESIGNATION, 21);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(0.0, 0.2), Offset.RIGHT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }

        // Speed modifier layout.
        this.appendTextModifier(sb, modifiers, SymbologyConstants.SPEED, 8);
        if (sb.length() > 0)
        {
            this.addLabel(dc, Offset.fromFraction(0.0, -0.1), Offset.RIGHT_CENTER, sb.toString(), font, null, null, osym);
            sb.delete(0, sb.length());
        }
    }

    @Override
    protected int getMaxLabelLines(AVList modifiers)
    {
        // Determine how many lines of text are on the left side of the symbol.
        int leftLines = 0;
        if (modifiers.hasKey(SymbologyConstants.DATE_TIME_GROUP))
            leftLines++;
        if (modifiers.hasKey(SymbologyConstants.ALTITUDE_DEPTH) || modifiers.hasKey(SymbologyConstants.LOCATION))
            leftLines++;
        if (modifiers.hasKey(SymbologyConstants.TYPE))
            leftLines++;
        if (modifiers.hasKey(SymbologyConstants.UNIQUE_DESIGNATION))
            leftLines++;
        if (modifiers.hasKey(SymbologyConstants.SPEED))
            leftLines++;

        // Determine how many lines of text are on the right side of the symbol.
        int rightLines = 0;
        if (modifiers.hasKey(SymbologyConstants.FRAME_SHAPE) || modifiers.hasKey(SymbologyConstants.REINFORCED_REDUCED))
            rightLines++;
        if (modifiers.hasKey(SymbologyConstants.STAFF_COMMENTS))
            rightLines++;
        if (modifiers.hasKey(SymbologyConstants.ADDITIONAL_INFORMATION))
            rightLines++;
        if (modifiers.hasKey(SymbologyConstants.HIGHER_FORMATION))
            rightLines++;
        if (modifiers.hasKey(SymbologyConstants.COMBAT_EFFECTIVENESS)
            || modifiers.hasKey(SymbologyConstants.SIGNATURE_EQUIPMENT)
            || modifiers.hasKey(SymbologyConstants.HOSTILE_ENEMY)
            || modifiers.hasKey(SymbologyConstants.IFF_SIF))
        {
            rightLines++;
        }

        return Math.max(leftLines, rightLines);
    }

    protected String getModifierCode(AVList modifiers, String modifierKey)
    {
        return SymbolCode.composeSymbolModifierCode(this.symbolCode, modifiers, modifierKey);
    }

    protected String getReinforcedReducedModifier(AVList modifiers, String modifierKey)
    {
        Object o = modifiers.getValue(modifierKey);
        if (o != null && o.toString().equalsIgnoreCase(SymbologyConstants.REINFORCED))
            return "+";
        else if (o != null && o.toString().equalsIgnoreCase(SymbologyConstants.REDUCED))
            return "-";
        else if (o != null && o.toString().equalsIgnoreCase(SymbologyConstants.REINFORCED_AND_REDUCED))
            return "+-";
        else
            return null;
    }

    protected void appendTextModifier(StringBuilder sb, AVList modifiers, String modifierKey, Integer maxLength)
    {
        Object modifierValue = modifiers.getValue(modifierKey);
        if (WWUtil.isEmpty(modifierValue))
            return;

        String modifierText = modifierValue.toString();
        int len = maxLength != null && maxLength < modifierText.length() ? maxLength : modifierText.length();

        if (sb.length() > 0)
            sb.append(" ");

        sb.append(modifierText, 0, len);
    }

    @Override
    protected void computeTransform(DrawContext dc, OrderedSymbol osym)
    {
        super.computeTransform(dc, osym);

        // Compute an appropriate default offset if the application has not specified an offset and this symbol has no
        // default offset.
        if (this.getOffset() == null && this.iconRect != null && osym.layoutRect != null && this.isGroundSymbol)
        {
            osym.dx = -this.iconRect.getCenterX();
            osym.dy = -osym.layoutRect.getMinY();
        }
        else if (this.getOffset() == null && this.iconRect != null)
        {
            osym.dx = -this.iconRect.getCenterX();
            osym.dy = -this.iconRect.getCenterY();
        }
    }
}
