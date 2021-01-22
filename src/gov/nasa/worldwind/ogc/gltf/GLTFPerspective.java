package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFPerspective {

    protected double aspectRatio;
    protected double yFOV;
    protected double zFar;
    protected double zNear;

    public GLTFPerspective(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_ASPECT_RATIO:
                    this.aspectRatio = GLTFUtil.getDouble(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_YFOV:
                    this.yFOV = GLTFUtil.getDouble(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_ZFAR:
                    this.zFar = GLTFUtil.getDouble(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_ZNEAR:
                    this.zNear = GLTFUtil.getDouble(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFPerspective: Unsupported " + propName);
                    break;
            }
        }
    }

}
