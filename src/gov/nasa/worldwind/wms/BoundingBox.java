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
