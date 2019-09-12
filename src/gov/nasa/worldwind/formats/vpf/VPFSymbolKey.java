/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: VPFSymbolKey.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFSymbolKey implements Comparable<VPFSymbolKey>
{
    public static final VPFSymbolKey UNKNOWN_SYMBOL_KEY = new VPFSymbolKey(-1);

    protected int symbolCode;

    public VPFSymbolKey(int symbolCode)
    {
        this.symbolCode = symbolCode;
    }

    public int getSymbolCode()
    {
        return this.symbolCode;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        VPFSymbolKey that = (VPFSymbolKey) o;

        return this.symbolCode == that.symbolCode;
    }

    public int hashCode()
    {
        return this.symbolCode;
    }

    public int compareTo(VPFSymbolKey key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return (this.symbolCode < key.symbolCode) ? -1 : (this.symbolCode > key.symbolCode ? 1 : 0);
    }
}
