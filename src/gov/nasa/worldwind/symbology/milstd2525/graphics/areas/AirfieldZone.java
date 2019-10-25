/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.symbology.milstd2525.graphics.areas;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.*;

/**
 * Implementation of the Airfield Zone graphic (hierarchy 2.X.2.1.3.11, SIDC: G*GPGAZ---****X).
 *
 * @author pabercrombie
 * @version $Id: AirfieldZone.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class AirfieldZone extends BasicArea
{
    /** Paths used to draw the airfield graphic. */
    protected List<Path> airfieldPaths;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this class supports.
     */
    public static List<String> getSupportedGraphics()
    {
        return Arrays.asList(TacGrpSidc.C2GM_GNL_ARS_AIRFZ);
    }

    public AirfieldZone(String sidc)
    {
        super(sidc);
    }

    /** {@inheritDoc} */
    @Override
    public void setPositions(Iterable<? extends Position> positions)
    {
        super.setPositions(positions);
        this.airfieldPaths = null; // Need to regenerate
    }

    /** {@inheritDoc} Overridden to draw airfield graphic. */
    @Override
    protected void doRenderGraphic(DrawContext dc)
    {
        super.doRenderGraphic(dc);

        for (Path path : this.airfieldPaths)
        {
            path.render(dc);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return null, Airfield Zone does not support text modifiers.
     */
    @Override
    protected String createLabelText()
    {
        // Text modifier not supported
        return "";
    }

    /**
     * Create shapes to draw the airfield graphic.
     *
     * @param dc Current draw context.
     */
    @Override
    protected void makeShapes(DrawContext dc)
    {
        if (this.airfieldPaths == null)
        {
            this.airfieldPaths = this.createAirfieldPaths(dc);
        }
    }

    /**
     * Create shapes to draw the airfield graphic.
     *
     * @param dc Current draw context.
     *
     * @return List of Paths that make up the airfield graphic.
     */
    protected List<Path> createAirfieldPaths(DrawContext dc)
    {
        List<Path> paths = new ArrayList<Path>();

        List<Sector> sectors = this.polygon.getSectors(dc);
        if (sectors == null)
        {
            return Collections.emptyList();
        }

        Sector sector = sectors.get(0);
        LatLon centroid = sector.getCentroid();

        // Size the symbol to fill about 30% of the polygon
        Angle distance = sector.getDeltaLon().divide(6);

        // Construct a path from East to West
        LatLon p1 = LatLon.greatCircleEndPosition(centroid, Angle.POS90, distance);
        LatLon p2 = LatLon.greatCircleEndPosition(centroid, Angle.NEG90, distance);
        Path newPath = new Path(new Position(p1, 0), new Position(p2, 0));
        this.configurePath(newPath);
        paths.add(newPath);

        // Construct a path skewed 40 degrees to the first path
        p1 = LatLon.greatCircleEndPosition(centroid, Angle.fromDegrees(50), distance);
        p2 = LatLon.greatCircleEndPosition(centroid, Angle.fromDegrees(-130), distance);
        newPath = new Path(new Position(p1, 0), new Position(p2, 0));
        this.configurePath(newPath);
        paths.add(newPath);

        return paths;
    }

    /**
     * Configure a path in the airfield graphic. Paths are configured to follow terrain and clamp to the ground.
     *
     * @param path Path to configure.
     */
    protected void configurePath(Path path)
    {
        path.setDelegateOwner(this);
        path.setSurfacePath(true);
        path.setAttributes(this.activeShapeAttributes);
    }
}
