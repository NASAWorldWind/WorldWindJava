/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

/**
 * Illustrates how to configure and display World Wind <code>{@link gov.nasa.worldwind.render.SurfaceShape}s</code>.
 * Surface shapes are used to visualize flat standard shapes types that follow the terrain. This illustrates how to use
 * all 7 standard surface shape types:
 * <p/>
 * <ul> <li><code>{@link gov.nasa.worldwind.render.SurfacePolygon}</code></li> <li><code>{@link
 * gov.nasa.worldwind.render.SurfaceEllipse}</code></li> <li><code>{@link gov.nasa.worldwind.render.SurfaceCircle}</code></li>
 * <li><code>{@link gov.nasa.worldwind.render.SurfaceQuad}</code></li> <li><code>{@link
 * gov.nasa.worldwind.render.SurfaceSquare}</code></li> <li><code>{@link gov.nasa.worldwind.render.SurfaceSector}</code></li>
 * <li><code>{@link gov.nasa.worldwind.render.SurfacePolyline}</code></li> </ul>
 *
 * @author dcollins
 * @version $Id: SurfaceShapes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceShapes extends DraggingShapes
{
    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Surface Shapes", AppFrame.class);
    }
}
