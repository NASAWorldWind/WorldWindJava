/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
