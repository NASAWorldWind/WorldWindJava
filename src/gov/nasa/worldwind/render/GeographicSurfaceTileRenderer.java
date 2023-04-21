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
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.terrain.SectorGeometry;

import java.util.*;

/**
 * @author tag
 * @version $Id: GeographicSurfaceTileRenderer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeographicSurfaceTileRenderer extends SurfaceTileRenderer
{
    private double sgWidth;
    private double sgHeight;
    private double sgMinWE;
    private double sgMinSN;

    protected void preComputeTextureTransform(DrawContext dc, SectorGeometry sg, Transform t)
    {
        Sector st = sg.getSector();
        this.sgWidth = st.getDeltaLonRadians();
        this.sgHeight = st.getDeltaLatRadians();
        this.sgMinWE = st.getMinLongitude().radians;
        this.sgMinSN = st.getMinLatitude().radians;
    }

    protected void computeTextureTransform(DrawContext dc, SurfaceTile tile, Transform t)
    {
        Sector st = tile.getSector();
        double tileWidth = st.getDeltaLonRadians();
        double tileHeight = st.getDeltaLatRadians();
        double minLon = st.getMinLongitude().radians;
        double minLat = st.getMinLatitude().radians;

        t.VScale = tileHeight > 0 ? this.sgHeight / tileHeight : 1;
        t.HScale = tileWidth > 0 ? this.sgWidth / tileWidth : 1;
        t.VShift = -(minLat - this.sgMinSN) / this.sgHeight;
        t.HShift = -(minLon - this.sgMinWE) / this.sgWidth;
    }

    protected Iterable<SurfaceTile> getIntersectingTiles(DrawContext dc, SectorGeometry sg,
        Iterable<? extends SurfaceTile> tiles)
    {
        ArrayList<SurfaceTile> intersectingTiles = null;

        for (SurfaceTile tile : tiles)
        {
            if (!tile.getSector().intersectsInterior(sg.getSector()))
                continue;

            if (intersectingTiles == null) // lazy creation because most common case is no intersecting tiles
                intersectingTiles = new ArrayList<SurfaceTile>();

            intersectingTiles.add(tile);
        }

        return intersectingTiles; // will be null if no intersecting tiles
    }
}
