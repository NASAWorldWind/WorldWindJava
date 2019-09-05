/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.io.IOException;

/**
 * Implements at KML <i>ScreenOverlay</i> element.
 *
 * @author pabercrombie
 * @version $Id: KMLScreenImageImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLScreenImageImpl extends ScreenImage implements KMLRenderable
{
    /** Size value that KML uses to indicate that the native image dimension should be maintained. */
    protected static final int KML_NATIVE_DIMENSION = -1;

    /** Size value that KML uses to indicate that the image aspect ration should be maintained. */
    protected static final int KML_MAINTAIN_ASPECT_RATIO = 0;

    protected final KMLScreenOverlay parent;

    /** Indicates the time at which the image source was specified. */
    protected long iconRetrievalTime;

    /**
     * Create an screen image.
     *
     * @param tc      the current {@link KMLTraversalContext}.
     * @param overlay the <i>Overlay</i> element containing.
     *
     * @throws NullPointerException     if the traversal context is null.
     * @throws IllegalArgumentException if the parent overlay or the traversal context is null.
     */
    public KMLScreenImageImpl(KMLTraversalContext tc, KMLScreenOverlay overlay)
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

        KMLVec2 xy = this.parent.getScreenXY();
        if (xy != null)
        {
            this.screenOffset = new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(xy.getYunits()));
        }

        xy = this.parent.getOverlayXY();
        if (xy != null)
        {
            this.imageOffset = new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(xy.getYunits()));
        }

        this.setRotation(overlay.getRotation());

        xy = this.parent.getRotationXY();
        if (xy != null)
        {
            setRotationOffset(new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(xy.getYunits())));
        }

        String colorStr = overlay.getColor();
        if (colorStr != null)
        {
            Color color = WWUtil.decodeColorABGR(colorStr);
            this.setColor(color);
        }

        // Compute desired image size, and the scale factor that will make it that size
        KMLVec2 kmlSize = this.parent.getSize();
        if (kmlSize != null)
        {
            Size size = new Size();
            size.setWidth(getSizeMode(kmlSize.getX()), kmlSize.getX(), KMLUtil.kmlUnitsToWWUnits(kmlSize.getXunits()));
            size.setHeight(getSizeMode(kmlSize.getY()), kmlSize.getY(), KMLUtil.kmlUnitsToWWUnits(kmlSize.getYunits()));
            this.setSize(size);
        }
    }

    /** {@inheritDoc} */
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        // No pre-rendering
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
        // let the normal ScreenImage code resolve the reference.
        String href = this.parent.getIcon().getHref();
        String localAddress = null;
        try
        {
            localAddress = this.parent.getRoot().getSupportFilePath(href);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", href);
            Logging.logger().warning(message);
        }

        return localAddress != null ? localAddress : href;
    }

    /** {@inheritDoc} */
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.mustResolveHref()) // resolve the href to either a local file or a remote URL
        {
            String path = this.resolveHref();

            // Evict the resource from the file store if there is a cached resource older than the icon update time.
            // This prevents fetching a stale resource out of the cache when the Icon is updated.
            this.parent.getRoot().evictIfExpired(path, this.iconRetrievalTime);

            this.setImageSource(path);
        }

        this.render(dc);
    }

    /**
     * {@inheritDoc} Overridden to set the link expiration time based on HTTP headers after the image has been
     * retrieved.
     */
    protected BasicWWTexture initializeTexture()
    {
        BasicWWTexture ret = super.initializeTexture();
        if (this.texture != null)
        {
            this.iconRetrievalTime = System.currentTimeMillis();

            String path = this.resolveHref();

            // Query the KMLRoot for the expiration time.
            long expiration = this.parent.getRoot().getExpiration(path);

            // Set the Icon's expiration. This has no effect if the refreshMode is not onExpire.
            this.parent.getIcon().setExpirationTime(expiration);
        }
        return ret;
    }

    /**
     * Get the size mode for a size parameter. The KML size tag takes a numeric size attribute, but certain values of
     * this attribute change the interpretation of the tag.
     * <ul><li> A value of -1 indicates to use the native dimension.</li> <li> A value of 0 indicates to maintain the
     * aspect ratio.</li> <li> A value of n sets the value of the dimension.</li></ul>
     *
     * @param size The KML size attribute
     *
     * @return One of {@link gov.nasa.worldwind.render.Size#NATIVE_DIMENSION}, {@link gov.nasa.worldwind.render.Size#MAINTAIN_ASPECT_RATIO},
     *         or {@link gov.nasa.worldwind.render.Size#EXPLICIT_DIMENSION}.
     */
    protected String getSizeMode(Double size)
    {
        // KML spec requires a value, but if there isn't one, use the image's native size.
        if (size == null)
            return Size.NATIVE_DIMENSION;

        int s = (int) size.doubleValue();

        if (s == KML_NATIVE_DIMENSION)
            return Size.NATIVE_DIMENSION;
        else if (size == KML_MAINTAIN_ASPECT_RATIO)
            return Size.MAINTAIN_ASPECT_RATIO;
        else
            return Size.EXPLICIT_DIMENSION;
    }
}
