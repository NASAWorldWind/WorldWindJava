package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFScene extends GLTFArray {

    private int[] nodeIndices;

    public GLTFScene(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_NODES:
                    this.nodeIndices = GLTFUtil.retrieveIntArray((Object[]) properties.getValue(propName));
                    break;
                default:
                    System.out.println("Unsupported");
                    break;

            }
        }
    }
    
    public GLTFNode[] getSceneNodes(GLTFNode[] allNodes) {
        GLTFNode[] nodes=new GLTFNode[nodeIndices.length];
        for (int i=0; i<this.nodeIndices.length; i++) {
            nodes[i]=allNodes[this.nodeIndices[i]];
        }
        return nodes;
    }
}
