/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.ContourBuilder;
import gov.nasa.worldwindx.examples.analytics.*;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Shows how to use the {@link gov.nasa.worldwind.util.ContourBuilder} class to compute contour lines in an arbitrary
 * rectangular array of numeric values.
 * <p/>
 * This example creates a 60x60 rectangular array of floating point values in the range from 0.0 to 1.0, inclusive. The
 * array is displayed on the globe as an AnalyticSurface by mapping each array value to a color, where an array value of
 * 0.0 maps to hue 0, an array value of 1.0 maps to hue 360, and values in between are interpolated between those two
 * hues. Contour lines are created at the boundaries between adjacent colors using ContourBuilder, and are displayed on
 * the globe as Path shapes.
 *
 * @author dcollins
 * @version $Id: ContourBuilderExample.java 2429 2014-11-14 17:52:34Z dcollins $
 */
public class ContourBuilderExample extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected Sector arraySector;
        protected int arrayWidth;
        protected int arrayHeight;
        protected double[] arrayValues;

        public AppFrame()
        {
            // Create the rectangular array of numeric values in the range from 0.0 to 1.0, inclusive, and assign the
            // array a geographic sector.
            this.createRectangularArray();

            // Display the rectangular array of numeric values as a surface that maps each numeric value to a hue, and
            // interpolates the colors between array points.
            RenderableLayer arrayLayer = new RenderableLayer();
            arrayLayer.setName("Rectangular Array");
            this.getWwd().getModel().getLayers().add(arrayLayer);
            this.addRectangularArrayShapes(arrayLayer);

            // Create a layer to group the contour line shapes.
            RenderableLayer contourLayer = new RenderableLayer();
            contourLayer.setName("Contour Lines");
            this.getWwd().getModel().getLayers().add(contourLayer);

            // Create a ContourBuilder with the rectangular array of numeric values as a one-dimensional array of
            // floating point numbers. The contour builder assumes that the array is organized in row-major order, with
            // the first value indicating the value at the upper-left corner.
            ContourBuilder cb = new ContourBuilder(this.arrayWidth, this.arrayHeight, this.arrayValues);

            // Build contour lines for a list of pre-determined threshold values. Contour line coordinates are computed
            // by mapping the rectangular array's coordinates to a geographic sector.
            for (double value : Arrays.asList(0.083, 0.250, 0.416, 0.583, 0.75, 0.916))
            {
                List<List<Position>> contourList = cb.buildContourLines(value, this.arraySector, 0); // altitude 0
                this.addContourShapes(contourList, value, contourLayer);
            }
        }

        protected void createRectangularArray()
        {
            this.arraySector = Sector.fromDegrees(20, 30, -110, -100);
            this.arrayWidth = 60;
            this.arrayHeight = 60;
            this.arrayValues = ExampleUtil.readCommaDelimitedNumbers(
                "gov/nasa/worldwindx/examples/data/GridValues01_60x60.csv");
        }

        protected void addContourShapes(List<List<Position>> contourList, double value, RenderableLayer layer)
        {
            String text = this.textForValue(value);
            Color color = this.colorForValue(value, 1.0); // color for value at 100% brightness

            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(color));
            attrs.setOutlineWidth(2);

            for (List<Position> positions : contourList)
            {
                Path path = new Path(positions);
                path.setAttributes(attrs);
                path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                path.setFollowTerrain(true);
                path.setValue(AVKey.DISPLAY_NAME, text);
                layer.addRenderable(path);
            }
        }

        protected void addRectangularArrayShapes(RenderableLayer layer)
        {
            ArrayList<AnalyticSurface.GridPointAttributes> pointAttrs =
                new ArrayList<AnalyticSurface.GridPointAttributes>();
            for (double value : this.arrayValues)
            {
                Color color = this.colorForValue(value, 0.5); // color for value at 50% brightness
                pointAttrs.add(AnalyticSurface.createGridPointAttributes(value, color));
            }

            AnalyticSurfaceAttributes attrs = new AnalyticSurfaceAttributes();
            attrs.setDrawOutline(false);
            attrs.setDrawShadow(false);

            AnalyticSurface surface = new AnalyticSurface();
            surface.setSurfaceAttributes(attrs);
            surface.setSector(this.arraySector);
            surface.setDimensions(this.arrayWidth, this.arrayHeight);
            surface.setValues(pointAttrs);
            surface.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            layer.addRenderable(surface);
        }

        protected Color colorForValue(double value, double brightness)
        {
            return Color.getHSBColor((float) value, 1.0f, (float) brightness); // use array value as hue
        }

        protected String textForValue(double value)
        {
            return String.format("%.2f", value);
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Contour Building", AppFrame.class);
    }
}
