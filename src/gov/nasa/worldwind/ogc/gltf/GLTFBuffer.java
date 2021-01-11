package gov.nasa.worldwind.ogc.gltf;

import java.util.*;

import gov.nasa.worldwind.avlist.AVListImpl;

public class GLTFBuffer extends GLTFArray {

    private String uri;
    private int byteLength;
    private byte[] byteData;

    public GLTFBuffer(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_URI:
                    this.uri = (String) properties.getValue(propName);
                    if (this.uri.startsWith("data:")) {
                        this.unpackData();
                    }
                    break;
                case GLTFParserContext.KEY_BYTE_LENGTH:
                    this.byteLength = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("Unsupported");
                    break;
            }
        }
    }

    private void unpackData() {
        StringTokenizer st = new StringTokenizer(this.uri, " ,");
        String descriptor = st.nextToken();
        String data = st.nextToken();
        st = new StringTokenizer(descriptor, " :;");
        st.nextToken();
        String dataType = st.nextToken();
        String encoding = st.nextToken();
        switch (encoding) {
            case "base64":
                this.byteData = Base64.getDecoder().decode(data);
                break;
            default:
                System.out.println("Unsupported.");
                break;
        }
    }

    public byte[] getBytes(int offset, int length) {
        return Arrays.copyOfRange(this.byteData, offset, offset + length);
    }

}
