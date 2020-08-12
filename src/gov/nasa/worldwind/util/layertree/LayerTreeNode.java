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

package gov.nasa.worldwind.util.layertree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.tree.BasicTreeNode;

/**
 * A <code>TreeNode</code> that represents a <code>{@link gov.nasa.worldwind.layers.Layer}</code>.
 * <p>
 * The node's selection state is synchronized with its <code>Layer</code>'s enabled state. <code>{@link
 * #isSelected()}</code> returns whether the node's <code>Layer</code> is enabled. Calling <code>{@link
 * #setSelected(boolean)}</code> specifies both the the node's selection state, and whether its <code>Layer</code>
 * should be enabled for rendering and selection.
 *
 * @author pabercrombie
 * @version $Id: LayerTreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerTreeNode extends BasicTreeNode
{
    /** The layer node's default icon path. */
    protected static final String DEFAULT_IMAGE = "images/16x16-icon-earth.png";

    /**
     * Indicates the <code>Layer</code> this node represents. Initialized to a non-<code>null</code> value during
     * construction.
     */
    protected Layer layer;

    /**
     * Creates a new <code>LayerTreeNode</code> from the specified <code>layer</code>. The node's name is set to the
     * layer's name.
     *
     * @param layer the <code>Layer</code> this node represents.
     *
     * @throws IllegalArgumentException if the <code>layer</code> is <code>null</code>.
     */
    public LayerTreeNode(Layer layer)
    {
        super(layer != null ? layer.getName() : "");

        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.layer = layer;
        this.initialize();
    }

    /** Initializes this node's image source. */
    protected void initialize()
    {
        Object imageSource = this.layer.getValue(AVKey.IMAGE);
        if (imageSource == null)
            imageSource = DEFAULT_IMAGE;
        this.setImageSource(imageSource);
    }

    /**
     * Indicates whether this node's <code>Layer</code> is enabled for rendering and selection.
     *
     * @return <code>true</code> if the <code>Layer</code> is enabled, otherwise <code>false</code>.
     */
    @Override
    public boolean isSelected()
    {
        return this.layer.isEnabled();
    }

    /**
     * Specifies whether this node's <code>Layer</code> is enabled for rendering and selection. This sets both the
     * node's selection state and its <code>Layer</code>'s enabled state.
     *
     * @param selected <code>true</code> to enable the <code>Layer</code>, otherwise <code>false</code>.
     */
    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        this.layer.setEnabled(selected);
    }
}
