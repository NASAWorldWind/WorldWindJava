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
package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.ViewPropertyAccessor;

/**
 * @author dcollins
 * @version $Id: OrbitViewPropertyAccessor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OrbitViewPropertyAccessor extends ViewPropertyAccessor
{
    
    private OrbitViewPropertyAccessor()
    {
    }


    public static PropertyAccessor.PositionAccessor createCenterPositionAccessor(OrbitView view)
    {
        return new CenterPositionAccessor(view);
    }



    public static PropertyAccessor.DoubleAccessor createZoomAccessor(OrbitView view)
    {
        return new ZoomAccessor(view);
    }

    //public static RotationAccessor createRotationAccessor()
    //{
    //    return new RotationAccessor();
    //}

    // ============== Implementation ======================= //
    // ============== Implementation ======================= //
    // ============== Implementation ======================= //

    private static class CenterPositionAccessor implements PropertyAccessor.PositionAccessor
    {
        private OrbitView orbitView;
        public CenterPositionAccessor(OrbitView view)
        {
            this.orbitView = view;
        }

        public Position getPosition()
        {
            if (this.orbitView == null)
                return null;

            return orbitView.getCenterPosition();

        }

        public boolean setPosition(Position value)
        {
             //noinspection SimplifiableIfStatement
            if (this.orbitView == null || value == null)
                return false;


            try
            {

                this.orbitView.setCenterPosition(value);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }



    private static class ZoomAccessor implements PropertyAccessor.DoubleAccessor
    {
        OrbitView orbitView;
        public ZoomAccessor(OrbitView orbitView)
        {
            this.orbitView = orbitView;
        }
        public final Double getDouble()
        {
            if (this.orbitView == null)
                return null;

            return this.orbitView.getZoom();

        }

        public final boolean setDouble(Double value)
        {
            //noinspection SimplifiableIfStatement
            if (this.orbitView == null || value == null)
                return false;

            try
            {
                this.orbitView.setZoom(value);
                return true;

            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    //private static class RotationAccessor implements QuaternionAccessor
    //{
    //    public final Quaternion getQuaternion(OrbitView orbitView)
    //    {
    //        if (orbitView == null)
    //            return null;
    //
    //        return orbitView.getRotation();
    //    }
    //
    //    public final boolean setQuaternion(OrbitView orbitView, Quaternion value)
    //    {
    //        if (orbitView == null || value == null)
    //            return false;
    //
    //        orbitView.setRotation(value);
    //        return true;
    //    }
    //}
}
