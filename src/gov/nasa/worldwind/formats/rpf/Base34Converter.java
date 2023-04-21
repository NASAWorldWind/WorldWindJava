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

import gov.nasa.worldwind.util.Logging;

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
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (offset < 0 || count < 0 || (offset + count) >= dest.length)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().fine(message);
            throw new IndexOutOfBoundsException(message);
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
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (offset < 0 || count < 0 || (offset + count) >= src.length)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().fine(message);
            throw new IndexOutOfBoundsException(message);
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
                String message = Logging.getMessage("Base34Converter.Base34Error");
                Logging.logger().fine(message);
                throw new IllegalArgumentException(message);
            }
            i = (i * 34) + index;
        }
        return i;
    }

    public static boolean isBase34(char[] src, int offset, int count)
    {
        if (src == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (offset < 0 || count < 0 || (offset + count) >= src.length)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().fine(message);
            throw new IndexOutOfBoundsException(message);
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
