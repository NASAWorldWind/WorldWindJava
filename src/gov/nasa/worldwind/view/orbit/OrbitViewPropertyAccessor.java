/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
