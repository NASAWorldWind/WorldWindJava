/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwindx.applications.glider;

import gov.nasa.worldwind.WorldWind;
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
public class GliderImageLayer extends AbstractLayer {

    public static final String GLIDER_IMAGE = "gov.nasa.worldwind.glider.Image";

    protected GliderImage image;
    protected SurfaceImageLayer imageLayer = new SurfaceImageLayer();
    protected RenderableLayer regionLayer = new RenderableLayer();
    protected ImageListener imageListener = new ImageListener();

    @Override
    public void dispose() {
        if (this.image != null) {
            this.image.removePropertyChangeListener(this.imageListener);
        }
        this.imageLayer.dispose();
        this.regionLayer.dispose();
    }

    public void setImage(GliderImage image) throws IOException {
        if (image.getImageSource() instanceof String) {
            ((SurfaceImageLayer) this.getImageLayer()).addImage((String) image.getImageSource(), image.getCorners());
        } else if (image.getImageSource() instanceof BufferedImage) {
            ((SurfaceImageLayer) this.getImageLayer()).addImage(image.getName(), (BufferedImage) image.getImageSource(),
                    image.getSector());
        } else {
            throw new IllegalArgumentException("Unsupported image source type");
        }

        this.setOpacity(image.opacity);

        if (image != this.image) {
            image.addPropertyChangeListener(this.imageListener);
            if (this.image != null) {
                this.image.removePropertyChangeListener(this.imageListener);
            }
        }

        this.regionLayer.removeAllRenderables();
        if (image.getRegionsOfInterest() != null) {
            this.regionLayer.addRenderables(makePaths(image.getRegionsOfInterest(), image.getAltitude()));
        }

        this.firePropertyChange(GliderImageLayer.GLIDER_IMAGE, this.image, this.image = image);
    }

    @Override
    public String getName() {
        return this.image != null ? this.image.getName() : "Unnamed Layer";
    }

    protected Layer getImageLayer() {
        return this.imageLayer;
    }

    @Override
    protected void doRender(DrawContext dc) {
        // dummy method; rendering is performed by image and region layers
    }

    @Override
    public void render(DrawContext dc) {
        if (!this.isEnabled()) {
            return;
        }

        this.imageLayer.render(dc);
        this.regionLayer.render(dc);
    }

    @Override
    public void doPreRender(DrawContext dc) {
        this.imageLayer.preRender(dc);
        this.regionLayer.preRender(dc);
    }

    protected class ImageListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() != GliderImageLayer.this.image && evt.getPropagationId() != GliderImageLayer.this.image) {
                return;
            }

            if (null != evt.getPropertyName()) {
                switch (evt.getPropertyName()) {
                    case GliderImage.GLIDER_IMAGE_SOURCE:
                        imageLayer.removeImage(((GliderImage) evt.getSource()).getName());
                        if (evt.getNewValue() != null) {
                            try {
                                GliderImageLayer.this.setImage((GliderImage) evt.getNewValue());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }   // Cause owner to repaint
                        evt.setPropagationId(GliderImageLayer.this);
                        GliderImageLayer.this.firePropertyChange(evt);
                        break;
                    case GliderImage.GLIDER_IMAGE_OPACITY:
                        if (evt.getNewValue() == null) {
                            return;
                        }
                        GliderImageLayer.this.imageLayer.setOpacity(((GliderImage) evt.getNewValue()).getOpacity());
                        // Cause owner to repaint
                        evt.setPropagationId(GliderImageLayer.this);
                        GliderImageLayer.this.firePropertyChange(evt);
                        break;
                    case GliderRegionOfInterest.GLIDER_REGION_OF_INTEREST:
                    case GliderImage.GLIDER_REGIONS_OF_INTEREST:
                        GliderImageLayer.this.regionLayer.removeAllRenderables();
                        GliderRegionOfInterest.RegionSet regions = (GliderRegionOfInterest.RegionSet) evt.getNewValue();
                        if (regions != null) {
                            GliderImageLayer.this.regionLayer.addRenderables(makePaths(regions,
                                    GliderImageLayer.this.image.getAltitude()));
                        }   // Cause owner to repaint
                        evt.setPropagationId(GliderImageLayer.this);
                        GliderImageLayer.this.firePropertyChange(evt);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    protected static List<Renderable> makePaths(GliderRegionOfInterest.RegionSet regions, double altitude) {
        ArrayList<Renderable> paths = new ArrayList<>(regions.regions.size());

        for (GliderRegionOfInterest region : regions.regions) {
            Path p = new Path(region.getLocations(), altitude);
            var attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(region.getColor()));
            p.setAttributes(attrs);
            p.setSurfacePath(true);
            p.makeClosed();
            paths.add(p);
        }

        return paths;
    }
}
