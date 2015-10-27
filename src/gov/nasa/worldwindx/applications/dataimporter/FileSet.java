/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Represents a collection of installable data.
 *
 * @author tag
 * @version $Id: FileSet.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileSet extends AVListImpl
{
    protected static final int MAX_FILES_FOR_PREVIEW_IMAGE = 20;
    protected static final int PREVIEW_IMAGE_SIZE = 1024;
    protected static final int ICON_IMAGE_SIZE = 32;

    public static final String FILE_SET_CODE = "gov.nasa.worldwindx.dataimport.FileSet.FileSetCode";
    public static final String FILE_SET_ABBREVIATION = "gov.nasa.worldwindx.dataimport.FileSet.FileSetAbbreviation";
    public static final String FILE_SET_SCALE = "gov.nasa.worldwindx.dataimport.FileSet.FileSetScale";
    public static final String FILE_SET_GSD = "gov.nasa.worldwindx.dataimport.FileSet.FileSetGSD";

    public static final String SECTOR_LIST = "SectorList";
    public static final String IMAGE_ICON = "ImageIcon";
    public static final String IMAGE_IN_PROGRESS = "ImageInProgress";

    List<File> files = new ArrayList<File>();

    // This thread pool is for preview image generation.
    static protected ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 1L, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>(200));

    public FileSet()
    {
        this.setValue(AVKey.COLOR, ColorAllocator.getNextColor());
    }

    public void clear()
    {
        Layer layer = (Layer) this.getValue(AVKey.LAYER);
        if (layer != null)
        {
            layer.dispose();
            this.removeKey(AVKey.LAYER);
        }

        this.removeKey(AVKey.IMAGE);
        this.removeKey(FileSet.IMAGE_ICON);
    }

    /**
     * Returns the number of files in the file set.
     *
     * @return the number of files in the file set.
     */
    public int getLength()
    {
        return this.files.size();
    }

    public void addFile(File file)
    {
        this.files.add(file);
    }

    public List<File> getFiles()
    {
        return this.files;
    }

    public boolean isImagery()
    {
        return DataInstaller.IMAGERY.equals(this.getDataType());
    }

    public boolean isElevation()
    {
        return DataInstaller.ELEVATION.equals(this.getDataType());
    }

    public String getDataType()
    {
        return this.getStringValue(AVKey.DATA_TYPE);
    }

    public void setDataType(String dataType)
    {
        this.setValue(AVKey.DATA_TYPE, dataType);
    }

    public String getName()
    {
        return this.getStringValue(AVKey.DISPLAY_NAME);
    }

    public void setName(String name)
    {
        if (name != null)
            this.setValue(AVKey.DISPLAY_NAME, name);
    }

    public String getDatasetType()
    {
        return this.getStringValue(AVKey.DATASET_TYPE);
    }

    public void setDatasetType(String datasetType)
    {
        if (datasetType != null)
            this.setValue(AVKey.DATASET_TYPE, datasetType);
    }

    public String getScale()
    {
        return this.getStringValue(FileSet.FILE_SET_SCALE);
    }

    public void setScale(String scale)
    {
        if (scale != null)
            this.setValue(FileSet.FILE_SET_SCALE, scale);
    }

    public Sector getSector()
    {
        return (Sector) this.getValue(AVKey.SECTOR);
    }

    public void setSector(Sector sector)
    {
        this.setValue(AVKey.SECTOR, sector);
    }

    public Object[] getSectorList()
    {
        return (Object[]) this.getValue(FileSet.SECTOR_LIST);
    }

    public void addSectorList(Object[] sectorList)
    {
        Object[] current = (Object[]) this.getValue(FileSet.SECTOR_LIST);

        if (current == null)
        {
            this.setValue(FileSet.SECTOR_LIST, sectorList);
            return;
        }

        List<Object> newList = new ArrayList<Object>(current.length + sectorList.length);
        newList.add(current);
        newList.add(sectorList);

        this.setValue(FileSet.SECTOR_LIST, newList.toArray());
    }

    public Color getColor()
    {
        return (Color) this.getValue(AVKey.COLOR);
    }

    public void setColor(Color color)
    {
        this.setValue(AVKey.COLOR, color);
    }

    public int getMaxFilesForPreviewImage()
    {
        return MAX_FILES_FOR_PREVIEW_IMAGE;
    }

    public ImageIcon getImageIcon()
    {
        if (this.getValue(IMAGE_ICON) != null)
            return (ImageIcon) this.getValue(IMAGE_ICON);

        return this.makeImageIcon();
    }

    public BufferedImage getImage()
    {
        if (!this.isImagery() || this.getLength() > this.getMaxFilesForPreviewImage())
            return null;

        if (this.getValue(AVKey.IMAGE) != null)
            return (BufferedImage) this.getValue(AVKey.IMAGE);

        this.makeImage();

        return null;
    }

    public void setImage(BufferedImage image)
    {
        this.setValue(AVKey.IMAGE, image);
        this.removeKey(IMAGE_IN_PROGRESS);

        this.firePropertyChange(new PropertyChangeEvent(this, AVKey.IMAGE, false, true));
    }

    /** Causes the preview image to be built. */
    protected void makeImage()
    {
        if (this.getValue(IMAGE_IN_PROGRESS) != null) // don't generate more than one image at a time for this data set
            return;

        this.setValue(IMAGE_IN_PROGRESS, true);

        FileSetPreviewImageGenerator tg =
            new FileSetPreviewImageGenerator(this, PREVIEW_IMAGE_SIZE, PREVIEW_IMAGE_SIZE);
        this.threadPoolExecutor.submit(tg);
    }

    /**
     * Causes the preview thumbnail to be built, or returns it if it's already available.
     *
     * @return the preview thumbnail, or null if it's not yet available.
     */
    protected ImageIcon makeImageIcon()
    {
        // The thumbnail is a reduced size image of the preview image.

        BufferedImage image = this.getImage();
        if (image == null)
            return null;

        BufferedImage iconImage = new BufferedImage(ICON_IMAGE_SIZE, ICON_IMAGE_SIZE, image.getType());

        Graphics2D g = (Graphics2D) iconImage.getGraphics();

        while (!g.drawImage(image, 0, 0, ICON_IMAGE_SIZE, ICON_IMAGE_SIZE, null))
        {
            continue;
        }

        g.dispose();

        ImageIcon imageIcon = new ImageIcon(iconImage);

        this.setValue(IMAGE_ICON, imageIcon);

        return imageIcon;
    }
}
