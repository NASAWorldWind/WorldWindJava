/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import java.util.*;

/**
 * Represents the COLLADA <i>vertices</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaVertices.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaVertices extends ColladaAbstractObject
{
    /** Inputs to the vertices element. */
    protected List<ColladaInput> inputs = new ArrayList<ColladaInput>();

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaVertices(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the vertex inputs.
     *
     * @return Vertex inputs. May return an empty list, but never returns null.
     */
    public List<ColladaInput> getInputs()
    {
        return this.inputs;
    }

    /**
     * Indicates the input with the semantic "POSITION".
     *
     * @return The input labeled with semantic "POSITION", or null if no such input is set.
     */
    public ColladaInput getPositionInput()
    {
        for (ColladaInput input : this.getInputs())
        {
            if ("POSITION".equals(input.getSemantic()))
                return input;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("input"))
        {
            this.inputs.add((ColladaInput) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
