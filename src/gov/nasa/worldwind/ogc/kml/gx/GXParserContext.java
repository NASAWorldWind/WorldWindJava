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

package gov.nasa.worldwind.ogc.kml.gx;

import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tag
 * @version $Id: GXParserContext.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GXParserContext extends BasicXMLEventParserContext
{
    protected static final String[] StringFields = new String[]
        {
            "altitudeMode",
            "description",
            "flyToMode",
            "playMode",
        };

    protected static final String[] DoubleFields = new String[]
        {
            "duration",
        };

    protected static final String[] BooleanFields = new String[]
        {
            "balloonVisibility",
        };

    public static Map<QName, XMLEventParser> getDefaultParsers()
    {
        ConcurrentHashMap<QName, XMLEventParser> parsers = new ConcurrentHashMap<QName, XMLEventParser>();

        String ns = GXConstants.GX_NAMESPACE;
        parsers.put(new QName(ns, "AnimatedUpdate"), new GXAnimatedUpdate(ns));
        parsers.put(new QName(ns, "FlyTo"), new GXFlyTo(ns));
        parsers.put(new QName(ns, "LatLonQuad"), new GXLatLongQuad(ns));
        parsers.put(new QName(ns, "Playlist"), new GXPlaylist(ns));
        parsers.put(new QName(ns, "SoundCue"), new GXSoundCue(ns));
        parsers.put(new QName(ns, "TimeSpan"), new KMLTimeSpan(ns));
        parsers.put(new QName(ns, "TimeStamp"), new KMLTimeStamp(ns));
        parsers.put(new QName(ns, "Tour"), new GXTour(ns));
        parsers.put(new QName(ns, "TourControl"), new GXTourControl(ns));
        parsers.put(new QName(ns, "Wait"), new GXWait(ns));

        StringXMLEventParser stringParser = new StringXMLEventParser();
        for (String s : StringFields)
        {
            parsers.put(new QName(ns, s), stringParser);
        }

        DoubleXMLEventParser doubleParser = new DoubleXMLEventParser();
        for (String s : DoubleFields)
        {
            parsers.put(new QName(ns, s), doubleParser);
        }

        BooleanXMLEventParser booleanParser = new BooleanXMLEventParser();
        for (String s : BooleanFields)
        {
            parsers.put(new QName(ns, s), booleanParser);
        }

        return parsers;
    }
}
