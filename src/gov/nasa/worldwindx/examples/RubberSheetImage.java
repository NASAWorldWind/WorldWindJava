/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwindx.examples.util.*;
import gov.nasa.worldwind.formats.gcps.GCPSReader;
import gov.nasa.worldwind.formats.tab.TABRasterReader;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Demonstrates the use of the {@link gov.nasa.worldwind.render.SurfaceImage} class to create a "rubber sheet" image
 * that can be arbitrarily positioned, scaled and warped on the globe's surface using control points at the image's four
 * corners.
 *
 * @author tag
 * @version $Id: RubberSheetImage.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class RubberSheetImage extends ApplicationTemplate
{
    public static final String OPEN_IMAGE_FILE = "OpenImageFile";
    public static final String SET_IMAGE_OPACITY = "SetImageOpacity";
    public static final String TOGGLE_EDITING = "ToggleEditing";

    public static class AppFrame extends ApplicationTemplate.AppFrame implements ActionListener
    {
        private Controller controller;

        public AppFrame()
        {
            this.controller = new Controller(this);
            this.getWwd().addSelectListener(this.controller);

            this.initComponents();
        }

        private void initComponents()
        {
            Box controlBox = Box.createVerticalBox();
            controlBox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right
            {
                JButton openFileButton = new JButton("Open Image File...");
                openFileButton.setActionCommand(OPEN_IMAGE_FILE);
                openFileButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                openFileButton.addActionListener(this);
                controlBox.add(openFileButton);
                controlBox.add(Box.createVerticalStrut(10));

                JCheckBox toggleEditBox = new JCheckBox("Enable Editing", true);
                toggleEditBox.setActionCommand(TOGGLE_EDITING);
                toggleEditBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                toggleEditBox.addActionListener(this);
                controlBox.add(toggleEditBox);
                controlBox.add(Box.createVerticalStrut(10));

                JLabel label = new JLabel("Opacity");
                JSlider slider = new JSlider(0, 100, 100);
                slider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent event)
                    {

                        ActionEvent actionEvent = new ActionEvent(event.getSource(), 0, SET_IMAGE_OPACITY);
                        AppFrame.this.actionPerformed(actionEvent);
                    }
                });
                Box box = Box.createHorizontalBox();
                box.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                box.add(label);
                box.add(slider);
                controlBox.add(box);

                controlBox.add(Box.createVerticalGlue());
            }
            this.getControlPanel().add(controlBox, BorderLayout.SOUTH);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e == null)
                return;

            if (this.controller == null)
                return;

            this.controller.actionPerformed(e);
        }
    }

    public static class SurfaceImageEntry
    {
        private SurfaceImage surfaceImage;
        private SurfaceImageEditor editor;
        private RenderableLayer layer;

        public SurfaceImageEntry(WorldWindow wwd, SurfaceImage surfaceImage, String name)
        {
            this.surfaceImage = surfaceImage;
            this.editor = new SurfaceImageEditor(wwd, surfaceImage);

            this.layer = new RenderableLayer();
            this.layer.setName(name);
            this.layer.setPickEnabled(true);
            this.layer.addRenderable(surfaceImage);

            insertBeforePlacenames(wwd, this.layer);
        }

        public SurfaceImage getSurfaceImage()
        {
            return this.surfaceImage;
        }

        public SurfaceImageEditor getEditor()
        {
            return this.editor;
        }

        public RenderableLayer getLayer()
        {
            return this.layer;
        }
    }

    public static class Controller implements ActionListener, SelectListener
    {
        private AppFrame appFrame;
        private JFileChooser openFileChooser;
        private boolean isEditingEnabled = true;

        private ArrayList<SurfaceImageEntry> entryList = new ArrayList<SurfaceImageEntry>();

        public Controller(AppFrame appFrame)
        {
            this.appFrame = appFrame;
        }

        @SuppressWarnings( {"StringEquality"})
        public void actionPerformed(ActionEvent event)
        {
            String actionCommand = event.getActionCommand();
            if (WWUtil.isEmpty(actionCommand))
                return;

            if (actionCommand == OPEN_IMAGE_FILE)
            {
                this.doOpenImageFile();
            }
            else if (actionCommand == SET_IMAGE_OPACITY)
            {
                JSlider slider = (JSlider) event.getSource();
                this.doSetImageOpacity(slider.getValue() / 100.0);
            }
            else if (actionCommand == TOGGLE_EDITING)
            {
                AbstractButton button = (AbstractButton) event.getSource();
                this.enableEditing(button.isSelected());
            }
        }

        public void selected(SelectEvent e)
        {
            PickedObject topObject = e.getTopPickedObject();

            if (e.getEventAction().equals(SelectEvent.LEFT_PRESS))
            {
                if (topObject != null && !topObject.isTerrain() && topObject.getObject() instanceof SurfaceImage)
                {
                    SurfaceImageEntry entry = this.getEntryFor((SurfaceImage) topObject.getObject());
                    if (entry != null)
                    {
                        this.setSelectedEntry(entry);
                    }
                }
            }
        }

        protected void enableEditing(boolean enable)
        {
            this.isEditingEnabled = enable;

            for (SurfaceImageEntry entry : this.entryList)
            {
                entry.getLayer().setPickEnabled(enable);
                if (!enable)
                {
                    entry.getEditor().setArmed(false);
                }
            }
        }

        protected void addSurfaceImage(SurfaceImage surfaceImage, String name)
        {
            SurfaceImageEntry entry = new SurfaceImageEntry(this.appFrame.getWwd(), surfaceImage, name);
            this.entryList.add(entry);
            this.setSelectedEntry(entry);

            entry.getLayer().setPickEnabled(this.isEditingEnabled);
            if (!this.isEditingEnabled)
            {
                entry.getEditor().setArmed(false);
            }
        }

        protected SurfaceImageEntry getEntryFor(SurfaceImage surfaceImage)
        {
            for (SurfaceImageEntry entry : this.entryList)
            {
                if (entry.getSurfaceImage() == surfaceImage)
                {
                    return entry;
                }
            }

            return null;
        }

        protected void setSelectedEntry(SurfaceImageEntry selected)
        {
            for (SurfaceImageEntry entry : this.entryList)
            {
                if (entry != selected)
                {
                    if (entry.getEditor().isArmed())
                    {
                        entry.getEditor().setArmed(false);
                    }
                }
            }

            if (!selected.getEditor().isArmed())
            {
                selected.getEditor().setArmed(true);
            }
        }

        protected void doOpenImageFile()
        {
            if (this.openFileChooser == null)
            {
                this.openFileChooser = new JFileChooser(Configuration.getUserHomeDirectory());
                this.openFileChooser.setAcceptAllFileFilterUsed(false);
                this.openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                this.openFileChooser.setMultiSelectionEnabled(true);
                this.openFileChooser.addChoosableFileFilter(
                    new FileNameExtensionFilter("Images", ImageIO.getReaderFormatNames()));
            }

            int retVal = this.openFileChooser.showOpenDialog(this.appFrame);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            File[] files = this.openFileChooser.getSelectedFiles();
            this.loadFiles(files);
        }

        protected void doSetImageOpacity(double opacity)
        {
            for (SurfaceImageEntry entry : this.entryList)
            {
                entry.getSurfaceImage().setOpacity(opacity);
            }

            this.appFrame.getWwd().redraw();
        }

        protected void loadFiles(final File[] files)
        {
            this.appFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    for (File f : files)
                    {
                        loadFile(f);
                    }

                    appFrame.setCursor(null);
                }
            });
            thread.start();
        }

        protected void loadFile(final File file)
        {
            final BufferedImage image = this.readImage(file);
            if (image == null)
                return;

            final SurfaceImage si = this.createGeoreferencedSurfaceImage(file, image);
            if (si == null)
            {
                this.addNonGeoreferencedSurfaceImage(file, image, this.appFrame.getWwd());
                return;
            }

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    addSurfaceImage(si, file.getName());
                }
            });
        }

        protected BufferedImage readImage(File file)
        {
            try
            {
                return ImageIO.read(file);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        protected SurfaceImage createGeoreferencedSurfaceImage(File file, BufferedImage image)
        {
            try
            {
                SurfaceImage si = null;

                File tabFile = this.getAssociatedTABFile(file);
                if (tabFile != null)
                    si = this.createSurfaceImageFromTABFile(image, tabFile);

                if (si == null)
                {
                    File gcpsFile = this.getAssociatedGCPSFile(file);
                    if (gcpsFile != null)
                        si = this.createSurfaceImageFromGCPSFile(image, gcpsFile);
                }

                if (si == null)
                {
                    File[] worldFiles = this.getAssociatedWorldFiles(file);
                    if (worldFiles != null)
                        si = this.createSurfaceImageFromWorldFiles(image, worldFiles);
                }

                return si;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        public File getAssociatedTABFile(File file)
        {
            File tabFile = TABRasterReader.getTABFileFor(file);
            if (tabFile != null && tabFile.exists())
            {
                TABRasterReader reader = new TABRasterReader();
                if (reader.canRead(tabFile))
                    return tabFile;
            }

            return null;
        }

        public File getAssociatedGCPSFile(File file)
        {
            File gcpsFile = GCPSReader.getGCPSFileFor(file);
            if (gcpsFile != null && gcpsFile.exists())
            {
                GCPSReader reader = new GCPSReader();
                if (reader.canRead(gcpsFile))
                    return gcpsFile;
            }

            return null;
        }

        public File[] getAssociatedWorldFiles(File file)
        {
            try
            {
                File[] worldFiles = WorldFile.getWorldFiles(file);
                if (worldFiles != null && worldFiles.length > 0)
                    return worldFiles;
            }
            catch (Exception ignored)
            {
            }

            return null;
        }

        protected SurfaceImage createSurfaceImageFromWorldFiles(BufferedImage image, File[] worldFiles)
            throws java.io.IOException
        {
            AVList worldFileParams = new AVListImpl();
            WorldFile.decodeWorldFiles(worldFiles, worldFileParams);

            BufferedImage alignedImage = createPowerOfTwoImage(image.getWidth(), image.getHeight());
            Sector sector = ImageUtil.warpImageWithWorldFile(image, worldFileParams, alignedImage);

            return new SurfaceImage(alignedImage, sector);
        }

        protected SurfaceImage createSurfaceImageFromTABFile(BufferedImage image, File tabFile)
            throws java.io.IOException
        {
            TABRasterReader reader = new TABRasterReader();
            RasterControlPointList controlPoints = reader.read(tabFile);

            return this.createSurfaceImageFromControlPoints(image, controlPoints);
        }

        protected SurfaceImage createSurfaceImageFromGCPSFile(BufferedImage image, File gcpsFile)
            throws java.io.IOException
        {
            GCPSReader reader = new GCPSReader();
            RasterControlPointList controlPoints = reader.read(gcpsFile);

            return this.createSurfaceImageFromControlPoints(image, controlPoints);
        }

        protected SurfaceImage createSurfaceImageFromControlPoints(BufferedImage image,
            RasterControlPointList controlPoints) throws java.io.IOException
        {
            int numControlPoints = controlPoints.size();
            Point2D[] imagePoints = new Point2D[numControlPoints];
            LatLon[] geoPoints = new LatLon[numControlPoints];

            for (int i = 0; i < numControlPoints; i++)
            {
                RasterControlPointList.ControlPoint p = controlPoints.get(i);
                imagePoints[i] = p.getRasterPoint();
                geoPoints[i] = p.getWorldPointAsLatLon();
            }

            BufferedImage destImage = createPowerOfTwoImage(image.getWidth(), image.getHeight());
            Sector sector = ImageUtil.warpImageWithControlPoints(image, imagePoints, geoPoints, destImage);

            return new SurfaceImage(destImage, sector);
        }

        protected void addNonGeoreferencedSurfaceImage(final File file, final BufferedImage image,
            final WorldWindow wwd)
        {
            if (!SwingUtilities.isEventDispatchThread())
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        addNonGeoreferencedSurfaceImage(file, image, wwd);
                    }
                });
            }
            else
            {
                StringBuilder message = new StringBuilder();
                message.append("Unable to find geographic coordinates for: ");
                message.append("\"").append(file.getPath()).append("\"");
                message.append("\n");
                message.append("Open image anyway?");

                int retVal = JOptionPane.showConfirmDialog(this.appFrame, message, null, JOptionPane.YES_NO_OPTION);
                if (retVal != JOptionPane.YES_OPTION)
                    return;

                Position position = ShapeUtils.getNewShapePosition(wwd);
                double lat = position.getLatitude().radians;
                double lon = position.getLongitude().radians;
                double sizeInMeters = ShapeUtils.getViewportScaleFactor(wwd);
                double arcLength = sizeInMeters / wwd.getModel().getGlobe().getRadiusAt(position);
                Sector sector = Sector.fromRadians(lat - arcLength, lat + arcLength, lon - arcLength, lon + arcLength);

                BufferedImage powerOfTwoImage = createPowerOfTwoScaledCopy(image);

                this.addSurfaceImage(new SurfaceImage(powerOfTwoImage, sector), file.getName());
            }
        }
    }

    protected static BufferedImage createPowerOfTwoImage(int minWidth, int minHeight)
    {
        return new BufferedImage(WWMath.powerOfTwoCeiling(minWidth), WWMath.powerOfTwoCeiling(minHeight),
            BufferedImage.TYPE_INT_ARGB);
    }

    protected static BufferedImage createPowerOfTwoScaledCopy(BufferedImage image)
    {
        if (WWMath.isPowerOfTwo(image.getWidth()) && WWMath.isPowerOfTwo(image.getHeight()))
            return image;

        BufferedImage powerOfTwoImage = createPowerOfTwoImage(image.getWidth(), image.getHeight());
        ImageUtil.getScaledCopy(image, powerOfTwoImage);
        return powerOfTwoImage;
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Rubber Sheet Image", RubberSheetImage.AppFrame.class);
    }
}
