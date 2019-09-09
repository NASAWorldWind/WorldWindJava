/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of general area graphics. This class implements the following graphics:
 * <ul> <li>General Area (2.X.2.1.3.1)</li> <li>Assembly Area (2.X.2.1.3.2)</li> <li>Engagement Area (2.X.2.1.3.3)</li>
 * <li>Drop Zone (2.X.2.1.3.5)</li> <li>Extraction Zone (2.X.2.1.3.6)</li> <li>Landing Zone (2.X.2.1.3.7)</li>
 * <li>Pickup Zone (2.X.2.1.3.8)</li> <li>Forward Arming and Refueling Area (FARP) (2.X.5.3.3)</li></ul>
 *
 * @author pabercrombie
 * @version $Id: BasicArea.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class BasicArea extends AbstractMilStd2525TacticalGraphic implements PreRenderable
{
    protected SurfacePolygon polygon;

    /** First "ENY" label, for hostile entities. */
    protected TacticalGraphicLabel identityLabel1;
    /** Second "ENY" label, for hostile entities. */
    protected TacticalGraphicLabel identityLabel2;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(
            TacGrpSidc.C2GM_GNL_ARS_GENARA,
            TacGrpSidc.C2GM_GNL_ARS_ABYARA,
            TacGrpSidc.C2GM_GNL_ARS_EMTARA,
            TacGrpSidc.C2GM_GNL_ARS_EZ,
            TacGrpSidc.C2GM_GNL_ARS_LZ,
            TacGrpSidc.C2GM_GNL_ARS_PZ,
            TacGrpSidc.C2GM_GNL_ARS_DRPZ,
            TacGrpSidc.C2GM_DEF_ARS_EMTARA);
    }

    public BasicArea(String sidc)
    {
        super(sidc);
        this.polygon = this.createPolygon();
    }

    /** {@inheritDoc} */
    public void setPositions(Iterable<? extends Position> positions)
    {
        this.polygon.setLocations(positions);
    }

    /** {@inheritDoc} */
    public Iterable<? extends Position> getPositions()
    {
        Iterable<? extends LatLon> locations = this.polygon.getLocations();
        ArrayList<Position> positions = new ArrayList<Position>();

        if (locations == null)
        {
            return null;
        }

        for (LatLon ll : locations)
        {
            if (ll instanceof Position)
                positions.add((Position) ll);
            else
                positions.add(new Position(ll, 0));
        }

        return positions;
    }

    /** {@inheritDoc} */
    public Position getReferencePosition()
    {
        return this.polygon.getReferencePosition();
    }

    /** {@inheritDoc} */
    public void preRender(DrawContext dc)
    {
        if (!this.isVisible())
        {
            return;
        }

        this.makeShapes(dc);

        this.determinePerFrameAttributes(dc);

        this.polygon.preRender(dc);
    }

    protected void makeShapes(DrawContext dc)
    {
        // Do nothing, but allow subclasses to override
    }

    /**
     * Render the polygon.
     *
     * @param dc Current draw context.
     */
    protected void doRenderGraphic(DrawContext dc)
    {
        this.polygon.render(dc);
    }

    /**
     * Create the text for the main label on this graphic.
     *
     * @return Text for the main label. May return null if there is no text.
     */
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
            sb.append(label).append("\n");
        }

        if (!WWUtil.isEmpty(text))
        {
            sb.append(text);
        }

        return sb.toString();
    }

    protected String getGraphicLabel()
    {
        String code = this.maskedSymbolCode;

        if (TacGrpSidc.C2GM_GNL_ARS_GENARA.equalsIgnoreCase(code))
            return "";
        else if (TacGrpSidc.C2GM_GNL_ARS_ABYARA.equalsIgnoreCase(code))
            return "AA";
        else if (TacGrpSidc.C2GM_GNL_ARS_DRPZ.equalsIgnoreCase(code))
            return "DZ";
        else if (TacGrpSidc.C2GM_GNL_ARS_EMTARA.equalsIgnoreCase(
            code) || TacGrpSidc.C2GM_DEF_ARS_EMTARA.equalsIgnoreCase(code))
            return "EA";
        else if (TacGrpSidc.C2GM_GNL_ARS_EZ.equalsIgnoreCase(code))
            return "EZ";
        else if (TacGrpSidc.C2GM_GNL_ARS_LZ.equalsIgnoreCase(code))
            return "LZ";
        else if (TacGrpSidc.C2GM_GNL_ARS_PZ.equalsIgnoreCase(code))
            return "PZ";

        return "";
    }

    /**
     * Indicates the alignment of the graphic's main label.
     *
     * @return Alignment for the main label. One of AVKey.CENTER, AVKey.LEFT, or AVKey.RIGHT.
     */
    protected String getLabelAlignment()
    {
        return AVKey.CENTER;
    }

    @Override
    protected void createLabels()
    {
        String labelText = this.createLabelText();
        if (!WWUtil.isEmpty(labelText))
        {
            TacticalGraphicLabel mainLabel = this.addLabel(labelText);
            mainLabel.setTextAlign(this.getLabelAlignment());

            mainLabel.setOffset(this.getDefaultLabelOffset());
        }

        if (this.mustShowHostileIndicator())
        {
            this.identityLabel1 = this.addLabel(SymbologyConstants.HOSTILE_ENEMY);
            this.identityLabel2 = this.addLabel(SymbologyConstants.HOSTILE_ENEMY);
        }
    }

    /**
     * Determine the appropriate position for the graphic's labels.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void determineLabelPositions(DrawContext dc)
    {
        if (this.labels == null || this.labels.isEmpty())
            return;

        Position mainLabelPosition = this.determineMainLabelPosition(dc);
        this.labels.get(0).setPosition(mainLabelPosition);

        if (this.mustShowHostileIndicator())
        {
            this.determineIdentityLabelPositions();
        }
    }

    /**
     * Compute the position for the area's main label. This position indicates the position of the first line of the
     * label. If there are more lines, they will be arranged South of the first line.
     *
     * @param dc Current draw context.
     *
     * @return Position for the graphic's main label.
     */
    protected Position determineMainLabelPosition(DrawContext dc)
    {
        List<Sector> sectors = this.polygon.getSectors(dc);
        if (sectors != null)
        {
            // TODO: centroid of bounding sector is not always a good choice for label position
            Sector sector = sectors.get(0);
            return new Position(sector.getCentroid(), 0);
        }
        return this.getReferencePosition();
    }

    protected void determineIdentityLabelPositions()
    {
        // Position the first label between the first and second control points.
        Iterator<? extends Position> iterator = this.getPositions().iterator();
        Position first = iterator.next();
        Position second = iterator.next();

        LatLon midpoint = LatLon.interpolate(0.5, first, second);
        if (this.identityLabel1 != null)
        {
            this.identityLabel1.setPosition(new Position(midpoint, 0));
        }

        // Position the second label between the middle two control points in the position list. If the control
        // points are more or less evenly distributed, this will be about half way around the shape.
        int count = this.getPositionCount();
        iterator = this.getPositions().iterator();
        for (int i = 0; i < count / 2 + 1; i++)
        {
            first = iterator.next();
        }
        second = iterator.next();

        midpoint = LatLon.interpolate(0.5, first, second);
        if (this.identityLabel2 != null)
        {
            this.identityLabel2.setPosition(new Position(midpoint, 0));
        }
    }

    protected int getPositionCount()
    {
        int count = 0;
        //noinspection UnusedDeclaration
        for (Position p : this.getPositions())
        {
            count++;
        }
        return count;
    }

    /** {@inheritDoc} */
    protected void applyDelegateOwner(Object owner)
    {
        this.polygon.setDelegateOwner(owner);
    }

    protected SurfacePolygon createPolygon()
    {
        SurfacePolygon polygon = new SurfacePolygon();
        polygon.setDelegateOwner(this.getActiveDelegateOwner());
        polygon.setAttributes(this.getActiveShapeAttributes());
        return polygon;
    }
}
