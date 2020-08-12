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

package gov.nasa.worldwind.symbology.milstd1477;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.symbology.AbstractIconRetriever;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.*;
import java.util.MissingResourceException;

/**
 * @author ccrick
 * @version $Id: MilStd1477IconRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MilStd1477IconRetriever extends AbstractIconRetriever
{
    // TODO: add more error checking

    public MilStd1477IconRetriever(String retrieverPath)
    {
        super(retrieverPath);
    }

    public BufferedImage createIcon(String symbolId, AVList params)
    {
        if (symbolId == null)
        {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // retrieve desired symbol and convert to bufferedImage

        // SymbolCode symbolCode = new SymbolCode(symbolIdentifier);

        String filename = this.getFilename(symbolId);
        BufferedImage img = this.readImage(filename);

        if (img == null)
        {
            String msg = Logging.getMessage("Symbology.SymbolIconNotFound", symbolId);
            Logging.logger().severe(msg);
            throw new MissingResourceException(msg, BufferedImage.class.getName(), filename);
        }

        return img;
    }

    protected String getFilename(String code)
    {
        return code.toLowerCase() + ".png";
    }
}