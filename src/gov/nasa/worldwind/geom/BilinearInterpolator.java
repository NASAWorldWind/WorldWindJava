/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
