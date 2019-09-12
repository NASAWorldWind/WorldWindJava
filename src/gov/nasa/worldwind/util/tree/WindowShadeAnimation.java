/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
