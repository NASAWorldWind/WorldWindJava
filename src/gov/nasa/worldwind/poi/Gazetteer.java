/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.poi;

import gov.nasa.worldwind.exception.*;

import java.util.List;

/**
 * Interface to gazetteers.
 *
 * @author tag
 * @version $Id: Gazetteer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Gazetteer
{
    /**
     * Find places identified in a string of free text.
     *
     * @param placeInfo a string containing the place description.
     *
     * @return the points-of-interest that match the place description.
     *
     * @throws NoItemException  if the place description cannot be matched.
     * @throws ServiceException if the lookup service is not available or invocation of it fails.
     */
    public List<PointOfInterest> findPlaces(String placeInfo) throws NoItemException, ServiceException;
}
