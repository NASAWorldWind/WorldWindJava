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
