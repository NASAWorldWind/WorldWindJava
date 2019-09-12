/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class MatrixTest
{
    private static final double EQUALITY_TOLERANCE = 1.0e-9;
    private static final double NEAR_SINGULAR_EQUALITY_TOLERANCE = 1.0e-6;
    private static final long RANDOM_SEED = 6124946250748012048L;

    private Random random;

    @Before
    public void setUp() throws Exception
    {
        // Use a random initialized with a constant seed to ensure that subsequent test executes get the same
        // pseudorandom values. Using Math.random may results in tests failing unpredictably, and prevents debugging
        // since the random seed is not known.
        random = new Random(RANDOM_SEED);
    }

    //**************************************************************//
    //********************  Test Matrix Inversion  *****************//
    //**************************************************************//

    @Test
    public void testInverseOfTransform()
    {
        Matrix m = Matrix.IDENTITY;
        m = m.multiply(Matrix.fromTranslation(100, 200, 300));
        m = m.multiply(Matrix.fromRotationXYZ(Angle.fromDegrees(10), Angle.fromDegrees(20), Angle.fromDegrees(30)));

        Matrix mInv = m.getInverse();
        assertNotNull("Matrix inverse is null", mInv);

        Matrix identity = m.multiply(mInv);
        assertTrue("Matrix inverse is incorrect", equals(identity, Matrix.IDENTITY, EQUALITY_TOLERANCE));
    }

    @Test
    public void testInverseOfRandom()
    {
        Matrix m = new Matrix(
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(),
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(),
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(),
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble());

        Matrix mInv = m.getInverse();
        assertNotNull("Matrix inverse is null", mInv);

        Matrix identity = m.multiply(mInv);
        assertTrue("Matrix inverse is incorrect", equals(identity, Matrix.IDENTITY, EQUALITY_TOLERANCE));
    }

    @Test
    public void testInverseOfSingular()
    {
        // Create a singular matrix, where the fourth row is a linear combination of first three.
        double m11 = random.nextDouble(), m12 = random.nextDouble(), m13 = random.nextDouble(), m14
            = random.nextDouble();
        double m21 = random.nextDouble(), m22 = random.nextDouble(), m23 = random.nextDouble(), m24
            = random.nextDouble();
        double m31 = random.nextDouble(), m32 = random.nextDouble(), m33 = random.nextDouble(), m34
            = random.nextDouble();
        double f1 = 1.4, f2 = -4.02, f3 = 0.3;
        double m41 = f1 * m11 + f2 * m21 + f3 * m31;
        double m42 = f1 * m12 + f2 * m22 + f3 * m32;
        double m43 = f1 * m13 + f2 * m23 + f3 * m33;
        double m44 = f1 * m14 + f2 * m24 + f3 * m34;

        Matrix m = new Matrix(
            m11, m12, m13, m14,
            m21, m22, m23, m24,
            m31, m32, m33, m34,
            m41, m42, m43, m44);

        Matrix mInv = m.getInverse();
        assertNull("Singular matrix should not have an inverse", mInv);
    }

    @Test
    public void testInverseOfZeroRow()
    {
        Matrix m = new Matrix(
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(),
            0, 0, 0, 0,
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble(),
            random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble());

        Matrix mInv = m.getInverse();
        assertNull("Singular matrix should not have an inverse", mInv);
    }

    @Test
    public void testInverseOfNearSingular()
    {
        // Create a singular matrix, where the fourth row is a linear combination of first three.
        double m11 = random.nextDouble(), m12 = random.nextDouble(), m13 = random.nextDouble(), m14
            = random.nextDouble();
        double m21 = random.nextDouble(), m22 = random.nextDouble(), m23 = random.nextDouble(), m24
            = random.nextDouble();
        double m31 = random.nextDouble(), m32 = random.nextDouble(), m33 = random.nextDouble(), m34
            = random.nextDouble();
        double f1 = 1.4, f2 = -4.02, f3 = 0.3;
        double m41 = f1 * m11 + f2 * m21 + f3 * m31;
        double m42 = f1 * m12 + f2 * m22 + f3 * m32;
        double m43 = f1 * m13 + f2 * m23 + f3 * m33;
        double m44 = f1 * m14 + f2 * m24 + f3 * m34;

        // Slightly perturb the matrix away from singular.
        m42 += 1.0e-5;

        Matrix m = new Matrix(
            m11, m12, m13, m14,
            m21, m22, m23, m24,
            m31, m32, m33, m34,
            m41, m42, m43, m44);

        Matrix mInv = m.getInverse();
        assertNotNull("Matrix inverse is null", mInv);

        Matrix identity = m.multiply(mInv);
        assertTrue("Matrix inverse is incorrect", equals(identity, Matrix.IDENTITY, NEAR_SINGULAR_EQUALITY_TOLERANCE));
    }

    //**************************************************************//
    //********************  Helper Methods  ************************//
    //**************************************************************//

    private static boolean equals(Matrix a, Matrix b, double tolerance)
    {
        return Math.abs(a.m11 - b.m11) < tolerance
            && Math.abs(a.m12 - b.m12) < tolerance
            && Math.abs(a.m13 - b.m13) < tolerance
            && Math.abs(a.m14 - b.m14) < tolerance
            && Math.abs(a.m21 - b.m21) < tolerance
            && Math.abs(a.m22 - b.m22) < tolerance
            && Math.abs(a.m23 - b.m23) < tolerance
            && Math.abs(a.m24 - b.m24) < tolerance
            && Math.abs(a.m31 - b.m31) < tolerance
            && Math.abs(a.m32 - b.m32) < tolerance
            && Math.abs(a.m33 - b.m33) < tolerance
            && Math.abs(a.m34 - b.m34) < tolerance
            && Math.abs(a.m41 - b.m41) < tolerance
            && Math.abs(a.m42 - b.m42) < tolerance
            && Math.abs(a.m43 - b.m43) < tolerance
            && Math.abs(a.m44 - b.m44) < tolerance;
    }
}
