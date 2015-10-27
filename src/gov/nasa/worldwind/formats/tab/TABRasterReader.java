/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tab;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.*;

/**
 * Reader for the MapInfo TAB file format.
 * Documentation on the MapInfo TAB format can be found here:
 * http://community.mapinfo.com/forums/thread.jspa?messageID=23770&
 *
 * @author dcollins
 * @version $Id: TABRasterReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class TABRasterReader
{
    public static final String VERSION = "TABRaster.Version";
    public static final String CHARSET = "TABRaster.Charset";
    public static final String TYPE = "TABRaster.Type";
    public static final String IMAGE_PATH = "TABRaster.ImagePath";
    public static final String LABEL = "TABRaster.Label";

    public static final String RASTER_STYLE_BRIGHTNESS_VALUE = "TABRaster.RasterStyleBrightnessValue";
    public static final String RASTER_STYLE_CONTRAST_VALUE = "TABRaster.RasterStyleContrastValue";
    public static final String RASTER_STYLE_GRAYSCALE_VALUE = "TABRaster.RasterStyleGrayscaleValue";
    public static final String RASTER_STYLE_USE_TRANSPARENT_VALUE = "TABRaster.RasterStyleUseTransparentValue";
    public static final String RASTER_STYLE_TRANSPARENT_INDEX_VALUE = "TABRaster.RasterStyleTransparentIndexValue";
    public static final String RASTER_STYLE_GRID_VALUE = "TABRaster.RasterStyleGridValue";
    public static final String RASTER_STYLE_TRANSPARENT_COLOR_VALUE = "TABRaster.TransparentColorValue";
    public static final String RASTER_STYLE_TRANSLUCENT_ALPHA_VALUE = "TABRaster.TranslucentAlphaValue";
    
    protected static final String TAG_DEFINITION = "Definition";
    protected static final String TAG_FILE = "File";
    protected static final String TAG_HEADER_TABLE = "!table";
    protected static final String TAG_HEADER_VERSION = "!version";
    protected static final String TAG_HEADER_CHARSET = "!charset";
    protected static final String TAG_TABLE = "Table";
    protected static final String TAG_TYPE = "Type";

    protected static final int RASTER_STYLE_ID_BRIGHTNESS_VALUE = 1;
    protected static final int RASTER_STYLE_ID_CONTRAST_VALUE = 2;
    protected static final int RASTER_STYLE_ID_GRAYSCALE_VALUE = 3;
    protected static final int RASTER_STYLE_ID_USE_TRANSPARENT_VALUE = 4;
    protected static final int RASTER_STYLE_ID_TRANSPARENT_INDEX_VALUE = 5;
    protected static final int RASTER_STYLE_ID_GRID_VALUE = 6;
    protected static final int RASTER_STYLE_ID_TRANSPARENT_COLOR_VALUE = 7;
    protected static final int RASTER_STYLE_ID_TRANSLUCENT_ALPHA_VALUE = 8;

    public TABRasterReader()
    {
    }

    public static java.io.File getTABFileFor(java.io.File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.io.File parent = file.getParentFile();
        if (parent == null)
            return null;

        String tabFilename = WWIO.replaceSuffix(file.getName(), ".tab");

        // The file already has a TAB extension. Rather than returning a self reference, we return null to deonte that
        // a TAB file does not associate with itself.
        if (file.getName().equalsIgnoreCase(tabFilename))
        {
            return null;
        }

        // Find the first sibling with the matching filename, and TAB extension.
        for (java.io.File child : parent.listFiles())
        {
            if (!child.equals(file) && child.getName().equalsIgnoreCase(tabFilename))
            {
                return child;
            }
        }

        return null;
    }

    public boolean canRead(java.io.File file)
    {
        if (file == null || !file.exists() || !file.canRead())
            return false;

        java.io.FileReader fileReader = null;
        try
        {
            fileReader = new java.io.FileReader(file);
            RasterControlPointList controlPoints = new RasterControlPointList();
            return this.doCanRead(fileReader, controlPoints);
        }
        catch (Exception ignored)
        {
            return false;
        }
        finally
        {
            //noinspection EmptyCatchBlock
            try
            {
                if (fileReader != null)
                    fileReader.close();
            }
            catch (java.io.IOException e)
            {
            }
        }
    }

    public boolean canRead(String path)
    {
        if (path == null)
            return false;

        Object streamOrException = WWIO.getFileOrResourceAsStream(path, this.getClass());
        if (streamOrException == null || streamOrException instanceof Exception)
            return false;

        java.io.InputStream stream = (java.io.InputStream) streamOrException;
        try
        {
            java.io.InputStreamReader streamReader = new java.io.InputStreamReader(stream);
            RasterControlPointList controlPoints = new RasterControlPointList();
            return this.doCanRead(streamReader, controlPoints);
        }
        catch (Exception ignored)
        {
            return false;
        }
        finally
        {
            WWIO.closeStream(stream, path);
        }
    }

    public RasterControlPointList read(java.io.File file) throws java.io.IOException
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!file.canRead())
        {
            String message = Logging.getMessage("generic.FileNoReadPermission", file);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.io.FileReader fileReader = null;
        try
        {
            fileReader = new java.io.FileReader(file);
            RasterControlPointList controlPoints = new RasterControlPointList();
            this.doRead(fileReader, file.getParent(), controlPoints);
            return controlPoints;
        }
        finally
        {
            WWIO.closeStream(fileReader, file.getPath());
        }
    }

    public RasterControlPointList read(String path) throws java.io.IOException
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object streamOrException = WWIO.getFileOrResourceAsStream(path, this.getClass());
        if (streamOrException == null || streamOrException instanceof Exception)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadFile",
                (streamOrException != null) ? streamOrException : path);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.io.InputStream stream = (java.io.InputStream) streamOrException;
        try
        {
            java.io.InputStreamReader streamReader = new java.io.InputStreamReader(stream);

            String workingDirectory = WWIO.getParentFilePath(path);
            
            RasterControlPointList controlPoints = new RasterControlPointList();
            this.doRead(streamReader, workingDirectory, controlPoints);
            return controlPoints;
        }
        finally
        {
            WWIO.closeStream(stream, path);
        }
    }

    protected boolean doCanRead(java.io.Reader reader, RasterControlPointList controlPoints)
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            java.io.BufferedReader br = new java.io.BufferedReader(reader);
            this.readHeader(br, controlPoints);

            String s = this.validateHeaderValues(controlPoints);
            return (s == null);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    protected void doRead(java.io.Reader reader, String workingDirectory, RasterControlPointList controlPoints)
        throws java.io.IOException
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.io.BufferedReader br = new java.io.BufferedReader(reader);
        this.readHeader(br, controlPoints);
        this.readDefinitionTable(br, workingDirectory, controlPoints);

        String s = this.validateHeaderValues(controlPoints);
        if (s != null)
        {
            String message = Logging.getMessage("TABReader.MissingHeaderValues", s);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        s = this.validateRasterControlPoints(controlPoints);
        if (s != null)
        {
            String message = Logging.getMessage("TABReader.MissingRasterData", s);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }
    }

    protected void readHeader(java.io.BufferedReader reader, RasterControlPointList controlPoints)
        throws java.io.IOException
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String line = this.skipToHeader(reader);
        if (line == null || !line.equalsIgnoreCase(TAG_HEADER_TABLE))
        {
            String message = Logging.getMessage("TABReader.InvalidMagicString", line);
            Logging.logger().severe(message);
            throw new java.io.IOException(message);
        }

        line = this.nextLine(reader);
        if (line != null && line.startsWith(TAG_HEADER_VERSION))
        {
            if (controlPoints.getValue(VERSION) == null)
                setProperty(line, VERSION, controlPoints);
        }

        line = this.nextLine(reader);
        if (line != null && line.startsWith(TAG_HEADER_CHARSET))
        {
            if (controlPoints.getValue(CHARSET) == null)
                setProperty(line, CHARSET, controlPoints);
        }
    }

    protected void readDefinitionTable(java.io.BufferedReader reader, String workingDirectory,
        RasterControlPointList controlPoints) throws java.io.IOException
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String line = this.skipToDefinition(reader);
        if (line == null || !line.equalsIgnoreCase(TAG_TABLE))
            return;

        line = this.nextLine(reader);
        if (line != null && line.startsWith(TAG_FILE))
        {
            if (controlPoints.getStringValue(IMAGE_PATH) == null
                || controlPoints.getStringValue(IMAGE_PATH).length() == 0)
            {
                String[] tokens = line.split(" ", 2);
                if (tokens.length >= 2 && tokens[1] != null)
                {
                    String pathname = stripQuotes(tokens[1].trim());
                    controlPoints.setValue(IMAGE_PATH, WWIO.appendPathPart(workingDirectory, pathname));
                }
            }
        }

        line = this.nextLine(reader);
        if (line != null && line.startsWith(TAG_TYPE))
        {
            if (controlPoints.getValue(TYPE) == null)
                setProperty(line, TYPE, controlPoints);
        }

        this.readControlPoints(reader, controlPoints);
        this.readCoordSys(reader, controlPoints);
        this.readRasterStyle(reader, controlPoints);
    }

    protected void readControlPoints(java.io.BufferedReader reader, RasterControlPointList controlPoints)
        throws java.io.IOException
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "[(](.+)[,](.+)[)].+[(](.+)[,](.+)[)][\\s]+.+[\\s]+[\"\']?(.+)[\"\']?[,]?");

        String line;
        java.util.regex.Matcher matcher;
        while ((line = this.nextLine(reader)) != null && (matcher = pattern.matcher(line)).matches())
        {
            String swx = matcher.group(1);
            String swy = matcher.group(2);
            String srx = matcher.group(3);
            String sry = matcher.group(4);
            String label = matcher.group(5);

            Double wx = WWUtil.convertStringToDouble(swx);
            Double wy = WWUtil.convertStringToDouble(swy);
            Double rx = WWUtil.convertStringToDouble(srx);
            Double ry = WWUtil.convertStringToDouble(sry);

            if (wx != null && wy != null && rx != null && ry != null)
            {
                RasterControlPointList.ControlPoint controlPoint =
                    new RasterControlPointList.ControlPoint(wx, wy, rx, ry);
                controlPoint.setValue(LABEL, label);
                controlPoints.add(controlPoint);
            }
        }
    }

    protected void readCoordSys(java.io.BufferedReader reader, RasterControlPointList controlPoints)
        throws java.io.IOException
    {
        if (reader == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void readRasterStyle(java.io.BufferedReader reader, RasterControlPointList controlPoints)
        throws java.io.IOException
    {
        if (controlPoints == null)
        {
            String message = Logging.getMessage("nullValue.RasterControlPointListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO
    }

    protected String skipToHeader(java.io.BufferedReader reader) throws java.io.IOException
    {
        return this.nextLine(reader);
    }

    protected String skipToDefinition(java.io.BufferedReader reader) throws java.io.IOException
    {
        String line = this.nextLine(reader);

        if (line == null || line.length() == 0)
            return null;

        String[] tokens = line.split(" ", 2);
        if (tokens.length < 2)
            return null;

        return (tokens[1] != null) ? tokens[1].trim() : null;
    }

    protected String nextLine(java.io.BufferedReader reader) throws java.io.IOException
    {
        // Read until the next non-whitespace line.

        String line;
        while ((line = reader.readLine()) != null && line.trim().length() == 0)
        {
        }

        return (line != null) ? line.trim() : null;
    }

    protected String validateHeaderValues(AVList values)
    {
        StringBuilder sb = new StringBuilder();

        String s = values.getStringValue(VERSION);
        if (s == null || s.length() == 0)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(Logging.getMessage("term.version"));
        }

        s = values.getStringValue(CHARSET);
        if (s == null || s.length() == 0)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(Logging.getMessage("term.charset"));
        }

        if (sb.length() > 0)
            return sb.toString();

        return null;
    }

    protected String validateRasterControlPoints(RasterControlPointList controlPoints)
    {
        StringBuilder sb = new StringBuilder();

        if (controlPoints.getStringValue(IMAGE_PATH) == null && controlPoints.getStringValue(IMAGE_PATH).length() == 0)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(Logging.getMessage("TABReader.MissingOrInvalidFileName",
                controlPoints.getStringValue(IMAGE_PATH)));
        }

        if (controlPoints.size() < 3)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(Logging.getMessage("TABReader.NotEnoughControlPoints", controlPoints.size()));
        }

        if (sb.length() > 0)
            return sb.toString();

        return null;
    }

    private static String stripQuotes(String s)
    {
        if (s.startsWith("\"") || s.startsWith("\'"))
            s = s.substring(1, s.length());
        if (s.endsWith("\"") || s.endsWith("\'"))
            s = s.substring(0, s.length() - 1);
        return s;
    }

    private static void setProperty(String line, String key, AVList values)
    {
        String[] tokens = line.split(" ", 2);
        if (tokens == null || tokens.length < 2)
            return;

        String value = tokens[1];
        if (value == null || value.trim().length() == 0)
            return;

        values.setValue(key, value.trim());
    }
}
