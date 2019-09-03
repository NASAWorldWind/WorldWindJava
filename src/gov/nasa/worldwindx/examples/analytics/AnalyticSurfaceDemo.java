/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.analytics;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwindx.examples.util.ExampleUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.*;
import java.util.ArrayList;

/**
 * Illustrates how to configure and display a 3D geographic grid of scalar data using the WorldWind <code>{@link
 * AnalyticSurface}</code>. Analytic surface defines a grid over a geographic <code>{@link Sector}</code> at a specified
 * altitude, and enables the caller to specify the color and height at each grid point.
 * <p>
 * This illustrates three key AnalyticSurface configurations: <ul> <li>Displaying a static data set where each grid
 * point uses color and height to indicate the data's magnitude.</li> <li>Displaying data that varies by color over time
 * on the terrain surface.</li> <li>Displaying data that varies by color and height over time at a specified
 * altitude.</li> </ul>
 *
 * @author dcollins
 * @version $Id: AnalyticSurfaceDemo.java 2425 2014-11-13 19:44:19Z dcollins $
 */
public class AnalyticSurfaceDemo extends ApplicationTemplate
{
    protected static final String DATA_PATH = "gov/nasa/worldwindx/examples/data/wa-precip-24hmam-5km.tif";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected static final double HUE_BLUE = 240d / 360d;
        protected static final double HUE_RED = 0d / 360d;
        protected RenderableLayer analyticSurfaceLayer;

        public AppFrame()
        {
            this.initAnalyticSurfaceLayer();
        }

        protected void initAnalyticSurfaceLayer()
        {
            this.analyticSurfaceLayer = new RenderableLayer();
            this.analyticSurfaceLayer.setPickEnabled(false);
            this.analyticSurfaceLayer.setName("Analytic Surfaces");
            insertBeforePlacenames(this.getWwd(), this.analyticSurfaceLayer);

            createRandomAltitudeSurface(HUE_BLUE, HUE_RED, 40, 40, this.analyticSurfaceLayer);
            createRandomColorSurface(HUE_BLUE, HUE_RED, 40, 40, this.analyticSurfaceLayer);

            // Load the static precipitation data. Since it comes over the network, load it in a separate thread to
            // avoid blocking the example if the load is slow or fails.
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    createPrecipitationSurface(HUE_BLUE, HUE_RED, analyticSurfaceLayer);
                }
            });
            t.start();
        }
    }

    protected static Renderable createLegendRenderable(final AnalyticSurface surface, final double surfaceMinScreenSize,
        final AnalyticSurfaceLegend legend)
    {
        return new Renderable()
        {
            public void render(DrawContext dc)
            {
                Extent extent = surface.getExtent(dc);
                if (!extent.intersects(dc.getView().getFrustumInModelCoordinates()))
                    return;

                if (WWMath.computeSizeInWindowCoordinates(dc, extent) < surfaceMinScreenSize)
                    return;

                legend.render(dc);
            }
        };
    }

    //**************************************************************//
    //********************  Random Altitude Surface  ***************//
    //**************************************************************//

    protected static void createRandomAltitudeSurface(double minHue, double maxHue, int width, int height,
        RenderableLayer outLayer)
    {
        double minValue = -200e3;
        double maxValue = 200e3;

        AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(Sector.fromDegrees(25, 35, -90, -80));
        surface.setAltitude(400e3);
        surface.setDimensions(width, height);
        surface.setClientLayer(outLayer);
        outLayer.addRenderable(surface);

        BufferWrapper firstBuffer = randomGridValues(width, height, minValue, maxValue);
        BufferWrapper secondBuffer = randomGridValues(width, height, minValue * 2d, maxValue / 2d);
        mixValuesOverTime(2000L, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue, surface);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setShadowOpacity(0.5);
        surface.setSurfaceAttributes(attr);

        final double altitude = surface.getAltitude();
        final double verticalScale = surface.getVerticalScale();
        Format legendLabelFormat = new DecimalFormat("# km")
        {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
            {
                double altitudeMeters = altitude + verticalScale * number;
                double altitudeKm = altitudeMeters * WWMath.METERS_TO_KILOMETERS;
                return super.format(altitudeKm, result, fieldPosition);
            }
        };

        AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(minValue, maxValue, minHue, maxHue,
            AnalyticSurfaceLegend.createDefaultColorGradientLabels(minValue, maxValue, legendLabelFormat),
            AnalyticSurfaceLegend.createDefaultTitle("Random Altitudes"));
        legend.setOpacity(0.8);
        legend.setScreenLocation(new Point(650, 300));
        outLayer.addRenderable(createLegendRenderable(surface, 300, legend));
    }

    protected static void createRandomColorSurface(double minHue, double maxHue, int width, int height,
        RenderableLayer outLayer)
    {
        double minValue = -200e3;
        double maxValue = 200e3;

        AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(Sector.fromDegrees(25, 35, -110, -100));
        surface.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        surface.setDimensions(width, height);
        surface.setClientLayer(outLayer);
        outLayer.addRenderable(surface);

        BufferWrapper firstBuffer = randomGridValues(width, height, minValue, maxValue);
        BufferWrapper secondBuffer = randomGridValues(width, height, minValue * 2d, maxValue / 2d);
        mixValuesOverTime(2000L, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue, surface);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawShadow(false);
        attr.setInteriorOpacity(0.6);
        attr.setOutlineWidth(3);
        surface.setSurfaceAttributes(attr);
    }

    protected static void mixValuesOverTime(
        final long timeToMix,
        final BufferWrapper firstBuffer, final BufferWrapper secondBuffer,
        final double minValue, final double maxValue, final double minHue, final double maxHue,
        final AnalyticSurface surface)
    {
        Timer timer = new Timer(20, new ActionListener()
        {
            protected long startTime = -1;

            public void actionPerformed(ActionEvent e)
            {
                if (this.startTime < 0)
                    this.startTime = System.currentTimeMillis();

                double t = (double) (e.getWhen() - this.startTime) / (double) timeToMix;
                int ti = (int) Math.floor(t);

                double a = t - ti;
                if ((ti % 2) == 0)
                    a = 1d - a;

                surface.setValues(createMixedColorGradientGridValues(
                    a, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue));

                if (surface.getClientLayer() != null)
                    surface.getClientLayer().firePropertyChange(AVKey.LAYER, null, surface.getClientLayer());
            }
        });
        timer.start();
    }

    public static Iterable<? extends AnalyticSurface.GridPointAttributes> createMixedColorGradientGridValues(double a,
        BufferWrapper firstBuffer, BufferWrapper secondBuffer, double minValue, double maxValue,
        double minHue, double maxHue)
    {
        ArrayList<AnalyticSurface.GridPointAttributes> attributesList
            = new ArrayList<AnalyticSurface.GridPointAttributes>();

        long length = Math.min(firstBuffer.length(), secondBuffer.length());
        for (int i = 0; i < length; i++)
        {
            double value = WWMath.mixSmooth(a, firstBuffer.getDouble(i), secondBuffer.getDouble(i));
            attributesList.add(
                AnalyticSurface.createColorGradientAttributes(value, minValue, maxValue, minHue, maxHue));
        }

        return attributesList;
    }

    //**************************************************************//
    //********************  Precipitation Surface  *****************//
    //**************************************************************//

    protected static void createPrecipitationSurface(double minHue, double maxHue, final RenderableLayer outLayer)
    {
        BufferWrapperRaster raster = loadRasterElevations(DATA_PATH);
        if (raster == null)
            return;

        double[] extremes = WWBufferUtil.computeExtremeValues(raster.getBuffer(), raster.getTransparentValue());
        if (extremes == null)
            return;

        final AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(raster.getSector());
        surface.setDimensions(raster.getWidth(), raster.getHeight());
        surface.setValues(AnalyticSurface.createColorGradientValues(
            raster.getBuffer(), raster.getTransparentValue(), extremes[0], extremes[1], minHue, maxHue));
        surface.setVerticalScale(5e3);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawOutline(false);
        attr.setDrawShadow(false);
        attr.setInteriorOpacity(0.6);
        surface.setSurfaceAttributes(attr);

        Format legendLabelFormat = new DecimalFormat("# ft")
        {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
            {
                double valueInFeet = number * WWMath.METERS_TO_FEET;
                return super.format(valueInFeet, result, fieldPosition);
            }
        };

        final AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(extremes[0], extremes[1],
            minHue, maxHue,
            AnalyticSurfaceLegend.createDefaultColorGradientLabels(extremes[0], extremes[1], legendLabelFormat),
            AnalyticSurfaceLegend.createDefaultTitle("Annual Precipitation"));
        legend.setOpacity(0.8);
        legend.setScreenLocation(new Point(100, 300));

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                surface.setClientLayer(outLayer);
                outLayer.addRenderable(surface);
                outLayer.addRenderable(createLegendRenderable(surface, 300, legend));
            }
        });
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

    //**************************************************************//
    //********************  Random Grid Construction  **************//
    //**************************************************************//

    protected static final int DEFAULT_RANDOM_ITERATIONS = 1000;
    protected static final double DEFAULT_RANDOM_SMOOTHING = 0.5d;

    public static BufferWrapper randomGridValues(int width, int height, double min, double max, int numIterations,
        double smoothness, BufferFactory factory)
    {
        double[] values = ExampleUtil.createRandomGridValues(width, height, min, max, numIterations, smoothness);
        BufferWrapper wrapper = factory.newBuffer(values.length);
        wrapper.putDouble(0, values, 0, values.length);

        return wrapper;
    }

    public static BufferWrapper randomGridValues(int width, int height, double min, double max)
    {
        return randomGridValues(width, height, min, max, DEFAULT_RANDOM_ITERATIONS, DEFAULT_RANDOM_SMOOTHING,
            new BufferFactory.DoubleBufferFactory());
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Analytic Surface", AppFrame.class);
    }
}
