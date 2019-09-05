/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * Displays the layer list in a heads-up display in the viewport. Layers can be turned on and off by clicking the check
 * box next to the layer name. The order of layers in the list can be changed by dragging the layer names.
 *
 * @author Patrick Murris
 * @version $Id: LayerManagerLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class LayerManagerLayer extends RenderableLayer implements SelectListener
{
    protected WorldWindow wwd;
    protected boolean update = true;

    private ScreenAnnotation annotation;
    protected Dimension size;
    private int selectedIndex = -1;
    private Color color = Color.decode("#b0b0b0");
    private Color highlightColor = Color.decode("#ffffff");
    private double minOpacity = .6;
    private double maxOpacity = 1;
    private char layerEnabledSymbol = '\u25a0';
    private char layerDisabledSymbol = '\u25a1';
    private Font font = new Font("SansSerif", Font.PLAIN, 14);
    private boolean minimized = false;
    private int borderWidth = 20; // TODO: make configurable
    private String position = AVKey.SOUTHWEST; // TODO: make configurable
    private Vec4 locationCenter = null;
    private Vec4 locationOffset = null;

    // Dragging
    private boolean componentDragEnabled = true;
    private boolean layerDragEnabled = true;
    private boolean snapToCorners = true;
    protected boolean draggingComponent = false;
    protected boolean draggingLayer = false;
    protected Point dragRefCursorPoint;
    protected Point dragRefPoint;
    protected int dragRefIndex = -1;
    protected Color dragColor = Color.RED;

    public LayerManagerLayer(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.initialize();
    }

    protected void initialize()
    {
        // Set up screen annotation that will display the layer list
        this.annotation = new ScreenAnnotation("", new Point(0, 0));

        // Set annotation so that it will not force text to wrap (large width) and will adjust it's width to
        // that of the text. A height of zero will have the annotation height follow that of the text too.
        this.annotation.getAttributes().setSize(new Dimension(Integer.MAX_VALUE, 0));
        this.annotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);

        // Set appearance attributes
        this.annotation.getAttributes().setCornerRadius(0);
        this.annotation.getAttributes().setFont(this.font);
        this.annotation.getAttributes().setHighlightScale(1);
        this.annotation.getAttributes().setTextColor(Color.WHITE);
        this.annotation.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        this.annotation.getAttributes().setInsets(new Insets(6, 6, 6, 6));
        this.annotation.getAttributes().setBorderWidth(1);
        this.addRenderable(this.annotation);

        // Listen to WorldWindow for select event
        this.wwd.addSelectListener(this);
    }

    /**
     * Get the <code>ScreenAnnotation</code> used to display the layer list.
     *
     * @return the <code>ScreenAnnotation</code> used to display the layer list.
     */
    public ScreenAnnotation getAnnotation()
    {
        return this.annotation;
    }

    public void setEnabled(boolean enabled)
    {
        this.setMinimized(!enabled);
    }

    public boolean isEnabled()
    {
        return !this.isMinimized();
    }

    /**
     * Get the <code>Font</code> used to draw the layer list text.
     *
     * @return the <code>Font</code> used to draw the layer list text.
     */
    public Font getFont()
    {
        return this.font;
    }

    /**
     * Set the <code>Font</code> used to draw the layer list text.
     *
     * @param font the <code>Font</code> used to draw the layer list text.
     */
    public void setFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.font.equals(font))
        {
            this.font = font;
            this.annotation.getAttributes().setFont(font);
            this.update();
        }
    }

    /**
     * Get the <code>Color</code> used to draw the layer names and the frame border when they are not highlighted.
     *
     * @return the <code>Color</code> used to draw the layer names and the frame border when they are not highlighted.
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the <code>Color</code> used to draw the layer names and the frame border when they are not highlighted.
     *
     * @param color the <code>Color</code> used to draw the layer names and the frame border when they are not
     *              highlighted.
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.color = color;
        this.update();
    }

    /**
     * Get the <code>Color</code> used to draw the layer names and the frame border when they are highlighted.
     *
     * @return the <code>Color</code> used to draw the layer names and the frame border when they are highlighted.
     */
    public Color getHighlightColor()
    {
        return this.highlightColor;
    }

    /**
     * Set the <code>Color</code> used to draw the layer names and the frame border when they are highlighted.
     *
     * @param color the <code>Color</code> used to draw the layer names and the frame border when they are highlighted.
     */
    public void setHighlightColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.highlightColor = color;
        this.update();
    }

    /**
     * Get the opacity applied to the layer list when the cursor is outside it's frame.
     *
     * @return the opacity applied to the layer list when the cursor is outside it's frame.
     */
    public double getMinOpacity()
    {
        return this.minOpacity;
    }

    /**
     * Set the opacity applied to the layer list when the cursor is outside it's frame - zero to one, one is fully
     * opaque.
     *
     * @param opacity the opacity applied to the layer list when the cursor is outside it's frame.
     */
    public void setMinOpacity(double opacity)
    {
        this.minOpacity = opacity;
        this.update();
    }

    /**
     * Get the opacity applied to the layer list when the cursor is inside it's frame.
     *
     * @return the opacity applied to the layer list when the cursor is inside it's frame.
     */
    public double getMaxOpacity()
    {
        return this.maxOpacity;
    }

    /**
     * Set the opacity applied to the layer list when the cursor is inside it's frame - zero to one, one is fully
     * opaque.
     *
     * @param opacity the opacity applied to the layer list when the cursor is inside it's frame.
     */
    public void setMaxOpacity(double opacity)
    {
        this.maxOpacity = opacity;
        this.update();
    }

    /**
     * Get the character used to denote an enabled layer.
     *
     * @return the character used to denote an enabled layer.
     */
    public char getLayerEnabledSymbol()
    {
        return this.layerEnabledSymbol;
    }

    /**
     * Set the character used to denote an enabled layer.
     *
     * @param c the character used to denote an enabled layer.
     */
    public void setLayerEnabledSymbol(char c)
    {
        this.layerEnabledSymbol = c;
        this.update();
    }

    /**
     * Get the character used to denote a disabled layer.
     *
     * @return the character used to denote a disabled layer.
     */
    public char getLayerDisabledSymbol()
    {
        return this.layerDisabledSymbol;
    }

    /**
     * Set the character used to denote a disabled layer.
     *
     * @param c the character used to denote a disabled layer.
     */
    public void setLayerDisabledSymbol(char c)
    {
        this.layerDisabledSymbol = c;
        this.update();
    }

    /**
     * Get the layer manager frame offset from the viewport borders.
     *
     * @return the number of pixels to offset the layer manager frame from the borders indicated by {@link
     *         #setPosition(String)}.
     */
    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * Sets the layer manager frame offset from the viewport borders.
     *
     * @param borderWidth the number of pixels to offset the layer manager frame from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
        this.update();
    }

    /**
     * Returns the current relative layer manager frame position.
     *
     * @return the current layer manager frame position
     */
    public String getPosition()
    {
        return position;
    }

    /**
     * Sets the relative viewport location to display the layer manager. Can be one of {@link AVKey#NORTHEAST} (the
     * default), {@link AVKey#NORTHWEST}, {@link AVKey#SOUTHEAST}, or {@link AVKey#SOUTHWEST}. These indicate the corner
     * of the viewport to place the frame.
     *
     * @param position the desired layer manager position
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.ScreenPositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.position = position;
        this.update();
    }

    /**
     * Returns the current layer manager location.
     *
     * @return the current location center. May be null.
     */
    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    /**
     * Specifies the screen location of the layer manager, relative to it's frame center. May be null. If this value is
     * non-null, it overrides the position specified by #setPosition. The location is specified in pixels. The origin is
     * the window's lower left corner. Positive X values are to the right of the origin, positive Y values are upwards
     * from the origin. The final frame location will be affected by the currently specified location offset if a
     * non-null location offset has been specified (see {@link #setLocationOffset(Vec4)}).
     *
     * @param locationCenter the location center. May be null.
     *
     * @see #setPosition(String)
     * @see #setLocationOffset(gov.nasa.worldwind.geom.Vec4)
     */
    public void setLocationCenter(Vec4 locationCenter)
    {
        this.locationCenter = locationCenter;
        this.update();
    }

    /**
     * Returns the current location offset. See #setLocationOffset for a description of the offset and its values.
     *
     * @return the location offset. Will be null if no offset has been specified.
     */
    public Vec4 getLocationOffset()
    {
        return locationOffset;
    }

    /**
     * Specifies a placement offset from the layer manager frame position on the screen.
     *
     * @param locationOffset the number of pixels to shift the layer manager frame from its specified screen position. A
     *                       positive X value shifts the frame to the right. A positive Y value shifts the frame up. If
     *                       null, no offset is applied. The default offset is null.
     *
     * @see #setLocationCenter(gov.nasa.worldwind.geom.Vec4)
     * @see #setPosition(String)
     */
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset;
        this.update();
    }

    /**
     * Determines whether the layer list frame is minimized. When minimized, the layer list only contains itself as the
     * only item, and thus shrinks toward it's corner position.
     *
     * @return <code>true</code> if the layer list frame is minimized.
     */
    public boolean isMinimized()
    {
        return this.minimized;
    }

    /**
     * Set the layer list frame to be minimized. When minimized, the layer list only contains itself as the only item,
     * and thus shrinks toward it's corner position.
     *
     * @param minimized <code>true</code> if the layer list frame sould be minimized.
     */
    public void setMinimized(boolean minimized)
    {
        this.minimized = minimized;
        this.update();
    }

    /**
     * Determines whether the layer list can be moved or dragged with the mouse cursor.
     * <p>
     * If enabled, dragging the frame will result in a change to it's location offset - {@link
     * #setLocationOffset(Vec4)}. If the list is also set to snap to corners - {@link #setSnapToCorners(boolean)}, the
     * frame position may change so as to be attached to the nearest corner - see {@link #setPosition(String)}.
     *
     * @return <code>true</code> if the layer list can be moved or dragged with the mouse cursor.
     */
    public boolean isComponentDragEnabled()
    {
        return this.componentDragEnabled;
    }

    /**
     * Sets whether the layer list can be moved or dragged with the mouse cursor.
     * <p>
     * If enabled, dragging the frame will result in a change to it's location offset - {@link
     * #setLocationOffset(Vec4)}. If the list is also set to snap to corners - {@link #setSnapToCorners(boolean)}, the
     * frame position may change so as to be attached to the nearest corner - see {@link #setPosition(String)}.
     *
     * @param enabled <code>true</code> if the layer list can be moved or dragged with the mouse cursor.
     */
    public void setComponentDragEnabled(boolean enabled)
    {
        this.componentDragEnabled = enabled;
    }

    /**
     * Determines whether a layer can be moved or dragged within the list with the mouse cursor. If enabled, layers can
     * be moved up and down the list.
     *
     * @return <code>true</code> if a layer can be moved or dragged within the list.
     */
    public boolean isLayerDragEnabled()
    {
        return this.layerDragEnabled;
    }

    /**
     * Sets whether a layer can be moved or dragged within the list with the mouse cursor. If enabled, layers can be
     * moved up and down the list.
     *
     * @param enabled <code>true</code> if a layer can be moved or dragged within the list.
     */
    public void setLayerDragEnabled(boolean enabled)
    {
        this.layerDragEnabled = enabled;
    }

    /**
     * Determines whether the layer list snaps to the viewport sides and corners while being dragged.
     * <p>
     * Dragging the layer list frame will result in a change to it's location offset - {@link #setLocationOffset(Vec4)}.
     * If the list is also set to snap to corners - {@link #setSnapToCorners(boolean)}, the frame position may change so
     * as to be attached to the nearest corner - see {@link #setPosition(String)}.
     *
     * @return <code>true</code> if the layer list snaps to the viewport sides and corners while being dragged.
     */
    public boolean isSnapToCorners()
    {
        return this.snapToCorners;
    }

    /**
     * Sets whether the layer list snaps to the viewport sides and corners while being dragged.
     * <p>
     * Dragging the layer list frame will result in a change to it's location offset - {@link #setLocationOffset(Vec4)}.
     * If the list is also set to snap to corners the frame position may change so as to be attached to the nearest
     * corner - see {@link #setPosition(String)}.
     *
     * @param enabled <code>true</code> if the layer list should snaps to the viewport sides and corners while being
     *                dragged.
     */
    public void setSnapToCorners(boolean enabled)
    {
        this.snapToCorners = enabled;
    }

    /**
     * Get the selected layer index number in the current <code>Model</code> layer list. A layer is selected when the
     * cursor is over it in the list. Returns -1 if no layer is currently selected.
     *
     * @return the selected layer index number or -1 if none is selected.
     */
    public int getSelectedIndex()
    {
        return this.selectedIndex;
    }

    /**
     * Set the selected layer index number. When selected a layer is highlighted in the list - this usually happens when
     * the cursor is over it.
     *
     * @param index the selected layer index number.
     */
    public void setSelectedIndex(int index)
    {
        this.selectedIndex = index;
        this.update();
    }

    /**
     * <code>SelectListener</code> implementation.
     *
     * @param event the current <code>SelectEvent</code>
     */
    public void selected(SelectEvent event)
    {
        if (event.hasObjects() && event.getTopObject() == this.annotation)
        {
            boolean update = false;
            if (event.getEventAction().equals(SelectEvent.ROLLOVER)
                || event.getEventAction().equals(SelectEvent.LEFT_CLICK))
            {
                // Highlight annotation
                if (!this.annotation.getAttributes().isHighlighted())
                {
                    this.annotation.getAttributes().setHighlighted(true);
                    update = true;
                }
                // Check for text or url
                PickedObject po = event.getTopPickedObject();
                if (po.getValue(AVKey.URL) != null)
                {
                    // Set cursor hand on hyperlinks
                    ((Component) this.wwd).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    int i = Integer.parseInt((String) po.getValue(AVKey.URL));
                    // Select current hyperlink
                    if (this.selectedIndex != i)
                    {
                        this.selectedIndex = i;
                        update = true;
                    }
                    // Enable/disable layer on left click
                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                    {
                        LayerList layers = wwd.getModel().getLayers();
                        if (i >= 0 && i < layers.size())
                        {
                            layers.get(i).setEnabled(!layers.get(i).isEnabled());
                            update = true;
                        }
                    }
                }
                else
                {
                    // Unselect if not on an hyperlink
                    if (this.selectedIndex != -1)
                    {
                        this.selectedIndex = -1;
                        update = true;
                    }
                    // Set cursor
                    if (this.isComponentDragEnabled())
                        ((Component) this.wwd).setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    else
                        ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
                }
            }
            if (event.getEventAction().equals(SelectEvent.DRAG)
                || event.getEventAction().equals(SelectEvent.DRAG_END))
            {
                // Handle dragging
                if (this.isComponentDragEnabled() || this.isLayerDragEnabled())
                {
                    boolean wasDraggingLayer = this.draggingLayer;
                    this.drag(event);
                    // Update list if dragging a layer, otherwise just redraw the WorldWindow
                    if (this.draggingLayer || wasDraggingLayer)
                        update = true;
                    else
                        this.wwd.redraw();
                    event.consume();
                }
            }
            // Redraw annotation if needed
            if (update)
                this.update();
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && this.annotation.getAttributes().isHighlighted())
        {
            // de-highlight annotation
            this.annotation.getAttributes().setHighlighted(false);
            ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
            this.update();
        }
    }

    protected void drag(SelectEvent event)
    {
        if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            if ((this.isComponentDragEnabled() && this.selectedIndex == -1 && this.dragRefIndex == -1)
                || this.draggingComponent)
            {
                // Dragging the whole list
                if (!this.draggingComponent)
                {
                    this.dragRefCursorPoint = event.getMouseEvent().getPoint();
                    this.dragRefPoint = this.annotation.getScreenPoint();
                    this.draggingComponent = true;
                }
                Point cursorOffset = new Point(event.getMouseEvent().getPoint().x - this.dragRefCursorPoint.x,
                    event.getMouseEvent().getPoint().y - this.dragRefCursorPoint.y);
                Point targetPoint = new Point(this.dragRefPoint.x + cursorOffset.x,
                    this.dragRefPoint.y - cursorOffset.y);
                this.moveTo(targetPoint);
                event.consume();
            }
            else if (this.isLayerDragEnabled())
            {
                // Dragging a layer inside the list
                if (!this.draggingLayer)
                {
                    this.dragRefIndex = this.selectedIndex;
                    this.draggingLayer = true;
                }
                if (this.selectedIndex != -1 && this.dragRefIndex != -1 && this.dragRefIndex != this.selectedIndex)
                {
                    // Move dragged layer
                    LayerList layers = this.wwd.getModel().getLayers();
                    int insertIndex = this.dragRefIndex > this.selectedIndex ?
                        this.selectedIndex : this.selectedIndex + 1;
                    int removeIndex = this.dragRefIndex > this.selectedIndex ?
                        this.dragRefIndex + 1 : this.dragRefIndex;
                    layers.add(insertIndex, layers.get(this.dragRefIndex));
                    layers.remove(removeIndex);
                    this.dragRefIndex = this.selectedIndex;
                    event.consume();
                }
            }
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.draggingComponent = false;
            this.draggingLayer = false;
            this.dragRefIndex = -1;
        }
    }

    protected void moveTo(Point targetPoint)
    {
        Point refPoint = this.annotation.getScreenPoint();
        if (this.locationOffset == null)
            this.locationOffset = Vec4.ZERO;

        // Compute appropriate offset
        int x = (int) this.locationOffset.x - (refPoint.x - targetPoint.x);
        int y = (int) this.locationOffset.y - (refPoint.y - targetPoint.y);
        this.locationOffset = new Vec4(x, y, 0);

        // Compensate for rounding errors
        Point computedPoint = this.computeLocation(this.wwd.getView().getViewport());
        x += targetPoint.x - computedPoint.x;
        y += targetPoint.y - computedPoint.y;
        this.locationOffset = new Vec4(x, y, 0);

        if (this.snapToCorners)
            this.snapToCorners();
    }

    protected void snapToCorners()
    {
        // TODO: handle annotation scaling
        int width = this.size.width;
        int height = this.size.height;
        Rectangle viewport = this.wwd.getView().getViewport();
        Point refPoint = this.computeLocation(viewport);
        Point centerPoint = new Point(refPoint.x + width / 2, refPoint.y + height / 2);

        // Find closest corner position
        String newPos;
        if (centerPoint.x > viewport.width / 2)
            newPos = (centerPoint.y > viewport.height / 2) ? AVKey.NORTHEAST : AVKey.SOUTHEAST;
        else
            newPos = (centerPoint.y > viewport.height / 2) ? AVKey.NORTHWEST : AVKey.SOUTHWEST;

        // Adjust offset if position changed
        int x = 0, y = 0;
        if (newPos.equals(this.getPosition()))
        {
            x = (int) this.locationOffset.x;
            y = (int) this.locationOffset.y;
        }
        else
        {
            if (newPos.equals(AVKey.NORTHEAST))
            {
                x = refPoint.x - (viewport.width - width - this.borderWidth);
                y = refPoint.y - (viewport.height - height - this.borderWidth);
            }
            else if (newPos.equals(AVKey.SOUTHEAST))
            {
                x = refPoint.x - (viewport.width - width - this.borderWidth);
                y = refPoint.y - this.borderWidth;
            }
            if (newPos.equals(AVKey.NORTHWEST))
            {
                x = refPoint.x - this.borderWidth;
                y = refPoint.y - (viewport.height - height - this.borderWidth);
            }
            else if (newPos.equals(AVKey.SOUTHWEST))
            {
                x = refPoint.x - this.borderWidth;
                y = refPoint.y - this.borderWidth;
            }
        }

        // Snap to edges
        x = Math.abs(x) < 16 ? 0 : x;
        y = Math.abs(y) < 16 ? 0 : y;

        this.position = newPos;
        this.locationOffset = new Vec4(x, y, 0);
    }

    /** Schedule the layer list for redrawing before the next render pass. */
    public void update()
    {
        this.update = true;
        this.wwd.redraw();
    }

    /**
     * Force the layer list to redraw itself from the current <code>Model</code> with the current highlighted state and
     * selected layer colors and opacity.
     *
     * @param dc the current {@link DrawContext}.
     *
     * @see #setMinOpacity(double)
     * @see #setMaxOpacity(double)
     * @see #setColor(java.awt.Color)
     * @see #setHighlightColor(java.awt.Color)
     */
    public void updateNow(DrawContext dc)
    {
        // Adjust annotation appearance to highlighted state
        this.highlight(this.annotation.getAttributes().isHighlighted());

        // Compose html text
        String text = this.makeAnnotationText(this.wwd.getModel().getLayers());
        this.annotation.setText(text);

        // Update current size and adjust annotation draw offset according to it's width
        // TODO: handle annotation scaling
        this.size = this.annotation.getPreferredSize(dc);
        this.annotation.getAttributes().setDrawOffset(new Point(this.size.width / 2, 0));

        // Clear update flag
        this.update = false;
    }

    /**
     * Change the annotation appearance according to the given highlighted state.
     *
     * @param highlighted <code>true</code> if the annotation should appear highlighted.
     */
    protected void highlight(boolean highlighted)
    {
        // Adjust border color and annotation opacity
        if (highlighted)
        {
            this.annotation.getAttributes().setBorderColor(this.highlightColor);
            this.annotation.getAttributes().setOpacity(this.maxOpacity);
        }
        else
        {
            this.annotation.getAttributes().setBorderColor(this.color);
            this.annotation.getAttributes().setOpacity(this.minOpacity);
        }
    }

    /**
     * Compose the annotation text from the given <code>LayerList</code>.
     *
     * @param layers the <code>LayerList</code> to draw names from.
     *
     * @return the annotation text to be displayed.
     */
    protected String makeAnnotationText(LayerList layers)
    {
        // Compose html text
        StringBuilder text = new StringBuilder();
        Color color;
        int i = 0;
        for (Layer layer : layers)
        {
            if (!this.isMinimized() || layer == this)
            {
                color = (i == this.selectedIndex) ? this.highlightColor : this.color;
                color = (i == this.dragRefIndex) ? dragColor : color;
                text.append("<a href=\"");
                text.append(i);
                text.append("\"><font color=\"");
                text.append(encodeHTMLColor(color));
                text.append("\">");
                text.append((layer.isEnabled() ? layerEnabledSymbol : layerDisabledSymbol));
                text.append(" ");
                text.append((layer.isEnabled() ? "<b>" : "<i>"));
                text.append(layer.getName());
                text.append((layer.isEnabled() ? "</b>" : "</i>"));
                text.append("</a><br />");
            }
            i++;
        }
        return text.toString();
    }

    protected static String encodeHTMLColor(Color c)
    {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public void render(DrawContext dc)
    {
        if (this.update)
            this.updateNow(dc);

        this.annotation.setScreenPoint(computeLocation(dc.getView().getViewport()));
        super.render(dc);
    }

    /**
     * Compute the draw frame south-west corner screen location according to it's position - see {@link
     * #setPosition(String)}, location offset - see {@link #setLocationOffset(Vec4)}, or location center - see {@link
     * #setLocationCenter(Vec4)}, and border distance from the viewport edges - see {@link #setBorderWidth(int)}.
     *
     * @param viewport the current <code>Viewport</code> rectangle.
     *
     * @return the draw frame south-west corner screen location.
     */
    protected Point computeLocation(Rectangle viewport)
    {
        // TODO: handle annotation scaling
        int width = this.size.width;
        int height = this.size.height;

        int x;
        int y;

        if (this.locationCenter != null)
        {
            x = (int) this.locationCenter.x - width / 2;
            y = (int) this.locationCenter.y - height / 2;
        }
        else if (this.position.equals(AVKey.NORTHEAST))
        {
            x = (int) viewport.getWidth() - width - this.borderWidth;
            y = (int) viewport.getHeight() - height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHEAST))
        {
            x = (int) viewport.getWidth() - width - this.borderWidth;
            //noinspection SuspiciousNameCombination
            y = this.borderWidth;
        }
        else if (this.position.equals(AVKey.NORTHWEST))
        {
            x = this.borderWidth;
            y = (int) viewport.getHeight() - height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHWEST))
        {
            x = this.borderWidth;
            //noinspection SuspiciousNameCombination
            y = this.borderWidth;
        }
        else // use North East as default
        {
            x = (int) viewport.getWidth() - width - this.borderWidth;
            y = (int) viewport.getHeight() - height - this.borderWidth;
        }

        if (this.locationOffset != null)
        {
            x += this.locationOffset.x;
            y += this.locationOffset.y;
        }

        return new Point(x, y);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.LayerManagerLayer.Name");
    }
}
