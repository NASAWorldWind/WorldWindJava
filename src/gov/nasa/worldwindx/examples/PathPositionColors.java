/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.examples.util.ToolTipController;

import java.awt.*;
import java.util.ArrayList;

/**
 * Illustrates usage of the per-position color feature of {@link Path}. Path's per-position colors may be assigned in
 * any manner the application chooses. This example illustrates only one way of assigning color to each path position.
 * <p>
 * Also illustrates the use of the "show positions" feature of {@link Path}.
 *
 * @author dcollins
 * @version $Id: PathPositionColors.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class PathPositionColors extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(47.58495213398363, -122.31517238581496, 0));
            pathPositions.add(Position.fromDegrees(47.58557392961059, -122.31038095541774, 0));
            pathPositions.add(Position.fromDegrees(47.585643343225826, -122.30380786403879, 0));
            pathPositions.add(Position.fromDegrees(47.588442264013494, -122.30292363044828, 0));
            pathPositions.add(Position.fromDegrees(47.59082509462611, -122.30354883680081, 0));
            pathPositions.add(Position.fromDegrees(47.592022012752125, -122.30513978083029, 0));
            pathPositions.add(Position.fromDegrees(47.59253456911678, -122.30947924950297, 0));
            pathPositions.add(Position.fromDegrees(47.592351544411116, -122.31370241373538, 0));
            pathPositions.add(Position.fromDegrees(47.59215114879707, -122.31893777293358, 0));
            pathPositions.add(Position.fromDegrees(47.591633551472405, -122.32215105706649, 0));
            pathPositions.add(Position.fromDegrees(47.59107338189763, -122.32440174494027, 0));
            pathPositions.add(Position.fromDegrees(47.58935250213581, -122.32511028595225, 0));
            pathPositions.add(Position.fromDegrees(47.585577065954475, -122.32436748847293, 0));
            pathPositions.add(Position.fromDegrees(47.58476284067178, -122.32277296412886, 0));
            pathPositions.add(Position.fromDegrees(47.584304450759085, -122.32186049274797, 0));

            // Create a path with the specified positions that follows the terrain and draws a point at each position.
            Path path = new Path(pathPositions);
            path.setSurfacePath(true);
            path.setShowPositions(true);
            path.setShowPositionsScale(3);

            // Create and set an attribute bundle. Specify only the path's outline width; the position colors override
            // the outline color and opacity.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineWidth(3);
            path.setAttributes(attrs);

            // Configure the path to draw its outline and position points in the colors below. We use three colors that
            // are evenly distributed along the path's length and gradually increasing in opacity. Position colors may
            // be assigned in any manner the application chooses. This example illustrates only one way of assigning
            // color to each path position.
            Color[] colors =
                {
                    new Color(1f, 0f, 0f, 0.2f),
                    new Color(0f, 1f, 0f, 0.6f),
                    new Color(0f, 0f, 1f, 1.0f),
                };
            path.setPositionColors(new ExamplePositionColors(colors, pathPositions.size()));

            // Create a layer on which to display the path.
            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Establish a select listener that causes the tooltip controller to show the picked path position's
            // ordinal.
            this.setToolTipController(new ToolTipController(this.getWwd())
            {
                @Override
                public void selected(SelectEvent event)
                {
                    // Intercept the selected position and assign the selected object's display name to the picked
                    // ordinal.
                    PickedObject po = event.getTopPickedObject();
                    if (po != null && po.getObject() instanceof Path)
                    {
                        String name = (po.getValue(AVKey.ORDINAL) != null) ? "Position " + po.getValue(AVKey.ORDINAL)
                            : null;
                        ((Path) po.getObject()).setValue(AVKey.DISPLAY_NAME, name);
                    }

                    super.selected(event);
                }
            });
        }
    }

    /**
     * Example implementation of {@link gov.nasa.worldwind.render.Path.PositionColors} that evenly distributes the
     * specified colors along a path with the specified length. For example, if the Colors array contains red, green,
     * blue (in that order) and the pathLength is 6, this assigns the following colors to each path ordinal: 0:red,
     * 1:red, 2:green, 3:green, 4:blue, 5:blue.
     */
    public static class ExamplePositionColors implements Path.PositionColors
    {
        protected Color[] colors;
        protected int pathLength;

        public ExamplePositionColors(Color[] colors, int pathLength)
        {
            this.colors = colors;
            this.pathLength = pathLength;
        }

        public Color getColor(Position position, int ordinal)
        {
            int index = colors.length * ordinal / this.pathLength;
            return this.colors[index];
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 47.5890);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.3137);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 3000);

        ApplicationTemplate.start("WorldWind Path Position Colors", AppFrame.class);
    }
}
