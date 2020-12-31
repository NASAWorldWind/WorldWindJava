package gov.nasa.worldwind.ogc.gltf;

import java.io.IOException;

import gov.nasa.worldwind.formats.json.*;

public class GLTFDoc extends JSONDoc {
    public GLTFDoc(Object source) {
        super(source);
    }
    
    public GLTFParserContext createEventParserContext() throws IOException
    {
        return new GLTFParserContext(this.jsonParser);
    }

//    @Override
//    public GLTFEventParser createRootObjectParser() throws IOException
//    {
//        return new GLTFEventParser();
//    }
}
