/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.util.BasicDragger;

import javax.swing.*;

/**
 * Shows how to edit a PointPlacemark's label when the user left-clicks on the label.
 *
 * @author tag
 * @version $Id: PlacemarkLabelEditing.java 2379 2014-10-11 17:59:47Z tgaskins $
 */
public class PlacemarkLabelEditing extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Create a layer for the placemark.
            final RenderableLayer layer = new RenderableLayer();

            // Create a placemark that uses a 2525C tactical symbol. The symbol is downloaded from the internet on a
            // separate thread.
            WorldWind.getTaskService().addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    // Use the function in the Placemarks example to create a tactical symbol placemark.
                    Placemarks.createTacticalSymbolPointPlacemark(layer);
                }
            });

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Add a dragger so the user can relocate the placemark.
            this.getWwd().addSelectListener(new BasicDragger(this.getWwd()));

            // Add a select listener in order to determine when the label is selected.
            this.getWwd().addSelectListener(new SelectListener()
            {
                @Override
                public void selected(SelectEvent event)
                {
                    PickedObject po = event.getTopPickedObject();
                    if (po != null && po.getObject() instanceof PointPlacemark)
                    {
                        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                        {
                            // See if it was the label that was picked. If so, raise an input dialog prompting
                            // for new label text.
                            Object placemarkPiece = po.getValue(AVKey.PICKED_OBJECT_ID);
                            if (placemarkPiece != null && placemarkPiece.equals(AVKey.LABEL))
                            {
                                PointPlacemark placemark = (PointPlacemark) po.getObject();
                                String labelText = placemark.getLabelText();
                                labelText = JOptionPane.showInputDialog(null, "Enter label text", labelText);
                                if (labelText != null)
                                {
                                    placemark.setLabelText(labelText);
                                }
                                event.consume();
                            }
                        }
                    }
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Placemark Label Editing", AppFrame.class);
    }
}
