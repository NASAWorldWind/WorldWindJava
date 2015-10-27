/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * @author tag
 * @version $Id: KMLUpdateOperation.java 390 2012-02-17 01:09:57Z tgaskins $
 */
public interface KMLUpdateOperation
{
    public void applyOperation(KMLRoot operationsRoot);
}