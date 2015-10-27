/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Implementation of TacticalSymbol to render point graphics defined by MIL-STD-2525C Appendix B (Tactical Graphics).
 * This class implements the logic for rendering tactical point graphics, but actually implements the TacticalSymbol
 * interface.
 * <p/>
 * This class is not meant to be used directly by applications. Instead, apps should use {@link MilStd2525PointGraphic},
 * which implements the {@link TacticalGraphic} interface. (MilStd2525PointGraphic uses TacticalGraphicSymbol internally
 * to render the point graphic.)
 *
 * @author pabercrombie
 * @version $Id: TacticalGraphicSymbol.java 2196 2014-08-06 19:42:15Z tgaskins $
 * @see MilStd2525PointGraphic
 */
public class TacticalGraphicSymbol extends AbstractTacticalSymbol
{
    /**
     * Object that provides the default offset for each point graphic. Most graphics are centered on their position, but
     * some require a different offset.
     */
    protected static DefaultOffsets defaultOffsets = new DefaultOffsets();

    /** Object that provides the default label layouts for each point graphic. */
    protected static DefaultLabelLayouts defaultLayouts = new DefaultLabelLayouts();

    protected static final Offset BELOW_BOTTOM_CENTER_OFFSET = Offset.fromFraction(0.5, -0.1);
    /** The default number of label lines to expect when computing the minimum size of the text layout rectangle. */
    protected static final int DEFAULT_LABEL_LINES = 2;

    public static class LabelLayout
    {
        protected String modifier;
        protected List<OffsetPair> offsets = new ArrayList<OffsetPair>();

        public LabelLayout(String modifier)
        {
            this.modifier = modifier;
        }

        public void add(Offset offset, Offset hotspot)
        {
            this.offsets.add(new OffsetPair(offset, hotspot));
        }

        public String getModifier()
        {
            return modifier;
        }

        public List<OffsetPair> getOffsets()
        {
            return this.offsets;
        }
    }

    public static class OffsetPair
    {
        public Offset offset;
        public Offset hotSpot;

        public OffsetPair(Offset offset, Offset hotSpot)
        {
            this.offset = offset;
            this.hotSpot = hotSpot;
        }
    }

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

    /**
     * Constructs a new symbol with no position.
     *
     * @param sidc Code that identifies the graphic.
     */
    public TacticalGraphicSymbol(String sidc)
    {
        super();
        init(sidc);
    }

    /**
     * Constructs a new symbol with the specified position. The position specifies the latitude, longitude, and altitude
     * where this symbol is drawn on the globe. The position's altitude component is interpreted according to the
     * altitudeMode.
     *
     * @param sidc     Code that identifies the graphic.
     * @param position The latitude, longitude, and altitude where the symbol is drawn.
     *
     * @throws IllegalArgumentException if the position is <code>null</code>.
     */
    public TacticalGraphicSymbol(String sidc, Position position)
    {
        super(position);
        init(sidc);
    }

    /**
     * Indicates the current value of graphic's Status/Operational Condition field.
     *
     * @return this graphic's Status/Operational Condition field.
     *
     * @see #setStatus(String)
     */
    public String getStatus()
    {
        return this.symbolCode.getStatus();
    }

    /**
     * Specifies this graphic's Status/Operational Condition field. A graphic's Status defines whether the represented
     * object exists at the time the symbol was generated, or is anticipated to exist in the future. Additionally, a
     * graphic's Status can define its operational condition. The recognized values depend on the graphic's scheme:
     * <p/>
     * <strong>Tactical graphics</strong>
     * <p/>
     * <ul> <li>STATUS_ANTICIPATED</li> <li>STATUS_SUSPECTED</li> <li>STATUS_PRESENT</li> <li>STATUS_KNOWN</li> </ul>
     * <p/>
     * <strong>Meteorological and Oceanographic</strong>
     * <p/>
     * <ul> <li>Not supported</li> </ul>
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
     * Initialize the new symbol.
     *
     * @param sidc Code that identifies the graphic.
     */
    protected void init(String sidc)
    {
        this.symbolCode = new SymbolCode(sidc);
        this.maskedSymbolCode = this.symbolCode.toMaskedString();

        this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);

        // Configure this tactical point graphic's icon retriever and modifier retriever with either the
        // configuration value or the default value (in that order of precedence).
        String iconRetrieverPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
            MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
        this.setIconRetriever(new MilStd2525PointGraphicRetriever(iconRetrieverPath));

        Offset offset = defaultOffsets.get(this.symbolCode.toMaskedString());
        this.setOffset(offset);

        // By default, show the hostile indicator (the letters "ENY"). Note that this default is different from
        // MilStd2525TacticalSymbol, which does not display the hostile indicator by default. Section 5.5.1.1 (pg. 37)
        // of MIL-STD-2525C states that the indicator is not required if color is used in the display. We choose to
        // display the indicator by default following the principle that by default hostile entities should look as
        // hostile as possible (to avoid being mistaken for friendly entities). In the case of tactical symbols, however
        // the indicator is redundant to both the symbol frame and fill, so it is not displayed by default.
        this.setShowHostileIndicator(true);

        // Use the same default unit format as 2525 tactical symbols.
        this.setUnitsFormat(MilStd2525TacticalSymbol.DEFAULT_UNITS_FORMAT);
    }

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        return this.symbolCode.toString();
    }

    @Override
    protected int getMaxLabelLines(AVList modifiers)
    {
        return DEFAULT_LABEL_LINES;
    }

    @Override
    protected void applyImplicitModifiers(AVList modifiers)
    {
        String si = this.symbolCode.getStandardIdentity();

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

        if (!modifiers.hasKey(SymbologyConstants.TYPE))
        {
            if (TacGrpSidc.MOBSU_CBRN_REEVNT_BIO.equalsIgnoreCase(this.maskedSymbolCode))
                modifiers.setValue(SymbologyConstants.TYPE, "BIO");
            else if (TacGrpSidc.MOBSU_CBRN_REEVNT_CML.equalsIgnoreCase(this.maskedSymbolCode))
                modifiers.setValue(SymbologyConstants.TYPE, "CML");
        }
    }

    /**
     * Layout text and graphic modifiers around the symbol.
     *
     * @param dc        Current draw context.
     * @param modifiers Modifiers applied to this graphic.
     */
    @Override
    protected void layoutTextModifiers(DrawContext dc, AVList modifiers, OrderedSymbol osym)
    {
        this.currentLabels.clear();

        Font font = this.getActiveAttributes().getTextModifierFont();
        List<LabelLayout> allLayouts = this.getLayouts(this.symbolCode.toMaskedString());

        for (LabelLayout layout : allLayouts)
        {
            java.util.List<OffsetPair> offsets = layout.offsets;

            if (WWUtil.isEmpty(offsets))
                continue;

            Object value = modifiers.getValue(layout.modifier);
            if (WWUtil.isEmpty(value))
                continue;

            // If we're retrieving the date modifier, maybe add a hyphen to the first value to indicate a date range.
            if (SymbologyConstants.DATE_TIME_GROUP.equals(layout.modifier) && (value instanceof Iterable))
            {
                value = this.addHyphenToDateRange((Iterable) value, offsets);
            }

            String mode = SymbologyConstants.LOCATION.equals(layout.modifier) ? LAYOUT_RELATIVE : LAYOUT_NONE;

            // Some graphics support multiple instances of the same modifier. Handle this case differently than the
            // single instance case.
            if (value instanceof Iterable)
            {
                this.layoutMultiLabel(dc, font, offsets, (Iterable) value, mode, osym);
            }
            else if (value != null)
            {
                this.layoutLabel(dc, font, layout.offsets.get(0), value.toString(), mode, osym);
            }
        }
    }

    /**
     * Indicates the label layouts designed to a particular graphic.
     *
     * @param sidc Symbol ID to for which to determine layout.
     *
     * @return List of label layouts for the specified symbol.
     */
    protected List<LabelLayout> getLayouts(String sidc)
    {
        return defaultLayouts.get(sidc);
    }

    @Override
    protected void layoutDynamicModifiers(DrawContext dc, AVList modifiers, OrderedSymbol osym)
    {
        this.currentLines.clear();

        if (!this.isShowGraphicModifiers())
            return;

        // Direction of Movement indicator. Placed at the bottom of the symbol layout. Direction of Movement applies
        // only to CBRN graphics (see MIL-STD-2525C table XI, pg. 38).
        Object o = modifiers.getValue(SymbologyConstants.DIRECTION_OF_MOVEMENT);
        if (this.isShowDirectionOfMovement() && o instanceof Angle)
        {
            // The length of the direction of movement line is equal to the height of the symbol frame. See
            // MIL-STD-2525C section 5.3.4.1.c, page 33.
            double length = this.iconRect.getHeight();

            java.util.List<? extends Point2D> points = MilStd2525Util.computeGroundHeadingIndicatorPoints(dc,
                osym.placePoint, (Angle) o, length, this.iconRect.getHeight());
            this.addLine(dc, BELOW_BOTTOM_CENTER_OFFSET, points, LAYOUT_RELATIVE, points.size() - 1, osym);
        }
    }

    //////////////////////////////////////////////
    // Modifier layout
    //////////////////////////////////////////////

    /**
     * Add a hyphen to the first element in a list of dates to indicate a date range. This method only modifiers the
     * date list if exactly two dates are displayed in the graphic.
     *
     * @param value   Iterable of date modifiers.
     * @param offsets Layouts for the date modifiers.
     *
     * @return Iterable of modified dates. This may be a new, modified list, or the same list as {@code value} if no
     *         modification was required.
     */
    protected Iterable addHyphenToDateRange(Iterable value, java.util.List<OffsetPair> offsets)
    {
        // Only add a hyphen if exactly two dates are displayed in the graphic.
        if (offsets.size() != 2)
            return value;

        // Make sure that two date values are provided.
        Iterator iterator = value.iterator();
        Object date1 = iterator.hasNext() ? iterator.next() : null;
        Object date2 = iterator.hasNext() ? iterator.next() : null;

        // If only two dates were provided, add a hyphen to indicate a date range. If more or less
        // date were provided it's not a date range, so don't change anything.
        if (date1 != null && date2 != null)
        {
            return Arrays.asList(date1 + "-", date2);
        }
        return value;
    }

    protected void layoutLabel(DrawContext dc, Font font, OffsetPair layout, String value, String mode,
        OrderedSymbol osym)
    {
        if (!WWUtil.isEmpty(value))
        {
            this.addLabel(dc, layout.offset, layout.hotSpot, value, font, null, mode, osym);
        }
    }

    protected void layoutMultiLabel(DrawContext dc, Font font, java.util.List<OffsetPair> layouts, Iterable values,
        String mode, OrderedSymbol osym)
    {
        Iterator valueIterator = values.iterator();
        Iterator<OffsetPair> layoutIterator = layouts.iterator();

        while (layoutIterator.hasNext() && valueIterator.hasNext())
        {
            OffsetPair layout = layoutIterator.next();
            Object value = valueIterator.next();
            if (value != null)
            {
                this.layoutLabel(dc, font, layout, value.toString(), mode, osym);
            }
        }
    }

    /**
     * Indicates whether or not this graphic supports the direction of movement indicator. Only chemical, biological,
     * radiological, and nuclear point graphics support this modifier  (see MIL-STD-2525C, table XI, pg. 38).
     *
     * @return True if the graphic is chemical, biological, radiological, or nuclear.
     */
    protected boolean isShowDirectionOfMovement()
    {
        String code = this.maskedSymbolCode;

        return TacGrpSidc.MOBSU_CBRN_NDGZ.equalsIgnoreCase(code)
            || TacGrpSidc.MOBSU_CBRN_FAOTP.equalsIgnoreCase(code)
            || TacGrpSidc.MOBSU_CBRN_REEVNT_BIO.equalsIgnoreCase(code)
            || TacGrpSidc.MOBSU_CBRN_REEVNT_CML.equalsIgnoreCase(code);
    }

    @Override
    protected void computeTransform(DrawContext dc, OrderedSymbol osym)
    {
        super.computeTransform(dc, osym);

        // Compute an appropriate offset if the application has not specified an offset and this symbol supports the
        // direction of movement indicator. Only the CBRN graphics in MIL-STD-2525C support this indicator. (Using the
        // graphic's default offset would cause the direction of movement line and location label to be cut off by the
        // surface when the globe is tilted.)
        if (this.iconRect != null && osym.layoutRect != null && this.isShowDirectionOfMovement())
        {
            osym.dx = -this.iconRect.getCenterX();
            osym.dy = -osym.layoutRect.getMinY();
        }
    }
}
