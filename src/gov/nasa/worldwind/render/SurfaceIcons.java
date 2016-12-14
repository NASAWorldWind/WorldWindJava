/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

import com.jogamp.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.SurfaceTileDrawContext;

import com.jogamp.opengl.*;
import java.awt.geom.*;
import java.util.*;

/**
 * Renders an icon image over the terrain surface in many locations.
 *
 * @author Patrick Murris
 * @version $Id: SurfaceIcons.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class SurfaceIcons extends SurfaceIcon
{
    private Iterable<? extends LatLon> locations;

    public SurfaceIcons(Object imageSource, Iterable<? extends LatLon> locations)
    {
        super(imageSource);
        this.setLocations(locations);
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return this.locations;
    }

    public void setLocations(Iterable<? extends LatLon> newLocations)
    {
        this.locations = newLocations;
        this.onPropertyChanged();
    }

    protected List<Sector> computeSectors(DrawContext dc)
    {
        if (this.locations == null || !this.locations.iterator().hasNext())
            return null;

        // Compute all locations bounding sector, then add some padding for the icon half diagonal extent
        Sector sector = Sector.boundingSector(this.locations);
        // Compute padding
        double minCosLat = Math.min(sector.getMinLatitude().cos(), sector.getMaxLatitude().cos());
        minCosLat = Math.max(minCosLat, .01); // avoids division by zero at the poles
        Rectangle2D iconDimension = this.computeDrawDimension(dc, sector.getCentroid());
        double diagonalLength = Math.sqrt(iconDimension.getWidth() * iconDimension.getWidth()
            + iconDimension.getHeight() * iconDimension.getHeight());
        double padLatRadians = diagonalLength / 2 / dc.getGlobe().getRadius();
        double padLonRadians = diagonalLength / 2 / dc.getGlobe().getRadius() / minCosLat;
        // Apply padding to sector
        Angle minLat = sector.getMinLatitude().subtractRadians(padLatRadians);
        Angle maxLat = sector.getMaxLatitude().addRadians(padLatRadians);
        Angle minLon = sector.getMinLongitude().subtractRadians(padLonRadians);
        Angle maxLon = sector.getMaxLongitude().addRadians(padLatRadians);

        return this.computeNormalizedSectors(new Sector(minLat, maxLat, minLon, maxLon));
    }

    protected void drawIcon(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        if (this.locations == null)
            return;

        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        double drawScale = 1;
        TextureCoords textureCoords = new TextureCoords(0, 0, 1, 1);

        // Compute draw scale only once if not maintaining strict appearance
        if (!this.isMaintainAppearance())
            drawScale = this.computeDrawScale(dc, sdc, null);
        // Determine which locations are to be drawn
        Iterable<? extends LatLon> drawLocations = this.computeDrawLocations(dc, sdc);
        // Draw icons
        for (LatLon location : drawLocations)
        {
            gl.glPushMatrix();

            if (this.isMaintainAppearance())
                drawScale = this.computeDrawScale(dc, sdc, location);
            this.applyDrawTransform(dc, sdc, location, drawScale);
            gl.glScaled(this.imageWidth, this.imageHeight, 1d);
            dc.drawUnitQuad(textureCoords);

            gl.glPopMatrix();
        }
    }

    protected Iterable<? extends LatLon> computeDrawLocations(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        ArrayList<LatLon> drawList = new ArrayList<LatLon>();
        double safeDistanceDegreesSquared = Math.pow(this.computeSafeRadius(dc, sdc).degrees, 2);
        for (LatLon location : this.getLocations())
        {
            if (this.computeLocationDistanceDegreesSquared(sdc.getSector(), location) <= safeDistanceDegreesSquared)
                drawList.add(location);
        }
        return drawList;
    }

    protected Angle computeSafeRadius(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        double regionPixelSize = this.computeDrawPixelSize(dc, sdc);
        Angle sectorRadius = this.computeSectorRadius(sdc.getSector());
        Angle iconRadius = this.computeIconRadius(dc, regionPixelSize, sdc.getSector());
        return sectorRadius.add(iconRadius);
    }

    protected Angle computeSectorRadius(Sector sector)
    {
        double dLat = sector.getDeltaLatRadians();
        double dLon = sector.getDeltaLonRadians();
        return Angle.fromRadians(Math.sqrt(dLat * dLat + dLon * dLon) / 2);
    }

    protected Angle computeIconRadius(DrawContext dc, double regionPixelSize, Sector drawSector)
    {
        double minCosLat = Math.min(drawSector.getMinLatitude().cos(), drawSector.getMaxLatitude().cos());
        if (minCosLat < 0.001)
            return Angle.POS180;

        Rectangle2D iconDimension = this.computeDrawDimension(regionPixelSize); // Meter
        double dLat = iconDimension.getHeight() / dc.getGlobe().getRadius();
        double dLon = iconDimension.getWidth() / dc.getGlobe().getRadius() / minCosLat;
        return Angle.fromRadians(Math.sqrt(dLat * dLat + dLon * dLon) / 2);
    }

    protected double computeLocationDistanceDegreesSquared(Sector drawSector, LatLon location)
    {
        double lonOffset = computeHemisphereOffset(drawSector, location);
        double dLat = location.getLatitude().degrees - drawSector.getCentroid().getLatitude().degrees;
        double dLon = location.getLongitude().degrees - drawSector.getCentroid().getLongitude().degrees + lonOffset;
        return dLat * dLat + dLon * dLon;
    }
}
