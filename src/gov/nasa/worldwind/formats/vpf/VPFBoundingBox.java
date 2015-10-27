/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: VPFBoundingBox.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFBoundingBox
{
    private double xmin;
    private double ymin;
    private double xmax;
    private double ymax;

    public VPFBoundingBox(double xmin, double ymin, double xmax, double ymax)
    {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public double getXmin()
    {
        return this.xmin;
    }

    public double getYmin()
    {
        return this.ymin;
    }

    public double getXmax()
    {
        return this.xmax;
    }

    public double getYmax()
    {
        return this.ymax;
    }

    public Sector toSector()
    {
        return Sector.fromDegrees(this.ymin, this.ymax, this.xmin, this.xmax);
    }

    public VPFBoundingBox union(VPFBoundingBox boundingBox)
    {
        if (boundingBox == null)
        {
            String message = Logging.getMessage("nullValue.BoundingBoxIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return new VPFBoundingBox(
            (this.xmin < boundingBox.xmin) ? this.xmin : boundingBox.xmin,
            (this.ymin < boundingBox.ymin) ? this.ymin : boundingBox.ymin,
            (this.xmax > boundingBox.xmax) ? this.xmax : boundingBox.xmax,
            (this.ymax > boundingBox.ymax) ? this.ymax : boundingBox.ymax);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("xmin=").append(this.xmin).append(", ");
        sb.append("ymin=").append(this.ymin).append(", ");
        sb.append("xmax=").append(this.xmax).append(", ");
        sb.append("ymax=").append(this.ymax);

        return sb.toString();
    }

    public static VPFBoundingBox fromVecBuffer(VecBuffer buffer)
    {
        if (buffer == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (buffer.getCoordsPerVec() < 2)
        {
            String message = Logging.getMessage("generic.BufferIncompatible", buffer);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double xmin = Double.MAX_VALUE;
        double ymin = Double.MAX_VALUE;
        double xmax = -Double.MAX_VALUE;
        double ymax = -Double.MAX_VALUE;

        int bufferSize = buffer.getSize();
        double[] compArray = new double[2];

        for (int i = 0; i < bufferSize; i++)
        {
            buffer.get(i, compArray);

            if (xmin > compArray[0])
                xmin = compArray[0];
            if (xmax < compArray[0])
                xmax = compArray[0];

            if (ymin > compArray[1])
                ymin = compArray[1];
            if (ymax < compArray[1])
                ymax = compArray[1];
        }

        return new VPFBoundingBox(xmin, ymin, xmax, ymax);
    }
}
