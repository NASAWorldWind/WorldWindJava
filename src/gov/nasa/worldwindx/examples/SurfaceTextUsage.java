/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.SurfaceText;

/**
 * Example of using the {@link SurfaceText} class. SurfaceText draws text on the surface of the globe.
 *
 * @author pabercrombie
 * @version $Id: SurfaceTextUsage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceTextUsage extends ApplicationTemplate
{
    private static Position center = Position.fromDegrees(38.9345, -120.1670, 50000);
    
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();
            
            int j = 0;
            for (double x = -1.0; x <= 0.0; x += 0.5, j++)
            {
                for (double y = -1.0; y <= 0.0; y += 0.5, j++)
                {
                    for (int i = 0; i <= 12; i++)
                    {
                        double latitude = center.latitude.degrees + ((j - 4) / 5.0);
                        double longitude = center.longitude.degrees + ((i - 6) / 5.0);
                        Position position = Position.fromDegrees(latitude, longitude, 0);
                        
                        SurfaceText surfaceText = new SurfaceText("Test Label Description", position);
                        surfaceText.setDrawBoundingSectors(true);
                        surfaceText.setHeading(Angle.fromDegrees(i * 30));
                        surfaceText.setOffset(Offset.fromFraction(x, y));
                        layer.addRenderable(surfaceText);

                        PointPlacemark placemark = new PointPlacemark(position);
                        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                        PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
                        attrs.setLabelColor("ffffffff");
                        attrs.setLineColor("ff0000ff");
                        attrs.setUsePointAsDefaultImage(true);
                        attrs.setScale(5d);
                        placemark.setAttributes(attrs);
                        layer.addRenderable(placemark);
                    }
                }
            }

            this.getWwd().getModel().getLayers().add(layer);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, center.latitude.degrees);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, center.longitude.degrees);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, center.elevation);

        ApplicationTemplate.start("WorldWind Surface Text", AppFrame.class);
    }
}
