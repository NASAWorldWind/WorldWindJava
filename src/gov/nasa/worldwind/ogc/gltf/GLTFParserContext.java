package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import org.codehaus.jackson.JsonParser;
@TypeScriptImports(imports = "../json/JsonParser,../json/BasicJSONEventParserContext,./GLTFScene,./GLTFScenes")

public class GLTFParserContext extends BasicJSONEventParserContext {
    public GLTFParserContext(JsonParser parser) throws IOException {
        super(parser);
        this.initializeParsers();
    }
    protected void initializeParsers()
    {
        this.registerParser("scene", new GLTFScene());
        this.registerParser("scenes", new GLTFScenes());
    }
}
