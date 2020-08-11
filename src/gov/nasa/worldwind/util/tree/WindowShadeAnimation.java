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

package gov.nasa.worldwind.util.tree;

import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Animation to minimize a frame with a window shade effect.
 *
 * @author pabercrombie
 * @version $Id: WindowShadeAnimation.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WindowShadeAnimation implements Animation
{
    /** Default animation duration, in milliseconds. */
    public int DEFAULT_DURATION = 400;

    protected ScrollFrame frame;
    protected int startWindowHeight;
    protected int targetWindowHeight;
    /** Duration, in milliseconds, of the animation. */
    protected int duration = DEFAULT_DURATION;
    /** Time when the animation started. */
    protected long animationStart;

    protected Size targetWindowSize;
    protected int maximizedWindowHeight;

    public WindowShadeAnimation(ScrollFrame frame)
    {
        this.frame = frame;
    }

    /** {@inheritDoc} */
    public void reset()
    {
        this.animationStart = System.currentTimeMillis();
        Dimension currentSize = this.frame.getCurrentSize();

        // The minimized flag is set before the animation starts. So if the layout says that it is minimized, we want to
        // animate toward a minimized size.
        if (this.frame.isMinimized())
        {
            this.startWindowHeight = currentSize.height;
            this.maximizedWindowHeight = currentSize.height;
            this.targetWindowHeight = this.frame.getTitleBarHeight() + this.frame.frameBorder * 2;
        }
        else
        {
            this.startWindowHeight = currentSize.height;
            this.targetWindowHeight = this.maximizedWindowHeight;
        }
    }

    /** {@inheritDoc} */
    public void step()
    {
        long now = System.currentTimeMillis();
        double a = WWMath.computeInterpolationFactor(now, this.animationStart,
            this.animationStart + this.duration);

        //noinspection SuspiciousNameCombination
        int newHeight = (int) WWMath.mix(a, startWindowHeight, targetWindowHeight);

        Dimension size = this.frame.getCurrentSize();

        this.frame.setMinimizedSize(Size.fromPixels(size.width, newHeight));
    }

    /** {@inheritDoc} */
    public boolean hasNext()
    {
        return this.frame.getCurrentSize().height != targetWindowHeight;
    }

    /**
     * Indicates the duration of the animation.
     *
     * @return The duration of the animation, in milliseconds.
     */
    public int getDuration()
    {
        return duration;
    }

    /**
     * Specifies the duration of the animation.
     *
     * @param duration The duration of the animation, in milliseconds.
     */
    public void setDuration(int duration)
    {
        if (duration < 0)
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.duration = duration;
    }
}
