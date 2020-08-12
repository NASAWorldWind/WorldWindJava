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

package gov.nasa.worldwind.util;

import com.jogamp.opengl.util.texture.TextureData;
import gov.nasa.worldwind.Configuration;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.jogamp.opengl.*;
import java.io.*;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class OGLUtilTest
{
    private static final String PNG_FILE = "./src/images/earth-map-512x256.png";
    private static final String DDS_FILE = "./src/images/BMNG_world.topo.bathy.200405.3.2048x1024.dds";
    private static final String JPG_FILE = "./src/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg";

    private GLProfile glProfile;

    @Before
    public void setUp()
    {
        this.glProfile = Configuration.getMaxCompatibleGLProfile();
    }

    @After
    public void tearDown()
    {
        this.glProfile = null;
    }

    @Test
    public void testPngFile() throws IOException
    {
        File f = new File(PNG_FILE);

        TextureData td = OGLUtil.newTextureData(this.glProfile, f, false);

        assertEquals(td.getWidth(), 512);
        assertEquals(td.getHeight(), 256);
    }

    @Test
    public void testPngStream() throws IOException
    {
        File f = new File(PNG_FILE);
        InputStream s = new FileInputStream(f);

        TextureData td = OGLUtil.newTextureData(this.glProfile, s, false);

        assertEquals(td.getWidth(), 512);
        assertEquals(td.getHeight(), 256);
    }

    @Test
    public void testPngUrl() throws IOException
    {
        URL url = new File(PNG_FILE).toURI().toURL();

        TextureData td = OGLUtil.newTextureData(this.glProfile, url, false);

        assertEquals(td.getWidth(), 512);
        assertEquals(td.getHeight(), 256);
    }

    @Test
    public void testDdsFile() throws IOException
    {
        File f = new File(DDS_FILE);

        TextureData td = OGLUtil.newTextureData(this.glProfile, f, false);

        assertEquals(td.getWidth(), 2048);
        assertEquals(td.getHeight(), 1024);
    }

    @Test
    public void testDdsStream() throws IOException
    {
        File f = new File(DDS_FILE);
        InputStream s = new FileInputStream(f);

        TextureData td = OGLUtil.newTextureData(this.glProfile, s, false);

        assertEquals(td.getWidth(), 2048);
        assertEquals(td.getHeight(), 1024);
    }

    @Test
    public void testDdsUrl() throws IOException
    {
        URL url = new File(DDS_FILE).toURI().toURL();

        TextureData td = OGLUtil.newTextureData(this.glProfile, url, false);

        assertEquals(td.getWidth(), 2048);
        assertEquals(td.getHeight(), 1024);
    }

    @Test
    public void testJpgFile() throws IOException
    {
        File f = new File(JPG_FILE);

        TextureData td = OGLUtil.newTextureData(this.glProfile, f, false);

        assertEquals(td.getWidth(), 2048);
        assertEquals(td.getHeight(), 1024);
    }

    @Test
    public void testJpgStream() throws IOException
    {
        File f = new File(JPG_FILE);
        InputStream s = new FileInputStream(f);

        TextureData td = OGLUtil.newTextureData(this.glProfile, s, false);

        assertEquals(td.getWidth(), 2048);
        assertEquals(td.getHeight(), 1024);
    }

    @Test
    public void testJpgUrl() throws IOException
    {
        URL url = new File(JPG_FILE).toURI().toURL();

        TextureData td = OGLUtil.newTextureData(this.glProfile, url, false);

        assertEquals(td.getWidth(), 2048);
        assertEquals(td.getHeight(), 1024);
    }

    /** Test that the newTextureData supports indexed color PNG images. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-369. */
    @Test
    public void testIndexedColorPng() throws IOException
    {
        URL url = new File("./testData/32x32-icon-nasa-indexed-color.png").toURI().toURL();

        TextureData td = OGLUtil.newTextureData(this.glProfile, url, false);
        assertEquals(td.getPixelFormat(), GL.GL_RGBA);
    }

    /** Test that the newTextureData supports interlaced PNG images. See http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-365. */
    @Test
    public void testInterlacedPng() throws IOException
    {
        URL url = new File("./testData/32x32-icon-nasa-interlaced.png").toURI().toURL();

        TextureData td = OGLUtil.newTextureData(this.glProfile, url, false);

        assertEquals(td.getWidth(), 32);
        assertEquals(td.getHeight(), 32);
    }
}
