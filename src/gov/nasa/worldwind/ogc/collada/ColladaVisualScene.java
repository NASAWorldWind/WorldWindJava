/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.ogc.collada.impl.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Represents the COLLADA <i>visual_scene</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaVisualScene.java 1696 2013-10-31 18:46:55Z tgaskins $
 */
public class ColladaVisualScene extends ColladaAbstractObject implements ColladaRenderable
{
    /** Nodes in this scene. */
    protected List<ColladaNode> nodes = new ArrayList<ColladaNode>();

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaVisualScene(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the nodes in the scene.
     *
     * @return List of nodes. May return an empty list, but never returns null.
     */
    public List<ColladaNode> getNodes()
    {
        return this.nodes;
    }

    /** {@inheritDoc} */
    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("node"))
        {
            this.nodes.add((ColladaNode) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    @Override
    public Box getLocalExtent(ColladaTraversalContext tc)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<Box> extents = new ArrayList<Box>();
        for (ColladaNode node : this.getNodes())
        {
            Box extent = node.getLocalExtent(tc);
            if (extent != null)
                extents.add(extent);
        }

        return extents.isEmpty() ? null : Box.union(extents);
    }

    /** {@inheritDoc} Renders all nodes in this scene. */
    public void preRender(ColladaTraversalContext tc, DrawContext dc)
    {
        for (ColladaNode node : this.getNodes())
        {
            node.preRender(tc, dc);
        }
    }

    /** {@inheritDoc} Renders all nodes in this scene. */
    public void render(ColladaTraversalContext tc, DrawContext dc)
    {
        for (ColladaNode node : this.getNodes())
        {
            node.render(tc, dc);
        }
    }
}
