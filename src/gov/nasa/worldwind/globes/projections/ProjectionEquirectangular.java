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

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;

/**
 * Implements an Equirectangular projection, also known as Equidistant Cylindrical, Plate Carree and Rectangular. The
 * projected globe is spherical, not ellipsoidal.
 *
 * @author tag
 * @version $Id: ProjectionEquirectangular.java 2277 2014-08-28 21:19:37Z dcollins $
 */
public class ProjectionEquirectangular extends AbstractGeographicProjection
{
    public ProjectionEquirectangular()
    {
        super(Sector.FULL_SPHERE);
    }

    public String getName()
    {
        return "Equirectangular";
    }

    @Override
    public boolean isContinuous()
    {
        return true;
    }

    @Override
    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
    {
        return new Vec4(globe.getEquatorialRadius() * longitude.radians + offset.x,
            globe.getEquatorialRadius() * latitude.radians, metersElevation);
    }

    @Override
    public void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out)
    {
        double eqr = globe.getEquatorialRadius();
        double minLat = sector.getMinLatitude().radians;
        double maxLat = sector.getMaxLatitude().radians;
        double minLon = sector.getMinLongitude().radians;
        double maxLon = sector.getMaxLongitude().radians;
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);
        double offset_x = offset.x;
        int pos = 0;

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
        // corresponding to each latitude and longitude.
        double lat = minLat;
        for (int j = 0; j < numLat; j++, lat += deltaLat)
        {
            if (j == numLat - 1) // explicitly set the last lat to the max latitude to ensure alignment
                lat = maxLat;

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double y = eqr * lat;

            double lon = minLon;
            for (int i = 0; i < numLon; i++, lon += deltaLon)
            {
                if (i == numLon - 1) // explicitly set the last lon to the max longitude to ensure alignment
                    lon = maxLon;

                double x = eqr * lon + offset_x;
                double z = metersElevation[pos];
                out[pos++] = new Vec4(x, y, z);
            }
        }
    }

    @Override
    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
    {
        return Position.fromRadians(cart.y / globe.getEquatorialRadius(),
            (cart.x - offset.x) / globe.getEquatorialRadius(), cart.z);
    }

    @Override
    public Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude)
    {
        return Vec4.UNIT_Y;
    }
}
