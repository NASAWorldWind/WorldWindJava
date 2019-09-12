/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.tiff.GeotiffImageReaderSpi;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

/**
 * @author dcollins
 * @version $Id: ImageIORasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImageIORasterReader extends AbstractDataRasterReader
{
    static
    {
        javax.imageio.spi.IIORegistry.getDefaultInstance().registerServiceProvider(GeotiffImageReaderSpi.inst());
    }

    private boolean generateMipMaps;

    public ImageIORasterReader(boolean generateMipMaps)
    {
        super(javax.imageio.ImageIO.getReaderMIMETypes(), getImageIOReaderSuffixes());
        this.generateMipMaps = generateMipMaps;
    }

    public ImageIORasterReader()
    {
        this(false);
    }

    public boolean isGenerateMipMaps()
    {
        return this.generateMipMaps;
    }

    public void setGenerateMipMaps(boolean generateMipMaps)
    {
        this.generateMipMaps = generateMipMaps;
    }

    protected boolean doCanRead(Object source, AVList params)
    {
        // Determine whether or not the data source can be read.
        //if (!this.canReadImage(source))
        //    return false;

        // If the data source doesn't already have all the necessary metadata, then we determine whether or not
        // the missing metadata can be read.
        Object o = (params != null) ? params.getValue(AVKey.SECTOR) : null;
        if (o == null || !(o instanceof Sector))
        {
            if (!this.canReadWorldFiles(source))
            {
                return false;
            }
        }

        if (null != params && !params.hasKey(AVKey.PIXEL_FORMAT))
        {
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
        }

        return true;
    }

    protected DataRaster[] doRead(Object source, AVList params) throws java.io.IOException
    {
        javax.imageio.stream.ImageInputStream iis = createInputStream(source);
        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(iis);
        image = ImageUtil.toCompatibleImage(image);

        // If the data source doesn't already have all the necessary metadata, then we attempt to read the metadata.
        Object o = (params != null) ? params.getValue(AVKey.SECTOR) : null;
        if (o == null || !(o instanceof Sector))
        {
            AVList values = new AVListImpl();
            values.setValue(AVKey.IMAGE, image);
            this.readWorldFiles(source, values);
            o = values.getValue(AVKey.SECTOR);
        }

        return new DataRaster[]{this.createRaster((Sector) o, image)};
    }

    protected void doReadMetadata(Object source, AVList params) throws java.io.IOException
    {
        Object width = params.getValue(AVKey.WIDTH);
        Object height = params.getValue(AVKey.HEIGHT);
        if (width == null || height == null || !(width instanceof Integer) || !(height instanceof Integer))
        {
            this.readImageDimension(source, params);
        }

        Object sector = params.getValue(AVKey.SECTOR);
        if (sector == null || !(sector instanceof Sector))
        {
            this.readWorldFiles(source, params);
        }

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
        {
            params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
        }
    }

    protected DataRaster createRaster(Sector sector, java.awt.image.BufferedImage image)
    {
        if (this.isGenerateMipMaps())
        {
            return new MipMappedBufferedImageRaster(sector, image);
        }
        else
        {
            return new BufferedImageRaster(sector, image);
        }
    }

    //private boolean canReadImage(DataSource source)
    //{
    //    javax.imageio.stream.ImageInputStream iis = null;
    //    javax.imageio.ImageReader reader = null;
    //    try
    //    {
    //        iis = createInputStream(source);
    //        reader = readerFor(iis);
    //        if (reader == null)
    //            return false;
    //    }
    //    catch (Exception e)
    //    {
    //        // Not interested in logging the exception, we only want to report the failure to read.
    //        return false;
    //    }
    //    finally
    //    {
    //        if (reader != null)
    //            reader.dispose();
    //        try
    //        {
    //            if (iis != null)
    //                iis.close();
    //        }
    //        catch (Exception e)
    //        {
    //            // Not interested in logging the exception.
    //        }
    //    }
    //
    //    return true;
    //}

    private boolean canReadWorldFiles(Object source)
    {
        if (!(source instanceof java.io.File))
        {
            return false;
        }

        try
        {
            java.io.File[] worldFiles = WorldFile.getWorldFiles((java.io.File) source);
            if (worldFiles == null || worldFiles.length == 0)
            {
                return false;
            }
        }
        catch (java.io.IOException e)
        {
            // Not interested in logging the exception, we only want to report the failure to read.
            return false;
        }

        return true;
    }

    private void readImageDimension(Object source, AVList params) throws java.io.IOException
    {
        javax.imageio.stream.ImageInputStream iis = createInputStream(source);
        javax.imageio.ImageReader reader = readerFor(iis);
        try
        {
            if (reader == null)
            {
                String message = Logging.getMessage("generic.UnrecognizedImageSourceType", source);
                Logging.logger().severe(message);
                throw new java.io.IOException(message);
            }

            reader.setInput(iis, true, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            params.setValue(AVKey.WIDTH, width);
            params.setValue(AVKey.HEIGHT, height);
        }
        finally
        {
            if (reader != null)
            {
                reader.dispose();
            }
            iis.close();
        }
    }

    private void readWorldFiles(Object source, AVList params) throws java.io.IOException
    {
        if (!(source instanceof java.io.File))
        {
            String message = Logging.getMessage("DataRaster.CannotRead", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        // If an image is not specified in the metadata values, then attempt to construct the image size from other
        // parameters.
        Object o = params.getValue(AVKey.IMAGE);
        if (o == null || !(o instanceof java.awt.image.BufferedImage))
        {
            o = params.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
            if (o == null || !(o instanceof int[]))
            {
                // If the image size is specified in the parameters WIDTH and HEIGHT, then translate them to the
                // WORLD_FILE_IMAGE_SIZE parameter.
                Object width = params.getValue(AVKey.WIDTH);
                Object height = params.getValue(AVKey.HEIGHT);
                if (width != null && height != null && width instanceof Integer && height instanceof Integer)
                {
                    int[] size = new int[]{(Integer) width, (Integer) height};
                    params.setValue(WorldFile.WORLD_FILE_IMAGE_SIZE, size);
                }
            }
        }

        java.io.File[] worldFiles = WorldFile.getWorldFiles((java.io.File) source);
        WorldFile.decodeWorldFiles(worldFiles, params);
    }

    private static javax.imageio.stream.ImageInputStream createInputStream(Object source) throws java.io.IOException
    {
        // ImageIO can create an ImageInputStream automatically from a File references or a standard I/O InputStream
        // reference. If the data source is a URL, or a string file path, then we must open an input stream ourselves.

        Object input = source;

        if (source instanceof java.net.URL)
        {
            input = ((java.net.URL) source).openStream();
        }
        else if (source instanceof CharSequence)
        {
            input = openInputStream(source.toString());
        }

        return javax.imageio.ImageIO.createImageInputStream(input);
    }

    private static java.io.InputStream openInputStream(String path) throws java.io.IOException
    {
        Object streamOrException = WWIO.getFileOrResourceAsStream(path, null);
        if (streamOrException == null)
        {
            return null;
        }
        else if (streamOrException instanceof java.io.IOException)
        {
            throw (java.io.IOException) streamOrException;
        }
        else if (streamOrException instanceof Exception)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadImageFile", path);
            Logging.logger().log(java.util.logging.Level.SEVERE, message, streamOrException);
            throw new java.io.IOException(message);
        }

        return (java.io.InputStream) streamOrException;
    }

    private static javax.imageio.ImageReader readerFor(javax.imageio.stream.ImageInputStream iis)
    {
        java.util.Iterator<javax.imageio.ImageReader> readers = javax.imageio.ImageIO.getImageReaders(iis);
        if (!readers.hasNext())
        {
            return null;
        }

        return readers.next();
    }

    private static String[] getImageIOReaderSuffixes()
    {
        java.util.Iterator<javax.imageio.spi.ImageReaderSpi> iter;
        try
        {
            iter = javax.imageio.spi.IIORegistry.getDefaultInstance().getServiceProviders(
                    javax.imageio.spi.ImageReaderSpi.class, true);
        }
        catch (Exception e)
        {
            return new String[0];
        }

        java.util.Set<String> set = new java.util.HashSet<String>();
        while (iter.hasNext())
        {
            javax.imageio.spi.ImageReaderSpi spi = iter.next();
            String[] names = spi.getFileSuffixes();
            set.addAll(java.util.Arrays.asList(names));
        }

        String[] array = new String[set.size()];
        set.toArray(array);
        return array;
    }
}
