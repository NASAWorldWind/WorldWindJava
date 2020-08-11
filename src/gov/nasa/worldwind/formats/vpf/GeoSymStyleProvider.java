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
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * @author Patrick Murris
 * @version $Id: GeoSymStyleProvider.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymStyleProvider
{
    //private static String TYPE_POINT = "Point";
    private static String TYPE_LINE_PLAIN = "LinePlain";
    private static String TYPE_LINE_COMPLEX = "LineComplex";
    private static String TYPE_AREA_PLAIN = "AreaPlain";
    private static String TYPE_AREA_PATTERN = "AreaPattern";

    // CSV columns
    private static final int CODE = 0;
    private static final int TYPE = 1;
    private static final int LINE_WIDTH = 2;
    private static final int LINE_COLOR = 3;
    private static final int STIPPLE_PATTERN = 4;
    private static final int STIPPLE_FACTOR = 5;
    private static final int FILL_COLOR = 6;

    private Map<String, VPFSymbolAttributes> attributes;
    private double lineWidthFactor = 3d; // mm to pixels

    public GeoSymStyleProvider(String filePath)
    {
        try
        {
            this.loadStylesFromFile(filePath);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionWhileReading", filePath);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    protected void loadStylesFromFile(String filePath) throws IOException
    {
        InputStream inputStream = WWIO.openFileOrResourceStream(filePath, this.getClass());
        if (inputStream == null)
        {
            String message = Logging.getMessage("generic.ExceptionWhileReading", filePath);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        this.attributes = new HashMap<String, VPFSymbolAttributes>();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine())
        {
            String s = scanner.nextLine().trim();
            if (s.length() == 0 || s.startsWith("#"))
                continue;

            String[] tokens = s.split(",");
            String code = tokens[0];
            VPFSymbolAttributes attr = getAttributes(tokens);
            if (attr != null)
                this.attributes.put(code, attr);
        }

        inputStream.close();
    }

    private VPFSymbolAttributes getAttributes(String[] tokens)
    {
        VPFSymbolAttributes attr = new VPFSymbolAttributes(null, null);
        if (tokens[TYPE].equals(TYPE_AREA_PLAIN) || tokens[TYPE].equals(TYPE_AREA_PATTERN))
        {
            attr.setInteriorMaterial(new Material(Color.decode(tokens[FILL_COLOR])));
            if (tokens[TYPE].equals(TYPE_AREA_PATTERN))
            {
                attr.setImageSource(tokens[CODE]);
            }
        }
        else if (tokens[TYPE].equals(TYPE_LINE_PLAIN) || tokens[TYPE].equals(TYPE_LINE_COMPLEX))
        {
            attr.setOutlineMaterial(new Material(Color.decode(tokens[LINE_COLOR])));
            attr.setOutlineWidth(Double.parseDouble(tokens[LINE_WIDTH]) * this.lineWidthFactor);
            if (tokens[TYPE].equals(TYPE_LINE_COMPLEX))
            {
                attr.setOutlineStipplePattern(Integer.decode(tokens[STIPPLE_PATTERN]).shortValue());
                attr.setOutlineStippleFactor(Integer.parseInt(tokens[STIPPLE_FACTOR]));
            }
        }

        return attr;
    }

    public VPFSymbolAttributes getAttributes(String code)
    {
        return this.attributes.get(code);
    }
}
