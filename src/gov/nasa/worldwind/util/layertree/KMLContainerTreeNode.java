/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
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
