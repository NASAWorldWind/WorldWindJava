/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Base class for tactical graphics. See the TacticalGraphic <a title="Tactical Graphic Usage Guide"
 * href="http://goworldwind.org/developers-guide/symbology/tactical-graphics/" target="_blank">Usage Guide</a> for
 * instructions on using TacticalGraphic in an application. This base class provides functionality for creating and
 * rendering a graphic that is made up of one or more shapes, and text labels.
 * <p/>
 * Implementations must implement at least {@link #doRenderGraphic(gov.nasa.worldwind.render.DrawContext)
 * doRenderGraphic} and {@link #applyDelegateOwner(Object)}.
 *
 * @author pabercrombie
 * @version $Id: AbstractTacticalGraphic.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public abstract class AbstractTacticalGraphic extends AVListImpl implements TacticalGraphic, Renderable, Draggable
{
    /** The default highlight color. */
    protected static final Material DEFAULT_HIGHLIGHT_MATERIAL = Material.WHITE;
    /**
     * Opacity of label interiors. This value is multiplied by the label text opacity to determine the final interior
     * opacity.
     */
    protected static final double DEFAULT_LABEL_INTERIOR_OPACITY = 0.7;

    /**
     * The graphic's text string. This field corresponds to the {@link SymbologyConstants#UNIQUE_DESIGNATION} modifier.
     * Note that this field is not used if an Iterable is specified as the unique designation.
     */
    protected String text;

    /** Indicates whether or not the graphic is highlighted. */
    protected boolean highlighted;
    /** Indicates whether or not to render the graphic. */
    protected boolean visible = true;
    /** Indicates whether or not to render text modifiers. */
    protected boolean showTextModifiers = true;
    /** Indicates whether or not to render graphic modifiers. */
    protected boolean showGraphicModifiers = true;
    /** Indicates whether this object can be dragged. */
    protected boolean dragEnabled = true;
    /** Provides additional information for dragging regarding this particular object. */
    protected DraggableSupport draggableSupport = null;

    // Implementation note: by default, show the hostile indicator (the letters "ENY"). Note that this default is
    // different from MilStd2525TacticalSymbol, which does not display the hostile indicator by default. Section 5.5.1.1
    // (pg. 37) of MIL-STD-2525C states that the indicator is not required if color is used in the display. We choose to
    // display the indicator by default following the principle that by default hostile entities should look as
    // hostile as possible (to avoid being mistaken for friendly entities). In the case of tactical symbols, however,
    // the indicator is redundant to both the symbol frame and fill, so it is not displayed by default.
    /** Indicates whether or not to render the hostile/enemy modifier. This modifier is displayed by default. */
    protected boolean showHostileIndicator = true;
    /** Indicates whether or not to render the location modifier. */
    protected boolean showLocation = true;

    /** Object returned during picking to represent this graphic. */
    protected Object delegateOwner;
    /** Unit format used to format location and altitude for text modifiers. */
    protected UnitsFormat unitsFormat = AbstractTacticalSymbol.DEFAULT_UNITS_FORMAT;

    /**
     * Attributes to apply when the graphic is not highlighted. These attributes override defaults determined by the
     * graphic's symbol code.
     */
    protected TacticalGraphicAttributes normalAttributes;
    /**
     * Attributes to apply when the graphic is highlighted. These attributes override defaults determined by the
     * graphic's symbol code.
     */
    protected TacticalGraphicAttributes highlightAttributes;

    /** Offset applied to the graphic's main label. */
    protected Offset labelOffset;
    /** Labels to render with the graphic. */
    protected List<TacticalGraphicLabel> labels;

    /**
     * Map of modifiers applied to this graphic. Note that implementations may not store all modifiers in this map. Some
     * modifiers may be handled specially.
     */
    protected AVList modifiers;

    /** Current frame timestamp. */
    protected long frameTimestamp = -1L;

    /** Override attributes for the current frame. */
    protected TacticalGraphicAttributes activeOverrides = new BasicTacticalGraphicAttributes();
    /**
     * Shape attributes shared by all shapes that make up this graphic. The graphic's active attributes are copied into
     * this attribute bundle on each frame.
     */
    protected ShapeAttributes activeShapeAttributes = new BasicShapeAttributes();

    /** Flag to indicate that labels must be recreated before the graphic is rendered. */
    protected boolean mustCreateLabels = true;

    /**
     * Render this graphic, without modifiers.
     *
     * @param dc Current draw context.
     *
     * @see #doRenderTextModifiers(gov.nasa.worldwind.render.DrawContext)
     * @see #doRenderGraphicModifiers(gov.nasa.worldwind.render.DrawContext)
     */
    protected abstract void doRenderGraphic(DrawContext dc);

    /**
     * Invoked each frame to apply to the current delegate owner to all renderable objects used to draw the graphic.
     * This base class will apply the delegate owner to Label objects. Subclasses must implement this method to apply
     * the delegate owner to any Renderables that they will draw in order to render the graphic.
     *
     * @param owner Current delegate owner.
     */
    protected abstract void applyDelegateOwner(Object owner);

    /** {@inheritDoc} */
    public Object getModifier(String modifier)
    {
        return this.modifiers != null ? this.modifiers.getValue(modifier) : null;
    }

    /** {@inheritDoc} */
    public void setModifier(String modifier, Object value)
    {
        if (this.modifiers == null)
            this.modifiers = new AVListImpl();

        this.modifiers.setValue(modifier, value);
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public boolean isShowTextModifiers()
    {
        return this.showTextModifiers;
    }

    /** {@inheritDoc} */
    public void setShowTextModifiers(boolean showModifiers)
    {
        this.showTextModifiers = showModifiers;
    }

    /** {@inheritDoc} */
    public boolean isShowGraphicModifiers()
    {
        return this.showGraphicModifiers;
    }

    /** {@inheritDoc} */
    public void setShowGraphicModifiers(boolean showModifiers)
    {
        this.showGraphicModifiers = showModifiers;
    }

    /** {@inheritDoc} */
    public boolean isShowHostileIndicator()
    {
        return this.showHostileIndicator;
    }

    /** {@inheritDoc} */
    public void setShowHostileIndicator(boolean showHostileIndicator)
    {
        this.showHostileIndicator = showHostileIndicator;
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public boolean isShowLocation()
    {
        return this.showLocation;
    }

    /** {@inheritDoc} */
    public void setShowLocation(boolean showLocation)
    {
        this.showLocation = showLocation;
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public String getText()
    {
        return this.text;
    }

    /** {@inheritDoc} */
    public void setText(String text)
    {
        this.text = text;
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return this.visible;
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /** {@inheritDoc} */
    public TacticalGraphicAttributes getAttributes()
    {
        return this.normalAttributes;
    }

    /** {@inheritDoc} */
    public void setAttributes(TacticalGraphicAttributes attributes)
    {
        this.normalAttributes = attributes;
    }

    /** {@inheritDoc} */
    public TacticalGraphicAttributes getHighlightAttributes()
    {
        return this.highlightAttributes;
    }

    /** {@inheritDoc} */
    public void setHighlightAttributes(TacticalGraphicAttributes attributes)
    {
        this.highlightAttributes = attributes;
    }

    /** {@inheritDoc} */
    public Object getDelegateOwner()
    {
        return this.delegateOwner;
    }

    /** {@inheritDoc} */
    public void setDelegateOwner(Object owner)
    {
        this.delegateOwner = owner;
    }

    /** {@inheritDoc} */
    public UnitsFormat getUnitsFormat()
    {
        return this.unitsFormat;
    }

    /** {@inheritDoc} */
    public void setUnitsFormat(UnitsFormat unitsFormat)
    {
        if (unitsFormat == null)
        {
            String msg = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.unitsFormat = unitsFormat;
    }

    /** {@inheritDoc} */
    public Offset getLabelOffset()
    {
        return this.labelOffset;
    }

    /** {@inheritDoc} */
    public void setLabelOffset(Offset labelOffset)
    {
        this.labelOffset = labelOffset;
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return this.highlighted;
    }

    /** {@inheritDoc} */
    public void setHighlighted(boolean highlighted)
    {
        this.highlighted = highlighted;
    }

    /////////////////////////////
    // Movable interface
    /////////////////////////////

    /** {@inheritDoc} */
    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position refPos = this.getReferencePosition();

        // The reference position is null if this shape has no positions. In this case moving the shape by a
        // relative delta is meaningless. Therefore we fail softly by exiting and doing nothing.
        if (refPos == null)
            return;

        this.moveTo(refPos.add(delta));
    }

    /** {@inheritDoc} */
    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position oldPosition = this.getReferencePosition();

        // The reference position is null if this shape has no positions. In this case moving the shape to a new
        // reference position is meaningless. Therefore we fail softly by exiting and doing nothing.
        if (oldPosition == null)
            return;

        List<Position> newPositions = Position.computeShiftedPositions(oldPosition, position, this.getPositions());

        if (newPositions != null)
            this.setPositions(newPositions);
    }

    @Override
    public boolean isDragEnabled()
    {
        return this.dragEnabled;
    }

    @Override
    public void setDragEnabled(boolean enabled)
    {
        this.dragEnabled = enabled;
    }

    @Override
    public void drag(DragContext dragContext)
    {
        if (!this.dragEnabled)
            return;

        if (this.draggableSupport == null)
            this.draggableSupport = new DraggableSupport(this, WorldWind.CLAMP_TO_GROUND);

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragGlobeSizeConstant(dragContext);
    }

    /////////////
    // Rendering
    /////////////

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        this.determinePerFrameAttributes(dc);

        this.doRenderGraphic(dc);

        if (this.isShowTextModifiers())
            this.doRenderTextModifiers(dc);

        if (this.isShowGraphicModifiers())
            this.doRenderGraphicModifiers(dc);
    }

    /**
     * Determine geometry and attributes for this frame. This method only determines attributes the first time that it
     * is called for each frame. Multiple calls in the same frame will have no effect.
     *
     * @param dc Current draw context.
     */
    protected void determinePerFrameAttributes(DrawContext dc)
    {
        long timeStamp = dc.getFrameTimeStamp();
        if (this.frameTimestamp != timeStamp)
        {
            // Allow the subclass to create labels, if necessary
            if (this.mustCreateLabels)
            {
                if (this.labels != null)
                {
                    this.labels.clear();
                }

                this.createLabels();
                this.mustCreateLabels = false;
            }

            this.determineActiveAttributes();
            this.determineDelegateOwner();

            this.computeGeometry(dc);
            this.frameTimestamp = timeStamp;
        }
    }

    /**
     * Render the text modifiers.
     *
     * @param dc Current draw context.
     */
    protected void doRenderTextModifiers(DrawContext dc)
    {
        if (this.labels == null)
            return;

        for (TacticalGraphicLabel label : this.labels)
        {
            label.render(dc);
        }
    }

    /**
     * Render the graphic modifiers. This base class does not render anything, but subclasses may override this method
     * to draw graphic modifiers.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphicModifiers(DrawContext dc)
    {
        // Do nothing, but allow subclasses to override to add graphic modifiers.
    }

    /**
     * Invoked when a modifier is changed. This implementation marks the label text as invalid causing it to be
     * recreated based on the new modifiers.
     */
    protected void onModifierChanged()
    {
        // Text may need to change to reflect new modifiers.
        this.mustCreateLabels = true;
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    protected void determineLabelPositions(DrawContext dc)
    {
        // Do nothing, but allow subclasses to override
    }

    protected void createLabels()
    {
        // Do nothing, but allow subclasses to override
    }

    protected TacticalGraphicLabel addLabel(String text)
    {
        if (this.labels == null)
            this.labels = new ArrayList<TacticalGraphicLabel>();

        TacticalGraphicLabel label = new TacticalGraphicLabel();
        label.setText(text);
        label.setDelegateOwner(this.getActiveDelegateOwner());
        label.setTextAlign(AVKey.CENTER);
        this.labels.add(label);

        return label;
    }

    protected void computeGeometry(DrawContext dc)
    {
        // Allow the subclass to decide where to put the labels
        this.determineLabelPositions(dc);
    }

    /**
     * Determine the delegate owner for the current frame, and apply the owner to all renderable objects used to draw
     * the graphic.
     */
    protected void determineDelegateOwner()
    {
        Object owner = this.getActiveDelegateOwner();

        // Apply the delegate owner to all label objects.
        if (this.labels != null)
        {
            for (TacticalGraphicLabel label : this.labels)
            {
                label.setDelegateOwner(owner);
            }
        }

        // Give subclasses a chance to apply the delegate owner to shapes they own.
        this.applyDelegateOwner(owner);
    }

    /**
     * Indicates the object attached to the pick list to represent this graphic.
     *
     * @return Delegate owner, if specified, or {@code this} if an owner is not specified.
     */
    protected Object getActiveDelegateOwner()
    {
        Object owner = this.getDelegateOwner();
        return owner != null ? owner : this;
    }

    /** Determine active attributes for this frame. */
    protected void determineActiveAttributes()
    {
        // Apply defaults for this graphic
        this.applyDefaultAttributes(this.activeShapeAttributes);

        if (this.isHighlighted())
        {
            TacticalGraphicAttributes highlightAttributes = this.getHighlightAttributes();

            // If the application specified overrides to the highlight attributes, then apply the overrides
            if (highlightAttributes != null)
            {
                this.activeOverrides.copy(highlightAttributes);

                // Apply overrides specified by application
                this.applyOverrideAttributes(highlightAttributes, this.activeShapeAttributes);
            }
            else
            {
                // If no highlight attributes have been specified we need to use the normal attributes but adjust them
                // to cause highlighting.
                this.activeShapeAttributes.setOutlineMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeShapeAttributes.setInteriorMaterial(DEFAULT_HIGHLIGHT_MATERIAL);
                this.activeShapeAttributes.setInteriorOpacity(1.0);
                this.activeShapeAttributes.setOutlineOpacity(1.0);
            }
        }
        else
        {
            // Apply overrides specified by application
            TacticalGraphicAttributes normalAttributes = this.getAttributes();
            if (normalAttributes != null)
            {
                this.activeOverrides.copy(normalAttributes);
                this.applyOverrideAttributes(normalAttributes, this.activeShapeAttributes);
            }
        }

        this.applyLabelAttributes();
    }

    /** Apply the active attributes to the graphic's labels. */
    protected void applyLabelAttributes()
    {
        if (WWUtil.isEmpty(this.labels))
            return;

        Material labelMaterial = this.getLabelMaterial();

        Font font = this.activeOverrides.getTextModifierFont();
        if (font == null)
            font = TacticalGraphicLabel.DEFAULT_FONT;

        double opacity = this.getActiveShapeAttributes().getInteriorOpacity();

        // Compute an interior opacity for the label. Note that not all tactical graphic labels are drawn with an
        // interior.
        double labelInteriorOpacity = this.computeLabelInteriorOpacity(opacity);

        for (TacticalGraphicLabel label : this.labels)
        {
            label.setMaterial(labelMaterial);
            label.setFont(font);
            label.setOpacity(opacity);
            label.setInteriorOpacity(labelInteriorOpacity);
        }

        // Apply the offset to the main label.
        Offset offset = this.getLabelOffset();
        if (offset == null)
            offset = this.getDefaultLabelOffset();
        this.labels.get(0).setOffset(offset);
    }

    /**
     * Compute the opacity for the label interior. By default, the label interior is opacity is computed as 70% of the
     * text opacity.
     *
     * @param textOpacity Opacity of the label text.
     *
     * @return Opacity of the label interior as a floating point number between 0.0 and 1.0.
     */
    protected double computeLabelInteriorOpacity(double textOpacity)
    {
        return textOpacity * DEFAULT_LABEL_INTERIOR_OPACITY;
    }

    /**
     * Indicates the default offset applied to the graphic's main label. This offset may be overridden by the graphic
     * attributes.
     *
     * @return Offset to apply to the main label.
     */
    protected Offset getDefaultLabelOffset()
    {
        return TacticalGraphicLabel.DEFAULT_OFFSET;
    }

    /**
     * Get the override attributes that are active for this frame.
     *
     * @return Override attributes. Values set in this bundle override defaults specified by the symbol set.
     */
    protected TacticalGraphicAttributes getActiveOverrideAttributes()
    {
        return this.activeOverrides;
    }

    /**
     * Get the active shape attributes for this frame. The active attributes are created by applying application
     * specified overrides to the default attributes specified by the symbol set.
     *
     * @return Active shape attributes.
     */
    protected ShapeAttributes getActiveShapeAttributes()
    {
        return this.activeShapeAttributes;
    }

    /**
     * Get the Material that should be used to draw labels. If no override material has been specified, the graphic's
     * outline Material is used for the labels.
     *
     * @return The Material that should be used when drawing labels. May change each frame.
     */
    protected Material getLabelMaterial()
    {
        Material material = this.activeOverrides.getTextModifierMaterial();
        if (material != null)
            return material;
        else
            return this.activeShapeAttributes.getOutlineMaterial();
    }

    /**
     * Apply defaults to the active attributes bundle. The default attributes are determined by the type of graphic.
     * This method is called each frame to reset the active shape attributes to the appropriate default state. Override
     * attributes specified by the application may be applied after the defaults have been set.
     *
     * @param attributes Attributes bundle to receive defaults.
     */
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        // Do nothing but allow subclasses to override
    }

    /**
     * Apply override attributes specified in a TacticalGraphicAttributes bundle to the active ShapeAttributes. Any
     * non-null properties of {@code graphicAttributes} will be applied to {@code shapeAttributes}.
     *
     * @param graphicAttributes Override attributes.
     * @param shapeAttributes   Shape attributes to receive overrides.
     */
    protected void applyOverrideAttributes(TacticalGraphicAttributes graphicAttributes, ShapeAttributes shapeAttributes)
    {
        Material material = graphicAttributes.getInteriorMaterial();
        if (material != null)
        {
            shapeAttributes.setInteriorMaterial(material);
        }

        material = graphicAttributes.getOutlineMaterial();
        if (material != null)
        {
            shapeAttributes.setOutlineMaterial(material);
        }

        Double value = graphicAttributes.getInteriorOpacity();
        if (value != null)
        {
            shapeAttributes.setInteriorOpacity(value);
        }

        value = graphicAttributes.getOutlineOpacity();
        if (value != null)
        {
            shapeAttributes.setOutlineOpacity(value);
        }

        value = graphicAttributes.getOutlineWidth();
        if (value != null)
        {
            shapeAttributes.setOutlineWidth(value);
        }
    }
}
