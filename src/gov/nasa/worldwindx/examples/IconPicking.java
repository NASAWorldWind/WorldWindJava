/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.UserFacingIcon;

import java.awt.*;

/**
 * Shows how to detect picked icons. Place the cursor over the icons to see the response printed to the console.
 *
 * @author Patrick Murris
 * @version $Id: IconPicking.java 2219 2014-08-11 21:39:44Z dcollins $
 * @see gov.nasa.worldwind.globes.FlatGlobe
 * @see EarthFlat
 */
public class IconPicking extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            IconLayer layer = new IconLayer();
            layer.setPickEnabled(true);
            layer.setAllowBatchPicking(false);
            layer.setRegionCulling(true);

            UserFacingIcon icon = new UserFacingIcon("src/images/32x32-icon-nasa.png",
                new Position(Angle.fromRadians(0), Angle.fromRadians(0), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("src/images/32x32-icon-nasa.png",
                new Position(Angle.fromRadians(0.1), Angle.fromRadians(0.0), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("src/images/32x32-icon-nasa.png",
                new Position(Angle.fromRadians(0.0), Angle.fromRadians(0.1), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("src/images/32x32-icon-nasa.png",
                new Position(Angle.fromRadians(0.1), Angle.fromRadians(0.1), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("src/images/32x32-icon-nasa.png",
                new Position(Angle.fromRadians(0), Angle.fromDegrees(180), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);

            this.getWwd().addSelectListener(new SelectListener()
            {
                @Override
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                    {
                        PickedObjectList pol = event.getObjects();
                        System.out.println(" Picked Objects Size " + pol.size());
                        for (PickedObject po : pol)
                        {
                            System.out.println(" Class " + po.getObject().getClass().getName() + "  isTerrian=" + po.isTerrain());
                        }
                    }
                }
            });
            this.getWwd().getSceneController().setDeepPickEnabled(true);
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 27e6);
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 88);
        ApplicationTemplate.start("World Wind Icon Picking", AppFrame.class);
    }
}
