/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.worldwindow.core.layermanager;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwindx.applications.worldwindow.features.Feature;
import gov.nasa.worldwindx.applications.worldwindow.features.swinglayermanager.LayerNode;

/**
 * @author tag
 * @version $Id: LayerManager.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface LayerManager extends Feature
{
    Layer findLayerByTitle(String layerTitle, String groupTitle);

    void addGroup(LayerPath pathToGroup);

    void addLayer(Layer layer, LayerPath pathToParent);

    void removeLayer(Layer layer);

    void redraw();

    void scrollToLayer(Layer layer);

    void selectLayer(Layer layer, boolean tf);

    /**
     * Returns a path to the group node of the default layer group, which is the group holding the base layers.
     *
     * @return a path to the default group node.
     */
    LayerPath getDefaultGroupPath();

    void removeLayers(LayerList layerList);

    void removeLayer(LayerPath path);

    /**
     * Returns the layer at the end of a specified path.
     *
     * @param path the path to the layer.
     *
     * @return the layer at the end of the path, or null if no layer is there.
     */
    Layer getLayerFromPath(LayerPath path);

    /**
     * Expands the display of the group so that all layers are visibly listed.
     *
     * @param groupName the name of the group.
     */
    void expandGroup(String groupName);

    /**
     * Enables or disables the ability to select a group as a whole and therby enable or disable all layers in that
     * group. For some layer groups, such as the base group, it's not appropriate to turn them all on or all off.
     *
     * @param path the path to the group.
     * @param tf   true if group selection should be allowed, false if group selection should not be allowed.
     */
    void enableGroupSelection(LayerPath path, boolean tf);

    boolean containsPath(LayerPath pathToGroup);

    void expandPath(LayerPath path);

    LayerNode getNode(LayerPath path);
}
