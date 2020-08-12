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
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.formats.nitfs.NITFSImageSegment;
import gov.nasa.worldwind.formats.nitfs.NITFSRuntimeException;
import gov.nasa.worldwind.formats.nitfs.NITFSSegmentType;
import gov.nasa.worldwind.formats.nitfs.UserDefinedImageSubheader;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * @author lado
 * @version $Id: RPFImageFile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class RPFImageFile extends RPFFile
{
    private NITFSImageSegment imageSegment = null;
    private UserDefinedImageSubheader imageSubheader = null;
    private RPFFrameFileComponents rpfFrameFileComponents = null;

    public RPFFrameFileComponents getRPFFrameFileComponents()
    {
        return this.rpfFrameFileComponents;
    }

    public UserDefinedImageSubheader getImageSubheader()
    {
        return this.imageSubheader;
    }

    public NITFSImageSegment getImageSegment()
    {
        return this.imageSegment;
    }


    
    private RPFImageFile(java.io.File rpfFile) throws java.io.IOException, NITFSRuntimeException
    {
        super(rpfFile);

        this.imageSegment = (NITFSImageSegment) this.getNITFSSegment(NITFSSegmentType.IMAGE_SEGMENT);
        this.validateRPFImage();

        this.imageSubheader = this.imageSegment.getUserDefinedImageSubheader();
        this.rpfFrameFileComponents = this.imageSubheader.getRPFFrameFileComponents();
    }

    private void validateRPFImage() throws NITFSRuntimeException
    {
        if ( null == this.imageSegment )
            throw new NITFSRuntimeException("NITFSReader.ImageSegmentWasNotFound");
        if( null == this.imageSegment.getUserDefinedImageSubheader())
            throw new NITFSRuntimeException("NITFSReader.UserDefinedImageSubheaderWasNotFound");
        if( null == this.imageSegment.getUserDefinedImageSubheader().getRPFFrameFileComponents())
            throw new NITFSRuntimeException("NITFSReader.RPFFrameFileComponentsWereNotFoundInUserDefinedImageSubheader");
    }

    public int[] getImagePixelsAsArray(int[] dest, RPFImageType imageType)
    {
        //IntBuffer buffer = IntBuffer.wrap(dest);
        //this.getImagePixelsAsBuffer(buffer, imageType);
        this.getImageSegment().getImagePixelsAsArray(dest, imageType);
        return dest;
    }

    //public IntBuffer getImagePixelsAsBuffer(IntBuffer dest, RPFImageType imageType)
    //{
    //    if (null != this.imageSegment)
    //        this.imageSegment.getImagePixelsAsArray(dest, imageType);
    //    return dest;
    //}

    public BufferedImage getBufferedImage()
    {
        if (null == this.imageSegment)
            return null;

        BufferedImage bimage = new BufferedImage(
            this.getImageSegment().numSignificantCols,
            this.getImageSegment().numSignificantRows,
            BufferedImage.TYPE_INT_ARGB);

        WritableRaster raster = bimage.getRaster();
        java.awt.image.DataBufferInt dataBuffer = (java.awt.image.DataBufferInt) raster.getDataBuffer();

//        IntBuffer buffer = IntBuffer.wrap(dataBuffer.getData());
        int[] buffer = dataBuffer.getData();
        this.getImageSegment().getImagePixelsAsArray(buffer, RPFImageType.IMAGE_TYPE_ALPHA_RGB);
        return bimage;
    }

    public boolean hasTransparentAreas()
    {
        //noinspection SimplifiableIfStatement
        if(null != this.imageSegment)
            return (this.imageSegment.hasTransparentPixels() || this.imageSegment.hasMaskedSubframes());
        return false;
    }

    public static RPFImageFile load(java.io.File rpfFile) throws java.io.IOException, NITFSRuntimeException {
        return new RPFImageFile(rpfFile);
    }
}
