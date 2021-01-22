package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFCamera {
    
    protected String type;
    protected GLTFPerspective perspective;
    protected double[] emissiveFactor;
    
    public GLTFCamera(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_PERSPECTIVE:
                    this.perspective = new GLTFPerspective((AVListImpl) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_TYPE:
                    this.type = properties.getStringValue(propName);
                    break;
                case GLTFParserContext.KEY_EMISSIVE_FACTOR:
                    this.emissiveFactor=GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFCamera: Unsupported "+propName);
                    break;
            }
        }
    }
    
}
