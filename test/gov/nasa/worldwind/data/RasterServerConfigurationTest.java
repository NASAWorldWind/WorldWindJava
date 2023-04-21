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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
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

        assertNotNull("Version is null", config.getVersion());
        assertEquals("Incorrect version number", "1.0", config.getVersion());

        Sector sector = config.getSector();
        assertNotNull("Configuration sector is null", sector);
        assertEquals("Configuration sector min latitude is incorrect", -50.0, sector.getMinLatitude().degrees, 0.0);
        assertEquals("Configuration sector max latitude is incorrect", -47.0, sector.getMaxLatitude().degrees, 0.0);
        assertEquals("Configuration sector min longitude is incorrect", 178.0, sector.getMinLongitude().degrees, 0.0);
        assertEquals("Configuration sector max longitude is incorrect", 180.0, sector.getMaxLongitude().degrees, 0.0);

        Map<String, String> props = config.getProperties();
        assertNotNull("Properties table is null", props);
        assertEquals("Properties table length is incorrect", 3, props.size());

        String prop = props.get("gov.nasa.worldwind.avkey.DisplayName");
        assertNotNull("Property 1 is missing", prop);
        assertEquals("Property 1 is incorrect", "Desktop DTEDfromSTL 30m DTED2  Elevations", prop);

        prop = props.get("gov.nasa.worldwind.avkey.DatasetNameKey");
        assertNotNull("Property 2 is missing", prop);
        assertEquals("Property 2 is incorrect", "Desktop DTEDfromSTL 30m DTED2  Elevations", prop);

        prop = props.get("gov.nasa.worldwind.avkey.DataCacheNameKey");
        assertNotNull("Property 3 is missing", prop);
        assertEquals("Property 3 is incorrect", "Desktop DTEDfromSTL 30m DTED2  Elevations", prop);

        List<RasterServerConfiguration.Source> sources = config.getSources();
        assertNotNull("Configuration sources is null", sources);
        assertEquals("Configuration sources length is incorrect", 2, sources.size());

        RasterServerConfiguration.Source source = sources.get(0);
        assertNotNull("Source 1 is null", source);
        String path = source.getPath();
        assertNotNull("Source path 1 is null", path);
        assertEquals("Source path 1 is incorrect", "/Users/tag/Desktop/DTEDfromSTL/30m DTED2/s48 e179.dt2", path);
        String type = source.getType();
        assertNotNull("Source type 1 is null", type);
        assertEquals("Source type 1 is incorrect", "file", type);
        sector = source.getSector();
        assertNotNull("Source sector 1 is null", sector);
        assertEquals("Source sector 1 min latitude is incorrect", -48.0, sector.getMinLatitude().degrees, 0.0);
        assertEquals("Source sector 1 max latitude is incorrect", -47.0, sector.getMaxLatitude().degrees, 0.0);
        assertEquals("Source sector 1 min longitude is incorrect", 179.0, sector.getMinLongitude().degrees, 0.0);
        assertEquals("Source sector 1 max longitude is incorrect", 180.0, sector.getMaxLongitude().degrees, 0.0);

        source = sources.get(1);
        assertNotNull("Source 2 is null", source);
        path = source.getPath();
        assertNotNull("Source path 2 is null", path);
        assertEquals("Source path 2 is incorrect", "/Users/tag/Desktop/DTEDfromSTL/30m DTED2/s50 e178.dt2", path);
        type = source.getType();
        assertNotNull("Source type 2 is null", type);
        assertEquals("Source type 2 is incorrect", "file", type);
        sector = source.getSector();
        assertNotNull("Source sector 2 is null", sector);
        assertEquals("Source sector 2 min latitude is incorrect", -50.0, sector.getMinLatitude().degrees, 0.0);
        assertEquals("Source sector 2 max latitude is incorrect", -49.0, sector.getMaxLatitude().degrees, 0.0);
        assertEquals("Source sector 2 min longitude is incorrect", 178.0, sector.getMinLongitude().degrees, 0.0);
        assertEquals("Source sector 2 max longitude is incorrect", 179.0, sector.getMaxLongitude().degrees, 0.0);
    }
}
