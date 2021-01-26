package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Vec4;

public class GLTFMesh extends GLTFArray {

    private GLTFPrimitive[] primitives;
    private Vec4[] vertexBuffer;
    private Vec4[] normalBuffer;
    private int[] bufferIndices;
    private String name;
    private GLTFMaterial material;

    public GLTFMesh(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_PRIMITIVES:
                    Object[] sourceArray = (Object[]) properties.getValue(propName);
                    this.primitives = new GLTFPrimitive[sourceArray.length];
                    for (int i = 0; i < sourceArray.length; i++) {
                        this.primitives[i] = new GLTFPrimitive((AVListImpl) sourceArray[i]);
                    }
                    break;
                case GLTFParserContext.KEY_NAME:
                    this.name = properties.getStringValue(propName);
                    break;
                default:
                    System.out.println("GLTFMesh: Unsupported " + propName);
                    break;
            }
        }
    }

    public void assembleGeometry(GLTFRoot root) {
        for (GLTFPrimitive primitive : this.primitives) {
            int materialIdx=primitive.getMaterialIdx();
            this.material=root.getMaterialForIdx(materialIdx);
            
            int vertexAccessorIdx = primitive.getVertexAccessorIdx();
            GLTFAccessor accessor = root.getAccessorForIdx(vertexAccessorIdx);
            this.vertexBuffer = accessor.getCoordBuffer(root);

            int normalAccessorIdx = primitive.getNormalAccessorIdx();
            if (normalAccessorIdx >= 0) {
                accessor = root.getAccessorForIdx(normalAccessorIdx);
                this.normalBuffer = accessor.getCoordBuffer(root);
            }

            int vertexIndicesAccessorIdx = primitive.getVertexIndicesAccessorIdx();
            accessor = root.getAccessorForIdx(vertexIndicesAccessorIdx);
            this.bufferIndices = accessor.getBufferIndices(root);

        }
    }

    public Vec4[] getVertexBuffer() {
        return this.vertexBuffer;
    }

    public Vec4[] getNormalBuffer() {
        return this.normalBuffer;
    }

    public int[] getBufferIndices() {
        return this.bufferIndices;
    }
    
    public GLTFMaterial getMaterial() {
        return this.material;
    }
}
