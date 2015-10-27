/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.util.tree;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.*;
import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;
import java.util.List;

/**
 * Layout that draws a {@link Tree} similar to a file browser tree.
 *
 * @author pabercrombie
 * @version $Id: BasicTreeLayout.java 2053 2014-06-10 20:16:57Z tgaskins $
 */
public class BasicTreeLayout extends WWObjectImpl implements TreeLayout, Scrollable, PreRenderable
{
    /** Tree that is drawn by this layout. */
    protected Tree tree;

    /** Frame that contains the tree. */
    protected ScrollFrame frame;

    /** Attributes to use when the tree is not highlighted. */
    protected TreeAttributes normalAttributes = new BasicTreeAttributes();
    /** Attributes to use when the frame is highlighted. */
    protected TreeAttributes highlightAttributes = new BasicTreeAttributes();
    /** Active attributes, either normal or highlight. */
    protected TreeAttributes activeAttributes = new BasicTreeAttributes();
    /** The attributes used if attributes are not specified. */
    protected static final TreeAttributes defaultAttributes;

    /** Indicates whether or not the tree is highlighted. */
    protected boolean highlighted;

    /** Support for setting up and restoring picking state, and resolving the picked object. */
    protected PickSupport pickSupport = new PickSupport();

    /**
     * This field is set by {@link #makeVisible(TreePath)}, and read by {@link #scrollToNode(gov.nasa.worldwind.render.DrawContext)}
     * during rendering.
     */
    protected TreeNode scrollToNode;

    /** Cache the rendered size of the tree and recompute when the tree changes. */
    protected Dimension size;
    /** Indicates that the tree size needs to be computed. */
    protected boolean mustRecomputeSize = true;
    /** Indicates that the tree layout needs to be computed. */
    protected boolean mustRecomputeLayout = true;

    /** Indicates that node description text must be drawn. */
    protected boolean showDescription = true;
    /** Indicates that a triangle must be drawn to indicate if a group node is expanded or collapsed. */
    protected boolean drawNodeStateSymbol = true;
    /** Indicates that a checkbox must be drawn for each node to indicate if the node is selected or not. */
    protected boolean drawSelectedSymbol = true;

    /** Indicates whether or not the description text will be wrapped to fit the frame. */
    protected boolean wrapText = true;
    /**
     * Maximum number of lines of wrapped description text to draw. If the description exceeds this it will be cut off
     * at this number of lines, with a trailing "...".
     */
    protected int maxWrappedLines = 2;

    /** Cache of computed text bounds. */
    protected BoundedHashMap<TextCacheKey, Rectangle2D> textCache = new BoundedHashMap<TextCacheKey, Rectangle2D>();

    /** Cache of computed node layout data. */
    protected BoundedHashMap<TreeNode, NodeLayout> layoutCache = new BoundedHashMap<TreeNode, NodeLayout>();

    /** Cache of node layouts. This list is populated when the tree layout is computed. */
    protected java.util.List<NodeLayout> treeNodes = new ArrayList<NodeLayout>();

    /**
     * A little extra space is added to the tree dimensions to give the tree a little bit of separation from the
     * scrollable frame. This value determines the amount of padding, in pixels.
     */
    protected int padding = 10;

    // Computed each frame
    protected long frameNumber = -1L;
    protected long attributesFrameNumber = -1L;
    /** Location of the lower left corner of the tree, in GL coordinates. */
    protected Point screenLocation;
    /**
     * Time at which the rendered tree last changed. Used to indicate when the ScrollFrame needs to refresh the rendered
     * representation
     */
    protected long updateTime;
    /** Frame size when the tree layout was last computed. */
    protected Dimension previousFrameSize;
    /** Frame size when the tree size was last computed. */
    protected Dimension previousSizeBounds;
    /** The height of one line of text in the active font. */
    protected int lineHeight;
    /** Number of nodes in the tree, used to set a bound on the text cache. */
    protected int nodeCount;
    /** Indentation in pixels applied to each new level of the tree. */
    protected int indent;

    static
    {
        defaultAttributes = new BasicTreeAttributes();
    }

    /**
     * Create a layout for a tree.
     *
     * @param tree Tree to create layout for.
     */
    public BasicTreeLayout(Tree tree)
    {
        this(tree, null);
    }

    /**
     * Create a layout for a tree, at a screen location.
     *
     * @param tree Tree to create layout for.
     * @param x    X coordinate of the upper left corner of the tree frame.
     * @param y    Y coordinate of the upper left corner of the tree frame, measured from the top of the screen.
     */
    public BasicTreeLayout(Tree tree, int x, int y)
    {
        this(tree, new Offset((double) x, (double) y, AVKey.PIXELS, AVKey.INSET_PIXELS));
    }

    /**
     * Create a layout for a tree, at a screen location.
     *
     * @param tree           Tree to create layout for.
     * @param screenLocation The location of the upper left corner of the tree frame. The offset is interpreted relative
     *                       to the lower left corner of the screen.
     */
    public BasicTreeLayout(Tree tree, Offset screenLocation)
    {
        this.tree = tree;
        this.frame = this.createFrame();
        this.frame.setContents(this);

        // Listen for property changes in the frame. These will be forwarded to the layout listeners. This is necessary
        // to pass AVKey.REPAINT events up the layer.
        this.frame.addPropertyChangeListener(this);

        // Add listener for tree events so that we can recompute the tree size when things change. Because TreeLayout
        // is a WWObject, it sends property change events to its listeners. Since Tree is likely to listen for property
        // change events on TreeLayout, we add an anonymous listener to avoid an infinite cycle of property change
        // events between TreeLayout and Tree.
        this.tree.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                // Ignore events originated by this TreeLayout, and repaint events. There is no need to recompute the
                // tree layout just because a repaint was triggered.
                if (propertyChangeEvent.getSource() != BasicTreeLayout.this
                    && !AVKey.REPAINT.equals(propertyChangeEvent.getPropertyName()))
                {
                    BasicTreeLayout.this.invalidate();
                }
            }
        });

        if (screenLocation != null)
            this.setScreenLocation(screenLocation);
    }

    /**
     * Indicates whether or not the layout wraps the node description to multiple lines. Note that the node title is
     * never wrapped, only the description.
     *
     * @return {@code true} if the description will be wrapped to fit the frame.
     */
    public boolean isWrapText()
    {
        return this.wrapText;
    }

    /**
     * Specifies whether or not the layout wraps the node description to multiple lines. Note that the node title is
     * never wrapped, only the description.
     *
     * @param wrapText {@code true} if the description text must be wrapped to fit the frame.
     */
    public void setWrapText(boolean wrapText)
    {
        this.wrapText = wrapText;
    }

    /**
     * Get the size of the symbol that indicates that a node is expanded or collapsed.
     *
     * @return The size of the node state symbol.
     */
    protected Dimension getNodeStateSymbolSize()
    {
        return new Dimension(12, 12);
    }

    /**
     * Get the size of the symbol that indicates that a node is selected or not selected.
     *
     * @return The size of the node selection symbol.
     */
    protected Dimension getSelectedSymbolSize()
    {
        return new Dimension(12, 12);
    }

    /**
     * Should the node renderer include node descriptions?
     *
     * @return True if the renderer should renderer node descriptions.
     */
    public boolean isShowDescription()
    {
        return this.showDescription;
    }

    /**
     * Set the renderer to renderer node descriptions (additional text rendered under the node title).
     *
     * @param showDescription True if the description should be rendered. False if only the icon and title should be
     *                        rendered.
     */
    public void setShowDescription(boolean showDescription)
    {
        this.showDescription = showDescription;
    }

    /**
     * Will the renderer draw a symbol to indicate that the node is selected? The default symbol is a checkbox.
     *
     * @return True if the node selected symbol (a checkbox by default) will be drawn.
     */
    public boolean isDrawSelectedSymbol()
    {
        return this.drawSelectedSymbol;
    }

    /**
     * Set whether or not the renderer will draw a symbol to indicate that the node is selected. The default symbol is a
     * checkbox.
     *
     * @param drawSelectedSymbol True if the node selected symbol (a checkbox by default) will be drawn.
     */
    public void setDrawSelectedSymbol(boolean drawSelectedSymbol)
    {
        this.drawSelectedSymbol = drawSelectedSymbol;
    }

    /**
     * Will the renderer draw a symbol to indicate that the node is expanded or collapsed (applies only to non-leaf
     * nodes). The default symbol is a triangle pointing to the right, for collapsed nodes, or down for expanded nodes.
     *
     * @return True if the node state symbol (default is a triangle pointing either to the right or down) will be
     *         drawn.
     */
    public boolean isDrawNodeStateSymbol()
    {
        return this.drawNodeStateSymbol;
    }

    /**
     * Specifies the maximum number of lines of text wrapped description text to draw. If the description exceeds this
     * number of lines it will be cut off with a trailing "...".
     *
     * @return Maximum number of lines of description text that will be drawn.
     */
    public int getMaxWrappedLines()
    {
        return this.maxWrappedLines;
    }

    /**
     * Indicates the maximum number of lines of text wrapped description text to draw. If the description exceeds this
     * number of lines it will be cut off with a trailing "...".
     *
     * @param maxLines Maximum number of lines of description text that will be drawn.
     */
    public void setMaxWrappedLines(int maxLines)
    {
        if (maxLines != this.maxWrappedLines)
        {
            this.maxWrappedLines = maxLines;

            // Need to re-wrap the text because the number of lines changes.
            this.invalidate();
            this.invalidateWrappedText();
        }
    }

    /**
     * Set whether or not the renderer will draw a symbol to indicate that the node is expanded or collapsed (applies
     * only to non-leaf nodes). The default symbol is a triangle pointing to the right, for collapsed nodes, or down for
     * expanded nodes.
     *
     * @param drawNodeStateSymbol True if the node state symbol (default is a triangle pointing either to the right or
     *                            down) will be drawn.
     */
    public void setDrawNodeStateSymbol(boolean drawNodeStateSymbol)
    {
        this.drawNodeStateSymbol = drawNodeStateSymbol;
    }

    /** {@inheritDoc} */
    public long getUpdateTime()
    {
        return this.updateTime;
    }

    /**
     * Create the frame that the tree will be rendered inside.
     *
     * @return A new frame.
     */
    protected ScrollFrame createFrame()
    {
        return new ScrollFrame();
    }

    /**
     * Get the size of the entire tree, including the part that is not visible in the scroll pane.
     *
     * @param dc        Draw context.
     * @param frameSize Size of the frame the tree will be rendered into. May be {@code null}.
     *
     * @return Size of the rendered tree.
     */
    public Dimension getSize(DrawContext dc, Dimension frameSize)
    {
        this.updateAttributes(dc);

        // Computing the size of rendered text is expensive, so only recompute the tree size when necessary.
        if (this.mustRecomputeSize(frameSize))
        {
            TreeModel model = this.tree.getModel();
            TreeNode root = model.getRoot();

            this.size = new Dimension();
            this.nodeCount = 0;
            this.computeSize(this.tree, root, dc, frameSize, this.size, 0, 1);

            // Limit the caches to the number of the nodes that are visible in the tree.
            this.layoutCache.setCapacity(this.nodeCount);
            this.textCache.setCapacity(this.nodeCount * 2); // Each node can have two strings (title and description)

            // Add a little padding to the dimension so that no text gets clipped off by the scrollable frame
            this.size.height += this.padding;

            this.mustRecomputeSize = false;
            this.previousSizeBounds = frameSize;

            this.markUpdated();
        }
        return this.size;
    }

    /**
     * Compute the size of a tree. This method invokes itself recursively to calculate the size of the tree, taking into
     * account which nodes are expanded and which are not. This computed size will be stored in the {@code size}
     * parameter.
     *
     * @param tree      Tree that contains the root node.
     * @param root      Root node of the subtree to find the size of. This does not need to be the root node of the
     *                  tree.
     * @param dc        Draw context.
     * @param frameSize Size of the frame into which the tree will render.
     * @param size      Size object to modify. This method will change the width and height fields of {@code size} to
     *                  hold the new size of the tree.
     * @param x         Horizontal coordinate of the start of this node. This parameter must be zero. This method calls
     *                  itself recursively and changes the {@code x} parameter to reflect the indentation level of
     *                  different levels of the tree.
     * @param level     Level of this node. Tree root node is level 1, children of the root are level 2, etc.
     */
    protected void computeSize(Tree tree, TreeNode root, DrawContext dc, Dimension frameSize, Dimension size, int x,
        int level)
    {
        this.nodeCount++;

        TreeAttributes attributes = this.getActiveAttributes();

        Dimension thisSize = this.getNodeSize(dc, frameSize, x, root, attributes);

        int indent = 0;

        if (this.mustDisplayNode(root, level))
        {
            int thisWidth = thisSize.width + x;

            if (thisWidth > size.width)
                size.width = thisWidth;

            size.height += thisSize.height;
            size.height += attributes.getRowSpacing();

            indent = this.indent;
        }

        if (tree.isNodeExpanded(root))
        {
            for (TreeNode child : root.getChildren())
            {
                this.computeSize(tree, child, dc, frameSize, size, x + indent, level + 1);
            }
        }
    }

    /** Force the layout to recompute the size of the tree. */
    public void invalidate()
    {
        this.markUpdated();
        this.mustRecomputeSize = true;
        this.mustRecomputeLayout = true;
    }

    /** Set the {@link #updateTime} to the current system time, marking the Scrollable contents as updated. */
    protected void markUpdated()
    {
        this.updateTime = System.currentTimeMillis();
    }

    /**
     * Determine if a node needs to be displayed. This method examines only one node at a time. It does not take into
     * account that the node's parent may be in the collapsed state, in which the children are not rendered.
     *
     * @param node  Node to test.
     * @param level Level of the node in the tree. The root node is level 1, its children are level 2, etc.
     *
     * @return True if the node must be displayed.
     */
    protected boolean mustDisplayNode(TreeNode node, int level)
    {
        return node.isVisible() && (level > 1 || this.getActiveAttributes().isRootVisible());
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        // Adjust scroll position if an application has requested that the layout scroll to make a node visible.
        this.scrollToNode(dc);

        this.frame.preRender(dc);
    }

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        this.frame.render(dc);
    }

    /**
     * Scroll the frame to make a the node set in {@link #scrollToNode} node visible. Does nothing if {@link
     * #scrollToNode} is null.
     *
     * @param dc Draw context.
     */
    protected synchronized void scrollToNode(DrawContext dc)
    {
        if (this.scrollToNode != null)
        {
            // Update the frame bounds to make sure that the frame's scroll model includes the full extent of the tree
            ScrollFrame frame = this.getFrame();
            frame.updateBounds(dc);

            Point drawPoint = new Point(0, 0);
            Rectangle bounds = this.findNodeBounds(this.scrollToNode, this.tree.getModel().getRoot(), dc,
                frame.getBounds(dc).getSize(), drawPoint, 1);

            // Calculate a scroll position that will bring the node to the top of the visible area. Subtract the row spacing
            // to avoid clipping off the top of the node.
            int scroll = (int) Math.abs(bounds.getMaxY()) - this.getActiveAttributes().getRowSpacing();
            this.frame.getScrollBar(AVKey.VERTICAL).setValue(scroll);

            this.scrollToNode = null;
        }
    }

    /** {@inheritDoc} */
    public void renderScrollable(DrawContext dc, Point location, Dimension frameSize, Rectangle clipBounds)
    {
        TreeModel model = this.tree.getModel();
        TreeNode root = model.getRoot();

        this.screenLocation = location;
        this.updateAttributes(dc);

        if (this.frameNumber != dc.getFrameTimeStamp())
        {
            if (this.mustRecomputeTreeLayout(frameSize))
            {
                this.treeNodes.clear();

                Point drawPoint = new Point(0, this.size.height);
                this.computeTreeLayout(root, dc, frameSize, drawPoint, 1, treeNodes);

                this.previousFrameSize = frameSize;
                this.mustRecomputeLayout = false;
            }

            this.frameNumber = dc.getFrameTimeStamp();
        }

        try
        {
            if (dc.isPickingMode())
            {
                this.pickSupport.clearPickList();
                this.pickSupport.beginPicking(dc);
            }

            this.renderNodes(dc, location, treeNodes, clipBounds);
        }
        finally
        {
            if (dc.isPickingMode())
            {
                this.pickSupport.endPicking(dc);
                this.pickSupport.resolvePick(dc, dc.getPickPoint(), dc.getCurrentLayer());
            }
        }
    }

    /**
     * Indicates whether or not the tree layout needs to be recomputed.
     *
     * @param frameSize Size of the frame that holds the tree.
     *
     * @return {@code true} if the layout needs to be recomputed, otherwise {@code false}.
     */
    protected boolean mustRecomputeTreeLayout(Dimension frameSize)
    {
        return this.mustRecomputeLayout || this.previousFrameSize == null
            || this.previousFrameSize.width != frameSize.width;
    }

    /**
     * Indicates whether or not the tree size needs to be recomputed.
     *
     * @param frameSize Size of the frame that holds the tree. Size may be null if the frame size is not known.
     *
     * @return {@code true} if the size needs to be recomputed, otherwise {@code false}.
     */
    protected boolean mustRecomputeSize(Dimension frameSize)
    {
        return this.mustRecomputeSize
            || (this.previousSizeBounds == null && frameSize != null)
            || (frameSize != null && this.previousSizeBounds.width != frameSize.width);
    }

    /**
     * Update the active attributes for the current frame, and compute other properties that are based on the active
     * attributes. This method only computes attributes once for each frame. Subsequent calls in the same frame will not
     * recompute the attributes.
     *
     * @param dc Current draw context.
     */
    protected void updateAttributes(DrawContext dc)
    {
        if (dc.getFrameTimeStamp() != this.attributesFrameNumber)
        {
            this.determineActiveAttributes();
            this.indent = this.computeIndentation();
            this.lineHeight = this.computeMaxTextHeight(dc);

            this.attributesFrameNumber = dc.getFrameTimeStamp();
        }
    }

    /**
     * Compute the indentation, in pixels, applied to each new level of the tree.
     *
     * @return indention (in pixels) to apply to each new level in the tree.
     */
    protected int computeIndentation()
    {
        int iconWidth = this.getActiveAttributes().getIconSize().width;
        int iconSpacing = this.getActiveAttributes().getIconSpace();
        int checkboxWidth = this.getSelectedSymbolSize().width;

        // Compute the indentation to make the checkbox of the child level line up the icon of the parent level
        return checkboxWidth + iconSpacing + ((iconWidth - checkboxWidth) / 2);
    }

    /**
     * Determine the maximum height of a line of text using the active font.
     *
     * @param dc Current draw context.
     *
     * @return The maximum height of a line of text.
     */
    protected int computeMaxTextHeight(DrawContext dc)
    {
        TreeAttributes attributes = this.getActiveAttributes();

        // Use underscore + capital E with acute accent as max height
        Rectangle2D bounds = this.getTextBounds(dc, "_\u00c9", attributes.getFont());

        double lineHeight = Math.abs(bounds.getY());
        return (int) Math.max(lineHeight, attributes.getIconSize().height);
    }

    /**
     * Render a list of tree nodes.
     *
     * @param dc         Current draw context.
     * @param drawPoint  Point in GL coordinates (origin bottom left corner of the screen) that locates the bottom left
     *                   corner of the tree.
     * @param nodes      Nodes to draw.
     * @param clipBounds Pixels outside of this rectangle will be discarded. Any nodes that do not intersect this
     *                   rectangle will not be drawn.
     */
    protected void renderNodes(DrawContext dc, Point drawPoint, Iterable<NodeLayout> nodes, Rectangle clipBounds)
    {
        // Collect the nodes that are actually visible in the scroll area in a list.
        List<NodeLayout> visibleNodes = new ArrayList<NodeLayout>();

        for (NodeLayout layout : nodes)
        {
            layout.reset(drawPoint);

            if (this.intersectsFrustum(dc, layout, clipBounds))
                visibleNodes.add(layout);

//            // Draw a box around the node bounds. Useful for debugging node layout
//
//            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
//            gl.glBegin(GL2.GL_LINE_LOOP);
//            gl.glVertex2d(layout.screenBounds.getMinX(), layout.screenBounds.getMinY());
//            gl.glVertex2d(layout.screenBounds.getMaxX(), layout.screenBounds.getMinY());
//            gl.glVertex2d(layout.screenBounds.getMaxX(), layout.screenBounds.getMaxY());
//            gl.glVertex2d(layout.screenBounds.getMinX(), layout.screenBounds.getMaxY());
//            gl.glEnd();
        }

        if (this.isDrawNodeStateSymbol())
            this.drawTriangles(dc, visibleNodes);

        if (this.isDrawSelectedSymbol())
            this.drawCheckboxes(dc, visibleNodes);

        // If not picking, draw text and icons. Otherwise just draw pickable rectangles tagged with the node. Unlike
        // the toggle and select controls, selecting the node does not mean anything to the tree, but it may mean
        // something to an application controller.
        if (!dc.isPickingMode())
        {
            this.drawIcons(dc, visibleNodes);
            this.drawText(dc, visibleNodes);

            if (this.isShowDescription())
                this.drawDescriptionText(dc, visibleNodes);
        }
        else
        {
            this.pickTextAndIcon(dc, visibleNodes);
        }
    }

    /**
     * Determines whether a node intersects the view frustum.
     *
     * @param dc           the current draw context.
     * @param layout       node to test intersection of.
     * @param scrollBounds bounds of the area currently visible in the scroll frame.
     *
     * @return {@code true} If the frame intersects the frustum, otherwise {@code false}.
     */
    protected boolean intersectsFrustum(DrawContext dc, NodeLayout layout, Rectangle scrollBounds)
    {
        //noinspection SimplifiableIfStatement
        if (!scrollBounds.intersects(layout.screenBounds))
            return false;

        return !dc.isPickingMode() || dc.getPickFrustums().intersectsAny(layout.pickScreenBounds);
    }

    /**
     * Draw pick rectangles over the icon and text areas the visible nodes.
     *
     * @param dc    Current draw context.
     * @param nodes Visible nodes.
     */
    protected void pickTextAndIcon(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            gl.glBegin(GL2.GL_QUADS);

            for (NodeLayout layout : nodes)
            {
                Color color = dc.getUniquePickColor();
                PickedObject pickedObject = new PickedObject(color.getRGB(), layout.node);
                pickedObject.setValue(AVKey.HOT_SPOT, this.getFrame());
                this.pickSupport.addPickableObject(pickedObject);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                float minX = (float) layout.drawPoint.x;
                float minY = (float) layout.drawPoint.y;
                float maxX = (float) layout.screenBounds.getMaxX();
                float maxY = (float) layout.screenBounds.getMaxY();

                gl.glVertex2f(minX, maxY);
                gl.glVertex2f(maxX, maxY);
                gl.glVertex2f(maxX, minY);
                gl.glVertex2f(minX, minY);
            }
        }
        finally
        {
            gl.glEnd(); // Quads
        }
    }

    /**
     * Draw the main line of text for a list of tree nodes.
     *
     * @param dc    Current draw context.
     * @param nodes List of visible nodes.
     */
    protected void drawText(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        TreeAttributes attributes = this.getActiveAttributes();
        Color color = attributes.getColor();
        float[] colorRGB = color.getRGBColorComponents(null);

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            attributes.getFont(), true, false, false);

        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);

        try
        {
            textRenderer.begin3DRendering();
            textRenderer.setColor(colorRGB[0], colorRGB[1], colorRGB[2], 1);

            for (NodeLayout layout : nodes)
            {
                String text = this.getText(layout.node);
                Rectangle2D textBounds = this.getTextBounds(dc, text, attributes.getFont());

                // Calculate height of text from baseline to top of text. Note that this does not include descenders
                // below the baseline.
                int textHeight = (int) Math.abs(textBounds.getY());
                int vertAdjust = layout.bounds.height - textHeight - (this.lineHeight - textHeight) / 2;

                textRenderer.draw(text, layout.drawPoint.x, layout.drawPoint.y + vertAdjust);
            }
        }
        finally
        {
            textRenderer.end3DRendering();
        }
    }

    /**
     * Draw the description text for tree nodes. The description text is drawn under the main line of text.
     *
     * @param dc    Current draw context.
     * @param nodes List of visible nodes.
     */
    protected void drawDescriptionText(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        TreeAttributes attributes = this.getActiveAttributes();
        Color color = attributes.getColor();
        float[] colorRGB = color.getRGBColorComponents(null);

        TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
            attributes.getDescriptionFont(), true, false, false);
        MultiLineTextRenderer mltr = new MultiLineTextRenderer(textRenderer);

        try
        {
            textRenderer.begin3DRendering();
            textRenderer.setColor(colorRGB[0], colorRGB[1], colorRGB[2], 1);

            for (NodeLayout layout : nodes)
            {
                String description = layout.node.getDescription();

                if (description != null)
                {
                    String wrappedText = this.computeWrappedText(dc, layout.node, attributes.getDescriptionFont(),
                        (int) (layout.screenBounds.getMaxX() - layout.drawPoint.x));

                    int vertAdjust = layout.bounds.height - this.lineHeight;
                    mltr.draw(wrappedText, layout.drawPoint.x, layout.drawPoint.y + vertAdjust);
                }
            }
        }
        finally
        {
            textRenderer.end3DRendering();
        }
    }

    /**
     * Draw icons for a tree nodes.
     *
     * @param dc    Current draw context.
     * @param nodes List of visible nodes.
     */
    protected void drawIcons(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        try
        {
            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            gl.glEnable(GL.GL_TEXTURE_2D);

            TreeAttributes attributes = this.getActiveAttributes();
            Dimension iconSize = attributes.getIconSize();

            gl.glColor4d(1d, 1d, 1d, 1);

            WWTexture activeTexture = null;

            for (NodeLayout layout : nodes)
            {
                WWTexture texture = layout.node.getTexture();
                if (texture == null)
                    continue;

                // Check to see if this node's icon is the same as the previous node. If so, there's no need to rebind
                // the texture.
                boolean textureBound;
                // noinspection SimplifiableIfStatement
                if ((activeTexture != null) && (texture.getImageSource() == activeTexture.getImageSource()))
                {
                    textureBound = true;
                }
                else
                {
                    textureBound = texture.bind(dc);
                    if (textureBound)
                        activeTexture = texture;
                }

                if (textureBound)
                {
                    // If the total node height is greater than the image height, vertically center the image
                    int vertAdjustment = 0;
                    if (iconSize.height < layout.bounds.height)
                    {
                        vertAdjustment = layout.bounds.height - iconSize.height
                            - (this.lineHeight - iconSize.height) / 2;
                    }

                    try
                    {
                        gl.glPushMatrix();

                        TextureCoords texCoords = activeTexture.getTexCoords();
                        gl.glTranslated(layout.drawPoint.x, layout.drawPoint.y + vertAdjustment, 1.0);
                        gl.glScaled((double) iconSize.width, (double) iconSize.width, 1d);
                        dc.drawUnitQuad(texCoords);
                    }
                    finally
                    {
                        gl.glPopMatrix();
                    }

                    layout.drawPoint.x += attributes.getIconSize().width + attributes.getIconSpace();
                }
            }
        }
        finally
        {
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        }
    }

    /**
     * Draw check boxes. Each box includes a check mark is the node is selected, or is filled with a gradient if the
     * node is partially selected.
     *
     * @param dc    Current draw context.
     * @param nodes List of visible nodes.
     */
    protected void drawCheckboxes(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        // The check boxes are drawn in three passes:
        // 1) Draw filled background for partially selected nodes
        // 2) Draw check marks for selected nodes
        // 3) Draw checkbox outlines

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Dimension symbolSize;

        if (!dc.isPickingMode())
        {
            this.drawFilledCheckboxes(dc, nodes); // Draw filled boxes for partially selected nodes
            this.drawCheckmarks(dc, nodes); // Draw check marks for selected nodes

            symbolSize = this.getSelectedSymbolSize();
        }
        else
        {
            // Make the pickable area of the checkbox a little bigger than the actual box so that it is easier to hit.
            symbolSize = new Dimension(this.getSelectedSymbolSize().width + this.getActiveAttributes().getIconSpace(),
                this.lineHeight + this.getActiveAttributes().getRowSpacing());
        }

        // In picking mode all of the boxes can be drawn as filled quads. Otherwise, each box is drawn as a
        // separate line loop
        if (dc.isPickingMode())
        {
            gl.glBegin(GL2.GL_QUADS);
        }
        try
        {
            for (NodeLayout layout : nodes)
            {
                int vertAdjust = layout.bounds.height - symbolSize.height
                    - (this.lineHeight - symbolSize.height) / 2;

                int x = layout.drawPoint.x;
                int y = layout.drawPoint.y + vertAdjust;
                int width = symbolSize.width;

                if (!dc.isPickingMode())
                {
                    // Draw a hollow box uses a line loop
                    gl.glBegin(GL2.GL_LINE_LOOP);
                    try
                    {
                        gl.glVertex2f(x + width, y + symbolSize.height + 0.5f);
                        gl.glVertex2f(x, y + symbolSize.height + 0.5f);
                        gl.glVertex2f(x, y);
                        gl.glVertex2f(x + width, y + 0.5f);
                    }
                    finally
                    {
                        gl.glEnd();
                    }
                }
                // Otherwise draw a filled quad
                else
                {
                    Color color = dc.getUniquePickColor();
                    int colorCode = color.getRGB();
                    this.pickSupport.addPickableObject(colorCode, this.createSelectControl(layout.node));
                    gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                    // If the node does not have a triangle to the left of the checkbox, make the checkbox pickable
                    // area stretch all the way to the frame on the left hand side, since this is otherwise dead space.
                    if (layout.node.isLeaf() || !this.isDrawNodeStateSymbol())
                    {
                        width = x - this.screenLocation.x + symbolSize.width;
                        x = this.screenLocation.x;
                    }

                    gl.glVertex2f(x + width, y + symbolSize.height);
                    gl.glVertex2f(x, y + symbolSize.height);
                    gl.glVertex2f(x, y);
                    gl.glVertex2f(x + width, y);
                }

                layout.drawPoint.x += symbolSize.width + this.getActiveAttributes().getIconSpace();
            }
        }
        finally
        {
            if (dc.isPickingMode())
            {
                gl.glEnd(); // Quads
            }
        }
    }

    /**
     * Draw squares filled with a gradient for partially selected checkboxes.
     *
     * @param dc    Current draw context.
     * @param nodes List of visible nodes.
     */
    protected void drawFilledCheckboxes(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        Dimension selectedSymbolSize = this.getSelectedSymbolSize();
        TreeAttributes attributes = this.getActiveAttributes();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Color[] colors = attributes.getCheckBoxColor();

        try
        {
            gl.glLineWidth(1f);
            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            // Fill box with a diagonal gradient
            gl.glBegin(GL2.GL_QUADS);

            for (NodeLayout layout : nodes)
            {
                int vertAdjust = layout.bounds.height - selectedSymbolSize.height
                    - (this.lineHeight - selectedSymbolSize.height) / 2;

                int x = layout.drawPoint.x;
                int y = layout.drawPoint.y + vertAdjust;

                String selected = layout.node.isTreeSelected();
                boolean filled = TreeNode.PARTIALLY_SELECTED.equals(selected);

                if (filled)
                {
                    OGLUtil.applyColor(gl, colors[0], 1, false);
                    gl.glVertex2f(x + selectedSymbolSize.width, y + selectedSymbolSize.height);
                    gl.glVertex2f(x, y + selectedSymbolSize.height);
                    gl.glVertex2f(x, y);

                    OGLUtil.applyColor(gl, colors[1], 1, false);
                    gl.glVertex2f(x + selectedSymbolSize.width, y);
                }
            }
        }
        finally
        {
            gl.glEnd(); // Quads
        }
    }

    /**
     * Draw checkmark symbols in the selected checkboxes.
     *
     * @param dc    Current draw context.
     * @param nodes List of visible nodes.
     */
    protected void drawCheckmarks(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        Dimension selectedSymbolSize = this.getSelectedSymbolSize();
        TreeAttributes attributes = this.getActiveAttributes();

        Color color = attributes.getColor();

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        // Draw checkmarks for selected nodes
        OGLUtil.applyColor(gl, color, 1, false);
        try
        {
            gl.glEnable(GL.GL_LINE_SMOOTH);
            gl.glBegin(GL2.GL_LINES);

            for (NodeLayout layout : nodes)
            {
                int vertAdjust = layout.bounds.height - selectedSymbolSize.height
                    - (this.lineHeight - selectedSymbolSize.height) / 2;

                String selected = layout.node.isTreeSelected();
                boolean checked = TreeNode.SELECTED.equals(selected);
                if (checked)
                {
                    int x = layout.drawPoint.x;
                    int y = layout.drawPoint.y + vertAdjust;

                    gl.glVertex2f(x + selectedSymbolSize.width * 0.3f - 1, y + selectedSymbolSize.height * 0.6f);
                    gl.glVertex2f(x + selectedSymbolSize.width * 0.3f - 1, y + selectedSymbolSize.height * 0.2f + 1);

                    gl.glVertex2f(x + selectedSymbolSize.width * 0.3f - 1, y + selectedSymbolSize.height * 0.2f + 1);
                    gl.glVertex2f(x + selectedSymbolSize.width * 0.8f - 1, y + selectedSymbolSize.height * 0.8f);
                }
            }
        }
        finally
        {
            gl.glEnd(); // Lines
            gl.glDisable(GL.GL_LINE_SMOOTH);
        }
    }

    /**
     * Draw triangles to indicate that the nodes are expanded or collapsed.
     *
     * @param dc    Current draw context.
     * @param nodes Visible nodes.
     */
    protected void drawTriangles(DrawContext dc, Iterable<NodeLayout> nodes)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        Dimension symbolSize = this.getNodeStateSymbolSize();

        int halfHeight = symbolSize.height / 2;
        int halfWidth = symbolSize.width / 2;

        int iconSpace = this.getActiveAttributes().getIconSpace();
        int pickWidth = symbolSize.width + iconSpace;

        if (!dc.isPickingMode())
        {
            TreeAttributes attributes = this.getActiveAttributes();

            Color color = attributes.getColor();

            gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
            gl.glLineWidth(1f);
            OGLUtil.applyColor(gl, color, 1, false);

            gl.glBegin(GL2.GL_TRIANGLES);
        }
        else
        {
            gl.glBegin(GL2.GL_QUADS); // Draw pick areas as rectangles, not triangles
        }

        try
        {
            for (NodeLayout layout : nodes)
            {

                // If the node is not a leaf, draw a symbol to indicate if it is expanded or collapsed
                if (!layout.node.isLeaf())
                {
                    int x = layout.drawPoint.x;
                    int y = layout.drawPoint.y;

                    if (!dc.isPickingMode())
                    {
                        x += halfWidth;
                        y += halfHeight;

                        if (this.tree.isNodeExpanded(layout.node))
                        {
                            int vertAdjust = layout.bounds.height - halfWidth - (this.lineHeight - halfWidth) / 2;
                            y += vertAdjust;

                            // Draw triangle pointing down
                            gl.glVertex2i(x - halfHeight, y);
                            gl.glVertex2i(x, -halfWidth + y);
                            gl.glVertex2i(x + halfHeight, y);
                        }
                        else
                        {
                            int vertAdjust = layout.bounds.height - symbolSize.height
                                - (this.lineHeight - symbolSize.height) / 2;
                            y += vertAdjust;

                            // Draw triangle pointing right
                            gl.glVertex2f(x, -halfHeight + y - 0.5f);
                            gl.glVertex2f(x + halfWidth, y);
                            gl.glVertex2f(x, halfHeight + y - 0.5f);
                        }
                    }
                    else
                    {
                        Color color = dc.getUniquePickColor();
                        int colorCode = color.getRGB();
                        this.pickSupport.addPickableObject(colorCode,
                            this.createTogglePathControl(this.tree, layout.node));
                        gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

                        x = this.screenLocation.x;
                        int width = (layout.drawPoint.x + pickWidth) - x;
                        y = (int) layout.screenBounds.getMaxY() - this.lineHeight;

                        gl.glVertex2f(x, y);
                        gl.glVertex2f(x, y + this.lineHeight);
                        gl.glVertex2f(x + width, y + this.lineHeight);
                        gl.glVertex2f(x + width, y);
                    }
                }

                if (this.isDrawNodeStateSymbol())
                    layout.drawPoint.x += this.getNodeStateSymbolSize().width
                        + this.getActiveAttributes().getIconSpace();
            }
        }
        finally
        {
            gl.glEnd(); // Triangles if drawing, quads if picking
        }
    }

    /**
     * Determine the tree layout. This method determines which nodes are visible, and where they will be drawn.
     *
     * @param root      Root node of the subtree to render.
     * @param dc        Draw context.
     * @param frameSize Size of the frame into which the tree will render.
     * @param location  Location at which to draw the node. The location specifies the upper left corner of the
     *                  subtree.
     * @param level     The level of this node in the tree. The root node is at level 1, its child nodes are at level 2,
     *                  etc.
     * @param nodes     List to collect nodes that are currently visible. This method adds nodes to this list.
     */
    protected void computeTreeLayout(TreeNode root, DrawContext dc, Dimension frameSize, Point location, int level,
        java.util.List<NodeLayout> nodes)
    {
        TreeAttributes attributes = this.getActiveAttributes();

        int oldX = location.x;

        if (this.mustDisplayNode(root, level))
        {
            Dimension size = this.getNodeSize(dc, frameSize, location.x, root, attributes);

            // Adjust y to the bottom of the node area
            int y = location.y - (size.height + this.getActiveAttributes().getRowSpacing());
            Rectangle nodeBounds = new Rectangle(location.x, y, size.width, size.height);

            NodeLayout layout = this.layoutCache.get(root);
            if (layout == null)
                layout = new NodeLayout(root);

            layout.bounds = nodeBounds;

            // Compute pick bounds for the node that include the row spacing above and below the node, and the full
            // width of the frame.
            int rowSpacing = attributes.getRowSpacing();
            layout.pickBounds = new Rectangle(0, nodeBounds.y - rowSpacing, frameSize.width,
                nodeBounds.height + rowSpacing * 2);

            nodes.add(layout);

            location.x += this.indent;
            location.y -= (size.height + this.getActiveAttributes().getRowSpacing());
        }

        // Draw child nodes if the root node is expanded.
        if (this.tree.isNodeExpanded(root))
        {
            for (TreeNode child : root.getChildren())
            {
                this.computeTreeLayout(child, dc, frameSize, location, level + 1, nodes);
            }
        }
        location.x = oldX; // Restore previous indent level
    }

    /**
     * Find the bounds of a node in the tree.
     *
     * @param needle    The node to find.
     * @param haystack  Root node of the subtree to search.
     * @param dc        Draw context.
     * @param frameSize Size of the frame into which the tree is rendered.
     * @param location  Point in OpenGL screen coordinates (origin lower left corner) that defines the upper left corner
     *                  of the subtree.
     * @param level     Level of this subtree in the tree. The root node is level 1, its children are level 2, etc.
     *
     * @return Bounds of the node {@code needle}.
     */
    protected Rectangle findNodeBounds(TreeNode needle, TreeNode haystack, DrawContext dc, Dimension frameSize,
        Point location, int level)
    {
        TreeAttributes attributes = this.getActiveAttributes();

        int oldX = location.x;

        if (level > 1 || attributes.isRootVisible())
        {
            Dimension size = this.getNodeSize(dc, frameSize, location.x, haystack, attributes);

            // Adjust y to the bottom of the node area
            location.y -= (size.height + this.getActiveAttributes().getRowSpacing());

            Rectangle nodeBounds = new Rectangle(location.x, location.y, size.width, size.height);

            if (haystack.getPath().equals(needle.getPath()))
                return nodeBounds;

            location.x += level * this.indent;
        }

        // Draw child nodes if the root node is expanded
        if (this.tree.isNodeExpanded(haystack))
        {
            for (TreeNode child : haystack.getChildren())
            {
                Rectangle bounds = this.findNodeBounds(needle, child, dc, frameSize, location, level + 1);
                if (bounds != null)
                    return bounds;
            }
        }
        location.x = oldX; // Restore previous indent level

        return null;
    }

    /** {@inheritDoc} */
    public synchronized void makeVisible(TreePath path)
    {
        TreeNode node = this.tree.getNode(path);
        if (node == null)
            return;

        TreeNode parent = node.getParent();
        while (parent != null)
        {
            this.tree.expandPath(parent.getPath());
            parent = parent.getParent();
        }

        // Set the scrollToNode field. This field will be read during rendering, and the frame will be
        // scrolled appropriately.
        this.scrollToNode = node;
    }

    /**
     * Get the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @return Screen location, measured in pixels from the upper left corner of the screen.
     */
    public Offset getScreenLocation()
    {
        return this.frame.getScreenLocation();
    }

    /**
     * Set the location of the upper left corner of the tree, measured in screen coordinates with the origin at the
     * upper left corner of the screen.
     *
     * @param screenLocation New screen location.
     */
    public void setScreenLocation(Offset screenLocation)
    {
        frame.setScreenLocation(screenLocation);
    }

    /** {@inheritDoc} */
    public TreeAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    /** {@inheritDoc} */
    public void setAttributes(TreeAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.normalAttributes = attributes;
    }

    /**
     * Get the attributes to apply when the tree is highlighted.
     *
     * @return Attributes to use when tree is highlighted.
     */
    public TreeAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /**
     * Set the attributes to use when the tree is highlighted.
     *
     * @param attributes New highlight attributes.
     */
    public void setHighlightAttributes(TreeAttributes attributes)
    {
        if (attributes == null)
        {
            String msg = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.highlightAttributes = attributes;
    }

    /**
     * Get the active attributes, based on the highlight state.
     *
     * @return Highlight attributes if the tree is highlighted. Otherwise, the normal attributes.
     */
    protected TreeAttributes getActiveAttributes()
    {
        return this.activeAttributes;
    }

    /** Determines which attributes -- normal, highlight or default -- to use each frame. */
    protected void determineActiveAttributes()
    {
        TreeAttributes newAttributes = defaultAttributes;

        if (this.isHighlighted())
        {
            if (this.getHighlightAttributes() != null)
                newAttributes = this.getHighlightAttributes();
            else
            {
                // If no highlight attributes have been specified we will use the normal attributes.
                if (this.getAttributes() != null)
                    newAttributes = this.getAttributes();
                else
                    newAttributes = defaultAttributes;
            }
        }
        else if (this.getAttributes() != null)
        {
            newAttributes = this.getAttributes();
        }

        // If the attributes have changed since the last frame, change the update time since the tree needs to repaint
        if (!newAttributes.equals(this.activeAttributes))
        {
            this.markUpdated();
        }

        this.activeAttributes.copy(newAttributes);
    }

    /**
     * Is the tree highlighted? The tree is highlighted when the mouse is within the bounds of the containing frame.
     *
     * @return True if the tree is highlighted.
     */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /**
     * Set the tree layout to highlighted or not highlighted.
     *
     * @param highlighted True if the tree should be highlighted.
     */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /**
     * Get the frame that surrounds the tree.
     *
     * @return The frame that the tree is drawn on.
     */
    public ScrollFrame getFrame()
    {
        return this.frame;
    }

    /**
     * Compute the size of a node.
     *
     * @param dc         Current draw context.
     * @param frameSize  Size of the frame into which the tree is rendered.
     * @param x          Offset in pixels from the left side of the screen to the left most part of the node.
     * @param node       Node for which to compute bounds.
     * @param attributes Attributes to use for bounds calculation.
     *
     * @return The dimensions of the node.
     */
    public Dimension getNodeSize(DrawContext dc, Dimension frameSize, int x, TreeNode node, TreeAttributes attributes)
    {
        Dimension size = new Dimension();

        // Find bounds of the node icon.
        if (node.hasImage())
        {
            Dimension iconSize = attributes.getIconSize();
            if (iconSize.height > size.height)
                size.height = iconSize.height;

            size.width += (iconSize.width + attributes.getIconSpace());
        }

        // Add width of the check box and toggle control, if present
        if (this.isDrawSelectedSymbol())
            size.width += (this.getSelectedSymbolSize().width + attributes.getIconSpace());

        if (this.isDrawNodeStateSymbol())
            size.width += (this.getNodeStateSymbolSize().width + attributes.getIconSpace());

        int textIndent = size.width;
        int textWidth;

        // Find the bounds of the main line of text.
        Rectangle2D textBounds = this.getTextBounds(dc, this.getText(node), attributes.getFont());
        textWidth = (int) textBounds.getWidth();
        size.height = (int) Math.max(size.height, textBounds.getHeight());

        // Find the bounds of the description string, which may be wrapped to multiple lines.
        String description = this.getDescriptionText(node);
        if (description != null)
        {
            Rectangle2D descriptionBounds;

            // Compute bounds based on wrapped text, if text is set to wrap
            if (this.isWrapText() && frameSize != null) // Can't wrap text without frame bounds
            {
                // Estimate the bounds of the wrapped text. Wrapping text is expensive, so we wait until the node
                // is rendered to actually wrap the text. All we need to know here is how many lines we will have.

                int textAreaWidth = frameSize.width - x - textIndent;
                int numLines = this.estimateWrappedTextLines(dc, description, attributes.getDescriptionFont(),
                    textAreaWidth);

                // If the text needs to wrap, then use the text area width as the width of the text since this is the
                // edge that the text wraps to. Otherwise, the text will display on one line, so compute the bounds of the
                // unwrapped text.
                int width;
                if (numLines == 1)
                {
                    descriptionBounds = this.getMultilineTextBounds(dc, description, attributes.getDescriptionFont());
                    width = (int) Math.min(textAreaWidth, descriptionBounds.getWidth());
                }
                else
                {
                    width = textAreaWidth;
                }
                descriptionBounds = new Rectangle(width, numLines * this.lineHeight);

                NodeLayout layout = this.layoutCache.get(node);
                if (layout == null)
                {
                    layout = new NodeLayout(node);
                    this.layoutCache.put(node, layout);
                }
                layout.numLines = numLines;
            }
            else
            {
                descriptionBounds = this.getMultilineTextBounds(dc, description, attributes.getDescriptionFont());
            }

            size.height += (int) Math.abs(descriptionBounds.getHeight());
            size.width += Math.max(textWidth, descriptionBounds.getWidth());
        }
        else
        {
            size.width += textWidth;
        }

        return size;
    }

    protected int estimateWrappedTextLines(DrawContext dc, String text, Font font, int frameWidth)
    {
        boolean containsWhitespace = (text.contains(" ") || text.contains("\t"));

        // If there's no whitespace in the string, then the text can't wrap, it must be one line
        if (!containsWhitespace)
        {
            return 1;
        }
        else
        {
            // Compute the bounds of the first 50 characters of the string, and use this to estimate the length of the
            // full string. Computing the length of a very long description string can be very expensive, and all we're
            // really trying to figure out is whether the line will need to wrap or not.
            int numChars = Math.min(text.length(), 50);
            Rectangle2D estTextBounds = this.getTextBounds(dc, text.substring(0, numChars), font);
            double avgCharWidth = estTextBounds.getWidth() / numChars;

            double textWidth = (int) avgCharWidth * text.length();
            return (int) Math.min(Math.ceil(textWidth / frameWidth), this.getMaxWrappedLines());
        }
    }

    /**
     * Get the wrapped description text for a node. The wrapped text will be cached in the {@link #layoutCache}.
     *
     * @param dc    Current draw context.
     * @param node  Node for which to get wrapped text.
     * @param font  Font to use for the description.
     * @param width Width to which to wrap text.
     *
     * @return The wrapped text as a String. The string will contain newline characters to delimit the lines of wrapped
     *         text.
     */
    protected String computeWrappedText(DrawContext dc, TreeNode node, Font font, int width)
    {
        NodeLayout layout = this.layoutCache.get(node);

        if (layout == null)
        {
            layout = new NodeLayout(node);
            this.layoutCache.put(node, layout);
        }

        String description = node.getDescription();
        if ((layout.wrappedText == null || layout.textWrapWidth != width) && description != null)
        {
            TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
            MultiLineTextRenderer mltr = new MultiLineTextRenderer(textRenderer);

            // Compute the maximum text height from the number of lines. Multiply by 1.5 to ensure that the height is
            // enough to render maxLines, but not enough to render maxLines + 1.
            int maxHeight = (int) (this.lineHeight * layout.numLines + this.lineHeight * 0.5);

            layout.wrappedText = mltr.wrap(description, width, maxHeight);
            layout.textWrapWidth = width;
        }

        return layout.wrappedText;
    }

    /** Invalidate the computed wrapped text, forcing the text wrap to be recomputed. */
    protected void invalidateWrappedText()
    {
        for (Map.Entry<TreeNode, NodeLayout> entry : this.layoutCache.entrySet())
        {
            entry.getValue().wrappedText = null;
        }
    }

    /**
     * Create a pickable object to represent a toggle control in the tree. The toggle control will expand or collapse a
     * node in response to user input.
     *
     * @param tree Tree that contains the node.
     * @param node The node to expand or collapse.
     *
     * @return A {@link TreeHotSpot} that will be added as a pickable object to the screen area occupied by the toggle
     *         control.
     */
    protected HotSpot createTogglePathControl(final Tree tree, final TreeNode node)
    {
        return new TreeHotSpot(this.getFrame())
        {
            @Override
            public void selected(SelectEvent event)
            {
                if (event == null || this.isConsumed(event))
                    return;

                if (event.isLeftClick() || event.isLeftDoubleClick())
                {
                    tree.togglePath(node.getPath());
                    event.consume();
                }
                else
                {
                    super.selected(event);
                }
            }
        };
    }

    /**
     * Create a pickable object to represent selection control in the tree. The selection control will select or
     * deselect a node in response to user input. The returned <code>HotSpot</code> calls <code>{@link
     * #toggleNodeSelection(TreeNode)}</code> upon a left-click select event.
     *
     * @param node The node to expand or collapse.
     *
     * @return A {@link TreeHotSpot} that will be added as a pickable object to the screen area occupied by the toggle
     *         control.
     */
    protected HotSpot createSelectControl(final TreeNode node)
    {
        return new TreeHotSpot(this.getFrame())
        {
            @Override
            public void selected(SelectEvent event)
            {
                if (event == null || this.isConsumed(event))
                    return;

                if (event.isLeftClick() || event.isLeftDoubleClick())
                {
                    toggleNodeSelection(node);
                    event.consume();
                }
                else
                {
                    super.selected(event);
                }
            }
        };
    }

    /**
     * Get the bounds of a text string. This method consults the text bound cache. If the bounds of the input string are
     * not already cached, they will be computed and added to the cache.
     *
     * @param dc   Draw context.
     * @param text Text to get bounds of.
     * @param font Font applied to the text.
     *
     * @return A rectangle that describes the node bounds. See com.jogamp.opengl.util.awt.TextRenderer.getBounds for
     *         information on how this rectangle should be interpreted.
     */
    protected Rectangle2D getTextBounds(DrawContext dc, String text, Font font)
    {
        TextCacheKey cacheKey = new TextCacheKey(text, font);
        Rectangle2D bounds = this.textCache.get(cacheKey);

        if (bounds == null)
        {
            TextRenderer textRenderer = OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), font);
            bounds = textRenderer.getBounds(text);

            this.textCache.put(cacheKey, bounds);
        }

        return bounds;
    }

    /**
     * Get the bounds of a multi-line text string. Each newline character in the input string (\n) indicates the start
     * of a new line.
     *
     * @param dc   Current draw context.
     * @param text Text to find bounds of.
     * @param font Font applied to the text.
     *
     * @return A rectangle that describes the node bounds. See com.jogamp.opengl.util.awt.TextRenderer.getBounds for
     *         information on how this rectangle should be interpreted.
     */
    protected Rectangle2D getMultilineTextBounds(DrawContext dc, String text, Font font)
    {
        int width = 0;
        int maxLineHeight = 0;
        String[] lines = text.split("\n");

        for (String line : lines)
        {
            Rectangle2D lineBounds = this.getTextBounds(dc, line, font);
            width = (int) Math.max(lineBounds.getWidth(), width);
            maxLineHeight = (int) Math.max(lineBounds.getMaxY(), lineHeight);
        }

        // Compute final height using maxLineHeight and number of lines
        return new Rectangle(lines.length, lineHeight, width, lines.length * maxLineHeight);
    }

    /**
     * Toggles the selection state of the specified <code>node</code>. In order to provide an intuitive tree selection
     * model to the application, this changes the selection state of the <code>node</code>'s ancestors and descendants
     * as follows:
     * <p/>
     * <ul> <li>The branch beneath the node it also set to the node's new selection state. Toggling an interior node's
     * selection state causes that entire branch to toggle.</li> <li>The node's ancestors are set to match the node's
     * new selection state. If the new state is <code>false</code>, this stops at the first ancestor with another branch
     * that has a selected node. When an interior or leaf node is toggled, the path to that node is also toggled, except
     * when doing so would clear a selected path to another interior or leaf node.</li> </ul>
     * <p/>
     *
     * @param node the <code>TreeNode</code> who's selection state should be toggled.
     */
    protected void toggleNodeSelection(TreeNode node)
    {
        boolean selected = !node.isSelected();
        node.setSelected(selected);

        // Change the selection state of the node's descendants to match. Toggling an interior node's selection state
        // causes that entire branch to toggle.
        if (!node.isLeaf())
            this.setDescendantsSelected(node, selected);

        // Change the selection state of the node's ancestors to match. If the node's new selection state is true, then
        // mark its ancestors as selected. When an interior or leaf node is selected, the path to that node is also
        // selected. If the node's new selection state is false, then mark its ancestors as not selected, stopping at
        // the first ancestor with a selected child. This avoids clearing a selected path to another interior or leaf
        // node.
        TreeNode parent = node.getParent();
        while (parent != null)
        {
            boolean prevSelected = parent.isSelected();
            parent.setSelected(selected);

            if (!selected && !TreeNode.NOT_SELECTED.equals(parent.isTreeSelected()))
            {
                parent.setSelected(prevSelected);
                break;
            }

            parent = parent.getParent();
        }
    }

    /**
     * Sets the selection state of the branch beneath the specified <code>node</code>.
     *
     * @param node     the <code>TreeNode</code> who descendants selection should be set.
     * @param selected <code>true</code> to mark the descendants and selected, otherwise <code>false</code>.
     */
    protected void setDescendantsSelected(TreeNode node, boolean selected)
    {
        for (TreeNode child : node.getChildren())
        {
            child.setSelected(selected);

            if (!child.isLeaf())
                this.setDescendantsSelected(child, selected);
        }
    }

    /**
     * Get the text for a node.
     *
     * @param node Node to get text for.
     *
     * @return Text for node.
     */
    protected String getText(TreeNode node)
    {
        return node.getText();
    }

    /**
     * Get the description text for a node.
     *
     * @param node Node to get text for.
     *
     * @return Description text for {@code node}. May return null if there is no description.
     */
    protected String getDescriptionText(TreeNode node)
    {
        return node.getDescription();
    }

    /** Cache key for cache text bound cache. */
    protected static class TextCacheKey
    {
        /** Text string. */
        protected String text;
        /** Font used to compute bounds. */
        protected Font font;
        /** Hash code. */
        protected int hash = 0;

        /**
         * Create a cache key for a string rendered in a font.
         *
         * @param text String for which to cache bounds.
         * @param font Font of the rendered string.
         */
        public TextCacheKey(String text, Font font)
        {
            if (text == null)
            {
                String message = Logging.getMessage("nullValue.StringIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (font == null)
            {
                String message = Logging.getMessage("nullValue.FontIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.text = text;
            this.font = font;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            TextCacheKey cacheKey = (TextCacheKey) o;

            return this.text.equals(cacheKey.text) && this.font.equals(cacheKey.font);
        }

        @Override
        public int hashCode()
        {
            if (this.hash == 0)
            {
                int result;
                result = this.text.hashCode();
                result = 31 * result + this.font.hashCode();
                this.hash = result;
            }
            return this.hash;
        }
    }

    /** Class to hold information about how a tree node is laid out. */
    protected static class NodeLayout
    {
        /** Node that this layout applies to. */
        protected TreeNode node;
        /** Node bounds, relative to the bottom left corner of the tree. */
        protected Rectangle bounds;
        protected Rectangle pickBounds;
        /**
         * Node bounds relative to the bottom left corner of the viewport. This field is set by {@link
         * #reset(java.awt.Point)}.
         */
        protected Rectangle screenBounds;
        protected Rectangle pickScreenBounds;

        /** Wrapped version of the node description text. Computed once and then cached here. */
        protected String wrappedText;
        /** The width used to wrap the description text. */
        protected int textWrapWidth;
        /** Number of lines of wrapped description text in this layout. */
        protected int numLines;

        /**
         * Point at which the next component should be drawn. Nodes are drawn left to right, and the draw point is
         * updated as parts of the node are rendered. For example, the toggle triangle is drawn first at the draw point,
         * and then the draw point is moved to the right by the width of the triangle, so the next component will draw
         * at the correct point. The draw point is reset to the lower left corner of the node bounds before each render
         * cycle.
         */
        protected Point drawPoint;

        /**
         * Create a new node layout.
         *
         * @param node Node that is being laid out.
         */
        protected NodeLayout(TreeNode node)
        {
            if (node == null)
            {
                String message = Logging.getMessage("nullValue.TreeNodeIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.node = node;
            this.drawPoint = new Point();
        }

        /**
         * Reset the draw point to the lower left corner of the node bounds.
         *
         * @param treePoint location of the lower left corner of the tree, measured in GL coordinates (origin lower left
         *                  corner of the screen).
         */
        protected void reset(Point treePoint)
        {
            this.drawPoint.x = this.bounds.x + treePoint.x;
            this.drawPoint.y = this.bounds.y + treePoint.y;

            this.screenBounds = new Rectangle(this.drawPoint.x, this.drawPoint.y, this.bounds.width,
                this.bounds.height);

            int pickX = this.pickBounds.x + treePoint.x;
            int pickY = this.pickBounds.y + treePoint.y;
            this.pickScreenBounds = new Rectangle(pickX, pickY, this.pickBounds.width, this.pickBounds.height);
        }
    }
}
