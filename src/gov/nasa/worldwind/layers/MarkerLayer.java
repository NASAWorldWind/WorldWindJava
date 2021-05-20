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

package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.*;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: MarkerLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MarkerLayer extends AbstractLayer
{
    private MarkerRenderer markerRenderer = new MarkerRenderer();
    private Iterable<Marker> markers;

    public MarkerLayer()
    {
    }

    public MarkerLayer(Iterable<Marker> markers)
    {
        this.markers = markers;
    }

    public Iterable<Marker> getMarkers()
    {
        return markers;
    }

    public void setMarkers(Iterable<Marker> markers)
    {
        this.markers = markers;
    }

    public double getElevation()
    {
        return this.getMarkerRenderer().getElevation();
    }

    public void setElevation(double elevation)
    {
        this.getMarkerRenderer().setElevation(elevation);
    }

    public boolean isOverrideMarkerElevation()
    {
        return this.getMarkerRenderer().isOverrideMarkerElevation();
    }

    public void setOverrideMarkerElevation(boolean overrideMarkerElevation)
    {
        this.getMarkerRenderer().setOverrideMarkerElevation(overrideMarkerElevation);
    }

    public boolean isKeepSeparated()
    {
        return this.getMarkerRenderer().isKeepSeparated();
    }

    public void setKeepSeparated(boolean keepSeparated)
    {
        this.getMarkerRenderer().setKeepSeparated(keepSeparated);
    }

    public boolean isEnablePickSizeReturn()
    {
        return this.getMarkerRenderer().isEnablePickSizeReturn();
    }

    public void setEnablePickSizeReturn(boolean enablePickSizeReturn)
    {
        this.getMarkerRenderer().setEnablePickSizeReturn(enablePickSizeReturn);
    }

    /**
     * Opacity is not applied to layers of this type because each marker has an attribute set with opacity control.
     *
     * @param opacity the current opacity value, which is ignored by this layer.
     */
    @Override
    public void setOpacity(double opacity)
    {
        super.setOpacity(opacity);
    }

    /**
     * Returns the layer's opacity value, which is ignored by this layer because each of its markers has an attribute
     * with its own opacity control.
     *
     * @return The layer opacity, a value between 0 and 1.
     */
    @Override
    public double getOpacity()
    {
        return super.getOpacity();
    }

    protected MarkerRenderer getMarkerRenderer()
    {
        return markerRenderer;
    }

    protected void setMarkerRenderer(MarkerRenderer markerRenderer)
    {
        this.markerRenderer = markerRenderer;
    }

    protected void doRender(DrawContext dc)
    {
        this.draw(dc, null);
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.draw(dc, pickPoint);
    }

    protected void draw(DrawContext dc, java.awt.Point pickPoint)
    {
        if (this.markers == null)
            return;

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return;

        // Adds markers to the draw context's ordered renderable queue. During picking, this gets the pick point and the
        // current layer from the draw context.
        this.getMarkerRenderer().render(dc, this.markers);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.MarkerLayer.Name");
    }
}
