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
        ApplicationTemplate.start("WorldWind Placemark Label Editing", AppFrame.class);
    }
}
