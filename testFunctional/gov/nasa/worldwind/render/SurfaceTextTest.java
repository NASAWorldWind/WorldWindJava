/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

/**
 * Test of {@link SurfaceText} rotation and offset calculations. This test creates
 * several SurfaceText objects with different rotation and offset configurations.
 * The SurfaceText objects can be visually inspected to confirm that the rotations
 * and placements are correctly calculated, and that all of them are located
 * within their respective bounding-sectors.
 *
 * @author pabercrombie
 * @version $Id: SurfaceTextTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceTextTest extends ApplicationTemplate
{
    private static Position center = Position.fromDegrees(38.9345, -120.1670, 50000);
    
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();
            
            PointPlacemarkAttributes attributes = new PointPlacemarkAttributes();
            attributes.setLabelColor("ffffffff");
            attributes.setLineColor("ff0000ff");
            attributes.setUsePointAsDefaultImage(true);
            attributes.setScale(5d);
            
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
                        placemark.setAttributes(attributes);
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
