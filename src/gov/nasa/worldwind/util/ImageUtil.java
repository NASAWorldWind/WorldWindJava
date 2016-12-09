/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.tiff.GeotiffReader;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.*;
import gov.nasa.worldwind.globes.Earth;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;
import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: ImageUtil.java 1353 2013-05-20 18:43:06Z tgaskins $
 */
public class ImageUtil
{
    public static int NEAREST_NEIGHBOR_INTERPOLATION = 1;
    public static int BILINEAR_INTERPOLATION = 2;
    public static int IMAGE_TILE_SIZE = 1024; // default size to make subimages
    public static Color TRANSPARENT = new Color(0, 0, 0, 0);

    /**
     * Draws the specified <code>image</code> onto the <code>canvas</code>, scaling or stretching the image to fit the
     * canvas. This will apply a bilinear filter to the image if any scaling or stretching is necessary.
     *
     * @param image  the BufferedImage to draw, potentially scaling or stretching to fit the <code>canvas</code>.
     * @param canvas the BufferedImage to receive the scaled or stretched <code>image</code>.
     *
     * @throws IllegalArgumentException if either <code>image</code> or <code>canvas</code> is null.
     */
    public static void getScaledCopy(BufferedImage image, BufferedImage canvas)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.CanvasIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.awt.Graphics2D g2d = canvas.createGraphics();
        try
        {
            g2d.setComposite(java.awt.AlphaComposite.Src);
            g2d.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
        }
        finally
        {
            g2d.dispose();
        }
    }

    /**
     * Rasterizes the image into the canvas, given a transform that maps canvas coordinates to image coordinates.
     *
     * @param image                  the source image.
     * @param canvas                 the image to receive the transformed source image.
     * @param canvasToImageTransform <code>Matrix</code> that maps a canvas coordinates to image coordinates.
     *
     * @throws IllegalArgumentException if any of <code>image</code>, <code>canvas</code>, or <code>canvasToImageTransform</code>
     *                                  are null.
     */
    public static void warpImageWithTransform(BufferedImage image, BufferedImage canvas, Matrix canvasToImageTransform)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.CanvasIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (canvasToImageTransform == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int sourceWidth = image.getWidth();
        int sourceHeight = image.getHeight();
        int destWidth = canvas.getWidth();
        int destHeight = canvas.getHeight();

        for (int dy = 0; dy < destHeight; dy++)
        {
            for (int dx = 0; dx < destWidth; dx++)
            {
                Vec4 vec = new Vec4(dx, dy, 1).transformBy3(canvasToImageTransform);
                if (vec.x >= 0 && vec.y >= 0 && vec.x <= (sourceWidth - 1) && vec.y <= (sourceHeight - 1))
                {
                    int x0 = (int) Math.floor(vec.x);
                    int x1 = (int) Math.ceil(vec.x);
                    double xf = vec.x - x0;

                    int y0 = (int) Math.floor(vec.y);
                    int y1 = (int) Math.ceil(vec.y);
                    double yf = vec.y - y0;

                    int color = interpolateColor(xf, yf,
                        image.getRGB(x0, y0),
                        image.getRGB(x1, y0),
                        image.getRGB(x0, y1),
                        image.getRGB(x1, y1));
                    canvas.setRGB(dx, dy, color);
                }
            }
        }
    }

    /**
     * Convenience method for transforming a georeferenced source image into a geographically aligned destination image.
     * The source image is georeferenced by either three four control points. Each control point maps a location in the
     * source image to a geographic location. This is equivalent to calling {#transformConstrainedImage3} or
     * {#transformConstrainedImage4}, depending on the number of control points.
     *
     * @param sourceImage the source image to transform.
     * @param imagePoints three or four control points in the source image.
     * @param geoPoints   three or four geographic locations corresponding to each source control point.
     * @param destImage   the destination image to receive the transformed source imnage.
     *
     * @return bounding sector for the geographically aligned destination image.
     *
     * @throws IllegalArgumentException if any of <code>sourceImage</code>, <code>destImage</code>,
     *                                  <code>imagePoints</code> or <code>geoPoints</code> is null, or if either
     *                                  <code>imagePoints</code> or <code>geoPoints</code> have length less than 3.
     */
    public static Sector warpImageWithControlPoints(BufferedImage sourceImage, java.awt.geom.Point2D[] imagePoints,
        LatLon[] geoPoints, BufferedImage destImage)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.SourceImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (destImage == null)
        {
            String message = Logging.getMessage("nullValue.DestinationImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        String message = validateControlPoints(3, imagePoints, geoPoints);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (imagePoints.length >= 4 && geoPoints.length >= 4)
        {
            return warpImageWithControlPoints4(sourceImage, imagePoints, geoPoints, destImage);
        }
        else // (imagePoints.length == 3)
        {
            return warpImageWithControlPoints3(sourceImage, imagePoints, geoPoints, destImage);
        }
    }

    /**
     * Transforms a georeferenced source image into a geographically aligned destination image. The source image is
     * georeferenced by four control points. Each control point maps a location in the source image to a geographic
     * location.
     *
     * @param sourceImage the source image to transform.
     * @param imagePoints four control points in the source image.
     * @param geoPoints   four geographic locations corresponding to each source control point.
     * @param destImage   the destination image to receive the transformed source imnage.
     *
     * @return bounding sector for the geographically aligned destination image.
     *
     * @throws IllegalArgumentException if any of <code>sourceImage</code>, <code>destImage</code>,
     *                                  <code>imagePoints</code> or <code>geoPoints</code> is null, or if either
     *                                  <code>imagePoints</code> or <code>geoPoints</code> have length less than 4.
     */
    public static Sector warpImageWithControlPoints4(BufferedImage sourceImage, java.awt.geom.Point2D[] imagePoints,
        LatLon[] geoPoints, BufferedImage destImage)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.SourceImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (destImage == null)
        {
            String message = Logging.getMessage("nullValue.DestinationImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        String message = validateControlPoints(4, imagePoints, geoPoints);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // We can only create an affine transform from three of the given points. To increase accruacy, we will compute
        // the error for each combination of three points, and choose the combination with the least error.

        java.awt.geom.Point2D[] bestFitImagePoints = new java.awt.geom.Point2D[3];
        LatLon[] bestFitGeoPoints = new LatLon[3];
        computeBestFittingControlPoints4(imagePoints, geoPoints, bestFitImagePoints, bestFitGeoPoints);

        return warpImageWithControlPoints3(sourceImage, bestFitImagePoints, bestFitGeoPoints, destImage);
    }

    /**
     * Transforms a georeferenced source image into a geographically aligned destination image. The source image is
     * georeferenced by three control points. Each control point maps a location in the source image to a geographic
     * location.
     *
     * @param sourceImage the source image to transform.
     * @param imagePoints three control points in the source image.
     * @param geoPoints   three geographic locations corresponding to each source control point.
     * @param destImage   the destination image to receive the transformed source imnage.
     *
     * @return bounding sector for the geographically aligned destination image.
     *
     * @throws IllegalArgumentException if any of <code>sourceImage</code>, <code>destImage</code>,
     *                                  <code>imagePoints</code> or <code>geoPoints</code> is null, or if either
     *                                  <code>imagePoints</code> or <code>geoPoints</code> have length less than 3.
     */
    public static Sector warpImageWithControlPoints3(BufferedImage sourceImage, java.awt.geom.Point2D[] imagePoints,
        LatLon[] geoPoints, BufferedImage destImage)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.SourceImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (destImage == null)
        {
            String message = Logging.getMessage("nullValue.DestinationImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        String message = validateControlPoints(3, imagePoints, geoPoints);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Compute the destination sector. We want a lat/lon aligned sector that bounds the source image once it is
        // transformed into a lat/lon aligned image. We compute a matrix that will map source grid coordinates to
        // geographic coordinates. Then we transform the source image's four corners into geographic coordinates.
        // The sector we want is the sector that bounds those four geographic coordinates.

        Matrix gridToGeographic = Matrix.fromImageToGeographic(imagePoints, geoPoints);
        List<LatLon> corners = computeImageCorners(sourceImage.getWidth(), sourceImage.getHeight(), gridToGeographic);
        Sector destSector = Sector.boundingSector(corners);

        if (Sector.isSector(corners) && destSector.isSameSector(corners))
        {
            getScaledCopy(sourceImage, destImage);
        }
        else
        {
            // Compute a matrix that will map from destination grid coordinates to source grid coordinates. By using
            // matrix multiplication in this order, an incoming vector will be transformed by the last matrix multiplied,
            // then the previous, and so on. So an incoming destination coordinate would be transformed into geographic
            // coordinates, then into source coordinates.

            Matrix transform = Matrix.IDENTITY;
            transform = transform.multiply(Matrix.fromGeographicToImage(imagePoints, geoPoints));
            transform = transform.multiply(Matrix.fromImageToGeographic(destImage.getWidth(), destImage.getHeight(),
                destSector));

            warpImageWithTransform(sourceImage, destImage, transform);
        }

        return destSector;
    }

    /**
     * Transforms a georeferenced source image into a geographically aligned destination image. The source image is
     * georeferenced by a world file, which defines an affine transform from source coordinates to geographic
     * coordinates.
     *
     * @param sourceImage     the source image to transform.
     * @param worldFileParams world file parameters which define an affine transform.
     * @param destImage       the destination image to receive the transformed source imnage.
     *
     * @return bounding sector for the geographically aligned destination image.
     *
     * @throws IllegalArgumentException if any of <code>sourceImage</code>, <code>destImage</code> or
     *                                  <code>worldFileParams</code> is null.
     */
    public static Sector warpImageWithWorldFile(BufferedImage sourceImage, AVList worldFileParams,
        BufferedImage destImage)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.SourceImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (worldFileParams == null)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (destImage == null)
        {
            String message = Logging.getMessage("nullValue.DestinationImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix imageToGeographic = Matrix.fromImageToGeographic(worldFileParams);
        if (imageToGeographic == null)
        {
            String message = Logging.getMessage("WorldFile.UnrecognizedValues", "");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<LatLon> corners = computeImageCorners(sourceImage.getWidth(), sourceImage.getHeight(), imageToGeographic);
        Sector destSector = Sector.boundingSector(corners);

        if (Sector.isSector(corners) && destSector.isSameSector(corners))
        {
            getScaledCopy(sourceImage, destImage);
        }
        else
        {
            Matrix transform = Matrix.IDENTITY;
            transform = transform.multiply(Matrix.fromGeographicToImage(worldFileParams));
            transform = transform.multiply(Matrix.fromImageToGeographic(destImage.getWidth(), destImage.getHeight(),
                destSector));

            warpImageWithTransform(sourceImage, destImage, transform);
        }

        return destSector;
    }

    /**
     * Computes which three control points out of four provide the best estimate an image's geographic location. The
     * result is placed in the output parameters <code>outImagePoints</code> and <code>outGeoPoints</code>, both of
     * which must be non-null and at least length 3.
     *
     * @param imagePoints    four control points in the image.
     * @param geoPoints      four geographic locations corresponding to the four <code>imagePoints</code>.
     * @param outImagePoints three control points that best estimate the image's location.
     * @param outGeoPoints   three geographic locations corresponding to the three <code>outImagePoints</code>.
     *
     * @throws IllegalArgumentException if any of <code>imagePoints</code>, <code>geoPoints</code>,
     *                                  <code>outImagePoints</code> or <code>outGeoPoints</code> is null, or if
     *                                  <code>imagePoints</code> or <code>geoPoints</code> have length less than 4, or
     *                                  if <code>outImagePoints</code> or <code>outGeoPoints</code> have length less
     *                                  than 3.
     */
    public static void computeBestFittingControlPoints4(java.awt.geom.Point2D[] imagePoints, LatLon[] geoPoints,
        java.awt.geom.Point2D[] outImagePoints, LatLon[] outGeoPoints)
    {
        String message = validateControlPoints(4, imagePoints, geoPoints);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        message = validateControlPoints(3, outImagePoints, outGeoPoints);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Compute the error for each combination of three points, and choose the combination with the least error.

        java.awt.geom.Point2D[] bestFitImagePoints = null;
        LatLon[] bestFitGeoPoints = null;
        double minError = Double.MAX_VALUE;

        for (int[] indices : new int[][] {
            {0, 1, 2},
            {0, 1, 3},
            {1, 2, 3},
            {0, 2, 3}})
        {
            java.awt.geom.Point2D[] points = new java.awt.geom.Point2D[] {
                imagePoints[indices[0]], imagePoints[indices[1]], imagePoints[indices[2]]};
            LatLon[] locations = new LatLon[] {geoPoints[indices[0]], geoPoints[indices[1]], geoPoints[indices[2]]};
            Matrix m = Matrix.fromImageToGeographic(points, locations);

            double error = 0.0;
            for (int j = 0; j < 4; j++)
            {
                Vec4 vec = new Vec4(imagePoints[j].getX(), imagePoints[j].getY(), 1.0).transformBy3(m);
                LatLon ll = LatLon.fromDegrees(vec.y, vec.x);
                LatLon diff = geoPoints[j].subtract(ll);
                double d = diff.getLatitude().degrees * diff.getLatitude().degrees
                    + diff.getLongitude().degrees * diff.getLongitude().degrees;
                error += d;
            }

            if (error < minError)
            {
                bestFitImagePoints = points;
                bestFitGeoPoints = locations;
                minError = error;
            }
        }

        if (bestFitImagePoints != null)
        {
            System.arraycopy(bestFitImagePoints, 0, outImagePoints, 0, 3);
            System.arraycopy(bestFitGeoPoints, 0, outGeoPoints, 0, 3);
        }
    }

    /**
     * Returns the geographic corners of an image with the specified dimensions, and a transform that maps image
     * coordinates to geographic coordinates.
     *
     * @param imageWidth        width of the image grid.
     * @param imageHeight       height of the image grid.
     * @param imageToGeographic Matrix that maps image coordinates to geographic coordinates.
     *
     * @return List of the image's corner locations in geographic coordinates.
     *
     * @throws IllegalArgumentException if either <code>imageWidth</code> or <code>imageHeight</code> are less than 1,
     *                                  or if <code>imageToGeographic</code> is null.
     */
    public static List<LatLon> computeImageCorners(int imageWidth, int imageHeight, Matrix imageToGeographic)
    {
        if (imageWidth < 1 || imageHeight < 1)
        {
            String message = Logging.getMessage("generic.InvalidImageSize", imageWidth, imageHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (imageToGeographic == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<LatLon> corners = new ArrayList<LatLon>();

        // Lower left corner.
        Vec4 vec = new Vec4(0, imageHeight, 1).transformBy3(imageToGeographic);
        corners.add(LatLon.fromDegrees(vec.y, vec.x));
        // Lower right corner.
        vec = new Vec4(imageWidth, imageHeight, 1).transformBy3(imageToGeographic);
        corners.add(LatLon.fromDegrees(vec.y, vec.x));
        // Upper right corner.
        vec = new Vec4(imageWidth, 0, 1).transformBy3(imageToGeographic);
        corners.add(LatLon.fromDegrees(vec.y, vec.x));
        // Upper left corner.
        vec = new Vec4(0, 0, 1).transformBy3(imageToGeographic);
        corners.add(LatLon.fromDegrees(vec.y, vec.x));

        return corners;
    }

    private static String validateControlPoints(int numExpected,
        java.awt.geom.Point2D[] imagePoints, LatLon[] geoPoints)
    {
        if (imagePoints == null)
        {
            return Logging.getMessage("nullValue.ImagePointsIsNull");
        }
        if (geoPoints == null)
        {
            return Logging.getMessage("nullValue.GeoPointsIsNull");
        }
        if (imagePoints.length < numExpected)
        {
            return Logging.getMessage("generic.ArrayInvalidLength", imagePoints.length);
        }
        if (geoPoints.length < numExpected)
        {
            return Logging.getMessage("generic.ArrayInvalidLength", imagePoints.length);
        }

        return null;
    }

    /**
     * Merge an image into another image. This method is typically used to assemble a composite, seamless image from
     * several individual images. The receiving image, called here the canvas because it's analogous to the Photoshop
     * notion of a canvas, merges the incoming image according to the specified aspect ratio.
     *
     * @param canvasSector the sector defining the canvas' location and range.
     * @param imageSector  the sector defining the image's locaion and range.
     * @param aspectRatio  the aspect ratio, width/height, of the assembled image. If the aspect ratio is greater than
     *                     or equal to one, the assembled image uses the full width of the canvas; the height used is
     *                     proportional to the inverse of the aspect ratio. If the aspect ratio is less than one, the
     *                     full height of the canvas is used; the width used is proportional to the aspect ratio. <p/>
     *                     The aspect ratio is typically used to maintain consistent width and height units while
     *                     assembling multiple images into a canvas of a different aspect ratio than the canvas sector,
     *                     such as drawing a non-square region into a 1024x1024 canvas. An aspect ratio of 1 causes the
     *                     incoming images to be stretched as necessary in one dimension to match the aspect ratio of
     *                     the canvas sector.
     * @param image        the image to merge into the canvas.
     * @param canvas       the canvas into which the images are merged. The canvas is not changed if the specified image
     *                     and canvas sectors are disjoint.
     *
     * @throws IllegalArgumentException if the any of the reference arguments are null or the aspect ratio is less than
     *                                  or equal to zero.
     */
    public static void mergeImage(Sector canvasSector, Sector imageSector, double aspectRatio, BufferedImage image,
        BufferedImage canvas)
    {
        if (canvasSector == null || imageSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (canvas == null || image == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (aspectRatio <= 0)
        {
            String message = Logging.getMessage("Util.AspectRatioInvalid", aspectRatio);
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (!(canvasSector.intersects(imageSector)))
            return;

        // Create an image with the desired aspect ratio within an enclosing canvas of possibly different aspect ratio.
        int subWidth = aspectRatio >= 1 ? canvas.getWidth() : (int) Math.ceil((canvas.getWidth() * aspectRatio));
        int subHeight = aspectRatio >= 1 ? (int) Math.ceil((canvas.getHeight() / aspectRatio)) : canvas.getHeight();

        // yShift shifts image down to change origin from upper-left to lower-left
        double yShift = aspectRatio >= 1d ? (1d - 1d / aspectRatio) * canvas.getHeight() : 0d;

        double sh = ((double) subHeight / (double) image.getHeight())
            * (imageSector.getDeltaLat().divide(canvasSector.getDeltaLat()));
        double sw = ((double) subWidth / (double) image.getWidth())
            * (imageSector.getDeltaLon().divide(canvasSector.getDeltaLon()));

        double dh = subHeight *
            (-imageSector.getMaxLatitude().subtract(canvasSector.getMaxLatitude()).degrees
                / canvasSector.getDeltaLat().degrees);
        double dw = subWidth *
            (imageSector.getMinLongitude().subtract(canvasSector.getMinLongitude()).degrees
                / canvasSector.getDeltaLon().degrees);

        Graphics2D g = canvas.createGraphics();
        g.translate(dw, dh + yShift);
        g.scale(sw, sh);
        g.drawImage(image, 0, 0, null);
    }

    public static Sector positionImage(BufferedImage sourceImage, Point[] imagePoints, LatLon[] geoPoints,
        BufferedImage destImage)
    {
        if (imagePoints.length == 3)
            return positionImage3(sourceImage, imagePoints, geoPoints, destImage);
        else if (imagePoints.length == 4)
            return positionImage4(sourceImage, imagePoints, geoPoints, destImage);
        else
            return null;
    }

    public static Sector positionImage3(BufferedImage sourceImage, Point[] imagePoints, LatLon[] geoPoints,
        BufferedImage destImage)
    {
        // TODO: check args
        BarycentricTriangle sourceLatLon = new BarycentricTriangle(geoPoints[0], geoPoints[1], geoPoints[2]);
        BarycentricTriangle sourcePixels = new BarycentricTriangle(imagePoints[0], imagePoints[1], imagePoints[2]);

        ArrayList<LatLon> extremes = new ArrayList<LatLon>(4);
        // Lower left corner.
        double[] bc = sourcePixels.getBarycentricCoords(new Vec4(0, sourceImage.getHeight(), 0));
        extremes.add(sourceLatLon.getLocation(bc));
        // Lower right corner.
        bc = sourcePixels.getBarycentricCoords(new Vec4(sourceImage.getWidth(), sourceImage.getHeight(), 0));
        extremes.add(sourceLatLon.getLocation(bc));
        // Upper right corner.
        bc = sourcePixels.getBarycentricCoords(new Vec4(sourceImage.getWidth(), 0, 0));
        extremes.add(sourceLatLon.getLocation(bc));
        // Upper left corner.
        bc = sourcePixels.getBarycentricCoords(new Vec4(0, 0, 0));
        extremes.add(sourceLatLon.getLocation(bc));

        Sector sector = Sector.boundingSector(extremes);
        GeoQuad destLatLon = new GeoQuad(sector.asList());

        double width = destImage.getWidth();
        double height = destImage.getHeight();

        for (int row = 0; row < destImage.getHeight(); row++)
        {
            double t = (double) row / height;

            for (int col = 0; col < destImage.getWidth(); col++)
            {
                double s = (double) col / width;
                LatLon latLon = destLatLon.interpolate(1 - t, s);
                double[] baryCoords = sourceLatLon.getBarycentricCoords(latLon);
                Vec4 pixelPostion = sourcePixels.getPoint(baryCoords);
                if (pixelPostion.x < 0 || pixelPostion.x >= sourceImage.getWidth()
                    || pixelPostion.y < 0 || pixelPostion.y >= sourceImage.getHeight())
                    continue;
                int pixel = sourceImage.getRGB((int) pixelPostion.x, (int) pixelPostion.y);
                destImage.setRGB(col, row, pixel);
            }
        }

        return sector;
    }

    public static Sector positionImage4(BufferedImage sourceImage, Point[] imagePoints, LatLon[] geoPoints,
        BufferedImage destImage)
    {
        // TODO: check args
        BarycentricQuadrilateral sourceLatLon = new BarycentricQuadrilateral(geoPoints[0], geoPoints[1], geoPoints[2],
            geoPoints[3]);
        BarycentricQuadrilateral sourcePixels = new BarycentricQuadrilateral(imagePoints[0], imagePoints[1],
            imagePoints[2], imagePoints[3]);

        ArrayList<LatLon> extremes = new ArrayList<LatLon>(4);
        // Lower left corner.
        double[] bc = sourcePixels.getBarycentricCoords(new Vec4(0, sourceImage.getHeight(), 0));
        extremes.add(sourceLatLon.getLocation(bc));
        // Lower right corner.
        bc = sourcePixels.getBarycentricCoords(new Vec4(sourceImage.getWidth(), sourceImage.getHeight(), 0));
        extremes.add(sourceLatLon.getLocation(bc));
        // Upper right corner.
        bc = sourcePixels.getBarycentricCoords(new Vec4(sourceImage.getWidth(), 0, 0));
        extremes.add(sourceLatLon.getLocation(bc));
        // Upper left corner.
        bc = sourcePixels.getBarycentricCoords(new Vec4(0, 0, 0));
        extremes.add(sourceLatLon.getLocation(bc));

        Sector sector = Sector.boundingSector(extremes);
        GeoQuad destLatLon = new GeoQuad(sector.asList());

        double width = destImage.getWidth();
        double height = destImage.getHeight();

        for (int row = 0; row < destImage.getHeight(); row++)
        {
            double t = (double) row / height;

            for (int col = 0; col < destImage.getWidth(); col++)
            {
                double s = (double) col / width;
                LatLon latLon = destLatLon.interpolate(1 - t, s);
                double[] baryCoords = sourceLatLon.getBarycentricCoords(latLon);
                Vec4 pixelPostion = sourcePixels.getPoint(baryCoords);
                if (pixelPostion.x < 0 || pixelPostion.x >= sourceImage.getWidth()
                    || pixelPostion.y < 0 || pixelPostion.y >= sourceImage.getHeight())
                    continue;
                int pixel = sourceImage.getRGB((int) pixelPostion.x, (int) pixelPostion.y);
                destImage.setRGB(col, row, pixel);
            }
        }

        return sector;
    }

    /**
     * Builds a sequence of mipmaps for the specified image. The number of mipmap levels created will be equal to
     * <code>maxLevel + 1</code>, including level 0. The level 0 image will be a reference to the original image, not a
     * copy. Each mipmap level will be created with the specified BufferedImage type <code>mipmapImageType</code>. Each
     * level will have dimensions equal to 1/2 the previous level's dimensions, rounding down, to a minimum width or
     * height of 1.
     *
     * @param image           the BufferedImage to build mipmaps for.
     * @param mipmapImageType the BufferedImage type to use when creating each mipmap image.
     * @param maxLevel        the maximum mip level to create. Specifying zero will return an array containing the
     *                        original image.
     *
     * @return array of mipmap levels, starting at level 0 and stopping at maxLevel. This array will have length
     * maxLevel + 1.
     *
     * @throws IllegalArgumentException if <code>image</code> is null, or if <code>maxLevel</code> is less than zero.
     * @see #getMaxMipmapLevel(int, int)
     */
    public static BufferedImage[] buildMipmaps(BufferedImage image, int mipmapImageType, int maxLevel)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (maxLevel < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "maxLevel < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BufferedImage[] mipMapLevels = new BufferedImage[1 + maxLevel];

        // If the image and mipmap type are equivalent, then just pass the original image along. Otherwise, create a
        // copy of the original image with the appropriate image type.
        if (image.getType() == mipmapImageType)
        {
            mipMapLevels[0] = image;
        }
        else
        {
            mipMapLevels[0] = new BufferedImage(image.getWidth(), image.getHeight(), mipmapImageType);
            getScaledCopy(image, mipMapLevels[0]);
        }

        for (int level = 1; level <= maxLevel; level++)
        {
            int width = Math.max(image.getWidth() >> level, 1);
            int height = Math.max(image.getHeight() >> level, 1);

            mipMapLevels[level] = new BufferedImage(width, height, mipmapImageType);
            getScaledCopy(mipMapLevels[level - 1], mipMapLevels[level]);
        }

        return mipMapLevels;
    }

    /**
     * Builds a sequence of mipmaps for the specified image. This is equivalent to invoking
     * <code>buildMipmaps(BufferedImage, int, int)</code>, with <code>mipmapImageType</code> equal to
     * <code>getMipmapType(image.getType())</code>, and <code>maxLevel</code> equal to
     * <code>getMaxMipmapLevel(image.getWidth(), image.getHeight())</code>.
     *
     * @param image the BufferedImage to build mipmaps for.
     *
     * @return array of mipmap levels.
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     */
    public static BufferedImage[] buildMipmaps(BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int mipmapImageType = getMipmapType(image.getType());
        int maxLevel = getMaxMipmapLevel(image.getWidth(), image.getHeight());

        return buildMipmaps(image, mipmapImageType, maxLevel);
    }

    /**
     * Returns an image type appropriate for generating mipmaps.
     *
     * @param imageType the original BufferedImage type.
     *
     * @return mipmap image type.
     */
    public static int getMipmapType(int imageType)
    {
        // We cannot create a BufferedImage of type "custom", so we fall back to a default image type.
        if (imageType == BufferedImage.TYPE_CUSTOM)
            return BufferedImage.TYPE_INT_ARGB;

        return imageType;
    }

    /**
     * Returns the maximum desired mip level for an image with dimensions <code>width</code> and <code>height</code>.
     * The maximum desired level is the number of levels required to reduce the original image dimensions to a 1x1
     * image.
     *
     * @param width  the level 0 image width.
     * @param height the level 0 image height.
     *
     * @return maximum mip level for the specified <code>width</code> and <code>height</code>.
     *
     * @throws IllegalArgumentException if either <code>width</code> or <code>height</code> are less than 1.
     */
    public static int getMaxMipmapLevel(int width, int height)
    {
        if (width < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (height < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "height < 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int widthLevels = (int) WWMath.logBase2(width);
        int heightLevels = (int) WWMath.logBase2(height);
        return Math.max(widthLevels, heightLevels);
    }

    /**
     * Returns a copy of the specified image such that the new dimensions are powers of two. The new image dimensions
     * will be equal to or greater than the original image. The flag <code>scaleToFit</code> determines whether the
     * original image should be drawn into the new image with no special scaling, or whether the original image should
     * be scaled to fit exactly in the new image. <p/> If the original image dimensions are already powers of two, this
     * method will simply return the original image.
     *
     * @param image      the BufferedImage to convert to a power of two image.
     * @param scaleToFit true if <code>image</code> should be scaled to fit the new image dimensions; false otherwise.s
     *
     * @return copy of <code>image</code> with power of two dimensions.
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     */
    public static BufferedImage convertToPowerOfTwoImage(BufferedImage image, boolean scaleToFit)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If the original image is already a power of two in both dimensions, then simply return it.
        if (WWMath.isPowerOfTwo(image.getWidth()) && WWMath.isPowerOfTwo(image.getHeight()))
        {
            return image;
        }

        int potWidth = WWMath.powerOfTwoCeiling(image.getWidth());
        int potHeight = WWMath.powerOfTwoCeiling(image.getHeight());
        BufferedImage potImage = new BufferedImage(potWidth, potHeight, image.getColorModel().hasAlpha() ?
            BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D g2d = potImage.createGraphics();
        try
        {
            if (scaleToFit)
            {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(image, 0, 0, potImage.getWidth(), potImage.getHeight(), null);
            }
            else
            {
                g2d.drawImage(image, 0, 0, null);
            }
        }
        finally
        {
            g2d.dispose();
        }

        return potImage;
    }

    /**
     * Returns a copy of the specified image with any fully-transparent borders removed. The resultant image is cropped
     * to fit its contents.
     *
     * @param image the BufferedImage to trim.
     *
     * @return copy of <code>image</code> with its transparent borders trimmed
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     */
    public static BufferedImage trimImage(BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int[] rowPixels = new int[width];

        int x1 = Integer.MAX_VALUE;
        int y1 = Integer.MAX_VALUE;
        int x2 = 0;
        int y2 = 0;

        for (int y = 0; y < height; y++)
        {
            image.getRGB(0, y, width, 1, rowPixels, 0, width);

            for (int x = 0; x < width; x++)
            {
                int a = ((rowPixels[x] >> 24) & 0xff);
                if (a <= 0)
                    continue;

                if (x1 > x)
                    x1 = x;
                if (x2 < x)
                    x2 = x;
                if (y1 > y)
                    y1 = y;
                if (y2 < y)
                    y2 = y;
            }
        }

        return (x1 < x2 && y1 < y2) ? image.getSubimage(x1, y1, x2 - x1 + 1, y2 - y1 + 1)
            : new BufferedImage(BufferedImage.TYPE_INT_ARGB, 0, 0);
    }

    /**
     * Returns the size in bytes of the specified image. This takes into account only the image's backing DataBuffers,
     * and not the numerous supporting classes a BufferedImage references.
     *
     * @param image the BufferedImage to compute the size of.
     *
     * @return size of the BufferedImage in bytes.
     *
     * @throws IllegalArgumentException if <code>image</code> is null.
     */
    public static long computeSizeInBytes(BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        long size = 0L;

        java.awt.image.Raster raster = image.getRaster();
        if (raster != null)
        {
            java.awt.image.DataBuffer db = raster.getDataBuffer();
            if (db != null)
            {
                size = computeSizeOfDataBuffer(db);
            }
        }

        return size;
    }

    private static long computeSizeOfDataBuffer(java.awt.image.DataBuffer dataBuffer)
    {
        return dataBuffer.getSize() * computeSizeOfBufferDataType(dataBuffer.getDataType());
    }

    private static long computeSizeOfBufferDataType(int bufferDataType)
    {
        switch (bufferDataType)
        {
            case java.awt.image.DataBuffer.TYPE_BYTE:
                return (Byte.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_DOUBLE:
                return (Double.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_FLOAT:
                return (Float.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_INT:
                return (Integer.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_SHORT:
            case java.awt.image.DataBuffer.TYPE_USHORT:
                return (Short.SIZE / 8);
            case java.awt.image.DataBuffer.TYPE_UNDEFINED:
                break;
        }
        return 0L;
    }

    /**
     * Opens a spatial image.  Reprojects the image if it is in UTM projection.
     *
     * @param imageFile          source image
     * @param interpolation_mode the interpolation mode if the image is reprojected.
     *
     * @return AVList
     *
     * @throws IOException        if there is a problem opening the file.
     * @throws WWRuntimeException if the image type is unsupported.
     */
    public static AVList openSpatialImage(File imageFile, int interpolation_mode) throws IOException
    {
        AVList values = new AVListImpl();
        BufferedImage image;
        Sector sector;

        //Check for Geotiff
        if ((imageFile.getName().toLowerCase().endsWith(".tiff") || (imageFile.getName().toLowerCase().endsWith(
            ".tif"))))
        {
            GeotiffReader reader = new GeotiffReader(imageFile);
            int imageIndex = 0;
            image = reader.read(imageIndex);
            if (reader.isGeotiff(imageIndex))
            {
                return handleGeotiff(image, reader, imageIndex, interpolation_mode);
            }
        }

        //if not geotiff, contine through for other formats
        image = ImageIO.read(imageFile);
        if (image == null)
        {
            String message = Logging.getMessage("generic.ImageReadFailed", imageFile);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        File[] worldFiles = WorldFile.getWorldFiles(imageFile.getAbsoluteFile());
        if (worldFiles == null || worldFiles.length == 0)
        {
            String message = Logging.getMessage("WorldFile.WorldFileNotFound", imageFile.getAbsolutePath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        values.setValue(AVKey.IMAGE, image);
        WorldFile.decodeWorldFiles(worldFiles, values);

        sector = (Sector) values.getValue(AVKey.SECTOR);
        if (sector == null)
            ImageUtil.reprojectUtmToGeographic(values, interpolation_mode);

        sector = (Sector) values.getValue(AVKey.SECTOR);
        if (sector == null)
        {
            String message = "Problem generating bounding sector for the image";
            throw new WWRuntimeException(message);
        }

        values.setValue(AVKey.SECTOR, sector);

        return values;
    }

    /**
     * Opens a spatial image.  Reprojects the image if it is in UTM projection.
     *
     * @param imageFile source image
     *
     * @return AVList
     *
     * @throws IOException if there is a problem opening the file.
     */
    public static AVList openSpatialImage(File imageFile) throws IOException
    {
        return openSpatialImage(imageFile, ImageUtil.NEAREST_NEIGHBOR_INTERPOLATION);
    }

    /**
     * Reads Geo-referenced metadata from Geo-TIFF file
     *
     * @param reader     GeotiffReader
     * @param imageIndex image index (could be a band; GeoTiff file could contain overview images, bands, etc)
     * @param values     AVList
     *
     * @return values            AVList
     *
     * @throws IOException if there is a problem opening the file.
     */
    public static AVList readGeoKeys(GeotiffReader reader, int imageIndex, AVList values) throws IOException
    {
        if (null == values)
            values = new AVListImpl();

        if (null == reader)
            return values;

        return reader.copyMetadataTo(imageIndex, values);
    }

    /**
     * Opens a Geotiff image image.  Reprojects the image if it is in UTM projection.
     *
     * @param image              BufferedImage
     * @param reader             GeotiffReader
     * @param imageIndex         image index (GeoTiff file could contain overview images, bands, etc)
     * @param interpolation_mode the interpolation mode if the image is reprojected.
     *
     * @return AVList
     *
     * @throws IOException if there is a problem opening the file.
     */
    private static AVList handleGeotiff(BufferedImage image, GeotiffReader reader, int imageIndex,
        int interpolation_mode)
        throws IOException
    {

        AVList values = new AVListImpl();
        if (null != image)
        {
            values.setValue(AVKey.IMAGE, image);
            values.setValue(AVKey.WIDTH, image.getWidth());
            values.setValue(AVKey.HEIGHT, image.getHeight());
        }

        ImageUtil.readGeoKeys(reader, imageIndex, values);

        if (AVKey.COORDINATE_SYSTEM_PROJECTED.equals(values.getValue(AVKey.COORDINATE_SYSTEM)))
            ImageUtil.reprojectUtmToGeographic(values, interpolation_mode);

        return values;
    }

    public static Sector calcBoundingBoxForUTM(AVList params) throws IOException
    {
        if (null == params)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(AVKey.WIDTH))
        {
            String message = Logging.getMessage("Geom.WidthInvalid");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(AVKey.HEIGHT))
        {
            String message = Logging.getMessage("Geom.HeightInvalid");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(WorldFile.WORLD_FILE_X_PIXEL_SIZE))
        {
            String message = Logging.getMessage("WorldFile.NoPixelSizeSpecified", "X");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(WorldFile.WORLD_FILE_Y_PIXEL_SIZE))
        {
            String message = Logging.getMessage("WorldFile.NoPixelSizeSpecified", "Y");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(WorldFile.WORLD_FILE_X_LOCATION))
        {
            String message = Logging.getMessage("WorldFile.NoLocationSpecified", "X");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(WorldFile.WORLD_FILE_Y_LOCATION))
        {
            String message = Logging.getMessage("WorldFile.NoLocationSpecified", "Y");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(AVKey.PROJECTION_ZONE))
        {
            String message = Logging.getMessage("generic.ZoneIsMissing");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        if (!params.hasKey(AVKey.PROJECTION_HEMISPHERE))
        {
            String message = Logging.getMessage("generic.HemisphereIsMissing");
            Logging.logger().severe(message);
            throw new IOException(message);
        }

        int width = (Integer) params.getValue(AVKey.WIDTH);
        int height = (Integer) params.getValue(AVKey.HEIGHT);

        double xPixelSize = (Double) params.getValue(WorldFile.WORLD_FILE_X_PIXEL_SIZE);
        double yPixelSize = (Double) params.getValue(WorldFile.WORLD_FILE_Y_PIXEL_SIZE);

        double xLocation = (Double) params.getValue(WorldFile.WORLD_FILE_X_LOCATION);
        double yLocation = (Double) params.getValue(WorldFile.WORLD_FILE_Y_LOCATION);

        Integer zone = (Integer) params.getValue(AVKey.PROJECTION_ZONE);
        String hemisphere = (String) params.getValue(AVKey.PROJECTION_HEMISPHERE);

        UTMCoord upperLeft = UTMCoord.fromUTM(zone, hemisphere, xLocation, yLocation);

        UTMCoord utmUpperLeft = UTMCoord.fromUTM(zone, hemisphere, upperLeft.getEasting() - xPixelSize * .5,
            upperLeft.getNorthing() - yPixelSize * .5);

        UTMCoord utmLowerRight = UTMCoord.fromUTM(zone, hemisphere, utmUpperLeft.getEasting() + (width * xPixelSize),
            utmUpperLeft.getNorthing() + (height * yPixelSize));

        //Get rect Geo bbox
        UTMCoord utmLowerLeft = UTMCoord.fromUTM(zone, upperLeft.getHemisphere(), utmUpperLeft.getEasting(),
            utmLowerRight.getNorthing());
        UTMCoord utmUpperRight = UTMCoord.fromUTM(zone, upperLeft.getHemisphere(), utmLowerRight.getEasting(),
            utmUpperLeft.getNorthing());

        Angle rightExtent = Angle.max(utmUpperRight.getLongitude(), utmLowerRight.getLongitude());
        Angle leftExtent = Angle.min(utmLowerLeft.getLongitude(), utmUpperLeft.getLongitude());
        Angle topExtent = Angle.max(utmUpperRight.getLatitude(), utmUpperLeft.getLatitude());
        Angle bottomExtent = Angle.min(utmLowerRight.getLatitude(), utmLowerLeft.getLatitude());

        Sector sector = new Sector(bottomExtent, topExtent, leftExtent, rightExtent);
        params.setValue(AVKey.SECTOR, sector);
        params.setValue(AVKey.ORIGIN, new LatLon(upperLeft.getLatitude(), upperLeft.getLongitude()));

        return sector;
    }

    /**
     * Reprojects an imge in UTM projection to Geo/WGS84.
     *
     * @param values AVList: contains the bufferedimage and the values from the world file. Stores resulting image in
     *               values
     * @param mode   the interpolation mode if the image is reprojected.
     */
    public static void reprojectUtmToGeographic(AVList values, int mode)
    {
        //TODO pull these const from TMCoord?
        double False_Easting = 500000;
        double False_Northing = 0;
        double Scale = 0.9996;
        Earth earth = new Earth(); //need globe for TM

        if (values == null)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        BufferedImage image = (BufferedImage) values.getValue(AVKey.IMAGE);
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage biOut;
        //Note: image type always BufferedImage.TYPE_INT_ARGB to handle transparent no-data areas after reprojection
        if ((image.getColorModel() != null) && (image.getColorModel() instanceof IndexColorModel))
        {
            biOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB,
                (IndexColorModel) image.getColorModel());
        }
        else
        {
            biOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        double xPixelSize = 0;
        double yPixelSize = 0;

        Object o = values.getValue(WorldFile.WORLD_FILE_X_PIXEL_SIZE);
        if (o != null && o instanceof Double)
            xPixelSize = (Double) o;

        o = values.getValue(WorldFile.WORLD_FILE_Y_PIXEL_SIZE);
        if (o != null && o instanceof Double)
            yPixelSize = (Double) o;

        // TODO: validate that all these values exist and are valid
        double xLocation = (Double) values.getValue(WorldFile.WORLD_FILE_X_LOCATION);
        double yLocation = (Double) values.getValue(WorldFile.WORLD_FILE_Y_LOCATION);
        Integer zone = (Integer) values.getValue(AVKey.PROJECTION_ZONE);
        String hemisphere = (String) values.getValue(AVKey.PROJECTION_HEMISPHERE);

        UTMCoord upperLeft = UTMCoord.fromUTM(zone, hemisphere, xLocation, yLocation);
        UTMCoord utmUpperLeft = UTMCoord.fromUTM(zone, hemisphere, upperLeft.getEasting() - xPixelSize * .5,
            upperLeft.getNorthing() - yPixelSize * .5);

        UTMCoord utmLowerRight = UTMCoord.fromUTM(zone, hemisphere, utmUpperLeft.getEasting() + (width * xPixelSize),
            utmUpperLeft.getNorthing() + (height * yPixelSize));

        //Get rect Geo bbox
        UTMCoord utmLowerLeft = UTMCoord.fromUTM(zone, upperLeft.getHemisphere(), utmUpperLeft.getEasting(),
            utmLowerRight.getNorthing());
        UTMCoord utmUpperRight = UTMCoord.fromUTM(zone, upperLeft.getHemisphere(), utmLowerRight.getEasting(),
            utmUpperLeft.getNorthing());

        Angle rightExtent = Angle.max(utmUpperRight.getLongitude(), utmLowerRight.getLongitude());
        Angle leftExtent = Angle.min(utmLowerLeft.getLongitude(), utmUpperLeft.getLongitude());
        Angle topExtent = Angle.max(utmUpperRight.getLatitude(), utmUpperLeft.getLatitude());
        Angle bottomExtent = Angle.min(utmLowerRight.getLatitude(), utmLowerLeft.getLatitude());
        Sector sector = new Sector(bottomExtent, topExtent, leftExtent, rightExtent);
        values.setValue(AVKey.SECTOR, sector);

        //moving to center of pixel
        double yPixel = (bottomExtent.getDegrees() - topExtent.getDegrees()) / height;
        double xPixel = (rightExtent.getDegrees() - leftExtent.getDegrees()) / width;
        double topExtent2 = sector.getMaxLatitude().getDegrees() + (yPixel * .5);
        double leftExtent2 = sector.getMinLongitude().getDegrees() + (xPixel * .5);

        TMCoord tmUpperLeft = TMCoord.fromLatLon(utmUpperLeft.getLatitude(), utmUpperLeft.getLongitude(),
            earth, null, null, Angle.fromDegrees(0.0), utmUpperLeft.getCentralMeridian(),
            False_Easting, False_Northing, Scale);

        double srcTop = tmUpperLeft.getNorthing() + (yPixelSize * .5);
        double srcLeft = tmUpperLeft.getEasting() + (xPixelSize * .5);

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                double yTarget = topExtent2 + y * yPixel;
                double xTarget = leftExtent2 + x * xPixel;
                TMCoord TM = TMCoord.fromLatLon(Angle.fromDegreesLatitude(yTarget), Angle.fromDegreesLongitude(xTarget),
                    earth, null, null, Angle.fromDegrees(0.0), utmUpperLeft.getCentralMeridian(),
                    False_Easting, False_Northing, Scale);

                double distFromCornerX = TM.getEasting() - srcLeft;
                double distFromCornerY = srcTop - TM.getNorthing();
                long rx = Math.round(distFromCornerX / Math.abs(xPixelSize));
                long ry = Math.round(distFromCornerY / Math.abs(yPixelSize));

                if (mode == ImageUtil.BILINEAR_INTERPOLATION)
                {
                    double rxD = distFromCornerX / Math.abs(xPixelSize);
                    double ryD = distFromCornerY / Math.abs(yPixelSize);
                    int iX = (int) Math.floor(rxD);
                    int iY = (int) Math.floor(ryD);
                    double dx = rxD - iX;
                    double dy = ryD - iY;
                    if ((iX > 0) && (iY > 0))
                        if ((iX < width - 1) && (iY < height - 1))
                        {
                            //get four pixels from image
                            int a = image.getRGB(iX, iY);
                            int b = image.getRGB(iX + 1, iY);
                            int c = image.getRGB(iX, iY + 1);
                            int d = image.getRGB(iX + 1, iY + 1);
                            int sum = interpolateColor(dx, dy, a, b, c, d);

                            biOut.setRGB(x, y, Math.round(sum));
                        }
                        else
                            biOut.setRGB(x, y, 0);
                }
                else  //NEAREST_NEIGHBOR is default
                {
                    if ((rx > 0) && (ry > 0))
                        if ((rx < width) && (ry < height))
                            biOut.setRGB(x, y, image.getRGB(Long.valueOf(rx).intValue(), Long.valueOf(ry).intValue()));
                        else
                            biOut.setRGB(x, y, 0);
                }
            }
        }

        values.setValue(AVKey.IMAGE, biOut);
    }

    /**
     * Performs bilinear interpolation of 32-bit colors over a convex quadrilateral.
     *
     * @param x  horizontal coordinate of the interpolation point relative to the lower left corner of the
     *           quadrilateral. The value should generally be in the range [0, 1].
     * @param y  vertical coordinate of the interpolation point relative to the lower left corner of the quadrilateral.
     *           The value should generally be in the range [0, 1].
     * @param c0 color at the lower left corner of the quadrilateral.
     * @param c1 color at the lower right corner of the quadrilateral.
     * @param c2 color at the pixel upper left corner of the quadrilateral.
     * @param c3 color at the pixel upper right corner of the quadrilateral.
     *
     * @return int the interpolated color.
     */
    public static int interpolateColor(double x, double y, int c0, int c1, int c2, int c3)
    {
        //pull out alpha, red, green, blue values for each pixel
        int a0 = (c0 >> 24) & 0xff;
        int r0 = (c0 >> 16) & 0xff;
        int g0 = (c0 >> 8) & 0xff;
        int b0 = c0 & 0xff;

        int a1 = (c1 >> 24) & 0xff;
        int r1 = (c1 >> 16) & 0xff;
        int g1 = (c1 >> 8) & 0xff;
        int b1 = c1 & 0xff;

        int a2 = (c2 >> 24) & 0xff;
        int r2 = (c2 >> 16) & 0xff;
        int g2 = (c2 >> 8) & 0xff;
        int b2 = c2 & 0xff;

        int a3 = (c3 >> 24) & 0xff;
        int r3 = (c3 >> 16) & 0xff;
        int g3 = (c3 >> 8) & 0xff;
        int b3 = c3 & 0xff;

        double rx = 1.0d - x;
        double ry = 1.0d - y;

        double x0 = rx * a0 + x * a1;
        double x1 = rx * a2 + x * a3;
        int a = (int) (ry * x0 + y * x1);  //final alpha value
        a = a << 24;

        x0 = rx * r0 + x * r1;
        x1 = rx * r2 + x * r3;
        int r = (int) (ry * x0 + y * x1); //final red value
        r = r << 16;

        x0 = rx * g0 + x * g1;
        x1 = rx * g2 + x * g3;
        int g = (int) (ry * x0 + y * x1); //final green value
        g = g << 8;

        x0 = rx * b0 + x * b1;
        x1 = rx * b2 + x * b3;
        int b = (int) (ry * x0 + y * x1); //final blue value

        return (a | r | g | b);
    }

    public static class AlignedImage
    {
        public final Sector sector;
        public final BufferedImage image;

        public AlignedImage(BufferedImage image, Sector sector)
        {
            this.image = image;
            this.sector = sector;
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
     * @return a new image containing the original image but reprojected to align to the bounding sector. Pixels in the
     * new image that have no correspondence with the source image are transparent.
     *
     * @throws InterruptedException if any thread has interrupted the current thread while alignImage is running. The
     *                              <i>interrupted status</i> of the current thread is cleared when this exception is
     *                              thrown.
     */
    public static AlignedImage alignImage(BufferedImage sourceImage, float[] latitudes, float[] longitudes)
        throws InterruptedException
    {
        return alignImage(sourceImage, latitudes, longitudes, null, null);
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
     * @param sector      the sector to align the image to. If null, this computes the aligned image's sector.
     * @param dimension   the the aligned image's dimensions. If null, this computes the aligned image's dimension.
     *
     * @return a new image containing the original image but reprojected to align to the sector. Pixels in the new image
     * that have no correspondence with the source image are transparent.
     *
     * @throws InterruptedException if any thread has interrupted the current thread while alignImage is running. The
     *                              <i>interrupted status</i> of the current thread is cleared when this exception is
     *                              thrown.
     */
    public static AlignedImage alignImage(BufferedImage sourceImage, float[] latitudes, float[] longitudes,
        Sector sector, Dimension dimension) throws InterruptedException
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (latitudes == null || longitudes == null || latitudes.length != longitudes.length)
        {
            String message = Logging.getMessage("ImageUtil.FieldArrayInvalid");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();

        if (sourceWidth < 1 || sourceHeight < 1)
        {
            String message = Logging.getMessage("ImageUtil.EmptyImage");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (longitudes.length < sourceWidth * sourceHeight || latitudes.length < sourceWidth * sourceHeight)
        {
            String message = Logging.getMessage("ImageUtil.FieldArrayTooShort");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        GeographicImageInterpolator grid = new GeographicImageInterpolator(new Dimension(sourceWidth, sourceHeight),
            longitudes, latitudes, 10, 1);

        // If the caller did not specify a Sector, then use the image's bounding sector as computed by
        // GeographicImageInterpolator. We let GeographicImageInterpolator perform the computation because it computes
        // the correct sector for images which cross the international dateline.
        if (sector == null)
        {
            sector = grid.getSector();
        }

        // If the caller did not specify a dimension for the aligned image, then compute its dimensions as follows:
        //
        // 1. Assign the output width or height to the source image's maximum dimension, depending on the output
        //    sector's largest delta (latitude or longitude).
        // 2. Compute the remaining output dimension so that it has the same geographic resolution as the dimension
        //    from #1. This computed dimension is always less than or equal to the dimension from #1.
        //
        // This has the effect of allocating resolution where the aligned image needs it most, and gives the aligned
        // image square pixels in geographic coordinates. Without square pixels the aligned image's resolution can be
        // extremely anisotriopic, causing severe aliasing in one dimension.
        if (dimension == null)
        {
            double maxDimension = Math.max(sourceWidth, sourceHeight);
            double maxSectorDelta = Math.max(sector.getDeltaLonDegrees(), sector.getDeltaLatDegrees());
            double pixelsPerDegree = maxDimension / maxSectorDelta;
            dimension = new Dimension(
                (int) Math.round(pixelsPerDegree * sector.getDeltaLonDegrees()),
                (int) Math.round(pixelsPerDegree * sector.getDeltaLatDegrees()));
        }

        // Get a buffer containing the source image's pixels.
        int[] sourceColors = sourceImage.getRGB(0, 0, sourceWidth, sourceHeight, null, 0, sourceWidth);
        // Create a buffer for the aligned image pixels, initially filled with transparent values.
        int[] destColors = new int[dimension.width * dimension.height];

        // Compute the geographic dimensions of the aligned image's pixels. We divide by the dimension instead of
        // dimension-1 because we treat the aligned image pixels as having area.
        double dLon = sector.getDeltaLonDegrees() / dimension.width;
        double dLat = sector.getDeltaLatDegrees() / dimension.height;

        // Compute each aligned image pixel's color by mapping its location into the source image. This treats the
        // aligned image pixel's as having area, and the location of each pixel's at its center. This loop begins in the
        // center of the upper left hand pixel and continues in row major order across the image, stepping by a pixels
        // geographic size.
        for (int j = 0; j < dimension.height; j++)
        {
            // Generate an InterruptedException if the current thread is interrupted. Responding to thread interruptions
            // before processing each image row ensures that this method terminates in a reasonable amount of time after
            // the currently executing thread is interrupted, but without consuming unecessary CPU time. Using either
            // Thread.sleep(1), or executing Thread.sleep() for each pixel would unecessarily increase the this methods
            // total CPU time.
            Thread.sleep(0);

            float lat = (float) (sector.getMaxLatitude().degrees - j * dLat - dLon / 2d);

            for (int i = 0; i < dimension.width; i++)
            {
                float lon = (float) (sector.getMinLongitude().degrees + i * dLon + dLat / 2d);

                // Search for a cell in the source image which contains this aligned image pixel's location.
                ImageInterpolator.ContainingCell cell = grid.findContainingCell(lon, lat);

                // If there's a source cell for this location, then write a color to the destination image by linearly
                // interpolating between the four pixels at the cell's corners. Otherwise, don't change the destination
                // image. This ensures pixels which don't correspond to the source image remain transparent.
                if (cell != null)
                {
                    int color = interpolateColor(cell.uv[0], cell.uv[1],
                        sourceColors[cell.fieldIndices[0]],
                        sourceColors[cell.fieldIndices[1]],
                        sourceColors[cell.fieldIndices[3]],
                        sourceColors[cell.fieldIndices[2]]
                    );

                    destColors[j * dimension.width + i] = color;
                }
            }
        }

        // Release memory used by source colors and the grid
        //noinspection UnusedAssignment
        sourceColors = null;
        //noinspection UnusedAssignment
        grid = null;

        BufferedImage destImage = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_4BYTE_ABGR);
        destImage.setRGB(0, 0, dimension.width, dimension.height, destColors, 0, dimension.width);

        return new AlignedImage(destImage, sector);
    }

    public static void alignImageDump(BufferedImage sourceImage, float[] latitudes, float[] longitudes)
    {
        try
        {
            JFileChooser fileChooser = new JFileChooser();
            int status = fileChooser.showSaveDialog(null);
            if (status != JFileChooser.APPROVE_OPTION)
                return;

            File imageFile = fileChooser.getSelectedFile();
            if (!imageFile.getName().endsWith(".png"))
                imageFile = new File(imageFile.getPath() + ".png");

            ImageIO.write(sourceImage, "png", imageFile);

            File latsFile = new File(WWIO.replaceSuffix(imageFile.getPath(), ".lats.dat"));
            File lonsFile = new File(WWIO.replaceSuffix(imageFile.getPath(), ".lons.dat"));

            DataOutputStream latsOut = new DataOutputStream(new FileOutputStream(latsFile));
            DataOutputStream lonsOut = new DataOutputStream(new FileOutputStream(lonsFile));
            for (int i = 0; i < latitudes.length; i++)
            {
                latsOut.writeFloat(latitudes[i]);
                lonsOut.writeFloat(longitudes[i]);
            }
            latsOut.flush();
            lonsOut.flush();
            latsOut.close();
            lonsOut.close();
            System.out.println("FILES SAVED");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static final int MAX_IMAGE_SIZE_TO_CONVERT = 4096;

    public static BufferedImage toCompatibleImage(BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (java.awt.GraphicsEnvironment.isHeadless())
            return image;

        // If the image is not already compatible, and is within the restrictions on dimension, then convert it
        // to a compatible image type.
        if (!isCompatibleImage(image)
            && (image.getWidth() <= MAX_IMAGE_SIZE_TO_CONVERT)
            && (image.getHeight() <= MAX_IMAGE_SIZE_TO_CONVERT))
        {
            java.awt.image.BufferedImage compatibleImage =
                createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
            java.awt.Graphics2D g2d = compatibleImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return compatibleImage;
        }
        // Otherwise return the original image.
        else
        {
            return image;
        }
    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency)
    {
        if (width < 1)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 1)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

//        if (java.awt.GraphicsEnvironment.isHeadless())
        {
            return new BufferedImage(width, height,
                (transparency == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB
            );
        }
//
//        java.awt.GraphicsConfiguration gc = getDefaultGraphicsConfiguration();
//        return gc.createCompatibleImage(width, height, transparency);
    }

    protected static boolean isCompatibleImage(BufferedImage image)
    {
        if (java.awt.GraphicsEnvironment.isHeadless())
            return false;

        java.awt.GraphicsConfiguration gc = getDefaultGraphicsConfiguration();
        java.awt.image.ColorModel gcColorModel = gc.getColorModel(image.getTransparency());
        return image.getColorModel().equals(gcColorModel);
    }

    protected static java.awt.GraphicsConfiguration getDefaultGraphicsConfiguration()
    {
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    public static BufferedImage mapTransparencyColors(ByteBuffer imageBuffer, int originalColors[])
    {
        try
        {
            InputStream inputStream = WWIO.getInputStreamFromByteBuffer(imageBuffer);
            BufferedImage image = ImageIO.read(inputStream);
            return mapTransparencyColors(image, originalColors);
        }
        catch (IOException e)
        {
            Logging.logger().finest(e.getMessage());
            return null;
        }
    }

    public static BufferedImage mapTransparencyColors(BufferedImage sourceImage, int[] originalColors)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (originalColors == null)
        {
            String message = Logging.getMessage("nullValue.ColorArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        if (width < 1 || height < 1)
        {
            String message = Logging.getMessage("ImageUtil.EmptyImage");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        int[] sourceColors = sourceImage.getRGB(0, 0, width, height, null, 0, width);
        int[] destColors = Arrays.copyOf(sourceColors, sourceColors.length);

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int index = j * width + i;
                for (int c : originalColors)
                {
                    if (sourceColors[index] == c)
                    {
                        destColors[index] = 0;
                        break;
                    }
                }
            }
        }

        // Release memory used by source colors prior to creating the new image
        //noinspection UnusedAssignment
        sourceColors = null;

        BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        destImage.setRGB(0, 0, width, height, destColors, 0, width);

        return destImage;
    }

    public static BufferedImage mapColors(ByteBuffer imageBuffer, int originalColor, int newColor)
    {
        try
        {
            InputStream inputStream = WWIO.getInputStreamFromByteBuffer(imageBuffer);
            BufferedImage image = ImageIO.read(inputStream);
            return mapColors(image, new int[] {originalColor}, new int[] {newColor});
        }
        catch (IOException e)
        {
            Logging.logger().finest(e.getMessage());
            return null;
        }
    }

    public static BufferedImage mapColors(BufferedImage sourceImage, int[] originalColors, int[] newColors)
    {
        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (originalColors == null || newColors == null)
        {
            String message = Logging.getMessage("nullValue.ColorArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        if (width < 1 || height < 1)
        {
            String message = Logging.getMessage("ImageUtil.EmptyImage");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        int[] sourceColors = sourceImage.getRGB(0, 0, width, height, null, 0, width);
        int[] destColors = Arrays.copyOf(sourceColors, sourceColors.length);

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int index = j * width + i;
                for (int c : originalColors)
                {
                    if (sourceColors[index] == originalColors[c])
                        destColors[index] = newColors[c];
                }
            }
        }

        // Release memory used by source colors prior to creating the new image
        //noinspection UnusedAssignment
        sourceColors = null;

        BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        destImage.setRGB(0, 0, width, height, destColors, 0, width);

        return destImage;
    }

    public static ByteBuffer asJPEG(DataRaster raster)
    {
        ByteBuffer buffer = null;

        if (null == raster)
        {
            String msg = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        BufferedImage image;

        if (raster instanceof BufferedImageRaster)
        {
            image = ((BufferedImageRaster) raster).getBufferedImage();
        }
        else if (raster instanceof BufferWrapperRaster)
        {
            image = ImageUtil.visualize((BufferWrapperRaster) raster);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnexpectedRasterType", raster.getClass().getName());
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        ImageOutputStream ios = null;

        if (null == image)
        {
            String msg = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        try
        {
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            ios = new MemoryCacheImageOutputStream(imageBytes);

            ColorModel cm = image.getColorModel();
            if (cm instanceof ComponentColorModel)
            {
                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                if (null != writer)
                {
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    param.setSourceBands(new int[] {0, 1, 2});
                    cm = new DirectColorModel(24, /*Red*/0x00ff0000, /*Green*/0x0000ff00, /*Blue*/ 0x000000ff,
                        /*Alpha*/0x0);
                    param.setDestinationType(new ImageTypeSpecifier(cm, cm.createCompatibleSampleModel(1, 1)));

                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(image, null, null), param);
                    writer.dispose();
                }
            }
            else
                ImageIO.write(image, "jpeg", ios);

            buffer = ByteBuffer.wrap(imageBytes.toByteArray());
        }
        catch (Throwable t)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
        }
        finally
        {
            close(ios);
        }

        return buffer;
    }

    public static ByteBuffer asPNG(DataRaster raster)
    {
        ByteBuffer buffer = null;

        if (null == raster)
        {
            String msg = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        BufferedImage image;

        if (raster instanceof BufferedImageRaster)
        {
            image = ((BufferedImageRaster) raster).getBufferedImage();
        }
        else if (raster instanceof BufferWrapperRaster)
        {
            image = ImageUtil.visualize((BufferWrapperRaster) raster);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnexpectedRasterType", raster.getClass().getName());
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        ImageOutputStream ios = null;

        if (null == image)
        {
            String msg = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg);
        }

        try
        {
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            ios = new MemoryCacheImageOutputStream(imageBytes);

            ImageIO.write(image, "png", ios);

            buffer = ByteBuffer.wrap(imageBytes.toByteArray());
        }
        catch (Throwable t)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
        }
        finally
        {
            close(ios);
        }

        return buffer;
    }

    protected static void close(ImageOutputStream ios)
    {
        if (null != ios)
        {
            try
            {
                ios.close();
            }
            catch (Throwable t)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
            }
        }
    }

    /**
     * Converts a non-imagery data raster (elevations) to visually representable image raster.
     * Calculates min and max values, and normalizes pixel value from 0 - 65,535 and creates
     * a GRAY color image raster with pixel data type of unsigned short.
     *
     * @param raster non-imagery data raster (elevations) instance of data raster derived from BufferWrapperRaster
     *
     * @return BufferedImage visual representation of the non-imagery data raster
     */
    public static BufferedImage visualize(BufferWrapperRaster raster)
    {
        if (null == raster)
        {
            String message = Logging.getMessage("nullValue.RasterIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        // we are building UINT DataBuffer, cannot use negative values as -32768 or -9999, so we will use 0
        double missingDataSignal = AVListImpl.getDoubleValue(raster, AVKey.MISSING_DATA_SIGNAL,
            (double) Short.MIN_VALUE);
        int missingDataReplacement = 0;

        Double minElevation = (Double) raster.getValue(AVKey.ELEVATION_MIN);
        Double maxElevation = (Double) raster.getValue(AVKey.ELEVATION_MAX);

        double min = (null != minElevation && minElevation >= Earth.ELEVATION_MIN) ? minElevation : 0d;
        double max = (null != maxElevation && minElevation <= Earth.ELEVATION_MAX) ? maxElevation : Earth.ELEVATION_MAX;

        int width = raster.getWidth();
        int height = raster.getHeight();
        int size = width * height;

        short[][] data = new short[][]
            {
                new short[size], // intensity band
                new short[size]  // alpha (transparency band)
            };

        final int BAND_Y = 0, BAND_ALPHA = 1;

        final int ALPHA_OPAQUE = (short) 0xFFFF;
        final int ALPHA_TRANSLUCENT = (short) 0;

        int i = 0;
        boolean hasVoids = false;
        double norm = (max != min) ? Math.abs(65534d / (max - min)) : 0d;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                double v = raster.getDoubleAtPosition(y, x);
                // set pixel and alpha as zero (transparent) for pixel which is:
                // - equals to missingDataSignal
                // - is zero
                // - greater than max elevation or smaller than min elevation
                if (v == missingDataSignal || v == 0 || v < min || v > max)
                {
                    data[BAND_Y][i] = (short) (0xFFFF & missingDataReplacement);
                    data[BAND_ALPHA][i] = ALPHA_TRANSLUCENT;
                    hasVoids = true;
                }
                else
                {
                    data[BAND_Y][i] = (short) (0xFFFF & (int) ((v - min) * norm));
                    data[BAND_ALPHA][i] = ALPHA_OPAQUE;
                }
                i++;
            }
        }

        int[] bandOrder = (hasVoids) ? new int[] {BAND_Y, BAND_ALPHA} : new int[] {BAND_Y};
        int[] offsets = (hasVoids) ? new int[] {0, 0} : new int[] {0};
        int[] nBits = (hasVoids) ? new int[] {16, 8} : new int[] {16};

        DataBuffer imgBuffer = new DataBufferUShort(data, size);

        SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_USHORT, width, height, width, bandOrder, offsets);

        WritableRaster wr = Raster.createWritableRaster(sm, imgBuffer, null);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);

        ColorModel cm = new ComponentColorModel(cs, nBits, hasVoids, false,
            hasVoids ? Transparency.TRANSLUCENT : Transparency.OPAQUE,
            DataBuffer.TYPE_USHORT);

        return new BufferedImage(cm, wr, false, null);
    }

//
//    public static void main(String[] args)
//    {
//        // Test alignImage(...)
//        try
//        {
//            BufferedImage sourceImage = ImageIO.read(
//                new File("src/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg"));
//
//            int width = sourceImage.getWidth();
//            int height = sourceImage.getHeight();
//
//            float[] xs = new float[width * height];
//            float[] ys = new float[xs.length];
//
//            double dx = 360d / (width - 1);
//            double dy = 180d / (height - 1);
//
//            for (int j = 0; j < height; j++)
//            {
//                for (int i = 0; i < width; i++)
//                {
//                    xs[j * width + i] = -180 + i * (float) dx;
//                    ys[j * width + i] = -90 + j * (float) dy;
//                }
//            }
//            xs[xs.length - 1] = 180;
//            ys[ys.length - 1] = 90;
//
//            long start = System.currentTimeMillis();
//            AlignedImage destImage = alignImage(sourceImage, ys, xs, Sector.fromDegrees(-90, 90, -180, 180));
//
//            System.out.println(System.currentTimeMillis() - start);
////            int[] src = sourceImage.getRGB(0, 0, width, height, null, 0, width);
////            int[] dest = destImage.image.getRGB(0, 0, width, height, null, 0, width);
////
////            ColorModel cm = destImage.getColorModel();
////            double count = 0;
////            for (int i = 0; i < src.length; i++)
////            {
////                if (src[i] != dest[i])
////                {
////                    ++count;
////                    int s = src[i];
////                    int d = dest[i];
////                    int sr = cm.getRed(s);
////                    int sg = cm.getGreen(s);
////                    int sb = cm.getBlue(s);
////                    int sa = cm.getAlpha(s);
////                    int dr = cm.getRed(d);
////                    int dg = cm.getGreen(d);
////                    int db = cm.getBlue(d);
////                    int da = cm.getAlpha(d);
////
////                    if (Math.abs(dr - sr) > 1 || Math.abs(dg - sg) > 1 || Math.abs(db - sb) > 1
////                        || Math.abs(da - sa) > 1)
////                        System.out.printf("%d: (%d, %d, %d, %d) : (%d, %d, %d, %d) delta: (%d, %d, %d, %d) (%f, %f)\n",
////                            i,
////                            sr, sg, sb, sa, dr, dg, db, da,
////                            dr - sr, dg - sg, db - sb, da - sa,
////                            xs[i], ys[i]);
////                }
////            }
////
////            System.out.println(count + " of " + src.length + " (" + 100d * count / src.length + "%)");
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
