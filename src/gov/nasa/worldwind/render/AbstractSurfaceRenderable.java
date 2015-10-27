/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.media.opengl.GL2;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Surface renderable.
 *
 * @author Patrick Murris
 * @version $Id: AbstractSurfaceRenderable.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public abstract class AbstractSurfaceRenderable extends AbstractSurfaceObject
{
    private double opacity = 1d;

    public double getOpacity()
    {
        return this.opacity;
    }

    public void setOpacity(double opacity)
    {
        this.opacity = opacity < 0 ? 0 : opacity > 1 ? 1 : opacity;  // clamp to 0..1
        this.updateModifiedTime();
    }

    // *** Utility methods

    protected Angle getViewHeading(DrawContext dc)
    {
        Angle heading = Angle.ZERO;
        if (dc.getView() instanceof OrbitView)
            heading = dc.getView().getHeading();
        return heading;
    }

    protected double computePixelSizeAtLocation(DrawContext dc, LatLon location)
    {
        Globe globe = dc.getGlobe();
        Vec4 locationPoint = globe.computePointFromPosition(location.getLatitude(), location.getLongitude(),
            globe.getElevation(location.getLatitude(), location.getLongitude()));
        double distance = dc.getView().getEyePoint().distanceTo3(locationPoint);
        return dc.getView().computePixelSizeAtDistance(distance);
    }

    protected double computeDrawPixelSize(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        return dc.getGlobe().getRadius() * sdc.getSector().getDeltaLatRadians() / sdc.getViewport().height;
    }

    protected Vec4 computeDrawPoint(LatLon location, SurfaceTileDrawContext sdc)
    {
        Vec4 point = new Vec4(location.getLongitude().degrees, location.getLatitude().degrees, 1);
        return point.transformBy4(sdc.getModelviewMatrix());
    }

    protected Sector computeRotatedSectorBounds(Sector sector, LatLon location, Angle heading)
    {
        if (Math.abs(heading.degrees) < .001)
            return sector;

        LatLon[] corners = new LatLon[] {
            new LatLon(sector.getMaxLatitude(), sector.getMinLongitude()),  // nw
            new LatLon(sector.getMaxLatitude(), sector.getMaxLongitude()),  // ne
            new LatLon(sector.getMinLatitude(), sector.getMinLongitude()),  // sw
            new LatLon(sector.getMinLatitude(), sector.getMaxLongitude()),  // se
        };
        // Rotate corners around location
        for (int i = 0; i < corners.length; i++)
        {
            Angle azimuth = LatLon.greatCircleAzimuth(location, corners[i]);
            Angle distance = LatLon.greatCircleDistance(location, corners[i]);
            corners[i] = LatLon.greatCircleEndPosition(location, azimuth.add(heading), distance);
        }

        return Sector.boundingSector(Arrays.asList(corners));
    }

    protected List<Sector> computeNormalizedSectors(Sector sector)
    {
        Angle minLat = sector.getMinLatitude();
        Angle maxLat = sector.getMaxLatitude();
        Angle minLon = sector.getMinLongitude();
        Angle maxLon = sector.getMaxLongitude();
        minLat = minLat.degrees >= -90 ? minLat : Angle.NEG90;
        maxLat = maxLat.degrees <= 90 ? maxLat : Angle.POS90;

        java.util.List<Sector> sectors = new ArrayList<Sector>();
        if (minLon.degrees >= -180 && maxLon.degrees <= 180)
        {
            // No date line crossing on both sides
            sectors.add(new Sector(minLat, maxLat, minLon, maxLon));
        }
        else
        {
            if (minLon.degrees < -180 && maxLon.degrees > 180)
            {
                // min and max lon overlap at the date line - span the whole ongitude range
                sectors.add(new Sector(minLat, maxLat, Angle.NEG180, Angle.POS180));
            }
            else
            {
                // Date line crossing, produce two sectors, one on each side of the date line
                while (minLon.degrees < -180)
                {
                    minLon = minLon.addDegrees(360);
                }
                while (maxLon.degrees > 180)
                {
                    maxLon = maxLon.subtractDegrees(360);
                }
                if (minLat.degrees > maxLat.degrees)
                {
                    sector = new Sector(minLat, maxLat, minLon, maxLon);
                    sectors.addAll(Arrays.asList(Sector.splitBoundingSectors(sector)));
                }
                else
                {
                    // min and max lon overlap - span the whole ongitude range
                    sectors.add(new Sector(minLat, maxLat, Angle.NEG180, Angle.POS180));
                }
            }
        }

        return sectors;
    }

    protected int computeHemisphereOffset(Sector sector, LatLon location)
    {
        Angle sectorLon = sector.getCentroid().getLongitude();
        Angle locationLon = location.getLongitude();
        if (Math.abs(locationLon.degrees - sectorLon.degrees) > 180
            && Math.signum(locationLon.degrees) != Math.signum(sectorLon.degrees))
        {
            return (int) (360 * Math.signum(sectorLon.degrees));
        }

        return 0;
    }

    protected void applyPremultipliedAlphaColor(GL2 gl, Color color, double opacity)
    {
        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        compArray[3] = (float) WWMath.clamp(opacity, 0, 1);
        compArray[0] *= compArray[3];
        compArray[1] *= compArray[3];
        compArray[2] *= compArray[3];
        gl.glColor4fv(compArray, 0);
    }

    protected void applyNonPremultipliedAlphaColor(GL2 gl, Color color, double opacity)
    {
        float[] compArray = new float[4];
        color.getRGBComponents(compArray);
        compArray[3] = (float) WWMath.clamp(opacity, 0, 1);
        gl.glColor4fv(compArray, 0);
    }
}
