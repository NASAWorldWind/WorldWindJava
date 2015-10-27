/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Illustrates how to import ESRI Shapefiles into World Wind. This uses a <code>{@link ShapefileLayerFactory}</code> to
 * parse a Shapefile's contents and convert the shapefile into an equivalent World Wind shape. This provides examples of
 * importing a Shapefile on the local hard drive and importing a Shapefile at a remote URL.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileViewer.java 3212 2015-06-18 02:45:56Z tgaskins $
 */
public class ShapefileViewer extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
        implements ShapefileLayerFactory.CompletionCallback
    {
        protected RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

        public AppFrame()
        {
            makeMenu(this);
        }

        public void loadShapefile(Object source)
        {
            this.randomAttrs.nextAttributes(); // display each shapefile in different attributes

            ShapefileLayerFactory factory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(
                AVKey.SHAPEFILE_LAYER_FACTORY);
            factory.setNormalPointAttributes(this.randomAttrs.asPointAttributes());
            factory.setNormalShapeAttributes(this.randomAttrs.asShapeAttributes());
            factory.createFromShapefileSource(source, this); // add the layer in the completion callback

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        @Override
        public void completion(final Object result)
        {
            if (!SwingUtilities.isEventDispatchThread())
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        completion(result);
                    }
                });
                return;
            }

            Layer layer = (Layer) result;
            layer.setName(WWIO.getFilename(layer.getName())); // convert the layer name to the source's filename
            this.getWwd().getModel().getLayers().add(layer);

            Sector sector = (Sector) layer.getValue(AVKey.SECTOR);
            if (sector != null)
            {
                ExampleUtil.goTo(this.getWwd(), sector);
            }

            this.setCursor(null);
        }

        @Override
        public void exception(Exception e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
        }
    }

    protected static void makeMenu(final AppFrame appFrame)
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Shapefile", "shp"));
        fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[1]);

        JMenuBar menuBar = new JMenuBar();
        appFrame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File...")
        {
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    int status = fileChooser.showOpenDialog(appFrame);
                    if (status == JFileChooser.APPROVE_OPTION)
                    {
                        for (File file : fileChooser.getSelectedFiles())
                        {
                            appFrame.loadShapefile(file);
                        }
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
                        appFrame.loadShapefile(status.trim());
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
        start("World Wind Shapefile Viewer", AppFrame.class);
    }
}
