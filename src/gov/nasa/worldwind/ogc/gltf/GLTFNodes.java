/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import java.io.IOException;
import java.util.ArrayList;

@TypeScriptImports(imports = "../json/BasicJSONEventParser,../json/JSONEventParserContext,../json/JSONEvent")

public class GLTFNodes extends BasicJSONEventParser {

    public GLTFNodes() {

    }

    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object parsedObject = super.parse(ctx, event);
        if (parsedObject instanceof AVListImpl) {
            return new GLTFNode((AVListImpl) parsedObject);
        }
        return parsedObject;
    }
}
