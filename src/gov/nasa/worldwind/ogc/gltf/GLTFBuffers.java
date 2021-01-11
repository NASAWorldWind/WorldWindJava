package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.json.BasicJSONEventParser;
import gov.nasa.worldwind.formats.json.JSONEvent;
import gov.nasa.worldwind.formats.json.JSONEventParserContext;
import java.io.IOException;

public class GLTFBuffers extends BasicJSONEventParser {
    @Override
    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object parsedObject = super.parse(ctx, event);
        if (parsedObject instanceof AVListImpl) {
            return new GLTFBuffer((AVListImpl) parsedObject);
        }
        return parsedObject;
    }
    
}
