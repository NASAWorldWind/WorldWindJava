/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.ogc.kml.gx.GXLatLongQuad;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * @author pabercrombie
 * @version $Id: KMLSurfaceImageImpl.java 1551 2013-08-17 18:00:09Z pabercrombie $
 */
public class KMLSurfaceImageImpl extends SurfaceImage implements KMLRenderable
{
    protected KMLGroundOverlay parent;

    protected boolean attributesResolved;

    /** Indicates that the source texture has been resolved and loaded. */
    protected boolean textureResolved;
    /** Indicates the time at which the image source was specified. */
    protected long iconRetrievalTime;

    /**
     * Flag to indicate the rotation must be applied to the SurfaceImage. Rotation is applied the first time that the
     * image is rendered.
     */
    protected boolean mustApplyRotation;

    /**
     * Create an screen image.
     *
     * @param tc      the current {@link KMLTraversalContext}.
     * @param overlay the <i>Overlay</i> element containing.
     *
     * @throws NullPointerException     if the traversal context is null.
     * @throws IllegalArgumentException if the parent overlay or the traversal context is null.
     */
    public KMLSurfaceImageImpl(KMLTraversalContext tc, KMLGroundOverlay overlay)
    {
        this.parent = overlay;

        if (tc == null)
        {
            String msg = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (overlay == null)
        {
            String msg = Logging.getMessage("nullValue.ParentIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Positions are specified either as a kml:LatLonBox or a gx:LatLonQuad
        KMLLatLonBox box = overlay.getLatLonBox();
        if (box != null)
        {
            Sector sector = KMLUtil.createSectorFromLatLonBox(box);
            this.initializeGeometry(sector);

            // Check to see if a rotation is provided. The rotation will be applied when the image is rendered, because
            // how the rotation is performed depends on the globe.
            Double rotation = box.getRotation();
            if (rotation != null)
            {
                this.mustApplyRotation = true;
            }
        }
        else
        {
            GXLatLongQuad latLonQuad = overlay.getLatLonQuad();
            if (latLonQuad != null && latLonQuad.getCoordinates() != null)
            {
                this.initializeGeometry(latLonQuad.getCoordinates().list);
            }
        }

        // Apply opacity to the surface image
        String colorStr = overlay.getColor();
        if (!WWUtil.isEmpty(colorStr))
        {
            Color color = WWUtil.decodeColorABGR(colorStr);
            int alpha = color.getAlpha();

            this.setOpacity((double) alpha / 255);
        }

        this.setPickEnabled(false);
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.mustResolveHref()) // resolve the href to either a local file or a remote URL
        {
            String path = this.resolveHref();

            // Evict the resource from the file store if there is a cached resource older than the icon update time.
            // This prevents fetching a stale resource out of the cache when the Icon is updated.
            this.parent.getRoot().evictIfExpired(path, this.iconRetrievalTime);

            this.setImageSource(path, this.getCorners());
            this.iconRetrievalTime = System.currentTimeMillis();
            this.textureResolved = false;
        }

        // Set the Icon's expiration time the first time that the image is rendered after the texture has been retrieved.
        // The expiration time comes from the HTTP headers, so we can't do this until the resource is available.
        boolean mustSetExpiration = !this.textureResolved && this.sourceTexture != null
            && this.sourceTexture.isTextureCurrent(dc);
        if (mustSetExpiration)
        {
            String path = this.resolveHref();

            // Query the KMLRoot for the expiration time.
            long expiration = this.parent.getRoot().getExpiration(path);

            // Set the Icon's expiration. This has no effect if the refreshMode is not onExpire.
            this.parent.getIcon().setExpirationTime(expiration);
            this.textureResolved = true;
        }

        // Apply rotation the first time the overlay is rendered
        if (this.mustApplyRotation)
        {
            this.applyRotation(dc);
            this.mustApplyRotation = false;
        }

        super.preRender(dc);
    }

    /**
     * Indicates whether or not the image source needs to be resolved. The source needs to be resolved when the KMLIcon
     * is updated.
     *
     * @return True if the image source must be resolved.
     */
    protected boolean mustResolveHref()
    {
        KMLIcon icon = this.parent.getIcon();
        //noinspection SimplifiableIfStatement
        if (icon == null || icon.getHref() == null)
            return false;

        // Resolve the reference if the image hasn't been retrieved, or if the link has expired.
        return this.getImageSource() == null || icon.getUpdateTime() > this.iconRetrievalTime;
    }

    /**
     * Resolve the HREF in this overlay's Icon element against the KML root.
     *
     * @return The resolved path to the image source.
     */
    protected String resolveHref()
    {
        // The icon reference may be to a support file within a KMZ file, so check for that. If it's not, then just
        // let the normal SurfaceImage code resolve the reference.
        String href = this.parent.getIcon().getHref();
        String localAddress = null;
        try
        {
            localAddress = this.parent.getRoot().getSupportFilePath(href);
        }
        catch (IOException ignored)
        {
        }

        return localAddress != null ? localAddress : href;
    }

    /** {@inheritDoc} */
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        // We've already resolved the SurfaceImage's attributes during the preRender pass. During the render pass we
        // simply draw the SurfaceImage.
        super.render(dc);
    }

    /**
     * Apply a rotation to the corner points of the overlay.
     *
     * @param dc Current draw context.
     */
    protected void applyRotation(DrawContext dc)
    {
        KMLLatLonBox box = this.parent.getLatLonBox();
        if (box != null)
        {
            Double rotation = box.getRotation();
            if (rotation != null)
            {
                List<LatLon> corners = KMLUtil.rotateSector(dc.getGlobe(), this.getSector(),
                    Angle.fromDegrees(rotation));
                this.setCorners(corners);
            }
        }
    }
}
