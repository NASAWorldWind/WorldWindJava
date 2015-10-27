/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.animation;

import java.util.*;

/**
 * The <code>AnimationController</code> class is a convenience class for managing a
 * group of <code>Animators</code>.
 *
 * @author jym
 * @version $Id: AnimationController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AnimationController extends
            HashMap<Object, Animator>
{

    /**
     * Starts all of the <code>Animator</code>s in the map
     */
    public void startAnimations()
    {
        Collection<Animator> animators = this.values();
        for (Animator a : animators)
        {
            a.start();
        }
    }

    /**
     * Stops all of the <code>Animator</code>s in the map
     */
    public void stopAnimations()
    {
        Collection<Animator> animators = this.values();
        for (Animator a : animators)
        {
            a.stop();
        }

    }

    /**
     * Starts the animation associated with <code>animationName</code>
     *
     * @param animationName the name of the animation to be started.
     */
    public void startAnimation(Object animationName)
    {
        this.get(animationName).start();
    }

    /**
     * Stops the <code>Animator</code> associated with <code>animationName</code>
     * @param animationName the name of the animation to be stopped
     */
    public void stopAnimation(Object animationName)
    {
        this.get(animationName).stop();
    }

    /**
     * Stops all <code>Animator</code>s in the map.
     * @return true if any <code>Animator</code> was started, false otherwise
     */
    public boolean stepAnimators()
    {
        boolean didStep = false;
        Collection<Animator> animators = this.values();
        for (Animator a : animators)
        {
            if (a.hasNext())
            {
                didStep = true;
                a.next();
            }
        }
        return didStep;

    }

    /**
     * Returns <code>true</code> if the controller has any active <code>Animations</code>
     * 
     * @return true if there are any active animations in this <code>CompountAnimation</code>
     */
    public boolean hasActiveAnimation()
    {

        Collection<Animator> animators = this.values();
        for (Animator a : animators)
        {
            if (a.hasNext())
            {
                return true;
            }
        }
        return false;
    }


}
