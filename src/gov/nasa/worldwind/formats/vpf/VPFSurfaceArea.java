/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;
import java.nio.IntBuffer;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: VPFSurfaceArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFSurfaceArea extends SurfacePolygon // TODO: consolidate with SurfacePolygons
{
    protected VPFFeature feature;
    protected VPFPrimitiveData primitiveData;
    protected VecBufferSequence buffer;
    protected LatLon referenceLocation;
    protected Object interiorDisplayListCacheKey = new Object();

    public VPFSurfaceArea(VPFFeature feature, VPFPrimitiveData primitiveData)
    {
        this.feature = feature;
        this.primitiveData = primitiveData;
        this.buffer = computeAreaFeatureCoords(feature, primitiveData);
        this.referenceLocation = feature.getBounds().toSector().getCentroid();
    }

    protected static VecBufferSequence computeAreaFeatureCoords(VPFFeature feature, VPFPrimitiveData primitiveData)
    {
        final int numEdges = traverseAreaEdges(feature, primitiveData, null);
        final IntBuffer edgeIds = IntBuffer.wrap(new int[numEdges]);

        traverseAreaEdges(feature, primitiveData, new EdgeListener()
        {
            public void nextEdge(int edgeId, VPFPrimitiveData.EdgeInfo edgeInfo)
            {
                edgeIds.put(edgeId);
            }
        });

        edgeIds.rewind();

        VecBufferSequence buffer = primitiveData.getPrimitiveCoords(VPFConstants.EDGE_PRIMITIVE_TABLE);
        return (VecBufferSequence) buffer.slice(edgeIds.array(), 0, numEdges);
    }

    protected interface EdgeListener
    {
        void nextEdge(int edgeId, VPFPrimitiveData.EdgeInfo edgeInfo);
    }

    protected static int traverseAreaEdges(VPFFeature feature, VPFPrimitiveData primitiveData, EdgeListener listener)
    {
        int count = 0;

        String primitiveName = feature.getFeatureClass().getPrimitiveTableName();

        for (int id : feature.getPrimitiveIds())
        {
            VPFPrimitiveData.FaceInfo faceInfo = (VPFPrimitiveData.FaceInfo) primitiveData.getPrimitiveInfo(
                primitiveName, id);

            VPFPrimitiveData.Ring outerRing = faceInfo.getOuterRing();
            count += traverseRingEdges(outerRing, primitiveData, listener);

            for (VPFPrimitiveData.Ring ring : faceInfo.getInnerRings())
            {
                count += traverseRingEdges(ring, primitiveData, listener);
            }
        }

        return count;
    }

    protected static int traverseRingEdges(VPFPrimitiveData.Ring ring, VPFPrimitiveData primitiveData,
        EdgeListener listener)
    {
        int count = 0;

        for (int edgeId : ring.edgeId)
        {
            VPFPrimitiveData.EdgeInfo edgeInfo = (VPFPrimitiveData.EdgeInfo)
                primitiveData.getPrimitiveInfo(VPFConstants.EDGE_PRIMITIVE_TABLE, edgeId);

            if (!edgeInfo.isOnTileBoundary())
            {
                if (listener != null)
                    listener.nextEdge(edgeId, edgeInfo);
                count++;
            }
        }

        return count;
    }

    protected List<Sector> computeSectors(Globe globe)
    {
        Sector s = this.feature.getBounds().toSector();
        if (s == null || s.equals(Sector.EMPTY_SECTOR))
            return null;

        return Arrays.asList(s);
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return this.buffer.getLocations();
    }

    public void setLocations(Iterable<? extends LatLon> iterable)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Position getReferencePosition()
    {
        return new Position(this.referenceLocation, 0d);
    }

    @Override
    protected void applyModelviewTransform(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Apply the geographic to surface tile coordinate transform.
        Matrix modelview = sdc.getModelviewMatrix();
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glMultMatrixd(modelview.toArray(new double[16], 0, false), 0);
    }

    @Override
    protected ShapeAttributes createActiveAttributes()
    {
        return new VPFSymbolAttributes();
    }

    protected void determineActiveGeometry(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Intentionally left blank in order to override the superclass behavior with nothing.
    }

    protected void drawInterior(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Concave shape makes no assumptions about the nature or structure of the shape's vertices. The interior is
        // treated as a potentially complex polygon, and this code will do its best to rasterize that polygon. The
        // outline is treated as a simple line loop, regardless of whether the shape's vertices actually define a
        // closed path.

        // Apply interior attributes using a reference location of (0, 0), because VPFSurfaceArea's coordinates
        // are not offset with respect to a reference location.
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        this.applyInteriorState(dc, sdc, this.getActiveAttributes(), this.getInteriorTexture(), LatLon.ZERO);

        int[] dlResource = (int[]) dc.getGpuResourceCache().get(this.interiorDisplayListCacheKey);
        if (dlResource == null)
        {
            dlResource = new int[] {gl.glGenLists(1), 1};
            gl.glNewList(dlResource[0], GL2.GL_COMPILE);
            // Tessellate the interior vertices using a reference location of (0, 0), because VPFSurfaceArea's
            // coordinates do not need to be offset with respect to a reference location.
            Integer numBytes = this.tessellateInterior(dc);
            gl.glEndList();

            if (numBytes == null)
            {
                gl.glDeleteLists(dlResource[0], dlResource[1]);
                dlResource = null;
            }
            else
            {
                dc.getGpuResourceCache().put(this.interiorDisplayListCacheKey, dlResource,
                    GpuResourceCache.DISPLAY_LISTS, numBytes);
            }
        }

        if (dlResource != null)
            gl.glCallList(dlResource[0]);
    }

    protected void drawOutline(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        this.applyOutlineState(dc, this.getActiveAttributes());

        // Edges features are not necessarily closed loops, therefore each edge must be rendered as separate line strip.
        this.buffer.bindAsVertexBuffer(dc);
        this.buffer.multiDrawArrays(dc, GL.GL_LINE_STRIP);
    }

    protected WWTexture getInteriorTexture()
    {
        if (this.getActiveAttributes().getImageSource() == null)
        {
            this.texture = null;
        }
        else if (this.texture == null
            || this.texture.getImageSource() != this.getActiveAttributes().getImageSource())
        {
            this.texture = new BasicWWTexture(this.getActiveAttributes().getImageSource(),
                ((VPFSymbolAttributes) this.getActiveAttributes()).isMipMapIconImage());
        }

        return this.texture;
    }

    //**************************************************************//
    //********************  Interior Tessellation  *****************//
    //**************************************************************//

    protected Integer tessellateInteriorVertices(GLUtessellator tess)
    {
        // Setup the winding order to correctly tessellate the outer and inner rings. The outer ring is specified
        // with a clockwise winding order, while inner rings are specified with a counter-clockwise order. Inner
        // rings are subtracted from the outer ring, producing an area with holes.
        GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NEGATIVE);
        GLU.gluTessBeginPolygon(tess, null);

        int numBytes = 0; // approximate size of the display list
        String primitiveName = this.feature.getFeatureClass().getPrimitiveTableName();

        for (int id : this.feature.getPrimitiveIds())
        {
            VPFPrimitiveData.FaceInfo faceInfo = (VPFPrimitiveData.FaceInfo) primitiveData.getPrimitiveInfo(
                primitiveName, id);

            Integer nb = this.tessellateRing(tess, faceInfo.getOuterRing());
            if (nb != null)
                numBytes += nb;

            for (VPFPrimitiveData.Ring ring : faceInfo.getInnerRings())
            {
                nb = this.tessellateRing(tess, ring);
                if (nb != null)
                    numBytes += nb;
            }
        }

        GLU.gluTessEndPolygon(tess);

        return numBytes;
    }

    protected Integer tessellateRing(GLUtessellator tess, VPFPrimitiveData.Ring ring)
    {
        GLU.gluTessBeginContour(tess);

        CompoundVecBuffer buffer = this.primitiveData.getPrimitiveCoords(VPFConstants.EDGE_PRIMITIVE_TABLE);
        int numEdges = ring.getNumEdges();
        int numBytes = 0;

        for (int i = 0; i < numEdges; i++)
        {
            VecBuffer vecBuffer = buffer.subBuffer(ring.getEdgeId(i));
            Iterable<double[]> iterable = (ring.getEdgeOrientation(i) < 0) ?
                vecBuffer.getReverseCoords(3) : vecBuffer.getCoords(3);

            for (double[] coords : iterable)
            {
                GLU.gluTessVertex(tess, coords, 0, coords);
                numBytes += 3 * 4; // 3 float coords
            }
        }

        GLU.gluTessEndContour(tess);

        return numBytes;
    }

    /**
     * Overridden to clear the shape's coordinate buffer upon an unsuccessful tessellation attempt. This ensures the
     * shape won't attempt to re-tessellate itself each frame.
     *
     * @param dc the current DrawContext.
     */
    @Override
    protected void handleUnsuccessfulInteriorTessellation(DrawContext dc)
    {
        super.handleUnsuccessfulInteriorTessellation(dc);

        // If tessellating the shape's interior was unsuccessful, we modify the shape to avoid any additional
        // tessellation attempts, and free any resources that the shape won't use.

        // Replace the shape's coordinate buffer with an empty VecBufferSequence . This ensures that any rendering
        // code won't attempt to re-tessellate this shape.
        this.buffer = VecBufferSequence.emptyVecBufferSequence(2);
        // Flag the shape as having changed, since we've replaced its coordinate buffer with an empty VecBufferSequence.
        this.onShapeChanged();
    }
}
