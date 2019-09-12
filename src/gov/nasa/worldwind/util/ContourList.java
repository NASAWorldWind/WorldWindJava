/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.combine.*;

import com.jogamp.opengl.glu.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: ContourList.java 2405 2014-10-29 23:33:08Z dcollins $
 */
public class ContourList extends WWObjectImpl implements Combinable
{
    protected ArrayList<Iterable<? extends LatLon>> contours = new ArrayList<Iterable<? extends LatLon>>();
    protected Sector sector;

    public ContourList()
    {
    }

    public ContourList(ContourList that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.ContourListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.contours.addAll(that.contours);
        this.sector = that.sector;
    }

    public int getContourCount()
    {
        return this.contours.size();
    }

    public Iterable<? extends LatLon> getContour(int index)
    {
        if (index < 0 || index >= this.contours.size())
        {
            String msg = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.contours.get(index);
    }

    public void setContour(int index, Iterable<? extends LatLon> contour)
    {
        if (index < 0 || index >= this.contours.size())
        {
            String msg = Logging.getMessage("generic.indexOutOfRange", index);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (contour == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.contours.set(index, contour);
        this.computeSector();
    }

    public void addContour(Iterable<? extends LatLon> contour)
    {
        if (contour == null)
        {
            String msg = Logging.getMessage("nullValue.IterableIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.contours.add(contour);

        Sector contourSector = Sector.boundingSector(contour);
        this.sector = (this.sector != null ? this.sector.union(contourSector) : contourSector);
    }

    public void addAllContours(ContourList that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.ContourListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.contours.addAll(that.contours);
        this.sector = (this.sector != null ? this.sector.union(that.sector) : that.sector);
    }

    public void removeAllContours()
    {
        this.contours.clear();
        this.sector = null;
    }

    public Sector getSector()
    {
        return this.sector;
    }

    protected void computeSector()
    {
        this.sector = null;

        for (Iterable<? extends LatLon> contour : this.contours)
        {
            Sector contourSector = Sector.boundingSector(contour);
            this.sector = (this.sector != null ? this.sector.union(contourSector) : contourSector);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void combine(CombineContext cc)
    {
        if (cc == null)
        {
            String msg = Logging.getMessage("nullValue.CombineContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (cc.isBoundingSectorMode())
            this.combineBounds(cc);
        else
            this.combineContours(cc);
    }

    protected void combineBounds(CombineContext cc)
    {
        if (this.sector == null)
            return; // no contours

        cc.addBoundingSector(this.sector);
    }

    protected void combineContours(CombineContext cc)
    {
        if (this.sector == null)
            return; // no contours

        if (!cc.getSector().intersects(this.sector))
            return;  // this contour list does not intersect the region of interest

        this.doCombineContours(cc);
    }

    protected void doCombineContours(CombineContext cc)
    {
        GLUtessellator tess = cc.getTessellator();

        for (Iterable<? extends LatLon> contour : this.contours)
        {
            try
            {
                GLU.gluTessBeginContour(tess);

                for (LatLon location : contour)
                {
                    double[] vertex = {location.longitude.degrees, location.latitude.degrees, 0};
                    GLU.gluTessVertex(tess, vertex, 0, vertex);
                }
            }
            finally
            {
                GLU.gluTessEndContour(tess);
            }
        }
    }
}