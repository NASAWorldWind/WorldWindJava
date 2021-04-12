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
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;

import com.jogamp.opengl.glu.*;

// TODO: Consider replacing the clipping capability in PolygonTessellator2 with use of this independent component.
// TODO: Consider clipping contour coordinates to the sector bounds, rather than just reducing complexity.

/**
 * ClippingTessellator
 *
 * @author dcollins
 * @version $Id: ClippingTessellator.java 2398 2014-10-28 17:14:41Z dcollins $
 */
public class ClippingTessellator
{
    protected GLUtessellator tessellator;
    protected double[] clipDegrees;
    protected double[] prevCoord = new double[2];
    protected int prevClipCode;

    public ClippingTessellator(GLUtessellator tessellator, Sector sector)
    {
        if (tessellator == null)
        {
            String msg = Logging.getMessage("nullValue.TessellatorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.tessellator = tessellator;
        this.clipDegrees = sector.asDegreesArray();
    }

    public void beginContour()
    {
        GLU.gluTessBeginContour(this.tessellator);
        this.prevClipCode = -1;
    }

    public void endContour()
    {
        GLU.gluTessEndContour(this.tessellator);
    }

    public void addVertex(double degreesLatitude, double degreesLongitude)
    {
        int code = this.clipCode(degreesLatitude, degreesLongitude);

        if (this.prevClipCode > 0 && code != this.prevClipCode)
        {
            this.doAddVertex(this.prevCoord[0], prevCoord[1]);
        }

        if (code == 0 || code != this.prevClipCode)
        {
            this.doAddVertex(degreesLatitude, degreesLongitude);
        }

        this.prevCoord[0] = degreesLatitude;
        this.prevCoord[1] = degreesLongitude;
        this.prevClipCode = code; // copy the current clip code to the previous clip code
    }

    protected void doAddVertex(double degreesLatitude, double degreesLongitude)
    {
        double[] vertex = {degreesLongitude, degreesLatitude, 0}; // lon,lat -> x,y,0
        GLU.gluTessVertex(this.tessellator, vertex, 0, vertex);
    }

    /**
     * Computes a 4-bit code indicating the vertex's location in the 9 cell grid defined by the clip coordinates and the
     * eight adjacent spaces defined by extending the min/max boundaries to infinity.0 indicates that the vertex is
     * inside the clip coordinates.
     *
     * @param degreesLatitude The latitude for computation.
     * @param degreesLongitude The longitude for computation.
     * @return The vertex location code.
     */
    protected int clipCode(double degreesLatitude, double degreesLongitude)
    {
        int code = 0;
        code |= (degreesLatitude < this.clipDegrees[0] ? 0x0001 : 0x0); // minLat
        code |= (degreesLatitude > this.clipDegrees[1] ? 0x0010 : 0x0); // maxLat
        code |= (degreesLongitude < this.clipDegrees[2] ? 0x0100 : 0x0); // minLon
        code |= (degreesLongitude > this.clipDegrees[3] ? 0x1000 : 0x0); // maxLon

        return code;
    }
}
