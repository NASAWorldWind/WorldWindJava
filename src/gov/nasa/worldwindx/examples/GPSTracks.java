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
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.gpx.GpxReader;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.util.WWIO;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * Demonstrates displaying GPS tracks using markers. When the view is far away from the data, only a few markers are
 * displayed. As the view zooms in, more markers appear to fill in the track data. When the mouse hovers over a marker
 * the coordinates of that point will be printed to stdout.
 *
 * @author tag
 * @version $Id: GPSTracks.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class GPSTracks extends ApplicationTemplate
{
    protected static final String TRACK_PATH = "gov/nasa/worldwindx/examples/data/tuolumne.gpx";

    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            MarkerLayer layer = this.buildTracksLayer();
            insertBeforeCompass(this.getWwd(), layer);

            this.getWwd().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getTopObject() != null)
                    {
                        if (event.getTopPickedObject().getParentLayer() instanceof MarkerLayer)
                        {
                            PickedObject po = event.getTopPickedObject();
                            //noinspection RedundantCast
                            System.out.printf("Track position %s, %s, size = %f\n",
                                po.getValue(AVKey.PICKED_OBJECT_ID).toString(),
                                po.getPosition(), (Double) po.getValue(AVKey.PICKED_OBJECT_SIZE));
                        }
                    }
                }
            });
        }

        protected MarkerLayer buildTracksLayer()
        {
            try
            {
                GpxReader reader = new GpxReader();
                reader.readStream(WWIO.openFileOrResourceStream(TRACK_PATH, this.getClass()));
                Iterator<Position> positions = reader.getTrackPositionIterator();

                BasicMarkerAttributes attrs =
                    new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.SPHERE, 1d);

                ArrayList<Marker> markers = new ArrayList<Marker>();
                while (positions.hasNext())
                {
                    markers.add(new BasicMarker(positions.next(), attrs));
                }

                MarkerLayer layer = new MarkerLayer(markers);
                layer.setOverrideMarkerElevation(true);
                layer.setElevation(0);
                layer.setEnablePickSizeReturn(true);

                return layer;
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Tracks", AppFrame.class);
    }
}
