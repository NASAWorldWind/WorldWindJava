package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.json.BasicJSONEventParser;
import gov.nasa.worldwind.formats.json.JSONEvent;
import gov.nasa.worldwind.formats.json.JSONEventParserContext;
import java.io.IOException;

public class GLTFAsset extends BasicJSONEventParser {

    private String version;

    public GLTFAsset() {

    }

    public GLTFAsset(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_VERSION:
                    this.version = properties.getValue(propName).toString();
                    break;
                default:
                    System.out.println("Unsupported");
                    break;
            }
        }
    }

    @Override
    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object parsedObject = super.parse(ctx, event);
        if (parsedObject instanceof AVListImpl) {
            return new GLTFAsset((AVListImpl) parsedObject);
        }
        return parsedObject;
    }

}
