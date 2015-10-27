/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

/**
 * @author dcollins
 * @version $Id: PolylineGeneralizer.java 2321 2014-09-17 19:34:42Z dcollins $
 */
public class PolylineGeneralizer
{
    protected static class Element
    {
        public final int ordinal;
        public final double x;
        public final double y;
        public final double z;
        public double area;
        public int heapIndex;
        public Element prev;
        public Element next;

        public Element(int ordinal, double x, double y, double z)
        {
            this.ordinal = ordinal;
            this.heapIndex = ordinal;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    protected int heapSize;
    protected Element[] heap;
    protected int vertexCount;
    protected double[] vertexArea;

    public PolylineGeneralizer()
    {
        this.heap = new Element[10];
        this.vertexArea = new double[10];
    }

    public int getVertexCount()
    {
        return this.vertexCount;
    }

    public double[] getVertexEffectiveArea(double[] array)
    {
        if (array == null || array.length < this.vertexCount)
            array = new double[this.vertexCount];

        System.arraycopy(this.vertexArea, 0, array, 0, this.vertexCount);

        return array;
    }

    public void beginPolyline()
    {
        this.heapSize = 0;
    }

    public void endPolyline()
    {
        this.computeInitialArea(); // compute the effective area of each vertex
        this.heapify(); // rearrange the vertex array in order to satisfy the min-heap property based on effective area
        this.computeEliminationArea(); // simulate repeated elimination of the min-area vertex
    }

    public void reset()
    {
        this.heapSize = 0;
        this.vertexCount = 0;
    }

    public void addVertex(double x, double y, double z)
    {
        if (this.heapSize == this.heap.length)
        {
            int capacity = this.heap.length + this.heap.length / 2; // increase heap capacity by 50%
            Element[] array = new Element[capacity];
            System.arraycopy(this.heap, 0, array, 0, this.heap.length);
            this.heap = array;
        }

        this.heap[this.heapSize++] = new Element(this.vertexCount++, x, y, z);
    }

    protected void computeInitialArea()
    {
        this.heap[0].area = Double.MAX_VALUE; // assign the start point the maximum area

        for (int i = 1; i < this.heapSize - 1; i++)
        {
            this.heap[i].prev = this.heap[i - 1];
            this.heap[i].next = this.heap[i + 1];
            this.heap[i].area = this.computeEffectiveArea(this.heap[i]);
        }

        this.heap[this.heapSize - 1].area = Double.MAX_VALUE; // assign the end point the maximum area
    }

    protected void computeEliminationArea()
    {
        if (this.vertexArea.length < this.vertexCount)
        {
            double[] array = new double[this.vertexCount];
            System.arraycopy(this.vertexArea, 0, array, 0, this.vertexArea.length);
            this.vertexArea = array;
        }

        // Repeatedly find the point with the least effective area and eliminate it, until only the start point and the
        // end point remain (the start point and end point are not in the heap).
        Element cur;
        double lastArea = 0;
        while ((cur = this.pop()) != null)
        {
            // If the current point's area is less than that of the last point to be eliminated, use the latter's area
            // instead. This ensures that the current point cannot be filtered before previously eliminated points.
            double area = cur.area;
            if (area < lastArea)
                area = lastArea;
            else // Otherwise, update the last area with the current point's area.
                lastArea = area;
            this.vertexArea[cur.ordinal] = area;

            // Recompute previous point's effective area, unless it's the start point.
            if (cur.prev != null && cur.prev.prev != null)
            {
                cur.prev.next = cur.next;
                this.updateEffectiveArea(cur.prev);
            }

            // Recompute next point's effective area, unless it's the end point.
            if (cur.next != null && cur.next.next != null)
            {
                cur.next.prev = cur.prev;
                this.updateEffectiveArea(cur.next);
            }

            // Drop references the previous point and the next point.
            cur.prev = null;
            cur.next = null;
        }
    }

    // TODO: Modify computeEffectiveArea to correctly compute area when z != 0
    protected double computeEffectiveArea(Element e)
    {
        Element c = e;
        Element p = e.prev;
        Element n = e.next;

        return 0.5 * Math.abs((p.x - c.x) * (n.y - c.y) - (p.y - c.y) * (n.x - c.x));
    }

    protected void updateEffectiveArea(Element e)
    {
        double oldArea = e.area;
        double newArea = this.computeEffectiveArea(e);
        e.area = newArea;

        if (newArea < oldArea)
            this.siftUp(e.heapIndex, e);
        else if (newArea > oldArea)
            this.siftDown(e.heapIndex, e);
    }

    protected void heapify()
    {
        for (int i = (this.heapSize >>> 1) - 1; i >= 0; i--)
        {
            this.siftDown(i, this.heap[i]);
        }
    }

    protected Element pop()
    {
        if (this.heapSize == 0)
            return null;

        int size = --this.heapSize;
        Element top = this.heap[0];
        Element last = this.heap[size];
        this.heap[size] = null;

        if (size != 0)
        {
            this.siftDown(0, last);
        }

        return top;
    }

    protected void siftUp(int k, Element x)
    {
        while (k > 0)
        {
            int parent = (k - 1) >>> 1;
            Element e = this.heap[parent];
            if (x.area >= e.area)
                break;

            this.heap[k] = e;
            e.heapIndex = k;
            k = parent;
        }

        this.heap[k] = x;
        x.heapIndex = k;
    }

    protected void siftDown(int k, Element x)
    {
        int half = this.heapSize >>> 1;
        while (k < half)
        {
            int child = (k << 1) + 1;
            Element c = this.heap[child];

            int right = child + 1;
            if (right < this.heapSize && c.area > this.heap[right].area)
                c = this.heap[child = right];
            if (x.area <= c.area)
                break;

            this.heap[k] = c;
            c.heapIndex = k;
            k = child;
        }

        this.heap[k] = x;
        x.heapIndex = k;
    }
}
