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
