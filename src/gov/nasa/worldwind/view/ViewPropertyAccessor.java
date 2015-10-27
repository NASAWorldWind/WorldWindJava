/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.PropertyAccessor;

/**
 * @author jym
 * @version $Id: ViewPropertyAccessor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ViewPropertyAccessor
{
    public ViewPropertyAccessor()
    {
    }

    public static PropertyAccessor.DoubleAccessor createElevationAccessor(View view)
    {
        return new ElevationAccessor(view);
    }

    public static PropertyAccessor.AngleAccessor createHeadingAccessor(View view)
    {
        return new HeadingAccessor(view);
    }

    public static PropertyAccessor.AngleAccessor createPitchAccessor(View view)
    {
        return new PitchAccessor(view);
    }

    public static PropertyAccessor.AngleAccessor createRollAccessor(View view)
    {
        return new RollAccessor(view);
    }

    public static PropertyAccessor.PositionAccessor createEyePositionAccessor(View view)
    {
        return new EyePositionAccessor(view);
    }

    public static class HeadingAccessor implements PropertyAccessor.AngleAccessor
    {
        protected View view;

        HeadingAccessor(View view)
        {
            this.view = view;
        }

        public final Angle getAngle()
        {
            if (this.view == null)
                return null;

            return this.view.getHeading();
        }

        public final boolean setAngle(Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (this.view == null || value == null)
                return false;

            try
            {
                this.view.setHeading(value);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    public static class PitchAccessor implements PropertyAccessor.AngleAccessor
    {
        protected View view;

        PitchAccessor(View view)
        {
            this.view = view;
        }

        public final Angle getAngle()
        {
            if (this.view == null)
                return null;

            return view.getPitch();
        }

        public final boolean setAngle(Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (this.view == null || value == null)
                return false;

            try
            {
                this.view.setPitch(value);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    public static class RollAccessor implements PropertyAccessor.AngleAccessor
    {
        protected View view;

        RollAccessor(View view)
        {
            this.view = view;
        }

        public final Angle getAngle()
        {
            if (this.view == null)
                return null;

            return view.getRoll();
        }

        public final boolean setAngle(Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (this.view == null || value == null)
                return false;

            try
            {
                this.view.setRoll(value);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    public static class EyePositionAccessor implements
        PropertyAccessor.PositionAccessor
    {

        protected View view;

        EyePositionAccessor(View view)
        {
            this.view = view;
        }

        public Position getPosition()
        {
            if (this.view == null)
                return null;

            return this.view.getEyePosition();
        }

        public boolean setPosition(Position value)
        {
            //noinspection SimplifiableIfStatement
            if (this.view == null || value == null)
                return false;

            try
            {
                this.view.setEyePosition(value);
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }

    public static class ElevationAccessor implements
        PropertyAccessor.DoubleAccessor
    {
        protected View view;

        ElevationAccessor(View view)
        {
            this.view = view;
        }

        public Double getDouble()
        {
            if (this.view == null)
                return null;

            return this.view.getEyePosition().getElevation();
        }

        public boolean setDouble(Double value)
        {
            //noinspection SimplifiableIfStatement
            if (this.view == null || value == null)
                return false;

            try
            {

                this.view.setEyePosition(
                    new Position(this.view.getCurrentEyePosition(), value));
                return true;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
}
