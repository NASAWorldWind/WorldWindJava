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
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: VPFPrimitiveData.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFPrimitiveData
{
    public interface PrimitiveInfo
    {
        VPFBoundingBox getBounds();
    }

    public static class BasicPrimitiveInfo implements PrimitiveInfo
    {
        protected VPFBoundingBox bounds;

        public BasicPrimitiveInfo(VPFBoundingBox bounds)
        {
            this.bounds = bounds;
        }

        public VPFBoundingBox getBounds()
        {
            return this.bounds;
        }
    }

    public static class EdgeInfo extends BasicPrimitiveInfo
    {
        protected int edgeType;
        protected int startNode;
        protected int endNode;
        protected int leftFace;
        protected int rightFace;
        protected int leftEdge;
        protected int rightEdge;
        protected boolean isOnTileBoundary;

        public EdgeInfo(int edgeType, int startNode, int endNode, int leftFace, int rightFace, int leftEdge,
            int rightEdge, boolean isOnTileBoundary, VPFBoundingBox bounds)
        {
            super(bounds);
            this.edgeType = edgeType;
            this.startNode = startNode;
            this.endNode = endNode;
            this.leftFace = leftFace;
            this.rightFace = rightFace;
            this.leftEdge = leftEdge;
            this.rightEdge = rightEdge;
            this.isOnTileBoundary = isOnTileBoundary;
        }

        public int getEdgeType()
        {
            return this.edgeType;
        }

        public int getStartNode()
        {
            return this.startNode;
        }

        public int getEndNode()
        {
            return this.endNode;
        }

        public int getLeftFace()
        {
            return this.leftFace;
        }

        public int getRightFace()
        {
            return this.rightFace;
        }

        public int getLeftEdge()
        {
            return this.leftEdge;
        }

        public int getRightEdge()
        {
            return this.rightEdge;
        }

        public boolean isOnTileBoundary()
        {
            return this.isOnTileBoundary;
        }
    }

    public static class FaceInfo extends BasicPrimitiveInfo
    {
        protected Ring outerRing;
        protected Ring[] innerRings;
        protected VPFBoundingBox bounds;

        public FaceInfo(Ring outerRing, Ring[] innerRings, VPFBoundingBox bounds)
        {
            super(bounds);
            this.outerRing = outerRing;
            this.innerRings = innerRings;
        }

        public Ring getOuterRing()
        {
            return this.outerRing;
        }

        public Ring[] getInnerRings()
        {
            return this.innerRings;
        }
    }

    public static class Ring
    {
        protected int numEdges;
        protected int[] edgeId;
        protected int[] edgeOrientation;

        public Ring(int numEdges, int[] edgeId, int[] edgeOrientation)
        {
            this.numEdges = numEdges;
            this.edgeId = edgeId;
            this.edgeOrientation = edgeOrientation;
        }

        public int getNumEdges()
        {
            return this.numEdges;
        }

        public int getEdgeId(int index)
        {
            return this.edgeId[index];
        }

        public int getEdgeOrientation(int index)
        {
            return this.edgeOrientation[index];
        }
    }

    protected Map<String, PrimitiveInfo[]> primitiveInfo;
    protected Map<String, VecBufferSequence> primitiveCoords;
    protected Map<String, CompoundStringBuilder> primitiveStrings;

    public VPFPrimitiveData()
    {
        this.primitiveInfo = new HashMap<String, PrimitiveInfo[]>();
        this.primitiveCoords = new HashMap<String, VecBufferSequence>();
        this.primitiveStrings = new HashMap<String, CompoundStringBuilder>();
    }

    public PrimitiveInfo[] getPrimitiveInfo(String name)
    {
        return this.primitiveInfo.get(name);
    }

    public void setPrimitiveInfo(String name, PrimitiveInfo[] info)
    {
        this.primitiveInfo.put(name, info);
    }

    public PrimitiveInfo getPrimitiveInfo(String name, int id)
    {
        return this.primitiveInfo.get(name)[VPFBufferedRecordData.indexFromId(id)];
    }

    public VecBufferSequence getPrimitiveCoords(String name)
    {
        return this.primitiveCoords.get(name);
    }

    public void setPrimitiveCoords(String name, VecBufferSequence coords)
    {
        this.primitiveCoords.put(name, coords);
    }

    public CompoundStringBuilder getPrimitiveStrings(String name)
    {
        return this.primitiveStrings.get(name);
    }

    public void setPrimitiveStrings(String name, CompoundStringBuilder strings)
    {
        this.primitiveStrings.put(name, strings);
    }
}
