/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * @author dcollins
 * @version $Id: GeoSymColumn.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymColumn
{
    private final String name;
    private String dataType;
    private String dataSize;
    private String description;
    private String codeRef;

    public GeoSymColumn(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getDataSize()
    {
        return dataSize;
    }

    public void setDataSize(String dataSize)
    {
        this.dataSize = dataSize;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getCodeRef()
    {
        return codeRef;
    }

    public void setCodeRef(String codeRef)
    {
        this.codeRef = codeRef;
    }
}
