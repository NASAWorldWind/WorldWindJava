/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

/**
 * @author jym
 * @version $Id: Animator.java 1171 2013-02-11 21:45:02Z dcollins $
 */

/**
 * The <code>Animator</code> interface provides a way to iterate through a series of values.  It can be used with
 * a simple interpolation function, or something more elaborate.  The <code>PropertyAccessor</code> class and its
 * interfaces can be used to agnostically attach to data members of any class.
*/
public interface Animator
{
    /**
     * Iterates to the next value.  The implementation is expected to apply that next value to the property
     * it is attached to.
     */
    void next();

    /**
     * Starts the <code>Animator</code>.  The implemenation should return <code>true</code> from <code>hasNext</code>
     */
    void start();

    /**
     * Stops the <code>Animator</code>.  The implmenentation should return <code>false</code> from <code>hasNext</code>
     */
    void stop();

    /**
     * Returns <code>true</code> if the <code>Animator</code> has more elements.
     *
     * @return <code>true</code> if the <code>Animator</code> has more elements.
     */
    boolean hasNext();

    /**
     * Set the value of the attached property to the value associated with this interpolant value.
     * @param interpolant A value between 0 and 1.
     */
    void set(double interpolant);
}
