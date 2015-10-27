/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwindx.examples.ApplicationTemplate;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.*;

/**
 * Controls display of tool tips on picked objects. Any shape implementing {@link AVList} can participate. Shapes
 * provide tool tip text in their AVList for either or both of hover and rollover events. The keys associated with the
 * text are specified to the constructor.
 *
 * @author tag
 * @version $Id: ToolTipController.java 2365 2014-10-02 23:15:16Z tgaskins $
 */
public class ToolTipController implements SelectListener, Disposable
{
    protected WorldWindow wwd;
    protected String hoverKey = AVKey.HOVER_TEXT;
    protected String rolloverKey = AVKey.ROLLOVER_TEXT;
    protected Object lastRolloverObject;
    protected Object lastHoverObject;
    protected AnnotationLayer layer;
    protected ToolTipAnnotation annotation;

    /**
     * Create a controller for a specified {@link WorldWindow} that displays tool tips on hover and/or rollover.
     *
     * @param wwd         the World Window to monitor.
     * @param rolloverKey the key to use when looking up tool tip text from the shape's AVList when a rollover event
     *                    occurs. May be null, in which case a tool tip is not displayed for rollover events.
     * @param hoverKey    the key to use when looking up tool tip text from the shape's AVList when a hover event
     *                    occurs. May be null, in which case a tool tip is not displayed for hover events.
     */
    public ToolTipController(WorldWindow wwd, String rolloverKey, String hoverKey)
    {
        this.wwd = wwd;
        this.hoverKey = hoverKey;
        this.rolloverKey = rolloverKey;

        this.wwd.addSelectListener(this);
    }

    /**
     * Create a controller for a specified {@link WorldWindow} that displays "DISPLAY_NAME" on rollover.
     *
     * @param wwd         the World Window to monitor.
     */
    public ToolTipController(WorldWindow wwd)
    {
        this.wwd = wwd;
        this.rolloverKey = AVKey.DISPLAY_NAME;

        this.wwd.addSelectListener(this);
    }

    public void dispose()
    {
        this.wwd.removeSelectListener(this);
    }

    protected String getHoverText(SelectEvent event)
    {
        return event.getTopObject() != null && event.getTopObject() instanceof AVList ?
            ((AVList) event.getTopObject()).getStringValue(this.hoverKey) : null;
    }

    protected String getRolloverText(SelectEvent event)
    {
        return event.getTopObject() != null && event.getTopObject() instanceof AVList ?
            ((AVList) event.getTopObject()).getStringValue(this.rolloverKey) : null;
    }

    public void selected(SelectEvent event)
    {
        try
        {
            if (event.isRollover() && this.rolloverKey != null)
                this.handleRollover(event);
            else if (event.isHover() && this.hoverKey != null)
                this.handleHover(event);
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
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
            this.wwd.redraw();
        }

        if (getRolloverText(event) != null)
        {
            this.lastRolloverObject = event.getTopObject();
            this.showToolTip(event, getRolloverText(event).replace("\\n", "\n"));
            this.wwd.redraw();
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
            this.wwd.redraw();
        }

        if (getHoverText(event) != null)
        {
            this.lastHoverObject = event.getTopObject();
            this.showToolTip(event, getHoverText(event).replace("\\n", "\n"));
            this.wwd.redraw();
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
            layer.setValue(AVKey.IGNORE, true);
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
        if (!this.wwd.getModel().getLayers().contains(layer))
            ApplicationTemplate.insertBeforeCompass(this.wwd, layer);
    }

    protected void removeLayer(Layer layer)
    {
        this.wwd.getModel().getLayers().remove(layer);
    }
}
