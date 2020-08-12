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

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: BilinearInterpolator.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BilinearInterpolator
{
    private Vec4 ll;
    private Vec4 lr;
    private Vec4 ur;
    private Vec4 ul;

    public BilinearInterpolator(Vec4 ll, Vec4 lr, Vec4 ur, Vec4 ul)
    {
        if (ll == null || lr == null || ur == null || ul == null)
        {
            String message = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.ll = ll;
        this.lr = lr;
        this.ur = ur;
        this.ul = ul;
    }

    public Vec4[] getCorners()
    {
        return new Vec4[] {this.ll, this.lr, this.ur, this.ul};
    }

    public void interpolate(double u, double v, double[] compArray)
    {
        if (compArray == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (compArray.length < 1)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double pll = (1.0 - u) * (1.0 - v);
        double plr = u         * (1.0 - v);
        double pur = u         * v;
        double pul = (1.0 - u) * v;

        compArray[0] = (pll * ll.x) + (plr * lr.x) + (pur * ur.x) + (pul * ul.x);
        compArray[1] = (pll * ll.y) + (plr * lr.y) + (pur * ur.y) + (pul * ul.y);
        compArray[2] = (pll * ll.z) + (plr * lr.z) + (pur * ur.z) + (pul * ul.z);
        compArray[3] = (pll * ll.w) + (plr * lr.w) + (pur * ur.w) + (pul * ul.w);
    }

    public Vec4 interpolateAsPoint(double u, double v)
    {
        double[] compArray = new double[4];
        this.interpolate(u, v, compArray);
        
        return Vec4.fromArray4(compArray, 0);
    }
}
