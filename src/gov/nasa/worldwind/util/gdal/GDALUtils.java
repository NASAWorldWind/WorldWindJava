/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.gdal;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.data.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.tiff.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import org.gdal.gdal.*;
import org.gdal.gdalconst.*;
import org.gdal.ogr.ogr;
import org.gdal.osr.*;

import java.awt.*;
import java.awt.color.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALUtils.java 3031 2015-04-17 14:53:23Z tgaskins $
 */
public class GDALUtils
{
    public static long ALPHA_MASK = 0xFFFFFFFFL;

    protected static byte ALPHA_TRANSPARENT = (byte) 0x00;
    protected static byte ALPHA_OPAQUE = (byte) 0xFF;

    protected static final String JAVA_LIBRARY_PATH = "java.library.path";
    protected static final String GDAL_DRIVER_PATH = "GDAL_DRIVER_PATH";
    protected static final String OGR_DRIVER_PATH = "OGR_DRIVER_PATH";
    protected static final String GDAL_DATA_PATH = "GDAL_DATA";

    protected static final AtomicBoolean gdalIsAvailable = new AtomicBoolean(false);

    // This is an OLD default libname request by WW build of GDAL
    protected static final String gdalalljni = Configuration.isMacOS()
        ? "gdalalljni" : (is32bitArchitecture() ? "gdalalljni32" : "gdalalljni64");

    protected static final CopyOnWriteArraySet<String> loadedLibraries = new CopyOnWriteArraySet<String>();
    protected static final CopyOnWriteArraySet<String> failedLibraries = new CopyOnWriteArraySet<String>();

    static
    {
        // Allow the app or user to prevent library loader replacement.
        if (System.getProperty("gov.nasa.worldwind.prevent.gdal.loader.replacement") == null)
            replaceLibraryLoader(); // This must be the first line of initialization
        initialize();
    }

    private static class GDALLibraryLoader implements gdal.LibraryLoader
    {
        public void load(String libName) throws UnsatisfiedLinkError
        {
            if (WWUtil.isEmpty(libName))
            {
                String message = Logging.getMessage("nullValue.LibraryIsNull");
                Logging.logger().severe(message);
                throw new java.lang.UnsatisfiedLinkError(message);
            }

            // check if the library is already loaded
            if (loadedLibraries.contains(libName))
                return;

            String message;

            // check if the library is already know (from previous attempts) to fail to load
            if ( !failedLibraries.contains(libName) )
            {
                try
                {
                    NativeLibraryLoader.loadLibrary(libName);
                    loadedLibraries.add(libName);
                    Logging.logger().info( Logging.getMessage("generic.LibraryLoadedOK", libName ));

                    return; // GOOD! Leaving now
                }
                catch (Throwable t)
                {
                    String reason = WWUtil.extractExceptionReason(t);
                    message = Logging.getMessage("generic.LibraryNotLoaded", libName, reason);
                    Logging.logger().finest(message);

                    failedLibraries.add(libName);
                }
            }
            else
            {
                String reason = Logging.getMessage("generic.LibraryNotFound", libName );
                message = Logging.getMessage("generic.LibraryNotLoaded", libName, reason);
            }

            throw new UnsatisfiedLinkError(message);
        }
    }

    protected static void replaceLibraryLoader()
    {
        try
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class gdalClass = cl.loadClass("org.gdal.gdal.gdal");

            boolean isKnownBuild = false;
            Method[] methods = gdalClass.getDeclaredMethods();
            for (Method m : methods)
            {
                if ("setLibraryLoader".equals(m.getName()))
                {
                    gdal.setLibraryLoader(new GDALLibraryLoader());
//                    Logging.logger().finest(Logging.getMessage("gdal.LibraryLoaderReplacedOK"));
                    isKnownBuild = true;
                    break;
                }
            }

            if (!isKnownBuild)
            {
                String message = Logging.getMessage("gdal.UnknownBuild", gdal.VersionInfo());
                Logging.logger().finest(message);
            }
        }
        catch (ClassNotFoundException cnf)
        {
            Logging.logger().finest(cnf.getMessage());
        }
        catch (Throwable t)
        {
            Logging.logger().finest(t.getMessage());
        }
    }

    protected static boolean is32bitArchitecture()
    {
        String arch = System.getProperty("sun.arch.data.model");
        if( !WWUtil.isEmpty(arch) )
            return ("32".equals(arch));

        // GNU JAVA does not return "sun.arch.data.model"
        return "x86".equals(System.getProperty("os.arch"));
    }

    protected static boolean gdalPreLoadNativeLibrary(boolean allowLogErrors)
    {
        try
        {
            NativeLibraryLoader.loadLibrary(gdalalljni);
            loadedLibraries.add(gdalalljni);
            Logging.logger().info( Logging.getMessage("generic.LibraryLoadedOK", gdalalljni ));

            return true;
        }
        catch (Throwable t)
        {
            if( allowLogErrors )
                Logging.logger().finest(WWUtil.extractExceptionReason(t));
        }

        return false;
    }

    protected static void initialize()
    {
        try
        {
            boolean runningAsJavaWebStart = (null != System.getProperty("javawebstart.version", null));

			// attempt to load library from default locations
			// (current path OR by specifying java.library.path from the command line)
            boolean gdalNativeLibraryLoaded = gdalPreLoadNativeLibrary(false);

            if (!gdalNativeLibraryLoaded && !runningAsJavaWebStart)
            {
            	// if we are here, library is not in any default place, so we will search in sub-folders
                String[] folders = findGdalFolders();
                String newJavaLibraryPath = buildPathString(folders, true);
                if (newJavaLibraryPath != null)
                {
                    alterJavaLibraryPath(newJavaLibraryPath);
//                    gdalNativeLibraryLoaded = gdalLoadNativeLibrary(true);
                }
            }

            if ( /* gdalNativeLibraryLoaded && */ gdalJNI.isAvailable() && gdalconstJNI.isAvailable())
            {
                if (!runningAsJavaWebStart)
                {
                    // No need, because we are build one dynamic library that contains ALL  drivers
                    // and dependant libraries
                    // gdal.SetConfigOption(GDAL_DRIVER_PATH, pathToLibs);
                    // gdal.SetConfigOption(OGR_DRIVER_PATH, pathToLibs);
                    String dataFolder = findGdalDataFolder();
                    if (null != dataFolder)
                    {
                        String msg = Logging.getMessage("gdal.SharedDataFolderFound", dataFolder);
                        Logging.logger().finest(msg);
                        gdal.SetConfigOption(GDAL_DATA_PATH, dataFolder);
                    }
                }

                gdal.AllRegister();
                ogr.RegisterAll();

                /**
                 *  "VERSION_NUM": Returns GDAL_VERSION_NUM formatted as a string.  ie. "1170"
                 *  "RELEASE_DATE": Returns GDAL_RELEASE_DATE formatted as a string. "20020416"
                 *  "RELEASE_NAME": Returns the GDAL_RELEASE_NAME. ie. "1.1.7"
                 *   "--version": Returns full version , ie. "GDAL 1.1.7, released 2002/04/16"
                 */
                String msg = Logging.getMessage("generic.LibraryLoadedOK", "GDAL v" + gdal.VersionInfo("RELEASE_NAME"));
                Logging.logger().info(msg);
                listAllRegisteredDrivers();

                gdalIsAvailable.set(true);
            }
            else
            {
                String reason = Logging.getMessage("generic.LibraryNotFound", "GDAL" );
                String msg = Logging.getMessage("generic.LibraryNotLoaded", "GDAL", reason );
                Logging.logger().warning(msg);
            }
        }
        catch (Throwable t)
        {
            Logging.logger().log(Level.FINEST, t.getMessage(), t);
        }
    }

    protected static String getCurrentDirectory()
    {
        String cwd = System.getProperty("user.dir");

        if (null == cwd || cwd.length() == 0)
        {
            String message = Logging.getMessage("generic.UsersHomeDirectoryNotKnown");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        return cwd;
    }

    protected static String[] findGdalFolders()
    {
        try
        {
            String cwd = getCurrentDirectory();

            FileTree fileTree = new FileTree(new File(cwd));
            fileTree.setMode(FileTree.FILES_AND_DIRECTORIES);

            GDALLibraryFinder filter = new GDALLibraryFinder(/*gdalalljni*/);
            fileTree.asList(filter);
            return filter.getFolders();
        }
        catch (Throwable t)
        {
            Logging.logger().severe(t.getMessage());
        }
        return null;
    }

    protected static String findGdalDataFolder()
    {
        try
        {
            String cwd = getCurrentDirectory();

            FileTree fileTree = new FileTree(new File(cwd));
            fileTree.setMode(FileTree.FILES_AND_DIRECTORIES);

            GDALDataFinder filter = new GDALDataFinder();
            fileTree.asList(filter);
            String[] folders = filter.getFolders();

            if (null != folders && folders.length > 0)
            {
                if (folders.length > 1)
                {
                    String msg = Logging.getMessage("gdal.MultipleDataFoldersFound", buildPathString(folders, false));
                    Logging.logger().warning(msg);
                }
                return folders[0];
            }
        }
        catch (Throwable t)
        {
            Logging.logger().severe(t.getMessage());
        }

        String message = Logging.getMessage("gdal.SharedDataFolderNotFound");
        Logging.logger().severe(message);
        // throw new WWRuntimeException( message );
        return null;
    }

    protected static String buildPathString(String[] folders, boolean addDefaultValues)
    {
        String del = System.getProperty("path.separator");
        StringBuffer path = new StringBuffer();

        path.append("lib-external/gdal").append(del);

        if (null != folders && folders.length > 0)
        {
            for (String folder : folders)
            {
                path.append(folder).append(del);
            }
        }
        if (addDefaultValues)
        {
            path.append(".").append(del); // append current directory
            path.append(System.getProperty("user.dir")).append(del);
            path.append(System.getProperty(JAVA_LIBRARY_PATH));
        }

        return path.toString();
    }

    protected static void listAllRegisteredDrivers()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < gdal.GetDriverCount(); i++)
        {
            Driver drv = gdal.GetDriver(i);
            String msg = Logging.getMessage("gdal.DriverDetails", drv.getShortName(), drv.getLongName(),
                drv.GetDescription());
            sb.append(msg).append("\n");
        }
        Logging.logger().finest(sb.toString());
    }

    /** @return returns an error string, if no errors returns null */
    public static String getErrorMessage()
    {
        try
        {
            if (gdalIsAvailable.get())
            {
                int errno = gdal.GetLastErrorNo();
                if (errno != gdalconst.CE_None)
                {
                    return Logging.getMessage("gdal.InternalError", errno, gdal.GetLastErrorMsg());
                }
            }
        }
        catch (Throwable t)
        {
            return t.getMessage();
        }
        return null;
    }

    /**
     * Opens image or elevation file, returns a DataSet object
     *
     * @param source       the location of the local file, expressed as either a String path, a File, or a file URL.
     * @param isSilentMode specifies a silent mode of reading file (usually needed for canRead() and readMetadata())
     *
     * @return returns a Dataset object
     *
     * @throws FileNotFoundException    if file not found
     * @throws IllegalArgumentException if file is null
     * @throws SecurityException        if file could not be read
     * @throws WWRuntimeException       if GDAL library was not initialized
     */
    public static Dataset open(Object source, boolean isSilentMode)
        throws FileNotFoundException, IllegalArgumentException, SecurityException, WWRuntimeException
    {
        if (!gdalIsAvailable.get())
        {
            if (isSilentMode)
            {
                return null;
            }

            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        File file = WWIO.getFileForLocalAddress(source);
        if (null == file)
        {
            if (isSilentMode)
            {
                return null;
            }

            String message = Logging.getMessage("generic.UnrecognizedSourceType", source.getClass().getName());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!file.exists())
        {
            if (isSilentMode)
            {
                return null;
            }

            String message = Logging.getMessage("generic.FileNotFound", file.getAbsolutePath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }

        if (!file.canRead())
        {
            if (isSilentMode)
            {
                return null;
            }

            String message = Logging.getMessage("generic.FileNoReadPermission", file.getAbsolutePath());
            Logging.logger().severe(message);
            throw new SecurityException(message);
        }

        Dataset ds = null;
        try
        {
            gdal.PushErrorHandler("CPLQuietErrorHandler");

            ds = gdal.Open(file.getAbsolutePath(), gdalconst.GA_ReadOnly);
        }
        finally
        {
            gdal.PopErrorHandler();
        }

        if (ds == null)
        {
            if (isSilentMode)
            {
                return null;
            }

            String message = Logging.getMessage("generic.CannotOpenFile", GDALUtils.getErrorMessage());
            Logging.logger().fine(message);
            throw new WWRuntimeException(message);
        }

        return ds;
    }

    /**
     * Opens image or elevation file, returns a DataSet object
     *
     * @param source the location of the local file, expressed as either a String path, a File, or a file URL.
     *
     * @return returns a Dataset object
     *
     * @throws FileNotFoundException    if file not found
     * @throws IllegalArgumentException if file is null
     * @throws SecurityException        if file could not be read
     * @throws WWRuntimeException       if GDAL library was not initialized
     */
    public static Dataset open(Object source)
        throws FileNotFoundException, IllegalArgumentException, SecurityException, WWRuntimeException
    {
        return open(source, false);
    }

    /**
     * Checks if a data raster can is readable
     *
     * @param source the location of the local file, expressed as either a String path, a File, or a file URL.
     *
     * @return true, if source is readable
     */
    public static boolean canOpen(Object source)
    {
        if (!gdalIsAvailable.get())
        {
            return false;
        }

        File file = (null != source) ? WWIO.getFileForLocalAddress(source) : null;
        if (null == file)
        {
            return false;
        }

        Dataset ds = null;
        boolean canOpen = false;

        try
        {
            gdal.PushErrorHandler("CPLQuietErrorHandler");

            if (file.exists() && file.canRead())
            {
                ds = gdal.Open(file.getAbsolutePath(), gdalconst.GA_ReadOnly);
                canOpen = !(ds == null);
            }
        }
        catch (Throwable t)
        {
            // this is a quiet mode, no need to log
        }
        finally
        {
            if (null != ds)
            {
                ds.delete();
            }

            gdal.PopErrorHandler();
        }
        return canOpen;
    }

    /**
     * Opens image or elevation file, returns as a BufferedImage (even for elevations)
     *
     * @param ds     GDAL's Dataset object
     * @param params AVList of parameters
     *
     * @return DataRaster returns as a BufferedImage (even for elevations)
     *
     * @throws IllegalArgumentException if file is null
     * @throws SecurityException        if file could not be read
     * @throws WWRuntimeException       if GDAL library was not initialized
     */
    protected static DataRaster composeImageDataRaster(Dataset ds, AVList params)
        throws IllegalArgumentException, SecurityException, WWRuntimeException
    {
        if (!gdalIsAvailable.get())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BufferedImage img = null;

        int width = ds.getRasterXSize();
        int height = ds.getRasterYSize();
        int bandCount = ds.getRasterCount();

        if( bandCount < 1 )
        {
            String message = Logging.getMessage("generic.UnexpectedBandCount", bandCount );
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double[] dbls = new Double[16];

        ByteBuffer[] bands = new ByteBuffer[bandCount];
        int[] bandsOrder = new int[bandCount];
        int[] offsets = new int[bandCount];

        int imgSize = width * height;
        int bandDataType = 0, buf_size = 0;

        double maxValue = -Double.MAX_VALUE;

        for (int bandIdx = 0; bandIdx < bandCount; bandIdx++)
        {
            /* Bands are not 0-base indexed, so we must add 1 */
            Band imageBand = ds.GetRasterBand(bandIdx + 1);
            if (null == imageBand)
            {
                String message = Logging.getMessage("nullValue.RasterBandIsNull`");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            bandDataType = imageBand.getDataType();
            buf_size = imgSize * (gdal.GetDataTypeSize(bandDataType) / 8);

            ByteBuffer data = ByteBuffer.allocateDirect(buf_size);
            data.order(ByteOrder.nativeOrder());

            int colorInt = imageBand.GetRasterColorInterpretation();

            if (params.hasKey(AVKey.RASTER_BAND_MAX_PIXEL_VALUE))
            {
                maxValue = (Double) params.getValue(AVKey.RASTER_BAND_MAX_PIXEL_VALUE);
            }
            else if ((bandDataType == gdalconstConstants.GDT_UInt16 || bandDataType == gdalconstConstants.GDT_UInt32)
                && colorInt != gdalconst.GCI_AlphaBand && colorInt != gdalconst.GCI_Undefined)
            {
                imageBand.GetMaximum(dbls);
                if (dbls[0] == null)
                {
                    double[] minmax = new double[2];
                    imageBand.ComputeRasterMinMax(minmax);
                    maxValue = (minmax[1] > maxValue) ? minmax[1] : maxValue;
                }
                else
                {
                    maxValue = (dbls[0] > maxValue) ? dbls[0] : maxValue;
                }
            }

            int returnVal = imageBand.ReadRaster_Direct(0, 0, imageBand.getXSize(),
                imageBand.getYSize(), width, height, bandDataType, data);

            if (returnVal != gdalconstConstants.CE_None)
            {
                throw new WWRuntimeException(GDALUtils.getErrorMessage());
            }

            int destBandIdx = bandIdx;

            if (colorInt == gdalconst.GCI_RedBand)
            {
                destBandIdx = 0;
            }
            else if (colorInt == gdalconst.GCI_GreenBand)
            {
                destBandIdx = 1;
            }
            else if (colorInt == gdalconst.GCI_BlueBand)
            {
                destBandIdx = 2;
            }

            bands[destBandIdx] = data;
            bandsOrder[destBandIdx] = destBandIdx;
            offsets[destBandIdx] = 0;
        }

        int bitsPerColor = gdal.GetDataTypeSize(bandDataType);

        int actualBitsPerColor = bitsPerColor;

        if (params.hasKey(AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL))
        {
            actualBitsPerColor = (Integer) params.getValue(AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL);
        }
        else if (maxValue > 0d)
        {
            actualBitsPerColor = (int) Math.ceil(Math.log(maxValue) / Math.log(2d));
        }
        else
        {
            actualBitsPerColor = bitsPerColor;
        }

        int[] reqBandOrder = bandsOrder;
        try
        {
            reqBandOrder = extractBandOrder(ds, params);
            if (null == reqBandOrder || 0 == reqBandOrder.length)
            {
                reqBandOrder = bandsOrder;
            }
            else
            {
                offsets = new int[reqBandOrder.length];
                bandsOrder = new int[reqBandOrder.length];
                for (int i = 0; i < reqBandOrder.length; i++)
                {
                    bandsOrder[i] = i;
                    offsets[i] = 0;
                }
            }
        }
        catch (Exception e)
        {
            reqBandOrder = bandsOrder;
            Logging.logger().severe(e.getMessage());
        }

        DataBuffer imgBuffer = null;
        int bufferType = 0;

        // A typical sample RGB:
        //  bitsPerSample is 24=3x8, bitsPerColor { 8,8,8 }, SignificantBitsPerColor {8,8,8}, byteOffsets {2, 1, 0}

        // A typical sample RGBA:
        //  bitsPerSample is 32=4x8, bitsPerColor { 8,8,8,8 }, SignificantBitsPerColor {8,8,8,8}, byteOffsets { 3, 2, 1, 0}

        // A typical Aerial Photo Image RGB
        //  (16 bits per each color, significant bits per color vary from 9bits, 10bits, 11bits, and 12bits
        //  bitsPerSample is 48=3x16, bitsPerColor { 16,16,16 }, SignificantBitsPerColor { 11,11,11 }, byteOffsets {  4, 2, 0}

        // A typical Aerial Photo Image RGBA
        //  (16 bits per each color, significant bits per color vary from 9bits, 10bits, 11bits, and 12bits
        //  bitsPerSample is 64=4x16, bitsPerColor { 16,16,16,16 }, SignificantBitsPerColor { 12,12,12,12 }, byteOffsets {  6, 4, 2, 0 }

        int reqBandCount = reqBandOrder.length;
        boolean hasAlpha = (reqBandCount == 2) || (reqBandCount == 4);

        IntBuffer imageMask = null;
        if (hasAlpha && params.hasKey(AVKey.GDAL_MASK_DATASET))
        {
            imageMask = extractImageMask(params);
        }

        if (bandDataType == gdalconstConstants.GDT_Byte)
        {
            byte[][] int8 = new byte[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                int8[i] = new byte[imgSize];
                bands[srcBandIndex].get(int8[i]);
            }

            if (hasAlpha && null != imageMask)
            {
                applyImageMask(int8[reqBandCount - 1], imageMask);
            }

            imgBuffer = new DataBufferByte(int8, imgSize);

            bufferType = DataBuffer.TYPE_BYTE;
        }
        else if (bandDataType == gdalconstConstants.GDT_Int16)
        {
            short[][] int16 = new short[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                int16[i] = new short[imgSize];
                bands[srcBandIndex].asShortBuffer().get(int16[i]);
            }

            if (hasAlpha && null != imageMask)
            {
                applyImageMask(int16[reqBandCount - 1], imageMask);
            }

            imgBuffer = new DataBufferShort(int16, imgSize);
            bufferType = DataBuffer.TYPE_SHORT;
        }
        else if (bandDataType == gdalconstConstants.GDT_Int32 || bandDataType == gdalconstConstants.GDT_UInt32)
        {
            int[][] uint32 = new int[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                uint32[i] = new int[imgSize];
                bands[srcBandIndex].asIntBuffer().get(uint32[i]);
            }
            if (hasAlpha && null != imageMask)
            {
                applyImageMask(uint32[reqBandCount - 1], imageMask);
            }

            imgBuffer = new DataBufferInt(uint32, imgSize);
            bufferType = DataBuffer.TYPE_INT;
        }
        else if (bandDataType == gdalconstConstants.GDT_UInt16)
        {

            short[][] uint16 = new short[reqBandCount][];
            for (int i = 0; i < reqBandCount; i++)
            {
                int srcBandIndex = reqBandOrder[i];
                uint16[i] = new short[imgSize];
                bands[srcBandIndex].asShortBuffer().get(uint16[i]);
            }
            if (hasAlpha && null != imageMask)
            {
                applyImageMask(uint16[reqBandCount - 1], imageMask);
            }

            imgBuffer = new DataBufferUShort(uint16, imgSize);
            bufferType = DataBuffer.TYPE_USHORT;
        }
        else
        {
            String message = Logging.getMessage("generic.UnrecognizedDataType", bandDataType);
            Logging.logger().severe(message);
        }

        SampleModel sm = new BandedSampleModel(bufferType, width, height, width, bandsOrder, offsets);
        WritableRaster raster = Raster.createWritableRaster(sm, imgBuffer, null);
        ColorModel cm;

        Band band1 = ds.GetRasterBand(1);
        if (band1.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex)
        {
            cm = band1.GetRasterColorTable().getIndexColorModel(gdal.GetDataTypeSize(bandDataType));
            img = new BufferedImage(cm, raster, false, null);
        }
        else if (band1.GetRasterColorInterpretation() == gdalconstConstants.GCI_GrayIndex && reqBandCount == 2)
        {
            int transparency = Transparency.BITMASK;
            int baseColorSpace = ColorSpace.CS_GRAY;
            ColorSpace cs = ColorSpace.getInstance(baseColorSpace);
            int[] nBits = new int[reqBandCount];
            for (int i = 0; i < reqBandCount; i++)
            {
                nBits[i] = actualBitsPerColor;
            }

            cm = new ComponentColorModel(cs, nBits, hasAlpha, false, transparency, bufferType);
            
            // Work around for
            // Bug ID: JDK-5051418 Grayscale TYPE_CUSTOM BufferedImages are rendered lighter than TYPE_BYTE_GRAY
            BufferedImage tmpImg = new BufferedImage(cm, raster, false, null);
            //keep the alpha
            img = new BufferedImage(tmpImg.getWidth(), tmpImg.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

            Raster srcRaster = tmpImg.getRaster();
            WritableRaster dstRaster = img.getRaster();
            int[] gray = null, alpha = null;
            int w = srcRaster.getWidth();
            for (int y = 0; y < tmpImg.getHeight(); y++) {
                gray = srcRaster.getSamples(0, y, w, 1, 0, gray);
                alpha = srcRaster.getSamples(0, y, w, 1, 1, alpha);

                dstRaster.setSamples(0, y, w, 1, 0, gray);
                dstRaster.setSamples(0, y, w, 1, 1, gray);
                dstRaster.setSamples(0, y, w, 1, 2, gray);
                dstRaster.setSamples(0, y, w, 1, 3, alpha);
            }
        }
        else
        {
            // Determine the color space.
            int transparency = hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
            int baseColorSpace = (reqBandCount > 2) ? ColorSpace.CS_sRGB : ColorSpace.CS_GRAY;
            ColorSpace cs = ColorSpace.getInstance(baseColorSpace);

            int[] nBits = new int[reqBandCount];
            for (int i = 0; i < reqBandCount; i++)
            {
                nBits[i] = actualBitsPerColor;
            }

            cm = new ComponentColorModel(cs, nBits, hasAlpha, false, transparency, bufferType);
            img = new BufferedImage(cm, raster, false, null);
        }

        if( null != img )
        {
            if( AVListImpl.getBooleanValue(params, AVKey.BLACK_GAPS_DETECTION, false) )
            {
                // remove voids
                img = detectVoidsAndMakeThemTransparent(img);
            }
        }

        return BufferedImageRaster.wrap(img, params);
    }

    /**
     * Attempts to detect if there are any black|white|gray voids (also called black skirts) i n the image raster caused
     * by inaccurate clipping. The algorithm checks each corner of the image and if it detects black|white|gray pixel,
     * uses a scanline version of flood fill algorithm to make the area transparent. See more
     * <code>http://en.wikipedia.org/wiki/Flood_fill#Scanline_fill</code>
     *
     * @param sourceImage a source image raster
     *
     * @return BufferedImage with voids (if detected) filled with a transparent pixel values
     */
    protected static BufferedImage detectVoidsAndMakeThemTransparent(BufferedImage sourceImage)
    {
        BufferedImage dest;

        if (sourceImage == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        if (width <= 3 || height <= 3)
        {
            // raster size is too small for the algorithm, just return the source raster
            return sourceImage;
        }

        try
        {
            // first run (creates a copy and flips vertically)
            dest = verticalFlip(sourceImage);
            scanFill(dest);
            // second run
            dest = verticalFlip(dest);
            scanFill(dest);
        }
        catch (Throwable t)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
            dest = sourceImage;
        }

        return dest;
    }

    protected static void scanFill(BufferedImage sourceImage)
    {
        if (null == sourceImage || sourceImage.getWidth() <= 3 || sourceImage.getHeight() <= 3)
            return;

        ArrayList<Integer> voids = new ArrayList<Integer>();
        voids.add(0); // a=r=g=b=0
        voids.add(0xFF << 24); // a=255, r=g=b=0
        voids.add(0xFFFFFFFF); // a=255, r=g=b=255
        voids.add(0x00FFFFFF); // a=0, r=g=b=255

        int NODATA_TRANSPARENT = 0x00000000;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        int[] scanline1 = new int[width + 2];
        int[] scanline2 = new int[width + 2];

        sourceImage.getRGB(0, 0, width, 1, scanline2, 1, width);

        // check the first pixel (in NW corner)
        int nw = 0x00FFFFFF & scanline2[1]; // ignore alpha value
        if (nw == 0x00808080) // r=g=b=128 (80H)
        {
            voids.add((0xFF << 24) | nw); // alpha=255(FFH), r=g=b=128 (80H)
            voids.add(0x00FFFFFF & nw); // alpha=0, r=g=b=128 (80H)
        }

        // check the last pixel (in NE corner)
        int ne = 0x00FFFFFF & scanline2[width]; // ignore alpha value
        if (ne == 0x00808080) // r=g=b=128 (80H)
        {
            voids.add((0xFF << 24) | ne); // alpha=255(FFH), r=g=b=128 (80H)
            voids.add(0x00FFFFFF & ne); // alpha=0, r=g=b=128 (80H)
        }

        int numVoids = voids.size();
        int[] nodata = new int[numVoids];
        for (int i = 0; i < numVoids; i++)
        {
            nodata[i] = voids.get(i);
        }

        scanline2[0] = scanline2[width + 1] = NODATA_TRANSPARENT;

        Arrays.fill(scanline1, NODATA_TRANSPARENT);

        int pixel;
        for (int h = 0; h < height; h++)
        {
            int[] scanline0 = scanline1.clone();
            scanline1 = scanline2.clone();

            if (h + 1 < height)
            {
                sourceImage.getRGB(0, h + 1, width, 1, scanline2, 1, width);
                scanline2[0] = scanline2[width + 1] = NODATA_TRANSPARENT;
            }
            else
                Arrays.fill(scanline2, NODATA_TRANSPARENT);

            for (int i = 1; i <= width; i++)
            {
                pixel = scanline1[i];

                for (int v = 0; v < numVoids; v++)
                {
                    if (pixel == nodata[v] &&
                        (scanline0[i - 1] == NODATA_TRANSPARENT || scanline0[i] == NODATA_TRANSPARENT
                            || scanline0[i + 1] == NODATA_TRANSPARENT || scanline1[i - 1] == NODATA_TRANSPARENT
                            || scanline1[i + 1] == NODATA_TRANSPARENT || scanline2[i - 1] == NODATA_TRANSPARENT
                            || scanline2[i] == NODATA_TRANSPARENT || scanline2[i + 1] == NODATA_TRANSPARENT))
                    {
                        scanline1[i] = NODATA_TRANSPARENT;
                        break;
                    }
                }
            }

            sourceImage.setRGB(0, h, width, 1, scanline1, 1, width);
        }
    }

    /**
     * Flips image raster vertically
     *
     * @param img A source raster as a BufferedImage
     *
     * @return A vertically flipped image raster as a BufferedImage
     */
    protected static BufferedImage verticalFlip(BufferedImage img)
    {
        if (null == img)
            return null;

        int w = img.getWidth();
        int h = img.getHeight();

//        BufferedImage flipImg = new BufferedImage(w, h, img.getColorModel().getTransparency() );
        BufferedImage flipImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = flipImg.createGraphics();
        java.awt.Composite prevComposite = g2d.getComposite();
        g2d.setComposite(java.awt.AlphaComposite.Src);
        g2d.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
        g2d.setComposite(prevComposite);
        g2d.dispose();
        return flipImg;
    }

    protected static void applyImageMask(byte[] alphaBand, IntBuffer maskBand)
    {
        if (null == alphaBand || null == maskBand || alphaBand.length != maskBand.capacity())
        {
            return;
        }

        int size = alphaBand.length;

        maskBand.rewind();
        for (int i = 0; i < size; i++)
        {
            long pixel = ALPHA_MASK & maskBand.get();
            if (pixel == ALPHA_MASK)
            {
                alphaBand[i] = ALPHA_TRANSPARENT;
            }
        }
        maskBand.rewind();
    }

    protected static void applyImageMask(short[] alphaBand, IntBuffer maskBand)
    {
        if (null == alphaBand || null == maskBand || alphaBand.length != maskBand.capacity())
        {
            return;
        }

        int size = alphaBand.length;

        maskBand.rewind();
        for (int i = 0; i < size; i++)
        {
            long pixel = ALPHA_MASK & maskBand.get();
            if (pixel == ALPHA_MASK)
            {
                alphaBand[i] = ALPHA_TRANSPARENT;
            }
        }
        maskBand.rewind();
    }

    protected static void applyImageMask(int[] alphaBand, IntBuffer maskBand)
    {
        if (null == alphaBand || null == maskBand || alphaBand.length != maskBand.capacity())
        {
            return;
        }

        int size = alphaBand.length;

        maskBand.rewind();
        for (int i = 0; i < size; i++)
        {
            long pixel = ALPHA_MASK & maskBand.get();
            if (pixel == ALPHA_MASK)
            {
                alphaBand[i] = ALPHA_TRANSPARENT;
            }
        }
        maskBand.rewind();
    }

    protected static IntBuffer extractImageMask(AVList params)
    {
        if (null == params || !params.hasKey(AVKey.GDAL_MASK_DATASET))
        {
            return null;
        }

        try
        {
            Object o = params.getValue(AVKey.GDAL_MASK_DATASET);
            if (o instanceof Dataset)
            {
                Dataset maskDS = (Dataset) o;

                Band maskBand = maskDS.GetRasterBand(1);
                if (null == maskBand)
                {
                    String message = Logging.getMessage("nullValue.RasterBandIsNull");
                    Logging.logger().severe(message);
                    return null;
                }

                int width = maskDS.getRasterXSize();
                int height = maskDS.getRasterYSize();

                int maskBandDataType = maskBand.getDataType();
                int maskDataSize = width * height * (gdal.GetDataTypeSize(maskBandDataType) / 8);

                ByteBuffer maskData = ByteBuffer.allocateDirect(maskDataSize);
                maskData.order(ByteOrder.nativeOrder());

                int returnVal = maskBand.ReadRaster_Direct(0, 0, maskBand.getXSize(),
                    maskBand.getYSize(), width, height, maskBandDataType, maskData);

                if (returnVal != gdalconstConstants.CE_None)
                {
                    throw new WWRuntimeException(GDALUtils.getErrorMessage());
                }

                return maskData.asIntBuffer();
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Calculates geo-transform matrix for a north-up raster
     *
     * @param sector Geographic area, a Sector
     * @param width  none-zero width of a raster
     * @param height none-zero height of a raster
     *
     * @return an array of 6 doubles that contain a geo-transform matrix
     *
     * @throws IllegalArgumentException if sector is null, or raster size is zero
     */
    public static double[] calcGetGeoTransform(Sector sector, int width, int height) throws IllegalArgumentException
    {
        if (null == sector)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (0 == width)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (0 == height)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

//        * geotransform[1] : width of pixel
//        * geotransform[4] : rotational coefficient, zero for north up images.
//        * geotransform[2] : rotational coefficient, zero for north up images.
//        * geotransform[5] : height of pixel (but negative)
//        * geotransform[0] + 0.5 * geotransform[1] + 0.5 * geotransform[2] : x offset to center of top left pixel.
//        * geotransform[3] + 0.5 * geotransform[4] + 0.5 * geotransform[5] : y offset to center of top left pixel.

        double[] gx = new double[6];

        gx[GDAL.GT_0_ORIGIN_LON] = sector.getMinLongitude().degrees;
        gx[GDAL.GT_1_PIXEL_WIDTH] = Math.abs(sector.getDeltaLonDegrees() / (double) width);
        gx[GDAL.GT_2_ROTATION_X] = 0d;
        gx[GDAL.GT_3_ORIGIN_LAT] = sector.getMaxLatitude().degrees;
        gx[GDAL.GT_4_ROTATION_Y] = 0d;
        gx[GDAL.GT_5_PIXEL_HEIGHT] = -Math.abs(sector.getDeltaLatDegrees() / (double) height);

//      correct for center of pixel vs. top left of pixel

//      GeoTransform[0] -= 0.5 * GeoTransform[1];
//      GeoTransform[0] -= 0.5 * GeoTransform[2];
//      GeoTransform[3] -= 0.5 * GeoTransform[4];
//      GeoTransform[3] -= 0.5 * GeoTransform[5];

        return gx;
    }

    public static SpatialReference createGeographicSRS() throws WWRuntimeException
    {
        if (!gdalIsAvailable.get())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        SpatialReference srs = new SpatialReference();
        srs.ImportFromProj4("+proj=latlong +datum=WGS84 +no_defs");
        return srs;
    }

    protected static LatLon getLatLonForRasterPoint(double[] gt, int x, int y, CoordinateTransformation ct)
    {
        if (!gdalIsAvailable.get())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        java.awt.geom.Point2D geoPoint = GDAL.getGeoPointForRasterPoint(gt, x, y);
        if (null == geoPoint)
        {
            return null;
        }

        double[] latlon = ct.TransformPoint(geoPoint.getX(), geoPoint.getY());
        return LatLon.fromDegrees(latlon[1] /* latitude */, latlon[0] /* longitude */);
    }

    public static AVList extractRasterParameters(Dataset ds) throws IllegalArgumentException, WWRuntimeException
    {
        return extractRasterParameters(ds, null, false);
    }

    /**
     * Extracts raster parameters to an AVList
     *
     * @param ds               A GDAL dataset
     * @param params           AVList to hold retrieved metadata, if null, a new instance will be created and returned
     *                         as a return value
     * @param quickReadingMode if quick reading mode is enabled GDAL will not spend much time on heavy calculations,
     *                         like for example calculating Min/Max for entire elevation raster
     *
     * @return AVList with retrieved metadata
     *
     * @throws IllegalArgumentException when the passed dataset is null pr emtpy, or any of the dimension is 0
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if GDAL is not available, or a dataset contains no bands
     *                                  <p/>
     *                                  The extractRasterParameters() sets next key/value pairs:
     *                                  <p/>
     *                                  AVKey.WIDTH - the maximum width of the image
     *                                  <p/>
     *                                  AVKey.HEIGHT - the maximum height of the image
     *                                  <p/>
     *                                  AVKey.COORDINATE_SYSTEM - one of the next values: AVKey.COORDINATE_SYSTEM_SCREEN
     *                                  AVKey.COORDINATE_SYSTEM_GEOGRAPHIC AVKey.COORDINATE_SYSTEM_PROJECTED
     *                                  <p/>
     *                                  AVKey.SECTOR - in case of Geographic CS, contains a regular Geographic Sector
     *                                  defined by lat/lon coordinates of corners in case of Projected CS, contains a
     *                                  bounding box of the area
     *                                  <p/>
     *                                  AVKey.COORDINATE_SYSTEM_NAME
     *                                  <p/>
     *                                  <p/>
     *                                  AVKey.PIXEL_WIDTH (Double) pixel size, UTM images usually specify 1 (1 meter);
     *                                  if missing and Geographic Coordinate System is specified will be calculated as
     *                                  LongitudeDelta/WIDTH
     *                                  <p/>
     *                                  AVKey.PIXEL_HEIGHT (Double) pixel size, UTM images usually specify 1 (1 meter);
     *                                  if missing and Geographic Coordinate System is specified will be calculated as
     *                                  LatitudeDelta/HEIGHT
     *                                  <p/>
     *                                  AVKey.ORIGIN (LatLon) specifies coordinate of the image's origin (one of the
     *                                  corners, or center) If missing, upper left corner will be set as origin
     *                                  <p/>
     *                                  AVKey.DATE_TIME (0 terminated String, length == 20) if missing, current date &
     *                                  time will be used
     *                                  <p/>
     *                                  AVKey.PIXEL_FORMAT required (valid values: AVKey.ELEVATION | AVKey.IMAGE }
     *                                  specifies weather it is a digital elevation model or image
     *                                  <p/>
     *                                  AVKey.IMAGE_COLOR_FORMAT required if AVKey.PIXEL_FORMAT is AVKey.IMAGE (valid
     *                                  values: AVKey.COLOR and AVKey.MONOCHROME)
     *                                  <p/>
     *                                  AVKey.DATA_TYPE required ( valid values: AVKey.INT16, and AVKey.FLOAT32 )
     *                                  <p/>
     *                                  AVKey.VERSION optional, if missing a default will be used "NASA World Wind"
     *                                  <p/>
     *                                  AVKey.DISPLAY_NAME, (String) optional, specifies a name of the document/image
     *                                  <p/>
     *                                  AVKey.DESCRIPTION (String) optional, for any kind of descriptions
     *                                  <p/>
     *                                  AVKey.MISSING_DATA_SIGNAL optional, set the AVKey.MISSING_DATA_SIGNAL ONLY if
     *                                  you know for sure that the specified value actually represents void (NODATA)
     *                                  areas. Elevation data usually has "-32767" (like DTED), or "-32768" like SRTM,
     *                                  but some has "0" (mostly images) and "-9999" like NED. Note! Setting "-9999" is
     *                                  very ambiguos because -9999 for elevation is valid value;
     *                                  <p/>
     *                                  AVKey.MISSING_DATA_REPLACEMENT (String type forced by spec) Most images have
     *                                  "NODATA" as "0", elevations have as "-9999", or "-32768" (sometimes "-32767")
     *                                  <p/>
     *                                  AVKey.COORDINATE_SYSTEM required, valid values AVKey.COORDINATE_SYSTEM_GEOGRAPHIC
     *                                  or AVKey.COORDINATE_SYSTEM_PROJECTED
     *                                  <p/>
     *                                  AVKey.COORDINATE_SYSTEM_NAME Optional, A name of the Coordinates System as a
     *                                  String
     *                                  <p/>
     *                                  AVKey.PROJECTION_EPSG_CODE Required; Integer; EPSG code or Projection Code If CS
     *                                  is Geodetic and EPSG code is not specified, a default WGS84 (4326) will be used
     *                                  <p/>
     *                                  AVKey.PROJECTION_DATUM  Optional, AVKey.PROJECTION_DESC   Optional,
     *                                  AVKey.PROJECTION_NAME   Optional, AVKey.PROJECTION_UNITS  Optional,
     *                                  <p/>
     *                                  AVKey.ELEVATION_UNIT Required, if AVKey.PIXEL_FORMAT = AVKey.ELEVATION, value:
     *                                  AVKey.UNIT_FOOT or AVKey.UNIT_METER (default, if not specified)
     *                                  <p/>
     *                                  AVKey.RASTER_PIXEL, optional, values: AVKey.RASTER_PIXEL_IS_AREA or
     *                                  AVKey.RASTER_PIXEL_IS_POINT if not specified, default for images is
     *                                  RASTER_PIXEL_IS_AREA, and AVKey.RASTER_PIXEL_IS_POINT for elevations
     */
    public static AVList extractRasterParameters(Dataset ds, AVList params, boolean quickReadingMode)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (null == params)
        {
            params = new AVListImpl();
        }

        if (!gdalIsAvailable.get())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().finest(message);
            throw new WWRuntimeException(message);
        }

        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().finest(message);
            throw new IllegalArgumentException(message);
        }

        int width = ds.getRasterXSize();
        if (0 >= width)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().finest(message);
            throw new IllegalArgumentException(message);
        }
        params.setValue(AVKey.WIDTH, width);

        int height = ds.getRasterYSize();
        if (0 >= height)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().finest(message);
            throw new IllegalArgumentException(message);
        }
        params.setValue(AVKey.HEIGHT, height);

        int bandCount = ds.getRasterCount();
        if (0 >= bandCount)
        {
            String message = Logging.getMessage("generic.UnexpectedBandCount", bandCount);
            Logging.logger().finest(message);
            throw new WWRuntimeException(message);
        }
        params.setValue(AVKey.NUM_BANDS, bandCount);

        Band band = ds.GetRasterBand(1);
        if (null != band)
        {
            if (band.GetOverviewCount() > 0)
            {
                params.setValue(AVKey.RASTER_HAS_OVERVIEWS, Boolean.TRUE);
            }

            int dataType = band.getDataType();

            if (dataType == gdalconst.GDT_Int16 || dataType == gdalconst.GDT_CInt16)
            {
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT16);
            }
            else if (dataType == gdalconst.GDT_Int32 || dataType == gdalconst.GDT_CInt32)
            {
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT32);
            }
            else if (dataType == gdalconst.GDT_Float32 || dataType == gdalconst.GDT_CFloat32)
            {
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
                params.setValue(AVKey.DATA_TYPE, AVKey.FLOAT32);
            }
            else if (dataType == gdalconst.GDT_Byte)
            {
                int colorInt = band.GetColorInterpretation();
                if (colorInt == gdalconst.GCI_GrayIndex && bandCount < 3) {
                    params.setValue(AVKey.IMAGE_COLOR_FORMAT, AVKey.GRAYSCALE);
                } else {
                    // if has only one band => one byte index of the palette, 216 marks voids
                    params.setValue(AVKey.IMAGE_COLOR_FORMAT, AVKey.COLOR);
                }
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT8);
            }
            else if (dataType == gdalconst.GDT_UInt16)
            {
                params.setValue(AVKey.IMAGE_COLOR_FORMAT,
                    ((bandCount >= 3) ? AVKey.COLOR : AVKey.GRAYSCALE));
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT16);
            }
            else if (dataType == gdalconst.GDT_UInt32)
            {
                params.setValue(AVKey.IMAGE_COLOR_FORMAT,
                    ((bandCount >= 3) ? AVKey.COLOR : AVKey.GRAYSCALE));
                params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                params.setValue(AVKey.DATA_TYPE, AVKey.INT32);
            }
            else
            {
                String msg = Logging.getMessage("generic.UnrecognizedDataType", dataType);
                Logging.logger().severe(msg);
                throw new WWRuntimeException(msg);
            }

            if( "GTiff".equalsIgnoreCase(ds.GetDriver().getShortName())
                && params.hasKey(AVKey.FILE)
                && AVKey.ELEVATION.equals(params.getValue(AVKey.PIXEL_FORMAT))
                && !params.hasKey(AVKey.ELEVATION_UNIT) )
            {
                GeotiffReader reader = null;
                try
                {
                    File src = (File)params.getValue(AVKey.FILE);
                    AVList tiffParams = new AVListImpl();
                    reader = new GeotiffReader(src);
                    reader.copyMetadataTo(tiffParams);

                    WWUtil.copyValues( tiffParams, params, new String[] { AVKey.ELEVATION_UNIT,
                        AVKey.ELEVATION_MIN, AVKey.ELEVATION_MAX, AVKey.MISSING_DATA_SIGNAL }, false );
                }
                catch (Throwable t)
                {
                    Logging.logger().finest(WWUtil.extractExceptionReason(t));
                }
                finally
                {
                    if( null != reader )
                        reader.dispose();
                }
            }

            extractMinMaxSampleValues(ds, band, params );

            if(      AVKey.ELEVATION.equals(params.getValue(AVKey.PIXEL_FORMAT))
                  && (     !params.hasKey(AVKey.ELEVATION_MIN)
                        || !params.hasKey(AVKey.ELEVATION_MAX)
                        || !params.hasKey(AVKey.MISSING_DATA_SIGNAL)
                     )
                  // skip this heavy calculation if the file is opened in Quick Reading Node (when checking canRead())
                  && !quickReadingMode
              )
            {
                double[] minmax = new double[2];
                band.ComputeRasterMinMax(minmax);

                if ( ElevationsUtil.isKnownMissingSignal(minmax[0]))
                {
                    params.setValue(AVKey.MISSING_DATA_SIGNAL, minmax[0]);

                    if( setNoDataValue( band, minmax[0]) )
                    {
                        band.ComputeRasterMinMax(minmax);

                        params.setValue(AVKey.ELEVATION_MIN, minmax[0]);
                        params.setValue(AVKey.ELEVATION_MAX, minmax[1]);
                    }
                }
                else
                {
                    params.setValue(AVKey.ELEVATION_MIN, minmax[0]);
                    params.setValue(AVKey.ELEVATION_MAX, minmax[1]);
                }
            }
        }

        String proj_wkt = null;

        if (params.hasKey(AVKey.SPATIAL_REFERENCE_WKT))
        {
            proj_wkt = params.getStringValue(AVKey.SPATIAL_REFERENCE_WKT);
        }

        if (WWUtil.isEmpty(proj_wkt))
        {
            proj_wkt = ds.GetProjectionRef();
        }

        if (WWUtil.isEmpty(proj_wkt))
        {
            proj_wkt = ds.GetProjection();
        }

        SpatialReference srs = null;
        if (!WWUtil.isEmpty(proj_wkt))
        {
            params.setValue(AVKey.SPATIAL_REFERENCE_WKT, proj_wkt);
            srs = new SpatialReference(proj_wkt);
        }

        double[] gt = new double[6];
        ds.GetGeoTransform(gt);

        if (gt[GDAL.GT_5_PIXEL_HEIGHT] > 0)
        {
            gt[GDAL.GT_5_PIXEL_HEIGHT] = -gt[GDAL.GT_5_PIXEL_HEIGHT];
        }

        // calculate geo-coordinates in image's native CS and Projection (these are NOT lat/lon coordinates)
        java.awt.geom.Point2D[] corners = GDAL.computeCornersFromGeotransform(gt, width, height);

        double minX = GDAL.getMinX(corners);
        double minY = GDAL.getMinY(corners);
        double maxX = GDAL.getMaxX(corners);
        double maxY = GDAL.getMaxY(corners);

        double rotX = gt[GDAL.GT_2_ROTATION_X];
        double rotY = gt[GDAL.GT_4_ROTATION_Y];
        double pixelWidth = gt[GDAL.GT_1_PIXEL_WIDTH];
        double pixelHeight = gt[GDAL.GT_5_PIXEL_HEIGHT];

        params.setValue(AVKey.PIXEL_WIDTH, pixelWidth);
        params.setValue(AVKey.PIXEL_HEIGHT, pixelHeight);

        if (minX == 0d && pixelWidth == 1d && rotX == 0d && maxY == 0d && rotY == 0d && pixelHeight == 1d)
        {
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_SCREEN);
        }
        else if (Angle.isValidLongitude(minX) && Angle.isValidLatitude(maxY)
            && Angle.isValidLongitude(maxX) && Angle.isValidLatitude(minY))
        {
            if (null == srs)
            {
                srs = createGeographicSRS();
            }
            else if (srs.IsGeographic() == 0)
            {
                String msg = Logging.getMessage("generic.UnexpectedCoordinateSystem", srs.ExportToWkt());
                Logging.logger().warning(msg);
                srs = createGeographicSRS();
            }
        }

        if (null != srs)
        {
            Sector sector = null;

            if (!params.hasKey(AVKey.SPATIAL_REFERENCE_WKT))
            {
                params.setValue(AVKey.SPATIAL_REFERENCE_WKT, srs.ExportToWkt());
            }

            if (srs.IsLocal() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);
                String msg = Logging.getMessage("generic.UnknownCoordinateSystem", proj_wkt);
                Logging.logger().severe(msg);
                return params;
//                throw new WWRuntimeException(msg);
            }

            // save area in image's native CS and Projection 
            GDAL.Area area = new GDAL.Area(srs, ds);

            if (null != area)
            {
                params.setValue(AVKey.GDAL_AREA, area);
                sector = area.getSector();
                if (null != sector)
                {
                    params.setValue(AVKey.SECTOR, sector);
                    LatLon origin = new LatLon(sector.getMaxLatitude(), sector.getMinLongitude());
                    params.setValue(AVKey.ORIGIN, origin);
                }
            }

            if (srs.IsGeographic() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
                // no need to extract anything, all parameters were extracted above
            }
            else if (srs.IsProjected() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_PROJECTED);

                // ----8><----------------------------------------------------------------------------------------
                // Example of a non-typical GDAL projection string
                //
                // PROJCS
                // [
                //      "NAD83 / Massachusetts Mainland",
                //      GEOGCS
                //      [
                //          "NAD83",
                //          DATUM
                //          [
                //              "North_American_Datum_1983",
                //              SPHEROID [ "GRS 1980", 6378137, 298.2572221010002, AUTHORITY[ "EPSG", "7019" ]],
                //              AUTHORITY [ "EPSG", "6269" ]
                //          ],
                //          PRIMEM [ "Greenwich", 0 ],
                //          UNIT [ "degree", 0.0174532925199433 ],
                //          AUTHORITY [ "EPSG", "4269" ]
                //      ],
                //      PROJECTION [ "Lambert_Conformal_Conic_2SP" ],
                //      PARAMETER [ "standard_parallel_1",42.68333333333333 ],
                //      PARAMETER["standard_parallel_2",41.71666666666667],
                //      PARAMETER["latitude_of_origin",41],
                //      PARAMETER["central_meridian",-71.5],
                //      PARAMETER["false_easting",200000],
                //      PARAMETER["false_northing",750000],
                //      UNIT [ "metre", 1, AUTHORITY [ "EPSG", "9001" ]],
                //      AUTHORITY [ "EPSG", "26986" ]
                //  ]
                // ----8><----------------------------------------------------------------------------------------

//                String projcs = srs.GetAttrValue("PROJCS");
//                String geocs = srs.GetAttrValue("PROJCS|GEOGCS");
//                String projcs_unit = srs.GetAttrValue("PROJCS|GEOGCS|UNIT");

                String projection = srs.GetAttrValue("PROJCS|PROJECTION");
                String unit = srs.GetAttrValue("PROJCS|UNIT");
                if (null != unit)
                {
                    unit = unit.toLowerCase();
                    if ("meter".equals(unit) || "meters".equals(unit) || "metre".equals(unit) || "metres".equals(unit))
                    {
                        params.setValue(AVKey.PROJECTION_UNITS, AVKey.UNIT_METER);
                    }
                    else if ("foot".equals(unit) || "feet".equals(unit))
                    {
                        params.setValue(AVKey.PROJECTION_UNITS, AVKey.UNIT_FOOT);
                    }
                    else
                    {
                        Logging.logger().warning(Logging.getMessage("generic.UnknownProjectionUnits", unit));
                    }
                }

                if (null != projection && 0 < projection.length())
                {
                    params.setValue(AVKey.PROJECTION_NAME, projection);
                }
            }
            else if (srs.IsLocal() == 1)
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_SCREEN);
            }
            else
            {
                params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);
                String msg = Logging.getMessage("generic.UnknownCoordinateSystem", proj_wkt);
                Logging.logger().severe(msg);
//                throw new WWRuntimeException(msg);
            }
        }

        if (!params.hasKey(AVKey.COORDINATE_SYSTEM))
        {
            params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_UNKNOWN);
        }

        return params;
    }

    protected static Double convertStringToDouble(String s)
    {
        return ( s == null ) ? null : WWUtil.convertStringToDouble(s);
    }

    protected static void extractMinMaxSampleValues(Dataset ds, Band band, AVList params)
    {
        if( null != ds && null != params && AVKey.ELEVATION.equals(params.getValue(AVKey.PIXEL_FORMAT)))
        {
            band = (null != band ) ? band : ds.GetRasterBand(1);

            Double[] dbls = new Double[16];

            Double minValue = convertStringToDouble(ds.GetMetadataItem("TIFFTAG_MINSAMPLEVALUE"));
            Double maxValue = convertStringToDouble(ds.GetMetadataItem("TIFFTAG_MAXSAMPLEVALUE"));

            // TODO garakl This feature is not working for GeoTiff files
//            String type = band.GetUnitType();

            if( minValue == null || maxValue == null )
            {
                band.GetMinimum(dbls);
                minValue = (null != dbls[0] ) ? dbls[0] : minValue;

                band.GetMaximum(dbls);
                maxValue = (null != dbls[0] ) ? dbls[0] : maxValue;
            }

            band.GetNoDataValue(dbls);
            Double missingSignal = (null != dbls[0])
                ? dbls[0] : convertStringToDouble(ds.GetMetadataItem("TIFFTAG_GDAL_NODATA"));

            if( ElevationsUtil.isKnownMissingSignal(minValue) )
            {
                if( missingSignal == null )
                    missingSignal = minValue;

                minValue = null;
            }

            if( null != minValue )
                params.setValue(AVKey.ELEVATION_MIN, minValue);

            if( null != maxValue )
                params.setValue(AVKey.ELEVATION_MAX, maxValue);

            if( null != missingSignal )
                params.setValue(AVKey.MISSING_DATA_SIGNAL, missingSignal );
        }
    }

    protected static boolean setNoDataValue(Band band, Double nodata)
    {
        if( null != band && null != nodata )
        {
            try
            {
                gdal.PushErrorHandler("CPLQuietErrorHandler");

                return gdalconst.CE_None == band.SetNoDataValue( nodata );
            }
            finally
            {
                gdal.PopErrorHandler();
            }
        }

        return false;
    }

    public static DataRaster composeDataRaster(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (!gdalIsAvailable.get())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        params = extractRasterParameters(ds, params, false);

        String pixelFormat = params.getStringValue(AVKey.PIXEL_FORMAT);
        if (AVKey.ELEVATION.equals(pixelFormat))
        {
            return composeNonImageDataRaster(ds, params);
        }
        else if (AVKey.IMAGE.equals(pixelFormat))
        {
            return composeImageDataRaster(ds, params);
        }
        else
        {
            String message = Logging.getMessage("generic.UnexpectedRasterType", pixelFormat);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
    }

    public static int[] extractBandOrder(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        if (!gdalIsAvailable.get())
        {
            String message = Logging.getMessage("gdal.GDALNotAvailable");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (null == params)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int[] bandsOrder = null;

        if (params.hasKey(AVKey.BANDS_ORDER))
        {
            int bandsCount = ds.getRasterCount();

            Object o = params.getValue(AVKey.BANDS_ORDER);

            if (null != o && o instanceof Integer[])
            {
                Integer[] order = (Integer[]) o;
                bandsOrder = new int[order.length];
                for (int i = 0; i < order.length; i++)
                {
                    bandsOrder[i] = order[i];
                }
            }
            else if (null != o && o instanceof int[])
            {
                bandsOrder = (int[]) o;
            }

            if (null == bandsOrder)
            {
                String message = Logging.getMessage("nullValue.BandOrderIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (0 == bandsOrder.length)
            {
                String message = Logging.getMessage("generic.BandOrderIsEmpty");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            for (int i = 0; i < bandsOrder.length; i++)
            {
                if (bandsOrder[i] < 0 || bandsOrder[i] >= bandsCount)
                {
                    String message = Logging.getMessage("generic.InvalidBandOrder", bandsOrder[i], bandsCount);
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }
            }
        }

        return bandsOrder;
    }

    /**
     * The "composeDataRaster" method creates a ByteBufferRaster from an elevation (or non-image) Dataset.
     *
     * @param ds     The GDAL dataset with data raster (expected only elevation raster); f or imagery rasters use
     *               composeImageDataRaster() method
     * @param params , The AVList with properties (usually used to force projection info or sector)
     *
     * @return ByteBufferRaster as DataRaster
     *
     * @throws IllegalArgumentException if raster parameters (height, width, sector, etc) are invalid
     * @throws WWRuntimeException       when invalid raster detected (like attempt to use the method for imagery
     *                                  raster)
     */
    protected static DataRaster composeNonImageDataRaster(Dataset ds, AVList params)
        throws IllegalArgumentException, WWRuntimeException
    {
        String pixelFormat = params.getStringValue(AVKey.PIXEL_FORMAT);
        if (!AVKey.ELEVATION.equals(pixelFormat))
        {
            String message = Logging.getMessage("generic.UnexpectedRasterType", pixelFormat);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        Object o = params.getValue(AVKey.SECTOR);
        if (null == o || !(o instanceof Sector))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }
        Sector sector = (Sector) o;

        int bandCount = ds.getRasterCount();
        // we expect here one band (elevation rasters have -32767 or -32768 in void places) data raster
        if (bandCount != 1)
        {
            String message = Logging.getMessage("generic.UnexpectedBandCount", bandCount);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        ByteOrder byteOrder = ByteOrder.nativeOrder();
        if (params.hasKey(AVKey.BYTE_ORDER))
        {
            byteOrder = AVKey.LITTLE_ENDIAN.equals(params.getStringValue(AVKey.BYTE_ORDER))
                ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        }
        else
        {
            params.setValue(AVKey.BYTE_ORDER,
                (byteOrder == ByteOrder.BIG_ENDIAN) ? AVKey.BIG_ENDIAN : AVKey.LITTLE_ENDIAN);
        }

        int width = ds.getRasterXSize();
        int height = ds.getRasterYSize();

        Band band = ds.GetRasterBand(1);
        if (null == band)
        {
            String message = Logging.getMessage("nullValue.RasterBandIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        int dataType = band.getDataType();
        int dataTypeSize = gdal.GetDataTypeSize(dataType);
        int bufferSize = width * height * (dataTypeSize / 8);

        ByteBuffer data = null;
        try
        {
            data = ByteBuffer.allocateDirect(bufferSize);
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("generic.MemoryAllocationError", bufferSize);
            Logging.logger().log(Level.SEVERE, message, t);
            throw new WWRuntimeException(message);
        }

        data.order(byteOrder);

        int returnVal = band.ReadRaster_Direct(0, 0, band.getXSize(), band.getYSize(),
            width, height, band.getDataType(), data);

        if (returnVal != gdalconstConstants.CE_None)
        {
            throw new WWRuntimeException(GDALUtils.getErrorMessage());
        }

        ByteBufferRaster   raster = new ByteBufferRaster(width, height, sector, data, params);
        ElevationsUtil.rectify( raster );
        return raster;
    }

    protected static void alterJavaLibraryPath(String newJavaLibraryPath)
        throws IllegalAccessException, NoSuchFieldException
    {
        System.setProperty(JAVA_LIBRARY_PATH, newJavaLibraryPath);

        newClassLoader = ClassLoader.class;
        fieldSysPaths = newClassLoader.getDeclaredField("sys_paths");
        if (null != fieldSysPaths)
        {
            fieldSysPaths_accessible = fieldSysPaths.isAccessible();
            if (!fieldSysPaths_accessible)
            {
                fieldSysPaths.setAccessible(true);
            }

            originalClassLoader = fieldSysPaths.get(newClassLoader);

            // Reset it to null so that whenever "System.loadLibrary" is called,
            // it will be reconstructed with the changed value.
            fieldSysPaths.set(newClassLoader, null);
        }
    }

    protected static void restoreJavaLibraryPath()
    {
        try
        {
            //Revert back the changes.
            if (null != originalClassLoader && null != fieldSysPaths)
            {
                fieldSysPaths.set(newClassLoader, originalClassLoader);
                fieldSysPaths.setAccessible(fieldSysPaths_accessible);
            }
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static Class newClassLoader = null;
    private static Object originalClassLoader = null;
    private static Field fieldSysPaths = null;
    private static boolean fieldSysPaths_accessible = false;
}