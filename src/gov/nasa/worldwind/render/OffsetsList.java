/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.util.*;

/**
 * Defines a structure to hold all the uv offsets for textures applied to a particular shape.
 *
 * @author ccrick
 * @version $Id: OffsetsList.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class OffsetsList
{
    protected Map<Integer, float[]> offsets;

    public OffsetsList()
    {
        offsets = new HashMap<Integer, float[]>();

        // set default values to zero offset
        float[] zeroOffset = {0.0f, 0.0f};
        for (int i = 0; i < 4; i++)
        {
            offsets.put(i, zeroOffset);
        }
    }

    public float[] getOffset(int index)
    {
        return offsets.get(index);
    }

    public void setOffset(int index, float uOffset, float vOffset)
    {
        float[] offsetPair = {uOffset, vOffset};
        offsets.put(index, offsetPair);
    }
}