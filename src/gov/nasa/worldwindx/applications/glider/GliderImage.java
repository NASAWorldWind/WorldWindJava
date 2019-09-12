/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.glider;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.awt.image.*;
import java.beans.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author tag
 * @version $Id: GliderImage.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GliderImage extends AVListImpl
{
    public static final String GLIDER_REGIONS_OF_INTEREST = "gov.nasa.worldwind.glider.RegionsOfInterest";
    public static final String GLIDER_IMAGE_SOURCE = "gov.nasa.worldwind.glider.ImageSource";
    public static final String GLIDER_IMAGE_OPACITY = "gov.nasa.worldwind.glider.ImageOpacity";

    protected String name;
    protected Sector sector;
    protected List<LatLon> corners;
    protected double altitude;
    protected double opacity = 1;
    protected Object imageSource;
    protected PropertyChangeListener regionListener = new RegionListener();

    private CopyOnWriteArraySet<GliderRegionOfInterest> regionTable
        = new CopyOnWriteArraySet<GliderRegionOfInterest>();

    /**
     * Construct an image from a file.
     *
     * @param imageSource The path to the source image. Images can be any of those supported by {@link
     *                    javax.imageio.ImageIO}, as well as uncompressed TIFF images..
     * @param corners     The lat/lon locations of the region in which to map the image. The image will be stretched as
     *                    necessary to fully fill the region. The locations must be specified in counterclockwise order
     *                    beginning with the lower-left image corner.
     * @param altitude    The altitude at which to display the image. Specify 0 to have the image draped over the
     *                    globe's surface.
     *
     * @throws IllegalArgumentException if any of the first three arguments are null.
     */
    public GliderImage(String imageSource, Iterable<? extends LatLon> corners, double altitude)
    {
        this(imageSource, imageSource, corners, altitude);
    }

    /**
     * Construct an image from a file or {@link java.awt.image.BufferedImage} and an arbitrary bounding region.
     *
     * @param name         A unique name to identify the image. If the image source is a file, the file path can be used
     *                     as the name.
     * @param alignedImage An aligned image containing a {@link BufferedImage} and a {@link Sector} specifying the image
     *                     and the location to place it.The image will be stretched as necessary to fully fill the
     *                     region.
     * @param altitude     The altitude at which to display the image. Specify 0 to have the image draped over the
     *                     globe's surface.
     *
     * @throws IllegalArgumentException if any of the first three arguments are null.
     */
    public GliderImage(String name, ImageUtil.AlignedImage alignedImage, double altitude)
    {
        this(name, alignedImage.image, alignedImage.sector, altitude);
    }

    /**
     * Construct an image from a file or {@link java.awt.image.BufferedImage} and an arbitrary bounding region.
     *
     * @param name        A unique name to identify the image. If the image source is a file, the file path can be used
     *                    as the name.
     * @param imageSource Either the file path to the source image or a reference to the {@link
     *                    java.awt.image.BufferedImage} containing it. Images can be any of those supported by {@link
     *                    javax.imageio.ImageIO}, as well as uncompressed TIFF images.
     * @param corners     The lat/lon locations of the region in which to map the image. The image will be stretched as
     *                    necessary to fully fill the region. The locations must be specified in counterclockwise order
     *                    beginning with the lower-left image corner.
     * @param altitude    The altitude at which to display the image. Specify 0 to have the image draped over the
     *                    globe's surface.
     *
     * @throws IllegalArgumentException if any of the first three arguments are null.
     */
    public GliderImage(String name, Object imageSource, Iterable<? extends LatLon> corners, double altitude)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (imageSource == null)
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

        this.name = name;
        this.imageSource = imageSource;
        this.sector = Sector.boundingSector(corners);
        this.altitude = altitude;

        this.corners = new ArrayList<LatLon>();
        for (LatLon c : corners)
        {
            this.corners.add(c);
        }
    }
//
//    /**
//     * Copy constructor. A shallow copy is performed.
//     *
//     * @param image the image to copy from.
//     *
//     * @throws IllegalArgumentException if <code>image</code> is null.
//     */
//    public GliderImage(GliderImage image)
//    {
//        if (image == null)
//        {
//            String message = Logging.getMessage("nullValue.ImageIsNull");
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        this.name = image.getName();
//        this.imageSource = image.getImageSource();
//        this.sector = image.getSector();
//        this.altitude = image.getAltitude();
//        this.corners = image.corners;
//    }

    public void releaseImageSource()
    {
        this.imageSource = null;
    }

    /**
     * Returns the name of the image, as specified at construction. If no name was specified at construction the name is
     * that of the image file path.
     *
     * @return the image name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Return the image's location.
     *
     * @return the image's location.
     */
    public Sector getSector()
    {
        return sector;
    }

    public List<LatLon> getCorners()
    {
        return Collections.unmodifiableList(this.corners);
    }

    /**
     * Return the image's altitude.
     *
     * @return the image's altitude.
     */
    public double getAltitude()
    {
        return altitude;
    }

    /**
     * Changes the image source. The allowable sources are those allowed by {@link #GliderImage}
     *
     * @param newSource the new image source.
     *
     * @throws IllegalArgumentException if <code>newSource</code> is null.
     */
    public void setImageSource(String newSource)
    {
        if (newSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setImageSource(newSource, newSource);
    }

    /**
     * Changes the image source and gives the image a new name. The allowable sources are those allowed by {@link
     * #GliderImage}
     *
     * @param newName   the new image name.
     * @param newSource the new image source.
     *
     * @throws IllegalArgumentException if either argument is null.
     */
    public void setImageSource(String newName, Object newSource)
    {
        if (newName == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (newSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

//        GliderImage oldImage = new GliderImage(this);
        this.name = newName;
        this.imageSource = newSource;
        this.firePropertyChange(GLIDER_IMAGE_SOURCE, null, this);
    }

    /**
     * Returns the image source.
     *
     * @return the image source.
     */
    public Object getImageSource()
    {
        return imageSource;
    }

    public double getOpacity()
    {
        return opacity;
    }

    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
        this.firePropertyChange(GLIDER_IMAGE_OPACITY, null, this);
    }

    /**
     * Adds a region of interest to display on the image.
     *
     * @param region the region of interest to add.
     *
     * @throws IllegalArgumentException if <code>region</code> is null.
     */
    public void addRegionOfInterest(GliderRegionOfInterest region)
    {
        if (region == null)
        {
            String message = Logging.getMessage("nullValue.RegionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        region.removePropertyChangeListener(this.regionListener); // prevent duplicate registrations
        region.addPropertyChangeListener(this.regionListener);

        if (this.regionTable.add(region))
            this.firePropertyChange(GLIDER_REGIONS_OF_INTEREST, null, this.getRegionsOfInterest());
    }

    /**
     * Removes a region of interest.
     *
     * @param region the region of interest to remove.
     *
     * @throws IllegalArgumentException if <code>region</code> is null.
     */
    public void removeRegionOfInterest(GliderRegionOfInterest region)
    {
        if (region == null)
        {
            String message = Logging.getMessage("nullValue.RegionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        region.removePropertyChangeListener(this.regionListener);

        if (this.regionTable.remove(region))
            this.firePropertyChange(GLIDER_REGIONS_OF_INTEREST, null, this.getRegionsOfInterest());
    }

    public GliderRegionOfInterest.RegionSet getRegionsOfInterest()
    {
        return new GliderRegionOfInterest.RegionSet(this.regionTable);
    }

    protected class RegionListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            //noinspection StringEquality
            if (evt.getPropertyName() == GliderRegionOfInterest.GLIDER_REGION_OF_INTEREST)
            {
                GliderImage.this.firePropertyChange(GLIDER_REGIONS_OF_INTEREST, null,
                    GliderImage.this.getRegionsOfInterest());
            }
        }
    }

    /**
     * Reprojects an image into an aligned image, one with edges of constant latitude and longitude.
     *
     * @param sourceImage the image to reproject, typically a non-aligned image
     * @param latitudes   an array identifying the latitude of each pixels if the source image. There must be an entry
     *                    in the array for all pixels. The values are taken to be in row-major order relative to the
     *                    image -- the horizontal component varies fastest.
     * @param longitudes  an array identifying the longitude of each pixels if the source image. There must be an entry
     *                    in the array for all pixels. The values are taken to be in row-major order relative to the
     *                    image -- the horizontal component varies fastest.
     *
     * @return a new image containing the original image but reprojected to align to the sector. Pixels in the new image
     *         that have no correspondence with the source image are transparent.
     *
     * @throws InterruptedException if any thread has interrupted the current thread while alignImage is running. The
     *                              <i>interrupted status</i> of the current thread is cleared when this exception is
     *                              thrown.
     */
    public static ImageUtil.AlignedImage alignImage(BufferedImage sourceImage, float[] latitudes, float[] longitudes)
        throws InterruptedException
    {
        return ImageUtil.alignImage(sourceImage, latitudes, longitudes, null, null);
    }

    public static void alignImageDump(BufferedImage sourceImage, float[] latitudes, float[] longitudes)
    {
        ImageUtil.alignImageDump(sourceImage, latitudes, longitudes);
    }

    @SuppressWarnings({"RedundantIfStatement"})
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GliderImage that = (GliderImage) o;

        if (Double.compare(that.altitude, altitude) != 0)
            return false;
        if (corners != null ? !corners.equals(that.corners) : that.corners != null)
            return false;
//        if (imageSource != null ? !imageSource.equals(that.imageSource) : that.imageSource != null)
//            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (sector != null ? !sector.equals(that.sector) : that.sector != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (sector != null ? sector.hashCode() : 0);
        result = 31 * result + (corners != null ? corners.hashCode() : 0);
        temp = altitude != +0.0d ? Double.doubleToLongBits(altitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
//        result = 31 * result + (imageSource != null ? imageSource.hashCode() : 0);
        return result;
    }
}
