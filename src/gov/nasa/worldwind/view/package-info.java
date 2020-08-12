/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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
