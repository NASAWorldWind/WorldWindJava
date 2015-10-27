/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: PathsOnDateline.java 2189 2014-07-30 19:25:51Z tgaskins $
 */
public class PathsOnDateline extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setOutlineWidth(2d);

            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(28, 170, 1e4));
            pathPositions.add(Position.fromDegrees(35, -179, 1e4));
            pathPositions.add(Position.fromDegrees(38, 180, 1e4));
            pathPositions.add(Position.fromDegrees(35, -170, 1e4));
            pathPositions.add(Position.fromDegrees(30, -170, 1e4));
            pathPositions.add(Position.fromDegrees(32, -180, 1e4));
            pathPositions.add(Position.fromDegrees(30, 170, 1e4));
            Path path = new Path(pathPositions);
            path.setAttributes(attrs);
            BasicShapeAttributes highlightAttrs = new BasicShapeAttributes(attrs);
            highlightAttrs.setOutlineMaterial(Material.WHITE);
            path.setHighlightAttributes(highlightAttrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            path.setShowPositions(true);
            path.setShowPositionsThreshold(1e12);

            // Configure the path to draw its outline and position points in the colors below. We use three colors that
            // are evenly distributed along the path's length and gradually increasing in opacity. Position colors may
            // be assigned in any manner the application chooses. This example illustrates only one way of assigning
            // color to each path position.
            Color[] colors =
                {
                    new Color(1f, 0f, 0f, 0.4f),
                    new Color(0f, 1f, 0f, 0.6f),
                    new Color(0f, 0f, 1f, 1.0f),
                };
            path.setPositionColors(new ExamplePositionColors(colors, pathPositions.size()));

            layer.addRenderable(path);

            insertBeforeCompass(getWwd(), layer);

            this.getWwd().addSelectListener(new BasicDragger((this.getWwd())));
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
        ApplicationTemplate.start("Paths on Dateline", AppFrame.class);
    }
}
