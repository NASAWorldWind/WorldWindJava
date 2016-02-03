/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
     * eight adjacent spaces defined by extending the min/max boundaries to infinity. 0 indicates that the vertex is
     * inside the clip coordinates.
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
