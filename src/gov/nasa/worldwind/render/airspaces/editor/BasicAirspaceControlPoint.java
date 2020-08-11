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
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.geom.Vec4;

/**
 * @author dcollins
 * @version $Id: BasicAirspaceControlPoint.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicAirspaceControlPoint implements AirspaceControlPoint
{
    public static class BasicControlPointKey
    {
        private int locationIndex;
        private int altitudeIndex;

        public BasicControlPointKey(int locationIndex, int altitudeIndex)
        {
            this.locationIndex = locationIndex;
            this.altitudeIndex = altitudeIndex;
        }

        public int getLocationIndex()
        {
            return this.locationIndex;
        }

        public int getAltitudeIndex()
        {
            return this.altitudeIndex;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            BasicControlPointKey that = (BasicControlPointKey) o;
            return (this.locationIndex == that.locationIndex) && (this.altitudeIndex == that.altitudeIndex);
        }

        public int hashCode()
        {
            int result = this.locationIndex;
            result = 31 * result + this.altitudeIndex;
            return result;
        }
    }

    private AirspaceEditor editor;
    private Airspace airspace;
    private int locationIndex;
    private int altitudeIndex;
    private Vec4 point;

    public BasicAirspaceControlPoint(AirspaceEditor editor, Airspace airspace, int locationIndex, int altitudeIndex,
        Vec4 point)
    {
        this.editor = editor;
        this.airspace = airspace;
        this.locationIndex = locationIndex;
        this.altitudeIndex = altitudeIndex;
        this.point = point;
    }

    public BasicAirspaceControlPoint(AirspaceEditor editor, Airspace airspace, Vec4 point)
    {
        this(editor, airspace, -1, -1, point);
    }

    public AirspaceEditor getEditor()
    {
        return this.editor;
    }

    public Airspace getAirspace()
    {
        return this.airspace;
    }

    public int getLocationIndex()
    {
        return this.locationIndex;
    }

    public int getAltitudeIndex()
    {
        return this.altitudeIndex;
    }

    public Vec4 getPoint()
    {
        return this.point;
    }

    public Object getKey()
    {
        return keyFor(this.locationIndex, this.altitudeIndex);
    }

    public static Object keyFor(int locationIndex, int altitudeIndex)
    {
        return new BasicControlPointKey(locationIndex, altitudeIndex);    
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        BasicAirspaceControlPoint that = (BasicAirspaceControlPoint) o;

        // Editor and airspace are compared by references, because we're only concerned about the exact reference
        // a control point refers to, rather than an equivalent object.
        if (this.editor != that.editor)
            return false;
        if (this.airspace != that.airspace)
            return false;
        if (this.altitudeIndex != that.altitudeIndex)
            return false;
        if (this.locationIndex != that.locationIndex)
            return false;
        //noinspection RedundantIfStatement
        if (this.point != null ? !this.point.equals(that.point) : that.point != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result = this.editor != null ? this.editor.hashCode() : 0;
        result = 31 * result + (this.airspace != null ? this.airspace.hashCode() : 0);
        result = 31 * result + this.locationIndex;
        result = 31 * result + this.altitudeIndex;
        result = 31 * result + (this.point != null ? this.point.hashCode() : 0);
        return result;
    }
}
