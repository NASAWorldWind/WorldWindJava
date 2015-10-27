/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util.layertree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.tree.*;
import gov.nasa.worldwind.util.EntityMap;

/**
 * A <code>TreeNode</code> that represents a KML feature defined by a <code>{@link
 * gov.nasa.worldwind.ogc.kml.KMLAbstractFeature}</code>.
 * <p/>
 * The node's selection state is synchronized with its KML feature's visibility state. <code>{@link
 * #isSelected()}</code> returns whether the node's feature is visible. Calling <code>{@link
 * #setSelected(boolean)}</code> specifies both the the node's selection state, and whether its feature should be
 * enabled for rendering and selection.
 *
 * @author dcollins
 * @version $Id: KMLFeatureTreeNode.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLFeatureTreeNode extends BasicTreeNode
{
    /** Indicates the KML feature this node represents. Initialized during construction. */
    protected KMLAbstractFeature feature;

    /**
     * Creates a new <code>KMLFeatureTreeNode</code> from the specified <code>feature</code>. The node's name is set to
     * the feature's name.
     *
     * @param feature the KML feature this node represents.
     *
     * @throws IllegalArgumentException if the <code>feature</code> is <code>null</code>.
     */
    public KMLFeatureTreeNode(KMLAbstractFeature feature)
    {
        super(""); // Node text is set below

        if (feature == null)
        {
            String message = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.feature = feature;

        this.initialize();
    }

    /** Places the KML feature in the node's <code>AVKey.CONTEXT</code> field. */
    protected void initialize()
    {
        // The CONTEXT key identifies the KML feature this tree node is associated with.
        this.setValue(AVKey.CONTEXT, this.getFeature());
    }

    /**
     * Creates a new <code>KMLFeatureTreeNode</code> from the specified <code>feature</code>. This maps the feature type
     * to a node type as follows: <ul> <li>KML container to <code>KMLContainerTreeNode</code>.</li> <li>KML network link
     * to <code>KMLNetworkLink</code>.</li> <li>All other KML features to <code>KMLFeatureTreeNode</code>.</li> </ul>
     *
     * @param feature the KML feature to create a new <code>KMLFeatureTreeNode</code> for.
     *
     * @return a new <code>KMLFeatureTreeNode</code>.
     *
     * @throws IllegalArgumentException if the <code>feature</code> is <code>null</code>.
     */
    public static KMLFeatureTreeNode fromKMLFeature(KMLAbstractFeature feature)
    {
        if (feature == null)
        {
            String message = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (feature instanceof KMLNetworkLink)
            return new KMLNetworkLinkTreeNode((KMLNetworkLink) feature);
        else if (feature instanceof KMLAbstractContainer)
            return new KMLContainerTreeNode((KMLAbstractContainer) feature);
        else
            return new KMLFeatureTreeNode(feature);
    }

    /**
     * Indicates the KML feature this node represents.
     *
     * @return this node's KML feature.
     */
    public KMLAbstractFeature getFeature()
    {
        return this.feature;
    }

    /**
     * Indicates whether this node's KML feature is enabled for rendering.
     *
     * @return <code>true</code> if the KML feature is enabled for rendering, otherwise <code>false</code>.
     */
    @Override
    public boolean isSelected()
    {
        Boolean b = this.feature.getVisibility();
        return b == null || b;
    }

    /**
     * Specifies whether this node's feature is enabled for rendering and selection. This sets both the node's selection
     * state and its KML feature's visible state.
     *
     * @param selected <code>true</code> to enable the KML feature, otherwise <code>false</code>.
     */
    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        this.getFeature().setVisibility(selected);
    }

    /**
     * Expands paths in the specified <code>tree</code> corresponding to open KML container elements. This assumes that
     * the <code>tree</code>'s model contains this node.
     * <p/>
     * This node's path is expanded if the feature's <code>open</code> property is <code>true</code>.
     * <p/>
     * This calls <code>expandOpenContainers</code> on all children which are instances of
     * <code>KMLFeatureTreeNode</code>.
     *
     * @param tree the <code>Tree</code> who's paths should be expanded.
     *
     * @throws IllegalArgumentException if the <code>tree</code> is null.
     */
    public void expandOpenContainers(Tree tree)
    {
        if (tree == null)
        {
            String message = Logging.getMessage("nullValue.TreeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.mustExpandNode())
            tree.expandPath(this.getPath());

        for (TreeNode child : this.getChildren())
        {
            if (child instanceof KMLFeatureTreeNode)
                ((KMLFeatureTreeNode) child).expandOpenContainers(tree);
        }
    }

    /**
     * Indicates whether the tree path for this node must expanded. This return <code>true</code> if the KML feature's
     * <code>open</code> property is <code>true</code>, and <code>false</code> otherwise.
     *
     * @return <code>true</code> if the tree path for this node must be expanded, otherwise <code>false</code>.
     */
    protected boolean mustExpandNode()
    {
        return Boolean.TRUE.equals(this.getFeature().getOpen());
    }

    @Override
    public String getText()
    {
        String name = feature.getName();

        return name != null ? this.stripHtmlTags(name) : feature.getClass().getSimpleName();
    }

    @Override
    public String getDescription()
    {
        return this.makeFeatureDescription();
    }

    /**
     * Makes this node's description text from its KML feature. This uses the feature's <code>KMLSnippet</code> if
     * present, or the feature's <code>description</code> if there is no snippet.
     *
     * @return The feature description.
     */
    protected String makeFeatureDescription()
    {
        String text;

        Object snippet = this.getFeature().getSnippet();
        if (snippet instanceof KMLSnippet)
        {
            KMLSnippet kmlSnippet = (KMLSnippet) snippet;

            // Check the maxLines property of the snippet. maxLines == 0, don't set any description.
            Integer maxLines = kmlSnippet.getMaxLines();
            if (maxLines == null || maxLines > 0)
                text = kmlSnippet.getCharacters();
            else
                text = null;
        }
        else
        {
            text = this.getFeature().getDescription();
        }

        return EntityMap.replaceAll(this.stripHtmlTags(text));
    }

    /**
     * Remove HTML tags and extra whitespace from a string. Runs of whitespace will be collapsed to a single space.
     *
     * @param input Text to strip of HTML tags and extra whitespace.
     *
     * @return The input string with HTML tags removed, and runs of whitespace collapsed to a single space. Returns
     *         {@code null} if {@code input} is {@code null}.
     */
    protected String stripHtmlTags(String input)
    {
        if (input == null)
            return null;

        StringBuilder output = new StringBuilder();

        boolean inTag = false;
        boolean inWhitespace = false;

        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);

            if (Character.isWhitespace(c))
            {
                inWhitespace = true;
                continue;
            }

            if (!inTag && inWhitespace && output.length() > 0)
            {
                output.append(' ');
            }
            inWhitespace = false;

            if (c == '<')
            {
                inTag = true;
            }
            else if (c == '>')
            {
                inTag = false;
            }
            else if (!inTag)
            {
                output.append(c);
            }
        }

        return output.toString();
    }
}
