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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;

/**
 * Illustrates how to use the {@link gov.nasa.worldwind.util.PlacemarkClutterFilter} to declutter PointPlacemark labels.
 * To enable this decluttering a filter has to be specified to the scene controller and each PointPlacemark that
 * participates in decluttering must be enabled for decluttering.
 * <p>
 * This example also enables label picking for all PointPlacemarks to illustrate that labels can be picked both when
 * they're not decluttered and when they are.
 *
 * @author tag
 * @version $Id: PlacemarkDecluttering.java 2388 2014-10-15 22:58:36Z tgaskins $
 */
public class PlacemarkDecluttering extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Specify the decluttering filter to the scene controller.
            this.getWwd().getSceneController().setClutterFilter(new PlacemarkClutterFilter());

            RenderableLayer layer = new RenderableLayer();

            PointPlacemark pp = new PointPlacemark(Position.fromDegrees(28, -102, 1e4));
            pp.setEnableDecluttering(true); // enable the placemark for decluttering
            pp.setEnableLabelPicking(true); // enable the placemark for label picking
            pp.setLabelText("Placemark A");
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Label, Semi-transparent, Audio icon");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/audioicon-64.png");
            attrs.setImageColor(new Color(1f, 1f, 1f, 0.6f));
            attrs.setScale(0.6);
//            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Place a default pin placemark at the same location over the previous one.
            pp = new PointPlacemark(pp.getPosition());
            pp.setEnableDecluttering(true);
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Default icon over audio icon");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(28, -104, 1e4));
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Audio icon, Heading 90, Screen relative");
            pp.setLabelText("Placemark B");
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setHeading(90d);
            attrs.setHeadingReference(AVKey.RELATIVE_TO_SCREEN);
            attrs.setScale(0.6);
            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Place a pin placemark at the same location over the previous one.
            pp = new PointPlacemark(pp.getPosition());
            pp.setEnableDecluttering(true);
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Default icon over rotated audio icon");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Use a new attributes instance.
            // Note that a new attributes instance must be created for every unique set of attribute values, although
            // the new attributes can be initialized from an existing attributes instance.
            pp = new PointPlacemark(Position.fromDegrees(29, -104, 2e4));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLabelText("Placemark C");
            pp.setValue(AVKey.DISPLAY_NAME, "Absolute, Label, Red pin icon, Line in random color and 2 wide");
            pp.setLineEnabled(true);
            pp.setAltitudeMode(WorldWind.ABSOLUTE);
            attrs = new PointPlacemarkAttributes();
            attrs.setScale(0.6);
            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            attrs.setLineMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setLineWidth(2d);
            attrs.setImageAddress("images/pushpins/plain-red.png");
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Create a placemark without a leader line.
            pp = new PointPlacemark(Position.fromDegrees(30, -104.5, 2e4));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLabelText("Placemark D");
            pp.setValue(AVKey.DISPLAY_NAME, "Relative to ground, Label, Teal pin icon, No line");
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageAddress("images/pushpins/plain-teal.png");
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Create a placemark clamped to ground.
            pp = new PointPlacemark(Position.fromDegrees(28, -104.5, 2e4));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLabelText("Placemark E");
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Blue label, White pin icon");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setLabelColor("ffff0000");
            attrs.setImageAddress("images/pushpins/plain-white.png");
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Create a placemark that uses all default values.
            pp = new PointPlacemark(Position.fromDegrees(30, -103.5, 2e3));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLabelText("Placemark F");
            pp.setValue(AVKey.DISPLAY_NAME, "All defaults");
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Create a placemark without an image.
            pp = new PointPlacemark(Position.fromDegrees(29, -104.5, 2e4));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLabelText("Placemark G");
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, White label, Red point, Scale 5");
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes();
            attrs.setLabelColor("ffffffff");
            attrs.setLineColor("ff0000ff");
            attrs.setUsePointAsDefaultImage(true);
            attrs.setScale(5d);
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Create a placemark off the surface and with a line.
            pp = new PointPlacemark(Position.fromDegrees(30, -104, 2e4));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setLabelText("Placemark H");
            pp.setValue(AVKey.DISPLAY_NAME, "Relative to ground, Blue label, Magenta point and line, Scale 10");
            pp.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            pp.setLineEnabled(true);
            attrs = new PointPlacemarkAttributes();
            attrs.setLabelColor("ffff0000");
            attrs.setLineMaterial(Material.MAGENTA);
            attrs.setLineWidth(2d);
            attrs.setUsePointAsDefaultImage(true);
            attrs.setScale(10d);
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            pp = new PointPlacemark(Position.fromDegrees(28, -103, 1e4));
            pp.setEnableDecluttering(true);
            pp.setEnableLabelPicking(true);
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Audio icon, Heading -45, Globe relative");
            pp.setLabelText("Placemark I");
            pp.setLineEnabled(false);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageAddress("gov/nasa/worldwindx/examples/images/audioicon-64.png");
            attrs.setHeading(-45d);
            attrs.setHeadingReference(AVKey.RELATIVE_TO_GLOBE);
            attrs.setScale(0.6);
//            attrs.setImageOffset(new Offset(0.5, 0.5, AVKey.FRACTION, AVKey.FRACTION));
            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setLabelColor("ffffffff");
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            this.setHighlightAttributes(pp);
            layer.addRenderable(pp);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Add a select listener in order to determine when a label is clicked on.
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
                                System.out.println(labelText);
                                event.consume();
                            }
                        }
                    }
                }
            });
        }

        protected void setHighlightAttributes(PointPlacemark pp)
        {
            // Change the label color to orange when the placemark is selected.
            PointPlacemarkAttributes highlightAttributes = new PointPlacemarkAttributes(pp.getAttributes());
            highlightAttributes.setLabelMaterial(Material.ORANGE);
            pp.setHighlightAttributes(highlightAttributes);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("WorldWind Placemark Decluttering", AppFrame.class);
    }
}
