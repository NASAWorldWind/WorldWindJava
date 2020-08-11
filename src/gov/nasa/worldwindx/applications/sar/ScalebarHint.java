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
package gov.nasa.worldwindx.applications.sar;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.markers.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * Handles marker indicator for the scalebar.
 *
 * @author Patrick Murris
 * @version $Id: ScalebarHint.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ScalebarHint
{
    private WorldWindow wwd;
    private RenderableLayer layer = new RenderableLayer();
    private MarkerRenderer markerRenderer = new MarkerRenderer();
    private RenderableMarker marker;
    MarkerAttributes markerAttributes;
    private boolean enabled = true;

    public ScalebarHint()
    {
        this.layer.setName("Scalebar reference");
        this.layer.setEnabled(false);
        this.markerAttributes = new BasicMarkerAttributes(new Material(Color.YELLOW),
                        BasicMarkerShape.CONE, 1, 10, 5);
        this.marker = new RenderableMarker(Position.ZERO, this.markerAttributes);
        this.layer.addRenderable(this.marker);
    }

    public void setWwd(WorldWindow worldWindow)
    {
        this.wwd = worldWindow;
        // Enable picking on the scalebar layer
        for (Layer l : this.wwd.getModel().getLayers())
            if (l instanceof ScalebarLayer)
                l.setPickEnabled(true);
        // Add our layer
        this.wwd.getModel().getLayers().add(this.layer);
        
        // Add scalebar select listener to handle rollover
        this.wwd.addSelectListener(new SelectListener()
        {
            public void selected(SelectEvent event)
            {
                if (!enabled || event.getTopObject() == null || !(event.getTopObject() instanceof ScalebarLayer))
                {
                    layer.setEnabled(false);
                    return;
                }

                if (!event.getEventAction().equals(SelectEvent.ROLLOVER))
                    return;

                marker.setPosition(event.getTopPickedObject().getPosition());
                layer.setEnabled(true);
                wwd.redraw();
            }
        });
    }

    public MarkerAttributes getMarkerAttributes()
    {
        return this.markerAttributes;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean state)
    {
        this.enabled = state;
    }

    private class RenderableMarker extends BasicMarker implements Renderable
    {
        private ArrayList<Marker> markerList;

        public RenderableMarker(Position position, MarkerAttributes attrs)
        {
            super(position, attrs);
        }

        public void render(DrawContext dc)
        {
            if (this.markerList == null)
            {
                this.markerList = new ArrayList<Marker>();
                this.markerList.add(this);
            }
            markerRenderer.render(dc, this.markerList);
        }
    }
}
