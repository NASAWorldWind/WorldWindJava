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
package gov.nasa.worldwind.util.combine;

/**
 * Interface for shapes that can be combined into a complex set of contours by using boolean operations. Combinable
 * shapes implement the single method combine(CombineContext). This method is typically called by a controller that
 * operates on one or more combinable shapes and implements the boolean operation that is applied to those shapes, such
 * as {@link gov.nasa.worldwind.util.combine.ShapeCombiner}. When combine is called, the Combinable draws its contours
 * using the GLU tessellator attached to the provided CombineContext. When the CombineContext is in bounding sector
 * mode, the Combinable adds its geographic bounding sector to the CombineContext's bounding sector list.
 * <h2>Drawing Contours</h2> Shapes are combined into a complex set of contours by drawing their individual contours
 * using the GLU tessellator attached to a CombineContext. A controller that implements the boolean operation configures
 * the CombineContext then calls combine on each shape. It is the responsibility of the controller to configure the GLU
 * tessellator and define the beginning and end of a polygon. Shapes define the contours and vertices that represent the
 * shape's geometry. Each vertex must be a 3-tuple in geographic coordinates ordered as (longitude, latitude, 0). The
 * winding order of each contour defines whether the contour is an exterior region or an interior region.
 * Counterclockwise contours define the outer boundaries, and clockwise contours define holes or inner boundaries.
 * Contours may be nested, but a nested contour must be oriented oppositely from the contour that contains it.
 * <h2>Bounding Sector Mode</h2> CombineContext may be configured in bounding sector mode by returning true from
 * isBoundingSectorMode(). When combine is called in this mode, a shape adds its geographic bounding sector to the
 * context by calling addBoundingSector(Sector). CombineContext assumes that each shape either contributes one bounding
 * sector or does not contribute anything.
 * <h2>Example Implementation</h2>
 * <pre>
 * public class CombinableSector implements Combinable
 * {
 *     protected Sector sector = Sector.fromDegrees(-10, 10, -10, 10);
 *
 *     public void combine(CombineContext cc)
 *     {
 *         if (cc.isBoundingSectorMode())
 *             this.combineBounds(cc);
 *         else
 *             this.combineContours(cc);
 *     }
 *
 *     protected void combineBounds(CombineContext cc)
 *     {
 *         cc.addBoundingSector(this.sector);
 *     }
 *
 *     protected void combineContours(CombineContext cc)
 *     {
 *         if (!cc.getSector().intersects(this.sector))
 *             return;  // the sector does not intersect the context's region of interest
 *
 *         this.doCombineContours(cc);
 *     }
 *
 *     protected void doCombineContours(CombineContext cc)
 *     {
 *         GLUtessellator tess = cc.getTessellator();
 *
 *         try
 *         {
 *             GLU.gluTessBeginContour(tess);
 *
 *             for (LatLon location : this.sector) // counter clockwise iteration of the sector's four corners
 *             {
 *                 double[] vertex = {location.longitude.degrees, location.latitude.degrees, 0};
 *                 GLU.gluTessVertex(tess, vertex, 0, vertex); // longitude,latitude,0 -&gt; x,y,z
 *             }
 *         }
 *         finally
 *         {
 *             GLU.gluTessEndContour(tess);
 *         }
 *     }
 * }
 * </pre>
 *
 * @author dcollins
 * @version $Id: Combinable.java 2411 2014-10-30 21:27:00Z dcollins $
 * @see gov.nasa.worldwind.util.combine.CombineContext
 * @see gov.nasa.worldwind.util.combine.ShapeCombiner
 */
public interface Combinable
{
    /**
     * Causes this Combinable to draw its contours using the GLU tessellator attached to the provided CombineContext.
     * When the CombineContext is in bounding sector mode, this adds the Combinable's geographic bounding sector to the
     * CombineContext's bounding sector list. See the interface documentation for more information.
     *
     * @param cc the CombineContext to be used.
     *
     * @throws java.lang.IllegalArgumentException if the CombineContext is null.
     * @see gov.nasa.worldwind.util.combine.CombineContext
     */
    void combine(CombineContext cc);
}