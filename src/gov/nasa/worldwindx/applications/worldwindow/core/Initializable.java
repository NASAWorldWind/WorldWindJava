/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core;

/**
 * @author tag
 * @version $Id: Initializable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface Initializable
{
    void initialize(Controller controller);

    boolean isInitialized();
}
