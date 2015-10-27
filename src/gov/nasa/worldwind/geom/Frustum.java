/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * Represents a view frustum composed of six planes: left, right, bottom, top, near far.
 * <p/>
 * Frustum instances are immutable.
 *
 * @author Tom Gaskins
 * @version $Id: Frustum.java 2178 2014-07-25 16:40:09Z dcollins $
 */
public class Frustum
{
    protected final Plane left;
    protected final Plane right;
    protected final Plane bottom;
    protected final Plane top;
    protected final Plane near;
    protected final Plane far;
    /** Holds all six frustum planes in an array in the order left, right, bottom, top, near, far. */
    protected final Plane[] allPlanes;

    /** Constructs a frustum two meters wide centered at the origin. Primarily used for testing. */
    public Frustum()
    {
        this(
            new Plane(1, 0, 0, 1),  // Left
            new Plane(-1, 0, 0, 1),  // Right
            new Plane(0, 1, 0, 1),  // Bottom
            new Plane(0, -1, 0, 1),  // Top
            new Plane(0, 0, -1, 1),  // Near
            new Plane(0, 0, 1, 1)); // Far
    }

    /**
     * Create a frustum from six {@link gov.nasa.worldwind.geom.Plane}s defining the frustum boundaries.
     * <p/>
     * None of the arguments may be null.
     *
     * @param near   the near plane
     * @param far    the far plane
     * @param left   the left plane
     * @param right  the right plane
     * @param top    the top plane
     * @param bottom the bottom plane
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public Frustum(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far)
    {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null)
        {
            String message = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        this.allPlanes = new Plane[] {this.left, this.right, this.bottom, this.top, this.near, this.far};
    }

    /**
     * Returns the left plane.
     *
     * @return the left plane.
     */
    public final Plane getLeft()
    {
        return this.left;
    }

    /**
     * Returns the right plane.
     *
     * @return the right plane.
     */
    public final Plane getRight()
    {
        return this.right;
    }

    /**
     * Returns the bottom plane.
     *
     * @return the bottom plane.
     */
    public final Plane getBottom()
    {
        return this.bottom;
    }

    /**
     * Returns the top plane.
     *
     * @return the top plane.
     */
    public final Plane getTop()
    {
        return this.top;
    }

    /**
     * Returns the near plane.
     *
     * @return the left plane.
     */
    public final Plane getNear()
    {
        return this.near;
    }

    /**
     * Returns the far plane.
     *
     * @return the left plane.
     */
    public final Plane getFar()
    {
        return this.far;
    }

    /**
     * Returns all the planes.
     *
     * @return an array of the frustum planes, in the order left, right, bottom, top, near, far.
     */
    public Plane[] getAllPlanes()
    {
        return this.allPlanes;
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Frustum that = (Frustum) obj;
        return this.left.equals(that.left)
            && this.right.equals(that.right)
            && this.bottom.equals(that.bottom)
            && this.top.equals(that.top)
            && this.near.equals(that.near)
            && this.far.equals(that.far);
    }

    public int hashCode()
    {
        int result;
        result = this.left.hashCode();
        result = 31 * result + this.right.hashCode();
        result = 19 * result + this.bottom.hashCode();
        result = 23 * result + this.top.hashCode();
        result = 17 * result + this.near.hashCode();
        result = 19 * result + this.far.hashCode();

        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("left=").append(this.left);
        sb.append(", right=").append(this.right);
        sb.append(", bottom=").append(this.bottom);
        sb.append(", top=").append(this.top);
        sb.append(", near=").append(this.near);
        sb.append(", far=").append(this.far);
        sb.append(")");
        return sb.toString();
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    /**
     * Creates a frustum by extracting the six frustum planes from a projection matrix.
     *
     * @param projectionMatrix the projection matrix to extract the frustum planes from.
     *
     * @return a frustum defined by the extracted planes.
     *
     * @throws IllegalArgumentException if the projection matrix is null.
     */
    public static Frustum fromProjectionMatrix(Matrix projectionMatrix)
    {
        //noinspection UnnecessaryLocalVariable
        Matrix m = projectionMatrix;
        if (m == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Left Plane = row 4 + row 1:
        double x = m.m41 + m.m11;
        double y = m.m42 + m.m12;
        double z = m.m43 + m.m13;
        double w = m.m44 + m.m14;
        double d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane left = new Plane(x / d, y / d, z / d, w / d);

        // Right Plane = row 4 - row 1:
        x = m.m41 - m.m11;
        y = m.m42 - m.m12;
        z = m.m43 - m.m13;
        w = m.m44 - m.m14;
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane right = new Plane(x / d, y / d, z / d, w / d);

        // Bottom Plane = row 4 + row 2:
        x = m.m41 + m.m21;
        y = m.m42 + m.m22;
        z = m.m43 + m.m23;
        w = m.m44 + m.m23;
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane bottom = new Plane(x / d, y / d, z / d, w / d);

        // Top Plane = row 4 - row 2:
        x = m.m41 - m.m21;
        y = m.m42 - m.m22;
        z = m.m43 - m.m23;
        w = m.m44 - m.m23;
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane top = new Plane(x / d, y / d, z / d, w / d);

        // Near Plane = row 4 + row 3:
        x = m.m41 + m.m31;
        y = m.m42 + m.m32;
        z = m.m43 + m.m33;
        w = m.m44 + m.m34;
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane near = new Plane(x / d, y / d, z / d, w / d);

        // Far Plane = row 4 - row 3:
        x = m.m41 - m.m31;
        y = m.m42 - m.m32;
        z = m.m43 - m.m33;
        w = m.m44 - m.m34;
        d = Math.sqrt(x * x + y * y + z * z); // for normalizing the coordinates
        Plane far = new Plane(x / d, y / d, z / d, w / d).normalize();

        return new Frustum(left, right, bottom, top, near, far);
    }

    /**
     * Creates a <code>Frustum</code> from a horizontal field-of-view, viewport aspect ratio and distance to near and
     * far depth clipping planes. The near plane must be closer than the far plane, and both near and far values must be
     * positive.
     *
     * @param horizontalFieldOfView horizontal field-of-view angle in the range (0, 180)
     * @param viewportWidth         the width of the viewport in screen pixels
     * @param viewportHeight        the height of the viewport in screen pixels
     * @param near                  distance to the near depth clipping plane
     * @param far                   distance to far depth clipping plane
     *
     * @return Frustum configured from the specified perspective parameters.
     *
     * @throws IllegalArgumentException if fov is not in the range (0, 180), if either near or far are negative, or near
     *                                  is greater than or equal to far
     */
    public static Frustum fromPerspective(Angle horizontalFieldOfView, int viewportWidth, int viewportHeight,
        double near, double far)
    {
        if (horizontalFieldOfView == null)
        {
            String message = Logging.getMessage("Geom.ViewFrustum.FieldOfViewIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        double fov = horizontalFieldOfView.getDegrees();
        double farMinusNear = far - near;
        String message = null;
        if (fov <= 0 || fov > 180)
            message = Logging.getMessage("Geom.ViewFrustum.FieldOfViewOutOfRange", fov);
        if (near <= 0 || farMinusNear <= 0)
            message = Logging.getMessage("Geom.ViewFrustum.ClippingDistanceOutOfRange");
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double focalLength = 1d / horizontalFieldOfView.tanHalfAngle();
        double aspect = viewportHeight / (double) viewportWidth;
        double lrLen = Math.sqrt(focalLength * focalLength + 1);
        double btLen = Math.sqrt(focalLength * focalLength + aspect * aspect);
        Plane leftPlane = new Plane(focalLength / lrLen, 0d, 0d - 1d / lrLen, 0);
        Plane rightPlane = new Plane(0d - focalLength / lrLen, 0d, 0d - 1d / lrLen, 0d);
        Plane bottomPlane = new Plane(0d, focalLength / btLen, 0d - aspect / btLen, 0d);
        Plane topPlane = new Plane(0d, 0d - focalLength / btLen, 0d - aspect / btLen, 0d);
        Plane nearPlane = new Plane(0d, 0d, 0d - 1d, 0d - near);
        Plane farPlane = new Plane(0d, 0d, 1d, far);
        return new Frustum(leftPlane, rightPlane, bottomPlane, topPlane, nearPlane, farPlane);
    }

    /**
     * Creates a <code>Frustum</code> from three sets of parallel clipping planes (a parallel projectionMatrix). In this
     * case, the near and far depth clipping planes may be negative.
     *
     * @param near   distance to the near depth clipping plane
     * @param far    distance to far depth clipping plane
     * @param width  horizontal dimension of the near clipping plane
     * @param height vertical dimension of the near clipping plane
     *
     * @return a Frustum configured with the specified perspective parameters.
     *
     * @throws IllegalArgumentException if the difference of any plane set (lright - left, top - bottom, far - near) is
     *                                  less than or equal to zero.
     */
    public static Frustum fromPerspective(double width, double height, double near, double far)
    {
        double farMinusNear = far - near;
        if (farMinusNear <= 0.0 || width <= 0.0 || height <= 0.0)
        {
            String message = Logging.getMessage("Geom.ViewFrustum.ClippingDistanceOutOfRange");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        double width_over_2 = width / 2.0;
        double height_over_2 = height / 2.0;
        Plane leftPlane = new Plane(1.0, 0.0, 0.0, width_over_2);
        Plane rightPlane = new Plane(-1.0, 0.0, 0.0, width_over_2);
        Plane bottomPlane = new Plane(0.0, 1.0, 0.0, height_over_2);
        Plane topPlane = new Plane(0.0, -1.0, 0.0, height_over_2);
        Plane nearPlane = new Plane(0.0, 0.0, -1.0, (near < 0.0) ? near : -near);
        Plane farPlane = new Plane(0.0, 0.0, 1.0, (far < 0.0) ? -far : far);
        return new Frustum(leftPlane, rightPlane, bottomPlane, topPlane, nearPlane, farPlane);
    }

    /**
     * Creates a <code>Frustum</code> from four edge vectors, viewport aspect ratio and distance to near and far planes.
     * The edge vectors connect the near corners of the frustum to the far corners. The near plane must be closer than
     * the far plane, and both planes must be positive.
     *
     * @param vTL  vector defining the top-left of the frustum
     * @param vTR  vector defining the top-right of the frustum
     * @param vBL  vector defining the bottom-left of the frustum
     * @param vBR  vector defining the bottom-right of the frustum
     * @param near distance to the near plane
     * @param far  distance to far plane
     *
     * @return Frustum that was created
     *
     * @throws IllegalArgumentException if any of the vectors are null, if either near or far are negative, or near is
     *                                  greater than or equal to far
     */
    public static Frustum fromPerspectiveVecs(Vec4 vTL, Vec4 vTR, Vec4 vBL, Vec4 vBR,
        double near, double far)
    {
        if (vTL == null || vTR == null || vBL == null || vBR == null)
        {
            String message = Logging.getMessage("Geom.ViewFrustum.EdgeVectorIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        double farMinusNear = far - near;
        if (near <= 0 || farMinusNear <= 0)
        {
            String message = Logging.getMessage("Geom.ViewFrustum.ClippingDistanceOutOfRange");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 lpn = vBL.cross3(vTL).normalize3();
        Plane leftPlane = new Plane(lpn.x, lpn.y, lpn.z, 0);
        Vec4 rpn = vTR.cross3(vBR).normalize3();
        Plane rightPlane = new Plane(rpn.x, rpn.y, rpn.z, 0);
        Vec4 bpn = vBR.cross3(vBL).normalize3();
        Plane bottomPlane = new Plane(bpn.x, bpn.y, bpn.z, 0);
        Vec4 tpn = vTL.cross3(vTR).normalize3();
        Plane topPlane = new Plane(tpn.x, tpn.y, tpn.z, 0);

        Plane nearPlane = new Plane(0d, 0d, 0d - 1d, 0d - near);
        Plane farPlane = new Plane(0d, 0d, 1d, far);
        return new Frustum(leftPlane, rightPlane, bottomPlane, topPlane, nearPlane, farPlane);
    }

    // ============== Intersection Functions ======================= //
    // ============== Intersection Functions ======================= //
    // ============== Intersection Functions ======================= //

    /**
     * Indicates whether a specified {@link Extent} intersects this frustum.
     *
     * @param extent the Extent to test.
     *
     * @return true if the extent intersects this frustum, otherwise false.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public boolean intersects(Extent extent)
    {
        if (extent == null)
        {
            String msg = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        return extent.intersects(this);
    }

    /**
     * Determines whether a line segment intersects this frustum.
     *
     * @param pa one end of the segment.
     * @param pb the other end of the segment.
     *
     * @return true if the segment intersects or is contained in the frustum, otherwise false.
     *
     * @throws IllegalArgumentException if either point is null.
     */
    public boolean intersectsSegment(Vec4 pa, Vec4 pb)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // First do a trivial accept test.
        if (this.contains(pa) || this.contains(pb))
            return true;

        if (pa.equals(pb))
            return false;

        for (Plane p : this.getAllPlanes())
        {
            // See if both points are behind the plane and therefore not in the frustum.
            if (p.onSameSide(pa, pb) < 0)
                return false;

            // See if the segment intersects the plane.
            if (p.clip(pa, pb) != null)
                return true;
        }

        return false; // segment does not intersect frustum
    }

    /**
     * Indicates whether a specified {@link Extent} is fully within this frustum.
     *
     * @param extent the Extent to test.
     *
     * @return true if the extent is fully within this frustum, otherwise false.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public final boolean contains(Extent extent)
    {
        // TODO: This method should be implemented in the concrete extent classes and those implementing methods
        // invoked here, as is done above for intersects(Frustum).
        if (extent == null)
        {
            String msg = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        // See if the extent's bounding sphere is entirely within the frustum. The dot product of the extent's center
        // point with each plane's vector provides a distance to each plane. If this distance is less than the extent's
        // radius, some part of the extent is clipped by that plane and therefore is not completely contained in the
        // space enclosed by this Frustum.

        Vec4 c = extent.getCenter();
        double r = extent.getRadius();

        if (this.far.dot(c) <= r)
            return false;
        if (this.left.dot(c) <= r)
            return false;
        if (this.right.dot(c) <= r)
            return false;
        if (this.top.dot(c) <= r)
            return false;
        if (this.bottom.dot(c) <= r)
            return false;
        //noinspection RedundantIfStatement
        if (this.near.dot(c) <= r)
            return false;

        return true;
    }

    /**
     * Indicates whether a specified point is within this frustum.
     *
     * @param point the point to test.
     *
     * @return true if the point is within the frustum, otherwise false.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public final boolean contains(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        // See if the point is entirely within the frustum. The dot product of the point with each plane's vector
        // provides a distance to each plane. If this distance is less than 0, the point is clipped by that plane and
        // neither intersects nor is contained by the space enclosed by this Frustum.

        if (this.far.dot(point) <= 0)
            return false;
        if (this.left.dot(point) <= 0)
            return false;
        if (this.right.dot(point) <= 0)
            return false;
        if (this.top.dot(point) <= 0)
            return false;
        if (this.bottom.dot(point) <= 0)
            return false;
        //noinspection RedundantIfStatement
        if (this.near.dot(point) <= 0)
            return false;

        return true;
    }

    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //

    /**
     * Returns a copy of this frustum transformed by a specified {@link Matrix}.
     *
     * @param matrix the Matrix to apply to this frustum.
     *
     * @return a new frustum transformed by the specified matrix.
     *
     * @throws IllegalArgumentException if the matrix is null.
     */
    public Frustum transformBy(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        Plane left = new Plane(this.left.getVector().transformBy4(matrix));
        Plane right = new Plane(this.right.getVector().transformBy4(matrix));
        Plane bottom = new Plane(this.bottom.getVector().transformBy4(matrix));
        Plane top = new Plane(this.top.getVector().transformBy4(matrix));
        Plane near = new Plane(this.near.getVector().transformBy4(matrix));
        Plane far = new Plane(this.far.getVector().transformBy4(matrix));
        return new Frustum(left, right, bottom, top, near, far);
    }

    /** Holds the eight corner points of a frustum. */
    public static class Corners
    {
        public Vec4 nbl, nbr, ntl, ntr, fbl, fbr, ftl, ftr;
    }

    /**
     * Returns the eight corners of this frustum.
     *
     * @return the eight frustum corners.
     */
    public Corners getCorners()
    {
        Corners corners = new Corners();

        corners.nbl = Plane.intersect(this.near, this.bottom, this.left);
        corners.nbr = Plane.intersect(this.near, this.bottom, this.right);
        corners.ntl = Plane.intersect(this.near, this.top, this.left);
        corners.ntr = Plane.intersect(this.near, this.top, this.right);

        corners.fbl = Plane.intersect(this.far, this.bottom, this.left);
        corners.fbr = Plane.intersect(this.far, this.bottom, this.right);
        corners.ftl = Plane.intersect(this.far, this.top, this.left);
        corners.ftr = Plane.intersect(this.far, this.top, this.right);

        return corners;
    }
}
