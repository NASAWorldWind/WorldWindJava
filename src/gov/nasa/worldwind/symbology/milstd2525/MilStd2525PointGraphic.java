/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.drag.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Implementation of MIL-STD-2525 point graphics. Point graphics are rendered in the same way as tactical symbols: by
 * drawing an icon at constant screen size.
 *
 * @author pabercrombie
 * @version $Id: MilStd2525PointGraphic.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public class MilStd2525PointGraphic extends AVListImpl implements MilStd2525TacticalGraphic, TacticalPoint, Draggable
{
    // Implementation note: This class wraps an instance of TacticalGraphicSymbol. TacticalGraphicSymbol implements the
    // logic for rendering point graphics using the TacticalSymbol base classes. This class adapts the TacticalGraphic
    // interface to the TacticalSymbol interface.

    /** Symbol used to render this graphic. */
    protected TacticalGraphicSymbol symbol;

    /** Indicates whether or not the graphic is highlighted. */
    protected boolean highlighted;

    /** Indicates whether the object is draggable and provides additional information for dragging about this object. */
    protected boolean dragEnabled = true;
    protected DraggableSupport draggableSupport = null;

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

    /** Current frame timestamp. */
    protected long frameTimestamp = -1L;
    /** Attributes to use for the current frame. */
    protected TacticalSymbolAttributes activeSymbolAttributes = new BasicTacticalSymbolAttributes();

    protected static TacticalSymbolAttributes defaultSymbolAttributes = new BasicTacticalSymbolAttributes();

    /**
     * Create a new point graphic.
     *
     * @param sidc MIL-STD-2525 SIDC code that identifies the graphic.
     */
    public MilStd2525PointGraphic(String sidc)
    {
        this.symbol = this.createSymbol(sidc);
    }

    /**
     * Create a tactical symbol to render this graphic.
     *
     * @param sidc Symbol code that identifies the graphic.
     *
     * @return A new tactical symbol.
     */
    protected TacticalGraphicSymbol createSymbol(String sidc)
    {
        TacticalGraphicSymbol symbol = new TacticalGraphicSymbol(sidc);
        symbol.setAttributes(this.activeSymbolAttributes);
        symbol.setDelegateOwner(this);
        return symbol;
    }

    /** {@inheritDoc} */
    public boolean isVisible()
    {
        return this.symbol.isVisible();
    }

    /** {@inheritDoc} */
    public void setVisible(boolean visible)
    {
        this.symbol.setVisible(visible);
    }

    /** {@inheritDoc} */
    public Object getModifier(String modifier)
    {
        return this.symbol.getModifier(modifier);
    }

    /** {@inheritDoc} */
    public void setModifier(String modifier, Object value)
    {
        this.symbol.setModifier(modifier, value);
    }

    /** {@inheritDoc} */
    public boolean isShowTextModifiers()
    {
        return this.symbol.isShowTextModifiers();
    }

    /** {@inheritDoc} */
    public void setShowTextModifiers(boolean showModifiers)
    {
        this.symbol.setShowTextModifiers(showModifiers);
    }

    /** {@inheritDoc} */
    public boolean isShowGraphicModifiers()
    {
        return this.symbol.isShowGraphicModifiers();
    }

    /** {@inheritDoc} */
    public void setShowGraphicModifiers(boolean showModifiers)
    {
        this.symbol.setShowGraphicModifiers(showModifiers);
    }

    /** {@inheritDoc} */
    public boolean isShowLocation()
    {
        return this.symbol.isShowLocation();
    }

    /** {@inheritDoc} */
    public void setShowLocation(boolean show)
    {
        this.symbol.setShowLocation(show);
    }

    /** {@inheritDoc} */
    public boolean isShowHostileIndicator()
    {
        return this.symbol.isShowHostileIndicator();
    }

    /** {@inheritDoc} */
    public void setShowHostileIndicator(boolean show)
    {
        this.symbol.setShowHostileIndicator(show);
    }

    /** {@inheritDoc} */
    public String getIdentifier()
    {
        return this.symbol.getIdentifier();
    }

    /** {@inheritDoc} */
    public void setText(String text)
    {
        this.symbol.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, text);
    }

    /** {@inheritDoc} */
    public String getText()
    {
        // Get the Unique Designation modifier. If it's an iterable, return the first value.
        Object value = this.getModifier(SymbologyConstants.UNIQUE_DESIGNATION);
        if (value instanceof String)
        {
            return (String) value;
        }
        else if (value instanceof Iterable)
        {
            Iterator iterator = ((Iterable) value).iterator();
            Object o = iterator.hasNext() ? iterator.next() : null;
            if (o != null)
                return o.toString();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always returns an Iterable with only one position.
     */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.getPosition());
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only one control point.
     */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterator<? extends Position> iterator = positions.iterator();
        if (!iterator.hasNext())
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setPosition(iterator.next());
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
    public Offset getLabelOffset()
    {
        return null; // Does not apply to point graphic
    }

    /** {@inheritDoc} */
    public void setLabelOffset(Offset offset)
    {
        // Does not apply to point graphic
    }

    /**
     * Indicates a location within the symbol to align with the symbol point. See {@link
     * #setOffset(gov.nasa.worldwind.render.Offset) setOffset} for more information.
     *
     * @return the hot spot controlling the symbol's placement relative to the symbol point. null indicates default
     *         alignment.
     */
    public Offset getOffset()
    {
        return this.symbol.getOffset();
    }

    /**
     * Specifies a location within the tactical symbol to align with the symbol point. By default, ground symbols are
     * aligned at the bottom center of the symbol, and other symbols are aligned to the center of the symbol. {@code
     * setOffset(Offset.CENTER)} aligns the center of the symbol with the symbol point, and {@code
     * setOffset(Offset.BOTTOM_CENTER)} aligns the center of the bottom edge with the symbol point.
     *
     * @param offset the hot spot controlling the symbol's placement relative to the symbol point. May be null to
     *               indicate default alignment.
     */
    public void setOffset(Offset offset)
    {
        this.symbol.setOffset(offset);
    }

    /** {@inheritDoc} */
    public Object getDelegateOwner()
    {
        // If the application has supplied a delegate owner, return that object. If the owner is this object (the
        // default), return null to keep the contract of getDelegateOwner, which specifies that a value of null
        // indicates that the graphic itself is used during picking.
        Object owner = this.symbol.getDelegateOwner();
        return owner != this ? owner : null;
    }

    /** {@inheritDoc} */
    public void setDelegateOwner(Object owner)
    {
        // Apply new delegate owner if non-null. If the new owner is null, set this object as symbol's delegate owner
        // (the default).
        if (owner != null)
            this.symbol.setDelegateOwner(owner);
        else
            this.symbol.setDelegateOwner(this);
    }

    /** {@inheritDoc} */
    public UnitsFormat getUnitsFormat()
    {
        return this.symbol.getUnitsFormat();
    }

    /** {@inheritDoc} */
    public void setUnitsFormat(UnitsFormat unitsFormat)
    {
        this.symbol.setUnitsFormat(unitsFormat);
    }

    /** {@inheritDoc} */
    public Position getPosition()
    {
        return this.symbol.getPosition();
    }

    /** {@inheritDoc} */
    public void setPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.symbol.setPosition(position);
    }

    /**
     * Indicates this symbol's altitude mode. See {@link #setAltitudeMode(int)} for a description of the valid altitude
     * modes.
     *
     * @return this symbol's altitude mode.
     */
    public int getAltitudeMode()
    {
        return this.symbol.getAltitudeMode();
    }

    /**
     * Specifies this graphic's altitude mode. Altitude mode defines how the altitude component of this symbol's
     * position is interpreted. Recognized modes are: <ul> <li>WorldWind.CLAMP_TO_GROUND -- this graphic is placed on
     * the terrain at the latitude and longitude of its position.</li> <li>WorldWind.RELATIVE_TO_GROUND -- this graphic
     * is placed above the terrain at the latitude and longitude of its position and the distance specified by its
     * elevation.</li> <li>WorldWind.ABSOLUTE -- this graphic is placed at its specified position.</li> </ul>
     * <p>
     * This graphic assumes the altitude mode WorldWind.ABSOLUTE if the specified mode is not recognized.
     *
     * @param altitudeMode this symbol's new altitude mode.
     */
    public void setAltitudeMode(int altitudeMode)
    {
        this.symbol.setAltitudeMode(altitudeMode);
    }

    ////////////////////////////////////////
    // MilStd2525TacticalGraphic interface
    ////////////////////////////////////////

    /** {@inheritDoc} */
    public String getStatus()
    {
        return this.symbol.getStatus();
    }

    /** {@inheritDoc} */
    public void setStatus(String value)
    {
        this.symbol.setStatus(value);
    }

    /////////////////////////////
    // Movable interface
    /////////////////////////////

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.getPosition();
    }

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
        this.symbol.setPosition(position);
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
            this.draggableSupport = new DraggableSupport(this, this.getAltitudeMode());

        this.doDrag(dragContext);
    }

    protected void doDrag(DragContext dragContext)
    {
        this.draggableSupport.dragScreenSizeConstant(dragContext);
    }

    /////////////////////////////
    // Highlightable interface
    /////////////////////////////

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
    // Rendering
    /////////////////////////////

    /** {@inheritDoc} */
    public void render(DrawContext dc)
    {
        long timestamp = dc.getFrameTimeStamp();
        if (this.frameTimestamp != timestamp)
        {
            this.determineActiveAttributes();
            this.frameTimestamp = timestamp;
        }

        this.symbol.render(dc);
    }

    /** Determine active attributes for this frame. */
    protected void determineActiveAttributes()
    {
        // Reset symbol attributes to default before applying overrides.
        this.activeSymbolAttributes.copy(defaultSymbolAttributes);

        if (this.isHighlighted())
        {
            TacticalGraphicAttributes highlightAttributes = this.getHighlightAttributes();

            // If the application specified overrides to the highlight attributes, then apply the overrides
            if (highlightAttributes != null)
            {
                // Apply overrides specified by application
                this.applyAttributesToSymbol(highlightAttributes, this.activeSymbolAttributes);
            }
        }
        else
        {
            // Apply overrides specified by application
            TacticalGraphicAttributes normalAttributes = this.getAttributes();
            if (normalAttributes != null)
            {
                this.applyAttributesToSymbol(normalAttributes, this.activeSymbolAttributes);
            }
        }
    }

    /**
     * Apply graphic attributes to the symbol.
     *
     * @param graphicAttributes Tactical graphic attributes to apply to the tactical symbol.
     * @param symbolAttributes  Symbol attributes to be modified.
     */
    protected void applyAttributesToSymbol(TacticalGraphicAttributes graphicAttributes,
        TacticalSymbolAttributes symbolAttributes)
    {
        // Line and area graphics distinguish between interior and outline opacity. Tactical symbols only support one
        // opacity, so use the interior opacity.
        Double value = graphicAttributes.getInteriorOpacity();
        if (value != null)
        {
            symbolAttributes.setOpacity(value);
        }

        value = graphicAttributes.getScale();
        if (value != null)
        {
            symbolAttributes.setScale(value);
        }

        Material material = graphicAttributes.getInteriorMaterial();
        symbolAttributes.setInteriorMaterial(material);

        Font font = graphicAttributes.getTextModifierFont();
        if (font != null)
        {
            symbolAttributes.setTextModifierFont(font);
        }

        material = graphicAttributes.getTextModifierMaterial();
        if (material != null)
        {
            symbolAttributes.setTextModifierMaterial(material);
        }
    }

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        List<String> graphics = new ArrayList<String>();
        graphics.addAll(getTacGrpGraphics());
        graphics.addAll(getMetocGraphics());
        graphics.addAll(getEmsGraphics());
        return graphics;
    }

    /**
     * Indicates the graphics in MIL-STD-2525C Appendix B supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getTacGrpGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.TSK_DSTY,
            TacGrpSidc.TSK_ITDT,
            TacGrpSidc.TSK_NEUT,
            TacGrpSidc.C2GM_GNL_PNT_USW_UH2_DTM,
            TacGrpSidc.C2GM_GNL_PNT_USW_UH2_BCON,
            TacGrpSidc.C2GM_GNL_PNT_USW_UH2_LCON,
            TacGrpSidc.C2GM_GNL_PNT_USW_UH2_SNK,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_PTNCTR,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_DIFAR,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_LOFAR,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_CASS,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_DICASS,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_BT,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_ANM,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_VLAD,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_ATAC,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_RO,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_KGP,
            TacGrpSidc.C2GM_GNL_PNT_USW_SNBY_EXP,
            TacGrpSidc.C2GM_GNL_PNT_USW_SRH,
            TacGrpSidc.C2GM_GNL_PNT_USW_SRH_ARA,
            TacGrpSidc.C2GM_GNL_PNT_USW_SRH_DIPPSN,
            TacGrpSidc.C2GM_GNL_PNT_USW_SRH_CTR,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_NAVREF,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_SPLPNT,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_DLRP,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_PIM,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_MRSH,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_WAP,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_CRDRTB,
            TacGrpSidc.C2GM_GNL_PNT_REFPNT_PNTINR,
            TacGrpSidc.C2GM_GNL_PNT_WPN_AIMPNT,
            TacGrpSidc.C2GM_GNL_PNT_WPN_DRPPNT,
            TacGrpSidc.C2GM_GNL_PNT_WPN_ENTPNT,
            TacGrpSidc.C2GM_GNL_PNT_WPN_GRDZRO,
            TacGrpSidc.C2GM_GNL_PNT_WPN_MSLPNT,
            TacGrpSidc.C2GM_GNL_PNT_WPN_IMTPNT,
            TacGrpSidc.C2GM_GNL_PNT_WPN_PIPNT,
            TacGrpSidc.C2GM_GNL_PNT_FRMN,
            TacGrpSidc.C2GM_GNL_PNT_HBR,
            TacGrpSidc.C2GM_GNL_PNT_HBR_PNTQ,
            TacGrpSidc.C2GM_GNL_PNT_HBR_PNTA,
            TacGrpSidc.C2GM_GNL_PNT_HBR_PNTY,
            TacGrpSidc.C2GM_GNL_PNT_HBR_PNTX,
            TacGrpSidc.C2GM_GNL_PNT_RTE,
            TacGrpSidc.C2GM_GNL_PNT_RTE_RDV,
            TacGrpSidc.C2GM_GNL_PNT_RTE_DVSN,
            TacGrpSidc.C2GM_GNL_PNT_RTE_WAP,
            TacGrpSidc.C2GM_GNL_PNT_RTE_PIM,
            TacGrpSidc.C2GM_GNL_PNT_RTE_PNTR,
            TacGrpSidc.C2GM_GNL_PNT_ACTL,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_CAP,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ABNEW,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_TAK,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ASBWF,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ASBWR,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_SUWF,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_SUWR,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_MIWF,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_MIWR,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_SKEIP,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_TCN,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_TMC,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_RSC,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_RPH,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_UA,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_VTUA,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ORB,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ORBF8,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ORBRT,
            TacGrpSidc.C2GM_GNL_PNT_ACTL_ORBRD,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_CHKPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_CONPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_CRDPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_DCNPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_LNKUPT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_PSSPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_RAYPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_RELPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_STRPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_AMNPNT,
            TacGrpSidc.C2GM_GNL_PNT_ACTPNT_WAP,
            TacGrpSidc.C2GM_GNL_PNT_SCTL,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_USV,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_USV_RMV,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_USV_ASW,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_USV_SUW,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_USV_MIW,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_ASW,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_SUW,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_MIW,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_PKT,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_RDV,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_RSC,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_REP,
            TacGrpSidc.C2GM_GNL_PNT_SCTL_NCBTT,
            TacGrpSidc.C2GM_GNL_PNT_UCTL,
            TacGrpSidc.C2GM_GNL_PNT_UCTL_UUV,
            TacGrpSidc.C2GM_GNL_PNT_UCTL_UUV_ASW,
            TacGrpSidc.C2GM_GNL_PNT_UCTL_UUV_SUW,
            TacGrpSidc.C2GM_GNL_PNT_UCTL_UUV_MIW,
            TacGrpSidc.C2GM_GNL_PNT_UCTL_SBSTN,
            TacGrpSidc.C2GM_GNL_PNT_UCTL_SBSTN_ASW,
            TacGrpSidc.C2GM_DEF_PNT_TGTREF,
            TacGrpSidc.C2GM_DEF_PNT_OBSPST,
            TacGrpSidc.C2GM_DEF_PNT_OBSPST_CBTPST,
            TacGrpSidc.C2GM_DEF_PNT_OBSPST_RECON,
            TacGrpSidc.C2GM_DEF_PNT_OBSPST_FWDOP,
            TacGrpSidc.C2GM_DEF_PNT_OBSPST_SOP,
            TacGrpSidc.C2GM_DEF_PNT_OBSPST_CBRNOP,
            TacGrpSidc.C2GM_OFF_PNT_PNTD,
            TacGrpSidc.C2GM_AVN_PNT_DAPP,
            TacGrpSidc.MOBSU_OBST_ATO_TDTSM_FIXPFD,
            TacGrpSidc.MOBSU_OBST_ATO_TDTSM_MVB,
            TacGrpSidc.MOBSU_OBST_ATO_TDTSM_MVBPFD,
            TacGrpSidc.MOBSU_OBST_BBY,
            TacGrpSidc.MOBSU_OBST_MNE_USPMNE,
            TacGrpSidc.MOBSU_OBST_MNE_ATMNE,
            TacGrpSidc.MOBSU_OBST_MNE_ATMAHD,
            TacGrpSidc.MOBSU_OBST_MNE_ATMDIR,
            TacGrpSidc.MOBSU_OBST_MNE_APMNE,
            TacGrpSidc.MOBSU_OBST_MNE_WAMNE,
            TacGrpSidc.MOBSU_OBST_AVN_TWR_LOW,
            TacGrpSidc.MOBSU_OBST_AVN_TWR_HIGH,
            TacGrpSidc.MOBSU_OBSTBP_CSGSTE_ERP,
            TacGrpSidc.MOBSU_SU_ESTOF,
            TacGrpSidc.MOBSU_SU_FRT,
            TacGrpSidc.MOBSU_SU_SUFSHL,
            TacGrpSidc.MOBSU_SU_UGDSHL,
            TacGrpSidc.MOBSU_CBRN_NDGZ,
            TacGrpSidc.MOBSU_CBRN_FAOTP,
            TacGrpSidc.MOBSU_CBRN_REEVNT_BIO,
            TacGrpSidc.MOBSU_CBRN_REEVNT_CML,
            TacGrpSidc.MOBSU_CBRN_DECONP_USP,
            TacGrpSidc.MOBSU_CBRN_DECONP_ALTUSP,
            TacGrpSidc.MOBSU_CBRN_DECONP_TRP,
            TacGrpSidc.MOBSU_CBRN_DECONP_EQT,
            TacGrpSidc.MOBSU_CBRN_DECONP_EQTTRP,
            TacGrpSidc.MOBSU_CBRN_DECONP_OPDECN,
            TacGrpSidc.MOBSU_CBRN_DECONP_TRGH,
            TacGrpSidc.FSUPP_PNT_TGT_PTGT,
            TacGrpSidc.FSUPP_PNT_TGT_NUCTGT,
            TacGrpSidc.FSUPP_PNT_C2PNT_FSS,
            TacGrpSidc.FSUPP_PNT_C2PNT_SCP,
            TacGrpSidc.FSUPP_PNT_C2PNT_FP,
            TacGrpSidc.FSUPP_PNT_C2PNT_RP,
            TacGrpSidc.FSUPP_PNT_C2PNT_HP,
            TacGrpSidc.FSUPP_PNT_C2PNT_LP,
            TacGrpSidc.CSS_PNT_AEP,
            TacGrpSidc.CSS_PNT_CBNP,
            TacGrpSidc.CSS_PNT_CCP,
            TacGrpSidc.CSS_PNT_CVP,
            TacGrpSidc.CSS_PNT_DCP,
            TacGrpSidc.CSS_PNT_EPWCP,
            TacGrpSidc.CSS_PNT_LRP,
            TacGrpSidc.CSS_PNT_MCP,
            TacGrpSidc.CSS_PNT_RRRP,
            TacGrpSidc.CSS_PNT_ROM,
            TacGrpSidc.CSS_PNT_TCP,
            TacGrpSidc.CSS_PNT_TTP,
            TacGrpSidc.CSS_PNT_UMC,
            TacGrpSidc.CSS_PNT_SPT_GNL,
            TacGrpSidc.CSS_PNT_SPT_CLS1,
            TacGrpSidc.CSS_PNT_SPT_CLS2,
            TacGrpSidc.CSS_PNT_SPT_CLS3,
            TacGrpSidc.CSS_PNT_SPT_CLS4,
            TacGrpSidc.CSS_PNT_SPT_CLS5,
            TacGrpSidc.CSS_PNT_SPT_CLS6,
            TacGrpSidc.CSS_PNT_SPT_CLS7,
            TacGrpSidc.CSS_PNT_SPT_CLS8,
            TacGrpSidc.CSS_PNT_SPT_CLS9,
            TacGrpSidc.CSS_PNT_SPT_CLS10,
            TacGrpSidc.CSS_PNT_AP_ASP,
            TacGrpSidc.CSS_PNT_AP_ATP,
            TacGrpSidc.OTH_ER_DTHAC,
            TacGrpSidc.OTH_ER_PIW,
            TacGrpSidc.OTH_ER_DSTVES,
            TacGrpSidc.OTH_HAZ_SML,
            TacGrpSidc.OTH_HAZ_IB,
            TacGrpSidc.OTH_HAZ_OLRG,
            TacGrpSidc.OTH_SSUBSR_BTMRTN,
            TacGrpSidc.OTH_SSUBSR_BTMRTN_INS,
            TacGrpSidc.OTH_SSUBSR_BTMRTN_SBRSOO,
            TacGrpSidc.OTH_SSUBSR_BTMRTN_WRKND,
            TacGrpSidc.OTH_SSUBSR_BTMRTN_WRKD,
            TacGrpSidc.OTH_SSUBSR_MARLFE,
            TacGrpSidc.OTH_SSUBSR_SA,
            TacGrpSidc.OTH_FIX_ACU,
            TacGrpSidc.OTH_FIX_EM,
            TacGrpSidc.OTH_FIX_EOP);
    }

    public static List<String> getMetocGraphics()
    {
        return Arrays.asList(
            MetocSidc.AMPHC_PRS_LOWCTR,
            MetocSidc.AMPHC_PRS_LOWCTR_CYC,
            MetocSidc.AMPHC_PRS_LOWCTR_TROPLW,
            MetocSidc.AMPHC_PRS_HGHCTR,
            MetocSidc.AMPHC_PRS_HGHCTR_ACYC,
            MetocSidc.AMPHC_PRS_HGHCTR_TROPHG,
            MetocSidc.AMPHC_TRB_LIT,
            MetocSidc.AMPHC_TRB_MOD,
            MetocSidc.AMPHC_TRB_SVR,
            MetocSidc.AMPHC_TRB_EXT,
            MetocSidc.AMPHC_TRB_MNTWAV,
            MetocSidc.AMPHC_ICG_CLR_LIT,
            MetocSidc.AMPHC_ICG_CLR_MOD,
            MetocSidc.AMPHC_ICG_CLR_SVR,
            MetocSidc.AMPHC_ICG_RIME_LIT,
            MetocSidc.AMPHC_ICG_RIME_MOD,
            MetocSidc.AMPHC_ICG_RIME_SVR,
            MetocSidc.AMPHC_ICG_MIX_LIT,
            MetocSidc.AMPHC_ICG_MIX_MOD,
            MetocSidc.AMPHC_ICG_MIX_SVR,
            MetocSidc.AMPHC_WND_CALM,
//            MetocSidc.AMPHC_WND_PLT,
            MetocSidc.AMPHC_CUDCOV_SYM_SKC,
            MetocSidc.AMPHC_CUDCOV_SYM_FEW,
            MetocSidc.AMPHC_CUDCOV_SYM_SCT,
            MetocSidc.AMPHC_CUDCOV_SYM_BKN,
            MetocSidc.AMPHC_CUDCOV_SYM_OVC,
            MetocSidc.AMPHC_CUDCOV_SYM_STOPO,
            MetocSidc.AMPHC_WTH_RA_INMLIT,
            MetocSidc.AMPHC_WTH_RA_INMLIT_CTSLIT,
            MetocSidc.AMPHC_WTH_RA_INMMOD,
            MetocSidc.AMPHC_WTH_RA_INMMOD_CTSMOD,
            MetocSidc.AMPHC_WTH_RA_INMHVY,
            MetocSidc.AMPHC_WTH_RA_INMHVY_CTSHVY,
            MetocSidc.AMPHC_WTH_FZRA_LIT,
            MetocSidc.AMPHC_WTH_FZRA_MODHVY,
            MetocSidc.AMPHC_WTH_RASWR_LIT,
            MetocSidc.AMPHC_WTH_RASWR_MODHVY,
            MetocSidc.AMPHC_WTH_RASWR_TOR,
            MetocSidc.AMPHC_WTH_DZ_INMLIT,
            MetocSidc.AMPHC_WTH_DZ_INMLIT_CTSLIT,
            MetocSidc.AMPHC_WTH_DZ_INMMOD,
            MetocSidc.AMPHC_WTH_DZ_INMMOD_CTSMOD,
            MetocSidc.AMPHC_WTH_DZ_INMHVY,
            MetocSidc.AMPHC_WTH_DZ_INMHVY_CTSHVY,
            MetocSidc.AMPHC_WTH_FZDZ_LIT,
            MetocSidc.AMPHC_WTH_FZDZ_MODHVY,
            MetocSidc.AMPHC_WTH_RASN_RDSLIT,
            MetocSidc.AMPHC_WTH_RASN_RDSMH,
            MetocSidc.AMPHC_WTH_RASN_SWRLIT,
            MetocSidc.AMPHC_WTH_RASN_SWRMOD,
            MetocSidc.AMPHC_WTH_SN_INMLIT,
            MetocSidc.AMPHC_WTH_SN_INMLIT_CTSLIT,
            MetocSidc.AMPHC_WTH_SN_INMMOD,
            MetocSidc.AMPHC_WTH_SN_INMMOD_CTSMOD,
            MetocSidc.AMPHC_WTH_SN_INMHVY,
            MetocSidc.AMPHC_WTH_SN_INMHVY_CTSHVY,
            MetocSidc.AMPHC_WTH_SN_BLSNLM,
            MetocSidc.AMPHC_WTH_SN_BLSNHY,
            MetocSidc.AMPHC_WTH_SG,
            MetocSidc.AMPHC_WTH_SSWR_LIT,
            MetocSidc.AMPHC_WTH_SSWR_MODHVY,
            MetocSidc.AMPHC_WTH_HL_LIT,
            MetocSidc.AMPHC_WTH_HL_MODHVY,
            MetocSidc.AMPHC_WTH_IC,
            MetocSidc.AMPHC_WTH_PE_LIT,
            MetocSidc.AMPHC_WTH_PE_MOD,
            MetocSidc.AMPHC_WTH_PE_HVY,
            MetocSidc.AMPHC_WTH_STMS_TS,
            MetocSidc.AMPHC_WTH_STMS_TSLMNH,
            MetocSidc.AMPHC_WTH_STMS_TSHVNH,
            MetocSidc.AMPHC_WTH_STMS_TSLMWH,
            MetocSidc.AMPHC_WTH_STMS_TSHVWH,
            MetocSidc.AMPHC_WTH_STMS_FC,
            MetocSidc.AMPHC_WTH_STMS_SQL,
            MetocSidc.AMPHC_WTH_STMS_LTG,
            MetocSidc.AMPHC_WTH_FG_SHWPTH,
            MetocSidc.AMPHC_WTH_FG_SHWCTS,
            MetocSidc.AMPHC_WTH_FG_PTHY,
            MetocSidc.AMPHC_WTH_FG_SKYVSB,
            MetocSidc.AMPHC_WTH_FG_SKYOBD,
            MetocSidc.AMPHC_WTH_FG_FZSV,
            MetocSidc.AMPHC_WTH_FG_FZSNV,
            MetocSidc.AMPHC_WTH_MIST,
            MetocSidc.AMPHC_WTH_FU,
            MetocSidc.AMPHC_WTH_HZ,
            MetocSidc.AMPHC_WTH_DTSD_LITMOD,
            MetocSidc.AMPHC_WTH_DTSD_SVR,
            MetocSidc.AMPHC_WTH_DTSD_DTDVL,
//            MetocSidc.AMPHC_WTH_DTSD_BLDTSD,
            MetocSidc.AMPHC_WTH_TPLSYS_TROPDN,
            MetocSidc.AMPHC_WTH_TPLSYS_TROPSM,
            MetocSidc.AMPHC_WTH_TPLSYS_HC,
//            MetocSidc.AMPHC_WTH_TPLSYS_TSWADL,
            MetocSidc.AMPHC_WTH_VOLERN,
            MetocSidc.AMPHC_WTH_VOLERN_VOLASH,
            MetocSidc.AMPHC_WTH_TROPLV,
            MetocSidc.AMPHC_WTH_FZLVL,
            MetocSidc.AMPHC_WTH_POUTAI,
//            MetocSidc.AMPHC_STOG_WOSMIC_SUFDRY,
//            MetocSidc.AMPHC_STOG_WOSMIC_SUFMST,
//            MetocSidc.AMPHC_STOG_WOSMIC_SUFWET,
//            MetocSidc.AMPHC_STOG_WOSMIC_SUFFLD,
//            MetocSidc.AMPHC_STOG_WOSMIC_SUFFZN,
            MetocSidc.AMPHC_STOG_WOSMIC_GLZGRD,
//            MetocSidc.AMPHC_STOG_WOSMIC_LDNCGC,
//            MetocSidc.AMPHC_STOG_WOSMIC_TLDCGC,
//            MetocSidc.AMPHC_STOG_WOSMIC_MLDCGC,
            MetocSidc.AMPHC_STOG_WOSMIC_EXTDWC,
            MetocSidc.AMPHC_STOG_WSMIC_PDMIC,
//            MetocSidc.AMPHC_STOG_WSMIC_CWSNLH,
//            MetocSidc.AMPHC_STOG_WSMIC_CSNALH,
//            MetocSidc.AMPHC_STOG_WSMIC_ELCSCG,
//            MetocSidc.AMPHC_STOG_WSMIC_ULCSCG,
//            MetocSidc.AMPHC_STOG_WSMIC_LDSNLH,
//            MetocSidc.AMPHC_STOG_WSMIC_LDSALH,
//            MetocSidc.AMPHC_STOG_WSMIC_ELDSCG,
//            MetocSidc.AMPHC_STOG_WSMIC_ULDSCG,
            MetocSidc.AMPHC_STOG_WSMIC_SCGC,
            MetocSidc.OCA_ISYS_IB,
            MetocSidc.OCA_ISYS_IB_MNY,
            MetocSidc.OCA_ISYS_IB_BAS,
            MetocSidc.OCA_ISYS_IB_GNL,
            MetocSidc.OCA_ISYS_IB_MNYGNL,
            MetocSidc.OCA_ISYS_IB_BB,
            MetocSidc.OCA_ISYS_IB_MNYBB,
            MetocSidc.OCA_ISYS_IB_GWL,
            MetocSidc.OCA_ISYS_IB_MNYGWL,
            MetocSidc.OCA_ISYS_IB_FBG,
            MetocSidc.OCA_ISYS_IB_II,
            MetocSidc.OCA_ISYS_ICN_BW,
            MetocSidc.OCA_ISYS_ICN_WWRT,
            MetocSidc.OCA_ISYS_ICN_IF,
            MetocSidc.OCA_ISYS_DYNPRO_CNG,
            MetocSidc.OCA_ISYS_DYNPRO_DVG,
            MetocSidc.OCA_ISYS_DYNPRO_SHAZ,
            MetocSidc.OCA_ISYS_SI,
//            MetocSidc.OCA_ISYS_SI_ITOBS,
//            MetocSidc.OCA_ISYS_SI_ITEST,
            MetocSidc.OCA_ISYS_SI_MPOFI,
            MetocSidc.OCA_ISYS_SC,
            MetocSidc.OCA_ISYS_SC_SWO,
            MetocSidc.OCA_ISYS_TOPFTR_HUM,
            MetocSidc.OCA_ISYS_TOPFTR_RFTG,
            MetocSidc.OCA_ISYS_TOPFTR_JBB,
            MetocSidc.OCA_HYDGRY_DPH_SNDG,
            MetocSidc.OCA_HYDGRY_PRTHBR_PRT_BRHSO,
            MetocSidc.OCA_HYDGRY_PRTHBR_PRT_BRHSA,
            MetocSidc.OCA_HYDGRY_PRTHBR_PRT_ANCRG1,
            MetocSidc.OCA_HYDGRY_PRTHBR_PRT_CIP,
            MetocSidc.OCA_HYDGRY_PRTHBR_FSG_FSGHBR,
            MetocSidc.OCA_HYDGRY_PRTHBR_FSG_FSTK1,
            MetocSidc.OCA_HYDGRY_PRTHBR_FAC_LNDPLC,
            MetocSidc.OCA_HYDGRY_PRTHBR_FAC_OSLF1,
            MetocSidc.OCA_HYDGRY_PRTHBR_FAC_LNDRNG,
            MetocSidc.OCA_HYDGRY_PRTHBR_FAC_DOPN,
            MetocSidc.OCA_HYDGRY_ATN_BCN,
            MetocSidc.OCA_HYDGRY_ATN_BUOY,
            MetocSidc.OCA_HYDGRY_ATN_MRK,
            MetocSidc.OCA_HYDGRY_ATN_PRH1_PRH2,
            MetocSidc.OCA_HYDGRY_ATN_LIT,
            MetocSidc.OCA_HYDGRY_ATN_LITVES,
            MetocSidc.OCA_HYDGRY_ATN_LITHSE,
            MetocSidc.OCA_HYDGRY_DANHAZ_RCKSBM,
            MetocSidc.OCA_HYDGRY_DANHAZ_RCKAWD,
            MetocSidc.OCA_HYDGRY_DANHAZ_FLGRD1_FLGRD2,
            MetocSidc.OCA_HYDGRY_DANHAZ_KLP1_KLP2,
            MetocSidc.OCA_HYDGRY_DANHAZ_MNENAV_DBT,
            MetocSidc.OCA_HYDGRY_DANHAZ_MNENAV_DEFN,
            MetocSidc.OCA_HYDGRY_DANHAZ_SNAG,
            MetocSidc.OCA_HYDGRY_DANHAZ_WRK_UCOV,
            MetocSidc.OCA_HYDGRY_DANHAZ_WRK_SBM,
            MetocSidc.OCA_HYDGRY_DANHAZ_EOTR,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_SD,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_MUD,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_CLAY,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_SLT,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_STNE,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_GVL,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_PBL,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_COBL,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_RCK,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_CRL,
            MetocSidc.OCA_HYDGRY_BTMFAT_BTMCHR_SHE,
            MetocSidc.OCA_HYDGRY_BTMFAT_QLFYTM_FNE,
            MetocSidc.OCA_HYDGRY_BTMFAT_QLFYTM_MDM,
            MetocSidc.OCA_HYDGRY_BTMFAT_QLFYTM_CSE,
            MetocSidc.OCA_HYDGRY_TDECUR_H2OTRB,
            MetocSidc.OCA_HYDGRY_TDECUR_TDEDP,
            MetocSidc.OCA_HYDGRY_TDECUR_TDEG,
            MetocSidc.OCA_MMD_FRD,
            MetocSidc.OCA_MMD_LCK,
            MetocSidc.OCA_MMD_OLRG,
            MetocSidc.OCA_MMD_PLE
        );
    }

    public static List<String> getEmsGraphics()
    {
        return Arrays.asList(
            EmsSidc.NATEVT_GEO_AFTSHK,
            EmsSidc.NATEVT_GEO_AVL,
            EmsSidc.NATEVT_GEO_EQKEPI,
            EmsSidc.NATEVT_GEO_LNDSLD,
            EmsSidc.NATEVT_GEO_SBSDNC,
            EmsSidc.NATEVT_GEO_VLCTHT,
            EmsSidc.NATEVT_HYDMET_DRGHT,
            EmsSidc.NATEVT_HYDMET_FLD,
            EmsSidc.NATEVT_HYDMET_INV,
            EmsSidc.NATEVT_HYDMET_TSNMI,
            EmsSidc.NATEVT_INFST_BIRD,
            EmsSidc.NATEVT_INFST_INSCT,
            EmsSidc.NATEVT_INFST_MICROB,
            EmsSidc.NATEVT_INFST_REPT,
            EmsSidc.NATEVT_INFST_RDNT
        );
    }
}
