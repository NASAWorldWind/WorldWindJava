/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWMath;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Example of how to draw parallel paths. This example specifies the positions of a control path, and then computes four
 * paths that run parallel to the control path.
 *
 * @author pabercrombie
 * @version $Id: ParallelPaths.java 617 2012-05-31 23:32:31Z tgaskins $
 */
public class ParallelPaths extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Create list of positions along the control line.
            List<Position> positions = Arrays.asList(
                Position.fromDegrees(49.0457, -122.8115, 100),
                Position.fromDegrees(49.0539, -122.8091, 110),
                Position.fromDegrees(49.0621, -122.7937, 120),
                Position.fromDegrees(49.0681, -122.8044, 130),
                Position.fromDegrees(49.0682, -122.7730, 140),
                Position.fromDegrees(49.0482, -122.7764, 150),
                Position.fromDegrees(49.0498, -122.7466, 140),
                Position.fromDegrees(49.0389, -122.7453, 130),
                Position.fromDegrees(49.0321, -122.7759, 120),
                Position.fromDegrees(49.0394, -122.7689, 110),
                Position.fromDegrees(49.0629, -122.7666, 100));

            // We will generate four paths parallel to the control path. Allocate lists to store the positions of these
            // paths.
            List<Position> pathPositions1 = new ArrayList<Position>();
            List<Position> pathPositions2 = new ArrayList<Position>();
            List<Position> pathPositions3 = new ArrayList<Position>();
            List<Position> pathPositions4 = new ArrayList<Position>();

            Globe globe = getWwd().getModel().getGlobe();

            // Generate two sets of lines parallel to the control line. The positions will be added to the pathPosition lists.
            WWMath.generateParallelLines(positions, pathPositions1, pathPositions2, 50, globe);
            WWMath.generateParallelLines(positions, pathPositions3, pathPositions4, 100, globe);

            // Create Path objects from the position lists, and add them to a layer.
            RenderableLayer layer = new RenderableLayer();
            this.addPath(layer, positions, "Control Path");
            this.addPath(layer, pathPositions1, "Path 1");
            this.addPath(layer, pathPositions2, "Path 2");
            this.addPath(layer, pathPositions3, "Path 3");
            this.addPath(layer, pathPositions4, "Path 4");

            insertBeforePlacenames(getWwd(), layer);
        }

        public static class ExamplePositionColors implements Path.PositionColors
        {
            public Color getColor(Position position, int ordinal)
            {
                // Color the positions based on their altitude.
                double altitude = position.getAltitude();
                return altitude < 115 ? Color.GREEN : altitude < 135 ? Color.BLUE : Color.RED;
            }
        }

        protected void addPath(RenderableLayer layer, List<Position> positions, String displayName)
        {
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineWidth(5);

            Path path = new Path(positions);
            path.setPathType(AVKey.LINEAR);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setAttributes(attrs);
            path.setValue(AVKey.DISPLAY_NAME, displayName);
            layer.addRenderable(path);

            // Show how to make the colors vary along the paths.
            path.setPositionColors(new ExamplePositionColors());
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 49.05);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.78);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 8000);

        ApplicationTemplate.start("World Wind Multi Path", AppFrame.class);
    }
}
