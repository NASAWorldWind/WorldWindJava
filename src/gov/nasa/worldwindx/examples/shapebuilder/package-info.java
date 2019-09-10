/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
