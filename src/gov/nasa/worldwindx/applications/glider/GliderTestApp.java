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
package gov.nasa.worldwindx.applications.glider;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: GliderTestApp.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class GliderTestApp extends ApplicationTemplate {

    public static class GliderAppPanel extends AppPanel {

        public GliderAppPanel(Dimension canvasSize, boolean includeStatusBar) {
            super(canvasSize, includeStatusBar);
        }

        @Override
        protected WorldWindow createWorldWindow() {
            return new GliderWorldWindow();
        }
    }

    public static class GliderAppFrame extends AppFrame {

        public GliderAppFrame() {
            super(true, true, false);
        }

        @Override
        protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar) {
            return new GliderAppPanel(canvasSize, includeStatusBar);
        }
    }

    protected static LatLon nw = LatLon.fromDegrees(48.55774732, -134.459224670811);
    protected static LatLon ne = nw.add(LatLon.fromDegrees(0, 0.036795 * 250));
    protected static LatLon se = nw.add(LatLon.fromDegrees(-0.036795 * 200, 0.036795 * 250));
    protected static LatLon sw = nw.add(LatLon.fromDegrees(-0.036795 * 200, 0));
    protected static List<LatLon> corners = Arrays.asList(sw, se, ne, nw);

    protected static String cloudImagePath = "gov/nasa/worldwindx/examples/images/GLIDERTestImage-800x519.jpg";

    protected static float[][] makeField(List<LatLon> corners, int width, int height, Angle angle) {
        Sector sector = Sector.boundingSector(corners);
        double dLat = sector.getDeltaLatDegrees() / (height - 1d);
        double dLon = sector.getDeltaLonDegrees() / (width - 1d);

        float[] lons = new float[width * height];
        float[] lats = new float[lons.length];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                lons[j * width + i] = (float) (sector.getMinLongitude().degrees + i * dLon);
                lats[j * width + i] = (float) (sector.getMaxLatitude().degrees - j * dLat);
            }
        }

        double cosAngle = angle.cos();
        double sinAngle = angle.sin();

        LatLon c = sector.getCentroid();
        float cx = (float) c.getLongitude().degrees;
        float cy = (float) c.getLatitude().degrees;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int index = j * width + i;

                float x = lons[index];
                float y = lats[index];

                lons[index] = (float) ((x - cx) * cosAngle - (y - cy) * sinAngle + cx);
                lats[index] = (float) ((x - cx) * sinAngle + (y - cy) * cosAngle + cy);
            }
        }

        return new float[][]{lats, lons};
    }

    protected static ArrayList<LatLon> makeBorder(float[][] field, int width, int height, ArrayList<LatLon> latLons) {
        for (int i = 0; i < width; i++) {
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        }
        for (int i = 2 * width - 1; i < height * width; i += width) {
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        }
        for (int i = width * height - 2; i > width * (height - 1); i--) {
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        }
        for (int i = width * (height - 2); i > 0; i -= width) {
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        }

        return latLons;
    }

    private static double opacityIncrement = -0.1;

    public static void main(String[] args) {
        final ImageUtil.AlignedImage projectedImage;
        final String imageName;
        final BufferedImage testImage;
        final ArrayList<LatLon> latLons = new ArrayList<>();

        final AppFrame frame = start("GLIDER Test Application", GliderAppFrame.class);

        InputStream stream = null;
        try {
            stream = WWIO.openFileOrResourceStream(cloudImagePath, null);
            testImage = ImageIO.read(stream);
            long start = System.currentTimeMillis();
            float[][] field = makeField(corners, testImage.getWidth(), testImage.getHeight(), Angle.fromDegrees(15));
            makeBorder(field, testImage.getWidth(), testImage.getHeight(), latLons);
            projectedImage = GliderImage.alignImage(testImage, field[0], field[1]);
            System.out.printf("Image projected, %d ms\n", System.currentTimeMillis() - start);
            imageName = WWIO.getFilename(cloudImagePath);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            WWIO.closeStream(stream, cloudImagePath);
        }

        SwingUtilities.invokeLater(() -> {
            final GliderImage image = new GliderImage(imageName, projectedImage, 100);
            final GliderRegionOfInterest regionOfInterest = new GliderRegionOfInterest(latLons, Color.RED);
            image.addRegionOfInterest(regionOfInterest);

            final javax.swing.Timer timer = new javax.swing.Timer(1000, (ActionEvent evt) -> {
                try {
                    if (((GliderWorldWindow) ((GliderAppFrame) frame).getWwd()).getImages().isEmpty()) {
                        System.out.println("ADDING");
                        ((GliderWorldWindow) ((GliderAppFrame) frame).getWwd()).addImage(image);
                        image.releaseImageSource();
                    } else {
                        double opacity = image.getOpacity() + opacityIncrement;
                        image.setOpacity(opacity);
                        if (opacity <= 0.1 || opacity >= 1.0) {
                            opacityIncrement *= -1.0;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            timer.setRepeats(true);
            timer.start();
        });
    }
}
