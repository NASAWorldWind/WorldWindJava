package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.json.BasicJSONEventParser;
import gov.nasa.worldwind.formats.json.JSONEvent;
import gov.nasa.worldwind.formats.json.JSONEventParserContext;
import java.io.IOException;

public class GLTFAttributes extends BasicJSONEventParser {

    private int posAccessorIdx;

    public GLTFAttributes() {
    }

    public GLTFAttributes(AVListImpl properties) {
        this.posAccessorIdx = -1;
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_POSITION:
                    this.posAccessorIdx = GLTFUtil.getInt(properties.getValue(propName));
                    break;
                default:
                    System.out.println("Unknown property: " + propName);
                    break;
            }
        }

    }

    public int getVertexAccessorIdx() {
        return this.posAccessorIdx;
    }

    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object parsedObject = super.parse(ctx, event);
        if (parsedObject instanceof AVListImpl) {
            return new GLTFAttributes((AVListImpl) parsedObject);
        }
        return parsedObject;
    }

}
