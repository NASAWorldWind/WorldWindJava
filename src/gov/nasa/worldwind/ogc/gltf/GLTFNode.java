package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFNode extends GLTFArray {

    private int meshIdx;
    
    public GLTFNode(AVListImpl properties) {
        this.meshIdx = -1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_MESH:
                    this.meshIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("Unknown property: " + propName);
                    break;
            }
        }
    }
    
    public void assembleGeometry(GLTFRoot root) {
        if (meshIdx<0) {
            return;
        }
        
        GLTFMesh mesh=root.getMeshForIdx(meshIdx);
        mesh.assembleGeometry(root);
    }

}
