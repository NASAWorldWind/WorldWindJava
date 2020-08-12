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

package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.util.Logging;

/**
 * This is a package private class that contains methods of reading TIFF structures
 *
 * @author Lado Garakanidze
 * @version $Id: BaselineTiff.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class BaselineTiff
{
    public int width = Tiff.Undefined;
    public int height = Tiff.Undefined;
    public int samplesPerPixel = Tiff.Undefined;
    public int photometric = Tiff.Photometric.Undefined;
    public int rowsPerStrip = Tiff.Undefined;
    public int planarConfig = Tiff.Undefined;
    public int minSampleValue;
    public int maxSampleValue;

    public int[] sampleFormat = null;
    public int[] bitsPerSample = null;

    public String displayName = null;
    public String imageDescription = null;
    public String softwareVersion = null;
    public String dateTime = null;

    private BaselineTiff()
    {
    }

    public static BaselineTiff extract(TiffIFDEntry[] ifd, TIFFReader tiffReader)
    {
        if (null == ifd || null == tiffReader)
        {
            return null;
        }

        BaselineTiff tiff = new BaselineTiff();

        for (TiffIFDEntry entry : ifd)
        {
            try
            {
                switch (entry.tag)
                {
                    // base TIFF tags
                    case Tiff.Tag.IMAGE_WIDTH:
                        tiff.width = (int) entry.asLong();
                        break;

                    case Tiff.Tag.IMAGE_LENGTH:
                        tiff.height = (int) entry.asLong();
                        break;

                    case Tiff.Tag.DOCUMENT_NAME:
                        tiff.displayName = tiffReader.readString(entry);
                        break;

                    case Tiff.Tag.IMAGE_DESCRIPTION:
                        tiff.imageDescription = tiffReader.readString(entry);
                        break;

                    case Tiff.Tag.SOFTWARE_VERSION:
                        tiff.softwareVersion = tiffReader.readString(entry);
                        break;

                    case Tiff.Tag.DATE_TIME:
                        tiff.dateTime = tiffReader.readString(entry);
                        break;

                    case Tiff.Tag.SAMPLES_PER_PIXEL:
                        tiff.samplesPerPixel = (int) entry.asLong();
                        break;

                    case Tiff.Tag.PHOTO_INTERPRETATION:
                        tiff.photometric = (int) entry.asLong();
                        break;

                    case Tiff.Tag.ROWS_PER_STRIP:
                        tiff.rowsPerStrip = (int) entry.asLong();
                        break;

                    case Tiff.Tag.PLANAR_CONFIGURATION:
                        tiff.planarConfig = (int) entry.asLong();
                        break;

                    case Tiff.Tag.SAMPLE_FORMAT:
                        tiff.sampleFormat = entry.getShortsAsInts();
                        break;

                    case Tiff.Tag.BITS_PER_SAMPLE:
                        tiff.bitsPerSample = entry.getShortsAsInts();
                        break;

                    case Tiff.Tag.MIN_SAMPLE_VALUE:
                        tiff.minSampleValue = entry.asShort();
                        break;

                    case Tiff.Tag.MAX_SAMPLE_VALUE:
                        tiff.maxSampleValue = entry.asShort();
                        break;
                }
            }
            catch (Exception e)
            {
                Logging.logger().finest(e.toString());
            }
        }
        return tiff;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("{ ");
        sb.append("width=").append(this.width).append(", ");
        sb.append("height=").append(this.height).append(", ");
        sb.append("samplesPerPixel=").append(this.samplesPerPixel).append(", ");
        sb.append("photometric=").append(this.photometric).append(", ");
        sb.append("rowsPerStrip=").append(this.rowsPerStrip).append(", ");
        sb.append("planarConfig=").append(this.planarConfig).append(", ");

        sb.append("sampleFormat=( ");
        if (null != this.sampleFormat)
        {
            for (int i = 0; i < this.sampleFormat.length; i++)
            {
                sb.append(this.sampleFormat[i]).append(" ");
            }
        }
        else
            sb.append(" NULL ");
        sb.append("), ");

        sb.append("bitsPerSample=( ");
        if (null != this.bitsPerSample)
        {
            for (int i = 0; i < this.bitsPerSample.length; i++)
            {
                sb.append(this.bitsPerSample[i]).append(" ");
            }
        }
        else
            sb.append(" NULL ");
        sb.append("), ");

        sb.append("displayName=").append(this.displayName).append(", ");
        sb.append("imageDescription=").append(this.imageDescription).append(", ");
        sb.append("softwareVersion=").append(this.softwareVersion).append(", ");
        sb.append("dateTime=").append(this.dateTime);

        sb.append(" }");

        return sb.toString();
    }
}
