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

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.*;

/**
 * Shows how to add a layer over the globe's surface imagery to simulate dimming the surface. The technique is very
 * simple: just create a {@link SurfaceImage}, apply it to the full globe, and use its opacity to control the amount
 * of dimming. This example uses a black surface image, but any color could be used.
 *
 * Note that this does not provide a filtering effect -- enhancing or blocking specific colors. For that
 * <code>SurfaceImage</code> would need blending controls, but it doesn't have them.
 *
 * @author tag
 * @version $Id: DimGlobeSurface.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class DimGlobeSurface extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected SurfaceImage surfaceImage;
        protected JSlider opacitySlider;

        public AppFrame()
        {
            super(true, true, false);

            // Create a surface image covering the full globe and set its initial opacity.

            this.surfaceImage = new SurfaceImage(this.makeFilterImage(), Sector.FULL_SPHERE);
            this.surfaceImage.setOpacity(0.10);

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Surface Dimmer");
            layer.setPickEnabled(false);

            layer.addRenderable(surfaceImage);

            ApplicationTemplate.insertBeforePlacenames(this.getWwd(), layer);

            // Create an opacity control panel.

            JPanel opacityPanel = new JPanel(new BorderLayout(5, 5));
            opacityPanel.setBorder(new EmptyBorder(5, 10, 10, 5));
            opacityPanel.add(new JLabel("Opacity"), BorderLayout.WEST);
            this.makeOpacitySlider();
            opacityPanel.add(this.opacitySlider, BorderLayout.CENTER);
            this.getControlPanel().add(opacityPanel, BorderLayout.SOUTH);
        }

        protected BufferedImage makeFilterImage()
        {
            // A very small image can be used because it's all the same color.
            BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = (Graphics2D) image.getGraphics();

            g.setColor(new Color(0f, 0f, 0f, 1f)); // black, but any color could be used
            g.fillRect(0, 0, image.getWidth(), image.getHeight());

            g.dispose();

            return image;
        }

        protected void makeOpacitySlider()
        {
            this.opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int) (this.surfaceImage.getOpacity() * 100));
            this.opacitySlider.setToolTipText("Filter opacity");
            this.opacitySlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    double value = opacitySlider.getValue();
                    surfaceImage.setOpacity(value / 100);
                    getWwd().redraw();
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Surface Dimming", AppFrame.class);
    }
}
