/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: GeoSymAttributeExpression.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GeoSymAttributeExpression
{
    boolean evaluate(AVList featureAttributes);
}
