/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import java.io.IOException;

@TypeScriptImports(imports = "../json/BasicJSONEventParser,../json/JSONEventParserContext,../json/JSONEvent")

public class GLTFScene extends BasicJSONEventParser {

    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object foo = super.parse(ctx, event);
        System.out.println(foo);
        return foo;
    }
}
