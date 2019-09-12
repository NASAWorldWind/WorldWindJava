/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tiff;

import gov.nasa.worldwind.util.Logging;

import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Lado Garakanidze
 * @version $Id: TIFFIFDFactory.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class TIFFIFDFactory
{
    public static int MASK_USHORT = 0xFFFF;
    public static long MASK_UINT = 0xFFFFFFFFL;

    private TIFFIFDFactory()
    {

    }

    public static TiffIFDEntry create(FileChannel fc, ByteOrder tiffFileOrder)
    {
        if( null == fc )
            return null;

        long savedPosition = 0;


        ByteBuffer header = ByteBuffer.wrap(new byte[12]).order( tiffFileOrder );

        try
        {
            fc.read( header );
            header.flip();

            int tag = getUnsignedShort( header );
            int type = getUnsignedShort( header );
            long count = getUnsignedInt( header );


            // To save time and space the Value Offset contains the Value instead of pointing to
            // the Value if and only if the Value fits into 4 bytes. If the Value is shorter than 4 bytes,
            // it is left-justified within the 4-byte Value Offset, i.e., stored in the lowernumbered bytes.
            // Whether the Value fits within 4 bytes is determined by the Type and Count of the field.

            if ( type == Tiff.Type.SHORT && count == 1 )
            {
                // these get packed left-justified in the bytes...
                int upper = getUnsignedShort( header );
                int lower = getUnsignedShort( header );
                long value = (MASK_USHORT & upper) << 16 | (MASK_USHORT & lower);

                return new TiffIFDEntry(tag, type, value );
            }
            else if( count == 1 && (type == Tiff.Type.LONG || type == Tiff.Type.FLOAT))
            {
                long value = header.getInt();
                return new TiffIFDEntry(tag, type, value );
            }
            else
            {
                long offset = getUnsignedInt( header );
                int size = MASK_USHORT & (int)calcSize( type, count );

                if( size > 0L )
                {
                    ByteBuffer data = ByteBuffer.allocateDirect( size ).order( tiffFileOrder );
                    savedPosition = fc.position();
                    fc.position( offset );
                    fc.read( data );
                    data.flip();

                    fc.position( savedPosition );
                    savedPosition = 0;

                    return new TiffIFDEntry(tag, type, count, offset, data );
                }
                else
                    return new TiffIFDEntry(tag, type, count, offset );
            }
        }
        catch(Exception e)
        {
            Logging.logger().finest( e.getMessage() );

        }
        finally
        {
            if( savedPosition != 0 && fc != null )
            {
                try
                {
                    fc.position( savedPosition );
                }
                catch(Exception e2)
                {
                    Logging.logger().finest( e2.getMessage() );
                }
            }
        }

        return null;
    }


    private static long calcSize(int type, long count)
    {
        switch( type )
        {
            case Tiff.Type.BYTE:
            case Tiff.Type.SBYTE:
            case Tiff.Type.ASCII:
                return count;

            case Tiff.Type.SHORT:
            case Tiff.Type.SSHORT:
                return count * 2L;

            case Tiff.Type.LONG:
            case Tiff.Type.SLONG:
                return count * 4L;

            case Tiff.Type.FLOAT:
                return count * 4L;

            case Tiff.Type.DOUBLE:
                return count * 8L;

            case Tiff.Type.RATIONAL:
            case Tiff.Type.SRATIONAL:
                return count * 8L;

            case Tiff.Type.UNDEFINED:
            default:
                return 0;
        }
    }

    private static int getUnsignedShort(ByteBuffer bb)
    {
        return MASK_USHORT & (int) bb.getShort();
    }

    private static long getUnsignedInt(ByteBuffer bb)
    {
        return MASK_UINT & bb.getInt();
    }

}
