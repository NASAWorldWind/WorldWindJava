/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: OrbitViewCollisionSupport.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OrbitViewCollisionSupport
{
    private double collisionThreshold;
    private int numIterations;

    public OrbitViewCollisionSupport()
    {
        setNumIterations(1);
    }

    public double getCollisionThreshold()
    {
        return this.collisionThreshold;
    }

    public void setCollisionThreshold(double collisionThreshold)
    {
        if (collisionThreshold < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", collisionThreshold);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.collisionThreshold = collisionThreshold;
    }

    public int getNumIterations()
    {
        return this.numIterations;
    }

    public void setNumIterations(int numIterations)
    {
        if (numIterations < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", numIterations);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numIterations = numIterations;
    }

    public boolean isColliding(OrbitView orbitView, double nearDistance, DrawContext dc)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (nearDistance < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", nearDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        Globe globe = dc.getGlobe();
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix modelviewInv = getModelviewInverse(globe,
            orbitView.getCenterPosition(), orbitView.getHeading(), orbitView.getPitch(), orbitView.getRoll(),
            orbitView.getZoom());
        if (modelviewInv != null)
        {
            // OrbitView is colliding when its eye point is below the collision threshold.
            double heightAboveSurface = computeViewHeightAboveSurface(dc, modelviewInv,
                orbitView.getFieldOfView(), orbitView.getViewport(), nearDistance);
            return heightAboveSurface < this.collisionThreshold;
        }

        return false;
    }

    public Position computeCenterPositionToResolveCollision(BasicOrbitView orbitView, double nearDistance,
        DrawContext dc)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (nearDistance < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", nearDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        Globe globe = dc.getGlobe();
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position newCenter = null;

        for (int i = 0; i < this.numIterations; i++)
        {
            Matrix modelviewInv = getModelviewInverse(globe,
                newCenter != null ? newCenter : orbitView.getCenterPosition(),
                orbitView.getHeading(), orbitView.getPitch(), orbitView.getRoll(), orbitView.getZoom());
            if (modelviewInv != null)
            {
                double heightAboveSurface = computeViewHeightAboveSurface(dc, modelviewInv,
                    orbitView.getFieldOfView(), orbitView.getViewport(), nearDistance);
                double adjustedHeight = heightAboveSurface - this.collisionThreshold;
                if (adjustedHeight < 0)
                {
                    newCenter = new Position(
                        newCenter != null ? newCenter : orbitView.getCenterPosition(),
                        (newCenter != null ? newCenter.getElevation() : orbitView.getCenterPosition().getElevation())
                            - adjustedHeight);
                }
            }
        }

        return newCenter;
    }

    public Angle computePitchToResolveCollision(BasicOrbitView orbitView, double nearDistance, DrawContext dc)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (nearDistance < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", nearDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        Globe globe = dc.getGlobe();
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle newPitch = null;

        for (int i = 0; i < this.numIterations; i++)
        {
            Matrix modelviewInv = getModelviewInverse(globe,
                orbitView.getCenterPosition(), orbitView.getHeading(),
                newPitch != null ? newPitch : orbitView.getPitch(), orbitView.getRoll(),
                orbitView.getZoom());
            if (modelviewInv != null)
            {
                double heightAboveSurface = computeViewHeightAboveSurface(dc, modelviewInv,
                    orbitView.getFieldOfView(), orbitView.getViewport(), nearDistance);
                double adjustedHeight = heightAboveSurface - this.collisionThreshold;
                if (adjustedHeight < 0)
                {
                    Vec4 eyePoint = getEyePoint(modelviewInv);
                    Vec4 centerPoint = globe.computePointFromPosition(orbitView.getCenterPosition());
                    if (eyePoint != null && centerPoint != null)
                    {
                        Position eyePos = globe.computePositionFromPoint(eyePoint);
                        // Compute the eye point required to resolve the collision.
                        Vec4 newEyePoint = globe.computePointFromPosition(eyePos.getLatitude(), eyePos.getLongitude(),
                            eyePos.getElevation() - adjustedHeight);
                        // Compute the pitch that corresponds with the elevation of the eye point
                        // (but not necessarily the latitude and longitude).
                        Vec4 normalAtCenter = globe.computeSurfaceNormalAtPoint(centerPoint);
                        Vec4 newEye_sub_center = newEyePoint.subtract3(centerPoint).normalize3();
                        double dot = normalAtCenter.dot3(newEye_sub_center);
                        if (dot >= -1 || dot <= 1)
                        {
                            double angle = Math.acos(dot);
                            newPitch = Angle.fromRadians(angle);
                        }
                    }
                }
            }
        }

        return newPitch;
    }

    private double computeViewHeightAboveSurface(DrawContext dc, Matrix modelviewInv,
        Angle fieldOfView, java.awt.Rectangle viewport, double nearDistance)
    {
        double height = Double.POSITIVE_INFINITY;
        if (dc != null && modelviewInv != null && fieldOfView != null && viewport != null && nearDistance >= 0)
        {
            Vec4 eyePoint = getEyePoint(modelviewInv);
            if (eyePoint != null)
            {
                double eyeHeight = computePointHeightAboveSurface(dc, eyePoint);
                if (eyeHeight < height)
                    height = eyeHeight;
            }

            Vec4 nearPoint = getPointOnNearPlane(modelviewInv, fieldOfView, viewport, nearDistance);
            if (nearPoint != null)
            {
                double nearHeight = computePointHeightAboveSurface(dc, nearPoint);
                if (nearHeight < height)
                    height = nearHeight;
            }
        }
        return height;
    }

    private double computePointHeightAboveSurface(DrawContext dc, Vec4 point)
    {
        double height = Double.POSITIVE_INFINITY;
        if (dc != null && dc.getGlobe() != null && point != null)
        {
            Globe globe = dc.getGlobe();
            Position position = globe.computePositionFromPoint(point);
            Position surfacePosition = null;
            // Look for the surface geometry point at 'position'.
            Vec4 pointOnGlobe = dc.getPointOnTerrain(position.getLatitude(), position.getLongitude());
            if (pointOnGlobe != null)
                surfacePosition = globe.computePositionFromPoint(pointOnGlobe);
            // Fallback to using globe elevation values.
            if (surfacePosition == null)
                surfacePosition = new Position(position,
                    globe.getElevation(position.getLatitude(), position.getLongitude()) * dc.getVerticalExaggeration());
            height = position.getElevation() - surfacePosition.getElevation();
        }
        return height;
    }

    private Matrix getModelviewInverse(Globe globe,
        Position centerPosition, Angle heading, Angle pitch, Angle roll, double zoom)
    {
        if (globe != null && centerPosition != null && heading != null && pitch != null)
        {
            Matrix modelview = OrbitViewInputSupport.computeTransformMatrix(globe,
                centerPosition, heading, pitch, roll, zoom);
            if (modelview != null)
                return modelview.getInverse();
        }

        return null;
    }

    private Vec4 getEyePoint(Matrix modelviewInv)
    {
        return modelviewInv != null ? Vec4.UNIT_W.transformBy4(modelviewInv) : null;
    }

    private Vec4 getPointOnNearPlane(Matrix modelviewInv, Angle fieldOfView, java.awt.Rectangle viewport,
        double nearDistance)
    {
        if (modelviewInv != null && fieldOfView != null && viewport != null && nearDistance >= 0)
        {
            // If either either the viewport width or height is zero, then fall back to an aspect ratio of 1. 
            // Otherwise, compute the standard aspect ratio.
            double aspect = (viewport.getWidth() <= 0 || viewport.getHeight() <= 0) ?
                1d : (viewport.getHeight() / viewport.getWidth());
            double nearClipHeight = 2 * aspect * nearDistance * fieldOfView.tanHalfAngle();
            // Computes the point on the bottom center of the near clip plane.
            Vec4 nearClipVec = new Vec4(0, -nearClipHeight / 2.0, -nearDistance, 1);
            return nearClipVec.transformBy4(modelviewInv);
        }

        return null;
    }
}
