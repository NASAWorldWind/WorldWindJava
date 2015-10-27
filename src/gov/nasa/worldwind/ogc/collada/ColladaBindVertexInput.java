/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

/**
 * Represents the COLLADA <i>bind_vertex_input</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaBindVertexInput.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaBindVertexInput extends ColladaAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaBindVertexInput(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Indicates the value of the <i>semantic</i> field.
     *
     * @return The value of the <i>semantic</i>  field, or null if the field is not set.
     */
    public String getSemantic()
    {
        return (String) this.getField("semantic");
    }

    /**
     * Indicates the value of the <i>input_semantic</i> field.
     *
     * @return The value of the <i>input_semantic</i>  field, or null if the field is not set.
     */
    public String getInputSemantic()
    {
        return (String) this.getField("input_semantic");
    }
}
