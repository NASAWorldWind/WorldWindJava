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
