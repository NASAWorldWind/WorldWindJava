/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceText;

/**
 * Example of using the {@link SurfaceText} class. SurfaceText draws text on the surface of the globe.
 *
 * @author pabercrombie
 * @version $Id: SurfaceTextUsage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceTextUsage extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            SurfaceText surfaceText = new SurfaceText("Desolation Wilderness", Position.fromDegrees(38.9345, -120.1670, 0));
            layer.addRenderable(surfaceText);

            this.getWwd().getModel().getLayers().add(layer);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 38.9345);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -120.1670);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 50000);

        ApplicationTemplate.start("WorldWind Surface Text", AppFrame.class);
    }
}
