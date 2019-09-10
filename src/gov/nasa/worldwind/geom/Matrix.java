/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: Matrix.java 2201 2014-08-07 23:17:54Z dcollins $
 */
public class Matrix
{
    public static final Matrix IDENTITY = new Matrix(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1,
        true);

    // Row 1
    public final double m11;
    public final double m12;
    public final double m13;
    public final double m14;
    // Row 2
    public final double m21;
    public final double m22;
    public final double m23;
    public final double m24;
    // Row 3
    public final double m31;
    public final double m32;
    public final double m33;
    public final double m34;
    // Row 4
    public final double m41;
    public final double m42;
    public final double m43;
    public final double m44;

    protected static final double EPSILON = 1.0e-6;
    protected static final double NEAR_ZERO_THRESHOLD = 1.0e-8;

    // 16 values in a 4x4 matrix.
    private static final int NUM_ELEMENTS = 16;
    // True when this matrix represents a 3D transform.
    private final boolean isOrthonormalTransform;
    // Cached computations.
    private int hashCode;

    public Matrix(double value)
    {
        // 'value' is placed in the diagonal.
        this(
            value, 0, 0, 0,
            0, value, 0, 0,
            0, 0, value, 0,
            0, 0, 0, value);
    }

    public Matrix(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44)
    {
        this(
            m11, m12, m13, m14,
            m21, m22, m23, m24,
            m31, m32, m33, m34,
            m41, m42, m43, m44,
            false);
    }

    Matrix(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44,
        boolean isOrthonormalTransform)
    {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
        this.isOrthonormalTransform = isOrthonormalTransform;
    }

    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Matrix that = (Matrix) obj;
        return (this.m11 == that.m11) && (this.m12 == that.m12) && (this.m13 == that.m13) && (this.m14 == that.m14)
            && (this.m21 == that.m21) && (this.m22 == that.m22) && (this.m23 == that.m23) && (this.m24 == that.m24)
            && (this.m31 == that.m31) && (this.m32 == that.m32) && (this.m33 == that.m33) && (this.m34 == that.m34)
            && (this.m41 == that.m41) && (this.m42 == that.m42) && (this.m43 == that.m43) && (this.m44 == that.m44);
    }

    public final int hashCode()
    {
        if (this.hashCode == 0)
        {
            int result;
            long tmp;
            tmp = Double.doubleToLongBits(this.m11);
            result = (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m12);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m13);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m14);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m21);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m22);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m23);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m24);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m31);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m32);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m33);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m34);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m41);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m42);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m43);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.m44);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            this.hashCode = result;
        }
        return this.hashCode;
    }

    public static Matrix fromArray(double[] compArray, int offset, boolean rowMajor)
    {
        if (compArray == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if ((compArray.length - offset) < NUM_ELEMENTS)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rowMajor)
        {
            //noinspection PointlessArithmeticExpression
            return new Matrix(
                // Row 1
                compArray[0 + offset],
                compArray[1 + offset],
                compArray[2 + offset],
                compArray[3 + offset],
                // Row 2
                compArray[4 + offset],
                compArray[5 + offset],
                compArray[6 + offset],
                compArray[7 + offset],
                // Row 3
                compArray[8 + offset],
                compArray[9 + offset],
                compArray[10 + offset],
                compArray[11 + offset],
                // Row 4
                compArray[12 + offset],
                compArray[13 + offset],
                compArray[14 + offset],
                compArray[15 + offset]);
        }
        else
        {
            //noinspection PointlessArithmeticExpression
            return new Matrix(
                // Row 1
                compArray[0 + offset],
                compArray[4 + offset],
                compArray[8 + offset],
                compArray[12 + offset],
                // Row 2
                compArray[1 + offset],
                compArray[5 + offset],
                compArray[9 + offset],
                compArray[13 + offset],
                // Row 3
                compArray[2 + offset],
                compArray[6 + offset],
                compArray[10 + offset],
                compArray[14 + offset],
                // Row 4
                compArray[3 + offset],
                compArray[7 + offset],
                compArray[11 + offset],
                compArray[15 + offset]);
        }
    }

    public final double[] toArray(double[] compArray, int offset, boolean rowMajor)
    {
        if (compArray == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if ((compArray.length - offset) < NUM_ELEMENTS)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rowMajor)
        {
            // Row 1
            //noinspection PointlessArithmeticExpression
            compArray[0 + offset] = this.m11;
            compArray[1 + offset] = this.m12;
            compArray[2 + offset] = this.m13;
            compArray[3 + offset] = this.m14;
            // Row 2
            compArray[4 + offset] = this.m21;
            compArray[5 + offset] = this.m22;
            compArray[6 + offset] = this.m23;
            compArray[7 + offset] = this.m24;
            // Row 3
            compArray[8 + offset] = this.m31;
            compArray[9 + offset] = this.m32;
            compArray[10 + offset] = this.m33;
            compArray[11 + offset] = this.m34;
            // Row 4
            compArray[12 + offset] = this.m41;
            compArray[13 + offset] = this.m42;
            compArray[14 + offset] = this.m43;
            compArray[15 + offset] = this.m44;
        }
        else
        {
            // Row 1
            //noinspection PointlessArithmeticExpression
            compArray[0 + offset] = this.m11;
            compArray[4 + offset] = this.m12;
            compArray[8 + offset] = this.m13;
            compArray[12 + offset] = this.m14;
            // Row 2
            compArray[1 + offset] = this.m21;
            compArray[5 + offset] = this.m22;
            compArray[9 + offset] = this.m23;
            compArray[13 + offset] = this.m24;
            // Row 3
            compArray[2 + offset] = this.m31;
            compArray[6 + offset] = this.m32;
            compArray[10 + offset] = this.m33;
            compArray[14 + offset] = this.m34;
            // Row 4
            compArray[3 + offset] = this.m41;
            compArray[7 + offset] = this.m42;
            compArray[11 + offset] = this.m43;
            compArray[15 + offset] = this.m44;
        }

        return compArray;
    }

    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.m11).append(", ").append(this.m12).append(", ").append(this.m13).append(", ").append(this.m14);
        sb.append(", \r\n");
        sb.append(this.m21).append(", ").append(this.m22).append(", ").append(this.m23).append(", ").append(this.m24);
        sb.append(", \r\n");
        sb.append(this.m31).append(", ").append(this.m32).append(", ").append(this.m33).append(", ").append(this.m34);
        sb.append(", \r\n");
        sb.append(this.m41).append(", ").append(this.m42).append(", ").append(this.m43).append(", ").append(this.m44);
        sb.append(")");
        return sb.toString();
    }

    public final double getM11()
    {
        return this.m11;
    }

    public final double getM12()
    {
        return this.m12;
    }

    public final double getM13()
    {
        return this.m13;
    }

    public final double getM14()
    {
        return this.m14;
    }

    public final double getM21()
    {
        return this.m21;
    }

    public final double getM22()
    {
        return this.m22;
    }

    public final double getM23()
    {
        return this.m23;
    }

    public final double getM24()
    {
        return this.m24;
    }

    public final double getM31()
    {
        return this.m31;
    }

    public final double getM32()
    {
        return this.m32;
    }

    public final double getM33()
    {
        return this.m33;
    }

    public final double getM34()
    {
        return this.m34;
    }

    public final double getM41()
    {
        return this.m41;
    }

    public final double getM42()
    {
        return this.m42;
    }

    public final double getM43()
    {
        return this.m43;
    }

    public final double getM44()
    {
        return this.m44;
    }

    public final double m11()
    {
        return this.m11;
    }

    public final double m12()
    {
        return this.m12;
    }

    public final double m13()
    {
        return this.m13;
    }

    public final double m14()
    {
        return this.m14;
    }

    public final double m21()
    {
        return this.m21;
    }

    public final double m22()
    {
        return this.m22;
    }

    public final double m23()
    {
        return this.m23;
    }

    public final double m24()
    {
        return this.m24;
    }

    public final double m31()
    {
        return this.m31;
    }

    public final double m32()
    {
        return this.m32;
    }

    public final double m33()
    {
        return this.m33;
    }

    public final double m34()
    {
        return this.m34;
    }

    public final double m41()
    {
        return this.m41;
    }

    public final double m42()
    {
        return this.m42;
    }

    public final double m43()
    {
        return this.m43;
    }

    public final double m44()
    {
        return this.m44;
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    /**
     * Returns a Cartesian transform <code>Matrix</code> that maps a local orientation to model coordinates. The
     * orientation is specified by an array of three <code>axes</code>. The <code>axes</code> array must contain three
     * non-null vectors, which are interpreted in the following order: x-axis, y-axis, z-axis. This ensures that the
     * axes in the returned <code>Matrix</code> have unit length and are orthogonal to each other.
     *
     * @param axes an array must of three non-null vectors defining a local orientation in the following order: x-axis,
     *             y-axis, z-axis.
     *
     * @return a <code>Matrix</code> that a transforms local coordinates to world coordinates.
     *
     * @throws IllegalArgumentException if <code>axes</code> is <code>null</code>, if <code>axes</code> contains less
     *                                  than three elements, or if any of the first three elements in <code>axes</code>
     *                                  is <code>null</code>.
     */
    public static Matrix fromAxes(Vec4[] axes)
    {
        if (axes == null)
        {
            String msg = Logging.getMessage("nullValue.AxesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axes.length < 3)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", axes.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axes[0] == null || axes[1] == null || axes[2] == null)
        {
            String msg = Logging.getMessage("nullValue.AxesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 s = axes[0].normalize3();
        Vec4 f = s.cross3(axes[1]).normalize3();
        Vec4 u = f.cross3(s).normalize3();

        return new Matrix(
            s.x, u.x, f.x, 0.0,
            s.y, u.y, f.y, 0.0,
            s.z, u.z, f.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
            true);
    }

    public static Matrix fromAxisAngle(Angle angle, Vec4 axis)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (axis == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromAxisAngle(angle, axis.x, axis.y, axis.z, true);
    }

    public static Matrix fromAxisAngle(Angle angle, double axisX, double axisY, double axisZ)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        return fromAxisAngle(angle, axisX, axisY, axisZ, true);
    }

    private static Matrix fromAxisAngle(Angle angle, double axisX, double axisY, double axisZ, boolean normalize)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (normalize)
        {
            double length = Math.sqrt((axisX * axisX) + (axisY * axisY) + (axisZ * axisZ));
            if (!isZero(length) && (length != 1.0))
            {
                axisX /= length;
                axisY /= length;
                axisZ /= length;
            }
        }

        double c = angle.cos();
        double s = angle.sin();
        double one_minus_c = 1.0 - c;
        return new Matrix(
            // Row 1
            c + (one_minus_c * axisX * axisX),
            (one_minus_c * axisX * axisY) - (s * axisZ),
            (one_minus_c * axisX * axisZ) + (s * axisY),
            0.0,
            // Row 2
            (one_minus_c * axisX * axisY) + (s * axisZ),
            c + (one_minus_c * axisY * axisY),
            (one_minus_c * axisY * axisZ) - (s * axisX),
            0.0,
            // Row 3
            (one_minus_c * axisX * axisZ) - (s * axisY),
            (one_minus_c * axisY * axisZ) + (s * axisX),
            c + (one_minus_c * axisZ * axisZ),
            0.0,
            // Row 4
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromQuaternion(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromQuaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w, true);
    }

    private static Matrix fromQuaternion(double x, double y, double z, double w, boolean normalize)
    {
        if (normalize)
        {
            double length = Math.sqrt((x * x) + (y * y) + (z * z) + (w * w));
            if (!isZero(length) && (length != 1.0))
            {
                x /= length;
                y /= length;
                z /= length;
                w /= length;
            }
        }

        return new Matrix(
            // Row 1
            1.0 - (2.0 * y * y) - (2.0 * z * z),
            (2.0 * x * y) - (2.0 * z * w),
            (2.0 * x * z) + (2.0 * y * w),
            0.0,
            // Row 2
            (2.0 * x * y) + (2.0 * z * w),
            1.0 - (2.0 * x * x) - (2.0 * z * z),
            (2.0 * y * z) - (2.0 * x * w),
            0.0,
            // Row 3
            (2.0 * x * z) - (2.0 * y * w),
            (2.0 * y * z) + (2.0 * x * w),
            1.0 - (2.0 * x * x) - (2.0 * y * y),
            0.0,
            // Row 4
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationXYZ(Angle xRotation, Angle yRotation, Angle zRotation)
    {
        if ((xRotation == null) || (yRotation == null) || (zRotation == null))
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double cx = xRotation.cos();
        double cy = yRotation.cos();
        double cz = zRotation.cos();
        double sx = xRotation.sin();
        double sy = yRotation.sin();
        double sz = zRotation.sin();
        return new Matrix(
            cy * cz, -cy * sz, sy, 0.0,
            (sx * sy * cz) + (cx * sz), -(sx * sy * sz) + (cx * cz), -sx * cy, 0.0,
            -(cx * sy * cz) + (sx * sz), (cx * sy * sz) + (sx * cz), cx * cy, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationX(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();
        return new Matrix(
            1.0, 0.0, 0.0, 0.0,
            0.0, c, -s, 0.0,
            0.0, s, c, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationY(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();
        return new Matrix(
            c, 0.0, s, 0.0,
            0.0, 1.0, 0.0, 0.0,
            -s, 0.0, c, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromRotationZ(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();
        return new Matrix(
            c, -s, 0.0, 0.0,
            s, c, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Rotation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromScale(double scale)
    {
        return fromScale(scale, scale, scale);
    }

    public static Matrix fromScale(Vec4 scale)
    {
        if (scale == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromScale(scale.x, scale.y, scale.z);
    }

    public static Matrix fromScale(double scaleX, double scaleY, double scaleZ)
    {
        return new Matrix(
            scaleX, 0.0, 0.0, 0.0,
            0.0, scaleY, 0.0, 0.0,
            0.0, 0.0, scaleZ, 0.0,
            0.0, 0.0, 0.0, 1.0,
            // Scale matrices are non-orthogonal, 3D transforms.
            false);
    }

    public static Matrix fromTranslation(Vec4 translation)
    {
        if (translation == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromTranslation(translation.x, translation.y, translation.z);
    }

    public static Matrix fromTranslation(double x, double y, double z)
    {
        return new Matrix(
            1.0, 0.0, 0.0, x,
            0.0, 1.0, 0.0, y,
            0.0, 0.0, 1.0, z,
            0.0, 0.0, 0.0, 1.0,
            // Translation matrices are orthogonal, 3D transforms.
            true);
    }

    public static Matrix fromSkew(Angle theta, Angle phi)
    {
        // from http://faculty.juniata.edu/rhodes/graphics/projectionmat.htm

        double cotTheta = 1.0e6;
        double cotPhi = 1.0e6;

        if (theta.getRadians() < EPSILON && phi.getRadians() < EPSILON)
        {
            cotTheta = 0;
            cotPhi = 0;
        }
        else
        {
            if (Math.abs(Math.tan(theta.getRadians())) > EPSILON)
                cotTheta = 1 / Math.tan(theta.getRadians());
            if (Math.abs(Math.tan(phi.getRadians())) > EPSILON)
                cotPhi = 1 / Math.tan(phi.getRadians());
        }

        return new Matrix(
            1.0, 0.0, -cotTheta, 0,
            0.0, 1.0, -cotPhi, 0,
            0.0, 0.0, 1.0, 0,
            0.0, 0.0, 0.0, 1.0,
            false);
    }

    /**
     * Returns a Cartesian transform <code>Matrix</code> that maps a local origin and orientation to model coordinates.
     * The transform is specified by a local <code>origin</code> and an array of three <code>axes</code>. The
     * <code>axes</code> array must contain three non-null vectors, which are interpreted in the following order:
     * x-axis, y-axis, z-axis. This ensures that the axes in the returned <code>Matrix</code> have unit length and are
     * orthogonal to each other.
     *
     * @param origin the origin of the local coordinate system.
     * @param axes   an array must of three non-null vectors defining a local orientation in the following order:
     *               x-axis, y-axis, z-axis.
     *
     * @return a <code>Matrix</code> that transforms local coordinates to world coordinates.
     *
     * @throws IllegalArgumentException if <code>origin</code> is <code>null</code>, if <code>axes</code> is
     *                                  <code>null</code>, if <code>axes</code> contains less than three elements, or if
     *                                  any of the first three elements in <code>axes</code> is <code>null</code>.
     */
    public static Matrix fromLocalOrientation(Vec4 origin, Vec4[] axes)
    {
        if (origin == null)
        {
            String msg = Logging.getMessage("nullValue.OriginIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axes == null)
        {
            String msg = Logging.getMessage("nullValue.AxesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axes.length < 3)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", axes.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axes[0] == null || axes[1] == null || axes[2] == null)
        {
            String msg = Logging.getMessage("nullValue.AxesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromTranslation(origin).multiply(fromAxes(axes));
    }

    /**
     * Returns a viewing matrix in model coordinates defined by the specified View eye point, reference point indicating
     * the center of the scene, and up vector. The eye point, center point, and up vector are in model coordinates. The
     * returned viewing matrix maps the reference center point to the negative Z axis, and the eye point to the origin,
     * and the up vector to the positive Y axis. When this matrix is used to define an OGL viewing transform along with
     * a typical projection matrix such as {@link #fromPerspective(Angle, double, double, double, double)} , this maps
     * the center of the scene to the center of the viewport, and maps the up vector to the viewoport's positive Y axis
     * (the up vector points up in the viewport). The eye point and reference center point must not be coincident, and
     * the up vector must not be parallel to the line of sight (the vector from the eye point to the reference center
     * point).
     *
     * @param eye    the eye point, in model coordinates.
     * @param center the scene's reference center point, in model coordinates.
     * @param up     the direction of the up vector, in model coordinates.
     *
     * @return a viewing matrix in model coordinates defined by the specified eye point, reference center point, and up
     *         vector.
     *
     * @throws IllegalArgumentException if any of the eye point, reference center point, or up vector are null, if the
     *                                  eye point and reference center point are coincident, or if the up vector and the
     *                                  line of sight are parallel.
     */
    public static Matrix fromViewLookAt(Vec4 eye, Vec4 center, Vec4 up)
    {
        if (eye == null || center == null || up == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (eye.distanceTo3(center) <= EPSILON)
        {
            String msg = Logging.getMessage("Geom.EyeAndCenterInvalid", eye, center);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 forward = center.subtract3(eye);
        Vec4 f = forward.normalize3();

        Vec4 s = f.cross3(up);
        s = s.normalize3();

        if (s.getLength3() <= EPSILON)
        {
            String msg = Logging.getMessage("Geom.UpAndLineOfSightInvalid", up, forward);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 u = s.cross3(f);
        u = u.normalize3();

        Matrix mAxes = new Matrix(
            s.x, s.y, s.z, 0.0,
            u.x, u.y, u.z, 0.0,
            -f.x, -f.y, -f.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
            true);
        Matrix mEye = Matrix.fromTranslation(
            -eye.x, -eye.y, -eye.z);
        return mAxes.multiply(mEye);
    }

    /**
     * Returns a local origin transform matrix in model coordinates defined by the specified eye point, reference point
     * indicating the center of the local scene, and up vector. The eye point, center point, and up vector are in model
     * coordinates. The returned viewing matrix maps the the positive Z axis to the reference center point, the origin
     * to the eye point, and the positive Y axis to the up vector. The eye point and reference center point must not be
     * coincident, and the up vector must not be parallel to the line of sight (the vector from the eye point to the
     * reference center point).
     *
     * @param eye    the eye point, in model coordinates.
     * @param center the scene's reference center point, in model coordinates.
     * @param up     the direction of the up vector, in model coordinates.
     *
     * @return a viewing matrix in model coordinates defined by the specified eye point, reference center point, and up
     *         vector.
     *
     * @throws IllegalArgumentException if any of the eye point, reference center point, or up vector are null, if the
     *                                  eye point and reference center point are coincident, or if the up vector and the
     *                                  line of sight are parallel.
     */
    public static Matrix fromModelLookAt(Vec4 eye, Vec4 center, Vec4 up)
    {
        if (eye == null || center == null || up == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (eye.distanceTo3(center) <= EPSILON)
        {
            String msg = Logging.getMessage("Geom.EyeAndCenterInvalid", eye, center);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 forward = center.subtract3(eye);
        Vec4 f = forward.normalize3();

        Vec4 s = up.cross3(f);
        s = s.normalize3();

        if (s.getLength3() <= EPSILON)
        {
            String msg = Logging.getMessage("Geom.UpAndLineOfSightInvalid", up, forward);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 u = f.cross3(s);
        u = u.normalize3();

        Matrix mAxes = new Matrix(
            s.x, u.x, f.x, 0.0,
            s.y, u.y, f.y, 0.0,
            s.z, u.z, f.z, 0.0,
            0.0, 0.0, 0.0, 1.0,
            true);
        Matrix mEye = Matrix.fromTranslation(
            eye.x, eye.y, eye.z);
        return mEye.multiply(mAxes);
    }

    public static Matrix fromPerspective(Angle horizontalFieldOfView, double viewportWidth, double viewportHeight,
        double near, double far)
    {
        if (horizontalFieldOfView == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double fovX = horizontalFieldOfView.degrees;
        if (fovX <= 0.0 || fovX > 180.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", "horizontalFieldOfView=" + fovX);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (viewportWidth <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", "viewportWidth=" + viewportWidth);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (viewportHeight <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", "viewportHeight=" + viewportHeight);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (near <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", "near=" + near);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (far <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", "far=" + far);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (far <= near)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", "far=" + far + ",near=" + near);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double f = 1.0 / horizontalFieldOfView.tanHalfAngle();
        // We are using *horizontal* field-of-view here. This results in a different matrix than documented in sources
        // using vertical field-of-view.
        return new Matrix(
            f, 0.0, 0.0, 0.0,
            0.0, (f * viewportWidth) / viewportHeight, 0.0, 0.0,
            0.0, 0.0, -(far + near) / (far - near), -(2.0 * far * near) / (far - near),
            0.0, 0.0, -1.0, 0.0);
    }

    public static Matrix fromPerspective(double width, double height, double near, double far)
    {
        if (width <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", width);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (height <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", height);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (near <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", near);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (far <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", far);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (far <= near)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", far);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            2.0 / width, 0.0, 0.0, 0.0,
            0.0, (2.0 * near) / height, 0.0, 0.0,
            0.0, 0.0, -(far + near) / (far - near), -(2.0 * far * near) / (far - near),
            0.0, 0.0, -1.0, 0.0);
    }

    public static Matrix fromOrthographic(double width, double height, double near, double far)
    {
        if (width <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", width);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (height <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", height);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (near <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", near);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (far <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", far);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (far <= near)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", far);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            2.0 / width, 0.0, 0.0, 0.0,
            0.0, 2.0 / height, 0.0, 0.0,
            0.0, 0.0, -2.0 / (far - near), -(far + near) / (far - near),
            0.0, 0.0, 0.0, 1.0);
    }

    public static Matrix fromOrthographic2D(double width, double height)
    {
        if (width <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", width);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (height <= 0.0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", height);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            2.0 / width, 0.0, 0.0, 0.0,
            0.0, 2.0 / height, 0.0, 0.0,
            0.0, 0.0, -1.0, 0.0,
            0.0, 0.0, 0.0, 1.0);
    }

    /**
     * Computes a <code>Matrix</code> that will map a aligned 2D grid coordinates to geographic coordinates in degrees.
     * It is assumed that the destination grid is parallel with lines of latitude and longitude, and has its origin in
     * the upper left hand corner.
     *
     * @param sector      the grid sector.
     * @param imageWidth  the grid width.
     * @param imageHeight the grid height.
     *
     * @return <code>Matrix</code> that will map from grid coordinates to geographic coordinates in degrees.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null, or if either <code>width</code> or
     *                                  <code>height</code> are less than 1.
     */
    public static Matrix fromImageToGeographic(int imageWidth, int imageHeight, Sector sector)
    {
        if (imageWidth < 1 || imageHeight < 1)
        {
            String message = Logging.getMessage("generic.InvalidImageSize", imageWidth, imageHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Transform from grid coordinates to geographic coordinates. Since the grid is parallel with lines of latitude
        // and longitude, this is a simple scale and translation.

        double sx = sector.getDeltaLonDegrees() / imageWidth;
        double sy = -sector.getDeltaLatDegrees() / imageHeight;
        double tx = sector.getMinLongitude().degrees;
        double ty = sector.getMaxLatitude().degrees;

        return new Matrix(
            sx, 0.0, tx, 0.0,
            0.0, sy, ty, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 0.0);
    }

    public static Matrix fromImageToGeographic(AVList worldFileParams)
    {
        if (worldFileParams == null)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Transform from geographic coordinates to source grid coordinates. Start with the following system of
        // equations. The values a-f are defined by the world file, which construct and affine transform mapping grid
        // coordinates to geographic coordinates. We can simply plug these into the upper 3x3 values of our matrix.
        //
        // | a b c |   | x |   | lon |
        // | d e f | * | y | = | lat |
        // | 0 0 1 |   | 1 |   | 1   |

        Double a = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_X_PIXEL_SIZE);
        Double d = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_Y_COEFFICIENT);
        Double b = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_X_COEFFICIENT);
        Double e = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_Y_PIXEL_SIZE);
        Double c = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_X_LOCATION);
        Double f = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_Y_LOCATION);

        if (a == null || b == null || c == null || d == null || e == null || f == null)
        {
            return null;
        }

        return new Matrix(
            a, b, c, 0.0,
            d, e, f, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 0.0);
    }

    public static Matrix fromGeographicToImage(AVList worldFileParams)
    {
        if (worldFileParams == null)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Transform from geographic coordinates to source grid coordinates. Start with the following system of
        // equations. The values a-f are defined by the world file, which construct and affine transform mapping grid
        // coordinates to geographic coordinates. We want to find the transform that maps geographic coordinates to
        // grid coordinates.
        //
        // | a b c |   | x |   | lon |
        // | d e f | * | y | = | lat |
        // | 0 0 1 |   | 1 |   | 1   |
        //
        // Expanding the matrix multiplication:
        //
        // a*x + b*y + c = lon
        // d*x + e*y + f = lat
        //
        // Then solving for x and y by eliminating variables:
        //
        // x0 = d - (e*a)/b
        // y0 = e - (d*b)/a
        // (-e/(b*x0))*lon + (1/x0)*lat + (e*c)/(b*x0) - f/x0 = x
        // (-d/(a*y0))*lon + (1/y0)*lat + (d*c)/(a*y0) - f/y0 = y
        //
        // And extracting new the matrix coefficients a'-f':
        //
        // a' = -e/(b*x0)
        // b' = 1/x0
        // c' = (e*c)/(b*x0) - f/x0
        // d' = -d/(a*y0)
        // e' = 1/y0
        // f' = (d*c)/(a*y0) - f/y0
        //
        // If b==0 and d==0, then we have the equation simplifies to:
        //
        // (1/a)*lon + (-c/a) = x
        // (1/e)*lat + (-f/e) = y
        //
        // And and the new matrix coefficients will be:
        //
        // a' = 1/a
        // b' = 0
        // c' = -c/a
        // d' = 0
        // e' = 1/e
        // f' = -f/e

        Double a = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_X_PIXEL_SIZE);
        Double d = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_Y_COEFFICIENT);
        Double b = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_X_COEFFICIENT);
        Double e = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_Y_PIXEL_SIZE);
        Double c = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_X_LOCATION);
        Double f = AVListImpl.getDoubleValue(worldFileParams, WorldFile.WORLD_FILE_Y_LOCATION);

        if (a == null || b == null || c == null || d == null || e == null || f == null)
        {
            return null;
        }

        if (b == 0.0 && d == 0.0)
        {
            return new Matrix(
                1.0 / a, 0.0, (-c / a), 0.0,
                0.0, 1.0 / e, (-f / e), 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 0.0);
        }
        else
        {
            double x0 = d - (e * a) / b;
            double ap = -e / (b * x0);
            double bp = 1.0 / x0;
            double cp = (e * c) / (b * x0) - f / x0;

            double y0 = e - (d * b) / a;
            double dp = -d / (a * y0);
            double ep = 1.0 / y0;
            double fp = (d * c) / (a * y0) - f / y0;

            return new Matrix(
                ap, bp, cp, 0.0,
                dp, ep, fp, 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Computes a <code>Matrix</code> that will map constrained 2D grid coordinates to geographic coordinates in
     * degrees. The grid is defined by three control points. Each control point maps a location in the source grid to a
     * geographic location.
     *
     * @param imagePoints three control points in the source grid.
     * @param geoPoints   three geographic locations corresponding to each grid control point.
     *
     * @return <code>Matrix</code> that will map from geographic coordinates to grid coordinates in degrees.
     *
     * @throws IllegalArgumentException if either <code>imagePoints</code> or <code>geoPoints</code> is null or have
     *                                  length less than 3.
     */
    public static Matrix fromImageToGeographic(java.awt.geom.Point2D[] imagePoints, LatLon[] geoPoints)
    {
        if (imagePoints == null)
        {
            String message = Logging.getMessage("nullValue.ImagePointsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (geoPoints == null)
        {
            String message = Logging.getMessage("nullValue.GeoPointsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (imagePoints.length < 3)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "imagePoints.length < 3");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (geoPoints.length < 3)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "geoPoints.length < 3");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Transform from geographic coordinates to source grid coordinates. Start with the following system of
        // equations. The values a-f are the unknown coefficients we want to derive, The (lat,lon) and (x,y)
        // coordinates are constants defined by the caller via geoPoints and imagePoints, respectively.
        //
        // | a b c |   | x1 x2 x3 |   | lon1 lon2 lon3 |
        // | d e f | * | y1 y2 y3 | = | lat1 lat2 lat3 |
        // | 0 0 1 |   | 1  1  1  |   | 1    1    1    |
        //
        // Expanding the matrix multiplication:
        //
        // a*x1 + b*y1 + c = lon1
        // a*x2 + b*y2 + c = lon2
        // a*x3 + b*y3 + c = lon3
        // d*x1 + e*y1 + f = lat1
        // d*x2 + e*y2 + f = lat2
        // d*x3 + e*y3 + f = lat3
        //
        // Then solving for a-c, and d-f by repeatedly eliminating variables:
        //
        // a0 = (x3-x1) - (x2-x1)*(y3-y1)/(y2-y1)
        // a = (1/a0) * [(lon3-lon1) - (lon2-lon1)*(y3-y1)/(y2-y1)]
        // b = (lon2-lon1)/(y2-y1) - a*(x2-x1)/(y2-y1)
        // c = lon1 - a*x1 - b*y1
        //
        // d0 = (x3-x1) - (x2-x1)*(y3-y1)/(y2-y1)
        // d = (1/d0) * [(lat3-lat1) - (lat2-lat1)*(y3-y1)/(y2-y1)]
        // e = (lat2-lat1)/(y2-y1) - d*(x2-x1)/(y2-y1)
        // f = lat1 - d*x1 - e*y1

        double lat1 = geoPoints[0].getLatitude().degrees;
        double lat2 = geoPoints[1].getLatitude().degrees;
        double lat3 = geoPoints[2].getLatitude().degrees;
        double lon1 = geoPoints[0].getLongitude().degrees;
        double lon2 = geoPoints[1].getLongitude().degrees;
        double lon3 = geoPoints[2].getLongitude().degrees;

        double x1 = imagePoints[0].getX();
        double x2 = imagePoints[1].getX();
        double x3 = imagePoints[2].getX();
        double y1 = imagePoints[0].getY();
        double y2 = imagePoints[1].getY();
        double y3 = imagePoints[2].getY();

        double a0 = (x3 - x1) - (x2 - x1) * (y3 - y1) / (y2 - y1);
        double a = (1 / a0) * ((lon3 - lon1) - (lon2 - lon1) * (y3 - y1) / (y2 - y1));
        double b = (lon2 - lon1) / (y2 - y1) - a * (x2 - x1) / (y2 - y1);
        double c = lon1 - a * x1 - b * y1;

        double d0 = (x3 - x1) - (x2 - x1) * (y3 - y1) / (y2 - y1);
        double d = (1 / d0) * ((lat3 - lat1) - (lat2 - lat1) * (y3 - y1) / (y2 - y1));
        double e = (lat2 - lat1) / (y2 - y1) - d * (x2 - x1) / (y2 - y1);
        double f = lat1 - d * x1 - e * y1;

        return new Matrix(
            a, b, c, 0.0,
            d, e, f, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 0.0);
    }

    public static Matrix fromGeographicToImage(java.awt.geom.Point2D[] imagePoints, LatLon[] geoPoints)
    {
        if (imagePoints == null)
        {
            String message = Logging.getMessage("nullValue.ImagePointsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (geoPoints == null)
        {
            String message = Logging.getMessage("nullValue.GeoPointsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (imagePoints.length < 3)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "imagePoints.length < 3");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (geoPoints.length < 3)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "geoPoints.length < 3");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Transform from geographic coordinates to source grid coordinates. Start with the following system of
        // equations. The values a-f are the unknown coefficients we want to derive, The (lat,lon) and (x,y)
        // coordinates are constants defined by the caller via geoPoints and imagePoints, respectively.
        //
        // | a b c |   | lon1 lon2 lon3 |   | x1 x2 x3 |
        // | d e f | * | lat1 lat2 lat3 | = | y1 y2 y3 |
        // | 0 0 1 |   | 1    1    1    |   | 1  1  1  |
        //
        // Expanding the matrix multiplication:
        //
        // a*lon1 + b*lat1 + c = x1
        // a*lon2 + b*lat2 + c = x2
        // a*lon3 + b*lat3 + c = x3
        // d*lon1 + e*lat1 + f = y1
        // d*lon2 + e*lat2 + f = y2
        // d*lon3 + e*lat3 + f = y3
        //
        // Then solving for a-c, and d-f by repeatedly eliminating variables:
        //
        // a0 = (lon3-lon1) - (lon2-lon1)*(lat3-lat1)/(lat2-lat1)
        // a = (1/a0) * [(x3-x1) - (x2-x1)*(lat3-lat1)/(lat2-lat1)]
        // b = (x2-x1)/(lat2-lat1) - a*(lon2-lon1)/(lat2-lat1)
        // c = x1 - a*lon1 - b*lat1
        //
        // d0 = (lon3-lon1) - (lon2-lon1)*(lat3-lat1)/(lat2-lat1)
        // d = (1/d0) * [(y3-y1) - (y2-y1)*(lat3-lat1)/(lat2-lat1)]
        // e = (y2-y1)/(lat2-lat1) - d*(lon2-lon1)/(lat2-lat1)
        // f = y1 - d*lon1 - e*lat1

        double lat1 = geoPoints[0].getLatitude().degrees;
        double lat2 = geoPoints[1].getLatitude().degrees;
        double lat3 = geoPoints[2].getLatitude().degrees;
        double lon1 = geoPoints[0].getLongitude().degrees;
        double lon2 = geoPoints[1].getLongitude().degrees;
        double lon3 = geoPoints[2].getLongitude().degrees;

        double x1 = imagePoints[0].getX();
        double x2 = imagePoints[1].getX();
        double x3 = imagePoints[2].getX();
        double y1 = imagePoints[0].getY();
        double y2 = imagePoints[1].getY();
        double y3 = imagePoints[2].getY();

        double a0 = (lon3 - lon1) - (lon2 - lon1) * (lat3 - lat1) / (lat2 - lat1);
        double a = (1 / a0) * ((x3 - x1) - (x2 - x1) * (lat3 - lat1) / (lat2 - lat1));
        double b = (x2 - x1) / (lat2 - lat1) - a * (lon2 - lon1) / (lat2 - lat1);
        double c = x1 - a * lon1 - b * lat1;

        double d0 = (lon3 - lon1) - (lon2 - lon1) * (lat3 - lat1) / (lat2 - lat1);
        double d = (1 / d0) * ((y3 - y1) - (y2 - y1) * (lat3 - lat1) / (lat2 - lat1));
        double e = (y2 - y1) / (lat2 - lat1) - d * (lon2 - lon1) / (lat2 - lat1);
        double f = y1 - d * lon1 - e * lat1;

        return new Matrix(
            a, b, c, 0.0,
            d, e, f, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Computes a Matrix that will map the geographic region defined by sector onto a Cartesian region of the specified
     * <code>width</code> and <code>height</code> and centered at the point <code>(x, y)</code>.
     *
     * @param sector the geographic region which will be mapped to the Cartesian region
     * @param x      x-coordinate of lower left hand corner of the Cartesian region
     * @param y      y-coordinate of lower left hand corner of the Cartesian region
     * @param width  width of the Cartesian region, extending to the right from the x-coordinate
     * @param height height of the Cartesian region, extending up from the y-coordinate
     *
     * @return Matrix that will map from the geographic region to the Cartesian region.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null, or if <code>width</code> or <code>height</code>
     *                                  are less than zero.
     */
    public static Matrix fromGeographicToViewport(Sector sector, int x, int y, int width, int height)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(
            Matrix.fromTranslation(-x, -y, 0.0));
        transform = transform.multiply(
            Matrix.fromScale(width / sector.getDeltaLonDegrees(), height / sector.getDeltaLatDegrees(), 1.0));
        transform = transform.multiply(
            Matrix.fromTranslation(-sector.getMinLongitude().degrees, -sector.getMinLatitude().degrees, 0.0));

        return transform;
    }

    /**
     * Computes a Matrix that will map a Cartesian region of the specified <code>width</code> and <code>height</code>
     * and centered at the point <code>(x, y)</code> to the geographic region defined by sector onto .
     *
     * @param sector the geographic region the Cartesian region will be mapped to
     * @param x      x-coordinate of lower left hand corner of the Cartesian region
     * @param y      y-coordinate of lower left hand corner of the Cartesian region
     * @param width  width of the Cartesian region, extending to the right from the x-coordinate
     * @param height height of the Cartesian region, extending up from the y-coordinate
     *
     * @return Matrix that will map from Cartesian region to the geographic region.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null, or if <code>width</code> or <code>height</code>
     *                                  are less than zero.
     */
    public static Matrix fromViewportToGeographic(Sector sector, int x, int y, int width, int height)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix transform = Matrix.IDENTITY;
        transform = transform.multiply(
            Matrix.fromTranslation(sector.getMinLongitude().degrees, sector.getMinLatitude().degrees, 0.0));
        transform = transform.multiply(
            Matrix.fromScale(sector.getDeltaLonDegrees() / width, sector.getDeltaLatDegrees() / height, 1.0));
        transform = transform.multiply(
            Matrix.fromTranslation(x, y, 0.0));

        return transform;
    }

    /**
     * Computes a symmetric covariance Matrix from the x, y, z coordinates of the specified points Iterable. This
     * returns null if the points Iterable is empty, or if all of the points are null.
     * <p>
     * The returned covariance matrix represents the correlation between each pair of x-, y-, and z-coordinates as
     * they're distributed about the point Iterable's arithmetic mean. Its layout is as follows:
     * <p>
     * <code> C(x, x)  C(x, y)  C(x, z) <br> C(x, y)  C(y, y)  C(y, z) <br> C(x, z)  C(y, z)  C(z, z) </code>
     * <p>
     * C(i, j) is the covariance of coordinates i and j, where i or j are a coordinate's dispersion about its mean
     * value. If any entry is zero, then there's no correlation between the two coordinates defining that entry. If the
     * returned matrix is diagonal, then all three coordinates are uncorrelated, and the specified point Iterable is
     * distributed evenly about its mean point.
     *
     * @param points the Iterable of points for which to compute a Covariance matrix.
     *
     * @return the covariance matrix for the iterable of 3D points.
     *
     * @throws IllegalArgumentException if the points Iterable is null.
     */
    public static Matrix fromCovarianceOfVertices(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 mean = Vec4.computeAveragePoint(points);
        if (mean == null)
            return null;

        int count = 0;
        double c11 = 0d;
        double c22 = 0d;
        double c33 = 0d;
        double c12 = 0d;
        double c13 = 0d;
        double c23 = 0d;

        for (Vec4 vec : points)
        {
            if (vec == null)
                continue;

            count++;
            c11 += (vec.x - mean.x) * (vec.x - mean.x);
            c22 += (vec.y - mean.y) * (vec.y - mean.y);
            c33 += (vec.z - mean.z) * (vec.z - mean.z);
            c12 += (vec.x - mean.x) * (vec.y - mean.y); // c12 = c21
            c13 += (vec.x - mean.x) * (vec.z - mean.z); // c13 = c31
            c23 += (vec.y - mean.y) * (vec.z - mean.z); // c23 = c32
        }

        if (count == 0)
            return null;

        return new Matrix(
            c11 / (double) count, c12 / (double) count, c13 / (double) count, 0d,
            c12 / (double) count, c22 / (double) count, c23 / (double) count, 0d,
            c13 / (double) count, c23 / (double) count, c33 / (double) count, 0d,
            0d, 0d, 0d, 0d);
    }

    /**
     * Computes a symmetric covariance Matrix from the x, y, z coordinates of the specified buffer of points. This
     * returns null if the buffer is empty.
     * <p>
     * The returned covariance matrix represents the correlation between each pair of x-, y-, and z-coordinates as
     * they're distributed about the points arithmetic mean. Its layout is as follows:
     * <p>
     * <code> C(x, x)  C(x, y)  C(x, z) <br> C(x, y)  C(y, y)  C(y, z) <br> C(x, z)  C(y, z)  C(z, z) </code>
     * <p>
     * C(i, j) is the covariance of coordinates i and j, where i or j are a coordinate's dispersion about its mean
     * value. If any entry is zero, then there's no correlation between the two coordinates defining that entry. If the
     * returned matrix is diagonal, then all three coordinates are uncorrelated, and the specified points are
     * distributed evenly about their mean point.
     * <p>
     * The buffer must contain XYZ coordinate tuples which are either tightly packed or offset by the specified stride.
     * The stride specifies the number of buffer elements between the first coordinate of consecutive tuples. For
     * example, a stride of 3 specifies that each tuple is tightly packed as XYZXYZXYZ, whereas a stride of 5 specifies
     * that there are two elements between each tuple as XYZabXYZab (the elements "a" and "b" are ignored). The stride
     * must be at least 3. If the buffer's length is not evenly divisible into stride-sized tuples, this ignores the
     * remaining elements that follow the last complete tuple.
     *
     * @param coordinates the buffer containing the point coordinates for which to compute a Covariance matrix.
     * @param stride      the number of elements between the first coordinate of consecutive points. If stride is 3,
     *                    this interprets the buffer has having tightly packed XYZ coordinate tuples.
     *
     * @return the covariance matrix for the buffer of points.
     *
     * @throws IllegalArgumentException if the buffer is null, or if the stride is less than three.
     */
    public static Matrix fromCovarianceOfVertices(BufferWrapper coordinates, int stride)
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

        Vec4 mean = Vec4.computeAveragePoint3(coordinates, stride);
        if (mean == null)
            return null;

        int count = 0;
        double c11 = 0d;
        double c22 = 0d;
        double c33 = 0d;
        double c12 = 0d;
        double c13 = 0d;
        double c23 = 0d;

        for (int i = 0; i <= coordinates.length() - stride; i += stride)
        {
            double x = coordinates.getDouble(i);
            double y = coordinates.getDouble(i + 1);
            double z = coordinates.getDouble(i + 2);
            count++;
            c11 += (x - mean.x) * (x - mean.x);
            c22 += (y - mean.y) * (y - mean.y);
            c33 += (z - mean.z) * (z - mean.z);
            c12 += (x - mean.x) * (y - mean.y); // c12 = c21
            c13 += (x - mean.x) * (z - mean.z); // c13 = c31
            c23 += (y - mean.y) * (z - mean.z); // c23 = c32
        }

        if (count == 0)
            return null;

        return new Matrix(
            c11 / (double) count, c12 / (double) count, c13 / (double) count, 0d,
            c12 / (double) count, c22 / (double) count, c23 / (double) count, 0d,
            c13 / (double) count, c23 / (double) count, c33 / (double) count, 0d,
            0d, 0d, 0d, 0d);
    }

    /**
     * Computes the eigensystem of the specified symmetric Matrix's upper 3x3 matrix. If the Matrix's upper 3x3 matrix
     * is not symmetric, this throws an IllegalArgumentException. This writes the eigensystem parameters to the
     * specified arrays <code>outEigenValues</code> and <code>outEigenVectors</code>, placing the eigenvalues in the
     * entries of array <code>outEigenValues</code>, and the corresponding eigenvectors in the entires of array
     * <code>outEigenVectors</code>. These arrays must be non-null, and have length three or greater.
     *
     * @param matrix          the symmetric Matrix for which to compute an eigensystem.
     * @param outEigenvalues  the array which receives the three output eigenvalues.
     * @param outEigenvectors the array which receives the three output eigenvectors.
     *
     * @throws IllegalArgumentException if the Matrix is null or is not symmetric, if the output eigenvalue array is
     *                                  null or has length less than 3, or if the output eigenvector is null or has
     *                                  length less than 3.
     */
    public static void computeEigensystemFromSymmetricMatrix3(Matrix matrix, double[] outEigenvalues,
        Vec4[] outEigenvectors)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix.m12 != matrix.m21 || matrix.m13 != matrix.m31 || matrix.m23 != matrix.m32)
        {
            String msg = Logging.getMessage("generic.MatrixNotSymmetric", matrix);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Take from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition" by Eric Lengyel,
        // Listing 14.6 (pages 441-444).

        final double EPSILON = 1.0e-10;
        final int MAX_SWEEPS = 32;

        // Since the Matrix is symmetric, m12=m21, m13=m31, and m23=m32. Therefore we can ignore the values m21, m31,
        // and m32.
        double m11 = matrix.m11;
        double m12 = matrix.m12;
        double m13 = matrix.m13;
        double m22 = matrix.m22;
        double m23 = matrix.m23;
        double m33 = matrix.m33;

        double[][] r = new double[3][3];
        r[0][0] = r[1][1] = r[2][2] = 1d;

        for (int a = 0; a < MAX_SWEEPS; a++)
        {
            // Exit if off-diagonal entries small enough
            if ((Math.abs(m12) < EPSILON) && (Math.abs(m13) < EPSILON) && (Math.abs(m23) < EPSILON))
                break;

            // Annihilate (1,2) entry
            if (m12 != 0d)
            {
                double u = (m22 - m11) * 0.5 / m12;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m11 -= t * m12;
                m22 += t * m12;
                m12 = 0d;

                double temp = c * m13 - s * m23;
                m23 = s * m13 + c * m23;
                m13 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][0] - s * r[i][1];
                    r[i][1] = s * r[i][0] + c * r[i][1];
                    r[i][0] = temp;
                }
            }

            // Annihilate (1,3) entry
            if (m13 != 0d)
            {
                double u = (m33 - m11) * 0.5 / m13;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m11 -= t * m13;
                m33 += t * m13;
                m13 = 0d;

                double temp = c * m12 - s * m23;
                m23 = s * m12 + c * m23;
                m12 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][0] - s * r[i][2];
                    r[i][2] = s * r[i][0] + c * r[i][2];
                    r[i][0] = temp;
                }
            }

            // Annihilate (2,3) entry
            if (m23 != 0d)
            {
                double u = (m33 - m22) * 0.5 / m23;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m22 -= t * m23;
                m33 += t * m23;
                m23 = 0d;

                double temp = c * m12 - s * m13;
                m13 = s * m12 + c * m13;
                m12 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][1] - s * r[i][2];
                    r[i][2] = s * r[i][1] + c * r[i][2];
                    r[i][1] = temp;
                }
            }
        }

        outEigenvalues[0] = m11;
        outEigenvalues[1] = m22;
        outEigenvalues[2] = m33;

        outEigenvectors[0] = new Vec4(r[0][0], r[1][0], r[2][0]);
        outEigenvectors[1] = new Vec4(r[0][1], r[1][1], r[2][1]);
        outEigenvectors[2] = new Vec4(r[0][2], r[1][2], r[2][2]);
    }

    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //

    public final Matrix add(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            this.m11 + matrix.m11, this.m12 + matrix.m12, this.m13 + matrix.m13, this.m14 + matrix.m14,
            this.m21 + matrix.m21, this.m22 + matrix.m22, this.m23 + matrix.m23, this.m24 + matrix.m24,
            this.m31 + matrix.m31, this.m32 + matrix.m32, this.m33 + matrix.m33, this.m34 + matrix.m34,
            this.m41 + matrix.m41, this.m42 + matrix.m42, this.m43 + matrix.m43, this.m44 + matrix.m44);
    }

    public final Matrix subtract(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            this.m11 - matrix.m11, this.m12 - matrix.m12, this.m13 - matrix.m13, this.m14 - matrix.m14,
            this.m21 - matrix.m21, this.m22 - matrix.m22, this.m23 - matrix.m23, this.m24 - matrix.m24,
            this.m31 - matrix.m31, this.m32 - matrix.m32, this.m33 - matrix.m33, this.m34 - matrix.m34,
            this.m41 - matrix.m41, this.m42 - matrix.m42, this.m43 - matrix.m43, this.m44 - matrix.m44);
    }

    public final Matrix multiplyComponents(double value)
    {
        return new Matrix(
            this.m11 * value, this.m12 * value, this.m13 * value, this.m14 * value,
            this.m21 * value, this.m22 * value, this.m23 * value, this.m24 * value,
            this.m31 * value, this.m32 * value, this.m33 * value, this.m34 * value,
            this.m41 * value, this.m42 * value, this.m43 * value, this.m44 * value);
    }

    public final Matrix multiply(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            // Row 1
            (this.m11 * matrix.m11) + (this.m12 * matrix.m21) + (this.m13 * matrix.m31) + (this.m14 * matrix.m41),
            (this.m11 * matrix.m12) + (this.m12 * matrix.m22) + (this.m13 * matrix.m32) + (this.m14 * matrix.m42),
            (this.m11 * matrix.m13) + (this.m12 * matrix.m23) + (this.m13 * matrix.m33) + (this.m14 * matrix.m43),
            (this.m11 * matrix.m14) + (this.m12 * matrix.m24) + (this.m13 * matrix.m34) + (this.m14 * matrix.m44),
            // Row 2
            (this.m21 * matrix.m11) + (this.m22 * matrix.m21) + (this.m23 * matrix.m31) + (this.m24 * matrix.m41),
            (this.m21 * matrix.m12) + (this.m22 * matrix.m22) + (this.m23 * matrix.m32) + (this.m24 * matrix.m42),
            (this.m21 * matrix.m13) + (this.m22 * matrix.m23) + (this.m23 * matrix.m33) + (this.m24 * matrix.m43),
            (this.m21 * matrix.m14) + (this.m22 * matrix.m24) + (this.m23 * matrix.m34) + (this.m24 * matrix.m44),
            // Row 3
            (this.m31 * matrix.m11) + (this.m32 * matrix.m21) + (this.m33 * matrix.m31) + (this.m34 * matrix.m41),
            (this.m31 * matrix.m12) + (this.m32 * matrix.m22) + (this.m33 * matrix.m32) + (this.m34 * matrix.m42),
            (this.m31 * matrix.m13) + (this.m32 * matrix.m23) + (this.m33 * matrix.m33) + (this.m34 * matrix.m43),
            (this.m31 * matrix.m14) + (this.m32 * matrix.m24) + (this.m33 * matrix.m34) + (this.m34 * matrix.m44),
            // Row 4
            (this.m41 * matrix.m11) + (this.m42 * matrix.m21) + (this.m43 * matrix.m31) + (this.m44 * matrix.m41),
            (this.m41 * matrix.m12) + (this.m42 * matrix.m22) + (this.m43 * matrix.m32) + (this.m44 * matrix.m42),
            (this.m41 * matrix.m13) + (this.m42 * matrix.m23) + (this.m43 * matrix.m33) + (this.m44 * matrix.m43),
            (this.m41 * matrix.m14) + (this.m42 * matrix.m24) + (this.m43 * matrix.m34) + (this.m44 * matrix.m44),
            // Product of orthonormal 3D transform matrices is also an orthonormal 3D transform.
            this.isOrthonormalTransform && matrix.isOrthonormalTransform);
    }

    public final Matrix divideComponents(double value)
    {
        if (isZero(value))
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            this.m11 / value, this.m12 / value, this.m13 / value, this.m14 / value,
            this.m21 / value, this.m22 / value, this.m23 / value, this.m24 / value,
            this.m31 / value, this.m32 / value, this.m33 / value, this.m34 / value,
            this.m41 / value, this.m42 / value, this.m43 / value, this.m44 / value);
    }

    public final Matrix divideComponents(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Matrix(
            this.m11 / matrix.m11, this.m12 / matrix.m12, this.m13 / matrix.m13, this.m14 / matrix.m14,
            this.m21 / matrix.m21, this.m22 / matrix.m22, this.m23 / matrix.m23, this.m24 / matrix.m24,
            this.m31 / matrix.m31, this.m32 / matrix.m32, this.m33 / matrix.m33, this.m34 / matrix.m34,
            this.m41 / matrix.m41, this.m42 / matrix.m42, this.m43 / matrix.m43, this.m44 / matrix.m44);
    }

    public final Matrix negate()
    {
        return new Matrix(
            0.0 - this.m11, 0.0 - this.m12, 0.0 - this.m13, 0.0 - this.m14,
            0.0 - this.m21, 0.0 - this.m22, 0.0 - this.m23, 0.0 - this.m24,
            0.0 - this.m31, 0.0 - this.m32, 0.0 - this.m33, 0.0 - this.m34,
            0.0 - this.m41, 0.0 - this.m42, 0.0 - this.m43, 0.0 - this.m44,
            // Negative of orthonormal 3D transform matrix is also an orthonormal 3D transform.
            this.isOrthonormalTransform);
    }

    public final Vec4 transformBy3(Matrix matrix, double x, double y, double z)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (matrix.m11 * x) + (matrix.m12 * y) + (matrix.m13 * z),
            (matrix.m21 * x) + (matrix.m22 * y) + (matrix.m23 * z),
            (matrix.m31 * x) + (matrix.m32 * y) + (matrix.m33 * z));
    }

    // ============== Matrix Arithmetic Functions ======================= //
    // ============== Matrix Arithmetic Functions ======================= //
    // ============== Matrix Arithmetic Functions ======================= //

    public final double getDeterminant()
    {
        double result = 0.0;
        // Columns 2, 3, 4.
        result += this.m11 *
            (this.m22 * (this.m33 * this.m44 - this.m43 * this.m34)
                - this.m23 * (this.m32 * this.m44 - this.m42 * this.m34)
                + this.m24 * (this.m32 * this.m43 - this.m42 * this.m33));
        // Columns 1, 3, 4.
        result -= this.m12 *
            (this.m21 * (this.m33 * this.m44 - this.m43 * this.m34)
                - this.m23 * (this.m31 * this.m44 - this.m41 * this.m34)
                + this.m24 * (this.m31 * this.m43 - this.m41 * this.m33));
        // Columns 1, 2, 4.
        result += this.m13 *
            (this.m21 * (this.m32 * this.m44 - this.m42 * this.m34)
                - this.m22 * (this.m31 * this.m44 - this.m41 * this.m34)
                + this.m24 * (this.m31 * this.m42 - this.m41 * this.m32));
        // Columns 1, 2, 3.
        result -= this.m14 *
            (this.m21 * (this.m32 * this.m43 - this.m42 - this.m33)
                - this.m22 * (this.m31 * this.m43 - this.m41 * this.m33)
                + this.m23 * (this.m31 * this.m42 - this.m41 * this.m32));
        return result;
    }

    public final Matrix getTranspose()
    {
        // Swap rows with columns.
        return new Matrix(
            this.m11, this.m21, this.m31, this.m41,
            this.m12, this.m22, this.m32, this.m42,
            this.m13, this.m23, this.m33, this.m43,
            this.m14, this.m24, this.m34, this.m44,
            // Transpose of orthonormal 3D transform matrix is not an orthonormal 3D transform matrix.
            false);
    }

    public final double getTrace()
    {
        return this.m11 + this.m22 + this.m33 + this.m44;
    }

    /**
     * Returns the inverse of this matrix, or <code>null</code> if this matrix is singular and has no inverse.
     *
     * @return the inverse of this matrix, or <code>null</code> if this matrix has no inverse.
     */
    public final Matrix getInverse()
    {
        if (this.isOrthonormalTransform)
            return computeTransformInverse(this);
        else
            return computeGeneralInverse(this);
    }

    private static Matrix computeTransformInverse(Matrix a)
    {
        // 'a' is assumed to contain a 3D transformation matrix.
        // Upper-3x3 is inverted, translation is transformed by inverted-upper-3x3 and negated.
        return new Matrix(
            a.m11, a.m21, a.m31, 0.0 - (a.m11 * a.m14) - (a.m21 * a.m24) - (a.m31 * a.m34),
            a.m12, a.m22, a.m32, 0.0 - (a.m12 * a.m14) - (a.m22 * a.m24) - (a.m32 * a.m34),
            a.m13, a.m23, a.m33, 0.0 - (a.m13 * a.m14) - (a.m23 * a.m24) - (a.m33 * a.m34),
            0.0, 0.0, 0.0, 1.0,
            false); // Inverse of an orthogonal, 3D transform matrix is not an orthogonal 3D transform.
    }

    private static Matrix computeGeneralInverse(Matrix a)
    {
        // Copy the specified matrix into a mutable two-dimensional array.
        double[][] A = new double[4][4];
        A[0][0] = a.m11;
        A[0][1] = a.m12;
        A[0][2] = a.m13;
        A[0][3] = a.m14;
        A[1][0] = a.m21;
        A[1][1] = a.m22;
        A[1][2] = a.m23;
        A[1][3] = a.m24;
        A[2][0] = a.m31;
        A[2][1] = a.m32;
        A[2][2] = a.m33;
        A[2][3] = a.m34;
        A[3][0] = a.m41;
        A[3][1] = a.m42;
        A[3][2] = a.m43;
        A[3][3] = a.m44;

        int[] indx = new int[4];
        double d = ludcmp(A, indx);

        // Compute the matrix's determinant.
        for (int i = 0; i < 4; i++)
        {
            d *= A[i][i];
        }

        // The matrix is singular if its determinant is zero or very close to zero.
        if (Math.abs(d) < NEAR_ZERO_THRESHOLD)
            return null;

        double[][] Y = new double[4][4];
        double[] col = new double[4];
        for (int j = 0; j < 4; j++)
        {
            for (int i = 0; i < 4; i++)
            {
                col[i] = 0.0;
            }

            col[j] = 1.0;
            lubksb(A, indx, col);

            for (int i = 0; i < 4; i++)
            {
                Y[i][j] = col[i];
            }
        }

        return new Matrix(
            Y[0][0], Y[0][1], Y[0][2], Y[0][3],
            Y[1][0], Y[1][1], Y[1][2], Y[1][3],
            Y[2][0], Y[2][1], Y[2][2], Y[2][3],
            Y[3][0], Y[3][1], Y[3][2], Y[3][3]);
    }

    // Method "lubksb" derived from "Numerical Recipes in C", Press et al., 1988
    private static void lubksb(double[][] A, int[] indx, double[] b)
    {
        int ii = -1;
        for (int i = 0; i < 4; i++)
        {
            int ip = indx[i];
            double sum = b[ip];
            b[ip] = b[i];

            if (ii != -1)
            {
                for (int j = ii; j <= i - 1; j++)
                {
                    sum -= A[i][j] * b[j];
                }
            }
            else if (sum != 0.0)
            {
                ii = i;
            }

            b[i] = sum;
        }

        for (int i = 3; i >= 0; i--)
        {
            double sum = b[i];
            for (int j = i + 1; j < 4; j++)
            {
                sum -= A[i][j] * b[j];
            }

            b[i] = sum / A[i][i];
        }
    }

    // Method "ludcmp" derived from "Numerical Recipes in C", Press et al., 1988
    private static double ludcmp(double[][] A, int[] indx)
    {
        final double TINY = 1.0e-20;

        double[] vv = new double[4];
        double d = 1.0;
        double temp;
        for (int i = 0; i < 4; i++)
        {
            double big = 0.0;
            for (int j = 0; j < 4; j++)
            {
                if ((temp = Math.abs(A[i][j])) > big)
                    big = temp;
            }

            if (big == 0.0)
                return 0.0; // Matrix is singular if the entire row contains zero.
            else
                vv[i] = 1.0 / big;
        }

        double sum;
        for (int j = 0; j < 4; j++)
        {
            for (int i = 0; i < j; i++)
            {
                sum = A[i][j];
                for (int k = 0; k < i; k++)
                {
                    sum -= A[i][k] * A[k][j];
                }

                A[i][j] = sum;
            }

            double big = 0.0;
            double dum;
            int imax = -1;
            for (int i = j; i < 4; i++)
            {
                sum = A[i][j];
                for (int k = 0; k < j; k++)
                {
                    sum -= A[i][k] * A[k][j];
                }

                A[i][j] = sum;

                if ((dum = vv[i] * Math.abs(sum)) >= big)
                {
                    big = dum;
                    imax = i;
                }
            }

            if (j != imax)
            {
                for (int k = 0; k < 4; k++)
                {
                    dum = A[imax][k];
                    A[imax][k] = A[j][k];
                    A[j][k] = dum;
                }

                d = -d;
                vv[imax] = vv[j];
            }

            indx[j] = imax;
            if (A[j][j] == 0.0)
                A[j][j] = TINY;

            if (j != 3)
            {
                dum = 1.0 / A[j][j];
                for (int i = j + 1; i < 4; i++)
                {
                    A[i][j] *= dum;
                }
            }
        }

        return d;
    }

    // ============== Accessor Functions ======================= //
    // ============== Accessor Functions ======================= //
    // ============== Accessor Functions ======================= //

    public final Angle getRotationX()
    {
        double yRadians = Math.asin(this.m13);
        double cosY = Math.cos(yRadians);
        if (isZero(cosY))
            return null;

        double xRadians;
        // No Gimball lock.
        if (Math.abs(cosY) > 0.005)
        {
            xRadians = Math.atan2(-this.m23 / cosY, this.m33 / cosY);
        }
        // Gimball lock has occurred. Rotation around X axis becomes rotation around Z axis.
        else
        {
            xRadians = 0;
        }

        if (Double.isNaN(xRadians))
            return null;

        return Angle.fromRadians(xRadians);
    }

    public final Angle getRotationY()
    {
        double yRadians = Math.asin(this.m13);
        if (Double.isNaN(yRadians))
            return null;

        return Angle.fromRadians(yRadians);
    }

    public final Angle getRotationZ()
    {
        double yRadians = Math.asin(this.m13);
        double cosY = Math.cos(yRadians);
        if (isZero(cosY))
            return null;

        double zRadians;
        // No Gimball lock.
        if (Math.abs(cosY) > 0.005)
        {
            zRadians = Math.atan2(-this.m12 / cosY, this.m11 / cosY);
        }
        // Gimball lock has occurred. Rotation around X axis becomes rotation around Z axis.
        else
        {
            zRadians = Math.atan2(this.m21, this.m22);
        }

        if (Double.isNaN(zRadians))
            return null;

        return Angle.fromRadians(zRadians);
    }

    public final Angle getKMLRotationX()    // KML assumes the order of rotations is YXZ, positive CW
    {
        double xRadians = Math.asin(-this.m23);
        if (Double.isNaN(xRadians))
            return null;

        return Angle.fromRadians(-xRadians);    // negate to make angle CW
    }

    public final Angle getKMLRotationY()    // KML assumes the order of rotations is YXZ, positive CW
    {
        double xRadians = Math.asin(-this.m23);
        if (Double.isNaN(xRadians))
            return null;

        double yRadians;
        if (xRadians < Math.PI / 2)
        {
            if (xRadians > -Math.PI / 2)
            {
                yRadians = Math.atan2(this.m13, this.m33);
            }
            else
            {
                yRadians = -Math.atan2(-this.m12, this.m11);
            }
        }
        else
        {
            yRadians = Math.atan2(-this.m12, this.m11);
        }

        if (Double.isNaN(yRadians))
            return null;

        return Angle.fromRadians(-yRadians);    // negate angle to make it CW
    }

    public final Angle getKMLRotationZ()    // KML assumes the order of rotations is YXZ, positive CW
    {
        double xRadians = Math.asin(-this.m23);
        if (Double.isNaN(xRadians))
            return null;

        double zRadians;
        if (xRadians < Math.PI / 2 && xRadians > -Math.PI / 2)
        {
            zRadians = Math.atan2(this.m21, this.m22);
        }
        else
        {
            zRadians = 0;
        }

        if (Double.isNaN(zRadians))
            return null;

        return Angle.fromRadians(-zRadians);    // negate angle to make it CW
    }

    public final Vec4 getTranslation()
    {
        return new Vec4(this.m14, this.m24, this.m34);
    }

    /**
     * Extracts this viewing matrix's eye point.
     * <p>
     * This method assumes that this matrix represents a viewing matrix. If this does not represent a viewing matrix the
     * results are undefined.
     * <p>
     * In model coordinates, a viewing matrix's eye point is the point the viewer is looking from and maps to the center
     * of the screen.
     *
     * @return this viewing matrix's eye point, in model coordinates.
     */
    public Vec4 extractEyePoint()
    {
        // The eye point of a modelview matrix is computed by transforming the origin (0, 0, 0, 1) by the matrix's
        // inverse. This is equivalent to transforming the inverse of this matrix's translation components in the
        // rightmost column by the transpose of its upper 3x3 components.
        double x = -(m11 * m14) - (m21 * m24) - (m31 * m34);
        double y = -(m12 * m14) - (m22 * m24) - (m32 * m34);
        double z = -(m13 * m14) - (m23 * m24) - (m33 * m34);

        return new Vec4(x, y, z);
    }

    /**
     * Extracts this viewing matrix's forward vector.
     * <p>
     * This method assumes that this matrix represents a viewing matrix. If this does not represent a viewing matrix the
     * results are undefined.
     * <p>
     * In model coordinates, a viewing matrix's forward vector is the direction the viewer is looking and maps to a
     * vector going into the screen.
     *
     * @return this viewing matrix's forward vector, in model coordinates.
     */
    public Vec4 extractForwardVector()
    {
        // The forward vector of a modelview matrix is computed by transforming the negative Z axis (0, 0, -1, 0) by the
        // matrix's inverse. We have pre-computed the result inline here to simplify this computation.
        return new Vec4(-this.m31, -this.m32, -this.m33);
    }

    /**
     * Extracts this viewing matrix's parameters given a viewing origin and a globe.
     * <p>
     * This method assumes that this matrix represents a viewing matrix. If this does not represent a viewing matrix the
     * results are undefined.
     * <p>
     * This returns a parameterization of this viewing matrix based on the specified origin and globe. The origin
     * indicates the model coordinate point that the view's orientation is relative to, while the globe provides the
     * necessary model coordinate context for the origin and the orientation. The origin should be either the view's eye
     * point or a point on the view's forward vector. The view's roll must be specified in order to disambiguate heading
     * and roll when the view's tilt is zero.
     *
     * The following list outlines the returned key-value pairs and their meanings:
     * <ul>
     * <li>AVKey.ORIGIN - The geographic position corresponding to the origin point.</li>
     * <li>AVKey.RANGE - The distance between the specified origin point and the view's eye point, in model coordinates.</li>
     * <li>AVKey.HEADING - The view's heading angle relative to the globe's north pointing tangent at the origin point.</li>
     * <li>AVKey.TILT - The view's tilt angle relative to the globe's normal vector at the origin point.</li>
     * <li>AVKey.ROLL - The view's roll relative to the globe's normal vector at the origin point.</li>
     * </ul>
     *
     * @param origin the origin of the viewing parameters, in model coordinates.
     * @param roll   the view's roll.
     * @param globe  the globe the viewer is looking at.
     *
     * @return a parameterization of this viewing matrix as a list of key-value pairs.
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public AVList extractViewingParameters(Vec4 origin, Angle roll, Globe globe)
    {
        if (origin == null)
        {
            String msg = Logging.getMessage("nullValue.OriginIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (roll == null)
        {
            String msg = Logging.getMessage("nullValue.RollIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Transform the modelview matrix to a local coordinate system at the origin. This eliminates the geographic
        // transform contained in the modelview matrix while maintaining rotation and translation relative to the origin.
        Position originPos = globe.computePositionFromPoint(origin);
        Matrix modelviewLocal = this.multiply(globe.computeModelCoordinateOriginTransform(originPos));

        // Extract the viewing parameters from the transform in local coordinates.
        // TODO: Document how these parameters are extracted. See [WWMatrix extractViewingParameters] in WWiOS.

        Matrix m = modelviewLocal;
        double range = -m.m34;

        double ct = m.m33;
        double st = Math.sqrt(m.m13 * m.m13 + m.m23 * m.m23);
        double tilt = Math.atan2(st, ct);

        double cr = Math.cos(roll.radians);
        double sr = Math.sin(roll.radians);
        double ch = cr * m.m11 - sr * m.m21;
        double sh = sr * m.m22 - cr * m.m12;
        double heading = Math.atan2(sh, ch);

        AVList params = new AVListImpl();
        params.setValue(AVKey.ORIGIN, originPos);
        params.setValue(AVKey.RANGE, range);
        params.setValue(AVKey.HEADING, Angle.fromRadians(heading));
        params.setValue(AVKey.TILT, Angle.fromRadians(tilt));
        params.setValue(AVKey.ROLL, roll);

        return params;
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    private static final Double POSITIVE_ZERO = +0.0d;

    private static final Double NEGATIVE_ZERO = -0.0d;

    private static boolean isZero(double value)
    {
        return (POSITIVE_ZERO.compareTo(value) == 0)
            || (NEGATIVE_ZERO.compareTo(value) == 0);
    }
}
