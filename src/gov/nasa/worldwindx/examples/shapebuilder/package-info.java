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
 * The shapebuilder package contains shape building applications for 3D shapes, including the rigid shapes ({@link
 * gov.nasa.worldwind.render.Ellipsoid}, {@link gov.nasa.worldwind.render.Box}, {@link
 * gov.nasa.worldwind.render.Cylinder}, {@link gov.nasa.worldwind.render.Cone}, {@link
 * gov.nasa.worldwind.render.Pyramid}, {@link gov.nasa.worldwind.render.Wedge}) and {@link
 * gov.nasa.worldwind.render.ExtrudedPolygon}. </p>
 *
<p>
 * These shape builders rely on a set of editor classes to provide specific editing functionality for each particular
 * type of shape. Each of these editors is descended from the {@link
 * gov.nasa.worldwindx.examples.shapebuilder.AbstractShapeEditor} abstract class, and the editors for the rigid shapes
 * extend further from {@link gov.nasa.worldwindx.examples.shapebuilder.RigidShapeEditor}. </p>
 *
<p>
 * The main shape building application in the package is {@link
 * gov.nasa.worldwindx.examples.shapebuilder.RigidShapeBuilder}. This example allows the user to select the desired
 * shape from a dropdown menu, create an instance of it with the click of a button, and specify an "edit mode" for
 * modifying the shape: move, scale, rotate or skew. Numerous shapes may be created and placed on the globe together,
 * but only one may be selected and edited at any given time. </p>
 *
<p>
 * Under the hood, the editors work by affixing control points or other affordances to the edited shape that allow the
 * user to interactively adjust parameters of the shape such as width, height, heading, altitude, etc. Each control
 * point is associated with a particular action, and specific implementations of this action can be found in the
 * different editors for the different shapes: {@link gov.nasa.worldwindx.examples.shapebuilder.BoxEditor}, {@link
 * gov.nasa.worldwindx.examples.shapebuilder.CylinderEditor}, {@link
 * gov.nasa.worldwindx.examples.shapebuilder.ConeEditor}, {@link
 * gov.nasa.worldwindx.examples.shapebuilder.PyramidEditor}, {@link
 * gov.nasa.worldwindx.examples.shapebuilder.WedgeEditor}, and {@link
 * gov.nasa.worldwindx.examples.shapebuilder.ExtrudedPolygonEditor}. </p>
 *
<p>
 * The following actions can be performed on all shapes except {@link gov.nasa.worldwind.render.ExtrudedPolygon}: </p>
 *
<p>
 * <b>Move:</b> Select and drag the arrows to constrain movement to latitude, longitude, or altitude only. Select the
 * body of the shape to move it freely.
 *
 * <p>
 * <b>Scale:</b> Select and drag the red control points to scale along one axis only. Select and drag the blue control
 * point to scale the entire base. Select and drag the body of the shape to scale evenly along all axes at once. </p>
 *
<p>
 * <b>Rotate:</b> Select one of the three discs and drag clockwise or counter-clockwise to rotate the shape in that
 * plane. </p>
 *
<p>
 * <b>Skew:</b> Select and drag the control points to skew the shape either along the east-west axis or the north-south
 * axis. </p>
 *
<p>
 * Keyboard shortcuts allow the user to toggle easily between the various edit modes. These shortcuts are as follows:
 * <ul>
 * <li>
 * Ctrl-Z: &nbsp; &nbsp; move
 * </li>
 * <li>
 * Ctrl-X: &nbsp; &nbsp; scale
 * </li>
 * <li>
 * Ctrl-C: &nbsp; &nbsp; rotate
 * </li>
 * <li>
 * Ctrl-V: &nbsp; &nbsp; skew
 * </li>
 * </ul>
 * <p>
 * <b>Annotations:</b>Annotations appear during editing to help guide the user in positioning, sizing, and orienting the
 * shapes. </p>
 *
 */
package gov.nasa.worldwindx.examples.shapebuilder;
