/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.io.IOException;

/**
 * Implements the <i>Point</i> case of a KML <i>Placemark</i> element.
 *
 * @author tag
 * @version $Id: KMLPointPlacemarkImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLPointPlacemarkImpl extends PointPlacemark implements KMLRenderable
{
    protected final KMLPlacemark parent;
    protected boolean highlightAttributesResolved = false;
    protected boolean normalAttributesResolved = false;

    /** Indicates the time at which the image source was specified. */
    protected long iconRetrievalTime;
    /** Indicates the time at which the highlight image source was specified. */
    protected long highlightIconRetrievalTime;

    public static final double DEFAULT_LABEL_SCALE_THRESHOLD = 1.0;
    /**
     * Placemark labels with a scale less than this threshold will only be drawn when the placemark is highlighted. This
     * logic supports KML files with many placemarks with small labels, and drawing all the labels would be too
     * cluttered.
     */
    protected double labelScaleThreshold = DEFAULT_LABEL_SCALE_THRESHOLD;

    /**
     * Create an instance.
     *
     * @param tc        the current {@link KMLTraversalContext}.
     * @param placemark the <i>Placemark</i> element containing the <i>Point</i>.
     * @param geom      the {@link KMLPoint} geometry.
     *
     * @throws NullPointerException     if the geometry is null.
     * @throws IllegalArgumentException if the parent placemark or the traversal context is null.
     */
    public KMLPointPlacemarkImpl(KMLTraversalContext tc, KMLPlacemark placemark, KMLAbstractGeometry geom)
    {
        super(((KMLPoint) geom).getCoordinates());

        if (tc == null)
        {
            String msg = Logging.getMessage("nullValue.TraversalContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (placemark == null)
        {
            String msg = Logging.getMessage("nullValue.ParentIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.parent = placemark;

        KMLPoint point = (KMLPoint) geom;

        this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // KML default

        if (point.isExtrude())
            this.setLineEnabled(true);

        String altMode = point.getAltitudeMode();
        if (!WWUtil.isEmpty(altMode))
        {
            if ("clampToGround".equals(altMode))
                this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            else if ("relativeToGround".equals(altMode))
                this.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            else if ("absolute".equals(altMode))
                this.setAltitudeMode(WorldWind.ABSOLUTE);
        }

        if (this.parent.getVisibility() != null)
            this.setVisible(this.parent.getVisibility());

        if (placemark.getName() != null)
        {
            this.setLabelText(placemark.getName());
            this.setValue(AVKey.DISPLAY_NAME, placemark.getName());
        }

        String description = placemark.getDescription();
        if (description != null)
            this.setValue(AVKey.DESCRIPTION, description);

        if (placemark.getSnippetText() != null)
            this.setValue(AVKey.SHORT_DESCRIPTION, placemark.getSnippetText());

        this.setValue(AVKey.CONTEXT, this.parent);
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        // Intentionally left blank; KML point placemark does nothing during the preRender phase.
    }

    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        // If the attributes are not inline or internal then they might not be resolved until the external KML
        // document is resolved. Therefore check to see if resolution has occurred.

        if (this.isHighlighted())
        {
            if (!this.highlightAttributesResolved)
            {
                PointPlacemarkAttributes a = this.getHighlightAttributes();
                if (a == null || a.isUnresolved())
                {
                    a = this.makeAttributesCurrent(KMLConstants.HIGHLIGHT);
                    if (a != null)
                    {
                        this.setHighlightAttributes(a);
                        if (!a.isUnresolved())
                            this.highlightAttributesResolved = true;
                    }
                    else
                    {
                        // There are no highlight attributes, so we can stop looking for them. Note that this is
                        // different from having unresolved highlight attributes (handled above).
                        this.highlightAttributesResolved = true;
                    }
                }
            }
        }
        else
        {
            if (!this.normalAttributesResolved)
            {
                PointPlacemarkAttributes a = this.getAttributes();
                if (a == null || a.isUnresolved())
                {
                    a = this.makeAttributesCurrent(KMLConstants.NORMAL);
                    if (a != null)
                    {
                        this.setAttributes(a);
                        if (!a.isUnresolved())
                            this.normalAttributesResolved = true;
                    }
                    else
                    {
                        // There are no normal attributes, so we can stop looking for them.  Note that this is different
                        // from having unresolved attributes (handled above).
                        this.normalAttributesResolved = true;
                    }
                }
            }
        }

        this.render(dc);
    }

    protected void determineActiveAttributes()
    {
        super.determineActiveAttributes();

        if (this.mustRefreshIcon())
        {
            String path = this.getActiveAttributes().getImageAddress();

            if (!WWUtil.isEmpty(path))
            {
                // Evict the resource from the file store if there is a cached resource older than the icon update
                // time. This prevents fetching a stale resource out of the cache when the Icon is updated.
                boolean highlighted = this.isHighlighted();
                this.parent.getRoot().evictIfExpired(path,
                    highlighted ? this.highlightIconRetrievalTime : this.iconRetrievalTime);
                this.textures.remove(path);
            }
        }
    }

    /**
     * Indicates whether or not the icon resource has expired.
     *
     * @return True if the icon has expired and must be refreshed.
     */
    protected boolean mustRefreshIcon()
    {
        String mode;
        long retrievalTime;

        if (this.isHighlighted())
        {
            mode = KMLConstants.HIGHLIGHT;
            retrievalTime = this.highlightIconRetrievalTime;
        }
        else
        {
            mode = KMLConstants.NORMAL;
            retrievalTime = this.iconRetrievalTime;
        }

        KMLIconStyle iconStyle = (KMLIconStyle) this.parent.getSubStyle(new KMLIconStyle(null), mode);
        KMLIcon icon = iconStyle.getIcon();
        return icon != null && icon.getUpdateTime() > retrievalTime;
    }

    /**
     * {@inheritDoc} Overridden to set the expiration time of the placemark's icon based on the HTTP headers of the
     * linked resource.
     */
    protected WWTexture initializeTexture(String address)
    {
        WWTexture texture = super.initializeTexture(address);
        if (texture != null)
        {
            // Query the KMLRoot for the expiration time.
            long expiration = this.parent.getRoot().getExpiration(address);

            // Set the Icon's expiration. This has no effect if the refreshMode is not onExpire.
            String mode = this.isHighlighted() ? KMLConstants.HIGHLIGHT : KMLConstants.NORMAL;
            KMLIconStyle iconStyle = (KMLIconStyle) this.parent.getSubStyle(new KMLIconStyle(null), mode);
            KMLIcon icon = iconStyle.getIcon();
            if (icon != null)
                icon.setExpirationTime(expiration);

            if (this.isHighlighted())
                this.highlightIconRetrievalTime = System.currentTimeMillis();
            else
                this.iconRetrievalTime = System.currentTimeMillis();
        }

        return texture;
    }

    /** {@inheritDoc} */
    @Override
    protected PickedObject createPickedObject(DrawContext dc, Color pickColor)
    {
        PickedObject po = super.createPickedObject(dc, pickColor);

        // Add the KMLPlacemark to the picked object as the context of the picked object.        
        po.setValue(AVKey.CONTEXT, this.parent);
        return po;
    }

    /**
     * Draw the label if the label scale is greater than the label scale threshold, if the image scale is zero (only the
     * text is rendered, there is no image), or if the placemark is highlighted.
     *
     * @return True if the label must be drawn.
     */
    @Override
    protected boolean mustDrawLabel()
    {
        double labelScale = this.getActiveAttributes().getLabelScale() != null
            ? this.getActiveAttributes().getLabelScale() : PointPlacemarkAttributes.DEFAULT_LABEL_SCALE;
        double imageScale = this.getActiveAttributes().getScale() != null
            ? this.getActiveAttributes().getScale() : PointPlacemarkAttributes.DEFAULT_IMAGE_SCALE;

        return this.isHighlighted() || labelScale >= this.getLabelScaleThreshold() || imageScale == 0;
    }

    /**
     * Determine and set the {@link PointPlacemark} attributes from the KML <i>Feature</i> fields.
     *
     * @param attrType the type of attributes, either {@link KMLConstants#NORMAL} or {@link KMLConstants#HIGHLIGHT}.
     *
     * @return The new attributes, or null if there are no attributes defined. Returns a partially empty attributes
     *         bundle marked unresolved if any of placemark KML styles are unresolved.
     */
    protected PointPlacemarkAttributes makeAttributesCurrent(String attrType)
    {
        boolean hasLineStyle = false;
        boolean hasIconStyle = false;
        boolean hasLabelStyle = false;

        PointPlacemarkAttributes attrs = this.getInitialAttributes(
            this.isHighlighted() ? KMLConstants.HIGHLIGHT : KMLConstants.NORMAL);

        // Get the KML sub-style for Line attributes. Map them to Shape attributes.

        KMLAbstractSubStyle subStyle = this.parent.getSubStyle(new KMLLineStyle(null), attrType);
        if (subStyle.hasFields() && (!this.isHighlighted() || KMLUtil.isHighlightStyleState(subStyle)))
        {
            hasLineStyle = true;
            this.assembleLineAttributes(attrs, (KMLLineStyle) subStyle);
            if (subStyle.hasField(AVKey.UNRESOLVED))
                attrs.setUnresolved(true);
        }

        subStyle = this.parent.getSubStyle(new KMLIconStyle(null), attrType);
        if (subStyle.hasFields() && (!this.isHighlighted() || KMLUtil.isHighlightStyleState(subStyle)))
        {
            hasIconStyle = true;
            this.assemblePointAttributes(attrs, (KMLIconStyle) subStyle);
            if (subStyle.hasField(AVKey.UNRESOLVED))
                attrs.setUnresolved(true);
        }

        subStyle = this.parent.getSubStyle(new KMLLabelStyle(null), attrType);
        if (subStyle.hasFields() && (!this.isHighlighted() || KMLUtil.isHighlightStyleState(subStyle)))
        {
            hasLabelStyle = true;
            this.assembleLabelAttributes(attrs, (KMLLabelStyle) subStyle);
            if (subStyle.hasField(AVKey.UNRESOLVED))
                attrs.setUnresolved(true);
        }

        // Return the attributes only if we actually found a KML style. If no style was found, return null instead of an
        // empty attributes bundle. If a style was found, but could not be resolved, we will return a partially empty
        // attributes bundle that is marked unresolved.
        if (hasLineStyle || hasIconStyle || hasLabelStyle)
            return attrs;
        else
            return null;
    }

    protected PointPlacemarkAttributes assemblePointAttributes(PointPlacemarkAttributes attrs, KMLIconStyle style)
    {
        KMLIcon icon = style.getIcon();
        if (icon != null && icon.getHref() != null)
        {
            // The icon reference may be to a support file within a KMZ file, so check for that. If it's not, then just
            // let the normal PointPlacemark code resolve the reference.
            String href = icon.getHref();
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
            attrs.setImageAddress((localAddress != null ? localAddress : href));
        }
        // If the Icon element is present, but there is no href, draw a point instead of the default icon.
        else if (icon != null && WWUtil.isEmpty(icon.getHref()))
        {
            attrs.setUsePointAsDefaultImage(true);
        }

        // Assign the other attributes defined in the KML Feature element.

        if (style.getColor() != null)
            attrs.setImageColor(WWUtil.decodeColorABGR(style.getColor()));

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setImageColor(WWUtil.makeRandomColor(attrs.getImageColor()));

        if (style.getScale() != null)
            attrs.setScale(style.getScale());

        if (style.getHeading() != null)
        {
            attrs.setHeading(style.getHeading());
            attrs.setHeadingReference(AVKey.RELATIVE_TO_GLOBE); // KML spec is not clear about this
        }

        if (style.getHotSpot() != null)
        {
            KMLVec2 hs = style.getHotSpot();
            attrs.setImageOffset(new Offset(hs.getX(), hs.getY(), KMLUtil.kmlUnitsToWWUnits(hs.getXunits()),
                KMLUtil.kmlUnitsToWWUnits(hs.getYunits())));
        }
        else
        {
            // By default, use the center of the image as the offset.
            attrs.setImageOffset(new Offset(0.5, 0.5, AVKey.FRACTION, AVKey.FRACTION));
        }

        return attrs;
    }

    protected PointPlacemarkAttributes assembleLineAttributes(PointPlacemarkAttributes attrs, KMLLineStyle style)
    {
        // Assign the attributes defined in the KML Feature element.

        if (style.getWidth() != null)
            attrs.setLineWidth(style.getWidth());

        if (style.getColor() != null)
            attrs.setLineColor(style.getColor());

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setLineMaterial(new Material(WWUtil.makeRandomColor(attrs.getLineColor())));

        return attrs;
    }

    protected PointPlacemarkAttributes assembleLabelAttributes(PointPlacemarkAttributes attrs, KMLLabelStyle style)
    {
        // Assign the attributes defined in the KML Feature element.

        if (style.getScale() != null)
            attrs.setLabelScale(style.getScale());

        if (style.getColor() != null)
            attrs.setLabelColor(style.getColor());

        if (style.getColorMode() != null && "random".equals(style.getColorMode()))
            attrs.setLabelMaterial(new Material(WWUtil.makeRandomColor(attrs.getLabelColor())));

        return attrs;
    }

    /**
     * Get the initial attributes for this feature. These attributes will be changed to reflect the feature's style.
     *
     * @param attrType {@link KMLConstants#NORMAL} or {@link KMLConstants#HIGHLIGHT}.
     *
     * @return New placemark attributes.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected PointPlacemarkAttributes getInitialAttributes(String attrType)
    {
        return new PointPlacemarkAttributes();
    }

    /**
     * Get the label scale threshold. The placemark label will be drawn if the label scale is greater than or equal to
     * this threshold, or if the placemark is highlighted.
     *
     * @return Label scale threshold.
     *
     * @see #setLabelScaleThreshold(double)
     */
    public double getLabelScaleThreshold()
    {
        return this.labelScaleThreshold;
    }

    /**
     * Set the label scale threshold. The placemark label will be drawn if the label scale is greater or equal to than
     * this threshold, or if the placemark is highlighted.
     *
     * @param labelScaleThreshold New label scale threshold.
     *
     * @see #getLabelScaleThreshold()
     */
    public void setLabelScaleThreshold(double labelScaleThreshold)
    {
        this.labelScaleThreshold = labelScaleThreshold;
    }

    @Override
    public void onMessage(Message message)
    {
        super.onMessage(message);

        if (KMLAbstractObject.MSG_STYLE_CHANGED.equals(message.getName()))
        {
            this.normalAttributesResolved = false;
            this.highlightAttributesResolved = false;

            if (this.getAttributes() != null)
                this.getAttributes().setUnresolved(true);
            if (this.getHighlightAttributes() != null)
                this.getHighlightAttributes().setUnresolved(true);
        }
    }
}
