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

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.BasicDragger;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;

/**
 * This example illustrates how you might show video on the globe's surface. It uses a {@link
 * gov.nasa.worldwind.render.SurfaceImage} to display one image after another, each of which could correspond to a frame
 * of video. The video frames are simulated by creating a new buffered image for each frame. The same
 * <code>SurfaceImage</code> is used. The image source of the <code>SurfaceImage</code> is continually set to a new
 * BufferedImage. (It would be more efficient to also re-use a single BufferedImage, but one objective of this example
 * is to show how to do this when the image can't be re-used.) The <code>SurfaceImage</code> location could also be
 * continually changed, but this example doesn't do that.
 *
 * @author tag
 * @version $Id: VideoOnTerrain.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class VideoOnTerrain extends ApplicationTemplate
{
    protected static final int IMAGE_SIZE = 512;
    protected static final double IMAGE_OPACITY = 0.5;
    protected static final double IMAGE_SELECTED_OPACITY = 0.8;

    // These corners do not form a Sector, so SurfaceImage must generate a texture rather than simply using the source
    // image.
    protected static final java.util.List<LatLon> CORNERS = Arrays.asList(
        LatLon.fromDegrees(37.8313, -105.0653),
        LatLon.fromDegrees(37.8313, -105.0396),
        LatLon.fromDegrees(37.8539, -105.04),
        LatLon.fromDegrees(37.8539, -105.0653)
    );

    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, true);

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Video on terrain");
            insertBeforePlacenames(this.getWwd(), layer);

            // Set up a SelectListener to drag the SurfaceImage.
            this.getWwd().addSelectListener(new SurfaceImageDragger(this.getWwd()));

            final SurfaceImage surfaceImage = new SurfaceImage(makeImage(), CORNERS);
            surfaceImage.setOpacity(IMAGE_OPACITY);
            layer.addRenderable(surfaceImage);

            javax.swing.Timer timer = new javax.swing.Timer(50, new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    Iterable<LatLon> corners = surfaceImage.getCorners();
                    surfaceImage.setImageSource(makeImage(), corners);
                    getWwd().redraw();
                }
            });
            timer.start();
        }

        protected long counter;
        protected long start = System.currentTimeMillis();

        protected BufferedImage makeImage()
        {
            BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics();

            g.setPaint(Color.WHITE);
            g.fill3DRect(0, 0, IMAGE_SIZE, IMAGE_SIZE, false);

            g.setPaint(Color.RED);
            g.setFont(Font.decode("ARIAL-BOLD-50"));

            g.drawString(Long.toString(++this.counter) + " frames", 10, IMAGE_SIZE / 4);
            g.drawString(Long.toString((System.currentTimeMillis() - start) / 1000) + " sec", 10, IMAGE_SIZE / 2);
            g.drawString("Heap:" + Long.toString(Runtime.getRuntime().totalMemory()), 10, 3 * IMAGE_SIZE / 4);

            g.dispose();

            return image;
        }
    }

    protected static class SurfaceImageDragger implements SelectListener
    {
        protected WorldWindow wwd;
        protected SurfaceImage lastHighlit;
        protected BasicDragger dragger;

        public SurfaceImageDragger(WorldWindow wwd)
        {
            this.wwd = wwd;
            this.dragger = new BasicDragger(wwd);
        }

        public void selected(SelectEvent event)
        {
            // Have rollover events highlight the rolled-over object.
            if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
            {
                this.highlight(event.getTopObject());
                this.wwd.redraw();
            }

            // Drag the selected object.
            if (event.getEventAction().equals(SelectEvent.DRAG) ||
                event.getEventAction().equals(SelectEvent.DRAG_END))
            {
                this.dragger.selected(event);

                if (this.dragger.isDragging())
                    this.wwd.redraw();
            }

            // We missed any roll-over events while dragging, so highlight any under the cursor now.
            if (event.getEventAction().equals(SelectEvent.DRAG_END))
            {
                PickedObjectList pol = this.wwd.getObjectsAtCurrentPosition();
                if (pol != null)
                {
                    this.highlight(pol.getTopObject());
                    this.wwd.redraw();
                }
            }
        }

        protected void highlight(Object o)
        {
            if (this.lastHighlit == o)
                return; // Same thing selected

            // Turn off highlight if on.
            if (this.lastHighlit != null)
            {
                this.lastHighlit.setOpacity(IMAGE_OPACITY);
                this.lastHighlit = null;
            }

            // Turn on highlight if selected object is a SurfaceImage.
            if (o instanceof SurfaceImage)
            {
                this.lastHighlit = (SurfaceImage) o;
                this.lastHighlit.setOpacity(IMAGE_SELECTED_OPACITY);
            }
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 37.8432);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -105.0527);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 7000);
        ApplicationTemplate.start("WorldWind Video on Terrain", AppFrame.class);
    }
}
