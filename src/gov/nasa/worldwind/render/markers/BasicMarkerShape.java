/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.markers;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.*;
import javax.media.opengl.glu.gl2.GLUgl2;
import java.util.ArrayList;

/**
 * This is the base class for marker symbols.
 *
 * @author tag
 * @version $Id: BasicMarkerShape.java 2279 2014-08-29 21:32:19Z tgaskins $
 */
public class BasicMarkerShape
{
    public static final String SPHERE = "gov.nasa.worldwind.render.markers.Sphere";
    public static final String CUBE = "gov.nasa.worldwind.render.markers.Cube";
    public static final String CONE = "gov.nasa.worldwind.render.markers.Cone";
    public static final String CYLINDER = "gov.nasa.worldwind.render.markers.Cylinder";
    public static final String HEADING_ARROW = "gov.nasa.worldwind.render.markers.HeadingArrow";
    public static final String HEADING_LINE = "gov.nasa.worldwind.render.markers.HeadingLine";
    public static final String ORIENTED_SPHERE = "gov.nasa.worldwind.render.markers.DirectionalSphere";
    public static final String ORIENTED_CUBE = "gov.nasa.worldwind.render.markers.DirectionalCube";
    public static final String ORIENTED_CONE = "gov.nasa.worldwind.render.markers.DirectionalCone";
    public static final String ORIENTED_CYLINDER = "gov.nasa.worldwind.render.markers.DirectionalCylinder";
    public static final String ORIENTED_SPHERE_LINE = "gov.nasa.worldwind.render.markers.DirectionalSphereLine";
    public static final String ORIENTED_CONE_LINE = "gov.nasa.worldwind.render.markers.DirectionalConeLine";
    public static final String ORIENTED_CYLINDER_LINE = "gov.nasa.worldwind.render.markers.DirectionalCylinderLine";

    @SuppressWarnings({"StringEquality"})
    public static MarkerShape createShapeInstance(String shapeType)
    {
        if (shapeType == null)
        {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // String identity rather than equality is wanted here, to avoid a bunch of unnecessary string compares
        if (shapeType == BasicMarkerShape.SPHERE)
            return new Sphere();
        else if (shapeType == BasicMarkerShape.CUBE)
            return new Cube();
        else if (shapeType == BasicMarkerShape.CONE)
            return new Cone();
        else if (shapeType == BasicMarkerShape.CYLINDER)
            return new Cylinder();
        else if (shapeType == BasicMarkerShape.HEADING_ARROW)
            return new HeadingArrow();
        else if (shapeType == BasicMarkerShape.HEADING_LINE)
            return new HeadingLine();
        else if (shapeType == BasicMarkerShape.ORIENTED_SPHERE)
            return new CompoundShape(BasicMarkerShape.ORIENTED_SPHERE, "Oriented Sphere", new Sphere(),
                new HeadingArrow());
        else if (shapeType == BasicMarkerShape.ORIENTED_CUBE)
        {
            Cube cube = new Cube();
            cube.setApplyOrientation(false);  // Heading arrow shows orientation, do not rotate shape
            return new CompoundShape(BasicMarkerShape.ORIENTED_CUBE, "Oriented Cube", cube, new HeadingArrow(),
                .6);
        }
        else if (shapeType == BasicMarkerShape.ORIENTED_CONE)
        {
            Cone cone = new Cone();
            cone.setApplyOrientation(false); // Heading arrow shows orientation, do not rotate shape
            return new CompoundShape(BasicMarkerShape.ORIENTED_CONE, "Oriented Cone", cone, new HeadingArrow(), 0.6);
        }
        else if (shapeType == BasicMarkerShape.ORIENTED_CYLINDER)
        {
            Cylinder cylinder = new Cylinder();
            cylinder.setApplyOrientation(false);  // Heading arrow shows orientation, do not rotate shape
            return new CompoundShape(BasicMarkerShape.ORIENTED_CYLINDER, "Oriented Cylinder", cylinder,
                new HeadingArrow(), .6);
        }
        else if (shapeType == BasicMarkerShape.ORIENTED_SPHERE_LINE)
            return new CompoundShape(BasicMarkerShape.ORIENTED_SPHERE_LINE, "Oriented Sphere Line", new Sphere(),
                new HeadingLine(), 1);
        else if (shapeType == BasicMarkerShape.ORIENTED_CONE_LINE)
        {
            Cone cone = new Cone();
            cone.setApplyOrientation(false);  // Heading arrow shows orientation, do not rotate shape
            return new CompoundShape(BasicMarkerShape.ORIENTED_CONE_LINE, "Oriented Cone Line", cone,
                new HeadingLine(), 2);
        }
        else if (shapeType == BasicMarkerShape.ORIENTED_CYLINDER_LINE)
        {
            Cylinder cylinder = new Cylinder();
            cylinder.setApplyOrientation(false);  // Heading arrow shows orientation, do not rotate shape
            return new CompoundShape(BasicMarkerShape.ORIENTED_CYLINDER_LINE, "Oriented Cylinder Line", cylinder,
                new HeadingLine(), 2);
        }
        else
            return new Sphere();
    }

    protected static class CompoundShape implements MarkerShape, Disposable
    {
        protected String name;
        protected String shapeType;
        protected ArrayList<MarkerShape> shapes = new ArrayList<MarkerShape>(2);
        protected double offset = 0;

        public CompoundShape(String shapeType, String name, MarkerShape shape1, MarkerShape shape2)
        {
            this.name = name;
            this.shapeType = shapeType;
            this.shapes.add(shape1);
            this.shapes.add(shape2);
        }

        public CompoundShape(String shapeType, String name, MarkerShape shape1, MarkerShape shape2, double offset)
        {
            this.name = name;
            this.shapeType = shapeType;
            this.shapes.add(shape1);
            this.shapes.add(shape2);
            this.offset = offset;
        }

        public void dispose()
        {
            for (MarkerShape shape : this.shapes)
            {
                if (shape instanceof Disposable)
                    ((Disposable) shape).dispose();
            }
        }

        public String getName()
        {
            return name;
        }

        public String getShapeType()
        {
            return shapeType;
        }

        public void render(DrawContext dc, Marker marker, Vec4 point, double radius)
        {
            this.shapes.get(0).render(dc, marker, point, radius, false);
            if (this.offset != 0)
            {
                Position pos = dc.getGlobe().computePositionFromPoint(point);
                point = dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                    pos.getElevation() + radius * this.offset);
            }
            this.shapes.get(1).render(dc, marker, point, radius, false);
        }

        public void render(DrawContext dc, Marker marker, Vec4 point, double radius, boolean isRelative)
        {
            this.shapes.get(0).render(dc, marker, point, radius, isRelative);
            if (this.offset != 0)
            {
                Position pos = dc.getGlobe().computePositionFromPoint(point);
                point = dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(),
                    pos.getElevation() + radius * this.offset);
            }
            this.shapes.get(1).render(dc, marker, point, radius, isRelative);
        }
    }

    protected static abstract class Shape implements MarkerShape, Disposable
    {
        protected String name;
        protected String shapeType;
        protected GLUquadric quadric;
        protected boolean isInitialized = false;
        protected Object displayListCacheKey = new Object();
        /** Indicates that the shape must apply heading, pitch, and roll. */
        protected boolean applyOrientation = true;

        abstract protected void doRender(DrawContext dc, Marker marker, Vec4 point, double radius, int[] dlResource);

        abstract protected int drawShape(DrawContext dc, double radius);

        protected void initialize(DrawContext dc)
        {
            this.quadric = dc.getGLU().gluNewQuadric();
            dc.getGLU().gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
            dc.getGLU().gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
            dc.getGLU().gluQuadricOrientation(quadric, GLU.GLU_OUTSIDE);
            dc.getGLU().gluQuadricTexture(quadric, false);
        }

        public void dispose()
        {
            if (this.isInitialized)
            {
                GLU glu = new GLUgl2();
                glu.gluDeleteQuadric(this.quadric);
                this.isInitialized = false;
            }
        }

        public String getName()
        {
            return this.name;
        }

        public String getShapeType()
        {
            return this.shapeType;
        }

        /**
         * Indicates whether or not the shape applies heading, pitch, and roll when it draws itself. Even if this field
         * is {@code true}, the shape may not apply all of the rotations.
         *
         * @return {@code true} if orientation is applied to the rendered shape, {@code false} if not.
         */
        public boolean isApplyOrientation()
        {
            return this.applyOrientation;
        }

        /**
         * Specifies whether or not the shape applies heading, pitch, and roll when it renders.
         *
         * @param applyOrientation {@code true} if the shape must apply heading, pitch, and roll (if they are supported
         *                         by the shape), {@code false} if it the shape must not apply this orientation.
         */
        public void setApplyOrientation(boolean applyOrientation)
        {
            this.applyOrientation = applyOrientation;
        }

        public void render(DrawContext dc, Marker marker, Vec4 point, double radius)
        {
            render(dc, marker, point, radius, true);
        }

        public void render(DrawContext dc, Marker marker, Vec4 point, double radius, boolean isRelative)
        {
            if (!this.isInitialized)
                this.initialize(dc);

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (!isRelative)
            {
                dc.getView().pushReferenceCenter(dc, point);
            }
            else
            {
                gl.glPushMatrix();
                gl.glTranslated(point.x, point.y, point.z);
            }

            int[] dlResource = (int[]) dc.getGpuResourceCache().get(this.displayListCacheKey);
            if (dlResource == null)
                dlResource = this.createDisplayList(dc, radius);

            this.doRender(dc, marker, point, radius, dlResource);
            if (!isRelative)
            {
                dc.getView().popReferenceCenter(dc);
            }
            else
            {
                gl.glPopMatrix();
            }
        }

        /**
         * Compute a direction vector given a point, heading and pitch.
         *
         * @param dc      current draw context
         * @param point   point at which to compute direction vector
         * @param normal  surface normal at {@code point}
         * @param heading desired heading
         * @param pitch   desired pitch
         *
         * @return A vector pointing in the direction of the desired heading and pitch
         */
        protected Vec4 computeOrientationVector(DrawContext dc, Vec4 point, Vec4 normal, Angle heading,
            Angle pitch)
        {
            // To compute rotation of the shape toward the proper heading, find a second point in that direction.
            Globe globe = dc.getGlobe();
            Position pos = globe.computePositionFromPoint(point);
            LatLon p2ll = LatLon.greatCircleEndPosition(pos, heading, Angle.fromDegrees(0.1));
            Vec4 p2 = globe.computePointFromPosition(p2ll.getLatitude(), p2ll.getLongitude(),
                pos.getElevation());

            // Find vector in the direction of the heading
            Vec4 p1p2 = p2.subtract3(point).normalize3();

            // Take cross product of normal vector and heading vector to create an axis around which to apply pitch
            // rotation.
            Vec4 pitchAxis = normal.cross3(p1p2);

            return normal.transformBy3(Matrix.fromAxisAngle(pitch, pitchAxis));
        }

        protected int[] createDisplayList(DrawContext dc, double radius)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            int[] dlResource = new int[] {gl.glGenLists(1), 1};

            int size;
            try
            {
                gl.glNewList(dlResource[0], GL2.GL_COMPILE);
                size = this.drawShape(dc, radius);
                gl.glEndList();
            }
            catch (Exception e)
            {
                gl.glEndList();
                gl.glDeleteLists(dlResource[0], dlResource[1]);
                return null;
            }

            dc.getGpuResourceCache().put(this.displayListCacheKey, dlResource, GpuResourceCache.DISPLAY_LISTS, size);

            return dlResource;
        }
    }

    protected static class Sphere extends Shape
    {
        @Override
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);

            this.name = "Sphere";
            this.shapeType = BasicMarkerShape.SPHERE;

            this.isInitialized = true;
        }

        protected void doRender(DrawContext dc, Marker marker, Vec4 point, double radius, int[] dlResource)
        {
            // Sphere is symmetric about all axes, so no need to apply heading, pitch, or roll.
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glScaled(radius, radius, radius);
            gl.glCallList(dlResource[0]);
        }

        @Override
        protected int drawShape(DrawContext dc, double radius)
        {
            int slices = 24;
            int stacks = 12;

            dc.getGLU().gluSphere(this.quadric, 1, slices, stacks);

            return slices * stacks * 6 * 4; // num vertices * (vertex + normal float coords)
        }
    }

    /** Cube marker shape. The cube can be oriented using heading, pitch, and roll. */
    protected static class Cube extends Shape
    {
        @Override
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);

            this.name = "Cube";
            this.shapeType = BasicMarkerShape.CUBE;

            this.isInitialized = true;
        }

        @Override
        protected int drawShape(DrawContext dc, double radius)
        {
            // Vertices of a cube, 2 units on each side, with the center of the bottom face on the origin.
            float[][] v = {{-1f, 1f, 0f}, {-1f, 1f, 2f}, {1f, 1f, 2f}, {1f, 1f, 0f},
                {-1f, -1f, 2f}, {1f, -1f, 2f}, {1f, -1f, 0f}, {-1f, -1f, 0f}};

            // Array to group vertices into faces
            int[][] faces = {{0, 1, 2, 3}, {2, 5, 6, 3}, {1, 4, 5, 2}, {0, 7, 4, 1}, {0, 7, 6, 3}, {4, 7, 6, 5}};

            // Normal vectors for each face
            float[][] n = {{0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {0, -1, 0}};

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            gl.glBegin(GL2.GL_QUADS);

            for (int i = 0; i < faces.length; i++)
            {
                gl.glNormal3f(n[i][0], n[i][1], n[i][2]);

                for (int j = 0; j < faces[0].length; j++)
                {
                    gl.glVertex3f(v[faces[i][j]][0], v[faces[i][j]][1], v[faces[i][j]][2]);
                }
            }

            gl.glEnd();

            return (8 + 4) * 3 * 4; // assume 8 verts, 4 normals, all of them 3 float coords
        }

        protected void doRender(DrawContext dc, Marker marker, Vec4 point, double size, int[] dlResource)
        {
            Vec4 normal = dc.getGlobe().computeSurfaceNormalAtPoint(point);

            // This performs the same operation as Vec4.axisAngle() but with a "v2" of <0, 0, 1>.
            // Compute rotation angle
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (!(normal.equals(Vec4.UNIT_Z) || normal.equals(Vec4.UNIT_NEGATIVE_Z)))
            {
                Angle angle = Angle.fromRadians(Math.acos(normal.z));
                // Compute the direction cosine factors that define the rotation axis
                double A = -normal.y;
                double B = normal.x;
                double L = Math.sqrt(A * A + B * B);

                // Rotate the cube so that one of the faces points north
                Position position = dc.getGlobe().computePositionFromPoint(point);
                Vec4 north = dc.getGlobe().computeNorthPointingTangentAtLocation(position.getLatitude(),
                    position.getLongitude());
                Vec4 rotatedY = Vec4.UNIT_NEGATIVE_Y.transformBy3(Matrix.fromAxisAngle(angle, A / L, B / L, 0));
                Angle northAngle = rotatedY.angleBetween3(north);

                gl.glRotated(angle.degrees, A / L, B / L, 0);  // rotate cube normal to globe

                gl.glRotated(northAngle.degrees, 0, 0, 1); // rotate to face north
            }

            // Apply heading, pitch, and roll
            if (this.isApplyOrientation())
            {
                if (marker.getHeading() != null)
                    gl.glRotated(-marker.getHeading().degrees, 0, 0, 1);
                if (marker.getPitch() != null)
                    gl.glRotated(marker.getPitch().degrees, 1, 0, 0);
                if (marker.getRoll() != null)
                    gl.glRotated(marker.getRoll().degrees, 0, 0, 1);
            }

            gl.glScaled(size, size, size);
            gl.glCallList(dlResource[0]);
        }
    }

    /** A cone marker shape. The cone can be oriented using heading and pitch. */
    protected static class Cone extends Shape
    {
        @Override
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);

            this.name = "Cone";
            this.shapeType = BasicMarkerShape.CONE;
            this.isInitialized = true;
        }

        protected void doRender(DrawContext dc, Marker marker, Vec4 point, double size, int[] dlResource)
        {
            // By default, the shape is normal to the globe (0 heading, 0 pitch, 0 roll)
            Vec4 orientation = dc.getGlobe().computeSurfaceNormalAtPoint(point);

            // Heading only applies to cone if pitch is also specified. A heading without pitch spins the cone
            // around its axis. A heading with pitch spins the cone, and then tilts it in the direction of the
            // heading.
            if (this.isApplyOrientation() && marker.getPitch() != null)
            {
                orientation = this.computeOrientationVector(dc, point, orientation,
                    marker.getHeading() != null ? marker.getHeading() : Angle.ZERO,
                    marker.getPitch());
            }

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (!(orientation.equals(Vec4.UNIT_Z) || orientation.equals(Vec4.UNIT_NEGATIVE_Z)))
            {
                // This code performs the same operation as Vec4.axisAngle() but with a "v2" of <0, 0, 1>.
                // Compute rotation angle
                Angle angle = Angle.fromRadians(Math.acos(orientation.z));
                // Compute the direction cosine factors that define the rotation axis
                double A = -orientation.y;
                double B = orientation.x;
                double L = Math.sqrt(A * A + B * B);

                gl.glRotated(angle.degrees, A / L, B / L, 0);  // rotate shape to proper heading and pitch
            }
            else if (orientation.equals(Vec4.UNIT_NEGATIVE_Z))
            {
                gl.glRotated(180, 1, 0, 0); // rotate to point cone away from globe's surface
            }

            gl.glScaled(size, size, size);                 // scale
            gl.glCallList(dlResource[0]);
        }

        @Override
        protected int drawShape(DrawContext dc, double radius)
        {
            int slices = 20;
            int stacks = 20;
            int loops = 2;

            dc.getGLU().gluQuadricOrientation(this.quadric, GLU.GLU_OUTSIDE);
            dc.getGLU().gluCylinder(this.quadric, 1d, 0d, 2d, slices, (int) (2 * (Math.sqrt(stacks)) + 1));
            dc.getGLU().gluDisk(this.quadric, 0d, 1d, slices, loops);

            return (slices + stacks + slices * loops) * 6 * 4; // num vertices * (vertex + normal floats) * float size
        }
    }

    /** A cylinder marker shape. The cylinder can be oriented using heading and pitch. */
    protected static class Cylinder extends Shape
    {
        @Override
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);

            this.name = "Cylinder";
            this.shapeType = BasicMarkerShape.CYLINDER;
            this.isInitialized = true;
        }

        protected void doRender(DrawContext dc, Marker marker, Vec4 point, double size, int[] dlResource)
        {
            Vec4 orientation = dc.getGlobe().computeSurfaceNormalAtPoint(point);

            // Heading only applies to cylinder if pitch is also specified. A heading without pitch spins the cylinder
            // around its axis. A heading with pitch spins the cylinder, and then tilts it in the direction of the
            // heading.
            if (this.isApplyOrientation() && marker.getPitch() != null)
            {
                orientation = this.computeOrientationVector(dc, point, orientation,
                    marker.getHeading() != null ? marker.getHeading() : Angle.ZERO,
                    marker.getPitch());
            }

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

            if (!(orientation.equals(Vec4.UNIT_Z) || orientation.equals(Vec4.UNIT_NEGATIVE_Z)))
            {
                // This performs the same operation as Vec4.axisAngle() but with a "v2" of <0, 0, 1>.
                // Compute rotation angle
                Angle angle = Angle.fromRadians(Math.acos(orientation.z));
                // Compute the direction cosine factors that define the rotation axis
                double A = -orientation.y;
                double B = orientation.x;
                double L = Math.sqrt(A * A + B * B);

                gl.glRotated(angle.degrees, A / L, B / L, 0);  // rotate to proper heading and pitch
            }

            gl.glScaled(size, size, size);                 // scale
            gl.glCallList(dlResource[0]);
        }

        @Override
        protected int drawShape(DrawContext dc, double radius)
        {
            int slices = 20;
            int stacks = 1;
            int loops = 1;

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            GLU glu = dc.getGLU();

            glu.gluCylinder(quadric, 1d, 1d, 2d, slices, (int) (2 * (Math.sqrt(stacks)) + 1));
            glu.gluDisk(quadric, 0d, 1d, slices, loops);
            gl.glTranslated(0, 0, 2);
            glu.gluDisk(quadric, 0d, 1d, slices, loops);
            gl.glTranslated(0, 0, -2);

            return slices * 2 * 6 * 4; // top and bottom vertices and normals, assume float coords (4 bytes)
        }
    }

    /** A line that indicates heading. This shape indicates heading; it ignores pitch and roll. */
    protected static class HeadingLine extends Shape
    {
        @Override
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);

            this.name = "Heading Line";
            this.shapeType = BasicMarkerShape.HEADING_LINE;
            this.isInitialized = true;
        }

        protected void doRender(DrawContext dc, Marker marker, Vec4 point, double size, int[] dlResource)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            MarkerAttributes attrs = marker.getAttributes();

            if (marker.getHeading() == null)
                return;

            // Apply heading material if different from marker's
            if (!dc.isPickingMode() && attrs.getHeadingMaterial() != null
                && !attrs.getHeadingMaterial().equals(attrs.getMaterial()))
            {
                if (attrs.getOpacity() < 1)
                    attrs.getHeadingMaterial().apply(gl, GL2.GL_FRONT, (float) attrs.getOpacity());
                else
                    attrs.getHeadingMaterial().apply(gl, GL2.GL_FRONT);
            }

            // Orient the unit shape to lie parallel to the globe's surface at its position and oriented to the
            // specified heading.
            if (dc.is2DGlobe())
            {
                Vec4 npt = dc.getGlobe().computeNorthPointingTangentAtLocation(marker.getPosition().getLatitude(),
                    marker.getPosition().getLongitude());
                //noinspection SuspiciousNameCombination
                double npta = Math.atan2(npt.x, npt.y);
                gl.glRotated(-marker.getHeading().degrees - npta * 180 / Math.PI, 0, 0, 1);
            }
            else
            {
                gl.glRotated(marker.getPosition().getLongitude().degrees, 0, 1, 0);
                gl.glRotated(-marker.getPosition().getLatitude().degrees, 1, 0, 0);
                gl.glRotated(-marker.getHeading().degrees, 0, 0, 1);
            }

            double scale = attrs.getHeadingScale() * size;
            gl.glScaled(scale, scale, scale);                       // scale
            gl.glCallList(dlResource[0]);

            // Restore the marker material if the heading material was applied
            if (!dc.isPickingMode() && attrs.getHeadingMaterial() != null
                && !attrs.getHeadingMaterial().equals(attrs.getMaterial()))
                attrs.apply(dc);
        }

        @Override
        protected int drawShape(DrawContext dc, double radius)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glBegin(GL2.GL_LINE_STRIP);
            gl.glNormal3f(0f, 0f, 1f);
            gl.glVertex3f(0, 0, 0);
            gl.glVertex3f(0, 1, 0);
            gl.glEnd();

            return 3 * 3 * 4; // three vertices and a normal each with 3 float coordinates
        }
    }

    /** An arrow that indicates heading. This shape indicates heading; it ignores pitch and roll. */
    protected static class HeadingArrow extends Shape
    {
        @Override
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);

            this.name = "Heading Arrow";
            this.shapeType = BasicMarkerShape.HEADING_ARROW;

            this.isInitialized = true;
        }

        protected void doRender(DrawContext dc, Marker marker, Vec4 point, double size, int[] dlResource)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            MarkerAttributes attrs = marker.getAttributes();

            if (marker.getHeading() == null)
                return;

            // Apply heading material if different from marker's
            if (!dc.isPickingMode() && attrs.getHeadingMaterial() != null
                && !attrs.getHeadingMaterial().equals(attrs.getMaterial()))
            {
                if (attrs.getOpacity() < 1)
                    attrs.getHeadingMaterial().apply(gl, GL2.GL_FRONT, (float) attrs.getOpacity());
                else
                    attrs.getHeadingMaterial().apply(gl, GL2.GL_FRONT);
            }

            // Orient the unit shape to lie parallel to the globe's surface at its position and oriented to the
            // specified heading.
            if (dc.is2DGlobe())
            {
                Vec4 npt = dc.getGlobe().computeNorthPointingTangentAtLocation(marker.getPosition().getLatitude(),
                    marker.getPosition().getLongitude());
                //noinspection SuspiciousNameCombination
                double npta = Math.atan2(npt.x, npt.y);
                gl.glRotated(-marker.getHeading().degrees - npta * 180 / Math.PI, 0, 0, 1);
            }
            else
            {
                gl.glRotated(marker.getPosition().getLongitude().degrees, 0, 1, 0);
                gl.glRotated(-marker.getPosition().getLatitude().degrees, 1, 0, 0);
                gl.glRotated(-marker.getHeading().degrees, 0, 0, 1);
            }

            double scale = attrs.getHeadingScale() * size;
            gl.glScaled(scale, scale, scale);                       // scale
            gl.glCallList(dlResource[0]);

            // Restore the marker material if the heading material was applied
            if (!dc.isPickingMode() && attrs.getHeadingMaterial() != null
                && !attrs.getHeadingMaterial().equals(attrs.getMaterial()))
                attrs.apply(dc);
        }

        @Override
        protected int drawShape(DrawContext dc, double radius)
        {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glBegin(GL2.GL_POLYGON);
            gl.glNormal3f(0f, 0f, 1f);
            gl.glVertex3f(-.5f, 0, 0);
            gl.glVertex3f(0, 1, 0);
            gl.glVertex3f(0.5f, 0, 0);
            gl.glVertex3f(-0.5f, 0, 0);
            gl.glEnd();

            return 5 * 3 * 4; // 5 vertices and a normal each with 3 float coordinates
        }
    }
}
