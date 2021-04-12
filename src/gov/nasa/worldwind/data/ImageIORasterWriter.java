/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
