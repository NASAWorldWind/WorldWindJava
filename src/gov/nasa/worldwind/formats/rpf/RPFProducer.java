/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: RPFProducer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public enum RPFProducer
{
    /* [Section 5.2.1, MIL-STD-2411-1] */
    PRODUCER_1('1', "AFACC", "Air Force Air Combat Command"),
    PRODUCER_2('2', "AFESC", "Air Force Electronic Systems Center"),
    PRODUCER_3('3', "NIMA", "National Imagery and Mapping Agency, Primary"),
    PRODUCER_4('4', "NIMA1", "NIMA, Alternate Site 1"),
    PRODUCER_5('5', "NIMA2", "NIMA, Alternate Site 2"),
    PRODUCER_6('6', "NIMA3", "NIMA, Alternate Site 3"),
    PRODUCER_7('7', "SOCAF", "Air Force Special Operations Command"),
    PRODUCER_8('8', "SOCOM", "United States Special Operations Command"),
    PRODUCER_9('9', "PACAF", "Pacific Air Forces"),
    PRODUCER_A('A', "USAFE", "United States Air Force, Europe"),
    PRODUCER_B('B', "Non-DoD (NonDD)", "US producer outside the Department of Defense"),
    PRODUCER_C('C', "Non-US (NonUS)", "Non-US producer"),
    PRODUCER_D('D', "NIMA", "DCHUM (DCHUM) NIMA produced Digital CHUM file"),
    PRODUCER_E('E', "Non-NIMA DCHUM (DCHMD)", "DoD producer of Digital CHUM file otherthan NIMA "),
    PRODUCER_F('F', "Non-US DCHUM (DCHMF)", "Non-US (foreign)producer of Digital CHUMfiles"),
    PRODUCER_G('G', "Non-DoD DCHUM (DCHMG)", "US producer of Digital CHUM files outsideDoD"),
    PRODUCER_H('H', "IMG2RPF", "Non-specified, Imagery formatted to RPF"),
//  PRODUCER_?('I'-'Z', "",                   "Reserved for future standardization"),
    ;

    public final Character id;
    public final String producerCode;
    public final String producer;

    private RPFProducer(Character id, String producerCode, String producer)
    {
        this.id = id;
        this.producer = producer;
        this.producerCode = producerCode;
    }

    private static RPFProducer[] enumConstantAlphabet = null;

    private static synchronized RPFProducer[] enumConstantAlphabet()
    {
        if (enumConstantAlphabet == null)
        {
            RPFProducer[] universe = RPFProducer.class.getEnumConstants();
            enumConstantAlphabet = new RPFProducer[36];
            for (RPFProducer producer : universe)
            {
                enumConstantAlphabet[indexFor(producer.id)] = producer;
            }
        }
        return enumConstantAlphabet;
    }

    private static int indexFor(Character id)
    {
        if (id >= '0' && id <= '9')
            return id - '0';
        else if (id >= 'A' && id <= 'Z')
            return 10 + id - 'A';
        return -1;
    }

    public static boolean isProducerId(Character id)
    {
        if (id == null)
        {
            String message = Logging.getMessage("nullValue.CharacterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        RPFProducer[] alphabet = enumConstantAlphabet();
        int index = indexFor(Character.toUpperCase(id));
        return (index >= 0) && (index < alphabet.length) && (alphabet[index] != null);
    }

    public static RPFProducer producerFor(Character id)
    {
        if (id == null)
        {
            String message = Logging.getMessage("nullValue.CharacterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        RPFProducer producer;
        RPFProducer[] alphabet = enumConstantAlphabet();
        int index = indexFor(Character.toUpperCase(id));
        if (index < 0 || index >= alphabet.length || (producer = alphabet[index]) == null)
        {
            String message = Logging.getMessage("generic.EnumNotFound", id);
            Logging.logger().severe(message);
            throw new EnumConstantNotPresentException(RPFZone.class, message);
        }
        return producer;
    }
}
