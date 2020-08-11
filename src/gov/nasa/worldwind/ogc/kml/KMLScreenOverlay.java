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

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * Represents the KML <i>ScreenOverlay</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLScreenOverlay.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class KMLScreenOverlay extends KMLAbstractOverlay
{
    protected KMLRenderable renderable;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLScreenOverlay(String namespaceURI)
    {
        super(namespaceURI);
    }

    public KMLVec2 getOverlayXY()
    {
        return (KMLVec2) this.getField("overlayXY");
    }

    public KMLVec2 getScreenXY()
    {
        return (KMLVec2) this.getField("screenXY");
    }

    public KMLVec2 getRotationXY()
    {
        return (KMLVec2) this.getField("rotationXY");
    }

    public KMLVec2 getSize()
    {
        return (KMLVec2) this.getField("size");
    }

    public Double getRotation()
    {
        return (Double) this.getField("rotation");
    }

    /**
     * Pre-renders the screen overlay geometry represented by this <code>KMLScreenOverlay</code>. This initializes the
     * screen overlay geometry if necessary, prior to pre-rendering.
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
     * Renders the screen overlay geometry represented by this <code>KMLScreenOverlay</code>.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doRender(KMLTraversalContext tc, DrawContext dc)
    {
        // We've already initialized the screen image renderable during the preRender pass. Render the screen image
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
        renderable = new KMLScreenImageImpl(tc, this);
    }

    /**
     * Get the renderable that represents the screen overlay. The renderable is created the first time that the overlay
     * is rendered. Until then, the method will return null.
     *
     * @return The renderable, or null if the renderable has not been created yet.
     */
    public KMLRenderable getRenderable()
    {
        return renderable;
    }

    @Override
    public void applyChange(KMLAbstractObject sourceValues)
    {
        if (!(sourceValues instanceof KMLScreenOverlay))
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

        super.onChange(msg);
    }
}
