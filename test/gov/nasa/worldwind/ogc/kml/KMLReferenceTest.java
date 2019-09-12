/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test resolution of local and remote references using {@link KMLRoot#resolveReference(String)}.
 */
@RunWith(JUnit4.class)
public class KMLReferenceTest
{
    private KMLRoot root;

    @Before
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

    @After
    public void tearDown()
    {
        this.root = null;
    }

    @Test
    public void testReferenceToLocalKMLFile()
    {
        Object o = this.root.resolveReference("testData/KML/PointPlacemark.kml");
        assertTrue("Cannot resolve reference to local KML file", o instanceof KMLRoot);

        o = this.root.resolveLocalReference("testData/KML/PointPlacemark.kml", null);
        assertTrue("Cannot resolve reference to local KML file", o instanceof KMLRoot);
    }

    @Test
    public void testReferenceToLocalKMZFile()
    {
        Object o = this.root.resolveReference("testData/KML/PointPlacemarkLocalImage.kmz");
        assertTrue("Cannot resolve reference to local KML file", o instanceof KMLRoot);

        o = this.root.resolveLocalReference("testData/KML/PointPlacemarkLocalImage.kmz", null);
        assertTrue("Cannot resolve reference to local KML file", o instanceof KMLRoot);
    }

    @Test
    public void testReferenceToLocalImage()
    {
        String path = "testData/KML/etna.jpg";
        Object o = this.root.resolveReference(path);
        assertEquals("Cannot resolve reference to local image file", path, o);

        o = this.root.resolveLocalReference(path, null);
        assertEquals("Cannot resolve reference to local image file", path, o);
    }

    @Test
    public void testReferenceToLocalElement()
    {
        Object o = this.root.resolveReference("#normalPlacemark");
        assertTrue("Cannot resolve reference to local style", o instanceof KMLStyle);

        // Local references should start with #, but many files do not include the #. Test that resolution works even
        // if the reference is malformed.
        o = this.root.resolveReference("normalPlacemark");
        assertTrue("Cannot resolve reference to local style (without leading #)", o instanceof KMLStyle);
    }

    @Test
    public void testReferenceToElementInLocalFile()
    {
        Object o = this.root.resolveReference("testData/KML/StyleReferences.kml#transBluePoly");
        assertTrue("Cannot resolve reference to element in local KML file", o instanceof KMLStyle);

        o = this.root.resolveLocalReference("testData/KML/StyleReferences.kml", "transBluePoly");
        assertTrue("Cannot resolve reference to element in local KML file", o instanceof KMLStyle);
    }

    @Test
    public void testKMZReference() throws IOException, XMLStreamException
    {
        KMLRoot root = KMLRoot.createAndParse("testData/KML/PointPlacemarkLocalImage.kmz");

        Object o = root.resolveReference("icon21.png");
        assertNotNull("Cannot resolve reference to file KMZ archive", o);
    }

    @Ignore
    @Test
    public void testReferenceToRemoteKML()
    {
        String url
            = "https://worldwind.arc.nasa.gov/kml-samples/morekml/Network_Links/Targets/Network_Links.Targets.Simple.kml";
        Object o = this.resolveReferenceBlocking(this.root, url);
        assertTrue("Cannot resolve reference to remote KML file", o instanceof KMLRoot);
    }

    @Ignore
    @Test
    public void testReferenceToRemoteKMZ()
    {
        String url = "https://worldwind.arc.nasa.gov/kml-samples/kml/kmz/simple/mimetype.kmz";
        Object o = this.resolveReferenceBlocking(this.root, url);
        assertTrue("Cannot resolve reference to remote KMZ file", o instanceof KMLRoot);

        o = this.resolveRemoteReferenceBlocking(this.root, url, null);
        assertTrue("Cannot resolve reference to remote KMZ file", o instanceof KMLRoot);

        o = this.resolveNetworkLinkBlocking(this.root, url);
        assertTrue("Cannot resolve reference to remote KMZ file", o instanceof KMLRoot);
    }

    @Ignore
    @Test
    public void testReferenceToRemoteElement()
    {
        String url
            = "https://worldwind.arc.nasa.gov/kml-samples/morekml/Network_Links/Targets/Network_Links.Targets.Simple.kml#networkLinkPlacemark";
        Object o = this.resolveReferenceBlocking(this.root, url);
        assertTrue("Cannot resolve reference to remote KML file", o instanceof KMLPlacemark);

        o = this.resolveRemoteReferenceBlocking(this.root,
            "https://worldwind.arc.nasa.gov/kml-samples/morekml/Network_Links/Targets/Network_Links.Targets.Simple.kml",
            "networkLinkPlacemark");
        assertTrue("Cannot resolve reference to remote KML file", o instanceof KMLPlacemark);
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