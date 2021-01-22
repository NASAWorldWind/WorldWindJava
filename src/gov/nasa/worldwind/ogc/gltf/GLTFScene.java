package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFScene extends GLTFArray {

    private int[] nodeIndices;
    private GLTFNode[] nodes;

    public GLTFScene(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_NODES:
                    this.nodeIndices = GLTFUtil.retrieveIntArray((Object[]) properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFScene: Unsupported "+propName);
                    break;

            }
        }
    }
    
    public GLTFNode[] getNodes() {
        return this.nodes;
    }

    public GLTFNode[] setSceneNodes(GLTFRoot root) {
        GLTFNode[] allNodes = root.getNodes();
        this.nodes = new GLTFNode[nodeIndices.length];
        for (int i = 0; i < this.nodeIndices.length; i++) {
            nodes[i] = allNodes[this.nodeIndices[i]];
        }
        return nodes;
    }
}
