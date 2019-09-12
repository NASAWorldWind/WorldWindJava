/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import java.util.*;

/**
 * Base class for COLLADA elements that hold parameters.
 *
 * @author pabercrombie
 * @version $Id: ColladaAbstractParamContainer.java 654 2012-06-25 04:15:52Z pabercrombie $
 */
public class ColladaAbstractParamContainer extends ColladaAbstractObject
{
    /** Named <i>newparam</i> elements in the container. */
    protected Map<String, ColladaNewParam> newParams;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected ColladaAbstractParamContainer(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Find a NewParam item by scoped ID (the sid attribute of the param).
     *
     * @param sid Id to search for.
     *
     * @return The requested parameter, or null if no such parameter can be found.
     */
    public ColladaNewParam getParam(String sid)
    {
        if (this.newParams != null)
            return this.newParams.get(sid);

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if ("newparam".equals(keyName))
        {
            ColladaNewParam param = (ColladaNewParam) value;
            String sid = (String) param.getField("sid");

            // SID is a required attribute of newparam, so should never be null. Check for null to guard against
            // malformed documents, and just ignore the parameter in this these cases.
            if (sid != null)
            {
                if (this.newParams == null)
                    this.newParams = new HashMap<String, ColladaNewParam>();

                this.newParams.put(sid, param);
            }
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
