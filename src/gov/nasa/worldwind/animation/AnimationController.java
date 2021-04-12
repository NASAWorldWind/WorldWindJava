/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
