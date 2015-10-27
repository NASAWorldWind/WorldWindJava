/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.SymbolCode;
import gov.nasa.worldwind.symbology.milstd2525.graphics.*;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of Battle Position graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Battle Position (2.X.2.4.3.1)</li> <li>Battle Position, Prepared But Not Occupied (2.X.2.4.3.1.1)</li>
 * </ul>
 * <p/>
 * The Echelon label (field B) will be placed between the first and second control points.
 *
 * @author pabercrombie
 * @version $Id: BattlePosition.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public class BattlePosition extends BasicArea
{
    /** Factor applied to the stipple pattern used to draw the dashed line for a Prepared but not Occupied area. */
    protected static final int PBNO_OUTLINE_STIPPLE_FACTOR = 12;

    /** Tactical symbol used to render the echelon modifier. */
    protected TacticalSymbol echelonSymbol;
    /** Attribute bundle for the echelon symbol. */
    protected TacticalSymbolAttributes symbolAttributes;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_DEF_ARS_BTLPSN,
            TacGrpSidc.C2GM_DEF_ARS_BTLPSN_PBNO);
    }

    /**
     * Create a new area graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public BattlePosition(String sidc)
    {
        super(sidc);

        String echelon = this.symbolCode.getEchelon();
        if (!SymbolCode.isFieldEmpty(echelon))
            this.echelonSymbol = this.createEchelonSymbol(sidc);
    }

    /** {@inheritDoc} Overridden to render the echelon modifier. */
    @Override
    protected void doRenderGraphicModifiers(DrawContext dc)
    {
        super.doRenderGraphicModifiers(dc);

        if (this.echelonSymbol != null)
        {
            this.echelonSymbol.render(dc);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected String createLabelText()
    {
        String label = this.getGraphicLabel();
        String text = this.getText();

        if (label == null && text == null)
        {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (!WWUtil.isEmpty(label))
        {
            sb.append(label).append(" ");
        }

        if (!WWUtil.isEmpty(text))
        {
            sb.append(text);
        }

        return sb.toString();
    }

    @Override
    protected String getGraphicLabel()
    {
        if (TacGrpSidc.C2GM_DEF_ARS_BTLPSN_PBNO.equalsIgnoreCase(this.maskedSymbolCode))
            return "(P)";

        return null;
    }

    /** {@inheritDoc} Overridden to determine the position of the echelon label. */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (this.labels == null || this.labels.isEmpty())
            return;

        Position mainLabelPosition = this.determineMainLabelPosition(dc);
        this.labels.get(0).setPosition(mainLabelPosition);

        // Always call identity label position method because this method also places the echelon label.
        this.determineIdentityLabelPositions();
    }

    /** {@inheritDoc} Overridden to determine the position of the echelon label as well as the identity labels. */
    @Override
    protected void determineIdentityLabelPositions()
    {
        // Note: this method determines the position of the echelon modifier even though this modifier is implemented
        // as a tactical symbol and not as a label. The position of the echelon modifier is related to the position of
        // the identity labels, so it makes sense to handle them together.

        // Position the echelon symbol between the first and second control points.
        Iterator<? extends Position> iterator = this.getPositions().iterator();
        Position first = iterator.next();
        Position second = iterator.next();

        // Keep track of where the polygon begins.
        Position startPosition = first;

        LatLon midpoint = LatLon.interpolate(0.5, first, second);
        if (this.echelonSymbol != null)
        {
            this.echelonSymbol.setPosition(new Position(midpoint, 0));
        }

        int count = this.getPositionCount();
        if (this.identityLabel1 != null)
        {
            // Step one quarter of the way around the polygon and place the first identity label.
            for (int i = 0; i < (count + 1) / 4; i++)
            {
                first = second;
                second = iterator.next();
            }

            midpoint = LatLon.interpolate(0.5, first, second);
            this.identityLabel1.setPosition(new Position(midpoint, 0));
        }

        if (this.identityLabel2 != null)
        {
            // Step another quarter of the way and place the second identity label.  If the control points are more or less
            // evenly distributed, this will put the identity labels on opposite sides of the polygon, and away from the
            // echelon label.
            for (int i = 0; i <= count / 4; i++)
            {
                first = second;
                second = iterator.hasNext() ? iterator.next() : startPosition;
            }

            midpoint = LatLon.interpolate(0.5, first, second);
            if (this.identityLabel2 != null)
            {
                this.identityLabel2.setPosition(new Position(midpoint, 0));
            }
        }
    }

    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        // Prepared but not Occupied graphic always renders with dashed lines.
        if (TacGrpSidc.C2GM_DEF_ARS_BTLPSN_PBNO.equalsIgnoreCase(this.maskedSymbolCode))
        {
            attributes.setOutlineStippleFactor(PBNO_OUTLINE_STIPPLE_FACTOR);
            attributes.setOutlineStipplePattern(this.getOutlineStipplePattern());
        }
    }

    /** {@inheritDoc} Overridden to update echelon symbol attributes. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Apply active attributes to the symbol.
        if (this.symbolAttributes != null)
        {
            ShapeAttributes activeAttributes = this.getActiveShapeAttributes();
            this.symbolAttributes.setOpacity(activeAttributes.getInteriorOpacity());
            this.symbolAttributes.setTextModifierMaterial(this.getLabelMaterial());
        }
    }

    /** {@inheritDoc} Overridden to apply delegate owner to echelon symbol. */
    @Override
    protected void applyDelegateOwner(Object owner)
    {
        super.applyDelegateOwner(owner);
        if (this.echelonSymbol != null)
        {
            this.echelonSymbol.setDelegateOwner(owner);
        }
    }

    /**
     * Create a tactical symbol to render the echelon modifier.
     *
     * @param sidc Identifier for the symbol.
     *
     * @return A symbol to render the echelon modifier.
     */
    protected TacticalSymbol createEchelonSymbol(String sidc)
    {
        TacticalSymbol symbol = new EchelonSymbol(sidc);

        if (this.symbolAttributes == null)
            this.symbolAttributes = new BasicTacticalSymbolAttributes();
        symbol.setAttributes(this.symbolAttributes);

        return symbol;
    }
}
