/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Displays a collection of local images on the globe.
 * <p/>
 * Note: The view input handlers detect surface images rather than the terrain as the top picked object in {@link
 * gov.nasa.worldwind.event.SelectEvent}s and will not respond to the user's attempts at navigation when the cursor is
 * over the image. If this is not the desired behavior, disable picking for the layer containing the surface image.
 *
 * @author tag
 * @version $Id: SurfaceImageLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceImageLayer extends RenderableLayer
{
    protected ImageTiler imageTiler = new ImageTiler();
    protected ConcurrentHashMap<String, ArrayList<SurfaceImage>> imageTable =
        new ConcurrentHashMap<String, ArrayList<SurfaceImage>>();

    protected BasicDataRasterReaderFactory factory = new BasicDataRasterReaderFactory();

    @Override
    public void dispose()
    {
        super.dispose();

        this.imageTable.clear();
    }

    /**
     * Add an image to the collection, reprojecting it to geographic (latitude & longitude) coordinates if necessary.
     * The image's location is determined from metadata files co-located with the image file. The number, names and
     * contents of these files are governed by the type of the specified image. Location metadata must be available.
     * <p/>
     * If projection information is available and reprojection of the image's projection type is supported, the image
     * will be reprojected to geographic coordinates. If projection information is not available then it's assumed that
     * the image is already in geographic projection.
     * <p/>
     * Only reprojection from UTM is currently provided.
     *
     * @param imagePath the path to the image file.
     *
     * @throws IllegalArgumentException if the image path is null.
     * @throws IOException              if an error occurs reading the image file.
     * @throws IllegalStateException    if an error occurs while reprojecting or otherwise processing the image.
     * @throws WWRuntimeException       if the image type is unsupported.
     */
    public void addImage(final String imagePath) throws IOException
    {
        DataRaster raster = this.openDataRaster(imagePath, null);
        final BufferedImage image = this.getBufferedImage(raster);

        if (null != raster && image != null)
        {
            addImage(imagePath, image, raster.getSector());
        }
        else
        {
            String message = Logging.getMessage("generic.ImageReadFailed", imagePath);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    protected BufferedImage getBufferedImage(DataRaster raster)
    {
        if (null == raster)
            return null;

        if (raster instanceof GDALDataRaster)
        {
            AVList params = new AVListImpl();

            params.setValue(AVKey.WIDTH, raster.getWidth());
            params.setValue(AVKey.HEIGHT, raster.getHeight());
            params.setValue(AVKey.SECTOR, raster.getSector());

            raster = raster.getSubRaster(params);
        }

        if (raster instanceof BufferedImageRaster)
        {
            return ((BufferedImageRaster) raster).getBufferedImage();
        }

        if (raster instanceof BufferWrapperRaster)
        {
            return ImageUtil.visualize((BufferWrapperRaster) raster);
        }

        return null;
    }

    protected DataRaster openDataRaster(Object src, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (src == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final File rasterFile = WWIO.getFileForLocalAddress(src);
        if (null == rasterFile || !rasterFile.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", src);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null == params)
            params = new AVListImpl();

        DataRaster raster;

        try
        {
            DataRasterReader reader = factory.findReaderFor(rasterFile, params);
            DataRaster[] rasters = reader.read(rasterFile, params);
            if (null == rasters || rasters.length == 0 || null == rasters[0])
            {
                String message = Logging.getMessage("generic.ImageReadFailed", src);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            raster = rasters[0];

            if (raster.getSector() == null && params.hasKey(AVKey.SECTOR))
            {
                Object o = params.getValue(AVKey.SECTOR);
                if (null != o && o instanceof Sector)
                {
                    Sector sector = (Sector) o;

                    if (raster instanceof GDALDataRaster)
                        ((GDALDataRaster) raster).setSector(sector);
                    else
                        raster.setValue(AVKey.SECTOR, sector);
                }
            }

            if (raster.getSector() == null)
            {
                String reason = Logging.getMessage("nullValue.SpatialReferenceIsNull");
                String message = Logging.getMessage("generic.ImageReadFailed", src + ":" + reason);
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }
        }
        catch (WWRuntimeException wwre)
        {
            throw wwre;
        }
        catch (Throwable t)
        {
            String reason = WWUtil.extractExceptionReason(t);
            String message = Logging.getMessage("generic.ImageReadFailed", reason);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        return raster;
    }

    /**
     * Add an image to the collection and specify its coverage. The image is assumed to be in geographic projection
     * (latitude & longitude).
     *
     * @param imagePath the path to the image file.
     * @param sector    the geographic location of the image.
     *
     * @throws IllegalArgumentException if the image path or sector is null.
     * @throws IOException              if an error occurs reading the image file.
     * @throws WWRuntimeException       if the image type is unsupported.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void addImage(String imagePath, Sector sector) throws IOException, WWRuntimeException
    {
        AVList params = new AVListImpl();

        if (null != sector)
            params.setValue(AVKey.SECTOR, sector);

        DataRaster raster = this.openDataRaster(imagePath, params);
        final BufferedImage image = this.getBufferedImage(raster);

        if (null != raster && image != null)
        {
            Sector rasterSector = raster.getSector();
            rasterSector = (null == rasterSector) ? sector : rasterSector;
            addImage(imagePath, image, rasterSector);
        }
        else
        {
            String message = Logging.getMessage("generic.ImageReadFailed", imagePath);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    /**
     * Add a {@link BufferedImage} to the collection at an explicitly specified location. The image is assumed to be in
     * geographic projection (latitude & longitude).
     *
     * @param name   a unique name to associate with the image so that it can be subsequently referred to without having
     *               to keep a reference to the image itself. Use this name in calls to {@link #removeImage(String)}.
     * @param image  the image to add.
     * @param sector the geographic location of the image.
     *
     * @throws IllegalArgumentException if the image path or sector is null.
     * @throws WWRuntimeException       if the image type is unsupported.
     */
    public void addImage(String name, BufferedImage image, Sector sector)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.imageTable.contains(name))
            this.removeImage(name);

        final ArrayList<SurfaceImage> surfaceImages = new ArrayList<SurfaceImage>();
        this.imageTable.put(name, surfaceImages);
        this.imageTiler.tileImage(image, sector, new ImageTiler.ImageTilerListener()
        {
            public void newTile(BufferedImage tileImage, Sector tileSector)
            {
                try
                {
                    File tempFile = File.createTempFile("wwj-", ".png");
                    tempFile.deleteOnExit();
                    ImageIO.write(tileImage, "png", tempFile);
                    SurfaceImage si = new SurfaceImage(tempFile.getPath(), tileSector);
                    surfaceImages.add(si);
                    si.setOpacity(SurfaceImageLayer.this.getOpacity());
                    SurfaceImageLayer.this.addRenderable(si);
                }
                catch (IOException e)
                {
                    String message = Logging.getMessage("generic.ImageReadFailed");
                    Logging.logger().severe(message);
                }
            }

            public void newTile(BufferedImage tileImage, List<? extends LatLon> corners)
            {
            }
        });
    }

    /**
     * Add an image to the collection at an explicitly specified location. The image is assumed to be in geographic
     * projection (latitude & longitude).
     *
     * @param imagePath the path to the image file.
     * @param corners   the geographic location of the image's corners, specified in order of lower-left, lower-right,
     *                  upper-right, upper-left.
     *
     * @throws IllegalArgumentException if the image path or sector is null.
     * @throws IOException              if an error occurs reading the image file.
     * @throws WWRuntimeException       if the image type is unsupported.
     */
    public void addImage(String imagePath, List<? extends LatLon> corners) throws IOException, WWRuntimeException
    {
        AVList params = new AVListImpl();

        if (null != corners)
        {
            Sector sector = Sector.boundingSector(corners);
            params.setValue(AVKey.SECTOR, sector);
        }

        DataRaster raster = this.openDataRaster(imagePath, params);
        final BufferedImage image = this.getBufferedImage(raster);

        if (null != raster && image != null)
        {
            this.addImage(imagePath, image, corners);
        }
        else
        {
            String message = Logging.getMessage("generic.ImageReadFailed", imagePath);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    /**
     * Add a {@link BufferedImage} to the collection at an explicitly specified location. The image is assumed to be in
     * geographic projection (latitude & longitude).
     *
     * @param name    a unique name to associate with the image so that it can be subsequently referred to without
     *                having to keep a reference to the image itself. Use this name in calls to {@link
     *                #removeImage(String)}.
     * @param image   the image to add.
     * @param corners the geographic location of the image's corners, specified in order of lower-left, lower-right,
     *                upper-right, upper-left.
     *
     * @throws IllegalArgumentException if the image path is null, the corners list is null, contains null values or
     *                                  fewer than four locations.
     * @throws WWRuntimeException       if the image type is unsupported.
     */
    public void addImage(String name, BufferedImage image, List<? extends LatLon> corners)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (corners == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.imageTable.contains(name))
            this.removeImage(name);

        final ArrayList<SurfaceImage> surfaceImages = new ArrayList<SurfaceImage>();
        this.imageTable.put(name, surfaceImages);
        this.imageTiler.tileImage(image, corners, new ImageTiler.ImageTilerListener()
        {
            public void newTile(BufferedImage tileImage, Sector tileSector)
            {
            }

            public void newTile(BufferedImage tileImage, List<? extends LatLon> corners)
            {
                SurfaceImage si = new SurfaceImage(tileImage, corners);
                surfaceImages.add(si);
                si.setOpacity(SurfaceImageLayer.this.getOpacity());
                SurfaceImageLayer.this.addRenderable(si);
            }
        });
    }

    public void removeImage(String imagePath)
    {
        ArrayList<SurfaceImage> images = this.imageTable.get(imagePath);
        if (images == null)
            return;

        this.imageTable.remove(imagePath);

        for (SurfaceImage si : images)
        {
            if (si != null)
                this.removeRenderable(si);
        }
    }

    @Override
    public void setOpacity(double opacity)
    {
        super.setOpacity(opacity);

        for (Map.Entry<String, ArrayList<SurfaceImage>> entry : this.imageTable.entrySet())
        {
            for (SurfaceImage si : entry.getValue())
            {
                if (si != null)
                    si.setOpacity(opacity);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getNumImages()
    {
        int count = 0;

        for (ArrayList<SurfaceImage> images : this.imageTable.values())
        {
            count += images.size();
        }

        return count;
    }

    /**
     * Create an image for the portion of this layer lying within a specified sector. The image is created at a
     * specified aspect ratio within a canvas of a specified size.
     *
     * @param sector       the sector of interest.
     * @param canvasWidth  the width of the canvas.
     * @param canvasHeight the height of the canvas.
     * @param aspectRatio  the aspect ratio, width/height, of the window. If the aspect ratio is greater or equal to
     *                     one, the full width of the canvas is used for the image; the height used is proportional to
     *                     the inverse of the aspect ratio. If the aspect ratio is less than one, the full height of the
     *                     canvas is used, and the width used is proportional to the aspect ratio.
     * @param image        if non-null, a {@link BufferedImage} in which to place the image. If null, a new buffered
     *                     image of type {@link BufferedImage#TYPE_INT_RGB} is created. The image must be the width and
     *                     height specified in the <code>canvasWidth</code> and <code>canvasHeight</code> arguments.
     *
     * @return image        the assembelled image, of size indicated by the <code>canvasWidth</code> and
     *         <code>canvasHeight</code>. If the specified aspect ratio is one, all pixels contain values. If the aspect
     *         ratio is greater than one, a full-width segment along the top of the canvas is blank. If the aspect ratio
     *         is less than one, a full-height segment along the right side of the canvase is blank. If the
     *         <code>image</code> argument was non-null, that buffered image is returned.
     *
     * @see ImageUtil#mergeImage(Sector, Sector, double, BufferedImage, BufferedImage)
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public BufferedImage composeImageForSector(Sector sector, int canvasWidth, int canvasHeight, double aspectRatio,
        BufferedImage image)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!this.getRenderables().iterator().hasNext())
        {
            Logging.logger().severe(Logging.getMessage("generic.NoImagesAvailable"));
            return null;
        }

        if (image == null)
            image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);

        for (Renderable r : this.getRenderables())
        {
            SurfaceImage si = (SurfaceImage) r;

            if (si.getImageSource() == null)
                continue;

            BufferedImage sourceImage = null;
            try
            {
                if (si.getImageSource() instanceof String)
                    sourceImage = ImageIO.read(new File((String) si.getImageSource()));
                else
                    sourceImage = (BufferedImage) si.getImageSource();
            }
            catch (IOException e)
            {
                Logging.logger().severe(Logging.getMessage("generic.ExceptionAttemptingToReadImageFile", sourceImage));
                return null;
            }

            ImageUtil.mergeImage(sector, si.getSector(), aspectRatio, sourceImage, image);
        }

        return image;
    }
}
