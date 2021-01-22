package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.json.BasicJSONEventParser;
import gov.nasa.worldwind.formats.json.JSONEvent;
import gov.nasa.worldwind.formats.json.JSONEventParserContext;
import java.io.IOException;

public class GLTFAsset extends BasicJSONEventParser {

    private String version;
    private String generator;

    public GLTFAsset() {

    }

    public GLTFAsset(AVListImpl properties) {
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_VERSION:
                    this.version = properties.getStringValue(propName);
                    break;
                case GLTFParserContext.KEY_GENERATOR:
                    this.generator=properties.getStringValue(propName);
                    break;
                default:
                    System.out.println("GLTFAsset: Unsupported "+propName);
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
