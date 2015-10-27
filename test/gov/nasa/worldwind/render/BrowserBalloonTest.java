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
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * @author pabercrombie
 * @version $Id: BrowserBalloonTest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BrowserBalloonTest extends ApplicationTemplate
{
    public static final String DEFAULT_FILE = "test/gov/nasa/worldwind/render/BrowserBalloonTest.html";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected GlobeBrowserBalloon balloon;
        protected HotSpotController hotSpotController;
        protected BalloonController balloonController;

        public AppFrame()
        {
            super(true, false, false); // Don't include the layer panel; we're using the on-screen layer tree.

            // Add a controller to handle input events on the layer selector and on browser balloons.
            this.hotSpotController = new HotSpotController(this.getWwd());

            // Add a controller to display balloons when placemarks are clicked. We override the method addDocumentLayer
            // so that loading a KML document by clicking a KML balloon link displays an entry in the on-screen layer
            // tree.
            this.balloonController = new BalloonController(this.getWwd());

            // Size the World Window to take up the space typically used by the layer panel.
            Dimension size = new Dimension(1000, 600);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);

            this.makeBalloon();

            makeMenu(this);
        }

        public void load(File file)
        {
            // Read the text file into a string using the default encoding (UTF-8).
            String htmlString = WWIO.readTextFile(file);
            this.balloon.setText(htmlString);

            // Configure the balloon to resolve relative paths against the current working directory. If the current
            // working directory cannot be converted to a file URL, this does nothing and the balloon does not resolve
            // relative paths.
            File cwd = new File(System.getProperty("user.dir"));
            this.balloon.setResourceResolver(WWIO.makeURL(cwd));
        }

        public void load(URL url)
        {
            // Read the URL content into a String using the default encoding (UTF-8).
            String htmlString = getURLContent(url, null);

            if (htmlString == null)
                htmlString = Logging.getMessage("URLRetriever.ErrorOpeningConnection", url.getHost());

            this.balloon.setResourceResolver(url);
            this.balloon.setText(htmlString);
            this.balloon.setVisible(true);
        }

        protected void makeBalloon()
        {
            this.balloon = new GlobeBrowserBalloon("", Position.fromDegrees(25, -100));
            this.balloon.setAlwaysOnTop(true);

            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(Size.fromPixels(640, 350));
            this.balloon.setAttributes(attrs);

            this.load(new File(DEFAULT_FILE));

            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(this.balloon);
            insertBeforeCompass(this.getWwd(), layer);
        }

        /**
         * Read content from a URL into a String.
         *
         * @param url     URL to download.
         * @param charset Charset to use to convert the downloaded bytes into a String. Pass {@code null} to use the
         *                default encoding, UTF-8.
         *
         * @return A string containing the URL content, or {@code null} if the download fails.
         */
        protected static String getURLContent(URL url, String charset)
        {
            try
            {
                ByteBuffer buffer = WWIO.readURLContentToBuffer(url);
                return WWIO.byteBufferToString(buffer, charset);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }

    protected static void makeMenu(final AppFrame appFrame)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("HTML File", "html"));

        JMenuBar menuBar = new JMenuBar();
        appFrame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem resetMenuItem = new JMenuItem(new AbstractAction("Reset")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    appFrame.load(new File(DEFAULT_FILE));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        fileMenu.add(resetMenuItem);

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File...")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    int status = fileChooser.showOpenDialog(appFrame);
                    if (status == JFileChooser.APPROVE_OPTION)
                    {
                        appFrame.load(fileChooser.getSelectedFile());
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(openFileMenuItem);

        JMenuItem openURLMenuItem = new JMenuItem(new AbstractAction("Open URL...")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    String status = JOptionPane.showInputDialog(appFrame, "URL");
                    if (!WWUtil.isEmpty(status))
                    {
                        appFrame.load(new URL(status));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        fileMenu.add(openURLMenuItem);
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the balloon is immediately visible.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 40);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -100);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 9500000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 20);

        start("BrowserBalloon Test", AppFrame.class);
    }
}
