/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.poi.*;
import gov.nasa.worldwindx.applications.worldwindow.core.*;

import java.util.List;

/**
 * @author tag
 * @version $Id: YahooGazetteer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class YahooGazetteer extends AbstractFeature implements Gazetteer
{
    private Gazetteer gazetteer;

    public YahooGazetteer()
    {
        this(null);
    }

    public YahooGazetteer(Registry registry)
    {
        super("Gazeteer", Constants.FEATURE_GAZETTEER, null, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.gazetteer = new gov.nasa.worldwind.poi.YahooGazetteer();
    }

    public List<PointOfInterest> findPlaces(String placeInfo) throws NoItemException, ServiceException
    {
        return this.gazetteer.findPlaces(placeInfo);
    }
}
