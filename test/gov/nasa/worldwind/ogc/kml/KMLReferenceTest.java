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

package gov.nasa.worldwind.ogc.kml;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test resolution of local and remote references using {@link KMLRoot#resolveReference(String)}.
 */
public class KMLReferenceTest
{
    private KMLRoot root;

    @BeforeEach
    public void setUp()
    {
        try
        {
            this.root = KMLRoot.createAndParse("testData/KML/StyleMap.kml");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown()
    {
        this.root = null;
    }

    @Test
    public void testReferenceToLocalKMLFile()
    {
        Object o = this.root.resolveReference("testData/KML/PointPlacemark.kml");
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to local KML file");

        o = this.root.resolveLocalReference("testData/KML/PointPlacemark.kml", null);
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to local KML file");
    }

    @Test
    public void testReferenceToLocalKMZFile()
    {
        Object o = this.root.resolveReference("testData/KML/PointPlacemarkLocalImage.kmz");
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to local KML file");

        o = this.root.resolveLocalReference("testData/KML/PointPlacemarkLocalImage.kmz", null);
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to local KML file");
    }

    @Test
    public void testReferenceToLocalImage()
    {
        String path = "testData/KML/etna.jpg";
        Object o = this.root.resolveReference(path);
        assertEquals(path, o, "Cannot resolve reference to local image file");

        o = this.root.resolveLocalReference(path, null);
        assertEquals(path, o, "Cannot resolve reference to local image file");
    }

    @Test
    public void testReferenceToLocalElement()
    {
        Object o = this.root.resolveReference("#normalPlacemark");
        assertTrue(o instanceof KMLStyle, "Cannot resolve reference to local style");

        // Local references should start with #, but many files do not include the #. Test that resolution works even
        // if the reference is malformed.
        o = this.root.resolveReference("normalPlacemark");
        assertTrue(o instanceof KMLStyle, "Cannot resolve reference to local style (without leading #)");
    }

    @Test
    public void testReferenceToElementInLocalFile()
    {
        Object o = this.root.resolveReference("testData/KML/StyleReferences.kml#transBluePoly");
        assertTrue(o instanceof KMLStyle, "Cannot resolve reference to element in local KML file");

        o = this.root.resolveLocalReference("testData/KML/StyleReferences.kml", "transBluePoly");
        assertTrue(o instanceof KMLStyle, "Cannot resolve reference to element in local KML file");
    }

    @Test
    public void testKMZReference() throws IOException, XMLStreamException
    {
        KMLRoot root = KMLRoot.createAndParse("testData/KML/PointPlacemarkLocalImage.kmz");

        Object o = root.resolveReference("icon21.png");
        assertNotNull(o, "Cannot resolve reference to file KMZ archive");
    }

    @Disabled
    @Test
    public void testReferenceToRemoteKML()
    {
        String url
            = "https://worldwind.arc.nasa.gov/kml-samples/morekml/Network_Links/Targets/Network_Links.Targets.Simple.kml";
        Object o = this.resolveReferenceBlocking(this.root, url);
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to remote KML file");
    }

    @Disabled
    @Test
    public void testReferenceToRemoteKMZ()
    {
        String url = "https://worldwind.arc.nasa.gov/kml-samples/kml/kmz/simple/mimetype.kmz";
        Object o = this.resolveReferenceBlocking(this.root, url);
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to remote KMZ file");

        o = this.resolveRemoteReferenceBlocking(this.root, url, null);
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to remote KMZ file");

        o = this.resolveNetworkLinkBlocking(this.root, url);
        assertTrue(o instanceof KMLRoot, "Cannot resolve reference to remote KMZ file");
    }

    @Disabled
    @Test
    public void testReferenceToRemoteElement()
    {
        String url
            = "https://worldwind.arc.nasa.gov/kml-samples/morekml/Network_Links/Targets/Network_Links.Targets.Simple.kml#networkLinkPlacemark";
        Object o = this.resolveReferenceBlocking(this.root, url);
        assertTrue(o instanceof KMLPlacemark, "Cannot resolve reference to remote KML file");

        o = this.resolveRemoteReferenceBlocking(this.root,
            "https://worldwind.arc.nasa.gov/kml-samples/morekml/Network_Links/Targets/Network_Links.Targets.Simple.kml",
            "networkLinkPlacemark");
        assertTrue(o instanceof KMLPlacemark, "Cannot resolve reference to remote KML file");
    }

    /**
     * Attempt to resolve a reference using {@link KMLRoot#resolveReference(String)}, and do not return until the
     * reference has been resolved or a timeout (one minute) elapses.
     *
     * @param root Resolve the link relative to this document root.
     * @param link Link to resolve
     *
     * @return File pointed to by {@code link}, or null if the link cannot be resolved, or the timeout elapses.
     */
    private Object resolveReferenceBlocking(KMLRoot root, String link)
    {
        long timeout = 60000; // One minute
        long start = System.currentTimeMillis();

        Object o = root.resolveReference(link);
        while (o == null && (System.currentTimeMillis() - start) < timeout)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored)
            {
            }
            o = root.resolveReference(link);
        }

        return o;
    }

    /**
     * Attempt to resolve a reference using {@link KMLRoot#resolveRemoteReference(String, String)}, and do not return
     * until the reference has been resolved or a timeout (one minute) elapses.
     *
     * @param root     Resolve the link relative to this document root.
     * @param linkBase Link to resolve.
     * @param linkRef  Relative reference part of the link.
     *
     * @return File pointed to by {@code link}, or null if the link cannot be resolved, or the timeout elapses.
     */
    private Object resolveRemoteReferenceBlocking(KMLRoot root, String linkBase, String linkRef)
    {
        long timeout = 60000; // One minute
        long start = System.currentTimeMillis();

        Object o = root.resolveRemoteReference(linkBase, linkRef);
        while (o == null && (System.currentTimeMillis() - start) < timeout)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored)
            {
            }
            o = root.resolveRemoteReference(linkBase, linkRef);
        }

        return o;
    }

    /**
     * Attempt to resolve a reference using {@link KMLRoot#resolveRemoteReference(String, String)}, and do not return
     * until the reference has been resolved or a timeout (one minute) elapses.
     *
     * @param root Resolve the link relative to this document root.
     * @param link Link to resolve.
     *
     * @return File pointed to by {@code link}, or null if the link cannot be resolved, or the timeout elapses.
     */
    private Object resolveNetworkLinkBlocking(KMLRoot root, String link)
    {
        long timeout = 60000; // One minute
        long start = System.currentTimeMillis();

        Object o = root.resolveNetworkLink(link, true, System.currentTimeMillis());
        while (o == null && (System.currentTimeMillis() - start) < timeout)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException ignored)
            {
            }
            o = root.resolveNetworkLink(link, true, System.currentTimeMillis());
        }

        return o;
    }
}