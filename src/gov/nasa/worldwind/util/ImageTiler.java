/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.*;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * Subdivides an image into tiles and computes the corresponding sectors. The width and height of the returned tiles can
 * be specified but default to 1024. If the base image width or height is not evenly divisible by the corresponding
 * desired tile dimension, tiles along the right and bottom of the base image may contain pixels that do not correspond
 * to pixels in the image. These pixels will have an alpha component of 0 and the corresponding tile will have an alpha
 * channel. Otherwise tiles will not have an alpha channel. If the input image is already the desired subimage size, it
 * is returned without being copied.
 *
 * @author tag
 * @version $Id: ImageTiler.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImageTiler
{
    public static int DEFAULT_IMAGE_TILE_SIZE = 2048; // default size to make subimages

    private int tileWidth = DEFAULT_IMAGE_TILE_SIZE;
    private int tileHeight = DEFAULT_IMAGE_TILE_SIZE;
    private Color transparencyColor = new Color(0, 0, 0, 0);

    public int getTileWidth()
    {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth)
    {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight()
    {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight)
    {
        this.tileHeight = tileHeight;
    }

    public Color getTransparencyColor()
    {
        return transparencyColor;
    }

    public void setTransparencyColor(Color transparencyColor)
    {
        this.transparencyColor = transparencyColor;
    }

    /**
     * Performs a subdivision according to the current parameters and assuming that the image corresponds with a {@link
     * Sector} rather than a quadrilateral or other shape. Conveys each tile created to the caller via a listener
     * callback.
     *
     * @param baseImage  the image to tile.
     * @param baseSector the sector defining the geographic extent of the base image.
     * @param listener   the listener to invoke when each new tile is created.
     *
     * @see ImageTilerListener
     */
    public void tileImage(BufferedImage baseImage, Sector baseSector, ImageTilerListener listener)
    {
        if (baseImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (baseSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (baseImage.getWidth() <= 0 || baseImage.getHeight() <= 0)
        {
            String message = Logging.getMessage("generic.InvalidImageSize");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (listener == null)
        {
            String message = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Just return the input image if it's already the desired subimage size
        if (baseImage.getWidth() == this.getTileWidth() && baseImage.getHeight() == this.getTileHeight())
        {
            listener.newTile(baseImage, baseSector);
            return;
        }

        int M = baseImage.getWidth();
        int N = baseImage.getHeight();
        int a = Math.min(M, this.getTileWidth());
        int b = Math.min(N, this.getTileHeight());
        int cols = (int) Math.ceil((double) M / a);
        int rows = (int) Math.ceil((double) N / b);
        boolean hasAlpha = baseImage.getColorModel().hasAlpha();

        for (int j = 0; j < rows; j++)
        {
            int y = j * b;
            int h = y + b <= N ? b : N - y;

            double t0 = (double) (y + this.getTileHeight()) / N;
            double t1 = (double) y / N;
            Angle minLat = baseSector.getMaxLatitude().subtract(baseSector.getDeltaLat().multiply(t0));
            Angle maxLat = baseSector.getMaxLatitude().subtract(baseSector.getDeltaLat().multiply(t1));

            for (int i = 0; i < cols; i++)
            {
                int x = i * a;
                int w = x + a <= M ? a : M - x;

                BufferedImage image;
                if (w == this.getTileWidth() && h == this.getTileHeight())
                {
                    // The source image fills this tile entirely,
                    if (!hasAlpha)
                    {
                        // If the source image does not have an alpha channel, create a tile with no alpha channel.
                        image = new BufferedImage(this.getTileWidth(), this.getTileHeight(),
                            BufferedImage.TYPE_3BYTE_BGR);
                        if (!ImageUtil.isCompatibleImage(image))
                            image = ImageUtil.toCompatibleImage(image);
                        Graphics2D g = image.createGraphics();
                        g.drawImage(baseImage.getSubimage(x, y, w, h), 0, 0, w, h, null);
                    }
                    else
                    {
                        // The source image has an alpha channel, create a tile with an alpha channel.
                        image = new BufferedImage(this.getTileWidth(), this.getTileHeight(),
                            BufferedImage.TYPE_4BYTE_ABGR);
                        if (!ImageUtil.isCompatibleImage(image))
                            image = ImageUtil.toCompatibleImage(image);
                        Graphics2D g = image.createGraphics();
                        g.setBackground(this.transparencyColor);
                        g.clearRect(0, 0, image.getWidth(), image.getHeight());
                        g.drawImage(baseImage.getSubimage(x, y, w, h), 0, 0, w, h, null);
                    }

                    // Compute the sector for this tile
                    double s0 = (double) x / M;
                    double s1 = ((double) x + this.getTileWidth()) / M;
                    Angle minLon = baseSector.getMinLongitude().add(baseSector.getDeltaLon().multiply(s0));
                    Angle maxLon = baseSector.getMinLongitude().add(baseSector.getDeltaLon().multiply(s1));

//                    System.out.println(new Sector(minLat, maxLat, minLon, maxLon));
                    listener.newTile(image, new Sector(minLat, maxLat, minLon, maxLon));
                }
                else
                {
                    // The source image does not fill this tile, so create a smaller tile with an alpha channel.
                    int shortWidth = w == this.getTileWidth() ? this.getTileWidth() : WWMath.powerOfTwoCeiling(w);
                    int shortheight = h == this.getTileHeight() ? this.getTileHeight() : WWMath.powerOfTwoCeiling(h);

                    image = new BufferedImage(shortWidth, shortheight, BufferedImage.TYPE_4BYTE_ABGR);
                    if (!ImageUtil.isCompatibleImage(image))
                        image = ImageUtil.toCompatibleImage(image);
                    Graphics2D g = image.createGraphics();
                    g.setBackground(this.transparencyColor);
                    g.clearRect(0, 0, image.getWidth(), image.getHeight());
                    g.drawImage(baseImage.getSubimage(x, y, w, h), 0, 0, w, h, null);

                    // Compute the sector for this tile
                    double s0 = (double) x / M;
                    double s1 = ((double) x + image.getWidth()) / M;
                    Angle minLon = baseSector.getMinLongitude().add(baseSector.getDeltaLon().multiply(s0));
                    Angle maxLon = baseSector.getMinLongitude().add(baseSector.getDeltaLon().multiply(s1));

                    // Must recalculate t0 to account for short tile height.
                    double t00 = (double) (y + image.getHeight()) / N;
                    Angle minLat0 = baseSector.getMaxLatitude().subtract(baseSector.getDeltaLat().multiply(t00));

//                    System.out.println(new Sector(minLat0, maxLat, minLon, maxLon));
                    listener.newTile(image, new Sector(minLat0, maxLat, minLon, maxLon));
                }
            }
        }
    }

    public void tileImage(BufferedImage image, java.util.List<? extends LatLon> corners,ImageTilerListener listener)
    {
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

        if (image.getWidth() <= 0 || image.getHeight() <= 0)
        {
            String message = Logging.getMessage("generic.InvalidImageSize");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (listener == null)
        {
            String message = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Just return the input image if it's already the desired subimage size
        if (image.getWidth() == this.getTileWidth() && image.getHeight() == this.getTileHeight())
        {
            listener.newTile(image, corners);
            return;
        }

        // Count the corners and check for nulls
        int numCorners = 0;
        for (LatLon c : corners)
        {
            if (c == null)
            {
                String message = Logging.getMessage("nullValue.LocationInListIsNull");
                Logging.logger().log(Level.SEVERE, message);
                throw new IllegalArgumentException(message);
            }

            if (++numCorners > 3)
                break;
        }

        if (numCorners < 4)
        {
            String message = Logging.getMessage("nullValue.LocationInListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        GeoQuad geoQuad = new GeoQuad(corners);

        int M = image.getWidth();
        int N = image.getHeight();
        int a = Math.min(M, this.getTileWidth());
        int b = Math.min(N, this.getTileHeight());
        int cols = (int) Math.ceil((double) M / a);
        int rows = (int) Math.ceil((double) N / b);
        boolean hasAlpha = image.getColorModel().hasAlpha();

        for (int j = 0; j < rows; j++)
        {
            LatLon se, sw, ne, nw;

            int y = j * b;
            int h = y + b <= N ? b : N - y;

            double t0 = 1d - (double) (y + this.getTileHeight()) / N;
            double t1 = 1d - (double) y / N;

            for (int i = 0; i < cols; i++)
            {
                int x = i * a;
                int w = x + a <= M ? a : M - x;

                BufferedImage subImage;
                if (w == this.getTileWidth() && h == this.getTileHeight())
                {

                    // The source image fills this tile entirely,
                    if (!hasAlpha)
                    {
                        // If the source image does not have an alpha channel, create a tile with no alpha channel.
                        subImage = new BufferedImage(this.getTileWidth(), this.getTileHeight(),
                            BufferedImage.TYPE_3BYTE_BGR);
                        Graphics2D g = subImage.createGraphics();
                        g.drawImage(image.getSubimage(x, y, w, h), 0, 0, w, h, null);
                        
                        continue;
                    }
                    else
                    {
                        // The source image has an alpha channel, create a tile with an alpha channel.
                        subImage = new BufferedImage(this.getTileWidth(), this.getTileHeight(),
                            BufferedImage.TYPE_4BYTE_ABGR);
                        Graphics2D g = subImage.createGraphics();
                        g.setBackground(this.transparencyColor);
                        g.clearRect(0, 0, subImage.getWidth(), subImage.getHeight());
                        g.drawImage(image.getSubimage(x, y, w, h), 0, 0, w, h, null);
                    }

                    // Compute the sector for this tile
                    double s0 = (double) x / M;
                    double s1 = ((double) x + this.getTileWidth()) / M;

                    sw = geoQuad.interpolate(t0, s0);
                    se = geoQuad.interpolate(t0, s1);
                    ne = geoQuad.interpolate(t1, s1);
                    nw = geoQuad.interpolate(t1, s0);
                }
                else
                {
                    // The source image does not fill this tile, so create a smaller tile with an alpha channel.
                    int shortWidth = w == this.getTileWidth() ? this.getTileWidth() : WWMath.powerOfTwoCeiling(w);
                    int shortheight = h == this.getTileHeight() ? this.getTileHeight() : WWMath.powerOfTwoCeiling(h);

                    subImage = new BufferedImage(shortWidth, shortheight, BufferedImage.TYPE_4BYTE_ABGR);
                    Graphics2D g = subImage.createGraphics();
                    g.setBackground(this.transparencyColor);
                    g.clearRect(0, 0, subImage.getWidth(), subImage.getHeight());
                    g.drawImage(image.getSubimage(x, y, w, h), 0, 0, w, h, null);

                    // Compute the sector for this tile
                    double s0 = (double) x / M;
                    double s1 = ((double) x + subImage.getWidth()) / M;

                    // Must recalculate t0 to account for short tile height.
                    double t0b = 1d - (double) (y + subImage.getHeight()) / N;

                    sw = geoQuad.interpolate(t0b, s0);
                    se = geoQuad.interpolate(t0b, s1);
                    ne = geoQuad.interpolate(t1, s1);
                    nw = geoQuad.interpolate(t1, s0);
                }

//                System.out.printf("%d: (%d, %d) : SW %s; SE %s; NE %s; NW %s\n",
//                    System.currentTimeMillis(), x, y, sw, se, ne, nw);

                listener.newTile(subImage, Arrays.asList(sw,se, ne, nw));
            }
        }
    }

    public abstract static class ImageTilerListener
    {
        public abstract void newTile(BufferedImage tileImage, Sector tileSector);

        public abstract void newTile(BufferedImage tileImage, List<? extends LatLon> corners);
    }
}
