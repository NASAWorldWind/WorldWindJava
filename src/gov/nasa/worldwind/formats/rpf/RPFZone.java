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
 * @version $Id: RPFZone.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public enum RPFZone
{
    /* [Table III, Section 70, MIL-A-89007] */
    ZONE_1('1'),
    ZONE_2('2'),
    ZONE_3('3'),
    ZONE_4('4'),
    ZONE_5('5'),
    ZONE_6('6'),
    ZONE_7('7'),
    ZONE_8('8'),
    ZONE_9('9'),
    ZONE_A('A'),
    ZONE_B('B'),
    ZONE_C('C'),
    ZONE_D('D'),
    ZONE_E('E'),
    ZONE_F('F'),
    ZONE_G('G'),
    ZONE_H('H'),
    ZONE_J('J'),
    ;

    public final char zoneCode;

    private RPFZone(char zoneCode)
    {
        this.zoneCode = zoneCode;
    }

    public static boolean isZoneCode(char c)
    {
        char upperChar = Character.toUpperCase(c);
        return ((upperChar >= '1' && upperChar <= '9')
                || (upperChar >= 'A' && upperChar <= 'H')
                || (upperChar == 'J'));
    }

    static int indexFor(char zoneCode)
    {
        final int NUM_START_INDEX = 0;
        final int ALPHA_START_INDEX = 9;

        int index = -1;
        char upperChar = Character.toUpperCase(zoneCode);
        if (upperChar >= '1' && upperChar <= '9')
        {
            index = NUM_START_INDEX + upperChar - '1';
        }
        else if (upperChar >= 'A' && upperChar <= 'H')
        {
            index = ALPHA_START_INDEX + upperChar - 'A';
        }
        else if (upperChar == 'J')
        {
            index = ALPHA_START_INDEX + upperChar - 'A' - 1;
        }

        return index;
    }

    static boolean isZoneInUpperHemisphere(char zoneCode)
    {
        char upperChar = Character.toUpperCase(zoneCode);
        return (upperChar >= '1' && upperChar <= '9');
    }

    static boolean isPolarZone(char zoneCode)
    {
        char upperChar = Character.toUpperCase(zoneCode);
        return (upperChar == '9') || (upperChar == 'J');
    }

    public static RPFZone zoneFor(char zoneCode)
    {
        RPFZone[] alphabet = enumConstantAlphabet();
        int index = indexFor(zoneCode);
        if (index < 0 || index >= alphabet.length)
        {
            String message = Logging.getMessage("generic.EnumNotFound", zoneCode);
            Logging.logger().fine(message);
            throw new EnumConstantNotPresentException(RPFZone.class, message);
        }

        RPFZone zone = alphabet[index];
        if (zone == null)
        {
            String message = Logging.getMessage("generic.EnumNotFound", zoneCode);
            Logging.logger().fine(message);
            throw new EnumConstantNotPresentException(RPFZone.class, message);
        }

        return zone;
    }

    private static RPFZone[] enumConstantAlphabet = null;

    private static synchronized RPFZone[] enumConstantAlphabet()
    {
        if (enumConstantAlphabet == null)
        {
            RPFZone[] universe = RPFZone.class.getEnumConstants();
            enumConstantAlphabet = new RPFZone[universe.length];
            for (RPFZone zone : universe)
            {
                enumConstantAlphabet[indexFor(zone.zoneCode)] = zone;
            }
        }
        return enumConstantAlphabet;
    }
}
