/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.worldfile;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.util.*;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.*;

/**
 * Static methods to handle world files typically distributed with georeferenced rasters.
 *
 * @author tag
 * @version $Id: WorldFile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class WorldFile
{
    public final static String WORLD_FILE_X_PIXEL_SIZE = "gov.nasa.worldwind.worldFile.XPixelSize";
    public final static String WORLD_FILE_Y_PIXEL_SIZE = "gov.nasa.worldwind.worldFile.YPixelSize";
    public final static String WORLD_FILE_X_COEFFICIENT = "gov.nasa.worldwind.worldFile.XCoefficient";
    public final static String WORLD_FILE_Y_COEFFICIENT = "gov.nasa.worldwind.worldFile.YCoefficient";
    public final static String WORLD_FILE_X_LOCATION = "gov.nasa.worldwind.worldFile.XLocation";
    public final static String WORLD_FILE_Y_LOCATION = "gov.nasa.worldwind.worldFile.YLocation";
    public final static String WORLD_FILE_IMAGE_SIZE = "gov.nasa.worldwind.worldFile.ImageSize";

    /**
     * Retrieve the metadata files corresponding to a specified image or elevation file.
     *
     * @param dataFile the image or elevation file for which to retrieve the metadata files.
     *
     * @return the metadata files that exist in the same directory as the data file, otherwise null.
     *
     * @throws IllegalArgumentException if the data file is null.
     * @throws FileNotFoundException    if the data file does not exist.
     */
    public static File[] getWorldFiles(File dataFile) throws FileNotFoundException
    {
        if (dataFile == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!dataFile.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", dataFile.getPath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        File dir = dataFile.getParentFile();
        final String imageSuffix = WWIO.getSuffix(dataFile.getPath());
        final String base = WWIO.replaceSuffix(dataFile.getName(), "").trim();

        return dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                int length = base.length() + 4;
                name = name.trim();
                if (!name.startsWith(base) || name.length() != length)
                    return false;

                if (name.toLowerCase().endsWith("w"))
                {
                    // Match world file to the corresponding image file: certain chars of suffixes must match
                    String nameSuffix = WWIO.getSuffix(name);
                    if (imageSuffix != null && nameSuffix != null)
                    {
                        if (nameSuffix.substring(0, 1).equalsIgnoreCase(imageSuffix.substring(0, 1))
                            && imageSuffix.toLowerCase().endsWith(nameSuffix.substring(1, 2)))
                            return true;
                    }
                }

                return name.toLowerCase().endsWith(".hdr") || name.toLowerCase().endsWith(".prj");
            }
        });
    }

    /**
     * Retrieves the useful values from a collection of world files.
     *
     * @param worldFiles the world files.
     * @param values     may contain a buffered image, needed to retrieve image size
     *
     * @return an attribute-value list containing the values from the world files.
     *
     * @throws IllegalArgumentException if the file is null.
     * @throws FileNotFoundException    if the file does not exist.
     * @throws IllegalStateException    if the file cannot be parsed as a world file.
     */
    public static AVList decodeWorldFiles(File worldFiles[], AVList values) throws FileNotFoundException
    {
        if (worldFiles == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (values == null)
            values = new AVListImpl();

        for (File file : worldFiles)
        {
            if (!file.exists())
            {
                String message = Logging.getMessage("generic.FileNotFound", file.getPath());
                Logging.logger().severe(message);
                throw new FileNotFoundException(message);
            }
        }

        //File transformFile = null;
        for (File file : worldFiles)
        {
            if (file.getName().toLowerCase().endsWith("w"))
            {
                scanWorldFile(file, values);
                //transformFile = file;
            }
            else if (file.getName().toLowerCase().endsWith(".hdr"))
            {
                scanHdrFile(file, values);
            }
            else if (file.getName().toLowerCase().endsWith(".prj"))
            {
                String text = WWIO.readTextFile(file);
                decodeOGCCoordinateSystemWKT(text, values);
            }
        }

        int[] size;
        Object o = values.getValue(WORLD_FILE_IMAGE_SIZE);
        if (o != null && (o instanceof int[]))
        {
            size = (int[]) o;
        }
        else
        {
            size = WorldFile.parseSize(values);
            if (size != null)
                values.setValue(WORLD_FILE_IMAGE_SIZE, size);
        }

        o = WorldFile.parseByteOrder(values);
        if (o != null)
            values.setValue(AVKey.BYTE_ORDER, o);

        o = WorldFile.parsePixelFormat(values);
        if (o != null)
            values.setValue(AVKey.PIXEL_FORMAT, o);

        o = WorldFile.parseDataType(values);
        if (o != null)
            values.setValue(AVKey.DATA_TYPE, o);

        // Consumers of this property are expecting the string "gov.nasa.worldwind.avkey.MissingDataValue", which now 
        // corresponds to the key MISSING_DATA_REPLACEMENT.
        o = WorldFile.parseMissingDataValue(values);
        if (o != null)
            values.setValue(AVKey.MISSING_DATA_REPLACEMENT, o);

        Sector sector = null;
        if (WorldFile.worldFileValuesAppearGeographic(values))
        {
            if (size != null)
            {
                sector = WorldFile.parseDegrees(values, size[0], size[1]);
            }
            else
            {
                BufferedImage image = (BufferedImage) values.getValue(AVKey.IMAGE);
                if (image != null)
                {
                    sector = WorldFile.parseDegrees(values, image.getWidth(), image.getHeight());
                }
            }

            if (sector != null)
            {
                values.setValue(AVKey.SECTOR, sector);
            }
        }

        if (null == sector)
        {
            sector = WorldFile.extractSectorFromHeader(values); // TODO: not checking for non-geographic proj
            if (sector != null)
                values.setValue(AVKey.SECTOR, sector);
        }

        return values;
    }

    protected static void scanWorldFile(File file, AVList values) throws FileNotFoundException
    {
        Scanner scanner = new Scanner(file);
        scanner.useLocale(Locale.US);

        try
        {
            for (int i = 0; i < 6; i++)
            {
                if (scanner.hasNextDouble())
                {
                    switch (i)
                    {
                        case 0:
                            values.setValue(WORLD_FILE_X_PIXEL_SIZE, scanner.nextDouble());
                            break;
                        case 1:
                            values.setValue(WORLD_FILE_Y_COEFFICIENT, scanner.nextDouble());
                            break;
                        case 2:
                            values.setValue(WORLD_FILE_X_COEFFICIENT, scanner.nextDouble());
                            break;
                        case 3:
                            values.setValue(WORLD_FILE_Y_PIXEL_SIZE, scanner.nextDouble());
                            break;
                        case 4:
                            values.setValue(WORLD_FILE_X_LOCATION, scanner.nextDouble());
                            break;
                        case 5:
                            values.setValue(WORLD_FILE_Y_LOCATION, scanner.nextDouble());
                            break;
                    }
                }
                else
                {
                    String message = Logging.getMessage("SurfaceImage.WorldFileLineMissing", i + 1);
                    Logging.logger().severe(message);
                    throw new IllegalStateException(message);
                }
            }
        }
        finally
        {
            if (null != scanner)
                scanner.close();
        }
    }

    protected static void scanHdrFile(File file, AVList values) throws FileNotFoundException
    {
        Scanner scanner = new Scanner(file);
        scanner.useLocale(Locale.US);

        try
        {
            while (scanner.hasNext())
            {
                String key = scanner.next().toUpperCase();
                if (!scanner.hasNext())
                    return; // Error. Log it.

                if (key.equalsIgnoreCase("NROWS"))
                    values.setValue(key, scanner.nextInt());
                else if (key.equalsIgnoreCase("NCOLS"))
                    values.setValue(key, scanner.nextInt());
                else if (key.equalsIgnoreCase("NBANDS"))
                    values.setValue(key, scanner.nextInt());
                else if (key.equalsIgnoreCase("NBITS"))
                    values.setValue(key, scanner.nextInt());
                else if (key.equalsIgnoreCase("BANDROWBYTES"))
                {
                    // BANDROWBYTES number of bytes in one row of data
                    values.setValue(key, scanner.nextInt());
                }

                else if (key.equalsIgnoreCase("TOTALROWBYTES"))
                {
                    // TOTALROWBYTES number of bytes in one row of data (for multi-band)
                    values.setValue(key, scanner.nextInt());
                }

                else if (key.equalsIgnoreCase("SKIPBYTES"))
                {
                    // SKIPBYTES number of header bytes before data starts in binary file
                    values.setValue(key, scanner.nextInt());
                }
                else if (key.equalsIgnoreCase("NODATA") || key.equalsIgnoreCase("NODATA_VALUE"))
                {
                    // NODATA_VALUE is a newer version of the NODATA keyword, often = -9999
                    double nodata = scanner.nextDouble();
                    values.setValue(key, nodata);
                    values.setValue("NODATA", nodata);
                }
                else if (key.equalsIgnoreCase("ULXMAP"))
                {
                    // ULXMAP center x-coordinate of grid cell in upper-left corner
                    values.setValue(key, scanner.nextDouble());
                }
                else if (key.equalsIgnoreCase("ULYMAP"))
                {
                    // ULYMAP center y-coordinate of grid cell in upper-left corner
                    values.setValue(key, scanner.nextDouble());
                }
                else if (key.equalsIgnoreCase("XLLCORNER"))
                {
                    // XLLCORNER left-edge x-coordinate of grid cell in lower-left corner
                    values.setValue(key, scanner.nextDouble());
                }
                else if (key.equalsIgnoreCase("YLLCORNER"))
                {
                    // YLLCORNER bottom y-coordinate of grid cell in lower-left corner
                    values.setValue(key, scanner.nextDouble());
                }
                else if (key.equalsIgnoreCase("XLLCENTER"))
                {
                    // XLLCENTER center x-coordinate of grid cell in lower-left corner
                    values.setValue(key, scanner.nextDouble());
                }
                else if (key.equalsIgnoreCase("YLLCCENTER"))
                {
                    // YLLCCENTER center y-coordinate of grid cell in lower-left corner
                    values.setValue(key, scanner.nextDouble());
                }
                else if (key.equalsIgnoreCase("XDIM"))
                    values.setValue(key, scanner.nextDouble());
                else if (key.equalsIgnoreCase("YDIM"))
                    values.setValue(key, scanner.nextDouble());
                else if (key.equalsIgnoreCase("CELLSIZE"))
                {
                    // CELLSIZE size of a grid cell, using this keyword implies same size in x and y
                    double cell_size = scanner.nextDouble();
                    values.setValue(key, cell_size);
                    values.setValue("XDIM", cell_size);
                    values.setValue("YDIM", cell_size);
                }
                else if (key.equalsIgnoreCase("PIXELTYPE"))
                {
                    values.setValue(key, scanner.next());
                }
                else if (key.equalsIgnoreCase("BYTEORDER"))
                {
                    // BYTEORDER byte order (only relevant for binary files, e.g. BIL, FLT)
                    // I or LSBFIRST for Intel, M or MSBFIRST for Motorola
                    values.setValue(key, scanner.next());
                }
                else
                    values.setValue(key, scanner.next());
            }

            // USGS NED 10m HDR files do not contain NBANDS, NBITS, and PIXELTYPE properties
            if (!values.hasKey("NBANDS") || !values.hasKey("NBITS"))
            {
                if (values.hasKey(AVKey.FILE_SIZE) && values.hasKey("NCOLS") && values.hasKey("NROWS"))
                {
                    Integer nCols = (Integer) values.getValue("NCOLS");
                    Integer nRows = (Integer) values.getValue("NROWS");
                    Integer fileSize = (Integer) values.getValue(AVKey.FILE_SIZE);

                    double bits = (((double) fileSize) / ((double) nCols) / ((double) nRows)) * 8d;

                    if (bits == 8d || bits == 16d || bits == 32d)
                    {
                        values.setValue("NBANDS", 1);
                        values.setValue("NBITS", (int) bits);
                    }
                    if (bits == 24d)
                    {
                        values.setValue("NBANDS", 3);
                        values.setValue("NBITS", 8);
                    }
                }
            }
        }
        finally
        {
            if (null != scanner)
                scanner.close();
        }
    }

    /**
     * Retrieves the six lines from a world file.
     *
     * @param worldFile the world file.
     *
     * @return an array containing the six values in the order they are found in the world file.
     *
     * @throws IllegalArgumentException if the file is null.
     * @throws FileNotFoundException    if the file does not exist.
     * @throws IllegalStateException    if the file cannot be parsed as a world file.
     */
    public static double[] decodeWorldFile(File worldFile) throws FileNotFoundException
    {
        if (worldFile == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!worldFile.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", worldFile.getPath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        double[] values = new double[6];

        Scanner scanner = new Scanner(worldFile);
        scanner.useLocale(Locale.US);

        try
        {

            for (int i = 0; i < 6; i++)
            {
                if (scanner.hasNextDouble())
                {
                    values[i] = scanner.nextDouble();
                }
                else
                {
                    String message = Logging.getMessage("SurfaceImage.WorldFileLineMissing", i + 1);
                    Logging.logger().severe(message);
                    throw new IllegalStateException(message);
                }
            }
        }
        finally
        {
            scanner.close();
        }

        return values;
    }

    /**
     * Indicates whether world file values appear to be in latitude and longitude.
     *
     * @param values the six values to examine.
     *
     * @return true if the values are between the normal limits of latitude, [-90, 90], and longitude, [-180, 180],
     *         othewise false.
     */
    public static boolean worldFileValuesAppearGeographic(AVList values)
    {
        double xLocation;
        double yLocation;
        double xPixelSize;
        double yPixelSize;

        Object o = values.getValue(WORLD_FILE_X_LOCATION);
        if (o != null && o instanceof Double)
            xLocation = (Double) o;
        else
            return false;

        o = values.getValue(WORLD_FILE_Y_LOCATION);
        if (o != null && o instanceof Double)
            yLocation = (Double) o;
        else
            return false;

        o = values.getValue(WORLD_FILE_X_PIXEL_SIZE);
        if (o != null && o instanceof Double)
            xPixelSize = (Double) o;
        else
            return false;

        o = values.getValue(WORLD_FILE_Y_PIXEL_SIZE);
        if (o != null && o instanceof Double)
            yPixelSize = (Double) o;
        else
            return false;

        return (Angle.isValidLongitude(xPixelSize) && Angle.isValidLatitude(yPixelSize)
            && Angle.isValidLongitude(xLocation) && Angle.isValidLatitude(yLocation));
    }

    public static Sector extractSectorFromHeader(AVList values) // TODO: assumes degrees
    {
        if (null != values
            && values.hasKey("NROWS") && values.hasKey("NCOLS")
            && values.hasKey("XDIM") && values.hasKey("YDIM")
            )
        {
            Integer nCols = (Integer) values.getValue("NCOLS");
            Integer nRows = (Integer) values.getValue("NROWS");
            double xDim = Math.abs((Double) values.getValue("XDIM"));
            double yDim = Math.abs((Double) values.getValue("YDIM"));

            if (values.hasKey("XLLCORNER") && values.hasKey("YLLCORNER")
                && Angle.isValidLongitude((Double) values.getValue("XLLCORNER"))
                && Angle.isValidLatitude((Double) values.getValue("YLLCORNER"))
                )
            {
                // XLLCORNER,YLLCORNER left-edge x-coordinate and bottom y-coordinate of grid cell in lower-left corner

                double xmin = Angle.fromDegreesLongitude((Double) values.getValue("XLLCORNER")).degrees;
                double ymin = Angle.fromDegreesLatitude((Double) values.getValue("YLLCORNER")).degrees;

                double xmax = Angle.fromDegreesLongitude(xmin + (nCols * xDim)).degrees;
                double ymax = Angle.fromDegreesLatitude(ymin + (nRows * yDim)).degrees;

                return Sector.fromDegrees(ymin, ymax, xmin, xmax);
            }

            if (values.hasKey("XLLCENTER") && values.hasKey("YLLCCENTER")
                && Angle.isValidLongitude((Double) values.getValue("XLLCENTER"))
                && Angle.isValidLatitude((Double) values.getValue("YLLCCENTER"))
                )
            {
                // XLLCENTER,YLLCCENTER are center coordinate of grid cell in lower-left corner
                double xmin = Angle.fromDegreesLongitude((Double) values.getValue("XLLCENTER") - (xDim / 2d)).degrees;
                double ymin = Angle.fromDegreesLatitude((Double) values.getValue("YLLCENTER") - (yDim / 2d)).degrees;

                double xmax = Angle.fromDegreesLongitude(xmin + (nCols * xDim)).degrees;
                double ymax = Angle.fromDegreesLatitude(ymin + (nRows * yDim)).degrees;

                return Sector.fromDegrees(ymin, ymax, xmin, xmax);
            }

            if (values.hasKey("ULXMAP") && values.hasKey("ULYMAP")
                && Angle.isValidLongitude((Double) values.getValue("ULXMAP"))
                && Angle.isValidLatitude((Double) values.getValue("ULYMAP"))
                )
            {
                // ULXMAP and ULYMAP are center coordinates of grid cell in upper-left corner
                double xmin = Angle.fromDegreesLongitude((Double) values.getValue("ULXMAP") - (xDim / 2d)).degrees;
                double ymax = Angle.fromDegreesLatitude((Double) values.getValue("ULYMAP") + (yDim / 2d)).degrees;

                double xmax = Angle.fromDegreesLongitude(xmin + (nCols * xDim)).degrees;
                double ymin = Angle.fromDegreesLatitude(ymax - (nRows * yDim)).degrees;

                return Sector.fromDegrees(ymin, ymax, xmin, xmax);
            }
        }
        return null;
    }

    public static int[] parseSize(AVList values)
    {
        if (values == null)
            return null;

        if (!values.hasKey("NROWS") && !values.hasKey("NCOLS"))
            return null;

        return new int[] {(Integer) values.getValue("NCOLS"), (Integer) values.getValue("NROWS")};
    }

    public static Object parseByteOrder(AVList values)
    {
        if (values == null)
            return null;

        if (!values.hasKey("BYTEORDER"))
            return null;

        String s = values.getValue("BYTEORDER").toString();
        return (s.equalsIgnoreCase("I") || s.equalsIgnoreCase("LSBFIRST")) ? AVKey.LITTLE_ENDIAN : AVKey.BIG_ENDIAN;
    }

    public static Object parsePixelFormat(AVList values)
    {
        if (values == null)
            return null;

        if (values.hasKey("NBANDS") && values.hasKey("NBITS"))
        {
            Integer nBands = (Integer) values.getValue("NBANDS");
            Integer nBits = (Integer) values.getValue("NBITS");

            if (nBands == 1 && (nBits == 16 || nBits == 32))
                return AVKey.ELEVATION;
            if (nBands == 1 && nBits == 8)
                return AVKey.IMAGE;
            if (nBands == 3 && nBits == 8)
                return AVKey.IMAGE;
        }

        return null;
    }

    public static Object parseDataType(AVList values)
    {
        if (values == null)
            return null;

        if (values.hasKey("NBITS"))
        {
            Integer nBits = (Integer) values.getValue("NBITS");

            switch (nBits)
            {
                case 8:
                    return AVKey.INT8;
                case 16:
                    return AVKey.INT16;
                case 32:
                    return AVKey.FLOAT32;
            }
        }
        else if (values.hasKey("PIXELTYPE"))
        {
            String pixelType = (String) values.getValue("PIXELTYPE");
            if ("FLOAT".equalsIgnoreCase(pixelType))
                return AVKey.FLOAT32;
        }

        return null;
    }

    public static Object parseMissingDataValue(AVList values)
    {
        if (values == null)
            return null;

        if (!values.hasKey("NODATA"))
            return null;

        return values.getValue("NODATA");
    }

    /**
     * Decodes the six values of a world file in the lat/lon coordinate system to a Sector. The rotation values are
     * ignored.
     *
     * @param values      the values to parse
     * @param imageWidth  the width of the image associated with the world file.
     * @param imageHeight the height of the image associated with the world file.
     *
     * @return a sector computed from the world file values.
     *
     * @throws IllegalArgumentException if the values array is null or has a length less than six, or the image width
     *                                  and height are less than zero.
     * @throws IllegalStateException    if the decoded values are not within the normal range of latituded and
     *                                  longitude.
     * @see #worldFileValuesAppearGeographic(gov.nasa.worldwind.avlist.AVList)
     */
    public static Sector parseDegrees(AVList values, int imageWidth, int imageHeight)
    {
        if (values == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (imageWidth <= 0 || imageHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidImageSize");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle latOrigin = Angle.fromDegrees((Double) values.getValue(WORLD_FILE_Y_LOCATION));
        // Make y offset negative if it's not already. World file convention is upper left origin.
        double s = (Double) values.getValue(WORLD_FILE_Y_PIXEL_SIZE);
        // The latitude and longitude dimensions are computed by multiplying the pixel size by the image's width or
        // height. The pixel size denotes the dimension of a pixel in degrees.
        Angle latOffset = latOrigin.addDegrees((s <= 0 ? s : -s) * imageHeight);
        Angle lonOrigin = Angle.fromDegrees((Double) values.getValue(WORLD_FILE_X_LOCATION));
        Angle lonOffset = lonOrigin.addDegrees((Double) values.getValue(WORLD_FILE_X_PIXEL_SIZE) * imageWidth);

        Angle minLon, maxLon;
        if (lonOrigin.degrees < lonOffset.degrees)
        {
            minLon = lonOrigin;
            maxLon = lonOffset;
        }
        else
        {
            minLon = lonOffset;
            maxLon = lonOrigin;
        }

        Angle minLat, maxLat;
        if (latOrigin.degrees < latOffset.degrees)
        {
            minLat = latOrigin;
            maxLat = latOffset;
        }
        else
        {
            minLat = latOffset;
            maxLat = latOrigin;
        }

        Sector sector = new Sector(minLat, maxLat, minLon, maxLon);

        if (!sector.isWithinLatLonLimits())
        {
            String message = Logging.getMessage("generic.SectorNotGeographic");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return sector;
    }

    /**
     * Compute a <code>Sector</code> from UTM world file values.
     *
     * @param values      the six values from the world file.
     * @param imageWidth  the width of the image associated with the world file.
     * @param imageHeight the height of the image associated with the world file.
     * @param zone        the UTM zone number (1 to 60), can be zero if expected units are in decimal degrees.
     * @param hemisphere  the UTM hemisphere, either {@link gov.nasa.worldwind.avlist.AVKey#NORTH} or {@link
     *                    gov.nasa.worldwind.avlist.AVKey#SOUTH}.
     *
     * @return the corresponding <code>Sector</code> or <code>null</code> if the sector could not be computed.
     *
     * @throws IllegalArgumentException if the values array is null or has a length less than six, , the image width and
     *                                  height are less than zero, or the hemisphere indicator is not {@link
     *                                  gov.nasa.worldwind.avlist.AVKey#NORTH} or {@link gov.nasa.worldwind.avlist.AVKey#SOUTH}
     * @throws IllegalStateException    if the decoded values are not within the normal range of latituded and
     *                                  longitude.
     * @see #worldFileValuesAppearGeographic(gov.nasa.worldwind.avlist.AVList)
     */
    @SuppressWarnings( {"UnnecessaryLocalVariable"})
    public static Sector parseUTM(double[] values, int imageWidth, int imageHeight, int zone, String hemisphere)
    {

        if (values.length < 6)
        {
            String message = Logging.getMessage("WorldFile.TooFewWorldFileValues");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (imageWidth <= 0 || imageHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidImageSize");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (zone < 1 || zone > 60)
        {
            String message = Logging.getMessage("generic.ZoneIsInvalid", zone);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!AVKey.NORTH.equals(hemisphere) && !AVKey.SOUTH.equals(hemisphere))
        {
            String msg = Logging.getMessage("generic.HemisphereIsInvalid", hemisphere);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Ref http://en.wikipedia.org/wiki/World_file
        double A = values[0]; // pixel size in the x-direction in map units/pixel
        double D = values[1]; // rotation about y-axis
        double B = values[2]; // rotation about x-axis
        double E = values[3]; // pixel size in the y-direction in map units, almost always negative
        double C = values[4]; // x-coordinate of the center of the upper left pixel
        double F = values[5]; // y-coordinate of the center of the upper left pixel

        double ULx = C;
        double ULy = F;
        double LRx = A * (imageWidth - 1) + B * (imageHeight - 1) + ULx;
        double LRy = D * (imageWidth - 1) + E * (imageHeight - 1) + ULy;

        UTMCoord UL = UTMCoord.fromUTM(zone, hemisphere, ULx, ULy);
        UTMCoord LR = UTMCoord.fromUTM(zone, hemisphere, LRx, LRy);

        Sector sector = new Sector(LR.getLatitude(), UL.getLatitude(), UL.getLongitude(), LR.getLongitude());

        if (!sector.isWithinLatLonLimits())
        {
            String message = Logging.getMessage("generic.SectorNotGeographic");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return sector;
    }

    /** Pattern matching the geographic coordinate system keyword in an OGC coordinate system well-known text. */
    protected static final Pattern GEOGCS_WKT_PATTERN = Pattern.compile("\\{*GEOGCS[\\[\\(](.*)[\\]\\)]\\}*");
    /** Pattern matching the projected coordinate system keyword in an OGC coordinate system well-known text. */
    protected static final Pattern PROJCS_WKT_PATTERN = Pattern.compile("\\{*PROJCS[\\[\\(](.*)[\\]\\)]\\}*");
    /** Pattern matching the UTM name in an projected coordinate system's well-known text. */
    protected static final Pattern UTM_NAME_WKT_PATTERN = Pattern.compile(".*UTM.*ZONE.*?(\\d+).*?([\\w\\s]+).*?");

    /**
     * Retrieves the coordinate system and its parameters from an OGC coordinate system encoded as well-known text. For
     * details, see to the OGC Coordinate Transform Service (CT) specification at <a
     * href="http://www.opengeospatial.org/standards/ct">http://www.opengeospatial.org/standards/ct</a>. This recognizes
     * Geographic and UTM coordinate systems. This configures the parameter list according to the coordinate system as
     * follows: <ul> <li>Geographic - {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM} set to {@link
     * gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM_GEOGRAPHIC}.</li> <li>Projected coordinate system: Universal
     * Transverse Mercator (UTM) - {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM} set to {@link
     * gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM_PROJECTED} and {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_NAME}
     * set to {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_UTM}. {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_HEMISPHERE}
     * set to either {@link gov.nasa.worldwind.avlist.AVKey#NORTH} or {@link gov.nasa.worldwind.avlist.AVKey#SOUTH}.
     * {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_ZONE} set to an integer in the range 1-60</li> <li>Projected
     * coordinate system: unknown projection - {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM} set to {@link
     * gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM_PROJECTED} and {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_NAME}
     * set to {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_UNKNOWN}.</li> <li>Unknown coordinate system - {@link
     * gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM} set to {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM_UNKNOWN}.
     * </ul> If an exception occurs while parsing the coordinate system text, the parameter list is left unchanged.
     *
     * @param text   a String containing an OGC coordinate system in well-known text format.
     * @param params the coordinate system parameter list, or null to indicate a parameter list should be created.
     *
     * @return the coordinate system parameter list.
     *
     * @throws IllegalArgumentException if <code>text</code> is null.
     */
    public static AVList decodeOGCCoordinateSystemWKT(String text, AVList params)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        // Convert the coordinate system text to upper case. The coordinate system regular expressions match against
        // upper case characters.
        text = text.trim().toUpperCase();

        try
        {
            Matcher csMatcher = GEOGCS_WKT_PATTERN.matcher(text);
            if (csMatcher.matches())
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
            }
            else if ((csMatcher = PROJCS_WKT_PATTERN.matcher(text)).matches())
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_PROJECTED);

                String csText = csMatcher.group(1);
                Matcher projMatcher = UTM_NAME_WKT_PATTERN.matcher(csText);
                if (projMatcher.matches())
                {
                    params.setValue(AVKey.PROJECTION_NAME, AVKey.PROJECTION_UTM);

                    // Parse the UTM zone from the coordinate system name.
                    String s = projMatcher.group(1);
                    if (s != null)
                    {
                        Integer i = WWUtil.makeInteger(s.trim());
                        if (i != null && i >= 1 && i <= 60)
                            params.setValue(AVKey.PROJECTION_ZONE, i);
                    }

                    if (params.getValue(AVKey.PROJECTION_ZONE) == null)
                        Logging.logger().warning(Logging.getMessage("generic.ZoneIsInvalid", s));

                    // Parse the UTM hemisphere form the coordinate system name.
                    s = projMatcher.group(2);
                    if (s != null)
                    {
                        s = s.trim();
                        if (s.startsWith("N") || s.startsWith("n"))
                            params.setValue(AVKey.PROJECTION_HEMISPHERE, AVKey.NORTH);
                        else if (s.startsWith("S") || s.startsWith("s"))
                            params.setValue(AVKey.PROJECTION_HEMISPHERE, AVKey.SOUTH);
                    }

                    if (params.getValue(AVKey.PROJECTION_HEMISPHERE) == null)
                        Logging.logger().warning(Logging.getMessage("generic.HemisphereIsInvalid", s));
                }
                else
                {
                    params.setValue(AVKey.PROJECTION_NAME, AVKey.PROJECTION_UNKNOWN);
                }
            }
            else
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE,
                Logging.getMessage("generic.ExceptionParsingCoordinateSystem", text), e);
        }

        return params;
    }

    /**
     * Checks if the source is accompanied by any world file
     * <p/>
     * The source type may be one of the following: <ul><li>{@link java.net.URL}</li> <li>{@link java.net.URI}</li>
     * <li>{@link java.io.File}</li> <li>{@link String} containing a valid URL description, a valid URI description, or
     * a valid path to a local file.</li> </ul>
     *
     * @param source the source to local file path.
     *
     * @return TRUE if there is a world file for the source, otherwise returns FALSE
     */
    public static boolean hasWorldFiles(Object source)
    {
        try
        {
            File file = WWIO.getFileForLocalAddress(source);
            if (null == file)
                return false;

            java.io.File[] worldFiles = WorldFile.getWorldFiles(file);
            if (worldFiles == null || worldFiles.length == 0)
            {
                return false;
            }
        }
        catch (java.io.IOException e)
        {
            // Not interested in logging the exception, we only want to report the failure to read.
            return false;
        }

        return true;
    }

    /**
     * Reads and decodes world file for the source
     * <p/>
     * The source type may be one of the following: <ul><li>{@link java.net.URL}</li> <li>{@link java.net.URI}</li>
     * <li>{@link java.io.File}</li> <li>{@link String} containing a valid URL description, a valid URI description, or
     * a valid path to a local file.</li> </ul>
     *
     * @param source the source to local file path.
     * @param params AVList for storing parameters
     *
     * @throws IOException if the world file could not be read
     */
    public static void readWorldFiles(Object source, AVList params) throws java.io.IOException
    {
        File file = WWIO.getFileForLocalAddress(source);
        if (null == file)
        {
            String message = Logging.getMessage("generic.UnrecognizedSourceType", source);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        if (null == params)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        java.io.File[] worldFiles = WorldFile.getWorldFiles(file);
        WorldFile.decodeWorldFiles(worldFiles, params);

        // Translate the property WORLD_FILE_IMAGE_SIZE to separate properties WIDTH and HEIGHT.
        Object o = params.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
        if (o != null && o instanceof int[])
        {
            int[] size = (int[]) o;

            if (!params.hasKey(AVKey.WIDTH))
                params.setValue(AVKey.WIDTH, size[0]);

            if (!params.hasKey(AVKey.HEIGHT))
                params.setValue(AVKey.HEIGHT, size[1]);
        }
    }
}
