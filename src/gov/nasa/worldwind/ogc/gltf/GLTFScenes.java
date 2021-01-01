package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;

public class GLTFScenes extends BasicJSONEventParser {
    @Override
    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException
    {
        Object foo=super.parse(ctx, event);
        System.out.println(foo);
        return foo;
    }

}
