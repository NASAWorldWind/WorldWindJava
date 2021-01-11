package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFPrimitive extends GLTFArray {
    
    private GLTFAttributes attributes;
    private int indices;
    
    public GLTFPrimitive(AVListImpl properties) {
        this.indices=-1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_ATTRIBUTES:
                    this.attributes = (GLTFAttributes) properties.getValue(propName);
                    break;
                case GLTFParserContext.KEY_INDICES:
                    this.indices=GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("Unsupported");
                    break;

            }
        }
    }
    
    public int getVertexAccessorIdx() {
        if (this.attributes!=null) {
            return this.attributes.getVertexAccessorIdx();
        }
        return -1;
    }
    
    public int getVertexIndicesAccessorIdx() {
        return this.indices;
    }
    
}
