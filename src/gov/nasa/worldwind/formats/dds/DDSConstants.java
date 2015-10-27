/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.dds;

/**
 * Documentation on Direct3D format constants is available at
 * http://msdn.microsoft.com/en-us/library/bb172558(VS.85).aspx.
 *
 * @author dcollins
 * @version $Id: DDSConstants.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class DDSConstants
{
    public static final int DDS_SIGNATURE_SIZE = 4;
    public static final int DDS_HEADER_SIZE = 124;
    public static final int DDS_PIXEL_FORMAT_SIZE = 32;
    public static final int DDS_PIXEL_FORMAT_OFFSET = 76;

    public static final int DDS_DATA_OFFSET = DDS_SIGNATURE_SIZE + DDS_HEADER_SIZE;


    public static final int DDPF_FOURCC = 0x0004;
    public static final int DDSCAPS_TEXTURE = 0x1000;
    public static final int DDSD_CAPS = 0x0001;
    public static final int DDSD_HEIGHT = 0x0002;
    public static final int DDSD_WIDTH = 0x0004;
    public static final int DDSD_PIXELFORMAT = 0x1000;
    public static final int DDSD_MIPMAPCOUNT = 0x20000;
    public static final int DDSD_LINEARSIZE = 0x80000;

    public static final int D3DFMT_DXT1 = makeFourCC('D', 'X', 'T', '1');
    public static final int D3DFMT_DXT2 = makeFourCC('D', 'X', 'T', '2');
    public static final int D3DFMT_DXT3 = makeFourCC('D', 'X', 'T', '3');
    public static final int D3DFMT_DXT4 = makeFourCC('D', 'X', 'T', '4');
    public static final int D3DFMT_DXT5 = makeFourCC('D', 'X', 'T', '5');

    // A DWORD (magic number) containing the four character code value 'DDS ' (0x20534444)
    public static final int MAGIC = makeFourCC('D', 'D', 'S', ' ');

    public static int makeFourCC(char ch0, char ch1, char ch2, char ch3)
    {
        return (((int) ch0))
               | (((int) ch1) << 8)
               | (((int) ch2) << 16)
               | (((int) ch3) << 24);
    }
}
