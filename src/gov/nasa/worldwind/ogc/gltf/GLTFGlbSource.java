/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import java.io.ByteArrayInputStream;
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

    public GLTFDoc load() throws Exception {
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
        String json=null;
        glbBuffer.getInt(); // glb total Length
        while (glbBuffer.hasRemaining()) {
            int chunkLength = glbBuffer.getInt();
            int chunkType = glbBuffer.getInt();
            byte[] data = new byte[chunkLength];
            glbBuffer.get(data);
            if (chunkType == GLB_CHUNK_JSON) {
                json = new String(data, StandardCharsets.UTF_8);
            } else if (chunkType == GLB_CHUNK_BIN) {
                this.buffers.add(GLTFBuffer.fromBytes(data));
            } else {
                String message = Logging.getMessage("generic.InvalidImageFormat");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }
        return new GLTFDoc(new ByteArrayInputStream(json.getBytes()));
    }
    
    public ArrayList<GLTFBuffer> getBuffers() {
        return this.buffers;
    }
}
