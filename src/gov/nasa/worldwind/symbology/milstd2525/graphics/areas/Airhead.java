/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;
import gov.nasa.worldwind.util.WWUtil;

import java.util.*;

/**
 * Implementation of the Airhead graphic (2.X.2.6.2.2).
 *
 * @author pabercrombie
 * @version $Id: Airhead.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Airhead extends BasicArea
{
    /**
     * Default offset to apply to the label. The default aligns the top center of the label with the label's geographic
     * position, in order to keep the text South of the area.
     */
    public final static Offset DEFAULT_OFFSET = new Offset(0d, 0d, AVKey.FRACTION, AVKey.FRACTION);

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_SPL_ARA_AHD);
    }

    /**
     * Create a new area graphic.
     *
     * @param sidc Symbol code the identifies the graphic.
     */
    public Airhead(String sidc)
    {
        super(sidc);
        this.setShowHostileIndicator(false);
    }

    @Override
    protected String createLabelText()
    {
        String text = this.getText();

        StringBuilder sb = new StringBuilder();

        sb.append("AIRHEAD LINE\n");
        sb.append("(PL ");

        if (!WWUtil.isEmpty(text))
        {
            sb.append(text);
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Compute the position for the area's main label. This position indicates the position of the first line of the
     * label. If there are more lines, they will be arranged South of the first line.
     *
     * @param dc Current draw context.
     *
     * @return Position for the graphic's main label.
     */
    @Override
    protected Position determineMainLabelPosition(DrawContext dc)
    {
        Iterable<? extends LatLon> locations = this.polygon.getLocations();
        if (locations == null)
            return null;

        Sector sector = Sector.boundingSector(locations);

        Angle minLat = sector.getMinLatitude();
        Angle avgLon = sector.getCentroid().longitude;

        // Place the label at Southern edge of the area, at the average longitude.
        return new Position(minLat, avgLon, 0);
    }

    /** {@inheritDoc} */
    @Override
    protected Offset getDefaultLabelOffset()
    {
        return DEFAULT_OFFSET;
    }
}
