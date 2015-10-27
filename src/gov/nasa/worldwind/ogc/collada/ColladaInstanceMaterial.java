/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import java.util.*;

/**
 * Represents the COLLADA <i>instance_material</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaInstanceMaterial.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaInstanceMaterial extends ColladaAbstractInstance<ColladaMaterial>
{
    protected List<ColladaBindVertexInput> bindVertexInputs = new ArrayList<ColladaBindVertexInput>();

    public ColladaInstanceMaterial(String ns)
    {
        super(ns);
    }

    public String getTarget()
    {
        return (String) this.getField("target");
    }

    public String getSymbol()
    {
        return (String) this.getField("symbol");
    }

    /** Instance_material uses a "target" attribute instead of the "url" attribute used by other instance elements. */
    @Override
    public String getUrl()
    {
        return this.getTarget();
    }

    /**
     * Indicates the <i>bind_vertex_input</i> element.
     *
     * @return The bind_vertex_input elements, if present. Otherwise null.
     */
    public List<ColladaBindVertexInput> getBindVertexInputs()
    {
        return this.bindVertexInputs;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if ("bind_vertex_input".equals(keyName))
        {
            this.bindVertexInputs.add((ColladaBindVertexInput) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
