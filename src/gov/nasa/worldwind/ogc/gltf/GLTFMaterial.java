package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFArray,./GLTFPBRMetallicRoughness,./GLTFParserContext,./GLTFUtil,../../avlist/AVListImpl")
public class GLTFMaterial extends GLTFArray {

    private String name;
    private GLTFPBRMetallicRoughness pbrMetallicRoughness;
    private double[] emissiveFactor;

    public GLTFMaterial(AVListImpl properties) {
        super();
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_PBR_METALLIC_ROUGHNESS:
                    this.pbrMetallicRoughness = new GLTFPBRMetallicRoughness((AVListImpl) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_NAME:
                    this.name = properties.getStringValue(propName);
                    break;
                case GLTFParserContext.KEY_EMISSIVE_FACTOR:
                    this.emissiveFactor = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFMaterial: Unsupported " + propName);
                    break;
            }
        }
    }

    public GLTFPBRMetallicRoughness getPbrMetallicRoughness() {
        return this.pbrMetallicRoughness;
    }

}
