/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.antenna;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.io.InputStream;

/**
 * @author tag
 * @version $Id: AntennaViewer.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class AntennaViewer extends ApplicationTemplate
{
    protected static Position ANTENNA_POSITION = Position.fromDegrees(35, -120, 1e3);

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setOutlineOpacity(0.6);
            normalAttributes.setInteriorOpacity(0.4);
            normalAttributes.setOutlineMaterial(Material.WHITE);

            ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
            highlightAttributes.setOutlineOpacity(0.3);
            highlightAttributes.setInteriorOpacity(0.6);

            AntennaModel gain = new AntennaModel(makeInterpolator());
            gain.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            gain.setPosition(ANTENNA_POSITION);
            gain.setAzimuth(Angle.fromDegrees(30));
            gain.setElevationAngle(Angle.fromDegrees(20));
            gain.setAttributes(normalAttributes);
            gain.setHighlightAttributes(highlightAttributes);
            gain.setGainOffset(640);
            gain.setGainScale(10);

            AntennaAxes axes = new AntennaAxes();
            axes.setLength(2 * gain.getGainOffset());
            axes.setRadius(0.02 * axes.getLength());
            axes.setAltitudeMode(gain.getAltitudeMode());
            axes.setPosition(gain.getPosition());
            axes.setAzimuth(gain.getAzimuth());
            axes.setElevationAngle(gain.getElevationAngle());

            ShapeAttributes normalAxesAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorOpacity(0.5);
            normalAxesAttributes.setInteriorMaterial(Material.RED);
            normalAxesAttributes.setEnableLighting(true);
            axes.setAttributes(normalAxesAttributes);

            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(gain);
            layer.setName("Antenna Gain");
            insertBeforeCompass(getWwd(), layer);

            layer = new RenderableLayer();
            layer.addRenderable(axes);
            layer.setName("Antenna Axes");
            layer.setPickEnabled(false);
            insertBeforeCompass(getWwd(), layer);
        }
    }

    private static Interpolator2D makeInterpolator()
    {
        Interpolator2D interpolator = new Interpolator2D();
        interpolator.setWrapT(true); // wrap along "phi"

        try
        {
            InputStream is = WWIO.openFileOrResourceStream(
                "gov/nasa/worldwindx/examples/data/ThetaPhi3.antennaTestFile.txt", AntennaViewer.class);
            interpolator.addFromStream(is);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return interpolator;
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, ANTENNA_POSITION.getLatitude().degrees);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, ANTENNA_POSITION.getLongitude().degrees);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 30e3);

        ApplicationTemplate.start("World Wind Antenna Gain Visualization", AppFrame.class);
    }
}
