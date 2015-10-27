/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.TacticalGraphicLabel;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Implementation of phase line graphics. This class implements the following graphics:
 * <p/>
 * <ul> <li>Phase Line (2.X.2.1.2.4)</li> <li>Light Line (2.X.2.1.2.5)</li> <li>Final Coordination Line
 * (2.X.2.5.2.3)</li> <li>Limits of Advance (2.X.2.5.2.5)</li> <li>Line of Departure (2.X.2.5.2.6)</li> <li>Line of
 * Departure/Line of Contact (2.X.2.5.2.7)</li> <li>Line of Departure/Line of Contact (2.X.2.5.2.8)</li> <li>Release
 * Line (2.X.2.6.1.3)</li> <li>No-Fire Line (2.X.4.2.2.3)</li> </ul>
 *
 * @author pabercrombie
 * @version $Id: PhaseLine.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class PhaseLine extends AbstractMilStd2525TacticalGraphic
{
    /** Factor applied to the stipple pattern used to draw the dashed line for a Probable Line of Deployment. */
    protected static final int PLD_OUTLINE_STIPPLE_FACTOR = 12;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_GNL_LNE_PHELNE,
            TacGrpSidc.C2GM_GNL_LNE_LITLNE,
            TacGrpSidc.C2GM_OFF_LNE_FCL,
            TacGrpSidc.C2GM_OFF_LNE_LMTADV,
            TacGrpSidc.C2GM_OFF_LNE_LD,
            TacGrpSidc.C2GM_OFF_LNE_LDLC,
            TacGrpSidc.C2GM_OFF_LNE_PLD,
            TacGrpSidc.C2GM_SPL_LNE_REL,
            TacGrpSidc.FSUPP_LNE_C2LNE_NFL
        );
    }

    /** Path used to render the line. */
    protected Path path;

    /**
     * Create a new Phase Line.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public PhaseLine(String sidc)
    {
        super(sidc);
        this.path = this.createPath();
    }

    /** {@inheritDoc} */
    public void setPositions(Iterable<? extends Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            // Ensure that at least two positions are provided.
            Iterator<? extends Position> iterator = positions.iterator();
            iterator.next();
            iterator.next();
        }
        catch (NoSuchElementException e)
        {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.path.setPositions(positions);
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        return this.path.getPositions();
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.path.getReferencePosition();
    }

    /** {@inheritDoc} */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.path.render(dc);
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.path.setDelegateOwner(owner);
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @return New path configured with defaults appropriate for this type of graphic.
     */
    protected Path createPath()
    {
        Path path = new Path();
        path.setFollowTerrain(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        return path;
    }

    protected String getGraphicLabel()
    {
        String code = this.maskedSymbolCode;

        String pattern = null;

        if (TacGrpSidc.C2GM_GNL_LNE_PHELNE.equalsIgnoreCase(code))
            pattern = "PL %s";
        else if (TacGrpSidc.C2GM_GNL_LNE_LITLNE.equalsIgnoreCase(code))
            pattern = "LL\n(PL %s)";
        else if (TacGrpSidc.C2GM_OFF_LNE_FCL.equalsIgnoreCase(code))
            pattern = "FINAL CL\n(PL %s)";
        else if (TacGrpSidc.C2GM_OFF_LNE_LMTADV.equalsIgnoreCase(code))
            pattern = "LOA\n(PL %s)";
        else if (TacGrpSidc.C2GM_OFF_LNE_LD.equalsIgnoreCase(code))
            pattern = "LD\n(PL %s)";
        else if (TacGrpSidc.C2GM_OFF_LNE_LDLC.equalsIgnoreCase(code))
            pattern = "LD/LC\n(PL %s)";
        else if (TacGrpSidc.C2GM_OFF_LNE_PLD.equalsIgnoreCase(code))
            pattern = "PLD\n(PL %s)";
        else if (TacGrpSidc.C2GM_SPL_LNE_REL.equalsIgnoreCase(code))
            pattern = "RL\n(PL %s)";
        else if (TacGrpSidc.FSUPP_LNE_C2LNE_NFL.equalsIgnoreCase(code))
            pattern = "NFL\n(PL %s)";

        if (pattern != null)
        {
            String text = this.getText();
            return String.format(pattern, text != null ? text : "");
        }

        return "";
    }

    /** Create labels for the start and end of the path. */
    @Override
    protected void createLabels()
    {
        String text = this.getGraphicLabel();

        this.addLabel(text); // Start label
        this.addLabel(text); // End label
    }

    /**
     * Determine positions for the start and end labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        Iterator<? extends Position> iterator = this.path.getPositions().iterator();

        // Find the first and last positions on the path
        Position first = iterator.next();
        Position last = first;
        while (iterator.hasNext())
        {
            last = iterator.next();
        }

        TacticalGraphicLabel startLabel = this.labels.get(0);
        TacticalGraphicLabel endLabel = this.labels.get(1);

        // Position the labels at the ends of the path
        startLabel.setPosition(first);
        endLabel.setPosition(last);

        // Set the West-most label to right alignment, and the East-most label to left alignment.
        if (first.longitude.degrees < last.longitude.degrees)
        {
            startLabel.setTextAlign(AVKey.RIGHT);
            endLabel.setTextAlign(AVKey.LEFT);
        }
        else
        {
            startLabel.setTextAlign(AVKey.LEFT);
            endLabel.setTextAlign(AVKey.RIGHT);
        }
    }

    @Override
    protected void applyDefaultAttributes(ShapeAttributes attributes)
    {
        super.applyDefaultAttributes(attributes);

        // Probable Line of Deployment graphic always renders with dashed lines.
        if (TacGrpSidc.C2GM_OFF_LNE_PLD.equalsIgnoreCase(this.maskedSymbolCode))
        {
            attributes.setOutlineStippleFactor(PLD_OUTLINE_STIPPLE_FACTOR);
            attributes.setOutlineStipplePattern(this.getOutlineStipplePattern());
        }
    }
}
