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

package gov.nasa.worldwind.geom;

import java.awt.*;

/**
 * @author tag
 * @version $Id: BarycentricTriangle.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BarycentricTriangle implements BarycentricPlanarShape
{
    // TODO: arg checking
    // TODO: account for degenerate quads
    protected Vec4 p00;
    protected Vec4 p10;
    protected Vec4 p01;

    protected Vec4 q1;
    protected Vec4 q3;

    public BarycentricTriangle(Vec4 p00, Vec4 p10, Vec4 p01)
    {
        this.p00 = p00;
        this.p10 = p10;
        this.p01 = p01;

        q1 = p10.subtract3(p00);
        q3 = p01.subtract3(p00);
    }

    public BarycentricTriangle(LatLon p00, LatLon p10, LatLon p01)
    {
        this.p00 = new Vec4(p00.getLongitude().getRadians(), p00.getLatitude().getRadians(), 0);
        this.p10 = new Vec4(p01.getLongitude().getRadians(), p01.getLatitude().getRadians(), 0);
        this.p01 = new Vec4(p10.getLongitude().getRadians(), p10.getLatitude().getRadians(), 0);

        q1 = this.p10.subtract3(this.p00);
        q3 = this.p01.subtract3(this.p00);
    }

    public BarycentricTriangle(Point p00, Point p10, Point p01)
    {
        this.p00 = new Vec4(p00.x, p00.y, 0);
        this.p10 = new Vec4(p10.x, p10.y, 0);
        this.p01 = new Vec4(p01.x, p01.y, 0);

        q1 = this.p10.subtract3(this.p00);
        q3 = this.p01.subtract3(this.p00);
    }

    public Vec4 getP00()
    {
        return p00;
    }

    public Vec4 getP10()
    {
        return p10;
    }

    public Vec4 getP01()
    {
        return p01;
    }

    public double[] getBarycentricCoords(Vec4 p)
    {
        Vec4 n = this.q1.cross3(this.q3);
        Vec4 na = n.getAbs3();
        Vec4 q2 = p.subtract3(this.p00);

        double a, b;

        // Choose equations providing best numerical accuracy
        if (na.x >= na.y && na.x >= na.z)
        {
            a = (q2.y * q3.z - q2.z * q3.y) / n.x;
            b = (q1.y * q2.z - q1.z * q2.y) / n.y;
        }
        else if (na.y >= na.x && na.y >= na.z)
        {
            a = (q2.z * q3.x - q2.x * q3.z) / n.y;
            b = (q1.z * q2.x - q1.x * q2.z) / n.y;
        }
        else
        {
            a = (q2.x * q3.y - q2.y * q3.x) / n.z;
            b = (q1.x * q2.y - q1.y * q2.x) / n.z;
        }

        return new double[] {1 - a - b, a, b};
    }

    public double[] getBarycentricCoords(LatLon location)
    {
        return this.getBarycentricCoords(new Vec4(location.getLongitude().radians, location.getLatitude().radians, 0));
    }

    public boolean contains(Vec4 p)
    {
        return this.getBarycentricCoords(p)[0] >= 0;
    }

    public Vec4 getPoint(double[] w)
    {
        Vec4 pa = this.p00.multiply3(w[0]);
        Vec4 pb = this.p10.multiply3(w[1]);
        Vec4 pc = this.p01.multiply3(w[2]);

        return pa.add3(pb).add3(pc);
    }

    public LatLon getLocation(double[] w)
    {
        Vec4 p = this.getPoint(w);

        return LatLon.fromRadians(p.y, p.x);
    }

    public double[] getBilinearCoords(double alpha, double beta)
    {
        return new double[] {alpha, beta};
    }
}
