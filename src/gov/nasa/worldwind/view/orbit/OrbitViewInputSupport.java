/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.view.ViewUtil;

/**
 * @author dcollins
 * @version $Id: OrbitViewInputSupport.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OrbitViewInputSupport
{
    public static class OrbitViewState // public to allow access from subclasses
    {
        private final Position center;
        private final Angle heading;
        private final Angle pitch;
        private final double zoom;

        public OrbitViewState(Position center, Angle heading, Angle pitch, double zoom)
        {
            if (center == null)
            {
                    throw new IllegalArgumentException();
            }
            if (heading == null)
            {
                    throw new IllegalArgumentException();
            }
            if (pitch == null)
            {
                    throw new IllegalArgumentException();
            }

            this.center = center;
            this.heading = heading;
            this.pitch = pitch;
            this.zoom = zoom;
        }

        public Position getCenterPosition()
        {
            return this.center;
        }

        public Angle getHeading()
        {
            return this.heading;
        }

        public Angle getPitch()
        {
            return this.pitch;
        }

        public double getZoom()
        {
            return this.zoom;
        }
    }

    public OrbitViewInputSupport()
    {
    }

    public static Matrix computeTransformMatrix(Globe globe, Position center, Angle heading, Angle pitch, Angle roll,
        double zoom)
    {
        if (globe == null)
        {
            throw new IllegalArgumentException();
        }
        if (center == null)
        {
            throw new IllegalArgumentException();
        }
        if (heading == null)
        {
            throw new IllegalArgumentException();
        }
        if (pitch == null)
        {
            throw new IllegalArgumentException();
        }

        // Construct the model-view transform matrix for the specified coordinates.
        // Because this is a model-view transform, matrices are applied in reverse order.
        Matrix transform;
        // Zoom, heading, pitch.
        transform = OrbitViewInputSupport.computeHeadingPitchRollZoomTransform(heading, pitch, roll, zoom);
        // Center position.
        transform = transform.multiply(OrbitViewInputSupport.computeCenterTransform(globe, center));

        return transform;
    }

    public static OrbitViewState computeOrbitViewState(Globe globe, Vec4 eyePoint, Vec4 centerPoint, Vec4 up)
    {
        if (globe == null)
        {
            throw new IllegalArgumentException();
        }
        if (eyePoint == null)
        {
            String message = "nullValue.EyePointIsNull";
            throw new IllegalArgumentException();
        }
        if (centerPoint == null)
        {
            String message = "nullValue.CenterPointIsNull";
            throw new IllegalArgumentException();
        }
        if (up == null)
        {
            String message = "nullValue.UpIsNull";
            throw new IllegalArgumentException();
        }

        Matrix modelview = Matrix.fromViewLookAt(eyePoint, centerPoint, up);
        return OrbitViewInputSupport.computeOrbitViewState(globe, modelview, centerPoint);
    }

    public static OrbitViewState computeOrbitViewState(Globe globe, Matrix modelTransform, Vec4 centerPoint)
    {
        if (globe == null)
        {
            throw new IllegalArgumentException();
        }
        if (modelTransform == null)
        {
            String message = "nullValue.ModelTransformIsNull";
            throw new IllegalArgumentException();
        }
        if (centerPoint == null)
        {
            String message = "nullValue.CenterPointIsNull";
            throw new IllegalArgumentException();
        }

        // Compute the center position.
        Position centerPos = globe.computePositionFromPoint(centerPoint);
        // Compute the center position transform.
        Matrix centerTransform = OrbitViewInputSupport.computeCenterTransform(globe, centerPos);
        Matrix centerTransformInv = centerTransform.getInverse();
        if (centerTransformInv == null)
        {
            throw new IllegalStateException();
        }

        // Compute the heading-pitch-zoom transform.
        Matrix hpzTransform = modelTransform.multiply(centerTransformInv);
        // Extract the heading, pitch, and zoom values from the transform.
        Angle heading = ViewUtil.computeHeading(hpzTransform);
        Angle pitch = ViewUtil.computePitch(hpzTransform);
        double zoom = OrbitViewInputSupport.computeZoom(hpzTransform);
        if (heading == null || pitch == null)
            return null;

        return new OrbitViewState(centerPos, heading, pitch, zoom);
    }

    protected static Matrix computeCenterTransform(Globe globe, Position center)
    {
        if (globe == null)
        {
            throw new IllegalArgumentException();
        }
        if (center == null)
        {
            throw new IllegalArgumentException();
        }

        // The view eye position will be the same as the center position.
        // This is only the case without any zoom, heading, and pitch.
        Vec4 eyePoint = globe.computePointFromPosition(center);
        // The view forward direction will be colinear with the
        // geoid surface normal at the center position.
        Vec4 normal = globe.computeSurfaceNormalAtLocation(center.getLatitude(), center.getLongitude());
        Vec4 lookAtPoint = eyePoint.subtract3(normal);
        // The up direction will be pointing towards the north pole.
        Vec4 north = globe.computeNorthPointingTangentAtLocation(center.getLatitude(), center.getLongitude());
        // Creates a viewing matrix looking from eyePoint towards lookAtPoint,
        // with the given up direction. The forward, right, and up vectors
        // contained in the matrix are guaranteed to be orthogonal. This means
        // that the Matrix's up may not be equivalent to the specified up vector
        // here (though it will point in the same general direction).
        // In this case, the forward direction would not be affected.
        return Matrix.fromViewLookAt(eyePoint, lookAtPoint, north);
    }

    protected static Matrix computeHeadingPitchRollZoomTransform(Angle heading, Angle pitch, Angle roll, double zoom)
    {
        if (heading == null)
        {
            throw new IllegalArgumentException();
        }
        if (pitch == null)
        {
            throw new IllegalArgumentException();
        }
        if (roll == null)
        {
            throw new IllegalArgumentException();
        }

        Matrix transform;
        // Zoom.
        transform = Matrix.fromTranslation(0, 0, -zoom);
        // Roll is rotation around the Z axis
        transform = transform.multiply(Matrix.fromRotationZ(roll));
        // Pitch is treated clockwise as rotation about the X-axis. We flip the pitch value so that a positive
        // rotation produces a clockwise rotation (when facing the axis).
        transform = transform.multiply(Matrix.fromRotationX(pitch.multiply(-1.0)));
        // Heading.
        transform = transform.multiply(Matrix.fromRotationZ(heading));
        return transform;
    }


    protected static double computeZoom(Matrix headingPitchZoomTransform)
    {
        if (headingPitchZoomTransform == null)
        {
            String message = "nullValue.HeadingPitchZoomTransformTransformIsNull";
            throw new IllegalArgumentException();
        }

        Vec4 v = headingPitchZoomTransform.getTranslation();
        return v != null ? v.getLength3() : 0.0;
    }

    public static OrbitViewState getSurfaceIntersection(Globe globe, SectorGeometryList terrain, Position centerPosition,
        Angle heading, Angle pitch, double zoom)
    {
        if (globe != null)
        {
            Matrix modelview = OrbitViewInputSupport.computeTransformMatrix(globe, centerPosition,
                    heading, pitch, Angle.ZERO, zoom);
            if (modelview != null)
            {
                Matrix modelviewInv = modelview.getInverse();
                if (modelviewInv != null)
                {
                    Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
                    Vec4 centerPoint = globe.computePointFromPosition(centerPosition);
                    Vec4 eyeToCenter = eyePoint.subtract3(centerPoint);
                    Intersection[] intersections = terrain.intersect(new Line(eyePoint, eyeToCenter.normalize3().multiply3(-1)));
                    if (intersections != null && intersections.length >= 0)
                    {
                        Position newCenter = globe.computePositionFromPoint(intersections[0].getIntersectionPoint());
                        return(new OrbitViewState(newCenter, heading, pitch, zoom));
                    }
                }
            }
        }
        return null;
    }
}
