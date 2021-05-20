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

/**
 * Represents the COLLADA <i>instance_node</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaInstanceNode.java 1696 2013-10-31 18:46:55Z tgaskins $
 */
public class ColladaInstanceNode extends ColladaAbstractInstance<ColladaNode> implements ColladaRenderable
{
    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaInstanceNode(String ns)
    {
        super(ns);
    }

    public Box getLocalExtent(ColladaTraversalContext tc)
    {
        ColladaNode instance = this.get();

        return instance != null ? instance.getLocalExtent(tc) : null;
    }

    /** {@inheritDoc} Renders the target of the instance pointer, if the target can be resolved. */
    public void preRender(ColladaTraversalContext tc, DrawContext dc)
    {
        ColladaNode instance = this.get();
        if (instance != null)
            instance.preRender(tc, dc);
    }

    /** {@inheritDoc} Renders the target of the instance pointer, if the target can be resolved. */
    public void render(ColladaTraversalContext tc, DrawContext dc)
    {
        ColladaNode instance = this.get();
        if (instance != null)
            instance.render(tc, dc);
    }
}