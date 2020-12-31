package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;
import org.codehaus.jackson.JsonParser;

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
