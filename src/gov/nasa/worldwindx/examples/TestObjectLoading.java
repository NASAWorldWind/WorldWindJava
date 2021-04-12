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
                ColladaRoot model = ColladaRoot.createAndParse("testData/collada/airliner.dae");
//                ColladaRoot model = ColladaRoot.createAndParse("testData/texture-cube.dae");
                // model.setPosition(new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 1500.0));
                Position eyePos = Position.fromDegrees(42.3638,-71.0607, 2000.0); // Boston
                model.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                model.setPosition(new Position(eyePos.latitude, eyePos.longitude,eyePos.elevation-1000));
                model.setModelScale(new Vec4(100, 100, 100));
                RenderableLayer layer = new RenderableLayer();
                wwd.getModel().getLayers().add(layer);
                layer.addRenderable(new ColladaController(model));
//                Position eyePos = new Position(Angle.fromDegreesLatitude(32.897), Angle.fromDegreesLongitude(-97.04), 2000.0); // DFW
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
