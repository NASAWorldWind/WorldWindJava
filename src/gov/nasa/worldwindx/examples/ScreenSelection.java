/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Demonstrates how to use the {@link gov.nasa.worldwindx.examples.util.ScreenSelector} utility to perform
 * multiple-object selection in screen space.
 *
 * @author dcollins
 * @version $Id: ScreenSelection.java 2109 2014-06-30 16:52:38Z tgaskins $
 */
public class ScreenSelection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ScreenSelector screenSelector;
        protected SelectionHighlightController selectionHighlightController;

        public AppFrame()
        {
            // Create a screen selector to display a screen selection rectangle and track the objects intersecting
            // that rectangle.
            this.screenSelector = new ScreenSelector(this.getWwd());

            // Set up a custom highlight controller that highlights objects both under the cursor and inside the
            // selection rectangle. Disable the superclass' default highlight controller to prevent it from interfering
            // with our highlight controller.
            this.selectionHighlightController = new SelectionHighlightController(this.getWwd(), this.screenSelector);
            this.getWwjPanel().highlightController.dispose();

            // Create a button to enable and disable screen selection.
            JButton btn = new JButton(new EnableSelectorAction());
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.add(btn, BorderLayout.CENTER);
            this.getControlPanel().add(panel, BorderLayout.SOUTH);

            // Create layer of highlightable shapes to select.
            this.addShapes();
        }

        protected void addShapes()
        {
            RenderableLayer layer = new RenderableLayer();

            ShapeAttributes highlightAttrs = new BasicShapeAttributes();
            highlightAttrs.setInteriorMaterial(Material.RED);
            highlightAttrs.setOutlineMaterial(Material.WHITE);

            for (int lon = -180; lon <= 170; lon += 10)
            {
                for (int lat = -60; lat <= 60; lat += 10)
                {
                    ExtrudedPolygon poly = new ExtrudedPolygon(Arrays.asList(
                        LatLon.fromDegrees(lat - 1, Angle.normalizedDegreesLongitude(lon - 1)),
                        LatLon.fromDegrees(lat - 1, Angle.normalizedDegreesLongitude(lon + 1)),
                        LatLon.fromDegrees(lat + 1, Angle.normalizedDegreesLongitude(lon + 1)),
                        LatLon.fromDegrees(lat + 1, Angle.normalizedDegreesLongitude(lon - 1))),
                        100000d);
                    poly.setHighlightAttributes(highlightAttrs);
                    poly.setSideHighlightAttributes(highlightAttrs);
                    layer.addRenderable(poly);
                }
            }

            this.getWwd().getModel().getLayers().add(layer);
        }

        protected class EnableSelectorAction extends AbstractAction
        {
            public EnableSelectorAction()
            {
                super("Start");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new DisableSelectorAction());
                screenSelector.enable();
            }
        }

        protected class DisableSelectorAction extends AbstractAction
        {
            public DisableSelectorAction()
            {
                super("Stop");
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                ((JButton) actionEvent.getSource()).setAction(new EnableSelectorAction());
                screenSelector.disable();
            }
        }
    }

    /**
     * Extends HighlightController to add the capability to highlight objects selected by a ScreenSelector. This tracks
     * objects highlighted by both cursor rollover events and screen selection changes, and ensures that objects stay
     * highlighted when they are either under cursor or in the ScreenSelector's selection rectangle.
     */
    protected static class SelectionHighlightController extends HighlightController implements MessageListener
    {
        protected ScreenSelector screenSelector;
        protected List<Highlightable> lastBoxHighlightObjects = new ArrayList<Highlightable>();

        public SelectionHighlightController(WorldWindow wwd, ScreenSelector screenSelector)
        {
            super(wwd, SelectEvent.ROLLOVER);

            this.screenSelector = screenSelector;
            this.screenSelector.addMessageListener(this);
        }

        @Override
        public void dispose()
        {
            super.dispose();

            this.screenSelector.removeMessageListener(this);
        }

        public void onMessage(Message msg)
        {
            try
            {
                // Update the list of highlighted objects whenever the ScreenSelector's selection changes. We capture
                // both the selection started and selection changed events to ensure that we clear the list of selected
                // objects when the selection begins or re-starts, as well as update the list when it changes.
                if (msg.getName().equals(ScreenSelector.SELECTION_STARTED)
                    || msg.getName().equals(ScreenSelector.SELECTION_CHANGED))
                {
                    this.highlightSelectedObjects(this.screenSelector.getSelectedObjects());
                }
            }
            catch (Exception e)
            {
                // Wrap the handler in a try/catch to keep exceptions from bubbling up
                Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
            }
        }

        protected void highlight(Object o)
        {
            // Determine if the highlighted object under the cursor has changed, but should remain highlighted because
            // its in the selection box. In this case we assign the highlighted object under the cursor to null and
            // return, and thereby avoid changing the highlight state of objects still highlighted by the selection box.
            if (this.lastHighlightObject != o && this.lastBoxHighlightObjects.contains(this.lastHighlightObject))
            {
                this.lastHighlightObject = null;
                return;
            }

            super.highlight(o);
        }

        protected void highlightSelectedObjects(List<?> list)
        {
            if (this.lastBoxHighlightObjects.equals(list))
                return; // same thing selected

            // Turn off highlight for the last set of selected objects, if any. Since one of these objects may still be
            // highlighted due to a cursor rollover, we detect that object and avoid changing its highlight state.
            for (Highlightable h : this.lastBoxHighlightObjects)
            {
                if (h != this.lastHighlightObject)
                    h.setHighlighted(false);
            }
            this.lastBoxHighlightObjects.clear();

            if (list != null)
            {
                // Turn on highlight if object selected.
                for (Object o : list)
                {
                    if (o instanceof Highlightable)
                    {
                        ((Highlightable) o).setHighlighted(true);
                        this.lastBoxHighlightObjects.add((Highlightable) o);
                    }
                }
            }

            // We've potentially changed the highlight state of one or more objects. Request that the world window
            // redraw itself in order to refresh these object's display. This is necessary because changes in the
            // objects in the pick rectangle do not necessarily correspond to mouse movements. For example, the pick
            // rectangle may be cleared when the user releases the mouse button at the end of a drag. In this case,
            // there's no mouse movement to cause an automatic redraw.
            this.wwd.redraw();
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Screen Selection", AppFrame.class);
    }
}
