/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.formats.json.*;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;

@TypeScriptImports(imports = "../json/BasicJSONEventParser")

public class GLTFAbstractObject extends BasicJSONEventParser {
    public GLTFAbstractObject() {
        super();
    }
    
}
