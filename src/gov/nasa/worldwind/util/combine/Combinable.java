/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.combine;

/**
 * Interface for shapes that can be combined into a complex set of contours by using boolean operations. Combinable
 * shapes implement the single method combine(CombineContext). This method is typically called by a controller that
 * operates on one or more combinable shapes and implements the boolean operation that is applied to those shapes, such
 * as {@link gov.nasa.worldwind.util.combine.ShapeCombiner}. When combine is called, the Combinable draws its contours
 * using the GLU tessellator attached to the provided CombineContext. When the CombineContext is in bounding sector
 * mode, the Combinable adds its geographic bounding sector to the CombineContext's bounding sector list.
 * <p/>
 * <h2>Drawing Contours</h2> Shapes are combined into a complex set of contours by drawing their individual contours
 * using the GLU tessellator attached to a CombineContext. A controller that implements the boolean operation configures
 * the CombineContext then calls combine on each shape. It is the responsibility of the controller to configure the GLU
 * tessellator and define the beginning and end of a polygon. Shapes define the contours and vertices that represent the
 * shape's geometry. Each vertex must be a 3-tuple in geographic coordinates ordered as (longitude, latitude, 0). The
 * winding order of each contour defines whether the contour is an exterior region or an interior region.
 * Counterclockwise contours define the outer boundaries, and clockwise contours define holes or inner boundaries.
 * Contours may be nested, but a nested contour must be oriented oppositely from the contour that contains it.
 * <p/>
 * <h2>Bounding Sector Mode</h2> CombineContext may be configured in bounding sector mode by returning true from
 * isBoundingSectorMode(). When combine is called in this mode, a shape adds its geographic bounding sector to the
 * context by calling addBoundingSector(Sector). CombineContext assumes that each shape either contributes one bounding
 * sector or does not contribute anything.
 * <p/>
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
 *                 GLU.gluTessVertex(tess, vertex, 0, vertex); // longitude,latitude,0 -> x,y,z
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