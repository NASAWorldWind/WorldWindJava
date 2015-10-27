/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.WWUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Test of BrowserBalloon sizing. This test creates several BrowserBalloons with different size configurations. The
 * balloons can be visually inspected to confirm that the sizes are correct.
 *
 * @author pabercrombie
 * @version $Id: BrowserBalloonSizeTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BrowserBalloonSizeTest extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HotSpotController hotSpotController;
        protected BalloonController balloonController;

        public AppFrame()
        {
            super(true, false, false); // Don't include the layer panel; we're using the on-screen layer tree.

            // Add a controller to handle input events on the layer selector and on browser balloons.
            this.hotSpotController = new HotSpotController(this.getWwd());

            // Add a controller to display balloons when placemarks are clicked.
            this.balloonController = new BalloonController(this.getWwd());

            // Size the World Window to take up the space typically used by the layer panel.
            Dimension size = new Dimension(1000, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);

            this.makeBalloons();
        }

        protected void makeBalloons()
        {
            // Create a layer to hold the balloons
            RenderableLayer layer = new RenderableLayer();
            insertBeforeCompass(this.getWwd(), layer);

            // Balloon sized to 200x200
            Balloon balloon = new GlobeBrowserBalloon("", Position.fromDegrees(0, -160));
            balloon.setAlwaysOnTop(true);

            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(Size.fromPixels(200, 200));
            balloon.setAttributes(attrs);

            balloon.setText("<table border='1'><tr><td>1) 200 pixels square.</td></tr></table>");
            layer.addRenderable(balloon);

            // Balloon sized to fit content
            balloon = new GlobeBrowserBalloon("", Position.fromDegrees(0, -120));
            balloon.setAlwaysOnTop(true);

            attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0, null, Size.NATIVE_DIMENSION, 0, null));
            balloon.setAttributes(attrs);

            balloon.setText(
                "<table border='1'><tr><td>2) This balloon is sized to fit it's contents. The text will wrap to the default balloon width, and the height will fit the balloon contents.</td></tr></table>");
            layer.addRenderable(balloon);

            // Balloon sized to content width, and 200 pixels tall
            balloon = new GlobeBrowserBalloon("", Position.fromDegrees(0, -100));
            balloon.setAlwaysOnTop(true);

            attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0, null, Size.EXPLICIT_DIMENSION, 200, AVKey.PIXELS));
            balloon.setAttributes(attrs);

            // Put the text for this balloon in a table with an explicit width. If the balloon just contains text, the
            // text will wrap to the default balloon width, which is not an interesting test of sizing the balloon to
            // the content width.
            balloon.setText(
                "<table width='400' height='200' border='1'><tr><td>3) This balloon is 200 pixels tall. It's width will fit the balloon contents, in this case a table that is 400 pixels wide.</td></tr></table>");
            layer.addRenderable(balloon);

            // Balloon sized to 100 pixels wide, and content height
            balloon = new GlobeBrowserBalloon("", Position.fromDegrees(0, -80));
            balloon.setAlwaysOnTop(true);

            attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.EXPLICIT_DIMENSION, 100, AVKey.PIXELS, Size.NATIVE_DIMENSION, 0, null));
            balloon.setAttributes(attrs);

            balloon.setText(
                "<table width='100' border='1'><tr><td>4) 100 pixels wide, fits height of content.</td></tr></table>");
            layer.addRenderable(balloon);

            // Balloon with aspect ratio size
            balloon = new GlobeBrowserBalloon("", Position.fromDegrees(-18, -137));
            balloon.setAlwaysOnTop(true);

            attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.EXPLICIT_DIMENSION, 300, AVKey.PIXELS, Size.MAINTAIN_ASPECT_RATIO, 0, null));
            balloon.setAttributes(attrs);

            balloon.setText(
                "<table width='200' height='100' border='1'><tr><td>5) This balloon is 300 pixels wide. It's height will maintain the aspect ratio of its content.</td></tr></table>");

            layer.addRenderable(balloon);

            // Balloon with aspect ratio size
            balloon = new GlobeBrowserBalloon("", Position.fromDegrees(-20, -90));
            balloon.setAlwaysOnTop(true);

            attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.MAINTAIN_ASPECT_RATIO, 0, null, Size.EXPLICIT_DIMENSION, 200, AVKey.PIXELS));
            balloon.setAttributes(attrs);

            balloon.setText(
                "<table width='300' height='200' border='1'><tr><td>6) This balloon is 200 pixels tall. It's width will maintain the aspect ratio of its content.</td></tr></table>");

            layer.addRenderable(balloon);

            // Balloon sized to fit content; content is replaced after 5 seconds.
            final Balloon textUpdateBalloon = new GlobeBrowserBalloon("", Position.fromDegrees(0, -60));

            attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0, null, Size.NATIVE_DIMENSION, 0, null));
            textUpdateBalloon.setAttributes(attrs);

            textUpdateBalloon.setText(
                "<table width='600' height='600' border='1'><tr><td>7) 600x600 table, replaced by a smaller 300x300 table after 5 seconds.</td></tr></table>");
            layer.addRenderable(textUpdateBalloon);

            Timer timer = new Timer(5000, new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    textUpdateBalloon.setText(
                        "<table width='300' height='300' border='1'><tr><td>7) 300x300 table, replaced the larger 600x600 table.</td></tr></table>");
                    getWwd().redraw();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the test balloons are immediately visible.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);

        start("BrowserBalloon Size Test", AppFrame.class);
    }
}
