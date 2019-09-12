/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.Airspace;

/**
 * Illustrates how to cause all elements under the cursor in a WorldWindow to be reported in <code>{@link
 * SelectEvent}s</code>. This prints all elements under the cursor to the console in response to a <code>HOVER</code>
 * SelectEvent.
 * <p>
 * In order to enable deep picking, any batch picking for the desired elements must be disabled and the
 * SceneController's deep picking property must be enabled. See <code>{@link gov.nasa.worldwind.SceneController#setDeepPickEnabled(boolean)}</code>.
 *
 * @author tag
 * @version $Id: DeepPicking.java 2231 2014-08-15 19:03:12Z dcollins $
 */
public class DeepPicking extends Airspaces
{
    public static class AppFrame extends Airspaces.AppFrame
    {
        public AppFrame()
        {
            // Prohibit batch picking for the airspaces.
            this.disableBatchPicking();

            // Tell the scene controller to perform deep picking.
            this.getWwd().getSceneController().setDeepPickEnabled(true);

            // Register a select listener to print the class names of the items under the cursor.
            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.HOVER) && event.getObjects() != null)
                    {
                        System.out.printf("%d objects\n", event.getObjects().size());
                        if (event.getObjects().size() > 1)
                        {
                            for (PickedObject po : event.getObjects())
                            {
                                System.out.println(po.getObject().getClass().getName());
                            }
                        }
                    }
                }
            });
        }

        protected void disableBatchPicking()
        {
            for (Layer layer : this.getWwd().getModel().getLayers())
            {
                if (!layer.getName().toLowerCase().contains("airspace"))
                    continue;

                for (Renderable airspace : ((RenderableLayer) layer).getRenderables())
                {
                    ((Airspace) airspace).setEnableBatchPicking(false);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        start("WorldWind Deep Picking", AppFrame.class);
    }
}
