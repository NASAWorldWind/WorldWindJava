/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes;

/**
 * Holds a globe's configuration state. The state can be used to compare a globe's current configuration with a previous
 * configuration.
 *
 * @author tag
 * @version $Id: GlobeStateKey.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface GlobeStateKey
{
    /**
     * Indicates the globe associated with this state key.
     *
     * @return the globe associated with this state key.
     */
    Globe getGlobe();
}