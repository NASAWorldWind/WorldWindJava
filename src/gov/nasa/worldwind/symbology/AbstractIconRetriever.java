/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;
import java.net.URL;

/**
 * Base class for icon retrievers. This class provides methods for loading and manipulating icons.
 * <p/>
 * <h2>Icon retrieval</h2>
 * <p/>
 * Each symbol in a symbology set must have a unique identifier. The IconRetriever's job is to create a BufferedImage to
 * represent a symbol given the symbol identifier. Usually this means retrieving an image from the file system or the
 * network, and optionally manipulating the symbol (for example, changing the color to represent a hostile or friendly
 * entity).
 * <p/>
 * Each instance of AbstractIconRetriever is configured with a retrieval path which specifies the location of a symbol
 * repository on the file system or the network. {@link #readImage(String) readImage} retrieves images relative to this
 * base path. The retrieval path may be a file URL to a directory on the local file system (for example,
 * file:///symbols/mil-std-2525). A URL to a network resource (http://myserver.com/milstd2525/), or a URL to a JAR or
 * ZIP file (jar:file:milstd2525-symbols.zip!).
 * <p/>
 * A simple icon retriever might use a symbol repository that is a simple directory of PNG files, where each file name
 * matches a symbol identifier. Such an icon retriever could be implemented like this:
 * <p/>
 * <pre>
 * class SimpleIconRetriever extends AbstractIconRetriever
 * {
 *     public BufferedImage createIcon(String symbolId)
 *     {
 *         // Retrieves retrievalPath/symbolId.png
 *         return this.readImage(symbolId + ".png");
 *     }
 * }
 * </pre>
 * <p/>
 * <h2>Composite icons</h2>
 * <p/>
 * Complicated symbols may be made up of several different graphical elements. {@link
 * #drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImage) drawImage} helps build a complex symbol from
 * simple pieces. For example, if a symbol is composed of a frame and an icon, the icon retriever could load the frame
 * and icon independently, draw the icon over the frame, and return the composite image:
 * <pre>
 * // Load the frame and icon as separate pieces.
 * BufferedImage frame = this.readImage("path/to/frame.png");
 * BufferedImage icon = this.readImage("path/to/icon.png");
 *
 * // Draw the icon on top of the frame. This call modifies the frame image.
 * BufferedImage fullImage = this.drawImage(icon, frame);
 *
 * // Return the composite image.
 * return fullImage;
 * </pre>
 * <p/>
 * <h2>Changing the color of an icon</h2>
 * <p/>
 * {@link #multiply(java.awt.image.BufferedImage, java.awt.Color) multiply} can change the color of an image by
 * multiplying each pixel in the image by a color. The multiplication color will replace any white pixels and black
 * pixels will be unaffected. For example, a symbol set in which hostile symbols are drawn in red and friendly symbols
 * are drawn in green could be implemented by creating white icons, and then multiplying by either red or green when the
 * retriever constructs the icon.
 *
 * @author ccrick
 * @version $Id: AbstractIconRetriever.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractIconRetriever implements IconRetriever
{
    /** Path in the file system or network to the symbol repository. */
    protected String retrieverPath;

    /**
     * Create a new retriever that will retrieve icons from the specified location. The retrieval path may be a file URL
     * to a directory on the local file system (for example, file:///symbols/mil-std-2525). A URL to a network resource
     * (http://myserver.com/milstd2525/), or a URL to a JAR or ZIP file (jar:file:milstd2525-symbols.zip!).
     *
     * @param retrieverPath URL to to the base symbol directory on the local file system or the network.
     */
    public AbstractIconRetriever(String retrieverPath)
    {
        if (retrieverPath == null || retrieverPath.length() == 0)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.retrieverPath = retrieverPath;
    }

    /**
     * Indicates the file system or network path of the symbol directory.. The retrieval path may be a file URL to a
     * directory on the local file system (for example, file:///symbols/mil-std-2525). A URL to a network resource (
     * http://myserver.com/milstd2525/), or a URL to a JAR or ZIP file (jar:file:milstd2525-symbols.zip!).
     *
     * @return File system or network path to symbol repository.
     */
    public String getRetrieverPath()
    {
        return this.retrieverPath;
    }

    /**
     * Indicates whether or not this retriever is equal to another.
     *
     * @param o Object to compare.
     *
     * @return {@code true} if {@code o} is an instance of AbstractIconRetriever and has the same retrieval path as this
     *         retriever.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        AbstractIconRetriever that = (AbstractIconRetriever) o;
        return this.retrieverPath != null ? this.retrieverPath.equals(that.retrieverPath) : that.retrieverPath == null;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return this.retrieverPath != null ? this.retrieverPath.hashCode() : 0;
    }

    /**
     * Load an image from a local or remote path. The image path is interpreted relative to the retrieval path. For
     * example, if the retrieval path is http://myserver.com/milstd2525/, calling readImage("icon.png") will attempt to
     * retrieve an image from http://myserver.com/milstd2525/icon.png.
     *
     * @param path Path to the icon resource, relative to this retriever's retrieval path.
     *
     * @return The requested icon as a BufferedImage, or null if the icon cannot be loaded.
     */
    protected BufferedImage readImage(String path)
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(WWIO.stripTrailingSeparator(this.getRetrieverPath()));
        sb.append("/");
        sb.append(WWIO.stripLeadingSeparator(path));

        InputStream is = null;
        try
        {
            URL url = WWIO.makeURL(sb.toString());
            if (url != null)
                return ImageIO.read(url);

            is = WWIO.openFileOrResourceStream(sb.toString(), this.getClass());
            if (is != null)
                return ImageIO.read(is);
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("generic.ExceptionWhileReading", sb.toString());
            Logging.logger().fine(msg);
        }
        finally
        {
            WWIO.closeStream(is, sb.toString());
        }

        return null;
    }

    /**
     * Draw one image into another image. The image is drawn at location (0, 0).
     *
     * @param src  Image to draw.
     * @param dest Image to draw into.
     *
     * @return {@code dest} BufferedImage.
     */
    protected BufferedImage drawImage(BufferedImage src, BufferedImage dest)
    {
        if (src == null)
        {
            String msg = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dest == null)
        {
            String msg = Logging.getMessage("nullValue.DestinationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Graphics2D g = null;
        try
        {
            g = dest.createGraphics();
            g.drawImage(src, 0, 0, null);
        }
        finally
        {
            if (g != null)
                g.dispose();
        }

        return dest;
    }

    /**
     * Multiply each pixel in an image by a color. White pixels are replaced by the multiplication color, black pixels
     * are unaffected.
     *
     * @param image Image to operate on.
     * @param color Color to multiply by.
     *
     * @see #replaceColor(java.awt.image.BufferedImage, java.awt.Color)
     */
    protected void multiply(BufferedImage image, Color color)
    {
        if (image == null)
        {
            String msg = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int w = image.getWidth();
        int h = image.getHeight();

        if (w == 0 || h == 0)
            return;

        int[] pixels = new int[w];
        int c = color.getRGB();
        float ca = ((c >> 24) & 0xff) / 255f;
        float cr = ((c >> 16) & 0xff) / 255f;
        float cg = ((c >> 8) & 0xff) / 255f;
        float cb = (c & 0xff) / 255f;

        for (int y = 0; y < h; y++)
        {
            image.getRGB(0, y, w, 1, pixels, 0, w);

            for (int x = 0; x < w; x++)
            {
                int s = pixels[x];
                float sa = ((s >> 24) & 0xff) / 255f;
                float sr = ((s >> 16) & 0xff) / 255f;
                float sg = ((s >> 8) & 0xff) / 255f;
                float sb = (s & 0xff) / 255f;

                int fa = (int) (ca * sa * 255 + 0.5);
                int fr = (int) (cr * sr * 255 + 0.5);
                int fg = (int) (cg * sg * 255 + 0.5);
                int fb = (int) (cb * sb * 255 + 0.5);

                pixels[x] = (fa & 0xff) << 24
                    | (fr & 0xff) << 16
                    | (fg & 0xff) << 8
                    | (fb & 0xff);
            }

            image.setRGB(0, y, w, 1, pixels, 0, w);
        }
    }

    /**
     * Replace the color of each pixel in an image. This method retains the alpha channel of each pixel, but completely
     * replaces the red, green, and blue components with the replacement color. Unlike {@link
     * #multiply(java.awt.image.BufferedImage, java.awt.Color) multiply}, this method changes the color of all pixels.
     *
     * @param image Image to operate on.
     * @param color Color to apply to to each pixel.
     *
     * @see #multiply(java.awt.image.BufferedImage, java.awt.Color)
     */
    protected void replaceColor(BufferedImage image, Color color)
    {
        if (image == null)
        {
            String msg = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        int w = image.getWidth();
        int h = image.getHeight();

        if (w == 0 || h == 0)
            return;

        int[] pixels = new int[w];
        int c = color.getRGB();
        float cr = ((c >> 16) & 0xff) / 255f;
        float cg = ((c >> 8) & 0xff) / 255f;
        float cb = (c & 0xff) / 255f;

        for (int y = 0; y < h; y++)
        {
            image.getRGB(0, y, w, 1, pixels, 0, w);

            for (int x = 0; x < w; x++)
            {
                int s = pixels[x];
                float sa = ((s >> 24) & 0xff) / 255f;

                int fa = (int) (sa * 255 + 0.5);
                int fr = (int) (cr * 255 + 0.5);
                int fg = (int) (cg * 255 + 0.5);
                int fb = (int) (cb * 255 + 0.5);

                pixels[x] = (fa & 0xff) << 24
                    | (fr & 0xff) << 16
                    | (fg & 0xff) << 8
                    | (fb & 0xff);
            }

            image.setRGB(0, y, w, 1, pixels, 0, w);
        }
    }
}
