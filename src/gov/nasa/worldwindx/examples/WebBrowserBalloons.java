/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.*;

import java.awt.*;
import java.io.InputStream;

/**
 * Illustrates how to use WorldWind browser balloons to display HTML, JavaScript, and Flash content to the user in the
 * form of a screen-aligned balloon. There are two browser balloon types: <code>{@link ScreenBrowserBalloon}</code>
 * which displays a balloon at a point on the screen, and <code>{@link GlobeBrowserBalloon}</code> which displays a
 * balloon attached to a position on the Globe.
 * <p>
 * <strong>Browser Balloon Content</strong> <br> A balloon's HTML content is specified by calling <code>{@link
 * Balloon#setText(String)}</code> with a string containing either plain text or HTML + JavaScript. The balloon's visual
 * attributes are specified by calling <code>{@link Balloon#setAttributes(gov.nasa.worldwind.render.BalloonAttributes)}</code>
 * with an instance of <code>{@link BalloonAttributes}</code>.
 *
 * @author dcollins
 * @version $Id: WebBrowserBalloons.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class WebBrowserBalloons extends ApplicationTemplate
{
    protected static final String BROWSER_BALLOON_CONTENT_PATH
        = "gov/nasa/worldwindx/examples/data/BrowserBalloonExample.html";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HotSpotController hotSpotController;
        protected BalloonController balloonController;

        public AppFrame()
        {
            this.makeBrowserBalloon();

            // Add a controller to send input events to BrowserBalloons.
            this.hotSpotController = new HotSpotController(this.getWwd());
            // Add a controller to handle link and navigation events in BrowserBalloons.
            this.balloonController = new BalloonController(this.getWwd());

            // Size the WorldWindow to provide enough screen space for the BrowserBalloon, and center the WorldWindow
            // on the screen.
            Dimension size = new Dimension(1200, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void makeBrowserBalloon()
        {
            String htmlString = null;
            InputStream contentStream = null;

            try
            {
                // Read the URL content into a String using the default encoding (UTF-8).
                contentStream = WWIO.openFileOrResourceStream(BROWSER_BALLOON_CONTENT_PATH, this.getClass());
                htmlString = WWIO.readStreamToString(contentStream, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                WWIO.closeStream(contentStream, BROWSER_BALLOON_CONTENT_PATH);
            }

            if (htmlString == null)
                htmlString = Logging.getMessage("generic.ExceptionAttemptingToReadFile", BROWSER_BALLOON_CONTENT_PATH);

            Position balloonPosition = Position.fromDegrees(38.883056, -77.016389);

            // Create a Browser Balloon attached to the globe, and pointing at the NASA headquarters in Washington, D.C.
            // We use the balloon page's URL as its resource resolver to handle relative paths in the page content.
            AbstractBrowserBalloon balloon = new GlobeBrowserBalloon(htmlString, balloonPosition);
            // Size the balloon to provide enough space for its content.
            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null));
            balloon.setAttributes(attrs);

            // Create a placemark on the globe that the user can click to open the balloon.
            PointPlacemark placemark = new PointPlacemark(balloonPosition);
            placemark.setLabelText("Click to open balloon");
            // Associate the balloon with the placemark by setting AVKey.BALLOON. The BalloonController looks for this
            // value when an object is clicked.
            placemark.setValue(AVKey.BALLOON, balloon);

            // Create a layer to display the balloons.
            RenderableLayer layer = new RenderableLayer();
            layer.setName("Web Browser Balloons");
            layer.addRenderable(balloon);
            layer.addRenderable(placemark);
            // Add the layer to the ApplicationTemplate's layer panel.
            insertBeforePlacenames(getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the browser balloon is centered in the viewport.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 62);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -77);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 9500000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 45);

        start("WorldWind Web Browser Balloons", AppFrame.class);
    }
}
