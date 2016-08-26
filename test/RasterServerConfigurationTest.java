/*
 * Copyright (C) 2015 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

import gov.nasa.worldwind.data.RasterServerConfiguration;
import gov.nasa.worldwind.geom.Sector;
import junit.framework.*;
import junit.textui.TestRunner;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * @author tag
 * @version $Id: RasterServerConfigurationTest.java 2813 2015-02-18 23:35:24Z tgaskins $
 */
@RunWith(Enclosed.class)
public class RasterServerConfigurationTest
{
    public static class Tests extends TestCase
    {
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
            assertEquals("Configuration sector min latitude is incorrect", -50.0, sector.getMinLatitude().degrees);
            assertEquals("Configuration sector max latitude is incorrect", -47.0, sector.getMaxLatitude().degrees);
            assertEquals("Configuration sector min longitude is incorrect", 178.0, sector.getMinLongitude().degrees);
            assertEquals("Configuration sector max longitude is incorrect", 180.0, sector.getMaxLongitude().degrees);

            Map<String, String> props = config.getProperties();
            assertNotNull("Properties table is null", props);
            assertEquals("Properties table length is incorrect", 3, props.size());

            String prop = props.get("gov.nasa.worldwind.avkey.DisplayName");
            assertNotNull("Property 1 is missing", prop);
            assertEquals("Property 1 is incorrect", "Desktop DTEDfromSTL 30m DTED2  Elevations",prop);

            prop = props.get("gov.nasa.worldwind.avkey.DatasetNameKey");
            assertNotNull("Property 2 is missing", prop);
            assertEquals("Property 2 is incorrect", "Desktop DTEDfromSTL 30m DTED2  Elevations", prop);

            prop = props.get("gov.nasa.worldwind.avkey.DataCacheNameKey");
            assertNotNull("Property 3 is missing", prop);
            assertEquals("Property 3 is incorrect", "Desktop DTEDfromSTL 30m DTED2  Elevations",prop);

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
            assertEquals("Source sector 1 min latitude is incorrect", -48.0, sector.getMinLatitude().degrees);
            assertEquals("Source sector 1 max latitude is incorrect", -47.0, sector.getMaxLatitude().degrees);
            assertEquals("Source sector 1 min longitude is incorrect", 179.0, sector.getMinLongitude().degrees);
            assertEquals("Source sector 1 max longitude is incorrect", 180.0, sector.getMaxLongitude().degrees);

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
            assertEquals("Source sector 2 min latitude is incorrect", -50.0, sector.getMinLatitude().degrees);
            assertEquals("Source sector 2 max latitude is incorrect", -49.0, sector.getMaxLatitude().degrees);
            assertEquals("Source sector 2 min longitude is incorrect", 178.0, sector.getMinLongitude().degrees);
            assertEquals("Source sector 2 max longitude is incorrect", 179.0, sector.getMaxLongitude().degrees);
        }

        public static void main(String[] args)
        {
            new TestRunner().doRun(new TestSuite(Tests.class));
        }
    }
}
