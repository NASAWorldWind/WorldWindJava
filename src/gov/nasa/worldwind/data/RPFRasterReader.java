/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.nitfs.*;
import gov.nasa.worldwind.formats.rpf.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: RPFRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFRasterReader extends AbstractDataRasterReader
{
    public RPFRasterReader()
    {
        super("RPF Imagery");
    }

    public boolean canRead(Object source, AVList params)
    {
        if (source == null)
            return false;

        // RPF imagery cannot be identified by a small set of suffixes or mime types, so we override the standard
        // suffix comparison behavior here.

        return this.doCanRead(source, params);
    }

    protected boolean doCanRead(Object source, AVList params)
    {
        if (!(source instanceof java.io.File))
            return false;

        java.io.File file = (java.io.File) source;
        String filename = file.getName().toUpperCase();

        boolean canRead = RPFFrameFilename.isFilename(filename);

        if (canRead && null != params && !params.hasKey(AVKey.PIXEL_FORMAT))
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);

        return canRead;
    }

    protected DataRaster[] doRead(Object source, AVList params) throws java.io.IOException
    {
        if (!(source instanceof java.io.File))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        java.io.File file = (java.io.File) source;
        RPFFrameFilename filename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
        if (filename.getZoneCode() == '9' || filename.getZoneCode() == 'J')
        {
            return this.readPolarImage(source, filename);
        }
        else
        {
            return this.readNonPolarImage(source, params);
        }
    }

    protected void doReadMetadata(Object source, AVList params) throws java.io.IOException
    {
        if (!(source instanceof java.io.File))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        java.io.File file = (java.io.File) source;
        RPFImageFile rpfFile = null;

        Object width = params.getValue(AVKey.WIDTH);
        Object height = params.getValue(AVKey.HEIGHT);
        if (width == null || height == null || !(width instanceof Integer) || !(height instanceof Integer))
        {
            rpfFile = RPFImageFile.load(file);
            this.readFileSize(rpfFile, params);
        }

        Object sector = params.getValue(AVKey.SECTOR);
        if (sector == null || !(sector instanceof Sector))
            this.readFileSector(file, rpfFile, params);

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
    }

    private DataRaster[] readNonPolarImage(Object source, AVList params) throws java.io.IOException
    {
        // TODO: break the raster along the international dateline, if necessary
        // Nonpolar images need no special processing. We convert it to a compatible image type to improve performance.

        java.io.File file = (java.io.File) source;
        RPFImageFile rpfFile = RPFImageFile.load(file);

        java.awt.image.BufferedImage image = rpfFile.getBufferedImage();
        image = ImageUtil.toCompatibleImage(image);

        // If the data source doesn't already have all the necessary metadata, then we attempt to read the metadata.
        Object o = (params != null) ? params.getValue(AVKey.SECTOR) : null;
        if (o == null || !(o instanceof Sector))
        {
            AVList values = new AVListImpl();
            this.readFileSector(file, rpfFile, values);
            o = values.getValue(AVKey.SECTOR);
        }

        DataRaster raster = new BufferedImageRaster((Sector) o, image);
        return new DataRaster[] {raster};
    }

    private DataRaster[] readPolarImage(Object source, RPFFrameFilename filename) throws java.io.IOException
    {
        java.io.File file = (java.io.File) source;
        RPFImageFile rpfFile = RPFImageFile.load(file);

        java.awt.image.BufferedImage image = rpfFile.getBufferedImage();

        // This is a polar image. We must project it's raster and bounding sector into Geographic/WGS84.
        RPFDataSeries ds = RPFDataSeries.dataSeriesFor(filename.getDataSeriesCode());
        RPFFrameTransform tx = RPFFrameTransform.createFrameTransform(filename.getZoneCode(),
            ds.rpfDataType, ds.scaleOrGSD);
        RPFFrameTransform.RPFImage[] images = tx.deproject(filename.getFrameNumber(), image);

        DataRaster[] rasters = new DataRaster[images.length];
        for (int i = 0; i < images.length; i++)
        {
            java.awt.image.BufferedImage compatibleImage = ImageUtil.toCompatibleImage(images[i].getImage());
            rasters[i] = new BufferedImageRaster(images[i].getSector(), compatibleImage);
        }
        return rasters;
    }

    private void readFileSize(RPFImageFile rpfFile, AVList values)
    {
        int width = rpfFile.getImageSegment().numSignificantCols;
        int height = rpfFile.getImageSegment().numSignificantRows;
        values.setValue(AVKey.WIDTH, width);
        values.setValue(AVKey.HEIGHT, height);
    }

    private void readFileSector(java.io.File file, RPFImageFile rpfFile, AVList values)
    {
        // We'll first attempt to compute the Sector, if possible, from the filename (if it exists) by using
        // the conventions for CADRG and CIB filenames. It has been observed that for polar frame files in
        // particular that coverage information in the file itself is sometimes unreliable.
        Sector sector = this.sectorFromFilename(file);
        // If the sector cannot be computed from the filename, then get it from the RPF file headers.
        if (sector == null)
            sector = this.sectorFromHeader(file, rpfFile);

        values.setValue(AVKey.SECTOR, sector);
    }

    private Sector sectorFromFilename(java.io.File file)
    {
        Sector sector = null;
        try
        {
            // Parse the filename, using the conventions for CADRG and CIB filenames.
            RPFFrameFilename rpfFilename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
            // Get the dataseries associated with that code.
            RPFDataSeries ds = RPFDataSeries.dataSeriesFor(rpfFilename.getDataSeriesCode());
            // If the scale or GSD associated with the dataseries is valid, then proceed computing the georeferencing
            // information from the filename. Otherwise return null.
            if (ds.scaleOrGSD > 0d)
            {
                // Create a transform to compute coverage information.
                RPFFrameTransform tx = RPFFrameTransform.createFrameTransform(
                    rpfFilename.getZoneCode(), ds.rpfDataType, ds.scaleOrGSD);
                // Get coverage information from the transform.
                sector = tx.computeFrameCoverage(rpfFilename.getFrameNumber());
            }
        }
        catch (Exception e)
        {
            // Computing the file's coverage failed. Log the condition and return null.
            // This at allows the coverage to be re-computed at a later time.
            String message = String.format("Exception while computing file sector: %s", file);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            sector = null;
        }
        return sector;
    }

    private Sector sectorFromHeader(java.io.File file, RPFFile rpfFile)
    {
        Sector sector;
        try
        {
            if (rpfFile == null)
                rpfFile = RPFImageFile.load(file);

            NITFSImageSegment imageSegment = (NITFSImageSegment) rpfFile.getNITFSSegment(
                NITFSSegmentType.IMAGE_SEGMENT);
            RPFFrameFileComponents comps = imageSegment.getUserDefinedImageSubheader().getRPFFrameFileComponents();
            Angle minLat = comps.swLowerleft.getLatitude();
            Angle maxLat = comps.neUpperRight.getLatitude();
            Angle minLon = comps.swLowerleft.getLongitude();
            Angle maxLon = comps.neUpperRight.getLongitude();
            // This sector spans the longitude boundary. In order to render this sector,
            // we must adjust the longitudes such that minLon<maxLon.
            if (Angle.crossesLongitudeBoundary(minLon, maxLon))
            {
                if (minLon.compareTo(maxLon) > 0)
                {
                    double degrees = 360 + maxLon.degrees;
                    maxLon = Angle.fromDegrees(degrees);
                }
            }
            sector = new Sector(minLat, maxLat, minLon, maxLon);
        }
        catch (Exception e)
        {
            // Computing the file's coverage failed. Log the condition and return null.
            // This at allows the coverage to be re-computed at a later time.
            String message = String.format("Exception while getting file sector: %s", file);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            sector = null;
        }
        return sector;
    }
}
