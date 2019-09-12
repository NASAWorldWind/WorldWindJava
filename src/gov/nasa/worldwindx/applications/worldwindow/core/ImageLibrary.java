/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tag
 * @version $Id: ImageLibrary.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImageLibrary
{
    // These images are available for situation where a desired image is not available.
    private static final String[] WARNING_IMAGES = new String[]
        {
            "gov/nasa/worldwindx/applications/worldwindow/images/warning16.png",
            "gov/nasa/worldwindx/applications/worldwindow/images/warning24.png",
            "gov/nasa/worldwindx/applications/worldwindow/images/warning32.png",
            "gov/nasa/worldwindx/applications/worldwindow/images/warning64.png"
        };

    private static ImageLibrary instance;

    /**
     * Specify the instance for this conceptual singleton.
     *
     * @param library the image library instance.
     */
    public static void setInstance(ImageLibrary library)
    {
        instance = library;
    }

    private ConcurrentHashMap<String, BufferedImage> imageMap = new ConcurrentHashMap<String, BufferedImage>();
    private ConcurrentHashMap<String, ImageIcon> iconMap = new ConcurrentHashMap<String, ImageIcon>();

    public ImageLibrary()
    {
        this.loadWarningImages();
    }

    protected void loadWarningImages()
    {
        for (String imageName : WARNING_IMAGES)
        {
            try
            {
                InputStream is = WWIO.openFileOrResourceStream(imageName, this.getClass());
                this.imageMap.put(imageName, ImageUtil.toCompatibleImage(ImageIO.read(is)));
            }
            catch (Exception e)
            {
                Util.getLogger().log(java.util.logging.Level.WARNING,
                    e.getMessage() + " Stand-in image, name is " + imageName, e);
            }
        }
    }

    /**
     * Returns a warning image, an image that can be used when a desired image is not available.
     *
     * @param size the desired image size in pixels, either 16, 24, 32 or 64.
     *
     * @return a warning image of the requested size, or one of size 64 if a size larger than 64 is requested.
     */
    public static BufferedImage getWarningImage(int size)
    {
        if (size < 24)
            return getImage(WARNING_IMAGES[0]);
        else if (size < 32)
            return getImage(WARNING_IMAGES[1]);
        else if (size < 64)
            return getImage(WARNING_IMAGES[2]);
        else
            return getImage(WARNING_IMAGES[3]);
    }

    /**
     * Returns a warning icon, an icon that can be used when a desired icon is not available.
     *
     * @param size the desired icon size in pixels, either 16, 24, 32 or 64.
     *
     * @return a warning icon of the requested size, or one of size 64 if a size larger than 64 is requested.
     */
    public static Icon getWarningIcon(int size)
    {
        if (size < 24)
            return getIcon(WARNING_IMAGES[0]);
        else if (size < 32)
            return getIcon(WARNING_IMAGES[1]);
        else if (size < 64)
            return getIcon(WARNING_IMAGES[2]);
        else
            return getIcon(WARNING_IMAGES[3]);
    }

    /**
     * Retrun the image associated with a specified name. If the library does not contain an image of the specified
     * name, it is first searched for in the application's classpath, and if not found retrieved from this instance's
     * image server.
     *
     * @param imageName the name of the desired image.
     *
     * @return the image if it's available, otherwise null.
     */
    public static synchronized BufferedImage getImage(String imageName)
    {
        try
        {
            BufferedImage image = !WWUtil.isEmpty(imageName) ? instance.imageMap.get(imageName) : null;
            if (image != null)
                return image;

            URL url = getImageURL(imageName);
            if (url != null)
            {
                image = ImageIO.read(url);
                if (image != null)
                {
                    image = ImageUtil.toCompatibleImage(image);
                    register(imageName, image);
                    return image;
                }
            }

            return null;
        }
        catch (IOException e)
        {
            Util.getLogger().log(java.util.logging.Level.SEVERE,
                e.getMessage() + " Image name " + (imageName != null ? imageName : null), e);
            return null;
        }
    }

    public static synchronized URL getImageURL(String imageName)
    {
        URL url = instance.getClass().getResource(imageName); // look locallly
        if (url == null)
            url = instance.getClass().getResource("/" + imageName); // look locallly
        if (url == null)
            url = instance.getClass().getResource("images" + File.separatorChar + imageName);
        if (url == null)
            url = instance.getClass().getResource("/images" + File.separatorChar + imageName);
        if (url == null)
            url = instance.getClass().getResource(
                "gov/nasa/worldwindx/applications/worldwindow/images" + File.separatorChar + imageName);
        if (url == null)
            url = instance.getClass().getResource(
                "/gov/nasa/worldwindx/applications/worldwindow/images" + File.separatorChar + imageName);

        return url;
    }

    /**
     * Retrun the icon associated with a specified name. If the library does not contain an icon of the specified name,
     * it is first searched for in the application's classpath, and if not found retrieved from this instance's image
     * server.
     *
     * @param iconName the name of the desired icon.
     *
     * @return the icon if it's available, otherwise null.
     */
    public static synchronized ImageIcon getIcon(String iconName)
    {
        try
        {
            ImageIcon icon = !WWUtil.isEmpty(iconName) ? instance.iconMap.get(iconName) : null;
            if (icon != null)
                return icon;

            // Load it as an image first, because image failures occur immediately.
            BufferedImage image = getImage(iconName);
            if (image != null)
            {
                icon = new ImageIcon(image);
                register(iconName, icon);
                return icon;
            }

            return null;
        }
        catch (Exception e)
        {
            Util.getLogger().log(java.util.logging.Level.SEVERE,
                e.getMessage() + " Icon name " + (iconName != null ? iconName : null), e);
            return null;
        }
    }

    /**
     * Returns the image associated with a specified icon.
     *
     * @param icon the icon whose image is desired.
     *
     * @return the image associated with the icon, or null if the icon is not available.
     */
    public static BufferedImage getImageForIcon(Icon icon)
    {
        if (icon == null)
            return null;

        return getImage(getIconName(icon));
    }

    /**
     * Register an image with the library.
     *
     * @param name  the image name. If null the image is not registered.
     * @param image the image. If null the image is not registered.
     *
     * @return the reference to the image passed in the <code>image</code> argument.
     */
    public static synchronized Object register(String name, Object image)
    {
        if (!WWUtil.isEmpty(name) && image != null)
        {
            if (image instanceof BufferedImage)
                instance.imageMap.put(name, (BufferedImage) image);
            else if (image instanceof ImageIcon)
                instance.iconMap.put(name, (ImageIcon) image);
        }

        return image;
    }

    /**
     * Return the name associated with a specified image.
     *
     * @param image the image whose name to return.
     *
     * @return the image name, or null if the image is not registered with this instance.
     */
    public static String getImageName(BufferedImage image)
    {
        for (Map.Entry<String, BufferedImage> entry : instance.imageMap.entrySet())
        {
            if (entry.getValue() == image)
                return entry.getKey();
        }

        return null;
    }

    /**
     * Return the name associated with a specified icon.
     *
     * @param icon the icon whose name to return.
     *
     * @return the icon name, or null if the icon is not registered with this instance.
     */
    public static String getIconName(Icon icon)
    {
        for (Map.Entry<String, ImageIcon> entry : instance.iconMap.entrySet())
        {
            if (entry.getValue() == icon)
                return entry.getKey();
        }

        return null;
    }
}
