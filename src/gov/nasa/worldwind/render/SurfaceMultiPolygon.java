/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: SurfaceMultiPolygon.java 2409 2014-10-29 23:47:03Z dcollins $
 */
public class SurfaceMultiPolygon extends AbstractSurfaceShape
{
    protected ContourList boundaries = new ContourList();

    /** Constructs a new surface multi polygon with the default attributes and no locations. */
    public SurfaceMultiPolygon()
    {
    }

    public SurfaceMultiPolygon(SurfaceMultiPolygon source)
    {
        super(source);

        this.boundaries.addAllContours(source.boundaries);
    }

    public SurfaceMultiPolygon(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    public SurfaceMultiPolygon(Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.addContour(iterable);
    }

    public SurfaceMultiPolygon(ContourList contours)
    {
        if (contours == null)
        {
            String message = Logging.getMessage("nullValue.ContourListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.addAllContours(contours);
    }

    public SurfaceMultiPolygon(ShapeAttributes normalAttrs, Iterable<? extends LatLon> iterable)
    {
        super(normalAttrs);

        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.addContour(iterable);
    }

    public SurfaceMultiPolygon(ShapeAttributes normalAttrs, ContourList contours)
    {
        super(normalAttrs);

        if (contours == null)
        {
            String message = Logging.getMessage("nullValue.ContourListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.addAllContours(contours);
    }

    public int getBoundaryCount()
    {
        return this.boundaries.getContourCount();
    }

    public Iterable<? extends LatLon> getBoundary(int index)
    {
        return this.boundaries.getContour(index);
    }

    public void addBoundary(Iterable<? extends LatLon> iterable)
    {
        if (iterable == null)
        {
            String message = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.addContour(iterable);
    }

    public void addAllBoundaries(ContourList contours)
    {
        if (contours == null)
        {
            String message = Logging.getMessage("nullValue.ContourListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.boundaries.addAllContours(contours);
    }

    public void removeAllBoundaries()
    {
        this.boundaries.removeAllContours();
    }

    @Override
    public Position getReferencePosition()
    {
        if (this.boundaries.getContourCount() == 0)
            return null;

        Iterator<? extends LatLon> iterator = this.boundaries.getContour(0).iterator();
        if (!iterator.hasNext())
            return null;

        return new Position(iterator.next(), 0);
    }

    @Override
    protected void doMoveTo(Position oldReferencePosition, Position newReferencePosition)
    {
        if (this.boundaries.getContourCount() == 0)
            return;

        for (int i = 0; i < this.boundaries.getContourCount(); i++)
        {
            ArrayList<LatLon> newLocations = new ArrayList<LatLon>();

            for (LatLon ll : this.boundaries.getContour(i))
            {
                Angle heading = LatLon.greatCircleAzimuth(oldReferencePosition, ll);
                Angle pathLength = LatLon.greatCircleDistance(oldReferencePosition, ll);
                newLocations.add(LatLon.greatCircleEndPosition(newReferencePosition, heading, pathLength));
            }

            this.boundaries.setContour(i, newLocations);
        }

        // We've changed the multi-polygon's list of boundaries; flag the shape as changed.
        this.onShapeChanged();
    }

    @Override
    protected void doMoveTo(Globe globe, Position oldReferencePosition, Position newReferencePosition)
    {
        if (this.boundaries.getContourCount() == 0)
            return;

        for (int i = 0; i < this.boundaries.getContourCount(); i++)
        {
            List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldReferencePosition,
                newReferencePosition, this.boundaries.getContour(i));

            this.boundaries.setContour(i, newLocations);
        }

        // We've changed the multi-polygon's list of boundaries; flag the shape as changed.
        this.onShapeChanged();
    }

    @Override
    protected List<List<LatLon>> createGeometry(Globe globe, double edgeIntervalsPerDegree)
    {
        if (this.boundaries.getContourCount() == 0)
            return null;

        ArrayList<List<LatLon>> geom = new ArrayList<List<LatLon>>();

        for (int i = 0; i < this.boundaries.getContourCount(); i++)
        {
            ArrayList<LatLon> locations = new ArrayList<LatLon>();
            this.generateIntermediateLocations(this.boundaries.getContour(i), edgeIntervalsPerDegree, true, locations);
            geom.add(locations);
        }

        return geom;
    }

    @Override
    public Iterable<? extends LatLon> getLocations(Globe globe)
    {
        if (this.boundaries.getContourCount() == 0)
            return null;

        ArrayList<LatLon> combinedBoundaries = new ArrayList<LatLon>();

        for (int i = 0; i < this.boundaries.getContourCount(); i++)
        {
            for (LatLon location : this.boundaries.getContour(i))
            {
                combinedBoundaries.add(location);
            }
        }

        return combinedBoundaries;
    }

    /**
     * Overridden to clear the multi-polygon's boundary list upon an unsuccessful tessellation attempt. This ensures the
     * multi-polygon won't attempt to re-tessellate itself each frame.
     *
     * @param dc the current DrawContext.
     */
    @Override
    protected void handleUnsuccessfulInteriorTessellation(DrawContext dc)
    {
        super.handleUnsuccessfulInteriorTessellation(dc);

        // If tessellating the multi-polygon's interior was unsuccessful, we clear the boundary list to avoid any
        // additional tessellation attempts and free any resources that the multi-polygon won't use.
        this.boundaries.removeAllContours();
        this.onShapeChanged();
    }

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        if (this.boundaries.getContourCount() > 0)
        {
            RestorableSupport.StateObject so = rs.addStateObject(context, "boundaries");
            for (int i = 0; i < this.boundaries.getContourCount(); i++)
            {
                rs.addStateValueAsLatLonList(so, "boundary", this.boundaries.getContour(i));
            }
        }
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        RestorableSupport.StateObject so = rs.getStateObject(context, "boundaries");
        if (so != null)
        {
            this.boundaries.removeAllContours();

            RestorableSupport.StateObject[] sos = rs.getAllStateObjects(so, "boundary");
            if (sos != null)
            {
                for (RestorableSupport.StateObject boundary : sos)
                {
                    if (boundary == null)
                        continue;

                    Iterable<LatLon> locations = rs.getStateObjectAsLatLonList(boundary);
                    if (locations != null)
                        this.boundaries.addContour(locations);
                }
            }

            // We've changed the polygon's list of boundaries; flag the shape as changed.
            this.onShapeChanged();
        }
    }

    @Override
    protected void exportAsKML(Object output) throws IOException, XMLStreamException
    {
        throw new UnsupportedOperationException("KML output not supported for SurfaceMultiPolygon");
    }
}
