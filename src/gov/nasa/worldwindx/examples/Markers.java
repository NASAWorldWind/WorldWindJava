/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.event.SelectEvent;

import java.util.ArrayList;

/**
 * Displays markers (small shapes) in different shapes and colors. Markers can be configured to have an orientation,
 * represented by a line or triangle pointing in the direction of the marker heading. Markers are commonly used to
 * represent track data, for example from a GPS device. The marker orientation can be used to indicate the device
 * heading at a given track point.
 *
 * @author tag
 * @version $Id: Markers.java 2280 2014-08-29 21:45:02Z tgaskins $
 */
public class Markers extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private static final MarkerAttributes[] attrs = new BasicMarkerAttributes[]
            {
                new BasicMarkerAttributes(Material.BLACK, BasicMarkerShape.SPHERE, 1d, 10, 5),
                new BasicMarkerAttributes(Material.MAGENTA, BasicMarkerShape.CUBE, 1d, 10, 5),
                new BasicMarkerAttributes(Material.YELLOW, BasicMarkerShape.CONE, 1d, 10, 5),
                new BasicMarkerAttributes(Material.LIGHT_GRAY, BasicMarkerShape.CYLINDER, 1d, 10, 5),
                new BasicMarkerAttributes(Material.GRAY, BasicMarkerShape.HEADING_ARROW, 1d, 10, 5),
                new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.HEADING_LINE, 1d, 10, 5),
                new BasicMarkerAttributes(Material.RED, BasicMarkerShape.ORIENTED_CONE_LINE, 0.7),
                new BasicMarkerAttributes(Material.YELLOW, BasicMarkerShape.ORIENTED_CYLINDER_LINE, 0.9),
                new BasicMarkerAttributes(Material.CYAN, BasicMarkerShape.ORIENTED_SPHERE_LINE, 0.7),
                new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.ORIENTED_CONE, 1d),
                new BasicMarkerAttributes(Material.PINK, BasicMarkerShape.ORIENTED_SPHERE, 0.8),
                new BasicMarkerAttributes(Material.BLUE, BasicMarkerShape.ORIENTED_CYLINDER, 0.6),
                new BasicMarkerAttributes(Material.RED, BasicMarkerShape.ORIENTED_CUBE, 1d)
            };

        static
        {
            for (MarkerAttributes attr : attrs)
            {
                String shapeType = attr.getShapeType();
                //noinspection StringEquality
                if (shapeType == BasicMarkerShape.ORIENTED_SPHERE)
                    attr.setHeadingMaterial(Material.YELLOW);
                //noinspection StringEquality
                if (shapeType == BasicMarkerShape.ORIENTED_CONE)
                    attr.setHeadingMaterial(Material.PINK);
            }
        }

        private Marker lastHighlit;
        private BasicMarkerAttributes lastAttrs;

        public AppFrame()
        {
            super(true, true, false);

            double minLat = -60, maxLat = 60, latDelta = 2;
            double minLon = -179, maxLon = 180, lonDelta = 10;

            int i = 0;
            ArrayList<Marker> markers = new ArrayList<Marker>();
            for (double lat = minLat; lat <= maxLat; lat += latDelta)
            {
                for (double lon = minLon; lon <= maxLon; lon += lonDelta)
                {
                    Marker marker = new BasicMarker(Position.fromDegrees(lat, lon, 0), attrs[i % attrs.length]);
                    marker.setPosition(Position.fromDegrees(lat, lon, 0));
                    marker.setHeading(Angle.fromDegrees(0));
                    marker.setPitch(Angle.fromDegrees(90));
                    markers.add(marker);
                    i++;
                }
            }

            Marker marker = new BasicMarker(Position.fromDegrees(0, 180, 0), attrs[2]);
            markers.add(marker);

            final MarkerLayer layer = new MarkerLayer();
            layer.setOverrideMarkerElevation(true);
            layer.setKeepSeparated(false);
            layer.setElevation(1000d);
            layer.setMarkers(markers);
            insertBeforePlacenames(this.getWwd(), layer);

            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (lastHighlit != null
                        && (event.getTopObject() == null || !event.getTopObject().equals(lastHighlit)))
                    {
                        lastHighlit.setAttributes(lastAttrs);
                        lastHighlit = null;
                    }

                    if (!event.getEventAction().equals(SelectEvent.ROLLOVER))
                        return;

                    if (event.getTopObject() == null || event.getTopPickedObject().getParentLayer() == null)
                        return;

                    if (event.getTopPickedObject().getParentLayer() != layer)
                        return;

                    if (lastHighlit == null && event.getTopObject() instanceof Marker)
                    {
                        lastHighlit = (Marker) event.getTopObject();
                        lastAttrs = (BasicMarkerAttributes) lastHighlit.getAttributes();
                        MarkerAttributes highliteAttrs = new BasicMarkerAttributes(lastAttrs);
                        highliteAttrs.setMaterial(Material.WHITE);
                        highliteAttrs.setOpacity(1d);
                        highliteAttrs.setMarkerPixels(lastAttrs.getMarkerPixels() * 1.4);
                        highliteAttrs.setMinMarkerSize(lastAttrs.getMinMarkerSize() * 1.4);
                        lastHighlit.setAttributes(highliteAttrs);
                    }
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Markers", AppFrame.class);
    }
}
