/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

/**
 * @author dcollins
 * @version $Id: Base34Converter.java 1171 2013-02-11 21:45:02Z dcollins $
 */
class Base34Converter
{
    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    private static final char[] BASE34_ALPHABET ={
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    public static char[] valueOf(int i, char[] dest, int offset, int count)
    {
        if (dest == null)
        {
            throw new IllegalArgumentException();
        }
        if (offset < 0 || count < 0 || (offset + count) >= dest.length)
        {
            throw new IndexOutOfBoundsException();
        }

        for (int digit = count + offset - 1; digit >= offset; digit--)
        {
            dest[digit] = BASE34_ALPHABET[i % 34];
            i /= 34;
        }
        return dest;
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    public static int parseChars(char[] src, int offset, int count)
    {
        if (src == null)
        {
            throw new IllegalArgumentException();
        }
        if (offset < 0 || count < 0 || (offset + count) >= src.length)
        {
            throw new IndexOutOfBoundsException();
        }

        int i = 0;
        for (int digit = offset; digit < offset + count; digit++)
        {
            int index;
            char charUpper = Character.toUpperCase(src[digit]);
            if (charUpper >= '0' && charUpper <= '9')
                index = charUpper - '0';
            else if (charUpper >= 'A' && charUpper <= 'H')
                index = 10 + charUpper - 'A';
            else if (charUpper >= 'J' && charUpper <= 'N')
                index = 18 + charUpper - 'J';
            else if (charUpper >= 'P' && charUpper <= 'Z')
                index = 23 + charUpper - 'P';
            else
            {
                    throw new IllegalArgumentException();
            }
            i = (i * 34) + index;
        }
        return i;
    }

    public static boolean isBase34(char[] src, int offset, int count)
    {
        if (src == null)
        {
            throw new IllegalArgumentException();
        }
        if (offset < 0 || count < 0 || (offset + count) >= src.length)
        {
            throw new IndexOutOfBoundsException();
        }

        for (int digit = offset; digit < offset + count; digit++)
        {
            char charUpper = Character.toUpperCase(src[digit]);
            if (!(charUpper >= '0' && charUpper <= '9')
                && !(charUpper >= 'A' && charUpper <= 'H')
                && !(charUpper >= 'J' && charUpper <= 'N')
                && !(charUpper >= 'P' && charUpper <= 'Z'))
                return false;
        }
        return true;
    }
}
