package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFPBRMetallicRoughness extends GLTFArray {

    private double[] baseColorFactor;
    private double metallicFactor;
    private double roughnessFactor;

    public GLTFPBRMetallicRoughness(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_BASE_COLOR_FACTOR:
                    this.baseColorFactor = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_METALLIC_FACTOR:
                    this.metallicFactor = GLTFUtil.getDouble(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_ROUGHNESS_FACTOR:
                    this.roughnessFactor = GLTFUtil.getDouble(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFPBRMetallicRoughness: Unsupported " + propName);
                    break;
            }
        }
    }

    public double[] getBaseColorFactor() {
        return this.baseColorFactor;
    }
}
