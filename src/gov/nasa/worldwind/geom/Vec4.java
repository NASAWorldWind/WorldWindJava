/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: Vec4.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Vec4
{
    public static final Vec4 INFINITY =
        new Vec4(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
    public static final Vec4 ZERO = new Vec4(0, 0, 0, 1);
    public static final Vec4 ONE = new Vec4(1, 1, 1, 1);
    public static final Vec4 UNIT_X = new Vec4(1, 0, 0, 0);
    public static final Vec4 UNIT_NEGATIVE_X = new Vec4(-1, 0, 0, 0);
    public static final Vec4 UNIT_Y = new Vec4(0, 1, 0, 0);
    public static final Vec4 UNIT_NEGATIVE_Y = new Vec4(0, -1, 0, 0);
    public static final Vec4 UNIT_Z = new Vec4(0, 0, 1, 0);
    public static final Vec4 UNIT_NEGATIVE_Z = new Vec4(0, 0, -1, 0);
    public static final Vec4 UNIT_W = new Vec4(0, 0, 0, 1);
    public static final Vec4 UNIT_NEGATIVE_W = new Vec4(0, 0, 0, -1);

    public final double x;
    public final double y;
    public final double z;
    public final double w;

    // Default W-component will be 1 to handle Matrix transformation.
    private static final double DEFAULT_W = 1.0;
    // Cached computations.
    private int hashCode;

    public Vec4(double value)
    {
        this(value, value, value);
    }

    public Vec4(double x, double y)
    {
        this(x, y, 0, DEFAULT_W);
    }

    public Vec4(double x, double y, double z)
    {
        this(x, y, z, DEFAULT_W);
    }

    public Vec4(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Vec4 that = (Vec4) obj;
        return (this.x == that.x)
            && (this.y == that.y)
            && (this.z == that.z)
            && (this.w == that.w);
    }

    public final int hashCode()
    {
        if (this.hashCode == 0)
        {
            int result;
            long tmp;
            tmp = Double.doubleToLongBits(this.x);
            result = (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.y);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.z);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.w);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            this.hashCode = result;
        }
        return this.hashCode;
    }

    /**
     * Constructs a new Vec4 with coordinate values read from the specified double array. The specified offset must be 0
     * or greater, the specified length must be 1 or greater, and the array must have capacity equal to or greater than
     * <code>offset + length</code>. Coordinates are assigned as follows:<p><code>x = array[offset]</code><br> <code>y
     * = array[offset + 1]</code> if <code>length &gt; 1</code>, otherwise <code>y=0</code><br><code>z = array[offset +
     * 2]</code> if <code>length &gt; 2</code>, otherwise <code>z=0</code><br><code>w = array[offset + 3]</code> if
     * <code>length &gt; 3</code>, otherwise <code>w=1</code></p>
     *
     * @param array  the double array from which to read coordinate data.
     * @param offset the array starting index.
     * @param length the number of coordinates to read.
     *
     * @return a new Vec4 with coordinate values read from the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, if length is less than 1, or if the
     *                                  array's capacity is less than <code>offset + length</code>.
     */
    public static Vec4 fromDoubleArray(double[] array, int offset, int length)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length < 1)
        {
            String msg = Logging.getMessage("generic.LengthIsInvalid", length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < offset + length)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length == 1)
            return new Vec4(array[offset], 0d);

        if (length == 2)
            return new Vec4(array[offset], array[offset + 1]);

        if (length == 3)
            return new Vec4(array[offset], array[offset + 1], array[offset + 2]);

        return new Vec4(array[offset], array[offset + 1], array[offset + 2], array[offset + 3]);
    }

    /**
     * Constructs a new Vec4 with coordinate values read from the specified float array. The specified offset must be 0
     * or greater, the specified length must be 1 or greater, and the array must have capacity equal to or greater than
     * <code>offset + length</code>. Coordinates are assigned as follows:<p><code>x = array[offset]</code><br> <code>y
     * = array[offset + 1]</code> if <code>length &gt; 1</code>, otherwise <code>y=0</code><br><code>z = array[offset +
     * 2]</code> if <code>length &gt; 2</code>, otherwise <code>z=0</code><br><code>w = array[offset + 3]</code> if
     * <code>length &gt; 3</code>, otherwise <code>w=1</code></p>
     *
     * @param array  the float array from which to read coordinate data.
     * @param offset the array starting index.
     * @param length the number of coordinates to read.
     *
     * @return a new Vec4 with coordinate values read from the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, if length is less than 1, or if the
     *                                  array's capacity is less than <code>offset + length</code>.
     */
    public static Vec4 fromFloatArray(float[] array, int offset, int length)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length < 1)
        {
            String msg = Logging.getMessage("generic.LengthIsInvalid", length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < offset + length)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length == 2)
            return new Vec4(array[offset], array[offset + 1], 0d);

        if (length == 3)
            return new Vec4(array[offset], array[offset + 1], array[offset + 2]);

        return new Vec4(array[offset], array[offset + 1], array[offset + 2], array[offset + 3]);
    }

    /**
     * Constructs a new Vec4 with <code>x</code> and <code>y</code> values from the specified double array. The
     * specified offset must be 0 or greater, and the array must have capacity equal to or greater than <code>offset +
     * 2</code>. Coordinates are assigned as follows:<p><code>x = array[offset]</code><br><code>y = array[offset +
     * 1]</code></p>
     *
     * @param array  the double array from which to read coordinate data.
     * @param offset the array starting index.
     *
     * @return a new Vec4 with <code>x</code> and <code>y</code> coordinate values from the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, or if the array's capacity is less
     *                                  than <code>offset + 2</code>.
     */
    public static Vec4 fromArray2(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromDoubleArray(array, offset, 2);
    }

    /**
     * Constructs a new Vec4 with <code>x</code>, <code>y</code> and <code>z</code> values from the specified double
     * array. The specified offset must be 0 or greater, and the array must have capacity equal to or greater than
     * <code>offset + 3</code>. Coordinates are assigned as follows:<p><code>x = array[offset]</code><br><code>y =
     * array[offset + 1]</code><br><code>z = array[offset + 2]</code></p>
     *
     * @param array  the double array from which to read coordinate data.
     * @param offset the array starting index.
     *
     * @return a new Vec4 with <code>x</code>, <code>y</code> and <code>z</code> coordinate values from the specified
     *         double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, or if the array's capacity is less
     *                                  than <code>offset + 3</code>.
     */
    public static Vec4 fromArray3(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromDoubleArray(array, offset, 3);
    }

    /**
     * Constructs a new Vec4 with <code>x</code>, <code>y</code>, <code>z</code> and <code>w</code> values from the
     * specified double array. The specified offset must be 0 or greater, and the array must have capacity equal to or
     * greater than <code>offset + 4</code>. Coordinates are assigned as follows:<p><code>x =
     * array[offset]</code><br><code>y = array[offset + 1]</code><br><code>z = array[offset + 2]</code><br><code>w =
     * array[offset + 3]</code></p>
     *
     * @param array  the double array from which to read coordinate data.
     * @param offset the array starting index.
     *
     * @return a new Vec4 with <code>x</code>, <code>y</code>, <code>z</code> and <code>w</code> coordinate values from
     *         the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, or if the array's capacity is less
     *                                  than <code>offset + 4</code>.
     */
    public static Vec4 fromArray4(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromDoubleArray(array, offset, 4);
    }

    /**
     * Writes this Vec4's coordinate values to the specified double array. The specified offset must be 0 or greater,
     * the specified length must be 1 or greater, and the array must have capacity equal to or greater than <code>offset
     * + length</code>. Coordinates are written to the array as follows:<p><code>array[offset] =
     * x</code><br><code>array[offset + 1] = y</code> if <code>length &gt; 1</code>, otherwise <code>array[offset +
     * 1]</code> is not written to<br> <code>array[offset + 2] = z</code> if <code>length &gt; 2</code>, otherwise
     * <code>array[offset + 2]</code> is not written to<br><code>array[offset + 3] = w</code> if <code>length &gt;
     * 3</code>, otherwise <code>array[offset + 3]</code> is not written to</p>
     *
     * @param array  the double array to receive the coordinate data.
     * @param offset the array starting index.
     * @param length the number of coordinates to write.
     *
     * @return the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, if length is less than 1, or if the
     *                                  array's capacity is less than <code>offset + length</code>.
     */
    public final double[] toDoubleArray(double[] array, int offset, int length)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length < 1)
        {
            String msg = Logging.getMessage("generic.LengthIsInvalid", length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < offset + length)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        array[offset] = this.x;

        if (length > 1)
            array[offset + 1] = this.y;
        if (length > 2)
            array[offset + 2] = this.z;
        if (length > 3)
            array[offset + 3] = this.w;

        return array;
    }

    /**
     * Writes this Vec4's coordinate values to the specified float array. The specified offset must be 0 or greater, the
     * specified length must be 1 or greater, and the array must have capacity equal to or greater than <code>offset +
     * length</code>. Coordinates are written to the array as follows:<p><code>array[offset] =
     * x</code><br><code>array[offset + 1] = y</code> if <code>length &gt; 1</code>, otherwise <code>array[offset +
     * 1]</code> is not written to<br> <code>array[offset + 2] = z</code> if <code>length &gt; 2</code>, otherwise
     * <code>array[offset + 2]</code> is not written to<br><code>array[offset + 3] = w</code> if <code>length &gt;
     * 3</code>, otherwise <code>array[offset + 3]</code> is not written to</p>
     *
     * @param array  the float array to receive the coordinate data.
     * @param offset the array starting index.
     * @param length the number of coordinates to write.
     *
     * @return the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, if length is less than 1, or if the
     *                                  array's capacity is less than <code>offset + length</code>.
     */
    public final float[] toFloatArray(float[] array, int offset, int length)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (length < 1)
        {
            String msg = Logging.getMessage("generic.LengthIsInvalid", length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < offset + length)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        array[offset] = (float) this.x;
        array[offset + 1] = (float) this.y;

        if (length > 2)
            array[offset + 2] = (float) this.z;
        if (length > 3)
            array[offset + 3] = (float) this.w;

        return array;
    }

    /**
     * Writes this Vec4's <code>x</code> and <code>y</code> values to the specified double array. The specified offset
     * must be 0 or greater, and the array must have have capacity equal to or greater than <code>offset + 2</code>.
     * Coordinates are written to the array as follows:<p><code>array[offset] = x</code><br><code>array[offset + 1] =
     * y</code></p>
     *
     * @param array  the double array to receive the coordinate data.
     * @param offset the array starting index.
     *
     * @return the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, or if the array's capacity is less
     *                                  than <code>offset + 2</code>.
     */
    public final double[] toArray2(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return toDoubleArray(array, offset, 2);
    }

    /**
     * Writes this Vec4's <code>x</code>, <code>y</code> and <code>z</code> values to the specified double array. The
     * specified offset must be 0 or greater, and the array must have have capacity equal to or greater than
     * <code>offset + 3</code>. Coordinates are written to the array as follows:<p><code>array[offset] =
     * x</code><br><code>array[offset + 1] = y</code><br><code>array[offset + 2] = z</code></p>
     *
     * @param array  the double array to receive the coordinate data.
     * @param offset the array starting index.
     *
     * @return the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, or if the array's capacity is less
     *                                  than <code>offset + 3</code>.
     */
    public final double[] toArray3(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return toDoubleArray(array, offset, 3);
    }

    /**
     * Writes this Vec4's <code>x</code>, <code>y</code>, <code>z</code> and <code>w</code> values to the specified
     * double array. The specified offset must be 0 or greater, and the array must have have capacity equal to or
     * greater than <code>offset + 4</code>. Coordinates are written to the array as follows:<p><code>array[offset] =
     * x</code><br><code>array[offset + 1] = y</code><br><code>array[offset + 2] = z</code><br><code>array[offset +
     * 3] = w</code></p>
     *
     * @param array  the double array to receive the coordinate data.
     * @param offset the array starting index.
     *
     * @return the specified double array.
     *
     * @throws IllegalArgumentException if the array is null, if offset is negative, or if the array's capacity is less
     *                                  than <code>offset + 4</code>.
     */
    public final double[] toArray4(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return toDoubleArray(array, offset, 4);
    }

    /**
     * Returns a representation of this vector as an <code>x y z</code> point suitable for use where four-dimensional
     * homogeneous coordinates are required. The returned vector has <code>x y z</code> coordinates are equal to this
     * vector's <code>x y z</code> coordinates, and has <code>w</code> coordinate equal to 1.0.
     * <p>
     * A three-dimensional point in homogeneous coordinates is necessary when transforming that point by a 4x4
     * transformation matrix, or when calculating the dot product of the point and the equation of a plane. The returned
     * vector is affected by the translation component of a 4x4 transformation matrix.
     *
     * @return this <code>Vec4</code> converted to a point vector in four-dimensional homogeneous coordinates.
     */
    public Vec4 toHomogeneousPoint3()
    {
        // For a discussion of homogeneous coordinates, see "Mathematics for 3D Game Programming and Computer Graphics,
        // Second Edition" by Eric Lengyel, Section 3.4 (pages 81-84).

        if (this.w == 1.0)
            return this;

        return new Vec4(this.x, this.y, this.z, 1.0);
    }

    /**
     * Returns a representation of this vector as an <code>x y z</code> direction suitable for use where
     * four-dimensional homogeneous coordinates are required. The returned vector has <code>x y z</code> coordinates are
     * equal to this vector's <code>x y z</code> coordinates, and has <code>w</code> coordinate equal to 0.0.
     * <p>
     * A three-dimensional direction in homogeneous coordinates is necessary when transforming that direction by a 4x4
     * transformation matrix. The returned vector is not affected by the translation component of a 4x4 transformation
     * matrix, and therefore remains invariant under translation.
     *
     * @return this <code>Vec4</code> converted to a direction vector in four-dimensional homogeneous coordinates.
     */
    public Vec4 toHomogeneousDirection3()
    {
        // For a discussion of homogeneous coordinates, see "Mathematics for 3D Game Programming and Computer Graphics, 
        // Second Edition" by Eric Lengyel, Section 3.4 (pages 81-84).

        if (this.w == 0.0)
            return this;

        return new Vec4(this.x, this.y, this.z, 0.0);
    }

    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.x).append(", ");
        sb.append(this.y).append(", ");
        sb.append(this.z).append(", ");
        sb.append(this.w);
        sb.append(")");
        return sb.toString();
    }

    public final double getX()
    {
        return this.x;
    }

    public final double getY()
    {
        return this.y;
    }

    public final double getZ()
    {
        return this.z;
    }

    public final double getW()
    {
        return this.w;
    }

    public final double x()
    {
        return this.x;
    }

    public final double y()
    {
        return this.y;
    }

    public final double z()
    {
        return this.z;
    }

    public final double w()
    {
        return this.w;
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    public static Vec4 fromLine3(Vec4 origin, double t, Vec4 direction)
    {
        if (origin == null || direction == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            origin.x + (direction.x * t),
            origin.y + (direction.y * t),
            origin.z + (direction.z * t));

//        return fromLine3(
//            origin.x, origin.y, origin.z,
//            direction.x, direction.y, direction.z,
//            t,
//            true);
    }
//
//    private static Vec4 fromLine3(
//        double px, double py, double pz,
//        double dx, double dy, double dz,
//        double t,
//        boolean normalize)
//    {
//        if (normalize)
//        {
//            double dLength = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
//            if (!isZero(dLength) && (dLength != 1.0))
//            {
//                dx /= dLength;
//                dy /= dLength;
//                dz /= dLength;
//            }
//        }
//
//        return new Vec4(
//            px + (dx * t),
//            py + (dy * t),
//            pz + (dz * t));
//    }

    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //

    public final Vec4 add3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x + vec4.x,
            this.y + vec4.y,
            this.z + vec4.z,
            this.w);
    }

    public final Vec4 add3(double x, double y, double z)
    {
        return new Vec4(this.x + x, this.y + y, this.z + z, this.w);
    }

    public final Vec4 subtract3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x - vec4.x,
            this.y - vec4.y,
            this.z - vec4.z,
            this.w);
    }

    public final Vec4 subtract3(double x, double y, double z)
    {
        return new Vec4(this.x - x, this.y - y, this.z - z, this.w);
    }

    public final Vec4 multiply3(double value)
    {
        return new Vec4(
            this.x * value,
            this.y * value,
            this.z * value,
            this.w);
    }

    public final Vec4 multiply3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x * vec4.x,
            this.y * vec4.y,
            this.z * vec4.z,
            this.w);
    }

    public final Vec4 divide3(double value)
    {
        if (value == 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x / value,
            this.y / value,
            this.z / value,
            this.w);
    }

    public final Vec4 divide3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x / vec4.x,
            this.y / vec4.y,
            this.z / vec4.z,
            this.w);
    }

    public final Vec4 getNegative3()
    {
        return new Vec4(
            0.0 - this.x,
            0.0 - this.y,
            0.0 - this.z,
            this.w);
    }

    public final Vec4 getAbs3()
    {
        return new Vec4(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //

    public final double getLength3()
    {
        return Math.sqrt(this.getLengthSquared3());
    }

    public final double getLengthSquared3()
    {
        return (this.x * this.x)
            + (this.y * this.y)
            + (this.z * this.z);
    }

    public final Vec4 normalize3()
    {
        double length = this.getLength3();
        // Vector has zero length.
        if (length == 0)
        {
            return this;
        }
        else
        {
            return new Vec4(
                this.x / length,
                this.y / length,
                this.z / length);
        }
    }

    public final double distanceTo2(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double dx = vec4.x - this.x;
        double dy = vec4.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public final double distanceTo3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Math.sqrt(this.distanceToSquared3(vec4));
    }

    public final double distanceToSquared3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double tmp;
        double result = 0.0;
        tmp = this.x - vec4.x;
        result += tmp * tmp;
        tmp = this.y - vec4.y;
        result += tmp * tmp;
        tmp = this.z - vec4.z;
        result += tmp * tmp;
        return result;
    }

    public final double dot3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return (this.x * vec4.x) + (this.y * vec4.y) + (this.z * vec4.z);
    }

    public final double dot4(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return (this.x * vec4.x) + (this.y * vec4.y) + (this.z * vec4.z) + (this.w * vec4.w);
    }

    public final double dotSelf3()
    {
        return this.dot3(this);
    }

    public final double dotSelf4()
    {
        return this.dot4(this);
    }

    public final Vec4 cross3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (this.y * vec4.z) - (this.z * vec4.y),
            (this.z * vec4.x) - (this.x * vec4.z),
            (this.x * vec4.y) - (this.y * vec4.x));
    }

    public final Angle angleBetween3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double a_dot_b = this.dot3(vec4);
        // Compute the sum of magnitudes.
        double length = this.getLength3() * vec4.getLength3();
        // Normalize the dot product, if necessary.
        if (!(length == 0) && (length != 1.0))
            a_dot_b /= length;

        // The normalized dot product should be in the range [-1, 1]. Otherwise the result is an error from floating
        // point roundoff. So if a_dot_b is less than -1 or greater than +1, we treat it as -1 and +1 respectively.
        if (a_dot_b < -1.0)
            a_dot_b = -1.0;
        else if (a_dot_b > 1.0)
            a_dot_b = 1.0;

        // Angle is arc-cosine of normalized dot product.
        return Angle.fromRadians(Math.acos(a_dot_b));
    }

    /**
     * Compute the angle and rotation axis required to rotate one vector to align with another.
     *
     * @param v1     The base vector.
     * @param v2     The vector to rotate into alignment with <code>v1</code>.
     * @param result A reference to an array in which to return the computed axis. May not be null.
     *
     * @return The rotation angle.
     *
     * @throws IllegalArgumentException if any parameter is null.
     */
    public static Angle axisAngle(Vec4 v1, Vec4 v2, Vec4[] result)
    {
        if (v1 == null || v2 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute rotation angle
        Vec4 u1 = v1.normalize3();
        Vec4 u0 = v2.normalize3();
        Angle angle = Angle.fromRadians(Math.acos(u0.x * u1.x + u0.y * u1.y + u0.z * u1.z));

        // Compute rotation axis
        double A = (u0.y * u1.z) - (u0.z * u1.y);
        double B = (u0.z * u1.x) - (u0.x * u1.z);
        double C = (u0.x * u1.y) - (u0.y * u1.x);
        double L = Math.sqrt(A * A + B * B + C * C);
        result[0] = new Vec4(A / L, B / L, C / L);

        return angle;
    }

    public final Vec4 projectOnto3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double dot = this.dot3(vec4);
        double length = vec4.getLength3();
        // Normalize the dot product, if necessary.
        if (!(length == 0) && (length != 1.0))
            dot /= (length * length);
        return vec4.multiply3(dot);
    }

    public final Vec4 perpendicularTo3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.subtract3(projectOnto3(vec4));
    }

    /**
     * Computes two vectors mutually perpendicular to this vector and each other.
     *
     * @return an array of two unit vectors mutually orthogonal to this vector.
     */
    public Vec4[] perpendicularVectors()
    {
        // For the first vector, use the direction of the least component of this, which indicates the more
        // orthogonal axis to this.
        Vec4 v = this;
        Vec4 v1 = v.x <= v.y && v.x <= v.z ? Vec4.UNIT_X : v.y <= v.x && v.y <= v.z ? Vec4.UNIT_Y : Vec4.UNIT_Z;
        Vec4 va = v.cross3(v1).normalize3();
        Vec4 vb = v.cross3(va).normalize3();

        return new Vec4[] {va, vb};
    }

    public final Vec4 transformBy3(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (matrix.m11 * this.x) + (matrix.m12 * this.y) + (matrix.m13 * this.z),
            (matrix.m21 * this.x) + (matrix.m22 * this.y) + (matrix.m23 * this.z),
            (matrix.m31 * this.x) + (matrix.m32 * this.y) + (matrix.m33 * this.z));
    }

    public final Vec4 transformBy3(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Quaternion tmp = new Quaternion(this.x, this.y, this.z, 0.0);
        tmp = quaternion.multiply(tmp);
        tmp = tmp.multiply(quaternion.getInverse());
        return new Vec4(tmp.x, tmp.y, tmp.z, 0.0);
    }

    public final Vec4 transformBy4(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (matrix.m11 * this.x) + (matrix.m12 * this.y) + (matrix.m13 * this.z) + (matrix.m14 * this.w),
            (matrix.m21 * this.x) + (matrix.m22 * this.y) + (matrix.m23 * this.z) + (matrix.m24 * this.w),
            (matrix.m31 * this.x) + (matrix.m32 * this.y) + (matrix.m33 * this.z) + (matrix.m34 * this.w),
            (matrix.m41 * this.x) + (matrix.m42 * this.y) + (matrix.m43 * this.z) + (matrix.m44 * this.w));
    }

    // ============== Mixing Functions ======================= //
    // ============== Mixing Functions ======================= //
    // ============== Mixing Functions ======================= //

    public static Vec4 min3(Vec4 value1, Vec4 value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (value1.x < value2.x) ? value1.x : value2.x,
            (value1.y < value2.y) ? value1.y : value2.y,
            (value1.z < value2.z) ? value1.z : value2.z);
    }

    public static Vec4 max3(Vec4 value1, Vec4 value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (value1.x > value2.x) ? value1.x : value2.x,
            (value1.y > value2.y) ? value1.y : value2.y,
            (value1.z > value2.z) ? value1.z : value2.z);
    }

    public static Vec4 clamp3(Vec4 vec4, double min, double max)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (vec4.x < min) ? min : ((vec4.x > max) ? max : vec4.x),
            (vec4.y < min) ? min : ((vec4.y > max) ? max : vec4.y),
            (vec4.z < min) ? min : ((vec4.z > max) ? max : vec4.z));
    }

    public static Vec4 mix3(double amount, Vec4 value1, Vec4 value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (amount < 0.0)
            return value1;
        else if (amount > 1.0)
            return value2;

        double t1 = 1.0 - amount;
        return new Vec4(
            (value1.x * t1) + (value2.x * amount),
            (value1.y * t1) + (value2.y * amount),
            (value1.z * t1) + (value2.z * amount));
    }

    /**
     * Returns the arithmetic mean of the x, y, z, and w coordinates of the specified points Iterable. This returns null
     * if the Iterable contains no points, or if all of the points are null.
     *
     * @param points the Iterable of points which define the returned arithmetic mean.
     *
     * @return the arithmetic mean point of the specified points Iterable, or null if the Iterable is empty or contains
     *         only null points.
     *
     * @throws IllegalArgumentException if the Iterable is null.
     */
    public static Vec4 computeAveragePoint(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int count = 0;
        double x = 0d;
        double y = 0d;
        double z = 0d;
        double w = 0d;

        for (Vec4 vec : points)
        {
            if (vec == null)
                continue;

            count++;
            x += vec.x;
            y += vec.y;
            z += vec.z;
            w += vec.w;
        }

        if (count == 0)
            return null;

        return new Vec4(x / (double) count, y / (double) count, z / (double) count, w / (double) count);
    }

    /**
     * Returns the arithmetic mean of the x, y, z coordinates of the specified points buffer. This returns null if the
     * buffer is empty.
     * <p>
     * The buffer must contain XYZ coordinate tuples which are either tightly packed or offset by the specified stride.
     * The stride specifies the number of buffer elements between the first coordinate of consecutive tuples. For
     * example, a stride of 3 specifies that each tuple is tightly packed as XYZXYZXYZ, whereas a stride of 5 specifies
     * that there are two elements between each tuple as XYZabXYZab (the elements "a" and "b" are ignored). The stride
     * must be at least 3. If the buffer's length is not evenly divisible into stride-sized tuples, this ignores the
     * remaining elements that follow the last complete tuple.
     *
     * @param coordinates the buffer containing the point coordinates for which to compute a bounding volume.
     * @param stride      the number of elements between the first coordinate of consecutive points. If stride is 3,
     *                    this interprets the buffer has having tightly packed XYZ coordinate tuples.
     *
     * @return the arithmetic mean point of the specified points Iterable, or null if the Iterable is empty or contains
     *         only null points.
     *
     * @throws IllegalArgumentException if the buffer is null, or if the stride is less than three.
     */
    public static Vec4 computeAveragePoint3(BufferWrapper coordinates, int stride)
    {
        if (coordinates == null)
        {
            String msg = Logging.getMessage("nullValue.CoordinatesAreNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (stride < 3)
        {
            String msg = Logging.getMessage("generic.StrideIsInvalid");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int count = 0;
        double x = 0d;
        double y = 0d;
        double z = 0d;

        for (int i = 0; i <= coordinates.length() - stride; i += stride)
        {
            count++;
            x += coordinates.getDouble(i);
            y += coordinates.getDouble(i + 1);
            z += coordinates.getDouble(i + 2);
        }

        if (count == 0)
            return null;

        return new Vec4(x / (double) count, y / (double) count, z / (double) count);
    }

    public static double getAverageDistance(Iterable<? extends Vec4> points)
    {
        if ((points == null))
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double totalDistance = 0.0;
        int count = 0;

        for (Vec4 p1 : points)
        {
            for (Vec4 p2 : points)
            {
                if (p1 != p2)
                {
                    double d = p1.distanceTo3(p2);
                    totalDistance += d;
                    count++;
                }
            }
        }

        return (count == 0) ? 0.0 : (totalDistance / (double) count);
    }

    /**
     * Calculate the extrema of a given array of <code>Vec4</code>s. The resulting array is always of length 2, with the
     * first element containing the minimum extremum, and the second containing the maximum. The minimum extremum is
     * composed by taking the smallest x, y and z values from all the <code>Vec4</code>s in the array. These values are
     * not necessarily taken from the same <code>Vec4</code>. The maximum extrema is composed in the same fashion.
     *
     * @param points any array of <code>Vec4</code>s
     *
     * @return a array with length of 2, comprising the most extreme values in the given array
     *
     * @throws IllegalArgumentException if <code>points</code> is null
     */
    public static Vec4[] computeExtrema(Vec4 points[])
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.PointsArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (points.length == 0)
            return null;

        double xmin = points[0].x;
        double ymin = points[0].y;
        double zmin = points[0].z;
        double xmax = xmin;
        double ymax = ymin;
        double zmax = zmin;

        for (int i = 1; i < points.length; i++)
        {
            double x = points[i].x;
            if (x > xmax)
            {
                xmax = x;
            }
            else if (x < xmin)
            {
                xmin = x;
            }

            double y = points[i].y;
            if (y > ymax)
            {
                ymax = y;
            }
            else if (y < ymin)
            {
                ymin = y;
            }

            double z = points[i].z;
            if (z > zmax)
            {
                zmax = z;
            }
            else if (z < zmin)
            {
                zmin = z;
            }
        }

        return new Vec4[] {new Vec4(xmin, ymin, zmin), new Vec4(xmax, ymax, zmax)};
    }

    public static Vec4[] computeExtrema(BufferWrapper buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer.getBackingBuffer().position() > buffer.getBackingBuffer().limit() - 3)
            return null;

        double xmin = buffer.getDouble(0);
        double ymin = buffer.getDouble(1);
        double zmin = buffer.getDouble(2);
        double xmax = xmin;
        double ymax = ymin;
        double zmax = zmin;

        for (int i = 1; i < buffer.length() / 3; i++)
        {
            double x = buffer.getDouble(i * 3);
            if (x > xmax)
            {
                xmax = x;
            }
            else if (x < xmin)
            {
                xmin = x;
            }

            double y = buffer.getDouble(i * 3 + 1);
            if (y > ymax)
            {
                ymax = y;
            }
            else if (y < ymin)
            {
                ymin = y;
            }

            double z = buffer.getDouble(i * 3 + 2);
            if (z > zmax)
            {
                zmax = z;
            }
            else if (z < zmin)
            {
                zmin = z;
            }
        }

        return new Vec4[] {new Vec4(xmin, ymin, zmin), new Vec4(xmax, ymax, zmax)};
    }

    /**
     * Indicates whether three vectors are colinear.
     *
     * @param a the first vector.
     * @param b the second vector.
     * @param c the third vector.
     *
     * @return true if the vectors are colinear, otherwise false.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public static boolean areColinear(Vec4 a, Vec4 b, Vec4 c)
    {
        if (a == null || b == null || c == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 ab = b.subtract3(a).normalize3();
        Vec4 bc = c.subtract3(b).normalize3();

        return Math.abs(ab.dot3(bc)) > 0.999; // ab and bc are considered colinear if their dot product is near +/-1
    }
}
