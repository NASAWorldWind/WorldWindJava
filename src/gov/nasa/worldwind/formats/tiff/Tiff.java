/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tiff;

/**
 * @author Lado Garakanidze
 * @version $Id: Tiff.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Tiff
{
    public static final int Undefined = 0;

    public interface Type
    {
        public static final int BYTE = 1;
        public static final int ASCII = 2;
        public static final int SHORT = 3;
        public static final int LONG = 4;
        public static final int RATIONAL = 5;
        public static final int SBYTE = 6;
        public static final int UNDEFINED = 7;
        public static final int SSHORT = 8;
        public static final int SLONG = 9;
        public static final int SRATIONAL = 10;
        public static final int FLOAT = 11;
        public static final int DOUBLE = 12;
    }

    public interface Tag
    {
        // Baseline Tiff 6.0 tags...
        public static final int IMAGE_WIDTH = 256;
        public static final int IMAGE_LENGTH = 257;
        public static final int BITS_PER_SAMPLE = 258;
        public static final int COMPRESSION = 259;
        public static final int PHOTO_INTERPRETATION = 262;

        public static final int DOCUMENT_NAME = 269;
        public static final int IMAGE_DESCRIPTION = 270;
        public static final int DEVICE_MAKE = 271; // manufacturer of the scanner or video digitizer
        public static final int DEVICE_MODEL = 272; // model name/number of the scanner or video digitizer
        public static final int STRIP_OFFSETS = 273;
        public static final int ORIENTATION = 274;

        public static final int SAMPLES_PER_PIXEL = 277;
        public static final int ROWS_PER_STRIP = 278;
        public static final int STRIP_BYTE_COUNTS = 279;
        public static final int MIN_SAMPLE_VALUE = 280;
        public static final int MAX_SAMPLE_VALUE = 281;
        public static final int X_RESOLUTION = 282;
        public static final int Y_RESOLUTION = 283;
        public static final int PLANAR_CONFIGURATION = 284;
        public static final int RESOLUTION_UNIT = 296;

        public static final int SOFTWARE_VERSION = 305; // Name and release # of the software that created the image
        public static final int DATE_TIME = 306; // uses format "YYYY:MM:DD HH:MM:SS"
        public static final int ARTIST = 315;
        public static final int COPYRIGHT = 315; // same as ARTIST

        public static final int TIFF_PREDICTOR = 317;
        public static final int COLORMAP = 320;
        public static final int TILE_WIDTH = 322;
        public static final int TILE_LENGTH = 323;
        public static final int TILE_OFFSETS = 324;
        public static final int TILE_COUNTS = 325;

        // Tiff extensions...
        public static final int SAMPLE_FORMAT = 339;  // SHORT array of samplesPerPixel size
    }

    // The orientation of the image with respect to the rows and columns.
    public interface Orientation
    {
        // 1 = The 0th row represents the visual top of the image,
        // and the 0th column represents the visual left-hand side.
        public static final int Row0_IS_TOP__Col0_IS_LHS = 1;

        //2 = The 0th Row represents the visual top of the image,
        // and the 0th column represents the visual right-hand side.
        public static final int Row0_IS_TOP__Col0_IS_RHS = 2;

        //3 = The 0th row represents the visual bottom of the image,
        // and the 0th column represents the visual right-hand side.
        public static final int Row0_IS_BOTTOM__Col0_IS_RHS = 3;

        //4 = The 0th row represents the visual bottom of the image,
        // and the 0th column represents the visual left-hand side.
        public static final int Row0_IS_BOTTOM__Col0_IS_LHS = 4;

        //5 = The 0th row represents the visual left-hand side of the image,
        // and the 0th column represents the visual top.
        public static final int Row0_IS_LHS__Col0_IS_TOP = 5;

        //6 = The 0th row represents the visual right-hand side of the image,
        // and the 0th column represents the visual top.
        public static final int Row0_IS_RHS__Col0_IS_TOP = 6;

        //7 = The 0th row represents the visual right-hand side of the image,
        // and the 0th column represents the visual bottom.
        public static final int Row0_IS_RHS__Col0_IS_BOTTOM = 7;

        public static final int DEFAULT = Row0_IS_TOP__Col0_IS_LHS;
    }

    public interface BitsPerSample
    {
        public static final int MONOCHROME_BYTE = 8;
        public static final int MONOCHROME_UINT8 = 8;
        public static final int MONOCHROME_UINT16 = 16;
        public static final int ELEVATIONS_INT16 = 16;
        public static final int ELEVATIONS_FLOAT32 = 32;
        public static final int RGB = 24;
        public static final int YCbCr = 24;
        public static final int CMYK = 32;
    }

    public interface SamplesPerPixel
    {
        public static final int MONOCHROME = 1;
        public static final int RGB = 3;
        public static final int RGBA = 4;
        public static final int YCbCr = 3;
        public static final int CMYK = 4;
    }

    // The color space of the image data
    public interface Photometric
    {
        public static final int Undefined = -1;

        // 0 = WhiteIsZero
        // For bilevel and grayscale images: 0 is imaged as white.
        // 2**BitsPerSample-1 is imaged as black.
        // This is the normal value for Compression=2
        public static final int Grayscale_WhiteIsZero = 0;

        // 1 = BlackIsZero
        // For bilevel and grayscale images: 0 is imaged as black.
        // 2**BitsPerSample-1 is imaged as white.
        // If this value is specified for Compression=2, the image should display and print reversed.
        public static final int Grayscale_BlackIsZero = 1;

        // 2 = RGB
        // The RGB value of (0,0,0) represents black, (255,255,255) represents white,
        // assuming 8-bit components.
        // Note! For PlanarConfiguration=1, the components are stored in the indicated order:
        // first Red, then Green, then Blue.
        // For PlanarConfiguration = 2, the StripOffsets for the component planes are stored
        // in the indicated order: first the Red component plane StripOffsets,
        // then the Green plane StripOffsets, then the Blue plane StripOffsets.
        public static final int Color_RGB = 2;

        // 3 = Palette color
        // In this model, a color is described with a single component.
        // The value of the component is used as an index into the red, green and blue curves in
        // the ColorMap field to retrieve an RGB triplet that defines the color.
        //
        // Note!!
        // When PhotometricInterpretation=3 is used, ColorMap must be present and SamplesPerPixel must be 1.
        public static final int Color_Palette = 3;

        // 4 = Transparency Mask.
        // This means that the image is used to define an irregularly shaped region of another
        // image in the same TIFF file.
        //
        // SamplesPerPixel and BitsPerSample must be 1.
        //
        // PackBits compression is recommended.
        // The 1-bits define the interior of the region; the 0-bits define the exterior of the region.
        //
        // A reader application can use the mask to determine which parts of the image to
        // display. Main image pixels that correspond to 1-bits in the transparency mask are
        // imaged to the screen or printer, but main image pixels that correspond to 0-bits in
        // the mask are not displayed or printed.
        // The image mask is typically at a higher resolution than the main image, if the
        // main image is grayscale or color so that the edges can be sharp.
        public static final int Transparency_Mask = 4;

        public static final int CMYK = 5;

        public static final int YCbCr = 6;

        // There is no default for PhotometricInterpretation, and it is required.
    }

    public interface Compression
    {
        public static final int NONE = 1;
        public static final int LZW = 5;
        public static final int JPEG = 6;
        public static final int PACKBITS = 32773;
    }

    public interface PlanarConfiguration
    {
        // CHUNKY
        // The component values for each pixel are stored contiguously.
        // The order of the components within the pixel is specified by PhotometricInterpretation.
        // For example, for RGB data, the data is stored as RGBRGBRGB...
        public static final int CHUNKY = 1;

        // PLANAR
        // The components are stored in separate component planes.
        // The values in StripOffsets and StripByteCounts are then arranged as
        // a 2-dimensional array, with SamplesPerPixel rows and StripsPerImage columns.
        // (All of the columns for row 0 are stored first, followed by the columns of row 1, and so on.)
        //
        // PhotometricInterpretation describes the type of data stored in each component plane.
        // For example, RGB data is stored with the Red components in one component plane,
        // the Green in another, and the Blue in another.
        //
        // Note!
        // If SamplesPerPixel is 1, PlanarConfiguration is irrelevant, and need not be included.
        public static final int PLANAR = 2;

        public static final int DEFAULT = CHUNKY;
    }

    public interface ResolutionUnit
    {
        public static final int NONE = 1;
        public static final int INCH = 2;
        public static final int CENTIMETER = 3;
    }

    public interface SampleFormat
    {
        public static final int UNSIGNED = 1;
        public static final int SIGNED = 2;
        public static final int IEEEFLOAT = 3;
        public static final int UNDEFINED = 4;
    }
}
