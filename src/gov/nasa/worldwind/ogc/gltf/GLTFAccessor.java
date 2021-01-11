package gov.nasa.worldwind.ogc.gltf;

import java.util.*;
import java.nio.*;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFAccessor extends GLTFArray {

    public static final int COMPONENT_BYTE = 5120; // (BYTE) 	1
    public static final int COMPONENT_UNSIGNED_BYTE = 5121; //(UNSIGNED_BYTE) 	1
    public static final int COMPONENT_SHORT = 5122; // (SHORT) 	2
    public static final int COMPONENT_UNSIGNED_SHORT = 5123; // (UNSIGNED_SHORT) 	2
    public static final int COMPONENT_UNSIGNED_INT = 5125; // (UNSIGNED_INT) 	4
    public static final int COMPONENT_FLOAT = 5126; // (FLOAT) 	4

    public enum AccessorType {
        SCALAR, VEC3
    };
    private int bufferView;
    private int byteOffset;
    private int componentType;
    private int count;
    private AccessorType type;
    private double[] max;
    private double[] min;

    public GLTFAccessor(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_BUFFER_VIEW:
                    this.bufferView = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_BYTE_OFFSET:
                    this.byteOffset = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_COMPONENT_TYPE:
                    this.componentType = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_COUNT:
                    this.count = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_TYPE:
                    String value = properties.getValue(propName).toString();
                    this.type = AccessorType.valueOf(value);
                    break;
                case GLTFParserContext.KEY_MAX:
                    this.max = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                case GLTFParserContext.KEY_MIN:
                    this.min = GLTFUtil.retrieveDoubleArray((Object[]) properties.getValue(propName));
                    break;
                default:
                    System.out.println("Unsupported");
                    break;
            }
        }
    }
    
    private ByteBuffer retrieveByteBuffer(GLTFRoot root) {
        GLTFBufferView view = root.getBufferViewForIdx(this.bufferView);
        byte[] srcBuffer = view.getViewData(root, this.byteOffset);
        ByteBuffer viewBuffer = ByteBuffer.allocate(srcBuffer.length);
        viewBuffer.put(srcBuffer,0,srcBuffer.length);
        viewBuffer.rewind();
        viewBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return viewBuffer;
    }

    public float[] getVertexBuffer(GLTFRoot root) {
        ByteBuffer srcBuffer = this.retrieveByteBuffer(root);
        float[] ret = null;
        switch (this.componentType) {
            case COMPONENT_FLOAT:
                FloatBuffer floatBuffer = srcBuffer.asFloatBuffer();
                int n = floatBuffer.limit();
                ret = new float[n];
                for (int i = 0; i < n; i++) {
                    ret[i] = floatBuffer.get();
                }
                break;
            default:
                System.out.println("Unsupported");
                break;
        }
        return ret;
    }
    
    public int[] getVertexIndices(GLTFRoot root) {
        ByteBuffer srcBuffer = this.retrieveByteBuffer(root);
        int[] ret = null;
        switch (this.componentType) {
            case COMPONENT_UNSIGNED_SHORT:
                ShortBuffer shortBuffer = srcBuffer.asShortBuffer();
                int n = shortBuffer.limit();
                ret = new int[n];
                for (int i = 0; i < n; i++) {
                    ret[i] = shortBuffer.get();
                }
                break;
            default:
                System.out.println("Unsupported");
                break;
        }
        return ret;
    }
}
