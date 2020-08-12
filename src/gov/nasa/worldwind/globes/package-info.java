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
 * Provides classes for representing the shape and terrain of a planet. A {@link gov.nasa.worldwind.globes.Globe} is
 * part of a {@link gov.nasa.worldwind.Model}, and provides methods for converting between geographic and Cartesian
 * coordinates. {@link gov.nasa.worldwind.globes.EllipsoidalGlobe} captures features of globes modelled with an
 * ellipsoid, and {@link gov.nasa.worldwind.globes.Earth} is an ellipsoidal model of the Earth using the <a
 * href="http://en.wikipedia.org/wiki/World_Geodetic_System" target="_blank">World Geodetic System</a> (WGS84).
 * {@link gov.nasa.worldwind.globes.FlatGlobe} represents a globe projected onto a plane.</p>
 *
 */
package gov.nasa.worldwind.globes;
