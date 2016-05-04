/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id: BasicRasterServer.java 2813 2015-02-18 23:35:24Z tgaskins $
 */

/**
 * BasicRasterServer maintains a list of data sources and their properties in the BasicRasterServerCache and is used to
 * compose (mosaic) a data raster of the given region of interest from data sources.
 */
public class BasicRasterServer extends WWObjectImpl implements RasterServer
{
    protected java.util.List<DataRaster> dataRasterList = new java.util.ArrayList<DataRaster>();

    protected DataRasterReaderFactory readerFactory;

    protected static final MemoryCache cache = new BasicRasterServerCache();

    /**
     * BasicRasterServer constructor reads a list of data raster sources from *.RasterServer.xml (the file that
     * accompanies layer description XML file), reads sector of each source and maintains a list of data sources, their
     * properties,
     *
     * @param o      the RasterServer.xml source to read.  May by a {@link java.io.File}, a file path, a URL or an
     *               {@link org.w3c.dom.Element}
     * @param params optional metadata associated with the data source that might be useful to the BasicRasterServer.
     */
    public BasicRasterServer(Object o, AVList params)
    {
        super();

        if (null != params)
        {
            this.setValues(params);
        }

        try
        {
            this.readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
                AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        }
        catch (Exception e)
        {
            this.readerFactory = new BasicDataRasterReaderFactory();
        }

        this.init(o);
    }

    /** Returns an instance of the MemoryCache that contains DataRasters and their properties
     *
     * @return  an instance of the MemoryCache that contains DataRasters and their properties
     */
    public MemoryCache getCache()
    {
        return cache;
    }

    /**
     * Returns TRUE, if the DataRaster list is not empty
     *
     * @return true, if the DataRaster list is not empty
     */
    public boolean hasDataRasters()
    {
        return (this.dataRasterList.size() > 0);
    }

    protected void init(Object o)
    {
        if (null == o)
        {
            throw new IllegalArgumentException();
        }

//        if (null == rootElement)
//        {
//            String message = Logging.getMessage("generic.UnexpectedObjectType", o.getClass().getName());
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException();
//        }
//
//        String rootElementName = rootElement.getNodeName();
//        if (!"RasterServer".equals(rootElementName))
//        {
//            String message = Logging.getMessage("generic.InvalidDataSource", rootElementName);
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException();
//        }

        RasterServerConfiguration config = new RasterServerConfiguration(o);
        try
        {
            config.parse();
        }
        catch (XMLStreamException e)
        {
            throw new IllegalArgumentException();
        }

        this.extractProperties(config);

        if( this.readRasterSources(config) )
        {
            // success, all raster sources are available
        }
        else
        {
            // some (or all) required source rasters are not available (either missing or unreadable)
            // and therefore the dataset may not generate high resolution on-the-fly
        }
    }

    protected String getDataSetName()
    {
        return AVListImpl.getStringValue( this, AVKey.DATASET_NAME, "" );
    }

    /**
     * Returns DataSet's pixel format (AVKey.IMAGE or AVKey.ELEVATION), or empty string if not set
     *
     * @return DataSet's pixel format (AVKey.IMAGE or AVKey.ELEVATION), or empty string if not set
     */
    protected String getDataSetPixelFormat()
    {
        return AVListImpl.getStringValue( this, AVKey.PIXEL_FORMAT, "" );
    }

    protected void setDataSetPixelFormat(String pixelFormat)
    {
        this.setValue(AVKey.PIXEL_FORMAT, pixelFormat);
    }

    /**
     * Extracts all <Property/> key and values from the given DOM element
     *
     * @param config Parsed configuration document.
     */
    protected void extractProperties(RasterServerConfiguration config)
    {
        for (Map.Entry<String, String> prop : config.getProperties().entrySet())
        {
            this.setValue(prop.getKey(), prop.getValue());
        }
    }
    /**
     * Reads XML document and extracts raster sources
     *
     * @param config Parsed configuration document.
     *
     * @return TRUE, if all raster sources are available, FALSE otherwise
     *
     */
    protected boolean readRasterSources(RasterServerConfiguration config)
    {
        long startTime = System.currentTimeMillis();

        boolean hasUnavailableRasterSources = false;

        int numSources = 0;
        Sector extent = null;

        try
        {
            List<RasterServerConfiguration.Source> sources = config.getSources();

            if (sources == null || sources.size() == 0)
            {
                return false;
            }

            numSources = sources.size();
            for (RasterServerConfiguration.Source source : sources) {
                Thread.yield();
                try
                {
                    String rasterSourcePath = source.getPath();
                    if (WWUtil.isEmpty(rasterSourcePath))
                    {
                        continue;
                    }

                    AVList rasterMetadata = new AVListImpl();
                    File rasterSourceFile = new File(rasterSourcePath);
                    // normalize
                    rasterSourcePath = rasterSourceFile.getAbsolutePath();

                    if( !rasterSourceFile.exists() )
                    {
                        hasUnavailableRasterSources = true;
                        String reason = null;
                        continue;
                    }

                    if( !rasterSourceFile.canRead() )
                    {
                        hasUnavailableRasterSources = true;
                        String reason = null;
                        continue;
                    }

                    DataRasterReader rasterReader = this.findDataRasterReader(rasterSourceFile, rasterMetadata);
                    if (null == rasterReader)
                    {
                        hasUnavailableRasterSources = true;
                        String reason = null;
                        continue;
                    }

                    Sector sector = source.getSector();
                    if (null == sector)
                    {
                        rasterReader.readMetadata(rasterSourceFile, rasterMetadata);

                        Object o = rasterMetadata.getValue(AVKey.SECTOR);
                        sector = (o instanceof Sector) ? (Sector) o : null;
                    }
                    else
                    {
                        rasterMetadata.setValue(AVKey.SECTOR, sector);
                    }

                    Object rasterPixelFormat = rasterMetadata.getValue(AVKey.PIXEL_FORMAT);
                    String datasetPixelFormat = this.getDataSetPixelFormat();

                    if( !WWUtil.isEmpty(datasetPixelFormat) )
                    {
                        // verify all data rasters are the same type - we do not allow to mix elevations and imagery
                        if (!datasetPixelFormat.equals(rasterPixelFormat))
                        {
                            hasUnavailableRasterSources = true;
                            String reason = null;
                            continue;
                        }
                    }
                    else
                    {
                        if( AVKey.IMAGE.equals(rasterPixelFormat) || AVKey.ELEVATION.equals(rasterPixelFormat) )
                        {
                            this.setDataSetPixelFormat( (String)rasterPixelFormat );
                        }
                        else
                        {
                            hasUnavailableRasterSources = true;
                            String reason = null;
                            continue;
                        }
                    }

                    if (null != sector)
                    {
                        extent = Sector.union(extent, sector);
                        this.dataRasterList.add(
                            new CachedDataRaster(rasterSourceFile, rasterMetadata, rasterReader, this.getCache())
                        );
                    }
                    else
                    {
                        hasUnavailableRasterSources = true;
                        String reason = null;
                    }
                }
                catch (Throwable t)
                {
                    String message = t.getMessage();
                    message = (WWUtil.isEmpty(message)) ? t.getCause().getMessage() : message;
                }
            }

            if (null != extent && extent.getDeltaLatDegrees() > 0d && extent.getDeltaLonDegrees() > 0d)
            {
                this.setValue(AVKey.SECTOR, extent);
            }
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            message = (WWUtil.isEmpty(message)) ? t.getCause().getMessage() : message;
        }
        finally
        {
        }

        return !hasUnavailableRasterSources;
    }

    protected DataRasterReader findDataRasterReader(Object source, AVList params)
    {
        if (source == null)
        {
            throw new IllegalArgumentException();
        }

        params = (null == params) ? new AVListImpl() : params;

        DataRasterReader reader = this.readerFactory.findReaderFor(source, params);
        if (reader == null)
        {
            return null;
        }

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
        {
            try
            {
                reader.readMetadata(source, params);
            }
            catch (Exception e)
            {
                // Reading the input source's metadata caused an exception. This exception does not prevent us from
                // determining if the source represents elevation data, but we want to make a note of it. Therefore we
                // log the exception with level FINE.
                }
        }

        return reader;
    }

    public Sector getSector()
    {
        return (this.hasKey(AVKey.SECTOR)) ? (Sector) this.getValue(AVKey.SECTOR) : null;
    }

    /**
     * Composes a DataRaster of the given width and height for the specific geographic region of interest (ROI).
     *
     * @param reqParams This is a required parameter, must not be null or empty; Must contain AVKey.WIDTH, AVKey.HEIGHT,
     *                  and AVKey.SECTOR values.
     *                  <p/>
     *                  Optional keys are: AVKey.PIXEL_FORMAT (AVKey.ELEVATION | AVKey.IMAGE) AVKey.DATA_TYPE
     *                  AVKey.BYTE_ORDER (AVKey.BIG_ENDIAN | AVKey.LITTLE_ENDIAN )
     *
     * @return a DataRaster for the requested ROI
     *
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if there is no intersection of the source rasters with the requested ROI or the
     *                                  source format is unknown or not supported by currently loaded drivers
     * @throws IllegalArgumentException if any of the required parameters or values are missing
     */
    public DataRaster composeRaster(AVList reqParams) throws IllegalArgumentException, WWRuntimeException
    {
        DataRaster reqRaster;

        if (null == reqParams)
        {
            throw new IllegalArgumentException();
        }
        if (!reqParams.hasKey(AVKey.WIDTH))
        {
            throw new IllegalArgumentException();
        }
        if (!reqParams.hasKey(AVKey.HEIGHT))
        {
            throw new IllegalArgumentException();
        }

        Object o = reqParams.getValue(AVKey.SECTOR);
        if (null == o || !(o instanceof Sector))
        {
            throw new IllegalArgumentException();
        }

        Sector reqSector = (Sector) o;
        Sector rasterExtent = this.getSector();
        if (!reqSector.intersects(rasterExtent))
        {
            throw new WWRuntimeException();
        }

        try
        {
            int reqWidth = (Integer) reqParams.getValue(AVKey.WIDTH);
            int reqHeight = (Integer) reqParams.getValue(AVKey.HEIGHT);

            if (!reqParams.hasKey(AVKey.BYTE_ORDER))
            {
                reqParams.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);
            }

            // check if this Raster Server serves elevations or imagery
            if (AVKey.ELEVATION.equals(this.getStringValue(AVKey.PIXEL_FORMAT)))
            {
                reqParams.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);

                if (!reqParams.hasKey(AVKey.DATA_TYPE))
                {
                    reqParams.setValue(AVKey.DATA_TYPE, AVKey.INT16);
                }

                reqRaster = new ByteBufferRaster(reqWidth, reqHeight, reqSector, reqParams);
            }
            else if (AVKey.IMAGE.equals(this.getStringValue(AVKey.PIXEL_FORMAT)))
            {
                reqParams.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                reqRaster = new BufferedImageRaster(reqWidth, reqHeight, Transparency.TRANSLUCENT, reqSector);
            }
            else
            {
                    throw new WWRuntimeException();
            }

            int numIntersectedRasters = 0;
            for (DataRaster raster : this.dataRasterList)
            {
                Sector rasterSector = raster.getSector();
                Sector overlap = reqSector.intersection(rasterSector);
                // SKIP, if not intersection, or intersects only on edges
                if (null == overlap || overlap.getDeltaLatDegrees() == 0d || overlap.getDeltaLonDegrees() == 0d)
                {
                    continue;
                }

                raster.drawOnTo(reqRaster);
                numIntersectedRasters++;
            }

            if (numIntersectedRasters == 0)
            {
                    throw new WWRuntimeException();
            }
        }
        catch (WWRuntimeException wwe)
        {
            throw wwe;
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            message = (WWUtil.isEmpty(message)) ? t.getCause().getMessage() : message;
            throw new WWRuntimeException();
        }

        return reqRaster;
    }

    /**
     * Composes a DataRaster of the given width and height for the specific geographic region of interest (ROI), in the
     * requested file format (AVKey.IMAGE_FORMAT) and returns as a ByteBuffer
     *
     * @param params This is a required parameter, must not be null or empty; Must contain AVKey.WIDTH, AVKey.HEIGHT,
     *               AVKey.SECTOR, and AVKey.IMAGE_FORMAT (mime type) values. Supported mime types are: "image/png",
     *               "image/jpeg", "image/dds".
     *               <p/>
     *               Optional keys are: AVKey.PIXEL_FORMAT (AVKey.ELEVATION | AVKey.IMAGE) AVKey.DATA_TYPE
     *               AVKey.BYTE_ORDER (AVKey.BIG_ENDIAN | AVKey.LITTLE_ENDIAN )
     *
     * @return a DataRaster for the requested ROI
     *
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if there is no intersection of the source rasters with the requested ROI or the
     *                                  source format is unknown or not supported by currently loaded drivers
     * @throws IllegalArgumentException if any of the required parameters or values are missing
     */
    public ByteBuffer getRasterAsByteBuffer(AVList params)
    {
        // request may contain a specific file format, different from a default file format
        String format = (null != params && params.hasKey(AVKey.IMAGE_FORMAT))
            ? params.getStringValue(AVKey.IMAGE_FORMAT) : this.getStringValue(AVKey.IMAGE_FORMAT);
        if (WWUtil.isEmpty(format))
        {
            throw new WWRuntimeException();
        }

        if (this.dataRasterList.isEmpty())
        {
            throw new WWRuntimeException();
        }

        try
        {
            DataRaster raster = this.composeRaster(params);

            if (raster instanceof BufferedImageRaster)
            {
                if ("image/png".equalsIgnoreCase(format))
                {
                    return ImageUtil.asPNG(raster);
                }
                else if ("image/jpeg".equalsIgnoreCase(format) || "image/jpg".equalsIgnoreCase(format))
                {
                    return ImageUtil.asJPEG(raster);
                }
                if ("image/dds".equalsIgnoreCase(format))
                {
                    return DDSCompressor.compressImage(((BufferedImageRaster) raster).getBufferedImage());
                }
                else
                {
                            throw new WWRuntimeException();
                }
            }
            else if (raster instanceof ByteBufferRaster)
            {
                // Elevations as BIL16 or as BIL32 are stored in the simple ByteBuffer object
                return ((ByteBufferRaster) raster).getByteBuffer();
            }
            else
            {
                    throw new WWRuntimeException();
            }
        }
        catch (WWRuntimeException wwe)
        {
        }
        catch (Throwable t)
        {
            String message = t.getMessage();
            message = (WWUtil.isEmpty(message)) ? t.getCause().getMessage() : message;
        }

        return null;
    }
}

