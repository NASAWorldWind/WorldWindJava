/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.ogc.gltf;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.json.BasicJSONEventParser;
import gov.nasa.worldwind.formats.json.JSONEvent;
import gov.nasa.worldwind.formats.json.JSONEventParserContext;
import gov.nasa.worldwind.util.typescript.TypeScriptImports;
import java.io.IOException;

@TypeScriptImports(imports = "./GLTFUtil,../json/JSONEvent,../json/JSONEventParserContext,../json/BasicJSONEventParser,./GLTFParserContext,../../avlist/AVListImpl")
public class GLTFAsset extends BasicJSONEventParser {

    private String version;
    private String generator;
    private String copyright;

    public GLTFAsset(AVListImpl properties) {
        super();
        for (String propName : properties.getKeys()) {
            switch (propName) {
                case GLTFParserContext.KEY_VERSION:
                    this.version = properties.getStringValue(propName);
                    break;
                case GLTFParserContext.KEY_GENERATOR:
                    this.generator = properties.getStringValue(propName);
                    break;
                case GLTFParserContext.KEY_COPYRIGHT:
                    this.copyright = properties.getStringValue(propName);
                    break;
                default:
                    System.out.println("GLTFAsset: Unsupported " + propName);
                    break;
            }
        }
    }

    @Override
    public Object parse(JSONEventParserContext ctx, JSONEvent event) throws IOException {
        Object parsedObject = super.parse(ctx, event);
        if (parsedObject instanceof AVListImpl) {
            return new GLTFAsset(GLTFUtil.asAVList(parsedObject));
        }
        return parsedObject;
    }

}
