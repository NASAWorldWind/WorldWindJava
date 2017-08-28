/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada.impl;

import gov.nasa.worldwind.ogc.collada.ColladaRoot;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

/**
 * Executes the mapping from COLLADA to WorldWind. Traverses a parsed COLLADA document and creates the appropriate
 * WorldWind object to represent the COLLADA model.
 *
 * @author pabercrombie
 * @version $Id: ColladaController.java 661 2012-06-26 18:02:23Z pabercrombie $
 */
public class ColladaController implements Renderable, PreRenderable
{
    /** Collada document rendered by this controller. */
    protected ColladaRoot colladaRoot;
    /** Traversal context used to render the document. */
    protected ColladaTraversalContext tc;

    /**
     * Create a new controller to render a COLLADA document.
     *
     * @param root Parsed COLLADA document to render.
     */
    public ColladaController(ColladaRoot root)
    {
        this.setColladaRoot(root);
        this.setTraversalContext(new ColladaTraversalContext());
    }

    /**
     * Indicates the COLLADA document that this controller will render.
     *
     * @return The COLLADA document referenced by this controller.
     */
    public ColladaRoot getColladaRoot()
    {
        return this.colladaRoot;
    }

    /**
     * Specifies the COLLADA document that this controller will render.
     *
     * @param colladaRoot New COLLADA document to render.
     */
    public void setColladaRoot(ColladaRoot colladaRoot)
    {
        if (colladaRoot == null)
        {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.colladaRoot = colladaRoot;
    }

    /**
     * Indicates the traversal context used to render the COLLADA document.
     *
     * @return The active traversal context.
     */
    public ColladaTraversalContext getTraversalContext()
    {
        return this.tc;
    }

    /**
     * Specifies a traversal context to use while rendering the COLLADA document.
     *
     * @param tc New traversal context.
     */
    public void setTraversalContext(ColladaTraversalContext tc)
    {
        if (tc == null)
        {
            String msg = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.tc = tc;
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        this.initializeTraversalContext(this.getTraversalContext());
        this.colladaRoot.preRender(this.getTraversalContext(), dc);
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        this.initializeTraversalContext(this.getTraversalContext());
        this.colladaRoot.render(this.getTraversalContext(), dc);
    }

    /**
     * Initializes this COLLADA controller's traversal context to its default state. A COLLADA traversal context must be
     * initialized prior to use during preRendering or rendering, to ensure that state from the previous pass does not
     * affect the current pass.
     *
     * @param tc the COLLADA traversal context to initialize.
     */
    protected void initializeTraversalContext(ColladaTraversalContext tc)
    {
        tc.initialize();
    }
}
