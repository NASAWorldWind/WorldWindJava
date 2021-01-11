package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import org.codehaus.jackson.JsonParser;
@TypeScriptImports(imports = "../json/JsonParser,../json/BasicJSONEventParserContext,./GLTFScene,./GLTFScenes")

public class GLTFParserContext extends BasicJSONEventParserContext {
    public static final String KEY_NODES="nodes";
    public static final String KEY_SCENE="scene";
    public static final String KEY_SCENES="scenes";
    public static final String KEY_MESH="mesh";
    public static final String KEY_MESHES="meshes";
    public static final String KEY_PRIMITIVES="primitives";
    public static final String KEY_ATTRIBUTES="attributes";
    public static final String KEY_POSITION="POSITION";
    public static final String KEY_INDICES="indices";
    public static final String KEY_URI="uri";
    public static final String KEY_BYTE_LENGTH="byteLength";
    public static final String KEY_BUFFERS="buffers";
    public static final String KEY_BUFFER="buffer";
    public static final String KEY_BYTE_OFFSET="byteOffset";
    public static final String KEY_TARGET="target";
    public static final String KEY_BUFFER_VIEW="bufferView";
    public static final String KEY_BUFFER_VIEWS="bufferViews";
    public static final String KEY_ACCESSORS="accessors";
    public static final String KEY_COMPONENT_TYPE="componentType";
    public static final String KEY_COUNT="count";
    public static final String KEY_TYPE="type";
    public static final String KEY_MIN="min";
    public static final String KEY_MAX="max";
    public static final String KEY_ASSET="asset";
    public static final String KEY_VERSION="version";
    
    private GLTFRoot gltfRoot;
    
    public GLTFParserContext(GLTFRoot gltfRoot,JsonParser parser) throws IOException {
        super(parser);
        this.gltfRoot=gltfRoot;
        this.initializeParsers();
    }
    
    protected void initializeParsers()
    {
        this.registerParser(GLTFParserContext.KEY_NODES, new GLTFNodes());
        this.registerParser(GLTFParserContext.KEY_SCENES, new GLTFScenes());
        this.registerParser(GLTFParserContext.KEY_MESHES, new GLTFMeshes());
        this.registerParser(GLTFParserContext.KEY_PRIMITIVES, new GLTFPrimitives());
        this.registerParser(GLTFParserContext.KEY_ATTRIBUTES, new GLTFAttributes());
        this.registerParser(GLTFParserContext.KEY_BUFFERS, new GLTFBuffers());
        this.registerParser(GLTFParserContext.KEY_BUFFER_VIEWS, new GLTFBufferViews());
        this.registerParser(GLTFParserContext.KEY_ACCESSORS, new GLTFAccessors());
        this.registerParser(GLTFParserContext.KEY_ASSET, new GLTFAsset());
    }
    
    public GLTFRoot getGLTFRoot() {
        return this.gltfRoot;
    }
}
