/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.globes.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BoundingVolumeTest
{
    private Sector sector;
    private Globe globe;
    private double ve = 1;
    private double minElevation = 0;
    private double maxElevation = 1e3;
    private int numIterations = (int) 1e6;

    @Before
    public void setUp()
    {
        this.globe = new Earth();
        this.sector = Sector.fromDegrees(-20, -10, -15, -10);
    }

    @After
    public void tearDown()
    {
        this.globe = null;
    }

    @Test
    public void testBoxCulling()
    {
        Frustum frustum = new Frustum(
            new Plane(0, 1, 0, 2), new Plane(0, -1, 0, 2),
            new Plane(0, 0, 1, 2), new Plane(0, 0, -1, 2),
            new Plane(1, 0, 0, 0), new Plane(-1, 0, 0, 1.5)
        );

        Box box = new Box(
            new Vec4[] {new Vec4(1, 0, 0), new Vec4(0, 1, 0), new Vec4(0, 0, 1)},
            1, 2, -1, 1, -1, 1);

        boolean tf = box.intersects(frustum);
        assertTrue("Box/Frustum intersection not detected", tf);
    }

    @Test
    public void testSphereCulling()
    {
        Frustum frustum = new Frustum(
            new Plane(0, 1, 0, 2), new Plane(0, -1, 0, 2),
            new Plane(0, 0, 1, 2), new Plane(0, 0, -1, 2),
            new Plane(1, 0, 0, 0), new Plane(-1, 0, 0, 1.5)
        );

        Sphere sphere = new Sphere(new Vec4(0, 0, 0, 1), 1);

        boolean tf = sphere.intersects(frustum);
        assertTrue("sphere.intersects(frustum) intersection not detected", tf);
        tf = frustum.intersects(sphere);
        assertTrue("frustum.intersects(sphere) intersection not detected", tf);

        sphere = new Sphere(new Vec4(3, 3, 3, 1), 1);

        tf = sphere.intersects(frustum);
        assertFalse("sphere.intersects(frustum) erroneously detects intersection", tf);
    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    public void testBoxCullingSpeed()
    {
        Frustum frustum = new Frustum(); // unit frustum around origin
        Box box = new Box(new Vec4(0, 0, 0));

        boolean tf = box.intersects(frustum);
        assertTrue("Box/Frustum intersection not detected", tf);

        for (int j = 0; j < 3; j++)
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < this.numIterations; i++)
            {
                tf = box.intersects(frustum);
            }
            double elapsed = System.currentTimeMillis() - start;
            System.out.printf("Box culling %d in %f milis, %f micros per box\n", this.numIterations,
                elapsed, 1e3 * elapsed / this.numIterations);
        }
    }

    @SuppressWarnings("UnusedAssignment")
    @Test
    public void testCylinderCullingSpeed()
    {
        Frustum frustum = new Frustum(); // unit frustum around origin
        Cylinder cyl = new Cylinder(new Vec4(0, 0.5, 0.5), new Vec4(1, 0.5, 0.5), 0.5);

        boolean tf = cyl.intersects(frustum);
        assertTrue("Box/Frustum intersection not detected", tf);

        for (int j = 0; j < 3; j++)
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < this.numIterations; i++)
            {
                tf = cyl.intersects(frustum);
            }
            double elapsed = System.currentTimeMillis() - start;
            System.out.printf("Cylinder culling %d in %f milis, %f micros per cylinder\n", this.numIterations,
                elapsed, 1e3 * elapsed / this.numIterations);
        }
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void testBoxCreationSpeed()
    {
        Box box;

        for (int j = 0; j < 3; j++)
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < this.numIterations; i++)
            {
                box = Sector.computeBoundingBox(this.globe, this.ve, this.sector, this.minElevation, this.maxElevation);
            }
            double elapsed = System.currentTimeMillis() - start;
            System.out.printf("Box creation %d in %f milis, %f micros per box\n", this.numIterations, elapsed,
                1e3 * elapsed / this.numIterations);
        }
    }

    @SuppressWarnings({"unused", "UnusedAssignment"})
    @Test
    public void testCylinderCreationSpeed()
    {
        Cylinder cyl;

        for (int j = 0; j < 3; j++)
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < this.numIterations; i++)
            {
                cyl = Sector.computeBoundingCylinder(this.globe, this.ve, this.sector, this.minElevation, this.maxElevation);
            }
            double elapsed = System.currentTimeMillis() - start;
            System.out.printf("Cylinder creation %d in %f milis, %f micros per cylinder\n", this.numIterations,
                elapsed, 1e3 * elapsed / this.numIterations);
        }
    }
}
