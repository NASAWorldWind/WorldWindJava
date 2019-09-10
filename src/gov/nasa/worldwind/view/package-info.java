/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/**
 * <p>
 * The view package contains implementations, and support for implementations of the {@link gov.nasa.worldwind.View}
 * interface. It contains 2 inner packages that implement specific view models, {@link gov.nasa.worldwind.view.orbit},
 * and {@link gov.nasa.worldwind.view.firstperson}.
 * </p>
 * The {@link gov.nasa.worldwind.View} interface is based on the premise that a view's position and orientation can be
 * defined as Position(latitude, longitude, elevation), heading, pitch, and roll. The
 * {@link gov.nasa.worldwind.view.ViewUtil} class provides utility methods for:
 * <ol>
 * <li>
 * Constructing modelview matrices for use in OpenGL from this basic geocentric position/orientation.
 * </li>
 * <li>
 * Constructing OpenGL projection matrices from a frustum.
 * </li>
 * <li>
 * Computing position, heading, pitch, roll values from modelview matrices.
 * </li>
 * </ol>
 * <p>
 * A {@link gov.nasa.worldwind.View} has a reference to a {@link gov.nasa.worldwind.globes.Globe}, that it gleens from
 * the {@link gov.nasa.worldwind.render.DrawContext} passed to it in its {@link gov.nasa.worldwind.View#apply} method. A
 * single {@link gov.nasa.worldwind.View} instance may not be used simultaneously on {@link gov.nasa.worldwind.Model}s
 * with different {@link gov.nasa.worldwind.globes.Globe}s.
 * </p>
 *
 */
package gov.nasa.worldwind.view;
