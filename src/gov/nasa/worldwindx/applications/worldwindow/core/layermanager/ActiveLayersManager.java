/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core.layermanager;

import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwindx.applications.worldwindow.core.Constants;
import gov.nasa.worldwindx.applications.worldwindow.features.Feature;

/**
 * @author tag
 * @version $Id: ActiveLayersManager.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface ActiveLayersManager extends Feature
{
    /**
     * Indicates whether to show internal layers, those whose attribute-value list contains {@link
     * Constants#INTERNAL_LAYER}.
     *
     * @return true if internal layers are shown, otherwise false.
     */
    boolean isIncludeInternalLayers();

    /**
     * Specifies whether to show internal layers.
     *
     * @param includeInternalLayers true for internal layers to be shown, otherwise false.
     */
    void setIncludeInternalLayers(boolean includeInternalLayers);

    /**
     * Replace the contents of the model with the contents of a specified [@link LayerList}.
     *
     * @param layerList the layer list to display.
     */
    void updateLayerList(LayerList layerList);
}
