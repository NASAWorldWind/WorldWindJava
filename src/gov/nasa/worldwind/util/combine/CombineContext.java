/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.combine;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.glu.*;
import java.util.*;

/**
 * CombineContext provides a suitcase of state used by Combinable shapes to generate a complex set of contours by
 * applying boolean operations to one or more shapes. Instances of CombineContext are typically created and configured
 * by a controller that operates on one or more combinable shapes and implements the boolean operation that is applied
 * to those shapes, such as {@link gov.nasa.worldwind.util.combine.ShapeCombiner}. The parameters used by shapes and by
 * the controller are as follows: a globe, a minimum resolution in radians, a region of interest, and a GLU tessellator.
 * The globe is used by shapes that define geometry relative to a globe. The resolution is used to filter shape detail
 * and compute geometry for resolution independent shapes. Shape geometry outside of the region of interest may be
 * ignored, clipped, or simplified at the discretion of the shape.
 * <p>
 * CombineContext initializes its GLU tessellator according to the conventions for Combinable shapes. See the {@link
 * gov.nasa.worldwind.util.combine.Combinable} interface documentation for information on drawing Combinable contours.
 * The complex set of contours computed as a result of drawing shapes into the tessellator are collected in the
 * context's contour list. This list may be accessed by calling getContours().
 *
 * @author dcollins
 * @version $Id: CombineContext.java 2412 2014-10-30 21:32:34Z dcollins $
 */
public class CombineContext implements Disposable
{
    /**
     * Implementation of GLUtessellatorCallback that forwards GLU tessellator callbacks to protected methods on
     * CombineContext.
     */
    protected static class TessCallbackAdapter extends GLUtessellatorCallbackAdapter
    {
        /**
         * The CombineContext that receives forwarded GLU tessellator callbacks.
         */
        protected CombineContext cc;

        /**
         * Creates a new TessCallbackAdapter with a CombineContext that receives GLU tessellator callbacks sent to this
         * instance.
         *
         * @param cc the CombineContext that receives forwarded GLU tessellator callbacks.
         */
        public TessCallbackAdapter(CombineContext cc)
        {
            this.cc = cc;
        }

        /**
         * Calls CombineContext.tessBegin with the specified type.
         *
         * @param type the GL primitive type.
         */
        @Override
        public void begin(int type)
        {
            this.cc.tessBegin(type);
        }

        /**
         * Calls CombineContext.tessVertex with the specified vertexData.
         *
         * @param vertexData the caller specified vertex data.
         */
        @Override
        public void vertex(Object vertexData)
        {
            this.cc.tessVertex(vertexData);
        }

        /**
         * Calls CombineContext.tessEnd.
         */
        @Override
        public void end()
        {
            this.cc.tessEnd();
        }

        /**
         * Calls CombineContext.tessCombine with the specified arguments.
         *
         * @param coords     A three element array containing the x, y and z coordinates of the new vertex.
         * @param vertexData The caller specified vertex data of the original vertices.
         * @param weight     The coefficients of the linear combination. These weights sum to 1.
         * @param outData    A one element array that must contain the caller specified data associated with the new
         *                   vertex after this method returns.
         */
        @Override
        public void combine(double[] coords, Object[] vertexData, float[] weight, Object[] outData)
        {
            this.cc.tessCombine(coords, vertexData, weight, outData);
        }

        /**
         * Calls CombineContext.tessError with the specified errno.
         *
         * @param errno a GLU enumeration indicating the error.
         */
        @Override
        public void error(int errno)
        {
            this.cc.tessError(errno);
        }
    }

    /** The globe associated with the context. */
    protected Globe globe;
    /** A geographic sector indicating the context's region of interest. */
    protected Sector sector = Sector.FULL_SPHERE;
    /** A minimum resolution in radians used to filter shape detail and compute resolution independent geometry. */
    protected double resolution;
    /** The GLU tessellator used to draw shape contours. Initalized during construction. */
    protected GLUtessellator tess;
    /** The list of contours representing the result of a boolean operation on one or more Combinable shapes. */
    protected ContourList contours = new ContourList();
    /** The vertices of the current contour currently being assembled. Used by the tess* methods. */
    protected ArrayList<LatLon> currentContour;
    /** Indicates whether this context is currently operating in bounding sector mode. */
    protected boolean isBoundingSectorMode;
    /** The shape bounding sectors associated with this context. */
    protected ArrayList<Sector> boundingSectors = new ArrayList<Sector>();

    /**
     * Creates a new combine context with the specified globe, resolution, and the default region of interest.
     *
     * @param globe      the globe to associate with this context. Shape geometry defined relative to a globe must use
     *                   this globe to compute that geometry.
     * @param resolution the minimum resolution, in radians. Used to filter shape detail and compute geometry for
     *                   resolution independent shapes.
     *
     * @throws java.lang.IllegalArgumentException if the globe is null.
     */
    public CombineContext(Globe globe, double resolution)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        GLUtessellatorCallback cb = new TessCallbackAdapter(this); // forward GLU tessellator callbacks to tess* methods
        GLUtessellator tess = GLU.gluNewTess();
        GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, cb);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, cb);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_END, cb);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, cb);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR, cb);
        GLU.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GL.GL_TRUE);
        GLU.gluTessNormal(tess, 0, 0, 1);

        this.globe = globe;
        this.resolution = resolution;
        this.tess = tess;
    }

    /**
     * Releases the releases GLU tessellator resources associated with this context.
     */
    @Override
    public void dispose()
    {
        GLU.gluDeleteTess(this.tess);
        this.tess = null;
    }

    /**
     * Returns the globe associated with this context. Shape geometry defined relative to a globe must use this globe to
     * compute that geometry.
     *
     * @return the globe associated with this context.
     */
    public Globe getGlobe()
    {
        return this.globe;
    }

    /**
     * Specifies the globe associated with this context. Shape geometry defined relative to a globe must use this globe
     * to compute that geometry.
     *
     * @param globe the globe to associate with this context.
     *
     * @throws java.lang.IllegalArgumentException if the globe is null.
     */
    public void setGlobe(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.globe = globe;
    }

    /**
     * Returns the context's region of interest as a geographic sector. Shape geometry outside of this region may be
     * ignored, clipped, or simplified at the discretion of the shape.
     *
     * @return the geographic sector indicating the context's region of interest.
     */
    public Sector getSector()
    {
        return this.sector;
    }

    /**
     * Specifies the context's region of interest as a geographic sector. Shape geometry outside of this region may be
     * ignored, clipped, or simplified at the discretion of the shape.
     *
     * @param sector a geographic sector indicating the context's region of interest.
     *
     * @throws java.lang.IllegalArgumentException if the sector is null.
     */
    public void setSector(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
    }

    /**
     * Returns the context's minimum resolution in radians. Used to filter shape detail and compute resolution
     * independent geometry.
     *
     * @return the minimum resolution, in radians.
     */
    public double getResolution()
    {
        return this.resolution;
    }

    /**
     * Specifies the context's minimum resolution in radians. Used to filter shape detail and compute resolution
     * independent geometry.
     *
     * @param resolution the minimum resolution, in radians.
     */
    public void setResolution(double resolution)
    {
        this.resolution = resolution;
    }

    /**
     * Returns the GLU tessellator used to draw shape contours. The GLU tessellator is configured according to the
     * conventions for Combinable shapes. See the {@link gov.nasa.worldwind.util.combine.Combinable} interface
     * documentation for information on drawing Combinable contours. The complex set of contours computed as a result of
     * drawing shapes into the tessellator are collected in the context's contour list. This list may be accessed by
     * calling getContours().
     *
     * @return the GLU tessellator used to draw shape contours.
     */
    public GLUtessellator getTessellator()
    {
        return this.tess;
    }

    /**
     * Returns the list of contours representing the result of a boolean operation on one or more Combinable shapes.
     *
     * @return the list of contours associated with this context.
     */
    public ContourList getContours()
    {
        return this.contours;
    }

    /**
     * Adds the specified iterable to this context's list of contours.
     *
     * @param contour the contour to add.
     *
     * @throws java.lang.IllegalArgumentException if the contour is null.
     */
    public void addContour(Iterable<? extends LatLon> contour)
    {
        if (contour == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.contours.addContour(contour);
    }

    /**
     * Removes all entries from this context's list of contours.
     */
    public void removeAllContours()
    {
        this.contours.removeAllContours();
    }

    @SuppressWarnings("UnusedParameters")
    protected void tessBegin(int type)
    {
        this.currentContour = new ArrayList<LatLon>();
    }

    protected void tessVertex(Object vertexData)
    {
        double[] vertex = (double[]) vertexData; // longitude, latitude, 0
        double latDegrees = Angle.normalizedDegreesLatitude(vertex[1]);
        double lonDegrees = Angle.normalizedDegreesLongitude(vertex[0]);
        this.currentContour.add(LatLon.fromDegrees(latDegrees, lonDegrees));
    }

    protected void tessEnd()
    {
        this.addContour(this.currentContour);
        this.currentContour = null;
    }

    @SuppressWarnings("UnusedParameters")
    protected void tessCombine(double[] coords, Object[] vertexData, float[] weight, Object[] outData)
    {
        outData[0] = coords;
    }

    protected void tessError(int errno)
    {
        String errstr = GLUTessellatorSupport.convertGLUTessErrorToString(errno);
        String msg = Logging.getMessage("generic.ExceptionWhileTessellating", errstr);
        Logging.logger().severe(msg);
    }

    /**
     * Indicates whether this context is currently operating in bounding sector mode. When
     * Combinable.combine(CombineContext) is called in this mode, a shape adds its geographic bounding sector to the
     * context by calling addBoundingSector(Sector). See the Combinable interface documentation for more information.
     *
     * @return true if the context is currently in bounding sector mode, otherwise false.
     */
    public boolean isBoundingSectorMode()
    {
        return this.isBoundingSectorMode;
    }

    /**
     * Specifies whether this context is currently operating in bounding sector mode. When
     * Combinable.combine(CombineContext) is called in this mode, a shape adds its geographic bounding sector to the
     * context by calling addBoundingSector(Sector). See the Combinable interface documentation for more information.
     *
     * @param tf true to set the context is bounding sector mode, otherwise false.
     */
    public void setBoundingSectorMode(boolean tf)
    {
        this.isBoundingSectorMode = tf;
    }

    /**
     * Returns the shape bounding sectors associated with this context. This list is populated by Combinable shapes when
     * the context is in bounding sector mode.
     *
     * @return the shape bounding sectors associated with this context.
     *
     * @see #isBoundingSectorMode()
     */
    public List<Sector> getBoundingSectors()
    {
        return this.boundingSectors;
    }

    /**
     * Adds the specified geographic sector to this context's list of shape bounding sectors.
     *
     * @param sector the sector to add.
     *
     * @throws java.lang.IllegalArgumentException if the sector is null.
     */
    public void addBoundingSector(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.boundingSectors.add(sector);
    }

    /**
     * Removes all entries from this context's list of shape bounding sectors.
     */
    public void removeAllBoundingSectors()
    {
        this.boundingSectors.clear();
    }
}