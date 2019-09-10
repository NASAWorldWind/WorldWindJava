/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * <p>
 * Examples of how to use multiple WorldWind globes in the same application.</p>
 *
<p>
 * Applications using multiple WorldWind windows simultaneously should instruct WorldWind to share OpenGL and other
 * resources among those windows. Most WorldWind classes are designed to be shared across {@link
 * gov.nasa.worldwind.WorldWindow} objects and will be shared automatically. But OpenGL resources are not automatically
 * shared. To share them, a reference to a previously created WorldWindow must be specified as a constructor argument
 * for subsequently created WorldWindows.</p>
 *
<p>
 * Most WorldWind {@link gov.nasa.worldwind.globes.Globe} and {@link gov.nasa.worldwind.layers.Layer} objects can be
 * shared among WorldWindows. Those that cannot be shared have an operational dependency on the WorldWindow they're
 * associated with. An example is the {@link gov.nasa.worldwind.layers.ViewControlsLayer} layer for on-screen
 * navigation. Because this layer responds to input events within a specific WorldWindow, it is not sharable. Refer to
 * the WorldWind Overview page for a list of layers that cannot be shared.</p>
 *
 *
 */
package gov.nasa.worldwindx.examples.multiwindow;
