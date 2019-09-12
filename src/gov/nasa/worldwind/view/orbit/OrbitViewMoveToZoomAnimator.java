/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.animation.MoveToDoubleAnimator;
import gov.nasa.worldwind.util.PropertyAccessor;

/**
 * @author jym
 * @version $Id: OrbitViewMoveToZoomAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OrbitViewMoveToZoomAnimator  extends MoveToDoubleAnimator
{
    BasicOrbitView orbitView;
    boolean endCenterOnSurface;

    OrbitViewMoveToZoomAnimator(BasicOrbitView orbitView, Double end, double smoothing,
        PropertyAccessor.DoubleAccessor propertyAccessor, boolean endCenterOnSurface)
    {
        super(end, smoothing, propertyAccessor);
        this.orbitView = orbitView;
        this.endCenterOnSurface = endCenterOnSurface;
    }

    protected void setImpl(double interpolant)
    {
       Double newValue = this.nextDouble(interpolant);
       if (newValue == null)
           return;

       this.propertyAccessor.setDouble(newValue);
    }

    public Double nextDouble(double interpolant)
    {
        double newValue = (1 - interpolant) * propertyAccessor.getDouble() + interpolant * this.end;
        if (Math.abs(newValue - propertyAccessor.getDouble()) < minEpsilon)
        {
            this.stop();
            if (this.endCenterOnSurface)
                orbitView.setViewOutOfFocus(true);
            return(null);
        }
        return newValue;
    }
}
