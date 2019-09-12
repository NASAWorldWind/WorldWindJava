/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.UTMCoord;

import java.awt.*;
import java.lang.reflect.*;
import java.nio.*;
import java.text.*;
import java.util.regex.Pattern;

/**
 * @author tag
 * @version $Id: WWUtil.java 2396 2014-10-27 23:46:42Z tgaskins $
 */
public class WWUtil
{
    /**
     * Converts a specified string to an integer value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return integer value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Integer convertStringToInteger(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            return Integer.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", s);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Converts a specified string to a floating point value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return floating point value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Double convertStringToDouble(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", s);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Converts a specified string to a long integer value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return long integer value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Long convertStringToLong(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            return Long.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", s);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Converts a specified string to a boolean value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return boolean value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Boolean convertStringToBoolean(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            s = s.trim();

            if (s.length() == 0)
                return null;

            if (s.length() == 1)
                return convertNumericStringToBoolean(s);

            return Boolean.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", s);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Converts a specified string to a boolean value. Returns null if the string cannot be converted.
     *
     * @param s the string to convert.
     *
     * @return boolean value of the string, or null if the string cannot be converted.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static Boolean convertNumericStringToBoolean(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            if (s.length() == 0)
            {
                return null;
            }

            Integer i = makeInteger(s);
            return i != null && i != 0;
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", s);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }
    }

    /**
     * Parses a string to an integer value if the string can be parsed as a integer. Does not log a message if the
     * string can not be parsed as an integer.
     *
     * @param s the string to parse.
     *
     * @return the integer value parsed from the string, or null if the string cannot be parsed as an integer.
     */
    public static Integer makeInteger(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a long value if the string can be parsed as a long. Does not log a message if the string can
     * not be parsed as a long.
     *
     * @param s the string to parse.
     *
     * @return the long value parsed from the string, or null if the string cannot be parsed as a long.
     */
    public static Long makeLong(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Long.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a double value using the current locale if the string can be parsed as a double. Does not log
     * a message if the string can not be parsed as a double.
     *
     * @param s the string to parse.
     *
     * @return the double value parsed from the string, or null if the string cannot be parsed as a double.
     */
    public static Double makeDoubleForLocale(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return NumberFormat.getInstance().parse(s.trim()).doubleValue();
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    /**
     * Parses a string to a double value if the string can be parsed as a double. Does not log a message if the string
     * can not be parsed as a double.
     *
     * @param s the string to parse.
     *
     * @return the double value parsed from the string, or null if the string cannot be parsed as a double.
     */
    public static Double makeDouble(String s)
    {
        if (WWUtil.isEmpty(s))
        {
            return null;
        }

        try
        {
            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Returns a sub sequence of the specified {@link CharSequence}, with leading and trailing whitespace omitted. If
     * the CharSequence has length zero, this returns a reference to the CharSequence. If the CharSequence represents
     * and empty character sequence, this returns an empty CharSequence.
     *
     * @param charSequence the CharSequence to trim.
     *
     * @return a sub sequence with leading and trailing whitespace omitted.
     *
     * @throws IllegalArgumentException if the charSequence is null.
     */
    public static CharSequence trimCharSequence(CharSequence charSequence)
    {
        if (charSequence == null)
        {
            String message = Logging.getMessage("nullValue.CharSequenceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int len = charSequence.length();
        if (len == 0)
        {
            return charSequence;
        }

        int start, end;

        for (start = 0; (start < len) && charSequence.charAt(start) == ' '; start++)
        {
        }

        for (end = charSequence.length() - 1; (end > start) && charSequence.charAt(end) == ' '; end--)
        {
        }

        return charSequence.subSequence(start, end + 1);
    }

    public static void alignComponent(Component parent, Component child, String alignment)
    {
        Dimension prefSize = child.getPreferredSize();
        java.awt.Point parentLocation = parent != null ? parent.getLocation() : new java.awt.Point(0, 0);
        Dimension parentSize = parent != null ? parent.getSize() : Toolkit.getDefaultToolkit().getScreenSize();

        int x = parentLocation.x;
        int y = parentLocation.y;

        if (alignment != null && alignment.equals(AVKey.RIGHT))
        {
            x += parentSize.width - 50;
            y += parentSize.height - prefSize.height;
        }
        else if (alignment != null && alignment.equals(AVKey.CENTER))
        {
            x += (parentSize.width - prefSize.width) / 2;
            y += (parentSize.height - prefSize.height) / 2;
        }
        else if (alignment != null && alignment.equals(AVKey.LEFT_OF_CENTER))
        {
            x += parentSize.width / 2 - 1.05 * prefSize.width;
            y += (parentSize.height - prefSize.height) / 2;
        }
        else if (alignment != null && alignment.equals(AVKey.RIGHT_OF_CENTER))
        {
            x += parentSize.width / 2 + 0.05 * prefSize.width;
            y += (parentSize.height - prefSize.height) / 2;
        }
        // else it's left aligned by default

        child.setLocation(x, y);
    }

    /**
     * Generates a random {@link Color} by scaling each of the red, green and blue components of a specified color with
     * independent random numbers. The alpha component is not scaled and is copied to the new color. The returned color
     * can be any value between white (0x000000aa) and black (0xffffffaa).
     * <p>
     * Unless there's a reason to use a specific input color, the best color to use is white.
     *
     * @param color the color to generate a random color from. If null, the color white (0x000000aa) is used.
     *
     * @return a new color with random red, green and blue components.
     */
    public static Color makeRandomColor(Color color)
    {
        if (color == null)
        {
            color = Color.WHITE;
        }

        float[] cc = color.getRGBComponents(null);

        return new Color(cc[0] * (float) Math.random(), cc[1] * (float) Math.random(), cc[2] * (float) Math.random(),
            cc[3]);
    }

    /**
     * Generates a random {@link Color} by scaling each of the red, green and blue components of a specified color with
     * independent random numbers. The alpha component is not scaled and is copied to the new color. The returned color
     * can be any value between white (0x000000aa) and a specified darkest color.
     * <p>
     * Unless there's a reason to use a specific input color, the best color to use is white.
     *
     * @param color        the color to generate a random color from. If null, the color white (0x000000aa) is used.
     * @param darkestColor the darkest color allowed. If any of the generated color's components are less than the
     *                     corresponding component in this color, new colors are generated until one satisfies this
     *                     requirement, up to the specified maximum number of attempts.
     * @param maxAttempts  the maximum number of attempts to create a color lighter than the specified darkestColor. If
     *                     this limit is reached, the last color generated is returned.
     *
     * @return a new color with random red, green and blue components.
     */
    public static Color makeRandomColor(Color color, Color darkestColor, int maxAttempts)
    {
        Color randomColor = makeRandomColor(color);

        if (darkestColor == null)
        {
            return randomColor;
        }

        float[] dc = darkestColor.getRGBComponents(null);

        float[] rc = randomColor.getRGBComponents(null);
        for (int i = 0; i < (maxAttempts - 1) && (rc[0] < dc[0] || rc[1] < dc[1] || rc[2] < dc[2]); i++)
        {
            rc = randomColor.getRGBComponents(null);
        }

        return randomColor;
    }

    public static Color makeColorBrighter(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float[] hsbComponents = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
        float hue = hsbComponents[0];
        float saturation = hsbComponents[1];
        float brightness = hsbComponents[2];

        saturation /= 3f;
        brightness *= 3f;

        if (saturation < 0f)
        {
            saturation = 0f;
        }

        if (brightness > 1f)
        {
            brightness = 1f;
        }

        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);

        return new Color(rgbInt);
    }

    public static Color makeColorDarker(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float[] hsbComponents = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbComponents);
        float hue = hsbComponents[0];
        float saturation = hsbComponents[1];
        float brightness = hsbComponents[2];

        saturation *= 3f;
        brightness /= 3f;

        if (saturation > 1f)
        {
            saturation = 1f;
        }

        if (brightness < 0f)
        {
            brightness = 0f;
        }

        int rgbInt = Color.HSBtoRGB(hue, saturation, brightness);

        return new Color(rgbInt);
    }

    public static Color computeContrastingColor(Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float[] compArray = new float[4];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        int colorValue = compArray[2] < 0.5f ? 255 : 0;
        int alphaValue = color.getAlpha();

        return new Color(colorValue, colorValue, colorValue, alphaValue);
    }

    /**
     * Returns the component-wise linear interpolation of <code>color1</code> and <code>color2</code>. Each of the RGBA
     * components in the colors are interpolated according to the function: <code>(1 - amount) * c1 + amount *
     * c2</code>, where c1 and c2 are components of <code>color1</code> and <code>color2</code>, respectively. The
     * interpolation factor <code>amount</code> defines the weight given to each value, and is clamped to the range [0,
     * 1].
     *
     * @param amount the interpolation factor.
     * @param color1 the first color.
     * @param color2 the second color.
     *
     * @return this returns the linear interpolation of <code>color1</code> and <code>color2</code> if &lt;amount&gt; is
     *         between 0 and 1, a color equivalent to color1 if <code>amount</code> is 0 or less, or a color equivalent
     *         to <code>color2</code> if <code>amount</code> is 1 or more.
     *
     * @throws IllegalArgumentException if either <code>color1</code> or <code>color2</code> are <code>null</code>.
     */
    public static Color interpolateColor(double amount, Color color1, Color color2)
    {
        if (color1 == null || color2 == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float t = (amount < 0 ? 0 : (amount > 1 ? 1 : (float) amount));
        float r = color1.getRed() + t * (color2.getRed() - color1.getRed());
        float g = color1.getGreen() + t * (color2.getGreen() - color1.getGreen());
        float b = color1.getBlue() + t * (color2.getBlue() - color1.getBlue());
        float a = color1.getAlpha() + t * (color2.getAlpha() - color1.getAlpha());

        return new Color(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    /**
     * Creates a hexadecimal string representation of a {@link Color} in the form 0xrrggbbaa.
     *
     * @param color Color to encode.
     *
     * @return String encoding of the specified color.
     *
     * @throws IllegalArgumentException If the specified color is null.
     * @see #decodeColorRGBA(String)
     * @see #encodeColorABGR(java.awt.Color)
     */
    public static String encodeColorRGBA(java.awt.Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Encode the red, green, blue, and alpha components
        int rgba = (color.getRed() & 0xFF) << 24
            | (color.getGreen() & 0xFF) << 16
            | (color.getBlue() & 0xFF) << 8
            | (color.getAlpha() & 0xFF);
        return String.format("%#08X", rgba);
    }

    /**
     * Creates a hexadecimal string representation of a {@link Color} in the form 0xaabbggrr.
     *
     * @param color Color to encode.
     *
     * @return String encoding of the specified color.
     *
     * @throws IllegalArgumentException If the specified color is null.
     * @see #decodeColorABGR(String)
     * @see #encodeColorRGBA(java.awt.Color)
     */
    public static String encodeColorABGR(java.awt.Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Encode the red, green, blue, and alpha components
        int rgba = (color.getRed() & 0xFF)
            | (color.getGreen() & 0xFF) << 8
            | (color.getBlue() & 0xFF) << 16
            | (color.getAlpha() & 0xFF) << 24;
        return String.format("%#08X", rgba);
    }

    /**
     * Decodes a hexadecimal string in the form <i>rrggbbaa</i>, <i>rrggbbaa</i> or <i>#rrggbbaa</i> to a color.
     *
     * @param encodedString String to decode.
     *
     * @return the decoded color, or null if the string cannot be decoded.
     *
     * @throws IllegalArgumentException If the specified string is null.
     * @see #decodeColorABGR(String) (String)
     * @see #encodeColorRGBA(java.awt.Color)
     */
    public static java.awt.Color decodeColorRGBA(String encodedString)
    {
        if (encodedString == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (encodedString.startsWith("#"))
        {
            encodedString = encodedString.replaceFirst("#", "0x");
        }
        else if (!encodedString.startsWith("0x") && !encodedString.startsWith("0X"))
        {
            encodedString = "0x" + encodedString;
        }

        // The hexadecimal representation for an RGBA color can result in a value larger than
        // Integer.MAX_VALUE (for example, 0XFFFF). Therefore we decode the string as a long,
        // then keep only the lower four bytes.
        Long longValue;
        try
        {
            longValue = Long.parseLong(encodedString.substring(2), 16);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", encodedString);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }

        int i = (int) (longValue & 0xFFFFFFFFL);
        return new java.awt.Color(
            (i >> 24) & 0xFF,
            (i >> 16) & 0xFF,
            (i >> 8) & 0xFF,
            i & 0xFF);
    }

    /**
     * Decodes a hexadecimal string in the form <i>aabbggrr</i>, <i>0xaabbggrr</i> or <i>#aabbggrr</i> to a color.
     *
     * @param encodedString String to decode.
     *
     * @return the decoded color, or null if the string cannot be decoded.
     *
     * @throws IllegalArgumentException If the specified string is null.
     * @see #decodeColorRGBA(String)
     * @see #encodeColorABGR(java.awt.Color)
     */
    public static java.awt.Color decodeColorABGR(String encodedString)
    {
        if (encodedString == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (encodedString.startsWith("#"))
        {
            encodedString = encodedString.replaceFirst("#", "0x");
        }
        else if (!encodedString.startsWith("0x") && !encodedString.startsWith("0X"))
        {
            encodedString = "0x" + encodedString;
        }

        // The hexadecimal representation for an RGBA color can result in a value larger than
        // Integer.MAX_VALUE (for example, 0XFFFF). Therefore we decode the string as a long,
        // then keep only the lower four bytes.
        Long longValue;
        try
        {
            longValue = Long.parseLong(encodedString.substring(2), 16);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", encodedString);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            return null;
        }

        int i = (int) (longValue & 0xFFFFFFFFL);
        return new java.awt.Color(
            i & 0xFF,
            (i >> 8) & 0xFF,
            (i >> 16) & 0xFF,
            (i >> 24) & 0xFF);
    }

    /**
     * Determine whether an object reference is null or a reference to an empty string.
     *
     * @param s the reference to examine.
     *
     * @return true if the reference is null or is a zero-length {@link String}.
     */
    public static boolean isEmpty(Object s)
    {
        return s == null || (s instanceof String && ((String) s).length() == 0);
    }

    /**
     * Determine whether an {@link List} is null or empty.
     *
     * @param list the list to examine.
     *
     * @return true if the list is null or zero-length.
     */
    public static boolean isEmpty(java.util.List<?> list)
    {
        return list == null || list.size() == 0;
    }

    /**
     * Creates a two-element array of default min and max values, typically used to initialize extreme values searches.
     *
     * @return a two-element array of extreme values. Entry 0 is the maximum double value; entry 1 is the negative of
     *         the maximum double value;
     */
    public static double[] defaultMinMix()
    {
        return new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
    }

    /**
     * Converts the specified buffer of UTM tuples to geographic coordinates, according to the specified UTM zone and
     * hemisphere. The buffer must be organized as pairs of tightly packed UTM tuples in the order <code>(easting,
     * northing)</code>. Each UTM tuple is replaced with its corresponding geographic location in the order
     * <code>(longitude, latitude)</code>. Geographic locations are expressed in degrees. Tuples are replaced starting
     * at the buffer's position and ending at its limit.
     *
     * @param zone       the UTM zone.
     * @param hemisphere the UTM hemisphere, either {@link gov.nasa.worldwind.avlist.AVKey#NORTH} or {@link
     *                   gov.nasa.worldwind.avlist.AVKey#SOUTH}.
     * @param buffer     the buffer of UTM tuples to convert.
     *
     * @throws IllegalArgumentException if <code>zone</code> is outside the range 1-60, if <code>hemisphere</code> is
     *                                  null, if <code>hemisphere</code> is not one of {@link gov.nasa.worldwind.avlist.AVKey#NORTH}
     *                                  or {@link gov.nasa.worldwind.avlist.AVKey#SOUTH}, if <code>buffer</code> is
     *                                  null, or if the number of remaining elements in <code>buffer</code> is not a
     *                                  multiple of two.
     */
    public static void convertUTMCoordinatesToGeographic(int zone, String hemisphere, DoubleBuffer buffer)
    {
        if (zone < 1 || zone > 60)
        {
            String message = Logging.getMessage("generic.ZoneIsInvalid", zone);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!AVKey.NORTH.equals(hemisphere) && !AVKey.SOUTH.equals(hemisphere))
        {
            String message = Logging.getMessage("generic.HemisphereIsInvalid", hemisphere);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if ((buffer.remaining() % 2) != 0)
        {
            String message = Logging.getMessage("generic.BufferSize", buffer.remaining());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        while (buffer.hasRemaining())
        {
            buffer.mark();
            double easting = buffer.get();
            double northing = buffer.get();

            LatLon location = UTMCoord.locationFromUTMCoord(zone, hemisphere, easting, northing, null);

            buffer.reset();
            buffer.put(location.getLongitude().degrees);
            buffer.put(location.getLatitude().degrees);
        }
    }

    /**
     * Normalizes the specified buffer of geographic tuples. The buffer must be organized as pairs of tightly packed
     * geographic tuples in the order <code>(longitude, latitude)</code>. Each geographic tuple is normalized to the
     * range +-90 latitude and +-180 longitude and replaced with its normalized values. Geographic locations are
     * expressed in degrees. Tuples are replaced starting at the buffer's position and ending at its limit.
     *
     * @param buffer the buffer of geographic tuples to convert.
     *
     * @throws IllegalArgumentException if <code>buffer</code> is null, or if the number of remaining elements in
     *                                  <code>buffer</code> is not a multiple of two.
     */
    public static void normalizeGeographicCoordinates(DoubleBuffer buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if ((buffer.remaining() % 2) != 0)
        {
            String message = Logging.getMessage("generic.BufferSize", buffer.remaining());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        while (buffer.hasRemaining())
        {
            buffer.mark();
            Angle lon = Angle.fromDegrees(buffer.get());
            Angle lat = Angle.fromDegrees(buffer.get());

            buffer.reset();
            buffer.put(Angle.normalizedLongitude(lon).degrees);
            buffer.put(Angle.normalizedLatitude(lat).degrees);
        }
    }

    /**
     * Uses reflection to invoke a <i>set</i> method for a specified property. The specified class must have a method
     * named "set" + propertyName, with either a single <code>String</code> argument, a single <code>double</code>
     * argument, a single <code>int</code> argument or a single <code>long</code> argument. If it does, the method is
     * called with the specified property value argument.
     *
     * @param parent        the object on which to set the property.
     * @param propertyName  the name of the property.
     * @param propertyValue the value to give the property. Specify double, int and long values in a
     *                      <code>String</code>.
     *
     * @return the return value of the <i>set</i> method, or null if the method has no return value.
     *
     * @throws IllegalArgumentException  if the parent object or the property name is null.
     * @throws NoSuchMethodException     if no <i>set</i> method exists for the property name.
     * @throws InvocationTargetException if the <i>set</i> method throws an exception.
     * @throws IllegalAccessException    if the <i>set</i> method is inaccessible due to access control.
     */
    public static Object invokePropertyMethod(Object parent, String propertyName, String propertyValue)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        if (parent == null)
        {
            String message = Logging.getMessage("nullValue.nullValue.ParentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (propertyName == null)
        {
            String message = Logging.getMessage("nullValue.PropertyNameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String methodName = "set" + propertyName;

        try // String arg
        {
            Method method = parent.getClass().getMethod(methodName, new Class[] {String.class});
            return method != null ? method.invoke(parent, propertyValue) : null;
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // double arg
        {
            Double d = WWUtil.makeDouble(propertyValue);
            if (d != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {double.class});
                return method != null ? method.invoke(parent, d) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // int arg
        {
            Integer i = WWUtil.makeInteger(propertyValue);
            if (i != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {int.class});
                return method != null ? method.invoke(parent, i) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // boolean arg
        {
            Boolean b = WWUtil.convertStringToBoolean(propertyValue);
            if (b != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {boolean.class});
                return method != null ? method.invoke(parent, b) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        try // long arg
        {
            Long l = WWUtil.makeLong(propertyValue);
            if (l != null)
            {
                Method method = parent.getClass().getMethod(methodName, new Class[] {long.class});
                return method != null ? method.invoke(parent, l) : null;
            }
        }
        catch (NoSuchMethodException e)
        {
            // skip to next arg type
        }

        throw new NoSuchMethodException();
    }

    /**
     * Copies only values of the specified <code>keys</code >from <code>srcList</code> to another <code>destList</code>.
     * The <code>forceOverwrite</code> controls what to do if the destination list already contains values for specified
     * <code>keys</code >. If  <code>forceOverwrite</code> is set to true, the existing value wills be overwritten.
     *
     * @param srcList        The source list. May not be <code>null</code>.
     * @param destList       The destination list. May not be <code>null</code>.
     * @param forceOverwrite Allow overwrite existing values in the destination list
     * @param keys           Array of <code>keys</code >
     */
    public static void copyValues(AVList srcList, AVList destList, String[] keys, boolean forceOverwrite)
    {
        if (WWUtil.isEmpty(srcList) || WWUtil.isEmpty(destList) || WWUtil.isEmpty(keys) || keys.length == 0)
        {
            return;
        }

        for (String key : keys)
        {
            if (WWUtil.isEmpty(key) || !srcList.hasKey(key))
            {
                continue;
            }

            Object o = srcList.getValue(key);
            if (!destList.hasKey(key) || forceOverwrite)
            {
                destList.setValue(key, o);
            }
        }
    }

    /**
     * Eliminates all white space in a specified string. (Applies the regular expression "\\s+".)
     *
     * @param inputString the string to remove white space from.
     *
     * @return the string with white space eliminated, or null if the input string is null.
     */
    public static String removeWhiteSpace(String inputString)
    {
        if (WWUtil.isEmpty(inputString))
        {
            return inputString;
        }

        return inputString.replaceAll("\\s+", "");
    }

    /**
     * Extracts an error message from the exception object
     *
     * @param t Exception instance
     *
     * @return A string that contains an error message
     */
    public static String extractExceptionReason(Throwable t)
    {
        if (t == null)
        {
            return Logging.getMessage("generic.Unknown");
        }

        StringBuilder sb = new StringBuilder();

        String message = t.getMessage();
        if (!WWUtil.isEmpty(message))
            sb.append(message);

        String messageClass = t.getClass().getName();

        Throwable cause = t.getCause();
        if (null != cause && cause != t)
        {
            String causeMessage = cause.getMessage();
            String causeClass = cause.getClass().getName();

            if (!WWUtil.isEmpty(messageClass) && !WWUtil.isEmpty(causeClass) && !messageClass.equals(causeClass))
            {
                if (sb.length() != 0)
                {
                    sb.append(" : ");
                }
                sb.append(causeClass).append(" (").append(causeMessage).append(")");
            }
        }

        if (sb.length() == 0)
        {
            sb.append(messageClass);
        }

        return sb.toString();
    }

    /**
     * Strips leading period from a string (Example: input -&gt; ".ext", output -&gt; "ext")
     *
     * @param s String to test, must not be null
     *
     * @return String without leading period
     */
    public static String stripLeadingPeriod(String s)
    {
        if (null != s && s.startsWith("."))
            return s.substring(Math.min(1, s.length()), s.length());
        return s;
    }

    protected static boolean isKMLTimeShift(String timeString)
    {
        return Pattern.matches(".*[+-]+\\d\\d:\\d\\d$", timeString.trim());
    }

    /**
     * Parse a date/time string and return its equivalent in milliseconds (using same time coordinate system as
     * System.currentTimeMillis()). The following formats are recognized and conform to those defined in KML version
     * 2.2: "1997", "1997-07", "1997-07-16", "1997-07-16T07:30:15Z", "1997-07-16T07:30:15+03:00" and
     * "1997-07-16T07:30:15+0300".
     *
     * @param timeString the date/time string to parse.
     *
     * @return the number of milliseconds since 00:00:00 1970 indicated by the date/time string, or null if the input
     *         string is null or the string is not a recognizable format.
     */
    public static Long parseTimeString(String timeString)
    {
        if (timeString == null)
            return null;

        // KML allows a hybrid time zone offset that does not contain the leading "GMT", e.g. 1997-05-10T09:30:00+03:00.
        // If the time string has this pattern, we convert it to an RFC 822 time zone so that SimpleDateFormat can
        // parse it.
        if (isKMLTimeShift(timeString))
        {
            // Remove the colon from the GMT offset portion of the time string.
            timeString = timeString.trim();
            int colonPosition = timeString.length() - 3;
            String newTimeString = timeString.substring(0, colonPosition);
            timeString = newTimeString + timeString.substring(colonPosition + 1, timeString.length());
        }

        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzzzz");
            return df.parse(timeString).getTime();
        }
        catch (ParseException ignored)
        {
        }

        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            return df.parse(timeString).getTime();
        }
        catch (ParseException ignored)
        {
        }

        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            return df.parse(timeString).getTime();
        }
        catch (ParseException ignored)
        {
        }

        try
        {
            DateFormat df = new SimpleDateFormat("yyyy-MM");
            return df.parse(timeString).getTime();
        }
        catch (ParseException ignored)
        {
        }

        try
        {
            DateFormat df = new SimpleDateFormat("yyyy");
            return df.parse(timeString).getTime();
        }
        catch (ParseException ignored)
        {
        }

        return null;
    }

    /**
     * Compares two version strings, e.g., 1.3.0, and returns 0 if they match, -1 if the first string is less than the
     * second, and 1 if the first string is greater than the second. The strings can be arbitrary length but the
     * components must be separated by ".". A missing component maps to 0, e.g. 1.3 will match 1.3.0.
     *
     * @param va the first version string
     * @param vb the second version string
     *
     * @return -1 if the first string is less than the second, 0 if the strings match, 1 if the first string is greater
     *         than the second string.
     */
    public static int compareVersion(String va, String vb)
    {
        if (va == null || vb == null)
        {
            throw new IllegalArgumentException(Logging.getMessage("nullValue.StringIsNull"));
        }

        if (va.equals(vb))
            return 0;

        String[] vas = va.split("\\.");
        String[] vbs = vb.split("\\.");

        for (int i = 0; i < Math.max(vas.length, vbs.length); i++)
        {
            String sa = vas.length > i ? vas[i] : "0";
            String sb = vbs.length > i ? vbs[i] : "0";

            if (sa.compareTo(sb) < 0)
                return -1;

            if (sa.compareTo(sb) > 0)
                return 1;
        }

        return 0; // the versions match
    }

    /**
     * Generates average normal vectors for the vertices of a triangle strip.
     *
     * @param vertices the triangle strip vertices.
     * @param indices  the indices identifying the triangle strip from the specified vertices.
     * @param normals  a buffer to accept the output normals. The buffer must be allocated and all its values must be
     *                 initialized to 0. The buffer's size limit must be at least as large as that of the specified
     *                 vertex buffer.
     *
     * @throws IllegalArgumentException if any of the specified buffers are null or the limit of the normal
     *                                            buffer is less than that of the vertex buffer.
     */
    public static void generateTriStripNormals(FloatBuffer vertices, IntBuffer indices, FloatBuffer normals)
    {
        if (vertices == null || indices == null || normals == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (normals.limit() < vertices.limit())
        {
            String message = Logging.getMessage("generic.BufferSize", normals.limit());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (int i = 0; i < indices.limit() - 2; i++)
        {
            int i1 = 3 * indices.get(i);
            int i2 = 3 * indices.get(i + 1);
            int i3 = 3 * indices.get(i + 2);

            Vec4 t0 = new Vec4(vertices.get(i1), vertices.get(i1 + 1), vertices.get(i1 + 2));
            Vec4 t1 = new Vec4(vertices.get(i2), vertices.get(i2 + 1), vertices.get(i2 + 2));
            Vec4 t2 = new Vec4(vertices.get(i3), vertices.get(i3 + 1), vertices.get(i3 + 2));
            Vec4 va = new Vec4(t1.x - t0.x, t1.y - t0.y, t1.z - t0.z);
            Vec4 vb = new Vec4(t2.x - t0.x, t2.y - t0.y, t2.z - t0.z);

            Vec4 facetNormal;
            if (i % 2 == 0)
            {
                facetNormal = va.cross3(vb).normalize3();
            }
            else
            {
                facetNormal = vb.cross3(va).normalize3();
            }

            normals.put(i1, normals.get(i1) + (float) facetNormal.x);
            normals.put(i1 + 1, normals.get(i1 + 1) + (float) facetNormal.y);
            normals.put(i1 + 2, normals.get(i1 + 2) + (float) facetNormal.z);

            normals.put(i2, normals.get(i2) + (float) facetNormal.x);
            normals.put(i2 + 1, normals.get(i2 + 1) + (float) facetNormal.y);
            normals.put(i2 + 2, normals.get(i2 + 2) + (float) facetNormal.z);

            normals.put(i3, normals.get(i3) + (float) facetNormal.x);
            normals.put(i3 + 1, normals.get(i3 + 1) + (float) facetNormal.y);
            normals.put(i3 + 2, normals.get(i3 + 2) + (float) facetNormal.z);
        }

        // Normalize all the computed normals.
        for (int i = 0; i < indices.limit() - 2; i++)
        {
            int i1 = 3 * indices.get(i);
            int i2 = 3 * indices.get(i + 1);
            int i3 = 3 * indices.get(i + 2);

            Vec4 n1 = new Vec4(normals.get(i1), normals.get(i1 + 1), normals.get(i1 + 2)).normalize3();
            Vec4 n2 = new Vec4(normals.get(i2), normals.get(i2 + 1), normals.get(i2 + 2)).normalize3();
            Vec4 n3 = new Vec4(normals.get(i3), normals.get(i3 + 1), normals.get(i3 + 2)).normalize3();

            normals.put(i1, (float) n1.x);
            normals.put(i1 + 1, (float) n1.y);
            normals.put(i1 + 2, (float) n1.z);

            normals.put(i2, (float) n2.x);
            normals.put(i2 + 1, (float) n2.y);
            normals.put(i2 + 2, (float) n2.z);

            normals.put(i3, (float) n3.x);
            normals.put(i3 + 1, (float) n3.y);
            normals.put(i3 + 2, (float) n3.z);
        }
    }
}
