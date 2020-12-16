package gov.nasa.worldwind.geom;

import java.util.*;

public class BoundedPlane {

    protected ArrayList<Vec4> vertices;

    protected Vec4 xVector;
    protected Vec4 yVector;
    protected Vec4 zVector;
    protected Vec4 center;
    protected Plane xzPlane;
    protected Plane yzPlane;
    protected double xAxisLength;
    protected double yAxisLength;

    public BoundedPlane(List<? extends Vec4> vertices, Vec4 yVector, Vec4 zVector) {
        this.vertices = new ArrayList<>();
        this.xVector = yVector.cross3(zVector);
        this.yVector = yVector;
        this.zVector = zVector;
        if (vertices != null) {
            this.expand(vertices);
        }
    }

    public BoundedPlane(Vec4[] vertices, Vec4 yVector, Vec4 zVector) {
        this((List<? extends Vec4>) null, yVector, zVector);
        ArrayList<Vec4> vertexList = new ArrayList<>();
        for (Vec4 v : vertices) {
            vertexList.add(v);
        }
        this.expand(vertexList);
    }

    public final void expand(List<? extends Vec4> vertices) {
        this.vertices.addAll(vertices);
        double xSum = 0, ySum = 0, zSum = 0;
        for (Vec4 vtx : this.vertices) {
            xSum += vtx.x;
            ySum += vtx.y;
            zSum += vtx.z;
        }
        double nVtx = this.vertices.size();
        this.center = new Vec4(xSum / nVtx, ySum / nVtx, zSum / nVtx);

        this.xzPlane = Plane.fromPoints(this.center, this.center.add3(this.xVector), this.center.add3(this.zVector));
        this.yzPlane = Plane.fromPoints(this.center, this.center.add3(this.yVector), this.center.add3(this.zVector));
        this.xAxisLength = 0;
        this.yAxisLength = 0;
        for (Vec4 vtx : this.vertices) {
            this.xAxisLength = Math.max(this.xAxisLength, this.yzPlane.distanceTo(vtx));
            this.yAxisLength = Math.max(this.yAxisLength, xzPlane.distanceTo(vtx));
        }
        this.xAxisLength *= 2;
        this.yAxisLength *= 2;
    }

    public Plane getXZPlane() {
        return this.xzPlane;
    }

    public Plane getYZPlane() {
        return this.yzPlane;
    }

    public double getXAxisLength() {
        return this.xAxisLength;
    }

    public double getYAxisLength() {
        return this.yAxisLength;
    }

    public double getArea() {
        return this.xAxisLength * this.yAxisLength;
    }

    @Override
    public String toString() {
        return String.format("(% 6.1f,% 6.1f,% 6.1f) (% 6.1f,% 6.1f)", this.center.x, this.center.y, this.center.z, this.xAxisLength, this.yAxisLength);
    }

}
