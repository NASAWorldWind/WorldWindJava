package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.*;
import org.codehaus.jackson.JsonParser;
@TypeScriptImports(imports = "../json/JSONDoc,../json/JsonParser,../json/JSONEventParserContext,./GLTFParserContext,./GLTFRoot")

public class GLTFDoc extends JSONDoc {
    @TypeScript(skipMethod=true)
    public GLTFDoc(Object source) {
        super(source);
    }
    
    public GLTFDoc(String source) {
        super(source);
    }
    
    public JSONEventParserContext createEventParserContext(GLTFRoot gltfRoot, JsonParser parser) throws IOException
    {
        return new GLTFParserContext(gltfRoot,this.jsonParser);
    }

//    @Override
//    public GLTFEventParser createRootObjectParser() throws IOException
//    {
//        return new GLTFEventParser();
//    }
}
