/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.util.Logging;

/**
 * SymbolCode provides a utility for parsing and representing the individual fields of a MIL-STD-2525 symbol
 * identification code (SIDC). A SymbolCode can either be created by parsing a 15-character symbol code string or by
 * creating an empty SymbolCode and manually specifying its fields.
 * <p>
 * To parse a symbol code string, construct a new SymbolCode passing in the identifier string as the sole argument.
 * SymbolCode validates and parses the string, and populates its fields according to the contents of the string. If any
 * field in the code is unrecognized SymbolCode throws an exception and indicates the problematic fields in the
 * exception's message. After parsing, each field can be accessed by calling the appropriate accessor methods (for
 * example: getScheme/setScheme). SymbolCodes supports the following fields:
 * <ul> <li>Coding Scheme</li> <li>Standard Identity</li> <li>Battle Dimension</li> <li>Category</li> <li>Function
 * ID</li> <li>Symbol Modifier</li> <li>Echelon</li> <li>Status</li> <li>Country Code</li> <li>Order of Battle</li>
 * </ul>
 * <p>
 * Which fields are populated after parsing a symbol code depends on the MIL-STD-2525 symbology set the symbol code
 * belongs to:
 * <table border="1"> <caption style="font-weight: bold;">Populated Fields</caption><tr><th>Symbology Set</th><th>Coding Scheme</th><th>Standard Identity</th><th>Battle
 * Dimension</th><th>Category</th><th>Status</th><th>Function ID</th><th>Symbol Modifier</th><th>Echelon</th><th>Country
 * Code</th><th>Order of Battle</th></tr> <tr><td>Warfighting</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td></tr>
 * <tr><td>Tactical Graphics</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td><td>YES</td></tr>
 * <tr><td>Signals Intelligence</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td><td>NO</td><td>NO</td><td>YES</td><td>YES</td></tr>
 * <tr><td>Stability Operations</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td></tr>
 * <tr><td>Emergency Management</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td><td>YES</td><td>YES</td><td>NO</td><td>YES</td><td>YES</td></tr>
 * </table>
 *
 * @author pabercrombie
 * @version $Id: SymbolCode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SymbolCode extends AVListImpl
{
    /** Indicates the character for an unused position in a MIL-STD-2525 symbol identification code */
    protected static final String UNUSED_POSITION_CODE = "-";

    /** Creates a new symbol code, but otherwise does nothing. All fields are initialized to <code>null</code>. */
    public SymbolCode()
    {
        // Intentionally left blank. All symbol code fields are null by default.
    }

    /**
     * Creates a new SymbolCode by parsing the fields of the specified MIL-STD-2525 15-character alphanumeric symbol
     * identification code (SIDC). This populates the new SymbolCode's fields according to the contents of the string.
     * This throws an exception if any field in the symbol code is unrecognized, and indicates the problematic fields in
     * the exception's message. After construction, each field can be accessed by calling the appropriate accessor
     * methods (for example: getScheme/setScheme)
     * <p>
     * See SymbolCode's class-level documentation for an overview of the supported MIL-STD-2525 symbol code fields.
     *
     * @param symCode the symbol identification code to parse.
     *
     * @throws IllegalArgumentException if the symCode is <code>null</code> or has a length other than 15.
     * @throws WWUnrecognizedException  if any field in the symCode is invalid or cannot be recognized.
     */
    public SymbolCode(String symCode)
    {
        if (symCode == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (symCode.length() != 15)
        {
            String msg = Logging.getMessage("Symbology.SymbolCodeLengthInvalid", symCode);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String s = this.parseSymCode(symCode);
        if (s != null)
        {
            // A non-null return value indicates the symCode is unrecognized, and contains a message indicating the
            // problematic fields.
            Logging.logger().severe(s);
            throw new WWUnrecognizedException(s);
        }
    }

    /**
     * Indicates this symbol code's Coding Scheme field.
     *
     * @return the value of the Coding Scheme field. May be <code>null</code>.
     *
     * @see #setScheme(String)
     */
    public String getScheme()
    {
        return this.getStringValue(SymbologyConstants.SCHEME);
    }

    /**
     * Specifies this symbol code's Coding Scheme field.  A symbol code's Coding Scheme defines the specific
     * MIL-STD-2525 symbology set that it belongs to. The value must be <code>null</code> or one of the following:
     * <ul> <li>SCHEME_WARFIGHTING</li> <li>SCHEME_TACTICAL_GRAPHICS</li> <li>SCHEME_METOC</li>
     * <li>SCHEME_INTELLIGENCE</li> <li>SCHEME_STABILITY_OPERATIONS</li> <li>SCHEME_EMERGENCY_MANAGEMENT</li> </ul>
     *
     * @param value the new value for the Coding Scheme field. May be <code>null</code>.
     */
    public void setScheme(String value)
    {
        this.setValue(SymbologyConstants.SCHEME, value);
    }

    /**
     * Indicates this symbol code's Standard Identity field.
     *
     * @return the value of the Standard Identity field. May be <code>null</code>.
     *
     * @see #setStandardIdentity(String)
     */
    public String getStandardIdentity()
    {
        return this.getStringValue(SymbologyConstants.STANDARD_IDENTITY);
    }

    /**
     * Specifies this symbol code's Standard Identity field. A symbol code's Standard Identity defines the threat posed
     * by the object being represented. The value must be <code>null</code> or one of the following:
     * <ul> <li>STANDARD_IDENTITY_PENDING</li> <li>STANDARD_IDENTITY_UNKNOWN</li> <li>STANDARD_IDENTITY_ASSUMED_FRIEND</li>
     * <li>STANDARD_IDENTITY_FRIEND</li> <li>STANDARD_IDENTITY_NEUTRAL</li> <li>STANDARD_IDENTITY_SUSPECT</li>
     * <li>STANDARD_IDENTITY_HOSTILE</li> <li>STANDARD_IDENTITY_EXERCISE_PENDING</li>
     * <li>STANDARD_IDENTITY_EXERCISE_UNKNOWN</li> <li>STANDARD_IDENTITY_EXERCISE_ASSUMED_FRIEND</li>
     * <li>STANDARD_IDENTITY_EXERCISE_FRIEND</li> <li>STANDARD_IDENTITY_EXERCISE_NEUTRAL</li>
     * <li>STANDARD_IDENTITY_JOKER</li> <li>STANDARD_IDENTITY_FAKER</li> </ul>
     *
     * @param value the new value for the Standard Identity field. May be <code>null</code>.
     */
    public void setStandardIdentity(String value)
    {
        this.setValue(SymbologyConstants.STANDARD_IDENTITY, value);
    }

    /**
     * Indicates this symbol code's Battle Dimension field.
     *
     * @return the value of the Battle Dimension field. May be <code>null</code>.
     *
     * @see #setBattleDimension(String)
     */
    public String getBattleDimension()
    {
        return this.getStringValue(SymbologyConstants.BATTLE_DIMENSION);
    }

    /**
     * Specifies this symbol code's Battle Dimension field. A symbol code's Battle Dimension defines the primary mission
     * area for the object being represented. The value must be <code>null</code> or one of the following:
     * <ul> <li>BATTLE_DIMENSION_SPACE</li> <li>BATTLE_DIMENSION_AIR</li> <li>BATTLE_DIMENSION_GROUND</li>
     * <li>BATTLE_DIMENSION_SEA_SURFACE</li> <li>BATTLE_DIMENSION_SEA_SUBSURFACE</li> <li>BATTLE_DIMENSION_SOF</li>
     * <li>BATTLE_DIMENSION_OTHER</li> </ul>
     *
     * @param value the new value for the Battle Dimension field. May be <code>null</code>.
     */
    public void setBattleDimension(String value)
    {
        this.setValue(SymbologyConstants.BATTLE_DIMENSION, value);
    }

    /**
     * Indicates this symbol code's Category field.
     *
     * @return the value of the Category field. May be <code>null</code>.
     *
     * @see #setCategory(String)
     */
    public String getCategory()
    {
        return this.getStringValue(SymbologyConstants.CATEGORY);
    }

    /**
     * Specifies this symbol code's Category field. The meaning of a symbol code's Category and the recognized values
     * depend on the specific MIL-STD-2525 symbology scheme the symbol code belongs to:
     * <p>
     * <strong>Tactical Graphics</strong>
     * <ul> <li>CATEGORY_TASKS</li> <li>CATEGORY_COMMAND_CONTROL_GENERAL_MANEUVER</li>
     * <li>CATEGORY_MOBILITY_SURVIVABILITY</li> <li>CATEGORY_FIRE_SUPPORT</li> <li>CATEGORY_COMBAT_SERVICE_SUPPORT</li>
     * <li>CATEGORY_OTHER</li> </ul>
     * <p>
     * <strong>Stability Operations</strong>
     * <ul> <li>CATEGORY_VIOLENT_ACTIVITIES</li> <li>CATEGORY_LOCATIONS</li> <li>CATEGORY_OPERATIONS</li>
     * <li>CATEGORY_ITEMS</li> <li>CATEGORY_INDIVIDUAL</li> <li>CATEGORY_NONMILITARY_GROUP_ORGANIZATION</li>
     * <li>CATEGORY_RAPE</li> </ul>
     * <p>
     * <strong>Emergency Management</strong>
     * <ul> <li>CATEGORY_INCIDENT</li> <li>CATEGORY_NATURAL_EVENTS</li> <li>CATEGORY_OPERATIONS</li>
     * <li>CATEGORY_INFRASTRUCTURE</li> </ul>
     *
     * @param value the new value for the Category field. May be <code>null</code>.
     */
    public void setCategory(String value)
    {
        this.setValue(SymbologyConstants.CATEGORY, value);
    }

    /**
     * Indicates this symbol code's Status/Operational Condition field.
     *
     * @return the value of the Status/Operational Condition field. May be <code>null</code>.
     *
     * @see #setStatus(String)
     */
    public String getStatus()
    {
        return this.getStringValue(SymbologyConstants.STATUS);
    }

    /**
     * Specifies this symbol code's Status/Operational Condition field. A symbol code's Status defines whether the
     * represented object exists at the time the symbol was generated, or is anticipated to exist in the future.
     * Additionally, a symbol code's Status can define its operational condition. The recognized values depend on the
     * specific MIL-STD-2525 symbology scheme the symbol code belongs to:
     * <p>
     * <strong>Warfighting, Signals Intelligence, Stability Operations</strong>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_PRESENT</li> <li>STATUS_PRESENT_FULLY_CAPABLE</li>
     * <li>STATUS_PRESENT_DAMAGED</li> <li>STATUS_PRESENT_DESTROYED</li> <li>STATUS_PRESENT_FULL_TO_CAPACITY</li> </ul>
     * <p>
     * <strong>Tactical Graphics</strong>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_SUSPECTED</li> <li>STATUS_PRESENT</li> <li>STATUS_KNOWN</li> </ul>
     * <p>
     * <strong>Emergency Management</strong>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_PRESENT</li> </ul>
     *
     * @param value the new value for the Status/Operational Condition field. May be <code>null</code>.
     */
    public void setStatus(String value)
    {
        this.setValue(SymbologyConstants.STATUS, value);
    }

    /**
     * Indicates this symbol code's Function ID field.
     *
     * @return the value of the Function ID field. May be <code>null</code>.
     *
     * @see #setFunctionId(String)
     */
    public String getFunctionId()
    {
        return this.getStringValue(SymbologyConstants.FUNCTION_ID);
    }

    /**
     * Specifies this symbol code's Function ID field. The Function IDs are unique to each symbology schemes that uses
     * them, and are defined in each appendix of the MIL-STD-2525C specification:
     * <ul> <li>Warfighting - section A.5.2.1.e (page 51) and table A-I (page 51)</li> <li>Tactical Graphics - section
     * B.5.2.1.e (page 304) and table B-I (page 305)</li> <li>Meteorological and Oceanographic - section C.5.2.1.d (page
     * 763) and table C-I (page 763)</li> <li>Signals Intelligence - section D.5.2.1.e (page 964) and table D-I (page
     * 964)</li> <li>Stability Operations - section E.5.2.1.e (page 991) and table E-I (page 991)</li> <li>Emergency
     * Management - table G-I (page 1032)</li> </ul>
     *
     * @param value the new value for the Function ID field. May be <code>null</code>.
     */
    public void setFunctionId(String value)
    {
        this.setValue(SymbologyConstants.FUNCTION_ID, value);
    }

    /**
     * Indicates this symbol code's Symbol Modifier field.
     *
     * @return the value of the Symbol Modifier field. May be <code>null</code>.
     *
     * @see #setSymbolModifier(String)
     */
    public String getSymbolModifier()
    {
        return this.getStringValue(SymbologyConstants.SYMBOL_MODIFIER);
    }

    /**
     * Specifies this symbol code's Symbol Modifier field. The Symbol Modifier defines what graphic symbol modifiers
     * should be displayed around the symbol's icon such as echelon, headquarters, task force, feint/dummy,
     * installation, equipment mobility, and auxiliary equipment. The recognized values depend on the specific
     * MIL-STD-2525 symbology scheme the symbol code belongs to, and are defined in each appendix of the MIL-STD-2525C
     * specification:
     * <ul> <li>Warfighting - section A.5.2.1.f (page 51) and table A-II (pages 52-54)</li> <li>Stability Operations -
     * section E.5.2.1.f (page 991) and table E-II (pages 992-994)</li> <li>Emergency Management - section G.5.5 (page
     * 1029) and table EG-II (page 1032)</li> </ul>
     *
     * @param value the new value for the Symbol Modifier field. May be <code>null</code>.
     */
    public void setSymbolModifier(String value)
    {
        this.setValue(SymbologyConstants.SYMBOL_MODIFIER, value);
    }

    /**
     * Indicates this symbol code's Echelon field.
     *
     * @return the value of the Echelon field. May be <code>null</code>.
     *
     * @see #setEchelon(String)
     */
    public String getEchelon()
    {
        return this.getStringValue(SymbologyConstants.ECHELON);
    }

    /**
     * Specifies this symbol code's Echelon field. A symbol code's Echelon defines the command level of a unit
     * represented by the symbol. The value must be <code>null</code> or one of the following:
     * <ul> <li>ECHELON_TEAM_CREW</li> <li>ECHELON_SQUAD</li> <li>ECHELON_SECTION</li>
     * <li>ECHELON_PLATOON_DETACHMENT</li> <li>ECHELON_COMPANY_BATTERY_TROOP</li> <li>ECHELON_BATTALION_SQUADRON</li>
     * <li>ECHELON_REGIMENT_GROUP</li> <li>ECHELON_BRIGADE</li> <li>ECHELON_DIVISION</li> <li>ECHELON_CORPS</li>
     * <li>ECHELON_ARMY</li> <li>ECHELON_ARMY_GROUP_FRONT</li> <li>ECHELON_REGION</li> <li>ECHELON_COMMAND</li> </ul>
     * <p>
     *
     * @param value the new value for the Echelon field. May be <code>null</code>.
     */
    public void setEchelon(String value)
    {
        this.setValue(SymbologyConstants.ECHELON, value);
    }

    /**
     * Indicates this symbol code's Country Code field.
     *
     * @return the value of the Country Code field. May be <code>null</code>.
     *
     * @see #setCountryCode(String)
     */
    public String getCountryCode()
    {
        return this.getStringValue(SymbologyConstants.COUNTRY_CODE);
    }

    /**
     * Specifies this symbol code's Country Code field. See <a href="http://www.iso.org/iso/country_codes.htm"
     * target="_blank">ISO 3166-1</a> for a definition of valid Country Codes. The Country Codes are the same for all
     * symbology schemes that use them.
     *
     * @param value the new value for the Country Code field. May be <code>null</code>.
     */
    public void setCountryCode(String value)
    {
        this.setValue(SymbologyConstants.COUNTRY_CODE, value);
    }

    /**
     * Indicates this symbol code's Order of Battle field.
     *
     * @return the value of the Order of Battle field. May be <code>null</code>.
     *
     * @see #setOrderOfBattle(String)
     */
    public String getOrderOfBattle()
    {
        return this.getStringValue(SymbologyConstants.ORDER_OF_BATTLE);
    }

    /**
     * Specifies this symbol code's Order of Battle field. A symbol code's Order of Battle provides additional
     * information about the symbol in the operational environment. The recognized values depend on the specific
     * MIL-STD-2525 symbology scheme the symbol code belongs to:
     * <p>
     * <strong>Warfighting, Signals Intelligence, Stability Operations, Emergency Management</strong>
     * <ul> <li>ORDER_OF_BATTLE_AIR</li> <li>ORDER_OF_BATTLE_ELECTRONIC</li> <li>ORDER_OF_BATTLE_CIVILIAN</li>
     * <li>ORDER_OF_BATTLE_GROUND</li> <li>ORDER_OF_BATTLE_MARITIME</li> <li>ORDER_OF_BATTLE_STRATEGIC_FORCE_RELATED</li>
     * </ul>
     * <p>
     * <strong>Tactical Graphics</strong>
     * <ul> <li>ORDER_OF_BATTLE_CONTROL_MARKINGS</li> </ul>
     *
     * @param value the new value for the Order of Battle field. May be <code>null</code>.
     */
    public void setOrderOfBattle(String value)
    {
        this.setValue(SymbologyConstants.ORDER_OF_BATTLE, value);
    }

    /**
     * Indicates this symbol code's Static/Dynamic field.
     *
     * @return the value of the Static/Dynamic Condition field. May be <code>null</code>.
     *
     * @see #setStaticDynamic(String)
     */
    public String getStaticDynamic()
    {
        return this.getStringValue(SymbologyConstants.STATIC_DYNAMIC);
    }

    /**
     * Specifies this symbol code's Static/Dynamic field. This field is used by graphics in the Meteorological and
     * Oceanographic scheme. Valid values are STATIC and DYNAMIC.
     *
     * @param value the new value for the Static/Dynamic field. May be <code>null</code>.
     */
    public void setStaticDynamic(String value)
    {
        this.setValue(SymbologyConstants.STATIC_DYNAMIC, value);
    }

    /**
     * Indicates this symbol code's Graphic Type field.
     *
     * @return the value of the Graphic Type field. May be <code>null</code>.
     *
     * @see #setStaticDynamic(String)
     */
    public String getGraphicType()
    {
        return this.getStringValue(SymbologyConstants.GRAPHIC_TYPE);
    }

    /**
     * Specifies this symbol code's Graphic Type field. This field is used by graphics in the Meteorological and
     * Oceanographic scheme. Valid values are GRAPHIC_TYPE_POINT, GRAPHIC_TYPE_LINE, GRAPHIC_TYPE_AREA.
     *
     * @param value the new value for the Graphic Type field. May be <code>null</code>.
     */
    public void setGraphicType(String value)
    {
        this.setValue(SymbologyConstants.GRAPHIC_TYPE, value);
    }

    /**
     * Returns the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode's
     * current field values. Fields that are not part of this SymbolCode's current Coding Scheme are ignored. Fields
     * that are unspecified or null are replaced with the MIL-STD-2525 unused position character "-". Field values are
     * either padded or trimmed to fit their portion of the symbol code, adding unused characters to pad or ignoring
     * extra characters to trim.
     * <p>
     * This returns <code>null</code> if this SymbolCode's Coding Scheme is <code>null</code> or unrecognized.
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode, or
     *         <code>null</code> if the Coding Scheme is unrecognized.
     */
    public String toString()
    {
        return this.composeSymCode();
    }

    /**
     * Indicates a string representation of the symbol code with positions that do not uniquely identify a particular
     * symbol or graphic replaced with hyphens. This method masks out the Standard Identity, Status, Echelon, Symbol
     * Modifier, Country Code, and Order Of Battle fields. For example, the masked version of "GFGPGPAD---AUSX" is
     * "G-F-GPAD-------".
     *
     * @return String representation of the symbol code with some fields replaced with hyphens.
     */
    public String toMaskedString()
    {
        SymbolCode masked = new SymbolCode();
        masked.setValues(this);

        masked.setStandardIdentity(null);
        masked.setStatus(null);
        masked.setEchelon(null);
        masked.setSymbolModifier(null);
        masked.setCountryCode(null);
        masked.setOrderOfBattle(null);

        return masked.toString();
    }

    /**
     * Computes and returns the modifier key-value pairs associated with the specified SymbolModifier code. This
     * recognizes modifier codes used by the Warfighting, Stability Operations, and Emergency Management symbology
     * schemes: echelon, headquarters, task force, feint/dummy, installation, equipment mobility, and auxiliary
     * equipment. This adds modifier keys only for those modifiers present in the SymbolModifier field. Any modifiers
     * not in the SymbolModifier field are ignored. The following key-value pairs are used to indicate each modifier:
     * <table border="1"> <caption style="font-weight: bold;">Key Value Pairs</caption> 
     * <tr><th>Modifier</th><th>Key</th><th>Value</th></tr> <tr><td>Echelon</td><td>SymbologyConstants.ECHELON</td><td>See
     * {@link SymbologyConstants#ECHELON}</td></tr> <tr><td>Headquarters</td><td>SymbologyConstants.HEADQUARTERS</td><td>Boolean.TRUE
     * or <code>null</code></td></tr> <tr><td>Task Force</td><td>SymbologyConstants.TASK_FORCE</td><td>Boolean.TRUE or
     * <code>null</code></td></tr> <tr><td>Feint/Dummy</td><td>SymbologyConstants.FEINT_DUMMY</td><td>Boolean.TRUE or
     * <code>null</code></td></tr> <tr><td>Installation</td><td>SymbologyConstants.INSTALLATION</td><td>See {@link
     * SymbologyConstants#INSTALLATION}</td></tr> <tr><td>Equipment Mobility</td><td>SymbologyConstants.MOBILITY</td><td>See
     * {@link SymbologyConstants#MOBILITY}</td></tr> <tr><td>Auxiliary Equipment</td><td>SymbologyConstants.AUXILIARY_EQUIPMENT</td><td>See
     * {@link SymbologyConstants#AUXILIARY_EQUIPMENT}</td></tr> </table>
     * <p>
     * Note that the installation modifier code indicates that an installation is either a normal installation or a
     * feint/dummy installation. In the latter case, this also sets the modifier key SymbologyConstants.FEINT_DUMMY to
     * Boolean.TRUE. This provides a consistent way to identify feint/dummy modifier status for both units/equipment and
     * installations.
     *
     * @param code   the symbol modifier code to parse.
     * @param params a parameter list in which to place the modifier key-value pairs, or <code>null</code> to allocate
     *               and return a new parameter list.
     *
     * @return a parameter list containing the modifier key-value pairs.
     */
    public static AVList parseSymbolModifierCode(String code, AVList params)
    {
        if (code == null || code.length() != 2 || code.equals("--"))
            return params;

        if (params == null)
            params = new AVListImpl();

        String firstChar = code.substring(0, 1);
        String secondChar = code.substring(1, 2);
        String uppercaseCode = code.toUpperCase();
        String uppercaseFirstChar = firstChar.toUpperCase();
        String uppercaseSecondChar = secondChar.toUpperCase();

        if (SymbologyConstants.MODIFIER_CODE_ALL_UEI.contains(uppercaseFirstChar)
            || UNUSED_POSITION_CODE.equals(uppercaseFirstChar))
        {
            // The symbol modifier code indicates units and equipment modifiers. The first character is either unused or
            // indicates the symbol's headquarters, task force, and feint/dummy status. MIL-STD-2525 supports any
            // combination of the headquarters, task force, and feint/dummy states, so we check for each independently.
            // The second character is either unused or indicates the symbol's echelon.

            if (SymbologyConstants.ECHELON_ALL.contains(uppercaseSecondChar))
                params.setValue(SymbologyConstants.ECHELON, secondChar);

            if (SymbologyConstants.MODIFIER_CODE_ALL_HEADQUARTERS.contains(uppercaseFirstChar))
                params.setValue(SymbologyConstants.HEADQUARTERS, Boolean.TRUE);

            if (SymbologyConstants.MODIFIER_CODE_ALL_TASK_FORCE.contains(uppercaseFirstChar))
                params.setValue(SymbologyConstants.TASK_FORCE, Boolean.TRUE);

            if (SymbologyConstants.MODIFIER_CODE_ALL_FEINT_DUMMY.contains(uppercaseFirstChar))
                params.setValue(SymbologyConstants.FEINT_DUMMY, Boolean.TRUE);
        }
        else if (SymbologyConstants.INSTALLATION_ALL.contains(uppercaseCode))
        {
            // The symbol modifier code indicates an installation modifier. Currently, this must either be a normal
            // installation or a feint/dummy installation. Though the installation modifier code indicates that an
            // installation is a feint/dummy, we check for this case and set the FEINT_DUMMY modifier key to TRUE. This
            // provides a consistent modifier key for feint/dummy status across for units/equipment and installations.

            params.setValue(SymbologyConstants.INSTALLATION, code);

            if (SymbologyConstants.INSTALLATION_FEINT_DUMMY.equalsIgnoreCase(code))
                params.setValue(SymbologyConstants.FEINT_DUMMY, Boolean.TRUE);
        }
        else if (SymbologyConstants.MOBILITY_ALL.contains(uppercaseCode))
        {
            // The symbol modifier code indicates an equipment mobility modifier.
            params.setValue(SymbologyConstants.MOBILITY, code);
        }
        else if (SymbologyConstants.AUXILIARY_EQUIPMENT_ALL.contains(uppercaseCode))
        {
            // The symbol modifier code indicates an auxiliary equipment modifier. Currently, this is limited to the
            // towed sonar array modifier.
            params.setValue(SymbologyConstants.AUXILIARY_EQUIPMENT, code);
        }
        else if (SymbologyConstants.OPERATIONAL_CONDITION_ALL.contains(uppercaseCode))
        {
            params.setValue(SymbologyConstants.OPERATIONAL_CONDITION, code);
        }
        else if (SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE_ALL.contains(uppercaseCode))
        {
            params.setValue(SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE, code);
        }

        return params;
    }

    public static String composeSymbolModifierCode(SymbolCode symbolCode, AVList modifiers, String modifierKey)
    {
        if (symbolCode == null)
            return null;

        if (modifiers == null || modifierKey == null)
            return null;

        Object modifierValue = modifiers.getValue(modifierKey);
        String uppercaseValue = modifierValue != null ? modifierValue.toString().toUpperCase() : null;

        if (SymbologyConstants.ECHELON.equalsIgnoreCase(modifierKey)
            && SymbologyConstants.ECHELON_ALL.contains(uppercaseValue))
        {
            return UNUSED_POSITION_CODE + uppercaseValue;
        }
        else if (SymbologyConstants.TASK_FORCE.equalsIgnoreCase(modifierKey) && Boolean.TRUE.equals(modifierValue))
        {
            Object echelonValue = modifiers.getValue(SymbologyConstants.ECHELON);
            if (echelonValue != null && SymbologyConstants.ECHELON_ALL.contains(echelonValue.toString().toUpperCase()))
                return SymbologyConstants.MODIFIER_CODE_TASK_FORCE + echelonValue.toString().toUpperCase();
            else
                return SymbologyConstants.MODIFIER_CODE_TASK_FORCE + UNUSED_POSITION_CODE;
        }
        else if (SymbologyConstants.FEINT_DUMMY.equalsIgnoreCase(modifierKey) && Boolean.TRUE.equals(modifierValue))
        {
            return SymbologyConstants.MODIFIER_CODE_FEINT_DUMMY + UNUSED_POSITION_CODE;
        }
        else if (SymbologyConstants.INSTALLATION.equalsIgnoreCase(modifierKey)
            && SymbologyConstants.INSTALLATION_ALL.contains(uppercaseValue))
        {
            return SymbologyConstants.INSTALLATION_NORMAL;
        }
        else if (SymbologyConstants.MOBILITY.equalsIgnoreCase(modifierKey)
            && SymbologyConstants.MOBILITY_ALL.contains(uppercaseValue))
        {
            return uppercaseValue;
        }
        else if (SymbologyConstants.AUXILIARY_EQUIPMENT.equalsIgnoreCase(modifierKey)
            && SymbologyConstants.AUXILIARY_EQUIPMENT_ALL.contains(uppercaseValue))
        {
            return uppercaseValue;
        }
        else if (SymbologyConstants.OPERATIONAL_CONDITION.equalsIgnoreCase(modifierKey))
        {
            Object status = symbolCode.getStatus();
            String uppercaseStatus = (status != null ? status.toString().toUpperCase() : null);

            if (SymbologyConstants.STATUS_DAMAGED.equalsIgnoreCase(uppercaseStatus))
                return SymbologyConstants.OPERATIONAL_CONDITION_DAMAGED;
            else if (SymbologyConstants.STATUS_DESTROYED.equalsIgnoreCase(uppercaseStatus))
                return SymbologyConstants.OPERATIONAL_CONDITION_DESTROYED;
        }
        else if (SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE.equalsIgnoreCase(modifierKey))
        {
            Object status = symbolCode.getStatus();
            String uppercaseStatus = (status != null ? status.toString().toUpperCase() : null);

            if (SymbologyConstants.STATUS_FULLY_CAPABLE.equalsIgnoreCase(uppercaseStatus))
                return SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE_FULLY_CAPABLE;
            else if (SymbologyConstants.STATUS_DAMAGED.equalsIgnoreCase(uppercaseStatus))
                return SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE_DAMAGED;
            else if (SymbologyConstants.STATUS_DESTROYED.equalsIgnoreCase(uppercaseStatus))
                return SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE_DESTROYED;
            else if (SymbologyConstants.STATUS_FULL_TO_CAPACITY.equalsIgnoreCase(uppercaseStatus))
                return SymbologyConstants.OPERATIONAL_CONDITION_ALTERNATE_FULL_TO_CAPACITY;
        }

        return null;
    }

    /**
     * Parses a symbol code encoded into its individual fields, populating this SymbolCode's fields with the value of
     * each field. Fields that are either not part of the specified symbol code or are unspecified are left unchanged.
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return <code>null</code> if the symbol code is recognized, otherwise a non-<code>null</code> string listing the
     *         unrecognized symbol code fields.
     */
    protected String parseSymCode(String symCode)
    {
        // Coding Scheme (position 1).
        String scheme = symCode.substring(0, 1);

        if (SymbologyConstants.SCHEME_WARFIGHTING.equalsIgnoreCase(scheme))
        {
            return this.parseWarfightingSymCode(symCode);
        }
        else if (SymbologyConstants.SCHEME_TACTICAL_GRAPHICS.equalsIgnoreCase(scheme))
        {
            return this.parseTacticalGraphicsSymCode(symCode);
        }
        else if (SymbologyConstants.SCHEME_METOC.equalsIgnoreCase(scheme))
        {
            return this.parseMetocSymCode(symCode);
        }
        else if (SymbologyConstants.SCHEME_INTELLIGENCE.equalsIgnoreCase(scheme))
        {
            return this.parseIntelligenceSymCode(symCode);
        }
        else if (SymbologyConstants.SCHEME_STABILITY_OPERATIONS.equalsIgnoreCase(scheme))
        {
            return this.parseStabilityOperationsSymCode(symCode);
        }
        else if (SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT.equalsIgnoreCase(scheme))
        {
            return this.parseEmergencyManagementSymCode(symCode);
        }
        else
        {
            return this.parseUnrecognizedSymCode(symCode);
        }
    }

    /**
     * Returns a error string indicating that the symbol code's scheme is not recognized.
     *
     * @param symCode the unknown symbol code.
     *
     * @return an error string.
     */
    protected String parseUnrecognizedSymCode(String symCode)
    {
        // Return a message indicating that the symCode's scheme is not recognized.
        String scheme = symCode.substring(0, 1);
        return Logging.getMessage("Symbology.SymbolCodeSchemeUnrecognized", scheme, symCode);
    }

    /**
     * Parses a symbol code encoded in the Warfighting coding scheme. Warfighting symbol codes contain the following
     * fields: Coding Scheme, Standard Identity, Battle Dimension, Status, Function ID, Symbol Modifier, Country Code,
     * Order of Battle. All fields except Function ID, Symbol Modifier, Country Code and Order of Battle must be
     * non-<code>null</code>.
     * <p>
     * The Warfighting coding scheme is defined in MIL-STD-2525C table A-I (page 51).
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return <code>null</code> if the symbol code is recognized, otherwise a non-<code>null</code> string listing the
     *         unrecognized symbol code fields.
     */
    protected String parseWarfightingSymCode(String symCode)
    {
        StringBuilder sb = new StringBuilder();

        // Coding Scheme (position 1).
        String s = symCode.substring(0, 1);
        if (s != null && s.equalsIgnoreCase(SymbologyConstants.SCHEME_WARFIGHTING))
            this.setScheme(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.scheme"));

        // Standard Identity/Exercise Amplifying Descriptor (position 2).
        s = symCode.substring(1, 2);
        if (SymbologyConstants.STANDARD_IDENTITY_ALL.contains(s.toUpperCase()))
            this.setStandardIdentity(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.standardIdentity"));

        // Battle Dimension (position 3).
        s = symCode.substring(2, 3);
        if (SymbologyConstants.BATTLE_DIMENSION_ALL.contains(s.toUpperCase()))
            this.setBattleDimension(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.battleDimension"));

        // Status/Operational Condition (position 4).
        s = symCode.substring(3, 4);
        if (SymbologyConstants.STATUS_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setStatus(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.status"));

        // Function ID (positions 5-10).
        s = symCode.substring(4, 10);
        if (!"------".equals(s)) // "------" is accepted and indicates a null function ID.
            this.setFunctionId(s);

        // Symbol Modifier (positions 11-12).
        s = symCode.substring(10, 12);
        if (this.isUnitsAndEquipmentSymbolModifier(s)
            || SymbologyConstants.INSTALLATION_ALL.contains(s.toUpperCase())
            || SymbologyConstants.MOBILITY_ALL.contains(s.toUpperCase())
            || SymbologyConstants.AUXILIARY_EQUIPMENT_ALL.contains(s.toUpperCase()))
        {
            this.setSymbolModifier(s);
        }
        else if (!"--".equals(s)) // "--" is accepted and indicates a null symbol modifier.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.symbolModifier"));

        // Country Code (positions 13-14).
        s = symCode.substring(12, 14);
        if (!"--".equals(s)) // "--" is accepted and indicates a null country code.
            this.setCountryCode(s);

        // Order Of Battle (position 15).
        s = symCode.substring(14, 15);
        if (SymbologyConstants.ORDER_OF_BATTLE_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setOrderOfBattle(s);
        else if (!"-".equals(s)) // "-" is accepted and indicates a null order of battle.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.orderOfBattle"));

        return sb.length() > 0
            ? Logging.getMessage("Symbology.SymbolCodeFieldsUnrecognized", sb.toString(), symCode) : null;
    }

    /**
     * Parses a symbol code encoded in the Tactical Graphics coding scheme. Tactical Graphic symbol codes contain the
     * following fields: Coding Scheme, Standard Identity, Category, Status, Function ID, Echelon, Country Code, Order
     * of Battle. All fields except Function ID, Echelon, Country Code and Order of Battle must be
     * non-<code>null</code>.
     * <p>
     * The Tactical Graphics coding scheme is defined in MIL-STD-2525C table B-I (page 305).
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return <code>null</code> if the symbol code is recognized, otherwise a non-<code>null</code> string listing the
     *         unrecognized symbol elements.
     */
    protected String parseTacticalGraphicsSymCode(String symCode)
    {
        StringBuilder sb = new StringBuilder();

        // Coding Scheme (position 1).
        String s = symCode.substring(0, 1);
        if (s != null && s.equalsIgnoreCase(SymbologyConstants.SCHEME_TACTICAL_GRAPHICS))
            this.setScheme(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.scheme"));

        // Standard Identity/Exercise Amplifying Descriptor (position 2).
        s = symCode.substring(1, 2);
        if (SymbologyConstants.STANDARD_IDENTITY_ALL.contains(s.toUpperCase()))
            this.setStandardIdentity(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.standardIdentity"));

        // Category (position 3).
        s = symCode.substring(2, 3);
        if (SymbologyConstants.CATEGORY_ALL_TACTICAL_GRAPHICS.contains(s.toUpperCase()))
            this.setCategory(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.category"));

        // Status/Operational Condition (position 4).
        s = symCode.substring(3, 4);
        if (SymbologyConstants.STATUS_ALL_TACTICAL_GRAPHICS_METOC.contains(s.toUpperCase()))
            this.setStatus(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.status"));

        // Function ID (positions 5-10).
        s = symCode.substring(4, 10);
        if (!"------".equals(s)) // "------" is accepted and indicates a null function ID.
            this.setFunctionId(s);

        // Echelon (position 12, position 11 is unused).
        s = symCode.substring(11, 12);
        if (SymbologyConstants.ECHELON_ALL.contains(s.toUpperCase()))
            this.setEchelon(s);
        else if (!UNUSED_POSITION_CODE.equals(s)) // "-" is accepted and indicates a null echelon.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.echelon"));

        // Country Code (positions 13-14).
        s = symCode.substring(12, 14);
        if (!"--".equals(s)) // "--" is accepted and indicates a null country code. We don't validate country codes.
            this.setCountryCode(s);

        // Order Of Battle (position 15).
        s = symCode.substring(14, 15);
        if (SymbologyConstants.ORDER_OF_BATTLE_ALL_TACTICAL_GRAPHICS.contains(s.toUpperCase()))
            this.setOrderOfBattle(s);
        else if (!"-".equals(s)) // "-" is accepted and indicates a null order of battle.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.orderOfBattle"));

        return sb.length() > 0
            ? Logging.getMessage("Symbology.SymbolCodeFieldsUnrecognized", sb.toString(), symCode) : null;
    }

    /**
     * Parses a symbol code encoded in the Meteorological and Oceanographic coding scheme. METOC symbol codes are not
     * currently supported, and this returns a string indicating that the scheme is unrecognized.
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return an error string.
     */
    protected String parseMetocSymCode(String symCode)
    {
        StringBuilder sb = new StringBuilder();

        // Coding Scheme (position 1).
        String s = symCode.substring(0, 1);
        if (SymbologyConstants.SCHEME_METOC.equalsIgnoreCase(s))
            this.setScheme(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.scheme"));

        // Category (position 2).
        s = symCode.substring(1, 2);
        if (SymbologyConstants.CATEGORY_ALL_METOC.contains(s.toUpperCase()))
            this.setCategory(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.category"));

        // Static/Dynamic (position 3,4).
        s = symCode.substring(2, 4);
        if (SymbologyConstants.STATIC_DYNAMIC_ALL.contains(s.toUpperCase()))
            this.setStaticDynamic(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.status"));

        // Function ID (positions 5-10).
        s = symCode.substring(4, 10);
        if (!"------".equals(s)) // "------" is accepted and indicates a null function ID.
            this.setFunctionId(s);

        // Graphic Type (position 11-13).
        s = symCode.substring(10, 13);
        if (SymbologyConstants.GRAPHIC_TYPE_ALL.contains(s.toUpperCase()))
            this.setGraphicType(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.echelon"));

        // Positions 14 and 15 unused

        return sb.length() > 0
            ? Logging.getMessage("Symbology.SymbolCodeFieldsUnrecognized", sb.toString(), symCode) : null;
    }

    /**
     * Parses symbol codes encoded for the Signals Intelligence coding scheme. Signals Intelligence symbol codes contain
     * the following fields: Scheme, Standard Identity, Battle Dimension, Status, Function ID, Country Code, Order of
     * Battle. All fields except Function ID, Country Code and Order of Battle must be non-<code>null</code>.
     * <p>
     * The Signals Intelligence coding scheme is defined in MIL-STD-2525C table D-I (page 964).
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return <code>null</code> if the symbol code is recognized, otherwise a non-<code>null</code> string listing the
     *         unrecognized symbol elements.
     */
    protected String parseIntelligenceSymCode(String symCode)
    {
        StringBuilder sb = new StringBuilder();

        // Coding Scheme (position 1).
        String s = symCode.substring(0, 1);
        if (s != null && s.equalsIgnoreCase(SymbologyConstants.SCHEME_INTELLIGENCE))
            this.setScheme(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.scheme"));

        // Standard Identity/Exercise Amplifying Descriptor (position 2).
        s = symCode.substring(1, 2);
        if (SymbologyConstants.STANDARD_IDENTITY_ALL.contains(s.toUpperCase()))
            this.setStandardIdentity(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.standardIdentity"));

        // Battle Dimension (position 3).
        s = symCode.substring(2, 3);
        if (SymbologyConstants.BATTLE_DIMENSION_ALL_INTELLIGENCE.contains(s.toUpperCase()))
            this.setBattleDimension(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.battleDimension"));

        // Status/Operational Condition (position 4)
        s = symCode.substring(3, 4);
        if (SymbologyConstants.STATUS_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setStatus(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.status"));

        // Function ID (positions 5-10)
        s = symCode.substring(4, 10);
        if (!"------".equals(s)) // "------" is accepted and indicates a null function ID.
            this.setFunctionId(s);

        // Not Used (positions 11-12).
        s = symCode.substring(10, 12);
        if (!"--".equals(s)) // "--" is the only accepted string in positions 11-12.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.symbolModifier"));

        // Country Code (positions 13-14).
        s = symCode.substring(12, 14);
        if (!"--".equals(s)) // "--" is accepted and indicates a null country code.
            this.setCountryCode(s);

        // Order of Battle (position 15).
        s = symCode.substring(14, 15);
        if (SymbologyConstants.ORDER_OF_BATTLE_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setOrderOfBattle(s);
        else if (!"-".equals(s)) // "-" is accepted and indicates a null order of battle.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.orderOfBattle"));

        return sb.length() > 0
            ? Logging.getMessage("Symbology.SymbolCodeFieldsUnrecognized", sb.toString(), symCode) : null;
    }

    /**
     * Parses a symbol code encoded in the Stability Operations coding scheme. Stability Operations symbol codes contain
     * the following fields: Scheme, Standard Identity, Category, Status, Function ID, Symbol Modifier, Country Code,
     * Order of Battle. All fields except Function ID, Symbol Modifier, Country Code and Order of Battle must be
     * non-<code>null</code>.
     * <p>
     * The Stability Operations coding scheme is defined in MIL-STD-2525C table E-I (page 991).
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return <code>null</code> if the symbol code is recognized, otherwise a non-<code>null</code> string listing the
     *         unrecognized symbol elements.
     */
    protected String parseStabilityOperationsSymCode(String symCode)
    {
        StringBuilder sb = new StringBuilder();

        // Coding Scheme (position 1).
        String s = symCode.substring(0, 1);
        if (s != null && s.equalsIgnoreCase(SymbologyConstants.SCHEME_STABILITY_OPERATIONS))
            this.setScheme(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.scheme"));

        // Standard Identity/Exercise Amplifying Descriptor (position 2).
        s = symCode.substring(1, 2);
        if (SymbologyConstants.STANDARD_IDENTITY_ALL.contains(s.toUpperCase()))
            this.setStandardIdentity(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.standardIdentity"));

        // Category (position 3).
        s = symCode.substring(2, 3);
        if (SymbologyConstants.CATEGORY_ALL_STABILITY_OPERATIONS.contains(s.toUpperCase()))
            this.setCategory(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.category"));

        // Status/Operational Condition (position 4).
        s = symCode.substring(3, 4);
        if (SymbologyConstants.STATUS_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setStatus(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.status"));

        // Function ID (positions 5-10).
        s = symCode.substring(4, 10);
        if (!"------".equals(s)) // "------" is accepted and indicates a null function ID.
            this.setFunctionId(s);

        // Symbol Modifier (positions 11-12).
        s = symCode.substring(10, 12);
        if (this.isUnitsAndEquipmentSymbolModifier(s) || SymbologyConstants.INSTALLATION_ALL.contains(s.toUpperCase()))
            this.setSymbolModifier(s);
        else if (!"--".equals(s)) // "--" is accepted and indicates a null symbol modifier.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.symbolModifier"));

        // Country Code (positions 13-14).
        s = symCode.substring(12, 14);
        if (!"--".equals(s)) // "--" is accepted and indicates a null country code.
            this.setCountryCode(s);

        // Order Of Battle (position 15).
        s = symCode.substring(14, 15);
        if (SymbologyConstants.ORDER_OF_BATTLE_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setOrderOfBattle(s);
        else if (!"-".equals(s)) // "-" is accepted and indicates a null order of battle.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.orderOfBattle"));

        return sb.length() > 0
            ? Logging.getMessage("Symbology.SymbolCodeFieldsUnrecognized", sb.toString(), symCode) : null;
    }

    /**
     * Parses a symbol code encoded in the Emergency Management coding scheme. Emergency Management symbol codes contain
     * the following fields: Scheme, Standard Identity, Category, Status, Function ID, Symbol Modifier, Country Code,
     * Order of Battle. All fields except Function ID, Symbol Modifier, Country Code and Order of Battle must be
     * non-<code>null</code>.
     * <p>
     * The Emergency Management coding scheme is defined in MIL-STD-2525C table G-I (page 1032).
     *
     * @param symCode the symbol code to parse. Must be non-<code>null</code> and have length of 15 or greater. Any
     *                characters after the 15th character are ignored.
     *
     * @return <code>null</code> if the symbol code is recognized, otherwise a non-<code>null</code> string listing the
     *         unrecognized symbol elements.
     */
    protected String parseEmergencyManagementSymCode(String symCode)
    {
        StringBuilder sb = new StringBuilder();

        // Coding Scheme (position 1).
        String s = symCode.substring(0, 1);
        if (s != null && s.equalsIgnoreCase(SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT))
            this.setScheme(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.scheme"));

        // Standard Identity/Exercise Amplifying Descriptor (position 2).
        s = symCode.substring(1, 2);
        if (SymbologyConstants.STANDARD_IDENTITY_ALL.contains(s.toUpperCase()))
            this.setStandardIdentity(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.standardIdentity"));

        // Category (position 3).
        s = symCode.substring(2, 3);
        if (SymbologyConstants.CATEGORY_ALL_EMERGENCY_MANAGEMENT.contains(s.toUpperCase()))
            this.setCategory(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.category"));

        // Status/Operational Condition (position 4).
        s = symCode.substring(3, 4);
        if (SymbologyConstants.STATUS_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setStatus(s);
        else
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.status"));

        // Function ID (positions 5-10).
        s = symCode.substring(4, 10);
        if (!"------".equals(s)) // "------" is accepted and indicates a null function ID.
            this.setFunctionId(s);

        // Symbol Modifier (positions 11-12).
        s = symCode.substring(10, 12);
        if (SymbologyConstants.INSTALLATION_ALL.contains(s.toUpperCase())
            || SymbologyConstants.MOBILITY_ALL.contains(s.toUpperCase()))
        {
            this.setSymbolModifier(s);
        }
        else if (!"--".equals(s)) // "--" is accepted and indicates a null symbol modifier.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.symbolModifier"));

        // Country Code (positions 13-14).
        s = symCode.substring(12, 14);
        if (!"--".equals(s)) // "--" is accepted and indicates a null country code.
            this.setCountryCode(s);

        // Order Of Battle (position 15).
        s = symCode.substring(14, 15);
        if (SymbologyConstants.ORDER_OF_BATTLE_ALL_UEI_SIGINT_SO_EM.contains(s.toUpperCase()))
            this.setOrderOfBattle(s);
        else if (!"-".equals(s)) // "-" is accepted and indicates a null order of battle.
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("term.orderOfBattle"));

        return sb.length() > 0
            ? Logging.getMessage("Symbology.SymbolCodeFieldsUnrecognized", sb.toString(), symCode) : null;
    }

    /**
     * Indicates whether the specified 2-character Symbol Modifier code represents a units and equipment symbol modifier
     * code.
     *
     * @param value the modifier code to test.  Must be non-<code>null</code> and have length of 2 or greater. Any
     *              characters after the 2nd character are ignored.
     *
     * @return <code>true</code> if the specified code represents a units and equipment modifier code, and
     *         <code>false</code> otherwise.
     */
    protected boolean isUnitsAndEquipmentSymbolModifier(String value)
    {
        String firstChar = value.substring(0, 1).toUpperCase();
        String secondChar = value.substring(1, 2).toUpperCase();

        return (UNUSED_POSITION_CODE.equals(firstChar) && SymbologyConstants.ECHELON_ALL.contains(secondChar))
            || (SymbologyConstants.MODIFIER_CODE_ALL_UEI.contains(firstChar) && UNUSED_POSITION_CODE.equals(secondChar))
            || (SymbologyConstants.MODIFIER_CODE_ALL_UEI.contains(firstChar)
            && SymbologyConstants.ECHELON_ALL.contains(secondChar));
    }

    /**
     * Composes and returns a MIL-STD-2525 15-character symbol identification code (SIDC) from this SymbolCode's current
     * field values. Fields that are not part of this SymbolCode's current Coding Scheme are ignored. Fields that are
     * unspecified or null are replaced with the MIL-STD-2525 unused position character "-". Field values are either
     * padded or trimmed to fit their portion of the symbol code, adding unused characters to pad or ignoring extra
     * characters to trim.
     * <p>
     * This returns <code>null</code> if this SymbolCode's Coding Scheme is <code>null</code> or unrecognized.
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode, or
     *         <code>null</code> if the Coding Scheme is unrecognized.
     */
    protected String composeSymCode()
    {
        String scheme = this.getScheme();

        if (SymbologyConstants.SCHEME_WARFIGHTING.equalsIgnoreCase(scheme))
        {
            return this.composeWarfightingSymCode();
        }
        else if (SymbologyConstants.SCHEME_TACTICAL_GRAPHICS.equalsIgnoreCase(scheme))
        {
            return this.composeTacticalGraphicsSymCode();
        }
        else if (SymbologyConstants.SCHEME_METOC.equalsIgnoreCase(scheme))
        {
            return this.composeMetocSymCode();
        }
        else if (SymbologyConstants.SCHEME_INTELLIGENCE.equalsIgnoreCase(scheme))
        {
            return this.composeIntelligenceSymCode();
        }
        else if (SymbologyConstants.SCHEME_STABILITY_OPERATIONS.equalsIgnoreCase(scheme))
        {
            return this.composeStabilityOperationsSymCode();
        }
        else if (SymbologyConstants.SCHEME_EMERGENCY_MANAGEMENT.equalsIgnoreCase(scheme))
        {
            return this.composeEmergencyManagementSymCode();
        }
        else
        {
            return this.composeUnrecognizedSymCode();
        }
    }

    /**
     * Returns <code>null</code> indicating that this SymbolCode's Coding Scheme is not recognized.
     *
     * @return <code>null</code>.
     */
    protected String composeUnrecognizedSymCode()
    {
        return null;
    }

    /**
     * Composes a 15-character symbol identification code (SIDC) for the Warfighting coding scheme. Warfighting symbol
     * codes contain the following fields: Coding Scheme, Standard Identity, Battle Dimension, Status, Function ID,
     * Symbol Modifier, Country Code, Order of Battle.
     * <p>
     * The Warfighting coding scheme is defined in MIL-STD-2525C table A-I (page 51).
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode,
     *         according to the Warfighting coding scheme.
     */
    protected String composeWarfightingSymCode()
    {
        StringBuilder sb = new StringBuilder();

        appendFieldValue(sb, this.getScheme(), 1); // Position 1.
        appendFieldValue(sb, this.getStandardIdentity(), 1); // Position 2.
        appendFieldValue(sb, this.getBattleDimension(), 1); // Position 3.
        appendFieldValue(sb, this.getStatus(), 1); // Position 4.
        appendFieldValue(sb, this.getFunctionId(), 6); // Positions 5-10.
        appendFieldValue(sb, this.getSymbolModifier(), 2); // Positions 11-12.
        appendFieldValue(sb, this.getCountryCode(), 2);  // Positions 13-14.
        appendFieldValue(sb, this.getOrderOfBattle(), 1);// Position 15.

        return sb.toString();
    }

    /**
     * Composes a 15-character symbol identification code (SIDC) for the Tactical Graphics coding scheme. Tactical
     * Graphics symbol codes contain the following fields: Coding Scheme, Standard Identity, Category, Status, Function
     * ID, Echelon, Country Code, Order of Battle.
     * <p>
     * The Tactical Graphics coding scheme is defined in MIL-STD-2525C table B-I (page 305).
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode,
     *         according to the Tactical Graphics coding scheme.
     */
    protected String composeTacticalGraphicsSymCode()
    {
        StringBuilder sb = new StringBuilder();

        appendFieldValue(sb, this.getScheme(), 1); // Position 1.
        appendFieldValue(sb, this.getStandardIdentity(), 1); // Position 2.
        appendFieldValue(sb, this.getCategory(), 1); // Position 3.
        appendFieldValue(sb, this.getStatus(), 1); // Position 4.
        appendFieldValue(sb, this.getFunctionId(), 6); // Positions 5-10.
        sb.append(UNUSED_POSITION_CODE); // Position 11. Unused because the echelon code uses only position 12.
        appendFieldValue(sb, this.getEchelon(), 1); // Position 12.
        appendFieldValue(sb, this.getCountryCode(), 2);  // Positions 13-14.
        appendFieldValue(sb, this.getOrderOfBattle(), 1);// Position 15.

        return sb.toString();
    }

    /**
     * Composes a 15-character symbol identification code (SIDC) for the Meteorological and Oceanographic coding scheme.
     * METOC symbol codes contain the following fields: Coding Scheme, Category, Static/Dynamic, Function ID, Graphic
     * Type.
     * <p>
     * The Meteorological and Oceanographic coding scheme is defined in MIL-STD-2525C table C-I (page 763).
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode,
     *         according to the METOC coding scheme.
     */
    protected String composeMetocSymCode()
    {
        StringBuilder sb = new StringBuilder();

        appendFieldValue(sb, this.getScheme(), 1); // Position 1.
        appendFieldValue(sb, this.getCategory(), 1); // Position 2.
        appendFieldValue(sb, this.getStaticDynamic(), 2); // Position 3, 4.
        appendFieldValue(sb, this.getFunctionId(), 6); // Positions 5-10.
        appendFieldValue(sb, this.getGraphicType(), 3); // Position 11-13
        sb.append(UNUSED_POSITION_CODE); // Position 14 unused
        sb.append(UNUSED_POSITION_CODE); // Position 15 unused

        return sb.toString();
    }

    /**
     * Composes a 15-character symbol identification code (SIDC) for the Signals Intelligence coding scheme. Signals
     * Intelligence symbol codes contain the following fields: Scheme, Standard Identity, Battle Dimension, Status,
     * Function ID, Country Code, Order of Battle.
     * <p>
     * The Signals Intelligence coding scheme is defined in MIL-STD-2525C table D-I (page 964).
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode,
     *         according to the Signals Intelligence coding scheme.
     */
    protected String composeIntelligenceSymCode()
    {
        StringBuilder sb = new StringBuilder();

        appendFieldValue(sb, this.getScheme(), 1); // Position 1.
        appendFieldValue(sb, this.getStandardIdentity(), 1); // Position 2.
        appendFieldValue(sb, this.getBattleDimension(), 1); // Position 3.
        appendFieldValue(sb, this.getStatus(), 1); // Position 4.
        appendFieldValue(sb, this.getFunctionId(), 6); // Positions 5-10.
        sb.append(UNUSED_POSITION_CODE).append(UNUSED_POSITION_CODE); // Positions 11-12 are not used.
        appendFieldValue(sb, this.getCountryCode(), 2);  // Positions 13-14.
        appendFieldValue(sb, this.getOrderOfBattle(), 1);// Position 15.

        return sb.toString();
    }

    /**
     * Composes a 15-character symbol identification code (SIDC) for the Stability Operations coding scheme. Stability
     * Operations symbol codes contain the following fields: Scheme, Standard Identity, Category, Status, Function ID,
     * Symbol Modifier, Country Code, Order of Battle.
     * <p>
     * The Stability Operations coding scheme is defined in MIL-STD-2525C table E-I (page 991).
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode,
     *         according to the Stability Operations coding scheme.
     */
    protected String composeStabilityOperationsSymCode()
    {
        StringBuilder sb = new StringBuilder();

        appendFieldValue(sb, this.getScheme(), 1); // Position 1.
        appendFieldValue(sb, this.getStandardIdentity(), 1); // Position 2.
        appendFieldValue(sb, this.getCategory(), 1); // Position 3.
        appendFieldValue(sb, this.getStatus(), 1); // Position 4.
        appendFieldValue(sb, this.getFunctionId(), 6); // Positions 5-10.
        appendFieldValue(sb, this.getSymbolModifier(), 2); // Positions 11-12.
        appendFieldValue(sb, this.getCountryCode(), 2);  // Positions 13-14.
        appendFieldValue(sb, this.getOrderOfBattle(), 1);// Position 15.

        return sb.toString();
    }

    /**
     * Composes a 15-character symbol identification code (SIDC) for the Emergency Management coding scheme. Emergency
     * Management symbol codes contain the following fields: Standard Identity, Category, Status, Function ID, Symbol
     * Modifier, Country Code, Order of Battle.
     * <p>
     * The Emergency Management coding scheme is defined in MIL-STD-2525C table G-I (page 1032).
     *
     * @return the MIL-STD-2525 15-character symbol identification code (SIDC) corresponding to this SymbolCode,
     *         according to the Emergency Management coding scheme.
     */
    protected String composeEmergencyManagementSymCode()
    {
        StringBuilder sb = new StringBuilder();

        appendFieldValue(sb, this.getScheme(), 1); // Position 1.
        appendFieldValue(sb, this.getStandardIdentity(), 1); // Position 2.
        appendFieldValue(sb, this.getCategory(), 1); // Position 3.
        appendFieldValue(sb, this.getStatus(), 1); // Position 4.
        appendFieldValue(sb, this.getFunctionId(), 6); // Positions 5-10.
        appendFieldValue(sb, this.getSymbolModifier(), 2); // Positions 11-12.
        appendFieldValue(sb, this.getCountryCode(), 2);  // Positions 13-14.
        appendFieldValue(sb, this.getOrderOfBattle(), 1);// Position 15.

        return sb.toString();
    }

    /**
     * Appends the specified field value to the specified StringBuilder, padding or trimming the value to fit its length
     * in the symbol code as necessary. If the value is shorter than the specified length, this appends the MIL-STD-2525
     * unused character "-" to fill the unused characters. If the value is longer than the specified length, this
     * ignores the extra characters. If the value is <code>null</code> or empty, this appends unused characters to fill
     * the entire space used by the field.
     *
     * @param sb     the StringBuilder representing a MIL-STD-2525 symbol identification code (SIDC).
     * @param value  the field value to append.
     * @param length the number of positions used by the field in the SIDC.
     */
    public static void appendFieldValue(StringBuilder sb, String value, int length)
    {
        if (sb == null)
        {
            String msg = Logging.getMessage("nullValue.StringBuilderIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length < 0)
        {
            String msg = Logging.getMessage("generic.LengthIsInvalid", length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Append the code's characters, starting at character 0 and stopping after the number of character positions
        // assigned to the code have been reached or the code's characters are exhausted, whichever comes first. This
        // does nothing if the code is null or empty. If the code contains fewer characters then its assigned length,
        // then only those characters are appended.
        if (value != null && value.length() > 0)
            sb.append(value, 0, value.length() < length ? value.length() : length);

        // Append the "unused" character for each unused character position assigned to the code. We encounter unused
        // positions when the code is null or its length is less than the number of assigned character positions.
        for (int i = (value != null ? value.length() : 0); i < length; i++)
        {
            sb.append(UNUSED_POSITION_CODE);
        }
    }

    /**
     * Indicates whether the specified field value is empty. This returns <code>true</code> if the specified value is
     * <code>null</code>, is the empty string, or is filled entirely with the unused character "-".
     *
     * @param value the value to test. May be <code>null</code>.
     *
     * @return <code>true</code> if the value is empty, and <code>false</code> otherwise.
     */
    public static boolean isFieldEmpty(String value)
    {
        return value == null || value.isEmpty() || value.replaceAll(UNUSED_POSITION_CODE, "").trim().isEmpty();
    }
}
