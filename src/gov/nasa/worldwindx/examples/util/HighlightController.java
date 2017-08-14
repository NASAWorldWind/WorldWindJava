/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples.util;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

/**
 * Controls highlighting of shapes implementing {@link Highlightable} in response to pick events. Monitors a specified
 * WorldWindow for an indicated {@link gov.nasa.worldwind.event.SelectEvent} type and turns highlighting on and off in
 * response.
 *
 * @author tag
 * @version $Id: HighlightController.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class HighlightController implements SelectListener
{
    protected WorldWindow wwd;
    protected Object highlightEventType = SelectEvent.ROLLOVER;
    protected Highlightable lastHighlightObject;

    /**
     * Creates a controller for a specified WorldWindow.
     *
     * @param wwd                the WorldWindow to monitor.
     * @param highlightEventType the type of {@link SelectEvent} to highlight in response to. The default is {@link
     *                           SelectEvent#ROLLOVER}.
     */
    public HighlightController(WorldWindow wwd, Object highlightEventType)
    {
        this.wwd = wwd;
        this.highlightEventType = highlightEventType;

        this.wwd.addSelectListener(this);
    }

    public void dispose()
    {
        this.wwd.removeSelectListener(this);
    }

    public void selected(SelectEvent event)
    {
        try
        {
            if (this.highlightEventType != null && event.getEventAction().equals(this.highlightEventType))
                highlight(event.getTopObject());
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void highlight(Object o)
    {
        if (this.lastHighlightObject == o)
            return; // same thing selected

        // Turn off highlight if on.
        if (this.lastHighlightObject != null)
        {
            this.lastHighlightObject.setHighlighted(false);
            this.lastHighlightObject = null;
        }

        // Turn on highlight if object selected.
        if (o instanceof Highlightable)
        {
            this.lastHighlightObject = (Highlightable) o;
            this.lastHighlightObject.setHighlighted(true);
        }
    }
}
