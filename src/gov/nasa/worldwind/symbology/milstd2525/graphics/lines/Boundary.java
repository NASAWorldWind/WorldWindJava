/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.SymbolCode;
import gov.nasa.worldwind.symbology.milstd2525.graphics.*;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of the Boundary graphic (2.X.2.1.2.1).
 *
 * @author pabercrombie
 * @version $Id: Boundary.java 560 2012-04-26 16:28:24Z pabercrombie $
 */
public class Boundary extends PhaseLine
{
    /**
     * Offset applied to the graphic's upper label. This offset aligns the bottom edge of the label with the geographic
     * position, in order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset TOP_LABEL_OFFSET = Offset.fromFraction(0.0, 0.5);
    /**
     * Offset applied to the graphic's lower label. This offset aligns the top edge of the label with the geographic
     * position, in order to keep the label above the graphic as the zoom changes.
     */
    protected final static Offset BOTTOM_LABEL_OFFSET = Offset.fromFraction(0.0, -1.5);

    /** Tactical symbols used to render the echelon modifiers. */
    protected List<EchelonSymbol> echelonSymbols = Collections.emptyList();
    /** Attribute bundle for the echelon symbols. */
    protected TacticalSymbolAttributes symbolAttributes;

    // Determined when labels are created
    /** Indicates whether or not there are labels above the boundary line. */
    protected boolean haveTopLabel;
    /** Indicates whether or not there are labels below the boundary line. */
    protected boolean haveBottomLabel;
    /** Indicates whether or not there are hostile indicator labels ("ENY") along the line. */
    protected boolean haveHostileLabels;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_GNL_LNE_BNDS);
    }

    /**
     * The value of an optional second text string for the graphic. This value is equivalent to the "T1" modifier
     * defined by MIL-STD-2525C. It can be set using {@link #setAdditionalText(String)}, or by passing an Iterable to
     * {@link #setModifier(String, Object)} with a key of {@link gov.nasa.worldwind.symbology.SymbologyConstants#UNIQUE_DESIGNATION}
     * (additional text is the second value in the iterable).
     */
    protected String additionalText;

    /**
     * Create a new Boundary.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Boundary(String sidc)
    {
        super(sidc);
    }

    /**
     * Indicates an additional text identification for this graphic. This value is equivalent to the "T1" modifier in
     * MIL-STD-2525C (a second Unique Designation modifier).
     *
     * @return The additional text. May be null.
     */
    public String getAdditionalText()
    {
        return this.additionalText;
    }

    /**
     * Indicates an additional text identification for this graphic. Setting this value is equivalent to setting the
     * "T1" modifier in MIL-STD-2525C (a second Unique Designation modifier).
     *
     * @param text The additional text. May be null.
     */
    public void setAdditionalText(String text)
    {
        this.additionalText = text;
        this.onModifierChanged();
    }

    @Override
    public Object getModifier(String key)
    {
        // If two values are set for the Unique Designation, return both in a list.
        if (SymbologyConstants.UNIQUE_DESIGNATION.equals(key) && this.additionalText != null)
        {
            return Arrays.asList(this.getText(), this.getAdditionalText());
        }

        return super.getModifier(key);
    }

    @Override
    public void setModifier(String key, Object value)
    {
        if (SymbologyConstants.UNIQUE_DESIGNATION.equals(key) && value instanceof Iterable)
        {
            Iterator iterator = ((Iterable) value).iterator();
            if (iterator.hasNext())
            {
                this.setText((String) iterator.next());
            }

            // The Final Protective Fire graphic supports a second Unique Designation value
            if (iterator.hasNext())
            {
                this.setAdditionalText((String) iterator.next());
            }
        }
        else
        {
            super.setModifier(key, value);
        }
    }

    /** {@inheritDoc} Overridden to render the echelon modifier. */
    @Override
    protected void doRenderGraphicModifiers(DrawContext dc)
    {
        super.doRenderGraphicModifiers(dc);

        for (TacticalSymbol symbol : this.echelonSymbols)
        {
            symbol.render(dc);
        }
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        Iterable<? extends Position> positions = this.getPositions();
        if (positions == null)
            return;

        Iterator<? extends Position> iterator = positions.iterator();

        this.haveHostileLabels = this.mustCreateIdentityLabels();

        String text = this.getText();
        String additionalText = this.getAdditionalText();

        this.haveTopLabel = !WWUtil.isEmpty(text);
        this.haveBottomLabel = !WWUtil.isEmpty(additionalText);

        Offset topLabelOffset = this.getTopLabelOffset();
        Offset bottomLabelOffset = this.getBottomLabelOffset();

        String echelon = this.symbolCode.getEchelon();
        boolean haveEchelon = !SymbolCode.isFieldEmpty(echelon);
        this.echelonSymbols = haveEchelon ? new ArrayList<EchelonSymbol>() : Collections.<EchelonSymbol>emptyList();

        String sidc = this.symbolCode.toString();

        // There can be up to four labels per segment of the path, and one echelon modifier. Add labels to the list in
        // this order: top, bottom, hostile indicators.
        TacticalGraphicLabel label;
        iterator.next(); // Skip the first point. We are interested in line segments, not individual points.
        while (iterator.hasNext())
        {
            if (this.haveTopLabel)
            {
                label = this.addLabel(text);
                label.setOffset(topLabelOffset);
            }

            if (this.haveBottomLabel)
            {
                label = this.addLabel(additionalText);
                label.setOffset(bottomLabelOffset);
            }

            if (this.haveHostileLabels)
            {
                this.addLabel(SymbologyConstants.HOSTILE_ENEMY);
                this.addLabel(SymbologyConstants.HOSTILE_ENEMY);
            }

            if (haveEchelon)
            {
                this.echelonSymbols.add(this.createEchelonSymbol(sidc));
            }

            iterator.next();
        }
    }

    protected boolean mustCreateIdentityLabels()
    {
        return this.isShowHostileIndicator()
            && SymbologyConstants.STANDARD_IDENTITY_HOSTILE.equalsIgnoreCase(this.symbolCode.getStandardIdentity());
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Iterable<? extends Position> positions = this.getPositions();
        if (positions == null)
            return;

        Iterator<? extends Position> iterator = positions.iterator();
        Iterator<EchelonSymbol> echelonIterator = this.echelonSymbols.iterator();

        Iterator<TacticalGraphicLabel> labelIterator;
        if (this.labels != null)
            labelIterator = this.labels.iterator();
        else
            labelIterator = Collections.<TacticalGraphicLabel>emptyList().iterator();

        // There can be up to four labels for each segment of the path: top, bottom, and two hostile indicators. Expect
        // them in the label is in this order.
        Position posA = iterator.next();
        while (iterator.hasNext() && (labelIterator.hasNext() || echelonIterator.hasNext()))
        {
            Position posB = iterator.next();

            TacticalGraphicLabel topLabel = this.haveTopLabel ? labelIterator.next() : null;
            TacticalGraphicLabel bottomLabel = this.haveBottomLabel ? labelIterator.next() : null;

            // Place top, bottom, and echelon labels at the midpoint of the segment
            LatLon ll = LatLon.interpolate(0.5, posA, posB);
            Position labelPosition = new Position(ll, 0);

            if (topLabel != null)
            {
                topLabel.setPosition(labelPosition);
                topLabel.setOrientationPosition(posB);
            }

            if (bottomLabel != null)
            {
                bottomLabel.setPosition(labelPosition);
                bottomLabel.setOrientationPosition(posB);
            }

            if (echelonIterator.hasNext())
            {
                EchelonSymbol symbol = echelonIterator.next();
                symbol.setPosition(labelPosition);
                symbol.setOrientationPosition(posB);
            }

            if (this.haveHostileLabels)
            {
                // Position the first ENY label 25% of the distance from points A and B.
                TacticalGraphicLabel label = labelIterator.next();

                ll = LatLon.interpolate(0.25, posA, posB);
                label.setPosition(new Position(ll, 0));
                label.setOrientationPosition(posB);

                // Position the second ENY label at 75% of the distance.
                label = labelIterator.next();
                ll = LatLon.interpolate(0.75, posA, posB);
                label.setPosition(new Position(ll, 0));
                label.setOrientationPosition(posB);
            }

            posA = posB;
        }
    }

    /** {@inheritDoc} Overridden to apply owner to echelon modifiers. */
    @Override
    protected void applyDelegateOwner(Object owner)
    {
        super.applyDelegateOwner(owner);

        for (EchelonSymbol symbol : this.echelonSymbols)
        {
            symbol.setDelegateOwner(owner);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        // The default offset is applied to the "main" label by AbstractTacticalGraphic if the application does
        // not specify an override offset. On a boundary there isn't really a "main" label, but the first label
        // in the list is considered the main label. If we have top labels, this will be either a top label, bottom
        // label, or hostile indicator.
        if (this.haveTopLabel)
            return TOP_LABEL_OFFSET;
        else if (this.haveBottomLabel)
            return BOTTOM_LABEL_OFFSET;
        else
            return TacticalGraphicLabel.DEFAULT_OFFSET;
    }

    /**
     * Indicates the offset applied to the upper label.
     *
     * @return Offset applied to the upper label.
     */
    protected Offset getTopLabelOffset()
    {
        return TOP_LABEL_OFFSET;
    }

    /**
     * Indicates the offset applied to the lower label.
     *
     * @return Offset applied to the bottom label.
     */
    protected Offset getBottomLabelOffset()
    {
        return BOTTOM_LABEL_OFFSET;
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

    /**
     * Create a tactical symbol to render the echelon modifier.
     *
     * @param sidc Identifier for the symbol.
     *
     * @return A symbol to render the echelon modifier.
     */
    protected EchelonSymbol createEchelonSymbol(String sidc)
    {
        EchelonSymbol symbol = new EchelonSymbol(sidc);

        if (this.symbolAttributes == null)
            this.symbolAttributes = new BasicTacticalSymbolAttributes();
        symbol.setAttributes(this.symbolAttributes);

        return symbol;
    }
}
