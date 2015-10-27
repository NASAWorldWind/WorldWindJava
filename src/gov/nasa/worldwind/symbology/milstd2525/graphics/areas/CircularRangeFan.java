/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of the Circular Weapon/Sensor Range Fan graphic (2.X.4.3.4.1).
 *
 * @author pabercrombie
 * @version $Id: CircularRangeFan.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class CircularRangeFan extends AbstractMilStd2525TacticalGraphic implements PreRenderable
{
    protected final static Offset LABEL_OFFSET = Offset.fromFraction(0d, 0d);

    /** Position of the center of the range fan. */
    protected Position position;
    /** Rings that make up the range fan. */
    protected List<SurfaceCircle> rings;

    /** Symbol drawn at the center of the range fan. */
    protected TacticalSymbol symbol;
    /** Attributes applied to the symbol. */
    protected TacticalSymbolAttributes symbolAttributes;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.FSUPP_ARS_WPNRF_CIRCLR);
    }

    /**
     * Create the range fan.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public CircularRangeFan(String sidc)
    {
        super(sidc);
        this.rings = new ArrayList<SurfaceCircle>();
    }

    /**
     * Indicates the center position of the range ran.
     *
     * @return The range fan center position.
     */
    public Position getPosition()
    {
        return this.getReferencePosition();
    }

    /**
     * Specifies the center position of the range ran.
     *
     * @param position The new center position.
     */
    public void setPosition(Position position)
    {
        this.moveTo(position);
    }

    /**
     * {@inheritDoc}
     *
     * @param positions Control points. This graphic uses only one control point, which determines the center of the
     *                  circle.
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

        this.position = iterator.next();

        for (SurfaceCircle ring : this.rings)
        {
            ring.setCenter(this.position);
        }

        if (this.symbol != null)
        {
            this.symbol.setPosition(this.position);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void setModifier(String modifier, Object value)
    {
        if (SymbologyConstants.DISTANCE.equals(modifier))
        {
            if (value instanceof Iterable)
            {
                //noinspection unchecked
                this.setRadii((Iterable) value);
            }
            else if (value instanceof Double)
            {
                this.setRadii(Arrays.asList((Double) value));
            }
        }
        else if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier) && value instanceof String)
        {
            this.setSymbol((String) value);
        }
        else
        {
            super.setModifier(modifier, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getModifier(String modifier)
    {
        if (SymbologyConstants.DISTANCE.equals(modifier))
        {
            return this.getRadii();
        }
        else if (SymbologyConstants.SYMBOL_INDICATOR.equals(modifier))
        {
            return this.getSymbol();
        }
        else
        {
            return super.getModifier(modifier);
        }
    }

    /**
     * Indicates the radii of the rings that make up the range fan.
     *
     * @return List of radii, in meters. If there are no rings this returns an empty list.
     */
    public List<Double> getRadii()
    {
        List<Double> radii = new ArrayList<Double>();
        for (SurfaceCircle ring : this.rings)
        {
            radii.add(ring.getRadius());
        }
        return radii;
    }

    /**
     * Specifies the radii of the rings that make up the range fan.
     *
     * @param radii List of radii, in meters. A circle will be created for each radius.
     */
    public void setRadii(Iterable<Double> radii)
    {
        this.rings.clear();

        for (Double d : radii)
        {
            if (d != null)
            {
                SurfaceCircle ring = this.createCircle();
                ring.setRadius(d);
                if (this.position != null)
                {
                    ring.setCenter(this.position);
                }

                this.rings.add(ring);
            }
        }

        this.onModifierChanged();
    }

    /**
     * Indicates a symbol drawn at the center of the range fan.
     *
     * @return The symbol drawn at the center of the range fan. May be null.
     */
    public String getSymbol()
    {
        return this.symbol != null ? this.symbol.getIdentifier() : null;
    }

    /**
     * Specifies a symbol to draw at the center of the range fan. Equivalent to setting the {@link
     * SymbologyConstants#SYMBOL_INDICATOR} modifier. The symbol's position will be changed to match the range fan
     * center position.
     *
     * @param sidc Identifier for a MIL-STD-2525C symbol to draw at the center of the range fan. May be null to indicate
     *             that no symbol is drawn.
     */
    public void setSymbol(String sidc)
    {
        if (sidc != null)
        {
            if (this.symbolAttributes == null)
                this.symbolAttributes = new BasicTacticalSymbolAttributes();

            this.symbol = this.createSymbol(sidc, this.getPosition(), this.symbolAttributes);
        }
        else
        {
            // Null value indicates no symbol.
            this.symbol = null;
            this.symbolAttributes = null;
        }
        this.onModifierChanged();
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return Arrays.asList(this.position);
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        this.determineActiveAttributes();

        for (SurfaceCircle ring : this.rings)
        {
            ring.preRender(dc);
        }
    }

    /**
     * Render the polygon.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphic(DrawContext dc)
    {
        for (SurfaceCircle ring : this.rings)
        {
            ring.render(dc);
        }
    }

    /** {@inheritDoc} Overridden to render symbol at the center of the range fan. */
    @Override
    protected void doRenderGraphicModifiers(DrawContext dc)
    {
        super.doRenderGraphicModifiers(dc);

        if (this.symbol != null)
        {
            this.symbol.render(dc);
        }
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        Iterator altIterator = null;

        // See if the altitude modifier is set. If so, use it's value to construct altitude labels.
        Object modifier = this.getModifier(SymbologyConstants.ALTITUDE_DEPTH);
        if (modifier instanceof Iterable)
        {
            altIterator = ((Iterable) modifier).iterator();
        }
        else if (modifier != null)
        {
            // Use the modifier as the altitude of the first ring
            altIterator = Arrays.asList(modifier).iterator();
        }

        // Create a label for each ring
        for (int i = 0; i < this.rings.size(); i++)
        {
            SurfaceCircle ring = this.rings.get(i);
            StringBuilder sb = new StringBuilder();

            if (i == 0)
            {
                sb.append("MIN RG ");
            }
            else
            {
                sb.append("MAX RG(");
                sb.append(i);
                sb.append(") ");
            }
            sb.append(ring.getRadius());

            // Append the altitude, if available
            if (altIterator != null && altIterator.hasNext())
            {
                Object alt = altIterator.next();
                sb.append("\n");
                sb.append("ALT ");
                sb.append(alt);
            }

            TacticalGraphicLabel label = this.addLabel(sb.toString());
            label.setOffset(LABEL_OFFSET);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        double dueSouth = Angle.POS180.radians;
        double globeRadius = dc.getGlobe().getRadius();

        int i = 0;
        for (SurfaceCircle ring : this.rings)
        {
            double radius = ring.getRadius();

            // Position the label at the Southern edge of the ring
            LatLon ll = LatLon.greatCircleEndPosition(this.position, dueSouth, radius / globeRadius);

            this.labels.get(i).setPosition(new Position(ll, 0));
            i += 1;
        }
    }

    /** {@inheritDoc} Overridden to update symbol attributes. */
    @Override
    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        // Apply active attributes to the symbol.
        if (this.symbolAttributes != null)
        {
            ShapeAttributes activeAttributes = this.getActiveShapeAttributes();
            this.symbolAttributes.setOpacity(activeAttributes.getInteriorOpacity());
            this.symbolAttributes.setScale(this.activeOverrides.getScale());
        }
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        if (this.rings == null)
            return;

        for (SurfaceCircle ring : this.rings)
        {
            ring.setDelegateOwner(owner);
        }
    }

    /**
     * Create a circle for a range ring.
     *
     * @return New circle.
     */
    protected SurfaceCircle createCircle()
    {
        SurfaceCircle circle = new SurfaceCircle();
        circle.setDelegateOwner(this.getActiveDelegateOwner());
        circle.setAttributes(this.activeShapeAttributes);
        return circle;
    }
}