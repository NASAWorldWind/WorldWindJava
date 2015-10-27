/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import com.jogamp.opengl.util.Animator;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;

/**
 * Shows how to use a JOGL Animator to animate in World Wind
 *
 * @author tag
 * @version $Id: AnimatedGlobe.java 1893 2014-04-04 04:31:59Z tgaskins $
 */
public class AnimatedGlobe extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame implements RenderingListener
    {
        Animator animator;
        double rotationRate = 100; // degrees per second
        long lastTime;
        Position eyePosition = Position.fromDegrees(0, 0, 20000000);

        public AppFrame()
        {

            // Reduce the frequency at which terrain is regenerated.
            getWwd().getModel().getGlobe().getTessellator().setUpdateFrequency(5000);

            // Add a rendering listener to update the eye position each frame. It's implementation is the
            // stageChanged method below.
            getWwd().addRenderingListener(this);

            // Use a JOGL Animator to spin the globe
            lastTime = System.currentTimeMillis();
            animator = new Animator();
            animator.add((WorldWindowGLCanvas) getWwd());
            animator.start();
        }

        @Override
        public void stageChanged(RenderingEvent event)
        {
            if (event.getStage().equals(RenderingEvent.BEFORE_RENDERING))
            {
                // The globe may not be instantiated the first time the listener is called.
                if (getWwd().getView().getGlobe() == null)
                    return;

                long now = System.currentTimeMillis();
                double d = rotationRate * (now - lastTime) * 1.0e-3;
                lastTime = now;

                double longitude = eyePosition.getLongitude().degrees;
                longitude += d;
                if (longitude > 180)
                    longitude = -180 + (180 - longitude);

                eyePosition = Position.fromDegrees(eyePosition.getLatitude().degrees, longitude,
                    eyePosition.getAltitude());
                Position groundPos = new Position(eyePosition.getLatitude(), eyePosition.getLongitude(), 0);
                getWwd().getView().setOrientation(eyePosition, groundPos);
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Animated Globe", AppFrame.class);
    }
}
