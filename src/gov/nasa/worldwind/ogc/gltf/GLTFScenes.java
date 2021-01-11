package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "../json/BasicJSONEventParser,../json/JSONEventParserContext,../json/JSONEvent")

public class GLTFScenes extends BasicJSONEventParser {

    private GLTFNodes nodes;

    @Override
    protected Object parseComplexContent(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        String fieldName = ctx.getCurrentFieldName();
        if (fieldName.equals(GLTFParserContext.KEY_NODES)) {
            JSONEventParser parser = ctx.getUnrecognizedParser();
            return parser.parse(ctx, event);
        }
        return super.parseComplexContent(ctx, event);
    }

    @Override
    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object parsedObject = super.parse(ctx, event);
        if (parsedObject instanceof AVListImpl) {
            return new GLTFScene((AVListImpl) parsedObject);
        }
        return parsedObject;
    }
}
