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
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.layers.Layer;

import java.awt.*;

/**
 * @author dcollins
 * @version $Id: AirspaceEditor.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public interface AirspaceEditor extends Layer
{
    Airspace getAirspace();

    boolean isArmed();

    void setArmed(boolean armed);

    boolean isUseRubberBand();

    void setUseRubberBand(boolean state);

    boolean isKeepControlPointsAboveTerrain();

    void setKeepControlPointsAboveTerrain(boolean state);

    AirspaceControlPointRenderer getControlPointRenderer();

    void setControlPointRenderer(AirspaceControlPointRenderer renderer);

    AirspaceEditListener[] getEditListeners();

    void addEditListener(AirspaceEditListener listener);

    void removeEditListener(AirspaceEditListener listener);

    // TODO
    // the purposes of these methods may be okay, but there are some obvious problems:
    //
    // 1. any change in parameters would require a signature change (params should be bundled)
    //
    // 2. they do not allow the editor any control over how to respond to input
    //
    // 3. they assume the editor can do something reasonable with the call

    void moveAirspaceLaterally(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint);

    void moveAirspaceVertically(WorldWindow wwd, Airspace airspace,
        Point mousePoint, Point previousMousePoint);

    AirspaceControlPoint addControlPoint(WorldWindow wwd, Airspace airspace, 
        Point mousePoint);

    void removeControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint);

    void moveControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint);

    void resizeAtControlPoint(WorldWindow wwd, AirspaceControlPoint controlPoint,
        Point mousePoint, Point previousMousePoint);
}
