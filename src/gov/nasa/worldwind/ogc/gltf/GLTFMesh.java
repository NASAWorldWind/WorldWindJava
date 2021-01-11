package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFMesh extends GLTFArray {

    private GLTFPrimitive[] primitives;
    private float[] vertexBuffer;
    private int[] vertexIndices;

    public GLTFMesh(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_PRIMITIVES:
                    Object[] sourceArray = (Object[]) properties.getValue(propName);
                    this.primitives = new GLTFPrimitive[sourceArray.length];
                    for (int i = 0; i < sourceArray.length; i++) {
                        this.primitives[i] = (GLTFPrimitive) sourceArray[i];
                    }
                    break;
                default:
                    System.out.println("Unsupported");
                    break;
            }
        }
    }
    
    public void assembleGeometry(GLTFRoot root) {
        for (GLTFPrimitive primitive:this.primitives) {
            int vertexAccessorIdx=primitive.getVertexAccessorIdx();
            GLTFAccessor accessor=root.getAccessorForIdx(vertexAccessorIdx);
            this.vertexBuffer=accessor.getVertexBuffer(root);
            
            int vertexIndicesAccessorIdx=primitive.getVertexIndicesAccessorIdx();
            accessor=root.getAccessorForIdx(vertexIndicesAccessorIdx);
            this.vertexIndices=accessor.getVertexIndices(root);
            
        }
    }

}
