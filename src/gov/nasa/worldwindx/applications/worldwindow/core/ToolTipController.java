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

package gov.nasa.worldwindx.applications.worldwindow.core;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.applications.worldwindow.features.AbstractFeature;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

/**
 * @author tag
 * @version $Id: ToolTipController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ToolTipController extends AbstractFeature implements SelectListener
{
    protected Object lastRolloverObject;
    protected Object lastHoverObject;
    protected AnnotationLayer layer;
    protected ToolTipAnnotation annotation;

    public ToolTipController(Registry registry)
    {
        super("ToolTip Controller", Constants.FEATURE_TOOLTIP_CONTROLLER, registry);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.controller.getWWd().addSelectListener(this);
    }

    protected String getHoverText(SelectEvent event)
    {
        return event.getTopObject() != null && event.getTopObject() instanceof AVList ?
            ((AVList) event.getTopObject()).getStringValue(AVKey.HOVER_TEXT) : null;
    }

    protected String getRolloverText(SelectEvent event)
    {
        return event.getTopObject() != null && event.getTopObject() instanceof AVList ?
            ((AVList) event.getTopObject()).getStringValue(AVKey.ROLLOVER_TEXT) : null;
    }

    public void selected(SelectEvent event)
    {
        try
        {
            if (event.isRollover())
                this.handleRollover(event);
            else if (event.isHover())
                this.handleHover(event);
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void handleRollover(SelectEvent event)
    {
        if (this.lastRolloverObject != null)
        {
            if (this.lastRolloverObject == event.getTopObject() && !WWUtil.isEmpty(getRolloverText(event)))
                return;

            this.hideToolTip();
            this.lastRolloverObject = null;
            controller.redraw();
        }

        if (getRolloverText(event) != null)
        {
            this.lastRolloverObject = event.getTopObject();
            this.showToolTip(event, getRolloverText(event));
            controller.redraw();
        }
    }

    protected void handleHover(SelectEvent event)
    {
        if (this.lastHoverObject != null)
        {
            if (this.lastHoverObject == event.getTopObject())
                return;

            this.hideToolTip();
            this.lastHoverObject = null;
            controller.redraw();
        }

        if (getHoverText(event) != null)
        {
            this.lastHoverObject = event.getTopObject();
            this.showToolTip(event, getHoverText(event));
            controller.redraw();
        }
    }

    protected void showToolTip(SelectEvent event, String text)
    {
        if (annotation != null)
        {
            annotation.setText(text);
            annotation.setScreenPoint(event.getPickPoint());
        }
        else
        {
            annotation = new ToolTipAnnotation(text);
        }

        if (layer == null)
        {
            layer = new AnnotationLayer();
            layer.setPickEnabled(false);
        }

        layer.removeAllAnnotations();
        layer.addAnnotation(annotation);
        this.addLayer(layer);
    }

    protected void hideToolTip()
    {
        if (this.layer != null)
        {
            this.layer.removeAllAnnotations();
            this.removeLayer(this.layer);
            this.layer.dispose();
            this.layer = null;
        }

        if (this.annotation != null)
        {
            this.annotation.dispose();
            this.annotation = null;
        }
    }

    protected void addLayer(Layer layer)
    {
        if (!this.controller.getActiveLayers().contains(layer))
            this.controller.addInternalLayer(layer);
    }

    protected void removeLayer(Layer layer)
    {
        this.controller.getActiveLayers().remove(layer);
    }
}
