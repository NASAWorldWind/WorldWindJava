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
package gov.nasa.worldwind.ogc.gltf.impl;

import gov.nasa.worldwind.ogc.gltf.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.typescript.*;

@TypeScriptImports(imports="../../../shapes/ShapeAttributes,../../../render/IRenderable,../../../render/PreRenderable,../../../render/DrawContext,../../../util/Logger,../GLTFRoot,./GLTFTraversalContext")

/**
 * Executes the mapping from GLTF to WorldWind. Traverses a parsed GLTF document and creates the appropriate
 * WorldWind object to represent the GLTF model.
 */
@TypeScript(substitute="Renderable,|IRenderable,")
public class GLTFController  implements Renderable, PreRenderable {
    /**
     * GLTF document rendered by this controller.
     */
    protected GLTFRoot gltfRoot;
    /**
     * Traversal context used to render the document.
     */
    protected GLTFTraversalContext tc;

    /**
     * Create a new controller to render a GLTF document.
     *
     * @param root Parsed GLTF document to render.
     */
    public GLTFController(GLTFRoot root) {
        this.setGLTFRoot(root);
        this.setTraversalContext(new GLTFTraversalContext());
    }
    
    /**
     * Specifies the GLTF document that this controller will render.
     *
     * @param gltfRoot New GLTF document to render.
     */
    public void setGLTFRoot(GLTFRoot gltfRoot) {
        if (gltfRoot == null) {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.gltfRoot = gltfRoot;
    }

    /**
     * Specifies a traversal context to use while rendering the COLLADA document.
     *
     * @param tc New traversal context.
     */
    public void setTraversalContext(GLTFTraversalContext tc) {
        if (tc == null) {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.tc = tc;
    }

    /**
     * Initializes this COLLADA controller's traversal context to its default state. A COLLADA traversal context must be
     * initialized prior to use during preRendering or rendering, to ensure that state from the previous pass does not
     * affect the current pass.
     *
     * @param tc the COLLADA traversal context to initialize.
     */
    protected void initializeTraversalContext(GLTFTraversalContext tc) {
        tc.initialize();
    }
    
    public void setAttributes(ShapeAttributes attrs) {
        this.gltfRoot.setAttributes(attrs);
    }
    
    /**
     * Indicates the traversal context used to render the COLLADA document.
     *
     * @return The active traversal context.
     */
    public GLTFTraversalContext getTraversalContext() {
        return this.tc;
    }

    /**
     * {@inheritDoc}
     */
    public void preRender(DrawContext dc) {
        this.initializeTraversalContext(this.getTraversalContext());
        this.gltfRoot.preRender(this.getTraversalContext(), dc);
    }

    /**
     * {@inheritDoc}
     */
    public void render(DrawContext dc) {
        this.initializeTraversalContext(this.getTraversalContext());
        this.gltfRoot.render(this.getTraversalContext(), dc);
    }
    
    public String getDisplayName() {
        return "GLTF Controller";
    }
}
