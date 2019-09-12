/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.applications.glider;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;

import java.awt.image.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;

/**
 * Internal class to realize and control image and region drawing.
 *
 * @author tag
 * @version $Id: GliderImageLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GliderImageLayer extends AbstractLayer
{
    public static final String GLIDER_IMAGE = "gov.nasa.worldwind.glider.Image";

    protected GliderImage image;
    protected SurfaceImageLayer imageLayer = new SurfaceImageLayer();
    protected RenderableLayer regionLayer = new RenderableLayer();
    protected ImageListener imageListener = new ImageListener();

    public void dispose()
    {
        if (this.image != null)
            this.image.removePropertyChangeListener(this.imageListener);
        this.imageLayer.dispose();
        this.regionLayer.dispose();
    }

    public void setImage(GliderImage image) throws IOException
    {
        if (image.getImageSource() instanceof String)
            ((SurfaceImageLayer) this.getImageLayer()).addImage((String) image.getImageSource(), image.getCorners());

        else if (image.getImageSource() instanceof BufferedImage)
            ((SurfaceImageLayer) this.getImageLayer()).addImage(image.getName(), (BufferedImage) image.getImageSource(),
                image.getSector());
        else
            throw new IllegalArgumentException("Unsupported image source type");

        this.setOpacity(image.opacity);

        if (image != this.image)
        {
            image.addPropertyChangeListener(this.imageListener);
            if (this.image != null)
                this.image.removePropertyChangeListener(this.imageListener);
        }

        this.regionLayer.removeAllRenderables();
        if (image.getRegionsOfInterest() != null)
            this.regionLayer.addRenderables(makePolylines(image.getRegionsOfInterest(), image.getAltitude()));

        this.firePropertyChange(GliderImageLayer.GLIDER_IMAGE, this.image, this.image = image);
    }

    public String getName()
    {
        return this.image != null ? this.image.getName() : "Unnamed Layer";
    }

    protected Layer getImageLayer()
    {
        return this.imageLayer;
    }

    protected void doRender(DrawContext dc)
    {
        // dummy method; rendering is performed by image and region layers
    }

    @Override
    public void render(DrawContext dc)
    {
        if (!this.isEnabled())
            return;

        this.imageLayer.render(dc);
        this.regionLayer.render(dc);
    }

    @Override
    public void doPreRender(DrawContext dc)
    {
        this.imageLayer.preRender(dc);
        this.regionLayer.preRender(dc);
    }

    protected class ImageListener implements PropertyChangeListener
    {
        @SuppressWarnings({"StringEquality"})
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getSource() != GliderImageLayer.this.image && evt.getPropagationId() != GliderImageLayer.this.image)
                return;

            if (evt.getPropertyName() == GliderImage.GLIDER_IMAGE_SOURCE)
            {
                imageLayer.removeImage(((GliderImage) evt.getSource()).getName());

                if (evt.getNewValue() != null)
                {
                    try
                    {
                        GliderImageLayer.this.setImage((GliderImage) evt.getNewValue());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                // Cause owner to repaint
                evt.setPropagationId(GliderImageLayer.this);
                GliderImageLayer.this.firePropertyChange(evt);
            }
            else if (evt.getPropertyName() == GliderImage.GLIDER_IMAGE_OPACITY)
            {
                if (evt.getNewValue() == null)
                    return;

                GliderImageLayer.this.imageLayer.setOpacity(((GliderImage) evt.getNewValue()).getOpacity());

                // Cause owner to repaint
                evt.setPropagationId(GliderImageLayer.this);
                GliderImageLayer.this.firePropertyChange(evt);
            }
            else if (evt.getPropertyName() == GliderRegionOfInterest.GLIDER_REGION_OF_INTEREST
                || evt.getPropertyName() == GliderImage.GLIDER_REGIONS_OF_INTEREST)
            {
                GliderImageLayer.this.regionLayer.removeAllRenderables();

                GliderRegionOfInterest.RegionSet regions = (GliderRegionOfInterest.RegionSet) evt.getNewValue();
                if (regions != null)
                {
                    GliderImageLayer.this.regionLayer.addRenderables(makePolylines(regions,
                        GliderImageLayer.this.image.getAltitude()));
                }

                // Cause owner to repaint
                evt.setPropagationId(GliderImageLayer.this);
                GliderImageLayer.this.firePropertyChange(evt);
            }
        }
    }

    protected static List<Renderable> makePolylines(GliderRegionOfInterest.RegionSet regions, double altitude)
    {
        ArrayList<Renderable> polylines = new ArrayList<Renderable>(regions.regions.size());

        for (GliderRegionOfInterest region : regions.regions)
        {
            Polyline p = new Polyline(region.getLocations(), altitude);
            p.setClosed(true);
            p.setColor(region.getColor());
            p.setFollowTerrain(true);
            polylines.add(p);
        }

        return polylines;
    }
}
