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
package gov.nasa.worldwind.render;

import com.jogamp.opengl.glu.*;
import java.nio.IntBuffer;

/**
 * @author dcollins
 * @version $Id: PolygonTessellator.java 2067 2014-06-20 20:59:29Z dcollins $
 */
public class PolygonTessellator
{
    protected static class TessCallbackAdapter extends GLUtessellatorCallbackAdapter
    {
        @Override
        public void beginData(int type, Object userData)
        {
            ((PolygonTessellator) userData).tessBegin(type);
        }

        @Override
        public void edgeFlagData(boolean boundaryEdge, Object userData)
        {
            ((PolygonTessellator) userData).tessEdgeFlag(boundaryEdge);
        }

        @Override
        public void vertexData(Object vertexData, Object userData)
        {
            ((PolygonTessellator) userData).tessVertex(vertexData);
        }

        @Override
        public void endData(Object userData)
        {
            ((PolygonTessellator) userData).tessEnd();
        }

        @Override
        public void combineData(double[] coords, Object[] vertexData, float[] weight, Object[] outData, Object userData)
        {
            ((PolygonTessellator) userData).tessCombine(coords, vertexData, weight, outData);
        }
    }

    protected boolean enabled = true;
    protected GLUtessellator tess;
    protected IntBuffer interiorIndices;
    protected IntBuffer boundaryIndices;
    protected boolean isBoundaryEdge;
    protected double[] vertexCoord = new double[3];

    public PolygonTessellator()
    {
        this.tess = GLU.gluNewTess();
        TessCallbackAdapter callback = new TessCallbackAdapter();
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_BEGIN_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_EDGE_FLAG_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_VERTEX_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_END_DATA, callback);
        GLU.gluTessCallback(this.tess, GLU.GLU_TESS_COMBINE_DATA, callback);

        this.interiorIndices = IntBuffer.allocate(10);
        this.boundaryIndices = IntBuffer.allocate(10);
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public IntBuffer getInteriorIndices()
    {
        return this.interiorIndices;
    }

    public IntBuffer getBoundaryIndices()
    {
        return this.boundaryIndices;
    }

    public void reset()
    {
        if (!this.enabled)
            return;

        this.interiorIndices.clear();
        this.boundaryIndices.clear();
    }

    public void setPolygonNormal(double x, double y, double z)
    {
        if (!this.enabled)
            return;

        GLU.gluTessNormal(this.tess, x, y, z);
    }

    public void beginPolygon()
    {
        if (!this.enabled)
            return;

        GLU.gluTessBeginPolygon(this.tess, this); // Use this as the polygon user data to enable callbacks to this instance.
    }

    public void beginContour()
    {
        if (!this.enabled)
            return;

        GLU.gluTessBeginContour(this.tess);
    }

    public void addVertex(double x, double y, double z, int index)
    {
        if (!this.enabled)
            return;

        this.vertexCoord[0] = x;
        this.vertexCoord[1] = y;
        this.vertexCoord[2] = z;

        GLU.gluTessVertex(this.tess, this.vertexCoord, 0, index); // Associate the vertex with its index in the vertex array.
    }

    public void endContour()
    {
        if (!this.enabled)
            return;

        GLU.gluTessEndContour(this.tess);
    }

    public void endPolygon()
    {
        if (!this.enabled)
            return;

        GLU.gluTessEndPolygon(this.tess);
    }

    protected void tessBegin(int type)
    {
        // Intentionally left blank.
    }

    protected void tessEdgeFlag(boolean boundaryEdge)
    {
        this.isBoundaryEdge = boundaryEdge;
    }

    protected void tessVertex(Object vertexData)
    {
        // Accumulate interior indices appropriate for use as GL_interiorIndices primitives. Based on the GLU tessellator
        // documentation we can assume that the tessellator is providing interiorIndices because it's configured with the
        // edgeFlag callback.
        int index = (Integer) vertexData;
        this.interiorIndices = this.addIndex(this.interiorIndices, index);

        // Accumulate outline indices appropriate for use as GL_boundaryIndices. The tessBoundaryEdge flag indicates whether or
        // not the triangle edge starting with the current vertex is a boundary edge.
        if ((this.boundaryIndices.position() % 2) == 1)
        {
            this.boundaryIndices = this.addIndex(this.boundaryIndices, index);
        }
        if (this.isBoundaryEdge)
        {
            this.boundaryIndices = this.addIndex(this.boundaryIndices, index);

            int interiorCount = this.interiorIndices.position();
            if (interiorCount > 0 && (interiorCount % 3) == 0)
            {
                int firstTriIndex = this.interiorIndices.get(interiorCount - 3);
                this.boundaryIndices = this.addIndex(this.boundaryIndices, firstTriIndex);
            }
        }

    }

    protected void tessEnd()
    {
        // Intentionally left blank.
    }

    protected void tessCombine(double[] coords, Object[] vertexData, float[] weight, Object[] outData)
    {
        // TODO: Implement the combine callback to handle complex polygons.
    }

    protected IntBuffer addIndex(IntBuffer buffer, int index)
    {
        if (!buffer.hasRemaining())
        {
            int newCapacity = buffer.capacity() + buffer.capacity() / 2; // increase capacity by 50%
            IntBuffer newBuffer = IntBuffer.allocate(newCapacity);
            newBuffer.put((IntBuffer) buffer.flip());
            return newBuffer.put(index);
        }
        else
        {
            return buffer.put(index);
        }
    }
}
