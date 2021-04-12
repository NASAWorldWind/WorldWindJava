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
