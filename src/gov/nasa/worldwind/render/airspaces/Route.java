/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Creates a sequence of connected rectangular airspaces specified by a list of positions. Each position but the last
 * begins a new rectangle from that position to the following position. The width of the rectangles is specified by this
 * class's width parameter.
 *
 * @author garakl
 * @version $Id: Route.java 2563 2014-12-12 19:29:38Z dcollins $
 */
public class Route extends TrackAirspace
{
    private List<LatLon> locations = new ArrayList<LatLon>();
    private double width = 1.0;

    public Route(List<? extends LatLon> locations, double width)
    {
        if (width < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width=" + width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.width = width;
        this.addLocations(locations);
        this.setEnableInnerCaps(false);
    }

    public Route(AirspaceAttributes attributes)
    {
        super(attributes);
        this.setEnableInnerCaps(false);
    }

    public Route()
    {
        this.setEnableInnerCaps(false);
    }

    public Route(Route source)
    {
        super(source);

        this.locations = new ArrayList<LatLon>(source.locations.size());
        for (LatLon location : source.locations)
        {
            this.locations.add(location);
        }

        this.width = source.width;
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return java.util.Collections.unmodifiableList(this.locations);
    }

    public void setLocations(Iterable<? extends LatLon> locations)
    {
        this.locations.clear();
        this.removeAllLegs();
        this.addLocations(locations);
    }

    protected void addLocations(Iterable<? extends LatLon> newLocations)
    {
        if (newLocations != null)
        {
            LatLon last = null;
            for (LatLon cur : newLocations)
            {
                if (cur != null)
                {
                    if (last != null)
                        this.addLeg(last, cur);
                    last = cur;
                }
            }
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    public double getWidth()
    {
        return this.width;
    }

    public void setWidth(double width)
    {
        if (width < 0.0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width=" + width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.width = width;

        double legWidth = this.width / 2.0;
        for (Box l : this.getLegs())
        {
            l.setWidths(legWidth, legWidth);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    public Box addLeg(LatLon start, LatLon end)
    {
        if (start == null)
        {
            String message = "nullValue.StartIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (end == null)
        {
            String message = "nullValue.EndIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.locations.size() == 0)
        {
            this.locations.add(start);
            this.locations.add(end);
        }
        else
        {
            LatLon last = this.locations.get(this.locations.size() - 1);
            if (start.equals(last))
            {
                this.locations.add(end);
            }
            else
            {
                String message = "Shapes.Route.DisjointLegDetected";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        double[] altitudes = this.getAltitudes();
        boolean[] terrainConformant = this.isTerrainConforming();
        double legWidth = this.width / 2.0;

        Box leg = new Box();
        leg.setAltitudes(altitudes[0], altitudes[1]);
        leg.setTerrainConforming(terrainConformant[0], terrainConformant[1]);
        leg.setLocations(start, end);
        leg.setWidths(legWidth, legWidth);
        this.addLeg(leg);

        return leg;
    }

    @Override
    /**
     * This method is not supported for {@link gov.nasa.worldwind.render.airspaces.Route}.
     */
    public void setLegs(Collection<Box> legs)
    {
        String message = Logging.getMessage("generic.UnsupportedOperation", "setLegs");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException();
//
//        super.setLegs(legs);
//
//        this.locations.clear();
//
//        if (legs != null)
//        {
//            Iterator<Box> iterator = legs.iterator();
//            while (iterator.hasNext())
//            {
//                Box leg = iterator.next();
//                this.locations.add(leg.getLocations()[0]);
//
//                if (!iterator.hasNext())
//                    this.locations.add(leg.getLocations()[1]);
//            }
//
//            double[] widths = this.getLegs().get(0).getWidths();
//            this.width = widths[0] + widths[1];
//        }
    }

    @Override
    /**
     * This method is not supported for {@link gov.nasa.worldwind.render.airspaces.Route}.
     */
    public Box addLeg(LatLon start, LatLon end, double lowerAltitude, double upperAltitude, double leftWidth,
        double rightWidth)
    {
        String message = Logging.getMessage("generic.UnsupportedOperation", "addLeg");
        Logging.logger().severe(message);
        throw new UnsupportedOperationException();

//        Box newLeg = super.addLeg(start, end, lowerAltitude, upperAltitude, leftWidth, rightWidth);
//
//        if (this.getLegs().size() == 1)
//            this.locations.add(newLeg.getLocations()[0]);
//
//        this.locations.add(newLeg.getLocations()[1]);
//
//        this.width = newLeg.getWidths()[0] + newLeg.getWidths()[1];
//
//        return newLeg;
    }

    public Position getReferencePosition()
    {
        return this.computeReferencePosition(this.locations, this.getAltitudes());
    }

    protected void doMoveTo(Globe globe, Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldRef, newRef, this.getLocations());
        this.setLocations(newLocations);
    }

    protected void doMoveTo(Position oldRef, Position newRef)
    {
        if (oldRef == null)
        {
            String message = "nullValue.OldRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRef == null)
        {
            String message = "nullValue.NewRefIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.doMoveTo(oldRef, newRef);

        int count = this.locations.size();
        LatLon[] newLocations = new LatLon[count];
        for (int i = 0; i < count; i++)
        {
            LatLon ll = this.locations.get(i);
            double distance = LatLon.greatCircleDistance(oldRef, ll).radians;
            double azimuth = LatLon.greatCircleAzimuth(oldRef, ll).radians;
            newLocations[i] = LatLon.greatCircleEndPosition(newRef, azimuth, distance);
        }
        this.setLocations(Arrays.asList(newLocations));
    }

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsDouble(context, "width", this.width);
        rs.addStateValueAsLatLonList(context, "locations", this.locations);
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Double d = rs.getStateValueAsDouble(context, "width");
        if (d != null)
            this.setWidth(d);

        List<LatLon> locs = rs.getStateValueAsLatLonList(context, "locations");
        if (locs != null)
            this.setLocations(locs);
    }
}
