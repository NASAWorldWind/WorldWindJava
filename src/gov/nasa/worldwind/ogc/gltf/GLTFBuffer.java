package gov.nasa.worldwind.ogc.gltf;

import java.util.*;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.typescript.TypeScript;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "./GLTFArray,./GLTFParserContext,./GLTFUtil,../../util/java/StringTokenizer,../../util/java/Base64,../../avlist/AVListImpl")

public class GLTFBuffer extends GLTFArray {

    private String uri;
    private int byteLength;
    private byte[] byteData;

    public GLTFBuffer(AVListImpl properties) {
        super();
        if (properties == null) {
            return;
        }
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_URI:
                    this.uri = properties.getValue(propName).toString();
                    if (this.uri.startsWith("data:")) {
                        this.unpackData();
                    } else {
                        System.out.println("Unsupported URI: " + this.uri);
                    }
                    break;
                case GLTFParserContext.KEY_BYTE_LENGTH:
                    this.byteLength = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("GLTFBuffer: Unsupported " + propName);
                    break;
            }
        }
    }

    @TypeScript(substitute = "Base64.getDecoder().decode(data)|Base64.decode(data)")
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
                System.out.println("GLTFBuffer: Unsupported encoding " + encoding);
                break;
        }
    }

    @TypeScript(substitute = "Arrays.copyOfRange(this.byteData, offset, offset + length)|this.byteData.slice(offset, offset + length)")
    public byte[] getBytes(int offset, int length) {
        return Arrays.copyOfRange(this.byteData, offset, offset + length);
    }

    public static GLTFBuffer fromBytes(byte[] data) {
        GLTFBuffer ret = new GLTFBuffer(null);
        ret.byteLength = data.length;
        ret.byteData = data;
        return ret;
    }

}
