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

import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.util.tree.TreeNode;

/**
 * A <code>KMLFeatureTreeNode</code> that represents a KML container defined by a <code>{@link
 * gov.nasa.worldwind.ogc.kml.KMLAbstractContainer}</code>.
 *
 * @author dcollins
 * @version $Id: KMLContainerTreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLContainerTreeNode extends KMLFeatureTreeNode
{
    /**
     * Creates a new <code>KMLContainerTreeNode</code> from the specified <code>container</code>. The node's name is set
     * to the feature's name, and the node's hierarchy is populated from the container's KML features.
     *
     * @param container the KML container this node represents.
     *
     * @throws IllegalArgumentException if the <code>container</code> is <code>null</code>.
     */
    public KMLContainerTreeNode(KMLAbstractContainer container)
    {
        super(container);
    }

    /**
     * Indicates the KML container this node represents.
     *
     * @return this node's KML container.
     */
    @Override
    public KMLAbstractContainer getFeature()
    {
        return (KMLAbstractContainer) super.getFeature();
    }

    /** {@inheritDoc} */
    @Override
    protected void initialize()
    {
        super.initialize();
        this.refresh();
    }

    /** Populate this node's hierarchy from the KML features in its <code>KMLAbstractContainer</code>. */
    protected void refresh()
    {
        this.removeAllChildren();

        for (KMLAbstractFeature child : this.getFeature().getFeatures())
        {
            if (child != null)
                this.addFeatureNode(child);
        }
    }

    /**
     * Adds the a new <code>KMLFeatureTreeNode</code> created with the specified <code>feature</code> to this node.
     *
     * @param feature the KML feature to add.
     */
    protected void addFeatureNode(KMLAbstractFeature feature)
    {
        TreeNode featureNode = KMLFeatureTreeNode.fromKMLFeature(feature);
        if (featureNode != null)
            this.addChild(featureNode);
    }
}
