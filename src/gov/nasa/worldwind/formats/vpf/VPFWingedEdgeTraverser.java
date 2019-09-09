/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

/**
 * Encapsulation of the Wiged-Edge Algorithm for VPF ring primitives, described in DIGEST Part 2, Annex C2.4.3. Given a
 * row from the ring primitive table, navigate the ring and edge primitive tables to construct the edge information
 * associated with the specified ring.
 *
 * @author dcollins
 * @version $Id: VPFWingedEdgeTraverser.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFWingedEdgeTraverser
{
    public interface EdgeTraversalListener
    {
        void nextEdge(int index, int primitiveId, boolean reverseCoordinates);
    }

    protected enum Orientation
    {
        LEFT,
        RIGHT,
        LEFT_AND_RIGHT
    }

    public VPFWingedEdgeTraverser()
    {
    }

    /**
     * Implementation of the Wiged-Edge Algorithm for ring primitives, described in DIGEST Part 2, Annex C2.4.3.Given a
     * row from the ring primitive table, navigate the ring and edge primitive tables to construct the edge information
     * associated with the specified ring.
     *
     * @param faceId The face.
     * @param startEdgeId The start edge.
     * @param edgeInfoArray the edge primitive data.
     * @param listener the ring edge listener, may be null.
     *
     * @return the number of edges composing the specified ring.
     */
    public int traverseRing(int faceId, int startEdgeId, VPFPrimitiveData.PrimitiveInfo[] edgeInfoArray,
        EdgeTraversalListener listener)
    {
        // 1. Determine which face primitive to construct.
        // The face is determined for us by the selection of a row in the face primitive table.

        // 2. Identify the start edge.
        // Select the row in the ring primitive table which is associated with the face primitive. Then select the row
        // in the edge primitive table which corresponds to the ring primitive. Essentially, we follow the face to a
        // ring, and the ring to a starting edge.

        // 3. Follow the edge network until we arrive back at the starting edge, or the data does not specify a next
        // edge. Travel in the direction according to which side of the edge (left or right) the face belongs to. If we
        // reach an auxiliary edge (face is both left and right of the edge), then travel to the next edge which does
        // not cause us to backtrack.

        int count = 0;
        int prevEdgeId;
        int curEdgeId = -1;
        int nextEdgeId = startEdgeId;

        do
        {
            prevEdgeId = curEdgeId;
            curEdgeId = nextEdgeId;

            if (listener != null)
            {
                listener.nextEdge(count, curEdgeId, this.getMustReverseCoordinates(faceId, prevEdgeId, curEdgeId,
                    edgeInfoArray));
            }

            count++;
        }
        while ((nextEdgeId = this.nextEdgeId(faceId, prevEdgeId, curEdgeId, edgeInfoArray)) > 0
            && (nextEdgeId != startEdgeId));

        return count;
    }

    protected int nextEdgeId(int faceId, int prevEdgeId, int curEdgeId, VPFPrimitiveData.PrimitiveInfo[] edgeInfoArray)
    {
        // The next edge depends on which side of this edge (left or right) the face belongs to. If the face is on
        // the left side of this edge, we travel to the left edge, and visa versa. However if this is an auxiliary
        // edge (face is both left and right of the edge), then travel to the next edge which does not cause us to
        // backtrack.

        Orientation o = this.getOrientation(faceId, curEdgeId, edgeInfoArray);
        if (o == null)
        {
            return -1;
        }

        switch (o)
        {
            case LEFT:
                return getEdgeInfo(edgeInfoArray, curEdgeId).getLeftEdge();
            case RIGHT:
                return getEdgeInfo(edgeInfoArray, curEdgeId).getRightEdge();
            case LEFT_AND_RIGHT:
                return (prevEdgeId > 0) ? this.auxiliaryNextEdgeId(prevEdgeId, curEdgeId, edgeInfoArray) : -1;
            default:
                return -1;
        }
    }

    protected Orientation getOrientation(int faceId, int edgeId, VPFPrimitiveData.PrimitiveInfo[] edgeInfo)
    {
        VPFPrimitiveData.EdgeInfo thisInfo = getEdgeInfo(edgeInfo, edgeId);
        boolean matchLeft = thisInfo.getLeftFace() == faceId;
        boolean matchRight = thisInfo.getRightFace() == faceId;

        if (matchLeft && matchRight) // Auxiliary edge has the same face on both sides.
        {
            return Orientation.LEFT_AND_RIGHT;
        }
        else if (matchLeft)
        {
            return Orientation.LEFT;
        }
        else if (matchRight)
        {
            return Orientation.RIGHT;
        }

        return null;
    }

    protected boolean getMustReverseCoordinates(int faceId, int prevEdgeId, int curEdgeId,
        VPFPrimitiveData.PrimitiveInfo[] edgeInfo)
    {
        // Determine whether or not this edge's coordinate array must be reversed to provide a consistent ordering
        // of ring coordinates. There are two cases which cause the coordinates to need reversal:
        // 1. If the edge has left orientation, then we will travel backwards along this edge to arrive at the next
        // edge in the ring.
        // 2. If the edge is an auxiliary edge (a connecting edge between an inner and outer loop which), then
        // we *may* travel backwards along this edge.

        Orientation o = this.getOrientation(faceId, curEdgeId, edgeInfo);
        if (o == null)
        {
            return false;
        }

        switch (o)
        {
            case LEFT:
                return true;
            case RIGHT:
                return false;
            case LEFT_AND_RIGHT:
                return (prevEdgeId > 0) && this.auxiliaryMustReverseCoordinates(prevEdgeId, curEdgeId, edgeInfo);
            default:
                return false;
        }
    }

    protected int auxiliaryNextEdgeId(int prevEdgeId, int curEdgeId,
        VPFPrimitiveData.PrimitiveInfo[] edgeInfoArray)
    {
        // Note: an edge which has the same face for both its left and right faces is known to be an "auxiliary edge".
        // Using auxiliary edges is a solution to defining a face (polygon) with holes. The face becomes a single loop,
        // with an auxiliary edge joining each inner and outer loop. The auxiliary edge is traversed twice; one upon
        // entering the inner ring, then  again upon exit.

        VPFPrimitiveData.EdgeInfo prevInfo = getEdgeInfo(edgeInfoArray, prevEdgeId);
        VPFPrimitiveData.EdgeInfo curInfo = getEdgeInfo(edgeInfoArray, curEdgeId);

        // Previous edge is adjacent to starting node.
        if (curInfo.getStartNode() == prevInfo.getStartNode() || curInfo.getStartNode() == prevInfo.getEndNode())
        {
            return (curInfo.getRightEdge() != curEdgeId) ? curInfo.getRightEdge() : curInfo.getLeftEdge();
        }
        // Previous edge is adjacent to ending node.
        else if (curInfo.getEndNode() == prevInfo.getStartNode() || curInfo.getEndNode() == prevInfo.getEndNode())
        {
            return (curInfo.getLeftEdge() != curEdgeId) ? curInfo.getLeftEdge() : curInfo.getRightEdge();
        }
        // Edges are not actually adjacent. This should never happen, but we check anyway.
        else
        {
            return -1;
        }
    }

    protected boolean auxiliaryMustReverseCoordinates(int prevEdgeId, int curEdgeId,
        VPFPrimitiveData.PrimitiveInfo[] edgeInfoArray)
    {
        VPFPrimitiveData.EdgeInfo prevInfo = getEdgeInfo(edgeInfoArray, prevEdgeId);
        VPFPrimitiveData.EdgeInfo curInfo = getEdgeInfo(edgeInfoArray, curEdgeId);

        return curInfo.getEndNode() == prevInfo.getStartNode() || curInfo.getEndNode() == prevInfo.getEndNode();
    }

    protected static VPFPrimitiveData.EdgeInfo getEdgeInfo(VPFPrimitiveData.PrimitiveInfo[] edgeInfo, int id)
    {
        return (VPFPrimitiveData.EdgeInfo) edgeInfo[VPFBufferedRecordData.indexFromId(id)];
    }
}
