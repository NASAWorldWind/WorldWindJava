/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * Demonstrates how to use the {@link SectorSelector} utility to save selected image or/and elevations to a GeoTIFF
 * file. Click "Start selection", select a region on the globe, and then click "Save elevations..." to export elevation
 * data for the selected region to a GeoTIFF, or "Save image..." to export imagery.
 *
 * @author Lado Garakanidze
 * @version $Id: ExportImageOrElevations.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ExportImageOrElevations extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private static final double MISSING_DATA_SIGNAL = (double) Short.MIN_VALUE;

        private JButton btnSaveElevations = null;
        private JButton btnSaveImage = null;
        private Sector selectedSector = null;
        private JFileChooser fileChooser = null;
        private SectorSelector selector;

        public AppFrame()
        {
            super(true, true, false);

            this.selector = new SectorSelector(getWwd());
            this.selector.setInteriorColor(new Color(1f, 1f, 1f, 0.1f));
            this.selector.setBorderColor(new Color(1f, 0f, 0f, 0.5f));
            this.selector.setBorderWidth(3);

            JPanel btnPanel = new JPanel(new GridLayout(5, 1, 0, 5));
            {
                JButton
                    // Set up a button to enable and disable region selection.
                    btn = new JButton(new EnableSelectorAction());
                btn.setToolTipText("Press Start then press and drag button 1 on globe");
                btnPanel.add(btn);

                btnSaveElevations = new JButton(new SaveElevationsAction());
                btnSaveElevations.setEnabled(false);
                btnSaveElevations.setToolTipText("Click the button to save elevations of the selected area");
                btnPanel.add(btnSaveElevations);

                btnSaveImage = new JButton(new SaveImageAction());
                btnSaveImage.setEnabled(false);
                btnSaveImage.setToolTipText("Click the button to save image of the selected area");
                btnPanel.add(btnSaveImage);
            }
            this.getControlPanel().add(btnPanel, BorderLayout.SOUTH);

            // Listen for changes to the sector selector's region. Could also just wait until the user finishes
            // and query the result using selector.getSector().
            this.selector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    Sector sector = (Sector) evt.getNewValue();
                    if (null != sector)
                    {
                        selectedSector = sector;
                        btnSaveElevations.setEnabled(true);
                        btnSaveImage.setEnabled(true);
                    }
                }
            });

            this.enableNAIPLayer();
        }

        private class SaveElevationsAction extends AbstractAction
        {
            public SaveElevationsAction()
            {
                super("Save elevations ...");
            }

            public void actionPerformed(ActionEvent e)
            {
                doSaveElevations();
            }
        }

        private class SaveImageAction extends AbstractAction
        {
            public SaveImageAction()
            {
                super("Save image ...");
            }

            public void actionPerformed(ActionEvent e)
            {
                doSaveImage();
            }
        }

        private class EnableSelectorAction extends AbstractAction
        {
            public EnableSelectorAction()
            {
                super("Start selection");
            }

            public void actionPerformed(ActionEvent e)
            {
                ((JButton) e.getSource()).setAction(new DisableSelectorAction());
                selector.enable();
            }
        }

        private class DisableSelectorAction extends AbstractAction
        {
            public DisableSelectorAction()
            {
                super("Clear selection");
            }

            public void actionPerformed(ActionEvent e)
            {
                selector.disable();
                btnSaveElevations.setEnabled(false);
                btnSaveImage.setEnabled(false);
                selectedSector = null;
                ((JButton) e.getSource()).setAction(new EnableSelectorAction());
            }
        }

        public static class GeotiffFileFilter extends javax.swing.filechooser.FileFilter
        {
            public boolean accept(File file)
            {
                if (file == null)
                {
                    String message = Logging.getMessage("nullValue.FileIsNull");
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }

                return file.isDirectory() || file.getName().toLowerCase().endsWith(".tif");
            }

            public String getDescription()
            {
                return "Geo-TIFF (tif)";
            }
        }

        private File selectDestinationFile(String title, String filename)
        {
            File destFile = null;

            if (this.fileChooser == null)
            {
                this.fileChooser = new JFileChooser();
                this.fileChooser.setCurrentDirectory(new File(Configuration.getUserHomeDirectory()));
                this.fileChooser.addChoosableFileFilter(new GeotiffFileFilter());
            }

            this.fileChooser.setDialogTitle(title);
            this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            this.fileChooser.setMultiSelectionEnabled(false);
            this.fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

            this.fileChooser.setName(filename);

            int status = this.fileChooser.showSaveDialog(null);
            if (status == JFileChooser.APPROVE_OPTION)
            {
                destFile = this.fileChooser.getSelectedFile();
                if (!destFile.getName().endsWith(".tif"))
                    destFile = new File(destFile.getPath() + ".tif");
            }
            return destFile;
        }

        public void doSaveElevations()
        {
            final File saveToFile = this.selectDestinationFile(
                "Select a destination GeoTiff file to save elevations", "elevation");

            if (saveToFile == null)
                return;

            final JOptionPane jop = new JOptionPane("Requesting elevations ...",
                JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);

            final JDialog jd = jop.createDialog(this.getRootPane().getTopLevelAncestor(), "Please wait...");
            jd.setModal(false);
            jd.setVisible(true);

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        int[] size = adjustSize(selectedSector, 512);
                        int width = size[0], height = size[1];

                        double[] elevations = readElevations(selectedSector, width, height);
                        if (null != elevations)
                        {
                            jd.setTitle("Writing elevations to " + saveToFile.getName());
                            writeElevationsToFile(selectedSector, width, height, elevations, saveToFile);
                            jd.setVisible(false);
                            JOptionPane.showMessageDialog(wwjPanel,
                                "Elevations saved into the " + saveToFile.getName());
                        }
                        else
                        {
                            jd.setVisible(false);
                            JOptionPane.showMessageDialog(wwjPanel,
                                "Attempt to save elevations to the " + saveToFile.getName() + " has failed.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        jd.setVisible(false);
                        JOptionPane.showMessageDialog(wwjPanel, e.getMessage());
                    }
                    finally
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                setCursor(Cursor.getDefaultCursor());
                                getWwd().redraw();
                                jd.setVisible(false);
                            }
                        });
                    }
                }
            });

            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            this.getWwd().redraw();
            t.start();
        }

        public void enableNAIPLayer()
        {
            LayerList list = this.getWwd().getModel().getLayers();
            ListIterator iterator = list.listIterator();
            while (iterator.hasNext())
            {
                Layer layer = (Layer) iterator.next();
                if (layer.getName().contains("NAIP"))
                {
                    layer.setEnabled(true);
                    break;
                }
            }
        }

        public void doSaveImage()
        {
            TiledImageLayer currentLayer = null;
            LayerList list = this.getWwd().getModel().getLayers();
            DrawContext dc = this.getWwd().getSceneController().getDrawContext();

            ListIterator iterator = list.listIterator();
            while (iterator.hasNext())
            {
                Object o = iterator.next();
                if (o instanceof TiledImageLayer)
                {
                    TiledImageLayer layer = (TiledImageLayer) o;
                    if (layer.isEnabled() && layer.isLayerActive(dc) && layer.isLayerInView(dc))
                    {
                        currentLayer = layer;
                    }
                }
            }

            if (null == currentLayer)
                return;

            final File saveToFile = this.selectDestinationFile("Select a destination GeoTiff file to save the image",
                "image");

            if (saveToFile == null)
                return;

            final TiledImageLayer activeLayer = currentLayer;

            final JOptionPane jop = new JOptionPane("Requesting image ...",
                JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {}, null);

            final JDialog jd = jop.createDialog(this.getRootPane().getTopLevelAncestor(), "Please wait...");
            jd.setModal(false);
            jd.setVisible(true);

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        BufferedImage image = captureImage(activeLayer, selectedSector, 2048);

                        if (null != image)
                        {
                            jd.setTitle("Writing image to " + saveToFile.getName());
                            writeImageToFile(selectedSector, image, saveToFile);
                            jd.setVisible(false);
                            JOptionPane.showMessageDialog(wwjPanel, "Image saved into the " + saveToFile.getName());
                        }
                        else
                        {
                            jd.setVisible(false);
                            JOptionPane.showMessageDialog(wwjPanel,
                                "Attempt to save image to the " + saveToFile.getName() + " has failed.");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        jd.setVisible(false);
                        JOptionPane.showMessageDialog(wwjPanel, e.getMessage());
                    }
                    finally
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                setCursor(Cursor.getDefaultCursor());
                                getWwd().redraw();
                                jd.setVisible(false);
                            }
                        });
                    }
                }
            });

            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            this.getWwd().redraw();
            t.start();
        }

        private int[] adjustSize(Sector sector, int desiredSize)
        {
            int[] size = new int[] {desiredSize, desiredSize};

            if (null != sector && desiredSize > 0)
            {
                LatLon centroid = sector.getCentroid();
                Angle dLat = LatLon.greatCircleDistance(new LatLon(sector.getMinLatitude(), sector.getMinLongitude()),
                    new LatLon(sector.getMaxLatitude(), sector.getMinLongitude()));
                Angle dLon = LatLon.greatCircleDistance(new LatLon(centroid.getLatitude(), sector.getMinLongitude()),
                    new LatLon(centroid.getLatitude(), sector.getMaxLongitude()));

                double max = Math.max(dLat.radians, dLon.radians);
                double min = Math.min(dLat.radians, dLon.radians);

                int minSize = (int) ((min == 0d) ? desiredSize : ((double) desiredSize * min / max));

                if (dLon.radians > dLat.radians)
                {
                    size[0] = desiredSize;      // width
                    size[1] = minSize;  // height
                }
                else
                {
                    size[0] = minSize;  // width
                    size[1] = desiredSize;      // height
                }
            }

            return size;
        }

        private BufferedImage captureImage(TiledImageLayer layer, Sector sector, int minSize)
            throws Exception
        {
            int[] size = this.adjustSize(sector, minSize);
            int width = size[0], height = size[1];

            String mimeType = layer.getDefaultImageFormat();
            if (layer.isImageFormatAvailable("image/png"))
                mimeType = "image/png";
            else if (layer.isImageFormatAvailable("image/jpg"))
                mimeType = "image/jpg";

            return layer.composeImageForSector(this.selectedSector, width, height, 1d, -1, mimeType, true, null, 30000);
        }

        private double[] readElevations(Sector sector, int width, int height)
        {
            double[] elevations;

            double latMin = sector.getMinLatitude().radians;
            double latMax = sector.getMaxLatitude().radians;
            double dLat = (latMax - latMin) / (double) (height - 1);

            double lonMin = sector.getMinLongitude().radians;
            double lonMax = sector.getMaxLongitude().radians;
            double dLon = (lonMax - lonMin) / (double) (width - 1);

            ArrayList<LatLon> latlons = new ArrayList<LatLon>(width * height);

            int maxx = width - 1, maxy = height - 1;

            double lat = latMin;
            for (int y = 0; y < height; y++)
            {
                double lon = lonMin;

                for (int x = 0; x < width; x++)
                {
                    latlons.add(LatLon.fromRadians(lat, lon));
                    lon = (x == maxx) ? lonMax : (lon + dLon);
                }

                lat = (y == maxy) ? latMax : (lat + dLat);
            }

            try
            {
                Globe globe = this.getWwd().getModel().getGlobe();
                ElevationModel model = globe.getElevationModel();

                elevations = new double[latlons.size()];
                Arrays.fill(elevations, MISSING_DATA_SIGNAL);

                // retrieve elevations
                model.composeElevations(sector, latlons, width, elevations);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                elevations = null;
            }

            return elevations;
        }

        private void writeImageToFile(Sector sector, BufferedImage image, File gtFile)
            throws IOException
        {
            AVList params = new AVListImpl();

            params.setValue(AVKey.SECTOR, sector);
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
            params.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

            GeotiffWriter writer = new GeotiffWriter(gtFile);
            try
            {
                writer.write(BufferedImageRaster.wrapAsGeoreferencedRaster(image, params));
            }
            finally
            {
                writer.close();
            }
        }

        private void writeElevationsToFile(Sector sector, int width, int height, double[] elevations, File gtFile)
            throws IOException
        {
            // These parameters are required for writeElevation
            AVList elev32 = new AVListImpl();

            elev32.setValue(AVKey.SECTOR, sector);
            elev32.setValue(AVKey.WIDTH, width);
            elev32.setValue(AVKey.HEIGHT, height);
            elev32.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
            elev32.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
            elev32.setValue(AVKey.DATA_TYPE, AVKey.FLOAT32);
            elev32.setValue(AVKey.ELEVATION_UNIT, AVKey.UNIT_METER);
            elev32.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);
            elev32.setValue(AVKey.MISSING_DATA_SIGNAL, MISSING_DATA_SIGNAL);

            ByteBufferRaster raster = (ByteBufferRaster) ByteBufferRaster.createGeoreferencedRaster(elev32);
            // copy elevation values to the elevation raster
            int i = 0;
            for (int y = height - 1; y >= 0; y--)
            {
                for (int x = 0; x < width; x++)
                {
                    raster.setDoubleAtPosition(y, x, elevations[i++]);
                }
            }

            GeotiffWriter writer = new GeotiffWriter(gtFile);
            try
            {
                writer.write(raster);
            }
            finally
            {
                writer.close();
            }
        }
    }

    public static void main(String[] args)
    {
        // zoom to San Francisco downtown
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 1000d);
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 37.7794d);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.4192d);

        ApplicationTemplate.start("WorldWind Exporting Surface Imagery and Elevations", AppFrame.class);
    }
}
