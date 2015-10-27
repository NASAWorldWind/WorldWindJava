/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.BasicQuadTree;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.util.HashSet;

/**
 * Demonstrate use of {@link gov.nasa.worldwind.util.BasicQuadTree} to limit which markers in a collection are displayed
 * to those in a specific region.
 *
 * @author tag
 * @version $Id: TreeFiltering.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class TreeFiltering extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame() throws IOException, ParserConfigurationException, SAXException
        {
            super(true, true, false);

            final MyMarkerLayer layer = new MyMarkerLayer(this.makeDatabase());
            layer.setKeepSeparated(false);
            layer.setPickEnabled(true);
            insertBeforePlacenames(this.getWwd(), layer);

            this.getWwd().addPositionListener(new PositionListener()
            {
                public void moved(PositionEvent event)
                {
                    layer.setCursorLocation(event.getPosition());
                }
            });
        }

        private BasicQuadTree<Marker> makeDatabase()
        {
            int treeDepth = 5;
            int minLat = 23, maxLat = 50, latDelta = 3;
            int minLon = -130, maxLon = -70, lonDelta = 3;
            BasicQuadTree<Marker> tree = new BasicQuadTree<Marker>(treeDepth, Sector.FULL_SPHERE, null);

            MarkerAttributes attrs = new BasicMarkerAttributes();

            for (int lat = minLat; lat <= maxLat; lat += latDelta)
            {
                for (int lon = minLon; lon <= maxLon; lon += lonDelta)
                {
                    tree.add(new BasicMarker(Position.fromDegrees(lat, lon, 0), attrs),
                        new double[] {(double) lat, (double) lon}, null);
                }
            }

            return tree;
        }
    }

    private static class MyMarkerLayer extends MarkerLayer
    {
        private static final double[] REGION_SIZES = new double[] {5, 2};
        private static final long TIME_LIMIT = 5; // ms

        private BasicQuadTree<Marker> database;
        private Position position;
        private Iterable<Marker> markers;

        public MyMarkerLayer(BasicQuadTree<Marker> database)
        {
            this.database = database;
            this.setOverrideMarkerElevation(true);
            this.setKeepSeparated(false);
        }

        public void setCursorLocation(Position position)
        {
            this.position = position;
        }

        protected void draw(DrawContext dc, Point pickPoint)
        {
            if (this.position == null)
                return;

            // Refresh the visibility tree only during the pick pass, or the display pass if picking is disabled
            if (!this.isPickEnabled() || dc.isPickingMode() || this.markers == null)
                this.markers = this.getVisibleMarkers(dc);

            this.setMarkers(this.markers);
            super.draw(dc, pickPoint);
        }

        private Iterable<Marker> getVisibleMarkers(DrawContext dc)
        {
            HashSet<Marker> markers = new HashSet<Marker>();
            for (Sector sector : dc.getVisibleSectors(REGION_SIZES, TIME_LIMIT, this.computeSector()))
            {
                this.database.getItemsInRegion(sector, markers);
            }

            return markers;
        }

        private Sector computeSector()
        {
            double size = 5;
            double lat = this.position.getLatitude().degrees;
            double lon = this.position.getLongitude().degrees;
            double minLat = Math.max(lat - size, -90);
            double maxLat = Math.min(lat + size, 90);
            double minLon = Math.max(lon - size, -180);
            double maxLon = Math.min(lon + size, 180);

            return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Filtering by Region", AppFrame.class);
    }
}
