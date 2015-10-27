/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.*;

/**
 * @author tag
 * @version $Id: PointOfInterest.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface PointOfInterest extends WWObject
{
    LatLon getLatlon();
}
