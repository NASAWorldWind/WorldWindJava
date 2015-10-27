/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: VPFDataType.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public enum VPFDataType
{
    NULL(VPFConstants.NULL, 0, new VPFBasicDataBufferFactory.NullDataFactory()),
    DATE_AND_TIME(VPFConstants.DATE_AND_TIME, 20, new VPFBasicDataBufferFactory.DateTimeDataFactory()),
    TRIPLET_ID(VPFConstants.TRIPLET_ID, -1, new VPFBasicDataBufferFactory.TripledIdDataFactory()),
    TEXT(VPFConstants.TEXT, 1, new VPFBasicDataBufferFactory.TextDataFactory("US-ASCII")),
    TEXT_L1(VPFConstants.TEXT_L1, 1, new VPFBasicDataBufferFactory.TextDataFactory("ISO-8859-1")),
    TEXT_L2(VPFConstants.TEXT_L2, 1, new VPFBasicDataBufferFactory.TextDataFactory("ISO_6937-2-add")),
    TEXT_L3(VPFConstants.TEXT_L3, 1, new VPFBasicDataBufferFactory.TextDataFactory("ISO-10646")),
    SHORT_INT(VPFConstants.SHORT_INT, 2, new VPFBasicDataBufferFactory.ShortDataFactory()),
    LONG_INT(VPFConstants.LONG_INT, 4, new VPFBasicDataBufferFactory.IntDataFactory()),
    SHORT_FLOAT(VPFConstants.SHORT_FLOAT, 4, new VPFBasicDataBufferFactory.FloatDataFactory()),
    LONG_FLOAT(VPFConstants.LONG_FLOAT, 8, new VPFBasicDataBufferFactory.DoubleDataFactory()),
    SHORT_COORD_2I(VPFConstants.SHORT_COORD_2I, 4, new VPFBasicDataBufferFactory.ShortVecDataFactory(2)),
    LONG_COORD_2I(VPFConstants.LONG_COORD_2I, 8, new VPFBasicDataBufferFactory.IntVecDataFactory(2)),
    SHORT_COORD_3I(VPFConstants.SHORT_COORD_3I, 6, new VPFBasicDataBufferFactory.ShortVecDataFactory(3)),
    LONG_COORD_3I(VPFConstants.LONG_COORD_3I, 12, new VPFBasicDataBufferFactory.IntVecDataFactory(3)),
    SHORT_COORD_2F(VPFConstants.SHORT_COORD_2F, 8, new VPFBasicDataBufferFactory.FloatVecDataFactory(2)),
    LONG_COORD_2F(VPFConstants.LONG_COORD_2F, 16, new VPFBasicDataBufferFactory.DoubleVecDataFactory(2)),
    SHORT_COORD_3F(VPFConstants.SHORT_COORD_3F, 12, new VPFBasicDataBufferFactory.FloatVecDataFactory(3)),
    LONG_COORD_3F(VPFConstants.LONG_COORD_3F, 24, new VPFBasicDataBufferFactory.DoubleVecDataFactory(3));

    protected String name;
    protected int length;
    protected VPFDataBufferFactory dataBufferFactory;
    private static Map<String, VPFDataType> nameRegistry;

    private VPFDataType(String name, int length, VPFDataBufferFactory dataBufferFactory)
    {
        this.name = name;
        this.length = length;
        this.dataBufferFactory = dataBufferFactory;
        register(name, this);
    }

    private static void register(String name, VPFDataType type)
    {
        if (nameRegistry == null)
            nameRegistry = new HashMap<String, VPFDataType>();

        nameRegistry.put(name, type);
    }

    public static VPFDataType fromTypeName(String name)
    {
        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (nameRegistry != null) ? nameRegistry.get(name) : null;
    }

    public String getFieldName()
    {
        return this.name;
    }

    public int getFieldLength()
    {
        return this.length;
    }

    public boolean isVariableLength()
    {
        return this.length == -1;
    }

    public VPFDataBuffer createDataBuffer(int numRows, int elementsPerRow)
    {
        if (numRows < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "numRows < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.dataBufferFactory.newDataBuffer(numRows, elementsPerRow);
    }
}
