/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.formats.tiff.GeoTiff;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Calendar;

/**
 * @author dcollins
 * @version $Id: BufferedImageRaster.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BufferedImageRaster extends AbstractDataRaster implements Cacheable, Disposable
{
    private java.awt.image.BufferedImage bufferedImage;
    private java.awt.Graphics2D g2d;

    public BufferedImageRaster(Sector sector, java.awt.image.BufferedImage bufferedImage)
    {
        this(sector, bufferedImage, null);
    }

    public BufferedImageRaster(Sector sector, java.awt.image.BufferedImage bufferedImage, AVList list)
    {
        super((null != bufferedImage) ? bufferedImage.getWidth() : 0,
            (null != bufferedImage) ? bufferedImage.getHeight() : 0,
            sector, list);

        if (bufferedImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.bufferedImage = bufferedImage;
    }

    public BufferedImageRaster(int width, int height, int transparency, Sector sector)
    {
        super(width, height, sector);

        if (width < 1)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (height < 1)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.bufferedImage = ImageUtil.createCompatibleImage(width, height, transparency);
    }

    public java.awt.image.BufferedImage getBufferedImage()
    {
        return this.bufferedImage;
    }

    public java.awt.Graphics2D getGraphics()
    {
        if (this.g2d == null)
        {
            this.g2d = this.bufferedImage.createGraphics();
            // Enable bilinear interpolation.
            this.g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        return g2d;
    }

    public void drawOnTo(DataRaster canvas)
    {
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.DestinationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(canvas instanceof BufferedImageRaster))
        {
            String message = Logging.getMessage("DataRaster.IncompatibleRaster", canvas);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doDrawOnTo((BufferedImageRaster) canvas);
    }

    public void fill(java.awt.Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.awt.Graphics2D g2d = this.getGraphics();

        // Keep track of the previous color.
        java.awt.Color prevColor = g2d.getColor();
        try
        {
            // Fill the raster with the specified color.
            g2d.setColor(color);
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        finally
        {
            // Restore the previous color.
            g2d.setColor(prevColor);
        }
    }

    public long getSizeInBytes()
    {
        long size = 0L;
        java.awt.image.Raster raster = this.bufferedImage.getRaster();
        if (raster != null)
        {
            java.awt.image.DataBuffer db = raster.getDataBuffer();
            if (db != null)
            {
                size = sizeOfDataBuffer(db);
            }
        }
        return size;
    }

//    public void finalize()
//    {
//        try
//        {
//            super.finalize();
//            this.dispose();
//        }
//        catch (Throwable t)
//        {
//            Logging.logger().log(java.util.logging.Level.FINEST, t.getMessage(), t);
//        }
//    }

    public void dispose()
    {
        if (this.g2d != null)
        {
            this.g2d.dispose();
            this.g2d = null;
        }

        if (this.bufferedImage != null)
        {
            this.bufferedImage.flush();
            this.bufferedImage = null;
        }
    }

//    protected void doDrawOnTo(BufferedImageRaster canvas)
//    {
//        Sector sector = this.getSector();
//        if (null == sector)
//        {
//            String message = Logging.getMessage("nullValue.SectorIsNull");
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        if (!sector.intersects(canvas.getSector()))
//        {
//            return;
//        }
//
//        BufferedImage transformedImage = null;
//        java.awt.Graphics2D g2d = null;
//        java.awt.Shape prevClip = null;
//        java.awt.Composite prevComposite = null;
//
//        try
//        {
//            int canvasWidth = canvas.getWidth();
//            int canvasHeight = canvas.getHeight();
//
//            // Apply the transform that correctly maps the image onto the canvas.
//            java.awt.geom.AffineTransform transform = this.computeSourceToDestTransform(
//                this.getWidth(), this.getHeight(), this.getSector(), canvasWidth, canvasHeight, canvas.getSector());
//
//            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
//            Rectangle2D rect = op.getBounds2D(this.getBufferedImage());
//
//            int clipWidth = (int) Math.ceil((rect.getMaxX() >= canvasWidth) ? canvasWidth : rect.getMaxX());
//            int clipHeight = (int) Math.ceil((rect.getMaxY() >= canvasHeight) ? canvasHeight : rect.getMaxY());
//
//            if (clipWidth <= 0 || clipHeight <= 0)
//            {
//                return;
//            }
//
//            int transformedImageType = (BufferedImage.TYPE_CUSTOM != this.getBufferedImage().getType())
//                ? this.getBufferedImage().getType() : BufferedImage.TYPE_INT_ARGB;
//
//            transformedImage = new BufferedImage(clipWidth, clipHeight, transformedImageType);
//            op.filter(this.getBufferedImage(), transformedImage);
//
//            g2d = canvas.getGraphics();
//
//            prevClip = g2d.getClip();
//            prevComposite = g2d.getComposite();
//
//            // Set the alpha composite for appropriate alpha blending.
//            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
//            g2d.drawImage(transformedImage, 0, 0, null);
//        }
//        catch (java.awt.image.ImagingOpException ioe)
//        {
//            // If we catch a ImagingOpException, then the transformed image has a width or height of 0.
//            // This indicates that there is no intersection between the source image and the canvas,
//            // or the intersection is smaller than one pixel.
//        }
//        catch (java.awt.image.RasterFormatException rfe)
//        {
//            // If we catch a RasterFormatException, then the transformed image has a width or height of 0.
//            // This indicates that there is no intersection between the source image and the canvas,
//            // or the intersection is smaller than one pixel.
//        }
//        finally
//        {
//            // Restore the previous clip, composite, and transform.
//            try
//            {
//                if (null != transformedImage)
//                {
//                    transformedImage.flush();
//                }
//
//                if (null != g2d)
//                {
//                    if (null != prevClip)
//                    {
//                        g2d.setClip(prevClip);
//                    }
//
//                    if (null != prevComposite)
//                    {
//                        g2d.setComposite(prevComposite);
//                    }
//                }
//            }
//            catch (Throwable t)
//            {
//                Logging.logger().log(java.util.logging.Level.FINEST, WWUtil.extractExceptionReason(t), t);
//            }
//        }
//    }

    protected void doDrawOnTo(BufferedImageRaster canvas)
    {
        Sector sector = this.getSector();
        if (null == sector)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!sector.intersects(canvas.getSector()))
        {
            return;
        }

        java.awt.Graphics2D g2d = null;
        java.awt.Shape prevClip = null;
        java.awt.Composite prevComposite = null;
        java.lang.Object prevInterpolation = null, prevAntialiasing = null;

        try
        {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            // Apply the transform that correctly maps the image onto the canvas.
            java.awt.geom.AffineTransform transform = this.computeSourceToDestTransform(
                this.getWidth(), this.getHeight(), this.getSector(), canvasWidth, canvasHeight, canvas.getSector());

            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            Rectangle2D rect = op.getBounds2D(this.getBufferedImage());

            int clipWidth = (int) Math.ceil((rect.getMaxX() >= canvasWidth) ? canvasWidth : rect.getMaxX());
            int clipHeight = (int) Math.ceil((rect.getMaxY() >= canvasHeight) ? canvasHeight : rect.getMaxY());

            if (clipWidth <= 0 || clipHeight <= 0)
            {
                return;
            }

            g2d = canvas.getGraphics();

            prevClip = g2d.getClip();
            prevComposite = g2d.getComposite();
            prevInterpolation = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            prevAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

            // Set the alpha composite for appropriate alpha blending.
            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(this.getBufferedImage(), transform, null);
        }
//        catch (java.awt.image.ImagingOpException ioe)
//        {
//            // If we catch a ImagingOpException, then the transformed image has a width or height of 0.
//            // This indicates that there is no intersection between the source image and the canvas,
//            // or the intersection is smaller than one pixel.
//        }
//        catch (java.awt.image.RasterFormatException rfe)
//        {
//            // If we catch a RasterFormatException, then the transformed image has a width or height of 0.
//            // This indicates that there is no intersection between the source image and the canvas,
//            // or the intersection is smaller than one pixel.
//        }
        catch (Throwable t)
        {
            String reason = WWUtil.extractExceptionReason(t);
            Logging.logger().log(java.util.logging.Level.SEVERE, reason, t);
        }
        finally
        {
            // Restore the previous clip, composite, and transform.
            try
            {
                if (null != g2d)
                {
                    if (null != prevClip)
                        g2d.setClip(prevClip);

                    if (null != prevComposite)
                        g2d.setComposite(prevComposite);

                    if (null != prevInterpolation)
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterpolation);

                    if (null != prevAntialiasing)
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAntialiasing);
                }
            }
            catch (Throwable t)
            {
                Logging.logger().log(java.util.logging.Level.FINEST, WWUtil.extractExceptionReason(t), t);
            }
        }
    }

    private static long sizeOfDataBuffer(java.awt.image.DataBuffer dataBuffer)
    {
        return sizeOfElement(dataBuffer.getDataType()) * dataBuffer.getSize();
    }

    private static long sizeOfElement(int dataType)
    {
        switch (dataType)
        {
            case java.awt.image.DataBuffer.TYPE_BYTE:
                return (Byte.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_DOUBLE:
                return (Double.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_FLOAT:
                return (Float.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_INT:
                return (Integer.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_SHORT:
            case java.awt.image.DataBuffer.TYPE_USHORT:
                return (Short.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_UNDEFINED:
                break;
        }
        return 0L;
    }

    public static DataRaster wrap(BufferedImage image, AVList params)
    {
        if (null == image)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null == params)
        {
            String msg = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().finest(msg);
            throw new IllegalArgumentException(msg);
        }

        if (params.hasKey(AVKey.WIDTH))
        {
            int width = (Integer) params.getValue(AVKey.WIDTH);
            if (width != image.getWidth())
            {
                String msg = Logging.getMessage("generic.InvalidWidth", "" + width + "!=" + image.getWidth());
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            params.setValue(AVKey.WIDTH, image.getWidth());
        }

        if (params.hasKey(AVKey.HEIGHT))
        {
            int height = (Integer) params.getValue(AVKey.HEIGHT);
            if (height != image.getHeight())
            {
                String msg = Logging.getMessage("generic.InvalidHeight", "" + height + "!=" + image.getHeight());
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }
        else
        {
            params.setValue(AVKey.HEIGHT, image.getHeight());
        }

        Sector sector = null;
        if (params.hasKey(AVKey.SECTOR))
        {
            Object o = params.getValue(AVKey.SECTOR);
            if (o instanceof Sector)
            {
                sector = (Sector) o;
            }
        }

        return new BufferedImageRaster(sector, image, params);
    }

    public static DataRaster wrapAsGeoreferencedRaster(BufferedImage image, AVList params)
    {
        if (null == image)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null == params)
        {
            String msg = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().finest(msg);
            throw new IllegalArgumentException(msg);
        }

        if (params.hasKey(AVKey.WIDTH))
        {
            int width = (Integer) params.getValue(AVKey.WIDTH);
            if (width != image.getWidth())
            {
                String msg = Logging.getMessage("generic.InvalidWidth", "" + width + "!=" + image.getWidth());
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        if (params.hasKey(AVKey.HEIGHT))
        {
            int height = (Integer) params.getValue(AVKey.HEIGHT);
            if (height != image.getHeight())
            {
                String msg = Logging.getMessage("generic.InvalidHeight", "" + height + "!=" + image.getHeight());
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        if (!params.hasKey(AVKey.SECTOR))
        {
            String msg = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().finest(msg);
            throw new IllegalArgumentException(msg);
        }

        Sector sector = (Sector) params.getValue(AVKey.SECTOR);
        if (null == sector)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!params.hasKey(AVKey.COORDINATE_SYSTEM))
        {
            // assume Geodetic Coordinate System
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
        }

        String cs = params.getStringValue(AVKey.COORDINATE_SYSTEM);
        if (!params.hasKey(AVKey.PROJECTION_EPSG_CODE))
        {
            if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
            {
                // assume WGS84
                params.setValue(AVKey.PROJECTION_EPSG_CODE, GeoTiff.GCS.WGS_84);
            }
            else
            {
                String msg = Logging.getMessage("generic.MissingRequiredParameter", AVKey.PROJECTION_EPSG_CODE);
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // if PIXEL_WIDTH is specified, we are not overriding it because UTM images
        // will have different pixel size
        if (!params.hasKey(AVKey.PIXEL_WIDTH))
        {
            if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
            {
                double pixelWidth = sector.getDeltaLonDegrees() / (double) image.getWidth();
                params.setValue(AVKey.PIXEL_WIDTH, pixelWidth);
            }
            else
            {
                String msg = Logging.getMessage("generic.MissingRequiredParameter", AVKey.PIXEL_WIDTH);
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        // if PIXEL_HEIGHT is specified, we are not overriding it
        // because UTM images will have different pixel size
        if (!params.hasKey(AVKey.PIXEL_HEIGHT))
        {
            if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
            {
                double pixelHeight = sector.getDeltaLatDegrees() / (double) image.getHeight();
                params.setValue(AVKey.PIXEL_HEIGHT, pixelHeight);
            }
            else
            {
                String msg = Logging.getMessage("generic.MissingRequiredParameter", AVKey.PIXEL_HEIGHT);
                Logging.logger().finest(msg);
                throw new IllegalArgumentException(msg);
            }
        }

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
        {
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
        }
        else if (!AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT)))
        {
            String msg = Logging.getMessage("generic.UnknownValueForKey",
                params.getStringValue(AVKey.PIXEL_FORMAT), AVKey.PIXEL_FORMAT);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!params.hasKey(AVKey.ORIGIN) && AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs))
        {
            // set UpperLeft corner as the origin, if not specified
            LatLon origin = new LatLon(sector.getMaxLatitude(), sector.getMinLongitude());
            params.setValue(AVKey.ORIGIN, origin);
        }

        if (!params.hasKey(AVKey.DATE_TIME))
        {
            // add NUL (\0) termination as required by TIFF v6 spec (20 bytes length)
            String timestamp = String.format("%1$tY:%1$tm:%1$td %tT\0", Calendar.getInstance());
            params.setValue(AVKey.DATE_TIME, timestamp);
        }

        if (!params.hasKey(AVKey.VERSION))
        {
            params.setValue(AVKey.VERSION, Version.getVersion());
        }

        boolean hasAlpha = (null != image.getColorModel() && image.getColorModel().hasAlpha());
        params.setValue(AVKey.RASTER_HAS_ALPHA, hasAlpha);

        return new BufferedImageRaster(sector, image, params);
    }

    @Override
    DataRaster doGetSubRaster(int roiWidth, int roiHeight, Sector roiSector, AVList roiParams)
    {
        int transparency = java.awt.image.BufferedImage.TRANSLUCENT; // TODO: make configurable
        BufferedImageRaster canvas = new BufferedImageRaster(roiWidth, roiHeight, transparency, roiSector);
        this.drawOnTo(canvas);
        return canvas;
    }
}
