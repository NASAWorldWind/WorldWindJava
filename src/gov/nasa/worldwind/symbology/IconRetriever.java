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

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.image.*;

/**
 * Retrieves icons for symbols in a symbol set from a local disk or the network. Typically, an icon retriever will be
 * implemented for a specific symbol set. For example, the {@link gov.nasa.worldwind.symbology.milstd2525.MilStd2525IconRetriever}
 * retrieves icons for symbols in the MIL-STD-2525 symbology set. See the <a href="https://worldwind.arc.nasa.gov/java/tutorials/icon-retriever/"
 * target="_blank">Icon Retriever Usage Guide</a> for more information.
 *
 * @author ccrick
 * @version $Id: IconRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface IconRetriever
{
    /**
     * Create an icon to represent a symbol in a symbol set.
     *
     * @param symbolId Identifier for the symbol. The format of this identifier depends on the symbology set.
     * @param params   Parameters that affect icon retrieval.
     *
     * @return A BufferedImage containing the requested icon, or null if the icon cannot be retrieved.
     */
    BufferedImage createIcon(String symbolId, AVList params);
}