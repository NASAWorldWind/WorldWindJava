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

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.io.IOException;

/**
 * Implementation of {@link Polygon} to render KML <i>GroundOverlay</i>.
 *
 * @author pabercrombie
 * @version $Id: KMLGroundOverlayPolygonImpl.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLGroundOverlayPolygonImpl extends Polygon implements KMLRenderable
{
    protected final KMLGroundOverlay parent;

    protected boolean attributesResolved;

    /**
     * Create an instance.
     *
     * @param tc      the current {@link KMLTraversalContext}.
     * @param overlay the {@link gov.nasa.worldwind.ogc.kml.KMLGroundOverlay} to render as a polygon.
     *
     * @throws NullPointerException     if the geomtry is null.
     * @throws IllegalArgumentException if the parent placemark or the traversal context is null.
     */
    public KMLGroundOverlayPolygonImpl(KMLTraversalContext tc, KMLGroundOverlay overlay)
    {
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

        this.parent = overlay;

        String altMode = overlay.getAltitudeMode();
        if (!WWUtil.isEmpty(altMode))
        {
            if ("relativeToGround".equals(altMode))
                this.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            else if ("absolute".equals(altMode))
            {
                this.setAltitudeMode(WorldWind.ABSOLUTE);
            }
        }

        // Positions are specified either as a kml:LatLonBox or a gx:LatLonQuad
        Position.PositionList corners = overlay.getPositions();
        this.setOuterBoundary(corners.list);

        // Apply rotation if the overlay includes a LatLonBox
        KMLLatLonBox box = overlay.getLatLonBox();
        if (box != null)
        {
            this.setRotation(box.getRotation());
        }

        if (overlay.getName() != null)
            this.setValue(AVKey.DISPLAY_NAME, overlay.getName());

        if (overlay.getDescription() != null)
            this.setValue(AVKey.BALLOON_TEXT, overlay.getDescription());

        if (overlay.getSnippetText() != null)
            this.setValue(AVKey.SHORT_DESCRIPTION, overlay.getSnippetText());

        // If no image is specified, draw a filled rectangle
        if (this.parent.getIcon() == null || this.parent.getIcon().getHref() == null)
        {
            String colorStr = overlay.getColor();
            if (!WWUtil.isEmpty(colorStr))
            {
                Color color = WWUtil.decodeColorABGR(colorStr);

                ShapeAttributes attributes = new BasicShapeAttributes();
                attributes.setDrawInterior(true);
                attributes.setInteriorMaterial(new Material(color));
                this.setAttributes(attributes);
            }
        }
    }

    /** {@inheritDoc} */
    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        // Intentionally left blank; KML polygon does nothing during the preRender phase.
    }

    protected boolean mustResolveHref()
    {
        return this.getTextureImageSource() == null
            && this.parent.getIcon() != null
            && this.parent.getIcon().getHref() != null;
    }

    /** {@inheritDoc} */
    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.mustResolveHref()) // resolve the href to either a local file or a remote URL
        {
            // The icon reference may be to a support file within a KMZ file, so check for that. If it's not, then just
            // let the normal Polygon code resolve the reference.
            String href = this.parent.getIcon().getHref();
            String localAddress = null;
            try
            {
                localAddress = this.parent.getRoot().getSupportFilePath(href);
            }
            catch (IOException ignored)
            {
            }

            float[] texCoords = new float[] {0, 0, 1, 0, 1, 1, 0, 1};
            this.setTextureImageSource((localAddress != null ? localAddress : href), texCoords, 4);
        }

        this.render(dc);
    }

    /** {@inheritDoc} */
    public boolean isHighlighted()
    {
        return false;
    }
}
