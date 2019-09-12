/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.wms;

import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: BoundingBox.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BoundingBox
{
    private String crs;
    private double minx;
    private double maxx;
    private double miny;
    private double maxy;
    private double resx;
    private double resy;

    public static BoundingBox createFromStrings(String crs, String minx, String maxx, String miny, String maxy,
        String resx, String resy)
    {
        BoundingBox bbox = new BoundingBox();

        try
        {
            bbox.crs = crs;
            bbox.minx = Double.parseDouble(minx);
            bbox.maxx = Double.parseDouble(maxx);
            bbox.miny = Double.parseDouble(miny);
            bbox.maxy = Double.parseDouble(maxy);
            bbox.resx = resx != null && !resx.equals("") ? Double.parseDouble(resx) : 0;
            bbox.resy = resy != null && !resy.equals("") ? Double.parseDouble(resy) : 0;
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("XML.ImproperDataType");
            Logging.logger().severe(message);
            throw e;
        }

        return bbox;
    }

    public String getCrs()
    {
        return crs;
    }

    public double getMinx()
    {
        return minx;
    }

    public double getMaxx()
    {
        return maxx;
    }

    public double getMiny()
    {
        return miny;
    }

    public double getMaxy()
    {
        return maxy;
    }

    public double getResx()
    {
        return resx;
    }

    public double getResy()
    {
        return resy;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(this.crs);
        sb.append(": minx = ");
        sb.append(this.minx);
        sb.append(" miny = ");
        sb.append(this.miny);
        sb.append(" maxx = ");
        sb.append(this.maxx);
        sb.append(" maxy = ");
        sb.append(this.maxy);
        sb.append(" resx = ");
        sb.append(this.resx);
        sb.append(" resy = ");
        sb.append(this.resy);

        return sb.toString();
    }
}
