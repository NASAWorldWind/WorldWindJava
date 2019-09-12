/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.analytics;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.kml.KMLDocumentBuilder;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * @author tag
 * @version $Id: ExportAsKMLGroundOverlay.java 1352 2013-05-20 18:41:16Z tgaskins $
 */
public class ExportAsKMLGroundOverlay
{
    protected static final String DATA_PATH = "gov/nasa/worldwindx/examples/data/wa-precip-24hmam-5km.tif";
    protected static final double HUE_BLUE = 240d / 360d;
    protected static final double HUE_RED = 0d / 360d;

    protected static ExportableAnalyticSurface createPrecipitationSurface()
    {
        BufferWrapperRaster raster = loadRasterElevations(DATA_PATH);
        if (raster == null)
            return null;

        double[] extremes = WWBufferUtil.computeExtremeValues(raster.getBuffer(), raster.getTransparentValue());
        if (extremes == null)
            return null;

        final ExportableAnalyticSurface surface = new ExportableAnalyticSurface();
        surface.setSector(raster.getSector());
        surface.setDimensions(raster.getWidth(), raster.getHeight());
        surface.setValues(AnalyticSurface.createColorGradientValues(
            raster.getBuffer(), raster.getTransparentValue(), extremes[0], extremes[1], HUE_BLUE, HUE_RED));
        surface.setVerticalScale(5e3);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawOutline(false);
        surface.setSurfaceAttributes(attr);

        return surface;
    }

    protected static BufferWrapperRaster loadRasterElevations(String path)
    {
        // Download the data and save it in a temp file.
        File file = ExampleUtil.saveResourceToTempFile(path, "." + WWIO.getSuffix(path));

        // Create a raster reader for the file type.
        DataRasterReaderFactory readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
            AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        DataRasterReader reader = readerFactory.findReaderFor(file, null);

        try
        {
            // Before reading the raster, verify that the file contains elevations.
            AVList metadata = reader.readMetadata(file, null);
            if (metadata == null || !AVKey.ELEVATION.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
            {
                String msg = Logging.getMessage("ElevationModel.SourceNotElevations", file.getAbsolutePath());
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            // Read the file into the raster.
            DataRaster[] rasters = reader.read(file, null);
            if (rasters == null || rasters.length == 0)
            {
                String msg = Logging.getMessage("ElevationModel.CannotReadElevations", file.getAbsolutePath());
                Logging.logger().severe(msg);
                throw new WWRuntimeException(msg);
            }

            // Determine the sector covered by the elevations. This information is in the GeoTIFF file or auxiliary
            // files associated with the elevations file.
            Sector sector = (Sector) rasters[0].getValue(AVKey.SECTOR);
            if (sector == null)
            {
                String msg = Logging.getMessage("DataRaster.MissingMetadata", AVKey.SECTOR);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            // Request a sub-raster that contains the whole file. This step is necessary because only sub-rasters
            // are reprojected (if necessary); primary rasters are not.
            int width = rasters[0].getWidth();
            int height = rasters[0].getHeight();

            DataRaster subRaster = rasters[0].getSubRaster(width, height, sector, rasters[0]);

            // Verify that the sub-raster can create a ByteBuffer, then create one.
            if (!(subRaster instanceof BufferWrapperRaster))
            {
                String msg = Logging.getMessage("ElevationModel.CannotCreateElevationBuffer", path);
                Logging.logger().severe(msg);
                throw new WWRuntimeException(msg);
            }

            return (BufferWrapperRaster) subRaster;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    {
        try
        {
            ExportableAnalyticSurface surface = createPrecipitationSurface();

            if (surface != null)
            {
                surface.setExportImagePath(Configuration.getUserHomeDirectory());
                surface.setExportImageName("GroundOverlayImage.png");
                surface.setExportImageWidth(1024);
                surface.setExportImageHeight(1024);

                OutputStream os = new FileOutputStream(Configuration.getUserHomeDirectory() + "/GroundOverlay.kml");
                KMLDocumentBuilder kmlBuilder = new KMLDocumentBuilder(os);

                kmlBuilder.writeObject(surface);
                kmlBuilder.close();
            }
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
