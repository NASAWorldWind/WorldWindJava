package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFParserContext,../../avlist/AVListImpl")
public class GLTFScene extends GLTFArray {

    private int[] nodeIndices;
    private GLTFNode[] nodes;
    private String name;

    public GLTFScene(AVListImpl properties) {
        super();
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_NODES:
                    this.nodeIndices = GLTFUtil.retrieveIntArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_NAME:
                    this.name = properties.getStringValue(propName);
                    break;
                default:
                    System.out.println("GLTFScene: Unsupported " + propName);
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
