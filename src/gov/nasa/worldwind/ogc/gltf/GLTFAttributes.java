package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFAttributes extends GLTFArray {

    private int posAccessorIdx;
    private int normalAccessorIdx;

    public GLTFAttributes(AVListImpl properties) {
        this.normalAccessorIdx = -1;
        this.posAccessorIdx = -1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_POSITION:
                    this.posAccessorIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_NORMAL:
                    this.normalAccessorIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFAttributes: Unsupported " + propName);
                    break;
            }
        }

    }

    public int getVertexAccessorIdx() {
        return this.posAccessorIdx;
    }

    public int getNormalAccessorIdx() {
        return this.normalAccessorIdx;
    }

}
