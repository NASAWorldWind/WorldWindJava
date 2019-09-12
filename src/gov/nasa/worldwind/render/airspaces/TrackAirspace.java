/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Creates a sequence of potentially disconnected rectangular airspaces specified by a collection of {@link
 * gov.nasa.worldwind.render.airspaces.Box} objects.
 *
 * @author garakl
 * @version $Id: TrackAirspace.java 2565 2014-12-12 23:57:06Z dcollins $
 */
public class TrackAirspace extends AbstractAirspace
{
    protected List<Box> legs = new ArrayList<Box>();
    protected boolean legsOutOfDate = true;
    protected boolean enableInnerCaps = true;
    protected boolean enableCenterLine;
    /**
     * Denotes the the threshold that defines whether the angle between two adjacent legs is small. Initially 22.5
     * degrees.
     */
    protected Angle smallAngleThreshold = Angle.fromDegrees(22.5);

    public TrackAirspace(Collection<Box> legs)
    {
        this.addLegs(legs);
    }

    public TrackAirspace(AirspaceAttributes attributes)
    {
        super(attributes);
    }

    public TrackAirspace()
    {
    }

    public TrackAirspace(TrackAirspace source)
    {
        super(source);

        this.legs = new ArrayList<Box>(source.legs.size());
        for (Box leg : source.legs)
        {
            this.legs.add(new Box(leg));
        }

        this.enableInnerCaps = source.enableInnerCaps;
        this.enableCenterLine = source.enableInnerCaps;
        this.smallAngleThreshold = source.smallAngleThreshold;
    }

    public List<Box> getLegs()
    {
        return Collections.unmodifiableList(this.legs);
    }

    public void setLegs(Collection<Box> legs)
    {
        this.legs.clear();
        this.addLegs(legs);
    }

    protected void addLegs(Iterable<Box> newLegs)
    {
        if (newLegs != null)
        {
            for (Box b : newLegs)
            {
                if (b != null)
                    this.addLeg(b);
            }
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    public Box addLeg(LatLon start, LatLon end, double lowerAltitude, double upperAltitude,
        double leftWidth, double rightWidth)
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

        boolean[] terrainConformant = this.isTerrainConforming();

        Box leg = new Box();
        leg.setAltitudes(lowerAltitude, upperAltitude);
        leg.setTerrainConforming(terrainConformant[0], terrainConformant[1]);
        leg.setLocations(start, end);
        leg.setWidths(leftWidth, rightWidth);
        this.addLeg(leg);
        return leg;
    }

    protected void addLeg(Box leg)
    {
        if (leg == null)
        {
            String message = "nullValue.LegIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        leg.setAlwaysOnTop(this.isAlwaysOnTop());
        leg.setForceCullFace(true);
        leg.setEnableCenterLine(this.enableCenterLine);
        leg.setDrawSurfaceShape(this.drawSurfaceShape);
        this.legs.add(leg);
        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    public void removeAllLegs()
    {
        this.legs.clear();
    }

    public boolean isEnableInnerCaps()
    {
        return this.enableInnerCaps;
    }

    public void setEnableInnerCaps(boolean draw)
    {
        this.enableInnerCaps = draw;
        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    public boolean isEnableCenterLine()
    {
        return this.enableCenterLine;
    }

    public void setEnableCenterLine(boolean enable)
    {
        this.enableCenterLine = enable;

        for (Box leg : this.legs)
        {
            leg.setEnableCenterLine(enable);
        }
    }

    public void setEnableDepthOffset(boolean enable)
    {
        super.setEnableDepthOffset(enable);
        this.setLegsOutOfDate(true);
    }

    /**
     * Desnotes the threshold that defines whether the angle between two adjacent legs is small. This threshold is used
     * to determine the best method for adjusting the vertices of adjacent legs.
     *
     * @return the angle used to determine when the angle between two adjacent legs is small.
     *
     * @see #setSmallAngleThreshold(gov.nasa.worldwind.geom.Angle)
     */
    public Angle getSmallAngleThreshold()
    {
        return smallAngleThreshold;
    }

    /**
     * Specifies the threshold that defines whether the angle between two adjacent legs is small. This threshold is used
     * to determine the best method for adjusting the vertices of adjacent legs.
     * <p>
     * When the angle between adjacent legs is small, the standard method of joining the leg's vertices forms a very
     * large peak pointing away from the leg's common point. In this case <code>TrackAirspace</code> uses a method that
     * avoids this peak and produces a seamless transition between the adjacent legs.
     *
     * @param angle the angle to use when determining when the angle between two adjacent legs is small.
     *
     * @throws IllegalArgumentException if <code>angle</code> is <code>null</code>.
     * @see #getSmallAngleThreshold()
     */
    public void setSmallAngleThreshold(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.smallAngleThreshold = angle;
    }

    public void setAltitudes(double lowerAltitude, double upperAltitude)
    {
        super.setAltitudes(lowerAltitude, upperAltitude);

        for (Box l : this.legs)
        {
            l.setAltitudes(lowerAltitude, upperAltitude);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    public void setTerrainConforming(boolean lowerTerrainConformant, boolean upperTerrainConformant)
    {
        super.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);

        for (Box l : this.legs)
        {
            l.setTerrainConforming(lowerTerrainConformant, upperTerrainConformant);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    @Override
    public void setAlwaysOnTop(boolean alwaysOnTop)
    {
        super.setAlwaysOnTop(alwaysOnTop);

        for (Box l : this.getLegs())
        {
            l.setAlwaysOnTop(alwaysOnTop);
        }
    }

    @Override
    public void setDrawSurfaceShape(boolean drawSurfaceShape)
    {
        super.setDrawSurfaceShape(drawSurfaceShape);

        for (Box l : this.getLegs())
        {
            l.setDrawSurfaceShape(drawSurfaceShape);
        }
    }

    public boolean isAirspaceVisible(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // If the parent TrackAirspace is not visible, then return false immediately without testing the child legs.
        if (!super.isAirspaceVisible(dc))
            return false;

        boolean visible = false;

        // The parent TrackAirspace is visible. Since the parent TrackAirspace's extent potentially contains volumes
        // where no child geometry exists, test that at least one of the child legs are visible.
        for (Box b : this.legs)
        {
            if (b.isAirspaceVisible(dc))
            {
                visible = true;
                break;
            }
        }

        return visible;
    }

    public Position getReferencePosition()
    {
        ArrayList<LatLon> locations = new ArrayList<LatLon>(2 * this.legs.size());
        for (Box box : this.legs)
        {
            LatLon[] ll = box.getLocations();
            locations.add(ll[0]);
            locations.add(ll[1]);
        }

        return this.computeReferencePosition(locations, this.getAltitudes());
    }

    @Override
    protected Extent computeExtent(DrawContext dc)
    {
        // Update the child leg vertices if they're out of date. Since the leg vertices are input to the parent
        // TrackAirspace's extent computation, they must be current before computing the parent's extent.
        if (this.isLegsOutOfDate())
        {
            this.doUpdateLegs();
        }

        return super.computeExtent(dc);
    }

    protected Extent computeExtent(Globe globe, double verticalExaggeration)
    {
        List<Box> trackLegs = this.getLegs();

        if (trackLegs == null || trackLegs.isEmpty())
        {
            return null;
        }
        else if (trackLegs.size() == 0)
        {
            return trackLegs.get(0).computeExtent(globe, verticalExaggeration);
        }
        else
        {
            ArrayList<gov.nasa.worldwind.geom.Box> extents = new ArrayList<gov.nasa.worldwind.geom.Box>();

            for (Box leg : trackLegs)
            {
                extents.add(leg.computeExtent(globe, verticalExaggeration));
            }

            return gov.nasa.worldwind.geom.Box.union(extents);
        }
    }

    @Override
    protected List<Vec4> computeMinimalGeometry(Globe globe, double verticalExaggeration)
    {
        return null; // Track is a geometry container, and therefore has no geometry itself.
    }

    @Override
    protected void invalidateAirspaceData()
    {
        super.invalidateAirspaceData();

        for (Box leg : this.legs)
        {
            leg.invalidateAirspaceData();
        }
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

        // Don't call super.moveTo(). Each box should move itself according to the properties it was constructed with.
        for (Box box : this.legs)
        {
            box.doMoveTo(globe, oldRef, newRef);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
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

        // Don't call super.moveTo(). Each box should move itself according to the properties it was constructed with.
        for (Box box : this.legs)
        {
            box.doMoveTo(oldRef, newRef);
        }

        this.invalidateAirspaceData();
        this.setLegsOutOfDate(true);
    }

    protected boolean isLegsOutOfDate()
    {
        return this.legsOutOfDate;
    }

    protected void setLegsOutOfDate(boolean tf)
    {
        this.legsOutOfDate = tf;
    }

    protected void doUpdateLegs()
    {
        // Assign the standard corner azimuths to each box and enable the starting and ending caps. We start by assuming
        // that each leg is independent, then join adjacent legs to give the appearance of a continuous track.
        for (Box leg : this.legs)
        {
            if (leg == null) // This should never happen, but we check anyway.
                continue;

            leg.setEnableCaps(true);
            leg.setEnableDepthOffset(this.isEnableDepthOffset());
            leg.setCornerAzimuths(null, null, null, null);
        }

        // If there's more than one leg, we potentially align the corner azimuths of adjacent legs to give the
        // appearance of a continuous track. This loop never executes if the list of legs has less than two elements.
        // Each iteration works on the adjacent vertices of the current leg and the next leg. Therefore this does not
        // modify the starting corner azimuths of the first leg, or the ending corner azimuths of the last leg.
        for (int i = 0; i < this.legs.size() - 1; i++)
        {
            Box leg = this.legs.get(i);
            Box nextLeg = this.legs.get(i + 1);

            if (leg == null || nextLeg == null) // This should never happen, but we check anyway.
                continue;

            // If the two legs have equivalent locations, altitude, and altitude mode where they meet, then adjust each
            // leg's corner azimuths so the two legs appear to make a continuous shape.
            if (this.mustJoinLegs(leg, nextLeg))
            {
                this.joinLegs(leg, nextLeg);
            }
        }

        this.setLegsOutOfDate(false);
    }

    /**
     * Specifies whether the legs must have their adjacent edges joined. <code>leg1</code> must precede
     * <code>leg2</code>. A track's legs must be joined when two adjacent legs share a common location. In this case,
     * the geometry of the two adjacent boxes contains a gap on one side and an intersection on the other. Joining the
     * legs modifies the edges of each leg at their common location to produce a seamless transition from the first leg
     * to the second.
     *
     * @param leg1 the first leg.
     * @param leg2 the second leg.
     *
     * @return <code>true</code> if the legs must be joined, otherwise <code>false</code>.
     */
    protected boolean mustJoinLegs(Box leg1, Box leg2)
    {
        return leg1.getLocations()[1].equals(leg2.getLocations()[0]) // leg1 end == leg2 begin
            && Arrays.equals(leg1.getAltitudes(), leg2.getAltitudes())
            && Arrays.equals(leg1.isTerrainConforming(), leg2.isTerrainConforming());
    }

    /**
     * Modifies the adjacent edges of the specified adjacent legs to produce a seamless transition from the first leg to
     * the second. <code>leg1</code> must precede <code>leg2</code>, and they must share a common location at the end of
     * <code>leg1</code> and the beginning of <code>leg2</code>. Without joining the adjacent edges, the geometry of the
     * two adjacent boxes contains a gap on one side and an intersection on the other.
     * <p>
     * This has no effect if the legs cannot be joined for any reason.
     *
     * @param leg1  the first leg.
     * @param leg2  the second leg.
     */
    protected void joinLegs(Box leg1, Box leg2)
    {
        LatLon[] locations1 = leg1.getLocations();
        LatLon[] locations2 = leg2.getLocations();
        Angle[] azimuths1 = leg1.getCornerAzimuths();
        Angle[] azimuths2 = leg2.getCornerAzimuths();

        Angle azimuth1 = LatLon.greatCircleAzimuth(locations1[1], locations1[0]);
        Angle azimuth2 = LatLon.greatCircleAzimuth(locations2[0], locations2[1]);
        Angle angularDistance = azimuth1.angularDistanceTo(azimuth2);
        Angle signedDistance = azimuth2.subtract(azimuth1).normalize();
        Angle shortAngle = Angle.mix(0.5, azimuth1, azimuth2);
        Angle longAngle = shortAngle.add(Angle.POS180).normalize();
        boolean isLeftTurn = signedDistance.compareTo(Angle.ZERO) > 0;

        if (angularDistance.compareTo(this.getSmallAngleThreshold()) > 0) // align both sides of the common edge
        {
            Angle leftAzimuth = isLeftTurn ? shortAngle : longAngle;
            Angle rightAzimuth = isLeftTurn ? longAngle : shortAngle;

            boolean widthsDifferent = !Arrays.equals(leg1.getWidths(), leg2.getWidths());
            leg1.setEnableEndCap(widthsDifferent || this.isEnableInnerCaps());
            leg2.setEnableStartCap(widthsDifferent || this.isEnableInnerCaps());
            leg1.setCornerAzimuths(azimuths1[0], azimuths1[1], leftAzimuth, rightAzimuth); // end of first leg
            leg2.setCornerAzimuths(leftAzimuth, rightAzimuth, azimuths2[2], azimuths2[3]); // begin of second leg
        }
        else if (isLeftTurn) // left turn; align only the left side
        {
            leg1.setEnableEndCap(true);
            leg2.setEnableStartCap(true);
            leg1.setCornerAzimuths(azimuths1[0], azimuths1[1], shortAngle, azimuths1[3]); // end left of first leg
            leg2.setCornerAzimuths(shortAngle, azimuths2[1], azimuths2[2], azimuths2[3]); // begin left of second leg
        }
        else // right turn; align only the right side
        {
            leg1.setEnableEndCap(true);
            leg2.setEnableStartCap(true);
            leg1.setCornerAzimuths(azimuths1[0], azimuths1[1], azimuths1[2], shortAngle); // end right of first leg
            leg2.setCornerAzimuths(azimuths2[0], shortAngle, azimuths2[2], azimuths2[3]); // begin right of second leg
        }
    }

    //**************************************************************//
    //********************  Geometry Rendering  ********************//
    //**************************************************************//

    @Override
    public void preRender(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        this.determineActiveAttributes(dc);

        // Update the child leg vertices if they're out of date. Since the leg vertices are used to determine how each
        // leg is shaped with respect to its neighbors, the vertices must be current before rendering each leg.
        if (this.isLegsOutOfDate())
        {
            this.doUpdateLegs();
        }

        for (Box leg : this.legs)
        {
            // Synchronize the leg's attributes with this track's attributes, and setup this track as the leg's pick
            // delegate.
            leg.setAttributes(this.getActiveAttributes());
            leg.setDelegateOwner(this.getDelegateOwner() != null ? this.getDelegateOwner() : this);
            leg.preRender(dc);
        }
    }

    @Override
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.isVisible())
            return;

        if (!this.isAirspaceVisible(dc))
            return;

        for (Box leg : this.legs)
        {
            leg.render(dc);
        }
    }

    @Override
    protected void doRenderGeometry(DrawContext dc, String drawStyle)
    {
        // Intentionally left blank.
    }

    //**************************************************************//
    //********************  END Geometry Rendering  ****************//
    //**************************************************************//

    @Override
    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsBoolean(context, "enableInnerCaps", this.isEnableInnerCaps());
        rs.addStateValueAsBoolean(context, "enableCenterLine", this.isEnableCenterLine());
        rs.addStateValueAsDouble(context, "smallAngleThresholdDegrees", this.getSmallAngleThreshold().degrees);

        RestorableSupport.StateObject so = rs.addStateObject(context, "legs");
        for (Box leg : this.legs)
        {
            RestorableSupport.StateObject lso = rs.addStateObject(so, "leg");
            leg.doGetRestorableState(rs, lso);
        }
    }

    @Override
    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        Boolean b = rs.getStateValueAsBoolean(context, "enableInnerCaps");
        if (b != null)
            this.setEnableInnerCaps(b);

        b = rs.getStateValueAsBoolean(context, "enableCenterLine");
        if (b != null)
            this.setEnableCenterLine(b);

        Double d = rs.getStateValueAsDouble(context, "smallAngleThresholdDegrees");
        if (d != null)
            this.setSmallAngleThreshold(Angle.fromDegrees(d));

        RestorableSupport.StateObject so = rs.getStateObject(context, "legs");
        if (so == null)
            return;

        RestorableSupport.StateObject[] lsos = rs.getAllStateObjects(so, "leg");
        if (lsos == null || lsos.length == 0)
            return;

        ArrayList<Box> legList = new ArrayList<Box>(lsos.length);

        for (RestorableSupport.StateObject lso : lsos)
        {
            if (lso != null)
            {
                Box leg = new Box();
                leg.doRestoreState(rs, lso);
                legList.add(leg);
            }
        }

        this.setLegs(legList);
    }
}
