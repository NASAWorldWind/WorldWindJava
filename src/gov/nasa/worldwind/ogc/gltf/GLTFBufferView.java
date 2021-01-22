package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFBufferView  extends GLTFArray  {
    private int buffer;
    private int byteOffset;
    private int byteLength;
    private int target;
    private int byteStride;
    
    public GLTFBufferView(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_BUFFER:
                    this.buffer=GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_BYTE_OFFSET:
                    this.byteOffset=GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_BYTE_LENGTH:
                    this.byteLength=GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_TARGET:
                    this.target=GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_BYTE_STRIDE:
                    this.byteStride=GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFBufferView: Unsupported "+propName);
                    break;
            }
        }
    }
    
    public byte[] getViewData(GLTFRoot root,int accessorOffset) {
        GLTFBuffer buffer=root.getBufferForIdx(this.buffer);
        return buffer.getBytes(this.byteOffset+accessorOffset,this.byteLength);
    }
   
}
