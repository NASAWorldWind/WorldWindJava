/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Renders a contour line on the terrain at a given elevation. The contour line extent can be bounded by a
 * <code>Sector</code>.
 *
 * @author Patrick Murris
 * @version $Id: ContourLine.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ContourLine implements Renderable
{
    private double elevation;
    private Sector sector;
    private Color color = Color.CYAN;
    private double lineWidth = 1;
    private boolean enabled = true;
    private ArrayList<Renderable> renderables = new ArrayList<Renderable>();
    private boolean viewClippingEnabled = false;
    protected Object globeStateKey;

    // Geometry update support.
    TimedExpirySupport expirySupport = new TimedExpirySupport(1000, 2000);

    // Segments connection criteria
    protected int maxConnectingDistance = 10; // meters

    public ContourLine()
    {
        this(0, Sector.FULL_SPHERE);
    }

    public ContourLine(double elevation)
    {
        this(elevation, Sector.FULL_SPHERE);
    }

    @SuppressWarnings( {"UnusedDeclaration"})
    public ContourLine(Sector sector)
    {
        this(0, sector);
    }

    public ContourLine(double elevation, Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.elevation = elevation;
        this.sector = sector;
    }

    /**
     * Get the contour line current elevation.
     *
     * @return the contour line current elevation.
     */
    public double getElevation()
    {
        return this.elevation;
    }

    /**
     * Set the contour line elevation.
     *
     * @param elevation the contour line elevation.
     */
    public void setElevation(double elevation)
    {
        if (this.elevation != elevation)
        {
            this.elevation = elevation;
            this.update();
        }
    }

    /**
     * Get the contour line current bounding sector.
     *
     * @return the contour line current bounding sector.
     */
    public Sector getSector()
    {
        return this.sector;
    }

    /**
     * Set the contour line bounding sector.
     *
     * @param sector the contour line bounding sector.
     */
    public void setSector(Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.sector.equals(sector))
        {
            this.sector = sector;
            this.update();
        }
    }

    /**
     * Get the contour line color.
     *
     * @return the contour line color.
     */
    public Color getColor()
    {
        return this.color;
    }

    /**
     * Set the contour line color.
     *
     * @param color the contour line color.
     */
    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.color.equals(color))
        {
            this.color = color;
            for (Renderable r : this.getRenderables())
            {
                if (r instanceof Polyline)
                    ((Polyline) r).setColor(color);
            }
        }
    }

    /**
     * Get the contour line width.
     *
     * @return the contour line width.
     */
    public double getLineWidth()
    {
        return this.lineWidth;
    }

    /**
     * Set the contour line width.
     *
     * @param width the contour line width.
     */
    public void setLineWidth(double width)
    {
        if (this.lineWidth != width)
        {
            this.lineWidth = width;
            for (Renderable r : this.getRenderables())
            {
                if (r instanceof Polyline)
                    ((Polyline) r).setLineWidth(width);
            }
        }
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean state)
    {
        this.enabled = state;
    }

    /**
     * Indicates whether view volume clipping is performed.
     *
     * @return <code>true</code> if view volume clipping is performed, otherwise <code>false</code> (the default).
     */
    public boolean isViewClippingEnabled()
    {
        return viewClippingEnabled;
    }

    /**
     * Set whether view volume clipping is performed.
     *
     * @param viewClippingEnabled <code>true</code> if view clipping should be performed, otherwise <code>false</code>
     *                            (the default).
     */
    @SuppressWarnings( {"UnusedDeclaration"})
    public void setViewClippingEnabled(boolean viewClippingEnabled)
    {
        this.viewClippingEnabled = viewClippingEnabled;
    }

    /** Update the contour line according to the current terrain geometry. */
    public void update()
    {
        this.expirySupport.setExpired(true);
    }

    public List<Renderable> getRenderables()
    {
        return this.renderables;
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.isEnabled())
            return;

        if (!this.getSector().intersects(dc.getVisibleSector()))
            return;

        if (!this.isValid(dc))
        {
            makeContourLine(dc);
            this.expirySupport.restart(dc);
            this.globeStateKey = dc.getGlobe().getGlobeStateKey(dc);
        }

        for (Renderable r : this.getRenderables())
        {
            r.render(dc);
        }
    }

    protected boolean isValid(DrawContext dc)
    {
        if (this.expirySupport.isExpired(dc))
            return false;

        return this.globeStateKey != null && this.globeStateKey.equals(dc.getGlobe().getStateKey(dc));
    }

    /**
     * Update the renderable list with appropriate renderables to display the contour line.
     *
     * @param dc the current <code>DrawContext</code>.
     */
    protected void makeContourLine(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.getRenderables().clear();

        // Get intersection points with terrain
        double ve = dc.getVerticalExaggeration();
        Intersection[] interArray = dc.getSurfaceGeometry().intersect(this.getElevation() * ve, this.getSector());

        if (interArray != null)
        {
            ArrayList<Intersection> inter = new ArrayList<Intersection>(
                Arrays.asList(interArray));

            // Filter intersection segment list
            if (isViewClippingEnabled())
                inter = filterIntersectionsOnViewFrustum(dc, inter);
            inter = filterIntersections(dc, inter);

            // Create polyline segments
            makePolylinesConnected(dc, inter, this.maxConnectingDistance);
        }
    }

    /**
     * Filters the given intersection segments list according to the current view frustum.
     *
     * @param dc   the current <code>DrawContext</code>
     * @param list the list of <code>Intersection</code> to be filtered.
     *
     * @return the filtered list.
     */
    protected ArrayList<Intersection> filterIntersectionsOnViewFrustum(DrawContext dc, ArrayList<Intersection> list)
    {
        Frustum vf = dc.getView().getFrustumInModelCoordinates();
        int i = 0;
        while (i < list.size())
        {
            if (vf.contains(list.get(i).getIntersectionPoint())
                || vf.contains(list.get(i + 1).getIntersectionPoint()))
                // Keep segment
                i += 2;
            else
            {
                // Remove segment
                list.remove(i);
                list.remove(i);
            }
        }
        return list;
    }

    /**
     * Filters the given intersection segments list according to some criteria - here the inclusion inside the bounding
     * sector.
     *
     * @param dc   the current <code>DrawContext</code>
     * @param list the list of <code>Intersection</code> to be filtered.
     *
     * @return the filtered list.
     */
    protected ArrayList<Intersection> filterIntersections(DrawContext dc, ArrayList<Intersection> list)
    {
        if (getSector().equals(Sector.FULL_SPHERE))
            return list;

        Globe globe = dc.getGlobe();
        Sector s = getSector();
        int i = 0;
        while (i < list.size())
        {
            if (s.contains(globe.computePositionFromPoint(list.get(i).getIntersectionPoint()))
                && s.contains(globe.computePositionFromPoint(list.get(i + 1).getIntersectionPoint())))
                // Keep segment
                i += 2;
            else
            {
                // Remove segment
                list.remove(i);
                list.remove(i);
            }
        }
        return list;
    }

    /**
     * Add a set of <code>Polyline</code> objects to the contour line renderable list by connecting as much as possible
     * the segments from the given <code>Intersection</code> array.
     *
     * @param dc        the current <code>DrawContext</code>.
     * @param inter     the list of <code>Intersection</code> to sort out.
     * @param tolerance how far in meter can two points be considered connected.
     *
     * @return the number of <code>Polyline</code> objects added.
     */
    protected int makePolylinesConnected(DrawContext dc, ArrayList<Intersection> inter, int tolerance)
    {
        if (inter == null)
            return 0;

        Globe globe = dc.getGlobe();
        Vec4 start, end, p;
        Polyline line;
        int tolerance2 = tolerance * tolerance; // distance squared in meters
        int count = 0;
        while (inter.size() > 0)
        {
            ArrayList<Position> positions = new ArrayList<Position>();
            // Start with first segment
            start = inter.remove(0).getIntersectionPoint();
            end = inter.remove(0).getIntersectionPoint();
            positions.add(globe.computePositionFromPoint(start));
            positions.add(globe.computePositionFromPoint(end));
            // Try to connect remaining segments
            for (int i = 0; i < inter.size();)
            {
                // Try segment start point
                p = inter.get(i).getIntersectionPoint();
                if (p.distanceToSquared3(start) < tolerance2)
                {
                    // Connect segment to start
                    inter.remove(i);
                    start = inter.remove(i).getIntersectionPoint();
                    positions.add(0, globe.computePositionFromPoint(start));
                    i = 0;
                    continue;
                }
                if (p.distanceToSquared3(end) < tolerance2)
                {
                    // Connect segment to end
                    inter.remove(i);
                    end = inter.remove(i).getIntersectionPoint();
                    positions.add(globe.computePositionFromPoint(end));
                    i = 0;
                    continue;
                }
                // Try segment end point
                p = inter.get(i + 1).getIntersectionPoint();
                if (p.distanceToSquared3(start) < tolerance2)
                {
                    // Connect segment to start
                    inter.remove(i + 1);
                    start = inter.remove(i).getIntersectionPoint();
                    positions.add(0, globe.computePositionFromPoint(start));
                    i = 0;
                    continue;
                }
                if (p.distanceToSquared3(end) < tolerance2)
                {
                    // Connect segment to end
                    inter.remove(i + 1);
                    end = inter.remove(i).getIntersectionPoint();
                    positions.add(globe.computePositionFromPoint(end));
                    i = 0;
                    continue;
                }
                // Next segment
                i += 2;
            }
            // Create polyline
            line = new Polyline(positions);
            line.setNumSubsegments(0);
            line.setFollowTerrain(true);
            line.setColor(this.getColor());
            line.setLineWidth(this.getLineWidth());
            this.getRenderables().add(line);
            count++;
        }
        return count;
    }
}
