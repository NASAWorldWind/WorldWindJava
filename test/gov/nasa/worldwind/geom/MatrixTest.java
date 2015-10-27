/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import junit.framework.*;
import junit.textui.TestRunner;
import org.junit.*;

/**
 * @author dcollins
 * @version $Id: MatrixTest.java 872 2012-11-01 17:02:57Z dcollins $
 */
public class MatrixTest
{
    public static void main(String[] args)
    {
        TestSuite testSuite = new TestSuite();
        testSuite.addTestSuite(BasicTests.class);
        new TestRunner().doRun(testSuite);
    }

    public static class BasicTests extends TestCase
    {
        @Before
        public void setUp()
        {
        }

        @After
        public void tearDown()
        {
        }

        //**************************************************************//
        //********************  Test Matrix Inversion  *****************//
        //**************************************************************//

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

        public void testInverseOfRandom()
        {
            Matrix m = new Matrix(
                Math.random(), Math.random(), Math.random(), Math.random(),
                Math.random(), Math.random(), Math.random(), Math.random(),
                Math.random(), Math.random(), Math.random(), Math.random(),
                Math.random(), Math.random(), Math.random(), Math.random());

            Matrix mInv = m.getInverse();
            assertNotNull("Matrix inverse is null", mInv);

            Matrix identity = m.multiply(mInv);
            assertTrue("Matrix inverse is incorrect", equals(identity, Matrix.IDENTITY, EQUALITY_TOLERANCE));
        }

        public void testInverseOfSingular()
        {
            // Create a singular matrix, where the fourth row is a linear combination of first three.
            double m11 = Math.random(), m12 = Math.random(), m13 = Math.random(), m14 = Math.random();
            double m21 = Math.random(), m22 = Math.random(), m23 = Math.random(), m24 = Math.random();
            double m31 = Math.random(), m32 = Math.random(), m33 = Math.random(), m34 = Math.random();
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

        public void testInverseOfZeroRow()
        {
            Matrix m = new Matrix(
                Math.random(), Math.random(), Math.random(), Math.random(),
                0, 0, 0, 0,
                Math.random(), Math.random(), Math.random(), Math.random(),
                Math.random(), Math.random(), Math.random(), Math.random());

            Matrix mInv = m.getInverse();
            assertNull("Singular matrix should not have an inverse", mInv);
        }

        public void testInverseOfNearSingular()
        {
            // Create a singular matrix, where the fourth row is a linear combination of first three.
            double m11 = Math.random(), m12 = Math.random(), m13 = Math.random(), m14 = Math.random();
            double m21 = Math.random(), m22 = Math.random(), m23 = Math.random(), m24 = Math.random();
            double m31 = Math.random(), m32 = Math.random(), m33 = Math.random(), m34 = Math.random();
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
            assertTrue("Matrix inverse is incorrect", equals(identity, Matrix.IDENTITY, EQUALITY_TOLERANCE));
        }

        //**************************************************************//
        //********************  Helper Methods  ************************//
        //**************************************************************//

        public static final double EQUALITY_TOLERANCE = 1.0e-9;

        public static boolean equals(Matrix a, Matrix b, double tolerance)
        {
            return Math.abs(a.m11 - b.m11) < tolerance && Math.abs(a.m12 - b.m12) < tolerance
                && Math.abs(a.m13 - b.m13) < tolerance && Math.abs(a.m14 - b.m14) < tolerance
                && Math.abs(a.m21 - b.m21) < tolerance && Math.abs(a.m22 - b.m22) < tolerance
                && Math.abs(a.m23 - b.m23) < tolerance && Math.abs(a.m24 - b.m24) < tolerance
                && Math.abs(a.m31 - b.m31) < tolerance && Math.abs(a.m32 - b.m32) < tolerance
                && Math.abs(a.m33 - b.m33) < tolerance && Math.abs(a.m34 - b.m34) < tolerance
                && Math.abs(a.m41 - b.m41) < tolerance && Math.abs(a.m42 - b.m42) < tolerance
                && Math.abs(a.m43 - b.m43) < tolerance && Math.abs(a.m44 - b.m44) < tolerance;
        }
    }
}
