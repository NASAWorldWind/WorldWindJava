/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.layertree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.kml.*;

import javax.swing.*;
import java.beans.*;

/**
 * A <code>KMLFeatureTreeNode</code> that represents a KML network link defined by a <code>{@link
 * gov.nasa.worldwind.ogc.kml.KMLNetworkLink}</code>.
 * <p/>
 * <code>KMLNetworkLinkTreeNode</code>  automatically repopulates its hierarchy when its <code>KMLNetworkLink</code> is
 * refreshed, and notifies its listeners when this happens.
 *
 * @author dcollins
 * @version $Id: KMLNetworkLinkTreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLNetworkLinkTreeNode extends KMLContainerTreeNode
{
    /**
     * Creates a new <code>KMLNetworkLinkTreeNode</code> from the specified <code>networkLink</code>. The node's name is
     * set to the network link's name, and the node's hierarchy is populated from the network link's KML features.
     *
     * @param networkLink the KML network link this node represents.
     *
     * @throws IllegalArgumentException if the <code>networkLink</code> is <code>null</code>.
     */
    public KMLNetworkLinkTreeNode(KMLNetworkLink networkLink)
    {
        super(networkLink);
    }

    /**
     * Indicates the KML network link this node represents.
     *
     * @return this node's KML network link.
     */
    @Override
    public KMLNetworkLink getFeature()
    {
        return (KMLNetworkLink) super.getFeature();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Additionally, this node's hierarchy is populated from the KML features in its <code>KMLNetworkLink</code>, and
     * this registers a <code>RETRIEVAL_STATE_SUCCESSFUL</code> property change listener on the
     * <code>KMLNetworkLink</code>.
     */
    @Override
    protected void initialize()
    {
        super.initialize();

        // Add a property change listener to the KMLRoot. Upon receiving an RETRIEVAL_STATE_SUCCESSFUL event,
        // repopulate this node's hierarchy with the KML features in its KMLNetworkLink and fire a
        // RETRIEVAL_STATE_SUCCESSFUL to this nodes listeners.
        this.getFeature().getRoot().addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                if (AVKey.RETRIEVAL_STATE_SUCCESSFUL.equals(propertyChangeEvent.getPropertyName())
                    && KMLNetworkLinkTreeNode.this.getFeature() == propertyChangeEvent.getNewValue())
                {
                    // Ensure that the node list is manipulated on the EDT
                    if (SwingUtilities.isEventDispatchThread())
                    {
                        refresh();
                        KMLNetworkLinkTreeNode.this.firePropertyChange(AVKey.RETRIEVAL_STATE_SUCCESSFUL, null, this);
                    }
                    else
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                refresh();
                                KMLNetworkLinkTreeNode.this.firePropertyChange(AVKey.RETRIEVAL_STATE_SUCCESSFUL, null,
                                    this);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Called when this node's <code>KMLNetworkLink</code> refreshes. Clears this node's hierarchy by removing its
     * children, then adds a new <code>KMLFeatureTreeNode</code> to this node for each KML feature in the
     * <code>KMLNetworkLink</code>.
     * <p/>
     * If the <code>KMLNetworkLink</code>'s top level feature is a <code>KMLDocument</code>, this method ignores the
     * document and adds its children directly to this node. Creating a node for the document adds an extra level to the
     * tree node that doesn't provide any meaningful grouping.
     */
    @Override
    protected void refresh()
    {
        // Call super to add features contained by the NetworkLink.
        super.refresh();

        // Now add the network resource.
        KMLRoot kmlRoot = this.getFeature().getNetworkResource();
        if (kmlRoot == null || kmlRoot.getFeature() == null)
            return;

        // A KML document has only one top-level feature. Except for very simple files, this top level is typically a
        // Document. In this case we skip the top level document, and attach tree nodes for the features beneath that
        // document. Attaching the document as a tree node would add an extra level to the tree that doesn't provide any
        // meaningful grouping.

        if (kmlRoot.getFeature() instanceof KMLDocument)
        {
            KMLDocument doc = (KMLDocument) kmlRoot.getFeature();
            for (KMLAbstractFeature child : doc.getFeatures())
            {
                if (child != null)
                    this.addFeatureNode(child);
            }
        }
        else
        {
            this.addFeatureNode(kmlRoot.getFeature());
        }
    }
}
