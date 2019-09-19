/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.view;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.poi.PointOfInterest;
import gov.nasa.worldwind.view.firstperson.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * This example demonstrates the use of {@link gov.nasa.worldwind.view.firstperson.BasicFlyView} with a {@link
 * gov.nasa.worldwind.view.firstperson.FlyToFlyViewAnimator}, allowing the user to fly smoothly to new locations by
 * simply specifying a location's name or lat-long coordinates.
 *
 * @author jym
 * @version $Id: AddAnimator.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class AddAnimator extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public BasicFlyView view;

        public class ViewDisplay extends JPanel
        {

            public class FlyViewGazetteerPanel extends GazetteerPanel
            {
                FlyViewGazetteerPanel(final WorldWindow wwd, String FlyViewGazetteerPanel)
                    throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException
                {
                    super(wwd, FlyViewGazetteerPanel);
                }

                public void moveToLocation(PointOfInterest location)
                {
                    if (location == null)
                    {
                        return;
                    }
                    double elevation = view.getGlobe().getElevation(
                        location.getLatlon().getLatitude(), location.getLatlon().getLongitude());
                    FlyToFlyViewAnimator animator =
                        FlyToFlyViewAnimator.createFlyToFlyViewAnimator(view,
                            view.getEyePosition(),
                            new Position(location.getLatlon(), elevation),
                            view.getHeading(), view.getHeading(),
                            view.getPitch(), view.getPitch(),
                            view.getEyePosition().getElevation(), view.getEyePosition().getElevation(),
                            10000, WorldWind.ABSOLUTE);
                    view.addAnimator(animator);
                    animator.start();
                    view.firePropertyChange(AVKey.VIEW, null, view);
                }
            }

            public ViewDisplay()
            {
                super(new GridLayout(0, 1));
                try
                {
                    this.add(new FlyViewGazetteerPanel(getWwd(),
                        "gov.nasa.worldwind.poi.YahooGazetteer"), SwingConstants.CENTER);
                }
                catch (Exception e)
                {
                    JOptionPane.showMessageDialog(this, "Error creating Gazetteer");
                }
            }
        }

        ViewDisplay viewDisplay;

        public AppFrame()
        {
            super(true, true, true);
            this.getControlPanel().add(makeControlPanel(), BorderLayout.SOUTH);

            WorldWindow wwd = this.getWwd();

            // Force the view to be a FlyView
            view = new BasicFlyView();
            wwd.setView(view);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    new TitledBorder("View Controls")));
            controlPanel.setToolTipText("Proved a location");
            viewDisplay = new ViewDisplay();
            controlPanel.add(viewDisplay);

            return (controlPanel);
        }
    }

    public static void main(String[] args)
    {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        ApplicationTemplate.start("WorldWind View Animator", AppFrame.class);
    }
}
