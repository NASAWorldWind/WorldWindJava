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

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.geom.Sector;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
public class RasterServerConfigurationTest
{
    @Test
    public void testParsing001()
    {
        RasterServerConfiguration config = new RasterServerConfiguration("testData/RasterServerConfiguration.xml");

        try
        {
            config.parse();
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }

        assertNotNull(config.getVersion(), "Version is null");
        assertEquals("1.0", config.getVersion(), "Incorrect version number");

        Sector sector = config.getSector();
        assertNotNull(sector, "Configuration sector is null");
        assertEquals(-50.0, sector.getMinLatitude().degrees, 0.0, "Configuration sector min latitude is incorrect");
        assertEquals(-47.0, sector.getMaxLatitude().degrees, 0.0, "Configuration sector max latitude is incorrect");
        assertEquals(178.0, sector.getMinLongitude().degrees, 0.0, "Configuration sector min longitude is incorrect");
        assertEquals(180.0, sector.getMaxLongitude().degrees, 0.0, "Configuration sector max longitude is incorrect");

        Map<String, String> props = config.getProperties();
        assertNotNull(props, "Properties table is null");
        assertEquals(3, props.size(), "Properties table length is incorrect");

        String prop = props.get("gov.nasa.worldwind.avkey.DisplayName");
        assertNotNull(prop, "Property 1 is missing");
        assertEquals("Desktop DTEDfromSTL 30m DTED2  Elevations", prop, "Property 1 is incorrect");

        prop = props.get("gov.nasa.worldwind.avkey.DatasetNameKey");
        assertNotNull(prop, "Property 2 is missing");
        assertEquals("Desktop DTEDfromSTL 30m DTED2  Elevations", prop, "Property 2 is incorrect");

        prop = props.get("gov.nasa.worldwind.avkey.DataCacheNameKey");
        assertNotNull(prop, "Property 3 is missing");
        assertEquals("Desktop DTEDfromSTL 30m DTED2  Elevations", prop, "Property 3 is incorrect");

        List<RasterServerConfiguration.Source> sources = config.getSources();
        assertNotNull(sources, "Configuration sources is null");
        assertEquals(2, sources.size(), "Configuration sources length is incorrect");

        RasterServerConfiguration.Source source = sources.get(0);
        assertNotNull(source, "Source 1 is null");
        String path = source.getPath();
        assertNotNull(path, "Source path 1 is null");
        assertEquals("/Users/tag/Desktop/DTEDfromSTL/30m DTED2/s48 e179.dt2", path, "Source path 1 is incorrect");
        String type = source.getType();
        assertNotNull(type, "Source type 1 is null");
        assertEquals("file", type, "Source type 1 is incorrect");
        sector = source.getSector();
        assertNotNull(sector, "Source sector 1 is null");
        assertEquals(-48.0, sector.getMinLatitude().degrees, 0.0, "Source sector 1 min latitude is incorrect");
        assertEquals(-47.0, sector.getMaxLatitude().degrees, 0.0, "Source sector 1 max latitude is incorrect");
        assertEquals(179.0, sector.getMinLongitude().degrees, 0.0, "Source sector 1 min longitude is incorrect");
        assertEquals(180.0, sector.getMaxLongitude().degrees, 0.0, "Source sector 1 max longitude is incorrect");

        source = sources.get(1);
        assertNotNull(source, "Source 2 is null");
        path = source.getPath();
        assertNotNull(path, "Source path 2 is null");
        assertEquals("/Users/tag/Desktop/DTEDfromSTL/30m DTED2/s50 e178.dt2", path, "Source path 2 is incorrect");
        type = source.getType();
        assertNotNull(type, "Source type 2 is null");
        assertEquals("file", type, "Source type 2 is incorrect");
        sector = source.getSector();
        assertNotNull(sector, "Source sector 2 is null");
        assertEquals(-50.0, sector.getMinLatitude().degrees, 0.0, "Source sector 2 min latitude is incorrect");
        assertEquals(-49.0, sector.getMaxLatitude().degrees, 0.0, "Source sector 2 max latitude is incorrect");
        assertEquals(178.0, sector.getMinLongitude().degrees, 0.0, "Source sector 2 min longitude is incorrect");
        assertEquals(179.0, sector.getMaxLongitude().degrees, 0.0, "Source sector 2 max longitude is incorrect");
    }
}
