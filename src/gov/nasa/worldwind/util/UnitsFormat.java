/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.*;

/**
 * Consolidates the conversion, display and formatting of geographic units such as lengths in miles and areas in
 * hectares. Applications configure a class instance to the desired units, labels and display formats, then simply
 * retrieve display strings from the instance via one standard interface. All input values are in meters; the class
 * performs the necessary conversions to the selected display units.
 *
 * @author tag
 * @version $Id: UnitsFormat.java 2301 2014-09-06 00:34:45Z tgaskins $
 */
public class UnitsFormat extends AVListImpl
{
    // Keys identifying unit systems and units
    public static final String IMPERIAL_SYSTEM = "gov.nasa.worldwind.units.ImperialSystem";
    public static final String METRIC_SYSTEM = "gov.nasa.worldwind.units.MetricSystem";

    public static final String METERS = "UnitsFormat.Meters";
    public static final String KILOMETERS = "UnitsFormat.Kilometers";
    public static final String MILES = "UnitsFormat.Miles";
    public static final String NAUTICAL_MILES = "UnitsFormat.NauticalMiles";
    public static final String YARDS = "UnitsFormat.Yards";
    public static final String FEET = "UnitsFormat.Feet";

    public static final String SQUARE_METERS = "UnitsFormat.SquareMeters";
    public static final String SQUARE_KILOMETERS = "UnitsFormat.SquareKilometers";
    public static final String SQUARE_MILES = "UnitsFormat.SquareMiles";
    public static final String HECTARE = "UnitsFormat.Hectare";
    public static final String ACRE = "UnitsFormat.Acre";
    public static final String SQUARE_YARDS = "UnitsFormat.SquareYards";
    public static final String SQUARE_FEET = "UnitsFormat.SquareFeet";

    // Keys identifying the symbol used when displaying the units
    public static final String SYMBOL_METERS = "m";
    public static final String SYMBOL_KILOMETERS = "km";
    public static final String SYMBOL_MILES = "miles";
    public static final String SYMBOL_NAUTICAL_MILES = "Nm";
    public static final String SYMBOL_YARDS = "yd";
    public static final String SYMBOL_FEET = "ft";

    public static final String SYMBOL_SQUARE_METERS = "m\u00b2";
    public static final String SYMBOL_SQUARE_KILOMETERS = "km\u00b2";
    public static final String SYMBOL_SQUARE_MILES = "miles\u00b2";
    public static final String SYMBOL_HECTARE = "ha";
    public static final String SYMBOL_ACRE = "acres";
    public static final String SYMBOL_SQUARE_YARDS = "yd\u00b2";
    public static final String SYMBOL_SQUARE_FEET = "ft\u00b2";

    // These keys identifying labels correspond to message catalog properties
    public static final String LABEL_LATITUDE = "UnitsFormat.LatitudeLabel";
    public static final String LABEL_LONGITUDE = "UnitsFormat.LongitudeLabel";
    public static final String LABEL_LATLON_LAT = "UnitsFormat.LatLonLatLabel";
    public static final String LABEL_LATLON_LON = "UnitsFormat.LatLonLonLabel";
    public static final String LABEL_HEADING = "UnitsFormat.HeadingLabel";
    public static final String LABEL_EYE_ALTITUDE = "UnitsFormat.EyeAltitudeLabel";
    public static final String LABEL_PITCH = "UnitsFormat.PitchLabel";
    public static final String LABEL_UTM_ZONE = "UnitsFormat.UTMZoneLabel";
    public static final String LABEL_UTM_EASTING = "UnitsFormat.UTMEastingLabel";
    public static final String LABEL_UTM_NORTHING = "UnitsFormat.UTMNorthingLabel";
    public static final String LABEL_TERRAIN_HEIGHT = "UnitsFormat.TerrainHeightLabel";
    public static final String LABEL_DATUM = "UnitsFormat.DatumLabel";

    // Keys identifying display formats
    public static final String FORMAT_LENGTH = "UnitsFormat.FormatLength";
    public static final String FORMAT_AREA = "UnitsFormat.FormatArea";
    public static final String FORMAT_PITCH = "UnitsFormat.FormatPitch";
    public static final String FORMAT_HEADING = "UnitsFormat.FormatHeading";
    public static final String FORMAT_UTM_NORTHING = "UnitsFormat.FormatUTMNorthing";
    public static final String FORMAT_UTM_EASTING = "UnitsFormat.FormatUTMEasting";
    public static final String FORMAT_EYE_ALTITUDE = "UnitsFormat.FormatEyeAltitude";
    public static final String FORMAT_DECIMAL_DEGREES = "UnitsFormat.FormatDecimalDegrees";
    public static final String FORMAT_TERRAIN_HEIGHT = "UnitsFormat.FormatTerrainHeight";

    protected static final String NL = "\n";

    protected boolean showDMS = false;

    protected String lengthUnits;
    protected String lengthUnitsSymbol;
    protected double lengthUnitsMultiplier; // factor to convert from meters to current units
    protected String areaUnits;
    protected String areaUnitsSymbol;
    protected double areaUnitsMultiplier; // factor to convert from meters to current units
    protected String altitudeUnits;
    protected String altitudeUnitsSymbol;
    protected double altitudeUnitsMultiplier; // factor to convert from meters to current units

    /**
     * Construct an instance that displays length in kilometers, area in square kilometers and angles in decimal
     * degrees.
     */
    public UnitsFormat()
    {
        this(UnitsFormat.KILOMETERS, UnitsFormat.SQUARE_KILOMETERS, false);
    }

    /**
     * Constructs an instance that display length and area in specified units, and angles in decimal degrees.
     *
     * @param lengthUnits the desired length units. Available length units are <code>METERS, KILOMETERS, MILES,
     *                    NAUTICAL_MILES, YARDS</code> and <code>FEET</code.
     * @param areaUnits   the desired area units. Available area units are <code>SQUARE_METERS, SQUARE_KILOMETERS,
     *                    HECTARE, ACRE, SQUARE_YARD</code> and <code>SQUARE_FEET</code>.
     *
     * @throws IllegalArgumentException if either <code>lengthUnits</code> or <code>areaUnits</code> is null.
     */
    public UnitsFormat(String lengthUnits, String areaUnits)
    {
        this(lengthUnits, areaUnits, false);
    }

    /**
     * Constructs an instance that display length and area in specified units, and angles in a specified format.
     *
     * @param lengthUnits the desired length units. Available length units are <code>METERS, KILOMETERS, MILES,
     *                    NAUTICAL_MILES, YARDS</code> and <code>FEET</code.
     * @param areaUnits   the desired area units. Available area units are <code>SQUARE_METERS, SQUARE_KILOMETERS,
     *                    HECTARE, ACRE, SQUARE_YARD</code> and <code>SQUARE_FEET</code>.
     * @param showDMS     true if the desired angle format is degrees-minutes-seconds, false if the format is decimal
     *                    degrees.
     *
     * @throws IllegalArgumentException if either <code>lengthUnits</code> or <code>areaUnits</code> is null.
     */
    public UnitsFormat(String lengthUnits, String areaUnits, boolean showDMS)
    {
        if (lengthUnits == null)
        {
            String msg = Logging.getMessage("nullValue.LengthUnit");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (areaUnits == null)
        {
            String msg = Logging.getMessage("nullValue.AreaUnit");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setDefaultLabels();
        this.setDefaultFormats();
        this.setLengthUnits(lengthUnits);
        this.setAltitudeUnits(lengthUnits); // initialize to same as length units
        this.setAreaUnits(areaUnits);
        this.setShowDMS(showDMS);
    }

    /**
     * Establishes the labels to use when displaying values other than length and area. A label is a string placed
     * before the value and its units. Examples are <em>Latitude</em> 24.36, <em>UTM Zone:</em> 12, and <em>Eye
     * Altitude:</em> 500 km, where the emphasized terms are the labels. Subclasses can override this method to
     * establish labels other than the defaults. The default labels are drawn from the current message properties. The
     * recognized labels are those indicated by the "LABEL_" constants defined by this class or by its subclasses.
     */
    protected void setDefaultLabels()
    {
        this.setLabel(LABEL_LATITUDE, Logging.getMessage(LABEL_LATITUDE));
        this.setLabel(LABEL_LONGITUDE, Logging.getMessage(LABEL_LONGITUDE));
        this.setLabel(LABEL_LATLON_LAT, Logging.getMessage(LABEL_LATLON_LAT));
        this.setLabel(LABEL_LATLON_LON, Logging.getMessage(LABEL_LATLON_LON));
        this.setLabel(LABEL_HEADING, Logging.getMessage(LABEL_HEADING));
        this.setLabel(LABEL_EYE_ALTITUDE, Logging.getMessage(LABEL_EYE_ALTITUDE));
        this.setLabel(LABEL_PITCH, Logging.getMessage(LABEL_PITCH));
        this.setLabel(LABEL_UTM_ZONE, Logging.getMessage(LABEL_UTM_ZONE));
        this.setLabel(LABEL_UTM_EASTING, Logging.getMessage(LABEL_UTM_EASTING));
        this.setLabel(LABEL_UTM_NORTHING, Logging.getMessage(LABEL_UTM_NORTHING));
        this.setLabel(LABEL_TERRAIN_HEIGHT, Logging.getMessage(LABEL_TERRAIN_HEIGHT));
        this.setLabel(LABEL_DATUM, "Datum:");
    }

    /**
     * Establishes the format declarations to use for certain values. Examples are " %,12.1f %s" for lengths and "
     * %,11.1f" for UTM northings. Subclasses can override this method to establish formats other than the defaults. The
     * value types that have associated formats are indicated by the "FORMAT_" constants defined by this class or by its
     * subclasses.
     */
    protected void setDefaultFormats()
    {
        this.setFormat(FORMAT_LENGTH, " %,12.1f %s");
        this.setFormat(FORMAT_AREA, " %,12.1f %s");
        this.setFormat(FORMAT_PITCH, " %9.2f\u00b0");
        this.setFormat(FORMAT_HEADING, " %9.2f\u00b0");
        this.setFormat(FORMAT_UTM_NORTHING, " %,11.1f");
        this.setFormat(FORMAT_UTM_EASTING, " %,11.1f");
        this.setFormat(FORMAT_EYE_ALTITUDE, " %,6d %s");
        this.setFormat(FORMAT_DECIMAL_DEGREES, "%9.4f\u00B0");
        this.setFormat(FORMAT_TERRAIN_HEIGHT, " (ve %3.1f) %,6d %s");
    }

    /**
     * Set the label of a specified value type. See {@link #setDefaultLabels()} for a description and examples of
     * labels.
     *
     * @param labelName a key identifying the label type. Available names are those indicated by the "LABEL_" constants
     *                  defined by this class or by its subclasses
     * @param label     the label to use for the specified value type.
     *
     * @throws IllegalArgumentException if either the label or label name is null.
     */
    public void setLabel(String labelName, String label)
    {
        if (labelName == null)
        {
            String msg = Logging.getMessage("nullValue.LabelKey");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (label == null)
        {
            String msg = Logging.getMessage("nullValue.Label");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setValue(labelName, label);
    }

    /**
     * Returns the label for a specified label name.
     *
     * @param labelName the name of the label to return.
     *
     * @return the label, or null if the label does not exist.
     *
     * @throws IllegalArgumentException if the label name is null.
     */
    public String getLabel(String labelName)
    {
        if (labelName == null)
        {
            String msg = Logging.getMessage("nullValue.LabelKey");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.getStringValue(labelName);
    }

    /**
     * Set the format of a specified value type. See {@link #setDefaultFormats()} for a description and examples of
     * formats.
     *
     * @param formatName a key identifying the value type that is to have the specified format. Available types are
     *                   those indicated by the "FORMAT_" constants defined by this class or by its subclasses
     * @param format     the label to use for the specified value type.
     *
     * @throws IllegalArgumentException if either the format or format name are null.
     */
    public void setFormat(String formatName, String format)
    {
        if (formatName == null)
        {
            String msg = Logging.getMessage("nullValue.FormatKey");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (format == null)
        {
            String msg = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setValue(formatName, format);
    }

    /**
     * Returns the format for a specified value type.
     *
     * @param formatName the name of the value type whose format is desired.
     *
     * @return the format, or null if the format does not exist.
     *
     * @throws IllegalArgumentException if the format name is null.
     */
    public String getFormat(String formatName)
    {
        if (formatName == null)
        {
            String msg = Logging.getMessage("nullValue.FormatKey");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.getStringValue(formatName);
    }

    /**
     * Indicates whether angles are displayed in degrees-minutes-seconds.
     *
     * @return true if angles are displayed in degrees-minutes seconds, false if they're displayed in decimal degrees.
     */
    public boolean isShowDMS()
    {
        return this.showDMS;
    }

    /**
     * Specifies whether angles are displayed in degrees-minutes-seconds.
     *
     * @param showDMS true to display angles in degrees-minutes seconds, false to display them in decimal degrees.
     */
    public void setShowDMS(boolean showDMS)
    {
        this.showDMS = showDMS;
    }

    /**
     * Returns the units symbol for the current length units. Examples are "m" for meters and "Nm" for nautical miles.
     *
     * @return the units symbol for the current length units.
     */
    public String getLengthUnitsSymbol()
    {
        return this.lengthUnitsSymbol;
    }

    /**
     * Returns the current length units.
     *
     * @return the current length units. See {@link #UnitsFormat(String, String, boolean)} for the list of those
     *         available.
     */
    public String getLengthUnits()
    {
        return this.lengthUnits;
    }

    /**
     * Specifies the units in which to display length values. Units subsequently formatted by the instance are converted
     * from meters to the desired units prior to formatting.
     *
     * @param lengthUnits the desired length units. See {@link #UnitsFormat(String, String, boolean)} for the list of
     *                    those available.
     *
     * @throws IllegalArgumentException if <code>lengthUnits</code> is null.
     */
    public void setLengthUnits(String lengthUnits)
    {
        if (lengthUnits == null)
        {
            String msg = Logging.getMessage("nullValue.LengthUnit");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.lengthUnits = lengthUnits;

        if (lengthUnits.equals(UnitsFormat.KILOMETERS))
        {
            this.lengthUnitsMultiplier = WWMath.METERS_TO_KILOMETERS;
            this.lengthUnitsSymbol = UnitsFormat.SYMBOL_KILOMETERS;
        }
        else if (lengthUnits.equals(UnitsFormat.MILES))
        {
            this.lengthUnitsMultiplier = WWMath.METERS_TO_MILES;
            this.lengthUnitsSymbol = UnitsFormat.SYMBOL_MILES;
        }
        else if (lengthUnits.equals(UnitsFormat.NAUTICAL_MILES))
        {
            this.lengthUnitsMultiplier = WWMath.METERS_TO_NAUTICAL_MILES;
            this.lengthUnitsSymbol = UnitsFormat.SYMBOL_NAUTICAL_MILES;
        }
        else if (lengthUnits.equals(UnitsFormat.YARDS))
        {
            this.lengthUnitsMultiplier = WWMath.METERS_TO_YARDS;
            this.lengthUnitsSymbol = UnitsFormat.SYMBOL_YARDS;
        }
        else if (lengthUnits.equals(UnitsFormat.FEET))
        {
            this.lengthUnitsMultiplier = WWMath.METERS_TO_FEET;
            this.lengthUnitsSymbol = UnitsFormat.SYMBOL_FEET;
        }
        else
        {
            this.lengthUnitsMultiplier = 1d;
            this.lengthUnitsSymbol = UnitsFormat.SYMBOL_METERS;
        }
    }

    /**
     * Indicates the multiplier used to convert from meters to the current length units.
     *
     * @return the conversion multiplier to convert meters to the current length units.
     */
    public double getLengthUnitsMultiplier()
    {
        return this.lengthUnitsMultiplier;
    }

    /**
     * Returns the units symbol for the current altitude units. Examples are "m" for meters and "Nm" for nautical
     * miles.
     *
     * @return the units symbol for the current altitude units.
     */
    public String getAltitudeUnitsSymbol()
    {
        return this.altitudeUnitsSymbol;
    }

    /**
     * Returns the current altitude units.
     *
     * @return the current altitude units. See {@link #UnitsFormat(String, String, boolean)} for the list of those
     *         available.
     */
    public String getAltitudeUnits()
    {
        return this.altitudeUnits;
    }

    /**
     * Specifies the units in which to display altitude values. Units subsequently formatted by the instance are
     * converted from meters to the desired units prior to formatting.
     *
     * @param altitudeUnits the desired altitude units. See {@link #UnitsFormat(String, String, boolean)} for the list
     *                      of those available.
     *
     * @throws IllegalArgumentException if <code>lengthUnits</code> is null.
     */
    public void setAltitudeUnits(String altitudeUnits)
    {
        if (altitudeUnits == null)
        {
            String msg = Logging.getMessage("nullValue.AltitudeUnit");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.altitudeUnits = altitudeUnits;

        if (altitudeUnits.equals(UnitsFormat.KILOMETERS))
        {
            this.altitudeUnitsMultiplier = WWMath.METERS_TO_KILOMETERS;
            this.altitudeUnitsSymbol = UnitsFormat.SYMBOL_KILOMETERS;
        }
        else if (altitudeUnits.equals(UnitsFormat.MILES))
        {
            this.altitudeUnitsMultiplier = WWMath.METERS_TO_MILES;
            this.altitudeUnitsSymbol = UnitsFormat.SYMBOL_MILES;
        }
        else if (altitudeUnits.equals(UnitsFormat.NAUTICAL_MILES))
        {
            this.altitudeUnitsMultiplier = WWMath.METERS_TO_NAUTICAL_MILES;
            this.altitudeUnitsSymbol = UnitsFormat.SYMBOL_NAUTICAL_MILES;
        }
        else if (altitudeUnits.equals(UnitsFormat.YARDS))
        {
            this.altitudeUnitsMultiplier = WWMath.METERS_TO_YARDS;
            this.altitudeUnitsSymbol = UnitsFormat.SYMBOL_YARDS;
        }
        else if (altitudeUnits.equals(UnitsFormat.FEET))
        {
            this.altitudeUnitsMultiplier = WWMath.METERS_TO_FEET;
            this.altitudeUnitsSymbol = UnitsFormat.SYMBOL_FEET;
        }
        else
        {
            this.altitudeUnitsMultiplier = 1d;
            this.altitudeUnitsSymbol = UnitsFormat.SYMBOL_METERS;
        }
    }

    /**
     * Indicates the multiplier used to convert from meters to the current altitude units.
     *
     * @return the conversion multiplier to convert meters to the current altitude units.
     */
    public double getAltitudeUnitsMultiplier()
    {
        return this.altitudeUnitsMultiplier;
    }

    /**
     * Returns the current area units.
     *
     * @return the current area units. See {@link #UnitsFormat(String, String, boolean)} for the list of those
     *         available.
     */
    public String getAreaUnits()
    {
        return this.areaUnits;
    }

    /**
     * Specifies the units in which to display area values. Units subsequently formatted by the instance are converted
     * from square meters to the desired units prior to formatting.
     *
     * @param areaUnits the desired length units. See {@link #UnitsFormat(String, String, boolean)} for the list of
     *                  those available.
     *
     * @throws IllegalArgumentException if <code>areaUnits</code> is null.
     */
    public void setAreaUnits(String areaUnits)
    {
        if (areaUnits == null)
        {
            String msg = Logging.getMessage("nullValue.AreaUnit");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.areaUnits = areaUnits;

        if (areaUnits.equals(UnitsFormat.SQUARE_KILOMETERS))
        {
            this.areaUnitsMultiplier = WWMath.SQUARE_METERS_TO_SQUARE_KILOMETERS;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_SQUARE_KILOMETERS;
        }
        else if (areaUnits.equals(UnitsFormat.SQUARE_MILES))
        {
            this.areaUnitsMultiplier = WWMath.SQUARE_METERS_TO_SQUARE_MILES;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_SQUARE_MILES;
        }
        else if (areaUnits.equals(UnitsFormat.HECTARE))
        {
            this.areaUnitsMultiplier = WWMath.SQUARE_METERS_TO_HECTARES;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_HECTARE;
        }
        else if (areaUnits.equals(UnitsFormat.ACRE))
        {
            this.areaUnitsMultiplier = WWMath.SQUARE_METERS_TO_ACRES;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_ACRE;
        }
        else if (areaUnits.equals(UnitsFormat.SQUARE_YARDS))
        {
            this.areaUnitsMultiplier = WWMath.SQUARE_METERS_TO_SQUARE_YARDS;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_SQUARE_YARDS;
        }
        else if (areaUnits.equals(UnitsFormat.SQUARE_FEET))
        {
            this.areaUnitsMultiplier = WWMath.SQUARE_METERS_TO_SQUARE_FEET;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_SQUARE_FEET;
        }
        else
        {
            this.areaUnitsMultiplier = 1d;
            this.areaUnitsSymbol = UnitsFormat.SYMBOL_SQUARE_METERS;
        }
    }

    /**
     * Indicates the multiplier used to convert from square meters to the current area units.
     *
     * @return the conversion multiplier to convert square meters to the current area units.
     */
    public double getAreaUnitsMultiplier()
    {
        return this.areaUnitsMultiplier;
    }

    /**
     * Returns the units symbol for the current area units. Examples are "m\u00b2" for square meters and "miles\u00b2"
     * for square miles.
     *
     * @return the units symbol for the current area units.
     */
    public String getAreaUnitsSymbol()
    {
        return this.areaUnitsSymbol;
    }

    /**
     * Sets the length and area units to those common for a given units system. Recognized systems are {@link
     * #METRIC_SYSTEM}, which uses kilometers and square kilometers, and {@link #IMPERIAL_SYSTEM}, which uses miles and
     * square miles.
     *
     * @param unitsSystem the desired units system, either <code>METRIC_SYSTEM</code> or <code>IMPERIAL_SYSTEM</code>.
     *
     * @throws IllegalArgumentException if <code>unitsSystem</code> is null.
     */
    public void setUnitsSystem(String unitsSystem)
    {
        if (unitsSystem == null)
        {
            String msg = Logging.getMessage("nullValue.UnitsSystem");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (unitsSystem.equals(UnitsFormat.IMPERIAL_SYSTEM))
        {
            this.setLengthUnits(UnitsFormat.MILES);
            this.setAltitudeUnits(UnitsFormat.MILES);
            this.setAreaUnits(UnitsFormat.SQUARE_MILES);
        }
        else
        {
            this.setLengthUnits(UnitsFormat.KILOMETERS);
            this.setAltitudeUnits(UnitsFormat.KILOMETERS);
            this.setAreaUnits(UnitsFormat.SQUARE_KILOMETERS);
        }
    }

    /**
     * Indicates the unit system of the current length units. The available systems are {@link #METRIC_SYSTEM} and
     * {@link #IMPERIAL_SYSTEM}.
     *
     * @return the current units system for lengths.
     */
    public String getLengthUnitsSystem()
    {
        if (this.getLengthUnits().equals(UnitsFormat.METERS)
            || this.getLengthUnits().equals(UnitsFormat.KILOMETERS))
            return UnitsFormat.METRIC_SYSTEM;
        else
            return UnitsFormat.IMPERIAL_SYSTEM;
    }

    /**
     * Indicates the unit system of the current area units. The available systems are {@link #METRIC_SYSTEM} and {@link
     * #IMPERIAL_SYSTEM}.
     *
     * @return the current units system for areas.
     */
    public String getAreaUnitsSystem()
    {
        if (this.getAreaUnits().equals(UnitsFormat.SQUARE_METERS)
            || this.getAreaUnits().equals(UnitsFormat.SQUARE_KILOMETERS)
            || this.getAreaUnits().equals(UnitsFormat.HECTARE)
            )
            return UnitsFormat.METRIC_SYSTEM;
        else
            return UnitsFormat.IMPERIAL_SYSTEM;
    }

    /**
     * Format an angle of latitude and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_LATITUDE} and angle format.
     *
     * @param angle the angle to format.
     *
     * @return a string containing the formatted angle and ending with the new-line character.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String latitudeNL(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.angleNL(this.getLabel(LABEL_LATITUDE), angle);
    }

    /**
     * Format an angle of latitude.
     * <p/>
     * The value is formatted using the current {@link #LABEL_LATITUDE} and angle format.
     *
     * @param angle the angle to format.
     *
     * @return a string containing the formatted angle.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String latitude(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.angle(this.getLabel(LABEL_LATITUDE), angle);
    }

    /**
     * Format an angle of longitude and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_LONGITUDE} and angle format.
     *
     * @param angle the angle to format.
     *
     * @return a string containing the formatted angle and ending with the new-line character.
     */
    public String longitudeNL(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.angleNL(this.getLabel(LABEL_LONGITUDE), angle);
    }

    /**
     * Format an angle of longitude.
     * <p/>
     * The value is formatted using the current {@link #LABEL_LONGITUDE} and angle format.
     *
     * @param angle the angle to format.
     *
     * @return a string containing the formatted angle.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String longitude(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.angle(this.getLabel(LABEL_LONGITUDE), angle);
    }

    /**
     * Format an angle of heading according to the current angle format, and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_HEADING} and angle format.
     *
     * @param angle the heading angle to format.
     *
     * @return a string containing the formatted angle and ending with the new-line character.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String headingNL(Angle angle)
    {
        return this.angleNL(this.getLabel(LABEL_HEADING), angle);
    }

    /**
     * Format an angle of heading according to the current angle format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_HEADING} and angle format.
     *
     * @param angle the heading angle to format.
     *
     * @return a string containing the formatted angle.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String heading(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.angle(this.getLabel(LABEL_HEADING), angle);
    }

    /**
     * Format an angle of heading in degrees according to the current angle format, and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_HEADING} and {@link #FORMAT_HEADING}. The default
     * <code>FORMAT_HEADING</code> is " %9.2f\u00b0".
     *
     * @param heading the angle to format.
     *
     * @return a string containing the formatted angle and ending with the new-line character.
     */
    public String headingNL(double heading)
    {
        return this.heading(heading) + NL;
    }

    /**
     * Format an angle of heading in degrees according to the current angle format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_HEADING} and {@link #FORMAT_HEADING}. The default
     * <code>FORMAT_HEADING</code> is " %9.2f\u00b0".
     *
     * @param heading the angle to format.
     *
     * @return a string containing the formatted angle.
     */
    public String heading(double heading)
    {
        return String.format(this.getLabel(LABEL_HEADING) + this.getFormat(FORMAT_HEADING), heading);
    }

    /**
     * Format angles of latitude and longitude according to the current angle format, and append a new-line character.
     * <p/>
     * The values are formatted using the current {@link #LABEL_LATLON_LAT}, {@link #LABEL_LATLON_LON} and angle
     * format.
     *
     * @param latlon the angles to format.
     *
     * @return a string containing the formatted angles and ending with the new-line character.
     *
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public String latLonNL(LatLon latlon)
    {
        if (latlon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.latLon(latlon) + NL;
    }

    /**
     * Format angles of latitude and longitude according to the current angle format.
     * <p/>
     * The values are formatted using the current {@link #LABEL_LATLON_LAT}, {@link #LABEL_LATLON_LON} and angle
     * format.
     *
     * @param latlon the angles to format.
     *
     * @return a string containing the formatted angles.
     *
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public String latLon(LatLon latlon)
    {
        if (latlon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return String.format("%s %s", this.angle(this.getLabel(LABEL_LATLON_LAT), latlon.getLatitude()),
            this.angle(this.getLabel(LABEL_LATLON_LON), latlon.getLongitude())).trim();
    }

    /**
     * Format angles with {@link #latLon2(gov.nasa.worldwind.geom.LatLon)} and append a new-line character.
     *
     * @param latlon the angles to format.
     *
     * @return a string containing the formatted angles and ending with the new-line character.
     *
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public String latLon2NL(LatLon latlon)
    {
        if (latlon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.latLon2(latlon) + NL;
    }

    /**
     * Format angles of latitude and longitude according to the current angle format and in the form "20\u00B0N
     * 85\u00B0S".
     *
     * @param latlon the angles to format.
     *
     * @return a string containing the formatted angles.
     *
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public String latLon2(LatLon latlon)
    {
        if (latlon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String latAngle = this.angle("", Angle.fromDegrees(Math.abs(latlon.getLatitude().degrees)));
        String latString = String.format("%s%s", latAngle, latlon.getLatitude().degrees >= 0 ? "N" : "S");

        String lonAngle = this.angle("", Angle.fromDegrees(Math.abs(latlon.getLongitude().degrees)));
        String lonString = String.format("%s%s", lonAngle, latlon.getLongitude().degrees >= 0 ? "E" : "W");

        return String.format("%s %s", latString, lonString);
    }

    /**
     * Format an angle according to the current angle format. Prepend a specified label and append a new-line
     * character.
     *
     * @param label a label to prepend to the formatted angle. May be null to indicate no label.
     * @param angle the angle to format.
     *
     * @return a string containing the formatted angle prepended with the specified label and ending with the new-line
     *         character.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String angleNL(String label, Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.angle(label, angle) + NL;
    }

    /**
     * Format an angle according to the current angle format. Prepend a specified label.
     *
     * @param label a label to prepend to the formatted angle. May be null to indicate no label.
     * @param angle the angle to format.
     *
     * @return a string containing the formatted angle prepended with the specified label.
     *
     * @throws IllegalArgumentException if the angle is null.
     */
    public String angle(String label, Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String s;
        if (this.isShowDMS())
            s = String.format("%s", angle.toFormattedDMSString()).trim();
        else
            s = String.format(this.getFormat(FORMAT_DECIMAL_DEGREES), angle.degrees).trim();

        return label != null ? label + " " + s : s;
    }

    /**
     * Format an eye altitude according to the current eye altitude format, and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_EYE_ALTITUDE}, {@link #FORMAT_EYE_ALTITUDE} and length
     * format.
     *
     * @param metersAltitude the eye altitude to format, in meters.
     *
     * @return a string containing the formatted eye altitude and ending with the new-line character.
     */
    public String eyeAltitudeNL(double metersAltitude)
    {
        return this.eyeAltitude(metersAltitude) + NL;
    }

    /**
     * Format an eye altitude according to the current eye altitude format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_EYE_ALTITUDE}, {@link #FORMAT_EYE_ALTITUDE} and length
     * format.
     *
     * @param metersAltitude the eye altitude to format, in meters.
     *
     * @return a string containing the formatted eye altitude.
     */
    public String eyeAltitude(double metersAltitude)
    {
        if (this.getFormat(FORMAT_EYE_ALTITUDE).contains("f"))
            return String.format(this.getLabel(LABEL_EYE_ALTITUDE) + this.getFormat(FORMAT_EYE_ALTITUDE),
                metersAltitude * this.getAltitudeUnitsMultiplier(),
                this.getAltitudeUnitsSymbol());
        else
            return String.format(this.getLabel(LABEL_EYE_ALTITUDE) + this.getFormat(FORMAT_EYE_ALTITUDE),
                (int) Math.round(metersAltitude * this.getAltitudeUnitsMultiplier()),
                this.getAltitudeUnitsSymbol());
    }

    /**
     * Format an angle of pitch according to the current angle format, and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_PITCH} and {@link #FORMAT_PITCH}. The default
     * <code>FORMAT_PITCH</code> is " %9.2f\u00b0".
     *
     * @param pitch the angle to format.
     *
     * @return a string containing the formatted angle and ending with the new-line character.
     */
    public String pitchNL(double pitch)
    {
        return this.pitch(pitch) + NL;
    }

    /**
     * Format an angle of pitch according to the current angle format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_PITCH} and {@link #FORMAT_PITCH}. The default
     * <code>FORMAT_PITCH</code> is " %9.2f\u00b0".
     *
     * @param pitch the angle to format.
     *
     * @return a string containing the formatted angle.
     */
    public String pitch(double pitch)
    {
        return String.format(this.getLabel(LABEL_PITCH) + this.getFormat(FORMAT_PITCH), pitch);
    }

    /**
     * Format a UTM zone according to the current UTM zone format and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_UTM_ZONE}.
     *
     * @param zone the UTM zone to format.
     *
     * @return the formatted UTM zone ending with a new-line character.
     */
    public String utmZoneNL(int zone)
    {
        return this.utmZone(zone) + NL;
    }

    /**
     * Format a UTM zone according to the current UTM zone format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_UTM_ZONE}.
     *
     * @param zone the UTM zone to format.
     *
     * @return the formatted UTM zone.
     */
    public String utmZone(int zone)
    {
        return String.format(this.getLabel(LABEL_UTM_ZONE) + " %2d", zone);
    }

    /**
     * Format a UTM northing value according to the current UTM northing format and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_UTM_NORTHING} and current {@link #FORMAT_UTM_NORTHING}.
     * The default UTM northing format is " %,11.1f". No units symbol is included in the formatted string because UTM
     * northing units are always meters.
     *
     * @param northing the UTM northing to format.
     *
     * @return the formatted UTM northing ending with a new-line character.
     */
    public String utmNorthingNL(double northing)
    {
        return this.utmNorthing(northing) + NL;
    }

    /**
     * Format a UTM northing value according to the current UTM northing format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_UTM_NORTHING} and current {@link #FORMAT_UTM_NORTHING}.
     * The default UTM northing format is " %,11.1f". No units symbol is included in the formatted string because UTM
     * northing units are always meters.
     *
     * @param northing the UTM northing to format.
     *
     * @return the formatted UTM northing.
     */
    public String utmNorthing(double northing)
    {
        return String.format(this.getLabel(LABEL_UTM_NORTHING) + this.getFormat(FORMAT_UTM_NORTHING), northing);
    }

    /**
     * Format a UTM easting value according to the current UTM easting format and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_UTM_EASTING} and current {@link #FORMAT_UTM_EASTING}. The
     * default UTM easting format is " %,11.1f". No units symbol is included in the formatted string because UTM easting
     * units are always meters.
     *
     * @param easting the UTM northing to format.
     *
     * @return the formatted UTM easting ending with a new-line character.
     */
    public String utmEastingNL(double easting)
    {
        return this.utmEasting(easting) + NL;
    }

    /**
     * Format a UTM easting value according to the current UTM easting format.
     * <p/>
     * The value is formatted using the current {@link #LABEL_UTM_EASTING} and current {@link #FORMAT_UTM_EASTING}. The
     * default UTM easting format is " %,11.1f". No units symbol is included in the formatted string because UTM easting
     * units are always meters.
     *
     * @param easting the UTM northing to format.
     *
     * @return the formatted UTM easting.
     */
    public String utmEasting(double easting)
    {
        return String.format(this.getLabel(LABEL_UTM_EASTING) + this.getFormat(FORMAT_UTM_EASTING), easting);
    }

    /**
     * Format a terrain height value according to the current configuration and append a new-line character. See {@link
     * #terrainHeight(double, double)} for a description of the formatting.
     *
     * @param metersElevation      the terrain height in meters.
     * @param verticalExaggeration the vertical exaggeration to apply to the terrain height.
     *
     * @return the formatted terrain height ending with a new-line character.
     */
    public String terrainHeightNL(double metersElevation, double verticalExaggeration)
    {
        return this.terrainHeight(metersElevation, verticalExaggeration) + NL;
    }

    /**
     * Format a terrain height value according to the current configuration and append a new-line character.
     * <p/>
     * The value is formatted using the current {@link #LABEL_TERRAIN_HEIGHT}, {@link #FORMAT_TERRAIN_HEIGHT} and length
     * units symbol. The default terrain height format is " (ve %3.1f): %,6d %s", where the %3.1f specifier stands for
     * the vertical exaggeration, the %,6d specifier stands for the terrain height, and the %s specifier stands for the
     * units symbol.
     * <p/>
     * Note: While the <code>FORMAT_TERRAIN_HEIGHT</code> string may be specified by the application, the terrain height
     * components are always passed to the internal formatter in the order: vertical exaggeration, terrain height, units
     * symbol.
     *
     * @param metersElevation      the terrain height in meters.
     * @param verticalExaggeration the vertical exaggeration to apply to the terrain height.
     *
     * @return the formatted terrain height ending with a new-line character.
     */
    public String terrainHeight(double metersElevation, double verticalExaggeration)
    {
        double multiplier;
        String symbol;

        if (this.getLengthUnitsSystem().equals(UnitsFormat.METRIC_SYSTEM))
        {
            multiplier = 1d;
            symbol = UnitsFormat.SYMBOL_METERS;
        }
        else
        {
            multiplier = WWMath.METERS_TO_FEET;
            symbol = UnitsFormat.SYMBOL_FEET;
        }

        return String.format(this.getLabel(LABEL_TERRAIN_HEIGHT) + getFormat(FORMAT_TERRAIN_HEIGHT),
            verticalExaggeration, (int) Math.round((metersElevation / verticalExaggeration) * multiplier), symbol);
    }

    /**
     * Format a length according to the current length configuration. Prepend a specified label and append a new-line
     * character.
     * <p/>
     * The value is formatted using the current {@link #FORMAT_LENGTH} and length units symbol,  and is converted to the
     * current length units prior to formatting. The default length format is " %,12.1f %s", where the %s specifier
     * stands for the units symbol.
     *
     * @param label  the label to prepend to the formatted length. May be null to indicate no label.
     * @param meters the length to format, in meters.
     *
     * @return the formatted length with the specified label prepended and a new-line character appended.
     */
    public String lengthNL(String label, double meters)
    {
        return this.length(label, meters) + NL;
    }

    /**
     * Format a length according to the current length configuration. Prepend a specified label.
     * <p/>
     * The value is formatted using the current {@link #FORMAT_LENGTH} and length units symbol,  and is converted to the
     * current length units prior to formatting. The default length format is " %,12.1f %s", where the %s specifier
     * stands for the units symbol.
     *
     * @param label  the label to prepend to the formatted length. May be null to indicate no label.
     * @param meters the length to format, in meters.
     *
     * @return the formatted length with the specified label prepended.
     */
    public String length(String label, double meters)
    {
        String s = String.format(this.getFormat(FORMAT_LENGTH), meters * this.getLengthUnitsMultiplier(),
            this.getLengthUnitsSymbol()).trim();

        return label != null ? label + " " + s : s;
    }

    /**
     * Format an area value according to the current length configuration. Prepend a specified label and append a
     * new-line character.
     * <p/>
     * The value is formatted using the current {@link #FORMAT_AREA} and area units symbol,  and is converted to the
     * current area units prior to formatting. The default area format is " %,12.1f %s", where the %s specifier stands
     * for the units symbol.
     *
     * @param label        the label to prepend to the formatted length. May be null to indicate no label.
     * @param squareMeters the area value to format, in square meters.
     *
     * @return the formatted area with the specified label prepended and a new-line character appended.
     */
    public String areaNL(String label, double squareMeters)
    {
        return this.area(label, squareMeters) + NL;
    }

    /**
     * Format an area value according to the current length configuration and prepend a specified label.
     * <p/>
     * The value is formatted using the current {@link #FORMAT_AREA} and area units symbol,  and is converted to the
     * current area units prior to formatting. The default area format is " %,12.1f %s", where the %s specifier stands
     * for the units symbol.
     *
     * @param label        the label to prepend to the formatted length. May be null to indicate no label.
     * @param squareMeters the area value to format, in square meters.
     *
     * @return the formatted area with the specified label prepended.
     */
    public String area(String label, double squareMeters)
    {
        String s = String.format(this.getFormat(FORMAT_AREA), squareMeters * this.getAreaUnitsMultiplier(),
            this.getAreaUnitsSymbol()).trim();

        return label != null ? label + " " + s : s;
    }
}
