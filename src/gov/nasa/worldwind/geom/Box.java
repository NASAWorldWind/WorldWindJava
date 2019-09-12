/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import java.util.*;

/**
 * An arbitrarily oriented box, typically used as a oriented bounding volume for a collection of points or shapes. A
 * <code>Box</code> is defined by three orthogonal axes and two positions along each of those axes. Each of the
 * positions specifies the location of a box side along the respective axis. The three axes are named by convention "R",
 * "S" and "T", and are ordered by decreasing length -- R is the longest axis, followed by S and then T.
 * <p>
 * The static class field, <code>ProjectionHullTable</code>, defines a table of all possible vertex combinations
 * representing a <code>Box's</code> 2D convex hull in screen coordinates. The index to this table is a 6-bit code,
 * where each bit denotes whether one of the <code>Box's</code> six planes faces the <code>View</code>. This code is
 * organized as follows:
 * <table border='1'> <caption style="font-weight: bold;">Bit Mapping</caption>
 * <tr><td><strong>Bit</strong></td><td >5</td><td>4</td><td >3</td><td >2</td><td >1</td><td >0</td></tr>
 * <tr><td><strong>Code</strong></td><td >left</td><td>right</td><td >back</td><td
 * >front</td><td>bottom</td><td>top</td></tr>
 * </table>
 * <p>
 * Since at most three of a <code>Box's</code> planes can be visible at one time, there are a total of 26 unique vertex
 * combinations that define a <code>Box's</code> 2D convex hull in the viewport. Index codes that represent a valid
 * combination of planes facing the <code>View</code> result in an array of 4 or 6 integers (depending on whether one,
 * two or three planes face the <code>View</code>), where each element in the array is an index for one of the
 * <code>Box's</code> eight vertices as follows:
 * <table border='1'> <caption style="font-weight: bold;">Index Mapping</caption>
 * <tr><td><strong>Index</strong></td><td>0</td>
 * <td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td></tr>
 * <tr><td><strong>Vertex</strong></td><td>bottom-lower-left</td><td>bottom-lower-right</td>
 * <td>bottom-upper-right</td><td>bottom-upper-left</td><td>top-lower-left</td><td>top-lower-right</td>
 * <td>top-upper-right</td><td>top-upper-left</td></tr>
 * </table>
 * <p>
 * The vertices are organized so that they appear in counter-clockwise order on the screen. Index codes that represent
 * an invalid combination of planes facing the <code>View</code> map to <code>null</code>.
 *
 * @author tag
 * @version $Id: Box.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Box implements Extent, Renderable
{
    /**
     */
    protected static final int[][] ProjectionHullTable = new int[][]
        {
            null,               // 000000: inside
            {7, 6, 5, 4},       // 000001: top
            {0, 1, 2, 3},       // 000010: bottom
            null,               // 000011: -
            {3, 2, 6, 7},       // 000100: front
            {3, 2, 6, 5, 4, 7}, // 000101: front, top
            {0, 1, 2, 6, 7, 3}, // 000110: front, bottom
            null,               // 000111: -
            {1, 0, 4, 5},       // 001000: back
            {1, 0, 4, 7, 6, 5}, // 001001: back, top
            {2, 3, 0, 4, 5, 1}, // 001010: back, bottom
            null,               // 001011: -
            null,               // 001100: -
            null,               // 001101: -
            null,               // 001110: -
            null,               // 001111: -
            {2, 1, 5, 6},       // 010000: right
            {2, 1, 5, 4, 7, 6}, // 010001: right, top
            {3, 0, 1, 5, 6, 2}, // 010010: right, bottom
            null,               // 010011: -
            {3, 2, 1, 5, 6, 7}, // 010100: right, front
            {3, 2, 1, 5, 4, 7}, // 010101: right, front, top
            {3, 0, 1, 5, 6, 7}, // 010110: right, front, bottom
            null,               // 010111: -
            {2, 1, 0, 4, 5, 6}, // 011000: right, back
            {2, 1, 0, 4, 7, 6}, // 011001: right, back, top
            {2, 3, 0, 4, 5, 6}, // 011010: right, back, bottom
            null,               // 011011: -
            null,               // 011100: -
            null,               // 011101: -
            null,               // 011110: -
            null,               // 011111: -
            {0, 3, 7, 4},       // 100000: left
            {0, 3, 7, 6, 5, 4}, // 100001: left, top
            {1, 2, 3, 7, 4, 0}, // 100010: left, bottom
            null,               // 100011: -
            {0, 3, 2, 6, 7, 4}, // 100100: left, front
            {0, 3, 2, 6, 5, 4}, // 100101: left, front, top
            {0, 1, 2, 6, 7, 4}, // 100110: left, front, bottom
            null,               // 100111: -
            {1, 0, 3, 7, 4, 5}, // 101000: left, back
            {1, 0, 3, 7, 6, 5}, // 101001: left, back, top
            {1, 2, 3, 7, 4, 5}, // 101010: left, back, bottom
        };

    public Vec4 bottomCenter; // point at center of box's longest axis
    public Vec4 topCenter; // point at center of box's longest axis
    protected final Vec4 center; // center of box
    protected final Vec4 r; // longest axis
    protected final Vec4 s; // next longest axis
    protected final Vec4 t; // shortest axis
    protected final Vec4 ru; // r axis unit normal
    protected final Vec4 su; // s axis unit normal
    protected final Vec4 tu; // t axis unit normal
    protected final double rLength; // length of r axis
    protected final double sLength; // length of s axis
    protected final double tLength; // length of t axis
    protected final Plane[] planes; // the six planes, with positive normals facing outwards

    protected Box(Vec4 bottomCenter, Vec4 topCenter, Vec4 center, Vec4 r, Vec4 s, Vec4 t, Vec4 ru, Vec4 su, Vec4 tu,
        double rlength, double sLength, double tLength, Plane[] planes)
    {
        this.bottomCenter = bottomCenter;
        this.topCenter = topCenter;
        this.center = center;
        this.r = r;
        this.s = s;
        this.t = t;
        this.ru = ru;
        this.su = su;
        this.tu = tu;
        this.rLength = rlength;
        this.sLength = sLength;
        this.tLength = tLength;
        this.planes = planes;
    }

    /**
     * Construct a box from three specified unit axes and the locations of the box faces relative to those axes. The box
     * faces are specified by two scalar locations along each axis, each location indicating a face. The non-unit length
     * of an axis is the distance between its respective two locations. The longest side is specified first, followed by
     * the second longest side and then the shortest side.
     * <p>
     * The axes are normally principal axes computed from a collection of points in order to form an oriented bounding
     * volume. See {@link WWMath#computePrincipalAxes(Iterable)}.
     * <p>
     * Note: No check is made to ensure the order of the face locations.
     *
     * @param axes the unit-length axes.
     * @param rMin the location along the first axis corresponding to the left-most box side relative to the axis.
     * @param rMax the location along the first axis corresponding to the right-most box side relative to the axis.
     * @param sMin the location along the second axis corresponding to the left-most box side relative to the axis.
     * @param sMax the location along the second axis corresponding to the right-most box side relative to the axis.
     * @param tMin the location along the third axis corresponding to the left-most box side relative to the axis.
     * @param tMax the location along the third axis corresponding to the right-most box side relative to the axis.
     *
     * @throws IllegalArgumentException if the axes array or one of its entries is null.
     */
    public Box(Vec4 axes[], double rMin, double rMax, double sMin, double sMax, double tMin, double tMax)
    {
        if (axes == null || axes[0] == null || axes[1] == null || axes[2] == null)
        {
            String msg = Logging.getMessage("nullValue.AxesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ru = axes[0];
        this.su = axes[1];
        this.tu = axes[2];

        this.r = this.ru.multiply3(rMax - rMin);
        this.s = this.su.multiply3(sMax - sMin);
        this.t = this.tu.multiply3(tMax - tMin);

        this.rLength = this.r.getLength3();
        this.sLength = this.s.getLength3();
        this.tLength = this.t.getLength3();

        // Plane normals point outward from the box.
        this.planes = new Plane[6];
        this.planes[0] = new Plane(-this.ru.x, -this.ru.y, -this.ru.z, +rMin);
        this.planes[1] = new Plane(+this.ru.x, +this.ru.y, +this.ru.z, -rMax);
        this.planes[2] = new Plane(-this.su.x, -this.su.y, -this.su.z, +sMin);
        this.planes[3] = new Plane(+this.su.x, +this.su.y, +this.su.z, -sMax);
        this.planes[4] = new Plane(-this.tu.x, -this.tu.y, -this.tu.z, +tMin);
        this.planes[5] = new Plane(+this.tu.x, +this.tu.y, +this.tu.z, -tMax);

        double a = 0.5 * (rMin + rMax);
        double b = 0.5 * (sMin + sMax);
        double c = 0.5 * (tMin + tMax);
        this.center = ru.multiply3(a).add3(su.multiply3(b)).add3(tu.multiply3(c));

        Vec4 rHalf = r.multiply3(0.5);
        this.topCenter = this.center.add3(rHalf);
        this.bottomCenter = this.center.subtract3(rHalf);
    }

    /**
     * Construct a unit-length cube centered at a specified point.
     *
     * @param point the center of the cube.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public Box(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ru = new Vec4(1, 0, 0, 1);
        this.su = new Vec4(0, 1, 0, 1);
        this.tu = new Vec4(0, 0, 1, 1);

        this.r = this.ru;
        this.s = this.su;
        this.t = this.tu;

        this.rLength = 1;
        this.sLength = 1;
        this.tLength = 1;

        // Plane normals point outwards from the box.
        this.planes = new Plane[6];
        double d = 0.5 * point.getLength3();
        this.planes[0] = new Plane(-this.ru.x, -this.ru.y, -this.ru.z, -(d + 0.5));
        this.planes[1] = new Plane(+this.ru.x, +this.ru.y, +this.ru.z, -(d + 0.5));
        this.planes[2] = new Plane(-this.su.x, -this.su.y, -this.su.z, -(d + 0.5));
        this.planes[3] = new Plane(+this.su.x, +this.su.y, +this.su.z, -(d + 0.5));
        this.planes[4] = new Plane(-this.tu.x, -this.tu.y, -this.tu.z, -(d + 0.5));
        this.planes[5] = new Plane(+this.tu.x, +this.tu.y, +this.tu.z, -(d + 0.5));

        this.center = ru.add3(su).add3(tu).multiply3(0.5);

        Vec4 rHalf = r.multiply3(0.5);
        this.topCenter = this.center.add3(rHalf);
        this.bottomCenter = this.center.subtract3(rHalf);
    }

    /**
     * Returns the box's center point.
     *
     * @return the box's center point.
     */
    public Vec4 getCenter()
    {
        return this.center;
    }

    /**
     * Returns the point corresponding to the center of the box side left-most along the R (first) axis.
     *
     * @return the bottom-center point.
     */
    public Vec4 getBottomCenter()
    {
        return this.bottomCenter;
    }

    /**
     * Returns the point corresponding to the center of the box side right-most along the R (first) axis.
     *
     * @return the top-center point.
     */
    public Vec4 getTopCenter()
    {
        return this.topCenter;
    }

    /**
     * Returns the R (first) axis. The axis length is the distance between the box sides perpendicular to the axis.
     *
     * @return the R axis.
     */
    public Vec4 getRAxis()
    {
        return this.r;
    }

    /**
     * Returns the S (second) axis. The axis length is the distance between the box sides perpendicular to the axis.
     *
     * @return the S axis.
     */
    public Vec4 getSAxis()
    {
        return this.s;
    }

    /**
     * Returns the T (third) axis. The axis length is the distance between the box sides perpendicular to the axis.
     *
     * @return the T axis.
     */
    public Vec4 getTAxis()
    {
        return this.t;
    }

    /**
     * Returns the R (first) axis in unit length.
     *
     * @return the unit R axis.
     */
    public Vec4 getUnitRAxis()
    {
        return this.ru;
    }

    /**
     * Returns the S (second) axis in unit length.
     *
     * @return the unit S axis.
     */
    public Vec4 getUnitSAxis()
    {
        return this.su;
    }

    /**
     * Returns the T (third) axis in unit length.
     *
     * @return the unit T axis.
     */
    public Vec4 getUnitTAxis()
    {
        return this.tu;
    }

    /**
     * Returns the eight corners of the box.
     *
     * @return the eight box corners in the order bottom-lower-left, bottom-lower-right, bottom-upper-right,
     *         bottom-upper-left, top-lower-left, top-lower-right, top-upper-right, top-upper-left.
     */
    public Vec4[] getCorners()
    {
        Vec4 ll = this.s.add3(this.t).multiply3(-0.5);     // Lower left.
        Vec4 lr = this.t.subtract3(this.s).multiply3(0.5); // Lower right.
        Vec4 ur = this.s.add3(this.t).multiply3(0.5);      // Upper right.
        Vec4 ul = this.s.subtract3(this.t).multiply3(0.5); // Upper left.

        Vec4[] corners = new Vec4[8];
        corners[0] = this.bottomCenter.add3(ll);
        corners[1] = this.bottomCenter.add3(lr);
        corners[2] = this.bottomCenter.add3(ur);
        corners[3] = this.bottomCenter.add3(ul);
        corners[4] = this.topCenter.add3(ll);
        corners[5] = this.topCenter.add3(lr);
        corners[6] = this.topCenter.add3(ur);
        corners[7] = this.topCenter.add3(ul);

        return corners;
    }

    /**
     * Returns the six planes of the box. The plane normals are directed outwards from the box.
     *
     * @return the six box planes in the order R-min, R-max, S-min, S-max, T-min, T-max.
     */
    public Plane[] getPlanes()
    {
        return this.planes;
    }

    /**
     * Returns the length of the R axis.
     *
     * @return the length of the R axis.
     */
    public double getRLength()
    {
        return rLength;
    }

    /**
     * Returns the length of the S axis.
     *
     * @return the length of the S axis.
     */
    public double getSLength()
    {
        return sLength;
    }

    /**
     * Returns the length of the T axis.
     *
     * @return the length of the T axis.
     */
    public double getTLength()
    {
        return tLength;
    }

    /**
     * Returns the effective diameter of the box as if it were a sphere. The length returned is the square root of the
     * sum of the squares of axis lengths.
     *
     * @return the effective diameter of the box.
     */
    public double getDiameter()
    {
        return Math.sqrt(this.rLength * this.rLength + this.sLength * this.sLength + this.tLength * this.tLength);
    }

    /**
     * Returns the effective radius of the box as if it were a sphere. The length returned is half the square root of
     * the sum of the squares of axis lengths.
     *
     * @return the effective radius of the box.
     */
    public double getRadius()
    {
        return 0.5 * this.getDiameter();
    }

    public Box translate(Vec4 point)
    {
        Vec4 bc = this.bottomCenter.add3(point);
        Vec4 tc = this.topCenter.add3(point);
        Vec4 c = this.center.add3(point);

        Plane[] newPlanes = new Plane[this.planes.length];
        for (int i = 0; i < this.planes.length; i++)
        {
            Plane pl = this.planes[i];
            Vec4 n = pl.getNormal();
            newPlanes[i] = new Plane(n.x, n.y, n.z, pl.getDistance() - (n.dot3(point)));
        }

        return new Box(bc, tc, c, this.r, this.s, this.t, this.ru, this.su, this.tu, this.rLength, this.sLength,
            this.tLength, newPlanes);
    }

    /**
     * Compute a <code>Box</code> that bounds a specified list of points. Principal axes are computed for the points and
     * used to form a <code>Box</code>.
     *
     * @param points the points for which to compute a bounding volume.
     *
     * @return the bounding volume, with axes lengths consistent with the conventions described in the <code>Box</code>
     *         class overview.
     *
     * @throws IllegalArgumentException if the point list is null or empty.
     */
    public static Box computeBoundingBox(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4[] axes = WWMath.computePrincipalAxes(points);
        if (axes == null)
        {
            String msg = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 r = axes[0];
        Vec4 s = axes[1];
        Vec4 t = axes[2];

        // Find the extremes along each axis.
        double minDotR = Double.MAX_VALUE;
        double maxDotR = -minDotR;
        double minDotS = Double.MAX_VALUE;
        double maxDotS = -minDotS;
        double minDotT = Double.MAX_VALUE;
        double maxDotT = -minDotT;

        for (Vec4 p : points)
        {
            if (p == null)
                continue;

            double pdr = p.dot3(r);
            if (pdr < minDotR)
                minDotR = pdr;
            if (pdr > maxDotR)
                maxDotR = pdr;

            double pds = p.dot3(s);
            if (pds < minDotS)
                minDotS = pds;
            if (pds > maxDotS)
                maxDotS = pds;

            double pdt = p.dot3(t);
            if (pdt < minDotT)
                minDotT = pdt;
            if (pdt > maxDotT)
                maxDotT = pdt;
        }

        if (maxDotR == minDotR)
            maxDotR = minDotR + 1;
        if (maxDotS == minDotS)
            maxDotS = minDotS + 1;
        if (maxDotT == minDotT)
            maxDotT = minDotT + 1;

        return new Box(axes, minDotR, maxDotR, minDotS, maxDotS, minDotT, maxDotT);
    }

    /**
     * Computes a <code>Box</code> that bounds a specified buffer of points. Principal axes are computed for the points
     * and used to form a <code>Box</code>.
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
     * @return the bounding volume, with axes lengths consistent with the conventions described in the <code>Box</code>
     *         class overview.
     *
     * @throws IllegalArgumentException if the buffer is null or empty, or if the stride is less than three.
     */
    public static Box computeBoundingBox(BufferWrapper coordinates, int stride)
    {
        if (coordinates == null)
        {
            String msg = Logging.getMessage("nullValue.CoordinatesAreNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (stride < 3)
        {
            String msg = Logging.getMessage("generic.StrideIsInvalid", stride);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4[] axes = WWMath.computePrincipalAxes(coordinates, stride);
        if (axes == null)
        {
            String msg = Logging.getMessage("generic.ListIsEmpty");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 r = axes[0];
        Vec4 s = axes[1];
        Vec4 t = axes[2];

        // Find the extremes along each axis.
        double minDotR = Double.MAX_VALUE;
        double maxDotR = -minDotR;
        double minDotS = Double.MAX_VALUE;
        double maxDotS = -minDotS;
        double minDotT = Double.MAX_VALUE;
        double maxDotT = -minDotT;

        for (int i = 0; i <= coordinates.length() - stride; i += stride)
        {
            double x = coordinates.getDouble(i);
            double y = coordinates.getDouble(i + 1);
            double z = coordinates.getDouble(i + 2);

            double pdr = x * r.x + y * r.y + z * r.z;
            if (pdr < minDotR)
                minDotR = pdr;
            if (pdr > maxDotR)
                maxDotR = pdr;

            double pds = x * s.x + y * s.y + z * s.z;
            if (pds < minDotS)
                minDotS = pds;
            if (pds > maxDotS)
                maxDotS = pds;

            double pdt = x * t.x + y * t.y + z * t.z;
            if (pdt < minDotT)
                minDotT = pdt;
            if (pdt > maxDotT)
                maxDotT = pdt;
        }

        if (maxDotR == minDotR)
            maxDotR = minDotR + 1;
        if (maxDotS == minDotS)
            maxDotS = minDotS + 1;
        if (maxDotT == minDotT)
            maxDotT = minDotT + 1;

        return new Box(axes, minDotR, maxDotR, minDotS, maxDotS, minDotT, maxDotT);
    }

    /**
     * Computes a <code>Box</code> that represents the union of one or more <code>Boxes</code>. If the iterable has two
     * or more non-<code>null</code> <code>Boxes</code>, the returned encloses the <code>Boxes</code>. In this case axes
     * and center point of the returned <code>Box</code> may differ from the <code>Boxes</code>. If the iterable has
     * only one non-<code>null</code> <code>Box</code>, this returns that sole <code>Box</code>. This returns
     * <code>null</code> if the iterable is empty or contains only <code>null</code> references.
     *
     * @param iterable an iterable of <code>Boxes</code> to enclose.
     *
     * @return a new <code>Box</code> that encloses the specified iterable of <code>Boxes</code>.
     *
     * @throws IllegalArgumentException if the <code>iterable</code> is null.
     */
    public static Box union(Iterable<? extends Box> iterable)
    {
        if (iterable == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        ArrayList<Box> boxes = new ArrayList<Box>();

        for (Box box : iterable)
        {
            if (box == null)
                continue;

            boxes.add(box);
        }

        if (boxes.size() == 0)
        {
            return null;
        }
        else if (boxes.size() == 1)
        {
            // If the iterable contains only a single non-null box, we avoid unnecessarily computing its bouding box and
            // just return it directly. This also ensures that we do not return a box larger than the original box, by
            // performing a principal component analysis on the corners of a single box.
            return boxes.get(0);
        }
        else
        {
            // If the iterable contains two or more boxes, gather up their corners and return a box that encloses the
            // boxes corners. We create an ArrayList with enough room to hold all the boxes corners to avoid unnecessary
            // overhead.
            ArrayList<Vec4> corners = new ArrayList<Vec4>(8 * boxes.size());
            for (Box box : boxes)
            {
                corners.addAll(Arrays.asList(box.getCorners()));
            }

            return computeBoundingBox(corners);
        }
    }

    /** {@inheritDoc} */
    public boolean intersects(Frustum frustum)
    {
        // FYI: this code is identical to that in Cylinder.intersects.

        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double intersectionPoint;
        Vec4[] endPoints = new Vec4[] {this.bottomCenter, this.topCenter};

        double effectiveRadius = this.getEffectiveRadius2(frustum.getNear());
        intersectionPoint = this.intersectsAt(frustum.getNear(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        // Near and far have the same effective radius.
        effectiveRadius = this.getEffectiveRadius2(frustum.getFar());
        intersectionPoint = this.intersectsAt(frustum.getFar(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius2(frustum.getLeft());
        intersectionPoint = this.intersectsAt(frustum.getLeft(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius2(frustum.getRight());
        intersectionPoint = this.intersectsAt(frustum.getRight(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius2(frustum.getTop());
        intersectionPoint = this.intersectsAt(frustum.getTop(), effectiveRadius, endPoints);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius2(frustum.getBottom());
        intersectionPoint = this.intersectsAt(frustum.getBottom(), effectiveRadius, endPoints);
        return intersectionPoint >= 0;
    }

    /**
     * Returns the effective radius of this box relative to a specified plane, using only this box's S and T axes. This
     * is an optimization available when using the effective radius to test the distance from a plane to the line
     * segment along this box's R axis, as is done in this class' {@link #intersects(Frustum)} method. See Lengyel, 2
     * Ed, Section 7.2.4.
     *
     * @param plane the plane in question.
     *
     * @return the effective radius of this box relative to the specified plane, using only this box's S and T axes to
     *         determine the effective radius.
     */
    protected double getEffectiveRadius2(Plane plane)
    {
        if (plane == null)
            return 0;

        // Determine the effective radius of the box axis relative to the plane, use only the S and T axes because the
        // R axis is incorporated into the endpoints of the line this place is being tested against.
        Vec4 n = plane.getNormal();
        return 0.5 * (Math.abs(this.s.dot3(n)) + Math.abs(this.t.dot3(n)));
    }

    /** {@inheritDoc} */
    public double getEffectiveRadius(Plane plane)
    {
        if (plane == null)
            return 0;

        // Determine the effective radius of the box axis relative to the plane.
        Vec4 n = plane.getNormal();
        return 0.5 * (Math.abs(this.s.dot3(n)) + Math.abs(this.t.dot3(n)) + Math.abs(this.r.dot3(n)));
    }

    protected double intersectsAt(Plane plane, double effectiveRadius, Vec4[] endpoints)
    {
        // Test the distance from the first end-point.
        double dq1 = plane.dot(endpoints[0]);
        boolean bq1 = dq1 <= -effectiveRadius;

        // Test the distance from the possibly reduced second end-point.
        double dq2 = plane.dot(endpoints[1]);
        boolean bq2 = dq2 <= -effectiveRadius;

        if (bq1 && bq2) // endpoints more distant from plane than effective radius; box is on neg. side of plane
            return -1;

        if (bq1 == bq2) // endpoints less distant from plane than effective radius; can't draw any conclusions
            return 0;

        // Compute and return the endpoints of the cylinder on the positive side of the plane.
        double t = (effectiveRadius + dq1) / plane.getNormal().dot3(endpoints[0].subtract3(endpoints[1]));

        Vec4 newEndPoint = endpoints[0].add3(endpoints[1].subtract3(endpoints[0]).multiply3(t));
        // truncate the line to only that in the positive halfspace (e.g., inside the frustum)
        if (bq1)
            endpoints[0] = newEndPoint;
        else
            endpoints[1] = newEndPoint;

        return t;
    }

    /** {@inheritDoc} */
    public boolean intersects(Plane plane)
    {
        if (plane == null)
        {
            String message = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double effectiveRadius = this.getEffectiveRadius(plane);
        return this.intersects(plane, effectiveRadius) >= 0;
    }

    protected double intersects(Plane plane, double effectiveRadius)
    {
        // Test the distance from the first end-point.
        double dq1 = plane.dot(this.bottomCenter);
        boolean bq1 = dq1 <= -effectiveRadius;

        // Test the distance from the top of the box.
        double dq2 = plane.dot(this.topCenter);
        boolean bq2 = dq2 <= -effectiveRadius;

        if (bq1 && bq2) // both beyond effective radius; box is on negative side of plane
            return -1;

        if (bq1 == bq2) // both within effective radius; can't draw any conclusions
            return 0;

        return 1; // box almost certainly intersects
    }

    /** {@inheritDoc} */
    public Intersection[] intersect(Line line)
    {
        return WWMath.polytopeIntersect(line, this.planes);
    }

    /** {@inheritDoc} */
    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return WWMath.polytopeIntersect(line, this.planes) != null;
    }

    /** {@inheritDoc} */
    public double getProjectedArea(View view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Implementation based on "Real-time Bounding Box Area Computation" by Dieter Schmalstieg and Robert F. Tobler,
        // Vienna University of Technology:
        // http://jgt.akpeters.com/papers/SchmalstiegTobler99/

        // Compute the exact area of this Box in the screen by determining exact area covered by this Box's projected
        // vertices. We avoid computing a 2D bounding box of the projected vertices, because it is not necessarily more
        // efficient and is is less accurate: it is an approximation of an approximation. We start by computing the 2D
        // hull of this Box's vertices in screen coordinates. We create a 6-bit index into a predetermined table of
        // possible vertex combinations by comparing the eye point to each of the six planes. The resultant index
        // determines the vertices that define the 2D hull for that viewport.
        int lookupCode = this.computeProjectionHullCode(view);

        // Index 0 indicates that the view is inside this Box. Return positive infinity, indicating that this Box does
        // not have a finite area in the viewport.
        if (lookupCode == 0)
            return Double.POSITIVE_INFINITY;

        if (lookupCode < 0 || lookupCode >= ProjectionHullTable.length)
            return 0; // This should never happen, but we check anyway.

        // Get the 4 or 6 vertex indices that define this Box's convex hull in screen coordinates. Each element is used
        // as an index into this Box's array of corners.
        int[] indices = ProjectionHullTable[lookupCode];
        if (indices == null || (indices.length != 4 && indices.length != 6))
            return 0; // This should never happen, but we check anyway.

        // Compute this Box's convex hull in screen coordinates, by transforming the 4 or 6 vertices that define its
        // projected outline from model coordinates into screen coordinates.
        Vec4[] vertices = this.getCorners();
        Vec4[] screenVertices = new Vec4[indices.length];

        // If any of this Box's vertices are behind the eye point, return positive infinity indicating that this Box
        // does not have a finite area in the viewport.
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < indices.length; i++)
        {
            Vec4 eyeVertex = vertices[indices[i]].transformBy4(view.getModelviewMatrix());
            if (eyeVertex.z >= 0)
                return Double.POSITIVE_INFINITY;
        }

        for (int i = 0; i < indices.length; i++)
        {
            screenVertices[i] = view.project(vertices[indices[i]]);
        }

        // Compute the area of the convex hull by treating the projected vertices as a 2D polygon. If this Box's r, s,
        // and t axes define a right-handed coordinate system, the vertices have a counter-clockwise winding order on
        // screen and the area is positive. If the axes define a left-handed coordinate system, the vertices have a
        // clockwise winding order on scree and the area is negative. We return the area's absolute value to handle
        // either case.
        double area = WWMath.computePolygonAreaFromVertices(Arrays.asList(screenVertices));
        return Math.abs(area);
    }

    /**
     * Computes an index into the <code>ProjectionHullTable</code> for this <code>Box</code> given the specified
     * <code>view</code>. The returned integer is a 6-bit code, where each bit denotes whether one of this
     * <code>Box's</code> six planes faces the <code>View</code>. See the documentation for <code>{@link
     * #ProjectionHullTable}</code> for details.
     * <p>
     * If the <code>view</code> is inside this <code>Box</code>, this returns 0 indicating that none of this
     * <code>Box's</code> planes face the <code>view</code>.
     *
     * @param view the <code>View</code> to compute a lookup index for.
     *
     * @return an integer who's first 6 bits define an index into the <code>ProjectionHullTable</code>.
     */
    protected int computeProjectionHullCode(View view)
    {
        // Transform the view's eye point from world coordinates to this box's local coordinates. We use this box's r, s,
        // and t unit vectors axes as the x, y, and z axes of the local coordinate system. The r-axis is orthogonal to
        // the top and bottom sides, the s-axis is orthogonal to the front and back sides, and the t-axis is orthogonal
        // to the left and right sides.
        Vec4 p = view.getEyePoint().subtract3(this.center);
        double dr = p.dot3(this.ru);
        double ds = p.dot3(this.su);
        double dt = p.dot3(this.tu);

        return (dr > this.rLength / 2.0 ? 1 : 0)           // bit 0: top
            | ((dr < (-this.rLength / 2.0) ? 1 : 0) << 1)  // bit 1: bottom
            | ((ds > this.sLength / 2.0 ? 1 : 0) << 2)     // bit 2: front
            | ((ds < (-this.sLength / 2.0) ? 1 : 0) << 3)  // bit 3: back
            | ((dt > this.tLength / 2.0 ? 1 : 0) << 4)     // bit 4: right
            | ((dt < (-this.tLength / 2.0) ? 1 : 0) << 5); // bit 5: left
    }

//    public static void main(String[] args)
//    {
//        Box box = new Box(new Vec4[] {new Vec4(1, 0, 0), new Vec4(0, 1, 0), new Vec4(0, 0, 1)},
//            -.5, .5, -.5, .5, -.5, .5);
//        Line line = new Line(new Vec4(-1, 0.5, 0.5), new Vec4(1, 0, 0));
//        Intersection[] intersections = box.intersect(line);
//        if (intersections != null && intersections.length > 0 && intersections[0] != null)
//            System.out.println(intersections[0]);
//        if (intersections != null && intersections.length > 1 && intersections[1] != null)
//            System.out.println(intersections[1]);
//    }

//    /** {@inheritDoc} */
//    public Intersection[] intersect(Line line)
//    {
//        return WWMath.polytopeIntersect(line, this.planes);
//        // Algorithm from "3-D Computer Graphics" by Samuel R. Buss, 2005, Section X.1.4.
//
//        // Determine intersection with each plane and categorize the intersections as "front" if the line intersects
//        // the front side of the plane (dot product of line direction with plane normal is negative) and "back" if the
//        // line intersects the back side of the plane (dot product of line direction with plane normal is positive).
//
//        double fMax = -Double.MAX_VALUE;
//        double bMin = Double.MAX_VALUE;
//        boolean isTangent = false;
//
//        Vec4 u = line.getDirection();
//        Vec4 p = line.getOrigin();
//
//        for (Plane plane : this.planes)
//        {
//            Vec4 n = plane.getNormal();
//            double d = -plane.getDistance();
//
//            double s = u.dot3(n);
//            if (s == 0) // line is parallel to plane
//            {
//                double pdn = p.dot3(n);
//                if (pdn > d) // is line in positive halfspace (in front of) of the plane?
//                    return null; // no intersection
//                else
//                {
//                    if (pdn == d)
//                        isTangent = true; // line coincident with plane
//                    continue; // line is in negative halfspace; possible intersection; check other planes
//                }
//            }
//
//            // Determine whether front or back intersection.
//            double a = (d - p.dot3(n)) / s;
//            if (u.dot3(n) < 0) // line intersects front face and therefore entering box
//            {
//                if (a > fMax)
//                {
//                    if (a > bMin)
//                        return null;
//                    fMax = a;
//                }
//            }
//            else // line intersects back face and therefore leaving box
//            {
//                if (a < bMin)
//                {
//                    if (a < 0 || a < fMax)
//                        return null;
//                    bMin = a;
//                }
//            }
//        }
//
//        // Compute the Cartesian intersection points. There will be no more than two.
//        if (fMax >= 0) // intersects frontface and backface; point origin is outside the box
//            return new Intersection[]
//                {
//                    new Intersection(p.add3(u.multiply3(fMax)), isTangent),
//                    new Intersection(p.add3(u.multiply3(bMin)), isTangent)
//                };
//        else // intersects backface only; point origin is within the box
//            return new Intersection[] {new Intersection(p.add3(u.multiply3(bMin)), isTangent)};
//    }

    /**
     * Draws a representation of the <code>Box</code>.
     *
     * @param dc the <code>DrawContext</code> to be used.
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.isPickingMode())
            return;

        Vec4 a = this.s.add3(this.t).multiply3(-0.5);
        Vec4 b = this.s.subtract3(this.t).multiply3(0.5);
        Vec4 c = this.s.add3(this.t).multiply3(0.5);
        Vec4 d = this.t.subtract3(this.s).multiply3(0.5);

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushAttrib(gl, GL2.GL_COLOR_BUFFER_BIT // For alpha enable, blend enable, alpha func, blend func.
            | GL2.GL_CURRENT_BIT // For current color.
            | GL2.GL_LINE_BIT // For line width.
            | GL2.GL_TRANSFORM_BIT // For matrix mode.
            | GL2.GL_DEPTH_BUFFER_BIT); // For depth test enable, depth func.
        try
        {
            gl.glLineWidth(1f);
            gl.glEnable(GL.GL_BLEND);
            OGLUtil.applyBlending(gl, false);
            gl.glEnable(GL.GL_DEPTH_TEST);

            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glColor4f(1f, 1f, 1f, 0.5f);
            this.drawBox(dc, a, b, c, d);

            gl.glDepthFunc(GL.GL_GREATER);
            gl.glColor4f(1f, 0f, 1f, 0.4f);
            this.drawBox(dc, a, b, c, d);
        }
        finally
        {
            ogsh.pop(gl);
        }
    }

    protected void drawBox(DrawContext dc, Vec4 a, Vec4 b, Vec4 c, Vec4 d)
    {
        Vec4 e = a.add3(this.r);
        Vec4 f = d.add3(this.r);
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        dc.getView().pushReferenceCenter(dc, this.bottomCenter);
        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushModelview(gl);
        try
        {
            // Draw parallel lines in R direction
            int n = 20;
            Vec4 dr = this.r.multiply3(1d / (double) n);

            this.drawOutline(dc, a, b, c, d);
            for (int i = 1; i < n; i++)
            {
                gl.glTranslated(dr.x, dr.y, dr.z);
                this.drawOutline(dc, a, b, c, d);
            }

            // Draw parallel lines in S direction
            n = 20;
            Vec4 ds = this.s.multiply3(1d / (double) n);

            gl.glPopMatrix();
            gl.glPushMatrix();
            this.drawOutline(dc, a, e, f, d);
            for (int i = 1; i < n; i++)
            {
                gl.glTranslated(ds.x, ds.y, ds.z);
                this.drawOutline(dc, a, e, f, d);
            }
        }
        finally
        {
            ogsh.pop(gl);
            dc.getView().popReferenceCenter(dc);
        }
    }

    protected void drawOutline(DrawContext dc, Vec4 a, Vec4 b, Vec4 c, Vec4 d)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(a.x, a.y, a.z);
        gl.glVertex3d(b.x, b.y, b.z);
        gl.glVertex3d(c.x, c.y, c.z);
        gl.glVertex3d(d.x, d.y, d.z);
        gl.glEnd();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Box))
            return false;

        Box box = (Box) o;

        if (center != null ? !center.equals(box.center) : box.center != null)
            return false;
        if (r != null ? !r.equals(box.r) : box.r != null)
            return false;
        if (s != null ? !s.equals(box.s) : box.s != null)
            return false;
        //noinspection RedundantIfStatement
        if (t != null ? !t.equals(box.t) : box.t != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = center != null ? center.hashCode() : 0;
        result = 31 * result + (r != null ? r.hashCode() : 0);
        result = 31 * result + (s != null ? s.hashCode() : 0);
        result = 31 * result + (t != null ? t.hashCode() : 0);
        return result;
    }
}
