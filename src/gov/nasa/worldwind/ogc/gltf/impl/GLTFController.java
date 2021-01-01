/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.gltf.impl;

import gov.nasa.worldwind.ogc.gltf.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.typescript.*;

@TypeScriptImports(imports="../../../render/IRenderable,../../../render/PreRenderable,../../../render/DrawContext,../../../util/Logger,../GLTFRoot,./GLTFTraversalContext")

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
}
