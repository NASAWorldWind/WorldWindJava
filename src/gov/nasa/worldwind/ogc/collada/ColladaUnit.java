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

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.WWUtil;

/**
 * Represents the COLLADA <i>Unit</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaUnit.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaUnit extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaUnit(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the "meter" attribute of the unit. This attribute is a scaling factor that converts length
     * units in the COLLADA document to meters (1.0 for meters, 1000 for kilometers, etc.) See COLLADA spec pg. 5-18.
     *
     * @return The scaling factor, or null if none is defined.
     */
    public Double getMeter()
    {
        String s = (String) this.getField("meter");
        return WWUtil.makeDouble(s);
    }
}
