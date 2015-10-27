/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.dataimporter;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.formats.rpf.RPFFrameFilename;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

import java.io.File;
import java.util.*;

/**
 * Finds installable data sets, either imagery or elevations.
 *
 * @author tag
 * @version $Id: FileSetFinder.java 2915 2015-03-20 16:48:43Z tgaskins $
 */
public class FileSetFinder
{
    FileSetMap fileSetMap = new FileSetMap();

    public FileSetMap getFileSetMap()
    {
        return this.fileSetMap;
    }

    public void findFileSets(File[] rootDirectories)
    {
        this.fileSetMap.clear();

        for (File rootDirectory : rootDirectories)
        {
            this.findFileSets(rootDirectory.getPath());
        }
    }

    protected void findFileSets(String rootDirectory)
    {
        File root = new File(rootDirectory);

        // Retrieve all the files that match the filter.
        String[] matches = WWIO.listDescendantFilenames(root, new FileSetFilter());

        // Build the file-set map. Each file set contains its associated files.
        for (String match : matches)
        {
            if (Thread.currentThread().isInterrupted())
                return;

            try
            {
                FileSet fileSet;
                File file = new File(root, match);

                RPFFrameFilename rpfFilename;
                try
                {
                    rpfFilename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
                    String code = rpfFilename.getDataSeriesCode();
                    if (code != null)
                    {
                        fileSet = this.fileSetMap.get(code);
                        if (fileSet == null)
                            this.fileSetMap.put(code, fileSet = new FileSetRPF(code));
                        fileSet.addFile(file);
                        continue;
                    }
                }
                catch (Exception e)
                {
                    // Just means it's not RPF, so keep going
                }

                // This code shows how to consolidate a collection of file sets, grouping them by suffix.
                String suffix = WWIO.getSuffix(file.getPath().toUpperCase());
                if (suffix != null)
                {
                    fileSet = this.fileSetMap.get(suffix);
                    if (fileSet == null)
                    {
                        this.fileSetMap.put(suffix, fileSet = new FileSet());
                        File parent = new File(file.getParent());
                        fileSet.setName(parent.getName());
                        fileSet.setDatasetType(suffix);
                    }
                    fileSet.addFile(file);
                    continue;
                }

                // Just treat it as its own fileset.
                fileSet = new FileSet();
                fileSet.setName(file.getName());
                fileSet.setDatasetType(WWIO.getSuffix(file.getPath().toUpperCase()));
                this.fileSetMap.put(file.getPath(), fileSet);
                fileSet.addFile(file);
            }
            catch (Exception e)
            {
                continue;
            }
        }

        // Attach metadata to the file sets.
        for (FileSet fileSet : this.fileSetMap.values())
        {
            this.attachMetadata(fileSet);
        }
    }

    public void attachMetadata(FileSet fileSet)
    {
        // Open the data set and extract metadata needed by the data installer panel.

        DataRasterReaderFactory readerFactory = DataInstaller.getReaderFactory();
        List<Sector> fileSectors = new ArrayList<Sector>(fileSet.getLength());

        Sector sector = null;
        for (File file : fileSet.getFiles())
        {
            AVList params = new AVListImpl();
            DataRasterReader reader = readerFactory.findReaderFor(file, params);
            if (reader == null)
            {
                Logging.logger().fine("No reader for " + file.getPath());
                continue;
            }

            try
            {
                reader.readMetadata(file, params);
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileReading", e.getMessage());
                Logging.logger().finest(message);
            }

            // Set the file set's pixel format and data type.
            if (fileSet.getDataType() == null)
            {
                String pixelFormat = params.getStringValue(AVKey.PIXEL_FORMAT);
                fileSet.setValue(AVKey.PIXEL_FORMAT, pixelFormat);

                if (AVKey.IMAGE.equals(pixelFormat))
                    fileSet.setDataType(DataInstaller.IMAGERY);
                else if (AVKey.ELEVATION.equals(pixelFormat))
                    fileSet.setDataType(DataInstaller.ELEVATION);
            }

            Sector fileSector = (Sector) params.getValue(AVKey.SECTOR);
            if (fileSector == null)
            {
                Logging.logger().fine("No sector for " + file.getPath());
                continue;
            }

            // Construct the file set's sector list.
            fileSectors.add(fileSector);

            // Compute the file set's overall sector.
            sector = sector == null ? fileSector : sector.union(fileSector);
        }

        if (sector != null)
        {
            fileSet.setValue(AVKey.SECTOR, sector);
//            fileSet.setValue(FileSet.SECTOR_LIST, fileSectors.toArray());
            fileSet.addSectorList(fileSectors.toArray());
        }
    }

    protected static class FileSetKey
    {
        protected String suffix;
        protected String dataType;
        protected File parentDirectory;

        public FileSetKey(File file, String dataType)
        {
            this.dataType =dataType;
            this.suffix = WWIO.getSuffix(file.getPath().toUpperCase());
            this.parentDirectory = file.getParentFile();
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            FileSetKey that = (FileSetKey) o;

            if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null)
                return false;
            if (parentDirectory != null ? !parentDirectory.equals(that.parentDirectory) : that.parentDirectory != null)
                return false;
            if (suffix != null ? !suffix.equals(that.suffix) : that.suffix != null)
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = suffix != null ? suffix.hashCode() : 0;
            result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
            result = 31 * result + (parentDirectory != null ? parentDirectory.hashCode() : 0);
            return result;
        }
    }

    public List<FileSet> consolidateFileSets(List<FileSet> fileSets)
    {
        if (fileSets.size() <= 1)
            return fileSets;

        FileSetMap map = new FileSetMap();
        List<FileSet> commonFilesets =  new ArrayList<FileSet>();

        for (FileSet fs : fileSets)
        {
            if (fs.getFiles().size() > 1)
            {
                commonFilesets.add(fs);
                continue;
            }

            File file = fs.getFiles().get(0);
            String dataType = fs.getDataType();
            FileSetKey key = new FileSetKey(file, dataType);

            FileSet consolidatedFileSet = map.get(key);
            if (consolidatedFileSet == null)
            {
                consolidatedFileSet = new FileSet();
                map.put(key, consolidatedFileSet);
                consolidatedFileSet.setDataType(fs.getDataType());
                consolidatedFileSet.setValue(AVKey.PIXEL_FORMAT, fs.getValue(AVKey.PIXEL_FORMAT));
                consolidatedFileSet.setSector(fs.getSector());
                commonFilesets.add(consolidatedFileSet);
            }

            consolidatedFileSet.addFile(file);
            consolidatedFileSet.setSector(consolidatedFileSet.getSector().union(fs.getSector()));
            consolidatedFileSet.addSectorList(fs.getSectorList());
        }

        return commonFilesets;
    }
}
