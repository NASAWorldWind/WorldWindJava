package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import java.util.ArrayList;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class GLTFGlbSource {

    public static int GLB_MAGIC = 0x46546C67;
    public static int GLB_CHUNK_JSON = 0x4E4F534A;
    public static int GLB_CHUNK_BIN = 0x004E4942;
    public static int GLB_VERSION = 0x2;

    private String source;
    protected String json;
    protected ArrayList<GLTFBuffer> buffers;

    public GLTFGlbSource(String source) {
        if (WWUtil.isEmpty(source)) {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.source = source;
        this.buffers = new ArrayList<>();
    }

    public void load() throws Exception {
        byte[] rawBuffer;
        try (InputStream glbStream = WWIO.openStream(source)) {
            rawBuffer = glbStream.readAllBytes();
        }
        ByteBuffer glbBuffer = ByteBuffer.wrap(rawBuffer);
        glbBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int magic = glbBuffer.getInt();
        int version = glbBuffer.getInt();
        if (magic != GLB_MAGIC || version != GLB_VERSION) {
            String message = Logging.getMessage("generic.InvalidImageFormat");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        glbBuffer.getInt(); // glb total Length
        while (glbBuffer.hasRemaining()) {
            int chunkLength = glbBuffer.getInt();
            int chunkType = glbBuffer.getInt();
            byte[] data = new byte[chunkLength];
            glbBuffer.get(data);
            if (chunkType == GLB_CHUNK_JSON) {
                this.json = new String(data, StandardCharsets.UTF_8);
            } else if (chunkType == GLB_CHUNK_BIN) {
                this.buffers.add(GLTFBuffer.fromBytes(data));
            } else {
                String message = Logging.getMessage("generic.InvalidImageFormat");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }
    }
    
    public String getJson() {
        return this.json;
    }
    
    public ArrayList<GLTFBuffer> getBuffers() {
        return this.buffers;
    }
}
