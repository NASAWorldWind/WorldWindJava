/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.WWIO;

/**
 * @author dcollins
 * @version $Id: ImageIORasterWriter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ImageIORasterWriter extends AbstractDataRasterWriter
{
    private boolean writeGeoreferenceFiles;

    public ImageIORasterWriter(boolean writeGeoreferenceFiles)
    {
        super(javax.imageio.ImageIO.getWriterMIMETypes(), getImageIOWriterSuffixes());

        this.writeGeoreferenceFiles = writeGeoreferenceFiles;
    }

    public ImageIORasterWriter()
    {
        this(true); // Enable writing georeference files by default.
    }

    public boolean isWriteGeoreferenceFiles()
    {
        return this.writeGeoreferenceFiles;
    }

    public void setWriteGeoreferenceFiles(boolean writeGeoreferenceFiles)
    {
        this.writeGeoreferenceFiles = writeGeoreferenceFiles;
    }

    protected boolean doCanWrite(DataRaster raster, String formatSuffix, java.io.File file)
    {
        return (raster != null) && (raster instanceof BufferedImageRaster);
    }

    protected void doWrite(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException
    {
        this.writeImage(raster, formatSuffix, file);

        if (this.isWriteGeoreferenceFiles())
        {
            AVList worldFileParams = new AVListImpl();
            this.initWorldFileParams(raster, worldFileParams);
            
            java.io.File dir = file.getParentFile();
            String base = WWIO.replaceSuffix(file.getName(), "");
            String suffix = WWIO.getSuffix(file.getName());
            String worldFileSuffix = this.suffixForWorldFile(suffix);

            this.writeImageMetadata(new java.io.File(dir, base + "." +  worldFileSuffix), worldFileParams);
        }
    }

    protected void writeImage(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException
    {
        BufferedImageRaster bufferedImageRaster = (BufferedImageRaster) raster;
        java.awt.image.BufferedImage image = bufferedImageRaster.getBufferedImage();
        javax.imageio.ImageIO.write(image, formatSuffix, file);
    }

    protected void writeImageMetadata(java.io.File file, AVList values) throws java.io.IOException
    {
        Sector sector = (Sector) values.getValue(AVKey.SECTOR);
        int[] size = (int[]) values.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);

        double xPixelSize = sector.getDeltaLonDegrees() / size[0];
        double yPixelSize = -sector.getDeltaLatDegrees() / size[1];
        double xCoeff = 0.0;
        double yCoeff = 0.0;
        double xLocation = sector.getMinLongitude().degrees + (xPixelSize * .5);
        double yLocation = sector.getMaxLatitude().degrees + (yPixelSize * .5);

        java.io.PrintWriter out = new java.io.PrintWriter(file);
        try
        {
            out.println(xPixelSize);
            out.println(xCoeff);
            //noinspection SuspiciousNameCombination
            out.println(yCoeff);
            //noinspection SuspiciousNameCombination
            out.println(yPixelSize);
            out.println(xLocation);
            //noinspection SuspiciousNameCombination
            out.println(yLocation);
        }
        finally
        {
            out.close();
        }
    }

    protected String suffixForWorldFile(String suffix)
    {
        int length = suffix.length();
        if (length < 2)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(suffix.charAt(0)));
        sb.append(Character.toLowerCase(suffix.charAt(length - 1)));
        sb.append("w");

        return sb.toString();
    }

    protected void initWorldFileParams(DataRaster raster, AVList worldFileParams)
    {
        int[] size = new int[2];
        size[0] = raster.getWidth();
        size[1] = raster.getHeight();
        worldFileParams.setValue(WorldFile.WORLD_FILE_IMAGE_SIZE, size);

        Sector sector = raster.getSector();
        worldFileParams.setValue(AVKey.SECTOR, sector);
    }

    private static String[] getImageIOWriterSuffixes()
    {
        java.util.Iterator<javax.imageio.spi.ImageWriterSpi> iter;
        try
        {
            iter = javax.imageio.spi.IIORegistry.getDefaultInstance().getServiceProviders(
                javax.imageio.spi.ImageWriterSpi.class, true);
        }
        catch (Exception e)
        {
            return new String[0];
        }

        java.util.Set<String> set = new java.util.HashSet<String>();
        while (iter.hasNext())
        {
            javax.imageio.spi.ImageWriterSpi spi = iter.next();
            String[] names = spi.getFileSuffixes();
            set.addAll(java.util.Arrays.asList(names));
        }

        String[] array = new String[set.size()];
        set.toArray(array);
        return array;
    }
}
