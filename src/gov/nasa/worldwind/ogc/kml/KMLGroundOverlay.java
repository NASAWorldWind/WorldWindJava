/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.gx.GXLatLongQuad;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * Represents the KML <i>GroundOverlay</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLGroundOverlay.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLGroundOverlay extends KMLAbstractOverlay implements KMLRenderable
{
    protected KMLRenderable renderable;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLGroundOverlay(String namespaceURI)
    {
        super(namespaceURI);
    }

    public Double getAltitude()
    {
        return (Double) this.getField("altitude");
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }

    public KMLLatLonBox getLatLonBox()
    {
        return (KMLLatLonBox) this.getField("LatLonBox");
    }

    public GXLatLongQuad getLatLonQuad()
    {
        return (GXLatLongQuad) this.getField("LatLonQuad");
    }

    /**
     * Pre-renders the ground overlay geometry represented by this <code>KMLGroundOverlay</code>. This initializes the
     * ground overlay geometry if necessary, prior to pre-rendering.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doPreRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getRenderable() == null)
            this.initializeRenderable(tc);

        KMLRenderable r = this.getRenderable();
        if (r != null)
        {
            r.preRender(tc, dc);
        }
    }

    /**
     * Renders the ground overlay geometry represented by this <code>KMLGroundOverlay</code>.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doRender(KMLTraversalContext tc, DrawContext dc)
    {
        // We've already initialized the image renderable during the preRender pass. Render the image
        // without any further preparation.

        KMLRenderable r = this.getRenderable();
        if (r != null)
        {
            r.render(tc, dc);
        }

        // Render the feature balloon (if any)
        this.renderBalloon(tc, dc);
    }

    /**
     * Create the renderable that will represent the overlay.
     *
     * @param tc the current KML traversal context.
     */
    protected void initializeRenderable(KMLTraversalContext tc)
    {
        final String altitudeMode = this.getAltitudeMode();
        if ("absolute".equals(altitudeMode))
        {
            this.renderable = new KMLGroundOverlayPolygonImpl(tc, this);
        }
        else // Default to clampToGround
        {
            // If the overlay has an icon, create a surface image renderable. Otherwise, create a surface polygon to
            // render the overlay as a colored polygon.
            if (this.getIcon() != null && this.getIcon().getHref() != null)
                this.renderable = new KMLSurfaceImageImpl(tc, this);
            else
                this.renderable = new KMLSurfacePolygonImpl(tc, this);
        }
    }

    /**
     * Get the renderable that represents the screen overlay. The renderable is created the first time that the overlay
     * is rendered. Until then, the method will return null.
     *
     * @return The renderable, or null if the renderable has not been created yet.
     */
    public KMLRenderable getRenderable()
    {
        return this.renderable;
    }

    /**
     * Convenience method to get the positions defined by either {@code LatLonBox} or {@code gx:LatLonQuad}. If the
     * overlay includes a {@code LatLonBox} element, this method returns the corners of the sector defined by the {@code
     * LatLonBox}. Otherwise, if the overlay contains a {@code gx:LatLonQuad}, this method returns the positions defined
     * by the quad.
     *
     * @return A list of the positions that define the corner points of the overlay.
     */
    public Position.PositionList getPositions()
    {
        double altitude = this.getAltitude() != null ? this.getAltitude() : 0.0;

        // Positions are specified either as a kml:LatLonBox or a gx:LatLonQuad
        List<Position> corners = new ArrayList<Position>(4);
        KMLLatLonBox box = this.getLatLonBox();
        if (box != null)
        {
            Sector sector = KMLUtil.createSectorFromLatLonBox(box);
            for (LatLon ll : sector.getCorners())
            {
                corners.add(new Position(ll, altitude));
            }
        }
        else
        {
            GXLatLongQuad latLonQuad = this.getLatLonQuad();
            if (latLonQuad != null && latLonQuad.getCoordinates() != null)
            {
                for (Position position : latLonQuad.getCoordinates().list)
                {
                    corners.add(new Position(position, altitude));
                }
            }
        }

        return new Position.PositionList(corners);
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLGroundOverlay))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        this.renderable = null;

        super.applyChange(sourceValues);
    }

    @Override
    public void onChange(Message msg)
    {
        if (KMLAbstractObject.MSG_LINK_CHANGED.equals(msg.getName()))
            this.renderable = null;

        if (KMLAbstractObject.MSG_BOX_CHANGED.equals(msg.getName()))
            this.renderable = null;

        super.onChange(msg);
    }
}

