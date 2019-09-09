/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.symbology;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Demonstrates how to create and display WorldWind tactical graphics. See the 
 * <a href="https://worldwind.arc.nasa.gov/java/tutorials/tactical-graphics/" target="_blank">Tutorial</a>
 * for more
 * information on symbology support in WorldWind.
 * <p>
 * See the {@link TacticalSymbols} for a detailed example of using WorldWind tactical symbols in an application.
 *
 * @author pabercrombie
 * @version $Id: TacticalGraphics.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class TacticalGraphics extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected RenderableLayer pointLayer;
        protected RenderableLayer lineLayer;
        protected RenderableLayer areaLayer;

        /** Shared attributes for line and area graphics. */
        protected TacticalGraphicAttributes sharedAttrs;
        /** Shared attributes for point graphics. */
        protected TacticalGraphicAttributes sharedPointAttrs;

        public AppFrame()
        {
            super(true, true, false);

            this.pointLayer = new RenderableLayer();
            this.pointLayer.setName("Tactical Graphics (Points)");

            this.lineLayer = new RenderableLayer();
            this.lineLayer.setName("Tactical Graphics (Lines)");

            this.areaLayer = new RenderableLayer();
            this.areaLayer.setName("Tactical Graphics (Areas)");

            // Create attributes for line and area graphics. Line and area graphics use the scale attribute to determine
            // the size of tactical symbols included in the graphic. Setting the scale to 1/4 prevents the symbol
            // overwhelming the rest of the graphic.
            this.sharedAttrs = new BasicTacticalGraphicAttributes();
            this.sharedAttrs.setScale(0.25);

            // Create attributes for point graphics. Point graphics use the scale attribute to determine the size of the
            // point graphic icon. Do not share attributes with line and area graphics because point graphics look
            // better with a larger scale value.
            this.sharedPointAttrs = new BasicTacticalGraphicAttributes();

            // Create tactical symbols and graphics and add them to the layer
            this.createPointGraphics(this.pointLayer);
            this.createLineGraphics(this.lineLayer);
            this.createAreaGraphics(this.areaLayer);

            insertBeforePlacenames(getWwd(), this.pointLayer);
            insertBeforePlacenames(getWwd(), this.lineLayer);
            insertBeforePlacenames(getWwd(), this.areaLayer);

            // Add a BasicDragger so that graphics can be moved by clicking and dragging.
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            this.addGraphicControls();

            // Size the WorldWindow to provide enough screen space for the graphics, and center the WorldWindow
            // on the screen.
            Dimension size = new Dimension(1800, 1000);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void createPointGraphics(RenderableLayer layer)
        {
            TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
            TacticalGraphic graphic;

            /////////////////////////////////////////////
            // Nuclear Event (2.X.3.4.2)
            /////////////////////////////////////////////

            graphic = factory.createPoint("GHMPNZ--------X", Position.fromDegrees(35.2144, -117.8824), null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Nuclear Event (2.X.3.4.2)");
            graphic.setText("X691");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, "10095900ZJAN92");
            graphic.setModifier(SymbologyConstants.QUANTITY, "15");
            graphic.setModifier(SymbologyConstants.TYPE, "B83");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // High tower
            /////////////////////////////////////////////

            graphic = factory.createPoint("GHMPOHTH------X", Position.fromDegrees(35.2544, -117.724), null);
            graphic.setValue(AVKey.DISPLAY_NAME, "High Tower");
            graphic.setModifier(SymbologyConstants.ALTITUDE_DEPTH, "2562");
            layer.addRenderable(graphic);

            // Apply shared attributes to all graphics on this layer
            this.setAttributes(layer, this.sharedAttrs, this.sharedPointAttrs);
        }

        protected void createLineGraphics(RenderableLayer layer)
        {
            TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
            TacticalGraphic graphic;

            /////////////////////////////////////////////
            // Phase line (2.X.2.1.2.4)
            /////////////////////////////////////////////

            // Create a Friendly Phase Line
            List<Position> positions = Arrays.asList(
                Position.fromDegrees(34.9349, -117.6303),
                Position.fromDegrees(34.9843, -117.5885),
                Position.fromDegrees(34.9961, -117.4891));
            graphic = factory.createGraphic("GFGPGLP----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Phase line: friendly (2.X.2.2.2.2)");
            graphic.setText("A");
            layer.addRenderable(graphic);

            // Create a Hostile Phase Line
            positions = Arrays.asList(
                Position.fromDegrees(34.9190, -117.5919),
                Position.fromDegrees(34.9589, -117.5618),
                Position.fromDegrees(34.9701, -117.4798));
            graphic = factory.createGraphic("GHGPGLP----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Phase line: hostile (2.X.2.2.2.2)");
            graphic.setText("B");
            layer.addRenderable(graphic);

            // Create a Hostile, Anticipated, Phase Line
            positions = Arrays.asList(
                Position.fromDegrees(34.8910, -117.5726),
                Position.fromDegrees(34.9367, -117.5354),
                Position.fromDegrees(34.9480, -117.4741));
            graphic = factory.createGraphic("GHGAGLP----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Phase line: hostile, anticipated (2.X.2.2.2.2)");
            graphic.setText("C");
            layer.addRenderable(graphic);

            // Create a line with a custom color and font
            positions = Arrays.asList(
                Position.fromDegrees(34.8703, -117.5525),
                Position.fromDegrees(34.9158, -117.5153),
                Position.fromDegrees(34.9238, -117.4600));
            graphic = factory.createGraphic("GFGPGLP----AUSX", positions, null);
            graphic.setText("D");
            graphic.setValue(AVKey.DISPLAY_NAME, "Phase line: custom color and font (2.X.2.2.2.2)");

            // Create a custom attributes bundle. Any fields set in this bundle will override the default attributes.
            TacticalGraphicAttributes attrs = new BasicTacticalGraphicAttributes();
            attrs.setOutlineMaterial(Material.GRAY);
            attrs.setTextModifierFont(Font.decode("Arial-12"));

            // Apply the attributes to the graphic
            graphic.setAttributes(attrs);
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Boundary (2.X.2.1.2.1)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.5479, -117.8777),
                Position.fromDegrees(34.5137, -118.0092),
                Position.fromDegrees(34.5898, -118.0775),
                Position.fromDegrees(34.5360, -118.1833));
            graphic = factory.createGraphic("GHGPGLB----JUSX", positions, null);
            graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, Arrays.asList("40ID", "18ID"));
            graphic.setValue(AVKey.DISPLAY_NAME, "Boundary (2.X.2.1.2.1)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Main Attack (2.X.2.5.2.1.4.1)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.6643, -117.7323), // Pt. 1: Tip of the arrow
                Position.fromDegrees(34.6962, -117.7808), // Pt. 2: First path control point
                Position.fromDegrees(34.6934, -117.8444),
                Position.fromDegrees(34.6602, -117.8570), // Pt. N - 1: Last path control point
                Position.fromDegrees(34.6844, -117.7303)); // Pt. N: Width of the arrow head
            graphic = factory.createGraphic("GFGPOLAGM-----X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Main Attack (2.X.2.5.2.1.4.1)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Attack, rotary wing (2.X.2.5.2.1.3)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7610, -117.4541),
                Position.fromDegrees(34.7614, -117.5852),
                Position.fromDegrees(34.7287, -117.6363),
                Position.fromDegrees(34.6726, -117.6363),
                Position.fromDegrees(34.7820, -117.4700));
            graphic = factory.createGraphic("GFGPOLAR------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Attack, Rotary Wing (2.X.2.5.2.1.3)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Aviation axis of advance (2.X.2.5.2.1.1)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7437, -117.8007),
                Position.fromDegrees(34.7535, -117.9256),
                Position.fromDegrees(34.8051, -117.9707),
                Position.fromDegrees(34.7643, -117.8219));
            graphic = factory.createGraphic("GFGPOLAV------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Aviation (2.X.2.5.2.1.1)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Supporting attack (2.X.2.5.2.1.4.2)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.6980, -117.5541),
                Position.fromDegrees(34.6951, -117.4667),
                Position.fromDegrees(34.6733, -117.4303),
                Position.fromDegrees(34.6217, -117.4056),
                Position.fromDegrees(34.6780, -117.53));
            graphic = factory.createGraphic("GFGPOLAGS-----X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Supporting Attack (2.X.2.5.2.1.4.2)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Airborne (2.X.2.5.2.1.2)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7099, -117.9047),
                Position.fromDegrees(34.6725, -117.9590),
                Position.fromDegrees(34.6814, -118.0522),
                Position.fromDegrees(34.7124, -117.9284));
            graphic = factory.createGraphic("GFGPOLAA------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Airborne (2.X.2.5.2.1.2)");
            graphic.setModifier(SymbologyConstants.SYMBOL_INDICATOR, "SFGPUCI--------");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Minimum risk route (2.X.2.2.2.2)
            /////////////////////////////////////////////

            // Create a Minimum Risk Route graphic
            // First create some Air Control Points to place along the route
            List<TacticalPoint> controlPoints = new ArrayList<TacticalPoint>();

            // Create an Air Control Point
            TacticalPoint point = factory.createPoint("GFGPAPP-------X", Position.fromDegrees(34.8802, -117.9773),
                null);
            point.setText("1");
            controlPoints.add(point);

            // And another Air Control Point
            point = factory.createPoint("GFGPAPP-------X", Position.fromDegrees(35.0947, -117.9578), null);
            point.setText("2");
            controlPoints.add(point);

            // And a Communication Checkpoint
            point = factory.createPoint("GFGPAPC-------X", Position.fromDegrees(35.1739, -117.6957), null);
            point.setText("3");
            controlPoints.add(point);

            // Now create the route from the control points
            TacticalRoute route = factory.createRoute("GFGPALM-------X", controlPoints, null);
            route.setValue(AVKey.DISPLAY_NAME, "Minimum Risk Route (2.X.2.2.2.2)");
            route.setText("KNIGHT");
            route.setModifier(SymbologyConstants.DISTANCE, 4000.0);
            route.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("270600Z", "271845Z"));
            route.setModifier(SymbologyConstants.ALTITUDE_DEPTH, Arrays.asList("50 FT AGL", "200 FT AGL"));

            // And add the route to the layer. Note that we do not need to add the individual control points
            // to the layer because the route will take care of drawing them.
            layer.addRenderable(route);

            ///////////////////////////////////////////////////
            // Direction of Main Attack (2.X.2.5.2.2.2.1)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.0459, -117.3795),
                Position.fromDegrees(35.0459, -117.5633));
            graphic = factory.createGraphic("GFGPOLKGM-----X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Direction of Main Attack (2.X.2.5.2.2.2.1)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Final Protective Fire (2.X.4.2.1.2)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.8917, -117.8344),
                Position.fromDegrees(34.8283, -117.6794));
            graphic = factory.createGraphic("GFFPLTF-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Final Protective Fire (2.X.4.2.1.2)");
            graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, Arrays.asList("AG1201", "2-22 INF MORT"));
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Ambush (2.X.2.6.1.1)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.5556, -117.4123),
                Position.fromDegrees(34.5210, -117.4786),
                Position.fromDegrees(34.5942, -117.4766));
            graphic = factory.createGraphic("GFGPSLA-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Ambush (2.X.2.6.1.1)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Forward Edge of Battle Area (FEBA) (2.X.2.4.2.1)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.2805, -117.5978),
                Position.fromDegrees(35.2978, -117.3702));
            graphic = factory.createGraphic("GFGPDLF-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Forward Edge of Battle Area (2.X.2.4.2.1)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Line of Contact (.X.2.1.2.3)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.2829, -117.5900),
                Position.fromDegrees(35.2990, -117.3727));
            graphic = factory.createGraphic("GFGPGLC-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Line of Contact (2.X.2.1.2.3)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Fire Support Coordination Line (2.X.4.2.2.1)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.4179, -117.4399),
                Position.fromDegrees(34.4486, -117.6857));
            graphic = factory.createGraphic("GFFPLCF-------X", positions, null);
            graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, Arrays.asList("X CORPS", "ALPHA"));
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("202100Z", "270800Z SEP"));
            graphic.setValue(AVKey.DISPLAY_NAME, "Fire Support Coordination Line (2.X.4.2.2.1)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Principle Direction of Fire (2.X.2.4.2.2)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7502, -118.1980),
                Position.fromDegrees(34.8542, -118.2565),
                Position.fromDegrees(34.8515, -118.1129));
            graphic = factory.createGraphic("GFGPDLP-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Principle Direction of Fire (2.X.2.4.2.2)");
            graphic.setModifier(SymbologyConstants.SYMBOL_INDICATOR, "SFGPEWRH--MTUSG");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Search Area/Reconnaissance Area (2.X.2.1.3.9)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.9738, -118.2632),
                Position.fromDegrees(34.9306, -118.1597),
                Position.fromDegrees(35.0092, -118.1717));
            graphic = factory.createGraphic("GFGPGAS-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Search Area/Reconnaissance Area (2.X.2.1.3.9)");
            graphic.setModifier(SymbologyConstants.SYMBOL_INDICATOR, "SFGPEWRH--MTUSG");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Forward Line of Own Troops (2.X.2.1.2.2)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.3401, -117.6356),
                Position.fromDegrees(35.3723, -117.5290),
                Position.fromDegrees(35.3776, -117.4182));
            graphic = factory.createGraphic("GFGPGLF-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Forward Line of Own Troops (2.X.2.1.2.2)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Direction of Attack, Aviation (2.X.2.5.2.2.1)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.1143, -118.0609),
                Position.fromDegrees(35.0765, -118.2397));
            graphic = factory.createGraphic("GHGPOLKA------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Direction of Attack, Aviation (2.X.2.5.2.2.1)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Infiltration Lane (2.X.2.5.2.4)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.2678, -118.0194),
                Position.fromDegrees(35.1504, -118.1822),
                Position.fromDegrees(35.2402, -118.0716));
            graphic = factory.createGraphic("GFGPOLI-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Infiltration Lane (2.X.2.5.2.4)");
            graphic.setText("CHARLIE");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Axis of Advance for Feint (2.X.2.3.2)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.6377, -118.0770),
                Position.fromDegrees(34.6413, -118.1572),
                Position.fromDegrees(34.6808, -118.2100),
                Position.fromDegrees(34.6547, -118.0988));
            graphic = factory.createGraphic("GFGPPA--------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Axis of Advance for Feint (2.X.2.3.2)");
            graphic.setText("GREEN");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Holding Line (2.X.2.6.1.2)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.1184, -118.4377),
                Position.fromDegrees(35.0339, -118.4318),
                Position.fromDegrees(35.0843, -118.3376));
            graphic = factory.createGraphic("GFGPSLH-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Holding Line (2.X.2.6.1.2)");
            graphic.setText("ALPHA");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Direction of Attack for Feint (2.X.2.3.3)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.0656, -118.0508),
                Position.fromDegrees(35.0383, -118.2275));
            graphic = factory.createGraphic("GFGPPF--------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Direction of Attack for Feint (2.X.2.3.3)");
            graphic.setText("DAVID");
            layer.addRenderable(graphic);

            // Apply shared attributes to all graphics on this layer
            this.setAttributes(layer, this.sharedAttrs, this.sharedPointAttrs);
        }

        protected void createAreaGraphics(RenderableLayer layer)
        {
            TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
            TacticalGraphic graphic;

            /////////////////////////////////////////////
            // Assembly area (2.X.2.1.3.2)
            /////////////////////////////////////////////

            List<Position> positions = Arrays.asList(
                Position.fromDegrees(34.9130, -117.1897),
                Position.fromDegrees(34.9789, -117.1368),
                Position.fromDegrees(34.9706, -116.9900),
                Position.fromDegrees(34.9188, -116.9906));
            graphic = factory.createGraphic("GHGPGAA----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Assembly Area (2.X.2.1.3.2)");
            graphic.setText("Atlanta");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Fortified area (2.X.2.1.3.4)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7985, -117.1938),
                Position.fromDegrees(34.8358, -117.1282),
                Position.fromDegrees(34.8456, -117.0773),
                Position.fromDegrees(34.8159, -116.9723),
                Position.fromDegrees(34.7836, -117.1010),
                Position.fromDegrees(34.7985, -117.1938));
            graphic = factory.createGraphic("GHGPGAF----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Fortified Area (2.X.2.1.3.4)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Airfield zone (2.X.2.1.3.11)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.9001, -117.4044),
                Position.fromDegrees(34.9910, -117.3297),
                Position.fromDegrees(34.9851, -117.2224),
                Position.fromDegrees(34.9134, -117.2670));
            graphic = factory.createGraphic("GFGPGAZ----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Airfield Zone (2.X.2.1.3.11)");
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Restricted Operation Zone (2.X.2.2.3.1)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.2331, -117.6217),
                Position.fromDegrees(35.2331, -117.3552),
                Position.fromDegrees(35.1998, -117.2560),
                Position.fromDegrees(35.0851, -117.3604),
                Position.fromDegrees(35.0857, -117.6261));
            graphic = factory.createGraphic("GFGPAAR----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Restricted Operations Zone (2.X.2.2.3.1)");
            graphic.setText("(Unit ID)");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("180500Z", "180615Z"));
            graphic.setModifier(SymbologyConstants.ALTITUDE_DEPTH, Arrays.asList("2000 FT AGL", "3000 FT AGL"));
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Weapons Free Zone (2.X.2.2.3.5)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7849, -117.3661),
                Position.fromDegrees(34.6715, -117.3738),
                Position.fromDegrees(34.6374, -117.3208),
                Position.fromDegrees(34.6549, -117.1448),
                Position.fromDegrees(34.7506, -117.1436));
            graphic = factory.createGraphic("GFGPAAW----AUSX", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Weapons Free Zone (2.X.2.2.3.5)");
            graphic.setText("(Unit ID)");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("1000Z", "1300Z"));
            layer.addRenderable(graphic);

            /////////////////////////////////////////////
            // Circular target (2.X.4.3.1.2)
            /////////////////////////////////////////////

            Position position = (Position.fromDegrees(35.1108, -117.0870));
            TacticalCircle circle = factory.createCircle("GHFPATC-------X", position, 5000.0, null);
            circle.setValue(AVKey.DISPLAY_NAME, "Circular Target (2.X.4.3.1.2)");
            circle.setText("AG9999");
            layer.addRenderable(circle);

            /////////////////////////////////////////////
            // Rectangular target (2.X.4.3.1.1)
            /////////////////////////////////////////////

            position = (Position.fromDegrees(35.0295, -116.9290));
            TacticalQuad quad = factory.createQuad("GHFPATR-------X", Arrays.asList(position), null);
            quad.setLength(8000.0);
            quad.setWidth(4000.0);
            quad.setText("AB0176");
            quad.setValue(AVKey.DISPLAY_NAME, "Rectangular Target (2.X.4.3.1.1)");
            layer.addRenderable(quad);

            /////////////////////////////////////////////
            // Position Area for Artillery, Circular target (2.X.4.3.2.6.2)
            /////////////////////////////////////////////

            position = Position.fromDegrees(35.2106, -117.0092);
            circle = factory.createCircle("GFFPACPC------X", position, 5000.0, null);
            circle.setValue(AVKey.DISPLAY_NAME, "Position Area for Artillery (2.X.4.3.2.6.2)");
            layer.addRenderable(circle);

            /////////////////////////////////////////////
            // Position Area for Artillery, Rectangular target (2.X.4.3.2.6.1)
            /////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.2520, -116.8968),
                Position.fromDegrees(35.2364, -116.8063));
            quad = factory.createQuad("GFFPACPR------X", positions, null);
            quad.setWidth(5000.0);
            quad.setValue(AVKey.DISPLAY_NAME, "Position Area for Artillery (2.X.4.3.2.6.1)");
            layer.addRenderable(quad);

            //////////////////////////////////////////////////
            // Circular Weapon/Sensor Range Fan (2.X.4.3.4.1)
            //////////////////////////////////////////////////

            position = (Position.fromDegrees(34.6813, -116.9724));
            graphic = factory.createGraphic("GFFPAXC-------X", Arrays.asList(position), null);
            graphic.setModifier(SymbologyConstants.DISTANCE, Arrays.asList(1000.0, 6000.0, 11000.0));
            graphic.setModifier(SymbologyConstants.ALTITUDE_DEPTH, Arrays.asList("GL", "GL", "GL"));
            graphic.setValue(AVKey.DISPLAY_NAME, "Weapon/Sensor Range Fan (2.X.4.3.4.1)");

            // Add a symbol at the center of the range fan
            graphic.setModifier(SymbologyConstants.SYMBOL_INDICATOR, "SFGPUCFH-------");

            layer.addRenderable(graphic);

            //////////////////////////////////////////////////
            // Sector Weapon/Sensor Range Fan (2.X.4.3.4.2)
            //////////////////////////////////////////////////

            List<Angle> azimuths = Arrays.asList(
                Angle.fromDegrees(290), Angle.fromDegrees(30), // Range 1, left azimuth, right azimuth
                Angle.fromDegrees(290), Angle.fromDegrees(30), // Range 2, left azimuth, right azimuth
                Angle.fromDegrees(315), Angle.fromDegrees(30), // Range 3, left azimuth, right azimuth
                Angle.fromDegrees(315), Angle.fromDegrees(35));

            position = (Position.fromDegrees(34.5798, -116.6591));
            graphic = factory.createGraphic("GFFPAXS-------X", Arrays.asList(position), null);
            graphic.setModifier(SymbologyConstants.DISTANCE, Arrays.asList(4000.0, 6000.0, 11000.0, 15000.0));
            graphic.setModifier(SymbologyConstants.AZIMUTH, azimuths);
            graphic.setModifier(SymbologyConstants.ALTITUDE_DEPTH, Arrays.asList("GL", "GL", "GL", "GL"));
            graphic.setValue(AVKey.DISPLAY_NAME, "Weapon/Sensor Range Fan (2.X.4.3.4.2)");

            // Add a symbol at the center of the range fan.
            graphic.setModifier(SymbologyConstants.SYMBOL_INDICATOR, "SFGPUCFTR------");
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Restrictive Fire Area (RFA), Rectangular (2.X.4.3.2.5.2)
            ////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.9672, -116.8974),
                Position.fromDegrees(34.9188, -116.8205));
            graphic = factory.createGraphic("GFFPACRR------X", positions, null);
            graphic.setModifier(SymbologyConstants.DISTANCE, 5000.0);
            graphic.setValue(AVKey.DISPLAY_NAME, "Restrictive Fire Area (2.X.4.3.2.5.2)");
            graphic.setText("X CORPS");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("051030", "051600Z"));
            layer.addRenderable(graphic);

            /////////////////////////////////////////////////////
            // Fire Support Area (FSA), Circular (2.X.4.3.2.1.3)
            /////////////////////////////////////////////////////

            position = Position.fromDegrees(34.8208, -116.8284);
            graphic = factory.createCircle("GFFPACSC------X", position, 5000.0, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Fire Support Area (2.X.4.3.2.1.3)");
            graphic.setText("GREEN");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("051030", "051600Z"));
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Sensor Zone, Rectangular
            ////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.0592, -117.2903),
                Position.fromDegrees(35.0620, -117.1606));
            graphic = factory.createGraphic("GFFPACER------X", positions, null);
            graphic.setModifier(SymbologyConstants.DISTANCE, 5000.0);
            graphic.setValue(AVKey.DISPLAY_NAME, "Sensor Zone");
            graphic.setText("Q37");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("051030", "051600Z"));
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Call for Fire Zone, Irregular (2.X.4.3.3.2.1)
            ////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.1386, -116.9302),
                Position.fromDegrees(35.1030, -116.9302),
                Position.fromDegrees(35.0731, -116.9561),
                Position.fromDegrees(35.0620, -116.8345),
                Position.fromDegrees(35.1422, -116.8193));
            graphic = factory.createGraphic("GFFPAZXI------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Call for Fire Zone (2.X.4.3.3.2.1)");
            graphic.setText("3BDE 4ID");
            graphic.setModifier(SymbologyConstants.DATE_TIME_GROUP, Arrays.asList("051030", "051600Z"));
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Series or Group of Targets (2.X.4.3.1.3)
            ////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.0160, -116.6625),
                Position.fromDegrees(35.0601, -116.6773),
                Position.fromDegrees(35.0542, -116.7611),
                Position.fromDegrees(35.0092, -116.7790));
            graphic = factory.createGraphic("GFFPATG-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Series or Group of Targets (2.X.4.3.1.3)");
            graphic.setText("JEFF");
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Airhead (2.X.2.6.2.2)
            ////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(35.1260, -116.6625),
                Position.fromDegrees(35.1701, -116.6773),
                Position.fromDegrees(35.1642, -116.7611),
                Position.fromDegrees(35.1192, -116.7790));
            graphic = factory.createGraphic("GFGPSAA-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Airhead (2.X.2.6.2.2)");
            graphic.setText("DELTA");
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Minimum Safe Distance Zones (2.X.3.4.1)
            ////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.9073, -116.7041), // Center point of graphic
                Position.fromDegrees(34.9230, -116.6842), // Radius of ring 1
                Position.fromDegrees(34.8723, -116.6841), // Radius of ring 2
                Position.fromDegrees(34.8870, -116.7783)); // Radius of ring 3
            graphic = factory.createGraphic("GFMPNM--------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Minimum Safe Distance Zones (2.X.3.4.1)");
            layer.addRenderable(graphic);

            ////////////////////////////////////////////////////////////
            // Pull-Up Point (2.X.3.4.1)
            ////////////////////////////////////////////////////////////

            position = Position.fromDegrees(35.2132, -117.8042);
            circle = factory.createCircle("GFGPAPU-------X", position, 2000.0, null);
            circle.setValue(AVKey.DISPLAY_NAME, "Pull-Up Point (2.X.2.2.1.3)");
            layer.addRenderable(circle);

            ///////////////////////////////////////////////////
            // Attack by Fire Position (2.X.2.5.3.3)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.6297, -117.6718),
                Position.fromDegrees(34.5807, -117.5902),
                Position.fromDegrees(34.5344, -117.6686));
            graphic = factory.createGraphic("GFGPOAF-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Attack by Fire Position (2.X.2.5.3.3)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Support by Fire Position (2.X.2.5.3.4)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.6159, -117.8571),
                Position.fromDegrees(34.5775, -117.7887),
                Position.fromDegrees(34.6308, -117.8477),
                Position.fromDegrees(34.5837, -117.7725));
            graphic = factory.createGraphic("GFGPOAS-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Support by Fire Position (2.X.2.5.3.4)");
            layer.addRenderable(graphic);

            ///////////////////////////////////////////////////
            // Biologically Contaminated Area (2.X.3.4.5)
            ///////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.4916, -117.3427),
                Position.fromDegrees(34.5770, -117.2273),
                Position.fromDegrees(34.5666, -117.0920),
                Position.fromDegrees(34.4943, -117.0629),
                Position.fromDegrees(34.4570, -117.1613),
                Position.fromDegrees(34.4508, -117.3106));
            graphic = factory.createGraphic("GFMPNB--------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Biologically Contaminated Area (2.X.3.4.5)");
            layer.addRenderable(graphic);

            graphic = factory.createPoint("GFMPNEB-------X", Position.fromDegrees(34.5059, -117.2020), null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Biological Release Event (2.X.3.4.7.1)");
            graphic.setShowLocation(false);
            layer.addRenderable(graphic);

            // Apply shared attributes to all graphics on this layer
            this.setAttributes(layer, this.sharedAttrs, this.sharedPointAttrs);

            //////////////////////////////////////////////////////////////
            // Battle Position, Prepared but not occupied (2.X.2.4.3.1.1)
            //////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.9684, -116.4864),
                Position.fromDegrees(34.9776, -116.6114),
                Position.fromDegrees(35.0271, -116.5614),
                Position.fromDegrees(35.0309, -116.4132));

            graphic = factory.createGraphic("GHGPDAB----J--X", positions, null);
            graphic.setText("Blue");
            graphic.setValue(AVKey.DISPLAY_NAME, "Battle Position, Prepared but not Occupied (2.X.2.4.3.1.1)");
            layer.addRenderable(graphic);

            //////////////////////////////////////////////////////////////
            // Encirclement (2.X.2.6.2.3)
            //////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.8166, -116.5491),
                Position.fromDegrees(34.8121, -116.5027),
                Position.fromDegrees(34.8182, -116.4528),
                Position.fromDegrees(34.8489, -116.4137),
                Position.fromDegrees(34.8848, -116.4246),
                Position.fromDegrees(34.9050, -116.4664),
                Position.fromDegrees(34.9080, -116.5246),
                Position.fromDegrees(34.8893, -116.5828),
                Position.fromDegrees(34.8495, -116.6091),
                Position.fromDegrees(34.8211, -116.5782));

            graphic = factory.createGraphic("GHGPSAE-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Encirclement (2.X.2.6.2.3)");
            layer.addRenderable(graphic);

            //////////////////////////////////////////////////////////////
            // Dose Rate Contour Line (2.X.3.4.9)
            //////////////////////////////////////////////////////////////

            positions = Arrays.asList(
                Position.fromDegrees(34.7710, -116.4909),
                Position.fromDegrees(34.7764, -116.4720),
                Position.fromDegrees(34.7790, -116.4210),
                Position.fromDegrees(34.7509, -116.3918),
                Position.fromDegrees(34.7285, -116.3810),
                Position.fromDegrees(34.7066, -116.3831),
                Position.fromDegrees(34.6787, -116.4378),
                Position.fromDegrees(34.6850, -116.4900),
                Position.fromDegrees(34.7063, -116.5192),
                Position.fromDegrees(34.7330, -116.5170));

            graphic = factory.createGraphic("GFMPNL--------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Dose Rate Contour Line (2.X.3.4.9)");
            graphic.setText("100 cGY");
            layer.addRenderable(graphic);

            // Create a second contour line inside the first.
            positions = Arrays.asList(
                Position.fromDegrees(34.7504, -116.4800),
                Position.fromDegrees(34.7552, -116.4637),
                Position.fromDegrees(34.7556, -116.4283),
                Position.fromDegrees(34.7469, -116.4137),
                Position.fromDegrees(34.7259, -116.4129),
                Position.fromDegrees(34.7091, -116.4138),
                Position.fromDegrees(34.7053, -116.4280),
                Position.fromDegrees(34.7073, -116.4634),
                Position.fromDegrees(34.7149, -116.4891),
                Position.fromDegrees(34.7314, -116.4908));

            graphic = factory.createGraphic("GFMPNL--------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Dose Rate Contour Line (2.X.3.4.9)");
            graphic.setText("300 cGY");
            layer.addRenderable(graphic);

            //////////////////////////////////////////////////////////////
            // Limited Access Area (2.X.2.1.3.10)
            //////////////////////////////////////////////////////////////

            // Create the area part of the graphic. This can be any type of area graphic. Here we use a General Area.
            positions = Arrays.asList(
                Position.fromDegrees(35.1421, -116.5798),
                Position.fromDegrees(35.0979, -116.5971),
                Position.fromDegrees(35.0934, -116.4595),
                Position.fromDegrees(35.1570, -116.4832));
            graphic = factory.createGraphic("GFGPGAG-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "General Area (2.X.2.1.3.1)");
            graphic.setText("CHEM");
            layer.addRenderable(graphic);

            // Now create a pentagon associated with the area. The first position specifies the position of the pentagon,
            // and the second position specifies the end point of the line that connects the pentagon to the area.
            positions = Arrays.asList(
                Position.fromDegrees(35.1612, -116.4304),
                Position.fromDegrees(35.1358, -116.4955));

            graphic = factory.createGraphic("GFGPGAY-------X", positions, null);
            graphic.setValue(AVKey.DISPLAY_NAME, "Limited Access Area (2.X.2.1.3.10)");
            graphic.setModifier(SymbologyConstants.SYMBOL_INDICATOR, "SFGPUCI--------");
            layer.addRenderable(graphic);

            // Apply shared attributes to all graphics on this layer
            this.setAttributes(layer, this.sharedAttrs, this.sharedPointAttrs);
        }

        /**
         * Apply attributes to all TacticalGraphics on a layer.
         *
         * @param layer      Layer of graphics to modify.
         * @param attrs      Attributes to apply to non-point graphics.
         * @param pointAttrs Attributes to apply to TacticalPoint graphics graphics.
         */
        protected void setAttributes(RenderableLayer layer, TacticalGraphicAttributes attrs,
            TacticalGraphicAttributes pointAttrs)
        {
            for (Renderable renderable : layer.getRenderables())
            {
                if (renderable instanceof TacticalPoint)
                {
                    // Apply attributes if the graphic does not already have attributes.
                    TacticalGraphic graphic = (TacticalGraphic) renderable;
                    if (graphic.getAttributes() == null)
                    {
                        graphic.setAttributes(pointAttrs);
                    }
                }
                else if (renderable instanceof TacticalGraphic)
                {
                    // Apply attributes if the graphic does not already have attributes.
                    TacticalGraphic graphic = (TacticalGraphic) renderable;
                    if (graphic.getAttributes() == null)
                    {
                        graphic.setAttributes(attrs);
                    }
                }
            }
        }

        protected void addGraphicControls()
        {
            javax.swing.Box box = javax.swing.Box.createVerticalBox();
            box.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Create a slider that controls the opacity of all symbols.
            JLabel label = new JLabel("Opacity");
            JSlider slider = new JSlider(0, 100, 100);
            slider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    // Set the opacity for only the normal attributes. This causes symbols to return to 100% opacity
                    // when highlighted. Changes in these attributes are reflected in all symbols that use them.
                    JSlider slider = (JSlider) changeEvent.getSource();
                    double opacity = (double) slider.getValue() / 100d;

                    sharedAttrs.setInteriorOpacity(opacity);
                    sharedAttrs.setOutlineOpacity(opacity);

                    sharedPointAttrs.setInteriorOpacity(opacity);
                    sharedPointAttrs.setOutlineOpacity(opacity);

                    getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                }
            });
            box.add(javax.swing.Box.createVerticalStrut(10));
            label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            slider.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(label);
            box.add(slider);

            // Create a check box that toggles the visibility of text modifiers for all symbols.
            JCheckBox cb = new JCheckBox("Text Modifiers", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    this.setShowModifiers(pointLayer, tf);
                    this.setShowModifiers(lineLayer, tf);
                    this.setShowModifiers(areaLayer, tf);

                    getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                }

                protected void setShowModifiers(RenderableLayer layer, boolean show)
                {
                    for (Renderable r : layer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowTextModifiers(show);
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of graphic modifiers for all symbols.
            cb = new JCheckBox("Graphic Modifiers", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    this.setShowModifiers(pointLayer, tf);
                    this.setShowModifiers(lineLayer, tf);
                    this.setShowModifiers(areaLayer, tf);

                    getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                }

                protected void setShowModifiers(RenderableLayer layer, boolean show)
                {
                    for (Renderable r : layer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowGraphicModifiers(show);
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            // Create a check box that toggles the visibility of text and graphic modifiers for all symbols.
            cb = new JCheckBox("Hostile indicator", true);
            cb.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    boolean tf = ((JCheckBox) actionEvent.getSource()).isSelected();

                    this.setShowHostile(pointLayer, tf);
                    this.setShowHostile(lineLayer, tf);
                    this.setShowHostile(areaLayer, tf);

                    getWwd().redraw(); // Cause the WorldWindow to refresh in order to make these changes visible.
                }

                protected void setShowHostile(RenderableLayer layer, boolean show)
                {
                    for (Renderable r : layer.getRenderables())
                    {
                        if (r instanceof TacticalGraphic)
                            ((TacticalGraphic) r).setShowHostileIndicator(show);
                    }
                }
            });
            cb.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            box.add(javax.swing.Box.createVerticalStrut(10));
            box.add(cb);

            this.getControlPanel().add(box, BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 34.90);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -117.44);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 155000);

        ApplicationTemplate.start("WorldWind Tactical Graphics", AppFrame.class);
    }
}
