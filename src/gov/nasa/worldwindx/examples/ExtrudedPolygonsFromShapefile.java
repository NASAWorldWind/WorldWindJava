/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.util.VecBuffer;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.io.File;
import java.util.Map;

/**
 * Shows how to make extruded shapes from shapefiles.
 *
 * Use the File menu to open a shapefile containing pre-defined extruded shapes.
 *
 * @author tag
 * @version $Id: ExtrudedPolygonsFromShapefile.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ExtrudedPolygonsFromShapefile extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            this.makeMenu();
        }

        public class WorkerThread extends Thread
        {
            private File file;
            private WorldWindow wwd;

            public WorkerThread(File file, WorldWindow wwd)
            {
                this.file = file;
                this.wwd = wwd;
            }

            public void run()
            {
                Shapefile sf = new Shapefile(this.file);

                final RenderableLayer layer = new RenderableLayer();

                try
                {
                    while (sf.hasNext())
                    {
                        ShapefileRecord r = sf.nextRecord();
                        if (r == null)
                            continue;

                        //printShapefileInfo(r);

                        if (r.getNumberOfPoints() < 4)
                            continue;

                        layer.addRenderable(this.makeShape(r));
                    }
                }
                finally
                {
                    sf.close();
                }

                this.wwd.addSelectListener(new SelectListener()
                {
                    public void selected(SelectEvent event)
                    {
                        if (event.getTopObject() instanceof ExtrudedPolygon)
                            System.out.println("EXTRUDED POLYGON SELECTED");
                    }
                });
            }

            protected String[] heightKeys = new String[] {"height", "Height", "HEIGHT"};

            protected ExtrudedPolygon makeShape(ShapefileRecord record)
            {
                Double height = null;

                for (String key : heightKeys)
                {
                    Object o = record.getAttributes().getValue(key);
                    if (o != null)
                    {
                        height = Double.parseDouble(o.toString());
                    }
                }

                ExtrudedPolygon pgon = new ExtrudedPolygon();
                VecBuffer vb = record.getPointBuffer(0);
                pgon.setOuterBoundary(vb.getLocations(), height);

                return pgon;
            }
        }

        protected void makeMenu()
        {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ESRI Shapefiles", "shp"));

            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);
            JMenuItem openMenuItem = new JMenuItem(new AbstractAction("Open File...")
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    try
                    {
                        int status = fileChooser.showOpenDialog(AppFrame.this);
                        if (status == JFileChooser.APPROVE_OPTION)
                        {
                            Thread t = new WorkerThread(fileChooser.getSelectedFile(), getWwd());
                            t.start();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            fileMenu.add(openMenuItem);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Extruded Polygons from Shapefile", AppFrame.class);
    }

    public static void printShapefileInfo(ShapefileRecord r)
    {
        System.out.printf("%d, %s: %d parts, %d points", r.getRecordNumber(), r.getShapeType(),
            r.getNumberOfParts(), r.getNumberOfPoints());
        for (Map.Entry<String, Object> a : r.getAttributes().getEntries())
        {
            if (a.getKey() != null)
                System.out.printf(", %s", a.getKey());
            if (a.getValue() != null)
                System.out.printf(", %s", a.getValue());
        }
        System.out.println();

        System.out.print("\tAttributes: ");
        for (Map.Entry<String, Object> entry : r.getAttributes().getEntries())
        {
            System.out.printf("%s = %s, ", entry.getKey(), entry.getValue());
        }
        System.out.println();

        VecBuffer vb = r.getPointBuffer(0);
        for (LatLon ll : vb.getLocations())
        {
            System.out.printf("\t%f, %f\n", ll.getLatitude().degrees, ll.getLongitude().degrees);
        }
    }
}
