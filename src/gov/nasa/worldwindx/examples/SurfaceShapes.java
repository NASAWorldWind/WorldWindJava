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
package gov.nasa.worldwindx.examples;

/**
 * Illustrates how to configure and display WorldWind <code>{@link gov.nasa.worldwind.render.SurfaceShape}s</code>.
 * Surface shapes are used to visualize flat standard shapes types that follow the terrain. This illustrates how to use
 * all 7 standard surface shape types:
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
        ApplicationTemplate.start("WorldWind Surface Shapes", AppFrame.class);
    }
}
