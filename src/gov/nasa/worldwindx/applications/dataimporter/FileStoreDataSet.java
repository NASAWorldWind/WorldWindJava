/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import java.awt.*;
import java.io.*;

/**
 * Represents one data set within a World Wind filestore.
 *
 * @author tag
 * @version $Id: FileStoreDataSet.java 1180 2013-02-15 18:40:47Z tgaskins $
 */
public class FileStoreDataSet extends AVListImpl
{
    public static final String IMAGERY = "Imagery";
    public static final String ELEVATION = "Elevation";

    protected final String dataSetPath; // full path to data set in installed directory
    protected final String filestorePath; // full path to filestore root
    protected final String configFilePath; // full path to data set's config file

    /**
     * Constructs a new filestore data set.
     *
     * @param filestorePath  the full path to the filestore containing the data set.
     * @param dataSetPath    the full path to the data set in the specified filestore.
     * @param configFilePath the full path to the data set's config file.
     */
    public FileStoreDataSet(String filestorePath, String dataSetPath, String configFilePath)
    {
        if (filestorePath == null || dataSetPath == null || configFilePath == null)
        {
            String message = Logging.getMessage("nullValue.FileStorePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.filestorePath = filestorePath;
        this.dataSetPath = dataSetPath;
        this.configFilePath = configFilePath;

        this.setValue(AVKey.COLOR, ColorAllocator.getNextColor());

        this.attachMetadata();
    }

    public String getPath()
    {
        return dataSetPath;
    }

    public String getName()
    {
        return this.getStringValue(AVKey.DISPLAY_NAME);
//        // Strip all but the data set's root directory name.
//        String name = this.cacheRootPath == null ? this.getPath() : this.getPath().replace(
//            this.cacheRootPath.subSequence(0, this.cacheRootPath.length()), "".subSequence(0, 0));
//        return name.startsWith("/") ? name.substring(1) : name;
    }

    public String getDatasetType()
    {
        return this.getStringValue(AVKey.DATASET_TYPE);
    }

    public boolean isImagery()
    {
        return this.getDatasetType() != null && this.getDatasetType().equals(IMAGERY);
    }

    public boolean isElevation()
    {
        return this.getDatasetType() != null && this.getDatasetType().equals(ELEVATION);
    }

    public Sector getSector()
    {
        return (Sector) this.getValue(AVKey.SECTOR);
    }

    public Color getColor()
    {
        return (Color) this.getValue(AVKey.COLOR);
    }

    public long getSize()
    {
        return this.getSize(this.dataSetPath);
    }

    public long getSize(String path)
    {
        long size = 0;

        File pathFile = new File(path);
        File[] files = pathFile.listFiles();
        for (File file : files)
        {
            try
            {
                if (file.isDirectory())
                {
                    size += this.getSize(file.getPath());
                }
                else
                {
                    FileInputStream fis = new FileInputStream(file);
                    size += fis.available();
                    fis.close();
                }
            }
            catch (IOException e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileComputingSize", file.getAbsolutePath());
                Logging.logger().fine(message);
            }
        }

        return size;
    }

    protected void attachMetadata()
    {
        Document doc = null;

        try
        {
            doc = WWXML.openDocument(new File(this.configFilePath));
            doc = DataConfigurationUtils.convertToStandardDataConfigDocument(doc);
        }
        catch (WWRuntimeException e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "Exception reading data configuration", e);
        }

        if (doc == null)
            return;

        // This data configuration came from an existing file from disk, therefore we cannot guarantee that the
        // current version of World Wind's data installer produced it. This data configuration file may have been
        // created by a previous version of World Wind, or by another program. Set fallback values for any missing
        // parameters that World Wind needs to construct a Layer or ElevationModel from this data configuration.
        setFallbackParams(doc, this.configFilePath, this);

        Element domElement = doc.getDocumentElement();

        Sector sector = WWXML.getSector(domElement, "Sector", null);
        if (sector != null)
            this.setValue(AVKey.SECTOR, sector);

        String name = DataConfigurationUtils.getDataConfigDisplayName(domElement);
        this.setValue(AVKey.DISPLAY_NAME, name);

        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type.equalsIgnoreCase("Layer"))
            this.setValue(AVKey.DATASET_TYPE, IMAGERY);
        else if (type.equalsIgnoreCase("ElevationModel"))
            this.setValue(AVKey.DATASET_TYPE, ELEVATION);
    }

    protected static void setFallbackParams(Document dataConfig, String filename, AVList params)
    {
        XPath xpath = WWXML.makeXPath();
        Element domElement = dataConfig.getDocumentElement();

        // If the data configuration document doesn't define a cache name, then compute one using the file's path
        // relative to its file cache directory.
        String s = WWXML.getText(domElement, "DataCacheName", xpath);
        if (s == null || s.length() == 0)
            DataConfigurationUtils.getDataConfigCacheName(filename, params);

        // If the data configuration document doesn't define the data's extreme elevations,
        // provide default values using
        // the minimum and maximum elevations of Earth.
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type.equalsIgnoreCase("ElevationModel"))
        {
            if (WWXML.getDouble(domElement, "ExtremeElevations/@min", xpath) == null)
                params.setValue(AVKey.ELEVATION_MIN, Earth.ELEVATION_MIN);
            if (WWXML.getDouble(domElement, "ExtremeElevations/@max", xpath) == null)
                params.setValue(AVKey.ELEVATION_MAX, Earth.ELEVATION_MAX);
        }
    }

    /** Delete an installed data set. */
    public void delete()
    {
        removeDirectory(new File(this.dataSetPath));
    }

    protected static boolean removeDirectory(File directory)
    {
        if (directory == null)
            return false;

        if (!directory.exists())
            return true;

        if (!directory.isDirectory())
            return false;

        String[] list = directory.list();

        if (list != null) // Some JVMs return null for File.list() when the directory is empty.
        {
            for (int i = 0; i < list.length; i++)
            {
                File entry = new File(directory, list[i]);
                if (entry.isDirectory())
                {
                    if (!removeDirectory(entry))
                        return false;
                }
                else
                {
                    if (!entry.delete())
                        return false;
                }
            }
        }

        return directory.delete();
    }
}
