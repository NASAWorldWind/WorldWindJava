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
package gov.nasa.worldwind.formats.shapefile;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.glu.GLU;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.WWTexture;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.LazilyLoadedTexture;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.avlist.AVKey;

import com.jogamp.opengl.*;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.meshes.*;
import java.nio.*;
import java.util.*;

public class ShapefileMultiPatch extends Mesh3D {

    protected static class BuildingTexture {

        protected LazilyLoadedTexture texture;

        public BuildingTexture(String path) {
            this.texture = new LazilyLoadedTexture(path);
        }

        public LazilyLoadedTexture getTexture() {
            return this.texture;
        }
    }

    protected static BuildingTexture[] buildingTextures;

    protected class PatchGeometry implements AbstractGeometry {

        protected FloatBuffer vertices;
        protected FloatBuffer texCoords;
        protected FloatBuffer normals;
        protected boolean verticalPatch;
        protected ArrayList<Face> faces;

        public void setVerticalPatch(boolean verticalPatch) {
            this.verticalPatch = verticalPatch;
        }

        public boolean isVerticalPatch() {
            return this.verticalPatch;
        }

        public PatchGeometry(FloatBuffer vertices, FloatBuffer normals) {
            init(vertices, normals);
        }

        protected final void init(FloatBuffer vertices, FloatBuffer normals) {
            this.vertices = vertices;
            this.normals = normals;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getVertices(FloatBuffer buffer) {
            this.vertices.rewind();
            buffer.put(this.vertices);
        }

        @Override
        public FloatBuffer getVertexBuffer() {
            return this.vertices;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNormals() {
            return this.normals != null;
        }

        public boolean hasTexCoords() {
            return this.texCoords != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getNormals(FloatBuffer buffer) {
            if (this.hasNormals()) {
                this.normals.rewind();
                buffer.put(this.normals);
            }
        }

        @Override
        public FloatBuffer getNormalBuffer() {
            return this.normals;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCount() {
            return this.vertices.capacity() / 9;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getTextureCoordinates(FloatBuffer buffer) {
            if (this.hasTexCoords()) {
                this.texCoords.rewind();
                buffer.put(this.texCoords);
            }
        }

        public void setFaces(ArrayList<Face> faces) {
            this.faces = faces;
        }

        public boolean isDirty() {  // needs rebuilding
            int nFaceTris = 0;
            for (Face face : faces) {
                nFaceTris += face.getTriangles().size();
            }
            return this.getCount() != nFaceTris;
        }

        public ArrayList<FaceTriangle> getFaceTriangles() {
            ArrayList<FaceTriangle> faceTriangles = new ArrayList<>();
            for (Face face : faces) {
                faceTriangles.addAll(face.getTriangles());
            }
            return faceTriangles;
        }

    }

    protected static class Record {

        protected final CompoundVecBuffer pointBuffer;
        protected ShapefileRecordMultiPatch.PartType[] partTypes;
        protected ArrayList<Position[]> partPositions;
        protected int numberOfParts;

        public Record(ShapefileRecord shapefileRecord) {
            partPositions = new ArrayList<>();
            this.numberOfParts = shapefileRecord.getNumberOfParts();
            int firstPartNumber = shapefileRecord.getFirstPartNumber();
            this.pointBuffer = shapefileRecord.getShapeFile().getPointBuffer();
            this.partTypes = ((ShapefileRecordMultiPatch) shapefileRecord).getPartTypes();
            double[] location = new double[2];
            for (int i = 0; i < numberOfParts; i++) {
                VecBuffer points = this.getBoundaryPoints(firstPartNumber, i);
                Position[] locations = new Position[points.getSize()];
                double[] zValues = ((ShapefileRecordMultiPatch) shapefileRecord).getZValues(i);
                for (int j = 0; j < points.getSize(); j++) {
                    points.get(j, location);
                    locations[j] = new Position(Angle.fromDegrees(location[1]), Angle.fromDegrees(location[0]), zValues[j]);
                }
                partPositions.add(locations);
            }
        }

        private VecBuffer getBoundaryPoints(int firstPartNumber, int index) {
            if (index < 0 || index >= this.numberOfParts) {
                String msg = Logging.getMessage("generic.indexOutOfRange", index);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            synchronized (this.pointBuffer) // synchronize access to the Shapefile's shared pointBuffer
            {
                return this.pointBuffer.subBuffer(firstPartNumber + index);
            }
        }

        public ShapefileRecordMultiPatch.PartType getPartType(int partNo) {
            return this.partTypes[partNo];
        }

        public int getNumberOfParts() {
            return this.numberOfParts;
        }

        public Position[] getPartPositions(int index) {
            if (index < 0 || index >= this.numberOfParts) {
                String msg = Logging.getMessage("generic.indexOutOfRange", index);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            return partPositions.get(index);
        }

        public void printPositions() {
            for (int i = 0; i < this.numberOfParts; i++) {
                Position[] positions = this.getPartPositions(i);
                System.out.println("Part " + i);
                for (Position p : positions) {
                    System.out.println(p);
                }
                System.out.println();
            }
        }

    }

    protected Record record;
    protected ArrayList<PatchGeometry> patchGeometries;
    protected boolean useTextureMaps;
    protected ShapeAttributes verticalAttrs;

    /**
     * Creates a new ShapefileMultiPatch with the specified shapefile.The normal
     * attributes, the highlight attributes and the attribute delegate are
     * optional. Specifying a non-null value for normalAttrs or highlightAttrs
     * causes each ShapefileRenderable.Record to adopt those attributes.
     * Specifying a non-null value for the attribute delegate enables callbacks
     * during creation of each ShapefileRenderable.Record. See
     * {@link AttributeDelegate} for more information.
     *
     * @param shapefile The shapefile to display.
     * @param normalAttrs The normal attributes for each
     * ShapefileRenderable.Record. May be null to use the default attributes.
     * @param highlightAttrs The highlight attributes for each
     * ShapefileRenderable.Record. May be null to use the default highlight
     * attributes.
     * @param attributeDelegate Optional callback for configuring each
     * ShapefileRenderable.Record's shape attributes and key-value attributes.
     * May be null.
     * @param aoiFilter Area of Interest filter for shapes. Shapes external to
     * the filter will not be shown.
     *
     * @throws IllegalArgumentException if the shapefile is null.
     */
    public ShapefileMultiPatch(Record record, ShapeAttributes normalAttrs, ShapeAttributes highlightAttrs,
            ShapefileRenderable.AttributeDelegate attributeDelegate, boolean useTextureMaps) {
        if (record == null) {
            String msg = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.record = record;
        this.normalAttrs = normalAttrs;
        this.highlightAttrs = highlightAttrs;
        this.useTextureMaps = useTextureMaps;
//        this.attributeDelegate = attributeDelegate;
    }

    private void addTriangleNormals(Vec4[] trianglePoints, FloatBuffer normals) {
        Vec4 normal = WWMath.computeTriangleNormal(trianglePoints[0], trianglePoints[1], trianglePoints[2]);
        for (int i = 0; i < this.vertsPerShape; i++) {
            normals.put((float) normal.x);
            normals.put((float) normal.y);
            normals.put((float) normal.z);
        }
    }

    private Vec4[] addTriangle(Vec4[] trianglePoints, FloatBuffer vertices) {
        Triangle t = new Triangle(trianglePoints);
        if (t.getWinding().equals(AVKey.CLOCKWISE)) {
            trianglePoints = new Vec4[]{trianglePoints[2], trianglePoints[1], trianglePoints[0]};
        }

        for (int i = 0; i < 3; i++) {
            vertices.put((float) trianglePoints[i].x);
            vertices.put((float) trianglePoints[i].y);
            vertices.put((float) trianglePoints[i].z);
        }

        return trianglePoints;
    }

    protected PatchGeometry generateTriangleStrip(Terrain terrain, Position[] locations, Vec4 referencePoint) {
        int bufSize = (locations.length - 2) * this.vertsPerShape * this.vertsPerShape;
        FloatBuffer vertices = Buffers.newDirectFloatBuffer(bufSize);
        FloatBuffer normals = Buffers.newDirectFloatBuffer(bufSize);
        Vec4[] trianglePoints = new Vec4[this.vertsPerShape];
        for (int i = 0; i < locations.length - 2; i++) {
            trianglePoints[0] = this.computePoint(terrain, locations[i]).subtract3(referencePoint);
            trianglePoints[1] = this.computePoint(terrain, locations[i + 1]).subtract3(referencePoint);
            trianglePoints[2] = this.computePoint(terrain, locations[i + 2]).subtract3(referencePoint);
            trianglePoints = this.addTriangle(trianglePoints, vertices);
            addTriangleNormals(trianglePoints, normals);
        }
        return new PatchGeometry(vertices, normals);
    }

    protected PatchGeometry generateTriangleFan(Terrain terrain, Position[] locations, Vec4 referencePoint) {
        int bufSize = (locations.length - 2) * this.vertsPerShape * this.vertsPerShape;
        FloatBuffer vertices = Buffers.newDirectFloatBuffer(bufSize);
        FloatBuffer normals = Buffers.newDirectFloatBuffer(bufSize);
        Vec4[] trianglePoints = new Vec4[this.vertsPerShape];
        for (int i = 1; i < locations.length - 1; i++) {
            trianglePoints[0] = this.computePoint(terrain, locations[0]).subtract3(referencePoint);
            trianglePoints[1] = this.computePoint(terrain, locations[i]).subtract3(referencePoint);
            trianglePoints[2] = this.computePoint(terrain, locations[i + 1]).subtract3(referencePoint);
            trianglePoints = this.addTriangle(trianglePoints, vertices);
            addTriangleNormals(trianglePoints, normals);
        }
        return new PatchGeometry(vertices, normals);
    }

    protected IntBuffer tessellatePolygon(Terrain terrain, Vec4[] modelPoints, Vec4 normal) {
        GLUTessellatorSupport glts = new GLUTessellatorSupport();
        GLUTessellatorSupport.CollectIndexListsCallback cb = new GLUTessellatorSupport.CollectIndexListsCallback();

        glts.beginTessellation(cb, normal);
        try {
            double[] coords = new double[3];

            GLU.gluTessBeginPolygon(glts.getGLUtessellator(), null);

            GLU.gluTessBeginContour(glts.getGLUtessellator());
            for (int i = 0; i < modelPoints.length; i++) {
                coords[0] = modelPoints[i].x;
                coords[1] = modelPoints[i].y;
                coords[2] = modelPoints[i].z;

                GLU.gluTessVertex(glts.getGLUtessellator(), coords, 0, i);
            }
            GLU.gluTessEndContour(glts.getGLUtessellator());
            GLU.gluTessEndPolygon(glts.getGLUtessellator());
        } finally {
            glts.endTessellation();
        }

        int size = this.countTriangleVertices(cb.getPrims(), cb.getPrimTypes());
        IntBuffer indices = Buffers.newDirectIntBuffer(size);
        for (int i = 0; i < cb.getPrims().size(); i++) {
            switch (cb.getPrimTypes().get(i)) {
                case GL.GL_TRIANGLES:
                    Triangle.expandTriangles(cb.getPrims().get(i), indices);
                    break;
                case GL.GL_TRIANGLE_FAN:
                    Triangle.expandTriangleFan(cb.getPrims().get(i), indices);
                    break;
                case GL.GL_TRIANGLE_STRIP:
                    Triangle.expandTriangleStrip(cb.getPrims().get(i), indices);
                    break;
            }
        }
        return indices;
    }

    protected PatchGeometry generatePolygon(DrawContext dc, Position[] locations, Vec4 referencePoint) {
        Vec4[] modelPoints = new Vec4[locations.length];
        Terrain terrain = dc.getTerrain();
        for (int i = 0; i < locations.length; i++) {
            modelPoints[i] = this.computePoint(terrain, locations[i]).subtract3(referencePoint);
        }
        Vec4 normal = WWMath.computeArrayNormal(modelPoints);
        if (normal == null) { // degenerate
            return null;
        }
        IntBuffer indices = this.tessellatePolygon(terrain, modelPoints, normal);
        if (indices != null) {
            int nIndices = indices.capacity();
            int bufSize = nIndices * this.vertsPerShape;
            FloatBuffer vertices = Buffers.newDirectFloatBuffer(bufSize);
            FloatBuffer normals = Buffers.newDirectFloatBuffer(bufSize);
            Vec4[] trianglePoints = new Vec4[this.vertsPerShape];
            int triIdx = 0;
            for (int i = 0; i < nIndices; i++) {
                int idx = indices.get(i) - 1;
                trianglePoints[triIdx++] = modelPoints[idx];
                if (triIdx == this.vertsPerShape) {
                    triIdx = 0;
                    trianglePoints = this.addTriangle(trianglePoints, vertices);
                    addTriangleNormals(trianglePoints, normals);
                }
            }
            return new PatchGeometry(vertices, normals);
        }
        return null;
    }

    protected void assembleGeometries(DrawContext dc) {
        this.setElementType(GL.GL_TRIANGLES);
        this.setVertsPerShape(3);
        this.patchGeometries = new ArrayList<>();
        Terrain terrain = dc.getTerrain();
        Vec4 referencePoint = terrain.getSurfacePoint(this.getModelPosition());
        PatchGeometry newGeometry = null;
        for (int i = 0; i < this.record.getNumberOfParts(); i++) {
            Position[] locations = this.record.getPartPositions(i);
            switch (this.record.getPartType(i)) {
                case TriangleStrip:
                    newGeometry = generateTriangleStrip(terrain, locations, referencePoint);
                    break;
                case TriangleFan:
                    newGeometry = generateTriangleFan(terrain, locations, referencePoint);
                    break;
                case OuterRing: // Polygon outer boundary
                    newGeometry = generatePolygon(dc, locations, referencePoint);
                    break;
                case InnerRing: // Polygon hole
                    newGeometry = null;
                    System.out.println("2Not implemented.");
                    break;
                case FirstRing: // Polygon hole
                    newGeometry = null;
                    System.out.println("3Not implemented.");
                    break;
                case Ring: // Polygon outer boundary
                    newGeometry = null;
                    System.out.println("4Not implemented.");
                    break;
                default:
                    String message = Logging.getMessage("generic.UnrecognizedDataType", this.record.getPartType(i));
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
            }

            if (newGeometry != null) {
                this.patchGeometries.add(newGeometry);
            }
        }

        this.combineGeometries(dc.getGlobe().computeSurfaceNormalAtPoint(referencePoint));
        this.computeTextureCoordinates(dc.getGlobe().computeSurfaceNormalAtPoint(referencePoint));
        this.setRenderableGeometries(this.patchGeometries);
    }

    protected class FaceTriangle extends Triangle {

        protected int[] vertexIndices;

        public FaceTriangle(Vec4[] trianglePoints, int[] vertexIndices) {
            super(trianglePoints);
            this.vertexIndices = new int[]{vertexIndices[0], vertexIndices[1], vertexIndices[2]};
        }

        public int[] getVertexIndices() {
            return this.vertexIndices;
        }

        public boolean isDegenerate() {
            return getNormal().distanceTo3(Vec4.ZERO) == 0;
        }

        @Override
        public String toString() {
            String ret = "";
            for (Vec4 v : this.getVertices()) {
                ret += String.format("(% 6.1f,% 6.1f,% 6.1f) ", v.x, v.y, v.z);
            }
            return ret;
        }

        public Vec4 getNormal() {
            return WWMath.computeTriangleNormal(this);
        }
    }

    protected class Face {

        protected static final double NORMAL_EPSILON = 0.0000001;
        protected static final double VERTICAL_EPSILON = 0.017;
        protected ArrayList<FaceTriangle> faceTriangles;
        protected Vec4 normal;
        protected BoundedPlane plane;

        public Face(FaceTriangle initialFace) {
            this.faceTriangles = new ArrayList<>();
            this.add(initialFace);
        }

        public final void add(FaceTriangle t) {
            faceTriangles.add(t);
            this.plane = null;
        }

        public final void add(Face that) {
            that.faceTriangles.forEach((t) -> {
                this.add(t);
            });
        }

        public Vec4 getNormal() {
            this.normal = WWMath.computeTriangleNormal(this.faceTriangles.get(0));
            return this.normal;
        }

        public boolean isVertical(Vec4 surfaceNormal) {
            double normalAngle = Math.abs(this.getNormal().angleBetween3(surfaceNormal).radians);
            double radians90 = Angle.POS90.radians;
            return (radians90 - VERTICAL_EPSILON) <= normalAngle && normalAngle <= (radians90 + VERTICAL_EPSILON);
        }

        public boolean adjoins(Face that) {
            for (Triangle t : that.faceTriangles) {
                if (this.adjoins(t)) {
                    return true;
                }
            }

            return false;
        }

        public boolean adjoins(Triangle that) {
            Vec4 thatNormal = WWMath.computeTriangleNormal(that);
            if (thatNormal.angleBetween3(this.getNormal()).radians > NORMAL_EPSILON) {
                return false;
            }

            boolean commonVertex = false;
            Vec4[] vThat = that.getVertices();
            for (int i = 0; i < this.faceTriangles.size() && !commonVertex; i++) {
                Triangle t = this.faceTriangles.get(i);
                Vec4[] vThis = t.getVertices();
                for (int j = 0; j < 3 && !commonVertex; j++) {
                    for (int k = 0; k < 3 && !commonVertex; k++) {
                        commonVertex = vThis[j].equals(vThat[k]);
                    }
                }
            }

            return commonVertex;
        }

        public ArrayList<Vec4> getVertices() {
            ArrayList<Vec4> faceVertices = new ArrayList<>();
            for (Triangle t : this.faceTriangles) {
                Vec4[] triVertices = t.getVertices();
                for (Vec4 v : triVertices) {
                    faceVertices.add(v);
                }
            }

            return faceVertices;
        }

        public ArrayList<FaceTriangle> getTriangles() {
            return this.faceTriangles;
        }

        public int getVertexHash(Vec4 vtx) {
            return this.hashCode() + vtx.hashCode();
        }

        public BoundedPlane calculatePlane(Vec4 surfaceNormal) {
            this.plane = new BoundedPlane(this.getVertices(), surfaceNormal, this.getNormal());
            return this.plane;
        }
    }

    private ArrayList<Face> combineAdjoiningFaces(ArrayList<Face> faces) {
        for (int i = 0; i < faces.size(); i++) {
            Face faceI = faces.get(i);
            for (int j = i + 1; j < faces.size(); j++) {
                Face faceJ = faces.get(j);
                if (faceI.adjoins(faceJ)) {
                    faceI.add(faceJ);
                    faces.remove(j);
                    break;
                }
            }
        }

        return faces;
    }

    private HashMap<Integer, Vec4> computeFaceTexCoords(ArrayList<Face> faces, Vec4 surfaceNormal) {
        HashMap<Integer, Vec4> coordMap = new HashMap<>();
        for (Face face : faces) {
            if (face.isVertical(surfaceNormal)) {
                ArrayList<Vec4> faceVertices = face.getVertices();
                BoundedPlane bounds = face.calculatePlane(surfaceNormal); // new BoundedPlane(faceVertices, surfaceNormal, face.getNormal());
                Plane xzPlane = bounds.getXZPlane();
                Plane yzPlane = bounds.getYZPlane();
                double xLength = bounds.getXAxisLength();
                double yLength = bounds.getYAxisLength();
                double xLength2 = xLength / 2;
                double yLength2 = yLength / 2;
                for (Vec4 vtx : faceVertices) {
                    int vertexKey = face.getVertexHash(vtx);
                    if (!coordMap.containsKey(vertexKey)) {
                        double u = Math.min((yzPlane.distanceTo(vtx) + xLength2) / xLength, 1.0);
                        double v = Math.min((yLength - (xzPlane.distanceTo(vtx) + yLength2)) / yLength, 1.0);
                        coordMap.put(vertexKey, new Vec4(u, v));
                    }
                }
            } else {
                return null;
            }
        }

        if (coordMap.isEmpty()) {
            return null;
        }

        return coordMap;

    }

    protected ArrayList<Face> removeTrivialFaces(ArrayList<Face> faces, Vec4 surfaceNormal) {
        ArrayList<Face> newFaces = new ArrayList<>();
        double minArea = 200;
        for (Face face : faces) {
            BoundedPlane plane = face.calculatePlane(surfaceNormal);
            if (plane.getArea() > minArea) {
                newFaces.add(face);
            }
        }
        return newFaces;

    }

    protected ArrayList<Face> splitFacesByHeight(ArrayList<Face> faces) {
        ArrayList<Face> newFaces = new ArrayList<>();
        newFaces.addAll(faces);
        return newFaces;
    }

    protected void combineGeometries(Vec4 surfaceNormal) {
        Vec4[] trianglePoints = new Vec4[this.vertsPerShape];
        int[] triangleIndices = new int[3];
        float x, y, z;
        int p = 0;
        while (p < this.patchGeometries.size()) {
            PatchGeometry pg = this.patchGeometries.get(p);
            ArrayList<Face> faces = new ArrayList<>();
            int nTris = pg.getCount();
            pg.vertices.rewind();
            int vertexIndex = 0;
            for (int i = 0; i < nTris; i++) {
                for (int j = 0; j < this.vertsPerShape; j++) {
                    x = pg.vertices.get();
                    y = pg.vertices.get();
                    z = pg.vertices.get();
                    trianglePoints[j] = new Vec4(x, y, z);
                    triangleIndices[j] = vertexIndex++;
                }
                FaceTriangle newTri = new FaceTriangle(trianglePoints, triangleIndices);
                if (!newTri.isDegenerate()) {
                    boolean faceFound = false;
                    for (Face face : faces) {
                        if (face.adjoins(newTri)) {
                            face.add(newTri);
                            faceFound = true;
                            break;
                        }
                    }
                    if (!faceFound) {
                        Face newFace = new Face(newTri);
                        faces.add(newFace);
                    }
                }
            }
            faces = combineAdjoiningFaces(faces);
            if (faces.isEmpty()) {
                this.patchGeometries.remove(p);
            } else {
                p++;
                boolean verticalPatch = true;
                for (Face face : faces) {
                    verticalPatch = verticalPatch && face.isVertical(surfaceNormal);
                }

                pg.setVerticalPatch(verticalPatch);
                pg.setFaces(faces);
            }
        }
    }

    private void rebuildGeometry(PatchGeometry pg) {
        ArrayList<FaceTriangle> faceTris = pg.getFaceTriangles();
        int bufSize = faceTris.size() * this.vertsPerShape * this.vertsPerShape;
        FloatBuffer vertices = Buffers.newDirectFloatBuffer(bufSize);
        FloatBuffer normals = Buffers.newDirectFloatBuffer(bufSize);
        for (FaceTriangle tri : faceTris) {
            Vec4[] triVertices = tri.getVertices();
            this.addTriangle(triVertices, vertices);
            this.addTriangleNormals(triVertices, normals);
        }
        pg.init(vertices, normals);
    }

    private boolean moveFaces(PatchGeometry pg1, PatchGeometry pg2) {
        boolean anyMoved = false;
        boolean facesMoved = true;
        while (facesMoved) {
            facesMoved = false;
            for (int i = 0; i < pg1.faces.size() && !facesMoved; i++) {
                Face faceI = pg1.faces.get(i);
                for (int j = 0; j < pg2.faces.size() && !facesMoved; j++) {
                    Face faceJ = pg2.faces.get(j);
                    if (faceI.adjoins(faceJ)) {
                        facesMoved = true;
                        anyMoved = true;
                        faceI.add(faceJ);
                        pg2.faces.remove(j);
                    }
                }
            }

        }
        return anyMoved;
    }

    protected void computeTextureCoordinates(Vec4 surfaceNormal) {
        if (!this.useTextureMaps) {
            return;
        }
        for (PatchGeometry pg : this.patchGeometries) {
            ArrayList<Face> faces = pg.faces;
            HashMap<Integer, Vec4> textureMap = computeFaceTexCoords(faces, surfaceNormal);
            if (textureMap != null) {
                pg.texCoords = Buffers.newDirectFloatBuffer((pg.vertices.capacity() / this.vertsPerShape) * 2);
                for (Face face : faces) {
                    for (FaceTriangle tri : face.getTriangles()) {
                        Vec4[] triVertices = tri.getVertices();
                        int[] vertexIndices = tri.getVertexIndices();
                        for (int i = 0; i < triVertices.length; i++) {
                            int vertexKey = face.getVertexHash(triVertices[i]);
                            Vec4 texCoord = textureMap.get(vertexKey);
                            int idx = vertexIndices[i] * 2;
                            if (texCoord != null) {
                                pg.texCoords.put(idx, (float) texCoord.x);
                                pg.texCoords.put(idx + 1, (float) texCoord.y);
                            } else {
                                pg.texCoords.put(idx, 0);
                                pg.texCoords.put(idx + 1, 0);
                                System.out.println("error");
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc) {
        if (this.patchGeometries == null) {
            this.assembleGeometries(dc);
        }
        return super.doMakeOrderedRenderable(dc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean mustApplyTexture(Geometry geometry) {
        return this.useTextureMaps && ((PatchGeometry) geometry.getNativeGeometry()).hasTexCoords() && this.getTexture(geometry) != null;
    }

    public void setVerticalAttributes(ShapeAttributes verticalAttrs) {
        this.verticalAttrs = verticalAttrs;
    }

    public Record getRecord() {
        return this.record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WWTexture getTexture(Geometry geometry) {
        if (geometry.getTexture() != null) {
            return geometry.getTexture();
        }

        geometry.setTexture(ShapefileMultiPatch.buildingTextures[0].getTexture());

        return geometry.getTexture();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Material getMaterial(AbstractGeometry geometry) {
        if (((PatchGeometry) geometry).isVerticalPatch() && this.verticalAttrs != null) {
            return this.verticalAttrs.getInteriorMaterial();
        }

        if (this.normalAttrs != null) {
            return this.normalAttrs.getInteriorMaterial();
        }

        return this.defaultAttributes.getInteriorMaterial();
    }

    /**
     * {@inheritDoc} TODO: Fix windings and normals
     */
    @Override
    protected boolean isDoubleSided(AbstractGeometry geometry) {
        return true;
    }

    protected static boolean mustAssembleRecord(ShapefileRecord shapefileRecord, Sector aoiFilter) {
        boolean inAOI = true;
        double[] bounds = shapefileRecord.getBoundingRectangle();
        if (bounds != null) {
            if (aoiFilter != null) {
                Sector boundsSector = new Sector(Angle.fromDegrees(bounds[0]), Angle.fromDegrees(bounds[1]),
                        Angle.fromDegrees(bounds[2]), Angle.fromDegrees(bounds[3]));
                inAOI = boundsSector.intersects(aoiFilter);
            }
        } else {
            inAOI = false;
        }
        return inAOI && shapefileRecord.getNumberOfParts() > 0
                && shapefileRecord.getNumberOfPoints() > 0
                && !shapefileRecord.isNullRecord();
    }

    protected static ArrayList<Record> assembleRecords(Shapefile shapefile, Sector aoiFilter) {
        ArrayList<Record> records = new ArrayList<>();

        while (shapefile.hasNext()) {
            ShapefileRecord shapefileRecord = shapefile.nextRecord();

            if (mustAssembleRecord(shapefileRecord, aoiFilter)) {
                Record record = new Record(shapefileRecord);
                records.add(record);
            }

        }
        records.trimToSize(); // Reduce memory overhead from unused ArrayList capacity.
        return records;
    }

    public static ArrayList<ShapefileMultiPatch> createMeshes(Shapefile shapefile, ShapeAttributes normalAttrs, ShapeAttributes highlightAttrs,
            ShapefileRenderable.AttributeDelegate attributeDelegate, Sector aoiFilter, boolean useTextureMaps) {
        String[] texturePaths = new String[]{"testData/white-office-building.jpg"};
        ShapefileMultiPatch.buildingTextures = new ShapefileMultiPatch.BuildingTexture[texturePaths.length];
        for (int i = 0; i < texturePaths.length; i++) {
            ShapefileMultiPatch.buildingTextures[i] = new ShapefileMultiPatch.BuildingTexture(texturePaths[i]);
        }
        ArrayList<Record> records = assembleRecords(shapefile, aoiFilter);
        ArrayList<ShapefileMultiPatch> shapes = new ArrayList<>();
        records.forEach((record) -> {
            ShapefileMultiPatch shape = new ShapefileMultiPatch(record, normalAttrs, highlightAttrs, attributeDelegate, useTextureMaps);
            Position modelPos = record.getPartPositions(0)[0];
            shape.setModelPosition(new Position(modelPos.latitude, modelPos.longitude, 0));
            shape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            shapes.add(shape);

        });
        return shapes;
    }
}
