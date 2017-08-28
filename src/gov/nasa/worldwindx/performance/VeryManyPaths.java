/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.performance;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * @author tag
 * @version $Id: VeryManyPaths.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class VeryManyPaths extends ApplicationTemplate
{
    protected static final int NUM_PATHS = 2000;
    protected static final int NUM_POSITIONS = 300;
    protected static final Angle PATH_LENGTH = Angle.fromDegrees(5);
    protected static final double PATH_HEIGHT = 1e3;
    protected static final LatLon START_LOCATION = LatLon.fromDegrees(48.86, 2.33);
    protected static final int ALTITUDE_MODE = WorldWind.ABSOLUTE;
    protected static final double LINE_WIDTH = 1d;

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected List<Path> paths;

        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();
            
            this.makePaths(layer, new Position(START_LOCATION, PATH_HEIGHT), NUM_PATHS, PATH_LENGTH, NUM_POSITIONS);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Update layer panel
            this.getWwd().getView().setEyePosition(new Position(START_LOCATION, 3e6));
        }

        protected void makePaths(RenderableLayer layer, Position origin, int numPaths, Angle length, int numPositions)
        {
            double dAngle = 360d / numPaths;

            for (int i = 0; i < numPaths; i++)
            {
                Angle heading = Angle.fromDegrees(i * dAngle);
                layer.addRenderable(this.makePath(origin, heading, length, numPositions));
            }

            System.out.printf("%d paths, each with %d positions\n", NUM_PATHS, NUM_POSITIONS);
        }

        protected Path makePath(Position startPosition, Angle heading, Angle length, int numPositions)
        {
            double dLength = length.radians / (numPositions - 1);
            List<Position> positions = new ArrayList<Position>(numPositions);

            for (int i = 0; i < numPositions - 1; i++)
            {
                LatLon ll = Position.greatCircleEndPosition(startPosition, heading, Angle.fromRadians(i * dLength));
                positions.add(new Position(ll, PATH_HEIGHT));
            }

            LatLon ll = Position.greatCircleEndPosition(startPosition, heading, length);
            positions.add(new Position(ll, PATH_HEIGHT));

            Path path = new Path(positions);
            path.setAltitudeMode(ALTITUDE_MODE);
            path.setExtrude(true);
            path.setDrawVerticals(true);

            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setInteriorMaterial(attrs.getOutlineMaterial());
            attrs.setInteriorOpacity(0.5);
            attrs.setDrawOutline(true);
            attrs.setDrawInterior(true);
            attrs.setOutlineWidth(LINE_WIDTH);
            path.setAttributes(attrs);

            return path;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Paths", AppFrame.class);
    }
}
