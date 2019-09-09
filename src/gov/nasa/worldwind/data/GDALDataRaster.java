/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.gdal.GDALUtils;
import org.gdal.gdal.*;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.SpatialReference;

import java.awt.geom.*;
import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALDataRaster.java 2678 2015-01-24 22:07:39Z tgaskins $
 */

public class GDALDataRaster extends AbstractDataRaster implements Cacheable
{
    protected Dataset dsVRT = null;
    protected SpatialReference srs;
    protected File srcFile = null;
    protected GDAL.Area area = null;
    protected final Object usageLock = new Object(); // GDAL rasters are not thread-safe

    protected static final int DEFAULT_MAX_RASTER_SIZE_LIMIT = 3072;

    protected static int getMaxRasterSizeLimit()
    {
        return DEFAULT_MAX_RASTER_SIZE_LIMIT;
    }

    /**
     * Opens a data raster
     *
     * @param source the location of the local file, expressed as either a String path, a File, or a file URL.
     *
     * @throws IllegalArgumentException if the source is null
     * @throws FileNotFoundException    if the source (File) does not exist
     */
    public GDALDataRaster(Object source) throws IllegalArgumentException, FileNotFoundException
    {
        this(source, false);
    }

    /**
     * Opens a data raster
     *
     * @param source           the location of the local file, expressed as either a String path, a File, or a file
     *                         URL.
     * @param quickReadingMode if quick reading mode is enabled GDAL will not spend much time on heavy calculations,
     *                         like for example calculating Min/Max for entire elevation raster
     *
     * @throws IllegalArgumentException if the source is null
     * @throws FileNotFoundException    if the source (File) does not exist
     */
    public GDALDataRaster(Object source, boolean quickReadingMode)
        throws IllegalArgumentException, FileNotFoundException
    {
        super();

        File file = WWIO.getFileForLocalAddress(source);
        if (null == file)
        {
            String message;
            if (null != source)
            {
                message = Logging.getMessage("generic.UnrecognizedSourceType", source.getClass().getName());
            }
            else
            {
                message = Logging.getMessage("nullValue.SourceIsNull");
            }

            if (!quickReadingMode)
            {
                Logging.logger().finest(message);
            }

            throw new IllegalArgumentException(message);
        }

        this.srcFile = file;
        String name = this.srcFile.getName();
        if (null != name && name.length() > 0)
        {
            this.setValue(AVKey.DATASET_NAME, name);
            this.setValue(AVKey.DISPLAY_NAME, name);
            this.setValue(AVKey.FILE, this.srcFile);
        }

        Dataset ds = GDALUtils.open(file, quickReadingMode);
        if (ds == null)
        {
            String message = GDALUtils.getErrorMessage();
            if( WWUtil.isEmpty(message) )
                message = Logging.getMessage("nullValue.DataSetIsNull");

            if (!quickReadingMode)
            {
                Logging.logger().severe(message);
            }
            throw new IllegalArgumentException(message);
        }

        this.init(ds, quickReadingMode);
    }

    /**
     * Set a new extent to the data raster. This operation is mostly required for rasters that does not have a
     * georeferenced information. A new geo-transform matrix will be created. The coordinate system is set to
     * Geographic.
     *
     * @param sector A valid sector instance
     *
     * @throws IllegalArgumentException if the Sector is null
     */
    public void setSector(Sector sector) throws IllegalArgumentException
    {
        if (null == sector)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.hasKey(AVKey.COORDINATE_SYSTEM)
            || AVKey.COORDINATE_SYSTEM_UNKNOWN.equals(this.getValue(AVKey.COORDINATE_SYSTEM))
            )
        {
            this.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
        }

        this.srs = GDALUtils.createGeographicSRS();

        this.setValue(AVKey.SECTOR, sector);

        this.area = new GDAL.Area(this.srs, sector);
        this.setValue(AVKey.GDAL_AREA, this.area);

        if (this.width > 0)
        {
            double dx = sector.getDeltaLonDegrees() / this.width;
            this.setValue(AVKey.PIXEL_WIDTH, dx);
        }

        if (this.height > 0)
        {
            double dy = sector.getDeltaLatDegrees() / this.height;
            this.setValue(AVKey.PIXEL_WIDTH, dy);
        }

        if (this.dsVRT != null)
        {
            if (!"VRT".equalsIgnoreCase(this.dsVRT.GetDriver().getShortName()))
            {
                Driver vrt = gdal.GetDriverByName("VRT");
                if (null != vrt)
                {
                    this.dsVRT = vrt.CreateCopy("", this.dsVRT);
                }
            }

            double[] gt = GDALUtils.calcGetGeoTransform(sector, this.width, this.height);
            this.dsVRT.SetGeoTransform(gt);

            String error = GDALUtils.getErrorMessage();
            if (error != null)
            {
                String message = Logging.getMessage("gdal.InternalError", error);
                Logging.logger().severe(message);
//                throw new WWRuntimeException( message );
            }

            if (null != this.srs)
            {
                this.dsVRT.SetProjection(srs.ExportToWkt());
            }

            error = GDALUtils.getErrorMessage();
            if (error != null)
            {
                String message = Logging.getMessage("gdal.InternalError", error);
                Logging.logger().severe(message);
//                throw new WWRuntimeException( message );
            }

            this.srs = this.readSpatialReference(this.dsVRT);
        }
    }

    protected SpatialReference readSpatialReference(Dataset ds)
    {
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        String proj = ds.GetProjectionRef();
        if (null == proj || 0 == proj.length())
        {
            proj = ds.GetProjection();
        }

        if ((null == proj || 0 == proj.length()) && null != this.srcFile)
        {
            // check if there is a corresponding .PRJ (or .prj file)
            String pathToPrjFile = WWIO.replaceSuffix(this.srcFile.getAbsolutePath(), ".prj");
            File prjFile = new File(pathToPrjFile);

            if (!prjFile.exists() && Configuration.isUnixOS())
            {
                pathToPrjFile = WWIO.replaceSuffix(this.srcFile.getAbsolutePath(), ".PRJ");
                prjFile = new File(pathToPrjFile);
            }

            try
            {
                if (prjFile.exists())
                {
                    proj = WWIO.readTextFile(prjFile);
                }
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("generic.UnknownProjection", proj);
                Logging.logger().severe(message);
            }
        }

        SpatialReference srs = null;

        if (!WWUtil.isEmpty(proj))
        {
            srs = new SpatialReference(proj);
        }

        if ((null == srs || srs.IsLocal() == 1) && this.hasKey(AVKey.SPATIAL_REFERENCE_WKT))
        {
            proj = this.getStringValue(AVKey.SPATIAL_REFERENCE_WKT);
            srs = new SpatialReference(proj);
        }

        return srs;
    }

    public GDALDataRaster(Dataset ds) throws IllegalArgumentException
    {
        super();

        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.init(ds, false);
    }

    /**
     * Extracts metadata and sets next key/value pairs:
     * <p>
     * AVKey.WIDTH - the maximum width of the image
     * <p>
     * AVKey.HEIGHT - the maximum height of the image
     * <p>
     * AVKey.COORDINATE_SYSTEM - one of the next values: AVKey.COORDINATE_SYSTEM_SCREEN
     * AVKey.COORDINATE_SYSTEM_GEOGRAPHIC AVKey.COORDINATE_SYSTEM_PROJECTED
     * <p>
     * AVKey.SECTOR - in case of Geographic CS, contains a regular Geographic Sector defined by lat/lon coordinates of
     * corners in case of Projected CS, contains a bounding box of the area
     *
     * @param ds               GDAL's Dataset
     * @param quickReadingMode if quick reading mode is enabled GDAL will not spend much time on heavy calculations,
     *                         like for example calculating Min/Max for entire elevation raster
     */
    protected void init(Dataset ds, boolean quickReadingMode)
    {
        String srcWKT = null;

        AVList extParams = new AVListImpl();
        AVList params = new AVListImpl();
        GDALMetadata.extractExtendedAndFormatSpecificMetadata(ds, extParams, params);
        this.setValues(params);

        this.srs = this.readSpatialReference(ds);
        if (null != this.srs)
        {
            srcWKT = this.srs.ExportToWkt();
            this.setValue(AVKey.SPATIAL_REFERENCE_WKT, this.srs.ExportToWkt());
        }

        GDALUtils.extractRasterParameters(ds, this, quickReadingMode);

        this.dsVRT = ds;

        this.width = (Integer) this.getValue(AVKey.WIDTH);
        this.height = (Integer) this.getValue(AVKey.HEIGHT);

        Object o = this.getValue(AVKey.GDAL_AREA);
        this.area = (o != null && o instanceof GDAL.Area) ? (GDAL.Area) o : null;

        String proj = ds.GetProjectionRef();
        proj = (null == proj || 0 == proj.length()) ? ds.GetProjection() : proj;

        if ((null == proj || 0 == proj.length())
            && (srcWKT == null || 0 == srcWKT.length())
            && AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(this.getValue(AVKey.COORDINATE_SYSTEM))
            )
        {   // this is a case where file has GEODETIC GeoTranform matrix but does not have CS or PROJECTION data
            this.srs = GDALUtils.createGeographicSRS();
            srcWKT = this.srs.ExportToWkt();
            this.setValue(AVKey.SPATIAL_REFERENCE_WKT, this.srs.ExportToWkt());
        }

        // if the original dataset does NOT have projection information
        // AND the "srcWKT" is not empty (was taken from the accompanied .PRJ file)
        // we need to create a VRT dataset to be able to change/assign projection/geotransforms etc
        // most real drivers do not support overriding properties
        // However, JP2 files are 3 times slow when wrapped in the VRT dataset
        // therefore, we only wrap in to VRT when needed
        if ((null == proj || 0 == proj.length()) && (null != srcWKT && 0 < srcWKT.length()))
        {
            try
            {
                Driver vrt = gdal.GetDriverByName("VRT");
                if (null != vrt)
                {
                    Dataset dsWarp = vrt.CreateCopy("", ds);
                    dsWarp.SetProjection(srcWKT);
                    this.dsVRT = dsWarp;
                }
                else
                {
                    String message = Logging.getMessage("gdal.InternalError", GDALUtils.getErrorMessage());
                    Logging.logger().severe(message);
                    throw new WWRuntimeException(message);
                }
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public AVList getMetadata()
    {
        return this.copy();
    }

    public void drawOnTo(DataRaster canvas)
    {
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.DestinationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doDrawOnTo(canvas);
    }

    protected void doDrawOnTo(DataRaster canvas)
    {
        try
        {
            Sector imageSector = this.getSector();
            Sector canvasSector = canvas.getSector();
            Sector overlap = null;

            if ( null == imageSector || null == canvasSector || !this.intersects(canvasSector)
                || null == (overlap = imageSector.intersection(canvasSector)))
            {
                String msg = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", canvasSector, imageSector);
                Logging.logger().finest(msg);
                return;
            }

            // Compute the region of the destination raster to be be clipped by the specified clipping sector. If no
            // clipping sector is specified, then perform no clipping. We compute the clip region for the destination
            // raster because this region is used by AWT to limit which pixels are rasterized to the destination.
            java.awt.Rectangle clipRect = this.computeClipRect(overlap, canvas);
            if (null == clipRect || clipRect.width == 0 || clipRect.height == 0 )
            {
                return;
            }

            AVList params = canvas.copy();

            // copy parent raster keys/values; only those key/value will be copied that do exist in the parent raster
            // AND does NOT exist in the requested raster
            String[] keysToCopy = new String[] {
                AVKey.DATA_TYPE, AVKey.MISSING_DATA_SIGNAL, AVKey.BYTE_ORDER, AVKey.PIXEL_FORMAT, AVKey.ELEVATION_UNIT
            };
            WWUtil.copyValues(this, params, keysToCopy, false);

            DataRaster raster = this.doGetSubRaster(clipRect.width, clipRect.height, overlap, params );
            raster.drawOnTo(canvas);
        }
        catch (WWRuntimeException wwe)
        {
            Logging.logger().severe(wwe.getMessage());
        }
        catch (Exception e)
        {
            String message = this.composeExceptionReason(e);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected String composeExceptionReason(Throwable t)
    {
        StringBuilder sb = new StringBuilder();

        if (null != t.getMessage())
            sb.append(t.getMessage());
        else if (null != t.getCause())
            sb.append(t.getCause().getMessage());

        if (sb.length() > 0)
            sb.append(" : ");

        if (null != this.srcFile)
            sb.append(this.srcFile);

        return sb.toString();
    }

    public void dispose()
    {
        if (this.dsVRT != null)
        {
            this.dsVRT.delete();
            this.dsVRT = null;
        }

        this.clearList();

        if (this.srcFile != null)
        {
            this.srcFile = null;
        }

        this.srs = null;
    }

    protected Dataset createMaskDataset(int width, int height, Sector sector)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Driver drvMem = gdal.GetDriverByName("MEM");

        Dataset ds = drvMem.Create("roi-mask", width, height, 1, gdalconst.GDT_UInt32);
        Band band = ds.GetRasterBand(1);
        band.SetColorInterpretation(gdalconst.GCI_AlphaBand);
        double missingSignal = (double) GDALUtils.ALPHA_MASK;
        band.SetNoDataValue(missingSignal);
        band.Fill(missingSignal);

        if (null != sector)
        {
            SpatialReference t_srs = GDALUtils.createGeographicSRS();
            String t_srs_wkt = t_srs.ExportToWkt();
            ds.SetProjection(t_srs_wkt);

            ds.SetGeoTransform(GDALUtils.calcGetGeoTransform(sector, width, height));
        }

        return ds;
    }

    /**
     * The purpose of this method is to create the best suited dataset for the requested area. The dataset may contain
     * overviews, so instead of retrieving raster from the highest resolution source, we will compose a temporary
     * dataset from an overview, and/or we may clip only the requested area. This will accelerate reprojection (if
     * needed), because the reporjection will be done on much smaller dataset.
     *
     * @param reqWidth  width of the requested area
     * @param reqHeight height of the requested area
     * @param reqSector sector of the requested area
     *
     * @return a dataset with the best suitable raster for the request
     */
    protected Dataset getBestSuitedDataset(int reqWidth, int reqHeight, Sector reqSector)
    {
        if (reqWidth <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", reqWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (reqHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", reqHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (reqSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (null == this.dsVRT)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        if (null == this.area)
        {
            return this.dsVRT;
        }

        Sector extent = this.getSector();
        if (!this.intersects(reqSector))
        {
            String msg = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", reqSector, extent);
            Logging.logger().finest(msg);
            throw new WWRuntimeException(msg);
        }

        Object cs = this.getValue(AVKey.COORDINATE_SYSTEM);
        if (null == cs
            || (!AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs) && !AVKey.COORDINATE_SYSTEM_PROJECTED.equals(cs)))
        {
            String msg = (null == cs) ? "generic.UnspecifiedCoordinateSystem" : "generic.UnsupportedCoordinateSystem";
            String reason = Logging.getMessage(msg, cs);
            Logging.logger().finest(Logging.getMessage("generic.CannotCreateRaster", reason));
            return this.dsVRT;
        }

        double reqWidthRes = Math.abs(reqSector.getDeltaLonDegrees() / (double) reqWidth);
        double reqHeightRes = Math.abs(reqSector.getDeltaLatDegrees() / (double) reqHeight);

        int bandCount = this.dsVRT.getRasterCount();
        if (bandCount == 0)
        {
            return this.dsVRT;
        }

        Band firstBand = this.dsVRT.GetRasterBand(1);
        if (null == firstBand)
        {
            return this.dsVRT;
        }

        double[] gt = new double[6];
        this.dsVRT.GetGeoTransform(gt);

        boolean isNorthUpRaster = (gt[GDAL.GT_2_ROTATION_X] == 0d && gt[GDAL.GT_4_ROTATION_Y] == 0d);

        int bestOverviewIdx = -1;

        int srcHeight = this.getHeight();
        int srcWidth = this.getWidth();

        for (int i = 0; i < firstBand.GetOverviewCount(); i++)
        {
            Band overview = firstBand.GetOverview(i);
            if (null == overview)
            {
                continue;
            }

            int w = overview.getXSize();
            int h = overview.getYSize();

            if (0 == h || 0 == w)
            {
                continue;
            }

//          double ovWidthRes = Math.abs(extent.getDeltaLonDegrees() / (double) w);
            double ovHeightRes = Math.abs(extent.getDeltaLatDegrees() / (double) h);

            if (ovHeightRes <= reqHeightRes /*&& ovWidthRes <= reqWidthRes*/)
            {
                bestOverviewIdx = i;
                srcWidth = w;
                srcHeight = h;
                continue;
            }
            else
            {
                break;
            }
        }

        if (!isNorthUpRaster)
        {
            // It is a non-Northup oriented raster  (raster with rotation coefficients in the GT matrix)

            if (bestOverviewIdx == -1)
            {
                // no overviews, working with a full resolution raster
                srcHeight = this.getHeight();
                srcWidth = this.getWidth();

                for (int i = 0; true; i++)
                {
                    double scale = Math.pow(2, i);
                    double h = Math.floor(this.getHeight() / scale);
                    double w = Math.floor(this.getWidth() / scale);
                    double ovWidthRes = Math.abs(extent.getDeltaLonDegrees() / w);
                    double ovHeightRes = Math.abs(extent.getDeltaLatDegrees() / h);
                    if (ovHeightRes <= reqHeightRes && ovWidthRes <= reqWidthRes)
                    {
                        srcWidth = (int) w;
                        srcHeight = (int) h;
                        continue;
                    }
                    else
                    {
                        break;
                    }
                }
            }

            if (srcHeight > getMaxRasterSizeLimit() || srcWidth > getMaxRasterSizeLimit())
            {
                return this.dsVRT;
            }

            String msg = Logging.getMessage("gdal.UseOverviewRaster", srcWidth, srcHeight, reqWidth, reqHeight);
            Logging.logger().finest(msg);

            Dataset ds = this.buildNonNorthUpDatasetFromOverview(bestOverviewIdx, srcWidth, srcHeight);

            return (null != ds) ? ds : this.dsVRT;
        }

        if (bestOverviewIdx == -1)
        {
            // no overview was found, will use image's source bands
            srcWidth = this.getWidth();
            srcHeight = this.getHeight();
//            return this.dsVRT;
        }
        else
        {
            String msg = Logging.getMessage("gdal.UseOverviewRaster", srcWidth, srcHeight, reqWidth, reqHeight);
            Logging.logger().finest(msg);
        }

        return this.buildNorthUpDatasetFromOverview(reqSector, reqWidth, reqHeight, bestOverviewIdx, srcWidth,
            srcHeight);
    }

    protected Dataset buildNorthUpDatasetFromOverview(Sector reqSector, int reqWidth, int reqHeight,
        int bestOverviewIdx, int srcWidth, int srcHeight)
    {
        GDAL.Area cropArea = this.area.intersection(new GDAL.Area(this.srs, reqSector).getBoundingArea());
        if (null == cropArea)
        {
            String msg = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", reqSector, this.area);
            Logging.logger().finest(msg);
            throw new WWRuntimeException(msg);
        }

        java.awt.geom.AffineTransform geoToRaster = this.area.computeGeoToRasterTransform(srcWidth, srcHeight);

        java.awt.geom.Point2D geoPoint = new java.awt.geom.Point2D.Double();
        java.awt.geom.Point2D ul = new java.awt.geom.Point2D.Double();
        java.awt.geom.Point2D lr = new java.awt.geom.Point2D.Double();

        geoPoint.setLocation(cropArea.getMinX(), cropArea.getMaxY());
        geoToRaster.transform(geoPoint, ul);

        geoPoint.setLocation(cropArea.getMaxX(), cropArea.getMinY());
        geoToRaster.transform(geoPoint, lr);

        int clipXoff = (int) Math.floor(ul.getX());
        int clipYoff = (int) Math.floor(ul.getY());
        int clipWidth = (int) Math.floor(lr.getX() - ul.getX());
        int clipHeight = (int) Math.floor(lr.getY() - ul.getY());

        clipWidth = (clipWidth > srcWidth) ? srcWidth : clipWidth;
        clipHeight = (clipHeight > srcHeight) ? srcHeight : clipHeight;

        Driver drv = gdal.GetDriverByName("MEM");
        if (null == drv)
        {
            return this.dsVRT;
        }

        int bandCount = this.dsVRT.getRasterCount();
        if (bandCount == 0)
        {
            return this.dsVRT;
        }

        Band firstBand = this.dsVRT.GetRasterBand(1);
        if (null == firstBand)
        {
            return this.dsVRT;
        }

        int dataType = firstBand.GetRasterDataType();

        Dataset ds = drv.Create("cropped", reqWidth, reqHeight, bandCount, dataType);
        if (this.srs != null)
        {
            ds.SetProjection(this.srs.ExportToWkt());
        }

        double[] gt = new double[6];

        gt[GDAL.GT_0_ORIGIN_LON] = cropArea.getMinX();
        gt[GDAL.GT_3_ORIGIN_LAT] = cropArea.getMaxY();
        gt[GDAL.GT_1_PIXEL_WIDTH] = Math.abs((cropArea.getMaxX() - cropArea.getMinX()) / (double) reqWidth);
        gt[GDAL.GT_5_PIXEL_HEIGHT] = -Math.abs((cropArea.getMaxY() - cropArea.getMinY()) / (double) reqHeight);
        gt[GDAL.GT_2_ROTATION_X] = gt[GDAL.GT_4_ROTATION_Y] = 0d;

        ds.SetGeoTransform(gt);

        int size = reqWidth * reqHeight * (gdal.GetDataTypeSize(dataType) / 8);
        ByteBuffer data = ByteBuffer.allocateDirect(size);
        data.order(ByteOrder.nativeOrder());

        Double nodata = this.hasKey(AVKey.MISSING_DATA_SIGNAL) ? (Double) this.getValue(AVKey.MISSING_DATA_SIGNAL)
            : null;

        for (int i = 0; i < bandCount; i++)
        {
            Band srcBand = this.dsVRT.GetRasterBand(i + 1);
            if (null == srcBand)
            {
                continue;
            }

            Band ovBand = (bestOverviewIdx == -1) ? srcBand : srcBand.GetOverview(bestOverviewIdx);
            if (null == ovBand)
            {
                continue;
            }

            Band destBand = ds.GetRasterBand(i + 1);
            if (null != nodata)
            {
                destBand.SetNoDataValue(nodata);
            }

            int colorInt = srcBand.GetColorInterpretation();
            destBand.SetColorInterpretation(colorInt);
            if (colorInt == gdalconst.GCI_PaletteIndex)
            {
                destBand.SetColorTable(srcBand.GetColorTable());
            }

            data.rewind();
            ovBand.ReadRaster_Direct(clipXoff, clipYoff, clipWidth, clipHeight, reqWidth, reqHeight, dataType, data);

            data.rewind();
            destBand.WriteRaster_Direct(0, 0, reqWidth, reqHeight, dataType, data);
        }

        return ds;
    }

    protected Dataset buildNonNorthUpDatasetFromOverview(int bestOverviewIdx, int destWidth, int destHeight)
    {
        if (null == this.dsVRT)
        {
            return null;
        }

        Driver drv = gdal.GetDriverByName("MEM");
        if (null == drv)
        {
            return null;
        }

        Band firstBand = this.dsVRT.GetRasterBand(1);
        if (null == firstBand)
        {
            return null;
        }

        int bandCount = this.dsVRT.GetRasterCount();
        int destDataType = firstBand.GetRasterDataType();

        int size = destWidth * destHeight * (gdal.GetDataTypeSize(destDataType) / 8);
        ByteBuffer data = ByteBuffer.allocateDirect(size);
        data.order(ByteOrder.nativeOrder());

        Double nodata = this.hasKey(AVKey.MISSING_DATA_SIGNAL) ? (Double) this.getValue(AVKey.MISSING_DATA_SIGNAL)
            : null;

        Dataset ds = drv.Create("overview", destWidth, destHeight, bandCount, destDataType);
        if (this.srs != null)
        {
            ds.SetProjection(this.srs.ExportToWkt());
        }

        AffineTransform atxOverview = GDAL.getAffineTransform(this.dsVRT, destWidth, destHeight);

        double[] gt = new double[6];
        gt[GDAL.GT_0_ORIGIN_LON] = atxOverview.getTranslateX();
        gt[GDAL.GT_1_PIXEL_WIDTH] = atxOverview.getScaleX();
        gt[GDAL.GT_2_ROTATION_X] = atxOverview.getShearX();
        gt[GDAL.GT_3_ORIGIN_LAT] = atxOverview.getTranslateY();
        gt[GDAL.GT_4_ROTATION_Y] = atxOverview.getShearY();
        gt[GDAL.GT_5_PIXEL_HEIGHT] = atxOverview.getScaleY();

        ds.SetGeoTransform(gt);

        for (int i = 0; i < bandCount; i++)
        {
            Band srcBand = this.dsVRT.GetRasterBand(i + 1);
            if (null == srcBand)
            {
                continue;
            }

            Band ovBand = (bestOverviewIdx == -1) ? srcBand : srcBand.GetOverview(bestOverviewIdx);
            if (null == ovBand)
            {
                continue;
            }

            Band destBand = ds.GetRasterBand(i + 1);
            if (null != nodata)
            {
                destBand.SetNoDataValue(nodata);
            }

            int colorInt = srcBand.GetColorInterpretation();
            destBand.SetColorInterpretation(colorInt);
            if (colorInt == gdalconst.GCI_PaletteIndex)
            {
                destBand.SetColorTable(srcBand.GetColorTable());
            }

            data.rewind();
            ovBand.ReadRaster_Direct(0, 0, ovBand.getXSize(), ovBand.getYSize(),
                destWidth, destHeight, destDataType, data);

            data.rewind();
            destBand.WriteRaster_Direct(0, 0, destWidth, destHeight, destDataType, data);
        }

        return ds;
    }

    protected Dataset createCompatibleDataset(int width, int height, Sector sector, AVList destParams)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Driver drvMem = gdal.GetDriverByName("MEM");
        int srcNumOfBands = this.dsVRT.getRasterCount();
        Band srcBand1 = this.dsVRT.GetRasterBand(1);
        int bandDataType = srcBand1.getDataType();

        int[] bandColorInt = new int[] {gdalconst.GCI_RedBand, gdalconst.GCI_GreenBand,
            gdalconst.GCI_BlueBand, gdalconst.GCI_AlphaBand};

        int destNumOfBands = 4; // RGBA by default
        String pixelFormat = this.getStringValue(AVKey.PIXEL_FORMAT);
        String colorFormat = this.getStringValue(AVKey.IMAGE_COLOR_FORMAT);
        if (AVKey.ELEVATION.equals(pixelFormat))
        {
            destNumOfBands = 1;
            bandColorInt = new int[] {gdalconst.GCI_GrayIndex};
        }
        else if (AVKey.IMAGE.equals(pixelFormat) && AVKey.GRAYSCALE.equals(colorFormat))
        {
            bandColorInt = new int[] {gdalconst.GCI_GrayIndex, gdalconst.GCI_AlphaBand};
            destNumOfBands = 2; // Y + alpha
        }
        else if (AVKey.IMAGE.equals(pixelFormat) && AVKey.COLOR.equals(colorFormat))
        {
            bandColorInt = new int[] {
                gdalconst.GCI_RedBand, gdalconst.GCI_GreenBand, gdalconst.GCI_BlueBand, gdalconst.GCI_AlphaBand};

            if (AVKey.INT16.equals(this.getValue(AVKey.DATA_TYPE)) && srcNumOfBands > 3)
            {
                destNumOfBands = 3; // ignore 4th band which is some kind of infra-red
            }
            else if (srcNumOfBands >= 3)
            {
                destNumOfBands = 4; // RGBA
            }
            else
            {
                destNumOfBands = 1; // indexed 256 color image (like CADRG)
                bandColorInt = new int[] {gdalconst.GCI_PaletteIndex};
            }
        }

        Dataset ds = drvMem.Create("roi", width, height, destNumOfBands, bandDataType);

//        Double nodata = this.calcNoDataForDestinationRaster(destParams);
        Double missingDataSignal = AVListImpl.getDoubleValue( this, AVKey.MISSING_DATA_SIGNAL, null);
        Double minValue = AVListImpl.getDoubleValue( this, AVKey.ELEVATION_MIN, null );
        Double maxValue = AVListImpl.getDoubleValue( this, AVKey.ELEVATION_MAX, null );

        missingDataSignal = AVListImpl.getDoubleValue(destParams, AVKey.MISSING_DATA_REPLACEMENT, missingDataSignal);

        for (int i = 0; i < destNumOfBands; i++)
        {
            Band band = ds.GetRasterBand(i + 1);

            if ( missingDataSignal != null)
            {
                band.SetNoDataValue( missingDataSignal );
            }

            Band srcBand = (i < srcNumOfBands) ? this.dsVRT.GetRasterBand(i + 1) : null;

            int colorInt = gdalconst.GCI_Undefined;

            if (null != srcBand)
            {
                colorInt = srcBand.GetColorInterpretation();

                if (colorInt == gdalconst.GCI_Undefined)
                {
                    colorInt = bandColorInt[i];
                }

                band.SetColorInterpretation(colorInt);

                if (colorInt == gdalconst.GCI_PaletteIndex)
                {
                    band.SetColorTable(srcBand.GetColorTable());
                }
            }
            else
            {
                colorInt = bandColorInt[i];
                band.SetColorInterpretation(colorInt);
            }

            if (colorInt == gdalconst.GCI_AlphaBand)
            {
                band.Fill((double) GDALUtils.ALPHA_MASK);
            }

            if (null != missingDataSignal && colorInt == gdalconst.GCI_GrayIndex)
            {
                band.Fill(missingDataSignal);
                if( null != srcBand && minValue != null && maxValue != null )
                    band.SetStatistics( minValue, maxValue, 0d, 0d);
            }
        }

        if (null != sector)
        {
            SpatialReference t_srs = GDALUtils.createGeographicSRS();
            String t_srs_wkt = t_srs.ExportToWkt();
            ds.SetProjection(t_srs_wkt);

            ds.SetGeoTransform(GDALUtils.calcGetGeoTransform(sector, width, height));
        }

        String[] keysToCopy = new String[] {
            AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL,
            AVKey.RASTER_BAND_MAX_PIXEL_VALUE
        };

        WWUtil.copyValues(this, destParams, keysToCopy, true);

        return ds;
    }

    /**
     * Builds a writable data raster for the requested region of interest (ROI)
     *
     * @param params Required parameters are:
     *               <p> AVKey.HEIGHT as Integer, specifies a height of the desired ROI
     *               <p> AVKey.WIDTH as Integer, specifies a width of the desired ROI
     *               <p> AVKey.SECTOR as Sector, specifies an extent of the desired ROI
     *               <p>
     *               Optional parameters are:
     *               <p> AVKey.BAND_ORDER as array of integers, examples: for RGBA image: new int[] { 0, 1, 2, 3 }, or
     *               for  ARGB image: new int[] { 3, 0, 1, 2 } , or if you want only RGB bands of the RGBA image: new
     *               int[] {0, 1, 2 }, or only Intensity (4th) band of the specific aerial image: new int[] { 3 }
     *
     * @return A writable data raster: BufferedImageRaster (if the source dataset is imagery) or ByteBufferRaster (if
     *         the source dataset is elevations)
     */
    @Override
    public DataRaster getSubRaster(AVList params)
    {
        if (params.hasKey(AVKey.BANDS_ORDER))
        {
            GDALUtils.extractBandOrder(this.dsVRT, params);
        }

        // copy parent raster keys/values; only those key/value will be copied that do exist in the parent raster
        // AND does NOT exist in the requested raster
        String[] keysToCopy = new String[] {
            AVKey.DATA_TYPE, AVKey.MISSING_DATA_SIGNAL, AVKey.BYTE_ORDER, AVKey.PIXEL_FORMAT, AVKey.ELEVATION_UNIT
        };
        WWUtil.copyValues(this, params, keysToCopy, false);

        return super.getSubRaster(params);
    }

    protected DataRaster doGetSubRaster(int roiWidth, int roiHeight, Sector roiSector, AVList roiParams)
    {
        synchronized (this.usageLock)
        {
            Dataset destDS = null;
            Dataset maskDS = null;
            Dataset srcDS = null;
            DataRaster raster = null;

            try
            {
                gdal.PushErrorHandler("CPLQuietErrorHandler");

                roiParams = (null == roiParams) ? new AVListImpl() : roiParams;

                if (null != roiSector)
                {
                    roiParams.setValue(AVKey.SECTOR, roiSector);
                }

                roiParams.setValue(AVKey.WIDTH, roiWidth);
                roiParams.setValue(AVKey.HEIGHT, roiHeight);

                if (null == roiSector
                    || Sector.EMPTY_SECTOR.equals(roiSector)
                    || !this.hasKey(AVKey.COORDINATE_SYSTEM)
                    || AVKey.COORDINATE_SYSTEM_UNKNOWN.equals(this.getValue(AVKey.COORDINATE_SYSTEM))
                    )
                {
                    // return the entire data raster
                    return GDALUtils.composeDataRaster(this.dsVRT, roiParams);
                }

                destDS = this.createCompatibleDataset(roiWidth, roiHeight, roiSector, roiParams);

                String t_srs_wkt = destDS.GetProjection();
//            SpatialReference t_srs = new SpatialReference(t_srs_wkt);

                // check if image fully contains the ROI, in this case we do not need mask
                // if (null == this.area || null == this.srs || !this.area.contains(new GDAL.Area(this.srs, roiSector)))
                {
                    maskDS = this.createMaskDataset(roiWidth, roiHeight, roiSector);
                }

                long projTime = 0L, maskTime = 0L, cropTime = 0L, totalTime = System.currentTimeMillis();

                long start = System.currentTimeMillis();

                srcDS = this.getBestSuitedDataset(roiWidth, roiHeight, roiSector);
                if (srcDS == this.dsVRT)
                {
                    String message = Logging.getMessage("gdal.UseFullResolutionRaster", this.getWidth(),
                        this.getHeight(),
                        roiWidth, roiHeight);
                    Logging.logger().finest(message);
                }

                cropTime = System.currentTimeMillis() - start;

                start = System.currentTimeMillis();

                if (this.srs != null)
                {
                    String s_srs_wkt = this.srs.ExportToWkt();

                    gdal.ReprojectImage(srcDS, destDS, s_srs_wkt, t_srs_wkt, gdalconst.GRA_Bilinear);
                    projTime = System.currentTimeMillis() - start;

                    start = System.currentTimeMillis();
                    if (null != maskDS)
                    {
                        gdal.ReprojectImage(srcDS, maskDS, s_srs_wkt, t_srs_wkt, gdalconst.GRA_NearestNeighbour);
                    }
                    maskTime = System.currentTimeMillis() - start;
                }
                else
                {
                    gdal.ReprojectImage(srcDS, destDS);
                    projTime = System.currentTimeMillis() - start;

                    start = System.currentTimeMillis();
                    if (null != maskDS)
                    {
                        gdal.ReprojectImage(srcDS, maskDS);
                    }
                    maskTime = System.currentTimeMillis() - start;
                }

                String error = GDALUtils.getErrorMessage();
                if (error != null)
                {
                    String message = Logging.getMessage("gdal.InternalError", error);
                    Logging.logger().severe(message);
//            throw new WWRuntimeException( message );
                }

                if (null != maskDS)
                {
                    roiParams.setValue(AVKey.GDAL_MASK_DATASET, maskDS);
                }

                start = System.currentTimeMillis();
                raster = GDALUtils.composeDataRaster(destDS, roiParams);
                long composeTime = System.currentTimeMillis() - start;

                Logging.logger().finest("doGetSubRaster(): [" + roiWidth + "x" + roiHeight + "] - "
                    + " totalTime = " + (System.currentTimeMillis() - totalTime)
                    + " msec { Cropping = " + cropTime + " msec, Reprojection = " + projTime
                    + " msec, Masking = " + maskTime + " msec, Composing = " + composeTime + " msec }");
            }
            finally
            {
                gdal.PopErrorHandler();

                if (null != maskDS)
                {
                    maskDS.delete();
                }

                if (null != destDS && destDS != this.dsVRT)
                {
                    destDS.delete();
                }

                if (null != srcDS && srcDS != this.dsVRT)
                {
                    srcDS.delete();
                }
            }
            return raster;
        }
    }

    protected static Band findAlphaBand(Dataset ds)
    {
        if (null != ds)
        {
            // search backward
            int bandCount = ds.getRasterCount();
            for (int i = bandCount; i > 0; i--)
            {
                Band band = ds.GetRasterBand(i);
                if (band.GetColorInterpretation() == gdalconst.GCI_AlphaBand)
                {
                    return band;
                }
            }
        }
        return null;
    }

    protected static String convertAVListToString(AVList list)
    {
        if (null == list)
        {
            return "";
        }

        StringBuffer sb = new StringBuffer("{ ");
        Vector<String> keys = new Vector<String>();

        Set<Map.Entry<String, Object>> entries = list.getEntries();
        for (Map.Entry<String, Object> entry : entries)
        {
            keys.add(entry.getKey());
        }

        // sort keys
        Collections.sort(keys);

        for (String key : keys)
        {
            sb.append("\n").append(key).append("=").append(list.getValue(key));
        }
        sb.append("\n};");

        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "GDALDataRaster " + convertAVListToString(this);
    }

    protected boolean intersects(Sector reqSector)
    {
        if (null != reqSector)
        {
            if (null != this.area)
            {
                return (null != this.area.intersection(reqSector));
            }
            else
            {
                return reqSector.intersects(this.getSector());
            }
        }
        return false;
    }

    public long getSizeInBytes()
    {
        // this is empiric number; on average GDALDataRaster object takes between 30K-131KB
        // we need to provide a non-zero length to make sure it will be added to the memory cache
        return 2048L;
    }
}