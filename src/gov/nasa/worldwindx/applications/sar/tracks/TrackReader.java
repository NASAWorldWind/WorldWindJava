/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.applications.sar.tracks;

import gov.nasa.worldwind.tracks.Track;

/**
 * @author dcollins
 * @version $Id: TrackReader.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface TrackReader
{
    String getDescription();

    boolean canRead(Object source);

    Track[] read(Object source);
}
