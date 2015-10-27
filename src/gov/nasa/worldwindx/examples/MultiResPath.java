/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.util.*;

/**
 * Illustrates use of the {@link MultiResolutionPath} shape, which adapts its complexity as the path's distance frome
 * the eye point changes.
 * <p/>
 * Also illustrates the use of the "show positions" feature of {@link Path}.
 *
 * @author tag
 * @version $Id: MultiResPath.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class MultiResPath extends ApplicationTemplate
{
    // Specify several Paths, each with large numbers of positions.
    protected static final int NUM_POSITIONS = 108000;
    protected static final double SPEED = 90 / Earth.WGS84_EQUATORIAL_RADIUS; // meters per second to radians
    protected static final Position[] ORIGIN = new Position[]
        {
            Position.fromDegrees(40.2377, -105.6480, 200),
            Position.fromDegrees(41.2625, -105.6503, 200),
            Position.fromDegrees(42.2285, -105.6169, 200),
            Position.fromDegrees(43.2019, -105.6467, 200),
            Position.fromDegrees(44.2414, -105.6911, 200),
        };

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            makeMany();
        }

        protected void makeMany()
        {
            for (Position pos : ORIGIN)
            {
                this.addShape(pos);
            }
        }

        protected void addShape(Position origin)
        {
            int altitudeMode = WorldWind.RELATIVE_TO_GROUND;

            LatLon location = new LatLon(origin);
            double altitude = 500;
            double angle = 0;
            double distance = 60 * SPEED / 20; // seconds x radians per second

            // Create the Path positions.
            List<Position> positions = new ArrayList<Position>(NUM_POSITIONS);
            for (int i = 0; i < NUM_POSITIONS; i++)
            {
                Position position = new Position(location, altitude);
                positions.add(position);

                altitude += 5;
                angle += Math.PI / 64;
                location = LatLon.rhumbEndPosition(location, angle, distance);
            }

            // Create attributes for the Path.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setDrawInterior(false);
            attrs.setOutlineMaterial(Material.RED);

            MultiResolutionPath path = new MultiResolutionPath(positions);

            // Indicate that dots are to be drawn at each specified path position.
            path.setShowPositions(true);

            // Indicate that the dots be drawn only when the path is less than 5 KM from the eye point.
            path.setShowPositionsThreshold(5e3);

            // Override generic view-distance geometry regeneration because multi-res Paths handle that themselves.
            path.setViewDistanceExpiration(false);

            path.setAltitudeMode(altitudeMode);
            path.setAttributes(attrs);

            RenderableLayer rLayer = new RenderableLayer();
            rLayer.addRenderable(path);
            insertBeforeCompass(getWwd(), rLayer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind UAVPath Test", AppFrame.class);
    }
}
