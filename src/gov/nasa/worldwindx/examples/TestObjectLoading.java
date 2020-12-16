/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.collada.*;
import gov.nasa.worldwind.ogc.collada.impl.*;
import gov.nasa.worldwind.layers.*;

public class TestObjectLoading extends ApplicationTemplate {

    public static class AppFrame extends ApplicationTemplate.AppFrame {

        public AppFrame() {
            super(true, true, false);

            WorldWindow wwd = this.getWwd();
            try {
                ColladaRoot planeModel = ColladaRoot.createAndParse("testData/collada/airliner.dae");
                planeModel.setPosition(new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 1500.0));
                planeModel.setModelScale(new Vec4(10, 10, 10));
                RenderableLayer layer = new RenderableLayer();
                wwd.getModel().getLayers().add(layer);
                layer.addRenderable(new ColladaController(planeModel));
                Position eyePos = new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 2000.0); // DFW
                wwd.getView().setEyePosition(eyePos);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        ApplicationTemplate.start("WorldWind Object Loading", AppFrame.class);
    }
}
