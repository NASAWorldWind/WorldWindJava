/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.lineofsight;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ToolTipController;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;

import java.util.*;

/**
 * Draws a grid of points on the terrain. The points are evenly spaced throughout a region defined by a four sided
 * polygon.
 *
 * @author tag
 * @version $Id: GridOfPoints.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class GridOfPoints extends ApplicationTemplate
{
    protected static final int NUM_POINTS_WIDE = 500;
    protected static final int NUM_POINTS_HIGH = 500;

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected RenderableLayer layer; // layer to display the polygon and the intersection
        protected HashMap<Position, Object> positionInfo;

        public AppFrame()
        {
            super(true, true, false);

            // Create the grid and its positions. This one is rectangular; it need not be, but must be four-sided.
            List<Position> corners = new ArrayList<Position>();
            corners.add(Position.fromDegrees(35.0, -125.0, 10e3));
            corners.add(Position.fromDegrees(35.0, -115.0, 10e3));
            corners.add(Position.fromDegrees(45.0, -115.0, 10e3));
            corners.add(Position.fromDegrees(45.0, -125.0, 10e3));

            List<Position> positions = new ArrayList<Position>(NUM_POINTS_WIDE * NUM_POINTS_WIDE);

            // Create a hash map to store the data associated with each position.
            this.positionInfo = new HashMap<Position, Object>(NUM_POINTS_WIDE * NUM_POINTS_HIGH);

            // Populate the position list with positions and the positionInfo map with data.
            int aDataValue = 0;// generate an arbitrary data value
            PositionIterator posIter = new PositionIterator(corners, NUM_POINTS_WIDE, NUM_POINTS_HIGH);
            while (posIter.hasNext())
            {
                Position position = posIter.next();
                positions.add(position);
                this.positionInfo.put(position, aDataValue++);
            }

            // Create the PointGrid shape.
            PointGrid grid = new PointGrid(corners, positions, NUM_POINTS_WIDE * NUM_POINTS_HIGH);

            // Set its attributes.
            PointGrid.Attributes attrs = new PointGrid.Attributes();
            attrs.setPointSize(6d);
            grid.setAttributes(attrs);

            // Add the shape to the display layer.
            this.layer = new RenderableLayer();
            this.layer.addRenderable(grid);
            insertBeforeCompass(getWwd(), this.layer);

            // Establish a select listener that causes the tooltip controller to show the picked position's data value.
            this.setToolTipController(new ToolTipController(getWwd())
            {
                @Override
                public void selected(SelectEvent event)
                {
                    // Intercept the selected position and assign its display name the position's data value.
                    if (event.getTopObject() instanceof PointGrid)
                        ((PointGrid) event.getTopObject()).setValue(AVKey.DISPLAY_NAME,
                            positionInfo.get(event.getTopPickedObject().getPosition()).toString());

                    super.selected(event);
                }
            });
        }
    }

    /** Generates positions forming a lat/lon grid. */
    protected static class PositionIterator implements Iterator<Position>
    {
        protected int numWide = NUM_POINTS_WIDE;
        protected int numHigh = NUM_POINTS_HIGH;
        protected int w;
        protected int h;

        protected List<Position> corners;
        protected Position sw;
        protected Position se;
        protected Position ne;
        protected Position nw;

        public PositionIterator(List<Position> corners, int numPointsWide, int numPointsHigh)
        {
            this.corners = corners;
            this.sw = corners.get(0);
            this.se = corners.get(1);
            this.ne = corners.get(2);
            this.nw = corners.get(3);

            this.numWide = numPointsWide;
            this.numHigh = numPointsHigh;
        }

        public boolean hasNext()
        {
            return this.h < this.numHigh;
        }

        public Position next()
        {
            if (this.h >= this.numHigh)
                throw new NoSuchElementException("PointGridIterator");

            return this.computeNextPosition();
        }

        public void remove()
        {
            throw new UnsupportedOperationException("PointGridIterator");
        }

        protected Position computeNextPosition()
        {
            Position left, right;

            if (h == 0)
            {
                left = sw;
                right = se;
            }
            else if (h == numHigh - 1)
            {
                left = nw;
                right = ne;
            }
            else
            {
                double t = h / (double) (numHigh - 1);

                left = Position.interpolate(t, sw, nw);
                right = Position.interpolate(t, se, ne);
            }

            Position pos;

            if (w == 0)
            {
                pos = left;
                ++w;
            }
            else if (w == numWide - 1)
            {
                pos = right;

                w = ++h < numHigh ? 0 : w + 1;
            }
            else
            {
                double s = w / (double) (numWide - 1);
                pos = Position.interpolate(s, left, right);
                ++w;
            }

            return pos;
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the balloons are immediately visible.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40.5);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -120.4);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 2000e3);
        Configuration.setValue(AVKey.INITIAL_HEADING, 27);
        Configuration.setValue(AVKey.INITIAL_PITCH, 30);

        ApplicationTemplate.start("World Wind Point Grid", AppFrame.class);
    }
}
