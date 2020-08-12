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

/**
 * Represents the COLLADA <i>scene</i> element and provides access to its contents.
 *
 * @author pabercrombie
 * @version $Id: ColladaScene.java 1696 2013-10-31 18:46:55Z tgaskins $
 */
public class ColladaScene extends ColladaAbstractObject implements ColladaRenderable
{
    /** Flag to indicate that the scene has been fetched from the hash map. */
    protected boolean sceneFetched = false;
    /** Cached value of the <i>instance_visual_scene</i> field. */
    protected ColladaInstanceVisualScene instanceVisualScene;

    /**
     * Construct an instance.
     *
     * @param ns the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public ColladaScene(String ns)
    {
        super(ns);
    }

    /**
     * Indicates the value of the <i>instance_visual_scene</i> field.
     *
     * @return The value of the <i>instance_visual_scene</i> field, or null if the field is not set.
     */
    protected ColladaInstanceVisualScene getInstanceVisualScene()
    {
        if (!this.sceneFetched)
        {
            this.instanceVisualScene = (ColladaInstanceVisualScene) this.getField("instance_visual_scene");
            this.sceneFetched = true;
        }
        return this.instanceVisualScene;
    }

    public Box getLocalExtent(ColladaTraversalContext tc)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ColladaInstanceVisualScene sceneInstance = this.getInstanceVisualScene();

        return sceneInstance != null ? sceneInstance.getLocalExtent(tc) : null;
    }

    /** {@inheritDoc} */
    public void preRender(ColladaTraversalContext tc, DrawContext dc)
    {
        ColladaInstanceVisualScene sceneInstance = this.getInstanceVisualScene();
        if (sceneInstance != null)
            sceneInstance.preRender(tc, dc);
    }

    /** {@inheritDoc} */
    public void render(ColladaTraversalContext tc, DrawContext dc)
    {
        ColladaInstanceVisualScene sceneInstance = this.getInstanceVisualScene();
        if (sceneInstance != null)
            sceneInstance.render(tc, dc);
    }
}
