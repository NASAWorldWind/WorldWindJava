/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.layers.mercator;

import com.jogamp.opengl.util.texture.*;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.*;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * BasicTiledImageLayer modified 2009-02-03 to add support for Mercator projections.
 *
 * @author tag
 * @version $Id: BasicMercatorTiledImageLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicMercatorTiledImageLayer extends MercatorTiledImageLayer
{
    private final Object fileLock = new Object();

    public BasicMercatorTiledImageLayer(LevelSet levelSet)
    {
        super(levelSet);

        if (!WorldWind.getMemoryCacheSet().containsCache(MercatorTextureTile.class.getName()))
        {
            long size = Configuration.getLongValue(
                AVKey.TEXTURE_IMAGE_CACHE_SIZE, 3000000L);
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName("Texture Tiles");
            WorldWind.getMemoryCacheSet().addCache(MercatorTextureTile.class.getName(), cache);
        }
    }

    public BasicMercatorTiledImageLayer(AVList params)
    {
        this(new LevelSet(params));
        this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params);
    }

    protected void forceTextureLoad(MercatorTextureTile tile)
    {
        final URL textureURL = this.getDataFileStore().findFile(
            tile.getPath(), true);

        if (textureURL != null && !this.isTextureExpired(tile, textureURL))
        {
            this.loadTexture(tile, textureURL);
        }
    }

    protected void requestTexture(DrawContext dc, MercatorTextureTile tile)
    {
        Vec4 centroid = tile.getCentroidPoint(dc.getGlobe());
        if (this.getReferencePoint() != null)
            tile.setPriority(centroid.distanceTo3(this.getReferencePoint()));

        RequestTask task = new RequestTask(tile, this);
        this.getRequestQ().add(task);
    }

    private static class RequestTask implements Runnable,
        Comparable<RequestTask>
    {
        private final BasicMercatorTiledImageLayer layer;
        private final MercatorTextureTile tile;

        private RequestTask(MercatorTextureTile tile,
            BasicMercatorTiledImageLayer layer)
        {
            this.layer = layer;
            this.tile = tile;
        }

        public void run()
        {
            // TODO: check to ensure load is still needed

            final java.net.URL textureURL = this.layer.getDataFileStore()
                .findFile(tile.getPath(), false);
            if (textureURL != null
                && !this.layer.isTextureExpired(tile, textureURL))
            {
                if (this.layer.loadTexture(tile, textureURL))
                {
                    layer.getLevels().unmarkResourceAbsent(tile);
                    this.layer.firePropertyChange(AVKey.LAYER, null, this);
                    return;
                }
                else
                {
                    // Assume that something's wrong with the file and delete it.
                    this.layer.getDataFileStore().removeFile(
                        textureURL);
                    layer.getLevels().markResourceAbsent(tile);
                    String message = Logging.getMessage(
                        "generic.DeletedCorruptDataFile", textureURL);
                    Logging.logger().info(message);
                }
            }

            this.layer.downloadTexture(this.tile);
        }

        /**
         * @param that the task to compare
         *
         * @return -1 if <code>this</code> less than <code>that</code>, 1 if greater than, 0 if equal
         *
         * @throws IllegalArgumentException if <code>that</code> is null
         */
        public int compareTo(RequestTask that)
        {
            if (that == null)
            {
                String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return this.tile.getPriority() == that.tile.getPriority() ? 0
                : this.tile.getPriority() < that.tile.getPriority() ? -1
                    : 1;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final RequestTask that = (RequestTask) o;

            // Don't include layer in comparison so that requests are shared among layers
            return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
        }

        public int hashCode()
        {
            return (tile != null ? tile.hashCode() : 0);
        }

        public String toString()
        {
            return this.tile.toString();
        }
    }

    private boolean isTextureExpired(MercatorTextureTile tile,
        java.net.URL textureURL)
    {
        if (!WWIO.isFileOutOfDate(textureURL, tile.getLevel().getExpiryTime()))
            return false;

        // The file has expired. Delete it.
        this.getDataFileStore().removeFile(textureURL);
        String message = Logging.getMessage("generic.DataFileExpired",
            textureURL);
        Logging.logger().fine(message);
        return true;
    }

    private boolean loadTexture(MercatorTextureTile tile,
        java.net.URL textureURL)
    {
        TextureData textureData;

        synchronized (this.fileLock)
        {
            textureData = readTexture(textureURL, this.isUseMipMaps());
        }

        if (textureData == null)
            return false;

        tile.setTextureData(textureData);
        if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
            this.addTileToCache(tile);

        return true;
    }

    private static TextureData readTexture(java.net.URL url, boolean useMipMaps)
    {
        try
        {
            return OGLUtil.newTextureData(Configuration.getMaxCompatibleGLProfile(), url, useMipMaps);
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", url.toString());
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
            return null;
        }
    }

    private void addTileToCache(MercatorTextureTile tile)
    {
        WorldWind.getMemoryCache(MercatorTextureTile.class.getName()).add(
            tile.getTileKey(), tile);
    }

    protected void downloadTexture(final MercatorTextureTile tile)
    {
        if (!WorldWind.getRetrievalService().isAvailable())
            return;

        java.net.URL url;
        try
        {
            url = tile.getResourceURL();
            if (url == null)
                return;

            if (WorldWind.getNetworkStatus().isHostUnavailable(url))
                return;
        }
        catch (java.net.MalformedURLException e)
        {
            Logging.logger().log(
                java.util.logging.Level.SEVERE,
                Logging.getMessage(
                    "layers.TextureLayer.ExceptionCreatingTextureUrl",
                    tile), e);
            return;
        }

        Retriever retriever;

        if ("http".equalsIgnoreCase(url.getProtocol()))
        {
            retriever = new HTTPRetriever(url, new DownloadPostProcessor(tile, this));
            retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true"); // supports legacy layers
        }
        else
        {
            Logging.logger().severe(
                Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", url.toString()));
            return;
        }

        // Apply any overridden timeouts.
        Integer cto = AVListImpl.getIntegerValue(this,
            AVKey.URL_CONNECT_TIMEOUT);
        if (cto != null && cto > 0)
            retriever.setConnectTimeout(cto);
        Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
        if (cro != null && cro > 0)
            retriever.setReadTimeout(cro);
        Integer srl = AVListImpl.getIntegerValue(this,
            AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
        if (srl != null && srl > 0)
            retriever.setStaleRequestLimit(srl);

        WorldWind.getRetrievalService().runRetriever(retriever,
            tile.getPriority());
    }

    private void saveBuffer(java.nio.ByteBuffer buffer, java.io.File outFile)
        throws java.io.IOException
    {
        synchronized (this.fileLock) // synchronized with read of file in RequestTask.run()
        {
            WWIO.saveBuffer(buffer, outFile);
        }
    }

    private static class DownloadPostProcessor implements
        RetrievalPostProcessor
    {
        // TODO: Rewrite this inner class, factoring out the generic parts.
        private final MercatorTextureTile tile;
        private final BasicMercatorTiledImageLayer layer;

        public DownloadPostProcessor(MercatorTextureTile tile,
            BasicMercatorTiledImageLayer layer)
        {
            this.tile = tile;
            this.layer = layer;
        }

        public ByteBuffer run(Retriever retriever)
        {
            if (retriever == null)
            {
                String msg = Logging.getMessage("nullValue.RetrieverIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            try
            {
                if (!retriever.getState().equals(
                    Retriever.RETRIEVER_STATE_SUCCESSFUL))
                    return null;

                URLRetriever r = (URLRetriever) retriever;
                ByteBuffer buffer = r.getBuffer();

                if (retriever instanceof HTTPRetriever)
                {
                    HTTPRetriever htr = (HTTPRetriever) retriever;
                    if (htr.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT)
                    {
                        // Mark tile as missing to avoid excessive attempts
                        this.layer.getLevels().markResourceAbsent(this.tile);
                        return null;
                    }
                    else if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
                    {
                        // Also mark tile as missing, but for an unknown reason.
                        this.layer.getLevels().markResourceAbsent(this.tile);
                        return null;
                    }
                }

                final File outFile = this.layer.getDataFileStore().newFile(
                    this.tile.getPath());
                if (outFile == null)
                    return null;

                if (outFile.exists())
                    return buffer;

                // TODO: Better, more generic and flexible handling of file-format type
                if (buffer != null)
                {
                    String contentType = r.getContentType();
                    if (contentType == null)
                    {
                        // TODO: logger message
                        return null;
                    }

                    if (contentType.contains("xml")
                        || contentType.contains("html")
                        || contentType.contains("text"))
                    {
                        this.layer.getLevels().markResourceAbsent(this.tile);

                        StringBuffer sb = new StringBuffer();
                        while (buffer.hasRemaining())
                        {
                            sb.append((char) buffer.get());
                        }
                        // TODO: parse out the message if the content is xml or html.
                        Logging.logger().severe(sb.toString());

                        return null;
                    }
                    else if (contentType.contains("dds"))
                    {
                        this.layer.saveBuffer(buffer, outFile);
                    }
                    else if (contentType.contains("zip"))
                    {
                        // Assume it's zipped DDS, which the retriever would have unzipped into the buffer.
                        this.layer.saveBuffer(buffer, outFile);
                    }
//                    else if (outFile.getName().endsWith(".dds"))
//                    {
//                        // Convert to DDS and save the result.
//                        buffer = DDSConverter.convertToDDS(buffer, contentType);
//                        if (buffer != null)
//                            this.layer.saveBuffer(buffer, outFile);
//                    }
                    else if (contentType.contains("image"))
                    {
                        BufferedImage image = this.layer.convertBufferToImage(buffer);
                        if (image != null)
                        {
                            image = this.layer.modifyImage(image);
                            if (this.layer.isTileValid(image))
                            {
                                if (!this.layer.transformAndSave(image, tile.getMercatorSector(), outFile))
                                    image = null;
                            }
                            else
                            {
                                this.layer.getLevels().markResourceAbsent(this.tile);
                                return null;
                            }
                        }
                        if (image == null)
                        {
                            // Just save whatever it is to the cache.
                            this.layer.saveBuffer(buffer, outFile);
                        }
                    }

                    if (buffer != null)
                    {
                        this.layer.firePropertyChange(AVKey.LAYER, null, this);
                    }
                    return buffer;
                }
            }
            catch (java.io.IOException e)
            {
                this.layer.getLevels().markResourceAbsent(this.tile);
                Logging.logger().log(java.util.logging.Level.SEVERE,
                    Logging.getMessage("layers.TextureLayer.ExceptionSavingRetrievedTextureFile", tile.getPath()), e);
            }
            return null;
        }
    }

    protected boolean isTileValid(BufferedImage image)
    {
        //override in subclass to check image tile
        //if false is returned, then tile is marked absent
        return true;
    }

    protected BufferedImage modifyImage(BufferedImage image)
    {
        //override in subclass to modify image tile
        return image;
    }

    private BufferedImage convertBufferToImage(ByteBuffer buffer)
    {
        try
        {
            InputStream is = new ByteArrayInputStream(buffer.array());
            return ImageIO.read(is);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private boolean transformAndSave(BufferedImage image, MercatorSector sector,
        File outFile)
    {
        try
        {
            image = transform(image, sector);
            String extension = outFile.getName().substring(
                outFile.getName().lastIndexOf('.') + 1);
            synchronized (this.fileLock) // synchronized with read of file in RequestTask.run()
            {
                return ImageIO.write(image, extension, outFile);
            }
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private BufferedImage transform(BufferedImage image, MercatorSector sector)
    {
        int type = image.getType();
        if (type == 0)
            type = BufferedImage.TYPE_INT_RGB;
        BufferedImage trans = new BufferedImage(image.getWidth(), image
            .getHeight(), type);
        double miny = sector.getMinLatPercent();
        double maxy = sector.getMaxLatPercent();
        for (int y = 0; y < image.getHeight(); y++)
        {
            double sy = 1.0 - y / (double) (image.getHeight() - 1);
            Angle lat = Angle.fromRadians(sy * sector.getDeltaLatRadians()
                + sector.getMinLatitude().radians);
            double dy = 1.0 - (MercatorSector.gudermannianInverse(lat) - miny)
                / (maxy - miny);
            dy = Math.max(0.0, Math.min(1.0, dy));
            int iy = (int) (dy * (image.getHeight() - 1));

            for (int x = 0; x < image.getWidth(); x++)
            {
                trans.setRGB(x, y, image.getRGB(x, iy));
            }
        }
        return trans;
    }
}
