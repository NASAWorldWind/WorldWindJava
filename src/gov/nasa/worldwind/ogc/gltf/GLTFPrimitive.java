package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFPrimitive extends GLTFArray {

    private GLTFAttributes attributes;
    private int indicesAccessorIdx;
    private int modeIdx;
    private int materialIdx;

    public GLTFPrimitive(AVListImpl properties) {
        this.indicesAccessorIdx = -1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_ATTRIBUTES:
                    this.attributes = new GLTFAttributes((AVListImpl) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_INDICES:
                    this.indicesAccessorIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MODE:
                    this.modeIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MATERIAL:
                    this.materialIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFPrimited: Unsupported " + propName);
                    break;

            }
        }
    }

    public int getVertexAccessorIdx() {
        if (this.attributes != null) {
            return this.attributes.getVertexAccessorIdx();
        }
        return -1;
    }

    public int getNormalAccessorIdx() {
        if (this.attributes != null) {
            return this.attributes.getNormalAccessorIdx();
        }
        return -1;
    }
    
    public int getVertexIndicesAccessorIdx() {
        return this.indicesAccessorIdx;
    }

}
