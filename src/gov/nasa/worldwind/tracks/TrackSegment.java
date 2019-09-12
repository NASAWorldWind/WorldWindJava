/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.tracks;

/**
 * @author tag
 * @version $Id: TrackSegment.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface TrackSegment
{
    java.util.List<TrackPoint> getPoints();
}
