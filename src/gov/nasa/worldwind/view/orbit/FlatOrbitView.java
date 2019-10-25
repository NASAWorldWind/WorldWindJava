/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.*;

/**
 * @author Patrick Muris
 * @version $Id: FlatOrbitView.java 2219 2014-08-11 21:39:44Z dcollins $
 * @deprecated Use {@link gov.nasa.worldwind.view.orbit.BasicOrbitView} instead. BasicOrbitView implements the correct
 *             horizon distance and far clip distance when used with a 2D globe.
 */
@Deprecated
public class FlatOrbitView extends BasicOrbitView
{
    // TODO: make configurable
    private static final double MINIMUM_FAR_DISTANCE = 100;

    public FlatOrbitView()
    {
    }

    protected double computeHorizonDistance()
    {
        // Use the eye point from the last call to apply() to compute horizon distance.
        Vec4 eyePoint = this.getEyePoint();
        return this.computeHorizonDistance(eyePoint);
    }

    public double computeFarClipDistance()
    {
        double far = this.computeHorizonDistance(this.getCurrentEyePoint());
        return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
    }

    protected double computeHorizonDistance(Vec4 eyePoint)
    {
        double horizon = 0;
        // Compute largest distance to flat globe 'corners'.
        if (this.globe != null && eyePoint != null)
        {
            double dist = 0;
            Vec4 p;
            // Use max distance to six points around the map
            p = this.globe.computePointFromPosition(Angle.POS90, Angle.NEG180, 0); // NW
            dist = Math.max(dist, eyePoint.distanceTo3(p));
            p = this.globe.computePointFromPosition(Angle.POS90, Angle.POS180, 0); // NE
            dist = Math.max(dist, eyePoint.distanceTo3(p));
            p = this.globe.computePointFromPosition(Angle.NEG90, Angle.NEG180, 0); // SW
            dist = Math.max(dist, eyePoint.distanceTo3(p));
            p = this.globe.computePointFromPosition(Angle.NEG90, Angle.POS180, 0); // SE
            dist = Math.max(dist, eyePoint.distanceTo3(p));
            p = this.globe.computePointFromPosition(Angle.ZERO, Angle.POS180, 0); // E
            dist = Math.max(dist, eyePoint.distanceTo3(p));
            p = this.globe.computePointFromPosition(Angle.ZERO, Angle.NEG180, 0); // W
            dist = Math.max(dist, eyePoint.distanceTo3(p));
            horizon = dist;
        }
        return horizon;
    }
}
