/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import com.jogamp.opengl.util.*;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;

import com.jogamp.opengl.GLAnimatorControl;

/**
 * Shows how to use a JOGL Animator to animate in WorldWind
 *
 * @author tag
 * @version $Id: AnimatedGlobe.java 1893 2014-04-04 04:31:59Z tgaskins $
 */
public class AnimatedGlobe extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame implements RenderingListener
    {
        protected GLAnimatorControl animator;
        protected double rotationDegreesPerSecond = 40;
        protected long lastTime;
        protected Position eyePosition = Position.fromDegrees(0, 0, 20000000);

        public AppFrame()
        {
            // Reduce the frequency at which terrain is regenerated.
            getWwd().getModel().getGlobe().getTessellator().setUpdateFrequency(5000);

            // Add a rendering listener to update the eye position each frame. It's implementation is the
            // stageChanged method below.
            getWwd().addRenderingListener(this);

            // Use a JOGL Animator to spin the globe
            lastTime = System.currentTimeMillis();
            animator = new FPSAnimator((WorldWindowGLCanvas) getWwd(), 60 /*frames per second*/);
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
                double elapsedSeconds = (now - lastTime) * 1.0e-3;;
                double rotationDegrees = rotationDegreesPerSecond * elapsedSeconds;
                lastTime = now;

                double lat = eyePosition.getLatitude().degrees;
                double lon = Angle.normalizedDegreesLongitude(eyePosition.getLongitude().degrees + rotationDegrees);
                double alt = eyePosition.getAltitude();

                eyePosition = Position.fromDegrees(lat, lon, alt);
                getWwd().getView().stopAnimations();
                getWwd().getView().setEyePosition(eyePosition);
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Animated Globe", AppFrame.class);
    }
}
