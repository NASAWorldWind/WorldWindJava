/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

import java.util.regex.*;

/**
 * Represents a geometric angle. Instances of <code>Angle</code> are immutable. An angle can be obtained through the
 * factory methods {@link #fromDegrees} and {@link #fromRadians}.
 *
 * @author Tom Gaskins
 * @version $Id: Angle.java 2419 2014-11-08 04:44:55Z tgaskins $
 */
public class Angle implements Comparable<Angle>
{
    // Angle format
    public final static String ANGLE_FORMAT_DD = "gov.nasa.worldwind.Geom.AngleDD";
    public final static String ANGLE_FORMAT_DM = "gov.nasa.worldwind.Geom.AngleDM";
    public final static String ANGLE_FORMAT_DMS = "gov.nasa.worldwind.Geom.AngleDMS";

    /** Represents an angle of zero degrees */
    public final static Angle ZERO = Angle.fromDegrees(0);

    /** Represents a right angle of positive 90 degrees */
    public final static Angle POS90 = Angle.fromDegrees(90);

    /** Represents a right angle of negative 90 degrees */
    public final static Angle NEG90 = Angle.fromDegrees(-90);

    /** Represents an angle of positive 180 degrees */
    public final static Angle POS180 = Angle.fromDegrees(180);

    /** Represents an angle of negative 180 degrees */
    public final static Angle NEG180 = Angle.fromDegrees(-180);

    /** Represents an angle of positive 360 degrees */
    public final static Angle POS360 = Angle.fromDegrees(360);

    /** Represents an angle of negative 360 degrees */
    public final static Angle NEG360 = Angle.fromDegrees(-360);

    /** Represents an angle of 1 minute */
    public final static Angle MINUTE = Angle.fromDegrees(1d / 60d);

    /** Represents an angle of 1 second */
    public final static Angle SECOND = Angle.fromDegrees(1d / 3600d);

    private final static double DEGREES_TO_RADIANS = Math.PI / 180d;
    private final static double RADIANS_TO_DEGREES = 180d / Math.PI;

    /**
     * Obtains an angle from a specified number of degrees.
     *
     * @param degrees the size in degrees of the angle to be obtained
     *
     * @return a new angle, whose size in degrees is given by <code>degrees</code>
     */
    public static Angle fromDegrees(double degrees)
    {
        return new Angle(degrees, DEGREES_TO_RADIANS * degrees);
    }

    /**
     * Obtains an angle from a specified number of radians.
     *
     * @param radians the size in radians of the angle to be obtained.
     *
     * @return a new angle, whose size in radians is given by <code>radians</code>.
     */
    public static Angle fromRadians(double radians)
    {
        return new Angle(RADIANS_TO_DEGREES * radians, radians);
    }

    private static final double PIOver2 = Math.PI / 2;

    public static Angle fromDegreesLatitude(double degrees)
    {
        degrees = degrees < -90 ? -90 : degrees > 90 ? 90 : degrees;
        double radians = DEGREES_TO_RADIANS * degrees;
        radians = radians < -PIOver2 ? -PIOver2 : radians > PIOver2 ? PIOver2 : radians;

        return new Angle(degrees, radians);
    }

    public static Angle fromRadiansLatitude(double radians)
    {
        radians = radians < -PIOver2 ? -PIOver2 : radians > PIOver2 ? PIOver2 : radians;
        double degrees = RADIANS_TO_DEGREES * radians;
        degrees = degrees < -90 ? -90 : degrees > 90 ? 90 : degrees;

        return new Angle(degrees, radians);
    }

    public static Angle fromDegreesLongitude(double degrees)
    {
        degrees = degrees < -180 ? -180 : degrees > 180 ? 180 : degrees;
        double radians = DEGREES_TO_RADIANS * degrees;
        radians = radians < -Math.PI ? -Math.PI : radians > Math.PI ? Math.PI : radians;

        return new Angle(degrees, radians);
    }

    public static Angle fromRadiansLongitude(double radians)
    {
        radians = radians < -Math.PI ? -Math.PI : radians > Math.PI ? Math.PI : radians;
        double degrees = RADIANS_TO_DEGREES * radians;
        degrees = degrees < -180 ? -180 : degrees > 180 ? 180 : degrees;

        return new Angle(degrees, radians);
    }

    /**
     * Obtains an angle from rectangular coordinates.
     *
     * @param x the abscissa coordinate.
     * @param y the ordinate coordinate.
     *
     * @return a new angle, whose size is determined from <code>x</code> and <code>y</code>.
     */
    public static Angle fromXY(double x, double y)
    {
        double radians = Math.atan2(y, x);
        return new Angle(RADIANS_TO_DEGREES * radians, radians);
    }

    /**
     * Obtain an angle from a given number of positive degrees, minutes and seconds.
     *
     * @param degrees integer number of degrees, positive.
     * @param minutes integer number of minutes, positive only between 0 and 60.
     * @param seconds integer number of seconds, positive only between 0 and 60.
     *
     * @return a new angle whose size in degrees is given by <code>degrees</code>, <code>minutes</code> and
     *         <code>seconds</code>.
     *
     * @throws IllegalArgumentException if minutes or seconds are outside the 0-60 range or the degrees is negative.
     */
    public static Angle fromDMS(int degrees, int minutes, int seconds)
    {
        if (degrees < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", degrees);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (minutes < 0 || minutes >= 60)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", minutes);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (seconds < 0 || seconds >= 60)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", seconds);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(degrees + minutes / 60d + seconds / 3600d);
    }

    /**
     * Obtain an angle from a given number of positive degrees and decimal minutes.
     *
     * @param degrees integer number of degrees, positive.
     * @param minutes double representing the decimal representation of minutes and seconds.
     *
     * @return a new angle whose size in degrees is given by <code>degrees</code> and decimal <code>minutes</code>.
     *
     * @throws IllegalArgumentException if minutes or seconds are outside the 0-60 range or the degrees is negative.
     */
    public static Angle fromDMdS(int degrees, double minutes)
    {
        if (degrees < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", degrees);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (minutes < 0 || minutes >= 60)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", minutes);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(degrees + minutes / 60d);
    }

    /**
     * Obtain an angle from a degrees, minute and seconds character string.
     * <p>eg:</p><pre>
     * 123 34 42
     * -123* 34' 42" (where * stands for the degree symbol)
     * +45* 12' 30" (where * stands for the degree symbol)
     * 45 12 30 S
     * 45 12 30 N
     * </pre>
     * For a string containing both a sign and compass direction, the compass direction will take precedence.
     *
     * @param dmsString the degrees, minute and second character string.
     *
     * @return the corresponding angle.
     *
     * @throws IllegalArgumentException if dmsString is null or not properly formated.
     */
    public static Angle fromDMS(String dmsString)
    {
        if (dmsString == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        // Check for string format validity
        String regex = "([-|\\+]?\\d{1,3}[d|D|\u00B0|\\s](\\s*\\d{1,2}['|\u2019|\\s])?"
            + "(\\s*\\d{1,2}[\"|\u201d|\\s])?\\s*([N|n|S|s|E|e|W|w])?\\s?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dmsString + " ");
        if (!matcher.matches())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", dmsString);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        // Replace degree, min and sec signs with space
        dmsString = dmsString.replaceAll("[D|d|\u00B0|'|\u2019|\"|\u201d]", " ");
        // Replace multiple spaces with single ones
        dmsString = dmsString.replaceAll("\\s+", " ");
        dmsString = dmsString.trim();

        // Check for sign prefix and suffix
        int sign = 1;
        char suffix = dmsString.toUpperCase().charAt(dmsString.length() - 1);
        char prefix = dmsString.charAt(0);
        if (!Character.isDigit(suffix))
        {
            sign = (suffix == 'S' || suffix == 'W') ? -1 : 1;
            dmsString = dmsString.substring(0, dmsString.length() - 1);
            dmsString = dmsString.trim();

            // check and trim the prefix if it is erroneously included
            if (!Character.isDigit(prefix))
            {
                dmsString = dmsString.substring(1, dmsString.length());
                dmsString = dmsString.trim();
            }
        }
        else if (!Character.isDigit(prefix))
        {
            sign *= (prefix == '-') ? -1 : 1;
            dmsString = dmsString.substring(1, dmsString.length());
        }

        // Extract degrees, minutes and seconds
        String[] DMS = dmsString.split(" ");
        int d = Integer.parseInt(DMS[0]);
        int m = DMS.length > 1 ? Integer.parseInt(DMS[1]) : 0;
        int s = DMS.length > 2 ? Integer.parseInt(DMS[2]) : 0;

        return fromDMS(d, m, s).multiply(sign);
    }

    public final double degrees;
    public final double radians;

    public Angle(Angle angle)
    {
        this.degrees = angle.degrees;
        this.radians = angle.radians;
    }

    private Angle(double degrees, double radians)
    {
        this.degrees = degrees;
        this.radians = radians;
    }

    /**
     * Retrieves the size of this angle in degrees. This method may be faster than first obtaining the radians and then
     * converting to degrees.
     *
     * @return the size of this angle in degrees.
     */
    public final double getDegrees()
    {
        return this.degrees;
    }

    /**
     * Retrieves the size of this angle in radians. This may be useful for <code>java.lang.Math</code> functions, which
     * generally take radians as trigonometric arguments. This method may be faster that first obtaining the degrees and
     * then converting to radians.
     *
     * @return the size of this angle in radians.
     */
    public final double getRadians()
    {
        return this.radians;
    }

    /**
     * Obtains the sum of these two angles. Does not accept a null argument. This method is commutative, so
     * <code>a.add(b)</code> and <code>b.add(a)</code> are equivalent. Neither this angle nor angle is changed, instead
     * the result is returned as a new angle.
     *
     * @param angle the angle to add to this one.
     *
     * @return an angle whose size is the total of this angles and angles size.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    public final Angle add(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(this.degrees + angle.degrees);
    }

    /**
     * Obtains the difference of these two angles. Does not accept a null argument. This method is not commutative.
     * Neither this angle nor angle is changed, instead the result is returned as a new angle.
     *
     * @param angle the angle to subtract from this angle.
     *
     * @return a new angle corresponding to this angle's size minus angle's size.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    public final Angle subtract(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(this.degrees - angle.degrees);
    }

    /**
     * Multiplies this angle by <code>multiplier</code>. This angle remains unchanged. The result is returned as a new
     * angle.
     *
     * @param multiplier a scalar by which this angle is multiplied.
     *
     * @return a new angle whose size equals this angle's size multiplied by <code>multiplier</code>.
     */
    public final Angle multiply(double multiplier)
    {
        return Angle.fromDegrees(this.degrees * multiplier);
    }

    /**
     * Divides this angle by another angle. This angle remains unchanged, instead the resulting value in degrees is
     * returned.
     *
     * @param angle the angle by which to divide.
     *
     * @return this angle's degrees divided by angle's degrees.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    public final double divide(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (angle.getDegrees() == 0.0)
        {
            String message = Logging.getMessage("generic.DivideByZero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.degrees / angle.degrees;
    }

    public final Angle addDegrees(double degrees)
    {
        return Angle.fromDegrees(this.degrees + degrees);
    }

    public final Angle subtractDegrees(double degrees)
    {
        return Angle.fromDegrees(this.degrees - degrees);
    }

    /**
     * Divides this angle by <code>divisor</code>. This angle remains unchanged. The result is returned as a new angle.
     * Behaviour is undefined if <code>divisor</code> equals zero.
     *
     * @param divisor the number to be divided by.
     *
     * @return a new angle equivalent to this angle divided by <code>divisor</code>.
     */
    public final Angle divide(double divisor)
    {
        return Angle.fromDegrees(this.degrees / divisor);
    }

    public final Angle addRadians(double radians)
    {
        return Angle.fromRadians(this.radians + radians);
    }

    public final Angle subtractRadians(double radians)
    {
        return Angle.fromRadians(this.radians - radians);
    }

    /**
     * Computes the shortest distance between this and angle, as an angle.
     *
     * @param angle the angle to measure angular distance to.
     *
     * @return the angular distance between this and <code>value</code>.
     */
    public Angle angularDistanceTo(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double differenceDegrees = angle.subtract(this).degrees;
        if (differenceDegrees < -180)
            differenceDegrees += 360;
        else if (differenceDegrees > 180)
            differenceDegrees -= 360;

        double absAngle = Math.abs(differenceDegrees);
        return Angle.fromDegrees(absAngle);
    }

    /**
     * Obtains the sine of this angle.
     *
     * @return the trigonometric sine of this angle.
     */
    public final double sin()
    {
        return Math.sin(this.radians);
    }

    public final double sinHalfAngle()
    {
        return Math.sin(0.5 * this.radians);
    }

    public static Angle asin(double sine)
    {
        return Angle.fromRadians(Math.asin(sine));
    }

    public static double arctanh(double radians)
    {
        return 0.5 * Math.log((1 + radians) / (1 - radians));
    }

    /**
     * Obtains the cosine of this angle.
     *
     * @return the trigonometric cosine of this angle.
     */
    public final double cos()
    {
        return Math.cos(this.radians);
    }

    public final double cosHalfAngle()
    {
        return Math.cos(0.5 * this.radians);
    }

    public static Angle acos(double cosine)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromRadians(Math.acos(cosine));
    }

    /**
     * Obtains the tangent of half of this angle.
     *
     * @return the trigonometric tangent of half of this angle.
     */
    public final double tanHalfAngle()
    {
        return Math.tan(0.5 * this.radians);
    }

    public static Angle atan(double tan)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromRadians(Math.atan(tan));
    }

    /**
     * Obtains the average of two angles. This method is commutative, so <code>midAngle(m, n)</code> and
     * <code>midAngle(n, m)</code> are equivalent.
     *
     * @param a1 the first angle.
     * @param a2 the second angle.
     *
     * @return the average of <code>a1</code> and <code>a2</code> throws IllegalArgumentException if either angle is
     *         null.
     */
    public static Angle midAngle(Angle a1, Angle a2)
    {
        if (a1 == null || a2 == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(0.5 * (a1.degrees + a2.degrees));
    }

    /**
     * Obtains the average of three angles. The order of parameters does not matter.
     *
     * @param a the first angle.
     * @param b the second angle.
     *
     * @return the average of <code>a1</code>, <code>a2</code> and <code>a3</code>
     *
     * @throws IllegalArgumentException if <code>a</code> or <code>b</code> is null
     */
    public static Angle average(Angle a, Angle b)
    {
        if (a == null || b == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(0.5 * (a.degrees + b.degrees));
    }

    /**
     * Obtains the average of three angles. The order of parameters does not matter.
     *
     * @param a the first angle.
     * @param b the second angle.
     * @param c the third angle.
     *
     * @return the average of <code>a1</code>, <code>a2</code> and <code>a3</code>.
     *
     * @throws IllegalArgumentException if <code>a</code>, <code>b</code> or <code>c</code> is null.
     */
    public static Angle average(Angle a, Angle b, Angle c)
    {
        if (a == null || b == null || c == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees((a.degrees + b.degrees + c.degrees) / 3);
    }

    /**
     * Limits a specified angle to be within a specified minimum and maximum.
     * <p>
     * The returned angle is undefined if min &gt; max. Otherwise, this method's return value is equivalent to the
     * following:
     * <ul> <li>min - If value &lt; min</li> <li>max - If value &gt; max</li> <li>value - If min &lt;= value &gt;= max</li> </ul>
     *
     * @param value The angle to clamp.
     * @param min   The minimum angle to clamp to.
     * @param max   The maximum angle to clamp to.
     *
     * @return The clamped angle.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public static Angle clamp(Angle value, Angle min, Angle max)
    {
        if (value == null || min == null || max == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return value.degrees < min.degrees ? min : (value.degrees > max.degrees ? max : value);
    }

    /**
     * Linearly interpolates between two angles.
     *
     * @param amount the interpolant.
     * @param value1 the first angle.
     * @param value2 the second angle.
     *
     * @return a new angle between <code>value1</code> and <code>value2</code>.
     */
    public static Angle mix(double amount, Angle value1, Angle value2)
    {
        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (amount < 0)
            return value1;
        else if (amount > 1)
            return value2;

        Quaternion quat = Quaternion.slerp(
            amount,
            Quaternion.fromAxisAngle(value1, Vec4.UNIT_X),
            Quaternion.fromAxisAngle(value2, Vec4.UNIT_X));

        Angle angle = quat.getRotationX();
        if (Double.isNaN(angle.degrees))
            return null;

        return angle;
    }

    /**
     * Compares this {@link Angle} with another. Returns a negative integer if this is the smaller angle, a positive
     * integer if this is the larger, and zero if both angles are equal.
     *
     * @param angle the angle to compare against.
     *
     * @return -1 if this angle is smaller, 0 if both are equal and +1 if this angle is larger.
     *
     * @throws IllegalArgumentException if angle is null.
     */
    public final int compareTo(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.degrees < angle.degrees)
            return -1;

        if (this.degrees > angle.degrees)
            return 1;

        return 0;
    }

    public static double normalizedDegrees(double degrees)
    {
        double a = degrees % 360;
        return a > 180 ? a - 360 : a < -180 ? 360 + a : a;
    }

    public static double normalizedDegreesLatitude(double degrees)
    {
        double lat = degrees % 180;
        return lat > 90 ? 180 - lat : lat < -90 ? -180 - lat : lat;
    }

    public static double normalizedDegreesLongitude(double degrees)
    {
        double lon = degrees % 360;
        return lon > 180 ? lon - 360 : lon < -180 ? 360 + lon : lon;
    }

    public static Angle normalizedAngle(Angle unnormalizedAngle)
    {
        if (unnormalizedAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Angle.fromDegrees(normalizedDegrees(unnormalizedAngle.degrees));
    }

    public static Angle normalizedLatitude(Angle unnormalizedAngle)
    {
        if (unnormalizedAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Angle.fromDegrees(normalizedDegreesLatitude(unnormalizedAngle.degrees));
    }

    public static Angle normalizedLongitude(Angle unnormalizedAngle)
    {
        if (unnormalizedAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Angle.fromDegrees(normalizedDegreesLongitude(unnormalizedAngle.degrees));
    }

    public Angle normalize()
    {
        return normalizedAngle(this);
    }

    public Angle normalizedLatitude()
    {
        return normalizedLatitude(this);
    }

    public Angle normalizedLongitude()
    {
        return normalizedLongitude(this);
    }

    public static boolean crossesLongitudeBoundary(Angle angleA, Angle angleB)
    {
        if (angleA == null || angleB == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // A segment cross the line if end pos have different longitude signs
        // and are more than 180 degrees longitude apart
        return (Math.signum(angleA.degrees) != Math.signum(angleB.degrees))
            && (Math.abs(angleA.degrees - angleB.degrees) > 180);
    }

    public static boolean isValidLatitude(double value)
    {
        return value >= -90 && value <= 90;
    }

    public static boolean isValidLongitude(double value)
    {
        return value >= -180 && value <= 180;
    }

    public static Angle max(Angle a, Angle b)
    {
        return a.degrees >= b.degrees ? a : b;
    }

    public static Angle min(Angle a, Angle b)
    {
        return a.degrees <= b.degrees ? a : b;
    }

    /**
     * Obtains a <code>String</code> representation of this angle.
     *
     * @return the value of this angle in degrees and as a <code>String</code>.
     */
    @Override
    public final String toString()
    {
        return Double.toString(this.degrees) + '\u00B0';
    }

    /**
     * Forms a decimal degrees {@link String} representation of this {@link Angle}.
     *
     * @param digits the number of digits past the decimal point to include in the string.
     *
     * @return the value of this angle in decimal degrees as a string with the specified number of digits beyond the
     *         decimal point. The string is padded with trailing zeros to fill the number of post-decimal point
     *         positions requested.
     */
    public final String toDecimalDegreesString(int digits)
    {
        if ((digits < 0) || (digits > 15))
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", digits);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return String.format("%." + digits + "f\u00B0", this.degrees);
    }

    /**
     * Obtains a {@link String} representation of this {@link Angle} formatted as degrees, minutes and seconds integer
     * values.
     *
     * @return the value of this angle in degrees, minutes, seconds as a string.
     */
    public final String toDMSString()
    {
        double temp = this.degrees;
        int sign = (int) Math.signum(temp);
        temp *= sign;
        int d = (int) Math.floor(temp);
        temp = (temp - d) * 60d;
        int m = (int) Math.floor(temp);
        temp = (temp - m) * 60d;
        int s = (int) Math.round(temp);

        if (s == 60)
        {
            m++;
            s = 0;
        } // Fix rounding errors
        if (m == 60)
        {
            d++;
            m = 0;
        }

        return (sign == -1 ? "-" : "") + d + '\u00B0' + ' ' + m + '\u2019' + ' ' + s + '\u201d';
    }

    /**
     * Obtains a {@link String} representation of this {@link Angle} formatted as degrees and decimal minutes.
     *
     * @return the value of this angle in degrees and decimal minutes as a string.
     */
    public final String toDMString()
    {
        double temp = this.degrees;
        int sign = (int) Math.signum(temp);
        temp *= sign;
        int d = (int) Math.floor(temp);
        temp = (temp - d) * 60d;
        int m = (int) Math.floor(temp);
        temp = (temp - m) * 60d;
        int s = (int) Math.round(temp);

        if (s == 60)
        {
            m++;
            s = 0;
        } // Fix rounding errors
        if (m == 60)
        {
            d++;
            m = 0;
        }

        double mf = s == 0 ? m : m + s / 60.0;

        return (sign == -1 ? "-" : "") + d + '\u00B0' + ' ' + String.format("%5.2f", mf) + '\u2019';
    }

    public final String toFormattedDMSString()
    {
        double temp = this.degrees;
        int sign = (int) Math.signum(temp);

        temp *= sign;
        int d = (int) Math.floor(temp);
        temp = (temp - d) * 60d;
        int m = (int) Math.floor(temp);
        temp = (temp - m) * 60d;
        double s = Math.rint(temp * 100) / 100;  // keep two decimals for seconds

        if (s == 60)
        {
            m++;
            s = 0;
        } // Fix rounding errors
        if (m == 60)
        {
            d++;
            m = 0;
        }

        return String.format("%4d\u00B0 %2d\u2019 %5.2f\u201d", sign * d, m, s);
    }

    public final double[] toDMS()
    {
        double temp = this.degrees;
        int sign = (int) Math.signum(temp);

        temp *= sign;
        int d = (int) Math.floor(temp);
        temp = (temp - d) * 60d;
        int m = (int) Math.floor(temp);
        temp = (temp - m) * 60d;
        double s = Math.rint(temp * 100) / 100;  // keep two decimals for seconds

        if (s == 60)
        {
            m++;
            s = 0;
        } // Fix rounding errors
        if (m == 60)
        {
            d++;
            m = 0;
        }

        return new double[] {sign * d, m, s};
    }

    /**
     * Obtains the amount of memory this {@link Angle} consumes.
     *
     * @return the memory footprint of this angle in bytes.
     */
    public long getSizeInBytes()
    {
        return Double.SIZE / 8;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Angle angle = (Angle) o;

        //noinspection RedundantIfStatement
        if (angle.degrees != this.degrees)
            return false;

        return true;
    }

    public int hashCode()
    {
        long temp = degrees != +0.0d ? Double.doubleToLongBits(degrees) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }
}
