package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import org.codehaus.jackson.JsonParser;

@TypeScriptImports(imports = "../json/JsonParser,../json/BasicJSONEventParserContext,./GLTFRoot")

public class GLTFParserContext extends BasicJSONEventParserContext {

    public static final String KEY_NODES = "nodes";
    public static final String KEY_SCENE = "scene";
    public static final String KEY_SCENES = "scenes";
    public static final String KEY_MESH = "mesh";
    public static final String KEY_MESHES = "meshes";
    public static final String KEY_PRIMITIVES = "primitives";
    public static final String KEY_ATTRIBUTES = "attributes";
    public static final String KEY_NORMAL = "NORMAL";
    public static final String KEY_POSITION = "POSITION";
    public static final String KEY_INDICES = "indices";
    public static final String KEY_URI = "uri";
    public static final String KEY_BYTE_LENGTH = "byteLength";
    public static final String KEY_BUFFERS = "buffers";
    public static final String KEY_BUFFER = "buffer";
    public static final String KEY_BYTE_OFFSET = "byteOffset";
    public static final String KEY_TARGET = "target";
    public static final String KEY_BUFFER_VIEW = "bufferView";
    public static final String KEY_BUFFER_VIEWS = "bufferViews";
    public static final String KEY_ACCESSORS = "accessors";
    public static final String KEY_COMPONENT_TYPE = "componentType";
    public static final String KEY_COUNT = "count";
    public static final String KEY_TYPE = "type";
    public static final String KEY_MIN = "min";
    public static final String KEY_MAX = "max";
    public static final String KEY_ASSET = "asset";
    public static final String KEY_VERSION = "version";
    public static final String KEY_MATRIX = "matrix";
    public static final String KEY_CHILDREN = "children";
    public static final String KEY_MODE = "mode";
    public static final String KEY_MATERIAL = "material";
    public static final String KEY_MATERIALS = "materials";
    public static final String KEY_NAME = "name";
    public static final String KEY_PBR_METALLIC_ROUGHNESS = "pbrMetallicRoughness";
    public static final String KEY_BASE_COLOR_FACTOR = "baseColorFactor";
    public static final String KEY_METALLIC_FACTOR = "metallicFactor";
    public static final String KEY_GENERATOR = "generator";
    public static final String KEY_BYTE_STRIDE = "byteStride";
    public static final String KEY_EMISSIVE_FACTOR = "emissiveFactor";
    public static final String KEY_CAMERA = "camera";
    public static final String KEY_CAMERAS = "cameras";
    public static final String KEY_PERSPECTIVE = "perspective";
    public static final String KEY_ASPECT_RATIO = "aspectRatio";
    public static final String KEY_YFOV = "yfov";
    public static final String KEY_ZFAR = "zfar";
    public static final String KEY_ZNEAR = "znear";
    public static final String KEY_ROTATION = "rotation";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_TRANSLATION = "translation";
    public static final String KEY_ROUGHNESS_FACTOR = "roughnessFactor";
    public static final String KEY_COPYRIGHT = "copyright";
    public static final String KEY_SCALAR = "SCALAR";
    public static final String KEY_VEC3 = "VEC3";

    private GLTFRoot gltfRoot;

    public GLTFParserContext(GLTFRoot gltfRoot, JsonParser parser) throws IOException {
        super(parser);
        this.gltfRoot = gltfRoot;
    }

    public GLTFRoot getGLTFRoot() {
        return this.gltfRoot;
    }
}
