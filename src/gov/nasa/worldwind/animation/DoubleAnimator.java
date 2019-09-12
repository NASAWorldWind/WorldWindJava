/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import gov.nasa.worldwind.util.*;

/**
 * An {@link Animator} implentation for animating values of type Double.
 *
 * @author jym
 * @version $Id: DoubleAnimator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DoubleAnimator extends BasicAnimator
{
    protected double begin;
    protected double end;
    protected final PropertyAccessor.DoubleAccessor propertyAccessor;

    public DoubleAnimator(Interpolator interpolator,
       double begin, double end,
       PropertyAccessor.DoubleAccessor propertyAccessor)
    {
       super(interpolator);
       if (interpolator == null)
       {
           this.interpolator = new ScheduledInterpolator(10000);
       }
       
       if (propertyAccessor == null)
       {
           String message = Logging.getMessage("nullValue.ViewPropertyAccessorIsNull");
           Logging.logger().severe(message);
           throw new IllegalArgumentException(message);
       }

       this.begin = begin;
       this.end = end;
       this.propertyAccessor = propertyAccessor;
    }

    public void setBegin(Double begin)
    {
        this.begin = begin;
    }

    public void setEnd(Double end)
    {
        this.end = end;
    }

    public final Double getBegin()
    {
       return this.begin;
    }

    public final Double getEnd()
    {
       return this.end;
    }

    public final PropertyAccessor.DoubleAccessor getPropertyAccessor()
    {
       return this.propertyAccessor;
    }

    protected void setImpl(double interpolant)
    {
       Double newValue = this.nextDouble(interpolant);
       if (newValue == null)
           return;

       boolean success = this.propertyAccessor.setDouble(newValue);
       if (!success)
       {
           this.flagLastStateInvalid();
       }
       if (interpolant >= 1.0)
           this.stop();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Double nextDouble(double interpolant)
    {
       return AnimationSupport.mixDouble(
           interpolant,
           this.begin,
           this.end);
    }

    
}
