/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwindx.examples.symbology;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Demonstrates the simplest possible usage of WorldWind {@link TacticalSymbol} and {@link
 * gov.nasa.worldwind.symbology.TacticalGraphic} to display symbols from the MIL-STD-2525 symbology set. See the
 * <a href="https://worldwind.arc.nasa.gov/java/tutorials/tactical-graphics/" target="_blank">Tutorial</a>
 * for more information on symbology support in WorldWind.
 * <p>
 * For more detailed examples of how to use TacticalSymbol and TacticalGraphic in an application, see the {@link
 * TacticalSymbols} example and the {@link TacticalGraphics} example.
 *
 * @author pabercrombie
 * @version $Id: Symbology.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class Symbology extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Create a layer that displays WorldWind tactical symbols.
            this.addTacticalSymbols();

            // Create a layer that displays WorldWind tactical graphics.
            this.addTacticalGraphics();

            // Size the WorldWindow to provide enough screen space for the graphics, and center the WorldWindow
            // on the screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void addTacticalSymbols()
        {
            // Create a layer to display the tactical symbol. We add just one tactical symbol, but multiple tactical
            // symbols and tactical graphics may be combined on the same RenderableLayer.
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Tactical Symbols");

            // Create a tactical symbol for the MIL-STD-2525 symbology set. The symbol identifier specifies a
            // MIL-STD-2525 friendly Special Operations Forces Drone Aircraft. The position places the tactical symbol
            // at 3km above mean sea level.
            TacticalSymbol symbol = new MilStd2525TacticalSymbol("SFAPMFQM------A",
                Position.fromDegrees(34.4934, -117.6003, 3000));
            symbol.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Tactical Symbol"); // Tool tip text.
            symbol.setShowLocation(false);
            layer.addRenderable(symbol);

            // Add the symbol layer to the WorldWind model.
            this.getWwd().getModel().getLayers().add(layer);
        }

        protected void addTacticalGraphics()
        {
            // Create a layer to display the tactical graphic. We add just one tactical graphic, but multiple tactical
            // graphics and tactical symbols may be combined on the same RenderableLayer.
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Tactical Graphics");

            // Define the control point positions for the tactical graphic we create below.
            List<Position> positions = Arrays.asList(
                Position.fromDegrees(34.4980, -117.5541, 0),
                Position.fromDegrees(34.4951, -117.4667, 0),
                Position.fromDegrees(34.4733, -117.4303, 0),
                Position.fromDegrees(34.4217, -117.4056, 0),
                Position.fromDegrees(34.4780, -117.5300, 0));

            // Create a tactical graphic for the MIL-STD-2525 symbology set. The graphic identifies a MIL-STD-2525
            // friendly Supporting Attack.
            TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
            TacticalGraphic graphic = factory.createGraphic("GFGPOLAGS-----X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "MIL-STD-2525 Tactical Graphic"); // Tool tip text.
            layer.addRenderable(graphic);

            // Create point placemarks to mark each of the control points used to define the tactical graphic. This
            // provides a visualization of how the control point positions affect the displayed graphic.
            this.addControlPoints(positions, layer);

            // Add the graphic layer to the WorldWind model.
            this.getWwd().getModel().getLayers().add(layer);
        }

        /**
         * Add placemarks to a layer to mark the position of tactical graphic control points.
         *
         * @param positions list of control points positions.
         * @param layer     layer to receive control point placemarks.
         */
        protected void addControlPoints(Iterable<Position> positions, RenderableLayer layer)
        {
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setUsePointAsDefaultImage(true);

            int i = 1;
            for (Position p : positions)
            {
                PointPlacemark placemark = new PointPlacemark(p);
                placemark.setValue(AVKey.DISPLAY_NAME, "Tactical Graphic Position " + i);
                placemark.setAttributes(attrs);
                placemark.setHighlightAttributes(attrs);
                layer.addRenderable(placemark);
                i++;
            }
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 34.4780);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -117.5250);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 40000);

        ApplicationTemplate.start("WorldWind Symbology", AppFrame.class);
    }
}
